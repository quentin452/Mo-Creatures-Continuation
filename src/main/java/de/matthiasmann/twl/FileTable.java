package de.matthiasmann.twl;

import java.util.*;
import de.matthiasmann.twl.model.*;
import java.text.*;
import de.matthiasmann.twl.utils.*;

public class FileTable extends Table
{
    private final FileTableModel fileTableModel;
    private final Runnable selectionChangedListener;
    private TableSelectionModel fileTableSelectionModel;
    private TableSearchWindow tableSearchWindow;
    private SortColumn sortColumn;
    private SortOrder sortOrder;
    private boolean allowMultiSelection;
    private FileSystemModel.FileFilter fileFilter;
    private boolean showFolders;
    private boolean showHidden;
    private FileSystemModel fsm;
    private Object currentFolder;
    private Callback[] fileTableCallbacks;
    static Entry[] EMPTY;

    public FileTable() {
        this.sortColumn = SortColumn.NAME;
        this.sortOrder = SortOrder.ASCENDING;
        this.fileFilter = null;
        this.showFolders = true;
        this.showHidden = false;
        this.setModel(this.fileTableModel = new FileTableModel());
        this.selectionChangedListener = new Runnable() {
            @Override
            public void run() {
                FileTable.this.selectionChanged();
            }
        };
    }

    public void addCallback(final Callback callback) {
        this.fileTableCallbacks = CallbackSupport.addCallbackToList(this.fileTableCallbacks, callback, Callback.class);
    }

    public void removeCallback(final Callback callback) {
        this.fileTableCallbacks = CallbackSupport.removeCallbackFromList(this.fileTableCallbacks, callback);
    }

    public boolean getShowFolders() {
        return this.showFolders;
    }

    public void setShowFolders(final boolean showFolders) {
        if (this.showFolders != showFolders) {
            this.showFolders = showFolders;
            this.refreshFileTable();
        }
    }

    public boolean getShowHidden() {
        return this.showHidden;
    }

    public void setShowHidden(final boolean showHidden) {
        if (this.showHidden != showHidden) {
            this.showHidden = showHidden;
            this.refreshFileTable();
        }
    }

    public void setFileFilter(final FileSystemModel.FileFilter filter) {
        this.fileFilter = filter;
        this.refreshFileTable();
    }

    public FileSystemModel.FileFilter getFileFilter() {
        return this.fileFilter;
    }

    public Entry[] getSelection() {
        return this.fileTableModel.getEntries(this.fileTableSelectionModel.getSelection());
    }

    public void setSelection(final Object... files) {
        this.fileTableSelectionModel.clearSelection();
        for (final Object file : files) {
            final int idx = this.fileTableModel.findFile(file);
            if (idx >= 0) {
                this.fileTableSelectionModel.addSelection(idx, idx);
            }
        }
    }

    public boolean setSelection(final Object file) {
        this.fileTableSelectionModel.clearSelection();
        final int idx = this.fileTableModel.findFile(file);
        if (idx >= 0) {
            this.fileTableSelectionModel.addSelection(idx, idx);
            this.scrollToRow(idx);
            return true;
        }
        return false;
    }

    public void clearSelection() {
        this.fileTableSelectionModel.clearSelection();
    }

    public void setSortColumn(final SortColumn column) {
        if (column == null) {
            throw new NullPointerException("column");
        }
        if (this.sortColumn != column) {
            this.sortColumn = column;
            this.sortingChanged();
        }
    }

    public void setSortOrder(final SortOrder order) {
        if (order == null) {
            throw new NullPointerException("order");
        }
        if (this.sortOrder != order) {
            this.sortOrder = order;
            this.sortingChanged();
        }
    }

    public boolean getAllowMultiSelection() {
        return this.allowMultiSelection;
    }

    public void setAllowMultiSelection(final boolean allowMultiSelection) {
        this.allowMultiSelection = allowMultiSelection;
        if (this.fileTableSelectionModel != null) {
            this.fileTableSelectionModel.removeSelectionChangeListener(this.selectionChangedListener);
        }
        if (this.tableSearchWindow != null) {
            this.tableSearchWindow.setModel(null, 0);
        }
        if (allowMultiSelection) {
            this.fileTableSelectionModel = new DefaultTableSelectionModel();
        }
        else {
            this.fileTableSelectionModel = new TableSingleSelectionModel();
        }
        this.fileTableSelectionModel.addSelectionChangeListener(this.selectionChangedListener);
        (this.tableSearchWindow = new TableSearchWindow(this, this.fileTableSelectionModel)).setModel(this.fileTableModel, 0);
        this.setSelectionManager(new TableRowSelectionManager(this.fileTableSelectionModel));
        this.setKeyboardSearchHandler(this.tableSearchWindow);
        this.selectionChanged();
    }

    public FileSystemModel getFileSystemModel() {
        return this.fsm;
    }

    public final Object getCurrentFolder() {
        return this.currentFolder;
    }

    public final boolean isRoot() {
        return this.currentFolder == null;
    }

    public void setCurrentFolder(final FileSystemModel fsm, final Object folder) {
        this.fsm = fsm;
        this.currentFolder = folder;
        this.refreshFileTable();
    }

    public void refreshFileTable() {
        final Object[] objs = this.collectObjects();
        if (objs != null) {
            int lastFileIdx = objs.length;
            final Entry[] entries = new Entry[lastFileIdx];
            int numFolders = 0;
            final boolean isRoot = this.isRoot();
            for (int i = 0; i < objs.length; ++i) {
                final Entry e = new Entry(this.fsm, objs[i], isRoot);
                if (e.isFolder) {
                    entries[numFolders++] = e;
                }
                else {
                    entries[--lastFileIdx] = e;
                }
            }
            Arrays.sort(entries, 0, numFolders, NameComparator.instance);
            this.sortFilesAndUpdateModel(entries, numFolders);
        }
        else {
            this.sortFilesAndUpdateModel(FileTable.EMPTY, 0);
        }
        if (this.tableSearchWindow != null) {
            this.tableSearchWindow.cancelSearch();
        }
    }

    protected void selectionChanged() {
        if (this.fileTableCallbacks != null) {
            for (final Callback cb : this.fileTableCallbacks) {
                cb.selectionChanged();
            }
        }
    }

    protected void sortingChanged() {
        this.setSortArrows();
        this.sortFilesAndUpdateModel();
        if (this.fileTableCallbacks != null) {
            for (final Callback cb : this.fileTableCallbacks) {
                cb.sortingChanged();
            }
        }
    }

    private Object[] collectObjects() {
        if (this.fsm == null) {
            return null;
        }
        if (this.isRoot()) {
            return this.fsm.listRoots();
        }
        FileSystemModel.FileFilter filter = this.fileFilter;
        if (filter != null || !this.getShowFolders() || !this.getShowHidden()) {
            filter = new FileFilterWrapper(filter, this.getShowFolders(), this.getShowHidden());
        }
        return this.fsm.listFolder(this.currentFolder, filter);
    }

    private void sortFilesAndUpdateModel(final Entry[] entries, final int numFolders) {
        final StateSnapshot snapshot = this.makeSnapshot();
        Arrays.sort(entries, numFolders, entries.length, this.sortOrder.map(this.sortColumn.comparator));
        this.fileTableModel.setData(entries, numFolders);
        this.restoreSnapshot(snapshot);
    }

    @Override
    protected void columnHeaderClicked(final int column) {
        super.columnHeaderClicked(column);
        final SortColumn thisColumn = SortColumn.values()[column];
        if (this.sortColumn == thisColumn) {
            this.setSortOrder(this.sortOrder.invert());
        }
        else {
            this.setSortColumn(thisColumn);
        }
    }

    @Override
    protected void updateColumnHeaderNumbers() {
        super.updateColumnHeaderNumbers();
        this.setSortArrows();
    }

    protected void setSortArrows() {
        this.setColumnSortOrderAnimationState(this.sortColumn.ordinal(), this.sortOrder);
    }

    private void sortFilesAndUpdateModel() {
        this.sortFilesAndUpdateModel(this.fileTableModel.entries, this.fileTableModel.numFolders);
    }

    private StateSnapshot makeSnapshot() {
        return new StateSnapshot(this.fileTableModel.getEntry(this.fileTableSelectionModel.getLeadIndex()), this.fileTableModel.getEntry(this.fileTableSelectionModel.getAnchorIndex()), this.fileTableModel.getEntries(this.fileTableSelectionModel.getSelection()));
    }

    private void restoreSnapshot(final StateSnapshot snapshot) {
        for (final Entry e : snapshot.selected) {
            final int idx = this.fileTableModel.findEntry(e);
            if (idx >= 0) {
                this.fileTableSelectionModel.addSelection(idx, idx);
            }
        }
        final int leadIndex = this.fileTableModel.findEntry(snapshot.leadEntry);
        final int anchorIndex = this.fileTableModel.findEntry(snapshot.anchorEntry);
        this.fileTableSelectionModel.setLeadIndex(leadIndex);
        this.fileTableSelectionModel.setAnchorIndex(anchorIndex);
        this.scrollToRow(Math.max(0, leadIndex));
    }

    static {
        FileTable.EMPTY = new Entry[0];
    }

    public enum SortColumn
    {
        NAME((Comparator<Entry>)NameComparator.instance),
        TYPE((Comparator<Entry>)ExtensionComparator.instance),
        SIZE((Comparator<Entry>)SizeComparator.instance),
        LAST_MODIFIED((Comparator<Entry>)LastModifiedComparator.instance);

        final Comparator<Entry> comparator;

        private SortColumn(final Comparator<Entry> comparator) {
            this.comparator = comparator;
        }
    }

    public static final class Entry
    {
        public final FileSystemModel fsm;
        public final Object obj;
        public final String name;
        public final boolean isFolder;
        public final long size;
        public final Date lastModified;

        public Entry(final FileSystemModel fsm, final Object obj, final boolean isRoot) {
            this.fsm = fsm;
            this.obj = obj;
            this.name = fsm.getName(obj);
            if (isRoot) {
                this.isFolder = true;
                this.lastModified = null;
            }
            else {
                this.isFolder = fsm.isFolder(obj);
                this.lastModified = new Date(fsm.getLastModified(obj));
            }
            if (this.isFolder) {
                this.size = 0L;
            }
            else {
                this.size = fsm.getSize(obj);
            }
        }

        public String getExtension() {
            final int idx = this.name.lastIndexOf(46);
            if (idx >= 0) {
                return this.name.substring(idx + 1);
            }
            return "";
        }

        public String getPath() {
            return this.fsm.getPath(this.obj);
        }

        @Override
        public boolean equals(final Object o) {
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final Entry that = (Entry)o;
            return this.fsm == that.fsm && this.fsm.equals(this.obj, that.obj);
        }

        @Override
        public int hashCode() {
            return (this.obj != null) ? this.obj.hashCode() : 203;
        }
    }

    static class FileTableModel extends AbstractTableModel
    {
        private final DateFormat dateFormat;
        Entry[] entries;
        int numFolders;
        static String[] COLUMN_HEADER;
        static String[] SIZE_UNITS;
        static long[] SIZE_FACTORS;

        FileTableModel() {
            this.dateFormat = DateFormat.getDateInstance();
            this.entries = FileTable.EMPTY;
        }

        public void setData(final Entry[] entries, final int numFolders) {
            this.fireRowsDeleted(0, this.getNumRows());
            this.entries = entries;
            this.numFolders = numFolders;
            this.fireRowsInserted(0, this.getNumRows());
        }

        @Override
        public String getColumnHeaderText(final int column) {
            return FileTableModel.COLUMN_HEADER[column];
        }

        @Override
        public int getNumColumns() {
            return FileTableModel.COLUMN_HEADER.length;
        }

        @Override
        public Object getCell(final int row, final int column) {
            final Entry e = this.entries[row];
            if (e.isFolder) {
                switch (column) {
                    case 0: {
                        return "[" + e.name + "]";
                    }
                    case 1: {
                        return "Folder";
                    }
                    case 2: {
                        return "";
                    }
                    case 3: {
                        return this.formatDate(e.lastModified);
                    }
                    default: {
                        return "??";
                    }
                }
            }
            else {
                switch (column) {
                    case 0: {
                        return e.name;
                    }
                    case 1: {
                        final String ext = e.getExtension();
                        return (ext.length() == 0) ? "File" : (ext + "-file");
                    }
                    case 2: {
                        return this.formatFileSize(e.size);
                    }
                    case 3: {
                        return this.formatDate(e.lastModified);
                    }
                    default: {
                        return "??";
                    }
                }
            }
        }

        @Override
        public Object getTooltipContent(final int row, final int column) {
            final Entry e = this.entries[row];
            final StringBuilder sb = new StringBuilder(e.name);
            if (!e.isFolder) {
                sb.append("\nSize: ").append(this.formatFileSize(e.size));
            }
            if (e.lastModified != null) {
                sb.append("\nLast modified: ").append(this.formatDate(e.lastModified));
            }
            return sb.toString();
        }

        @Override
        public int getNumRows() {
            return this.entries.length;
        }

        Entry getEntry(final int row) {
            if (row >= 0 && row < this.entries.length) {
                return this.entries[row];
            }
            return null;
        }

        int findEntry(final Entry entry) {
            for (int i = 0; i < this.entries.length; ++i) {
                if (this.entries[i].equals(entry)) {
                    return i;
                }
            }
            return -1;
        }

        int findFile(final Object file) {
            for (int i = 0; i < this.entries.length; ++i) {
                final Entry e = this.entries[i];
                if (e.fsm.equals(e.obj, file)) {
                    return i;
                }
            }
            return -1;
        }

        Entry[] getEntries(final int[] selection) {
            final int count = selection.length;
            if (count == 0) {
                return FileTable.EMPTY;
            }
            final Entry[] result = new Entry[count];
            for (int i = 0; i < count; ++i) {
                result[i] = this.entries[selection[i]];
            }
            return result;
        }

        private String formatFileSize(final long size) {
            if (size <= 0L) {
                return "0 B";
            }
            int i;
            for (i = 0; size < FileTableModel.SIZE_FACTORS[i]; ++i) {}
            final long value = size * 10L / FileTableModel.SIZE_FACTORS[i];
            return Long.toString(value / 10L) + '.' + Character.forDigit((int)(value % 10L), 10) + FileTableModel.SIZE_UNITS[i];
        }

        private String formatDate(final Date date) {
            if (date == null) {
                return "";
            }
            return this.dateFormat.format(date);
        }

        static {
            FileTableModel.COLUMN_HEADER = new String[] { "File name", "Type", "Size", "Last modified" };
            FileTableModel.SIZE_UNITS = new String[] { " MB", " KB", " B" };
            FileTableModel.SIZE_FACTORS = new long[] { 1048576L, 1024L, 1L };
        }
    }

    static class StateSnapshot
    {
        final Entry leadEntry;
        final Entry anchorEntry;
        final Entry[] selected;

        StateSnapshot(final Entry leadEntry, final Entry anchorEntry, final Entry[] selected) {
            this.leadEntry = leadEntry;
            this.anchorEntry = anchorEntry;
            this.selected = selected;
        }
    }

    static class NameComparator implements Comparator<Entry>
    {
        static final NameComparator instance;

        @Override
        public int compare(final Entry o1, final Entry o2) {
            return NaturalSortComparator.naturalCompare(o1.name, o2.name);
        }

        static {
            instance = new NameComparator();
        }
    }

    static class ExtensionComparator implements Comparator<Entry>
    {
        static final ExtensionComparator instance;

        @Override
        public int compare(final Entry o1, final Entry o2) {
            return NaturalSortComparator.naturalCompare(o1.getExtension(), o2.getExtension());
        }

        static {
            instance = new ExtensionComparator();
        }
    }

    static class SizeComparator implements Comparator<Entry>
    {
        static final SizeComparator instance;

        @Override
        public int compare(final Entry o1, final Entry o2) {
            return Long.signum(o1.size - o2.size);
        }

        static {
            instance = new SizeComparator();
        }
    }

    static class LastModifiedComparator implements Comparator<Entry>
    {
        static final LastModifiedComparator instance;

        @Override
        public int compare(final Entry o1, final Entry o2) {
            final Date lm1 = o1.lastModified;
            final Date lm2 = o2.lastModified;
            if (lm1 != null && lm2 != null) {
                return lm1.compareTo(lm2);
            }
            if (lm1 != null) {
                return 1;
            }
            if (lm2 != null) {
                return -1;
            }
            return 0;
        }

        static {
            instance = new LastModifiedComparator();
        }
    }

    private static class FileFilterWrapper implements FileSystemModel.FileFilter
    {
        private final FileSystemModel.FileFilter base;
        private final boolean showFolder;
        private final boolean showHidden;

        public FileFilterWrapper(final FileSystemModel.FileFilter base, final boolean showFolder, final boolean showHidden) {
            this.base = base;
            this.showFolder = showFolder;
            this.showHidden = showHidden;
        }

        @Override
        public boolean accept(final FileSystemModel fsm, final Object file) {
            if (!this.showHidden && fsm.isHidden(file)) {
                return false;
            }
            if (fsm.isFolder(file)) {
                return this.showFolder;
            }
            return this.base == null || this.base.accept(fsm, file);
        }
    }

    public interface Callback
    {
        void selectionChanged();

        void sortingChanged();
    }
}
