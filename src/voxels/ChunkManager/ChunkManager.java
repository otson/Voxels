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
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL15;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import voxels.Voxels;
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
    public static final int maxThreads = Runtime.getRuntime().availableProcessors()-1;

    private ConcurrentHashMap<Integer, byte[]> map;
    private ConcurrentHashMap<Integer, Handle> handles;
    private ArrayList<Data> dataToProcess;
    private ChunkCoordinateCreator chunkCreator;
    private ActiveChunkLoader chunkLoader;
    private ChunkMaker[] threads = new ChunkMaker[maxThreads];
    private ChunkMaker updateThread;

    private boolean atMax = false;
    private boolean inLoop;
    private boolean initialLoad = true;
    private boolean generate = false;
    private int lastMessage = -1;
    
    private boolean wait = false;

    public ChunkManager() {
        map = new ConcurrentHashMap<>();
        handles = new ConcurrentHashMap<>();
        dataToProcess = new ArrayList<>();
        chunkCreator = new ChunkCoordinateCreator(map);
        chunkLoader = new ActiveChunkLoader(this);
        chunkLoader.setPriority(Thread.MIN_PRIORITY);
        updateThread = new ChunkMaker(map, this, dataToProcess);
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

    public void editBlock(final short type, final int x, final int y, final int z, final int chunkX, final int chunkZ) {
        //long start = System.nanoTime();

        final Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk == null) {
            System.out.println("Tried to modify a null chunk.");
            return;
        }
        new Thread(new Runnable() {
            public void run() {
                wait = true;
                chunk.blocks[x][y][z].setType(type);
                updateThread.update(chunk);
                
                if (x == Chunk.CHUNK_WIDTH - 1)
                    updateThread.update(getChunk(chunkX + 1, chunkZ));
                if (x == 0)
                    updateThread.update(getChunk(chunkX - 1, chunkZ));
                if (z == Chunk.CHUNK_WIDTH - 1)
                    updateThread.update(getChunk(chunkX, chunkZ + 1));
                if (z == 0)
                    updateThread.update(getChunk(chunkX, chunkZ - 1));
                wait = false;
            }
        }).start();

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

        // if generating and there are free threads to use
        if (generate && hasFreeThreads()) {
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
                threads[threadId] = new ChunkMaker(dataToProcess, newChunkX, newChunkZ, x * Chunk.CHUNK_WIDTH, z * Chunk.CHUNK_WIDTH, map, this);
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
            for (int i = 0; i < threads.length; i++) {
                if (threads[i] != null)
                    if (!threads[i].isAlive()) {
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
        if(wait == false)
            processBufferData();
    }

    private void processBufferData() {
        while (dataToProcess.isEmpty() == false) {
            Data data = dataToProcess.get(0);
            if (data != null) {
                if (data.UPDATE) {
                    updateBuffers(data);
                    System.out.println("Updating");
                }
                else {
                    createBuffers(data);
                }
                dataToProcess.remove(0);
            }
        }

        // Create buffers from all the data in the arrayList
//        while (dataToProcess.iterator().hasNext()) {
//            Data data = dataToProcess.iterator().next();
//            if (data.UPDATE)
//                updateBuffers(data);
//            else
//                createBuffers(data);
//            dataToProcess.remove(data);
//        }
    }

    public void stopGeneration() {
        generate = false;
    }

    public void startGeneration() {
        generate = true;
    }

    public void updateBuffers(Data data) {
        Handle temp = handles.get(new Pair(data.chunkX, data.chunkZ).hashCode());

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

        glDeleteBuffers(temp.vertexHandle);
        glDeleteBuffers(temp.normalHandle);
        glDeleteBuffers(temp.texHandle);
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

    public void setWait(boolean wait) {
        this.wait = wait;
    }
    
    
}
