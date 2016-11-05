package classes;

import android.graphics.Point;
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

    public  Bullet(ArrayList<ImageView> targets, float angle, Tank tank, MyPoint tankPosition){
        this.allTarget =  targets;
        this.headingAngle = angle;
        this.tank = tank;
        this.tankPosition = tankPosition;
    }
    public  Bullet(){
    }

    public ArrayList<ImageView> shoot(){
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



