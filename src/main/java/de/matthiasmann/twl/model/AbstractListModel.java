package de.matthiasmann.twl.model;

import de.matthiasmann.twl.utils.*;

public abstract class AbstractListModel<T> implements ListModel<T>
{
    private ChangeListener[] listeners;
    
    @Override
    public void addChangeListener(final ChangeListener listener) {
        this.listeners = CallbackSupport.addCallbackToList(this.listeners, listener, ChangeListener.class);
    }
    
    @Override
    public void removeChangeListener(final ChangeListener listener) {
        this.listeners = CallbackSupport.removeCallbackFromList(this.listeners, listener);
    }
    
    protected void fireEntriesInserted(final int first, final int last) {
        if (this.listeners != null) {
            for (final ChangeListener cl : this.listeners) {
                cl.entriesInserted(first, last);
            }
        }
    }
    
    protected void fireEntriesDeleted(final int first, final int last) {
        if (this.listeners != null) {
            for (final ChangeListener cl : this.listeners) {
                cl.entriesDeleted(first, last);
            }
        }
    }
    
    protected void fireEntriesChanged(final int first, final int last) {
        if (this.listeners != null) {
            for (final ChangeListener cl : this.listeners) {
                cl.entriesChanged(first, last);
            }
        }
    }
    
    protected void fireAllChanged() {
        if (this.listeners != null) {
            for (final ChangeListener cl : this.listeners) {
                cl.allChanged();
            }
        }
    }
}
