package drzhark.guiapi;

import net.minecraft.client.*;
import de.matthiasmann.twl.*;
import drzhark.guiapi.widget.*;
import java.security.*;
import drzhark.guiapi.setting.*;
import java.io.*;
import java.util.*;

public class ModSettings
{
    public static ArrayList<ModSettings> all;
    public static HashMap<String, String> contextDatadirs;
    public static String currentContext;
    public static final boolean debug = false;
    public String backendname;
    public ArrayList<Setting> Settings;
    public boolean settingsLoaded;

    public static void dbgout(final String s) {
    }

    public static File getAppDir(final String app) {
        try {
            return new File(Minecraft.getMinecraft().mcDataDir, app).getCanonicalFile();
        }
        catch (IOException e) {
            return new File(Minecraft.getMinecraft().mcDataDir, app);
        }
    }

    public static Minecraft getMcinst() {
        return Minecraft.getMinecraft();
    }

    public static void loadAll(final String context) {
        for (int i = 0; i < ModSettings.all.size(); ++i) {
            ModSettings.all.get(i).load(context);
        }
    }

    public static void setContext(final String name, final String location) {
        if (name != null) {
            ModSettings.contextDatadirs.put(name, location);
            ModSettings.currentContext = name;
            if (!name.equals("")) {
                loadAll(ModSettings.currentContext);
            }
        }
        else {
            ModSettings.currentContext = "";
        }
    }

    public ModSettings(final String modbackendname) {
        this.settingsLoaded = false;
        this.backendname = modbackendname;
        this.Settings = new ArrayList<Setting>();
        ModSettings.all.add(this);
    }

    public SettingBoolean addSetting(final ModSettingScreen screen, final String nicename, final String backendname, final boolean value) {
        final SettingBoolean s = new SettingBoolean(backendname, value);
        final WidgetBoolean w = new WidgetBoolean(s, nicename);
        screen.append(w);
        this.append(s);
        return s;
    }

    public SettingBoolean addSetting(final ModSettingScreen screen, final String nicename, final String backendname, final boolean value, final String truestring, final String falsestring) {
        final SettingBoolean s = new SettingBoolean(backendname, value);
        final WidgetBoolean w = new WidgetBoolean(s, nicename, truestring, falsestring);
        screen.append(w);
        this.append(s);
        return s;
    }

    public SettingFloat addSetting(final ModSettingScreen screen, final String nicename, final String backendname, final float value) {
        final SettingFloat s = new SettingFloat(backendname, value);
        final WidgetFloat w = new WidgetFloat(s, nicename);
        screen.append(w);
        this.append(s);
        return s;
    }

    public SettingFloat addSetting(final ModSettingScreen screen, final String nicename, final String backendname, final float value, final float min, final float step, final float max) {
        final SettingFloat s = new SettingFloat(backendname, value, min, step, max);
        final WidgetFloat w = new WidgetFloat(s, nicename);
        screen.append(w);
        this.append(s);
        return s;
    }

    public SettingKey addSetting(final ModSettingScreen screen, final String nicename, final String backendname, final int value) {
        final SettingKey s = new SettingKey(backendname, value);
        final WidgetKeybinding w = new WidgetKeybinding(s, nicename);
        screen.append(w);
        this.append(s);
        return s;
    }

    public SettingInt addSetting(final ModSettingScreen screen, final String nicename, final String backendname, final int value, final int min, final int max) {
        final SettingInt s = new SettingInt(backendname, value, min, 1, max);
        final WidgetInt w = new WidgetInt(s, nicename);
        screen.append(w);
        this.append(s);
        return s;
    }

    public SettingInt addSetting(final ModSettingScreen screen, final String nicename, final String backendname, final int value, final int min, final int step, final int max) {
        final SettingInt s = new SettingInt(backendname, value, min, step, max);
        final WidgetInt w = new WidgetInt(s, nicename);
        screen.append(w);
        this.append(s);
        return s;
    }

    public SettingMulti addSetting(final ModSettingScreen screen, final String nicename, final String backendname, final int value, final String... labels) {
        final SettingMulti s = new SettingMulti(backendname, value, labels);
        final WidgetMulti w = new WidgetMulti(s, nicename);
        screen.append(w);
        this.append(s);
        return s;
    }

    public SettingText addSetting(final ModSettingScreen screen, final String nicename, final String backendname, final String value) {
        final SettingText s = new SettingText(backendname, value);
        final WidgetText w = new WidgetText(s, nicename);
        screen.append(w);
        this.append(s);
        return s;
    }

    public SettingBoolean addSetting(final Widget w2, final String nicename, final String backendname, final boolean value) {
        final SettingBoolean s = new SettingBoolean(backendname, value);
        final WidgetBoolean w3 = new WidgetBoolean(s, nicename);
        w2.add((Widget)w3);
        this.append(s);
        return s;
    }

    public SettingBoolean addSetting(final Widget w2, final String nicename, final String backendname, final boolean value, final String truestring, final String falsestring) {
        final SettingBoolean s = new SettingBoolean(backendname, value);
        final WidgetBoolean w3 = new WidgetBoolean(s, nicename, truestring, falsestring);
        w2.add((Widget)w3);
        this.append(s);
        return s;
    }

    public SettingFloat addSetting(final Widget w2, final String nicename, final String backendname, final float value) {
        final SettingFloat s = new SettingFloat(backendname, value);
        final WidgetFloat w3 = new WidgetFloat(s, nicename);
        w2.add((Widget)w3);
        this.append(s);
        return s;
    }

    public SettingFloat addSetting(final Widget w2, final String nicename, final String backendname, final float value, final float min, final float step, final float max) {
        final SettingFloat s = new SettingFloat(backendname, value, min, step, max);
        final WidgetFloat w3 = new WidgetFloat(s, nicename);
        w2.add((Widget)w3);
        this.append(s);
        return s;
    }

    public SettingKey addSetting(final Widget w2, final String nicename, final String backendname, final int value) {
        final SettingKey s = new SettingKey(backendname, value);
        final WidgetKeybinding w3 = new WidgetKeybinding(s, nicename);
        w2.add((Widget)w3);
        this.append(s);
        return s;
    }

    public SettingInt addSetting(final Widget w2, final String nicename, final String backendname, final int value, final int min, final int max) {
        final SettingInt s = new SettingInt(backendname, value, min, 1, max);
        final WidgetInt w3 = new WidgetInt(s, nicename);
        w2.add((Widget)w3);
        this.append(s);
        return s;
    }

    public SettingInt addSetting(final Widget w2, final String nicename, final String backendname, final int value, final int min, final int step, final int max) {
        final SettingInt s = new SettingInt(backendname, value, min, step, max);
        final WidgetInt w3 = new WidgetInt(s, nicename);
        w2.add((Widget)w3);
        this.append(s);
        return s;
    }

    public SettingMulti addSetting(final Widget w2, final String nicename, final String backendname, final int value, final String... labels) {
        final SettingMulti s = new SettingMulti(backendname, value, labels);
        final WidgetMulti w3 = new WidgetMulti(s, nicename);
        w2.add((Widget)w3);
        this.append(s);
        return s;
    }

    public SettingList addSetting(final Widget w2, final String nicename, final String backendname, final String... options) {
        final ArrayList<String> arrayList = new ArrayList<String>();
        for (int i = 0; i < options.length; ++i) {
            arrayList.add(options[i]);
        }
        final SettingList s = new SettingList(backendname, arrayList);
        final WidgetList w3 = new WidgetList(s, nicename);
        w2.add((Widget)w3);
        this.append(s);
        return s;
    }

    public SettingText addSetting(final Widget w2, final String nicename, final String backendname, final String value) {
        final SettingText s = new SettingText(backendname, value);
        final WidgetText w3 = new WidgetText(s, nicename);
        w2.add((Widget)w3);
        this.append(s);
        return s;
    }

    public void append(final Setting s) {
        this.Settings.add(s);
        s.parent = this;
    }

    public void copyContextAll(final String src, final String dest) {
        for (int i = 0; i < this.Settings.size(); ++i) {
            this.Settings.get(i).copyContext(src, dest);
        }
    }

    public ArrayList<SettingBoolean> getAllBooleanSettings() {
        return this.getAllBooleanSettings(ModSettings.currentContext);
    }

    public ArrayList<SettingBoolean> getAllBooleanSettings(final String context) {
        final ArrayList<SettingBoolean> settings = new ArrayList<SettingBoolean>();
        for (final Setting setting : this.Settings) {
            if (!SettingBoolean.class.isAssignableFrom(setting.getClass())) {
                continue;
            }
            settings.add((SettingBoolean)setting);
        }
        return settings;
    }

    public ArrayList<SettingFloat> getAllFloatSettings() {
        return this.getAllFloatSettings(ModSettings.currentContext);
    }

    public ArrayList<SettingFloat> getAllFloatSettings(final String context) {
        final ArrayList<SettingFloat> settings = new ArrayList<SettingFloat>();
        for (final Setting setting : this.Settings) {
            if (!SettingFloat.class.isAssignableFrom(setting.getClass())) {
                continue;
            }
            settings.add((SettingFloat)setting);
        }
        return settings;
    }

    public ArrayList<SettingInt> getAllIntSettings() {
        return this.getAllIntSettings(ModSettings.currentContext);
    }

    public ArrayList<SettingInt> getAllIntSettings(final String context) {
        final ArrayList<SettingInt> settings = new ArrayList<SettingInt>();
        for (final Setting setting : this.Settings) {
            if (!SettingInt.class.isAssignableFrom(setting.getClass())) {
                continue;
            }
            settings.add((SettingInt)setting);
        }
        return settings;
    }

    public ArrayList<SettingKey> getAllKeySettings() {
        return this.getAllKeySettings(ModSettings.currentContext);
    }

    public ArrayList<SettingKey> getAllKeySettings(final String context) {
        final ArrayList<SettingKey> settings = new ArrayList<SettingKey>();
        for (final Setting setting : this.Settings) {
            if (!SettingKey.class.isAssignableFrom(setting.getClass())) {
                continue;
            }
            settings.add((SettingKey)setting);
        }
        return settings;
    }

    public ArrayList<SettingMulti> getAllMultiSettings() {
        return this.getAllMultiSettings(ModSettings.currentContext);
    }

    public ArrayList<SettingMulti> getAllMultiSettings(final String context) {
        final ArrayList<SettingMulti> settings = new ArrayList<SettingMulti>();
        for (final Setting setting : this.Settings) {
            if (!SettingMulti.class.isAssignableFrom(setting.getClass())) {
                continue;
            }
            settings.add((SettingMulti)setting);
        }
        return settings;
    }

    public ArrayList<SettingText> getAllTextSettings() {
        return this.getAllTextSettings(ModSettings.currentContext);
    }

    public ArrayList<SettingText> getAllTextSettings(final String context) {
        final ArrayList<SettingText> settings = new ArrayList<SettingText>();
        for (final Setting setting : this.Settings) {
            if (!SettingText.class.isAssignableFrom(setting.getClass())) {
                continue;
            }
            settings.add((SettingText)setting);
        }
        return settings;
    }

    public Boolean getBooleanSettingValue(final String backendName) {
        return this.getBooleanSettingValue(backendName, ModSettings.currentContext);
    }

    public Boolean getBooleanSettingValue(final String backendName, final String context) {
        return this.getSettingBoolean(backendName).get(context);
    }

    public Float getFloatSettingValue(final String backendName) {
        return this.getFloatSettingValue(backendName, ModSettings.currentContext);
    }

    public Float getFloatSettingValue(final String backendName, final String context) {
        return this.getSettingFloat(backendName).get(context);
    }

    public Integer getIntSettingValue(final String backendName) {
        return this.getIntSettingValue(backendName, ModSettings.currentContext);
    }

    public Integer getIntSettingValue(final String backendName, final String context) {
        return this.getSettingInt(backendName).get(context);
    }

    public Integer getKeySettingValue(final String backendName) {
        return this.getKeySettingValue(backendName, ModSettings.currentContext);
    }

    public Integer getKeySettingValue(final String backendName, final String context) {
        return this.getSettingKey(backendName).get(context);
    }

    public String getMultiSettingLabel(final String backendName) {
        return this.getMultiSettingLabel(backendName, ModSettings.currentContext);
    }

    public String getMultiSettingLabel(final String backendName, final String context) {
        final SettingMulti setting = this.getSettingMulti(backendName);
        return setting.labelValues[setting.get(context)];
    }

    public Integer getMultiSettingValue(final String backendName) {
        return this.getMultiSettingValue(backendName, ModSettings.currentContext);
    }

    public Integer getMultiSettingValue(final String backendName, final String context) {
        return this.getSettingMulti(backendName).get(context);
    }

    public SettingBoolean getSettingBoolean(final String backendName) {
        for (final Setting setting : this.Settings) {
            if (!SettingBoolean.class.isAssignableFrom(setting.getClass())) {
                continue;
            }
            if (setting.backendName.equals(backendName)) {
                return (SettingBoolean)setting;
            }
        }
        throw new InvalidParameterException("SettingBoolean '" + backendName + "' not found.");
    }

    public SettingFloat getSettingFloat(final String backendName) {
        for (final Setting setting : this.Settings) {
            if (!SettingFloat.class.isAssignableFrom(setting.getClass())) {
                continue;
            }
            if (setting.backendName.equals(backendName)) {
                return (SettingFloat)setting;
            }
        }
        throw new InvalidParameterException("SettingFloat '" + backendName + "' not found.");
    }

    public SettingInt getSettingInt(final String backendName) {
        for (final Setting setting : this.Settings) {
            if (!SettingInt.class.isAssignableFrom(setting.getClass())) {
                continue;
            }
            if (setting.backendName.equals(backendName)) {
                return (SettingInt)setting;
            }
        }
        throw new InvalidParameterException("SettingInt '" + backendName + "' not found.");
    }

    public SettingKey getSettingKey(final String backendName) {
        for (final Setting setting : this.Settings) {
            if (!SettingKey.class.isAssignableFrom(setting.getClass())) {
                continue;
            }
            if (setting.backendName.equals(backendName)) {
                return (SettingKey)setting;
            }
        }
        throw new InvalidParameterException("SettingKey '" + backendName + "' not found.");
    }

    public SettingDictionary getSettingList(final String backendName) {
        for (final Setting setting : this.Settings) {
            if (!SettingDictionary.class.isAssignableFrom(setting.getClass())) {
                continue;
            }
            if (setting.backendName.equals(backendName)) {
                return (SettingDictionary)setting;
            }
        }
        throw new InvalidParameterException("SettingList '" + backendName + "' not found.");
    }

    public SettingMulti getSettingMulti(final String backendName) {
        for (final Setting setting : this.Settings) {
            if (!SettingMulti.class.isAssignableFrom(setting.getClass())) {
                continue;
            }
            if (setting.backendName.equals(backendName)) {
                return (SettingMulti)setting;
            }
        }
        throw new InvalidParameterException("SettingMulti '" + backendName + "' not found.");
    }

    public SettingText getSettingText(final String backendName) {
        for (final Setting setting : this.Settings) {
            if (!SettingText.class.isAssignableFrom(setting.getClass())) {
                continue;
            }
            if (setting.backendName.equals(backendName)) {
                return (SettingText)setting;
            }
        }
        throw new InvalidParameterException("SettingText '" + backendName + "' not found.");
    }

    public String getTextSettingValue(final String backendName) {
        return this.getTextSettingValue(backendName, ModSettings.currentContext);
    }

    public String getTextSettingValue(final String backendName, final String context) {
        return this.getSettingText(backendName).get(context);
    }

    public void load() {
        this.load("");
        this.settingsLoaded = true;
    }

    public void load(final String context) {
        try {
            if (ModSettings.contextDatadirs.get(context) != null) {
                final File path = getAppDir("/" + ModSettings.contextDatadirs.get(context) + "/" + this.backendname + "/");
                if (path.exists()) {
                    final File file = new File(path, "guiconfig.properties");
                    if (file.exists()) {
                        final Properties p = new Properties();
                        p.load(new FileInputStream(file));
                        for (Setting setting : this.Settings) {
                            dbgout("setting load");
                            if (p.containsKey(setting.backendName)) {
                                dbgout("setting " + p.get(setting.backendName));
                                setting.fromString((String) p.get(setting.backendName), context);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void remove(final Setting s) {
        this.Settings.remove(s);
        s.parent = null;
    }

    public void resetAll() {
        this.resetAll(ModSettings.currentContext);
    }

    public void resetAll(final String context) {
        for (int i = 0; i < this.Settings.size(); ++i) {
            this.Settings.get(i).reset(context);
        }
    }

    public void save(final String context) {
        if (!this.settingsLoaded) {
            return;
        }
        try {
            final File path = getAppDir("/" + ModSettings.contextDatadirs.get(context) + "/" + this.backendname + "/");
            dbgout("saving context " + context + " (" + path.getAbsolutePath() + " [" + ModSettings.contextDatadirs.get(context) + "])");
            if (!path.exists()) {
                path.mkdirs();
            }
            final File file = new File(path, "guiconfig.properties");
            final Properties p = new Properties();
            for (int i = 0; i < this.Settings.size(); ++i) {
                final Setting z = this.Settings.get(i);
                (p).put(z.backendName, z.toString(context));
            }
            final FileOutputStream out = new FileOutputStream(file);
            p.store(out, "");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int size() {
        return this.Settings.size();
    }

    static {
        ModSettings.all = new ArrayList<ModSettings>();
        ModSettings.contextDatadirs = new HashMap<String, String>();
        ModSettings.currentContext = "";
        ModSettings.contextDatadirs.put("", "mods");
    }
}
