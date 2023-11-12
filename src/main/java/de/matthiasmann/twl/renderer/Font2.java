package de.matthiasmann.twl.renderer;

public interface Font2 extends Font
{
    int drawText(final int p0, final int p1, final AttributedString p2);
    
    int drawText(final int p0, final int p1, final AttributedString p2, final int p3, final int p4);
    
    void drawMultiLineText(final int p0, final int p1, final AttributedString p2);
    
    void drawMultiLineText(final int p0, final int p1, final AttributedString p2, final int p3, final int p4);
    
    AttributedStringFontCache cacheText(final AttributedStringFontCache p0, final AttributedString p1);
    
    AttributedStringFontCache cacheText(final AttributedStringFontCache p0, final AttributedString p1, final int p2, final int p3);
    
    AttributedStringFontCache cacheMultiLineText(final AttributedStringFontCache p0, final AttributedString p1);
    
    AttributedStringFontCache cacheMultiLineText(final AttributedStringFontCache p0, final AttributedString p1, final int p2, final int p3);
}
