package de.matthiasmann.twl;

public enum Alignment
{
    LEFT(HAlignment.LEFT, 0, 1), 
    CENTER(HAlignment.CENTER, 1, 1), 
    RIGHT(HAlignment.RIGHT, 2, 1), 
    TOP(HAlignment.CENTER, 1, 0), 
    BOTTOM(HAlignment.CENTER, 1, 2), 
    TOPLEFT(HAlignment.LEFT, 0, 0), 
    TOPRIGHT(HAlignment.RIGHT, 2, 0), 
    BOTTOMLEFT(HAlignment.LEFT, 0, 2), 
    BOTTOMRIGHT(HAlignment.RIGHT, 2, 2), 
    FILL(HAlignment.CENTER, 1, 1);
    
    final HAlignment fontHAlignment;
    final byte hpos;
    final byte vpos;
    
    private Alignment(final HAlignment fontHAlignment, final int hpos, final int vpos) {
        this.fontHAlignment = fontHAlignment;
        this.hpos = (byte)hpos;
        this.vpos = (byte)vpos;
    }
    
    public HAlignment getFontHAlignment() {
        return this.fontHAlignment;
    }
    
    public int getHPosition() {
        return this.hpos;
    }
    
    public int getVPosition() {
        return this.vpos;
    }
    
    public int computePositionX(final int containerWidth, final int objectWidth) {
        return Math.max(0, containerWidth - objectWidth) * this.hpos / 2;
    }
    
    public int computePositionY(final int containerHeight, final int objectHeight) {
        return Math.max(0, containerHeight - objectHeight) * this.vpos / 2;
    }
}
