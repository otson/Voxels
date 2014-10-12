/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels.ChunkManager;

import java.nio.FloatBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import org.lwjgl.util.vector.Vector3f;
import static voxels.Voxels.getChunkX;
import static voxels.Voxels.getChunkX;
import static voxels.Voxels.getChunkY;
import static voxels.Voxels.getChunkY;
import static voxels.Voxels.getChunkZ;
import static voxels.Voxels.getChunkZ;

/**
 *
 * @author otso
 */
public class WaterHandler {

    private LinkedBlockingQueue<Water> waters;
    private ConcurrentHashMap<Integer, Coordinates> chunksToUpdate = new ConcurrentHashMap<>();
    private ChunkManager chunkManager;

    public int vertexHandle;
    public int normalHandle;
    public int vertices = 0;

    public WaterHandler(ChunkManager chunkManager) {
        waters = new LinkedBlockingQueue<>();
        this.chunkManager = chunkManager;
        this.chunkManager.setWaterHandler(this);
    }

    public void add(Water water) {
        waters.offer(water);
    }

    public void simulateWaters() {
        for (Water water : waters) {
            byte block = chunkManager.getActiveBlock(water.x, water.y - 1, water.z);
            if (block == Type.AIR) {
                chunkManager.setActiveBlock(new Vector3f(water.x, water.y, water.z), Type.AIR);
                water.y--;
            }

        }
        createVBO();
    }

    private void createVBO() {
        vertices = waters.size() * 24;
        System.out.println("Size: " + waters.size());
        final int vertexSize = 3;
        FloatBuffer vertexData = BufferUtils.createFloatBuffer(vertices * vertexSize);
        FloatBuffer normalData = BufferUtils.createFloatBuffer(vertices * vertexSize);
        for (Water water : waters) {
            int x = water.x;
            int y = water.y;
            int z = water.z;

            vertexData.put(new float[]{0 + x, 1 + y, 0 + z, 0 + x, 1 + y, 1 + z, 1 + x, 1 + y, 1 + z, 1 + x, 1 + y, 0 + z, // top
                0 + x, 0 + y, 0 + z, 1 + x, 0 + y, 0 + z, 1 + x, 0 + y, 1 + z, 0 + x, 0 + y, 1 + z, // bottom
                0 + x, 1 + y, 0 + z, 0 + x, 0 + y, 0 + z, 0 + x, 0 + y, 1 + z, 0 + x, 1 + y, 1 + z, // left
                1 + x, 1 + y, 0 + z, 1 + x, 1 + y, 1 + z, 1 + x, 0 + y, 1 + z, 1 + x, 0 + y, 0 + z, // right
                0 + x, 1 + y, 1 + z, 0 + x, 0 + y, 1 + z, 1 + x, 0 + y, 1 + z, 1 + x, 1 + y, 1 + z, // front
                0 + x, 1 + y, 0 + z, 1 + x, 1 + y, 0 + z, 1 + x, 0 + y, 0 + z, 0 + x, 0 + y, 0 + z // back
        });

            normalData.put(new float[]{0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, // top
                0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, // bottom
                -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, // left
                1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, // right
                0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, // front
                0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1 // back
        });

        }
        vertexData.flip();
        normalData.flip();
        
        vertexHandle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexHandle);
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STREAM_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        normalHandle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, normalHandle);
        glBufferData(GL_ARRAY_BUFFER, normalData, GL_STREAM_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

}
