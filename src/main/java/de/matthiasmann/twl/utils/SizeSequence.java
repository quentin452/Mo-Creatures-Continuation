package de.matthiasmann.twl.utils;

import java.util.*;

public class SizeSequence
{
    private static final int INITIAL_CAPACITY = 64;
    protected int[] table;
    protected int size;
    protected int defaultValue;
    
    public SizeSequence() {
        this(64);
    }
    
    public SizeSequence(final int initialCapacity) {
        this.table = new int[initialCapacity];
    }
    
    public int size() {
        return this.size;
    }
    
    public int getPosition(final int index) {
        int low = 0;
        int high = this.size;
        int result = 0;
        while (low < high) {
            final int mid = low + high >>> 1;
            if (index <= mid) {
                high = mid;
            }
            else {
                result += this.table[mid];
                low = mid + 1;
            }
        }
        return result;
    }
    
    public int getEndPosition() {
        int low = 0;
        final int high = this.size;
        int result = 0;
        while (low < high) {
            final int mid = low + high >>> 1;
            result += this.table[mid];
            low = mid + 1;
        }
        return result;
    }
    
    public int getIndex(int position) {
        int low = 0;
        int high = this.size;
        while (low < high) {
            final int mid = low + high >>> 1;
            final int pos = this.table[mid];
            if (position < pos) {
                high = mid;
            }
            else {
                low = mid + 1;
                position -= pos;
            }
        }
        return low;
    }
    
    public int getSize(final int index) {
        return this.getPosition(index + 1) - this.getPosition(index);
    }
    
    public boolean setSize(final int index, final int size) {
        final int delta = size - this.getSize(index);
        if (delta != 0) {
            this.adjustSize(index, delta);
            return true;
        }
        return false;
    }
    
    protected void adjustSize(final int index, final int delta) {
        int low = 0;
        int high = this.size;
        while (low < high) {
            final int mid = low + high >>> 1;
            if (index <= mid) {
                final int[] table = this.table;
                final int n = mid;
                table[n] += delta;
                high = mid;
            }
            else {
                low = mid + 1;
            }
        }
    }
    
    protected int toSizes(int low, final int high, final int[] dst) {
        int subResult = 0;
        while (low < high) {
            final int mid = low + high >>> 1;
            final int pos = this.table[mid];
            dst[mid] = pos - this.toSizes(low, mid, dst);
            subResult += pos;
            low = mid + 1;
        }
        return subResult;
    }
    
    protected int fromSizes(int low, final int high) {
        int subResult = 0;
        while (low < high) {
            final int mid = low + high >>> 1;
            final int pos = this.table[mid] + this.fromSizes(low, mid);
            this.table[mid] = pos;
            subResult += pos;
            low = mid + 1;
        }
        return subResult;
    }
    
    public void insert(final int index, final int count) {
        final int newSize = this.size + count;
        if (newSize >= this.table.length) {
            final int[] sizes = new int[newSize];
            this.toSizes(0, this.size, sizes);
            this.table = sizes;
        }
        else {
            this.toSizes(0, this.size, this.table);
        }
        System.arraycopy(this.table, index, this.table, index + count, this.size - index);
        this.size = newSize;
        this.initializeSizes(index, count);
        this.fromSizes(0, newSize);
    }
    
    public void remove(final int index, final int count) {
        this.toSizes(0, this.size, this.table);
        final int newSize = this.size - count;
        System.arraycopy(this.table, index + count, this.table, index, newSize - index);
        this.fromSizes(0, this.size = newSize);
    }
    
    public void initializeAll(final int count) {
        if (this.table.length < count) {
            this.table = new int[count];
        }
        this.initializeSizes(0, this.size = count);
        this.fromSizes(0, count);
    }
    
    public void setDefaultValue(final int defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    protected void initializeSizes(final int index, final int count) {
        Arrays.fill(this.table, index, index + count, this.defaultValue);
    }
}
