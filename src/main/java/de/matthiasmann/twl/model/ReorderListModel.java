package de.matthiasmann.twl.model;

import java.util.*;

public class ReorderListModel<T> extends AbstractListModel<T>
{
    private final ListModel<T> base;
    private final ListModel.ChangeListener listener;
    private int[] reorderList;
    private int size;
    private static final int INSERTIONSORT_THRESHOLD = 7;
    
    public ReorderListModel(final ListModel<T> base) {
        this.base = base;
        this.reorderList = new int[0];
        base.addChangeListener(this.listener = (ListModel.ChangeListener)new ListModel.ChangeListener() {
            public void entriesInserted(final int first, final int last) {
                ReorderListModel.this.entriesInserted(first, last);
            }
            
            public void entriesDeleted(final int first, final int last) {
                ReorderListModel.this.entriesDeleted(first, last);
            }
            
            public void entriesChanged(final int first, final int last) {
            }
            
            public void allChanged() {
                ReorderListModel.this.buildNewList();
            }
        });
        this.buildNewList();
    }
    
    public void destroy() {
        this.base.removeChangeListener(this.listener);
    }
    
    public int getNumEntries() {
        return this.size;
    }
    
    public T getEntry(final int index) {
        final int remappedIndex = this.reorderList[index];
        return (T)this.base.getEntry(remappedIndex);
    }
    
    public Object getEntryTooltip(final int index) {
        final int remappedIndex = this.reorderList[index];
        return this.base.getEntryTooltip(remappedIndex);
    }
    
    public boolean matchPrefix(final int index, final String prefix) {
        final int remappedIndex = this.reorderList[index];
        return this.base.matchPrefix(remappedIndex, prefix);
    }
    
    public int findEntry(final Object o) {
        final int[] list = this.reorderList;
        for (int i = 0, n = this.size; i < n; ++i) {
            final T entry = (T)this.base.getEntry(list[i]);
            if (entry == o || (entry != null && entry.equals(o))) {
                return i;
            }
        }
        return -1;
    }
    
    public void shuffle() {
        final Random r = new Random();
        int j;
        int temp;
        for (int i = this.size; i > 1; j = r.nextInt(i--), temp = this.reorderList[i], this.reorderList[i] = this.reorderList[j], this.reorderList[j] = temp) {}
        this.fireAllChanged();
    }
    
    public void sort(final Comparator<T> c) {
        final int[] aux = new int[this.size];
        System.arraycopy(this.reorderList, 0, aux, 0, this.size);
        this.mergeSort(aux, this.reorderList, 0, this.size, c);
        this.fireAllChanged();
    }
    
    private void mergeSort(final int[] src, final int[] dest, final int low, final int high, final Comparator<T> c) {
        final int length = high - low;
        if (length < 7) {
            for (int i = low; i < high; ++i) {
                for (int j = i; j > low && this.compare(dest, j - 1, j, c) > 0; --j) {
                    swap(dest, j, j - 1);
                }
            }
            return;
        }
        final int mid = low + high >>> 1;
        this.mergeSort(dest, src, low, mid, c);
        this.mergeSort(dest, src, mid, high, c);
        if (this.compare(src, mid - 1, mid, c) <= 0) {
            System.arraycopy(src, low, dest, low, length);
            return;
        }
        int k = low;
        int p = low;
        int q = mid;
        while (k < high) {
            if (q >= high || (p < mid && this.compare(src, p, q, c) <= 0)) {
                dest[k] = src[p++];
            }
            else {
                dest[k] = src[q++];
            }
            ++k;
        }
    }
    
    private int compare(final int[] list, final int a, final int b, final Comparator<T> c) {
        final int aIdx = list[a];
        final int bIdx = list[b];
        final T objA = (T)this.base.getEntry(aIdx);
        final T objB = (T)this.base.getEntry(bIdx);
        return c.compare(objA, objB);
    }
    
    private static void swap(final int[] x, final int a, final int b) {
        final int t = x[a];
        x[a] = x[b];
        x[b] = t;
    }
    
    private void buildNewList() {
        this.size = this.base.getNumEntries();
        this.reorderList = new int[this.size + 1024];
        for (int i = 0; i < this.size; ++i) {
            this.reorderList[i] = i;
        }
        this.fireAllChanged();
    }
    
    private void entriesInserted(final int first, final int last) {
        final int delta = last - first + 1;
        for (int i = 0; i < this.size; ++i) {
            if (this.reorderList[i] >= first) {
                final int[] reorderList = this.reorderList;
                final int n = i;
                reorderList[n] += delta;
            }
        }
        if (this.size + delta > this.reorderList.length) {
            final int[] newList = new int[Math.max(this.size * 2, this.size + delta + 1024)];
            System.arraycopy(this.reorderList, 0, newList, 0, this.size);
            this.reorderList = newList;
        }
        final int oldSize = this.size;
        for (int j = 0; j < delta; ++j) {
            this.reorderList[this.size++] = first + j;
        }
        this.fireEntriesInserted(oldSize, this.size - 1);
    }
    
    private void entriesDeleted(final int first, final int last) {
        final int delta = last - first + 1;
        for (int i = 0; i < this.size; ++i) {
            final int entry = this.reorderList[i];
            if (entry >= first) {
                if (entry <= last) {
                    this.entriesDeletedCopy(first, last, i);
                    return;
                }
                this.reorderList[i] = entry - delta;
            }
        }
    }
    
    private void entriesDeletedCopy(final int first, final int last, int i) {
        final int delta = last - first + 1;
        final int oldSize = this.size;
        int j = i;
        while (i < oldSize) {
            int entry = this.reorderList[i];
            Label_0082: {
                if (entry >= first) {
                    if (entry <= last) {
                        --this.size;
                        this.fireEntriesDeleted(j, j);
                        break Label_0082;
                    }
                    entry -= delta;
                }
                this.reorderList[j++] = entry;
            }
            ++i;
        }
        assert this.size == j;
    }
}
