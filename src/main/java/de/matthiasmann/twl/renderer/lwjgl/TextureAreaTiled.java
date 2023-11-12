package de.matthiasmann.twl.renderer.lwjgl;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.AnimationState;
import org.lwjgl.opengl.*;
import de.matthiasmann.twl.renderer.*;

public class TextureAreaTiled extends TextureArea
{
    public TextureAreaTiled(final LWJGLTexture texture, final int x, final int y, final int width, final int height, final Color tintColor) {
        super(texture, x, y, width, height, tintColor);
    }

    TextureAreaTiled(final TextureAreaTiled src, final Color tintColor) {
        super((TextureArea)src, tintColor);
    }

    public void draw(final AnimationState as, final int x, final int y, final int w, final int h) {
        if (this.texture.bind(this.tintColor)) {
            final int repeatCountX = w / this.width;
            final int repeatCountY = h / this.height;
            if (repeatCountX < 10 || repeatCountY < 10) {
                this.drawRepeat(x, y, repeatCountX, repeatCountY);
            }
            else {
                this.drawRepeatCached(x, y, repeatCountX, repeatCountY);
            }
            final int drawnX = repeatCountX * this.width;
            final int drawnY = repeatCountY * this.height;
            final int restWidth = w - drawnX;
            final int restHeight = h - drawnY;
            if (restWidth > 0 || restHeight > 0) {
                GL11.glBegin(7);
                if (restWidth > 0 && repeatCountY > 0) {
                    this.drawClipped(x + drawnX, y, restWidth, this.height, 1, repeatCountY);
                }
                if (restHeight > 0) {
                    if (repeatCountX > 0) {
                        this.drawClipped(x, y + drawnY, this.width, restHeight, repeatCountX, 1);
                    }
                    if (restWidth > 0) {
                        this.drawClipped(x + drawnX, y + drawnY, restWidth, restHeight, 1, 1);
                    }
                }
                GL11.glEnd();
            }
        }
    }

    private void drawClipped(final int x, int y, final int width, final int height, final int repeatCountX, int repeatCountY) {
        final float ctx0 = this.tx0;
        final float cty0 = this.ty0;
        float ctx2 = this.tx1;
        float cty2 = this.ty1;
        if (this.width > 1) {
            ctx2 = ctx0 + width / (float)this.texture.getTexWidth();
        }
        if (this.height > 1) {
            cty2 = cty0 + height / (float)this.texture.getTexHeight();
        }
        while (repeatCountY-- > 0) {
            final int y2 = y + height;
            int x2 = x;
            int cx = repeatCountX;
            while (cx-- > 0) {
                final int x3 = x2 + width;
                GL11.glTexCoord2f(ctx0, cty0);
                GL11.glVertex2i(x2, y);
                GL11.glTexCoord2f(ctx0, cty2);
                GL11.glVertex2i(x2, y2);
                GL11.glTexCoord2f(ctx2, cty2);
                GL11.glVertex2i(x3, y2);
                GL11.glTexCoord2f(ctx2, cty0);
                GL11.glVertex2i(x3, y);
                x2 = x3;
            }
            y = y2;
        }
    }

    public Image createTintedVersion(final Color color) {
        if (color == null) {
            throw new NullPointerException("color");
        }
        final Color newTintColor = this.tintColor.multiply(color);
        if (newTintColor.equals((Object)this.tintColor)) {
            return (Image)this;
        }
        return (Image)new TextureAreaTiled(this, newTintColor);
    }
}
