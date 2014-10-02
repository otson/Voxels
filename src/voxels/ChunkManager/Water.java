/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels.ChunkManager;

import java.io.Serializable;

/**
 *
 * @author otso
 */
public class Water implements Serializable{

    public int x;
    public int y;
    public int z;
    public int xw;
    public int yw;
    public int zw;

    private int level;
    private boolean fresh;

    public Water(int x, int y, int z, int xw, int yw, int zw, int level) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.xw = xw;
        this.yw = yw;
        this.zw = zw;
        this.level = level;
        fresh = true;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void decreaseLevel(int decrease) {
        this.level -= decrease;
    }

    public void increaseLevel(int increase) {
        this.level += increase;
    }

    public boolean isFresh() {
        return fresh;
    }

    public void setFresh(boolean fresh) {
        this.fresh = fresh;
    }
    
}
