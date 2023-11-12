package de.matthiasmann.twl.model;

public interface TreeTableModel extends TableColumnHeaderModel, TreeTableNode
{
    void addChangeListener(final ChangeListener p0);
    
    void removeChangeListener(final ChangeListener p0);
    
    public interface ChangeListener extends TableColumnHeaderModel.ColumnHeaderChangeListener
    {
        void nodesAdded(final TreeTableNode p0, final int p1, final int p2);
        
        void nodesRemoved(final TreeTableNode p0, final int p1, final int p2);
        
        void nodesChanged(final TreeTableNode p0, final int p1, final int p2);
    }
}
