package de.matthiasmann.twl.model;

import java.util.*;

public class CombinedListModel<T> extends SimpleListModel<T>
{
    private final ArrayList<Sublist> sublists;
    private int[] sublistStarts;
    private SubListsModel subListsModel;
    
    public CombinedListModel() {
        this.sublists = new ArrayList<Sublist>();
        this.sublistStarts = new int[1];
    }
    
    public int getNumEntries() {
        return this.sublistStarts[this.sublistStarts.length - 1];
    }
    
    public T getEntry(final int index) {
        final Sublist sl = this.getSublistForIndex(index);
        if (sl != null) {
            return sl.getEntry(index - sl.startIndex);
        }
        throw new IndexOutOfBoundsException();
    }
    
    @Override
    public Object getEntryTooltip(final int index) {
        final Sublist sl = this.getSublistForIndex(index);
        if (sl != null) {
            return sl.getEntryTooltip(index - sl.startIndex);
        }
        throw new IndexOutOfBoundsException();
    }
    
    public int getNumSubLists() {
        return this.sublists.size();
    }
    
    public void addSubList(final ListModel<T> model) {
        this.addSubList(this.sublists.size(), model);
    }
    
    public void addSubList(final int index, final ListModel<T> model) {
        final Sublist sl = new Sublist(model);
        this.sublists.add(index, sl);
        this.adjustStartOffsets();
        final int numEntries = sl.getNumEntries();
        if (numEntries > 0) {
            this.fireEntriesInserted(sl.startIndex, sl.startIndex + numEntries - 1);
        }
        if (this.subListsModel != null) {
            this.subListsModel.fireEntriesInserted(index, index);
        }
    }
    
    public int findSubList(final ListModel<T> model) {
        for (int i = 0; i < this.sublists.size(); ++i) {
            final Sublist sl = this.sublists.get(i);
            if (sl.list == model) {
                return i;
            }
        }
        return -1;
    }
    
    public void removeAllSubLists() {
        for (int i = 0; i < this.sublists.size(); ++i) {
            this.sublists.get(i).removeChangeListener();
        }
        this.sublists.clear();
        this.adjustStartOffsets();
        this.fireAllChanged();
        if (this.subListsModel != null) {
            this.subListsModel.fireAllChanged();
        }
    }
    
    public boolean removeSubList(final ListModel<T> model) {
        final int index = this.findSubList(model);
        if (index >= 0) {
            this.removeSubList(index);
            return true;
        }
        return false;
    }
    
    public ListModel<T> removeSubList(final int index) {
        final Sublist sl = this.sublists.remove(index);
        sl.removeChangeListener();
        this.adjustStartOffsets();
        final int numEntries = sl.getNumEntries();
        if (numEntries > 0) {
            this.fireEntriesDeleted(sl.startIndex, sl.startIndex + numEntries - 1);
        }
        if (this.subListsModel != null) {
            this.subListsModel.fireEntriesDeleted(index, index);
        }
        return sl.list;
    }
    
    public ListModel<ListModel<T>> getModelForSubLists() {
        if (this.subListsModel == null) {
            this.subListsModel = new SubListsModel();
        }
        return (ListModel<ListModel<T>>)this.subListsModel;
    }
    
    public int getStartIndexOfSublist(final int sublistIndex) {
        return this.sublists.get(sublistIndex).startIndex;
    }
    
    private Sublist getSublistForIndex(final int index) {
        final int[] offsets = this.sublistStarts;
        int lo = 0;
        int mid;
        for (int hi = offsets.length - 1; lo <= hi; hi = mid - 1) {
            mid = lo + hi >>> 1;
            final int delta = offsets[mid] - index;
            if (delta <= 0) {
                lo = mid + 1;
            }
            if (delta > 0) {}
        }
        if (lo <= 0 || lo > this.sublists.size()) {
            return null;
        }
        final Sublist sl = this.sublists.get(lo - 1);
        assert sl.startIndex <= index;
        return sl;
    }
    
    void adjustStartOffsets() {
        final int[] offsets = new int[this.sublists.size() + 1];
        for (int startIdx = 0, idx = 0; idx < this.sublists.size(); offsets[++idx] = startIdx) {
            final Sublist sl = this.sublists.get(idx);
            sl.startIndex = startIdx;
            startIdx += sl.getNumEntries();
        }
        this.sublistStarts = offsets;
    }
    
    class Sublist implements ListModel.ChangeListener
    {
        final ListModel<T> list;
        int startIndex;
        
        public Sublist(final ListModel<T> list) {
            (this.list = list).addChangeListener(this);
        }
        
        public void removeChangeListener() {
            this.list.removeChangeListener(this);
        }
        
        public boolean matchPrefix(final int index, final String prefix) {
            return this.list.matchPrefix(index, prefix);
        }
        
        public int getNumEntries() {
            return this.list.getNumEntries();
        }
        
        public Object getEntryTooltip(final int index) {
            return this.list.getEntryTooltip(index);
        }
        
        public T getEntry(final int index) {
            return this.list.getEntry(index);
        }
        
        @Override
        public void entriesInserted(final int first, final int last) {
            CombinedListModel.this.adjustStartOffsets();
            CombinedListModel.this.fireEntriesInserted(this.startIndex + first, this.startIndex + last);
        }
        
        @Override
        public void entriesDeleted(final int first, final int last) {
            CombinedListModel.this.adjustStartOffsets();
            CombinedListModel.this.fireEntriesDeleted(this.startIndex + first, this.startIndex + last);
        }
        
        @Override
        public void entriesChanged(final int first, final int last) {
            CombinedListModel.this.fireEntriesChanged(this.startIndex + first, this.startIndex + last);
        }
        
        @Override
        public void allChanged() {
            CombinedListModel.this.fireAllChanged();
        }
    }
    
    class SubListsModel extends SimpleListModel<ListModel<T>>
    {
        public int getNumEntries() {
            return CombinedListModel.this.sublists.size();
        }
        
        public ListModel<T> getEntry(final int index) {
            return CombinedListModel.this.sublists.get(index).list;
        }
    }
}
