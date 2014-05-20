package voxels;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import static org.lwjgl.Sys.getTime;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import voxels.Camera.EulerCamera;
import static voxels.Chunk.CHUNK_HEIGHT;
import static voxels.Chunk.CHUNK_WIDTH;
import voxels.Noise.FastNoise;

/**
 *
 * @author otso
 */
public class Voxels {

    public static final String TITLE = "Voxels";
    /**
     * Set terrain smoothness. Value of one gives mountains withs a width of one
     * block, 30 gives enormous flat areas. Default value is 15.
     */
    public static final int TERRAINS_SMOOTHESS = 9;
    /**
     * Set player's height. One block's height is 1.
     */
    public static float PLAYER_HEIGHT = 3.7f + 0.5f;
    /**
     * Set player's Field of View.
     */
    public static final int FIELD_OF_VIEW = 90;

    public static HashMap<Integer, Chunk> map;
    public static int chunkCreationDistance = 3;
    public static int chunkRenderDistance = 3;
    public static Texture atlas;
    public static final float WaterOffs = 0.28f;

    private static EulerCamera camera;
    private static ChunkCreator chunkCreator = new ChunkCreator();
    private static float light0Position[] = {-200.0f, 5000.0f, -800.0f, 1.0f};
    private static float light1Position[] = {200.0f, 5000.0f, 800.0f, 1.0f};

    private static boolean[][][] top = new boolean[Chunk.CHUNK_WIDTH][][];
    private static boolean[][][] bottom = new boolean[Chunk.CHUNK_WIDTH][][];
    private static boolean[][][] left = new boolean[Chunk.CHUNK_WIDTH][][];
    private static boolean[][][] right = new boolean[Chunk.CHUNK_WIDTH][][];
    private static boolean[][][] front = new boolean[Chunk.CHUNK_WIDTH][][];
    private static boolean[][][] back = new boolean[Chunk.CHUNK_WIDTH][][];
    private static int vertexSize = 3;
    private static int normalSize = 3;
    private static int texSize = 2;
    private static int fps = 0;
    private static long lastFPS = getTime();
    
    public static int count = 0;

    private static long lastFrame = System.nanoTime();

    public static void main(String[] args) {
        long start = System.nanoTime();
        int temp;
        for (int i = 0; i < 500; i++) {
            temp = getNoise(i * i, i * i);
        }
        System.out.println("Time taken: "+(System.nanoTime()-start)/1000000+" ms.");
        initDisplay();
        initOpenGL();
        initLighting();
        initTextures();
        initBooleanArrays();
        gameLoop();
    }

    private static void initDisplay() {
        try {
            Display.setDisplayMode(new DisplayMode(800, 480));
            Display.setVSyncEnabled(true);
            Display.setTitle("Voxels");
            Display.create();
        } catch (LWJGLException e) {
            e.printStackTrace();
            Display.destroy();
            System.exit(1);
        }
    }

    private static void initOpenGL() {
        glMatrixMode(GL_PROJECTION);
        glMatrixMode(GL_MODELVIEW);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
    }

    private static void initTextures() {
        glEnable(GL_TEXTURE_2D);
        atlas = loadTexture("atlas");
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        atlas.bind();
    }

    private static void initLighting() {
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        glEnable(GL_LIGHT1);
        glEnable(GL_COLOR_MATERIAL);

        float lightAmbient[] = {0.3f, 0.3f, 0.3f, 1.0f};
        float lightDiffuse[] = {1f, 1f, 1f, 1.0f};

        glLightModel(GL_LIGHT_MODEL_AMBIENT, asFloatBuffer(lightAmbient));
        glLight(GL_LIGHT0, GL_DIFFUSE, asFloatBuffer(lightDiffuse));
        glLight(GL_LIGHT1, GL_DIFFUSE, asFloatBuffer(lightDiffuse));
        glLight(GL_LIGHT0, GL_POSITION, asFloatBuffer(light0Position));
        glLight(GL_LIGHT1, GL_POSITION, asFloatBuffer(light1Position));
    }

    private static EulerCamera InitCamera() {
        camera = new EulerCamera.Builder().setAspectRatio((float) Display.getWidth() / (float) Display.getHeight()).setFieldOfView(FIELD_OF_VIEW).build();
        camera.applyPerspectiveMatrix();
        camera.applyOptimalStates();
        Mouse.setGrabbed(true);
        return camera;
    }

    private static void gameLoop() {
        float delta = getDelta();
        long startTime;
        long endTime;
        long totalTime = 0;
        int camSpeed = 2;
        boolean generateChunks = false;
        boolean running = true;
        boolean moveFaster;
        boolean canFly = false;
        camera = InitCamera();

        map = new HashMap<>();
        map.put(new Pair(getCamChunkX(), getCamChunkZ()).hashCode(), new Chunk(0, 0));
        drawChunkVBO(map.get(new Pair(getCamChunkX(), getCamChunkZ()).hashCode()), 0, 0);

        glMatrixMode(GL_PROJECTION);

        glLoadIdentity();
        //gluPerspective((float) 90, 1.6f, 0.3f, 5000);
        glMatrixMode(GL_MODELVIEW);
        glEnable(GL_DEPTH_TEST);
        glClearColor(0f / 255f, 0f / 255f, 190f / 255f, 1.0f);
        camera.setPosition(camera.x(), 256f, camera.z());

        while (!Display.isCloseRequested() && running) {
            delta = getDelta();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            moveFaster = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
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

                if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
                    canFly = !canFly;
                    camera.setFlying(canFly);
                }

                if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
                    running = false;
                }
            }
            if (moveFaster)
                camSpeed *= 3;
            glLoadIdentity();
            if (generateChunks) {
                if (fps %10 == 0) {
                    checkChunkUpdates(map);
                }
            }

            glViewport(0, 0, Display.getWidth(), Display.getHeight());
            camera.setAspectRatio((float) Display.getWidth() / Display.getHeight());

            if (Display.wasResized()) {
                camera.applyPerspectiveMatrix();
            }
            camera.applyTranslations();

            if (Mouse.isGrabbed()) {
                camera.processMouse();
                camera.processKeyboard(delta, camSpeed);
                glLight(GL_LIGHT0, GL_POSITION, asFloatBuffer(light0Position));
                glLight(GL_LIGHT1, GL_POSITION, asFloatBuffer(light1Position));
            }

            for (int x = -chunkRenderDistance; x <= chunkRenderDistance; x++) {
                for (int z = -chunkRenderDistance; z <= chunkRenderDistance; z++) {
                    if (map.containsKey(new Pair(getCamChunkX() + x, getCamChunkZ() + z).hashCode())) {
                        Chunk currentChunk = map.get(new Pair(getCamChunkX() + x, getCamChunkZ() + z).hashCode());
                        int vboVertexHandle = currentChunk.getVboVertexHandle();
                        int vboNormalHandle = currentChunk.getVboNormalHandle();
                        int vboTexHandle = currentChunk.getVboTexHandle();
                        int vertices = currentChunk.getVertices();
                        glPushMatrix();
                        glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandle);
                        glVertexPointer(vertexSize, GL_FLOAT, 0, 0L);

                        glBindBuffer(GL_ARRAY_BUFFER, vboNormalHandle);
                        glNormalPointer(GL_FLOAT, 0, 0L);

                        glBindBuffer(GL_ARRAY_BUFFER, vboTexHandle);
                        glTexCoordPointer(texSize, GL_FLOAT, 0, 0L);

                        glEnableClientState(GL_VERTEX_ARRAY);
                        glEnableClientState(GL_NORMAL_ARRAY);
                        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
                        glDrawArrays(GL_TRIANGLE_STRIP, 0, vertices);
                        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
                        glDisableClientState(GL_NORMAL_ARRAY);
                        glDisableClientState(GL_VERTEX_ARRAY);

                        glBindBuffer(GL_ARRAY_BUFFER, 0);
                        glPopMatrix();
                    }
                }
            }

            if (moveFaster)
                camSpeed /= 3;
            Display.update();
            Display.sync(60);
            updateFPS();

        }
        Display.destroy();
        System.exit(0);
    }

    private static void drawChunkVBO(Chunk chunk, int xOff, int zOff) {
        long startTime = System.nanoTime();
        int drawnBlocks = 0;
        int vertices = 0;
        int size = 1;
        zOff += getCamChunkZ() * chunk.blocks.length;
        xOff += getCamChunkX() * chunk.blocks.length;
        for (int x = 0; x < chunk.blocks.length; x++) {
            for (int z = 0; z < chunk.blocks[x][0].length; z++) {
                for (int y = 0; y < chunk.blocks[x].length; y++) {
                    if (chunk.blocks[x][y][z].isType(Block.GROUND)) {
                        vertices += calculateGroundVertices(chunk, x, y, z, getCamChunkX() * chunk.blocks.length + xOff, 0, getCamChunkZ() * chunk.blocks.length + zOff, 1);
                        drawnBlocks++;
                    }
                    else if (chunk.blocks[x][y][z].isType(Block.WATER)) {
                        vertices += calculateWaterVertices(chunk, x, y, z, getCamChunkX() * chunk.blocks.length + xOff, 0, getCamChunkZ() * chunk.blocks.length + zOff, 1);
                        drawnBlocks++;
                    }
                }
            }
        }
        //System.out.println("");
        startTime = System.nanoTime();
        chunk.setVertices(vertices);
        //System.out.println("Vertices: " + vertices);
        float[] vertexArray = new float[vertices * vertexSize];
        float[] normalArray = new float[vertices * normalSize];
        float[] texArray = new float[vertices * texSize];

        int vArrayPos = 0;
        int nArrayPos = 0;
        int tArrayPos = 0;

        FloatBuffer vertexData = BufferUtils.createFloatBuffer(vertices * vertexSize);
        FloatBuffer normalData = BufferUtils.createFloatBuffer(vertices * vertexSize);
        FloatBuffer texData = BufferUtils.createFloatBuffer(vertices * texSize);

        for (int x = 0; x < chunk.blocks.length; x++) {
            for (int z = 0; z < chunk.blocks[x][0].length; z++) {
                for (int y = 0; y < chunk.blocks[x].length; y++) {
                    if (chunk.blocks[x][y][z].isType(Block.GROUND)) {
                        if (front[x][y][z]) {

                            // upper left - +
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            // lower left - -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            // lower right + -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

//                            // upper right + +
//                            normalArray[nArrayPos] = 0;
//                            nArrayPos++;
//                            normalArray[nArrayPos] = 0;
//                            nArrayPos++;
//                            normalArray[nArrayPos] = 1;
//                            nArrayPos++;
//
//                            vertexArray[vArrayPos] = size / 2f + x + xOff;
//                            vArrayPos++;
//                            vertexArray[vArrayPos] = size / 2f + y;
//                            vArrayPos++;
//                            vertexArray[vArrayPos] = size / 2f + z + zOff;
//                            vArrayPos++;
//
//                            texArray[tArrayPos] = 1 / 2f + 0.5f;
//                            tArrayPos++;
//                            texArray[tArrayPos] = 0 + 0.5f;
//                            tArrayPos++;

                        }
                        if (back[x][y][z]) {
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

//                            texArray[tArrayPos] = 1 / 2f + 0.5f;
//                            tArrayPos++;
//                            texArray[tArrayPos] = 0 + 0.5f;
//                            tArrayPos++;

                            // upper left + +
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // lower left + -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // lower right - -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

//                            // upper right - +
//                            normalArray[nArrayPos] = 0;
//                            nArrayPos++;
//                            normalArray[nArrayPos] = 0;
//                            nArrayPos++;
//                            normalArray[nArrayPos] = -1;
//                            nArrayPos++;
//
//                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
//                            vArrayPos++;
//                            vertexArray[vArrayPos] = size / 2f + y;
//                            vArrayPos++;
//                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
//                            vArrayPos++;

                        }
                        if (right[x][y][z]) {

                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

//                            texArray[tArrayPos] = 1 / 2f + 0.5f;
//                            tArrayPos++;
//                            texArray[tArrayPos] = 0 + 0.5f;
//                            tArrayPos++;

                            // upper right + +
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            // lower right - +
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            // lower left - -
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

//                            // upper left + -
//                            normalArray[nArrayPos] = 1;
//                            nArrayPos++;
//                            normalArray[nArrayPos] = 0;
//                            nArrayPos++;
//                            normalArray[nArrayPos] = 0;
//                            nArrayPos++;
//
//                            vertexArray[vArrayPos] = size / 2f + x + xOff;
//                            vArrayPos++;
//                            vertexArray[vArrayPos] = size / 2f + y;
//                            vArrayPos++;
//                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
//                            vArrayPos++;

                        }
                        if (left[x][y][z]) {
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

//                            texArray[tArrayPos] = 1 / 2f + 0.5f;
//                            tArrayPos++;
//                            texArray[tArrayPos] = 0 + 0.5f;
//                            tArrayPos++;

                            // upper right + -
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // lower right - -
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // lower left - +
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

//                            // upper left + +
//                            normalArray[nArrayPos] = -1;
//                            nArrayPos++;
//                            normalArray[nArrayPos] = 0;
//                            nArrayPos++;
//                            normalArray[nArrayPos] = 0;
//                            nArrayPos++;
//
//                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
//                            vArrayPos++;
//                            vertexArray[vArrayPos] = size / 2f + y;
//                            vArrayPos++;
//                            vertexArray[vArrayPos] = size / 2f + z + zOff;
//                            vArrayPos++;
                        }
                        if (top[x][y][z]) {

                            // upper left
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 0f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0f;
                            tArrayPos++;

                            // lower left
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 0f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1f / 2f;
                            tArrayPos++;

                            // lower right
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 1f / 2f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1f / 2f;
                            tArrayPos++;

//                            // upper right
//                            normalArray[nArrayPos] = 0;
//                            nArrayPos++;
//                            normalArray[nArrayPos] = 1;
//                            nArrayPos++;
//                            normalArray[nArrayPos] = 0;
//                            nArrayPos++;
//
//                            vertexArray[vArrayPos] = size / 2f + x + xOff;
//                            vArrayPos++;
//                            vertexArray[vArrayPos] = size / 2f + y;
//                            vArrayPos++;
//                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
//                            vArrayPos++;
//
//                            texArray[tArrayPos] = 1f / 2f;
//                            tArrayPos++;
//                            texArray[tArrayPos] = 0f;
//                            tArrayPos++;
                        }
                        if (bottom[x][y][z]) {
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 0 + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1 / 2f + 0.5f;
                            tArrayPos++;

//                            texArray[tArrayPos] = 1 / 2f + 0.5f;
//                            tArrayPos++;
//                            texArray[tArrayPos] = 0 + 0.5f;
//                            tArrayPos++;

                            // upper left + -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // lower left - -
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            // lower right - +
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = -1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + y;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

//                            // upper right + +
//                            normalArray[nArrayPos] = 0;
//                            nArrayPos++;
//                            normalArray[nArrayPos] = -1;
//                            nArrayPos++;
//                            normalArray[nArrayPos] = 0;
//                            nArrayPos++;
//
//                            vertexArray[vArrayPos] = size / 2f + x + xOff;
//                            vArrayPos++;
//                            vertexArray[vArrayPos] = -size / 2f + y;
//                            vArrayPos++;
//                            vertexArray[vArrayPos] = size / 2f + z + zOff;
//                            vArrayPos++;

                        }
                    }
                    if (chunk.blocks[x][y][z].isType(Block.WATER)) {
                        if (top[x][y][z]) {

                            // upper left
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y - WaterOffs;
                            vArrayPos++;
                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 0f;
                            tArrayPos++;
                            texArray[tArrayPos] = 0.5f;
                            tArrayPos++;

                            // lower left
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = -size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y - WaterOffs;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 0f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1f;
                            tArrayPos++;

                            // lower right
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;
                            normalArray[nArrayPos] = 1;
                            nArrayPos++;
                            normalArray[nArrayPos] = 0;
                            nArrayPos++;

                            vertexArray[vArrayPos] = size / 2f + x + xOff;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + y - WaterOffs;
                            vArrayPos++;
                            vertexArray[vArrayPos] = size / 2f + z + zOff;
                            vArrayPos++;

                            texArray[tArrayPos] = 1f / 2f;
                            tArrayPos++;
                            texArray[tArrayPos] = 1f;
                            tArrayPos++;

//                            // upper right
//                            normalArray[nArrayPos] = 0;
//                            nArrayPos++;
//                            normalArray[nArrayPos] = 1;
//                            nArrayPos++;
//                            normalArray[nArrayPos] = 0;
//                            nArrayPos++;
//
//                            vertexArray[vArrayPos] = size / 2f + x + xOff;
//                            vArrayPos++;
//                            vertexArray[vArrayPos] = size / 2f + y - WaterOffs;
//                            vArrayPos++;
//                            vertexArray[vArrayPos] = -size / 2f + z + zOff;
//                            vArrayPos++;
//
//                            texArray[tArrayPos] = 1f / 2f;
//                            tArrayPos++;
//                            texArray[tArrayPos] = 0.5f;
//                            tArrayPos++;
                        }

                    }
                }
            }
        }

        vertexData.put(vertexArray);
        vertexData.flip();

        normalData.put(normalArray);
        normalData.flip();

        texData.put(texArray);
        texData.flip();

        int vboVertexHandle = glGenBuffers();
        chunk.setVboVertexHandle(vboVertexHandle);

        glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandle);
        glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vboNormalHandle = glGenBuffers();
        chunk.setVboNormalHandle(vboNormalHandle);

        glBindBuffer(GL_ARRAY_BUFFER, vboNormalHandle);
        glBufferData(GL_ARRAY_BUFFER, normalData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        int vboTexHandle = glGenBuffers();
        chunk.setVboTexHandle(vboTexHandle);

        glBindBuffer(GL_ARRAY_BUFFER, vboTexHandle);
        glBufferData(GL_ARRAY_BUFFER, texData, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

    }

    public static int calculateGroundVertices(Chunk chunk, float x, float y, float z, float xOff, float yOff, float zOff, float size) {
        int zMax = Chunk.CHUNK_WIDTH - 1;
        int xMax = Chunk.CHUNK_WIDTH - 1;
        int yMax = Chunk.CHUNK_HEIGHT - 1;
        int xx = Math.round(x);
        int yy = Math.round(y);
        int zz = Math.round(z);
        boolean render = false;
        int returnVertices = 0;
        int difference = 0;
        int[][] maxHeights = chunk.maxHeights;

        // front face
        if (z == zMax)
            render = true;
        else {
            difference = maxHeights[xx][zz] - maxHeights[xx][zz + 1];
        }
        if (render || chunk.blocks[xx][yy][zz + 1].isType(Block.AIR) || chunk.blocks[xx][yy][zz + 1].isType(Block.WATER) && (maxHeights[xx][zz] - difference < y && y <= maxHeights[xx][zz])) {
            front[xx][yy][zz] = true;
            returnVertices += 3;
        }
        else
            front[xx][yy][zz] = false;

        // left face
        render = false;
        if (x == 0)
            render = true;
        else {
            difference = maxHeights[xx][zz] - maxHeights[xx - 1][zz];
        }
        if (render || chunk.blocks[xx - 1][yy][zz].isType(Block.AIR) || chunk.blocks[xx - 1][yy][zz].isType(Block.WATER) && (maxHeights[xx][zz] - difference < y && y <= maxHeights[xx][zz])) {
            left[xx][yy][zz] = true;
            returnVertices += 3;
        }
        else
            left[xx][yy][zz] = false;

        // back face
        render = false;
        if (z == 0)
            render = true;
        else {
            difference = maxHeights[xx][zz] - maxHeights[xx][zz - 1];
        }
        if (render || chunk.blocks[xx][yy][zz - 1].isType(Block.AIR) || chunk.blocks[xx][yy][zz - 1].isType(Block.WATER) && (maxHeights[xx][zz] - difference < y && y <= maxHeights[xx][zz])) {
            back[xx][yy][zz] = true;
            returnVertices += 3;
        }
        else
            back[xx][yy][zz] = false;

        // right face
        render = false;
        if (x == xMax)
            render = true;
        else {
            difference = maxHeights[xx][zz] - maxHeights[xx + 1][zz];
        }
        if (render || chunk.blocks[xx + 1][yy][zz].isType(Block.AIR) || chunk.blocks[xx + 1][yy][zz].isType(Block.WATER) && (maxHeights[xx][zz] - difference < y && y <= maxHeights[xx][zz])) {
            right[xx][yy][zz] = true;
            returnVertices += 3;
        }
        else
            right[xx][yy][zz] = false;

        // top face
        render = false;
        if (y == yMax)
            render = true;
        if (render || chunk.blocks[xx][yy + 1][zz].isType(Block.AIR) || chunk.blocks[xx][yy + 1][zz].isType(Block.WATER) && maxHeights[xx][zz] == yy) {
            top[xx][yy][zz] = true;
            returnVertices += 3;
        }
        else
            top[xx][yy][zz] = false;

        // bottom face
        render = false;
        if (y == 0)
            render = true;
        if (render || chunk.blocks[xx][yy - 1][zz].isType(Block.AIR) || chunk.blocks[xx][yy - 1][zz].isType(Block.WATER) && maxHeights[xx][zz] + 1 == yy) {
            bottom[xx][yy][zz] = true;
            returnVertices += 3;
        }
        else
            bottom[xx][yy][zz] = false;
        return returnVertices;
    }

    public static int calculateWaterVertices(Chunk chunk, float x, float y, float z, float xOff, float yOff, float zOff, float size) {
        int yMax = Chunk.CHUNK_HEIGHT - 1;
        int xx = Math.round(x);
        int yy = Math.round(y);
        int zz = Math.round(z);
        int returnVertices = 0;
        boolean render = false;

        // top face
        if (y == yMax)
            render = true;
        if (render || chunk.blocks[xx][yy + 1][zz].isType(Block.AIR) && y == Chunk.WATER_HEIGHT) {
            top[xx][yy][zz] = true;
            returnVertices += 3;
        }
        else
            top[xx][yy][zz] = false;

        return returnVertices;

    }

    private static void processKeyboard() {
        boolean wireFrame = Keyboard.isKeyDown(Keyboard.KEY_1);
        boolean notWireFrame = Keyboard.isKeyDown(Keyboard.KEY_2);

        if (wireFrame && !notWireFrame)
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        if (notWireFrame && !wireFrame)
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

    }

    public static FloatBuffer asFloatBuffer(float[] values) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(values.length);
        buffer.put(values);
        buffer.flip();
        return buffer;
    }

    public final static int getCamChunkX() {
        int size = Chunk.CHUNK_WIDTH;
        int x = (int) (camera.x());
        if (x < 0)
            x -= size;
        return x / size;
    }

    public final static int getCamChunkZ() {
        int size = Chunk.CHUNK_WIDTH;
        int z = (int) (camera.z());
        if (z < 0)
            z -= size;
        return z / size;
    }

    private static void checkChunkUpdates(HashMap<Integer, Chunk> map) {
        boolean newChunk = false;
        Chunk chunk;
        int[] xzCoords;
        chunkCreator.setCurrentChunkX(getCamChunkX());
        chunkCreator.setCurrentChunkZ(getCamChunkZ());
        while (!newChunk && chunkCreator.notAtMax()) {
            xzCoords = chunkCreator.getNewXZ();
            int x = xzCoords[0];
            int z = xzCoords[1];

            if (map.containsKey(new Pair(getCamChunkX() + x, getCamChunkZ() + z).hashCode()) == false) {
                chunk = new Chunk(getCamChunkX() + x, getCamChunkZ() + z);
                long start = System.nanoTime();
                drawChunkVBO(chunk, x * Chunk.CHUNK_WIDTH, z * Chunk.CHUNK_WIDTH);

                map.put(new Pair(getCamChunkX() + x, getCamChunkZ() + z).hashCode(), chunk);
                newChunk = true;
                long end = System.nanoTime();
                //if((end - start) / 1000000 > 50)
                    System.out.println("Chunk creation took: " + (end - start) / 1000000 + " ms.");
            }
        }
    }

    public static int getNoise(float x, float z) {
        int noise = (int) ((FastNoise.noise(x / (1f * TERRAINS_SMOOTHESS * TERRAINS_SMOOTHESS), z / (1f * TERRAINS_SMOOTHESS * TERRAINS_SMOOTHESS), 2)) * (Chunk.CHUNK_HEIGHT / 256f)) - 1;
        count++;
        return Math.max(noise, 0);
    }

    public static Texture loadTexture(String key) {
        try {
            return TextureLoader.getTexture("png", new FileInputStream(new File("res/" + key + ".png")));

        } catch (IOException ex) {
            Logger.getLogger(Voxels.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static void initBooleanArrays() {
        for (int x = 0; x < top.length; x++) {
            top[x] = new boolean[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < top[x].length; y++) {
                top[x][y] = new boolean[CHUNK_WIDTH];
            }
        }
        for (int x = 0; x < bottom.length; x++) {
            bottom[x] = new boolean[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < bottom[x].length; y++) {
                bottom[x][y] = new boolean[CHUNK_WIDTH];
            }
        }
        for (int x = 0; x < right.length; x++) {
            right[x] = new boolean[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < right[x].length; y++) {
                right[x][y] = new boolean[CHUNK_WIDTH];
            }
        }
        for (int x = 0; x < left.length; x++) {
            left[x] = new boolean[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < left[x].length; y++) {
                left[x][y] = new boolean[CHUNK_WIDTH];
            }
        }
        for (int x = 0; x < front.length; x++) {
            front[x] = new boolean[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < front[x].length; y++) {
                front[x][y] = new boolean[CHUNK_WIDTH];
            }
        }
        for (int x = 0; x < back.length; x++) {
            back[x] = new boolean[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < back[x].length; y++) {
                back[x][y] = new boolean[CHUNK_WIDTH];
            }
        }
    }

    public static float getDelta() {
        long time = getTime();
        int delta = (int) (time - lastFrame);
        lastFrame = time;
        return Math.max(delta, 1);

    }

    public static long getTime() {
        return System.nanoTime() / 1000000;
    }

    public static void updateFPS() {
        if (getTime() - lastFPS > 1000) {
            Display.setTitle(TITLE + " - FPS: " + fps);
            fps = 0; //reset the FPS counter
            lastFPS += 1000; //add one second
        }
        fps++;
    }
}
