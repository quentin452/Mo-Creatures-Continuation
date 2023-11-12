package de.matthiasmann.twl.model;

import de.matthiasmann.twl.renderer.*;

public interface TableColumnHeaderModel
{
    int getNumColumns();
    
    AnimationState.StateKey[] getColumnHeaderStates();
    
    String getColumnHeaderText(final int p0);
    
    boolean getColumnHeaderState(final int p0, final int p1);
    
    public interface ColumnHeaderChangeListener
    {
        void columnInserted(final int p0, final int p1);
        
        void columnDeleted(final int p0, final int p1);
        
        void columnHeaderChanged(final int p0);
    }
}
