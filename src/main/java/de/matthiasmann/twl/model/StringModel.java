package de.matthiasmann.twl.model;

import de.matthiasmann.twl.utils.*;

public interface StringModel extends WithRunnableCallback
{
    String getValue();
    
    void setValue(final String p0);
}
