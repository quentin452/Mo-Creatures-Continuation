package de.matthiasmann.twl.renderer.lwjgl;

import de.matthiasmann.twl.renderer.AnimationState;
import de.matthiasmann.twl.utils.*;
import de.matthiasmann.twl.renderer.*;
import java.nio.*;
import de.matthiasmann.twl.*;

public class LWJGLFont implements Font, Font2
{
    static final int STYLE_UNDERLINE = 1;
    static final int STYLE_LINETHROUGH = 2;
    private final LWJGLRenderer renderer;
    private final BitmapFont font;
    private final FontState[] fontStates;
    private final StateSelect stateSelect;
    private int[] multiLineInfo;

    public LWJGLFont clone() {
        return new LWJGLFont(this);
    }

    private LWJGLFont(final LWJGLFont oldFont) {
        this.renderer = oldFont.renderer;
        this.font = oldFont.font;
        this.stateSelect = oldFont.stateSelect;
        this.fontStates = new FontState[oldFont.fontStates.length];
        for (int i = 0; i < this.fontStates.length; ++i) {
            final FontState oldState = oldFont.fontStates[i];
            this.fontStates[i] = new FontState(oldState);
        }
    }

    LWJGLFont(final LWJGLRenderer renderer, final BitmapFont font, final StateSelect select, final FontParameter... parameterList) {
        this.renderer = renderer;
        this.font = font;
        this.stateSelect = select;
        this.fontStates = new FontState[parameterList.length];
        for (int i = 0; i < parameterList.length; ++i) {
            this.fontStates[i] = new FontState(parameterList[i]);
        }
    }

    public FontState evalFontState(final AnimationState as) {
        return this.fontStates[this.stateSelect.evaluate(as)];
    }

    private int[] getMultiLineInfo(final int numLines) {
        if (this.multiLineInfo == null || this.multiLineInfo.length < numLines) {
            this.multiLineInfo = new int[numLines];
        }
        return this.multiLineInfo;
    }

    public void destroy() {
        this.font.destroy();
    }

    public boolean isProportional() {
        return this.font.isProportional();
    }

    public int getSpaceWidth() {
        return this.font.getSpaceWidth();
    }

    public int getLineHeight() {
        return this.font.getLineHeight();
    }

    public int getBaseLine() {
        return this.font.getBaseLine();
    }

    public int getEM() {
        return this.font.getEM();
    }

    public int getEX() {
        return this.font.getEX();
    }

    public int drawText(final AnimationState as, final int x, final int y, final CharSequence str) {
        return this.drawText(as, x, y, str, 0, str.length());
    }

    public int drawText(final AnimationState as, int x, int y, final CharSequence str, final int start, final int end) {
        final FontState fontState = this.evalFontState(as);
        x += fontState.offsetX;
        y += fontState.offsetY;
        if (!this.font.prepare()) {
            return 0;
        }
        int width;
        try {
            this.renderer.tintStack.setColor(fontState.color);
            width = this.font.drawText(x, y, str, start, end);
        }
        finally {
            this.font.cleanup();
        }
        this.drawLine(fontState, x, y, width);
        return width;
    }

    public int drawMultiLineText(final AnimationState as, int x, int y, final CharSequence str, final int width, final HAlignment align) {
        final FontState fontState = this.evalFontState(as);
        x += fontState.offsetX;
        y += fontState.offsetY;
        if (!this.font.prepare()) {
            return 0;
        }
        int numLines;
        try {
            this.renderer.tintStack.setColor(fontState.color);
            numLines = this.font.drawMultiLineText(x, y, str, width, align);
        }
        finally {
            this.font.cleanup();
        }
        if (fontState.style != 0) {
            final int[] info = this.getMultiLineInfo(numLines);
            this.font.computeMultiLineInfo(str, width, align, info);
            this.drawLines(fontState, x, y, info, numLines);
        }
        return numLines * this.font.getLineHeight();
    }

    void drawLines(final FontState fontState, final int x, final int y, final int[] info, final int numLines) {
        if ((fontState.style & 0x1) != 0x0) {
            this.font.drawMultiLineLines(x, y + this.font.getBaseLine() + fontState.underlineOffset, info, numLines);
        }
        if ((fontState.style & 0x2) != 0x0) {
            this.font.drawMultiLineLines(x, y + this.font.getLineHeight() / 2, info, numLines);
        }
    }

    void drawLine(final FontState fontState, final int x, final int y, final int width) {
        if ((fontState.style & 0x1) != 0x0) {
            this.font.drawLine(x, y + this.font.getBaseLine() + fontState.underlineOffset, x + width);
        }
        if ((fontState.style & 0x2) != 0x0) {
            this.font.drawLine(x, y + this.font.getLineHeight() / 2, x + width);
        }
    }

    public int computeVisibleGlpyhs(final CharSequence str, final int start, final int end, final int availWidth) {
        return this.font.computeVisibleGlpyhs(str, start, end, availWidth);
    }

    public int computeTextWidth(final CharSequence str) {
        return this.font.computeTextWidth(str, 0, str.length());
    }

    public int computeTextWidth(final CharSequence str, final int start, final int end) {
        return this.font.computeTextWidth(str, start, end);
    }

    public int computeMultiLineTextWidth(final CharSequence str) {
        return this.font.computeMultiLineTextWidth(str);
    }

    public FontCache cacheText(final FontCache prevCache, final CharSequence str) {
        return this.cacheText(prevCache, str, 0, str.length());
    }

    public FontCache cacheText(final FontCache prevCache, final CharSequence str, final int start, final int end) {
        LWJGLFontCache cache = (LWJGLFontCache)prevCache;
        if (cache == null) {
            cache = new LWJGLFontCache(this.renderer, this);
        }
        return this.font.cacheText(cache, str, start, end);
    }

    public FontCache cacheMultiLineText(final FontCache prevCache, final CharSequence str, final int width, final HAlignment align) {
        LWJGLFontCache cache = (LWJGLFontCache)prevCache;
        if (cache == null) {
            cache = new LWJGLFontCache(this.renderer, this);
        }
        return this.font.cacheMultiLineText(cache, str, width, align);
    }

    public int drawText(final int x, final int y, final AttributedString attributedString) {
        return this.drawText(x, y, attributedString, 0, attributedString.length(), false);
    }

    public int drawText(final int x, final int y, final AttributedString attributedString, final int start, final int end) {
        return this.drawText(x, y, attributedString, 0, attributedString.length(), false);
    }

    public void drawMultiLineText(final int x, final int y, final AttributedString attributedString) {
        this.drawText(x, y, attributedString, 0, attributedString.length(), true);
    }

    public void drawMultiLineText(final int x, final int y, final AttributedString attributedString, final int start, final int end) {
        this.drawText(x, y, attributedString, start, end, true);
    }

    private int drawText(int x, int y, final AttributedString attributedString, int start, final int end, final boolean multiLine) {
        final int startX = x;
        attributedString.setPosition(start);
        if (!this.font.prepare()) {
            return 0;
        }
        try {
            BitmapFont.Glyph lastGlyph = null;
            do {
                final FontState fontState = this.evalFontState((AnimationState)attributedString);
                x += fontState.offsetX;
                y += fontState.offsetY;
                final int runStart = x;
                this.renderer.tintStack.setColor(fontState.color);
                int nextStop = Math.min(end, attributedString.advance());
                if (multiLine) {
                    nextStop = TextUtil.indexOf((CharSequence)attributedString, '\n', start, nextStop);
                }
                while (start < nextStop) {
                    final char ch = attributedString.charAt(start++);
                    final BitmapFont.Glyph g = this.font.getGlyph(ch);
                    if (g != null) {
                        if (lastGlyph != null) {
                            x += lastGlyph.getKerning(ch);
                        }
                        lastGlyph = g;
                        if (g.width > 0) {
                            g.draw(x, y);
                        }
                        x += g.xadvance;
                    }
                }
                this.drawLine(fontState, x, y, x - runStart);
                x -= fontState.offsetX;
                y -= fontState.offsetY;
                if (multiLine && start < end && attributedString.charAt(start) == '\n') {
                    attributedString.setPosition(++start);
                    x = startX;
                    y += this.font.getLineHeight();
                    lastGlyph = null;
                }
            } while (start < end);
        }
        finally {
            this.font.cleanup();
        }
        return x - startX;
    }

    public AttributedStringFontCache cacheText(final AttributedStringFontCache prevCache, final AttributedString attributedString) {
        return this.cacheText(prevCache, attributedString, 0, attributedString.length(), false);
    }

    public AttributedStringFontCache cacheText(final AttributedStringFontCache prevCache, final AttributedString attributedString, final int start, final int end) {
        return this.cacheText(prevCache, attributedString, start, end, false);
    }

    public AttributedStringFontCache cacheMultiLineText(final AttributedStringFontCache prevCache, final AttributedString attributedString) {
        return this.cacheText(prevCache, attributedString, 0, attributedString.length(), true);
    }

    public AttributedStringFontCache cacheMultiLineText(final AttributedStringFontCache prevCache, final AttributedString attributedString, final int start, final int end) {
        return this.cacheText(prevCache, attributedString, start, end, true);
    }

    private AttributedStringFontCache cacheText(final AttributedStringFontCache prevCache, final AttributedString attributedString, int start, final int end, final boolean multiLine) {
        if (end <= start) {
            return null;
        }
        LWJGLAttributedStringFontCache cache = (LWJGLAttributedStringFontCache)prevCache;
        if (cache == null) {
            cache = new LWJGLAttributedStringFontCache(this.renderer, this.font);
        }
        final FloatBuffer va = cache.allocate(end - start);
        attributedString.setPosition(start);
        BitmapFont.Glyph lastGlyph = null;
        int x = 0;
        int y = 0;
        int width = 0;
        do {
            final FontState fontState = this.evalFontState((AnimationState)attributedString);
            x += fontState.offsetX;
            y += fontState.offsetY;
            int runLength = 0;
            final int xStart = x;
            int nextStop;
            for (nextStop = Math.min(end, attributedString.advance()); nextStop < end && fontState == this.evalFontState((AnimationState)attributedString); nextStop = Math.min(end, attributedString.advance())) {}
            if (multiLine) {
                nextStop = TextUtil.indexOf((CharSequence)attributedString, '\n', start, nextStop);
            }
            while (start < nextStop) {
                final char ch = attributedString.charAt(start++);
                final BitmapFont.Glyph g = this.font.getGlyph(ch);
                if (g != null) {
                    if (lastGlyph != null) {
                        x += lastGlyph.getKerning(ch);
                    }
                    lastGlyph = g;
                    if (g.width > 0 && g.height > 0) {
                        g.draw(va, x, y);
                        ++runLength;
                    }
                    x += g.xadvance;
                }
            }
            x -= fontState.offsetX;
            y -= fontState.offsetY;
            if (runLength > 0 || fontState.style != 0) {
                final LWJGLAttributedStringFontCache.Run run = cache.addRun();
                run.state = fontState;
                run.numVertices = runLength * 4;
                run.x = xStart;
                run.xend = x;
                run.y = y;
            }
            if (multiLine && start < end && attributedString.charAt(start) == '\n') {
                attributedString.setPosition(++start);
                width = Math.max(width, x);
                x = 0;
                y += this.font.getLineHeight();
                lastGlyph = null;
            }
        } while (start < end);
        if (x > 0) {
            width = Math.max(width, x);
            y += this.font.getLineHeight();
        }
        cache.width = width;
        cache.height = y;
        return (AttributedStringFontCache)cache;
    }

    public static class FontState
    {
        Color color;
        int offsetX;
        int offsetY;
        int style;
        int underlineOffset;

        FontState(final FontParameter fontParam) {
            int lineStyle = 0;
            if (fontParam.get(FontParameter.UNDERLINE)) {
                lineStyle |= 0x1;
            }
            if (fontParam.get(FontParameter.LINETHROUGH)) {
                lineStyle |= 0x2;
            }
            this.color = (Color)fontParam.get(FontParameter.COLOR);
            this.offsetX = (int)fontParam.get((FontParameter.Parameter)LWJGLRenderer.FONTPARAM_OFFSET_X);
            this.offsetY = (int)fontParam.get((FontParameter.Parameter)LWJGLRenderer.FONTPARAM_OFFSET_Y);
            this.style = lineStyle;
            this.underlineOffset = (int)fontParam.get((FontParameter.Parameter)LWJGLRenderer.FONTPARAM_UNDERLINE_OFFSET);
        }

        public FontState(final FontState oldState) {
            this.color = oldState.color;
            this.offsetX = oldState.offsetX;
            this.offsetY = oldState.offsetY;
            this.style = oldState.style;
            this.underlineOffset = oldState.underlineOffset;
        }

        public FontState(final Color color, final int offsetX, final int offsetY, final int style, final int underlineOffset) {
            this.color = color;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.style = style;
            this.underlineOffset = underlineOffset;
        }

        public Color getColor() {
            return this.color;
        }

        public boolean getLineThrough() {
            return (this.style & 0x2) == 0x2;
        }

        public int getOffsetX() {
            return this.offsetX;
        }

        public int getOffsetY() {
            return this.offsetY;
        }

        public boolean getUnderline() {
            return (this.style & 0x1) == 0x1;
        }

        public int getUnderlineOffset() {
            return this.underlineOffset;
        }

        public void setColor(final Color col) {
            this.color = col;
        }

        public void setUnderlineOffset(final int i) {
            this.underlineOffset = i;
        }

        public void setUnderline(final boolean val) {
            if (this.getUnderline() != val) {
                this.style ^= 0x1;
            }
        }

        public void setOffsetY(final int i) {
            this.offsetY = i;
        }

        public void setOffsetX(final int i) {
            this.offsetX = i;
        }

        public void setLineThrough(final boolean val) {
            if (this.getLineThrough() != val) {
                this.style ^= 0x2;
            }
        }
    }
}
