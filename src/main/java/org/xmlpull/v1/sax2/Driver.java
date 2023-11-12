//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Administrator\Downloads\Minecraft-Deobfuscator3000-1.2.3\Minecraft-Deobfuscator3000-1.2.3\1.7.10 stable mappings"!

//Decompiled by Procyon!

package org.xmlpull.v1.sax2;

import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.*;
import org.xmlpull.v1.*;
import java.net.*;
import org.xml.sax.*;
import java.io.*;

public class Driver implements Locator, XMLReader, Attributes
{
    protected static final String DECLARATION_HANDLER_PROPERTY = "http://xml.org/sax/properties/declaration-handler";
    protected static final String LEXICAL_HANDLER_PROPERTY = "http://xml.org/sax/properties/lexical-handler";
    protected static final String NAMESPACES_FEATURE = "http://xml.org/sax/features/namespaces";
    protected static final String NAMESPACE_PREFIXES_FEATURE = "http://xml.org/sax/features/namespace-prefixes";
    protected static final String VALIDATION_FEATURE = "http://xml.org/sax/features/validation";
    protected static final String APACHE_SCHEMA_VALIDATION_FEATURE = "http://apache.org/xml/features/validation/schema";
    protected static final String APACHE_DYNAMIC_VALIDATION_FEATURE = "http://apache.org/xml/features/validation/dynamic";
    protected ContentHandler contentHandler;
    protected ErrorHandler errorHandler;
    protected String systemId;
    protected XmlPullParser pp;

    public Driver() throws XmlPullParserException {
        this.contentHandler = new DefaultHandler();
        this.errorHandler = new DefaultHandler();
        final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        this.pp = factory.newPullParser();
    }

    public Driver(final XmlPullParser pp) throws XmlPullParserException {
        this.contentHandler = new DefaultHandler();
        this.errorHandler = new DefaultHandler();
        this.pp = pp;
    }

    public int getLength() {
        return this.pp.getAttributeCount();
    }

    public String getURI(final int index) {
        return this.pp.getAttributeNamespace(index);
    }

    public String getLocalName(final int index) {
        return this.pp.getAttributeName(index);
    }

    public String getQName(final int index) {
        final String prefix = this.pp.getAttributePrefix(index);
        if (prefix != null) {
            return prefix + ':' + this.pp.getAttributeName(index);
        }
        return this.pp.getAttributeName(index);
    }

    public String getType(final int index) {
        return this.pp.getAttributeType(index);
    }

    public String getValue(final int index) {
        return this.pp.getAttributeValue(index);
    }

    public int getIndex(final String uri, final String localName) {
        for (int i = 0; i < this.pp.getAttributeCount(); ++i) {
            if (this.pp.getAttributeNamespace(i).equals(uri) && this.pp.getAttributeName(i).equals(localName)) {
                return i;
            }
        }
        return -1;
    }

    public int getIndex(final String qName) {
        for (int i = 0; i < this.pp.getAttributeCount(); ++i) {
            if (this.pp.getAttributeName(i).equals(qName)) {
                return i;
            }
        }
        return -1;
    }

    public String getType(final String uri, final String localName) {
        for (int i = 0; i < this.pp.getAttributeCount(); ++i) {
            if (this.pp.getAttributeNamespace(i).equals(uri) && this.pp.getAttributeName(i).equals(localName)) {
                return this.pp.getAttributeType(i);
            }
        }
        return null;
    }

    public String getType(final String qName) {
        for (int i = 0; i < this.pp.getAttributeCount(); ++i) {
            if (this.pp.getAttributeName(i).equals(qName)) {
                return this.pp.getAttributeType(i);
            }
        }
        return null;
    }

    public String getValue(final String uri, final String localName) {
        return this.pp.getAttributeValue(uri, localName);
    }

    public String getValue(final String qName) {
        return this.pp.getAttributeValue(null, qName);
    }

    public String getPublicId() {
        return null;
    }

    public String getSystemId() {
        return this.systemId;
    }

    public int getLineNumber() {
        return this.pp.getLineNumber();
    }

    public int getColumnNumber() {
        return this.pp.getColumnNumber();
    }

    public boolean getFeature(final String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if ("http://xml.org/sax/features/namespaces".equals(name)) {
            return this.pp.getFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces");
        }
        if ("http://xml.org/sax/features/namespace-prefixes".equals(name)) {
            return this.pp.getFeature("http://xmlpull.org/v1/doc/features.html#report-namespace-prefixes");
        }
        if ("http://xml.org/sax/features/validation".equals(name)) {
            return this.pp.getFeature("http://xmlpull.org/v1/doc/features.html#validation");
        }
        return this.pp.getFeature(name);
    }

    public void setFeature(final String name, final boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        try {
            if ("http://xml.org/sax/features/namespaces".equals(name)) {
                this.pp.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", value);
            }
            else if ("http://xml.org/sax/features/namespace-prefixes".equals(name)) {
                if (this.pp.getFeature("http://xmlpull.org/v1/doc/features.html#report-namespace-prefixes") != value) {
                    this.pp.setFeature("http://xmlpull.org/v1/doc/features.html#report-namespace-prefixes", value);
                }
            }
            else if ("http://xml.org/sax/features/validation".equals(name)) {
                this.pp.setFeature("http://xmlpull.org/v1/doc/features.html#validation", value);
            }
            else {
                this.pp.setFeature(name, value);
            }
        }
        catch (XmlPullParserException ex) {
            throw new SAXNotSupportedException("problem with setting feature " + name + ": " + ex);
        }
    }

    public Object getProperty(final String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        if ("http://xml.org/sax/properties/declaration-handler".equals(name)) {
            return null;
        }
        if ("http://xml.org/sax/properties/lexical-handler".equals(name)) {
            return null;
        }
        return this.pp.getProperty(name);
    }

    public void setProperty(final String name, final Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if ("http://xml.org/sax/properties/declaration-handler".equals(name)) {
            throw new SAXNotSupportedException("not supported setting property " + name);
        }
        if ("http://xml.org/sax/properties/lexical-handler".equals(name)) {
            throw new SAXNotSupportedException("not supported setting property " + name);
        }
        try {
            this.pp.setProperty(name, value);
        }
        catch (XmlPullParserException ex) {
            throw new SAXNotSupportedException("not supported set property " + name + ": " + ex);
        }
    }

    public void setEntityResolver(final EntityResolver resolver) {
    }

    public EntityResolver getEntityResolver() {
        return null;
    }

    public void setDTDHandler(final DTDHandler handler) {
    }

    public DTDHandler getDTDHandler() {
        return null;
    }

    public void setContentHandler(final ContentHandler handler) {
        this.contentHandler = handler;
    }

    public ContentHandler getContentHandler() {
        return this.contentHandler;
    }

    public void setErrorHandler(final ErrorHandler handler) {
        this.errorHandler = handler;
    }

    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    public void parse(final InputSource source) throws SAXException, IOException {
        this.systemId = source.getSystemId();
        this.contentHandler.setDocumentLocator(this);
        final Reader reader = source.getCharacterStream();
        try {
            if (reader == null) {
                InputStream stream = source.getByteStream();
                final String encoding = source.getEncoding();
                if (stream == null) {
                    this.systemId = source.getSystemId();
                    if (this.systemId == null) {
                        final SAXParseException saxException = new SAXParseException("null source systemId", this);
                        this.errorHandler.fatalError(saxException);
                        return;
                    }
                    try {
                        final URL url = new URL(this.systemId);
                        stream = url.openStream();
                    }
                    catch (MalformedURLException nue) {
                        try {
                            stream = new FileInputStream(this.systemId);
                        }
                        catch (FileNotFoundException fnfe) {
                            final SAXParseException saxException2 = new SAXParseException("could not open file with systemId " + this.systemId, this, fnfe);
                            this.errorHandler.fatalError(saxException2);
                            return;
                        }
                    }
                }
                this.pp.setInput(stream, encoding);
            }
            else {
                this.pp.setInput(reader);
            }
        }
        catch (XmlPullParserException ex) {
            final SAXParseException saxException3 = new SAXParseException("parsing initialization error: " + ex, this, ex);
            this.errorHandler.fatalError(saxException3);
            return;
        }
        try {
            this.contentHandler.startDocument();
            this.pp.next();
            if (this.pp.getEventType() != 2) {
                final SAXParseException saxException4 = new SAXParseException("expected start tag not" + this.pp.getPositionDescription(), this);
                this.errorHandler.fatalError(saxException4);
                return;
            }
        }
        catch (XmlPullParserException ex) {
            final SAXParseException saxException3 = new SAXParseException("parsing initialization error: " + ex, this, ex);
            this.errorHandler.fatalError(saxException3);
            return;
        }
        this.parseSubTree(this.pp);
        this.contentHandler.endDocument();
    }

    public void parse(final String systemId) throws SAXException, IOException {
        this.parse(new InputSource(systemId));
    }

    public void parseSubTree(final XmlPullParser pp) throws SAXException, IOException {
        this.pp = pp;
        final boolean namespaceAware = pp.getFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces");
        try {
            if (pp.getEventType() != 2) {
                throw new SAXException("start tag must be read before skiping subtree" + pp.getPositionDescription());
            }
            final int[] holderForStartAndLength = new int[2];
            final StringBuffer rawName = new StringBuffer(16);
            String prefix = null;
            String name = null;
            final int level = pp.getDepth() - 1;
            int type = 2;
        Label_0575:
            do {
                switch (type) {
                    case 2: {
                        if (namespaceAware) {
                            final int depth = pp.getDepth() - 1;
                            final int countPrev = (level > depth) ? pp.getNamespaceCount(depth) : 0;
                            for (int count = pp.getNamespaceCount(depth + 1), i = countPrev; i < count; ++i) {
                                this.contentHandler.startPrefixMapping(pp.getNamespacePrefix(i), pp.getNamespaceUri(i));
                            }
                            name = pp.getName();
                            prefix = pp.getPrefix();
                            if (prefix != null) {
                                rawName.setLength(0);
                                rawName.append(prefix);
                                rawName.append(':');
                                rawName.append(name);
                            }
                            this.startElement(pp.getNamespace(), name, (prefix != null) ? rawName.toString() : name);
                            break;
                        }
                        this.startElement(pp.getNamespace(), pp.getName(), pp.getName());
                        break;
                    }
                    case 4: {
                        final char[] chars = pp.getTextCharacters(holderForStartAndLength);
                        this.contentHandler.characters(chars, holderForStartAndLength[0], holderForStartAndLength[1]);
                        break;
                    }
                    case 3: {
                        if (namespaceAware) {
                            name = pp.getName();
                            prefix = pp.getPrefix();
                            if (prefix != null) {
                                rawName.setLength(0);
                                rawName.append(prefix);
                                rawName.append(':');
                                rawName.append(name);
                            }
                            this.contentHandler.endElement(pp.getNamespace(), name, (prefix != null) ? rawName.toString() : name);
                            final int depth2 = pp.getDepth();
                            final int countPrev2 = (level > depth2) ? pp.getNamespaceCount(pp.getDepth()) : 0;
                            final int count2 = pp.getNamespaceCount(pp.getDepth() - 1);
                            for (int j = count2 - 1; j >= countPrev2; --j) {
                                this.contentHandler.endPrefixMapping(pp.getNamespacePrefix(j));
                            }
                            break;
                        }
                        this.contentHandler.endElement(pp.getNamespace(), pp.getName(), pp.getName());
                        break;
                    }
                    case 1: {
                        break Label_0575;
                    }
                }
                type = pp.next();
            } while (pp.getDepth() > level);
        }
        catch (XmlPullParserException ex) {
            final SAXParseException saxException = new SAXParseException("parsing error: " + ex, this, ex);
            ex.printStackTrace();
            this.errorHandler.fatalError(saxException);
        }
    }

    protected void startElement(final String namespace, final String localName, final String qName) throws SAXException {
        this.contentHandler.startElement(namespace, localName, qName, this);
    }
}
