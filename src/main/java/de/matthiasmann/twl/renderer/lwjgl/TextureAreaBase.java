package de.matthiasmann.twl.renderer.lwjgl;

import org.lwjgl.opengl.*;

public class TextureAreaBase
{
    protected final float tx0;
    protected final float ty0;
    protected final float tx1;
    protected final float ty1;
    protected final short width;
    protected final short height;
    
    TextureAreaBase(final int x, final int y, int width, int height, final float texWidth, final float texHeight) {
        this.width = (short)Math.abs(width);
        this.height = (short)Math.abs(height);
        float fx = (float)x;
        float fy = (float)y;
        if (width == 1 || width == -1) {
            fx += 0.5f;
            width = 0;
        }
        else if (width < 0) {
            fx -= width;
        }
        if (height == 1 || height == -1) {
            fy += 0.5f;
            height = 0;
        }
        else if (height < 0) {
            fy -= height;
        }
        this.tx0 = fx / texWidth;
        this.ty0 = fy / texHeight;
        this.tx1 = this.tx0 + width / texWidth;
        this.ty1 = this.ty0 + height / texHeight;
    }
    
    TextureAreaBase(final TextureAreaBase src) {
        this.tx0 = src.tx0;
        this.ty0 = src.ty0;
        this.tx1 = src.tx1;
        this.ty1 = src.ty1;
        this.width = src.width;
        this.height = src.height;
    }
    
    public int getWidth() {
        return this.width;
    }
    
    public int getHeight() {
        return this.height;
    }
    
    void drawQuad(final int x, final int y, final int w, final int h) {
        GL11.glTexCoord2f(this.tx0, this.ty0);
        GL11.glVertex2i(x, y);
        GL11.glTexCoord2f(this.tx0, this.ty1);
        GL11.glVertex2i(x, y + h);
        GL11.glTexCoord2f(this.tx1, this.ty1);
        GL11.glVertex2i(x + w, y + h);
        GL11.glTexCoord2f(this.tx1, this.ty0);
        GL11.glVertex2i(x + w, y);
    }
}
