package voxels.ChunkManager;

/**
 *
 * @author otso
 */
public class Pair {

    public final int x;
    public final int y;
    public final int z;

    public Pair(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int hashCode() {
        int hash = 7243;
        hash = 769 * hash + this.x;
        hash = 769 * hash + this.y;
        hash = 769 * hash + this.z;
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
        if (this.z != other.z)
            return false;
        return true;
    }

    
}
