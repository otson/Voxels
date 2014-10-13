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

    private int level;
    private boolean fresh;
    private boolean active;
    private int prevLevel;

    public Water(int x, int y, int z,int level) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.level = -level;
        fresh = true;
        active = true;
        prevLevel = level;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;

    }

    public void decreaseLevel() {
        this.level--;

    }
    
    public void decreaseLevel(int i) {
        this.level-=i;

    }

    public void increaseLevel(int increase) {
        this.level += increase;
        active = true;
    }

    public boolean isFresh() {
        return fresh;
    }

    public void setFresh(boolean fresh) {
        this.fresh = fresh;
        active = true;
    }

    public boolean isActive() {
        return active;
    }

    public void resetActive(){
        prevLevel = level;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    
    
    
    
}
