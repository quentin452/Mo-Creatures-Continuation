package drzhark.guiapi.setting;

public class SettingBoolean extends Setting<Boolean>
{
    public SettingBoolean(final String name) {
        this(name, false);
    }
    
    public SettingBoolean(final String name, final Boolean defValue) {
        this.defaultValue = defValue;
        this.values.put("", this.defaultValue);
        this.backendName = name;
    }
    
    public void fromString(final String s, final String context) {
        this.values.put(context, s.equals("true"));
        if (this.displayWidget != null) {
            this.displayWidget.update();
        }
    }
    
    public Boolean get(final String context) {
        if (this.values.get(context) != null) {
            return this.values.get(context);
        }
        if (this.values.get("") != null) {
            return this.values.get("");
        }
        return (Boolean)this.defaultValue;
    }
    
    public void set(final Boolean v, final String context) {
        this.values.put(context, v);
        if (this.parent != null) {
            this.parent.save(context);
        }
        if (this.displayWidget != null) {
            this.displayWidget.update();
        }
    }
    
    public String toString(final String context) {
        return this.get(context) ? "true" : "false";
    }
}
