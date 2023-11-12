package de.matthiasmann.twl.renderer.lwjgl;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.renderer.AnimationState;
import org.lwjgl.opengl.*;

public class TextureAreaRotated implements Image
{
    protected static final int REPEAT_CACHE_SIZE = 10;
    private final LWJGLTexture texture;
    private final Color tintColor;
    private final float txTL;
    private final float tyTL;
    private final float txTR;
    private final float tyTR;
    private final float txBL;
    private final float tyBL;
    private final float txBR;
    private final float tyBR;
    private final char width;
    private final char height;
    private final boolean tiled;
    protected int repeatCacheID;

    public TextureAreaRotated(final LWJGLTexture texture, final int x, final int y, int width, int height, final Color tintColor, final boolean tiled, final Texture.Rotation rotation) {
        this.repeatCacheID = -1;
        if (rotation == Texture.Rotation.CLOCKWISE_90 || rotation == Texture.Rotation.CLOCKWISE_270) {
            this.width = (char)Math.abs(height);
            this.height = (char)Math.abs(width);
        }
        else {
            this.width = (char)Math.abs(width);
            this.height = (char)Math.abs(height);
        }
        float fx = (float)x;
        float fy = (float)y;
        if (width == 1) {
            fx += 0.5f;
            width = 0;
        }
        else if (width < -1) {
            fx -= width + 1;
        }
        if (height == 1) {
            fy += 0.5f;
            height = 0;
        }
        else if (height < -1) {
            fy -= height + 1;
        }
        final float texWidth = (float)texture.getTexWidth();
        final float texHeight = (float)texture.getTexHeight();
        final float tx0 = fx / texWidth;
        final float ty0 = fy / texHeight;
        final float tx2 = tx0 + width / texWidth;
        final float ty2 = ty0 + height / texHeight;
        switch (rotation) {
            default: {
                final float n = tx0;
                this.txBL = n;
                this.txTL = n;
                final float n2 = tx2;
                this.txBR = n2;
                this.txTR = n2;
                final float n3 = ty0;
                this.tyTR = n3;
                this.tyTL = n3;
                final float n4 = ty2;
                this.tyBR = n4;
                this.tyBL = n4;
                break;
            }
            case CLOCKWISE_90: {
                this.txTL = tx0;
                this.tyTL = ty2;
                this.txTR = tx0;
                this.tyTR = ty0;
                this.txBL = tx2;
                this.tyBL = ty2;
                this.txBR = tx2;
                this.tyBR = ty0;
                break;
            }
            case CLOCKWISE_180: {
                this.txTL = tx2;
                this.tyTL = ty2;
                this.txTR = tx0;
                this.tyTR = ty2;
                this.txBL = tx2;
                this.tyBL = ty0;
                this.txBR = tx0;
                this.tyBR = ty0;
                break;
            }
            case CLOCKWISE_270: {
                this.txTL = tx2;
                this.tyTL = ty0;
                this.txTR = tx2;
                this.tyTR = ty2;
                this.txBL = tx0;
                this.tyBL = ty0;
                this.txBR = tx0;
                this.tyBR = ty2;
                break;
            }
        }
        this.texture = texture;
        this.tintColor = ((tintColor == null) ? Color.WHITE : tintColor);
        this.tiled = tiled;
    }

    TextureAreaRotated(final TextureAreaRotated src, final Color tintColor) {
        this.repeatCacheID = -1;
        this.txTL = src.txTL;
        this.tyTL = src.tyTL;
        this.txTR = src.txTR;
        this.tyTR = src.tyTR;
        this.txBL = src.txBL;
        this.tyBL = src.tyBL;
        this.txBR = src.txBR;
        this.tyBR = src.tyBR;
        this.width = src.width;
        this.height = src.height;
        this.texture = src.texture;
        this.tiled = src.tiled;
        this.tintColor = tintColor;
    }

    public int getHeight() {
        return this.height;
    }

    public int getWidth() {
        return this.width;
    }

    public void draw(final AnimationState as, final int x, final int y) {
        this.draw(as, x, y, this.width, this.height);
    }

    public void draw(final AnimationState as, final int x, final int y, final int w, final int h) {
        if (this.texture.bind(this.tintColor)) {
            if (this.tiled) {
                this.drawTiled(x, y, w, h);
            }
            else {
                GL11.glBegin(7);
                this.drawQuad(x, y, w, h);
                GL11.glEnd();
            }
        }
    }

    private void drawRepeat(final int x, int y, final int repeatCountX, int repeatCountY) {
        GL11.glBegin(7);
        final int w = this.width;
        final int h = this.height;
        while (repeatCountY-- > 0) {
            int curX = x;
            int cntX = repeatCountX;
            while (cntX-- > 0) {
                this.drawQuad(curX, y, w, h);
                curX += w;
            }
            y += h;
        }
        GL11.glEnd();
    }

    private void drawTiled(final int x, final int y, final int w, final int h) {
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

    protected void drawRepeatCached(final int x, int y, final int repeatCountX, int repeatCountY) {
        if (this.repeatCacheID < 0) {
            this.createRepeatCache();
        }
        final int cacheBlocksX = repeatCountX / 10;
        final int repeatsByCacheX = cacheBlocksX * 10;
        if (repeatCountX > repeatsByCacheX) {
            this.drawRepeat(x + this.width * repeatsByCacheX, y, repeatCountX - repeatsByCacheX, repeatCountY);
        }
        do {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)x, (float)y, 0.0f);
            GL11.glCallList(this.repeatCacheID);
            for (int i = 1; i < cacheBlocksX; ++i) {
                GL11.glTranslatef((float)(this.width * '\n'), 0.0f, 0.0f);
                GL11.glCallList(this.repeatCacheID);
            }
            GL11.glPopMatrix();
            repeatCountY -= 10;
            y += this.height * '\n';
        } while (repeatCountY >= 10);
        if (repeatCountY > 0) {
            this.drawRepeat(x, y, repeatsByCacheX, repeatCountY);
        }
    }

    private void drawClipped(final int x, int y, final int width, final int height, final int repeatCountX, int repeatCountY) {
        final float ctxTL = this.txTL;
        final float ctyTL = this.tyTL;
        float ctxTR = this.txTR;
        float ctyTR = this.tyTR;
        float ctxBL = this.txBL;
        float ctyBL = this.tyBL;
        float ctxBR = this.txBR;
        float ctyBR = this.tyBR;
        if (this.width > '\u0001') {
            final float f = width / (float)this.width;
            ctxTR = ctxTL + (ctxTR - ctxTL) * f;
            ctyTR = ctyTL + (ctyTR - ctyTL) * f;
            ctxBR = ctxBL + (ctxBR - ctxBL) * f;
            ctyBR = ctyBL + (ctyBR - ctyBL) * f;
        }
        if (this.height > '\u0001') {
            final float f = height / (float)this.height;
            ctxBL = ctxTL + (ctxBL - ctxTL) * f;
            ctyBL = ctyTL + (ctyBL - ctyTL) * f;
            ctxBR = ctxTR + (ctxBR - ctxTR) * f;
            ctyBR = ctyTR + (ctyBR - ctyTR) * f;
        }
        while (repeatCountY-- > 0) {
            final int y2 = y + height;
            int x2 = x;
            int cx = repeatCountX;
            while (cx-- > 0) {
                final int x3 = x2 + width;
                GL11.glTexCoord2f(ctxTL, ctyTL);
                GL11.glVertex2i(x2, y);
                GL11.glTexCoord2f(ctxBL, ctyBL);
                GL11.glVertex2i(x2, y2);
                GL11.glTexCoord2f(ctxBR, ctyBR);
                GL11.glVertex2i(x3, y2);
                GL11.glTexCoord2f(ctxTR, ctyTR);
                GL11.glVertex2i(x3, y);
                x2 = x3;
            }
            y = y2;
        }
    }

    private void drawQuad(final int x, final int y, final int w, final int h) {
        GL11.glTexCoord2f(this.txTL, this.tyTL);
        GL11.glVertex2i(x, y);
        GL11.glTexCoord2f(this.txBL, this.tyBL);
        GL11.glVertex2i(x, y + h);
        GL11.glTexCoord2f(this.txBR, this.tyBR);
        GL11.glVertex2i(x + w, y + h);
        GL11.glTexCoord2f(this.txTR, this.tyTR);
        GL11.glVertex2i(x + w, y);
    }

    private void createRepeatCache() {
        this.repeatCacheID = GL11.glGenLists(1);
        this.texture.renderer.rotatedTextureAreas.add(this);
        GL11.glNewList(this.repeatCacheID, 4864);
        this.drawRepeat(0, 0, 10, 10);
        GL11.glEndList();
    }

    void destroyRepeatCache() {
        GL11.glDeleteLists(this.repeatCacheID, 1);
        this.repeatCacheID = -1;
    }

    public Image createTintedVersion(final Color color) {
        if (color == null) {
            throw new NullPointerException("color");
        }
        final Color newTintColor = this.tintColor.multiply(color);
        if (newTintColor.equals((Object)this.tintColor)) {
            return (Image)this;
        }
        return (Image)new TextureAreaRotated(this, newTintColor);
    }
}
