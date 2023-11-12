package de.matthiasmann.twl.theme;

class ThemeChildImpl
{
    final ThemeManager manager;
    final ThemeInfoImpl parent;
    
    ThemeChildImpl(final ThemeManager manager, final ThemeInfoImpl parent) {
        this.manager = manager;
        this.parent = parent;
    }
    
    protected String getParentDescription() {
        if (this.parent != null) {
            return ", defined in " + this.parent.getThemePath();
        }
        return "";
    }
}
