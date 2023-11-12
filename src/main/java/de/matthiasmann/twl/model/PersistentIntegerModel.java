package de.matthiasmann.twl.model;

import java.util.prefs.*;

public class PersistentIntegerModel extends AbstractIntegerModel
{
    private final Preferences prefs;
    private final String prefKey;
    private final int minValue;
    private final int maxValue;
    private int value;
    
    public PersistentIntegerModel(final Preferences prefs, final String prefKey, final int minValue, final int maxValue, final int defaultValue) {
        if (maxValue < minValue) {
            throw new IllegalArgumentException("maxValue < minValue");
        }
        if (prefs == null) {
            throw new NullPointerException("prefs");
        }
        if (prefKey == null) {
            throw new NullPointerException("prefKey");
        }
        this.prefs = prefs;
        this.prefKey = prefKey;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.setValue(prefs.getInt(prefKey, defaultValue));
    }
    
    public PersistentIntegerModel(final int minValue, final int maxValue, final int value) {
        if (maxValue < minValue) {
            throw new IllegalArgumentException("maxValue < minValue");
        }
        this.prefs = null;
        this.prefKey = null;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.setValue(value);
    }
    
    public int getValue() {
        return this.value;
    }
    
    public int getMinValue() {
        return this.minValue;
    }
    
    public int getMaxValue() {
        return this.maxValue;
    }
    
    public void setValue(int value) {
        if (value > this.maxValue) {
            value = this.maxValue;
        }
        else if (value < this.minValue) {
            value = this.minValue;
        }
        if (this.value != value) {
            this.value = value;
            this.storeSetting();
            this.doCallback();
        }
    }
    
    private void storeSetting() {
        if (this.prefs != null) {
            this.prefs.putInt(this.prefKey, this.value);
        }
    }
}
