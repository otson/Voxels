package voxels.ChunkManager;

import java.io.Serializable;

/**
 *
 * @author otso
 */
public class Block implements Serializable {

    private static final long serialVersionUID = 1L;

    private short type;
    private boolean active;

    private boolean left;
    private boolean right;
    private boolean top;
    private boolean bottom;
    private boolean front;
    private boolean back;

    public Block(short type) {
        this.type = type;
    }

    public void setType(short type) {
        this.type = type;
        active = false;
    }

    public boolean is(short type) {
        return this.type == type;
    }

    public boolean isActive() {
        return front || back || left || right || top || bottom;

    }

    public void setActive(boolean active) {
        this.active = active;
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

    public boolean isOpaque() {
        return this.type == Type.AIR || this.type == Type.WATER;
    }
}
