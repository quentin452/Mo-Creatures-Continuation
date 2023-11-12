package org.xmlpull.v1.builder;

public interface XmlUnparsedEntity
{
    String getName();
    
    String getSystemIdentifier();
    
    String getPublicIdentifier();
    
    String getDeclarationBaseUri();
    
    String getNotationName();
    
    XmlNotation getNotation();
}
