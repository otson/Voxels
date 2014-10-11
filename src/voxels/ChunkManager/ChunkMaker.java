package voxels.ChunkManager;

import com.ning.compress.lzf.LZFEncoder;
import de.ruedigermoeller.serialization.FSTObjectOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import org.lwjgl.BufferUtils;

/**
 *
 * @author otso
 */
public class ChunkMaker extends Thread {

    private static int vertexSize = 3;
    private static int normalSize = 3;
    private static int texSize = 2;
    private static int colorSize = 3;

    private FloatBuffer vertexData;
    private FloatBuffer normalData;
    private FloatBuffer texData;
    private FloatBuffer colorData;

    private float[] vertexArray;
    private float[] normalArray;
    private float[] texArray;
    private float[] colorArray;

    private Chunk chunk;
    private boolean[][][] left;
    private boolean[][][] right;
    private boolean[][][] top;
    private boolean[][][] bottom;
    private boolean[][][] front;
    private boolean[][][] back;

    private ArrayList<Vertex> vertexList;
    private int chunkX;
    private int chunkY;
    private int chunkZ;
    private int xOff;
    private int yOff;
    private int zOff;
    private ConcurrentHashMap<Integer, byte[]> map;
    private LinkedList<Pair> chunksToRender;
    private BlockingQueue<Pair> queue;
    private boolean ready = false;

    private Chunk rightChunk;
    private Chunk leftChunk;
    private Chunk frontChunk;
    private Chunk backChunk;
    private Chunk topChunk;
    private Chunk bottomChunk;

    private ChunkManager chunkManager;
    private ArrayList<Data> dataToProcess;

    LZ4Factory factory = LZ4Factory.fastestInstance();
    private ConcurrentHashMap<Integer, Integer> decompLengths;

    private Data updateData;

    boolean update;

    public ChunkMaker(ConcurrentHashMap<Integer, Integer> decompLengths, ArrayList<Data> dataToProcess, int chunkX, int chunkY, int chunkZ, int xOff, int yOff, int zOff, ConcurrentHashMap<Integer, byte[]> map, ChunkManager chunkManager, BlockingQueue<Pair> queue) {
        this.decompLengths = decompLengths;
        this.xOff = xOff;
        this.yOff = yOff;
        this.zOff = zOff;
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;
        this.map = map;
        this.dataToProcess = dataToProcess;
        this.chunkManager = chunkManager;
        this.queue = queue;
        left = new boolean[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE];
        right = new boolean[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE];
        top = new boolean[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE];
        bottom = new boolean[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE];
        front = new boolean[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE];
        back = new boolean[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE];

        update = false;

    }

    public ChunkMaker(ConcurrentHashMap<Integer, Integer> decompLengths, ConcurrentHashMap<Integer, byte[]> map, ChunkManager chunkManager, ArrayList<Data> dataToProcess, BlockingQueue<Pair> queue) {
        this.decompLengths = decompLengths;
        this.map = map;
        this.chunkManager = chunkManager;
        this.dataToProcess = dataToProcess;
        this.queue = queue;
        left = new boolean[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE];
        right = new boolean[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE];
        top = new boolean[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE];
        bottom = new boolean[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE];
        front = new boolean[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE];
        back = new boolean[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE];
    }

    @Override
    public void run() {
        if (!map.containsKey(new Pair(chunkX, chunkY, chunkZ).hashCode())) {
            chunk = new Chunk(chunkX, chunkY, chunkZ);
            map.put(new Pair(chunkX, chunkY, chunkZ).hashCode(), toByte(chunk));
            //chunkManager.getChunkLoader().getChunkMap().put(new Pair(chunkX, chunkY, chunkZ).hashCode(), chunk);
            //chunkManager.putUncompressed(chunk);
            try {
                queue.offer(new Pair(chunkX, chunkY, chunkZ), 1, TimeUnit.DAYS);
            } catch (InterruptedException ex) {
                Logger.getLogger(ChunkMaker.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            System.out.println("Already contains");
        }
    }

    public void update(Chunk chunk) {
        if (chunk != null) {
            this.chunk = chunk;
            this.xOff = chunk.xCoordinate;
            this.zOff = chunk.zCoordinate;
            this.yOff = chunk.yId * Chunk.CHUNK_SIZE;
            updateAllBlocks();
            drawChunkVBO();
            chunk.setUpdateActive(true);
            chunk.setUpdatePacked(true);
            //map.put(new Pair(this.chunk.xId, this.chunk.yId, this.chunk.zId).hashCode(), toByte(this.chunk));
            Handle handle = chunkManager.getHandle(this.chunk.xId, this.chunk.yId, this.chunk.zId);
            dataToProcess.add(new Data(this.chunk.xId, this.chunk.yId, this.chunk.zId, this.chunk.getVertices(), vertexData, normalData, texData, handle.vertexHandle, handle.normalHandle, handle.texHandle, true));

        }
    }

    public void addDataToProcess() {
        dataToProcess.add(new Data(chunkX, chunkY, chunkZ, chunk.getVertices(), vertexData, normalData, texData, false));
    }

    public void drawChunkVBOold() {

        int vertices = calculateVertexCount();
        chunk.setVertices(vertices);

        vertexArray = new float[vertices * vertexSize];
        normalArray = new float[vertices * normalSize];
        texArray = new float[vertices * texSize];

        int vArrayPos = 0;
        int nArrayPos = 0;
        int tArrayPos = 0;

        vertexData = BufferUtils.createFloatBuffer(vertices * vertexSize);
        normalData = BufferUtils.createFloatBuffer(vertices * vertexSize);
        texData = BufferUtils.createFloatBuffer(vertices * texSize);
        float frontXOff;
        float frontYOff;
        float backXOff;
        float backYOff;
        float rightXOff;
        float rightYOff;
        float leftXOff;
        float leftYOff;
        float topXOff;
        float topYOff;
        float bottomXOff;
        float bottomYOff;
        float tSize = 1f / 8f;
        for (int x = 0; x < chunk.blocks.length; x++) {
            for (int z = 0; z < chunk.blocks[x][0].length; z++) {
                for (int y = 0; y < chunk.blocks[x].length; y++) {
                    if (chunk.blocks[x][y][z] != Type.AIR) {
                        short type = chunk.blocks[x][y][z];

                        frontXOff = AtlasManager.getFrontXOff(type);
                        frontYOff = AtlasManager.getFrontYOff(type);

                        backXOff = AtlasManager.getBackXOff(type);
                        backYOff = AtlasManager.getBackYOff(type);

                        rightXOff = AtlasManager.getRightXOff(type);
                        rightYOff = AtlasManager.getRightYOff(type);

                        leftXOff = AtlasManager.getLeftXOff(type);
                        leftYOff = AtlasManager.getLeftYOff(type);

                        if (type != Type.DIRT) {
                            topXOff = AtlasManager.getTopXOff(type);
                            topYOff = AtlasManager.getTopYOff(type);
                        } else {
                            topXOff = AtlasManager.getTopXOff(Type.GRASS);
                            topYOff = AtlasManager.getTopYOff(Type.GRASS);
                        }

                        bottomXOff = AtlasManager.getBottomXOff(type);
                        bottomYOff = AtlasManager.getBottomYOff(type);

                        if (front[x][y][z]) {
                            // 1st
                            // upper left - +
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 0 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = frontXOff;
                            tArrayPos++;
                            texArray[tArrayPos] = frontYOff;
                            tArrayPos++;

                            // lower left - -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 0 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = frontXOff;
                            tArrayPos++;
                            texArray[tArrayPos] = frontYOff + tSize;
                            tArrayPos++;

                            // lower right + -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 1 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = frontXOff + tSize;
                            tArrayPos++;
                            texArray[tArrayPos] = frontYOff + tSize;
                            tArrayPos++;

                            // 2nd
                            // upper left - +
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 0 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = frontXOff;
                            tArrayPos++;
                            texArray[tArrayPos] = frontYOff;
                            tArrayPos++;

                            // lower right + -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 1 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = frontXOff + tSize;
                            tArrayPos++;
                            texArray[tArrayPos] = frontYOff + tSize;
                            tArrayPos++;

                            // upper right + +
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 1 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = frontXOff + tSize;
                            tArrayPos++;
                            texArray[tArrayPos] = frontYOff;
                            tArrayPos++;

                        }
                        if (back[x][y][z]) {
                            texArray[tArrayPos] = backXOff;
                            tArrayPos++;
                            texArray[tArrayPos] = backYOff;
                            tArrayPos++;

                            texArray[tArrayPos] = backXOff;
                            tArrayPos++;
                            texArray[tArrayPos] = backYOff + tSize;
                            tArrayPos++;

                            texArray[tArrayPos] = backXOff + tSize;
                            tArrayPos++;
                            texArray[tArrayPos] = backYOff + tSize;
                            tArrayPos++;

                            //2nd
                            texArray[tArrayPos] = backXOff;
                            tArrayPos++;
                            texArray[tArrayPos] = backYOff;
                            tArrayPos++;

                            texArray[tArrayPos] = backXOff + tSize;
                            tArrayPos++;
                            texArray[tArrayPos] = backYOff + tSize;
                            tArrayPos++;

                            texArray[tArrayPos] = backXOff + tSize;
                            tArrayPos++;
                            texArray[tArrayPos] = backYOff;
                            tArrayPos++;

                            // 1st
                            // upper left + -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 1 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;
                            // lower left + -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 1 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;

                            // lower right - -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 0 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;

                            // 2nd
                            // upper left + -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 1 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;
                            // lower right - -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 0 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;

                            // upper right - +
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 0 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;

                        }
                        if (right[x][y][z]) {

                            texArray[tArrayPos] = rightXOff;
                            tArrayPos++;
                            texArray[tArrayPos] = rightYOff;
                            tArrayPos++;

                            texArray[tArrayPos] = rightXOff;
                            tArrayPos++;
                            texArray[tArrayPos] = rightYOff + tSize;
                            tArrayPos++;

                            texArray[tArrayPos] = rightXOff + tSize;
                            tArrayPos++;
                            texArray[tArrayPos] = rightYOff + tSize;
                            tArrayPos++;

                            // 2nd
                            texArray[tArrayPos] = rightXOff;
                            tArrayPos++;
                            texArray[tArrayPos] = rightYOff;
                            tArrayPos++;

                            texArray[tArrayPos] = rightXOff + tSize;
                            tArrayPos++;
                            texArray[tArrayPos] = rightYOff + tSize;
                            tArrayPos++;

                            texArray[tArrayPos] = rightXOff + tSize;
                            tArrayPos++;
                            texArray[tArrayPos] = rightYOff;
                            tArrayPos++;

                            // 1st
                            // upper right + +
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 1 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            // lower right - +
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 1 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            // lower left - -
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 1 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;

                            // 2nd
                            // upper right + +
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 1 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            // lower left - -
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 1 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;

                            // upper left + -
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 1 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;

                        }
                        if (left[x][y][z]) {
                            texArray[tArrayPos] = leftXOff;
                            tArrayPos++;
                            texArray[tArrayPos] = leftYOff;
                            tArrayPos++;

                            texArray[tArrayPos] = leftXOff;
                            tArrayPos++;
                            texArray[tArrayPos] = leftYOff + tSize;
                            tArrayPos++;

                            texArray[tArrayPos] = leftXOff + tSize;
                            tArrayPos++;
                            texArray[tArrayPos] = leftYOff + tSize;
                            tArrayPos++;

                            // 2nd
                            texArray[tArrayPos] = leftXOff;
                            tArrayPos++;
                            texArray[tArrayPos] = leftYOff;
                            tArrayPos++;

                            texArray[tArrayPos] = leftXOff + tSize;
                            tArrayPos++;
                            texArray[tArrayPos] = leftYOff + tSize;
                            tArrayPos++;

                            texArray[tArrayPos] = leftXOff + tSize;
                            tArrayPos++;
                            texArray[tArrayPos] = leftYOff;
                            tArrayPos++;

                            // 1st
                            // upper right + -
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 0 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;

                            // lower right - -
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 0 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;

                            // lower left - +
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 0 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            // 2nd
                            // upper right + -
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 0 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;

                            // lower left - +
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 0 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            // upper left + +
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 0 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;
                        }
                        if (top[x][y][z]) {
                            // 1st
                            // upper left
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 0 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = topXOff;
                            tArrayPos++;
                            texArray[tArrayPos] = topYOff;
                            tArrayPos++;

                            // lower left
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 0 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = topXOff;
                            tArrayPos++;
                            texArray[tArrayPos] = topYOff + tSize;
                            tArrayPos++;

                            // lower right
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 1 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = topXOff + tSize;
                            tArrayPos++;
                            texArray[tArrayPos] = topYOff + tSize;
                            tArrayPos++;

                            // 2nd
                            // upper left
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 0 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = topXOff;
                            tArrayPos++;
                            texArray[tArrayPos] = topYOff;
                            tArrayPos++;

                            // lower right
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 1 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = topXOff + tSize;
                            tArrayPos++;
                            texArray[tArrayPos] = topYOff + tSize;
                            tArrayPos++;

                            // upper right
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 1 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = topXOff + tSize;
                            tArrayPos++;
                            texArray[tArrayPos] = topYOff;
                            tArrayPos++;
                        }
                        if (bottom[x][y][z]) {
                            texArray[tArrayPos] = bottomXOff;
                            tArrayPos++;
                            texArray[tArrayPos] = bottomYOff;
                            tArrayPos++;

                            texArray[tArrayPos] = bottomXOff + tSize;
                            tArrayPos++;
                            texArray[tArrayPos] = bottomYOff + tSize;
                            tArrayPos++;

                            texArray[tArrayPos] = bottomXOff;
                            tArrayPos++;
                            texArray[tArrayPos] = bottomYOff + tSize;
                            tArrayPos++;

                            // 2nd
                            texArray[tArrayPos] = bottomXOff;
                            tArrayPos++;
                            texArray[tArrayPos] = bottomYOff;
                            tArrayPos++;

                            texArray[tArrayPos] = bottomXOff + tSize;
                            tArrayPos++;
                            texArray[tArrayPos] = bottomYOff;
                            tArrayPos++;

                            texArray[tArrayPos] = bottomXOff + tSize;
                            tArrayPos++;
                            texArray[tArrayPos] = bottomYOff + tSize;
                            tArrayPos++;

                            // 1st
                            // upper left + -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 1 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;

                            // lower right - +
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 0 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            // lower left - -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 0 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;

                            // 2nd
                            // upper left + -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 1 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;

                            // upper right + +
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 1 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            // lower right - +
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 0 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + y + yOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                        }
                    }
                }
            }
        }

        vertexData.put(vertexArray);
        vertexData.flip();

        normalData.put(normalArray);
        normalData.flip();

        texData.put(texArray);
        texData.flip();
    }

    public byte[] toByte(Chunk chunk) {

//        byte[] data = null;
//        data = serialize(chunk);
//        int decompressedLength = data.length;
//        decompLengths.put(new Pair(chunk.xId,chunk.yId,chunk.zId).hashCode(), decompressedLength);
//
//        
//        LZ4Compressor compressor = factory.fastCompressor();
//        int maxCompressedLength = compressor.maxCompressedLength(decompressedLength);
//        byte[] compressed = new byte[maxCompressedLength];
//        //int compressedLength = compressor.compress(data, 0, decompressedLength, compressed, 0, maxCompressedLength);
//        return compressed;
        //long start = System.nanoTime();
        byte[] temp = LZFEncoder.encode(serialize(chunk));
        //System.out.println("Encode (including serialize): " + (System.nanoTime() - start) / 1000000 + " ms.");
        return temp;

    }

    public static byte[] serialize(Object obj) {

        long start = System.nanoTime();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FSTObjectOutput os;
        try {
            os = new FSTObjectOutput(out);
            os.writeObject(obj);
        } catch (IOException ex) {
            Logger.getLogger(ChunkMaker.class.getName()).log(Level.SEVERE, null, ex);
        }

        byte[] temp = out.toByteArray();
        return temp;

//        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        ObjectOutput out = null;
//        byte[] yourBytes = null;
//        try {
//            out = new ObjectOutputStream(bos);
//            out.writeObject(obj);
//            yourBytes = bos.toByteArray();
//        } catch (IOException ex) {
//            Logger.getLogger(ChunkMaker.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                if (out != null) {
//                    out.close();
//                }
//            } catch (IOException ex) {
//                // ignore close exception
//            }
//            try {
//                bos.close();
//            } catch (IOException ex) {
//                // ignore close exception
//            }
//        }
        //System.out.println("Serializing: " + (System.nanoTime() - start) / 1000000 + " ms.");
        //return yourBytes;
        //return out.toByteArray();
    }

    public static int getNormalSize() {
        return normalSize;
    }

    public FloatBuffer getVertexData() {
        return vertexData;
    }

    public FloatBuffer getNormalData() {
        return normalData;
    }

    public FloatBuffer getTexData() {
        return texData;
    }

    public FloatBuffer getColorData() {
        return colorData;
    }

    public float[] getVertexArray() {
        return vertexArray;
    }

    public float[] getTexArray() {
        return texArray;
    }

    public float[] getColorArray() {
        return colorArray;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public boolean isReady() {
        return ready;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public boolean isUpdate() {
        return update;
    }

    public Data getUpdateData() {
        return updateData;
    }

    public void updateAllBlocks() {
        top = new boolean[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE];
        bottom = new boolean[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE];
        right = new boolean[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE];
        left = new boolean[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE];
        front = new boolean[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE];
        back = new boolean[Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE][Chunk.CHUNK_SIZE];

        getAdjacentChunks();

        updateMiddle();

        updateTopLeftBack();
        updateTopLeftFront();
        updateTopRightBack();
        updateTopRightFront();

        updateBottomLeftBack();
        updateBottomLeftFront();
        updateBottomRightBack();
        updateBottomRightFront();

        updateTopLeft();
        updateTopRight();
        updateTopFront();
        updateTopBack();

        updateBottomLeft();
        updateBottomRight();
        updateBottomFront();
        updateBottomBack();

        updateLeftBack();
        updateLeftFront();
        updateRightBack();
        updateRightFront();

        updateLeftSide();
        updateRightSide();
        updateTopSide();
        updateBottomSide();
        updateFrontSide();
        updateBackSide();

        //chunk.checkBuffer();
        rightChunk = null;
        leftChunk = null;
        backChunk = null;
        frontChunk = null;
        topChunk = null;
        bottomChunk = null;
    }

    private void getAdjacentChunks() {
        rightChunk = chunkManager.getActiveChunk(chunk.xId + 1, chunk.yId, chunk.zId);
        if (rightChunk == null) {
            rightChunk = chunkManager.getChunk(chunk.xId + 1, chunk.yId, chunk.zId);
        }
        leftChunk = chunkManager.getActiveChunk(chunk.xId - 1, chunk.yId, chunk.zId);
        if (leftChunk == null) {
            leftChunk = chunkManager.getChunk(chunk.xId - 1, chunk.yId, chunk.zId);
        }
        backChunk = chunkManager.getActiveChunk(chunk.xId, chunk.yId, chunk.zId - 1);
        if (backChunk == null) {
            backChunk = chunkManager.getChunk(chunk.xId, chunk.yId, chunk.zId - 1);
        }
        frontChunk = chunkManager.getActiveChunk(chunk.xId, chunk.yId, chunk.zId + 1);
        if (frontChunk == null) {
            frontChunk = chunkManager.getChunk(chunk.xId, chunk.yId, chunk.zId + 1);
        }
        topChunk = chunkManager.getActiveChunk(chunk.xId, chunk.yId + 1, chunk.zId);
        if (topChunk == null) {
            topChunk = chunkManager.getChunk(chunk.xId, chunk.yId + 1, chunk.zId);
        }
        bottomChunk = chunkManager.getActiveChunk(chunk.xId, chunk.yId - 1, chunk.zId);
        if (topChunk == null) {
            bottomChunk = chunkManager.getChunk(chunk.xId, chunk.yId - 1, chunk.zId);
        }
    }

    private void updateMiddle() {
        for (int x = 1; x < chunk.blocks.length - 1; x++) {
            for (int y = 1; y < chunk.blocks[x].length - 1; y++) {
                for (int z = 1; z < chunk.blocks[x][y].length - 1; z++) {

                    if (chunk.blocks[x][y][z] != Type.AIR) {

                        // set active sides to be rendered, rendered if the side is not touching dirt
                        if (chunk.blocks[x + 1][y][z] == Type.AIR) {
                            //chunk.blocks[x][y][z].setRight(true);
                            right[x][y][z] = true;
                        }
                        if (chunk.blocks[x - 1][y][z] == Type.AIR) {
                            //chunk.blocks[x][y][z].setLeft(true);
                            left[x][y][z] = true;

                        }
                        if (chunk.blocks[x][y + 1][z] == Type.AIR) {
                            //chunk.blocks[x][y][z].setTop(true);
                            top[x][y][z] = true;

                        }
                        if (chunk.blocks[x][y - 1][z] == Type.AIR) {
                            //chunk.blocks[x][y][z].setBottom(true);
                            bottom[x][y][z] = true;

                        }
                        if (chunk.blocks[x][y][z + 1] == Type.AIR) {
                            //chunk.blocks[x][y][z].setFront(true);
                            front[x][y][z] = true;

                        }
                        if (chunk.blocks[x][y][z - 1] == Type.AIR) {
                            //chunk.blocks[x][y][z].setBack(true);
                            back[x][y][z] = true;

                        }
                    }
                }
            }
        }
    }

    private void updateTopLeftBack() {
        if (leftChunk != null) {
            if (leftChunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0] == Type.AIR) {
                //chunk.blocks[0][Chunk.CHUNK_SIZE - 1][0].setLeft(true);
                left[0][Chunk.CHUNK_SIZE - 1][0] = true;
            }
        }

        if (backChunk != null) {
            if (backChunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                //chunk.blocks[0][Chunk.CHUNK_SIZE - 1][0].setBack(true);
                back[0][Chunk.CHUNK_SIZE - 1][0] = true;
            }
        }

        if (topChunk != null) {
            if (topChunk.blocks[0][0][0] == Type.AIR) {
                //chunk.blocks[0][Chunk.CHUNK_SIZE - 1][0].setTop(true);
                top[0][Chunk.CHUNK_SIZE - 1][0] = true;
            }
        }

        if (chunk.blocks[1][Chunk.CHUNK_SIZE - 1][0] == Type.AIR) {
            //chunk.blocks[0][Chunk.CHUNK_SIZE - 1][0].setRight(true);
            right[0][Chunk.CHUNK_SIZE - 1][0] = true;
        }

        if (chunk.blocks[0][Chunk.CHUNK_SIZE - 2][0] == Type.AIR) {
            //chunk.blocks[0][Chunk.CHUNK_SIZE - 1][0].setBottom(true);
            bottom[0][Chunk.CHUNK_SIZE - 1][0] = true;
        }

        if (chunk.blocks[0][Chunk.CHUNK_SIZE - 1][1] == Type.AIR) {
            //chunk.blocks[0][Chunk.CHUNK_SIZE - 1][0].setFront(true);
            front[0][Chunk.CHUNK_SIZE - 1][0] = true;
        }

    }

    private void updateTopLeftFront() {
        if (leftChunk != null) {
            if (leftChunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                //chunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setLeft(true);
                left[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] = true;
            }
        }
        if (frontChunk != null) {
            if (frontChunk.blocks[0][Chunk.CHUNK_SIZE - 1][0] == Type.AIR) {
                //chunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setFront(true);
                front[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] = true;
            }
        }

        if (topChunk != null) {
            if (topChunk.blocks[0][0][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                //chunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setTop(true);
                top[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] = true;
            }
        }

        if (chunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 2] == Type.AIR) {
            //chunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setBack(true);
            back[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] = true;
        }

        if (chunk.blocks[0][Chunk.CHUNK_SIZE - 2][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
            //chunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setBottom(true);
            bottom[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] = true;
        }

        if (chunk.blocks[1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
            //chunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setRight(true);
            right[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] = true;
        }
    }

    private void updateTopRightBack() {
        if (backChunk != null) {
            if (backChunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                //chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0].setBack(true);
                back[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0] = true;
            }
        }
        if (rightChunk != null) {
            if (rightChunk.blocks[0][Chunk.CHUNK_SIZE - 1][0] == Type.AIR) {
                //chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0].setRight(true);
                right[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0] = true;
            }
        }

        if (topChunk != null) {
            if (topChunk.blocks[Chunk.CHUNK_SIZE - 1][0][0] == Type.AIR) {
                //chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0].setTop(true);
                top[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0] = true;
            }
        }

        if (chunk.blocks[Chunk.CHUNK_SIZE - 2][Chunk.CHUNK_SIZE - 1][0] == Type.AIR) {
            //chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0].setLeft(true);
            left[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0] = true;
        }

        if (chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 2][0] == Type.AIR) {
            //chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0].setBottom(true);
            bottom[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0] = true;
        }

        if (chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][1] == Type.AIR) {
            //chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0].setFront(true);
            front[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0] = true;
        }
    }

    private void updateTopRightFront() {
        if (rightChunk != null) {
            if (rightChunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                //chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setRight(true);
                right[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] = true;
            }
        }
        if (frontChunk != null) {
            if (frontChunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0] == Type.AIR) {
                //chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setFront(true);
                front[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] = true;
            }

        }

        if (topChunk != null) {

            if (topChunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                //chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setTop(true);
                top[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] = true;
            }
        }

        if (chunk.blocks[Chunk.CHUNK_SIZE - 2][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
            //chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setLeft(true);
            left[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] = true;
        }

        if (chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 2][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
            //chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setBottom(true);
            bottom[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] = true;
        }

        if (chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 2] == Type.AIR) {
            //chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setBack(true);
            back[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] = true;
        }
    }

    private void updateTopLeft() {
        boolean isValid = leftChunk != null;

        for (int z = 1; z < Chunk.CHUNK_SIZE - 1; z++) {
            if (isValid) {
                if (leftChunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z] == Type.AIR) {
                    //chunk.blocks[0][Chunk.CHUNK_SIZE - 1][z].setLeft(true);
                    left[0][Chunk.CHUNK_SIZE - 1][z] = true;
                }
            }

            if (topChunk != null) {
                if (topChunk.blocks[0][0][z] == Type.AIR) {
                    //chunk.blocks[0][Chunk.CHUNK_SIZE - 1][z].setTop(true);
                    top[0][Chunk.CHUNK_SIZE - 1][z] = true;
                }
            }

            if (chunk.blocks[1][Chunk.CHUNK_SIZE - 1][z] == Type.AIR) {
                //chunk.blocks[0][Chunk.CHUNK_SIZE - 1][z].setRight(true);
                right[0][Chunk.CHUNK_SIZE - 1][z] = true;
            }
            if (chunk.blocks[0][Chunk.CHUNK_SIZE - 2][z] == Type.AIR) {
                //chunk.blocks[0][Chunk.CHUNK_SIZE - 1][z].setBottom(true);
                bottom[0][Chunk.CHUNK_SIZE - 1][z] = true;
            }

            if (chunk.blocks[0][Chunk.CHUNK_SIZE - 1][z + 1] == Type.AIR) {
                //chunk.blocks[0][Chunk.CHUNK_SIZE - 1][z].setFront(true);
                front[0][Chunk.CHUNK_SIZE - 1][z] = true;
            }

            if (chunk.blocks[0][Chunk.CHUNK_SIZE - 1][z - 1] == Type.AIR) {
                //chunk.blocks[0][Chunk.CHUNK_SIZE - 1][z].setBack(true);
                back[0][Chunk.CHUNK_SIZE - 1][z] = true;
            }
        }
    }

    private void updateTopRight() {
        boolean isValid = rightChunk != null;

        for (int z = 1; z < Chunk.CHUNK_SIZE - 1; z++) {
            if (isValid) {
                if (rightChunk.blocks[0][Chunk.CHUNK_SIZE - 1][z] == Type.AIR) {
                    //chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z].setRight(true);
                    right[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z] = true;
                }
            }

            if (topChunk != null) {
                if (topChunk.blocks[Chunk.CHUNK_SIZE - 1][0][z] == Type.AIR) {
                    //chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z].setTop(true);
                    top[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z] = true;
                }
            }

            if (chunk.blocks[Chunk.CHUNK_SIZE - 2][Chunk.CHUNK_SIZE - 1][z] == Type.AIR) {
                //chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z].setLeft(true);
                left[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z] = true;
            }

            if (chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 2][z] == Type.AIR) {
                //chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z].setBottom(true);
                bottom[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z] = true;
            }

            if (chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z + 1] == Type.AIR) {
                //chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z].setFront(true);
                front[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z] = true;
            }

            if (chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z - 1] == Type.AIR) {
                //chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z].setBack(true);
                back[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z] = true;
            }
        }
    }

    private void updateTopFront() {
        boolean isValid = frontChunk != null;

        for (int x = 1; x < Chunk.CHUNK_SIZE - 1; x++) {
            if (isValid) {
                if (frontChunk.blocks[x][Chunk.CHUNK_SIZE - 1][0] == Type.AIR) {
                    //chunk.blocks[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setFront(true);
                    front[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] = true;
                }
            }

            if (topChunk != null) {
                if (topChunk.blocks[x][0][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                    //chunk.blocks[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setTop(true);
                    top[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] = true;
                }
            }

            if (chunk.blocks[x + 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                //chunk.blocks[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setRight(true);
                right[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] = true;
            }

            if (chunk.blocks[x - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                //chunk.blocks[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setLeft(true);
                left[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] = true;
            }

            if (chunk.blocks[x][Chunk.CHUNK_SIZE - 2][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                //chunk.blocks[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setBottom(true);
                bottom[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] = true;
            }

            if (chunk.blocks[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 2] == Type.AIR) {
                //chunk.blocks[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setBack(true);
                back[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] = true;
            }
        }
    }

    private void updateTopBack() {
        boolean isValid = backChunk != null;

        for (int x = 1; x < Chunk.CHUNK_SIZE - 1; x++) {
            if (isValid) {
                if (backChunk.blocks[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                    //chunk.blocks[x][Chunk.CHUNK_SIZE - 1][0].setBack(true);
                    back[x][Chunk.CHUNK_SIZE - 1][0] = true;
                }
            }

            if (topChunk != null) {
                if (topChunk.blocks[x][0][0] == Type.AIR) {
                    //chunk.blocks[x][Chunk.CHUNK_SIZE - 1][0].setTop(true);
                    top[x][Chunk.CHUNK_SIZE - 1][0] = true;
                }
            }

            if (chunk.blocks[x + 1][Chunk.CHUNK_SIZE - 1][0] == Type.AIR) {
                //chunk.blocks[x][Chunk.CHUNK_SIZE - 1][0].setRight(true);
                right[x][Chunk.CHUNK_SIZE - 1][0] = true;
            }

            if (chunk.blocks[x - 1][Chunk.CHUNK_SIZE - 1][0] == Type.AIR) {
                //chunk.blocks[x][Chunk.CHUNK_SIZE - 1][0].setLeft(true);
                left[x][Chunk.CHUNK_SIZE - 1][0] = true;
            }

            if (chunk.blocks[x][Chunk.CHUNK_SIZE - 2][0] == Type.AIR) {
                //chunk.blocks[x][Chunk.CHUNK_SIZE - 1][0].setBottom(true);
                bottom[x][Chunk.CHUNK_SIZE - 1][0] = true;
            }

            if (chunk.blocks[x][Chunk.CHUNK_SIZE - 1][1] == Type.AIR) {
                //chunk.blocks[x][Chunk.CHUNK_SIZE - 1][0].setFront(true);
                front[x][Chunk.CHUNK_SIZE - 1][0] = true;
            }
        }

    }

    private void updateTopSide() {
        for (int x = 1; x < Chunk.CHUNK_SIZE - 1; x++) {
            for (int z = 1; z < Chunk.CHUNK_SIZE - 1; z++) {
                if (chunk.blocks[x][Chunk.CHUNK_SIZE - 1][z] != Type.AIR) {

                    if (topChunk != null) {
                        if (topChunk.blocks[x][0][z] == Type.AIR) {
                            //chunk.blocks[x][Chunk.CHUNK_SIZE - 1][z].setTop(true);
                            top[x][Chunk.CHUNK_SIZE - 1][z] = true;
                        }
                    }

                    if (chunk.blocks[x + 1][Chunk.CHUNK_SIZE - 1][z] == Type.AIR) {
                        //chunk.blocks[x][Chunk.CHUNK_SIZE - 1][z].setRight(true);
                        right[x][Chunk.CHUNK_SIZE - 1][z] = true;
                    }

                    if (chunk.blocks[x - 1][Chunk.CHUNK_SIZE - 1][z] == Type.AIR) {
                        //chunk.blocks[x][Chunk.CHUNK_SIZE - 1][z].setLeft(true);
                        left[x][Chunk.CHUNK_SIZE - 1][z] = true;
                    }

                    if (chunk.blocks[x][Chunk.CHUNK_SIZE - 2][z] == Type.AIR) {
                        //chunk.blocks[x][Chunk.CHUNK_SIZE - 1][z].setBottom(true);
                        bottom[x][Chunk.CHUNK_SIZE - 1][z] = true;
                    }

                    if (chunk.blocks[x][Chunk.CHUNK_SIZE - 1][z + 1] == Type.AIR) {
                        //chunk.blocks[x][Chunk.CHUNK_SIZE - 1][z].setFront(true);
                        front[x][Chunk.CHUNK_SIZE - 1][z] = true;
                    }

                    if (chunk.blocks[x][Chunk.CHUNK_SIZE - 1][z - 1] == Type.AIR) {
                        //chunk.blocks[x][Chunk.CHUNK_SIZE - 1][z].setBack(true);
                        back[x][Chunk.CHUNK_SIZE - 1][z] = true;
                    }
                }
            }

        }
    }

    private void updateBottomLeftBack() {
        if (leftChunk != null) {
            if (leftChunk.blocks[Chunk.CHUNK_SIZE - 1][0][0] == Type.AIR) {
                //chunk.blocks[0][0][0].setLeft(true);
                left[0][0][0] = true;

            }
        }

        if (backChunk != null) {
            if (backChunk.blocks[0][0][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                //chunk.blocks[0][0][0].setBack(true);
                back[0][0][0] = true;
            }
        }

        if (bottomChunk != null) {
            if (bottomChunk.blocks[0][Chunk.CHUNK_SIZE - 1][0] == Type.AIR) {
                //chunk.blocks[0][0][0].setBottom(true);
                bottom[0][0][0] = true;
            }
        }

        if (chunk.blocks[1][0][0] == Type.AIR) {
            //chunk.blocks[0][0][0].setRight(true);
            right[0][0][0] = true;
        }

        if (chunk.blocks[0][1][0] == Type.AIR) {
            //chunk.blocks[0][0][0].setTop(true);
            top[0][0][0] = true;
        }

        if (chunk.blocks[0][0][1] == Type.AIR) {
            //chunk.blocks[0][0][0].setFront(true);
            front[0][0][0] = true;
        }
    }

    private void updateBottomLeftFront() {
        if (leftChunk != null) {
            if (leftChunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                //chunk.blocks[0][0][Chunk.CHUNK_SIZE - 1].setLeft(true);
                left[0][0][Chunk.CHUNK_SIZE - 1] = true;
            }
        }

        if (frontChunk != null) {
            if (frontChunk.blocks[0][0][0] == Type.AIR) {
                //chunk.blocks[0][0][Chunk.CHUNK_SIZE - 1].setFront(true);
                front[0][0][Chunk.CHUNK_SIZE - 1] = true;
            }
        }

        if (bottomChunk != null) {
            if (bottomChunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                //chunk.blocks[0][0][Chunk.CHUNK_SIZE - 1].setBottom(true);
                bottom[0][0][Chunk.CHUNK_SIZE - 1] = true;
            }
        }

        if (chunk.blocks[1][0][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
            //chunk.blocks[0][0][Chunk.CHUNK_SIZE - 1].setRight(true);
            right[0][0][Chunk.CHUNK_SIZE - 1] = true;
        }

        if (chunk.blocks[0][1][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
            //chunk.blocks[0][0][Chunk.CHUNK_SIZE - 1].setTop(true);
            top[0][0][Chunk.CHUNK_SIZE - 1] = true;
        }

        if (chunk.blocks[0][0][Chunk.CHUNK_SIZE - 2] == Type.AIR) {
            //chunk.blocks[0][0][Chunk.CHUNK_SIZE - 1].setBack(true);
            back[0][0][Chunk.CHUNK_SIZE - 1] = true;
        }
    }

    private void updateBottomRightBack() {
        if (rightChunk != null) {
            if (rightChunk.blocks[0][0][0] == Type.AIR) {
                //chunk.blocks[Chunk.CHUNK_SIZE - 1][0][0].setRight(true);
                right[Chunk.CHUNK_SIZE - 1][0][0] = true;
            }
        }

        if (backChunk != null) {
            if (backChunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                //chunk.blocks[Chunk.CHUNK_SIZE - 1][0][0].setBack(true);
                back[Chunk.CHUNK_SIZE - 1][0][0] = true;
            }
        }

        if (bottomChunk != null) {
            if (bottomChunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0] == Type.AIR) {
                //chunk.blocks[Chunk.CHUNK_SIZE - 1][0][0].setBottom(true);
                bottom[Chunk.CHUNK_SIZE - 1][0][0] = true;
            }
        }

        if (chunk.blocks[Chunk.CHUNK_SIZE - 2][0][0] == Type.AIR) {
            //chunk.blocks[Chunk.CHUNK_SIZE - 1][0][0].setLeft(true);
            left[Chunk.CHUNK_SIZE - 1][0][0] = true;
        }

        if (chunk.blocks[Chunk.CHUNK_SIZE - 1][1][0] == Type.AIR) {
            //chunk.blocks[Chunk.CHUNK_SIZE - 1][0][0].setTop(true);
            top[Chunk.CHUNK_SIZE - 1][0][0] = true;
        }

        if (chunk.blocks[Chunk.CHUNK_SIZE - 1][0][1] == Type.AIR) {
            //chunk.blocks[Chunk.CHUNK_SIZE - 1][0][0].setFront(true);
            front[Chunk.CHUNK_SIZE - 1][0][0] = true;
        }
    }

    private void updateBottomRightFront() {

        if (rightChunk != null) {
            if (rightChunk.blocks[0][0][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                //chunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1].setRight(true);
                right[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1] = true;
            }
        }

        if (frontChunk != null) {
            if (frontChunk.blocks[Chunk.CHUNK_SIZE - 1][0][0] == Type.AIR) {
                //chunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1].setFront(true);
                front[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1] = true;
            }
        }

        if (bottomChunk != null) {
            if (bottomChunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                //chunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1].setBottom(true);
                bottom[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1] = true;
            }
        }

        if (chunk.blocks[Chunk.CHUNK_SIZE - 2][0][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
            //chunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1].setLeft(true);
            left[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1] = true;
        }

        if (chunk.blocks[Chunk.CHUNK_SIZE - 1][1][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
            //chunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1].setTop(true);
            top[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1] = true;
        }

        if (chunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 2] == Type.AIR) {
            //chunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1].setBack(true);
            back[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1] = true;
        }
    }

    private void updateBottomLeft() {
        boolean isValid = leftChunk != null;

        for (int z = 1; z < Chunk.CHUNK_SIZE - 1; z++) {
            if (isValid) {
                if (leftChunk.blocks[Chunk.CHUNK_SIZE - 1][0][z] == Type.AIR) {
                    //chunk.blocks[0][0][z].setLeft(true);
                    left[0][0][z] = true;
                }
            }

            if (bottomChunk != null) {
                if (bottomChunk.blocks[0][Chunk.CHUNK_SIZE - 1][z] == Type.AIR) {
                    //chunk.blocks[0][0][z].setBottom(true);
                    bottom[0][0][z] = true;
                }
            }

            if (chunk.blocks[1][0][z] == Type.AIR) {
                //chunk.blocks[0][0][z].setRight(true);
                right[0][0][z] = true;
            }

            if (chunk.blocks[0][1][z] == Type.AIR) {
                //chunk.blocks[0][0][z].setTop(true);
                top[0][0][z] = true;
            }

            if (chunk.blocks[0][0][z + 1] == Type.AIR) {
                //chunk.blocks[0][0][z].setFront(true);
                front[0][0][z] = true;
            }

            if (chunk.blocks[0][0][z - 1] == Type.AIR) {
                //chunk.blocks[0][0][z].setBack(true);
                back[0][0][z] = true;
            }
        }
    }

    private void updateBottomRight() {

        boolean isValid = rightChunk != null;

        for (int z = 1; z < Chunk.CHUNK_SIZE - 1; z++) {
            if (isValid) {
                if (rightChunk.blocks[0][0][z] == Type.AIR) {
                    //chunk.blocks[Chunk.CHUNK_SIZE - 1][0][z].setRight(true);
                    right[Chunk.CHUNK_SIZE - 1][0][z] = true;
                }
            }

            if (bottomChunk != null) {
                if (bottomChunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z] == Type.AIR) {
                    //chunk.blocks[Chunk.CHUNK_SIZE - 1][0][z].setBottom(true);
                    bottom[Chunk.CHUNK_SIZE - 1][0][z] = true;
                }
            }

            if (chunk.blocks[Chunk.CHUNK_SIZE - 2][0][z] == Type.AIR) {
                //chunk.blocks[Chunk.CHUNK_SIZE - 1][0][z].setLeft(true);
                left[Chunk.CHUNK_SIZE - 1][0][z] = true;
            }

            if (chunk.blocks[Chunk.CHUNK_SIZE - 1][1][z] == Type.AIR) {
                //chunk.blocks[Chunk.CHUNK_SIZE - 1][0][z].setTop(true);
                top[Chunk.CHUNK_SIZE - 1][0][z] = true;
            }

            if (chunk.blocks[Chunk.CHUNK_SIZE - 1][0][z + 1] == Type.AIR) {
                //chunk.blocks[Chunk.CHUNK_SIZE - 1][0][z].setFront(true);
                front[Chunk.CHUNK_SIZE - 1][0][z] = true;
            }

            if (chunk.blocks[Chunk.CHUNK_SIZE - 1][0][z - 1] == Type.AIR) {
                //chunk.blocks[Chunk.CHUNK_SIZE - 1][0][z].setBack(true);
                back[Chunk.CHUNK_SIZE - 1][0][z] = true;
            }
        }
    }

    private void updateBottomFront() {
        boolean isValid = frontChunk != null;

        for (int x = 1; x < Chunk.CHUNK_SIZE - 1; x++) {
            if (isValid) {
                if (frontChunk.blocks[x][0][0] == Type.AIR) {
                    //chunk.blocks[x][0][Chunk.CHUNK_SIZE - 1].setFront(true);
                    front[x][0][Chunk.CHUNK_SIZE - 1] = true;
                }
            }

            if (bottomChunk != null) {
                if (bottomChunk.blocks[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                    //chunk.blocks[x][0][Chunk.CHUNK_SIZE - 1].setBottom(true);
                    bottom[x][0][Chunk.CHUNK_SIZE - 1] = true;
                }
            }

            if (chunk.blocks[x + 1][0][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                //chunk.blocks[x][0][Chunk.CHUNK_SIZE - 1].setRight(true);
                right[x][0][Chunk.CHUNK_SIZE - 1] = true;
            }

            if (chunk.blocks[x - 1][0][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                //chunk.blocks[x][0][Chunk.CHUNK_SIZE - 1].setLeft(true);
                left[x][0][Chunk.CHUNK_SIZE - 1] = true;
            }

            if (chunk.blocks[x][1][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                //chunk.blocks[x][0][Chunk.CHUNK_SIZE - 1].setTop(true);
                top[x][0][Chunk.CHUNK_SIZE - 1] = true;
            }

            if (chunk.blocks[x][0][Chunk.CHUNK_SIZE - 2] == Type.AIR) {
                //chunk.blocks[x][0][Chunk.CHUNK_SIZE - 1].setBack(true);
                back[x][0][Chunk.CHUNK_SIZE - 1] = true;
            }
        }
    }

    private void updateBottomBack() {
        boolean isValid = backChunk != null;

        for (int x = 1; x < Chunk.CHUNK_SIZE - 1; x++) {
            if (isValid) {
                if (backChunk.blocks[x][0][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                    //chunk.blocks[x][0][0].setBack(true);
                    back[x][0][0] = true;
                }
            }

            if (bottomChunk != null) {
                if (bottomChunk.blocks[x][Chunk.CHUNK_SIZE - 1][0] == Type.AIR) {
                    //chunk.blocks[x][0][0].setBottom(true);
                    bottom[x][0][0] = true;
                }
            }

            if (chunk.blocks[x + 1][0][0] == Type.AIR) {
                //chunk.blocks[x][0][0].setRight(true);
                right[x][0][0] = true;
            }

            if (chunk.blocks[x - 1][0][0] == Type.AIR) {
                //chunk.blocks[x][0][0].setLeft(true);
                left[x][0][0] = true;
            }

            if (chunk.blocks[x][1][0] == Type.AIR) {
                //chunk.blocks[x][0][0].setTop(true);
                top[x][0][0] = true;
            }

            if (chunk.blocks[x][0][1] == Type.AIR) {
                //chunk.blocks[x][0][0].setFront(true);
                front[x][0][0] = true;
            }
        }
    }

    private void updateBottomSide() {
        for (int x = 1; x < Chunk.CHUNK_SIZE - 1; x++) {
            for (int z = 1; z < Chunk.CHUNK_SIZE - 1; z++) {
                if (chunk.blocks[x][0][z] != Type.AIR) {
                    if (bottomChunk != null) {
                        if (bottomChunk.blocks[x][Chunk.CHUNK_SIZE - 1][z] == Type.AIR) {
                            //chunk.blocks[x][0][z].setBottom(true);
                            bottom[x][0][z] = true;
                        }
                    }

                    if (chunk.blocks[x + 1][0][z] == Type.AIR) {
                        //chunk.blocks[x][0][z].setRight(true);
                        right[x][0][z] = true;
                    }

                    if (chunk.blocks[x - 1][0][z] == Type.AIR) {
                        //chunk.blocks[x][0][z].setLeft(true);
                        left[x][0][z] = true;
                    }

                    if (chunk.blocks[x][1][z] == Type.AIR) {
                        //chunk.blocks[x][0][z].setTop(true);
                        top[x][0][z] = true;
                    }

                    if (chunk.blocks[x][0][z + 1] == Type.AIR) {
                        //chunk.blocks[x][0][z].setFront(true);
                        front[x][0][z] = true;
                    }

                    if (chunk.blocks[x][0][z - 1] == Type.AIR) {
                        //chunk.blocks[x][0][z].setBack(true);
                        back[x][0][z] = true;
                    }
                }
            }
        }
    }

    private void updateLeftSide() {
        boolean isValid = leftChunk != null;

        for (int z = 1; z < Chunk.CHUNK_SIZE - 1; z++) {
            for (int y = 1; y < Chunk.CHUNK_SIZE - 1; y++) {
                if (chunk.blocks[0][y][z] != Type.AIR) {
                    if (isValid) {
                        if (leftChunk.blocks[Chunk.CHUNK_SIZE - 1][y][z] == Type.AIR) {
                            //chunk.blocks[0][y][z].setLeft(true);
                            left[0][y][z] = true;
                        }
                    }

                    if (chunk.blocks[0][y + 1][z] == Type.AIR) {
                        //chunk.blocks[0][y][z].setTop(true);
                        top[0][y][z] = true;
                    }

                    if (chunk.blocks[0][y - 1][z] == Type.AIR) {
                        //chunk.blocks[0][y][z].setBottom(true);
                        bottom[0][y][z] = true;
                    }

                    if (chunk.blocks[0][y][z + 1] == Type.AIR) {
                        //chunk.blocks[0][y][z].setFront(true);
                        front[0][y][z] = true;
                    }

                    if (chunk.blocks[0][y][z - 1] == Type.AIR) {
                        //chunk.blocks[0][y][z].setBack(true);
                        back[0][y][z] = true;
                    }

                    if (chunk.blocks[1][y][z] == Type.AIR) {
                        //chunk.blocks[0][y][z].setRight(true);
                        right[0][y][z] = true;
                    }
                }
            }
        }
    }

    private void updateRightSide() {
        boolean isValid = rightChunk != null;

        for (int z = 1; z < Chunk.CHUNK_SIZE - 1; z++) {
            for (int y = 1; y < Chunk.CHUNK_SIZE - 1; y++) {
                if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y][z] != Type.AIR) {
                    if (isValid) {
                        if (rightChunk.blocks[0][y][z] == Type.AIR) {
                            //chunk.blocks[Chunk.CHUNK_SIZE - 1][y][z].setRight(true);
                            right[Chunk.CHUNK_SIZE - 1][y][z] = true;
                        }
                    }

                    if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y + 1][z] == Type.AIR) {
                        //chunk.blocks[Chunk.CHUNK_SIZE - 1][y][z].setTop(true);
                        top[Chunk.CHUNK_SIZE - 1][y][z] = true;
                    }

                    if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y - 1][z] == Type.AIR) {
                        //chunk.blocks[Chunk.CHUNK_SIZE - 1][y][z].setBottom(true);
                        bottom[Chunk.CHUNK_SIZE - 1][y][z] = true;
                    }

                    if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y][z + 1] == Type.AIR) {
                        //chunk.blocks[Chunk.CHUNK_SIZE - 1][y][z].setFront(true);
                        front[Chunk.CHUNK_SIZE - 1][y][z] = true;
                    }

                    if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y][z - 1] == Type.AIR) {
                        //chunk.blocks[Chunk.CHUNK_SIZE - 1][y][z].setBack(true);
                        back[Chunk.CHUNK_SIZE - 1][y][z] = true;
                    }

                    if (chunk.blocks[Chunk.CHUNK_SIZE - 2][y][z] == Type.AIR) {
                        //chunk.blocks[Chunk.CHUNK_SIZE - 1][y][z].setLeft(true);
                        left[Chunk.CHUNK_SIZE - 1][y][z] = true;
                    }
                }
            }
        }
    }

    private void updateFrontSide() {
        boolean isValid = frontChunk != null;

        for (int x = 1; x < Chunk.CHUNK_SIZE - 1; x++) {
            for (int y = 1; y < Chunk.CHUNK_SIZE - 1; y++) {
                if (chunk.blocks[x][y][Chunk.CHUNK_SIZE - 1] != Type.AIR) {
                    if (isValid) {
                        if (frontChunk.blocks[x][y][0] == Type.AIR) {
                            //chunk.blocks[x][y][Chunk.CHUNK_SIZE - 1].setFront(true);
                            front[x][y][Chunk.CHUNK_SIZE - 1] = true;
                        }
                    }

                    if (chunk.blocks[x + 1][y][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                        //chunk.blocks[x][y][Chunk.CHUNK_SIZE - 1].setRight(true);
                        right[x][y][Chunk.CHUNK_SIZE - 1] = true;
                    }

                    if (chunk.blocks[x - 1][y][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                        //chunk.blocks[x][y][Chunk.CHUNK_SIZE - 1].setLeft(true);
                        left[x][y][Chunk.CHUNK_SIZE - 1] = true;
                    }
                    if (chunk.blocks[x][y + 1][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                        //chunk.blocks[x][y][Chunk.CHUNK_SIZE - 1].setTop(true);
                        top[x][y][Chunk.CHUNK_SIZE - 1] = true;
                    }

                    if (chunk.blocks[x][y - 1][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                        //chunk.blocks[x][y][Chunk.CHUNK_SIZE - 1].setBottom(true);
                        bottom[x][y][Chunk.CHUNK_SIZE - 1] = true;
                    }

                    if (chunk.blocks[x][y][Chunk.CHUNK_SIZE - 2] == Type.AIR) {
                        //chunk.blocks[x][y][Chunk.CHUNK_SIZE - 1].setBack(true);
                        back[x][y][Chunk.CHUNK_SIZE - 1] = true;
                    }
                }
            }
        }
    }

    private void updateBackSide() {
        boolean isValid = backChunk != null;

        for (int x = 1; x < Chunk.CHUNK_SIZE - 1; x++) {
            for (int y = 1; y < Chunk.CHUNK_SIZE - 1; y++) {
                if (chunk.blocks[x][y][0] != Type.AIR) {
                    if (isValid) {
                        if (backChunk.blocks[x][y][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                            //chunk.blocks[x][y][0].setBack(true);
                            back[x][y][0] = true;
                        }
                    }

                    if (chunk.blocks[x + 1][y][0] == Type.AIR) {
                        //chunk.blocks[x][y][0].setRight(true);
                        right[x][y][0] = true;
                    }

                    if (chunk.blocks[x - 1][y][0] == Type.AIR) {
                        //chunk.blocks[x][y][0].setLeft(true);
                        left[x][y][0] = true;
                    }

                    if (chunk.blocks[x][y + 1][0] == Type.AIR) {
                        //chunk.blocks[x][y][0].setTop(true);
                        top[x][y][0] = true;
                    }

                    if (chunk.blocks[x][y - 1][0] == Type.AIR) {
                        //chunk.blocks[x][y][0].setBottom(true);
                        bottom[x][y][0] = true;
                    }

                    if (chunk.blocks[x][y][1] == Type.AIR) {
                        //chunk.blocks[x][y][0].setFront(true);
                        front[x][y][0] = true;
                    }
                }
            }
        }
    }

    private int calculateVertexCount() {
        int vertexCount = 0;
        for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                for (int y = 0; y < Chunk.CHUNK_SIZE; y++) {
                    if (left[x][y][z]) {
                        vertexCount += 6;
                    }
                    if (right[x][y][z]) {
                        vertexCount += 6;
                    }
                    if (top[x][y][z]) {
                        vertexCount += 6;
                    }
                    if (bottom[x][y][z]) {
                        vertexCount += 6;
                    }
                    if (front[x][y][z]) {
                        vertexCount += 6;
                    }
                    if (back[x][y][z]) {
                        vertexCount += 6;
                    }
                }
            }
        }

        return vertexCount;
    }

    private void updateLeftBack() {
        boolean backIsValid = backChunk != null;
        boolean leftIsValid = leftChunk != null;

        for (int y = 1; y < Chunk.CHUNK_SIZE - 1; y++) {
            if (chunk.blocks[0][y][0] != Type.AIR) {
                if (backIsValid) {
                    if (backChunk.blocks[0][y][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                        //chunk.blocks[0][y][0].setBack(true);
                        back[0][y][0] = true;
                    }
                }

                if (leftIsValid) {
                    if (leftChunk.blocks[Chunk.CHUNK_SIZE - 1][y][0] == Type.AIR) {
                        //chunk.blocks[0][y][0].setLeft(true);
                        left[0][y][0] = true;
                    }
                }

                if (chunk.blocks[1][y][0] == Type.AIR) {
                    //chunk.blocks[0][y][0].setRight(true);
                    right[0][y][0] = true;
                }

                if (chunk.blocks[0][y + 1][0] == Type.AIR) {
                    //chunk.blocks[0][y][0].setTop(true);
                    top[0][y][0] = true;
                }

                if (chunk.blocks[0][y - 1][0] == Type.AIR) {
                    //chunk.blocks[0][y][0].setBottom(true);
                    bottom[0][y][0] = true;
                }

                if (chunk.blocks[0][y][1] == Type.AIR) {
                    //chunk.blocks[0][y][0].setFront(true);
                    front[0][y][0] = true;
                }
            }
        }
    }

    private void updateLeftFront() {
        boolean frontIsValid = frontChunk != null;
        boolean leftIsValid = leftChunk != null;

        for (int y = 1; y < Chunk.CHUNK_SIZE - 1; y++) {
            if (chunk.blocks[0][y][Chunk.CHUNK_SIZE - 1] != Type.AIR) {
                if (frontIsValid) {
                    if (frontChunk.blocks[0][y][0] == Type.AIR) {
                        //chunk.blocks[0][y][Chunk.CHUNK_SIZE - 1].setFront(true);
                        front[0][y][Chunk.CHUNK_SIZE - 1] = true;
                    }
                }

                if (leftIsValid) {
                    if (leftChunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                        //chunk.blocks[0][y][Chunk.CHUNK_SIZE - 1].setLeft(true);
                        left[0][y][Chunk.CHUNK_SIZE - 1] = true;
                    }
                }

                if (chunk.blocks[1][y][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                    //chunk.blocks[0][y][Chunk.CHUNK_SIZE - 1].setRight(true);
                    right[0][y][Chunk.CHUNK_SIZE - 1] = true;
                }

                if (chunk.blocks[0][y + 1][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                    //chunk.blocks[0][y][Chunk.CHUNK_SIZE - 1].setTop(true);
                    top[0][y][Chunk.CHUNK_SIZE - 1] = true;
                }

                if (chunk.blocks[0][y - 1][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                    //chunk.blocks[0][y][Chunk.CHUNK_SIZE - 1].setBottom(true);
                    bottom[0][y][Chunk.CHUNK_SIZE - 1] = true;
                }

                if (chunk.blocks[0][y][Chunk.CHUNK_SIZE - 2] == Type.AIR) {
                    //chunk.blocks[0][y][Chunk.CHUNK_SIZE - 1].setBack(true);
                    back[0][y][Chunk.CHUNK_SIZE - 1] = true;
                }
            }
        }
    }

    private void updateRightBack() {
        boolean rightIsValid = rightChunk != null;
        boolean backIsValid = backChunk != null;

        for (int y = 1; y < Chunk.CHUNK_SIZE - 1; y++) {
            if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y][0] != Type.AIR) {
                if (rightIsValid) {
                    if (rightChunk.blocks[0][y][0] == Type.AIR) {
                        //chunk.blocks[Chunk.CHUNK_SIZE - 1][y][0].setRight(true);
                        right[Chunk.CHUNK_SIZE - 1][y][0] = true;
                    }
                }

                if (backIsValid) {
                    if (backChunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                        //chunk.blocks[Chunk.CHUNK_SIZE - 1][y][0].setBack(true);
                        back[Chunk.CHUNK_SIZE - 1][y][0] = true;
                    }
                }

                if (chunk.blocks[Chunk.CHUNK_SIZE - 2][y][0] == Type.AIR) {
                    //chunk.blocks[Chunk.CHUNK_SIZE - 1][y][0].setLeft(true);
                    left[Chunk.CHUNK_SIZE - 1][y][0] = true;
                }

                if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y + 1][0] == Type.AIR) {
                    //chunk.blocks[Chunk.CHUNK_SIZE - 1][y][0].setTop(true);
                    top[Chunk.CHUNK_SIZE - 1][y][0] = true;
                }
                if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y - 1][0] == Type.AIR) {
                    //chunk.blocks[Chunk.CHUNK_SIZE - 1][y][0].setBottom(true);
                    bottom[Chunk.CHUNK_SIZE - 1][y][0] = true;
                }

                if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y][1] == Type.AIR) {
                    //chunk.blocks[Chunk.CHUNK_SIZE - 1][y][0].setFront(true);
                    front[Chunk.CHUNK_SIZE - 1][y][0] = true;
                }
            }
        }
    }

    private void updateRightFront() {
        boolean rightIsValid = rightChunk != null;
        boolean frontIsValid = frontChunk != null;

        for (int y = 1; y < Chunk.CHUNK_SIZE - 1; y++) {
            if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1] != Type.AIR) {
                if (rightIsValid) {
                    if (rightChunk.blocks[0][y][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                        //chunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1].setRight(true);
                        right[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1] = true;
                    }
                }

                if (frontIsValid) {
                    if (frontChunk.blocks[Chunk.CHUNK_SIZE - 1][y][0] == Type.AIR) {
                        //chunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1].setFront(true);
                        front[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1] = true;
                    }
                }

                if (chunk.blocks[Chunk.CHUNK_SIZE - 2][y][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                    //chunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1].setLeft(true);
                    left[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1] = true;
                }

                if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y + 1][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                    //chunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1].setTop(true);
                    top[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1] = true;
                }

                if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y - 1][Chunk.CHUNK_SIZE - 1] == Type.AIR) {
                    //chunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1].setBottom(true);
                    bottom[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1] = true;
                }

                if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 2] == Type.AIR) {
                    //chunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1].setBack(true);
                    back[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1] = true;
                }

            }
        }
    }

    private void createVertexHash() {
        vertexList = new ArrayList<>();

        // top
        for (int y = 0; y < Chunk.CHUNK_SIZE; y++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
                    if (chunk.blocks[x][y][z] != Type.AIR) {
                        if (top[x][y][z]) {
                            int width = 0;
                            int rows = 0;
                            short current = chunk.blocks[x][y][z];
                            top[x][y][z] = false;
                            while (x + width + 1 < Chunk.CHUNK_SIZE && top[x + width + 1][y][z] && chunk.blocks[x + width + 1][y][z] == current) {
                                top[x + width + 1][y][z] = false;
                                width++;
                            }
                            boolean inLoop = true;
                            int zInc = 0;
                            while (inLoop) {
                                zInc++;
                                boolean addRow = true;
                                if (z + zInc < Chunk.CHUNK_SIZE) {
                                    for (int i = 0; i <= width; i++) {
                                        if (chunk.blocks[x + i][y][z + zInc] != current || !top[x + i][y][z + zInc]) {
                                            addRow = false;
                                            inLoop = false;
                                        }
                                    }
                                } else {
                                    addRow = false;
                                    inLoop = false;
                                }
                                if (addRow) {
                                    rows++;
                                    for (int i = 0; i <= width; i++) {
                                        //topReady[x + i][y][z + rows] = true;
                                        top[x + i][y][z + rows] = false;
                                    }

                                }
                            }

                            vertexList.add(new Vertex(x, y, z, x, y, z + rows, x + width, y, z + rows, x + width, y, z, current, Side.TOP));

                        }
                    }
                }
            }
        }

        //bottom
        for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                for (int y = 0; y < Chunk.CHUNK_SIZE; y++) {
                    if (chunk.blocks[x][y][z] != Type.AIR) {
                        if (bottom[x][y][z]) {
                            int width = 0;
                            int rows = 0;
                            short current = chunk.blocks[x][y][z];
                            bottom[x][y][z] = false;
                            while (x + width + 1 < Chunk.CHUNK_SIZE && bottom[x + width + 1][y][z] && chunk.blocks[x + width + 1][y][z] == current) {
                                bottom[x + width + 1][y][z] = false;
                                width++;
                            }
                            boolean inLoop = true;
                            int zInc = 0;
                            while (inLoop) {
                                zInc++;
                                boolean addRow = true;
                                if (z + zInc < Chunk.CHUNK_SIZE) {
                                    for (int i = 0; i <= width; i++) {
                                        if (chunk.blocks[x + i][y][z + zInc] != current || !bottom[x + i][y][z + zInc]) {
                                            addRow = false;
                                            inLoop = false;
                                        }
                                    }
                                } else {
                                    addRow = false;
                                    inLoop = false;
                                }
                                if (addRow) {
                                    rows++;
                                    for (int i = 0; i <= width; i++) {
                                        bottom[x + i][y][z + rows] = false;
                                    }

                                }
                            }

                            vertexList.add(new Vertex(x + width, y, z, x, y, z, x, y, z + rows, x + width, y, z + rows, current, Side.BOTTOM));
                        }
                    }
                }
            }
        }
        //right
        for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                for (int y = 0; y < Chunk.CHUNK_SIZE; y++) {
                    if (chunk.blocks[x][y][z] != Type.AIR) {
                        if (right[x][y][z]) {
                            int width = 0;
                            int rows = 0;
                            short current = chunk.blocks[x][y][z];
                            right[x][y][z] = false;
                            while (z + width + 1 < Chunk.CHUNK_SIZE && right[x][y][z + width + 1] && chunk.blocks[x][y][z + width + 1] == current) {
                                right[x][y][z + width + 1] = false;
                                width++;
                            }
                            boolean inLoop = true;
                            int yInc = 0;
                            while (inLoop) {
                                yInc++;
                                boolean addRow = true;
                                if (y + yInc < Chunk.CHUNK_SIZE) {
                                    for (int i = 0; i <= width; i++) {
                                        if (chunk.blocks[x][y + yInc][z + i] != current || !right[x][y + yInc][z + i]) {
                                            addRow = false;
                                            inLoop = false;
                                        }
                                    }
                                } else {
                                    addRow = false;
                                    inLoop = false;
                                }
                                if (addRow) {
                                    rows++;
                                    for (int i = 0; i <= width; i++) {
                                        right[x][y + rows][z + i] = false;
                                    }

                                }
                            }
                            vertexList.add(new Vertex(x, y + rows, z, x, y, z, x, y, z + width, x, y + rows, z + width, current, Side.RIGHT));
                        }
                    }
                }
            }
        }
        //left
        for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                for (int y = 0; y < Chunk.CHUNK_SIZE; y++) {
                    if (chunk.blocks[x][y][z] != Type.AIR) {
                        if (left[x][y][z]) {
                            int width = 0;
                            int rows = 0;
                            short current = chunk.blocks[x][y][z];
                            left[x][y][z] = false;
                            while (z + width + 1 < Chunk.CHUNK_SIZE && left[x][y][z + width + 1] && chunk.blocks[x][y][z + width + 1] == current) {
                                left[x][y][z + width + 1] = false;
                                width++;
                            }

                            boolean inLoop = true;
                            int yInc = 0;
                            while (inLoop) {
                                yInc++;
                                boolean addRow = true;
                                if (y + yInc < Chunk.CHUNK_SIZE) {
                                    for (int i = 0; i <= width; i++) {
                                        if (chunk.blocks[x][y + yInc][z + i] != current || !left[x][y + yInc][z + i]) {
                                            addRow = false;
                                            inLoop = false;
                                        }
                                    }
                                } else {
                                    addRow = false;
                                    inLoop = false;
                                }
                                if (addRow) {
                                    rows++;
                                    for (int i = 0; i <= width; i++) {
                                        left[x][y + rows][z + i] = false;
                                    }

                                }
                            }
                            vertexList.add(new Vertex(x, y + rows, z + width, x, y, z + width, x, y, z, x, y + rows, z, current, Side.LEFT));
                        }
                    }
                }
            }
        }
        //front
        for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                for (int y = 0; y < Chunk.CHUNK_SIZE; y++) {
                    if (chunk.blocks[x][y][z] != Type.AIR) {
                        if (front[x][y][z]) {
                            int width = 0;
                            int rows = 0;
                            short current = chunk.blocks[x][y][z];
                            front[x][y][z] = false;
                            while (x + width + 1 < Chunk.CHUNK_SIZE && front[x + width + 1][y][z] && chunk.blocks[x + width + 1][y][z] == current) {
                                front[x + width + 1][y][z] = false;
                                width++;
                            }

                            boolean inLoop = true;
                            int yInc = 0;
                            while (inLoop) {
                                yInc++;
                                boolean addRow = true;
                                if (y + yInc < Chunk.CHUNK_SIZE) {
                                    for (int i = 0; i <= width; i++) {
                                        if (chunk.blocks[x + i][y + yInc][z] != current || !front[x + i][y + yInc][z]) {
                                            addRow = false;
                                            inLoop = false;
                                        }
                                    }
                                } else {
                                    addRow = false;
                                    inLoop = false;
                                }
                                if (addRow) {
                                    rows++;
                                    for (int i = 0; i <= width; i++) {
                                        front[x + i][y + rows][z] = false;
                                    }

                                }
                            }
                            vertexList.add(new Vertex(x, y + rows, z, x, y, z, x + width, y, z, x + width, y + rows, z, current, Side.FRONT));
                        }
                    }
                }
            }
        }
        //back
        for (int x = 0; x < Chunk.CHUNK_SIZE; x++) {
            for (int z = 0; z < Chunk.CHUNK_SIZE; z++) {
                for (int y = 0; y < Chunk.CHUNK_SIZE; y++) {
                    if (chunk.blocks[x][y][z] != Type.AIR) {
                        if (back[x][y][z]) {
                            int width = 0;
                            int rows = 0;
                            short current = chunk.blocks[x][y][z];
                            back[x][y][z] = false;
                            while (x + width + 1 < Chunk.CHUNK_SIZE && back[x + width + 1][y][z] && chunk.blocks[x + width + 1][y][z] == current) {
                                back[x + width + 1][y][z] = false;
                                width++;
                            }
                            boolean inLoop = true;
                            int yInc = 0;
                            while (inLoop) {
                                yInc++;
                                boolean addRow = true;
                                if (y + yInc < Chunk.CHUNK_SIZE) {
                                    for (int i = 0; i <= width; i++) {
                                        if (chunk.blocks[x + i][y + yInc][z] != current || !back[x + i][y + yInc][z]) {
                                            addRow = false;
                                            inLoop = false;
                                        }
                                    }
                                } else {
                                    addRow = false;
                                    inLoop = false;
                                }
                                if (addRow) {
                                    rows++;
                                    for (int i = 0; i <= width; i++) {
                                        back[x + i][y + rows][z] = false;
                                    }

                                }
                            }
                            vertexList.add(new Vertex(x + width, y + rows, z, x + width, y, z, x, y, z, x, y + rows, z, current, Side.BACK));
                        }
                    }
                }
            }
        }
    }

    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
    }

    public void drawChunkVBO() {

        createVertexHash();

        int vertices = vertexList.size() * 4;
        chunk.setVertices(vertices);

        vertexArray = new float[vertices * vertexSize];
        normalArray = new float[vertices * normalSize];
        texArray = new float[vertices * texSize];

        int vArrayPos = 0;
        int nArrayPos = 0;
        int tArrayPos = 0;

        vertexData = BufferUtils.createFloatBuffer(vertices * vertexSize);
        normalData = BufferUtils.createFloatBuffer(vertices * vertexSize);
        texData = BufferUtils.createFloatBuffer(vertices * texSize);
        float frontXOff;
        float frontYOff;
        float backXOff;
        float backYOff;
        float rightXOff;
        float rightYOff;
        float leftXOff;
        float leftYOff;
        float topXOff;
        float topYOff;
        float bottomXOff;
        float bottomYOff;
        float tSize = 1f / 8f;

        for (Vertex v : vertexList) {
            short type = v.type;

            frontXOff = AtlasManager.getFrontXOff(type);
            frontYOff = AtlasManager.getFrontYOff(type);

            backXOff = AtlasManager.getBackXOff(type);
            backYOff = AtlasManager.getBackYOff(type);

            rightXOff = AtlasManager.getRightXOff(type);
            rightYOff = AtlasManager.getRightYOff(type);

            leftXOff = AtlasManager.getLeftXOff(type);
            leftYOff = AtlasManager.getLeftYOff(type);

            if (type != Type.DIRT) {
                topXOff = AtlasManager.getTopXOff(type);
                topYOff = AtlasManager.getTopYOff(type);
            } else {
                topXOff = AtlasManager.getTopXOff(Type.GRASS);
                topYOff = AtlasManager.getTopYOff(Type.GRASS);
            }

            bottomXOff = AtlasManager.getBottomXOff(type);
            bottomYOff = AtlasManager.getBottomYOff(type);

            if (v.side == Side.FRONT) {
                // 1st
                // upper left - +
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = 1;
                nArrayPos++;

                vertexArray[vArrayPos] = 0 + v.topLeftX + xOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 1 + v.topLeftY + yOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 1 + v.topLeftZ + zOff;
                vArrayPos++;

                texArray[tArrayPos] = frontXOff;
                tArrayPos++;
                texArray[tArrayPos] = frontYOff;
                tArrayPos++;

                // lower left - -
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = 1;
                nArrayPos++;

                vertexArray[vArrayPos] = 0 + v.bottomLeftX + xOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 0 + v.bottomLeftY + yOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 1 + v.bottomLeftZ + zOff;
                vArrayPos++;

                texArray[tArrayPos] = frontXOff;
                tArrayPos++;
                texArray[tArrayPos] = frontYOff + tSize;
                tArrayPos++;

                // lower right + -
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = 1;
                nArrayPos++;

                vertexArray[vArrayPos] = 1 + v.bottomRightX + xOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 0 + v.bottomRightY + yOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 1 + v.bottomRightZ + zOff;
                vArrayPos++;

                texArray[tArrayPos] = frontXOff + tSize;
                tArrayPos++;
                texArray[tArrayPos] = frontYOff + tSize;
                tArrayPos++;

                // upper right + +
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = 1;
                nArrayPos++;

                vertexArray[vArrayPos] = 1 + v.topRightX + xOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 1 + v.topRightY + yOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 1 + v.topRightZ + zOff;
                vArrayPos++;

                texArray[tArrayPos] = frontXOff + tSize;
                tArrayPos++;
                texArray[tArrayPos] = frontYOff;
                tArrayPos++;

            }
            if (v.side == Side.BACK) {

                texArray[tArrayPos] = backXOff;
                tArrayPos++;
                texArray[tArrayPos] = backYOff + tSize;
                tArrayPos++;

                texArray[tArrayPos] = backXOff + tSize;
                tArrayPos++;
                texArray[tArrayPos] = backYOff + tSize;
                tArrayPos++;

                texArray[tArrayPos] = backXOff + tSize;
                tArrayPos++;
                texArray[tArrayPos] = backYOff;
                tArrayPos++;

                //2nd
                texArray[tArrayPos] = backXOff;
                tArrayPos++;
                texArray[tArrayPos] = backYOff;
                tArrayPos++;

                // 1st
                // upper left + -
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = -1;
                nArrayPos++;

                vertexArray[vArrayPos] = 1 + v.topLeftX + xOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 1 + v.topLeftY + yOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 0 + v.topLeftZ + zOff;
                vArrayPos++;
                // lower left + -
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = -1;
                nArrayPos++;

                vertexArray[vArrayPos] = 1 + v.bottomLeftX + xOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 0 + v.bottomLeftY + yOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 0 + v.bottomLeftZ + zOff;
                vArrayPos++;

                // lower right - -
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = -1;
                nArrayPos++;

                vertexArray[vArrayPos] = 0 + v.bottomRightX + xOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 0 + v.bottomRightY + yOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 0 + v.bottomRightZ + zOff;
                vArrayPos++;

                // upper right - +
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = -1;
                nArrayPos++;

                vertexArray[vArrayPos] = 0 + v.topRightX + xOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 1 + v.topRightY + yOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 0 + v.topRightZ + zOff;
                vArrayPos++;

            }
            if (v.side == Side.RIGHT) {
                texArray[tArrayPos] = rightXOff;
                tArrayPos++;
                texArray[tArrayPos] = rightYOff;
                tArrayPos++;

                texArray[tArrayPos] = rightXOff;
                tArrayPos++;
                texArray[tArrayPos] = rightYOff + tSize;
                tArrayPos++;

                texArray[tArrayPos] = rightXOff + tSize;
                tArrayPos++;
                texArray[tArrayPos] = rightYOff + tSize;
                tArrayPos++;

                texArray[tArrayPos] = rightXOff + tSize;
                tArrayPos++;
                texArray[tArrayPos] = rightYOff;
                tArrayPos++;

                // 1st
                // upper right + +
                normalArray[nArrayPos] = 1;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;

                vertexArray[vArrayPos] = 1 + v.topRightX + xOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 1 + v.topRightY + yOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 1 + v.bottomRightZ + zOff;
                vArrayPos++;

                // lower right - +
                normalArray[nArrayPos] = 1;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;

                vertexArray[vArrayPos] = 1 + v.bottomRightX + xOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 0 + v.bottomRightY + yOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 1 + v.bottomRightZ + zOff;
                vArrayPos++;

                // lower left - -
                normalArray[nArrayPos] = 1;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;

                vertexArray[vArrayPos] = 1 + v.bottomLeftX + xOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 0 + v.bottomLeftY + yOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 0 + v.bottomLeftZ + zOff;
                vArrayPos++;

                // upper left + -
                normalArray[nArrayPos] = 1;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;

                vertexArray[vArrayPos] = 1 + v.topLeftX + xOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 1 + v.topLeftY + yOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 0 + v.topLeftZ + zOff;
                vArrayPos++;
            }
            if (v.side == Side.LEFT) {
                texArray[tArrayPos] = leftXOff;
                tArrayPos++;
                texArray[tArrayPos] = leftYOff;
                tArrayPos++;

                texArray[tArrayPos] = leftXOff;
                tArrayPos++;
                texArray[tArrayPos] = leftYOff + tSize;
                tArrayPos++;

                texArray[tArrayPos] = leftXOff + tSize;
                tArrayPos++;
                texArray[tArrayPos] = leftYOff + tSize;
                tArrayPos++;

                texArray[tArrayPos] = leftXOff + tSize;
                tArrayPos++;
                texArray[tArrayPos] = leftYOff;
                tArrayPos++;

                // upper right + -
                normalArray[nArrayPos] = -1;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;

                vertexArray[vArrayPos] = 0 + v.topRightX + xOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 1 + v.topRightY + yOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 0 + v.topRightZ + zOff;
                vArrayPos++;

                // lower right - -
                normalArray[nArrayPos] = -1;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;

                vertexArray[vArrayPos] = 0 + v.bottomRightX + xOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 0 + v.bottomRightY + yOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 0 + v.bottomRightZ + zOff;
                vArrayPos++;

                // lower left - +
                normalArray[nArrayPos] = -1;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;

                vertexArray[vArrayPos] = 0 + v.bottomLeftX + xOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 0 + v.bottomLeftY + yOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 1 + v.bottomLeftZ + zOff;
                vArrayPos++;

                // upper left + +
                normalArray[nArrayPos] = -1;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;

                vertexArray[vArrayPos] = 0 + v.topLeftX + xOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 1 + v.topLeftY + yOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 1 + v.bottomLeftZ + zOff;
                vArrayPos++;
            }
            if (v.side == Side.TOP) {
                // 1st
                // upper left
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = 1;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;

                vertexArray[vArrayPos] = 0 + v.topLeftX + xOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 1 + v.topLeftY + yOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 0 + v.topLeftZ + zOff;
                vArrayPos++;

                texArray[tArrayPos] = topXOff;
                tArrayPos++;
                texArray[tArrayPos] = topYOff;
                tArrayPos++;

                // lower left
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = 1;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;

                vertexArray[vArrayPos] = 0 + v.bottomLeftX + xOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 1 + v.bottomLeftY + yOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 1 + v.bottomLeftZ + zOff;
                vArrayPos++;

                texArray[tArrayPos] = topXOff;
                tArrayPos++;
                texArray[tArrayPos] = topYOff + tSize;
                tArrayPos++;

                // lower right
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = 1;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;

                vertexArray[vArrayPos] = 1 + v.bottomRightX + xOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 1 + v.bottomRightY + yOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 1 + v.bottomRightZ + zOff;
                vArrayPos++;

                texArray[tArrayPos] = topXOff + tSize;
                tArrayPos++;
                texArray[tArrayPos] = topYOff + tSize;
                tArrayPos++;

                // upper right
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = 1;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;

                vertexArray[vArrayPos] = 1 + v.topRightX + xOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 1 + v.topRightY + yOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 0 + v.topRightZ + zOff;
                vArrayPos++;

                texArray[tArrayPos] = topXOff + tSize;
                tArrayPos++;
                texArray[tArrayPos] = topYOff;
                tArrayPos++;

            }
            if (v.side == Side.BOTTOM) {
                texArray[tArrayPos] = bottomXOff;
                tArrayPos++;
                texArray[tArrayPos] = bottomYOff;
                tArrayPos++;

                texArray[tArrayPos] = bottomXOff + tSize;
                tArrayPos++;
                texArray[tArrayPos] = bottomYOff;
                tArrayPos++;

                texArray[tArrayPos] = bottomXOff + tSize;
                tArrayPos++;
                texArray[tArrayPos] = bottomYOff + tSize;
                tArrayPos++;

                texArray[tArrayPos] = bottomXOff;
                tArrayPos++;
                texArray[tArrayPos] = bottomYOff + tSize;
                tArrayPos++;
//
//                // 2nd
//                texArray[tArrayPos] = bottomXOff;
//                tArrayPos++;
//                texArray[tArrayPos] = bottomYOff;
//                tArrayPos++;
//

//                texArray[tArrayPos] = bottomXOff + tSize;
//                tArrayPos++;
//                texArray[tArrayPos] = bottomYOff + tSize;
//                tArrayPos++;
                // upper right + +
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = -1;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;

                vertexArray[vArrayPos] = 1 + v.topRightX + xOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 0 + v.topRightY + yOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 1 + v.topRightZ + zOff;
                vArrayPos++;

                // lower right - +
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = -1;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;

                vertexArray[vArrayPos] = 0 + v.bottomRightX + xOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 0 + v.bottomRightY + yOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 1 + v.bottomRightZ + zOff;
                vArrayPos++;

                // lower left - -
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = -1;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;

                vertexArray[vArrayPos] = 0 + v.bottomLeftX + xOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 0 + v.bottomLeftY + yOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 0 + v.bottomLeftZ + zOff;
                vArrayPos++;

                // 1st
                // upper left + -
                normalArray[nArrayPos] = 0;
                nArrayPos++;
                normalArray[nArrayPos] = -1;
                nArrayPos++;
                normalArray[nArrayPos] = 0;
                nArrayPos++;

                vertexArray[vArrayPos] = 1 + v.topLeftX + xOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 0 + v.topLeftY + yOff;
                vArrayPos++;
                vertexArray[vArrayPos] = 0 + v.topLeftZ + zOff;
                vArrayPos++;

            }

        }
        vertexData.put(vertexArray);
        vertexData.flip();

        normalData.put(normalArray);
        normalData.flip();

        texData.put(texArray);
        texData.flip();

    }
}
