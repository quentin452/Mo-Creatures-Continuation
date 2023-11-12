package de.matthiasmann.twl.model;

public interface TreeTableNode
{
    Object getData(final int p0);
    
    Object getTooltipContent(final int p0);
    
    TreeTableNode getParent();
    
    boolean isLeaf();
    
    int getNumChildren();
    
    TreeTableNode getChild(final int p0);
    
    int getChildIndex(final TreeTableNode p0);
}
