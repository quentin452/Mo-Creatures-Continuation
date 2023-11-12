package de.matthiasmann.twl.model;

public class SimpleEnumModel<T extends Enum<T>> extends AbstractEnumModel<T>
{
    private T value;
    
    public SimpleEnumModel(final Class<T> clazz, final T value) {
        super((Class)clazz);
        if (value == null) {
            throw new NullPointerException("value");
        }
        if (!clazz.isInstance(value)) {
            throw new IllegalArgumentException("value");
        }
        this.value = value;
    }
    
    public T getValue() {
        return this.value;
    }
    
    public void setValue(final T value) {
        if (!this.getEnumClass().isInstance(value)) {
            throw new IllegalArgumentException("value");
        }
        if (this.value != value) {
            this.value = value;
            this.doCallback();
        }
    }
}
