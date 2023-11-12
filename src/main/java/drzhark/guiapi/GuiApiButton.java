package drzhark.guiapi;

import net.minecraft.client.gui.*;
import net.minecraft.client.*;
import drzhark.guiapi.widget.*;

public class GuiApiButton extends GuiButton
{
    public GuiApiButton(final int par1, final int par2, final int par3, final int par4, final int par5, final String par6Str) {
        super(par1, par2, par3, par4, par5, par6Str);
    }
    
    public boolean mousePressed(final Minecraft par1Minecraft, final int par2, final int par3) {
        if (super.mousePressed(par1Minecraft, par2, par3)) {
            par1Minecraft.gameSettings.saveOptions();
            ModSettingScreen.guiContext = "";
            WidgetSetting.updateAll();
            GuiModScreen.show(new GuiModSelect(par1Minecraft.currentScreen));
            return true;
        }
        return false;
    }
}
