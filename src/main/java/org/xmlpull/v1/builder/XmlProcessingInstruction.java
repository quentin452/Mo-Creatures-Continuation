package org.xmlpull.v1.builder;

public interface XmlProcessingInstruction
{
    String getTarget();

    String getContent();

    String getBaseUri();

    XmlNotation getNotation();

    XmlContainer getParent();
}
