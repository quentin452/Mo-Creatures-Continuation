package de.matthiasmann.twl.model;

public interface ListModel<T>
{
    int getNumEntries();
    
    T getEntry(final int p0);
    
    Object getEntryTooltip(final int p0);
    
    boolean matchPrefix(final int p0, final String p1);
    
    void addChangeListener(final ChangeListener p0);
    
    void removeChangeListener(final ChangeListener p0);
    
    public interface ChangeListener
    {
        void entriesInserted(final int p0, final int p1);
        
        void entriesDeleted(final int p0, final int p1);
        
        void entriesChanged(final int p0, final int p1);
        
        void allChanged();
    }
}
