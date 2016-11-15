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

import voxels.Noise.RandomNumber;

/**
 *
 * @author otso
 */
public class ItemLocation {

    public byte type;
    public float x;
    public float y;
    public float z;
    public float rotY;

    private float fallingSpeed;
    private float xSpeed;
    private float zSpeed;
    private float rSpeed;

    private static float fallSpeedInc = 0.013f;

    public ItemLocation(float x, float y, float z, byte type) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        rotY = 0;
        fallingSpeed = (float) (-0.20f-RandomNumber.getRandom()*0.06f);
        xSpeed = (float) (0.05f-0.1f*RandomNumber.getRandom());
        zSpeed = (float) (0.05f-0.1f*RandomNumber.getRandom());
    }

    public void rotate() {
        rotY = (rotY + 0.5f) % 360;
    }

    public void fall() {
        y -= fallingSpeed;
        fallingSpeed += fallSpeedInc;
    }

    public float getxSpeed() {
        return xSpeed;
    }

    public void setxSpeed(float xSpeed) {
        this.xSpeed = xSpeed;
    }

    public float getzSpeed() {
        return zSpeed;
    }

    public void setzSpeed(float zSpeed) {
        this.zSpeed = zSpeed;
    }
    
    

    public float getFallingSpeed() {
        return fallingSpeed;
    }

    public void setFallingSpeed(float fallingSpeed) {
        this.fallingSpeed = fallingSpeed;
    }
    
}
