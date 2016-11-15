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
public class AtlasManager {

    public static float getX(int type) {
        return 0;//type % 8 / 8f;
    }

    public static float getY(int type) {
        return type / 16f;
    }
    
    public static float getFrontXOff(short type){
        return getX(type);
    }
    
    public static float getFrontYOff(short type){
        return getY(type);
    }
    
    public static float getBackXOff(short type){
        return getX(type);
    }
    public static float getBackYOff(short type){
        return getY(type);
    }
    
    public static float getRightXOff(short type){
        return getX(type);
    }
    
    public static float getRightYOff(short type){
        return getY(type);
    }
    
    public static float getLeftXOff(short type){
        return getX(type);
    }
    
    public static float getLeftYOff(short type){
        return getY(type);
    }
    
    public static float getTopXOff(short type){
        return getX(type);
    }
    
    public static float getTopYOff(short type){
        return getY(type);
    }
    
    public static float getBottomXOff(short type){
        return getX(type);
    }
    
    public static float getBottomYOff(short type){
        return getY(type);
    }

}
