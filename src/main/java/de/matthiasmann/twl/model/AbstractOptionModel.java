package de.matthiasmann.twl.model;

import de.matthiasmann.twl.utils.*;

public abstract class AbstractOptionModel implements BooleanModel
{
    Runnable[] callbacks;
    Runnable srcCallback;
    
    @Override
    public void addCallback(final Runnable callback) {
        if (callback == null) {
            throw new NullPointerException("callback");
        }
        if (this.callbacks == null) {
            this.srcCallback = new Runnable() {
                boolean lastValue = AbstractOptionModel.this.getValue();
                
                @Override
                public void run() {
                    final boolean value = AbstractOptionModel.this.getValue();
                    if (this.lastValue != value) {
                        this.lastValue = value;
                        CallbackSupport.fireCallbacks(AbstractOptionModel.this.callbacks);
                    }
                }
            };
            this.callbacks = new Runnable[] { callback };
            this.installSrcCallback(this.srcCallback);
        }
        else {
            this.callbacks = CallbackSupport.addCallbackToList(this.callbacks, callback, Runnable.class);
        }
    }
    
    @Override
    public void removeCallback(final Runnable callback) {
        this.callbacks = CallbackSupport.removeCallbackFromList(this.callbacks, callback);
        if (this.callbacks == null && this.srcCallback != null) {
            this.removeSrcCallback(this.srcCallback);
            this.srcCallback = null;
        }
    }
    
    protected abstract void installSrcCallback(final Runnable p0);
    
    protected abstract void removeSrcCallback(final Runnable p0);
}
