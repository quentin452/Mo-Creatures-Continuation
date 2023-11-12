package de.matthiasmann.twl.model;

public class TableSingleSelectionModel extends AbstractTableSelectionModel
{
    public static final int NO_SELECTION = -1;
    private int selection;
    
    public void rowsInserted(final int index, final int count) {
        boolean changed = false;
        if (this.selection >= index) {
            this.selection += count;
            changed = true;
        }
        super.rowsInserted(index, count);
        if (changed) {
            this.fireSelectionChange();
        }
    }
    
    public void rowsDeleted(final int index, final int count) {
        boolean changed = false;
        if (this.selection >= index) {
            if (this.selection < index + count) {
                this.selection = -1;
            }
            else {
                this.selection -= count;
            }
            changed = true;
        }
        super.rowsDeleted(index, count);
        if (changed) {
            this.fireSelectionChange();
        }
    }
    
    public void clearSelection() {
        if (this.hasSelection()) {
            this.selection = -1;
            this.fireSelectionChange();
        }
    }
    
    public void setSelection(final int index0, final int index1) {
        this.updateLeadAndAnchor(index0, index1);
        this.selection = index1;
        this.fireSelectionChange();
    }
    
    public void addSelection(final int index0, final int index1) {
        this.updateLeadAndAnchor(index0, index1);
        this.selection = index1;
        this.fireSelectionChange();
    }
    
    public void invertSelection(final int index0, final int index1) {
        this.updateLeadAndAnchor(index0, index1);
        if (this.selection == index1) {
            this.selection = -1;
        }
        else {
            this.selection = index1;
        }
        this.fireSelectionChange();
    }
    
    public void removeSelection(final int index0, final int index1) {
        this.updateLeadAndAnchor(index0, index1);
        if (this.hasSelection()) {
            final int first = Math.min(index0, index1);
            final int last = Math.max(index0, index1);
            if (this.selection >= first && this.selection <= last) {
                this.selection = -1;
            }
            this.fireSelectionChange();
        }
    }
    
    public boolean isSelected(final int index) {
        return this.selection == index;
    }
    
    public boolean hasSelection() {
        return this.selection >= 0;
    }
    
    public int getFirstSelected() {
        return this.selection;
    }
    
    public int getLastSelected() {
        return this.selection;
    }
    
    public int[] getSelection() {
        if (this.selection >= 0) {
            return new int[] { this.selection };
        }
        return new int[0];
    }
}
