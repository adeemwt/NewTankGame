package classes;

import android.graphics.Color;
import android.graphics.Point;

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
public class Tank implements Comparable<Tank> {
    private Point position;
    private int currentScore;
    private Player player;
    private Color colour;

    private float headingAngle;

    private boolean shot = false;

    public Tank(Player player,Color colour){
        this.position = new Point(0,0);//initial possitoin
        this.currentScore = 0;
        this.colour = colour;
        this.player = player;
    }

    public Player getPlayer(){
        return this.player;
    }
    public Color getColour(){
        return this.colour;
    }
    public Point getPosition() {
        return position;
    }

    public  void setShot(){
        this.shot = true;
    }


    public boolean getShot(){
        return shot;
    }

    public void setPosition(Point position) {
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
       return this.getPlayer().getName().compareTo(another.getPlayer().getName());
    }
}
