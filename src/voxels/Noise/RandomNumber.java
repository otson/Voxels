/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels.Noise;

import java.util.Random;

/**
 *
 * @author otso
 */
public class RandomNumber {
    
    private static final long SEED = 1;
    public static Random random = new Random(SEED);
    
    public static double getRandom(){
        return random.nextDouble();
    }

}
