package de.matthiasmann.twl.model;

import java.util.*;

public class FileSystemTreeModel extends AbstractTreeTableModel
{
    private final FileSystemModel fsm;
    private final boolean includeLastModified;
    protected Comparator<Object> sorter;
    static final FolderNode[] NO_CHILDREN;
    
    public FileSystemTreeModel(final FileSystemModel fsm, final boolean includeLastModified) {
        this.fsm = fsm;
        this.includeLastModified = includeLastModified;
        this.insertRoots();
    }
    
    public FileSystemTreeModel(final FileSystemModel fsm) {
        this(fsm, false);
    }
    
    public int getNumColumns() {
        return this.includeLastModified ? 2 : 1;
    }
    
    public String getColumnHeaderText(final int column) {
        switch (column) {
            case 0: {
                return "Folder";
            }
            case 1: {
                return "Last modified";
            }
            default: {
                return "";
            }
        }
    }
    
    public FileSystemModel getFileSystemModel() {
        return this.fsm;
    }
    
    public void insertRoots() {
        this.removeAllChildren();
        for (final Object root : this.fsm.listRoots()) {
            this.insertChild((TreeTableNode)new FolderNode((TreeTableNode)this, this.fsm, root), this.getNumChildren());
        }
    }
    
    public FolderNode getNodeForFolder(final Object obj) {
        final Object parent = this.fsm.getParent(obj);
        TreeTableNode parentNode;
        if (parent == null) {
            parentNode = (TreeTableNode)this;
        }
        else {
            parentNode = this.getNodeForFolder(parent);
        }
        if (parentNode != null) {
            for (int i = 0; i < parentNode.getNumChildren(); ++i) {
                final FolderNode node = (FolderNode)parentNode.getChild(i);
                if (this.fsm.equals(node.folder, obj)) {
                    return node;
                }
            }
        }
        return null;
    }
    
    public Comparator<Object> getSorter() {
        return this.sorter;
    }
    
    public void setSorter(final Comparator<Object> sorter) {
        if (this.sorter != sorter) {
            this.sorter = sorter;
            this.insertRoots();
        }
    }
    
    static {
        NO_CHILDREN = new FolderNode[0];
    }
    
    public static class FolderNode implements TreeTableNode
    {
        private final TreeTableNode parent;
        private final FileSystemModel fsm;
        final Object folder;
        FolderNode[] children;
        
        protected FolderNode(final TreeTableNode parent, final FileSystemModel fsm, final Object folder) {
            this.parent = parent;
            this.fsm = fsm;
            this.folder = folder;
        }
        
        public Object getFolder() {
            return this.folder;
        }
        
        @Override
        public Object getData(final int column) {
            switch (column) {
                case 0: {
                    return this.fsm.getName(this.folder);
                }
                case 1: {
                    return this.getlastModified();
                }
                default: {
                    return null;
                }
            }
        }
        
        @Override
        public Object getTooltipContent(final int column) {
            final StringBuilder sb = new StringBuilder(this.fsm.getPath(this.folder));
            final Date lastModified = this.getlastModified();
            if (lastModified != null) {
                sb.append("\nLast modified: ").append(lastModified);
            }
            return sb.toString();
        }
        
        @Override
        public TreeTableNode getChild(final int idx) {
            return this.children[idx];
        }
        
        @Override
        public int getChildIndex(final TreeTableNode child) {
            for (int i = 0, n = this.children.length; i < n; ++i) {
                if (this.children[i] == child) {
                    return i;
                }
            }
            return -1;
        }
        
        @Override
        public int getNumChildren() {
            if (this.children == null) {
                this.collectChilds();
            }
            return this.children.length;
        }
        
        @Override
        public TreeTableNode getParent() {
            return this.parent;
        }
        
        @Override
        public boolean isLeaf() {
            return false;
        }
        
        public FileSystemTreeModel getTreeModel() {
            TreeTableNode node;
            TreeTableNode nodeParent;
            for (node = this.parent; (nodeParent = node.getParent()) != null; node = nodeParent) {}
            return (FileSystemTreeModel)node;
        }
        
        private void collectChilds() {
            this.children = FileSystemTreeModel.NO_CHILDREN;
            try {
                final Object[] subFolder = this.fsm.listFolder(this.folder, (FileSystemModel.FileFilter)FolderFilter.instance);
                if (subFolder != null && subFolder.length > 0) {
                    final Comparator<Object> sorter = this.getTreeModel().sorter;
                    if (sorter != null) {
                        Arrays.sort(subFolder, sorter);
                    }
                    final FolderNode[] newChildren = new FolderNode[subFolder.length];
                    for (int i = 0; i < subFolder.length; ++i) {
                        newChildren[i] = new FolderNode(this, this.fsm, subFolder[i]);
                    }
                    this.children = newChildren;
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        private Date getlastModified() {
            if (this.parent instanceof FileSystemTreeModel) {
                return null;
            }
            return new Date(this.fsm.getLastModified(this.folder));
        }
    }
    
    public static final class FolderFilter implements FileSystemModel.FileFilter
    {
        public static final FolderFilter instance;
        
        public boolean accept(final FileSystemModel model, final Object file) {
            return model.isFolder(file);
        }
        
        static {
            instance = new FolderFilter();
        }
    }
}
