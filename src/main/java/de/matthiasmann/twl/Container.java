package de.matthiasmann.twl;

public class Container extends Widget
{
    @Override
    public int getMinWidth() {
        return Math.max(super.getMinWidth(), this.getBorderHorizontal() + BoxLayout.computeMinWidthVertical((Widget)this));
    }
    
    @Override
    public int getMinHeight() {
        return Math.max(super.getMinHeight(), this.getBorderVertical() + BoxLayout.computeMinHeightHorizontal((Widget)this));
    }
    
    @Override
    public int getPreferredInnerWidth() {
        return BoxLayout.computePreferredWidthVertical((Widget)this);
    }
    
    @Override
    public int getPreferredInnerHeight() {
        return BoxLayout.computePreferredHeightHorizontal((Widget)this);
    }
    
    @Override
    protected void layout() {
        this.layoutChildrenFullInnerArea();
    }
}
