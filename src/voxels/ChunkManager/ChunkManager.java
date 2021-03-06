/* 
 * Copyright (C) 2016 Otso Nuortimo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package voxels.ChunkManager;

import Items.ItemHandler;
import com.ning.compress.lzf.LZFDecoder;
import com.ning.compress.lzf.LZFException;
import de.ruedigermoeller.serialization.FSTObjectInput;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.opengl.Display;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import org.lwjgl.util.vector.Vector3f;
import voxels.Voxels;
import static voxels.Voxels.removeBlock;
import static voxels.Voxels.toX;
import static voxels.Voxels.toXid;
import static voxels.Voxels.toY;
import static voxels.Voxels.toYid;
import static voxels.Voxels.toZ;
import static voxels.Voxels.toZid;
import static voxels.Voxels.getBlockX;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static voxels.Voxels.getBlockY;
import static voxels.Voxels.getBlockZ;
import static voxels.Voxels.getChunkX;
import static voxels.Voxels.getChunkY;
import static voxels.Voxels.getChunkZ;
import static voxels.Voxels.getCurrentChunkXId;
import static voxels.Voxels.getCurrentChunkZId;
import static voxels.Voxels.getBlockX;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static voxels.Voxels.getBlockY;
import static voxels.Voxels.getChunkX;
import static voxels.Voxels.getChunkY;
import static voxels.Voxels.getChunkZ;
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
    
    private ConcurrentHashMap<Integer, BlockingQueue> blockBuffer;
    private ConcurrentHashMap activeChunkMap;
    
    private ArrayList<Data> dataToProcess;
    private ChunkCoordinateCreator chunkCreator;
    private ChunkRenderChecker chunkRenderChecker;
    private ActiveChunkLoader chunkLoader;
    private ChunkMaker[] threads = new ChunkMaker[maxThreads];
    private ChunkMaker ChunkVBOMaker;
    
    private BlockingQueue<Triple> queue = new LinkedBlockingQueue<>();
    
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
    private byte selectedBlock = 8;
    
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
        ChunkVBOMaker = new ChunkMaker(decompLengths, map, this, dataToProcess, queue);
        chunkRenderChecker = new ChunkRenderChecker(queue, map, this);
        chunkRenderChecker.setPriority(Thread.MAX_PRIORITY);
    }
    
    public Chunk getChunk(int chunkX, int chunkY, int chunkZ) {
        if (activeChunkMap.containsKey(new Triple(chunkX, chunkY, chunkZ).hashCode())) {
            return (Chunk)activeChunkMap.get(new Triple(chunkX, chunkY, chunkZ).hashCode());
        } else if (map.containsKey(new Triple(chunkX, chunkY, chunkZ).hashCode())) {
            return toChunk(map.get(new Triple(chunkX, chunkY, chunkZ).hashCode()));
        } else {
            return null;
        }
    }
    public void increaseSelectedBlock(){
        selectedBlock++;
        if(selectedBlock == 7) // skip grass
            selectedBlock = 8;
        if(selectedBlock > 12)
            selectedBlock = 1;
    }
    
    public void decreaseSelectedBlock(){
        selectedBlock--;
        if(selectedBlock == 7) // skip grass
            selectedBlock = 6;
        if(selectedBlock < 1)
            selectedBlock = 12;
    }
    public byte getSelectedBlock(){
        return selectedBlock;
    }
    
    public Handle getHandle(int x, int y, int z) {
        if (handles.containsKey(new Triple(x, y, z).hashCode())) {
            return handles.get(new Triple(x, y, z).hashCode());
        } else {
            return null;
        }
    }
    
    public boolean isChunk(int chunkX, int chunkY, int chunkZ) {
        return map.containsKey(new Triple(chunkX, chunkY, chunkZ).hashCode());
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
        Vector3f pos;
        for (float f = 0f; f < maxDistance; f += increment) {
            if (type != Type.AIR) {
                pos = Voxels.getDirectionVector(maxDistance - f);
            } else {
                pos = Voxels.getDirectionVector(f);
            }
            
            byte block = getBlock(pos);
            
            if (block == -1) {
                return;
            } else {
                if (type == Type.AIR) {
                    if (block != Type.UNBREAKABLE && block != Type.AIR) {
                        setBlock(pos, type);
                        removeBlock.play();
                        if (block > 0) {
                            toDropped(pos, block);
                        }
                        return;
                    }
                }
                if (type != Type.AIR) {
                    if (block == Type.AIR) {
                        setBlock(pos, type);
                        return;
                    }
                }
            }
        }
    }
    
    public void bigRemove() {
        int size = 12;
        int maxDistance = 50;
        float increment = 0.25f;
        ConcurrentHashMap<Integer, Coordinates> chunksToUpdate = new ConcurrentHashMap<>();
        boolean found = false;
        for (float f = 0f; f < maxDistance; f += increment) {
            Vector3f vector = Voxels.getDirectionVector(f);
            byte block = getBlock(vector);
            int removeAmount = 0;
            if (block != Type.AIR && block != -1 && block != Type.UNBREAKABLE) {
                found = true;
                for (int i = -size / 2; i < size / 2; i++) {
                    for (int j = -size / 2; j < size / 2; j++) {
                        for (int w = -size / 2; w < size / 2; w++) {
                            Vector3f temp = new Vector3f(vector.x + j, vector.y + i, vector.z + w);
                            block = getBlock(temp);
                            if (block != Type.AIR && block != -1) {
                                removeAmount++;
                            }
                        }
                    }
                }
                int maxDropped = 1000;
                int interval = removeAmount / maxDropped;
                int timeSinceDrop = 0;
                for (int i = -size / 2; i < size / 2; i++) {
                    for (int j = -size / 2; j < size / 2; j++) {
                        for (int w = -size / 2; w < size / 2; w++) {
                            Vector3f temp = new Vector3f(vector.x + j, vector.y + i, vector.z + w);
                            block = getBlock(temp);
                            if (block != Type.AIR && block != -1 && block != Type.UNBREAKABLE) {
                                setActiveBlockNoUpdate(temp, Type.AIR);
                                if (timeSinceDrop >= interval) {
                                    toDropped(temp, block);
                                    timeSinceDrop = 0;
                                } else {
                                    timeSinceDrop++;
                                }
                                int chunkX = getChunkX(temp.x);
                                int chunkY = getChunkY(temp.y);
                                int chunkZ = getChunkZ(temp.z);
                                int xInChunk = getBlockX(temp.x);
                                int yInChunk = getBlockY(temp.y);
                                int zInChunk = getBlockZ(temp.z);
                                chunksToUpdate.putIfAbsent(new Triple(chunkX, chunkY, chunkZ).hashCode(), new Coordinates(chunkX, chunkY, chunkZ));
                                if (xInChunk == 0) {
                                    chunksToUpdate.putIfAbsent(new Triple(chunkX - 1, chunkY, chunkZ).hashCode(), new Coordinates(chunkX - 1, chunkY, chunkZ));
                                }
                                if (xInChunk == Chunk.CHUNK_SIZE - 1) {
                                    chunksToUpdate.putIfAbsent(new Triple(chunkX + 1, chunkY, chunkZ).hashCode(), new Coordinates(chunkX + 1, chunkY, chunkZ));
                                }
                                if (yInChunk == 0) {
                                    chunksToUpdate.putIfAbsent(new Triple(chunkX, chunkY - 1, chunkZ).hashCode(), new Coordinates(chunkX, chunkY - 1, chunkZ));
                                }
                                if (yInChunk == Chunk.CHUNK_SIZE - 1) {
                                    chunksToUpdate.putIfAbsent(new Triple(chunkX, chunkY + 1, chunkZ).hashCode(), new Coordinates(chunkX, chunkY + 1, chunkZ));
                                }
                                if (zInChunk == 0) {
                                    chunksToUpdate.putIfAbsent(new Triple(chunkX, chunkY, chunkZ - 1).hashCode(), new Coordinates(chunkX, chunkY, chunkZ - 1));
                                }
                                if (zInChunk == Chunk.CHUNK_SIZE - 1) {
                                    chunksToUpdate.putIfAbsent(new Triple(chunkX, chunkY, chunkZ + 1).hashCode(), new Coordinates(chunkX, chunkY, chunkZ + 1));
                                }
                                
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
            updateChunk(getActiveChunk(coord.x, coord.y, coord.z));
        }
    }
    
    public void bigAdd() {
        int size = 12;
        int maxDistance = 50;
        float increment = 0.25f;
        ConcurrentHashMap<Integer, Coordinates> chunksToUpdate = new ConcurrentHashMap<>();
        boolean found = false;
        for (float f = maxDistance; f > size/1.5f; f -= increment) {
            Vector3f vector = Voxels.getDirectionVector(f);
            byte block = getBlock(vector);
            if (block == Type.AIR && block != -1 && block != Type.UNBREAKABLE) {
                found = true;
                for (int i = -size / 2; i < size / 2; i++) {
                    for (int j = -size / 2; j < size / 2; j++) {
                        for (int w = -size / 2; w < size / 2; w++) {
                            Vector3f temp = new Vector3f(vector.x + j, vector.y + i, vector.z + w);
                            block = getBlock(temp);
                            if (block == Type.AIR && block != -1) {
                                setActiveBlockNoUpdate(temp, getSelectedBlock());
                                int chunkX = getChunkX(temp.x);
                                int chunkY = getChunkY(temp.y);
                                int chunkZ = getChunkZ(temp.z);
                                int xInChunk = getBlockX(temp.x);
                                int yInChunk = getBlockY(temp.y);
                                int zInChunk = getBlockZ(temp.z);
                                chunksToUpdate.putIfAbsent(new Triple(chunkX, chunkY, chunkZ).hashCode(), new Coordinates(chunkX, chunkY, chunkZ));
                                if (xInChunk == 0) {
                                    chunksToUpdate.putIfAbsent(new Triple(chunkX - 1, chunkY, chunkZ).hashCode(), new Coordinates(chunkX - 1, chunkY, chunkZ));
                                }
                                if (xInChunk == Chunk.CHUNK_SIZE - 1) {
                                    chunksToUpdate.putIfAbsent(new Triple(chunkX + 1, chunkY, chunkZ).hashCode(), new Coordinates(chunkX + 1, chunkY, chunkZ));
                                }
                                if (yInChunk == 0) {
                                    chunksToUpdate.putIfAbsent(new Triple(chunkX, chunkY - 1, chunkZ).hashCode(), new Coordinates(chunkX, chunkY - 1, chunkZ));
                                }
                                if (yInChunk == Chunk.CHUNK_SIZE - 1) {
                                    chunksToUpdate.putIfAbsent(new Triple(chunkX, chunkY + 1, chunkZ).hashCode(), new Coordinates(chunkX, chunkY + 1, chunkZ));
                                }
                                if (zInChunk == 0) {
                                    chunksToUpdate.putIfAbsent(new Triple(chunkX, chunkY, chunkZ - 1).hashCode(), new Coordinates(chunkX, chunkY, chunkZ - 1));
                                }
                                if (zInChunk == Chunk.CHUNK_SIZE - 1) {
                                    chunksToUpdate.putIfAbsent(new Triple(chunkX, chunkY, chunkZ + 1).hashCode(), new Coordinates(chunkX, chunkY, chunkZ + 1));
                                }
                                
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
            updateChunk(getActiveChunk(coord.x, coord.y, coord.z));
        }
    }
    
    public void updateChunk(Chunk chunk) {
        ChunkVBOMaker.update(chunk);
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
            itr.remove();
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
        glBufferData(GL_ARRAY_BUFFER, data.vertexBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        glBindBuffer(GL_ARRAY_BUFFER, data.normalHandle);
        glBufferData(GL_ARRAY_BUFFER, data.normalBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        glBindBuffer(GL_ARRAY_BUFFER, data.texHandle);
        glBufferData(GL_ARRAY_BUFFER, data.texBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
    
    public void createBuffers(Data data) {

        int vboVertexHandle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandle);
        glBufferData(GL_ARRAY_BUFFER, data.vertexBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vboNormalHandle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboNormalHandle);
        glBufferData(GL_ARRAY_BUFFER, data.normalBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vboTexHandle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboTexHandle);
        glBufferData(GL_ARRAY_BUFFER, data.texBuffer, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        /*
private ConcurrentHashMap<Integer, Handle> handles;
int vertexCount = data.vertices;
Handle h = new Handle(
vboVertexHandle, vboNormalHandle, vboNormalHandle, vertexCount);
handles.put(new Triple(data.chunkX, data.chunkY, data.chunkZ), h);
        
        */
        handles.put(new Triple(data.chunkX, data.chunkY, data.chunkZ).hashCode(), new Handle(vboVertexHandle, vboNormalHandle, vboTexHandle, data.vertices, new Coordinates(data.chunkX*Chunk.CHUNK_SIZE, data.chunkY*Chunk.CHUNK_SIZE, data.chunkZ*Chunk.CHUNK_SIZE)));
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
    
    private void checkAdjacentChunks(Vector3f position) {
        
        int xId = getChunkX(position.x);
        int yId = getChunkX(position.y);
        int zId = getChunkX(position.z);
        
        int xInChunk = getBlockX(position.x);
        int yInChunk = getBlockX(position.y);
        int zInChunk = getBlockX(position.z);
        /**
         *
         * For now, all the blocks are updated.
         *
         */
        if (xInChunk == Chunk.CHUNK_SIZE - 1) {
            ChunkVBOMaker.update(getActiveChunk(xId + 1, yId, zId));
        }
        if (xInChunk == 0) {
            ChunkVBOMaker.update(getActiveChunk(xId - 1, yId, zId));
        }
        if (yInChunk == Chunk.CHUNK_SIZE - 1) {
            ChunkVBOMaker.update(getActiveChunk(xId, yId + 1, zId));
        }
        if (yInChunk == 0) {
            ChunkVBOMaker.update(getActiveChunk(xId, yId - 1, zId));
        }
        if (zInChunk == Chunk.CHUNK_SIZE - 1) {
            ChunkVBOMaker.update(getActiveChunk(xId, yId, zId + 1));
        }
        if (zInChunk == 0) {
            ChunkVBOMaker.update(getActiveChunk(xId, yId, zId - 1));
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
        Chunk chunk = (Chunk)activeChunkMap.get(new Triple(currentChunkXId, currentChunkYId, currentChunkZId).hashCode());
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

    public void updateBlock(int x, int y, int z, byte type) {
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
    
public byte getBlock(Vector3f v) {
    Chunk chunk = getActiveChunk(getChunkX(v.x), getChunkY(v.y), getChunkZ(v.z));
    if (chunk != null) {
        return chunk.blocks[getBlockX(v.x)][getBlockY(v.y)][getBlockZ(v.z)];
    }
    // Chunk does not exist or is not in active chunks.
    else 
       return -1;
}
    
    public byte getActiveBlock(float x, float y, float z) {
        return getBlock(new Vector3f(x, y, z));
    }
    
    public void setActiveBlock(float x, float y, float z, byte type) {
        setBlock(new Vector3f(x, y, z), type);
    }
    
    public void setBlock(Vector3f pos, byte type) {
        int chunkX = getChunkX(pos.x);
        int chunkY = getChunkY(pos.y);
        int chunkZ = getChunkZ(pos.z);
        Chunk chunk = getChunk(chunkX, chunkY, chunkZ);
        if (chunk != null) {
            int blockX = getBlockX(pos.x);
            int blockY = getBlockY(pos.y);
            int blockZ = getBlockZ(pos.z);
            chunk.blocks[blockX][blockY][blockZ] = type;
            ChunkVBOMaker.update(chunk);
            checkAdjacentChunks(pos);
            chunkLoader.refresh();
        }
    }
    
    public void setActiveBlockNoUpdate(Vector3f v, byte type) {
        Chunk chunk = getActiveChunk(getChunkX(v.x), getChunkY(v.y), getChunkZ(v.z));
        if (chunk != null) {
            chunk.blocks[getBlockX(v.x)][getBlockY(v.y)][getBlockZ(v.z)] = type;
        }
    }
    
    public ConcurrentHashMap<Integer, Handle> getHandles() {
        return handles;
    }
    
    public ArrayList<Data> getDataToProcess() {
        return dataToProcess;
    }
    
    public BlockingQueue<Triple> getQueue() {
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
