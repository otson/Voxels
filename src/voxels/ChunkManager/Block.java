package voxels.ChunkManager;

import java.io.Serializable;

/**
 *
 * @author otso
 */
public class Block implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    private short type;
    private boolean active;

    public Block(short type) {
        this.type = type;
    }

    public void setType(short type) {
        this.type = type;
    }
    public boolean is(short type){
        return this.type == type;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    
}
