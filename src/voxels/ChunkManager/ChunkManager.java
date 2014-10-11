package voxels.ChunkManager;

import Items.ItemHandler;
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
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import net.jpountz.lz4.LZ4SafeDecompressor;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferData;

import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;

import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;

import static org.lwjgl.opengl.GL15.glGenBuffers;
import org.lwjgl.util.vector.Vector3f;
import voxels.Voxels;
import static voxels.Voxels.getChunkX;
import static voxels.Voxels.getChunkX;
import static voxels.Voxels.getChunkX;
import static voxels.Voxels.getChunkX;
import static voxels.Voxels.getChunkX;

import static voxels.Voxels.getChunkX;
import static voxels.Voxels.getChunkY;
import static voxels.Voxels.getChunkY;
import static voxels.Voxels.getChunkY;
import static voxels.Voxels.getChunkY;
import static voxels.Voxels.getChunkY;

import static voxels.Voxels.getChunkY;
import static voxels.Voxels.getChunkZ;
import static voxels.Voxels.getChunkZ;
import static voxels.Voxels.getChunkZ;
import static voxels.Voxels.getChunkZ;
import static voxels.Voxels.getChunkZ;
import static voxels.Voxels.getChunkZ;
import static voxels.Voxels.getCurrentChunkXId;
import static voxels.Voxels.getCurrentChunkZId;
import static voxels.Voxels.getX;
import static voxels.Voxels.getX;
import static voxels.Voxels.getX;
import static voxels.Voxels.getX;
import static voxels.Voxels.getX;
import static voxels.Voxels.getX;
import static voxels.Voxels.getY;
import static voxels.Voxels.getY;
import static voxels.Voxels.getY;
import static voxels.Voxels.getY;
import static voxels.Voxels.getY;
import static voxels.Voxels.getY;
import static voxels.Voxels.getZ;
import static voxels.Voxels.getZ;
import static voxels.Voxels.getZ;
import static voxels.Voxels.getZ;
import static voxels.Voxels.getZ;
import static voxels.Voxels.getZ;
import static voxels.Voxels.removeBlock;
import static voxels.Voxels.toWorldX;
import static voxels.Voxels.toWorldY;
import static voxels.Voxels.toWorldZ;
import static voxels.Voxels.toX;
import static voxels.Voxels.toXid;
import static voxels.Voxels.toY;
import static voxels.Voxels.toYid;
import static voxels.Voxels.toZ;
import static voxels.Voxels.toZid;

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

    private ConcurrentHashMap<Integer, BlockingQueue> blockBuffer;
    private ConcurrentHashMap<Integer, Chunk> activeChunkMap;

    private ArrayList<Data> dataToProcess;
    private ChunkCoordinateCreator chunkCreator;
    private ChunkRenderChecker chunkRenderChecker;
    private ActiveChunkLoader chunkLoader;
    private ChunkMaker[] threads = new ChunkMaker[maxThreads];
    private ChunkMaker updateThread;

    private BlockingQueue<Pair> queue = new LinkedBlockingQueue<>();

    private ConcurrentHashMap<Integer, Integer> decompLengths;

    private boolean atMax = false;
    private boolean inLoop;
    private boolean initialLoad = true;
    private boolean generate = false;
    private int lastMessage = -1;

    private boolean wait = false;
    private int waterCounter;
    private ItemHandler itemHandler;
    private WaterHandler waterHandler;

    public ChunkManager() {
        decompLengths = new ConcurrentHashMap<>();
        map = new ConcurrentHashMap<>(16, 0.9f, 1);
        handles = new ConcurrentHashMap<>(16, 0.9f, 1);
        blockBuffer = new ConcurrentHashMap<>(16, 0.9f, 1);
        activeChunkMap = new ConcurrentHashMap<>(16, 0.9f, 1);
        dataToProcess = new ArrayList<>();
        chunkCreator = new ChunkCoordinateCreator(map);
        chunkLoader = new ActiveChunkLoader(this, activeChunkMap);
        chunkLoader.setPriority(Thread.NORM_PRIORITY);
        updateThread = new ChunkMaker(decompLengths, map, this, dataToProcess, queue);
        chunkRenderChecker = new ChunkRenderChecker(queue, map, this);
        chunkRenderChecker.setPriority(Thread.MAX_PRIORITY);
    }

    public Chunk getChunk(int chunkX, int chunkY, int chunkZ) {
        if (activeChunkMap.containsKey(new Pair(chunkX, chunkY, chunkZ).hashCode())) {
            return activeChunkMap.get(new Pair(chunkX, chunkY, chunkZ).hashCode());
        } else if (map.containsKey(new Pair(chunkX, chunkY, chunkZ).hashCode())) {
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
                threads[threadId] = new ChunkMaker(decompLengths, dataToProcess, newChunkX, newChunkY, newChunkZ, x * Chunk.CHUNK_SIZE, y * Chunk.CHUNK_SIZE, z * Chunk.CHUNK_SIZE, map, this, queue);
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

    public void castRay(byte type) {
        int maxDistance = 5;
        float increment = 0.25f;
        Vector3f vector;
        for (float f = 0f; f < maxDistance; f += increment) {
            if (type != Type.AIR) {
                vector = Voxels.getDirectionVector(maxDistance - f);
            } else {
                vector = Voxels.getDirectionVector(f);
            }

            byte block = getActiveBlock(vector);

            if (block == -1) {
                return;
            } else {
                if (type == Type.AIR) {
                    if (block != Type.UNBREAKABLE && block != Type.AIR) {
                        setActiveBlock(vector, type);
                        removeBlock.play();
                        toDropped(vector, block);
                        return;
                    }
                }
                if (type != Type.AIR) {
                    if (block == Type.AIR) {
                        setActiveBlock(vector, type);
                        return;
                    }
                }
            }
        }
    }

    public void bigRemove() {
        int size = 40;
        int maxDistance = 50;
        float increment = 0.25f;
        ConcurrentHashMap<Integer, Coordinates> chunksToUpdate = new ConcurrentHashMap<>();
        boolean found = false;
        for (float f = 0f; f < maxDistance; f += increment) {
            Vector3f vector = Voxels.getDirectionVector(f);
            byte block = getActiveBlock(vector);
            int removeAmount = 0;
            if (block != Type.AIR && block != -1) {
                found = true;
                for (int i = -size / 2; i < size / 2; i++) {
                    for (int j = -size / 2; j < size / 2; j++) {
                        for (int w = -size / 2; w < size / 2; w++) {
                            Vector3f temp = new Vector3f(vector.x + j, vector.y + i, vector.z + w);
                            block = getActiveBlock(temp);
                            if (block != Type.AIR && block != -1) {
                                removeAmount++;
                            }
                        }
                    }
                }
                int maxDropped = 1000;
                int interval = removeAmount/maxDropped;
                int timeSinceDrop = 0;
                for (int i = -size / 2; i < size / 2; i++) {
                    for (int j = -size / 2; j < size / 2; j++) {
                        for (int w = -size / 2; w < size / 2; w++) {
                            Vector3f temp = new Vector3f(vector.x + j, vector.y + i, vector.z + w);
                            block = getActiveBlock(temp);
                            if (block != Type.AIR && block != -1) {
                                setActiveBlockNoUpdate(temp, Type.AIR);
                                if(timeSinceDrop >= interval){
                                    toDropped(temp, block);
                                    timeSinceDrop = 0;
                                }
                                else{
                                    timeSinceDrop++;
                                }
                                int chunkX = getChunkX(temp.x);
                                int chunkY = getChunkY(temp.y);
                                int chunkZ = getChunkZ(temp.z);
                                chunksToUpdate.putIfAbsent(new Pair(chunkX, chunkY, chunkZ).hashCode(), new Coordinates(chunkX, chunkY, chunkZ));
                            }
                        }
                    }
                }
            }
            if (found) {
                break;
            }
        }
        for (Coordinates coord : chunksToUpdate.values()) {
            updateChunk(getActiveChunk(coord.x, coord.y, coord.z), 0, 0, 0);
        }
    }

    public void updateChunk(Chunk chunk, int x, int y, int z) {
        updateThread.update(chunk);
        checkAdjacentChunks(chunk, x, y, z);
        chunkLoader.refresh();
    }

    public void createVBO(Chunk chunk) {
        long start = System.nanoTime();
        ChunkMaker cm = new ChunkMaker(decompLengths, dataToProcess, chunk.xId, chunk.yId, chunk.zId, chunk.xCoordinate, chunk.yCoordinate, chunk.zCoordinate, map, this, queue);
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
            ChunkMaker cm = new ChunkMaker(decompLengths, dataToProcess, chunk.xId, chunk.yId, chunk.zId, chunk.xCoordinate, chunk.yCoordinate, chunk.zCoordinate, map, this, queue);
            cm.setChunk(chunk);
            cm.updateAllBlocks();
            cm.drawChunkVBO();
            cm.addDataToProcess();
        }
        //();
    }

    public void processBufferData() {
        int count = 0;
        if (dataToProcess != null) {
            while (dataToProcess.isEmpty() == false && count < 500000) {
                count++;
                Data data = dataToProcess.remove(0);
                if (data != null) {
                    if (data.UPDATE) {
                        glDeleteBuffers(data.normalHandle);
                        glDeleteBuffers(data.texHandle);
                        glDeleteBuffers(data.vertexHandle);
                        createBuffers(data);
                    } else {
                        createBuffers(data);
                    }
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
        glBufferData(GL_ARRAY_BUFFER, data.vertexData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glBindBuffer(GL_ARRAY_BUFFER, data.normalHandle);
        glBufferData(GL_ARRAY_BUFFER, data.normalData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        glBindBuffer(GL_ARRAY_BUFFER, data.texHandle);
        glBufferData(GL_ARRAY_BUFFER, data.texData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
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

        handles.put(new Pair(data.chunkX, data.chunkY, data.chunkZ).hashCode(), new Handle(vboVertexHandle, vboNormalHandle, vboTexHandle, data.vertices));
    }

    public boolean isAtMax() {
        return atMax;
    }

    public Chunk toChunk(byte[] bytes) {
//        LZ4FastDecompressor decompressor = factory.fastDecompressor();
//        //byte[] restored = new byte[decompressedLength];
//        Chunk chunk = deserialize(decompressor.decompress(bytes, decompressedLength));
//        return chunk;
//        
        //long start = System.nanoTime();
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

    public void startChunkRenderChecker() {
        chunkRenderChecker.start();
    }

    public ChunkRenderChecker getChunkRenderChecker() {
        return chunkRenderChecker;
    }

    public ConcurrentHashMap<Integer, BlockingQueue> getBlockBuffer() {
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

//    public void processWater() {
//        waterCounter++;
//        if (waterCounter % 6 == 0) {
//            chunkLoader.simulateWater();
//        }
//    }
    public byte getBlock(int x, int y, int z) {
        int xId = toXid(x);
        int yId = toYid(y);
        int zId = toZid(z);
        Chunk chunk = getChunk(xId, yId, zId);

        return chunk.blocks[toX(x)][toY(y)][toZ(z)];
    }

//    public byte getActiveBlock(int x, int y, int z) {
//        int xId = toXid(x);
//        int yId = toYid(y);
//        int zId = toZid(z);
//        Chunk chunk = getActiveChunk(xId, yId, zId);
//        if (chunk != null) {
//            return chunk.blocks[toX(x)][toY(y)][toZ(z)];
//        } else {
//            return -1;
//        }
//    }
    public void setActiveBlock(int x, int y, int z, byte type) {
        int xId = toXid(x);
        int yId = toYid(y);
        int zId = toZid(z);
        Chunk chunk = getActiveChunk(xId, yId, zId);
        if (chunk != null) {
            chunk.setBlock(toX(x), toY(y), toZ(z), type);
        } else {
            System.out.println("Failed to in setActiveBlock");
        }
    }

    public byte getActiveBlock(Vector3f v) {
        Chunk chunk = getActiveChunk(getChunkX(v.x), getChunkY(v.y), getChunkZ(v.z));
        if (chunk != null) {
            return chunk.blocks[getX(v.x)][getY(v.y)][getZ(v.z)];
        } else {
            return -1;
        }
    }
    
    public byte getActiveBlock(float x, float y, float z) {
        return getActiveBlock(new Vector3f(x,y,z));
    }
    
    public void setActiveBlock(float x, float y, float z, byte type) {
        setActiveBlock(new Vector3f(x,y,z), type);
    }

    public void setActiveBlock(Vector3f v, byte type) {
        Chunk chunk = getActiveChunk(getChunkX(v.x), getChunkY(v.y), getChunkZ(v.z));
        if (chunk != null) {
            chunk.blocks[getX(v.x)][getY(v.y)][getZ(v.z)] = type;
            if(type == Type.WATER)
                waterHandler.add(new Water(toWorldX(v.x),toWorldY(v.y),toWorldZ(v.z),0,0,0,0));
            updateThread.update(chunk);
            checkAdjacentChunks(chunk, getX(v.x), getY(v.y), getZ(v.z));
            //processBufferData();
            chunkLoader.refresh();
        }

    }

    public void setActiveBlockNoUpdate(Vector3f v, byte type) {
        Chunk chunk = getActiveChunk(getChunkX(v.x), getChunkY(v.y), getChunkZ(v.z));
        if (chunk != null) {
            chunk.blocks[getX(v.x)][getY(v.y)][getZ(v.z)] = type;
            if(type == Type.WATER)
                waterHandler.add(new Water(toWorldX(v.x),toWorldY(v.y),toWorldZ(v.z),0,0,0,0));
        }
    }

    public ConcurrentHashMap<Integer, Handle> getHandles() {
        return handles;
    }

    public ArrayList<Data> getDataToProcess() {
        return dataToProcess;
    }

    public BlockingQueue<Pair> getQueue() {
        return queue;
    }

    public void toDropped(Vector3f coords, byte type) {
        itemHandler.put(new ItemLocation(coords.x, coords.y, coords.z, type));
    }

    public void setItemHandler(ItemHandler itemHandler) {
        this.itemHandler = itemHandler;
    }

    public void setWaterHandler(WaterHandler waterHandler) {
        this.waterHandler = waterHandler;
    }
    

}
