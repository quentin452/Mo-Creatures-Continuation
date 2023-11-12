package de.matthiasmann.twl.model;

public class SimpleIntegerModel extends HasCallback implements IntegerModel
{
    private final int minValue;
    private final int maxValue;
    private int value;
    
    public SimpleIntegerModel(final int minValue, final int maxValue, final int value) {
        if (maxValue < minValue) {
            throw new IllegalArgumentException("maxValue < minValue");
        }
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.value = value;
    }
    
    public int getMaxValue() {
        return this.maxValue;
    }
    
    public int getMinValue() {
        return this.minValue;
    }
    
    public int getValue() {
        return this.value;
    }
    
    public void setValue(final int value) {
        if (this.value != value) {
            this.value = value;
            this.doCallback();
        }
    }
}
