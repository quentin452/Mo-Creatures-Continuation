package de.matthiasmann.twl.model;

import java.util.*;
import de.matthiasmann.twl.utils.*;

public abstract class AbstractTreeTableModel extends AbstractTableColumnHeaderModel implements TreeTableModel
{
    private final ArrayList<TreeTableNode> children;
    private ChangeListener[] callbacks;
    
    public AbstractTreeTableModel() {
        this.children = new ArrayList<TreeTableNode>();
    }
    
    public void addChangeListener(final ChangeListener listener) {
        this.callbacks = CallbackSupport.addCallbackToList(this.callbacks, listener, ChangeListener.class);
    }
    
    public void removeChangeListener(final ChangeListener listener) {
        this.callbacks = CallbackSupport.removeCallbackFromList(this.callbacks, listener);
    }
    
    public Object getData(final int column) {
        return null;
    }
    
    public Object getTooltipContent(final int column) {
        return null;
    }
    
    public final TreeTableNode getParent() {
        return null;
    }
    
    public boolean isLeaf() {
        return false;
    }
    
    public int getNumChildren() {
        return this.children.size();
    }
    
    public TreeTableNode getChild(final int idx) {
        return this.children.get(idx);
    }
    
    public int getChildIndex(final TreeTableNode child) {
        for (int i = 0, n = this.children.size(); i < n; ++i) {
            if (this.children.get(i) == child) {
                return i;
            }
        }
        return -1;
    }
    
    protected void insertChild(final TreeTableNode node, final int idx) {
        assert this.getChildIndex(node) < 0;
        assert node.getParent() == this;
        this.children.add(idx, node);
        this.fireNodesAdded(this, idx, 1);
    }
    
    protected void removeChild(final int idx) {
        this.children.remove(idx);
        this.fireNodesRemoved(this, idx, 1);
    }
    
    protected void removeAllChildren() {
        final int count = this.children.size();
        if (count > 0) {
            this.children.clear();
            this.fireNodesRemoved(this, 0, count);
        }
    }
    
    protected void fireNodesAdded(final TreeTableNode parent, final int idx, final int count) {
        if (this.callbacks != null) {
            for (final ChangeListener cl : this.callbacks) {
                cl.nodesAdded(parent, idx, count);
            }
        }
    }
    
    protected void fireNodesRemoved(final TreeTableNode parent, final int idx, final int count) {
        if (this.callbacks != null) {
            for (final ChangeListener cl : this.callbacks) {
                cl.nodesRemoved(parent, idx, count);
            }
        }
    }
    
    protected void fireNodesChanged(final TreeTableNode parent, final int idx, final int count) {
        if (this.callbacks != null) {
            for (final ChangeListener cl : this.callbacks) {
                cl.nodesChanged(parent, idx, count);
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
}
