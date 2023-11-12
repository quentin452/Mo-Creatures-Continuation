package de.matthiasmann.twl;

import de.matthiasmann.twl.model.*;
import de.matthiasmann.twl.utils.*;
import de.matthiasmann.twl.renderer.*;

public class ComboBox<T> extends ComboBoxBase
{
    public static final AnimationState.StateKey STATE_ERROR;
    private static final int INVALID_WIDTH = -1;
    private final ComboboxLabel label;
    private final ListBox<T> listbox;
    private Runnable[] selectionChangedListeners;
    private ListModel.ChangeListener modelChangeListener;
    String displayTextNoSelection;
    boolean noSelectionIsError;
    boolean computeWidthFromModel;
    int modelWidth;
    
    public ComboBox(final ListSelectionModel<T> model) {
        this();
        this.setModel(model);
    }
    
    public ComboBox(final ListModel<T> model, final IntegerModel selectionModel) {
        this();
        this.setModel(model);
        this.setSelectionModel(selectionModel);
    }
    
    public ComboBox(final ListModel<T> model) {
        this();
        this.setModel(model);
    }
    
    public ComboBox() {
        this.displayTextNoSelection = "";
        this.modelWidth = -1;
        this.label = new ComboboxLabel(this.getAnimationState());
        this.listbox = new ComboboxListbox<T>();
        this.button.getModel().addStateCallback(new Runnable() {
            @Override
            public void run() {
                ComboBox.this.updateHover();
            }
        });
        this.label.addCallback((CallbackWithReason<Label.CallbackReason>)new CallbackWithReason<Label.CallbackReason>() {
            public void callback(final Label.CallbackReason reason) {
                ComboBox.this.openPopup();
            }
        });
        this.listbox.addCallback((CallbackWithReason<ListBox.CallbackReason>)new CallbackWithReason<ListBox.CallbackReason>() {
            public void callback(final ListBox.CallbackReason reason) {
                switch (reason) {
                    case KEYBOARD_RETURN:
                    case MOUSE_CLICK:
                    case MOUSE_DOUBLE_CLICK: {
                        ComboBox.this.listBoxSelectionChanged(true);
                        break;
                    }
                    default: {
                        ComboBox.this.listBoxSelectionChanged(false);
                        break;
                    }
                }
            }
        });
        this.popup.setTheme("comboboxPopup");
        this.popup.add(this.listbox);
        this.add(this.label);
    }
    
    public void addCallback(final Runnable cb) {
        this.selectionChangedListeners = CallbackSupport.addCallbackToList(this.selectionChangedListeners, cb, Runnable.class);
    }
    
    public void removeCallback(final Runnable cb) {
        this.selectionChangedListeners = CallbackSupport.removeCallbackFromList(this.selectionChangedListeners, cb);
    }
    
    private void doCallback() {
        CallbackSupport.fireCallbacks(this.selectionChangedListeners);
    }
    
    public void setModel(final ListModel<T> model) {
        this.unregisterModelChangeListener();
        this.listbox.setModel(model);
        if (this.computeWidthFromModel) {
            this.registerModelChangeListener();
        }
    }
    
    public ListModel<T> getModel() {
        return this.listbox.getModel();
    }
    
    public void setSelectionModel(final IntegerModel selectionModel) {
        this.listbox.setSelectionModel(selectionModel);
    }
    
    public IntegerModel getSelectionModel() {
        return this.listbox.getSelectionModel();
    }
    
    public void setModel(final ListSelectionModel<T> model) {
        this.listbox.setModel(model);
    }
    
    public void setSelected(final int selected) {
        this.listbox.setSelected(selected);
        this.updateLabel();
    }
    
    public int getSelected() {
        return this.listbox.getSelected();
    }
    
    public boolean isComputeWidthFromModel() {
        return this.computeWidthFromModel;
    }
    
    public void setComputeWidthFromModel(final boolean computeWidthFromModel) {
        if (this.computeWidthFromModel != computeWidthFromModel) {
            this.computeWidthFromModel = computeWidthFromModel;
            if (computeWidthFromModel) {
                this.registerModelChangeListener();
            }
            else {
                this.unregisterModelChangeListener();
            }
        }
    }
    
    public String getDisplayTextNoSelection() {
        return this.displayTextNoSelection;
    }
    
    public void setDisplayTextNoSelection(final String displayTextNoSelection) {
        if (displayTextNoSelection == null) {
            throw new NullPointerException("displayTextNoSelection");
        }
        this.displayTextNoSelection = displayTextNoSelection;
        this.updateLabel();
    }
    
    public boolean isNoSelectionIsError() {
        return this.noSelectionIsError;
    }
    
    public void setNoSelectionIsError(final boolean noSelectionIsError) {
        this.noSelectionIsError = noSelectionIsError;
        this.updateLabel();
    }
    
    private void registerModelChangeListener() {
        final ListModel<?> model = this.getModel();
        if (model != null) {
            this.modelWidth = -1;
            if (this.modelChangeListener == null) {
                this.modelChangeListener = new ModelChangeListener();
            }
            model.addChangeListener(this.modelChangeListener);
        }
    }
    
    private void unregisterModelChangeListener() {
        if (this.modelChangeListener != null) {
            final ListModel<T> model = this.getModel();
            if (model != null) {
                model.removeChangeListener(this.modelChangeListener);
            }
        }
    }
    
    @Override
    protected boolean openPopup() {
        if (super.openPopup()) {
            this.popup.validateLayout();
            this.listbox.scrollToSelected();
            return true;
        }
        return false;
    }
    
    protected void listBoxSelectionChanged(final boolean close) {
        this.updateLabel();
        if (close) {
            this.popup.closePopup();
        }
        this.doCallback();
    }
    
    protected String getModelData(final int idx) {
        return String.valueOf(this.getModel().getEntry(idx));
    }
    
    @Override
    protected Widget getLabel() {
        return this.label;
    }
    
    protected void updateLabel() {
        final int selected = this.getSelected();
        if (selected == -1) {
            this.label.setText(this.displayTextNoSelection);
            this.label.getAnimationState().setAnimationState(ComboBox.STATE_ERROR, this.noSelectionIsError);
        }
        else {
            this.label.setText(this.getModelData(selected));
            this.label.getAnimationState().setAnimationState(ComboBox.STATE_ERROR, false);
        }
        if (!this.computeWidthFromModel) {
            this.invalidateLayout();
        }
    }
    
    @Override
    protected void applyTheme(final ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        this.modelWidth = -1;
    }
    
    @Override
    protected boolean handleEvent(final Event evt) {
        if (super.handleEvent(evt)) {
            return true;
        }
        if (evt.isKeyPressedEvent()) {
            switch (evt.getKeyCode()) {
                case 199:
                case 200:
                case 207:
                case 208: {
                    this.listbox.handleEvent(evt);
                    return true;
                }
                case 28:
                case 57: {
                    this.openPopup();
                    return true;
                }
            }
        }
        return false;
    }
    
    void invalidateModelWidth() {
        if (this.computeWidthFromModel) {
            this.modelWidth = -1;
            this.invalidateLayout();
        }
    }
    
    void updateModelWidth() {
        if (this.computeWidthFromModel) {
            this.updateModelWidth(this.modelWidth = 0, this.getModel().getNumEntries() - 1);
        }
    }
    
    void updateModelWidth(final int first, final int last) {
        if (this.computeWidthFromModel) {
            int newModelWidth = this.modelWidth;
            for (int idx = first; idx <= last; ++idx) {
                newModelWidth = Math.max(newModelWidth, this.computeEntryWidth(idx));
            }
            if (newModelWidth > this.modelWidth) {
                this.modelWidth = newModelWidth;
                this.invalidateLayout();
            }
        }
    }
    
    protected int computeEntryWidth(final int idx) {
        int width = this.label.getBorderHorizontal();
        final Font font = this.label.getFont();
        if (font != null) {
            width += font.computeMultiLineTextWidth(this.getModelData(idx));
        }
        return width;
    }
    
    void updateHover() {
        this.getAnimationState().setAnimationState(Label.STATE_HOVER, this.label.hover || this.button.getModel().isHover());
    }
    
    static {
        STATE_ERROR = AnimationState.StateKey.get("error");
    }
    
    class ComboboxLabel extends Label
    {
        boolean hover;
        
        public ComboboxLabel(final de.matthiasmann.twl.AnimationState animState) {
            super(animState);
            this.setAutoSize(false);
            this.setClip(true);
            this.setTheme("display");
        }
        
        @Override
        public int getPreferredInnerWidth() {
            if (ComboBox.this.computeWidthFromModel && ComboBox.this.getModel() != null) {
                if (ComboBox.this.modelWidth == -1) {
                    ComboBox.this.updateModelWidth();
                }
                return ComboBox.this.modelWidth;
            }
            return super.getPreferredInnerWidth();
        }
        
        @Override
        public int getPreferredInnerHeight() {
            int prefHeight = super.getPreferredInnerHeight();
            if (this.getFont() != null) {
                prefHeight = Math.max(prefHeight, this.getFont().getLineHeight());
            }
            return prefHeight;
        }
        
        @Override
        protected void handleMouseHover(final Event evt) {
            if (evt.isMouseEvent()) {
                final boolean newHover = evt.getType() != Event.Type.MOUSE_EXITED;
                if (newHover != this.hover) {
                    this.hover = newHover;
                    ComboBox.this.updateHover();
                }
            }
        }
    }
    
    class ModelChangeListener implements ListModel.ChangeListener
    {
        @Override
        public void entriesInserted(final int first, final int last) {
            ComboBox.this.updateModelWidth(first, last);
        }
        
        @Override
        public void entriesDeleted(final int first, final int last) {
            ComboBox.this.invalidateModelWidth();
        }
        
        @Override
        public void entriesChanged(final int first, final int last) {
            ComboBox.this.invalidateModelWidth();
        }
        
        @Override
        public void allChanged() {
            ComboBox.this.invalidateModelWidth();
        }
    }
    
    static class ComboboxListbox<T> extends ListBox<T>
    {
        public ComboboxListbox() {
            this.setTheme("listbox");
        }
        
        @Override
        protected ListBoxDisplay createDisplay() {
            return new ComboboxListboxLabel();
        }
    }
    
    static class ComboboxListboxLabel extends ListBox.ListBoxLabel
    {
        @Override
        protected boolean handleListBoxEvent(final Event evt) {
            if (evt.getType() == Event.Type.MOUSE_CLICKED) {
                this.doListBoxCallback(ListBox.CallbackReason.MOUSE_CLICK);
                return true;
            }
            if (evt.getType() == Event.Type.MOUSE_BTNDOWN) {
                this.doListBoxCallback(ListBox.CallbackReason.SET_SELECTED);
                return true;
            }
            return false;
        }
    }
}
