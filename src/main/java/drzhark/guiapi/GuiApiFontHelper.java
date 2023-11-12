package drzhark.guiapi;

import de.matthiasmann.twl.renderer.AnimationState;
import de.matthiasmann.twl.renderer.lwjgl.*;
import drzhark.guiapi.widget.*;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.*;
import java.util.*;

public class GuiApiFontHelper
{
    private static Map<Widget, GuiApiFontHelper> customFontWidgets;
    private static Map<FontStates, AnimationStateString> stateTable;
    private LWJGLFont myFont;

    public static void resyncCustomFonts() {
        for (final Map.Entry<Widget, GuiApiFontHelper> entry : GuiApiFontHelper.customFontWidgets.entrySet()) {
            final GuiApiFontHelper font = entry.getValue();
            final Widget widget = entry.getKey();
            if (widget instanceof TextWidget) {
                font.setFont((TextWidget)widget);
            }
            if (widget instanceof EditField) {
                font.setFont((EditField)widget);
            }
            if (widget instanceof WidgetText) {
                font.setFont((WidgetText)widget);
            }
        }
    }

    public GuiApiFontHelper() {
        final GuiWidgetScreen widgetScreen = GuiWidgetScreen.getInstance();
        final LWJGLFont baseFont = (LWJGLFont)widgetScreen.theme.getDefaultFont();
        this.myFont = baseFont.clone();
    }

    public Color getColor(final FontStates state) {
        return this.myFont.evalFontState((AnimationState)GuiApiFontHelper.stateTable.get(state)).getColor();
    }

    public boolean getLineThrough(final FontStates state) {
        return this.myFont.evalFontState((AnimationState)GuiApiFontHelper.stateTable.get(state)).getLineThrough();
    }

    public int getOffsetX(final FontStates state) {
        return this.myFont.evalFontState((AnimationState)GuiApiFontHelper.stateTable.get(state)).getOffsetX();
    }

    public int getOffsetY(final FontStates state) {
        return this.myFont.evalFontState((AnimationState)GuiApiFontHelper.stateTable.get(state)).getOffsetY();
    }

    public boolean getUnderline(final FontStates state) {
        return this.myFont.evalFontState((AnimationState)GuiApiFontHelper.stateTable.get(state)).getUnderline();
    }

    public int getUnderlineOffset(final FontStates state) {
        return this.myFont.evalFontState((AnimationState)GuiApiFontHelper.stateTable.get(state)).getUnderlineOffset();
    }

    public void setColor(final FontStates state, final Color col) {
        this.myFont.evalFontState((AnimationState)GuiApiFontHelper.stateTable.get(state)).setColor(col);
        resyncCustomFonts();
    }

    public void setFont(final EditField widget) {
        try {
            this.setFont((TextWidget)widget.textRenderer);
            GuiApiFontHelper.customFontWidgets.put((Widget)widget, this);
        }
        catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void setFont(final TextWidget widget) {
        widget.setFont((Font)this.myFont);
        GuiApiFontHelper.customFontWidgets.put((Widget)widget, this);
    }

    public void setFont(final WidgetText widget) {
        if (widget.displayLabel != null) {
            widget.displayLabel.setFont((Font)this.myFont);
            GuiApiFontHelper.customFontWidgets.put(widget, this);
        }
        this.setFont(widget.editField);
        GuiApiFontHelper.customFontWidgets.put(widget, this);
    }

    public void setLineThrough(final FontStates state, final boolean val) {
        this.myFont.evalFontState((AnimationState)GuiApiFontHelper.stateTable.get(state)).setLineThrough(val);
        resyncCustomFonts();
    }

    public void setOffsetX(final FontStates state, final int i) {
        this.myFont.evalFontState((AnimationState)GuiApiFontHelper.stateTable.get(state)).setOffsetX(i);
        resyncCustomFonts();
    }

    public void setOffsetY(final FontStates state, final int i) {
        this.myFont.evalFontState((AnimationState)GuiApiFontHelper.stateTable.get(state)).setOffsetY(i);
        resyncCustomFonts();
    }

    public void setUnderline(final FontStates state, final boolean val) {
        this.myFont.evalFontState((AnimationState)GuiApiFontHelper.stateTable.get(state)).setUnderline(val);
        resyncCustomFonts();
    }

    public void setUnderlineOffset(final FontStates state, final int i) {
        this.myFont.evalFontState((AnimationState)GuiApiFontHelper.stateTable.get(state)).setUnderlineOffset(i);
        resyncCustomFonts();
    }

    static {
        GuiApiFontHelper.customFontWidgets = new HashMap<Widget, GuiApiFontHelper>();
        try {
            GuiApiFontHelper.stateTable = new HashMap<FontStates, AnimationStateString>();
            final FontStates[] states = FontStates.values();
            for (int i = 0; i < states.length; ++i) {
                GuiApiFontHelper.stateTable.put(states[i], new AnimationStateString(states[i].name()));
            }
        }
        catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public enum FontStates
    {
        disabled,
        error,
        hover,
        normal,
        textSelection,
        warning;
    }
}
