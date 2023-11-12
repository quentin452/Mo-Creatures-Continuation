package de.matthiasmann.twl;

import java.util.*;

public class MenuManager extends PopupWindow
{
    private final boolean isMenuBar;
    private final IdentityHashMap<MenuElement, Widget> popups;
    private final Runnable closeCB;
    private final Runnable timerCB;
    private boolean mouseOverOwner;
    private Widget lastMouseOverWidget;
    private Timer timer;
    
    public MenuManager(final Widget owner, final boolean isMenuBar) {
        super(owner);
        this.isMenuBar = isMenuBar;
        this.popups = new IdentityHashMap<MenuElement, Widget>();
        this.closeCB = new Runnable() {
            @Override
            public void run() {
                MenuManager.this.closePopup();
            }
        };
        this.timerCB = new Runnable() {
            @Override
            public void run() {
                MenuManager.this.popupTimer();
            }
        };
    }
    
    public Runnable getCloseCallback() {
        return this.closeCB;
    }
    
    boolean isSubMenuOpen(final Menu menu) {
        final Widget popup = this.popups.get(menu);
        return popup != null && popup.getParent() == this;
    }
    
    void closeSubMenu(final int level) {
        while (this.getNumChildren() > level) {
            this.closeSubMenu();
        }
    }
    
    Widget openSubMenu(final int level, final Menu menu, final Widget btn, final boolean setPosition) {
        Widget popup = this.popups.get(menu);
        if (popup == null) {
            popup = (Widget)menu.createPopup(this, level + 1, btn);
            this.popups.put((MenuElement)menu, popup);
        }
        if (popup.getParent() == this) {
            this.closeSubMenu(level + 1);
            return popup;
        }
        if (!this.isOpen()) {
            if (!this.openPopup()) {
                this.closePopup();
                return null;
            }
            this.getParent().layoutChildFullInnerArea((Widget)this);
        }
        while (this.getNumChildren() > level) {
            this.closeSubMenu();
        }
        this.add(popup);
        popup.adjustSize();
        if (setPosition) {
            final int popupWidth = popup.getWidth();
            int popupX = btn.getRight();
            int popupY = btn.getY();
            if (level == 0) {
                popupX = btn.getX();
                popupY = btn.getBottom();
            }
            if (popupWidth + btn.getRight() > this.getInnerRight()) {
                popupX = btn.getX() - popupWidth;
                if (popupX < this.getInnerX()) {
                    popupX = this.getInnerRight() - popupWidth;
                }
            }
            final int popupHeight = popup.getHeight();
            if (popupY + popupHeight > this.getInnerBottom()) {
                popupY = Math.max(this.getInnerY(), this.getInnerBottom() - popupHeight);
            }
            popup.setPosition(popupX, popupY);
        }
        return popup;
    }
    
    void closeSubMenu() {
        this.removeChild(this.getNumChildren() - 1);
    }
    
    @Override
    public void closePopup() {
        this.stopTimer();
        final GUI gui = this.getGUI();
        super.closePopup();
        this.removeAllChildren();
        this.popups.clear();
        if (gui != null) {
            gui.resendLastMouseMove();
        }
    }
    
    public Widget getPopupForMenu(final Menu menu) {
        return this.popups.get(menu);
    }
    
    protected void afterAddToGUI(final GUI gui) {
        super.afterAddToGUI(gui);
        (this.timer = gui.createTimer()).setDelay(300);
        this.timer.setCallback(this.timerCB);
    }
    
    protected void layout() {
    }
    
    Widget routeMouseEvent(final Event evt) {
        this.mouseOverOwner = false;
        Widget widget = super.routeMouseEvent(evt);
        if (widget == this && this.isMenuBar && this.getOwner().isMouseInside(evt)) {
            final Widget menuBarWidget = this.getOwner().routeMouseEvent(evt);
            if (menuBarWidget != null) {
                this.mouseOverOwner = true;
                widget = menuBarWidget;
            }
        }
        final Widget mouseOverWidget = this.getWidgetUnderMouse();
        if (this.lastMouseOverWidget != mouseOverWidget) {
            this.lastMouseOverWidget = mouseOverWidget;
            if (this.isMenuBar && widget.getParent() == this.getOwner() && widget instanceof Menu.SubMenuBtn) {
                this.popupTimer();
            }
            else {
                this.startTimer();
            }
        }
        return widget;
    }
    
    @Override
    protected boolean handleEventPopup(final Event evt) {
        if (this.isMenuBar && this.getOwner().handleEvent(evt)) {
            return true;
        }
        if (super.handleEventPopup(evt)) {
            return true;
        }
        if (evt.getType() == Event.Type.MOUSE_CLICKED) {
            this.mouseClickedOutside(evt);
            return true;
        }
        return false;
    }
    
    Widget getWidgetUnderMouse() {
        if (this.mouseOverOwner) {
            return this.getOwner().getWidgetUnderMouse();
        }
        return super.getWidgetUnderMouse();
    }
    
    void popupTimer() {
        if (this.lastMouseOverWidget instanceof Menu.SubMenuBtn && this.lastMouseOverWidget.isEnabled()) {
            ((Menu.SubMenuBtn)this.lastMouseOverWidget).run();
        }
        else if (this.lastMouseOverWidget != this) {
            int level = 0;
            for (Widget w = this.lastMouseOverWidget; w != null; w = w.getParent()) {
                if (w instanceof Menu.MenuPopup) {
                    level = ((Menu.MenuPopup)w).level;
                    break;
                }
            }
            this.closeSubMenu(level);
        }
    }
    
    void startTimer() {
        if (this.timer != null) {
            this.timer.stop();
            this.timer.start();
        }
    }
    
    void stopTimer() {
        if (this.timer != null) {
            this.timer.stop();
        }
    }
}
