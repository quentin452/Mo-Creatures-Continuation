package de.matthiasmann.twl;

public class Dimension
{
    public static final Dimension ZERO;
    private final int x;
    private final int y;
    
    public Dimension(final int x, final int y) {
        this.x = x;
        this.y = y;
    }
    
    public int getX() {
        return this.x;
    }
    
    public int getY() {
        return this.y;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final Dimension other = (Dimension)obj;
        return this.x == other.x && this.y == other.y;
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + this.x;
        hash = 71 * hash + this.y;
        return hash;
    }
    
    @Override
    public String toString() {
        return "Dimension[x=" + this.x + ", y=" + this.y + "]";
    }
    
    static {
        ZERO = new Dimension(0, 0);
    }
}
