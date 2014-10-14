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
    public Coordinates translate;
    
    public Handle(int vertexHandle, int normalHandle, int texHandle, int vertices, Coordinates translate){
        
        this.vertexHandle = vertexHandle;
        this.normalHandle= normalHandle;
        this.texHandle = texHandle;
        this.vertices = vertices;
        this.translate = translate;
    }
    
    public int translateX(){
        return translate.x;
    }
    public int translateY(){
        return translate.y;
    }
    public int translateZ(){
        return translate.z;
    }
}
