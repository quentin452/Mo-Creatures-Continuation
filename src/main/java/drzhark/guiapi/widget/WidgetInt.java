package drzhark.guiapi.widget;

import drzhark.guiapi.setting.*;
import de.matthiasmann.twl.model.*;
import de.matthiasmann.twl.*;
import drzhark.guiapi.*;

public class WidgetInt extends WidgetSetting implements Runnable
{
    public SettingInt settingReference;
    public WidgetSlider slider;

    public WidgetInt(final SettingInt setting, final String title) {
        super(title);
        this.setTheme("");
        this.settingReference = setting;
        this.settingReference.displayWidget = this;
        final SimpleFloatModel smodel = new SimpleFloatModel((float)this.settingReference.minimumValue, (float)this.settingReference.maximumValue, (float)(int)this.settingReference.get());
        (this.slider = new WidgetSlider((FloatModel)smodel)).setFormat(String.format("%s: %%.0f", this.niceName));
        if (this.settingReference.stepValue > 1 && this.settingReference.stepValue <= this.settingReference.maximumValue) {
            this.slider.setStepSize((float)this.settingReference.stepValue);
        }
        smodel.addCallback((Runnable)this);
        this.add((Widget)this.slider);
        this.update();
    }

    @Override
    public void addCallback(final Runnable paramRunnable) {
        this.slider.getModel().addCallback(paramRunnable);
    }

    @Override
    public void removeCallback(final Runnable paramRunnable) {
        this.slider.getModel().removeCallback(paramRunnable);
    }

    @Override
    public void run() {
        ModSettings.dbgout("run " + (int)this.slider.getValue());
        this.settingReference.set((int) this.slider.getValue(), ModSettingScreen.guiContext);
    }

    @Override
    public void update() {
        this.slider.setValue((float)this.settingReference.get(ModSettingScreen.guiContext));
        this.slider.setMinMaxValue((float)this.settingReference.minimumValue, (float)this.settingReference.maximumValue);
        this.slider.setFormat(String.format("%s: %%.0f", this.niceName));
        ModSettings.dbgout("update " + this.settingReference.get(ModSettingScreen.guiContext) + " -> " + (int)this.slider.getValue());
    }

    @Override
    public String userString() {
        return String.format("%s: %.0d", this.niceName, this.settingReference.get(ModSettingScreen.guiContext));
    }
}
