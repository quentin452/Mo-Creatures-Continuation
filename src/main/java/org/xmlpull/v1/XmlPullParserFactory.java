//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Administrator\Downloads\Minecraft-Deobfuscator3000-1.2.3\Minecraft-Deobfuscator3000-1.2.3\1.7.10 stable mappings"!

//Decompiled by Procyon!

package org.xmlpull.v1;

import java.util.*;
import java.io.*;

public class XmlPullParserFactory
{
    static final Class referenceContextClass;
    public static final String PROPERTY_NAME = "org.xmlpull.v1.XmlPullParserFactory";
    private static final String RESOURCE_NAME = "/xml-pull/services/org.xmlpull.v1.XmlPullParserFactory";
    protected Vector parserClasses;
    protected String classNamesLocation;
    protected Vector serializerClasses;
    protected Hashtable features;

    protected XmlPullParserFactory() {
        this.features = new Hashtable();
    }

    public void setFeature(final String name, final boolean state) throws XmlPullParserException {
        this.features.put(name, state);
    }

    public boolean getFeature(final String name) {
        final Boolean value = (Boolean) this.features.get(name);
        return value != null && value;
    }

    public void setNamespaceAware(final boolean awareness) {
        this.features.put("http://xmlpull.org/v1/doc/features.html#process-namespaces",awareness);
    }

    public boolean isNamespaceAware() {
        return this.getFeature("http://xmlpull.org/v1/doc/features.html#process-namespaces");
    }

    public void setValidating(final boolean validating) {
        this.features.put("http://xmlpull.org/v1/doc/features.html#validation",validating);
    }

    public boolean isValidating() {
        return this.getFeature("http://xmlpull.org/v1/doc/features.html#validation");
    }

    public XmlPullParser newPullParser() throws XmlPullParserException {
        if (this.parserClasses == null) {
            throw new XmlPullParserException("Factory initialization was incomplete - has not tried " + this.classNamesLocation);
        }
        if (this.parserClasses.size() == 0) {
            throw new XmlPullParserException("No valid parser classes found in " + this.classNamesLocation);
        }
        final StringBuffer issues = new StringBuffer();
        int i = 0;
        while (i < this.parserClasses.size()) {
            final Class ppClass = (Class) this.parserClasses.elementAt(i);
            try {
                final XmlPullParser pp = (XmlPullParser) ppClass.newInstance();
                final Enumeration e = this.features.keys();
                while (e.hasMoreElements()) {
                    final String key = (String) e.nextElement();
                    final Boolean value = (Boolean) this.features.get(key);
                    if (value != null && value) {
                        pp.setFeature(key, true);
                    }
                }
                return pp;
            }
            catch (Exception ex) {
                issues.append(ppClass.getName()).append(": ").append(ex).append("; ");
                ++i;
            }
        }
        throw new XmlPullParserException("could not create parser: " + issues);
    }

    public XmlSerializer newSerializer() throws XmlPullParserException {
        if (this.serializerClasses == null) {
            throw new XmlPullParserException("Factory initialization incomplete - has not tried " + this.classNamesLocation);
        }
        if (this.serializerClasses.size() == 0) {
            throw new XmlPullParserException("No valid serializer classes found in " + this.classNamesLocation);
        }
        final StringBuffer issues = new StringBuffer();
        int i = 0;
        while (i < this.serializerClasses.size()) {
            final Class ppClass = (Class) this.serializerClasses.elementAt(i);
            try {
                final XmlSerializer ser = (XmlSerializer) ppClass.newInstance();
                return ser;
            }
            catch (Exception ex) {
                issues.append(ppClass.getName()).append(": ").append(ex).append("; ");
                ++i;
            }
        }
        throw new XmlPullParserException("could not create serializer: " + issues);
    }

    public static XmlPullParserFactory newInstance() throws XmlPullParserException {
        return newInstance(null, null);
    }

    public static XmlPullParserFactory newInstance(String classNames, Class context) throws XmlPullParserException {
        if (context == null) {
            context = XmlPullParserFactory.referenceContextClass;
        }
        String classNamesLocation;
        Label_0171: {
            if (classNames != null && classNames.length() != 0) {
                if (!"DEFAULT".equals(classNames)) {
                    classNamesLocation = "parameter classNames to newInstance() that contained '" + classNames + "'";
                    break Label_0171;
                }
            }
            try {
                final InputStream is = context.getResourceAsStream("/xml-pull/services/org.xmlpull.v1.XmlPullParserFactory");
                if (is == null) {
                    throw new XmlPullParserException("resource not found: /xml-pull/services/org.xmlpull.v1.XmlPullParserFactory make sure that parser implementing XmlPull API is available");
                }
                final StringBuffer sb = new StringBuffer();
                while (true) {
                    final int ch = is.read();
                    if (ch < 0) {
                        break;
                    }
                    if (ch <= 32) {
                        continue;
                    }
                    sb.append((char)ch);
                }
                is.close();
                classNames = sb.toString();
            }
            catch (Exception e) {
                throw new XmlPullParserException((String)null, (XmlPullParser)null, (Throwable)e);
            }
            classNamesLocation = "resource /xml-pull/services/org.xmlpull.v1.XmlPullParserFactory that contained '" + classNames + "'";
        }
        XmlPullParserFactory factory = null;
        final Vector parserClasses = new Vector();
        final Vector serializerClasses = new Vector();
        int cut;
        for (int pos = 0; pos < classNames.length(); pos = cut + 1) {
            cut = classNames.indexOf(44, pos);
            if (cut == -1) {
                cut = classNames.length();
            }
            final String name = classNames.substring(pos, cut);
            Class candidate = null;
            Object instance = null;
            try {
                candidate = Class.forName(name);
                instance = candidate.newInstance();
            }
            catch (Exception ex) {}
            if (candidate != null) {
                boolean recognized = false;
                if (instance instanceof XmlPullParser) {
                    parserClasses.addElement(candidate);
                    recognized = true;
                }
                if (instance instanceof XmlSerializer) {
                    serializerClasses.addElement(candidate);
                    recognized = true;
                }
                if (instance instanceof XmlPullParserFactory) {
                    if (factory == null) {
                        factory = (XmlPullParserFactory)instance;
                    }
                    recognized = true;
                }
                if (!recognized) {
                    throw new XmlPullParserException("incompatible class: " + name);
                }
            }
        }
        if (factory == null) {
            factory = new XmlPullParserFactory();
        }
        factory.parserClasses = parserClasses;
        factory.serializerClasses = serializerClasses;
        factory.classNamesLocation = classNamesLocation;
        return factory;
    }

    static {
        final XmlPullParserFactory f = new XmlPullParserFactory();
        referenceContextClass = f.getClass();
    }
}
