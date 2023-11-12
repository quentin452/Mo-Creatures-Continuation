package drzhark.guiapi.setting;

import org.lwjgl.input.*;
import drzhark.guiapi.*;

public class SettingKey extends Setting<Integer>
{
    public SettingKey(final String title, final int key) {
        this.defaultValue = key;
        this.values.put("", key);
        this.backendName = title;
    }
    
    public SettingKey(final String title, final String key) {
        this(title, Keyboard.getKeyIndex(key));
    }
    
    public void fromString(final String s, final String context) {
        if (s.equals("UNBOUND")) {
            this.values.put(context, 0);
        }
        else {
            this.values.put(context, Keyboard.getKeyIndex(s));
        }
        if (this.displayWidget != null) {
            this.displayWidget.update();
        }
    }
    
    public Integer get(final String context) {
        if (this.values.get(context) != null) {
            return this.values.get(context);
        }
        if (this.values.get("") != null) {
            return this.values.get("");
        }
        return (Integer)this.defaultValue;
    }
    
    public boolean isKeyDown() {
        return this.isKeyDown(ModSettings.currentContext);
    }
    
    public boolean isKeyDown(final String context) {
        return this.get(context) != -1 && Keyboard.isKeyDown((int)this.get(context));
    }
    
    public void set(final Integer v, final String context) {
        this.values.put(context, v);
        if (this.parent != null) {
            this.parent.save(context);
        }
        if (this.displayWidget != null) {
            this.displayWidget.update();
        }
    }
    
    public void set(final String v) {
        this.set(v, ModSettings.currentContext);
    }
    
    public void set(final String v, final String context) {
        this.set(Keyboard.getKeyIndex(v), context);
    }
    
    public String toString(final String context) {
        return Keyboard.getKeyName((int)this.get(context));
    }
}
