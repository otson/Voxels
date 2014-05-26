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
public class ChunkLoader extends Thread {

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

    boolean movedLeft;
    boolean movedRight;
    boolean movedBack;
    boolean movedFront;
    boolean moreThanOne = true;

    ChunkManager chunkManager;
    int currentChunkX;
    int currentChunkZ;

    public ChunkLoader(ChunkManager chunkManager) {
        this.chunkManager = chunkManager;
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            if (hasMoved()) {
                System.out.println("here");
                loadChunks();
                try {
                    sleep(5);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ChunkLoader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private boolean hasMoved() {
        if (currentChunkX - 1 > Voxels.getCurrentChunkX() || currentChunkX + 1 < Voxels.getCurrentChunkX() || currentChunkZ - 1 > Voxels.getCurrentChunkZ() || currentChunkZ + 1 < Voxels.getCurrentChunkZ())
            moreThanOne = true;
        else {
            if (currentChunkX - 1 == Voxels.getCurrentChunkX())
                movedRight = true;

            if (currentChunkX + 1 == Voxels.getCurrentChunkX())
                movedLeft = true;

            if (currentChunkZ - 1 == Voxels.getCurrentChunkZ())
                movedFront = true;

            if (currentChunkZ + 1 == Voxels.getCurrentChunkZ())
                movedBack = true;
        }

        currentChunkX = Voxels.getCurrentChunkX();
        currentChunkZ = Voxels.getCurrentChunkZ();

        return moreThanOne || movedRight || movedLeft || movedFront || movedBack;

    }

    public void loadChunks() {
        if (movedLeft && movedBack) {
            middle = chunkManager.getChunk(Voxels.getCurrentChunkX(), Voxels.getCurrentChunkZ());
            topMiddle = chunkManager.getChunk(Voxels.getCurrentChunkX(), Voxels.getCurrentChunkZ() - 1);
            lowMiddle = chunkManager.getChunk(Voxels.getCurrentChunkX(), Voxels.getCurrentChunkZ() + 1);
            midLeft = chunkManager.getChunk(Voxels.getCurrentChunkX() - 1, Voxels.getCurrentChunkZ());
            midRight = chunkManager.getChunk(Voxels.getCurrentChunkX() + 1, Voxels.getCurrentChunkZ());
            topLeft = chunkManager.getChunk(Voxels.getCurrentChunkX() - 1, Voxels.getCurrentChunkZ() - 1);
            topRight = chunkManager.getChunk(Voxels.getCurrentChunkX() + 1, Voxels.getCurrentChunkZ() - 1);
            lowLeft = chunkManager.getChunk(Voxels.getCurrentChunkX() - 1, Voxels.getCurrentChunkZ() + 1);
            lowRight = chunkManager.getChunk(Voxels.getCurrentChunkX() + 1, Voxels.getCurrentChunkZ() + 1);
        }
        else if (movedLeft && movedFront) {
            middle = chunkManager.getChunk(Voxels.getCurrentChunkX(), Voxels.getCurrentChunkZ());
            topMiddle = chunkManager.getChunk(Voxels.getCurrentChunkX(), Voxels.getCurrentChunkZ() - 1);
            lowMiddle = chunkManager.getChunk(Voxels.getCurrentChunkX(), Voxels.getCurrentChunkZ() + 1);
            midLeft = chunkManager.getChunk(Voxels.getCurrentChunkX() - 1, Voxels.getCurrentChunkZ());
            midRight = chunkManager.getChunk(Voxels.getCurrentChunkX() + 1, Voxels.getCurrentChunkZ());
            topLeft = chunkManager.getChunk(Voxels.getCurrentChunkX() - 1, Voxels.getCurrentChunkZ() - 1);
            topRight = chunkManager.getChunk(Voxels.getCurrentChunkX() + 1, Voxels.getCurrentChunkZ() - 1);
            lowLeft = chunkManager.getChunk(Voxels.getCurrentChunkX() - 1, Voxels.getCurrentChunkZ() + 1);
            lowRight = chunkManager.getChunk(Voxels.getCurrentChunkX() + 1, Voxels.getCurrentChunkZ() + 1);
        }
        else if (movedRight && movedBack) {
            middle = chunkManager.getChunk(Voxels.getCurrentChunkX(), Voxels.getCurrentChunkZ());
            topMiddle = chunkManager.getChunk(Voxels.getCurrentChunkX(), Voxels.getCurrentChunkZ() - 1);
            lowMiddle = chunkManager.getChunk(Voxels.getCurrentChunkX(), Voxels.getCurrentChunkZ() + 1);
            midLeft = chunkManager.getChunk(Voxels.getCurrentChunkX() - 1, Voxels.getCurrentChunkZ());
            midRight = chunkManager.getChunk(Voxels.getCurrentChunkX() + 1, Voxels.getCurrentChunkZ());
            topLeft = chunkManager.getChunk(Voxels.getCurrentChunkX() - 1, Voxels.getCurrentChunkZ() - 1);
            topRight = chunkManager.getChunk(Voxels.getCurrentChunkX() + 1, Voxels.getCurrentChunkZ() - 1);
            lowLeft = chunkManager.getChunk(Voxels.getCurrentChunkX() - 1, Voxels.getCurrentChunkZ() + 1);
            lowRight = chunkManager.getChunk(Voxels.getCurrentChunkX() + 1, Voxels.getCurrentChunkZ() + 1);
        }
        else if (movedRight && movedFront) {
            middle = chunkManager.getChunk(Voxels.getCurrentChunkX(), Voxels.getCurrentChunkZ());
            topMiddle = chunkManager.getChunk(Voxels.getCurrentChunkX(), Voxels.getCurrentChunkZ() - 1);
            lowMiddle = chunkManager.getChunk(Voxels.getCurrentChunkX(), Voxels.getCurrentChunkZ() + 1);
            midLeft = chunkManager.getChunk(Voxels.getCurrentChunkX() - 1, Voxels.getCurrentChunkZ());
            midRight = chunkManager.getChunk(Voxels.getCurrentChunkX() + 1, Voxels.getCurrentChunkZ());
            topLeft = chunkManager.getChunk(Voxels.getCurrentChunkX() - 1, Voxels.getCurrentChunkZ() - 1);
            topRight = chunkManager.getChunk(Voxels.getCurrentChunkX() + 1, Voxels.getCurrentChunkZ() - 1);
            lowLeft = chunkManager.getChunk(Voxels.getCurrentChunkX() - 1, Voxels.getCurrentChunkZ() + 1);
            lowRight = chunkManager.getChunk(Voxels.getCurrentChunkX() + 1, Voxels.getCurrentChunkZ() + 1);
        }
        else if (movedLeft) {
            topLeft = topMiddle;
            midLeft = middle;
            lowLeft = lowMiddle;

            topMiddle = topRight;
            middle = midRight;
            lowMiddle = lowRight;

            topRight = chunkManager.getChunk(Voxels.getCurrentChunkX() + 1, Voxels.getCurrentChunkZ() - 1);
            midRight = chunkManager.getChunk(Voxels.getCurrentChunkX() + 1, Voxels.getCurrentChunkZ());
            lowRight = chunkManager.getChunk(Voxels.getCurrentChunkX() + 1, Voxels.getCurrentChunkZ() + 1);
            

        }
        else if (movedRight) {
            topRight = topMiddle;
            midRight = middle;
            lowRight = lowMiddle;
            topMiddle = topLeft;
            middle = midLeft;
            lowMiddle = lowLeft;
            
            topLeft = chunkManager.getChunk(Voxels.getCurrentChunkX() - 1, Voxels.getCurrentChunkZ() - 1);
            midLeft = chunkManager.getChunk(Voxels.getCurrentChunkX() - 1, Voxels.getCurrentChunkZ());
            lowLeft = chunkManager.getChunk(Voxels.getCurrentChunkX() - 1, Voxels.getCurrentChunkZ() + 1);
            
            
        }
        else if (movedFront) {
            lowLeft = midLeft;
            lowMiddle = middle;
            lowRight = midRight;
            midLeft = topLeft;
            middle = topMiddle;
            midRight = topRight;
            
            topLeft = chunkManager.getChunk(Voxels.getCurrentChunkX() - 1, Voxels.getCurrentChunkZ() - 1);
            topMiddle = chunkManager.getChunk(Voxels.getCurrentChunkX(), Voxels.getCurrentChunkZ() - 1);
            topRight = chunkManager.getChunk(Voxels.getCurrentChunkX() + 1, Voxels.getCurrentChunkZ() - 1);
            
        }
        else if (movedBack) {
            topLeft = midLeft;
            topMiddle = middle;
            topRight = midRight;
            midLeft = lowLeft;
            middle = lowMiddle;
            midRight = lowRight;
            
            lowLeft = chunkManager.getChunk(Voxels.getCurrentChunkX() - 1, Voxels.getCurrentChunkZ() + 1);
            lowMiddle = chunkManager.getChunk(Voxels.getCurrentChunkX(), Voxels.getCurrentChunkZ() + 1);
            lowRight = chunkManager.getChunk(Voxels.getCurrentChunkX() + 1, Voxels.getCurrentChunkZ() + 1);
            
        }
        else if (moreThanOne) {
            middle = chunkManager.getChunk(Voxels.getCurrentChunkX(), Voxels.getCurrentChunkZ());
            topMiddle = chunkManager.getChunk(Voxels.getCurrentChunkX(), Voxels.getCurrentChunkZ() - 1);
            lowMiddle = chunkManager.getChunk(Voxels.getCurrentChunkX(), Voxels.getCurrentChunkZ() + 1);
            midLeft = chunkManager.getChunk(Voxels.getCurrentChunkX() - 1, Voxels.getCurrentChunkZ());
            midRight = chunkManager.getChunk(Voxels.getCurrentChunkX() + 1, Voxels.getCurrentChunkZ());
            topLeft = chunkManager.getChunk(Voxels.getCurrentChunkX() - 1, Voxels.getCurrentChunkZ() - 1);
            topRight = chunkManager.getChunk(Voxels.getCurrentChunkX() + 1, Voxels.getCurrentChunkZ() - 1);
            lowLeft = chunkManager.getChunk(Voxels.getCurrentChunkX() - 1, Voxels.getCurrentChunkZ() + 1);
            lowRight = chunkManager.getChunk(Voxels.getCurrentChunkX() + 1, Voxels.getCurrentChunkZ() + 1);
        }

        movedBack = false;
        movedFront = false;
        movedRight = false;
        movedLeft = false;
        moreThanOne = false;
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
