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
    private int chunkY;
    private int chunkZ;
    private int xOff;
    private int yOff;
    private int zOff;
    private ConcurrentHashMap<Integer, byte[]> map;
    private boolean ready = false;

    private Chunk rightChunk;
    private Chunk leftChunk;
    private Chunk frontChunk;
    private Chunk backChunk;
    private Chunk topChunk;
    private Chunk bottomChunk;

    private ChunkManager chunkManager;
    private ArrayList<Data> dataToProcess;

    private Data updateData;

    boolean update;

    public ChunkMaker(ArrayList<Data> dataToProcess, int chunkX, int chunkY, int chunkZ, int xOff, int yOff, int zOff, ConcurrentHashMap<Integer, byte[]> map, ChunkManager chunkManager) {
        this.xOff = xOff;
        this.yOff = yOff;
        this.zOff = zOff;
        this.chunkX = chunkX;
        this.chunkY = chunkY;
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
        if (!map.containsKey(new Pair(chunkX, chunkY, chunkZ).hashCode())) {
            chunk = new Chunk(chunkX, chunkY, chunkZ);
            map.put(new Pair(chunkX, chunkY, chunkZ).hashCode(), toByte(chunk));
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
            map.put(new Pair(this.chunk.xId, this.chunk.yId, this.chunk.zId).hashCode(), toByte(this.chunk));
            Handle handle = chunkManager.getHandle(this.chunk.xId, this.chunk.yId, this.chunk.zId);
            dataToProcess.add(new Data(this.chunk.xId, this.chunk.yId, this.chunk.zId, this.chunk.getVertices(), vertexData, normalData, texData, handle.vertexHandle, handle.normalHandle, handle.texHandle, true));
        }
    }

    public void updateLeft(Chunk chunk) {
        if (chunk != null) {
            this.chunk = chunk;
            this.xOff = chunk.xCoordinate;
            this.zOff = chunk.zCoordinate;
            this.yOff = chunk.yId * Chunk.CHUNK_SIZE;
            updateLeft();
            drawChunkVBO();

            map.put(new Pair(this.chunk.xId, this.chunk.yId, this.chunk.zId).hashCode(), toByte(this.chunk));
            Handle handle = chunkManager.getHandle(this.chunk.xId, this.chunk.yId, this.chunk.zId);
            dataToProcess.add(new Data(this.chunk.xId, this.chunk.yId, this.chunk.zId, this.chunk.getVertices(), vertexData, normalData, texData, handle.vertexHandle, handle.normalHandle, handle.texHandle, true));
        }
    }

    public void updateXYZ(int x, int y, int z) {
        Chunk chunk = chunkManager.getChunk(x / Chunk.CHUNK_SIZE, y / Chunk.CHUNK_SIZE, z / Chunk.CHUNK_SIZE);
        int xIn = x % Chunk.CHUNK_SIZE;
        int yIn = y % Chunk.CHUNK_SIZE;
        int zIn = z % Chunk.CHUNK_SIZE;

        Chunk right = chunkManager.getChunk(chunk.xId + 1, chunk.yId, chunk.zId);
        Chunk left = chunkManager.getChunk(chunk.xId - 1, chunk.yId, chunk.zId);
        Chunk top = chunkManager.getChunk(chunk.xId, chunk.yId + 1, chunk.zId);
        Chunk bottom = chunkManager.getChunk(chunk.xId, chunk.yId - 1, chunk.zId);
        Chunk front = chunkManager.getChunk(chunk.xId, chunk.yId, chunk.zId + 1);
        Chunk back = chunkManager.getChunk(chunk.xId, chunk.yId, chunk.zId - 1);

        if (xIn == 0 && yIn == 0 && zIn == 0) {

        }
        if (xIn == Chunk.CHUNK_SIZE - 1 && yIn == 0 && zIn == 0) {

        }
        if (xIn == 0 && yIn == Chunk.CHUNK_SIZE && zIn == 0) {

        }
        if (xIn == Chunk.CHUNK_SIZE - 1 && yIn == Chunk.CHUNK_SIZE - 1 && zIn == 0) {

        }
        if (xIn == 0 && yIn == 0 && zIn == Chunk.CHUNK_SIZE - 1) {

        }
        if (xIn == Chunk.CHUNK_SIZE - 1 && yIn == 0 && zIn == Chunk.CHUNK_SIZE - 1) {

        }
        if (xIn == 0 && yIn == Chunk.CHUNK_SIZE - 1 && zIn == Chunk.CHUNK_SIZE - 1) {

        }
        if (xIn == Chunk.CHUNK_SIZE - 1 && yIn == Chunk.CHUNK_SIZE - 1 && zIn == Chunk.CHUNK_SIZE - 1) {

        }

    }

    public void updateSides(Chunk chunk) {
        if (chunk != null) {
            this.chunk = chunk;
            this.xOff = chunk.xCoordinate;
            this.zOff = chunk.zCoordinate;
            this.yOff = chunk.yId * Chunk.CHUNK_SIZE;
            updateRight();
            updateLeft();
            updateBack();
            updateFront();
            updateTop();
            updateBottom();
            drawChunkVBO();

            map.put(new Pair(this.chunk.xId, this.chunk.yId, this.chunk.zId).hashCode(), toByte(this.chunk));
            Handle handle = chunkManager.getHandle(this.chunk.xId, this.chunk.yId, this.chunk.zId);
            dataToProcess.add(new Data(this.chunk.xId, this.chunk.yId, this.chunk.zId, this.chunk.getVertices(), vertexData, normalData, texData, handle.vertexHandle, handle.normalHandle, handle.texHandle, true));
        }
    }

    public void addDataToProcess() {
        dataToProcess.add(new Data(chunkX, chunkY, chunkZ, chunk.getVertices(), vertexData, normalData, texData, false));
    }

    public void updateRight(Chunk chunk) {
        if (chunk != null) {
            this.chunk = chunk;
            this.xOff = chunk.xCoordinate;
            this.zOff = chunk.zCoordinate;
            this.yOff = chunk.yId * Chunk.CHUNK_SIZE;
            updateRight();
            drawChunkVBO();

            map.put(new Pair(this.chunk.xId, this.chunk.yId, this.chunk.zId).hashCode(), toByte(this.chunk));
            Handle handle = chunkManager.getHandle(this.chunk.xId, this.chunk.yId, this.chunk.zId);
            dataToProcess.add(new Data(this.chunk.xId, this.chunk.yId, this.chunk.zId, this.chunk.getVertices(), vertexData, normalData, texData, handle.vertexHandle, handle.normalHandle, handle.texHandle, true));
        }
    }

    public void updateTop(Chunk chunk) {
        if (chunk != null) {
            this.chunk = chunk;
            this.xOff = chunk.xCoordinate;
            this.zOff = chunk.zCoordinate;
            this.yOff = chunk.yId * Chunk.CHUNK_SIZE;
            updateTop();
            drawChunkVBO();

            map.put(new Pair(this.chunk.xId, this.chunk.yId, this.chunk.zId).hashCode(), toByte(this.chunk));
            Handle handle = chunkManager.getHandle(this.chunk.xId, this.chunk.yId, this.chunk.zId);
            dataToProcess.add(new Data(this.chunk.xId, this.chunk.yId, this.chunk.zId, this.chunk.getVertices(), vertexData, normalData, texData, handle.vertexHandle, handle.normalHandle, handle.texHandle, true));
        }
    }

    public void updateBottom(Chunk chunk) {
        if (chunk != null) {
            this.chunk = chunk;
            this.xOff = chunk.xCoordinate;
            this.zOff = chunk.zCoordinate;
            this.yOff = chunk.yId * Chunk.CHUNK_SIZE;
            updateBottom();
            drawChunkVBO();

            map.put(new Pair(this.chunk.xId, this.chunk.yId, this.chunk.zId).hashCode(), toByte(this.chunk));
            Handle handle = chunkManager.getHandle(this.chunk.xId, this.chunk.yId, this.chunk.zId);
            dataToProcess.add(new Data(this.chunk.xId, this.chunk.yId, this.chunk.zId, this.chunk.getVertices(), vertexData, normalData, texData, handle.vertexHandle, handle.normalHandle, handle.texHandle, true));
        }
    }

    public void updateBack(Chunk chunk) {
        if (chunk != null) {
            this.chunk = chunk;
            this.xOff = chunk.xCoordinate;
            this.zOff = chunk.zCoordinate;
            this.yOff = chunk.yId * Chunk.CHUNK_SIZE;
            updateBack();
            drawChunkVBO();

            map.put(new Pair(this.chunk.xId, this.chunk.yId, this.chunk.zId).hashCode(), toByte(this.chunk));
            Handle handle = chunkManager.getHandle(this.chunk.xId, this.chunk.yId, this.chunk.zId);
            dataToProcess.add(new Data(this.chunk.xId, this.chunk.yId, this.chunk.zId, this.chunk.getVertices(), vertexData, normalData, texData, handle.vertexHandle, handle.normalHandle, handle.texHandle, true));
        }
    }

    public void updateFront(Chunk chunk) {
        if (chunk != null) {
            this.chunk = chunk;
            this.xOff = chunk.xCoordinate;
            this.zOff = chunk.zCoordinate;
            this.yOff = chunk.yId * Chunk.CHUNK_SIZE;
            updateFront();
            drawChunkVBO();

            map.put(new Pair(this.chunk.xId, this.chunk.yId, this.chunk.zId).hashCode(), toByte(this.chunk));
            Handle handle = chunkManager.getHandle(this.chunk.xId, this.chunk.yId, this.chunk.zId);
            dataToProcess.add(new Data(this.chunk.xId, this.chunk.yId, this.chunk.zId, this.chunk.getVertices(), vertexData, normalData, texData, handle.vertexHandle, handle.normalHandle, handle.texHandle, true));
        }
    }

    public void drawChunkVBO() {

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
        float tSize = 0.1f;
        for (int x = 0; x < chunk.blocks.length; x++) {
            for (int z = 0; z < chunk.blocks[x][0].length; z++) {
                for (int y = 0; y < chunk.blocks[x].length; y++) {
                    if (!chunk.blocks[x][y][z].is(Type.AIR)) {
                        short type = chunk.blocks[x][y][z].getType();

                        frontXOff = AtlasManager.getFrontXOff(type);
                        frontYOff = AtlasManager.getFrontYOff(type);

                        backXOff = AtlasManager.getBackXOff(type);
                        backYOff = AtlasManager.getBackYOff(type);

                        rightXOff = AtlasManager.getRightXOff(type);
                        rightYOff = AtlasManager.getRightYOff(type);

                        leftXOff = AtlasManager.getLeftXOff(type);
                        leftYOff = AtlasManager.getLeftYOff(type);

                        topXOff = AtlasManager.getTopXOff(type);
                        topYOff = AtlasManager.getTopYOff(type);

                        bottomXOff = AtlasManager.getBottomXOff(type);
                        bottomYOff = AtlasManager.getBottomYOff(type);

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
                            vertexArray[vArrayPos] = 1 + y + yOff;
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
                            vertexArray[vArrayPos] = 0 + y + yOff;
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
                            vertexArray[vArrayPos] = 0 + y + yOff;
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
                            vertexArray[vArrayPos] = 1 + y + yOff;
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
                            vertexArray[vArrayPos] = 0 + y + yOff;
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
                            vertexArray[vArrayPos] = 1 + y + yOff;
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
                            texArray[tArrayPos] = topYOff-tSize;
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
                            texArray[tArrayPos] = topYOff - tSize;
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

                            texArray[tArrayPos] = topXOff+tSize;
                            tArrayPos++;
                            texArray[tArrayPos] = topYOff-tSize;
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

                            texArray[tArrayPos] = topXOff;
                            tArrayPos++;
                            texArray[tArrayPos] = topYOff- tSize;
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

    public void updateAllBlocks() {
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
        topChunk = null;
        bottomChunk = null;
    }

    private void getAdjacentChunks() {
        rightChunk = chunkManager.getChunk(chunk.xId + 1, chunk.yId, chunk.zId);
        leftChunk = chunkManager.getChunk(chunk.xId - 1, chunk.yId, chunk.zId);
        backChunk = chunkManager.getChunk(chunk.xId, chunk.yId, chunk.zId - 1);
        frontChunk = chunkManager.getChunk(chunk.xId, chunk.yId, chunk.zId + 1);
        topChunk = chunkManager.getChunk(chunk.xId, chunk.yId + 1, chunk.zId);
        bottomChunk = chunkManager.getChunk(chunk.xId, chunk.yId - 1, chunk.zId);
    }

    private void updateBack() {
        backChunk = chunkManager.getChunk(chunk.xId, chunk.yId, chunk.zId - 1);
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
        frontChunk = chunkManager.getChunk(chunk.xId, chunk.yId, chunk.zId + 1);
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
        leftChunk = chunkManager.getChunk(chunk.xId - 1, chunk.yId, chunk.zId);
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

    private void updateBottom() {
        bottomChunk = chunkManager.getChunk(chunk.xId, chunk.yId - 1, chunk.zId);
        updateBottomBack();
        updateBottomFront();
        updateBottomLeft();
        updateBottomLeftBack();
        updateBottomLeftFront();
        updateBottomRight();
        updateBottomRightBack();
        updateBottomRightFront();
        updateBottomSide();
        bottomChunk = null;
    }

    private void updateTop() {
        topChunk = chunkManager.getChunk(chunk.xId, chunk.yId + 1, chunk.zId);
        updateTopBack();
        updateTopFront();
        updateTopLeft();
        updateTopRight();
        updateTopLeftBack();
        updateTopLeftFront();
        updateTopRightBack();
        updateTopRightFront();
        updateTopSide();
        topChunk = null;
    }

    private void updateRight() {
        rightChunk = chunkManager.getChunk(chunk.xId + 1, chunk.yId, chunk.zId);
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
                        } else {
                            chunk.blocks[x][y][z].setRight(false);
                        }
                        if (chunk.blocks[x - 1][y][z].isOpaque()) {
                            chunk.blocks[x][y][z].setLeft(true);

                        } else {
                            chunk.blocks[x][y][z].setLeft(false);
                        }
                        if (chunk.blocks[x][y + 1][z].isOpaque()) {
                            chunk.blocks[x][y][z].setTop(true);

                        } else {
                            chunk.blocks[x][y][z].setTop(false);
                        }
                        if (chunk.blocks[x][y - 1][z].isOpaque()) {
                            chunk.blocks[x][y][z].setBottom(true);

                        } else {
                            chunk.blocks[x][y][z].setBottom(false);
                        }
                        if (chunk.blocks[x][y][z + 1].isOpaque()) {
                            chunk.blocks[x][y][z].setFront(true);

                        } else {
                            chunk.blocks[x][y][z].setFront(false);
                        }
                        if (chunk.blocks[x][y][z - 1].isOpaque()) {
                            chunk.blocks[x][y][z].setBack(true);

                        } else {
                            chunk.blocks[x][y][z].setBack(false);
                        }
                    } else if (chunk.blocks[x][y][z].is(Type.WATER)) {
                        // if water, if the block above it is not water, make it active
                        if (chunk.blocks[x][y + 1][z].is(Type.WATER) == false) {
                            chunk.blocks[x][y][z].setTop(true);
                        } else {
                            chunk.blocks[x][y][z].setTop(false);
                        }
                    }
                }
            }
        }
    }

    private void updateTopLeftBack() {
        if (leftChunk != null) {
            if (leftChunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0].isOpaque()) {
                chunk.blocks[0][Chunk.CHUNK_SIZE - 1][0].setLeft(true);
            } else {
                chunk.blocks[0][Chunk.CHUNK_SIZE - 1][0].setLeft(false);
            }
        }

        if (backChunk != null) {
            if (backChunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                chunk.blocks[0][Chunk.CHUNK_SIZE - 1][0].setBack(true);
            } else {
                chunk.blocks[0][Chunk.CHUNK_SIZE - 1][0].setBack(false);
            }
        }

        if (topChunk != null) {
            if (topChunk.blocks[0][0][0].isOpaque()) {
                chunk.blocks[0][Chunk.CHUNK_SIZE - 1][0].setTop(true);
            } else {
                chunk.blocks[0][Chunk.CHUNK_SIZE - 1][0].setTop(false);
            }
        }

        if (chunk.blocks[1][Chunk.CHUNK_SIZE - 1][0].isOpaque()) {
            chunk.blocks[0][Chunk.CHUNK_SIZE - 1][0].setRight(true);
        } else {
            chunk.blocks[0][Chunk.CHUNK_SIZE - 1][0].setRight(false);
        }

        if (chunk.blocks[0][Chunk.CHUNK_SIZE - 2][0].isOpaque()) {
            chunk.blocks[0][Chunk.CHUNK_SIZE - 1][0].setBottom(true);
        } else {
            chunk.blocks[0][Chunk.CHUNK_SIZE - 1][0].setBottom(false);
        }

        if (chunk.blocks[0][Chunk.CHUNK_SIZE - 1][1].isOpaque()) {
            chunk.blocks[0][Chunk.CHUNK_SIZE - 1][0].setFront(true);
        } else {
            chunk.blocks[0][Chunk.CHUNK_SIZE - 1][0].setFront(false);
        }

    }

    private void updateTopLeftFront() {
        if (leftChunk != null) {
            if (leftChunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                chunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setLeft(true);
            } else {
                chunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setLeft(false);
            }
        }
        if (frontChunk != null) {
            if (frontChunk.blocks[0][Chunk.CHUNK_SIZE - 1][0].isOpaque()) {
                chunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setFront(true);
            } else {
                chunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setFront(false);
            }
        }

        if (topChunk != null) {
            if (topChunk.blocks[0][0][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                chunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setTop(true);
            } else {
                chunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setTop(false);
            }
        }

        if (chunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 2].isOpaque()) {
            chunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setBack(true);
        } else {
            chunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setBack(false);
        }

        if (chunk.blocks[0][Chunk.CHUNK_SIZE - 2][Chunk.CHUNK_SIZE - 1].isOpaque()) {
            chunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setBottom(true);
        } else {
            chunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setBottom(false);
        }

        if (chunk.blocks[1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].isOpaque()) {
            chunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setRight(true);
        } else {
            chunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setRight(false);
        }
    }

    private void updateTopRightBack() {
        if (backChunk != null) {
            if (backChunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0].setBack(true);
            } else {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0].setBack(false);
            }
        }
        if (rightChunk != null) {
            if (rightChunk.blocks[0][Chunk.CHUNK_SIZE - 1][0].isOpaque()) {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0].setRight(true);
            } else {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0].setRight(false);
            }
        }

        if (topChunk != null) {
            if (topChunk.blocks[Chunk.CHUNK_SIZE - 1][0][0].isOpaque()) {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0].setTop(true);
            } else {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0].setTop(false);
            }
        }

        if (chunk.blocks[Chunk.CHUNK_SIZE - 2][Chunk.CHUNK_SIZE - 1][0].isOpaque()) {
            chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0].setLeft(true);
        } else {
            chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0].setLeft(false);
        }

        if (chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 2][0].isOpaque()) {
            chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0].setBottom(true);
        } else {
            chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0].setBottom(false);
        }

        if (chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][1].isOpaque()) {
            chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0].setFront(true);
        } else {
            chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0].setFront(false);
        }
    }

    private void updateTopRightFront() {
        if (rightChunk != null) {
            if (rightChunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setRight(true);
            } else {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setRight(false);
            }
        }
        if (frontChunk != null) {
            if (frontChunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][0].isOpaque()) {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setFront(true);
            } else {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setFront(false);
            }
        }

        if (topChunk != null) {

            if (topChunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setTop(true);
            } else {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setTop(false);
            }
        }

        if (chunk.blocks[Chunk.CHUNK_SIZE - 2][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].isOpaque()) {
            chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setLeft(true);
        } else {
            chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setLeft(false);
        }

        if (chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 2][Chunk.CHUNK_SIZE - 1].isOpaque()) {
            chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setBottom(true);
        } else {
            chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setBottom(false);
        }

        if (chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 2].isOpaque()) {
            chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setBack(true);
        } else {
            chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setBack(false);
        }
    }

    private void updateTopLeft() {
        boolean isValid = leftChunk != null;

        for (int z = 1; z < Chunk.CHUNK_SIZE - 1; z++) {
            if (isValid) {
                if (leftChunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z].isOpaque()) {
                    chunk.blocks[0][Chunk.CHUNK_SIZE - 1][z].setLeft(true);
                } else {
                    chunk.blocks[0][Chunk.CHUNK_SIZE - 1][z].setLeft(false);
                }
            }

            if (topChunk != null) {
                if (topChunk.blocks[0][0][z].isOpaque()) {
                    chunk.blocks[0][Chunk.CHUNK_SIZE - 1][z].setTop(true);
                } else {
                    chunk.blocks[0][Chunk.CHUNK_SIZE - 1][z].setTop(false);
                }
            }

            if (chunk.blocks[1][Chunk.CHUNK_SIZE - 1][z].isOpaque()) {
                chunk.blocks[0][Chunk.CHUNK_SIZE - 1][z].setRight(true);
            } else {
                chunk.blocks[0][Chunk.CHUNK_SIZE - 1][z].setRight(false);
            }

            if (chunk.blocks[0][Chunk.CHUNK_SIZE - 2][z].isOpaque()) {
                chunk.blocks[0][Chunk.CHUNK_SIZE - 1][z].setBottom(true);
            } else {
                chunk.blocks[0][Chunk.CHUNK_SIZE - 1][z].setBottom(false);
            }

            if (chunk.blocks[0][Chunk.CHUNK_SIZE - 1][z + 1].isOpaque()) {
                chunk.blocks[0][Chunk.CHUNK_SIZE - 1][z].setFront(true);
            } else {
                chunk.blocks[0][Chunk.CHUNK_SIZE - 1][z].setFront(false);
            }

            if (chunk.blocks[0][Chunk.CHUNK_SIZE - 1][z - 1].isOpaque()) {
                chunk.blocks[0][Chunk.CHUNK_SIZE - 1][z].setBack(true);
            } else {
                chunk.blocks[0][Chunk.CHUNK_SIZE - 1][z].setBack(false);
            }
        }
    }

    private void updateTopRight() {
        boolean isValid = rightChunk != null;

        for (int z = 1; z < Chunk.CHUNK_SIZE - 1; z++) {
            if (isValid) {
                if (rightChunk.blocks[0][Chunk.CHUNK_SIZE - 1][z].isOpaque()) {
                    chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z].setRight(true);
                } else {
                    chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z].setRight(false);
                }
            }

            if (topChunk != null) {
                if (topChunk.blocks[Chunk.CHUNK_SIZE - 1][0][z].isOpaque()) {
                    chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z].setTop(true);
                } else {
                    chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z].setTop(false);
                }
            }

            if (chunk.blocks[Chunk.CHUNK_SIZE - 2][Chunk.CHUNK_SIZE - 1][z].isOpaque()) {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z].setLeft(true);
            } else {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z].setLeft(false);
            }

            if (chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 2][z].isOpaque()) {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z].setBottom(true);
            } else {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z].setBottom(false);
            }

            if (chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z + 1].isOpaque()) {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z].setFront(true);
            } else {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z].setFront(false);
            }

            if (chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z - 1].isOpaque()) {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z].setBack(true);
            } else {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z].setBack(false);
            }
        }
    }

    private void updateTopFront() {
        boolean isValid = frontChunk != null;

        for (int x = 1; x < Chunk.CHUNK_SIZE - 1; x++) {
            if (isValid) {
                if (frontChunk.blocks[x][Chunk.CHUNK_SIZE - 1][0].isOpaque()) {
                    chunk.blocks[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setFront(true);
                } else {
                    chunk.blocks[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setFront(false);
                }
            }

            if (topChunk != null) {
                if (topChunk.blocks[x][0][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                    chunk.blocks[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setTop(true);
                } else {
                    chunk.blocks[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setTop(false);
                }
            }

            if (chunk.blocks[x + 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                chunk.blocks[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setRight(true);
            } else {
                chunk.blocks[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setRight(false);
            }

            if (chunk.blocks[x - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                chunk.blocks[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setLeft(true);
            } else {
                chunk.blocks[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setLeft(false);
            }

            if (chunk.blocks[x][Chunk.CHUNK_SIZE - 2][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                chunk.blocks[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setBottom(true);
            } else {
                chunk.blocks[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setBottom(false);
            }

            if (chunk.blocks[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 2].isOpaque()) {
                chunk.blocks[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setBack(true);
            } else {
                chunk.blocks[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].setBack(false);
            }
        }
    }

    private void updateTopBack() {
        boolean isValid = backChunk != null;

        for (int x = 1; x < Chunk.CHUNK_SIZE - 1; x++) {
            if (isValid) {
                if (backChunk.blocks[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                    chunk.blocks[x][Chunk.CHUNK_SIZE - 1][0].setBack(true);
                } else {
                    chunk.blocks[x][Chunk.CHUNK_SIZE - 1][0].setBack(false);
                }
            }

            if (topChunk != null) {
                if (topChunk.blocks[x][0][0].isOpaque()) {
                    chunk.blocks[x][Chunk.CHUNK_SIZE - 1][0].setTop(true);
                } else {
                    chunk.blocks[x][Chunk.CHUNK_SIZE - 1][0].setTop(false);
                }
            }

            if (chunk.blocks[x + 1][Chunk.CHUNK_SIZE - 1][0].isOpaque()) {
                chunk.blocks[x][Chunk.CHUNK_SIZE - 1][0].setRight(true);
            } else {
                chunk.blocks[x][Chunk.CHUNK_SIZE - 1][0].setRight(false);
            }

            if (chunk.blocks[x - 1][Chunk.CHUNK_SIZE - 1][0].isOpaque()) {
                chunk.blocks[x][Chunk.CHUNK_SIZE - 1][0].setLeft(true);
            } else {
                chunk.blocks[x][Chunk.CHUNK_SIZE - 1][0].setLeft(false);
            }

            if (chunk.blocks[x][Chunk.CHUNK_SIZE - 2][0].isOpaque()) {
                chunk.blocks[x][Chunk.CHUNK_SIZE - 1][0].setBottom(true);
            } else {
                chunk.blocks[x][Chunk.CHUNK_SIZE - 1][0].setBottom(false);
            }

            if (chunk.blocks[x][Chunk.CHUNK_SIZE - 1][1].isOpaque()) {
                chunk.blocks[x][Chunk.CHUNK_SIZE - 1][0].setFront(true);
            } else {
                chunk.blocks[x][Chunk.CHUNK_SIZE - 1][0].setFront(false);
            }
        }

    }

    private void updateTopSide() {
        for (int x = 1; x < Chunk.CHUNK_SIZE - 1; x++) {
            for (int z = 1; z < Chunk.CHUNK_SIZE - 1; z++) {
                if (chunk.blocks[x][Chunk.CHUNK_SIZE - 1][z].is(Type.AIR) == false) {

                    if (topChunk != null) {
                        if (topChunk.blocks[x][0][z].isOpaque()) {
                            chunk.blocks[x][Chunk.CHUNK_SIZE - 1][z].setTop(true);
                        } else {
                            chunk.blocks[x][Chunk.CHUNK_SIZE - 1][z].setTop(false);
                        }
                    }

                    if (chunk.blocks[x + 1][Chunk.CHUNK_SIZE - 1][z].isOpaque()) {
                        chunk.blocks[x][Chunk.CHUNK_SIZE - 1][z].setRight(true);
                    } else {
                        chunk.blocks[x][Chunk.CHUNK_SIZE - 1][z].setRight(false);
                    }

                    if (chunk.blocks[x - 1][Chunk.CHUNK_SIZE - 1][z].isOpaque()) {
                        chunk.blocks[x][Chunk.CHUNK_SIZE - 1][z].setLeft(true);
                    } else {
                        chunk.blocks[x][Chunk.CHUNK_SIZE - 1][z].setLeft(false);
                    }

                    if (chunk.blocks[x][Chunk.CHUNK_SIZE - 2][z].isOpaque()) {
                        chunk.blocks[x][Chunk.CHUNK_SIZE - 1][z].setBottom(true);
                    } else {
                        chunk.blocks[x][Chunk.CHUNK_SIZE - 1][z].setBottom(false);
                    }

                    if (chunk.blocks[x][Chunk.CHUNK_SIZE - 1][z + 1].isOpaque()) {
                        chunk.blocks[x][Chunk.CHUNK_SIZE - 1][z].setFront(true);
                    } else {
                        chunk.blocks[x][Chunk.CHUNK_SIZE - 1][z].setFront(false);
                    }

                    if (chunk.blocks[x][Chunk.CHUNK_SIZE - 1][z - 1].isOpaque()) {
                        chunk.blocks[x][Chunk.CHUNK_SIZE - 1][z].setBack(true);
                    } else {
                        chunk.blocks[x][Chunk.CHUNK_SIZE - 1][z].setBack(false);
                    }
                }
            }

        }
    }

    private void updateBottomLeftBack() {
        if (leftChunk != null) {
            if (leftChunk.blocks[Chunk.CHUNK_SIZE - 1][0][0].isOpaque()) {
                chunk.blocks[0][0][0].setLeft(true);
            } else {
                chunk.blocks[0][0][0].setLeft(false);
            }
        }

        if (backChunk != null) {
            if (backChunk.blocks[0][0][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                chunk.blocks[0][0][0].setBack(true);
            } else {
                chunk.blocks[0][0][0].setBack(false);
            }
        }

        if (bottomChunk != null) {
            if (bottomChunk.blocks[0][Chunk.CHUNK_SIZE - 1][0].isOpaque()) {
                chunk.blocks[0][0][0].setBottom(true);
            } else {
                chunk.blocks[0][0][0].setBottom(false);
            }
        }

        if (chunk.blocks[1][0][0].isOpaque()) {
            chunk.blocks[0][0][0].setRight(true);
        } else {
            chunk.blocks[0][0][0].setRight(false);
        }

        if (chunk.blocks[0][1][0].isOpaque()) {
            chunk.blocks[0][0][0].setTop(true);
        } else {
            chunk.blocks[0][0][0].setTop(false);
        }

        if (chunk.blocks[0][0][1].isOpaque()) {
            chunk.blocks[0][0][0].setFront(true);
        } else {
            chunk.blocks[0][0][0].setFront(false);
        }
    }

    private void updateBottomLeftFront() {
        if (leftChunk != null) {
            if (leftChunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                chunk.blocks[0][0][Chunk.CHUNK_SIZE - 1].setLeft(true);
            } else {
                chunk.blocks[0][0][Chunk.CHUNK_SIZE - 1].setLeft(false);
            }
        }

        if (frontChunk != null) {
            if (frontChunk.blocks[0][0][0].isOpaque()) {
                chunk.blocks[0][0][Chunk.CHUNK_SIZE - 1].setFront(true);
            } else {
                chunk.blocks[0][0][Chunk.CHUNK_SIZE - 1].setFront(false);
            }
        }

        if (bottomChunk != null) {
            if (bottomChunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                chunk.blocks[0][0][Chunk.CHUNK_SIZE - 1].setBottom(true);
            } else {
                chunk.blocks[0][0][Chunk.CHUNK_SIZE - 1].setBottom(false);
            }
        }

        if (chunk.blocks[1][0][Chunk.CHUNK_SIZE - 1].isOpaque()) {
            chunk.blocks[0][0][Chunk.CHUNK_SIZE - 1].setRight(true);
        } else {
            chunk.blocks[0][0][Chunk.CHUNK_SIZE - 1].setRight(false);
        }

        if (chunk.blocks[0][1][Chunk.CHUNK_SIZE - 1].isOpaque()) {
            chunk.blocks[0][0][Chunk.CHUNK_SIZE - 1].setTop(true);
        } else {
            chunk.blocks[0][0][Chunk.CHUNK_SIZE - 1].setTop(false);
        }

        if (chunk.blocks[0][0][Chunk.CHUNK_SIZE - 2].isOpaque()) {
            chunk.blocks[0][0][Chunk.CHUNK_SIZE - 1].setBack(true);
        } else {
            chunk.blocks[0][0][Chunk.CHUNK_SIZE - 1].setBack(false);
        }
    }

    private void updateBottomRightBack() {
        if (rightChunk != null) {
            if (rightChunk.blocks[0][0][0].isOpaque()) {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][0][0].setRight(true);
            } else {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][0][0].setRight(false);
            }
        }

        if (backChunk != null) {
            if (backChunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][0][0].setBack(true);
            } else {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][0][0].setBack(false);
            }
        }

        if (bottomChunk != null) {
            if (bottomChunk.blocks[0][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][0][0].setBottom(true);
            } else {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][0][0].setBottom(false);
            }
        }

        if (chunk.blocks[Chunk.CHUNK_SIZE - 2][0][0].isOpaque()) {
            chunk.blocks[Chunk.CHUNK_SIZE - 1][0][0].setLeft(true);
        } else {
            chunk.blocks[Chunk.CHUNK_SIZE - 1][0][0].setLeft(false);
        }

        if (chunk.blocks[Chunk.CHUNK_SIZE - 1][1][0].isOpaque()) {
            chunk.blocks[Chunk.CHUNK_SIZE - 1][0][0].setTop(true);
        } else {
            chunk.blocks[Chunk.CHUNK_SIZE - 1][0][0].setTop(false);
        }

        if (chunk.blocks[Chunk.CHUNK_SIZE - 1][0][1].isOpaque()) {
            chunk.blocks[Chunk.CHUNK_SIZE - 1][0][0].setFront(true);
        } else {
            chunk.blocks[Chunk.CHUNK_SIZE - 1][0][0].setFront(false);
        }
    }

    private void updateBottomRightFront() {

        if (rightChunk != null) {
            if (rightChunk.blocks[0][0][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1].setRight(true);
            } else {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1].setRight(false);
            }
        }

        if (frontChunk != null) {
            if (frontChunk.blocks[Chunk.CHUNK_SIZE - 1][0][0].isOpaque()) {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1].setFront(true);
            } else {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1].setFront(false);
            }
        }

        if (bottomChunk != null) {
            if (bottomChunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1].setBottom(true);
            } else {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1].setBottom(false);
            }
        }

        if (chunk.blocks[Chunk.CHUNK_SIZE - 2][0][Chunk.CHUNK_SIZE - 1].isOpaque()) {
            chunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1].setLeft(true);
        } else {
            chunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1].setLeft(false);
        }

        if (chunk.blocks[Chunk.CHUNK_SIZE - 1][1][Chunk.CHUNK_SIZE - 1].isOpaque()) {
            chunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1].setTop(true);
        } else {
            chunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1].setTop(false);
        }

        if (chunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 2].isOpaque()) {
            chunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1].setBack(true);
        } else {
            chunk.blocks[Chunk.CHUNK_SIZE - 1][0][Chunk.CHUNK_SIZE - 1].setBack(false);
        }
    }

    private void updateBottomLeft() {
        boolean isValid = leftChunk != null;

        for (int z = 1; z < Chunk.CHUNK_SIZE - 1; z++) {
            if (isValid) {
                if (leftChunk.blocks[Chunk.CHUNK_SIZE - 1][0][z].isOpaque()) {
                    chunk.blocks[0][0][z].setLeft(true);
                } else {
                    chunk.blocks[0][0][z].setLeft(false);
                }
            }

            if (bottomChunk != null) {
                if (bottomChunk.blocks[0][Chunk.CHUNK_SIZE - 1][z].isOpaque()) {
                    chunk.blocks[0][0][z].setBottom(true);
                } else {
                    chunk.blocks[0][0][z].setBottom(false);
                }
            }

            if (chunk.blocks[1][0][z].isOpaque()) {
                chunk.blocks[0][0][z].setRight(true);
            } else {
                chunk.blocks[0][0][z].setRight(false);
            }

            if (chunk.blocks[0][1][z].isOpaque()) {
                chunk.blocks[0][0][z].setTop(true);
            } else {
                chunk.blocks[0][0][z].setTop(false);
            }

            if (chunk.blocks[0][0][z + 1].isOpaque()) {
                chunk.blocks[0][0][z].setFront(true);
            } else {
                chunk.blocks[0][0][z].setFront(false);
            }

            if (chunk.blocks[0][0][z - 1].isOpaque()) {
                chunk.blocks[0][0][z].setBack(true);
            } else {
                chunk.blocks[0][0][z].setBack(false);
            }
        }
    }

    private void updateBottomRight() {

        boolean isValid = rightChunk != null;

        for (int z = 1; z < Chunk.CHUNK_SIZE - 1; z++) {
            if (isValid) {
                if (rightChunk.blocks[0][0][z].isOpaque()) {
                    chunk.blocks[Chunk.CHUNK_SIZE - 1][0][z].setRight(true);
                } else {
                    chunk.blocks[Chunk.CHUNK_SIZE - 1][0][z].setRight(false);
                }
            }

            if (bottomChunk != null) {
                if (bottomChunk.blocks[Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1][z].isOpaque()) {
                    chunk.blocks[Chunk.CHUNK_SIZE - 1][0][z].setBottom(true);
                } else {
                    chunk.blocks[Chunk.CHUNK_SIZE - 1][0][z].setBottom(false);
                }
            }

            if (chunk.blocks[Chunk.CHUNK_SIZE - 2][0][z].isOpaque()) {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][0][z].setLeft(true);
            } else {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][0][z].setLeft(false);
            }

            if (chunk.blocks[Chunk.CHUNK_SIZE - 1][1][z].isOpaque()) {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][0][z].setTop(true);
            } else {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][0][z].setTop(false);
            }

            if (chunk.blocks[Chunk.CHUNK_SIZE - 1][0][z + 1].isOpaque()) {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][0][z].setFront(true);
            } else {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][0][z].setFront(false);
            }

            if (chunk.blocks[Chunk.CHUNK_SIZE - 1][0][z - 1].isOpaque()) {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][0][z].setBack(true);
            } else {
                chunk.blocks[Chunk.CHUNK_SIZE - 1][0][z].setBack(false);
            }
        }
    }

    private void updateBottomFront() {
        boolean isValid = frontChunk != null;

        for (int x = 1; x < Chunk.CHUNK_SIZE - 1; x++) {
            if (isValid) {
                if (frontChunk.blocks[x][0][0].isOpaque()) {
                    chunk.blocks[x][0][Chunk.CHUNK_SIZE - 1].setFront(true);
                } else {
                    chunk.blocks[x][0][Chunk.CHUNK_SIZE - 1].setFront(false);
                }
            }

            if (bottomChunk != null) {
                if (bottomChunk.blocks[x][Chunk.CHUNK_SIZE - 1][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                    chunk.blocks[x][0][Chunk.CHUNK_SIZE - 1].setBottom(true);
                } else {
                    chunk.blocks[x][0][Chunk.CHUNK_SIZE - 1].setBottom(false);
                }
            }

            if (chunk.blocks[x + 1][0][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                chunk.blocks[x][0][Chunk.CHUNK_SIZE - 1].setRight(true);
            } else {
                chunk.blocks[x][0][Chunk.CHUNK_SIZE - 1].setRight(false);
            }

            if (chunk.blocks[x - 1][0][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                chunk.blocks[x][0][Chunk.CHUNK_SIZE - 1].setLeft(true);
            } else {
                chunk.blocks[x][0][Chunk.CHUNK_SIZE - 1].setLeft(false);
            }

            if (chunk.blocks[x][1][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                chunk.blocks[x][0][Chunk.CHUNK_SIZE - 1].setTop(true);
            } else {
                chunk.blocks[x][0][Chunk.CHUNK_SIZE - 1].setTop(false);
            }

            if (chunk.blocks[x][0][Chunk.CHUNK_SIZE - 2].isOpaque()) {
                chunk.blocks[x][0][Chunk.CHUNK_SIZE - 1].setBack(true);
            } else {
                chunk.blocks[x][0][Chunk.CHUNK_SIZE - 1].setBack(false);
            }
        }
    }

    private void updateBottomBack() {
        boolean isValid = backChunk != null;

        for (int x = 1; x < Chunk.CHUNK_SIZE - 1; x++) {
            if (isValid) {
                if (backChunk.blocks[x][0][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                    chunk.blocks[x][0][0].setBack(true);
                } else {
                    chunk.blocks[x][0][0].setBack(false);
                }
            }

            if (bottomChunk != null) {
                if (bottomChunk.blocks[x][Chunk.CHUNK_SIZE - 1][0].isOpaque()) {
                    chunk.blocks[x][0][0].setBottom(true);
                } else {
                    chunk.blocks[x][0][0].setBottom(false);
                }
            }

            if (chunk.blocks[x + 1][0][0].isOpaque()) {
                chunk.blocks[x][0][0].setRight(true);
            } else {
                chunk.blocks[x][0][0].setRight(false);
            }

            if (chunk.blocks[x - 1][0][0].isOpaque()) {
                chunk.blocks[x][0][0].setLeft(true);
            } else {
                chunk.blocks[x][0][0].setLeft(false);
            }

            if (chunk.blocks[x][1][0].isOpaque()) {
                chunk.blocks[x][0][0].setTop(true);
            } else {
                chunk.blocks[x][0][0].setTop(false);
            }

            if (chunk.blocks[x][0][1].isOpaque()) {
                chunk.blocks[x][0][0].setFront(true);
            } else {
                chunk.blocks[x][0][0].setFront(false);
            }
        }
    }

    private void updateBottomSide() {
        for (int x = 1; x < Chunk.CHUNK_SIZE - 1; x++) {
            for (int z = 1; z < Chunk.CHUNK_SIZE - 1; z++) {
                if (chunk.blocks[x][0][z].is(Type.AIR) == false) {
                    if (bottomChunk != null) {
                        if (bottomChunk.blocks[x][Chunk.CHUNK_SIZE - 1][z].isOpaque()) {
                            chunk.blocks[x][0][z].setBottom(true);
                        } else {
                            chunk.blocks[x][0][z].setBottom(false);
                        }
                    }

                    if (chunk.blocks[x + 1][0][z].isOpaque()) {
                        chunk.blocks[x][0][z].setRight(true);
                    } else {
                        chunk.blocks[x][0][z].setRight(false);
                    }

                    if (chunk.blocks[x - 1][0][z].isOpaque()) {
                        chunk.blocks[x][0][z].setLeft(true);
                    } else {
                        chunk.blocks[x][0][z].setLeft(false);
                    }

                    if (chunk.blocks[x][1][z].isOpaque()) {
                        chunk.blocks[x][0][z].setTop(true);
                    } else {
                        chunk.blocks[x][0][z].setTop(false);
                    }

                    if (chunk.blocks[x][0][z + 1].isOpaque()) {
                        chunk.blocks[x][0][z].setFront(true);
                    } else {
                        chunk.blocks[x][0][z].setFront(false);
                    }

                    if (chunk.blocks[x][0][z - 1].isOpaque()) {
                        chunk.blocks[x][0][z].setBack(true);
                    } else {
                        chunk.blocks[x][0][z].setBack(false);
                    }
                }
            }
        }
    }

    private void updateLeftSide() {
        boolean isValid = leftChunk != null;

        for (int z = 1; z < Chunk.CHUNK_SIZE - 1; z++) {
            for (int y = 1; y < Chunk.CHUNK_SIZE - 1; y++) {
                if (chunk.blocks[0][y][z].is(Type.AIR) == false) {
                    if (isValid) {
                        if (leftChunk.blocks[Chunk.CHUNK_SIZE - 1][y][z].isOpaque()) {
                            chunk.blocks[0][y][z].setLeft(true);
                        } else {
                            chunk.blocks[0][y][z].setLeft(false);
                        }
                    }

                    if (chunk.blocks[0][y + 1][z].isOpaque()) {
                        chunk.blocks[0][y][z].setTop(true);
                    } else {
                        chunk.blocks[0][y][z].setTop(false);
                    }

                    if (chunk.blocks[0][y - 1][z].isOpaque()) {
                        chunk.blocks[0][y][z].setBottom(true);
                    } else {
                        chunk.blocks[0][y][z].setBottom(false);
                    }

                    if (chunk.blocks[0][y][z + 1].isOpaque()) {
                        chunk.blocks[0][y][z].setFront(true);
                    } else {
                        chunk.blocks[0][y][z].setFront(false);
                    }

                    if (chunk.blocks[0][y][z - 1].isOpaque()) {
                        chunk.blocks[0][y][z].setBack(true);
                    } else {
                        chunk.blocks[0][y][z].setBack(false);
                    }

                    if (chunk.blocks[1][y][z].isOpaque()) {
                        chunk.blocks[0][y][z].setRight(true);
                    } else {
                        chunk.blocks[0][y][z].setRight(false);
                    }
                }
            }
        }
    }

    private void updateRightSide() {
        boolean isValid = rightChunk != null;

        for (int z = 1; z < Chunk.CHUNK_SIZE - 1; z++) {
            for (int y = 1; y < Chunk.CHUNK_SIZE - 1; y++) {
                if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y][z].is(Type.AIR) == false) {
                    if (isValid) {
                        if (rightChunk.blocks[0][y][z].isOpaque()) {
                            chunk.blocks[Chunk.CHUNK_SIZE - 1][y][z].setRight(true);
                        } else {
                            chunk.blocks[Chunk.CHUNK_SIZE - 1][y][z].setRight(false);
                        }
                    }

                    if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y + 1][z].isOpaque()) {
                        chunk.blocks[Chunk.CHUNK_SIZE - 1][y][z].setTop(true);
                    } else {
                        chunk.blocks[Chunk.CHUNK_SIZE - 1][y][z].setTop(false);
                    }

                    if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y - 1][z].isOpaque()) {
                        chunk.blocks[Chunk.CHUNK_SIZE - 1][y][z].setBottom(true);
                    } else {
                        chunk.blocks[Chunk.CHUNK_SIZE - 1][y][z].setBottom(false);
                    }

                    if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y][z + 1].isOpaque()) {
                        chunk.blocks[Chunk.CHUNK_SIZE - 1][y][z].setFront(true);
                    } else {
                        chunk.blocks[Chunk.CHUNK_SIZE - 1][y][z].setFront(false);
                    }

                    if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y][z - 1].isOpaque()) {
                        chunk.blocks[Chunk.CHUNK_SIZE - 1][y][z].setBack(true);
                    } else {
                        chunk.blocks[Chunk.CHUNK_SIZE - 1][y][z].setBack(false);
                    }

                    if (chunk.blocks[Chunk.CHUNK_SIZE - 2][y][z].isOpaque()) {
                        chunk.blocks[Chunk.CHUNK_SIZE - 1][y][z].setLeft(true);
                    } else {
                        chunk.blocks[Chunk.CHUNK_SIZE - 1][y][z].setLeft(false);
                    }
                }
            }
        }
    }

    private void updateFrontSide() {
        boolean isValid = frontChunk != null;

        for (int x = 1; x < Chunk.CHUNK_SIZE - 1; x++) {
            for (int y = 1; y < Chunk.CHUNK_SIZE - 1; y++) {
                if (chunk.blocks[x][y][Chunk.CHUNK_SIZE - 1].is(Type.AIR) == false) {
                    if (isValid) {
                        if (frontChunk.blocks[x][y][0].isOpaque()) {
                            chunk.blocks[x][y][Chunk.CHUNK_SIZE - 1].setFront(true);
                        } else {
                            chunk.blocks[x][y][Chunk.CHUNK_SIZE - 1].setFront(false);
                        }
                    }

                    if (chunk.blocks[x + 1][y][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                        chunk.blocks[x][y][Chunk.CHUNK_SIZE - 1].setRight(true);
                    } else {
                        chunk.blocks[x][y][Chunk.CHUNK_SIZE - 1].setRight(false);
                    }

                    if (chunk.blocks[x - 1][y][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                        chunk.blocks[x][y][Chunk.CHUNK_SIZE - 1].setLeft(true);
                    } else {
                        chunk.blocks[x][y][Chunk.CHUNK_SIZE - 1].setLeft(false);
                    }

                    if (chunk.blocks[x][y + 1][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                        chunk.blocks[x][y][Chunk.CHUNK_SIZE - 1].setTop(true);
                    } else {
                        chunk.blocks[x][y][Chunk.CHUNK_SIZE - 1].setTop(false);
                    }

                    if (chunk.blocks[x][y - 1][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                        chunk.blocks[x][y][Chunk.CHUNK_SIZE - 1].setBottom(true);
                    } else {
                        chunk.blocks[x][y][Chunk.CHUNK_SIZE - 1].setBottom(false);
                    }

                    if (chunk.blocks[x][y][Chunk.CHUNK_SIZE - 2].isOpaque()) {
                        chunk.blocks[x][y][Chunk.CHUNK_SIZE - 1].setBack(true);
                    } else {
                        chunk.blocks[x][y][Chunk.CHUNK_SIZE - 1].setBack(false);
                    }
                }
            }
        }
    }

    private void updateBackSide() {
        boolean isValid = backChunk != null;

        for (int x = 1; x < Chunk.CHUNK_SIZE - 1; x++) {
            for (int y = 1; y < Chunk.CHUNK_SIZE - 1; y++) {
                if (chunk.blocks[x][y][0].is(Type.AIR) == false) {
                    if (isValid) {
                        if (backChunk.blocks[x][y][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                            chunk.blocks[x][y][0].setBack(true);
                        } else {
                            chunk.blocks[x][y][0].setBack(false);
                        }
                    }

                    if (chunk.blocks[x + 1][y][0].isOpaque()) {
                        chunk.blocks[x][y][0].setRight(true);
                    } else {
                        chunk.blocks[x][y][0].setRight(false);
                    }

                    if (chunk.blocks[x - 1][y][0].isOpaque()) {
                        chunk.blocks[x][y][0].setLeft(true);
                    } else {
                        chunk.blocks[x][y][0].setLeft(false);
                    }

                    if (chunk.blocks[x][y + 1][0].isOpaque()) {
                        chunk.blocks[x][y][0].setTop(true);
                    } else {
                        chunk.blocks[x][y][0].setTop(false);
                    }

                    if (chunk.blocks[x][y - 1][0].isOpaque()) {
                        chunk.blocks[x][y][0].setBottom(true);
                    } else {
                        chunk.blocks[x][y][0].setBottom(false);
                    }

                    if (chunk.blocks[x][y][1].isOpaque()) {
                        chunk.blocks[x][y][0].setFront(true);
                    } else {
                        chunk.blocks[x][y][0].setFront(false);
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
                    if (chunk.blocks[x][y][z].isBack()) {
                        vertexCount += 6;
                    }
                    if (chunk.blocks[x][y][z].isBottom()) {
                        vertexCount += 6;
                    }
                    if (chunk.blocks[x][y][z].isFront()) {
                        vertexCount += 6;
                    }
                    if (chunk.blocks[x][y][z].isLeft()) {
                        vertexCount += 6;
                    }
                    if (chunk.blocks[x][y][z].isRight()) {
                        vertexCount += 6;
                    }
                    if (chunk.blocks[x][y][z].isTop()) {
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
            if (chunk.blocks[0][y][0].is(Type.AIR) == false) {
                if (backIsValid) {
                    if (backChunk.blocks[0][y][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                        chunk.blocks[0][y][0].setBack(true);
                    } else {
                        chunk.blocks[0][y][0].setBack(false);
                    }
                }

                if (leftIsValid) {
                    if (leftChunk.blocks[Chunk.CHUNK_SIZE - 1][y][0].isOpaque()) {
                        chunk.blocks[0][y][0].setLeft(true);
                    } else {
                        chunk.blocks[0][y][0].setLeft(false);
                    }
                }

                if (chunk.blocks[1][y][0].isOpaque()) {
                    chunk.blocks[0][y][0].setRight(true);
                } else {
                    chunk.blocks[0][y][0].setRight(false);
                }

                if (chunk.blocks[0][y + 1][0].isOpaque()) {
                    chunk.blocks[0][y][0].setTop(true);
                } else {
                    chunk.blocks[0][y][0].setTop(false);
                }

                if (chunk.blocks[0][y - 1][0].isOpaque()) {
                    chunk.blocks[0][y][0].setBottom(true);
                } else {
                    chunk.blocks[0][y][0].setBottom(false);
                }

                if (chunk.blocks[0][y][1].isOpaque()) {
                    chunk.blocks[0][y][0].setFront(true);
                } else {
                    chunk.blocks[0][y][0].setFront(false);
                }
            }
        }
    }

    private void updateLeftFront() {
        boolean frontIsValid = frontChunk != null;
        boolean leftIsValid = leftChunk != null;

        for (int y = 1; y < Chunk.CHUNK_SIZE - 1; y++) {
            if (chunk.blocks[0][y][Chunk.CHUNK_SIZE - 1].is(Type.AIR) == false) {
                if (frontIsValid) {
                    if (frontChunk.blocks[0][y][0].isOpaque()) {
                        chunk.blocks[0][y][Chunk.CHUNK_SIZE - 1].setFront(true);
                    } else {
                        chunk.blocks[0][y][Chunk.CHUNK_SIZE - 1].setFront(false);
                    }
                }

                if (leftIsValid) {
                    if (leftChunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                        chunk.blocks[0][y][Chunk.CHUNK_SIZE - 1].setLeft(true);
                    } else {
                        chunk.blocks[0][y][Chunk.CHUNK_SIZE - 1].setLeft(false);
                    }
                }

                if (chunk.blocks[1][y][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                    chunk.blocks[0][y][Chunk.CHUNK_SIZE - 1].setRight(true);
                } else {
                    chunk.blocks[0][y][Chunk.CHUNK_SIZE - 1].setRight(false);
                }

                if (chunk.blocks[0][y + 1][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                    chunk.blocks[0][y][Chunk.CHUNK_SIZE - 1].setTop(true);
                } else {
                    chunk.blocks[0][y][Chunk.CHUNK_SIZE - 1].setTop(false);
                }

                if (chunk.blocks[0][y - 1][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                    chunk.blocks[0][y][Chunk.CHUNK_SIZE - 1].setBottom(true);
                } else {
                    chunk.blocks[0][y][Chunk.CHUNK_SIZE - 1].setBottom(false);
                }

                if (chunk.blocks[0][y][Chunk.CHUNK_SIZE - 2].isOpaque()) {
                    chunk.blocks[0][y][Chunk.CHUNK_SIZE - 1].setBack(true);
                } else {
                    chunk.blocks[0][y][Chunk.CHUNK_SIZE - 1].setBack(false);
                }
            }
        }
    }

    private void updateRightBack() {
        boolean rightIsValid = rightChunk != null;
        boolean backIsValid = backChunk != null;

        for (int y = 1; y < Chunk.CHUNK_SIZE - 1; y++) {
            if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y][0].is(Type.AIR) == false) {
                if (rightIsValid) {
                    if (rightChunk.blocks[0][y][0].isOpaque()) {
                        chunk.blocks[Chunk.CHUNK_SIZE - 1][y][0].setRight(true);
                    } else {
                        chunk.blocks[Chunk.CHUNK_SIZE - 1][y][0].setRight(false);
                    }
                }

                if (backIsValid) {
                    if (backChunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                        chunk.blocks[Chunk.CHUNK_SIZE - 1][y][0].setBack(true);
                    } else {
                        chunk.blocks[Chunk.CHUNK_SIZE - 1][y][0].setBack(false);
                    }
                }

                if (chunk.blocks[Chunk.CHUNK_SIZE - 2][y][0].isOpaque()) {
                    chunk.blocks[Chunk.CHUNK_SIZE - 1][y][0].setLeft(true);
                } else {
                    chunk.blocks[Chunk.CHUNK_SIZE - 1][y][0].setLeft(false);
                }

                if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y + 1][0].isOpaque()) {
                    chunk.blocks[Chunk.CHUNK_SIZE - 1][y][0].setTop(true);
                } else {
                    chunk.blocks[Chunk.CHUNK_SIZE - 1][y][0].setTop(false);
                }

                if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y - 1][0].isOpaque()) {
                    chunk.blocks[Chunk.CHUNK_SIZE - 1][y][0].setBottom(true);
                } else {
                    chunk.blocks[Chunk.CHUNK_SIZE - 1][y][0].setBottom(false);
                }

                if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y][1].isOpaque()) {
                    chunk.blocks[Chunk.CHUNK_SIZE - 1][y][0].setFront(true);
                } else {
                    chunk.blocks[Chunk.CHUNK_SIZE - 1][y][0].setFront(false);
                }
            }
        }
    }

    private void updateRightFront() {
        boolean rightIsValid = rightChunk != null;
        boolean frontIsValid = frontChunk != null;

        for (int y = 1; y < Chunk.CHUNK_SIZE - 1; y++) {
            if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1].is(Type.AIR) == false) {
                if (rightIsValid) {
                    if (rightChunk.blocks[0][y][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                        chunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1].setRight(true);
                    } else {
                        chunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1].setRight(false);
                    }
                }

                if (frontIsValid) {
                    if (frontChunk.blocks[Chunk.CHUNK_SIZE - 1][y][0].isOpaque()) {
                        chunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1].setFront(true);
                    } else {
                        chunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1].setFront(false);
                    }
                }

                if (chunk.blocks[Chunk.CHUNK_SIZE - 2][y][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                    chunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1].setLeft(true);
                } else {
                    chunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1].setLeft(false);
                }

                if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y + 1][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                    chunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1].setTop(true);
                } else {
                    chunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1].setTop(false);
                }

                if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y - 1][Chunk.CHUNK_SIZE - 1].isOpaque()) {
                    chunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1].setBottom(true);
                } else {
                    chunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1].setBottom(false);
                }

                if (chunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 2].isOpaque()) {
                    chunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1].setBack(true);
                } else {
                    chunk.blocks[Chunk.CHUNK_SIZE - 1][y][Chunk.CHUNK_SIZE - 1].setBack(false);
                }

            }
        }
    }

    public void setChunk(Chunk chunk) {
        this.chunk = chunk;
    }

}
