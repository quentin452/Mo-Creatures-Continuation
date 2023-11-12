package de.matthiasmann.twl.model;

import de.matthiasmann.twl.utils.*;

public class HasCallback implements WithRunnableCallback
{
    private Runnable[] callbacks;
    
    @Override
    public void addCallback(final Runnable callback) {
        this.callbacks = CallbackSupport.addCallbackToList(this.callbacks, callback, Runnable.class);
    }
    
    @Override
    public void removeCallback(final Runnable callback) {
        this.callbacks = CallbackSupport.removeCallbackFromList(this.callbacks, callback);
    }
    
    public boolean hasCallbacks() {
        return this.callbacks != null;
    }
    
    protected void doCallback() {
        CallbackSupport.fireCallbacks(this.callbacks);
    }
}
