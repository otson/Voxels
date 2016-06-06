/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package voxels.ChunkManager;

/**
 *
 * @author otso
 */
public class Vertex {
    public int topLeftX;
    public int bottomLeftX;
    public int bottomRightX;
    public int topRightX;
    public int topLeftY;
    public int bottomLeftY;
    public int bottomRightY;
    public int topRightY;
    public int topLeftZ;
    public int bottomLeftZ;
    public int bottomRightZ;
    public int topRightZ;
    public int width;
    
    public short type;
    public short side;

    public Vertex(int topLeftX, int topLeftY, int topLeftZ, int bottomLeftX, int bottomLeftY, int bottomLeftZ, int bottomRightX, int bottomRightY, int bottomRightZ, int topRightX, int topRightY, int topRightZ, short type,int width, short side){
        this.topLeftX = topLeftX;
        this.topLeftY = topLeftY;
        this.topLeftZ = topLeftZ;
        
        this.bottomLeftX = bottomLeftX;
        this.bottomLeftY = bottomLeftY;
        this.bottomLeftZ = bottomLeftZ;
        
        this.bottomRightX = bottomRightX;
        this.bottomRightY = bottomRightY;
        this.bottomRightZ = bottomRightZ;
        
        this.topRightX = topRightX;
        this.topRightY = topRightY;
        this.topRightZ = topRightZ;
        
        this.type = type;
        this.width = width;
        this.side = side;
        
    }
    

}
