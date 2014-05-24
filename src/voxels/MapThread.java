/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author otso
 */
public class MapThread extends Thread {

    private boolean ready = false;

    private ConcurrentHashMap<Integer, byte[]> map;
    private ConcurrentHashMap<Integer, Handle> handles;
    private Chunk chunk;
    private int chunkX;
    private int chunkZ;

    private ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private GZIPOutputStream gzipOut;
    private  ObjectOutputStream objectOut;

    MapThread(ConcurrentHashMap<Integer, byte[]> map, ConcurrentHashMap<Integer, Handle> handles, Chunk chunk, int chunkX, int chunkZ) {
        this.map = map;
        this.handles = handles;
        this.chunk = chunk;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;

    }

    @Override
    public void run() {
        if (!map.containsKey(new Pair(chunkX, chunkZ).hashCode())) {
            handles.put(new Pair(chunkX, chunkZ).hashCode(), new Handle(chunk.getVboVertexHandle(),chunk.getVboNormalHandle(),chunk.getVboTexHandle(), chunk.getVertices()));
            map.put(new Pair(chunkX, chunkZ).hashCode(), toByte(chunk));
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

    public byte[] toByte(Object object) {
        try {
            gzipOut = new GZIPOutputStream(baos);
            objectOut = new ObjectOutputStream(gzipOut);
            objectOut.writeObject(object);
            objectOut.close();
        } catch (IOException ex) {
            Logger.getLogger(ChunkManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        byte[] bytes = baos.toByteArray();
        return bytes;
    }
}
