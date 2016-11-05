package classes;

import java.io.Serializable;

/**
 * Created by ITSIK on 05/11/2016.
 */

public class MyPoint implements  Serializable {
    public int x;
    public int y;

    public MyPoint(int x, int y) {
        this.y = y;
        this.x = x;
    }
}
