package de.matthiasmann.twl.model;

import de.matthiasmann.twl.utils.*;

public interface DateModel extends WithRunnableCallback
{
    long getValue();
    
    void setValue(final long p0);
}
