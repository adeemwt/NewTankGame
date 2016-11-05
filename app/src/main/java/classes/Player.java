package classes;

import java.io.Serializable;

/**
 * Created by adeem on 14/09/2016.
 */
public class Player implements Serializable {
    String name;
    public Player(String name){
        this.name = name;
    }
    public String getName(){
        return this.name;
    }
}
