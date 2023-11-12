package de.matthiasmann.twl.textarea;

import java.util.*;
import de.matthiasmann.twl.utils.*;
import de.matthiasmann.twl.*;
import java.lang.reflect.*;

public final class StyleAttribute<T>
{
    private static final ArrayList<StyleAttribute<?>> attributes;
    public static final StyleAttribute<TextAreaModel.HAlignment> HORIZONTAL_ALIGNMENT;
    public static final StyleAttribute<TextAreaModel.VAlignment> VERTICAL_ALIGNMENT;
    public static final StyleAttribute<Value> TEXT_INDENT;
    public static final StyleAttribute<TextDecoration> TEXT_DECORATION;
    public static final StyleAttribute<TextDecoration> TEXT_DECORATION_HOVER;
    public static final StyleAttribute<StringList> FONT_FAMILIES;
    public static final StyleAttribute<Value> FONT_SIZE;
    public static final StyleAttribute<Integer> FONT_WEIGHT;
    public static final StyleAttribute<Boolean> FONT_ITALIC;
    public static final StyleAttribute<Integer> TAB_SIZE;
    public static final StyleAttribute<String> LIST_STYLE_IMAGE;
    public static final StyleAttribute<OrderedListType> LIST_STYLE_TYPE;
    public static final StyleAttribute<Boolean> PREFORMATTED;
    public static final StyleAttribute<Boolean> BREAKWORD;
    public static final StyleAttribute<Color> COLOR;
    public static final StyleAttribute<Color> COLOR_HOVER;
    public static final StyleAttribute<Boolean> INHERIT_HOVER;
    public static final StyleAttribute<TextAreaModel.Clear> CLEAR;
    public static final StyleAttribute<TextAreaModel.Display> DISPLAY;
    public static final StyleAttribute<TextAreaModel.FloatPosition> FLOAT_POSITION;
    public static final StyleAttribute<Value> WIDTH;
    public static final StyleAttribute<Value> HEIGHT;
    public static final StyleAttribute<String> BACKGROUND_IMAGE;
    public static final StyleAttribute<Color> BACKGROUND_COLOR;
    public static final StyleAttribute<Color> BACKGROUND_COLOR_HOVER;
    public static final StyleAttribute<Value> MARGIN_TOP;
    public static final StyleAttribute<Value> MARGIN_LEFT;
    public static final StyleAttribute<Value> MARGIN_RIGHT;
    public static final StyleAttribute<Value> MARGIN_BOTTOM;
    public static final StyleAttribute<Value> PADDING_TOP;
    public static final StyleAttribute<Value> PADDING_LEFT;
    public static final StyleAttribute<Value> PADDING_RIGHT;
    public static final StyleAttribute<Value> PADDING_BOTTOM;
    public static final BoxAttribute MARGIN;
    public static final BoxAttribute PADDING;
    private final boolean inherited;
    private final Class<T> dataType;
    private final T defaultValue;
    private final int ordinal;
    
    public boolean isInherited() {
        return this.inherited;
    }
    
    public Class<T> getDataType() {
        return this.dataType;
    }
    
    public T getDefaultValue() {
        return this.defaultValue;
    }
    
    public int ordinal() {
        return this.ordinal;
    }
    
    public String name() {
        try {
            for (final Field f : StyleAttribute.class.getFields()) {
                if (Modifier.isStatic(f.getModifiers()) && f.get(null) == this) {
                    return f.getName();
                }
            }
        }
        catch (Throwable t) {}
        return "?";
    }
    
    @Override
    public String toString() {
        return this.name();
    }
    
    private StyleAttribute(final boolean inherited, final Class<T> dataType, final T defaultValue) {
        this.inherited = inherited;
        this.dataType = dataType;
        this.defaultValue = defaultValue;
        this.ordinal = StyleAttribute.attributes.size();
        StyleAttribute.attributes.add(this);
    }
    
    public static int getNumAttributes() {
        return StyleAttribute.attributes.size();
    }
    
    public static StyleAttribute<?> getAttribute(final int ordinal) throws IndexOutOfBoundsException {
        return StyleAttribute.attributes.get(ordinal);
    }
    
    public static StyleAttribute<?> getAttribute(final String name) throws IllegalArgumentException {
        try {
            final Field f = StyleAttribute.class.getField(name);
            if (Modifier.isStatic(f.getModifiers()) && f.getType() == StyleAttribute.class) {
                return (StyleAttribute<?>)f.get(null);
            }
        }
        catch (Throwable t) {}
        throw new IllegalArgumentException("No style attribute " + name);
    }
    
    static {
        attributes = new ArrayList<StyleAttribute<?>>();
        HORIZONTAL_ALIGNMENT = new StyleAttribute<TextAreaModel.HAlignment>(true, TextAreaModel.HAlignment.class, TextAreaModel.HAlignment.LEFT);
        VERTICAL_ALIGNMENT = new StyleAttribute<TextAreaModel.VAlignment>(true, TextAreaModel.VAlignment.class, TextAreaModel.VAlignment.BOTTOM);
        TEXT_INDENT = new StyleAttribute<Value>(true, Value.class, Value.ZERO_PX);
        TEXT_DECORATION = new StyleAttribute<TextDecoration>(true, TextDecoration.class, TextDecoration.NONE);
        TEXT_DECORATION_HOVER = new StyleAttribute<TextDecoration>(true, TextDecoration.class, null);
        FONT_FAMILIES = new StyleAttribute<StringList>(true, StringList.class, new StringList("default"));
        FONT_SIZE = new StyleAttribute<Value>(true, Value.class, new Value(14.0f, Value.Unit.PX));
        FONT_WEIGHT = new StyleAttribute<Integer>(true, Integer.class, 400);
        FONT_ITALIC = new StyleAttribute<Boolean>(true, Boolean.class, Boolean.FALSE);
        TAB_SIZE = new StyleAttribute<Integer>(true, Integer.class, 8);
        LIST_STYLE_IMAGE = new StyleAttribute<String>(true, String.class, "ul-bullet");
        LIST_STYLE_TYPE = new StyleAttribute<OrderedListType>(true, OrderedListType.class, OrderedListType.DECIMAL);
        PREFORMATTED = new StyleAttribute<Boolean>(true, Boolean.class, Boolean.FALSE);
        BREAKWORD = new StyleAttribute<Boolean>(true, Boolean.class, Boolean.FALSE);
        COLOR = new StyleAttribute<Color>(true, Color.class, Color.WHITE);
        COLOR_HOVER = new StyleAttribute<Color>(true, Color.class, null);
        INHERIT_HOVER = new StyleAttribute<Boolean>(true, Boolean.class, Boolean.FALSE);
        CLEAR = new StyleAttribute<TextAreaModel.Clear>(false, TextAreaModel.Clear.class, TextAreaModel.Clear.NONE);
        DISPLAY = new StyleAttribute<TextAreaModel.Display>(false, TextAreaModel.Display.class, TextAreaModel.Display.INLINE);
        FLOAT_POSITION = new StyleAttribute<TextAreaModel.FloatPosition>(false, TextAreaModel.FloatPosition.class, TextAreaModel.FloatPosition.NONE);
        WIDTH = new StyleAttribute<Value>(false, Value.class, Value.AUTO);
        HEIGHT = new StyleAttribute<Value>(false, Value.class, Value.AUTO);
        BACKGROUND_IMAGE = new StyleAttribute<String>(false, String.class, null);
        BACKGROUND_COLOR = new StyleAttribute<Color>(false, Color.class, Color.TRANSPARENT);
        BACKGROUND_COLOR_HOVER = new StyleAttribute<Color>(false, Color.class, Color.TRANSPARENT);
        MARGIN_TOP = new StyleAttribute<Value>(false, Value.class, Value.ZERO_PX);
        MARGIN_LEFT = new StyleAttribute<Value>(false, Value.class, Value.ZERO_PX);
        MARGIN_RIGHT = new StyleAttribute<Value>(false, Value.class, Value.ZERO_PX);
        MARGIN_BOTTOM = new StyleAttribute<Value>(false, Value.class, Value.ZERO_PX);
        PADDING_TOP = new StyleAttribute<Value>(false, Value.class, Value.ZERO_PX);
        PADDING_LEFT = new StyleAttribute<Value>(false, Value.class, Value.ZERO_PX);
        PADDING_RIGHT = new StyleAttribute<Value>(false, Value.class, Value.ZERO_PX);
        PADDING_BOTTOM = new StyleAttribute<Value>(false, Value.class, Value.ZERO_PX);
        MARGIN = new BoxAttribute((StyleAttribute)StyleAttribute.MARGIN_TOP, (StyleAttribute)StyleAttribute.MARGIN_LEFT, (StyleAttribute)StyleAttribute.MARGIN_RIGHT, (StyleAttribute)StyleAttribute.MARGIN_BOTTOM);
        PADDING = new BoxAttribute((StyleAttribute)StyleAttribute.PADDING_TOP, (StyleAttribute)StyleAttribute.PADDING_LEFT, (StyleAttribute)StyleAttribute.PADDING_RIGHT, (StyleAttribute)StyleAttribute.PADDING_BOTTOM);
    }
}
