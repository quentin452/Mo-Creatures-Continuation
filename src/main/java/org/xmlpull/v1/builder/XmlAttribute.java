package org.xmlpull.v1.builder;

public interface XmlAttribute extends Cloneable
{
    Object clone() throws CloneNotSupportedException;
    
    XmlElement getOwner();
    
    String getNamespaceName();
    
    XmlNamespace getNamespace();
    
    String getName();
    
    String getValue();
    
    String getType();
    
    boolean isSpecified();
}
