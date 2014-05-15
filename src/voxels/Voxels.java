/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;
import voxels.Camera.EulerCamera;

/**
 *
 * @author otso
 */
public class Voxels {

    public static void main(String[] args) {
        initDisplay();
        initOpenGL();
        //initLighting();
        gameLoop();
    }

    private static void initDisplay() {
        try {
            Display.setDisplayMode(new DisplayMode(640, 480));
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
        EulerCamera camera = InitCamera();
        Chunk chunk = new Chunk();
        int displayListHandle = glGenLists(1);
        glNewList(displayListHandle, GL_COMPILE);
        drawChunk(chunk);
        glEndList();

        while (!Display.isCloseRequested()) {
            startTime = System.nanoTime();
            glViewport(0, 0, Display.getWidth(), Display.getHeight());
            camera.setAspectRatio((float) Display.getWidth() / Display.getHeight());

            if (Display.wasResized()) {
                camera.applyPerspectiveMatrix();
            }
            while (Keyboard.next()) {
                if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                    Mouse.setGrabbed(false);
                }
            }
            System.out.println(camera);
            glLoadIdentity();
            camera.applyTranslations();
            if (Mouse.isGrabbed()) {
                camera.processMouse();
                camera.processKeyboard(16, 5);
            }
            processKeyboard();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glTranslatef(-0, -5, -20);
            glCallList(displayListHandle);
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

    private static void drawChunk(Chunk chunk) {
        for (int x = 0; x < chunk.blocks.length; x++) {
            for (int y = 0; y < chunk.blocks[x].length; y++) {
                for (int z = 0; z < chunk.blocks[x][y].length; z++) {
                    drawCube(x, y, z, 1);
                }
            }
        }
    }

    private static EulerCamera InitCamera() {
        EulerCamera camera = new EulerCamera.Builder().setAspectRatio((float) Display.getWidth() / (float) Display.getHeight()).setFieldOfView(60).build();
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

    public static void drawCube(float size) {
        glBegin(GL_QUADS);
        // front face
        glNormal3f(0f, 0f, 1f);
        glColor3f(1.0f, 0.0f, 0.0f);
        glVertex3f(size / 2, size / 2, size / 2);
        glVertex3f(-size / 2, size / 2, size / 2);
        glVertex3f(-size / 2, -size / 2, size / 2);
        glVertex3f(size / 2, -size / 2, size / 2);
        // left face
        glNormal3f(-1f, 0f, 0f);
        glColor3f(0.0f, 1.0f, 0.0f);
        glVertex3f(-size / 2, size / 2, size / 2);
        glVertex3f(-size / 2, -size / 2, size / 2);
        glVertex3f(-size / 2, -size / 2, -size / 2);
        glVertex3f(-size / 2, size / 2, -size / 2);
        // back face
        glNormal3f(0f, 0f, -1f);
        glColor3f(0.0f, 0.0f, 1.0f);
        glVertex3f(size / 2, size / 2, -size / 2);
        glVertex3f(-size / 2, size / 2, -size / 2);
        glVertex3f(-size / 2, -size / 2, -size / 2);
        glVertex3f(size / 2, -size / 2, -size / 2);
        // right face
        glNormal3f(1f, 0f, 0f);
        glColor3f(1.0f, 1.0f, 0.0f);
        glVertex3f(size / 2, size / 2, size / 2);
        glVertex3f(size / 2, -size / 2, size / 2);
        glVertex3f(size / 2, -size / 2, -size / 2);
        glVertex3f(size / 2, size / 2, -size / 2);
        // top face
        glNormal3f(0f, 1f, 0f);
        glColor3f(1.0f, 0.0f, 1.0f);
        glVertex3f(size / 2, size / 2, size / 2);
        glVertex3f(-size / 2, size / 2, size / 2);
        glVertex3f(-size / 2, size / 2, -size / 2);
        glVertex3f(size / 2, size / 2, -size / 2);
        // bottom face
        glNormal3f(0f, -1f, 0f);
        glColor3f(0.0f, 1.0f, 1.0f);
        glVertex3f(size / 2, -size / 2, size / 2);
        glVertex3f(-size / 2, -size / 2, size / 2);
        glVertex3f(-size / 2, -size / 2, -size / 2);
        glVertex3f(size / 2, -size / 2, -size / 2);

        glEnd();
    }

    public static void drawCube(float x, float y, float z, float size) {
        glBegin(GL_QUADS);
        // front face
        glNormal3f(0f, 0f, 1f);
        glColor3f(1.0f, 0.0f, 0.0f);
        glVertex3f(size / 2 + x, size / 2 + y, size / 2 + z);
        glVertex3f(-size / 2 + x, size / 2 + y, size / 2 + z);
        glVertex3f(-size / 2 + x, -size / 2 + y, size / 2 + z);
        glVertex3f(size / 2 + x, -size / 2 + y, size / 2 + z);
        // left face
        glNormal3f(-1f, 0f, 0f);
        glColor3f(0.0f, 1.0f, 0.0f);
        glVertex3f(-size / 2 + x, size / 2 + y, size / 2 + z);
        glVertex3f(-size / 2 + x, -size / 2 + y, size / 2 + z);
        glVertex3f(-size / 2 + x, -size / 2 + y, -size / 2 + z);
        glVertex3f(-size / 2 + x, size / 2 + y, -size / 2 + z);
        // back face
        glNormal3f(0f, 0f, -1f);
        glColor3f(0.0f, 0.0f, 1.0f);
        glVertex3f(size / 2 + x, size / 2 + y, -size / 2 + z);
        glVertex3f(-size / 2 + x, size / 2 + y, -size / 2 + z);
        glVertex3f(-size / 2 + x, -size / 2 + y, -size / 2 + z);
        glVertex3f(size / 2 + x, -size / 2 + y, -size / 2 + z);
        // right face
        glNormal3f(1f, 0f, 0f);
        glColor3f(1.0f, 1.0f, 0.0f);
        glVertex3f(size / 2 + x, size / 2 + y, size / 2 + z);
        glVertex3f(size / 2 + x, -size / 2 + y, size / 2 + z);
        glVertex3f(size / 2 + x, -size / 2 + y, -size / 2 + z);
        glVertex3f(size / 2 + x, size / 2 + y, -size / 2 + z);
        // top face
        glNormal3f(0f, 1f, 0f);
        glColor3f(1.0f, 0.0f, 1.0f);
        glVertex3f(size / 2 + x, size / 2 + y, size / 2 + z);
        glVertex3f(-size / 2 + x, size / 2 + y, size / 2 + z);
        glVertex3f(-size / 2 + x, size / 2 + y, -size / 2 + z);
        glVertex3f(size / 2 + x, size / 2 + y, -size / 2 + z);
        // bottom face
        glNormal3f(0f, -1f, 0f);
        glColor3f(0.0f, 1.0f, 1.0f);
        glVertex3f(size / 2 + x, -size / 2 + y, size / 2 + z);
        glVertex3f(-size / 2 + x, -size / 2 + y, size / 2 + z);
        glVertex3f(-size / 2 + x, -size / 2 + y, -size / 2 + z);
        glVertex3f(size / 2 + x, -size / 2 + y, -size / 2 + z);

        glEnd();
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

        float lightAmbient[] = {0.1f, 0.1f, 0.1f, 1.0f};
        float lightDiffuse[] = {0.6f, 0.6f, 0.6f, 1.0f};
        float light0Position[] = {30.0f, 30.0f, 30.0f, 1.0f};

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

}
