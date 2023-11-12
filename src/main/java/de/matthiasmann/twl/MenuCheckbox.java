package de.matthiasmann.twl;

import java.beans.*;
import de.matthiasmann.twl.model.*;

public class MenuCheckbox extends MenuElement
{
    private BooleanModel model;
    
    public MenuCheckbox() {
    }
    
    public MenuCheckbox(final BooleanModel model) {
        this.model = model;
    }
    
    public MenuCheckbox(final String name, final BooleanModel model) {
        super(name);
        this.model = model;
    }
    
    public BooleanModel getModel() {
        return this.model;
    }
    
    public void setModel(final BooleanModel model) {
        final BooleanModel oldModel = this.model;
        this.firePropertyChange("model", oldModel, this.model = model);
    }
    
    @Override
    protected Widget createMenuWidget(final MenuManager mm, final int level) {
        final MenuBtn btn = new MenuBtn() {
            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                super.propertyChange(evt);
                ((ToggleButtonModel)this.getModel()).setModel(MenuCheckbox.this.getModel());
            }
        };
        btn.setModel((ButtonModel)new ToggleButtonModel(this.getModel()));
        this.setWidgetTheme((Widget)btn, "checkbox");
        btn.addCallback(mm.getCloseCallback());
        return (Widget)btn;
    }
}
