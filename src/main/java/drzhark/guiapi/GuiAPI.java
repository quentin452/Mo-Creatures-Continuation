package drzhark.guiapi;

import cpw.mods.fml.relauncher.*;
import java.lang.reflect.*;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.settings.*;
import java.util.*;
import cpw.mods.fml.common.gameevent.*;
import net.minecraft.client.*;
import cpw.mods.fml.common.eventhandler.*;

@SideOnly(Side.CLIENT)
public class GuiAPI implements IFMLLoadingPlugin
{
    Object cacheCheck;
    Field controlListField;
    
    public GuiAPI() {
        this.cacheCheck = null;
    }
    
    @Mod.EventHandler
    public void init(final FMLInitializationEvent event) {
        try {
            final Field[] fields = GuiScreen.class.getDeclaredFields();
            for (int i = 0; i < fields.length; ++i) {
                if (fields[i].getType() == List.class) {
                    (this.controlListField = fields[i]).setAccessible(true);
                    break;
                }
            }
            if (this.controlListField == null) {
                throw new Exception("No fields found on GuiScreen (" + GuiScreen.class.getSimpleName() + ") of type List! This should never happen!");
            }
        }
        catch (Throwable e) {
            throw new RuntimeException("Unable to get Field reference for GuiScreen.controlList!", e);
        }
        FMLCommonHandler.instance().bus().register((Object)this);
    }
    
    public List getControlList(final GuiOptions gui) {
        try {
            return (List)this.controlListField.get(gui);
        }
        catch (Throwable e) {
            return null;
        }
    }
    
    public void processGuiOptions(final GuiOptions gui) {
        final List controlList = this.getControlList(gui);
        if (controlList == null) {
            return;
        }
        if (controlList.get(0) == this.cacheCheck) {
            return;
        }
        final ArrayList<GuiOptionButton> buttonsPreSorted = new ArrayList<GuiOptionButton>();
        for (final Object guiButton : controlList) {
            if (guiButton instanceof GuiOptionButton) {
                buttonsPreSorted.add((GuiOptionButton)guiButton);
            }
        }
        int xPos = -1;
        int yPos = -1;
        for (final GuiOptionButton guiButton2 : buttonsPreSorted) {
            if (guiButton2.returnEnumOptions() == GameSettings.Options.DIFFICULTY) {
                xPos = guiButton2.xPosition;
            }
            if (guiButton2.returnEnumOptions() == GameSettings.Options.TOUCHSCREEN) {
                yPos = guiButton2.yPosition;
            }
        }
        controlList.add(new GuiApiButton(300, xPos, yPos, 150, 20, "Global Mod Options"));
        this.cacheCheck = controlList.get(0);
    }
    
    public String[] getASMTransformerClass() {
        return null;
    }
    
    public String getModContainerClass() {
        return null;
    }
    
    public String getSetupClass() {
        return null;
    }
    
    public void injectData(final Map<String, Object> data) {
    }
    
    @SubscribeEvent
    public void clientTick(final TickEvent.ClientTickEvent event) {
        final TickEvent.Type type = event.type;
        final TickEvent.Type type2 = event.type;
        if (type != TickEvent.Type.RENDER) {
            return;
        }
        if (Minecraft.getMinecraft() == null) {
            return;
        }
        if (Minecraft.getMinecraft().currentScreen == null) {
            return;
        }
        if (Minecraft.getMinecraft().currentScreen instanceof GuiOptions) {
            this.processGuiOptions((GuiOptions)Minecraft.getMinecraft().currentScreen);
        }
    }
    
    public String getAccessTransformerClass() {
        return null;
    }
}
