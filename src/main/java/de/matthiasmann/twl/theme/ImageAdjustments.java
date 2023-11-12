package de.matthiasmann.twl.theme;

import de.matthiasmann.twl.renderer.AnimationState;
import de.matthiasmann.twl.utils.*;
import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.*;

class ImageAdjustments implements Image, HasBorder
{
    final Image image;
    final Border border;
    final Border inset;
    final int sizeOverwriteH;
    final int sizeOverwriteV;
    final boolean center;
    final StateExpression condition;

    ImageAdjustments(final Image image, final Border border, final Border inset, final int sizeOverwriteH, final int sizeOverwriteV, final boolean center, final StateExpression condition) {
        this.image = image;
        this.border = border;
        this.inset = inset;
        this.sizeOverwriteH = sizeOverwriteH;
        this.sizeOverwriteV = sizeOverwriteV;
        this.center = center;
        this.condition = condition;
    }

    public int getWidth() {
        if (this.sizeOverwriteH >= 0) {
            return this.sizeOverwriteH;
        }
        if (this.inset != null) {
            return this.image.getWidth() + this.inset.getBorderLeft() + this.inset.getBorderRight();
        }
        return this.image.getWidth();
    }

    public int getHeight() {
        if (this.sizeOverwriteV >= 0) {
            return this.sizeOverwriteV;
        }
        if (this.inset != null) {
            return this.image.getHeight() + this.inset.getBorderTop() + this.inset.getBorderBottom();
        }
        return this.image.getHeight();
    }

    public void draw(final AnimationState as, int x, int y, int width, int height) {
        if (this.condition == null || this.condition.evaluate(as)) {
            if (this.inset != null) {
                x += this.inset.getBorderLeft();
                y += this.inset.getBorderTop();
                width = Math.max(0, width - this.inset.getBorderLeft() - this.inset.getBorderRight());
                height = Math.max(0, height - this.inset.getBorderTop() - this.inset.getBorderBottom());
            }
            if (this.center) {
                final int w = Math.min(width, this.image.getWidth());
                final int h = Math.min(height, this.image.getHeight());
                x += (width - w) / 2;
                y += (height - h) / 2;
                width = w;
                height = h;
            }
            this.image.draw(as, x, y, width, height);
        }
    }

    public void draw(final AnimationState as, final int x, final int y) {
        this.draw(as, x, y, this.image.getWidth(), this.image.getHeight());
    }

    public Border getBorder() {
        return this.border;
    }

    public Image createTintedVersion(final Color color) {
        return (Image)new ImageAdjustments(this.image.createTintedVersion(color), this.border, this.inset, this.sizeOverwriteH, this.sizeOverwriteV, this.center, this.condition);
    }

    boolean isSimple() {
        return !this.center && this.inset == null && this.sizeOverwriteH < 0 && this.sizeOverwriteV < 0;
    }
}
