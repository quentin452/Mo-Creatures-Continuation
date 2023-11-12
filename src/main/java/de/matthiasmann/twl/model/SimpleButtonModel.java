package de.matthiasmann.twl.model;

import de.matthiasmann.twl.utils.*;

public class SimpleButtonModel implements ButtonModel
{
    protected static final int STATE_MASK_HOVER = 1;
    protected static final int STATE_MASK_PRESSED = 2;
    protected static final int STATE_MASK_ARMED = 4;
    protected static final int STATE_MASK_DISABLED = 8;
    protected Runnable[] actionCallbacks;
    protected Runnable[] stateCallbacks;
    protected int state;
    
    public boolean isSelected() {
        return false;
    }
    
    public boolean isPressed() {
        return (this.state & 0x2) != 0x0;
    }
    
    public boolean isArmed() {
        return (this.state & 0x4) != 0x0;
    }
    
    public boolean isHover() {
        return (this.state & 0x1) != 0x0;
    }
    
    public boolean isEnabled() {
        return (this.state & 0x8) == 0x0;
    }
    
    public void setSelected(final boolean selected) {
    }
    
    public void setPressed(final boolean pressed) {
        if (pressed != this.isPressed()) {
            final boolean fireAction = !pressed && this.isArmed() && this.isEnabled();
            this.setStateBit(2, pressed);
            this.fireStateCallback();
            if (fireAction) {
                this.buttonAction();
            }
        }
    }
    
    public void setArmed(final boolean armed) {
        if (armed != this.isArmed()) {
            this.setStateBit(4, armed);
            this.fireStateCallback();
        }
    }
    
    public void setHover(final boolean hover) {
        if (hover != this.isHover()) {
            this.setStateBit(1, hover);
            this.fireStateCallback();
        }
    }
    
    public void setEnabled(final boolean enabled) {
        if (enabled != this.isEnabled()) {
            this.setStateBit(8, !enabled);
            this.fireStateCallback();
        }
    }
    
    protected void buttonAction() {
        this.fireActionCallback();
    }
    
    protected void setStateBit(final int mask, final boolean set) {
        if (set) {
            this.state |= mask;
        }
        else {
            this.state &= ~mask;
        }
    }
    
    protected void fireStateCallback() {
        CallbackSupport.fireCallbacks(this.stateCallbacks);
    }
    
    public void fireActionCallback() {
        CallbackSupport.fireCallbacks(this.actionCallbacks);
    }
    
    public void addActionCallback(final Runnable callback) {
        this.actionCallbacks = CallbackSupport.addCallbackToList(this.actionCallbacks, callback, Runnable.class);
    }
    
    public void removeActionCallback(final Runnable callback) {
        this.actionCallbacks = CallbackSupport.removeCallbackFromList(this.actionCallbacks, callback);
    }
    
    public boolean hasActionCallbacks() {
        return this.actionCallbacks != null;
    }
    
    public void addStateCallback(final Runnable callback) {
        this.stateCallbacks = CallbackSupport.addCallbackToList(this.stateCallbacks, callback, Runnable.class);
    }
    
    public void removeStateCallback(final Runnable callback) {
        this.stateCallbacks = CallbackSupport.removeCallbackFromList(this.stateCallbacks, callback);
    }
    
    public void connect() {
    }
    
    public void disconnect() {
    }
}
