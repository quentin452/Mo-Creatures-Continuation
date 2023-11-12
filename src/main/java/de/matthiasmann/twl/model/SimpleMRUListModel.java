package de.matthiasmann.twl.model;

import java.util.*;
import de.matthiasmann.twl.utils.*;

public class SimpleMRUListModel<T> implements MRUListModel<T>
{
    protected final ArrayList<T> entries;
    protected final int maxEntries;
    protected ListModel.ChangeListener[] listeners;
    
    public SimpleMRUListModel(final int maxEntries) {
        if (maxEntries <= 1) {
            throw new IllegalArgumentException("maxEntries <= 1");
        }
        this.entries = new ArrayList<T>();
        this.maxEntries = maxEntries;
    }
    
    public int getMaxEntries() {
        return this.maxEntries;
    }
    
    public int getNumEntries() {
        return this.entries.size();
    }
    
    public T getEntry(final int index) {
        return this.entries.get(index);
    }
    
    public void addEntry(final T entry) {
        final int idx = this.entries.indexOf(entry);
        if (idx >= 0) {
            this.doDeleteEntry(idx);
        }
        else if (this.entries.size() == this.maxEntries) {
            this.doDeleteEntry(this.maxEntries - 1);
        }
        this.entries.add(0, entry);
        if (this.listeners != null) {
            for (final ListModel.ChangeListener cl : this.listeners) {
                cl.entriesInserted(0, 0);
            }
        }
        this.saveEntries();
    }
    
    public void removeEntry(final int index) {
        if (index < 0 && index >= this.entries.size()) {
            throw new IndexOutOfBoundsException();
        }
        this.doDeleteEntry(index);
        this.saveEntries();
    }
    
    public void addChangeListener(final ListModel.ChangeListener listener) {
        this.listeners = CallbackSupport.addCallbackToList(this.listeners, listener, ListModel.ChangeListener.class);
    }
    
    public void removeChangeListener(final ListModel.ChangeListener listener) {
        this.listeners = CallbackSupport.removeCallbackFromList(this.listeners, listener);
    }
    
    protected void doDeleteEntry(final int idx) {
        this.entries.remove(idx);
        if (this.listeners != null) {
            for (final ListModel.ChangeListener cl : this.listeners) {
                cl.entriesDeleted(idx, idx);
            }
        }
    }
    
    protected void saveEntries() {
    }
    
    public Object getEntryTooltip(final int index) {
        return null;
    }
    
    public boolean matchPrefix(final int index, final String prefix) {
        return false;
    }
}
