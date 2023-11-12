package drzhark.guiapi.setting;

public class SettingText extends Setting<String>
{
    public SettingText(final String title, final String defaulttext) {
        this.values.put("", defaulttext);
        this.defaultValue = defaulttext;
        this.backendName = title;
    }
    
    public void fromString(final String s, final String context) {
        this.values.put(context, s);
        if (this.displayWidget != null) {
            this.displayWidget.update();
        }
    }
    
    public String get(final String context) {
        if (this.values.get(context) != null) {
            return this.values.get(context);
        }
        if (this.values.get("") != null) {
            return this.values.get("");
        }
        return (String)this.defaultValue;
    }
    
    public void set(final String v, final String context) {
        this.values.put(context, v);
        if (this.parent != null) {
            this.parent.save(context);
        }
        if (this.displayWidget != null) {
            this.displayWidget.update();
        }
    }
    
    public String toString(final String context) {
        return this.get(context);
    }
}
