package de.matthiasmann.twl.theme;

import java.util.*;
import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.*;

public class ParameterListImpl extends ThemeChildImpl implements ParameterList
{
    final ArrayList<Object> params;
    
    ParameterListImpl(final ThemeManager manager, final ThemeInfoImpl parent) {
        super(manager, parent);
        this.params = new ArrayList<Object>();
    }
    
    public int getSize() {
        return this.params.size();
    }
    
    public Font getFont(final int idx) {
        final Font value = this.getParameterValue(idx, Font.class);
        if (value != null) {
            return value;
        }
        return this.manager.getDefaultFont();
    }
    
    public Image getImage(final int idx) {
        final Image img = this.getParameterValue(idx, Image.class);
        if (img == ImageManager.NONE) {
            return null;
        }
        return img;
    }
    
    public MouseCursor getMouseCursor(final int idx) {
        final MouseCursor value = this.getParameterValue(idx, MouseCursor.class);
        return value;
    }
    
    public ParameterMap getParameterMap(final int idx) {
        final ParameterMap value = this.getParameterValue(idx, ParameterMap.class);
        if (value == null) {
            return (ParameterMap)this.manager.emptyMap;
        }
        return value;
    }
    
    public ParameterList getParameterList(final int idx) {
        final ParameterList value = this.getParameterValue(idx, ParameterList.class);
        if (value == null) {
            return (ParameterList)this.manager.emptyList;
        }
        return value;
    }
    
    public boolean getParameter(final int idx, final boolean defaultValue) {
        final Boolean value = this.getParameterValue(idx, Boolean.class);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }
    
    public int getParameter(final int idx, final int defaultValue) {
        final Integer value = this.getParameterValue(idx, Integer.class);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }
    
    public float getParameter(final int idx, final float defaultValue) {
        final Float value = this.getParameterValue(idx, Float.class);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }
    
    public String getParameter(final int idx, final String defaultValue) {
        final String value = this.getParameterValue(idx, String.class);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }
    
    public Color getParameter(final int idx, final Color defaultValue) {
        final Color value = this.getParameterValue(idx, Color.class);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }
    
    public <E extends Enum<E>> E getParameter(final int idx, final E defaultValue) {
        final Class<E> enumType = defaultValue.getDeclaringClass();
        final E value = this.getParameterValue(idx, enumType);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }
    
    public Object getParameterValue(final int idx) {
        return this.params.get(idx);
    }
    
    public <T> T getParameterValue(final int idx, final Class<T> clazz) {
        final Object value = this.getParameterValue(idx);
        if (value != null && !clazz.isInstance(value)) {
            this.wrongParameterType(idx, clazz, value.getClass());
            return null;
        }
        return clazz.cast(value);
    }
    
    protected void wrongParameterType(final int idx, final Class<?> expectedType, final Class<?> foundType) {
        DebugHook.getDebugHook().wrongParameterType((ParameterList)this, idx, (Class)expectedType, (Class)foundType, this.getParentDescription());
    }
}
