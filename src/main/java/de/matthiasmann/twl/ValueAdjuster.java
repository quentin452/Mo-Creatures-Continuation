package de.matthiasmann.twl;

import de.matthiasmann.twl.renderer.*;

public abstract class ValueAdjuster extends Widget
{
    public static final AnimationState.StateKey STATE_EDIT_ACTIVE;
    private static final int INITIAL_DELAY = 300;
    private static final int REPEAT_DELAY = 75;
    private final DraggableButton label;
    private final EditField editField;
    private final Button decButton;
    private final Button incButton;
    private final Runnable timerCallback;
    private final L listeners;
    private Timer timer;
    private String displayPrefix;
    private String displayPrefixTheme;
    private boolean useMouseWheel;
    private boolean acceptValueOnFocusLoss;
    private boolean wasInEditOnFocusLost;
    private int width;

    public ValueAdjuster() {
        this.displayPrefixTheme = "";
        this.useMouseWheel = true;
        this.acceptValueOnFocusLoss = true;
        this.label = new DraggableButton(this.getAnimationState(), true);
        this.editField = new EditField(this.getAnimationState());
        this.decButton = new Button(this.getAnimationState(), true);
        this.incButton = new Button(this.getAnimationState(), true);
        this.label.setClip(true);
        this.label.setTheme("valueDisplay");
        this.editField.setTheme("valueEdit");
        this.decButton.setTheme("decButton");
        this.incButton.setTheme("incButton");
        final Runnable cbUpdateTimer = new Runnable() {
            @Override
            public void run() {
                ValueAdjuster.this.updateTimer();
            }
        };
        this.timerCallback = new Runnable() {
            @Override
            public void run() {
                ValueAdjuster.this.onTimer(75);
            }
        };
        this.decButton.getModel().addStateCallback(cbUpdateTimer);
        this.incButton.getModel().addStateCallback(cbUpdateTimer);
        this.listeners = new L();
        this.label.addCallback((Runnable)this.listeners);
        this.label.setListener((DraggableButton.DragListener)this.listeners);
        this.editField.setVisible(false);
        this.editField.addCallback((EditField.Callback)this.listeners);
        this.add((Widget)this.label);
        this.add((Widget)this.editField);
        this.add((Widget)this.decButton);
        this.add((Widget)this.incButton);
        this.setCanAcceptKeyboardFocus(true);
        this.setDepthFocusTraversal(false);
    }

    public String getDisplayPrefix() {
        return this.displayPrefix;
    }

    public void setDisplayPrefix(final String displayPrefix) {
        this.displayPrefix = displayPrefix;
        this.setDisplayText();
    }

    public boolean isUseMouseWheel() {
        return this.useMouseWheel;
    }

    public void setAcceptValueOnFocusLoss(final boolean acceptValueOnFocusLoss) {
        this.acceptValueOnFocusLoss = acceptValueOnFocusLoss;
    }

    public boolean isAcceptValueOnFocusLoss() {
        return this.acceptValueOnFocusLoss;
    }

    public void setUseMouseWheel(final boolean useMouseWheel) {
        this.useMouseWheel = useMouseWheel;
    }

    @Override
    public void setTooltipContent(final Object tooltipContent) {
        super.setTooltipContent(tooltipContent);
        this.label.setTooltipContent(tooltipContent);
    }

    public void startEdit() {
        if (this.label.isVisible()) {
            this.editField.setErrorMessage((Object)null);
            this.editField.setText(this.onEditStart());
            this.editField.setVisible(true);
            this.editField.requestKeyboardFocus();
            this.editField.selectAll();
            this.editField.getAnimationState().setAnimationState(EditField.STATE_HOVER, this.label.getModel().isHover());
            this.label.setVisible(false);
            this.getAnimationState().setAnimationState(ValueAdjuster.STATE_EDIT_ACTIVE, true);
        }
    }

    public void cancelEdit() {
        if (this.editField.isVisible()) {
            this.onEditCanceled();
            this.label.setVisible(true);
            this.editField.setVisible(false);
            this.label.getModel().setHover(this.editField.getAnimationState().getAnimationState(Label.STATE_HOVER));
            this.getAnimationState().setAnimationState(ValueAdjuster.STATE_EDIT_ACTIVE, false);
        }
    }

    public void cancelOrAcceptEdit() {
        if (this.editField.isVisible()) {
            if (this.acceptValueOnFocusLoss) {
                this.onEditEnd(this.editField.getText());
            }
            this.cancelEdit();
        }
    }

    @Override
    protected void applyTheme(final ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        this.applyThemeValueAdjuster(themeInfo);
    }

    protected void applyThemeValueAdjuster(final ThemeInfo themeInfo) {
        this.width = themeInfo.getParameter("width", 100);
        this.displayPrefixTheme = themeInfo.getParameter("displayPrefix", "");
        this.useMouseWheel = themeInfo.getParameter("useMouseWheel", this.useMouseWheel);
    }

    @Override
    public int getMinWidth() {
        int minWidth = super.getMinWidth();
        minWidth = Math.max(minWidth, this.getBorderHorizontal() + this.decButton.getMinWidth() + Math.max(this.width, this.label.getMinWidth()) + this.incButton.getMinWidth());
        return minWidth;
    }

    @Override
    public int getMinHeight() {
        int minHeight = this.label.getMinHeight();
        minHeight = Math.max(minHeight, this.decButton.getMinHeight());
        minHeight = Math.max(minHeight, this.incButton.getMinHeight());
        minHeight += this.getBorderVertical();
        return Math.max(minHeight, super.getMinHeight());
    }

    @Override
    public int getPreferredInnerWidth() {
        return this.decButton.getPreferredWidth() + Math.max(this.width, this.label.getPreferredWidth()) + this.incButton.getPreferredWidth();
    }

    @Override
    public int getPreferredInnerHeight() {
        return Math.max(Math.max(this.decButton.getPreferredHeight(), this.incButton.getPreferredHeight()), this.label.getPreferredHeight());
    }

    @Override
    protected void keyboardFocusLost() {
        this.wasInEditOnFocusLost = this.editField.isVisible();
        this.cancelOrAcceptEdit();
        this.label.getAnimationState().setAnimationState(ValueAdjuster.STATE_KEYBOARD_FOCUS, false);
    }

    @Override
    protected void keyboardFocusGained() {
        this.label.getAnimationState().setAnimationState(ValueAdjuster.STATE_KEYBOARD_FOCUS, true);
    }

    @Override
    protected void keyboardFocusGained(final FocusGainedCause cause, final Widget previousWidget) {
        this.keyboardFocusGained();
        this.checkStartEditOnFocusGained(cause, previousWidget);
    }

    @Override
    public void setVisible(final boolean visible) {
        super.setVisible(visible);
        if (!visible) {
            this.cancelEdit();
        }
    }

    @Override
    protected void widgetDisabled() {
        this.cancelEdit();
    }

    @Override
    protected void layout() {
        final int height = this.getInnerHeight();
        final int y = this.getInnerY();
        this.decButton.setPosition(this.getInnerX(), y);
        this.decButton.setSize(this.decButton.getPreferredWidth(), height);
        this.incButton.setPosition(this.getInnerRight() - this.incButton.getPreferredWidth(), y);
        this.incButton.setSize(this.incButton.getPreferredWidth(), height);
        final int labelX = this.decButton.getRight();
        final int labelWidth = Math.max(0, this.incButton.getX() - labelX);
        this.label.setSize(labelWidth, height);
        this.label.setPosition(labelX, y);
        this.editField.setSize(labelWidth, height);
        this.editField.setPosition(labelX, y);
    }

    protected void setDisplayText() {
        final String prefix = (this.displayPrefix != null) ? this.displayPrefix : this.displayPrefixTheme;
        this.label.setText(prefix.concat(this.formatText()));
    }

    protected abstract String formatText();

    void checkStartEditOnFocusGained(final FocusGainedCause cause, Widget previousWidget) {
        if (cause == FocusGainedCause.FOCUS_KEY) {
            if (previousWidget != null && !(previousWidget instanceof ValueAdjuster)) {
                previousWidget = previousWidget.getParent();
            }
            if (previousWidget != this && previousWidget instanceof ValueAdjuster && ((ValueAdjuster)previousWidget).wasInEditOnFocusLost) {
                this.startEdit();
            }
        }
    }

    void onTimer(final int nextDelay) {
        this.timer.setDelay(nextDelay);
        if (this.incButton.getModel().isArmed()) {
            this.cancelEdit();
            this.doIncrement();
        }
        else if (this.decButton.getModel().isArmed()) {
            this.cancelEdit();
            this.doDecrement();
        }
    }

    void updateTimer() {
        if (this.timer != null) {
            if (this.incButton.getModel().isArmed() || this.decButton.getModel().isArmed()) {
                if (!this.timer.isRunning()) {
                    this.onTimer(300);
                    this.timer.start();
                }
            }
            else {
                this.timer.stop();
            }
        }
    }

    @Override
    protected void afterAddToGUI(final GUI gui) {
        super.afterAddToGUI(gui);
        (this.timer = gui.createTimer()).setCallback(this.timerCallback);
        this.timer.setContinuous(true);
    }

    @Override
    protected void beforeRemoveFromGUI(final GUI gui) {
        super.beforeRemoveFromGUI(gui);
        if (this.timer != null) {
            this.timer.stop();
        }
        this.timer = null;
    }

    @Override
    protected boolean handleEvent(final Event evt) {
        if (evt.isKeyEvent()) {
            if (evt.isKeyPressedEvent() && evt.getKeyCode() == 1 && this.listeners.dragActive) {
                this.listeners.dragActive = false;
                this.onDragCancelled();
                return true;
            }
            if (!this.editField.isVisible()) {
                {
                    switch (evt.getType()) {
                        case KEY_PRESSED: {
                            switch (evt.getKeyCode()) {
                                case 205: {
                                    this.doIncrement();
                                    return true;
                                }
                                case 203: {
                                    this.doDecrement();
                                    return true;
                                }
                                case 28:
                                case 57: {
                                    this.startEdit();
                                    return true;
                                }
                                default: {
                                    if (evt.hasKeyCharNoModifiers() && this.shouldStartEdit(evt.getKeyChar())) {
                                        this.startEdit();
                                        this.editField.handleEvent(evt);
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
                return false;
            }
        }
        else if (!this.editField.isVisible() && this.useMouseWheel && evt.getType() == Event.Type.MOUSE_WHEEL) {
            if (evt.getMouseWheelDelta() < 0) {
                this.doDecrement();
            }
            else if (evt.getMouseWheelDelta() > 0) {
                this.doIncrement();
            }
            return true;
        }
        return super.handleEvent(evt);
    }

    protected abstract String onEditStart();

    protected abstract boolean onEditEnd(final String p0);

    protected abstract String validateEdit(final String p0);

    protected abstract void onEditCanceled();

    protected abstract boolean shouldStartEdit(final char p0);

    protected abstract void onDragStart();

    protected abstract void onDragUpdate(final int p0);

    protected abstract void onDragCancelled();

    protected void onDragEnd() {
    }

    protected abstract void doDecrement();

    protected abstract void doIncrement();

    void handleEditCallback(final int key) {
        switch (key) {
            case 28: {
                if (this.onEditEnd(this.editField.getText())) {
                    this.label.setVisible(true);
                    this.editField.setVisible(false);
                    break;
                }
                break;
            }
            case 1: {
                this.cancelEdit();
                break;
            }
            default: {
                this.editField.setErrorMessage((Object)this.validateEdit(this.editField.getText()));
                break;
            }
        }
    }

    protected abstract void syncWithModel();

    static {
        STATE_EDIT_ACTIVE = AnimationState.StateKey.get("editActive");
    }

    class ModelCallback implements Runnable
    {
        @Override
        public void run() {
            ValueAdjuster.this.syncWithModel();
        }
    }

    class L implements Runnable, DraggableButton.DragListener, EditField.Callback
    {
        boolean dragActive;

        @Override
        public void run() {
            ValueAdjuster.this.startEdit();
        }

        public void dragStarted() {
            this.dragActive = true;
            ValueAdjuster.this.onDragStart();
        }

        public void dragged(final int deltaX, final int deltaY) {
            if (this.dragActive) {
                ValueAdjuster.this.onDragUpdate(deltaX);
            }
        }

        public void dragStopped() {
            this.dragActive = false;
            ValueAdjuster.this.onDragEnd();
        }

        public void callback(final int key) {
            ValueAdjuster.this.handleEditCallback(key);
        }
    }
}
