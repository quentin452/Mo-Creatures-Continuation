package de.matthiasmann.twl;

public class MenuSpacer extends MenuElement
{
    protected Widget createMenuWidget(final MenuManager mm, final int level) {
        final Widget w = new Widget();
        this.setWidgetTheme(w, "spacer");
        return w;
    }
}
