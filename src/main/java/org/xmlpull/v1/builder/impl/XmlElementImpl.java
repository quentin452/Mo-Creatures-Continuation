//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Administrator\Downloads\Minecraft-Deobfuscator3000-1.2.3\Minecraft-Deobfuscator3000-1.2.3\1.7.10 stable mappings"!

//Decompiled by Procyon!

package org.xmlpull.v1.builder.impl;

import java.util.*;
import org.xmlpull.v1.builder.*;
import org.xmlpull.v1.builder.Iterable;

public class XmlElementImpl implements XmlElement
{
    private XmlContainer parent;
    private XmlNamespace namespace;
    private String name;
    private List attrs;
    private List nsList;
    private List children;
    private static final Iterator EMPTY_ITERATOR;
    private static final Iterable EMPTY_ITERABLE;

    public Object clone() throws CloneNotSupportedException {
        final XmlElementImpl cloned = (XmlElementImpl)super.clone();
        cloned.parent = null;
        cloned.attrs = this.cloneList(cloned, this.attrs);
        cloned.nsList = this.cloneList(cloned, this.nsList);
        cloned.children = this.cloneList(cloned, this.children);
        if (cloned.children != null) {
            for (int i = 0; i < cloned.children.size(); ++i) {
                final Object member = cloned.children.get(i);
                if (member instanceof XmlContained) {
                    final XmlContained contained = (XmlContained)member;
                    if (contained.getParent() == this) {
                        contained.setParent(null);
                        contained.setParent(cloned);
                    }
                }
            }
        }
        return cloned;
    }

    private List cloneList(final XmlElementImpl cloned, final List list) throws CloneNotSupportedException {
        if (list == null) {
            return null;
        }
        final List newList = new ArrayList(list.size());
        for (int i = 0; i < list.size(); ++i) {
            final Object member = list.get(i);
            Object newMember = null;
            Label_0230: {
                if (member instanceof XmlNamespace || member instanceof String) {
                    newMember = member;
                }
                else if (member instanceof XmlElement) {
                    final XmlElement el = (XmlElement)member;
                    newMember = el.clone();
                }
                else {
                    if (!(member instanceof XmlAttribute)) {
                        if (member instanceof Cloneable) {
                            try {
                                newMember = member.getClass().getMethod("clone", (Class<?>[])null).invoke(member, (Object[])null);
                                break Label_0230;
                            }
                            catch (Exception e) {
                                throw new CloneNotSupportedException("failed to call clone() on  " + member + e);
                            }
                        }
                        throw new CloneNotSupportedException();
                    }
                    final XmlAttribute attr = (XmlAttribute)member;
                    newMember = new XmlAttributeImpl((XmlElement)cloned, attr.getType(), attr.getNamespace(), attr.getName(), attr.getValue(), attr.isSpecified());
                }
            }
            newList.add(newMember);
        }
        return newList;
    }

    XmlElementImpl(final String name) {
        this.name = name;
    }

    XmlElementImpl(final XmlNamespace namespace, final String name) {
        this.namespace = namespace;
        this.name = name;
    }

    XmlElementImpl(final String namespaceName, final String name) {
        if (namespaceName != null) {
            this.namespace = new XmlNamespaceImpl(null, namespaceName);
        }
        this.name = name;
    }

    public XmlContainer getRoot() {
        XmlContainer root;
        XmlElement el;
        for (root = this; root instanceof XmlElement; root = el.getParent()) {
            el = (XmlElement)root;
            if (el.getParent() == null) {
                return root;
            }
        }
        return root;
    }

    public XmlContainer getParent() {
        return this.parent;
    }

    public void setParent(final XmlContainer parent) {
        if (parent != null && parent instanceof XmlDocument) {
            final XmlDocument doc = (XmlDocument)parent;
            if (doc.getDocumentElement() != this) {
                throw new XmlBuilderException("this element must be root document element to have document set as parent but already different element is set as root document element");
            }
        }
        this.parent = parent;
    }

    public XmlNamespace getNamespace() {
        return this.namespace;
    }

    public String getNamespaceName() {
        return (this.namespace != null) ? this.namespace.getNamespaceName() : null;
    }

    public void setNamespace(final XmlNamespace namespace) {
        this.namespace = namespace;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String toString() {
        return "name[" + this.name + "]" + ((this.namespace != null) ? (" namespace[" + this.namespace.getNamespaceName() + "]") : "");
    }

    public String getBaseUri() {
        throw new XmlBuilderException("not implemented");
    }

    public void setBaseUri(final String baseUri) {
        throw new XmlBuilderException("not implemented");
    }

    public Iterator attributes() {
        if (this.attrs == null) {
            return XmlElementImpl.EMPTY_ITERATOR;
        }
        return this.attrs.iterator();
    }

    public XmlAttribute addAttribute(final XmlAttribute attributeValueToAdd) {
        if (this.attrs == null) {
            this.ensureAttributeCapacity(5);
        }
        this.attrs.add(attributeValueToAdd);
        return attributeValueToAdd;
    }

    public XmlAttribute addAttribute(final XmlNamespace namespace, final String name, final String value) {
        return this.addAttribute("CDATA", namespace, name, value, false);
    }

    public XmlAttribute addAttribute(final String name, final String value) {
        return this.addAttribute("CDATA", null, name, value, false);
    }

    public XmlAttribute addAttribute(final String attributeType, final XmlNamespace namespace, final String name, final String value) {
        return this.addAttribute(attributeType, namespace, name, value, false);
    }

    public XmlAttribute addAttribute(final String attributeType, final XmlNamespace namespace, final String name, final String value, final boolean specified) {
        final XmlAttribute a = (XmlAttribute)new XmlAttributeImpl((XmlElement)this, attributeType, namespace, name, value, specified);
        return this.addAttribute(a);
    }

    public XmlAttribute addAttribute(final String attributeType, final String attributePrefix, final String attributeNamespace, final String attributeName, final String attributeValue, final boolean specified) {
        final XmlNamespace n = this.newNamespace(attributePrefix, attributeNamespace);
        return this.addAttribute(attributeType, n, attributeName, attributeValue, specified);
    }

    public void ensureAttributeCapacity(final int minCapacity) {
        if (this.attrs == null) {
            this.attrs = new ArrayList(minCapacity);
        }
        else {
            ((ArrayList)this.attrs).ensureCapacity(minCapacity);
        }
    }

    public String getAttributeValue(final String attributeNamespaceName, final String attributeName) {
        final XmlAttribute xat = this.findAttribute(attributeNamespaceName, attributeName);
        if (xat != null) {
            return xat.getValue();
        }
        return null;
    }

    public boolean hasAttributes() {
        return this.attrs != null && this.attrs.size() > 0;
    }

    public XmlAttribute attribute(final String attributeName) {
        return this.attribute(null, attributeName);
    }

    public XmlAttribute attribute(final XmlNamespace attributeNamespace, final String attributeName) {
        return this.findAttribute((attributeNamespace != null) ? attributeNamespace.getNamespaceName() : null, attributeName);
    }

    public XmlAttribute findAttribute(final String attributeNamespace, final String attributeName) {
        if (attributeName == null) {
            throw new IllegalArgumentException("attribute name ca not ber null");
        }
        if (this.attrs == null) {
            return null;
        }
        for (int length = this.attrs.size(), i = 0; i < length; ++i) {
            final XmlAttribute a = (XmlAttribute) this.attrs.get(i);
            final String aName = a.getName();
            if (aName == attributeName || attributeName.equals(aName)) {
                if (attributeNamespace != null) {
                    final String aNamespace = a.getNamespaceName();
                    if (attributeNamespace.equals(aNamespace)) {
                        return a;
                    }
                    if (attributeNamespace == "" && aNamespace == null) {
                        return a;
                    }
                }
                else {
                    if (a.getNamespace() == null) {
                        return a;
                    }
                    if (a.getNamespace().getNamespaceName() == "") {
                        return a;
                    }
                }
            }
        }
        return null;
    }

    public void removeAllAttributes() {
        this.attrs = null;
    }

    public void removeAttribute(final XmlAttribute attr) {
        if (this.attrs == null) {
            throw new XmlBuilderException("this element has no attributes to remove");
        }
        for (int i = 0; i < this.attrs.size(); ++i) {
            if (this.attrs.get(i).equals(attr)) {
                this.attrs.remove(i);
                break;
            }
        }
    }

    public XmlNamespace declareNamespace(final String prefix, final String namespaceName) {
        if (prefix == null) {
            throw new XmlBuilderException("namespace added to element must have not null prefix");
        }
        final XmlNamespace n = this.newNamespace(prefix, namespaceName);
        return this.declareNamespace(n);
    }

    public XmlNamespace declareNamespace(final XmlNamespace n) {
        if (n.getPrefix() == null) {
            throw new XmlBuilderException("namespace added to element must have not null prefix");
        }
        if (this.nsList == null) {
            this.ensureNamespaceDeclarationsCapacity(5);
        }
        this.nsList.add(n);
        return n;
    }

    public boolean hasNamespaceDeclarations() {
        return this.nsList != null && this.nsList.size() > 0;
    }

    public XmlNamespace lookupNamespaceByPrefix(final String namespacePrefix) {
        if (namespacePrefix == null) {
            throw new IllegalArgumentException("namespace prefix can not be null");
        }
        if (this.hasNamespaceDeclarations()) {
            for (int length = this.nsList.size(), i = 0; i < length; ++i) {
                final XmlNamespace n = (XmlNamespace) this.nsList.get(i);
                if (namespacePrefix.equals(n.getPrefix())) {
                    return n;
                }
            }
        }
        if (this.parent != null && this.parent instanceof XmlElement) {
            return ((XmlElement)this.parent).lookupNamespaceByPrefix(namespacePrefix);
        }
        return null;
    }

    public XmlNamespace lookupNamespaceByName(final String namespaceName) {
        if (namespaceName == null) {
            throw new IllegalArgumentException("namespace name can not ber null");
        }
        if (this.hasNamespaceDeclarations()) {
            for (int length = this.nsList.size(), i = 0; i < length; ++i) {
                final XmlNamespace n = (XmlNamespace) this.nsList.get(i);
                if (namespaceName.equals(n.getNamespaceName())) {
                    return n;
                }
            }
        }
        if (this.parent != null && this.parent instanceof XmlElement) {
            return ((XmlElement)this.parent).lookupNamespaceByName(namespaceName);
        }
        return null;
    }

    public Iterator namespaces() {
        if (this.nsList == null) {
            return XmlElementImpl.EMPTY_ITERATOR;
        }
        return this.nsList.iterator();
    }

    public XmlNamespace newNamespace(final String namespaceName) {
        return this.newNamespace(null, namespaceName);
    }

    public XmlNamespace newNamespace(final String prefix, final String namespaceName) {
        return new XmlNamespaceImpl(prefix, namespaceName);
    }

    public void ensureNamespaceDeclarationsCapacity(final int minCapacity) {
        if (this.nsList == null) {
            this.nsList = new ArrayList(minCapacity);
        }
        else {
            ((ArrayList)this.nsList).ensureCapacity(minCapacity);
        }
    }

    public void removeAllNamespaceDeclarations() {
        this.nsList = null;
    }

    public void addChild(final Object child) {
        if (child == null) {
            throw new NullPointerException();
        }
        if (this.children == null) {
            this.ensureChildrenCapacity(1);
        }
        this.children.add(child);
    }

    public void addChild(final int index, final Object child) {
        if (this.children == null) {
            this.ensureChildrenCapacity(1);
        }
        this.children.add(index, child);
    }

    private void checkChildParent(final Object child) {
        if (child instanceof XmlContainer) {
            if (child instanceof XmlElement) {
                final XmlElement elChild = (XmlElement)child;
                final XmlContainer childParent = elChild.getParent();
                if (childParent != null && childParent != this.parent) {
                    throw new XmlBuilderException("child must have no parent to be added to this node");
                }
            }
            else if (child instanceof XmlDocument) {
                throw new XmlBuilderException("docuemet can not be stored as element child");
            }
        }
    }

    private void setChildParent(final Object child) {
        if (child instanceof XmlElement) {
            final XmlElement elChild = (XmlElement)child;
            elChild.setParent(this);
        }
    }

    public XmlElement addElement(final XmlElement element) {
        this.checkChildParent(element);
        this.addChild(element);
        this.setChildParent(element);
        return element;
    }

    public XmlElement addElement(final int pos, final XmlElement element) {
        this.checkChildParent(element);
        this.addChild(pos, element);
        this.setChildParent(element);
        return element;
    }

    public XmlElement addElement(final XmlNamespace namespace, final String name) {
        final XmlElement el = this.newElement(namespace, name);
        this.addChild(el);
        this.setChildParent(el);
        return el;
    }

    public XmlElement addElement(final String name) {
        return this.addElement(null, name);
    }

    public Iterator children() {
        if (this.children == null) {
            return XmlElementImpl.EMPTY_ITERATOR;
        }
        return this.children.iterator();
    }

    public Iterable requiredElementContent() {
        if (this.children == null) {
            return XmlElementImpl.EMPTY_ITERABLE;
        }
        return new Iterable() {
            public Iterator iterator() {
                return new RequiredElementContentIterator(XmlElementImpl.this.children.iterator());
            }
        };
    }

    public String requiredTextContent() {
        if (this.children == null) {
            return "";
        }
        if (this.children.size() == 0) {
            return "";
        }
        if (this.children.size() != 1) {
            final Iterator i = this.children();
            final StringBuffer buf = new StringBuffer();
            while (i.hasNext()) {
                final Object child = i.next();
                if (child instanceof String) {
                    buf.append(child.toString());
                }
                else {
                    if (!(child instanceof XmlCharacters)) {
                        throw new XmlBuilderException("expected text content and not " + child.getClass() + " with '" + child + "'");
                    }
                    buf.append(((XmlCharacters)child).getText());
                }
            }
            return buf.toString();
        }
        final Object child2 = this.children.get(0);
        if (child2 instanceof String) {
            return child2.toString();
        }
        if (child2 instanceof XmlCharacters) {
            return ((XmlCharacters)child2).getText();
        }
        throw new XmlBuilderException("expected text content and not " + ((child2 != null) ? child2.getClass() : null) + " with '" + child2 + "'");
    }

    public void ensureChildrenCapacity(final int minCapacity) {
        if (this.children == null) {
            this.children = new ArrayList(minCapacity);
        }
        else {
            ((ArrayList)this.children).ensureCapacity(minCapacity);
        }
    }

    public XmlElement element(final int position) {
        if (this.children == null) {
            return null;
        }
        final int length = this.children.size();
        int count = 0;
        if (position >= 0 && position < length + 1) {
            for (int pos = 0; pos < length; ++pos) {
                final Object child = this.children.get(pos);
                if (child instanceof XmlElement && ++count == position) {
                    return (XmlElement)child;
                }
            }
            throw new IndexOutOfBoundsException("position " + position + " too big as only " + count + " element(s) available");
        }
        throw new IndexOutOfBoundsException("position " + position + " bigger or equal to " + length + " children");
    }

    public XmlElement requiredElement(final XmlNamespace n, final String name) throws XmlBuilderException {
        final XmlElement el = this.element(n, name);
        if (el == null) {
            throw new XmlBuilderException("could not find element with name " + name + " in namespace " + ((n != null) ? n.getNamespaceName() : null));
        }
        return el;
    }

    public XmlElement element(final XmlNamespace n, final String name) {
        return this.element(n, name, false);
    }

    public XmlElement element(final XmlNamespace n, final String name, final boolean create) {
        final XmlElement e = (n != null) ? this.findElementByName(n.getNamespaceName(), name) : this.findElementByName(name);
        if (e != null) {
            return e;
        }
        if (create) {
            return this.addElement(n, name);
        }
        return null;
    }

    public Iterable elements(final XmlNamespace n, final String name) {
        return new Iterable() {
            public Iterator iterator() {
                return new ElementsSimpleIterator(n, name, XmlElementImpl.this.children());
            }
        };
    }

    public XmlElement findElementByName(final String name) {
        if (this.children == null) {
            return null;
        }
        for (int length = this.children.size(), i = 0; i < length; ++i) {
            final Object child = this.children.get(i);
            if (child instanceof XmlElement) {
                final XmlElement childEl = (XmlElement)child;
                if (name.equals(childEl.getName())) {
                    return childEl;
                }
            }
        }
        return null;
    }

    public XmlElement findElementByName(final String namespaceName, final String name, final XmlElement elementToStartLooking) {
        throw new UnsupportedOperationException();
    }

    public XmlElement findElementByName(final String name, final XmlElement elementToStartLooking) {
        throw new UnsupportedOperationException();
    }

    public XmlElement findElementByName(final String namespaceName, final String name) {
        if (this.children == null) {
            return null;
        }
        for (int length = this.children.size(), i = 0; i < length; ++i) {
            final Object child = this.children.get(i);
            if (child instanceof XmlElement) {
                final XmlElement childEl = (XmlElement)child;
                final XmlNamespace namespace = childEl.getNamespace();
                if (namespace != null) {
                    if (name.equals(childEl.getName()) && namespaceName.equals(namespace.getNamespaceName())) {
                        return childEl;
                    }
                }
                else if (name.equals(childEl.getName()) && namespaceName == null) {
                    return childEl;
                }
            }
        }
        return null;
    }

    public boolean hasChild(final Object child) {
        if (this.children == null) {
            return false;
        }
        for (int i = 0; i < this.children.size(); ++i) {
            if (this.children.get(i) == child) {
                return true;
            }
        }
        return false;
    }

    public boolean hasChildren() {
        return this.children != null && this.children.size() > 0;
    }

    public void insertChild(final int pos, final Object childToInsert) {
        if (this.children == null) {
            this.ensureChildrenCapacity(1);
        }
        this.children.add(pos, childToInsert);
    }

    public XmlElement newElement(final String name) {
        return this.newElement((XmlNamespace)null, name);
    }

    public XmlElement newElement(final String namespace, final String name) {
        return new XmlElementImpl(namespace, name);
    }

    public XmlElement newElement(final XmlNamespace namespace, final String name) {
        return new XmlElementImpl(namespace, name);
    }

    public void replaceChild(final Object newChild, final Object oldChild) {
        if (newChild == null) {
            throw new IllegalArgumentException("new child to replace can not be null");
        }
        if (oldChild == null) {
            throw new IllegalArgumentException("old child to replace can not be null");
        }
        if (!this.hasChildren()) {
            throw new XmlBuilderException("no children available for replacement");
        }
        final int pos = this.children.indexOf(oldChild);
        if (pos == -1) {
            throw new XmlBuilderException("could not find child to replace");
        }
        this.children.set(pos, newChild);
    }

    public void removeAllChildren() {
        this.children = null;
    }

    public void removeChild(final Object child) {
        if (child == null) {
            throw new IllegalArgumentException("child to remove can not be null");
        }
        if (!this.hasChildren()) {
            throw new XmlBuilderException("no children to remove");
        }
        final int pos = this.children.indexOf(child);
        if (pos != -1) {
            this.children.remove(pos);
        }
    }

    public void replaceChildrenWithText(final String textContent) {
        this.removeAllChildren();
        this.addChild(textContent);
    }

    private static final boolean isWhiteSpace(final String txt) {
        for (int i = 0; i < txt.length(); ++i) {
            if (txt.charAt(i) != ' ' && txt.charAt(i) != '\n' && txt.charAt(i) != '\t' && txt.charAt(i) != '\r') {
                return false;
            }
        }
        return true;
    }

    static {
        EMPTY_ITERATOR = new EmptyIterator();
        EMPTY_ITERABLE = new Iterable() {
            public Iterator iterator() {
                return XmlElementImpl.EMPTY_ITERATOR;
            }
        };
    }

    private class ElementsSimpleIterator implements Iterator
    {
        private Iterator children;
        private XmlElement currentEl;
        private XmlNamespace n;
        private String name;

        ElementsSimpleIterator(final XmlNamespace n, final String name, final Iterator children) {
            this.children = children;
            this.n = n;
            this.name = name;
            this.findNextEl();
        }

        private void findNextEl() {
            this.currentEl = null;
            while (this.children.hasNext()) {
                final Object child = this.children.next();
                if (child instanceof XmlElement) {
                    final XmlElement el = (XmlElement)child;
                    if ((this.name == null || el.getName() == this.name || this.name.equals(el.getName())) && (this.n == null || el.getNamespace() == this.n || this.n.equals(el.getNamespace()))) {
                        this.currentEl = el;
                        break;
                    }
                    continue;
                }
            }
        }

        public boolean hasNext() {
            return this.currentEl != null;
        }

        public Object next() {
            if (this.currentEl == null) {
                throw new XmlBuilderException("this iterator has no content and next() is not allowed");
            }
            final XmlElement el = this.currentEl;
            this.findNextEl();
            return el;
        }

        public void remove() {
            throw new XmlBuilderException("this element iterator does nto support remove()");
        }
    }

    private static class RequiredElementContentIterator implements Iterator
    {
        private Iterator children;
        private XmlElement currentEl;

        RequiredElementContentIterator(final Iterator children) {
            this.children = children;
            this.findNextEl();
        }

        private void findNextEl() {
            this.currentEl = null;
            while (this.children.hasNext()) {
                final Object child = this.children.next();
                if (child instanceof XmlElement) {
                    this.currentEl = (XmlElement)child;
                    break;
                }
                if (child instanceof String) {
                    final String s = child.toString();
                    if (!isWhiteSpace(s)) {
                        throw new XmlBuilderException("only whitespace string children allowed for non mixed element content");
                    }
                    continue;
                }
                else {
                    if (!(child instanceof XmlCharacters)) {
                        throw new XmlBuilderException("only whitespace characters and element children allowed for non mixed element content and not " + child.getClass());
                    }
                    final XmlCharacters xc = (XmlCharacters)child;
                    if (!Boolean.TRUE.equals(xc.isWhitespaceContent()) || !isWhiteSpace(xc.getText())) {
                        throw new XmlBuilderException("only whitespace characters children allowed for non mixed element content");
                    }
                    continue;
                }
            }
        }

        public boolean hasNext() {
            return this.currentEl != null;
        }

        public Object next() {
            if (this.currentEl == null) {
                throw new XmlBuilderException("this iterator has no content and next() is not allowed");
            }
            final XmlElement el = this.currentEl;
            this.findNextEl();
            return el;
        }

        public void remove() {
            throw new XmlBuilderException("this iterator does nto support remove()");
        }
    }

    private static class EmptyIterator implements Iterator
    {
        public boolean hasNext() {
            return false;
        }

        public Object next() {
            throw new XmlBuilderException("this iterator has no content and next() is not allowed");
        }

        public void remove() {
            throw new XmlBuilderException("this iterator has no content and remove() is not allowed");
        }
    }
}
