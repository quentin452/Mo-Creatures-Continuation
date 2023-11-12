package drzhark.guiapi.setting;

import drzhark.guiapi.*;

public class SettingMulti extends Setting<Integer>
{
    public String[] labelValues;
    
    public SettingMulti(final String title, final int defValue, final String... labelValues) {
        if (labelValues.length == 0) {
            return;
        }
        this.values.put("", defValue);
        this.defaultValue = defValue;
        this.labelValues = labelValues;
        this.backendName = title;
    }
    
    public SettingMulti(final String title, final String... labelValues) {
        this(title, 0, labelValues);
    }
    
    public void fromString(final String s, final String context) {
        int x = -1;
        for (int i = 0; i < this.labelValues.length; ++i) {
            if (this.labelValues[i].equals(s)) {
                x = i;
            }
        }
        if (x != -1) {
            this.values.put(context, x);
        }
        else {
            this.values.put(context, new Float(s).intValue());
        }
        ModSettings.dbgout("fromstring multi " + s);
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
    
    public String getLabel() {
        return this.labelValues[(int)this.get()];
    }
    
    public String getLabel(final String context) {
        return this.labelValues[this.get(context)];
    }
    
    public void next() {
        this.next(ModSettings.currentContext);
    }
    
    public void next(final String context) {
        int tempvalue;
        for (tempvalue = this.get(context) + 1; tempvalue >= this.labelValues.length; tempvalue -= this.labelValues.length) {}
        this.set(tempvalue, context);
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
        int x = -1;
        for (int i = 0; i < this.labelValues.length; ++i) {
            if (this.labelValues[i].equals(v)) {
                x = i;
            }
        }
        if (x != -1) {
            this.set(x, context);
        }
    }
    
    public String toString(final String context) {
        return this.labelValues[this.get(context)];
    }
}
