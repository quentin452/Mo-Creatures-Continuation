package de.matthiasmann.twl;

import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.utils.*;

public class ResizableFrame extends Widget
{
    public static final AnimationState.StateKey STATE_FADE;
    private String title;
    private final MouseCursor[] cursors;
    private ResizableAxis resizableAxis;
    private boolean draggable;
    private DragMode dragMode;
    private int dragStartX;
    private int dragStartY;
    private int dragInitialLeft;
    private int dragInitialTop;
    private int dragInitialRight;
    private int dragInitialBottom;
    private Color fadeColorInactive;
    private int fadeDurationActivate;
    private int fadeDurationDeactivate;
    private int fadeDurationShow;
    private int fadeDurationHide;
    private TextWidget titleWidget;
    private int titleAreaTop;
    private int titleAreaLeft;
    private int titleAreaRight;
    private int titleAreaBottom;
    private boolean hasCloseButton;
    private Button closeButton;
    private int closeButtonX;
    private int closeButtonY;
    private boolean hasResizeHandle;
    private Widget resizeHandle;
    private int resizeHandleX;
    private int resizeHandleY;
    private DragMode resizeHandleDragMode;
    
    public ResizableFrame() {
        this.resizableAxis = ResizableAxis.BOTH;
        this.draggable = true;
        this.dragMode = DragMode.NONE;
        this.fadeColorInactive = Color.WHITE;
        this.title = "";
        this.cursors = new MouseCursor[DragMode.values().length];
        this.setCanAcceptKeyboardFocus(true);
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public void setTitle(final String title) {
        this.title = title;
        if (this.titleWidget != null) {
            this.titleWidget.setCharSequence(title);
        }
    }
    
    public ResizableAxis getResizableAxis() {
        return this.resizableAxis;
    }
    
    public void setResizableAxis(final ResizableAxis resizableAxis) {
        if (resizableAxis == null) {
            throw new NullPointerException("resizableAxis");
        }
        this.resizableAxis = resizableAxis;
        if (this.resizeHandle != null) {
            this.layoutResizeHandle();
        }
    }
    
    public boolean isDraggable() {
        return this.draggable;
    }
    
    public void setDraggable(final boolean movable) {
        this.draggable = movable;
    }
    
    public boolean hasTitleBar() {
        return this.titleWidget != null && this.titleWidget.getParent() == this;
    }
    
    public void addCloseCallback(final Runnable cb) {
        if (this.closeButton == null) {
            (this.closeButton = new Button()).setTheme("closeButton");
            this.closeButton.setCanAcceptKeyboardFocus(false);
            this.add((Widget)this.closeButton);
            this.layoutCloseButton();
        }
        this.closeButton.setVisible(this.hasCloseButton);
        this.closeButton.addCallback(cb);
    }
    
    public void removeCloseCallback(final Runnable cb) {
        if (this.closeButton != null) {
            this.closeButton.removeCallback(cb);
            this.closeButton.setVisible(this.closeButton.hasCallbacks());
        }
    }
    
    public int getFadeDurationActivate() {
        return this.fadeDurationActivate;
    }
    
    public int getFadeDurationDeactivate() {
        return this.fadeDurationDeactivate;
    }
    
    public int getFadeDurationHide() {
        return this.fadeDurationHide;
    }
    
    public int getFadeDurationShow() {
        return this.fadeDurationShow;
    }
    
    @Override
    public void setVisible(final boolean visible) {
        if (visible) {
            final TintAnimator tintAnimator = this.getTintAnimator();
            if ((tintAnimator != null && tintAnimator.hasTint()) || !super.isVisible()) {
                this.fadeTo(this.hasKeyboardFocus() ? Color.WHITE : this.fadeColorInactive, this.fadeDurationShow);
            }
        }
        else if (super.isVisible()) {
            this.fadeToHide(this.fadeDurationHide);
        }
    }
    
    public void setHardVisible(final boolean visible) {
        super.setVisible(visible);
    }
    
    protected void applyThemeResizableFrame(final ThemeInfo themeInfo) {
        for (final DragMode m : DragMode.values()) {
            this.cursors[m.ordinal()] = themeInfo.getMouseCursor(m.cursorName);
        }
        this.titleAreaTop = themeInfo.getParameter("titleAreaTop", 0);
        this.titleAreaLeft = themeInfo.getParameter("titleAreaLeft", 0);
        this.titleAreaRight = themeInfo.getParameter("titleAreaRight", 0);
        this.titleAreaBottom = themeInfo.getParameter("titleAreaBottom", 0);
        this.closeButtonX = themeInfo.getParameter("closeButtonX", 0);
        this.closeButtonY = themeInfo.getParameter("closeButtonY", 0);
        this.hasCloseButton = themeInfo.getParameter("hasCloseButton", false);
        this.hasResizeHandle = themeInfo.getParameter("hasResizeHandle", false);
        this.resizeHandleX = themeInfo.getParameter("resizeHandleX", 0);
        this.resizeHandleY = themeInfo.getParameter("resizeHandleY", 0);
        this.fadeColorInactive = themeInfo.getParameter("fadeColorInactive", Color.WHITE);
        this.fadeDurationActivate = themeInfo.getParameter("fadeDurationActivate", 0);
        this.fadeDurationDeactivate = themeInfo.getParameter("fadeDurationDeactivate", 0);
        this.fadeDurationShow = themeInfo.getParameter("fadeDurationShow", 0);
        this.fadeDurationHide = themeInfo.getParameter("fadeDurationHide", 0);
        this.invalidateLayout();
        if (super.isVisible() && !this.hasKeyboardFocus() && (this.getTintAnimator() != null || !Color.WHITE.equals((Object)this.fadeColorInactive))) {
            this.fadeTo(this.fadeColorInactive, 0);
        }
    }
    
    @Override
    protected void applyTheme(final ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        this.applyThemeResizableFrame(themeInfo);
    }
    
    @Override
    protected void updateTintAnimation() {
        final TintAnimator tintAnimator = this.getTintAnimator();
        tintAnimator.update();
        if (!tintAnimator.isFadeActive() && tintAnimator.isZeroAlpha()) {
            this.setHardVisible(false);
        }
    }
    
    protected void fadeTo(final Color color, final int duration) {
        this.allocateTint().fadeTo(color, duration);
        if (!super.isVisible() && color.getAlpha() != 0) {
            this.setHardVisible(true);
        }
    }
    
    protected void fadeToHide(final int duration) {
        if (duration <= 0) {
            this.setHardVisible(false);
        }
        else {
            this.allocateTint().fadeToHide(duration);
        }
    }
    
    private TintAnimator allocateTint() {
        TintAnimator tintAnimator = this.getTintAnimator();
        if (tintAnimator == null) {
            tintAnimator = new TintAnimator(new TintAnimator.AnimationStateTimeSource(this.getAnimationState(), ResizableFrame.STATE_FADE));
            this.setTintAnimator(tintAnimator);
            if (!super.isVisible()) {
                tintAnimator.fadeToHide(0);
            }
        }
        return tintAnimator;
    }
    
    protected boolean isFrameElement(final Widget widget) {
        return widget == this.titleWidget || widget == this.closeButton || widget == this.resizeHandle;
    }
    
    @Override
    protected void layout() {
        final int minWidth = this.getMinWidth();
        final int minHeight = this.getMinHeight();
        if (this.getWidth() < minWidth || this.getHeight() < minHeight) {
            final int width = Math.max(this.getWidth(), minWidth);
            final int height = Math.max(this.getHeight(), minHeight);
            if (this.getParent() != null) {
                final int x = Math.min(this.getX(), this.getParent().getInnerRight() - width);
                final int y = Math.min(this.getY(), this.getParent().getInnerBottom() - height);
                this.setPosition(x, y);
            }
            this.setSize(width, height);
        }
        for (int i = 0, n = this.getNumChildren(); i < n; ++i) {
            final Widget child = this.getChild(i);
            if (!this.isFrameElement(child)) {
                this.layoutChildFullInnerArea(child);
            }
        }
        this.layoutTitle();
        this.layoutCloseButton();
        this.layoutResizeHandle();
    }
    
    protected void layoutTitle() {
        final int titleX = this.getTitleX(this.titleAreaLeft);
        final int titleY = this.getTitleY(this.titleAreaTop);
        final int titleWidth = Math.max(0, this.getTitleX(this.titleAreaRight) - titleX);
        final int titleHeight = Math.max(0, this.getTitleY(this.titleAreaBottom) - titleY);
        if (this.titleAreaLeft != this.titleAreaRight && this.titleAreaTop != this.titleAreaBottom) {
            if (this.titleWidget == null) {
                (this.titleWidget = new TextWidget(this.getAnimationState())).setTheme("title");
                this.titleWidget.setMouseCursor(this.cursors[DragMode.POSITION.ordinal()]);
                this.titleWidget.setCharSequence(this.title);
                this.titleWidget.setClip(true);
            }
            if (this.titleWidget.getParent() == null) {
                this.insertChild(this.titleWidget, 0);
            }
            this.titleWidget.setPosition(titleX, titleY);
            this.titleWidget.setSize(titleWidth, titleHeight);
        }
        else if (this.titleWidget != null && this.titleWidget.getParent() == this) {
            this.titleWidget.destroy();
            this.removeChild(this.titleWidget);
        }
    }
    
    protected void layoutCloseButton() {
        if (this.closeButton != null) {
            this.closeButton.adjustSize();
            this.closeButton.setPosition(this.getTitleX(this.closeButtonX), this.getTitleY(this.closeButtonY));
            this.closeButton.setVisible(this.closeButton.hasCallbacks() && this.hasCloseButton);
        }
    }
    
    protected void layoutResizeHandle() {
        if (this.hasResizeHandle && this.resizeHandle == null) {
            (this.resizeHandle = new Widget(this.getAnimationState(), true)).setTheme("resizeHandle");
            super.insertChild(this.resizeHandle, 0);
        }
        if (this.resizeHandle != null) {
            if (this.resizeHandleX > 0) {
                if (this.resizeHandleY > 0) {
                    this.resizeHandleDragMode = DragMode.CORNER_TL;
                }
                else {
                    this.resizeHandleDragMode = DragMode.CORNER_TR;
                }
            }
            else if (this.resizeHandleY > 0) {
                this.resizeHandleDragMode = DragMode.CORNER_BL;
            }
            else {
                this.resizeHandleDragMode = DragMode.CORNER_BR;
            }
            this.resizeHandle.adjustSize();
            this.resizeHandle.setPosition(this.getTitleX(this.resizeHandleX), this.getTitleY(this.resizeHandleY));
            this.resizeHandle.setVisible(this.hasResizeHandle && this.resizableAxis == ResizableAxis.BOTH);
        }
        else {
            this.resizeHandleDragMode = DragMode.NONE;
        }
    }
    
    @Override
    protected void keyboardFocusGained() {
        this.fadeTo(Color.WHITE, this.fadeDurationActivate);
    }
    
    @Override
    protected void keyboardFocusLost() {
        if (!this.hasOpenPopups() && super.isVisible()) {
            this.fadeTo(this.fadeColorInactive, this.fadeDurationDeactivate);
        }
    }
    
    @Override
    public int getMinWidth() {
        int minWidth = super.getMinWidth();
        for (int i = 0, n = this.getNumChildren(); i < n; ++i) {
            final Widget child = this.getChild(i);
            if (!this.isFrameElement(child)) {
                minWidth = Math.max(minWidth, child.getMinWidth() + this.getBorderHorizontal());
            }
        }
        if (this.hasTitleBar() && this.titleAreaRight < 0) {
            minWidth = Math.max(minWidth, this.titleWidget.getPreferredWidth() + this.titleAreaLeft - this.titleAreaRight);
        }
        return minWidth;
    }
    
    @Override
    public int getMinHeight() {
        int minHeight = super.getMinHeight();
        for (int i = 0, n = this.getNumChildren(); i < n; ++i) {
            final Widget child = this.getChild(i);
            if (!this.isFrameElement(child)) {
                minHeight = Math.max(minHeight, child.getMinHeight() + this.getBorderVertical());
            }
        }
        return minHeight;
    }
    
    @Override
    public int getMaxWidth() {
        int maxWidth = super.getMaxWidth();
        for (int i = 0, n = this.getNumChildren(); i < n; ++i) {
            final Widget child = this.getChild(i);
            if (!this.isFrameElement(child)) {
                int aMaxWidth = child.getMaxWidth();
                if (aMaxWidth > 0) {
                    aMaxWidth += this.getBorderHorizontal();
                    if (maxWidth == 0 || aMaxWidth < maxWidth) {
                        maxWidth = aMaxWidth;
                    }
                }
            }
        }
        return maxWidth;
    }
    
    @Override
    public int getMaxHeight() {
        int maxHeight = super.getMaxHeight();
        for (int i = 0, n = this.getNumChildren(); i < n; ++i) {
            final Widget child = this.getChild(i);
            if (!this.isFrameElement(child)) {
                int aMaxHeight = child.getMaxHeight();
                if (aMaxHeight > 0) {
                    aMaxHeight += this.getBorderVertical();
                    if (maxHeight == 0 || aMaxHeight < maxHeight) {
                        maxHeight = aMaxHeight;
                    }
                }
            }
        }
        return maxHeight;
    }
    
    @Override
    public int getPreferredInnerWidth() {
        int prefWidth = 0;
        for (int i = 0, n = this.getNumChildren(); i < n; ++i) {
            final Widget child = this.getChild(i);
            if (!this.isFrameElement(child)) {
                prefWidth = Math.max(prefWidth, child.getPreferredWidth());
            }
        }
        return prefWidth;
    }
    
    @Override
    public int getPreferredWidth() {
        int prefWidth = super.getPreferredWidth();
        if (this.hasTitleBar() && this.titleAreaRight < 0) {
            prefWidth = Math.max(prefWidth, this.titleWidget.getPreferredWidth() + this.titleAreaLeft - this.titleAreaRight);
        }
        return prefWidth;
    }
    
    @Override
    public int getPreferredInnerHeight() {
        int prefHeight = 0;
        for (int i = 0, n = this.getNumChildren(); i < n; ++i) {
            final Widget child = this.getChild(i);
            if (!this.isFrameElement(child)) {
                prefHeight = Math.max(prefHeight, child.getPreferredHeight());
            }
        }
        return prefHeight;
    }
    
    @Override
    public void adjustSize() {
        this.layoutTitle();
        super.adjustSize();
    }
    
    private int getTitleX(final int offset) {
        return (offset < 0) ? (this.getRight() + offset) : (this.getX() + offset);
    }
    
    private int getTitleY(final int offset) {
        return (offset < 0) ? (this.getBottom() + offset) : (this.getY() + offset);
    }
    
    @Override
    protected boolean handleEvent(final Event evt) {
        final boolean isMouseExit = evt.getType() == Event.Type.MOUSE_EXITED;
        if (isMouseExit && this.resizeHandle != null && this.resizeHandle.isVisible()) {
            this.resizeHandle.getAnimationState().setAnimationState(TextWidget.STATE_HOVER, false);
        }
        if (this.dragMode != DragMode.NONE) {
            if (evt.isMouseDragEnd()) {
                this.dragMode = DragMode.NONE;
            }
            else if (evt.getType() == Event.Type.MOUSE_DRAGGED) {
                this.handleMouseDrag(evt);
            }
            return true;
        }
        if (!isMouseExit && this.resizeHandle != null && this.resizeHandle.isVisible()) {
            this.resizeHandle.getAnimationState().setAnimationState(TextWidget.STATE_HOVER, this.resizeHandle.isMouseInside(evt));
        }
        return (!evt.isMouseDragEvent() && evt.getType() == Event.Type.MOUSE_BTNDOWN && evt.getMouseButton() == 0 && this.handleMouseDown(evt)) || super.handleEvent(evt) || evt.isMouseEvent();
    }
    
    @Override
    public MouseCursor getMouseCursor(final Event evt) {
        DragMode cursorMode = this.dragMode;
        if (cursorMode == DragMode.NONE) {
            cursorMode = this.getDragMode(evt.getMouseX(), evt.getMouseY());
            if (cursorMode == DragMode.NONE) {
                return this.getMouseCursor();
            }
        }
        return this.cursors[cursorMode.ordinal()];
    }
    
    private DragMode getDragMode(final int mx, final int my) {
        boolean left = mx < this.getInnerX();
        boolean right = mx >= this.getInnerRight();
        boolean top = my < this.getInnerY();
        boolean bot = my >= this.getInnerBottom();
        if (this.titleWidget != null && this.titleWidget.getParent() == this) {
            if (this.titleWidget.isInside(mx, my)) {
                if (this.draggable) {
                    return DragMode.POSITION;
                }
                return DragMode.NONE;
            }
            else {
                top = (my < this.titleWidget.getY());
            }
        }
        if (this.closeButton != null && this.closeButton.isVisible() && this.closeButton.isInside(mx, my)) {
            return DragMode.NONE;
        }
        if (this.resizableAxis == ResizableAxis.NONE) {
            return DragMode.NONE;
        }
        if (this.resizeHandle != null && this.resizeHandle.isVisible() && this.resizeHandle.isInside(mx, my)) {
            return this.resizeHandleDragMode;
        }
        if (!this.resizableAxis.allowX) {
            left = false;
            right = false;
        }
        if (!this.resizableAxis.allowY) {
            top = false;
            bot = false;
        }
        if (left) {
            if (top) {
                return DragMode.CORNER_TL;
            }
            if (bot) {
                return DragMode.CORNER_BL;
            }
            return DragMode.EDGE_LEFT;
        }
        else if (right) {
            if (top) {
                return DragMode.CORNER_TR;
            }
            if (bot) {
                return DragMode.CORNER_BR;
            }
            return DragMode.EDGE_RIGHT;
        }
        else {
            if (top) {
                return DragMode.EDGE_TOP;
            }
            if (bot) {
                return DragMode.EDGE_BOTTOM;
            }
            return DragMode.NONE;
        }
    }
    
    private boolean handleMouseDown(final Event evt) {
        final int mx = evt.getMouseX();
        final int my = evt.getMouseY();
        this.dragStartX = mx;
        this.dragStartY = my;
        this.dragInitialLeft = this.getX();
        this.dragInitialTop = this.getY();
        this.dragInitialRight = this.getRight();
        this.dragInitialBottom = this.getBottom();
        this.dragMode = this.getDragMode(mx, my);
        return this.dragMode != DragMode.NONE;
    }
    
    private void handleMouseDrag(final Event evt) {
        final int dx = evt.getMouseX() - this.dragStartX;
        final int dy = evt.getMouseY() - this.dragStartY;
        final int minWidth = this.getMinWidth();
        final int minHeight = this.getMinHeight();
        int maxWidth = this.getMaxWidth();
        int maxHeight = this.getMaxHeight();
        if (maxWidth > 0 && maxWidth < minWidth) {
            maxWidth = minWidth;
        }
        if (maxHeight > 0 && maxHeight < minHeight) {
            maxHeight = minHeight;
        }
        int left = this.dragInitialLeft;
        int top = this.dragInitialTop;
        int right = this.dragInitialRight;
        int bottom = this.dragInitialBottom;
        switch (this.dragMode) {
            case CORNER_BL:
            case CORNER_TL:
            case EDGE_LEFT: {
                left = Math.min(left + dx, right - minWidth);
                if (maxWidth > 0) {
                    left = Math.max(left, Math.min(this.dragInitialLeft, right - maxWidth));
                    break;
                }
                break;
            }
            case CORNER_BR:
            case CORNER_TR:
            case EDGE_RIGHT: {
                right = Math.max(right + dx, left + minWidth);
                if (maxWidth > 0) {
                    right = Math.min(right, Math.max(this.dragInitialRight, left + maxWidth));
                    break;
                }
                break;
            }
            case POSITION: {
                if (this.getParent() != null) {
                    final int minX = this.getParent().getInnerX();
                    final int maxX = this.getParent().getInnerRight();
                    final int width = this.dragInitialRight - this.dragInitialLeft;
                    left = Math.max(minX, Math.min(maxX - width, left + dx));
                    right = Math.min(maxX, Math.max(minX + width, right + dx));
                    break;
                }
                left += dx;
                right += dx;
                break;
            }
        }
        switch (this.dragMode) {
            case CORNER_TL:
            case CORNER_TR:
            case EDGE_TOP: {
                top = Math.min(top + dy, bottom - minHeight);
                if (maxHeight > 0) {
                    top = Math.max(top, Math.min(this.dragInitialTop, bottom - maxHeight));
                    break;
                }
                break;
            }
            case CORNER_BL:
            case CORNER_BR:
            case EDGE_BOTTOM: {
                bottom = Math.max(bottom + dy, top + minHeight);
                if (maxHeight > 0) {
                    bottom = Math.min(bottom, Math.max(this.dragInitialBottom, top + maxHeight));
                    break;
                }
                break;
            }
            case POSITION: {
                if (this.getParent() != null) {
                    final int minY = this.getParent().getInnerY();
                    final int maxY = this.getParent().getInnerBottom();
                    final int height = this.dragInitialBottom - this.dragInitialTop;
                    top = Math.max(minY, Math.min(maxY - height, top + dy));
                    bottom = Math.min(maxY, Math.max(minY + height, bottom + dy));
                    break;
                }
                top += dy;
                bottom += dy;
                break;
            }
        }
        this.setArea(top, left, right, bottom);
    }
    
    private void setArea(int top, int left, int right, int bottom) {
        final Widget p = this.getParent();
        if (p != null) {
            top = Math.max(top, p.getInnerY());
            left = Math.max(left, p.getInnerX());
            right = Math.min(right, p.getInnerRight());
            bottom = Math.min(bottom, p.getInnerBottom());
        }
        this.setPosition(left, top);
        this.setSize(Math.max(this.getMinWidth(), right - left), Math.max(this.getMinHeight(), bottom - top));
    }
    
    static {
        STATE_FADE = AnimationState.StateKey.get("fade");
    }
    
    public enum ResizableAxis
    {
        NONE(false, false), 
        HORIZONTAL(true, false), 
        VERTICAL(false, true), 
        BOTH(true, true);
        
        final boolean allowX;
        final boolean allowY;
        
        private ResizableAxis(final boolean allowX, final boolean allowY) {
            this.allowX = allowX;
            this.allowY = allowY;
        }
    }
    
    private enum DragMode
    {
        NONE("mouseCursor"), 
        EDGE_LEFT("mouseCursor.left"), 
        EDGE_TOP("mouseCursor.top"), 
        EDGE_RIGHT("mouseCursor.right"), 
        EDGE_BOTTOM("mouseCursor.bottom"), 
        CORNER_TL("mouseCursor.top-left"), 
        CORNER_TR("mouseCursor.top-right"), 
        CORNER_BR("mouseCursor.bottom-right"), 
        CORNER_BL("mouseCursor.bottom-left"), 
        POSITION("mouseCursor.all");
        
        final String cursorName;
        
        private DragMode(final String cursorName) {
            this.cursorName = cursorName;
        }
    }
}
