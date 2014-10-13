/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels.ChunkManager;

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
import static voxels.Voxels.getChunkX;
import static voxels.Voxels.getChunkY;
import static voxels.Voxels.getChunkY;
import static voxels.Voxels.getChunkZ;
import static voxels.Voxels.getChunkZ;

/**
 *
 * @author otso
 */
public class WaterHandler {

    private ConcurrentHashMap<Integer, Water> waters;
    private ConcurrentHashMap<Integer, Water> newWaters;
    private ConcurrentHashMap<Integer, Coordinates> chunksToUpdate = new ConcurrentHashMap<>();
    private ChunkManager chunkManager;

    public int vertexHandle;
    public int normalHandle;
    public int vertices = 0;

    public WaterHandler(ChunkManager chunkManager) {
        waters = new ConcurrentHashMap<>();
        newWaters = new ConcurrentHashMap<>();
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
        long start = System.nanoTime();
        for (Water water : waters.values()) {
            if (!water.isFresh()) {
                boolean falling = false;
                if (waters.containsKey(new Pair(water.x, water.y - 1, water.z).hashCode())) {
                    Water below = waters.get(new Pair(water.x, water.y - 1, water.z).hashCode());
                    if (below.getLevel() < 10) {
                        int missing = 10 - below.getLevel();
                        if (missing >= water.getLevel()) {
                            below.increaseLevel(water.getLevel());
                            waters.remove(new Pair(water.x, water.y, water.z).hashCode());
                            falling = true;
                        } else {
                            below.increaseLevel(missing);
                            water.decreaseLevel(missing);
                            falling = true;
                        }
//                        if(below.getLevel() < 10){
//                            below.increaseLevel(1);
//                            water.decreaseLevel(1);
//                            falling = true;
//                        }
                    }
                } else {
                    int x = water.x < 0 ? water.x + 1 : water.x;
                    int z = water.z < 0 ? water.z + 1 : water.z;
                    byte block = chunkManager.getActiveBlock(x, (float) (water.y - 1), z);
                    if (block == Type.AIR) {
                        chunkManager.setActiveBlock(new Vector3f(z, water.y, z), Type.AIR);
                        water.y--;
                        falling = true;
                    }
                }
                if (!falling) {
                    if (water.getLevel() > 1) {
                        if (waters.containsKey(new Pair(water.x + 1, water.y, water.z).hashCode())) {
                            Water right = waters.get(new Pair(water.x + 1, water.y, water.z).hashCode());
                            if (right.getLevel() + 1 < water.getLevel()) {
                                right.increaseLevel(1);
                                water.decreaseLevel(1);
                            }
                        } else {
                            int x = water.x < 0 ? water.x + 1 : water.x;
                            int z = water.z < 0 ? water.z + 1 : water.z;
                            byte block = chunkManager.getActiveBlock(x + 1, water.y, z);
                            if (block == Type.AIR) {
                                waters.put(new Pair(water.x + 1, water.y, water.z).hashCode(), new Water(water.x + 1, water.y, water.z, Type.WATER1));
                                water.decreaseLevel(1);
                            }
                        }
                    }

                    if (water.getLevel() > 1) {
                        if (waters.containsKey(new Pair(water.x - 1, water.y, water.z).hashCode())) {
                            Water left = waters.get(new Pair(water.x - 1, water.y, water.z).hashCode());
                            if (left.getLevel() + 1 < water.getLevel()) {
                                left.increaseLevel(1);
                                water.decreaseLevel(1);
                            }
                        } else {
                            int x = water.x < 0 ? water.x + 1 : water.x;
                            int z = water.z < 0 ? water.z + 1 : water.z;
                            byte block = chunkManager.getActiveBlock(x - 1, water.y, z);
                            if (block == Type.AIR) {
                                waters.put(new Pair(water.x - 1, water.y, water.z).hashCode(), new Water(water.x - 1, water.y, water.z, Type.WATER1));
                                water.decreaseLevel(1);
                            }
                        }
                    }

                    if (water.getLevel() > 1) {
                        if (waters.containsKey(new Pair(water.x, water.y, water.z + 1).hashCode())) {
                            Water front = waters.get(new Pair(water.x, water.y, water.z + 1).hashCode());
                            if (front.getLevel() + 1 < water.getLevel()) {
                                front.increaseLevel(1);
                                water.decreaseLevel(1);
                            }
                        } else {
                            int x = water.x < 0 ? water.x + 1 : water.x;
                            int z = water.z < 0 ? water.z + 1 : water.z;
                            byte block = chunkManager.getActiveBlock(x, water.y, z + 1);
                            if (block == Type.AIR) {
                                waters.put(new Pair(water.x, water.y, water.z + 1).hashCode(), new Water(water.x, water.y, water.z + 1, Type.WATER1));
                                water.decreaseLevel(1);
                            }
                        }
                    }
                    if (water.getLevel() > 1) {
                        if (waters.containsKey(new Pair(water.x, water.y, water.z - 1).hashCode())) {
                            Water back = waters.get(new Pair(water.x, water.y, water.z - 1).hashCode());
                            if (back.getLevel() + 1 < water.getLevel()) {
                                back.increaseLevel(1);
                                water.decreaseLevel(1);
                            }
                        } else {
                            int x = water.x < 0 ? water.x + 1 : water.x;
                            int z = water.z < 0 ? water.z + 1 : water.z;
                            byte block = chunkManager.getActiveBlock(x, water.y, z - 1);
                            if (block == Type.AIR) {
                                waters.put(new Pair(water.x, water.y, water.z - 1).hashCode(), new Water(water.x, water.y, water.z - 1, Type.WATER1));
                                water.decreaseLevel(1);
                            }
                        }
                    }
                }
            } else {
                water.setFresh(false);
            }

        }
        for (Water water : waters.values()) {
            if (water.getLevel() == 0) {
                waters.remove(new Pair(water.x, water.y, water.z).hashCode());
                System.out.println("removed");
            } else {
                int x = water.x < 0 ? water.x + 1 : water.x;
                int z = water.z < 0 ? water.z + 1 : water.z;
                chunkManager.setActiveBlock(new Vector3f(x, water.y, z), (byte) -water.getLevel());
            }
        }
        //System.out.println("Time to simulate: " + (System.nanoTime() / start) / 1000000 + " ms.");
        start = System.nanoTime();
        createVBO();
        //System.out.println("Time to create VBO: " + (System.nanoTime() / start) / 1000000 + " ms.");
    }

    private void createVBO() {
        vertices = waters.size() * 24;
        final int vertexSize = 3;
        FloatBuffer vertexData = BufferUtils.createFloatBuffer(vertices * vertexSize);
        FloatBuffer normalData = BufferUtils.createFloatBuffer(vertices * vertexSize);
        for (Water water : waters.values()) {
            int x = water.x;
            int y = water.y;
            int z = water.z;
            float h = (water.getLevel() / 10f);
            vertexData.put(new float[]{0 + x, h + y, 0 + z, 0 + x, h + y, 1 + z, 1 + x, h + y, 1 + z, 1 + x, h + y, 0 + z, // top
                0 + x, 0 + y, 0 + z, 1 + x, 0 + y, 0 + z, 1 + x, 0 + y, 1 + z, 0 + x, 0 + y, 1 + z, // bottom
                0 + x, h + y, 0 + z, 0 + x, 0 + y, 0 + z, 0 + x, 0 + y, 1 + z, 0 + x, h + y, 1 + z, // left
                1 + x, h + y, 0 + z, 1 + x, h + y, 1 + z, 1 + x, 0 + y, 1 + z, 1 + x, 0 + y, 0 + z, // right
                0 + x, h + y, 1 + z, 0 + x, 0 + y, 1 + z, 1 + x, 0 + y, 1 + z, 1 + x, h + y, 1 + z, // front
                0 + x, h + y, 0 + z, 1 + x, h + y, 0 + z, 1 + x, 0 + y, 0 + z, 0 + x, 0 + y, 0 + z // back
        });

            normalData.put(new float[]{0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, // top
                0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, // bottom
                -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, // left
                1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, // right
                0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, // front
                0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1 // back
        });

        }
        vertexData.flip();
        normalData.flip();

        //vertexHandle = 100;//glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexHandle);
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STREAM_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        //normalHandle = 100;//glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, normalHandle);
        glBufferData(GL_ARRAY_BUFFER, normalData, GL_STREAM_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

}
