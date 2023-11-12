package org.xmlpull.v1.wrapper;

import java.io.*;
import org.xmlpull.v1.*;

public interface XmlSerializerWrapper extends XmlSerializer
{
    public static final String NO_NAMESPACE = "";
    public static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";
    
    String getCurrentNamespaceForElements();
    
    String setCurrentNamespaceForElements(final String p0);
    
    XmlSerializerWrapper attribute(final String p0, final String p1) throws IOException, IllegalArgumentException, IllegalStateException;
    
    XmlSerializerWrapper startTag(final String p0) throws IOException, IllegalArgumentException, IllegalStateException;
    
    XmlSerializerWrapper endTag(final String p0) throws IOException, IllegalArgumentException, IllegalStateException;
    
    XmlSerializerWrapper element(final String p0, final String p1, final String p2) throws IOException, XmlPullParserException;
    
    XmlSerializerWrapper element(final String p0, final String p1) throws IOException, XmlPullParserException;
    
    void fragment(final String p0) throws IOException, IllegalArgumentException, IllegalStateException, XmlPullParserException;
    
    void event(final XmlPullParser p0) throws IOException, IllegalArgumentException, IllegalStateException, XmlPullParserException;
    
    String escapeText(final String p0) throws IllegalArgumentException;
    
    String escapeAttributeValue(final String p0) throws IllegalArgumentException;
}
