package voxels.ChunkManager;

import java.io.Serializable;

/**
 *
 * @author otso
 */
public class Block implements Serializable {

    private static final long serialVersionUID = 1L;

    private short type;
    
    private boolean left;
    private boolean right;
    private boolean front;
    private boolean back;
    private boolean top;
    private boolean bottom;

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
    
    public boolean isLeft() {
        return left;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public boolean isRight() {
        return right;
    }

    public void setRight(boolean right) {
        this.right = right;
    }

    public boolean isFront() {
        return front;
    }

    public void setFront(boolean front) {
        this.front = front;
    }

    public boolean isBack() {
        return back;
    }

    public void setBack(boolean back) {
        this.back = back;
    }

    public boolean isTop() {
        return top;
    }

    public void setTop(boolean top) {
        this.top = top;
    }

    public boolean isBottom() {
        return bottom;
    }

    public void setBottom(boolean bottom) {
        this.bottom = bottom;
    }

    public short getType() {
        return type;
    }
    

}
