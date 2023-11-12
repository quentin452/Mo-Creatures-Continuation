package de.matthiasmann.twl.theme;

import de.matthiasmann.twl.renderer.AnimationState;
import de.matthiasmann.twl.utils.*;
import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.*;

public class StateSelectImage implements Image, HasBorder
{
    final Image[] images;
    final StateSelect select;
    final Border border;

    public StateSelectImage(final StateSelect select, final Border border, final Image... images) {
        assert images.length >= select.getNumExpressions();
        assert images.length <= select.getNumExpressions() + 1;
        this.images = images;
        this.select = select;
        this.border = border;
    }

    public int getWidth() {
        return this.images[0].getWidth();
    }

    public int getHeight() {
        return this.images[0].getHeight();
    }

    public void draw(final AnimationState as, final int x, final int y) {
        this.draw(as, x, y, this.getWidth(), this.getHeight());
    }

    public void draw(final AnimationState as, final int x, final int y, final int width, final int height) {
        final int idx = this.select.evaluate(as);
        if (idx < this.images.length) {
            this.images[idx].draw(as, x, y, width, height);
        }
    }

    public Border getBorder() {
        return this.border;
    }

    public Image createTintedVersion(final Color color) {
        final Image[] newImages = new Image[this.images.length];
        for (int i = 0; i < newImages.length; ++i) {
            newImages[i] = this.images[i].createTintedVersion(color);
        }
        return (Image)new StateSelectImage(this.select, this.border, newImages);
    }
}
