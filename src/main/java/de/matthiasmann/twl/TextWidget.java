package de.matthiasmann.twl;

import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.utils.*;

public class TextWidget extends Widget
{
    public static final AnimationState.StateKey STATE_HOVER;
    public static final AnimationState.StateKey STATE_TEXT_CHANGED;
    public static final AnimationState.StateKey STATE_TEXT_SELECTION;
    private static final int NOT_CACHED = -1;
    private Font font;
    private FontCache cache;
    private CharSequence text;
    private int cachedTextWidth;
    private int numTextLines;
    private boolean useCache;
    private boolean cacheDirty;
    private Alignment alignment;
    
    public TextWidget() {
        this(null, false);
    }
    
    public TextWidget(final de.matthiasmann.twl.AnimationState animState) {
        this(animState, false);
    }
    
    public TextWidget(final de.matthiasmann.twl.AnimationState animState, final boolean inherit) {
        super(animState, inherit);
        this.cachedTextWidth = -1;
        this.useCache = true;
        this.alignment = Alignment.TOPLEFT;
        this.text = "";
    }
    
    public Font getFont() {
        return this.font;
    }
    
    public void setFont(final Font font) {
        if (this.cache != null) {
            this.cache.destroy();
            this.cache = null;
        }
        this.font = font;
        this.cachedTextWidth = -1;
        if (this.useCache) {
            this.cacheDirty = true;
        }
    }
    
    protected void setCharSequence(final CharSequence text) {
        if (text == null) {
            throw new NullPointerException("text");
        }
        this.text = text;
        this.cachedTextWidth = -1;
        this.numTextLines = TextUtil.countNumLines(text);
        this.cacheDirty = true;
        this.getAnimationState().resetAnimationTime(TextWidget.STATE_TEXT_CHANGED);
    }
    
    protected CharSequence getCharSequence() {
        return this.text;
    }
    
    public boolean hasText() {
        return this.numTextLines > 0;
    }
    
    public boolean isMultilineText() {
        return this.numTextLines > 1;
    }
    
    public int getNumTextLines() {
        return this.numTextLines;
    }
    
    public Alignment getAlignment() {
        return this.alignment;
    }
    
    public void setAlignment(final Alignment alignment) {
        if (alignment == null) {
            throw new NullPointerException("alignment");
        }
        if (this.alignment != alignment) {
            this.alignment = alignment;
            this.cacheDirty = true;
        }
    }
    
    public boolean isCache() {
        return this.useCache;
    }
    
    public void setCache(final boolean cache) {
        if (this.useCache != cache) {
            this.useCache = cache;
            this.cacheDirty = true;
        }
    }
    
    protected void applyThemeTextWidget(final ThemeInfo themeInfo) {
        this.setFont(themeInfo.getFont("font"));
        this.setAlignment((Alignment)themeInfo.getParameter("textAlignment", (Enum)Alignment.TOPLEFT));
    }
    
    @Override
    protected void applyTheme(final ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        this.applyThemeTextWidget(themeInfo);
    }
    
    @Override
    public void destroy() {
        if (this.cache != null) {
            this.cache.destroy();
            this.cache = null;
        }
        super.destroy();
    }
    
    protected int computeTextX() {
        final int x = this.getInnerX();
        final int pos = this.alignment.hpos;
        if (pos > 0) {
            return x + (this.getInnerWidth() - this.computeTextWidth()) * pos / 2;
        }
        return x;
    }
    
    protected int computeTextY() {
        final int y = this.getInnerY();
        final int pos = this.alignment.vpos;
        if (pos > 0) {
            return y + (this.getInnerHeight() - this.computeTextHeight()) * pos / 2;
        }
        return y;
    }
    
    @Override
    protected void paintWidget(final GUI gui) {
        this.paintLabelText((AnimationState)this.getAnimationState());
    }
    
    protected void paintLabelText(final AnimationState animState) {
        if (this.cacheDirty) {
            this.updateCache();
        }
        if (this.hasText() && this.font != null) {
            final int x = this.computeTextX();
            final int y = this.computeTextY();
            this.paintTextAt(animState, x, y);
        }
    }
    
    protected void paintTextAt(final AnimationState animState, final int x, final int y) {
        if (this.cache != null) {
            this.cache.draw(animState, x, y);
        }
        else if (this.numTextLines > 1) {
            this.font.drawMultiLineText(animState, x, y, this.text, this.computeTextWidth(), this.alignment.fontHAlignment);
        }
        else {
            this.font.drawText(animState, x, y, this.text);
        }
    }
    
    protected void paintWithSelection(final de.matthiasmann.twl.AnimationState animState, final int start, final int end) {
        this.paintWithSelection(animState, start, end, 0, this.text.length(), this.computeTextY());
    }
    
    protected void paintWithSelection(final de.matthiasmann.twl.AnimationState animState, int start, int end, final int lineStart, final int lineEnd, final int y) {
        if (this.cacheDirty) {
            this.updateCache();
        }
        if (this.hasText() && this.font != null) {
            int x = this.computeTextX();
            start = limit(start, lineStart, lineEnd);
            end = limit(end, lineStart, lineEnd);
            if (start > lineStart) {
                x += this.font.drawText((AnimationState)animState, x, y, this.text, lineStart, start);
            }
            if (end > start) {
                animState.setAnimationState(TextWidget.STATE_TEXT_SELECTION, true);
                x += this.font.drawText((AnimationState)animState, x, y, this.text, start, end);
                animState.setAnimationState(TextWidget.STATE_TEXT_SELECTION, false);
            }
            if (end < lineEnd) {
                this.font.drawText((AnimationState)animState, x, y, this.text, end, lineEnd);
            }
        }
    }
    
    private static int limit(final int value, final int min, final int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
    
    @Override
    public int getPreferredInnerWidth() {
        int prefWidth = super.getPreferredInnerWidth();
        if (this.hasText() && this.font != null) {
            prefWidth = Math.max(prefWidth, this.computeTextWidth());
        }
        return prefWidth;
    }
    
    @Override
    public int getPreferredInnerHeight() {
        int prefHeight = super.getPreferredInnerHeight();
        if (this.hasText() && this.font != null) {
            prefHeight = Math.max(prefHeight, this.computeTextHeight());
        }
        return prefHeight;
    }
    
    public int computeRelativeCursorPositionX(final int charIndex) {
        return this.computeRelativeCursorPositionX(0, charIndex);
    }
    
    public int computeRelativeCursorPositionX(final int startIndex, final int charIndex) {
        if (this.font != null && charIndex > startIndex) {
            return this.font.computeTextWidth(this.text, startIndex, charIndex);
        }
        return 0;
    }
    
    public int computeTextWidth() {
        if (this.font != null) {
            if (this.cachedTextWidth == -1 || this.cacheDirty) {
                if (this.numTextLines > 1) {
                    this.cachedTextWidth = this.font.computeMultiLineTextWidth(this.text);
                }
                else {
                    this.cachedTextWidth = this.font.computeTextWidth(this.text);
                }
            }
            return this.cachedTextWidth;
        }
        return 0;
    }
    
    public int computeTextHeight() {
        if (this.font != null) {
            return Math.max(1, this.numTextLines) * this.font.getLineHeight();
        }
        return 0;
    }
    
    private void updateCache() {
        this.cacheDirty = false;
        if (this.useCache && this.hasText() && this.font != null) {
            if (this.numTextLines > 1) {
                this.cache = this.font.cacheMultiLineText(this.cache, this.text, this.font.computeMultiLineTextWidth(this.text), this.alignment.fontHAlignment);
            }
            else {
                this.cache = this.font.cacheText(this.cache, this.text);
            }
            if (this.cache != null) {
                this.cachedTextWidth = this.cache.getWidth();
            }
        }
        else {
            this.destroy();
        }
    }
    
    protected void handleMouseHover(final Event evt) {
        if (evt.isMouseEvent() && !this.hasSharedAnimationState()) {
            this.getAnimationState().setAnimationState(TextWidget.STATE_HOVER, evt.getType() != Event.Type.MOUSE_EXITED);
        }
    }
    
    static {
        STATE_HOVER = AnimationState.StateKey.get("hover");
        STATE_TEXT_CHANGED = AnimationState.StateKey.get("textChanged");
        STATE_TEXT_SELECTION = AnimationState.StateKey.get("textSelection");
    }
}
