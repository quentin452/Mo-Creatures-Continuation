package de.matthiasmann.twl.renderer;

public interface AttributedString extends CharSequence, AnimationState
{
    int getPosition();
    
    void setPosition(final int p0);
    
    int advance();
}
