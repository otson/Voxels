/* 
 * Copyright (C) 2016 Otso Nuortimo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
