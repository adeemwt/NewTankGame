package classes;

import android.graphics.Color;
import android.graphics.Point;

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
public class Tank { //player id? or a class player? איקe vy tank has a player what deos the player class do other than save the id and name? nothing .. but you need a conection so you can use it to save if we won or not okzy, then plalyer , less searches WAT?
    private Point position;
    private int currentScore;
    private Player player;
    private Color colour;//every tank has a player, and when it scores it saves ... i dont know what the player deos XD
    private float headingAngle;
    public Tank(Player player,Color colour){
        this.position = new Point(0,0);
        this.currentScore = 0;
        this.colour = colour;
        this.player = player;
    }
    public Color getColour(){
        return this.colour;
    }
    public Point getPosition() {
        return position;
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
}
