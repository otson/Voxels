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

    public static final int CHUNK_WIDTH = 4;
    public static final int CHUNK_HEIGHT = 4;

    public Block[][][] blocks;

    public Chunk() {
        blocks = new Block[CHUNK_WIDTH][CHUNK_HEIGHT][CHUNK_WIDTH];

//        for (int i = 0; i < blocks.length; i++) {
//            blocks[i] = new Block[CHUNK_HEIGHT][CHUNK_WIDTH];
//            for (int j = 0; j < blocks[i].length; j++) {
//                blocks[i][j] = new Block[CHUNK_WIDTH];
//                for (int z = 0; z < blocks[i][j].length; z++) {
//                    blocks[i][j][z] = new Block();
//                }
//            }
//        }
    }

}
