package de.matthiasmann.twl;

import java.util.*;
import de.matthiasmann.twl.model.*;

public class RadialPopupMenu extends PopupWindow
{
    private final ArrayList<RoundButton> buttons;
    private int radius;
    private int buttonRadius;
    private int mouseButton;
    int buttonRadiusSqr;
    
    public RadialPopupMenu(final Widget owner) {
        super(owner);
        this.buttons = new ArrayList<RoundButton>();
    }
    
    public int getButtonRadius() {
        return this.buttonRadius;
    }
    
    public void setButtonRadius(final int buttonRadius) {
        if (buttonRadius < 0) {
            throw new IllegalArgumentException("buttonRadius");
        }
        this.buttonRadius = buttonRadius;
        this.buttonRadiusSqr = buttonRadius * buttonRadius;
        this.invalidateLayout();
    }
    
    public int getRadius() {
        return this.radius;
    }
    
    public void setRadius(final int radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("radius");
        }
        this.radius = radius;
        this.invalidateLayout();
    }
    
    public int getMouseButton() {
        return this.mouseButton;
    }
    
    public void setMouseButton(final int mouseButton) {
        if (mouseButton < 0 || mouseButton > 1) {
            throw new IllegalArgumentException("mouseButton");
        }
        this.mouseButton = mouseButton;
        for (int i = 0, n = this.buttons.size(); i < n; ++i) {
            this.buttons.get(i).setMouseButton(mouseButton);
        }
    }
    
    public Button addButton(final String theme, final Runnable cb) {
        final RoundButton button = new RoundButton();
        button.setTheme(theme);
        button.addCallback(cb);
        button.setMouseButton(this.mouseButton);
        this.addButton(button);
        return button;
    }
    
    public void removeButton(final Button btn) {
        final int idx = this.buttons.indexOf(btn);
        if (idx >= 0) {
            this.buttons.remove(idx);
            this.removeChild((Widget)btn);
        }
    }
    
    protected void addButton(final RoundButton button) {
        if (button == null) {
            throw new NullPointerException("button");
        }
        this.buttons.add(button);
        this.add((Widget)button);
    }
    
    public boolean openPopup() {
        if (super.openPopup()) {
            if (this.bindMouseDrag((Runnable)new Runnable() {
                @Override
                public void run() {
                    RadialPopupMenu.this.boundDragEventFinished();
                }
            })) {
                this.setAllButtonsPressed();
            }
            return true;
        }
        return false;
    }
    
    public boolean openPopupAt(final int centerX, final int centerY) {
        if (this.openPopup()) {
            this.adjustSize();
            final Widget parent = this.getParent();
            final int width = this.getWidth();
            final int height = this.getHeight();
            this.setPosition(limit(centerX - width / 2, parent.getInnerX(), parent.getInnerRight() - width), limit(centerY - height / 2, parent.getInnerY(), parent.getInnerBottom() - height));
            return true;
        }
        return false;
    }
    
    protected static int limit(final int value, final int min, final int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
    
    public boolean openPopup(final Event evt) {
        if (evt.getType() == Event.Type.MOUSE_BTNDOWN) {
            this.setMouseButton(evt.getMouseButton());
            return this.openPopupAt(evt.getMouseX(), evt.getMouseY());
        }
        return false;
    }
    
    public int getPreferredInnerWidth() {
        return 2 * (this.radius + this.buttonRadius);
    }
    
    public int getPreferredInnerHeight() {
        return 2 * (this.radius + this.buttonRadius);
    }
    
    protected void applyTheme(final ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        this.applyThemeRadialPopupMenu(themeInfo);
    }
    
    protected void applyThemeRadialPopupMenu(final ThemeInfo themeInfo) {
        this.setRadius(themeInfo.getParameter("radius", 40));
        this.setButtonRadius(themeInfo.getParameter("buttonRadius", 40));
    }
    
    protected void layout() {
        this.layoutRadial();
    }
    
    protected void layoutRadial() {
        final int numButtons = this.buttons.size();
        if (numButtons > 0) {
            final int centerX = this.getInnerX() + this.getInnerWidth() / 2;
            final int centerY = this.getInnerY() + this.getInnerHeight() / 2;
            final float toRad = 6.2831855f / numButtons;
            for (int i = 0; i < numButtons; ++i) {
                final float rad = i * toRad;
                final int btnCenterX = centerX + (int)(this.radius * Math.sin(rad));
                final int btnCenterY = centerY - (int)(this.radius * Math.cos(rad));
                final RoundButton button = this.buttons.get(i);
                button.setPosition(btnCenterX - this.buttonRadius, btnCenterY - this.buttonRadius);
                button.setSize(2 * this.buttonRadius, 2 * this.buttonRadius);
            }
        }
    }
    
    protected void setAllButtonsPressed() {
        for (int i = 0, n = this.buttons.size(); i < n; ++i) {
            final ButtonModel model = this.buttons.get(i).getModel();
            model.setPressed(true);
            model.setArmed(model.isHover());
        }
    }
    
    protected void boundDragEventFinished() {
        this.closePopup();
    }
    
    protected class RoundButton extends Button
    {
        public boolean isInside(final int x, final int y) {
            final int dx = x - (this.getX() + this.getWidth() / 2);
            final int dy = y - (this.getY() + this.getHeight() / 2);
            return dx * dx + dy * dy <= RadialPopupMenu.this.buttonRadiusSqr;
        }
    }
}
