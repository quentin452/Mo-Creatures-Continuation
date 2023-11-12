package de.matthiasmann.twl.model;

public class ColorSpaceHSL extends AbstractColorSpace
{
    public ColorSpaceHSL() {
        super("HSL", new String[] { "Hue", "Saturation", "Lightness" });
    }
    
    public String getComponentShortName(final int component) {
        return "HSL".substring(component, component + 1);
    }
    
    public float getMaxValue(final int component) {
        return (component == 0) ? 360.0f : 100.0f;
    }
    
    public float getDefaultValue(final int component) {
        return (component == 0) ? 0.0f : 50.0f;
    }
    
    public float[] fromRGB(final int rgb) {
        final float r = (rgb >> 16 & 0xFF) / 255.0f;
        final float g = (rgb >> 8 & 0xFF) / 255.0f;
        final float b = (rgb & 0xFF) / 255.0f;
        final float max = Math.max(Math.max(r, g), b);
        final float min = Math.min(Math.min(r, g), b);
        final float summe = max + min;
        float saturation = max - min;
        if (saturation > 0.0f) {
            saturation /= ((summe > 1.0f) ? (2.0f - summe) : summe);
        }
        return new float[] { 360.0f * getHue(r, g, b, max, min), 100.0f * saturation, 50.0f * summe };
    }
    
    public int toRGB(final float[] color) {
        float hue = color[0] / 360.0f;
        final float saturation = color[1] / 100.0f;
        final float lightness = color[2] / 100.0f;
        float r;
        float g;
        float b;
        if (saturation > 0.0f) {
            hue = ((hue < 1.0f) ? (hue * 6.0f) : 0.0f);
            final float q = lightness + saturation * ((lightness > 0.5f) ? (1.0f - lightness) : lightness);
            final float p = 2.0f * lightness - q;
            r = normalize(q, p, (hue < 4.0f) ? (hue + 2.0f) : (hue - 4.0f));
            g = normalize(q, p, hue);
            b = normalize(q, p, (hue < 2.0f) ? (hue + 4.0f) : (hue - 2.0f));
        }
        else {
            g = (r = (b = lightness));
        }
        return toByte(r) << 16 | toByte(g) << 8 | toByte(b);
    }
    
    static float getHue(final float red, final float green, final float blue, final float max, final float min) {
        float hue = max - min;
        if (hue > 0.0f) {
            if (max == red) {
                hue = (green - blue) / hue;
                if (hue < 0.0f) {
                    hue += 6.0f;
                }
            }
            else if (max == green) {
                hue = 2.0f + (blue - red) / hue;
            }
            else {
                hue = 4.0f + (red - green) / hue;
            }
            hue /= 6.0f;
        }
        return hue;
    }
    
    private static float normalize(final float q, final float p, final float color) {
        if (color < 1.0f) {
            return p + (q - p) * color;
        }
        if (color < 3.0f) {
            return q;
        }
        if (color < 4.0f) {
            return p + (q - p) * (4.0f - color);
        }
        return p;
    }
    
    private static int toByte(final float value) {
        return Math.max(0, Math.min(255, (int)(255.0f * value)));
    }
}
