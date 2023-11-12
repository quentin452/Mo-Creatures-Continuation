package de.matthiasmann.twl;

import de.matthiasmann.twl.utils.*;
import java.util.logging.*;
import de.matthiasmann.twl.model.*;

public class PropertySheet extends TreeTable
{
    private final SimplePropertyList rootList;
    private final PropertyListCellRenderer subListRenderer;
    private final CellRenderer editorRenderer;
    private final TypeMapping<PropertyEditorFactory<?>> factories;

    public PropertySheet() {
        this(new Model());
    }

    private PropertySheet(final Model model) {
        super((TreeTableModel)model);
        this.rootList = new SimplePropertyList("<root>");
        this.subListRenderer = new PropertyListCellRenderer();
        this.editorRenderer = new EditorRenderer();
        this.factories = new TypeMapping<PropertyEditorFactory<?>>();
        this.rootList.addValueChangedCallback((Runnable)new TreeGenerator((PropertyList)this.rootList, model));
        this.registerPropertyEditorFactory(String.class, new StringEditorFactory());
    }

    public SimplePropertyList getPropertyList() {
        return this.rootList;
    }

    public <T> void registerPropertyEditorFactory(final Class<T> clazz, final PropertyEditorFactory<T> factory) {
        if (clazz == null) {
            throw new NullPointerException("clazz");
        }
        if (factory == null) {
            throw new NullPointerException("factory");
        }
        this.factories.put(clazz, factory);
    }

    @Override
    public void setModel(final TreeTableModel model) {
        if (model instanceof Model) {
            super.setModel(model);
            return;
        }
        throw new UnsupportedOperationException("Do not call this method");
    }

    @Override
    protected void applyTheme(final ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        this.applyThemePropertiesSheet(themeInfo);
    }

    protected void applyThemePropertiesSheet(final ThemeInfo themeInfo) {
        this.applyCellRendererTheme(this.subListRenderer);
        this.applyCellRendererTheme(this.editorRenderer);
    }

    @Override
    protected CellRenderer getCellRenderer(final int row, final int col, TreeTableNode node) {
        if (node == null) {
            node = this.getNodeFromRow(row);
        }
        if (node instanceof ListNode) {
            if (col == 0) {
                final PropertyListCellRenderer cr = this.subListRenderer;
                final NodeState nodeState = this.getOrCreateNodeState(node);
                cr.setCellData(row, col, node.getData(col), nodeState);
                return cr;
            }
            return null;
        }
        else {
            if (col == 0) {
                return super.getCellRenderer(row, col, node);
            }
            final CellRenderer cr2 = this.editorRenderer;
            cr2.setCellData(row, col, node.getData(col));
            return cr2;
        }
    }

    TreeTableNode createNode(final TreeTableNode parent, final Property<?> property) {
        if (property.getType() == PropertyList.class) {
            return (TreeTableNode)new ListNode(parent, property);
        }
        final Class<?> type = (Class<?>)property.getType();
        final PropertyEditorFactory factory = this.factories.get(type);
        if (factory != null) {
            final PropertyEditor editor = factory.createEditor(property);
            if (editor != null) {
                return (TreeTableNode)new LeafNode(parent, property, editor);
            }
        }
        else {
            Logger.getLogger(PropertySheet.class.getName()).log(Level.WARNING, "No property editor factory for type {0}", type);
        }
        return null;
    }

    abstract static class PropertyNode extends AbstractTreeTableNode implements Runnable, PSTreeTableNode
    {
        protected final Property<?> property;

        public PropertyNode(final TreeTableNode parent, final Property<?> property) {
            super(parent);
            (this.property = property).addValueChangedCallback((Runnable)this);
        }

        protected void removeCallback() {
            this.property.removeValueChangedCallback((Runnable)this);
        }

        public void removeAllChildren() {
            super.removeAllChildren();
        }

        public void addChild(final TreeTableNode parent) {
            this.insertChild(parent, this.getNumChildren());
        }
    }

    class TreeGenerator implements Runnable
    {
        private final PropertyList list;
        private final PSTreeTableNode parent;

        public TreeGenerator(final PropertyList list, final PSTreeTableNode parent) {
            this.list = list;
            this.parent = parent;
        }

        @Override
        public void run() {
            this.parent.removeAllChildren();
            this.addSubProperties();
        }

        void removeChildCallbacks(final PSTreeTableNode parent) {
            for (int i = 0, n = parent.getNumChildren(); i < n; ++i) {
                ((PropertyNode)parent.getChild(i)).removeCallback();
            }
        }

        void addSubProperties() {
            for (int i = 0; i < this.list.getNumProperties(); ++i) {
                final TreeTableNode node = PropertySheet.this.createNode((TreeTableNode)this.parent, (Property<?>)this.list.getProperty(i));
                if (node != null) {
                    this.parent.addChild(node);
                }
            }
        }
    }

    static class LeafNode extends PropertyNode
    {
        private final PropertyEditor editor;

        public LeafNode(final TreeTableNode parent, final Property<?> property, final PropertyEditor editor) {
            super(parent, property);
            this.editor = editor;
            this.setLeaf(true);
        }

        public Object getData(final int column) {
            switch (column) {
                case 0: {
                    return this.property.getName();
                }
                case 1: {
                    return this.editor;
                }
                default: {
                    return "???";
                }
            }
        }

        public void run() {
            this.editor.valueChanged();
            this.fireNodeChanged();
        }
    }

    class ListNode extends PropertyNode
    {
        protected final TreeGenerator treeGenerator;

        public ListNode(final TreeTableNode parent, final Property<?> property) {
            super(parent, property);
            (this.treeGenerator = new TreeGenerator((PropertyList)property.getPropertyValue(), this)).run();
        }

        public Object getData(final int column) {
            return this.property.getName();
        }

        public void run() {
            this.treeGenerator.run();
        }

        @Override
        protected void removeCallback() {
            super.removeCallback();
            this.treeGenerator.removeChildCallbacks(this);
        }
    }

    class PropertyListCellRenderer extends TreeNodeCellRenderer
    {
        private final Widget bgRenderer;
        private final Label textRenderer;

        public PropertyListCellRenderer() {
            this.bgRenderer = new Widget();
            (this.textRenderer = new Label(this.bgRenderer.getAnimationState())).setAutoSize(false);
            this.bgRenderer.add((Widget)this.textRenderer);
            this.bgRenderer.setTheme(this.getTheme());
        }

        @Override
        public int getColumnSpan() {
            return 2;
        }

        @Override
        public Widget getCellRenderWidget(final int x, final int y, final int width, final int height, final boolean isSelected) {
            this.bgRenderer.setPosition(x, y);
            this.bgRenderer.setSize(width, height);
            final int indent = this.getIndentation();
            this.textRenderer.setPosition(x + indent, y);
            this.textRenderer.setSize(Math.max(0, width - indent), height);
            this.bgRenderer.getAnimationState().setAnimationState(TableBase.STATE_SELECTED, isSelected);
            return this.bgRenderer;
        }

        @Override
        public void setCellData(final int row, final int column, final Object data, final NodeState nodeState) {
            super.setCellData(row, column, data, nodeState);
            this.textRenderer.setText((String)data);
        }

        @Override
        protected void setSubRenderer(final int row, final int column, final Object colData) {
        }
    }

    static class EditorRenderer implements CellRenderer, CellWidgetCreator
    {
        private PropertyEditor editor;

        @Override
        public void applyTheme(final ThemeInfo themeInfo) {
        }

        @Override
        public Widget getCellRenderWidget(final int x, final int y, final int width, final int height, final boolean isSelected) {
            this.editor.setSelected(isSelected);
            return null;
        }

        @Override
        public int getColumnSpan() {
            return 1;
        }

        @Override
        public int getPreferredHeight() {
            return this.editor.getWidget().getPreferredHeight();
        }

        @Override
        public String getTheme() {
            return "PropertyEditorCellRender";
        }

        @Override
        public void setCellData(final int row, final int column, final Object data) {
            this.editor = (PropertyEditor)data;
        }

        @Override
        public Widget updateWidget(final Widget existingWidget) {
            return this.editor.getWidget();
        }

        @Override
        public void positionWidget(final Widget widget, final int x, final int y, final int w, final int h) {
            if (!this.editor.positionWidget(x, y, w, h)) {
                widget.setPosition(x, y);
                widget.setSize(w, h);
            }
        }
    }

    static class Model extends AbstractTreeTableModel implements PSTreeTableNode
    {
        public String getColumnHeaderText(final int column) {
            switch (column) {
                case 0: {
                    return "Name";
                }
                case 1: {
                    return "Value";
                }
                default: {
                    return "???";
                }
            }
        }

        public int getNumColumns() {
            return 2;
        }

        public void removeAllChildren() {
            super.removeAllChildren();
        }

        public void addChild(final TreeTableNode parent) {
            this.insertChild(parent, this.getNumChildren());
        }
    }

    static class StringEditor implements PropertyEditor, EditField.Callback
    {
        private final EditField editField;
        private final Property<String> property;

        public StringEditor(final Property<String> property) {
            this.property = property;
            (this.editField = new EditField()).addCallback((EditField.Callback)this);
            this.resetValue();
        }

        @Override
        public Widget getWidget() {
            return (Widget)this.editField;
        }

        @Override
        public void valueChanged() {
            this.resetValue();
        }

        @Override
        public void preDestroy() {
            this.editField.removeCallback((EditField.Callback)this);
        }

        @Override
        public void setSelected(final boolean selected) {
        }

        public void callback(final int key) {
            if (key == 1) {
                this.resetValue();
            }
            else if (!this.property.isReadOnly()) {
                try {
                    this.property.setPropertyValue(this.editField.getText());
                    this.editField.setErrorMessage(null);
                }
                catch (IllegalArgumentException ex) {
                    this.editField.setErrorMessage(ex.getMessage());
                }
            }
        }

        private void resetValue() {
            this.editField.setText(this.property.getPropertyValue());
            this.editField.setErrorMessage(null);
            this.editField.setReadOnly(this.property.isReadOnly());
        }

        @Override
        public boolean positionWidget(final int x, final int y, final int width, final int height) {
            return false;
        }
    }

    static class StringEditorFactory implements PropertyEditorFactory<String>
    {
        @Override
        public PropertyEditor createEditor(final Property<String> property) {
            return new StringEditor(property);
        }
    }

    public static class ComboBoxEditor<T> implements PropertyEditor, Runnable
    {
        protected final ComboBox<T> comboBox;
        protected final Property<T> property;
        protected final ListModel<T> model;

        public ComboBoxEditor(final Property<T> property, final ListModel<T> model) {
            this.property = property;
            this.comboBox = new ComboBox<>((ListModel<T>)model);
            this.model = model;
            this.comboBox.addCallback((Runnable)this);
            this.resetValue();
        }

        @Override
        public Widget getWidget() {
            return (Widget)this.comboBox;
        }

        @Override
        public void valueChanged() {
            this.resetValue();
        }

        @Override
        public void preDestroy() {
            this.comboBox.removeCallback((Runnable)this);
        }

        @Override
        public void setSelected(final boolean selected) {
        }

        @Override
        public void run() {
            if (this.property.isReadOnly()) {
                this.resetValue();
            }
            else {
                final int idx = this.comboBox.getSelected();
                if (idx >= 0) {
                    this.property.setPropertyValue(this.model.getEntry(idx));
                }
            }
        }

        protected void resetValue() {
            this.comboBox.setSelected(this.findEntry(this.property.getPropertyValue()));
        }

        protected int findEntry(final T value) {
            for (int i = 0, n = this.model.getNumEntries(); i < n; ++i) {
                if (this.model.getEntry(i).equals(value)) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public boolean positionWidget(final int x, final int y, final int width, final int height) {
            return false;
        }
    }

    public static class ComboBoxEditorFactory<T> implements PropertyEditorFactory<T>
    {
        private final ModelForwarder modelForwarder;

        public ComboBoxEditorFactory(final ListModel<T> model) {
            this.modelForwarder = new ModelForwarder(model);
        }

        public ListModel<T> getModel() {
            return this.modelForwarder.getModel();
        }

        public void setModel(final ListModel<T> model) {
            this.modelForwarder.setModel(model);
        }

        @Override
        public PropertyEditor createEditor(final Property<T> property) {
            return new ComboBoxEditor<>((Property<Object>) property, (ListModel<Object>) this.modelForwarder);
        }

        class ModelForwarder extends AbstractListModel<T> implements ListModel.ChangeListener
        {
            private ListModel<T> model;

            public ModelForwarder(final ListModel<T> model) {
                this.setModel(model);
            }

            public int getNumEntries() {
                return this.model.getNumEntries();
            }

            public T getEntry(final int index) {
                return (T)this.model.getEntry(index);
            }

            public Object getEntryTooltip(final int index) {
                return this.model.getEntryTooltip(index);
            }

            public boolean matchPrefix(final int index, final String prefix) {
                return this.model.matchPrefix(index, prefix);
            }

            public ListModel<T> getModel() {
                return this.model;
            }

            public void setModel(final ListModel<T> model) {
                if (this.model != null) {
                    this.model.removeChangeListener((ListModel.ChangeListener)this);
                }
                (this.model = model).addChangeListener((ListModel.ChangeListener)this);
                this.fireAllChanged();
            }

            public void entriesInserted(final int first, final int last) {
                this.fireEntriesInserted(first, last);
            }

            public void entriesDeleted(final int first, final int last) {
                this.fireEntriesDeleted(first, last);
            }

            public void entriesChanged(final int first, final int last) {
                this.fireEntriesChanged(first, last);
            }

            public void allChanged() {
                this.fireAllChanged();
            }
        }
    }

    public interface PropertyEditor
    {
        Widget getWidget();

        void valueChanged();

        void preDestroy();

        void setSelected(final boolean p0);

        boolean positionWidget(final int p0, final int p1, final int p2, final int p3);
    }

    public interface PropertyEditorFactory<T>
    {
        PropertyEditor createEditor(final Property<T> p0);
    }

    interface PSTreeTableNode extends TreeTableNode
    {
        void addChild(final TreeTableNode p0);

        void removeAllChildren();
    }
}
