package de.matthiasmann.twl.model;

import de.matthiasmann.twl.utils.*;

public interface IntegerModel extends WithRunnableCallback
{
    int getValue();
    
    int getMinValue();
    
    int getMaxValue();
    
    void setValue(final int p0);
}
