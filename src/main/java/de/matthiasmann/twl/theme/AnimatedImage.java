package de.matthiasmann.twl.theme;

import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.AnimationState;

public class AnimatedImage implements Image, HasBorder
{
    final Renderer renderer;
    final Element root;
    final AnimationState.StateKey timeSource;
    final Border border;
    final float r;
    final float g;
    final float b;
    final float a;
    final int width;
    final int height;
    final int frozenTime;

    AnimatedImage(final Renderer renderer, final Element root, final String timeSource, final Border border, final Color tintColor, final int frozenTime) {
        this.renderer = renderer;
        this.root = root;
        this.timeSource = AnimationState.StateKey.get(timeSource);
        this.border = border;
        this.r = tintColor.getRedFloat();
        this.g = tintColor.getGreenFloat();
        this.b = tintColor.getBlueFloat();
        this.a = tintColor.getAlphaFloat();
        this.width = root.getWidth();
        this.height = root.getHeight();
        this.frozenTime = frozenTime;
    }

    AnimatedImage(final AnimatedImage src, final Color tintColor) {
        this.renderer = src.renderer;
        this.root = src.root;
        this.timeSource = src.timeSource;
        this.border = src.border;
        this.r = src.r * tintColor.getRedFloat();
        this.g = src.g * tintColor.getGreenFloat();
        this.b = src.b * tintColor.getBlueFloat();
        this.a = src.a * tintColor.getAlphaFloat();
        this.width = src.width;
        this.height = src.height;
        this.frozenTime = src.frozenTime;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void draw(final AnimationState as, final int x, final int y) {
        this.draw(as, x, y, this.width, this.height);
    }

    public void draw(final AnimationState as, final int x, final int y, final int width, final int height) {
        int time = 0;
        if (as != null) {
            if (this.frozenTime < 0 || as.getShouldAnimateState(this.timeSource)) {
                time = as.getAnimationTime(this.timeSource);
            }
            else {
                time = this.frozenTime;
            }
        }
        this.root.render(time, null, x, y, width, height, this, as);
    }

    public Border getBorder() {
        return this.border;
    }

    public Image createTintedVersion(final Color color) {
        return (Image)new AnimatedImage(this, color);
    }

    abstract static class Element
    {
        int duration;

        abstract int getWidth();

        abstract int getHeight();

        abstract Img getFirstImg();

        abstract void render(final int p0, final Img p1, final int p2, final int p3, final int p4, final int p5, final AnimatedImage p6, final AnimationState p7);
    }

    static class Img extends Element
    {
        final Image image;
        final float r;
        final float g;
        final float b;
        final float a;
        final float zoomX;
        final float zoomY;
        final float zoomCenterX;
        final float zoomCenterY;

        Img(final int duration, final Image image, final Color tintColor, final float zoomX, final float zoomY, final float zoomCenterX, final float zoomCenterY) {
            if (duration < 0) {
                throw new IllegalArgumentException("duration");
            }
            this.duration = duration;
            this.image = image;
            this.r = tintColor.getRedFloat();
            this.g = tintColor.getGreenFloat();
            this.b = tintColor.getBlueFloat();
            this.a = tintColor.getAlphaFloat();
            this.zoomX = zoomX;
            this.zoomY = zoomY;
            this.zoomCenterX = zoomCenterX;
            this.zoomCenterY = zoomCenterY;
        }

        @Override
        int getWidth() {
            return this.image.getWidth();
        }

        @Override
        int getHeight() {
            return this.image.getHeight();
        }

        @Override
        Img getFirstImg() {
            return this;
        }

        @Override
        void render(final int time, final Img next, final int x, final int y, final int width, final int height, final AnimatedImage ai, final AnimationState as) {
            float rr = this.r;
            float gg = this.g;
            float bb = this.b;
            float aa = this.a;
            float zx = this.zoomX;
            float zy = this.zoomY;
            float cx = this.zoomCenterX;
            float cy = this.zoomCenterY;
            if (next != null) {
                final float t = time / (float)this.duration;
                rr = blend(rr, next.r, t);
                gg = blend(gg, next.g, t);
                bb = blend(bb, next.b, t);
                aa = blend(aa, next.a, t);
                zx = blend(zx, next.zoomX, t);
                zy = blend(zy, next.zoomY, t);
                cx = blend(cx, next.zoomCenterX, t);
                cy = blend(cy, next.zoomCenterY, t);
            }
            ai.renderer.pushGlobalTintColor(rr * ai.r, gg * ai.g, bb * ai.b, aa * ai.a);
            try {
                final int zWidth = (int)(width * zx);
                final int zHeight = (int)(height * zy);
                this.image.draw(as, x + (int)((width - zWidth) * cx), y + (int)((height - zHeight) * cy), zWidth, zHeight);
            }
            finally {
                ai.renderer.popGlobalTintColor();
            }
        }

        private static float blend(final float a, final float b, final float t) {
            return a + (b - a) * t;
        }
    }

    static class Repeat extends Element
    {
        final Element[] children;
        final int repeatCount;
        final int singleDuration;

        Repeat(final Element[] children, final int repeatCount) {
            this.children = children;
            this.repeatCount = repeatCount;
            assert repeatCount >= 0;
            assert children.length > 0;
            for (final Element e : children) {
                this.duration += e.duration;
            }
            this.singleDuration = this.duration;
            if (repeatCount == 0) {
                this.duration = Integer.MAX_VALUE;
            }
            else {
                this.duration *= repeatCount;
            }
        }

        @Override
        int getHeight() {
            int tmp = 0;
            for (final Element e : this.children) {
                tmp = Math.max(tmp, e.getHeight());
            }
            return tmp;
        }

        @Override
        int getWidth() {
            int tmp = 0;
            for (final Element e : this.children) {
                tmp = Math.max(tmp, e.getWidth());
            }
            return tmp;
        }

        @Override
        Img getFirstImg() {
            return this.children[0].getFirstImg();
        }

        @Override
        void render(int time, Img next, final int x, final int y, final int width, final int height, final AnimatedImage ai, final AnimationState as) {
            if (this.singleDuration == 0) {
                return;
            }
            int iteration = 0;
            if (this.repeatCount == 0) {
                time %= this.singleDuration;
            }
            else {
                iteration = time / this.singleDuration;
                time -= Math.min(iteration, this.repeatCount - 1) * this.singleDuration;
            }
            Element e = null;
            int i = 0;
            while (i < this.children.length) {
                e = this.children[i];
                if (time < e.duration && e.duration > 0) {
                    if (i + 1 < this.children.length) {
                        next = this.children[i + 1].getFirstImg();
                        break;
                    }
                    if (this.repeatCount == 0 || iteration + 1 < this.repeatCount) {
                        next = this.getFirstImg();
                        break;
                    }
                    break;
                }
                else {
                    time -= e.duration;
                    ++i;
                }
            }
            if (e != null) {
                e.render(time, next, x, y, width, height, ai, as);
            }
        }
    }
}
