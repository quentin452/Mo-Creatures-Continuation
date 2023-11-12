package de.matthiasmann.twl.model;

public abstract class SimpleListModel<T> extends AbstractListModel<T>
{
    public Object getEntryTooltip(final int index) {
        return null;
    }
    
    public boolean matchPrefix(final int index, final String prefix) {
        final Object entry = this.getEntry(index);
        return entry != null && entry.toString().regionMatches(true, 0, prefix, 0, prefix.length());
    }
}
