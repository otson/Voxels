package voxels.ChunkManager;

import java.io.Serializable;
import voxels.Voxels;

/**
 *
 * @author otso
 */
public class Chunk implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int CHUNK_WIDTH = 16;
    public static final int CHUNK_HEIGHT = 256;
    public static final int WATER_HEIGHT = -1;

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
        System.out.println((2.55f*Voxels.AIR_PERCENTAGE));
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
                    if (Voxels.USE_3D_NOISE) {
                        if (Voxels.get3DNoise(x + xCoordinate, y, z + zCoordinate) > (2.55f*Voxels.AIR_PERCENTAGE))
                            blocks[x][y][z] = new Block(Type.DIRT);
                        else
                            blocks[x][y][z] = new Block(Type.AIR);
                    }
                    else {
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
