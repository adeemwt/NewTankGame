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
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.Transaction;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Random;
import java.lang.Math;


//TODO : GYRO, SHOOTING, HIGHSCORE BORES, SAVES KILLS, LIMITS, CHECKING FOR ACCEDENTS ,SPINNER DIFF, SPINNER FOR MULTI, HOST/GUEST
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
            R.id.target5

    };

//    SensorManager sensorManager;
//    Sensor sensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_game_map);


        prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String restoredText = prefs.getString("difficultly", null); //we never chose this XD mybe thets whay the yegahyr o didnt work y
//      1sec

        ourTank = (ImageButton) findViewById(R.id.ourTank);
        test =(TextView) findViewById(R.id.gyrosxample);
        up = (Button) findViewById(R.id.upBTN);
        down = (Button) findViewById(R.id.downBTN);
        left= (Button) findViewById(R.id.leftBTN);
        right= (Button) findViewById(R.id.rightBTN);

        up.setOnClickListener(this);
        down.setOnClickListener(this);
        left.setOnClickListener(this);
        right.setOnClickListener(this);
//
//        SensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        Sensor = SensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED );
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

          //  sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
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
        }
    }

    public void  moveTargets(int xOry, int movement){
       // test.setText("ATTEMP  PPVKKKKKKKKKP P P P moved in X CORDS");

        if(xOry == CHOOSE_X){
           // test.setText("ATTEMP  PPP P P P moved in X CORDS");
            for(int i =0 ; i < TargetImages.size() ; i++)
                TargetImages.get(i).setX(TargetImages.get(i).getX()+movement);
            if(movement < 0 )
            ourTank.setRotation(90);
            else ourTank.setRotation(-90);
        }
        else{
            for(int i =0 ; i < TargetImages.size() ; i++)
                TargetImages.get(i).setY(TargetImages.get(i).getY()+movement);
            if( movement < 0 )
                ourTank.setRotation(180);
            else ourTank.setRotation(0);
        }
    }
    public void randTargets(int targetNum){
        targets = new ArrayList<>(targetNum);

        ImageView img;
        for(int i = 0 ; i<targetNum ; i++){
            int x = (int)(Math.random() * this.WidthAndHieght.x);
            int y = (int)(Math.random() * this.WidthAndHieght.y);
            int size = (int)(Math.random() *MAX_TARGET_SIZE);
            targets.add(new Taget(new Point(x,y),size));
            img =(ImageView) findViewById(imgIds[i]);
            img.setX(targets.get(i).getCords().x);
            img.setY(targets.get(i).getCords().y);
            img.setVisibility(View.VISIBLE);

        //    test.setText(test.getText() +"\n("+ x +"," + y +")");
            TargetImages.add(img);
        }

    }

//    public void onResume() {
//        super.onResume();
//        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
//    }
//
//    public void onStop() {
//        super.onStop();
//        sensorManager.unregisterListener(this);
//    }
//
//    // Create a constant to convert nanoseconds to seconds.// STOPSHIP: 19/09/2016
//    public void onAccuracyChanged(Sensor sensor, int acc) { }
//

    public void onSensorChanged(SensorEvent event) { // the same
        // This timestep's delta rotation to be multiplied by the current rotation
        // after computing it from the gyro sample data.
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
            // Integrate around this axis with the angular speed by the timestep
            // in order to get a delta rotation from this sample over the timestep
            // We will convert this axis-angle representation of the delta rotation
            // into a quaternion before turning it into the rotation matrix.
            float  thetaOverTwo = omegaMagnitude * dT / 2.0f;
            float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
            float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
            deltaRotationVector[0] = sinThetaOverTwo * axisX;
            deltaRotationVector[1] = sinThetaOverTwo * axisY;
            deltaRotationVector[2] = sinThetaOverTwo * axisZ;
            deltaRotationVector[3] = cosThetaOverTwo;

            test.setText("the Gyro is : \n  x = "+deltaRotationVector[0] +"\n y = "+deltaRotationVector[1] +"\n z = "+ deltaRotationVector[2] +"\n fourth one ... = "+deltaRotationVector[3]);
        }
        timestamp = event.timestamp;
        float[] deltaRotationMatrix = new float[9];

        SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
                for(int i =0 ; i < TargetImages.size() ; i++) {
            TargetImages.get(i).setY(TargetImages.get(i).getY() -deltaRotationVector[1]*50);
            TargetImages.get(i).setX(TargetImages.get(i).getX() +deltaRotationVector[0]*50);
        }
        double angle = Math.asin(deltaRotationVector[0]*100 / Math.sqrt(Math.pow(deltaRotationVector[0]*100,2)+Math.pow(deltaRotationVector[1]*100,2)));
//        if(deltaRotationVector[1] < 0)
//        ourTank.setRotation((float)-(deltaRotationVector[0])*1000);
//
//        else
            ourTank.setRotation((float)(-angle/Math.PI*180));

        test.setText(test.getText() + "\nthe angle is : " + angle + "turing from pi" + angle/Math.PI*180);

        // User code should concatenate the delta rotation we computed with the current rotation
        // in order to get the updated rotation.
        // rotationCurrent = rotationCurrent * deltaRotationMatrix;
    }

//
//
//    public void onResume() {
//        super.onResume();
//       // try {
//            sensorManager.registerListener(gyroListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
      //  }catch(Exception e ){
     ///        test.setText(test.getText()+"\n exception here ");
      //  }
//    }
//
//    public void onStop() {
//        super.onStop();
//        sensorManager.unregisterListener(gyroListener);
//    }
//
//    public SensorEventListener gyroListener = new SensorEventListener() {
//        public void onAccuracyChanged(Sensor sensor, int acc) { }
//
//        public void onSensorChanged(SensorEvent event) {
//            float x = event.values[0];
//            float y = event.values[1];
//            float z = event.values[2];
//
//            test.setText("X : " + (int)x + " rad/s" +"\nY : " + (int)y + " rad/s"+ "\nZ : " + (int)z + " rad/s");
//        }
//    };


@Override
protected void onResume()
{
    super.onResume();
        /*register the sensor listener to listen to the gyroscope sensor, use the
        callbacks defined in this class, and gather the sensor information as quick
        as possible*/
    //sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_FASTEST);
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

//    @Override
//    public void onSensorChanged(SensorEvent event)
//    {        //else it will output the Roll, Pitch and Yawn values
//
//        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
//        {
//            return;
//        }
//        for(int i =0 ; i < TargetImages.size() ; i++) {
//            TargetImages.get(i).setX(TargetImages.get(i).getX() +(event.values[1]-10)/2);
//            TargetImages.get(i).setY(TargetImages.get(i).getY() +(event.values[2]-10)/2);
//        }
//        test.setText("Orientation X (Roll) :"+ Float.toString(event.values[2]) +"\n"+
//                "Orientation Y (Pitch) :"+ Float.toString(event.values[1]) +"\n"+
//                "Orientation Z (Yaw) :"+ Float.toString(event.values[0]));
//        //if sensor is unreliable, return void
//
//    }
}


