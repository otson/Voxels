package voxels;

import java.io.Serializable;

/**
 *
 * @author otso
 */
public class Block implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    public static final int AIR = 0;
    public static final int GROUND = 1;
    public static final int WATER = 2;
    
    private int type;

    public Block(int type) {
        this.type = type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isType(int type) {
        return this.type == type;
    }

}
