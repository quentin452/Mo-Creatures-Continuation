//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Administrator\Downloads\Minecraft-Deobfuscator3000-1.2.3\Minecraft-Deobfuscator3000-1.2.3\1.7.10 stable mappings"!

//Decompiled by Procyon!

package org.xmlpull.v1.builder.impl;

import java.io.*;
import java.net.*;
import org.xmlpull.v1.*;
import java.util.*;
import org.xmlpull.v1.builder.*;

public class XmlInfosetBuilderImpl extends XmlInfosetBuilder
{
    private static final String PROPERTY_XMLDECL_STANDALONE = "http://xmlpull.org/v1/doc/properties.html#xmldecl-standalone";
    private static final String PROPERTY_XMLDECL_VERSION = "http://xmlpull.org/v1/doc/properties.html#xmldecl-version";

    public XmlDocument newDocument(final String version, final Boolean standalone, final String characterEncoding) {
        return (XmlDocument)new XmlDocumentImpl(version, standalone, characterEncoding);
    }

    public XmlElement newFragment(final String elementName) {
        return (XmlElement)new XmlElementImpl((XmlNamespace)null, elementName);
    }

    public XmlElement newFragment(final String elementNamespaceName, final String elementName) {
        return (XmlElement)new XmlElementImpl(elementNamespaceName, elementName);
    }

    public XmlElement newFragment(final XmlNamespace elementNamespace, final String elementName) {
        return (XmlElement)new XmlElementImpl(elementNamespace, elementName);
    }

    public XmlNamespace newNamespace(final String namespaceName) {
        return new XmlNamespaceImpl(null, namespaceName);
    }

    public XmlNamespace newNamespace(final String prefix, final String namespaceName) {
        return new XmlNamespaceImpl(prefix, namespaceName);
    }

    public XmlDocument parse(final XmlPullParser pp) {
        final XmlDocument doc = this.parseDocumentStart(pp);
        final XmlElement root = this.parseFragment(pp);
        doc.setDocumentElement(root);
        return doc;
    }

    public Object parseItem(final XmlPullParser pp) {
        try {
            final int eventType = pp.getEventType();
            if (eventType == 2) {
                return this.parseStartTag(pp);
            }
            if (eventType == 4) {
                return pp.getText();
            }
            if (eventType == 0) {
                return this.parseDocumentStart(pp);
            }
            throw new XmlBuilderException("currently unsupported event type " + XmlPullParser.TYPES[eventType] + pp.getPositionDescription());
        }
        catch (XmlPullParserException e) {
            throw new XmlBuilderException("could not parse XML item", e);
        }
    }

    private XmlDocument parseDocumentStart(final XmlPullParser pp) {
        XmlDocument doc = null;
        try {
            if (pp.getEventType() != 0) {
                throw new XmlBuilderException("parser must be positioned on beginning of document and not " + pp.getPositionDescription());
            }
            pp.next();
            final String xmlDeclVersion = (String)pp.getProperty("http://xmlpull.org/v1/doc/properties.html#xmldecl-version");
            final Boolean xmlDeclStandalone = (Boolean)pp.getProperty("http://xmlpull.org/v1/doc/properties.html#xmldecl-standalone");
            final String characterEncoding = pp.getInputEncoding();
            doc = (XmlDocument)new XmlDocumentImpl(xmlDeclVersion, xmlDeclStandalone, characterEncoding);
        }
        catch (XmlPullParserException e) {
            throw new XmlBuilderException("could not parse XML document prolog", e);
        }
        catch (IOException e2) {
            throw new XmlBuilderException("could not read XML document prolog", e2);
        }
        return doc;
    }

    public XmlElement parseFragment(final XmlPullParser pp) {
        try {
            final int depth = pp.getDepth();
            int eventType = pp.getEventType();
            if (eventType != 2) {
                throw new XmlBuilderException("expected parser to be on start tag and not " + XmlPullParser.TYPES[eventType] + pp.getPositionDescription());
            }
            XmlElement curElem = this.parseStartTag(pp);
            while (true) {
                eventType = pp.next();
                if (eventType == 2) {
                    final XmlElement child = this.parseStartTag(pp);
                    curElem.addElement(child);
                    curElem = child;
                }
                else if (eventType == 3) {
                    final XmlContainer parent = curElem.getParent();
                    if (parent == null) {
                        break;
                    }
                    curElem = (XmlElement)parent;
                }
                else {
                    if (eventType != 4) {
                        continue;
                    }
                    curElem.addChild(pp.getText());
                }
            }
            if (pp.getDepth() != depth) {
                throw new XmlBuilderException("unbalanced input" + pp.getPositionDescription());
            }
            return curElem;
        }
        catch (XmlPullParserException e) {
            throw new XmlBuilderException("could not build tree from XML", e);
        }
        catch (IOException e2) {
            throw new XmlBuilderException("could not read XML tree content", e2);
        }
    }

    public XmlElement parseStartTag(final XmlPullParser pp) {
        try {
            if (pp.getEventType() != 2) {
                throw new XmlBuilderException("parser must be on START_TAG and not " + pp.getPositionDescription());
            }
            final String elNsPrefix = pp.getPrefix();
            final XmlNamespace elementNs = new XmlNamespaceImpl(elNsPrefix, pp.getNamespace());
            final XmlElement el = (XmlElement)new XmlElementImpl(elementNs, pp.getName());
            for (int i = pp.getNamespaceCount(pp.getDepth() - 1); i < pp.getNamespaceCount(pp.getDepth()); ++i) {
                final String prefix = pp.getNamespacePrefix(i);
                el.declareNamespace((prefix == null) ? "" : prefix, pp.getNamespaceUri(i));
            }
            for (int i = 0; i < pp.getAttributeCount(); ++i) {
                el.addAttribute(pp.getAttributeType(i), pp.getAttributePrefix(i), pp.getAttributeNamespace(i), pp.getAttributeName(i), pp.getAttributeValue(i), !pp.isAttributeDefault(i));
            }
            return el;
        }
        catch (XmlPullParserException e) {
            throw new XmlBuilderException("could not parse XML start tag", e);
        }
    }

    public XmlDocument parseLocation(final String locationUrl) {
        URL url = null;
        try {
            url = new URL(locationUrl);
        }
        catch (MalformedURLException e) {
            throw new XmlBuilderException("could not parse URL " + locationUrl, e);
        }
        try {
            return this.parseInputStream(url.openStream());
        }
        catch (IOException e2) {
            throw new XmlBuilderException("could not open connection to URL " + locationUrl, e2);
        }
    }

    public void serialize(final Object item, final XmlSerializer serializer) {
        if (item instanceof Collection) {
            final Collection c = (Collection)item;
            final Iterator i = c.iterator();
            while (i.hasNext()) {
                this.serialize(i.next(), serializer);
            }
        }
        else if (item instanceof XmlContainer) {
            this.serializeContainer((XmlContainer)item, serializer);
        }
        else {
            this.serializeItem(item, serializer);
        }
    }

    private void serializeContainer(final XmlContainer node, final XmlSerializer serializer) {
        if (node instanceof XmlSerializable) {
            try {
                ((XmlSerializable)node).serialize(serializer);
                return;
            }
            catch (IOException e) {
                throw new XmlBuilderException("could not serialize node " + node + ": " + e, e);
            }
        }
        if (node instanceof XmlDocument) {
            this.serializeDocument((XmlDocument)node, serializer);
        }
        else {
            if (!(node instanceof XmlElement)) {
                throw new IllegalArgumentException("could not serialzie unknown XML container " + node.getClass());
            }
            this.serializeFragment((XmlElement)node, serializer);
        }
    }

    public void serializeItem(final Object item, final XmlSerializer ser) {
        try {
            if (item instanceof XmlSerializable) {
                try {
                    ((XmlSerializable)item).serialize(ser);
                    return;
                }
                catch (IOException e) {
                    throw new XmlBuilderException("could not serialize item " + item + ": " + e, e);
                }
            }
            if (item instanceof String) {
                ser.text(item.toString());
            }
            else if (item instanceof XmlCharacters) {
                ser.text(((XmlCharacters)item).getText());
            }
            else {
                if (!(item instanceof XmlComment)) {
                    throw new IllegalArgumentException("could not serialize " + ((item != null) ? item.getClass() : item));
                }
                ser.comment(((XmlComment)item).getContent());
            }
        }
        catch (IOException e) {
            throw new XmlBuilderException("serializing XML start tag failed", e);
        }
    }

    public void serializeStartTag(final XmlElement el, final XmlSerializer ser) {
        try {
            final XmlNamespace elNamespace = el.getNamespace();
            String elPrefix = (elNamespace != null) ? elNamespace.getPrefix() : "";
            if (elPrefix == null) {
                elPrefix = "";
            }
            String nToDeclare = null;
            if (el.hasNamespaceDeclarations()) {
                final Iterator iter = el.namespaces();
                while (iter.hasNext()) {
                    final XmlNamespace n = (XmlNamespace) iter.next();
                    final String nPrefix = n.getPrefix();
                    if (!elPrefix.equals(nPrefix)) {
                        ser.setPrefix(nPrefix, n.getNamespaceName());
                    }
                    else {
                        nToDeclare = n.getNamespaceName();
                    }
                }
            }
            if (nToDeclare != null) {
                ser.setPrefix(elPrefix, nToDeclare);
            }
            else if (elNamespace != null) {
                String namespaceName = elNamespace.getNamespaceName();
                if (namespaceName == null) {
                    namespaceName = "";
                }
                String serPrefix = null;
                if (namespaceName.length() > 0) {
                    ser.getPrefix(namespaceName, false);
                }
                if (serPrefix == null) {
                    serPrefix = "";
                }
                if (serPrefix != elPrefix && !serPrefix.equals(elPrefix)) {
                    ser.setPrefix(elPrefix, namespaceName);
                }
            }
            ser.startTag(el.getNamespaceName(), el.getName());
            if (el.hasAttributes()) {
                final Iterator iter = el.attributes();
                while (iter.hasNext()) {
                    final XmlAttribute a = (XmlAttribute) iter.next();
                    if (a instanceof XmlSerializable) {
                        ((XmlSerializable)a).serialize(ser);
                    }
                    else {
                        ser.attribute(a.getNamespaceName(), a.getName(), a.getValue());
                    }
                }
            }
        }
        catch (IOException e) {
            throw new XmlBuilderException("serializing XML start tag failed", e);
        }
    }

    public void serializeEndTag(final XmlElement el, final XmlSerializer ser) {
        try {
            ser.endTag(el.getNamespaceName(), el.getName());
        }
        catch (IOException e) {
            throw new XmlBuilderException("serializing XML end tag failed", e);
        }
    }

    private void serializeDocument(final XmlDocument doc, final XmlSerializer ser) {
        try {
            ser.startDocument(doc.getCharacterEncodingScheme(), doc.isStandalone());
        }
        catch (IOException e) {
            throw new XmlBuilderException("serializing XML document start failed", e);
        }
        if (doc.getDocumentElement() != null) {
            this.serializeFragment(doc.getDocumentElement(), ser);
            try {
                ser.endDocument();
            }
            catch (IOException e) {
                throw new XmlBuilderException("serializing XML document end failed", e);
            }
            return;
        }
        throw new XmlBuilderException("could not serialize document without root element " + doc + ": ");
    }

    private void serializeFragment(final XmlElement el, final XmlSerializer ser) {
        this.serializeStartTag(el, ser);
        if (el.hasChildren()) {
            final Iterator iter = el.children();
            while (iter.hasNext()) {
                final Object child = iter.next();
                if (child instanceof XmlSerializable) {
                    try {
                        ((XmlSerializable)child).serialize(ser);
                        continue;
                    }
                    catch (IOException e) {
                        throw new XmlBuilderException("could not serialize item " + child + ": " + e, e);
                    }
                }
                if (child instanceof XmlElement) {
                    this.serializeFragment((XmlElement)child, ser);
                }
                else {
                    this.serializeItem(child, ser);
                }
            }
        }
        this.serializeEndTag(el, ser);
    }
}
