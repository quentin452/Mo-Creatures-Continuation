package drzhark.guiapi.widget;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.*;

public class WidgetSlider extends ValueAdjusterFloat
{
    private boolean canEdit;
    
    public WidgetSlider(final FloatModel f) {
        super(f);
        this.canEdit = false;
    }
    
    public void setCanEdit(final boolean value) {
        this.canEdit = value;
    }
    
    public boolean getCanEdit() {
        return this.canEdit;
    }
    
    public void startEdit() {
        if (!this.getCanEdit()) {
            this.cancelEdit();
        }
        else {
            super.startEdit();
        }
    }
    
    protected String onEditStart() {
        return Float.toString(this.getValue());
    }
}
