package drzhark.guiapi.widget;

import drzhark.guiapi.setting.*;
import de.matthiasmann.twl.model.*;
import de.matthiasmann.twl.*;
import drzhark.guiapi.*;

public class WidgetMulti extends WidgetSetting implements Runnable
{
    public Button button;
    public SettingMulti value;
    
    public WidgetMulti(final SettingMulti setting, final String title) {
        super(title);
        this.setTheme("");
        this.value = setting;
        this.value.displayWidget = this;
        final SimpleButtonModel model = new SimpleButtonModel();
        this.button = new Button((ButtonModel)model);
        model.addActionCallback((Runnable)this);
        this.add((Widget)this.button);
        this.update();
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
        this.value.next(ModSettingScreen.guiContext);
        this.update();
        GuiModScreen.clicksound();
    }
    
    @Override
    public void update() {
        this.button.setText(this.userString());
        ModSettings.dbgout("multi update " + this.userString());
    }
    
    @Override
    public String userString() {
        if (this.niceName.length() > 0) {
            return String.format("%s: %s", this.niceName, this.value.getLabel(ModSettingScreen.guiContext));
        }
        return this.value.getLabel(ModSettingScreen.guiContext);
    }
}
