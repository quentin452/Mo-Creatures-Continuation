package de.matthiasmann.twl;

public interface ListBoxDisplay
{
    boolean isSelected();
    
    void setSelected(final boolean p0);
    
    boolean isFocused();
    
    void setFocused(final boolean p0);
    
    void setData(final Object p0);
    
    void setTooltipContent(final Object p0);
    
    Widget getWidget();
    
    void addListBoxCallback(final CallbackWithReason<ListBox.CallbackReason> p0);
    
    void removeListBoxCallback(final CallbackWithReason<ListBox.CallbackReason> p0);
}
