package de.matthiasmann.twl.model;

public class OptionEnumModel<T extends Enum<T>> extends AbstractOptionModel
{
    private final EnumModel<T> optionState;
    private final T optionCode;

    public OptionEnumModel(final EnumModel<T> optionState, final T optionCode) {
        if (optionState == null) {
            throw new NullPointerException("optionState");
        }
        if (optionCode == null) {
            throw new NullPointerException("optionCode");
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
