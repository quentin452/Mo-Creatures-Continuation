package de.matthiasmann.twl;

import de.matthiasmann.twl.renderer.*;

public abstract class ComboBoxBase extends Widget
{
    public static final AnimationState.StateKey STATE_COMBOBOX_KEYBOARD_FOCUS;
    protected final Button button;
    protected final PopupWindow popup;
    
    protected ComboBoxBase() {
        this.button = new Button(this.getAnimationState());
        this.popup = new PopupWindow(this);
        this.button.addCallback((Runnable)new Runnable() {
            @Override
            public void run() {
                ComboBoxBase.this.openPopup();
            }
        });
        this.add((Widget)this.button);
        this.setCanAcceptKeyboardFocus(true);
        this.setDepthFocusTraversal(false);
    }
    
    protected abstract Widget getLabel();
    
    protected boolean openPopup() {
        if (this.popup.openPopup()) {
            this.setPopupSize();
            return true;
        }
        return false;
    }
    
    @Override
    public int getPreferredInnerWidth() {
        return this.getLabel().getPreferredWidth() + this.button.getPreferredWidth();
    }
    
    @Override
    public int getPreferredInnerHeight() {
        return Math.max(this.getLabel().getPreferredHeight(), this.button.getPreferredHeight());
    }
    
    @Override
    public int getMinWidth() {
        int minWidth = super.getMinWidth();
        minWidth = Math.max(minWidth, this.getLabel().getMinWidth() + this.button.getMinWidth());
        return minWidth;
    }
    
    @Override
    public int getMinHeight() {
        final int minInnerHeight = Math.max(this.getLabel().getMinHeight(), this.button.getMinHeight());
        return Math.max(super.getMinHeight(), minInnerHeight + this.getBorderVertical());
    }
    
    protected void setPopupSize() {
        final int minHeight = this.popup.getMinHeight();
        int popupHeight = Widget.computeSize(minHeight, this.popup.getPreferredHeight(), this.popup.getMaxHeight());
        final int popupMaxBottom = this.popup.getParent().getInnerBottom();
        if (this.getBottom() + minHeight > popupMaxBottom) {
            if (this.getY() - popupHeight >= this.popup.getParent().getInnerY()) {
                this.popup.setPosition(this.getX(), this.getY() - popupHeight);
            }
            else {
                this.popup.setPosition(this.getX(), popupMaxBottom - minHeight);
            }
        }
        else {
            this.popup.setPosition(this.getX(), this.getBottom());
        }
        popupHeight = Math.min(popupHeight, popupMaxBottom - this.popup.getY());
        this.popup.setSize(this.getWidth(), popupHeight);
    }
    
    @Override
    protected void layout() {
        final int btnWidth = this.button.getPreferredWidth();
        final int innerHeight = this.getInnerHeight();
        final int innerX = this.getInnerX();
        final int innerY = this.getInnerY();
        this.button.setPosition(this.getInnerRight() - btnWidth, innerY);
        this.button.setSize(btnWidth, innerHeight);
        this.getLabel().setPosition(innerX, innerY);
        this.getLabel().setSize(Math.max(0, this.button.getX() - innerX), innerHeight);
    }
    
    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        if (this.popup.isOpen()) {
            this.setPopupSize();
        }
    }
    
    private void setRecursive(final Widget w, final AnimationState.StateKey what, final boolean state) {
        w.getAnimationState().setAnimationState(what, state);
        for (int i = 0; i < w.getNumChildren(); ++i) {
            final Widget child = w.getChild(i);
            this.setRecursive(child, what, state);
        }
    }
    
    @Override
    protected void keyboardFocusGained() {
        super.keyboardFocusGained();
        this.setRecursive(this.getLabel(), ComboBoxBase.STATE_COMBOBOX_KEYBOARD_FOCUS, true);
    }
    
    @Override
    protected void keyboardFocusLost() {
        super.keyboardFocusLost();
        this.setRecursive(this.getLabel(), ComboBoxBase.STATE_COMBOBOX_KEYBOARD_FOCUS, false);
    }
    
    static {
        STATE_COMBOBOX_KEYBOARD_FOCUS = AnimationState.StateKey.get("comboboxKeyboardFocus");
    }
}
