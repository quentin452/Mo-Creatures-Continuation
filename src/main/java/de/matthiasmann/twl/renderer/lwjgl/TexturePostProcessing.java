package de.matthiasmann.twl.renderer.lwjgl;

import java.nio.*;

public interface TexturePostProcessing
{
    void process(final ByteBuffer p0, final int p1, final int p2, final int p3, final LWJGLTexture.Format p4);
}
