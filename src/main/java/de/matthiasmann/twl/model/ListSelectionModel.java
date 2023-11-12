package de.matthiasmann.twl.model;

public interface ListSelectionModel<T> extends IntegerModel
{
    public static final int NO_SELECTION = -1;
    
    ListModel<T> getListModel();
    
    T getSelectedEntry();
    
    boolean setSelectedEntry(final T p0);
    
    boolean setSelectedEntry(final T p0, final int p1);
}
