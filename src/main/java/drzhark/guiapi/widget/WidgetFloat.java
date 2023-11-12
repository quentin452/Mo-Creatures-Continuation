package drzhark.guiapi.widget;

import drzhark.guiapi.setting.*;
import de.matthiasmann.twl.model.*;
import de.matthiasmann.twl.*;
import drzhark.guiapi.*;

public class WidgetFloat extends WidgetSetting implements Runnable
{
    public int decimalPlaces;
    public SettingFloat settingReference;
    public WidgetSlider slider;
    
    public WidgetFloat(final SettingFloat setting, final String title) {
        this(setting, title, 2);
    }
    
    public WidgetFloat(final SettingFloat setting, final String title, final int _decimalPlaces) {
        super(title);
        this.setTheme("");
        this.decimalPlaces = _decimalPlaces;
        this.settingReference = setting;
        this.settingReference.displayWidget = this;
        final SimpleFloatModel smodel = new SimpleFloatModel(this.settingReference.minimumValue, this.settingReference.maximumValue, (float)this.settingReference.get());
        smodel.addCallback((Runnable)this);
        this.slider = new WidgetSlider((FloatModel)smodel);
        if (this.settingReference.stepValue > 0.0f && this.settingReference.stepValue <= this.settingReference.maximumValue) {
            this.slider.setStepSize(this.settingReference.stepValue);
        }
        this.slider.setFormat(String.format("%s: %%.%df", this.niceName, this.decimalPlaces));
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
        this.settingReference.set(Float.valueOf(this.slider.getValue()), ModSettingScreen.guiContext);
    }
    
    @Override
    public void update() {
        this.slider.setValue((float)this.settingReference.get(ModSettingScreen.guiContext));
        this.slider.setMinMaxValue(this.settingReference.minimumValue, this.settingReference.maximumValue);
        this.slider.setFormat(String.format("%s: %%.%df", this.niceName, this.decimalPlaces));
    }
    
    @Override
    public String userString() {
        final String l = String.format("%02d", this.decimalPlaces);
        return String.format("%s: %." + l + "f", this.niceName, this.settingReference);
    }
}
