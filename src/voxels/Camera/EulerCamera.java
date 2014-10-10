/*
 * Copyright (c) 2013, Oskar Veerhoek
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */
package voxels.Camera;

import static java.lang.Math.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import static org.lwjgl.opengl.ARBDepthClamp.GL_DEPTH_CLAMP;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;
import voxels.ChunkManager.Chunk;
import voxels.ChunkManager.ChunkManager;
import voxels.ChunkManager.Type;
import voxels.Voxels;
import static voxels.Voxels.getChunkX;
import static voxels.Voxels.getChunkX;
import static voxels.Voxels.getChunkX;
import static voxels.Voxels.getChunkX;
import static voxels.Voxels.getChunkX;
import static voxels.Voxels.getChunkY;
import static voxels.Voxels.getChunkY;
import static voxels.Voxels.getChunkY;
import static voxels.Voxels.getChunkZ;
import static voxels.Voxels.getCurrentChunkXId;
import static voxels.Voxels.getCurrentChunkYId;
import static voxels.Voxels.getCurrentChunkYId;
import static voxels.Voxels.getCurrentChunkYId;
import static voxels.Voxels.getCurrentChunkZId;
import static voxels.Voxels.getX;
import static voxels.Voxels.getX;
import static voxels.Voxels.getY;
import static voxels.Voxels.getZ;
import static voxels.Voxels.getZ;
import static voxels.Voxels.getZ;
import static voxels.Voxels.impact;
import static voxels.Voxels.jumping;
import static voxels.Voxels.runOnStone;
import static voxels.Voxels.running;
import static voxels.Voxels.xInChunk;
import static voxels.Voxels.yInChunk;
import static voxels.Voxels.zInChunk;

/**
 * A camera set in 3D perspective. The camera uses Euler angles internally, so
 * beware of a gimbal lock.
 *
 * @author Oskar Veerhoek
 */
public class EulerCamera implements Camera {

    private static long runningPrevious = System.nanoTime();

    private float x = 0;
    private float y = 0;
    private float z = 0;
    private float pitch = 0;
    private float yaw = 0;
    private float roll = 0;
    private float fov = 90;
    private float aspectRatio = 1;
    private final float zNear;
    private final float zFar;
    private float fallingSpeedIncrease = 0.013f;
    private float fallingSpeed = 0;
    private float oldFallingSpeed = 0;

    private boolean flying = false;
    private boolean moving = false;
    private boolean zoomed = false;
    private long timeSinceJump = -1;
    private float minDistance = 0.1f;
    private byte prevBlock = -1;

    private ChunkManager chunkManager;

    private EulerCamera(Builder builder) {
        this.x = builder.x;
        this.y = builder.y;
        this.z = builder.z;
        this.pitch = builder.pitch;
        this.yaw = builder.yaw;
        this.roll = builder.roll;
        this.aspectRatio = builder.aspectRatio;
        this.zNear = builder.zNear;
        this.zFar = builder.zFar;
        this.fov = builder.fov;
    }

    /**
     * Creates a new camera with the given aspect ratio. It's located at [0 0 0]
     * with the orientation [0 0 0]. It has a zNear of 0.3, a zFar of 100.0, and
     * an fov of 90.
     */
    public EulerCamera() {
        this.zNear = 0.3f;
        this.zFar = 100;
    }

    /**
     * Creates a new camera with the given aspect ratio. It's located at [0 0 0]
     * with the orientation [0 0 0]. It has a zNear of 0.3, a zFar of 100.0, and
     * an fov of 90.
     *
     * @param aspectRatio the aspect ratio (width/height) of the camera
     *
     * @throws IllegalArgumentException if aspectRatio is 0 or smaller than 0
     */
    public EulerCamera(float aspectRatio) {
        if (aspectRatio <= 0) {
            throw new IllegalArgumentException("aspectRatio " + aspectRatio + " was 0 or was smaller than 0");
        }
        this.aspectRatio = aspectRatio;
        this.zNear = 0.3f;
        this.zFar = Chunk.CHUNK_SIZE * Voxels.chunkRenderDistance;
    }

    /**
     * Creates a new camera with the given aspect ratio and location.
     *
     * @param aspectRatio the aspect ratio (width/height) of the camera
     * @param x the first location coordinate
     * @param y the second location coordinate
     * @param z the third location coordinate
     *
     * @throws IllegalArgumentException if aspectRatio is 0 or smaller than 0
     */
    public EulerCamera(float aspectRatio, float x, float y, float z) {
        this(aspectRatio);
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Creates a new camera with the given aspect ratio, location, and
     * orientation.
     *
     * @param aspectRatio the aspect ratio (width/height) of the camera
     * @param x the first location coordinate
     * @param y the second location coordinate
     * @param z the third location coordinate
     * @param pitch the pitch (rotation on the x-axis)
     * @param yaw the yaw (rotation on the y-axis)
     * @param roll the roll (rotation on the z-axis)
     *
     * @throws IllegalArgumentException if aspectRatio is 0 or smaller than 0
     */
    public EulerCamera(float aspectRatio, float x, float y, float z, float pitch, float yaw, float roll) {
        this(aspectRatio, x, y, z);
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
    }

    /**
     * Creates a new camera with the given aspect ratio, location, zNear, zFar
     * and orientation.
     *
     * @param aspectRatio the aspect ratio (width/height) of the camera
     * @param x the first location coordinate
     * @param y the second location coordinate
     * @param z the third location coordinate
     * @param pitch the pitch (rotation on the x-axis)
     * @param yaw the yaw (rotation on the y-axis)
     * @param roll the roll (rotation on the z-axis)
     *
     * @throws IllegalArgumentException if aspectRatio is 0 or smaller than 0 or
     * if zNear is 0 or smaller than 0 or if zFar is the same or smaller than
     * zNear
     */
    public EulerCamera(float aspectRatio, float x, float y, float z, float pitch, float yaw, float roll, float zNear,
            float zFar) {
        if (aspectRatio <= 0) {
            throw new IllegalArgumentException("aspectRatio " + aspectRatio + " was 0 or was smaller than 0");
        }
        if (zNear <= 0) {
            throw new IllegalArgumentException("zNear " + zNear + " was 0 or was smaller than 0");
        }
        if (zFar <= zNear) {
            throw new IllegalArgumentException("zFar " + zFar + " was smaller or the same as zNear " + zNear);
        }
        this.aspectRatio = aspectRatio;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
        this.zNear = zNear;
        this.zFar = zFar;
    }

    /**
     * Processes mouse input and converts it in to camera movement.
     */
    public void processMouse() {
        final float MAX_LOOK_UP = 90;
        final float MAX_LOOK_DOWN = -90;

        float mouseDX = Mouse.getDX() * 0.16f;
        float mouseDY = Mouse.getDY() * 0.16f;

        if (zoomed) {
            mouseDX *= 0.25f;
            mouseDY *= 0.25f;
        }

        if (yaw + mouseDX >= 360) {
            yaw = yaw + mouseDX - 360;
        } else if (yaw + mouseDX < 0) {
            yaw = 360 - yaw + mouseDX;
        } else {
            yaw += mouseDX;
        }
        if (pitch - mouseDY >= MAX_LOOK_DOWN && pitch - mouseDY <= MAX_LOOK_UP) {
            pitch += -mouseDY;
        } else if (pitch - mouseDY < MAX_LOOK_DOWN) {
            pitch = MAX_LOOK_DOWN;
        } else if (pitch - mouseDY > MAX_LOOK_UP) {
            pitch = MAX_LOOK_UP;
        }
    }

    /**
     * Processes mouse input and converts it in to camera movement.
     *
     * @param mouseSpeed the speed (sensitivity) of the mouse, 1.0 should
     * suffice
     */
    public void processMouse(float mouseSpeed) {
        final float MAX_LOOK_UP = 90;
        final float MAX_LOOK_DOWN = -90;
        float mouseDX = Mouse.getDX() * mouseSpeed * 0.16f;
        float mouseDY = Mouse.getDY() * mouseSpeed * 0.16f;
        if (yaw + mouseDX >= 360) {
            yaw = yaw + mouseDX - 360;
        } else if (yaw + mouseDX < 0) {
            yaw = 360 - yaw + mouseDX;
        } else {
            yaw += mouseDX;
        }
        if (pitch - mouseDY >= MAX_LOOK_DOWN && pitch - mouseDY <= MAX_LOOK_UP) {
            pitch += -mouseDY;
        } else if (pitch - mouseDY < MAX_LOOK_DOWN) {
            pitch = MAX_LOOK_DOWN;
        } else if (pitch - mouseDY > MAX_LOOK_UP) {
            pitch = MAX_LOOK_UP;
        }
    }

    /**
     * Processes mouse input and converts it into camera movement.
     *
     * @param mouseSpeed the speed (sensitivity) of the mouse, 1.0 should
     * suffice
     * @param maxLookUp the maximum angle in degrees at which you can look up
     * @param maxLookDown the maximum angle in degrees at which you can look
     * down
     */
    public void processMouse(float mouseSpeed, float maxLookUp, float maxLookDown) {

        float mouseDX = Mouse.getDX() * mouseSpeed * 0.16f;
        float mouseDY = Mouse.getDY() * mouseSpeed * 0.16f;
        if (yaw + mouseDX >= 360) {
            yaw = yaw + mouseDX - 360;
        } else if (yaw + mouseDX < 0) {
            yaw = 360 - yaw + mouseDX;
        } else {
            yaw += mouseDX;
        }
        if (pitch - mouseDY >= maxLookDown && pitch - mouseDY <= maxLookUp) {
            pitch += -mouseDY;
        } else if (pitch - mouseDY < maxLookDown) {
            pitch = maxLookDown;
        } else if (pitch - mouseDY > maxLookUp) {
            pitch = maxLookUp;
        }
    }

    /**
     * Processes keyboard input and converts into camera movement.
     *
     * @param delta the elapsed time since the last frame update in milliseconds
     * @param speed the speed of the movement (normal = 1.0)
     *
     * @throws IllegalArgumentException if delta is 0 or delta is smaller than 0
     */
    public void processKeyboard(float delta, float speed) {
        if (delta <= 0) {
            throw new IllegalArgumentException("delta " + delta + " is 0 or is smaller than 0");
        }
        boolean keyUp = Keyboard.isKeyDown(Keyboard.KEY_UP) || Keyboard.isKeyDown(Keyboard.KEY_W);
        boolean keyDown = Keyboard.isKeyDown(Keyboard.KEY_DOWN) || Keyboard.isKeyDown(Keyboard.KEY_S);
        boolean keyLeft = Keyboard.isKeyDown(Keyboard.KEY_LEFT) || Keyboard.isKeyDown(Keyboard.KEY_A);
        boolean keyRight = Keyboard.isKeyDown(Keyboard.KEY_RIGHT) || Keyboard.isKeyDown(Keyboard.KEY_D);
        boolean flyUp = Keyboard.isKeyDown(Keyboard.KEY_SPACE);
        boolean flyDown = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);
        boolean moveFaster = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
        boolean moveSlower = Keyboard.isKeyDown(Keyboard.KEY_C);
        zoomed = Keyboard.isKeyDown(Keyboard.KEY_Z);

        if (moveFaster) {
            speed *= 9;
        }
        if (moveSlower) {
            speed /= 3;
        }

//        if (inWater()) {
//            speed /= 3;
//
//            float lightAmbient[] = {0, 0, 10f, 1.0f};
//            float lightDiffuse[] = {0.1f, 0.1f, 1f, 1.0f};
//
//            glLightModel(GL_LIGHT_MODEL_AMBIENT, Voxels.asFloatBuffer(lightAmbient));
//            glLight(GL_LIGHT1, GL_DIFFUSE, Voxels.asFloatBuffer(lightDiffuse));
//
//            glDisable(GL_CULL_FACE);
//        } else {
//            float lightAmbient[] = {0.3f, 0.3f, 0.3f, 1.0f};
//            float lightDiffuse[] = {1f, 1f, 1f, 1.0f};
//
//            glLightModel(GL_LIGHT_MODEL_AMBIENT, Voxels.asFloatBuffer(lightAmbient));
//            glLight(GL_LIGHT1, GL_DIFFUSE, Voxels.asFloatBuffer(lightDiffuse));
//
//            glEnable(GL_CULL_FACE);
//            glCullFace(GL_BACK);
//
//        }
        if (keyUp && keyRight && !keyLeft && !keyDown) {
            moveFromLook(speed * delta * 0.003f, 0, -speed * delta * 0.003f);
        }
        if (keyUp && keyLeft && !keyRight && !keyDown) {
            moveFromLook(-speed * delta * 0.003f, 0, -speed * delta * 0.003f);
        }
        if (keyUp && !keyLeft && !keyRight && !keyDown) {
            moveFromLook(0, 0, -speed * delta * 0.003f);
        }
        if (keyDown && keyLeft && !keyRight && !keyUp) {
            moveFromLook(-speed * delta * 0.003f, 0, speed * delta * 0.003f);
        }
        if (keyDown && keyRight && !keyLeft && !keyUp) {
            moveFromLook(speed * delta * 0.003f, 0, speed * delta * 0.003f);
        }
        if (keyDown && !keyUp && !keyLeft && !keyRight) {
            moveFromLook(0, 0, speed * delta * 0.003f);
        }
        if (keyLeft && !keyRight && !keyUp && !keyDown) {
            moveFromLook(-speed * delta * 0.003f, 0, 0);
        }
        if (keyRight && !keyLeft && !keyUp && !keyDown) {
            moveFromLook(speed * delta * 0.003f, 0, 0);
        }

        if (flyUp && !flyDown) {
            timeSinceJump = System.currentTimeMillis() - timeSinceJump;
            if (flying) {
                y += speed * delta * 0.003f;
            } else if (fallingSpeed == 0 && (timeSinceJump > 100 || timeSinceJump < 0)) {
                timeSinceJump = System.currentTimeMillis();
                fallingSpeed = -16 * 0.0125f;
                if (moveFaster) {
                    fallingSpeed *= 2.5f;
                }
                //running.stop();
                //jumping.stop();
                //jumping.play();
            }
        }

        if (flyDown && !flyUp) {
            if (flying) {
                y -= speed * delta * 0.003f;
            }
        }

        if (flying == false) {
//            if (oldFallingSpeed > fallingSpeedIncrease * 2 && fallingSpeed == 0) {
//                impact.play();
//            }

            float hAdj;
            if (fallingSpeed < 0) // going up
            {
                hAdj = 0.2f;
            } else {
                hAdj = Voxels.PLAYER_HEIGHT;
            }

            byte block = chunkManager.getActiveBlock(new Vector3f(x, y - fallingSpeed - fallingSpeedIncrease - hAdj, z));
            if (block == Type.AIR || block == -1) {
                fallingSpeed += fallingSpeedIncrease;
                y -= fallingSpeed;

            } else {
                y = (int) y;
                oldFallingSpeed = fallingSpeed;
                fallingSpeed = 0;
            }
            byte underFeet = chunkManager.getActiveBlock(new Vector3f(x, y - Voxels.PLAYER_HEIGHT, z));
            if(underFeet != Type.DIRT){
                running.stop();
            }
            if(underFeet != Type.STONE){
                runOnStone.stop();
            }
            if(prevBlock != underFeet){
                runningPrevious = 0;
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_S) || (Keyboard.isKeyDown(Keyboard.KEY_D))) {
                if (!flying && fallingSpeed == 0) {
                    if (underFeet == Type.DIRT) {
                        if (System.currentTimeMillis() - runningPrevious > 4273) {
                            runningPrevious = System.currentTimeMillis();
                            running.play();
                        }
                    }
                    else if (underFeet == Type.STONE) {
                        if (System.currentTimeMillis() - runningPrevious > 1874) {
                            runningPrevious = System.currentTimeMillis();
                            runOnStone.play();
                        }
                    }
                } else {
                    runningPrevious = 0;
                    running.stop();
                    runOnStone.stop();
                }

            }
            if (!keyUp && !keyDown && !keyLeft && !keyRight) {
                running.stop();
                runOnStone.stop();
                runningPrevious = 0;
            }
            prevBlock = underFeet;
        }
    }

    /**
     * Move in the direction you're looking. That is, this method assumes a new
     * coordinate system where the axis you're looking down is the z-axis, the
     * axis to your left is the x-axis, and the upward axis is the y-axis.
     *
     * @param dx the movement along the x-axis
     * @param dy the movement along the y-axis
     * @param dz the movement along the z-axis
     */
    public void moveFromLook(float dx, float dy, float dz) {
        if (!flying) {

            byte upperBlock = chunkManager.getActiveBlock(new Vector3f(x - (float) (dx * (float) sin(toRadians(yaw - 90)) + dz * sin(toRadians(yaw))), y - 1, z));
            byte lowerBlock = chunkManager.getActiveBlock(new Vector3f(x - (float) (dx * (float) sin(toRadians(yaw - 90)) + dz * sin(toRadians(yaw))), y - 2, z));

            if ((upperBlock == Type.AIR || upperBlock == -1) && (lowerBlock == Type.AIR || lowerBlock == -1)) {
                this.x -= dx * (float) sin(toRadians(yaw - 90)) + dz * sin(toRadians(yaw));
            }

            upperBlock = chunkManager.getActiveBlock(new Vector3f(x, y - 1, z + (float) (dx * (float) cos(toRadians(yaw - 90)) + dz * cos(toRadians(yaw)))));
            lowerBlock = chunkManager.getActiveBlock(new Vector3f(x, y - 2, z + (float) (dx * (float) cos(toRadians(yaw - 90)) + dz * cos(toRadians(yaw)))));

            if ((upperBlock == Type.AIR || upperBlock == -1) && (lowerBlock == Type.AIR || lowerBlock == -1)) {
                this.z += dx * (float) cos(toRadians(yaw - 90)) + dz * cos(toRadians(yaw));
            }

//            int potentChunkXId = Voxels.getCurrentChunkXId((float) (dx * (float) sin(toRadians(yaw - 90)) + dz * sin(toRadians(yaw))));
//            int potentXInChunk = Voxels.xInChunk((float) (dx * (float) sin(toRadians(yaw - 90)) + dz * sin(toRadians(yaw))));
//            Chunk potentChunk = chunkManager.getActiveChunk(potentChunkXId, getCurrentChunkYId(), Voxels.getCurrentChunkZId());
//            Chunk potentChunk2;
//            if (getCurrentChunkYId() != getCurrentChunkYId(1)) {
//                potentChunk2 = chunkManager.getActiveChunk(potentChunkXId, getCurrentChunkYId(1), Voxels.getCurrentChunkZId());
//            } else {
//                potentChunk2 = potentChunk;
//            }
//
//            // allowed to move in X axis
//            if (potentChunk != null && potentChunk2 != null) {
//                if (potentChunk.blocks[potentXInChunk][Voxels.yInChunk()][Voxels.zInChunk()] == Type.AIR && potentChunk2.blocks[potentXInChunk][Voxels.yInChunk(1)][Voxels.zInChunk()] == Type.AIR) {
//                    this.x -= dx * (float) sin(toRadians(yaw - 90)) + dz * sin(toRadians(yaw));
//                }
//            }
//
//            int potentZInChunk = Voxels.zInChunk((float) (dx * (float) cos(toRadians(yaw - 90)) + dz * cos(toRadians(yaw))));
//            int potentChunkZId = Voxels.getCurrentChunkZId((float) (dx * (float) cos(toRadians(yaw - 90)) + dz * cos(toRadians(yaw))));
//            potentChunk = chunkManager.getActiveChunk(Voxels.getCurrentChunkXId(), Voxels.getCurrentChunkYId(), potentChunkZId);
//
//            if (getCurrentChunkYId() != getCurrentChunkYId(1)) {
//                potentChunk2 = chunkManager.getActiveChunk(potentChunkXId, getCurrentChunkYId(1), Voxels.getCurrentChunkZId());
//            } else {
//                potentChunk2 = potentChunk;
//            }
//
//            // allowed to move in Z axis
//            if (potentChunk != null && potentChunk2 != null) {
//                if (potentChunk.blocks[xInChunk()][Voxels.yInChunk()][potentZInChunk] == Type.AIR && potentChunk2.blocks[xInChunk()][Voxels.yInChunk(1)][potentZInChunk] == Type.AIR) {
//                    this.z += dx * (float) cos(toRadians(yaw - 90)) + dz * cos(toRadians(yaw));
//                }
//            }
//
        } else {
            this.x -= dx * (float) sin(toRadians(yaw - 90)) + dz * sin(toRadians(yaw));
            this.y += dy * (float) sin(toRadians(pitch - 90)) + dz * sin(toRadians(pitch));
            this.z += dx * (float) cos(toRadians(yaw - 90)) + dz * cos(toRadians(yaw));
        }

    }

    /**
     * Sets the position of the camera.
     *
     * @param x the x-coordinate of the camera
     * @param y the y-coordinate of the camera
     * @param z the z-coordinate of the camera
     */
    public void setPosition(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Sets GL_PROJECTION to an orthographic projection matrix. The matrix mode
     * will be returned it its previous value after execution.
     */
    public void applyOrthographicMatrix() {
        glPushAttrib(GL_TRANSFORM_BIT);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(-aspectRatio, aspectRatio, -1, 1, 0, zFar);
        glPopAttrib();
    }

    /**
     * Enables or disables OpenGL states that will enhance the camera
     * appearance. Enable GL_DEPTH_CLAMP if ARB_depth_clamp is supported
     */
    public void applyOptimalStates() {
        if (GLContext.getCapabilities().GL_ARB_depth_clamp) {
            glEnable(GL_DEPTH_CLAMP);
        }
    }

    /**
     * Sets GL_PROJECTION to an perspective projection matrix. The matrix mode
     * will be returned it its previous value after execution.
     */
    public void applyPerspectiveMatrix() {
        glPushAttrib(GL_TRANSFORM_BIT);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        GLU.gluPerspective(fov, aspectRatio, zNear, zFar);
        glPopAttrib();
    }

    public void applyPerspectiveMatrix(int fov) {
        glPushAttrib(GL_TRANSFORM_BIT);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        GLU.gluPerspective(fov, aspectRatio, zNear, zFar);
        glPopAttrib();
    }

    /**
     * Applies the camera translations and rotations to GL_MODELVIEW.
     */
    public void applyTranslations() {
        glPushAttrib(GL_TRANSFORM_BIT);
        glMatrixMode(GL_MODELVIEW);
        glRotatef(pitch, 1, 0, 0);
        glRotatef(yaw, 0, 1, 0);
        glRotatef(roll, 0, 0, 1);
        glTranslatef(-x, -y + 0.25f, -z);
        glPopAttrib();
    }

    /**
     * Sets the rotation of the camera.
     *
     * @param pitch the rotation around the x-axis in degrees
     * @param yaw the rotation around the y-axis in degrees
     * @param roll the rotation around the z-axis in degrees
     */
    public void setRotation(float pitch, float yaw, float roll) {
        this.pitch = pitch;
        this.yaw = yaw;
        this.roll = roll;
    }

    /**
     * @return the x-coordinate of the camera
     */
    public float x() {
        return x;
    }

    /**
     * @return y the y-coordinate of the camera
     */
    public float y() {
        return y;
    }

    /**
     * @return the z-coordinate of the camera
     */
    public float z() {
        return z;
    }

    /**
     * @return the pitch of the camera in degrees
     */
    public float pitch() {
        return pitch;
    }

    /**
     * @return the yaw of the camera in degrees
     */
    public float yaw() {
        return yaw;
    }

    /**
     * @return the roll of the camera in degrees
     */
    public float roll() {
        return roll;
    }

    /**
     * @return the fov of the camera in degrees in the y direction
     */
    public float fieldOfView() {
        return fov;
    }

    /**
     * Sets the field of view angle in degrees in the y direction. Note that
     * this.applyPerspectiveMatrix() must be applied in order to see any
     * difference.
     *
     * @param fov the field of view angle in degrees in the y direction
     */
    public void setFieldOfView(float fov) {
        this.fov = fov;
    }

    @Override
    public void setAspectRatio(float aspectRatio) {
        if (aspectRatio <= 0) {
            throw new IllegalArgumentException("aspectRatio " + aspectRatio + " is 0 or less");
        }
        this.aspectRatio = aspectRatio;
    }

    /**
     * @return the aspect ratio of the camera
     */
    public float aspectRatio() {
        return aspectRatio;
    }

    /**
     * @return the distance from the camera to the near clipping pane
     */
    public float nearClippingPane() {
        return zNear;
    }

    /**
     * @return the distance from the camera to the far clipping pane
     */
    public float farClippingPane() {
        return zFar;
    }

    @Override
    public String toString() {
        return "EulerCamera [x=" + x + ", y=" + y + ", z=" + z + ", pitch=" + pitch + ", yaw=" + yaw + ", "
                + "roll=" + roll + ", fov=" + fov + ", aspectRatio=" + aspectRatio + ", zNear=" + zNear + ", "
                + "zFar=" + zFar + "]";
    }

    @Override
    public void processKeyboard(float delta) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void processKeyboard(float delta, float speedX, float speedY, float speedZ) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * A builder helper class for the EulerCamera class.
     */
    public static class Builder {

        private float aspectRatio = 1;
        private float x = 0, y = 0, z = 0, pitch = 0, yaw = 0, roll = 0;
        private float zNear = 0.3f;
        private float zFar = 1000;
        private float fov = 90;

        public Builder() {

        }

        /**
         * Sets the aspect ratio of the camera.
         *
         * @param aspectRatio the aspect ratio of the camera (window width /
         * window height)
         *
         * @return this
         */
        public Builder setAspectRatio(float aspectRatio) {
            if (aspectRatio <= 0) {
                throw new IllegalArgumentException("aspectRatio " + aspectRatio + " was 0 or was smaller than 0");
            }
            this.aspectRatio = aspectRatio;
            return this;
        }

        /**
         * Sets the distance from the camera to the near clipping pane.
         *
         * @param nearClippingPane the distance from the camera to the near
         * clipping pane
         *
         * @return this
         *
         * @throws IllegalArgumentException if nearClippingPane is 0 or less
         */
        public Builder setNearClippingPane(float nearClippingPane) {
            if (nearClippingPane <= 0) {
                throw new IllegalArgumentException("nearClippingPane " + nearClippingPane + " is 0 or less");
            }
            this.zNear = nearClippingPane;
            return this;
        }

        /**
         * Sets the distance from the camera to the far clipping pane.
         *
         * @param farClippingPane the distance from the camera to the far
         * clipping pane
         *
         * @return this
         *
         * @throws IllegalArgumentException if farClippingPane is 0 or less
         */
        public Builder setFarClippingPane(float farClippingPane) {
            if (farClippingPane <= 0) {
                throw new IllegalArgumentException("farClippingPane " + farClippingPane + " is 0 or less");
            }
            this.zFar = farClippingPane;
            return this;
        }

        /**
         * Sets the field of view angle in degrees in the y direction.
         *
         * @param fov the field of view angle in degrees in the y direction
         *
         * @return this
         */
        public Builder setFieldOfView(float fov) {
            this.fov = fov;
            return this;
        }

        /**
         * Sets the position of the camera.
         *
         * @param x the x-coordinate of the camera
         * @param y the y-coordinate of the camera
         * @param z the z-coordinate of the camera
         *
         * @return this
         */
        public Builder setPosition(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        /**
         * Sets the rotation of the camera.
         *
         * @param pitch the rotation around the x-axis in degrees
         * @param yaw the rotation around the y-axis in degrees
         * @param roll the rotation around the z-axis in degrees
         */
        public Builder setRotation(float pitch, float yaw, float roll) {
            this.pitch = pitch;
            this.yaw = yaw;
            this.roll = roll;
            return this;
        }

        /**
         * Constructs an instance of EulerCamera from this builder helper class.
         *
         * @return an instance of EulerCamera
         *
         * @throws IllegalArgumentException if farClippingPane is the same or
         * less than nearClippingPane
         */
        public EulerCamera build() {
            if (zFar <= zNear) {
                throw new IllegalArgumentException("farClippingPane " + zFar + " is the same or less than "
                        + "nearClippingPane " + zNear);
            }
            return new EulerCamera(this);
        }
    }

    public boolean inWater() {
        return this.y + Voxels.WaterOffs < Chunk.WATER_HEIGHT;
    }

    public boolean getFlying() {
        return flying;
    }

    public void setChunkManager(ChunkManager chunkManager) {
        this.chunkManager = chunkManager;
    }

    public void toggleFlight() {
        flying = !flying;
        if (flying) {
            fallingSpeed = 0;
        }
    }

    public boolean isZoomed() {
        return zoomed;
    }

}
