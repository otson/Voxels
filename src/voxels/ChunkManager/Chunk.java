package voxels.ChunkManager;

import java.io.Serializable;
import sun.text.CompactByteArray;
import voxels.Voxels;

/**
 *
 * @author otso
 */
public class Chunk implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int CHUNK_WIDTH = 16;
    public static final int CHUNK_HEIGHT = 256;
    public static final int WATER_HEIGHT = 128;

    private int vboVertexHandle;
    private int vboNormalHandle;
    private int vboTexHandle;
    private int vboColorHandle;
    private int vertices;

    public final int xCoordinate;
    public final int zCoordinate;
    public final int xId;
    public final int zId;

    public Block[][][] blocks;
    public int[][] maxHeights;

    public Chunk(int xId, int zId) {
        this.xId = xId;
        this.zId = zId;
        xCoordinate = xId * CHUNK_WIDTH;
        zCoordinate = zId * CHUNK_WIDTH;
        initMaxHeights();
        setBlocks();
        setActiveBlocks();
    }

    private void initMaxHeights() {
        maxHeights = new int[CHUNK_WIDTH][CHUNK_WIDTH];
        for (int x = 0; x < CHUNK_WIDTH; x++) {
            for (int z = 0; z < CHUNK_WIDTH; z++) {
                maxHeights[x][z] = Voxels.getNoise(x + xCoordinate, z + zCoordinate);
            }
        }
    }

    private void setBlocks() {
        blocks = new Block[CHUNK_WIDTH][CHUNK_HEIGHT][CHUNK_WIDTH];
        for (int x = 0; x < blocks.length; x++) {
            blocks[x] = new Block[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < blocks[x].length; y++) {
                blocks[x][y] = new Block[CHUNK_WIDTH];
                for (int z = 0; z < blocks[x][y].length; z++) {
                    if (y > maxHeights[x][z] && y <= Chunk.WATER_HEIGHT) {
                        blocks[x][y][z] = new Block(Type.WATER);
                    }
                    else if (y <= maxHeights[x][z])
                        blocks[x][y][z] = new Block(Type.DIRT);
                    else
                        blocks[x][y][z] = new Block(Type.AIR);
                }
            }
        }
    }

    private void setActiveBlocks() {
        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[x].length; y++) {
                for (int z = 0; z < blocks[x][y].length; z++) {
                    // if air, it is inactive
                    if (blocks[x][y][z].is(Type.AIR))
                        blocks[x][y][z].setActive(false);
                    // if dirt, if it surrounded by 6 dirt blocks, make it inactive
                    else if (blocks[x][y][z].is(Type.DIRT))
                        System.out.println("");
                    else if (blocks[x][y][z].is(Type.WATER))
                        // if water, if the block above it is not water, make it active
                        if (blocks[x][y + 1][z].is(Type.WATER) == false)
                            blocks[x][y][z].setActive(true);
                }
            }
        }
    }

    private void setGroundBlocks() {

        int difference = 0;
        for (int x = 0; x < blocks.length; x++) {
            for (int z = 0; z < blocks[x][0].length; z++) {
                if (x == 0 || z == 0 || x == blocks.length - 1 || z == blocks[x][0].length - 1)
                    // This shouldn't be min value of all 4 bordering block heights, since it can activate unnecessary blocks. Works for now.
                    difference = Math.min(Math.min(Voxels.getNoise(x + xCoordinate - 1, z + zCoordinate), Voxels.getNoise(x + xCoordinate + 1, z + zCoordinate)), Math.min(Voxels.getNoise(x + xCoordinate, z + zCoordinate + 1), Voxels.getNoise(x + xCoordinate, z + zCoordinate - 1)));

                for (int y = 0; y < blocks[x].length; y++) {

                    if (y == maxHeights[x][z]) {
                        blocks[x][y][z].type = Type.DIRT;
                    }
                    else if ((x == 0 || x == blocks.length - 1 || z == 0 || z == blocks[x][y].length - 1) && y < maxHeights[x][z] && y > difference) {
                        blocks[x][y][z].type = Type.DIRT;
                    }
                }
            }
        }
        // second loop, activate blocks in steps that are higher than one block
        int heightDifference;
        for (int x = 1; x < blocks.length - 1; x++) {
            for (int z = 1; z < blocks[x][0].length - 1; z++) {
                heightDifference = maxHeights[x][z] - Math.min(Math.min(maxHeights[x + 1][z], maxHeights[x - 1][z]), Math.min(maxHeights[x][z + 1], maxHeights[x][z - 1]));
                if (heightDifference > 1)
                    for (int y = maxHeights[x][z] - heightDifference; y < maxHeights[x][z]; y++) {
                        blocks[x][y][z].type = Type.DIRT;
                    }
            }
        }
    }

    public int[][] getMaxHeights() {
        return maxHeights;
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

    public int getVboColorHandle() {
        return vboColorHandle;
    }

    public void setVboColorHandle(int vboColorHandle) {
        this.vboColorHandle = vboColorHandle;
    }

}
