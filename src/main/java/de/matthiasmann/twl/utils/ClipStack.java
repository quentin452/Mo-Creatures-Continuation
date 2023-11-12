package de.matthiasmann.twl.utils;

import de.matthiasmann.twl.*;

public class ClipStack
{
    private Entry[] clipRects;
    private int numClipRects;
    
    public ClipStack() {
        this.clipRects = new Entry[8];
    }
    
    public void push(final int x, final int y, final int w, final int h) {
        final Entry tos = this.push();
        tos.setXYWH(x, y, w, h);
        this.intersect(tos);
    }
    
    public void push(final Rect rect) {
        if (rect == null) {
            throw new NullPointerException("rect");
        }
        final Entry tos = this.push();
        tos.set(rect);
        this.intersect(tos);
    }
    
    public void pushDisable() {
        final Entry rect = this.push();
        rect.disabled = true;
    }
    
    public void pop() {
        if (this.numClipRects == 0) {
            this.underflow();
        }
        --this.numClipRects;
    }
    
    public boolean isClipEmpty() {
        final Entry tos = this.clipRects[this.numClipRects - 1];
        return tos.isEmpty() && !tos.disabled;
    }
    
    public boolean getClipRect(final Rect rect) {
        if (this.numClipRects == 0) {
            return false;
        }
        final Entry tos = this.clipRects[this.numClipRects - 1];
        rect.set((Rect)tos);
        return !tos.disabled;
    }
    
    public int getStackSize() {
        return this.numClipRects;
    }
    
    public void clearStack() {
        this.numClipRects = 0;
    }
    
    protected Entry push() {
        if (this.numClipRects == this.clipRects.length) {
            this.grow();
        }
        Entry rect;
        if ((rect = this.clipRects[this.numClipRects]) == null) {
            rect = new Entry();
            this.clipRects[this.numClipRects] = rect;
        }
        rect.disabled = false;
        ++this.numClipRects;
        return rect;
    }
    
    protected void intersect(final Rect tos) {
        if (this.numClipRects > 1) {
            final Entry prev = this.clipRects[this.numClipRects - 2];
            if (!prev.disabled) {
                tos.intersect((Rect)prev);
            }
        }
    }
    
    private void grow() {
        final Entry[] newRects = new Entry[this.numClipRects * 2];
        System.arraycopy(this.clipRects, 0, newRects, 0, this.numClipRects);
        this.clipRects = newRects;
    }
    
    private void underflow() {
        throw new IllegalStateException("empty");
    }
    
    protected static class Entry extends Rect
    {
        boolean disabled;
    }
}
