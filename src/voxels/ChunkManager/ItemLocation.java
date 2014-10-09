/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels.ChunkManager;

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

    private static float fallSpeedInc = 0.016f;

    public ItemLocation(float x, float y, float z, byte type) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        rotY = 0;
        fallingSpeed = 0;
    }

    public void rotate() {
        rotY = (rotY + 0.5f) % 360;
    }

    public void fall() {
        y -= fallingSpeed;
        fallingSpeed += fallSpeedInc;
    }

    public float getFallingSpeed() {
        return fallingSpeed;
    }

    public void setFallingSpeed(float fallingSpeed) {
        this.fallingSpeed = fallingSpeed;
    }
    
}
