package de.matthiasmann.twl.theme;

import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.AnimationState;

class ComposedImage implements Image, HasBorder
{
    private final Image[] layers;
    private final Border border;

    public ComposedImage(final Image[] layers, final Border border) {
        this.layers = layers;
        this.border = border;
    }

    public void draw(final AnimationState as, final int x, final int y) {
        this.draw(as, x, y, this.getWidth(), this.getHeight());
    }

    public void draw(final AnimationState as, final int x, final int y, final int width, final int height) {
        for (final Image layer : this.layers) {
            layer.draw(as, x, y, width, height);
        }
    }

    public int getHeight() {
        return this.layers[0].getHeight();
    }

    public int getWidth() {
        return this.layers[0].getWidth();
    }

    public Border getBorder() {
        return this.border;
    }

    public Image createTintedVersion(final Color color) {
        final Image[] newLayers = new Image[this.layers.length];
        for (int i = 0; i < newLayers.length; ++i) {
            newLayers[i] = this.layers[i].createTintedVersion(color);
        }
        return (Image)new ComposedImage(newLayers, this.border);
    }
}
