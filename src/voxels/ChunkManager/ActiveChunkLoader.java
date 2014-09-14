/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels.ChunkManager;

import java.util.ArrayList;
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

    ArrayList<Chunk> chunkArray;

    ChunkManager chunkManager;
    int currentChunkX;
    int currentChunkY;
    int currentChunkZ;

    public ActiveChunkLoader(ChunkManager chunkManager) {
        this.chunkManager = chunkManager;
        chunkArray = new ArrayList<>(9000);
    }

    @Override
    public void run() {
        int count = 0;
        running = true;

        while (running) {

            if (refresh || hasMoved()) {
                System.out.println("In a new chunk " + count);
                count++;
                refresh = false;
                //loadChunks();
                try {
                    sleep(3);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ActiveChunkLoader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
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

    public void loadChunks() {
        Thread thread = new Thread(
                new Runnable() {
                    public void run() {
                        int loadDistance = 8;
                        long start = System.nanoTime();
                        for (int y = 0; y < Chunk.WORLD_HEIGHT; y++) {
                            for (int x = -loadDistance; x < loadDistance; x++) {
                                for (int z = -loadDistance; z < loadDistance; z++) {
                                    Chunk temp = chunkManager.getChunk(currentChunkX + x, y, currentChunkZ + z);
                                    if (temp != null) {
                                        chunkArray.add(temp);
                                    }
                                    
                                }
                            }
                        }
                        System.out.println("Loading chunks took: " + (System.nanoTime() - start) / 1000000 + " ms.");
                    }
                }
        );
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();

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
