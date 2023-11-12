package de.matthiasmann.twl;

public class PopupWindow extends Container
{
    private final Widget owner;
    private boolean closeOnClickedOutside;
    private boolean closeOnEscape;
    private Runnable requestCloseCallback;
    
    public PopupWindow(final Widget owner) {
        this.closeOnClickedOutside = true;
        this.closeOnEscape = true;
        if (owner == null) {
            throw new NullPointerException("owner");
        }
        this.owner = owner;
    }
    
    public Widget getOwner() {
        return this.owner;
    }
    
    public boolean isCloseOnClickedOutside() {
        return this.closeOnClickedOutside;
    }
    
    public void setCloseOnClickedOutside(final boolean closeOnClickedOutside) {
        this.closeOnClickedOutside = closeOnClickedOutside;
    }
    
    public boolean isCloseOnEscape() {
        return this.closeOnEscape;
    }
    
    public void setCloseOnEscape(final boolean closeOnEscape) {
        this.closeOnEscape = closeOnEscape;
    }
    
    public Runnable getRequestCloseCallback() {
        return this.requestCloseCallback;
    }
    
    public void setRequestCloseCallback(final Runnable requestCloseCallback) {
        this.requestCloseCallback = requestCloseCallback;
    }
    
    public boolean openPopup() {
        final GUI gui = this.owner.getGUI();
        if (gui != null) {
            super.setVisible(true);
            super.setEnabled(true);
            gui.openPopup(this);
            this.requestKeyboardFocus();
            this.focusFirstChild();
            return this.isOpen();
        }
        return false;
    }
    
    public void openPopupCentered() {
        if (this.openPopup()) {
            this.adjustSize();
            this.centerPopup();
        }
    }
    
    public void openPopupCentered(final int width, final int height) {
        if (this.openPopup()) {
            this.setSize(Math.min(this.getParent().getInnerWidth(), width), Math.min(this.getParent().getInnerHeight(), height));
            this.centerPopup();
        }
    }
    
    public void closePopup() {
        final GUI gui = this.getGUI();
        if (gui != null) {
            gui.closePopup(this);
            this.owner.requestKeyboardFocus();
        }
    }
    
    public final boolean isOpen() {
        return this.getParent() != null;
    }
    
    public void centerPopup() {
        final Widget parent = this.getParent();
        if (parent != null) {
            this.setPosition(parent.getInnerX() + (parent.getInnerWidth() - this.getWidth()) / 2, parent.getInnerY() + (parent.getInnerHeight() - this.getHeight()) / 2);
        }
    }
    
    public boolean bindMouseDrag(final Runnable cb) {
        final GUI gui = this.getGUI();
        return gui != null && gui.bindDragEvent(this, cb);
    }
    
    public int getPreferredWidth() {
        final int parentWidth = (this.getParent() != null) ? this.getParent().getInnerWidth() : 32767;
        return Math.min(parentWidth, super.getPreferredWidth());
    }
    
    public int getPreferredHeight() {
        final int parentHeight = (this.getParent() != null) ? this.getParent().getInnerHeight() : 32767;
        return Math.min(parentHeight, super.getPreferredHeight());
    }
    
    protected final boolean handleEvent(final Event evt) {
        if (this.handleEventPopup(evt)) {
            return true;
        }
        if (evt.getType() == Event.Type.MOUSE_CLICKED && !this.isInside(evt.getMouseX(), evt.getMouseY())) {
            this.mouseClickedOutside(evt);
            return true;
        }
        if (this.closeOnEscape && evt.isKeyPressedEvent() && evt.getKeyCode() == 1) {
            this.requestPopupClose();
            return true;
        }
        return true;
    }
    
    protected boolean handleEventPopup(final Event evt) {
        return super.handleEvent(evt);
    }
    
    protected final boolean isMouseInside(final Event evt) {
        return true;
    }
    
    protected void requestPopupClose() {
        if (this.requestCloseCallback != null) {
            this.requestCloseCallback.run();
        }
        else {
            this.closePopup();
        }
    }
    
    protected void mouseClickedOutside(final Event evt) {
        if (this.closeOnClickedOutside) {
            this.requestPopupClose();
        }
    }
    
    void setParent(final Widget parent) {
        if (!(parent instanceof GUI)) {
            throw new IllegalArgumentException("PopupWindow can't be used as child widget");
        }
        super.setParent(parent);
    }
}
