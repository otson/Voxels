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
import static voxels.Voxels.getCurrentChunkX;
import static voxels.Voxels.getCurrentChunkZ;

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
        if (render || chunk.blocks[xx][yy][zz + 1].type == Type.AIR || chunk.blocks[xx][yy][zz + 1].type == Type.WATER && (maxHeights[xx][zz] - difference < y && y <= maxHeights[xx][zz])) {
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
        if (render || chunk.blocks[xx - 1][yy][zz].type == Type.AIR || chunk.blocks[xx - 1][yy][zz].type == Type.WATER && (maxHeights[xx][zz] - difference < y && y <= maxHeights[xx][zz])) {
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
        if (render || chunk.blocks[xx][yy][zz - 1].type == Type.AIR || chunk.blocks[xx][yy][zz - 1].type == Type.WATER && (maxHeights[xx][zz] - difference < y && y <= maxHeights[xx][zz])) {
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
        if (render || chunk.blocks[xx + 1][yy][zz].type == Type.AIR || chunk.blocks[xx + 1][yy][zz].type == Type.WATER && (maxHeights[xx][zz] - difference < y && y <= maxHeights[xx][zz])) {
            right[xx][yy][zz] = true;
            returnVertices += 6;
        }
        else
            right[xx][yy][zz] = false;

        // top face
        render = false;
        if (y == yMax)
            render = true;
        if (render || chunk.blocks[xx][yy + 1][zz].type == Type.AIR || chunk.blocks[xx][yy + 1][zz].type == Type.AIR && maxHeights[xx][zz] == yy) {
            top[xx][yy][zz] = true;
            returnVertices += 6;
        }
        else
            top[xx][yy][zz] = false;

         //bottom face
        render = false;
        if (y == 0)
            render = true;
        if (render || chunk.blocks[xx][yy - 1][zz].type == Type.AIR || chunk.blocks[xx][yy - 1][zz].type == Type.AIR && maxHeights[xx][zz] + 1 == yy) {
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
        if (render || chunk.blocks[xx][yy + 1][zz].type == Type.AIR && y == Chunk.WATER_HEIGHT) {
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
                    if (chunk.blocks[x][y][z].type == Type.DIRT) {
                        vertices += calculateGroundVertices(chunk, x, y, z, getCurrentChunkX() * chunk.blocks.length + xOff, 0, getCurrentChunkZ() * chunk.blocks.length + zOff, 1);
                    }
                    else if (chunk.blocks[x][y][z].type == Type.WATER) {
                        vertices += calculateWaterVertices(chunk, x, y, z, getCurrentChunkX() * chunk.blocks.length + xOff, 0, getCurrentChunkZ() * chunk.blocks.length + zOff, 1);
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
                    if (chunk.blocks[x][y][z].type == Type.DIRT) {
                        if (front[x][y][z]) {
                            // 1st
                            // upper left - +
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
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

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
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

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
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

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
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

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
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

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
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

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;
                            // lower left + -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // lower right - -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // 2nd
                            // upper left + -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;
                            // lower right - -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // upper right - +
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
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

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            // lower right - +
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            // lower left - -
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // 2nd
                            // upper right + +
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            // lower left - -
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // upper left + -
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
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

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // lower right - -
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // lower left - +
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            // 2nd
                            // upper right + -
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // lower left - +
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            // upper left + +
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
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

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
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

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
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

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
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

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
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

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
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

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
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

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;
                            
                            // lower right - +
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            // lower left - -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            

                            // 2nd
                            // upper left + -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;
                            
                            // upper right + +
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;
                            
                            // lower right - +
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            

                        }
                    }
                    if (chunk.blocks[x][y][z].type == Type.WATER) {
                        if (top[x][y][z]) {

                            // upper left
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y - WaterOffs;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
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

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y - WaterOffs;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
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

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y - WaterOffs;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
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

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y - WaterOffs;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
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

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y - WaterOffs;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
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

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y - WaterOffs;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
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

}
