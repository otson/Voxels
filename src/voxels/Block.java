package voxels;

import java.io.Serializable;

/**
 *
 * @author otso
 */
public class Block implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    public short type;

    public Block(short type) {
        this.type = type;
    }
}
