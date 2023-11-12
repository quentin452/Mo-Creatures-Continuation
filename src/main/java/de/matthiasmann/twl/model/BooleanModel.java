package de.matthiasmann.twl.model;

import de.matthiasmann.twl.utils.*;

public interface BooleanModel extends WithRunnableCallback
{
    boolean getValue();
    
    void setValue(final boolean p0);
}
