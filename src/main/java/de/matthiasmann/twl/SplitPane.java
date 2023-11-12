package de.matthiasmann.twl;

public class SplitPane extends Widget
{
    public static final int CENTER = -1;
    public static final int MIN_SIZE = -2;
    public static final int PREFERRED_SIZE = -3;
    private final DraggableButton splitter;
    private Direction direction;
    private int splitPosition;
    private boolean reverseSplitPosition;
    private boolean respectMinSizes;
    
    public SplitPane() {
        this.splitPosition = -1;
        (this.splitter = new DraggableButton()).setCanAcceptKeyboardFocus(false);
        this.splitter.setListener((DraggableButton.DragListener)new DraggableButton.DragListener() {
            int initialPos;
            
            public void dragStarted() {
                this.initialPos = SplitPane.this.getEffectiveSplitPosition();
            }
            
            public void dragged(final int deltaX, final int deltaY) {
                SplitPane.this.dragged(this.initialPos, deltaX, deltaY);
            }
            
            public void dragStopped() {
            }
        });
        this.setDirection(Direction.HORIZONTAL);
        this.add((Widget)this.splitter);
    }
    
    public Direction getDirection() {
        return this.direction;
    }
    
    public void setDirection(final Direction direction) {
        if (direction == null) {
            throw new NullPointerException("direction");
        }
        this.direction = direction;
        this.splitter.setTheme(direction.splitterTheme);
    }
    
    public int getMaxSplitPosition() {
        return Math.max(0, this.direction.get(this.getInnerWidth() - this.splitter.getPreferredWidth(), this.getInnerHeight() - this.splitter.getPreferredHeight()));
    }
    
    public int getSplitPosition() {
        return this.splitPosition;
    }
    
    public void setSplitPosition(final int pos) {
        if (pos < -3) {
            throw new IllegalArgumentException("pos");
        }
        this.splitPosition = pos;
        this.invalidateLayoutLocally();
    }
    
    public boolean getReverseSplitPosition() {
        return this.reverseSplitPosition;
    }
    
    public void setReverseSplitPosition(final boolean reverseSplitPosition) {
        if (this.reverseSplitPosition != reverseSplitPosition) {
            this.reverseSplitPosition = reverseSplitPosition;
            this.invalidateLayoutLocally();
        }
    }
    
    public boolean isRespectMinSizes() {
        return this.respectMinSizes;
    }
    
    public void setRespectMinSizes(final boolean respectMinSizes) {
        if (this.respectMinSizes != respectMinSizes) {
            this.respectMinSizes = respectMinSizes;
            this.invalidateLayoutLocally();
        }
    }
    
    void dragged(final int initialPos, final int deltaX, final int deltaY) {
        int delta = this.direction.get(deltaX, deltaY);
        if (this.reverseSplitPosition) {
            delta = -delta;
        }
        this.setSplitPosition(this.clamp(initialPos + delta));
    }
    
    @Override
    protected void childRemoved(final Widget exChild) {
        super.childRemoved(exChild);
        if (exChild == this.splitter) {
            this.add((Widget)this.splitter);
        }
    }
    
    @Override
    protected void childAdded(final Widget child) {
        super.childAdded(child);
        final int numChildren = this.getNumChildren();
        if (numChildren > 0 && this.getChild(numChildren - 1) != this.splitter) {
            this.moveChild(this.getChildIndex((Widget)this.splitter), numChildren - 1);
        }
    }
    
    @Override
    public int getMinWidth() {
        int min;
        if (this.direction == Direction.HORIZONTAL) {
            min = BoxLayout.computeMinWidthHorizontal((Widget)this, 0);
        }
        else {
            min = BoxLayout.computeMinWidthVertical((Widget)this);
        }
        return Math.max(super.getMinWidth(), min);
    }
    
    @Override
    public int getMinHeight() {
        int min;
        if (this.direction == Direction.HORIZONTAL) {
            min = BoxLayout.computeMinHeightHorizontal((Widget)this);
        }
        else {
            min = BoxLayout.computeMinHeightVertical((Widget)this, 0);
        }
        return Math.max(super.getMinHeight(), min);
    }
    
    @Override
    public int getPreferredInnerWidth() {
        if (this.direction == Direction.HORIZONTAL) {
            return BoxLayout.computePreferredWidthHorizontal((Widget)this, 0);
        }
        return BoxLayout.computePreferredWidthVertical((Widget)this);
    }
    
    @Override
    public int getPreferredInnerHeight() {
        if (this.direction == Direction.HORIZONTAL) {
            return BoxLayout.computePreferredHeightHorizontal((Widget)this);
        }
        return BoxLayout.computePreferredHeightVertical((Widget)this, 0);
    }
    
    @Override
    protected void layout() {
        Widget a = null;
        Widget b = null;
        for (int i = 0; i < this.getNumChildren(); ++i) {
            final Widget w = this.getChild(i);
            if (w != this.splitter) {
                if (a != null) {
                    b = w;
                    break;
                }
                a = w;
            }
        }
        final int innerX = this.getInnerX();
        final int innerY = this.getInnerY();
        int splitPos = this.getEffectiveSplitPosition();
        if (this.reverseSplitPosition) {
            splitPos = this.getMaxSplitPosition() - splitPos;
        }
        switch (this.direction) {
            case HORIZONTAL: {
                final int innerHeight = this.getInnerHeight();
                this.splitter.setPosition(innerX + splitPos, innerY);
                this.splitter.setSize(this.splitter.getPreferredWidth(), innerHeight);
                if (a != null) {
                    a.setPosition(innerX, innerY);
                    a.setSize(splitPos, innerHeight);
                }
                if (b != null) {
                    b.setPosition(this.splitter.getRight(), innerY);
                    b.setSize(Math.max(0, this.getInnerRight() - this.splitter.getRight()), innerHeight);
                    break;
                }
                break;
            }
            case VERTICAL: {
                final int innerWidth = this.getInnerWidth();
                this.splitter.setPosition(innerX, innerY + splitPos);
                this.splitter.setSize(innerWidth, this.splitter.getPreferredHeight());
                if (a != null) {
                    a.setPosition(innerX, innerY);
                    a.setSize(innerWidth, splitPos);
                }
                if (b != null) {
                    b.setPosition(innerX, this.splitter.getBottom());
                    b.setSize(innerWidth, Math.max(0, this.getInnerBottom() - this.splitter.getBottom()));
                    break;
                }
                break;
            }
        }
    }
    
    int getEffectiveSplitPosition() {
        final int maxSplitPosition = this.getMaxSplitPosition();
        int pos = this.splitPosition;
        switch (pos) {
            case -1: {
                pos = maxSplitPosition / 2;
                break;
            }
            case -2: {
                final Widget w = this.getPrimaryWidget();
                if (w != null) {
                    pos = this.direction.getMinSize(w);
                    break;
                }
                pos = maxSplitPosition / 2;
                break;
            }
            case -3: {
                final Widget w = this.getPrimaryWidget();
                if (w != null) {
                    pos = this.direction.getPrefSize(w);
                    break;
                }
                pos = maxSplitPosition / 2;
                break;
            }
        }
        int minValue = 0;
        int maxValue = maxSplitPosition;
        if (this.respectMinSizes) {
            Widget a = null;
            Widget b = null;
            for (int i = 0; i < this.getNumChildren(); ++i) {
                final Widget w2 = this.getChild(i);
                if (w2 != this.splitter) {
                    if (a != null) {
                        b = w2;
                        break;
                    }
                    a = w2;
                }
            }
            final int aMinSize = (a != null) ? this.direction.getMinSize(a) : 0;
            final int bMinSize = (b != null) ? this.direction.getMinSize(b) : 0;
            if (this.reverseSplitPosition) {
                minValue = bMinSize;
                maxValue = Math.max(0, maxSplitPosition - aMinSize);
            }
            else {
                minValue = aMinSize;
                maxValue = Math.max(0, maxSplitPosition - bMinSize);
            }
        }
        return Math.max(minValue, Math.min(maxValue, pos));
    }
    
    private Widget getPrimaryWidget() {
        final int idx = this.reverseSplitPosition ? 1 : 0;
        if (this.getNumChildren() > idx) {
            return this.getChild(idx);
        }
        return null;
    }
    
    private int clamp(final int pos) {
        return Math.max(0, Math.min(this.getMaxSplitPosition(), pos));
    }
    
    public enum Direction
    {
        HORIZONTAL("splitterHorizontal") {
            @Override
            int get(final int x, final int y) {
                return x;
            }
            
            @Override
            int getMinSize(final Widget w) {
                return w.getMinWidth();
            }
            
            @Override
            int getPrefSize(final Widget w) {
                return w.getPreferredWidth();
            }
        }, 
        VERTICAL("splitterVertical") {
            @Override
            int get(final int x, final int y) {
                return y;
            }
            
            @Override
            int getMinSize(final Widget w) {
                return w.getMinHeight();
            }
            
            @Override
            int getPrefSize(final Widget w) {
                return w.getPreferredHeight();
            }
        };
        
        final String splitterTheme;
        
        private Direction(final String splitterTheme) {
            this.splitterTheme = splitterTheme;
        }
        
        abstract int get(final int p0, final int p1);
        
        abstract int getMinSize(final Widget p0);
        
        abstract int getPrefSize(final Widget p0);
    }
}
