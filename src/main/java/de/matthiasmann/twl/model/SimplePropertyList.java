package de.matthiasmann.twl.model;

import java.util.*;

public class SimplePropertyList extends AbstractProperty<PropertyList> implements PropertyList
{
    private final String name;
    private final ArrayList<Property<?>> properties;
    
    public SimplePropertyList(final String name) {
        this.name = name;
        this.properties = new ArrayList<Property<?>>();
    }
    
    public SimplePropertyList(final String name, final Property<?>... properties) {
        this(name);
        this.properties.addAll(Arrays.asList(properties));
    }
    
    public String getName() {
        return this.name;
    }
    
    public boolean isReadOnly() {
        return true;
    }
    
    public boolean canBeNull() {
        return false;
    }
    
    public PropertyList getPropertyValue() {
        return (PropertyList)this;
    }
    
    public void setPropertyValue(final PropertyList value) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported");
    }
    
    public Class<PropertyList> getType() {
        return PropertyList.class;
    }
    
    public int getNumProperties() {
        return this.properties.size();
    }
    
    public Property<?> getProperty(final int idx) {
        return this.properties.get(idx);
    }
    
    public void addProperty(final Property<?> property) {
        this.properties.add(property);
        this.fireValueChangedCallback();
    }
    
    public void addProperty(final int idx, final Property<?> property) {
        this.properties.add(idx, property);
        this.fireValueChangedCallback();
    }
    
    public void removeProperty(final int idx) {
        this.properties.remove(idx);
        this.fireValueChangedCallback();
    }
    
    public void removeAllProperties() {
        this.properties.clear();
        this.fireValueChangedCallback();
    }
}
