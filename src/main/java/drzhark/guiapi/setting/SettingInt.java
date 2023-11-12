package drzhark.guiapi.setting;

import drzhark.guiapi.*;

public class SettingInt extends Setting<Integer>
{
    public int maximumValue;
    public int minimumValue;
    public int stepValue;
    
    public SettingInt(final String title) {
        this(title, 0, 0, 1, 100);
    }
    
    public SettingInt(final String title, final int defValue) {
        this(title, defValue, 0, 1, 100);
    }
    
    public SettingInt(final String title, final int defValue, final int minValue, final int maxValue) {
        this(title, defValue, minValue, 1, maxValue);
    }
    
    public SettingInt(final String title, final int defValue, final int minValue, final int stepValue, final int maxValue) {
        this.values.put("", defValue);
        this.defaultValue = defValue;
        this.minimumValue = minValue;
        this.stepValue = stepValue;
        this.maximumValue = maxValue;
        this.backendName = title;
        if (this.minimumValue > this.maximumValue) {
            final int t = this.minimumValue;
            this.minimumValue = this.maximumValue;
            this.maximumValue = t;
        }
    }
    
    public void fromString(final String s, final String context) {
        this.values.put(context, new Integer(s));
        if (this.displayWidget != null) {
            this.displayWidget.update();
        }
        ModSettings.dbgout("fromstring " + s);
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
    
    public void set(final Integer v, final String context) {
        ModSettings.dbgout("set " + v);
        if (this.stepValue > 1) {
            this.values.put(context, (int)(Math.round(v / (float)this.stepValue) * (float)this.stepValue));
        }
        else {
            this.values.put(context, v);
        }
        if (this.parent != null) {
            this.parent.save(context);
        }
        if (this.displayWidget != null) {
            this.displayWidget.update();
        }
    }
    
    public String toString(final String context) {
        return "" + this.get(context);
    }
}
