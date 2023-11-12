package org.xmlpull.v1.util;

import java.io.*;
import org.xmlpull.v1.*;

public class XmlPullUtil
{
    public static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";
    
    private XmlPullUtil() {
    }
    
    public static String getAttributeValue(final XmlPullParser pp, final String name) {
        return pp.getAttributeValue("", name);
    }
    
    public static String getPITarget(final XmlPullParser pp) throws IllegalStateException {
        int eventType;
        try {
            eventType = pp.getEventType();
        }
        catch (XmlPullParserException ex) {
            throw new IllegalStateException("could not determine parser state: " + ex + pp.getPositionDescription());
        }
        if (eventType != 8) {
            throw new IllegalStateException("parser must be on processing instruction and not " + XmlPullParser.TYPES[eventType] + pp.getPositionDescription());
        }
        final String PI = pp.getText();
        for (int i = 0; i < PI.length(); ++i) {
            if (isS(PI.charAt(i))) {
                return PI.substring(0, i);
            }
        }
        return PI;
    }
    
    public static String getPIData(final XmlPullParser pp) throws IllegalStateException {
        int eventType;
        try {
            eventType = pp.getEventType();
        }
        catch (XmlPullParserException ex) {
            throw new IllegalStateException("could not determine parser state: " + ex + pp.getPositionDescription());
        }
        if (eventType != 8) {
            throw new IllegalStateException("parser must be on processing instruction and not " + XmlPullParser.TYPES[eventType] + pp.getPositionDescription());
        }
        final String PI = pp.getText();
        int pos = -1;
        for (int i = 0; i < PI.length(); ++i) {
            if (isS(PI.charAt(i))) {
                pos = i;
            }
            else if (pos > 0) {
                return PI.substring(i);
            }
        }
        return "";
    }
    
    private static boolean isS(final char ch) {
        return ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t';
    }
    
    public static void skipSubTree(final XmlPullParser pp) throws XmlPullParserException, IOException {
        pp.require(2, null, null);
        int level = 1;
        while (level > 0) {
            final int eventType = pp.next();
            if (eventType == 3) {
                --level;
            }
            else {
                if (eventType != 2) {
                    continue;
                }
                ++level;
            }
        }
    }
    
    public static void nextStartTag(final XmlPullParser pp) throws XmlPullParserException, IOException {
        if (pp.nextTag() != 2) {
            throw new XmlPullParserException("expected START_TAG and not " + pp.getPositionDescription());
        }
    }
    
    public static void nextStartTag(final XmlPullParser pp, final String name) throws XmlPullParserException, IOException {
        pp.nextTag();
        pp.require(2, null, name);
    }
    
    public static void nextStartTag(final XmlPullParser pp, final String namespace, final String name) throws XmlPullParserException, IOException {
        pp.nextTag();
        pp.require(2, namespace, name);
    }
    
    public static void nextEndTag(final XmlPullParser pp, final String namespace, final String name) throws XmlPullParserException, IOException {
        pp.nextTag();
        pp.require(3, namespace, name);
    }
    
    public static String nextText(final XmlPullParser pp, final String namespace, final String name) throws IOException, XmlPullParserException {
        if (name == null) {
            throw new XmlPullParserException("name for element can not be null");
        }
        pp.require(2, namespace, name);
        return pp.nextText();
    }
    
    public static String getRequiredAttributeValue(final XmlPullParser pp, final String namespace, final String name) throws IOException, XmlPullParserException {
        final String value = pp.getAttributeValue(namespace, name);
        if (value == null) {
            throw new XmlPullParserException("required attribute " + name + " is not present");
        }
        return value;
    }
    
    public static void nextEndTag(final XmlPullParser pp) throws XmlPullParserException, IOException {
        if (pp.nextTag() != 3) {
            throw new XmlPullParserException("expected END_TAG and not" + pp.getPositionDescription());
        }
    }
    
    public static boolean matches(final XmlPullParser pp, final int type, final String namespace, final String name) throws XmlPullParserException {
        final boolean matches = type == pp.getEventType() && (namespace == null || namespace.equals(pp.getNamespace())) && (name == null || name.equals(pp.getName()));
        return matches;
    }
    
    public static void writeSimpleElement(final XmlSerializer serializer, final String namespace, final String elementName, final String elementText) throws IOException, XmlPullParserException {
        if (elementName == null) {
            throw new XmlPullParserException("name for element can not be null");
        }
        serializer.startTag(namespace, elementName);
        if (elementText == null) {
            serializer.attribute("http://www.w3.org/2001/XMLSchema-instance", "nil", "true");
        }
        else {
            serializer.text(elementText);
        }
        serializer.endTag(namespace, elementName);
    }
}
