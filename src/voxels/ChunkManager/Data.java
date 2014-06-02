package voxels.ChunkManager;

import java.nio.FloatBuffer;

/**
 *
 * @author otso
 */
public class Data {

    public final FloatBuffer vertexData;
    public final FloatBuffer normalData;
    public final FloatBuffer texData;
    public final int vertices;
    public final int chunkX;
    public final int chunkY;
    public final int chunkZ;
    public final int vertexHandle;
    public final int normalHandle;
    public final int texHandle;
    public final boolean UPDATE;

    /**
     *
     * @param chunkX
     * @param chunkY
     * @param chunkZ
     * @param vertices
     * @param vertexData
     * @param normalData
     * @param texData
     * @param update
     */
    public Data(int chunkX, int chunkY, int chunkZ, int vertices, FloatBuffer vertexData, FloatBuffer normalData, FloatBuffer texData, boolean update) {
        this.vertexData = vertexData;
        this.normalData = normalData;
        this.texData = texData;
        this.vertices = vertices;
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;
        this.UPDATE = update;

        this.vertexHandle = -1;
        this.normalHandle = -1;
        this.texHandle = -1;
    }

    public Data(int chunkX, int chunkY, int chunkZ, int vertices, FloatBuffer vertexData, FloatBuffer normalData, FloatBuffer texData, int vertexHandle, int normalHandle, int texHandle, boolean update) {
        this.vertexData = vertexData;
        this.normalData = normalData;
        this.texData = texData;
        this.vertices = vertices;
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;
        this.vertexHandle = vertexHandle;
        this.normalHandle = normalHandle;
        this.texHandle = texHandle;
        this.UPDATE = update;
    }
}
