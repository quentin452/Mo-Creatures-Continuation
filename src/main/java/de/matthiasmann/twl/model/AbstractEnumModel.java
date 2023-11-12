package de.matthiasmann.twl.model;

public abstract class AbstractEnumModel<T extends Enum<T>> extends HasCallback implements EnumModel<T>
{
    private final Class<T> enumClass;
    
    protected AbstractEnumModel(final Class<T> clazz) {
        if (clazz == null) {
            throw new NullPointerException("clazz");
        }
        this.enumClass = clazz;
    }
    
    @Override
    public Class<T> getEnumClass() {
        return this.enumClass;
    }
}
