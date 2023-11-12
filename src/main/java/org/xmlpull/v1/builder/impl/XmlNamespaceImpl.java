//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Administrator\Downloads\Minecraft-Deobfuscator3000-1.2.3\Minecraft-Deobfuscator3000-1.2.3\1.7.10 stable mappings"!

//Decompiled by Procyon!

package org.xmlpull.v1.builder.impl;

import org.xmlpull.v1.builder.*;

public class XmlNamespaceImpl implements XmlNamespace
{
    private String namespaceName;
    private String prefix;
    
    XmlNamespaceImpl(final String namespaceName) {
        if (namespaceName == null) {
            throw new XmlBuilderException("namespace name can not be null");
        }
        this.namespaceName = namespaceName;
    }
    
    XmlNamespaceImpl(final String prefix, final String namespaceName) {
        this.prefix = prefix;
        if (namespaceName == null) {
            throw new XmlBuilderException("namespace name can not be null");
        }
        if (prefix != null && prefix.indexOf(58) != -1) {
            throw new XmlBuilderException("prefix '" + prefix + "' for namespace '" + namespaceName + "' can not contain colon (:)");
        }
        this.namespaceName = namespaceName;
    }
    
    public String getPrefix() {
        return this.prefix;
    }
    
    public String getNamespaceName() {
        return this.namespaceName;
    }
    
    public boolean equals(final Object other) {
        if (other == this) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof XmlNamespace)) {
            return false;
        }
        final XmlNamespace otherNamespace = (XmlNamespace)other;
        return this.getNamespaceName().equals(otherNamespace.getNamespaceName());
    }
    
    public String toString() {
        return "{prefix='" + this.prefix + "',namespaceName='" + this.namespaceName + "'}";
    }
}
