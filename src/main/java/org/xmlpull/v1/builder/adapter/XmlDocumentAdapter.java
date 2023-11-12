//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Administrator\Downloads\Minecraft-Deobfuscator3000-1.2.3\Minecraft-Deobfuscator3000-1.2.3\1.7.10 stable mappings"!

//Decompiled by Procyon!

package org.xmlpull.v1.builder.adapter;

import org.xmlpull.v1.builder.*;
import org.xmlpull.v1.builder.Iterable;

public class XmlDocumentAdapter implements XmlDocument
{
    private XmlDocument target;

    public Object clone() throws CloneNotSupportedException {
        final XmlDocumentAdapter ela = (XmlDocumentAdapter)super.clone();
        ela.target = (XmlDocument)this.target.clone();
        return ela;
    }

    public XmlDocumentAdapter(final XmlDocument target) {
        this.target = target;
        this.fixImportedChildParent(target.getDocumentElement());
    }

    private void fixImportedChildParent(final Object child) {
        if (child instanceof XmlElement) {
            final XmlElement childEl = (XmlElement)child;
            final XmlContainer childElParent = childEl.getParent();
            if (childElParent == this.target) {
                childEl.setParent(this);
            }
        }
    }

    public Iterable children() {
        return this.target.children();
    }

    public XmlElement getDocumentElement() {
        return this.target.getDocumentElement();
    }

    public XmlElement requiredElement(final XmlNamespace n, final String name) {
        return this.target.requiredElement(n, name);
    }

    public XmlElement element(final XmlNamespace n, final String name) {
        return this.target.element(n, name);
    }

    public XmlElement element(final XmlNamespace n, final String name, final boolean create) {
        return this.target.element(n, name, create);
    }

    public Iterable notations() {
        return this.target.notations();
    }

    public Iterable unparsedEntities() {
        return this.target.unparsedEntities();
    }

    public String getBaseUri() {
        return this.target.getBaseUri();
    }

    public String getCharacterEncodingScheme() {
        return this.target.getCharacterEncodingScheme();
    }

    public void setCharacterEncodingScheme(final String characterEncoding) {
        this.target.setCharacterEncodingScheme(characterEncoding);
    }

    public Boolean isStandalone() {
        return this.target.isStandalone();
    }

    public String getVersion() {
        return this.target.getVersion();
    }

    public boolean isAllDeclarationsProcessed() {
        return this.target.isAllDeclarationsProcessed();
    }

    public void setDocumentElement(final XmlElement rootElement) {
        this.target.setDocumentElement(rootElement);
    }

    public void addChild(final Object child) {
        this.target.addChild(child);
    }

    public void insertChild(final int pos, final Object child) {
        this.target.insertChild(pos, child);
    }

    public void removeAllChildren() {
        this.target.removeAllChildren();
    }

    public XmlComment newComment(final String content) {
        return this.target.newComment(content);
    }

    public XmlComment addComment(final String content) {
        return this.target.addComment(content);
    }

    public XmlDoctype newDoctype(final String systemIdentifier, final String publicIdentifier) {
        return this.target.newDoctype(systemIdentifier, publicIdentifier);
    }

    public XmlDoctype addDoctype(final String systemIdentifier, final String publicIdentifier) {
        return this.target.addDoctype(systemIdentifier, publicIdentifier);
    }

    public XmlElement addDocumentElement(final String name) {
        return this.target.addDocumentElement(name);
    }

    public XmlElement addDocumentElement(final XmlNamespace namespace, final String name) {
        return this.target.addDocumentElement(namespace, name);
    }

    public XmlProcessingInstruction newProcessingInstruction(final String target, final String content) {
        return this.target.newProcessingInstruction(target, content);
    }

    public XmlProcessingInstruction addProcessingInstruction(final String target, final String content) {
        return this.target.addProcessingInstruction(target, content);
    }

    public void removeAllUnparsedEntities() {
        this.target.removeAllUnparsedEntities();
    }

    public XmlNotation addNotation(final String name, final String systemIdentifier, final String publicIdentifier, final String declarationBaseUri) {
        return this.target.addNotation(name, systemIdentifier, publicIdentifier, declarationBaseUri);
    }

    public void removeAllNotations() {
        this.target.removeAllNotations();
    }
}
