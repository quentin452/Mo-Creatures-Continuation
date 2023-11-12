package de.matthiasmann.twl.model;

import java.nio.channels.*;
import java.io.*;

public class JavaFileSystemModel implements FileSystemModel
{
    private static final JavaFileSystemModel instance;

    public static JavaFileSystemModel getInstance() {
        return JavaFileSystemModel.instance;
    }

    public String getSeparator() {
        return File.separator;
    }

    public Object getFile(final String path) {
        final File file = new File(path);
        return file.exists() ? file : null;
    }

    public Object getParent(final Object file) {
        return ((File)file).getParentFile();
    }

    public boolean isFolder(final Object file) {
        return ((File)file).isDirectory();
    }

    public boolean isFile(final Object file) {
        return ((File)file).isFile();
    }

    public boolean isHidden(final Object file) {
        return ((File)file).isHidden();
    }

    public String getName(final Object file) {
        final String name = ((File)file).getName();
        if (name.length() == 0) {
            return file.toString();
        }
        return name;
    }

    public String getPath(final Object file) {
        return ((File)file).getPath();
    }

    public String getRelativePath(final Object from, final Object to) {
        return getRelativePath((FileSystemModel)this, from, to);
    }

    public static String getRelativePath(final FileSystemModel fsm, Object from, Object to) {
        int levelFrom = countLevel(fsm, from);
        int levelTo = countLevel(fsm, to);
        int prefixes = 0;
        final StringBuilder sb = new StringBuilder();
        while (!fsm.equals(from, to)) {
            final int diff = levelTo - levelFrom;
            if (diff <= 0) {
                ++prefixes;
                --levelFrom;
                from = fsm.getParent(from);
            }
            if (diff >= 0) {
                sb.insert(0, '/');
                sb.insert(0, fsm.getName(to));
                --levelTo;
                to = fsm.getParent(to);
            }
        }
        while (prefixes-- > 0) {
            sb.insert(0, "../");
        }
        return sb.toString();
    }

    public static int countLevel(final FileSystemModel fsm, Object file) {
        int level;
        for (level = 0; file != null; file = fsm.getParent(file), ++level) {}
        return level;
    }

    public static int countLevel(final FileSystemModel fsm, final Object parent, Object child) {
        int level;
        for (level = 0; fsm.equals(child, parent); child = fsm.getParent(child), ++level) {
            if (child == null) {
                return -1;
            }
        }
        return level;
    }

    public long getLastModified(final Object file) {
        try {
            return ((File)file).lastModified();
        }
        catch (Throwable ex) {
            return -1L;
        }
    }

    public long getSize(final Object file) {
        try {
            return ((File)file).length();
        }
        catch (Throwable ex) {
            return -1L;
        }
    }

    public boolean equals(final Object file1, final Object file2) {
        return file1 != null && file1.equals(file2);
    }

    public int find(final Object[] list, final Object file) {
        if (file == null) {
            return -1;
        }
        for (int i = 0; i < list.length; ++i) {
            if (file.equals(list[i])) {
                return i;
            }
        }
        return -1;
    }

    public Object[] listRoots() {
        return File.listRoots();
    }

    public Object[] listFolder(final Object file, final FileSystemModel.FileFilter filter) {
        try {
            if (filter == null) {
                return ((File)file).listFiles();
            }
            return ((File) file).listFiles((dir, name) -> filter.accept(JavaFileSystemModel.this, new File(dir, name)));
        }
        catch (Throwable ex) {
            return null;
        }
    }

    public Object getSpecialFolder(final String key) {
        File file = null;
        if ("user.home".equals(key)) {
            try {
                file = new File(System.getProperty("user.home"));
            }
            catch (SecurityException ex) {}
        }
        if (file != null && file.canRead() && file.isDirectory()) {
            return file;
        }
        return null;
    }

    public ReadableByteChannel openChannel(final Object file) throws IOException {
        return new FileInputStream((File)file).getChannel();
    }

    public InputStream openStream(final Object file) throws IOException {
        return new FileInputStream((File)file);
    }

    static {
        instance = new JavaFileSystemModel();
    }
}
