package voxels;

import java.io.BufferedReader;
import java.io.FileReader;
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
import static org.lwjgl.opengl.ARBFramebufferObject.GL_DEPTH_ATTACHMENT;
import static org.lwjgl.opengl.ARBFramebufferObject.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.ARBFramebufferObject.GL_FRAMEBUFFER_COMPLETE;
import static org.lwjgl.opengl.ARBFramebufferObject.GL_MAX_RENDERBUFFER_SIZE;
import static org.lwjgl.opengl.ARBFramebufferObject.GL_RENDERBUFFER;
import static org.lwjgl.opengl.ARBFramebufferObject.glBindFramebuffer;
import static org.lwjgl.opengl.ARBFramebufferObject.glBindRenderbuffer;
import static org.lwjgl.opengl.ARBFramebufferObject.glCheckFramebufferStatus;
import static org.lwjgl.opengl.ARBFramebufferObject.glFramebufferRenderbuffer;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenFramebuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenFramebuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenFramebuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenFramebuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenFramebuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenFramebuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenFramebuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenFramebuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenFramebuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenFramebuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenFramebuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenFramebuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenFramebuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenFramebuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenFramebuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenFramebuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenFramebuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenFramebuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenRenderbuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenRenderbuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenRenderbuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenRenderbuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenRenderbuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenRenderbuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenRenderbuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenRenderbuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenRenderbuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenRenderbuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenRenderbuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenRenderbuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenRenderbuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenRenderbuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenRenderbuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glGenRenderbuffers;
import static org.lwjgl.opengl.ARBFramebufferObject.glRenderbufferStorage;
import static org.lwjgl.opengl.ARBShadowAmbient.GL_TEXTURE_COMPARE_FAIL_VALUE_ARB;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT32;
import static org.lwjgl.opengl.GL14.GL_DEPTH_TEXTURE_MODE;

import static org.lwjgl.opengl.GL15.*;

import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glValidateProgram;
import static org.lwjgl.util.glu.GLU.gluErrorString;
import static org.lwjgl.util.glu.GLU.gluLookAt;
import static org.lwjgl.util.glu.GLU.gluPerspective;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import voxels.Camera.EulerCamera;
import voxels.ChunkManager.Chunk;
import static voxels.ChunkManager.Chunk.GROUND_SHARE;
import static voxels.ChunkManager.Chunk.WORLD_HEIGHT;
import voxels.ChunkManager.ChunkManager;
import voxels.ChunkManager.Handle;
import voxels.ChunkManager.Type;
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
    public static final int TERRAIN_SMOOTHNESS = 12;
    public static final int THREE_DIM_SMOOTHNESS = 40;
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
    public static int chunkCreationDistance = 0;
    public static int inGameCreationDistance = 11;
    public static int chunkRenderDistance = 10;
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
    
    // shaders
    
    
    private static int vertexShader;
    private static int fragmentShader;
    private static StringBuilder vertexShaderSource;
    private static StringBuilder fragmentShaderSource;
    private static BufferedReader reader = null;
    
    private static final String VERTEX_SHADER_LOCATION = "src/resources/shaders/vertex_phong_lighting.vs";
    private static final String FRAGMENT_SHADER_LOCATION = "src/resources/shaders/vertex_phong_lighting.fs";
    private static int shaderProgram;
    
    /** The position of the omnidirectional shadow-casting light. */
    private static final FloatBuffer lightPosition = asFloatBuffer(new float[]{-2000.0f, 50000.0f, 8000.0f, 1.0f});
    private static final FloatBuffer textureBuffer = BufferUtils.createFloatBuffer(16);
    private static final Matrix4f depthModelViewProjection = new Matrix4f();
    private static final DisplayMode DISPLAY_MODE = new DisplayMode(800, 600);
    /**
     * The width of the depth texture that is known as the shadow map. The higher the width, the more detailed the
     * shadows.
     */
    private static int shadowMapWidth;
    /**
     * The height of the depth texture that is known as the shadow map. The higher the height, the more detailed the
     * shadows.
     */
    private static int shadowMapHeight;
    /**
     * The frame buffer holding the shadow map. This frame buffer is an off-screen rendering location to which we will
     * draw the shadow map.
     */
    private static int frameBuffer;
    /** The render buffer holding the shadow map in the form of a depth texture. */
    private static int renderBuffer;

    public static void main(String[] args) {
        initDisplay();
        initOpenGL();
        initFog();
        initLighting();
        initTextures();
        initSounds();
        setUpFrameBufferObject();
        
        
        gameLoop();
        
    }
    
    /** Sets up the OpenGL states. */
    private static void setUpFrameBufferObject() {
        
        // State that the texture holds nondescript 'intensity' data.
        glTexParameteri(GL_TEXTURE_2D, GL_DEPTH_TEXTURE_MODE, GL_INTENSITY);
        // If the intensity of a given texel is lower than 0.5f, then the texture should not be sampled. In practice,
        // the higher the value, the less of the shadow is visible, and the other way around.
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FAIL_VALUE_ARB, 0.5F);
        // Set the automatic texture coordinate generation mode to eye linear. The texture coordinate is calculated
        // with the inverse of the model-view matrix and a plane that we will specify later on.
        glTexGeni(GL_S, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
        glTexGeni(GL_T, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
        glTexGeni(GL_R, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
        glTexGeni(GL_Q, GL_TEXTURE_GEN_MODE, GL_EYE_LINEAR);
        
        final int MAX_RENDERBUFFER_SIZE = glGetInteger(GL_MAX_RENDERBUFFER_SIZE);
        final int MAX_TEXTURE_SIZE = glGetInteger(GL_MAX_TEXTURE_SIZE);
        /**
         * Cap the maximum shadow map size at 1024x1024 pixels or at the maximum render buffer size. If you have a good
         * graphics card, feel free to increase this value. The program will lag
         * if I record and run the program at the same time with higher values.
         */
        if (MAX_TEXTURE_SIZE > 1024) {
            if (MAX_RENDERBUFFER_SIZE < MAX_TEXTURE_SIZE) {
                shadowMapWidth = shadowMapHeight = MAX_RENDERBUFFER_SIZE;
            } else {
                shadowMapWidth = shadowMapHeight = 1024;
            }
        } else {
            shadowMapWidth = shadowMapHeight = MAX_TEXTURE_SIZE;
        }
        // Generate and bind a frame buffer.
        frameBuffer = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
        // Generate and bind a render buffer.
        renderBuffer = glGenRenderbuffers();
        glBindRenderbuffer(GL_RENDERBUFFER, renderBuffer);
        // Set the internal storage format of the render buffer to a depth component of 32 bits (4 bytes).
        glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT32, shadowMapWidth, shadowMapHeight);
        // Attach the render buffer to the frame buffer as a depth attachment. This means that, if the frame buffer is
        // bound, any depth texture values will be copied to the render buffer object.
        glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, renderBuffer);
        // OpenGL shall make no amendment to the colour or multisample buffer.
        glDrawBuffer(GL_NONE);
        // Disable the colour buffer for pixel read operations (such as glReadPixels or glCopyTexImage2D).
        glReadBuffer(GL_NONE);
        // Check for frame buffer errors.
        int FBOStatus = glCheckFramebufferStatus(GL_FRAMEBUFFER);
        if (FBOStatus != GL_FRAMEBUFFER_COMPLETE) {
            System.err.println("Framebuffer error: " + gluErrorString(glGetError()));
        }
        // Bind the default frame buffer, which is used for ordinary drawing.
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
    
    /** Generate the shadow map. */
    private static void drawShadowMap() {
        /**
         * The model-view matrix of the light.
         */
        FloatBuffer lightModelView = BufferUtils.createFloatBuffer(16);
        /**
         * The projection matrix of the light.
         */
        FloatBuffer lightProjection = BufferUtils.createFloatBuffer(16);
        Matrix4f lightProjectionTemp = new Matrix4f();
        Matrix4f lightModelViewTemp = new Matrix4f();
        /**
         * The radius that encompasses all the objects that cast shadows in the scene. There should
         * be no object farther away than 50 units from [0, 0, 0] in any direction.
         * If an object exceeds the radius, the object may cast shadows wrongly.
         */
        float sceneBoundingRadius = 50;
        /**
         * The distance from the light to the scene, assuming that the scene is located
         * at [0, 0, 0]. Using the Pythagorean theorem, the distance is calculated by taking the square-root of the
         * sum of each of the components of the light position squared.
         */
        float lightToSceneDistance = (float) Math.sqrt(lightPosition.get(0) * lightPosition.get(0) +
                lightPosition.get(1) * lightPosition.get(1) +
                lightPosition.get(2) * lightPosition.get(2));
        /**
         * The distance to the object that is nearest to the camera. This excludes objects that do not cast shadows.
         * This will be used as the zNear parameter in gluPerspective.
         */
        float nearPlane = lightToSceneDistance - sceneBoundingRadius;
        if (nearPlane < 0) {
            System.err.println("Camera is too close to scene. A valid shadow map cannot be generated.");
        }
        /**
         * The field-of-view of the shadow frustum in degrees. Formula taken from the OpenGL SuperBible.
         */
        float fieldOfView = (float) Math.toDegrees(2.0F * Math.atan(sceneBoundingRadius / lightToSceneDistance));
        glMatrixMode(GL_PROJECTION);
        // Store the current projection matrix.
        glPushMatrix();
        glLoadIdentity();
        // Generate the 'shadow frustum', a perspective projection matrix that shows all the objects in the scene.
        gluPerspective(fieldOfView, 1, nearPlane, nearPlane + sceneBoundingRadius * 2);
        // Store the shadow frustum in 'lightProjection'.
        glGetFloat(GL_PROJECTION_MATRIX, lightProjection);
        glMatrixMode(GL_MODELVIEW);
        // Store the current model-view matrix.
        glPushMatrix();
        glLoadIdentity();
        // Have the 'shadow camera' look toward [0, 0, 0] and be location at the light's position.
        gluLookAt(lightPosition.get(0), lightPosition.get(1), lightPosition.get(2), 0, 0, 0, 0, 1, 0);
        glGetFloat(GL_MODELVIEW_MATRIX, lightModelView);
        // Set the view port to the shadow map dimensions so no part of the shadow is cut off.
        glViewport(0, 0, shadowMapWidth, shadowMapHeight);
        // Bind the extra frame buffer in which to store the shadow map in the form a depth texture.
        glBindFramebuffer(GL_FRAMEBUFFER, frameBuffer);
        // Clear only the depth buffer bit. Clearing the colour buffer is unnecessary, because it is disabled (we
        // only need depth components).
        glClear(GL_DEPTH_BUFFER_BIT);
        // Store the current attribute state.
        glPushAttrib(GL_ALL_ATTRIB_BITS);
        {
            // Disable smooth shading, because the shading in a shadow map is irrelevant. It only matters where the
            // shape
            // vertices are positioned, and not what colour they have.
            glShadeModel(GL_FLAT);
            // Enabling all these lighting states is unnecessary for reasons listed above.
            glDisable(GL_LIGHTING);
            glDisable(GL_COLOR_MATERIAL);
            glDisable(GL_NORMALIZE);
            // Disable the writing of the red, green, blue, and alpha colour components,
            // because we only need the depth component.
            glColorMask(false, false, false, false);
            // An offset is given to every depth value of every polygon fragment to prevent a visual quirk called
            // 'shadow
            // acne'.
            glEnable(GL_POLYGON_OFFSET_FILL);
            // Draw the objects that cast shadows.
            render();
            /**
             * Copy the pixels of the shadow map to the frame buffer object depth attachment.
             *  int target -> GL_TEXTURE_2D
             *  int level  -> 0, has to do with mip-mapping, which is not applicable to shadow maps
             *  int internalformat -> GL_DEPTH_COMPONENT
             *  int x, y -> 0, 0
             *  int width, height -> shadowMapWidth, shadowMapHeight
             *  int border -> 0
             */
            glCopyTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, 0, 0, shadowMapWidth, shadowMapHeight, 0);
            // Restore the previous model-view matrix.
            glPopMatrix();
            glMatrixMode(GL_PROJECTION);
            // Restore the previous projection matrix.
            glPopMatrix();
            glMatrixMode(GL_MODELVIEW);
            glBindFramebuffer(GL_FRAMEBUFFER, 0);
        }// Restore the previous attribute state.
        glPopAttrib();
        // Restore the view port.
        glViewport(0, 0, Display.getWidth(), Display.getHeight());
        lightProjectionTemp.load(lightProjection);
        lightModelViewTemp.load(lightModelView);
        lightProjection.flip();
        lightModelView.flip();
        depthModelViewProjection.setIdentity();
        // [-1,1] -> [-0.5,0.5] -> [0,1]
        depthModelViewProjection.translate(new Vector3f(0.5F, 0.5F, 0.5F));
        depthModelViewProjection.scale(new Vector3f(0.5F, 0.5F, 0.5F));
        // Multiply the texture matrix by the projection and model-view matrices of the light.
        Matrix4f.mul(depthModelViewProjection, lightProjectionTemp, depthModelViewProjection);
        Matrix4f.mul(depthModelViewProjection, lightModelViewTemp, depthModelViewProjection);
        // Transpose the texture matrix.
        Matrix4f.transpose(depthModelViewProjection, depthModelViewProjection);
    }
    
    private static void setUpShaders() {
        shaderProgram = ShaderLoader.loadShaderPair(VERTEX_SHADER_LOCATION, FRAGMENT_SHADER_LOCATION);
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
    
    private static void initShaders(){
        shaderProgram = glCreateProgram();
        vertexShader = glCreateShader(GL_VERTEX_SHADER);
        fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        vertexShaderSource = new StringBuilder();
        fragmentShaderSource = new StringBuilder();
        reader = null;
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

    private static void initFog() {
        glEnable(GL_FOG);
        glFog(GL_FOG_COLOR, asFloatBuffer(new float[]{0.65f, 0.65f, 0.85f, 1f}));
        glFogi(GL_FOG_MODE, GL_LINEAR);
        glFogf(GL_FOG_START, 48.f);
        glFogf(GL_FOG_END, 144.f);
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
        //chunkManager.createVBOs();
        System.out.println("VBOs created in " + (System.nanoTime() - time) / 1000000000 + " seconds.");
        chunkManager.getChunkLoader().loadChunks();
        chunkManager.getChunkLoader().start();
        //chunkManager.stopGeneration();  
        chunkManager.startChunkRenderChecker();
        chunkCreationDistance = inGameCreationDistance;
        drawShadowMap();
        
        while (!Display.isCloseRequested() && !Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
            
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            updateView();
            processInput(getDelta());
            for (int i = 0; i < ChunkManager.maxThreads / 2; i++) {
                chunkManager.checkChunkUpdates();
            }
            chunkManager.processBufferData();
            //glUseProgram(shaderProgram);
            render();
            drawShadowMap();
            //glUseProgram(0);
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

    public final static int xInChunk(int xx) {
        int x = xx;
        if (x <= 0) {
            x = Chunk.CHUNK_SIZE + x % Chunk.CHUNK_SIZE;
        }
        return x % Chunk.CHUNK_SIZE;
    }

    public final static int zInChunk(int zz) {
        int z = zz;
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
        return (int) (noise * GROUND_SHARE);
        //return Math.max((int) ((noise+getLargeNoise(x, z))*GROUND_SHARE),2);
    }

    private static int getLargeNoise(float x, float z) {
        return (int) (FastNoise.noise(x / (1f * 500), z / (1f * 100), 1) * ((float) (Chunk.VERTICAL_CHUNKS / 2 * Chunk.CHUNK_SIZE) / 256f)) - 1;

    }

    public static int getTreeNoise(int x, int z) {
        int xx = xInChunk(x);
        int zz = zInChunk(z);
        if (xx > 1 && xx < Chunk.CHUNK_SIZE - 2 && zz > 1 && zz < Chunk.CHUNK_SIZE - 2) {
            int noise = (int) (FastNoise.noise(x + 1000, z + 1000, 7));
            if (noise == 30) {
                return 0;
            }
        }
        return -1;

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
