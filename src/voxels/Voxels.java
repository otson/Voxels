/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voxels;

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
        gameLoop();
    }
    
    private static void initDisplay() {
        try {
            Display.setDisplayMode(new DisplayMode(640, 480));
            //Display.setVSyncEnabled(true);
            Display.setTitle("Demo");
            //Display.setResizable(true);
            Display.create();
        } catch (LWJGLException e) {
            e.printStackTrace();
            Display.destroy();
            System.exit(1);
        }
    }
    

    private static void gameLoop() {
        EulerCamera camera = InitCamera();

        while (!Display.isCloseRequested()) {
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
                camera.processKeyboard(16);
            }
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glTranslatef(0,-0,-5);

            drawCube(1);
            Display.update();
            Display.sync(60);
        }
        Display.destroy();
        System.exit(0);
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
        gluPerspective((float) 30, (float) (Display.getWidth() / Display.getHeight()), 0.001f, 400);
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

}
