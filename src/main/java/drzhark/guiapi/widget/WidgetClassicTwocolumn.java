package drzhark.guiapi.widget;

import de.matthiasmann.twl.*;
import java.util.*;

public class WidgetClassicTwocolumn extends Widget
{
    public int childDefaultHeight;
    public int childDefaultWidth;
    public int defaultPadding;
    public Map<Widget, Integer> heightOverrideExceptions;
    public boolean overrideHeight;
    public int splitDistance;
    public int verticalPadding;
    public Map<Widget, Integer> widthOverrideExceptions;
    
    protected void paintChildren(final GUI gui) {
        final ScrollPane pane = ScrollPane.getContainingScrollPane((Widget)this);
        final boolean isScrolling = pane != null;
        int minY = 0;
        int maxY = 0;
        if (isScrolling) {
            minY = this.getParent().getY();
            maxY = minY + pane.getContentAreaHeight();
        }
        for (int i = 0, n = this.getNumChildren(); i < n; ++i) {
            final Widget child = this.getChild(i);
            if (child.isVisible()) {
                boolean draw = !isScrolling;
                if (!draw) {
                    if (child instanceof IWidgetAlwaysDraw) {
                        draw = true;
                    }
                    else if (child.getY() + child.getHeight() >= minY && child.getY() <= maxY) {
                        draw = true;
                    }
                }
                if (draw) {
                    this.paintChild(gui, child);
                }
            }
        }
    }
    
    public WidgetClassicTwocolumn(final Widget... widgets) {
        this.childDefaultHeight = 20;
        this.childDefaultWidth = 150;
        this.defaultPadding = 4;
        this.heightOverrideExceptions = new HashMap<Widget, Integer>();
        this.overrideHeight = true;
        this.splitDistance = 10;
        this.verticalPadding = 0;
        this.widthOverrideExceptions = new HashMap<Widget, Integer>();
        for (int i = 0; i < widgets.length; ++i) {
            this.add(widgets[i]);
        }
        this.setTheme("");
    }
    
    public int getPreferredHeight() {
        int totalheight = this.verticalPadding;
        for (int i = 0; i < this.getNumChildren(); i += 2) {
            final Widget w = this.getChild(i);
            Widget w2 = null;
            if (i + 1 != this.getNumChildren()) {
                w2 = this.getChild(i + 1);
            }
            int height = this.childDefaultHeight;
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
            if (w2 != null) {
                int temp = height;
                if (!this.overrideHeight) {
                    temp = w2.getPreferredHeight();
                }
                if (this.heightOverrideExceptions.containsKey(w2)) {
                    Integer heightSet2 = this.heightOverrideExceptions.get(w2);
                    if (heightSet2 < 1) {
                        height = w.getPreferredHeight();
                        heightSet2 = -heightSet2;
                        if (heightSet2 != 0 && heightSet2 > height) {
                            height = heightSet2;
                        }
                    }
                    else {
                        height = heightSet2;
                    }
                }
                if (temp > height) {
                    height = temp;
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
        if (this.getParent().getTheme().equals("scrollpane-notch")) {
            this.verticalPadding = 10;
        }
        int totalheight = this.verticalPadding;
        for (int i = 0; i < this.getNumChildren(); i += 2) {
            final Widget w = this.getChild(i);
            Widget w2 = null;
            try {
                w2 = this.getChild(i + 1);
            }
            catch (IndexOutOfBoundsException ex) {}
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
            if (w2 != null) {
                int temph = height;
                final int tempw = width;
                if (!this.overrideHeight) {
                    temph = w2.getPreferredHeight();
                }
                if (this.heightOverrideExceptions.containsKey(w2)) {
                    Integer heightSet2 = this.heightOverrideExceptions.get(w2);
                    if (heightSet2 < 1) {
                        height = w.getPreferredHeight();
                        heightSet2 = -heightSet2;
                        if (heightSet2 != 0 && heightSet2 > height) {
                            height = heightSet2;
                        }
                    }
                    else {
                        height = heightSet2;
                    }
                }
                if (this.widthOverrideExceptions.containsKey(w2)) {
                    Integer widthSet2 = this.widthOverrideExceptions.get(w2);
                    if (widthSet2 < 1) {
                        width = w2.getPreferredWidth();
                        widthSet2 = -widthSet2;
                        if (widthSet2 != 0 && widthSet2 > width) {
                            width = widthSet2;
                        }
                    }
                    else {
                        width = widthSet2;
                    }
                }
                if (temph > height) {
                    height = temph;
                }
                if (tempw > width) {
                    width = tempw;
                }
            }
            w.setSize(width, height);
            w.setPosition(this.getX() + this.getWidth() / 2 - (width + this.splitDistance / 2), this.getY() + totalheight);
            if (w2 != null) {
                w2.setSize(width, height);
                w2.setPosition(this.getX() + this.getWidth() / 2 + this.splitDistance / 2, this.getY() + totalheight);
            }
            totalheight += height + this.defaultPadding;
        }
    }
}
