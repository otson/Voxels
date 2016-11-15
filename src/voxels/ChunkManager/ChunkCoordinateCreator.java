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

import java.util.concurrent.ConcurrentHashMap;
import voxels.Voxels;

/**
 *
 * @author otso
 */
public class ChunkCoordinateCreator {

    private int currentChunkX;
    private int currentChunkZ;
    private int maxDistance = Voxels.chunkCreationDistance;
    private int dx = 1;
    private int dz = -1;
    private int x = 0;
    private int z = 0;
    private int currentLength = 0;
    private int length = 1;
    private int count = 0;
    private int turnCount = 0;
    private boolean needMiddleChunk = true;
    private ConcurrentHashMap<Integer, byte[]> map;
    private int heightCount = 0;
    private Coordinates currentXZ;

    public ChunkCoordinateCreator(ConcurrentHashMap<Integer, byte[]> map) {
        this.map = map;
        currentXZ = getNewXZCoordinates();
    }

    private Coordinates getNewXZCoordinates() {

        if (needMiddleChunk) {
            needMiddleChunk = false;
            return new Coordinates(currentChunkX, null, currentChunkZ);
        }
        if (dz != 0) {
            z += dz;
            currentLength++;
            if (currentLength == length) {
                currentLength = 0;
                dx = dz;
                dz = 0;
                turnCount++;
                if (turnCount == 2)
                    length++;
            }
        }
        else {
            x += dx;
            currentLength++;
            if (currentLength == length) {
                currentLength = 0;
                dz = -dx;
                dx = 0;
                turnCount++;
                if (turnCount == 2) {
                    length++;
                    turnCount = 0;
                }
            }
        }
        return new Coordinates(x + currentChunkX, null, z + currentChunkZ);

    }

    public Coordinates getXYZ() {
        while (notAtMax()) {
            if (heightCount < Chunk.VERTICAL_CHUNKS) {
                heightCount++;    
                if (!map.containsKey(new Triple(x + currentChunkX, heightCount, z + currentChunkZ).hashCode())) {
                    return new Coordinates(x + currentChunkX, heightCount, z + currentChunkZ);
                }
            }
            else {
                heightCount = 0;
                getNewXZCoordinates();
            }
        }
        return null;
    }

    public void setCurrentChunkX(int currentChunkX) {
        if (currentChunkX != this.currentChunkX)
            reset();
        this.currentChunkX = currentChunkX;
    }

    public void setCurrentChunkZ(int currentChunkZ) {
        if (currentChunkZ != this.currentChunkZ)
            reset();
        this.currentChunkZ = currentChunkZ;
    }

    private void reset() {
        dx = 1;
        dz = -1;
        x = 0;
        z = 0;
        currentLength = 0;
        length = 1;
        count = 0;
        turnCount = 0;
        needMiddleChunk = true;
    }

    public boolean notAtMax() {
        maxDistance = Voxels.chunkCreationDistance;
        return Math.abs(x) <= maxDistance && Math.abs(z) <= maxDistance;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(int maxDistance) {
        this.maxDistance = maxDistance;
    }
}
