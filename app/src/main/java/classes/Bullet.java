package classes;

/**
 * Created by adeem on 14/09/2016.
 *
 * c.	Bullet(map, aim, tank) -
 i.	Functions –
 1.	ShootAndUpdate (the bullets calculates where the bullet hits from the current position and send an update to the map and updates the tank scores


 */
public class Bullet {
    Map map;
    float headingAngle;
    Tank tank;

    public  Bullet(Map map, float angle, Tank tank){
        this.map = map;
        this.headingAngle = angle;
        this.tank = tank;
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



