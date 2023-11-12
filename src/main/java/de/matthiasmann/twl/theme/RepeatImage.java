package de.matthiasmann.twl.theme;

import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.AnimationState;

class RepeatImage implements Image, HasBorder, SupportsDrawRepeat
{
    private final Image base;
    private final Border border;
    private final boolean repeatX;
    private final boolean repeatY;
    private final SupportsDrawRepeat sdr;

    RepeatImage(final Image base, final Border border, final boolean repeatX, final boolean repeatY) {
        assert repeatX || repeatY;
        this.base = base;
        this.border = border;
        this.repeatX = repeatX;
        this.repeatY = repeatY;
        if (base instanceof SupportsDrawRepeat) {
            this.sdr = (SupportsDrawRepeat)base;
        }
        else {
            this.sdr = (SupportsDrawRepeat)this;
        }
    }

    public int getWidth() {
        return this.base.getWidth();
    }

    public int getHeight() {
        return this.base.getHeight();
    }

    public void draw(final AnimationState as, final int x, final int y) {
        this.base.draw(as, x, y);
    }

    public void draw(final AnimationState as, final int x, final int y, final int width, final int height) {
        final int countX = this.repeatX ? Math.max(1, width / this.base.getWidth()) : 1;
        final int countY = this.repeatY ? Math.max(1, height / this.base.getHeight()) : 1;
        this.sdr.draw(as, x, y, width, height, countX, countY);
    }

    public void draw(final AnimationState as, final int x, int y, final int width, int height, final int repeatCountX, int repeatCountY) {
        while (repeatCountY > 0) {
            final int rowHeight = height / repeatCountY;
            int cx = 0;
            int xi = 0;
            while (xi < repeatCountX) {
                final int nx = ++xi * width / repeatCountX;
                this.base.draw(as, x + cx, y, nx - cx, rowHeight);
                cx = nx;
            }
            y += rowHeight;
            height -= rowHeight;
            --repeatCountY;
        }
    }

    public Border getBorder() {
        return this.border;
    }

    public Image createTintedVersion(final Color color) {
        return (Image)new RepeatImage(this.base.createTintedVersion(color), this.border, this.repeatX, this.repeatY);
    }
}
