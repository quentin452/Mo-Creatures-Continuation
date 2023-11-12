package de.matthiasmann.twl.theme;

import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.AnimationState;

public class EmptyImage implements Image
{
    private final int width;
    private final int height;

    public EmptyImage(final int width, final int height) {
        this.width = width;
        this.height = height;
    }

    public void draw(final AnimationState as, final int x, final int y) {
    }

    public void draw(final AnimationState as, final int x, final int y, final int width, final int height) {
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Image createTintedVersion(final Color color) {
        return (Image)this;
    }
}
