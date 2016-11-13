package classes;

import android.graphics.Point;
import android.widget.ImageButton;

import java.lang.annotation.Target;

/**
 * Created by adeem on 18/09/2016.
 */
public class MyTarget {
    Point cords;
    int Diameter;
    boolean shot;


    public Point getCords() {
        return cords;
    }

    public void setCords(Point cords) {
        this.cords = cords;
    }

    public int getDiameter() {
        return Diameter;
    }

    public void setDiameter(int diameter) {
        Diameter = diameter;
    }

    public boolean isShot() {
        return shot;
    }

    public void setShot(boolean shot) {
        this.shot = shot;
    }

    public MyTarget(Point point , int diamemter){
        this.cords = point;
        this.Diameter = diamemter;
        this.shot = false;
    }
    public void shoot(){
        this.Diameter -=1;
        if(this.Diameter ==0){
            this.shot = true;
        }
    }

}
