package voxels;

import voxels.ChunkManager.Chunk;
import voxels.ChunkManager.ChunkManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import voxels.Camera.EulerCamera;
import voxels.ChunkManager.Handle;
import voxels.ChunkManager.Type;
import voxels.Noise.FastNoise;

/**
 *
 * @author otso
 */
public class Voxels {

    /**
     * Title.
     */
    public static final String TITLE = "Voxels";
    /**
     * Texture file names.
     */
    public static final String ATLAS = "atlas";
    /**
     * Set terrain smoothness. Value of one gives mountains withs a width of one
     * block, 30 gives enormous flat areas. Default value is 15.
     */
    public static final int TERRAINS_SMOOTHESS = 15;
    /**
     * Set player's height. One block's height is 1.
     */
    public static float PLAYER_HEIGHT = 2.2f;
    /**
     * Set if night cycle is in use.
     */
    public static final boolean NIGHT_CYCLE = true;
    /**
     * Set if terrain generation's uses a seed.
     */
    public static final boolean USE_SEED = false;
    /**
     * Set seed for terrain generation.
     */
    public static final int SEED = (int) (Math.random() * 20000) - 10000;
    /**
     * Set player's Field of View.
     */
    public static final int FIELD_OF_VIEW = 90;
    public static int chunkCreationDistance = 3;
    public static int chunkRenderDistance = 12;
    private static Texture atlas;
    public static final float WaterOffs = 0.28f;
    public static float START_TIME;

    private static ChunkManager chunkManager;

    private static EulerCamera camera;
    private static float light0Position[] = {-2000.0f, 50000.0f, 8000.0f, 1.0f};

    private static int fps = 0;
    private static long lastFPS = getTime();
    public static int count = 0;
    private static long lastFrame = System.nanoTime();

    public static void main(String[] args) {
        initDisplay();
        initOpenGL();
        initLighting();
        initTextures();
        gameLoop();

    }

    private static void initDisplay() {
        try {
            Display.setDisplayMode(new DisplayMode(1024, 768));
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
        glLoadIdentity();

    }

    private static void initTextures() {
        glEnable(GL_TEXTURE_2D);
        atlas = loadTexture(ATLAS);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
        atlas.bind();
    }

    private static void initLighting() {
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        //glEnable(GL_LIGHT1);

        glEnable(GL_COLOR_MATERIAL);
        glColorMaterial(GL_FRONT, GL_AMBIENT_AND_DIFFUSE);

        float lightAmbient[] = {0.15f, 0.15f, 0.15f, 1.0f};
        float lightDiffuse[] = {1.0f, 1.0f, 1.0f, 1.0f};

        glLightModel(GL_LIGHT_MODEL_AMBIENT, asFloatBuffer(lightAmbient));
        glLight(GL_LIGHT0, GL_DIFFUSE, asFloatBuffer(lightDiffuse));
        //glLight(GL_LIGHT1, GL_DIFFUSE, asFloatBuffer(lightDiffuse));
        glLight(GL_LIGHT0, GL_POSITION, asFloatBuffer(light0Position));
        //glLight(GL_LIGHT1, GL_POSITION, asFloatBuffer(light1Position));

        // Set background to sky blue
        glClearColor(0f / 255f, 0f / 255f, 190f / 255f, 1.0f);
        START_TIME = (System.nanoTime() / 1000000);
    }

    private static EulerCamera InitCamera() {
        camera = new EulerCamera.Builder().setAspectRatio((float) Display.getWidth() / (float) Display.getHeight()).setFieldOfView(FIELD_OF_VIEW).build();
        camera.setChunkManager(chunkManager);
        camera.applyPerspectiveMatrix();
        camera.applyOptimalStates();
        camera.setPosition(camera.x(), 255, camera.z());
        Mouse.setGrabbed(true);
        return camera;
    }

    private static void gameLoop() {
        chunkManager = new ChunkManager();
        camera = InitCamera();
        chunkManager.startGeneration();
        long time = System.nanoTime();

        while (chunkManager.isAtMax() == false) {
            chunkManager.checkChunkUpdates();
            if (chunkManager.chunkAmount() % 20 == 0)
                Display.update();
        }
        System.out.println("Time taken: " + (System.nanoTime() - time) / 1000000000 + " s.");

        chunkManager.getChunkLoader().loadChunks();
        chunkManager.getChunkLoader().start();

        while (!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            processInput(getDelta());
            chunkManager.checkChunkUpdates();

            updateView();
            render();

            updateFPS();
            Display.update();
            Display.sync(60);
        }
        Display.destroy();
        System.exit(0);
    }

    private static void updateView() {
        glLoadIdentity();
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
        camera.applyPerspectiveMatrix();
        camera.applyTranslations();
    }

    private static void render() {

        for (int x = -chunkRenderDistance; x <= chunkRenderDistance; x++) {
            for (int z = -chunkRenderDistance; z <= chunkRenderDistance; z++) {
                //Chunk chunk = chunkManager.getChunk(getCurrentChunkX() + x, getCurrentChunkZId() + z);
                Handle handles = chunkManager.getHandle(getCurrentChunkXId() + x, getCurrentChunkZId() + z);
                if (handles != null) {

                    int vboVertexHandle = handles.vertexHandle;
                    int vboNormalHandle = handles.normalHandle;
                    int vboTexHandle = handles.texHandle;
                    //int vboColorHandle = chunk.getVboColorHandle();
                    int vertices = handles.vertices;

                    glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandle);
                    glVertexPointer(3, GL_FLOAT, 0, 0L);

                    glBindBuffer(GL_ARRAY_BUFFER, vboNormalHandle);
                    glNormalPointer(GL_FLOAT, 0, 0L);

                    glBindBuffer(GL_ARRAY_BUFFER, vboTexHandle);
                    glTexCoordPointer(2, GL_FLOAT, 0, 0L);

                    glEnableClientState(GL_VERTEX_ARRAY);
                    glEnableClientState(GL_NORMAL_ARRAY);
                    glEnableClientState(GL_TEXTURE_COORD_ARRAY);
                    glDrawArrays(GL_TRIANGLES, 0, vertices);
                    glDisableClientState(GL_TEXTURE_COORD_ARRAY);
                    glDisableClientState(GL_NORMAL_ARRAY);
                    glDisableClientState(GL_VERTEX_ARRAY);

                    glBindBuffer(GL_ARRAY_BUFFER, 0);

                }
            }
        }

    }

    private static void processInput(float delta) {
        while (Keyboard.next()) {

            if (Keyboard.isKeyDown(Keyboard.KEY_1)) {
                glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_2)) {
                glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_G)) {
                chunkManager.startGeneration();
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_X)) {
                chunkManager.stopGeneration();
            }

            if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
                camera.toggleFlight();
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_Z)) {
                chunkManager.editBlock(Type.DIRT, xInChunk(), Math.min((int) camera.y(), 255), zInChunk(), getCurrentChunkXId(), getCurrentChunkZId());
            }

        }
        if (Mouse.isGrabbed()) {
            camera.processMouse();
            camera.processKeyboard(delta, 2);
            if (NIGHT_CYCLE) {
                glClearColor(0f, (float) Math.max(0, (255 * Math.sin(timePassed() / 20000)) - 155) / 255f, (float) Math.max(25, (255 * Math.sin(timePassed() / 20000)) + 25) / 255f, 1.0f);
                glLight(GL_LIGHT0, GL_POSITION, asFloatBuffer(new float[]{2500 + camera.x(), (float) (10000 * Math.sin(timePassed() / 20000)), (float) (10000 * Math.cos(timePassed() / 20000)), 1f}));
            }
            else
                glLight(GL_LIGHT0, GL_POSITION, asFloatBuffer(light0Position));
        }
    }

    public static FloatBuffer asFloatBuffer(float[] values) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(values.length);
        buffer.put(values);
        buffer.flip();
        return buffer;
    }

    public final static int getCurrentChunkXId() {
        int x = (int) (camera.x());
        if (x < 0)
            x -= Chunk.CHUNK_WIDTH;
        return x / Chunk.CHUNK_WIDTH;
    }

    public final static int getCurrentChunkZId() {
        int z = (int) (camera.z());
        if (z < 0)
            z -= Chunk.CHUNK_WIDTH;
        return z / Chunk.CHUNK_WIDTH;
    }

    public final static int xInChunk() {
        int x = (int) (camera.x());
        if (x < 0)
            x = Chunk.CHUNK_WIDTH + x % 16;

        return x % Chunk.CHUNK_WIDTH;
    }

    public final static int zInChunk() {
        int z = (int) (camera.z());
        if (z < 0)
            z = Chunk.CHUNK_WIDTH + z % 16;

        return z % Chunk.CHUNK_WIDTH;
    }

    public static int getNoise(float x, float z) {
        int noise;
        if (USE_SEED)
            noise = (int) ((FastNoise.noise(x / (1f * TERRAINS_SMOOTHESS * TERRAINS_SMOOTHESS) + SEED, z / (1f * TERRAINS_SMOOTHESS * TERRAINS_SMOOTHESS) + SEED, 5)) * (Chunk.CHUNK_HEIGHT / 256f)) - 1;
        else
            noise = (int) ((FastNoise.noise(x / (1f * TERRAINS_SMOOTHESS * TERRAINS_SMOOTHESS), z / (1f * TERRAINS_SMOOTHESS * TERRAINS_SMOOTHESS), 5)) * (Chunk.CHUNK_HEIGHT / 256f)) - 1;

        return Math.max(noise, 0);
    }

    public static Texture loadTexture(String key) {
        try {
            return TextureLoader.getTexture("png", new FileInputStream(new File("res\\" + key + ".png")));

        } catch (IOException ex) {
            Logger.getLogger(Voxels.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static float getDelta() {
        long time = getTime();
        int delta = (int) (time - lastFrame);
        lastFrame = time;
        return Math.min(Math.max(delta, 1), 50);

    }

    public static long getTime() {
        return System.nanoTime() / 1000000;
    }

    public static float timePassed() {
        return (System.nanoTime() / 1000000) - START_TIME;
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
