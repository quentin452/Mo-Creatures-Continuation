package de.matthiasmann.twl.textarea;

import java.util.*;

public class Style
{
    private final Style parent;
    private final StyleSheetKey styleSheetKey;
    private Object[] values;
    
    public Style() {
        this(null, null);
    }
    
    public Style(final Style parent, final StyleSheetKey styleSheetKey) {
        this.parent = parent;
        this.styleSheetKey = styleSheetKey;
    }
    
    public Style(final Style parent, final StyleSheetKey styleSheetKey, final Map<StyleAttribute<?>, Object> values) {
        this(parent, styleSheetKey);
        if (values != null) {
            this.putAll(values);
        }
    }
    
    protected Style(final Style src) {
        this.parent = src.parent;
        this.styleSheetKey = src.styleSheetKey;
        this.values = (Object[])((src.values != null) ? ((Object[])src.values.clone()) : null);
    }
    
    public Style resolve(final StyleAttribute<?> attribute, final StyleSheetResolver resolver) {
        if (!attribute.isInherited()) {
            return this;
        }
        return doResolve(this, attribute.ordinal(), resolver);
    }
    
    private static Style doResolve(Style style, final int ord, final StyleSheetResolver resolver) {
        while (style.parent != null) {
            if (style.rawGet(ord) != null) {
                return style;
            }
            if (resolver != null && style.styleSheetKey != null) {
                final Style styleSheetStyle = resolver.resolve(style);
                if (styleSheetStyle != null && styleSheetStyle.rawGet(ord) != null) {
                    return style;
                }
            }
            style = style.parent;
        }
        return style;
    }
    
    public <V> V getNoResolve(final StyleAttribute<V> attribute, final StyleSheetResolver resolver) {
        Object value = this.rawGet(attribute.ordinal());
        if (value == null) {
            if (resolver != null && this.styleSheetKey != null) {
                final Style styleSheetStyle = resolver.resolve(this);
                if (styleSheetStyle != null) {
                    value = styleSheetStyle.rawGet(attribute.ordinal());
                }
            }
            if (value == null) {
                return attribute.getDefaultValue();
            }
        }
        return attribute.getDataType().cast(value);
    }
    
    public <V> V get(final StyleAttribute<V> attribute, final StyleSheetResolver resolver) {
        return (V)this.resolve(attribute, resolver).getNoResolve((StyleAttribute<Object>)attribute, resolver);
    }
    
    public <V> V getRaw(final StyleAttribute<V> attribute) {
        final Object value = this.rawGet(attribute.ordinal());
        return attribute.getDataType().cast(value);
    }
    
    public Style getParent() {
        return this.parent;
    }
    
    public StyleSheetKey getStyleSheetKey() {
        return this.styleSheetKey;
    }
    
    public Style with(final Map<StyleAttribute<?>, Object> values) {
        final Style newStyle = new Style(this);
        newStyle.putAll(values);
        return newStyle;
    }
    
    public <V> Style with(final StyleAttribute<V> attribute, final V value) {
        final Style newStyle = new Style(this);
        newStyle.put(attribute, value);
        return newStyle;
    }
    
    public Style withoutNonInheritable() {
        if (this.values != null) {
            for (int i = 0, n = this.values.length; i < n; ++i) {
                if (this.values[i] != null && !StyleAttribute.getAttribute(i).isInherited()) {
                    return this.withoutNonInheritableCopy();
                }
            }
        }
        return this;
    }
    
    private Style withoutNonInheritableCopy() {
        final Style result = new Style(this.parent, this.styleSheetKey);
        for (int i = 0, n = this.values.length; i < n; ++i) {
            final Object value = this.values[i];
            if (value != null) {
                final StyleAttribute<?> attribute = StyleAttribute.getAttribute(i);
                if (attribute.isInherited()) {
                    result.put(attribute, value);
                }
            }
        }
        return result;
    }
    
    protected void put(final StyleAttribute<?> attribute, final Object value) {
        if (attribute == null) {
            throw new IllegalArgumentException("attribute is null");
        }
        if (value == null) {
            if (this.values == null) {
                return;
            }
        }
        else {
            if (!attribute.getDataType().isInstance(value)) {
                throw new IllegalArgumentException("value is a " + value.getClass() + " but must be a " + attribute.getDataType());
            }
            this.ensureValues();
        }
        this.values[attribute.ordinal()] = value;
    }
    
    protected final void putAll(final Map<StyleAttribute<?>, Object> values) {
        for (final Map.Entry<StyleAttribute<?>, Object> e : values.entrySet()) {
            this.put(e.getKey(), e.getValue());
        }
    }
    
    protected final void putAll(final Style src) {
        if (src.values != null) {
            this.ensureValues();
            for (int i = 0, n = this.values.length; i < n; ++i) {
                final Object value = src.values[i];
                if (value != null) {
                    this.values[i] = value;
                }
            }
        }
    }
    
    protected final void ensureValues() {
        if (this.values == null) {
            this.values = new Object[StyleAttribute.getNumAttributes()];
        }
    }
    
    protected final Object rawGet(final int idx) {
        final Object[] vals = this.values;
        if (vals != null) {
            return vals[idx];
        }
        return null;
    }
    
    public Map<StyleAttribute<?>, Object> toMap() {
        final HashMap<StyleAttribute<?>, Object> result = new HashMap<StyleAttribute<?>, Object>();
        for (int ord = 0; ord < this.values.length; ++ord) {
            final Object value = this.values[ord];
            if (value != null) {
                result.put(StyleAttribute.getAttribute(ord), value);
            }
        }
        return result;
    }
}
