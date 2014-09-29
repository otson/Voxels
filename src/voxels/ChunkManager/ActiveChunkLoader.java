/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels.ChunkManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import voxels.Voxels;
import static voxels.Voxels.toXid;
import static voxels.Voxels.toYid;
import static voxels.Voxels.toZid;
import static voxels.Voxels.toX;
import static voxels.Voxels.toY;
import static voxels.Voxels.toZ;

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
        int loadDistance = Voxels.chunkRenderDistance;
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
        int distance = Voxels.chunkRenderDistance+1;
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
        HashMap<Integer,Coordinates> toUpdate = new HashMap<>();
        for (Chunk chunk : chunkMap.values()) {
            if (!chunk.getWaterArray().isEmpty()) {
                ArrayList<Water> array = chunk.getWaterArray();
                int size = array.size();
                for (int i = 0; i < size; i++) {
                    Water water = array.get(i);
                    {
                        boolean waterExists = processWater(water, chunk);
                        if(!waterExists){
                            array.remove(i);
                            size--;
                        }
                        toUpdate.put(new Pair(chunk.xId,chunk.yId,chunk.zId).hashCode(), new Coordinates(chunk.xId,chunk.zId,chunk.yId));
                    }
                }
                //chunkManager.updateChunk(chunk, x, y, z);
            }
        }
        for(Coordinates c : toUpdate.values()){
            chunkManager.updateChunk(chunkManager.getChunk(c.x, c.y, c.z), 0, 0, 0);
        }
        System.out.println("Simulated");
    }

    public boolean isRunning() {
        return running;
    }

    void refresh() {
        refresh = true;
    }

    private boolean processWater(Water water, Chunk chunk) {
        //if (water.x > 0 && water.x < Chunk.CHUNK_SIZE - 1 && water.y > 0 && water.z > 0 && water.z < Chunk.CHUNK_SIZE - 1) {
        // water block falls down, no spreading
        int x;
        int y;
        int z;
        Chunk tChunk;

        if (water.y == 0) {
            x = water.x;
            y = Chunk.CHUNK_SIZE - 1;
            z = water.z;
            tChunk = chunkManager.getActiveChunk(chunk.xId, chunk.yId-1, chunk.zId);
        } else {
            x = water.x;
            y = water.y-1;
            z = water.z;
            tChunk = chunk;
        }

        if (tChunk != null) {
            if (tChunk.blocks[x][y][z] == Type.AIR) {
                tChunk.setBlock(x, y, z, Type.WATER);
                chunk.setBlock(water.x, water.y, water.z, Type.AIR);
                //chunkManager.updateChunk(chunk, water.x, water.y, water.z);
                return false;

            }
//
//        else {
//
//            if (tareblocks[water.x][water.y - 1][water.z] != Type.WATER) {
//                if (chunk.blocks[water.x + 1][water.y][water.z] == Type.AIR) {
//                    chunk.setBlock(water.x + 1, water.y, water.z, Type.WATER);
//                }
//                if (chunk.blocks[water.x - 1][water.y][water.z] == Type.AIR) {
//                    chunk.setBlock(water.x - 1, water.y, water.z, Type.WATER);
//                }
//                if (chunk.blocks[water.x][water.y][water.z + 1] == Type.AIR) {
//                    chunk.setBlock(water.x, water.y, water.z + 1, Type.WATER);
//                }
//                if (chunk.blocks[water.x][water.y][water.z - 1] == Type.AIR) {
//                    chunk.setBlock(water.x, water.y, water.z - 1, Type.WATER);
//                }
//            }
//        }
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
        }
        return true;

    }

}
