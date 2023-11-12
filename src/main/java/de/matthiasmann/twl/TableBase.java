package de.matthiasmann.twl;

import de.matthiasmann.twl.utils.*;
import java.util.*;
import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.model.*;

public abstract class TableBase extends Widget implements ScrollPane.Scrollable, ScrollPane.AutoScrollable, ScrollPane.CustomPageSize
{
    public static final AnimationState.StateKey STATE_FIRST_COLUMNHEADER;
    public static final AnimationState.StateKey STATE_LAST_COLUMNHEADER;
    public static final AnimationState.StateKey STATE_ROW_SELECTED;
    public static final AnimationState.StateKey STATE_ROW_HOVER;
    public static final AnimationState.StateKey STATE_ROW_DROPTARGET;
    public static final AnimationState.StateKey STATE_ROW_ODD;
    public static final AnimationState.StateKey STATE_LEAD_ROW;
    public static final AnimationState.StateKey STATE_SELECTED;
    public static final AnimationState.StateKey STATE_SORT_ASCENDING;
    public static final AnimationState.StateKey STATE_SORT_DESCENDING;
    private final StringCellRenderer stringCellRenderer;
    private final RemoveCellWidgets removeCellWidgetsFunction;
    private final InsertCellWidgets insertCellWidgetsFunction;
    private final CellWidgetContainer cellWidgetContainer;
    protected final TypeMapping<CellRenderer> cellRenderers;
    protected final SparseGrid widgetGrid;
    protected final ColumnSizeSequence columnModel;
    protected TableColumnHeaderModel columnHeaderModel;
    protected SizeSequence rowModel;
    protected boolean hasCellWidgetCreators;
    protected ColumnHeader[] columnHeaders;
    protected CellRenderer[] columnDefaultCellRenderer;
    protected TableSelectionManager selectionManager;
    protected KeyboardSearchHandler keyboardSearchHandler;
    protected DragListener dragListener;
    protected Callback[] callbacks;
    protected Image imageColumnDivider;
    protected Image imageRowBackground;
    protected Image imageRowOverlay;
    protected Image imageRowDropMarker;
    protected ThemeInfo tableBaseThemeInfo;
    protected int columnHeaderHeight;
    protected int columnDividerDragableDistance;
    protected MouseCursor columnResizeCursor;
    protected MouseCursor normalCursor;
    protected MouseCursor dragNotPossibleCursor;
    protected boolean ensureColumnHeaderMinWidth;
    protected int numRows;
    protected int numColumns;
    protected int rowHeight;
    protected int defaultColumnWidth;
    protected boolean autoSizeAllRows;
    protected boolean updateAllCellWidgets;
    protected boolean updateAllColumnWidth;
    protected int scrollPosX;
    protected int scrollPosY;
    protected int firstVisibleRow;
    protected int firstVisibleColumn;
    protected int lastVisibleRow;
    protected int lastVisibleColumn;
    protected boolean firstRowPartialVisible;
    protected boolean lastRowPartialVisible;
    protected int dropMarkerRow;
    protected boolean dropMarkerBeforeRow;
    protected static final int LAST_MOUSE_Y_OUTSIDE = Integer.MIN_VALUE;
    protected int lastMouseY;
    protected int lastMouseRow;
    protected int lastMouseColumn;
    protected static final int DRAG_INACTIVE = 0;
    protected static final int DRAG_COLUMN_HEADER = 1;
    protected static final int DRAG_USER = 2;
    protected static final int DRAG_IGNORE = 3;
    protected int dragActive;
    protected int dragColumn;
    protected int dragStartX;
    protected int dragStartColWidth;
    protected int dragStartSumWidth;
    
    protected TableBase() {
        this.rowHeight = 32;
        this.defaultColumnWidth = 256;
        this.dropMarkerRow = -1;
        this.lastMouseY = Integer.MIN_VALUE;
        this.lastMouseRow = -1;
        this.lastMouseColumn = -1;
        this.cellRenderers = new TypeMapping<CellRenderer>();
        this.stringCellRenderer = new StringCellRenderer();
        this.widgetGrid = new SparseGrid(32);
        this.removeCellWidgetsFunction = new RemoveCellWidgets();
        this.insertCellWidgetsFunction = new InsertCellWidgets();
        this.columnModel = new ColumnSizeSequence();
        this.columnDefaultCellRenderer = new CellRenderer[8];
        super.insertChild(this.cellWidgetContainer = new CellWidgetContainer(), 0);
        this.setCanAcceptKeyboardFocus(true);
    }
    
    public TableSelectionManager getSelectionManager() {
        return this.selectionManager;
    }
    
    public void setSelectionManager(final TableSelectionManager selectionManager) {
        if (this.selectionManager != selectionManager) {
            if (this.selectionManager != null) {
                this.selectionManager.setAssociatedTable(null);
            }
            this.selectionManager = selectionManager;
            if (this.selectionManager != null) {
                this.selectionManager.setAssociatedTable(this);
            }
        }
    }
    
    public void setDefaultSelectionManager() {
        this.setSelectionManager(new TableRowSelectionManager());
    }
    
    public KeyboardSearchHandler getKeyboardSearchHandler() {
        return this.keyboardSearchHandler;
    }
    
    public void setKeyboardSearchHandler(final KeyboardSearchHandler keyboardSearchHandler) {
        this.keyboardSearchHandler = keyboardSearchHandler;
    }
    
    public DragListener getDragListener() {
        return this.dragListener;
    }
    
    public void setDragListener(final DragListener dragListener) {
        this.cancelDragging();
        this.dragListener = dragListener;
    }
    
    public boolean isDropMarkerBeforeRow() {
        return this.dropMarkerBeforeRow;
    }
    
    public int getDropMarkerRow() {
        return this.dropMarkerRow;
    }
    
    public void setDropMarker(final int row, final boolean beforeRow) {
        if (row < 0 || row > this.numRows) {
            throw new IllegalArgumentException("row");
        }
        if (row == this.numRows && !beforeRow) {
            throw new IllegalArgumentException("row");
        }
        this.dropMarkerRow = row;
        this.dropMarkerBeforeRow = beforeRow;
    }
    
    public boolean setDropMarker(final Event evt) {
        int mouseY = evt.getMouseY();
        if (this.isMouseInside(evt) && !this.isMouseInColumnHeader(mouseY)) {
            mouseY -= this.getOffsetY();
            final int row = this.getRowFromPosition(mouseY);
            if (row >= 0 && row < this.numRows) {
                final int rowStart = this.getRowStartPosition(row);
                final int rowEnd = this.getRowEndPosition(row);
                final int margin = (rowEnd - rowStart + 2) / 4;
                if (mouseY - rowStart < margin) {
                    this.setDropMarker(row, true);
                }
                else if (rowEnd - mouseY < margin) {
                    this.setDropMarker(row + 1, true);
                }
                else {
                    this.setDropMarker(row, false);
                }
                return true;
            }
            if (row == this.numRows) {
                this.setDropMarker(row, true);
                return true;
            }
        }
        return false;
    }
    
    public void clearDropMarker() {
        this.dropMarkerRow = -1;
    }
    
    public void addCallback(final Callback callback) {
        this.callbacks = CallbackSupport.addCallbackToList(this.callbacks, callback, Callback.class);
    }
    
    public void removeCallback(final Callback callback) {
        this.callbacks = CallbackSupport.removeCallbackFromList(this.callbacks, callback);
    }
    
    public boolean isVariableRowHeight() {
        return this.rowModel != null;
    }
    
    public void setVaribleRowHeight(final boolean varibleRowHeight) {
        if (varibleRowHeight && this.rowModel == null) {
            this.rowModel = new RowSizeSequence(this.numRows);
            this.autoSizeAllRows = true;
            this.invalidateLayout();
        }
        else if (!varibleRowHeight) {
            this.rowModel = null;
        }
    }
    
    public int getNumRows() {
        return this.numRows;
    }
    
    public int getNumColumns() {
        return this.numColumns;
    }
    
    public int getRowFromPosition(final int y) {
        if (y < 0) {
            return -1;
        }
        if (this.rowModel != null) {
            return this.rowModel.getIndex(y);
        }
        return Math.min(this.numRows - 1, y / this.rowHeight);
    }
    
    public int getRowStartPosition(final int row) {
        this.checkRowIndex(row);
        if (this.rowModel != null) {
            return this.rowModel.getPosition(row);
        }
        return row * this.rowHeight;
    }
    
    public int getRowHeight(final int row) {
        this.checkRowIndex(row);
        if (this.rowModel != null) {
            return this.rowModel.getSize(row);
        }
        return this.rowHeight;
    }
    
    public int getRowEndPosition(final int row) {
        this.checkRowIndex(row);
        if (this.rowModel != null) {
            return this.rowModel.getPosition(row + 1);
        }
        return (row + 1) * this.rowHeight;
    }
    
    public int getColumnFromPosition(final int x) {
        if (x >= 0) {
            final int column = this.columnModel.getIndex(x);
            return column;
        }
        return -1;
    }
    
    public int getColumnStartPosition(final int column) {
        this.checkColumnIndex(column);
        return this.columnModel.getPosition(column);
    }
    
    public int getColumnWidth(final int column) {
        this.checkColumnIndex(column);
        return this.columnModel.getSize(column);
    }
    
    public int getColumnEndPosition(final int column) {
        this.checkColumnIndex(column);
        return this.columnModel.getPosition(column + 1);
    }
    
    public void setColumnWidth(final int column, final int width) {
        this.checkColumnIndex(column);
        this.columnHeaders[column].setColumnWidth(width);
        if (this.columnModel.update(column)) {
            this.invalidateLayout();
        }
    }
    
    public de.matthiasmann.twl.AnimationState getColumnHeaderAnimationState(final int column) {
        this.checkColumnIndex(column);
        return this.columnHeaders[column].getAnimationState();
    }
    
    public void setColumnSortOrderAnimationState(final int sortColumn, final SortOrder sortOrder) {
        for (int column = 0; column < this.numColumns; ++column) {
            final de.matthiasmann.twl.AnimationState animState = this.columnHeaders[column].getAnimationState();
            animState.setAnimationState(TableBase.STATE_SORT_ASCENDING, column == sortColumn && sortOrder == SortOrder.ASCENDING);
            animState.setAnimationState(TableBase.STATE_SORT_DESCENDING, column == sortColumn && sortOrder == SortOrder.DESCENDING);
        }
    }
    
    public void scrollToRow(final int row) {
        final ScrollPane scrollPane = ScrollPane.getContainingScrollPane((Widget)this);
        if (scrollPane != null && this.numRows > 0) {
            scrollPane.validateLayout();
            final int rowStart = this.getRowStartPosition(row);
            final int rowEnd = this.getRowEndPosition(row);
            final int height = rowEnd - rowStart;
            scrollPane.scrollToAreaY(rowStart, height, height / 2);
        }
    }
    
    public int getNumVisibleRows() {
        int rows = this.lastVisibleRow - this.firstVisibleRow;
        if (!this.lastRowPartialVisible) {
            ++rows;
        }
        return rows;
    }
    
    @Override
    public int getMinHeight() {
        return Math.max(super.getMinHeight(), this.columnHeaderHeight);
    }
    
    @Override
    public int getPreferredInnerWidth() {
        if (this.getInnerWidth() == 0) {
            return this.columnModel.computePreferredWidth();
        }
        if (this.updateAllColumnWidth) {
            this.updateAllColumnWidth();
        }
        return (this.numColumns > 0) ? this.getColumnEndPosition(this.numColumns - 1) : 0;
    }
    
    @Override
    public int getPreferredInnerHeight() {
        if (this.autoSizeAllRows) {
            this.autoSizeAllRows();
        }
        return this.columnHeaderHeight + 1 + ((this.numRows > 0) ? this.getRowEndPosition(this.numRows - 1) : 0);
    }
    
    public void registerCellRenderer(final Class<?> dataClass, final CellRenderer cellRenderer) {
        if (dataClass == null) {
            throw new NullPointerException("dataClass");
        }
        this.cellRenderers.put(dataClass, cellRenderer);
        if (cellRenderer instanceof CellWidgetCreator) {
            this.hasCellWidgetCreators = true;
        }
        if (this.tableBaseThemeInfo != null) {
            this.applyCellRendererTheme(cellRenderer);
        }
    }
    
    public void setScrollPosition(final int scrollPosX, final int scrollPosY) {
        if (this.scrollPosX != scrollPosX || this.scrollPosY != scrollPosY) {
            this.scrollPosX = scrollPosX;
            this.scrollPosY = scrollPosY;
            this.invalidateLayoutLocally();
        }
    }
    
    public void adjustScrollPosition(final int row) {
        this.checkRowIndex(row);
        final ScrollPane scrollPane = ScrollPane.getContainingScrollPane((Widget)this);
        final int numVisibleRows = this.getNumVisibleRows();
        if (numVisibleRows >= 1 && scrollPane != null) {
            if (row < this.firstVisibleRow || (row == this.firstVisibleRow && this.firstRowPartialVisible)) {
                final int pos = this.getRowStartPosition(row);
                scrollPane.setScrollPositionY(pos);
            }
            else if (row > this.lastVisibleRow || (row == this.lastVisibleRow && this.lastRowPartialVisible)) {
                final int innerHeight = Math.max(0, this.getInnerHeight() - this.columnHeaderHeight);
                int pos2 = this.getRowEndPosition(row);
                pos2 = Math.max(0, pos2 - innerHeight);
                scrollPane.setScrollPositionY(pos2);
            }
        }
    }
    
    public int getAutoScrollDirection(final Event evt, final int autoScrollArea) {
        final int areaY = this.getInnerY() + this.columnHeaderHeight;
        final int areaHeight = this.getInnerHeight() - this.columnHeaderHeight;
        int mouseY = evt.getMouseY();
        if (mouseY >= areaY && mouseY < areaY + areaHeight) {
            mouseY -= areaY;
            if (mouseY <= autoScrollArea || areaHeight - mouseY <= autoScrollArea) {
                if (mouseY < areaHeight / 2) {
                    return -1;
                }
                return 1;
            }
        }
        return 0;
    }
    
    public int getPageSizeX(final int availableWidth) {
        return availableWidth;
    }
    
    public int getPageSizeY(final int availableHeight) {
        return availableHeight - this.columnHeaderHeight;
    }
    
    public boolean isFixedWidthMode() {
        final ScrollPane scrollPane = ScrollPane.getContainingScrollPane((Widget)this);
        return scrollPane == null || scrollPane.getFixed() == ScrollPane.Fixed.HORIZONTAL;
    }
    
    protected final void checkRowIndex(final int row) {
        if (row < 0 || row >= this.numRows) {
            throw new IndexOutOfBoundsException("row");
        }
    }
    
    protected final void checkColumnIndex(final int column) {
        if (column < 0 || column >= this.numColumns) {
            throw new IndexOutOfBoundsException("column");
        }
    }
    
    protected final void checkRowRange(final int idx, final int count) {
        if (idx < 0 || count < 0 || count > this.numRows || idx > this.numRows - count) {
            throw new IllegalArgumentException("row");
        }
    }
    
    protected final void checkColumnRange(final int idx, final int count) {
        if (idx < 0 || count < 0 || count > this.numColumns || idx > this.numColumns - count) {
            throw new IllegalArgumentException("column");
        }
    }
    
    @Override
    protected void applyTheme(final ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        this.applyThemeTableBase(themeInfo);
        this.updateAll();
    }
    
    protected void applyThemeTableBase(final ThemeInfo themeInfo) {
        this.tableBaseThemeInfo = themeInfo;
        this.imageColumnDivider = themeInfo.getImage("columnDivider");
        this.imageRowBackground = themeInfo.getImage("row.background");
        this.imageRowOverlay = themeInfo.getImage("row.overlay");
        this.imageRowDropMarker = themeInfo.getImage("row.dropmarker");
        this.rowHeight = themeInfo.getParameter("rowHeight", 32);
        this.defaultColumnWidth = themeInfo.getParameter("columnHeaderWidth", 256);
        this.columnHeaderHeight = themeInfo.getParameter("columnHeaderHeight", 10);
        this.columnDividerDragableDistance = themeInfo.getParameter("columnDividerDragableDistance", 3);
        this.ensureColumnHeaderMinWidth = themeInfo.getParameter("ensureColumnHeaderMinWidth", false);
        for (final CellRenderer cellRenderer : this.cellRenderers.getUniqueValues()) {
            this.applyCellRendererTheme(cellRenderer);
        }
        this.applyCellRendererTheme(this.stringCellRenderer);
        this.updateAllColumnWidth = true;
    }
    
    @Override
    protected void applyThemeMouseCursor(final ThemeInfo themeInfo) {
        this.columnResizeCursor = themeInfo.getMouseCursor("columnResizeCursor");
        this.normalCursor = themeInfo.getMouseCursor("mouseCursor");
        this.dragNotPossibleCursor = themeInfo.getMouseCursor("dragNotPossibleCursor");
    }
    
    protected void applyCellRendererTheme(final CellRenderer cellRenderer) {
        final String childThemeName = cellRenderer.getTheme();
        assert !Widget.isAbsoluteTheme(childThemeName);
        final ThemeInfo childTheme = this.tableBaseThemeInfo.getChildTheme(childThemeName);
        if (childTheme != null) {
            cellRenderer.applyTheme(childTheme);
        }
    }
    
    @Override
    public void removeAllChildren() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected void childAdded(final Widget child) {
    }
    
    @Override
    protected void childRemoved(final Widget exChild) {
    }
    
    protected int getOffsetX() {
        return this.getInnerX() - this.scrollPosX;
    }
    
    protected int getOffsetY() {
        return this.getInnerY() - this.scrollPosY + this.columnHeaderHeight;
    }
    
    @Override
    protected void positionChanged() {
        super.positionChanged();
        if (this.keyboardSearchHandler != null) {
            this.keyboardSearchHandler.updateInfoWindowPosition();
        }
    }
    
    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        if (this.isFixedWidthMode()) {
            this.updateAllColumnWidth = true;
        }
        if (this.keyboardSearchHandler != null) {
            this.keyboardSearchHandler.updateInfoWindowPosition();
        }
    }
    
    @Override
    protected Object getTooltipContentAt(final int mouseX, final int mouseY) {
        if (this.lastMouseRow >= 0 && this.lastMouseRow < this.getNumRows() && this.lastMouseColumn >= 0 && this.lastMouseColumn < this.getNumColumns()) {
            final Object tooltip = this.getTooltipContentFromRow(this.lastMouseRow, this.lastMouseColumn);
            if (tooltip != null) {
                return tooltip;
            }
        }
        return super.getTooltipContentAt(mouseX, mouseY);
    }
    
    @Override
    protected void layout() {
        final int innerWidth = this.getInnerWidth();
        final int innerHeight = Math.max(0, this.getInnerHeight() - this.columnHeaderHeight);
        this.cellWidgetContainer.setPosition(this.getInnerX(), this.getInnerY() + this.columnHeaderHeight);
        this.cellWidgetContainer.setSize(innerWidth, innerHeight);
        if (this.updateAllColumnWidth) {
            this.updateAllColumnWidth();
        }
        if (this.autoSizeAllRows) {
            this.autoSizeAllRows();
        }
        if (this.updateAllCellWidgets) {
            this.updateAllCellWidgets();
        }
        final int scrollEndX = this.scrollPosX + innerWidth;
        final int scrollEndY = this.scrollPosY + innerHeight;
        final int startRow = Math.min(this.numRows - 1, Math.max(0, this.getRowFromPosition(this.scrollPosY)));
        final int startColumn = Math.min(this.numColumns - 1, Math.max(0, this.getColumnFromPosition(this.scrollPosX)));
        final int endRow = Math.min(this.numRows - 1, Math.max(startRow, this.getRowFromPosition(scrollEndY)));
        final int endColumn = Math.min(this.numColumns - 1, Math.max(startColumn, this.getColumnFromPosition(scrollEndX)));
        if (this.numRows > 0) {
            this.firstRowPartialVisible = (this.getRowStartPosition(startRow) < this.scrollPosY);
            this.lastRowPartialVisible = (this.getRowEndPosition(endRow) > scrollEndY);
        }
        else {
            this.firstRowPartialVisible = false;
            this.lastRowPartialVisible = false;
        }
        if (!this.widgetGrid.isEmpty()) {
            if (startRow > this.firstVisibleRow) {
                this.widgetGrid.iterate(this.firstVisibleRow, 0, startRow - 1, this.numColumns, this.removeCellWidgetsFunction);
            }
            if (endRow < this.lastVisibleRow) {
                this.widgetGrid.iterate(endRow + 1, 0, this.lastVisibleRow, this.numColumns, this.removeCellWidgetsFunction);
            }
            this.widgetGrid.iterate(startRow, 0, endRow, this.numColumns, this.insertCellWidgetsFunction);
        }
        this.firstVisibleRow = startRow;
        this.firstVisibleColumn = startColumn;
        this.lastVisibleRow = endRow;
        this.lastVisibleColumn = endColumn;
        if (this.numColumns > 0) {
            final int offsetX = this.getOffsetX();
            int colStartPos = this.getColumnStartPosition(0);
            for (int i = 0; i < this.numColumns; ++i) {
                final int colEndPos = this.getColumnEndPosition(i);
                final Widget w = (Widget)this.columnHeaders[i];
                if (w != null) {
                    assert w.getParent() == this;
                    w.setPosition(offsetX + colStartPos + this.columnDividerDragableDistance, this.getInnerY());
                    w.setSize(Math.max(0, colEndPos - colStartPos - 2 * this.columnDividerDragableDistance), this.columnHeaderHeight);
                    w.setVisible(this.columnHeaderHeight > 0);
                    final de.matthiasmann.twl.AnimationState animationState = w.getAnimationState();
                    animationState.setAnimationState(TableBase.STATE_FIRST_COLUMNHEADER, i == 0);
                    animationState.setAnimationState(TableBase.STATE_LAST_COLUMNHEADER, i == this.numColumns - 1);
                }
                colStartPos = colEndPos;
            }
        }
    }
    
    @Override
    protected void paintWidget(final GUI gui) {
        if (this.firstVisibleRow < 0 || this.firstVisibleRow >= this.numRows) {
            return;
        }
        final int innerX = this.getInnerX();
        final int innerY = this.getInnerY() + this.columnHeaderHeight;
        final int innerWidth = this.getInnerWidth();
        final int innerHeight = this.getInnerHeight() - this.columnHeaderHeight;
        final int offsetX = this.getOffsetX();
        final int offsetY = this.getOffsetY();
        final Renderer renderer = gui.getRenderer();
        renderer.clipEnter(innerX, innerY, innerWidth, innerHeight);
        try {
            final de.matthiasmann.twl.AnimationState animState = this.getAnimationState();
            int leadRow;
            boolean isCellSelection;
            if (this.selectionManager != null) {
                leadRow = this.selectionManager.getLeadRow();
                final int leadColumn = this.selectionManager.getLeadColumn();
                isCellSelection = (this.selectionManager.getSelectionGranularity() == TableSelectionManager.SelectionGranularity.CELLS);
            }
            else {
                leadRow = -1;
                final int leadColumn = -1;
                isCellSelection = false;
            }
            if (this.imageRowBackground != null) {
                this.paintRowImage(this.imageRowBackground, leadRow);
            }
            if (this.imageColumnDivider != null) {
                animState.setAnimationState(TableBase.STATE_ROW_SELECTED, false);
                for (int col = this.firstVisibleColumn; col <= this.lastVisibleColumn; ++col) {
                    final int colEndPos = this.getColumnEndPosition(col);
                    final int curX = offsetX + colEndPos;
                    this.imageColumnDivider.draw((AnimationState)animState, curX, innerY, 1, innerHeight);
                }
            }
            int rowStartPos = this.getRowStartPosition(this.firstVisibleRow);
            for (int row = this.firstVisibleRow; row <= this.lastVisibleRow; ++row) {
                final int rowEndPos = this.getRowEndPosition(row);
                final int curRowHeight = rowEndPos - rowStartPos;
                final int curY = offsetY + rowStartPos;
                final TreeTableNode rowNode = this.getNodeFromRow(row);
                final boolean isRowSelected = !isCellSelection && this.isRowSelected(row);
                int colStartPos = this.getColumnStartPosition(this.firstVisibleColumn);
                int colEndPos2;
                int colSpan;
                for (int col2 = this.firstVisibleColumn; col2 <= this.lastVisibleColumn; col2 += Math.max(1, colSpan), colStartPos = colEndPos2) {
                    colEndPos2 = this.getColumnEndPosition(col2);
                    final CellRenderer cellRenderer = this.getCellRenderer(row, col2, rowNode);
                    final boolean isCellSelected = isRowSelected || this.isCellSelected(row, col2);
                    final int curX2 = offsetX + colStartPos;
                    colSpan = 1;
                    if (cellRenderer != null) {
                        colSpan = cellRenderer.getColumnSpan();
                        if (colSpan > 1) {
                            colEndPos2 = this.getColumnEndPosition(Math.max(this.numColumns - 1, col2 + colSpan - 1));
                        }
                        final Widget cellRendererWidget = cellRenderer.getCellRenderWidget(curX2, curY, colEndPos2 - colStartPos, curRowHeight, isCellSelected);
                        if (cellRendererWidget != null) {
                            if (cellRendererWidget.getParent() != this) {
                                this.insertCellRenderer(cellRendererWidget);
                            }
                            this.paintChild(gui, cellRendererWidget);
                        }
                    }
                }
                rowStartPos = rowEndPos;
            }
            if (this.imageRowOverlay != null) {
                this.paintRowImage(this.imageRowOverlay, leadRow);
            }
            if (this.dropMarkerRow >= 0 && this.dropMarkerBeforeRow && this.imageRowDropMarker != null) {
                final int y = (this.rowModel != null) ? this.rowModel.getPosition(this.dropMarkerRow) : (this.dropMarkerRow * this.rowHeight);
                this.imageRowDropMarker.draw((AnimationState)animState, this.getOffsetX(), this.getOffsetY() + y, this.columnModel.getEndPosition(), 1);
            }
        }
        finally {
            renderer.clipLeave();
        }
    }
    
    private void paintRowImage(final Image img, final int leadRow) {
        final de.matthiasmann.twl.AnimationState animState = this.getAnimationState();
        final int x = this.getOffsetX();
        final int width = this.columnModel.getEndPosition();
        final int offsetY = this.getOffsetY();
        int rowStartPos = this.getRowStartPosition(this.firstVisibleRow);
        for (int row = this.firstVisibleRow; row <= this.lastVisibleRow; ++row) {
            final int rowEndPos = this.getRowEndPosition(row);
            final int curRowHeight = rowEndPos - rowStartPos;
            final int curY = offsetY + rowStartPos;
            animState.setAnimationState(TableBase.STATE_ROW_SELECTED, this.isRowSelected(row));
            animState.setAnimationState(TableBase.STATE_ROW_HOVER, this.dragActive == 0 && this.lastMouseY >= curY && this.lastMouseY < curY + curRowHeight);
            animState.setAnimationState(TableBase.STATE_LEAD_ROW, row == leadRow);
            animState.setAnimationState(TableBase.STATE_ROW_DROPTARGET, !this.dropMarkerBeforeRow && row == this.dropMarkerRow);
            animState.setAnimationState(TableBase.STATE_ROW_ODD, (row & 0x1) == 0x1);
            img.draw((AnimationState)animState, x, curY, width, curRowHeight);
            rowStartPos = rowEndPos;
        }
    }
    
    protected void insertCellRenderer(final Widget widget) {
        final int posX = widget.getX();
        final int posY = widget.getY();
        widget.setVisible(false);
        super.insertChild(widget, super.getNumChildren());
        widget.setPosition(posX, posY);
    }
    
    protected abstract TreeTableNode getNodeFromRow(final int p0);
    
    protected abstract Object getCellData(final int p0, final int p1, final TreeTableNode p2);
    
    protected abstract Object getTooltipContentFromRow(final int p0, final int p1);
    
    protected boolean isRowSelected(final int row) {
        return this.selectionManager != null && this.selectionManager.isRowSelected(row);
    }
    
    protected boolean isCellSelected(final int row, final int column) {
        return this.selectionManager != null && this.selectionManager.isCellSelected(row, column);
    }
    
    public void setColumnDefaultCellRenderer(final int column, final CellRenderer cellRenderer) {
        if (column >= this.columnDefaultCellRenderer.length) {
            final CellRenderer[] tmp = new CellRenderer[Math.max(column + 1, this.numColumns)];
            System.arraycopy(this.columnDefaultCellRenderer, 0, tmp, 0, this.columnDefaultCellRenderer.length);
            this.columnDefaultCellRenderer = tmp;
        }
        this.columnDefaultCellRenderer[column] = cellRenderer;
    }
    
    public CellRenderer getColumnDefaultCellRenderer(final int column) {
        if (column < this.columnDefaultCellRenderer.length) {
            return this.columnDefaultCellRenderer[column];
        }
        return null;
    }
    
    protected CellRenderer getCellRendererNoDefault(final Object data) {
        final Class<?> dataClass = data.getClass();
        return this.cellRenderers.get(dataClass);
    }
    
    protected CellRenderer getDefaultCellRenderer(final int col) {
        CellRenderer cellRenderer = this.getColumnDefaultCellRenderer(col);
        if (cellRenderer == null) {
            cellRenderer = this.stringCellRenderer;
        }
        return cellRenderer;
    }
    
    protected CellRenderer getCellRenderer(final Object data, final int col) {
        CellRenderer cellRenderer = this.getCellRendererNoDefault(data);
        if (cellRenderer == null) {
            cellRenderer = this.getDefaultCellRenderer(col);
        }
        return cellRenderer;
    }
    
    protected CellRenderer getCellRenderer(final int row, final int col, final TreeTableNode node) {
        final Object data = this.getCellData(row, col, node);
        if (data != null) {
            final CellRenderer cellRenderer = this.getCellRenderer(data, col);
            cellRenderer.setCellData(row, col, data);
            return cellRenderer;
        }
        return null;
    }
    
    protected int computeRowHeight(final int row) {
        final TreeTableNode rowNode = this.getNodeFromRow(row);
        int height = 0;
        for (int column = 0; column < this.numColumns; ++column) {
            final CellRenderer cellRenderer = this.getCellRenderer(row, column, rowNode);
            if (cellRenderer != null) {
                height = Math.max(height, cellRenderer.getPreferredHeight());
                column += Math.max(cellRenderer.getColumnSpan() - 1, 0);
            }
        }
        return height;
    }
    
    protected int clampColumnWidth(final int width) {
        return Math.max(2 * this.columnDividerDragableDistance + 1, width);
    }
    
    protected int computePreferredColumnWidth(final int index) {
        return this.clampColumnWidth(this.columnHeaders[index].getPreferredWidth());
    }
    
    protected boolean autoSizeRow(final int row) {
        final int height = this.computeRowHeight(row);
        return this.rowModel.setSize(row, height);
    }
    
    protected void autoSizeAllRows() {
        if (this.rowModel != null) {
            this.rowModel.initializeAll(this.numRows);
        }
        this.autoSizeAllRows = false;
    }
    
    protected void removeCellWidget(final Widget widget) {
        final int idx = this.cellWidgetContainer.getChildIndex(widget);
        if (idx >= 0) {
            this.cellWidgetContainer.removeChild(idx);
        }
    }
    
    void insertCellWidget(final int row, final int column, final WidgetEntry widgetEntry) {
        final CellWidgetCreator cwc = (CellWidgetCreator)this.getCellRenderer(row, column, null);
        final Widget widget = widgetEntry.widget;
        if (widget != null) {
            if (widget.getParent() != this.cellWidgetContainer) {
                this.cellWidgetContainer.insertChild(widget, this.cellWidgetContainer.getNumChildren());
            }
            final int x = this.getColumnStartPosition(column);
            final int w = this.getColumnEndPosition(column) - x;
            final int y = this.getRowStartPosition(row);
            final int h = this.getRowEndPosition(row) - y;
            cwc.positionWidget(widget, x + this.getOffsetX(), y + this.getOffsetY(), w, h);
        }
    }
    
    protected void updateCellWidget(final int row, final int column) {
        WidgetEntry we = (WidgetEntry)this.widgetGrid.get(row, column);
        Widget oldWidget = (we != null) ? we.widget : null;
        Widget newWidget = null;
        final TreeTableNode rowNode = this.getNodeFromRow(row);
        final CellRenderer cellRenderer = this.getCellRenderer(row, column, rowNode);
        if (cellRenderer instanceof CellWidgetCreator) {
            final CellWidgetCreator cellWidgetCreator = (CellWidgetCreator)cellRenderer;
            if (we != null && we.creator != cellWidgetCreator) {
                this.removeCellWidget(oldWidget);
                oldWidget = null;
            }
            newWidget = cellWidgetCreator.updateWidget(oldWidget);
            if (newWidget != null) {
                if (we == null) {
                    we = new WidgetEntry();
                    this.widgetGrid.set(row, column, we);
                }
                we.widget = newWidget;
                we.creator = cellWidgetCreator;
            }
        }
        if (newWidget == null && we != null) {
            this.widgetGrid.remove(row, column);
        }
        if (oldWidget != null && newWidget != oldWidget) {
            this.removeCellWidget(oldWidget);
        }
    }
    
    protected void updateAllCellWidgets() {
        if (!this.widgetGrid.isEmpty() || this.hasCellWidgetCreators) {
            for (int row = 0; row < this.numRows; ++row) {
                for (int col = 0; col < this.numColumns; ++col) {
                    this.updateCellWidget(row, col);
                }
            }
        }
        this.updateAllCellWidgets = false;
    }
    
    protected void removeAllCellWidgets() {
        this.cellWidgetContainer.removeAllChildren();
    }
    
    protected DialogLayout.Gap getColumnMPM(final int column) {
        if (this.tableBaseThemeInfo != null) {
            final ParameterMap columnWidthMap = this.tableBaseThemeInfo.getParameterMap("columnWidths");
            final Object obj = columnWidthMap.getParameterValue(Integer.toString(column), false);
            if (obj instanceof DialogLayout.Gap) {
                return (DialogLayout.Gap)obj;
            }
            if (obj instanceof Integer) {
                return new DialogLayout.Gap((int)obj);
            }
        }
        return null;
    }
    
    protected ColumnHeader createColumnHeader(final int column) {
        final ColumnHeader btn = new ColumnHeader();
        btn.setTheme("columnHeader");
        btn.setCanAcceptKeyboardFocus(false);
        super.insertChild((Widget)btn, super.getNumChildren());
        return btn;
    }
    
    protected void updateColumnHeader(final int column) {
        final Button columnHeader = this.columnHeaders[column];
        columnHeader.setText(this.columnHeaderModel.getColumnHeaderText(column));
        final AnimationState.StateKey[] states = this.columnHeaderModel.getColumnHeaderStates();
        if (states.length > 0) {
            final de.matthiasmann.twl.AnimationState animationState = columnHeader.getAnimationState();
            for (int i = 0; i < states.length; ++i) {
                animationState.setAnimationState(states[i], this.columnHeaderModel.getColumnHeaderState(column, i));
            }
        }
    }
    
    protected void updateColumnHeaderNumbers() {
        for (int i = 0; i < this.columnHeaders.length; ++i) {
            this.columnHeaders[i].column = i;
        }
    }
    
    private void removeColumnHeaders(final int column, final int count) throws IndexOutOfBoundsException {
        for (int i = 0; i < count; ++i) {
            final int idx = super.getChildIndex((Widget)this.columnHeaders[column + i]);
            if (idx >= 0) {
                super.removeChild(idx);
            }
        }
    }
    
    protected boolean isMouseInColumnHeader(int y) {
        y -= this.getInnerY();
        return y >= 0 && y < this.columnHeaderHeight;
    }
    
    protected int getColumnSeparatorUnderMouse(int x) {
        x -= this.getOffsetX();
        x += this.columnDividerDragableDistance;
        final int col = this.columnModel.getIndex(x);
        final int dist = x - this.columnModel.getPosition(col);
        if (dist < 2 * this.columnDividerDragableDistance) {
            return col - 1;
        }
        return -1;
    }
    
    protected int getRowUnderMouse(int y) {
        y -= this.getOffsetY();
        final int row = this.getRowFromPosition(y);
        return row;
    }
    
    protected int getColumnUnderMouse(int x) {
        x -= this.getOffsetX();
        final int col = this.columnModel.getIndex(x);
        return col;
    }
    
    @Override
    protected boolean handleEvent(final Event evt) {
        if (this.dragActive != 0) {
            return this.handleDragEvent(evt);
        }
        if (evt.isKeyEvent() && this.keyboardSearchHandler != null && this.keyboardSearchHandler.isActive() && this.keyboardSearchHandler.handleKeyEvent(evt)) {
            return true;
        }
        if (super.handleEvent(evt)) {
            return true;
        }
        if (evt.isMouseEvent()) {
            return this.handleMouseEvent(evt);
        }
        return evt.isKeyEvent() && this.keyboardSearchHandler != null && this.keyboardSearchHandler.handleKeyEvent(evt);
    }
    
    @Override
    protected boolean handleKeyStrokeAction(final String action, final Event event) {
        if (!super.handleKeyStrokeAction(action, event)) {
            if (this.selectionManager == null) {
                return false;
            }
            if (!this.selectionManager.handleKeyStrokeAction(action, event)) {
                return false;
            }
        }
        this.requestKeyboardFocus(null);
        return true;
    }
    
    protected void cancelDragging() {
        if (this.dragActive == 2) {
            if (this.dragListener != null) {
                this.dragListener.dragCanceled();
            }
            this.dragActive = 3;
        }
    }
    
    protected boolean handleDragEvent(final Event evt) {
        if (evt.isMouseEvent()) {
            return this.handleMouseEvent(evt);
        }
        if (evt.isKeyPressedEvent() && evt.getKeyCode() == 1) {
            switch (this.dragActive) {
                case 2: {
                    this.cancelDragging();
                    break;
                }
                case 1: {
                    this.columnHeaderDragged(this.dragStartColWidth);
                    this.dragActive = 3;
                    break;
                }
            }
            this.setMouseCursor(this.normalCursor);
        }
        return true;
    }
    
    void mouseLeftTableArea() {
        this.lastMouseY = Integer.MIN_VALUE;
        this.lastMouseRow = -1;
        this.lastMouseColumn = -1;
    }
    
    @Override
    Widget routeMouseEvent(final Event evt) {
        if (evt.getType() == Event.Type.MOUSE_EXITED) {
            this.mouseLeftTableArea();
        }
        else {
            this.lastMouseY = evt.getMouseY();
        }
        if (this.dragActive == 0) {
            final boolean inHeader = this.isMouseInColumnHeader(evt.getMouseY());
            if (inHeader) {
                if (this.lastMouseRow != -1 || this.lastMouseColumn != -1) {
                    this.lastMouseRow = -1;
                    this.lastMouseColumn = -1;
                    this.resetTooltip();
                }
            }
            else {
                final int row = this.getRowUnderMouse(evt.getMouseY());
                final int column = this.getColumnUnderMouse(evt.getMouseX());
                if (this.lastMouseRow != row || this.lastMouseColumn != column) {
                    this.lastMouseRow = row;
                    this.lastMouseColumn = column;
                    this.resetTooltip();
                }
            }
        }
        return super.routeMouseEvent(evt);
    }
    
    protected boolean handleMouseEvent(final Event evt) {
        final Event.Type evtType = evt.getType();
        if (this.dragActive != 0) {
            switch (this.dragActive) {
                case 1: {
                    final int innerWidth = this.getInnerWidth();
                    if (this.dragColumn >= 0 && innerWidth > 0) {
                        final int newWidth = this.clampColumnWidth(evt.getMouseX() - this.dragStartX);
                        this.columnHeaderDragged(newWidth);
                        break;
                    }
                    break;
                }
                case 2: {
                    this.setMouseCursor(this.dragListener.dragged(evt));
                    if (evt.isMouseDragEnd()) {
                        this.dragListener.dragStopped(evt);
                        break;
                    }
                    break;
                }
                case 3: {
                    break;
                }
                default: {
                    throw new AssertionError();
                }
            }
            if (evt.isMouseDragEnd()) {
                this.dragActive = 0;
                this.setMouseCursor(this.normalCursor);
            }
            return true;
        }
        final boolean inHeader = this.isMouseInColumnHeader(evt.getMouseY());
        if (inHeader) {
            final int column = this.getColumnSeparatorUnderMouse(evt.getMouseX());
            final boolean fixedWidthMode = this.isFixedWidthMode();
            if (column >= 0 && (column < this.getNumColumns() - 1 || !fixedWidthMode)) {
                this.setMouseCursor(this.columnResizeCursor);
                if (evtType == Event.Type.MOUSE_BTNDOWN) {
                    this.dragStartColWidth = this.getColumnWidth(column);
                    this.dragColumn = column;
                    this.dragStartX = evt.getMouseX() - this.dragStartColWidth;
                    if (fixedWidthMode) {
                        for (int i = 0; i < this.numColumns; ++i) {
                            this.columnHeaders[i].setColumnWidth(this.getColumnWidth(i));
                        }
                        this.dragStartSumWidth = this.dragStartColWidth + this.getColumnWidth(column + 1);
                    }
                }
                if (evt.isMouseDragEvent()) {
                    this.dragActive = 1;
                }
                return true;
            }
        }
        else {
            final int row = this.lastMouseRow;
            final int column2 = this.lastMouseColumn;
            if (evt.isMouseDragEvent()) {
                if (this.dragListener != null && this.dragListener.dragStarted(row, row, evt)) {
                    this.setMouseCursor(this.dragListener.dragged(evt));
                    this.dragActive = 2;
                }
                else {
                    this.dragActive = 3;
                    this.setMouseCursor(this.dragNotPossibleCursor);
                }
                return true;
            }
            if (this.selectionManager != null) {
                this.selectionManager.handleMouseEvent(row, column2, evt);
            }
            if (evtType == Event.Type.MOUSE_CLICKED && evt.getMouseClickCount() == 2 && this.callbacks != null) {
                for (final Callback cb : this.callbacks) {
                    cb.mouseDoubleClicked(row, column2);
                }
            }
            if (evtType == Event.Type.MOUSE_BTNUP && evt.getMouseButton() == 1 && this.callbacks != null) {
                for (final Callback cb : this.callbacks) {
                    cb.mouseRightClick(row, column2, evt);
                }
            }
        }
        this.setMouseCursor(this.normalCursor);
        return evtType != Event.Type.MOUSE_WHEEL;
    }
    
    private void columnHeaderDragged(int newWidth) {
        if (this.isFixedWidthMode()) {
            assert this.dragColumn + 1 < this.numColumns;
            newWidth = Math.min(newWidth, this.dragStartSumWidth - 2 * this.columnDividerDragableDistance);
            this.columnHeaders[this.dragColumn].setColumnWidth(newWidth);
            this.columnHeaders[this.dragColumn + 1].setColumnWidth(this.dragStartSumWidth - newWidth);
            this.updateAllColumnWidth = true;
            this.invalidateLayout();
        }
        else {
            this.setColumnWidth(this.dragColumn, newWidth);
        }
    }
    
    protected void columnHeaderClicked(final int column) {
        if (this.callbacks != null) {
            for (final Callback cb : this.callbacks) {
                cb.columnHeaderClicked(column);
            }
        }
    }
    
    protected void updateAllColumnWidth() {
        if (this.getInnerWidth() > 0) {
            this.columnModel.initializeAll(this.numColumns);
            this.updateAllColumnWidth = false;
        }
    }
    
    protected void updateAll() {
        if (!this.widgetGrid.isEmpty()) {
            this.removeAllCellWidgets();
            this.widgetGrid.clear();
        }
        if (this.rowModel != null) {
            this.autoSizeAllRows = true;
        }
        this.updateAllCellWidgets = true;
        this.updateAllColumnWidth = true;
        this.invalidateLayout();
    }
    
    protected void modelAllChanged() {
        if (this.columnHeaders != null) {
            this.removeColumnHeaders(0, this.columnHeaders.length);
        }
        this.dropMarkerRow = -1;
        this.columnHeaders = new ColumnHeader[this.numColumns];
        for (int i = 0; i < this.numColumns; ++i) {
            this.columnHeaders[i] = this.createColumnHeader(i);
            this.updateColumnHeader(i);
        }
        this.updateColumnHeaderNumbers();
        if (this.selectionManager != null) {
            this.selectionManager.modelChanged();
        }
        this.updateAll();
    }
    
    protected void modelRowChanged(final int row) {
        if (this.rowModel != null && this.autoSizeRow(row)) {
            this.invalidateLayout();
        }
        for (int col = 0; col < this.numColumns; ++col) {
            this.updateCellWidget(row, col);
        }
        this.invalidateLayoutLocally();
    }
    
    protected void modelRowsChanged(final int idx, final int count) {
        this.checkRowRange(idx, count);
        boolean rowHeightChanged = false;
        for (int i = 0; i < count; ++i) {
            if (this.rowModel != null) {
                rowHeightChanged |= this.autoSizeRow(idx + i);
            }
            for (int col = 0; col < this.numColumns; ++col) {
                this.updateCellWidget(idx + i, col);
            }
        }
        this.invalidateLayoutLocally();
        if (rowHeightChanged) {
            this.invalidateLayout();
        }
    }
    
    protected void modelCellChanged(final int row, final int column) {
        this.checkRowIndex(row);
        this.checkColumnIndex(column);
        if (this.rowModel != null) {
            this.autoSizeRow(row);
        }
        this.updateCellWidget(row, column);
        this.invalidateLayout();
    }
    
    protected void modelRowsInserted(final int row, final int count) {
        this.checkRowRange(row, count);
        if (this.rowModel != null) {
            this.rowModel.insert(row, count);
        }
        if (this.dropMarkerRow > row || (this.dropMarkerRow == row && this.dropMarkerBeforeRow)) {
            this.dropMarkerRow += count;
        }
        if (!this.widgetGrid.isEmpty() || this.hasCellWidgetCreators) {
            this.removeAllCellWidgets();
            this.widgetGrid.insertRows(row, count);
            for (int i = 0; i < count; ++i) {
                for (int col = 0; col < this.numColumns; ++col) {
                    this.updateCellWidget(row + i, col);
                }
            }
        }
        this.invalidateLayout();
        if (row < this.getRowFromPosition(this.scrollPosY)) {
            final ScrollPane sp = ScrollPane.getContainingScrollPane((Widget)this);
            if (sp != null) {
                final int rowsStart = this.getRowStartPosition(row);
                final int rowsEnd = this.getRowEndPosition(row + count - 1);
                sp.setScrollPositionY(this.scrollPosY + rowsEnd - rowsStart);
            }
        }
        if (this.selectionManager != null) {
            this.selectionManager.rowsInserted(row, count);
        }
    }
    
    protected void modelRowsDeleted(final int row, final int count) {
        if (row + count <= this.getRowFromPosition(this.scrollPosY)) {
            final ScrollPane sp = ScrollPane.getContainingScrollPane((Widget)this);
            if (sp != null) {
                final int rowsStart = this.getRowStartPosition(row);
                final int rowsEnd = this.getRowEndPosition(row + count - 1);
                sp.setScrollPositionY(this.scrollPosY - rowsEnd + rowsStart);
            }
        }
        if (this.rowModel != null) {
            this.rowModel.remove(row, count);
        }
        if (this.dropMarkerRow >= row) {
            if (this.dropMarkerRow < row + count) {
                this.dropMarkerRow = -1;
            }
            else {
                this.dropMarkerRow -= count;
            }
        }
        if (!this.widgetGrid.isEmpty()) {
            this.widgetGrid.iterate(row, 0, row + count - 1, this.numColumns, this.removeCellWidgetsFunction);
            this.widgetGrid.removeRows(row, count);
        }
        if (this.selectionManager != null) {
            this.selectionManager.rowsDeleted(row, count);
        }
        this.invalidateLayout();
    }
    
    protected void modelColumnsInserted(final int column, final int count) {
        this.checkColumnRange(column, count);
        final ColumnHeader[] newColumnHeaders = new ColumnHeader[this.numColumns];
        System.arraycopy(this.columnHeaders, 0, newColumnHeaders, 0, column);
        System.arraycopy(this.columnHeaders, column, newColumnHeaders, column + count, this.numColumns - (column + count));
        for (int i = 0; i < count; ++i) {
            newColumnHeaders[column + i] = this.createColumnHeader(column + i);
        }
        this.columnHeaders = newColumnHeaders;
        this.updateColumnHeaderNumbers();
        this.columnModel.insert(column, count);
        if (!this.widgetGrid.isEmpty() || this.hasCellWidgetCreators) {
            this.removeAllCellWidgets();
            this.widgetGrid.insertColumns(column, count);
            for (int row = 0; row < this.numRows; ++row) {
                for (int j = 0; j < count; ++j) {
                    this.updateCellWidget(row, column + j);
                }
            }
        }
        if (column < this.getColumnStartPosition(this.scrollPosX)) {
            final ScrollPane sp = ScrollPane.getContainingScrollPane((Widget)this);
            if (sp != null) {
                final int columnsStart = this.getColumnStartPosition(column);
                final int columnsEnd = this.getColumnEndPosition(column + count - 1);
                sp.setScrollPositionX(this.scrollPosX + columnsEnd - columnsStart);
            }
        }
        this.invalidateLayout();
    }
    
    protected void modelColumnsDeleted(final int column, final int count) {
        if (column + count <= this.getColumnStartPosition(this.scrollPosX)) {
            final ScrollPane sp = ScrollPane.getContainingScrollPane((Widget)this);
            if (sp != null) {
                final int columnsStart = this.getColumnStartPosition(column);
                final int columnsEnd = this.getColumnEndPosition(column + count - 1);
                sp.setScrollPositionY(this.scrollPosX - columnsEnd + columnsStart);
            }
        }
        this.columnModel.remove(column, count);
        if (!this.widgetGrid.isEmpty()) {
            this.widgetGrid.iterate(0, column, this.numRows, column + count - 1, this.removeCellWidgetsFunction);
            this.widgetGrid.removeColumns(column, count);
        }
        this.removeColumnHeaders(column, count);
        final ColumnHeader[] newColumnHeaders = new ColumnHeader[this.numColumns];
        System.arraycopy(this.columnHeaders, 0, newColumnHeaders, 0, column);
        System.arraycopy(this.columnHeaders, column + count, newColumnHeaders, column, this.numColumns - count);
        this.columnHeaders = newColumnHeaders;
        this.updateColumnHeaderNumbers();
        this.invalidateLayout();
    }
    
    protected void modelColumnHeaderChanged(final int column) {
        this.checkColumnIndex(column);
        this.updateColumnHeader(column);
    }
    
    static {
        STATE_FIRST_COLUMNHEADER = AnimationState.StateKey.get("firstColumnHeader");
        STATE_LAST_COLUMNHEADER = AnimationState.StateKey.get("lastColumnHeader");
        STATE_ROW_SELECTED = AnimationState.StateKey.get("rowSelected");
        STATE_ROW_HOVER = AnimationState.StateKey.get("rowHover");
        STATE_ROW_DROPTARGET = AnimationState.StateKey.get("rowDropTarget");
        STATE_ROW_ODD = AnimationState.StateKey.get("rowOdd");
        STATE_LEAD_ROW = AnimationState.StateKey.get("leadRow");
        STATE_SELECTED = AnimationState.StateKey.get("selected");
        STATE_SORT_ASCENDING = AnimationState.StateKey.get("sortAscending");
        STATE_SORT_DESCENDING = AnimationState.StateKey.get("sortDescending");
    }
    
    class RowSizeSequence extends SizeSequence
    {
        public RowSizeSequence(final int initialCapacity) {
            super(initialCapacity);
        }
        
        @Override
        protected void initializeSizes(int index, final int count) {
            for (int i = 0; i < count; ++i, ++index) {
                this.table[index] = TableBase.this.computeRowHeight(index);
            }
        }
    }
    
    protected class ColumnSizeSequence extends SizeSequence
    {
        @Override
        protected void initializeSizes(final int index, final int count) {
            boolean useSprings = TableBase.this.isFixedWidthMode();
            if (!useSprings) {
                int sum = 0;
                for (int i = 0; i < count; ++i) {
                    final int width = TableBase.this.computePreferredColumnWidth(index + i);
                    this.table[index + i] = width;
                    sum += width;
                }
                useSprings = (sum < TableBase.this.getInnerWidth());
            }
            if (useSprings) {
                this.computeColumnHeaderLayout();
                for (int j = 0; j < count; ++j) {
                    this.table[index + j] = TableBase.this.clampColumnWidth(TableBase.this.columnHeaders[j].springWidth);
                }
            }
        }
        
        protected boolean update(final int index) {
            int width;
            if (TableBase.this.isFixedWidthMode()) {
                this.computeColumnHeaderLayout();
                width = TableBase.this.clampColumnWidth(TableBase.this.columnHeaders[index].springWidth);
            }
            else {
                width = TableBase.this.computePreferredColumnWidth(index);
                if (TableBase.this.ensureColumnHeaderMinWidth) {
                    width = Math.max(width, TableBase.this.columnHeaders[index].getMinWidth());
                }
            }
            return this.setSize(index, width);
        }
        
        void computeColumnHeaderLayout() {
            if (TableBase.this.columnHeaders != null) {
                final DialogLayout.SequentialGroup g = (DialogLayout.SequentialGroup)new DialogLayout().createSequentialGroup();
                for (final ColumnHeader h : TableBase.this.columnHeaders) {
                    g.addSpring(h.spring);
                }
                g.setSize(0, 0, TableBase.this.getInnerWidth());
            }
        }
        
        int computePreferredWidth() {
            final int count = TableBase.this.getNumColumns();
            if (!TableBase.this.isFixedWidthMode()) {
                int sum = 0;
                for (int i = 0; i < count; ++i) {
                    final int width = TableBase.this.computePreferredColumnWidth(i);
                    sum += width;
                }
                return sum;
            }
            if (TableBase.this.columnHeaders != null) {
                final DialogLayout.SequentialGroup g = (DialogLayout.SequentialGroup)new DialogLayout().createSequentialGroup();
                for (final ColumnHeader h : TableBase.this.columnHeaders) {
                    g.addSpring(h.spring);
                }
                return g.getPrefSize(0);
            }
            return 0;
        }
    }
    
    class RemoveCellWidgets implements SparseGrid.GridFunction
    {
        @Override
        public void apply(final int row, final int column, final SparseGrid.Entry e) {
            final WidgetEntry widgetEntry = (WidgetEntry)e;
            final Widget widget = widgetEntry.widget;
            if (widget != null) {
                TableBase.this.removeCellWidget(widget);
            }
        }
    }
    
    class InsertCellWidgets implements SparseGrid.GridFunction
    {
        @Override
        public void apply(final int row, final int column, final SparseGrid.Entry e) {
            TableBase.this.insertCellWidget(row, column, (WidgetEntry)e);
        }
    }
    
    protected class ColumnHeader extends Button implements Runnable
    {
        int column;
        private int columnWidth;
        int springWidth;
        final DialogLayout.Spring spring;
        
        public ColumnHeader() {
            this.spring = new DialogLayout.Spring() {
                int getMinSize(final int axis) {
                    return TableBase.this.clampColumnWidth(ColumnHeader.this.getMinWidth());
                }
                
                int getPrefSize(final int axis) {
                    return ColumnHeader.this.getPreferredWidth();
                }
                
                int getMaxSize(final int axis) {
                    return ColumnHeader.this.getMaxWidth();
                }
                
                void setSize(final int axis, final int pos, final int size) {
                    ColumnHeader.this.springWidth = size;
                }
            };
            this.addCallback((Runnable)this);
        }
        
        public int getColumnWidth() {
            return this.columnWidth;
        }
        
        public void setColumnWidth(final int columnWidth) {
            this.columnWidth = columnWidth;
        }
        
        public int getPreferredWidth() {
            if (this.columnWidth > 0) {
                return this.columnWidth;
            }
            final DialogLayout.Gap mpm = TableBase.this.getColumnMPM(this.column);
            final int prefWidth = (mpm != null) ? mpm.preferred : TableBase.this.defaultColumnWidth;
            return Math.max(prefWidth, super.getPreferredWidth());
        }
        
        public int getMinWidth() {
            final DialogLayout.Gap mpm = TableBase.this.getColumnMPM(this.column);
            final int minWidth = (mpm != null) ? mpm.min : 0;
            return Math.max(minWidth, super.getPreferredWidth());
        }
        
        public int getMaxWidth() {
            final DialogLayout.Gap mpm = TableBase.this.getColumnMPM(this.column);
            final int maxWidth = (mpm != null) ? mpm.max : 32767;
            return maxWidth;
        }
        
        public void adjustSize() {
        }
        
        protected boolean handleEvent(final Event evt) {
            if (evt.isMouseEventNoWheel()) {
                TableBase.this.mouseLeftTableArea();
            }
            return super.handleEvent(evt);
        }
        
        protected void paintWidget(final GUI gui) {
            final Renderer renderer = gui.getRenderer();
            renderer.clipEnter(this.getX(), this.getY(), this.getWidth(), this.getHeight());
            try {
                this.paintLabelText((AnimationState)this.getAnimationState());
            }
            finally {
                renderer.clipLeave();
            }
        }
        
        public void run() {
            TableBase.this.columnHeaderClicked(this.column);
        }
    }
    
    static class WidgetEntry extends SparseGrid.Entry
    {
        Widget widget;
        CellWidgetCreator creator;
    }
    
    static class CellWidgetContainer extends Widget
    {
        CellWidgetContainer() {
            this.setTheme("");
            this.setClip(true);
        }
        
        @Override
        protected void childInvalidateLayout(final Widget child) {
        }
        
        @Override
        protected void sizeChanged() {
        }
        
        @Override
        protected void childAdded(final Widget child) {
        }
        
        @Override
        protected void childRemoved(final Widget exChild) {
        }
        
        @Override
        protected void allChildrenRemoved() {
        }
    }
    
    public static class StringCellRenderer extends TextWidget implements CellRenderer
    {
        public StringCellRenderer() {
            this.setCache(false);
            this.setClip(true);
        }
        
        @Override
        public void applyTheme(final ThemeInfo themeInfo) {
            super.applyTheme(themeInfo);
        }
        
        @Override
        public void setCellData(final int row, final int column, final Object data) {
            this.setCharSequence(String.valueOf(data));
        }
        
        @Override
        public int getColumnSpan() {
            return 1;
        }
        
        @Override
        protected void sizeChanged() {
        }
        
        @Override
        public Widget getCellRenderWidget(final int x, final int y, final int width, final int height, final boolean isSelected) {
            this.setPosition(x, y);
            this.setSize(width, height);
            this.getAnimationState().setAnimationState(TableBase.STATE_SELECTED, isSelected);
            return this;
        }
    }
    
    public interface CellRenderer
    {
        void applyTheme(final ThemeInfo p0);
        
        String getTheme();
        
        void setCellData(final int p0, final int p1, final Object p2);
        
        int getColumnSpan();
        
        int getPreferredHeight();
        
        Widget getCellRenderWidget(final int p0, final int p1, final int p2, final int p3, final boolean p4);
    }
    
    public interface CellWidgetCreator extends CellRenderer
    {
        Widget updateWidget(final Widget p0);
        
        void positionWidget(final Widget p0, final int p1, final int p2, final int p3, final int p4);
    }
    
    public interface DragListener
    {
        boolean dragStarted(final int p0, final int p1, final Event p2);
        
        MouseCursor dragged(final Event p0);
        
        void dragStopped(final Event p0);
        
        void dragCanceled();
    }
    
    public interface KeyboardSearchHandler
    {
        boolean handleKeyEvent(final Event p0);
        
        boolean isActive();
        
        void updateInfoWindowPosition();
    }
    
    public interface Callback
    {
        void mouseDoubleClicked(final int p0, final int p1);
        
        void mouseRightClick(final int p0, final int p1, final Event p2);
        
        void columnHeaderClicked(final int p0);
    }
}
