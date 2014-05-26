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

    private static int vertexSize = 3;
    private static int normalSize = 3;
    private static int texSize = 2;
    private static int colorSize = 3;

    private boolean[][][] top = new boolean[Chunk.CHUNK_WIDTH][][];
    private boolean[][][] bottom = new boolean[Chunk.CHUNK_WIDTH][][];
    private boolean[][][] left = new boolean[Chunk.CHUNK_WIDTH][][];
    private boolean[][][] right = new boolean[Chunk.CHUNK_WIDTH][][];
    private boolean[][][] front = new boolean[Chunk.CHUNK_WIDTH][][];
    private boolean[][][] back = new boolean[Chunk.CHUNK_WIDTH][][];

    private boolean generate = false;

    private ChunkThread chunkThread = new ChunkThread(0, 0, 0, 0);
    private MapThread mapThread = new MapThread(null, null, null, 0, 0);

    private ConcurrentHashMap<Integer, byte[]> map;
    private ConcurrentHashMap<Integer, Handle> handles;
    private ChunkCreator chunkCreator;
    private ChunkLoader chunkLoader;

    private boolean atMax = false;

    private boolean inLoop;
    private boolean initialLoad = true;

    public ChunkManager() {
        map = new ConcurrentHashMap<>();
        handles = new ConcurrentHashMap<>();
        chunkCreator = new ChunkCreator(map);
        chunkLoader = new ChunkLoader(this);
        chunkLoader.setPriority(Thread.MIN_PRIORITY);
        initBooleanArrays();

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
        if (generate && !chunkThread.isAlive() && !mapThread.isAlive() && chunkThread.isReady() == false) {
            inLoop = true;
            // request new valid coordinates
            chunkCreator.setCurrentChunkX(getCurrentChunkX());
            chunkCreator.setCurrentChunkZ(getCurrentChunkZ());
            Coordinates coordinates = null;
            boolean running = true;

            coordinates = chunkCreator.getNewCoordinates();

            if (coordinates != null) {

                atMax = false;
                int x = coordinates.x;
                int z = coordinates.z;

                int newChunkX = coordinates.x;
                int newChunkZ = coordinates.z;

                // make a new chunk
                chunkThread = new ChunkThread(newChunkX, newChunkZ, x * Chunk.CHUNK_WIDTH, z * Chunk.CHUNK_WIDTH);
                chunkThread.setPriority(Thread.MIN_PRIORITY);
                chunkThread.start();
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
        else if (inLoop && chunkThread.isReady() && !chunkThread.isAlive()) // has finished chunk and exited the loop
        {

            // Create the buffers in main thread
            createBuffers(chunkThread.getChunk(), chunkThread.getVertexData(), chunkThread.getNormalData(), chunkThread.getTexData());

            // put the Chunk to HashMap in a new thread
            mapThread = new MapThread(map, handles, chunkThread.getChunk(), chunkThread.getChunkX(), chunkThread.getChunkZ());
            mapThread.setPriority(Thread.MIN_PRIORITY);
            mapThread.start();
            chunkThread = new ChunkThread(0, 0, 0, 0);
            inLoop = false;

            if (initialLoad) {
                String string = "Chunks loaded: " + (int) ((float) map.size() / (float) ((Voxels.chunkCreationDistance * 2 + 1) * (Voxels.chunkCreationDistance * 2 + 1)) * 100) + " % (" + map.size() + "/" + ((Voxels.chunkCreationDistance * 2 + 1) * (Voxels.chunkCreationDistance * 2 + 1)) + ")";
                System.out.println(string);
                Display.setTitle(string);

            }
        }
    }

    private void initBooleanArrays() {
        for (int x = 0; x < top.length; x++) {
            top[x] = new boolean[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < top[x].length; y++) {
                top[x][y] = new boolean[CHUNK_WIDTH];
            }
        }
        for (int x = 0; x < bottom.length; x++) {
            bottom[x] = new boolean[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < bottom[x].length; y++) {
                bottom[x][y] = new boolean[CHUNK_WIDTH];
            }
        }
        for (int x = 0; x < right.length; x++) {
            right[x] = new boolean[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < right[x].length; y++) {
                right[x][y] = new boolean[CHUNK_WIDTH];
            }
        }
        for (int x = 0; x < left.length; x++) {
            left[x] = new boolean[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < left[x].length; y++) {
                left[x][y] = new boolean[CHUNK_WIDTH];
            }
        }
        for (int x = 0; x < front.length; x++) {
            front[x] = new boolean[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < front[x].length; y++) {
                front[x][y] = new boolean[CHUNK_WIDTH];
            }
        }
        for (int x = 0; x < back.length; x++) {
            back[x] = new boolean[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < back[x].length; y++) {
                back[x][y] = new boolean[CHUNK_WIDTH];
            }
        }
    }

    public void stopGeneration() {
        generate = false;
    }

    public void startGeneration() {
        generate = true;
    }

    public void createBuffers(Chunk chunk, FloatBuffer vertexData, FloatBuffer normalData, FloatBuffer texData) {

        int vboVertexHandle = glGenBuffers();
        chunk.setVboVertexHandle(vboVertexHandle);

        glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandle);
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vboNormalHandle = glGenBuffers();
        chunk.setVboNormalHandle(vboNormalHandle);

        glBindBuffer(GL_ARRAY_BUFFER, vboNormalHandle);
        glBufferData(GL_ARRAY_BUFFER, normalData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vboTexHandle = glGenBuffers();
        chunk.setVboTexHandle(vboTexHandle);

        glBindBuffer(GL_ARRAY_BUFFER, vboTexHandle);
        glBufferData(GL_ARRAY_BUFFER, texData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vboColorHandle = glGenBuffers();
        chunk.setVboColorHandle(vboColorHandle);

//        glBindBuffer(GL_ARRAY_BUFFER, vboColorHandle);
//        glBufferData(GL_ARRAY_BUFFER, colorData, GL_STATIC_DRAW);
//        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public boolean isAtMax() {
        return atMax;
    }

    public byte[] toByte(Chunk chunk) {
        return LZFEncoder.encode(serialize(chunk));
    }

    public Chunk toChunk(byte[] bytes) {
        try {
            return (Chunk) deserialize(LZFDecoder.decode(bytes));
        } catch (LZFException ex) {
            Logger.getLogger(ChunkManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
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
