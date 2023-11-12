package de.matthiasmann.twl;

import de.matthiasmann.twl.model.*;

public class TableSearchWindow extends InfoWindow implements TableBase.KeyboardSearchHandler
{
    private final TableSelectionModel selectionModel;
    private final EditField searchTextField;
    private final StringBuilder searchTextBuffer;
    private String searchText;
    private String searchTextLowercase;
    private Timer timer;
    private TableModel model;
    private int column;
    private int currentRow;
    private boolean searchStartOnly;
    
    public TableSearchWindow(final Table table, final TableSelectionModel selectionModel) {
        super((Widget)table);
        this.selectionModel = selectionModel;
        this.searchTextField = new EditField();
        this.searchTextBuffer = new StringBuilder();
        this.searchText = "";
        final Label label = new Label("Search");
        label.setLabelFor((Widget)this.searchTextField);
        this.searchTextField.setReadOnly(true);
        final DialogLayout l = new DialogLayout();
        l.setHorizontalGroup(l.createSequentialGroup().addWidget((Widget)label).addWidget((Widget)this.searchTextField));
        l.setVerticalGroup(l.createParallelGroup().addWidget((Widget)label).addWidget((Widget)this.searchTextField));
        this.add((Widget)l);
    }
    
    public Table getTable() {
        return (Table)this.getOwner();
    }
    
    public TableModel getModel() {
        return this.model;
    }
    
    public void setModel(final TableModel model, final int column) {
        if (column < 0) {
            throw new IllegalArgumentException("column");
        }
        if (model != null && column >= model.getNumColumns()) {
            throw new IllegalArgumentException("column");
        }
        this.model = model;
        this.column = column;
        this.cancelSearch();
    }
    
    public boolean isActive() {
        return this.isOpen();
    }
    
    public void updateInfoWindowPosition() {
        this.adjustSize();
        this.setPosition(this.getOwner().getX(), this.getOwner().getBottom());
    }
    
    public boolean handleKeyEvent(final Event evt) {
        if (this.model == null) {
            return false;
        }
        if (evt.isKeyPressedEvent()) {
            switch (evt.getKeyCode()) {
                case 1: {
                    if (this.isOpen()) {
                        this.cancelSearch();
                        return true;
                    }
                    break;
                }
                case 28: {
                    return false;
                }
                case 14: {
                    if (this.isOpen()) {
                        final int length = this.searchTextBuffer.length();
                        if (length > 0) {
                            this.searchTextBuffer.setLength(length - 1);
                            this.updateText();
                        }
                        this.restartTimer();
                        return true;
                    }
                    break;
                }
                case 200: {
                    if (this.isOpen()) {
                        this.searchDir(-1);
                        this.restartTimer();
                        return true;
                    }
                    break;
                }
                case 208: {
                    if (this.isOpen()) {
                        this.searchDir(1);
                        this.restartTimer();
                        return true;
                    }
                    break;
                }
                default: {
                    if (evt.hasKeyCharNoModifiers()) {
                        if (this.searchTextBuffer.length() == 0) {
                            this.currentRow = Math.max(0, this.getTable().getSelectionManager().getLeadRow());
                            this.searchStartOnly = true;
                        }
                        this.searchTextBuffer.append(evt.getKeyChar());
                        this.updateText();
                        this.restartTimer();
                        return true;
                    }
                    break;
                }
            }
        }
        return false;
    }
    
    public void cancelSearch() {
        this.searchTextBuffer.setLength(0);
        this.updateText();
        this.closeInfo();
        if (this.timer != null) {
            this.timer.stop();
        }
    }
    
    protected void afterAddToGUI(final GUI gui) {
        super.afterAddToGUI(gui);
        (this.timer = gui.createTimer()).setDelay(3000);
        this.timer.setCallback(new Runnable() {
            @Override
            public void run() {
                TableSearchWindow.this.cancelSearch();
            }
        });
    }
    
    protected void beforeRemoveFromGUI(final GUI gui) {
        this.timer.stop();
        this.timer = null;
        super.beforeRemoveFromGUI(gui);
    }
    
    private void updateText() {
        this.searchText = this.searchTextBuffer.toString();
        this.searchTextLowercase = null;
        this.searchTextField.setText(this.searchText);
        if (this.searchText.length() >= 0 && this.model != null) {
            if (!this.isOpen() && this.openInfo()) {
                this.updateInfoWindowPosition();
            }
            this.updateSearch();
        }
    }
    
    private void restartTimer() {
        this.timer.stop();
        this.timer.start();
    }
    
    private void updateSearch() {
        int numRows = this.model.getNumRows();
        if (numRows == 0) {
            return;
        }
        for (int row = this.currentRow; row < numRows; ++row) {
            if (this.checkRow(row)) {
                this.setRow(row);
                return;
            }
        }
        if (this.searchStartOnly) {
            this.searchStartOnly = false;
        }
        else {
            numRows = this.currentRow;
        }
        for (int row = 0; row < numRows; ++row) {
            if (this.checkRow(row)) {
                this.setRow(row);
                return;
            }
        }
        this.searchTextField.setErrorMessage((Object)("'" + this.searchText + "' not found"));
    }
    
    private void searchDir(final int dir) {
        final int numRows = this.model.getNumRows();
        if (numRows == 0) {
            return;
        }
        int row;
        final int startRow = row = wrap(this.currentRow, numRows);
        while (true) {
            row = wrap(row + dir, numRows);
            if (this.checkRow(row)) {
                this.setRow(row);
                return;
            }
            if (row != startRow) {
                continue;
            }
            if (!this.searchStartOnly) {
                return;
            }
            this.searchStartOnly = false;
        }
    }
    
    private void setRow(final int row) {
        if (this.currentRow != row) {
            this.currentRow = row;
            this.getTable().scrollToRow(row);
            if (this.selectionModel != null) {
                this.selectionModel.setSelection(row, row);
            }
        }
        this.searchTextField.setErrorMessage((Object)null);
    }
    
    private boolean checkRow(final int row) {
        final Object data = this.model.getCell(row, this.column);
        if (data == null) {
            return false;
        }
        String str = data.toString();
        if (this.searchStartOnly) {
            return str.regionMatches(true, 0, this.searchText, 0, this.searchText.length());
        }
        str = str.toLowerCase();
        if (this.searchTextLowercase == null) {
            this.searchTextLowercase = this.searchText.toLowerCase();
        }
        return str.contains(this.searchTextLowercase);
    }
    
    private static int wrap(final int row, final int numRows) {
        if (row < 0) {
            return numRows - 1;
        }
        if (row >= numRows) {
            return 0;
        }
        return row;
    }
}
