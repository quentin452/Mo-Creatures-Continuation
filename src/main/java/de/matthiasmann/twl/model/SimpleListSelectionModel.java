package de.matthiasmann.twl.model;

public class SimpleListSelectionModel<T> extends HasCallback implements ListSelectionModel<T>
{
    private final ListModel<T> listModel;
    private int selected;
    
    public SimpleListSelectionModel(final ListModel<T> listModel) {
        if (listModel == null) {
            throw new NullPointerException("listModel");
        }
        this.listModel = listModel;
    }
    
    public ListModel<T> getListModel() {
        return this.listModel;
    }
    
    public T getSelectedEntry() {
        if (this.selected >= 0 && this.selected < this.listModel.getNumEntries()) {
            return (T)this.listModel.getEntry(this.selected);
        }
        return null;
    }
    
    public boolean setSelectedEntry(final T entry) {
        return this.setSelectedEntry(entry, -1);
    }
    
    public boolean setSelectedEntry(final T entry, final int defaultIndex) {
        if (entry != null) {
            for (int i = 0, n = this.listModel.getNumEntries(); i < n; ++i) {
                if (entry.equals(this.listModel.getEntry(i))) {
                    this.setValue(i);
                    return true;
                }
            }
        }
        this.setValue(defaultIndex);
        return false;
    }
    
    public int getMaxValue() {
        return this.listModel.getNumEntries() - 1;
    }
    
    public int getMinValue() {
        return -1;
    }
    
    public int getValue() {
        return this.selected;
    }
    
    public void setValue(final int value) {
        if (value < -1) {
            throw new IllegalArgumentException("value");
        }
        if (this.selected != value) {
            this.selected = value;
            this.doCallback();
        }
    }
}
