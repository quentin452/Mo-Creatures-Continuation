package de.matthiasmann.twl.renderer.lwjgl;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.renderer.AnimationState;
import org.lwjgl.opengl.*;

public class GradientImage implements Image
{
    private final LWJGLRenderer renderer;
    private final Gradient.Type type;
    private final Gradient.Wrap wrap;
    private final Gradient.Stop[] stops;
    private final Color tint;
    private final float endPos;

    GradientImage(final GradientImage src, final Color tint) {
        this.renderer = src.renderer;
        this.type = src.type;
        this.wrap = src.wrap;
        this.stops = src.stops;
        this.endPos = src.endPos;
        this.tint = tint;
    }

    public GradientImage(final LWJGLRenderer renderer, final Gradient gradient) {
        if (gradient == null) {
            throw new NullPointerException("gradient");
        }
        if (gradient.getNumStops() < 1) {
            throw new IllegalArgumentException("Need at least 1 stop for a gradient");
        }
        this.renderer = renderer;
        this.type = gradient.getType();
        this.tint = Color.WHITE;
        if (gradient.getNumStops() == 1) {
            final Color color = gradient.getStop(0).getColor();
            this.wrap = Gradient.Wrap.SCALE;
            this.stops = new Gradient.Stop[] { new Gradient.Stop(0.0f, color), new Gradient.Stop(1.0f, color) };
            this.endPos = 1.0f;
        }
        else if (gradient.getWrap() == Gradient.Wrap.MIRROR) {
            final int numStops = gradient.getNumStops();
            this.wrap = Gradient.Wrap.REPEAT;
            this.stops = new Gradient.Stop[numStops * 2 - 1];
            for (int i = 0; i < numStops; ++i) {
                this.stops[i] = gradient.getStop(i);
            }
            this.endPos = this.stops[numStops - 1].getPos() * 2.0f;
            int i = numStops;
            for (int j = numStops - 2; j >= 0; --j) {
                this.stops[i] = new Gradient.Stop(this.endPos - this.stops[j].getPos(), this.stops[j].getColor());
                ++i;
            }
        }
        else {
            this.wrap = gradient.getWrap();
            this.stops = gradient.getStops();
            this.endPos = this.stops[this.stops.length - 1].getPos();
        }
    }

    public Image createTintedVersion(final Color color) {
        return (Image)new GradientImage(this, this.tint.multiply(color));
    }

    private boolean isHorz() {
        return this.type == Gradient.Type.HORIZONTAL;
    }

    private int getLastPos() {
        return Math.round(this.stops[this.stops.length - 1].getPos());
    }

    public int getHeight() {
        return this.isHorz() ? 1 : this.getLastPos();
    }

    public int getWidth() {
        return this.isHorz() ? this.getLastPos() : 1;
    }

    public void draw(final AnimationState as, final int x, final int y) {
        if (this.isHorz()) {
            this.drawHorz(x, y, this.getLastPos(), 1);
        }
        else {
            this.drawVert(x, y, 1, this.getLastPos());
        }
    }

    public void draw(final AnimationState as, final int x, final int y, final int width, final int height) {
        if (this.isHorz()) {
            this.drawHorz(x, y, width, height);
        }
        else {
            this.drawVert(x, y, width, height);
        }
    }

    private void drawHorz(final int x, final int y, final int width, final int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        final TintStack tintStack = this.renderer.tintStack.push(this.tint);
        GL11.glDisable(3553);
        GL11.glBegin(8);
        if (this.wrap == Gradient.Wrap.SCALE) {
            for (final Gradient.Stop stop : this.stops) {
                tintStack.setColor(stop.getColor());
                final float pos = stop.getPos() * width / this.endPos;
                GL11.glVertex2f(x + pos, (float)y);
                GL11.glVertex2f(x + pos, (float)(y + height));
            }
        }
        else {
            float lastPos = 0.0f;
            float offset = 0.0f;
            Color lastColor = this.stops[0].getColor();
        Label_0292:
            do {
                for (final Gradient.Stop stop2 : this.stops) {
                    final float pos2 = stop2.getPos() + offset;
                    final Color color = stop2.getColor();
                    if (pos2 >= width) {
                        final float t = (width - lastPos) / (pos2 - lastPos);
                        setColor(tintStack, lastColor, color, t);
                        break Label_0292;
                    }
                    tintStack.setColor(color);
                    GL11.glVertex2f(x + pos2, (float)y);
                    GL11.glVertex2f(x + pos2, (float)(y + height));
                    lastPos = pos2;
                    lastColor = color;
                }
                offset += this.endPos;
            } while (this.wrap == Gradient.Wrap.REPEAT);
            GL11.glVertex2f((float)(x + width), (float)y);
            GL11.glVertex2f((float)(x + width), (float)(y + height));
        }
        GL11.glEnd();
        GL11.glEnable(3553);
    }

    private void drawVert(final int x, final int y, final int width, final int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        final TintStack tintStack = this.renderer.tintStack.push(this.tint);
        GL11.glDisable(3553);
        GL11.glBegin(8);
        if (this.wrap == Gradient.Wrap.SCALE) {
            for (final Gradient.Stop stop : this.stops) {
                tintStack.setColor(stop.getColor());
                final float pos = stop.getPos() * height / this.endPos;
                GL11.glVertex2f((float)x, y + pos);
                GL11.glVertex2f((float)(x + width), y + pos);
            }
        }
        else {
            float lastPos = 0.0f;
            float offset = 0.0f;
            Color lastColor = this.stops[0].getColor();
        Label_0293:
            do {
                for (final Gradient.Stop stop2 : this.stops) {
                    final float pos2 = stop2.getPos() + offset;
                    final Color color = stop2.getColor();
                    if (pos2 >= height) {
                        final float t = (height - lastPos) / (pos2 - lastPos);
                        setColor(tintStack, lastColor, color, t);
                        break Label_0293;
                    }
                    tintStack.setColor(color);
                    GL11.glVertex2f((float)x, y + pos2);
                    GL11.glVertex2f((float)(x + width), y + pos2);
                    lastPos = pos2;
                    lastColor = color;
                }
                offset += this.endPos;
            } while (this.wrap == Gradient.Wrap.REPEAT);
            GL11.glVertex2f((float)x, (float)(y + height));
            GL11.glVertex2f((float)(x + width), (float)(y + height));
        }
        GL11.glEnd();
        GL11.glEnable(3553);
    }

    private static void setColor(final TintStack tintStack, final Color a, final Color b, final float t) {
        tintStack.setColor(mix(a.getRed(), b.getRed(), t), mix(a.getGreen(), b.getGreen(), t), mix(a.getBlue(), b.getBlue(), t), mix(a.getAlpha(), b.getAlpha(), t));
    }

    private static float mix(final int a, final int b, final float t) {
        return a + (b - a) * t;
    }
}
