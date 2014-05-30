package voxels.ChunkManager;

import com.ning.compress.lzf.LZFEncoder;
import de.ruedigermoeller.serialization.FSTObjectOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.BufferUtils;
import static voxels.Voxels.WaterOffs;

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
    private int chunkX;
    private int chunkZ;
    private int xOff;
    private int zOff;
    private ConcurrentHashMap<Integer, byte[]> map;
    private boolean ready = false;

    private Chunk rightChunk;
    private Chunk leftChunk;
    private Chunk frontChunk;
    private Chunk backChunk;

    private ChunkManager chunkManager;
    private ArrayList<Data> dataToProcess;

    private Data updateData;

    boolean update;

    public ChunkMaker(ArrayList<Data> dataToProcess, int chunkX, int chunkZ, int xOff, int zOff, ConcurrentHashMap<Integer, byte[]> map, ChunkManager chunkManager) {
        this.xOff = xOff;
        this.zOff = zOff;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.map = map;
        this.dataToProcess = dataToProcess;
        this.chunkManager = chunkManager;

        update = false;

    }

    public ChunkMaker(ConcurrentHashMap<Integer, byte[]> map, ChunkManager chunkManager, ArrayList<Data> dataToProcess) {
        this.map = map;
        this.chunkManager = chunkManager;
        this.dataToProcess = dataToProcess;
    }

    @Override
    public void run() {
        if (!map.containsKey(new Pair(chunkX, chunkZ).hashCode())) {
            chunk = new Chunk(chunkX, chunkZ);
            updateAllBlocks();
            drawChunkVBO();

            map.put(new Pair(chunkX, chunkZ).hashCode(), toByte(chunk));
            dataToProcess.add(new Data(chunkX, chunkZ, chunk.getVertices(), vertexData, normalData, texData, false));
            updateSidesWithCheck(chunkManager.getChunk(chunkX + 1, chunkZ));
            updateSidesWithCheck(chunkManager.getChunk(chunkX - 1, chunkZ));
            updateSidesWithCheck(chunkManager.getChunk(chunkX, chunkZ + 1));
            updateSidesWithCheck(chunkManager.getChunk(chunkX, chunkZ - 1));

        }
        else
            System.out.println("Already contains");
    }

    public void updateSidesWithCheck(final Chunk chunk) {
        if (chunk != null) {
            // if all 4 surrounding chunks exist
//            new Thread(new Runnable() {
//                public void run() {
            if (map.containsKey(new Pair(chunk.xId + 1, chunk.zId).hashCode()) && map.containsKey(new Pair(chunk.xId - 1, chunk.zId).hashCode()) && map.containsKey(new Pair(chunk.xId, chunk.zId + 1).hashCode()) && map.containsKey(new Pair(chunk.xId, chunk.zId - 1).hashCode())) {
                updateSides(chunk);
//                    }
//                }
//            }).start();
            }
        }
    }

    public void update(Chunk chunk) {
        if (chunk != null) {
            this.chunk = chunk;
            this.xOff = chunk.xCoordinate;
            this.zOff = chunk.zCoordinate;
            updateAllBlocks();
            drawChunkVBO();
            map.put(new Pair(this.chunk.xId, this.chunk.zId).hashCode(), toByte(this.chunk));
            dataToProcess.add(new Data(this.chunk.xId, this.chunk.zId, this.chunk.getVertices(), vertexData, normalData, texData, true));
        }
    }

    public void updateLeft(final Chunk chunk) {
        if (chunk != null) {
            this.chunk = chunk;
            this.xOff = chunk.xCoordinate;
            this.zOff = chunk.zCoordinate;
            updateLeft();
            drawChunkVBO();
            new Thread(new Runnable() {
                public void run() {
                    map.put(new Pair(chunk.xId, chunk.zId).hashCode(), toByte(chunk));
                }
            }).start();

            dataToProcess.add(new Data(this.chunk.xId, this.chunk.zId, this.chunk.getVertices(), vertexData, normalData, texData, true));
        }
    }

    public void updateSides(final Chunk chunk) {
        if (chunk != null) {
            this.chunk = chunk;
            this.xOff = chunk.xCoordinate;
            this.zOff = chunk.zCoordinate;
            updateRight();
            updateLeft();
            updateBack();
            updateFront();
            drawChunkVBO();
            map.put(new Pair(chunk.xId, chunk.zId).hashCode(), toByte(chunk));
            dataToProcess.add(new Data(this.chunk.xId, this.chunk.zId, this.chunk.getVertices(), vertexData, normalData, texData, true));
        }
    }

    public void updateRight(final Chunk chunk) {
        if (chunk != null) {
            this.chunk = chunk;
            this.xOff = chunk.xCoordinate;
            this.zOff = chunk.zCoordinate;
            updateRight();
            drawChunkVBO();

            new Thread(new Runnable() {
                public void run() {
                    map.put(new Pair(chunk.xId, chunk.zId).hashCode(), toByte(chunk));
                }
            }).start();
            dataToProcess.add(new Data(this.chunk.xId, this.chunk.zId, this.chunk.getVertices(), vertexData, normalData, texData, true));
        }
    }

    public void updateBack(final Chunk chunk) {
        if (chunk != null) {
            this.chunk = chunk;
            this.xOff = chunk.xCoordinate;
            this.zOff = chunk.zCoordinate;
            updateBack();
            drawChunkVBO();
            new Thread(new Runnable() {
                public void run() {
                    map.put(new Pair(chunk.xId, chunk.zId).hashCode(), toByte(chunk));
                }
            }).start();

            dataToProcess.add(new Data(this.chunk.xId, this.chunk.zId, this.chunk.getVertices(), vertexData, normalData, texData, true));
        }
    }

    public void updateFront(final Chunk chunk) {
        if (chunk != null) {
            this.chunk = chunk;
            this.xOff = chunk.xCoordinate;
            this.zOff = chunk.zCoordinate;
            updateFront();
            drawChunkVBO();
            new Thread(new Runnable() {
                public void run() {
                    map.put(new Pair(chunk.xId, chunk.zId).hashCode(), toByte(chunk));
                }
            }).start();
            dataToProcess.add(new Data(this.chunk.xId, this.chunk.zId, this.chunk.getVertices(), vertexData, normalData, texData, true));
        }
    }

    public void drawChunkVBO() {

        int vertices = calculateVertexCount();
        chunk.setVertices(vertices);

        vertexArray = new float[vertices * vertexSize];
        normalArray = new float[vertices * normalSize];
        texArray = new float[vertices * texSize];
        colorArray = new float[vertices * colorSize];

        int vArrayPos = 0;
        int nArrayPos = 0;
        int tArrayPos = 0;

        vertexData = BufferUtils.createFloatBuffer(vertices * vertexSize);
        normalData = BufferUtils.createFloatBuffer(vertices * vertexSize);
        texData = BufferUtils.createFloatBuffer(vertices * texSize);
        colorData = BufferUtils.createFloatBuffer(vertices * colorSize);

        for (int x = 0; x < chunk.blocks.length; x++) {
            for (int z = 0; z < chunk.blocks[x][0].length; z++) {
                for (int y = 0; y < chunk.blocks[x].length; y++) {
                    if (chunk.blocks[x][y][z].is(Type.DIRT)) {
                        if (chunk.blocks[x][y][z].isFront()) {
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
                            vertexArray[vArrayPos] = 1 + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
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
                            vertexArray[vArrayPos] = 0 + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
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
                            vertexArray[vArrayPos] = 0 + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
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
                            vertexArray[vArrayPos] = 1 + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
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
                            vertexArray[vArrayPos] = 0 + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
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
                            vertexArray[vArrayPos] = 1 + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                        }
                        if (chunk.blocks[x][y][z].isBack()) {
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            //2nd
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
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
                            vertexArray[vArrayPos] = 1 + y;
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
                            vertexArray[vArrayPos] = 0 + y;
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
                            vertexArray[vArrayPos] = 0 + y;
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
                            vertexArray[vArrayPos] = 1 + y;
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
                            vertexArray[vArrayPos] = 0 + y;
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
                            vertexArray[vArrayPos] = 1 + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;

                        }
                        if (chunk.blocks[x][y][z].isRight()) {

                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            // 2nd
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
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
                            vertexArray[vArrayPos] = 1 + y;
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
                            vertexArray[vArrayPos] = 0 + y;
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
                            vertexArray[vArrayPos] = 0 + y;
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
                            vertexArray[vArrayPos] = 1 + y;
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
                            vertexArray[vArrayPos] = 0 + y;
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
                            vertexArray[vArrayPos] = 1 + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;

                        }
                        if (chunk.blocks[x][y][z].isLeft()) {
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            // 2nd
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
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
                            vertexArray[vArrayPos] = 1 + y;
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
                            vertexArray[vArrayPos] = 0 + y;
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
                            vertexArray[vArrayPos] = 0 + y;
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
                            vertexArray[vArrayPos] = 1 + y;
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
                            vertexArray[vArrayPos] = 0 + y;
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
                            vertexArray[vArrayPos] = 1 + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;
                        }
                        if (chunk.blocks[x][y][z].isTop()) {

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
                            vertexArray[vArrayPos] = 1 + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 0f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0f;
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
                            vertexArray[vArrayPos] = 1 + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 0f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1f / 2f;
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
                            vertexArray[vArrayPos] = 1 + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 1f / 2f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1f / 2f;
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
                            vertexArray[vArrayPos] = 1 + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 0f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0f;
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
                            vertexArray[vArrayPos] = 1 + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 1f / 2f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1f / 2f;
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
                            vertexArray[vArrayPos] = 1 + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 1f / 2f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0f;
                            tArrayPos++;
                        }
                        if (chunk.blocks[x][y][z].isBottom()) {
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            // 2nd
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
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
                            vertexArray[vArrayPos] = 0 + y;
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
                            vertexArray[vArrayPos] = 0 + y;
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
                            vertexArray[vArrayPos] = 0 + y;
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
                            vertexArray[vArrayPos] = 0 + y;
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
                            vertexArray[vArrayPos] = 0 + y;
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
                            vertexArray[vArrayPos] = 0 + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                        }
                    }
                    if (chunk.blocks[x][y][z].is(Type.WATER)) {
                        if (chunk.blocks[x][y][z].isTop()) {

                            // upper left
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = 0 + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + y - WaterOffs;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 0f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0.5f;
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
                            vertexArray[vArrayPos] = 1 + y - WaterOffs;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 0f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1f;
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
                            vertexArray[vArrayPos] = 1 + y - WaterOffs;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 1f / 2f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1f;
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
                            vertexArray[vArrayPos] = 1 + y - WaterOffs;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 0f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0.5f;
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
                            vertexArray[vArrayPos] = 1 + y - WaterOffs;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 1 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 1f / 2f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1f;
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
                            vertexArray[vArrayPos] = 1 + y - WaterOffs;
                            vArrayPos++;
                            vertexArray[vArrayPos] = 0 + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 1f / 2f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0.5f;
                            tArrayPos++;
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
        return LZFEncoder.encode(serialize(chunk));
    }

    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FSTObjectOutput os;
        try {
            os = new FSTObjectOutput(out);
            os.writeObject(obj);
        } catch (IOException ex) {
            Logger.getLogger(ChunkMaker.class.getName()).log(Level.SEVERE, null, ex);
        }
        return out.toByteArray();
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

    private void updateAllBlocks() {
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

        rightChunk = null;
        leftChunk = null;
        backChunk = null;
        frontChunk = null;
    }

    private void getAdjacentChunks() {
        rightChunk = chunkManager.getChunk(chunk.xId + 1, chunk.zId);
        leftChunk = chunkManager.getChunk(chunk.xId - 1, chunk.zId);
        backChunk = chunkManager.getChunk(chunk.xId, chunk.zId - 1);
        frontChunk = chunkManager.getChunk(chunk.xId, chunk.zId + 1);
    }

    private void updateBack() {
        backChunk = chunkManager.getChunk(chunk.xId, chunk.zId - 1);
        updateTopLeftBack();
        updateTopRightBack();
        updateBottomLeftBack();
        updateBottomRightBack();
        updateTopBack();
        updateBottomBack();
        updateLeftBack();
        updateRightBack();
        updateBackSide();
        backChunk = null;

    }

    private void updateFront() {
        frontChunk = chunkManager.getChunk(chunk.xId, chunk.zId + 1);
        updateTopLeftFront();
        updateTopRightFront();
        updateBottomLeftFront();
        updateBottomRightFront();
        updateTopFront();
        updateBottomFront();
        updateLeftFront();
        updateRightFront();
        updateFrontSide();
        frontChunk = null;
    }

    private void updateLeft() {
        leftChunk = chunkManager.getChunk(chunk.xId - 1, chunk.zId);
        updateTopLeftBack();
        updateTopLeftFront();
        updateBottomLeftBack();
        updateBottomLeftFront();
        updateTopLeft();
        updateBottomLeft();
        updateLeftBack();
        updateLeftFront();
        updateLeftSide();
        leftChunk = null;
    }

    private void updateRight() {
        rightChunk = chunkManager.getChunk(chunk.xId + 1, chunk.zId);
        updateTopRightBack();
        updateTopRightFront();
        updateBottomRightBack();
        updateBottomRightFront();
        updateTopRight();
        updateBottomRight();
        updateRightBack();
        updateRightFront();
        updateRightSide();
        rightChunk = null;
    }

    private void updateMiddle() {
        for (int x = 1; x < chunk.blocks.length - 1; x++) {
            for (int y = 1; y < chunk.blocks[x].length - 1; y++) {
                for (int z = 1; z < chunk.blocks[x][y].length - 1; z++) {

                    if (chunk.blocks[x][y][z].is(Type.DIRT)) {

                        // set active sides to be rendered, rendered if the side is not touching dirt
                        if (chunk.blocks[x + 1][y][z].isOpaque()) {
                            chunk.blocks[x][y][z].setRight(true);
                        }
                        if (chunk.blocks[x - 1][y][z].isOpaque()) {
                            chunk.blocks[x][y][z].setLeft(true);

                        }
                        if (chunk.blocks[x][y + 1][z].isOpaque()) {
                            chunk.blocks[x][y][z].setTop(true);

                        }
                        if (chunk.blocks[x][y - 1][z].isOpaque()) {
                            chunk.blocks[x][y][z].setBottom(true);

                        }
                        if (chunk.blocks[x][y][z + 1].isOpaque()) {
                            chunk.blocks[x][y][z].setFront(true);

                        }
                        if (chunk.blocks[x][y][z - 1].isOpaque()) {
                            chunk.blocks[x][y][z].setBack(true);

                        }
                    }
                    else if (chunk.blocks[x][y][z].is(Type.WATER))
                        // if water, if the block above it is not water, make it active
                        if (chunk.blocks[x][y + 1][z].is(Type.WATER) == false) {
                            chunk.blocks[x][y][z].setTop(true);
                        }
                }
            }
        }
    }

    private void updateTopLeftBack() {
        if (leftChunk != null) {
            if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
                chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][0].setLeft(true);
        }

        if (backChunk != null) {
            if (backChunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
                chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][0].setBack(true);
        }

        chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][0].setTop(true);

        // add code for top chunk check when chunks are made smaller
        if (chunk.blocks[1][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
            chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][0].setRight(true);

        if (chunk.blocks[0][Chunk.CHUNK_HEIGHT - 2][0].isOpaque())
            chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][0].setBottom(true);

        if (chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][1].isOpaque())
            chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][0].setFront(true);

    }

    private void updateTopLeftFront() {
        if (leftChunk != null) {
            if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
                chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setLeft(true);
        }
        if (frontChunk != null) {
            if (frontChunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
                chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setFront(true);
        }

        chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setTop(true);

        // add code for top chunk check when chunks are made smaller
        if (chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 2].isOpaque())
            chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setBack(true);

        if (chunk.blocks[0][Chunk.CHUNK_HEIGHT - 2][Chunk.CHUNK_WIDTH - 1].isOpaque())
            chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setBottom(true);

        if (chunk.blocks[1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
            chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setRight(true);
    }

    private void updateTopRightBack() {
        if (backChunk != null) {
            if (backChunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
                chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][0].setBack(true);
        }
        if (rightChunk != null) {
            if (rightChunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
                chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][0].setRight(true);
        }

        chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][0].setTop(true);

        if (chunk.blocks[Chunk.CHUNK_WIDTH - 2][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
            chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][0].setLeft(true);

        if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 2][0].isOpaque())
            chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][0].setBottom(true);

        if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][1].isOpaque())
            chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][0].setFront(true);
    }

    private void updateTopRightFront() {
        if (rightChunk != null) {
            if (rightChunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
                chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setRight(true);
        }
        if (frontChunk != null) {
            if (frontChunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
                chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setFront(true);
        }

        chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setTop(true);

        //chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setTop(true);
        if (chunk.blocks[Chunk.CHUNK_WIDTH - 2][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
            chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setLeft(true);

        if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 2][Chunk.CHUNK_WIDTH - 1].isOpaque())
            chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setBottom(true);

        if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 2].isOpaque())
            chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setBack(true);
    }

    private void updateTopLeft() {
        boolean isValid = leftChunk != null;

        for (int z = 1; z < Chunk.CHUNK_WIDTH - 1; z++) {
            if (isValid) {
                if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][z].isOpaque())
                    chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][z].setLeft(true);
            }

            chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][z].setTop(true);

            if (chunk.blocks[1][Chunk.CHUNK_HEIGHT - 1][z].isOpaque())
                chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][z].setRight(true);

            if (chunk.blocks[0][Chunk.CHUNK_HEIGHT - 2][z].isOpaque())
                chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][z].setBottom(true);

            if (chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][z + 1].isOpaque())
                chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][z].setFront(true);

            if (chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][z - 1].isOpaque())
                chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][z].setBack(true);
        }
    }

    private void updateTopRight() {
        boolean isValid = rightChunk != null;

        for (int z = 1; z < Chunk.CHUNK_WIDTH - 1; z++) {
            if (isValid) {
                if (rightChunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][z].isOpaque())
                    chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][z].setRight(true);
            }

            chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][z].setTop(true);

            if (chunk.blocks[Chunk.CHUNK_WIDTH - 2][Chunk.CHUNK_HEIGHT - 1][z].isOpaque())
                chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][z].setLeft(true);

            if (chunk.blocks[0][Chunk.CHUNK_HEIGHT - 2][z].isOpaque())
                chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][z].setBottom(true);

            if (chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][z + 1].isOpaque())
                chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][z].setFront(true);

            if (chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][z - 1].isOpaque())
                chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][z].setBack(true);
        }
    }

    private void updateTopFront() {
        boolean isValid = frontChunk != null;

        for (int x = 1; x < Chunk.CHUNK_WIDTH - 1; x++) {
            if (isValid) {
                if (frontChunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
                    chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setFront(true);
            }

            chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setTop(true);

            if (chunk.blocks[x + 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
                chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setRight(true);

            if (chunk.blocks[x - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
                chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setLeft(true);

            if (chunk.blocks[x][Chunk.CHUNK_HEIGHT - 2][Chunk.CHUNK_WIDTH - 1].isOpaque())
                chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setBottom(true);

            if (chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 2].isOpaque())
                chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setBack(true);
        }
    }

    private void updateTopBack() {
        boolean isValid = backChunk != null;

        for (int x = 1; x < Chunk.CHUNK_WIDTH - 1; x++) {
            if (isValid) {
                if (backChunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
                    chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][0].setBack(true);
            }

            chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][0].setTop(true);

            if (chunk.blocks[x + 1][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
                chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][0].setRight(true);

            if (chunk.blocks[x - 1][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
                chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][0].setLeft(true);

            if (chunk.blocks[x][Chunk.CHUNK_HEIGHT - 2][0].isOpaque())
                chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][0].setBottom(true);

            if (chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][1].isOpaque())
                chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][0].setFront(true);
        }

    }

    private void updateTopSide() {
        for (int x = 1; x < Chunk.CHUNK_WIDTH - 1; x++) {
            for (int z = 1; z < Chunk.CHUNK_WIDTH - 1; z++) {
                if (chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][z].is(Type.AIR) == false) {
                    chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][z].setTop(true);

                    if (chunk.blocks[x + 1][Chunk.CHUNK_HEIGHT - 1][z].isOpaque())
                        chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][z].setRight(true);

                    if (chunk.blocks[x - 1][Chunk.CHUNK_HEIGHT - 1][z].isOpaque())
                        chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][z].setLeft(true);

                    if (chunk.blocks[x][Chunk.CHUNK_HEIGHT - 2][z].isOpaque())
                        chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][z].setBottom(true);

                    if (chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][z + 1].isOpaque())
                        chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][z].setFront(true);

                    if (chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][z - 1].isOpaque())
                        chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][z].setBack(true);
                }
            }

        }
    }

    private void updateBottomLeftBack() {
        if (leftChunk != null)
            if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][0][0].isOpaque())
                chunk.blocks[0][0][0].setLeft(true);

        if (backChunk != null)
            if (backChunk.blocks[0][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
                chunk.blocks[0][0][0].setBack(true);

        if (chunk.blocks[1][0][0].isOpaque())
            chunk.blocks[0][0][0].setRight(true);

        if (chunk.blocks[0][1][0].isOpaque())
            chunk.blocks[0][0][0].setTop(true);

        if (chunk.blocks[0][0][1].isOpaque())
            chunk.blocks[0][0][0].setFront(true);
    }

    private void updateBottomLeftFront() {
        if (leftChunk != null) {
            if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
                chunk.blocks[0][0][Chunk.CHUNK_WIDTH - 1].setLeft(true);
        }

        if (frontChunk != null) {
            if (frontChunk.blocks[0][0][0].isOpaque())
                chunk.blocks[0][0][Chunk.CHUNK_WIDTH - 1].setFront(true);
        }

        if (chunk.blocks[1][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
            chunk.blocks[0][0][Chunk.CHUNK_WIDTH - 1].setRight(true);

        if (chunk.blocks[0][1][Chunk.CHUNK_WIDTH - 1].isOpaque())
            chunk.blocks[0][0][Chunk.CHUNK_WIDTH - 1].setTop(true);

        if (chunk.blocks[0][0][Chunk.CHUNK_WIDTH - 2].isOpaque())
            chunk.blocks[0][0][Chunk.CHUNK_WIDTH - 1].setBack(true);
    }

    private void updateBottomRightBack() {
        if (rightChunk != null)
            if (rightChunk.blocks[0][0][0].isOpaque())
                chunk.blocks[Chunk.CHUNK_WIDTH - 1][0][0].setRight(true);

        if (backChunk != null)
            if (backChunk.blocks[Chunk.CHUNK_WIDTH - 1][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
                chunk.blocks[Chunk.CHUNK_WIDTH - 1][0][0].setBack(true);

        if (chunk.blocks[Chunk.CHUNK_WIDTH - 2][0][0].isOpaque())
            chunk.blocks[Chunk.CHUNK_WIDTH - 1][0][0].setLeft(true);

        if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][1][0].isOpaque())
            chunk.blocks[Chunk.CHUNK_WIDTH - 1][0][0].setTop(true);

        if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][0][1].isOpaque())
            chunk.blocks[Chunk.CHUNK_WIDTH - 1][0][0].setFront(true);
    }

    private void updateBottomRightFront() {

        if (rightChunk != null) {
            if (rightChunk.blocks[Chunk.CHUNK_WIDTH - 1][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
                chunk.blocks[Chunk.CHUNK_WIDTH - 1][0][Chunk.CHUNK_WIDTH - 1].setRight(true);
        }

        if (frontChunk != null) {
            if (frontChunk.blocks[Chunk.CHUNK_WIDTH - 1][0][0].isOpaque())
                chunk.blocks[Chunk.CHUNK_WIDTH - 1][0][Chunk.CHUNK_WIDTH - 1].setFront(true);
        }

        if (chunk.blocks[Chunk.CHUNK_WIDTH - 2][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
            chunk.blocks[Chunk.CHUNK_WIDTH - 1][0][Chunk.CHUNK_WIDTH - 1].setLeft(true);

        if (chunk.blocks[0][1][Chunk.CHUNK_WIDTH - 1].isOpaque())
            chunk.blocks[Chunk.CHUNK_WIDTH - 1][0][Chunk.CHUNK_WIDTH - 1].setTop(true);

        if (chunk.blocks[0][0][Chunk.CHUNK_WIDTH - 2].isOpaque())
            chunk.blocks[Chunk.CHUNK_WIDTH - 1][0][Chunk.CHUNK_WIDTH - 1].setBack(true);
    }

    private void updateBottomLeft() {
        boolean isValid = leftChunk != null;

        for (int z = 1; z < Chunk.CHUNK_WIDTH - 1; z++) {
            if (isValid)
                if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][0][z].isOpaque())
                    chunk.blocks[0][0][z].setLeft(true);

            if (chunk.blocks[1][0][z].isOpaque()) {
                chunk.blocks[0][0][z].setRight(true);
            }

            if (chunk.blocks[0][1][z].isOpaque()) {
                chunk.blocks[0][0][z].setTop(true);
            }

            if (chunk.blocks[0][0][z + 1].isOpaque()) {
                chunk.blocks[0][0][z].setFront(true);
            }

            if (chunk.blocks[0][0][z - 1].isOpaque()) {
                chunk.blocks[0][0][z].setBack(true);
            }
        }
    }

    private void updateBottomRight() {

        boolean isValid = rightChunk != null;

        for (int z = 1; z < Chunk.CHUNK_WIDTH - 1; z++) {
            if (isValid)
                if (rightChunk.blocks[0][0][z].isOpaque())
                    chunk.blocks[Chunk.CHUNK_WIDTH - 1][0][z].setRight(true);

            if (chunk.blocks[Chunk.CHUNK_WIDTH - 2][0][z].isOpaque()) {
                chunk.blocks[Chunk.CHUNK_WIDTH - 1][0][z].setLeft(true);
            }

            if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][1][z].isOpaque()) {
                chunk.blocks[Chunk.CHUNK_WIDTH - 1][0][z].setTop(true);
            }

            if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][0][z + 1].isOpaque()) {
                chunk.blocks[Chunk.CHUNK_WIDTH - 1][0][z].setFront(true);
            }

            if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][0][z - 1].isOpaque()) {
                chunk.blocks[Chunk.CHUNK_WIDTH - 1][0][z].setBack(true);
            }
        }
    }

    private void updateBottomFront() {
        boolean isValid = frontChunk != null;

        for (int x = 1; x < Chunk.CHUNK_WIDTH - 1; x++) {
            if (isValid)
                if (frontChunk.blocks[x][0][0].isOpaque())
                    chunk.blocks[x][0][Chunk.CHUNK_WIDTH - 1].setFront(true);

            if (chunk.blocks[x + 1][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
                chunk.blocks[x][0][Chunk.CHUNK_WIDTH - 1].setRight(true);

            if (chunk.blocks[x - 1][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
                chunk.blocks[x][0][Chunk.CHUNK_WIDTH - 1].setLeft(true);

            if (chunk.blocks[x][1][Chunk.CHUNK_WIDTH - 1].isOpaque())
                chunk.blocks[x][0][Chunk.CHUNK_WIDTH - 1].setTop(true);

            if (chunk.blocks[x][0][Chunk.CHUNK_WIDTH - 2].isOpaque())
                chunk.blocks[x][0][Chunk.CHUNK_WIDTH - 1].setBack(true);
        }
    }

    private void updateBottomBack() {
        boolean isValid = backChunk != null;

        for (int x = 1; x < Chunk.CHUNK_WIDTH - 1; x++) {
            if (isValid)
                if (backChunk.blocks[x][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
                    chunk.blocks[x][0][0].setBack(true);

            if (chunk.blocks[x + 1][0][0].isOpaque())
                chunk.blocks[x][0][0].setRight(true);

            if (chunk.blocks[x - 1][0][0].isOpaque())
                chunk.blocks[x][0][0].setLeft(true);

            if (chunk.blocks[x][1][0].isOpaque())
                chunk.blocks[x][0][0].setTop(true);

            if (chunk.blocks[x][0][1].isOpaque())
                chunk.blocks[x][0][0].setFront(true);
        }
    }

    private void updateBottomSide() {
        for (int x = 1; x < Chunk.CHUNK_WIDTH - 1; x++) {
            for (int z = 1; z < Chunk.CHUNK_WIDTH - 1; z++) {
                if (chunk.blocks[x][0][z].is(Type.AIR) == false) {
                    if (chunk.blocks[x + 1][0][z].isOpaque())
                        chunk.blocks[x][0][z].setRight(true);

                    if (chunk.blocks[x - 1][0][z].isOpaque())
                        chunk.blocks[x][0][z].setLeft(true);

                    if (chunk.blocks[x][1][z].isOpaque())
                        chunk.blocks[x][0][z].setTop(true);

                    if (chunk.blocks[x][0][z + 1].isOpaque())
                        chunk.blocks[x][0][z].setFront(true);

                    if (chunk.blocks[x + 1][0][z - 1].isOpaque())
                        chunk.blocks[x][0][z].setBack(true);
                }
            }
        }
    }

    private void updateLeftSide() {
        boolean isValid = leftChunk != null;

        for (int z = 1; z < Chunk.CHUNK_WIDTH - 1; z++) {
            for (int y = 1; y < Chunk.CHUNK_HEIGHT - 1; y++) {
                if (chunk.blocks[0][y][z].is(Type.AIR) == false) {
                    if (isValid)
                        if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][y][z].isOpaque())
                            chunk.blocks[0][y][z].setLeft(true);

                    if (chunk.blocks[0][y + 1][z].isOpaque())
                        chunk.blocks[0][y][z].setTop(true);

                    if (chunk.blocks[0][y - 1][z].isOpaque())
                        chunk.blocks[0][y][z].setBottom(true);

                    if (chunk.blocks[0][y][z + 1].isOpaque())
                        chunk.blocks[0][y][z].setFront(true);

                    if (chunk.blocks[0][y][z - 1].isOpaque())
                        chunk.blocks[0][y][z].setBack(true);

                    if (chunk.blocks[1][y][z].isOpaque())
                        chunk.blocks[0][y][z].setRight(true);
                }
            }
        }
    }

    private void updateRightSide() {
        boolean isValid = rightChunk != null;

        for (int z = 1; z < Chunk.CHUNK_WIDTH - 1; z++) {
            for (int y = 1; y < Chunk.CHUNK_HEIGHT - 1; y++) {
                if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][z].is(Type.AIR) == false) {
                    if (isValid)
                        if (rightChunk.blocks[0][y][z].isOpaque()) {
                            chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][z].setRight(true);
                        }

                    if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y + 1][z].isOpaque())
                        chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][z].setTop(true);

                    if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y - 1][z].isOpaque())
                        chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][z].setBottom(true);

                    if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][z + 1].isOpaque())
                        chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][z].setFront(true);

                    if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][z - 1].isOpaque())
                        chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][z].setBack(true);

                    if (chunk.blocks[Chunk.CHUNK_WIDTH - 2][y][z].isOpaque())
                        chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][z].setLeft(true);
                }
            }
        }
    }

    private void updateFrontSide() {
        boolean isValid = frontChunk != null;

        for (int x = 1; x < Chunk.CHUNK_WIDTH - 1; x++) {
            for (int y = 1; y < Chunk.CHUNK_HEIGHT - 1; y++) {
                if (chunk.blocks[x][y][Chunk.CHUNK_WIDTH - 1].is(Type.AIR) == false) {
                    if (isValid) {
                        if (frontChunk.blocks[x][y][0].isOpaque())
                            chunk.blocks[x][y][Chunk.CHUNK_WIDTH - 1].setFront(true);
                    }

                    if (chunk.blocks[x + 1][y][Chunk.CHUNK_WIDTH - 1].isOpaque())
                        chunk.blocks[x][y][Chunk.CHUNK_WIDTH - 1].setRight(true);

                    if (chunk.blocks[x - 1][y][Chunk.CHUNK_WIDTH - 1].isOpaque())
                        chunk.blocks[x][y][Chunk.CHUNK_WIDTH - 1].setLeft(true);

                    if (chunk.blocks[x][y + 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
                        chunk.blocks[x][y][Chunk.CHUNK_WIDTH - 1].setTop(true);

                    if (chunk.blocks[x][y - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
                        chunk.blocks[x][y][Chunk.CHUNK_WIDTH - 1].setBottom(true);

                    if (chunk.blocks[x][y][Chunk.CHUNK_WIDTH - 2].isOpaque())
                        chunk.blocks[x][y][Chunk.CHUNK_WIDTH - 1].setBack(true);
                }
            }
        }
    }

    private void updateBackSide() {
        boolean isValid = backChunk != null;

        for (int x = 1; x < Chunk.CHUNK_WIDTH - 1; x++) {
            for (int y = 1; y < Chunk.CHUNK_HEIGHT - 1; y++) {
                if (chunk.blocks[x][y][0].is(Type.AIR) == false) {
                    if (isValid)
                        if (backChunk.blocks[x][y][Chunk.CHUNK_WIDTH - 1].isOpaque())
                            chunk.blocks[x][y][0].setBack(true);

                    if (chunk.blocks[x + 1][y][0].isOpaque())
                        chunk.blocks[x][y][0].setRight(true);

                    if (chunk.blocks[x - 1][y][0].isOpaque())
                        chunk.blocks[x][y][0].setLeft(true);

                    if (chunk.blocks[x][y + 1][0].isOpaque())
                        chunk.blocks[x][y][0].setTop(true);

                    if (chunk.blocks[x][y - 1][0].isOpaque())
                        chunk.blocks[x][y][0].setBottom(true);

                    if (chunk.blocks[x][y][1].isOpaque())
                        chunk.blocks[x][y][0].setFront(true);
                }
            }
        }
    }

    private int calculateVertexCount() {
        int vertexCount = 0;
        for (int x = 0; x < Chunk.CHUNK_WIDTH; x++)
            for (int z = 0; z < Chunk.CHUNK_WIDTH; z++)
                for (int y = 0; y < Chunk.CHUNK_HEIGHT; y++) {
                    if (chunk.blocks[x][y][z].isBack())
                        vertexCount += 6;
                    if (chunk.blocks[x][y][z].isBottom())
                        vertexCount += 6;
                    if (chunk.blocks[x][y][z].isFront())
                        vertexCount += 6;
                    if (chunk.blocks[x][y][z].isLeft())
                        vertexCount += 6;
                    if (chunk.blocks[x][y][z].isRight())
                        vertexCount += 6;
                    if (chunk.blocks[x][y][z].isTop())
                        vertexCount += 6;
                }

        return vertexCount;
    }

    private void updateLeftBack() {
        boolean backIsValid = backChunk != null;
        boolean leftIsValid = leftChunk != null;

        for (int y = 1; y < Chunk.CHUNK_HEIGHT - 1; y++) {
            if (chunk.blocks[0][y][0].is(Type.AIR) == false) {
                if (backIsValid)
                    if (backChunk.blocks[0][y][Chunk.CHUNK_WIDTH - 1].isOpaque())
                        chunk.blocks[0][y][0].setBack(true);

                if (leftIsValid)
                    if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][y][0].isOpaque())
                        chunk.blocks[0][y][0].setLeft(true);

                if (chunk.blocks[1][y][0].isOpaque())
                    chunk.blocks[0][y][0].setRight(true);

                if (chunk.blocks[0][y + 1][0].isOpaque())
                    chunk.blocks[0][y][0].setTop(true);

                if (chunk.blocks[0][y - 1][0].isOpaque())
                    chunk.blocks[0][y][0].setBottom(true);

                if (chunk.blocks[0][y][1].isOpaque())
                    chunk.blocks[0][y][0].setFront(true);
            }
        }
    }

    private void updateLeftFront() {
        boolean frontIsValid = frontChunk != null;
        boolean leftIsValid = leftChunk != null;

        for (int y = 1; y < Chunk.CHUNK_HEIGHT - 1; y++) {
            if (chunk.blocks[0][y][Chunk.CHUNK_WIDTH - 1].is(Type.AIR) == false) {
                if (frontIsValid)
                    if (frontChunk.blocks[0][y][0].isOpaque())
                        chunk.blocks[0][y][Chunk.CHUNK_WIDTH - 1].setFront(true);

                if (leftIsValid)
                    if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][y][Chunk.CHUNK_WIDTH - 1].isOpaque())
                        chunk.blocks[0][y][Chunk.CHUNK_WIDTH - 1].setLeft(true);

                if (chunk.blocks[1][y][Chunk.CHUNK_WIDTH - 1].isOpaque())
                    chunk.blocks[0][y][Chunk.CHUNK_WIDTH - 1].setRight(true);

                if (chunk.blocks[0][y + 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
                    chunk.blocks[0][y][Chunk.CHUNK_WIDTH - 1].setTop(true);

                if (chunk.blocks[0][y - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
                    chunk.blocks[0][y][Chunk.CHUNK_WIDTH - 1].setBottom(true);

                if (chunk.blocks[0][y][Chunk.CHUNK_WIDTH - 2].isOpaque())
                    chunk.blocks[0][y][Chunk.CHUNK_WIDTH - 1].setBack(true);
            }
        }
    }

    private void updateRightBack() {
        boolean rightIsValid = rightChunk != null;
        boolean backIsValid = backChunk != null;

        for (int y = 1; y < Chunk.CHUNK_HEIGHT - 1; y++) {
            if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][0].is(Type.AIR) == false) {
                if (rightIsValid)
                    if (rightChunk.blocks[0][y][0].isOpaque())
                        chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][0].setRight(true);

                if (backIsValid)
                    if (backChunk.blocks[Chunk.CHUNK_WIDTH - 1][y][Chunk.CHUNK_WIDTH - 1].isOpaque())
                        chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][0].setBack(true);

                if (chunk.blocks[Chunk.CHUNK_WIDTH - 2][y][0].isOpaque())
                    chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][0].setLeft(true);

                if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y + 1][0].isOpaque())
                    chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][0].setTop(true);

                if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y - 1][0].isOpaque())
                    chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][0].setBottom(true);

                if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][1].isOpaque())
                    chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][0].setFront(true);
            }
        }
    }

    private void updateRightFront() {
        boolean rightIsValid = rightChunk != null;
        boolean frontIsValid = frontChunk != null;

        for (int y = 1; y < Chunk.CHUNK_HEIGHT - 1; y++) {
            if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][Chunk.CHUNK_WIDTH - 1].is(Type.AIR) == false) {
                if (rightIsValid)
                    if (rightChunk.blocks[0][y][Chunk.CHUNK_WIDTH - 1].isOpaque())
                        chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][Chunk.CHUNK_WIDTH - 1].setRight(true);

                if (frontIsValid)
                    if (frontChunk.blocks[Chunk.CHUNK_WIDTH - 1][y][0].isOpaque())
                        chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][Chunk.CHUNK_WIDTH - 1].setFront(true);

                if (chunk.blocks[Chunk.CHUNK_WIDTH - 2][y][Chunk.CHUNK_WIDTH - 1].isOpaque())
                    chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][Chunk.CHUNK_WIDTH - 1].setLeft(true);

                if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y + 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
                    chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][Chunk.CHUNK_WIDTH - 1].setTop(true);

                if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
                    chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][Chunk.CHUNK_WIDTH - 1].setBottom(true);

                if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][Chunk.CHUNK_WIDTH - 2].isOpaque())
                    chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][Chunk.CHUNK_WIDTH - 1].setBack(true);

            }
        }
    }

}
