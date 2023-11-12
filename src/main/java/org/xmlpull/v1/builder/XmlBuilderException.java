//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Administrator\Downloads\Minecraft-Deobfuscator3000-1.2.3\Minecraft-Deobfuscator3000-1.2.3\1.7.10 stable mappings"!

//Decompiled by Procyon!

package org.xmlpull.v1.builder;

import java.io.*;

public class XmlBuilderException extends RuntimeException
{
    protected Throwable detail;
    
    public XmlBuilderException(final String s) {
        super(s);
    }
    
    public XmlBuilderException(final String s, final Throwable thrwble) {
        super(s);
        this.detail = thrwble;
    }
    
    public Throwable getDetail() {
        return this.detail;
    }
    
    public String getMessage() {
        if (this.detail == null) {
            return super.getMessage();
        }
        return super.getMessage() + "; nested exception is: \n\t" + this.detail.getMessage();
    }
    
    public void printStackTrace(final PrintStream ps) {
        if (this.detail == null) {
            super.printStackTrace(ps);
        }
        else {
            synchronized (ps) {
                ps.println(super.getMessage() + "; nested exception is:");
                this.detail.printStackTrace(ps);
            }
        }
    }
    
    public void printStackTrace() {
        this.printStackTrace(System.err);
    }
    
    public void printStackTrace(final PrintWriter pw) {
        if (this.detail == null) {
            super.printStackTrace(pw);
        }
        else {
            synchronized (pw) {
                pw.println(super.getMessage() + "; nested exception is:");
                this.detail.printStackTrace(pw);
            }
        }
    }
}
