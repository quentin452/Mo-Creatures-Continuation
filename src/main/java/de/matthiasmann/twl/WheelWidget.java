package de.matthiasmann.twl;

import de.matthiasmann.twl.utils.*;
import de.matthiasmann.twl.model.*;
import de.matthiasmann.twl.renderer.*;

public class WheelWidget<T> extends Widget
{
    private final TypeMapping<ItemRenderer> itemRenderer;
    private final L listener;
    private final R renderer;
    private final Runnable timerCB;
    protected int itemHeight;
    protected int numVisibleItems;
    protected Image selectedOverlay;
    private static final int TIMER_INTERVAL = 30;
    private static final int MIN_SPEED = 3;
    private static final int MAX_SPEED = 100;
    protected Timer timer;
    protected int dragStartY;
    protected long lastDragTime;
    protected long lastDragDelta;
    protected int lastDragDist;
    protected boolean hasDragStart;
    protected boolean dragActive;
    protected int scrollOffset;
    protected int scrollAmount;
    protected ListModel<T> model;
    protected IntegerModel selectedModel;
    protected int selected;
    protected boolean cyclic;

    public WheelWidget() {
        this.itemRenderer = (TypeMapping<ItemRenderer>)new TypeMapping();
        this.listener = new L();
        this.renderer = new R();
        this.timerCB = WheelWidget.this::onTimer;
        this.itemRenderer.put(String.class, new StringItemRenderer());
        super.insertChild(this.renderer, 0);
        this.setCanAcceptKeyboardFocus(true);
    }

    public WheelWidget(final ListModel<T> model) {
        this();
        this.model = model;
    }

    public ListModel<T> getModel() {
        return this.model;
    }

    public void setModel(final ListModel<T> model) {
        this.removeListener();
        this.model = model;
        this.addListener();
        this.invalidateLayout();
    }

    public IntegerModel getSelectedModel() {
        return this.selectedModel;
    }

    public void setSelectedModel(final IntegerModel selectedModel) {
        this.removeSelectedListener();
        this.selectedModel = selectedModel;
        this.addSelectedListener();
    }

    public int getSelected() {
        return this.selected;
    }

    public void setSelected(final int selected) {
        final int oldSelected = this.selected;
        if (oldSelected != selected) {
            this.selected = selected;
            if (this.selectedModel != null) {
                this.selectedModel.setValue(selected);
            }
            this.firePropertyChange("selected", oldSelected, selected);
        }
    }

    public boolean isCyclic() {
        return this.cyclic;
    }

    public void setCyclic(final boolean cyclic) {
        this.cyclic = cyclic;
    }

    public int getItemHeight() {
        return this.itemHeight;
    }

    public int getNumVisibleItems() {
        return this.numVisibleItems;
    }

    public boolean removeItemRenderer(final Class<? extends T> clazz) {
        if (this.itemRenderer.remove((Class)clazz)) {
            super.removeAllChildren();
            this.invalidateLayout();
            return true;
        }
        return false;
    }

    public void registerItemRenderer(final Class<? extends T> clazz, final ItemRenderer value) {
        this.itemRenderer.put(clazz, value);
        this.invalidateLayout();
    }

    public void scroll(final int amount) {
        this.scrollInt(amount);
        this.scrollAmount = 0;
    }

    protected void scrollInt(final int amount) {
        int pos = this.selected;
        final int half = this.itemHeight / 2;
        this.scrollOffset += amount;
        while (this.scrollOffset >= half) {
            this.scrollOffset -= this.itemHeight;
            ++pos;
        }
        while (this.scrollOffset <= -half) {
            this.scrollOffset += this.itemHeight;
            --pos;
        }
        if (!this.cyclic) {
            final int n = this.getNumEntries();
            if (n > 0) {
                while (pos >= n) {
                    --pos;
                    this.scrollOffset += this.itemHeight;
                }
            }
            while (pos < 0) {
                ++pos;
                this.scrollOffset -= this.itemHeight;
            }
            this.scrollOffset = Math.max(-this.itemHeight, Math.min(this.itemHeight, this.scrollOffset));
        }
        this.setSelected(pos);
        if (this.scrollOffset == 0 && this.scrollAmount == 0) {
            this.stopTimer();
        }
        else {
            this.startTimer();
        }
    }

    public void autoScroll(final int dir) {
        if (dir != 0) {
            if (this.scrollAmount != 0 && Integer.signum(this.scrollAmount) != Integer.signum(dir)) {
                this.scrollAmount = dir;
            }
            else {
                this.scrollAmount += dir;
            }
            this.startTimer();
        }
    }

    @Override
    public int getPreferredInnerHeight() {
        return this.numVisibleItems * this.itemHeight;
    }

    @Override
    public int getPreferredInnerWidth() {
        int width = 0;
        for (int i = 0, n = this.getNumEntries(); i < n; ++i) {
            final Widget w = this.getItemRenderer(i);
            if (w != null) {
                width = Math.max(width, w.getPreferredWidth());
            }
        }
        return width;
    }

    @Override
    protected void paintOverlay(final GUI gui) {
        super.paintOverlay(gui);
        if (this.selectedOverlay != null) {
            int y = this.getInnerY() + this.itemHeight * (this.numVisibleItems / 2);
            if ((this.numVisibleItems & 0x1) == 0x0) {
                y -= this.itemHeight / 2;
            }
            this.selectedOverlay.draw((AnimationState)this.getAnimationState(), this.getX(), y, this.getWidth(), this.itemHeight);
        }
    }

    @Override
    protected boolean handleEvent(final Event evt) {
        if (evt.isMouseDragEnd() && this.dragActive) {
            final int absDist = Math.abs(this.lastDragDist);
            if (absDist > 3 && this.lastDragDelta > 0L) {
                final int amount = (int)Math.min(1000L, absDist * 100 / this.lastDragDelta);
                this.autoScroll(amount * Integer.signum(this.lastDragDist));
            }
            this.hasDragStart = false;
            this.dragActive = false;
            return true;
        }
        if (evt.isMouseDragEvent()) {
            if (this.hasDragStart) {
                final long time = this.getTime();
                this.dragActive = true;
                this.lastDragDist = this.dragStartY - evt.getMouseY();
                this.lastDragDelta = Math.max(1L, time - this.lastDragTime);
                this.scroll(this.lastDragDist);
                this.dragStartY = evt.getMouseY();
                this.lastDragTime = time;
            }
            return true;
        }
        if (super.handleEvent(evt)) {
            return true;
        }
        switch (evt.getType()) {
            case MOUSE_WHEEL: {
                this.autoScroll(this.itemHeight * evt.getMouseWheelDelta());
                return true;
            }
            case MOUSE_BTNDOWN: {
                if (evt.getMouseButton() == 0) {
                    this.dragStartY = evt.getMouseY();
                    this.lastDragTime = this.getTime();
                    this.hasDragStart = true;
                }
                return true;
            }
            case KEY_PRESSED: {
                switch (evt.getKeyCode()) {
                    case 200: {
                        this.autoScroll(-this.itemHeight);
                        return true;
                    }
                    case 208: {
                        this.autoScroll(this.itemHeight);
                        return true;
                    }
                    default: {
                        return false;
                    }
                }
            }
            default: {
                return evt.isMouseEvent();
            }
        }
    }

    protected long getTime() {
        final GUI gui = this.getGUI();
        return (gui != null) ? gui.getCurrentTime() : 0L;
    }

    protected int getNumEntries() {
        return (this.model == null) ? 0 : this.model.getNumEntries();
    }

    protected Widget getItemRenderer(final int i) {
        final T item = (T)this.model.getEntry(i);
        if (item != null) {
            final ItemRenderer ir = (ItemRenderer)this.itemRenderer.get((Class)item.getClass());
            if (ir != null) {
                final Widget w = ir.getRenderWidget(item);
                if (w != null) {
                    if (w.getParent() != this.renderer) {
                        w.setVisible(false);
                        this.renderer.add(w);
                    }
                    return w;
                }
            }
        }
        return null;
    }

    protected void startTimer() {
        if (this.timer != null && !this.timer.isRunning()) {
            this.timer.start();
        }
    }

    protected void stopTimer() {
        if (this.timer != null) {
            this.timer.stop();
        }
    }

    protected void onTimer() {
        int newAmount;
        int amount = newAmount = this.scrollAmount;
        if (amount == 0 && !this.dragActive) {
            amount = -this.scrollOffset;
        }
        if (amount != 0) {
            final int absAmount = Math.abs(amount);
            final int speed = absAmount * 30 / 200;
            final int dir = Integer.signum(amount) * Math.min(absAmount, Math.max(3, Math.min(100, speed)));
            if (newAmount != 0) {
                newAmount -= dir;
            }
            this.scrollAmount = newAmount;
            this.scrollInt(dir);
        }
    }

    @Override
    protected void layout() {
        this.layoutChildFullInnerArea(this.renderer);
    }

    @Override
    protected void applyTheme(final ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        this.applyThemeWheel(themeInfo);
    }

    protected void applyThemeWheel(final ThemeInfo themeInfo) {
        this.itemHeight = themeInfo.getParameter("itemHeight", 10);
        this.numVisibleItems = themeInfo.getParameter("visibleItems", 5);
        this.selectedOverlay = themeInfo.getImage("selectedOverlay");
        this.invalidateLayout();
    }

    @Override
    protected void afterAddToGUI(final GUI gui) {
        super.afterAddToGUI(gui);
        this.addListener();
        this.addSelectedListener();
        (this.timer = gui.createTimer()).setCallback(this.timerCB);
        this.timer.setDelay(30);
        this.timer.setContinuous(true);
    }

    @Override
    protected void beforeRemoveFromGUI(final GUI gui) {
        this.timer.stop();
        this.timer = null;
        this.removeListener();
        this.removeSelectedListener();
        super.beforeRemoveFromGUI(gui);
    }

    @Override
    public void insertChild(final Widget child, final int index) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAllChildren() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Widget removeChild(final int index) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    private void addListener() {
        if (this.model != null) {
            this.model.addChangeListener((ListModel.ChangeListener)this.listener);
        }
    }

    private void removeListener() {
        if (this.model != null) {
            this.model.removeChangeListener((ListModel.ChangeListener)this.listener);
        }
    }

    private void addSelectedListener() {
        if (this.selectedModel != null) {
            this.selectedModel.addCallback((Runnable)this.listener);
            this.syncSelected();
        }
    }

    private void removeSelectedListener() {
        if (this.selectedModel != null) {
            this.selectedModel.removeCallback((Runnable)this.listener);
        }
    }

    void syncSelected() {
        this.setSelected(this.selectedModel.getValue());
    }

    void entriesDeleted(final int first, final int last) {
        if (this.selected > first) {
            if (this.selected > last) {
                this.setSelected(this.selected - (last - first + 1));
            }
            else {
                this.setSelected(first);
            }
        }
        this.invalidateLayout();
    }

    void entriesInserted(final int first, final int last) {
        if (this.selected >= first) {
            this.setSelected(this.selected + (last - first + 1));
        }
        this.invalidateLayout();
    }

    class L implements ListModel.ChangeListener, Runnable
    {
        public void allChanged() {
            WheelWidget.this.invalidateLayout();
        }

        public void entriesChanged(final int first, final int last) {
            WheelWidget.this.invalidateLayout();
        }

        public void entriesDeleted(final int first, final int last) {
            WheelWidget.this.entriesDeleted(first, last);
        }

        public void entriesInserted(final int first, final int last) {
            WheelWidget.this.entriesInserted(first, last);
        }

        public void run() {
            WheelWidget.this.syncSelected();
        }
    }

    class R extends Widget
    {
        public R() {
            this.setTheme("");
            this.setClip(true);
        }

        @Override
        protected void paintWidget(final GUI gui) {
            if (WheelWidget.this.model == null) {
                return;
            }
            final int width = this.getInnerWidth();
            final int x = this.getInnerX();
            int y = this.getInnerY();
            final int numItems = WheelWidget.this.model.getNumEntries();
            int numDraw = WheelWidget.this.numVisibleItems;
            int startIdx = WheelWidget.this.selected - WheelWidget.this.numVisibleItems / 2;
            if ((numDraw & 0x1) == 0x0) {
                y -= WheelWidget.this.itemHeight / 2;
                ++numDraw;
            }
            if (WheelWidget.this.scrollOffset > 0) {
                y -= WheelWidget.this.scrollOffset;
                ++numDraw;
            }
            if (WheelWidget.this.scrollOffset < 0) {
                y -= WheelWidget.this.itemHeight + WheelWidget.this.scrollOffset;
                ++numDraw;
                --startIdx;
            }
            int i = 0;
        Label_0157:
            while (i < numDraw) {
            Label_0291:
                while (true) {
                    int idx;
                    for (idx = startIdx + i; idx < 0; idx += numItems) {
                        if (!WheelWidget.this.cyclic) {
                            ++i;
                            continue Label_0157;
                        }
                    }
                    while (idx >= numItems) {
                        if (!WheelWidget.this.cyclic) {
                            continue Label_0291;
                        }
                        idx -= numItems;
                    }
                    final Widget w = WheelWidget.this.getItemRenderer(idx);
                    if (w != null) {
                        w.setSize(width, WheelWidget.this.itemHeight);
                        w.setPosition(x, y + i * WheelWidget.this.itemHeight);
                        w.validateLayout();
                        this.paintChild(gui, w);
                    }
                    continue Label_0291;
                }
            }
        }

        @Override
        public void invalidateLayout() {
        }

        @Override
        protected void sizeChanged() {
        }
    }

    public static class StringItemRenderer extends Label implements ItemRenderer
    {
        public StringItemRenderer() {
            this.setCache(false);
        }

        public Widget getRenderWidget(final Object data) {
            this.setText(String.valueOf(data));
            return (Widget)this;
        }

        protected void sizeChanged() {
        }
    }

    public interface ItemRenderer
    {
        Widget getRenderWidget(final Object p0);
    }
}
