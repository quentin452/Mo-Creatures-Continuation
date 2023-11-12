package de.matthiasmann.twl.renderer;

import java.util.*;
import de.matthiasmann.twl.*;

public final class FontParameter
{
    static final HashMap<String, Parameter<?>> parameterMap;
    public static final Parameter<Color> COLOR;
    public static final Parameter<Boolean> UNDERLINE;
    public static final Parameter<Boolean> LINETHROUGH;
    private Object[] values;
    
    public FontParameter() {
        this.values = new Object[8];
    }
    
    public FontParameter(final FontParameter base) {
        this.values = base.values.clone();
    }
    
    public <T> void put(final Parameter<T> param, final T value) {
        if (param == null) {
            throw new NullPointerException("type");
        }
        if (value != null && !param.dataClass.isInstance(value)) {
            throw new ClassCastException("value");
        }
        final int ordinal = param.ordinal;
        final int curLength = this.values.length;
        if (ordinal >= curLength) {
            final Object[] tmp = new Object[Math.max(ordinal + 1, curLength * 2)];
            System.arraycopy(this.values, 0, tmp, 0, curLength);
            this.values = tmp;
        }
        this.values[ordinal] = value;
    }
    
    public <T> T get(final Parameter<T> param) {
        if (param.ordinal < this.values.length) {
            final Object raw = this.values[param.ordinal];
            if (raw != null) {
                return param.dataClass.cast(raw);
            }
        }
        return param.defaultValue;
    }
    
    public static Parameter[] getRegisteredParameter() {
        synchronized (FontParameter.parameterMap) {
            return FontParameter.parameterMap.values().toArray(new Parameter[FontParameter.parameterMap.size()]);
        }
    }
    
    public static Parameter<?> getParameter(final String name) {
        synchronized (FontParameter.parameterMap) {
            return FontParameter.parameterMap.get(name);
        }
    }
    
    public static <T> Parameter<T> newParameter(final String name, final T defaultValue) {
        if (defaultValue == null) {
            throw new NullPointerException("defaultValue");
        }
        final Class<T> dataClass = (Class<T>)defaultValue.getClass();
        return newParameter(name, dataClass, defaultValue);
    }
    
    public static <T> Parameter<T> newParameter(final String name, final Class<T> dataClass, final T defaultValue) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (dataClass == null) {
            throw new NullPointerException("dataClass");
        }
        synchronized (FontParameter.parameterMap) {
            final Parameter<?> existing = FontParameter.parameterMap.get(name);
            if (existing == null) {
                final Parameter<T> type = new Parameter<T>(name, dataClass, defaultValue, FontParameter.parameterMap.size());
                FontParameter.parameterMap.put(name, type);
                return type;
            }
            if (existing.dataClass != dataClass || !equals(existing.defaultValue, defaultValue)) {
                throw new IllegalStateException("type '" + name + "' already registered but different");
            }
            final Parameter<T> type = (Parameter<T>)existing;
            return type;
        }
    }
    
    private static boolean equals(final Object a, final Object b) {
        return a == b || (a != null && a.equals(b));
    }
    
    static {
        parameterMap = new HashMap<String, Parameter<?>>();
        COLOR = newParameter("color", Color.WHITE);
        UNDERLINE = newParameter("underline", false);
        LINETHROUGH = newParameter("linethrough", false);
    }
    
    public static final class Parameter<T>
    {
        final String name;
        final Class<T> dataClass;
        final T defaultValue;
        final int ordinal;
        
        Parameter(final String name, final Class<T> dataClass, final T defaultValue, final int ordinal) {
            this.name = name;
            this.dataClass = dataClass;
            this.defaultValue = defaultValue;
            this.ordinal = ordinal;
        }
        
        public final String getName() {
            return this.name;
        }
        
        public final Class<T> getDataClass() {
            return this.dataClass;
        }
        
        public final T getDefaultValue() {
            return this.defaultValue;
        }
        
        @Override
        public String toString() {
            return this.ordinal + ":" + this.name + ":" + this.dataClass.getSimpleName();
        }
    }
}
