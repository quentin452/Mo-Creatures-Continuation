package de.matthiasmann.twl.renderer;

import java.net.*;
import de.matthiasmann.twl.utils.*;
import java.io.*;
import de.matthiasmann.twl.*;

public interface Renderer
{
    long getTimeMillis();
    
    boolean startRendering();
    
    void endRendering();
    
    int getWidth();
    
    int getHeight();
    
    CacheContext createNewCacheContext();
    
    void setActiveCacheContext(final CacheContext p0) throws IllegalStateException;
    
    CacheContext getActiveCacheContext();
    
    Font loadFont(final URL p0, final StateSelect p1, final FontParameter... p2) throws IOException;
    
    Texture loadTexture(final URL p0, final String p1, final String p2) throws IOException;
    
    LineRenderer getLineRenderer();
    
    OffscreenRenderer getOffscreenRenderer();
    
    FontMapper getFontMapper();
    
    DynamicImage createDynamicImage(final int p0, final int p1);
    
    Image createGradient(final Gradient p0);
    
    void clipEnter(final int p0, final int p1, final int p2, final int p3);
    
    void clipEnter(final Rect p0);
    
    boolean clipIsEmpty();
    
    void clipLeave();
    
    void setCursor(final MouseCursor p0);
    
    void setMousePosition(final int p0, final int p1);
    
    void setMouseButton(final int p0, final boolean p1);
    
    void pushGlobalTintColor(final float p0, final float p1, final float p2, final float p3);
    
    void popGlobalTintColor();
}
