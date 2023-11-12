package de.matthiasmann.twl.renderer;

import java.nio.*;

public interface DynamicImage extends Image, Resource
{
    void update(final ByteBuffer p0, final Format p1);
    
    void update(final ByteBuffer p0, final int p1, final Format p2);
    
    void update(final int p0, final int p1, final int p2, final int p3, final ByteBuffer p4, final Format p5);
    
    void update(final int p0, final int p1, final int p2, final int p3, final ByteBuffer p4, final int p5, final Format p6);
    
    public enum Format
    {
        RGBA, 
        BGRA;
    }
}
