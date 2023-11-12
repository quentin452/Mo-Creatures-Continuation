package de.matthiasmann.twl.renderer.lwjgl;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.AnimationState;
import org.lwjgl.opengl.*;
import java.nio.*;
import de.matthiasmann.twl.renderer.*;

public class LWJGLDynamicImage extends TextureAreaBase implements DynamicImage
{
    private final LWJGLRenderer renderer;
    private final int target;
    private final Color tintColor;
    private int id;

    public LWJGLDynamicImage(final LWJGLRenderer renderer, final int target, final int id, final int width, final int height, final int texWidth, final int texHeight, final Color tintColor) {
        super(0, 0, width, height, (target == 3553) ? ((float)texWidth) : 1.0f, (target == 3553) ? ((float)texHeight) : 1.0f);
        this.renderer = renderer;
        this.tintColor = tintColor;
        this.target = target;
        this.id = id;
    }

    LWJGLDynamicImage(final LWJGLDynamicImage src, final Color tintColor) {
        super(src);
        this.renderer = src.renderer;
        this.tintColor = tintColor;
        this.target = src.target;
        this.id = src.id;
    }

    public void destroy() {
        if (this.id != 0) {
            GL11.glDeleteTextures(this.id);
            this.renderer.dynamicImages.remove(this);
        }
    }

    public void update(final ByteBuffer data, final DynamicImage.Format format) {
        this.update(0, 0, this.width, this.height, data, this.width * 4, format);
    }

    public void update(final ByteBuffer data, final int stride, final DynamicImage.Format format) {
        this.update(0, 0, this.width, this.height, data, stride, format);
    }

    public void update(final int xoffset, final int yoffset, final int width, final int height, final ByteBuffer data, final DynamicImage.Format format) {
        this.update(xoffset, yoffset, width, height, data, width * 4, format);
    }

    public void update(final int xoffset, final int yoffset, final int width, final int height, final ByteBuffer data, final int stride, final DynamicImage.Format format) {
        if (xoffset < 0 || yoffset < 0 || this.getWidth() <= 0 || this.getHeight() <= 0) {
            throw new IllegalArgumentException("Negative offsets or size <= 0");
        }
        if (xoffset >= this.getWidth() || yoffset >= this.getHeight()) {
            throw new IllegalArgumentException("Offset outside of texture");
        }
        if (width > this.getWidth() - xoffset || height > this.getHeight() - yoffset) {
            throw new IllegalArgumentException("Rectangle outside of texture");
        }
        if (data == null) {
            throw new NullPointerException("data");
        }
        if (format == null) {
            throw new NullPointerException("format");
        }
        if (stride < 0 || (stride & 0x3) != 0x0) {
            throw new IllegalArgumentException("stride");
        }
        if (stride < width * 4) {
            throw new IllegalArgumentException("stride too short for width");
        }
        if (data.remaining() < stride * (height - 1) + width * 4) {
            throw new IllegalArgumentException("Not enough data remaining in the buffer");
        }
        final int glFormat = (format == DynamicImage.Format.RGBA) ? 6408 : 32993;
        this.bind();
        GL11.glPixelStorei(3314, stride / 4);
        GL11.glTexSubImage2D(this.target, 0, xoffset, yoffset, width, height, glFormat, 5121, data);
        GL11.glPixelStorei(3314, 0);
    }

    public Image createTintedVersion(final Color color) {
        if (color == null) {
            throw new NullPointerException("color");
        }
        final Color newTintColor = this.tintColor.multiply(color);
        if (newTintColor.equals((Object)this.tintColor)) {
            return (Image)this;
        }
        return (Image)new LWJGLDynamicImage(this, newTintColor);
    }

    public void draw(final AnimationState as, final int x, final int y) {
        this.draw(as, x, y, this.width, this.height);
    }

    public void draw(final AnimationState as, final int x, final int y, final int width, final int height) {
        this.bind();
        this.renderer.tintStack.setColor(this.tintColor);
        if (this.target != 3553) {
            GL11.glDisable(3553);
            GL11.glEnable(this.target);
        }
        GL11.glBegin(7);
        this.drawQuad(x, y, width, height);
        GL11.glEnd();
        if (this.target != 3553) {
            GL11.glDisable(this.target);
            GL11.glEnable(3553);
        }
    }

    private void bind() {
        if (this.id == 0) {
            throw new IllegalStateException("destroyed");
        }
        GL11.glBindTexture(this.target, this.id);
    }
}
