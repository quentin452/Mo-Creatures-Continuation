package de.matthiasmann.twl;

import java.util.*;

public class ColumnLayout extends DialogLayout
{
    final ArrayList<Group> columnGroups;
    private final Panel rootPanel;
    private final HashMap<Columns, Columns> columns;
    
    public ColumnLayout() {
        this.columnGroups = new ArrayList<Group>();
        this.rootPanel = new Panel(null);
        this.columns = new HashMap<Columns, Columns>();
        this.setHorizontalGroup(this.createParallelGroup());
        this.setVerticalGroup(this.rootPanel.rows);
    }
    
    public final Panel getRootPanel() {
        return this.rootPanel;
    }
    
    public Columns getColumns(final String... columnNames) {
        if (columnNames.length == 0) {
            throw new IllegalArgumentException("columnNames");
        }
        final Columns key = new Columns(columnNames);
        final Columns cl = this.columns.get(key);
        if (cl != null) {
            return cl;
        }
        this.createColumns(key);
        return key;
    }
    
    public Row addRow(final Columns columns) {
        return this.rootPanel.addRow(columns);
    }
    
    public Row addRow(final String... columnNames) {
        return this.rootPanel.addRow(this.getColumns(columnNames));
    }
    
    private void createColumns(final Columns cl) {
        int prefixSize = 0;
        Columns prefixColumns = null;
        for (final Columns c : this.columns.values()) {
            final int match = c.match(cl);
            if (match > prefixSize) {
                prefixSize = match;
                prefixColumns = c;
            }
        }
        int numColumns = 0;
        for (int i = 0, n = cl.names.length; i < n; ++i) {
            if (!cl.isGap(i)) {
                ++numColumns;
            }
        }
        cl.numColumns = numColumns;
        cl.firstColumn = this.columnGroups.size();
        cl.childGroups = new Group[cl.names.length];
        Group h = this.createSequentialGroup();
        if (prefixColumns == null) {
            this.getHorizontalGroup().addGroup(h);
        }
        else {
            for (int j = 0; j < prefixSize; ++j) {
                if (!cl.isGap(j)) {
                    final Group g = this.columnGroups.get(prefixColumns.firstColumn + j);
                    this.columnGroups.add(g);
                }
            }
            System.arraycopy(prefixColumns.childGroups, 0, cl.childGroups, 0, prefixSize);
            cl.childGroups[prefixSize - 1].addGroup(h);
        }
        for (int j = prefixSize, n2 = cl.names.length; j < n2; ++j) {
            if (cl.isGap(j)) {
                h.addGap();
            }
            else {
                final Group g2 = this.createParallelGroup();
                h.addGroup(g2);
                this.columnGroups.add(g2);
            }
            final Group nextSequential = this.createSequentialGroup();
            final Group childGroup = this.createParallelGroup().addGroup(nextSequential);
            h.addGroup(childGroup);
            h = nextSequential;
            cl.childGroups[j] = childGroup;
        }
        this.columns.put(cl, cl);
    }
    
    public static final class Columns
    {
        final String[] names;
        final int hashcode;
        int firstColumn;
        int numColumns;
        Group[] childGroups;
        
        Columns(final String[] names) {
            this.names = names.clone();
            this.hashcode = Arrays.hashCode(this.names);
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            final Columns other = (Columns)obj;
            return this.hashcode == other.hashcode && Arrays.equals(this.names, other.names);
        }
        
        public int getNumColumns() {
            return this.numColumns;
        }
        
        public int getNumColumnNames() {
            return this.names.length;
        }
        
        public String getColumnName(final int idx) {
            return this.names[idx];
        }
        
        @Override
        public int hashCode() {
            return this.hashcode;
        }
        
        boolean isGap(final int column) {
            final String name = this.names[column];
            return name.length() == 0 || "-".equals(name);
        }
        
        int match(final Columns other) {
            final int cnt = Math.min(this.names.length, other.names.length);
            for (int i = 0; i < cnt; ++i) {
                if (!this.names[i].equals(other.names[i])) {
                    return i;
                }
            }
            return cnt;
        }
    }
    
    public final class Row
    {
        final Columns columns;
        final Panel panel;
        final Group row;
        int curColumn;
        
        Row(final Columns columns, final Panel panel, final Group row) {
            this.columns = columns;
            this.panel = panel;
            this.row = row;
        }
        
        public int getCurrentColumn() {
            return this.curColumn;
        }
        
        public Columns getColumns() {
            return this.columns;
        }
        
        public Row add(final Widget w) {
            if (this.curColumn == this.columns.numColumns) {
                throw new IllegalStateException("Too many widgets for column layout");
            }
            this.panel.getColumn(this.columns.firstColumn + this.curColumn).addWidget(w);
            this.row.addWidget(w);
            ++this.curColumn;
            return this;
        }
        
        public Row add(final Widget w, final Alignment alignment) {
            this.add(w);
            ColumnLayout.this.setWidgetAlignment(w, alignment);
            return this;
        }
        
        public Row addLabel(final String labelText) {
            if (labelText == null) {
                throw new NullPointerException("labelText");
            }
            return this.add(new Label(labelText));
        }
        
        public Row addWithLabel(final String labelText, final Widget w) {
            if (labelText == null) {
                throw new NullPointerException("labelText");
            }
            final Label labelWidget = new Label(labelText);
            labelWidget.setLabelFor(w);
            this.add(labelWidget, Alignment.TOPLEFT).add(w);
            return this;
        }
        
        public Row addWithLabel(final String labelText, final Widget w, final Alignment alignment) {
            this.addWithLabel(labelText, w);
            ColumnLayout.this.setWidgetAlignment(w, alignment);
            return this;
        }
    }
    
    public final class Panel
    {
        final Panel parent;
        final ArrayList<Group> usedColumnGroups;
        final ArrayList<Panel> children;
        final Group rows;
        boolean valid;
        
        Panel(final Panel parent) {
            this.parent = parent;
            this.usedColumnGroups = new ArrayList<Group>();
            this.children = new ArrayList<Panel>();
            this.rows = ColumnLayout.this.createSequentialGroup();
            this.valid = true;
        }
        
        public boolean isValid() {
            return this.valid;
        }
        
        public Columns getColumns(final String... columnNames) {
            return ColumnLayout.this.getColumns(columnNames);
        }
        
        public Row addRow(final String... columnNames) {
            return this.addRow(ColumnLayout.this.getColumns(columnNames));
        }
        
        public Row addRow(final Columns columns) {
            if (columns == null) {
                throw new NullPointerException("columns");
            }
            this.checkValid();
            final Group row = ColumnLayout.this.createParallelGroup();
            this.rows.addGroup(row);
            return new Row(columns, this, row);
        }
        
        public void addVerticalGap(final String name) {
            this.checkValid();
            this.rows.addGap(name);
        }
        
        public Panel addPanel() {
            this.checkValid();
            final Panel panel = new Panel(this);
            this.rows.addGroup(panel.rows);
            this.children.add(panel);
            return panel;
        }
        
        public void removePanel(final Panel panel) {
            if (panel == null) {
                throw new NullPointerException("panel");
            }
            if (this.valid && this.children.remove(panel)) {
                panel.markInvalid();
                this.rows.removeGroup(panel.rows, true);
                for (int i = 0, n = panel.usedColumnGroups.size(); i < n; ++i) {
                    final Group column = panel.usedColumnGroups.get(i);
                    if (column != null) {
                        this.usedColumnGroups.get(i).removeGroup(column, false);
                    }
                }
            }
        }
        
        public void clearPanel() {
            if (this.valid) {
                this.children.clear();
                this.rows.clear(true);
                for (int i = 0, n = this.usedColumnGroups.size(); i < n; ++i) {
                    final Group column = this.usedColumnGroups.get(i);
                    if (column != null) {
                        column.clear(false);
                    }
                }
            }
        }
        
        void markInvalid() {
            this.valid = false;
            for (int i = 0, n = this.children.size(); i < n; ++i) {
                this.children.get(i).markInvalid();
            }
        }
        
        void checkValid() {
            if (!this.valid) {
                throw new IllegalStateException("Panel has been removed");
            }
        }
        
        Group getColumn(final int idx) {
            this.checkValid();
            if (this.usedColumnGroups.size() > idx) {
                final Group column = this.usedColumnGroups.get(idx);
                if (column != null) {
                    return column;
                }
            }
            return this.makeColumn(idx);
        }
        
        private Group makeColumn(final int idx) {
            Group parentColumn;
            if (this.parent != null) {
                parentColumn = this.parent.getColumn(idx);
            }
            else {
                parentColumn = ColumnLayout.this.columnGroups.get(idx);
            }
            final Group column = ColumnLayout.this.createParallelGroup();
            parentColumn.addGroup(column);
            while (this.usedColumnGroups.size() <= idx) {
                this.usedColumnGroups.add(null);
            }
            this.usedColumnGroups.set(idx, column);
            return column;
        }
    }
}
