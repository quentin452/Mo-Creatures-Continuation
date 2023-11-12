package de.matthiasmann.twl.model;

import de.matthiasmann.twl.utils.*;
import de.matthiasmann.twl.*;

public interface ColorModel extends WithRunnableCallback
{
    Color getValue();
    
    void setValue(final Color p0);
}
