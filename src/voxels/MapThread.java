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
public class MapThread extends Thread {

    private boolean ready = false;

    ConcurrentHashMap<Integer, Chunk> map;
    Chunk chunk;
    private int chunkX;
    private int chunkZ;

    MapThread(ConcurrentHashMap<Integer, Chunk> map, Chunk chunk, int chunkX, int chunkZ) {
        this.map = map;
        this.chunk = chunk;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;

    }

    @Override
    public void run() {
        if (!map.containsKey(new Pair(chunkX, chunkZ).hashCode())) {
            map.put(new Pair(chunkX, chunkZ).hashCode(), chunk);
            setReady();
        }
        else
            System.out.println("wrong");
    }

    private void setReady() {
        ready = true;
    }

    public boolean isReady() {
        return ready;
    }
}
