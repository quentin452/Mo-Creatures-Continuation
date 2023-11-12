package de.matthiasmann.twl.theme;

import de.matthiasmann.twl.utils.*;
import de.matthiasmann.twl.*;

class ThemeInfoImpl extends ParameterMapImpl implements ThemeInfo
{
    private final String name;
    private final CascadedHashMap<String, ThemeInfoImpl> children;
    boolean maybeUsedFromWildcard;
    String wildcardImportPath;
    
    public ThemeInfoImpl(final ThemeManager manager, final String name, final ThemeInfoImpl parent) {
        super(manager, parent);
        this.name = name;
        this.children = new CascadedHashMap<String, ThemeInfoImpl>();
    }
    
    void copy(final ThemeInfoImpl src) {
        super.copy((ParameterMapImpl)src);
        this.children.collapseAndSetFallback(src.children);
        this.wildcardImportPath = src.wildcardImportPath;
    }
    
    public String getName() {
        return this.name;
    }
    
    public ThemeInfo getChildTheme(final String theme) {
        return this.getChildThemeImpl(theme, true);
    }
    
    ThemeInfo getChildThemeImpl(final String theme, final boolean useFallback) {
        ThemeInfo info = this.children.get(theme);
        if (info == null) {
            if (this.wildcardImportPath != null) {
                info = this.manager.resolveWildcard(this.wildcardImportPath, theme, useFallback);
            }
            if (info == null && useFallback) {
                DebugHook.getDebugHook().missingChildTheme((ThemeInfo)this, theme);
            }
        }
        return info;
    }
    
    final ThemeInfoImpl getTheme(final String name) {
        return this.children.get(name);
    }
    
    void putTheme(final String name, final ThemeInfoImpl child) {
        this.children.put(name, child);
    }
    
    public String getThemePath() {
        return this.getThemePath(0).toString();
    }
    
    private StringBuilder getThemePath(int length) {
        length += this.getName().length();
        StringBuilder sb;
        if (this.parent != null) {
            sb = this.parent.getThemePath(length + 1);
            sb.append('.');
        }
        else {
            sb = new StringBuilder(length);
        }
        sb.append(this.getName());
        return sb;
    }
}
