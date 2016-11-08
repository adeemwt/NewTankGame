package classes;

import android.graphics.Point;
import android.media.Image;
import android.view.View;
import android.widget.ImageView;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by adeem on 14/09/2016.
 *
 * c.	Bullet(map, aim, tank) -
 i.	Functions –
 1.	ShootAndUpdate (the bullets calculates where the bullet hits from the current position and send an update to the map and updates the tank scores


 */
public class Bullet implements Serializable {

    float headingAngle;
    Tank tank;
    ArrayList<ImageView> allTarget;
    MyPoint tankPosition;

    int shooterIndex;
    ArrayList<Tank> tanks_arr = new ArrayList<>();
    boolean multi;

    //single player construstor
    public  Bullet(ArrayList<ImageView> targets, float angle, Tank tank, MyPoint tankPosition){
        this.allTarget =  targets;
        this.headingAngle = angle;
        this.tank = tank;
        this.tankPosition = tankPosition;
        multi=false;
    }
    //multi player constructor
    public  Bullet(ArrayList<Tank> tanks_Array,int IndexShooter){
        this.tanks_arr = tanks_Array;
        this.shooterIndex = IndexShooter;
        this.tank = tanks_arr.get(IndexShooter);
        this.tankPosition = tanks_arr.get(IndexShooter).getPosition();
        this.headingAngle =tanks_arr.get(IndexShooter).getheadingAngle();
        multi = true;

    }
    public ArrayList<?> shoot(){
        if(multi)
            return shooting(tanks_arr);
        else
            return shooting(allTarget);
    }

    public ArrayList<?> shooting(ArrayList<?> targets_){
        double alpha1 = 0;
        double Xs =0;
        double Ys =0;
        MyPoint position = new MyPoint(0,0);
        boolean shot_;
        if(headingAngle <0) headingAngle = 360 + headingAngle;


        for(int i =0 ; i <targets_.size()-1; i++)
        {

            if(multi) {
                position = ((ArrayList<Tank>) targets_).get(i).getPosition();
                shot_ =  ((ArrayList<Tank>) targets_).get(i).getShot();
            }
            else {
                position.x = (int)((ArrayList<ImageView>) targets_).get(i).getX();
                position.y = (int)((ArrayList<ImageView>) targets_).get(i).getY();
                shot_= ((ArrayList<ImageView>) targets_).get(i).getVisibility() == View.GONE;
            }

            //check if the target is visible to the user - (still in the game)
            if(shot_!= true)
                //Xs = ( X1 - X2 )^2
                Xs= Math.pow(position.x-tankPosition.x,2);

            //YS = ( Y1 - Y2 ) ^2
            Ys =  Math.pow(position.y - tankPosition.y,2);

            //sqrt = (Ys + Xs) ^ 0.5
            double sqrt =Math.sqrt(Xs + Ys );

            //alpha(rad) = sin((Y1 - Y2)/sqrt) (RAD   0-PI)
            alpha1 = Math.asin((position.y - tankPosition.y)/sqrt);

            //alpha(dig) = alpha(rad)*180/PI  (DIG  0-360)
            alpha1 =  alpha1 * 180 / Math.PI;

            //check if the tank is up or bellow the target  - calculate the alpha accordingly
            if(tankPosition.x <= position.x)
                alpha1   += 90;
            else{
                alpha1 = 270 -alpha1;
            }

            //check aim accuracy and set invisible if show
            if(alpha1 + 15 > headingAngle && alpha1 - 15 < headingAngle){
                //allTarget.get(i).setVisibility(View.GONE);
               // tanks_arr.get(i).setShot();
                if(multi)
                    ((ArrayList<Tank>) targets_).get(i).setShot();
                else
                    ((ArrayList<ImageView>) targets_).get(i).setVisibility(View.GONE);
            }

        }
        return targets_;
    }
/*
    public ArrayList<Tank> shootMulti(){
        double alpha1 = 0;
        double Xs =0;
        double Ys =0 ;
        if(headingAngle <0) headingAngle = 360 + headingAngle;

        for(int i =0 ; i < tanks_arr .size()-1; i++)
        {

            //check if the target is visible to the user - (still in the game)
            if(tanks_arr.get(i).getShot()!= true)

                //Xs = ( X1 - X2 )^2
                Xs= Math.pow(tanks_arr.get(i).getPosition().x-tankPosition.x,2);

            //YS = ( Y1 - Y2 ) ^2
            Ys =  Math.pow(tanks_arr.get(i).getPosition().y - tankPosition.y,2);

            //sqrt = (Ys + Xs) ^ 0.5
            double sqrt =Math.sqrt(Xs + Ys );

            //alpha(rad) = sin((Y1 - Y2)/sqrt) (RAD   0-PI)
            alpha1 = Math.asin((tanks_arr.get(i).getPosition().y - tankPosition.y)/sqrt);

            //alpha(dig) = alpha(rad)*180/PI  (DIG  0-360)
            alpha1 =  alpha1 * 180 / Math.PI;

            //check if the tank is up or bellow the target  - calculate the alpha accordingly
            if(tankPosition.x <= tanks_arr.get(i).getPosition().x)
                alpha1   += 90;
            else{
                alpha1 = 270 -alpha1;
            }

            //check aim accuracy and set invisible if show
            if(alpha1 + 15 > headingAngle && alpha1 - 15 < headingAngle){
                //allTarget.get(i).setVisibility(View.GONE);
                tanks_arr.get(i).setShot();
            }

        }
        return tanks_arr;
    }

    public ArrayList<ImageView> shootSingel(){
        double alpha1 = 0;
        double Xs =0;
        double Ys =0 ;
        if(headingAngle <0) headingAngle = 360 + headingAngle;

        for(int i =0 ; i < allTarget.size()-1; i++)
        {

            //check if the target is visible to the user - (still in the game)
            if(allTarget.get(i).getVisibility() != View.GONE)

            //Xs = ( X1 - X2 )^2
            Xs= Math.pow(allTarget.get(i).getX()-tankPosition.x,2);

            //YS = ( Y1 - Y2 ) ^2
            Ys =  Math.pow(allTarget.get(i).getY() - tankPosition.y,2);

            //sqrt = (Ys + Xs) ^ 0.5
            double sqrt =Math.sqrt(Xs + Ys );

            //alpha(rad) = sin((Y1 - Y2)/sqrt) (RAD   0-PI)
            alpha1 = Math.asin((allTarget.get(i).getY() - tankPosition.y)/sqrt);

            //alpha(dig) = alpha(rad)*180/PI  (DIG  0-360)
            alpha1 =  alpha1 * 180 / Math.PI;

            //check if the tank is up or bellow the target  - calculate the alpha accordingly
            if(tankPosition.x <= allTarget.get(i).getX())
                alpha1   += 90;
            else{
                alpha1 = 270 -alpha1;
            }

            //check aim accuracy and set invisible if show
            if(alpha1 + 15 > headingAngle && alpha1 - 15 < headingAngle){
                   allTarget.get(i).setVisibility(View.GONE);
            }

        }
        return allTarget;
    }
*/
    public Tank getTank() {
        return tank;
    }

    public float getHeadingAngle() {
        return headingAngle;
    }

    public void setHeadingAngle(float headingAngle) {
        this.headingAngle = headingAngle;
    }
}



