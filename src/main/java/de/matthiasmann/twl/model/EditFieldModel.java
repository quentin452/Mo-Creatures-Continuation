package de.matthiasmann.twl.model;

public interface EditFieldModel extends ObservableCharSequence
{
    int replace(final int p0, final int p1, final String p2);
    
    boolean replace(final int p0, final int p1, final char p2);
    
    String substring(final int p0, final int p1);
}
