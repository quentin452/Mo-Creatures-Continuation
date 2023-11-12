package drzhark.guiapi;

import de.matthiasmann.twl.*;
import net.minecraft.client.*;
import net.minecraft.util.*;
import net.minecraft.client.audio.*;
import net.minecraft.client.gui.*;
import de.matthiasmann.twl.renderer.lwjgl.*;

public class GuiModScreen extends GuiScreen
{
    public static GuiModScreen currentScreen;
    public int backgroundType;
    public Widget mainwidget;
    public GuiScreen parentScreen;
    
    public static void back() {
        if (GuiModScreen.currentScreen != null) {
            final Minecraft m = ModSettings.getMcinst();
            m.displayGuiScreen(GuiModScreen.currentScreen.parentScreen);
            if (GuiModScreen.currentScreen.parentScreen instanceof GuiModScreen) {
                (GuiModScreen.currentScreen = (GuiModScreen)GuiModScreen.currentScreen.parentScreen).setActive();
            }
            else {
                GuiModScreen.currentScreen = null;
            }
        }
    }
    
    public static void clicksound() {
        final Minecraft m = ModSettings.getMcinst();
        m.getSoundHandler().playSound((ISound)PositionedSoundRecord.func_147674_a(new ResourceLocation("gui.button.press"), 1.0f));
    }
    
    public static void show(final GuiModScreen screen) {
        final Minecraft m = ModSettings.getMcinst();
        m.displayGuiScreen((GuiScreen)screen);
        screen.setActive();
    }
    
    public static void show(final Widget screen) {
        show(new GuiModScreen(GuiModScreen.currentScreen, screen));
    }
    
    protected GuiModScreen(final GuiScreen screen) {
        this.backgroundType = 0;
        this.parentScreen = screen;
        GuiModScreen.currentScreen = this;
        this.allowUserInput = false;
    }
    
    public GuiModScreen(final GuiScreen screen, final Widget widget) {
        this.backgroundType = 0;
        this.mainwidget = widget;
        this.parentScreen = screen;
        GuiModScreen.currentScreen = this;
        this.allowUserInput = false;
    }
    
    public void drawScreen(final int var1, final int var2, final float var3) {
        switch (this.backgroundType) {
            case 0: {
                this.drawDefaultBackground();
                break;
            }
            case 1: {
                this.drawDefaultBackground();
                break;
            }
        }
        final LWJGLRenderer var4 = (LWJGLRenderer)GuiWidgetScreen.getInstance().gui.getRenderer();
        final ScaledResolution var5 = new ScaledResolution(GuiWidgetScreen.getInstance().minecraftInstance, GuiWidgetScreen.getInstance().minecraftInstance.displayWidth, GuiWidgetScreen.getInstance().minecraftInstance.displayHeight);
        RenderScale.scale = var5.getScaleFactor();
        var4.syncViewportSize();
        GuiWidgetScreen.getInstance().gui.update();
    }
    
    public void handleInput() {
    }
    
    private void setActive() {
        GuiWidgetScreen.getInstance().setScreen(this.mainwidget);
    }
}
