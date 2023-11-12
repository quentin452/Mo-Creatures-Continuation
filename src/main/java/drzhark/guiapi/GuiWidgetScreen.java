package drzhark.guiapi;

import de.matthiasmann.twl.*;
import net.minecraft.client.*;
import de.matthiasmann.twl.renderer.lwjgl.*;
import net.minecraft.client.gui.*;
import de.matthiasmann.twl.theme.*;
import de.matthiasmann.twl.input.lwjgl.*;
import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.input.*;
import java.net.*;
import java.io.*;

public class GuiWidgetScreen extends Widget
{
    public static GuiWidgetScreen instance;
    public static int screenheight;
    public static int screenwidth;
    public static URL themeURL;
    public Widget currentWidget;
    public GUI gui;
    public Minecraft minecraftInstance;
    public LWJGLRenderer renderer;
    public ScaledResolution screenSize;
    public ThemeManager theme;
    
    public static GuiWidgetScreen getInstance() {
        if (GuiWidgetScreen.instance != null) {
            return GuiWidgetScreen.instance;
        }
        try {
            GuiWidgetScreen.instance = new GuiWidgetScreen();
            GuiWidgetScreen.instance.renderer = new LWJGLRenderer();
            final String themename = "gui/twlGuiTheme.xml";
            GuiWidgetScreen.instance.gui = new GUI((Widget)GuiWidgetScreen.instance, (Renderer)GuiWidgetScreen.instance.renderer, (Input)new LWJGLInput());
            GuiWidgetScreen.themeURL = new URL("classloader", "", -1, themename, new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(final URL paramURL) throws IOException {
                    String file = paramURL.getFile();
                    if (file.startsWith("/")) {
                        file = file.substring(1);
                    }
                    return GuiWidgetScreen.class.getClassLoader().getResource(file).openConnection();
                }
            });
            GuiWidgetScreen.instance.theme = ThemeManager.createThemeManager(GuiWidgetScreen.themeURL, (Renderer)GuiWidgetScreen.instance.renderer);
            if (GuiWidgetScreen.instance.theme == null) {
                throw new RuntimeException("I don't think you installed the theme correctly ...");
            }
            GuiWidgetScreen.instance.setTheme("");
            GuiWidgetScreen.instance.gui.applyTheme(GuiWidgetScreen.instance.theme);
            GuiWidgetScreen.instance.minecraftInstance = ModSettings.getMcinst();
            GuiWidgetScreen.instance.screenSize = new ScaledResolution(GuiWidgetScreen.instance.minecraftInstance, GuiWidgetScreen.instance.minecraftInstance.displayWidth, GuiWidgetScreen.instance.minecraftInstance.displayHeight);
        }
        catch (Throwable e) {
            e.printStackTrace();
            final RuntimeException e2 = new RuntimeException("error loading theme");
            e2.initCause(e);
            throw e2;
        }
        return GuiWidgetScreen.instance;
    }
    
    public GuiWidgetScreen() {
        this.currentWidget = null;
        this.gui = null;
        this.renderer = null;
        this.screenSize = null;
        this.theme = null;
    }
    
    public void layout() {
        this.screenSize = new ScaledResolution(this.minecraftInstance, this.minecraftInstance.displayWidth, this.minecraftInstance.displayHeight);
        if (this.currentWidget != null) {
            GuiWidgetScreen.screenwidth = this.screenSize.getScaledWidth();
            GuiWidgetScreen.screenheight = this.screenSize.getScaledHeight();
            this.currentWidget.setSize(GuiWidgetScreen.screenwidth, GuiWidgetScreen.screenheight);
            this.currentWidget.setPosition(0, 0);
        }
    }
    
    public void resetScreen() {
        this.removeAllChildren();
        this.currentWidget = null;
    }
    
    public void setScreen(final Widget widget) {
        this.gui.resyncTimerAfterPause();
        this.gui.clearKeyboardState();
        this.gui.clearMouseState();
        this.removeAllChildren();
        this.add(widget);
        GuiApiFontHelper.resyncCustomFonts();
        this.currentWidget = widget;
    }
}
