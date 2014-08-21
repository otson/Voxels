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
public class AtlasManager {

    public static final int GRASS = 0;
    public static final int DIRT = 1;
    public static final int CLOUD = 2;
    public static final int STONE = 3;
    public static final int UNBREAKABLE = 4;

    public static float getX(int type) {
        return type % 10 / 10f;
    }

    public static float getY(int type) {
        return (type-type % 10) / 10f;
    }

}
