package de.matthiasmann.twl.renderer.lwjgl;

import org.lwjgl.opengl.*;

public class RenderScale
{
    public static int scale;
    
    public static void doscale() {
        GL11.glPushMatrix();
        GL11.glScalef((float)RenderScale.scale, (float)RenderScale.scale, (float)RenderScale.scale);
    }
    
    public static void descale() {
        GL11.glPopMatrix();
    }
    
    static {
        RenderScale.scale = 2;
    }
}
