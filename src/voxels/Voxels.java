package voxels;

import java.awt.Font;
import voxels.ChunkManager.Chunk;
import voxels.ChunkManager.ChunkManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
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
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT32;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.GL30.GL_MAX_RENDERBUFFER_SIZE;
import static org.lwjgl.opengl.GL30.GL_RENDERBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static org.lwjgl.opengl.GL30.glBindRenderbuffer;
import static org.lwjgl.opengl.GL30.glCheckFramebufferStatus;
import static org.lwjgl.opengl.GL30.glFramebufferRenderbuffer;
import static org.lwjgl.opengl.GL30.glGenFramebuffers;
import static org.lwjgl.opengl.GL30.glGenRenderbuffers;
import static org.lwjgl.opengl.GL30.glRenderbufferStorage;
import static org.lwjgl.util.glu.GLU.gluErrorString;
import org.newdawn.slick.Color;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.opengl.PNGDecoder;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import voxels.Camera.EulerCamera;
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
    public static final String ATLAS = "atlas";
    /**
     * Set terrain smoothness. Value of one gives mountains withs a width of one
     * block, 30 gives enormous flat areas. Default value is 15.
     */
    public static final int TERRAIN_SMOOTHNESS = 15;
    /**
     * Set player's height. One block's height is 1.
     */
    public static float PLAYER_HEIGHT = 2.2f;
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
    public static final boolean USE_3D_NOISE = false;

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
    public static int chunkCreationDistance = 1;
    public static int chunkRenderDistance = 12;
    private static Texture atlas;
    private static Texture bitMapFont;
    public static final float WaterOffs = 0.28f;
    public static float START_TIME;

    /**
     * The string that is rendered on-screen.
     */
    private static final StringBuilder renderString = new StringBuilder("Enter your text");
    /**
     * The texture object for the bitmap font.
     */
    private static int fontTexture;

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

        // Create a new texture for the bitmap font.
        fontTexture = glGenTextures();
        // Bind the texture object to the GL_TEXTURE_2D target, specifying that it will be a 2D texture.
        glBindTexture(GL_TEXTURE_2D, fontTexture);
        // Use TWL's utility classes to load the png file.
        PNGDecoder decoder = null;
        try {
            decoder = new PNGDecoder(new FileInputStream("res/font2.png"));
        } catch (IOException ex) {
            Logger.getLogger(Voxels.class.getName()).log(Level.SEVERE, null, ex);
        }
        ByteBuffer buffer = BufferUtils.createByteBuffer(4 * decoder.getWidth() * decoder.getHeight());
        try {
            decoder.decode(buffer, decoder.getWidth() * 4, PNGDecoder.RGBA);
        } catch (IOException ex) {
            Logger.getLogger(Voxels.class.getName()).log(Level.SEVERE, null, ex);
        }
        buffer.flip();
        // Load the previously loaded texture data into the texture object.
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, decoder.getWidth(), decoder.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE,
                buffer);
        // Unbind the texture.
        glBindTexture(GL_TEXTURE_2D, 0);

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
        chunkCreationDistance = 1;
        System.out.println("Time taken: " + (System.nanoTime() - time) / 1000000000 + " s.");

        chunkManager.getChunkLoader().loadChunks();
        chunkManager.getChunkLoader().start();

        while (!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            updateView();

            processInput(getDelta());

            chunkManager.checkChunkUpdates();

            render();
            renderString(renderString.toString(), fontTexture, 16, 0, 0, 30f, 23f);
            updateFPS();
            Display.update();
            Display.sync(60);
            //System.out.println("x: "+xInChunk()+" y: "+yInChunk()+" z: "+zInChunk());
            //System.out.println("ChunkX: " + getCurrentChunkXId() + " ChunkZ: " + getCurrentChunkZId());
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
        atlas.bind();
        for (int x = -chunkRenderDistance; x <= chunkRenderDistance; x++) {
            for (int z = -chunkRenderDistance; z <= chunkRenderDistance; z++) {
                for (int y = 0; y < 16; y++) {
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
                chunkManager.editBlock(Type.DIRT, xInChunk(), yInChunk(), zInChunk(), getCurrentChunkXId(), getCurrentChunkYId(), getCurrentChunkZId());
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
                chunkManager.editBlock(Type.AIR, xInChunk(), yInChunk() - 1, zInChunk(), getCurrentChunkXId(), getCurrentChunkYId(), getCurrentChunkZId());
            }

        }
        if (Keyboard.isKeyDown(Keyboard.KEY_U)) {
            glTranslatef(0, -200, 0);
        }

        if (Mouse.isGrabbed()) {
            camera.processMouse();
            camera.processKeyboard(delta, 1.4f);
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
        int x = (int) Math.floor(camera.x());
        if (x < 0)
            x -= Chunk.CHUNK_SIZE;
        return x / Chunk.CHUNK_SIZE;
    }

    public final static int getCurrentChunkZId() {
        int z = (int) Math.floor(camera.z());
        if (z < 0)
            z -= Chunk.CHUNK_SIZE;
        return z / Chunk.CHUNK_SIZE;
    }

    public final static int getCurrentChunkYId() {
        int y = (int) camera.y();
        return y / Chunk.CHUNK_SIZE;
    }

    public final static int yInChunk() {
        int y = (int) camera.y();
        return y % Chunk.CHUNK_SIZE;
    }

    public final static int xInChunk() {
        int x = (int) Math.floor(camera.x());
        if (x <= 0)
            x = Chunk.CHUNK_SIZE + x % Chunk.CHUNK_SIZE;
        return x % Chunk.CHUNK_SIZE;

    }

    public final static int zInChunk() {
        int z = (int) Math.floor(camera.z());
        if (z <= 0)
            z = Chunk.CHUNK_SIZE + z % Chunk.CHUNK_SIZE;
        return z % Chunk.CHUNK_SIZE;
    }

    public static int getNoise(float x, float z) {
        int noise;
        if (USE_SEED)
            noise = (int) ((FastNoise.noise(x / (1f * TERRAIN_SMOOTHNESS * TERRAIN_SMOOTHNESS) + SEED, z / (1f * TERRAIN_SMOOTHNESS * TERRAIN_SMOOTHNESS) + SEED, 5)) * (Chunk.CHUNK_SIZE / 256f)) - 1;
        else
            noise = (int) (FastNoise.noise(x / (1f * TERRAIN_SMOOTHNESS * TERRAIN_SMOOTHNESS), z / (1f * TERRAIN_SMOOTHNESS * TERRAIN_SMOOTHNESS), 5) * (Chunk.WORLD_HEIGHT * Chunk.CHUNK_SIZE / 256f)) - 1;

        return 63 + (int) (Math.random() * 2);//Math.max(noise, 0);
    }

    public static int get3DNoise(float x, float y, float z) {
        return (int) ((SimplexNoise.noise(x / (1f * TERRAIN_SMOOTHNESS * 2f), y / (1f * TERRAIN_SMOOTHNESS), z / (1f * TERRAIN_SMOOTHNESS * 2f)) + 1) * 128 * (Chunk.CHUNK_SIZE / 256f));
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

    /**
     * Renders text using a font bitmap.
     *
     * @param string the string to render
     * @param textureObject the texture object containing the font glyphs
     * @param gridSize the dimensions of the bitmap grid (e.g. 16 -> 16x16 grid;
     * 8 -> 8x8 grid)
     * @param x the x-coordinate of the bottom-left corner of where the string
     * starts rendering
     * @param y the y-coordinate of the bottom-left corner of where the string
     * starts rendering
     * @param characterWidth the width of the character
     * @param characterHeight the height of the character
     */
    private static void renderString(String string, int textureObject, int gridSize, float x, float y,
            float characterWidth, float characterHeight) {
        glPushAttrib(GL_TEXTURE_BIT | GL_ENABLE_BIT | GL_COLOR_BUFFER_BIT);
        glEnable(GL_CULL_FACE);
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureObject);
        // Enable linear texture filtering for smoothed results.
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        // Enable additive blending. This means that the colours will be added to already existing colours in the
        // frame buffer. In practice, this makes the black parts of the texture become invisible.
        glEnable(GL_BLEND);
        glBlendFunc(GL_ONE, GL_ONE);
        // Store the current model-view matrix.
        glPushMatrix();
        
        // Offset all subsequent (at least up until 'glPopMatrix') vertex coordinates.
        glTranslatef(-50, 250, -50);
        glBegin(GL_QUADS);
        // Iterate over all the characters in the string.
        for (int i = 0; i < string.length(); i++) {
            // Get the ASCII-code of the character by type-casting to integer.
            int asciiCode = (int) string.charAt(i);
            // There are 16 cells in a texture, and a texture coordinate ranges from 0.0 to 1.0.
            final float cellSize = 1.0f / gridSize;
            // The cell's x-coordinate is the greatest integer smaller than remainder of the ASCII-code divided by the
            // amount of cells on the x-axis, times the cell size.
            float cellX = ((int) asciiCode % gridSize) * cellSize;
            // The cell's y-coordinate is the greatest integer smaller than the ASCII-code divided by the amount of
            // cells on the y-axis.
            float cellY = ((int) asciiCode / gridSize) * cellSize;
            glTexCoord2f(cellX, cellY + cellSize);
            glVertex2f(i * characterWidth / 3, y);
            glTexCoord2f(cellX + cellSize, cellY + cellSize);
            glVertex2f(i * characterWidth / 3 + characterWidth / 2, y);
            glTexCoord2f(cellX + cellSize, cellY);
            glVertex2f(i * characterWidth / 3 + characterWidth / 2, y + characterHeight);
            glTexCoord2f(cellX, cellY);
            glVertex2f(i * characterWidth / 3, y + characterHeight);
        }
        glEnd();
        glPopMatrix();
        glPopAttrib();
    }
}
