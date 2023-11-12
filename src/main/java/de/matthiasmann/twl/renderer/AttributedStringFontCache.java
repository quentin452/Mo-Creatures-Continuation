package de.matthiasmann.twl.renderer;

public interface AttributedStringFontCache extends Resource
{
    int getWidth();
    
    int getHeight();
    
    void draw(final int p0, final int p1);
}
