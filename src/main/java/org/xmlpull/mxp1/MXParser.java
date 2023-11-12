//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Administrator\Downloads\Minecraft-Deobfuscator3000-1.2.3\Minecraft-Deobfuscator3000-1.2.3\1.7.10 stable mappings"!

//Decompiled by Procyon!

package org.xmlpull.mxp1;

import org.xmlpull.v1.*;
import java.io.*;

public class MXParser implements XmlPullParser
{
    protected static final String XML_URI = "http://www.w3.org/XML/1998/namespace";
    protected static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";
    protected static final String FEATURE_XML_ROUNDTRIP = "http://xmlpull.org/v1/doc/features.html#xml-roundtrip";
    protected static final String FEATURE_NAMES_INTERNED = "http://xmlpull.org/v1/doc/features.html#names-interned";
    protected static final String PROPERTY_XMLDECL_VERSION = "http://xmlpull.org/v1/doc/properties.html#xmldecl-version";
    protected static final String PROPERTY_XMLDECL_STANDALONE = "http://xmlpull.org/v1/doc/properties.html#xmldecl-standalone";
    protected static final String PROPERTY_XMLDECL_CONTENT = "http://xmlpull.org/v1/doc/properties.html#xmldecl-content";
    protected static final String PROPERTY_LOCATION = "http://xmlpull.org/v1/doc/properties.html#location";
    protected boolean allStringsInterned;
    private static final boolean TRACE_SIZING = false;
    protected boolean processNamespaces;
    protected boolean roundtripSupported;
    protected String location;
    protected int lineNumber;
    protected int columnNumber;
    protected boolean seenRoot;
    protected boolean reachedEnd;
    protected int eventType;
    protected boolean emptyElementTag;
    protected int depth;
    protected char[][] elRawName;
    protected int[] elRawNameEnd;
    protected int[] elRawNameLine;
    protected String[] elName;
    protected String[] elPrefix;
    protected String[] elUri;
    protected int[] elNamespaceCount;
    protected int attributeCount;
    protected String[] attributeName;
    protected int[] attributeNameHash;
    protected String[] attributePrefix;
    protected String[] attributeUri;
    protected String[] attributeValue;
    protected int namespaceEnd;
    protected String[] namespacePrefix;
    protected int[] namespacePrefixHash;
    protected String[] namespaceUri;
    protected int entityEnd;
    protected String[] entityName;
    protected char[][] entityNameBuf;
    protected String[] entityReplacement;
    protected char[][] entityReplacementBuf;
    protected int[] entityNameHash;
    protected static final int READ_CHUNK_SIZE = 8192;
    protected Reader reader;
    protected String inputEncoding;
    protected InputStream inputStream;
    protected int bufLoadFactor;
    protected char[] buf;
    protected int bufSoftLimit;
    protected boolean preventBufferCompaction;
    protected int bufAbsoluteStart;
    protected int bufStart;
    protected int bufEnd;
    protected int pos;
    protected int posStart;
    protected int posEnd;
    protected char[] pc;
    protected int pcStart;
    protected int pcEnd;
    protected boolean usePC;
    protected boolean seenStartTag;
    protected boolean seenEndTag;
    protected boolean pastEndTag;
    protected boolean seenAmpersand;
    protected boolean seenMarkup;
    protected boolean seenDocdecl;
    protected boolean tokenize;
    protected String text;
    protected String entityRefName;
    protected String xmlDeclVersion;
    protected Boolean xmlDeclStandalone;
    protected String xmlDeclContent;
    protected char[] charRefOneCharBuf;
    protected static final char[] VERSION;
    protected static final char[] NCODING;
    protected static final char[] TANDALONE;
    protected static final char[] YES;
    protected static final char[] NO;
    protected static final int LOOKUP_MAX = 1024;
    protected static final char LOOKUP_MAX_CHAR = '\u0400';
    protected static boolean[] lookupNameStartChar;
    protected static boolean[] lookupNameChar;

    protected void resetStringCache() {
    }

    protected String newString(final char[] cbuf, final int off, final int len) {
        return new String(cbuf, off, len);
    }

    protected String newStringIntern(final char[] cbuf, final int off, final int len) {
        return new String(cbuf, off, len).intern();
    }

    protected void ensureElementsCapacity() {
        final int elStackSize = (this.elName != null) ? this.elName.length : 0;
        if (this.depth + 1 >= elStackSize) {
            final int newSize = ((this.depth >= 7) ? (2 * this.depth) : 8) + 2;
            final boolean needsCopying = elStackSize > 0;
            String[] arr = null;
            arr = new String[newSize];
            if (needsCopying) {
                System.arraycopy(this.elName, 0, arr, 0, elStackSize);
            }
            this.elName = arr;
            arr = new String[newSize];
            if (needsCopying) {
                System.arraycopy(this.elPrefix, 0, arr, 0, elStackSize);
            }
            this.elPrefix = arr;
            arr = new String[newSize];
            if (needsCopying) {
                System.arraycopy(this.elUri, 0, arr, 0, elStackSize);
            }
            this.elUri = arr;
            int[] iarr = new int[newSize];
            if (needsCopying) {
                System.arraycopy(this.elNamespaceCount, 0, iarr, 0, elStackSize);
            }
            else {
                iarr[0] = 0;
            }
            this.elNamespaceCount = iarr;
            iarr = new int[newSize];
            if (needsCopying) {
                System.arraycopy(this.elRawNameEnd, 0, iarr, 0, elStackSize);
            }
            this.elRawNameEnd = iarr;
            iarr = new int[newSize];
            if (needsCopying) {
                System.arraycopy(this.elRawNameLine, 0, iarr, 0, elStackSize);
            }
            this.elRawNameLine = iarr;
            final char[][] carr = new char[newSize][];
            if (needsCopying) {
                System.arraycopy(this.elRawName, 0, carr, 0, elStackSize);
            }
            this.elRawName = carr;
        }
    }

    protected void ensureAttributesCapacity(final int size) {
        final int attrPosSize = (this.attributeName != null) ? this.attributeName.length : 0;
        if (size >= attrPosSize) {
            final int newSize = (size > 7) ? (2 * size) : 8;
            final boolean needsCopying = attrPosSize > 0;
            String[] arr = null;
            arr = new String[newSize];
            if (needsCopying) {
                System.arraycopy(this.attributeName, 0, arr, 0, attrPosSize);
            }
            this.attributeName = arr;
            arr = new String[newSize];
            if (needsCopying) {
                System.arraycopy(this.attributePrefix, 0, arr, 0, attrPosSize);
            }
            this.attributePrefix = arr;
            arr = new String[newSize];
            if (needsCopying) {
                System.arraycopy(this.attributeUri, 0, arr, 0, attrPosSize);
            }
            this.attributeUri = arr;
            arr = new String[newSize];
            if (needsCopying) {
                System.arraycopy(this.attributeValue, 0, arr, 0, attrPosSize);
            }
            this.attributeValue = arr;
            if (!this.allStringsInterned) {
                final int[] iarr = new int[newSize];
                if (needsCopying) {
                    System.arraycopy(this.attributeNameHash, 0, iarr, 0, attrPosSize);
                }
                this.attributeNameHash = iarr;
            }
            arr = null;
        }
    }

    protected void ensureNamespacesCapacity(final int size) {
        final int namespaceSize = (this.namespacePrefix != null) ? this.namespacePrefix.length : 0;
        if (size >= namespaceSize) {
            final int newSize = (size > 7) ? (2 * size) : 8;
            final String[] newNamespacePrefix = new String[newSize];
            final String[] newNamespaceUri = new String[newSize];
            if (this.namespacePrefix != null) {
                System.arraycopy(this.namespacePrefix, 0, newNamespacePrefix, 0, this.namespaceEnd);
                System.arraycopy(this.namespaceUri, 0, newNamespaceUri, 0, this.namespaceEnd);
            }
            this.namespacePrefix = newNamespacePrefix;
            this.namespaceUri = newNamespaceUri;
            if (!this.allStringsInterned) {
                final int[] newNamespacePrefixHash = new int[newSize];
                if (this.namespacePrefixHash != null) {
                    System.arraycopy(this.namespacePrefixHash, 0, newNamespacePrefixHash, 0, this.namespaceEnd);
                }
                this.namespacePrefixHash = newNamespacePrefixHash;
            }
        }
    }

    protected static final int fastHash(final char[] ch, final int off, final int len) {
        if (len == 0) {
            return 0;
        }
        int hash = ch[off];
        hash = (hash << 7) + ch[off + len - 1];
        if (len > 16) {
            hash = (hash << 7) + ch[off + len / 4];
        }
        if (len > 8) {
            hash = (hash << 7) + ch[off + len / 2];
        }
        return hash;
    }

    protected void ensureEntityCapacity() {
        final int entitySize = (this.entityReplacementBuf != null) ? this.entityReplacementBuf.length : 0;
        if (this.entityEnd >= entitySize) {
            final int newSize = (this.entityEnd > 7) ? (2 * this.entityEnd) : 8;
            final String[] newEntityName = new String[newSize];
            final char[][] newEntityNameBuf = new char[newSize][];
            final String[] newEntityReplacement = new String[newSize];
            final char[][] newEntityReplacementBuf = new char[newSize][];
            if (this.entityName != null) {
                System.arraycopy(this.entityName, 0, newEntityName, 0, this.entityEnd);
                System.arraycopy(this.entityNameBuf, 0, newEntityNameBuf, 0, this.entityEnd);
                System.arraycopy(this.entityReplacement, 0, newEntityReplacement, 0, this.entityEnd);
                System.arraycopy(this.entityReplacementBuf, 0, newEntityReplacementBuf, 0, this.entityEnd);
            }
            this.entityName = newEntityName;
            this.entityNameBuf = newEntityNameBuf;
            this.entityReplacement = newEntityReplacement;
            this.entityReplacementBuf = newEntityReplacementBuf;
            if (!this.allStringsInterned) {
                final int[] newEntityNameHash = new int[newSize];
                if (this.entityNameHash != null) {
                    System.arraycopy(this.entityNameHash, 0, newEntityNameHash, 0, this.entityEnd);
                }
                this.entityNameHash = newEntityNameHash;
            }
        }
    }

    protected void reset() {
        this.location = null;
        this.lineNumber = 1;
        this.columnNumber = 0;
        this.seenRoot = false;
        this.reachedEnd = false;
        this.eventType = 0;
        this.emptyElementTag = false;
        this.depth = 0;
        this.attributeCount = 0;
        this.namespaceEnd = 0;
        this.entityEnd = 0;
        this.reader = null;
        this.inputEncoding = null;
        this.preventBufferCompaction = false;
        this.bufAbsoluteStart = 0;
        final int n = 0;
        this.bufStart = n;
        this.bufEnd = n;
        final int pos = 0;
        this.posEnd = pos;
        this.posStart = pos;
        this.pos = pos;
        final int n2 = 0;
        this.pcStart = n2;
        this.pcEnd = n2;
        this.usePC = false;
        this.seenStartTag = false;
        this.seenEndTag = false;
        this.pastEndTag = false;
        this.seenAmpersand = false;
        this.seenMarkup = false;
        this.seenDocdecl = false;
        this.xmlDeclVersion = null;
        this.xmlDeclStandalone = null;
        this.xmlDeclContent = null;
        this.resetStringCache();
    }

    public MXParser() {
        this.bufLoadFactor = 95;
        this.buf = new char[(Runtime.getRuntime().freeMemory() > 1000000L) ? 8192 : 256];
        this.bufSoftLimit = this.bufLoadFactor * this.buf.length / 100;
        this.pc = new char[(Runtime.getRuntime().freeMemory() > 1000000L) ? 8192 : 64];
        this.charRefOneCharBuf = new char[1];
    }

    public void setFeature(final String name, final boolean state) throws XmlPullParserException {
        if (name == null) {
            throw new IllegalArgumentException("feature name should not be null");
        }
        if ("http://xmlpull.org/v1/doc/features.html#process-namespaces".equals(name)) {
            if (this.eventType != 0) {
                throw new XmlPullParserException("namespace processing feature can only be changed before parsing", this, null);
            }
            this.processNamespaces = state;
        }
        else if ("http://xmlpull.org/v1/doc/features.html#names-interned".equals(name)) {
            if (state) {
                throw new XmlPullParserException("interning names in this implementation is not supported");
            }
        }
        else if ("http://xmlpull.org/v1/doc/features.html#process-docdecl".equals(name)) {
            if (state) {
                throw new XmlPullParserException("processing DOCDECL is not supported");
            }
        }
        else {
            if (!"http://xmlpull.org/v1/doc/features.html#xml-roundtrip".equals(name)) {
                throw new XmlPullParserException("unsupported feature " + name);
            }
            this.roundtripSupported = state;
        }
    }

    public boolean getFeature(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("feature name should not be null");
        }
        if ("http://xmlpull.org/v1/doc/features.html#process-namespaces".equals(name)) {
            return this.processNamespaces;
        }
        return !"http://xmlpull.org/v1/doc/features.html#names-interned".equals(name) && !"http://xmlpull.org/v1/doc/features.html#process-docdecl".equals(name) && "http://xmlpull.org/v1/doc/features.html#xml-roundtrip".equals(name) && this.roundtripSupported;
    }

    public void setProperty(final String name, final Object value) throws XmlPullParserException {
        if ("http://xmlpull.org/v1/doc/properties.html#location".equals(name)) {
            this.location = (String)value;
            return;
        }
        throw new XmlPullParserException("unsupported property: '" + name + "'");
    }

    public Object getProperty(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("property name should not be null");
        }
        if ("http://xmlpull.org/v1/doc/properties.html#xmldecl-version".equals(name)) {
            return this.xmlDeclVersion;
        }
        if ("http://xmlpull.org/v1/doc/properties.html#xmldecl-standalone".equals(name)) {
            return this.xmlDeclStandalone;
        }
        if ("http://xmlpull.org/v1/doc/properties.html#xmldecl-content".equals(name)) {
            return this.xmlDeclContent;
        }
        if ("http://xmlpull.org/v1/doc/properties.html#location".equals(name)) {
            return this.location;
        }
        return null;
    }

    public void setInput(final Reader in) throws XmlPullParserException {
        this.reset();
        this.reader = in;
    }

    public void setInput(final InputStream inputStream, final String inputEncoding) throws XmlPullParserException {
        if (inputStream == null) {
            throw new IllegalArgumentException("input stream can not be null");
        }
        this.inputStream = inputStream;
        Reader reader;
        try {
            if (inputEncoding != null) {
                reader = new InputStreamReader(inputStream, inputEncoding);
            }
            else {
                reader = new InputStreamReader(inputStream, "UTF-8");
            }
        }
        catch (UnsupportedEncodingException une) {
            throw new XmlPullParserException("could not create reader for encoding " + inputEncoding + " : " + une, this, une);
        }
        this.setInput(reader);
        this.inputEncoding = inputEncoding;
    }

    public String getInputEncoding() {
        return this.inputEncoding;
    }

    public void defineEntityReplacementText(final String entityName, final String replacementText) throws XmlPullParserException {
        this.ensureEntityCapacity();
        this.entityName[this.entityEnd] = this.newString(entityName.toCharArray(), 0, entityName.length());
        this.entityNameBuf[this.entityEnd] = entityName.toCharArray();
        this.entityReplacement[this.entityEnd] = replacementText;
        this.entityReplacementBuf[this.entityEnd] = replacementText.toCharArray();
        if (!this.allStringsInterned) {
            this.entityNameHash[this.entityEnd] = fastHash(this.entityNameBuf[this.entityEnd], 0, this.entityNameBuf[this.entityEnd].length);
        }
        ++this.entityEnd;
    }

    public int getNamespaceCount(final int depth) throws XmlPullParserException {
        if (!this.processNamespaces || depth == 0) {
            return 0;
        }
        if (depth < 0 || depth > this.depth) {
            throw new IllegalArgumentException("allowed namespace depth 0.." + this.depth + " not " + depth);
        }
        return this.elNamespaceCount[depth];
    }

    public String getNamespacePrefix(final int pos) throws XmlPullParserException {
        if (pos < this.namespaceEnd) {
            return this.namespacePrefix[pos];
        }
        throw new XmlPullParserException("position " + pos + " exceeded number of available namespaces " + this.namespaceEnd);
    }

    public String getNamespaceUri(final int pos) throws XmlPullParserException {
        if (pos < this.namespaceEnd) {
            return this.namespaceUri[pos];
        }
        throw new XmlPullParserException("position " + pos + " exceeded number of available namespaces " + this.namespaceEnd);
    }

    public String getNamespace(final String prefix) {
        if (prefix != null) {
            for (int i = this.namespaceEnd - 1; i >= 0; --i) {
                if (prefix.equals(this.namespacePrefix[i])) {
                    return this.namespaceUri[i];
                }
            }
            if ("xml".equals(prefix)) {
                return "http://www.w3.org/XML/1998/namespace";
            }
            if ("xmlns".equals(prefix)) {
                return "http://www.w3.org/2000/xmlns/";
            }
        }
        else {
            for (int i = this.namespaceEnd - 1; i >= 0; --i) {
                if (this.namespacePrefix[i] == null) {
                    return this.namespaceUri[i];
                }
            }
        }
        return null;
    }

    public int getDepth() {
        return this.depth;
    }

    private static int findFragment(final int bufMinPos, final char[] b, int start, final int end) {
        if (start < bufMinPos) {
            start = bufMinPos;
            if (start > end) {
                start = end;
            }
            return start;
        }
        if (end - start > 65) {
            start = end - 10;
        }
        int i = start + 1;
        while (--i > bufMinPos) {
            if (end - i > 65) {
                break;
            }
            final char c = b[i];
            if (c == '<' && start - i > 10) {
                break;
            }
        }
        return i;
    }

    public String getPositionDescription() {
        String fragment = null;
        if (this.posStart <= this.pos) {
            final int start = findFragment(0, this.buf, this.posStart, this.pos);
            if (start < this.pos) {
                fragment = new String(this.buf, start, this.pos - start);
            }
            if (this.bufAbsoluteStart > 0 || start > 0) {
                fragment = "..." + fragment;
            }
        }
        return " " + XmlPullParser.TYPES[this.eventType] + ((fragment != null) ? (" seen " + this.printable(fragment) + "...") : "") + " " + ((this.location != null) ? this.location : "") + "@" + this.getLineNumber() + ":" + this.getColumnNumber();
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public int getColumnNumber() {
        return this.columnNumber;
    }

    public boolean isWhitespace() throws XmlPullParserException {
        if (this.eventType == 4 || this.eventType == 5) {
            if (this.usePC) {
                for (int i = this.pcStart; i < this.pcEnd; ++i) {
                    if (!this.isS(this.pc[i])) {
                        return false;
                    }
                }
                return true;
            }
            for (int i = this.posStart; i < this.posEnd; ++i) {
                if (!this.isS(this.buf[i])) {
                    return false;
                }
            }
            return true;
        }
        else {
            if (this.eventType == 7) {
                return true;
            }
            throw new XmlPullParserException("no content available to check for white spaces");
        }
    }

    public String getText() {
        if (this.eventType == 0 || this.eventType == 1) {
            return null;
        }
        if (this.eventType == 6) {
            return this.text;
        }
        if (this.text == null) {
            if (!this.usePC || this.eventType == 2 || this.eventType == 3) {
                this.text = new String(this.buf, this.posStart, this.posEnd - this.posStart);
            }
            else {
                this.text = new String(this.pc, this.pcStart, this.pcEnd - this.pcStart);
            }
        }
        return this.text;
    }

    public char[] getTextCharacters(final int[] holderForStartAndLength) {
        if (this.eventType == 4) {
            if (this.usePC) {
                holderForStartAndLength[0] = this.pcStart;
                holderForStartAndLength[1] = this.pcEnd - this.pcStart;
                return this.pc;
            }
            holderForStartAndLength[0] = this.posStart;
            holderForStartAndLength[1] = this.posEnd - this.posStart;
            return this.buf;
        }
        else {
            if (this.eventType == 2 || this.eventType == 3 || this.eventType == 5 || this.eventType == 9 || this.eventType == 6 || this.eventType == 8 || this.eventType == 7 || this.eventType == 10) {
                holderForStartAndLength[0] = this.posStart;
                holderForStartAndLength[1] = this.posEnd - this.posStart;
                return this.buf;
            }
            if (this.eventType == 0 || this.eventType == 1) {
                holderForStartAndLength[0] = (holderForStartAndLength[1] = -1);
                return null;
            }
            throw new IllegalArgumentException("unknown text eventType: " + this.eventType);
        }
    }

    public String getNamespace() {
        if (this.eventType == 2) {
            return this.processNamespaces ? this.elUri[this.depth] : "";
        }
        if (this.eventType == 3) {
            return this.processNamespaces ? this.elUri[this.depth] : "";
        }
        return null;
    }

    public String getName() {
        if (this.eventType == 2) {
            return this.elName[this.depth];
        }
        if (this.eventType == 3) {
            return this.elName[this.depth];
        }
        if (this.eventType == 6) {
            if (this.entityRefName == null) {
                this.entityRefName = this.newString(this.buf, this.posStart, this.posEnd - this.posStart);
            }
            return this.entityRefName;
        }
        return null;
    }

    public String getPrefix() {
        if (this.eventType == 2) {
            return this.elPrefix[this.depth];
        }
        if (this.eventType == 3) {
            return this.elPrefix[this.depth];
        }
        return null;
    }

    public boolean isEmptyElementTag() throws XmlPullParserException {
        if (this.eventType != 2) {
            throw new XmlPullParserException("parser must be on START_TAG to check for empty element", this, null);
        }
        return this.emptyElementTag;
    }

    public int getAttributeCount() {
        if (this.eventType != 2) {
            return -1;
        }
        return this.attributeCount;
    }

    public String getAttributeNamespace(final int index) {
        if (this.eventType != 2) {
            throw new IndexOutOfBoundsException("only START_TAG can have attributes");
        }
        if (!this.processNamespaces) {
            return "";
        }
        if (index < 0 || index >= this.attributeCount) {
            throw new IndexOutOfBoundsException("attribute position must be 0.." + (this.attributeCount - 1) + " and not " + index);
        }
        return this.attributeUri[index];
    }

    public String getAttributeName(final int index) {
        if (this.eventType != 2) {
            throw new IndexOutOfBoundsException("only START_TAG can have attributes");
        }
        if (index < 0 || index >= this.attributeCount) {
            throw new IndexOutOfBoundsException("attribute position must be 0.." + (this.attributeCount - 1) + " and not " + index);
        }
        return this.attributeName[index];
    }

    public String getAttributePrefix(final int index) {
        if (this.eventType != 2) {
            throw new IndexOutOfBoundsException("only START_TAG can have attributes");
        }
        if (!this.processNamespaces) {
            return null;
        }
        if (index < 0 || index >= this.attributeCount) {
            throw new IndexOutOfBoundsException("attribute position must be 0.." + (this.attributeCount - 1) + " and not " + index);
        }
        return this.attributePrefix[index];
    }

    public String getAttributeType(final int index) {
        if (this.eventType != 2) {
            throw new IndexOutOfBoundsException("only START_TAG can have attributes");
        }
        if (index < 0 || index >= this.attributeCount) {
            throw new IndexOutOfBoundsException("attribute position must be 0.." + (this.attributeCount - 1) + " and not " + index);
        }
        return "CDATA";
    }

    public boolean isAttributeDefault(final int index) {
        if (this.eventType != 2) {
            throw new IndexOutOfBoundsException("only START_TAG can have attributes");
        }
        if (index < 0 || index >= this.attributeCount) {
            throw new IndexOutOfBoundsException("attribute position must be 0.." + (this.attributeCount - 1) + " and not " + index);
        }
        return false;
    }

    public String getAttributeValue(final int index) {
        if (this.eventType != 2) {
            throw new IndexOutOfBoundsException("only START_TAG can have attributes");
        }
        if (index < 0 || index >= this.attributeCount) {
            throw new IndexOutOfBoundsException("attribute position must be 0.." + (this.attributeCount - 1) + " and not " + index);
        }
        return this.attributeValue[index];
    }

    public String getAttributeValue(String namespace, final String name) {
        if (this.eventType != 2) {
            throw new IndexOutOfBoundsException("only START_TAG can have attributes" + this.getPositionDescription());
        }
        if (name == null) {
            throw new IllegalArgumentException("attribute name can not be null");
        }
        if (this.processNamespaces) {
            if (namespace == null) {
                namespace = "";
            }
            for (int i = 0; i < this.attributeCount; ++i) {
                if ((namespace == this.attributeUri[i] || namespace.equals(this.attributeUri[i])) && name.equals(this.attributeName[i])) {
                    return this.attributeValue[i];
                }
            }
        }
        else {
            if (namespace != null && namespace.length() == 0) {
                namespace = null;
            }
            if (namespace != null) {
                throw new IllegalArgumentException("when namespaces processing is disabled attribute namespace must be null");
            }
            for (int i = 0; i < this.attributeCount; ++i) {
                if (name.equals(this.attributeName[i])) {
                    return this.attributeValue[i];
                }
            }
        }
        return null;
    }

    public int getEventType() throws XmlPullParserException {
        return this.eventType;
    }

    public void require(final int type, final String namespace, final String name) throws XmlPullParserException, IOException {
        if (!this.processNamespaces && namespace != null) {
            throw new XmlPullParserException("processing namespaces must be enabled on parser (or factory) to have possible namespaces declared on elements" + " (position:" + this.getPositionDescription() + ")");
        }
        if (type != this.getEventType() || (namespace != null && !namespace.equals(this.getNamespace())) || (name != null && !name.equals(this.getName()))) {
            throw new XmlPullParserException("expected event " + XmlPullParser.TYPES[type] + ((name != null) ? (" with name '" + name + "'") : "") + ((namespace != null && name != null) ? " and" : "") + ((namespace != null) ? (" with namespace '" + namespace + "'") : "") + " but got" + ((type != this.getEventType()) ? (" " + XmlPullParser.TYPES[this.getEventType()]) : "") + ((name != null && this.getName() != null && !name.equals(this.getName())) ? (" name '" + this.getName() + "'") : "") + ((namespace != null && name != null && this.getName() != null && !name.equals(this.getName()) && this.getNamespace() != null && !namespace.equals(this.getNamespace())) ? " and" : "") + ((namespace != null && this.getNamespace() != null && !namespace.equals(this.getNamespace())) ? (" namespace '" + this.getNamespace() + "'") : "") + " (position:" + this.getPositionDescription() + ")");
        }
    }

    public void skipSubTree() throws XmlPullParserException, IOException {
        this.require(2, null, null);
        int level = 1;
        while (level > 0) {
            final int eventType = this.next();
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

    public String nextText() throws XmlPullParserException, IOException {
        if (this.getEventType() != 2) {
            throw new XmlPullParserException("parser must be on START_TAG to read next text", this, null);
        }
        int eventType = this.next();
        if (eventType == 4) {
            final String result = this.getText();
            eventType = this.next();
            if (eventType != 3) {
                throw new XmlPullParserException("TEXT must be immediately followed by END_TAG and not " + XmlPullParser.TYPES[this.getEventType()], this, null);
            }
            return result;
        }
        else {
            if (eventType == 3) {
                return "";
            }
            throw new XmlPullParserException("parser must be on START_TAG or TEXT to read text", this, null);
        }
    }

    public int nextTag() throws XmlPullParserException, IOException {
        this.next();
        if (this.eventType == 4 && this.isWhitespace()) {
            this.next();
        }
        if (this.eventType != 2 && this.eventType != 3) {
            throw new XmlPullParserException("expected START_TAG or END_TAG not " + XmlPullParser.TYPES[this.getEventType()], this, null);
        }
        return this.eventType;
    }

    public int next() throws XmlPullParserException, IOException {
        this.tokenize = false;
        return this.nextImpl();
    }

    public int nextToken() throws XmlPullParserException, IOException {
        this.tokenize = true;
        return this.nextImpl();
    }

    protected int nextImpl() throws XmlPullParserException, IOException {
        this.text = null;
        final int n = 0;
        this.pcStart = n;
        this.pcEnd = n;
        this.usePC = false;
        this.bufStart = this.posEnd;
        if (this.pastEndTag) {
            this.pastEndTag = false;
            --this.depth;
            this.namespaceEnd = this.elNamespaceCount[this.depth];
        }
        if (this.emptyElementTag) {
            this.emptyElementTag = false;
            this.pastEndTag = true;
            return this.eventType = 3;
        }
        if (this.depth > 0) {
            if (this.seenStartTag) {
                this.seenStartTag = false;
                return this.eventType = this.parseStartTag();
            }
            if (this.seenEndTag) {
                this.seenEndTag = false;
                return this.eventType = this.parseEndTag();
            }
            char ch;
            if (this.seenMarkup) {
                this.seenMarkup = false;
                ch = '<';
            }
            else if (this.seenAmpersand) {
                this.seenAmpersand = false;
                ch = '&';
            }
            else {
                ch = this.more();
            }
            this.posStart = this.pos - 1;
            boolean hadCharData = false;
            boolean needsMerging = false;
            while (true) {
                if (ch == '<') {
                    if (hadCharData && this.tokenize) {
                        this.seenMarkup = true;
                        return this.eventType = 4;
                    }
                    ch = this.more();
                    if (ch == '/') {
                        if (!this.tokenize && hadCharData) {
                            this.seenEndTag = true;
                            return this.eventType = 4;
                        }
                        return this.eventType = this.parseEndTag();
                    }
                    else if (ch == '!') {
                        ch = this.more();
                        if (ch == '-') {
                            this.parseComment();
                            if (this.tokenize) {
                                return this.eventType = 9;
                            }
                            if (!this.usePC && hadCharData) {
                                needsMerging = true;
                            }
                            else {
                                this.posStart = this.pos;
                            }
                        }
                        else {
                            if (ch != '[') {
                                throw new XmlPullParserException("unexpected character in markup " + this.printable(ch), this, null);
                            }
                            this.parseCDSect(hadCharData);
                            if (this.tokenize) {
                                return this.eventType = 5;
                            }
                            final int cdStart = this.posStart;
                            final int cdEnd = this.posEnd;
                            final int cdLen = cdEnd - cdStart;
                            if (cdLen > 0) {
                                hadCharData = true;
                                if (!this.usePC) {
                                    needsMerging = true;
                                }
                            }
                        }
                    }
                    else if (ch == '?') {
                        this.parsePI();
                        if (this.tokenize) {
                            return this.eventType = 8;
                        }
                        if (!this.usePC && hadCharData) {
                            needsMerging = true;
                        }
                        else {
                            this.posStart = this.pos;
                        }
                    }
                    else {
                        if (!this.isNameStartChar(ch)) {
                            throw new XmlPullParserException("unexpected character in markup " + this.printable(ch), this, null);
                        }
                        if (!this.tokenize && hadCharData) {
                            this.seenStartTag = true;
                            return this.eventType = 4;
                        }
                        return this.eventType = this.parseStartTag();
                    }
                }
                else {
                    if (ch != '&') {
                        if (needsMerging) {
                            this.joinPC();
                            needsMerging = false;
                        }
                        hadCharData = true;
                        boolean normalizedCR = false;
                        final boolean normalizeInput = !this.tokenize || !this.roundtripSupported;
                        boolean seenBracket = false;
                        boolean seenBracketBracket = false;
                        do {
                            if (ch == ']') {
                                if (seenBracket) {
                                    seenBracketBracket = true;
                                }
                                else {
                                    seenBracket = true;
                                }
                            }
                            else {
                                if (seenBracketBracket && ch == '>') {
                                    throw new XmlPullParserException("characters ]]> are not allowed in content", this, null);
                                }
                                if (seenBracket) {
                                    seenBracket = (seenBracketBracket = false);
                                }
                            }
                            if (normalizeInput) {
                                if (ch == '\r') {
                                    normalizedCR = true;
                                    this.posEnd = this.pos - 1;
                                    if (!this.usePC) {
                                        if (this.posEnd > this.posStart) {
                                            this.joinPC();
                                        }
                                        else {
                                            this.usePC = true;
                                            final int n2 = 0;
                                            this.pcEnd = n2;
                                            this.pcStart = n2;
                                        }
                                    }
                                    if (this.pcEnd >= this.pc.length) {
                                        this.ensurePC(this.pcEnd);
                                    }
                                    this.pc[this.pcEnd++] = '\n';
                                }
                                else if (ch == '\n') {
                                    if (!normalizedCR && this.usePC) {
                                        if (this.pcEnd >= this.pc.length) {
                                            this.ensurePC(this.pcEnd);
                                        }
                                        this.pc[this.pcEnd++] = '\n';
                                    }
                                    normalizedCR = false;
                                }
                                else {
                                    if (this.usePC) {
                                        if (this.pcEnd >= this.pc.length) {
                                            this.ensurePC(this.pcEnd);
                                        }
                                        this.pc[this.pcEnd++] = ch;
                                    }
                                    normalizedCR = false;
                                }
                            }
                            ch = this.more();
                        } while (ch != '<' && ch != '&');
                        this.posEnd = this.pos - 1;
                        continue;
                    }
                    if (this.tokenize && hadCharData) {
                        this.seenAmpersand = true;
                        return this.eventType = 4;
                    }
                    final int oldStart = this.posStart + this.bufAbsoluteStart;
                    final int oldEnd = this.posEnd + this.bufAbsoluteStart;
                    final char[] resolvedEntity = this.parseEntityRef();
                    if (this.tokenize) {
                        return this.eventType = 6;
                    }
                    if (resolvedEntity == null) {
                        if (this.entityRefName == null) {
                            this.entityRefName = this.newString(this.buf, this.posStart, this.posEnd - this.posStart);
                        }
                        throw new XmlPullParserException("could not resolve entity named '" + this.printable(this.entityRefName) + "'", this, null);
                    }
                    this.posStart = oldStart - this.bufAbsoluteStart;
                    this.posEnd = oldEnd - this.bufAbsoluteStart;
                    if (!this.usePC) {
                        if (hadCharData) {
                            this.joinPC();
                            needsMerging = false;
                        }
                        else {
                            this.usePC = true;
                            final int n3 = 0;
                            this.pcEnd = n3;
                            this.pcStart = n3;
                        }
                    }
                    for (int i = 0; i < resolvedEntity.length; ++i) {
                        if (this.pcEnd >= this.pc.length) {
                            this.ensurePC(this.pcEnd);
                        }
                        this.pc[this.pcEnd++] = resolvedEntity[i];
                    }
                    hadCharData = true;
                }
                ch = this.more();
            }
        }
        else {
            if (this.seenRoot) {
                return this.parseEpilog();
            }
            return this.parseProlog();
        }
    }

    protected int parseProlog() throws XmlPullParserException, IOException {
        char ch;
        if (this.seenMarkup) {
            ch = this.buf[this.pos - 1];
        }
        else {
            ch = this.more();
        }
        if (this.eventType == 0) {
            if (ch == '\ufffe') {
                throw new XmlPullParserException("first character in input was UNICODE noncharacter (0xFFFE)- input requires int swapping", this, null);
            }
            if (ch == '\ufeff') {
                ch = this.more();
            }
        }
        this.seenMarkup = false;
        boolean gotS = false;
        this.posStart = this.pos - 1;
        final boolean normalizeIgnorableWS = this.tokenize && !this.roundtripSupported;
        boolean normalizedCR = false;
        while (true) {
            if (ch == '<') {
                if (gotS && this.tokenize) {
                    this.posEnd = this.pos - 1;
                    this.seenMarkup = true;
                    return this.eventType = 7;
                }
                ch = this.more();
                if (ch == '?') {
                    if (this.parsePI()) {
                        if (this.tokenize) {
                            return this.eventType = 8;
                        }
                    }
                    else {
                        this.posStart = this.pos;
                        gotS = false;
                    }
                }
                else if (ch == '!') {
                    ch = this.more();
                    if (ch == 'D') {
                        if (this.seenDocdecl) {
                            throw new XmlPullParserException("only one docdecl allowed in XML document", this, null);
                        }
                        this.seenDocdecl = true;
                        this.parseDocdecl();
                        if (this.tokenize) {
                            return this.eventType = 10;
                        }
                    }
                    else {
                        if (ch != '-') {
                            throw new XmlPullParserException("unexpected markup <!" + this.printable(ch), this, null);
                        }
                        this.parseComment();
                        if (this.tokenize) {
                            return this.eventType = 9;
                        }
                    }
                }
                else {
                    if (ch == '/') {
                        throw new XmlPullParserException("expected start tag name and not " + this.printable(ch), this, null);
                    }
                    if (this.isNameStartChar(ch)) {
                        this.seenRoot = true;
                        return this.parseStartTag();
                    }
                    throw new XmlPullParserException("expected start tag name and not " + this.printable(ch), this, null);
                }
            }
            else {
                if (!this.isS(ch)) {
                    throw new XmlPullParserException("only whitespace content allowed before start tag and not " + this.printable(ch), this, null);
                }
                gotS = true;
                if (normalizeIgnorableWS) {
                    if (ch == '\r') {
                        normalizedCR = true;
                        if (!this.usePC) {
                            this.posEnd = this.pos - 1;
                            if (this.posEnd > this.posStart) {
                                this.joinPC();
                            }
                            else {
                                this.usePC = true;
                                final int n = 0;
                                this.pcEnd = n;
                                this.pcStart = n;
                            }
                        }
                        if (this.pcEnd >= this.pc.length) {
                            this.ensurePC(this.pcEnd);
                        }
                        this.pc[this.pcEnd++] = '\n';
                    }
                    else if (ch == '\n') {
                        if (!normalizedCR && this.usePC) {
                            if (this.pcEnd >= this.pc.length) {
                                this.ensurePC(this.pcEnd);
                            }
                            this.pc[this.pcEnd++] = '\n';
                        }
                        normalizedCR = false;
                    }
                    else {
                        if (this.usePC) {
                            if (this.pcEnd >= this.pc.length) {
                                this.ensurePC(this.pcEnd);
                            }
                            this.pc[this.pcEnd++] = ch;
                        }
                        normalizedCR = false;
                    }
                }
            }
            ch = this.more();
        }
    }

    protected int parseEpilog() throws XmlPullParserException, IOException {
        if (this.eventType == 1) {
            throw new XmlPullParserException("already reached end of XML input", this, null);
        }
        if (this.reachedEnd) {
            return this.eventType = 1;
        }
        boolean gotS = false;
        final boolean normalizeIgnorableWS = this.tokenize && !this.roundtripSupported;
        boolean normalizedCR = false;
        try {
            char ch;
            if (this.seenMarkup) {
                ch = this.buf[this.pos - 1];
            }
            else {
                ch = this.more();
            }
            this.seenMarkup = false;
            this.posStart = this.pos - 1;
            if (!this.reachedEnd) {
                do {
                    if (ch == '<') {
                        if (gotS && this.tokenize) {
                            this.posEnd = this.pos - 1;
                            this.seenMarkup = true;
                            return this.eventType = 7;
                        }
                        ch = this.more();
                        if (this.reachedEnd) {
                            break;
                        }
                        if (ch == '?') {
                            this.parsePI();
                            if (this.tokenize) {
                                return this.eventType = 8;
                            }
                        }
                        else if (ch == '!') {
                            ch = this.more();
                            if (this.reachedEnd) {
                                break;
                            }
                            if (ch == 'D') {
                                this.parseDocdecl();
                                if (this.tokenize) {
                                    return this.eventType = 10;
                                }
                            }
                            else {
                                if (ch != '-') {
                                    throw new XmlPullParserException("unexpected markup <!" + this.printable(ch), this, null);
                                }
                                this.parseComment();
                                if (this.tokenize) {
                                    return this.eventType = 9;
                                }
                            }
                        }
                        else {
                            if (ch == '/') {
                                throw new XmlPullParserException("end tag not allowed in epilog but got " + this.printable(ch), this, null);
                            }
                            if (this.isNameStartChar(ch)) {
                                throw new XmlPullParserException("start tag not allowed in epilog but got " + this.printable(ch), this, null);
                            }
                            throw new XmlPullParserException("in epilog expected ignorable content and not " + this.printable(ch), this, null);
                        }
                    }
                    else {
                        if (!this.isS(ch)) {
                            throw new XmlPullParserException("in epilog non whitespace content is not allowed but got " + this.printable(ch), this, null);
                        }
                        gotS = true;
                        if (normalizeIgnorableWS) {
                            if (ch == '\r') {
                                normalizedCR = true;
                                if (!this.usePC) {
                                    this.posEnd = this.pos - 1;
                                    if (this.posEnd > this.posStart) {
                                        this.joinPC();
                                    }
                                    else {
                                        this.usePC = true;
                                        final int n = 0;
                                        this.pcEnd = n;
                                        this.pcStart = n;
                                    }
                                }
                                if (this.pcEnd >= this.pc.length) {
                                    this.ensurePC(this.pcEnd);
                                }
                                this.pc[this.pcEnd++] = '\n';
                            }
                            else if (ch == '\n') {
                                if (!normalizedCR && this.usePC) {
                                    if (this.pcEnd >= this.pc.length) {
                                        this.ensurePC(this.pcEnd);
                                    }
                                    this.pc[this.pcEnd++] = '\n';
                                }
                                normalizedCR = false;
                            }
                            else {
                                if (this.usePC) {
                                    if (this.pcEnd >= this.pc.length) {
                                        this.ensurePC(this.pcEnd);
                                    }
                                    this.pc[this.pcEnd++] = ch;
                                }
                                normalizedCR = false;
                            }
                        }
                    }
                    ch = this.more();
                } while (!this.reachedEnd);
            }
        }
        catch (EOFException ex) {
            this.reachedEnd = true;
        }
        if (!this.reachedEnd) {
            throw new XmlPullParserException("internal error in parseEpilog");
        }
        if (this.tokenize && gotS) {
            this.posEnd = this.pos;
            return this.eventType = 7;
        }
        return this.eventType = 1;
    }

    public int parseEndTag() throws XmlPullParserException, IOException {
        char ch = this.more();
        if (!this.isNameStartChar(ch)) {
            throw new XmlPullParserException("expected name start and not " + this.printable(ch), this, null);
        }
        this.posStart = this.pos - 3;
        final int nameStart = this.pos - 1 + this.bufAbsoluteStart;
        do {
            ch = this.more();
        } while (this.isNameChar(ch));
        int off = nameStart - this.bufAbsoluteStart;
        final int len = this.pos - 1 - off;
        final char[] cbuf = this.elRawName[this.depth];
        if (this.elRawNameEnd[this.depth] != len) {
            final String startname = new String(cbuf, 0, this.elRawNameEnd[this.depth]);
            final String endname = new String(this.buf, off, len);
            throw new XmlPullParserException("end tag name </" + endname + "> must match start tag name <" + startname + ">" + " from line " + this.elRawNameLine[this.depth], this, null);
        }
        for (int i = 0; i < len; ++i) {
            if (this.buf[off++] != cbuf[i]) {
                final String startname2 = new String(cbuf, 0, len);
                final String endname2 = new String(this.buf, off - i - 1, len);
                throw new XmlPullParserException("end tag name </" + endname2 + "> must be the same as start tag <" + startname2 + ">" + " from line " + this.elRawNameLine[this.depth], this, null);
            }
        }
        while (this.isS(ch)) {
            ch = this.more();
        }
        if (ch != '>') {
            throw new XmlPullParserException("expected > to finish end tag not " + this.printable(ch) + " from line " + this.elRawNameLine[this.depth], this, null);
        }
        this.posEnd = this.pos;
        this.pastEndTag = true;
        return this.eventType = 3;
    }

    public int parseStartTag() throws XmlPullParserException, IOException {
        ++this.depth;
        this.posStart = this.pos - 2;
        this.emptyElementTag = false;
        this.attributeCount = 0;
        final int nameStart = this.pos - 1 + this.bufAbsoluteStart;
        int colonPos = -1;
        char ch = this.buf[this.pos - 1];
        if (ch == ':' && this.processNamespaces) {
            throw new XmlPullParserException("when namespaces processing enabled colon can not be at element name start", this, null);
        }
        while (true) {
            ch = this.more();
            if (!this.isNameChar(ch)) {
                this.ensureElementsCapacity();
                final int elLen = this.pos - 1 - (nameStart - this.bufAbsoluteStart);
                if (this.elRawName[this.depth] == null || this.elRawName[this.depth].length < elLen) {
                    this.elRawName[this.depth] = new char[2 * elLen];
                }
                System.arraycopy(this.buf, nameStart - this.bufAbsoluteStart, this.elRawName[this.depth], 0, elLen);
                this.elRawNameEnd[this.depth] = elLen;
                this.elRawNameLine[this.depth] = this.lineNumber;
                String name = null;
                String prefix = null;
                if (this.processNamespaces) {
                    if (colonPos != -1) {
                        final String[] elPrefix = this.elPrefix;
                        final int depth = this.depth;
                        final String string = this.newString(this.buf, nameStart - this.bufAbsoluteStart, colonPos - nameStart);
                        elPrefix[depth] = string;
                        prefix = string;
                        final String[] elName = this.elName;
                        final int depth2 = this.depth;
                        final String string2 = this.newString(this.buf, colonPos + 1 - this.bufAbsoluteStart, this.pos - 2 - (colonPos - this.bufAbsoluteStart));
                        elName[depth2] = string2;
                        name = string2;
                    }
                    else {
                        final String[] elPrefix2 = this.elPrefix;
                        final int depth3 = this.depth;
                        final String s = null;
                        elPrefix2[depth3] = s;
                        prefix = s;
                        final String[] elName2 = this.elName;
                        final int depth4 = this.depth;
                        final String string3 = this.newString(this.buf, nameStart - this.bufAbsoluteStart, elLen);
                        elName2[depth4] = string3;
                        name = string3;
                    }
                }
                else {
                    final String[] elName3 = this.elName;
                    final int depth5 = this.depth;
                    final String string4 = this.newString(this.buf, nameStart - this.bufAbsoluteStart, elLen);
                    elName3[depth5] = string4;
                    name = string4;
                }
                while (true) {
                    if (this.isS(ch)) {
                        ch = this.more();
                    }
                    else {
                        if (ch == '>') {
                            break;
                        }
                        if (ch == '/') {
                            if (this.emptyElementTag) {
                                throw new XmlPullParserException("repeated / in tag declaration", this, null);
                            }
                            this.emptyElementTag = true;
                            ch = this.more();
                            if (ch != '>') {
                                throw new XmlPullParserException("expected > to end empty tag not " + this.printable(ch), this, null);
                            }
                            break;
                        }
                        else {
                            if (!this.isNameStartChar(ch)) {
                                throw new XmlPullParserException("start tag unexpected character " + this.printable(ch), this, null);
                            }
                            ch = this.parseAttribute();
                            ch = this.more();
                        }
                    }
                }
                if (this.processNamespaces) {
                    String uri = this.getNamespace(prefix);
                    if (uri == null) {
                        if (prefix != null) {
                            throw new XmlPullParserException("could not determine namespace bound to element prefix " + prefix, this, null);
                        }
                        uri = "";
                    }
                    this.elUri[this.depth] = uri;
                    for (int i = 0; i < this.attributeCount; ++i) {
                        final String attrPrefix = this.attributePrefix[i];
                        if (attrPrefix != null) {
                            final String attrUri = this.getNamespace(attrPrefix);
                            if (attrUri == null) {
                                throw new XmlPullParserException("could not determine namespace bound to attribute prefix " + attrPrefix, this, null);
                            }
                            this.attributeUri[i] = attrUri;
                        }
                        else {
                            this.attributeUri[i] = "";
                        }
                    }
                    for (int i = 1; i < this.attributeCount; ++i) {
                        for (int j = 0; j < i; ++j) {
                            if (this.attributeUri[j] == this.attributeUri[i] && ((this.allStringsInterned && this.attributeName[j].equals(this.attributeName[i])) || (!this.allStringsInterned && this.attributeNameHash[j] == this.attributeNameHash[i] && this.attributeName[j].equals(this.attributeName[i])))) {
                                String attr1 = this.attributeName[j];
                                if (this.attributeUri[j] != null) {
                                    attr1 = this.attributeUri[j] + ":" + attr1;
                                }
                                String attr2 = this.attributeName[i];
                                if (this.attributeUri[i] != null) {
                                    attr2 = this.attributeUri[i] + ":" + attr2;
                                }
                                throw new XmlPullParserException("duplicated attributes " + attr1 + " and " + attr2, this, null);
                            }
                        }
                    }
                }
                else {
                    for (int k = 1; k < this.attributeCount; ++k) {
                        for (int l = 0; l < k; ++l) {
                            if ((this.allStringsInterned && this.attributeName[l].equals(this.attributeName[k])) || (!this.allStringsInterned && this.attributeNameHash[l] == this.attributeNameHash[k] && this.attributeName[l].equals(this.attributeName[k]))) {
                                final String attr3 = this.attributeName[l];
                                final String attr4 = this.attributeName[k];
                                throw new XmlPullParserException("duplicated attributes " + attr3 + " and " + attr4, this, null);
                            }
                        }
                    }
                }
                this.elNamespaceCount[this.depth] = this.namespaceEnd;
                this.posEnd = this.pos;
                return this.eventType = 2;
            }
            if (ch != ':' || !this.processNamespaces) {
                continue;
            }
            if (colonPos != -1) {
                throw new XmlPullParserException("only one colon is allowed in name of element when namespaces are enabled", this, null);
            }
            colonPos = this.pos - 1 + this.bufAbsoluteStart;
        }
    }

    protected char parseAttribute() throws XmlPullParserException, IOException {
        final int prevPosStart = this.posStart + this.bufAbsoluteStart;
        final int nameStart = this.pos - 1 + this.bufAbsoluteStart;
        int colonPos = -1;
        char ch = this.buf[this.pos - 1];
        if (ch == ':' && this.processNamespaces) {
            throw new XmlPullParserException("when namespaces processing enabled colon can not be at attribute name start", this, null);
        }
        boolean startsWithXmlns = this.processNamespaces && ch == 'x';
        int xmlnsPos = 0;
        for (ch = this.more(); this.isNameChar(ch); ch = this.more()) {
            if (this.processNamespaces) {
                if (startsWithXmlns && xmlnsPos < 5) {
                    if (++xmlnsPos == 1) {
                        if (ch != 'm') {
                            startsWithXmlns = false;
                        }
                    }
                    else if (xmlnsPos == 2) {
                        if (ch != 'l') {
                            startsWithXmlns = false;
                        }
                    }
                    else if (xmlnsPos == 3) {
                        if (ch != 'n') {
                            startsWithXmlns = false;
                        }
                    }
                    else if (xmlnsPos == 4) {
                        if (ch != 's') {
                            startsWithXmlns = false;
                        }
                    }
                    else if (xmlnsPos == 5 && ch != ':') {
                        throw new XmlPullParserException("after xmlns in attribute name must be colonwhen namespaces are enabled", this, null);
                    }
                }
                if (ch == ':') {
                    if (colonPos != -1) {
                        throw new XmlPullParserException("only one colon is allowed in attribute name when namespaces are enabled", this, null);
                    }
                    colonPos = this.pos - 1 + this.bufAbsoluteStart;
                }
            }
        }
        this.ensureAttributesCapacity(this.attributeCount);
        String name = null;
        String prefix = null;
        if (this.processNamespaces) {
            if (xmlnsPos < 4) {
                startsWithXmlns = false;
            }
            if (startsWithXmlns) {
                if (colonPos != -1) {
                    final int nameLen = this.pos - 2 - (colonPos - this.bufAbsoluteStart);
                    if (nameLen == 0) {
                        throw new XmlPullParserException("namespace prefix is required after xmlns:  when namespaces are enabled", this, null);
                    }
                    name = this.newString(this.buf, colonPos - this.bufAbsoluteStart + 1, nameLen);
                }
            }
            else {
                if (colonPos != -1) {
                    final int prefixLen = colonPos - nameStart;
                    final String[] attributePrefix = this.attributePrefix;
                    final int attributeCount = this.attributeCount;
                    final String string = this.newString(this.buf, nameStart - this.bufAbsoluteStart, prefixLen);
                    attributePrefix[attributeCount] = string;
                    prefix = string;
                    final int nameLen2 = this.pos - 2 - (colonPos - this.bufAbsoluteStart);
                    final String[] attributeName = this.attributeName;
                    final int attributeCount2 = this.attributeCount;
                    final String string2 = this.newString(this.buf, colonPos - this.bufAbsoluteStart + 1, nameLen2);
                    attributeName[attributeCount2] = string2;
                    name = string2;
                }
                else {
                    final String[] attributePrefix2 = this.attributePrefix;
                    final int attributeCount3 = this.attributeCount;
                    final String s2 = null;
                    attributePrefix2[attributeCount3] = s2;
                    prefix = s2;
                    final String[] attributeName2 = this.attributeName;
                    final int attributeCount4 = this.attributeCount;
                    final String string3 = this.newString(this.buf, nameStart - this.bufAbsoluteStart, this.pos - 1 - (nameStart - this.bufAbsoluteStart));
                    attributeName2[attributeCount4] = string3;
                    name = string3;
                }
                if (!this.allStringsInterned) {
                    this.attributeNameHash[this.attributeCount] = name.hashCode();
                }
            }
        }
        else {
            final String[] attributeName3 = this.attributeName;
            final int attributeCount5 = this.attributeCount;
            final String string4 = this.newString(this.buf, nameStart - this.bufAbsoluteStart, this.pos - 1 - (nameStart - this.bufAbsoluteStart));
            attributeName3[attributeCount5] = string4;
            name = string4;
            if (!this.allStringsInterned) {
                this.attributeNameHash[this.attributeCount] = name.hashCode();
            }
        }
        while (this.isS(ch)) {
            ch = this.more();
        }
        if (ch != '=') {
            throw new XmlPullParserException("expected = after attribute name", this, null);
        }
        for (ch = this.more(); this.isS(ch); ch = this.more()) {}
        final char delimit = ch;
        if (delimit != '\"' && delimit != '\'') {
            throw new XmlPullParserException("attribute value must start with quotation or apostrophe not " + this.printable(delimit), this, null);
        }
        boolean normalizedCR = false;
        this.usePC = false;
        this.pcStart = this.pcEnd;
        this.posStart = this.pos;
        while (true) {
            ch = this.more();
            if (ch == delimit) {
                if (this.processNamespaces && startsWithXmlns) {
                    String ns = null;
                    if (!this.usePC) {
                        ns = this.newStringIntern(this.buf, this.posStart, this.pos - 1 - this.posStart);
                    }
                    else {
                        ns = this.newStringIntern(this.pc, this.pcStart, this.pcEnd - this.pcStart);
                    }
                    this.ensureNamespacesCapacity(this.namespaceEnd);
                    int prefixHash = -1;
                    if (colonPos != -1) {
                        if (ns.length() == 0) {
                            throw new XmlPullParserException("non-default namespace can not be declared to be empty string", this, null);
                        }
                        this.namespacePrefix[this.namespaceEnd] = name;
                        if (!this.allStringsInterned) {
                            final int[] namespacePrefixHash = this.namespacePrefixHash;
                            final int namespaceEnd = this.namespaceEnd;
                            final int hashCode = name.hashCode();
                            namespacePrefixHash[namespaceEnd] = hashCode;
                            prefixHash = hashCode;
                        }
                    }
                    else {
                        this.namespacePrefix[this.namespaceEnd] = null;
                        if (!this.allStringsInterned) {
                            final int[] namespacePrefixHash2 = this.namespacePrefixHash;
                            final int namespaceEnd2 = this.namespaceEnd;
                            final int n = -1;
                            namespacePrefixHash2[namespaceEnd2] = n;
                            prefixHash = n;
                        }
                    }
                    this.namespaceUri[this.namespaceEnd] = ns;
                    for (int startNs = this.elNamespaceCount[this.depth - 1], i = this.namespaceEnd - 1; i >= startNs; --i) {
                        if (((this.allStringsInterned || name == null) && this.namespacePrefix[i] == name) || (!this.allStringsInterned && name != null && this.namespacePrefixHash[i] == prefixHash && name.equals(this.namespacePrefix[i]))) {
                            final String s = (name == null) ? "default" : ("'" + name + "'");
                            throw new XmlPullParserException("duplicated namespace declaration for " + s + " prefix", this, null);
                        }
                    }
                    ++this.namespaceEnd;
                }
                else {
                    if (!this.usePC) {
                        this.attributeValue[this.attributeCount] = new String(this.buf, this.posStart, this.pos - 1 - this.posStart);
                    }
                    else {
                        this.attributeValue[this.attributeCount] = new String(this.pc, this.pcStart, this.pcEnd - this.pcStart);
                    }
                    ++this.attributeCount;
                }
                this.posStart = prevPosStart - this.bufAbsoluteStart;
                return ch;
            }
            if (ch == '<') {
                throw new XmlPullParserException("markup not allowed inside attribute value - illegal < ", this, null);
            }
            if (ch == '&') {
                this.posEnd = this.pos - 1;
                if (!this.usePC) {
                    final boolean hadCharData = this.posEnd > this.posStart;
                    if (hadCharData) {
                        this.joinPC();
                    }
                    else {
                        this.usePC = true;
                        final int n2 = 0;
                        this.pcEnd = n2;
                        this.pcStart = n2;
                    }
                }
                final char[] resolvedEntity = this.parseEntityRef();
                if (resolvedEntity == null) {
                    if (this.entityRefName == null) {
                        this.entityRefName = this.newString(this.buf, this.posStart, this.posEnd - this.posStart);
                    }
                    throw new XmlPullParserException("could not resolve entity named '" + this.printable(this.entityRefName) + "'", this, null);
                }
                for (int j = 0; j < resolvedEntity.length; ++j) {
                    if (this.pcEnd >= this.pc.length) {
                        this.ensurePC(this.pcEnd);
                    }
                    this.pc[this.pcEnd++] = resolvedEntity[j];
                }
            }
            else if (ch == '\t' || ch == '\n' || ch == '\r') {
                if (!this.usePC) {
                    this.posEnd = this.pos - 1;
                    if (this.posEnd > this.posStart) {
                        this.joinPC();
                    }
                    else {
                        this.usePC = true;
                        final int n3 = 0;
                        this.pcStart = n3;
                        this.pcEnd = n3;
                    }
                }
                if (this.pcEnd >= this.pc.length) {
                    this.ensurePC(this.pcEnd);
                }
                if (ch != '\n' || !normalizedCR) {
                    this.pc[this.pcEnd++] = ' ';
                }
            }
            else if (this.usePC) {
                if (this.pcEnd >= this.pc.length) {
                    this.ensurePC(this.pcEnd);
                }
                this.pc[this.pcEnd++] = ch;
            }
            normalizedCR = (ch == '\r');
        }
    }

    protected char[] parseEntityRef() throws XmlPullParserException, IOException {
        this.entityRefName = null;
        this.posStart = this.pos;
        char ch = this.more();
        if (ch == '#') {
            char charRef = '\0';
            ch = this.more();
            if (ch == 'x') {
                while (true) {
                    ch = this.more();
                    if (ch >= '0' && ch <= '9') {
                        charRef = (char)(charRef * '\u0010' + (ch - '0'));
                    }
                    else if (ch >= 'a' && ch <= 'f') {
                        charRef = (char)(charRef * '\u0010' + (ch - 'W'));
                    }
                    else {
                        if (ch < 'A' || ch > 'F') {
                            break;
                        }
                        charRef = (char)(charRef * '\u0010' + (ch - '7'));
                    }
                }
                if (ch != ';') {
                    throw new XmlPullParserException("character reference (with hex value) may not contain " + this.printable(ch), this, null);
                }
            }
            else {
                while (ch >= '0' && ch <= '9') {
                    charRef = (char)(charRef * '\n' + (ch - '0'));
                    ch = this.more();
                }
                if (ch != ';') {
                    throw new XmlPullParserException("character reference (with decimal value) may not contain " + this.printable(ch), this, null);
                }
            }
            this.posEnd = this.pos - 1;
            this.charRefOneCharBuf[0] = charRef;
            if (this.tokenize) {
                this.text = this.newString(this.charRefOneCharBuf, 0, 1);
            }
            return this.charRefOneCharBuf;
        }
        if (!this.isNameStartChar(ch)) {
            throw new XmlPullParserException("entity reference names can not start with character '" + this.printable(ch) + "'", this, null);
        }
        do {
            ch = this.more();
            if (ch == ';') {
                this.posEnd = this.pos - 1;
                final int len = this.posEnd - this.posStart;
                if (len == 2 && this.buf[this.posStart] == 'l' && this.buf[this.posStart + 1] == 't') {
                    if (this.tokenize) {
                        this.text = "<";
                    }
                    this.charRefOneCharBuf[0] = '<';
                    return this.charRefOneCharBuf;
                }
                if (len == 3 && this.buf[this.posStart] == 'a' && this.buf[this.posStart + 1] == 'm' && this.buf[this.posStart + 2] == 'p') {
                    if (this.tokenize) {
                        this.text = "&";
                    }
                    this.charRefOneCharBuf[0] = '&';
                    return this.charRefOneCharBuf;
                }
                if (len == 2 && this.buf[this.posStart] == 'g' && this.buf[this.posStart + 1] == 't') {
                    if (this.tokenize) {
                        this.text = ">";
                    }
                    this.charRefOneCharBuf[0] = '>';
                    return this.charRefOneCharBuf;
                }
                if (len == 4 && this.buf[this.posStart] == 'a' && this.buf[this.posStart + 1] == 'p' && this.buf[this.posStart + 2] == 'o' && this.buf[this.posStart + 3] == 's') {
                    if (this.tokenize) {
                        this.text = "'";
                    }
                    this.charRefOneCharBuf[0] = '\'';
                    return this.charRefOneCharBuf;
                }
                if (len == 4 && this.buf[this.posStart] == 'q' && this.buf[this.posStart + 1] == 'u' && this.buf[this.posStart + 2] == 'o' && this.buf[this.posStart + 3] == 't') {
                    if (this.tokenize) {
                        this.text = "\"";
                    }
                    this.charRefOneCharBuf[0] = '\"';
                    return this.charRefOneCharBuf;
                }
                final char[] result = this.lookuEntityReplacement(len);
                if (result != null) {
                    return result;
                }
                if (this.tokenize) {
                    this.text = null;
                }
                return null;
            }
        } while (this.isNameChar(ch));
        throw new XmlPullParserException("entity reference name can not contain character " + this.printable(ch) + "'", this, null);
    }

    protected char[] lookuEntityReplacement(final int entitNameLen) throws XmlPullParserException, IOException {
        if (!this.allStringsInterned) {
            final int hash = fastHash(this.buf, this.posStart, this.posEnd - this.posStart);
        Label_0130:
            for (int i = this.entityEnd - 1; i >= 0; --i) {
                if (hash == this.entityNameHash[i] && entitNameLen == this.entityNameBuf[i].length) {
                    final char[] entityBuf = this.entityNameBuf[i];
                    for (int j = 0; j < entitNameLen; ++j) {
                        if (this.buf[this.posStart + j] != entityBuf[j]) {
                            continue Label_0130;
                        }
                    }
                    if (this.tokenize) {
                        this.text = this.entityReplacement[i];
                    }
                    return this.entityReplacementBuf[i];
                }
            }
        }
        else {
            this.entityRefName = this.newString(this.buf, this.posStart, this.posEnd - this.posStart);
            for (int k = this.entityEnd - 1; k >= 0; --k) {
                if (this.entityRefName == this.entityName[k]) {
                    if (this.tokenize) {
                        this.text = this.entityReplacement[k];
                    }
                    return this.entityReplacementBuf[k];
                }
            }
        }
        return null;
    }

    protected void parseComment() throws XmlPullParserException, IOException {
        char ch = this.more();
        if (ch != '-') {
            throw new XmlPullParserException("expected <!-- for comment start", this, null);
        }
        if (this.tokenize) {
            this.posStart = this.pos;
        }
        final int curLine = this.lineNumber;
        final int curColumn = this.columnNumber;
        try {
            final boolean normalizeIgnorableWS = this.tokenize && !this.roundtripSupported;
            boolean normalizedCR = false;
            boolean seenDash = false;
            boolean seenDashDash = false;
            while (true) {
                ch = this.more();
                if (seenDashDash && ch != '>') {
                    throw new XmlPullParserException("in comment after two dashes (--) next character must be > not " + this.printable(ch), this, null);
                }
                if (ch == '-') {
                    if (!seenDash) {
                        seenDash = true;
                    }
                    else {
                        seenDashDash = true;
                        seenDash = false;
                    }
                }
                else if (ch == '>') {
                    if (seenDashDash) {
                        break;
                    }
                    seenDashDash = false;
                    seenDash = false;
                }
                else {
                    seenDash = false;
                }
                if (!normalizeIgnorableWS) {
                    continue;
                }
                if (ch == '\r') {
                    normalizedCR = true;
                    if (!this.usePC) {
                        this.posEnd = this.pos - 1;
                        if (this.posEnd > this.posStart) {
                            this.joinPC();
                        }
                        else {
                            this.usePC = true;
                            final int n = 0;
                            this.pcEnd = n;
                            this.pcStart = n;
                        }
                    }
                    if (this.pcEnd >= this.pc.length) {
                        this.ensurePC(this.pcEnd);
                    }
                    this.pc[this.pcEnd++] = '\n';
                }
                else if (ch == '\n') {
                    if (!normalizedCR && this.usePC) {
                        if (this.pcEnd >= this.pc.length) {
                            this.ensurePC(this.pcEnd);
                        }
                        this.pc[this.pcEnd++] = '\n';
                    }
                    normalizedCR = false;
                }
                else {
                    if (this.usePC) {
                        if (this.pcEnd >= this.pc.length) {
                            this.ensurePC(this.pcEnd);
                        }
                        this.pc[this.pcEnd++] = ch;
                    }
                    normalizedCR = false;
                }
            }
        }
        catch (EOFException ex) {
            throw new XmlPullParserException("comment started on line " + curLine + " and column " + curColumn + " was not closed", this, ex);
        }
        if (this.tokenize) {
            this.posEnd = this.pos - 3;
            if (this.usePC) {
                this.pcEnd -= 2;
            }
        }
    }

    protected boolean parsePI() throws XmlPullParserException, IOException {
        if (this.tokenize) {
            this.posStart = this.pos;
        }
        final int curLine = this.lineNumber;
        final int curColumn = this.columnNumber;
        int piTargetStart = this.pos + this.bufAbsoluteStart;
        int piTargetEnd = -1;
        final boolean normalizeIgnorableWS = this.tokenize && !this.roundtripSupported;
        boolean normalizedCR = false;
        try {
            boolean seenQ = false;
            char ch = this.more();
            if (this.isS(ch)) {
                throw new XmlPullParserException("processing instruction PITarget must be exactly after <? and not white space character", this, null);
            }
            while (true) {
                if (ch == '?') {
                    seenQ = true;
                }
                else if (ch == '>') {
                    if (seenQ) {
                        break;
                    }
                    seenQ = false;
                }
                else {
                    if (piTargetEnd == -1 && this.isS(ch)) {
                        piTargetEnd = this.pos - 1 + this.bufAbsoluteStart;
                        if (piTargetEnd - piTargetStart == 3 && (this.buf[piTargetStart] == 'x' || this.buf[piTargetStart] == 'X') && (this.buf[piTargetStart + 1] == 'm' || this.buf[piTargetStart + 1] == 'M') && (this.buf[piTargetStart + 2] == 'l' || this.buf[piTargetStart + 2] == 'L')) {
                            if (piTargetStart > 3) {
                                throw new XmlPullParserException("processing instruction can not have PITarget with reserveld xml name", this, null);
                            }
                            if (this.buf[piTargetStart] != 'x' && this.buf[piTargetStart + 1] != 'm' && this.buf[piTargetStart + 2] != 'l') {
                                throw new XmlPullParserException("XMLDecl must have xml name in lowercase", this, null);
                            }
                            this.parseXmlDecl(ch);
                            if (this.tokenize) {
                                this.posEnd = this.pos - 2;
                            }
                            final int off = piTargetStart - this.bufAbsoluteStart + 3;
                            final int len = this.pos - 2 - off;
                            this.xmlDeclContent = this.newString(this.buf, off, len);
                            return false;
                        }
                    }
                    seenQ = false;
                }
                if (normalizeIgnorableWS) {
                    if (ch == '\r') {
                        normalizedCR = true;
                        if (!this.usePC) {
                            this.posEnd = this.pos - 1;
                            if (this.posEnd > this.posStart) {
                                this.joinPC();
                            }
                            else {
                                this.usePC = true;
                                final int n = 0;
                                this.pcEnd = n;
                                this.pcStart = n;
                            }
                        }
                        if (this.pcEnd >= this.pc.length) {
                            this.ensurePC(this.pcEnd);
                        }
                        this.pc[this.pcEnd++] = '\n';
                    }
                    else if (ch == '\n') {
                        if (!normalizedCR && this.usePC) {
                            if (this.pcEnd >= this.pc.length) {
                                this.ensurePC(this.pcEnd);
                            }
                            this.pc[this.pcEnd++] = '\n';
                        }
                        normalizedCR = false;
                    }
                    else {
                        if (this.usePC) {
                            if (this.pcEnd >= this.pc.length) {
                                this.ensurePC(this.pcEnd);
                            }
                            this.pc[this.pcEnd++] = ch;
                        }
                        normalizedCR = false;
                    }
                }
                ch = this.more();
            }
        }
        catch (EOFException ex) {
            throw new XmlPullParserException("processing instruction started on line " + curLine + " and column " + curColumn + " was not closed", this, ex);
        }
        if (piTargetEnd == -1) {
            piTargetEnd = this.pos - 2 + this.bufAbsoluteStart;
        }
        piTargetStart -= this.bufAbsoluteStart;
        piTargetEnd -= this.bufAbsoluteStart;
        if (this.tokenize) {
            this.posEnd = this.pos - 2;
            if (normalizeIgnorableWS) {
                --this.pcEnd;
            }
        }
        return true;
    }

    protected void parseXmlDecl(char ch) throws XmlPullParserException, IOException {
        this.preventBufferCompaction = true;
        this.bufStart = 0;
        ch = this.skipS(ch);
        ch = this.requireInput(ch, MXParser.VERSION);
        ch = this.skipS(ch);
        if (ch != '=') {
            throw new XmlPullParserException("expected equals sign (=) after version and not " + this.printable(ch), this, null);
        }
        ch = this.more();
        ch = this.skipS(ch);
        if (ch != '\'' && ch != '\"') {
            throw new XmlPullParserException("expected apostrophe (') or quotation mark (\") after version and not " + this.printable(ch), this, null);
        }
        final char quotChar = ch;
        final int versionStart = this.pos;
        for (ch = this.more(); ch != quotChar; ch = this.more()) {
            if ((ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z') && (ch < '0' || ch > '9') && ch != '_' && ch != '.' && ch != ':' && ch != '-') {
                throw new XmlPullParserException("<?xml version value expected to be in ([a-zA-Z0-9_.:] | '-') not " + this.printable(ch), this, null);
            }
        }
        final int versionEnd = this.pos - 1;
        this.parseXmlDeclWithVersion(versionStart, versionEnd);
        this.preventBufferCompaction = false;
    }

    protected void parseXmlDeclWithVersion(final int versionStart, final int versionEnd) throws XmlPullParserException, IOException {
        final String oldEncoding = this.inputEncoding;
        if (versionEnd - versionStart != 3 || this.buf[versionStart] != '1' || this.buf[versionStart + 1] != '.' || this.buf[versionStart + 2] != '0') {
            throw new XmlPullParserException("only 1.0 is supported as <?xml version not '" + this.printable(new String(this.buf, versionStart, versionEnd - versionStart)) + "'", this, null);
        }
        this.xmlDeclVersion = this.newString(this.buf, versionStart, versionEnd - versionStart);
        char ch = this.more();
        ch = this.skipS(ch);
        if (ch == 'e') {
            ch = this.more();
            ch = this.requireInput(ch, MXParser.NCODING);
            ch = this.skipS(ch);
            if (ch != '=') {
                throw new XmlPullParserException("expected equals sign (=) after encoding and not " + this.printable(ch), this, null);
            }
            ch = this.more();
            ch = this.skipS(ch);
            if (ch != '\'' && ch != '\"') {
                throw new XmlPullParserException("expected apostrophe (') or quotation mark (\") after encoding and not " + this.printable(ch), this, null);
            }
            final char quotChar = ch;
            final int encodingStart = this.pos;
            ch = this.more();
            if ((ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z')) {
                throw new XmlPullParserException("<?xml encoding name expected to start with [A-Za-z] not " + this.printable(ch), this, null);
            }
            for (ch = this.more(); ch != quotChar; ch = this.more()) {
                if ((ch < 'a' || ch > 'z') && (ch < 'A' || ch > 'Z') && (ch < '0' || ch > '9') && ch != '.' && ch != '_' && ch != '-') {
                    throw new XmlPullParserException("<?xml encoding value expected to be in ([A-Za-z0-9._] | '-') not " + this.printable(ch), this, null);
                }
            }
            final int encodingEnd = this.pos - 1;
            this.inputEncoding = this.newString(this.buf, encodingStart, encodingEnd - encodingStart);
            ch = this.more();
        }
        ch = this.skipS(ch);
        if (ch == 's') {
            ch = this.more();
            ch = this.requireInput(ch, MXParser.TANDALONE);
            ch = this.skipS(ch);
            if (ch != '=') {
                throw new XmlPullParserException("expected equals sign (=) after standalone and not " + this.printable(ch), this, null);
            }
            ch = this.more();
            ch = this.skipS(ch);
            if (ch != '\'' && ch != '\"') {
                throw new XmlPullParserException("expected apostrophe (') or quotation mark (\") after encoding and not " + this.printable(ch), this, null);
            }
            final char quotChar = ch;
            final int standaloneStart = this.pos;
            ch = this.more();
            if (ch == 'y') {
                ch = this.requireInput(ch, MXParser.YES);
                this.xmlDeclStandalone = true;
            }
            else {
                if (ch != 'n') {
                    throw new XmlPullParserException("expected 'yes' or 'no' after standalone and not " + this.printable(ch), this, null);
                }
                ch = this.requireInput(ch, MXParser.NO);
                this.xmlDeclStandalone = false;
            }
            if (ch != quotChar) {
                throw new XmlPullParserException("expected " + quotChar + " after standalone value not " + this.printable(ch), this, null);
            }
            ch = this.more();
        }
        ch = this.skipS(ch);
        if (ch != '?') {
            throw new XmlPullParserException("expected ?> as last part of <?xml not " + this.printable(ch), this, null);
        }
        ch = this.more();
        if (ch != '>') {
            throw new XmlPullParserException("expected ?> as last part of <?xml not " + this.printable(ch), this, null);
        }
    }

    protected void parseDocdecl() throws XmlPullParserException, IOException {
        char ch = this.more();
        if (ch != 'O') {
            throw new XmlPullParserException("expected <!DOCTYPE", this, null);
        }
        ch = this.more();
        if (ch != 'C') {
            throw new XmlPullParserException("expected <!DOCTYPE", this, null);
        }
        ch = this.more();
        if (ch != 'T') {
            throw new XmlPullParserException("expected <!DOCTYPE", this, null);
        }
        ch = this.more();
        if (ch != 'Y') {
            throw new XmlPullParserException("expected <!DOCTYPE", this, null);
        }
        ch = this.more();
        if (ch != 'P') {
            throw new XmlPullParserException("expected <!DOCTYPE", this, null);
        }
        ch = this.more();
        if (ch != 'E') {
            throw new XmlPullParserException("expected <!DOCTYPE", this, null);
        }
        this.posStart = this.pos;
        int bracketLevel = 0;
        final boolean normalizeIgnorableWS = this.tokenize && !this.roundtripSupported;
        boolean normalizedCR = false;
        while (true) {
            ch = this.more();
            if (ch == '[') {
                ++bracketLevel;
            }
            if (ch == ']') {
                --bracketLevel;
            }
            if (ch == '>' && bracketLevel == 0) {
                break;
            }
            if (!normalizeIgnorableWS) {
                continue;
            }
            if (ch == '\r') {
                normalizedCR = true;
                if (!this.usePC) {
                    this.posEnd = this.pos - 1;
                    if (this.posEnd > this.posStart) {
                        this.joinPC();
                    }
                    else {
                        this.usePC = true;
                        final int n = 0;
                        this.pcEnd = n;
                        this.pcStart = n;
                    }
                }
                if (this.pcEnd >= this.pc.length) {
                    this.ensurePC(this.pcEnd);
                }
                this.pc[this.pcEnd++] = '\n';
            }
            else if (ch == '\n') {
                if (!normalizedCR && this.usePC) {
                    if (this.pcEnd >= this.pc.length) {
                        this.ensurePC(this.pcEnd);
                    }
                    this.pc[this.pcEnd++] = '\n';
                }
                normalizedCR = false;
            }
            else {
                if (this.usePC) {
                    if (this.pcEnd >= this.pc.length) {
                        this.ensurePC(this.pcEnd);
                    }
                    this.pc[this.pcEnd++] = ch;
                }
                normalizedCR = false;
            }
        }
        this.posEnd = this.pos - 1;
    }

    protected void parseCDSect(final boolean hadCharData) throws XmlPullParserException, IOException {
        char ch = this.more();
        if (ch != 'C') {
            throw new XmlPullParserException("expected <[CDATA[ for comment start", this, null);
        }
        ch = this.more();
        if (ch != 'D') {
            throw new XmlPullParserException("expected <[CDATA[ for comment start", this, null);
        }
        ch = this.more();
        if (ch != 'A') {
            throw new XmlPullParserException("expected <[CDATA[ for comment start", this, null);
        }
        ch = this.more();
        if (ch != 'T') {
            throw new XmlPullParserException("expected <[CDATA[ for comment start", this, null);
        }
        ch = this.more();
        if (ch != 'A') {
            throw new XmlPullParserException("expected <[CDATA[ for comment start", this, null);
        }
        ch = this.more();
        if (ch != '[') {
            throw new XmlPullParserException("expected <![CDATA[ for comment start", this, null);
        }
        final int cdStart = this.pos + this.bufAbsoluteStart;
        final int curLine = this.lineNumber;
        final int curColumn = this.columnNumber;
        final boolean normalizeInput = !this.tokenize || !this.roundtripSupported;
        try {
            if (normalizeInput && hadCharData && !this.usePC) {
                if (this.posEnd > this.posStart) {
                    this.joinPC();
                }
                else {
                    this.usePC = true;
                    final int n = 0;
                    this.pcEnd = n;
                    this.pcStart = n;
                }
            }
            boolean seenBracket = false;
            boolean seenBracketBracket = false;
            boolean normalizedCR = false;
            while (true) {
                ch = this.more();
                if (ch == ']') {
                    if (!seenBracket) {
                        seenBracket = true;
                    }
                    else {
                        seenBracketBracket = true;
                    }
                }
                else if (ch == '>') {
                    if (seenBracket && seenBracketBracket) {
                        break;
                    }
                    seenBracketBracket = false;
                    seenBracket = false;
                }
                else if (seenBracket) {
                    seenBracket = false;
                }
                if (normalizeInput) {
                    if (ch == '\r') {
                        normalizedCR = true;
                        this.posStart = cdStart - this.bufAbsoluteStart;
                        this.posEnd = this.pos - 1;
                        if (!this.usePC) {
                            if (this.posEnd > this.posStart) {
                                this.joinPC();
                            }
                            else {
                                this.usePC = true;
                                final int n2 = 0;
                                this.pcEnd = n2;
                                this.pcStart = n2;
                            }
                        }
                        if (this.pcEnd >= this.pc.length) {
                            this.ensurePC(this.pcEnd);
                        }
                        this.pc[this.pcEnd++] = '\n';
                    }
                    else if (ch == '\n') {
                        if (!normalizedCR && this.usePC) {
                            if (this.pcEnd >= this.pc.length) {
                                this.ensurePC(this.pcEnd);
                            }
                            this.pc[this.pcEnd++] = '\n';
                        }
                        normalizedCR = false;
                    }
                    else {
                        if (this.usePC) {
                            if (this.pcEnd >= this.pc.length) {
                                this.ensurePC(this.pcEnd);
                            }
                            this.pc[this.pcEnd++] = ch;
                        }
                        normalizedCR = false;
                    }
                }
            }
        }
        catch (EOFException ex) {
            throw new XmlPullParserException("CDATA section started on line " + curLine + " and column " + curColumn + " was not closed", this, ex);
        }
        if (normalizeInput && this.usePC) {
            this.pcEnd -= 2;
        }
        this.posStart = cdStart - this.bufAbsoluteStart;
        this.posEnd = this.pos - 3;
    }

    protected void fillBuf() throws IOException, XmlPullParserException {
        if (this.reader == null) {
            throw new XmlPullParserException("reader must be set before parsing is started");
        }
        if (this.bufEnd > this.bufSoftLimit) {
            boolean compact = this.bufStart > this.bufSoftLimit;
            boolean expand = false;
            if (this.preventBufferCompaction) {
                compact = false;
                expand = true;
            }
            else if (!compact) {
                if (this.bufStart < this.buf.length / 2) {
                    expand = true;
                }
                else {
                    compact = true;
                }
            }
            if (compact) {
                System.arraycopy(this.buf, this.bufStart, this.buf, 0, this.bufEnd - this.bufStart);
            }
            else {
                if (!expand) {
                    throw new XmlPullParserException("internal error in fillBuffer()");
                }
                final int newSize = 2 * this.buf.length;
                final char[] newBuf = new char[newSize];
                System.arraycopy(this.buf, this.bufStart, newBuf, 0, this.bufEnd - this.bufStart);
                this.buf = newBuf;
                if (this.bufLoadFactor > 0) {
                    this.bufSoftLimit = (int)(this.bufLoadFactor * (long)this.buf.length / 100L);
                }
            }
            this.bufEnd -= this.bufStart;
            this.pos -= this.bufStart;
            this.posStart -= this.bufStart;
            this.posEnd -= this.bufStart;
            this.bufAbsoluteStart += this.bufStart;
            this.bufStart = 0;
        }
        final int len = (this.buf.length - this.bufEnd > 8192) ? 8192 : (this.buf.length - this.bufEnd);
        final int ret = this.reader.read(this.buf, this.bufEnd, len);
        if (ret > 0) {
            this.bufEnd += ret;
            return;
        }
        if (ret != -1) {
            throw new IOException("error reading input, returned " + ret);
        }
        if (this.bufAbsoluteStart == 0 && this.pos == 0) {
            throw new EOFException("input contained no data");
        }
        if (this.seenRoot && this.depth == 0) {
            this.reachedEnd = true;
            return;
        }
        final StringBuffer expectedTagStack = new StringBuffer();
        if (this.depth > 0) {
            expectedTagStack.append(" - expected end tag");
            if (this.depth > 1) {
                expectedTagStack.append("s");
            }
            expectedTagStack.append(" ");
            for (int i = this.depth; i > 0; --i) {
                final String tagName = new String(this.elRawName[i], 0, this.elRawNameEnd[i]);
                expectedTagStack.append("</").append(tagName).append('>');
            }
            expectedTagStack.append(" to close");
            for (int i = this.depth; i > 0; --i) {
                if (i != this.depth) {
                    expectedTagStack.append(" and");
                }
                final String tagName = new String(this.elRawName[i], 0, this.elRawNameEnd[i]);
                expectedTagStack.append(" start tag <" + tagName + ">");
                expectedTagStack.append(" from line " + this.elRawNameLine[i]);
            }
            expectedTagStack.append(", parser stopped on");
        }
        throw new EOFException("no more data available" + expectedTagStack.toString() + this.getPositionDescription());
    }

    protected char more() throws IOException, XmlPullParserException {
        if (this.pos >= this.bufEnd) {
            this.fillBuf();
            if (this.reachedEnd) {
                return '\uffff';
            }
        }
        final char ch = this.buf[this.pos++];
        if (ch == '\n') {
            ++this.lineNumber;
            this.columnNumber = 1;
        }
        else {
            ++this.columnNumber;
        }
        return ch;
    }

    protected void ensurePC(final int end) {
        final int newSize = (end > 8192) ? (2 * end) : 16384;
        final char[] newPC = new char[newSize];
        System.arraycopy(this.pc, 0, newPC, 0, this.pcEnd);
        this.pc = newPC;
    }

    protected void joinPC() {
        final int len = this.posEnd - this.posStart;
        final int newEnd = this.pcEnd + len + 1;
        if (newEnd >= this.pc.length) {
            this.ensurePC(newEnd);
        }
        System.arraycopy(this.buf, this.posStart, this.pc, this.pcEnd, len);
        this.pcEnd += len;
        this.usePC = true;
    }

    protected char requireInput(char ch, final char[] input) throws XmlPullParserException, IOException {
        for (int i = 0; i < input.length; ++i) {
            if (ch != input[i]) {
                throw new XmlPullParserException("expected " + this.printable(input[i]) + " in " + new String(input) + " and not " + this.printable(ch), this, null);
            }
            ch = this.more();
        }
        return ch;
    }

    protected char requireNextS() throws XmlPullParserException, IOException {
        final char ch = this.more();
        if (!this.isS(ch)) {
            throw new XmlPullParserException("white space is required and not " + this.printable(ch), this, null);
        }
        return this.skipS(ch);
    }

    protected char skipS(char ch) throws XmlPullParserException, IOException {
        while (this.isS(ch)) {
            ch = this.more();
        }
        return ch;
    }

    private static final void setName(final char ch) {
        MXParser.lookupNameChar[ch] = true;
    }

    private static final void setNameStart(final char ch) {
        MXParser.lookupNameStartChar[ch] = true;
        setName(ch);
    }

    protected boolean isNameStartChar(final char ch) {
        return (ch < '\u0400' && MXParser.lookupNameStartChar[ch]) || (ch >= '\u0400' && ch <= '\u2027') || (ch >= '\u202a' && ch <= '\u218f') || (ch >= '\u2800' && ch <= '\uffef');
    }

    protected boolean isNameChar(final char ch) {
        return (ch < '\u0400' && MXParser.lookupNameChar[ch]) || (ch >= '\u0400' && ch <= '\u2027') || (ch >= '\u202a' && ch <= '\u218f') || (ch >= '\u2800' && ch <= '\uffef');
    }

    protected boolean isS(final char ch) {
        return ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t';
    }

    protected String printable(final char ch) {
        if (ch == '\n') {
            return "\\n";
        }
        if (ch == '\r') {
            return "\\r";
        }
        if (ch == '\t') {
            return "\\t";
        }
        if (ch == '\'') {
            return "\\'";
        }
        if (ch > '\u007f' || ch < ' ') {
            return "\\u" + Integer.toHexString(ch);
        }
        return "" + ch;
    }

    protected String printable(String s) {
        if (s == null) {
            return null;
        }
        final int sLen = s.length();
        final StringBuffer buf = new StringBuffer(sLen + 10);
        for (int i = 0; i < sLen; ++i) {
            buf.append(this.printable(s.charAt(i)));
        }
        s = buf.toString();
        return s;
    }

    static {
        VERSION = "version".toCharArray();
        NCODING = "ncoding".toCharArray();
        TANDALONE = "tandalone".toCharArray();
        YES = "yes".toCharArray();
        NO = "no".toCharArray();
        MXParser.lookupNameStartChar = new boolean[1024];
        MXParser.lookupNameChar = new boolean[1024];
        setNameStart(':');
        for (char ch = 'A'; ch <= 'Z'; ++ch) {
            setNameStart(ch);
        }
        setNameStart('_');
        for (char ch = 'a'; ch <= 'z'; ++ch) {
            setNameStart(ch);
        }
        for (char ch = '\u00c0'; ch <= '\u02ff'; ++ch) {
            setNameStart(ch);
        }
        for (char ch = '\u0370'; ch <= '\u037d'; ++ch) {
            setNameStart(ch);
        }
        for (char ch = '\u037f'; ch < '\u0400'; ++ch) {
            setNameStart(ch);
        }
        setName('-');
        setName('.');
        for (char ch = '0'; ch <= '9'; ++ch) {
            setName(ch);
        }
        setName('·');
        for (char ch = '\u0300'; ch <= '\u036f'; ++ch) {
            setName(ch);
        }
    }
}
