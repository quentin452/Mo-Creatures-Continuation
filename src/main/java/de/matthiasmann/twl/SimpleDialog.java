package de.matthiasmann.twl;

import java.util.logging.*;

public class SimpleDialog
{
    private String theme;
    private String title;
    private Object msg;
    private Runnable cbOk;
    private Runnable cbCancel;
    private boolean focusCancelButton;
    
    public SimpleDialog() {
        this.theme = "simpledialog";
    }
    
    public void setTheme(final String theme) {
        if (theme == null) {
            throw new NullPointerException();
        }
        this.theme = theme;
    }
    
    public String getTitle() {
        return this.title;
    }
    
    public void setTitle(final String title) {
        this.title = title;
    }
    
    public Object getMessage() {
        return this.msg;
    }
    
    public void setMessage(final Object msg) {
        this.msg = msg;
    }
    
    public Runnable getOkCallback() {
        return this.cbOk;
    }
    
    public void setOkCallback(final Runnable cbOk) {
        this.cbOk = cbOk;
    }
    
    public Runnable getCancelCallback() {
        return this.cbCancel;
    }
    
    public void setCancelCallback(final Runnable cbCancel) {
        this.cbCancel = cbCancel;
    }
    
    public boolean isFocusCancelButton() {
        return this.focusCancelButton;
    }
    
    public void setFocusCancelButton(final boolean focusCancelButton) {
        this.focusCancelButton = focusCancelButton;
    }
    
    public PopupWindow showDialog(final Widget owner) {
        if (owner == null) {
            throw new NullPointerException("owner");
        }
        Widget msgWidget = null;
        if (this.msg instanceof Widget) {
            msgWidget = (Widget)this.msg;
            if (msgWidget.getParent() instanceof DialogLayout && msgWidget.getParent().getParent() instanceof PopupWindow) {
                final PopupWindow prevPopup = (PopupWindow)msgWidget.getParent().getParent();
                if (!prevPopup.isOpen()) {
                    msgWidget.getParent().removeChild(msgWidget);
                }
            }
            if (msgWidget.getParent() != null) {
                throw new IllegalArgumentException("message widget alreay in use");
            }
        }
        else if (this.msg instanceof String) {
            msgWidget = (Widget)new Label((String)this.msg);
        }
        else if (this.msg != null) {
            Logger.getLogger(SimpleDialog.class.getName()).log(Level.WARNING, "Unsupported message type: {0}", this.msg.getClass());
        }
        final PopupWindow popupWindow = new PopupWindow(owner);
        final Button btnOk = new Button("Ok");
        btnOk.setTheme("btnOk");
        btnOk.addCallback((Runnable)new ButtonCB(popupWindow, this.cbOk));
        final ButtonCB btnCancelCallback = new ButtonCB(popupWindow, this.cbCancel);
        popupWindow.setRequestCloseCallback((Runnable)btnCancelCallback);
        final Button btnCancel = new Button("Cancel");
        btnCancel.setTheme("btnCancel");
        btnCancel.addCallback((Runnable)btnCancelCallback);
        final DialogLayout layout = new DialogLayout();
        layout.setTheme("content");
        layout.setHorizontalGroup(layout.createParallelGroup());
        layout.setVerticalGroup(layout.createSequentialGroup());
        String vertPrevWidget = "top";
        if (this.title != null) {
            final Label labelTitle = new Label(this.title);
            labelTitle.setTheme("title");
            labelTitle.setLabelFor(msgWidget);
            layout.getHorizontalGroup().addWidget((Widget)labelTitle);
            layout.getVerticalGroup().addWidget((Widget)labelTitle);
            vertPrevWidget = "title";
        }
        if (msgWidget != null) {
            layout.getHorizontalGroup().addGroup(layout.createSequentialGroup().addGap("left-msg").addWidget(msgWidget).addGap("msg-right"));
            layout.getVerticalGroup().addGap(vertPrevWidget.concat("-msg")).addWidget(msgWidget).addGap("msg-buttons");
        }
        else {
            layout.getVerticalGroup().addGap(vertPrevWidget.concat("-buttons"));
        }
        layout.getHorizontalGroup().addGroup(layout.createSequentialGroup().addGap("left-btnOk").addWidget((Widget)btnOk).addGap("btnOk-btnCancel").addWidget((Widget)btnCancel).addGap("btnCancel-right"));
        layout.getVerticalGroup().addGroup(layout.createParallelGroup(new Widget[] { (Widget)btnOk, (Widget)btnCancel }));
        popupWindow.setTheme(this.theme);
        popupWindow.add((Widget)layout);
        popupWindow.openPopupCentered();
        if (this.focusCancelButton) {
            btnCancel.requestKeyboardFocus();
        }
        else if (msgWidget != null && msgWidget.canAcceptKeyboardFocus()) {
            msgWidget.requestKeyboardFocus();
        }
        return popupWindow;
    }
    
    static class ButtonCB implements Runnable
    {
        private final PopupWindow popupWindow;
        private final Runnable cb;
        
        public ButtonCB(final PopupWindow popupWindow, final Runnable cb) {
            this.popupWindow = popupWindow;
            this.cb = cb;
        }
        
        @Override
        public void run() {
            this.popupWindow.closePopup();
            if (this.cb != null) {
                this.cb.run();
            }
        }
    }
}
