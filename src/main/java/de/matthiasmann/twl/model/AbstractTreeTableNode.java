package de.matthiasmann.twl.model;

import java.util.*;

public abstract class AbstractTreeTableNode implements TreeTableNode
{
    private final TreeTableNode parent;
    private ArrayList<TreeTableNode> children;
    private boolean leaf;
    
    protected AbstractTreeTableNode(final TreeTableNode parent) {
        if (parent == null) {
            throw new NullPointerException("parent");
        }
        this.parent = parent;
        assert this.getTreeTableModel() != null;
    }
    
    @Override
    public Object getTooltipContent(final int column) {
        return null;
    }
    
    @Override
    public final TreeTableNode getParent() {
        return this.parent;
    }
    
    @Override
    public boolean isLeaf() {
        return this.leaf;
    }
    
    @Override
    public int getNumChildren() {
        return (this.children != null) ? this.children.size() : 0;
    }
    
    @Override
    public TreeTableNode getChild(final int idx) {
        return this.children.get(idx);
    }
    
    @Override
    public int getChildIndex(final TreeTableNode child) {
        if (this.children != null) {
            for (int i = 0, n = this.children.size(); i < n; ++i) {
                if (this.children.get(i) == child) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    protected void setLeaf(final boolean leaf) {
        if (this.leaf != leaf) {
            this.leaf = leaf;
            this.fireNodeChanged();
        }
    }
    
    protected void insertChild(final TreeTableNode node, final int idx) {
        assert this.getChildIndex(node) < 0;
        assert node.getParent() == this;
        if (this.children == null) {
            this.children = new ArrayList<TreeTableNode>();
        }
        this.children.add(idx, node);
        this.getTreeTableModel().fireNodesAdded((TreeTableNode)this, idx, 1);
    }
    
    protected void removeChild(final int idx) {
        this.children.remove(idx);
        this.getTreeTableModel().fireNodesRemoved((TreeTableNode)this, idx, 1);
    }
    
    protected void removeAllChildren() {
        if (this.children != null) {
            final int count = this.children.size();
            this.children.clear();
            this.getTreeTableModel().fireNodesRemoved((TreeTableNode)this, 0, count);
        }
    }
    
    protected AbstractTreeTableModel getTreeTableModel() {
        TreeTableNode n = this.parent;
        while (true) {
            final TreeTableNode p = n.getParent();
            if (p == null) {
                break;
            }
            n = p;
        }
        return (AbstractTreeTableModel)n;
    }
    
    protected void fireNodeChanged() {
        final int selfIdxInParent = this.parent.getChildIndex(this);
        if (selfIdxInParent >= 0) {
            this.getTreeTableModel().fireNodesChanged(this.parent, selfIdxInParent, 1);
        }
    }
}
