package de.matthiasmann.twl;

public class InfoWindow extends Container
{
    private final Widget owner;
    
    public InfoWindow(final Widget owner) {
        if (owner == null) {
            throw new NullPointerException("owner");
        }
        this.owner = owner;
    }
    
    public Widget getOwner() {
        return this.owner;
    }
    
    public boolean isOpen() {
        return this.getParent() != null;
    }
    
    public boolean openInfo() {
        if (this.getParent() != null) {
            return true;
        }
        if (isParentInfoWindow(this.owner)) {
            return false;
        }
        final GUI gui = this.owner.getGUI();
        if (gui != null) {
            gui.openInfo(this);
            this.focusFirstChild();
            return true;
        }
        return false;
    }
    
    public void closeInfo() {
        final GUI gui = this.getGUI();
        if (gui != null) {
            gui.closeInfo(this);
        }
    }
    
    protected void infoWindowClosed() {
    }
    
    private static boolean isParentInfoWindow(Widget w) {
        while (w != null) {
            if (w instanceof InfoWindow) {
                return true;
            }
            w = w.getParent();
        }
        return false;
    }
}
