package org.xmlpull.v1.builder.impl;

import java.util.*;
import org.xmlpull.v1.builder.*;
import org.xmlpull.v1.builder.Iterable;

public class XmlDocumentImpl implements XmlDocument
{
    private List children;
    private XmlElement root;
    private String version;
    private Boolean standalone;
    private String characterEncoding;

    public Object clone() throws CloneNotSupportedException {
        final XmlDocumentImpl cloned = (XmlDocumentImpl)super.clone();
        cloned.root = null;
        cloned.children = this.cloneList(cloned, this.children);
        final int pos = cloned.findDocumentElement();
        if (pos >= 0) {
            (cloned.root = (XmlElement) cloned.children.get(pos)).setParent(cloned);
        }
        return cloned;
    }

    private List cloneList(final XmlDocumentImpl cloned, final List list) throws CloneNotSupportedException {
        if (list == null) {
            return null;
        }
        final List newList = new ArrayList(list.size());
        for (int i = 0; i < list.size(); ++i) {
            final Object member = list.get(i);
            Object newMember = null;
            Label_0190: {
                if (!(member instanceof XmlElement)) {
                    if (member instanceof Cloneable) {
                        try {
                            newMember = member.getClass().getMethod("clone", (Class<?>[])null).invoke(member, (Object[])null);
                            break Label_0190;
                        }
                        catch (Exception e) {
                            throw new CloneNotSupportedException("failed to call clone() on  " + member + e);
                        }
                    }
                    throw new CloneNotSupportedException("could not clone " + member + " of " + ((member != null) ? member.getClass().toString() : ""));
                }
                final XmlElement el = (XmlElement)member;
                newMember = el.clone();
            }
            newList.add(newMember);
        }
        return newList;
    }

    public XmlDocumentImpl(final String version, final Boolean standalone, final String characterEncoding) {
        this.children = new ArrayList();
        this.version = version;
        this.standalone = standalone;
        this.characterEncoding = characterEncoding;
    }

    public String getVersion() {
        return this.version;
    }

    public Boolean isStandalone() {
        return this.standalone;
    }

    public String getCharacterEncodingScheme() {
        return this.characterEncoding;
    }

    public void setCharacterEncodingScheme(final String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    public XmlProcessingInstruction newProcessingInstruction(final String target, final String content) {
        throw new XmlBuilderException("not implemented");
    }

    public XmlProcessingInstruction addProcessingInstruction(final String target, final String content) {
        throw new XmlBuilderException("not implemented");
    }

    public Iterable children() {
        return () -> XmlDocumentImpl.this.children.iterator();
    }

    public void removeAllUnparsedEntities() {
        throw new XmlBuilderException("not implemented");
    }

    public void setDocumentElement(final XmlElement rootElement) {
        final int pos = this.findDocumentElement();
        if (pos >= 0) {
            this.children.set(pos, rootElement);
        }
        else {
            this.children.add(rootElement);
        }
        (this.root = rootElement).setParent(this);
    }

    private int findDocumentElement() {
        for (int i = 0; i < this.children.size(); ++i) {
            final Object element = this.children.get(i);
            if (element instanceof XmlElement) {
                return i;
            }
        }
        return -1;
    }

    public XmlElement requiredElement(final XmlNamespace n, final String name) {
        final XmlElement el = this.element(n, name);
        if (el == null) {
            throw new XmlBuilderException("document does not contain element with name " + name + " in namespace " + n.getNamespaceName());
        }
        return el;
    }

    public XmlElement element(final XmlNamespace n, final String name) {
        return this.element(n, name, false);
    }

    public XmlElement element(final XmlNamespace namespace, final String name, final boolean create) {
        final XmlElement e = this.getDocumentElement();
        if (e == null) {
            return null;
        }
        final String eNamespaceName = (e.getNamespace() != null) ? e.getNamespace().getNamespaceName() : null;
        if (namespace != null) {
            if (name.equals(e.getName()) && eNamespaceName != null && eNamespaceName.equals(namespace.getNamespaceName())) {
                return e;
            }
        }
        else if (name.equals(e.getName()) && eNamespaceName == null) {
            return e;
        }
        if (create) {
            return this.addDocumentElement(namespace, name);
        }
        return null;
    }

    public void insertChild(final int pos, final Object child) {
        throw new XmlBuilderException("not implemented");
    }

    public XmlComment addComment(final String content) {
        final XmlComment comment = (XmlComment)new XmlCommentImpl((XmlContainer)this, content);
        this.children.add(comment);
        return comment;
    }

    public XmlDoctype newDoctype(final String systemIdentifier, final String publicIdentifier) {
        throw new XmlBuilderException("not implemented");
    }

    public Iterable unparsedEntities() {
        throw new XmlBuilderException("not implemented");
    }

    public void removeAllChildren() {
        throw new XmlBuilderException("not implemented");
    }

    public XmlComment newComment(final String content) {
        return (XmlComment)new XmlCommentImpl((XmlContainer)null, content);
    }

    public void removeAllNotations() {
        throw new XmlBuilderException("not implemented");
    }

    public XmlDoctype addDoctype(final String systemIdentifier, final String publicIdentifier) {
        throw new XmlBuilderException("not implemented");
    }

    public void addChild(final Object child) {
        throw new XmlBuilderException("not implemented");
    }

    public XmlNotation addNotation(final String name, final String systemIdentifier, final String publicIdentifier, final String declarationBaseUri) {
        throw new XmlBuilderException("not implemented");
    }

    public String getBaseUri() {
        throw new XmlBuilderException("not implemented");
    }

    public Iterable notations() {
        throw new XmlBuilderException("not implemented");
    }

    public XmlElement addDocumentElement(final String name) {
        return this.addDocumentElement(null, name);
    }

    public XmlElement addDocumentElement(final XmlNamespace namespace, final String name) {
        final XmlElement el = new XmlElementImpl(namespace, name);
        if (this.getDocumentElement() != null) {
            throw new XmlBuilderException("document already has root element");
        }
        this.setDocumentElement(el);
        return el;
    }

    public boolean isAllDeclarationsProcessed() {
        throw new XmlBuilderException("not implemented");
    }

    public XmlElement getDocumentElement() {
        return this.root;
    }
}
