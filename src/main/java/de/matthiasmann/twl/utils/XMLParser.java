package de.matthiasmann.twl.utils;

import org.xmlpull.mxp1.*;
import java.util.logging.*;
import java.net.*;
import java.io.*;
import java.util.*;
import org.xmlpull.v1.*;

public class XMLParser implements Closeable
{
    private static final Class<?>[] XPP_CLASS;
    private static boolean hasXMP1;
    private final XmlPullParser xpp;
    private final String source;
    private final InputStream inputStream;
    private final BitSet unusedAttributes;
    private String loggerName;

    public static XmlPullParser createParser() throws XmlPullParserException {
        if (XMLParser.hasXMP1) {
            try {
                final XmlPullParser xpp = (XmlPullParser)new MXParserCachingStrings();
                xpp.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", false);
                return xpp;
            }
            catch (Throwable ex) {
                XMLParser.hasXMP1 = false;
                Logger.getLogger(XMLParser.class.getName()).log(Level.WARNING, "Failed direct instantation", ex);
            }
        }
        return XPPF.newPullParser();
    }

    public XMLParser(final XmlPullParser xpp, final String source) {
        this.unusedAttributes = new BitSet();
        this.loggerName = XMLParser.class.getName();
        if (xpp == null) {
            throw new NullPointerException("xpp");
        }
        this.xpp = xpp;
        this.source = source;
        this.inputStream = null;
    }

    public XMLParser(final URL url) throws XmlPullParserException, IOException {
        this.unusedAttributes = new BitSet();
        this.loggerName = XMLParser.class.getName();
        if (url == null) {
            throw new NullPointerException("url");
        }
        XmlPullParser xpp_ = null;
        InputStream is = null;
        this.source = url.toString();
        try {
            xpp_ = (XmlPullParser)url.getContent(XMLParser.XPP_CLASS);
        }
        catch (IOException ex) {}
        if (xpp_ == null) {
            xpp_ = createParser();
            is = url.openStream();
            if (is == null) {
                throw new FileNotFoundException(this.source);
            }
            xpp_.setInput(is, "UTF8");
        }
        this.xpp = xpp_;
        this.inputStream = is;
    }

    @Override
    public void close() throws IOException {
        if (this.inputStream != null) {
            this.inputStream.close();
        }
    }

    public void setLoggerName(final String loggerName) {
        this.loggerName = loggerName;
    }

    public int next() throws XmlPullParserException, IOException {
        this.warnUnusedAttributes();
        final int type = this.xpp.next();
        this.handleType(type);
        return type;
    }

    public int nextTag() throws XmlPullParserException, IOException {
        this.warnUnusedAttributes();
        final int type = this.xpp.nextTag();
        this.handleType(type);
        return type;
    }

    public String nextText() throws XmlPullParserException, IOException {
        this.warnUnusedAttributes();
        return this.xpp.nextText();
    }

    public char[] nextText(final int[] startAndLength) throws XmlPullParserException, IOException {
        this.warnUnusedAttributes();
        while (true) {
            final int token = this.xpp.nextToken();
            switch (token) {
                case 4: {
                    return this.xpp.getTextCharacters(startAndLength);
                }
                case 6: {
                    final String replaced = this.xpp.getText();
                    startAndLength[0] = 0;
                    startAndLength[1] = replaced.length();
                    return replaced.toCharArray();
                }
                case 9: {
                    continue;
                }
                default: {
                    this.handleType(token);
                    return null;
                }
            }
        }
    }

    public void skipText() throws XmlPullParserException, IOException {
        for (int token = this.xpp.getEventType(); token == 4 || token == 6 || token == 9; token = this.xpp.nextToken()) {}
    }

    public boolean isStartTag() throws XmlPullParserException {
        return this.xpp.getEventType() == 2;
    }

    public boolean isEndTag() throws XmlPullParserException {
        return this.xpp.getEventType() == 3;
    }

    public String getPositionDescription() {
        final String desc = this.xpp.getPositionDescription();
        if (this.source != null) {
            return desc + " in " + this.source;
        }
        return desc;
    }

    public int getLineNumber() {
        return this.xpp.getLineNumber();
    }

    public int getColumnNumber() {
        return this.xpp.getColumnNumber();
    }

    public String getName() {
        return this.xpp.getName();
    }

    public void require(final int type, final String namespace, final String name) throws XmlPullParserException, IOException {
        this.xpp.require(type, namespace, name);
    }

    public String getAttributeValue(final int index) {
        this.unusedAttributes.clear(index);
        return this.xpp.getAttributeValue(index);
    }

    public String getAttributeNamespace(final int index) {
        return this.xpp.getAttributeNamespace(index);
    }

    public String getAttributeName(final int index) {
        return this.xpp.getAttributeName(index);
    }

    public int getAttributeCount() {
        return this.xpp.getAttributeCount();
    }

    public String getAttributeValue(final String namespace, final String name) {
        for (int i = 0, n = this.xpp.getAttributeCount(); i < n; ++i) {
            if ((namespace == null || namespace.equals(this.xpp.getAttributeNamespace(i))) && name.equals(this.xpp.getAttributeName(i))) {
                return this.getAttributeValue(i);
            }
        }
        return null;
    }

    public String getAttributeNotNull(final String attribute) throws XmlPullParserException {
        final String value = this.getAttributeValue(null, attribute);
        if (value == null) {
            this.missingAttribute(attribute);
        }
        return value;
    }

    public boolean parseBoolFromAttribute(final String attribName) throws XmlPullParserException {
        return this.parseBool(this.getAttributeNotNull(attribName));
    }

    public boolean parseBoolFromText() throws XmlPullParserException, IOException {
        return this.parseBool(this.nextText());
    }

    public boolean parseBoolFromAttribute(final String attribName, final boolean defaultValue) throws XmlPullParserException {
        final String value = this.getAttributeValue(null, attribName);
        if (value == null) {
            return defaultValue;
        }
        return this.parseBool(value);
    }

    public int parseIntFromAttribute(final String attribName) throws XmlPullParserException {
        return this.parseInt(this.getAttributeNotNull(attribName));
    }

    public int parseIntFromAttribute(final String attribName, final int defaultValue) throws XmlPullParserException {
        final String value = this.getAttributeValue(null, attribName);
        if (value == null) {
            return defaultValue;
        }
        return this.parseInt(value);
    }

    public float parseFloatFromAttribute(final String attribName) throws XmlPullParserException {
        return this.parseFloat(this.getAttributeNotNull(attribName));
    }

    public float parseFloatFromAttribute(final String attribName, final float defaultValue) throws XmlPullParserException {
        final String value = this.getAttributeValue(null, attribName);
        if (value == null) {
            return defaultValue;
        }
        return this.parseFloat(value);
    }

    public <E extends Enum<E>> E parseEnumFromAttribute(final String attribName, final Class<E> enumClazz) throws XmlPullParserException {
        return this.parseEnum(enumClazz, this.getAttributeNotNull(attribName));
    }

    public <E extends Enum<E>> E parseEnumFromAttribute(final String attribName, final Class<E> enumClazz, final E defaultValue) throws XmlPullParserException {
        final String value = this.getAttributeValue(null, attribName);
        if (value == null) {
            return defaultValue;
        }
        return this.parseEnum(enumClazz, value);
    }

    public <E extends Enum<E>> E parseEnumFromText(final Class<E> enumClazz) throws XmlPullParserException, IOException {
        return this.parseEnum(enumClazz, this.nextText());
    }

    public Map<String, String> getUnusedAttributes() {
        if (this.unusedAttributes.isEmpty()) {
            return Collections.emptyMap();
        }
        final LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();
        int i = -1;
        while ((i = this.unusedAttributes.nextSetBit(i + 1)) >= 0) {
            result.put(this.xpp.getAttributeName(i), this.xpp.getAttributeValue(i));
        }
        this.unusedAttributes.clear();
        return result;
    }

    public void ignoreOtherAttributes() {
        this.unusedAttributes.clear();
    }

    public boolean isAttributeUnused(final int idx) {
        return this.unusedAttributes.get(idx);
    }

    public XmlPullParserException error(final String msg) {
        return new XmlPullParserException(msg, this.xpp, (Throwable)null);
    }

    public XmlPullParserException error(final String msg, final Throwable cause) {
        return (XmlPullParserException)new XmlPullParserException(msg, this.xpp, cause).initCause(cause);
    }

    public XmlPullParserException unexpected() {
        return new XmlPullParserException("Unexpected '" + this.xpp.getName() + "'", this.xpp, (Throwable)null);
    }

    protected <E extends Enum<E>> E parseEnum(final Class<E> enumClazz, final String value) throws XmlPullParserException {
        try {
            return Enum.valueOf(enumClazz, value.toUpperCase(Locale.ENGLISH));
        }
        catch (IllegalArgumentException unused) {
            try {
                return Enum.valueOf(enumClazz, value);
            }
            catch (IllegalArgumentException unused2) {
                throw new XmlPullParserException("Unknown enum value \"" + value + "\" for enum class " + enumClazz, this.xpp, (Throwable)null);
            }
        }
    }

    public boolean parseBool(final String value) throws XmlPullParserException {
        if ("true".equals(value)) {
            return true;
        }
        if ("false".equals(value)) {
            return false;
        }
        throw new XmlPullParserException("boolean value must be 'true' or 'false'", this.xpp, (Throwable)null);
    }

    protected int parseInt(final String value) throws XmlPullParserException {
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException ex) {
            throw (XmlPullParserException)new XmlPullParserException("Unable to parse integer", this.xpp, (Throwable)ex).initCause((Throwable)ex);
        }
    }

    protected float parseFloat(final String value) throws XmlPullParserException {
        try {
            return Float.parseFloat(value);
        }
        catch (NumberFormatException ex) {
            throw (XmlPullParserException)new XmlPullParserException("Unable to parse float", this.xpp, (Throwable)ex).initCause((Throwable)ex);
        }
    }

    protected void missingAttribute(final String attribute) throws XmlPullParserException {
        throw new XmlPullParserException("missing '" + attribute + "' on '" + this.xpp.getName() + "'", this.xpp, (Throwable)null);
    }

    protected void handleType(final int type) {
        this.unusedAttributes.clear();
        if (type == 2) {
            this.unusedAttributes.set(0, this.xpp.getAttributeCount());
        }
    }

    protected void warnUnusedAttributes() {
        if (!this.unusedAttributes.isEmpty()) {
            final String positionDescription = this.getPositionDescription();
            int i = -1;
            while ((i = this.unusedAttributes.nextSetBit(i + 1)) >= 0) {
                this.getLogger().log(Level.WARNING, "Unused attribute ''{0}'' on ''{1}'' at {2}", new Object[] { this.xpp.getAttributeName(i), this.xpp.getName(), positionDescription });
            }
        }
    }

    protected Logger getLogger() {
        return Logger.getLogger(this.loggerName);
    }

    static {
        XPP_CLASS = new Class[] { XmlPullParser.class };
        XMLParser.hasXMP1 = true;
    }

    static class XPPF
    {
        private static final XmlPullParserFactory xppf;
        private static XmlPullParserException xppfex;

        static XmlPullParser newPullParser() throws XmlPullParserException {
            if (XPPF.xppf != null) {
                return XPPF.xppf.newPullParser();
            }
            throw XPPF.xppfex;
        }

        private XPPF() {
        }

        static {
            XmlPullParserFactory f = null;
            try {
                f = XmlPullParserFactory.newInstance();
                f.setNamespaceAware(false);
                f.setValidating(false);
            }
            catch (XmlPullParserException ex) {
                Logger.getLogger(XMLParser.class.getName()).log(Level.SEVERE, "Unable to construct XmlPullParserFactory", (Throwable)ex);
                XPPF.xppfex = ex;
            }
            xppf = f;
        }
    }
}
