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
public final class Type {

    public static final byte AIR = 0;
    public static final byte WATER = 1;
    public static final byte WOOD = 2;
    public static final byte LEAVES = 3;
    public static final byte STONE = 4;
    public static final byte CLOUD = 5;
    public static final byte UNBREAKABLE = 6;
    public static final byte GRASS = 7;
    public static final byte DIRT = 8;
    public static final byte SAND = 9;
    public static final byte CACTUS = 10;
    public static final byte ROCKSAND = 11;
    public static final byte SHORE = 12;
    
    public static final byte WATER1 = -1;
    public static final byte WATER2 = -2;
    public static final byte WATER3 = -3;
    public static final byte WATER4 = -4;
    public static final byte WATER5 = -5;
    public static final byte WATER6 = -6;
    public static final byte WATER7 = -7;
    public static final byte WATER8 = -8;
    public static final byte WATER9 = -9;
    public static final byte WATER10 = -10;
    
    private static final String[] blockNames = {"Air", "Water", "Wood", "Leaves", "Stone", "Cloud","Unbreakable","Grass","Dirt","Sand","Cactus","Rocksand","Shore"};
    
    public static String getBlockName(byte b){
        return blockNames[b];
    }
}
