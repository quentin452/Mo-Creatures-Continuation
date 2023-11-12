package drzhark.guiapi.widget;

import de.matthiasmann.twl.*;
import java.util.*;

public abstract class WidgetSetting extends Widget
{
    public static ArrayList<WidgetSetting> all;
    public String niceName;
    
    public static void updateAll() {
        for (int i = 0; i < WidgetSetting.all.size(); ++i) {
            WidgetSetting.all.get(i).update();
        }
    }
    
    public WidgetSetting(final String nicename) {
        this.niceName = nicename;
        WidgetSetting.all.add(this);
    }
    
    public void add(final Widget child) {
        final String T = child.getTheme();
        if (T.length() == 0) {
            child.setTheme("/-defaults");
        }
        else if (!T.substring(0, 1).equals("/")) {
            child.setTheme("/" + T);
        }
        super.add(child);
    }
    
    public abstract void addCallback(final Runnable p0);
    
    public void layout() {
        for (int i = 0; i < this.getNumChildren(); ++i) {
            final Widget w = this.getChild(i);
            w.setPosition(this.getX(), this.getY());
            w.setSize(this.getWidth(), this.getHeight());
        }
    }
    
    public abstract void removeCallback(final Runnable p0);
    
    public abstract void update();
    
    public abstract String userString();
    
    static {
        WidgetSetting.all = new ArrayList<WidgetSetting>();
    }
}
