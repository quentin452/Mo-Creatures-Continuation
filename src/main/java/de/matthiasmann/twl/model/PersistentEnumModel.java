package de.matthiasmann.twl.model;

import java.util.prefs.*;
import java.util.logging.*;

public class PersistentEnumModel<T extends Enum<T>> extends AbstractEnumModel<T>
{
    private final Preferences prefs;
    private final String prefKey;
    private T value;

    public PersistentEnumModel(final Preferences prefs, final String prefKey, final T defaultValue) {
        this(prefs, prefKey, (defaultValue).getDeclaringClass(), defaultValue);
    }

    public PersistentEnumModel(final Preferences prefs, final String prefKey, final Class<T> enumClass, final T defaultValue) {
        super(enumClass);
        if (prefs == null) {
            throw new NullPointerException("prefs");
        }
        if (prefKey == null) {
            throw new NullPointerException("prefKey");
        }
        if (defaultValue == null) {
            throw new NullPointerException("value");
        }
        this.prefs = prefs;
        this.prefKey = prefKey;
        T storedValue = defaultValue;
        final String storedStr = prefs.get(prefKey, null);
        if (storedStr != null) {
            try {
                storedValue = Enum.valueOf(enumClass, storedStr);
            }
            catch (IllegalArgumentException ex) {
                Logger.getLogger(PersistentEnumModel.class.getName()).log(Level.WARNING, "Unable to parse value '" + storedStr + "' of key '" + prefKey + "' of type " + enumClass, ex);
            }
        }
        this.setValue(storedValue);
    }

    public T getValue() {
        return this.value;
    }

    public void setValue(final T value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        if (this.value != value) {
            this.value = value;
            this.storeSetting();
            this.doCallback();
        }
    }

    private void storeSetting() {
        if (this.prefs != null) {
            this.prefs.put(this.prefKey, this.value.name());
        }
    }
}
