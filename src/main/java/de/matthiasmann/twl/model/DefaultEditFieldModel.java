package de.matthiasmann.twl.model;

import de.matthiasmann.twl.utils.*;

public class DefaultEditFieldModel implements EditFieldModel
{
    private final StringBuilder sb;
    private ObservableCharSequence.Callback[] callbacks;
    
    public DefaultEditFieldModel() {
        this.sb = new StringBuilder();
    }
    
    @Override
    public int length() {
        return this.sb.length();
    }
    
    @Override
    public char charAt(final int index) {
        return this.sb.charAt(index);
    }
    
    @Override
    public CharSequence subSequence(final int start, final int end) {
        return this.sb.subSequence(start, end);
    }
    
    @Override
    public String toString() {
        return this.sb.toString();
    }
    
    @Override
    public void addCallback(final ObservableCharSequence.Callback callback) {
        this.callbacks = CallbackSupport.addCallbackToList(this.callbacks, callback, ObservableCharSequence.Callback.class);
    }
    
    @Override
    public void removeCallback(final ObservableCharSequence.Callback callback) {
        this.callbacks = CallbackSupport.removeCallbackFromList(this.callbacks, callback);
    }
    
    @Override
    public int replace(final int start, final int count, final String replacement) {
        this.checkRange(start, count);
        final int replacementLength = replacement.length();
        if (count > 0 || replacementLength > 0) {
            this.sb.replace(start, start + count, replacement);
            this.fireCallback(start, count, replacementLength);
        }
        return replacementLength;
    }
    
    @Override
    public boolean replace(final int start, final int count, final char replacement) {
        this.checkRange(start, count);
        if (count == 0) {
            this.sb.insert(start, replacement);
        }
        else {
            this.sb.delete(start, start + count - 1);
            this.sb.setCharAt(start, replacement);
        }
        this.fireCallback(start, count, 1);
        return true;
    }
    
    @Override
    public String substring(final int start, final int end) {
        return this.sb.substring(start, end);
    }
    
    private void checkRange(final int start, final int count) {
        final int len = this.sb.length();
        if (start < 0 || start > len) {
            throw new StringIndexOutOfBoundsException(start);
        }
        if (count < 0 || count > len - start) {
            throw new StringIndexOutOfBoundsException();
        }
    }
    
    private void fireCallback(final int start, final int oldCount, final int newCount) {
        final ObservableCharSequence.Callback[] cbs = this.callbacks;
        if (cbs != null) {
            for (final ObservableCharSequence.Callback cb : cbs) {
                cb.charactersChanged(start, oldCount, newCount);
            }
        }
    }
}
