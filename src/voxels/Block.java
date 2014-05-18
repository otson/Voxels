package voxels;

/**
 *
 * @author otso
 */
public class Block {

    private boolean active;

    public Block() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

}
