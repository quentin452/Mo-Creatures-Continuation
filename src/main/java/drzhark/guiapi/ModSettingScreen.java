package drzhark.guiapi;

import java.util.*;
import de.matthiasmann.twl.*;
import drzhark.guiapi.widget.*;

public class ModSettingScreen
{
    public static String guiContext;
    public static ArrayList<ModSettingScreen> modScreens;
    public String buttonTitle;
    public String niceName;
    public Widget theWidget;
    public WidgetClassicTwocolumn widgetColumn;
    
    public ModSettingScreen(final String name) {
        this(name, name);
    }
    
    public ModSettingScreen(final String nicename, final String buttontitle) {
        ModSettingScreen.modScreens.add(this);
        this.buttonTitle = buttontitle;
        this.niceName = nicename;
        this.widgetColumn = new WidgetClassicTwocolumn(new Widget[0]);
        this.theWidget = new WidgetSimplewindow(this.widgetColumn, this.niceName);
    }
    
    public ModSettingScreen(final Widget widget, final String buttontitle) {
        ModSettingScreen.modScreens.add(this);
        this.buttonTitle = buttontitle;
        this.theWidget = widget;
    }
    
    public void append(final Widget newwidget) {
        if (this.widgetColumn != null) {
            this.widgetColumn.add(newwidget);
        }
        else {
            this.theWidget.add(newwidget);
        }
    }
    
    public void remove(final Widget child) {
        if (this.widgetColumn != null) {
            this.widgetColumn.removeChild(child);
        }
        else {
            this.theWidget.removeChild(child);
        }
    }
    
    public void setSingleColumn(final Boolean value) {
        final Boolean isSingle = WidgetSinglecolumn.class.isInstance(this.widgetColumn);
        if (isSingle == value) {
            return;
        }
        final WidgetClassicTwocolumn w2 = ((boolean)value) ? new WidgetSinglecolumn(new Widget[0]) : new WidgetClassicTwocolumn(new Widget[0]);
        for (int i = 0; i < this.widgetColumn.getNumChildren(); ++i) {
            w2.add(this.widgetColumn.getChild(i));
        }
        this.widgetColumn = w2;
        this.theWidget = new WidgetSimplewindow(this.widgetColumn, ((WidgetSimplewindow)this.theWidget).titleWidget.getText());
    }
    
    static {
        ModSettingScreen.guiContext = "";
        ModSettingScreen.modScreens = new ArrayList<ModSettingScreen>();
    }
}
