package de.matthiasmann.twl.renderer.lwjgl;

import de.matthiasmann.twl.renderer.*;
import java.net.*;
import de.matthiasmann.twl.utils.*;
import org.lwjgl.opengl.*;
import org.lwjgl.*;
import java.io.*;
import java.nio.*;
import java.util.*;

public class LWJGLCacheContext implements CacheContext
{
    final LWJGLRenderer renderer;
    final HashMap<String, LWJGLTexture> textures;
    final HashMap<String, BitmapFont> fontCache;
    final ArrayList<LWJGLTexture> allTextures;
    boolean valid;
    
    protected LWJGLCacheContext(final LWJGLRenderer renderer) {
        this.renderer = renderer;
        this.textures = new HashMap<String, LWJGLTexture>();
        this.fontCache = new HashMap<String, BitmapFont>();
        this.allTextures = new ArrayList<LWJGLTexture>();
        this.valid = true;
    }
    
    LWJGLTexture loadTexture(final URL url, final LWJGLTexture.Format fmt, final LWJGLTexture.Filter filter) throws IOException {
        final String urlString = url.toString();
        Util.checkGLError();
        LWJGLTexture texture = this.textures.get(urlString);
        Util.checkGLError();
        if (texture == null) {
            texture = this.createTexture(url, fmt, filter, null);
            Util.checkGLError();
            this.textures.put(urlString, texture);
        }
        return texture;
    }
    
    LWJGLTexture createTexture(final URL textureUrl, LWJGLTexture.Format fmt, final LWJGLTexture.Filter filter, final TexturePostProcessing tpp) throws IOException {
        if (!this.valid) {
            throw new IllegalStateException("CacheContext already destroyed");
        }
        Util.checkGLError();
        final InputStream is = textureUrl.openStream();
        Util.checkGLError();
        try {
            Util.checkGLError();
            final PNGDecoder dec = new PNGDecoder(is);
            Util.checkGLError();
            fmt = decideTextureFormat(dec, fmt);
            Util.checkGLError();
            final int width = dec.getWidth();
            final int height = dec.getHeight();
            final int maxTextureSize = this.renderer.maxTextureSize;
            if (width > maxTextureSize || height > maxTextureSize) {
                throw new IOException("Texture size too large. Maximum supported texture by this system is " + maxTextureSize);
            }
            Util.checkGLError();
            if (GLContext.getCapabilities().GL_EXT_abgr) {
                Util.checkGLError();
                if (fmt == LWJGLTexture.Format.RGBA) {
                    Util.checkGLError();
                    fmt = LWJGLTexture.Format.ABGR;
                }
            }
            else if (fmt == LWJGLTexture.Format.ABGR) {
                Util.checkGLError();
                fmt = LWJGLTexture.Format.RGBA;
            }
            Util.checkGLError();
            final int stride = width * fmt.getPixelSize();
            Util.checkGLError();
            final ByteBuffer buf = BufferUtils.createByteBuffer(stride * height);
            Util.checkGLError();
            dec.decode(buf, stride, fmt.getPngFormat());
            Util.checkGLError();
            buf.flip();
            if (tpp != null) {
                Util.checkGLError();
                tpp.process(buf, stride, width, height, fmt);
                Util.checkGLError();
            }
            Util.checkGLError();
            final LWJGLTexture texture = new LWJGLTexture(this.renderer, width, height, buf, fmt, filter);
            this.allTextures.add(texture);
            return texture;
        }
        catch (IOException ex) {
            throw (IOException)new IOException("Unable to load PNG file: " + textureUrl).initCause(ex);
        }
        finally {
            try {
                is.close();
            }
            catch (IOException ex2) {}
        }
    }
    
    BitmapFont loadBitmapFont(final URL url) throws IOException {
        final String urlString = url.toString();
        BitmapFont bmFont = this.fontCache.get(urlString);
        if (bmFont == null) {
            bmFont = BitmapFont.loadFont(this.renderer, url);
            this.fontCache.put(urlString, bmFont);
        }
        return bmFont;
    }
    
    public boolean isValid() {
        return this.valid;
    }
    
    public void destroy() {
        try {
            for (final LWJGLTexture t : this.allTextures) {
                t.destroy();
            }
            for (final BitmapFont f : this.fontCache.values()) {
                f.destroy();
            }
        }
        finally {
            this.textures.clear();
            this.fontCache.clear();
            this.allTextures.clear();
            this.valid = false;
        }
    }
    
    private static LWJGLTexture.Format decideTextureFormat(final PNGDecoder decoder, LWJGLTexture.Format fmt) {
        if (fmt == LWJGLTexture.Format.COLOR) {
            fmt = autoColorFormat(decoder);
        }
        final PNGDecoder.Format pngFormat = decoder.decideTextureFormat(fmt.getPngFormat());
        if (fmt.pngFormat == pngFormat) {
            return fmt;
        }
        switch (pngFormat) {
            case ALPHA: {
                return LWJGLTexture.Format.ALPHA;
            }
            case LUMINANCE: {
                return LWJGLTexture.Format.LUMINANCE;
            }
            case LUMINANCE_ALPHA: {
                return LWJGLTexture.Format.LUMINANCE_ALPHA;
            }
            case RGB: {
                return LWJGLTexture.Format.RGB;
            }
            case RGBA: {
                return LWJGLTexture.Format.RGBA;
            }
            case BGRA: {
                return LWJGLTexture.Format.BGRA;
            }
            case ABGR: {
                return LWJGLTexture.Format.ABGR;
            }
            default: {
                throw new UnsupportedOperationException("PNGFormat not handled: " + pngFormat);
            }
        }
    }
    
    private static LWJGLTexture.Format autoColorFormat(final PNGDecoder decoder) {
        if (decoder.hasAlpha()) {
            if (decoder.isRGB()) {
                return LWJGLTexture.Format.ABGR;
            }
            return LWJGLTexture.Format.LUMINANCE_ALPHA;
        }
        else {
            if (decoder.isRGB()) {
                return LWJGLTexture.Format.ABGR;
            }
            return LWJGLTexture.Format.LUMINANCE;
        }
    }
}
