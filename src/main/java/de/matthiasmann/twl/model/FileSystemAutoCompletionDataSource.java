package de.matthiasmann.twl.model;

import java.util.*;

public class FileSystemAutoCompletionDataSource implements AutoCompletionDataSource
{
    final FileSystemModel fsm;
    final FileSystemModel.FileFilter fileFilter;
    
    public FileSystemAutoCompletionDataSource(final FileSystemModel fsm, final FileSystemModel.FileFilter fileFilter) {
        if (fsm == null) {
            throw new NullPointerException("fsm");
        }
        this.fsm = fsm;
        this.fileFilter = fileFilter;
    }
    
    public FileSystemModel getFileSystemModel() {
        return this.fsm;
    }
    
    public FileSystemModel.FileFilter getFileFilter() {
        return this.fileFilter;
    }
    
    public AutoCompletionResult collectSuggestions(String text, final int cursorPos, final AutoCompletionResult prev) {
        text = text.substring(0, cursorPos);
        final int prefixLength = this.computePrefixLength(text);
        final String prefix = text.substring(0, prefixLength);
        Object parent;
        if (prev instanceof Result && prev.getPrefixLength() == prefixLength && prev.getText().startsWith(prefix)) {
            parent = ((Result)prev).parent;
        }
        else {
            parent = this.fsm.getFile(prefix);
        }
        if (parent == null) {
            return null;
        }
        final Result result = new Result(text, prefixLength, parent);
        this.fsm.listFolder(parent, result);
        if (result.getNumResults() == 0) {
            return null;
        }
        return result;
    }
    
    int computePrefixLength(final String text) {
        final String separator = this.fsm.getSeparator();
        int prefixLength = text.lastIndexOf(separator) + separator.length();
        if (prefixLength < 0) {
            prefixLength = 0;
        }
        return prefixLength;
    }
    
    class Result extends AutoCompletionResult implements FileSystemModel.FileFilter
    {
        final Object parent;
        final String nameFilter;
        final ArrayList<String> results1;
        final ArrayList<String> results2;
        
        public Result(final String text, final int prefixLength, final Object parent) {
            super(text, prefixLength);
            this.results1 = new ArrayList<String>();
            this.results2 = new ArrayList<String>();
            this.parent = parent;
            this.nameFilter = text.substring(prefixLength).toUpperCase();
        }
        
        public boolean accept(final FileSystemModel fsm, final Object file) {
            final FileSystemModel.FileFilter ff = FileSystemAutoCompletionDataSource.this.fileFilter;
            if (ff == null || ff.accept(fsm, file)) {
                final int idx = this.getMatchIndex(fsm.getName(file));
                if (idx >= 0) {
                    this.addName(fsm.getPath(file), idx);
                }
            }
            return false;
        }
        
        private int getMatchIndex(final String partName) {
            return partName.toUpperCase().indexOf(this.nameFilter);
        }
        
        private void addName(final String fullName, final int matchIdx) {
            if (matchIdx == 0) {
                this.results1.add(fullName);
            }
            else if (matchIdx > 0) {
                this.results2.add(fullName);
            }
        }
        
        private void addFiltedNames(final ArrayList<String> results) {
            for (int i = 0, n = results.size(); i < n; ++i) {
                final String fullName = results.get(i);
                final int idx = this.getMatchIndex(fullName.substring(this.prefixLength));
                this.addName(fullName, idx);
            }
        }
        
        public int getNumResults() {
            return this.results1.size() + this.results2.size();
        }
        
        public String getResult(final int idx) {
            final int size1 = this.results1.size();
            if (idx >= size1) {
                return this.results2.get(idx - size1);
            }
            return this.results1.get(idx);
        }
        
        boolean canRefine(final String text) {
            return this.prefixLength == FileSystemAutoCompletionDataSource.this.computePrefixLength(text) && text.startsWith(this.text);
        }
        
        public AutoCompletionResult refine(String text, final int cursorPos) {
            text = text.substring(0, cursorPos);
            if (this.canRefine(text)) {
                final Result result = new Result(text, this.prefixLength, this.parent);
                result.addFiltedNames(this.results1);
                result.addFiltedNames(this.results2);
                return result;
            }
            return null;
        }
    }
}
