package drzhark.guiapi.widget;

import de.matthiasmann.twl.*;

public class WidgetSinglecolumn extends WidgetClassicTwocolumn
{
    public WidgetSinglecolumn(final Widget... widgets) {
        super(widgets);
        this.childDefaultWidth = 200;
    }
    
    public int getPreferredHeight() {
        int totalheight = this.verticalPadding;
        for (int i = 0; i < this.getNumChildren(); ++i) {
            final Widget widget = this.getChild(i);
            int height = this.childDefaultHeight;
            if (!this.overrideHeight) {
                height = widget.getPreferredHeight();
            }
            if (this.heightOverrideExceptions.containsKey(widget)) {
                Integer heightSet = this.heightOverrideExceptions.get(widget);
                if (heightSet < 1) {
                    height = widget.getPreferredHeight();
                    heightSet = -heightSet;
                    if (heightSet != 0 && heightSet > height) {
                        height = heightSet;
                    }
                }
                else {
                    height = heightSet;
                }
            }
            totalheight += height + this.defaultPadding;
        }
        return totalheight;
    }
    
    public int getPreferredWidth() {
        return this.getParent().getWidth();
    }
    
    public void layout() {
        int totalheight = this.verticalPadding;
        for (int i = 0; i < this.getNumChildren(); ++i) {
            final Widget w = this.getChild(i);
            int height = this.childDefaultHeight;
            int width = this.childDefaultWidth;
            if (!this.overrideHeight) {
                height = w.getPreferredHeight();
            }
            if (this.heightOverrideExceptions.containsKey(w)) {
                Integer heightSet = this.heightOverrideExceptions.get(w);
                if (heightSet < 1) {
                    height = w.getPreferredHeight();
                    heightSet = -heightSet;
                    if (heightSet != 0 && heightSet > height) {
                        height = heightSet;
                    }
                }
                else {
                    height = heightSet;
                }
            }
            if (this.widthOverrideExceptions.containsKey(w)) {
                Integer widthSet = this.widthOverrideExceptions.get(w);
                if (widthSet < 1) {
                    width = w.getPreferredWidth();
                    widthSet = -widthSet;
                    if (widthSet != 0 && widthSet > width) {
                        width = widthSet;
                    }
                }
                else {
                    width = widthSet;
                }
            }
            w.setSize(width, height);
            w.setPosition(this.getX() + this.getWidth() / 2 - width / 2, this.getY() + totalheight);
            totalheight += height + this.defaultPadding;
        }
    }
}
