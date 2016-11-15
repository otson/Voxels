/* 
 * Copyright (C) 2016 Otso Nuortimo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package voxels.ChunkManager;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author otso
 */
public class ChunkRenderChecker extends Thread {

    private ConcurrentHashMap<Integer, byte[]> map;
    private boolean running = false;
    private ChunkManager chunkManager;
    private BlockingQueue<Triple> queue;

    public ChunkRenderChecker(BlockingQueue<Triple> queue, ConcurrentHashMap<Integer, byte[]> map, ChunkManager chunkManager) {
        this.queue = queue;
        this.map = map;
        this.chunkManager = chunkManager;
    }

    /**
     *
     * Maybe add different BlockingQueue for each layer, and prioritize the
     * closest layers to player's Y coordinate?
     *
     */
    @Override
    public void run() {
        running = true;
        int count = 0;
        Triple current = null;
        while (running) {
            int size = queue.size();
            long start = System.nanoTime();
            for (int i = 0; i < size; i++) {
                try {
                    current = queue.take();
                } catch (InterruptedException ex) {
                    Logger.getLogger(ChunkRenderChecker.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (current != null) {
                    
                    if (map.containsKey(new Triple(current.x + 1, current.y, current.z).hashCode())
                            && map.containsKey(new Triple(current.x - 1, current.y, current.z).hashCode())
                            && map.containsKey(new Triple(current.x, current.y, current.z + 1).hashCode())
                            && map.containsKey(new Triple(current.x, current.y, current.z - 1).hashCode())) {

                        if (current.y != 1 && current.y != Chunk.WORLD_HEIGHT) {
                            if (map.containsKey(new Triple(current.x, current.y + 1, current.z).hashCode())
                                    && map.containsKey(new Triple(current.x, current.y - 1, current.z).hashCode())) {
                                i--;
                                chunkManager.createVBO(chunkManager.getChunk(current.x, current.y, current.z));

                            }
                        } else if (current.y == 1) {
                            if (map.containsKey(new Triple(current.x, current.y + 1, current.z).hashCode())) {
                                i--;

                                chunkManager.createVBO(chunkManager.getChunk(current.x, current.y, current.z));

                            }
                        } else if (current.y == Chunk.WORLD_HEIGHT) {
                            if (map.containsKey(new Triple(current.x, current.y - 1, current.z).hashCode())) {
                                i--;

                                chunkManager.createVBO(chunkManager.getChunk(current.x, current.y, current.z));

                            }
                        }
                    } else {
                        try {
                            // put it back
                            queue.offer(current, 5, TimeUnit.SECONDS);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ChunkRenderChecker.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
            //System.out.println("Queue size: " + size + " Time taken: " + (System.nanoTime() - start) / 1000000 + " ms.");
            try {
                sleep(50);
            } catch (InterruptedException ex) {
                Logger.getLogger(ChunkRenderChecker.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

}
