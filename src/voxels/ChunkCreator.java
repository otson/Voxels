package voxels;

/**
 *
 * @author otso
 */
public class ChunkCreator {

    private int currentChunkX;
    private int currentChunkZ;
    private int maxDistance = Voxels.chunkCreationDistance;
    private int dx = 1;
    private int dz = -1;
    private int x = 0;
    private int z = 0;
    private int currentLength = 0;
    private int length = 1;
    private int count = 0;
    private int turnCount = 0;
    private boolean needMiddleChunk = false;

    public ChunkCreator() {

    }

    int[] getNewXZ() {
        if (needMiddleChunk) {
            needMiddleChunk = false;
            return new int[]{0, 0};
        }

        else if (dz != 0) {
            z += dz;
            currentLength++;
            if (currentLength == length) {
                currentLength = 0;
                dx = dz;
                dz = 0;
                turnCount++;
                if (turnCount == 2)
                    length++;
            }
        }
        else {
            x += dx;
            currentLength++;
            if (currentLength == length) {
                currentLength = 0;
                dz = -dx;
                dx = 0;
                turnCount++;
                if (turnCount == 2) {
                    length++;
                    turnCount = 0;
                }
            }
        }
        if (Math.abs(x) <= maxDistance && Math.abs(z) <= maxDistance)
            return new int[]{x, z};
        else
            return new int[]{0, 0};
    }

    public void setCurrentChunkX(int currentChunkX) {
        if (currentChunkX != this.currentChunkX)
            reset();
        this.currentChunkX = currentChunkX;
    }

    public void setCurrentChunkZ(int currentChunkZ) {
        if (currentChunkZ != this.currentChunkZ)
            reset();
        this.currentChunkZ = currentChunkZ;
    }

    private void reset() {
        dx = 1;
        dz = -1;
        x = 0;
        z = 0;
        currentLength = 0;
        length = 1;
        count = 0;
        turnCount = 0;
        needMiddleChunk = true;
    }

    public boolean notAtMax() {
        return Math.abs(x) <= maxDistance && Math.abs(z) <= maxDistance;
    }
}
