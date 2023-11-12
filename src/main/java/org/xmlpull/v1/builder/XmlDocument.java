//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Administrator\Downloads\Minecraft-Deobfuscator3000-1.2.3\Minecraft-Deobfuscator3000-1.2.3\1.7.10 stable mappings"!

//Decompiled by Procyon!

package org.xmlpull.v1.builder;

public interface XmlDocument extends XmlContainer, Cloneable
{
    Object clone() throws CloneNotSupportedException;
    
    Iterable children();
    
    XmlElement getDocumentElement();
    
    XmlElement requiredElement(final XmlNamespace p0, final String p1);
    
    XmlElement element(final XmlNamespace p0, final String p1);
    
    XmlElement element(final XmlNamespace p0, final String p1, final boolean p2);
    
    Iterable notations();
    
    Iterable unparsedEntities();
    
    String getBaseUri();
    
    String getCharacterEncodingScheme();
    
    void setCharacterEncodingScheme(final String p0);
    
    Boolean isStandalone();
    
    String getVersion();
    
    boolean isAllDeclarationsProcessed();
    
    void setDocumentElement(final XmlElement p0);
    
    void addChild(final Object p0);
    
    void insertChild(final int p0, final Object p1);
    
    void removeAllChildren();
    
    XmlComment newComment(final String p0);
    
    XmlComment addComment(final String p0);
    
    XmlDoctype newDoctype(final String p0, final String p1);
    
    XmlDoctype addDoctype(final String p0, final String p1);
    
    XmlElement addDocumentElement(final String p0);
    
    XmlElement addDocumentElement(final XmlNamespace p0, final String p1);
    
    XmlProcessingInstruction newProcessingInstruction(final String p0, final String p1);
    
    XmlProcessingInstruction addProcessingInstruction(final String p0, final String p1);
    
    void removeAllUnparsedEntities();
    
    XmlNotation addNotation(final String p0, final String p1, final String p2, final String p3);
    
    void removeAllNotations();
}
