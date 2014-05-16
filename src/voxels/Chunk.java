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

    public static final int CHUNK_WIDTH = 16;
    public static final int CHUNK_HEIGHT = 256;

    public final int X_OFF;
    public final int Z_OFF;

    public Block[][][] blocks;

    public Chunk(int xOff, int zOff) {

        X_OFF = xOff * (CHUNK_WIDTH);
        Z_OFF = zOff * (CHUNK_WIDTH);
        blocks = new Block[CHUNK_WIDTH][CHUNK_HEIGHT][CHUNK_WIDTH];

        int blockCount = 0;
        for (int x = 0; x < blocks.length; x++) {
            blocks[x] = new Block[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < blocks[x].length; y++) {
                blocks[x][y] = new Block[CHUNK_WIDTH];
                for (int z = 0; z < blocks[x][y].length; z++) {
                    blocks[x][y][z] = new Block();
                    blockCount++;
                }
            }
        }
        System.out.println("Total blocks in the chunk: " + blockCount);
        setActiveBlocks();
    }

    private void setActiveBlocks() {
        int activeBlocks = 0;
        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[x].length; y++) {
                for (int z = 0; z < blocks[x][y].length; z++) {
                    if (y == 0 || y == Voxels.getNoise(x + X_OFF, z + Z_OFF)) {
                        blocks[x][y][z].activate();
                        activeBlocks++;
                    }
                    else if ((x == 0 || x == blocks.length - 1) && y < Voxels.getNoise(x + X_OFF, z + Z_OFF)) {
                        blocks[x][y][z].activate();
                        activeBlocks++;
                    }
                    else if ((z == 0 || z == blocks[x][y].length - 1) && y < Voxels.getNoise(x + X_OFF, z + Z_OFF)) {
                        blocks[x][y][z].activate();
                        activeBlocks++;
                    }
                }
            }
        }
        System.out.println("Active blocks in the chunk: " + activeBlocks);
    }
}
