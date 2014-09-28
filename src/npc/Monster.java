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
import static org.lwjgl.opengl.GL15.glGenBuffers;

/**
 *
 * @author otso
 */


public class Monster {
    float x;
    float y;
    float z;
    private static int staticId = 0;
    private int handle;
    private int id;

    public Monster() {
        x = 10;
        y = 250;
        z = 0;
        id = staticId;
        staticId++;
        
    }

    Monster(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        id = staticId;
        staticId++;
    }
    
    public int createRender(){
        final int amountOfVertices = 4;
        final int vertexSize = 3;
        final int colorSize = 3;

        FloatBuffer vertexData = BufferUtils.createFloatBuffer(amountOfVertices * vertexSize);
        vertexData.put(new float[]{0,0,0, 0,0,5, 5,0,5, 5,0,0});
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
