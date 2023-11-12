package drzhark.guiapi.widget;

import java.lang.reflect.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.item.*;
import de.matthiasmann.twl.*;
import drzhark.guiapi.*;
import org.lwjgl.opengl.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.*;

public class WidgetItem2DRender extends Widget
{
    private static Field isDrawingField;
    private static RenderItem itemRenderer;
    private ItemStack renderStack;
    private int scaleType;
    
    public WidgetItem2DRender() {
        this(0);
    }
    
    public WidgetItem2DRender(final int renderID) {
        this(new ItemStack(Item.getItemById(renderID), 0, 0));
    }
    
    public WidgetItem2DRender(final ItemStack renderStack) {
        this.scaleType = 0;
        this.setMinSize(16, 16);
        this.setTheme("/progressbar");
    }
    
    public int getRenderID() {
        return (this.renderStack == null) ? 0 : Item.getIdFromItem(this.renderStack.getItem());
    }
    
    public ItemStack getRenderStack() {
        return this.renderStack;
    }
    
    public int getScaleType() {
        return this.scaleType;
    }
    
    private boolean isDrawing(final Tessellator tesselator) {
        if (WidgetItem2DRender.isDrawingField == null) {
            return false;
        }
        try {
            WidgetItem2DRender.isDrawingField.getBoolean(tesselator);
        }
        catch (Throwable t) {}
        return false;
    }
    
    protected void paintWidget(final GUI gui) {
        final Minecraft minecraft = ModSettings.getMcinst();
        int x = this.getX();
        int y = this.getY();
        float scalex = 1.0f;
        float scaley = 1.0f;
        final int maxWidth = this.getInnerWidth() - 4;
        final int maxHeight = this.getInnerHeight() - 4;
        int scale = this.getScaleType();
        if (scale == -1 && (maxWidth < 16 || maxHeight < 16)) {
            scale = 0;
        }
        switch (scale) {
            case 0: {
                int size = 0;
                if (maxWidth > maxHeight) {
                    size = maxHeight;
                }
                else {
                    size = maxWidth;
                }
                x += (maxWidth - size) / 2;
                y += (maxHeight - size) / 2;
                scalex = (scaley = size / 16.0f);
                x /= (int)scalex;
                y /= (int)scaley;
                break;
            }
            case -1: {
                int size = maxWidth - 16;
                x += size / 2;
                size = maxHeight - 16;
                y += size / 2;
                break;
            }
            case 1: {
                scalex = maxWidth / 16.0f;
                scaley = maxHeight / 16.0f;
                x /= (int)scalex;
                y /= (int)scaley;
                break;
            }
            default: {
                throw new IndexOutOfBoundsException("Scale Type is out of bounds! This should never happen!");
            }
        }
        x += 2;
        ++y;
        if (minecraft == null || this.getRenderStack() == null || this.getRenderStack().getItem() == null) {
            return;
        }
        final GuiWidgetScreen screen = GuiWidgetScreen.getInstance();
        screen.renderer.pauseRendering();
        screen.renderer.setClipRect();
        GL11.glEnable(3089);
        GL11.glPushMatrix();
        GL11.glDisable(3042);
        GL11.glEnable(32826);
        RenderHelper.enableStandardItemLighting();
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glScalef(scalex, scaley, 1.0f);
        final ItemStack stack = this.getRenderStack();
        if (this.isDrawing(Tessellator.instance)) {
            this.setDrawing(Tessellator.instance, false);
        }
        final int stackBeforeDraw = GL11.glGetInteger(2979);
        try {
            WidgetItem2DRender.itemRenderer.renderItemIntoGUI(minecraft.fontRenderer, minecraft.renderEngine, stack, x, y);
            if (this.isDrawing(Tessellator.instance)) {
                this.setDrawing(Tessellator.instance, false);
            }
            WidgetItem2DRender.itemRenderer.renderItemOverlayIntoGUI(minecraft.fontRenderer, minecraft.renderEngine, stack, x, y);
            if (this.isDrawing(Tessellator.instance)) {
                this.setDrawing(Tessellator.instance, false);
            }
        }
        catch (Throwable e) {
            if (this.isDrawing(Tessellator.instance)) {
                this.setDrawing(Tessellator.instance, false);
            }
        }
        final int stackAfterDraw = GL11.glGetInteger(2979);
        if (stackBeforeDraw != stackAfterDraw) {
            for (int i = 0; i < stackAfterDraw - stackBeforeDraw; ++i) {
                GL11.glPopMatrix();
            }
        }
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(32826);
        GL11.glPopMatrix();
        GL11.glDisable(3089);
        screen.renderer.resumeRendering();
    }
    
    private void setDrawing(final Tessellator tesselator, final boolean state) {
        if (WidgetItem2DRender.isDrawingField == null) {
            return;
        }
        try {
            WidgetItem2DRender.isDrawingField.setBoolean(tesselator, state);
        }
        catch (Throwable t) {}
    }
    
    public void setScaleType(int scaleType) {
        if (scaleType > 1) {
            scaleType = 1;
        }
        if (scaleType < -1) {
            scaleType = -1;
        }
        this.scaleType = scaleType;
    }
    
    static {
        WidgetItem2DRender.itemRenderer = new RenderItem();
        try {
            (WidgetItem2DRender.isDrawingField = Tessellator.class.getDeclaredField("z")).setAccessible(true);
        }
        catch (Throwable e) {
            try {
                (WidgetItem2DRender.isDrawingField = Tessellator.class.getDeclaredField("isDrawing")).setAccessible(true);
            }
            catch (Throwable e2) {
                System.out.println("GuiAPI Warning: Unable to get Tessellator.isDrawing field! There will be a chance of crashes if you attempt to render a mod item!");
            }
        }
    }
}
