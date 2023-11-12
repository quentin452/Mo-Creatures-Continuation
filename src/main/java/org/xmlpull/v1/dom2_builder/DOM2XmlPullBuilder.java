package org.xmlpull.v1.dom2_builder;

import javax.xml.parsers.*;
import org.xmlpull.v1.*;
import java.io.*;
import org.w3c.dom.*;

public class DOM2XmlPullBuilder
{
    protected Document newDoc() throws XmlPullParserException {
        try {
            final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = domFactory.newDocumentBuilder();
            final DOMImplementation impl = builder.getDOMImplementation();
            return builder.newDocument();
        }
        catch (FactoryConfigurationError ex) {
            throw new XmlPullParserException("could not configure factory JAXP DocumentBuilderFactory: " + ex, null, ex);
        }
        catch (ParserConfigurationException ex2) {
            throw new XmlPullParserException("could not configure parser JAXP DocumentBuilderFactory: " + ex2, null, ex2);
        }
    }
    
    protected XmlPullParser newParser() throws XmlPullParserException {
        final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        return factory.newPullParser();
    }
    
    public Element parse(final Reader reader) throws XmlPullParserException, IOException {
        final Document docFactory = this.newDoc();
        return this.parse(reader, docFactory);
    }
    
    public Element parse(final Reader reader, final Document docFactory) throws XmlPullParserException, IOException {
        final XmlPullParser pp = this.newParser();
        pp.setFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces", true);
        pp.setInput(reader);
        pp.next();
        return this.parse(pp, docFactory);
    }
    
    public Element parse(final XmlPullParser pp, final Document docFactory) throws XmlPullParserException, IOException {
        final Element root = this.parseSubTree(pp, docFactory);
        return root;
    }
    
    public Element parseSubTree(final XmlPullParser pp) throws XmlPullParserException, IOException {
        final Document doc = this.newDoc();
        final Element root = this.parseSubTree(pp, doc);
        return root;
    }
    
    public Element parseSubTree(final XmlPullParser pp, final Document docFactory) throws XmlPullParserException, IOException {
        final BuildProcess process = new BuildProcess();
        return process.parseSubTree(pp, docFactory);
    }
    
    private static void assertEquals(final String expected, final String s) {
        if ((expected != null && !expected.equals(s)) || (expected == null && s == null)) {
            throw new RuntimeException("expected '" + expected + "' but got '" + s + "'");
        }
    }
    
    private static void assertNotNull(final Object o) {
        if (o == null) {
            throw new RuntimeException("expected no null value");
        }
    }
    
    public static void main(final String[] args) throws Exception {
    }
    
    static class BuildProcess
    {
        private XmlPullParser pp;
        private Document docFactory;
        private boolean scanNamespaces;
        
        private BuildProcess() {
            this.scanNamespaces = true;
        }
        
        public Element parseSubTree(final XmlPullParser pp, final Document docFactory) throws XmlPullParserException, IOException {
            this.pp = pp;
            this.docFactory = docFactory;
            return this.parseSubTree();
        }
        
        private Element parseSubTree() throws XmlPullParserException, IOException {
            this.pp.require(2, null, null);
            final String name = this.pp.getName();
            final String ns = this.pp.getNamespace();
            final String prefix = this.pp.getPrefix();
            final String qname = (prefix != null) ? (prefix + ":" + name) : name;
            final Element parent = this.docFactory.createElementNS(ns, qname);
            this.declareNamespaces(this.pp, parent);
            for (int i = 0; i < this.pp.getAttributeCount(); ++i) {
                final String attrNs = this.pp.getAttributeNamespace(i);
                final String attrName = this.pp.getAttributeName(i);
                final String attrValue = this.pp.getAttributeValue(i);
                if (attrNs == null || attrNs.length() == 0) {
                    parent.setAttribute(attrName, attrValue);
                }
                else {
                    final String attrPrefix = this.pp.getAttributePrefix(i);
                    final String attrQname = (attrPrefix != null) ? (attrPrefix + ":" + attrName) : attrName;
                    parent.setAttributeNS(attrNs, attrQname, attrValue);
                }
            }
            while (this.pp.next() != 3) {
                if (this.pp.getEventType() == 2) {
                    final Element el = this.parseSubTree(this.pp, this.docFactory);
                    parent.appendChild(el);
                }
                else {
                    if (this.pp.getEventType() != 4) {
                        throw new XmlPullParserException("unexpected event " + XmlPullParser.TYPES[this.pp.getEventType()], this.pp, null);
                    }
                    final String text = this.pp.getText();
                    final Text textEl = this.docFactory.createTextNode(text);
                    parent.appendChild(textEl);
                }
            }
            this.pp.require(3, ns, name);
            return parent;
        }
        
        private void declareNamespaces(final XmlPullParser pp, final Element parent) throws DOMException, XmlPullParserException {
            if (this.scanNamespaces) {
                this.scanNamespaces = false;
                int i;
                final int top = i = pp.getNamespaceCount(pp.getDepth()) - 1;
            Label_0116_Outer:
                while (i >= pp.getNamespaceCount(0)) {
                    final String prefix = pp.getNamespacePrefix(i);
                    int j = top;
                    while (true) {
                        while (j > i) {
                            final String prefixJ = pp.getNamespacePrefix(j);
                            if (prefix == null || !prefix.equals(prefixJ)) {
                                if (prefix == null || prefix != prefixJ) {
                                    --j;
                                    continue Label_0116_Outer;
                                }
                            }
                            --i;
                            continue Label_0116_Outer;
                        }
                        this.declareOneNamespace(pp, i, parent);
                        continue;
                    }
                }
            }
            else {
                for (int k = pp.getNamespaceCount(pp.getDepth() - 1); k < pp.getNamespaceCount(pp.getDepth()); ++k) {
                    this.declareOneNamespace(pp, k, parent);
                }
            }
        }
        
        private void declareOneNamespace(final XmlPullParser pp, final int i, final Element parent) throws DOMException, XmlPullParserException {
            final String xmlnsPrefix = pp.getNamespacePrefix(i);
            final String xmlnsUri = pp.getNamespaceUri(i);
            final String xmlnsDecl = (xmlnsPrefix != null) ? ("xmlns:" + xmlnsPrefix) : "xmlns";
            parent.setAttributeNS("http://www.w3.org/2000/xmlns/", xmlnsDecl, xmlnsUri);
        }
    }
}
