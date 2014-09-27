///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package npc;
//
//import java.nio.FloatBuffer;
//import org.lwjgl.BufferUtils;
//import voxels.ChunkManager.Location;
//import voxels.Voxels;
//
///**
// *
// * @author otso
// */
//public class Monster {
//
//    private float x;
//    private float y;
//    private float z;
//
//    private float moveSpeed = 0.1f;
//
//    public Monster(float x, float y, float z) {
//        this.x = x;
//        this.y = y;
//        this.z = z;
//    }
//
//    public void moveTowardsPlayer() {
//        Location pLoc = Voxels.getPlayerLocation();
//        if (pLoc.x > x) {
//            x += moveSpeed;
//        } else {
//            x -= moveSpeed;
//        }
//        if (pLoc.z > z) {
//            z += moveSpeed;
//        } else {
//            z -= moveSpeed;
//        }
//
//    }
//
//    private static void createRender() {
//
//        int vertices = 4 * 6;
//        int vertexSize = 3;
//        int normalSize = 3;
//        int texSize = 2;
//
//        FloatBuffer vertexData;
//        FloatBuffer normalData;
//        FloatBuffer texData;
//
//        float[] vertexArray = new float[vertices * vertexSize];
//        float[] normalArray = new float[vertices * normalSize];
//        float[] texArray = new float[vertices * texSize];
//
//        int vArrayPos = 0;
//        int nArrayPos = 0;
//        int tArrayPos = 0;
//
//        vertexData = BufferUtils.createFloatBuffer(vertices * vertexSize);
//        normalData = BufferUtils.createFloatBuffer(vertices * vertexSize);
//        texData = BufferUtils.createFloatBuffer(vertices * texSize);
//        float frontXOff;
//        float frontYOff;
//        float backXOff;
//        float backYOff;
//        float rightXOff;
//        float rightYOff;
//        float leftXOff;
//        float leftYOff;
//        float topXOff;
//        float topYOff;
//        float bottomXOff;
//        float bottomYOff;
//        float tSize = 1f / 8f;
//
//        
//            if (type != Type.DIRT) {
//                topXOff = AtlasManager.getTopXOff(type);
//                topYOff = AtlasManager.getTopYOff(type);
//            } else {
//                topXOff = AtlasManager.getTopXOff(Type.GRASS);
//                topYOff = AtlasManager.getTopYOff(Type.GRASS);
//            }
//
//            bottomXOff = AtlasManager.getBottomXOff(type);
//            bottomYOff = AtlasManager.getBottomYOff(type);
//
//            if (v.side == Side.FRONT) {
//            // 1st
//                // upper left - +
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = 1;
//                nArrayPos++;
//
//                vertexArray[vArrayPos] = 0 + v.topLeftX + xOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 1 + v.topLeftY + yOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 1 + v.topLeftZ + zOff;
//                vArrayPos++;
//
//                texArray[tArrayPos] = frontXOff;
//                tArrayPos++;
//                texArray[tArrayPos] = frontYOff;
//                tArrayPos++;
//
//                // lower left - -
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = 1;
//                nArrayPos++;
//
//                vertexArray[vArrayPos] = 0 + v.bottomLeftX + xOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 0 + v.bottomLeftY + yOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 1 + v.bottomLeftZ + zOff;
//                vArrayPos++;
//
//                texArray[tArrayPos] = frontXOff;
//                tArrayPos++;
//                texArray[tArrayPos] = frontYOff + tSize;
//                tArrayPos++;
//
//                // lower right + -
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = 1;
//                nArrayPos++;
//
//                vertexArray[vArrayPos] = 1 + v.bottomRightX + xOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 0 + v.bottomRightY + yOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 1 + v.bottomRightZ + zOff;
//                vArrayPos++;
//
//                texArray[tArrayPos] = frontXOff + tSize;
//                tArrayPos++;
//                texArray[tArrayPos] = frontYOff + tSize;
//                tArrayPos++;
//
//                // upper right + +
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = 1;
//                nArrayPos++;
//
//                vertexArray[vArrayPos] = 1 + v.topRightX + xOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 1 + v.topRightY + yOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 1 + v.topRightZ + zOff;
//                vArrayPos++;
//
//                texArray[tArrayPos] = frontXOff + tSize;
//                tArrayPos++;
//                texArray[tArrayPos] = frontYOff;
//                tArrayPos++;
//
//            }
//            if (v.side == Side.BACK) {
//
//                texArray[tArrayPos] = backXOff;
//                tArrayPos++;
//                texArray[tArrayPos] = backYOff;
//                tArrayPos++;
//
//                texArray[tArrayPos] = backXOff + tSize;
//                tArrayPos++;
//                texArray[tArrayPos] = backYOff + tSize;
//                tArrayPos++;
//
//                texArray[tArrayPos] = backXOff;
//                tArrayPos++;
//                texArray[tArrayPos] = backYOff + tSize;
//                tArrayPos++;
//
//                //2nd
//                texArray[tArrayPos] = backXOff;
//                tArrayPos++;
//                texArray[tArrayPos] = backYOff;
//                tArrayPos++;
//
//            // 1st
//                // upper left + -
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = -1;
//                nArrayPos++;
//
//                vertexArray[vArrayPos] = 1 + v.topLeftX + xOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 1 + v.topLeftY + yOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 0 + v.topLeftZ + zOff;
//                vArrayPos++;
//                // lower left + -
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = -1;
//                nArrayPos++;
//
//                vertexArray[vArrayPos] = 1 + v.bottomLeftX + xOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 0 + v.bottomLeftY + yOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 0 + v.bottomLeftZ + zOff;
//                vArrayPos++;
//
//                // lower right - -
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = -1;
//                nArrayPos++;
//
//                vertexArray[vArrayPos] = 0 + v.bottomRightX + xOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 0 + v.bottomRightY + yOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 0 + v.bottomRightZ + zOff;
//                vArrayPos++;
//
//                // upper right - +
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = -1;
//                nArrayPos++;
//
//                vertexArray[vArrayPos] = 0 + v.topRightX + xOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 1 + v.topRightY + yOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 0 + v.topRightZ + zOff;
//                vArrayPos++;
//
//            }
//            if (v.side == Side.RIGHT) {
//                texArray[tArrayPos] = rightXOff;
//                tArrayPos++;
//                texArray[tArrayPos] = rightYOff;
//                tArrayPos++;
//
//                texArray[tArrayPos] = rightXOff;
//                tArrayPos++;
//                texArray[tArrayPos] = rightYOff + tSize;
//                tArrayPos++;
//
//                texArray[tArrayPos] = rightXOff + tSize;
//                tArrayPos++;
//                texArray[tArrayPos] = rightYOff + tSize;
//                tArrayPos++;
//
//                texArray[tArrayPos] = rightXOff + tSize;
//                tArrayPos++;
//                texArray[tArrayPos] = rightYOff;
//                tArrayPos++;
//
//            // 1st
//                // upper right + +
//                normalArray[nArrayPos] = 1;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//
//                vertexArray[vArrayPos] = 1 + v.topRightX + xOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 1 + v.topRightY + yOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 1 + v.bottomRightZ + zOff;
//                vArrayPos++;
//
//                // lower right - +
//                normalArray[nArrayPos] = 1;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//
//                vertexArray[vArrayPos] = 1 + v.bottomRightX + xOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 0 + v.bottomRightY + yOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 1 + v.bottomRightZ + zOff;
//                vArrayPos++;
//
//                // lower left - -
//                normalArray[nArrayPos] = 1;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//
//                vertexArray[vArrayPos] = 1 + v.bottomLeftX + xOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 0 + v.bottomLeftY + yOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 0 + v.bottomLeftZ + zOff;
//                vArrayPos++;
//
//                // upper left + -
//                normalArray[nArrayPos] = 1;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//
//                vertexArray[vArrayPos] = 1 + v.topLeftX + xOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 1 + v.topLeftY + yOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 0 + v.topLeftZ + zOff;
//                vArrayPos++;
//            }
//            if (v.side == Side.LEFT) {
//                texArray[tArrayPos] = leftXOff;
//                tArrayPos++;
//                texArray[tArrayPos] = leftYOff;
//                tArrayPos++;
//
//                texArray[tArrayPos] = leftXOff;
//                tArrayPos++;
//                texArray[tArrayPos] = leftYOff + tSize;
//                tArrayPos++;
//
//                texArray[tArrayPos] = leftXOff + tSize;
//                tArrayPos++;
//                texArray[tArrayPos] = leftYOff + tSize;
//                tArrayPos++;
//
//                texArray[tArrayPos] = leftXOff + tSize;
//                tArrayPos++;
//                texArray[tArrayPos] = leftYOff;
//                tArrayPos++;
//
//                // upper right + -
//                normalArray[nArrayPos] = -1;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//
//                vertexArray[vArrayPos] = 0 + v.topRightX + xOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 1 + v.topRightY + yOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 0 + v.topRightZ + zOff;
//                vArrayPos++;
//
//                // lower right - -
//                normalArray[nArrayPos] = -1;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//
//                vertexArray[vArrayPos] = 0 + v.bottomRightX + xOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 0 + v.bottomRightY + yOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 0 + v.bottomRightZ + zOff;
//                vArrayPos++;
//
//                // lower left - +
//                normalArray[nArrayPos] = -1;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//
//                vertexArray[vArrayPos] = 0 + v.bottomLeftX + xOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 0 + v.bottomLeftY + yOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 1 + v.bottomLeftZ + zOff;
//                vArrayPos++;
//
//                // upper left + +
//                normalArray[nArrayPos] = -1;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//
//                vertexArray[vArrayPos] = 0 + v.topLeftX + xOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 1 + v.topLeftY + yOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 1 + v.bottomLeftZ + zOff;
//                vArrayPos++;
//            }
//            if (v.side == Side.TOP) {
//            // 1st
//                // upper left
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = 1;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//
//                vertexArray[vArrayPos] = 0 + v.topLeftX + xOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 1 + v.topLeftY + yOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 0 + v.topLeftZ + zOff;
//                vArrayPos++;
//
//                texArray[tArrayPos] = topXOff;
//                tArrayPos++;
//                texArray[tArrayPos] = topYOff;
//                tArrayPos++;
//
//                // lower left
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = 1;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//
//                vertexArray[vArrayPos] = 0 + v.bottomLeftX + xOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 1 + v.bottomLeftY + yOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 1 + v.bottomLeftZ + zOff;
//                vArrayPos++;
//
//                texArray[tArrayPos] = topXOff;
//                tArrayPos++;
//                texArray[tArrayPos] = topYOff + tSize;
//                tArrayPos++;
//
//                // lower right
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = 1;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//
//                vertexArray[vArrayPos] = 1 + v.bottomRightX + xOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 1 + v.bottomRightY + yOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 1 + v.bottomRightZ + zOff;
//                vArrayPos++;
//
//                texArray[tArrayPos] = topXOff + tSize;
//                tArrayPos++;
//                texArray[tArrayPos] = topYOff + tSize;
//                tArrayPos++;
//
//                // upper right
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = 1;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//
//                vertexArray[vArrayPos] = 1 + v.topRightX + xOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 1 + v.topRightY + yOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 0 + v.topRightZ + zOff;
//                vArrayPos++;
//
//                texArray[tArrayPos] = topXOff + tSize;
//                tArrayPos++;
//                texArray[tArrayPos] = topYOff;
//                tArrayPos++;
//
//           
//                // bottom
//                texArray[tArrayPos] = bottomXOff;
//                tArrayPos++;
//                texArray[tArrayPos] = bottomYOff;
//                tArrayPos++;
//
//                texArray[tArrayPos] = bottomXOff + tSize;
//                tArrayPos++;
//                texArray[tArrayPos] = bottomYOff;
//                tArrayPos++;
//
//                texArray[tArrayPos] = bottomXOff + tSize;
//                tArrayPos++;
//                texArray[tArrayPos] = bottomYOff + tSize;
//                tArrayPos++;
//
//                texArray[tArrayPos] = bottomXOff;
//                tArrayPos++;
//                texArray[tArrayPos] = bottomYOff + tSize;
//                tArrayPos++;
//
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = -1;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//
//                vertexArray[vArrayPos] = 1 + v.topRightX + xOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 0 + v.topRightY + yOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 1 + v.topRightZ + zOff;
//                vArrayPos++;
//
//                // lower right - +
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = -1;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//
//                vertexArray[vArrayPos] = 0 + v.bottomRightX + xOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 0 + v.bottomRightY + yOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 1 + v.bottomRightZ + zOff;
//                vArrayPos++;
//
//                // lower left - -
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = -1;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//
//                vertexArray[vArrayPos] = 0 + v.bottomLeftX + xOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 0 + v.bottomLeftY + yOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 0 + v.bottomLeftZ + zOff;
//                vArrayPos++;
//
//            // 1st
//                // upper left + -
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//                normalArray[nArrayPos] = -1;
//                nArrayPos++;
//                normalArray[nArrayPos] = 0;
//                nArrayPos++;
//
//                vertexArray[vArrayPos] = 1 + v.topLeftX + xOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 0 + v.topLeftY + yOff;
//                vArrayPos++;
//                vertexArray[vArrayPos] = 0 + v.topLeftZ + zOff;
//                vArrayPos++;
//
//            }
//
//            vertexData.put(vertexArray);
//
//            vertexData.flip();
//
//            normalData.put(normalArray);
//
//            normalData.flip();
//
//            texData.put(texArray);
//
//            texData.flip();
//
//        }
//    }
