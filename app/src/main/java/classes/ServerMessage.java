package classes;

import java.util.ArrayList;

/**
 * Created by adeem on 03/11/2016.
 */

public class ServerMessage {
    //if a tank was shot we send the array with that tank or set the tank class as shot
    //objects in the starts, send them for all the clients to put in the map
    // update is for the ingame thing , gives the options to remove the shot tanks and make the passing faster
    public enum TYPE{UPDATE,OBJECTS};
    TYPE type ;
    ArrayList<Tank> tanks;

    public ServerMessage(){
       tanks = new ArrayList<Tank>();
    }
    public  void addTank(Tank tank)
    {
        this.tanks.add(tank);
    }
    public void removeTank(Tank tank){
        this.tanks.remove(tank);
    }
    public ArrayList<Tank> getTanks(){
        return this.tanks;
    }
    public void setType(TYPE  newType){
        this.type = newType;
    }
    public TYPE getType(){
        return this.type;
    }
}
