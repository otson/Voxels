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
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import voxels.Camera.Camera;

/**
 *
 * @author otso
 */


public class Monster {
    float x;
    float y;
    float z;
    private float movSpeed = 0.08f;
    private static int staticId = 0;
    private int handle;
    private int id;
    private Camera camera;

    public Monster() {
        x = 10;
        y = 250;
        z = 0;
        id = staticId;
        staticId++;
        
    }

    Monster(int x, int y, int z, Camera camera) {
        this.x = x;
        this.y = y;
        this.z = z;
        id = staticId;
        staticId++;
        this.camera = camera;
    }
    
    public void act(){
        if(camera.x()>x+movSpeed)
            x+=movSpeed;
        else if(camera.x()<x-movSpeed)
            x-=movSpeed;
        
        if(camera.y()>y+movSpeed)
            y+=movSpeed;
        else if(camera.y()<y-movSpeed)
            y-=movSpeed;
        
        if(camera.z()>z+movSpeed)
            z+=movSpeed;
        else if(camera.z()<z-movSpeed)
            z-=movSpeed;
    }
    
    public int createRender(){
        final int amountOfVertices = 24;
        final int vertexSize = 3;

        FloatBuffer vertexData = BufferUtils.createFloatBuffer(amountOfVertices * vertexSize);
        vertexData.put(new float[]{0,1,0, 0,1,1, 1,1,1, 1,1,0, // top
                                   0,0,0, 1,0,0, 1,0,1, 0,0,1, // bottom
                                   0,1,0, 0,0,0, 0,0,1, 0,1,1, // left
                                   1,1,0, 1,1,1, 1,0,1, 1,0,0, // right
                                   0,1,1, 0,0,1, 1,0,1, 1,1,1, // front
                                   0,1,0, 1,1,0, 1,0,0, 0,0,0  // back
        });
        vertexData.flip();
        
        int vboVertexHandle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandle);
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        handle = vboVertexHandle;
        return vboVertexHandle;
    }

    public int getId() {
        return id;
    }

    public int getHandle() {
        return handle;
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
    
    
    
    
    
    
    
    

}
