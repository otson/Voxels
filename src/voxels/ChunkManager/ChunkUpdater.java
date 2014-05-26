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
public class ChunkUpdater extends Thread{
    
    Chunk chunk;
    
    public ChunkUpdater(Chunk chunk){
        this.chunk = chunk;
        
    }
    
    @Override
    public void run(){
        
    }
}
