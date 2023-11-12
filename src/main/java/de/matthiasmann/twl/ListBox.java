package de.matthiasmann.twl;

import de.matthiasmann.twl.model.*;
import de.matthiasmann.twl.utils.*;
import de.matthiasmann.twl.renderer.*;

public class ListBox<T> extends Widget
{
    public static final int NO_SELECTION = -1;
    public static final int DEFAULT_CELL_HEIGHT = 20;
    public static final int SINGLE_COLUMN = -1;
    private static final ListBoxDisplay[] EMPTY_LABELS;
    private final ListModel.ChangeListener modelCallback;
    private final Scrollbar scrollbar;
    private ListBoxDisplay[] labels;
    private ListModel<T> model;
    private IntegerModel selectionModel;
    private Runnable selectionModelCallback;
    private int cellHeight;
    private int cellWidth;
    private boolean rowMajor;
    private boolean fixedCellWidth;
    private boolean fixedCellHeight;
    private int minDisplayedRows;
    private int numCols;
    private int firstVisible;
    private int selected;
    private int numEntries;
    private boolean needUpdate;
    private boolean inSetSelected;
    private CallbackWithReason<CallbackReason>[] callbacks;

    public ListBox() {
        this.cellHeight = 20;
        this.cellWidth = -1;
        this.rowMajor = true;
        this.minDisplayedRows = 1;
        this.numCols = 1;
        this.selected = -1;
        final LImpl li = new LImpl();
        this.modelCallback = li;
        (this.scrollbar = new Scrollbar()).addCallback(li);
        this.labels = ListBox.EMPTY_LABELS;
        super.insertChild(this.scrollbar, 0);
        this.setSize(200, 300);
        this.setCanAcceptKeyboardFocus(true);
        this.setDepthFocusTraversal(false);
    }

    public ListBox(final ListModel<T> model) {
        this();
        this.setModel(model);
    }

    public ListBox(final ListSelectionModel<T> model) {
        this();
        this.setModel(model);
    }

    public ListModel<T> getModel() {
        return this.model;
    }

    public void setModel(final ListModel<T> model) {
        if (this.model != model) {
            if (this.model != null) {
                this.model.removeChangeListener(this.modelCallback);
            }
            if ((this.model = model) != null) {
                model.addChangeListener(this.modelCallback);
            }
            this.modelCallback.allChanged();
        }
    }

    public IntegerModel getSelectionModel() {
        return this.selectionModel;
    }

    public void setSelectionModel(final IntegerModel selectionModel) {
        if (this.selectionModel != selectionModel) {
            if (this.selectionModel != null) {
                this.selectionModel.removeCallback(this.selectionModelCallback);
            }
            if ((this.selectionModel = selectionModel) != null) {
                if (this.selectionModelCallback == null) {
                    this.selectionModelCallback = new Runnable() {
                        @Override
                        public void run() {
                            ListBox.this.syncSelectionFromModel();
                        }
                    };
                }
                this.selectionModel.addCallback(this.selectionModelCallback);
                this.syncSelectionFromModel();
            }
        }
    }

    public void setModel(final ListSelectionModel<T> model) {
        this.setSelectionModel(null);
        if (model == null) {
            this.setModel((ListModel<T>)null);
        }
        else {
            this.setModel(model.getListModel());
            this.setSelectionModel(model);
        }
    }

    public void addCallback(final CallbackWithReason<CallbackReason> cb) {
        this.callbacks = CallbackSupport.addCallbackToList(this.callbacks, cb, CallbackWithReason.class);
    }

    public void removeCallback(final CallbackWithReason<CallbackReason> cb) {
        this.callbacks = CallbackSupport.removeCallbackFromList(this.callbacks, cb);
    }

    private void doCallback(final CallbackReason reason) {
        CallbackSupport.fireCallbacks(this.callbacks, reason);
    }

    public int getCellHeight() {
        return this.cellHeight;
    }

    public void setCellHeight(final int cellHeight) {
        if (cellHeight < 1) {
            throw new IllegalArgumentException("cellHeight < 1");
        }
        this.cellHeight = cellHeight;
    }

    public int getCellWidth() {
        return this.cellWidth;
    }

    public void setCellWidth(final int cellWidth) {
        if (cellWidth < 1 && cellWidth != -1) {
            throw new IllegalArgumentException("cellWidth < 1");
        }
        this.cellWidth = cellWidth;
    }

    public boolean isFixedCellHeight() {
        return this.fixedCellHeight;
    }

    public void setFixedCellHeight(final boolean fixedCellHeight) {
        this.fixedCellHeight = fixedCellHeight;
    }

    public boolean isFixedCellWidth() {
        return this.fixedCellWidth;
    }

    public void setFixedCellWidth(final boolean fixedCellWidth) {
        this.fixedCellWidth = fixedCellWidth;
    }

    public boolean isRowMajor() {
        return this.rowMajor;
    }

    public void setRowMajor(final boolean rowMajor) {
        this.rowMajor = rowMajor;
    }

    public int getFirstVisible() {
        return this.firstVisible;
    }

    public int getLastVisible() {
        return this.getFirstVisible() + this.labels.length - 1;
    }

    public void setFirstVisible(int firstVisible) {
        firstVisible = Math.max(0, Math.min(firstVisible, this.numEntries - 1));
        if (this.firstVisible != firstVisible) {
            this.firstVisible = firstVisible;
            this.scrollbar.setValue(firstVisible / this.numCols, false);
            this.needUpdate = true;
        }
    }

    public int getSelected() {
        return this.selected;
    }

    public void setSelected(final int selected) {
        this.setSelected(selected, true, CallbackReason.SET_SELECTED);
    }

    public void setSelected(final int selected, final boolean scroll) {
        this.setSelected(selected, scroll, CallbackReason.SET_SELECTED);
    }

    void setSelected(final int selected, final boolean scroll, final CallbackReason reason) {
        if (selected < -1 || selected >= this.numEntries) {
            throw new IllegalArgumentException();
        }
        if (scroll) {
            this.validateLayout();
            if (selected == -1) {
                this.setFirstVisible(0);
            }
            else {
                int delta = this.getFirstVisible() - selected;
                if (delta > 0) {
                    final int deltaRows = (delta + this.numCols - 1) / this.numCols;
                    this.setFirstVisible(this.getFirstVisible() - deltaRows * this.numCols);
                }
                else {
                    delta = selected - this.getLastVisible();
                    if (delta > 0) {
                        final int deltaRows = (delta + this.numCols - 1) / this.numCols;
                        this.setFirstVisible(this.getFirstVisible() + deltaRows * this.numCols);
                    }
                }
            }
        }
        if (this.selected != selected) {
            this.selected = selected;
            if (this.selectionModel != null) {
                try {
                    this.inSetSelected = true;
                    this.selectionModel.setValue(selected);
                }
                finally {
                    this.inSetSelected = false;
                }
            }
            this.needUpdate = true;
            this.doCallback(reason);
        }
        else if (reason.actionRequested() || reason == CallbackReason.MOUSE_CLICK) {
            this.doCallback(reason);
        }
    }

    public void scrollToSelected() {
        this.setSelected(this.selected, true, CallbackReason.SET_SELECTED);
    }

    public int getNumEntries() {
        return this.numEntries;
    }

    public int getNumRows() {
        return (this.numEntries + this.numCols - 1) / this.numCols;
    }

    public int getNumColumns() {
        return this.numCols;
    }

    public int findEntryByName(final String prefix) {
        for (int i = this.selected + 1; i < this.numEntries; ++i) {
            if (this.model.matchPrefix(i, prefix)) {
                return i;
            }
        }
        for (int i = 0; i < this.selected; ++i) {
            if (this.model.matchPrefix(i, prefix)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Widget getWidgetAt(final int x, final int y) {
        return this;
    }

    public int getEntryAt(final int x, final int y) {
        for (int n = Math.max(this.labels.length, this.numEntries - this.firstVisible), i = 0; i < n; ++i) {
            if (this.labels[i].getWidget().isInside(x, y)) {
                return this.firstVisible + i;
            }
        }
        return -1;
    }

    @Override
    public void insertChild(final Widget child, final int index) throws IndexOutOfBoundsException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAllChildren() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Widget removeChild(final int index) throws IndexOutOfBoundsException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void applyTheme(final ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        this.setCellHeight(themeInfo.getParameter("cellHeight", 20));
        this.setCellWidth(themeInfo.getParameter("cellWidth", -1));
        this.setRowMajor(themeInfo.getParameter("rowMajor", true));
        this.setFixedCellWidth(themeInfo.getParameter("fixedCellWidth", false));
        this.setFixedCellHeight(themeInfo.getParameter("fixedCellHeight", false));
        this.minDisplayedRows = themeInfo.getParameter("minDisplayedRows", 1);
    }

    protected void goKeyboard(final int dir) {
        final int newPos = this.selected + dir;
        if (newPos >= 0 && newPos < this.numEntries) {
            this.setSelected(newPos, true, CallbackReason.KEYBOARD);
        }
    }

    protected boolean isSearchChar(final char ch) {
        return ch != '\0' && Character.isLetterOrDigit(ch);
    }

    @Override
    protected void keyboardFocusGained() {
        this.setLabelFocused(true);
    }

    @Override
    protected void keyboardFocusLost() {
        this.setLabelFocused(false);
    }

    private void setLabelFocused(final boolean focused) {
        final int idx = this.selected - this.firstVisible;
        if (idx >= 0 && idx < this.labels.length) {
            this.labels[idx].setFocused(focused);
        }
    }

    public boolean handleEvent(final Event evt) {
        switch (evt.getType()) {
            case MOUSE_WHEEL: {
                this.scrollbar.scroll(-evt.getMouseWheelDelta());
                return true;
            }
            case KEY_PRESSED: {
                switch (evt.getKeyCode()) {
                    case 200: {
                        this.goKeyboard(-this.numCols);
                        break;
                    }
                    case 208: {
                        this.goKeyboard(this.numCols);
                        break;
                    }
                    case 203: {
                        this.goKeyboard(-1);
                        break;
                    }
                    case 205: {
                        this.goKeyboard(1);
                        break;
                    }
                    case 201: {
                        if (this.numEntries > 0) {
                            this.setSelected(Math.max(0, this.selected - this.labels.length), true, CallbackReason.KEYBOARD);
                            break;
                        }
                        break;
                    }
                    case 209: {
                        this.setSelected(Math.min(this.numEntries - 1, this.selected + this.labels.length), true, CallbackReason.KEYBOARD);
                        break;
                    }
                    case 199: {
                        if (this.numEntries > 0) {
                            this.setSelected(0, true, CallbackReason.KEYBOARD);
                            break;
                        }
                        break;
                    }
                    case 207: {
                        this.setSelected(this.numEntries - 1, true, CallbackReason.KEYBOARD);
                        break;
                    }
                    case 28: {
                        this.setSelected(this.selected, false, CallbackReason.KEYBOARD_RETURN);
                        break;
                    }
                    default: {
                        if (evt.hasKeyChar() && this.isSearchChar(evt.getKeyChar())) {
                            final int idx = this.findEntryByName(Character.toString(evt.getKeyChar()));
                            if (idx != -1) {
                                this.setSelected(idx, true, CallbackReason.KEYBOARD);
                            }
                            return true;
                        }
                        break;
                    }
                }
                return true;
            }
            case KEY_RELEASED: {
                return true;
            }
            default: {
                return super.handleEvent(evt) || evt.isMouseEvent();
            }
        }
    }

    @Override
    public int getMinWidth() {
        return Math.max(super.getMinWidth(), this.scrollbar.getMinWidth());
    }

    @Override
    public int getMinHeight() {
        int minHeight = Math.max(super.getMinHeight(), this.scrollbar.getMinHeight());
        if (this.minDisplayedRows > 0) {
            minHeight = Math.max(minHeight, this.getBorderVertical() + Math.min(this.numEntries, this.minDisplayedRows) * this.cellHeight);
        }
        return minHeight;
    }

    @Override
    public int getPreferredInnerWidth() {
        return Math.max(super.getPreferredInnerWidth(), this.scrollbar.getPreferredWidth());
    }

    @Override
    public int getPreferredInnerHeight() {
        return Math.max(this.getNumRows() * this.getCellHeight(), this.scrollbar.getPreferredHeight());
    }

    @Override
    protected void paint(final GUI gui) {
        if (this.needUpdate) {
            this.updateDisplay();
        }
        final int maxFirstVisibleRow = this.computeMaxFirstVisibleRow();
        this.scrollbar.setMinMaxValue(0, maxFirstVisibleRow);
        this.scrollbar.setValue(this.firstVisible / this.numCols, false);
        super.paint(gui);
    }

    private int computeMaxFirstVisibleRow() {
        int maxFirstVisibleRow = Math.max(0, this.numEntries - this.labels.length);
        maxFirstVisibleRow = (maxFirstVisibleRow + this.numCols - 1) / this.numCols;
        return maxFirstVisibleRow;
    }

    private void updateDisplay() {
        this.needUpdate = false;
        if (this.selected >= this.numEntries) {
            this.selected = -1;
        }
        final int maxFirstVisibleRow = this.computeMaxFirstVisibleRow();
        final int maxFirstVisible = maxFirstVisibleRow * this.numCols;
        if (this.firstVisible > maxFirstVisible) {
            this.firstVisible = Math.max(0, maxFirstVisible);
        }
        final boolean hasFocus = this.hasKeyboardFocus();
        for (int i = 0; i < this.labels.length; ++i) {
            final ListBoxDisplay label = this.labels[i];
            final int cell = i + this.firstVisible;
            if (cell < this.numEntries) {
                label.setData(this.model.getEntry(cell));
                label.setTooltipContent(this.model.getEntryTooltip(cell));
            }
            else {
                label.setData(null);
                label.setTooltipContent(null);
            }
            label.setSelected(cell == this.selected);
            label.setFocused(cell == this.selected && hasFocus);
        }
    }

    @Override
    protected void layout() {
        this.scrollbar.setSize(this.scrollbar.getPreferredWidth(), this.getInnerHeight());
        this.scrollbar.setPosition(this.getInnerRight() - this.scrollbar.getWidth(), this.getInnerY());
        final int numRows = Math.max(1, this.getInnerHeight() / this.cellHeight);
        if (this.cellWidth != -1) {
            this.numCols = Math.max(1, (this.scrollbar.getX() - this.getInnerX()) / this.cellWidth);
        }
        else {
            this.numCols = 1;
        }
        this.setVisibleCells(numRows);
        this.needUpdate = true;
    }

    private void setVisibleCells(final int numRows) {
        final int visibleCells = numRows * this.numCols;
        assert visibleCells >= 1;
        this.scrollbar.setPageSize(visibleCells);
        int i;
        final int curVisible = i = this.labels.length;
        while (i-- > visibleCells) {
            super.removeChild(1 + i);
        }
        final ListBoxDisplay[] newLabels = new ListBoxDisplay[visibleCells];
        System.arraycopy(this.labels, 0, newLabels, 0, Math.min(visibleCells, this.labels.length));
        this.labels = newLabels;
        for (int j = curVisible; j < visibleCells; ++j) {
            final int cellOffset = j;
            final ListBoxDisplay lbd = this.createDisplay();
            lbd.addListBoxCallback((CallbackWithReason<CallbackReason>)new CallbackWithReason<CallbackReason>() {
                public void callback(final CallbackReason reason) {
                    final int cell = ListBox.this.getFirstVisible() + cellOffset;
                    if (cell < ListBox.this.getNumEntries()) {
                        ListBox.this.setSelected(cell, false, reason);
                    }
                }
            });
            super.insertChild(lbd.getWidget(), 1 + j);
            this.labels[j] = lbd;
        }
        final int innerWidth = this.scrollbar.getX() - this.getInnerX();
        final int innerHeight = this.getInnerHeight();
        for (int k = 0; k < visibleCells; ++k) {
            int row;
            int col;
            if (this.rowMajor) {
                row = k / this.numCols;
                col = k % this.numCols;
            }
            else {
                row = k % numRows;
                col = k / numRows;
            }
            int y;
            int h;
            if (this.fixedCellHeight) {
                y = row * this.cellHeight;
                h = this.cellHeight;
            }
            else {
                y = row * innerHeight / numRows;
                h = (row + 1) * innerHeight / numRows - y;
            }
            int x;
            int w;
            if (this.fixedCellWidth && this.cellWidth != -1) {
                x = col * this.cellWidth;
                w = this.cellWidth;
            }
            else {
                x = col * innerWidth / this.numCols;
                w = (col + 1) * innerWidth / this.numCols - x;
            }
            final Widget cell = (Widget)this.labels[k];
            cell.setSize(Math.max(0, w), Math.max(0, h));
            cell.setPosition(x + this.getInnerX(), y + this.getInnerY());
        }
    }

    protected ListBoxDisplay createDisplay() {
        return new ListBoxLabel();
    }

    void entriesInserted(final int first, final int last) {
        final int delta = last - first + 1;
        final int prevNumEntries = this.numEntries;
        this.numEntries += delta;
        int fv = this.getFirstVisible();
        if (fv >= first && prevNumEntries >= this.labels.length) {
            fv += delta;
            this.setFirstVisible(fv);
        }
        final int s = this.getSelected();
        if (s >= first) {
            this.setSelected(s + delta, false, CallbackReason.MODEL_CHANGED);
        }
        if (first <= this.getLastVisible() && last >= fv) {
            this.needUpdate = true;
        }
    }

    void entriesDeleted(final int first, final int last) {
        final int delta = last - first + 1;
        this.numEntries -= delta;
        final int fv = this.getFirstVisible();
        final int lv = this.getLastVisible();
        if (fv > last) {
            this.setFirstVisible(fv - delta);
        }
        else if (fv <= last && lv >= first) {
            this.setFirstVisible(first);
        }
        final int s = this.getSelected();
        if (s > last) {
            this.setSelected(s - delta, false, CallbackReason.MODEL_CHANGED);
        }
        else if (s >= first && s <= last) {
            this.setSelected(-1, false, CallbackReason.MODEL_CHANGED);
        }
    }

    void entriesChanged(final int first, final int last) {
        final int fv = this.getFirstVisible();
        final int lv = this.getLastVisible();
        if (fv <= last && lv >= first) {
            this.needUpdate = true;
        }
    }

    void allChanged() {
        this.numEntries = ((this.model != null) ? this.model.getNumEntries() : 0);
        this.setSelected(-1, false, CallbackReason.MODEL_CHANGED);
        this.setFirstVisible(0);
        this.needUpdate = true;
    }

    void scrollbarChanged() {
        this.setFirstVisible(this.scrollbar.getValue() * this.numCols);
    }

    void syncSelectionFromModel() {
        if (!this.inSetSelected) {
            this.setSelected(this.selectionModel.getValue());
        }
    }

    static {
        EMPTY_LABELS = new ListBoxDisplay[0];
    }

    public enum CallbackReason
    {
        MODEL_CHANGED(false),
        SET_SELECTED(false),
        MOUSE_CLICK(false),
        MOUSE_DOUBLE_CLICK(true),
        KEYBOARD(false),
        KEYBOARD_RETURN(true);

        final boolean forceCallback;

        private CallbackReason(final boolean forceCallback) {
            this.forceCallback = forceCallback;
        }

        public boolean actionRequested() {
            return this.forceCallback;
        }
    }

    protected static class ListBoxLabel extends TextWidget implements ListBoxDisplay
    {
        public static final AnimationState.StateKey STATE_SELECTED;
        public static final AnimationState.StateKey STATE_EMPTY;
        private boolean selected;
        protected CallbackWithReason<CallbackReason>[] callbacks;

        public ListBoxLabel() {
            this.setClip(true);
            this.setTheme("display");
        }

        @Override
        public boolean isSelected() {
            return this.selected;
        }

        @Override
        public void setSelected(final boolean selected) {
            if (this.selected != selected) {
                this.selected = selected;
                this.getAnimationState().setAnimationState(ListBoxLabel.STATE_SELECTED, selected);
            }
        }

        @Override
        public boolean isFocused() {
            return this.getAnimationState().getAnimationState(ListBoxLabel.STATE_KEYBOARD_FOCUS);
        }

        @Override
        public void setFocused(final boolean focused) {
            this.getAnimationState().setAnimationState(ListBoxLabel.STATE_KEYBOARD_FOCUS, focused);
        }

        @Override
        public void setData(final Object data) {
            this.setCharSequence((data == null) ? "" : data.toString());
            this.getAnimationState().setAnimationState(ListBoxLabel.STATE_EMPTY, data == null);
        }

        @Override
        public Widget getWidget() {
            return this;
        }

        @Override
        public void addListBoxCallback(final CallbackWithReason<CallbackReason> cb) {
            this.callbacks = CallbackSupport.addCallbackToList(this.callbacks, cb, CallbackWithReason.class);
        }

        @Override
        public void removeListBoxCallback(final CallbackWithReason<CallbackReason> cb) {
            this.callbacks = CallbackSupport.removeCallbackFromList(this.callbacks, cb);
        }

        protected void doListBoxCallback(final CallbackReason reason) {
            CallbackSupport.fireCallbacks(callbacks, reason);
        }

        protected boolean handleListBoxEvent(final Event evt) {
            switch (evt.getType()) {
                case MOUSE_BTNDOWN: {
                    if (!this.selected) {
                        this.doListBoxCallback(CallbackReason.MOUSE_CLICK);
                    }
                    return true;
                }
                case MOUSE_CLICKED: {
                    if (this.selected && evt.getMouseClickCount() == 2) {
                        this.doListBoxCallback(CallbackReason.MOUSE_DOUBLE_CLICK);
                    }
                    return true;
                }
                default: {
                    return false;
                }
            }
        }

        @Override
        protected boolean handleEvent(final Event evt) {
            this.handleMouseHover(evt);
            return (!evt.isMouseDragEvent() && this.handleListBoxEvent(evt)) || super.handleEvent(evt) || evt.isMouseEventNoWheel();
        }

        static {
            STATE_SELECTED = AnimationState.StateKey.get("selected");
            STATE_EMPTY = AnimationState.StateKey.get("empty");
        }
    }

    private class LImpl implements ListModel.ChangeListener, Runnable
    {
        @Override
        public void entriesInserted(final int first, final int last) {
            ListBox.this.entriesInserted(first, last);
        }

        @Override
        public void entriesDeleted(final int first, final int last) {
            ListBox.this.entriesDeleted(first, last);
        }

        @Override
        public void entriesChanged(final int first, final int last) {
            ListBox.this.entriesChanged(first, last);
        }

        @Override
        public void allChanged() {
            ListBox.this.allChanged();
        }

        @Override
        public void run() {
            ListBox.this.scrollbarChanged();
        }
    }
}
