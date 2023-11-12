package de.matthiasmann.twl.model;

import java.util.*;

public class SimpleTableModel extends AbstractTableModel
{
    private final String[] columnHeaders;
    private final ArrayList<Object[]> rows;
    
    public SimpleTableModel(final String[] columnHeaders) {
        if (columnHeaders.length < 1) {
            throw new IllegalArgumentException("must have atleast one column");
        }
        this.columnHeaders = columnHeaders.clone();
        this.rows = new ArrayList<Object[]>();
    }
    
    public int getNumColumns() {
        return this.columnHeaders.length;
    }
    
    public String getColumnHeaderText(final int column) {
        return this.columnHeaders[column];
    }
    
    public void setColumnHeaderText(final int column, final String text) {
        if (text == null) {
            throw new NullPointerException("text");
        }
        this.columnHeaders[column] = text;
        this.fireColumnHeaderChanged(column);
    }
    
    public int getNumRows() {
        return this.rows.size();
    }
    
    public Object getCell(final int row, final int column) {
        return this.rows.get(row)[column];
    }
    
    public void setCell(final int row, final int column, final Object data) {
        this.rows.get(row)[column] = data;
        this.fireCellChanged(row, column);
    }
    
    public void addRow(final Object... data) {
        this.insertRow(this.rows.size(), data);
    }
    
    public void addRows(final Collection<Object[]> rows) {
        this.insertRows(this.rows.size(), rows);
    }
    
    public void insertRow(final int index, final Object... data) {
        this.rows.add(index, this.createRowData(data));
        this.fireRowsInserted(index, 1);
    }
    
    public void insertRows(final int index, final Collection<Object[]> rows) {
        if (!rows.isEmpty()) {
            final ArrayList<Object[]> rowData = new ArrayList<Object[]>();
            for (final Object[] row : rows) {
                rowData.add(this.createRowData(row));
            }
            this.rows.addAll(index, rowData);
            this.fireRowsInserted(index, rowData.size());
        }
    }
    
    public void deleteRow(final int index) {
        this.rows.remove(index);
        this.fireRowsDeleted(index, 1);
    }
    
    public void deleteRows(final int index, final int count) {
        final int numRows = this.rows.size();
        if (index < 0 || count < 0 || index >= numRows || count > numRows - index) {
            throw new IndexOutOfBoundsException("index=" + index + " count=" + count + " numRows=" + numRows);
        }
        if (count > 0) {
            int i = count;
            while (i-- > 0) {
                this.rows.remove(index + i);
            }
            this.fireRowsDeleted(index, count);
        }
    }
    
    private Object[] createRowData(final Object[] data) {
        final Object[] rowData = new Object[this.getNumColumns()];
        System.arraycopy(data, 0, rowData, 0, Math.min(rowData.length, data.length));
        return rowData;
    }
}
