/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package npc;

import voxels.ChunkManager.Location;
import voxels.Voxels;

/**
 *
 * @author otso
 */
public class Monster {

    private float x;
    private float y;
    private float z;

    private float moveSpeed = 0.1f;

    public Monster(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void moveTowardsPlayer() {
        Location pLoc = Voxels.getPlayerLocation();
        if (pLoc.x > x) {
            x += moveSpeed;
        } else {
            x -= moveSpeed ;
        }
        if (pLoc.z > z) {
            z+=moveSpeed;
        } else {
            z-=moveSpeed;
        }

    }
}
