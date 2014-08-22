package voxels;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.Math.PI;
import java.nio.FloatBuffer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;

import static org.lwjgl.opengl.GL15.*;

import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import voxels.Camera.EulerCamera;
import voxels.ChunkManager.Chunk;
import static voxels.ChunkManager.Chunk.WORLD_HEIGHT;
import static voxels.ChunkManager.Chunk.GROUND_SHARE;
import voxels.ChunkManager.ChunkManager;
import voxels.ChunkManager.Handle;
import voxels.ChunkManager.Type;
import voxels.Noise.FastNoise;
import voxels.Noise.SimplexNoise;

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
    public static final String ATLAS = "atlas2";
    /**
     * Set terrain smoothness. Value of one gives mountains widths a width of
     * one block, 30 gives enormous flat areas. Default value is 15.
     */
    public static final int TERRAIN_SMOOTHNESS = 20;
    public static final int THREE_DIM_SMOOTHNESS = 50;
    /**
     * Set player's height. One block's height is 1.
     */
    public static float PLAYER_HEIGHT = 2.0f;
    /**
     * Set if night cycle is in use.
     */
    public static final boolean NIGHT_CYCLE = false;
    /**
     * Set if terrain generation uses a seed.
     */
    public static final boolean USE_SEED = false;
    /**
     * Set if 3D simplex noise is used to generate terrain.
     */
    public static final boolean USE_3D_NOISE = true;

    /**
     * Set air block percentage if 3D noise is in use.
     */
    public final static int AIR_PERCENTAGE = 60;
    /**
     * Set seed for terrain generation.
     */
    public static final int SEED = (int) (Math.random() * 20000) - 10000;
    /**
     * Set player's Field of View.
     */
    public static final int FIELD_OF_VIEW = 90;
    public static int chunkCreationDistance = 7;
    public static int chunkRenderDistance = 20;
    public static Texture atlas;
    public static Sound running;
    public static Sound jumping;
    public static Sound impact;
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
        initSounds();
        gameLoop();
    }

    private static void initDisplay() {
        try {
            Display.setDisplayMode(new DisplayMode(1440, 900));
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

    public static void initSounds() {

        TinySound.init();

        running = TinySound.loadSound(Voxels.class.getClassLoader().getResource("resources/sounds/runningHardSurface.wav"));
        jumping = TinySound.loadSound(Voxels.class.getClassLoader().getResource("resources/sounds/jump.wav"));
        impact = TinySound.loadSound(Voxels.class.getClassLoader().getResource("resources/sounds/impact.wav"));

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

        glEnable(GL_COLOR_MATERIAL);
        glColorMaterial(GL_FRONT, GL_AMBIENT_AND_DIFFUSE);

        float lightAmbient[] = {0.15f, 0.15f, 0.15f, 1.0f};
        float lightDiffuse[] = {1.0f, 1.0f, 1.0f, 1.0f};

        glLightModel(GL_LIGHT_MODEL_AMBIENT, asFloatBuffer(lightAmbient));
        glLight(GL_LIGHT0, GL_DIFFUSE, asFloatBuffer(lightDiffuse));
        glLight(GL_LIGHT0, GL_POSITION, asFloatBuffer(light0Position));

        // Set background to sky blue
        glClearColor(0f / 255f, 0f / 255f, 190f / 255f, 1.0f);
        START_TIME = (System.nanoTime() / 1000000);
    }

    private static EulerCamera InitCamera() {
        camera = new EulerCamera.Builder().setAspectRatio((float) Display.getWidth() / (float) Display.getHeight()).setFieldOfView(FIELD_OF_VIEW).build();
        camera.setChunkManager(chunkManager);
        camera.applyPerspectiveMatrix();
        camera.applyOptimalStates();
        camera.setPosition(camera.x(), Chunk.CHUNK_SIZE * Chunk.VERTICAL_CHUNKS, camera.z());
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
            if (chunkManager.chunkAmount() % 20 == 0) {
                Display.update();
            }
        }
        System.out.println("Chunks created in " + (System.nanoTime() - time) / 1000000000 + " seconds.");
        time = System.nanoTime();
        chunkManager.createVBOs();
        System.out.println("VBOs created in " + (System.nanoTime() - time) / 1000000000 + " seconds.");
        chunkManager.getChunkLoader().loadChunks();
        chunkManager.getChunkLoader().start();
        chunkManager.stopGeneration();

        while (!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            updateView();
            processInput(getDelta());
            chunkManager.checkChunkUpdates();
            render();

            updateFPS();
            Display.update();
            Display.sync(60);
        }
        Display.destroy();
        TinySound.shutdown();
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
                for (int y = 0; y < Chunk.VERTICAL_CHUNKS; y++) {
                    Handle handles = chunkManager.getHandle(getCurrentChunkXId() + x, y, getCurrentChunkZId() + z);
                    if (handles != null) {

                        int vboVertexHandle = handles.vertexHandle;
                        int vboNormalHandle = handles.normalHandle;
                        int vboTexHandle = handles.texHandle;
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
        drawAimLine();

    }

    private static void drawAimLine() {
        glDisable(GL_TEXTURE_2D);
        Vector3f direction = getDirectionVector(1f);
        glColor3f(1f, 0, 0);
        glPointSize(25);
        glBegin(GL_POINTS);
        glVertex3f(direction.x, direction.y, direction.z);
        glEnd();
        glColor3f(1f, 1f, 1f);
        glEnable(GL_TEXTURE_2D);

    }

    public static Vector3f getDirectionVector(float distance) {
        double pitchRadians = Math.toRadians(camera.pitch());
        double yawRadians = Math.toRadians(camera.yaw());

        double sinPitch = Math.sin(pitchRadians);
        double cosPitch = Math.cos(pitchRadians);
        double sinYaw = Math.sin(yawRadians);
        double cosYaw = Math.cos(yawRadians);

        return new Vector3f(camera.x() - distance * (float) -cosPitch * (float) sinYaw, camera.y() - distance * (float) sinPitch, camera.z() + distance * (float) -cosPitch * (float) cosYaw);
    }

    public static double toRadians(double angdeg) {
        return angdeg / 180.0 * PI;
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

        }
        if (Keyboard.isKeyDown(Keyboard.KEY_U)) {
            glTranslatef(0, -200, 0);
        }
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            PLAYER_HEIGHT = 1.0f;
        } else {
            PLAYER_HEIGHT = 2.0f;
        }

        if (Mouse.isGrabbed()) {
            while (Mouse.next()) {
                if (Mouse.getEventButtonState()) {
                    if (Mouse.getEventButton() == 0) {
                        chunkManager.castRay(Type.DIRT);
                    } else if (Mouse.getEventButton() == 1) {
                        chunkManager.castRay(Type.AIR);
                    }
                }
            }
            camera.processMouse();
            camera.processKeyboard(delta, 1.4f);
            if (NIGHT_CYCLE) {
                glClearColor(0f, (float) Math.max(0, (255 * Math.sin(timePassed() / 20000)) - 155) / 255f, (float) Math.max(25, (255 * Math.sin(timePassed() / 20000)) + 25) / 255f, 1.0f);
                glLight(GL_LIGHT0, GL_POSITION, asFloatBuffer(new float[]{2500 + camera.x(), (float) (10000 * Math.sin(timePassed() / 20000)), (float) (10000 * Math.cos(timePassed() / 20000)), 1f}));
            } else {
                glLight(GL_LIGHT0, GL_POSITION, asFloatBuffer(light0Position));
            }
        }
    }

    public static FloatBuffer asFloatBuffer(float[] values) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(values.length);
        buffer.put(values);
        buffer.flip();
        return buffer;
    }

    public final static int getPointerChunkXId(Vector3f vector) {
        int x = (int) Math.floor(vector.x);
        if (x < 0) {
            x -= Chunk.CHUNK_SIZE - 1;
        }
        return x / Chunk.CHUNK_SIZE;
    }

    public final static int getPointerChunkZId(Vector3f vector) {
        int z = (int) Math.floor(vector.z);
        if (z < 0) {
            z -= Chunk.CHUNK_SIZE - 1;
        }
        return z / Chunk.CHUNK_SIZE;
    }

    public final static int getPointerChunkYId(Vector3f vector) {
        int y = (int) vector.y;
        return y / Chunk.CHUNK_SIZE;
    }

    public final static int getCurrentChunkXId() {
        int x = (int) Math.floor(camera.x());
        if (x < 0) {
            x -= Chunk.CHUNK_SIZE - 1;
        }
        return x / Chunk.CHUNK_SIZE;
    }

    public final static int getCurrentChunkZId() {
        int z = (int) Math.floor(camera.z());
        if (z < 0) {
            z -= Chunk.CHUNK_SIZE - 1;
        }
        return z / Chunk.CHUNK_SIZE;
    }

    public final static int getCurrentChunkYId() {
        int y = (int) (camera.y() - PLAYER_HEIGHT);
        return y / Chunk.CHUNK_SIZE;
    }

    public final static int getCurrentChunkXId(float add) {
        int x = (int) Math.floor(camera.x() - add);
        if (x < 0) {
            x -= Chunk.CHUNK_SIZE - 1;
        }
        return x / Chunk.CHUNK_SIZE;
    }

    public final static int getCurrentChunkZId(float add) {
        int z = (int) Math.floor(camera.z() + add);
        if (z < 0) {
            z -= Chunk.CHUNK_SIZE - 1;
        }
        return z / Chunk.CHUNK_SIZE;
    }

    public final static int getCurrentChunkYId(float add) {
        int y = (int) (camera.y() - PLAYER_HEIGHT + add);
        return y / Chunk.CHUNK_SIZE;
    }

    public final static int yInChunk() {
        int y = (int) (camera.y() - PLAYER_HEIGHT);
        return y % Chunk.CHUNK_SIZE;
    }

    public final static int xInChunk() {
        int x = (int) Math.floor(camera.x());
        if (x <= 0) {
            x = Chunk.CHUNK_SIZE + x % Chunk.CHUNK_SIZE;
        }
        return x % Chunk.CHUNK_SIZE;
    }

    public final static int zInChunk() {
        int z = (int) Math.floor(camera.z());
        if (z <= 0) {
            z = Chunk.CHUNK_SIZE + z % Chunk.CHUNK_SIZE;
        }
        return z % Chunk.CHUNK_SIZE;
    }

    public final static int yInChunk(float add) {
        int y = (int) (camera.y() - PLAYER_HEIGHT + add);
        return y % Chunk.CHUNK_SIZE;
    }

    public final static int xInChunk(float add) {
        int x = (int) Math.floor(camera.x() - add);
        if (x <= 0) {
            x = Chunk.CHUNK_SIZE + x % Chunk.CHUNK_SIZE;
        }
        return x % Chunk.CHUNK_SIZE;
    }

    public final static int zInChunk(float add) {
        int z = (int) Math.floor(camera.z() + add);
        if (z <= 0) {
            z = Chunk.CHUNK_SIZE + z % Chunk.CHUNK_SIZE;
        }
        return z % Chunk.CHUNK_SIZE;
    }

    public final static int yInChunkPointer(Vector3f direction) {
        int y = (int) (direction.y);
        return y % Chunk.CHUNK_SIZE;
    }

    public final static int xInChunkPointer(Vector3f direction) {
        int x = (int) Math.floor(direction.x);
        if (x <= 0) {
            x = Chunk.CHUNK_SIZE + x % Chunk.CHUNK_SIZE;
        }
        return x % Chunk.CHUNK_SIZE;
    }

    public final static int zInChunkPointer(Vector3f direction) {
        int z = (int) Math.floor(direction.z);
        if (z <= 0) {
            z = Chunk.CHUNK_SIZE + z % Chunk.CHUNK_SIZE;
        }
        return z % Chunk.CHUNK_SIZE;
    }

    public static int getNoise(float x, float z) {
        int noise;
        if (USE_SEED) {
            noise = (int) ((FastNoise.noise(x / (1f * TERRAIN_SMOOTHNESS * TERRAIN_SMOOTHNESS) + SEED, z / (1f * TERRAIN_SMOOTHNESS * TERRAIN_SMOOTHNESS) + SEED, 5)) * (Chunk.CHUNK_SIZE / 256f)) - 1;
        } else {
            noise = (int) (FastNoise.noise(x / (1f * TERRAIN_SMOOTHNESS * TERRAIN_SMOOTHNESS), z / (1f * TERRAIN_SMOOTHNESS * TERRAIN_SMOOTHNESS), 5) * ((float) (Chunk.VERTICAL_CHUNKS * Chunk.CHUNK_SIZE) / 256f)) - 1;
        }
        noise *= GROUND_SHARE;
        return noise;
    }

    public static int get3DNoise(float x, float y, float z) {
        int i = (int) ((SimplexNoise.noise(x / (1f * THREE_DIM_SMOOTHNESS * 2f), y / (1f * THREE_DIM_SMOOTHNESS), z / (1f * THREE_DIM_SMOOTHNESS * 2f)) + 1) * 128 * (Chunk.CHUNK_SIZE * Chunk.VERTICAL_CHUNKS / 256f));
        return i;
    }

    public static Texture loadTexture(String key) {
        InputStream resourceAsStream = Voxels.class.getClassLoader().getResourceAsStream("resources/textures/" + key + ".png");
        try {
            return TextureLoader.getTexture("png", resourceAsStream);

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
            Display.setTitle(TITLE + " - FPS: " + fps + " Chunk X: " + getCurrentChunkXId() + " Chunk Y: " + getCurrentChunkYId() + " Chunk Z: " + getCurrentChunkZId() + " Inside chunk: X: " + xInChunk() + " Y:" + yInChunk() + " Z: " + zInChunk());
            //Display.setTitle(TITLE + " - FPS: " + fps + " Pitch: " + camera.pitch() + " Yaw: " + camera.yaw());

            fps = 0; //reset the FPS counter
            lastFPS += 1000; //add one second
        }
        fps++;
    }
}
