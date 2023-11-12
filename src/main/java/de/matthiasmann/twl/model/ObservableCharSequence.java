package de.matthiasmann.twl.model;

public interface ObservableCharSequence extends CharSequence
{
    void addCallback(final Callback p0);
    
    void removeCallback(final Callback p0);
    
    public interface Callback
    {
        void charactersChanged(final int p0, final int p1, final int p2);
    }
}
