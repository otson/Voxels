/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package npc;

import java.util.ArrayList;
import java.util.HashMap;
import voxels.ChunkManager.ChunkManager;

/**
 *
 * @author otso
 */
public class npcHandler {
    private HashMap<Integer, Monster> monsterList;
    private ChunkManager chunkManager;
    private HashMap<Integer, Integer> npcHandles;

    public npcHandler(ChunkManager chunkManager) {
        monsterList = new HashMap<>();
        npcHandles = new HashMap<>();
        this.chunkManager = chunkManager;
    }
    
    public void addNPC(int x, int y, int z){
        Monster monster = new Monster(x,y,z);
        System.out.println("id: "+monster.getId());
        npcHandles.put(monster.getId(), monster.createRender());
        monsterList.put(monster.getId(), monster);
    }

    public HashMap<Integer, Monster> getMonsterList() {
        return monsterList;
    }

    public HashMap<Integer, Integer> getNpcHandles() {
        return npcHandles;
    }
    
    
}
