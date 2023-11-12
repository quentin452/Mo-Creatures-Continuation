package org.xmlpull.v1.wrapper;

import java.io.*;
import org.xmlpull.v1.*;

public interface XmlPullParserWrapper extends XmlPullParser
{
    public static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String XSD_NS = "http://www.w3.org/2001/XMLSchema";
    
    String getAttributeValue(final String p0);
    
    String getPITarget() throws IllegalStateException;
    
    String getPIData() throws IllegalStateException;
    
    String getRequiredAttributeValue(final String p0) throws IOException, XmlPullParserException;
    
    String getRequiredAttributeValue(final String p0, final String p1) throws IOException, XmlPullParserException;
    
    String getRequiredElementText(final String p0, final String p1) throws IOException, XmlPullParserException;
    
    boolean isNil() throws IOException, XmlPullParserException;
    
    boolean matches(final int p0, final String p1, final String p2) throws XmlPullParserException;
    
    void nextStartTag() throws XmlPullParserException, IOException;
    
    void nextStartTag(final String p0) throws XmlPullParserException, IOException;
    
    void nextStartTag(final String p0, final String p1) throws XmlPullParserException, IOException;
    
    void nextEndTag() throws XmlPullParserException, IOException;
    
    void nextEndTag(final String p0) throws XmlPullParserException, IOException;
    
    void nextEndTag(final String p0, final String p1) throws XmlPullParserException, IOException;
    
    String nextText(final String p0, final String p1) throws IOException, XmlPullParserException;
    
    void skipSubTree() throws XmlPullParserException, IOException;
}
