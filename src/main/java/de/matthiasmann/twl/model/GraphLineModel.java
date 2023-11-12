package de.matthiasmann.twl.model;

public interface GraphLineModel
{
    String getVisualStyleName();
    
    int getNumPoints();
    
    float getPoint(final int p0);
    
    float getMinValue();
    
    float getMaxValue();
}
