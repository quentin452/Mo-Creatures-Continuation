package de.matthiasmann.twl;

import java.util.prefs.*;
import de.matthiasmann.twl.model.*;
import java.util.*;
import de.matthiasmann.twl.utils.*;

public class FileSelector extends DialogLayout
{
    public static final NamedFileFilter AllFilesFilter;
    private final IntegerModel flags;
    private final MRUListModel<String> folderMRU;
    final MRUListModel<String> filesMRU;
    private final TreeComboBox currentFolder;
    private final Label labelCurrentFolder;
    private final FileTable fileTable;
    private final ScrollPane fileTableSP;
    private final Button btnUp;
    private final Button btnHome;
    private final Button btnFolderMRU;
    private final Button btnFilesMRU;
    private final Button btnOk;
    private final Button btnCancel;
    private final Button btnRefresh;
    private final Button btnShowFolders;
    private final Button btnShowHidden;
    private final ComboBox<String> fileFilterBox;
    private final FileFiltersModel fileFiltersModel;
    private final EditFieldAutoCompletionWindow autoCompletion;
    private boolean allowFolderSelection;
    private Callback[] callbacks;
    private NamedFileFilter activeFileFilter;
    FileSystemModel fsm;
    private FileSystemTreeModel model;
    private Widget userWidgetBottom;
    private Widget userWidgetRight;
    private Object fileToSelectOnSetCurrentNode;
    
    public FileSelector() {
        this(null, null);
    }
    
    public FileSelector(final Preferences prefs, final String prefsKey) {
        if (prefs == null != (prefsKey == null)) {
            throw new IllegalArgumentException("'prefs' and 'prefsKey' must both be valid or both null");
        }
        if (prefs != null) {
            this.flags = new PersistentIntegerModel(prefs, prefsKey.concat("_Flags"), 0, 65535, 0);
            this.folderMRU = new PersistentMRUListModel<String>(10, String.class, prefs, prefsKey.concat("_foldersMRU"));
            this.filesMRU = new PersistentMRUListModel<String>(20, String.class, prefs, prefsKey.concat("_filesMRU"));
        }
        else {
            this.flags = new SimpleIntegerModel(0, 65535, 0);
            this.folderMRU = new SimpleMRUListModel<String>(10);
            this.filesMRU = new SimpleMRUListModel<String>(20);
        }
        (this.currentFolder = new TreeComboBox()).setTheme("currentFolder");
        (this.fileTable = new FileTable()).setTheme("fileTable");
        this.fileTable.addCallback(new FileTable.Callback() {
            @Override
            public void selectionChanged() {
                FileSelector.this.selectionChanged();
            }
            
            @Override
            public void sortingChanged() {
            }
        });
        (this.btnUp = new Button()).setTheme("buttonUp");
        this.btnUp.addCallback((Runnable)new Runnable() {
            @Override
            public void run() {
                FileSelector.this.goOneLevelUp();
            }
        });
        (this.btnHome = new Button()).setTheme("buttonHome");
        this.btnHome.addCallback((Runnable)new Runnable() {
            @Override
            public void run() {
                FileSelector.this.goHome();
            }
        });
        (this.btnFolderMRU = new Button()).setTheme("buttonFoldersMRU");
        this.btnFolderMRU.addCallback((Runnable)new Runnable() {
            @Override
            public void run() {
                FileSelector.this.showFolderMRU();
            }
        });
        (this.btnFilesMRU = new Button()).setTheme("buttonFilesMRU");
        this.btnFilesMRU.addCallback((Runnable)new Runnable() {
            @Override
            public void run() {
                FileSelector.this.showFilesMRU();
            }
        });
        (this.btnOk = new Button()).setTheme("buttonOk");
        this.btnOk.addCallback((Runnable)new Runnable() {
            @Override
            public void run() {
                FileSelector.this.acceptSelection();
            }
        });
        (this.btnCancel = new Button()).setTheme("buttonCancel");
        this.btnCancel.addCallback((Runnable)new Runnable() {
            @Override
            public void run() {
                FileSelector.this.fireCanceled();
            }
        });
        this.currentFolder.setPathResolver(new TreeComboBox.PathResolver() {
            @Override
            public TreeTableNode resolvePath(final TreeTableModel model, final String path) throws IllegalArgumentException {
                return FileSelector.this.resolvePath(path);
            }
        });
        this.currentFolder.addCallback(new TreeComboBox.Callback() {
            @Override
            public void selectedNodeChanged(final TreeTableNode node, final TreeTableNode previousChildNode) {
                FileSelector.this.setCurrentNode(node, previousChildNode);
            }
        });
        (this.autoCompletion = new EditFieldAutoCompletionWindow(this.currentFolder.getEditField())).setUseInvokeAsync(true);
        this.currentFolder.getEditField().setAutoCompletionWindow(this.autoCompletion);
        this.fileTable.setAllowMultiSelection(true);
        this.fileTable.addCallback(new TableBase.Callback() {
            @Override
            public void mouseDoubleClicked(final int row, final int column) {
                FileSelector.this.acceptSelection();
            }
            
            @Override
            public void mouseRightClick(final int row, final int column, final Event evt) {
            }
            
            @Override
            public void columnHeaderClicked(final int column) {
            }
        });
        this.activeFileFilter = FileSelector.AllFilesFilter;
        this.fileFiltersModel = new FileFiltersModel();
        (this.fileFilterBox = (ComboBox<String>)new ComboBox((ListModel)this.fileFiltersModel)).setTheme("fileFiltersBox");
        this.fileFilterBox.setComputeWidthFromModel(true);
        this.fileFilterBox.setVisible(false);
        this.fileFilterBox.addCallback((Runnable)new Runnable() {
            @Override
            public void run() {
                FileSelector.this.fileFilterChanged();
            }
        });
        (this.labelCurrentFolder = new Label("Folder")).setLabelFor((Widget)this.currentFolder);
        this.fileTableSP = new ScrollPane(this.fileTable);
        final Runnable showBtnCallback = new Runnable() {
            @Override
            public void run() {
                FileSelector.this.refreshFileTable();
            }
        };
        (this.btnRefresh = new Button()).setTheme("buttonRefresh");
        this.btnRefresh.addCallback(showBtnCallback);
        (this.btnShowFolders = new Button((ButtonModel)new ToggleButtonModel(new BitfieldBooleanModel(this.flags, 0), true))).setTheme("buttonShowFolders");
        this.btnShowFolders.addCallback(showBtnCallback);
        (this.btnShowHidden = new Button((ButtonModel)new ToggleButtonModel(new BitfieldBooleanModel(this.flags, 1), false))).setTheme("buttonShowHidden");
        this.btnShowHidden.addCallback(showBtnCallback);
        this.addActionMapping("goOneLevelUp", "goOneLevelUp", new Object[0]);
        this.addActionMapping("acceptSelection", "acceptSelection", new Object[0]);
    }
    
    protected void createLayout() {
        this.setHorizontalGroup((DialogLayout.Group)null);
        this.setVerticalGroup((DialogLayout.Group)null);
        this.removeAllChildren();
        this.add((Widget)this.fileTableSP);
        this.add((Widget)this.fileFilterBox);
        this.add((Widget)this.btnOk);
        this.add((Widget)this.btnCancel);
        this.add((Widget)this.btnRefresh);
        this.add((Widget)this.btnShowFolders);
        this.add((Widget)this.btnShowHidden);
        this.add((Widget)this.labelCurrentFolder);
        this.add((Widget)this.currentFolder);
        this.add((Widget)this.btnFolderMRU);
        this.add((Widget)this.btnUp);
        final DialogLayout.Group hCurrentFolder = this.createSequentialGroup().addWidget((Widget)this.labelCurrentFolder).addWidget((Widget)this.currentFolder).addWidget((Widget)this.btnFolderMRU).addWidget((Widget)this.btnUp).addWidget((Widget)this.btnHome);
        final DialogLayout.Group vCurrentFolder = this.createParallelGroup().addWidget((Widget)this.labelCurrentFolder).addWidget((Widget)this.currentFolder).addWidget((Widget)this.btnFolderMRU).addWidget((Widget)this.btnUp).addWidget((Widget)this.btnHome);
        final DialogLayout.Group hButtonGroup = this.createSequentialGroup().addWidget((Widget)this.btnRefresh).addGap(-2).addWidget((Widget)this.btnShowFolders).addWidget((Widget)this.btnShowHidden).addWidget((Widget)this.fileFilterBox).addGap("buttonBarLeft").addWidget((Widget)this.btnFilesMRU).addGap("buttonBarSpacer").addWidget((Widget)this.btnOk).addGap("buttonBarSpacer").addWidget((Widget)this.btnCancel).addGap("buttonBarRight");
        final DialogLayout.Group vButtonGroup = this.createParallelGroup().addWidget((Widget)this.btnRefresh).addWidget((Widget)this.btnShowFolders).addWidget((Widget)this.btnShowHidden).addWidget((Widget)this.fileFilterBox).addWidget((Widget)this.btnFilesMRU).addWidget((Widget)this.btnOk).addWidget((Widget)this.btnCancel);
        DialogLayout.Group horz = this.createParallelGroup().addGroup(hCurrentFolder).addWidget((Widget)this.fileTableSP);
        DialogLayout.Group vert = this.createSequentialGroup().addGroup(vCurrentFolder).addWidget((Widget)this.fileTableSP);
        if (this.userWidgetBottom != null) {
            horz.addWidget(this.userWidgetBottom);
            vert.addWidget(this.userWidgetBottom);
        }
        if (this.userWidgetRight != null) {
            horz = this.createParallelGroup().addGroup(this.createSequentialGroup().addGroup(horz).addWidget(this.userWidgetRight));
            vert = this.createSequentialGroup().addGroup(this.createParallelGroup().addGroup(vert).addWidget(this.userWidgetRight));
        }
        this.setHorizontalGroup(horz.addGroup(hButtonGroup));
        this.setVerticalGroup(vert.addGroup(vButtonGroup));
    }
    
    protected void afterAddToGUI(final GUI gui) {
        super.afterAddToGUI(gui);
        this.createLayout();
    }
    
    public FileSystemModel getFileSystemModel() {
        return this.fsm;
    }
    
    public void setFileSystemModel(final FileSystemModel fsm) {
        this.fsm = fsm;
        if (fsm == null) {
            this.model = null;
            this.currentFolder.setModel(null);
            this.fileTable.setCurrentFolder(null, null);
            this.autoCompletion.setDataSource((AutoCompletionDataSource)null);
        }
        else {
            (this.model = new FileSystemTreeModel(fsm)).setSorter(new NameSorter(fsm));
            this.currentFolder.setModel(this.model);
            this.currentFolder.setSeparator(fsm.getSeparator());
            this.autoCompletion.setDataSource((AutoCompletionDataSource)new FileSystemAutoCompletionDataSource(fsm, FileSystemTreeModel.FolderFilter.instance));
            if (!this.gotoFolderFromMRU(0) && !this.goHome()) {
                this.setCurrentNode(this.model);
            }
        }
    }
    
    public boolean getAllowMultiSelection() {
        return this.fileTable.getAllowMultiSelection();
    }
    
    public void setAllowMultiSelection(final boolean allowMultiSelection) {
        this.fileTable.setAllowMultiSelection(allowMultiSelection);
    }
    
    public boolean getAllowFolderSelection() {
        return this.allowFolderSelection;
    }
    
    public void setAllowFolderSelection(final boolean allowFolderSelection) {
        this.allowFolderSelection = allowFolderSelection;
        this.selectionChanged();
    }
    
    public boolean getAllowHorizontalScrolling() {
        return this.fileTableSP.getFixed() != ScrollPane.Fixed.HORIZONTAL;
    }
    
    public void setAllowHorizontalScrolling(final boolean allowHorizontalScrolling) {
        this.fileTableSP.setFixed(allowHorizontalScrolling ? ScrollPane.Fixed.NONE : ScrollPane.Fixed.HORIZONTAL);
    }
    
    public void addCallback(final Callback callback) {
        this.callbacks = CallbackSupport.addCallbackToList(this.callbacks, callback, Callback.class);
    }
    
    public void removeCallback(final Callback callback) {
        this.callbacks = CallbackSupport.removeCallbackFromList(this.callbacks, callback);
    }
    
    public Widget getUserWidgetBottom() {
        return this.userWidgetBottom;
    }
    
    public void setUserWidgetBottom(final Widget userWidgetBottom) {
        this.userWidgetBottom = userWidgetBottom;
        this.createLayout();
    }
    
    public Widget getUserWidgetRight() {
        return this.userWidgetRight;
    }
    
    public void setUserWidgetRight(final Widget userWidgetRight) {
        this.userWidgetRight = userWidgetRight;
        this.createLayout();
    }
    
    public FileTable getFileTable() {
        return this.fileTable;
    }
    
    public void setOkButtonEnabled(final boolean enabled) {
        this.btnOk.setEnabled(enabled);
    }
    
    public Object getCurrentFolder() {
        final Object node = this.currentFolder.getCurrentNode();
        if (node instanceof FileSystemTreeModel.FolderNode) {
            return ((FileSystemTreeModel.FolderNode)node).getFolder();
        }
        return null;
    }
    
    public boolean setCurrentFolder(final Object folder) {
        final FileSystemTreeModel.FolderNode node = this.model.getNodeForFolder(folder);
        if (node != null) {
            this.setCurrentNode(node);
            return true;
        }
        return false;
    }
    
    public boolean selectFile(final Object file) {
        if (this.fsm == null) {
            return false;
        }
        final Object parent = this.fsm.getParent(file);
        return this.setCurrentFolder(parent) && this.fileTable.setSelection(file);
    }
    
    public void clearSelection() {
        this.fileTable.clearSelection();
    }
    
    public void addFileFilter(final NamedFileFilter filter) {
        if (filter == null) {
            throw new NullPointerException("filter");
        }
        this.fileFiltersModel.addFileFilter(filter);
        this.fileFilterBox.setVisible(this.fileFiltersModel.getNumEntries() > 0);
        if (this.fileFilterBox.getSelected() < 0) {
            this.fileFilterBox.setSelected(0);
        }
    }
    
    public void removeFileFilter(final NamedFileFilter filter) {
        if (filter == null) {
            throw new NullPointerException("filter");
        }
        this.fileFiltersModel.removeFileFilter(filter);
        if (this.fileFiltersModel.getNumEntries() == 0) {
            this.fileFilterBox.setVisible(false);
            this.setFileFilter(FileSelector.AllFilesFilter);
        }
    }
    
    public void removeAllFileFilters() {
        this.fileFiltersModel.removeAll();
        this.fileFilterBox.setVisible(false);
        this.setFileFilter(FileSelector.AllFilesFilter);
    }
    
    public void setFileFilter(final NamedFileFilter filter) {
        if (filter == null) {
            throw new NullPointerException("filter");
        }
        final int idx = this.fileFiltersModel.findFilter(filter);
        if (idx < 0) {
            throw new IllegalArgumentException("filter not registered");
        }
        this.fileFilterBox.setSelected(idx);
    }
    
    public NamedFileFilter getFileFilter() {
        return this.activeFileFilter;
    }
    
    public boolean getShowFolders() {
        return this.btnShowFolders.getModel().isSelected();
    }
    
    public void setShowFolders(final boolean showFolders) {
        this.btnShowFolders.getModel().setSelected(showFolders);
    }
    
    public boolean getShowHidden() {
        return this.btnShowHidden.getModel().isSelected();
    }
    
    public void setShowHidden(final boolean showHidden) {
        this.btnShowHidden.getModel().setSelected(showHidden);
    }
    
    public void goOneLevelUp() {
        final TreeTableNode node = this.currentFolder.getCurrentNode();
        final TreeTableNode parent = node.getParent();
        if (parent != null) {
            this.setCurrentNode(parent, node);
        }
    }
    
    public boolean goHome() {
        if (this.fsm != null) {
            final Object folder = this.fsm.getSpecialFolder("user.home");
            if (folder != null) {
                return this.setCurrentFolder(folder);
            }
        }
        return false;
    }
    
    public void acceptSelection() {
        final FileTable.Entry[] selection = this.fileTable.getSelection();
        if (selection.length == 1) {
            final FileTable.Entry entry = selection[0];
            if (entry != null && entry.isFolder) {
                this.setCurrentFolder(entry.obj);
                return;
            }
        }
        this.fireAcceptCallback(selection);
    }
    
    void fileFilterChanged() {
        final int idx = this.fileFilterBox.getSelected();
        if (idx >= 0) {
            final NamedFileFilter filter = this.fileFiltersModel.getFileFilter(idx);
            this.activeFileFilter = filter;
            this.fileTable.setFileFilter(filter.getFileFilter());
        }
    }
    
    void fireAcceptCallback(final FileTable.Entry[] selection) {
        if (this.callbacks != null) {
            final Object[] objects = new Object[selection.length];
            for (int i = 0; i < selection.length; ++i) {
                final FileTable.Entry e = selection[i];
                if (e.isFolder && !this.allowFolderSelection) {
                    return;
                }
                objects[i] = e.obj;
            }
            this.addToMRU(selection);
            for (final Callback cb : this.callbacks) {
                cb.filesSelected(objects);
            }
        }
    }
    
    void fireCanceled() {
        if (this.callbacks != null) {
            for (final Callback cb : this.callbacks) {
                cb.canceled();
            }
        }
    }
    
    void selectionChanged() {
        boolean foldersSelected = false;
        boolean filesSelected = false;
        final FileTable.Entry[] arr$;
        final FileTable.Entry[] selection = arr$ = this.fileTable.getSelection();
        for (final FileTable.Entry entry : arr$) {
            if (entry.isFolder) {
                foldersSelected = true;
            }
            else {
                filesSelected = true;
            }
        }
        if (this.allowFolderSelection) {
            this.btnOk.setEnabled(filesSelected || foldersSelected);
        }
        else {
            this.btnOk.setEnabled(filesSelected && !foldersSelected);
        }
        if (this.callbacks != null) {
            for (final Callback cb : this.callbacks) {
                if (cb instanceof Callback2) {
                    ((Callback2)cb).selectionChanged(selection);
                }
            }
        }
    }
    
    protected void setCurrentNode(final TreeTableNode node, final TreeTableNode childToSelect) {
        if (childToSelect instanceof FileSystemTreeModel.FolderNode) {
            this.fileToSelectOnSetCurrentNode = ((FileSystemTreeModel.FolderNode)childToSelect).getFolder();
        }
        this.setCurrentNode(node);
    }
    
    protected void setCurrentNode(final TreeTableNode node) {
        this.currentFolder.setCurrentNode(node);
        this.refreshFileTable();
        if (this.callbacks != null) {
            final Object curFolder = this.getCurrentFolder();
            for (final Callback cb : this.callbacks) {
                if (cb instanceof Callback2) {
                    ((Callback2)cb).folderChanged(curFolder);
                }
            }
        }
        if (this.fileToSelectOnSetCurrentNode != null) {
            this.fileTable.setSelection(this.fileToSelectOnSetCurrentNode);
            this.fileToSelectOnSetCurrentNode = null;
        }
    }
    
    void refreshFileTable() {
        this.fileTable.setShowFolders(this.btnShowFolders.getModel().isSelected());
        this.fileTable.setShowHidden(this.btnShowHidden.getModel().isSelected());
        this.fileTable.setCurrentFolder(this.fsm, this.getCurrentFolder());
    }
    
    TreeTableNode resolvePath(final String path) throws IllegalArgumentException {
        Object obj = this.fsm.getFile(path);
        this.fileToSelectOnSetCurrentNode = null;
        if (obj != null) {
            if (this.fsm.isFile(obj)) {
                this.fileToSelectOnSetCurrentNode = obj;
                obj = this.fsm.getParent(obj);
            }
            final FileSystemTreeModel.FolderNode node = this.model.getNodeForFolder(obj);
            if (node != null) {
                return node;
            }
        }
        throw new IllegalArgumentException("Could not resolve: " + path);
    }
    
    void showFolderMRU() {
        final PopupWindow popup = new PopupWindow((Widget)this);
        final ListBox<String> listBox = new ListBox<String>(this.folderMRU);
        popup.setTheme("fileselector-folderMRUpopup");
        popup.add((Widget)listBox);
        if (popup.openPopup()) {
            popup.setInnerSize(this.getInnerWidth() * 2 / 3, this.getInnerHeight() * 2 / 3);
            popup.setPosition(this.btnFolderMRU.getX() - popup.getWidth(), this.btnFolderMRU.getY());
            listBox.addCallback((CallbackWithReason<ListBox.CallbackReason>)new CallbackWithReason<ListBox.CallbackReason>() {
                public void callback(final ListBox.CallbackReason reason) {
                    if (reason.actionRequested()) {
                        popup.closePopup();
                        final int idx = listBox.getSelected();
                        if (idx >= 0) {
                            FileSelector.this.gotoFolderFromMRU(idx);
                        }
                    }
                }
            });
        }
    }
    
    void showFilesMRU() {
        final PopupWindow popup = new PopupWindow((Widget)this);
        final DialogLayout layout = new DialogLayout();
        final ListBox<String> listBox = new ListBox<String>(this.filesMRU);
        final Button popupBtnOk = new Button();
        final Button popupBtnCancel = new Button();
        popupBtnOk.setTheme("buttonOk");
        popupBtnCancel.setTheme("buttonCancel");
        popup.setTheme("fileselector-filesMRUpopup");
        popup.add((Widget)layout);
        layout.add((Widget)listBox);
        layout.add((Widget)popupBtnOk);
        layout.add((Widget)popupBtnCancel);
        final DialogLayout.Group hBtnGroup = layout.createSequentialGroup().addGap().addWidget((Widget)popupBtnOk).addWidget((Widget)popupBtnCancel);
        final DialogLayout.Group vBtnGroup = layout.createParallelGroup().addWidget((Widget)popupBtnOk).addWidget((Widget)popupBtnCancel);
        layout.setHorizontalGroup(layout.createParallelGroup().addWidget((Widget)listBox).addGroup(hBtnGroup));
        layout.setVerticalGroup(layout.createSequentialGroup().addWidget((Widget)listBox).addGroup(vBtnGroup));
        if (popup.openPopup()) {
            popup.setInnerSize(this.getInnerWidth() * 2 / 3, this.getInnerHeight() * 2 / 3);
            popup.setPosition(this.getInnerX() + (this.getInnerWidth() - popup.getWidth()) / 2, this.btnFilesMRU.getY() - popup.getHeight());
            final Runnable okCB = new Runnable() {
                @Override
                public void run() {
                    final int idx = listBox.getSelected();
                    if (idx >= 0) {
                        final Object obj = FileSelector.this.fsm.getFile(FileSelector.this.filesMRU.getEntry(idx));
                        if (obj != null) {
                            popup.closePopup();
                            FileSelector.this.fireAcceptCallback(new FileTable.Entry[] { new FileTable.Entry(FileSelector.this.fsm, obj, FileSelector.this.fsm.getParent(obj) == null) });
                        }
                        else {
                            FileSelector.this.filesMRU.removeEntry(idx);
                        }
                    }
                }
            };
            popupBtnOk.addCallback(okCB);
            popupBtnCancel.addCallback((Runnable)new Runnable() {
                @Override
                public void run() {
                    popup.closePopup();
                }
            });
            listBox.addCallback((CallbackWithReason<ListBox.CallbackReason>)new CallbackWithReason<ListBox.CallbackReason>() {
                public void callback(final ListBox.CallbackReason reason) {
                    if (reason.actionRequested()) {
                        okCB.run();
                    }
                }
            });
        }
    }
    
    private void addToMRU(final FileTable.Entry[] selection) {
        for (final FileTable.Entry entry : selection) {
            this.filesMRU.addEntry(entry.getPath());
        }
        this.folderMRU.addEntry(this.fsm.getPath(this.getCurrentFolder()));
    }
    
    boolean gotoFolderFromMRU(final int idx) {
        if (idx >= this.folderMRU.getNumEntries()) {
            return false;
        }
        final String path = this.folderMRU.getEntry(idx);
        try {
            final TreeTableNode node = this.resolvePath(path);
            this.setCurrentNode(node);
            return true;
        }
        catch (IllegalArgumentException ex) {
            this.folderMRU.removeEntry(idx);
            return false;
        }
    }
    
    static {
        AllFilesFilter = new NamedFileFilter("All files", null);
    }
    
    public static class NamedFileFilter
    {
        private final String name;
        private final FileSystemModel.FileFilter fileFilter;
        
        public NamedFileFilter(final String name, final FileSystemModel.FileFilter fileFilter) {
            this.name = name;
            this.fileFilter = fileFilter;
        }
        
        public String getDisplayName() {
            return this.name;
        }
        
        public FileSystemModel.FileFilter getFileFilter() {
            return this.fileFilter;
        }
    }
    
    static class FileFiltersModel extends SimpleListModel<String>
    {
        private final ArrayList<NamedFileFilter> filters;
        
        FileFiltersModel() {
            this.filters = new ArrayList<NamedFileFilter>();
        }
        
        public NamedFileFilter getFileFilter(final int index) {
            return this.filters.get(index);
        }
        
        @Override
        public String getEntry(final int index) {
            final NamedFileFilter filter = this.getFileFilter(index);
            return filter.getDisplayName();
        }
        
        @Override
        public int getNumEntries() {
            return this.filters.size();
        }
        
        public void addFileFilter(final NamedFileFilter filter) {
            final int index = this.filters.size();
            this.filters.add(filter);
            this.fireEntriesInserted(index, index);
        }
        
        public void removeFileFilter(final NamedFileFilter filter) {
            final int idx = this.filters.indexOf(filter);
            if (idx >= 0) {
                this.filters.remove(idx);
                this.fireEntriesDeleted(idx, idx);
            }
        }
        
        public int findFilter(final NamedFileFilter filter) {
            return this.filters.indexOf(filter);
        }
        
        void removeAll() {
            this.filters.clear();
            this.fireAllChanged();
        }
    }
    
    public static class NameSorter implements Comparator<Object>
    {
        private final FileSystemModel fsm;
        private final Comparator<String> nameComparator;
        
        public NameSorter(final FileSystemModel fsm) {
            this.fsm = fsm;
            this.nameComparator = NaturalSortComparator.stringComparator;
        }
        
        public NameSorter(final FileSystemModel fsm, final Comparator<String> nameComparator) {
            this.fsm = fsm;
            this.nameComparator = nameComparator;
        }
        
        @Override
        public int compare(final Object o1, final Object o2) {
            return this.nameComparator.compare(this.fsm.getName(o1), this.fsm.getName(o2));
        }
    }
    
    public interface Callback2 extends Callback
    {
        void folderChanged(final Object p0);
        
        void selectionChanged(final FileTable.Entry[] p0);
    }
    
    public interface Callback
    {
        void filesSelected(final Object[] p0);
        
        void canceled();
    }
}
