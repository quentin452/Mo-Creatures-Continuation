package de.matthiasmann.twl.renderer.lwjgl;

import java.nio.*;
import org.lwjgl.*;
import org.lwjgl.opengl.*;

public class VertexArray
{
    private FloatBuffer va;
    
    public FloatBuffer allocate(final int maxQuads) {
        final int capacity = 16 * maxQuads;
        if (this.va == null || this.va.capacity() < capacity) {
            this.va = BufferUtils.createFloatBuffer(capacity);
        }
        this.va.clear();
        return this.va;
    }
    
    public void bind() {
        this.va.position(2);
        GL11.glVertexPointer(2, 16, this.va);
        this.va.position(0);
        GL11.glTexCoordPointer(2, 16, this.va);
        GL11.glEnableClientState(32884);
        GL11.glEnableClientState(32888);
    }
    
    public void drawVertices(final int start, final int count) {
        GL11.glDrawArrays(7, start, count);
    }
    
    public void drawQuads(final int start, final int count) {
        GL11.glDrawArrays(7, start * 4, count * 4);
    }
    
    public void unbind() {
        GL11.glDisableClientState(32884);
        GL11.glDisableClientState(32888);
    }
}
