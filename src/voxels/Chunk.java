package voxels;

/**
 *
 * @author otso
 */
public class Chunk {

    public static final int CHUNK_WIDTH = 16;
    public static final int CHUNK_HEIGHT = 256;
    public static final int WATER_HEIGHT = 150;

    private int vboVertexHandle;
    private int vboNormalHandle;
    private int vboTexHandle;
    private int vboColorHandle;
    private int vertices;

    public final int xCoordinate;
    public final int zCoordinate;
    public final int xId;
    public final int zId;

    public Block[][][] blocks;
    public int[][] maxHeights;

    public Chunk(int xId, int zId) {
        this.xId = xId;
        this.zId = zId;
        xCoordinate = xId * CHUNK_WIDTH;
        zCoordinate = zId * CHUNK_WIDTH;
        blocks = new Block[CHUNK_WIDTH][CHUNK_HEIGHT][CHUNK_WIDTH];
        maxHeights = new int[CHUNK_WIDTH][CHUNK_WIDTH];

        for (int x = 0; x < blocks.length; x++) {
            blocks[x] = new Block[CHUNK_HEIGHT][CHUNK_WIDTH];
            for (int y = 0; y < blocks[x].length; y++) {
                blocks[x][y] = new Block[CHUNK_WIDTH];
                for (int z = 0; z < blocks[x][y].length; z++) {
                    blocks[x][y][z] = new Block(Block.AIR);
                }
            }
        }

        setGroundBlocks();
        setWaterBlocks();

    }

    private void setGroundBlocks() {
        int activeBlocks = 0;
        int maxHeight;
        for (int x = 0; x < blocks.length; x++) {
            for (int z = 0; z < blocks[x][0].length; z++) {
                maxHeight = Voxels.getNoise(x + xCoordinate, z + zCoordinate);
                maxHeights[x][z] = maxHeight;
            }
        }

        int difference = 0;
        for (int x = 0; x < blocks.length; x++) {
            for (int z = 0; z < blocks[x][0].length; z++) {
                if (x == 0 || z == 0 || x == blocks.length - 1 || z == blocks[x][0].length - 1)
                    // This shouldn't be min value of all 4 bordering block heights, since it can activate unnecessary blocks. Works for now.
                    difference = Math.min(Math.min(Voxels.getNoise(x + xCoordinate - 1, z + zCoordinate), Voxels.getNoise(x + xCoordinate + 1, z + zCoordinate)), Math.min(Voxels.getNoise(x + xCoordinate, z + zCoordinate + 1), Voxels.getNoise(x + xCoordinate, z + zCoordinate - 1)));

                for (int y = 0; y < blocks[x].length; y++) {

                    if (y == maxHeights[x][z]) {
                        blocks[x][y][z].setType(Block.GROUND);
                    }
                    else if ((x == 0 || x == blocks.length - 1 || z == 0 || z == blocks[x][y].length - 1) && y < maxHeights[x][z] && y > difference) {
                        blocks[x][y][z].setType(Block.GROUND);
                    }
                }
            }
        }
        // second loop, activate blocks in steps that are higher than one block
        int heightDifference;
        for (int x = 1; x < blocks.length - 1; x++) {
            for (int z = 1; z < blocks[x][0].length - 1; z++) {
                heightDifference = maxHeights[x][z] - Math.min(Math.min(maxHeights[x + 1][z], maxHeights[x - 1][z]), Math.min(maxHeights[x][z + 1], maxHeights[x][z - 1]));
                if (heightDifference > 1)
                    for (int y = maxHeights[x][z] - heightDifference; y < maxHeights[x][z]; y++) {
                        blocks[x][y][z].setType(Block.GROUND);
                    }
            }
        }
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

    private void setWaterBlocks() {
        int waterCount = 0;
        for (int x = 0; x < blocks.length; x++) {
            for (int z = 0; z < blocks[x][0].length; z++) {
                for (int y = maxHeights[x][z] + 1; y <= Chunk.WATER_HEIGHT; y++) {
                    if (y > maxHeights[x][z] && y <= Chunk.WATER_HEIGHT) {
                        blocks[x][y][z].setType(Block.WATER);
                        waterCount++;
                    }
                }
            }
        }
//        System.out.println("Water blocks: " + waterCount);
    }

}
