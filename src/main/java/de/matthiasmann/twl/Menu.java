package de.matthiasmann.twl;

import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.utils.*;
import java.util.*;
import de.matthiasmann.twl.model.*;

public class Menu extends MenuElement implements Iterable<MenuElement>
{
    public static final AnimationState.StateKey STATE_HAS_OPEN_MENUS;
    private final ArrayList<MenuElement> elements;
    private final TypeMapping<Alignment> classAlignments;
    private String popupTheme;
    private Listener[] listeners;
    
    public Menu() {
        this.elements = new ArrayList<MenuElement>();
        this.classAlignments = new TypeMapping<Alignment>();
    }
    
    public Menu(final String name) {
        super(name);
        this.elements = new ArrayList<MenuElement>();
        this.classAlignments = new TypeMapping<Alignment>();
    }
    
    public void addListener(final Listener listener) {
        this.listeners = CallbackSupport.addCallbackToList(this.listeners, listener, Listener.class);
    }
    
    public void removeListener(final Listener listener) {
        this.listeners = CallbackSupport.removeCallbackFromList(this.listeners, listener);
    }
    
    public String getPopupTheme() {
        return this.popupTheme;
    }
    
    public void setPopupTheme(final String popupTheme) {
        final String oldPopupTheme = this.popupTheme;
        this.firePropertyChange("popupTheme", oldPopupTheme, this.popupTheme = popupTheme);
    }
    
    public void setClassAlignment(final Class<? extends MenuElement> clazz, final Alignment value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        if (value == Alignment.FILL) {
            this.classAlignments.remove(clazz);
        }
        else {
            this.classAlignments.put(clazz, value);
        }
    }
    
    public Alignment getClassAlignment(final Class<? extends MenuElement> clazz) {
        final Alignment alignment = this.classAlignments.get(clazz);
        if (alignment == null) {
            return Alignment.FILL;
        }
        return alignment;
    }
    
    @Override
    public Iterator<MenuElement> iterator() {
        return this.elements.iterator();
    }
    
    public MenuElement get(final int index) {
        return this.elements.get(index);
    }
    
    public int getNumElements() {
        return this.elements.size();
    }
    
    public void clear() {
        this.elements.clear();
    }
    
    public Menu add(final MenuElement e) {
        this.elements.add(e);
        return this;
    }
    
    public Menu add(final String name, final Runnable cb) {
        return this.add(new MenuAction(name, cb));
    }
    
    public Menu add(final String name, final BooleanModel model) {
        return this.add(new MenuCheckbox(name, model));
    }
    
    public Menu addSpacer() {
        return this.add(new MenuSpacer());
    }
    
    public void createMenuBar(final Widget container) {
        final MenuManager mm = this.createMenuManager(container, true);
        for (final Widget w : this.createWidgets(mm, 0)) {
            container.add(w);
        }
    }
    
    public Widget createMenuBar() {
        final DialogLayout l = new DialogLayout();
        this.setWidgetTheme((Widget)l, "menubar");
        final MenuManager mm = this.createMenuManager((Widget)l, true);
        final Widget[] widgets = this.createWidgets(mm, 0);
        l.setHorizontalGroup(l.createSequentialGroup().addWidgetsWithGap("menuitem", widgets));
        l.setVerticalGroup(l.createParallelGroup(widgets));
        for (int i = 0, n = this.elements.size(); i < n; ++i) {
            final MenuElement e = this.elements.get(i);
            Alignment alignment = e.getAlignment();
            if (alignment == null) {
                alignment = this.getClassAlignment(e.getClass());
            }
            l.setWidgetAlignment(widgets[i], alignment);
        }
        l.getHorizontalGroup().addGap();
        return (Widget)l;
    }
    
    public MenuManager openPopupMenu(final Widget parent) {
        final MenuManager mm = this.createMenuManager(parent, false);
        mm.openSubMenu(0, this, parent, true);
        return mm;
    }
    
    public MenuManager openPopupMenu(final Widget parent, final int x, final int y) {
        final MenuManager mm = this.createMenuManager(parent, false);
        final Widget popup = mm.openSubMenu(0, this, parent, false);
        if (popup != null) {
            popup.setPosition(x, y);
        }
        return mm;
    }
    
    @Override
    protected Widget createMenuWidget(final MenuManager mm, final int level) {
        final SubMenuBtn smb = new SubMenuBtn(mm, level);
        this.setWidgetTheme((Widget)smb, "submenu");
        return (Widget)smb;
    }
    
    protected MenuManager createMenuManager(final Widget parent, final boolean isMenuBar) {
        return new MenuManager(parent, isMenuBar);
    }
    
    protected Widget[] createWidgets(final MenuManager mm, final int level) {
        final Widget[] widgets = new Widget[this.elements.size()];
        for (int i = 0, n = this.elements.size(); i < n; ++i) {
            final MenuElement e = this.elements.get(i);
            widgets[i] = e.createMenuWidget(mm, level);
        }
        return widgets;
    }
    
    DialogLayout createPopup(final MenuManager mm, final int level, final Widget btn) {
        if (this.listeners != null) {
            for (final Listener l : this.listeners) {
                l.menuOpening(this);
            }
        }
        final Widget[] widgets = this.createWidgets(mm, level);
        final MenuPopup popup = new MenuPopup(btn, level, this);
        if (this.popupTheme != null) {
            popup.setTheme(this.popupTheme);
        }
        popup.setHorizontalGroup(popup.createParallelGroup(widgets));
        popup.setVerticalGroup(popup.createSequentialGroup().addWidgetsWithGap("menuitem", widgets));
        return popup;
    }
    
    void fireMenuOpened() {
        if (this.listeners != null) {
            for (final Listener l : this.listeners) {
                l.menuOpened(this);
            }
        }
    }
    
    void fireMenuClosed() {
        if (this.listeners != null) {
            for (final Listener l : this.listeners) {
                l.menuClosed(this);
            }
        }
    }
    
    static {
        STATE_HAS_OPEN_MENUS = AnimationState.StateKey.get("hasOpenMenus");
    }
    
    static class MenuPopup extends DialogLayout
    {
        private final Widget btn;
        private final Menu menu;
        final int level;
        
        MenuPopup(final Widget btn, final int level, final Menu menu) {
            this.btn = btn;
            this.menu = menu;
            this.level = level;
        }
        
        protected void afterAddToGUI(final GUI gui) {
            super.afterAddToGUI(gui);
            this.menu.fireMenuOpened();
            this.btn.getAnimationState().setAnimationState(Menu.STATE_HAS_OPEN_MENUS, true);
        }
        
        protected void beforeRemoveFromGUI(final GUI gui) {
            this.btn.getAnimationState().setAnimationState(Menu.STATE_HAS_OPEN_MENUS, false);
            this.menu.fireMenuClosed();
            super.beforeRemoveFromGUI(gui);
        }
        
        protected boolean handleEvent(final Event evt) {
            return super.handleEvent(evt) || evt.isMouseEventNoWheel();
        }
    }
    
    class SubMenuBtn extends MenuBtn implements Runnable
    {
        private final MenuManager mm;
        private final int level;
        
        public SubMenuBtn(final MenuManager mm, final int level) {
            this.mm = mm;
            this.level = level;
            this.addCallback((Runnable)this);
        }
        
        @Override
        public void run() {
            this.mm.openSubMenu(this.level, Menu.this, (Widget)this, true);
        }
    }
    
    public interface Listener
    {
        void menuOpening(final Menu p0);
        
        void menuOpened(final Menu p0);
        
        void menuClosed(final Menu p0);
    }
}
