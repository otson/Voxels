/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels.ChunkManager;

import com.ning.compress.lzf.LZFDecoder;
import com.ning.compress.lzf.LZFEncoder;
import com.ning.compress.lzf.LZFException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.FloatBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import voxels.Voxels;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static voxels.ChunkManager.Chunk.CHUNK_HEIGHT;
import static voxels.ChunkManager.Chunk.CHUNK_WIDTH;
import static voxels.Voxels.WaterOffs;
import static voxels.Voxels.getCurrentChunkX;
import static voxels.Voxels.getCurrentChunkZ;

/**
 *
 * @author otso
 */
public class ChunkManager {

    private static int vertexSize = 3;
    private static int normalSize = 3;
    private static int texSize = 2;

    private boolean generate = false;

    private ChunkThread chunkThread = new ChunkThread(0, 0, 0, 0);
    private MapThread mapThread = new MapThread(null, null, null, 0, 0);

    private ConcurrentHashMap<Integer, byte[]> map;
    private ConcurrentHashMap<Integer, Handle> handles;
    private ChunkCreator chunkCreator;
    private ChunkLoader chunkLoader;

    private boolean atMax = false;

    private boolean inLoop;
    private boolean initialLoad = true;

    public ChunkManager() {
        map = new ConcurrentHashMap<>();
        handles = new ConcurrentHashMap<>();
        chunkCreator = new ChunkCreator(map);
        chunkLoader = new ChunkLoader(this);
        chunkLoader.setPriority(Thread.MIN_PRIORITY);
    }

    public void generateChunk(int chunkX, int chunkZ) {
        if (isChunk(chunkX, chunkZ) == false) {
            Chunk chunk = new Chunk(0, 0);
            drawChunkVBO(chunk, 0, 0);
            handles.put(new Pair(0, 0).hashCode(), new Handle(chunk.getVboVertexHandle(), chunk.getVboNormalHandle(), chunk.getVboTexHandle(), chunk.getVertices()));
            map.put(new Pair(0, 0).hashCode(), toByte(chunk));
        }
        else {
            System.out.println("Chunk already exists!");
        }
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

    public Handle getHandle(int x, int z) {
        if (handles.containsKey(new Pair(x, z).hashCode()))
            return handles.get(new Pair(x, z).hashCode());
        else
            return null;
    }

    public boolean isChunk(int chunkX, int chunkZ) {
        return map.containsKey(new Pair(chunkX, chunkZ).hashCode());
    }

    private void drawChunkVBO(Chunk chunk, int xOff, int zOff) {
        int size  = 1;
        float[] vertexArray = new float[chunk.getVertexCount()* vertexSize];
        float[] normalArray = new float[chunk.getVertexCount() * normalSize];
        float[] texArray = new float[chunk.getVertexCount() * texSize];

        int vArrayPos = 0;
        int nArrayPos = 0;
        int tArrayPos = 0;

        FloatBuffer vertexData = BufferUtils.createFloatBuffer(chunk.getVertexCount() * vertexSize);
        FloatBuffer normalData = BufferUtils.createFloatBuffer(chunk.getVertexCount() * vertexSize);
        FloatBuffer texData = BufferUtils.createFloatBuffer(chunk.getVertexCount() * texSize);

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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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
                        if (chunk.blocks[x][y][z].isBottom()) {
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
                            // upper left + -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // lower left - -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            // 2nd
                            // upper left + -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            // upper right + +
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
//                            colorArray[cArrayPos] = 1;
//                            cArrayPos++;
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

//        colorData.put(colorArray);
//        colorData.flip();
        int vboVertexHandle = glGenBuffers();
        chunk.setVboVertexHandle(vboVertexHandle);

        glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandle);
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vboNormalHandle = glGenBuffers();
        chunk.setVboNormalHandle(vboNormalHandle);

        glBindBuffer(GL_ARRAY_BUFFER, vboNormalHandle);
        glBufferData(GL_ARRAY_BUFFER, normalData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vboTexHandle = glGenBuffers();
        chunk.setVboTexHandle(vboTexHandle);

        glBindBuffer(GL_ARRAY_BUFFER, vboTexHandle);
        glBufferData(GL_ARRAY_BUFFER, texData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

    }

    public void checkChunkUpdates() {
        // neither thread is currently running
        if (generate && !chunkThread.isAlive() && !mapThread.isAlive() && chunkThread.isReady() == false) {
            inLoop = true;
            // request new valid coordinates
            chunkCreator.setCurrentChunkX(getCurrentChunkX());
            chunkCreator.setCurrentChunkZ(getCurrentChunkZ());
            Coordinates coordinates = null;
            boolean running = true;

            coordinates = chunkCreator.getNewCoordinates();

            if (coordinates != null) {

                atMax = false;
                int x = coordinates.x;
                int z = coordinates.z;

                int newChunkX = coordinates.x;
                int newChunkZ = coordinates.z;

                // make a new chunk
                chunkThread = new ChunkThread(newChunkX, newChunkZ, x * Chunk.CHUNK_WIDTH, z * Chunk.CHUNK_WIDTH);
                chunkThread.setPriority(Thread.MIN_PRIORITY);
                chunkThread.start();
            }
            else {
                atMax = true;
                if (initialLoad && Voxels.USE_SEED)
                    System.out.println("Loaded all chunks. Seed: " + Voxels.SEED);
//                else
//                    System.out.println("Loaded all chunks");
                initialLoad = false;
            }

        }
        else if (inLoop && chunkThread.isReady() && !chunkThread.isAlive()) // has finished chunk and exited the loop
        {

            // Create the buffers in main thread
            createBuffers(chunkThread.getChunk(), chunkThread.getVertexData(), chunkThread.getNormalData(), chunkThread.getTexData());

            // put the Chunk to HashMap in a new thread
            mapThread = new MapThread(map, handles, chunkThread.getChunk(), chunkThread.getChunkX(), chunkThread.getChunkZ());
            mapThread.setPriority(Thread.MIN_PRIORITY);
            mapThread.start();
            chunkThread = new ChunkThread(0, 0, 0, 0);
            inLoop = false;

            if (initialLoad) {
                String string = "Chunks loaded: " + (int) ((float) map.size() / (float) ((Voxels.chunkCreationDistance * 2 + 1) * (Voxels.chunkCreationDistance * 2 + 1)) * 100) + " % (" + map.size() + "/" + ((Voxels.chunkCreationDistance * 2 + 1) * (Voxels.chunkCreationDistance * 2 + 1)) + ")";
                System.out.println(string);
                Display.setTitle(string);

            }
        }
    }

    public void stopGeneration() {
        generate = false;
    }

    public void startGeneration() {
        generate = true;
    }

    public void createBuffers(Chunk chunk, FloatBuffer vertexData, FloatBuffer normalData, FloatBuffer texData) {

        int vboVertexHandle = glGenBuffers();
        chunk.setVboVertexHandle(vboVertexHandle);

        glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandle);
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vboNormalHandle = glGenBuffers();
        chunk.setVboNormalHandle(vboNormalHandle);

        glBindBuffer(GL_ARRAY_BUFFER, vboNormalHandle);
        glBufferData(GL_ARRAY_BUFFER, normalData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vboTexHandle = glGenBuffers();
        chunk.setVboTexHandle(vboTexHandle);

        glBindBuffer(GL_ARRAY_BUFFER, vboTexHandle);
        glBufferData(GL_ARRAY_BUFFER, texData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vboColorHandle = glGenBuffers();
        chunk.setVboColorHandle(vboColorHandle);

//        glBindBuffer(GL_ARRAY_BUFFER, vboColorHandle);
//        glBufferData(GL_ARRAY_BUFFER, colorData, GL_STATIC_DRAW);
//        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public boolean isAtMax() {
        return atMax;
    }

    public byte[] toByte(Chunk chunk) {
        return LZFEncoder.encode(serialize(chunk));
    }

    public Chunk toChunk(byte[] bytes) {
        try {
            return (Chunk) deserialize(LZFDecoder.decode(bytes));
        } catch (LZFException ex) {
            Logger.getLogger(ChunkManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os;
        try {
            os = new ObjectOutputStream(out);
            os.writeObject(obj);
        } catch (IOException ex) {
            Logger.getLogger(MapThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        return out.toByteArray();
    }

    public static Object deserialize(byte[] data) {
        try {
            return new ObjectInputStream(new ByteArrayInputStream(data)).readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(MapThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public int chunkAmount() {
        return map.size();
    }

    public ChunkLoader getChunkLoader() {
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

}
