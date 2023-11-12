package de.matthiasmann.twl;

import de.matthiasmann.twl.model.*;
import java.util.logging.*;
import de.matthiasmann.twl.utils.*;
import java.util.*;

public class FolderBrowser extends Widget
{
    final FileSystemModel fsm;
    final ListBox<Object> listbox;
    final FolderModel model;
    private final BoxLayout curFolderGroup;
    private Runnable[] selectionChangedCallbacks;
    Comparator<String> folderComparator;
    private Object currentFolder;
    private Runnable[] callbacks;
    
    public FolderBrowser() {
        this(JavaFileSystemModel.getInstance());
    }
    
    public FolderBrowser(final FileSystemModel fsm) {
        if (fsm == null) {
            throw new NullPointerException("fsm");
        }
        this.fsm = fsm;
        this.model = new FolderModel();
        this.listbox = new ListBox<Object>(this.model);
        (this.curFolderGroup = new BoxLayout()).setTheme("currentpathbox");
        this.curFolderGroup.setScroll(true);
        this.curFolderGroup.setClip(true);
        this.curFolderGroup.setAlignment(Alignment.BOTTOM);
        this.listbox.addCallback((CallbackWithReason<ListBox.CallbackReason>)new CallbackWithReason<ListBox.CallbackReason>() {
            private Object lastSelection;
            
            public void callback(final ListBox.CallbackReason reason) {
                if (FolderBrowser.this.listbox.getSelected() != -1 && reason.actionRequested()) {
                    FolderBrowser.this.setCurrentFolder(FolderBrowser.this.model.getFolder(FolderBrowser.this.listbox.getSelected()));
                }
                final Object selection = FolderBrowser.this.getSelectedFolder();
                if (selection != this.lastSelection) {
                    this.lastSelection = selection;
                    FolderBrowser.this.fireSelectionChangedCallback();
                }
            }
        });
        this.add(this.listbox);
        this.add((Widget)this.curFolderGroup);
        this.setCurrentFolder(null);
    }
    
    public void addCallback(final Runnable cb) {
        this.callbacks = CallbackSupport.addCallbackToList(this.callbacks, cb, Runnable.class);
    }
    
    public void removeCallback(final Runnable cb) {
        this.callbacks = CallbackSupport.removeCallbackFromList(this.callbacks, cb);
    }
    
    protected void doCallback() {
        CallbackSupport.fireCallbacks(this.callbacks);
    }
    
    public Comparator<String> getFolderComparator() {
        return this.folderComparator;
    }
    
    public void setFolderComparator(final Comparator<String> folderComparator) {
        this.folderComparator = folderComparator;
    }
    
    public FileSystemModel getFileSystemModel() {
        return this.fsm;
    }
    
    public Object getCurrentFolder() {
        return this.currentFolder;
    }
    
    public boolean setCurrentFolder(final Object folder) {
        if (!this.model.listFolders(folder)) {
            return false;
        }
        if (folder == null && this.model.getNumEntries() == 1 && this.setCurrentFolder(this.model.getFolder(0))) {
            return true;
        }
        this.currentFolder = folder;
        this.listbox.setSelected(-1);
        this.rebuildCurrentFolderGroup();
        this.doCallback();
        return true;
    }
    
    public boolean goToParentFolder() {
        if (this.currentFolder != null) {
            final Object current = this.currentFolder;
            if (this.setCurrentFolder(this.fsm.getParent(current))) {
                this.selectFolder(current);
                return true;
            }
        }
        return false;
    }
    
    public Object getSelectedFolder() {
        if (this.listbox.getSelected() != -1) {
            return this.model.getFolder(this.listbox.getSelected());
        }
        return null;
    }
    
    public boolean selectFolder(final Object current) {
        final int idx = this.model.findFolder(current);
        this.listbox.setSelected(idx);
        return idx != -1;
    }
    
    public void addSelectionChangedCallback(final Runnable cb) {
        this.callbacks = CallbackSupport.addCallbackToList(this.selectionChangedCallbacks, cb, Runnable.class);
    }
    
    public void removeSelectionChangedCallback(final Runnable cb) {
        this.selectionChangedCallbacks = CallbackSupport.removeCallbackFromList(this.selectionChangedCallbacks, cb);
    }
    
    protected void fireSelectionChangedCallback() {
        CallbackSupport.fireCallbacks(this.selectionChangedCallbacks);
    }
    
    public boolean handleEvent(final Event evt) {
        if (evt.isKeyPressedEvent()) {
            switch (evt.getKeyCode()) {
                case 14: {
                    this.goToParentFolder();
                    return true;
                }
            }
        }
        return super.handleEvent(evt);
    }
    
    private void rebuildCurrentFolderGroup() {
        this.curFolderGroup.removeAllChildren();
        this.recursiveAddFolder(this.currentFolder, null);
    }
    
    private void recursiveAddFolder(final Object folder, final Object subFolder) {
        if (folder != null) {
            this.recursiveAddFolder(this.fsm.getParent(folder), folder);
        }
        if (this.curFolderGroup.getNumChildren() > 0) {
            final Label l = new Label(this.fsm.getSeparator());
            l.setTheme("pathseparator");
            this.curFolderGroup.add((Widget)l);
        }
        String name = this.getFolderName(folder);
        if (name.endsWith(this.fsm.getSeparator())) {
            name = name.substring(0, name.length() - 1);
        }
        final Button btn = new Button(name);
        btn.addCallback((Runnable)new Runnable() {
            @Override
            public void run() {
                if (FolderBrowser.this.setCurrentFolder(folder)) {
                    FolderBrowser.this.selectFolder(subFolder);
                }
                FolderBrowser.this.listbox.requestKeyboardFocus();
            }
        });
        btn.setTheme("pathbutton");
        this.curFolderGroup.add((Widget)btn);
    }
    
    @Override
    public void adjustSize() {
    }
    
    @Override
    protected void layout() {
        this.curFolderGroup.setPosition(this.getInnerX(), this.getInnerY());
        this.curFolderGroup.setSize(this.getInnerWidth(), this.curFolderGroup.getHeight());
        this.listbox.setPosition(this.getInnerX(), this.curFolderGroup.getBottom());
        this.listbox.setSize(this.getInnerWidth(), Math.max(0, this.getInnerBottom() - this.listbox.getY()));
    }
    
    String getFolderName(final Object folder) {
        if (folder != null) {
            return this.fsm.getName(folder);
        }
        return "ROOT";
    }
    
    class FolderModel extends SimpleListModel<Object>
    {
        private Object[] folders;
        
        FolderModel() {
            this.folders = new Object[0];
        }
        
        public boolean listFolders(final Object parent) {
            Object[] newFolders;
            if (parent == null) {
                newFolders = FolderBrowser.this.fsm.listRoots();
            }
            else {
                newFolders = FolderBrowser.this.fsm.listFolder(parent, FileSystemTreeModel.FolderFilter.instance);
            }
            if (newFolders == null) {
                Logger.getLogger(FolderModel.class.getName()).log(Level.WARNING, "can''t list folder: {0}", parent);
                return false;
            }
            Arrays.sort(newFolders, (Comparator<? super Object>)new FileSelector.NameSorter(FolderBrowser.this.fsm, (Comparator)((FolderBrowser.this.folderComparator != null) ? FolderBrowser.this.folderComparator : NaturalSortComparator.stringComparator)));
            this.folders = newFolders;
            this.fireAllChanged();
            return true;
        }
        
        @Override
        public int getNumEntries() {
            return this.folders.length;
        }
        
        public Object getFolder(final int index) {
            return this.folders[index];
        }
        
        @Override
        public Object getEntry(final int index) {
            final Object folder = this.getFolder(index);
            return FolderBrowser.this.getFolderName(folder);
        }
        
        public int findFolder(final Object folder) {
            final int idx = FolderBrowser.this.fsm.find(this.folders, folder);
            return (idx < 0) ? -1 : idx;
        }
    }
}
