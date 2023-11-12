package de.matthiasmann.twl.renderer;

import de.matthiasmann.twl.*;

public interface Image
{
    int getWidth();
    
    int getHeight();
    
    void draw(final AnimationState p0, final int p1, final int p2);
    
    void draw(final AnimationState p0, final int p1, final int p2, final int p3, final int p4);
    
    Image createTintedVersion(final Color p0);
}
