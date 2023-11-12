package de.matthiasmann.twl.model;

public interface ColorSpace
{
    String getColorSpaceName();
    
    int getNumComponents();
    
    String getComponentName(final int p0);
    
    String getComponentShortName(final int p0);
    
    float getMinValue(final int p0);
    
    float getMaxValue(final int p0);
    
    float getDefaultValue(final int p0);
    
    int toRGB(final float[] p0);
    
    float[] fromRGB(final int p0);
}
