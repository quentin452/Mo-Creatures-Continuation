package de.matthiasmann.twl.renderer.lwjgl;

import de.matthiasmann.twl.renderer.*;
import org.lwjgl.input.*;
import java.nio.*;
import org.lwjgl.*;

class LWJGLCursor implements MouseCursor
{
    Cursor cursor;
    
    LWJGLCursor(final ByteBuffer src, final LWJGLTexture.Format srcFmt, final int srcStride, final int x, final int y, int width, int height, final int hotSpotX, final int hotSpotY) {
        width = Math.min(Cursor.getMaxCursorSize(), width);
        height = Math.min(Cursor.getMaxCursorSize(), height);
        final int dstSize = Math.max(Cursor.getMinCursorSize(), Math.max(width, height));
        final IntBuffer buf = BufferUtils.createIntBuffer(dstSize * dstSize);
        int row = height;
        int dstPos = 0;
        while (row-- > 0) {
            final int offset = srcStride * (y + row) + x * srcFmt.getPixelSize();
            buf.position(dstPos);
            switch (srcFmt) {
                case RGB: {
                    for (int col = 0; col < width; ++col) {
                        final int r = src.get(offset + col * 3 + 0) & 0xFF;
                        final int g = src.get(offset + col * 3 + 1) & 0xFF;
                        final int b = src.get(offset + col * 3 + 2) & 0xFF;
                        buf.put(makeColor(r, g, b, 255));
                    }
                    break;
                }
                case RGBA: {
                    for (int col = 0; col < width; ++col) {
                        final int r = src.get(offset + col * 4 + 0) & 0xFF;
                        final int g = src.get(offset + col * 4 + 1) & 0xFF;
                        final int b = src.get(offset + col * 4 + 2) & 0xFF;
                        final int a = src.get(offset + col * 4 + 3) & 0xFF;
                        buf.put(makeColor(r, g, b, a));
                    }
                    break;
                }
                case ABGR: {
                    for (int col = 0; col < width; ++col) {
                        final int r = src.get(offset + col * 4 + 3) & 0xFF;
                        final int g = src.get(offset + col * 4 + 2) & 0xFF;
                        final int b = src.get(offset + col * 4 + 1) & 0xFF;
                        final int a = src.get(offset + col * 4 + 0) & 0xFF;
                        buf.put(makeColor(r, g, b, a));
                    }
                    break;
                }
                default: {
                    throw new IllegalStateException("Unsupported color format");
                }
            }
            dstPos += dstSize;
        }
        buf.clear();
        try {
            this.cursor = new Cursor(dstSize, dstSize, hotSpotX, Math.min(dstSize - 1, height - hotSpotY - 1), 1, buf, (IntBuffer)null);
        }
        catch (LWJGLException ex) {
            ex.printStackTrace();
        }
    }
    
    private static int makeColor(final int r, final int g, final int b, int a) {
        a = ((a > 222) ? 255 : 0);
        return a << 24 | r << 16 | g << 8 | b;
    }
    
    void destroy() {
        if (this.cursor != null) {
            this.cursor.destroy();
            this.cursor = null;
        }
    }
}
