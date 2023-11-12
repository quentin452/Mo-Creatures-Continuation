package org.xmlpull.v1.builder;

import java.util.*;

public interface XmlElement extends XmlContainer, XmlContained, Cloneable
{
    public static final String NO_NAMESPACE = "";

    Object clone() throws CloneNotSupportedException;

    String getBaseUri();

    void setBaseUri(final String p0);

    XmlContainer getRoot();

    XmlContainer getParent();

    void setParent(final XmlContainer p0);

    XmlNamespace getNamespace();

    String getNamespaceName();

    void setNamespace(final XmlNamespace p0);

    String getName();

    void setName(final String p0);

    Iterator attributes();

    XmlAttribute addAttribute(final XmlAttribute p0);

    XmlAttribute addAttribute(final String p0, final String p1);

    XmlAttribute addAttribute(final XmlNamespace p0, final String p1, final String p2);

    XmlAttribute addAttribute(final String p0, final XmlNamespace p1, final String p2, final String p3);

    XmlAttribute addAttribute(final String p0, final XmlNamespace p1, final String p2, final String p3, final boolean p4);

    XmlAttribute addAttribute(final String p0, final String p1, final String p2, final String p3, final String p4, final boolean p5);

    void ensureAttributeCapacity(final int p0);

    String getAttributeValue(final String p0, final String p1);

    XmlAttribute attribute(final String p0);

    XmlAttribute attribute(final XmlNamespace p0, final String p1);

    XmlAttribute findAttribute(final String p0, final String p1);

    boolean hasAttributes();

    void removeAttribute(final XmlAttribute p0);

    void removeAllAttributes();

    Iterator namespaces();

    XmlNamespace declareNamespace(final String p0, final String p1);

    XmlNamespace declareNamespace(final XmlNamespace p0);

    void ensureNamespaceDeclarationsCapacity(final int p0);

    boolean hasNamespaceDeclarations();

    XmlNamespace lookupNamespaceByPrefix(final String p0);

    XmlNamespace lookupNamespaceByName(final String p0);

    XmlNamespace newNamespace(final String p0);

    XmlNamespace newNamespace(final String p0, final String p1);

    void removeAllNamespaceDeclarations();

    Iterator children();

    void addChild(final Object p0);

    void addChild(final int p0, final Object p1);

    XmlElement addElement(final XmlElement p0);

    XmlElement addElement(final int p0, final XmlElement p1);

    XmlElement addElement(final String p0);

    XmlElement addElement(final XmlNamespace p0, final String p1);

    boolean hasChildren();

    boolean hasChild(final Object p0);

    void ensureChildrenCapacity(final int p0);

    XmlElement findElementByName(final String p0);

    XmlElement findElementByName(final String p0, final String p1);

    XmlElement findElementByName(final String p0, final XmlElement p1);

    XmlElement findElementByName(final String p0, final String p1, final XmlElement p2);

    XmlElement element(final int p0);

    XmlElement requiredElement(final XmlNamespace p0, final String p1) throws XmlBuilderException;

    XmlElement element(final XmlNamespace p0, final String p1);

    XmlElement element(final XmlNamespace p0, final String p1, final boolean p2);

    Iterable elements(final XmlNamespace p0, final String p1);

    void insertChild(final int p0, final Object p1);

    XmlElement newElement(final String p0);

    XmlElement newElement(final XmlNamespace p0, final String p1);

    XmlElement newElement(final String p0, final String p1);

    void removeAllChildren();

    void removeChild(final Object p0);

    void replaceChild(final Object p0, final Object p1);

    Iterable requiredElementContent();

    String requiredTextContent();

    void replaceChildrenWithText(final String p0);
}
