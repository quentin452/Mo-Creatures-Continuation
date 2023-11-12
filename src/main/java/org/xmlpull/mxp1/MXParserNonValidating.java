package org.xmlpull.mxp1;

import org.xmlpull.v1.*;
import java.io.*;

public class MXParserNonValidating extends MXParserCachingStrings
{
    private boolean processDocDecl;
    
    public void setFeature(final String name, final boolean state) throws XmlPullParserException {
        if ("http://xmlpull.org/v1/doc/features.html#process-docdecl".equals(name)) {
            if (((MXParser)this).eventType != 0) {
                throw new XmlPullParserException("process DOCDECL feature can only be changed before parsing", (XmlPullParser)this, null);
            }
            if (!(this.processDocDecl = state)) {}
        }
        else {
            super.setFeature(name, state);
        }
    }
    
    public boolean getFeature(final String name) {
        if ("http://xmlpull.org/v1/doc/features.html#process-docdecl".equals(name)) {
            return this.processDocDecl;
        }
        return super.getFeature(name);
    }
    
    protected char more() throws IOException, XmlPullParserException {
        return super.more();
    }
    
    protected char[] lookuEntityReplacement(final int entitNameLen) throws XmlPullParserException, IOException {
        if (!((MXParser)this).allStringsInterned) {
            final int hash = MXParser.fastHash(((MXParser)this).buf, ((MXParser)this).posStart, ((MXParser)this).posEnd - ((MXParser)this).posStart);
        Label_0130:
            for (int i = ((MXParser)this).entityEnd - 1; i >= 0; --i) {
                if (hash == ((MXParser)this).entityNameHash[i] && entitNameLen == ((MXParser)this).entityNameBuf[i].length) {
                    final char[] entityBuf = ((MXParser)this).entityNameBuf[i];
                    for (int j = 0; j < entitNameLen; ++j) {
                        if (((MXParser)this).buf[((MXParser)this).posStart + j] != entityBuf[j]) {
                            continue Label_0130;
                        }
                    }
                    if (((MXParser)this).tokenize) {
                        ((MXParser)this).text = ((MXParser)this).entityReplacement[i];
                    }
                    return ((MXParser)this).entityReplacementBuf[i];
                }
            }
        }
        else {
            ((MXParser)this).entityRefName = this.newString(((MXParser)this).buf, ((MXParser)this).posStart, ((MXParser)this).posEnd - ((MXParser)this).posStart);
            for (int k = ((MXParser)this).entityEnd - 1; k >= 0; --k) {
                if (((MXParser)this).entityRefName == ((MXParser)this).entityName[k]) {
                    if (((MXParser)this).tokenize) {
                        ((MXParser)this).text = ((MXParser)this).entityReplacement[k];
                    }
                    return ((MXParser)this).entityReplacementBuf[k];
                }
            }
        }
        return null;
    }
    
    protected void parseDocdecl() throws XmlPullParserException, IOException {
        final boolean oldTokenize = ((MXParser)this).tokenize;
        try {
            char ch = this.more();
            if (ch != 'O') {
                throw new XmlPullParserException("expected <!DOCTYPE", (XmlPullParser)this, null);
            }
            ch = this.more();
            if (ch != 'C') {
                throw new XmlPullParserException("expected <!DOCTYPE", (XmlPullParser)this, null);
            }
            ch = this.more();
            if (ch != 'T') {
                throw new XmlPullParserException("expected <!DOCTYPE", (XmlPullParser)this, null);
            }
            ch = this.more();
            if (ch != 'Y') {
                throw new XmlPullParserException("expected <!DOCTYPE", (XmlPullParser)this, null);
            }
            ch = this.more();
            if (ch != 'P') {
                throw new XmlPullParserException("expected <!DOCTYPE", (XmlPullParser)this, null);
            }
            ch = this.more();
            if (ch != 'E') {
                throw new XmlPullParserException("expected <!DOCTYPE", (XmlPullParser)this, null);
            }
            ((MXParser)this).posStart = ((MXParser)this).pos;
            ch = ((MXParser)this).requireNextS();
            final int nameStart = ((MXParser)this).pos;
            ch = this.readName(ch);
            final int nameEnd = ((MXParser)this).pos;
            ch = ((MXParser)this).skipS(ch);
            if (ch == 'S' || ch == 'P') {
                ch = this.processExternalId(ch);
                ch = ((MXParser)this).skipS(ch);
            }
            if (ch == '[') {
                this.processInternalSubset();
            }
            ch = ((MXParser)this).skipS(ch);
            if (ch != '>') {
                throw new XmlPullParserException("expected > to finish <[DOCTYPE but got " + ((MXParser)this).printable(ch), (XmlPullParser)this, null);
            }
            ((MXParser)this).posEnd = ((MXParser)this).pos - 1;
        }
        finally {
            ((MXParser)this).tokenize = oldTokenize;
        }
    }
    
    protected char processExternalId(final char ch) throws XmlPullParserException, IOException {
        return ch;
    }
    
    protected void processInternalSubset() throws XmlPullParserException, IOException {
        while (true) {
            char ch = this.more();
            if (ch == ']') {
                break;
            }
            if (ch == '%') {
                this.processPEReference();
            }
            else if (((MXParser)this).isS(ch)) {
                ch = ((MXParser)this).skipS(ch);
            }
            else {
                this.processMarkupDecl(ch);
            }
        }
    }
    
    protected void processPEReference() throws XmlPullParserException, IOException {
    }
    
    protected void processMarkupDecl(char ch) throws XmlPullParserException, IOException {
        if (ch != '<') {
            throw new XmlPullParserException("expected < for markupdecl in DTD not " + ((MXParser)this).printable(ch), (XmlPullParser)this, null);
        }
        ch = this.more();
        if (ch == '?') {
            ((MXParser)this).parsePI();
        }
        else {
            if (ch != '!') {
                throw new XmlPullParserException("expected markupdecl in DTD not " + ((MXParser)this).printable(ch), (XmlPullParser)this, null);
            }
            ch = this.more();
            if (ch == '-') {
                ((MXParser)this).parseComment();
            }
            else {
                ch = this.more();
                if (ch == 'A') {
                    this.processAttlistDecl(ch);
                }
                else if (ch == 'E') {
                    ch = this.more();
                    if (ch == 'L') {
                        this.processElementDecl(ch);
                    }
                    else {
                        if (ch != 'N') {
                            throw new XmlPullParserException("expected ELEMENT or ENTITY after <! in DTD not " + ((MXParser)this).printable(ch), (XmlPullParser)this, null);
                        }
                        this.processEntityDecl(ch);
                    }
                }
                else {
                    if (ch != 'N') {
                        throw new XmlPullParserException("expected markupdecl after <! in DTD not " + ((MXParser)this).printable(ch), (XmlPullParser)this, null);
                    }
                    this.processNotationDecl(ch);
                }
            }
        }
    }
    
    protected void processElementDecl(char ch) throws XmlPullParserException, IOException {
        ch = ((MXParser)this).requireNextS();
        this.readName(ch);
        ch = ((MXParser)this).requireNextS();
    }
    
    protected void processAttlistDecl(final char ch) throws XmlPullParserException, IOException {
    }
    
    protected void processEntityDecl(final char ch) throws XmlPullParserException, IOException {
    }
    
    protected void processNotationDecl(final char ch) throws XmlPullParserException, IOException {
    }
    
    protected char readName(char ch) throws XmlPullParserException, IOException {
        if (((MXParser)this).isNameStartChar(ch)) {
            throw new XmlPullParserException("XML name must start with name start character not " + ((MXParser)this).printable(ch), (XmlPullParser)this, null);
        }
        while (((MXParser)this).isNameChar(ch)) {
            ch = this.more();
        }
        return ch;
    }
}
