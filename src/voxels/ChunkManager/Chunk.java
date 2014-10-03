package voxels.ChunkManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import voxels.Voxels;

/**
 *
 * @author otso
 */
public class Chunk implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int CHUNK_SIZE = 32;
    public static final int VERTICAL_CHUNKS = 8;
    public static final int WORLD_HEIGHT = CHUNK_SIZE * VERTICAL_CHUNKS;
    public static final int WATER_HEIGHT = -1;
    public static final int FORCED_AIR_LAYERS = 10;
    public static final float GROUND_SHARE = 0.9f;
    public static final int DIRT_LAYERS = 5;

    //3d noise min and max values
    public static final float noiseOneMin = 0.40f;
    public static final float noiseOneMax = 0.55f;
    public static final float noiseTwoMin = 0.40f;
    public static final float noiseTwoMax = 0.55f;

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

    //public Block[][][] blocks;
    public short[][] maxHeights;

    public byte[][][] blocks;

    private boolean updateActive = false;
    private boolean updatePacked = false;
    private boolean empty = false;

    private ArrayList<Water> waterArray;

    public Chunk(int xId, int yId, int zId) {
        blocks = new byte[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
        this.xId = xId;
        this.zId = zId;
        this.yId = yId;
        xCoordinate = xId * CHUNK_SIZE;
        zCoordinate = zId * CHUNK_SIZE;
        yCoordinate = yId * CHUNK_SIZE;
        waterArray = new ArrayList<>(64);
        initMaxHeights();
        //if(!empty || (Chunk.CHUNK_SIZE * yId+Chunk.CHUNK_SIZE) > WORLD_HEIGHT * GROUND_SHARE)
        setBlocks();
    }

    private void initMaxHeights() {
        maxHeights = new short[CHUNK_SIZE][CHUNK_SIZE];
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                maxHeights[x][z] = (short) Voxels.getNoise(x + xCoordinate, z + zCoordinate);
//                if(Chunk.CHUNK_SIZE*yId <= maxHeights[x][z])
//                    empty = false;
            }
        }
    }

    private void setBlocks() {
        for (int x = 0; x < blocks.length; x++) {
            for (int y = 0; y < blocks[x].length; y++) {
                for (int z = 0; z < blocks[x][y].length; z++) {

                    // Make the terrain using 2d noise
                    if (y + Chunk.CHUNK_SIZE * yId <= maxHeights[x][z] && y + Chunk.CHUNK_SIZE * yId < VERTICAL_CHUNKS * CHUNK_SIZE - FORCED_AIR_LAYERS) {
                        if (y == 0 && yId == 1) {
                            blocks[x][y][z] = Type.UNBREAKABLE;
                        } else if (y + Chunk.CHUNK_SIZE * yId <= maxHeights[x][z] - DIRT_LAYERS) {
                            blocks[x][y][z] = Type.STONE;
                        } else {
                            blocks[x][y][z] = Type.DIRT;
                        }
                    }
                    if (y == 0 && yId == 1) {
                        blocks[x][y][z] = Type.UNBREAKABLE;
                    }

                    // add 3d noise if enabled
                    if (Voxels.USE_3D_NOISE) {

                        //only add 3d noise to the upper part of the world (clouds)
                        if (y + Chunk.CHUNK_SIZE * yId > WORLD_HEIGHT * GROUND_SHARE) {
                            float noise1 = Voxels.get3DNoise(x + xCoordinate, y + yCoordinate, z + zCoordinate + 10) / (float) (CHUNK_SIZE * VERTICAL_CHUNKS);
                            if (noise1 > 0.90f && y + Chunk.CHUNK_SIZE * yId < VERTICAL_CHUNKS * CHUNK_SIZE - FORCED_AIR_LAYERS) {
                                blocks[x][y][z] = Type.CLOUD;
                            }
                            // modify the ground portion of the world (caves)
                        } else if (!empty && (yId != 1 || y != 0)) {

                            if (Voxels.getCaveNoise(x + xCoordinate, y + yCoordinate, z + zCoordinate)) {
                                blocks[x][y][z] = Type.AIR;
                            }

                        }
                        // add trees
                        if (y + Chunk.CHUNK_SIZE * yId == maxHeights[x][z] + 1) {
                            if (Voxels.getTreeNoise(x + CHUNK_SIZE * xId, y + yCoordinate - 1, z + CHUNK_SIZE * zId) == 0) {
                                createTree(x + CHUNK_SIZE * xId, y + yCoordinate, z + CHUNK_SIZE * zId);
                            }
                        }
                    }

                }
            }
        }

        //();
    }

    private static void createTree(int x, int y, int z) {
        int width = (int) (4 + Math.random() * 5);
        int height = (int) (5 + Math.random() * 12);
        boolean bigTree = false;
        if (Math.random() > 0.995f) {
            width *= 3;
            height *= 5;
            bigTree = true;

        }
        if(y+height >= WORLD_HEIGHT)
            height = WORLD_HEIGHT-1-y;
        // trunk
        for (int i = 0; i < height; i++) {
            Voxels.putToBuffer(Type.WOOD, x, y + i, z);
            if (bigTree) {
                Voxels.putToBuffer(Type.WOOD, x + 1, y + i, z + 1);
                Voxels.putToBuffer(Type.WOOD, x + 1, y + i, z);
                Voxels.putToBuffer(Type.WOOD, x + 1, y + i, z - 1);
                Voxels.putToBuffer(Type.WOOD, x, y + i, z + 1);
                Voxels.putToBuffer(Type.WOOD, x, y + i, z - 1);
                Voxels.putToBuffer(Type.WOOD, x - 1, y + i, z + 1);
                Voxels.putToBuffer(Type.WOOD, x - 1, y + i, z);
                Voxels.putToBuffer(Type.WOOD, x - 1, y + i, z - 1);
            }
        }
        int startingWidth = width;
        for (int yy = y + 1 + height / 3; yy < y + 1 + height / 3 + height; yy++) {
            for (int zz = -width / 2 + z; zz <= width / 2 + z; zz++) {
                for (int xx = -width / 2 + x; xx <= width / 2 + x; xx++) {
                    if (Math.random() < 0.65f) {
                        Voxels.putToBuffer(Type.LEAVES, xx, yy, zz);
                    }

                }
            }
            width = startingWidth - (int)(startingWidth * (((yy-height/2f-y)/ (float)(height-2))));
        }

    }

    public boolean checkBuffer() {
        boolean updated = false;
        if (Voxels.getBlockBuffer().containsKey(new Pair(xId, yId, zId).hashCode())) {

            BlockingQueue queue = Voxels.getBlockBuffer().get(new Pair(xId, yId, zId).hashCode());
            Iterator i = queue.iterator();
            while (i.hasNext()) {
                updated = true;
                BlockCoord bc = (BlockCoord) i.next();
                i.remove();
                setUpdateActive(true);
                setUpdatePacked(true);

                blocks[bc.x][bc.y][bc.z] = bc.Type;

            }
        }
        return updated;
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

    public boolean isUpdateActive() {
        return updateActive;
    }

    public boolean isUpdatePacked() {
        return updatePacked;
    }

    public void setUpdatePacked(boolean updatePacked) {
        this.updatePacked = updatePacked;
    }

    public void setUpdateActive(boolean updateActive) {
        this.updateActive = updateActive;
    }

    public void setBlock(int x, int y, int z, byte type) {
        blocks[x][y][z] = type;

        if (type == Type.WATER) {
            waterArray.add(new Water(x, y, z, x + xId * Chunk.CHUNK_SIZE, y + yId * Chunk.CHUNK_SIZE, z + zId * Chunk.CHUNK_SIZE, 10));
        }
    }

    public ArrayList<Water> getWaterArray() {
        return waterArray;
    }
}
