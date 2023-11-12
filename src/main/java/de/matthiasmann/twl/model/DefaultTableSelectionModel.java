package de.matthiasmann.twl.model;

import java.util.*;

public class DefaultTableSelectionModel extends AbstractTableSelectionModel
{
    private final BitSet value;
    private int minIndex;
    private int maxIndex;
    
    public DefaultTableSelectionModel() {
        this.value = new BitSet();
        this.minIndex = Integer.MAX_VALUE;
        this.maxIndex = Integer.MIN_VALUE;
    }
    
    public int getFirstSelected() {
        return this.minIndex;
    }
    
    public int getLastSelected() {
        return this.maxIndex;
    }
    
    public boolean hasSelection() {
        return this.maxIndex >= this.minIndex;
    }
    
    public boolean isSelected(final int index) {
        return this.value.get(index);
    }
    
    private void clearBit(final int idx) {
        if (this.value.get(idx)) {
            this.value.clear(idx);
            if (idx == this.minIndex) {
                this.minIndex = this.value.nextSetBit(this.minIndex + 1);
                if (this.minIndex < 0) {
                    this.minIndex = Integer.MAX_VALUE;
                    this.maxIndex = Integer.MIN_VALUE;
                    return;
                }
            }
            if (idx == this.maxIndex) {
                do {
                    --this.maxIndex;
                } while (this.maxIndex >= this.minIndex && !this.value.get(this.maxIndex));
            }
        }
    }
    
    private void setBit(final int idx) {
        if (!this.value.get(idx)) {
            this.value.set(idx);
            if (idx < this.minIndex) {
                this.minIndex = idx;
            }
            if (idx > this.maxIndex) {
                this.maxIndex = idx;
            }
        }
    }
    
    private void toggleBit(final int idx) {
        if (this.value.get(idx)) {
            this.clearBit(idx);
        }
        else {
            this.setBit(idx);
        }
    }
    
    public void clearSelection() {
        if (this.hasSelection()) {
            this.minIndex = Integer.MAX_VALUE;
            this.maxIndex = Integer.MIN_VALUE;
            this.value.clear();
            this.fireSelectionChange();
        }
    }
    
    public void setSelection(final int index0, final int index1) {
        this.updateLeadAndAnchor(index0, index1);
        this.minIndex = Math.min(index0, index1);
        this.maxIndex = Math.max(index0, index1);
        this.value.clear();
        this.value.set(this.minIndex, this.maxIndex + 1);
        this.fireSelectionChange();
    }
    
    public void addSelection(final int index0, final int index1) {
        this.updateLeadAndAnchor(index0, index1);
        final int min = Math.min(index0, index1);
        for (int max = Math.max(index0, index1), i = min; i <= max; ++i) {
            this.setBit(i);
        }
        this.fireSelectionChange();
    }
    
    public void invertSelection(final int index0, final int index1) {
        this.updateLeadAndAnchor(index0, index1);
        final int min = Math.min(index0, index1);
        for (int max = Math.max(index0, index1), i = min; i <= max; ++i) {
            this.toggleBit(i);
        }
        this.fireSelectionChange();
    }
    
    public void removeSelection(final int index0, final int index1) {
        this.updateLeadAndAnchor(index0, index1);
        if (this.hasSelection()) {
            final int min = Math.min(index0, index1);
            for (int max = Math.max(index0, index1), i = min; i <= max; ++i) {
                this.clearBit(i);
            }
            this.fireSelectionChange();
        }
    }
    
    public int[] getSelection() {
        final int[] result = new int[this.value.cardinality()];
        int idx = -1;
        int i = 0;
        while ((idx = this.value.nextSetBit(idx + 1)) >= 0) {
            result[i] = idx;
            ++i;
        }
        return result;
    }
    
    public void rowsInserted(final int index, final int count) {
        if (index <= this.maxIndex) {
            for (int i = this.maxIndex; i >= index; --i) {
                if (this.value.get(i)) {
                    this.value.set(i + count);
                }
                else {
                    this.value.clear(i + count);
                }
            }
            this.value.clear(index, index + count);
            this.maxIndex += count;
            if (index <= this.minIndex) {
                this.minIndex += count;
            }
        }
        super.rowsInserted(index, count);
    }
    
    public void rowsDeleted(final int index, final int count) {
        if (index <= this.maxIndex) {
            for (int i = index; i <= this.maxIndex; ++i) {
                if (this.value.get(i + count)) {
                    this.value.set(i);
                }
                else {
                    this.value.clear(i);
                }
            }
            this.minIndex = this.value.nextSetBit(0);
            if (this.minIndex < 0) {
                this.minIndex = Integer.MAX_VALUE;
                this.maxIndex = Integer.MIN_VALUE;
            }
            else {
                while (this.maxIndex >= this.minIndex && !this.value.get(this.maxIndex)) {
                    --this.maxIndex;
                }
            }
        }
        super.rowsDeleted(index, count);
    }
}
