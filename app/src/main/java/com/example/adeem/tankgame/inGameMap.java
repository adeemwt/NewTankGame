package com.example.adeem.tankgame;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.provider.ContactsContract;
import android.renderscript.ScriptGroup.*;
import android.support.annotation.AnimRes;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsoluteLayout;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Transaction;
import com.firebase.client.ValueEventListener;

import junit.framework.Test;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Random;
import java.lang.Math;
import java.util.Timer;
import java.util.TimerTask;

//TODO :SHOOTING, HIGHSCORE BORES, SAVES KILLS, ~LIMITS, CHECKING FOR ACCEDENTS ,  HOST/GUEST
import classes.*;

public class inGameMap extends AppCompatActivity implements View.OnClickListener, SensorEventListener  {
    final Point EASY_SIZE  = new Point(1000,1000);
    final Point MEDUIM_SIZE  = new Point(1500,1500);
    final Point HARD_SIZE  = new Point(2000,2000);

    int playerNum = 1;
    final int MAX_TARGET_SIZE = 5;

    final int STEP = 10;
    final int CHOOSE_X =0;
    final int CHOOSE_Y = 1;
    TextView test;
    final int TARGET_NUM_EASY = 5;
    final int TARGET_NUM_MADUIM = 10;
    final int TARGET_NUM_HARD = 15;
    final double EPSILON = 0.000001;


    private SensorManager sManager;

    int targetNum = 0;
    ArrayList<Tank> tanks;
    ArrayList<Taget> targets;
    ArrayList<ImageView> TargetImages = new ArrayList<>();
    Point WidthAndHieght ;

    ImageButton ourTank;
//    private SensorManager SensorManager;
//    private Sensor Sensor;

    SharedPreferences prefs;
    String MY_PREFS_NAME = "usernamePREFS";
    Timer t;
int seconds;
    ImageView backGround;
    private Button up;
    private Button left;
    private Button right;
    private Button down;

    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_game_map);


        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String restoredText = prefs.getString("difficultly", null);
        ourTank = (ImageButton) findViewById(R.id.ourTank);
        test =(TextView) findViewById(R.id.gyrosxample);
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
        up.setOnClickListener(this);
        down.setOnClickListener(this);
        left.setOnClickListener(this);
        right.setOnClickListener(this);

        up.setVisibility(View.GONE);
        down.setVisibility(View.GONE);
        left.setVisibility(View.GONE);
        right.setVisibility(View.GONE);

        backGround = (ImageView) findViewById(R.id.limitsView);
        sManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sManager.registerListener(this,
                sManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
        if(restoredText!=null){
            switch (restoredText){
                case("easy") : {
                    this.WidthAndHieght = this.EASY_SIZE;
                    targetNum = TARGET_NUM_EASY;
                    break;
                }
                case("medium") :{
                    this.WidthAndHieght = this.MEDUIM_SIZE;
                    targetNum = TARGET_NUM_MADUIM;
                    break;
                }
                case("hard"):{
                    this.WidthAndHieght = this.HARD_SIZE;
                    targetNum = TARGET_NUM_HARD;
                    break;
                }
            }
            randTargets(targetNum);
            String userName = prefs.getString("username", null);

            Random rnd = new Random();
            Color c = new Color();
            c.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
            tanks = new ArrayList<> (playerNum);
            tanks.add(new Tank(new Player(userName),c));
            ourTank.setOnClickListener(this);
          //  sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

            AbsoluteLayout rlayout = (AbsoluteLayout) findViewById(R.id.mainlayout);
            rlayout.setOnClickListener(this);

            rlayout.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN){
                        double alpha1;
                       double Xs= Math.pow(event.getX()-ourTank.getX(),2);
                        double Ys =        Math.pow(event.getY() - ourTank.getY(),2);
                        double sqrt =Math.sqrt(Xs + Ys );

                        alpha1 = Math.asin((event.getY() - ourTank.getY())/sqrt);
                       // alpha1 = Math.atan((event.getX() - ourTank.getX()) /(event.getY() - ourTank.getY()));
                        alpha1 =  alpha1 * 180 / Math.PI;
                //        test.setText("before ROTATION" + alpha1);

                        if(ourTank.getX() <= event.getX())
                         alpha1   += 90;
                        else{

                            alpha1 = 270 -alpha1;
                        }

                        ourTank.setRotation((float)alpha1);
                //        test.setText(test.getText() +"\nROTATION" + alpha1 + "\ntank cords (" +ourTank.getX()+","+ourTank.getY()+")" );
                 //       test.setText(test.getText() +"\nTouch coordinates : " +
                //                String.valueOf(event.getX()) + "," + String.valueOf(event.getY()));
                    }
                    return true;
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        int buttonId = view.getId();
        switch(buttonId){
            case(R.id.upBTN):
                moveTargets(CHOOSE_Y,STEP);
                break;
            case(R.id.leftBTN):
                moveTargets(CHOOSE_X,STEP);
                break;
            case(R.id.rightBTN):
                moveTargets(CHOOSE_X,-STEP);
                break;
            case(R.id.downBTN):
                moveTargets(CHOOSE_Y,-STEP);
                break;
            case(R.id.ourTank): {
                Bullet bullet = new Bullet(this.TargetImages,ourTank.getRotation(),tanks.get(0),new Point((int)ourTank.getX(),(int)ourTank.getY()));
                ArrayList<ImageView> targets= TargetImages;
                this.TargetImages = bullet.shoot();

                for(int i =0 ; i < TargetImages.size()-1; i++){
                    if(TargetImages.get(i).getVisibility() == View.GONE){
//                        final int z = i;
//
//                        TargetImages.get(z).setVisibility(View.VISIBLE);
//                        TargetImages.get(z).setImageResource(R.drawable.deadgoat);
//                        Handler handler = new Handler();
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                TargetImages.get(z).setVisibility(View.GONE);
//                            }
//                        }, 150);
                        targets.remove(TargetImages.get(i));
                    }
                }

                TargetImages = targets;
                if(TargetImages.size()==1){
                    this.saveAndExit();
                }
                break;
            }
            case(R.id.mainlayout):{
           //     test.setText("x  = "+view.getX() + "y= "+ view.getY());
            }
        }
    }


    public void  moveTargets(int xOry, int movement){
       // test.setText("ATTEMP  PPVKKKKKKKKKP P P P moved in X CORDS");
    movement*=3;
        test.setText("height  width " +backGround.getLayoutParams().height+
        "tank  x = " +ourTank.getX() + "y = " +ourTank.getY() + "\n backgourd (" + backGround.getX() +", " + backGround.getY() +") movement " + movement);
        if(xOry == CHOOSE_X){
           // test.setText("ATTEMP  PPP P P P moved in X CORDS");
            if(movement <  0 ) {

            }
            else{
                if(ourTank.getX()  < backGround.getX()) return;
            }
           //if(ourTank.getX() > backGround.getX() && ourTank.getX() > backGround.getX()+backGround.getWidth()) {
               for (int i = 0; i < TargetImages.size(); i++)
                   TargetImages.get(i).setX(TargetImages.get(i).getX() + movement);
               if (movement < 0)
                   ourTank.setRotation(90);
               else ourTank.setRotation(-90);
        //   }
        }
        else{
            if(movement <  0 ) {

            }
            else{
                if(ourTank.getY()  < backGround.getY()) return;
            }
          // if(ourTank.getY() < backGround.getY() && ourTank.getY() > backGround.getY()+backGround.getWidth()) {
                for (int i = 0; i < TargetImages.size(); i++)
                    TargetImages.get(i).setY(TargetImages.get(i).getY() + movement);
                if (movement < 0)
                    ourTank.setRotation(180);
                else ourTank.setRotation(0);
          //  }

            //todo MAKE THIS WORK
        }
    }
    public void randTargets(int targetNum){
        targets = new ArrayList<>(targetNum);


        final float scale = getResources().getDisplayMetrics().density;
        int pixels = (int) (WidthAndHieght.x * scale + 0.5f);

      //  int dimensionInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, WidthAndHieght.x, getResources().getDisplayMetrics());
        backGround.getLayoutParams().height = pixels;
        backGround.getLayoutParams().width =pixels;
        backGround.requestLayout();
        ImageView img;

        ///I CHANGED THIS
        for(int i = 0 ; i< targetNum ; i++){
            int x = (int)(Math.random() * this.WidthAndHieght.x- 55) + 55;
            int y = (int)(Math.random() * this.WidthAndHieght.y - 55) + 55;
            int size = (int)(Math.random() *MAX_TARGET_SIZE);
            targets.add(new Taget(new Point(x,y),size));
            img =(ImageView) findViewById(imgIds[i]);
            img.setX(targets.get(i).getCords().x);
            img.setY(targets.get(i).getCords().y);
            img.setVisibility(View.VISIBLE);

        //    test.setText(test.getText() +"\n("+ x +"," + y +")");
            TargetImages.add(img);
        }
        TargetImages.add(backGround);
    //    test.setText("initial \ntank  x = " +ourTank.getX() + "y = " +ourTank.getY() + "\n backgourd (" + backGround.getX() +", " + backGround.getY() );

    }


    public void onSensorChanged(SensorEvent event) { // the same
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

         //   test.setText("the Gyro is : \n  x = "+deltaRotationVector[0] +"\n y = "+deltaRotationVector[1] +"\n z = "+ deltaRotationVector[2] +"\n fourth one ... = "+deltaRotationVector[3]);
        }
        timestamp = event.timestamp;
        float[] deltaRotationMatrix = new float[9];

        SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
        for(int i =0 ; i < TargetImages.size() ; i++) {
            TargetImages.get(i).setY(TargetImages.get(i).getY() -deltaRotationVector[1]*50);
            TargetImages.get(i).setX(TargetImages.get(i).getX() +deltaRotationVector[0]*50);
        }

        double angel = Math.atan((deltaRotationVector[0]) /(deltaRotationVector[1]));
        angel =  angel * 180 / Math.PI;

       // test.setText(test.getText() + "\nangle = " +(int)angel);


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


    public void saveAndExit() {

        String name  = prefs.getString("username", null);
        final Firebase mRef;
        mRef = new Firebase("https://tankgameproject-85eb4.firebaseio.com/" + name + "_score");//here we copy the url ... so the "users" here is kind of a key that gets a value
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


