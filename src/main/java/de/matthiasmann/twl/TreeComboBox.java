package de.matthiasmann.twl;

import de.matthiasmann.twl.model.*;
import de.matthiasmann.twl.utils.*;

public class TreeComboBox extends ComboBoxBase
{
    private static final String DEFAULT_POPUP_THEME = "treecomboboxPopup";
    final TableSingleSelectionModel selectionModel;
    final TreePathDisplay display;
    final TreeTable table;
    private TreeTableModel model;
    private Callback[] callbacks;
    private PathResolver pathResolver;
    private boolean suppressCallback;
    boolean suppressTreeSelectionUpdating;
    
    public TreeComboBox() {
        this.selectionModel = new TableSingleSelectionModel();
        (this.display = new TreePathDisplay()).setTheme("display");
        (this.table = new TreeTable()).setSelectionManager((TableSelectionManager)new TableRowSelectionManager(this.selectionModel) {
            protected boolean handleMouseClick(final int row, final int column, final boolean isShift, final boolean isCtrl) {
                if (!isShift && !isCtrl && row >= 0 && row < this.getNumRows()) {
                    TreeComboBox.this.popup.closePopup();
                    return true;
                }
                return super.handleMouseClick(row, column, isShift, isCtrl);
            }
        });
        this.display.addCallback(new TreePathDisplay.Callback() {
            @Override
            public void pathElementClicked(final TreeTableNode node, final TreeTableNode child) {
                TreeComboBox.this.fireSelectedNodeChanged(node, child);
            }
            
            @Override
            public boolean resolvePath(final String path) {
                return TreeComboBox.this.resolvePath(path);
            }
        });
        this.selectionModel.addSelectionChangeListener((Runnable)new Runnable() {
            @Override
            public void run() {
                final int row = TreeComboBox.this.selectionModel.getFirstSelected();
                if (row >= 0) {
                    TreeComboBox.this.suppressTreeSelectionUpdating = true;
                    try {
                        TreeComboBox.this.nodeChanged(TreeComboBox.this.table.getNodeFromRow(row));
                    }
                    finally {
                        TreeComboBox.this.suppressTreeSelectionUpdating = false;
                    }
                }
            }
        });
        final ScrollPane scrollPane = new ScrollPane((Widget)this.table);
        scrollPane.setFixed(ScrollPane.Fixed.HORIZONTAL);
        this.add((Widget)this.display);
        this.popup.setTheme("treecomboboxPopup");
        this.popup.add((Widget)scrollPane);
    }
    
    public TreeComboBox(final TreeTableModel model) {
        this();
        this.setModel(model);
    }
    
    public TreeTableModel getModel() {
        return this.model;
    }
    
    public void setModel(final TreeTableModel model) {
        if (this.model != model) {
            this.model = model;
            this.table.setModel(model);
            this.display.setCurrentNode((TreeTableNode)model);
        }
    }
    
    public void setCurrentNode(final TreeTableNode node) {
        if (node == null) {
            throw new NullPointerException("node");
        }
        this.display.setCurrentNode(node);
        if (this.popup.isOpen()) {
            this.tableSelectToCurrentNode();
        }
    }
    
    public TreeTableNode getCurrentNode() {
        return this.display.getCurrentNode();
    }
    
    public void setSeparator(final String separator) {
        this.display.setSeparator(separator);
    }
    
    public String getSeparator() {
        return this.display.getSeparator();
    }
    
    public PathResolver getPathResolver() {
        return this.pathResolver;
    }
    
    public void setPathResolver(final PathResolver pathResolver) {
        this.pathResolver = pathResolver;
        this.display.setAllowEdit(pathResolver != null);
    }
    
    public TreeTable getTreeTable() {
        return this.table;
    }
    
    public EditField getEditField() {
        return this.display.getEditField();
    }
    
    public void addCallback(final Callback callback) {
        this.callbacks = CallbackSupport.addCallbackToList(this.callbacks, callback, Callback.class);
    }
    
    public void removeCallback(final Callback callback) {
        this.callbacks = CallbackSupport.removeCallbackFromList(this.callbacks, callback);
    }
    
    protected void applyTheme(final ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        this.applyTreeComboboxPopupThemeName(themeInfo);
    }
    
    protected void applyTreeComboboxPopupThemeName(final ThemeInfo themeInfo) {
        this.popup.setTheme(themeInfo.getParameter("popupThemeName", "treecomboboxPopup"));
    }
    
    protected Widget getLabel() {
        return this.display;
    }
    
    void fireSelectedNodeChanged(final TreeTableNode node, final TreeTableNode child) {
        if (this.callbacks != null) {
            for (final Callback cb : this.callbacks) {
                cb.selectedNodeChanged(node, child);
            }
        }
    }
    
    boolean resolvePath(final String path) {
        if (this.pathResolver != null) {
            try {
                final TreeTableNode node = this.pathResolver.resolvePath(this.model, path);
                assert node != null;
                this.nodeChanged(node);
                return true;
            }
            catch (IllegalArgumentException ex) {
                this.display.setEditErrorMessage(ex.getMessage());
            }
        }
        return false;
    }
    
    void nodeChanged(final TreeTableNode node) {
        final TreeTableNode oldNode = this.display.getCurrentNode();
        this.display.setCurrentNode(node);
        if (!this.suppressCallback) {
            this.fireSelectedNodeChanged(node, this.getChildOf(node, oldNode));
        }
    }
    
    private TreeTableNode getChildOf(final TreeTableNode parent, TreeTableNode node) {
        while (node != null && node != parent) {
            node = node.getParent();
        }
        return node;
    }
    
    private void tableSelectToCurrentNode() {
        if (!this.suppressTreeSelectionUpdating) {
            this.table.collapseAll();
            final int idx = this.table.getRowFromNodeExpand(this.display.getCurrentNode());
            this.suppressCallback = true;
            try {
                this.selectionModel.setSelection(idx, idx);
            }
            finally {
                this.suppressCallback = false;
            }
            this.table.scrollToRow(Math.max(0, idx));
        }
    }
    
    protected boolean openPopup() {
        if (super.openPopup()) {
            this.popup.validateLayout();
            this.tableSelectToCurrentNode();
            return true;
        }
        return false;
    }
    
    public interface PathResolver
    {
        TreeTableNode resolvePath(final TreeTableModel p0, final String p1) throws IllegalArgumentException;
    }
    
    public interface Callback
    {
        void selectedNodeChanged(final TreeTableNode p0, final TreeTableNode p1);
    }
}
