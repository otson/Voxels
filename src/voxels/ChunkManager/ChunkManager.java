package voxels.ChunkManager;

import com.ning.compress.lzf.LZFDecoder;
import com.ning.compress.lzf.LZFException;
import de.ruedigermoeller.serialization.FSTObjectInput;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import org.lwjgl.util.vector.Vector3f;
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
    public static int maxThreads = Runtime.getRuntime().availableProcessors() - 4;

    private ConcurrentHashMap<Integer, byte[]> map;
    private ConcurrentHashMap<Integer, Handle> handles;
    private ConcurrentHashMap<Integer, LinkedList<BlockCoord>> blockBuffer;
    private ConcurrentHashMap<Integer, Chunk> activeChunkMap;

    private ArrayList<Data> dataToProcess;
    private ChunkCoordinateCreator chunkCreator;
    private ChunkRenderChecker[] chunkRenderChecker;
    private ActiveChunkLoader chunkLoader;
    private ChunkMaker[] threads = new ChunkMaker[maxThreads];
    private ChunkMaker updateThread;

    //BlockingQueue<Pair> queue = new LinkedBlockingQueue<>();

    private boolean atMax = false;
    private boolean inLoop;
    private boolean initialLoad = true;
    private boolean generate = false;
    private int lastMessage = -1;

    private boolean wait = false;

    public ChunkManager() {
        map = new ConcurrentHashMap<>(16, 0.9f, 1);
        handles = new ConcurrentHashMap<>(16, 0.9f, 1);
        blockBuffer = new ConcurrentHashMap<>(16, 0.9f, 1);
        activeChunkMap = new ConcurrentHashMap<>(16, 0.9f, 1);
        dataToProcess = new ArrayList<>();
        chunkCreator = new ChunkCoordinateCreator(map);
        chunkLoader = new ActiveChunkLoader(this, activeChunkMap);
        chunkLoader.setPriority(Thread.NORM_PRIORITY);
        updateThread = new ChunkMaker(map, this, dataToProcess);
        chunkRenderChecker = new ChunkRenderChecker[Chunk.WORLD_HEIGHT];//(queue, map, this);
        initChunkRenderCheckers();
        //chunkRenderChecker.setPriority(Thread.MAX_PRIORITY);
    }

    public Chunk getChunk(int chunkX, int chunkY, int chunkZ) {
        if (map.containsKey(new Pair(chunkX, chunkY, chunkZ).hashCode())) {
            return toChunk(map.get(new Pair(chunkX, chunkY, chunkZ).hashCode()));
        } else {
            return null;
        }
    }

    public Handle getHandle(int x, int y, int z) {
        if (handles.containsKey(new Pair(x, y, z).hashCode())) {
            return handles.get(new Pair(x, y, z).hashCode());
        } else {
            return null;
        }
    }

    public boolean isChunk(int chunkX, int chunkY, int chunkZ) {
        return map.containsKey(new Pair(chunkX, chunkY, chunkZ).hashCode());
    }

    public void checkChunkUpdates() {

        // if generating and there are free threads to use
        if (generate && hasFreeThreads()) {
            inLoop = true;

            // request new valid coordinates
            chunkCreator.setCurrentChunkX(getCurrentChunkXId());
            chunkCreator.setCurrentChunkZ(getCurrentChunkZId());
            Coordinates coordinates = chunkCreator.getXYZ();

            if (coordinates != null) {

                atMax = false;
                int x = coordinates.x;
                int y = coordinates.y;
                int z = coordinates.z;

                int newChunkX = coordinates.x;
                int newChunkY = coordinates.y;
                int newChunkZ = coordinates.z;

                // Start a new thread, make a new chunk
                int threadId = getFreeThread();
                threads[threadId] = new ChunkMaker(dataToProcess, newChunkX, newChunkY, newChunkZ, x * Chunk.CHUNK_SIZE, y * Chunk.CHUNK_SIZE, z * Chunk.CHUNK_SIZE, map, this);
                threads[threadId].setPriority(Thread.MIN_PRIORITY);
                threads[threadId].start();

            } else {
                // Reached chunk creation distance.
                atMax = true;
                if (initialLoad && Voxels.USE_SEED) {
                    System.out.println("Loaded all chunks. Seed: " + Voxels.SEED);
                }
                if (initialLoad) {
                    threads = new ChunkMaker[maxThreads];
                }
                initialLoad = false;
            }

        }
        // Check if there are threads that are completed
        if (inLoop) {
            for (int i = 0; i < threads.length; i++) {
                if (threads[i] != null) {
                    if (!threads[i].isAlive()) {
                        threads[i] = null;
                    }
                }
            }
            // If maximum creation distance is reached, check if all threads are complete.
            if (atMax) {
                if (allThreadsFinished()) {
                    inLoop = false;
                }
            }

            if (initialLoad) {
                if (map.size() > lastMessage) {

                    lastMessage = map.size();
                    String string = "Chunks created: " + (int) ((float) map.size() / (float) ((Voxels.chunkCreationDistance * 2 + 1) * (Voxels.chunkCreationDistance * 2 + 1) * Chunk.VERTICAL_CHUNKS) * 100) + " % (" + map.size() + "/" + ((Voxels.chunkCreationDistance * 2 + 1) * (Voxels.chunkCreationDistance * 2 + 1) * Chunk.VERTICAL_CHUNKS) + ")";
                    System.out.println(string);
                    Display.setTitle(string);
                }
            }
        }
    }

    public void castRay(Short type) {
        int maxDistance = 4;
        float increment = 0.25f;
        Vector3f vector;
        for (float f = 0f; f < maxDistance; f += increment) {
            if (type == Type.DIRT) {
                vector = Voxels.getDirectionVector(maxDistance - f);
            } else {
                vector = Voxels.getDirectionVector(f);
            }
            int xInChunk = Voxels.xInChunkPointer(vector);
            int yInChunk = Voxels.yInChunkPointer(vector);
            int zInChunk = Voxels.zInChunkPointer(vector);
            int xChunkId = Voxels.getPointerChunkXId(vector);
            int yChunkId = Voxels.getPointerChunkYId(vector);
            int zChunkId = Voxels.getPointerChunkZId(vector);

            Chunk chunk = getActiveChunk(xChunkId, yChunkId, zChunkId);
            if (chunk == null) {
                System.out.println("Tried to modify a null chunk.");
                return;
            } else if (type == Type.DIRT) {
                if (chunk.blocks[xInChunk][yInChunk][zInChunk].is(Type.AIR)) {
                    chunk.blocks[xInChunk][yInChunk][zInChunk].setType(type);
                    updateThread.update(chunk);
                    checkAdjacentChunks(chunk, xInChunk, yInChunk, zInChunk);
                    processBufferData();
                    chunkLoader.refresh();
                    break;
                }
            } else if (type == Type.AIR) {
                if (!chunk.blocks[xInChunk][yInChunk][zInChunk].is(Type.AIR)) {
                    chunk.blocks[xInChunk][yInChunk][zInChunk].setType(type);
                    updateThread.update(chunk);
                    checkAdjacentChunks(chunk, xInChunk, yInChunk, zInChunk);
                    processBufferData();
                    chunkLoader.refresh();
                    break;
                }
            }
        }
    }

    public void createVBO(Chunk chunk) {
        long start = System.nanoTime();
        ChunkMaker cm = new ChunkMaker(dataToProcess, chunk.xId, chunk.yId, chunk.zId, chunk.xCoordinate, chunk.yCoordinate, chunk.zCoordinate, map, this);
        cm.setChunk(chunk);
        cm.updateAllBlocks();
        cm.drawChunkVBO();
        cm.addDataToProcess();
        //System.out.println("CreateVBO took: " + (System.nanoTime() - start) / 1000000 + " ms.");

    }

    public void createVBOs() {

        Collection c = activeChunkMap.values();
        System.out.println("Values in activeChunkMap: " + activeChunkMap.size());
        Iterator itr = c.iterator();
        while (itr.hasNext()) {
            Chunk chunk = (Chunk) itr.next();
            ChunkMaker cm = new ChunkMaker(dataToProcess, chunk.xId, chunk.yId, chunk.zId, chunk.xCoordinate, chunk.yCoordinate, chunk.zCoordinate, map, this);
            cm.setChunk(chunk);
            cm.updateAllBlocks();
            cm.drawChunkVBO();
            cm.addDataToProcess();
        }
        processBufferData();
    }

    public void processBufferData() {
        int count = 0;
        if (dataToProcess != null) {
            while (dataToProcess.isEmpty() == false && count < 500000) {
                count++;
                Data data = dataToProcess.get(0);
                if (data != null) {
                    if (data.UPDATE) {
                        createBuffers(data);
                    } else {
                        createBuffers(data);
                    }
                    dataToProcess.remove(0);
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

    public void updateBuffers(Data data) {
        glBindBuffer(GL_ARRAY_BUFFER, data.vertexHandle);
        glBufferData(GL_ARRAY_BUFFER, data.vertexData, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glBindBuffer(GL_ARRAY_BUFFER, data.normalHandle);
        glBufferData(GL_ARRAY_BUFFER, data.normalData, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glBindBuffer(GL_ARRAY_BUFFER, data.texHandle);
        glBufferData(GL_ARRAY_BUFFER, data.texData, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void createBuffers(Data data) {

        int vboVertexHandle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandle);
        glBufferData(GL_ARRAY_BUFFER, data.vertexData, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vboNormalHandle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboNormalHandle);
        glBufferData(GL_ARRAY_BUFFER, data.normalData, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vboTexHandle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboTexHandle);
        glBufferData(GL_ARRAY_BUFFER, data.texData, GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        handles.put(new Pair(data.chunkX, data.chunkY, data.chunkZ).hashCode(), new Handle(vboVertexHandle, vboNormalHandle, vboTexHandle, data.vertices));
    }

    public boolean isAtMax() {
        return atMax;
    }

    public Chunk toChunk(byte[] bytes) {
        long start = System.nanoTime();
        byte[] temp;
        try {
            temp = LZFDecoder.decode(bytes);
            //System.out.println("Decoding: " + (System.nanoTime() - start) / 1000000 + " ms.");
            return deserialize(temp);

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
//        long start = System.nanoTime();
//        ByteArrayInputStream bis = new ByteArrayInputStream(data);
//        ObjectInput in = null;
//        try {
//            in = new ObjectInputStream(bis);
//            Chunk temp = (Chunk) in.readObject();
//            //System.out.println("Deserializing: " + (System.nanoTime() - start) / 1000000 + " ms.");
//
//            return temp;//(Chunk) in.readObject();
//        } catch (IOException | ClassNotFoundException ex) {
//            Logger.getLogger(ChunkManager.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                bis.close();
//            } catch (IOException ex) {
//                // ignore close exception
//            }
//            try {
//                if (in != null) {
//                    in.close();
//                }
//            } catch (IOException ex) {
//                // ignore close exception
//            }
//        }
        return null;
    }

    public int chunkAmount() {
        return map.size();
    }

    public ActiveChunkLoader getChunkLoader() {
        return chunkLoader;
    }

    public Chunk getMiddle() {
        return chunkLoader.getMiddle();
    }

    public Chunk getBottom() {
        return chunkLoader.getBottom();
    }

    private boolean hasFreeThreads() {
        for (int i = 0; i < threads.length; i++) {
            if (threads[i] == null) {
                return true;
            }
        }

        return false;
    }

    private int getFreeThread() {
        for (int i = 0; i < threads.length; i++) {
            if (threads[i] == null) {
                return i;
            }
        }
        // all threads in use
        System.out.println("Wrong");
        return -1;
    }

    private boolean allThreadsFinished() {

        for (int i = 0; i < threads.length; i++) {
            if (threads[i] != null) {
                return false;
            }
        }

        return true;

    }

    public void setWait(boolean wait) {
        this.wait = wait;
    }

    public void putUncompressed(Chunk chunk) {
        chunkLoader.put(chunk);
    }

    private void checkAdjacentChunks(Chunk chunk, int x, int y, int z) {
        /**
         *
         * For now, all the blocks are updated.
         *
         */
        if (x == Chunk.CHUNK_SIZE - 1) {
            updateThread.update(getActiveChunk(chunk.xId + 1, chunk.yId, chunk.zId));
        }
        if (x == 0) {
            updateThread.update(getActiveChunk(chunk.xId - 1, chunk.yId, chunk.zId));
        }
        if (y == Chunk.CHUNK_SIZE - 1) {
            updateThread.update(getActiveChunk(chunk.xId, chunk.yId + 1, chunk.zId));
        }
        if (y == 0) {
            updateThread.update(getActiveChunk(chunk.xId, chunk.yId - 1, chunk.zId));
        }
        if (z == Chunk.CHUNK_SIZE - 1) {
            updateThread.update(getActiveChunk(chunk.xId, chunk.yId, chunk.zId + 1));
        }
        if (z == 0) {
            updateThread.update(getActiveChunk(chunk.xId, chunk.yId, chunk.zId - 1));
        }
    }

    public int getTotalChunks() {
        return map.size();
    }


    public ConcurrentHashMap<Integer, LinkedList<BlockCoord>> getBlockBuffer() {
        return blockBuffer;
    }

    public ConcurrentHashMap<Integer, byte[]> getMap() {
        return map;
    }

    public Chunk getActiveChunk(int currentChunkXId, int currentChunkYId, int currentChunkZId) {
        Chunk chunk = activeChunkMap.get(new Pair(currentChunkXId, currentChunkYId, currentChunkZId).hashCode());
        if (chunk == null) {
            return null;
        } else {
            return chunk;
        }
    }

    private void initChunkRenderCheckers() {
        for (int i = 0; i < Chunk.WORLD_HEIGHT; i++) {
            chunkRenderChecker[i] = new ChunkRenderChecker(i, map, this);
        }
    }
    
    public void startChunkRenderCheckers() {
        for (int i = 0; i < Chunk.WORLD_HEIGHT; i++) {
            chunkRenderChecker[i].start();
        }
    }

    void offerToRender(Pair pair, int y) {
        try {
            chunkRenderChecker[y].getQueue().offer(pair, 5, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Logger.getLogger(ChunkManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
