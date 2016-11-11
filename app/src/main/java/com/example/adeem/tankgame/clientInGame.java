package com.example.adeem.tankgame;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
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
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
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
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import classes.Bullet;
import classes.Player;
import classes.Taget;
import classes.Tank;

public class clientInGame extends AppCompatActivity implements View.OnClickListener, SensorEventListener {



    float RotationAngle;
    boolean isShooting = false;
    //the socketOuput and input streams , no need to initialize atm
    ObjectInputStream input = null;
    ObjectOutputStream output = null;
    Socket connection;
    MyPoint myMovement = new MyPoint(0,0);

    ArrayList<ImageView> myenemy = new ArrayList<ImageView>();
    WifiP2pInfo wifiP2pInfo;

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

    private Boolean gameRuning = true;

    private SharedPreferences prefs;
    private String my_pref_name;
    private String SHuserName;

    private SensorManager sManager;
    private int targetNum = 0;
    private ArrayList<Tank> tanks;
    private ArrayList<Taget> targets;
    //private ArrayList<ImageView> TargetImages = new ArrayList<>();
    private MyPoint WidthAndHieght;

    private ImageButton ourTank;
    private TextView test; ////// fot testing only

    private server_Listener Slistener;
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
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    private int[] imgIds = {
            R.id.ourTank_enemy_1_client2,
            R.id.ourTank_enemy_2_client2,
            R.id.ourTank_enemy_3_client2
    };
    int enemiesNum = 0;




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
    ArrayList<ImageView> bullets = new ArrayList<>();


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

        //!!!!!!!!!!!!!!!!!!!!! get the index and set the enemies visible!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        int playerAmount = 2; // get using sherd prefrenses

        try {
            int j = 0;
            for (int i = 0; i < playerAmount-1; i++) { // swap size with a fixed size
                //if (i != myIndex) {
                ImageView newTank = (ImageView) findViewById(imgIds[j++]);
                newTank.setVisibility(View.VISIBLE);
                enemiesNum++;
                myenemy.add(newTank);

                //}
            }
        }catch(Exception e){}


        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

        Slistener = new server_Listener(input, this);
        Slistener.start();


        Resources res = getResources();

        String[] diffSpinner = res.getStringArray(R.array.Diff_spinner);
        my_pref_name = res.getString(R.string.SharedPreferencesPrefsName);
        SHuserName = res.getString(R.string.SharedPreferencesUserName);
        ImageView temp;

        for(int i =0 ; i < blts.length ; i++){
            temp = (ImageView) findViewById(blts[i]);
            bullets.add(temp);
        }





        prefs = getSharedPreferences(my_pref_name, MODE_PRIVATE);
        Difficulty = prefs.getString("difficultly", null);
        UserName = prefs.getString(SHuserName, null);

        ourTank = (ImageButton) findViewById(R.id.ourTank_client2);
        //for debugging only
        test = (TextView) findViewById(R.id.log_client2);

        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView tv = (TextView) findViewById(R.id.time_client2);
                        tv.setText(String.valueOf(String.valueOf(seconds) + ""));
                        seconds++;
                    }
                });
            }
        }, 0, 1000);

        test.setText("");
        /////////////////////////////////////////////////

        backGround = (ImageView) findViewById(R.id.limitsView_client2);

        //initialize sensor
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sManager.registerListener(this,
                sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);




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
            //randTargets(targetNum);
            String userName = prefs.getString(SHuserName, null);

//            Random rnd = new Random();
//            Color c = new Color();                   // the tank does not have a color for now (well be added)
//            c.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
//            tanks = new ArrayList<>(playerNum);
//            tanks.add(new Tank(new Player(userName), c));
            ourTank.setOnClickListener(this);

            AbsoluteLayout rlayout = (AbsoluteLayout) findViewById(R.id.activity_client_in_game);
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
                }
            });
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        test.setText("all is good CLIENT");
    }

    public void settext_(String text){
        test.setText(test.getText() +"\n" + text);
    }

    public void conectToServer(){
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

    /////////////////////////////////////for debugging purposes //////////////////////////////////
    @Override
    public void onClick(View view) {
        int buttonId = view.getId();
        switch (buttonId) {
            case (R.id.ourTank_client2): {
                Lock lock = new ReentrantLock();

                boolean found = false;
                while(!found)
                    for(int i =0 ; i < this.bullets.size() ; i++){
                        synchronized (lock){
                              if(this.bullets.get(i).getVisibility()== View.GONE){
                               MyPoint P = new MyPoint((int)ourTank.getX(),(int)ourTank.getY());
                              bulletThread th = new bulletThread(P,ourTank.getRotation(),i,this);
                              th.start();
                              found = true;
                               break;
                            }
                        }

                    }
                isShooting = true;
                break;
            }

        }
    }



    //randomize the trget position and set visiable according to the number for the difficulty level
    public void setMapScale() {
//        final float scale = getResources().getDisplayMetrics().density;
//        int pixels = (int) (WidthAndHieght.x * scale + 0.5f);
//        backGround.getLayoutParams().height = pixels;
//        backGround.getLayoutParams().width = pixels;
//        backGround.requestLayout();
        //TargetImages.add(backGround);
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
        gameRuning = false;
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

    @Override
    public void onBackPressed() {
        gameRuning = false;
        Slistener.interrupt();

        super.onBackPressed();
    }

    private class server_Listener extends Thread {

        ObjectInputStream objectInputStream;
        int myIndex;
        TextView mytest;
        clientInGame contex ;
        int x=0,y=0,GIndxe;
        float rotation_;
        boolean amIShot = false;
        Lock lock = new ReentrantLock();

        public server_Listener(ObjectInputStream objectInputStream, clientInGame context_) {
            this.contex = context_;
            this.objectInputStream = objectInputStream;
            mytest = (TextView) findViewById(R.id.log_client2);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {

            try {
                myIndex = input.readInt();
            }
            catch (Exception e){
                e.printStackTrace();
            }

            while (gameRuning) {
                try {
                    // update server
                    contex.output.writeInt(myMovement.x); contex.output.flush();
                    contex.output.writeInt(myMovement.y);contex.output.flush();
                    contex.output.writeFloat(RotationAngle);contex.output.flush();
                    //send if you shot
                    contex.output.writeBoolean(isShooting);contex.output.flush();
                    isShooting = false;
                    // after geteting the movemnet the server should update all the other tanks about it

                    //update all tanks on the screen (from server)
                    boolean shooting = input.readBoolean();
                    if(shooting) {
                        boolean found = false;
                        while (!found)
                            for (int i = 0; i < contex.bullets.size(); i++) {
                                synchronized (lock) {
                                    if (contex.bullets.get(i).getVisibility() == View.GONE) {
                                        MyPoint P = new MyPoint((int) contex.myenemy.get(0).getX(), (int) contex.myenemy.get(0).getY());
                                        bulletThread th = new bulletThread(P, contex.myenemy.get(0).getRotation(), i, contex);
                                        th.start();
                                        found = true;
                                        break;
                                    }
                                }
                            }
                    }

                    //reaad the boolean ...
                    int j =0;
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
                            if(amIShot==true ) {
                                GIndxe = j;
                                contex.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        contex.myenemy.get(GIndxe).setImageResource(R.drawable.fire);
                                    }
                                });
                                //contex.myenemy.get(j++).setVisibility(View.GONE);
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
                            //contex.ourTank.setVisibility(View.GONE);
                        }

                    }


                }// get the new tank cords and tank status (shot or not - tank not there, its shot)
                catch (Exception e) {
                    //e.printStackTrace();
                    break;
                }
            }

            contex.runOnUiThread(new Runnable(){ // update tanks on the screen
                @Override
                public void run(){ // update players tank on the screen
                    if(contex.gameRuning )
                        contex.test.setText("the other player had left the game");
                    else
                        contex.test.setText("GAME OVER");

                }
            });

        }
    }



    private class bulletThread extends Thread {

        int index_;
        Timer t_ = new Timer();
        double x1,y1, x2,y2;
        float angle ;
        clientInGame contex;
        Lock lock = new ReentrantLock();

        public bulletThread(MyPoint position, final float angle_, int index, clientInGame contex_){

            y1 = position.y;
            x1 =position.x;
            angle =  (angle_-90);
            index_ = index;
            contex = contex_;
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

                            synchronized (lock) {
                                if (x1 > 0 && x1 < contex.backGround.getHeight() && y1 > 0 && y1 < contex.backGround.getWidth()) {
                                    contex.bullets.get(index_).setX((int) x1);
                                    contex.bullets.get(index_).setY((int) y1);
                                } else {
                                    contex.bullets.get(index_).setVisibility(View.GONE);
                                    //onStop();
                                    t_.cancel();
                                    //stop();
                                }
                            }
                        }
                    });
                }
            }, 0, 3);
        }
    }

}






