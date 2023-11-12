package de.matthiasmann.twl;

import java.text.*;
import de.matthiasmann.twl.model.*;
import java.util.*;

public class DatePickerComboBox extends ComboBoxBase
{
    private final ComboboxLabel label;
    private final DatePicker datePicker;
    
    public DatePickerComboBox() {
        this(Locale.getDefault(), DateFormat.getDateInstance());
    }
    
    public DatePickerComboBox(final Locale locale, final int style) {
        this(locale, DateFormat.getDateInstance(style, locale));
    }
    
    public DatePickerComboBox(final Locale locale, final DateFormat dateFormat) {
        final L l = new L();
        (this.label = new ComboboxLabel(this.getAnimationState())).setTheme("display");
        this.label.addCallback((CallbackWithReason<Label.CallbackReason>)l);
        (this.datePicker = new DatePicker(locale, dateFormat)).addCallback((DatePicker.Callback)l);
        this.popup.add((Widget)this.datePicker);
        this.popup.setTheme("datepickercomboboxPopup");
        this.button.getModel().addStateCallback(l);
        this.add((Widget)this.label);
    }
    
    public void setModel(final DateModel model) {
        this.datePicker.setModel(model);
    }
    
    public DateModel getModel() {
        return this.datePicker.getModel();
    }
    
    public void setDateFormat(final Locale locale, final DateFormat dateFormat) {
        this.datePicker.setDateFormat(locale, dateFormat);
    }
    
    public DateFormat getDateFormat() {
        return this.datePicker.getDateFormat();
    }
    
    public Locale getLocale() {
        return this.datePicker.getLocale();
    }
    
    protected ComboboxLabel getLabel() {
        return this.label;
    }
    
    protected DatePicker getDatePicker() {
        return this.datePicker;
    }
    
    protected void setPopupSize() {
        final int minWidth = this.popup.getMinWidth();
        final int minHeight = this.popup.getMinHeight();
        int popupWidth = computeSize(minWidth, this.popup.getPreferredWidth(), this.popup.getMaxWidth());
        int popupHeight = computeSize(minHeight, this.popup.getPreferredHeight(), this.popup.getMaxHeight());
        final Widget container = this.popup.getParent();
        final int popupMaxRight = container.getInnerRight();
        final int popupMaxBottom = container.getInnerBottom();
        int x = this.getX();
        int y = this.getBottom();
        if (x + popupWidth > popupMaxRight) {
            if (this.getRight() - popupWidth >= container.getInnerX()) {
                x = this.getRight() - popupWidth;
            }
            else {
                x = popupMaxRight - minWidth;
            }
        }
        if (y + popupHeight > popupMaxBottom) {
            if (this.getY() - popupHeight >= container.getInnerY()) {
                y = this.getY() - popupHeight;
            }
            else {
                y = popupMaxBottom - minHeight;
            }
        }
        popupWidth = Math.min(popupWidth, popupMaxRight - x);
        popupHeight = Math.min(popupHeight, popupMaxBottom - y);
        this.popup.setPosition(x, y);
        this.popup.setSize(popupWidth, popupHeight);
    }
    
    protected void updateLabel() {
        this.label.setText(this.datePicker.formatDate());
    }
    
    void updateHover() {
        this.getAnimationState().setAnimationState(Label.STATE_HOVER, this.label.hover || this.button.getModel().isHover());
    }
    
    protected class ComboboxLabel extends Label
    {
        boolean hover;
        
        public ComboboxLabel(final AnimationState animState) {
            super(animState);
            this.setAutoSize(false);
            this.setClip(true);
            this.setTheme("display");
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
                    DatePickerComboBox.this.updateHover();
                }
            }
        }
    }
    
    class L implements Runnable, CallbackWithReason<Label.CallbackReason>, DatePicker.Callback
    {
        @Override
        public void run() {
            DatePickerComboBox.this.updateHover();
        }
        
        public void callback(final Label.CallbackReason reason) {
            DatePickerComboBox.this.openPopup();
        }
        
        public void calendarChanged(final Calendar calendar) {
            DatePickerComboBox.this.updateLabel();
        }
    }
}
