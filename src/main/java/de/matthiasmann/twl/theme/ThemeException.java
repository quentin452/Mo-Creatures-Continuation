package de.matthiasmann.twl.theme;

import java.io.*;
import java.net.*;

public class ThemeException extends IOException
{
    protected final Source source;
    
    public ThemeException(final String msg, final URL url, final int lineNumber, final int columnNumber, final Throwable cause) {
        super(msg);
        this.source = new Source(url, lineNumber, columnNumber);
        this.initCause(cause);
    }
    
    void addIncludedBy(final URL url, final int lineNumber, final int columnNumber) {
        Source head;
        for (head = this.source; head.includedBy != null; head = head.includedBy) {}
        head.includedBy = new Source(url, lineNumber, columnNumber);
    }
    
    @Override
    public String getMessage() {
        final StringBuilder sb = new StringBuilder(super.getMessage());
        String prefix = "\n           in ";
        for (Source src = this.source; src != null; src = src.includedBy) {
            sb.append(prefix).append(src.url).append(" @").append(src.lineNumber).append(':').append(src.columnNumber);
            prefix = "\n  included by ";
        }
        return sb.toString();
    }
    
    public Source getSource() {
        return this.source;
    }
    
    public static final class Source
    {
        protected final URL url;
        protected final int lineNumber;
        protected final int columnNumber;
        protected Source includedBy;
        
        Source(final URL url, final int lineNumber, final int columnNumber) {
            this.url = url;
            this.lineNumber = lineNumber;
            this.columnNumber = columnNumber;
        }
        
        public URL getUrl() {
            return this.url;
        }
        
        public int getLineNumber() {
            return this.lineNumber;
        }
        
        public int getColumnNumber() {
            return this.columnNumber;
        }
        
        public Source getIncludedBy() {
            return this.includedBy;
        }
    }
}
