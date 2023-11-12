package de.matthiasmann.twl.model;

public class SimpleFloatModel extends AbstractFloatModel
{
    private final float minValue;
    private final float maxValue;
    private float value;
    
    public SimpleFloatModel(final float minValue, final float maxValue, final float value) {
        if (Float.isNaN(minValue)) {
            throw new IllegalArgumentException("minValue is NaN");
        }
        if (Float.isNaN(maxValue)) {
            throw new IllegalArgumentException("maxValue is NaN");
        }
        if (minValue > maxValue) {
            throw new IllegalArgumentException("minValue > maxValue");
        }
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.value = this.limit(value);
    }
    
    public float getMaxValue() {
        return this.maxValue;
    }
    
    public float getMinValue() {
        return this.minValue;
    }
    
    public float getValue() {
        return this.value;
    }
    
    public void setValue(float value) {
        value = this.limit(value);
        if (this.value != value) {
            this.value = value;
            this.doCallback();
        }
    }
    
    protected float limit(final float value) {
        if (Float.isNaN(value)) {
            return this.minValue;
        }
        return Math.max(this.minValue, Math.min(this.maxValue, value));
    }
}
