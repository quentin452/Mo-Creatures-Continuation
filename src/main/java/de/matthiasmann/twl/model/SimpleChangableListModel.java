package de.matthiasmann.twl.model;

import java.util.*;

public class SimpleChangableListModel<T> extends SimpleListModel<T>
{
    private final ArrayList<T> content;
    
    public SimpleChangableListModel() {
        this.content = new ArrayList<T>();
    }
    
    public SimpleChangableListModel(final Collection<T> content) {
        this.content = new ArrayList<T>((Collection<? extends T>)content);
    }
    
    public SimpleChangableListModel(final T... content) {
        this.content = new ArrayList<T>((Collection<? extends T>)Arrays.asList(content));
    }
    
    public T getEntry(final int index) {
        return this.content.get(index);
    }
    
    public int getNumEntries() {
        return this.content.size();
    }
    
    public void addElement(final T element) {
        this.insertElement(this.getNumEntries(), element);
    }
    
    public void addElements(final Collection<T> elements) {
        this.insertElements(this.getNumEntries(), elements);
    }
    
    public void addElements(final T... elements) {
        this.insertElements(this.getNumEntries(), elements);
    }
    
    public void insertElement(final int idx, final T element) {
        this.content.add(idx, element);
        this.fireEntriesInserted(idx, idx);
    }
    
    public void insertElements(final int idx, final Collection<T> elements) {
        this.content.addAll(idx, (Collection<? extends T>)elements);
        this.fireEntriesInserted(idx, idx + elements.size() - 1);
    }
    
    public void insertElements(final int idx, final T... elements) {
        this.insertElements(idx, Arrays.asList(elements));
    }
    
    public T removeElement(final int idx) {
        final T result = this.content.remove(idx);
        this.fireEntriesDeleted(idx, idx);
        return result;
    }
    
    public T setElement(final int idx, final T element) {
        final T result = this.content.set(idx, element);
        this.fireEntriesChanged(idx, idx);
        return result;
    }
    
    public int findElement(final Object element) {
        return this.content.indexOf(element);
    }
    
    public void clear() {
        this.content.clear();
        this.fireAllChanged();
    }
}
