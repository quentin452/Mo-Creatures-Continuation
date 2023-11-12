package drzhark.guiapi.widget;

import de.matthiasmann.twl.model.*;
import drzhark.guiapi.setting.*;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.utils.*;
import drzhark.guiapi.*;

public class WidgetText extends WidgetSetting implements StringModel
{
    private Runnable[] callbacks;
    public Label displayLabel;
    public EditField editField;
    public int setmode;
    public SettingText settingReference;
    
    public WidgetText(final SettingText setting, final String title) {
        super(title);
        this.setmode = 0;
        this.setTheme("");
        this.settingReference = setting;
        ((WidgetText)(this.settingReference.displayWidget = this)).add((Widget)(this.editField = new EditField()));
        if (title != null) {
            (this.displayLabel = new Label()).setText(String.format("%s: ", this.niceName));
            this.add((Widget)this.displayLabel);
        }
        this.editField.setModel((StringModel)this);
        this.update();
    }
    
    public void addCallback(final Runnable callback) {
        this.callbacks = (Runnable[])CallbackSupport.addCallbackToList((Object[])this.callbacks, (Object)callback, (Class)Runnable.class);
    }
    
    public String getValue() {
        return (String)this.settingReference.get();
    }
    
    public void layout() {
        if (this.displayLabel != null) {
            this.displayLabel.setPosition(this.getX(), this.getY() + this.getHeight() / 2 - this.displayLabel.computeTextHeight() / 2);
            this.displayLabel.setSize(this.displayLabel.computeTextWidth(), this.displayLabel.computeTextHeight());
            this.editField.setPosition(this.getX() + this.displayLabel.computeTextWidth(), this.getY());
            this.editField.setSize(this.getWidth() - this.displayLabel.computeTextWidth(), this.getHeight());
        }
        else {
            this.editField.setPosition(this.getX(), this.getY());
            this.editField.setSize(this.getWidth(), this.getHeight());
        }
    }
    
    public void removeCallback(final Runnable callback) {
        this.callbacks = (Runnable[])CallbackSupport.removeCallbackFromList((Object[])this.callbacks, (Object)callback);
    }
    
    public void setValue(final String _value) {
        GuiModScreen.clicksound();
        ModSettings.dbgout(String.format("setvalue %s", this.editField.getText()));
        if (this.setmode <= 0) {
            this.setmode = -1;
            this.settingReference.set(this.editField.getText(), ModSettingScreen.guiContext);
            this.setmode = 0;
        }
        CallbackSupport.fireCallbacks(this.callbacks);
    }
    
    public void update() {
        ModSettings.dbgout("update");
        if (this.displayLabel != null) {
            this.displayLabel.setText(String.format("%s: ", this.niceName));
        }
        if (this.setmode >= 0) {
            this.setmode = 1;
            this.editField.setText(this.settingReference.get(ModSettingScreen.guiContext));
            this.setmode = 0;
        }
        ModSettings.dbgout(String.format("update %s", this.editField.getText()));
    }
    
    public String userString() {
        return String.format("%s: %s", this.niceName, this.settingReference.get(ModSettingScreen.guiContext));
    }
}
