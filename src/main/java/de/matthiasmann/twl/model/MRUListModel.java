package de.matthiasmann.twl.model;

public interface MRUListModel<T> extends ListModel<T>
{
    int getMaxEntries();
    
    int getNumEntries();
    
    T getEntry(final int p0);
    
    void addEntry(final T p0);
    
    void removeEntry(final int p0);
    
    void addChangeListener(final ListModel.ChangeListener p0);
    
    void removeChangeListener(final ListModel.ChangeListener p0);
}
