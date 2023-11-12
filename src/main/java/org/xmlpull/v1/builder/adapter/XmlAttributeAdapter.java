package org.xmlpull.v1.builder.adapter;

import org.xmlpull.v1.builder.*;

public class XmlAttributeAdapter implements XmlAttribute
{
    private XmlAttribute target;
    
    public Object clone() throws CloneNotSupportedException {
        final XmlAttributeAdapter ela = (XmlAttributeAdapter)super.clone();
        ela.target = (XmlAttribute)this.target.clone();
        return ela;
    }
    
    public XmlAttributeAdapter(final XmlAttribute target) {
        this.target = target;
    }
    
    public XmlElement getOwner() {
        return this.target.getOwner();
    }
    
    public String getNamespaceName() {
        return this.target.getNamespaceName();
    }
    
    public XmlNamespace getNamespace() {
        return this.target.getNamespace();
    }
    
    public String getName() {
        return this.target.getName();
    }
    
    public String getValue() {
        return this.target.getValue();
    }
    
    public String getType() {
        return this.target.getType();
    }
    
    public boolean isSpecified() {
        return this.target.isSpecified();
    }
}
