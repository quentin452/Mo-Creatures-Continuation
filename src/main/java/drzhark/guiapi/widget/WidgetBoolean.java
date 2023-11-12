package drzhark.guiapi.widget;

import drzhark.guiapi.setting.*;
import de.matthiasmann.twl.model.*;
import de.matthiasmann.twl.*;
import drzhark.guiapi.*;

public class WidgetBoolean extends WidgetSetting implements Runnable
{
    public Button button;
    public String falseText;
    public SettingBoolean settingReference;
    public String trueText;

    public WidgetBoolean(final SettingBoolean setting, final String title) {
        this(setting, title, "true", "false");
    }

    public WidgetBoolean(final SettingBoolean setting, final String title, final String truetext, final String falsetext) {
        super(title);
        this.settingReference = null;
        this.setTheme("");
        this.trueText = truetext;
        this.falseText = falsetext;
        final SimpleButtonModel bmodel = new SimpleButtonModel();
        this.button = new Button((ButtonModel)bmodel);
        bmodel.addActionCallback((Runnable)this);
        this.add((Widget)this.button);
        this.settingReference = setting;
        ((WidgetBoolean)(this.settingReference.displayWidget = this)).update();
    }

    @Override
    public void addCallback(final Runnable paramRunnable) {
        this.button.getModel().addActionCallback(paramRunnable);
    }

    @Override
    public void removeCallback(final Runnable paramRunnable) {
        this.button.getModel().removeActionCallback(paramRunnable);
    }

    @Override
    public void run() {
        if (this.settingReference != null) {
            this.settingReference.set(!this.settingReference.get(ModSettingScreen.guiContext), ModSettingScreen.guiContext);
        }
        this.update();
        GuiModScreen.clicksound();
    }

    @Override
    public void update() {
        this.button.setText(this.userString());
    }

    @Override
    public String userString() {
        if (this.settingReference != null) {
            if (this.niceName.length() > 0) {
                return String.format("%s: %s", this.niceName, this.settingReference.get(ModSettingScreen.guiContext) ? this.trueText : this.falseText);
            }
            return this.settingReference.get(ModSettingScreen.guiContext) ? this.trueText : this.falseText;
        }
        else {
            if (this.niceName.length() > 0) {
                return String.format("%s: %s", this.niceName, "no value");
            }
            return "no value or title";
        }
    }
}
