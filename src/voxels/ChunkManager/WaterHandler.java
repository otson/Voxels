/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels.ChunkManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import org.lwjgl.util.vector.Vector3f;
import static voxels.Voxels.getChunkX;
import static voxels.Voxels.getChunkY;
import static voxels.Voxels.getChunkZ;

/**
 *
 * @author otso
 */
public class WaterHandler {

    private LinkedBlockingQueue<Water> waters;
    private ConcurrentHashMap<Integer, Coordinates> chunksToUpdate = new ConcurrentHashMap<>();
    private ChunkManager chunkManager;

    public WaterHandler(ChunkManager chunkManager) {
        this.chunkManager = chunkManager;
    }

    public void add(Water water) {
        waters.offer(water);
    }

    public void simulateWaters() {
        for (Water water : waters) {
            byte block = chunkManager.getActiveBlock(water.x, water.y - 1, water.z);
            if (block == Type.AIR) {
                chunkManager.setActiveBlockNoUpdate(new Vector3f(water.x, water.y, water.z), Type.AIR);
                int chunkX = getChunkX(water.x);
                int chunkY = getChunkY(water.y);
                int chunkZ = getChunkZ(water.z);
                chunksToUpdate.putIfAbsent(new Pair(chunkX, chunkY, chunkZ).hashCode(), new Coordinates(chunkX, chunkY, chunkZ));
                water.y--;
            }

        }
        for (Coordinates coord : chunksToUpdate.values()) {
            chunkManager.updateChunk(chunkManager.getActiveChunk(coord.x, coord.y, coord.z), 0, 0, 0);
        }
    }
}
