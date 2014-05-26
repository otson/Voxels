/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
    public final int chunkZ;
    

    public Data(int chunkX, int chunkZ, int vertices, FloatBuffer vertexData, FloatBuffer normalData, FloatBuffer texData) {
        this.vertexData = vertexData;
        this.normalData = normalData;
        this.texData = texData;
        this.vertices = vertices;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }
}
