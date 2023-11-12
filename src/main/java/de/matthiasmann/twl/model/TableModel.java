package de.matthiasmann.twl.model;

public interface TableModel extends TableColumnHeaderModel
{
    int getNumRows();
    
    Object getCell(final int p0, final int p1);
    
    Object getTooltipContent(final int p0, final int p1);
    
    void addChangeListener(final ChangeListener p0);
    
    void removeChangeListener(final ChangeListener p0);
    
    public interface ChangeListener extends TableColumnHeaderModel.ColumnHeaderChangeListener
    {
        void rowsInserted(final int p0, final int p1);
        
        void rowsDeleted(final int p0, final int p1);
        
        void rowsChanged(final int p0, final int p1);
        
        void cellChanged(final int p0, final int p1);
        
        void allChanged();
    }
}
