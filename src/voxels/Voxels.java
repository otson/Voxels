/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;

import voxels.Camera.EulerCamera;
import voxels.Noise.FastNoise;

/**
 *
 * @author otso
 */
public class Voxels {

    private static EulerCamera camera;
    private static int displayListHandle;
    private static int vertexCount = 0;
    private static float light0Position[] = {-200.0f, 5000.0f, -800.0f, 1.0f};

    public static void main(String[] args) {
        initDisplay();
        initOpenGL();
        initLighting();
        gameLoop();
    }

    private static void initDisplay() {
        try {
            Display.setDisplayMode(new DisplayMode(1650, 1050));
            Display.setVSyncEnabled(true);
            Display.setTitle("Voxels");
            Display.create();
        } catch (LWJGLException e) {
            e.printStackTrace();
            Display.destroy();
            System.exit(1);
        }
    }

    private static void gameLoop() {
        long startTime;
        long endTime;
        long totalTime = 0;
        int fps = 0;
        boolean generateChunks = true;
        boolean running = true;
        camera = InitCamera();
        HashMap<Integer, Chunk> map = new HashMap<>();

        map.put(new Pair(getCamChunkX(), getCamChunkZ()).hashCode(), new Chunk(0,0));
        //System.out.println("x: " + (int) camera.x() / 4 + " z: " + (int) camera.z() / 4);
        displayListHandle = glGenLists(1);
        glNewList(displayListHandle, GL_COMPILE);
        drawChunk(map.get(new Pair(getCamChunkX(), getCamChunkZ()).hashCode()), 0, 0);
        glEndList();
        System.out.println(displayListHandle);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective((float) 90, 1, 0.3f, 5000);
        glMatrixMode(GL_MODELVIEW);
        glEnable(GL_DEPTH_TEST);
        while (!Display.isCloseRequested() && running) {
            startTime = System.nanoTime();
            glViewport(0, 0, Display.getWidth(), Display.getHeight());
            camera.setAspectRatio((float) Display.getWidth() / Display.getHeight());

            if (Display.wasResized()) {
                camera.applyPerspectiveMatrix();
            }

            while (Keyboard.next()) {

                if (Keyboard.isKeyDown(Keyboard.KEY_1)) {
                    glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_2)) {
                    glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_G)) {
                    generateChunks = !generateChunks;
                }
                if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                    running = false;
                }
            }

            glLoadIdentity();
            if (generateChunks)
                checkChunkUpdates(map);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            camera.applyTranslations();

            if (Mouse.isGrabbed()) {
                camera.processMouse();
                camera.processKeyboard(16, 5);
            }
            processKeyboard();
            glLight(GL_LIGHT0, GL_POSITION, asFloatBuffer(light0Position));
            glTranslatef(0, -getNoise(0, 0)-5, 0);
            for (int i = 1; i <= displayListHandle; i++) {
                glCallList(i);
            }
            Display.update();
            Display.sync(60);
            fps++;
            endTime = System.nanoTime();
            totalTime += endTime - startTime;
            if (totalTime > 1000000000) {
                Display.setTitle("FPS: " + fps);
                totalTime = 0;
                fps = 0;
            }
        }
        Display.destroy();
        System.exit(0);
    }

    private static void drawChunk(Chunk chunk, int xOff, int zOff) {
        int yOff;
        int drawnBlocks = 0;
        for (int x = 0; x < chunk.blocks.length; x++) {
            for (int z = 0; z < chunk.blocks[x][0].length; z++) {
                for (int y = 0; y < chunk.blocks[x].length; y++) {
                    if(chunk.blocks[x][y][z].isActive()){
                        drawFullCube(chunk, x + getCamChunkX() * chunk.blocks.length + xOff, y, z + getCamChunkZ() * chunk.blocks.length + zOff, 1);
                    //drawCube(chunk, x, y, z, getCamChunkX() * chunk.blocks.length + xOff, 0, getCamChunkZ() * chunk.blocks.length + zOff, 1);
                    drawnBlocks++;
                    }
                    
                }
            }
        }
        System.out.println("Drawn blocks: "+drawnBlocks);
    }

    private static EulerCamera InitCamera() {
        camera = new EulerCamera.Builder().setAspectRatio((float) Display.getWidth() / (float) Display.getHeight()).setFieldOfView(60).build();
        camera.applyPerspectiveMatrix();
        camera.applyOptimalStates();
        Mouse.setGrabbed(true);
        return camera;
    }

    private static void initOpenGL() {
        glMatrixMode(GL_PROJECTION);
        glMatrixMode(GL_MODELVIEW);
        glEnable(GL_DEPTH_TEST);
    }

    public static void drawCube(Chunk chunk, float x, float y, float z, float xOff, float yOff, float zOff, float size) {
        int zMax = Chunk.CHUNK_WIDTH - 1;
        int xMax = Chunk.CHUNK_WIDTH - 1;
        int yMax = Chunk.CHUNK_HEIGHT - 1;
        yOff += getNoise(x, z);
        boolean render = false;
        glBegin(GL_QUADS);
        // front face
        if (z == zMax)
            render = true;
        if (render || !chunk.blocks[(int) x][(int) y][(int) z + 1].isActive()) {
            glNormal3f(0f, 0f, 1f);
            glColor3f(100f / 255f, 60f / 255f, 60f / 255f);
            glVertex3f(size / 2 + x + xOff, size / 2 + y + yOff, size / 2 + z + zOff);
            glVertex3f(-size / 2 + x + xOff, size / 2 + y + yOff, size / 2 + z + zOff);
            glVertex3f(-size / 2 + x + xOff, -size / 2 + y + yOff, size / 2 + z + zOff);
            glVertex3f(size / 2 + x + xOff, -size / 2 + y + yOff, size / 2 + z + zOff);
            vertexCount += 4;
        }
        // left face
        render = false;
        if (x == 0)
            render = true;
        if (render || !chunk.blocks[(int) x - 1][(int) y][(int) z].isActive()) {
            glNormal3f(-1f, 0f, 0f);
            glColor3f(100f / 255f, 60f / 255f, 60f / 255f);
            glVertex3f(-size / 2 + x + xOff, size / 2 + y + yOff, size / 2 + z + zOff);
            glVertex3f(-size / 2 + x + xOff, -size / 2 + y + yOff, size / 2 + z + zOff);
            glVertex3f(-size / 2 + x + xOff, -size / 2 + y + yOff, -size / 2 + z + zOff);
            glVertex3f(-size / 2 + x + xOff, size / 2 + y + yOff, -size / 2 + z + zOff);
            vertexCount += 4;
        }
        // back face
        render = false;
        if (z == 0)
            render = true;
        if (render || !chunk.blocks[(int) x][(int) y][(int) z - 1].isActive()) {
            glNormal3f(0f, 0f, -1f);
            glColor3f(100f / 255f, 60f / 255f, 60f / 255f);
            glVertex3f(size / 2 + x + xOff, size / 2 + y + yOff, -size / 2 + z + zOff);
            glVertex3f(-size / 2 + x + xOff, size / 2 + y + yOff, -size / 2 + z + zOff);
            glVertex3f(-size / 2 + x + xOff, -size / 2 + y + yOff, -size / 2 + z + zOff);
            glVertex3f(size / 2 + x + xOff, -size / 2 + y + yOff, -size / 2 + z + zOff);
            vertexCount += 4;
        }
        // right face
        render = false;
        if (x == xMax)
            render = true;
        if (render || !chunk.blocks[(int) x + 1][(int) y][(int) z].isActive()) {
            glNormal3f(1f, 0f, 0f);
            glColor3f(100f / 255f, 60f / 255f, 60f / 255f);
            glVertex3f(size / 2 + x + xOff, size / 2 + y + yOff, size / 2 + z + zOff);
            glVertex3f(size / 2 + x + xOff, -size / 2 + y + yOff, size / 2 + z + zOff);
            glVertex3f(size / 2 + x + xOff, -size / 2 + y + yOff, -size / 2 + z + zOff);
            glVertex3f(size / 2 + x + xOff, size / 2 + y + yOff, -size / 2 + z + zOff);
            vertexCount += 4;
        }
        // top face
        render = false;
        if (y == yMax)
            render = true;
        if (render || !chunk.blocks[(int) x][(int) y + 1][(int) z].isActive()) {
            glNormal3f(0f, 1f, 0f);
            glColor3f(0f, 127f / 255f, 14f / 255f);
            glVertex3f(size / 2 + x + xOff, size / 2 + y + yOff, size / 2 + z + zOff);
            glVertex3f(-size / 2 + x + xOff, size / 2 + y + yOff, size / 2 + z + zOff);
            glVertex3f(-size / 2 + x + xOff, size / 2 + y + yOff, -size / 2 + z + zOff);
            glVertex3f(size / 2 + x + xOff, size / 2 + y + yOff, -size / 2 + z + zOff);
            vertexCount += 4;
        }
        // bottom face
        render = false;
        if (y == 0)
            render = true;
        if (render || !chunk.blocks[(int) x][(int) y - 1][(int) z].isActive()) {
            glNormal3f(0f, -1f, 0f);
            glColor3f(64f / 255f, 64f / 255f, 64f / 255f);
            glVertex3f(size / 2 + x + xOff, -size / 2 + y + yOff, size / 2 + z + zOff);
            glVertex3f(-size / 2 + x + xOff, -size / 2 + y + yOff, size / 2 + z + zOff);
            glVertex3f(-size / 2 + x + xOff, -size / 2 + y + yOff, -size / 2 + z + zOff);
            glVertex3f(size / 2 + x + xOff, -size / 2 + y + yOff, -size / 2 + z + zOff);
            vertexCount += 4;
        }

        glEnd();
    }

    public static void drawFullCube(Chunk chunk, float x, float y, float z, float size) {
        glBegin(GL_QUADS);
        // front face

        glNormal3f(0f, 0f, 1f);
        glColor3f(100f / 255f, 60f / 255f, 60f / 255f);
        glVertex3f( (size / 2 + x),  (size / 2 + y),  (size / 2 + z));
        glVertex3f( (-size / 2 + x),  (size / 2 + y), (size / 2 + z));
        glVertex3f( (-size / 2 + x),  (-size / 2 + y),  (size / 2 + z));
        glVertex3f( (size / 2 + x),  (-size / 2 + y),  (size / 2 + z));
        // left face
        glNormal3f(-1f, 0f, 0f);
        glColor3f(100f / 255f, 60f / 255f, 60f / 255f);
        glVertex3f( (-size / 2 + x),  (size / 2 + y), (size / 2 + z));
        glVertex3f( (-size / 2 + x),  (-size / 2 + y),  (size / 2 + z));
        glVertex3f( (-size / 2 + x),  (-size / 2 + y),  (-size / 2 + z));
        glVertex3f( (-size / 2 + x),  (size / 2 + y),  (-size / 2 + z));

        // back face
        glNormal3f(0f, 0f, -1f);
        glColor3f(100f / 255f, 60f / 255f, 60f / 255f);
        glVertex3f( (size / 2 + x),  (size / 2 + y),  (-size / 2 + z));
        glVertex3f((-size / 2 + x),  (size / 2 + y), (-size / 2 + z));
        glVertex3f( (-size / 2 + x),  (-size / 2 + y),  (-size / 2 + z));
        glVertex3f( (size / 2 + x), (-size / 2 + y),  (-size / 2 + z));

        // right face
        glNormal3f(1f, 0f, 0f);
        glColor3f(100f / 255f, 60f / 255f, 60f / 255f);
        glVertex3f( (size / 2 + x), (size / 2 + y),  (size / 2 + z));
        glVertex3f( (size / 2 + x),  (-size / 2 + y),  (size / 2 + z));
        glVertex3f( (size / 2 + x),  (-size / 2 + y),  (-size / 2 + z));
        glVertex3f( (size / 2 + x),  (size / 2 + y),  (-size / 2 + z));

        // top face
        glNormal3f(0f, 1f, 0f);
        glColor3f(0f, 127f / 255f, 14f / 255f);
        glVertex3f( (size / 2 + x),  (size / 2 + y),  (size / 2 + z));
        glVertex3f( (-size / 2 + x),  (size / 2 + y),  (size / 2 + z));
        glVertex3f( (-size / 2 + x),  (size / 2 + y),  (-size / 2 + z));
        glVertex3f( (size / 2 + x),  (size / 2 + y),  (-size / 2 + z));

        // bottom face
        glNormal3f(0f, -1f, 0f);
        glColor3f(64f / 255f, 64f / 255f, 64f / 255f);
        glVertex3f( (size / 2 + x),  (-size / 2 + y),  (size / 2 + z));
        glVertex3f( (-size / 2 + x),  (-size / 2 + y),  (size / 2 + z));
        glVertex3f( (-size / 2 + x),  (-size / 2 + y),  (-size / 2 + z));
        glVertex3f( (size / 2 + x),  (-size / 2 + y),  (-size / 2 + z));

        glEnd();
        vertexCount += 4;
    }

    private static void processKeyboard() {
        boolean wireFrame = Keyboard.isKeyDown(Keyboard.KEY_1);
        boolean notWireFrame = Keyboard.isKeyDown(Keyboard.KEY_2);

        if (wireFrame && !notWireFrame)
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        if (notWireFrame && !wireFrame)
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

    }

    private static void initLighting() {
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        glEnable(GL_COLOR_MATERIAL);

        float lightAmbient[] = {0.3f, 0.3f, 0.3f, 1.0f};
        float lightDiffuse[] = {1.2f, 1.2f, 1.2f, 1.0f};

        glLightModel(GL_LIGHT_MODEL_AMBIENT, asFloatBuffer(lightAmbient));
        glLight(GL_LIGHT0, GL_DIFFUSE, asFloatBuffer(lightDiffuse));             // Setup The Diffuse Light         
        glLight(GL_LIGHT0, GL_POSITION, asFloatBuffer(light0Position));
    }

    private static FloatBuffer asFloatBuffer(float[] values) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(values.length);
        buffer.put(values);
        buffer.flip();
        return buffer;
    }

    private static int getCamChunkX() {
        int size = Chunk.CHUNK_WIDTH;
        int x = Math.round(camera.x());
        if (x < 0)
            x -= size;
        return x / size;
    }

    private static int getCamChunkZ() {
        int size = Chunk.CHUNK_WIDTH;
        int z = Math.round( camera.z());
        if (z < 0)
            z -= size;
        return z / size;
    }

    private static void checkChunkUpdates(HashMap<Integer, Chunk> map) {
        int chunkRadius = 0; // check 5*5 grid around camera for new Chunks
        Chunk chunk;
        for (int x = -chunkRadius; x <= chunkRadius; x++) {
            for (int z = -chunkRadius; z <= chunkRadius; z++) {
                if (map.containsKey(new Pair(getCamChunkX() + x, getCamChunkZ() + z).hashCode()) == false) {
                    chunk = new Chunk(getCamChunkX() + x,getCamChunkZ() + z);
                    displayListHandle = glGenLists(1);
                    //System.out.println("Chunk count: " + displayListHandle);

                    glNewList(displayListHandle, GL_COMPILE);
                    drawChunk(chunk, x * Chunk.CHUNK_WIDTH, z * Chunk.CHUNK_WIDTH);
                    glEndList();
                    map.put(new Pair(getCamChunkX() + x, getCamChunkZ() + z).hashCode(), chunk);
                    //System.out.println("Vertex count: " + vertexCount);
                }
            }
        }
    }

    public static int getNoise(float x, float z) {
        return (FastNoise.noise(x / 255f, z / 255f, 7));
    }
}
