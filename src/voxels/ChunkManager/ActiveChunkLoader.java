/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels.ChunkManager;

import java.util.logging.Level;
import java.util.logging.Logger;
import voxels.Voxels;

/**
 *
 * @author otso
 */
public class ActiveChunkLoader extends Thread {


    private Chunk middle;
    private Chunk right;
    private Chunk left;
    private Chunk top;
    private Chunk bottom;
   

    boolean running;
    boolean refresh;

    ChunkManager chunkManager;
    int currentChunkX;
    int currentChunkY;
    int currentChunkZ;

    public ActiveChunkLoader(ChunkManager chunkManager) {
        this.chunkManager = chunkManager;
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            if (refresh || hasMoved()) {
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
        if (currentChunkX != Voxels.getCurrentChunkXId() || currentChunkY != Voxels.getCurrentChunkYId() || currentChunkZ != Voxels.getCurrentChunkZId()) {
            currentChunkX = Voxels.getCurrentChunkXId();
            currentChunkY = Voxels.getCurrentChunkYId();
            currentChunkZ = Voxels.getCurrentChunkZId();
            return true;
        }
        else
            return false;

    }

    public void loadChunks() {
        middle = chunkManager.getChunk(Voxels.getCurrentChunkXId(), Voxels.getCurrentChunkYId(), Voxels.getCurrentChunkZId());
        right = chunkManager.getChunk(Voxels.getCurrentChunkXId()+1, Voxels.getCurrentChunkYId(), Voxels.getCurrentChunkZId());
        left = chunkManager.getChunk(Voxels.getCurrentChunkXId()-1, Voxels.getCurrentChunkYId(), Voxels.getCurrentChunkZId());
        top = chunkManager.getChunk(Voxels.getCurrentChunkXId(), Voxels.getCurrentChunkYId()+1, Voxels.getCurrentChunkZId());
        bottom = chunkManager.getChunk(Voxels.getCurrentChunkXId()+1, Voxels.getCurrentChunkYId()-1, Voxels.getCurrentChunkZId());
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
