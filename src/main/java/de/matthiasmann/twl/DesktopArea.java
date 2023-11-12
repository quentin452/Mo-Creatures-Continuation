package de.matthiasmann.twl;

public class DesktopArea extends Widget
{
    public DesktopArea() {
        this.setFocusKeyEnabled(false);
    }
    
    @Override
    protected void keyboardFocusChildChanged(final Widget child) {
        super.keyboardFocusChildChanged(child);
        if (child != null) {
            final int fromIdx = this.getChildIndex(child);
            assert fromIdx >= 0;
            final int numChildren = this.getNumChildren();
            if (fromIdx < numChildren - 1) {
                this.moveChild(fromIdx, numChildren - 1);
            }
        }
    }
    
    @Override
    protected void layout() {
        this.restrictChildrenToInnerArea();
    }
    
    protected void restrictChildrenToInnerArea() {
        final int top = this.getInnerY();
        final int left = this.getInnerX();
        final int right = this.getInnerRight();
        final int bottom = this.getInnerBottom();
        final int width = Math.max(0, right - left);
        final int height = Math.max(0, bottom - top);
        for (int i = 0, n = this.getNumChildren(); i < n; ++i) {
            final Widget w = this.getChild(i);
            w.setSize(Math.min(Math.max(width, w.getMinWidth()), w.getWidth()), Math.min(Math.max(height, w.getMinHeight()), w.getHeight()));
            w.setPosition(Math.max(left, Math.min(right - w.getWidth(), w.getX())), Math.max(top, Math.min(bottom - w.getHeight(), w.getY())));
        }
    }
}
