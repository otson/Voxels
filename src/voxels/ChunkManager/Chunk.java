package voxels.ChunkManager;

import java.io.Serializable;

import voxels.Voxels;

/**
 *
 * @author otso
 */
public class Chunk implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int CHUNK_WIDTH = 16;
    public static final int CHUNK_HEIGHT = 256;
    public static final int WATER_HEIGHT = 128;

    private int vboVertexHandle;
    private int vboNormalHandle;
    private int vboTexHandle;
    private int vboColorHandle;
    private int vertices;
    private int vertexCount = 0;

    public final int xCoordinate;
    public final int zCoordinate;
    public final int xId;
    public final int zId;

    public Block[][][] blocks;
    public int[][] maxHeights;

    private Chunk leftChunk;
    private Chunk rightChunk;
    private Chunk frontChunk;
    private Chunk backChunk;

    public Chunk(int xId, int zId) {
        this.xId = xId;
        this.zId = zId;
        xCoordinate = xId * CHUNK_WIDTH;
        zCoordinate = zId * CHUNK_WIDTH;
        initMaxHeights();
        setBlocks();
        setActiveBlocks();
        setActiveBorderBlocks();
        calculateVertexCount();
    }

    private void initMaxHeights() {
        maxHeights = new int[CHUNK_WIDTH][CHUNK_WIDTH];
        for (int x = 0; x < CHUNK_WIDTH; x++) {
            for (int z = 0; z < CHUNK_WIDTH; z++) {
                maxHeights[x][z] = Voxels.getNoise(x + xCoordinate, z + zCoordinate);
            }
        }
    }

    private void setBlocks() {
        blocks = new Block[CHUNK_WIDTH][CHUNK_HEIGHT][CHUNK_WIDTH];
        for (int x = 0; x < blocks.length; x++) {
            blocks[x] = new Block[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < blocks[x].length; y++) {
                blocks[x][y] = new Block[CHUNK_WIDTH];
                for (int z = 0; z < blocks[x][y].length; z++) {
                    if (y > maxHeights[x][z] && y <= Chunk.WATER_HEIGHT) {
                        blocks[x][y][z] = new Block(Type.WATER);
                    }
                    else if (y <= maxHeights[x][z])
                        blocks[x][y][z] = new Block(Type.DIRT);
                    else
                        blocks[x][y][z] = new Block(Type.AIR);
                }
            }
        }
    }

    private void setActiveBlocks() {

        // set blocks that are inside the chunk
        for (int x = 1; x < blocks.length - 1; x++) {
            for (int y = 1; y < blocks[x].length - 1; y++) {
                for (int z = 1; z < blocks[x][y].length - 1; z++) {
                    // if air, it is inactive
                    if (blocks[x][y][z].is(Type.AIR))
                        blocks[x][y][z].setActive(false);
                    // if dirt, if it surrounded by 6 dirt blocks, make it inactive
                    else if (blocks[x][y][z].is(Type.DIRT)) {
                        if (blocks[x + 1][y][z].is(Type.DIRT) && blocks[x - 1][y][z].is(Type.DIRT) && blocks[x][y + 1][z].is(Type.DIRT) && blocks[x][y - 1][z].is(Type.DIRT) && blocks[x][y][z + 1].is(Type.DIRT) && blocks[x][y][z - 1].is(Type.DIRT))
                            blocks[x][y][z].setActive(false);
                        else {
                            // set active sides to be rendered, rendered if the side is not touching dirt
                            blocks[x][y][z].setActive(true);
                            if (blocks[x + 1][y][z].isOpaque()) {
                                blocks[x][y][z].setRight(true);
                            }
                            if (blocks[x - 1][y][z].isOpaque()) {
                                blocks[x][y][z].setLeft(true);

                            }
                            if (blocks[x][y + 1][z].isOpaque()) {
                                blocks[x][y][z].setTop(true);

                            }
                            if (blocks[x][y - 1][z].isOpaque()) {
                                blocks[x][y][z].setBottom(true);

                            }
                            if (blocks[x][y][z + 1].isOpaque()) {
                                blocks[x][y][z].setFront(true);

                            }
                            if (blocks[x][y][z - 1].isOpaque()) {
                                blocks[x][y][z].setBack(true);

                            }
                        }
                    }
                    else if (blocks[x][y][z].is(Type.WATER))
                        // if water, if the block above it is not water, make it active
                        if (blocks[x][y + 1][z].is(Type.WATER) == false) {
                            blocks[x][y][z].setActive(true);
                            blocks[x][y][z].setTop(true);

                        }
                }
            }
        }
    }

    private void setActiveBorderBlocks() {
        updateChunks();

        updateTopLeftBack();
        updateTopLeftFront();
        updateTopRightBack();
        updateTopRightFront();

        updateBottomLeftBack();
        updateBottomLeftFront();
        updateBottomRightBack();
        updateBottomRightFront();

        updateTopLeft();
        updateTopRight();
        updateTopFront();
        updateTopBack();

        updateBottomLeft();
        updateBottomRight();
        updateBottomFront();
        updateBottomBack();

        updateLeftSide();
        updateRightSide();
        updateTopSide();
        updateBottomSide();
        updateFrontSide();
        updateBackSide();

    }

    private void updateChunks() {
        rightChunk = Voxels.chunkManager.getChunk(xId + 1, zId);
        leftChunk = Voxels.chunkManager.getChunk(xId - 1, zId);
        backChunk = Voxels.chunkManager.getChunk(xId, zId - 1);
        frontChunk = Voxels.chunkManager.getChunk(xId + 1, xId + 1);
    }

    public int[][] getMaxHeights() {
        return maxHeights;
    }

    public int getVboVertexHandle() {
        return vboVertexHandle;
    }

    public void setVboVertexHandle(int vboVertexHandle) {
        this.vboVertexHandle = vboVertexHandle;
    }

    public int getVertices() {
        return vertices;
    }

    public void setVertices(int vertices) {
        this.vertices = vertices;
    }

    public void setVboNormalHandle(int vboNormalHandle) {
        this.vboNormalHandle = vboNormalHandle;
    }

    public int getVboNormalHandle() {
        return vboNormalHandle;
    }

    public int getVboTexHandle() {
        return vboTexHandle;
    }

    public void setVboTexHandle(int vboTexHandle) {
        this.vboTexHandle = vboTexHandle;
    }

    public int getVboColorHandle() {
        return vboColorHandle;
    }

    public void setVboColorHandle(int vboColorHandle) {
        this.vboColorHandle = vboColorHandle;
    }

    private void updateTopLeftBack() {
        if (leftChunk != null) {
            if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
                blocks[0][Chunk.CHUNK_WIDTH - 1][0].setLeft(true);
        }
        if (backChunk != null) {
            if (backChunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
                blocks[0][Chunk.CHUNK_WIDTH - 1][0].setBack(true);
        }

        // add code for top chunk check when chunks are made smaller
        if (blocks[1][Chunk.CHUNK_WIDTH - 1][0].isOpaque())
            blocks[0][Chunk.CHUNK_WIDTH - 1][0].setRight(true);

        if (blocks[0][Chunk.CHUNK_WIDTH - 2][0].isOpaque())
            blocks[0][Chunk.CHUNK_WIDTH - 1][0].setBottom(true);

        if (blocks[0][Chunk.CHUNK_WIDTH - 1][1].isOpaque())
            blocks[0][Chunk.CHUNK_WIDTH - 1][0].setFront(true);

    }

    private void updateTopLeftFront() {
        if (leftChunk != null) {
            if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
                blocks[0][Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_WIDTH - 1].setLeft(true);
        }
        if (frontChunk != null) {
            if (frontChunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
                blocks[0][Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_WIDTH - 1].setFront(true);
        }

        // add code for top chunk check when chunks are made smaller
        if (blocks[0][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 2].isOpaque())
            blocks[0][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setBack(true);

        if (blocks[0][Chunk.CHUNK_HEIGHT - 2][Chunk.CHUNK_WIDTH - 1].isOpaque())
            blocks[0][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setBottom(true);

        if (blocks[1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
            blocks[0][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setRight(true);
    }

    private void updateTopRightBack() {
        if (backChunk != null) {
            if (backChunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
                blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][0].setBack(true);
        }
        if (rightChunk != null) {
            if (rightChunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
                blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][0].setRight(true);
        }

        if (blocks[Chunk.CHUNK_WIDTH - 2][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
            blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][0].setLeft(true);

        if (blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 2][0].isOpaque())
            blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][0].setBottom(true);

        if (blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][1].isOpaque())
            blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][0].setFront(true);
    }

    private void updateTopRightFront() {
        if (rightChunk != null) {
            if (rightChunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
                blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setRight(true);
        }
        if (frontChunk != null) {
            if (frontChunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
                blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setFront(true);
        }

        if (blocks[Chunk.CHUNK_WIDTH - 2][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
            blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setLeft(true);

        if (blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 2][Chunk.CHUNK_WIDTH - 1].isOpaque())
            blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setBottom(true);

        if (blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 2].isOpaque())
            blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setBack(true);
    }

    private void updateTopLeft() {
        boolean isValid = leftChunk != null;

        for (int z = 1; z < Chunk.CHUNK_WIDTH - 1; z++) {
            if (isValid)
                if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][z].isOpaque())
                    blocks[0][Chunk.CHUNK_HEIGHT - 1][z].setLeft(true);

            if (blocks[1][Chunk.CHUNK_HEIGHT - 1][z].isOpaque())
                blocks[0][Chunk.CHUNK_HEIGHT - 1][z].setRight(true);

            if (blocks[0][Chunk.CHUNK_HEIGHT - 2][z].isOpaque())
                blocks[0][Chunk.CHUNK_HEIGHT - 1][z].setBottom(true);

            if (blocks[0][Chunk.CHUNK_HEIGHT - 1][z + 1].isOpaque())
                blocks[0][Chunk.CHUNK_HEIGHT - 1][z].setFront(true);

            if (blocks[0][Chunk.CHUNK_HEIGHT - 1][z - 1].isOpaque())
                blocks[0][Chunk.CHUNK_HEIGHT - 1][z].setBack(true);
        }
    }

    private void updateTopRight() {
        boolean isValid = rightChunk != null;

        for (int z = 1; z < Chunk.CHUNK_WIDTH - 1; z++) {
            if (isValid)
                if (rightChunk.blocks[0][Chunk.CHUNK_HEIGHT - 1][z].isOpaque())
                    blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][z].setRight(true);

            if (blocks[Chunk.CHUNK_WIDTH - 2][Chunk.CHUNK_HEIGHT - 1][z].isOpaque())
                blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][z].setLeft(true);

            if (blocks[0][Chunk.CHUNK_HEIGHT - 2][z].isOpaque())
                blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][z].setBottom(true);

            if (blocks[0][Chunk.CHUNK_HEIGHT - 1][z + 1].isOpaque())
                blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][z].setFront(true);

            if (blocks[0][Chunk.CHUNK_HEIGHT - 1][z - 1].isOpaque())
                blocks[Chunk.CHUNK_WIDTH - 1][Chunk.CHUNK_HEIGHT - 1][z].setBack(true);
        }
    }

    private void updateTopFront() {
        boolean isValid = frontChunk != null;

        for (int x = 1; x < Chunk.CHUNK_WIDTH - 1; x++) {
            if (isValid)
                if (frontChunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
                    blocks[x][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setFront(true);

            if (blocks[x + 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
                blocks[x][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setRight(true);

            if (blocks[x - 1][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
                blocks[x][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setLeft(true);

            if (blocks[x][Chunk.CHUNK_HEIGHT - 2][Chunk.CHUNK_WIDTH - 1].isOpaque())
                blocks[x][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setBottom(true);

            if (blocks[x][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 2].isOpaque())
                blocks[x][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].setBack(true);
        }
    }

    private void updateTopBack() {
        boolean isValid = backChunk != null;

        for (int x = 1; x < Chunk.CHUNK_WIDTH - 1; x++) {
            if (isValid)
                if (backChunk.blocks[x][Chunk.CHUNK_HEIGHT - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
                    blocks[x][Chunk.CHUNK_HEIGHT - 1][0].setBack(true);

            if (blocks[x + 1][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
                blocks[x][Chunk.CHUNK_HEIGHT - 1][0].setRight(true);

            if (blocks[x - 1][Chunk.CHUNK_HEIGHT - 1][0].isOpaque())
                blocks[x][Chunk.CHUNK_HEIGHT - 1][0].setLeft(true);

            if (blocks[x][Chunk.CHUNK_HEIGHT - 2][0].isOpaque())
                blocks[x][Chunk.CHUNK_HEIGHT - 1][0].setBottom(true);

            if (blocks[x][Chunk.CHUNK_HEIGHT - 1][1].isOpaque())
                blocks[x][Chunk.CHUNK_HEIGHT - 1][0].setFront(true);
        }

    }

    private void updateTopSide() {
        for (int x = 1; x < Chunk.CHUNK_WIDTH - 1; x++) {
            for (int z = 1; z < Chunk.CHUNK_WIDTH - 1; z++) {

                if (blocks[x + 1][Chunk.CHUNK_WIDTH - 1][z].isOpaque())
                    blocks[x][Chunk.CHUNK_WIDTH - 1][z].setRight(true);

                if (blocks[x - 1][Chunk.CHUNK_WIDTH - 1][z].isOpaque())
                    blocks[x][Chunk.CHUNK_WIDTH - 1][z].setLeft(true);

                if (blocks[x][Chunk.CHUNK_WIDTH - 2][z].isOpaque())
                    blocks[x][Chunk.CHUNK_WIDTH - 1][z].setBottom(true);

                if (blocks[x][Chunk.CHUNK_WIDTH - 1][z + 1].isOpaque())
                    blocks[x][Chunk.CHUNK_WIDTH - 1][z].setFront(true);

                if (blocks[x][Chunk.CHUNK_WIDTH - 1][z - 1].isOpaque())
                    blocks[x][Chunk.CHUNK_WIDTH - 1][z].setBack(true);
            }

        }
    }

    private void updateBottomLeftBack() {
        if (leftChunk != null)
            if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][0][0].isOpaque())
                blocks[0][0][0].setLeft(true);

        if (backChunk != null)
            if (backChunk.blocks[0][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
                blocks[0][0][0].setBack(true);

        if (blocks[1][0][0].isOpaque())
            blocks[0][0][0].setRight(true);

        if (blocks[0][1][0].isOpaque())
            blocks[0][0][0].setTop(true);

        if (blocks[0][0][1].isOpaque())
            blocks[0][0][0].setFront(true);
    }

    private void updateBottomLeftFront() {
        if (leftChunk != null) {
            if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
                blocks[0][0][Chunk.CHUNK_WIDTH - 1].setLeft(true);
        }

        if (frontChunk != null) {
            if (frontChunk.blocks[0][0][0].isOpaque())
                blocks[0][0][Chunk.CHUNK_WIDTH - 1].setFront(true);
        }

        if (blocks[1][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
            blocks[0][0][Chunk.CHUNK_WIDTH - 1].setRight(true);

        if (blocks[0][1][Chunk.CHUNK_WIDTH - 1].isOpaque())
            blocks[0][0][Chunk.CHUNK_WIDTH - 1].setTop(true);

        if (blocks[0][0][Chunk.CHUNK_WIDTH - 2].isOpaque())
            blocks[0][0][Chunk.CHUNK_WIDTH - 1].setBack(true);
    }

    private void updateBottomRightBack() {
        if (rightChunk != null)
            if (rightChunk.blocks[0][0][0].isOpaque())
                blocks[Chunk.CHUNK_WIDTH - 1][0][0].setRight(true);

        if (backChunk != null)
            if (backChunk.blocks[Chunk.CHUNK_WIDTH - 1][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
                blocks[Chunk.CHUNK_WIDTH - 1][0][0].setBack(true);

        if (blocks[Chunk.CHUNK_WIDTH - 2][0][0].isOpaque())
            blocks[Chunk.CHUNK_WIDTH - 1][0][0].setLeft(true);

        if (blocks[Chunk.CHUNK_WIDTH - 1][1][0].isOpaque())
            blocks[Chunk.CHUNK_WIDTH - 1][0][0].setTop(true);

        if (blocks[Chunk.CHUNK_WIDTH - 1][0][1].isOpaque())
            blocks[Chunk.CHUNK_WIDTH - 1][0][0].setFront(true);
    }

    private void updateBottomRightFront() {

        if (rightChunk != null) {
            if (rightChunk.blocks[Chunk.CHUNK_WIDTH - 1][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
                blocks[Chunk.CHUNK_WIDTH - 1][0][Chunk.CHUNK_WIDTH - 1].setRight(true);
        }

        if (frontChunk != null) {
            if (frontChunk.blocks[Chunk.CHUNK_WIDTH - 1][0][0].isOpaque())
                blocks[Chunk.CHUNK_WIDTH - 1][0][Chunk.CHUNK_WIDTH - 1].setFront(true);
        }

        if (blocks[Chunk.CHUNK_WIDTH - 2][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
            blocks[Chunk.CHUNK_WIDTH - 1][0][Chunk.CHUNK_WIDTH - 1].setLeft(true);

        if (blocks[0][1][Chunk.CHUNK_WIDTH - 1].isOpaque())
            blocks[Chunk.CHUNK_WIDTH - 1][0][Chunk.CHUNK_WIDTH - 1].setTop(true);

        if (blocks[0][0][Chunk.CHUNK_WIDTH - 2].isOpaque())
            blocks[Chunk.CHUNK_WIDTH - 1][0][Chunk.CHUNK_WIDTH - 1].setBack(true);
    }

    private void updateBottomLeft() {
        boolean isValid = leftChunk != null;

        for (int z = 1; z < Chunk.CHUNK_WIDTH - 1; z++) {
            if (isValid)
                if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][0][z].isOpaque())
                    blocks[0][0][z].setLeft(true);

            if (blocks[1][0][z].isOpaque()) {
                blocks[0][0][z].setRight(true);
            }

            if (blocks[0][1][z].isOpaque()) {
                blocks[0][0][z].setTop(true);
            }

            if (blocks[0][0][z + 1].isOpaque()) {
                blocks[0][0][z].setFront(true);
            }

            if (blocks[0][0][z - 1].isOpaque()) {
                blocks[0][0][z].setBack(true);
            }
        }
    }

    private void updateBottomRight() {

        boolean isValid = rightChunk != null;

        for (int z = 1; z < Chunk.CHUNK_WIDTH - 1; z++) {
            if (isValid)
                if (rightChunk.blocks[0][0][z].isOpaque())
                    blocks[Chunk.CHUNK_WIDTH - 1][0][z].setRight(true);

            if (blocks[Chunk.CHUNK_WIDTH - 2][0][z].isOpaque()) {
                blocks[Chunk.CHUNK_WIDTH - 1][0][z].setLeft(true);
            }

            if (blocks[Chunk.CHUNK_WIDTH - 1][1][z].isOpaque()) {
                blocks[Chunk.CHUNK_WIDTH - 1][0][z].setTop(true);
            }

            if (blocks[Chunk.CHUNK_WIDTH - 1][0][z + 1].isOpaque()) {
                blocks[Chunk.CHUNK_WIDTH - 1][0][z].setFront(true);
            }

            if (blocks[Chunk.CHUNK_WIDTH - 1][0][z - 1].isOpaque()) {
                blocks[Chunk.CHUNK_WIDTH - 1][0][z].setBack(true);
            }
        }
    }

    private void updateBottomFront() {
        boolean isValid = frontChunk != null;

        for (int x = 1; x < Chunk.CHUNK_WIDTH - 1; x++) {
            if (isValid)
                if (frontChunk.blocks[x][0][0].isOpaque())
                    blocks[x][0][Chunk.CHUNK_WIDTH - 1].setFront(true);

            if (blocks[x + 1][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
                blocks[x][0][Chunk.CHUNK_WIDTH - 1].setRight(true);

            if (blocks[x - 1][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
                blocks[x][0][Chunk.CHUNK_WIDTH - 1].setLeft(true);

            if (blocks[x][1][Chunk.CHUNK_WIDTH - 1].isOpaque())
                blocks[x][0][Chunk.CHUNK_WIDTH - 1].setTop(true);

            if (blocks[x][0][Chunk.CHUNK_WIDTH - 2].isOpaque())
                blocks[x][0][Chunk.CHUNK_WIDTH - 1].setBack(true);
        }
    }

    private void updateBottomBack() {
        boolean isValid = backChunk != null;

        for (int x = 1; x < Chunk.CHUNK_WIDTH - 1; x++) {
            if (isValid)
                if (backChunk.blocks[x][0][Chunk.CHUNK_WIDTH - 1].isOpaque())
                    blocks[x][0][0].setBack(true);

            if (blocks[x + 1][0][0].isOpaque())
                blocks[x][0][0].setRight(true);

            if (blocks[x - 1][0][0].isOpaque())
                blocks[x][0][0].setLeft(true);

            if (blocks[x][1][0].isOpaque())
                blocks[x][0][0].setTop(true);

            if (blocks[x][0][1].isOpaque())
                blocks[x][0][0].setFront(true);
        }
    }

    private void updateBottomSide() {
        for (int x = 1; x < Chunk.CHUNK_WIDTH - 1; x++) {
            for (int z = 1; z < Chunk.CHUNK_WIDTH - 1; z++) {

                if (blocks[x + 1][0][z].isOpaque())
                    blocks[x][0][z].setRight(true);

                if (blocks[x - 1][0][z].isOpaque())
                    blocks[x][0][z].setLeft(true);

                if (blocks[x][1][z].isOpaque())
                    blocks[x][0][z].setTop(true);

                if (blocks[x][0][z + 1].isOpaque())
                    blocks[x][0][z].setFront(true);

                if (blocks[x + 1][0][z - 1].isOpaque())
                    blocks[x][0][z].setBack(true);
            }
        }
    }

    private void updateLeftSide() {
        boolean isValid = leftChunk != null;

        for (int z = 1; z < Chunk.CHUNK_WIDTH - 1; z++) {
            for (int y = 1; y < Chunk.CHUNK_HEIGHT - 1; y++) {
                if (isValid)
                    if (leftChunk.blocks[Chunk.CHUNK_WIDTH - 1][y][z].isOpaque())
                        blocks[0][y][z].setLeft(true);

                if (blocks[0][y + 1][z].isOpaque())
                    blocks[0][y][z].setTop(true);

                if (blocks[0][y - 1][z].isOpaque())
                    blocks[0][y][z].setBottom(true);

                if (blocks[0][y][z + 1].isOpaque())
                    blocks[0][y][z].setFront(true);

                if (blocks[0][y][z - 1].isOpaque())
                    blocks[0][y][z].setBack(true);

                if (blocks[1][y][z - 1].isOpaque())
                    blocks[0][y][z].setRight(true);
            }
        }
    }

    private void updateRightSide() {
        boolean isValid = rightChunk != null;

        for (int z = 1; z < Chunk.CHUNK_WIDTH - 1; z++) {
            for (int y = 1; y < Chunk.CHUNK_HEIGHT - 1; y++) {
                if (isValid)
                    if (rightChunk.blocks[0][y][z].isOpaque())
                        blocks[Chunk.CHUNK_WIDTH - 1][y][z].setRight(true);

                if (blocks[Chunk.CHUNK_WIDTH - 1][y + 1][z].isOpaque())
                    blocks[Chunk.CHUNK_WIDTH - 1][y][z].setTop(true);

                if (blocks[Chunk.CHUNK_WIDTH - 1][y - 1][z].isOpaque())
                    blocks[Chunk.CHUNK_WIDTH - 1][y][z].setBottom(true);

                if (blocks[Chunk.CHUNK_WIDTH - 1][y][z + 1].isOpaque())
                    blocks[Chunk.CHUNK_WIDTH - 1][y][z].setFront(true);

                if (blocks[Chunk.CHUNK_WIDTH - 1][y][z - 1].isOpaque())
                    blocks[Chunk.CHUNK_WIDTH - 1][y][z].setBack(true);

                if (blocks[Chunk.CHUNK_WIDTH - 2][y][z - 1].isOpaque())
                    blocks[Chunk.CHUNK_WIDTH - 1][y][z].setLeft(true);
            }
        }
    }

    private void updateFrontSide() {
        boolean isValid = frontChunk != null;

        for (int x = 1; x < Chunk.CHUNK_WIDTH - 1; x++) {
            for (int y = 1; y < Chunk.CHUNK_HEIGHT - 1; y++) {
                if (isValid)
                    if (frontChunk.blocks[x][y][0].isOpaque())
                        blocks[x][y][Chunk.CHUNK_WIDTH - 1].setFront(true);

                if (blocks[x + 1][y][Chunk.CHUNK_WIDTH - 1].isOpaque())
                    blocks[x][y][Chunk.CHUNK_WIDTH - 1].setRight(true);

                if (blocks[x - 1][y][Chunk.CHUNK_WIDTH - 1].isOpaque())
                    blocks[x][y][Chunk.CHUNK_WIDTH - 1].setLeft(true);

                if (blocks[x][y + 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
                    blocks[x][y][Chunk.CHUNK_WIDTH - 1].setTop(true);

                if (blocks[x][y - 1][Chunk.CHUNK_WIDTH - 1].isOpaque())
                    blocks[x][y][Chunk.CHUNK_WIDTH - 1].setBottom(true);

                if (blocks[x][y][Chunk.CHUNK_WIDTH - 2].isOpaque())
                    blocks[x][y][Chunk.CHUNK_WIDTH - 1].setBack(true);
            }
        }
    }

    private void updateBackSide() {
        boolean isValid = backChunk != null;

        for (int x = 1; x < Chunk.CHUNK_WIDTH - 1; x++) {
            for (int y = 1; y < Chunk.CHUNK_HEIGHT - 1; y++) {
                if (isValid)
                    if (backChunk.blocks[x][y][Chunk.CHUNK_WIDTH - 1].isOpaque())
                        blocks[x][y][0].setBack(true);

                if (blocks[x + 1][y][0].isOpaque())
                    blocks[x][y][0].setRight(true);

                if (blocks[x - 1][y][0].isOpaque())
                    blocks[x][y][0].setLeft(true);

                if (blocks[x][y + 1][0].isOpaque())
                    blocks[x][y][0].setTop(true);

                if (blocks[x][y - 1][0].isOpaque())
                    blocks[x][y][0].setBottom(true);

                if (blocks[x][y][1].isOpaque())
                    blocks[x][y][0].setFront(true);
            }
        }
    }

    private void calculateVertexCount() {
        vertexCount = 0;
        for (int x = 0; x < Chunk.CHUNK_WIDTH; x++)
            for (int z = 0; z < Chunk.CHUNK_WIDTH; z++)
                for (int y = 0; y < Chunk.CHUNK_HEIGHT; y++) {
                    if (blocks[x][y][z].isBack())
                        vertexCount += 6;
                    if (blocks[x][y][z].isBottom())
                        vertexCount += 6;
                    if (blocks[x][y][z].isFront())
                        vertexCount += 6;
                    if (blocks[x][y][z].isLeft())
                        vertexCount += 6;
                    if (blocks[x][y][z].isRight())
                        vertexCount += 6;
                    if (blocks[x][y][z].isTop())
                        vertexCount += 6;
                }
    }

    public int getVertexCount() {
        return vertexCount;
    }
    
    
}
