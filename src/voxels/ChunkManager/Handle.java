package voxels.ChunkManager;

/**
 *
 * @author otso
 */
public class Handle {
    
    public final int vertexHandle;
    public final int normalHandle;
    public final int texHandle;
    public int vertices;
    
    public Handle(int vertexHandle, int normalHandle, int texHandle, int vertices){
        
        this.vertexHandle = vertexHandle;
        this.normalHandle= normalHandle;
        this.texHandle = texHandle;
        this.vertices = vertices;
    }
}
