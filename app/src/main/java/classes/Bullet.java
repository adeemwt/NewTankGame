package classes;

import android.graphics.Point;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by adeem on 14/09/2016.
 *
 * c.	Bullet(map, aim, tank) -
 i.	Functions –
 1.	ShootAndUpdate (the bullets calculates where the bullet hits from the current position and send an update to the map and updates the tank scores


 */
public class Bullet {

    float headingAngle;
    Tank tank;
    ArrayList<ImageView> allTarget;
    Point tankPosition;

    public  Bullet(ArrayList<ImageView> targets, float angle, Tank tank, Point tankPosition){
        this.allTarget =  targets;
        this.headingAngle = angle;
        this.tank = tank;
        this.tankPosition = tankPosition;
    }

    public ArrayList<ImageView> shoot(){
        double alpha2 = 0 ;
        double alpha1 = 0;
        double Xs =0;
        double Ys =0 ;
        String s = "";
        if(headingAngle <0) headingAngle = 360 + headingAngle;

        for(int i =0 ; i < allTarget.size()-1; i++)
        {

            if(allTarget.get(i).getVisibility() != View.GONE)
            Xs= Math.pow(allTarget.get(i).getX()-tankPosition.x,2);
            Ys =  Math.pow(allTarget.get(i).getY() - tankPosition.y,2);
            double sqrt =Math.sqrt(Xs + Ys );

            alpha1 = Math.asin((allTarget.get(i).getY() - tankPosition.y)/sqrt);
            // alpha1 = Math.atan((event.getX() - ourTank.getX()) /(event.getY() - ourTank.getY()));
            alpha1 =  alpha1 * 180 / Math.PI;

            if(tankPosition.x <= allTarget.get(i).getX())
                alpha1   += 90;
            else{

                alpha1 = 270 -alpha1;
            }

            if(alpha1 + 10 > headingAngle && alpha1 - 15 < headingAngle){
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



