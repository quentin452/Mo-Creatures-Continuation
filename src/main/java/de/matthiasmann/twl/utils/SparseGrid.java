package de.matthiasmann.twl.utils;

import java.util.*;

public class SparseGrid
{
    Node root;
    int numLevels;
    
    public SparseGrid(final int pageSize) {
        this.root = new Node(pageSize);
        this.numLevels = 1;
    }
    
    public Entry get(final int row, final int column) {
        if (this.root.size > 0) {
            int levels = this.numLevels;
            Entry e = this.root;
            do {
                final Node node = (Node)e;
                final int pos = node.findPos(row, column, node.size);
                if (pos == node.size) {
                    return null;
                }
                e = node.children[pos];
            } while (--levels > 0);
            assert e != null;
            if (e.compare(row, column) == 0) {
                return e;
            }
        }
        return null;
    }
    
    public void set(final int row, final int column, final Entry entry) {
        entry.row = row;
        entry.column = column;
        if (this.root.size == 0) {
            this.root.insertAt(0, entry);
            this.root.updateRowColumn();
        }
        else if (!this.root.insert(entry, this.numLevels)) {
            this.splitRoot();
            this.root.insert(entry, this.numLevels);
        }
    }
    
    public Entry remove(final int row, final int column) {
        if (this.root.size == 0) {
            return null;
        }
        final Entry e = this.root.remove(row, column, this.numLevels);
        if (e != null) {
            this.maybeRemoveRoot();
        }
        return e;
    }
    
    public void insertRows(final int row, final int count) {
        if (count > 0 && this.root.size > 0) {
            this.root.insertRows(row, count, this.numLevels);
        }
    }
    
    public void insertColumns(final int column, final int count) {
        if (count > 0 && this.root.size > 0) {
            this.root.insertColumns(column, count, this.numLevels);
        }
    }
    
    public void removeRows(final int row, final int count) {
        if (count > 0) {
            this.root.removeRows(row, count, this.numLevels);
            this.maybeRemoveRoot();
        }
    }
    
    public void removeColumns(final int column, final int count) {
        if (count > 0) {
            this.root.removeColumns(column, count, this.numLevels);
            this.maybeRemoveRoot();
        }
    }
    
    public void iterate(final int startRow, final int startColumn, final int endRow, final int endColumn, final GridFunction func) {
        if (this.root.size > 0) {
            int levels = this.numLevels;
            Entry e = this.root;
            Node node;
            int pos;
            do {
                node = (Node)e;
                pos = node.findPos(startRow, startColumn, node.size - 1);
                e = node.children[pos];
            } while (--levels > 0);
            assert e != null;
            if (e.compare(startRow, startColumn) < 0) {
                return;
            }
            do {
                for (int size = node.size; pos < size; ++pos) {
                    e = node.children[pos];
                    if (e.row > endRow) {
                        return;
                    }
                    if (e.column >= startColumn && e.column <= endColumn) {
                        func.apply(e.row, e.column, e);
                    }
                }
                pos = 0;
                node = node.next;
            } while (node != null);
        }
    }
    
    public boolean isEmpty() {
        return this.root.size == 0;
    }
    
    public void clear() {
        Arrays.fill(this.root.children, null);
        this.root.size = 0;
        this.numLevels = 1;
    }
    
    private void maybeRemoveRoot() {
        while (this.numLevels > 1 && this.root.size == 1) {
            this.root = (Node)this.root.children[0];
            this.root.prev = null;
            this.root.next = null;
            --this.numLevels;
        }
        if (this.root.size == 0) {
            this.numLevels = 1;
        }
    }
    
    private void splitRoot() {
        final Node newNode = this.root.split();
        final Node newRoot = new Node(this.root.children.length);
        newRoot.children[0] = this.root;
        newRoot.children[1] = newNode;
        newRoot.size = 2;
        this.root = newRoot;
        ++this.numLevels;
    }
    
    static class Node extends Entry
    {
        final Entry[] children;
        int size;
        Node next;
        Node prev;
        
        public Node(final int size) {
            this.children = new Entry[size];
        }
        
        boolean insert(final Entry e, int levels) {
            if (--levels == 0) {
                return this.insertLeaf(e);
            }
            while (true) {
                final int pos = this.findPos(e.row, e.column, this.size - 1);
                assert pos < this.size;
                final Node node = (Node)this.children[pos];
                if (node.insert(e, levels)) {
                    this.updateRowColumn();
                    return true;
                }
                if (this.isFull()) {
                    return false;
                }
                final Node node2 = node.split();
                this.insertAt(pos + 1, node2);
            }
        }
        
        boolean insertLeaf(final Entry e) {
            final int pos = this.findPos(e.row, e.column, this.size);
            if (pos < this.size) {
                final Entry c = this.children[pos];
                assert c.getClass() != Node.class;
                final int cmp = c.compare(e.row, e.column);
                if (cmp == 0) {
                    this.children[pos] = e;
                    return true;
                }
                assert cmp > 0;
            }
            if (this.isFull()) {
                return false;
            }
            this.insertAt(pos, e);
            return true;
        }
        
        Entry remove(final int row, final int column, int levels) {
            if (--levels == 0) {
                return this.removeLeaf(row, column);
            }
            final int pos = this.findPos(row, column, this.size - 1);
            assert pos < this.size;
            final Node node = (Node)this.children[pos];
            final Entry e = node.remove(row, column, levels);
            if (e != null) {
                if (node.size == 0) {
                    this.removeNodeAt(pos);
                }
                else if (node.isBelowHalf()) {
                    this.tryMerge(pos);
                }
                this.updateRowColumn();
            }
            return e;
        }
        
        Entry removeLeaf(final int row, final int column) {
            final int pos = this.findPos(row, column, this.size);
            if (pos == this.size) {
                return null;
            }
            final Entry c = this.children[pos];
            assert c.getClass() != Node.class;
            final int cmp = c.compare(row, column);
            if (cmp == 0) {
                this.removeAt(pos);
                if (pos == this.size && this.size > 0) {
                    this.updateRowColumn();
                }
                return c;
            }
            return null;
        }
        
        int findPos(final int row, final int column, int high) {
            int low = 0;
            while (low < high) {
                final int mid = low + high >>> 1;
                final Entry e = this.children[mid];
                final int cmp = e.compare(row, column);
                if (cmp > 0) {
                    high = mid;
                }
                else {
                    if (cmp >= 0) {
                        return mid;
                    }
                    low = mid + 1;
                }
            }
            return low;
        }
        
        void insertRows(final int row, final int count, int levels) {
            if (--levels > 0) {
                int i = this.size;
                while (i-- > 0) {
                    final Node n = (Node)this.children[i];
                    if (n.row < row) {
                        break;
                    }
                    n.insertRows(row, count, levels);
                }
            }
            else {
                int i = this.size;
                while (i-- > 0) {
                    final Entry e = this.children[i];
                    if (e.row < row) {
                        break;
                    }
                    final Entry entry = e;
                    entry.row += count;
                }
            }
            this.updateRowColumn();
        }
        
        void insertColumns(final int column, final int count, int levels) {
            if (--levels > 0) {
                for (int i = 0; i < this.size; ++i) {
                    final Node n = (Node)this.children[i];
                    n.insertColumns(column, count, levels);
                }
            }
            else {
                for (int i = 0; i < this.size; ++i) {
                    final Entry e = this.children[i];
                    if (e.column >= column) {
                        final Entry entry = e;
                        entry.column += count;
                    }
                }
            }
            this.updateRowColumn();
        }
        
        boolean removeRows(final int row, final int count, int levels) {
            if (--levels > 0) {
                boolean needsMerging = false;
                int i = this.size;
                while (i-- > 0) {
                    final Node n = (Node)this.children[i];
                    if (n.row < row) {
                        break;
                    }
                    if (n.removeRows(row, count, levels)) {
                        this.removeNodeAt(i);
                    }
                    else {
                        needsMerging |= n.isBelowHalf();
                    }
                }
                if (needsMerging && this.size > 1) {
                    this.tryMerge();
                }
            }
            else {
                int j = this.size;
                while (j-- > 0) {
                    final Entry e = this.children[j];
                    if (e.row < row) {
                        break;
                    }
                    final Entry entry = e;
                    entry.row -= count;
                    if (e.row >= row) {
                        continue;
                    }
                    this.removeAt(j);
                }
            }
            if (this.size == 0) {
                return true;
            }
            this.updateRowColumn();
            return false;
        }
        
        boolean removeColumns(final int column, final int count, int levels) {
            if (--levels > 0) {
                boolean needsMerging = false;
                int i = this.size;
                while (i-- > 0) {
                    final Node n = (Node)this.children[i];
                    if (n.removeColumns(column, count, levels)) {
                        this.removeNodeAt(i);
                    }
                    else {
                        needsMerging |= n.isBelowHalf();
                    }
                }
                if (needsMerging && this.size > 1) {
                    this.tryMerge();
                }
            }
            else {
                int j = this.size;
                while (j-- > 0) {
                    final Entry e = this.children[j];
                    if (e.column >= column) {
                        final Entry entry = e;
                        entry.column -= count;
                        if (e.column >= column) {
                            continue;
                        }
                        this.removeAt(j);
                    }
                }
            }
            if (this.size == 0) {
                return true;
            }
            this.updateRowColumn();
            return false;
        }
        
        void insertAt(final int idx, final Entry what) {
            System.arraycopy(this.children, idx, this.children, idx + 1, this.size - idx);
            this.children[idx] = what;
            if (idx == this.size++) {
                this.updateRowColumn();
            }
        }
        
        void removeAt(final int idx) {
            --this.size;
            System.arraycopy(this.children, idx + 1, this.children, idx, this.size - idx);
            this.children[this.size] = null;
        }
        
        void removeNodeAt(final int idx) {
            final Node n = (Node)this.children[idx];
            if (n.next != null) {
                n.next.prev = n.prev;
            }
            if (n.prev != null) {
                n.prev.next = n.next;
            }
            n.next = null;
            n.prev = null;
            this.removeAt(idx);
        }
        
        void tryMerge() {
            if (this.size == 2) {
                this.tryMerge2(0);
            }
            else {
                for (int i = this.size - 1; i-- > 1; --i) {
                    if (this.tryMerge3(i)) {}
                }
            }
        }
        
        void tryMerge(final int pos) {
            switch (this.size) {
                case 0:
                case 1: {
                    break;
                }
                case 2: {
                    this.tryMerge2(0);
                    break;
                }
                default: {
                    if (pos + 1 == this.size) {
                        this.tryMerge3(pos - 1);
                        break;
                    }
                    if (pos == 0) {
                        this.tryMerge3(1);
                        break;
                    }
                    this.tryMerge3(pos);
                    break;
                }
            }
        }
        
        private void tryMerge2(final int pos) {
            final Node n1 = (Node)this.children[pos];
            final Node n2 = (Node)this.children[pos + 1];
            if (n1.isBelowHalf() || n2.isBelowHalf()) {
                final int sumSize = n1.size + n2.size;
                if (sumSize < this.children.length) {
                    System.arraycopy(n2.children, 0, n1.children, n1.size, n2.size);
                    n1.size = sumSize;
                    n1.updateRowColumn();
                    this.removeNodeAt(pos + 1);
                }
                else {
                    final Object[] temp = this.collect2(sumSize, n1, n2);
                    this.distribute2(temp, n1, n2);
                }
            }
        }
        
        private boolean tryMerge3(final int pos) {
            final Node n0 = (Node)this.children[pos - 1];
            final Node n2 = (Node)this.children[pos];
            final Node n3 = (Node)this.children[pos + 1];
            if (n0.isBelowHalf() || n2.isBelowHalf() || n3.isBelowHalf()) {
                final int sumSize = n0.size + n2.size + n3.size;
                if (sumSize < this.children.length) {
                    System.arraycopy(n2.children, 0, n0.children, n0.size, n2.size);
                    System.arraycopy(n3.children, 0, n0.children, n0.size + n2.size, n3.size);
                    n0.size = sumSize;
                    n0.updateRowColumn();
                    this.removeNodeAt(pos + 1);
                    this.removeNodeAt(pos);
                    return true;
                }
                final Object[] temp = this.collect3(sumSize, n0, n2, n3);
                if (sumSize < 2 * this.children.length) {
                    this.distribute2(temp, n0, n2);
                    this.removeNodeAt(pos + 1);
                }
                else {
                    this.distribute3(temp, n0, n2, n3);
                }
            }
            return false;
        }
        
        private Object[] collect2(final int sumSize, final Node n0, final Node n1) {
            final Object[] temp = new Object[sumSize];
            System.arraycopy(n0.children, 0, temp, 0, n0.size);
            System.arraycopy(n1.children, 0, temp, n0.size, n1.size);
            return temp;
        }
        
        private Object[] collect3(final int sumSize, final Node n0, final Node n1, final Node n2) {
            final Object[] temp = new Object[sumSize];
            System.arraycopy(n0.children, 0, temp, 0, n0.size);
            System.arraycopy(n1.children, 0, temp, n0.size, n1.size);
            System.arraycopy(n2.children, 0, temp, n0.size + n1.size, n2.size);
            return temp;
        }
        
        private void distribute2(final Object[] src, final Node n0, final Node n1) {
            final int sumSize = src.length;
            n0.size = sumSize / 2;
            n1.size = sumSize - n0.size;
            System.arraycopy(src, 0, n0.children, 0, n0.size);
            System.arraycopy(src, n0.size, n1.children, 0, n1.size);
            n0.updateRowColumn();
            n1.updateRowColumn();
        }
        
        private void distribute3(final Object[] src, final Node n0, final Node n1, final Node n2) {
            final int sumSize = src.length;
            n0.size = sumSize / 3;
            n1.size = (sumSize - n0.size) / 2;
            n2.size = sumSize - (n0.size + n1.size);
            System.arraycopy(src, 0, n0.children, 0, n0.size);
            System.arraycopy(src, n0.size, n1.children, 0, n1.size);
            System.arraycopy(src, n0.size + n1.size, n2.children, 0, n2.size);
            n0.updateRowColumn();
            n1.updateRowColumn();
            n2.updateRowColumn();
        }
        
        boolean isFull() {
            return this.size == this.children.length;
        }
        
        boolean isBelowHalf() {
            return this.size * 2 < this.children.length;
        }
        
        Node split() {
            final Node newNode = new Node(this.children.length);
            final int size1 = this.size / 2;
            final int size2 = this.size - size1;
            System.arraycopy(this.children, size1, newNode.children, 0, size2);
            Arrays.fill(this.children, size1, this.size, null);
            newNode.size = size2;
            newNode.updateRowColumn();
            newNode.prev = this;
            newNode.next = this.next;
            this.size = size1;
            this.updateRowColumn();
            this.next = newNode;
            if (newNode.next != null) {
                newNode.next.prev = newNode;
            }
            return newNode;
        }
        
        void updateRowColumn() {
            final Entry e = this.children[this.size - 1];
            this.row = e.row;
            this.column = e.column;
        }
    }
    
    public static class Entry
    {
        int row;
        int column;
        
        int compare(final int row, final int column) {
            int diff = this.row - row;
            if (diff == 0) {
                diff = this.column - column;
            }
            return diff;
        }
    }
    
    public interface GridFunction
    {
        void apply(final int p0, final int p1, final Entry p2);
    }
}
