package de.matthiasmann.twl.renderer;

import de.matthiasmann.twl.*;

public interface OffscreenRenderer
{
    OffscreenSurface startOffscreenRendering(final Widget p0, final OffscreenSurface p1, final int p2, final int p3, final int p4, final int p5);
    
    void endOffscreenRendering();
}
