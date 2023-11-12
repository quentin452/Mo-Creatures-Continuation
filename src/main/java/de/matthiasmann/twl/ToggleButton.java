package de.matthiasmann.twl;

import de.matthiasmann.twl.model.*;

public class ToggleButton extends Button
{
    public ToggleButton() {
        super((ButtonModel)new ToggleButtonModel());
    }
    
    public ToggleButton(final BooleanModel model) {
        super((ButtonModel)new ToggleButtonModel(model));
    }
    
    public ToggleButton(final String text) {
        this();
        this.setText(text);
    }
    
    public void setModel(final BooleanModel model) {
        ((ToggleButtonModel)this.getModel()).setModel(model);
    }
    
    public boolean isActive() {
        return this.getModel().isSelected();
    }
    
    public void setActive(final boolean active) {
        this.getModel().setSelected(active);
    }
}
