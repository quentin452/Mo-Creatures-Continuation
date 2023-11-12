//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Administrator\Downloads\Minecraft-Deobfuscator3000-1.2.3\Minecraft-Deobfuscator3000-1.2.3\1.7.10 stable mappings"!

//Decompiled by Procyon!

package org.xmlpull.v1.builder;

import java.util.*;

public interface XmlDoctype extends XmlContainer
{
    String getSystemIdentifier();
    
    String getPublicIdentifier();
    
    Iterator children();
    
    XmlDocument getParent();
    
    XmlProcessingInstruction addProcessingInstruction(final String p0, final String p1);
    
    void removeAllProcessingInstructions();
}
