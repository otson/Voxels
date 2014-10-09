/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels.ChunkManager;

import java.nio.FloatBuffer;
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
public class BlockRenders {

    private Handle dirt;
    private Handle stone;
    private Handle leaves;
    private Handle cloud;
    private Handle wood;

    private static int vertexSize = 3;
    private static int normalSize = 3;
    private static int texSize = 2;
    private static int colorSize = 3;

    private FloatBuffer vertexData;
    private FloatBuffer normalData;
    private FloatBuffer texData;

    public BlockRenders() {
        init();
    }

    private void init() {
        dirt = createVBO(Type.DIRT);
        stone = createVBO(Type.STONE);
        leaves = createVBO(Type.LEAVES);
        cloud = createVBO(Type.CLOUD);
        wood = createVBO(Type.WOOD);
        
        System.out.println("init done");
        if(cloud == null)
            System.out.println("ERROR");
    }

    private Handle createVBO(byte type) {

        int vertices = 24;

        vertexData = BufferUtils.createFloatBuffer(vertices * vertexSize);
        normalData = BufferUtils.createFloatBuffer(vertices * vertexSize);
        texData = BufferUtils.createFloatBuffer(vertices * texSize);
        float frontXOff = AtlasManager.getFrontXOff(type);
        float frontYOff = AtlasManager.getFrontYOff(type);

        float backXOff = AtlasManager.getBackXOff(type);
        float backYOff = AtlasManager.getBackYOff(type);

        float rightXOff = AtlasManager.getRightXOff(type);
        float rightYOff = AtlasManager.getRightYOff(type);

        float leftXOff = AtlasManager.getLeftXOff(type);
        float leftYOff = AtlasManager.getLeftYOff(type);

        float topXOff;
        float topYOff;

        if (type != Type.DIRT) {
            topXOff = AtlasManager.getTopXOff(type);
            topYOff = AtlasManager.getTopYOff(type);
        } else {
            topXOff = AtlasManager.getTopXOff(Type.GRASS);
            topYOff = AtlasManager.getTopYOff(Type.GRASS);
        }

        float bottomXOff = AtlasManager.getBottomXOff(type);
        float bottomYOff = AtlasManager.getBottomYOff(type);

        float t = 1f / 8f;

        vertexData.put(new float[]{0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, // top
            0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, // bottom
            0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, // left
            1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 0, // right
            0, 1, 1, 0, 0, 1, 1, 0, 1, 1, 1, 1, // front
            0, 1, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0 // back
    });
        vertexData.flip();

        normalData.put(new float[]{0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, // top
            0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, // bottom
            -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, // left
            1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, // right
            0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, // front
            0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1 // back
    });
        normalData.flip();

        texData.put(new float[]{topXOff, topYOff, topXOff, topYOff+t, topXOff+t, topYOff+t, topXOff+t, topYOff, // top
            bottomXOff, bottomYOff, bottomXOff+t, bottomYOff, bottomXOff+t, bottomYOff+t, bottomXOff, bottomYOff+t, // bottom
            leftXOff, leftYOff, leftXOff, leftYOff+t, leftXOff+t, leftYOff+t, leftXOff+t, leftYOff, // left
            rightXOff, rightYOff, rightXOff, rightYOff+t, rightXOff+t, rightYOff+t, rightXOff+t, rightYOff, // right
            frontXOff, frontYOff, frontXOff, frontYOff+t, frontXOff+t, frontYOff+t, frontXOff+t, frontYOff, // front
            backXOff, backYOff+t, backXOff+t, backYOff+t, backXOff+t, backYOff, backXOff, backYOff // back
    });
        texData.flip();
        

        int vboVertexHandle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandle);
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vboNormalHandle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboNormalHandle);
        glBufferData(GL_ARRAY_BUFFER, normalData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        int vboTexHandle = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboTexHandle);
        glBufferData(GL_ARRAY_BUFFER, texData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        
        return new Handle(vboVertexHandle, vboNormalHandle, vboTexHandle, vertices);
    }
    
    public Handle getHandle(byte type){
        System.out.println("type: "+type);
        if(type == Type.DIRT)
            return dirt;
        if(type == Type.STONE)
            return stone;
        if(type == Type.LEAVES)
            return leaves;
        if(type == Type.CLOUD)
            return cloud;
        if(type == Type.WOOD)
            return wood;
        else{
            System.out.println("Returning null...");
            return null;
        }
    }

    public Handle getDirt() {
        return dirt;
    }

    public Handle getStone() {
        return stone;
    }

    public Handle getLeaves() {
        return leaves;
    }

    public Handle getCloud() {
        return cloud;
    }

    public Handle getWood() {
        return wood;
    }
    
    
}
