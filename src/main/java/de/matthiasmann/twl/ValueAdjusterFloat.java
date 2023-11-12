package de.matthiasmann.twl;

import de.matthiasmann.twl.model.*;
import java.util.*;
import java.text.*;

public class ValueAdjusterFloat extends ValueAdjuster
{
    private float value;
    private float minValue;
    private float maxValue;
    private float dragStartValue;
    private float stepSize;
    private FloatModel model;
    private Runnable modelCallback;
    private String format;
    private Locale locale;

    public ValueAdjusterFloat() {
        this.maxValue = 100.0f;
        this.stepSize = 1.0f;
        this.format = "%.2f";
        this.locale = Locale.ENGLISH;
        this.setTheme("valueadjuster");
        this.setDisplayText();
    }

    public ValueAdjusterFloat(final FloatModel model) {
        this.maxValue = 100.0f;
        this.stepSize = 1.0f;
        this.format = "%.2f";
        this.locale = Locale.ENGLISH;
        this.setTheme("valueadjuster");
        this.setModel(model);
    }

    public float getMaxValue() {
        return this.maxValue;
    }

    public float getMinValue() {
        return this.minValue;
    }

    public void setMinMaxValue(final float minValue, final float maxValue) {
        if (maxValue < minValue) {
            throw new IllegalArgumentException("maxValue < minValue");
        }
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.setValue(this.value);
    }

    public float getValue() {
        return this.value;
    }

    public void setValue(float value) {
        if (value > this.maxValue) {
            value = this.maxValue;
        }
        else if (value < this.minValue) {
            value = this.minValue;
        }
        if (this.value != value) {
            this.value = value;
            if (this.model != null) {
                this.model.setValue(value);
            }
            this.setDisplayText();
        }
    }

    public float getStepSize() {
        return this.stepSize;
    }

    public void setStepSize(final float stepSize) {
        if (stepSize <= 0.0f) {
            throw new IllegalArgumentException("stepSize");
        }
        this.stepSize = stepSize;
    }

    public FloatModel getModel() {
        return this.model;
    }

    public void setModel(final FloatModel model) {
        if (this.model != model) {
            this.removeModelCallback();
            if ((this.model = model) != null) {
                this.minValue = model.getMinValue();
                this.maxValue = model.getMaxValue();
                this.addModelCallback();
            }
        }
    }

    public String getFormat() {
        return this.format;
    }

    public void setFormat(final String format) throws IllegalFormatException {
        String.format(this.locale, format, 42.0f);
        this.format = format;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public void setLocale(final Locale locale) {
        if (locale == null) {
            throw new NullPointerException("locale");
        }
        this.locale = locale;
    }

    protected String onEditStart() {
        return this.formatText();
    }

    protected boolean onEditEnd(final String text) {
        try {
            this.setValue(this.parseText(text));
            return true;
        }
        catch (ParseException ex) {
            return false;
        }
    }

    protected String validateEdit(final String text) {
        try {
            this.parseText(text);
            return null;
        }
        catch (ParseException ex) {
            return ex.toString();
        }
    }

    protected void onEditCanceled() {
    }

    protected boolean shouldStartEdit(final char ch) {
        return (ch >= '0' && ch <= '9') || ch == '-' || ch == '.';
    }

    protected void onDragStart() {
        this.dragStartValue = this.value;
    }

    protected void onDragUpdate(final int dragDelta) {
        final float range = Math.max(1.0E-4f, Math.abs(this.getMaxValue() - this.getMinValue()));
        this.setValue(this.dragStartValue + dragDelta / Math.max(3.0f, this.getWidth() / range));
    }

    protected void onDragCancelled() {
        this.setValue(this.dragStartValue);
    }

    protected void doDecrement() {
        this.setValue(this.value - this.getStepSize());
    }

    protected void doIncrement() {
        this.setValue(this.value + this.getStepSize());
    }

    protected String formatText() {
        return String.format(this.locale, this.format, this.value);
    }

    protected float parseText(final String value) throws ParseException {
        return NumberFormat.getNumberInstance(this.locale).parse(value).floatValue();
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
