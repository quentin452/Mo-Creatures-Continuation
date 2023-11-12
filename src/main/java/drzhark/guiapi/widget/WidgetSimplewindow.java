package drzhark.guiapi.widget;

import de.matthiasmann.twl.*;
import de.matthiasmann.twl.model.*;
import drzhark.guiapi.*;

public class WidgetSimplewindow extends Widget
{
    public Button backButton;
    public WidgetSingleRow buttonBar;
    public int hPadding;
    public Widget mainWidget;
    public Widget scrollPane;
    public Label titleWidget;
    public int vBottomPadding;
    public int vTopPadding;
    
    public WidgetSimplewindow() {
        this((Widget)new WidgetClassicTwocolumn(new Widget[0]), "", true);
    }
    
    public WidgetSimplewindow(final Widget w) {
        this(w, "", true);
    }
    
    public WidgetSimplewindow(final Widget w, final String s) {
        this(w, s, true);
    }
    
    public WidgetSimplewindow(final Widget w, final String s, final Boolean showbackButton) {
        this.backButton = new Button();
        this.buttonBar = new WidgetSingleRow(0, 0, new Widget[0]);
        this.hPadding = 30;
        this.mainWidget = new Widget();
        this.scrollPane = null;
        this.titleWidget = new Label();
        this.vBottomPadding = 40;
        this.vTopPadding = 30;
        final ScrollPane scrollpane = new ScrollPane(w);
        scrollpane.setFixed(ScrollPane.Fixed.HORIZONTAL);
        this.scrollPane = (Widget)scrollpane;
        this.mainWidget = w;
        this.setTheme("");
        this.init(showbackButton, s);
    }
    
    protected void init(final Boolean showBack, final String titleText) {
        if (titleText != null) {
            this.add((Widget)(this.titleWidget = new Label(titleText)));
        }
        else {
            this.vTopPadding = 10;
        }
        if (showBack) {
            this.backButton = new Button((ButtonModel)new SimpleButtonModel());
            this.backButton.getModel().addActionCallback((Runnable)GuiApiHelper.backModAction.mergeAction(new ModAction[] { GuiApiHelper.clickModAction }));
            this.backButton.setText("Back");
            this.add((Widget)(this.buttonBar = new WidgetSingleRow(200, 20, new Widget[] { (Widget)this.backButton })));
        }
        else {
            this.vBottomPadding = 0;
        }
        this.add(this.scrollPane);
    }
    
    public void layout() {
        if (this.buttonBar != null) {
            this.buttonBar.setSize(this.buttonBar.getPreferredWidth(), this.buttonBar.getPreferredHeight());
            this.buttonBar.setPosition(this.getWidth() / 2 - this.buttonBar.getPreferredWidth() / 2, this.getHeight() - (this.buttonBar.getPreferredHeight() + 4));
        }
        if (this.titleWidget != null) {
            this.titleWidget.setPosition(this.getWidth() / 2 - this.titleWidget.computeTextWidth() / 2, 10);
            this.titleWidget.setSize(this.titleWidget.computeTextWidth(), this.titleWidget.computeTextHeight());
        }
        this.scrollPane.setPosition(this.hPadding, this.vTopPadding);
        this.scrollPane.setSize(this.getWidth() - this.hPadding * 2, this.getHeight() - (this.vTopPadding + this.vBottomPadding));
    }
}
