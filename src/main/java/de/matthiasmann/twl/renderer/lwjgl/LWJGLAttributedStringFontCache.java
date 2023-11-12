package de.matthiasmann.twl.renderer.lwjgl;

import de.matthiasmann.twl.renderer.*;
import java.nio.*;
import org.lwjgl.opengl.*;

class LWJGLAttributedStringFontCache extends VertexArray implements AttributedStringFontCache
{
    final LWJGLRenderer renderer;
    final BitmapFont font;
    int width;
    int height;
    private Run[] runs;
    private int numRuns;
    
    LWJGLAttributedStringFontCache(final LWJGLRenderer renderer, final BitmapFont font) {
        this.renderer = renderer;
        this.font = font;
        this.runs = new Run[8];
    }
    
    @Override
    public FloatBuffer allocate(final int maxGlyphs) {
        this.numRuns = 0;
        return super.allocate(maxGlyphs);
    }
    
    Run addRun() {
        if (this.runs.length == this.numRuns) {
            this.grow();
        }
        Run run = this.runs[this.numRuns];
        if (run == null) {
            run = new Run();
            this.runs[this.numRuns] = run;
        }
        ++this.numRuns;
        return run;
    }
    
    private void grow() {
        final Run[] newRuns = new Run[this.numRuns * 2];
        System.arraycopy(this.runs, 0, newRuns, 0, this.numRuns);
        this.runs = newRuns;
    }
    
    public void destroy() {
    }
    
    public int getWidth() {
        return this.width;
    }
    
    public int getHeight() {
        return this.height;
    }
    
    public void draw(final int x, final int y) {
        if (this.font.bind()) {
            this.bind();
            GL11.glPushMatrix();
            GL11.glTranslatef((float)x, (float)y, 0.0f);
            final TintStack tintStack = this.renderer.tintStack;
            try {
                int idx = 0;
                for (int i = 0; i < this.numRuns; ++i) {
                    final Run run = this.runs[i];
                    final LWJGLFont.FontState state = run.state;
                    final int numVertices = run.numVertices;
                    tintStack.setColor(state.color);
                    if (numVertices > 0) {
                        this.drawVertices(idx, numVertices);
                        idx += numVertices;
                    }
                    if (state.style != 0) {
                        this.drawLines(run);
                    }
                }
            }
            finally {
                GL11.glPopMatrix();
                this.unbind();
            }
        }
    }
    
    private void drawLines(final Run run) {
        final LWJGLFont.FontState state = run.state;
        if ((state.style & 0x1) != 0x0) {
            this.font.drawLine(run.x, run.y + this.font.getBaseLine() + state.underlineOffset, run.xend);
        }
        if ((state.style & 0x2) != 0x0) {
            this.font.drawLine(run.x, run.y + this.font.getLineHeight() / 2, run.xend);
        }
    }
    
    static class Run
    {
        LWJGLFont.FontState state;
        int numVertices;
        int x;
        int xend;
        int y;
    }
}
