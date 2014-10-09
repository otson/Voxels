/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package npc;

import java.util.ArrayList;
import java.util.HashMap;
import voxels.Camera.Camera;
import voxels.ChunkManager.Chunk;
import voxels.Voxels.*;
import voxels.ChunkManager.ChunkManager;

/**
 *
 * @author otso
 */
public class npcHandler {
    private HashMap<Integer, Monster> monsterList;
    private ChunkManager chunkManager;
    private HashMap<Integer, Integer> npcHandles;
    private Camera camera;
    private int maxDistance = Chunk.CHUNK_SIZE*8;

    public npcHandler(ChunkManager chunkManager, Camera camera) {
        monsterList = new HashMap<>();
        npcHandles = new HashMap<>();
        this.camera = camera;
        this.chunkManager = chunkManager;
    }
    
    public void addNPC(float x, float y, float z, ChunkManager chunkManager){
        Monster monster = new Monster(x,y,z, camera, chunkManager);
        //System.out.println("id: "+monster.getId());
        npcHandles.put(monster.getId(), monster.createRender());
        monsterList.put(monster.getId(), monster);
    }

    public HashMap<Integer, Monster> getMonsterList() {
        return monsterList;
    }

    public HashMap<Integer, Integer> getNpcHandles() {
        return npcHandles;
    }
    
    public void processMonsters(){
        for(Monster monster: monsterList.values()){
            if(monster.getDistance() <= maxDistance*maxDistance)
            monster.act();
            //monsterList.put(monster.getId(), monster);
        }
    }
    
}
