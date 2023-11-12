package de.matthiasmann.twl.textarea;

import de.matthiasmann.twl.model.*;
import java.util.*;

public class SimpleTextAreaModel extends HasCallback implements TextAreaModel
{
    private Style style;
    private Element element;

    public SimpleTextAreaModel() {
        this.style = new Style();
    }

    public SimpleTextAreaModel(final String text) {
        this();
        this.setText(text);
    }

    public Style getStyle() {
        return this.style;
    }

    public void setStyle(final Style style) {
        if (style == null) {
            throw new NullPointerException("style");
        }
        this.style = style;
    }

    public void setText(final String text) {
        this.setText(text, true);
    }

    public void setText(final String text, final boolean preformatted) {
        final Style textstyle = this.style.with(StyleAttribute.PREFORMATTED, preformatted);
        this.element = new TextElement(textstyle, text);
        this.doCallback();
    }

    public Iterator iterator() {
        return ((this.element != null) ? Collections.singletonList(this.element) : Collections.emptyList()).iterator();
    }
}
