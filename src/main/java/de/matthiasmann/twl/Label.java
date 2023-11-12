package de.matthiasmann.twl;

import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.utils.*;

public class Label<T> extends TextWidget
{
    private boolean autoSize;
    private Widget labelFor;
    private CallbackWithReason<CallbackReason>[] callbacks;

    public Label() {
        this(null, false);
    }

    public Label(final AnimationState animState) {
        this(animState, false);
    }

    public Label(final AnimationState animState, final boolean inherit) {
        super(animState, inherit);
        this.autoSize = true;
    }

    public Label(final String text) {
        this();
        this.setText(text);
    }

    public void addCallback(final CallbackWithReason<CallbackReason> cb) {
        this.callbacks = CallbackSupport.addCallbackToList(this.callbacks, cb, CallbackWithReason.class);
    }

    public void removeCallback(final CallbackWithReason<CallbackReason> cb) {
        this.callbacks = CallbackSupport.removeCallbackFromList(this.callbacks, cb);
    }

    protected void doCallback(final CallbackReason reason) {
        CallbackSupport.fireCallbacks(this.callbacks, reason);
    }

    public boolean isAutoSize() {
        return this.autoSize;
    }

    public void setAutoSize(final boolean autoSize) {
        this.autoSize = autoSize;
    }

    @Override
    public void setFont(final Font font) {
        super.setFont(font);
        if (this.autoSize) {
            this.invalidateLayout();
        }
    }

    public String getText() {
        return super.getCharSequence().toString();
    }

    public void setText(String text) {
        text = TextUtil.notNull(text);
        if (!text.equals(this.getText())) {
            super.setCharSequence(text);
            if (this.autoSize) {
                this.invalidateLayout();
            }
        }
    }

    @Override
    public Object getTooltipContent() {
        final Object toolTipContent = super.getTooltipContent();
        if (toolTipContent == null && this.labelFor != null) {
            return this.labelFor.getTooltipContent();
        }
        return toolTipContent;
    }

    public Widget getLabelFor() {
        return this.labelFor;
    }

    public void setLabelFor(final Widget labelFor) {
        if (labelFor == this) {
            throw new IllegalArgumentException("labelFor == this");
        }
        this.labelFor = labelFor;
    }

    protected void applyThemeLabel(final ThemeInfo themeInfo) {
        final String themeText = themeInfo.getParameterValue("text", false, String.class);
        if (themeText != null) {
            this.setText(themeText);
        }
    }

    @Override
    protected void applyTheme(final ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        this.applyThemeLabel(themeInfo);
    }

    @Override
    public boolean requestKeyboardFocus() {
        if (this.labelFor != null) {
            return this.labelFor.requestKeyboardFocus();
        }
        return super.requestKeyboardFocus();
    }

    @Override
    public int getMinWidth() {
        return Math.max(super.getMinWidth(), this.getPreferredWidth());
    }

    @Override
    public int getMinHeight() {
        return Math.max(super.getMinHeight(), this.getPreferredHeight());
    }

    @Override
    protected boolean handleEvent(final Event evt) {
        this.handleMouseHover(evt);
        if (evt.isMouseEvent()) {
            if (evt.getType() == Event.Type.MOUSE_CLICKED) {
                switch (evt.getMouseClickCount()) {
                    case 1: {
                        this.handleClick(false);
                        break;
                    }
                    case 2: {
                        this.handleClick(true);
                        break;
                    }
                }
            }
            return evt.getType() != Event.Type.MOUSE_WHEEL;
        }
        return false;
    }

    protected void handleClick(final boolean doubleClick) {
        this.doCallback(doubleClick ? CallbackReason.DOUBLE_CLICK : CallbackReason.CLICK);
    }

    public enum CallbackReason
    {
        CLICK,
        DOUBLE_CLICK;
    }
}
