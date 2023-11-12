package de.matthiasmann.twl.model;

import java.io.*;
import java.nio.channels.*;

public interface FileSystemModel
{
    public static final String SPECIAL_FOLDER_HOME = "user.home";
    
    String getSeparator();
    
    Object getFile(final String p0);
    
    Object getParent(final Object p0);
    
    boolean isFolder(final Object p0);
    
    boolean isFile(final Object p0);
    
    boolean isHidden(final Object p0);
    
    String getName(final Object p0);
    
    String getPath(final Object p0);
    
    String getRelativePath(final Object p0, final Object p1);
    
    long getSize(final Object p0);
    
    long getLastModified(final Object p0);
    
    boolean equals(final Object p0, final Object p1);
    
    int find(final Object[] p0, final Object p1);
    
    Object[] listRoots();
    
    Object[] listFolder(final Object p0, final FileFilter p1);
    
    Object getSpecialFolder(final String p0);
    
    InputStream openStream(final Object p0) throws IOException;
    
    ReadableByteChannel openChannel(final Object p0) throws IOException;
    
    public interface FileFilter
    {
        boolean accept(final FileSystemModel p0, final Object p1);
    }
}
