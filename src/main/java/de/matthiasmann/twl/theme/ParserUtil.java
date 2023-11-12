package de.matthiasmann.twl.theme;

import org.xmlpull.v1.*;
import de.matthiasmann.twl.*;
import java.util.*;
import de.matthiasmann.twl.utils.*;
import java.text.*;

final class ParserUtil
{
    private ParserUtil() {
    }
    
    static void checkNameNotEmpty(final String name, final XMLParser xmlp) throws XmlPullParserException {
        if (name == null) {
            throw xmlp.error("missing 'name' on '" + xmlp.getName() + "'");
        }
        if (name.length() == 0) {
            throw xmlp.error("empty name not allowed");
        }
        if ("none".equals(name)) {
            throw xmlp.error("can't use reserved name \"none\"");
        }
        if (name.indexOf(42) >= 0) {
            throw xmlp.error("'*' is not allowed in names");
        }
        if (name.indexOf(47) >= 0) {
            throw xmlp.error("'/' is not allowed in names");
        }
    }
    
    static Border parseBorderFromAttribute(final XMLParser xmlp, final String attribute) throws XmlPullParserException {
        final String value = xmlp.getAttributeValue(null, attribute);
        if (value == null) {
            return null;
        }
        return parseBorder(xmlp, value);
    }
    
    static Border parseBorder(final XMLParser xmlp, final String value) throws XmlPullParserException {
        try {
            final int[] values = TextUtil.parseIntArray(value);
            switch (values.length) {
                case 1: {
                    return new Border(values[0]);
                }
                case 2: {
                    return new Border(values[0], values[1]);
                }
                case 4: {
                    return new Border(values[0], values[1], values[2], values[3]);
                }
                default: {
                    throw xmlp.error("Unsupported border format");
                }
            }
        }
        catch (NumberFormatException ex) {
            throw xmlp.error("Unable to parse border size", ex);
        }
    }
    
    static Color parseColorFromAttribute(final XMLParser xmlp, final String attribute, final ParameterMapImpl constants, final Color defaultColor) throws XmlPullParserException {
        final String value = xmlp.getAttributeValue(null, attribute);
        if (value == null) {
            return defaultColor;
        }
        return parseColor(xmlp, value, constants);
    }
    
    static Color parseColor(final XMLParser xmlp, final String value, final ParameterMapImpl constants) throws XmlPullParserException {
        try {
            Color color = Color.parserColor(value);
            if (color == null && constants != null) {
                color = (Color)constants.getParameterValue(value, false, (Class)Color.class);
            }
            if (color == null) {
                throw xmlp.error("Unknown color name: " + value);
            }
            return color;
        }
        catch (NumberFormatException ex) {
            throw xmlp.error("unable to parse color code", ex);
        }
    }
    
    static String appendDot(String name) {
        final int len = name.length();
        if (len > 0 && name.charAt(len - 1) != '.') {
            name = name.concat(".");
        }
        return name;
    }
    
    static int[] parseIntArrayFromAttribute(final XMLParser xmlp, final String attribute) throws XmlPullParserException {
        try {
            final String value = xmlp.getAttributeNotNull(attribute);
            return TextUtil.parseIntArray(value);
        }
        catch (NumberFormatException ex) {
            throw xmlp.error("Unable to parse", ex);
        }
    }
    
    static <V> SortedMap<String, V> find(final SortedMap<String, V> map, final String baseName) {
        return map.subMap(baseName, baseName.concat("\uffff"));
    }
    
    static <V> Map<String, V> resolve(final SortedMap<String, V> map, String ref, String name, final V mapToNull) {
        name = appendDot(name);
        final int refLen = ref.length() - 1;
        ref = ref.substring(0, refLen);
        final SortedMap<String, V> matched = find(map, ref);
        if (matched.isEmpty()) {
            return matched;
        }
        final HashMap<String, V> result = new HashMap<String, V>();
        for (final Map.Entry<String, V> texEntry : matched.entrySet()) {
            final String entryName = texEntry.getKey();
            assert entryName.startsWith(ref);
            V value = texEntry.getValue();
            if (value == mapToNull) {
                value = null;
            }
            result.put(name.concat(entryName.substring(refLen)), value);
        }
        return result;
    }
    
    static StateExpression parseCondition(final XMLParser xmlp) throws XmlPullParserException {
        String expression = xmlp.getAttributeValue(null, "if");
        final boolean negate = expression == null;
        if (expression == null) {
            expression = xmlp.getAttributeValue(null, "unless");
        }
        if (expression != null) {
            try {
                return StateExpression.parse(expression, negate);
            }
            catch (ParseException ex) {
                throw xmlp.error("Unable to parse condition", ex);
            }
        }
        return null;
    }
}
