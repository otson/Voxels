/* 
 * Copyright (C) 2016 Otso Nuortimo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
