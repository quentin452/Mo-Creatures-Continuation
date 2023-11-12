package de.matthiasmann.twl.textarea;

import java.util.logging.*;
import de.matthiasmann.twl.utils.*;
import java.util.*;
import de.matthiasmann.twl.*;

public class CSSStyle extends Style
{
    static final HashMap<String, Boolean> PRE;
    static final HashMap<String, Boolean> BREAKWORD;
    static final HashMap<String, OrderedListType> OLT;
    static final HashMap<String, Boolean> ITALIC;
    static final HashMap<String, Integer> WEIGHTS;
    static final HashMap<String, TextDecoration> TEXTDECORATION;
    static final HashMap<String, Boolean> INHERITHOVER;
    
    protected CSSStyle() {
    }
    
    public CSSStyle(final String cssStyle) {
        this.parseCSS(cssStyle);
    }
    
    public CSSStyle(final Style parent, final StyleSheetKey styleSheetKey, final String cssStyle) {
        super(parent, styleSheetKey);
        this.parseCSS(cssStyle);
    }
    
    private void parseCSS(final String style) {
        final ParameterStringParser psp = new ParameterStringParser(style, ';', ':');
        psp.setTrim(true);
        while (psp.next()) {
            try {
                this.parseCSSAttribute(psp.getKey(), psp.getValue());
            }
            catch (IllegalArgumentException ex) {
                Logger.getLogger(CSSStyle.class.getName()).log(Level.SEVERE, "Unable to parse CSS attribute: " + psp.getKey() + "=" + psp.getValue(), ex);
            }
        }
    }
    
    protected void parseCSSAttribute(final String key, final String value) {
        if (key.startsWith("margin")) {
            this.parseBox(key.substring(6), value, StyleAttribute.MARGIN);
            return;
        }
        if (key.startsWith("padding")) {
            this.parseBox(key.substring(7), value, StyleAttribute.PADDING);
            return;
        }
        if (key.startsWith("font")) {
            this.parseFont(key, value);
            return;
        }
        if ("text-indent".equals(key)) {
            this.parseValueUnit(StyleAttribute.TEXT_INDENT, value);
            return;
        }
        if ("-twl-font".equals(key)) {
            this.put(StyleAttribute.FONT_FAMILIES, new StringList(value));
            return;
        }
        if ("-twl-hover".equals(key)) {
            this.parseEnum(StyleAttribute.INHERIT_HOVER, CSSStyle.INHERITHOVER, value);
            return;
        }
        if ("text-align".equals(key)) {
            this.parseEnum(StyleAttribute.HORIZONTAL_ALIGNMENT, value);
            return;
        }
        if ("text-decoration".equals(key)) {
            this.parseEnum(StyleAttribute.TEXT_DECORATION, CSSStyle.TEXTDECORATION, value);
            return;
        }
        if ("vertical-align".equals(key)) {
            this.parseEnum(StyleAttribute.VERTICAL_ALIGNMENT, value);
            return;
        }
        if ("white-space".equals(key)) {
            this.parseEnum(StyleAttribute.PREFORMATTED, CSSStyle.PRE, value);
            return;
        }
        if ("word-wrap".equals(key)) {
            this.parseEnum(StyleAttribute.BREAKWORD, CSSStyle.BREAKWORD, value);
            return;
        }
        if ("list-style-image".equals(key)) {
            this.parseURL(StyleAttribute.LIST_STYLE_IMAGE, value);
            return;
        }
        if ("list-style-type".equals(key)) {
            this.parseEnum(StyleAttribute.LIST_STYLE_TYPE, CSSStyle.OLT, value);
            return;
        }
        if ("clear".equals(key)) {
            this.parseEnum(StyleAttribute.CLEAR, value);
            return;
        }
        if ("float".equals(key)) {
            this.parseEnum(StyleAttribute.FLOAT_POSITION, value);
            return;
        }
        if ("display".equals(key)) {
            this.parseEnum(StyleAttribute.DISPLAY, value);
            return;
        }
        if ("width".equals(key)) {
            this.parseValueUnit(StyleAttribute.WIDTH, value);
            return;
        }
        if ("height".equals(key)) {
            this.parseValueUnit(StyleAttribute.HEIGHT, value);
            return;
        }
        if ("background-image".equals(key)) {
            this.parseURL(StyleAttribute.BACKGROUND_IMAGE, value);
            return;
        }
        if ("background-color".equals(key) || "-twl-background-color".equals(key)) {
            this.parseColor(StyleAttribute.BACKGROUND_COLOR, value);
            return;
        }
        if ("color".equals(key)) {
            this.parseColor(StyleAttribute.COLOR, value);
            return;
        }
        if ("tab-size".equals(key) || "-moz-tab-size".equals(key)) {
            this.parseInteger(StyleAttribute.TAB_SIZE, value);
            return;
        }
        throw new IllegalArgumentException("Unsupported key: " + key);
    }
    
    private void parseBox(final String key, final String value, final BoxAttribute box) {
        if ("-top".equals(key)) {
            this.parseValueUnit(box.top, value);
        }
        else if ("-left".equals(key)) {
            this.parseValueUnit(box.left, value);
        }
        else if ("-right".equals(key)) {
            this.parseValueUnit(box.right, value);
        }
        else if ("-bottom".equals(key)) {
            this.parseValueUnit(box.bottom, value);
        }
        else if ("".equals(key)) {
            final Value[] vu = this.parseValueUnits(value);
            switch (vu.length) {
                case 1: {
                    this.put(box.top, vu[0]);
                    this.put(box.left, vu[0]);
                    this.put(box.right, vu[0]);
                    this.put(box.bottom, vu[0]);
                    break;
                }
                case 2: {
                    this.put(box.top, vu[0]);
                    this.put(box.left, vu[1]);
                    this.put(box.right, vu[1]);
                    this.put(box.bottom, vu[0]);
                    break;
                }
                case 3: {
                    this.put(box.top, vu[0]);
                    this.put(box.left, vu[1]);
                    this.put(box.right, vu[1]);
                    this.put(box.bottom, vu[2]);
                    break;
                }
                case 4: {
                    this.put(box.top, vu[0]);
                    this.put(box.left, vu[3]);
                    this.put(box.right, vu[1]);
                    this.put(box.bottom, vu[2]);
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Invalid number of margin values: " + vu.length);
                }
            }
        }
    }
    
    private void parseFont(final String key, String value) {
        if ("font-family".equals(key)) {
            this.parseList(StyleAttribute.FONT_FAMILIES, value);
            return;
        }
        if ("font-weight".equals(key)) {
            Integer weight = CSSStyle.WEIGHTS.get(value);
            if (weight == null) {
                weight = Integer.valueOf(value);
            }
            this.put(StyleAttribute.FONT_WEIGHT, weight);
            return;
        }
        if ("font-size".equals(key)) {
            this.parseValueUnit(StyleAttribute.FONT_SIZE, value);
            return;
        }
        if ("font-style".equals(key)) {
            this.parseEnum(StyleAttribute.FONT_ITALIC, CSSStyle.ITALIC, value);
            return;
        }
        if ("font".equals(key)) {
            value = this.parseStartsWith(StyleAttribute.FONT_WEIGHT, CSSStyle.WEIGHTS, value);
            value = this.parseStartsWith(StyleAttribute.FONT_ITALIC, CSSStyle.ITALIC, value);
            if (value.length() > 0 && Character.isDigit(value.charAt(0))) {
                int end = TextUtil.indexOf(value, ' ', 0);
                this.parseValueUnit(StyleAttribute.FONT_SIZE, value.substring(0, end));
                end = TextUtil.skipSpaces(value, end);
                value = value.substring(end);
            }
            this.parseList(StyleAttribute.FONT_FAMILIES, value);
        }
    }
    
    private Value parseValueUnit(final String value) {
        int suffixLength = 2;
        Value.Unit unit;
        if (value.endsWith("px")) {
            unit = Value.Unit.PX;
        }
        else if (value.endsWith("pt")) {
            unit = Value.Unit.PT;
        }
        else if (value.endsWith("em")) {
            unit = Value.Unit.EM;
        }
        else if (value.endsWith("ex")) {
            unit = Value.Unit.EX;
        }
        else if (value.endsWith("%")) {
            suffixLength = 1;
            unit = Value.Unit.PERCENT;
        }
        else {
            if ("0".equals(value)) {
                return Value.ZERO_PX;
            }
            if ("auto".equals(value)) {
                return Value.AUTO;
            }
            throw new IllegalArgumentException("Unknown numeric suffix: " + value);
        }
        final String numberPart = TextUtil.trim(value, 0, value.length() - suffixLength);
        return new Value(Float.parseFloat(numberPart), unit);
    }
    
    private Value[] parseValueUnits(final String value) {
        final String[] parts = value.split("\\s+");
        final Value[] result = new Value[parts.length];
        for (int i = 0; i < parts.length; ++i) {
            result[i] = this.parseValueUnit(parts[i]);
        }
        return result;
    }
    
    private void parseValueUnit(final StyleAttribute<?> attribute, final String value) {
        this.put(attribute, this.parseValueUnit(value));
    }
    
    private void parseInteger(final StyleAttribute<Integer> attribute, final String value) {
        if ("inherit".equals(value)) {
            this.put(attribute, null);
        }
        else {
            final int intval = Integer.parseInt(value);
            this.put(attribute, intval);
        }
    }
    
    private <T> void parseEnum(final StyleAttribute<T> attribute, final HashMap<String, T> map, final String value) {
        final T obj = map.get(value);
        if (obj == null) {
            throw new IllegalArgumentException("Unknown value: " + value);
        }
        this.put(attribute, obj);
    }
    
    private <E extends Enum<E>> void parseEnum(final StyleAttribute<E> attribute, final String value) {
        final E obj = Enum.valueOf(attribute.getDataType(), value.toUpperCase(Locale.ENGLISH));
        this.put(attribute, obj);
    }
    
    private <E> String parseStartsWith(final StyleAttribute<E> attribute, final HashMap<String, E> map, String value) {
        int end = TextUtil.indexOf(value, ' ', 0);
        final E obj = map.get(value.substring(0, end));
        if (obj != null) {
            end = TextUtil.skipSpaces(value, end);
            value = value.substring(end);
        }
        this.put(attribute, obj);
        return value;
    }
    
    private void parseURL(final StyleAttribute<String> attribute, final String value) {
        this.put(attribute, stripURL(value));
    }
    
    static String stripTrim(final String value, final int start, final int end) {
        return TextUtil.trim(value, start, value.length() - end);
    }
    
    static String stripURL(String value) {
        if (value.startsWith("url(") && value.endsWith(")")) {
            value = stripQuotes(stripTrim(value, 4, 1));
        }
        return value;
    }
    
    static String stripQuotes(String value) {
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }
    
    private void parseColor(final StyleAttribute<Color> attribute, String value) {
        Color color;
        if (value.startsWith("rgb(") && value.endsWith(")")) {
            value = stripTrim(value, 4, 1);
            final byte[] rgb = this.parseRGBA(value, 3);
            color = new Color(rgb[0], rgb[1], rgb[2], (byte)(-1));
        }
        else if (value.startsWith("rgba(") && value.endsWith(")")) {
            value = stripTrim(value, 5, 1);
            final byte[] rgba = this.parseRGBA(value, 4);
            color = new Color(rgba[0], rgba[1], rgba[2], rgba[3]);
        }
        else {
            color = Color.parserColor(value);
            if (color == null) {
                throw new IllegalArgumentException("unknown color name: " + value);
            }
        }
        this.put(attribute, color);
    }
    
    private byte[] parseRGBA(final String value, final int numElements) {
        final String[] parts = value.split(",");
        if (parts.length != numElements) {
            throw new IllegalArgumentException("3 values required for rgb()");
        }
        final byte[] rgba = new byte[numElements];
        for (int i = 0; i < numElements; ++i) {
            String part = parts[i].trim();
            int v;
            if (i == 3) {
                final float f = Float.parseFloat(part);
                v = Math.round(f * 255.0f);
            }
            else {
                final boolean percent = part.endsWith("%");
                if (percent) {
                    part = stripTrim(value, 0, 1);
                }
                v = Integer.parseInt(part);
                if (percent) {
                    v = 255 * v / 100;
                }
            }
            rgba[i] = (byte)Math.max(0, Math.min(255, v));
        }
        return rgba;
    }
    
    private void parseList(final StyleAttribute<StringList> attribute, final String value) {
        this.put(attribute, parseList(value, 0));
    }
    
    static StringList parseList(final String value, int idx) {
        idx = TextUtil.skipSpaces(value, idx);
        if (idx >= value.length()) {
            return null;
        }
        final char startChar = value.charAt(idx);
        int end;
        String part;
        if (startChar == '\"' || startChar == '\'') {
            ++idx;
            end = TextUtil.indexOf(value, startChar, idx);
            part = value.substring(idx, end);
            end = TextUtil.skipSpaces(value, ++end);
            if (end < value.length() && value.charAt(end) != ',') {
                throw new IllegalArgumentException("',' expected at " + idx);
            }
        }
        else {
            end = TextUtil.indexOf(value, ',', idx);
            part = TextUtil.trim(value, idx, end);
        }
        return new StringList(part, parseList(value, end + 1));
    }
    
    static OrderedListType createRoman(final boolean lowercase) {
        return new OrderedListType() {
            @Override
            public String format(final int nr) {
                if (nr >= 1 && nr <= 39999) {
                    final String str = TextUtil.toRomanNumberString(nr);
                    return lowercase ? str.toLowerCase() : str;
                }
                return Integer.toString(nr);
            }
        };
    }
    
    static {
        PRE = new HashMap<String, Boolean>();
        BREAKWORD = new HashMap<String, Boolean>();
        OLT = new HashMap<String, OrderedListType>();
        ITALIC = new HashMap<String, Boolean>();
        WEIGHTS = new HashMap<String, Integer>();
        TEXTDECORATION = new HashMap<String, TextDecoration>();
        INHERITHOVER = new HashMap<String, Boolean>();
        CSSStyle.PRE.put("pre", Boolean.TRUE);
        CSSStyle.PRE.put("normal", Boolean.FALSE);
        CSSStyle.BREAKWORD.put("normal", Boolean.FALSE);
        CSSStyle.BREAKWORD.put("break-word", Boolean.TRUE);
        final OrderedListType upper_alpha = new OrderedListType("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        final OrderedListType lower_alpha = new OrderedListType("abcdefghijklmnopqrstuvwxyz");
        CSSStyle.OLT.put("decimal", OrderedListType.DECIMAL);
        CSSStyle.OLT.put("upper-alpha", upper_alpha);
        CSSStyle.OLT.put("lower-alpha", lower_alpha);
        CSSStyle.OLT.put("upper-latin", upper_alpha);
        CSSStyle.OLT.put("lower-latin", lower_alpha);
        CSSStyle.OLT.put("upper-roman", createRoman(false));
        CSSStyle.OLT.put("lower-roman", createRoman(true));
        CSSStyle.OLT.put("lower-greek", new OrderedListType("\u03b1\u03b2\u03b3\u03b4\u03b5\u03b6\u03b7\u03b8\u03b9\u03ba\u03bb\u03bc\u03bd\u03be\u03bf\u03c0\u03c1\u03c3\u03c4\u03c5\u03c6\u03c7\u03c8\u03c9"));
        CSSStyle.OLT.put("upper-norwegian", new OrderedListType("ABCDEFGHIJKLMNOPQRSTUVWXYZ\u00c6\u00d8\u00c5"));
        CSSStyle.OLT.put("lower-norwegian", new OrderedListType("abcdefghijklmnopqrstuvwxyz\u00e6\u00f8\u00e5"));
        CSSStyle.OLT.put("upper-russian-short", new OrderedListType("\u0410\u0411\u0412\u0413\u0414\u0415\u0416\u0417\u0418\u041a\u041b\u041c\u041d\u041e\u041f\u0420\u0421\u0422\u0423\u0424\u0425\u0426\u0427\u0428\u0429\u042d\u042e\u042f"));
        CSSStyle.OLT.put("lower-russian-short", new OrderedListType("\u0430\u0431\u0432\u0433\u0434\u0435\u0436\u0437\u0438\u043a\u043b\u043c\u043d\u043e\u043f\u0440\u0441\u0442\u0443\u0444\u0445\u0446\u0447\u0448\u0449\u044d\u044e\u044f"));
        CSSStyle.ITALIC.put("normal", Boolean.FALSE);
        CSSStyle.ITALIC.put("italic", Boolean.TRUE);
        CSSStyle.ITALIC.put("oblique", Boolean.TRUE);
        CSSStyle.WEIGHTS.put("normal", 400);
        CSSStyle.WEIGHTS.put("bold", 700);
        CSSStyle.TEXTDECORATION.put("none", TextDecoration.NONE);
        CSSStyle.TEXTDECORATION.put("underline", TextDecoration.UNDERLINE);
        CSSStyle.TEXTDECORATION.put("line-through", TextDecoration.LINE_THROUGH);
        CSSStyle.INHERITHOVER.put("inherit", Boolean.TRUE);
        CSSStyle.INHERITHOVER.put("normal", Boolean.FALSE);
    }
}
