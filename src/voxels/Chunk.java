/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package voxels;

/**
 *
 * @author otso
 */
public class Chunk {
    
    private static final int CHUNK_WIDTH = 16;
    private static final int CHUNK_HEIGHT = 16;
    
    private Block[][][] blocks;
    
    public Chunk(){
        blocks = new Block[CHUNK_WIDTH][CHUNK_HEIGHT][CHUNK_WIDTH];
    }
    
}
