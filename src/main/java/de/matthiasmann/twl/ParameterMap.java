package de.matthiasmann.twl;

import de.matthiasmann.twl.renderer.*;

public interface ParameterMap
{
    Font getFont(final String p0);
    
    Image getImage(final String p0);
    
    MouseCursor getMouseCursor(final String p0);
    
    ParameterMap getParameterMap(final String p0);
    
    ParameterList getParameterList(final String p0);
    
    boolean getParameter(final String p0, final boolean p1);
    
    int getParameter(final String p0, final int p1);
    
    float getParameter(final String p0, final float p1);
    
    String getParameter(final String p0, final String p1);
    
    Color getParameter(final String p0, final Color p1);
    
     <E extends Enum<E>> E getParameter(final String p0, final E p1);
    
    Object getParameterValue(final String p0, final boolean p1);
    
     <T> T getParameterValue(final String p0, final boolean p1, final Class<T> p2);
    
     <T> T getParameterValue(final String p0, final boolean p1, final Class<T> p2, final T p3);
}
