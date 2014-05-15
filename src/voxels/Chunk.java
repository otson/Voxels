/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels;

import org.lwjgl.util.vector.Vector3f;

/**
 *
 * @author otso
 */
public class Chunk {

    public static final int CHUNK_WIDTH = 16;
    public static final int CHUNK_HEIGHT = 2;
    

    public Block[][][] blocks;

    public Chunk() {
        blocks = new Block[CHUNK_WIDTH][CHUNK_HEIGHT][CHUNK_WIDTH];

        for (int x = 0; x < blocks.length; x++) {
            blocks[x] = new Block[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < blocks[x].length; y++) {
                blocks[x][y] = new Block[CHUNK_WIDTH];
                for (int z = 0; z < blocks[x][y].length; z++) {
                    blocks[x][y][z] = new Block();
                }
            }
        }
        updateVisibility();
    }

    private void updateVisibility() {
        for (int x = 1; x < blocks.length-1; x++) {
            for (int y = 1; y < blocks[x].length-1; y++) {
                for (int z = 1; z < blocks[x][y].length-1; z++) {
                     if(!blocks[x+1][y][z].isActive() && 
                        !blocks[x-1][y][z].isActive() && 
                        !blocks[x][y+1][z].isActive() &&
                        !blocks[x][y-1][z].isActive() && 
                        !blocks[x][y][z+1].isActive() &&
                        !blocks[x][y][z-1].isActive())
                         
                         blocks[x][y][z].deactivate();
                }
            }
        }
    }

}
