package org.xmlpull.v1.wrapper.classic;

import org.xmlpull.v1.*;
import java.io.*;

public class XmlPullParserDelegate implements XmlPullParser
{
    protected XmlPullParser pp;
    
    public XmlPullParserDelegate(final XmlPullParser pp) {
        this.pp = pp;
    }
    
    public String getText() {
        return this.pp.getText();
    }
    
    public void setFeature(final String name, final boolean state) throws XmlPullParserException {
        this.pp.setFeature(name, state);
    }
    
    public char[] getTextCharacters(final int[] holderForStartAndLength) {
        return this.pp.getTextCharacters(holderForStartAndLength);
    }
    
    public int getColumnNumber() {
        return this.pp.getColumnNumber();
    }
    
    public int getNamespaceCount(final int depth) throws XmlPullParserException {
        return this.pp.getNamespaceCount(depth);
    }
    
    public String getNamespacePrefix(final int pos) throws XmlPullParserException {
        return this.pp.getNamespacePrefix(pos);
    }
    
    public String getAttributeName(final int index) {
        return this.pp.getAttributeName(index);
    }
    
    public String getName() {
        return this.pp.getName();
    }
    
    public boolean getFeature(final String name) {
        return this.pp.getFeature(name);
    }
    
    public String getInputEncoding() {
        return this.pp.getInputEncoding();
    }
    
    public String getAttributeValue(final int index) {
        return this.pp.getAttributeValue(index);
    }
    
    public String getNamespace(final String prefix) {
        return this.pp.getNamespace(prefix);
    }
    
    public void setInput(final Reader in) throws XmlPullParserException {
        this.pp.setInput(in);
    }
    
    public int getLineNumber() {
        return this.pp.getLineNumber();
    }
    
    public Object getProperty(final String name) {
        return this.pp.getProperty(name);
    }
    
    public boolean isEmptyElementTag() throws XmlPullParserException {
        return this.pp.isEmptyElementTag();
    }
    
    public boolean isAttributeDefault(final int index) {
        return this.pp.isAttributeDefault(index);
    }
    
    public String getNamespaceUri(final int pos) throws XmlPullParserException {
        return this.pp.getNamespaceUri(pos);
    }
    
    public int next() throws XmlPullParserException, IOException {
        return this.pp.next();
    }
    
    public int nextToken() throws XmlPullParserException, IOException {
        return this.pp.nextToken();
    }
    
    public void defineEntityReplacementText(final String entityName, final String replacementText) throws XmlPullParserException {
        this.pp.defineEntityReplacementText(entityName, replacementText);
    }
    
    public int getAttributeCount() {
        return this.pp.getAttributeCount();
    }
    
    public boolean isWhitespace() throws XmlPullParserException {
        return this.pp.isWhitespace();
    }
    
    public String getPrefix() {
        return this.pp.getPrefix();
    }
    
    public void require(final int type, final String namespace, final String name) throws XmlPullParserException, IOException {
        this.pp.require(type, namespace, name);
    }
    
    public String nextText() throws XmlPullParserException, IOException {
        return this.pp.nextText();
    }
    
    public String getAttributeType(final int index) {
        return this.pp.getAttributeType(index);
    }
    
    public int getDepth() {
        return this.pp.getDepth();
    }
    
    public int nextTag() throws XmlPullParserException, IOException {
        return this.pp.nextTag();
    }
    
    public int getEventType() throws XmlPullParserException {
        return this.pp.getEventType();
    }
    
    public String getAttributePrefix(final int index) {
        return this.pp.getAttributePrefix(index);
    }
    
    public void setInput(final InputStream inputStream, final String inputEncoding) throws XmlPullParserException {
        this.pp.setInput(inputStream, inputEncoding);
    }
    
    public String getAttributeValue(final String namespace, final String name) {
        return this.pp.getAttributeValue(namespace, name);
    }
    
    public void setProperty(final String name, final Object value) throws XmlPullParserException {
        this.pp.setProperty(name, value);
    }
    
    public String getPositionDescription() {
        return this.pp.getPositionDescription();
    }
    
    public String getNamespace() {
        return this.pp.getNamespace();
    }
    
    public String getAttributeNamespace(final int index) {
        return this.pp.getAttributeNamespace(index);
    }
}
