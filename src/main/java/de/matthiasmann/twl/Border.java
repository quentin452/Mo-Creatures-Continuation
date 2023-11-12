package de.matthiasmann.twl;

public class Border
{
    public static final Border ZERO;
    private final int top;
    private final int left;
    private final int bottom;
    private final int right;
    
    public Border(final int all) {
        this.top = all;
        this.left = all;
        this.bottom = all;
        this.right = all;
    }
    
    public Border(final int horz, final int vert) {
        this.top = vert;
        this.left = horz;
        this.bottom = vert;
        this.right = horz;
    }
    
    public Border(final int top, final int left, final int bottom, final int right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }
    
    public int getBorderBottom() {
        return this.bottom;
    }
    
    public int getBorderLeft() {
        return this.left;
    }
    
    public int getBorderRight() {
        return this.right;
    }
    
    public int getBorderTop() {
        return this.top;
    }
    
    public int getBottom() {
        return this.bottom;
    }
    
    public int getLeft() {
        return this.left;
    }
    
    public int getRight() {
        return this.right;
    }
    
    public int getTop() {
        return this.top;
    }
    
    @Override
    public String toString() {
        return "[Border top=" + this.top + " left=" + this.left + " bottom=" + this.bottom + " right=" + this.right + "]";
    }
    
    static {
        ZERO = new Border(0);
    }
}
