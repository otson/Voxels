package voxels;

import Items.DebugInfo;
import Items.ItemHandler;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.Math.PI;
import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import kuusisto.tinysound.Sound;
import kuusisto.tinysound.TinySound;
import npc.Monster;
import npc.npcHandler;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.Color;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.TrueTypeFont;
import org.newdawn.slick.UnicodeFont;
import org.newdawn.slick.font.effects.ColorEffect;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import voxels.Camera.EulerCamera;
import voxels.ChunkManager.AtlasManager;
import voxels.ChunkManager.BlockCoord;
import voxels.ChunkManager.BlockRenders;
import voxels.ChunkManager.Chunk;
import static voxels.ChunkManager.Chunk.GROUND_SHARE;
import static voxels.ChunkManager.Chunk.WORLD_HEIGHT;
import voxels.ChunkManager.ChunkMaker;
import voxels.ChunkManager.ChunkManager;
import voxels.ChunkManager.Coordinates;
import voxels.ChunkManager.Handle;
import voxels.ChunkManager.ItemLocation;
import voxels.ChunkManager.Location;
import voxels.ChunkManager.Pair;
import voxels.ChunkManager.Type;
import voxels.ChunkManager.WaterHandler;
import voxels.Noise.FastNoise;
import voxels.Noise.SimplexNoise;
import voxels.Shaders.ShaderLoader;

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
    public static final int TERRAIN_SMOOTHNESS = 25;
    public static final int THREE_DIM_SMOOTHNESS = 50;
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
    public static int chunkCreationDistance = 2;
    public static int inGameCreationDistance = 9;
    public static int chunkRenderDistance = 8;
    public static final int DISPLAY_WIDTH = 1280;
    public static final int DISPLAY_HEIGHT = 800;
    public static Texture atlas;
    public static Sound running;
    public static Sound jumping;
    public static Sound impact;
    public static Sound runOnStone;
    public static Sound removeBlock;
    public static final float WaterOffs = 0.28f;
    public static float START_TIME;

    private static ChunkManager chunkManager;
    private static npcHandler npcManager;
    private static ItemHandler itemHandler;
    private static WaterHandler waterHandler;

    private static EulerCamera camera;
    private static float light0Position[] = {-2000.0f, 2000.0f, 1000.0f, 1.0f};

    private static int fps = 0;
    private static long lastFPS = getTime();
    public static int count = 0;
    private static int vertexCount;
    private static long lastFrame = System.nanoTime();
    private static BlockRenders blockRenders;
    private static boolean isDebug = true;

    private static int shaderProgram;

    static UnicodeFont font;

    public static void main(String[] args) {
        //testChunkSpeeds();
        initDisplay();
        initOpenGL();
        //initFog();
        initLighting();
//        initShaders();
//        initShaderLighting();
        initFont();
        initTextures();
        initRenders();

        initSounds();
        initManagers();
        gameLoop();
    }

    private static void initFont() {
        Font awtFont = new Font("Calibri", Font.PLAIN, 14); //name, style (PLAIN, BOLD, or ITALIC), size

        font = new UnicodeFont(awtFont.deriveFont(0, 16));

        font.addAsciiGlyphs();
        ColorEffect e = new ColorEffect();

        e.setColor(java.awt.Color.white);

        font.getEffects()
                .add(e);
        try {
            font.loadGlyphs();
        } catch (SlickException e1) {
            e1.printStackTrace();
        }
    }

    private static void testChunkSpeeds() {

        chunkManager = new ChunkManager();

        HashMap<Integer, Chunk> hashMap = new HashMap<>();
        ChunkMaker maker = new ChunkMaker(null, null, chunkManager, null, null);
        long start = System.nanoTime();
        for (int i = 0; i < 5000; i++) {
            Chunk chunk = new Chunk(i, i, i);
            chunkManager.getMap().put(new Pair(i, i, i).hashCode(), maker.toByte(chunk));
        }
        System.out.println("Putting chunks took: " + (System.nanoTime() - start) / 1000000 + " ms.");

        start = System.nanoTime();
        for (int i = 0; i < 5000; i++) {
            Chunk chunk = chunkManager.getChunk(i, i, i);
            hashMap.put(new Pair(i, i, i).hashCode(), chunk); //17500 ms (no initial capacity)
        }
        System.out.println("Getting chunks took: " + (System.nanoTime() - start) / 1000000 + " ms.");

        start = System.nanoTime();
        for (int i = 0; i < 5000; i++) {
            Chunk chunk = hashMap.get(new Pair(i, i, i).hashCode());
            if (chunk == null) {
                System.out.println("null");
            }
            //hashMap.put(new Pair(i, i, i).hashCode(), chunk); //17500 ms (no initial capacity)
        }
        System.out.println("Getting chunks from uncompressed took: " + (System.nanoTime() - start) / 1000000 + " ms.");
        while (true) {

        }
        //System.exit(0);
    }

    private static void initDisplay() {
        try {
            DisplayMode displayMode = null;
//            DisplayMode[] modes = Display.getAvailableDisplayModes();
//
//            for (int i = 0; i < modes.length; i++) {
//                if (modes[i].getWidth() == DISPLAY_WIDTH
//                        && modes[i].getHeight() == DISPLAY_HEIGHT
//                        && modes[i].isFullscreenCapable()) {
//                    displayMode = modes[i];
//                }
//            }

            Display.setDisplayMode(new DisplayMode(DISPLAY_WIDTH, DISPLAY_HEIGHT));
            Display.setFullscreen(true);
            Display.setVSyncEnabled(true);
            Display.setTitle("Voxels");
            Display.create();
        } catch (LWJGLException e) {
            e.printStackTrace();
            Display.destroy();
            System.exit(1);
        }
    }

    private static void initManagers() {

        chunkManager = new ChunkManager();
        waterHandler = new WaterHandler(chunkManager);
        camera = InitCamera();
        itemHandler = new ItemHandler(chunkManager);
        npcManager = new npcHandler(chunkManager, camera);
        for (int i = 0; i < 100; i++) {
            npcManager.addNPC((float) (500f * Math.random() - 250f), (float) (150f * Math.random() + 100f), (float) (500f * Math.random() - 250f), chunkManager);
        }
    }

    private static void initShaders2() {
        shaderProgram = ShaderLoader.loadShaderPair("src/resources/shaders/vertex_phong_lighting.vs", "src/resources/shaders/vertex_phong_lighting.fs");
    }

    private static void initShaders() {
        shaderProgram = glCreateProgram();
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        StringBuilder vertexShaderSource = new StringBuilder();
        StringBuilder fragmentShaderSource = new StringBuilder();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader("src/resources/shaders/shader.vs"));
            String line;
            while ((line = reader.readLine()) != null) {
                vertexShaderSource.append(line).append('\n');
            }
        } catch (IOException e) {
            System.err.println("Vertex shader wasn't loaded properly.");
            e.printStackTrace();
            Display.destroy();
            System.exit(1);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        BufferedReader reader2 = null;
        try {
            reader2 = new BufferedReader(new FileReader("src/resources/shaders/shader.fs"));
            String line;
            while ((line = reader2.readLine()) != null) {
                fragmentShaderSource.append(line).append('\n');
            }
        } catch (IOException e) {
            System.err.println("Fragment shader wasn't loaded properly.");
            Display.destroy();
            System.exit(1);
        } finally {
            if (reader2 != null) {
                try {
                    reader2.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);
        if (glGetShaderi(vertexShader, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Vertex shader wasn't able to be compiled correctly.");
        }
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);
        if (glGetShaderi(fragmentShader, GL_COMPILE_STATUS) == GL_FALSE) {
            System.err.println("Fragment shader wasn't able to be compiled correctly.");
        }
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);
        glValidateProgram(shaderProgram);
    }

    private static void initOpenGL() {
        glMatrixMode(GL_PROJECTION);
        glMatrixMode(GL_MODELVIEW);

        glLoadIdentity();

    }

    private static void initFog() {
        glEnable(GL_FOG);
        glFog(GL_FOG_COLOR, asFloatBuffer(new float[]{0.65f, 0.65f, 0.85f, 1f}));
        //glFog(GL_FOG_COLOR, asFloatBuffer(new float[]{0f / 255f, 0f / 255f, 190f / 255f, 1.0f}));

        glFogi(GL_FOG_MODE, GL_LINEAR);
        glFogf(GL_FOG_START, (float) (0.1 * Chunk.CHUNK_SIZE * Voxels.chunkRenderDistance));
        glFogf(GL_FOG_END, Chunk.CHUNK_SIZE * Voxels.chunkRenderDistance * 2);
    }

    public static void initSounds() {

        TinySound.init();

        running
                = TinySound.loadSound(Voxels.class
                        .getClassLoader().getResource("resources/sounds/walk2.wav"));
        jumping = TinySound.loadSound(Voxels.class.getClassLoader().getResource("resources/sounds/jump.wav"));
        impact = TinySound.loadSound(Voxels.class.getClassLoader().getResource("resources/sounds/impact.wav"));
        runOnStone = TinySound.loadSound(Voxels.class.getClassLoader().getResource("resources/sounds/walkOnStone.wav"));
        removeBlock = TinySound.loadSound(Voxels.class.getClassLoader().getResource("resources/sounds/removeBlock.wav"));

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

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
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

    private static void initShaderLighting() {

        glMaterialf(GL_FRONT, GL_SHININESS, 120);
        glShadeModel(GL_SMOOTH);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        glLightModel(GL_LIGHT_MODEL_AMBIENT, asFloatBuffer(new float[]{0.05f, 0.05f, 0.05f, 1f}));
        glLight(GL_LIGHT0, GL_POSITION, asFloatBuffer(light0Position));
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glEnable(GL_COLOR_MATERIAL);
        glColorMaterial(GL_FRONT, GL_DIFFUSE);
        //glColorMaterial(GL_FRONT, GL_SPECULAR);
//        glShadeModel(GL_SMOOTH);
//        glEnable(GL_DEPTH_TEST);
//        glEnable(GL_LIGHTING);
//        glEnable(GL_LIGHT0);
////        glLightModel(GL_LIGHT_MODEL_AMBIENT, BufferTools.asFlippedFloatBuffer(new float[]{0.05f, 0.05f, 0.05f, 1f}));
////        glLight(GL_LIGHT0, GL_POSITION, BufferTools.asFlippedFloatBuffer(new float[]{0, 0, 0, 1}));
//        glLightModel(GL_LIGHT_MODEL_AMBIENT, asFloatBuffer(new float[]{0.05f, 0.05f, 0.05f, 1f}));
//        glLight(GL_LIGHT0, GL_POSITION, asFloatBuffer(light0Position));
//        glEnable(GL_CULL_FACE);
//        glCullFace(GL_BACK);
//        glEnable(GL_COLOR_MATERIAL);
//        glColorMaterial(GL_FRONT, GL_DIFFUSE);
//        
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

        chunkManager.startGeneration();
        blockRenders = new BlockRenders();
        long time = System.nanoTime();

        while (chunkManager.isAtMax() == false) {
            chunkManager.checkChunkUpdates();
            if (chunkManager.chunkAmount() % 20 == 0) {
                Display.update();
            }
        }
        System.out.println("Chunks created in " + (System.nanoTime() - time) / 1000000 + " ms.");
        //time = System.nanoTime();
        //chunkManager.createVBOs();
        //System.out.println("VBOs created in " + (System.nanoTime() - time) / 1000000000 + " seconds.");
        chunkManager.getChunkLoader().updateLocation();
        chunkManager.getChunkLoader().loadChunks();
        chunkManager.getChunkLoader().start();
        chunkManager.startChunkRenderChecker();
        chunkCreationDistance = inGameCreationDistance;
        Thread thread = new Thread(
                new Runnable() {
                    public void run() {
                        while (true) {
                            for (int i = 0; i < ChunkManager.maxThreads; i++) {
                                chunkManager.checkChunkUpdates();
                            }
                            try {
                                Thread.sleep(10);

                            } catch (InterruptedException ex) {
                                Logger.getLogger(Voxels.class
                                        .getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
        );
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();

        while (!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            updateView();
            processInput(getDelta());
            //if (fps % 10 == 0) {
                //waterHandler.simulateWaters();
            //}
            chunkManager.processBufferData();
            npcManager.processMonsters();
            itemHandler.processItemPhysics();
            glUseProgram(shaderProgram);
            render();
            glUseProgram(0);
            renderDebugText();
            updateFPS();
            Display.update();
            Display.sync(60);
            //System.out.println("Chunks: "+chunkManager.getTotalChunks());
        }
        glDeleteProgram(shaderProgram);
        Display.destroy();
        TinySound.shutdown();

        System.exit(0);
    }

    private static void updateView() {
        glLoadIdentity();

        glViewport(0, 0, Display.getWidth(), Display.getHeight());
        if (camera.isZoomed()) {
            camera.applyPerspectiveMatrix(15);
        } else {
            camera.applyPerspectiveMatrix();
        }
        camera.applyTranslations();

    }

    private static void render() {

        vertexCount = 0;
        int activeChunks = 0;
        for (int x = -chunkRenderDistance; x <= chunkRenderDistance; x++) {
            for (int z = -chunkRenderDistance; z <= chunkRenderDistance; z++) {
                for (int y = 0; y < Chunk.VERTICAL_CHUNKS; y++) {
                    Handle handles = chunkManager.getHandle(getCurrentChunkXId() + x, y, getCurrentChunkZId() + z);
                    if (handles != null) {
                        activeChunks++;
                        int vboVertexHandle = handles.vertexHandle;
                        int vboNormalHandle = handles.normalHandle;
                        int vboTexHandle = handles.texHandle;
                        int vertices = handles.vertices;
                        vertexCount += vertices;

                        glBindBuffer(GL_ARRAY_BUFFER, vboVertexHandle);
                        glVertexPointer(3, GL_FLOAT, 0, 0L);

                        glBindBuffer(GL_ARRAY_BUFFER, vboNormalHandle);
                        glNormalPointer(GL_FLOAT, 0, 0L);

                        glBindBuffer(GL_ARRAY_BUFFER, vboTexHandle);
                        glTexCoordPointer(2, GL_FLOAT, 0, 0L);

                        glEnableClientState(GL_VERTEX_ARRAY);
                        glEnableClientState(GL_NORMAL_ARRAY);
                        glEnableClientState(GL_TEXTURE_COORD_ARRAY);
                        glDrawArrays(GL_QUADS, 0, vertices);
                        glDisableClientState(GL_TEXTURE_COORD_ARRAY);
                        glDisableClientState(GL_NORMAL_ARRAY);
                        glDisableClientState(GL_VERTEX_ARRAY);

                        glBindBuffer(GL_ARRAY_BUFFER, 0);
                    }
                }
            }
        }
        //glDisable(GL_CULL_FACE);
        int npcCount = 0;
        for (Monster npc : npcManager.getMonsterList().values()) {
            vertexCount += 24;
            npcCount++;
            //glLoadIdentity();
            glTranslatef(npc.getX(), npc.getY(), npc.getZ());
            int vertices = 24;
            //System.out.println("handle: "+npc.getHandle());

            glBindBuffer(GL_ARRAY_BUFFER, npc.getVertexHandle());
            glVertexPointer(3, GL_FLOAT, 0, 0L);
            glBindBuffer(GL_ARRAY_BUFFER, npc.getNormalHandle());
            glNormalPointer(GL_FLOAT, 0, 0L);
            glBindBuffer(GL_ARRAY_BUFFER, npc.getColorHandle());
            glColorPointer(3, GL_FLOAT, 0, 0L);

            glEnableClientState(GL_VERTEX_ARRAY);
            glEnableClientState(GL_NORMAL_ARRAY);
            glEnableClientState(GL_COLOR_ARRAY);
            glDrawArrays(GL_QUADS, 0, vertices);
            glDisableClientState(GL_COLOR_ARRAY);
            glDisableClientState(GL_NORMAL_ARRAY);
            glDisableClientState(GL_VERTEX_ARRAY);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glTranslatef(-npc.getX(), -npc.getY(), -npc.getZ());
        }
        glScalef(0.5f, 0.5f, 0.5f);
        int activeItems = 0;
        for (ItemLocation item : itemHandler.getDroppedBlocks()) {
            activeItems++;
            vertexCount += 24;
            glTranslatef(item.x * 2, item.y * 2, item.z * 2);
            glRotatef(item.rotY, 0, 1, 0);
            int vertices = 24;

            Handle handles = blockRenders.getHandle(item.type);

            glBindBuffer(GL_ARRAY_BUFFER, handles.vertexHandle);
            glVertexPointer(3, GL_FLOAT, 0, 0L);
            glBindBuffer(GL_ARRAY_BUFFER, handles.normalHandle);
            glNormalPointer(GL_FLOAT, 0, 0L);
            glBindBuffer(GL_ARRAY_BUFFER, handles.texHandle);
            glTexCoordPointer(2, GL_FLOAT, 0, 0L);

            glEnableClientState(GL_VERTEX_ARRAY);
            glEnableClientState(GL_NORMAL_ARRAY);
            glEnableClientState(GL_TEXTURE_COORD_ARRAY);
            glDrawArrays(GL_QUADS, 0, vertices);
            glDisableClientState(GL_TEXTURE_COORD_ARRAY);
            glDisableClientState(GL_NORMAL_ARRAY);
            glDisableClientState(GL_VERTEX_ARRAY);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glRotatef(-item.rotY, 0, 1, 0);
            glTranslatef(-item.x * 2, -item.y * 2, -item.z * 2);

        }
        DebugInfo.activeItems = activeItems;
        DebugInfo.verticesDrawn = vertexCount;
        DebugInfo.activeNPCs = npcCount;
        DebugInfo.chunksLoaded = activeChunks;
        DebugInfo.chunkTotal = chunkManager.getHandles().size();
    }

    private static void renderDebugText() {
        if (isDebug) {
            DecimalFormat df = new DecimalFormat();
            df.setMaximumFractionDigits(0);
            glLoadIdentity();
            glDisable(GL_LIGHTING);
            glDisable(GL_LIGHT0);
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(0, DISPLAY_WIDTH, DISPLAY_HEIGHT, 0, -1, 1);
            glMatrixMode(GL_MODELVIEW);
            glDisable(GL_TEXTURE_2D);
            font.drawString(5, 5, "Player world coordinates: " + df.format(camera.x()) + " " + df.format(camera.y()) + " " + df.format(camera.z()));
            font.drawString(5, 25, "Player chunk coordinates: " + getChunkX() + " " + getChunkY() + " " + getChunkZ());
            font.drawString(5, 45, "Player in-chunk coordinates: " + getX() + " " + getY() + " " + getZ());
            font.drawString(5, 65, "Player rotation: " + df.format(camera.pitch()) + " " + df.format(camera.roll()) + " " + df.format(camera.yaw()));
            font.drawString(5, 85, "Active chunks (total chunks): " + DebugInfo.chunksLoaded + " (" + DebugInfo.chunkTotal + ")");
            font.drawString(5, 105, "Vertices: " + DebugInfo.verticesDrawn);
            font.drawString(5, 125, "NPCs: " + DebugInfo.activeNPCs);
            font.drawString(5, 145, "Items: " + DebugInfo.activeItems);
            font.drawString(5, 165, "Draw distance (chunks): " + chunkRenderDistance);
            font.drawString(5, 185, "Frames per Second: " + DebugInfo.fps);
            //font.drawString(5, 205, "GPU memory: " + (DebugInfo.get_video_card_used_memory()/1024)+" MB / "+(DebugInfo.get_video_card_total_memory()/1024)+" MB");
            glEnable(GL_TEXTURE_2D);
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(camera.x(), camera.x() + DISPLAY_WIDTH, camera.y() + DISPLAY_HEIGHT, camera.y(), -1, 1);
            glMatrixMode(GL_MODELVIEW);
            glEnable(GL_LIGHTING);
            glEnable(GL_LIGHT0);
            glLoadIdentity();
            atlas.bind();
        }

    }

    private static void drawAimLine() {
        glDisable(GL_TEXTURE_2D);
        Vector3f direction = getDirectionVector(1f);
        glColor3f(1f, 0, 0);
        glPointSize(1);
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

            if (Keyboard.isKeyDown(Keyboard.KEY_X)) {
                chunkManager.bigRemove();
            }

            if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
                camera.toggleFlight();
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_B)) {
                chunkManager.getChunkLoader().loadChunks();
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_F9)) {
                chunkCreationDistance++;
                chunkRenderDistance++;
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_F8)) {
                if (chunkCreationDistance > 2 && chunkRenderDistance > 1) {
                    chunkCreationDistance--;
                    chunkRenderDistance--;
                }
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_B)) {
                chunkManager.getChunkLoader().loadChunks();
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_M)) {
                npcManager.toggle();
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_F5)) {
                isDebug = !isDebug;
            }
        }

        if (Mouse.isGrabbed()) {
            while (Mouse.next()) {
                if (Mouse.getEventButtonState()) {
                    if (Mouse.getEventButton() == 0) {
                        chunkManager.castRay(Type.WATER10);
                    } else if (Mouse.getEventButton() == 1) {
                        chunkManager.castRay(Type.AIR);
                    }
                }
            }
            camera.processMouse();
            camera.processKeyboard(delta, 1.4f);
//            if (NIGHT_CYCLE) {
//                glClearColor(0f, (float) Math.max(0, (255 * Math.sin(timePassed() / 20000)) - 155) / 255f, (float) Math.max(25, (255 * Math.sin(timePassed() / 20000)) + 25) / 255f, 1.0f);
//                glLight(GL_LIGHT0, GL_POSITION, asFloatBuffer(new float[]{2500 + camera.x(), (float) (10000 * Math.sin(timePassed() / 20000)), (float) (10000 * Math.cos(timePassed() / 20000)), 1f}));
//            } else {
            glLight(GL_LIGHT0, GL_POSITION, asFloatBuffer(light0Position));
//            }
        }
    }

    private static void updateView(int fov) {
        glLoadIdentity();
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
        camera.applyPerspectiveMatrix(fov);
        camera.applyTranslations();
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
    /*
     Methods to convert float coordinate values to
     coordinates inside a chunk (0 - Chunksize-1)
     */

    public final static int getX() {
        return getX(camera.x());
    }

    public final static int getX(float x) {
        int value = (x >= 0) ? (int) x : (int) (x - 1);
        if (x < 0) {
            value = Chunk.CHUNK_SIZE - (-value) % Chunk.CHUNK_SIZE;
        }
        return value % Chunk.CHUNK_SIZE;
    }

    public final static int getY() {
        return getY(camera.y());
    }

    public final static int getY(float y) {
        int value = (y >= 0) ? (int) y : (int) (y - 1);
        if (y < 0) {
            value = Chunk.CHUNK_SIZE - (-value) % Chunk.CHUNK_SIZE;
        }
        return value % Chunk.CHUNK_SIZE;
    }

    public final static int getZ() {
        return getZ(camera.z());
    }

    public final static int getZ(float z) {
        int value = (z >= 0) ? (int) z : (int) (z - 1);
        if (z < 0) {
            value = Chunk.CHUNK_SIZE - (-value) % Chunk.CHUNK_SIZE;
        }
        return value % Chunk.CHUNK_SIZE;
    }

    /*
     Methods to convert float coordinate values to
     Chunk ID coordinates
     */
    public final static int getChunkX() {
        return getChunkX(camera.x());
    }

    public final static int getChunkX(float x) {
        int value = (x >= 0) ? (int) x : (int) (x - 1);
        if (x < 0) {
            value -= Chunk.CHUNK_SIZE - 1;

        }
        return value / Chunk.CHUNK_SIZE;
    }

    public final static int getChunkY() {
        return getChunkY(camera.y());
    }

    public final static int getChunkY(float y) {
        int value = (y >= 0) ? (int) y : (int) (y - 1);
        if (y < 0) {
            value -= Chunk.CHUNK_SIZE - 1;
        }
        return value / Chunk.CHUNK_SIZE;
    }

    public final static int getChunkZ() {
        return getChunkZ(camera.z());
    }

    public final static int getChunkZ(float z) {
        int value = (z >= 0) ? (int) z : (int) (z - 1);
        if (z < 0) {
            value -= Chunk.CHUNK_SIZE - 1;
        }
        return value / Chunk.CHUNK_SIZE;
    }

    public final static int toZid(int z) {
        if (z < 0) {
            z -= Chunk.CHUNK_SIZE - 1;
        }
        return z / Chunk.CHUNK_SIZE;
    }

    public final static int toXid(int x) {
        if (x < 0) {
            x -= Chunk.CHUNK_SIZE - 1;
        }
        return x / Chunk.CHUNK_SIZE;
    }

    public final static int toYid(int y) {
        return y / Chunk.CHUNK_SIZE;
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

    public static boolean getCaveNoise(float x, float y, float z) {
        float noise1 = get3DNoise(x, y, z) / (float) (Chunk.CHUNK_SIZE * Chunk.VERTICAL_CHUNKS);
        float noise2 = get3DNoise(x + 10000, y + 10000, z + 10000) / (float) (Chunk.CHUNK_SIZE * Chunk.VERTICAL_CHUNKS);

        return noise1 > Chunk.noiseOneMin && noise1 < Chunk.noiseOneMax && noise2 > Chunk.noiseTwoMin && noise2 < Chunk.noiseTwoMax;
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

    public static int toX(int x) {
        if (x <= 0) {
            x = Chunk.CHUNK_SIZE + x % Chunk.CHUNK_SIZE;
        }
        return x % Chunk.CHUNK_SIZE;
    }

    public static int toY(int y) {
        return y % Chunk.CHUNK_SIZE;
    }

    public static int toZ(int z) {
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
            noise = (int) ((FastNoise.noise(x / (1f * TERRAIN_SMOOTHNESS * TERRAIN_SMOOTHNESS) + 1000000, z / (1f * TERRAIN_SMOOTHNESS * TERRAIN_SMOOTHNESS) + 1000000, 5)) * (Chunk.CHUNK_SIZE / 255f)) - 1;
        } else {
            noise = (int) (FastNoise.noise(x / (1f * TERRAIN_SMOOTHNESS * TERRAIN_SMOOTHNESS), z / (1f * TERRAIN_SMOOTHNESS * TERRAIN_SMOOTHNESS), 5) * ((float) (Chunk.VERTICAL_CHUNKS * Chunk.CHUNK_SIZE) / 255f)) - 1;
        }
        noise *= GROUND_SHARE;
        return noise;
    }

    public static int getTreeNoise(float x, float y, float z) {
        if (FastNoise.noise(x / 100f, z / 100f, 3) > 100f) {

            int noise = (int) (FastNoise.noise(x + 1000, z + 1000, 3));
            if (noise == 10 || noise == 50) {
                if (getCaveNoise(x, y, z) == false) {
                    return 0;
                } else {
                    return 1;
                }
            } else if (noise == 31) {
                return 1;
            } else {
                return -1;
            }
        }
        return -1;

    }

    public static int get3DNoise(float x, float y, float z) {
        int i = (int) ((SimplexNoise.noise(x / (1f * THREE_DIM_SMOOTHNESS * 2f), y / (1f * THREE_DIM_SMOOTHNESS), z / (1f * THREE_DIM_SMOOTHNESS * 2f)) + 1) * 128 * (Chunk.CHUNK_SIZE * Chunk.VERTICAL_CHUNKS / 256f));
        return i;
    }

    public static Texture
            loadTexture(String key) {
        InputStream resourceAsStream = Voxels.class
                .getClassLoader().getResourceAsStream("resources/textures/" + key + ".png");

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
            Display.setTitle(TITLE);
            //Display.setTitle(TITLE + " - FPS: " + fps + " Pitch: " + camera.pitch() + " Yaw: " + camera.yaw());
            DebugInfo.fps = fps;
            fps = 0; //reset the FPS counter
            lastFPS += 1000; //add one second
        }
        fps++;

    }

    public static int toWorldX(float x) {
        return (x >= 0) ? (int) x : (int) (x - 1);
    }

    public static int toWorldY(float y) {
        return (y >= 0) ? (int) y : (int) (y - 1);
    }

    public static int toWorldZ(float z) {
        return (z >= 0) ? (int) z : (int) (z - 1);
    }

    public static void putToBuffer(byte type, int x, int y, int z) {
        int chunkXId = toXid(x);
        int chunkYId = toYid(y);
        int chunkZId = toZid(z);

        int xInChunk = toX(x);
        int yInChunk = toY(y);
        int zInChunk = toZ(z);

        if (!chunkManager.getBlockBuffer().containsKey(new Pair(chunkXId, chunkYId, chunkZId).hashCode())) {
            BlockingQueue<BlockCoord> queue = new LinkedBlockingQueue<>();
            queue.offer(new BlockCoord(type, xInChunk, yInChunk, zInChunk));
            chunkManager.getBlockBuffer().put(new Pair(chunkXId, chunkYId, chunkZId).hashCode(), queue);
        } else {
            BlockingQueue queue = chunkManager.getBlockBuffer().get(new Pair(chunkXId, chunkYId, chunkZId).hashCode());
            queue.offer(new BlockCoord(type, xInChunk, yInChunk, zInChunk));
            chunkManager.getBlockBuffer().put(new Pair(chunkXId, chunkYId, chunkZId).hashCode(), queue);
        }

        //System.out.println("Buffer size: "+chunkManager.getBlockBuffer().size());
    }

    public static ConcurrentHashMap<Integer, BlockingQueue> getBlockBuffer() {
        return chunkManager.getBlockBuffer();
    }

    public static Location getPlayerLocation() {
        return new Location(camera.x(), camera.y(), camera.z());
    }

    private static void initRenders() {

    }

    public static byte getTypeNoise(int x, int z) {
        double noise = FastNoise.noise(x/1000f+1000, z/1000f,7)/255f;
        if(noise < 0.35f)
            return Type.SAND;
        if(noise < 0.40f)
            return Type.ROCKSAND;
//        if(noise > 0.65f)
//            return Type.SNOW;
//        if(noise > 0.60f)
//            return Type.ROCKSAND; 
        else
            return Type.DIRT;
    }

}
