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

    public ItemLocation(float x, float y, float z, byte type) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
    }

}
