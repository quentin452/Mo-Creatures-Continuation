package de.matthiasmann.twl.model;

public interface Property<T>
{
    String getName();
    
    boolean isReadOnly();
    
    boolean canBeNull();
    
    T getPropertyValue();
    
    void setPropertyValue(final T p0) throws IllegalArgumentException;
    
    Class<T> getType();
    
    void addValueChangedCallback(final Runnable p0);
    
    void removeValueChangedCallback(final Runnable p0);
}
