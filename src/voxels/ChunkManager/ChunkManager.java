package voxels.ChunkManager;

import com.ning.compress.lzf.LZFDecoder;
import com.ning.compress.lzf.LZFException;
import de.ruedigermoeller.serialization.FSTObjectInput;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import static java.lang.Thread.sleep;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
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
    public static int maxThreads = Runtime.getRuntime().availableProcessors();

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
        map = new ConcurrentHashMap<>(16, 0.9f, 1);
        handles = new ConcurrentHashMap<>(16, 0.9f, 1);
        dataToProcess = new ArrayList<>();
        chunkCreator = new ChunkCoordinateCreator(map);
        chunkLoader = new ActiveChunkLoader(this);
        chunkLoader.setPriority(Thread.MIN_PRIORITY);
        updateThread = new ChunkMaker(map, this, dataToProcess);
    }

    public Block getBlock(int x, int y, int z) {

        return null;
    }

    public Chunk getChunk(int chunkX, int chunkY, int chunkZ) {
        if (map.containsKey(new Pair(chunkX, chunkY, chunkZ).hashCode())) {
            return toChunk(map.get(new Pair(chunkX, chunkY, chunkZ).hashCode()));
        } else {
            return null;
        }
    }

    public void editBlock(short type, int x, int y, int z, int chunkX, int chunkY, int chunkZ) {
        //long start = System.nanoTime();
        if (y < 0) {
            chunkY--;
            y = Chunk.CHUNK_SIZE + y;
        }

        Chunk chunk = getChunk(chunkX, chunkY, chunkZ);
        if (chunk == null) {
            System.out.println("Tried to modify a null chunk.");
            return;
        }

        chunk.blocks[x][y][z].setType(type);
        int oldVertexCount = chunk.getVertices();
        System.out.print("Old vertices: " + chunk.getVertices());
        updateThread.update(chunk);
        System.out.println("New vertices: " + chunk.getVertices() + " Change: " + (chunk.getVertices() - oldVertexCount));

        if (x == Chunk.CHUNK_SIZE - 1) {
            updateThread.updateLeft(getChunk(chunkX + 1, chunkY, chunkZ));
        }
        if (x == 0) {
            updateThread.updateRight(getChunk(chunkX - 1, chunkY, chunkZ));
        }
        if (y == Chunk.CHUNK_SIZE - 1) {
            updateThread.updateLeft(getChunk(chunkX, chunkY + 1, chunkZ));
        }
        if (y == 0) {
            updateThread.updateRight(getChunk(chunkX, chunkY - 1, chunkZ));
        }
        if (z == Chunk.CHUNK_SIZE - 1) {
            updateThread.updateBack(getChunk(chunkX, chunkY, chunkZ + 1));
        }
        if (z == 0) {
            updateThread.updateFront(getChunk(chunkX, chunkY, chunkZ - 1));
        }
        chunkLoader.refresh();

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
                    threads = new ChunkMaker[2];
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
                    String string = "Chunks created: " + (int) ((float) map.size() / (float) ((Voxels.chunkCreationDistance * 2 + 1) * (Voxels.chunkCreationDistance * 2 + 1) * Chunk.WORLD_HEIGHT) * 100) + " % (" + map.size() + "/" + ((Voxels.chunkCreationDistance * 2 + 1) * (Voxels.chunkCreationDistance * 2 + 1) * Chunk.WORLD_HEIGHT) + ")";
                    System.out.println(string);
                    Display.setTitle(string);
                }
            }
        }
        if (wait == false) {
            processBufferData();
        }
    }

    public void castRay(Short type) {
        int maxDistance = 4;
        Vector3f vector;
        for (float f = 0f; f < maxDistance; f += 0.25f) {
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

            Chunk chunk = getChunk(xChunkId, yChunkId, zChunkId);
            if (chunk == null) {
                System.out.println("Tried to modify a null chunk.");
                return;
            }
            if (type == Type.DIRT) {
                if (chunk.blocks[xInChunk][yInChunk][zInChunk].is(Type.AIR)) {
                    chunk.blocks[xInChunk][yInChunk][zInChunk].setType(type);
                    updateThread.update(chunk);
                    processBufferData();
                    checkAdjacentChunks(chunk, xInChunk, yInChunk, zInChunk);
                    processBufferData();
                    chunkLoader.refresh();
                    break;
                }
            } else if (type == Type.AIR) {
                if (chunk.blocks[xInChunk][yInChunk][zInChunk].is(Type.DIRT)) {
                    chunk.blocks[xInChunk][yInChunk][zInChunk].setType(type);
                    updateThread.update(chunk);
                    processBufferData();
                    checkAdjacentChunks(chunk, xInChunk, yInChunk, zInChunk);
                    processBufferData();
                    chunkLoader.refresh();
                    break;
                }
            }
        }
    }

    public void createVBOs() {

        Collection c = map.values();
        Iterator itr = c.iterator();
        while (itr.hasNext()) {
            Chunk chunk = toChunk((byte[]) itr.next());
            ChunkMaker cm = new ChunkMaker(dataToProcess, chunk.xId, chunk.yId, chunk.zId, chunk.xCoordinate, chunk.yCoordinate, chunk.zCoordinate, map, this);
            cm.setChunk(chunk);
            cm.updateAllBlocks();
            cm.drawChunkVBO();
            cm.addDataToProcess();

        }
        processBufferData();
        maxThreads = 1;
        threads = new ChunkMaker[maxThreads];
    }

    private void processBufferData() {
        int count = 0;
        if (dataToProcess != null) {
            while (dataToProcess.isEmpty() == false && count < 50000) {
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

    private void checkAdjacentChunks(Chunk chunk, int x, int y, int z) {
        /**
         * 
         * For now, all the blocks are updated.
         * 
         */
        if (x == Chunk.CHUNK_SIZE - 1) {
            updateThread.update(getChunk(chunk.xId + 1, chunk.yId, chunk.zId));
        }
        if (x == 0) {
            updateThread.update(getChunk(chunk.xId - 1, chunk.yId, chunk.zId));
        }
        if (y == Chunk.CHUNK_SIZE - 1) {
            updateThread.update(getChunk(chunk.xId, chunk.yId + 1, chunk.zId));
        }
        if (y == 0) {
            updateThread.update(getChunk(chunk.xId, chunk.yId - 1, chunk.zId));
        }
        if (z == Chunk.CHUNK_SIZE - 1) {
            updateThread.update(getChunk(chunk.xId, chunk.yId, chunk.zId + 1));
        }
        if (z == 0) {
            updateThread.update(getChunk(chunk.xId, chunk.yId, chunk.zId - 1));
        }
    }

}
