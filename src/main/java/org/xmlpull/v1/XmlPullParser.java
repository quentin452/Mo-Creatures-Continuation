//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Administrator\Downloads\Minecraft-Deobfuscator3000-1.2.3\Minecraft-Deobfuscator3000-1.2.3\1.7.10 stable mappings"!

//Decompiled by Procyon!

package org.xmlpull.v1;

import java.io.*;

public interface XmlPullParser
{
    public static final String NO_NAMESPACE = "";
    public static final int START_DOCUMENT = 0;
    public static final int END_DOCUMENT = 1;
    public static final int START_TAG = 2;
    public static final int END_TAG = 3;
    public static final int TEXT = 4;
    public static final int CDSECT = 5;
    public static final int ENTITY_REF = 6;
    public static final int IGNORABLE_WHITESPACE = 7;
    public static final int PROCESSING_INSTRUCTION = 8;
    public static final int COMMENT = 9;
    public static final int DOCDECL = 10;
    public static final String[] TYPES = { "START_DOCUMENT", "END_DOCUMENT", "START_TAG", "END_TAG", "TEXT", "CDSECT", "ENTITY_REF", "IGNORABLE_WHITESPACE", "PROCESSING_INSTRUCTION", "COMMENT", "DOCDECL" };
    public static final String FEATURE_PROCESS_NAMESPACES = "http://xmlpull.org/v1/doc/features.html#process-namespaces";
    public static final String FEATURE_REPORT_NAMESPACE_ATTRIBUTES = "http://xmlpull.org/v1/doc/features.html#report-namespace-prefixes";
    public static final String FEATURE_PROCESS_DOCDECL = "http://xmlpull.org/v1/doc/features.html#process-docdecl";
    public static final String FEATURE_VALIDATION = "http://xmlpull.org/v1/doc/features.html#validation";
    
    void setFeature(final String p0, final boolean p1) throws XmlPullParserException;
    
    boolean getFeature(final String p0);
    
    void setProperty(final String p0, final Object p1) throws XmlPullParserException;
    
    Object getProperty(final String p0);
    
    void setInput(final Reader p0) throws XmlPullParserException;
    
    void setInput(final InputStream p0, final String p1) throws XmlPullParserException;
    
    String getInputEncoding();
    
    void defineEntityReplacementText(final String p0, final String p1) throws XmlPullParserException;
    
    int getNamespaceCount(final int p0) throws XmlPullParserException;
    
    String getNamespacePrefix(final int p0) throws XmlPullParserException;
    
    String getNamespaceUri(final int p0) throws XmlPullParserException;
    
    String getNamespace(final String p0);
    
    int getDepth();
    
    String getPositionDescription();
    
    int getLineNumber();
    
    int getColumnNumber();
    
    boolean isWhitespace() throws XmlPullParserException;
    
    String getText();
    
    char[] getTextCharacters(final int[] p0);
    
    String getNamespace();
    
    String getName();
    
    String getPrefix();
    
    boolean isEmptyElementTag() throws XmlPullParserException;
    
    int getAttributeCount();
    
    String getAttributeNamespace(final int p0);
    
    String getAttributeName(final int p0);
    
    String getAttributePrefix(final int p0);
    
    String getAttributeType(final int p0);
    
    boolean isAttributeDefault(final int p0);
    
    String getAttributeValue(final int p0);
    
    String getAttributeValue(final String p0, final String p1);
    
    int getEventType() throws XmlPullParserException;
    
    int next() throws XmlPullParserException, IOException;
    
    int nextToken() throws XmlPullParserException, IOException;
    
    void require(final int p0, final String p1, final String p2) throws XmlPullParserException, IOException;
    
    String nextText() throws XmlPullParserException, IOException;
    
    int nextTag() throws XmlPullParserException, IOException;
}
