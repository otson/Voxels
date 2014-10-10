/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels;

import org.lwjgl.opengl.ATIMeminfo;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.NVXGpuMemoryInfo;

/**
 *
 * @author otso
 */
public class DebugInfo {

    public static int fps = 0;
    public static int verticesDrawn = 0;
    public static int chunksLoaded = 0;
    public static int chunkTotal = 0;
    public static int activeItems = 0;
    public static int activeNPCs = 0;

    public static int get_video_card_total_memory() { // If previous errors have not been called yet!
        int nvidia_total_memory = GL11.glGetInteger(NVXGpuMemoryInfo.GL_GPU_MEMORY_INFO_TOTAL_AVAILABLE_MEMORY_NVX);
        int ati_total_memory = GL11.glGetInteger(ATIMeminfo.GL_VBO_FREE_MEMORY_ATI);
        return Math.max(nvidia_total_memory, ati_total_memory);
    }
    
    public static int get_video_card_used_memory() { // If previous errors have not been called yet!
        int nvidia_used_memory = GL11.glGetInteger(NVXGpuMemoryInfo.GL_GPU_MEMORY_INFO_CURRENT_AVAILABLE_VIDMEM_NVX);
        int ati_used_memory = GL11.glGetInteger(ATIMeminfo.GL_RENDERBUFFER_FREE_MEMORY_ATI);
        
        return Math.max(nvidia_used_memory, ati_used_memory);
    }

}
