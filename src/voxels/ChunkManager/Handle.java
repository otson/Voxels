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
