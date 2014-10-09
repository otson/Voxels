/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package npc;

import java.nio.FloatBuffer;
import java.util.concurrent.ConcurrentHashMap;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import org.lwjgl.util.vector.Vector3f;
import voxels.Camera.Camera;
import voxels.ChunkManager.Chunk;
import voxels.ChunkManager.ChunkManager;
import voxels.ChunkManager.Type;
import voxels.Voxels;
import static voxels.Voxels.toXid;
import static voxels.Voxels.toYid;
import static voxels.Voxels.toZid;

/**
 *
 * @author otso
 */
public class Monster {

    private static float fallingSpeed = 0.013f;

    private float x;
    private float y;
    private float z;
    private float movSpeed = 0.10f;
    private static int staticId = 0;
    private int vertexHandle;
    private int colorHandle;
    private int normalHandle;
    private int id;
    private float currentFallingSpeed = 0;
    private Camera camera;
    private ChunkManager chunkManager;

    Monster(float x, float y, float z, Camera camera, ChunkManager chunkManager) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.chunkManager = chunkManager;
        id = staticId;
        staticId++;
        movSpeed += (float) (movSpeed * 2 * Math.random());
        this.camera = camera;
    }

    public void act() {

        Chunk chunk = chunkManager.getActiveChunk(toXid((int) x), toYid((int) y), toZid((int) z));
        if (chunk == null) {
            //System.out.println("null");
            return;
        }
        float distX = camera.x() - x;
        float distY = camera.y() - y;
        float distZ = camera.z() - z;

        if (distX < 0) {
            distX *= -1;
        }
        if (distY < 0) {
            distY *= -1;
        }
        if (distZ < 0) {
            distZ *= -1;
        }
        float xCoef = distX / ((distX + distY + distZ));
        float yCoef = distY / ((distX + distY + distZ));
        float zCoef = distZ / ((distX + distY + distZ));
        boolean moved = false;

        if (moveY(-currentFallingSpeed)) {
            currentFallingSpeed += fallingSpeed;
        } else {
            currentFallingSpeed = 0;
        }

        if (camera.x() > x + movSpeed) {
            if (!moveX(movSpeed * xCoef)) {
                jump();
                moved = true;
            }
        } else if (camera.x() < x - movSpeed) {
            if (!moveX(-movSpeed * xCoef)) {
                jump();
                moved = true;
            }
        }

        if (camera.z() > z + movSpeed) {
            if (!moveZ(movSpeed * zCoef)) {
                jump();
                moved = true;
            }
        } else if (camera.z() < z - movSpeed) {
            if (!moveZ(-movSpeed * zCoef)) {
                jump();
                moved = true;
            }
        }
        
        //System.out.println("Speed: "+(movSpeed*xCoef+movSpeed*yCoef+movSpeed*zCoef));
    }

    private void jump() {
        if (currentFallingSpeed >= 0) {
            if(Math.random() > 0.99)
                currentFallingSpeed = -0.8f;
            else
                currentFallingSpeed = -0.4f;
        }
    }

    private boolean moveX(float amount) {
        if (chunkManager.getActiveBlock(new Vector3f((int) (x + amount), (int) y, (int) z)) == Type.AIR) {
            x += amount;
            return true;
        } else {
            return false;
        }
    }

    private boolean moveY(float amount) {
        if (chunkManager.getActiveBlock(new Vector3f((int) x, (int) (y + amount), (int) z)) == Type.AIR) {
            y += amount;
            return true;
        } else {
            return false;
        }
    }

    private boolean moveZ(float amount) {
        if (chunkManager.getActiveBlock(new Vector3f((int) x, (int) y, (int) (z + amount))) == Type.AIR) {
            z += amount;
            return true;
        } else {
            return false;
        }
    }

    public int createRender() {
        final int amountOfVertices = 24;
        final int vertexSize = 3;
        final int colorSize = 3;

        FloatBuffer vertexData = BufferUtils.createFloatBuffer(amountOfVertices * vertexSize);
        vertexData.put(new float[]{0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, // top
            0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, // bottom
            0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, // left
            1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 0, // right
            0, 1, 1, 0, 0, 1, 1, 0, 1, 1, 1, 1, // front
            0, 1, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0 // back
    });
        vertexData.flip();
        FloatBuffer normalData = BufferUtils.createFloatBuffer(amountOfVertices * vertexSize);
        normalData.put(new float[]{0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, // top
       
             0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, // bottom
            -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, // left
            1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, // right
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, // front
            0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1 // back
    });
        normalData.flip();
        
        float r = (float) Math.random();
        float g = (float) Math.random();
        float b = (float) Math.random();
        
        FloatBuffer colorData = BufferUtils.createFloatBuffer(amountOfVertices * vertexSize);
        colorData.put(new float[]{r, g, b, r, g, b, r, g, b, r, g, b, // top
            r, g, b, r, g, b, r, g, b, r, g, b, // bottom
            r, g, b, r, g, b, r, g, b, r, g, b, // left
            r, g, b, r, g, b, r, g, b, r, g, b, // right
            r, g, b, r, g, b, r, g, b, r, g, b, // front
            r, g, b, r, g, b, r, g, b, r, g, b, // back
    });
        colorData.flip();

        int vboVertexHandle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandle);
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        int vboNormalHandle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboNormalHandle);
        glBufferData(GL_ARRAY_BUFFER, normalData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vboColorHandle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboColorHandle);
        glBufferData(GL_ARRAY_BUFFER, colorData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        vertexHandle = vboVertexHandle;
        colorHandle = vboColorHandle;
        normalHandle = vboNormalHandle;
        return vboVertexHandle;
    }

    public int getId() {
        return id;
    }

    public int getVertexHandle() {
        return vertexHandle;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public int getColorHandle() {
        return colorHandle;
    }

    public float getDistance() {
        float xDist = x-camera.x();
        float zDist = z-camera.z();
        
        return xDist*xDist+zDist*zDist;
    }

    public int getNormalHandle() {
        return normalHandle;
    }
    
}
