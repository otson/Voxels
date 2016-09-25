/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels.ChunkManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import voxels.Voxels;
import static voxels.Voxels.toX;
import static voxels.Voxels.toXid;
import static voxels.Voxels.toY;
import static voxels.Voxels.toYid;
import static voxels.Voxels.toZ;
import static voxels.Voxels.toZid;

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
        long totalTime = 0;
        while (running) {
            long time = System.currentTimeMillis();
            
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
            totalTime +=System.currentTimeMillis()-time;
            if(totalTime > 1000){
                refresh = true;
                totalTime = 0;
            }
        }
    }

    public void put(Chunk chunk) {
        chunkMap.put(new Pair(chunk.xId, chunk.yId, chunk.zId).hashCode(), chunk);
    }

    private boolean hasMoved() {
        if (currentChunkX != Voxels.getCurrentChunkXId() || currentChunkY != Voxels.getCurrentChunkYId(0.2f) || currentChunkZ != Voxels.getCurrentChunkZId()) {
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

        int loadDistance = Voxels.chunkRenderDistance;
        for (int y = 0; y < Chunk.VERTICAL_CHUNKS; y++) {
            for (int x = -loadDistance; x <= loadDistance; x++) {
                for (int z = -loadDistance; z <= loadDistance; z++) {
                    Chunk chunk = chunkManager.getChunk(currentChunkX + x, y, currentChunkZ + z);
                    if (chunk != null) {
                        if (chunk.isUpdateActive() || !chunkMap.containsKey(new Pair(chunk.xId, chunk.yId, chunk.zId).hashCode())) {
                            chunk.setUpdateActive(false);
                            chunkMap.put(new Pair(chunk.xId, chunk.yId, chunk.zId).hashCode(), chunk);
                        }
                    }
                }
            }
        }
        for(Chunk chunk : chunkMap.values()){
            if(chunk.checkBuffer()){
                if(chunkManager.getHandle(chunk.xId, chunk.yId, chunk.zId) != null){
                    chunkManager.createVBO(chunk);   
                }
            }
        }
        clearEntries();
    }

    private void clearEntries() {
        int distance = Voxels.chunkRenderDistance + 1;
        for (Chunk chunk : chunkMap.values()) {
            if (chunk.xId > currentChunkX + distance || chunk.xId < currentChunkX - distance || chunk.zId > currentChunkZ + distance || chunk.zId < currentChunkZ - distance) {
                if (chunk.isUpdatePacked()) {
                    chunk.setUpdatePacked(false);
                    chunkMaker.setChunk(chunk);
                    chunkManager.getMap().put(new Pair(chunk.xId, chunk.yId, chunk.zId).hashCode(), chunkMaker.toByte(chunk));
                }
                chunkMap.remove(new Pair(chunk.xId, chunk.yId, chunk.zId).hashCode());
            }
        }
    }


    public boolean isRunning() {
        return running;
    }

    void refresh() {
        refresh = true;
    }

    public ConcurrentHashMap<Integer, Chunk> getChunkMap() {
        return chunkMap;
    }
    
}
