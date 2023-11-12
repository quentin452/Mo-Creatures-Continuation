package org.xmlpull.v1.builder.adapter;

import java.util.*;
import org.xmlpull.v1.builder.*;
import org.xmlpull.v1.builder.Iterable;

public class XmlElementAdapter implements XmlElement
{
    private XmlElementAdapter topAdapter;
    private XmlElement target;
    private XmlContainer parent;

    public XmlElementAdapter(final XmlElement target) {
        this.setTarget(target);
    }

    private void setTarget(final XmlElement target) {
        this.target = target;
        if (target.getParent() != null) {
            this.parent = target.getParent();
            if (this.parent instanceof XmlDocument) {
                final XmlDocument doc = (XmlDocument)this.parent;
                doc.setDocumentElement(this);
            }
            if (this.parent instanceof XmlElement) {
                final XmlElement parentEl = (XmlElement)this.parent;
                parentEl.replaceChild(this, target);
            }
        }
        final Iterator iter = target.children();
        while (iter.hasNext()) {
            final Object child = iter.next();
            this.fixImportedChildParent(child);
        }
    }

    public Object clone() throws CloneNotSupportedException {
        final XmlElementAdapter ela = (XmlElementAdapter)super.clone();
        ela.parent = null;
        ela.target = (XmlElement)this.target.clone();
        return ela;
    }

    public XmlElement getTarget() {
        return this.target;
    }

    public XmlElementAdapter getTopAdapter() {
        return (this.topAdapter != null) ? this.topAdapter : this;
    }

    public void setTopAdapter(final XmlElementAdapter adapter) {
        this.topAdapter = adapter;
        if (this.target instanceof XmlElementAdapter) {
            ((XmlElementAdapter)this.target).setTopAdapter(adapter);
        }
    }

    public static XmlElementAdapter castOrWrap(final XmlElement el, final Class adapterClass) {
        if (el == null) {
            throw new IllegalArgumentException("null element can not be wrapped");
        }
        if (!XmlElementAdapter.class.isAssignableFrom(adapterClass)) {
            throw new IllegalArgumentException("class for cast/wrap must extend " + XmlElementAdapter.class);
        }
        if (el instanceof XmlElementAdapter) {
            XmlElementAdapter currentAdap = (XmlElementAdapter)el;
            Class currentAdapClass = currentAdap.getClass();
            if (adapterClass.isAssignableFrom(currentAdapClass)) {
                return currentAdap;
            }
            XmlElementAdapter topAdapter;
            for (currentAdap = (topAdapter = currentAdap.getTopAdapter()); currentAdap.topAdapter != null; currentAdap = (XmlElementAdapter)currentAdap.target) {
                currentAdapClass = currentAdap.getClass();
                if (currentAdapClass.isAssignableFrom(adapterClass)) {
                    return currentAdap;
                }
                if (!(currentAdap.target instanceof XmlElementAdapter)) {
                    break;
                }
            }
            try {
                (currentAdap.topAdapter = (XmlElementAdapter) adapterClass.getConstructor(XmlElement.class).newInstance(topAdapter)).setTopAdapter(currentAdap.topAdapter);
                return currentAdap.topAdapter;
            }
            catch (Exception e) {
                throw new XmlBuilderException("could not create wrapper of " + adapterClass, e);
            }
        }
        try {
            final XmlElementAdapter t = (XmlElementAdapter) adapterClass.getConstructor(XmlElement.class).newInstance(el);
            return t;
        }
        catch (Exception e2) {
            throw new XmlBuilderException("could not wrap element " + el, e2);
        }
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

    private XmlElement fixElementParent(final XmlElement el) {
        el.setParent(this);
        return el;
    }

    public XmlContainer getRoot() {
        XmlContainer root = this.target.getRoot();
        if (root == this.target) {
            root = this;
        }
        return root;
    }

    public XmlContainer getParent() {
        return this.parent;
    }

    public void setParent(final XmlContainer parent) {
        this.parent = parent;
    }

    public XmlNamespace newNamespace(final String prefix, final String namespaceName) {
        return this.target.newNamespace(prefix, namespaceName);
    }

    public XmlAttribute attribute(final String attributeName) {
        return this.target.attribute(attributeName);
    }

    public XmlAttribute attribute(final XmlNamespace attributeNamespaceName, final String attributeName) {
        return this.target.attribute(attributeNamespaceName, attributeName);
    }

    public XmlAttribute findAttribute(final String attributeNamespaceName, final String attributeName) {
        return this.target.findAttribute(attributeNamespaceName, attributeName);
    }

    public Iterator attributes() {
        return this.target.attributes();
    }

    public void removeAllChildren() {
        this.target.removeAllChildren();
    }

    public XmlAttribute addAttribute(final String attributeType, final String attributePrefix, final String attributeNamespace, final String attributeName, final String attributeValue, final boolean specified) {
        return this.target.addAttribute(attributeType, attributePrefix, attributeNamespace, attributeName, attributeValue, specified);
    }

    public String getAttributeValue(final String attributeNamespaceName, final String attributeName) {
        return this.target.getAttributeValue(attributeNamespaceName, attributeName);
    }

    public XmlAttribute addAttribute(final XmlNamespace namespace, final String name, final String value) {
        return this.target.addAttribute(namespace, name, value);
    }

    public String getNamespaceName() {
        return this.target.getNamespaceName();
    }

    public void ensureChildrenCapacity(final int minCapacity) {
        this.target.ensureChildrenCapacity(minCapacity);
    }

    public Iterator namespaces() {
        return this.target.namespaces();
    }

    public void removeAllAttributes() {
        this.target.removeAllAttributes();
    }

    public XmlNamespace getNamespace() {
        return this.target.getNamespace();
    }

    public String getBaseUri() {
        return this.target.getBaseUri();
    }

    public void removeAttribute(final XmlAttribute attr) {
        this.target.removeAttribute(attr);
    }

    public XmlNamespace declareNamespace(final String prefix, final String namespaceName) {
        return this.target.declareNamespace(prefix, namespaceName);
    }

    public void removeAllNamespaceDeclarations() {
        this.target.removeAllNamespaceDeclarations();
    }

    public boolean hasAttributes() {
        return this.target.hasAttributes();
    }

    public XmlAttribute addAttribute(final String type, final XmlNamespace namespace, final String name, final String value, final boolean specified) {
        return this.target.addAttribute(type, namespace, name, value, specified);
    }

    public XmlNamespace declareNamespace(final XmlNamespace namespace) {
        return this.target.declareNamespace(namespace);
    }

    public XmlAttribute addAttribute(final String name, final String value) {
        return this.target.addAttribute(name, value);
    }

    public boolean hasNamespaceDeclarations() {
        return this.target.hasNamespaceDeclarations();
    }

    public XmlNamespace lookupNamespaceByName(final String namespaceName) {
        final XmlNamespace ns = this.target.lookupNamespaceByName(namespaceName);
        if (ns == null) {
            final XmlContainer p = this.getParent();
            if (p instanceof XmlElement) {
                final XmlElement e = (XmlElement)p;
                return e.lookupNamespaceByName(namespaceName);
            }
        }
        return ns;
    }

    public XmlNamespace lookupNamespaceByPrefix(final String namespacePrefix) {
        final XmlNamespace ns = this.target.lookupNamespaceByPrefix(namespacePrefix);
        if (ns == null) {
            final XmlContainer p = this.getParent();
            if (p instanceof XmlElement) {
                final XmlElement e = (XmlElement)p;
                return e.lookupNamespaceByPrefix(namespacePrefix);
            }
        }
        return ns;
    }

    public XmlNamespace newNamespace(final String namespaceName) {
        return this.target.newNamespace(namespaceName);
    }

    public void setBaseUri(final String baseUri) {
        this.target.setBaseUri(baseUri);
    }

    public void setNamespace(final XmlNamespace namespace) {
        this.target.setNamespace(namespace);
    }

    public void ensureNamespaceDeclarationsCapacity(final int minCapacity) {
        this.target.ensureNamespaceDeclarationsCapacity(minCapacity);
    }

    public String getName() {
        return this.target.getName();
    }

    public void setName(final String name) {
        this.target.setName(name);
    }

    public XmlAttribute addAttribute(final String type, final XmlNamespace namespace, final String name, final String value) {
        return this.target.addAttribute(type, namespace, name, value);
    }

    public void ensureAttributeCapacity(final int minCapacity) {
        this.target.ensureAttributeCapacity(minCapacity);
    }

    public XmlAttribute addAttribute(final XmlAttribute attributeValueToAdd) {
        return this.target.addAttribute(attributeValueToAdd);
    }

    public XmlElement element(final int position) {
        return this.target.element(position);
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

    public Iterable elements(final XmlNamespace n, final String name) {
        return this.target.elements(n, name);
    }

    public XmlElement findElementByName(final String name, final XmlElement elementToStartLooking) {
        return this.target.findElementByName(name, elementToStartLooking);
    }

    public XmlElement newElement(final XmlNamespace namespace, final String name) {
        return this.target.newElement(namespace, name);
    }

    public XmlElement addElement(final XmlElement child) {
        return this.fixElementParent(this.target.addElement(child));
    }

    public XmlElement addElement(final int pos, final XmlElement child) {
        return this.fixElementParent(this.target.addElement(pos, child));
    }

    public XmlElement addElement(final String name) {
        return this.fixElementParent(this.target.addElement(name));
    }

    public XmlElement findElementByName(final String namespaceName, final String name) {
        return this.target.findElementByName(namespaceName, name);
    }

    public void addChild(final Object child) {
        this.target.addChild(child);
        this.fixImportedChildParent(child);
    }

    public void insertChild(final int pos, final Object childToInsert) {
        this.target.insertChild(pos, childToInsert);
        this.fixImportedChildParent(childToInsert);
    }

    public XmlElement findElementByName(final String name) {
        return this.target.findElementByName(name);
    }

    public XmlElement findElementByName(final String namespaceName, final String name, final XmlElement elementToStartLooking) {
        return this.target.findElementByName(namespaceName, name, elementToStartLooking);
    }

    public void removeChild(final Object child) {
        this.target.removeChild(child);
    }

    public Iterator children() {
        return this.target.children();
    }

    public Iterable requiredElementContent() {
        return this.target.requiredElementContent();
    }

    public String requiredTextContent() {
        return this.target.requiredTextContent();
    }

    public boolean hasChild(final Object child) {
        return this.target.hasChild(child);
    }

    public XmlElement newElement(final String namespaceName, final String name) {
        return this.target.newElement(namespaceName, name);
    }

    public XmlElement addElement(final XmlNamespace namespace, final String name) {
        return this.fixElementParent(this.target.addElement(namespace, name));
    }

    public boolean hasChildren() {
        return this.target.hasChildren();
    }

    public void addChild(final int pos, final Object child) {
        this.target.addChild(pos, child);
        this.fixImportedChildParent(child);
    }

    public void replaceChild(final Object newChild, final Object oldChild) {
        this.target.replaceChild(newChild, oldChild);
        this.fixImportedChildParent(newChild);
    }

    public XmlElement newElement(final String name) {
        return this.target.newElement(name);
    }

    public void replaceChildrenWithText(final String textContent) {
        this.target.replaceChildrenWithText(textContent);
    }
}
