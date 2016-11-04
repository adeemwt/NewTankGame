package com.example.adeem.tankgame;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import classes.Bullet;
import classes.Player;
import classes.ServerMessage;
import classes.Taget;
import classes.Tank;

public class Client_Ingame_trial extends AppCompatActivity implements View.OnClickListener, SensorEventListener {


    //the socketOuput and input streams , no need to initialize atm
    ObjectInputStream input = null;
    ObjectOutputStream output = null;


    ArrayList<ImageButton> mytanks = new ArrayList<ImageButton>();

    //final values
    final Point EASY_SIZE = new Point(1000, 1000);
    final Point MEDUIM_SIZE = new Point(1500, 1500);
    final Point HARD_SIZE = new Point(2000, 2000);

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
    private ArrayList<Tank> tanks;
    private ArrayList<Taget> targets;
    private ArrayList<ImageView> TargetImages = new ArrayList<>();
    private Point WidthAndHieght;

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
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_game_map);

        Resources res = getResources();
        String[] diffSpinner = res.getStringArray(R.array.Diff_spinner);
        my_pref_name = res.getString(R.string.SharedPreferencesPrefsName);
        SHuserName = res.getString(R.string.SharedPreferencesUserName);

        prefs = getSharedPreferences(my_pref_name, MODE_PRIVATE);
        Difficulty = prefs.getString("difficultly", null);
        UserName = prefs.getString(SHuserName, null);

        ourTank = (ImageButton) findViewById(R.id.ourTank_client);
        //for debugging only
        test = (TextView) findViewById(R.id.log_client);

        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView tv = (TextView) findViewById(R.id.time_client);
                        tv.setText(String.valueOf(String.valueOf(seconds) + ""));
                        seconds++;
                    }
                });
            }
        }, 0, 1000);

        test.setText("");
        /////////////////////////////////////////////////

        backGround = (ImageView) findViewById(R.id.limitsView_client);

        //initialize sensor
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sManager.registerListener(this,
                sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);


        server_Listener Slistener = new server_Listener(input);
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

            randTargets(targetNum);
            String userName = prefs.getString(SHuserName, null);

            Random rnd = new Random();
            Color c = new Color();                   // the tank does not have a color for now (well be added)
            c.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            tanks = new ArrayList<>(playerNum);
            tanks.add(new Tank(new Player(userName), c));
            ourTank.setOnClickListener(this);

            AbsoluteLayout rlayout = (AbsoluteLayout) findViewById(R.id.mainlayout_client);
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
                    }
                    return true;
                }
            });
        }
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }


    /////////////////////////////////////for debugging purposes //////////////////////////////////
    @Override
    public void onClick(View view) {
        int buttonId = view.getId();
        switch (buttonId) {
            case (R.id.upBTN):
                moveTargets(CHOOSE_Y, STEP);
                break;
            case (R.id.leftBTN):
                moveTargets(CHOOSE_X, STEP);
                break;
            case (R.id.rightBTN):
                moveTargets(CHOOSE_X, -STEP);
                break;
            case (R.id.downBTN):
                moveTargets(CHOOSE_Y, -STEP);
                break;
            case (R.id.ourTank): {
                Bullet bullet = new Bullet(this.TargetImages, ourTank.getRotation(), tanks.get(0), new Point((int) ourTank.getX(), (int) ourTank.getY()));
                ArrayList<ImageView> targets = TargetImages;
                this.TargetImages = bullet.shoot();

                for (int i = 0; i < TargetImages.size() - 1; i++) {
                    if (TargetImages.get(i).getVisibility() == View.GONE) {
                        targets.remove(TargetImages.get(i));
                    }
                }
                TargetImages = targets;
                if (TargetImages.size() == 1) {
                    this.saveAndExit();
                }

                try {
                    this.output.writeObject(bullet);// after geteting the movemnet the server should update all the other tanks about it
                }catch (Exception e){
                    //an excpetion has accured ...
                }
                break;
            }

        }
    }

    //////////////////////////// for debugging purposes ////////////////////////////////////////////////////////
    public void moveTargets(int xOry, int movement) { // buttons only
        movement *= 3;
        final float scale = getResources().getDisplayMetrics().density;
        test.setText("the Width is " + backGround.getWidth() + "\nthe dp calc is " + (backGround.getHeight() - 0.5f) / scale);
        if (xOry == CHOOSE_X) {
            if (movement < 0) {

            } else {
                if (ourTank.getX() < backGround.getX()) return;
            }
            if (ourTank.getX() > backGround.getX() && ourTank.getX() > backGround.getX() + (backGround.getWidth() - 0.5f) / scale) {
                for (int i = 0; i < TargetImages.size(); i++)
                    TargetImages.get(i).setX(TargetImages.get(i).getX() + movement);
                if (movement < 0)
                    ourTank.setRotation(90);
                else ourTank.setRotation(-90);
            }
        } else {
            if (movement < 0) {

            } else {
                if (ourTank.getY() < backGround.getY()) return;
            }
            if (ourTank.getY() < backGround.getY() && ourTank.getY() > backGround.getY() + (backGround.getHeight() - 0.5f) / scale) {
                for (int i = 0; i < TargetImages.size(); i++)
                    TargetImages.get(i).setY(TargetImages.get(i).getY() + movement);
                if (movement < 0)
                    ourTank.setRotation(180);
                else ourTank.setRotation(0);
            }

        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    //randomize the trget position and set visiable according to the number for the difficulty level
    public void randTargets(int targetNum) {
        targets = new ArrayList<>(targetNum);


        final float scale = getResources().getDisplayMetrics().density;
        test.setText("the dinsity is " + scale);
        int pixels = (int) (WidthAndHieght.x * scale + 0.5f);


        backGround.getLayoutParams().height = pixels;
        backGround.getLayoutParams().width = pixels;
        backGround.requestLayout();
        ImageView img;

//        for(int i = 0 ; i< targetNum ; i++){
////            int x = (int)(Math.random() * this.WidthAndHieght.x- 55) + 55; // 55?
////            int y = (int)(Math.random() * this.WidthAndHieght.y - 55) + 55;
////            int size = (int)(Math.random() *MAX_TARGET_SIZE);
////            targets.add(new Taget(new Point(x,y),size));
////            img =(ImageView) findViewById(imgIds[i]);
////            img.setX(targets.get(i).getCords().x);
////            img.setY(targets.get(i).getCords().y);
////            img.setVisibility(View.VISIBLE);
//
//
//            TargetImages.add(img);
//        }
        //add background for moving on map purposes
        TargetImages.add(backGround);
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

        for (int i = 0; i < TargetImages.size(); i++) {
            if (moveX)
                TargetImages.get(i).setX(TargetImages.get(i).getX() + deltaRotationVector[0] * 50);// tank going left = delta >0 , tank going right delta < 0
            if (moveY)
                TargetImages.get(i).setY(TargetImages.get(i).getY() - deltaRotationVector[1] * 50);// tank going up = delta>0, tank going down delta < 0
        }

        Point movement = new Point(0,0);
        for (int i = 0; i < mytanks.size(); i++) {
            if (moveX) {
                mytanks.get(i).setX(mytanks.get(i).getX() + deltaRotationVector[0] * 50);
                movement.x = (int)deltaRotationVector[0] * 50;
            }// tank going left = delta >0 , tank going right delta < 0
            if (moveY) {
                mytanks.get(i).setY(mytanks.get(i).getY() - deltaRotationVector[1] * 50);
                movement.y = (int)deltaRotationVector[1] * 50;
            }// tank going up = delta>0, tank going down delta < 0
        }

        try {
            this.output.writeObject(movement);// after geteting the movemnet the server should update all the other tanks about it
        }catch (Exception e){
            //an excpetion has accured ...
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


    private class server_Listener extends Thread {

        ObjectInputStream objectInputStream;
        ServerMessage msg;
        int enemiesNum = 0;


        public server_Listener(ObjectInputStream objectInputStream) {
            this.objectInputStream = objectInputStream;

        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            while (true) {
                try {
                    ServerMessage msg = (ServerMessage) objectInputStream.readObject();//object can be message, or new user
                    if (msg.getType().equals(ServerMessage.TYPE.OBJECTS)) {//ADD all the objects to the map
                        if(enemiesNum == 0) {//add this tank
                            ImageButton newTank = (ImageButton) findViewById(R.id.ourTank_enemy_1);
                            newTank.setVisibility(View.VISIBLE);
                            enemiesNum++;
                            mytanks.add(newTank);
                        }
                        if(enemiesNum == 1) {//add this tank
                            ImageButton newTank = (ImageButton) findViewById(R.id.ourTank_enemy_2);
                            newTank.setVisibility(View.VISIBLE);
                            enemiesNum++;
                            mytanks.add(newTank);
                        }
                        if(enemiesNum == 2) {//add this tank
                            ImageButton newTank = (ImageButton) findViewById(R.id.ourTank_enemy_3);
                            newTank.setVisibility(View.VISIBLE);
                            enemiesNum++;
                            mytanks.add(newTank);
                        }


                    } else if (msg.getType().equals(ServerMessage.TYPE.UPDATE)) { // UPDATE tank positions and if shot make it burn or some shit
                        for(int i =0 ; i < msg.getTanks().size() ; i++){
                            if(!msg.getTanks().get(i).getShot()) {//tank is still in te game // this is the position need to also get the angle
                                mytanks.get(i).setX(msg.getTanks().get(i).getPosition().x);
                                mytanks.get(i).setY(msg.getTanks().get(i).getPosition().y);
                            }
                            else{//tanks was shot down
                                mytanks.get(i).setImageResource(R.drawable.target_goat);//set fire or something
                            }
                        }
                    }
                    // get the new tank cords and tank status (shot or not - tank not there, its shot)
                } catch (ClassNotFoundException | IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
}


