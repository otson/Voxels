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
    
    public ItemHandler(ChunkManager chunkManager){
        this.chunkManager = chunkManager;
        this.chunkManager.setItemHandler(this);
    }
    
    public void processItemPhysics(){
        for(ItemLocation item : droppedBlocks){
            byte block = chunkManager.getActiveBlock(new Vector3f(item.x, item.y-item.getFallingSpeed()-0.5f, item.z));
            if(block == Type.AIR){
                item.fall();
            }
            else{
                item.y = (int)item.y+0.5f;
                item.setFallingSpeed(0);
            }
            item.rotate();
        }
    }

    public BlockingQueue<ItemLocation> getDroppedBlocks() {
        return droppedBlocks;
    }
    
}
