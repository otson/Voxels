/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels.ChunkManager;

import com.ning.compress.lzf.LZFEncoder;
import de.ruedigermoeller.serialization.FSTObjectOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.FloatBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.BufferUtils;
import static voxels.ChunkManager.Chunk.CHUNK_HEIGHT;
import static voxels.ChunkManager.Chunk.CHUNK_WIDTH;
import static voxels.Voxels.WaterOffs;
import static voxels.Voxels.getCurrentChunkXId;
import static voxels.Voxels.getCurrentChunkZId;

/**
 *
 * @author otso
 */
public class ChunkMaker extends Thread {

    private static int vertexSize = 3;
    private static int normalSize = 3;
    private static int texSize = 2;
    private static int colorSize = 3;

    private boolean[][][] top = new boolean[Chunk.CHUNK_WIDTH][Chunk.CHUNK_HEIGHT][Chunk.CHUNK_WIDTH];
    private boolean[][][] bottom = new boolean[Chunk.CHUNK_WIDTH][Chunk.CHUNK_HEIGHT][Chunk.CHUNK_WIDTH];
    private boolean[][][] left = new boolean[Chunk.CHUNK_WIDTH][Chunk.CHUNK_HEIGHT][Chunk.CHUNK_WIDTH];
    private boolean[][][] right = new boolean[Chunk.CHUNK_WIDTH][Chunk.CHUNK_HEIGHT][Chunk.CHUNK_WIDTH];
    private boolean[][][] front = new boolean[Chunk.CHUNK_WIDTH][Chunk.CHUNK_HEIGHT][Chunk.CHUNK_WIDTH];
    private boolean[][][] back = new boolean[Chunk.CHUNK_WIDTH][Chunk.CHUNK_HEIGHT][Chunk.CHUNK_WIDTH];

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

    private int threadId;
    private Data[] data;

    private Data updateData;

    boolean update;

    public ChunkMaker(int threadId, Data[] data, int chunkX, int chunkZ, int xOff, int zOff, ConcurrentHashMap<Integer, byte[]> map) {
        this.threadId = threadId;
        this.xOff = xOff;
        this.zOff = zOff;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.map = map;
        this.data = data;

        update = false;

    }

    public ChunkMaker(ConcurrentHashMap<Integer, byte[]> map, Chunk chunk) {
        this.chunk = chunk;
        this.map = map;
        update = true;
    }

    @Override
    public void run() {
        if (update) {
            drawChunkVBO(chunk, chunk.xCoordinate, chunk.zCoordinate);
            map.put(new Pair(chunk.xId, chunk.zId).hashCode(), toByte(chunk));
            updateData = new Data(chunk.xId, chunk.zId, chunk.getVertices(), vertexData, normalData, texData);
        }
        else if (!map.containsKey(new Pair(chunkX, chunkZ).hashCode())) {
            chunk = new Chunk(chunkX, chunkZ);
            drawChunkVBO(chunk, xOff, zOff);

            //handles.put(new Pair(chunkX, chunkZ).hashCode(), new Handle(chunk.getVboVertexHandle(), chunk.getVboNormalHandle(), chunk.getVboTexHandle(), chunk.getVertices()));
            map.put(new Pair(chunkX, chunkZ).hashCode(), toByte(chunk));
            data[threadId] = new Data(chunkX, chunkZ, chunk.getVertices(), vertexData, normalData, texData);
        }
        else
            System.out.println("Already contains");
    }

    public void update() {
        drawChunkVBO(chunk, chunk.xCoordinate, chunk.zCoordinate);
        map.put(new Pair(chunk.xId, chunk.zId).hashCode(), toByte(chunk));
        updateData = new Data(chunk.xId, chunk.zId, chunk.getVertices(), vertexData, normalData, texData);
    }

    public int calculateGroundVertices(Chunk chunk, float x, float y, float z, float xOff, float yOff, float zOff, float size) {
        int zMax = Chunk.CHUNK_WIDTH - 1;
        int xMax = Chunk.CHUNK_WIDTH - 1;
        int yMax = Chunk.CHUNK_HEIGHT - 1;
        int xx = Math.round(x);
        int yy = Math.round(y);
        int zz = Math.round(z);
        boolean render = false;
        int returnVertices = 0;
        int difference = 0;
        int[][] maxHeights = chunk.maxHeights;

        // front face
        if (z == zMax)
            render = true;
        else {
            difference = maxHeights[xx][zz] - maxHeights[xx][zz + 1];
        }
        if (render || chunk.blocks[xx][yy][zz + 1].is(Type.AIR) || chunk.blocks[xx][yy][zz + 1].is(Type.WATER) && (maxHeights[xx][zz] - difference < y && y <= maxHeights[xx][zz])) {
            front[xx][yy][zz] = true;
            returnVertices += 6;
        }
        else
            front[xx][yy][zz] = false;

        // left face
        render = false;
        if (x == 0)
            render = true;
        else {
            difference = maxHeights[xx][zz] - maxHeights[xx - 1][zz];
        }
        if (render || chunk.blocks[xx - 1][yy][zz].is(Type.AIR) || chunk.blocks[xx - 1][yy][zz].is(Type.WATER) && (maxHeights[xx][zz] - difference < y && y <= maxHeights[xx][zz])) {
            left[xx][yy][zz] = true;
            returnVertices += 6;
        }
        else
            left[xx][yy][zz] = false;

        // back face
        render = false;
        if (z == 0)
            render = true;
        else {
            difference = maxHeights[xx][zz] - maxHeights[xx][zz - 1];
        }
        if (render || chunk.blocks[xx][yy][zz - 1].is(Type.AIR) || chunk.blocks[xx][yy][zz - 1].is(Type.WATER) && (maxHeights[xx][zz] - difference < y && y <= maxHeights[xx][zz])) {
            back[xx][yy][zz] = true;
            returnVertices += 6;
        }
        else
            back[xx][yy][zz] = false;

        // right face
        render = false;
        if (x == xMax)
            render = true;
        else {
            difference = maxHeights[xx][zz] - maxHeights[xx + 1][zz];
        }
        if (render || chunk.blocks[xx + 1][yy][zz].is(Type.AIR) || chunk.blocks[xx + 1][yy][zz].is(Type.WATER) && (maxHeights[xx][zz] - difference < y && y <= maxHeights[xx][zz])) {
            right[xx][yy][zz] = true;
            returnVertices += 6;
        }
        else
            right[xx][yy][zz] = false;

        // top face
        render = false;
        if (y == yMax)
            render = true;
        if (render || chunk.blocks[xx][yy + 1][zz].is(Type.AIR) || chunk.blocks[xx][yy + 1][zz].is(Type.AIR) && maxHeights[xx][zz] == yy) {
            top[xx][yy][zz] = true;
            returnVertices += 6;
        }
        else
            top[xx][yy][zz] = false;

        //bottom face
        render = false;
        if (y == 0)
            render = true;
        if (render || chunk.blocks[xx][yy - 1][zz].is(Type.AIR) || chunk.blocks[xx][yy - 1][zz].is(Type.AIR) && maxHeights[xx][zz] + 1 == yy) {
            bottom[xx][yy][zz] = true;
            returnVertices += 6;
        }
        else
            bottom[xx][yy][zz] = false;

        return returnVertices;
    }

    public int calculateWaterVertices(Chunk chunk, float x, float y, float z, float xOff, float yOff, float zOff, float size) {
        int yMax = Chunk.CHUNK_HEIGHT - 1;
        int xx = Math.round(x);
        int yy = Math.round(y);
        int zz = Math.round(z);
        int returnVertices = 0;
        boolean render = false;

        // top face
        if (y == yMax)
            render = true;
        if (render || chunk.blocks[xx][yy + 1][zz].is(Type.AIR) && y == Chunk.WATER_HEIGHT) {
            top[xx][yy][zz] = true;
            returnVertices += 6;
        }
        else
            top[xx][yy][zz] = false;

        return returnVertices;

    }

    public void drawChunkVBO(Chunk chunk, int xOff, int zOff) {
        int vertices = 0;
        int size = 1;
        for (int x = 0; x < chunk.blocks.length; x++) {
            for (int z = 0; z < chunk.blocks[x][0].length; z++) {
                for (int y = 0; y < chunk.blocks[x].length; y++) {
                    if (chunk.blocks[x][y][z].is(Type.DIRT)) {
                        vertices += calculateGroundVertices(chunk, x, y, z, getCurrentChunkXId() * chunk.blocks.length + xOff, 0, getCurrentChunkZId() * chunk.blocks.length + zOff, 1);
                    }
                    else if (chunk.blocks[x][y][z].is(Type.WATER)) {
                        vertices += calculateWaterVertices(chunk, x, y, z, getCurrentChunkXId() * chunk.blocks.length + xOff, 0, getCurrentChunkZId() * chunk.blocks.length + zOff, 1);
                    }
                }
            }
        }
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
                        if (back[x][y][z]) {
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
                        if (right[x][y][z]) {

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
                        if (left[x][y][z]) {
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
                        if (bottom[x][y][z]) {
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
                        if (top[x][y][z]) {

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

    private boolean[][][] initTopArray() {
        top = new boolean[Chunk.CHUNK_WIDTH][][];
        for (int x = 0; x < top.length; x++) {
            top[x] = new boolean[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < top[x].length; y++) {
                top[x][y] = new boolean[CHUNK_WIDTH];
            }
        }
        return top;
    }

    private boolean[][][] initBottomArray() {
        bottom = new boolean[Chunk.CHUNK_WIDTH][][];
        for (int x = 0; x < bottom.length; x++) {
            bottom[x] = new boolean[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < bottom[x].length; y++) {
                bottom[x][y] = new boolean[CHUNK_WIDTH];
            }
        }
        return bottom;
    }

    private boolean[][][] initLeftArray() {
        left = new boolean[Chunk.CHUNK_WIDTH][][];
        for (int x = 0; x < left.length; x++) {
            left[x] = new boolean[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < left[x].length; y++) {
                left[x][y] = new boolean[CHUNK_WIDTH];
            }
        }
        return left;
    }

    private boolean[][][] initRightArray() {
        right = new boolean[Chunk.CHUNK_WIDTH][][];
        for (int x = 0; x < right.length; x++) {
            right[x] = new boolean[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < right[x].length; y++) {
                right[x][y] = new boolean[CHUNK_WIDTH];
            }
        }
        return right;
    }

    private boolean[][][] initFrontArray() {
        front = new boolean[Chunk.CHUNK_WIDTH][][];
        for (int x = 0; x < front.length; x++) {
            front[x] = new boolean[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < front[x].length; y++) {
                front[x][y] = new boolean[CHUNK_WIDTH];
            }
        }
        return front;
    }

    private boolean[][][] initBackArray() {
        back = new boolean[Chunk.CHUNK_WIDTH][][];
        for (int x = 0; x < back.length; x++) {
            back[x] = new boolean[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < back[x].length; y++) {
                back[x][y] = new boolean[CHUNK_WIDTH];
            }
        }
        return back;
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

//    private void setActiveBlocks() {
//
//        int activeBlocks = 0;
//        // set blocks that are inside the chunk
//        for (int x = 1; x < chunk.blocks.length - 1; x++) {
//            for (int y = 1; y < chunk.blocks[x].length - 1; y++) {
//                for (int z = 1; z < chunk.blocks[x][y].length - 1; z++) {
//                    // if air, it is inactive
//                    if (chunk.blocks[x][y][z].is(Type.AIR))
//                        chunk.blocks[x][y][z].setActive(false);
//                    // if dirt, if it surrounded by 6 dirt blocks, make it inactive
//                    else if (chunk.blocks[x][y][z].is(Type.DIRT)) {
//                        if (chunk.blocks[x + 1][y][z].is(Type.DIRT) && chunk.blocks[x - 1][y][z].is(Type.DIRT) && chunk.blocks[x][y + 1][z].is(Type.DIRT) && chunk.blocks[x][y - 1][z].is(Type.DIRT) && chunk.blocks[x][y][z + 1].is(Type.DIRT) && chunk.blocks[x][y][z - 1].is(Type.DIRT))
//                            chunk.blocks[x][y][z].setActive(false);
//                        else {
//                            // set active sides to be rendered, rendered if the side is not touching dirt
//                            chunk.blocks[x][y][z].setActive(true);
//                            if (chunk.blocks[x + 1][y][z].isOpaque()) {
//                                top[x][y][z] = true;
//                            }
//                            if (chunk.blocks[x - 1][y][z].isOpaque()) {
//                                left[x][y][z] = true;
//
//                            }
//                            if (chunk.blocks[x][y + 1][z].isOpaque()) {
//                                top[x][y][z] = true;
//
//                            }
//                            if (chunk.blocks[x][y - 1][z].isOpaque()) {
//                                bottom[x][y][z] = true;
//
//                            }
//                            if (chunk.blocks[x][y][z + 1].isOpaque()) {
//                                front[x][y][z] = true;
//
//                            }
//                            if (chunk.blocks[x][y][z - 1].isOpaque()) {
//                                back[x][y][z] = true;
//
//                            }
//                        }
//                    }
//                    else if (chunk.blocks[x][y][z].is(Type.WATER))
//                        // if water, if the block above it is not water, make it active
//                        if (chunk.blocks[x][y + 1][z].is(Type.WATER) == false) {
//                            chunk.blocks[x][y][z].setActive(true);
//                            top[x][y][z] = true;
//
//                        }
//
//                    if (chunk.blocks[x][y][z].isActive())
//                        activeBlocks++;
//                }
//            }
//        }
//
//        System.out.println("Activated blocks: " + activeBlocks);
//    }
//
//    private void updateAllSides() {
//
//        rightChunk = Voxels.chunkManager.getChunk(chunk.xId + 1, chunk.zId);
//        leftChunk = Voxels.chunkManager.getChunk(chunk.xId - 1, chunk.zId);
//        backChunk = Voxels.chunkManager.getChunk(chunk.xId, chunk.zId - 1);
//        frontChunk = Voxels.chunkManager.getChunk(chunk.xId + 1, chunk.xId + 1);
//        
//        updateTopLeftBack();
//        updateTopLeftFront();
//        updateTopRightBack();
//        updateTopRightFront();
//
//        updateBottomLeftBack();
//        updateBottomLeftFront();
//        updateBottomRightBack();
//        updateBottomRightFront();
//
//        updateTopLeft();
//        updateTopRight();
//        updateTopFront();
//        updateTopBack();
//
//        updateBottomLeft();
//        updateBottomRight();
//        updateBottomFront();
//        updateBottomBack();
//
//        updateLeftBack();
//        updateLeftFront();
//        updateRightBack();
//        updateRightFront();
//
//        updateLeftSide();
//        updateRightSide();
//        updateTopSide();
//        updateBottomSide();
//        updateFrontSide();
//        updateBackSide();
//    }
//    
//    private void updateTopLeftBack() {
//        if (leftChunk != null) {
//            if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
//                left[0][Chunk.CHUNK_WIDTH - 1][0] = true;
//        }
//        if (backChunk != null) {
//            if (backChunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                back[0][Chunk.CHUNK_WIDTH - 1][0] = true;
//        }
//
//        // add code for top chunk check when chunks are made smaller
//        if (chunk.blocks[1][Chunk.CHUNK_WIDTH - 1][0].isOpaque())
//            right[0][Chunk.CHUNK_WIDTH - 1][0] = true;
//
//        if (chunk.blocks[0][Chunk.CHUNK_WIDTH - 2][0].isOpaque())
//            bottom[0][Chunk.CHUNK_WIDTH - 1][0] = true;
//
//        if (chunk.blocks[0][Chunk.CHUNK_WIDTH - 1][1].isOpaque())
//            front[0][Chunk.CHUNK_WIDTH - 1][0] = true;
//
//    }
//
//    private void updateTopLeftFront() {
//        if (leftChunk != null) {
//            if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                blocks[0][Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_WIDTH - 1].setLeft(true);
//        }
//        if (frontChunk != null) {
//            if (frontChunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
//                blocks[0][Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_WIDTH - 1].setFront(true);
//        }
//
//        // add code for top chunk check when chunks are made smaller
//        if (chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 2].isOpaque())
//            blocks[0][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setBack(true);
//
//        if (chunk.blocks[0][Chunk.CHUNK_HEIGHT - 2][Chunk.CHUNK_WIDTH - 1].isOpaque())
//            blocks[0][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setBottom(true);
//
//        if (chunk.blocks[1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
//            blocks[0][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setRight(true);
//    }
//
//    private void updateTopRightBack() {
//        if (backChunk != null) {
//            if (backChunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][0].setBack(true);
//        }
//        if (rightChunk != null) {
//            if (rightChunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
//                blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][0].setRight(true);
//        }
//
//        if (chunk.blocks[Chunk.CHUNK_WIDTH - 2][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
//            blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][0].setLeft(true);
//
//        if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 2][0].isOpaque())
//            blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][0].setBottom(true);
//
//        if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][1].isOpaque())
//            blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][0].setFront(true);
//    }
//
//    private void updateTopRightFront() {
//        if (rightChunk != null) {
//            if (rightChunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setRight(true);
//        }
//        if (frontChunk != null) {
//            if (frontChunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
//                blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setFront(true);
//        }
//
//        blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setTop(true);
//
//        if (chunk.blocks[Chunk.CHUNK_WIDTH - 2][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
//            blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setLeft(true);
//
//        if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 2][Chunk.CHUNK_WIDTH - 1].isOpaque())
//            blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setBottom(true);
//
//        if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 2].isOpaque())
//            blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setBack(true);
//    }
//
//    private void updateTopLeft() {
//        boolean isValid = leftChunk != null;
//
//        for (int z = 1; z < Chunk.CHUNK_WIDTH - 1; z++) {
//            if (isValid)
//                if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][z].isOpaque())
//                    blocks[0][Chunk.CHUNK_HEIGHT - 1][z].setLeft(true);
//
//            blocks[0][Chunk.CHUNK_HEIGHT - 1][z].setTop(true);
//
//            if (chunk.blocks[1][Chunk.CHUNK_HEIGHT - 1][z].isOpaque())
//                blocks[0][Chunk.CHUNK_HEIGHT - 1][z].setRight(true);
//
//            if (chunk.blocks[0][Chunk.CHUNK_HEIGHT - 2][z].isOpaque())
//                blocks[0][Chunk.CHUNK_HEIGHT - 1][z].setBottom(true);
//
//            if (chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][z + 1].isOpaque())
//                blocks[0][Chunk.CHUNK_HEIGHT - 1][z].setFront(true);
//
//            if (chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][z - 1].isOpaque())
//                blocks[0][Chunk.CHUNK_HEIGHT - 1][z].setBack(true);
//        }
//    }
//
//    private void updateTopRight() {
//        boolean isValid = rightChunk != null;
//
//        for (int z = 1; z < Chunk.CHUNK_WIDTH - 1; z++) {
//            if (isValid)
//                if (rightChunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][z].isOpaque())
//                    blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][z].setRight(true);
//
//            blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][z].setTop(true);
//
//            if (chunk.blocks[Chunk.CHUNK_WIDTH - 2][Chunk.CHUNK_HEIGHT - 1][z].isOpaque())
//                blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][z].setLeft(true);
//
//            if (chunk.blocks[0][Chunk.CHUNK_HEIGHT - 2][z].isOpaque())
//                blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][z].setBottom(true);
//
//            if (chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][z + 1].isOpaque())
//                blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][z].setFront(true);
//
//            if (chunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][z - 1].isOpaque())
//                blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][z].setBack(true);
//        }
//    }
//
//    private void updateTopFront() {
//        boolean isValid = frontChunk != null;
//
//        for (int x = 1; x < Chunk.CHUNK_WIDTH - 1; x++) {
//            if (isValid)
//                if (frontChunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
//                    blocks[x][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setFront(true);
//
//            blocks[x][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setTop(true);
//
//            if (chunk.blocks[x + 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                blocks[x][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setRight(true);
//
//            if (chunk.blocks[x - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                blocks[x][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setLeft(true);
//
//            if (chunk.blocks[x][Chunk.CHUNK_HEIGHT - 2][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                blocks[x][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setBottom(true);
//
//            if (chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 2].isOpaque())
//                blocks[x][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setBack(true);
//        }
//    }
//
//    private void updateTopBack() {
//        boolean isValid = backChunk != null;
//
//        for (int x = 1; x < Chunk.CHUNK_WIDTH - 1; x++) {
//            if (isValid)
//                if (backChunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                    blocks[x][Chunk.CHUNK_HEIGHT - 1][0].setBack(true);
//
//            blocks[x][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setTop(true);
//
//            if (chunk.blocks[x + 1][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
//                blocks[x][Chunk.CHUNK_HEIGHT - 1][0].setRight(true);
//
//            if (chunk.blocks[x - 1][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
//                blocks[x][Chunk.CHUNK_HEIGHT - 1][0].setLeft(true);
//
//            if (chunk.blocks[x][Chunk.CHUNK_HEIGHT - 2][0].isOpaque())
//                blocks[x][Chunk.CHUNK_HEIGHT - 1][0].setBottom(true);
//
//            if (chunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][1].isOpaque())
//                blocks[x][Chunk.CHUNK_HEIGHT - 1][0].setFront(true);
//        }
//
//    }
//
//    private void updateTopSide() {
//        for (int x = 1; x < Chunk.CHUNK_WIDTH - 1; x++) {
//            for (int z = 1; z < Chunk.CHUNK_WIDTH - 1; z++) {
//                if (blocks[x][Chunk.CHUNK_WIDTH - 1][z].is(Type.AIR) == false) {
//
//                    blocks[x][Chunk.CHUNK_WIDTH - 1][z].setTop(true);
//
//                    if (chunk.blocks[x + 1][Chunk.CHUNK_WIDTH - 1][z].isOpaque())
//                        blocks[x][Chunk.CHUNK_WIDTH - 1][z].setRight(true);
//
//                    if (chunk.blocks[x - 1][Chunk.CHUNK_WIDTH - 1][z].isOpaque())
//                        blocks[x][Chunk.CHUNK_WIDTH - 1][z].setLeft(true);
//
//                    if (chunk.blocks[x][Chunk.CHUNK_WIDTH - 2][z].isOpaque())
//                        blocks[x][Chunk.CHUNK_WIDTH - 1][z].setBottom(true);
//
//                    if (chunk.blocks[x][Chunk.CHUNK_WIDTH - 1][z + 1].isOpaque())
//                        blocks[x][Chunk.CHUNK_WIDTH - 1][z].setFront(true);
//
//                    if (blocks[x][Chunk.CHUNK_WIDTH - 1][z - 1].isOpaque())
//                        blocks[x][Chunk.CHUNK_WIDTH - 1][z].setBack(true);
//                }
//            }
//
//        }
//    }
//
//    private void updateBottomLeftBack() {
//        if (leftChunk != null)
//            if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][0][0].isOpaque())
//                blocks[0][0][0].setLeft(true);
//
//        if (backChunk != null)
//            if (backChunk.blocks[0][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                blocks[0][0][0].setBack(true);
//
//        if (chunk.blocks[1][0][0].isOpaque())
//            blocks[0][0][0].setRight(true);
//
//        if (chunk.blocks[0][1][0].isOpaque())
//            blocks[0][0][0].setTop(true);
//
//        if (chunk.blocks[0][0][1].isOpaque())
//            blocks[0][0][0].setFront(true);
//    }
//
//    private void updateBottomLeftFront() {
//        if (leftChunk != null) {
//            if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                blocks[0][0][Chunk.CHUNK_WIDTH - 1].setLeft(true);
//        }
//
//        if (frontChunk != null) {
//            if (frontChunk.blocks[0][0][0].isOpaque())
//                blocks[0][0][Chunk.CHUNK_WIDTH - 1].setFront(true);
//        }
//
//        if (chunk.blocks[1][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
//            blocks[0][0][Chunk.CHUNK_WIDTH - 1].setRight(true);
//
//        if (chunk.blocks[0][1][Chunk.CHUNK_WIDTH - 1].isOpaque())
//            blocks[0][0][Chunk.CHUNK_WIDTH - 1].setTop(true);
//
//        if (chunk.blocks[0][0][Chunk.CHUNK_WIDTH - 2].isOpaque())
//            blocks[0][0][Chunk.CHUNK_WIDTH - 1].setBack(true);
//    }
//
//    private void updateBottomRightBack() {
//        if (rightChunk != null)
//            if (rightChunk.blocks[0][0][0].isOpaque())
//                blocks[Chunk.CHUNK_WIDTH - 1][0][0].setRight(true);
//
//        if (backChunk != null)
//            if (backChunk.blocks[Chunk.CHUNK_WIDTH - 1][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                blocks[Chunk.CHUNK_WIDTH - 1][0][0].setBack(true);
//
//        if (chunk.blocks[Chunk.CHUNK_WIDTH - 2][0][0].isOpaque())
//            blocks[Chunk.CHUNK_WIDTH - 1][0][0].setLeft(true);
//
//        if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][1][0].isOpaque())
//            blocks[Chunk.CHUNK_WIDTH - 1][0][0].setTop(true);
//
//        if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][0][1].isOpaque())
//            blocks[Chunk.CHUNK_WIDTH - 1][0][0].setFront(true);
//    }
//
//    private void updateBottomRightFront() {
//
//        if (rightChunk != null) {
//            if (rightChunk.blocks[Chunk.CHUNK_WIDTH - 1][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                blocks[Chunk.CHUNK_WIDTH - 1][0][Chunk.CHUNK_WIDTH - 1].setRight(true);
//        }
//
//        if (frontChunk != null) {
//            if (frontChunk.blocks[Chunk.CHUNK_WIDTH - 1][0][0].isOpaque())
//                blocks[Chunk.CHUNK_WIDTH - 1][0][Chunk.CHUNK_WIDTH - 1].setFront(true);
//        }
//
//        if (chunk.blocks[Chunk.CHUNK_WIDTH - 2][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
//            blocks[Chunk.CHUNK_WIDTH - 1][0][Chunk.CHUNK_WIDTH - 1].setLeft(true);
//
//        if (chunk.blocks[0][1][Chunk.CHUNK_WIDTH - 1].isOpaque())
//            blocks[Chunk.CHUNK_WIDTH - 1][0][Chunk.CHUNK_WIDTH - 1].setTop(true);
//
//        if (chunk.blocks[0][0][Chunk.CHUNK_WIDTH - 2].isOpaque())
//            blocks[Chunk.CHUNK_WIDTH - 1][0][Chunk.CHUNK_WIDTH - 1].setBack(true);
//    }
//
//    private void updateBottomLeft() {
//        boolean isValid = leftChunk != null;
//
//        for (int z = 1; z < Chunk.CHUNK_WIDTH - 1; z++) {
//            if (isValid)
//                if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][0][z].isOpaque())
//                    blocks[0][0][z].setLeft(true);
//
//            if (chunk.blocks[1][0][z].isOpaque()) {
//                blocks[0][0][z].setRight(true);
//            }
//
//            if (chunk.blocks[0][1][z].isOpaque()) {
//                blocks[0][0][z].setTop(true);
//            }
//
//            if (chunk.blocks[0][0][z + 1].isOpaque()) {
//                blocks[0][0][z].setFront(true);
//            }
//
//            if (chunk.blocks[0][0][z - 1].isOpaque()) {
//                blocks[0][0][z].setBack(true);
//            }
//        }
//    }
//
//    private void updateBottomRight() {
//
//        boolean isValid = rightChunk != null;
//
//        for (int z = 1; z < Chunk.CHUNK_WIDTH - 1; z++) {
//            if (isValid)
//                if (rightChunk.blocks[0][0][z].isOpaque())
//                    blocks[Chunk.CHUNK_WIDTH - 1][0][z].setRight(true);
//
//            if (chunk.blocks[Chunk.CHUNK_WIDTH - 2][0][z].isOpaque()) {
//                blocks[Chunk.CHUNK_WIDTH - 1][0][z].setLeft(true);
//            }
//
//            if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][1][z].isOpaque()) {
//                blocks[Chunk.CHUNK_WIDTH - 1][0][z].setTop(true);
//            }
//
//            if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][0][z + 1].isOpaque()) {
//                blocks[Chunk.CHUNK_WIDTH - 1][0][z].setFront(true);
//            }
//
//            if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][0][z - 1].isOpaque()) {
//                blocks[Chunk.CHUNK_WIDTH - 1][0][z].setBack(true);
//            }
//        }
//    }
//
//    private void updateBottomFront() {
//        boolean isValid = frontChunk != null;
//
//        for (int x = 1; x < Chunk.CHUNK_WIDTH - 1; x++) {
//            if (isValid)
//                if (frontChunk.blocks[x][0][0].isOpaque())
//                    blocks[x][0][Chunk.CHUNK_WIDTH - 1].setFront(true);
//
//            if (chunk.blocks[x + 1][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                blocks[x][0][Chunk.CHUNK_WIDTH - 1].setRight(true);
//
//            if (chunk.blocks[x - 1][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                blocks[x][0][Chunk.CHUNK_WIDTH - 1].setLeft(true);
//
//            if (chunk.blocks[x][1][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                blocks[x][0][Chunk.CHUNK_WIDTH - 1].setTop(true);
//
//            if (blocks[x][0][Chunk.CHUNK_WIDTH - 2].isOpaque())
//                blocks[x][0][Chunk.CHUNK_WIDTH - 1].setBack(true);
//        }
//    }
//
//    private void updateBottomBack() {
//        boolean isValid = backChunk != null;
//
//        for (int x = 1; x < Chunk.CHUNK_WIDTH - 1; x++) {
//            if (isValid)
//                if (backChunk.blocks[x][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                    blocks[x][0][0].setBack(true);
//
//            if (chunk.blocks[x + 1][0][0].isOpaque())
//                blocks[x][0][0].setRight(true);
//
//            if (chunk.blocks[x - 1][0][0].isOpaque())
//                blocks[x][0][0].setLeft(true);
//
//            if (chunk.blocks[x][1][0].isOpaque())
//                blocks[x][0][0].setTop(true);
//
//            if (chunk.blocks[x][0][1].isOpaque())
//                blocks[x][0][0].setFront(true);
//        }
//    }
//
//    private void updateBottomSide() {
//        for (int x = 1; x < Chunk.CHUNK_WIDTH - 1; x++) {
//            for (int z = 1; z < Chunk.CHUNK_WIDTH - 1; z++) {
//                if (chunk.blocks[x][0][z].is(Type.AIR) == false) {
//                    if (chunk.blocks[x + 1][0][z].isOpaque())
//                        blocks[x][0][z].setRight(true);
//
//                    if (chunk.blocks[x - 1][0][z].isOpaque())
//                        blocks[x][0][z].setLeft(true);
//
//                    if (chunk.blocks[x][1][z].isOpaque())
//                        blocks[x][0][z].setTop(true);
//
//                    if (chunk.blocks[x][0][z + 1].isOpaque())
//                        blocks[x][0][z].setFront(true);
//
//                    if (chunk.blocks[x + 1][0][z - 1].isOpaque())
//                        blocks[x][0][z].setBack(true);
//                }
//            }
//        }
//    }
//
//    private void updateLeftSide() {
//        boolean isValid = leftChunk != null;
//
//        for (int z = 1; z < Chunk.CHUNK_WIDTH - 1; z++) {
//            for (int y = 1; y < Chunk.CHUNK_HEIGHT - 1; y++) {
//                if (chunk.blocks[0][y][z].is(Type.AIR) == false) {
//                    if (isValid)
//                        if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][y][z].isOpaque())
//                            blocks[0][y][z].setLeft(true);
//
//                    if (chunk.blocks[0][y + 1][z].isOpaque())
//                        blocks[0][y][z].setTop(true);
//
//                    if (chunk.blocks[0][y - 1][z].isOpaque())
//                        blocks[0][y][z].setBottom(true);
//
//                    if (chunk.blocks[0][y][z + 1].isOpaque())
//                        blocks[0][y][z].setFront(true);
//
//                    if (chunk.blocks[0][y][z - 1].isOpaque())
//                        blocks[0][y][z].setBack(true);
//
//                    if (chunk.blocks[1][y][z - 1].isOpaque())
//                        blocks[0][y][z].setRight(true);
//                }
//            }
//        }
//    }
//
//    private void updateRightSide() {
//        boolean isValid = rightChunk != null;
//
//        for (int z = 1; z < Chunk.CHUNK_WIDTH - 1; z++) {
//            for (int y = 1; y < Chunk.CHUNK_HEIGHT - 1; y++) {
//                if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][z].is(Type.AIR) == false) {
//                    if (isValid)
//                        if (rightChunk.blocks[0][y][z].isOpaque())
//                            blocks[Chunk.CHUNK_WIDTH - 1][y][z].setRight(true);
//
//                    if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y + 1][z].isOpaque())
//                        blocks[Chunk.CHUNK_WIDTH - 1][y][z].setTop(true);
//
//                    if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y - 1][z].isOpaque())
//                        blocks[Chunk.CHUNK_WIDTH - 1][y][z].setBottom(true);
//
//                    if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][z + 1].isOpaque())
//                        blocks[Chunk.CHUNK_WIDTH - 1][y][z].setFront(true);
//
//                    if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][z - 1].isOpaque())
//                        blocks[Chunk.CHUNK_WIDTH - 1][y][z].setBack(true);
//
//                    if (chunk.blocks[Chunk.CHUNK_WIDTH - 2][y][z - 1].isOpaque())
//                        blocks[Chunk.CHUNK_WIDTH - 1][y][z].setLeft(true);
//                }
//            }
//        }
//    }
//
//    private void updateFrontSide() {
//        boolean isValid = frontChunk != null;
//
//        for (int x = 1; x < Chunk.CHUNK_WIDTH - 1; x++) {
//            for (int y = 1; y < Chunk.CHUNK_HEIGHT - 1; y++) {
//                if (chunk.blocks[x][y][Chunk.CHUNK_WIDTH - 1].is(Type.AIR) == false) {
//                    if (isValid)
//                        if (frontChunk.blocks[x][y][0].isOpaque())
//                            blocks[x][y][Chunk.CHUNK_WIDTH - 1].setFront(true);
//
//                    if (chunk.blocks[x + 1][y][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                        blocks[x][y][Chunk.CHUNK_WIDTH - 1].setRight(true);
//
//                    if (chunk.blocks[x - 1][y][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                        blocks[x][y][Chunk.CHUNK_WIDTH - 1].setLeft(true);
//
//                    if (chunk.blocks[x][y + 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                        blocks[x][y][Chunk.CHUNK_WIDTH - 1].setTop(true);
//
//                    if (chunk.blocks[x][y - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                        blocks[x][y][Chunk.CHUNK_WIDTH - 1].setBottom(true);
//
//                    if (chunk.blocks[x][y][Chunk.CHUNK_WIDTH - 2].isOpaque())
//                        blocks[x][y][Chunk.CHUNK_WIDTH - 1].setBack(true);
//                }
//            }
//        }
//    }
//
//    private void updateBackSide() {
//        boolean isValid = backChunk != null;
//
//        for (int x = 1; x < Chunk.CHUNK_WIDTH - 1; x++) {
//            for (int y = 1; y < Chunk.CHUNK_HEIGHT - 1; y++) {
//                if (chunk.blocks[x][y][0].is(Type.AIR) == false) {
//                    if (isValid)
//                        if (backChunk.blocks[x][y][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                            blocks[x][y][0].setBack(true);
//
//                    if (chunk.blocks[x + 1][y][0].isOpaque())
//                        blocks[x][y][0].setRight(true);
//
//                    if (chunk.blocks[x - 1][y][0].isOpaque())
//                        blocks[x][y][0].setLeft(true);
//
//                    if (chunk.blocks[x][y + 1][0].isOpaque())
//                        blocks[x][y][0].setTop(true);
//
//                    if (chunk.blocks[x][y - 1][0].isOpaque())
//                        blocks[x][y][0].setBottom(true);
//
//                    if (chunk.blocks[x][y][1].isOpaque())
//                        blocks[x][y][0].setFront(true);
//                }
//            }
//        }
//    }
//
//    private void calculateVertexCount() {
//        vertexCount = 0;
//        for (int x = 0; x < Chunk.CHUNK_WIDTH; x++)
//            for (int z = 0; z < Chunk.CHUNK_WIDTH; z++)
//                for (int y = 0; y < Chunk.CHUNK_HEIGHT; y++) {
//                    if (chunk.blocks[x][y][z].isBack())
//                        vertexCount += 6;
//                    if (chunk.blocks[x][y][z].isBottom())
//                        vertexCount += 6;
//                    if (chunk.blocks[x][y][z].isFront())
//                        vertexCount += 6;
//                    if (chunk.blocks[x][y][z].isLeft())
//                        vertexCount += 6;
//                    if (chunk.blocks[x][y][z].isRight())
//                        vertexCount += 6;
//                    if (chunk.blocks[x][y][z].isTop())
//                        vertexCount += 6;
//                }
//
//        System.out.println("vertexCount: " + vertexCount);
//    }
//
//    public int getVertexCount() {
//        return vertexCount;
//    }
//
//    private void updateLeftBack() {
//        boolean backIsValid = backChunk != null;
//        boolean leftIsValid = leftChunk != null;
//
//        for (int y = 1; y < Chunk.CHUNK_HEIGHT - 1; y++) {
//            if (chunk.blocks[0][y][0].is(Type.AIR) == false) {
//                if (backIsValid)
//                    if (backChunk.blocks[0][y][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                        blocks[0][y][0].setBack(true);
//
//                if (leftIsValid)
//                    if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][y][0].isOpaque())
//                        blocks[0][y][0].setLeft(true);
//
//                if (chunk.blocks[1][y][0].isOpaque())
//                    blocks[0][y][0].setRight(true);
//
//                if (chunk.blocks[0][y + 1][0].isOpaque())
//                    blocks[0][y][0].setTop(true);
//
//                if (chunk.blocks[0][y - 1][0].isOpaque())
//                    blocks[0][y][0].setBottom(true);
//
//                if (chunk.blocks[0][y][1].isOpaque())
//                    blocks[0][y][0].setFront(true);
//            }
//        }
//    }
//
//    private void updateLeftFront() {
//        boolean frontIsValid = frontChunk != null;
//        boolean leftIsValid = leftChunk != null;
//
//        for (int y = 1; y < Chunk.CHUNK_HEIGHT - 1; y++) {
//            if (chunk.blocks[0][y][Chunk.CHUNK_WIDTH - 1].is(Type.AIR) == false) {
//                if (frontIsValid)
//                    if (frontChunk.blocks[0][y][0].isOpaque())
//                        blocks[0][y][Chunk.CHUNK_WIDTH - 1].setFront(true);
//
//                if (leftIsValid)
//                    if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][y][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                        blocks[0][y][Chunk.CHUNK_WIDTH - 1].setLeft(true);
//
//                if (chunk.blocks[1][y][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                    blocks[0][y][Chunk.CHUNK_WIDTH - 1].setRight(true);
//
//                if (chunk.blocks[0][y + 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                    blocks[0][y][Chunk.CHUNK_WIDTH - 1].setTop(true);
//
//                if (chunk.blocks[0][y - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                    blocks[0][y][Chunk.CHUNK_WIDTH - 1].setBottom(true);
//
//                if (chunk.blocks[0][y][Chunk.CHUNK_WIDTH - 2].isOpaque())
//                    blocks[0][y][Chunk.CHUNK_WIDTH - 1].setBack(true);
//            }
//        }
//    }
//
//    private void updateRightBack() {
//        boolean rightIsValid = rightChunk != null;
//        boolean backIsValid = backChunk != null;
//
//        for (int y = 1; y < Chunk.CHUNK_HEIGHT - 1; y++) {
//            if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][0].is(Type.AIR) == false) {
//                if (rightIsValid)
//                    if (rightChunk.blocks[0][y][0].isOpaque())
//                        blocks[Chunk.CHUNK_WIDTH - 1][y][0].setRight(true);
//
//                if (backIsValid)
//                    if (backChunk.blocks[Chunk.CHUNK_WIDTH - 1][y][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                        blocks[Chunk.CHUNK_WIDTH - 1][y][0].setBack(true);
//
//                if (chunk.blocks[Chunk.CHUNK_WIDTH - 2][y][0].isOpaque())
//                    blocks[Chunk.CHUNK_WIDTH - 1][y][0].setLeft(true);
//
//                if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y + 1][0].isOpaque())
//                    blocks[Chunk.CHUNK_WIDTH - 1][y][0].setTop(true);
//
//                if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y - 1][0].isOpaque())
//                    blocks[Chunk.CHUNK_WIDTH - 1][y][0].setBottom(true);
//
//                if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][1].isOpaque())
//                    blocks[Chunk.CHUNK_WIDTH - 1][y][0].setFront(true);
//            }
//        }
//    }
//
//    private void updateRightFront() {
//        boolean rightIsValid = rightChunk != null;
//        boolean frontIsValid = frontChunk != null;
//
//        for (int y = 1; y < Chunk.CHUNK_HEIGHT - 1; y++) {
//            if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][Chunk.CHUNK_WIDTH - 1].is(Type.AIR) == false) {
//                if (rightIsValid)
//                    if (rightChunk.blocks[0][y][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                        blocks[Chunk.CHUNK_WIDTH - 1][y][Chunk.CHUNK_WIDTH - 1].setRight(true);
//
//                if (frontIsValid)
//                    if (frontChunk.blocks[Chunk.CHUNK_WIDTH - 1][y][0].isOpaque())
//                        blocks[Chunk.CHUNK_WIDTH - 1][y][Chunk.CHUNK_WIDTH - 1].setFront(true);
//
//                if (chunk.blocks[Chunk.CHUNK_WIDTH - 2][y][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                    blocks[Chunk.CHUNK_WIDTH - 1][y][Chunk.CHUNK_WIDTH - 1].setLeft(true);
//
//                if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y + 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                    blocks[Chunk.CHUNK_WIDTH - 1][y][Chunk.CHUNK_WIDTH - 1].setTop(true);
//
//                if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
//                    blocks[Chunk.CHUNK_WIDTH - 1][y][Chunk.CHUNK_WIDTH - 1].setBottom(true);
//
//                if (chunk.blocks[Chunk.CHUNK_WIDTH - 1][y][Chunk.CHUNK_WIDTH - 2].isOpaque())
//                    blocks[Chunk.CHUNK_WIDTH - 1][y][Chunk.CHUNK_WIDTH - 1].setBack(true);
//
//            }
//        }
//    }

}
