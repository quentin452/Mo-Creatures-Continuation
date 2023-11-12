package drzhark.guiapi;

import de.matthiasmann.twl.model.*;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.textarea.*;
import drzhark.guiapi.widget.*;
import java.util.*;

public class GuiApiHelper
{
    public static final ModAction backModAction;
    public static final ModAction clickModAction;
    private ArrayList<AbstractMap.SimpleEntry<String, ModAction>> buttonInfo_;
    private String displayText_;
    
    public static GuiApiHelper createChoiceMenu(final String displayText) {
        return new GuiApiHelper(displayText);
    }
    
    public static Widget createChoiceMenu(final String displayText, final Boolean showBackButton, final Boolean autoBack, final Object... args) {
        if (args.length % 2 == 1) {
            throw new IllegalArgumentException("Arguments not in correct format. You need to have an even number of arguments, in the form of String, ModAction for each button.");
        }
        final GuiApiHelper helper = new GuiApiHelper(displayText);
        try {
            for (int i = 0; i < args.length; i += 2) {
                helper.addButton((String)args[i], (ModAction)args[i + 1], autoBack);
            }
        }
        catch (Throwable e) {
            throw new IllegalArgumentException("Arguments not in correct format. You need to have an even number of arguments, in the form of String, ModAction for each button.", e);
        }
        return helper.genWidget(showBackButton);
    }
    
    public static Widget createChoiceMenu(final String displayText, final Boolean showBackButton, final Boolean autoBack, final String[] buttonTexts, final ModAction[] buttonActions) {
        if (buttonTexts.length != buttonActions.length) {
            throw new IllegalArgumentException("Arguments not in correct format. buttonTexts needs to be the same size as buttonActions.");
        }
        final GuiApiHelper helper = new GuiApiHelper(displayText);
        for (int i = 0; i < buttonTexts.length; i += 2) {
            helper.addButton(buttonTexts[i], buttonActions[i], autoBack);
        }
        return helper.genWidget(showBackButton);
    }
    
    public static Button makeButton(final String displayText, ModAction action, final Boolean addClick) {
        final SimpleButtonModel simplebuttonmodel = new SimpleButtonModel();
        if (addClick) {
            action = action.mergeAction(GuiApiHelper.clickModAction);
        }
        simplebuttonmodel.addActionCallback((Runnable)action);
        final Button button = new Button((ButtonModel)simplebuttonmodel);
        button.setText(displayText);
        return button;
    }
    
    public static Button makeButton(final String displayText, final String methodName, final Object me, final Boolean addClick) {
        return makeButton(displayText, new ModAction(me, methodName, new Class[0]), addClick);
    }
    
    public static Button makeButton(final String displayText, final String methodName, final Object me, final Boolean addClick, final Class[] classes, final Object... arguments) {
        return makeButton(displayText, new ModAction(me, methodName, classes).setDefaultArguments(arguments), addClick);
    }
    
    public static TextArea makeTextArea(final String text, final Boolean htmlMode) {
        if (!htmlMode) {
            final SimpleTextAreaModel model = new SimpleTextAreaModel();
            model.setText(text, false);
            return new TextArea((TextAreaModel)model);
        }
        final HTMLTextAreaModel model2 = new HTMLTextAreaModel();
        model2.setHtml(text);
        return new TextArea((TextAreaModel)model2);
    }
    
    public static Widget makeTextDisplayAndGoBack(final String titleText, final String displayText, final String buttonText, final Boolean htmlMode) {
        final WidgetSinglecolumn widget = new WidgetSinglecolumn(new Widget[0]);
        widget.add((Widget)makeTextArea(displayText, htmlMode));
        widget.overrideHeight = false;
        final WidgetSimplewindow window = new WidgetSimplewindow(widget, titleText);
        window.backButton.setText(buttonText);
        return window;
    }
    
    public static void setTextAreaText(final TextArea textArea, final String text) {
        final TextAreaModel model = textArea.getModel();
        if (model instanceof SimpleTextAreaModel) {
            ((SimpleTextAreaModel)model).setText(text, false);
        }
        else {
            ((HTMLTextAreaModel)model).setHtml(text);
        }
    }
    
    private GuiApiHelper(final String displayText) {
        this.displayText_ = displayText;
        this.buttonInfo_ = new ArrayList<AbstractMap.SimpleEntry<String, ModAction>>();
    }
    
    public void addButton(final String text, final ModAction action, final Boolean mergeBack) {
        ModAction buttonAction = action;
        if (mergeBack) {
            buttonAction = buttonAction.mergeAction(GuiApiHelper.backModAction);
            buttonAction.setTag("Button '" + text + "' with back.");
        }
        this.buttonInfo_.add(new AbstractMap.SimpleEntry<String, ModAction>(text, buttonAction));
    }
    
    public void addButton(final String text, final String methodName, final Object me, final Boolean mergeBack) {
        this.addButton(text, new ModAction(me, methodName, new Class[0]), mergeBack);
    }
    
    public void addButton(final String text, final String methodName, final Object me, final Class[] types, final Boolean mergeBack, final Object... arguments) {
        this.addButton(text, new ModAction(me, methodName, types).setDefaultArguments(arguments), mergeBack);
    }
    
    public WidgetSimplewindow genWidget(final Boolean showBackButton) {
        final WidgetSinglecolumn widget = new WidgetSinglecolumn(new Widget[0]);
        final TextArea textarea = makeTextArea(this.displayText_, false);
        widget.add((Widget)textarea);
        widget.heightOverrideExceptions.put((Widget)textarea, 0);
        for (final AbstractMap.SimpleEntry<String, ModAction> entry : this.buttonInfo_) {
            widget.add((Widget)makeButton(entry.getKey(), entry.getValue(), true));
        }
        final WidgetSimplewindow window = new WidgetSimplewindow(widget, null, showBackButton);
        return window;
    }
    
    static {
        (backModAction = new ModAction(GuiModScreen.class, "back", new Class[0])).setTag("Helper Back ModAction");
        (clickModAction = new ModAction(GuiModScreen.class, "clicksound", new Class[0])).setTag("Helper ClickSound ModAction");
    }
}
