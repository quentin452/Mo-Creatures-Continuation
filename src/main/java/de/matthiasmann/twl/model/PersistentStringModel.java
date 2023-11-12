package de.matthiasmann.twl.model;

import java.util.prefs.*;

public class PersistentStringModel extends HasCallback implements StringModel
{
    private final Preferences prefs;
    private final String prefKey;
    private String value;
    
    public PersistentStringModel(final Preferences prefs, final String prefKey, final String defaultValue) {
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
        this.value = prefs.get(prefKey, defaultValue);
    }
    
    public String getValue() {
        return this.value;
    }
    
    public void setValue(final String value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        if (!this.value.equals(value)) {
            this.value = value;
            this.prefs.put(this.prefKey, value);
            this.doCallback();
        }
    }
}
