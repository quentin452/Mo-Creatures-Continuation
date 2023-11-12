package de.matthiasmann.twl.model;

public abstract class AutoCompletionResult
{
    public static final int DEFAULT_CURSOR_POS = -1;
    protected final String text;
    protected final int prefixLength;
    
    public AutoCompletionResult(final String text, final int prefixLength) {
        this.text = text;
        this.prefixLength = prefixLength;
    }
    
    public int getPrefixLength() {
        return this.prefixLength;
    }
    
    public String getText() {
        return this.text;
    }
    
    public abstract int getNumResults();
    
    public abstract String getResult(final int p0);
    
    public int getCursorPosForResult(final int idx) {
        return -1;
    }
    
    public AutoCompletionResult refine(final String text, final int cursorPos) {
        return null;
    }
}
