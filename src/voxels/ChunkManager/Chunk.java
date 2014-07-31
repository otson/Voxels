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
    public static final int WORLD_HEIGHT = 5;
    public static final int WATER_HEIGHT = -1;
    

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
        int dirtCount = 0;
        blocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
        for (int x = 0; x < blocks.length; x++) {
            blocks[x] = new Block[CHUNK_SIZE][CHUNK_SIZE];
            for (int y = 0; y < blocks[x].length; y++) {
                blocks[x][y] = new Block[CHUNK_SIZE];
                for (int z = 0; z < blocks[x][y].length; z++) {
                    if (Voxels.USE_3D_NOISE) {
                        float noise1 = Voxels.get3DNoise(x + xCoordinate, y + yCoordinate, z + zCoordinate) / 255f;
                        //float noise2 = Voxels.get3DNoise(x + xCoordinate + 10000, y + yCoordinate + 10000, z + zCoordinate + 10000) / 255f;

                        if (noise1 > 0.80f)// && noise1 < 0.55f && noise2 > 0.45f && noise2 < 0.55f)
                            if(y+yCoordinate<Chunk.CHUNK_SIZE*Chunk.WORLD_HEIGHT*0.85)
                                blocks[x][y][z] = new Block(Type.DIRT);
                            else
                                blocks[x][y][z] = new Block(Type.AIR);
                        else
                            blocks[x][y][z] = new Block(Type.AIR);
                    }
                    else {
                        if (y + Chunk.CHUNK_SIZE * yId <= maxHeights[x][z]) {
                            blocks[x][y][z] = new Block(Type.DIRT);
                            dirtCount++;
                        }
                        else
                            if(y <= WATER_HEIGHT)
                                blocks[x][y][z] = new Block(Type.WATER);
                        else
                            blocks[x][y][z] = new Block(Type.AIR);
                    }
                }
            }
        }
        //System.out.println("Dirt blocks in chunk x: "+xId+" y: "+yId+" z: "+zId+": "+dirtCount);
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
