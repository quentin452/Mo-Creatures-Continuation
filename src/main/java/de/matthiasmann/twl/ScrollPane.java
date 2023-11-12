package de.matthiasmann.twl;

import de.matthiasmann.twl.renderer.*;

public class ScrollPane extends Widget
{
    public static final AnimationState.StateKey STATE_DOWNARROW_ARMED;
    public static final AnimationState.StateKey STATE_RIGHTARROW_ARMED;
    public static final AnimationState.StateKey STATE_HORIZONTAL_SCROLLBAR_VISIBLE;
    public static final AnimationState.StateKey STATE_VERTICAL_SCROLLBAR_VISIBLE;
    public static final AnimationState.StateKey STATE_AUTO_SCROLL_UP;
    public static final AnimationState.StateKey STATE_AUTO_SCROLL_DOWN;
    private static final int AUTO_SCROLL_DELAY = 50;
    final Scrollbar scrollbarH;
    final Scrollbar scrollbarV;
    private final Widget contentArea;
    private DraggableButton dragButton;
    private Widget content;
    private Fixed fixed;
    private Dimension hscrollbarOffset;
    private Dimension vscrollbarOffset;
    private Dimension contentScrollbarSpacing;
    private boolean inLayout;
    private boolean expandContentSize;
    private boolean scrollbarsAlwaysVisible;
    private int scrollbarsToggleFlags;
    private int autoScrollArea;
    private int autoScrollSpeed;
    private Timer autoScrollTimer;
    private int autoScrollDirection;
    
    public ScrollPane() {
        this((Widget)null);
    }
    
    public ScrollPane(final Widget content) {
        this.fixed = Fixed.NONE;
        this.hscrollbarOffset = Dimension.ZERO;
        this.vscrollbarOffset = Dimension.ZERO;
        this.contentScrollbarSpacing = Dimension.ZERO;
        this.scrollbarH = new Scrollbar(Scrollbar.Orientation.HORIZONTAL);
        this.scrollbarV = new Scrollbar(Scrollbar.Orientation.VERTICAL);
        this.contentArea = new Widget();
        final Runnable cb = new Runnable() {
            @Override
            public void run() {
                ScrollPane.this.scrollContent();
            }
        };
        this.scrollbarH.addCallback(cb);
        this.scrollbarH.setVisible(false);
        this.scrollbarV.addCallback(cb);
        this.scrollbarV.setVisible(false);
        this.contentArea.setClip(true);
        this.contentArea.setTheme("");
        super.insertChild(this.contentArea, 0);
        super.insertChild((Widget)this.scrollbarH, 1);
        super.insertChild((Widget)this.scrollbarV, 2);
        this.setContent(content);
        this.setCanAcceptKeyboardFocus(true);
    }
    
    public Fixed getFixed() {
        return this.fixed;
    }
    
    public void setFixed(final Fixed fixed) {
        if (fixed == null) {
            throw new NullPointerException("fixed");
        }
        if (this.fixed != fixed) {
            this.fixed = fixed;
            this.invalidateLayout();
        }
    }
    
    public Widget getContent() {
        return this.content;
    }
    
    public void setContent(final Widget content) {
        if (this.content != null) {
            this.contentArea.removeAllChildren();
            this.content = null;
        }
        if (content != null) {
            this.content = content;
            this.contentArea.add(content);
        }
    }
    
    public boolean isExpandContentSize() {
        return this.expandContentSize;
    }
    
    public void setExpandContentSize(final boolean expandContentSize) {
        if (this.expandContentSize != expandContentSize) {
            this.expandContentSize = expandContentSize;
            this.invalidateLayoutLocally();
        }
    }
    
    public void updateScrollbarSizes() {
        this.invalidateLayoutLocally();
        this.validateLayout();
    }
    
    public int getScrollPositionX() {
        return this.scrollbarH.getValue();
    }
    
    public int getMaxScrollPosX() {
        return this.scrollbarH.getMaxValue();
    }
    
    public void setScrollPositionX(final int pos) {
        this.scrollbarH.setValue(pos);
    }
    
    public void scrollToAreaX(final int start, final int size, final int extra) {
        this.scrollbarH.scrollToArea(start, size, extra);
    }
    
    public int getScrollPositionY() {
        return this.scrollbarV.getValue();
    }
    
    public int getMaxScrollPosY() {
        return this.scrollbarV.getMaxValue();
    }
    
    public void setScrollPositionY(final int pos) {
        this.scrollbarV.setValue(pos);
    }
    
    public void scrollToAreaY(final int start, final int size, final int extra) {
        this.scrollbarV.scrollToArea(start, size, extra);
    }
    
    public int getContentAreaWidth() {
        return this.contentArea.getWidth();
    }
    
    public int getContentAreaHeight() {
        return this.contentArea.getHeight();
    }
    
    public Scrollbar getHorizontalScrollbar() {
        return this.scrollbarH;
    }
    
    public Scrollbar getVerticalScrollbar() {
        return this.scrollbarV;
    }
    
    public DraggableButton.DragListener createDragListener() {
        return (DraggableButton.DragListener)new DraggableButton.DragListener() {
            int startScrollX;
            int startScrollY;
            
            public void dragStarted() {
                this.startScrollX = ScrollPane.this.getScrollPositionX();
                this.startScrollY = ScrollPane.this.getScrollPositionY();
            }
            
            public void dragged(final int deltaX, final int deltaY) {
                ScrollPane.this.setScrollPositionX(this.startScrollX - deltaX);
                ScrollPane.this.setScrollPositionY(this.startScrollY - deltaY);
            }
            
            public void dragStopped() {
            }
        };
    }
    
    public boolean checkAutoScroll(final Event evt) {
        final GUI gui = this.getGUI();
        if (gui == null) {
            this.stopAutoScroll();
            return false;
        }
        this.autoScrollDirection = this.getAutoScrollDirection(evt);
        if (this.autoScrollDirection == 0) {
            this.stopAutoScroll();
            return false;
        }
        this.setAutoScrollMarker();
        if (this.autoScrollTimer == null) {
            (this.autoScrollTimer = gui.createTimer()).setContinuous(true);
            this.autoScrollTimer.setDelay(50);
            this.autoScrollTimer.setCallback(new Runnable() {
                @Override
                public void run() {
                    ScrollPane.this.doAutoScroll();
                }
            });
            this.doAutoScroll();
        }
        this.autoScrollTimer.start();
        return true;
    }
    
    public void stopAutoScroll() {
        if (this.autoScrollTimer != null) {
            this.autoScrollTimer.stop();
        }
        this.autoScrollDirection = 0;
        this.setAutoScrollMarker();
    }
    
    public static ScrollPane getContainingScrollPane(final Widget widget) {
        final Widget ca = widget.getParent();
        if (ca != null) {
            final Widget sp = ca.getParent();
            if (sp instanceof ScrollPane) {
                final ScrollPane scrollPane = (ScrollPane)sp;
                assert scrollPane.getContent() == widget;
                return scrollPane;
            }
        }
        return null;
    }
    
    @Override
    public int getMinWidth() {
        int minWidth = super.getMinWidth();
        final int border = this.getBorderHorizontal();
        if (this.fixed == Fixed.HORIZONTAL && this.content != null) {
            minWidth = Math.max(minWidth, this.content.getMinWidth() + border + this.scrollbarV.getMinWidth());
        }
        return minWidth;
    }
    
    @Override
    public int getMinHeight() {
        int minHeight = super.getMinHeight();
        final int border = this.getBorderVertical();
        if (this.fixed == Fixed.VERTICAL && this.content != null) {
            minHeight = Math.max(minHeight, this.content.getMinHeight() + border + this.scrollbarH.getMinHeight());
        }
        return minHeight;
    }
    
    @Override
    public int getPreferredInnerWidth() {
        if (this.content != null) {
            switch (this.fixed) {
                case HORIZONTAL: {
                    int prefWidth = Widget.computeSize(this.content.getMinWidth(), this.content.getPreferredWidth(), this.content.getMaxWidth());
                    if (this.scrollbarV.isVisible()) {
                        prefWidth += this.scrollbarV.getPreferredWidth();
                    }
                    return prefWidth;
                }
                case VERTICAL: {
                    return this.content.getPreferredWidth();
                }
            }
        }
        return 0;
    }
    
    @Override
    public int getPreferredInnerHeight() {
        if (this.content != null) {
            switch (this.fixed) {
                case HORIZONTAL: {
                    return this.content.getPreferredHeight();
                }
                case VERTICAL: {
                    int prefHeight = Widget.computeSize(this.content.getMinHeight(), this.content.getPreferredHeight(), this.content.getMaxHeight());
                    if (this.scrollbarH.isVisible()) {
                        prefHeight += this.scrollbarH.getPreferredHeight();
                    }
                    return prefHeight;
                }
            }
        }
        return 0;
    }
    
    @Override
    public void insertChild(final Widget child, final int index) {
        throw new UnsupportedOperationException("use setContent");
    }
    
    @Override
    public void removeAllChildren() {
        throw new UnsupportedOperationException("use setContent");
    }
    
    @Override
    public Widget removeChild(final int index) {
        throw new UnsupportedOperationException("use setContent");
    }
    
    @Override
    protected void applyTheme(final ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        this.applyThemeScrollPane(themeInfo);
    }
    
    protected void applyThemeScrollPane(final ThemeInfo themeInfo) {
        this.autoScrollArea = themeInfo.getParameter("autoScrollArea", 5);
        this.autoScrollSpeed = themeInfo.getParameter("autoScrollSpeed", this.autoScrollArea * 2);
        this.hscrollbarOffset = (Dimension)themeInfo.getParameterValue("hscrollbarOffset", false, (Class)Dimension.class, (Object)Dimension.ZERO);
        this.vscrollbarOffset = (Dimension)themeInfo.getParameterValue("vscrollbarOffset", false, (Class)Dimension.class, (Object)Dimension.ZERO);
        this.contentScrollbarSpacing = (Dimension)themeInfo.getParameterValue("contentScrollbarSpacing", false, (Class)Dimension.class, (Object)Dimension.ZERO);
        this.scrollbarsAlwaysVisible = themeInfo.getParameter("scrollbarsAlwaysVisible", false);
        final boolean hasDragButton = themeInfo.getParameter("hasDragButton", false);
        if (hasDragButton && this.dragButton == null) {
            (this.dragButton = new DraggableButton()).setTheme("dragButton");
            this.dragButton.setListener((DraggableButton.DragListener)new DraggableButton.DragListener() {
                public void dragStarted() {
                    ScrollPane.this.scrollbarH.externalDragStart();
                    ScrollPane.this.scrollbarV.externalDragStart();
                }
                
                public void dragged(final int deltaX, final int deltaY) {
                    ScrollPane.this.scrollbarH.externalDragged(deltaX, deltaY);
                    ScrollPane.this.scrollbarV.externalDragged(deltaX, deltaY);
                }
                
                public void dragStopped() {
                    ScrollPane.this.scrollbarH.externalDragStopped();
                    ScrollPane.this.scrollbarV.externalDragStopped();
                }
            });
            super.insertChild((Widget)this.dragButton, 3);
        }
        else if (!hasDragButton && this.dragButton != null) {
            assert super.getChild(3) == this.dragButton;
            super.removeChild(3);
            this.dragButton = null;
        }
    }
    
    protected int getAutoScrollDirection(final Event evt) {
        if (this.content instanceof AutoScrollable) {
            return ((AutoScrollable)this.content).getAutoScrollDirection(evt, this.autoScrollArea);
        }
        if (this.contentArea.isMouseInside(evt)) {
            final int mouseY = evt.getMouseY();
            final int areaY = this.contentArea.getY();
            if (mouseY - areaY <= this.autoScrollArea || this.contentArea.getBottom() - mouseY <= this.autoScrollArea) {
                if (mouseY < areaY + this.contentArea.getHeight() / 2) {
                    return -1;
                }
                return 1;
            }
        }
        return 0;
    }
    
    @Override
    public void validateLayout() {
        if (!this.inLayout) {
            try {
                this.inLayout = true;
                if (this.content != null) {
                    this.content.validateLayout();
                }
                super.validateLayout();
            }
            finally {
                this.inLayout = false;
            }
        }
    }
    
    @Override
    protected void childInvalidateLayout(final Widget child) {
        if (child == this.contentArea) {
            this.invalidateLayoutLocally();
        }
        else {
            super.childInvalidateLayout(child);
        }
    }
    
    @Override
    protected void paintWidget(final GUI gui) {
        this.scrollbarsToggleFlags = 0;
    }
    
    @Override
    protected void layout() {
        if (this.content != null) {
            int innerWidth = this.getInnerWidth();
            int innerHeight = this.getInnerHeight();
            int availWidth = innerWidth;
            int availHeight = innerHeight;
            innerWidth += this.vscrollbarOffset.getX();
            innerHeight += this.hscrollbarOffset.getY();
            final int scrollbarHX = this.hscrollbarOffset.getX();
            int scrollbarHY = innerHeight;
            int scrollbarVX = innerWidth;
            final int scrollbarVY = this.vscrollbarOffset.getY();
            boolean visibleH = false;
            boolean visibleV = false;
            int requiredWidth = 0;
            int requiredHeight = 0;
            switch (this.fixed) {
                case HORIZONTAL: {
                    requiredWidth = availWidth;
                    requiredHeight = this.content.getPreferredHeight();
                    break;
                }
                case VERTICAL: {
                    requiredWidth = this.content.getPreferredWidth();
                    requiredHeight = availHeight;
                    break;
                }
                default: {
                    requiredWidth = this.content.getPreferredWidth();
                    requiredHeight = this.content.getPreferredHeight();
                    break;
                }
            }
            int hScrollbarMax = 0;
            int vScrollbarMax = 0;
            if (availWidth > 0 && availHeight > 0) {
                boolean repeat;
                do {
                    repeat = false;
                    if (this.fixed != Fixed.HORIZONTAL) {
                        hScrollbarMax = Math.max(0, requiredWidth - availWidth);
                        if (hScrollbarMax > 0 || this.scrollbarsAlwaysVisible || (this.scrollbarsToggleFlags & 0x3) == 0x3) {
                            repeat |= !visibleH;
                            visibleH = true;
                            final int prefHeight = this.scrollbarH.getPreferredHeight();
                            scrollbarHY = innerHeight - prefHeight;
                            availHeight = Math.max(0, scrollbarHY - this.contentScrollbarSpacing.getY());
                        }
                    }
                    else {
                        hScrollbarMax = 0;
                        requiredWidth = availWidth;
                    }
                    if (this.fixed != Fixed.VERTICAL) {
                        vScrollbarMax = Math.max(0, requiredHeight - availHeight);
                        if (vScrollbarMax <= 0 && !this.scrollbarsAlwaysVisible && (this.scrollbarsToggleFlags & 0xC) != 0xC) {
                            continue;
                        }
                        repeat |= !visibleV;
                        visibleV = true;
                        final int prefWidth = this.scrollbarV.getPreferredWidth();
                        scrollbarVX = innerWidth - prefWidth;
                        availWidth = Math.max(0, scrollbarVX - this.contentScrollbarSpacing.getX());
                    }
                    else {
                        vScrollbarMax = 0;
                        requiredHeight = availHeight;
                    }
                } while (repeat);
            }
            if (visibleH && !this.scrollbarH.isVisible()) {
                this.scrollbarsToggleFlags |= 0x1;
            }
            if (!visibleH && this.scrollbarH.isVisible()) {
                this.scrollbarsToggleFlags |= 0x2;
            }
            if (visibleV && !this.scrollbarV.isVisible()) {
                this.scrollbarsToggleFlags |= 0x4;
            }
            if (!visibleV && this.scrollbarV.isVisible()) {
                this.scrollbarsToggleFlags |= 0x8;
            }
            if (visibleH != this.scrollbarH.isVisible() || visibleV != this.scrollbarV.isVisible()) {
                this.invalidateLayoutLocally();
            }
            int pageSizeX;
            int pageSizeY;
            if (this.content instanceof CustomPageSize) {
                final CustomPageSize customPageSize = (CustomPageSize)this.content;
                pageSizeX = customPageSize.getPageSizeX(availWidth);
                pageSizeY = customPageSize.getPageSizeY(availHeight);
            }
            else {
                pageSizeX = availWidth;
                pageSizeY = availHeight;
            }
            this.scrollbarH.setVisible(visibleH);
            this.scrollbarH.setMinMaxValue(0, hScrollbarMax);
            this.scrollbarH.setSize(Math.max(0, scrollbarVX - scrollbarHX), Math.max(0, innerHeight - scrollbarHY));
            this.scrollbarH.setPosition(this.getInnerX() + scrollbarHX, this.getInnerY() + scrollbarHY);
            this.scrollbarH.setPageSize(Math.max(1, pageSizeX));
            this.scrollbarH.setStepSize(Math.max(1, pageSizeX / 10));
            this.scrollbarV.setVisible(visibleV);
            this.scrollbarV.setMinMaxValue(0, vScrollbarMax);
            this.scrollbarV.setSize(Math.max(0, innerWidth - scrollbarVX), Math.max(0, scrollbarHY - scrollbarVY));
            this.scrollbarV.setPosition(this.getInnerX() + scrollbarVX, this.getInnerY() + scrollbarVY);
            this.scrollbarV.setPageSize(Math.max(1, pageSizeY));
            this.scrollbarV.setStepSize(Math.max(1, pageSizeY / 10));
            if (this.dragButton != null) {
                this.dragButton.setVisible(visibleH && visibleV);
                this.dragButton.setSize(Math.max(0, innerWidth - scrollbarVX), Math.max(0, innerHeight - scrollbarHY));
                this.dragButton.setPosition(this.getInnerX() + scrollbarVX, this.getInnerY() + scrollbarHY);
            }
            this.contentArea.setPosition(this.getInnerX(), this.getInnerY());
            this.contentArea.setSize(availWidth, availHeight);
            if (this.content instanceof Scrollable) {
                this.content.setPosition(this.contentArea.getX(), this.contentArea.getY());
                this.content.setSize(availWidth, availHeight);
            }
            else if (this.expandContentSize) {
                this.content.setSize(Math.max(availWidth, requiredWidth), Math.max(availHeight, requiredHeight));
            }
            else {
                this.content.setSize(Math.max(0, requiredWidth), Math.max(0, requiredHeight));
            }
            final de.matthiasmann.twl.AnimationState animationState = this.getAnimationState();
            animationState.setAnimationState(ScrollPane.STATE_HORIZONTAL_SCROLLBAR_VISIBLE, visibleH);
            animationState.setAnimationState(ScrollPane.STATE_VERTICAL_SCROLLBAR_VISIBLE, visibleV);
            this.scrollContent();
        }
        else {
            this.scrollbarH.setVisible(false);
            this.scrollbarV.setVisible(false);
        }
    }
    
    @Override
    protected boolean handleEvent(final Event evt) {
        if (evt.isKeyEvent() && this.content != null && this.content.canAcceptKeyboardFocus() && this.content.handleEvent(evt)) {
            this.content.requestKeyboardFocus();
            return true;
        }
        if (super.handleEvent(evt)) {
            return true;
        }
        switch (evt.getType()) {
            case KEY_PRESSED:
            case KEY_RELEASED: {
                final int keyCode = evt.getKeyCode();
                if (keyCode == 203 || keyCode == 205) {
                    return this.scrollbarH.handleEvent(evt);
                }
                if (keyCode == 200 || keyCode == 208 || keyCode == 201 || keyCode == 209) {
                    return this.scrollbarV.handleEvent(evt);
                }
                break;
            }
            case MOUSE_WHEEL: {
                return this.scrollbarV.isVisible() && this.scrollbarV.handleEvent(evt);
            }
        }
        return evt.isMouseEvent() && this.contentArea.isMouseInside(evt);
    }
    
    @Override
    protected void paint(final GUI gui) {
        if (this.dragButton != null) {
            final de.matthiasmann.twl.AnimationState as = this.dragButton.getAnimationState();
            as.setAnimationState(ScrollPane.STATE_DOWNARROW_ARMED, this.scrollbarV.isDownRightButtonArmed());
            as.setAnimationState(ScrollPane.STATE_RIGHTARROW_ARMED, this.scrollbarH.isDownRightButtonArmed());
        }
        super.paint(gui);
    }
    
    void scrollContent() {
        if (this.content instanceof Scrollable) {
            final Scrollable scrollable = (Scrollable)this.content;
            scrollable.setScrollPosition(this.scrollbarH.getValue(), this.scrollbarV.getValue());
        }
        else {
            this.content.setPosition(this.contentArea.getX() - this.scrollbarH.getValue(), this.contentArea.getY() - this.scrollbarV.getValue());
        }
    }
    
    void setAutoScrollMarker() {
        final int scrollPos = this.scrollbarV.getValue();
        final de.matthiasmann.twl.AnimationState animationState = this.getAnimationState();
        animationState.setAnimationState(ScrollPane.STATE_AUTO_SCROLL_UP, this.autoScrollDirection < 0 && scrollPos > 0);
        animationState.setAnimationState(ScrollPane.STATE_AUTO_SCROLL_DOWN, this.autoScrollDirection > 0 && scrollPos < this.scrollbarV.getMaxValue());
    }
    
    void doAutoScroll() {
        this.scrollbarV.setValue(this.scrollbarV.getValue() + this.autoScrollDirection * this.autoScrollSpeed);
        this.setAutoScrollMarker();
    }
    
    static {
        STATE_DOWNARROW_ARMED = AnimationState.StateKey.get("downArrowArmed");
        STATE_RIGHTARROW_ARMED = AnimationState.StateKey.get("rightArrowArmed");
        STATE_HORIZONTAL_SCROLLBAR_VISIBLE = AnimationState.StateKey.get("horizontalScrollbarVisible");
        STATE_VERTICAL_SCROLLBAR_VISIBLE = AnimationState.StateKey.get("verticalScrollbarVisible");
        STATE_AUTO_SCROLL_UP = AnimationState.StateKey.get("autoScrollUp");
        STATE_AUTO_SCROLL_DOWN = AnimationState.StateKey.get("autoScrollDown");
    }
    
    public enum Fixed
    {
        NONE, 
        HORIZONTAL, 
        VERTICAL;
    }
    
    public interface CustomPageSize
    {
        int getPageSizeX(final int p0);
        
        int getPageSizeY(final int p0);
    }
    
    public interface AutoScrollable
    {
        int getAutoScrollDirection(final Event p0, final int p1);
    }
    
    public interface Scrollable
    {
        void setScrollPosition(final int p0, final int p1);
    }
}
