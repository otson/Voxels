package voxels.ChunkManager;

import java.io.Serializable;
import voxels.Voxels;

/**
 *
 * @author otso
 */
public class Chunk implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int CHUNK_SIZE = 16;
    public static final int VERTICAL_CHUNKS = 16;
    public static final int WORLD_HEIGHT = CHUNK_SIZE * VERTICAL_CHUNKS;
    public static final int WATER_HEIGHT = -1;
    public static final int FORCED_AIR_LAYERS = 5;
    public static final float GROUND_SHARE = 0.9f;

    //3d noise min and max values
    private static final float noiseOneMin = 0.7f;
    private static final float noiseOneMax = 1f;
    private static final float noiseTwoMin = 0f;
    private static final float noiseTwoMax = 0.7f;

    private int vboVertexHandle;
    private int vboNormalHandle;
    private int vboTexHandle;
    private int vboColorHandle;
    private int vertices;

    public final int xCoordinate;
    public final int zCoordinate;
    public final int yCoordinate;
    public final int xId;
    public final int zId;
    public final int yId;

    public Block[][][] blocks;
    public int[][] maxHeights;

    public Chunk(int xId, int yId, int zId) {
        this.xId = xId;
        this.zId = zId;
        this.yId = yId;
        xCoordinate = xId * CHUNK_SIZE;
        zCoordinate = zId * CHUNK_SIZE;
        yCoordinate = yId * CHUNK_SIZE;
        initMaxHeights();
        setBlocks();
    }

    private void initMaxHeights() {
        maxHeights = new int[CHUNK_SIZE][CHUNK_SIZE];
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                maxHeights[x][z] = Voxels.getNoise(x + xCoordinate, z + zCoordinate);
            }
        }
    }

    private void setBlocks() {
        blocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
        for (int x = 0; x < blocks.length; x++) {
            blocks[x] = new Block[CHUNK_SIZE][CHUNK_SIZE];
            for (int y = 0; y < blocks[x].length; y++) {
                blocks[x][y] = new Block[CHUNK_SIZE];
                for (int z = 0; z < blocks[x][y].length; z++) {

                    // Make the terrain using 2d noise
                    if (y + Chunk.CHUNK_SIZE * yId <= maxHeights[x][z] && y + Chunk.CHUNK_SIZE * yId < VERTICAL_CHUNKS * CHUNK_SIZE - FORCED_AIR_LAYERS) {
                        blocks[x][y][z] = new Block(Type.DIRT);
                        if(y == 0 && yId == 1)
                            blocks[x][y][z] = new Block(Type.UNBREAKABLE);
                    } else {
                        blocks[x][y][z] = new Block(Type.AIR);
                    }

                    // add 3d noise if enabled
                    if (Voxels.USE_3D_NOISE) {

                        //only add 3d noise to the upper part of the world (clouds)
                        if (y + Chunk.CHUNK_SIZE * yId > WORLD_HEIGHT * GROUND_SHARE) {
                            float noise1 = Voxels.get3DNoise(x + xCoordinate, y + yCoordinate, z + zCoordinate) / (float) (CHUNK_SIZE * VERTICAL_CHUNKS);
                            if (noise1 > 0.90f && y + Chunk.CHUNK_SIZE * yId < VERTICAL_CHUNKS * CHUNK_SIZE - FORCED_AIR_LAYERS) {
                                blocks[x][y][z] = new Block(Type.CLOUD);
                            }
                            // modify the ground portion of the world (caves)
                        } else if (yId != 1 || y != 0) {

                            float noise1 = Voxels.get3DNoise(x + xCoordinate, y + yCoordinate, z + zCoordinate) / (float) (CHUNK_SIZE * VERTICAL_CHUNKS);
                            float noise2 = Voxels.get3DNoise(x + xCoordinate + 10000, y + yCoordinate + 10000, z + zCoordinate + 10000) / (float) (CHUNK_SIZE * VERTICAL_CHUNKS);

                            if (noise1 > noiseOneMin && noise2 < noiseTwoMax) {
                                blocks[x][y][z] = new Block(Type.AIR);
                            }
                        }
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
