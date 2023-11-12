package de.matthiasmann.twl.renderer.lwjgl;

import org.lwjgl.opengl.*;
import de.matthiasmann.twl.renderer.*;

public class LWJGLFontCache implements FontCache
{
    private final LWJGLRenderer renderer;
    private final LWJGLFont font;
    private int id;
    private int width;
    private int height;
    private int[] multiLineInfo;
    private int numLines;
    
    LWJGLFontCache(final LWJGLRenderer renderer, final LWJGLFont font) {
        this.renderer = renderer;
        this.font = font;
        this.id = GL11.glGenLists(1);
    }
    
    public void draw(final AnimationState as, final int x, final int y) {
        if (this.id != 0) {
            final LWJGLFont.FontState fontState = this.font.evalFontState(as);
            this.renderer.tintStack.setColor(fontState.color);
            GL11.glPushMatrix();
            GL11.glTranslatef((float)(x + fontState.offsetX), (float)(y + fontState.offsetY), 0.0f);
            GL11.glCallList(this.id);
            if (fontState.style != 0) {
                if (this.numLines > 0) {
                    this.font.drawLines(fontState, 0, 0, this.multiLineInfo, this.numLines);
                }
                else {
                    this.font.drawLine(fontState, 0, 0, this.width);
                }
            }
            GL11.glPopMatrix();
        }
    }
    
    public void destroy() {
        if (this.id != 0) {
            GL11.glDeleteLists(this.id, 1);
            this.id = 0;
        }
    }
    
    boolean startCompile() {
        if (this.id != 0) {
            GL11.glNewList(this.id, 4864);
            this.numLines = 0;
            return true;
        }
        return false;
    }
    
    void endCompile(final int width, final int height) {
        GL11.glEndList();
        this.width = width;
        this.height = height;
    }
    
    int[] getMultiLineInfo(final int numLines) {
        if (this.multiLineInfo == null || this.multiLineInfo.length < numLines) {
            this.multiLineInfo = new int[numLines];
        }
        this.numLines = numLines;
        return this.multiLineInfo;
    }
    
    public int getHeight() {
        return this.height;
    }
    
    public int getWidth() {
        return this.width;
    }
}
