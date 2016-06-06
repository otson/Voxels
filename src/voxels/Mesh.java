package voxels;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import voxels.ChunkManager.VertexData;

/**
 *
 * @author otso
 */
public class Mesh {

    private static int meshCounter = 0;

    private VertexData[] vertices = null;
    private Vector3f modelPos = null;
    private Vector3f modelAngle = null;
    private Vector3f modelScale = null;
    private Matrix4f modelMatrix;
    private ByteBuffer verticesByteBuffer = null;

    private int vboId;
    private int vaoId;
    private int vboiId;
    private int indicesCount;
    private int id;

    public Mesh() {
        this(new Vector3f(0, 0, 0));
    }

    public Mesh(Vector3f modelPos) {
        this.modelPos = modelPos;
        modelAngle = new Vector3f();
        modelScale = new Vector3f(1, 1, 1);
        modelMatrix = new Matrix4f();
        id = meshCounter;
        meshCounter++;
        createMesh();
    }

    private void createMesh() {
        // We'll define our quad using 4 vertices of the custom 'TexturedVertex' class
        VertexData v0 = new VertexData();
        v0.setXYZ(-0.5f, 0.5f, 0.5f);
        v0.setRGB(1, 0, 0);
        v0.setST(0, 0);
        VertexData v1 = new VertexData();
        v1.setXYZ(-0.5f, -0.5f, 0.5f);
        v1.setRGB(0, 1, 0);
        v1.setST(0, 1);
        VertexData v2 = new VertexData();
        v2.setXYZ(0.5f, -0.5f, 0.5f);
        v2.setRGB(0, 0, 1);
        v2.setST(1, 1);
        VertexData v3 = new VertexData();
        v3.setXYZ(0.5f, 0.5f, 0.5f);
        v3.setRGB(1, 1, 1);
        v3.setST(1, 0);

        VertexData v4 = new VertexData();
        v4.setXYZ(-0.5f, 0.5f, -0.5f);
        v4.setRGB(1, 0, 0);
        v4.setST(0, 0);
        VertexData v5 = new VertexData();
        v5.setXYZ(-0.5f, -0.5f, -0.5f);
        v5.setRGB(0, 1, 0);
        v5.setST(0, 1);
        VertexData v6 = new VertexData();
        v6.setXYZ(-0.5f, -0.5f, 0.5f);
        v6.setRGB(0, 0, 1);
        v6.setST(1, 1);
        VertexData v7 = new VertexData();
        v7.setXYZ(-0.5f, 0.5f, 0.5f);
        v7.setRGB(1, 1, 1);
        v7.setST(1, 0);

        VertexData v8 = new VertexData();
        v8.setXYZ(-0.5f, 0.5f, -0.5f);
        v8.setRGB(1, 0, 0);
        v8.setST(0, 0);
        VertexData v9 = new VertexData();
        v9.setXYZ(-0.5f, 0.5f, 0.5f);
        v9.setRGB(0, 1, 0);
        v9.setST(0, 1);
        VertexData v10 = new VertexData();
        v10.setXYZ(0.5f, 0.5f, 0.5f);
        v10.setRGB(0, 0, 1);
        v10.setST(1, 1);
        VertexData v11 = new VertexData();
        v11.setXYZ(0.5f, 0.5f, -0.5f);
        v11.setRGB(1, 1, 1);
        v11.setST(1, 0);

        VertexData v12 = new VertexData();
        v12.setXYZ(0.5f, -0.5f, 0.5f);
        v12.setRGB(1, 0, 0);
        v12.setST(0, 0);
        VertexData v13 = new VertexData();
        v13.setXYZ(-0.5f, -0.5f, 0.5f);
        v13.setRGB(0, 1, 0);
        v13.setST(0, 1);
        VertexData v14 = new VertexData();
        v14.setXYZ(-0.5f, -0.5f, -0.5f);
        v14.setRGB(0, 0, 1);
        v14.setST(1, 1);
        VertexData v15 = new VertexData();
        v15.setXYZ(0.5f, -0.5f, -0.5f);
        v15.setRGB(1, 1, 1);
        v15.setST(1, 0);

        VertexData v16 = new VertexData();
        v16.setXYZ(0.5f, -0.5f, -0.5f);
        v16.setRGB(1, 0, 0);
        v16.setST(0, 0);
        VertexData v17 = new VertexData();
        v17.setXYZ(-0.5f, -0.5f, -0.5f);
        v17.setRGB(0, 1, 0);
        v17.setST(0, 1);
        VertexData v18 = new VertexData();
        v18.setXYZ(-0.5f, 0.5f, -0.5f);
        v18.setRGB(0, 0, 1);
        v18.setST(1, 1);
        VertexData v19 = new VertexData();
        v19.setXYZ(0.5f, 0.5f, -0.5f);
        v19.setRGB(1, 1, 1);
        v19.setST(1, 0);

        VertexData v20 = new VertexData();
        v20.setXYZ(0.5f, -0.5f, 0.5f);
        v20.setRGB(1, 0, 0);
        v20.setST(0, 0);
        VertexData v21 = new VertexData();
        v21.setXYZ(0.5f, -0.5f, -0.5f);
        v21.setRGB(0, 1, 0);
        v21.setST(0, 1);
        VertexData v22 = new VertexData();
        v22.setXYZ(0.5f, 0.5f, -0.5f);
        v22.setRGB(0, 0, 1);
        v22.setST(1, 1);
        VertexData v23 = new VertexData();
        v23.setXYZ(0.5f, 0.5f, 0.5f);
        v23.setRGB(1, 1, 1);
        v23.setST(1, 0);

        vertices = new VertexData[]{v0, v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23};

        // Put each 'Vertex' in one FloatBuffer
        verticesByteBuffer = BufferUtils.createByteBuffer(vertices.length
                * VertexData.stride);
        FloatBuffer verticesFloatBuffer = verticesByteBuffer.asFloatBuffer();
        for (VertexData vertice : vertices) {
            // Add position, color and texture floats to the buffer
            verticesFloatBuffer.put(vertice.getElements());
        }
        verticesFloatBuffer.flip();

        // OpenGL expects to draw vertices in counter clockwise order by default
        short[] indices = new short[6 * vertices.length / 4];
        for (short i = 0; i < indices.length / 6; i++) {
            // 0, 1, 2, 2, 3, 0,4, 5 , 0, 5, 1, 0,  1, 5, 2, 5, 6, 2,  3, 0, 7, 0, 2, 7,  8, 6, 4, 6, 5, 4,  2, 3, 8, 3, 6, 8
            indices[i * 6 + 0] = (short) (0 + 4 * i);
            indices[i * 6 + 1] = (short) (1 + 4 * i);
            indices[i * 6 + 2] = (short) (2 + 4 * i);
            indices[i * 6 + 3] = (short) (2 + 4 * i);
            indices[i * 6 + 4] = (short) (3 + 4 * i);
            indices[i * 6 + 5] = (short) (0 + 4 * i);
        }
        
        //indices = new byte[]{0, 1, 2, 2, 3, 0,4, 5 , 0, 5, 1, 0,  1, 5, 2, 5, 6, 2,  3, 0, 7, 0, 2, 7,  8, 6, 4, 6, 5, 4,  2, 3, 8, 3, 6, 8};
        vertices = null;
        
        indicesCount = indices.length;
        ShortBuffer indicesBuffer = BufferUtils.createShortBuffer(indicesCount);
        indicesBuffer.put(indices);
        indicesBuffer.flip();

        // Create a new Vertex Array Object in memory and select it (bind)
        vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        // Create a new Vertex Buffer Object in memory and select it (bind)
        vboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesFloatBuffer, GL15.GL_STREAM_DRAW);

        // Put the position coordinates in attribute list 0
        GL20.glVertexAttribPointer(0, VertexData.positionElementCount, GL11.GL_FLOAT,
                false, VertexData.stride, VertexData.positionByteOffset);
        // Put the color components in attribute list 1
        GL20.glVertexAttribPointer(1, VertexData.colorElementCount, GL11.GL_FLOAT,
                false, VertexData.stride, VertexData.colorByteOffset);
        // Put the texture coordinates in attribute list 2
        GL20.glVertexAttribPointer(2, VertexData.textureElementCount, GL11.GL_FLOAT,
                false, VertexData.stride, VertexData.textureByteOffset);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        // Deselect (bind to 0) the VAO
        GL30.glBindVertexArray(0);

        // Create a new VBO for the indices and select it (bind) - INDICES
        vboiId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer,
                GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

    }

    public Vector3f getModelPos() {
        return modelPos;
    }

    public Vector3f getModelAngle() {
        return modelAngle;
    }

    public Vector3f getModelScale() {
        return modelScale;
    }

    public int getVboId() {
        return vboId;
    }

    public int getVaoId() {
        return vaoId;
    }

    public int getVboiId() {
        return vboiId;
    }

    public int getIndicesCount() {
        return indicesCount;
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    public void resetModelMatrix() {
        modelMatrix = new Matrix4f();
    }

    public int getId() {
        return id;
    }

}
