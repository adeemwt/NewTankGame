package classes;

import android.graphics.Color;
import android.graphics.Point;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by adeem on 14/09/2016.
 *
 *
 * a.	Tank(position, current score, colour, aim)
 i.	Functions –
 1.	Move to a place in map (which also changes the aiming)
 2.	Update current score
 3.	Shoot (make a new bullet class)
 4.	Class - Player  (var)

 */
public class Tank implements Comparable<Tank> , Serializable {
    private MyPoint position;
    private int currentScore;
    private Color colour;

    private float headingAngle;

    private boolean shot = false;

    public Tank(Color colour){
        this.position = new MyPoint(0,0);//initial possitoin
        this.currentScore = 0;
        this.colour = colour;
    }

    public Color getColour(){
        return this.colour;
    }
    public MyPoint getPosition() {
        return position;
    }

    public  void setShot(){
        this.shot = true;
    }


    public boolean getShot(){
        return shot;
    }

    public void setPosition(MyPoint position) {
        this.position = position;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }

    public void setColour(Color colour) {
        this.colour = colour;
    }

    public float getheadingAngle() {
        return headingAngle;
    }

    public void setheadingAngle(float headingAngle) {
        this.headingAngle = headingAngle;
    }

    @Override
    public int compareTo(Tank another) {
      // return this.getPlayer().getName().compareTo(another.getPlayer().getName());
        return this.currentScore - another.currentScore;
    }
}
