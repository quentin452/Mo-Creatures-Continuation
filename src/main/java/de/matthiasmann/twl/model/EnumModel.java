package de.matthiasmann.twl.model;

import de.matthiasmann.twl.utils.*;

public interface EnumModel<T extends Enum<T>> extends WithRunnableCallback
{
    Class<T> getEnumClass();
    
    T getValue();
    
    void setValue(final T p0);
}
