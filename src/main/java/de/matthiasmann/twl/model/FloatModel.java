package de.matthiasmann.twl.model;

import de.matthiasmann.twl.utils.*;

public interface FloatModel extends WithRunnableCallback
{
    float getValue();
    
    float getMinValue();
    
    float getMaxValue();
    
    void setValue(final float p0);
}
