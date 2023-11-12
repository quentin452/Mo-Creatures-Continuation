package de.matthiasmann.twl;

import java.util.*;

public class BorderLayout extends Widget
{
    private final EnumMap<Location, Widget> widgets;
    private int hgap;
    private int vgap;
    
    public BorderLayout() {
        this.widgets = new EnumMap<Location, Widget>(Location.class);
    }
    
    public void add(final Widget widget, final Location location) {
        if (widget == null) {
            throw new NullPointerException("widget is null");
        }
        if (location == null) {
            throw new NullPointerException("location is null");
        }
        if (this.widgets.containsKey(location)) {
            throw new IllegalStateException("a widget was already added to that location: " + location);
        }
        this.widgets.put(location, widget);
        try {
            super.insertChild(widget, this.getNumChildren());
        }
        catch (Exception e) {
            this.removeChild(location);
        }
    }
    
    public Widget getChild(final Location location) {
        if (location == null) {
            throw new NullPointerException("location is null");
        }
        return this.widgets.get(location);
    }
    
    public Widget removeChild(final Location location) {
        if (location == null) {
            throw new NullPointerException("location is null");
        }
        final Widget w = this.widgets.remove(location);
        if (w != null) {
            this.removeChild(w);
        }
        return w;
    }
    
    @Override
    public void add(final Widget child) {
        this.add(child, Location.CENTER);
    }
    
    @Override
    public void insertChild(final Widget child, final int index) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("insert child is not supported by the BorderLayout");
    }
    
    @Override
    protected void childRemoved(final Widget exChild) {
        for (final Location loc : this.widgets.keySet()) {
            if (this.widgets.get(loc) == exChild) {
                this.widgets.remove(loc);
                break;
            }
        }
        super.childRemoved(exChild);
    }
    
    @Override
    protected void allChildrenRemoved() {
        this.widgets.clear();
        super.allChildrenRemoved();
    }
    
    @Override
    protected void applyTheme(final ThemeInfo themeInfo) {
        this.hgap = themeInfo.getParameter("hgap", 0);
        this.vgap = themeInfo.getParameter("vgap", 0);
        super.applyTheme(themeInfo);
    }
    
    @Override
    protected void layout() {
        int top = this.getInnerY();
        int bottom = this.getInnerBottom();
        int left = this.getInnerX();
        int right = this.getInnerRight();
        Widget w;
        if ((w = this.widgets.get(Location.NORTH)) != null) {
            w.setPosition(left, top);
            w.setSize(Math.max(right - left, 0), Math.max(w.getPreferredHeight(), 0));
            top += w.getPreferredHeight() + this.vgap;
        }
        if ((w = this.widgets.get(Location.SOUTH)) != null) {
            w.setPosition(left, bottom - w.getPreferredHeight());
            w.setSize(Math.max(right - left, 0), Math.max(w.getPreferredHeight(), 0));
            bottom -= w.getPreferredHeight() + this.vgap;
        }
        if ((w = this.widgets.get(Location.EAST)) != null) {
            w.setPosition(right - w.getPreferredWidth(), top);
            w.setSize(Math.max(w.getPreferredWidth(), 0), Math.max(bottom - top, 0));
            right -= w.getPreferredWidth() + this.hgap;
        }
        if ((w = this.widgets.get(Location.WEST)) != null) {
            w.setPosition(left, top);
            w.setSize(Math.max(w.getPreferredWidth(), 0), Math.max(bottom - top, 0));
            left += w.getPreferredWidth() + this.hgap;
        }
        if ((w = this.widgets.get(Location.CENTER)) != null) {
            w.setPosition(left, top);
            w.setSize(Math.max(right - left, 0), Math.max(bottom - top, 0));
        }
    }
    
    @Override
    public int getMinWidth() {
        return this.computeMinWidth();
    }
    
    @Override
    public int getMinHeight() {
        return this.computeMinHeight();
    }
    
    @Override
    public int getPreferredInnerWidth() {
        return this.computePrefWidth();
    }
    
    @Override
    public int getPreferredInnerHeight() {
        return this.computePrefHeight();
    }
    
    private int computeMinWidth() {
        int size = 0;
        size += this.getChildMinWidth(this.widgets.get(Location.EAST), this.hgap);
        size += this.getChildMinWidth(this.widgets.get(Location.WEST), this.hgap);
        size += this.getChildMinWidth(this.widgets.get(Location.CENTER), 0);
        size = Math.max(size, this.getChildMinWidth(this.widgets.get(Location.NORTH), 0));
        size = Math.max(size, this.getChildMinWidth(this.widgets.get(Location.SOUTH), 0));
        return size;
    }
    
    private int computeMinHeight() {
        int size = 0;
        size = Math.max(size, this.getChildMinHeight(this.widgets.get(Location.EAST), 0));
        size = Math.max(size, this.getChildMinHeight(this.widgets.get(Location.WEST), 0));
        size = Math.max(size, this.getChildMinHeight(this.widgets.get(Location.CENTER), 0));
        size += this.getChildMinHeight(this.widgets.get(Location.NORTH), this.vgap);
        size += this.getChildMinHeight(this.widgets.get(Location.SOUTH), this.vgap);
        return size;
    }
    
    private int computePrefWidth() {
        int size = 0;
        size += this.getChildPrefWidth(this.widgets.get(Location.EAST), this.hgap);
        size += this.getChildPrefWidth(this.widgets.get(Location.WEST), this.hgap);
        size += this.getChildPrefWidth(this.widgets.get(Location.CENTER), 0);
        size = Math.max(size, this.getChildPrefWidth(this.widgets.get(Location.NORTH), 0));
        size = Math.max(size, this.getChildPrefWidth(this.widgets.get(Location.SOUTH), 0));
        return size;
    }
    
    private int computePrefHeight() {
        int size = 0;
        size = Math.max(size, this.getChildPrefHeight(this.widgets.get(Location.EAST), 0));
        size = Math.max(size, this.getChildPrefHeight(this.widgets.get(Location.WEST), 0));
        size = Math.max(size, this.getChildPrefHeight(this.widgets.get(Location.CENTER), 0));
        size += this.getChildPrefHeight(this.widgets.get(Location.NORTH), this.vgap);
        size += this.getChildPrefHeight(this.widgets.get(Location.SOUTH), this.vgap);
        return size;
    }
    
    private int getChildMinWidth(final Widget w, final int gap) {
        if (w != null) {
            return w.getMinWidth() + gap;
        }
        return 0;
    }
    
    private int getChildMinHeight(final Widget w, final int gap) {
        if (w != null) {
            return w.getMinHeight() + gap;
        }
        return 0;
    }
    
    private int getChildPrefWidth(final Widget w, final int gap) {
        if (w != null) {
            return Widget.computeSize(w.getMinWidth(), w.getPreferredWidth(), w.getMaxWidth()) + gap;
        }
        return 0;
    }
    
    private int getChildPrefHeight(final Widget w, final int gap) {
        if (w != null) {
            return Widget.computeSize(w.getMinHeight(), w.getPreferredHeight(), w.getMaxHeight()) + gap;
        }
        return 0;
    }
    
    public enum Location
    {
        EAST, 
        WEST, 
        NORTH, 
        SOUTH, 
        CENTER;
    }
}
