package de.matthiasmann.twl.theme;

import de.matthiasmann.twl.utils.*;
import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.*;
import java.util.*;

class ParameterMapImpl extends ThemeChildImpl implements ParameterMap
{
    private final CascadedHashMap<String, Object> params;
    private static final Class<?>[] BASE_CLASSES;
    
    ParameterMapImpl(final ThemeManager manager, final ThemeInfoImpl parent) {
        super(manager, parent);
        this.params = new CascadedHashMap<String, Object>();
    }
    
    void copy(final ParameterMapImpl src) {
        this.params.collapseAndSetFallback(src.params);
    }
    
    public Font getFont(final String name) {
        final Font value = this.getParameterValue(name, true, Font.class);
        if (value != null) {
            return value;
        }
        return this.manager.getDefaultFont();
    }
    
    public Image getImage(final String name) {
        final Image img = this.getParameterValue(name, true, Image.class);
        if (img == ImageManager.NONE) {
            return null;
        }
        return img;
    }
    
    public MouseCursor getMouseCursor(final String name) {
        final MouseCursor value = this.getParameterValue(name, false, MouseCursor.class);
        return value;
    }
    
    public ParameterMap getParameterMap(final String name) {
        final ParameterMap value = this.getParameterValue(name, true, ParameterMap.class);
        if (value == null) {
            return (ParameterMap)this.manager.emptyMap;
        }
        return value;
    }
    
    public ParameterList getParameterList(final String name) {
        final ParameterList value = this.getParameterValue(name, true, ParameterList.class);
        if (value == null) {
            return (ParameterList)this.manager.emptyList;
        }
        return value;
    }
    
    public boolean getParameter(final String name, final boolean defaultValue) {
        final Boolean value = this.getParameterValue(name, true, Boolean.class);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }
    
    public int getParameter(final String name, final int defaultValue) {
        final Integer value = this.getParameterValue(name, true, Integer.class);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }
    
    public float getParameter(final String name, final float defaultValue) {
        final Float value = this.getParameterValue(name, true, Float.class);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }
    
    public String getParameter(final String name, final String defaultValue) {
        final String value = this.getParameterValue(name, true, String.class);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }
    
    public Color getParameter(final String name, final Color defaultValue) {
        final Color value = this.getParameterValue(name, true, Color.class);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }
    
    public <E extends Enum<E>> E getParameter(final String name, final E defaultValue) {
        final Class<E> enumType = defaultValue.getDeclaringClass();
        final E value = this.getParameterValue(name, true, enumType);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }
    
    public Object getParameterValue(final String name, final boolean warnIfNotPresent) {
        final Object value = this.params.get(name);
        if (value == null && warnIfNotPresent) {
            this.missingParameter(name, null);
        }
        return value;
    }
    
    public <T> T getParameterValue(final String name, final boolean warnIfNotPresent, final Class<T> clazz) {
        return this.getParameterValue(name, warnIfNotPresent, clazz, (T)null);
    }
    
    public <T> T getParameterValue(final String name, final boolean warnIfNotPresent, final Class<T> clazz, final T defaultValue) {
        final Object value = this.params.get(name);
        if (value == null && warnIfNotPresent) {
            this.missingParameter(name, clazz);
        }
        if (!clazz.isInstance(value)) {
            if (value != null) {
                this.wrongParameterType(name, clazz, value.getClass());
            }
            return defaultValue;
        }
        return clazz.cast(value);
    }
    
    protected void wrongParameterType(final String paramName, final Class<?> expectedType, final Class<?> foundType) {
        DebugHook.getDebugHook().wrongParameterType((ParameterMap)this, paramName, (Class)expectedType, (Class)foundType, this.getParentDescription());
    }
    
    protected void missingParameter(final String paramName, final Class<?> dataType) {
        DebugHook.getDebugHook().missingParameter((ParameterMap)this, paramName, this.getParentDescription(), (Class)dataType);
    }
    
    protected void replacingWithDifferentType(final String paramName, final Class<?> oldType, final Class<?> newType) {
        DebugHook.getDebugHook().replacingWithDifferentType((ParameterMap)this, paramName, (Class)oldType, (Class)newType, this.getParentDescription());
    }
    
    Object getParam(final String name) {
        return this.params.get(name);
    }
    
    void put(final Map<String, ?> params) {
        for (final Map.Entry<String, ?> e : params.entrySet()) {
            this.put(e.getKey(), e.getValue());
        }
    }
    
    void put(final String paramName, final Object value) {
        final Object old = this.params.put(paramName, value);
        if (old != null && value != null) {
            final Class<?> oldClass = old.getClass();
            final Class<?> newClass = value.getClass();
            if (oldClass != newClass && !areTypesCompatible(oldClass, newClass)) {
                this.replacingWithDifferentType(paramName, oldClass, newClass);
            }
        }
    }
    
    private static boolean areTypesCompatible(final Class<?> classA, final Class<?> classB) {
        for (final Class<?> clazz : ParameterMapImpl.BASE_CLASSES) {
            if (clazz.isAssignableFrom(classA) && clazz.isAssignableFrom(classB)) {
                return true;
            }
        }
        return false;
    }
    
    static {
        BASE_CLASSES = new Class[] { Image.class, Font.class, MouseCursor.class };
    }
}
