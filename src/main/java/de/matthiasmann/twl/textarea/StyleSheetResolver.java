package de.matthiasmann.twl.textarea;

public interface StyleSheetResolver
{
    void startLayout();
    
    Style resolve(final Style p0);
    
    void layoutFinished();
}
