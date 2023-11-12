package drzhark.guiapi.widget;

import drzhark.guiapi.setting.*;
import de.matthiasmann.twl.model.*;
import de.matthiasmann.twl.*;
import org.lwjgl.input.*;
import drzhark.guiapi.*;

public class WidgetKeybinding extends WidgetSetting implements Runnable
{
    public SimpleBooleanModel booleanModel;
    public int CLEARKEY;
    public int NEVERMINDKEY;
    public SettingKey settingReference;
    public ToggleButton toggleButton;

    public WidgetKeybinding(final SettingKey setting, final String title) {
        super(title);
        this.CLEARKEY = 211;
        this.NEVERMINDKEY = 1;
        this.setTheme("");
        this.settingReference = setting;
        this.settingReference.displayWidget = this;
        this.booleanModel = new SimpleBooleanModel(false);
        this.add((Widget)(this.toggleButton = new ToggleButton((BooleanModel)this.booleanModel)));
        this.update();
    }

    @Override
    public void addCallback(final Runnable paramRunnable) {
        this.booleanModel.addCallback(paramRunnable);
    }

    public boolean handleEvent(final Event evt) {
        if (evt.isKeyEvent() && !evt.isKeyPressedEvent() && this.booleanModel.getValue()) {
            System.out.println(Keyboard.getKeyName(evt.getKeyCode()));
            final int tmpvalue = evt.getKeyCode();
            if (tmpvalue == this.CLEARKEY) {
                this.settingReference.set(0, ModSettingScreen.guiContext);
            }
            else if (tmpvalue != this.NEVERMINDKEY) {
                this.settingReference.set(tmpvalue, ModSettingScreen.guiContext);
            }
            this.booleanModel.setValue(false);
            this.update();
            GuiModScreen.clicksound();
            return true;
        }
        return false;
    }

    public void keyboardFocusLost() {
        GuiModScreen.clicksound();
        this.booleanModel.setValue(false);
    }

    @Override
    public void removeCallback(final Runnable paramRunnable) {
        this.booleanModel.removeCallback(paramRunnable);
    }

    @Override
    public void run() {
        GuiModScreen.clicksound();
    }

    @Override
    public void update() {
        this.toggleButton.setText(this.userString());
    }

    @Override
    public String userString() {
        return String.format("%s: %s", this.niceName, Keyboard.getKeyName((int)this.settingReference.get(ModSettingScreen.guiContext)));
    }
}
