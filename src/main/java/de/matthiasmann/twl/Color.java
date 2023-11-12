package de.matthiasmann.twl;

import java.util.*;
import java.lang.reflect.*;

public final class Color
{
    public static final Color BLACK;
    public static final Color SILVER;
    public static final Color GRAY;
    public static final Color WHITE;
    public static final Color MAROON;
    public static final Color RED;
    public static final Color PURPLE;
    public static final Color FUCHSIA;
    public static final Color GREEN;
    public static final Color LIME;
    public static final Color OLIVE;
    public static final Color ORANGE;
    public static final Color YELLOW;
    public static final Color NAVY;
    public static final Color BLUE;
    public static final Color TEAL;
    public static final Color AQUA;
    public static final Color SKYBLUE;
    public static final Color LIGHTBLUE;
    public static final Color LIGHTCORAL;
    public static final Color LIGHTCYAN;
    public static final Color LIGHTGRAY;
    public static final Color LIGHTGREEN;
    public static final Color LIGHTPINK;
    public static final Color LIGHTSALMON;
    public static final Color LIGHTSKYBLUE;
    public static final Color LIGHTYELLOW;
    public static final Color TRANSPARENT;
    private final byte r;
    private final byte g;
    private final byte b;
    private final byte a;
    
    public Color(final byte r, final byte g, final byte b, final byte a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
    
    public Color(final int argb) {
        this.a = (byte)(argb >> 24);
        this.r = (byte)(argb >> 16);
        this.g = (byte)(argb >> 8);
        this.b = (byte)argb;
    }
    
    public int toARGB() {
        return (this.a & 0xFF) << 24 | (this.r & 0xFF) << 16 | (this.g & 0xFF) << 8 | (this.b & 0xFF);
    }
    
    public byte getR() {
        return this.r;
    }
    
    public byte getG() {
        return this.g;
    }
    
    public byte getB() {
        return this.b;
    }
    
    public byte getA() {
        return this.a;
    }
    
    public int getRed() {
        return this.r & 0xFF;
    }
    
    public int getGreen() {
        return this.g & 0xFF;
    }
    
    public int getBlue() {
        return this.b & 0xFF;
    }
    
    public int getAlpha() {
        return this.a & 0xFF;
    }
    
    public float getRedFloat() {
        return (this.r & 0xFF) * 0.003921569f;
    }
    
    public float getGreenFloat() {
        return (this.g & 0xFF) * 0.003921569f;
    }
    
    public float getBlueFloat() {
        return (this.b & 0xFF) * 0.003921569f;
    }
    
    public float getAlphaFloat() {
        return (this.a & 0xFF) * 0.003921569f;
    }
    
    public void getFloats(final float[] dst, final int off) {
        dst[off + 0] = this.getRedFloat();
        dst[off + 1] = this.getGreenFloat();
        dst[off + 2] = this.getBlueFloat();
        dst[off + 3] = this.getAlphaFloat();
    }
    
    public static Color getColorByName(String name) {
        name = name.toUpperCase(Locale.ENGLISH);
        try {
            final Field f = Color.class.getField(name);
            if (Modifier.isStatic(f.getModifiers()) && f.getType() == Color.class) {
                return (Color)f.get(null);
            }
        }
        catch (Throwable t) {}
        return null;
    }
    
    public static Color parserColor(final String value) throws NumberFormatException {
        if (value.length() <= 0 || value.charAt(0) != '#') {
            return getColorByName(value);
        }
        final String hexcode = value.substring(1);
        switch (value.length()) {
            case 4: {
                final int rgb4 = Integer.parseInt(hexcode, 16);
                final int r = (rgb4 >> 8 & 0xF) * 17;
                final int g = (rgb4 >> 4 & 0xF) * 17;
                final int b = (rgb4 & 0xF) * 17;
                return new Color(0xFF000000 | r << 16 | g << 8 | b);
            }
            case 5: {
                final int rgb4 = Integer.parseInt(hexcode, 16);
                final int a = (rgb4 >> 12 & 0xF) * 17;
                final int r2 = (rgb4 >> 8 & 0xF) * 17;
                final int g2 = (rgb4 >> 4 & 0xF) * 17;
                final int b2 = (rgb4 & 0xF) * 17;
                return new Color(a << 24 | r2 << 16 | g2 << 8 | b2);
            }
            case 7: {
                return new Color(0xFF000000 | Integer.parseInt(hexcode, 16));
            }
            case 9: {
                return new Color((int)Long.parseLong(hexcode, 16));
            }
            default: {
                throw new NumberFormatException("Can't parse '" + value + "' as hex color");
            }
        }
    }
    
    @Override
    public String toString() {
        if (this.a != -1) {
            return String.format("#%08X", this.toARGB());
        }
        return String.format("#%06X", this.toARGB() & 0xFFFFFF);
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Color)) {
            return false;
        }
        final Color other = (Color)obj;
        return this.toARGB() == other.toARGB();
    }
    
    @Override
    public int hashCode() {
        return this.toARGB();
    }
    
    public Color multiply(final Color other) {
        return new Color(this.mul(this.r, other.r), this.mul(this.g, other.g), this.mul(this.b, other.b), this.mul(this.a, other.a));
    }
    
    private byte mul(final byte a, final byte b) {
        return (byte)((a & 0xFF) * (b & 0xFF) / 255);
    }
    
    static {
        BLACK = new Color(-16777216);
        SILVER = new Color(-4144960);
        GRAY = new Color(-8355712);
        WHITE = new Color(-1);
        MAROON = new Color(-8388608);
        RED = new Color(-65536);
        PURPLE = new Color(-8388480);
        FUCHSIA = new Color(-65281);
        GREEN = new Color(-16744448);
        LIME = new Color(-16711936);
        OLIVE = new Color(-8355840);
        ORANGE = new Color(-23296);
        YELLOW = new Color(-256);
        NAVY = new Color(-16777088);
        BLUE = new Color(-16776961);
        TEAL = new Color(-16744320);
        AQUA = new Color(-16711681);
        SKYBLUE = new Color(-7876885);
        LIGHTBLUE = new Color(-5383962);
        LIGHTCORAL = new Color(-1015680);
        LIGHTCYAN = new Color(-2031617);
        LIGHTGRAY = new Color(-2894893);
        LIGHTGREEN = new Color(-7278960);
        LIGHTPINK = new Color(-18751);
        LIGHTSALMON = new Color(-24454);
        LIGHTSKYBLUE = new Color(-7876870);
        LIGHTYELLOW = new Color(-32);
        TRANSPARENT = new Color(0);
    }
}
