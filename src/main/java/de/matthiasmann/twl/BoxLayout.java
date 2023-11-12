package de.matthiasmann.twl;

public class BoxLayout extends Widget
{
    private Direction direction;
    private int spacing;
    private boolean scroll;
    private Alignment alignment;
    
    public BoxLayout() {
        this(Direction.HORIZONTAL);
    }
    
    public BoxLayout(final Direction direction) {
        this.alignment = Alignment.TOP;
        this.direction = direction;
    }
    
    public int getSpacing() {
        return this.spacing;
    }
    
    public void setSpacing(final int spacing) {
        if (this.spacing != spacing) {
            this.spacing = spacing;
            this.invalidateLayout();
        }
    }
    
    public boolean isScroll() {
        return this.scroll;
    }
    
    public void setScroll(final boolean scroll) {
        if (this.scroll != scroll) {
            this.scroll = scroll;
            this.invalidateLayout();
        }
    }
    
    public Alignment getAlignment() {
        return this.alignment;
    }
    
    public void setAlignment(final Alignment alignment) {
        if (alignment == null) {
            throw new NullPointerException("alignment");
        }
        if (this.alignment != alignment) {
            this.alignment = alignment;
            this.invalidateLayout();
        }
    }
    
    public Direction getDirection() {
        return this.direction;
    }
    
    public void setDirection(final Direction direction) {
        if (direction == null) {
            throw new NullPointerException("direction");
        }
        if (this.direction != direction) {
            this.direction = direction;
            this.invalidateLayout();
        }
    }
    
    @Override
    public int getMinWidth() {
        final int minWidth = (this.direction == Direction.HORIZONTAL) ? computeMinWidthHorizontal(this, this.spacing) : computeMinWidthVertical(this);
        return Math.max(super.getMinWidth(), minWidth + this.getBorderHorizontal());
    }
    
    @Override
    public int getMinHeight() {
        final int minHeight = (this.direction == Direction.HORIZONTAL) ? computeMinHeightHorizontal(this) : computeMinHeightVertical(this, this.spacing);
        return Math.max(super.getMinHeight(), minHeight + this.getBorderVertical());
    }
    
    @Override
    public int getPreferredInnerWidth() {
        return (this.direction == Direction.HORIZONTAL) ? computePreferredWidthHorizontal(this, this.spacing) : computePreferredWidthVertical(this);
    }
    
    @Override
    public int getPreferredInnerHeight() {
        return (this.direction == Direction.HORIZONTAL) ? computePreferredHeightHorizontal(this) : computePreferredHeightVertical(this, this.spacing);
    }
    
    @Override
    protected void applyTheme(final ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        this.setSpacing(themeInfo.getParameter("spacing", 0));
        this.setAlignment(themeInfo.getParameter("alignment", Alignment.TOP));
    }
    
    public static int computeMinWidthHorizontal(final Widget container, final int spacing) {
        final int n = container.getNumChildren();
        int minWidth = Math.max(0, n - 1) * spacing;
        for (int i = 0; i < n; ++i) {
            minWidth += container.getChild(i).getMinWidth();
        }
        return minWidth;
    }
    
    public static int computeMinHeightHorizontal(final Widget container) {
        final int n = container.getNumChildren();
        int minHeight = 0;
        for (int i = 0; i < n; ++i) {
            minHeight = Math.max(minHeight, container.getChild(i).getMinHeight());
        }
        return minHeight;
    }
    
    public static int computePreferredWidthHorizontal(final Widget container, final int spacing) {
        final int n = container.getNumChildren();
        int prefWidth = Math.max(0, n - 1) * spacing;
        for (int i = 0; i < n; ++i) {
            prefWidth += getPrefChildWidth(container.getChild(i));
        }
        return prefWidth;
    }
    
    public static int computePreferredHeightHorizontal(final Widget container) {
        final int n = container.getNumChildren();
        int prefHeight = 0;
        for (int i = 0; i < n; ++i) {
            prefHeight = Math.max(prefHeight, getPrefChildHeight(container.getChild(i)));
        }
        return prefHeight;
    }
    
    public static int computeMinWidthVertical(final Widget container) {
        final int n = container.getNumChildren();
        int minWidth = 0;
        for (int i = 0; i < n; ++i) {
            minWidth = Math.max(minWidth, container.getChild(i).getMinWidth());
        }
        return minWidth;
    }
    
    public static int computeMinHeightVertical(final Widget container, final int spacing) {
        final int n = container.getNumChildren();
        int minHeight = Math.max(0, n - 1) * spacing;
        for (int i = 0; i < n; ++i) {
            minHeight += container.getChild(i).getMinHeight();
        }
        return minHeight;
    }
    
    public static int computePreferredWidthVertical(final Widget container) {
        final int n = container.getNumChildren();
        int prefWidth = 0;
        for (int i = 0; i < n; ++i) {
            prefWidth = Math.max(prefWidth, getPrefChildWidth(container.getChild(i)));
        }
        return prefWidth;
    }
    
    public static int computePreferredHeightVertical(final Widget container, final int spacing) {
        final int n = container.getNumChildren();
        int prefHeight = Math.max(0, n - 1) * spacing;
        for (int i = 0; i < n; ++i) {
            prefHeight += getPrefChildHeight(container.getChild(i));
        }
        return prefHeight;
    }
    
    public static void layoutHorizontal(final Widget container, final int spacing, final Alignment alignment, final boolean scroll) {
        final int numChildren = container.getNumChildren();
        final int height = container.getInnerHeight();
        int x = container.getInnerX();
        final int y = container.getInnerY();
        if (scroll) {
            final int width = computePreferredWidthHorizontal(container, spacing);
            if (width > container.getInnerWidth()) {
                x -= width - container.getInnerWidth();
            }
        }
        for (int idx = 0; idx < numChildren; ++idx) {
            final Widget child = container.getChild(idx);
            final int childWidth = getPrefChildWidth(child);
            final int childHeight = (alignment == Alignment.FILL) ? height : getPrefChildHeight(child);
            final int yoff = (height - childHeight) * alignment.vpos / 2;
            child.setSize(childWidth, childHeight);
            child.setPosition(x, y + yoff);
            x += childWidth + spacing;
        }
    }
    
    public static void layoutVertical(final Widget container, final int spacing, final Alignment alignment, final boolean scroll) {
        final int numChildren = container.getNumChildren();
        final int width = container.getInnerWidth();
        int x = container.getInnerX();
        int y = container.getInnerY();
        if (scroll) {
            final int height = computePreferredHeightVertical(container, spacing);
            if (height > container.getInnerHeight()) {
                x -= height - container.getInnerHeight();
            }
        }
        for (int idx = 0; idx < numChildren; ++idx) {
            final Widget child = container.getChild(idx);
            final int childWidth = (alignment == Alignment.FILL) ? width : getPrefChildWidth(child);
            final int childHeight = getPrefChildHeight(child);
            final int xoff = (width - childWidth) * alignment.hpos / 2;
            child.setSize(childWidth, childHeight);
            child.setPosition(x + xoff, y);
            y += childHeight + spacing;
        }
    }
    
    @Override
    protected void layout() {
        if (this.getNumChildren() > 0) {
            if (this.direction == Direction.HORIZONTAL) {
                layoutHorizontal(this, this.spacing, this.alignment, this.scroll);
            }
            else {
                layoutVertical(this, this.spacing, this.alignment, this.scroll);
            }
        }
    }
    
    private static int getPrefChildWidth(final Widget child) {
        return Widget.computeSize(child.getMinWidth(), child.getPreferredWidth(), child.getMaxWidth());
    }
    
    private static int getPrefChildHeight(final Widget child) {
        return Widget.computeSize(child.getMinHeight(), child.getPreferredHeight(), child.getMaxHeight());
    }
    
    public enum Direction
    {
        HORIZONTAL, 
        VERTICAL;
    }
}
