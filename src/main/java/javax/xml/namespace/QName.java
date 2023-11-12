package javax.xml.namespace;

import java.io.*;

public class QName implements Serializable
{
    private static final String emptyString;
    private String namespaceURI;
    private String localPart;
    private String prefix;
    
    public QName(final String localPart) {
        this(QName.emptyString, localPart, QName.emptyString);
    }
    
    public QName(final String namespaceURI, final String localPart) {
        this(namespaceURI, localPart, QName.emptyString);
    }
    
    public QName(final String namespaceURI, final String localPart, final String prefix) {
        this.namespaceURI = ((namespaceURI == null) ? QName.emptyString : namespaceURI.intern());
        if (localPart == null) {
            throw new IllegalArgumentException("invalid QName local part");
        }
        this.localPart = localPart.intern();
        if (prefix == null) {
            throw new IllegalArgumentException("invalid QName prefix");
        }
        this.prefix = prefix.intern();
    }
    
    public String getNamespaceURI() {
        return this.namespaceURI;
    }
    
    public String getLocalPart() {
        return this.localPart;
    }
    
    public String getPrefix() {
        return this.prefix;
    }
    
    public String toString() {
        return (this.namespaceURI == QName.emptyString) ? this.localPart : ('{' + this.namespaceURI + '}' + this.localPart);
    }
    
    public final boolean equals(final Object obj) {
        return obj == this || (obj instanceof QName && (this.namespaceURI == ((QName)obj).namespaceURI && this.localPart == ((QName)obj).localPart));
    }
    
    public static QName valueOf(final String s) {
        if (s == null || s.equals("")) {
            throw new IllegalArgumentException("invalid QName literal");
        }
        if (s.charAt(0) != '{') {
            return new QName(s);
        }
        final int i = s.indexOf(125);
        if (i == -1) {
            throw new IllegalArgumentException("invalid QName literal");
        }
        if (i == s.length() - 1) {
            throw new IllegalArgumentException("invalid QName literal");
        }
        return new QName(s.substring(1, i), s.substring(i + 1));
    }
    
    public final int hashCode() {
        return this.namespaceURI.hashCode() ^ this.localPart.hashCode();
    }
    
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.namespaceURI = this.namespaceURI.intern();
        this.localPart = this.localPart.intern();
        this.prefix = this.prefix.intern();
    }
    
    static {
        emptyString = "".intern();
    }
}
