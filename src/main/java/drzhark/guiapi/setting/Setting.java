package drzhark.guiapi.setting;

import de.matthiasmann.twl.*;
import drzhark.guiapi.widget.*;
import drzhark.guiapi.*;
import java.util.*;

public abstract class Setting<T> extends Widget
{
    public String backendName;
    public T defaultValue;
    public WidgetSetting displayWidget;
    public ModSettings parent;
    public HashMap<String, T> values;
    
    public Setting() {
        this.displayWidget = null;
        this.parent = null;
        this.values = new HashMap<String, T>();
    }
    
    public void copyContext(final String srccontext, final String destcontext) {
        this.values.put(destcontext, this.values.get(srccontext));
    }
    
    public abstract void fromString(final String p0, final String p1);
    
    public T get() {
        return this.get(ModSettings.currentContext);
    }
    
    public abstract T get(final String p0);
    
    public void reset() {
        this.reset(ModSettings.currentContext);
    }
    
    public void reset(final String context) {
        this.set(this.defaultValue, context);
    }
    
    public void set(final T v) {
        this.set(v, ModSettings.currentContext);
    }
    
    public abstract void set(final T p0, final String p1);
    
    public abstract String toString(final String p0);
}
