package de.matthiasmann.twl;

import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.model.*;
import de.matthiasmann.twl.utils.*;

public class Button extends TextWidget
{
    public static final AnimationState.StateKey STATE_ARMED;
    public static final AnimationState.StateKey STATE_PRESSED;
    public static final AnimationState.StateKey STATE_SELECTED;
    private final Runnable stateChangedCB;
    private ButtonModel model;
    private String themeText;
    private String text;
    private int mouseButton;

    public Button() {
        this(null, false, null);
    }

    public Button(final ButtonModel model) {
        this(null, false, model);
    }

    public Button(final de.matthiasmann.twl.AnimationState animState) {
        this(animState, false, null);
    }

    public Button(final de.matthiasmann.twl.AnimationState animState, final boolean inherit) {
        this(animState, inherit, null);
    }

    public Button(final String text) {
        this(null, false, null);
        this.setText(text);
    }

    public Button(final de.matthiasmann.twl.AnimationState animState, final ButtonModel model) {
        this(animState, false, model);
    }

    public Button(final de.matthiasmann.twl.AnimationState animState, final boolean inherit, ButtonModel model) {
        super(animState, inherit);
        this.mouseButton = 0;
        this.stateChangedCB = new Runnable() {
            @Override
            public void run() {
                Button.this.modelStateChanged();
            }
        };
        if (model == null) {
            model = new SimpleButtonModel();
        }
        this.setModel(model);
        this.setCanAcceptKeyboardFocus(true);
    }

    public ButtonModel getModel() {
        return this.model;
    }

    public void setModel(final ButtonModel model) {
        if (model == null) {
            throw new NullPointerException("model");
        }
        final boolean isConnected = this.getGUI() != null;
        if (this.model != null) {
            if (isConnected) {
                this.model.disconnect();
            }
            this.model.removeStateCallback(this.stateChangedCB);
        }
        (this.model = model).addStateCallback(this.stateChangedCB);
        if (isConnected) {
            this.model.connect();
        }
        this.modelStateChanged();
        final de.matthiasmann.twl.AnimationState as = this.getAnimationState();
        as.dontAnimate(Button.STATE_ARMED);
        as.dontAnimate(Button.STATE_PRESSED);
        as.dontAnimate(Button.STATE_HOVER);
        as.dontAnimate(Button.STATE_SELECTED);
    }

    @Override
    protected void widgetDisabled() {
        this.disarm();
    }

    @Override
    public void setEnabled(final boolean enabled) {
        this.model.setEnabled(enabled);
    }

    public void addCallback(final Runnable callback) {
        this.model.addActionCallback(callback);
    }

    public void removeCallback(final Runnable callback) {
        this.model.removeActionCallback(callback);
    }

    public boolean hasCallbacks() {
        return this.model.hasActionCallbacks();
    }

    public String getText() {
        return this.text;
    }

    public void setText(final String text) {
        this.text = text;
        this.updateText();
    }

    public int getMouseButton() {
        return this.mouseButton;
    }

    public void setMouseButton(final int mouseButton) {
        if (mouseButton < 0 || mouseButton > 1) {
            throw new IllegalArgumentException("mouseButton");
        }
        this.mouseButton = mouseButton;
    }

    @Override
    protected void applyTheme(final ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        this.applyThemeButton(themeInfo);
    }

    protected void applyThemeButton(final ThemeInfo themeInfo) {
        this.themeText = themeInfo.getParameterValue("text", false, String.class);
        this.updateText();
    }

    @Override
    protected void afterAddToGUI(final GUI gui) {
        super.afterAddToGUI(gui);
        if (this.model != null) {
            this.model.connect();
        }
    }

    @Override
    protected void beforeRemoveFromGUI(final GUI gui) {
        if (this.model != null) {
            this.model.disconnect();
        }
        super.beforeRemoveFromGUI(gui);
    }

    @Override
    public int getMinWidth() {
        return Math.max(super.getMinWidth(), this.getPreferredWidth());
    }

    @Override
    public int getMinHeight() {
        return Math.max(super.getMinHeight(), this.getPreferredHeight());
    }

    protected final void doCallback() {
        this.getModel().fireActionCallback();
    }

    @Override
    public void setVisible(final boolean visible) {
        super.setVisible(visible);
        if (!visible) {
            this.disarm();
        }
    }

    protected void disarm() {
        this.model.setHover(false);
        this.model.setArmed(false);
        this.model.setPressed(false);
    }

    void modelStateChanged() {
        super.setEnabled(this.model.isEnabled());
        final de.matthiasmann.twl.AnimationState as = this.getAnimationState();
        as.setAnimationState(Button.STATE_SELECTED, this.model.isSelected());
        as.setAnimationState(Button.STATE_HOVER, this.model.isHover());
        as.setAnimationState(Button.STATE_ARMED, this.model.isArmed());
        as.setAnimationState(Button.STATE_PRESSED, this.model.isPressed());
    }

    void updateText() {
        if (this.text == null) {
            super.setCharSequence(TextUtil.notNull(this.themeText));
        }
        else {
            super.setCharSequence(this.text);
        }
        this.invalidateLayout();
    }

    @Override
    protected boolean handleEvent(final Event evt) {
        if (evt.isMouseEvent()) {
            final boolean hover = evt.getType() != Event.Type.MOUSE_EXITED && this.isMouseInside(evt);
            this.model.setHover(hover);
            this.model.setArmed(hover && this.model.isPressed());
        }
        Label_0324: {
            switch (evt.getType()) {
                case MOUSE_BTNDOWN: {
                    if (evt.getMouseButton() == this.mouseButton) {
                        this.model.setPressed(true);
                        this.model.setArmed(true);
                        break;
                    }
                    break;
                }
                case MOUSE_BTNUP: {
                    if (evt.getMouseButton() == this.mouseButton) {
                        this.model.setPressed(false);
                        this.model.setArmed(false);
                        break;
                    }
                    break;
                }
                case KEY_PRESSED: {
                    switch (evt.getKeyCode()) {
                        case 28:
                        case 57: {
                            if (!evt.isKeyRepeated()) {
                                this.model.setPressed(true);
                                this.model.setArmed(true);
                            }
                            return true;
                        }
                        default: {
                            break Label_0324;
                        }
                    }
                }
                case KEY_RELEASED: {
                    switch (evt.getKeyCode()) {
                        case 28:
                        case 57: {
                            this.model.setPressed(false);
                            this.model.setArmed(false);
                            return true;
                        }
                        default: {
                            break Label_0324;
                        }
                    }
                }
                case POPUP_OPENED: {
                    this.model.setHover(false);
                    break;
                }
                case MOUSE_WHEEL: {
                    return false;
                }
            }
        }
        return super.handleEvent(evt) || evt.isMouseEvent();
    }

    static {
        STATE_ARMED = AnimationState.StateKey.get("armed");
        STATE_PRESSED = AnimationState.StateKey.get("pressed");
        STATE_SELECTED = AnimationState.StateKey.get("selected");
    }
}
