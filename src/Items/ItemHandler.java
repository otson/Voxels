/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Items;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.lwjgl.util.vector.Vector3f;
import voxels.ChunkManager.ChunkManager;
import voxels.ChunkManager.ItemLocation;
import voxels.ChunkManager.Type;

/**
 *
 * @author otso
 */
public class ItemHandler {

    private BlockingQueue<ItemLocation> droppedBlocks = new LinkedBlockingQueue<>();
    private ChunkManager chunkManager;
    
    private static int MAX_SIZE = 2000;
    
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

            byte block = chunkManager.getActiveBlock(new Vector3f(item.x, item.y - item.getFallingSpeed() - adj, item.z));
            if (block == Type.AIR || block == -1) {
                item.fall();
            } else {
                item.y = (int) item.y + 0.2501f;
                item.setFallingSpeed(0);
                item.setxSpeed(0);
                item.setzSpeed(0);
            }
            if (item.getxSpeed() != 0) {
                block = chunkManager.getActiveBlock(new Vector3f(item.x + item.getxSpeed(), item.y - adj, item.z));
                if (block == Type.AIR || block == -1) {
                    item.x += item.getxSpeed();
                } else {
                    item.setxSpeed(0);
                    item.setzSpeed(0);
                }
            }
            if (item.getxSpeed() != 0) {
                block = chunkManager.getActiveBlock(new Vector3f(item.x, item.y - 0.5f, item.z + item.getzSpeed()));
                if (block == Type.AIR || block == -1) {
                    item.x += item.getxSpeed();
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
