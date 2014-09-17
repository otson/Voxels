package voxels.ChunkManager;

import java.io.Serializable;

/**
 *
 * @author otso
 */
public class Block implements Serializable {

    private static final long serialVersionUID = 1L;

    private short type;

    public Block(short type) {
        this.type = type;
    }

    public final boolean is(short type) {
        return this.type == type;
    }

    public final void setType(short type) {
        this.type = type;
    }
    
    public final boolean isOpaque(){
        return type == Type.AIR;
    }

    public short getType() {
        return type;
    }
    

}
