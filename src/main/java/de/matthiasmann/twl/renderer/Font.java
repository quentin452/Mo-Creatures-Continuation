package de.matthiasmann.twl.renderer;

import de.matthiasmann.twl.*;

public interface Font extends Resource
{
    boolean isProportional();
    
    int getBaseLine();
    
    int getLineHeight();
    
    int getSpaceWidth();
    
    int getEM();
    
    int getEX();
    
    int computeMultiLineTextWidth(final CharSequence p0);
    
    int computeTextWidth(final CharSequence p0);
    
    int computeTextWidth(final CharSequence p0, final int p1, final int p2);
    
    int computeVisibleGlpyhs(final CharSequence p0, final int p1, final int p2, final int p3);
    
    int drawMultiLineText(final AnimationState p0, final int p1, final int p2, final CharSequence p3, final int p4, final HAlignment p5);
    
    int drawText(final AnimationState p0, final int p1, final int p2, final CharSequence p3);
    
    int drawText(final AnimationState p0, final int p1, final int p2, final CharSequence p3, final int p4, final int p5);
    
    FontCache cacheMultiLineText(final FontCache p0, final CharSequence p1, final int p2, final HAlignment p3);
    
    FontCache cacheText(final FontCache p0, final CharSequence p1);
    
    FontCache cacheText(final FontCache p0, final CharSequence p1, final int p2, final int p3);
}
