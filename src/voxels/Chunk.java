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
    private int vboColorHandle;
    private int vboVertexHandle;
    private int vboNormalHandle;
    private int vboTexHandle;
    private int vertices;

    public Block[][][] blocks;

    public int[][] maxHeights;
    public int[][] outerLimits;
    public int[][] diffToChunks;

    public Chunk(int xOff, int zOff) {

        X_OFF = xOff * (CHUNK_WIDTH);
        Z_OFF = zOff * (CHUNK_WIDTH);
        blocks = new Block[CHUNK_WIDTH][CHUNK_HEIGHT][CHUNK_WIDTH];
        maxHeights = new int[CHUNK_WIDTH][CHUNK_WIDTH];
        outerLimits = new int[CHUNK_WIDTH + 2][CHUNK_WIDTH + 2];
        diffToChunks = new int[CHUNK_WIDTH][CHUNK_WIDTH];

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
        //System.out.println("Total blocks in the chunk: " + blockCount);

        setActiveBlocks();

    }

    private void setActiveBlocks() {
        int activeBlocks = 0;
        int maxHeight;
        for (int x = 0; x < blocks.length; x++) {
            for (int z = 0; z < blocks[x][0].length; z++) {
                maxHeight = Voxels.getNoise(x + X_OFF, z + Z_OFF);
                maxHeights[x][z] = maxHeight;
            }
        }
        initOuterLimits();

        for (int x = 0; x < blocks.length; x++) {
            for (int z = 0; z < blocks[x][0].length; z++) {
                for (int y = 0; y < blocks[x].length; y++) {
                    if (y == maxHeights[x][z]) {
                        blocks[x][y][z].activate();
                        activeBlocks++;
                    }
                    
                    else if (x == 0 && y < maxHeights[x][z] && y >= Math.min(Math.min(Voxels.getNoise(x+X_OFF-1, z+Z_OFF),Voxels.getNoise(x+X_OFF+1, z+Z_OFF)), Math.min(Voxels.getNoise(x+X_OFF, z+Z_OFF+1),Voxels.getNoise(x+X_OFF, z+Z_OFF-1)))) {
                        blocks[x][y][z].activate();
                        activeBlocks++;
                    }
                    else if (x == blocks.length - 1 && y < maxHeights[x][z] && y >= Math.min(Math.min(Voxels.getNoise(x+X_OFF-1, z+Z_OFF),Voxels.getNoise(x+X_OFF+1, z+Z_OFF)), Math.min(Voxels.getNoise(x+X_OFF, z+Z_OFF+1),Voxels.getNoise(x+X_OFF, z+Z_OFF-1)))) {
                        blocks[x][y][z].activate();
                        activeBlocks++;
                    }
                    else if (z == 0 && y < maxHeights[x][z] && y >= Math.min(Math.min(Voxels.getNoise(x+X_OFF-1, z+Z_OFF),Voxels.getNoise(x+X_OFF+1, z+Z_OFF)), Math.min(Voxels.getNoise(x+X_OFF, z+Z_OFF+1),Voxels.getNoise(x+X_OFF, z+Z_OFF-1)))+1) {
                        blocks[x][y][z].activate();
                        activeBlocks++;
                    }
                    else if (z == blocks[x][y].length - 1 && y < maxHeights[x][z] && y >= Math.min(Math.min(Voxels.getNoise(x+X_OFF-1, z+Z_OFF),Voxels.getNoise(x+X_OFF+1, z+Z_OFF)), Math.min(Voxels.getNoise(x+X_OFF, z+Z_OFF+1),Voxels.getNoise(x+X_OFF, z+Z_OFF-1)))) {
                        blocks[x][y][z].activate();
                        activeBlocks++;
                    }
                }
            }
        }
        System.out.println("Blocks activated in the first loop: " + activeBlocks);
        // second loop, activate blocks in steps that are higher than one block
        int heightDifference;
        for (int x = 1; x < blocks.length - 1; x++) {
            for (int z = 1; z < blocks[x][0].length - 1; z++) {
                heightDifference = maxHeights[x][z] - Math.min(Math.min(maxHeights[x + 1][z], maxHeights[x - 1][z]), Math.min(maxHeights[x][z + 1], maxHeights[x][z - 1]));
                if (heightDifference > 1)
                    for (int y = maxHeights[x][z] - heightDifference; y < maxHeights[x][z]; y++) {
                        blocks[x][y][z].activate();
                        activeBlocks++;
                    }
            }
        }
        System.out.println("Total blocks activated: " + activeBlocks);
    }

    public int[][] getMaxHeights() {
        return maxHeights;
    }

    public int getVboColorHandle() {
        return vboColorHandle;
    }

    public void setVboColorHandle(int vboColorHandle) {
        this.vboColorHandle = vboColorHandle;
    }

    public int getVboVertexHandle() {
        return vboVertexHandle;
    }

    public void setVboVertexHandle(int vboVertexHandle) {
        this.vboVertexHandle = vboVertexHandle;
    }

    public int getVertices() {
        return vertices;
    }

    public void setVertices(int vertices) {
        this.vertices = vertices;
    }

    public void setVboNormalHandle(int vboNormalHandle) {
        this.vboNormalHandle = vboNormalHandle;
    }

    public int getVboNormalHandle() {
        return vboNormalHandle;
    }

    public int getVboTexHandle() {
        return vboTexHandle;
    }

    public void setVboTexHandle(int vboTexHandle) {
        this.vboTexHandle = vboTexHandle;
    }

    private void initOuterLimits() {
        for (int x = 0; x < outerLimits.length; x++) {
            for (int z = 0; z < outerLimits[x].length; z++) {
                outerLimits[x][z] = Voxels.getNoise(x + X_OFF + 1, z + Z_OFF + 1);
            }
        }
        for (int x = 0; x < diffToChunks.length; x++) {
            for (int z = 0; z < diffToChunks[x].length; z++) {
                if (x == 0 || z == 0 || x == diffToChunks.length - 1 || z == diffToChunks[x].length - 1) {
                    if (x == 0 && z == 0) {
                        diffToChunks[x][z] = maxHeights[x][z] - Math.min(outerLimits[0][1], outerLimits[1][0]);
                    }
                    else if (x == 0 && z == diffToChunks[x].length - 1) {
                        diffToChunks[x][z] = maxHeights[x][z] - Math.min(outerLimits[0][outerLimits[x].length - 2], outerLimits[1][outerLimits[x].length - 1]);
                    }
                    else if (x == diffToChunks.length - 1 && z == 0) {
                        diffToChunks[x][z] = maxHeights[x][z] - Math.min(outerLimits[outerLimits.length - 2][0], outerLimits[outerLimits.length - 1][1]);
                    }
                    else if (x == diffToChunks.length - 1 && z == diffToChunks[x].length - 1) {
                        diffToChunks[x][z] = maxHeights[x][z] - Math.min(outerLimits[outerLimits.length - 2][outerLimits[x].length - 2], outerLimits[outerLimits.length - 1][outerLimits[x].length - 1]);
                    }
                    else if (x == 0) {
                        diffToChunks[x][z] = maxHeights[x][z] - outerLimits[0][z];
                    }
                    else if (x == diffToChunks.length - 1) {
                        diffToChunks[x][z] = maxHeights[x][z] - outerLimits[outerLimits.length - 1][z];
                    }
                    else if (z == 0) {
                        diffToChunks[x][z] = maxHeights[x][z] - outerLimits[x][0];
                    }
                    else if (z == diffToChunks[x].length - 1) {
                        diffToChunks[x][z] = maxHeights[x][z] - outerLimits[x][outerLimits[x].length - 1];
                    }
                }
            }
            System.out.println("This chunks lower left corner: " + maxHeights[0][15]);
        }
        //int count = 0;
//        for (int x = 0; x < blocks.length; x++) {
//            for (int z = 0; z < blocks[x][0].length; z++) {
//                if (x == 0 || z == 0 || x == blocks.length - 1 || z == blocks[x].length - 1) {
//                    if (diffToChunks[x][z] > 0) {
//                        for (int y = maxHeights[x][z] - diffToChunks[x][z]; y < maxHeights[x][z]; y++) {
//                            if (blocks[x][y][z].isActive() == false) {
//                                blocks[x][y][z].activate();
//                                count++;
//                            }
//                        }
//                    }
//                }
//            }
//        }
        //System.out.println("Activated blocks in outer limits loop: " + count);
        System.out.println("front block lower left corner should at height: " + outerLimits[0][1]);
    }
}
