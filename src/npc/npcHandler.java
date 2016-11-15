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
    private int maxDistance = Chunk.CHUNK_SIZE * 8;
    private boolean stopped = false;

    public npcHandler(ChunkManager chunkManager, Camera camera) {
        monsterList = new HashMap<>();
        npcHandles = new HashMap<>();
        this.camera = camera;
        this.chunkManager = chunkManager;
    }

    public void addNPC(float x, float y, float z, ChunkManager chunkManager) {
        Monster monster = new Monster(x, y, z, camera, chunkManager);
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

    public void processMonsters() {
        if (!stopped) {
            for (Monster monster : monsterList.values()) {
                if (monster.getDistance() <= maxDistance * maxDistance) {
                    monster.act();
                }
                //monsterList.put(monster.getId(), monster);
            }
        }
    }

    public void toggle() {
        stopped = !stopped;
    }

}
