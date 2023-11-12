//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Administrator\Downloads\Minecraft-Deobfuscator3000-1.2.3\Minecraft-Deobfuscator3000-1.2.3\1.7.10 stable mappings"!

//Decompiled by Procyon!

package org.xmlpull.v1.wrapper.classic;

import org.xmlpull.v1.*;
import java.io.*;

public class XmlSerializerDelegate implements XmlSerializer
{
    protected XmlSerializer xs;
    
    public XmlSerializerDelegate(final XmlSerializer serializer) {
        this.xs = serializer;
    }
    
    public String getName() {
        return this.xs.getName();
    }
    
    public void setPrefix(final String prefix, final String namespace) throws IOException, IllegalArgumentException, IllegalStateException {
        this.xs.setPrefix(prefix, namespace);
    }
    
    public void setOutput(final OutputStream os, final String encoding) throws IOException, IllegalArgumentException, IllegalStateException {
        this.xs.setOutput(os, encoding);
    }
    
    public void endDocument() throws IOException, IllegalArgumentException, IllegalStateException {
        this.xs.endDocument();
    }
    
    public void comment(final String text) throws IOException, IllegalArgumentException, IllegalStateException {
        this.xs.comment(text);
    }
    
    public int getDepth() {
        return this.xs.getDepth();
    }
    
    public void setProperty(final String name, final Object value) throws IllegalArgumentException, IllegalStateException {
        this.xs.setProperty(name, value);
    }
    
    public void cdsect(final String text) throws IOException, IllegalArgumentException, IllegalStateException {
        this.xs.cdsect(text);
    }
    
    public void setFeature(final String name, final boolean state) throws IllegalArgumentException, IllegalStateException {
        this.xs.setFeature(name, state);
    }
    
    public void entityRef(final String text) throws IOException, IllegalArgumentException, IllegalStateException {
        this.xs.entityRef(text);
    }
    
    public void processingInstruction(final String text) throws IOException, IllegalArgumentException, IllegalStateException {
        this.xs.processingInstruction(text);
    }
    
    public void setOutput(final Writer writer) throws IOException, IllegalArgumentException, IllegalStateException {
        this.xs.setOutput(writer);
    }
    
    public void docdecl(final String text) throws IOException, IllegalArgumentException, IllegalStateException {
        this.xs.docdecl(text);
    }
    
    public void flush() throws IOException {
        this.xs.flush();
    }
    
    public Object getProperty(final String name) {
        return this.xs.getProperty(name);
    }
    
    public XmlSerializer startTag(final String namespace, final String name) throws IOException, IllegalArgumentException, IllegalStateException {
        return this.xs.startTag(namespace, name);
    }
    
    public void ignorableWhitespace(final String text) throws IOException, IllegalArgumentException, IllegalStateException {
        this.xs.ignorableWhitespace(text);
    }
    
    public XmlSerializer text(final String text) throws IOException, IllegalArgumentException, IllegalStateException {
        return this.xs.text(text);
    }
    
    public boolean getFeature(final String name) {
        return this.xs.getFeature(name);
    }
    
    public XmlSerializer attribute(final String namespace, final String name, final String value) throws IOException, IllegalArgumentException, IllegalStateException {
        return this.xs.attribute(namespace, name, value);
    }
    
    public void startDocument(final String encoding, final Boolean standalone) throws IOException, IllegalArgumentException, IllegalStateException {
        this.xs.startDocument(encoding, standalone);
    }
    
    public String getPrefix(final String namespace, final boolean generatePrefix) throws IllegalArgumentException {
        return this.xs.getPrefix(namespace, generatePrefix);
    }
    
    public String getNamespace() {
        return this.xs.getNamespace();
    }
    
    public XmlSerializer endTag(final String namespace, final String name) throws IOException, IllegalArgumentException, IllegalStateException {
        return this.xs.endTag(namespace, name);
    }
    
    public XmlSerializer text(final char[] buf, final int start, final int len) throws IOException, IllegalArgumentException, IllegalStateException {
        return this.xs.text(buf, start, len);
    }
}
