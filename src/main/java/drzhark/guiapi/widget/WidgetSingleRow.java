package drzhark.guiapi.widget;

import de.matthiasmann.twl.*;
import java.util.*;

public class WidgetSingleRow extends Widget
{
    public int defaultHeight;
    public int defaultWidth;
    protected ArrayList<Integer> heights;
    protected ArrayList<Widget> widgets;
    protected ArrayList<Integer> widths;
    public int xSpacing;
    
    public WidgetSingleRow(final int defwidth, final int defheight, final Widget... widgets) {
        this.defaultHeight = 20;
        this.defaultWidth = 150;
        this.heights = new ArrayList<Integer>();
        this.widgets = new ArrayList<Widget>();
        this.widths = new ArrayList<Integer>();
        this.xSpacing = 3;
        this.setTheme("");
        this.defaultWidth = defwidth;
        this.defaultHeight = defheight;
        for (int i = 0; i < widgets.length; ++i) {
            this.add(widgets[i]);
        }
    }
    
    public void add(final Widget widget) {
        this.add(widget, this.defaultWidth, this.defaultHeight);
    }
    
    public void add(final Widget widget, final int width, final int height) {
        this.widgets.add(widget);
        this.heights.add(height);
        this.widths.add(width);
        super.add(widget);
    }
    
    private int getHeight(final int idx) {
        if (this.heights.get(idx) >= 0) {
            return this.heights.get(idx);
        }
        return this.widgets.get(idx).getPreferredHeight();
    }
    
    public int getPreferredHeight() {
        int maxheights = 0;
        for (int i = 0; i < this.heights.size(); ++i) {
            if (this.getHeight(i) > maxheights) {
                maxheights = this.getHeight(i);
            }
        }
        return maxheights;
    }
    
    public int getPreferredWidth() {
        int totalwidth = (this.widths.size() - 1) * this.xSpacing;
        totalwidth = ((totalwidth >= 0) ? totalwidth : 0);
        for (int i = 0; i < this.widths.size(); ++i) {
            totalwidth += this.getWidth(i);
        }
        return totalwidth;
    }
    
    private int getWidth(final int idx) {
        if (this.widths.get(idx) >= 0) {
            return this.widths.get(idx);
        }
        return this.widgets.get(idx).getPreferredWidth();
    }
    
    public void layout() {
        int curXpos = 0;
        for (int i = 0; i < this.widgets.size(); ++i) {
            final Widget w = this.widgets.get(i);
            w.setPosition(curXpos + this.getX(), this.getY());
            w.setSize(this.getWidth(i), this.getHeight(i));
            curXpos += this.getWidth(i) + this.xSpacing;
        }
    }
    
    public Widget removeChild(final int idx) {
        this.widgets.remove(idx);
        this.heights.remove(idx);
        this.widths.remove(idx);
        return super.removeChild(idx);
    }
    
    public boolean removeChild(final Widget widget) {
        final int idx = this.widgets.indexOf(widget);
        this.widgets.remove(idx);
        this.heights.remove(idx);
        this.widths.remove(idx);
        return super.removeChild(widget);
    }
}
