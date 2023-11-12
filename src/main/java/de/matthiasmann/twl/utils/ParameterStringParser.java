package de.matthiasmann.twl.utils;

public class ParameterStringParser
{
    private final String str;
    private final char parameterSeparator;
    private final char keyValueSeparator;
    private boolean trim;
    private int pos;
    private String key;
    private String value;
    
    public ParameterStringParser(final String str, final char parameterSeparator, final char keyValueSeparator) {
        if (str == null) {
            throw new NullPointerException("str");
        }
        if (parameterSeparator == keyValueSeparator) {
            throw new IllegalArgumentException("parameterSeperator == keyValueSeperator");
        }
        this.str = str;
        this.parameterSeparator = parameterSeparator;
        this.keyValueSeparator = keyValueSeparator;
    }
    
    public boolean isTrim() {
        return this.trim;
    }
    
    public void setTrim(final boolean trim) {
        this.trim = trim;
    }
    
    public boolean next() {
        while (this.pos < this.str.length()) {
            final int kvPairEnd = TextUtil.indexOf(this.str, this.parameterSeparator, this.pos);
            final int keyEnd = TextUtil.indexOf(this.str, this.keyValueSeparator, this.pos);
            if (keyEnd < kvPairEnd) {
                this.key = this.substring(this.pos, keyEnd);
                this.value = this.substring(keyEnd + 1, kvPairEnd);
                this.pos = kvPairEnd + 1;
                return true;
            }
            this.pos = kvPairEnd + 1;
        }
        this.key = null;
        this.value = null;
        return false;
    }
    
    public String getKey() {
        if (this.key == null) {
            throw new IllegalStateException("no key-value pair available");
        }
        return this.key;
    }
    
    public String getValue() {
        if (this.value == null) {
            throw new IllegalStateException("no key-value pair available");
        }
        return this.value;
    }
    
    private String substring(final int start, final int end) {
        if (this.trim) {
            return TextUtil.trim(this.str, start, end);
        }
        return this.str.substring(start, end);
    }
}
