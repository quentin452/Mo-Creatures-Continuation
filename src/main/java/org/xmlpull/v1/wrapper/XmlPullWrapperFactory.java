package org.xmlpull.v1.wrapper;

import org.xmlpull.v1.wrapper.classic.*;
import org.xmlpull.v1.*;

public class XmlPullWrapperFactory
{
    private static final boolean DEBUG = false;
    protected XmlPullParserFactory f;
    
    public static XmlPullWrapperFactory newInstance() throws XmlPullParserException {
        return new XmlPullWrapperFactory(null);
    }
    
    public static XmlPullWrapperFactory newInstance(final XmlPullParserFactory factory) throws XmlPullParserException {
        return new XmlPullWrapperFactory(factory);
    }
    
    public static XmlPullWrapperFactory newInstance(final String classNames, final Class context) throws XmlPullParserException {
        final XmlPullParserFactory factory = XmlPullParserFactory.newInstance(classNames, context);
        return new XmlPullWrapperFactory(factory);
    }
    
    protected XmlPullWrapperFactory(final XmlPullParserFactory factory) throws XmlPullParserException {
        if (factory != null) {
            this.f = factory;
        }
        else {
            this.f = XmlPullParserFactory.newInstance();
        }
    }
    
    public XmlPullParserFactory getFactory() throws XmlPullParserException {
        return this.f;
    }
    
    public void setFeature(final String name, final boolean state) throws XmlPullParserException {
        this.f.setFeature(name, state);
    }
    
    public boolean getFeature(final String name) {
        return this.f.getFeature(name);
    }
    
    public void setNamespaceAware(final boolean awareness) {
        this.f.setNamespaceAware(awareness);
    }
    
    public boolean isNamespaceAware() {
        return this.f.isNamespaceAware();
    }
    
    public void setValidating(final boolean validating) {
        this.f.setValidating(validating);
    }
    
    public boolean isValidating() {
        return this.f.isValidating();
    }
    
    public XmlPullParserWrapper newPullParserWrapper() throws XmlPullParserException {
        final XmlPullParser pp = this.f.newPullParser();
        return (XmlPullParserWrapper)new StaticXmlPullParserWrapper(pp);
    }
    
    public XmlPullParserWrapper newPullParserWrapper(final XmlPullParser pp) throws XmlPullParserException {
        return (XmlPullParserWrapper)new StaticXmlPullParserWrapper(pp);
    }
    
    public XmlSerializerWrapper newSerializerWrapper() throws XmlPullParserException {
        final XmlSerializer xs = this.f.newSerializer();
        return (XmlSerializerWrapper)new StaticXmlSerializerWrapper(xs, this);
    }
    
    public XmlSerializerWrapper newSerializerWrapper(final XmlSerializer xs) throws XmlPullParserException {
        return (XmlSerializerWrapper)new StaticXmlSerializerWrapper(xs, this);
    }
}
