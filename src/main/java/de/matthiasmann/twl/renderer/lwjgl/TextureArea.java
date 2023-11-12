package de.matthiasmann.twl.renderer.lwjgl;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.renderer.AnimationState;
import org.lwjgl.opengl.*;

public class TextureArea extends TextureAreaBase implements Image, SupportsDrawRepeat
{
    protected static final int REPEAT_CACHE_SIZE = 10;
    protected final LWJGLTexture texture;
    protected final Color tintColor;
    protected int repeatCacheID;

    public TextureArea(final LWJGLTexture texture, final int x, final int y, final int width, final int height, final Color tintColor) {
        super(x, y, width, height, (float)texture.getTexWidth(), (float)texture.getTexHeight());
        this.repeatCacheID = -1;
        this.texture = texture;
        this.tintColor = ((tintColor == null) ? Color.WHITE : tintColor);
    }

    TextureArea(final TextureArea src, final Color tintColor) {
        super(src);
        this.repeatCacheID = -1;
        this.texture = src.texture;
        this.tintColor = tintColor;
    }

    public void draw(final AnimationState as, final int x, final int y) {
        this.draw(as, x, y, this.width, this.height);
    }

    public void draw(final AnimationState as, final int x, final int y, final int w, final int h) {
        if (this.texture.bind(this.tintColor)) {
            GL11.glBegin(7);
            this.drawQuad(x, y, w, h);
            GL11.glEnd();
        }
    }

    public void draw(final AnimationState as, final int x, final int y, final int width, final int height, final int repeatCountX, final int repeatCountY) {
        if (this.texture.bind(this.tintColor)) {
            if (repeatCountX * this.width != width || repeatCountY * this.height != height) {
                this.drawRepeatSlow(x, y, width, height, repeatCountX, repeatCountY);
                return;
            }
            if (repeatCountX < 10 || repeatCountY < 10) {
                this.drawRepeat(x, y, repeatCountX, repeatCountY);
                return;
            }
            this.drawRepeatCached(x, y, repeatCountX, repeatCountY);
        }
    }

    private void drawRepeatSlow(final int x, int y, final int width, int height, final int repeatCountX, int repeatCountY) {
        GL11.glBegin(7);
        while (repeatCountY > 0) {
            final int rowHeight = height / repeatCountY;
            int cx = 0;
            int xi = 0;
            while (xi < repeatCountX) {
                final int nx = ++xi * width / repeatCountX;
                this.drawQuad(x + cx, y, nx - cx, rowHeight);
                cx = nx;
            }
            y += rowHeight;
            height -= rowHeight;
            --repeatCountY;
        }
        GL11.glEnd();
    }

    protected void drawRepeat(final int x, int y, final int repeatCountX, int repeatCountY) {
        final int w = this.width;
        final int h = this.height;
        GL11.glBegin(7);
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
                GL11.glTranslatef((float)(this.width * 10), 0.0f, 0.0f);
                GL11.glCallList(this.repeatCacheID);
            }
            GL11.glPopMatrix();
            repeatCountY -= 10;
            y += this.height * 10;
        } while (repeatCountY >= 10);
        if (repeatCountY > 0) {
            this.drawRepeat(x, y, repeatsByCacheX, repeatCountY);
        }
    }

    protected void createRepeatCache() {
        this.repeatCacheID = GL11.glGenLists(1);
        this.texture.renderer.textureAreas.add(this);
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
        return (Image)new TextureArea(this, newTintColor);
    }
}
