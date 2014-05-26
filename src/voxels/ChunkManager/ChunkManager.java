/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels.ChunkManager;

import com.ning.compress.lzf.LZFDecoder;
import com.ning.compress.lzf.LZFEncoder;
import com.ning.compress.lzf.LZFException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.FloatBuffer;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import voxels.Voxels;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static voxels.ChunkManager.Chunk.CHUNK_HEIGHT;
import static voxels.ChunkManager.Chunk.CHUNK_WIDTH;
import static voxels.Voxels.WaterOffs;
import static voxels.Voxels.getCurrentChunkX;
import static voxels.Voxels.getCurrentChunkZ;

/**
 *
 * @author otso
 */
public class ChunkManager {

    private boolean generate = false;

    private ConcurrentHashMap<Integer, byte[]> map;
    private ConcurrentHashMap<Integer, Handle> handles;
    private ChunkCreator chunkCreator;
    private ChunkLoader chunkLoader;
    private int maxThreads = 5;
    private boolean[] runningThreads = new boolean[maxThreads];
    private Data[] data = new Data[maxThreads];
    private Thread[] threads = new Thread[maxThreads];

    private boolean atMax = false;

    private boolean inLoop;
    private boolean initialLoad = true;

    public ChunkManager() {
        map = new ConcurrentHashMap<>();
        handles = new ConcurrentHashMap<>();
        chunkCreator = new ChunkCreator(map);
        chunkLoader = new ChunkLoader(this);
        chunkLoader.setPriority(Thread.MIN_PRIORITY);
    }

    public Block getBlock(int x, int y, int z) {

        return null;
    }

    public Chunk getChunk(int chunkX, int chunkZ) {
        if (map.containsKey(new Pair(chunkX, chunkZ).hashCode())) {
            return toChunk(map.get(new Pair(chunkX, chunkZ).hashCode()));
        }
        else {
            return null;
        }
    }

    public Handle getHandle(int x, int z) {
        if (handles.containsKey(new Pair(x, z).hashCode()))
            return handles.get(new Pair(x, z).hashCode());
        else
            return null;
    }

    public boolean isChunk(int chunkX, int chunkZ) {
        return map.containsKey(new Pair(chunkX, chunkZ).hashCode());
    }

    public void checkChunkUpdates() {
        // neither thread is currently running
        if (generate && threads[0] == null) {
            inLoop = true;
            System.out.println("here");
            // request new valid coordinates
            chunkCreator.setCurrentChunkX(getCurrentChunkX());
            chunkCreator.setCurrentChunkZ(getCurrentChunkZ());
            Coordinates coordinates;
            boolean running = true;

            coordinates = chunkCreator.getNewCoordinates();

            if (coordinates != null) {
                
                atMax = false;
                int x = coordinates.x;
                int z = coordinates.z;

                int newChunkX = coordinates.x;
                int newChunkZ = coordinates.z;

                // make a new chunk
                int threadId = 0;
                if (threads[0] == null) {
                    
                    threads[0] = new ChunkThread(threadId, data, newChunkX, newChunkZ, x * Chunk.CHUNK_WIDTH, z * Chunk.CHUNK_WIDTH, map);
                    threads[0].setPriority(Thread.MIN_PRIORITY);
                    threads[0].start();
                }

            }
            else {
                atMax = true;
                if (initialLoad && Voxels.USE_SEED)
                    System.out.println("Loaded all chunks. Seed: " + Voxels.SEED);
//                else
//                    System.out.println("Loaded all chunks");
                initialLoad = false;
            }

        }
        else if (inLoop) // has finished chunk and exited the loop
        {
            
            if(!threads[0].isAlive()){
                createBuffers(data[0]);
                threads[0] = null;
                inLoop = false;
            }

            if (initialLoad) {
                String string = "Chunks loaded: " + (int) ((float) map.size() / (float) ((Voxels.chunkCreationDistance * 2 + 1) * (Voxels.chunkCreationDistance * 2 + 1)) * 100) + " % (" + map.size() + "/" + ((Voxels.chunkCreationDistance * 2 + 1) * (Voxels.chunkCreationDistance * 2 + 1)) + ")";
                System.out.println(string);
                Display.setTitle(string);

            }
        }
    }

    public void stopGeneration() {
        generate = false;
    }

    public void startGeneration() {
        generate = true;
    }

    public void createBuffers(Data data) {

        int vboVertexHandle = glGenBuffers();
        

        glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandle);
        glBufferData(GL_ARRAY_BUFFER, data.vertexData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vboNormalHandle = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER, vboNormalHandle);
        glBufferData(GL_ARRAY_BUFFER, data.normalData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vboTexHandle = glGenBuffers();

        glBindBuffer(GL_ARRAY_BUFFER, vboTexHandle);
        glBufferData(GL_ARRAY_BUFFER, data.texData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        handles.put(new Pair(data.chunkX, data.chunkZ).hashCode(), new Handle(vboVertexHandle, vboNormalHandle, vboTexHandle, data.vertices));
    }

    public boolean isAtMax() {
        return atMax;
    }

    public Chunk toChunk(byte[] bytes) {
        try {
            return (Chunk) deserialize(LZFDecoder.decode(bytes));
        } catch (LZFException ex) {
            Logger.getLogger(ChunkManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static Object deserialize(byte[] data) {
        try {
            return new ObjectInputStream(new ByteArrayInputStream(data)).readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(MapThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public int chunkAmount() {
        return map.size();
    }

    public ChunkLoader getChunkLoader() {
        return chunkLoader;
    }

    public Chunk getTopLeft() {
        return chunkLoader.getTopLeft();
    }

    public Chunk getTopMiddle() {
        return chunkLoader.getTopMiddle();
    }

    public Chunk getTopRight() {
        return chunkLoader.getTopRight();
    }

    public Chunk getMidLeft() {
        return chunkLoader.getMidLeft();
    }

    public Chunk getMiddle() {
        return chunkLoader.getMiddle();
    }

    public Chunk getMidRight() {
        return chunkLoader.getMidRight();
    }

    public Chunk getLowLeft() {
        return chunkLoader.getLowLeft();
    }

    public Chunk getLowRight() {
        return chunkLoader.getLowRight();
    }

    public Chunk getLowMiddle() {
        return chunkLoader.getLowMiddle();
    }

}
