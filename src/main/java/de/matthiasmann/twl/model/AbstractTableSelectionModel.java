package de.matthiasmann.twl.model;

import de.matthiasmann.twl.utils.*;

public abstract class AbstractTableSelectionModel implements TableSelectionModel
{
    protected int leadIndex;
    protected int anchorIndex;
    protected Runnable[] selectionChangeListener;
    
    protected AbstractTableSelectionModel() {
        this.leadIndex = -1;
        this.anchorIndex = -1;
    }
    
    @Override
    public int getAnchorIndex() {
        return this.anchorIndex;
    }
    
    @Override
    public int getLeadIndex() {
        return this.leadIndex;
    }
    
    @Override
    public void setAnchorIndex(final int index) {
        this.anchorIndex = index;
    }
    
    @Override
    public void setLeadIndex(final int index) {
        this.leadIndex = index;
    }
    
    @Override
    public void addSelectionChangeListener(final Runnable cb) {
        this.selectionChangeListener = CallbackSupport.addCallbackToList(this.selectionChangeListener, cb, Runnable.class);
    }
    
    @Override
    public void removeSelectionChangeListener(final Runnable cb) {
        this.selectionChangeListener = CallbackSupport.removeCallbackFromList(this.selectionChangeListener, cb);
    }
    
    @Override
    public void rowsDeleted(final int index, final int count) {
        if (this.leadIndex >= index) {
            this.leadIndex = Math.max(index, this.leadIndex - count);
        }
        if (this.anchorIndex >= index) {
            this.anchorIndex = Math.max(index, this.anchorIndex - count);
        }
    }
    
    @Override
    public void rowsInserted(final int index, final int count) {
        if (this.leadIndex >= index) {
            this.leadIndex += count;
        }
        if (this.anchorIndex >= index) {
            this.anchorIndex += count;
        }
    }
    
    protected void fireSelectionChange() {
        CallbackSupport.fireCallbacks(this.selectionChangeListener);
    }
    
    protected void updateLeadAndAnchor(final int index0, final int index1) {
        this.anchorIndex = index0;
        this.leadIndex = index1;
    }
}
