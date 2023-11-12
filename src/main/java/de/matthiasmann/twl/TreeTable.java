package de.matthiasmann.twl;

import de.matthiasmann.twl.utils.*;
import de.matthiasmann.twl.model.*;

public class TreeTable extends TableBase
{
    private final ModelChangeListener modelChangeListener;
    private final TreeLeafCellRenderer leafRenderer;
    private final TreeNodeCellRenderer nodeRenderer;
    private NodeState[] nodeStateTable;
    private int nodeStateTableSize;
    TreeTableModel model;
    private NodeState rootNodeState;
    private ExpandListener[] expandListeners;

    public TreeTable() {
        this.modelChangeListener = new ModelChangeListener();
        this.nodeStateTable = new NodeState[64];
        this.leafRenderer = new TreeLeafCellRenderer();
        this.nodeRenderer = new TreeNodeCellRenderer();
        this.hasCellWidgetCreators = true;
        final ActionMap am = this.getOrCreateActionMap();
        am.addMapping("expandLeadRow", (Object)this, "setLeadRowExpanded", new Object[] { Boolean.TRUE }, 1);
        am.addMapping("collapseLeadRow", (Object)this, "setLeadRowExpanded", new Object[] { Boolean.FALSE }, 1);
    }

    public TreeTable(final TreeTableModel model) {
        this();
        this.setModel(model);
    }

    public void setModel(final TreeTableModel model) {
        if (this.model != null) {
            this.model.removeChangeListener((TreeTableModel.ChangeListener)this.modelChangeListener);
        }
        this.columnHeaderModel = (TableColumnHeaderModel)model;
        this.model = model;
        this.nodeStateTable = new NodeState[64];
        this.nodeStateTableSize = 0;
        if (this.model != null) {
            this.model.addChangeListener((TreeTableModel.ChangeListener)this.modelChangeListener);
            this.rootNodeState = this.createNodeState((TreeTableNode)model);
            this.rootNodeState.level = -1;
            this.rootNodeState.expanded = true;
            this.rootNodeState.initChildSizes();
            this.numRows = this.computeNumRows();
            this.numColumns = model.getNumColumns();
        }
        else {
            this.rootNodeState = null;
            this.numRows = 0;
            this.numColumns = 0;
        }
        this.modelAllChanged();
        this.invalidateLayout();
    }

    public void addExpandListener(final ExpandListener listener) {
        this.expandListeners = CallbackSupport.addCallbackToList(this.expandListeners, listener, ExpandListener.class);
    }

    public void removeExpandListener(final ExpandListener listener) {
        this.expandListeners = CallbackSupport.removeCallbackFromList(this.expandListeners, listener);
    }

    protected void applyTheme(final ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        this.applyThemeTreeTable(themeInfo);
    }

    protected void applyThemeTreeTable(final ThemeInfo themeInfo) {
        this.applyCellRendererTheme((TableBase.CellRenderer)this.leafRenderer);
        this.applyCellRendererTheme((TableBase.CellRenderer)this.nodeRenderer);
    }

    public int getRowFromNode(TreeTableNode node) {
        int position = -1;
        for (TreeTableNode parent = node.getParent(); parent != null; parent = node.getParent()) {
            final NodeState ns = HashEntry.get(this.nodeStateTable, parent);
            if (ns == null) {
                return -1;
            }
            int idx = parent.getChildIndex(node);
            if (idx < 0) {
                return -1;
            }
            if (ns.childSizes == null) {
                if (!ns.expanded) {
                    return -1;
                }
                ns.initChildSizes();
            }
            idx = ns.childSizes.getPosition(idx);
            position += idx + 1;
            node = parent;
        }
        return position;
    }

    public int getRowFromNodeExpand(final TreeTableNode node) {
        if (node.getParent() != null) {
            final TreeTableNode parent = node.getParent();
            final int row = this.getRowFromNodeExpand(parent);
            final int idx = parent.getChildIndex(node);
            final NodeState ns = HashEntry.get(this.nodeStateTable, parent);
            ns.setValue(true);
            if (ns.childSizes == null) {
                ns.initChildSizes();
            }
            return row + 1 + ns.childSizes.getPosition(idx);
        }
        return -1;
    }

    public TreeTableNode getNodeFromRow(int row) {
        NodeState ns = this.rootNodeState;
        while (true) {
            int idx;
            if (ns.childSizes == null) {
                idx = Math.min(((TreeTableNode)ns.key).getNumChildren() - 1, row);
                row -= idx + 1;
            }
            else {
                idx = ns.childSizes.getIndex(row);
                row -= ns.childSizes.getPosition(idx) + 1;
            }
            if (row < 0) {
                return ((TreeTableNode)ns.key).getChild(idx);
            }
            assert ns.children[idx] != null;
            ns = ns.children[idx];
        }
    }

    public void collapseAll() {
        for (int i = 0; i < this.nodeStateTable.length; ++i) {
            for (NodeState ns = this.nodeStateTable[i]; ns != null; ns = ns.next()) {
                if (ns != this.rootNodeState) {
                    ns.setValue(false);
                }
            }
        }
    }

    public boolean isRowExpanded(final int row) {
        this.checkRowIndex(row);
        final TreeTableNode node = this.getNodeFromRow(row);
        final NodeState ns = HashEntry.get(this.nodeStateTable, node);
        return ns != null && ns.expanded;
    }

    public void setRowExpanded(final int row, final boolean expanded) {
        this.checkRowIndex(row);
        final TreeTableNode node = this.getNodeFromRow(row);
        final NodeState state = this.getOrCreateNodeState(node);
        state.setValue(expanded);
    }

    public void setLeadRowExpanded(final boolean expanded) {
        final TableSelectionManager sm = this.getSelectionManager();
        if (sm != null) {
            final int row = sm.getLeadRow();
            if (row >= 0 && row < this.numRows) {
                this.setRowExpanded(row, expanded);
            }
        }
    }

    protected NodeState getOrCreateNodeState(final TreeTableNode node) {
        NodeState ns = HashEntry.get(this.nodeStateTable, node);
        if (ns == null) {
            ns = this.createNodeState(node);
        }
        return ns;
    }

    protected NodeState createNodeState(final TreeTableNode node) {
        final TreeTableNode parent = node.getParent();
        NodeState nsParent = null;
        if (parent != null) {
            nsParent = HashEntry.get(this.nodeStateTable, parent);
            assert nsParent != null;
        }
        final NodeState newNS = new NodeState(node, nsParent);
        HashEntry.insertEntry(this.nodeStateTable = HashEntry.maybeResizeTable(this.nodeStateTable, ++this.nodeStateTableSize), newNS);
        return newNS;
    }

    protected void expandedChanged(final NodeState ns) {
        TreeTableNode node = (TreeTableNode)ns.key;
        final int count = ns.getChildRows();
        int size = ns.expanded ? count : 0;
        for (TreeTableNode parent = node.getParent(); parent != null; parent = node.getParent()) {
            final NodeState nsParent = HashEntry.get(this.nodeStateTable, parent);
            if (nsParent.childSizes == null) {
                nsParent.initChildSizes();
            }
            final int idx = ((TreeTableNode)nsParent.key).getChildIndex(node);
            nsParent.childSizes.setSize(idx, size + 1);
            size = nsParent.childSizes.getEndPosition();
            node = parent;
        }
        this.numRows = this.computeNumRows();
        final int row = this.getRowFromNode((TreeTableNode)ns.key);
        if (ns.expanded) {
            this.modelRowsInserted(row + 1, count);
        }
        else {
            this.modelRowsDeleted(row + 1, count);
        }
        this.modelRowsChanged(row, 1);
        if (ns.expanded) {
            final ScrollPane scrollPane = ScrollPane.getContainingScrollPane((Widget)this);
            if (scrollPane != null) {
                scrollPane.validateLayout();
                final int rowStart = this.getRowStartPosition(row);
                final int rowEnd = this.getRowEndPosition(row + count);
                final int height = rowEnd - rowStart;
                scrollPane.scrollToAreaY(rowStart, height, this.rowHeight / 2);
            }
        }
        if (this.expandListeners != null) {
            for (final ExpandListener el : this.expandListeners) {
                if (ns.expanded) {
                    el.nodeExpanded(row, (TreeTableNode)ns.key);
                }
                else {
                    el.nodeCollapsed(row, (TreeTableNode)ns.key);
                }
            }
        }
    }

    protected int computeNumRows() {
        return this.rootNodeState.childSizes.getEndPosition();
    }

    protected Object getCellData(final int row, final int column, TreeTableNode node) {
        if (node == null) {
            node = this.getNodeFromRow(row);
        }
        return node.getData(column);
    }

    protected TableBase.CellRenderer getCellRenderer(final int row, final int col, TreeTableNode node) {
        if (node == null) {
            node = this.getNodeFromRow(row);
        }
        if (col != 0) {
            return super.getCellRenderer(row, col, node);
        }
        final Object data = node.getData(col);
        if (node.isLeaf()) {
            this.leafRenderer.setCellData(row, col, data, node);
            return (TableBase.CellRenderer)this.leafRenderer;
        }
        final NodeState nodeState = this.getOrCreateNodeState(node);
        this.nodeRenderer.setCellData(row, col, data, nodeState);
        return (TableBase.CellRenderer)this.nodeRenderer;
    }

    protected Object getTooltipContentFromRow(final int row, final int column) {
        final TreeTableNode node = this.getNodeFromRow(row);
        if (node != null) {
            return node.getTooltipContent(column);
        }
        return null;
    }

    private boolean updateParentSizes(NodeState ns) {
        while (ns.expanded && ns.parent != null) {
            final NodeState parent = ns.parent;
            final int idx = ((TreeTableNode)parent.key).getChildIndex((TreeTableNode)ns.key);
            assert parent.childSizes.size() == ((TreeTableNode)parent.key).getNumChildren();
            parent.childSizes.setSize(idx, ns.getChildRows() + 1);
            ns = parent;
        }
        this.numRows = this.computeNumRows();
        return ns.parent == null;
    }

    protected void modelNodesAdded(final TreeTableNode parent, final int idx, final int count) {
        final NodeState ns = HashEntry.get(this.nodeStateTable, parent);
        if (ns != null) {
            if (ns.childSizes != null) {
                assert idx <= ns.childSizes.size();
                ns.childSizes.insert(idx, count);
                assert ns.childSizes.size() == parent.getNumChildren();
            }
            if (ns.children != null) {
                final NodeState[] newChilds = new NodeState[parent.getNumChildren()];
                System.arraycopy(ns.children, 0, newChilds, 0, idx);
                System.arraycopy(ns.children, idx, newChilds, idx + count, ns.children.length - idx);
                ns.children = newChilds;
            }
            if (this.updateParentSizes(ns)) {
                final int row = this.getRowFromNode(parent.getChild(idx));
                assert row < this.numRows;
                this.modelRowsInserted(row, count);
            }
        }
    }

    protected void recursiveRemove(final NodeState ns) {
        if (ns != null) {
            --this.nodeStateTableSize;
            HashEntry.remove(this.nodeStateTable, ns);
            if (ns.children != null) {
                for (final NodeState nsChild : ns.children) {
                    this.recursiveRemove(nsChild);
                }
            }
        }
    }

    protected void modelNodesRemoved(final TreeTableNode parent, final int idx, final int count) {
        final NodeState ns = HashEntry.get(this.nodeStateTable, parent);
        if (ns != null) {
            final int rowsBase = this.getRowFromNode(parent) + 1;
            int rowsStart = rowsBase + idx;
            int rowsEnd = rowsBase + idx + count;
            if (ns.childSizes != null) {
                assert ns.childSizes.size() == parent.getNumChildren() + count;
                rowsStart = rowsBase + ns.childSizes.getPosition(idx);
                rowsEnd = rowsBase + ns.childSizes.getPosition(idx + count);
                ns.childSizes.remove(idx, count);
                assert ns.childSizes.size() == parent.getNumChildren();
            }
            if (ns.children != null) {
                for (int i = 0; i < count; ++i) {
                    this.recursiveRemove(ns.children[idx + i]);
                }
                final int numChildren = parent.getNumChildren();
                if (numChildren > 0) {
                    final NodeState[] newChilds = new NodeState[numChildren];
                    System.arraycopy(ns.children, 0, newChilds, 0, idx);
                    System.arraycopy(ns.children, idx + count, newChilds, idx, newChilds.length - idx);
                    ns.children = newChilds;
                }
                else {
                    ns.children = null;
                }
            }
            if (this.updateParentSizes(ns)) {
                this.modelRowsDeleted(rowsStart, rowsEnd - rowsStart);
            }
        }
    }

    protected boolean isVisible(NodeState ns) {
        while (ns.expanded && ns.parent != null) {
            ns = ns.parent;
        }
        return ns.expanded;
    }

    protected void modelNodesChanged(final TreeTableNode parent, final int idx, final int count) {
        final NodeState ns = HashEntry.get(this.nodeStateTable, parent);
        if (ns != null && this.isVisible(ns)) {
            final int rowsBase = this.getRowFromNode(parent) + 1;
            int rowsStart = rowsBase + idx;
            int rowsEnd = rowsBase + idx + count;
            if (ns.childSizes != null) {
                rowsStart = rowsBase + ns.childSizes.getPosition(idx);
                rowsEnd = rowsBase + ns.childSizes.getPosition(idx + count);
            }
            this.modelRowsChanged(rowsStart, rowsEnd - rowsStart);
        }
    }

    static int getLevel(TreeTableNode node) {
        int level = -2;
        while (node != null) {
            ++level;
            node = node.getParent();
        }
        return level;
    }

    protected class ModelChangeListener implements TreeTableModel.ChangeListener
    {
        public void nodesAdded(final TreeTableNode parent, final int idx, final int count) {
            TreeTable.this.modelNodesAdded(parent, idx, count);
        }

        public void nodesRemoved(final TreeTableNode parent, final int idx, final int count) {
            TreeTable.this.modelNodesRemoved(parent, idx, count);
        }

        public void nodesChanged(final TreeTableNode parent, final int idx, final int count) {
            TreeTable.this.modelNodesChanged(parent, idx, count);
        }

        public void columnInserted(final int idx, final int count) {
            TreeTable.this.numColumns = TreeTable.this.model.getNumColumns();
            TreeTable.this.modelColumnsInserted(idx, count);
        }

        public void columnDeleted(final int idx, final int count) {
            TreeTable.this.numColumns = TreeTable.this.model.getNumColumns();
            TreeTable.this.modelColumnsDeleted(idx, count);
        }

        public void columnHeaderChanged(final int column) {
            TreeTable.this.modelColumnHeaderChanged(column);
        }
    }

    protected class NodeState extends HashEntry<TreeTableNode, NodeState> implements BooleanModel
    {
        final NodeState parent;
        boolean expanded;
        boolean hasNoChildren;
        SizeSequence childSizes;
        NodeState[] children;
        Runnable[] callbacks;
        int level;

        public NodeState(final TreeTableNode key, final NodeState parent) {
            super(key);
            this.parent = parent;
            this.level = ((parent != null) ? (parent.level + 1) : 0);
            if (parent != null) {
                if (parent.children == null) {
                    parent.children = new NodeState[((TreeTableNode)parent.key).getNumChildren()];
                }
                parent.children[((TreeTableNode)parent.key).getChildIndex(key)] = this;
            }
        }

        public void addCallback(final Runnable callback) {
            this.callbacks = CallbackSupport.addCallbackToList(this.callbacks, callback, Runnable.class);
        }

        public void removeCallback(final Runnable callback) {
            this.callbacks = CallbackSupport.removeCallbackFromList(this.callbacks, callback);
        }

        public boolean getValue() {
            return this.expanded;
        }

        public void setValue(final boolean value) {
            if (this.expanded != value) {
                this.expanded = value;
                TreeTable.this.expandedChanged(this);
                CallbackSupport.fireCallbacks(this.callbacks);
            }
        }

        void initChildSizes() {
            (this.childSizes = new SizeSequence()).setDefaultValue(1);
            this.childSizes.initializeAll(((TreeTableNode)this.key).getNumChildren());
        }

        int getChildRows() {
            if (this.childSizes != null) {
                return this.childSizes.getEndPosition();
            }
            final int childCount = ((TreeTableNode)this.key).getNumChildren();
            this.hasNoChildren = (childCount == 0);
            return childCount;
        }

        boolean hasNoChildren() {
            return this.hasNoChildren;
        }
    }

    class TreeLeafCellRenderer implements TableBase.CellRenderer, TableBase.CellWidgetCreator
    {
        protected int treeIndent;
        protected int level;
        protected Dimension treeButtonSize;
        protected TableBase.CellRenderer subRenderer;

        public TreeLeafCellRenderer() {
            this.treeButtonSize = new Dimension(5, 5);
            TreeTable.this.setClip(true);
        }

        public void applyTheme(final ThemeInfo themeInfo) {
            this.treeIndent = themeInfo.getParameter("treeIndent", 10);
            this.treeButtonSize = (Dimension)themeInfo.getParameterValue("treeButtonSize", true, (Class)Dimension.class, (Object)Dimension.ZERO);
        }

        public String getTheme() {
            return this.getClass().getSimpleName();
        }

        public void setCellData(final int row, final int column, final Object data) {
            throw new UnsupportedOperationException("Don't call this method");
        }

        public void setCellData(final int row, final int column, final Object data, final TreeTableNode node) {
            this.level = TreeTable.getLevel(node);
            this.setSubRenderer(row, column, data);
        }

        protected int getIndentation() {
            return this.level * this.treeIndent + this.treeButtonSize.getX();
        }

        protected void setSubRenderer(final int row, final int column, final Object colData) {
            this.subRenderer = TreeTable.this.getCellRenderer(colData, column);
            if (this.subRenderer != null) {
                this.subRenderer.setCellData(row, column, colData);
            }
        }

        public int getColumnSpan() {
            return (this.subRenderer != null) ? this.subRenderer.getColumnSpan() : 1;
        }

        public int getPreferredHeight() {
            if (this.subRenderer != null) {
                return Math.max(this.treeButtonSize.getY(), this.subRenderer.getPreferredHeight());
            }
            return this.treeButtonSize.getY();
        }

        public Widget getCellRenderWidget(final int x, final int y, final int width, final int height, final boolean isSelected) {
            if (this.subRenderer != null) {
                final int indent = this.getIndentation();
                final Widget widget = this.subRenderer.getCellRenderWidget(x + indent, y, Math.max(0, width - indent), height, isSelected);
                return widget;
            }
            return null;
        }

        public Widget updateWidget(final Widget existingWidget) {
            if (this.subRenderer instanceof TableBase.CellWidgetCreator) {
                final TableBase.CellWidgetCreator subCreator = (TableBase.CellWidgetCreator)this.subRenderer;
                return subCreator.updateWidget(existingWidget);
            }
            return null;
        }

        public void positionWidget(final Widget widget, final int x, final int y, final int w, final int h) {
            if (this.subRenderer instanceof TableBase.CellWidgetCreator) {
                final TableBase.CellWidgetCreator subCreator = (TableBase.CellWidgetCreator)this.subRenderer;
                final int indent = this.level * this.treeIndent;
                subCreator.positionWidget(widget, x + indent, y, Math.max(0, w - indent), h);
            }
        }
    }

    static class WidgetChain extends Widget
    {
        final ToggleButton expandButton;
        Widget userWidget;

        WidgetChain() {
            this.setTheme("");
            (this.expandButton = new ToggleButton()).setTheme("treeButton");
            this.add((Widget)this.expandButton);
        }

        void setUserWidget(final Widget userWidget) {
            if (this.userWidget != userWidget) {
                if (this.userWidget != null) {
                    this.removeChild(1);
                }
                if ((this.userWidget = userWidget) != null) {
                    this.insertChild(userWidget, 1);
                }
            }
        }
    }

    class TreeNodeCellRenderer extends TreeLeafCellRenderer
    {
        private NodeState nodeState;

        @Override
        public Widget updateWidget(Widget existingWidget) {
            if (this.subRenderer instanceof TableBase.CellWidgetCreator) {
                final TableBase.CellWidgetCreator subCreator = (TableBase.CellWidgetCreator)this.subRenderer;
                WidgetChain widgetChain = null;
                if (existingWidget instanceof WidgetChain) {
                    widgetChain = (WidgetChain)existingWidget;
                }
                if (this.nodeState.hasNoChildren()) {
                    if (widgetChain != null) {
                        existingWidget = null;
                    }
                    return subCreator.updateWidget(existingWidget);
                }
                if (widgetChain == null) {
                    widgetChain = new WidgetChain();
                }
                widgetChain.expandButton.setModel((BooleanModel)this.nodeState);
                widgetChain.setUserWidget(subCreator.updateWidget(widgetChain.userWidget));
                return widgetChain;
            }
            else {
                if (this.nodeState.hasNoChildren()) {
                    return null;
                }
                ToggleButton tb = (ToggleButton)existingWidget;
                if (tb == null) {
                    tb = new ToggleButton();
                    tb.setTheme("treeButton");
                }
                tb.setModel((BooleanModel)this.nodeState);
                return (Widget)tb;
            }
        }

        @Override
        public void positionWidget(final Widget widget, final int x, final int y, final int w, final int h) {
            final int indent = this.level * this.treeIndent;
            final int availWidth = Math.max(0, w - indent);
            final int expandButtonWidth = Math.min(availWidth, this.treeButtonSize.getX());
            widget.setPosition(x + indent, y + (h - this.treeButtonSize.getY()) / 2);
            if (this.subRenderer instanceof TableBase.CellWidgetCreator) {
                final TableBase.CellWidgetCreator subCreator = (TableBase.CellWidgetCreator)this.subRenderer;
                final WidgetChain widgetChain = (WidgetChain)widget;
                final ToggleButton expandButton = widgetChain.expandButton;
                widgetChain.setSize(Math.max(0, w - indent), h);
                expandButton.setSize(expandButtonWidth, this.treeButtonSize.getY());
                if (widgetChain.userWidget != null) {
                    subCreator.positionWidget(widgetChain.userWidget, expandButton.getRight(), y, widget.getWidth(), h);
                }
            }
            else {
                widget.setSize(expandButtonWidth, this.treeButtonSize.getY());
            }
        }

        public void setCellData(final int row, final int column, final Object data, final NodeState nodeState) {
            assert nodeState != null;
            this.nodeState = nodeState;
            this.setSubRenderer(row, column, data);
            this.level = nodeState.level;
        }
    }

    public interface ExpandListener
    {
        void nodeExpanded(final int p0, final TreeTableNode p1);

        void nodeCollapsed(final int p0, final TreeTableNode p1);
    }
}
