package de.matthiasmann.twl.model;

public class OptionBooleanModel extends AbstractOptionModel
{
    private final IntegerModel optionState;
    private final int optionCode;
    
    public OptionBooleanModel(final IntegerModel optionState, final int optionCode) {
        if (optionState == null) {
            throw new NullPointerException("optionState");
        }
        if (optionCode < optionState.getMinValue() || optionCode > optionState.getMaxValue()) {
            throw new IllegalArgumentException("optionCode");
        }
        this.optionState = optionState;
        this.optionCode = optionCode;
    }
    
    public boolean getValue() {
        return this.optionState.getValue() == this.optionCode;
    }
    
    public void setValue(final boolean value) {
        if (value) {
            this.optionState.setValue(this.optionCode);
        }
    }
    
    protected void installSrcCallback(final Runnable cb) {
        this.optionState.addCallback(cb);
    }
    
    protected void removeSrcCallback(final Runnable cb) {
        this.optionState.removeCallback(cb);
    }
}
