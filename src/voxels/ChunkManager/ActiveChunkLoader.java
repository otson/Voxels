/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels.ChunkManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import voxels.Voxels;
import static voxels.Voxels.convertToChunkXId;
import static voxels.Voxels.convertToChunkYId;
import static voxels.Voxels.convertToChunkZId;
import static voxels.Voxels.convertToXInChunk;
import static voxels.Voxels.convertToYInChunk;
import static voxels.Voxels.convertToZInChunk;

/**
 *
 * @author otso
 */
public class ActiveChunkLoader extends Thread {

    boolean running;
    boolean refresh;

    private ConcurrentHashMap<Integer, Chunk> chunkMap;

    ChunkManager chunkManager;
    int currentChunkX;
    int currentChunkY;
    int currentChunkZ;

    public ActiveChunkLoader(ChunkManager chunkManager, ConcurrentHashMap<Integer, Chunk> chunkMap) {
        this.chunkManager = chunkManager;
        this.chunkMap = chunkMap;
    }

    @Override
    public void run() {
        int count = 0;
        running = true;

        while (running) {

            if (refresh || hasMoved()) {
                //System.out.println("Moved to a new chunk.");
                count++;
                refresh = false;
                loadChunks();
                try {
                    sleep(5);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ActiveChunkLoader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void put(Chunk chunk) {
        chunkMap.put(new Pair(chunk.xId, chunk.yId, chunk.zId).hashCode(), chunk);
    }

    private boolean hasMoved() {
        if (currentChunkX != Voxels.getCurrentChunkXId() || currentChunkY != Voxels.getCurrentChunkYId(0.02f) || currentChunkZ != Voxels.getCurrentChunkZId()) {
            currentChunkX = Voxels.getCurrentChunkXId();
            currentChunkY = Voxels.getCurrentChunkYId();
            currentChunkZ = Voxels.getCurrentChunkZId();
            return true;
        } else {
            return false;
        }

    }

    public void updateLocation() {
        currentChunkX = Voxels.getCurrentChunkXId();
        currentChunkY = Voxels.getCurrentChunkYId();
        currentChunkZ = Voxels.getCurrentChunkZId();
    }

    public void loadChunks() {
        //int count = 0;
        int loadDistance = 5;//Voxels.chunkRenderDistance;
        //long start = System.nanoTime();
        for (int y = 0; y < Chunk.VERTICAL_CHUNKS; y++) {
            for (int x = -loadDistance; x <= loadDistance; x++) {
                for (int z = -loadDistance; z <= loadDistance; z++) {
                    Chunk chunk = chunkManager.getChunk(currentChunkX + x, y, currentChunkZ + z);
                    if (chunk != null) {
                        if (chunk.isModified() || !chunkMap.containsKey(new Pair(chunk.xId, chunk.yId, chunk.zId).hashCode())) {
                            chunk.setModified(false);
                            chunkMap.put(new Pair(chunk.xId, chunk.yId, chunk.zId).hashCode(), chunk);
                        }
                    } else {
                        //System.out.println("null chunk at x: " + (currentChunkX + x) + " y: " + y + " z: " + (currentChunkZ + z));
                    }

                }
            }
        }
        //updateLocation();
        clearEntries();
        //System.out.println("count: " + count);
        //System.out.println("Loading chunks took: " + (System.nanoTime() - start) / 1000000 + " ms.");
        //System.out.println("Size: " + chunkMap.size());

    }

    private void clearEntries() {
        int distance = 6;//Voxels.chunkRenderDistance+1;
        int count = 0;
        for (Chunk chunk : chunkMap.values()) {
            if (chunk.xId > currentChunkX + distance || chunk.xId < currentChunkX - distance || chunk.zId > currentChunkZ + distance || chunk.zId < currentChunkZ - distance) {
                chunkMap.remove(new Pair(chunk.xId, chunk.yId, chunk.zId).hashCode());
                count++;
            }
        }
        if (count != 0) {
            //System.out.println("Removed entries: " + count);
            //System.out.println("Updated size: "+chunkMap.size());
        }
    }

    public void simulateWater() {
        int x = 1;
        int y = 1;
        int z = 1;
        for (Chunk chunk : chunkMap.values()) {
            if (!chunk.getWaterArray().isEmpty()) {
                ArrayList<Water> array = chunk.getWaterArray();
                int size = array.size();
                for (int i = 0; i < size; i++) {
                    Water water = array.get(i);
                    {
                        Water newWater = processWater(water, chunk);
                        if (newWater == null) {
                            array.remove(i);
                            chunk.setBlock(water.x, water.y, water.z, Type.AIR);
                            i++;
                        }
                    }
                }
                chunkManager.updateChunk(chunk, x, y, z);
            }
        }
        System.out.println("Simulated");
    }

    public boolean isRunning() {
        return running;
    }

    void refresh() {
        refresh = true;
    }

    private Water processWater(Water water, Chunk chunk) {
        //if (water.x > 0 && water.x < Chunk.CHUNK_SIZE - 1 && water.y > 0 && water.z > 0 && water.z < Chunk.CHUNK_SIZE - 1) {
            // water block falls down, no spreading
            Chunk targetChunk = chunkManager.getActiveChunk(convertToChunkXId(water.x), convertToChunkYId(water.y-1), convertToChunkZId(water.z));
            int x = Voxels.
            if (chunk.blocks[water.x][water.y - 1][water.z] == Type.AIR) {
                chunk.setBlock(water.x, water.y - 1, water.z, Type.AIR);
                chunk.setBlock(water.x, water.y - 1, water.z, Type.WATER);
                return null;
            } else {

                if (chunk.blocks[water.x][water.y - 1][water.z] != Type.WATER) {
                    if (chunk.blocks[water.x + 1][water.y][water.z] == Type.AIR) {
                        chunk.setBlock(water.x + 1, water.y, water.z, Type.WATER);
                    }
                    if (chunk.blocks[water.x - 1][water.y][water.z] == Type.AIR) {
                        chunk.setBlock(water.x - 1, water.y, water.z, Type.WATER);
                    }
                    if (chunk.blocks[water.x][water.y][water.z + 1] == Type.AIR) {
                        chunk.setBlock(water.x, water.y, water.z + 1, Type.WATER);
                    }
                    if (chunk.blocks[water.x][water.y][water.z - 1] == Type.AIR) {
                        chunk.setBlock(water.x, water.y, water.z - 1, Type.WATER);
                    }
                }
            }
        //} 
//        else if (water.y == 0) {
//            int y;
//            Chunk chunkBelow;
//
//            y = Chunk.CHUNK_SIZE - 1;
//            chunkBelow = chunkManager.getActiveChunk(chunk.xId,chunk.yId-1, chunk.zId);
//            if(chunkBelow == null)
//                System.out.println("null");
//            if (chunkBelow.blocks[water.x][y][water.z] == Type.AIR) {
//                chunkBelow.setBlock(water.x, y, water.z, Type.AIR);
//                chunkBelow.setBlock(water.x, y, water.z, Type.WATER);
//                return null;
//            } 
//            else {
//
//                if (chunk.blocks[water.x][water.y - 1][water.z] != Type.WATER) {
//                    if (chunk.blocks[water.x + 1][water.y][water.z] == Type.AIR) {
//                        chunk.setBlock(water.x + 1, water.y, water.z, Type.WATER);
//                    }
//                    if (chunk.blocks[water.x - 1][water.y][water.z] == Type.AIR) {
//                        chunk.setBlock(water.x - 1, water.y, water.z, Type.WATER);
//                    }
//                    if (chunk.blocks[water.x][water.y][water.z + 1] == Type.AIR) {
//                        chunk.setBlock(water.x, water.y, water.z + 1, Type.WATER);
//                    }
//                    if (chunk.blocks[water.x][water.y][water.z - 1] == Type.AIR) {
//                        chunk.setBlock(water.x, water.y, water.z - 1, Type.WATER);
//                    }
//                }
//            }
        //}
        return water;

    }

}
