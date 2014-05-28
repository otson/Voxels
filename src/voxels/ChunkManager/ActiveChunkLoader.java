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

    private Chunk topLeft;
    private Chunk topMiddle;
    private Chunk topRight;
    private Chunk midLeft;
    private Chunk middle;
    private Chunk midRight;
    private Chunk lowLeft;
    private Chunk lowRight;
    private Chunk lowMiddle;

    boolean running;

    ChunkManager chunkManager;
    int currentChunkX;
    int currentChunkZ;

    public ActiveChunkLoader(ChunkManager chunkManager) {
        this.chunkManager = chunkManager;
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            if (hasMoved()) {
                loadChunks();
                try {
                    sleep(1);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ActiveChunkLoader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private boolean hasMoved() {
        if (currentChunkX != Voxels.getCurrentChunkXId() || currentChunkZ != Voxels.getCurrentChunkZId()) {
            currentChunkX = Voxels.getCurrentChunkXId();
            currentChunkZ = Voxels.getCurrentChunkZId();
            return true;
        }
        else
            return false;

    }

    public void loadChunks() {

        middle = chunkManager.getChunk(Voxels.getCurrentChunkXId(), Voxels.getCurrentChunkZId());
        topMiddle = chunkManager.getChunk(Voxels.getCurrentChunkXId(), Voxels.getCurrentChunkZId() - 1);
        lowMiddle = chunkManager.getChunk(Voxels.getCurrentChunkXId(), Voxels.getCurrentChunkZId()+1);
        midLeft = chunkManager.getChunk(Voxels.getCurrentChunkXId()-1, Voxels.getCurrentChunkZId());
        midRight = chunkManager.getChunk(Voxels.getCurrentChunkXId()+1, Voxels.getCurrentChunkZId());
        topLeft = chunkManager.getChunk(Voxels.getCurrentChunkXId()-1, Voxels.getCurrentChunkZId()-1);
        topRight = chunkManager.getChunk(Voxels.getCurrentChunkXId()+1, Voxels.getCurrentChunkZId()-1);
        lowLeft = chunkManager.getChunk(Voxels.getCurrentChunkXId()-1, Voxels.getCurrentChunkZId()+1);
        lowRight = chunkManager.getChunk(Voxels.getCurrentChunkXId()+1, Voxels.getCurrentChunkZId()+1);
    }

    public Chunk getTopLeft() {
        return topLeft;
    }

    public Chunk getTopMiddle() {
        return topMiddle;
    }

    public Chunk getTopRight() {
        return topRight;
    }

    public Chunk getMidLeft() {
        return midLeft;
    }

    public Chunk getMiddle() {
        return middle;
    }

    public Chunk getMidRight() {
        return midRight;
    }

    public Chunk getLowLeft() {
        return lowLeft;
    }

    public Chunk getLowRight() {
        return lowRight;
    }

    public Chunk getLowMiddle() {
        return lowMiddle;
    }

    public boolean isRunning() {
        return running;
    }

}
