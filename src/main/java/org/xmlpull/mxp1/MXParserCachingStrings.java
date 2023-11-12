//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Administrator\Downloads\Minecraft-Deobfuscator3000-1.2.3\Minecraft-Deobfuscator3000-1.2.3\1.7.10 stable mappings"!

//Decompiled by Procyon!

package org.xmlpull.mxp1;

import java.io.*;
import org.xmlpull.v1.*;

public class MXParserCachingStrings extends MXParser implements Cloneable
{
    protected static final boolean CACHE_STATISTICS = false;
    protected static final boolean TRACE_SIZING = false;
    protected static final int INITIAL_CAPACITY = 13;
    protected int cacheStatCalls;
    protected int cacheStatWalks;
    protected int cacheStatResets;
    protected int cacheStatRehash;
    protected static final int CACHE_LOAD = 77;
    protected int cacheEntriesCount;
    protected int cacheEntriesThreshold;
    protected char[][] keys;
    protected String[] values;
    
    public Object clone() throws CloneNotSupportedException {
        if (super.reader != null && !(super.reader instanceof Cloneable)) {
            throw new CloneNotSupportedException("reader used in parser must implement Cloneable!");
        }
        final MXParserCachingStrings cloned = (MXParserCachingStrings)super.clone();
        if (super.reader != null) {
            try {
                final Object o = super.reader.getClass().getMethod("clone", (Class<?>[])null).invoke(super.reader, (Object[])null);
                cloned.reader = (Reader)o;
            }
            catch (Exception e) {
                final CloneNotSupportedException ee = new CloneNotSupportedException("failed to call clone() on reader " + super.reader + ":" + e);
                ee.initCause(e);
                throw ee;
            }
        }
        if (this.keys != null) {
            cloned.keys = this.keys.clone();
        }
        if (this.values != null) {
            cloned.values = this.values.clone();
        }
        if (super.elRawName != null) {
            cloned.elRawName = this.cloneCCArr(super.elRawName);
        }
        if (super.elRawNameEnd != null) {
            cloned.elRawNameEnd = super.elRawNameEnd.clone();
        }
        if (super.elRawNameLine != null) {
            cloned.elRawNameLine = super.elRawNameLine.clone();
        }
        if (super.elName != null) {
            cloned.elName = super.elName.clone();
        }
        if (super.elPrefix != null) {
            cloned.elPrefix = super.elPrefix.clone();
        }
        if (super.elUri != null) {
            cloned.elUri = super.elUri.clone();
        }
        if (super.elNamespaceCount != null) {
            cloned.elNamespaceCount = super.elNamespaceCount.clone();
        }
        if (super.attributeName != null) {
            cloned.attributeName = super.attributeName.clone();
        }
        if (super.attributeNameHash != null) {
            cloned.attributeNameHash = super.attributeNameHash.clone();
        }
        if (super.attributePrefix != null) {
            cloned.attributePrefix = super.attributePrefix.clone();
        }
        if (super.attributeUri != null) {
            cloned.attributeUri = super.attributeUri.clone();
        }
        if (super.attributeValue != null) {
            cloned.attributeValue = super.attributeValue.clone();
        }
        if (super.namespacePrefix != null) {
            cloned.namespacePrefix = super.namespacePrefix.clone();
        }
        if (super.namespacePrefixHash != null) {
            cloned.namespacePrefixHash = super.namespacePrefixHash.clone();
        }
        if (super.namespaceUri != null) {
            cloned.namespaceUri = super.namespaceUri.clone();
        }
        if (super.entityName != null) {
            cloned.entityName = super.entityName.clone();
        }
        if (super.entityNameBuf != null) {
            cloned.entityNameBuf = this.cloneCCArr(super.entityNameBuf);
        }
        if (super.entityNameHash != null) {
            cloned.entityNameHash = super.entityNameHash.clone();
        }
        if (super.entityReplacementBuf != null) {
            cloned.entityReplacementBuf = this.cloneCCArr(super.entityReplacementBuf);
        }
        if (super.entityReplacement != null) {
            cloned.entityReplacement = super.entityReplacement.clone();
        }
        if (super.buf != null) {
            cloned.buf = super.buf.clone();
        }
        if (super.pc != null) {
            cloned.pc = super.pc.clone();
        }
        if (super.charRefOneCharBuf != null) {
            cloned.charRefOneCharBuf = super.charRefOneCharBuf.clone();
        }
        return cloned;
    }
    
    private char[][] cloneCCArr(final char[][] ccarr) {
        final char[][] cca = ccarr.clone();
        for (int i = 0; i < cca.length; ++i) {
            if (cca[i] != null) {
                cca[i] = cca[i].clone();
            }
        }
        return cca;
    }
    
    public MXParserCachingStrings() {
        super.allStringsInterned = true;
        this.initStringCache();
    }
    
    public void setFeature(final String name, final boolean state) throws XmlPullParserException {
        if ("http://xmlpull.org/v1/doc/features.html#names-interned".equals(name)) {
            if (super.eventType != 0) {
                throw new XmlPullParserException("interning names feature can only be changed before parsing", (XmlPullParser)this, null);
            }
            super.allStringsInterned = state;
            if (!state && this.keys != null) {
                this.resetStringCache();
            }
        }
        else {
            super.setFeature(name, state);
        }
    }
    
    public boolean getFeature(final String name) {
        if ("http://xmlpull.org/v1/doc/features.html#names-interned".equals(name)) {
            return super.allStringsInterned;
        }
        return super.getFeature(name);
    }
    
    public void finalize() {
    }
    
    protected String newString(final char[] cbuf, final int off, final int len) {
        if (super.allStringsInterned) {
            return this.newStringIntern(cbuf, off, len);
        }
        return super.newString(cbuf, off, len);
    }
    
    protected String newStringIntern(final char[] cbuf, final int off, final int len) {
        if (this.cacheEntriesCount >= this.cacheEntriesThreshold) {
            this.rehash();
        }
        int offset;
        char[] k;
        for (offset = MXParser.fastHash(cbuf, off, len) % this.keys.length, k = null; (k = this.keys[offset]) != null && !keysAreEqual(k, 0, k.length, cbuf, off, len); offset = (offset + 1) % this.keys.length) {}
        if (k != null) {
            return this.values[offset];
        }
        k = new char[len];
        System.arraycopy(cbuf, off, k, 0, len);
        final String v = new String(k).intern();
        this.keys[offset] = k;
        this.values[offset] = v;
        ++this.cacheEntriesCount;
        return v;
    }
    
    protected void initStringCache() {
        if (this.keys == null) {
            this.cacheEntriesThreshold = 10;
            if (this.cacheEntriesThreshold >= 13) {
                throw new RuntimeException("internal error: threshold must be less than capacity: 13");
            }
            this.keys = new char[13][];
            this.values = new String[13];
            this.cacheEntriesCount = 0;
        }
    }
    
    protected void resetStringCache() {
        this.initStringCache();
    }
    
    private void rehash() {
        final int newSize = 2 * this.keys.length + 1;
        this.cacheEntriesThreshold = newSize * 77 / 100;
        if (this.cacheEntriesThreshold >= newSize) {
            throw new RuntimeException("internal error: threshold must be less than capacity: " + newSize);
        }
        final char[][] newKeys = new char[newSize][];
        final String[] newValues = new String[newSize];
        for (int i = 0; i < this.keys.length; ++i) {
            final char[] k = this.keys[i];
            this.keys[i] = null;
            final String v = this.values[i];
            this.values[i] = null;
            if (k != null) {
                int newOffset = MXParser.fastHash(k, 0, k.length) % newSize;
                char[] newk = null;
                while ((newk = newKeys[newOffset]) != null) {
                    if (keysAreEqual(newk, 0, newk.length, k, 0, k.length)) {
                        throw new RuntimeException("internal cache error: duplicated keys: " + new String(newk) + " and " + new String(k));
                    }
                    newOffset = (newOffset + 1) % newSize;
                }
                newKeys[newOffset] = k;
                newValues[newOffset] = v;
            }
        }
        this.keys = newKeys;
        this.values = newValues;
    }
    
    private static final boolean keysAreEqual(final char[] a, final int astart, final int alength, final char[] b, final int bstart, final int blength) {
        if (alength != blength) {
            return false;
        }
        for (int i = 0; i < alength; ++i) {
            if (a[astart + i] != b[bstart + i]) {
                return false;
            }
        }
        return true;
    }
}
