/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels.ChunkManager;

import com.ning.compress.lzf.LZFDecoder;
import com.ning.compress.lzf.LZFException;
import de.ruedigermoeller.serialization.FSTObjectInput;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.MappedByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL15;
import voxels.Voxels;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glMapBuffer;
import static voxels.Voxels.getCurrentChunkXId;
import static voxels.Voxels.getCurrentChunkZId;

/**
 *
 * @author otso
 */
public class ChunkManager {

    /**
     * Set the maximum amount of threads use to create chunks. Default number is
     * equal to the number of cores in the system CPU.
     */
    public static final int maxThreads = Runtime.getRuntime().availableProcessors();

    private ConcurrentHashMap<Integer, byte[]> map;
    private ConcurrentHashMap<Integer, Handle> handles;
    private ChunkCoordinateCreator chunkCreator;
    private ActiveChunkLoader chunkLoader;
    private Data[] data = new Data[maxThreads];
    private ChunkMaker[] threads = new ChunkMaker[maxThreads];
    private ChunkMaker updateThread;

    private Chunk[] updateChunks = new Chunk[1];

    private boolean update = false;

    private boolean atMax = false;
    private boolean inLoop;
    private boolean initialLoad = true;
    private boolean generate = false;
    private int lastMessage = -1;

    public ChunkManager() {
        map = new ConcurrentHashMap<>();
        handles = new ConcurrentHashMap<>();
        chunkCreator = new ChunkCoordinateCreator(map);
        chunkLoader = new ActiveChunkLoader(this);
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

    public void editBlock(short type, int x, int y, int z, int chunkX, int chunkZ) {
        long start = System.nanoTime();

        Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk == null){
            System.out.println("Tried to modify a null chunk.");
            return;
        }

        chunk.blocks[x][y][z].type = type;

        updateThread = new ChunkMaker(map, chunk);
        updateThread.update();
        createBuffers(updateThread.getUpdateData());

        System.out.println("Update finished: " + (System.nanoTime() - start) / 1000000 + " ms.");

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

        if (update) {
            inLoop = true;
            updateThread = new ChunkMaker(map, updateChunks[0]);
            updateThread.setPriority(Thread.MAX_PRIORITY);
            updateThread.start();
        }

        // if generating and there are free threads to use
        else if (generate && hasFreeThreads()) {
            inLoop = true;

            // request new valid coordinates
            chunkCreator.setCurrentChunkX(getCurrentChunkXId());
            chunkCreator.setCurrentChunkZ(getCurrentChunkZId());
            Coordinates coordinates = chunkCreator.getNewCoordinates();

            if (coordinates != null) {

                atMax = false;
                int x = coordinates.x;
                int z = coordinates.z;

                int newChunkX = coordinates.x;
                int newChunkZ = coordinates.z;

                // Start a new thread, make a new chunk
                int threadId = getFreeThread();
                threads[threadId] = new ChunkMaker(threadId, data, newChunkX, newChunkZ, x * Chunk.CHUNK_WIDTH, z * Chunk.CHUNK_WIDTH, map);
                threads[threadId].setPriority(Thread.MIN_PRIORITY);
                threads[threadId].start();

            }
            else {
                // Reached chunk creation distance.
                atMax = true;
                if (initialLoad && Voxels.USE_SEED)
                    System.out.println("Loaded all chunks. Seed: " + Voxels.SEED);
                initialLoad = false;
            }

        }
        // Check if there are threads that are completed
        if (inLoop) {
            if (update)
                if (updateThread != null)
                    if (!updateThread.isAlive()) {
                        createBuffers(updateThread.getUpdateData());
                        update = false;
                        updateThread = null;
                    }

            for (int i = 0; i < threads.length; i++) {
                if (threads[i] != null)
                    if (!threads[i].isAlive()) {
                        createBuffers(data[i]);
                        threads[i] = null;
                    }
            }
            // If maximum creation distance is reached, check if all threads are complete.
            if (atMax)
                if (allThreadsFinished())
                    inLoop = false;

            if (initialLoad) {
                if (map.size() > lastMessage) {

                    lastMessage = map.size();
                    String string = "Chunks loaded: " + (int) ((float) map.size() / (float) ((Voxels.chunkCreationDistance * 2 + 1) * (Voxels.chunkCreationDistance * 2 + 1)) * 100) + " % (" + map.size() + "/" + ((Voxels.chunkCreationDistance * 2 + 1) * (Voxels.chunkCreationDistance * 2 + 1)) + ")";
                    System.out.println(string);
                    Display.setTitle(string);
                }

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
            return deserialize(LZFDecoder.decode(bytes));

        } catch (LZFException ex) {
            Logger.getLogger(ChunkManager.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static Chunk deserialize(byte[] data) {
        try {
            return (Chunk) new FSTObjectInput(new ByteArrayInputStream(data)).readObject();

        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ChunkManager.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public int chunkAmount() {
        return map.size();
    }

    public ActiveChunkLoader getChunkLoader() {
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

    private boolean hasFreeThreads() {
        for (int i = 0; i < threads.length; i++) {
            if (threads[i] == null)
                return true;
        }

        return false;
    }

    private int getFreeThread() {
        for (int i = 0; i < threads.length; i++) {
            if (threads[i] == null)
                return i;
        }
        // all threads in use
        System.out.println("Wrong");
        return -1;
    }

    private boolean allThreadsFinished() {

        for (int i = 0; i < threads.length; i++)
            if (threads[i] != null)
                return false;

        return true;

    }

    private void updateChunk(int chunkX, int chunkZ) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
