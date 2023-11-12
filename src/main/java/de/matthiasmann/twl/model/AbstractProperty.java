package de.matthiasmann.twl.model;

import de.matthiasmann.twl.utils.*;

public abstract class AbstractProperty<T> implements Property<T>
{
    private Runnable[] valueChangedCallbacks;
    
    @Override
    public void addValueChangedCallback(final Runnable cb) {
        this.valueChangedCallbacks = CallbackSupport.addCallbackToList(this.valueChangedCallbacks, cb, Runnable.class);
    }
    
    @Override
    public void removeValueChangedCallback(final Runnable cb) {
        this.valueChangedCallbacks = CallbackSupport.removeCallbackFromList(this.valueChangedCallbacks, cb);
    }
    
    public boolean hasValueChangedCallbacks() {
        return this.valueChangedCallbacks != null;
    }
    
    protected void fireValueChangedCallback() {
        CallbackSupport.fireCallbacks(this.valueChangedCallbacks);
    }
}
