package de.matthiasmann.twl.model;

public class SimpleBooleanModel extends HasCallback implements BooleanModel
{
    private boolean value;
    
    public SimpleBooleanModel() {
        this(false);
    }
    
    public SimpleBooleanModel(final boolean value) {
        this.value = value;
    }
    
    public boolean getValue() {
        return this.value;
    }
    
    public void setValue(final boolean value) {
        if (this.value != value) {
            this.value = value;
            this.doCallback();
        }
    }
}
