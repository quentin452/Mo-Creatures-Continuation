package de.matthiasmann.twl.model;

public class SimpleProperty<T> extends AbstractProperty<T>
{
    private final Class<T> type;
    private final String name;
    private boolean readOnly;
    private T value;

    public SimpleProperty(final Class<T> type, final String name, final T value) {
        this(type, name, value, false);
    }

    public SimpleProperty(final Class<T> type, final String name, final T value, final boolean readOnly) {
        this.type = type;
        this.name = name;
        this.readOnly = readOnly;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public void setReadOnly(final boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean canBeNull() {
        return false;
    }

    public T getPropertyValue() {
        return this.value;
    }

    public void setPropertyValue(final T value) throws IllegalArgumentException {
        if (value == null && !this.canBeNull()) {
            throw new NullPointerException("value");
        }
        if (this.valueChanged(value)) {
            this.value = value;
            this.fireValueChangedCallback();
        }
    }

    public Class<T> getType() {
        return this.type;
    }

    protected boolean valueChanged(final T newValue) {
        return this.value != newValue && (this.value == null || !this.value.equals(newValue));
    }
}
