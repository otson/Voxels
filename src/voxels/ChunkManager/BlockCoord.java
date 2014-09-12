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
public class BlockCoord {
    
    public short Type;
    public int x;
    public int y;
    public int z;

    public BlockCoord(short Type, int x, int y, int z) {
        this.Type = Type;
        this.x = x;
        this.y = y;
        this.z = z;
    }

}
