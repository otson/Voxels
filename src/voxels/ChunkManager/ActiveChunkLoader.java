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
    private ChunkMaker chunkMaker;

    ChunkManager chunkManager;
    int currentChunkX;
    int currentChunkY;
    int currentChunkZ;

    public ActiveChunkLoader(ChunkManager chunkManager, ConcurrentHashMap<Integer, Chunk> chunkMap) {
        this.chunkManager = chunkManager;
        this.chunkMap = chunkMap;
        chunkMaker = new ChunkMaker(null, chunkManager.getMap(), chunkManager, chunkManager.getDataToProcess(), chunkManager.getQueue());
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
                        if (chunk.isUpdateActive() || !chunkMap.containsKey(new Pair(chunk.xId, chunk.yId, chunk.zId).hashCode())) {
                            chunk.setUpdateActive(false);
                            chunkMap.put(new Pair(chunk.xId, chunk.yId, chunk.zId).hashCode(), chunk);
                        }
                    } else {
                        //System.out.println("null chunk at x: " + (currentChunkX + x) + " y: " + y + " z: " + (currentChunkZ + z));
                    }

                }
            }
        }
        for(Chunk chunk : chunkMap.values()){
            if(chunk.checkBuffer()){
                //System.out.println("chunk x: "+chunk.xId+" y: "+chunk.yId+" z: "+chunk.zId);
                if(chunkManager.getHandle(chunk.xId, chunk.yId, chunk.zId) != null)
                    chunkManager.createVBO(chunk);
            }
        }

        //updateLocation();
        clearEntries();
        //System.out.println("count: " + count);
        //System.out.println("Loading chunks took: " + (System.nanoTime() - start) / 1000000 + " ms.");
        //System.out.println("Size: " + chunkMap.size());

    }

    private void clearEntries() {
        int distance = Voxels.chunkRenderDistance + 1;
        int count = 0;
        for (Chunk chunk : chunkMap.values()) {
            if (chunk.xId > currentChunkX + distance || chunk.xId < currentChunkX - distance || chunk.zId > currentChunkZ + distance || chunk.zId < currentChunkZ - distance) {
                if (chunk.isUpdatePacked()) {
                    chunk.setUpdatePacked(false);
                    chunkMaker.setChunk(chunk);
                    chunkManager.getMap().put(new Pair(chunk.xId, chunk.yId, chunk.zId).hashCode(), chunkMaker.toByte(chunk));
                }
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
        HashMap<Integer, Coordinates> toUpdate = new HashMap<>();
        for (Chunk chunk : chunkMap.values()) {
            if (!chunk.getWaterArray().isEmpty()) {
                ArrayList<Water> array = chunk.getWaterArray();
                int size = array.size();
                for (int i = 0; i < size; i++) {
                    Water water = array.get(i);
                    {
                        boolean isActive = processWater(water);
                        if (isActive) {
                            chunkManager.setActiveBlock(water.xw, water.yw, water.zw, Type.AIR);
                            array.remove(i);
                            size--;
                        }
                        toUpdate.put(new Pair(chunk.xId, chunk.yId, chunk.zId).hashCode(), new Coordinates(chunk.xId, chunk.yId, chunk.zId));
                    }
                }
            }
        }
        
        
        //System.out.println("Simulated");
    }

    public boolean isRunning() {
        return running;
    }

    void refresh() {
        refresh = true;
    }

    private boolean processWater(Water water) {
        // remove from active if block below is also water, no spreading
        if (chunkManager.getActiveBlock(water.xw, water.yw - 1, water.zw) == Type.WATER) {
            return true;
        }

        // return true if water spread, else false
        if (chunkManager.getActiveBlock(water.xw, water.yw - 1, water.zw) == Type.AIR) {
            chunkManager.setActiveBlock(water.xw, water.yw - 1, water.zw, Type.WATER);
            return true;
        } else {
            int dir = (int) (4 * Math.random());
            switch (dir) {
                case 1:
                    if (chunkManager.getActiveBlock(water.xw + 1, water.yw, water.zw) == Type.AIR) {
                        chunkManager.setActiveBlock(water.xw + 1, water.yw, water.zw, Type.WATER);
                        return true;
                    }
                case 2:
                    if (chunkManager.getActiveBlock(water.xw - 1, water.yw, water.zw) == Type.AIR) {
                        chunkManager.setActiveBlock(water.xw - 1, water.yw, water.zw, Type.WATER);
                        return true;
                    }
                case 3:
                    if (chunkManager.getActiveBlock(water.xw, water.yw, water.zw + 1) == Type.AIR) {
                        chunkManager.setActiveBlock(water.xw, water.yw, water.zw + 1, Type.WATER);
                        return true;
                    }
                case 4:
                    if (chunkManager.getActiveBlock(water.xw, water.yw, water.zw - 1) == Type.AIR) {
                        chunkManager.setActiveBlock(water.xw, water.yw, water.zw - 1, Type.WATER);
                        return true;
                    }
            }
            return false;
        }
    }

    public ConcurrentHashMap<Integer, Chunk> getChunkMap() {
        return chunkMap;
    }

}
