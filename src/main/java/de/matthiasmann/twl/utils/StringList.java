package de.matthiasmann.twl.utils;

import java.util.*;

public final class StringList implements Iterable<String>
{
    private final String value;
    private final StringList next;
    
    public StringList(final String value) {
        this(value, null);
    }
    
    public StringList(final String value, final StringList next) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        this.value = value;
        this.next = next;
    }
    
    public StringList getNext() {
        return this.next;
    }
    
    public String getValue() {
        return this.value;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof StringList)) {
            return false;
        }
        final StringList that = (StringList)obj;
        return this.value.equals(that.value) && (this.next == that.next || (this.next != null && this.next.equals(that.next)));
    }
    
    @Override
    public int hashCode() {
        int hash = this.value.hashCode();
        if (this.next != null) {
            hash = 67 * hash + this.next.hashCode();
        }
        return hash;
    }
    
    @Override
    public String toString() {
        if (this.next == null) {
            return this.value;
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(this.value);
        for (StringList list = this.next; list != null; list = list.next) {
            sb.append(", ").append(list.value);
        }
        return sb.toString();
    }
    
    @Override
    public Iterator<String> iterator() {
        return new I(this);
    }
    
    static class I implements Iterator<String>
    {
        private StringList list;
        
        I(final StringList list) {
            this.list = list;
        }
        
        @Override
        public boolean hasNext() {
            return this.list != null;
        }
        
        @Override
        public String next() {
            if (this.list == null) {
                throw new NoSuchElementException();
            }
            final String value = this.list.getValue();
            this.list = this.list.getNext();
            return value;
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported");
        }
    }
}
