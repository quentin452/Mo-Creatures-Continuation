package de.matthiasmann.twl.renderer;

import de.matthiasmann.twl.utils.*;
import java.net.*;
import java.io.*;

public interface FontMapper extends Resource
{
    public static final int STYLE_NORMAL = 0;
    public static final int STYLE_BOLD = 1;
    public static final int STYLE_ITALIC = 2;
    public static final int REGISTER_WEAK = 256;
    
    Font getFont(final StringList p0, final int p1, final int p2, final StateSelect p3, final FontParameter... p4);
    
    boolean registerFont(final String p0, final int p1, final URL p2);
    
    boolean registerFont(final String p0, final URL p1) throws IOException;
}
