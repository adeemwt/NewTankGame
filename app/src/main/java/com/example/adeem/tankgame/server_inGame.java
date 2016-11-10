package com.example.adeem.tankgame;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
//import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pInfo;
import android.support.annotation.UiThread;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.appindexing.Action; //you dont have the import in the library
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import classes.Bullet;
import classes.MyPoint;
import classes.Player;
import classes.ServerMessage;
import classes.Taget;
import classes.Tank;

public class server_inGame extends AppCompatActivity  implements View.OnClickListener, SensorEventListener {

    //the socketOuput and input streams , no need to initialize atm
    //ObjectInputStream input = null;
    //ObjectOutputStream output = null;

    int clientCount = 1; // server is number 0
    ArrayList<ImageButton> myenemy = new ArrayList<ImageButton>();
    ArrayList<Tank> tankArry = new ArrayList<Tank>();
    ArrayList<Bullet> bulletArry = new ArrayList<Bullet>();
    Boolean gameRuning = true;
    private ArrayList<Thread> ClienThreads = new ArrayList<Thread>();
    Thread socketServerThread;
    server_inGame contex = this;


    //final values
    final MyPoint EASY_SIZE = new MyPoint(1000, 1000);
    final MyPoint MEDUIM_SIZE = new MyPoint(1500, 1500);
    final MyPoint HARD_SIZE = new MyPoint(2000, 2000);

    //how big the step for the tank is, dp
    final int STEP = 10;
    final int CHOOSE_X = 0;
    final int CHOOSE_Y = 1;

    //how many target
    final int MAX_TARGET_SIZE = 5;
    final int TARGET_NUM_EASY = 5;
    final int TARGET_NUM_MADUIM = 10;
    final int TARGET_NUM_HARD = 15;
    final double EPSILON = 0.000001;


    private int playerNum = 1;


    private SharedPreferences prefs;
    private String my_pref_name;
    private String SHuserName;

    private SensorManager sManager;
    private int targetNum = 0;
 // /  private ArrayList<Tank> /tanks;
  //  private ArrayList<Taget> targets;
   // private ArrayList<ImageView> TargetImages = new ArrayList<>();

   // private ArrayList<Socket> connections;
    private MyPoint WidthAndHieght;

    private ImageButton ourTank;
    private TextView test; ////// fot testing only


    //TIMER variables
    private Timer t;
    private int seconds;
    private ImageView backGround;

    //shared preference variable
    private String Difficulty;
    private String UserName;

    //calculation
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;
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
    private int[] imgIds = {
            R.id.tank_1,
            R.id.tank_2,
            R.id.tank_3
    };
    int enemiesNum = 0;
    AbsoluteLayout rlayout;
    ArrayList<ImageView> enemiesTanks = new ArrayList<>();
    ArrayList<ImageView> bullets = new ArrayList<>();

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

        for(int i =0 ; i < blts.length ; i++){
            temp = (ImageView) findViewById(blts[i]);
            bullets.add(temp);
        }

        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!check if this is even possible!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

//        socketServerThread = new Thread(new ServerSocketThread());
//        socketServerThread.start();
        conectToClient();
//
//        Bundle bundle = getIntent().getExtras();
//        test = (Button) findViewById(R.id.send_test);
//        connections = (ArrayList<Socket>) bundle.get("TANKS_CONNECTIONS");

        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!check if this is even possible!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

//        for(int i =0 ; i < connections.size() ; i++){
//            client_Listener listener = new client_Listener(connections.get(i));
//            tankArry.add(new Tank(new Player(""+ i),null));
//        }
        Resources res = getResources();
        String[] diffSpinner = res.getStringArray(R.array.Diff_spinner);
        my_pref_name = res.getString(R.string.SharedPreferencesPrefsName);
        SHuserName = res.getString(R.string.SharedPreferencesUserName);





        prefs = getSharedPreferences(my_pref_name, MODE_PRIVATE);
        Difficulty = prefs.getString("difficultly", null);
        UserName = prefs.getString(SHuserName, null);

        ourTank = (ImageButton) findViewById(R.id.ourTank_server2);

        tankArry .add(new Tank(new Player("server"),null));
        tankArry .add(new Tank(new Player("client"),null));

        //for debugging only
        test = (TextView) findViewById(R.id.log_server);

        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView tv = (TextView) findViewById(R.id.time_server);
                        tv.setText(String.valueOf(String.valueOf(seconds) + ""));
                        seconds++;
                    }
                });
            }
        }, 0, 1000);

        test.setText("");
        /////////////////////////////////////////////////

        backGround = (ImageView) findViewById(R.id.limitsView_server2);

        //initialize sensor
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sManager.registerListener(this,
                sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);


       // server_Listener Slistener = new server_Listener(input);
        Difficulty = diffSpinner[0]; // for testing !!!!
        if (Difficulty != null) {
            if (Difficulty.equals(diffSpinner[0])) { // easy
                this.WidthAndHieght = this.EASY_SIZE;
                targetNum = TARGET_NUM_EASY;

            } else if (Difficulty.equals(diffSpinner[1])) { // medium
                this.WidthAndHieght = this.MEDUIM_SIZE;
                targetNum = TARGET_NUM_MADUIM;

            } else { // if(Difficulty.equals(diffSpinner[1]))   hard - default
                this.WidthAndHieght = this.HARD_SIZE;
                targetNum = TARGET_NUM_HARD;
            }
            setMapScale();
            String userName = prefs.getString(SHuserName, null);

            ourTank.setOnClickListener(this);

             rlayout = (AbsoluteLayout) findViewById(R.id.activity_server_in_game);
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
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.44444
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        test.setText("all is good SERVER ");
    }

    public void setMapScale() {
//        final float scale = getResources().getDisplayMetrics().density;
//        int pixels = (int) (WidthAndHieght.x * scale + 0.5f);
//        backGround.getLayoutParams().height = pixels;
//        backGround.getLayoutParams().width = pixels;
//        backGround.requestLayout();
    }

    /////////////////////////////////////for debugging purposes //////////////////////////////////
    @Override
    public void onClick(View view) {
        int buttonId = view.getId();
        switch (buttonId) {

            case (R.id.ourTank_server2): {
                Lock lock = new ReentrantLock();
                synchronized (lock){
                    //make bullet and make it shoot
                    Bullet bullet_ = new Bullet(tankArry,0);
                    this.tankArry = (ArrayList<Tank>) bullet_.shoot();
                    float headingAngle = tankArry.get(0).getheadingAngle();
                    if(headingAngle <0) headingAngle = 360 + headingAngle;
                    test.setText(tankArry.get(0).getheadingAngle() +" , " + headingAngle);

                    //add bullet thread to display the bullet \
                    boolean found = false;
                    while(!found)
                        for(int i =0 ; i < contex.bullets.size() ; i++){
                            if(contex.bullets.get(i).getVisibility()== View.GONE){
                                bulletThread th = new bulletThread(bullet_,i);
                                th.start();
                                found = true;
                                break;
                            }
                        }
                    amIShooting = true;
                }
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


        MyPoint movement = new MyPoint((int)(ourTank.getX() - backGround.getX()),(int)( ourTank.getY() - backGround.getY()));
        Lock lock = new ReentrantLock();
        synchronized (lock) {
            tankArry.get(0).setPosition(movement);
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
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
        //Do nothing.
    }


    public void saveAndExit() { // save score

        final Firebase mRef;

        mRef = new Firebase("https://tankgameproject-85eb4.firebaseio.com/users/" + UserName + "/MultiPlayer/");
        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String text = dataSnapshot.getValue(String.class);
                if (text == null) {
                    mRef.setValue("" + seconds);
                } else {
                    if (Integer.parseInt(text) > seconds) {
                        mRef.setValue("" + seconds);
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        this.onBackPressed();
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


    ImageView blt;
    private class client_Listener extends Thread {

        private Socket socket;
        private ObjectOutputStream outputToClient;
        private ObjectInputStream inputFromClient;
        int clientNum = 0;
        MyPoint position = new MyPoint(0,0);
        Boolean shooting = false;
        Lock lock = new ReentrantLock();

        float rotation_;
        public client_Listener(Socket socket) {
            this.socket = socket;
            clientNum = clientCount++;
            try {
                outputToClient = new ObjectOutputStream(this.socket.getOutputStream());
                inputFromClient = new ObjectInputStream(this.socket.getInputStream());
            } catch (IOException ioException) {
                //   displayToast("Server Ended the connection.");
                //   exit();
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
                                            bulletThread th = new bulletThread(bullet,i);
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
                                            //contex.ourTank.setVisibility(View.GONE);
                                            contex.test.setText("YOU SHOT SERVER BITCH!!");
                                        }
                                    }
                                });
                            }
                        }

                        contex.runOnUiThread(new Runnable(){ // update tanks on the screen
                            @Override
                            public void run(){
                                //enemiesTanks = ImageViews
                                contex.enemiesTanks.get(clientNum-1).setX(position.x + contex.backGround.getX());
                                contex.enemiesTanks.get(clientNum-1).setY(position.y + contex.backGround.getY());
                                contex.enemiesTanks.get(clientNum-1).setRotation(rotation_);
                                if(tankArry.get(clientNum).getShot() == true) {
                                    contex.enemiesTanks.get(clientNum - 1).setImageResource(R.drawable.fire);
                                    //contex.enemiesTanks.get(clientNum - 1).setVisibility(View.GONE);
                                    contex.test.setText("\n"+clientNum +"was shot");//try it now . if we get s
                                }
                            }
                        });

                    // send tankArry to client (that contains info of all tanks updated posions and if thay got shot or not
                    //outputToClient.writeObject(tankArry);

                    int playersInGame = 0;
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
                        if(tankArry.get(i).getShot() == false){
                            playersInGame++;
                        }
                        //send the stuff to the client
                    }
                    if(playersInGame <=1){
                        //game over!!!! , send to other players
                    }
                } catch ( IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            try {
                outputToClient.writeInt(-1);
                outputToClient.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
            // send client the game is over and player num thet won
        }
    }
    public void conectToClient(){
        ServerSocket server;
        ImageView img ;
        try {
            server = new ServerSocket(WiFiDirectReceiver.PORT);//, 1);
            boolean flag = true; // change
            while (flag) { // while waiting for players
                flag = false; // change
                    Socket socket = server.accept();
                    client_Listener cl = new client_Listener(socket);
                    ClienThreads.add(cl);
                    cl.start();
                    img = (ImageView) findViewById(this.imgIds[enemiesNum]);
                    this.enemiesTanks.add(img);
                    this.enemiesTanks.get(enemiesNum).setVisibility(View.VISIBLE);
                    this.enemiesNum++;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    private class bulletThread extends Thread {
        Bullet bullet;
        int index_;
        Timer t_ = new Timer();
        double x1,y1, x2,y2;
        float angle ;

        public bulletThread(Bullet b,int index){
            this.bullet = b;
            y1 = bullet.getTankPosition().y;
            x1 =  bullet.getTankPosition().x;
            angle =  (bullet.getHeadingAngle()-90);
            index_ = index;
            contex.runOnUiThread(new Runnable(){ // update tanks on the screen
                @Override
                public void run(){
                    //enemiesTanks = ImageViews
                    contex.bullets.get(index_).setVisibility(View.VISIBLE);
                    contex.bullets.get(index_).setRotation(bullet.getHeadingAngle());
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
                            x1 = x1 + (5*Math.cos(angle* (Math.PI / 180)));
                            y1 = y1 + (5*Math.sin(angle* (Math.PI / 180)));

                            //this works , the only thing is that its dp so the bullet place kinda changes ... other than that its good
                            //now we need to add that thread in the clients and make the client display it too
                            // do not copy and past ... lets make it a stand alone class and pass it context , i tried to do that, got an erro
                            // it wont take it,,  aitrse  you sure? its just one thread why go threw the pain in the ass of making a class
                            // cus we have shit loads of duplicate code that we shuld sort out anyway at some point ..
                            // this thread we really should not move ... its not getting duplicated eeither
                            // it is ... how do you put it on the client?  same code and how do you plan on getting on the client if its  a class ?
                            // mate its not worth the time ... i think we well lose points on this
//                            double ranAngel = angle*(Math.PI / 180);
//                            x2 = x1 * Math.cos(ranAngel) - y1 * Math.sin (ranAngel);
//                            y2 = x1 *  Math.sin (ranAngel) + y1 *  Math.cos (ranAngel);
//                            x1 = x2;
//                            y1 = y2;
                            contex.test.setText("angel "+ angle +", x = "+x1+", y="+y1);
                            if(x1 > 0 && x1 < contex.backGround.getHeight() && y1 > 0 && y1 < contex.backGround.getWidth()) {
                                contex.bullets.get(index_).setX((int) x1);
                                contex.bullets.get(index_).setY((int) y1);
                            }
                            else {
                                contex.bullets.get(index_).setVisibility(View.GONE);
                                //onStop();
                                t_.cancel();
                                //stop();
                            }
                        }
                    });
                }
            }, 0, 3);
        }
    }


    //1. server make new bullet thread when he soots and makes shooting boolean true , 2. server send boolean shooting to cleint (already does that )
    // so if server is shooting client makes new bullet thread , if client shoot he display for himself the bullet
}


