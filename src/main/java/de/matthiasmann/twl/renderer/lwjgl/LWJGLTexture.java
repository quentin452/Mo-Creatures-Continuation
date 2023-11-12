package de.matthiasmann.twl.renderer.lwjgl;

import java.nio.*;
import org.lwjgl.opengl.*;
import java.util.*;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.utils.*;

public class LWJGLTexture implements Texture, Resource
{
    final LWJGLRenderer renderer;
    private int id;
    private final int width;
    private final int height;
    private final int texWidth;
    private final int texHeight;
    private ByteBuffer texData;
    private Format texDataFmt;
    private ArrayList<LWJGLCursor> cursors;
    
    public LWJGLTexture(final LWJGLRenderer renderer, final int width, final int height, final ByteBuffer buf, final Format fmt, final Filter filter) {
        this.renderer = renderer;
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("size <= 0");
        }
        Util.checkGLError();
        this.id = GL11.glGenTextures();
        Util.checkGLError();
        if (this.id == 0) {
            throw new OpenGLException("failed to allocate texture ID");
        }
        Util.checkGLError();
        GL11.glBindTexture(3553, this.id);
        Util.checkGLError();
        GL11.glPixelStorei(3314, 0);
        Util.checkGLError();
        GL11.glPixelStorei(3317, 1);
        Util.checkGLError();
        if (GLContext.getCapabilities().OpenGL12) {
            Util.checkGLError();
            GL11.glTexParameteri(3553, 10242, 33071);
            Util.checkGLError();
            GL11.glTexParameteri(3553, 10243, 33071);
        }
        else {
            Util.checkGLError();
            GL11.glTexParameteri(3553, 10242, 10496);
            Util.checkGLError();
            GL11.glTexParameteri(3553, 10243, 10496);
        }
        Util.checkGLError();
        GL11.glTexParameteri(3553, 10240, filter.glValue);
        Util.checkGLError();
        GL11.glTexParameteri(3553, 10241, filter.glValue);
        Util.checkGLError();
        this.texWidth = roundUpPOT(width);
        Util.checkGLError();
        this.texHeight = roundUpPOT(height);
        Util.checkGLError();
        if (this.texWidth != width || this.texHeight != height) {
            Util.checkGLError();
            GL11.glTexImage2D(3553, 0, fmt.glInternalFormat, this.texWidth, this.texHeight, 0, fmt.glFormat, 5121, (ByteBuffer)null);
            if (buf != null) {
                Util.checkGLError();
                GL11.glTexSubImage2D(3553, 0, 0, 0, width, height, fmt.glFormat, 5121, buf);
            }
        }
        else {
            Util.checkGLError();
            GL11.glTexImage2D(3553, 0, fmt.glInternalFormat, this.texWidth, this.texHeight, 0, fmt.glFormat, 5121, buf);
        }
        Util.checkGLError();
        this.width = width;
        this.height = height;
        this.texData = buf;
        this.texDataFmt = fmt;
    }
    
    @Override
    public void destroy() {
        if (this.id != 0) {
            GL11.glBindTexture(3553, 0);
            GL11.glDeleteTextures(this.id);
            this.id = 0;
        }
        if (this.cursors != null) {
            for (final LWJGLCursor cursor : this.cursors) {
                cursor.destroy();
            }
            this.cursors.clear();
        }
    }
    
    @Override
    public int getWidth() {
        return this.width;
    }
    
    @Override
    public int getHeight() {
        return this.height;
    }
    
    public int getTexWidth() {
        return this.texWidth;
    }
    
    public int getTexHeight() {
        return this.texHeight;
    }
    
    public boolean bind(final Color color) {
        if (this.id != 0) {
            GL11.glBindTexture(3553, this.id);
            this.renderer.tintStack.setColor(color);
            return true;
        }
        return false;
    }
    
    public boolean bind() {
        if (this.id != 0) {
            GL11.glBindTexture(3553, this.id);
            return true;
        }
        return false;
    }
    
    @Override
    public Image getImage(final int x, final int y, final int width, final int height, final Color tintColor, final boolean tiled, final Rotation rotation) {
        if (x < 0 || x >= this.getWidth()) {
            throw new IllegalArgumentException("x");
        }
        if (y < 0 || y >= this.getHeight()) {
            throw new IllegalArgumentException("y");
        }
        if (x + Math.abs(width) > this.getWidth()) {
            throw new IllegalArgumentException("width");
        }
        if (y + Math.abs(height) > this.getHeight()) {
            throw new IllegalArgumentException("height");
        }
        if (rotation != Rotation.NONE || (tiled && (width < 0 || height < 0))) {
            return (Image)new TextureAreaRotated(this, x, y, width, height, tintColor, tiled, rotation);
        }
        if (tiled) {
            return (Image)new TextureAreaTiled(this, x, y, width, height, tintColor);
        }
        return (Image)new TextureArea(this, x, y, width, height, tintColor);
    }
    
    @Override
    public MouseCursor createCursor(final int x, final int y, final int width, final int height, final int hotSpotX, final int hotSpotY, final Image imageRef) {
        if (this.renderer.isUseSWMouseCursors() || imageRef != null) {
            return new SWCursor(this, x, y, width, height, hotSpotX, hotSpotY, imageRef);
        }
        if (this.texData != null) {
            final LWJGLCursor cursor = new LWJGLCursor(this.texData, this.texDataFmt, this.texDataFmt.getPixelSize() * this.width, x, y, width, height, hotSpotX, hotSpotY);
            if (this.cursors == null) {
                this.cursors = new ArrayList<LWJGLCursor>();
            }
            this.cursors.add(cursor);
            return (MouseCursor)cursor;
        }
        return null;
    }
    
    @Override
    public void themeLoadingDone() {
    }
    
    static int roundUpPOT(final int value) {
        return 1 << 32 - Integer.numberOfLeadingZeros(value - 1);
    }
    
    public enum Format
    {
        ALPHA(6406, 32828, PNGDecoder.Format.ALPHA), 
        LUMINANCE(6409, 32832, PNGDecoder.Format.LUMINANCE), 
        LUMINANCE_ALPHA(6410, 32837, PNGDecoder.Format.LUMINANCE_ALPHA), 
        RGB(6407, 32849, PNGDecoder.Format.RGB), 
        RGB_SMALL(6407, 32855, PNGDecoder.Format.RGB), 
        RGBA(6408, 32856, PNGDecoder.Format.RGBA), 
        BGRA(32993, 32856, PNGDecoder.Format.BGRA), 
        ABGR(32768, 32856, PNGDecoder.Format.ABGR), 
        COLOR(-1, -1, (PNGDecoder.Format)null);
        
        final int glFormat;
        final int glInternalFormat;
        final PNGDecoder.Format pngFormat;
        
        private Format(final int fmt, final int ifmt, final PNGDecoder.Format pf) {
            this.glFormat = fmt;
            this.glInternalFormat = ifmt;
            this.pngFormat = pf;
        }
        
        public int getPixelSize() {
            return this.pngFormat.getNumComponents();
        }
        
        public PNGDecoder.Format getPngFormat() {
            return this.pngFormat;
        }
    }
    
    public enum Filter
    {
        NEAREST(9728), 
        LINEAR(9729);
        
        final int glValue;
        
        private Filter(final int value) {
            this.glValue = value;
        }
    }
}
