package de.matthiasmann.twl.model;

public interface TableSelectionModel
{
    void rowsInserted(final int p0, final int p1);
    
    void rowsDeleted(final int p0, final int p1);
    
    void clearSelection();
    
    void setSelection(final int p0, final int p1);
    
    void addSelection(final int p0, final int p1);
    
    void invertSelection(final int p0, final int p1);
    
    void removeSelection(final int p0, final int p1);
    
    int getLeadIndex();
    
    int getAnchorIndex();
    
    void setLeadIndex(final int p0);
    
    void setAnchorIndex(final int p0);
    
    boolean isSelected(final int p0);
    
    boolean hasSelection();
    
    int getFirstSelected();
    
    int getLastSelected();
    
    int[] getSelection();
    
    void addSelectionChangeListener(final Runnable p0);
    
    void removeSelectionChangeListener(final Runnable p0);
}
