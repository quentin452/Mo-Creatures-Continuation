package drzhark.guiapi.widget;

import drzhark.guiapi.setting.*;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.*;
import de.matthiasmann.twl.utils.*;
import java.util.*;

public class WidgetList extends WidgetSetting implements CallbackWithReason<ListBox.CallbackReason>
{
    private Runnable[] callbacks;
    public Label displayLabel;
    public ListBox<String> listBox;
    public SimpleChangableListModel<String> listBoxModel;
    public SettingList settingReference;
    
    public WidgetList(final SettingList setting, final String title) {
        super(title);
        this.setTheme("");
        this.settingReference = setting;
        this.settingReference.displayWidget = this;
        if (title != null) {
            (this.displayLabel = new Label()).setText(this.niceName);
            this.add((Widget)this.displayLabel);
        }
        this.listBoxModel = (SimpleChangableListModel<String>)new SimpleChangableListModel((Collection)setting.get());
        this.add((Widget)(this.listBox = (ListBox<String>)new ListBox((ListModel)this.listBoxModel)));
        this.listBox.addCallback((CallbackWithReason)this);
        this.update();
    }
    
    @Override
    public void addCallback(final Runnable callback) {
        this.callbacks = (Runnable[])CallbackSupport.addCallbackToList((Object[])this.callbacks, (Object)callback, (Class)Runnable.class);
    }
    
    public void callback(final ListBox.CallbackReason paramT) {
        CallbackSupport.fireCallbacks(this.callbacks);
    }
    
    @Override
    public void layout() {
        if (this.displayLabel != null) {
            this.displayLabel.setPosition(this.getX(), this.getY());
            final int offset = this.displayLabel.computeTextHeight();
            this.displayLabel.setSize(this.getWidth(), offset);
            this.listBox.setPosition(this.getX(), this.getY() + offset);
            this.listBox.setSize(this.getWidth(), this.getHeight() - offset);
        }
        else {
            this.listBox.setPosition(this.getX(), this.getY());
            this.listBox.setSize(this.getWidth(), this.getHeight());
        }
    }
    
    @Override
    public void removeCallback(final Runnable callback) {
        this.callbacks = (Runnable[])CallbackSupport.removeCallbackFromList((Object[])this.callbacks, (Object)callback);
    }
    
    @Override
    public void update() {
        this.listBoxModel.clear();
        this.listBoxModel.addElements((Collection)this.settingReference.get());
    }
    
    @Override
    public String userString() {
        String output = "";
        if (this.niceName != null) {
            output = this.niceName + ": ";
        }
        final int sel = this.listBox.getSelected();
        final String text = (String)((sel != -1) ? this.listBoxModel.getEntry(sel) : "NOTHING");
        output += String.format("%s (Entry %s) currently selected from %s items.", text, sel, ((ArrayList)this.settingReference.get()).size());
        return output;
    }
}
