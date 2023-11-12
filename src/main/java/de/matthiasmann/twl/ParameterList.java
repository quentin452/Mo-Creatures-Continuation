package de.matthiasmann.twl;

import de.matthiasmann.twl.renderer.*;

public interface ParameterList
{
    int getSize();
    
    Font getFont(final int p0);
    
    Image getImage(final int p0);
    
    MouseCursor getMouseCursor(final int p0);
    
    ParameterMap getParameterMap(final int p0);
    
    ParameterList getParameterList(final int p0);
    
    boolean getParameter(final int p0, final boolean p1);
    
    int getParameter(final int p0, final int p1);
    
    float getParameter(final int p0, final float p1);
    
    String getParameter(final int p0, final String p1);
    
    Color getParameter(final int p0, final Color p1);
    
     <E extends Enum<E>> E getParameter(final int p0, final E p1);
    
    Object getParameterValue(final int p0);
    
     <T> T getParameterValue(final int p0, final Class<T> p1);
}
