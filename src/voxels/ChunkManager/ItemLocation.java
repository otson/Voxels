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
    private float xSpeed;
    private float zSpeed;

    private static float fallSpeedInc = 0.013f;

    public ItemLocation(float x, float y, float z, byte type) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        rotY = 0;
        fallingSpeed = (float) (-0.15f-Math.random()*0.05f);
        xSpeed = (float) (0.02f-0.04f*Math.random());
        zSpeed = (float) (0.02f-0.04f*Math.random());
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
