package de.matthiasmann.twl;

import de.matthiasmann.twl.input.*;
import de.matthiasmann.twl.input.lwjgl.*;
import de.matthiasmann.twl.theme.*;
import java.util.concurrent.*;
import java.util.*;
import java.util.logging.*;
import de.matthiasmann.twl.renderer.*;
import java.util.concurrent.atomic.*;

public final class GUI extends Widget
{
    private static final int DRAG_DIST = 3;
    private static final int DBLCLICK_TIME = 500;
    private static final int KEYREPEAT_INITIAL_DELAY = 250;
    private static final int KEYREPEAT_INTERVAL_DELAY = 33;
    private static final int NO_REPEAT = 0;
    private int tooltipOffsetX;
    private int tooltipOffsetY;
    private int tooltipDelay;
    private int tooltipReappearDelay;
    private final Renderer renderer;
    private final Input input;
    long curTime;
    private int deltaTime;
    private Widget rootPane;
    boolean hasInvalidLayouts;
    final Event event;
    private boolean wasInside;
    private boolean dragActive;
    private int mouseClickCount;
    private int dragButton;
    private int mouseDownX;
    private int mouseDownY;
    private int mouseLastX;
    private int mouseLastY;
    private int mouseClickedX;
    private int mouseClickedY;
    private long mouseEventTime;
    private long tooltipEventTime;
    private long mouseClickedTime;
    private long keyEventTime;
    private int keyRepeatDelay;
    private boolean popupEventOccured;
    private Widget lastMouseDownWidget;
    private Widget lastMouseClickWidget;
    private PopupWindow boundDragPopup;
    private Runnable boundDragCallback;
    private Widget focusKeyWidget;
    private int mouseIdleTime;
    private boolean mouseIdleState;
    private MouseIdleListener mouseIdleListener;
    private InfoWindow activeInfoWindow;
    private final Widget infoWindowPlaceholder;
    private final TooltipWindow tooltipWindow;
    private final Label tooltipLabel;
    private Widget tooltipOwner;
    private boolean hadOpenTooltip;
    private long tooltipClosedTime;
    final ArrayList<Timer> activeTimers;
    final ExecutorService executorService;
    private final Object invokeLock;
    private Runnable[] invokeLaterQueue;
    private int invokeLaterQueueSize;
    private Runnable[] invokeRunnables;
    private static final int FOCUS_KEY = 15;
    
    public GUI(final Renderer renderer) {
        this(new Widget(), renderer);
        this.rootPane.setTheme("");
        this.rootPane.setFocusKeyEnabled(false);
    }
    
    public GUI(final Widget rootPane, final Renderer renderer) {
        this(rootPane, renderer, new LWJGLInput());
    }
    
    public GUI(final Widget rootPane, final Renderer renderer, final Input input) {
        this.tooltipOffsetX = 0;
        this.tooltipOffsetY = 0;
        this.tooltipDelay = 1000;
        this.tooltipReappearDelay = 100;
        this.dragButton = -1;
        this.mouseIdleTime = 60;
        if (rootPane == null) {
            throw new IllegalArgumentException("rootPane is null");
        }
        if (renderer == null) {
            throw new IllegalArgumentException("renderer is null");
        }
        this.guiInstance = this;
        this.renderer = renderer;
        this.input = input;
        this.event = new Event();
        (this.rootPane = rootPane).setFocusKeyEnabled(false);
        (this.infoWindowPlaceholder = new Widget()).setTheme("");
        this.tooltipLabel = new Label();
        (this.tooltipWindow = new TooltipWindow()).setVisible(false);
        this.activeTimers = new ArrayList<Timer>();
        this.executorService = Executors.newSingleThreadExecutor(new TF());
        this.invokeLock = new Object();
        this.invokeLaterQueue = new Runnable[16];
        this.invokeRunnables = new Runnable[16];
        this.setTheme("");
        this.setFocusKeyEnabled(false);
        this.setSize();
        super.insertChild(rootPane, 0);
        super.insertChild(this.infoWindowPlaceholder, 1);
        super.insertChild((Widget)this.tooltipWindow, 2);
        this.resyncTimerAfterPause();
    }
    
    public void applyTheme(final ThemeManager themeManager) {
        if (themeManager == null) {
            throw new IllegalArgumentException("themeManager is null");
        }
        super.applyTheme(themeManager);
    }
    
    public Widget getRootPane() {
        return this.rootPane;
    }
    
    public void setRootPane(final Widget rootPane) {
        if (rootPane == null) {
            throw new IllegalArgumentException("rootPane is null");
        }
        this.rootPane = rootPane;
        super.removeChild(0);
        super.insertChild(rootPane, 0);
    }
    
    public Renderer getRenderer() {
        return this.renderer;
    }
    
    public Input getInput() {
        return this.input;
    }
    
    public MouseSensitiveRectangle createMouseSenitiveRectangle() {
        return new MouseSensitiveRectangle() {
            @Override
            public boolean isMouseOver() {
                return this.isInside(GUI.this.event.mouseX, GUI.this.event.mouseY);
            }
        };
    }
    
    public Timer createTimer() {
        return new Timer(this);
    }
    
    public long getCurrentTime() {
        return this.curTime;
    }
    
    public int getCurrentDeltaTime() {
        return this.deltaTime;
    }
    
    public void invokeLater(final Runnable runnable) {
        if (runnable == null) {
            throw new IllegalArgumentException("runnable is null");
        }
        synchronized (this.invokeLock) {
            if (this.invokeLaterQueueSize == this.invokeLaterQueue.length) {
                this.growInvokeLaterQueue();
            }
            this.invokeLaterQueue[this.invokeLaterQueueSize++] = runnable;
        }
    }
    
    public <V> Future<V> invokeAsync(final Callable<V> job, final AsyncCompletionListener<V> listener) {
        if (job == null) {
            throw new IllegalArgumentException("job is null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }
        return this.executorService.submit((Callable<V>)new AC<V>(job, null, listener));
    }
    
    public <V> Future<V> invokeAsync(final Runnable job, final AsyncCompletionListener<V> listener) {
        if (job == null) {
            throw new IllegalArgumentException("job is null");
        }
        if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }
        return this.executorService.submit((Callable<V>)new AC<V>(null, job, listener));
    }
    
    public boolean requestToolTip(final Widget widget, final int x, final int y, final Object content, final Alignment alignment) {
        if (alignment == null) {
            throw new IllegalArgumentException("alignment is null");
        }
        if (widget == this.getWidgetUnderMouse()) {
            this.setTooltip(x, y, widget, content, alignment);
            return true;
        }
        return false;
    }
    
    public MouseIdleListener getMouseIdleListener() {
        return this.mouseIdleListener;
    }
    
    public void setMouseIdleListener(final MouseIdleListener mouseIdleListener) {
        this.mouseIdleListener = mouseIdleListener;
        this.callMouseIdleListener();
    }
    
    public int getMouseIdleTime() {
        return this.mouseIdleTime;
    }
    
    public void setMouseIdleTime(final int mouseIdleTime) {
        if (mouseIdleTime < 1) {
            throw new IllegalArgumentException("mouseIdleTime < 1");
        }
        this.mouseIdleTime = mouseIdleTime;
    }
    
    public int getTooltipDelay() {
        return this.tooltipDelay;
    }
    
    public void setTooltipDelay(final int tooltipDelay) {
        if (tooltipDelay < 1) {
            throw new IllegalArgumentException("tooltipDelay");
        }
        this.tooltipDelay = tooltipDelay;
    }
    
    public int getTooltipReappearDelay() {
        return this.tooltipReappearDelay;
    }
    
    public void setTooltipReappearDelay(final int tooltipReappearDelay) {
        this.tooltipReappearDelay = tooltipReappearDelay;
    }
    
    public int getTooltipOffsetX() {
        return this.tooltipOffsetX;
    }
    
    public int getTooltipOffsetY() {
        return this.tooltipOffsetY;
    }
    
    public void setTooltipOffset(final int tooltipOffsetX, final int tooltipOffsetY) {
        this.tooltipOffsetX = tooltipOffsetX;
        this.tooltipOffsetY = tooltipOffsetY;
    }
    
    public void setTooltipWindowRenderOffscreen(final RenderOffscreen renderOffscreen) {
        this.tooltipWindow.setRenderOffscreen(renderOffscreen);
    }
    
    public void setTooltipWindowTheme(final String theme) {
        this.tooltipWindow.setTheme(theme);
        this.tooltipWindow.reapplyTheme();
    }
    
    @Override
    public boolean setPosition(final int x, final int y) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void insertChild(final Widget child, final int index) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void removeAllChildren() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Widget removeChild(final int index) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void adjustSize() {
    }
    
    @Override
    protected void layout() {
        this.layoutChildFullInnerArea(this.rootPane);
    }
    
    @Override
    public void validateLayout() {
        if (this.hasInvalidLayouts) {
            final int MAX_ITERATIONS = 1000;
            int iterations;
            for (iterations = 0; this.hasInvalidLayouts && iterations < 1000; ++iterations) {
                this.hasInvalidLayouts = false;
                super.validateLayout();
            }
            ArrayList<Widget> widgetsInLoop = null;
            if (this.hasInvalidLayouts) {
                widgetsInLoop = new ArrayList<Widget>();
                this.collectLayoutLoop(widgetsInLoop);
            }
            DebugHook.getDebugHook().guiLayoutValidated(iterations, (Collection)widgetsInLoop);
        }
    }
    
    public void setSize() {
        this.setSize(this.renderer.getWidth(), this.renderer.getHeight());
    }
    
    public void update() {
        this.setSize();
        this.updateTime();
        this.handleInput();
        this.handleKeyRepeat();
        this.handleTooltips();
        this.updateTimers();
        this.invokeRunables();
        this.validateLayout();
        this.draw();
        this.setCursor();
    }
    
    public void resyncTimerAfterPause() {
        this.curTime = this.renderer.getTimeMillis();
        this.deltaTime = 0;
    }
    
    public void updateTime() {
        final long newTime = this.renderer.getTimeMillis();
        this.deltaTime = Math.max(0, (int)(newTime - this.curTime));
        this.curTime = newTime;
    }
    
    public void updateTimers() {
        int i = 0;
        while (i < this.activeTimers.size()) {
            if (!this.activeTimers.get(i).tick(this.deltaTime)) {
                this.activeTimers.remove(i);
            }
            else {
                ++i;
            }
        }
    }
    
    public void invokeRunables() {
        Runnable[] runnables = null;
        final int count;
        synchronized (this.invokeLock) {
            count = this.invokeLaterQueueSize;
            if (count > 0) {
                this.invokeLaterQueueSize = 0;
                runnables = this.invokeLaterQueue;
                this.invokeLaterQueue = this.invokeRunnables;
                this.invokeRunnables = runnables;
            }
        }
        int i = 0;
        while (i < count) {
            final Runnable r = runnables[i];
            runnables[i++] = null;
            try {
                r.run();
            }
            catch (Throwable ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, "Exception in runnable", ex);
            }
        }
    }
    
    public void draw() {
        if (this.renderer.startRendering()) {
            try {
                this.drawWidget(this);
                if (this.dragActive && this.boundDragPopup == null && this.lastMouseDownWidget != null) {
                    this.lastMouseDownWidget.paintDragOverlay(this, this.event.mouseX, this.event.mouseY, this.event.modifier);
                }
            }
            finally {
                this.renderer.endRendering();
            }
        }
    }
    
    public void setCursor() {
        this.event.type = Event.Type.MOUSE_MOVED;
        Widget widget = this.getWidgetUnderMouse();
        MouseCursor cursor = null;
        while (widget != null) {
            if (widget.isEnabled()) {
                cursor = widget.getMouseCursor(this.event);
                if (cursor != null) {
                    break;
                }
            }
            widget = widget.getParent();
        }
        this.renderer.setCursor(cursor);
    }
    
    public void handleInput() {
        if (this.input != null && !this.input.pollInput(this)) {
            this.clearKeyboardState();
            this.clearMouseState();
        }
    }
    
    public final boolean handleMouse(final int mouseX, final int mouseY, final int button, boolean pressed) {
        this.mouseEventTime = this.curTime;
        this.tooltipEventTime = this.curTime;
        this.event.mouseButton = button;
        final int prevButtonState = this.event.modifier & 0x1C0;
        int buttonMask = 0;
        switch (button) {
            case 0: {
                buttonMask = 64;
                break;
            }
            case 1: {
                buttonMask = 128;
                break;
            }
            case 2: {
                buttonMask = 256;
                break;
            }
        }
        this.event.setModifier(buttonMask, pressed);
        final boolean wasPressed = (prevButtonState & buttonMask) != 0x0;
        if (buttonMask != 0) {
            this.renderer.setMouseButton(button, pressed);
        }
        if (this.dragActive || prevButtonState == 0) {
            this.event.mouseX = mouseX;
            this.event.mouseY = mouseY;
        }
        else {
            this.event.mouseX = this.mouseDownX;
            this.event.mouseY = this.mouseDownY;
        }
        boolean handled = this.dragActive;
        if (!this.dragActive) {
            if (!this.isInside(mouseX, mouseY)) {
                pressed = false;
                this.mouseClickCount = 0;
                if (this.wasInside) {
                    this.sendMouseEvent(Event.Type.MOUSE_EXITED, null);
                    this.wasInside = false;
                }
            }
            else if (!this.wasInside) {
                this.wasInside = true;
                if (this.sendMouseEvent(Event.Type.MOUSE_ENTERED, null) != null) {
                    handled = true;
                }
            }
        }
        if (mouseX != this.mouseLastX || mouseY != this.mouseLastY) {
            this.mouseLastX = mouseX;
            this.mouseLastY = mouseY;
            if (prevButtonState != 0 && !this.dragActive && (Math.abs(mouseX - this.mouseDownX) > 3 || Math.abs(mouseY - this.mouseDownY) > 3)) {
                this.dragActive = true;
                this.mouseClickCount = 0;
                this.hideTooltip();
                this.hadOpenTooltip = false;
                this.tooltipOwner = this.lastMouseDownWidget;
            }
            if (this.dragActive) {
                if (this.boundDragPopup != null) {
                    assert this.getTopPane() == this.boundDragPopup;
                    this.sendMouseEvent(Event.Type.MOUSE_MOVED, null);
                }
                else if (this.lastMouseDownWidget != null) {
                    this.sendMouseEvent(Event.Type.MOUSE_DRAGGED, this.lastMouseDownWidget);
                }
            }
            else if (prevButtonState == 0 && this.sendMouseEvent(Event.Type.MOUSE_MOVED, null) != null) {
                handled = true;
            }
        }
        if (buttonMask != 0 && pressed != wasPressed) {
            if (pressed) {
                if (this.dragButton < 0) {
                    this.mouseDownX = mouseX;
                    this.mouseDownY = mouseY;
                    this.dragButton = button;
                    this.lastMouseDownWidget = this.sendMouseEvent(Event.Type.MOUSE_BTNDOWN, null);
                }
                else if (this.lastMouseDownWidget != null && this.boundDragPopup == null) {
                    this.sendMouseEvent(Event.Type.MOUSE_BTNDOWN, this.lastMouseDownWidget);
                }
            }
            else if (this.dragButton >= 0 && (this.boundDragPopup == null || this.event.isMouseDragEnd())) {
                if (this.boundDragPopup != null && button == this.dragButton) {
                    this.sendMouseEvent(Event.Type.MOUSE_BTNUP, this.getWidgetUnderMouse());
                }
                if (this.lastMouseDownWidget != null) {
                    this.sendMouseEvent(Event.Type.MOUSE_BTNUP, this.lastMouseDownWidget);
                }
            }
            if (this.lastMouseDownWidget != null) {
                handled = true;
            }
            if (button == 0 && !this.popupEventOccured && !pressed && !this.dragActive) {
                if (this.mouseClickCount == 0 || this.curTime - this.mouseClickedTime > 500L || this.lastMouseClickWidget != this.lastMouseDownWidget) {
                    this.mouseClickedX = mouseX;
                    this.mouseClickedY = mouseY;
                    this.lastMouseClickWidget = this.lastMouseDownWidget;
                    this.mouseClickCount = 0;
                    this.mouseClickedTime = this.curTime;
                }
                if (Math.abs(mouseX - this.mouseClickedX) < 3 && Math.abs(mouseY - this.mouseClickedY) < 3) {
                    this.event.mouseX = this.mouseClickedX;
                    this.event.mouseY = this.mouseClickedY;
                    this.event.mouseClickCount = ++this.mouseClickCount;
                    this.mouseClickedTime = this.curTime;
                    if (this.lastMouseClickWidget != null) {
                        this.sendMouseEvent(Event.Type.MOUSE_CLICKED, this.lastMouseClickWidget);
                    }
                }
                else {
                    this.lastMouseClickWidget = null;
                }
            }
        }
        if (this.event.isMouseDragEnd()) {
            if (this.dragActive) {
                this.dragActive = false;
                this.sendMouseEvent(Event.Type.MOUSE_MOVED, null);
            }
            this.dragButton = -1;
            if (this.boundDragCallback != null) {
                try {
                    this.boundDragCallback.run();
                }
                catch (Exception ex) {
                    Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, "Exception in bound drag callback", ex);
                }
                finally {
                    this.boundDragCallback = null;
                    this.boundDragPopup = null;
                }
            }
        }
        return handled;
    }
    
    public void clearMouseState() {
        this.event.setModifier(64, false);
        this.event.setModifier(256, false);
        this.event.setModifier(128, false);
        this.renderer.setMouseButton(0, false);
        this.renderer.setMouseButton(2, false);
        this.renderer.setMouseButton(1, false);
        this.lastMouseClickWidget = null;
        this.mouseClickCount = 0;
        this.mouseClickedTime = this.curTime;
        this.boundDragPopup = null;
        this.boundDragCallback = null;
        if (this.dragActive) {
            this.dragActive = false;
            this.sendMouseEvent(Event.Type.MOUSE_MOVED, null);
        }
        this.dragButton = -1;
    }
    
    public final boolean handleMouseWheel(final int wheelDelta) {
        this.event.mouseWheelDelta = wheelDelta;
        final boolean handled = this.sendMouseEvent(Event.Type.MOUSE_WHEEL, this.dragActive ? this.lastMouseDownWidget : null) != null;
        this.event.mouseWheelDelta = 0;
        return handled;
    }
    
    public final boolean handleKey(final int keyCode, final char keyChar, final boolean pressed) {
        this.event.keyCode = keyCode;
        this.event.keyChar = keyChar;
        this.event.keyRepeated = false;
        this.keyEventTime = this.curTime;
        if (this.event.keyCode == 0 && this.event.keyChar == '\0') {
            this.keyRepeatDelay = 0;
            return false;
        }
        this.event.setModifiers(pressed);
        if (pressed) {
            this.keyRepeatDelay = 250;
            return this.sendKeyEvent(Event.Type.KEY_PRESSED);
        }
        this.keyRepeatDelay = 0;
        return this.sendKeyEvent(Event.Type.KEY_RELEASED);
    }
    
    public final void clearKeyboardState() {
        final Event event = this.event;
        event.modifier &= 0xFFFFF9C0;
        this.keyRepeatDelay = 0;
    }
    
    public final void handleKeyRepeat() {
        if (this.keyRepeatDelay != 0) {
            final long keyDeltaTime = this.curTime - this.keyEventTime;
            if (keyDeltaTime > this.keyRepeatDelay) {
                this.keyEventTime = this.curTime;
                this.keyRepeatDelay = 33;
                this.event.keyRepeated = true;
                this.sendKeyEvent(Event.Type.KEY_PRESSED);
            }
        }
    }
    
    public final void handleTooltips() {
        final Widget widgetUnderMouse = this.getWidgetUnderMouse();
        if (widgetUnderMouse != this.tooltipOwner) {
            if (widgetUnderMouse != null && (this.curTime - this.tooltipEventTime > this.tooltipDelay || (this.hadOpenTooltip && this.curTime - this.tooltipClosedTime < this.tooltipReappearDelay))) {
                this.setTooltip(this.event.mouseX + this.tooltipOffsetX, this.event.mouseY + this.tooltipOffsetY, widgetUnderMouse, widgetUnderMouse.getTooltipContentAt(this.event.mouseX, this.event.mouseY), Alignment.BOTTOMLEFT);
            }
            else {
                this.hideTooltip();
            }
        }
        final boolean mouseIdle = this.curTime - this.mouseEventTime > this.mouseIdleTime;
        if (this.mouseIdleState != mouseIdle) {
            this.mouseIdleState = mouseIdle;
            this.callMouseIdleListener();
        }
    }
    
    private Widget getTopPane() {
        return super.getChild(super.getNumChildren() - 3);
    }
    
    @Override
    Widget getWidgetUnderMouse() {
        return this.getTopPane().getWidgetUnderMouse();
    }
    
    private Widget sendMouseEvent(final Event.Type type, final Widget target) {
        assert type.isMouseEvent;
        this.popupEventOccured = false;
        this.event.type = type;
        this.event.dragEvent = (this.dragActive && this.boundDragPopup == null);
        this.renderer.setMousePosition(this.event.mouseX, this.event.mouseY);
        if (target != null) {
            if (target.isEnabled() || !Widget.isMouseAction(this.event)) {
                target.handleEvent(target.translateMouseEvent(this.event));
            }
            return target;
        }
        assert this.boundDragPopup != null;
        Widget widget = null;
        if (this.activeInfoWindow != null && this.activeInfoWindow.isMouseInside(this.event) && this.setMouseOverChild((Widget)this.activeInfoWindow, this.event)) {
            widget = (Widget)this.activeInfoWindow;
        }
        if (widget == null) {
            widget = this.getTopPane();
            this.setMouseOverChild(widget, this.event);
        }
        return widget.routeMouseEvent(this.event);
    }
    
    boolean isFocusKey() {
        return this.event.keyCode == 15 && (this.event.modifier & 0x636) == 0x0;
    }
    
    void setFocusKeyWidget(final Widget widget) {
        if (this.focusKeyWidget == null && this.isFocusKey()) {
            this.focusKeyWidget = widget;
        }
    }
    
    private boolean sendKeyEvent(final Event.Type type) {
        assert type.isKeyEvent;
        this.popupEventOccured = false;
        this.focusKeyWidget = null;
        this.event.type = type;
        this.event.dragEvent = false;
        boolean handled = this.getTopPane().handleEvent(this.event);
        if (!handled && this.focusKeyWidget != null) {
            this.focusKeyWidget.handleFocusKeyEvent(this.event);
            handled = true;
        }
        this.focusKeyWidget = null;
        return handled;
    }
    
    private void sendPopupEvent(final Event.Type type) {
        assert type == Event.Type.POPUP_CLOSED;
        this.popupEventOccured = false;
        this.event.type = type;
        this.event.dragEvent = false;
        try {
            this.getTopPane().routePopupEvent(this.event);
        }
        catch (Exception ex) {
            Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, "Exception in sendPopupEvent()", ex);
        }
    }
    
    void resendLastMouseMove() {
        if (!this.dragActive) {
            this.sendMouseEvent(Event.Type.MOUSE_MOVED, null);
        }
    }
    
    void openPopup(final PopupWindow popup) {
        if (popup.getParent() == this) {
            this.closePopup(popup);
        }
        else if (popup.getParent() != null) {
            throw new IllegalArgumentException("popup must not be added anywhere");
        }
        this.hideTooltip();
        this.hadOpenTooltip = false;
        this.sendPopupEvent(Event.Type.POPUP_OPENED);
        super.insertChild((Widget)popup, this.getNumChildren() - 2);
        popup.getOwner().setOpenPopup(this, true);
        this.popupEventOccured = true;
        if (this.activeInfoWindow != null) {
            this.closeInfo(this.activeInfoWindow);
        }
    }
    
    void closePopup(final PopupWindow popup) {
        if (this.boundDragPopup == popup) {
            this.boundDragPopup = null;
        }
        final int idx = this.getChildIndex((Widget)popup);
        if (idx > 0) {
            super.removeChild(idx);
        }
        popup.getOwner().recalcOpenPopups(this);
        this.sendPopupEvent(Event.Type.POPUP_CLOSED);
        this.popupEventOccured = true;
        this.closeInfoFromWidget((Widget)popup);
        this.requestKeyboardFocus(this.getTopPane());
        this.resendLastMouseMove();
    }
    
    boolean hasOpenPopups(final Widget owner) {
        int i = this.getNumChildren() - 2;
        while (i-- > 1) {
            final PopupWindow popup = (PopupWindow)this.getChild(i);
            if (popup.getOwner() == owner) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isOwner(Widget owner, final Widget widget) {
        while (owner != null && owner != widget) {
            owner = owner.getParent();
        }
        return owner == widget;
    }
    
    void closePopupFromWidgets(final Widget widget) {
        int i = this.getNumChildren() - 2;
        while (i-- > 1) {
            final PopupWindow popup = (PopupWindow)this.getChild(i);
            if (this.isOwner(popup.getOwner(), widget)) {
                this.closePopup(popup);
            }
        }
    }
    
    void closeIfPopup(final Widget widget) {
        if (widget instanceof PopupWindow) {
            this.closePopup((PopupWindow)widget);
        }
    }
    
    boolean bindDragEvent(final PopupWindow popup, final Runnable cb) {
        if (this.boundDragPopup == null && this.getTopPane() == popup && this.dragButton >= 0 && !this.isOwner(this.lastMouseDownWidget, (Widget)popup)) {
            this.dragActive = true;
            this.boundDragPopup = popup;
            this.boundDragCallback = cb;
            this.sendMouseEvent(Event.Type.MOUSE_MOVED, null);
            return true;
        }
        return false;
    }
    
    void widgetHidden(final Widget widget) {
        this.closeIfPopup(widget);
        this.closePopupFromWidgets(widget);
        if (this.isOwner(this.tooltipOwner, widget)) {
            this.hideTooltip();
            this.hadOpenTooltip = false;
        }
        this.closeInfoFromWidget(widget);
    }
    
    void widgetDisabled(final Widget widget) {
        this.closeIfPopup(widget);
        this.closeInfoFromWidget(widget);
    }
    
    void closeInfoFromWidget(final Widget widget) {
        if (this.activeInfoWindow != null && (this.activeInfoWindow == widget || this.isOwner(this.activeInfoWindow.getOwner(), widget))) {
            this.closeInfo(this.activeInfoWindow);
        }
    }
    
    void openInfo(final InfoWindow info) {
        final int idx = this.getNumChildren() - 2;
        super.removeChild(idx);
        super.insertChild((Widget)info, idx);
        this.activeInfoWindow = info;
    }
    
    void closeInfo(final InfoWindow info) {
        if (info == this.activeInfoWindow) {
            final int idx = this.getNumChildren() - 2;
            super.removeChild(idx);
            super.insertChild(this.infoWindowPlaceholder, idx);
            this.activeInfoWindow = null;
            try {
                info.infoWindowClosed();
            }
            catch (Exception ex) {
                Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, "Exception in infoWindowClosed()", ex);
            }
        }
    }
    
    @Override
    public boolean requestKeyboardFocus() {
        return true;
    }
    
    @Override
    protected boolean requestKeyboardFocus(final Widget child) {
        return (child == null || child == this.getTopPane()) && super.requestKeyboardFocus(child);
    }
    
    void requestTooltipUpdate(final Widget widget, final boolean resetToolTipTimer) {
        if (this.tooltipOwner == widget) {
            this.tooltipOwner = null;
            if (resetToolTipTimer) {
                this.hideTooltip();
                this.hadOpenTooltip = false;
                this.tooltipEventTime = this.curTime;
            }
        }
    }
    
    private void hideTooltip() {
        if (this.tooltipWindow.isVisible()) {
            this.tooltipClosedTime = this.curTime;
            this.hadOpenTooltip = true;
        }
        this.tooltipWindow.setVisible(false);
        this.tooltipOwner = null;
        if (this.tooltipLabel.getParent() != this.tooltipWindow) {
            this.tooltipWindow.removeAllChildren();
        }
    }
    
    private void setTooltip(int x, int y, final Widget widget, final Object content, final Alignment alignment) throws IllegalArgumentException {
        if (content == null) {
            this.hideTooltip();
            return;
        }
        if (content instanceof String) {
            final String text = (String)content;
            if (text.length() == 0) {
                this.hideTooltip();
                return;
            }
            if (this.tooltipLabel.getParent() != this.tooltipWindow) {
                this.tooltipWindow.removeAllChildren();
                this.tooltipWindow.add((Widget)this.tooltipLabel);
            }
            this.tooltipLabel.setBackground(null);
            this.tooltipLabel.setText(text);
        }
        else {
            if (!(content instanceof Widget)) {
                throw new IllegalArgumentException("Unsupported data type");
            }
            final Widget tooltipWidget = (Widget)content;
            if (tooltipWidget.getParent() != null && tooltipWidget.getParent() != this.tooltipWindow) {
                throw new IllegalArgumentException("Content widget must not be added to another widget");
            }
            this.tooltipWindow.removeAllChildren();
            this.tooltipWindow.add(tooltipWidget);
        }
        this.tooltipWindow.adjustSize();
        if (this.tooltipWindow.isLayoutInvalid()) {
            this.tooltipWindow.adjustSize();
        }
        final int ttWidth = this.tooltipWindow.getWidth();
        final int ttHeight = this.tooltipWindow.getHeight();
        switch (alignment) {
            case TOP:
            case CENTER:
            case BOTTOM: {
                x -= ttWidth / 2;
                break;
            }
            case TOPRIGHT:
            case RIGHT:
            case BOTTOMRIGHT: {
                x -= ttWidth;
                break;
            }
        }
        switch (alignment) {
            case CENTER:
            case RIGHT:
            case LEFT: {
                y -= ttHeight / 2;
                break;
            }
            case BOTTOM:
            case BOTTOMRIGHT:
            case BOTTOMLEFT: {
                y -= ttHeight;
                break;
            }
        }
        if (x + ttWidth > this.getWidth()) {
            x = this.getWidth() - ttWidth;
        }
        if (y + ttHeight > this.getHeight()) {
            y = this.getHeight() - ttHeight;
        }
        if (x < 0) {
            x = 0;
        }
        if (y < 0) {
            y = 0;
        }
        this.tooltipOwner = widget;
        this.tooltipWindow.setPosition(x, y);
        this.tooltipWindow.setVisible(true);
    }
    
    private void callMouseIdleListener() {
        if (this.mouseIdleListener != null) {
            if (this.mouseIdleState) {
                this.mouseIdleListener.mouseEnterIdle();
            }
            else {
                this.mouseIdleListener.mouseExitIdle();
            }
        }
    }
    
    private void growInvokeLaterQueue() {
        final Runnable[] tmp = new Runnable[this.invokeLaterQueueSize * 2];
        System.arraycopy(this.invokeLaterQueue, 0, tmp, 0, this.invokeLaterQueueSize);
        this.invokeLaterQueue = tmp;
    }
    
    static class TooltipWindow extends Container
    {
        public static final AnimationState.StateKey STATE_FADE;
        private int fadeInTime;
        
        protected void applyTheme(final ThemeInfo themeInfo) {
            super.applyTheme(themeInfo);
            this.fadeInTime = themeInfo.getParameter("fadeInTime", 0);
        }
        
        public void setVisible(final boolean visible) {
            super.setVisible(visible);
            this.getAnimationState().resetAnimationTime(TooltipWindow.STATE_FADE);
        }
        
        protected void paint(final GUI gui) {
            final int time = this.getAnimationState().getAnimationTime(TooltipWindow.STATE_FADE);
            if (time < this.fadeInTime) {
                final float alpha = time / (float)this.fadeInTime;
                gui.getRenderer().pushGlobalTintColor(1.0f, 1.0f, 1.0f, alpha);
                try {
                    super.paint(gui);
                }
                finally {
                    gui.getRenderer().popGlobalTintColor();
                }
            }
            else {
                super.paint(gui);
            }
        }
        
        static {
            STATE_FADE = AnimationState.StateKey.get("fade");
        }
    }
    
    class AC<V> implements Callable<V>, Runnable
    {
        private final Callable<V> jobC;
        private final Runnable jobR;
        private final AsyncCompletionListener<V> listener;
        private V result;
        private Exception exception;
        
        AC(final Callable<V> jobC, final Runnable jobR, final AsyncCompletionListener<V> listener) {
            this.jobC = jobC;
            this.jobR = jobR;
            this.listener = listener;
        }
        
        @Override
        public V call() throws Exception {
            try {
                if (this.jobC != null) {
                    this.result = this.jobC.call();
                }
                else {
                    this.jobR.run();
                }
                GUI.this.invokeLater(this);
                return this.result;
            }
            catch (Exception ex) {
                this.exception = ex;
                GUI.this.invokeLater(this);
                throw ex;
            }
        }
        
        @Override
        public void run() {
            if (this.exception != null) {
                this.listener.failed(this.exception);
            }
            else {
                this.listener.completed(this.result);
            }
        }
    }
    
    static class TF implements ThreadFactory
    {
        static final AtomicInteger poolNumber;
        final AtomicInteger threadNumber;
        final String prefix;
        
        TF() {
            this.threadNumber = new AtomicInteger(1);
            this.prefix = "GUI-" + TF.poolNumber.getAndIncrement() + "-invokeAsync-";
        }
        
        @Override
        public Thread newThread(final Runnable r) {
            final Thread t = new Thread(r, this.prefix + this.threadNumber.getAndIncrement());
            t.setDaemon(true);
            t.setPriority(5);
            return t;
        }
        
        static {
            poolNumber = new AtomicInteger(1);
        }
    }
    
    public interface AsyncCompletionListener<V>
    {
        void completed(final V p0);
        
        void failed(final Exception p0);
    }
    
    public interface MouseIdleListener
    {
        void mouseEnterIdle();
        
        void mouseExitIdle();
    }
}
