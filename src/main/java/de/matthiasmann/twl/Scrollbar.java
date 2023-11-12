package de.matthiasmann.twl;

import de.matthiasmann.twl.model.*;
import de.matthiasmann.twl.utils.*;
import de.matthiasmann.twl.renderer.*;

public class Scrollbar extends Widget
{
    private static final int INITIAL_DELAY = 300;
    private static final int REPEAT_DELAY = 75;
    private final Orientation orientation;
    private final Button btnUpLeft;
    private final Button btnDownRight;
    private final DraggableButton thumb;
    private final L dragTimerCB;
    private Timer timer;
    private int trackClicked;
    private int trackClickLimit;
    private Runnable[] callbacks;
    private Image trackImageUpLeft;
    private Image trackImageDownRight;
    private IntegerModel model;
    private Runnable modelCB;
    private int pageSize;
    private int stepSize;
    private boolean scaleThumb;
    private int minValue;
    private int maxValue;
    private int value;
    
    public Scrollbar() {
        this(Orientation.VERTICAL);
    }
    
    public Scrollbar(final Orientation orientation) {
        this.orientation = orientation;
        this.btnUpLeft = new Button();
        this.btnDownRight = new Button();
        this.thumb = new DraggableButton();
        final Runnable cbUpdateTimer = new Runnable() {
            @Override
            public void run() {
                Scrollbar.this.updateTimer();
            }
        };
        if (orientation == Orientation.HORIZONTAL) {
            this.setTheme("hscrollbar");
            this.btnUpLeft.setTheme("leftbutton");
            this.btnDownRight.setTheme("rightbutton");
        }
        else {
            this.setTheme("vscrollbar");
            this.btnUpLeft.setTheme("upbutton");
            this.btnDownRight.setTheme("downbutton");
        }
        this.dragTimerCB = new L();
        this.btnUpLeft.setCanAcceptKeyboardFocus(false);
        this.btnUpLeft.getModel().addStateCallback(cbUpdateTimer);
        this.btnDownRight.setCanAcceptKeyboardFocus(false);
        this.btnDownRight.getModel().addStateCallback(cbUpdateTimer);
        this.thumb.setCanAcceptKeyboardFocus(false);
        this.thumb.setTheme("thumb");
        this.thumb.setListener((DraggableButton.DragListener)this.dragTimerCB);
        this.add((Widget)this.btnUpLeft);
        this.add((Widget)this.btnDownRight);
        this.add((Widget)this.thumb);
        this.pageSize = 10;
        this.stepSize = 1;
        this.maxValue = 100;
        this.setSize(30, 200);
        this.setDepthFocusTraversal(false);
    }
    
    public void addCallback(final Runnable cb) {
        this.callbacks = CallbackSupport.addCallbackToList(this.callbacks, cb, Runnable.class);
    }
    
    public void removeCallback(final Runnable cb) {
        this.callbacks = CallbackSupport.removeCallbackFromList(this.callbacks, cb);
    }
    
    protected void doCallback() {
        CallbackSupport.fireCallbacks(this.callbacks);
    }
    
    public Orientation getOrientation() {
        return this.orientation;
    }
    
    public IntegerModel getModel() {
        return this.model;
    }
    
    public void setModel(final IntegerModel model) {
        if (this.model != model) {
            if (this.model != null) {
                this.model.removeCallback(this.modelCB);
            }
            if ((this.model = model) != null) {
                if (this.modelCB == null) {
                    this.modelCB = new Runnable() {
                        @Override
                        public void run() {
                            Scrollbar.this.syncModel();
                        }
                    };
                }
                model.addCallback(this.modelCB);
                this.syncModel();
            }
        }
    }
    
    public int getValue() {
        return this.value;
    }
    
    public void setValue(final int current) {
        this.setValue(current, true);
    }
    
    public void setValue(int value, final boolean fireCallbacks) {
        value = this.range(value);
        final int oldValue = this.value;
        if (oldValue != value) {
            this.value = value;
            this.setThumbPos();
            this.firePropertyChange("value", oldValue, value);
            if (this.model != null) {
                this.model.setValue(value);
            }
            if (fireCallbacks) {
                this.doCallback();
            }
        }
    }
    
    public void scroll(final int amount) {
        if (this.minValue < this.maxValue) {
            this.setValue(this.value + amount);
        }
        else {
            this.setValue(this.value - amount);
        }
    }
    
    public void scrollToArea(int start, int size, int extra) {
        if (size <= 0) {
            return;
        }
        if (extra < 0) {
            extra = 0;
        }
        final int end = start + size;
        start = this.range(start);
        int pos = this.value;
        final int startWithExtra = this.range(start - extra);
        if (startWithExtra < pos) {
            pos = startWithExtra;
        }
        final int pageEnd = pos + this.pageSize;
        final int endWithExtra = end + extra;
        if (endWithExtra > pageEnd) {
            pos = this.range(endWithExtra - this.pageSize);
            if (pos > startWithExtra) {
                size = end - start;
                pos = start - Math.max(0, this.pageSize - size) / 2;
            }
        }
        this.setValue(pos);
    }
    
    public int getMinValue() {
        return this.minValue;
    }
    
    public int getMaxValue() {
        return this.maxValue;
    }
    
    public void setMinMaxValue(final int minValue, final int maxValue) {
        if (maxValue < minValue) {
            throw new IllegalArgumentException("maxValue < minValue");
        }
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.value = this.range(this.value);
        this.setThumbPos();
        this.thumb.setVisible(minValue != maxValue);
    }
    
    public int getPageSize() {
        return this.pageSize;
    }
    
    public void setPageSize(final int pageSize) {
        if (pageSize < 1) {
            throw new IllegalArgumentException("pageSize < 1");
        }
        this.pageSize = pageSize;
        if (this.scaleThumb) {
            this.setThumbPos();
        }
    }
    
    public int getStepSize() {
        return this.stepSize;
    }
    
    public void setStepSize(final int stepSize) {
        if (stepSize < 1) {
            throw new IllegalArgumentException("stepSize < 1");
        }
        this.stepSize = stepSize;
    }
    
    public boolean isScaleThumb() {
        return this.scaleThumb;
    }
    
    public void setScaleThumb(final boolean scaleThumb) {
        this.scaleThumb = scaleThumb;
        this.setThumbPos();
    }
    
    public void externalDragStart() {
        this.thumb.getAnimationState().setAnimationState(Button.STATE_PRESSED, true);
        this.dragTimerCB.dragStarted();
    }
    
    public void externalDragged(final int deltaX, final int deltaY) {
        this.dragTimerCB.dragged(deltaX, deltaY);
    }
    
    public void externalDragStopped() {
        this.thumb.getAnimationState().setAnimationState(Button.STATE_PRESSED, false);
    }
    
    public boolean isUpLeftButtonArmed() {
        return this.btnUpLeft.getModel().isArmed();
    }
    
    public boolean isDownRightButtonArmed() {
        return this.btnDownRight.getModel().isArmed();
    }
    
    public boolean isThumbDragged() {
        return this.thumb.getModel().isPressed();
    }
    
    public void setThumbTooltipContent(final Object tooltipContent) {
        this.thumb.setTooltipContent(tooltipContent);
    }
    
    public Object getThumbTooltipContent() {
        return this.thumb.getTooltipContent();
    }
    
    @Override
    protected void applyTheme(final ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        this.applyThemeScrollbar(themeInfo);
    }
    
    protected void applyThemeScrollbar(final ThemeInfo themeInfo) {
        this.setScaleThumb(themeInfo.getParameter("scaleThumb", false));
        if (this.orientation == Orientation.HORIZONTAL) {
            this.trackImageUpLeft = (Image)themeInfo.getParameterValue("trackImageLeft", false, (Class)Image.class);
            this.trackImageDownRight = (Image)themeInfo.getParameterValue("trackImageRight", false, (Class)Image.class);
        }
        else {
            this.trackImageUpLeft = (Image)themeInfo.getParameterValue("trackImageUp", false, (Class)Image.class);
            this.trackImageDownRight = (Image)themeInfo.getParameterValue("trackImageDown", false, (Class)Image.class);
        }
    }
    
    @Override
    protected void paintWidget(final GUI gui) {
        final int x = this.getInnerX();
        final int y = this.getInnerY();
        if (this.orientation == Orientation.HORIZONTAL) {
            final int h = this.getInnerHeight();
            if (this.trackImageUpLeft != null) {
                this.trackImageUpLeft.draw((AnimationState)this.getAnimationState(), x, y, this.thumb.getX() - x, h);
            }
            if (this.trackImageDownRight != null) {
                final int thumbRight = this.thumb.getRight();
                this.trackImageDownRight.draw((AnimationState)this.getAnimationState(), thumbRight, y, this.getInnerRight() - thumbRight, h);
            }
        }
        else {
            final int w = this.getInnerWidth();
            if (this.trackImageUpLeft != null) {
                this.trackImageUpLeft.draw((AnimationState)this.getAnimationState(), x, y, w, this.thumb.getY() - y);
            }
            if (this.trackImageDownRight != null) {
                final int thumbBottom = this.thumb.getBottom();
                this.trackImageDownRight.draw((AnimationState)this.getAnimationState(), x, thumbBottom, w, this.getInnerBottom() - thumbBottom);
            }
        }
    }
    
    @Override
    protected void afterAddToGUI(final GUI gui) {
        super.afterAddToGUI(gui);
        (this.timer = gui.createTimer()).setCallback(this.dragTimerCB);
        this.timer.setContinuous(true);
        if (this.model != null) {
            this.model.addCallback(this.modelCB);
        }
    }
    
    @Override
    protected void beforeRemoveFromGUI(final GUI gui) {
        super.beforeRemoveFromGUI(gui);
        if (this.model != null) {
            this.model.removeCallback(this.modelCB);
        }
        if (this.timer != null) {
            this.timer.stop();
        }
        this.timer = null;
    }
    
    public boolean handleEvent(final Event evt) {
        if (evt.getType() == Event.Type.MOUSE_BTNUP && evt.getMouseButton() == 0) {
            this.trackClicked = 0;
            this.updateTimer();
        }
        if (!super.handleEvent(evt) && evt.getType() == Event.Type.MOUSE_BTNDOWN && evt.getMouseButton() == 0 && this.isMouseInside(evt)) {
            if (this.orientation == Orientation.HORIZONTAL) {
                this.trackClickLimit = evt.getMouseX();
                if (evt.getMouseX() < this.thumb.getX()) {
                    this.trackClicked = -1;
                }
                else {
                    this.trackClicked = 1;
                }
            }
            else {
                this.trackClickLimit = evt.getMouseY();
                if (evt.getMouseY() < this.thumb.getY()) {
                    this.trackClicked = -1;
                }
                else {
                    this.trackClicked = 1;
                }
            }
            this.updateTimer();
        }
        final boolean page = (evt.getModifiers() & 0x24) != 0x0;
        final int step = page ? this.pageSize : this.stepSize;
        if (evt.getType() == Event.Type.KEY_PRESSED) {
            switch (evt.getKeyCode()) {
                case 203: {
                    if (this.orientation == Orientation.HORIZONTAL) {
                        this.setValue(this.value - step);
                        return true;
                    }
                    break;
                }
                case 205: {
                    if (this.orientation == Orientation.HORIZONTAL) {
                        this.setValue(this.value + step);
                        return true;
                    }
                    break;
                }
                case 200: {
                    if (this.orientation == Orientation.VERTICAL) {
                        this.setValue(this.value - step);
                        return true;
                    }
                    break;
                }
                case 208: {
                    if (this.orientation == Orientation.VERTICAL) {
                        this.setValue(this.value + step);
                        return true;
                    }
                    break;
                }
                case 201: {
                    if (this.orientation == Orientation.VERTICAL) {
                        this.setValue(this.value - this.pageSize);
                        return true;
                    }
                    break;
                }
                case 209: {
                    if (this.orientation == Orientation.VERTICAL) {
                        this.setValue(this.value + this.pageSize);
                        return true;
                    }
                    break;
                }
            }
        }
        if (evt.getType() == Event.Type.MOUSE_WHEEL) {
            this.setValue(this.value - step * evt.getMouseWheelDelta());
        }
        return evt.isMouseEvent();
    }
    
    int range(int current) {
        if (this.minValue < this.maxValue) {
            if (current < this.minValue) {
                current = this.minValue;
            }
            else if (current > this.maxValue) {
                current = this.maxValue;
            }
        }
        else if (current > this.minValue) {
            current = this.minValue;
        }
        else if (current < this.maxValue) {
            current = this.maxValue;
        }
        return current;
    }
    
    void onTimer(final int nextDelay) {
        this.timer.setDelay(nextDelay);
        if (this.trackClicked != 0) {
            int thumbPos;
            if (this.orientation == Orientation.HORIZONTAL) {
                thumbPos = this.thumb.getX();
            }
            else {
                thumbPos = this.thumb.getY();
            }
            if ((this.trackClickLimit - thumbPos) * this.trackClicked > 0) {
                this.scroll(this.trackClicked * this.pageSize);
            }
        }
        else if (this.btnUpLeft.getModel().isArmed()) {
            this.scroll(-this.stepSize);
        }
        else if (this.btnDownRight.getModel().isArmed()) {
            this.scroll(this.stepSize);
        }
    }
    
    void updateTimer() {
        if (this.timer != null) {
            if (this.trackClicked != 0 || this.btnUpLeft.getModel().isArmed() || this.btnDownRight.getModel().isArmed()) {
                if (!this.timer.isRunning()) {
                    this.onTimer(300);
                    if (this.timer != null) {
                        this.timer.start();
                    }
                }
            }
            else {
                this.timer.stop();
            }
        }
    }
    
    void syncModel() {
        this.setMinMaxValue(this.model.getMinValue(), this.model.getMaxValue());
        this.setValue(this.model.getValue());
    }
    
    @Override
    public int getMinWidth() {
        if (this.orientation == Orientation.HORIZONTAL) {
            return Math.max(super.getMinWidth(), this.btnUpLeft.getMinWidth() + this.thumb.getMinWidth() + this.btnDownRight.getMinWidth());
        }
        return Math.max(super.getMinWidth(), this.thumb.getMinWidth());
    }
    
    @Override
    public int getMinHeight() {
        if (this.orientation == Orientation.HORIZONTAL) {
            return Math.max(super.getMinHeight(), this.thumb.getMinHeight());
        }
        return Math.max(super.getMinHeight(), this.btnUpLeft.getMinHeight() + this.thumb.getMinHeight() + this.btnDownRight.getMinHeight());
    }
    
    @Override
    public int getPreferredWidth() {
        return this.getMinWidth();
    }
    
    @Override
    public int getPreferredHeight() {
        return this.getMinHeight();
    }
    
    @Override
    protected void layout() {
        if (this.orientation == Orientation.HORIZONTAL) {
            this.btnUpLeft.setSize(this.btnUpLeft.getPreferredWidth(), this.getHeight());
            this.btnUpLeft.setPosition(this.getX(), this.getY());
            this.btnDownRight.setSize(this.btnUpLeft.getPreferredWidth(), this.getHeight());
            this.btnDownRight.setPosition(this.getX() + this.getWidth() - this.btnDownRight.getWidth(), this.getY());
        }
        else {
            this.btnUpLeft.setSize(this.getWidth(), this.btnUpLeft.getPreferredHeight());
            this.btnUpLeft.setPosition(this.getX(), this.getY());
            this.btnDownRight.setSize(this.getWidth(), this.btnDownRight.getPreferredHeight());
            this.btnDownRight.setPosition(this.getX(), this.getY() + this.getHeight() - this.btnDownRight.getHeight());
        }
        this.setThumbPos();
    }
    
    int calcThumbArea() {
        if (this.orientation == Orientation.HORIZONTAL) {
            return Math.max(1, this.getWidth() - this.btnUpLeft.getWidth() - this.thumb.getWidth() - this.btnDownRight.getWidth());
        }
        return Math.max(1, this.getHeight() - this.btnUpLeft.getHeight() - this.thumb.getHeight() - this.btnDownRight.getHeight());
    }
    
    private void setThumbPos() {
        final int delta = this.maxValue - this.minValue;
        if (this.orientation == Orientation.HORIZONTAL) {
            int thumbWidth = this.thumb.getPreferredWidth();
            if (this.scaleThumb) {
                final long availArea = Math.max(1, this.getWidth() - this.btnUpLeft.getWidth() - this.btnDownRight.getWidth());
                thumbWidth = (int)Math.max(thumbWidth, availArea * this.pageSize / (this.pageSize + delta + 1));
            }
            this.thumb.setSize(thumbWidth, this.getHeight());
            int xpos = this.btnUpLeft.getX() + this.btnUpLeft.getWidth();
            if (delta != 0) {
                xpos += (this.value - this.minValue) * this.calcThumbArea() / delta;
            }
            this.thumb.setPosition(xpos, this.getY());
        }
        else {
            int thumbHeight = this.thumb.getPreferredHeight();
            if (this.scaleThumb) {
                final long availArea = Math.max(1, this.getHeight() - this.btnUpLeft.getHeight() - this.btnDownRight.getHeight());
                thumbHeight = (int)Math.max(thumbHeight, availArea * this.pageSize / (this.pageSize + delta + 1));
            }
            this.thumb.setSize(this.getWidth(), thumbHeight);
            int ypos = this.btnUpLeft.getY() + this.btnUpLeft.getHeight();
            if (delta != 0) {
                ypos += (this.value - this.minValue) * this.calcThumbArea() / delta;
            }
            this.thumb.setPosition(this.getX(), ypos);
        }
    }
    
    public enum Orientation
    {
        HORIZONTAL, 
        VERTICAL;
    }
    
    final class L implements DraggableButton.DragListener, Runnable
    {
        private int startValue;
        
        public void dragStarted() {
            this.startValue = Scrollbar.this.getValue();
        }
        
        public void dragged(final int deltaX, final int deltaY) {
            int mouseDelta;
            if (Scrollbar.this.getOrientation() == Orientation.HORIZONTAL) {
                mouseDelta = deltaX;
            }
            else {
                mouseDelta = deltaY;
            }
            final int delta = (Scrollbar.this.getMaxValue() - Scrollbar.this.getMinValue()) * mouseDelta / Scrollbar.this.calcThumbArea();
            final int newValue = Scrollbar.this.range(this.startValue + delta);
            Scrollbar.this.setValue(newValue);
        }
        
        public void dragStopped() {
        }
        
        public void run() {
            Scrollbar.this.onTimer(75);
        }
    }
}
