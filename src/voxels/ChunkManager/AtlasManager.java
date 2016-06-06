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

    public static float getX(int type) {
        return 0;//type % 8 / 8f;
    }

    public static float getY(int type) {
        return type / 16f;
    }
    
    public static float getFrontXOff(short type){
        return getX(type);
    }
    
    public static float getFrontYOff(short type){
        return getY(type);
    }
    
    public static float getBackXOff(short type){
        return getX(type);
    }
    public static float getBackYOff(short type){
        return getY(type);
    }
    
    public static float getRightXOff(short type){
        return getX(type);
    }
    
    public static float getRightYOff(short type){
        return getY(type);
    }
    
    public static float getLeftXOff(short type){
        return getX(type);
    }
    
    public static float getLeftYOff(short type){
        return getY(type);
    }
    
    public static float getTopXOff(short type){
        return getX(type);
    }
    
    public static float getTopYOff(short type){
        return getY(type);
    }
    
    public static float getBottomXOff(short type){
        return getX(type);
    }
    
    public static float getBottomYOff(short type){
        return getY(type);
    }

}
