package de.matthiasmann.twl.renderer;

public interface FontCache extends Resource
{
    int getWidth();
    
    int getHeight();
    
    void draw(final AnimationState p0, final int p1, final int p2);
}
