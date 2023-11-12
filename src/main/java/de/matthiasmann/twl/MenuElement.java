package de.matthiasmann.twl;

import java.beans.*;

public abstract class MenuElement
{
    private String name;
    private String theme;
    private boolean enabled;
    private Object tooltipContent;
    private PropertyChangeSupport pcs;
    private Alignment alignment;
    
    public MenuElement() {
        this.enabled = true;
    }
    
    public MenuElement(final String name) {
        this.enabled = true;
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public MenuElement setName(final String name) {
        final String oldName = this.name;
        this.firePropertyChange("name", oldName, this.name = name);
        return this;
    }
    
    public String getTheme() {
        return this.theme;
    }
    
    public MenuElement setTheme(final String theme) {
        final String oldTheme = this.theme;
        this.firePropertyChange("theme", oldTheme, this.theme = theme);
        return this;
    }
    
    public boolean isEnabled() {
        return this.enabled;
    }
    
    public MenuElement setEnabled(final boolean enabled) {
        final boolean oldEnabled = this.enabled;
        this.firePropertyChange("enabled", oldEnabled, this.enabled = enabled);
        return this;
    }
    
    public Object getTooltipContent() {
        return this.tooltipContent;
    }
    
    public MenuElement setTooltipContent(final Object tooltip) {
        final Object oldTooltip = this.tooltipContent;
        this.firePropertyChange("tooltipContent", oldTooltip, this.tooltipContent = tooltip);
        return this;
    }
    
    public Alignment getAlignment() {
        return this.alignment;
    }
    
    public MenuElement setAlignment(final Alignment alignment) {
        final Alignment oldAlignment = this.alignment;
        this.firePropertyChange("alignment", oldAlignment, this.alignment = alignment);
        return this;
    }
    
    protected abstract Widget createMenuWidget(final MenuManager p0, final int p1);
    
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        if (this.pcs == null) {
            this.pcs = new PropertyChangeSupport(this);
        }
        this.pcs.addPropertyChangeListener(listener);
    }
    
    public void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
        if (this.pcs == null) {
            this.pcs = new PropertyChangeSupport(this);
        }
        this.pcs.addPropertyChangeListener(propertyName, listener);
    }
    
    public void removePropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
        if (this.pcs != null) {
            this.pcs.removePropertyChangeListener(propertyName, listener);
        }
    }
    
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        if (this.pcs != null) {
            this.pcs.removePropertyChangeListener(listener);
        }
    }
    
    protected void firePropertyChange(final String propertyName, final boolean oldValue, final boolean newValue) {
        if (this.pcs != null) {
            this.pcs.firePropertyChange(propertyName, oldValue, newValue);
        }
    }
    
    protected void firePropertyChange(final String propertyName, final int oldValue, final int newValue) {
        if (this.pcs != null) {
            this.pcs.firePropertyChange(propertyName, oldValue, newValue);
        }
    }
    
    protected void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
        if (this.pcs != null) {
            this.pcs.firePropertyChange(propertyName, oldValue, newValue);
        }
    }
    
    protected void setWidgetTheme(final Widget w, final String defaultTheme) {
        if (this.theme != null) {
            w.setTheme(this.theme);
        }
        else {
            w.setTheme(defaultTheme);
        }
    }
    
    class MenuBtn extends Button implements PropertyChangeListener
    {
        public MenuBtn() {
            this.sync();
        }
        
        protected void afterAddToGUI(final GUI gui) {
            super.afterAddToGUI(gui);
            MenuElement.this.addPropertyChangeListener(this);
        }
        
        protected void beforeRemoveFromGUI(final GUI gui) {
            MenuElement.this.removePropertyChangeListener(this);
            super.beforeRemoveFromGUI(gui);
        }
        
        public void propertyChange(final PropertyChangeEvent evt) {
            this.sync();
        }
        
        protected void sync() {
            this.setEnabled(MenuElement.this.isEnabled());
            this.setTooltipContent(MenuElement.this.getTooltipContent());
            this.setText(MenuElement.this.getName());
        }
    }
}
