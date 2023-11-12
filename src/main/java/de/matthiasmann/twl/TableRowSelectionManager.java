package de.matthiasmann.twl;

import de.matthiasmann.twl.model.*;

public class TableRowSelectionManager implements TableSelectionManager
{
    protected final ActionMap actionMap;
    protected final TableSelectionModel selectionModel;
    protected TableBase tableBase;
    protected static final int TOGGLE = 0;
    protected static final int EXTEND = 1;
    protected static final int SET = 2;
    protected static final int MOVE = 3;
    
    public TableRowSelectionManager(final TableSelectionModel selectionModel) {
        if (selectionModel == null) {
            throw new NullPointerException("selectionModel");
        }
        this.selectionModel = selectionModel;
        (this.actionMap = new ActionMap()).addMapping((Object)this);
    }
    
    public TableRowSelectionManager() {
        this((TableSelectionModel)new DefaultTableSelectionModel());
    }
    
    @Override
    public TableSelectionModel getSelectionModel() {
        return this.selectionModel;
    }
    
    @Override
    public void setAssociatedTable(final TableBase base) {
        if (this.tableBase != base) {
            if (this.tableBase != null && base != null) {
                throw new IllegalStateException("selection manager still in use");
            }
            this.tableBase = base;
            this.modelChanged();
        }
    }
    
    @Override
    public SelectionGranularity getSelectionGranularity() {
        return SelectionGranularity.ROWS;
    }
    
    @Override
    public boolean handleKeyStrokeAction(final String action, final Event event) {
        return this.actionMap.invoke(action, event);
    }
    
    @Override
    public boolean handleMouseEvent(final int row, final int column, final Event event) {
        final boolean isShift = (event.getModifiers() & 0x9) != 0x0;
        final boolean isCtrl = (event.getModifiers() & 0x24) != 0x0;
        if (event.getType() == Event.Type.MOUSE_BTNDOWN && event.getMouseButton() == 0) {
            this.handleMouseDown(row, column, isShift, isCtrl);
            return true;
        }
        return event.getType() == Event.Type.MOUSE_CLICKED && this.handleMouseClick(row, column, isShift, isCtrl);
    }
    
    @Override
    public boolean isRowSelected(final int row) {
        return this.selectionModel.isSelected(row);
    }
    
    @Override
    public boolean isCellSelected(final int row, final int column) {
        return false;
    }
    
    @Override
    public int getLeadRow() {
        return this.selectionModel.getLeadIndex();
    }
    
    @Override
    public int getLeadColumn() {
        return -1;
    }
    
    @Override
    public void modelChanged() {
        this.selectionModel.clearSelection();
        this.selectionModel.setAnchorIndex(-1);
        this.selectionModel.setLeadIndex(-1);
    }
    
    @Override
    public void rowsInserted(final int index, final int count) {
        this.selectionModel.rowsInserted(index, count);
    }
    
    @Override
    public void rowsDeleted(final int index, final int count) {
        this.selectionModel.rowsDeleted(index, count);
    }
    
    @Override
    public void columnInserted(final int index, final int count) {
    }
    
    @Override
    public void columnsDeleted(final int index, final int count) {
    }
    
    @ActionMap.Action
    public void selectNextRow() {
        this.handleRelativeAction(1, 2);
    }
    
    @ActionMap.Action
    public void selectPreviousRow() {
        this.handleRelativeAction(-1, 2);
    }
    
    @ActionMap.Action
    public void selectNextPage() {
        this.handleRelativeAction(this.getPageSize(), 2);
    }
    
    @ActionMap.Action
    public void selectPreviousPage() {
        this.handleRelativeAction(-this.getPageSize(), 2);
    }
    
    @ActionMap.Action
    public void selectFirstRow() {
        final int numRows = this.getNumRows();
        if (numRows > 0) {
            this.handleAbsoluteAction(0, 2);
        }
    }
    
    @ActionMap.Action
    public void selectLastRow() {
        final int numRows = this.getNumRows();
        if (numRows > 0) {
            this.handleRelativeAction(numRows - 1, 2);
        }
    }
    
    @ActionMap.Action
    public void extendSelectionToNextRow() {
        this.handleRelativeAction(1, 1);
    }
    
    @ActionMap.Action
    public void extendSelectionToPreviousRow() {
        this.handleRelativeAction(-1, 1);
    }
    
    @ActionMap.Action
    public void extendSelectionToNextPage() {
        this.handleRelativeAction(this.getPageSize(), 1);
    }
    
    @ActionMap.Action
    public void extendSelectionToPreviousPage() {
        this.handleRelativeAction(-this.getPageSize(), 1);
    }
    
    @ActionMap.Action
    public void extendSelectionToFirstRow() {
        final int numRows = this.getNumRows();
        if (numRows > 0) {
            this.handleAbsoluteAction(0, 1);
        }
    }
    
    @ActionMap.Action
    public void extendSelectionToLastRow() {
        final int numRows = this.getNumRows();
        if (numRows > 0) {
            this.handleRelativeAction(numRows - 1, 1);
        }
    }
    
    @ActionMap.Action
    public void moveLeadToNextRow() {
        this.handleRelativeAction(1, 3);
    }
    
    @ActionMap.Action
    public void moveLeadToPreviousRow() {
        this.handleRelativeAction(-1, 3);
    }
    
    @ActionMap.Action
    public void moveLeadToNextPage() {
        this.handleRelativeAction(this.getPageSize(), 3);
    }
    
    @ActionMap.Action
    public void moveLeadToPreviousPage() {
        this.handleRelativeAction(-this.getPageSize(), 3);
    }
    
    @ActionMap.Action
    public void moveLeadToFirstRow() {
        final int numRows = this.getNumRows();
        if (numRows > 0) {
            this.handleAbsoluteAction(0, 3);
        }
    }
    
    @ActionMap.Action
    public void moveLeadToLastRow() {
        final int numRows = this.getNumRows();
        if (numRows > 0) {
            this.handleAbsoluteAction(numRows - 1, 3);
        }
    }
    
    @ActionMap.Action
    public void toggleSelectionOnLeadRow() {
        final int leadIndex = this.selectionModel.getLeadIndex();
        if (leadIndex > 0) {
            this.selectionModel.invertSelection(leadIndex, leadIndex);
        }
    }
    
    @ActionMap.Action
    public void selectAll() {
        final int numRows = this.getNumRows();
        if (numRows > 0) {
            this.selectionModel.setSelection(0, numRows - 1);
        }
    }
    
    @ActionMap.Action
    public void selectNone() {
        this.selectionModel.clearSelection();
    }
    
    protected void handleRelativeAction(final int delta, final int mode) {
        final int numRows = this.getNumRows();
        if (numRows > 0) {
            final int leadIndex = Math.max(0, this.selectionModel.getLeadIndex());
            final int index = Math.max(0, Math.min(numRows - 1, leadIndex + delta));
            this.handleAbsoluteAction(index, mode);
        }
    }
    
    protected void handleAbsoluteAction(final int index, final int mode) {
        if (this.tableBase != null) {
            this.tableBase.adjustScrollPosition(index);
        }
        switch (mode) {
            case 3: {
                this.selectionModel.setLeadIndex(index);
                break;
            }
            case 1: {
                final int anchorIndex = Math.max(0, this.selectionModel.getAnchorIndex());
                this.selectionModel.setSelection(anchorIndex, index);
                break;
            }
            case 0: {
                this.selectionModel.invertSelection(index, index);
                break;
            }
            default: {
                this.selectionModel.setSelection(index, index);
                break;
            }
        }
    }
    
    protected void handleMouseDown(final int row, final int column, final boolean isShift, final boolean isCtrl) {
        if (row < 0 || row >= this.getNumRows()) {
            if (!isShift) {
                this.selectionModel.clearSelection();
            }
        }
        else {
            this.tableBase.adjustScrollPosition(row);
            int anchorIndex = this.selectionModel.getAnchorIndex();
            boolean anchorSelected;
            if (anchorIndex == -1) {
                anchorIndex = 0;
                anchorSelected = false;
            }
            else {
                anchorSelected = this.selectionModel.isSelected(anchorIndex);
            }
            if (isCtrl) {
                if (isShift) {
                    if (anchorSelected) {
                        this.selectionModel.addSelection(anchorIndex, row);
                    }
                    else {
                        this.selectionModel.removeSelection(anchorIndex, row);
                    }
                }
                else if (this.selectionModel.isSelected(row)) {
                    this.selectionModel.removeSelection(row, row);
                }
                else {
                    this.selectionModel.addSelection(row, row);
                }
            }
            else if (isShift) {
                this.selectionModel.setSelection(anchorIndex, row);
            }
            else {
                this.selectionModel.setSelection(row, row);
            }
        }
    }
    
    protected boolean handleMouseClick(final int row, final int column, final boolean isShift, final boolean isCtrl) {
        return false;
    }
    
    protected int getNumRows() {
        if (this.tableBase != null) {
            return this.tableBase.getNumRows();
        }
        return 0;
    }
    
    protected int getPageSize() {
        if (this.tableBase != null) {
            return Math.max(1, this.tableBase.getNumVisibleRows());
        }
        return 1;
    }
}
