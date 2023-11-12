package de.matthiasmann.twl;

public class MenuAction extends MenuElement
{
    private Runnable cb;
    
    public MenuAction() {
    }
    
    public MenuAction(final Runnable cb) {
        this.cb = cb;
    }
    
    public MenuAction(final String name, final Runnable cb) {
        super(name);
        this.cb = cb;
    }
    
    public Runnable getCallback() {
        return this.cb;
    }
    
    public void setCallback(final Runnable cb) {
        this.cb = cb;
    }
    
    @Override
    protected Widget createMenuWidget(final MenuManager mm, final int level) {
        final Button b = new MenuBtn();
        this.setWidgetTheme((Widget)b, "button");
        b.addCallback(mm.getCloseCallback());
        if (this.cb != null) {
            b.addCallback(this.cb);
        }
        return (Widget)b;
    }
}
