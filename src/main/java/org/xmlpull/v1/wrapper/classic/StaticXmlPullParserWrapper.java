//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Administrator\Downloads\Minecraft-Deobfuscator3000-1.2.3\Minecraft-Deobfuscator3000-1.2.3\1.7.10 stable mappings"!

//Decompiled by Procyon!

package org.xmlpull.v1.wrapper.classic;

import org.xmlpull.v1.wrapper.*;
import org.xmlpull.v1.util.*;
import java.io.*;
import org.xmlpull.v1.*;

public class StaticXmlPullParserWrapper extends XmlPullParserDelegate implements XmlPullParserWrapper
{
    public StaticXmlPullParserWrapper(final XmlPullParser pp) {
        super(pp);
    }
    
    public String getAttributeValue(final String name) {
        return XmlPullUtil.getAttributeValue(super.pp, name);
    }
    
    public String getRequiredAttributeValue(final String name) throws IOException, XmlPullParserException {
        return XmlPullUtil.getRequiredAttributeValue(super.pp, (String)null, name);
    }
    
    public String getRequiredAttributeValue(final String namespace, final String name) throws IOException, XmlPullParserException {
        return XmlPullUtil.getRequiredAttributeValue(super.pp, namespace, name);
    }
    
    public String getRequiredElementText(final String namespace, final String name) throws IOException, XmlPullParserException {
        if (name == null) {
            throw new XmlPullParserException("name for element can not be null");
        }
        String text = null;
        this.nextStartTag(namespace, name);
        if (this.isNil()) {
            this.nextEndTag(namespace, name);
        }
        else {
            text = super.pp.nextText();
        }
        super.pp.require(3, namespace, name);
        return text;
    }
    
    public boolean isNil() throws IOException, XmlPullParserException {
        boolean result = false;
        final String value = super.pp.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "nil");
        if ("true".equals(value)) {
            result = true;
        }
        return result;
    }
    
    public String getPITarget() throws IllegalStateException {
        return XmlPullUtil.getPITarget(super.pp);
    }
    
    public String getPIData() throws IllegalStateException {
        return XmlPullUtil.getPIData(super.pp);
    }
    
    public boolean matches(final int type, final String namespace, final String name) throws XmlPullParserException {
        return XmlPullUtil.matches(super.pp, type, namespace, name);
    }
    
    public void nextStartTag() throws XmlPullParserException, IOException {
        if (super.pp.nextTag() != 2) {
            throw new XmlPullParserException("expected START_TAG and not " + super.pp.getPositionDescription());
        }
    }
    
    public void nextStartTag(final String name) throws XmlPullParserException, IOException {
        super.pp.nextTag();
        super.pp.require(2, null, name);
    }
    
    public void nextStartTag(final String namespace, final String name) throws XmlPullParserException, IOException {
        super.pp.nextTag();
        super.pp.require(2, namespace, name);
    }
    
    public void nextEndTag() throws XmlPullParserException, IOException {
        XmlPullUtil.nextEndTag(super.pp);
    }
    
    public void nextEndTag(final String name) throws XmlPullParserException, IOException {
        XmlPullUtil.nextEndTag(super.pp, (String)null, name);
    }
    
    public void nextEndTag(final String namespace, final String name) throws XmlPullParserException, IOException {
        XmlPullUtil.nextEndTag(super.pp, namespace, name);
    }
    
    public String nextText(final String namespace, final String name) throws IOException, XmlPullParserException {
        return XmlPullUtil.nextText(super.pp, namespace, name);
    }
    
    public void skipSubTree() throws XmlPullParserException, IOException {
        XmlPullUtil.skipSubTree(super.pp);
    }
    
    public double readDouble() throws XmlPullParserException, IOException {
        final String value = super.pp.nextText();
        double d;
        try {
            d = Double.parseDouble(value);
        }
        catch (NumberFormatException ex) {
            if (value.equals("INF") || value.toLowerCase().equals("infinity")) {
                d = Double.POSITIVE_INFINITY;
            }
            else if (value.equals("-INF") || value.toLowerCase().equals("-infinity")) {
                d = Double.NEGATIVE_INFINITY;
            }
            else {
                if (!value.equals("NaN")) {
                    throw new XmlPullParserException("can't parse double value '" + value + "'", this, ex);
                }
                d = Double.NaN;
            }
        }
        return d;
    }
    
    public float readFloat() throws XmlPullParserException, IOException {
        final String value = super.pp.nextText();
        float f;
        try {
            f = Float.parseFloat(value);
        }
        catch (NumberFormatException ex) {
            if (value.equals("INF") || value.toLowerCase().equals("infinity")) {
                f = Float.POSITIVE_INFINITY;
            }
            else if (value.equals("-INF") || value.toLowerCase().equals("-infinity")) {
                f = Float.NEGATIVE_INFINITY;
            }
            else {
                if (!value.equals("NaN")) {
                    throw new XmlPullParserException("can't parse float value '" + value + "'", this, ex);
                }
                f = Float.NaN;
            }
        }
        return f;
    }
    
    private int parseDigits(final String text, int offset, final int length) throws XmlPullParserException {
        int value = 0;
        if (length > 9) {
            try {
                value = Integer.parseInt(text.substring(offset, offset + length));
                return value;
            }
            catch (NumberFormatException ex) {
                throw new XmlPullParserException(ex.getMessage());
            }
        }
        final int limit = offset + length;
        while (offset < limit) {
            final char chr = text.charAt(offset++);
            if (chr < '0' || chr > '9') {
                throw new XmlPullParserException("non-digit in number value", this, null);
            }
            value = value * 10 + (chr - '0');
        }
        return value;
    }
    
    private int parseInt(final String text) throws XmlPullParserException {
        int offset = 0;
        final int limit = text.length();
        if (limit == 0) {
            throw new XmlPullParserException("empty number value", this, null);
        }
        boolean negate = false;
        final char chr = text.charAt(0);
        if (chr == '-') {
            if (limit > 9) {
                try {
                    return Integer.parseInt(text);
                }
                catch (NumberFormatException ex) {
                    throw new XmlPullParserException(ex.getMessage(), this, null);
                }
            }
            negate = true;
            ++offset;
        }
        else if (chr == '+') {
            ++offset;
        }
        if (offset >= limit) {
            throw new XmlPullParserException("Invalid number format", this, null);
        }
        final int value = this.parseDigits(text, offset, limit - offset);
        if (negate) {
            return -value;
        }
        return value;
    }
    
    public int readInt() throws XmlPullParserException, IOException {
        try {
            final int i = this.parseInt(super.pp.nextText());
            return i;
        }
        catch (NumberFormatException ex) {
            throw new XmlPullParserException("can't parse int value", this, ex);
        }
    }
    
    public String readString() throws XmlPullParserException, IOException {
        final String xsiNil = super.pp.getAttributeValue("http://www.w3.org/2001/XMLSchema", "nil");
        if ("true".equals(xsiNil)) {
            this.nextEndTag();
            return null;
        }
        return super.pp.nextText();
    }
    
    public double readDoubleElement(final String namespace, final String name) throws XmlPullParserException, IOException {
        super.pp.require(2, namespace, name);
        return this.readDouble();
    }
    
    public float readFloatElement(final String namespace, final String name) throws XmlPullParserException, IOException {
        super.pp.require(2, namespace, name);
        return this.readFloat();
    }
    
    public int readIntElement(final String namespace, final String name) throws XmlPullParserException, IOException {
        super.pp.require(2, namespace, name);
        return this.readInt();
    }
    
    public String readStringElemet(final String namespace, final String name) throws XmlPullParserException, IOException {
        super.pp.require(2, namespace, name);
        return this.readString();
    }
}
