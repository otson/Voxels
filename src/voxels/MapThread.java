/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels;

import com.ning.compress.lzf.LZFEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;
import static voxels.ChunkManager.serialize;

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
            handles.put(new Pair(chunkX, chunkZ).hashCode(), new Handle(chunk.getVboVertexHandle(), chunk.getVboNormalHandle(), chunk.getVboTexHandle(), chunk.getVertices()));
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

    public byte[] toByte(Chunk chunk) {
        return LZFEncoder.encode(serialize(chunk));
    }

    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os;
        try {
            os = new ObjectOutputStream(out);
            os.writeObject(obj);
        } catch (IOException ex) {
            Logger.getLogger(MapThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        return out.toByteArray();
    }

    public static Object deserialize(byte[] data) {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is;
        try {
            return new ObjectInputStream(in).readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(MapThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
