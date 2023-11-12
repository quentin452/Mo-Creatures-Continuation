package de.matthiasmann.twl.model;

import de.matthiasmann.twl.utils.*;

public abstract class AbstractTableModel extends AbstractTableColumnHeaderModel implements TableModel
{
    private ChangeListener[] callbacks;
    
    public Object getTooltipContent(final int row, final int column) {
        return null;
    }
    
    public void addChangeListener(final ChangeListener listener) {
        this.callbacks = CallbackSupport.addCallbackToList(this.callbacks, listener, ChangeListener.class);
    }
    
    public void removeChangeListener(final ChangeListener listener) {
        this.callbacks = CallbackSupport.removeCallbackFromList(this.callbacks, listener);
    }
    
    protected boolean hasCallbacks() {
        return this.callbacks != null;
    }
    
    protected void fireRowsInserted(final int idx, final int count) {
        if (this.callbacks != null) {
            for (final ChangeListener cl : this.callbacks) {
                cl.rowsInserted(idx, count);
            }
        }
    }
    
    protected void fireRowsDeleted(final int idx, final int count) {
        if (this.callbacks != null) {
            for (final ChangeListener cl : this.callbacks) {
                cl.rowsDeleted(idx, count);
            }
        }
    }
    
    protected void fireRowsChanged(final int idx, final int count) {
        if (this.callbacks != null) {
            for (final ChangeListener cl : this.callbacks) {
                cl.rowsChanged(idx, count);
            }
        }
    }
    
    protected void fireColumnInserted(final int idx, final int count) {
        if (this.callbacks != null) {
            for (final ChangeListener cl : this.callbacks) {
                cl.columnInserted(idx, count);
            }
        }
    }
    
    protected void fireColumnDeleted(final int idx, final int count) {
        if (this.callbacks != null) {
            for (final ChangeListener cl : this.callbacks) {
                cl.columnDeleted(idx, count);
            }
        }
    }
    
    protected void fireColumnHeaderChanged(final int column) {
        if (this.callbacks != null) {
            for (final ChangeListener cl : this.callbacks) {
                cl.columnHeaderChanged(column);
            }
        }
    }
    
    protected void fireCellChanged(final int row, final int column) {
        if (this.callbacks != null) {
            for (final ChangeListener cl : this.callbacks) {
                cl.cellChanged(row, column);
            }
        }
    }
    
    protected void fireAllChanged() {
        if (this.callbacks != null) {
            for (final ChangeListener cl : this.callbacks) {
                cl.allChanged();
            }
        }
    }
}
