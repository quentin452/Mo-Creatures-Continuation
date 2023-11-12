package de.matthiasmann.twl.model;

public class EnumListModel<T extends Enum<T>> extends SimpleListModel<T>
{
    private final Class<T> enumClass;
    private final T[] enumValues;
    
    public EnumListModel(final Class<T> enumClass) {
        if (!enumClass.isEnum()) {
            throw new IllegalArgumentException("not an enum class");
        }
        this.enumClass = enumClass;
        this.enumValues = enumClass.getEnumConstants();
    }
    
    public Class<T> getEnumClass() {
        return this.enumClass;
    }
    
    public T getEntry(final int index) {
        return this.enumValues[index];
    }
    
    public int getNumEntries() {
        return this.enumValues.length;
    }
    
    public int findEntry(final T value) {
        for (int i = 0, n = this.enumValues.length; i < n; ++i) {
            if (this.enumValues[i] == value) {
                return i;
            }
        }
        return -1;
    }
}
