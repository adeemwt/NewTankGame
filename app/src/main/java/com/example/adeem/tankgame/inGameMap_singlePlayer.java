package com.example.adeem.tankgame;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

import java.util.ArrayList;
import java.util.Random;
import java.lang.Math;
import java.util.Timer;
import java.util.TimerTask;

//TODO : HIGHSCORE BORES, SAVES KILLS, ~LIMITS, CHECKING FOR ACCEDENTS ,  HOST/GUEST
import classes.*;

public class inGameMap_singlePlayer extends AppCompatActivity implements View.OnClickListener, SensorEventListener  {



    //final values
    final Point EASY_SIZE  = new Point(1000,1000);
    final Point MEDUIM_SIZE  = new Point(1500,1500);
    final Point HARD_SIZE  = new Point(2000,2000);

    //how big the step for the tank is, dp
    final int STEP = 10;
    final int CHOOSE_X =0;
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
    private ArrayList<MyTarget> targets;
    private ArrayList<ImageView> TargetImages = new ArrayList<>();
    private Point WidthAndHieght ;

    private ImageButton ourTank;
    private TextView test; ////// fot testing only



    //TIMER variables
    private Timer t;
    private int seconds;
    private ImageView backGround;




    //debugging buttons
    private Button up;
    private Button left;
    private Button right;
    private Button down;

    //shared preference variable
    private String Difficulty;
    private String UserName;

    //calculation
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;

    float outTankRotat= 0;
    //initialize all targets
    private int[] imgIds = {
            R.id.target1,
            R.id.target2,
            R.id.target3,
            R.id.target4,
            R.id.target5,
            R.id.target6,
            R.id.target7,
            R.id.target8,
            R.id.target9,
            R.id.target10,
            R.id.target11,
            R.id.target12,
            R.id.target13,
            R.id.target14,
            R.id.target15
    };
    private int[] imgtANK = {
            R.drawable.tank0,
            R.drawable.tank15,
            R.drawable.tank45,
            R.drawable.tank55,
            R.drawable.tank90,
            R.drawable.tank95,
            R.drawable.tank135,
            R.drawable.tank140,
            R.drawable.tank180,
            R.drawable.tank190,
            R.drawable.tank215,
            R.drawable.tank260,
            R.drawable.tank270,
            R.drawable.tank280,
            R.drawable.tank315,
            R.drawable.tank320
    };
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

        ourTank = (ImageButton) findViewById(R.id.ourTank);
        ourTank.setImageResource(imgtANK[0]);

        //for debugging only
        test =(TextView) findViewById(R.id.gyrosxample);
        test.setVisibility(View.GONE);
        up = (Button) findViewById(R.id.upBTN);
        down = (Button) findViewById(R.id.downBTN);
        left= (Button) findViewById(R.id.leftBTN);
        right= (Button) findViewById(R.id.rightBTN);

        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView tv = (TextView) findViewById(R.id.time);
                        tv.setText(String.valueOf(String.valueOf(seconds)+""));
                        seconds++;
                    }
                });
            }
        }, 0, 1000);

        test.setText("");
        ///////////////// for debugging  /////////////////
        up.setOnClickListener(this);
        down.setOnClickListener(this);
        left.setOnClickListener(this);
        right.setOnClickListener(this);

        up.setVisibility(View.GONE);
        down.setVisibility(View.GONE);
        left.setVisibility(View.GONE);
        right.setVisibility(View.GONE);
        /////////////////////////////////////////////////

        backGround = (ImageView) findViewById(R.id.limitsView);

        //initialize sensor
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sManager.registerListener(this,
                sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);



        if(Difficulty!=null){
            if(Difficulty.equals(diffSpinner[0])){ // easy
                this.WidthAndHieght = this.EASY_SIZE;
                targetNum = TARGET_NUM_EASY;

            }else if(Difficulty.equals(diffSpinner[1])){ // medium
                this.WidthAndHieght = this.MEDUIM_SIZE;
                targetNum = TARGET_NUM_MADUIM;

            } else { // if(Difficulty.equals(diffSpinner[1]))   hard - default
                this.WidthAndHieght = this.HARD_SIZE;
                targetNum = TARGET_NUM_HARD;
            }

            randTargets(targetNum);
            String userName = prefs.getString(SHuserName, null);

            Random rnd = new Random();
            Color c = new Color(); // the tank does not have a color for now (well be added)
            c.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            tanks = new ArrayList<> (playerNum);
            tanks.add(new Tank(c));
            ourTank.setOnClickListener(this);

            AbsoluteLayout rlayout = (AbsoluteLayout) findViewById(R.id.mainlayout);
            rlayout.setOnClickListener(this);

            //on screen  touch get coordinates and change the tank aim accordingly
            rlayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN){
                        double alpha1;
                        double Xs= Math.pow(event.getX()-ourTank.getX(),2);
                        double Ys =  Math.pow(event.getY() - ourTank.getY(),2);
                        double sqrt =Math.sqrt(Xs + Ys );
                        alpha1 = Math.asin((event.getY() - ourTank.getY())/sqrt);
                        alpha1 =  alpha1 * 180 / Math.PI;
                        if(ourTank.getX() <= event.getX())
                         alpha1   += 90;
                        else{
                            alpha1 = 270 -alpha1;
                        }
                        outTankRotat = (float)alpha1;
                        //ourTank.setRotation((float)alpha1);
                        setImageByRotation(outTankRotat);
                    }
                    return true;
                }
            });
        }
    }


    private void setImageByRotation(float rotation){
        ourTank.setImageResource(imgtANK[(int)(rotation/(360/imgtANK.length)) % imgtANK.length]);
    }
    /////////////////////////////////////for debugging purposes //////////////////////////////////
    @Override
    public void onClick(View view) {
        int buttonId = view.getId();
        switch(buttonId){
//            case(R.id.upBTN):
//                moveTargets(CHOOSE_Y,STEP);
//                break;
//            case(R.id.leftBTN):
//                moveTargets(CHOOSE_X,STEP);
//                break;
//            case(R.id.rightBTN):
//                moveTargets(CHOOSE_X,-STEP);
//                break;
//            case(R.id.downBTN):
//                moveTargets(CHOOSE_Y,-STEP);
//                break;
            case(R.id.ourTank): {
                Bullet bullet = new Bullet(this.TargetImages,outTankRotat,tanks.get(0),new MyPoint((int)ourTank.getX(),(int)ourTank.getY()));
                ArrayList<ImageView> targets= TargetImages;
                this.TargetImages =(ArrayList<ImageView>) bullet.shoot();

                for(int i =0 ; i < TargetImages.size()-1; i++){
                    if(TargetImages.get(i).getVisibility() == View.GONE){
                        targets.remove(TargetImages.get(i));
                    }
                }
                TargetImages = targets;
                if(TargetImages.size()==1){
                    this.saveAndExit();
                }
                break;
            }

        }
    }


    //randomize the trget position and set visiable according to the number for the difficulty level
    public void randTargets(int targetNum){
        targets = new ArrayList<>(targetNum);


        final float scale = getResources().getDisplayMetrics().density;
        test.setText("the density is " +scale);
        int pixels = (int) (WidthAndHieght.x* scale + 0.5f);


        backGround.getLayoutParams().height = pixels;
        backGround.getLayoutParams().width =pixels;
        backGround.requestLayout();
        ImageView img;

        for(int i = 0 ; i< targetNum ; i++){
            int x = (int)(Math.random() * this.WidthAndHieght.x- 55) + 55; // 55?
            int y = (int)(Math.random() * this.WidthAndHieght.y - 55) + 55;
            int size = (int)(Math.random() *MAX_TARGET_SIZE);
            targets.add(new MyTarget(new Point(x,y),size));
            img =(ImageView) findViewById(imgIds[i]);
            img.setX(targets.get(i).getCords().x);
            img.setY(targets.get(i).getCords().y);
            img.setVisibility(View.VISIBLE);


            TargetImages.add(img);
        }
        //add background for moving on map purposes
        TargetImages.add(backGround);

    }


    //on sensor change listener
    public void onSensorChanged(SensorEvent event) {
        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            // Axis of the rotation sample, not normalized yet.
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];
            // Calculate the angular speed of the sample
            float omegaMagnitude = (float)Math.sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

            // Normalize the rotation vector if it's big enough to get the axis
            // (that is, EPSILON should represent your maximum allowable margin of error)
            if (omegaMagnitude > EPSILON) {
                axisX /= omegaMagnitude;
                axisY /= omegaMagnitude;
                axisZ /= omegaMagnitude;
            }

            float  thetaOverTwo = omegaMagnitude * dT / 2.0f;
            float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
            float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
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
        if(deltaRotationVector[1]>0)//check tank direction on Y axis
        moveY = (backGround.getHeight() - Math.abs(backGround.getY())) > ourTank.getY() + 130;
        else moveY = (backGround.getY() < ourTank.getY());
        boolean moveX;
        if(deltaRotationVector[0]<0)//check tank direction on X axis
            moveX = (backGround.getWidth() - Math.abs(backGround.getX())) > ourTank.getX() + 130;
        else moveX = (backGround.getX() < ourTank.getX());

        //test.setText(ourTank.getY()+" < "+((backGround.getHeight()-0.5f) - Math.abs(backGround.getY()))+"\n  backGround.getY() is "+ backGround.getY() +"\n (backGround.getHeight()-0.5f)/scale is = " + (backGround.getHeight()-0.5f)/scale);

        for(int i =0 ; i < TargetImages.size() ; i++) {
            if(moveX)
                TargetImages.get(i).setX(TargetImages.get(i).getX() +deltaRotationVector[0]*50);// tank going left = delta >0 , tank going right delta < 0
            if(moveY)
                TargetImages.get(i).setY(TargetImages.get(i).getY() -deltaRotationVector[1]*50);// tank going up = delta>0, tank going down delta < 0
        }

//        double angel = Math.atan((deltaRotationVector[0]) /(deltaRotationVector[1]));
//        angel =  angel * 180 / Math.PI;
    }



@Override
protected void onResume()
{
    super.onResume();
}

    //When this Activity isn't visible anymore
    @Override
    protected void onStop()
    {
        //unregister the sensor listener
        sManager.unregisterListener(this);
        super.onStop();
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1)
    {
        //Do nothing.
    }


    public void saveAndExit() { // save score

        final Firebase mRef;

        mRef = new Firebase("https://tankgameproject-85eb4.firebaseio.com/users/" + UserName + "/singlePlayer/" + Difficulty +"/");
            mRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String text = dataSnapshot.getValue(String.class);
                    if(text == null){
                        mRef.setValue(""+seconds); }
                    else{
                        if(Integer.parseInt(text) > seconds){
                            mRef.setValue(""+seconds); }
                    }
                }
                @Override
                public void onCancelled(FirebaseError firebaseError) {

                }
            });
        this.onBackPressed();
        }

}


