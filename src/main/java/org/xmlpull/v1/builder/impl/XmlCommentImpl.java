package org.xmlpull.v1.builder.impl;

import org.xmlpull.v1.builder.*;

public class XmlCommentImpl implements XmlComment
{
    private XmlContainer owner_;
    private String content_;
    
    XmlCommentImpl(final XmlContainer owner, final String content) {
        this.owner_ = owner;
        this.content_ = content;
        if (content == null) {
            throw new IllegalArgumentException("comment content can not be null");
        }
    }
    
    public String getContent() {
        return this.content_;
    }
    
    public XmlContainer getParent() {
        return this.owner_;
    }
}
