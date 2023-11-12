package de.matthiasmann.twl;

public interface ThemeInfo extends ParameterMap
{
    ThemeInfo getChildTheme(final String p0);
    
    String getThemePath();
}
