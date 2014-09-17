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

/**
 *
 * @author otso
 */
public class ActiveChunkLoader extends Thread {

    private Chunk middle;
    private Chunk bottom;

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
                System.out.println("Moved to a new chunk.");
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

    private void updateLocation() {
        currentChunkX = Voxels.getCurrentChunkXId();
        currentChunkY = Voxels.getCurrentChunkYId();
        currentChunkZ = Voxels.getCurrentChunkZId();
    }

    public void loadChunks() {
        //int count = 0;
        int loadDistance = 1;
        //long start = System.nanoTime();
        for (int y = -loadDistance; y <= loadDistance; y++) {
            for (int x = -loadDistance; x <= loadDistance; x++) {
                for (int z = -loadDistance; z <= loadDistance; z++) {
                    Chunk chunk = chunkManager.getChunk(currentChunkX + x, currentChunkY + y, currentChunkZ + z);
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
        updateLocation();
        clearEntries();
        //System.out.println("count: " + count);
        //System.out.println("Loading chunks took: " + (System.nanoTime() - start) / 1000000 + " ms.");
        //System.out.println("Size: " + chunkMap.size());

    }

    private void clearEntries() {
        int distance = 2;
        int count = 0;
        for (Chunk chunk : chunkMap.values()) {
            if (chunk.xId > currentChunkX + distance || chunk.xId < currentChunkX - distance || chunk.zId > currentChunkZ + distance || chunk.zId < currentChunkZ - distance || chunk.yId > currentChunkY + distance || chunk.yId < currentChunkY - distance) {
                chunkMap.remove(new Pair(chunk.xId, chunk.yId, chunk.zId).hashCode());
                count++;
            }
        }
        if (count != 0) {
            System.out.println("Removed entries: " + count);
            System.out.println("Updated size: "+chunkMap.size());
        }
    }

    public Chunk getMiddle() {
        return middle;
    }

    public Chunk getBottom() {
        return bottom;
    }

    public boolean isRunning() {
        return running;
    }

    void refresh() {
        refresh = true;
    }

}
