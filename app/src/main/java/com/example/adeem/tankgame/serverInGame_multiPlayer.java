package com.example.adeem.tankgame;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
//import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action; //you dont have the import in the library
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import classes.Bullet;
import classes.MyPoint;
import classes.Tank;

public class serverInGame_multiPlayer extends AppCompatActivity  implements View.OnClickListener, SensorEventListener {


    private int clientCount = 1; // server is number 0
    private ArrayList<Tank> tankArry = new ArrayList<Tank>();
    private Boolean gameRuning = true;
    private ArrayList<Thread> ClienThreads = new ArrayList<Thread>();
    private serverInGame_multiPlayer contex = this;

    private final double EPSILON = 0.000001;
   // private int playerNum = 1;


    //share preferences for player name - if to be stored in firebase
    private SharedPreferences prefs;
    private String my_pref_name;
    private String SHuserName;
    private String UserName;
    private TextView GameOvertxt;

    //all the sensor objects used to detect movement
    private SensorManager sManager;
    AbsoluteLayout rlayout;
    //calculation
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;


    // the tank to be displayed as ours
    private ImageButton ourTank;
    private TextView test; ////// fot testing only (debugging pupposes)
    private ImageView backGround;

    Socket socket;
    ServerSocket server;
    //IDs related to shooting and boolean variable indicating if shooting or not
    private int[] blts = {
            R.id.blt1,
            R.id.blt2,
            R.id.blt3,
            R.id.blt4,
            R.id.blt5,
            R.id.blt6,
            R.id.blt7,
            R.id.blt8
    };
    boolean amIShooting = false;
    ArrayList<ImageView> bullets = new ArrayList<>();

    //IDs relating to other playes tanks
    private int[] imgIds = {
            R.id.tank_1,
            R.id.tank_2,
            R.id.tank_3
    };
    int enemiesNum = 0;
    ArrayList<ImageView> enemiesTanks = new ArrayList<>();

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_in_game);
        ImageView temp;


        conectToClient();
        Resources res = getResources();

        my_pref_name = res.getString(R.string.SharedPreferencesPrefsName);
        SHuserName = res.getString(R.string.SharedPreferencesUserName);
        prefs = getSharedPreferences(my_pref_name, MODE_PRIVATE);
        UserName = prefs.getString(SHuserName, null); ////// NOT USED
        ourTank = (ImageButton) findViewById(R.id.ourTank_server2);
        //for debugging only
        test = (TextView) findViewById(R.id.log_server);

        for(int i =0 ; i < blts.length ; i++){ // set all bullets
            temp = (ImageView) findViewById(blts[i]);
            bullets.add(temp);
        }


        rlayout = (AbsoluteLayout) findViewById(R.id.activity_server_in_game);
        backGround = (ImageView) findViewById(R.id.limitsView_server2);
        GameOvertxt = (TextView) findViewById(R.id.gameOver_viewServer);

        tankArry .add(new Tank(null));
        tankArry .add(new Tank(null));



        /////////////////////////////////////////////////
        //initialize sensor
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sManager.registerListener(this,
                sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);


        ourTank.setOnClickListener(this);
        rlayout.setOnClickListener(this);

        //on screen  touch get coordinates and change the tank aim accordingly
        rlayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        double alpha1;
                        double Xs = Math.pow(event.getX() - ourTank.getX(), 2);
                        double Ys = Math.pow(event.getY() - ourTank.getY(), 2);
                        double sqrt = Math.sqrt(Xs + Ys);
                        alpha1 = Math.asin((event.getY() - ourTank.getY()) / sqrt);
                        alpha1 = alpha1 * 180 / Math.PI;
                        if (ourTank.getX() <= event.getX())
                            alpha1 += 90;
                        else {
                            alpha1 = 270 - alpha1;
                        }
                        ourTank.setRotation((float) alpha1);
                        Lock lock = new ReentrantLock();
                        synchronized (lock) {
                            tankArry.get(0).setheadingAngle((float) alpha1);
                        }
                    }
                    return true;
                }
            });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.44444
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        //test.setText("SERVER");
    }



    /////////////////////////////////////for debugging purposes //////////////////////////////////
    @Override
    public void onClick(View view) {
        int buttonId = view.getId();
        switch (buttonId) {

            case (R.id.ourTank_server2): {
                Lock lock = new ReentrantLock();

                //make bullet and make it shoot
                Bullet bullet_ = new Bullet(tankArry,0);
                //add bullet thread to display the bullet \
                boolean found = false;

                while(!found) // look for a free bullet to fire
                    for(int i =0 ; i < contex.bullets.size() ; i++){
                        synchronized (lock){
                            if(contex.bullets.get(i).getVisibility()== View.GONE){
                                MyPoint P = new MyPoint((int)ourTank.getX(),(int)ourTank.getY());
                                bulletThread bt = new bulletThread(P,ourTank.getRotation(),i);
                                bt.start();
                                found = true;
                                break;
                            }
                        }
                    }
                    this.tankArry = (ArrayList<Tank>) bullet_.shoot();
                    float headingAngle = tankArry.get(0).getheadingAngle();
                    if(headingAngle <0) headingAngle = 360 + headingAngle;
                    test.setText(tankArry.get(0).getheadingAngle() +" , " + headingAngle);
                    amIShooting = true; // set amIShooting so the client well recive the info
                }
                break;
        }
    }



    //movement detected update the server
    //sending to server the movement and getting the places of the other tanks - (update the positions of the tanks accordingly)
    //on sensor change listener
    public void onSensorChanged(SensorEvent event) {

        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            // Axis of the rotation sample, not normalized yet.
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];
            // Calculate the angular speed of the sample
            float omegaMagnitude = (float) Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

            // Normalize the rotation vector if it's big enough to get the axis
            // (that is, EPSILON should represent your maximum allowable margin of error)
            if (omegaMagnitude > EPSILON) {
                axisX /= omegaMagnitude;
                axisY /= omegaMagnitude;
                axisZ /= omegaMagnitude;
            }

            float thetaOverTwo = omegaMagnitude * dT / 2.0f;
            float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
            float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
            deltaRotationVector[0] = sinThetaOverTwo * axisX;
            deltaRotationVector[1] = sinThetaOverTwo * axisY;
            deltaRotationVector[2] = sinThetaOverTwo * axisZ;
            deltaRotationVector[3] = cosThetaOverTwo;

        }
        timestamp = event.timestamp;
        float[] deltaRotationMatrix = new float[9];


        SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);

        //check if tank is out of border
        boolean moveY;
        if (deltaRotationVector[1] > 0)//check tank direction on Y axis
            moveY = (backGround.getHeight() - Math.abs(backGround.getY())) > ourTank.getY() + 130;
        else moveY = (backGround.getY() < ourTank.getY());
        boolean moveX;
        if (deltaRotationVector[0] < 0)//check tank direction on X axis
            moveX = (backGround.getWidth() - Math.abs(backGround.getX())) > ourTank.getX() + 130;
        else moveX = (backGround.getX() < ourTank.getX());
        if (moveX)
            backGround.setX(backGround.getX() + deltaRotationVector[0] * 50);// tank going left = delta >0 , tank going right delta < 0
        if (moveY)
            backGround.setY(backGround.getY() - deltaRotationVector[1] * 50);// tank going up = delta>0, tank going down delta < 0


        MyPoint movement = new MyPoint((int)(ourTank.getX() - backGround.getX()),(int)( ourTank.getY() - backGround.getY()));
        Lock lock = new ReentrantLock();
        synchronized (lock) {
            tankArry.get(0).setPosition(movement); // update position so clients well recive it
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    //When this Activity isn't visible anymore
    @Override
    protected void onStop() {



        //unregister the sensor listener
        sManager.unregisterListener(this);
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

        gameRuning = false;
        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
        try {
            Thread.sleep(100); //waiting 0.1 seconds for host to set up his socket server before connecting.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        wifiManager.setWifiEnabled(true);



        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        //Do nothing.
    }


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Client_Ingame_trial Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }


    public void conectToClient(){ // conect to clients and run a client_Listener for evry one of tham

        ImageView img ;
        try {
                server = new ServerSocket(WiFiDirectReceiver.PORT);//, 1);
                socket = server.accept();
                client_Listener cl = new client_Listener(socket);
                ClienThreads.add(cl);
                cl.start();
                // display the tamk
                img = (ImageView) findViewById(this.imgIds[enemiesNum]);
                this.enemiesTanks.add(img);
                this.enemiesTanks.get(enemiesNum).setVisibility(View.VISIBLE);
                this.enemiesNum++;
                server.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {

        gameRuning = false;
        //ClienThreads.get(0).interrupt();

        WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(false);
        try {
            Thread.sleep(100); //waiting 0.1 seconds for host to set up his socket server before connecting.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        wifiManager.setWifiEnabled(true);
        exit();

        super.onBackPressed();
    }
    private void closeStreams(ObjectInputStream input,ObjectOutputStream output,Socket soket){
        try{
            input.close();
            output.close();
            soket.close();
            socket.close();
        }
        catch (Exception Exception){
            Exception.printStackTrace();
        }
    }
    private void displayToast(final String toDisplay){
            runOnUiThread(new Runnable() {
                public void run()
                {
                    Toast.makeText(serverInGame_multiPlayer.this, toDisplay, Toast.LENGTH_SHORT).show();
                }
            });

    }

    private void exit(){
        // sendMessage(EXIT);
        displayToast("calling EXIT");
        finish();
    }
    private class client_Listener extends Thread { // class privieds a thread that updates fieds for the main GUI thread acording to inf it recives from the clients
        // also, this threads sends info about the game and other players to the client ( one thred per client is needed)

        private Socket socket;
        private ObjectOutputStream outputToClient;
        private ObjectInputStream inputFromClient;
        private int clientNum = 0;
        private MyPoint position = new MyPoint(0,0);
        private Boolean shooting = false;
        private Lock lock = new ReentrantLock();
        private float rotation_;
        private boolean iwon = false;

        public client_Listener(Socket socket) {
            this.socket = socket;
            clientNum = clientCount++;
            try {
                outputToClient = new ObjectOutputStream(this.socket.getOutputStream());
                inputFromClient = new ObjectInputStream(this.socket.getInputStream());
            } catch (IOException ioException) {
                //   displayToast("Server Ended the connection.");
                   exit();
            }
        }
        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            // send client his number
            try {
            outputToClient.writeInt(clientNum);
            outputToClient.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (gameRuning) {
                try {
                        // get update from client
                        int x  = inputFromClient.readInt();
                        int y = inputFromClient.readInt();
                        rotation_ = inputFromClient.readFloat();
                        shooting =  inputFromClient.readBoolean();

                        //
                        //read the input from the clients
                        position = new MyPoint(x,y);
                        tankArry.get(clientNum).setPosition(position);
                        tankArry.get(clientNum).setheadingAngle(rotation_);
                        if (shooting) {
                            synchronized (lock) {
                                //make bullet and make it shoot
                                Bullet bullet = new Bullet(tankArry, clientNum);
                                tankArry = (ArrayList<Tank>) bullet.shoot();

                                //!!!!!!!!!!!!!!!!!!make a new bullet!!!!!!!!!!!!!!!!
                                boolean found = false;
                                while(!found)
                                    for(int i =0 ; i < contex.bullets.size() ; i++){
                                        if(contex.bullets.get(i).getVisibility()== View.GONE){
                                            bulletThread th = new bulletThread(tankArry.get(clientNum).getPosition(), tankArry.get(clientNum).getheadingAngle(),i);
                                            th.start();
                                            found = true;
                                            break;
                                        }
                                    }

                                contex.runOnUiThread(new Runnable() { // update tanks on the screen
                                    @Override
                                    public void run() {
                                        if(tankArry.get(0).getShot()){
                                            contex.ourTank.setImageResource(R.drawable.fire);
                                            gameRuning = false;
                                        }
                                    }
                                });
                            }
                        }
                        contex.runOnUiThread(new Runnable(){ // update tanks on the screen
                            @Override
                            public void run(){ // update players tank on the screen
                                //enemiesTanks = ImageViews
                                contex.enemiesTanks.get(clientNum-1).setX(position.x + contex.backGround.getX());
                                contex.enemiesTanks.get(clientNum-1).setY(position.y + contex.backGround.getY());
                                contex.enemiesTanks.get(clientNum-1).setRotation(rotation_);
                                if(tankArry.get(clientNum).getShot() == true) {
                                    contex.enemiesTanks.get(clientNum - 1).setImageResource(R.drawable.fire);
                                    iwon = true;
                                    //contex.test.setText("\n"+clientNum +"was shot");//try it now . if we get s
                                    gameRuning = false;
                                }
                            }
                        });
                    //update clients
                    outputToClient.writeBoolean(contex.amIShooting);
                    contex.amIShooting = false;
                    for(int i =0 ; i < tankArry.size()  ; i ++ ) {
                        outputToClient.writeInt(tankArry.get(i).getPosition().x);
                        outputToClient.flush();
                        outputToClient.writeInt(tankArry.get(i).getPosition().y);
                        outputToClient.flush();
                        outputToClient.writeFloat(tankArry.get(i).getheadingAngle());
                        outputToClient.flush();
                        //if the client was shot or not  - in each client if that person was shot make the visibily gone
                        outputToClient.writeBoolean(tankArry.get(i).getShot());
                        outputToClient.flush();
                    }

                } catch (Exception e) {
                    displayToast("IN exception, calling close ");
                    break;

                }
            }
            contex.runOnUiThread(new Runnable(){ // update tanks on the screen
                @Override
                public void run(){ // update players tank on the screen
                    if(contex.gameRuning )
                         contex.test.setText("the other player had left the game");
                    else {
                        contex.GameOvertxt.setVisibility(View.VISIBLE);
                        contex.GameOvertxt.append(iwon == true ? " WIN!" : " LOSE!");
                    }


                }
            });
            try {
                socket.close();
                inputFromClient.close();
                outputToClient.close();

            }catch (Exception e1){}

            //closeStreams(inputFromClient,outputToClient,socket);


            contex.gameRuning=false;
        }
    }

    private class bulletThread extends Thread { // this thread displayes and moves the bullet acoding to parameters

        private int index_;
        private Timer t_ = new Timer();
        private double x1,y1;
        private float angle ;
        private Lock lock = new ReentrantLock();

        public bulletThread(MyPoint position, final float angle_, int index){

            y1 = position.y;
            x1 =position.x;
            angle =  (angle_-90);
            index_ = index;
            contex.runOnUiThread(new Runnable(){ // update tanks on the screen
                @Override
                public void run(){
                    //enemiesTanks = ImageViews
                    synchronized (lock) {
                        contex.bullets.get(index_).setVisibility(View.VISIBLE);
                        contex.bullets.get(index_).setRotation(angle_);
                    }
                }
            });
        }
        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            t_.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    contex.runOnUiThread(new Runnable(){ // update bullet  on the screen
                        @Override
                        public void run(){
                            // clac new location
                            x1 = x1 + (5*Math.cos(angle* (Math.PI / 180)));
                            y1 = y1 + (5*Math.sin(angle* (Math.PI / 180)));
                            synchronized (lock) { // update location

                                if (x1 > 0 && x1 < contex.backGround.getHeight() && y1 > 0 && y1 < contex.backGround.getWidth()) {
                                    contex.bullets.get(index_).setX((int) x1);
                                    contex.bullets.get(index_).setY((int) y1);
                                } else { // if out of map
                                    contex.bullets.get(index_).setVisibility(View.GONE);
                                    t_.cancel();
                                }
                            }
                        }
                    });
                }
            }, 0, 3);
        }
    }

}


