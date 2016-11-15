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
package Items;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.lwjgl.util.vector.Vector3f;
import voxels.ChunkManager.ChunkManager;
import voxels.ChunkManager.ItemLocation;
import voxels.ChunkManager.Type;
import voxels.Noise.RandomNumber;

/**
 *
 * @author otso
 */
public class ItemHandler {

    private BlockingQueue<ItemLocation> droppedBlocks = new LinkedBlockingQueue<>();
    private ChunkManager chunkManager;
    
    private static int MAX_SIZE = 1000;
    
    public ItemHandler(ChunkManager chunkManager) {
        this.chunkManager = chunkManager;
        this.chunkManager.setItemHandler(this);
    }

    public void processItemPhysics() {
        for (ItemLocation item : droppedBlocks) {
            float adj = -0.25f;
            if (item.getFallingSpeed() > 0) {
                adj *= -1;
            }

            byte block = chunkManager.getBlock(new Vector3f(item.x, item.y - item.getFallingSpeed() - adj, item.z));
            if (block == Type.AIR || block == -1) {
                item.fall();
            } else {
                item.y = (int) item.y + (float)(0.2501+RandomNumber.getRandom()*0.001f);
                item.setFallingSpeed(0);
                item.setxSpeed(0);
                item.setzSpeed(0);
            }
            if (item.getxSpeed() != 0) {
                block = chunkManager.getBlock(new Vector3f(item.x + item.getxSpeed(), item.y - adj, item.z));
                if (block == Type.AIR || block == -1) {
                    item.x += item.getxSpeed();
                } else {
                    item.setxSpeed(0);
                    item.setzSpeed(0);
                }
            }
            if (item.getxSpeed() != 0) {
                block = chunkManager.getBlock(new Vector3f(item.x, item.y - 0.5f, item.z + item.getzSpeed()));
                if (block == Type.AIR || block == -1) {
                    item.z += item.getzSpeed();
                } else {
                    item.setxSpeed(0);
                    item.setzSpeed(0);
                }
            }
            item.rotate();
        }
    }
    
    public void put(ItemLocation item){
        droppedBlocks.offer(item);
        
        while(droppedBlocks.size() > MAX_SIZE){
            droppedBlocks.remove();
        }
    }

    public BlockingQueue<ItemLocation> getDroppedBlocks() {
        return droppedBlocks;
    }

}
