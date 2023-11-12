package drzhark.guiapi.setting;

public class SettingFloat extends Setting<Float>
{
    public float maximumValue;
    public float minimumValue;
    public float stepValue;
    
    public SettingFloat(final String title) {
        this(title, 0.0f, 0.0f, 0.1f, 1.0f);
    }
    
    public SettingFloat(final String title, final float defValue) {
        this(title, defValue, 0.0f, 0.1f, 1.0f);
    }
    
    public SettingFloat(final String title, final float defValue, final float minValue, final float maxValue) {
        this(title, defValue, minValue, 0.1f, maxValue);
    }
    
    public SettingFloat(final String title, final float defValue, final float minValue, final float stepValue, final float maxValue) {
        this.values.put("", defValue);
        this.defaultValue = defValue;
        this.minimumValue = minValue;
        this.stepValue = stepValue;
        this.maximumValue = maxValue;
        this.backendName = title;
        if (this.minimumValue > this.maximumValue) {
            final float t = this.minimumValue;
            this.minimumValue = this.maximumValue;
            this.maximumValue = t;
        }
    }
    
    public void fromString(final String s, final String context) {
        this.values.put(context, new Float(s));
        if (this.displayWidget != null) {
            this.displayWidget.update();
        }
    }
    
    public Float get(final String context) {
        if (this.values.get(context) != null) {
            return this.values.get(context);
        }
        if (this.values.get("") != null) {
            return this.values.get("");
        }
        return (Float)this.defaultValue;
    }
    
    public void set(final Float v, final String context) {
        if (this.stepValue > 0.0f) {
            this.values.put(context, Math.round(v / this.stepValue) * this.stepValue);
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
