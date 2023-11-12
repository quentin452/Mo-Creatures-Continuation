package drzhark.guiapi.setting;

import java.util.*;
import drzhark.guiapi.*;
import java.io.*;

public class SettingDictionary extends Setting<Properties>
{
    public SettingDictionary(final String title) {
        this(title, new Properties());
    }
    
    public SettingDictionary(final String title, final Properties defaultvalue) {
        this.backendName = title;
        this.defaultValue = defaultvalue;
        this.values.put("", defaultvalue);
    }
    
    public void fromString(final String s, final String context) {
        final Properties prop = new Properties();
        try {
            prop.loadFromXML(new ByteArrayInputStream(s.getBytes("UTF-8")));
        }
        catch (Throwable e) {
            ModSettings.dbgout("Error reading SettingDictionary from context '" + context + "': " + e);
        }
        this.values.put(context, prop);
        if (this.displayWidget != null) {
            this.displayWidget.update();
        }
    }
    
    public Properties get(final String context) {
        if (this.values.get(context) != null) {
            return this.values.get(context);
        }
        if (this.values.get("") != null) {
            return this.values.get("");
        }
        return (Properties)this.defaultValue;
    }
    
    public void set(final Properties v, final String context) {
        this.values.put(context, v);
        if (this.parent != null) {
            this.parent.save(context);
        }
        if (this.displayWidget != null) {
            this.displayWidget.update();
        }
    }
    
    public String toString(final String context) {
        try {
            final Properties prop = this.get(context);
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            prop.storeToXML(output, "GuiAPI SettingDictionary: DO NOT EDIT.");
            return output.toString("UTF-8");
        }
        catch (IOException e) {
            ModSettings.dbgout("Error writing SettingDictionary from context '" + context + "': " + e);
            return "";
        }
    }
}
