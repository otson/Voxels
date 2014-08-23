/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels.ChunkManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author otso
 */
public class ChunkRenderChecker extends Thread {

    private ConcurrentHashMap<Integer, byte[]> map;
    private LinkedList<Pair> chunksToRender;
    private boolean running = false;
    private ChunkManager chunkManager;

    public ChunkRenderChecker(LinkedList<Pair> chunksToRender, ConcurrentHashMap<Integer, byte[]> map, ChunkManager chunkManager) {
        this.chunksToRender = chunksToRender;
        this.map = map;
        this.chunkManager = chunkManager;
    }

    @Override
    public void run() {
        running = true;
        Pair current;
        while (running) {
            long start = System.nanoTime();
            for (int i = 0; i < chunksToRender.size(); i++) {
                current = chunksToRender.get(i);
                if (map.containsKey(new Pair(current.x + 1, current.y, current.z).hashCode())
                        && map.containsKey(new Pair(current.x - 1, current.y, current.z).hashCode())
                        && map.containsKey(new Pair(current.x, current.y, current.z + 1).hashCode())
                        && map.containsKey(new Pair(current.x, current.y, current.z - 1).hashCode())) {

                    if (current.y != 1 && current.y != Chunk.WORLD_HEIGHT) {
                        if (map.containsKey(new Pair(current.x, current.y + 1, current.z).hashCode())
                                && map.containsKey(new Pair(current.x, current.y - 1, current.z).hashCode())) {
                            // create render
                            chunksToRender.remove(i);
                            i--;
                            chunkManager.createVBO(chunkManager.getChunk(current.x, current.y, current.z));
                        }
                    } else if (current.y == 1) {
                        if (map.containsKey(new Pair(current.x, current.y + 1, current.z).hashCode())) {
                            // create render
                            chunksToRender.remove(i);
                            i--;
                            chunkManager.createVBO(chunkManager.getChunk(current.x, current.y, current.z));
                        }
                    } else if (current.y == Chunk.WORLD_HEIGHT) {
                        if (map.containsKey(new Pair(current.x, current.y - 1, current.z).hashCode())) {
                            // create render
                            chunksToRender.remove(i);
                            i--;
                            chunkManager.createVBO(chunkManager.getChunk(current.x, current.y, current.z));
                        }
                    }
                }
            }
            System.out.println("chunksToRender size: " + chunksToRender.size() + "Time taken: " + (System.nanoTime() - start) / 1000000 + " ms.");

        }
    }

}
