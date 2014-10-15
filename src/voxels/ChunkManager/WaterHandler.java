/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels.ChunkManager;

import Items.DebugInfo;
import java.nio.FloatBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import org.lwjgl.util.vector.Vector3f;
import static voxels.Voxels.getChunkX;
import static voxels.Voxels.getChunkY;
import static voxels.Voxels.getChunkZ;
import static voxels.Voxels.getX;
import static voxels.Voxels.getY;
import static voxels.Voxels.getZ;

/**
 *
 * @author otso
 */
public class WaterHandler {

    private ConcurrentHashMap<Integer, Water> waters;
    private ConcurrentHashMap<Integer, Water> stableWaters;
    private ConcurrentHashMap<Integer, Coordinates> chunksToUpdate = new ConcurrentHashMap<>();
    private ChunkManager chunkManager;

    public int vertexHandle;
    public int normalHandle;
    public int vertices = 0;

    public WaterHandler(ChunkManager chunkManager) {
        waters = new ConcurrentHashMap<>();
        stableWaters = new ConcurrentHashMap<>();
        this.chunkManager = chunkManager;
        this.chunkManager.setWaterHandler(this);
        vertexHandle = glGenBuffers();
        normalHandle = glGenBuffers();
    }

    public void add(Water water) {
        int hash = new Pair(water.x, water.y, water.z).hashCode();
        if (!waters.containsKey(hash)) {
            waters.put(hash, water);
        } else {
            waters.get(hash).setLevel(water.getLevel());
        }
    }

    public void simulateWaters() {
        boolean updateVBO = false;
        long start = System.nanoTime();
        for (Water water : waters.values()) {
            if (!water.isFresh()) {
                boolean falling = false;
                if (waters.containsKey(new Pair(water.x, water.y - 1, water.z).hashCode())) {
                    Water below = waters.get(new Pair(water.x, water.y - 1, water.z).hashCode());
                    if (below.getLevel() < 7) {
                        water.setActive(true);
                        below.setLevel(7);
                        
                    }
                    falling = true;
                } else if (stableWaters.containsKey(new Pair(water.x, water.y - 1, water.z).hashCode())) {
                    if (stableWaters.get(new Pair(water.x, water.y - 1, water.z).hashCode()).getLevel() < 7) {
                        Water below = stableWaters.remove(new Pair(water.x, water.y - 1, water.z).hashCode());
                        waters.put(new Pair(water.x, water.y - 1, water.z).hashCode(), below);
                        water.setActive(true);
                        below.setLevel(7);
                    }
                    falling = true;
                } else {
                    int x = water.x < 0 ? water.x + 1 : water.x;
                    int z = water.z < 0 ? water.z + 1 : water.z;
                    byte block = chunkManager.getActiveBlock(x, (float) (water.y - 1), z);
                    if (block == Type.AIR) {
                        waters.put(new Pair(water.x, water.y - 1, water.z).hashCode(), new Water(water.x, water.y - 1, water.z, -(water.getLevel())));
                        water.setActive(true);
                        water.setLevel(0);
                        falling = true;
                    }
                }
                if (!falling) {
                    if (water.getLevel() > 1) {
                        if (waters.containsKey(new Pair(water.x + 1, water.y, water.z).hashCode())) {
                            Water right = waters.get(new Pair(water.x + 1, water.y, water.z).hashCode());
                            if (right.getLevel() + 1 < water.getLevel()) {
                                right.setLevel(water.getLevel() - 1);
                                water.setActive(true);
                            }
                        } else if (stableWaters.containsKey(new Pair(water.x + 1, water.y, water.z).hashCode())) {
                            Water right = stableWaters.remove(new Pair(water.x + 1, water.y, water.z).hashCode());
                            waters.put(new Pair(water.x + 1, water.y, water.z).hashCode(), right);
                            if (right.getLevel() + 1 < water.getLevel()) {
                                right.setLevel(water.getLevel() - 1);
                                water.setActive(true);
                            }
                        } else {
                            int x = water.x < 0 ? water.x + 1 : water.x;
                            int z = water.z < 0 ? water.z + 1 : water.z;
                            byte block = chunkManager.getActiveBlock(x + 1, water.y, z);
                            if (block == Type.AIR) {
                                waters.put(new Pair(water.x + 1, water.y, water.z).hashCode(), new Water(water.x + 1, water.y, water.z, -(water.getLevel() - 1)));
                                water.setActive(true);
                            }
                        }
                    }

                    if (water.getLevel() > 1) {
                        if (waters.containsKey(new Pair(water.x - 1, water.y, water.z).hashCode())) {
                            Water left = waters.get(new Pair(water.x - 1, water.y, water.z).hashCode());
                            if (left.getLevel() + 1 < water.getLevel()) {
                                left.setLevel(water.getLevel() - 1);
                                water.setActive(true);
                            }
                        } else if (stableWaters.containsKey(new Pair(water.x - 1, water.y, water.z).hashCode())) {
                            Water left = stableWaters.remove(new Pair(water.x - 1, water.y, water.z).hashCode());
                            waters.put(new Pair(water.x - 1, water.y, water.z).hashCode(), left);
                            if (left.getLevel() + 1 < water.getLevel()) {
                                left.setLevel(water.getLevel() - 1);
                                water.setActive(true);
                            }
                        } else {
                            int x = water.x < 0 ? water.x + 1 : water.x;
                            int z = water.z < 0 ? water.z + 1 : water.z;
                            byte block = chunkManager.getActiveBlock(x - 1, water.y, z);
                            if (block == Type.AIR) {
                                waters.put(new Pair(water.x - 1, water.y, water.z).hashCode(), new Water(water.x - 1, water.y, water.z, -(water.getLevel() - 1)));
                                water.setActive(true);
                            }
                        }
                    }

                    if (water.getLevel() > 1) {
                        if (waters.containsKey(new Pair(water.x, water.y, water.z + 1).hashCode())) {
                            Water front = waters.get(new Pair(water.x, water.y, water.z + 1).hashCode());
                            if (front.getLevel() + 1 < water.getLevel()) {
                                front.setLevel(water.getLevel() - 1);
                                water.setActive(true);
                            }
                        } else if (stableWaters.containsKey(new Pair(water.x, water.y, water.z + 1).hashCode())) {
                            Water front = stableWaters.remove(new Pair(water.x, water.y, water.z + 1).hashCode());
                            waters.put(new Pair(water.x, water.y, water.z + 1).hashCode(), front);
                            if (front.getLevel() + 1 < water.getLevel()) {
                                front.setLevel(water.getLevel() - 1);
                                water.setActive(true);
                            }
                        } else {
                            int x = water.x < 0 ? water.x + 1 : water.x;
                            int z = water.z < 0 ? water.z + 1 : water.z;
                            byte block = chunkManager.getActiveBlock(x, water.y, z + 1);
                            if (block == Type.AIR) {
                                waters.put(new Pair(water.x, water.y, water.z + 1).hashCode(), new Water(water.x, water.y, water.z + 1, -(water.getLevel() - 1)));
                                water.setActive(true);
                            }
                        }
                    }
                    if (water.getLevel() > 1) {
                        if (waters.containsKey(new Pair(water.x, water.y, water.z - 1).hashCode())) {
                            Water back = waters.get(new Pair(water.x, water.y, water.z - 1).hashCode());
                            if (back.getLevel() + 1 < water.getLevel()) {
                                back.setLevel(water.getLevel() - 1);
                                water.setActive(true);
                            }
                        } else if (stableWaters.containsKey(new Pair(water.x, water.y, water.z - 1).hashCode())) {
                            Water back = stableWaters.remove(new Pair(water.x, water.y, water.z - 1).hashCode());
                            waters.put(new Pair(water.x, water.y, water.z - 1).hashCode(), back);
                            if (back.getLevel() + 1 < water.getLevel()) {
                                back.setLevel(water.getLevel() - 1);
                                water.setActive(true);
                            }
                        } else {
                            int x = water.x < 0 ? water.x + 1 : water.x;
                            int z = water.z < 0 ? water.z + 1 : water.z;
                            byte block = chunkManager.getActiveBlock(x, water.y, z - 1);
                            if (block == Type.AIR) {
                                waters.put(new Pair(water.x, water.y, water.z - 1).hashCode(), new Water(water.x, water.y, water.z - 1, -(water.getLevel() - 1)));
                                water.setActive(true);
                            }
                        }
                    }
                }
            } else {
                water.setFresh(false);
            }

        }

        for (Water water : waters.values()) {
            if (water.getLevel() <= 0) {
                waters.remove(new Pair(water.x, water.y, water.z).hashCode());
            } else if (water.isActive()) {
                updateVBO = true;
                //int x = water.x < 0 ? water.x + 1 : water.x;
                //int z = water.z < 0 ? water.z + 1 : water.z;
                //chunkManager.setActiveBlockNoUpdate(new Vector3f(x, water.y, z), (byte) -water.getLevel());
//                int chunkX = getChunkX(x);
//                int chunkY = getChunkY(water.y);
//                int chunkZ = getChunkZ(z);
//                int xInChunk = getX(x);
//                int yInChunk = getY(water.y);
//                int zInChunk = getZ(z);
//                chunksToUpdate.putIfAbsent(new Pair(chunkX, chunkY, chunkZ).hashCode(), new Coordinates(chunkX, chunkY, chunkZ));
//                if (xInChunk == 0) {
//                    chunksToUpdate.putIfAbsent(new Pair(chunkX - 1, chunkY, chunkZ).hashCode(), new Coordinates(chunkX - 1, chunkY, chunkZ));
//                }
//                if (xInChunk == Chunk.CHUNK_SIZE - 1) {
//                    chunksToUpdate.putIfAbsent(new Pair(chunkX + 1, chunkY, chunkZ).hashCode(), new Coordinates(chunkX + 1, chunkY, chunkZ));
//                }
//                if (yInChunk == 0) {
//                    chunksToUpdate.putIfAbsent(new Pair(chunkX, chunkY - 1, chunkZ).hashCode(), new Coordinates(chunkX, chunkY - 1, chunkZ));
//                }
//                if (yInChunk == Chunk.CHUNK_SIZE - 1) {
//                    chunksToUpdate.putIfAbsent(new Pair(chunkX, chunkY + 1, chunkZ).hashCode(), new Coordinates(chunkX, chunkY + 1, chunkZ));
//                }
//                if (zInChunk == 0) {
//                    chunksToUpdate.putIfAbsent(new Pair(chunkX, chunkY, chunkZ - 1).hashCode(), new Coordinates(chunkX, chunkY, chunkZ - 1));
//                }
//                if (zInChunk == Chunk.CHUNK_SIZE - 1) {
//                    chunksToUpdate.putIfAbsent(new Pair(chunkX, chunkY, chunkZ + 1).hashCode(), new Coordinates(chunkX, chunkY, chunkZ + 1));
//                }
                water.setActive(false);
            } else {
                water.setActive(false);
                waters.remove(new Pair(water.x, water.y, water.z).hashCode());
                stableWaters.put(new Pair(water.x, water.y, water.z).hashCode(), water);
            }
        }

        //System.out.println("Chunks to update: " + chunksToUpdate.size());
//        for (Coordinates coord : chunksToUpdate.values()) {
//            chunkManager.updateChunk(chunkManager.getActiveChunk(coord.x, coord.y, coord.z));
//        }
//        chunksToUpdate.clear();
        
       //System.out.println("Active water blocks: " + waters.size());
        //System.out.println("Time to update : " + (System.nanoTime() - start) / 1000000 + " ms.");
        start = System.nanoTime();
        if (updateVBO) {
            createVBO();
            //System.out.println("Time to create VBO: " + (System.nanoTime() - start) / 1000000 + " ms.");
        }

    }

    private void createVBO() {
        vertices = waters.size() * 24 + stableWaters.size() * 24;
        final int vertexSize = 3;
        FloatBuffer vertexData = BufferUtils.createFloatBuffer(vertices * vertexSize);
        FloatBuffer normalData = BufferUtils.createFloatBuffer(vertices * vertexSize);
        for (Water water : waters.values()) {
            int x = water.x;
            int y = water.y;
            int z = water.z;
            float h = (water.getLevel() / 7f);
            vertexData.put(new float[]{0 + x, h + y, 0 + z, 0 + x, h + y, 1 + z, 1 + x, h + y, 1 + z, 1 + x, h + y, 0 + z, // top
                0 + x, 0 + y, 0 + z, 1 + x, 0 + y, 0 + z, 1 + x, 0 + y, 1 + z, 0 + x, 0 + y, 1 + z, // bottom
                0 + x, h + y, 0 + z, 0 + x, 0 + y, 0 + z, 0 + x, 0 + y, 1 + z, 0 + x, h + y, 1 + z, // left
                1 + x, h + y, 0 + z, 1 + x, h + y, 1 + z, 1 + x, 0 + y, 1 + z, 1 + x, 0 + y, 0 + z, // right
                0 + x, h + y, 1 + z, 0 + x, 0 + y, 1 + z, 1 + x, 0 + y, 1 + z, 1 + x, h + y, 1 + z, // front
                0 + x, h + y, 0 + z, 1 + x, h + y, 0 + z, 1 + x, 0 + y, 0 + z, 0 + x, 0 + y, 0 + z // back
        });

        }
        for (Water water : stableWaters.values()) {
            int x = water.x;
            int y = water.y;
            int z = water.z;
            float h = (water.getLevel() / 7f);
            vertexData.put(new float[]{0 + x, h + y, 0 + z, 0 + x, h + y, 1 + z, 1 + x, h + y, 1 + z, 1 + x, h + y, 0 + z, // top
                0 + x, 0 + y, 0 + z, 1 + x, 0 + y, 0 + z, 1 + x, 0 + y, 1 + z, 0 + x, 0 + y, 1 + z, // bottom
                0 + x, h + y, 0 + z, 0 + x, 0 + y, 0 + z, 0 + x, 0 + y, 1 + z, 0 + x, h + y, 1 + z, // left
                1 + x, h + y, 0 + z, 1 + x, h + y, 1 + z, 1 + x, 0 + y, 1 + z, 1 + x, 0 + y, 0 + z, // right
                0 + x, h + y, 1 + z, 0 + x, 0 + y, 1 + z, 1 + x, 0 + y, 1 + z, 1 + x, h + y, 1 + z, // front
                0 + x, h + y, 0 + z, 1 + x, h + y, 0 + z, 1 + x, 0 + y, 0 + z, 0 + x, 0 + y, 0 + z // back
        });

        }
        vertexData.flip();

        //vertexHandle = 100;//glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexHandle);
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STREAM_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

    }

}
