package com.example.adeem.tankgame;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import classes.MyPoint;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class clientInGame extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

        //the socketOuput and input streams , no need to initialize atm
        private ObjectInputStream input = null;
        private ObjectOutputStream output = null;
        private Socket connection;

        private MyPoint myMovement = new MyPoint(0,0);

        private ArrayList<ImageView> myenemy = new ArrayList<ImageView>();
        private WifiP2pInfo wifiP2pInfo;
        private int playerAmount = 2;    // change this if more than 2 players are wanted ( upgrade option)



        //sensor changes and tank movements
        private SensorManager sManager;
        private final double EPSILON = 0.000001;
        //calculation
        private static final float NS2S = 1.0f / 1000000000.0f;
        private final float[] deltaRotationVector = new float[4];
        private float timestamp;
        float RotationAngle;
        private ImageView backGround;


        //our tank
        private ImageButton ourTank;
        private TextView test; ////// fot testing only

        //shared preference variable
        //private String UserName; // not used



        //enemy tanks and variables
        private int[] imgIds = {
                R.id.ourTank_enemy_1_client2,
                R.id.ourTank_enemy_2_client2,
                R.id.ourTank_enemy_3_client2
        };
        int enemiesNum = 0;

        //shooting variables
        private int[] blts = {
                R.id.blt1_client,
                R.id.blt2_client,
                R.id.blt3_client,
                R.id.blt4_client,
                R.id.blt5_client,
                R.id.blt6_client,
                R.id.blt7_client,
                R.id.blt8_client
        };
        boolean isShooting = false;
        ArrayList<ImageView> bullets = new ArrayList<>();

        /**
         * ATTENTION: This was auto-generated to implement the App Indexing API.
         * See https://g.co/AppIndexing/AndroidStudio for more information.
         */
        private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_in_game);

        Bundle bundle = getIntent().getExtras();
        wifiP2pInfo = (WifiP2pInfo) bundle.get("WIFI_P2P_INFO");
        try {
            Thread.sleep(100); //waiting 0.1 seconds for host to set up his socket 			    server before connecting.
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        conectToServer();

        //get the index and set the enemies visible
        try {
            int j = 0;
            for (int i = 0; i < playerAmount-1; i++) { // swap size with a fixed size
                ImageView newTank = (ImageView) findViewById(imgIds[j++]);
                newTank.setVisibility(View.VISIBLE);
                enemiesNum++;
            }
        }catch(Exception e){}

        server_Listener Slistener = new server_Listener(input, this); //  lunch com thread
        Slistener.start();
       // Resources res = getResources();
        ImageView temp;
        for(int i =0 ; i < blts.length ; i++){
            temp = (ImageView) findViewById(blts[i]);
            bullets.add(temp);
        }
        ourTank = (ImageButton) findViewById(R.id.ourTank_client2);
        //for debugging only
        test = (TextView) findViewById(R.id.log_client2);
        backGround = (ImageView) findViewById(R.id.limitsView_client2);
        AbsoluteLayout rlayout = (AbsoluteLayout) findViewById(R.id.activity_client_in_game);
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
                    RotationAngle = (float) alpha1;
                }
                return true;
            }});

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        test.setText("CLIENT");
    }


    public void conectToServer(){ //  set up communication streams
        try {
            connection = new Socket();
            connection.bind(null);
            InetSocketAddress isa = new InetSocketAddress(wifiP2pInfo.groupOwnerAddress, WiFiDirectReceiver.PORT);
            connection.connect(isa, 100000);
            output = new ObjectOutputStream(connection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(connection.getInputStream());
        }catch (IOException ioException){
            ioException.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        int buttonId = view.getId();
        switch (buttonId) {
            case (R.id.ourTank_client2): {

                boolean found = false;
                while(!found) // look fot a free bullet
                    for(int i =0 ; i < this.bullets.size() ; i++){
                        if(this.bullets.get(i).getVisibility()== View.GONE){
                            MyPoint P = new MyPoint((int)ourTank.getX(),(int)ourTank.getY());
                            bulletThread bt = new bulletThread(P,ourTank.getRotation(),i,this);
                            bt.start();
                            found = true;
                            break;
                        }
                    }
                isShooting = true;
                break;
            }
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

        //get  scale for screen density - used to convert from PX to DP
        // final float scale = getResources().getDisplayMetrics().density;


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


        // set movment ( used to send to server)
        myMovement.x=(int)(ourTank.getX() - backGround.getX());
        myMovement.y=(int)( ourTank.getY() - backGround.getY());
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


    private class server_Listener extends Thread { // this thread updates the gui parameters acording to info coming from the server
        // this thread also sends info about the player's tank

        private int myIndex;
        private clientInGame contex ;
        private int x=0,y=0,GIndxe;
        private float rotation_;
        private boolean amIShot = false;
        private Lock lock = new ReentrantLock();

        public server_Listener(ObjectInputStream objectInputStream, clientInGame context_) {
            this.contex = context_;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {

            try {
                myIndex = input.readInt(); // get client id
            }
            catch (Exception e){
                e.printStackTrace();
            }

            while (true) {
                try {
                    // update server
                    contex.output.writeInt(myMovement.x); contex.output.flush();
                    contex.output.writeInt(myMovement.y);contex.output.flush();
                    contex.output.writeFloat(RotationAngle);contex.output.flush();
                    //send if you are shot
                    contex.output.writeBoolean(isShooting);contex.output.flush();
                    isShooting = false;
                    // after geteting the movemnet the server should update all the other tanks about it

                    //update all tanks on the screen (from server)
                    boolean shooting = input.readBoolean();
                    if(shooting) {
                        boolean found = false;
                        while (!found) // look for a free bullet
                            for (int i = 0; i < contex.bullets.size(); i++) {
                                synchronized (lock) {
                                    if (contex.bullets.get(i).getVisibility() == View.GONE) {
                                        MyPoint P = new MyPoint((int) contex.myenemy.get(0).getX(), (int) contex.myenemy.get(0).getY());
                                        bulletThread th = new bulletThread(P, contex.myenemy.get(0).getRotation(), i, contex); // fire a bullet
                                        th.start();
                                        found = true;
                                        break;
                                    }
                                }
                            }
                    }

                    //reaad the boolean ...
                    int j =0;
                    // get enemy info the update on the screen
                    for (int i = 0; i < contex.myenemy.size() + 1; i++) {
                        x = input.readInt();
                        y = input.readInt();
                        rotation_ = input.readFloat();
                        amIShot = input.readBoolean();
                        if(i != myIndex)
                        {
                            contex.myenemy.get(j).setX(x+contex.backGround.getX());
                            contex.myenemy.get(j).setY(y+contex.backGround.getY());
                            contex.myenemy.get(j).setRotation(rotation_);
                            if(amIShot==true ) { // update the shot tank
                                GIndxe = j;
                                contex.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            contex.myenemy.get(GIndxe).setImageResource(R.drawable.fire);
                                        }
                                });
                            }
                            j++;
                            }
                            else {
                                if(amIShot==true)
                                    contex.runOnUiThread(new Runnable() {
                                      @Override
                                      public void run() {
                                           contex.ourTank.setImageResource(R.drawable.fire);
                                      }
                                    });
                            }

                        }

                        contex.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //contex.test.setText(contex.test.getText() + "\nmoved : " + x + " , " + y);
                        }

                    });
                }// get the new tank cords and tank status (shot or not - tank not there, its shot)
                catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }



    private class bulletThread extends Thread { // this thread displayes and moves the bullet acoding to parameters

        private int index_;
        private Timer t_ = new Timer();
        private double x1,y1, x2,y2;
        private float angle ;
        private clientInGame contex;
        private Lock lock = new ReentrantLock();

        public bulletThread(MyPoint position, final float angle_, int index, clientInGame contex_){

            y1 = position.y;
            x1 =position.x;
            angle =  (angle_-90);
            index_ = index;
            contex = contex_;
            contex.runOnUiThread(new Runnable(){ // update tanks on the screen
                @Override
                public void run(){ // update tanks on the screen
                    //enemiesTanks = ImageViews
                    synchronized (lock) {
                        contex.bullets.get(index_).setVisibility(View.VISIBLE);
                        contex.bullets.get(index_).setRotation(angle_);
                    }
                }
            });
        }

        //currently working on this thread
        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            t_.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    contex.runOnUiThread(new Runnable(){ // update tanks on the screen
                        @Override
                        public void run(){
                            // calc new loca
                            x1 = x1 + (5*Math.cos(angle* (Math.PI / 180)));
                            y1 = y1 + (5*Math.sin(angle* (Math.PI / 180)));

                            //contex.test.setText("angel "+ angle +", x = "+x1+", y="+y1);
                            synchronized (lock) { // update location
                                if (x1 > 0 && x1 < contex.backGround.getHeight() && y1 > 0 && y1 < contex.backGround.getWidth()) {
                                    contex.bullets.get(index_).setX((int) x1);
                                    contex.bullets.get(index_).setY((int) y1);
                                } else {
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


