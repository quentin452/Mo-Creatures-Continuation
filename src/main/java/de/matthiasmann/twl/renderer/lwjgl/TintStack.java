package de.matthiasmann.twl.renderer.lwjgl;

import de.matthiasmann.twl.*;
import org.lwjgl.opengl.*;

public class TintStack
{
    private static final float ONE_OVER_255 = 0.003921569f;
    final TintStack prev;
    TintStack next;
    float r;
    float g;
    float b;
    float a;
    
    public TintStack() {
        this.prev = this;
        this.r = 0.003921569f;
        this.g = 0.003921569f;
        this.b = 0.003921569f;
        this.a = 0.003921569f;
    }
    
    private TintStack(final TintStack prev) {
        this.prev = prev;
    }
    
    public TintStack pushReset() {
        if (this.next == null) {
            this.next = new TintStack(this);
        }
        this.next.r = 0.003921569f;
        this.next.g = 0.003921569f;
        this.next.b = 0.003921569f;
        this.next.a = 0.003921569f;
        return this.next;
    }
    
    public TintStack push(final float r, final float g, final float b, final float a) {
        if (this.next == null) {
            this.next = new TintStack(this);
        }
        this.next.r = this.r * r;
        this.next.g = this.g * g;
        this.next.b = this.b * b;
        this.next.a = this.a * a;
        return this.next;
    }
    
    public TintStack push(final Color color) {
        return this.push(color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), color.getAlphaFloat());
    }
    
    public TintStack pop() {
        return this.prev;
    }
    
    public float getR() {
        return this.r;
    }
    
    public float getG() {
        return this.g;
    }
    
    public float getB() {
        return this.b;
    }
    
    public float getA() {
        return this.a;
    }
    
    public void setColor(final Color color) {
        GL11.glColor4f(this.r * color.getRed(), this.g * color.getGreen(), this.b * color.getBlue(), this.a * color.getAlpha());
    }
    
    public void setColor(final float r, final float g, final float b, final float a) {
        GL11.glColor4f(this.r * r, this.g * g, this.b * b, this.a * a);
    }
}
