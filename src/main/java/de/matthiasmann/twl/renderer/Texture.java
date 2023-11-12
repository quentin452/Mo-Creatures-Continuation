package de.matthiasmann.twl.renderer;

import de.matthiasmann.twl.*;

public interface Texture extends Resource
{
    int getWidth();
    
    int getHeight();
    
    Image getImage(final int p0, final int p1, final int p2, final int p3, final Color p4, final boolean p5, final Rotation p6);
    
    MouseCursor createCursor(final int p0, final int p1, final int p2, final int p3, final int p4, final int p5, final Image p6);
    
    void themeLoadingDone();
    
    public enum Rotation
    {
        NONE, 
        CLOCKWISE_90, 
        CLOCKWISE_180, 
        CLOCKWISE_270;
    }
}
