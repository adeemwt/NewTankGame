package classes;

import android.graphics.Point;

import java.lang.annotation.Target;
import java.util.ArrayList;

/**
 * Created by adeem on 14/09/2016.
 *
 *
 d.	Map(tanks)
 i.	Update map (updates the info)

 */
public class Map {
    ArrayList<Tank> tanks;
    ArrayList<Target> targets;
    Point WidthAndHieght;
//delete it ?
    public void Map(ArrayList<Tank> tanks, ArrayList<Target> targets, Point WidthAndHeight)
    {
        this.tanks = tanks;
        this.WidthAndHieght = WidthAndHeight;
        this.targets = targets;
    }
    public void UpdatePlayerPosition(String playername, Point point){
//        if(tanks.contains(tank)){
//            tanks.get(tanks.indexOf(tank)).setPosition(point);
//        }

    }



}
