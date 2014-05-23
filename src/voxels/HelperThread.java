/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author otso
 */
public class HelperThread extends Thread {

    private boolean running = false;

    ConcurrentHashMap<Integer, Chunk> map;
    Chunk chunk;
    Integer hash;

    HelperThread(ConcurrentHashMap<Integer, Chunk> map, Chunk chunk, Integer hash) {
        this.map = map;
        this.chunk = chunk;
        this.hash = hash;

    }

    @Override
    public void run() {
        running = true;
        map.put(hash, chunk);
        running = false;
    }

    public boolean isRunning() {
        return running;
    }
}
