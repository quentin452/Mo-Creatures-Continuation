package de.matthiasmann.twl;

import de.matthiasmann.twl.model.*;

public class Table extends TableBase
{
    private final TableModel.ChangeListener modelChangeListener;
    TableModel model;
    
    public Table() {
        this.modelChangeListener = (TableModel.ChangeListener)new ModelChangeListener();
    }
    
    public Table(final TableModel model) {
        this();
        this.setModel(model);
    }
    
    public TableModel getModel() {
        return this.model;
    }
    
    public void setModel(final TableModel model) {
        if (this.model != null) {
            this.model.removeChangeListener(this.modelChangeListener);
        }
        this.columnHeaderModel = (TableColumnHeaderModel)model;
        this.model = model;
        if (this.model != null) {
            this.numRows = model.getNumRows();
            this.numColumns = model.getNumColumns();
            this.model.addChangeListener(this.modelChangeListener);
        }
        else {
            this.numRows = 0;
            this.numColumns = 0;
        }
        this.modelAllChanged();
    }
    
    @Override
    protected Object getCellData(final int row, final int column, final TreeTableNode node) {
        return this.model.getCell(row, column);
    }
    
    @Override
    protected TreeTableNode getNodeFromRow(final int row) {
        return null;
    }
    
    @Override
    protected Object getTooltipContentFromRow(final int row, final int column) {
        return this.model.getTooltipContent(row, column);
    }
    
    class ModelChangeListener implements TableModel.ChangeListener
    {
        public void rowsInserted(final int idx, final int count) {
            Table.this.numRows = Table.this.model.getNumRows();
            Table.this.modelRowsInserted(idx, count);
        }
        
        public void rowsDeleted(final int idx, final int count) {
            Table.this.checkRowRange(idx, count);
            Table.this.numRows = Table.this.model.getNumRows();
            Table.this.modelRowsDeleted(idx, count);
        }
        
        public void rowsChanged(final int idx, final int count) {
            Table.this.modelRowsChanged(idx, count);
        }
        
        public void columnDeleted(final int idx, final int count) {
            Table.this.checkColumnRange(idx, count);
            Table.this.numColumns = Table.this.model.getNumColumns();
            Table.this.modelColumnsDeleted(count, count);
        }
        
        public void columnInserted(final int idx, final int count) {
            Table.this.numColumns = Table.this.model.getNumColumns();
            Table.this.modelColumnsInserted(count, count);
        }
        
        public void columnHeaderChanged(final int column) {
            Table.this.modelColumnHeaderChanged(column);
        }
        
        public void cellChanged(final int row, final int column) {
            Table.this.modelCellChanged(row, column);
        }
        
        public void allChanged() {
            Table.this.numRows = Table.this.model.getNumRows();
            Table.this.numColumns = Table.this.model.getNumColumns();
            Table.this.modelAllChanged();
        }
    }
}
