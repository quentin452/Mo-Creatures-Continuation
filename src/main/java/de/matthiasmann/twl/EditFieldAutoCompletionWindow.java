package de.matthiasmann.twl;

import java.util.concurrent.*;
import java.util.logging.*;
import de.matthiasmann.twl.model.*;

public class EditFieldAutoCompletionWindow extends InfoWindow
{
    private final ResultListModel listModel;
    private final ListBox<String> listBox;
    private boolean captureKeys;
    private boolean useInvokeAsync;
    private AutoCompletionDataSource dataSource;
    private ExecutorService executorService;
    private Future<AutoCompletionResult> future;
    
    public EditFieldAutoCompletionWindow(final EditField editField) {
        super((Widget)editField);
        this.listModel = new ResultListModel();
        this.add((Widget)(this.listBox = new ListBox<String>(this.listModel)));
        final Callbacks cb = new Callbacks();
        this.listBox.addCallback((CallbackWithReason<ListBox.CallbackReason>)cb);
    }
    
    public EditFieldAutoCompletionWindow(final EditField editField, final AutoCompletionDataSource dataSource) {
        this(editField);
        this.dataSource = dataSource;
    }
    
    public EditFieldAutoCompletionWindow(final EditField editField, final AutoCompletionDataSource dataSource, final ExecutorService executorService) {
        this(editField);
        this.dataSource = dataSource;
        this.executorService = executorService;
    }
    
    public final EditField getEditField() {
        return (EditField)this.getOwner();
    }
    
    public ExecutorService getExecutorService() {
        return this.executorService;
    }
    
    public boolean isUseInvokeAsync() {
        return this.useInvokeAsync;
    }
    
    public void setExecutorService(final ExecutorService executorService) {
        this.executorService = executorService;
        this.useInvokeAsync = false;
        this.cancelFuture();
    }
    
    public void setUseInvokeAsync(final boolean useInvokeAsync) {
        this.executorService = null;
        this.useInvokeAsync = useInvokeAsync;
        this.cancelFuture();
    }
    
    public AutoCompletionDataSource getDataSource() {
        return this.dataSource;
    }
    
    public void setDataSource(final AutoCompletionDataSource dataSource) {
        this.dataSource = dataSource;
        this.cancelFuture();
        if (this.isOpen()) {
            this.updateAutoCompletion();
        }
    }
    
    public void updateAutoCompletion() {
        this.cancelFuture();
        AutoCompletionResult result = null;
        if (this.dataSource != null) {
            final EditField ef = this.getEditField();
            final int cursorPos = ef.getCursorPos();
            if (cursorPos > 0) {
                final String text = ef.getText();
                final GUI gui = ef.getGUI();
                if (this.listModel.result != null) {
                    result = this.listModel.result.refine(text, cursorPos);
                }
                if (result == null) {
                    if (gui != null && (this.useInvokeAsync || this.executorService != null)) {
                        this.future = (this.useInvokeAsync ? gui.executorService : this.executorService).submit((Callable<AutoCompletionResult>)new AsyncQuery(gui, this.dataSource, text, cursorPos, this.listModel.result));
                    }
                    else {
                        try {
                            result = this.dataSource.collectSuggestions(text, cursorPos, this.listModel.result);
                        }
                        catch (Exception ex) {
                            this.reportQueryException(ex);
                        }
                    }
                }
            }
        }
        this.updateAutoCompletion(result);
    }
    
    public void stopAutoCompletion() {
        this.listModel.setResult(null);
        this.installAutoCompletion();
    }
    
    @Override
    protected void infoWindowClosed() {
        this.stopAutoCompletion();
    }
    
    protected void updateAutoCompletion(final AutoCompletionResult results) {
        this.listModel.setResult(results);
        this.captureKeys = false;
        this.installAutoCompletion();
    }
    
    void checkFuture() {
        if (this.future != null && this.future.isDone()) {
            AutoCompletionResult result = null;
            try {
                result = this.future.get();
            }
            catch (InterruptedException ex2) {
                Thread.currentThread().interrupt();
            }
            catch (ExecutionException ex) {
                this.reportQueryException(ex.getCause());
            }
            this.future = null;
            this.updateAutoCompletion(result);
        }
    }
    
    void cancelFuture() {
        if (this.future != null) {
            this.future.cancel(true);
            this.future = null;
        }
    }
    
    protected void reportQueryException(final Throwable ex) {
        Logger.getLogger(EditFieldAutoCompletionWindow.class.getName()).log(Level.SEVERE, "Exception while collecting auto completion results", ex);
    }
    
    protected boolean handleEvent(final Event evt) {
        if (!evt.isKeyEvent()) {
            return super.handleEvent(evt);
        }
        if (this.captureKeys) {
            if (evt.isKeyPressedEvent()) {
                switch (evt.getKeyCode()) {
                    case 28:
                    case 156: {
                        return this.acceptAutoCompletion();
                    }
                    case 1: {
                        this.stopAutoCompletion();
                        break;
                    }
                    case 199:
                    case 200:
                    case 201:
                    case 207:
                    case 208:
                    case 209: {
                        this.listBox.handleEvent(evt);
                        break;
                    }
                    case 203:
                    case 205: {
                        return false;
                    }
                    default: {
                        if (evt.hasKeyChar() || evt.getKeyCode() == 14) {
                            if (!this.acceptAutoCompletion()) {
                                this.stopAutoCompletion();
                            }
                            return false;
                        }
                        break;
                    }
                }
            }
            return true;
        }
        switch (evt.getKeyCode()) {
            case 200:
            case 208:
            case 209: {
                this.listBox.handleEvent(evt);
                this.startCapture();
                return this.captureKeys;
            }
            case 1: {
                this.stopAutoCompletion();
                return false;
            }
            case 57: {
                if ((evt.getModifiers() & 0x24) != 0x0) {
                    this.updateAutoCompletion();
                    return true;
                }
                return false;
            }
            default: {
                return false;
            }
        }
    }
    
    boolean acceptAutoCompletion() {
        final int selected = this.listBox.getSelected();
        if (selected >= 0) {
            final EditField editField = this.getEditField();
            final String text = this.listModel.getEntry(selected);
            final int pos = this.listModel.getCursorPosForEntry(selected);
            editField.setText(text);
            if (pos >= 0 && pos < text.length()) {
                editField.setCursorPos(pos);
            }
            this.stopAutoCompletion();
            return true;
        }
        return false;
    }
    
    private void startCapture() {
        this.captureKeys = true;
        this.installAutoCompletion();
    }
    
    private void installAutoCompletion() {
        if (this.listModel.getNumEntries() > 0) {
            this.openInfo();
        }
        else {
            this.captureKeys = false;
            this.closeInfo();
        }
    }
    
    static class ResultListModel extends SimpleListModel<String>
    {
        AutoCompletionResult result;
        
        public void setResult(final AutoCompletionResult result) {
            this.result = result;
            this.fireAllChanged();
        }
        
        @Override
        public int getNumEntries() {
            return (this.result == null) ? 0 : this.result.getNumResults();
        }
        
        @Override
        public String getEntry(final int index) {
            return this.result.getResult(index);
        }
        
        public int getCursorPosForEntry(final int index) {
            return this.result.getCursorPosForResult(index);
        }
    }
    
    class Callbacks implements CallbackWithReason<ListBox.CallbackReason>
    {
        public void callback(final ListBox.CallbackReason reason) {
            switch (reason) {
                case MOUSE_DOUBLE_CLICK: {
                    EditFieldAutoCompletionWindow.this.acceptAutoCompletion();
                    break;
                }
            }
        }
    }
    
    class AsyncQuery implements Callable<AutoCompletionResult>, Runnable
    {
        private final GUI gui;
        private final AutoCompletionDataSource dataSource;
        private final String text;
        private final int cursorPos;
        private final AutoCompletionResult prevResult;
        
        public AsyncQuery(final GUI gui, final AutoCompletionDataSource dataSource, final String text, final int cursorPos, final AutoCompletionResult prevResult) {
            this.gui = gui;
            this.dataSource = dataSource;
            this.text = text;
            this.cursorPos = cursorPos;
            this.prevResult = prevResult;
        }
        
        @Override
        public AutoCompletionResult call() throws Exception {
            final AutoCompletionResult acr = this.dataSource.collectSuggestions(this.text, this.cursorPos, this.prevResult);
            this.gui.invokeLater(this);
            return acr;
        }
        
        @Override
        public void run() {
            EditFieldAutoCompletionWindow.this.checkFuture();
        }
    }
}
