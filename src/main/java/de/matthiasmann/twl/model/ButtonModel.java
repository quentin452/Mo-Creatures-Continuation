package de.matthiasmann.twl.model;

public interface ButtonModel
{
    boolean isSelected();
    
    boolean isPressed();
    
    boolean isArmed();
    
    boolean isHover();
    
    boolean isEnabled();
    
    void setSelected(final boolean p0);
    
    void setPressed(final boolean p0);
    
    void setArmed(final boolean p0);
    
    void setHover(final boolean p0);
    
    void setEnabled(final boolean p0);
    
    void addActionCallback(final Runnable p0);
    
    void removeActionCallback(final Runnable p0);
    
    void fireActionCallback();
    
    boolean hasActionCallbacks();
    
    void addStateCallback(final Runnable p0);
    
    void removeStateCallback(final Runnable p0);
    
    void connect();
    
    void disconnect();
}
