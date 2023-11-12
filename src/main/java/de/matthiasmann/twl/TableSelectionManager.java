package de.matthiasmann.twl;

import de.matthiasmann.twl.model.*;

public interface TableSelectionManager
{
    TableSelectionModel getSelectionModel();
    
    void setAssociatedTable(final TableBase p0);
    
    SelectionGranularity getSelectionGranularity();
    
    boolean handleKeyStrokeAction(final String p0, final Event p1);
    
    boolean handleMouseEvent(final int p0, final int p1, final Event p2);
    
    boolean isRowSelected(final int p0);
    
    boolean isCellSelected(final int p0, final int p1);
    
    int getLeadRow();
    
    int getLeadColumn();
    
    void modelChanged();
    
    void rowsInserted(final int p0, final int p1);
    
    void rowsDeleted(final int p0, final int p1);
    
    void columnInserted(final int p0, final int p1);
    
    void columnsDeleted(final int p0, final int p1);
    
    public enum SelectionGranularity
    {
        ROWS, 
        CELLS;
    }
}
