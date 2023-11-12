package de.matthiasmann.twl.model;

import java.util.prefs.*;
import de.matthiasmann.twl.*;

public class PersistentColorModel extends HasCallback implements ColorModel
{
    private final Preferences prefs;
    private final String prefKey;
    private Color value;
    private IllegalArgumentException initialError;
    
    public PersistentColorModel(final Preferences prefs, final String prefKey, final Color defaultValue) {
        if (prefs == null) {
            throw new NullPointerException("prefs");
        }
        if (prefKey == null) {
            throw new NullPointerException("prefKey");
        }
        if (defaultValue == null) {
            throw new NullPointerException("defaultValue");
        }
        this.prefs = prefs;
        this.prefKey = prefKey;
        this.value = defaultValue;
        try {
            final String text = prefs.get(prefKey, null);
            if (text != null) {
                final Color aValue = Color.parserColor(text);
                if (aValue != null) {
                    this.value = aValue;
                }
                else {
                    this.initialError = new IllegalArgumentException("Unknown color name: " + text);
                }
            }
        }
        catch (IllegalArgumentException ex) {
            this.initialError = ex;
        }
    }
    
    public IllegalArgumentException getInitialError() {
        return this.initialError;
    }
    
    public void clearInitialError() {
        this.initialError = null;
    }
    
    public Color getValue() {
        return this.value;
    }
    
    public void setValue(final Color value) {
        if (this.value != value) {
            this.value = value;
            this.storeSettings();
            this.doCallback();
        }
    }
    
    private void storeSettings() {
        this.prefs.put(this.prefKey, this.value.toString());
    }
}
