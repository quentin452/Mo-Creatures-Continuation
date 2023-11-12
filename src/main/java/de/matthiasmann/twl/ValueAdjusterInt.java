package de.matthiasmann.twl;

import de.matthiasmann.twl.model.*;

public class ValueAdjusterInt extends ValueAdjuster
{
    private int value;
    private int minValue;
    private int maxValue;
    private int dragStartValue;
    private IntegerModel model;
    private Runnable modelCallback;

    public ValueAdjusterInt() {
        this.maxValue = 100;
        this.setTheme("valueadjuster");
        this.setDisplayText();
    }

    public ValueAdjusterInt(final IntegerModel model) {
        this.maxValue = 100;
        this.setTheme("valueadjuster");
        this.setModel(model);
    }

    public int getMaxValue() {
        if (this.model != null) {
            this.maxValue = this.model.getMaxValue();
        }
        return this.maxValue;
    }

    public int getMinValue() {
        if (this.model != null) {
            this.minValue = this.model.getMinValue();
        }
        return this.minValue;
    }

    public void setMinMaxValue(final int minValue, final int maxValue) {
        if (maxValue < minValue) {
            throw new IllegalArgumentException("maxValue < minValue");
        }
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.setValue(this.value);
    }

    public int getValue() {
        return this.value;
    }

    public void setValue(int value) {
        value = Math.max(this.getMinValue(), Math.min(this.getMaxValue(), value));
        if (this.value != value) {
            this.value = value;
            if (this.model != null) {
                this.model.setValue(value);
            }
            this.setDisplayText();
        }
    }

    public IntegerModel getModel() {
        return this.model;
    }

    public void setModel(final IntegerModel model) {
        if (this.model != model) {
            this.removeModelCallback();
            if ((this.model = model) != null) {
                this.minValue = model.getMinValue();
                this.maxValue = model.getMaxValue();
                this.addModelCallback();
            }
        }
    }

    protected String onEditStart() {
        return this.formatText();
    }

    protected boolean onEditEnd(final String text) {
        try {
            this.setValue(Integer.parseInt(text));
            return true;
        }
        catch (NumberFormatException ex) {
            return false;
        }
    }

    protected String validateEdit(final String text) {
        try {
            Integer.parseInt(text);
            return null;
        }
        catch (NumberFormatException ex) {
            return ex.toString();
        }
    }

    protected void onEditCanceled() {
    }

    protected boolean shouldStartEdit(final char ch) {
        return (ch >= '0' && ch <= '9') || ch == '-';
    }

    protected void onDragStart() {
        this.dragStartValue = this.value;
    }

    protected void onDragUpdate(final int dragDelta) {
        final int range = Math.max(1, Math.abs(this.getMaxValue() - this.getMinValue()));
        this.setValue(this.dragStartValue + dragDelta / Math.max(3, this.getWidth() / range));
    }

    protected void onDragCancelled() {
        this.setValue(this.dragStartValue);
    }

    protected void doDecrement() {
        this.setValue(this.value - 1);
    }

    protected void doIncrement() {
        this.setValue(this.value + 1);
    }

    protected String formatText() {
        return Integer.toString(this.value);
    }

    protected void syncWithModel() {
        this.cancelEdit();
        this.minValue = this.model.getMinValue();
        this.maxValue = this.model.getMaxValue();
        this.value = this.model.getValue();
        this.setDisplayText();
    }

    protected void afterAddToGUI(final GUI gui) {
        super.afterAddToGUI(gui);
        this.addModelCallback();
    }

    protected void beforeRemoveFromGUI(final GUI gui) {
        this.removeModelCallback();
        super.beforeRemoveFromGUI(gui);
    }

    protected void removeModelCallback() {
        if (this.model != null && this.modelCallback != null) {
            this.model.removeCallback(this.modelCallback);
        }
    }

    protected void addModelCallback() {
        if (this.model != null && this.getGUI() != null) {
            if (this.modelCallback == null) {
                this.modelCallback = new ModelCallback();
            }
            this.model.addCallback(this.modelCallback);
            this.syncWithModel();
        }
    }
}
