package voxels.ChunkManager;

/**
 *
 * @author otso
 */
public class Pair {

    public final int x;
    public final int y;

    public Pair(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.x;
        hash = 83 * hash + this.y;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Pair other = (Pair) obj;
        if (this.x != other.x)
            return false;
        if (this.y != other.y)
            return false;
        return true;
    }
}
