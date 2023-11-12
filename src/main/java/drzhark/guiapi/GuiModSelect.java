package drzhark.guiapi;

import net.minecraft.client.gui.*;
import de.matthiasmann.twl.*;
import drzhark.guiapi.widget.*;

public class GuiModSelect extends GuiModScreen
{
    private static void selectScreen(final Integer i) {
        GuiModScreen.show(ModSettingScreen.modScreens.get(i).theWidget);
        GuiModScreen.clicksound();
    }

    protected GuiModSelect(final GuiScreen screen) {
        super(screen);
        final WidgetClassicTwocolumn w = new WidgetClassicTwocolumn();
        w.verticalPadding = 10;
        for (int i = 0; i < ModSettingScreen.modScreens.size(); ++i) {
            final ModSettingScreen m = ModSettingScreen.modScreens.get(i);
            w.add(GuiApiHelper.makeButton(m.buttonTitle, "selectScreen", GuiModSelect.class, Boolean.FALSE, new Class[] { Integer.class }, i));
        }
        final WidgetSimplewindow mainwidget = new WidgetSimplewindow(w, "Select a Mod");
        mainwidget.hPadding = 0;
        mainwidget.mainWidget.setTheme("scrollpane-notch");
        this.mainwidget = mainwidget;
    }
}
