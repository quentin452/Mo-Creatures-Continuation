package de.matthiasmann.twl.model;

import java.util.prefs.*;

public class PersistentBooleanModel extends HasCallback implements BooleanModel
{
    private final Preferences prefs;
    private final String prefKey;
    private boolean value;
    
    public PersistentBooleanModel(final Preferences prefs, final String prefKey, final boolean defaultValue) {
        if (prefs == null) {
            throw new NullPointerException("prefs");
        }
        if (prefKey == null) {
            throw new NullPointerException("prefKey");
        }
        this.prefs = prefs;
        this.prefKey = prefKey;
        this.value = prefs.getBoolean(prefKey, defaultValue);
    }
    
    public boolean getValue() {
        return this.value;
    }
    
    public void setValue(final boolean value) {
        if (this.value != value) {
            this.value = value;
            this.storeSettings();
            this.doCallback();
        }
    }
    
    private void storeSettings() {
        this.prefs.putBoolean(this.prefKey, this.value);
    }
}
