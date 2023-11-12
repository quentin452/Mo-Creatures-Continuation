package de.matthiasmann.twl.textarea;

import javax.lang.model.element.Element;
import java.util.*;

public interface TextAreaModel extends Iterable<Element>
{
    void addCallback(final Runnable p0);

    void removeCallback(final Runnable p0);

    public enum HAlignment
    {
        LEFT,
        RIGHT,
        CENTER,
        JUSTIFY;
    }

    public enum Display
    {
        INLINE,
        BLOCK;
    }

    public enum VAlignment
    {
        TOP,
        MIDDLE,
        BOTTOM,
        FILL;
    }

    public enum Clear
    {
        NONE,
        LEFT,
        RIGHT,
        BOTH;
    }

    public enum FloatPosition
    {
        NONE,
        LEFT,
        RIGHT;
    }

    public abstract static class Element
    {
        private Style style;

        protected Element(final Style style) {
            notNull(style, "style");
            this.style = style;
        }

        public Style getStyle() {
            return this.style;
        }

        public void setStyle(final Style style) {
            notNull(style, "style");
            this.style = style;
        }

        static void notNull(final Object o, final String name) {
            if (o == null) {
                throw new NullPointerException(name);
            }
        }
    }

    public static class TextElement extends Element
    {
        private String text;

        public TextElement(final Style style, final String text) {
            super(style);
            Element.notNull(text, "text");
            this.text = text;
        }

        public String getText() {
            return this.text;
        }

        public void setText(final String text) {
            Element.notNull(text, "text");
            this.text = text;
        }
    }

    public static class ImageElement extends Element
    {
        private final String imageName;
        private final String tooltip;

        public ImageElement(final Style style, final String imageName, final String tooltip) {
            super(style);
            this.imageName = imageName;
            this.tooltip = tooltip;
        }

        public ImageElement(final Style style, final String imageName) {
            this(style, imageName, null);
        }

        public String getImageName() {
            return this.imageName;
        }

        public String getToolTip() {
            return this.tooltip;
        }
    }

    public static class WidgetElement extends Element
    {
        private final String widgetName;
        private final String widgetParam;

        public WidgetElement(final Style style, final String widgetName, final String widgetParam) {
            super(style);
            this.widgetName = widgetName;
            this.widgetParam = widgetParam;
        }

        public String getWidgetName() {
            return this.widgetName;
        }

        public String getWidgetParam() {
            return this.widgetParam;
        }
    }

    public static class ContainerElement extends Element implements Iterable<Element>
    {
        protected final ArrayList<Element> children;

        public ContainerElement(final Style style) {
            super(style);
            this.children = new ArrayList<Element>();
        }

        @Override
        public Iterator<Element> iterator() {
            return this.children.iterator();
        }

        public Element getElement(final int index) {
            return this.children.get(index);
        }

        public int getNumElements() {
            return this.children.size();
        }

        public void add(final Element element) {
            this.children.add(element);
        }
    }

    public static class ParagraphElement extends ContainerElement
    {
        public ParagraphElement(final Style style) {
            super(style);
        }
    }

    public static class LinkElement extends ContainerElement
    {
        private String href;

        public LinkElement(final Style style, final String href) {
            super(style);
            this.href = href;
        }

        public String getHREF() {
            return this.href;
        }

        public void setHREF(final String href) {
            this.href = href;
        }
    }

    public static class ListElement extends ContainerElement
    {
        public ListElement(final Style style) {
            super(style);
        }
    }

    public static class OrderedListElement extends ContainerElement
    {
        private final int start;

        public OrderedListElement(final Style style, final int start) {
            super(style);
            this.start = start;
        }

        public int getStart() {
            return this.start;
        }
    }

    public static class BlockElement extends ContainerElement
    {
        public BlockElement(final Style style) {
            super(style);
        }
    }

    public static class TableCellElement extends ContainerElement
    {
        private final int colspan;

        public TableCellElement(final Style style) {
            this(style, 1);
        }

        public TableCellElement(final Style style, final int colspan) {
            super(style);
            this.colspan = colspan;
        }

        public int getColspan() {
            return this.colspan;
        }
    }

    public static class TableElement extends Element
    {
        private final int numColumns;
        private final int numRows;
        private final int cellSpacing;
        private final int cellPadding;
        private final TableCellElement[] cells;
        private final Style[] rowStyles;

        public TableElement(final Style style, final int numColumns, final int numRows, final int cellSpacing, final int cellPadding) {
            super(style);
            if (numColumns < 0) {
                throw new IllegalArgumentException("numColumns");
            }
            if (numRows < 0) {
                throw new IllegalArgumentException("numRows");
            }
            this.numColumns = numColumns;
            this.numRows = numRows;
            this.cellSpacing = cellSpacing;
            this.cellPadding = cellPadding;
            this.cells = new TableCellElement[numRows * numColumns];
            this.rowStyles = new Style[numRows];
        }

        public int getNumColumns() {
            return this.numColumns;
        }

        public int getNumRows() {
            return this.numRows;
        }

        public int getCellPadding() {
            return this.cellPadding;
        }

        public int getCellSpacing() {
            return this.cellSpacing;
        }

        public TableCellElement getCell(final int row, final int column) {
            if (column < 0 || column >= this.numColumns) {
                throw new IndexOutOfBoundsException("column");
            }
            if (row < 0 || row >= this.numRows) {
                throw new IndexOutOfBoundsException("row");
            }
            return this.cells[row * this.numColumns + column];
        }

        public Style getRowStyle(final int row) {
            return this.rowStyles[row];
        }

        public void setCell(final int row, final int column, final TableCellElement cell) {
            if (column < 0 || column >= this.numColumns) {
                throw new IndexOutOfBoundsException("column");
            }
            if (row < 0 || row >= this.numRows) {
                throw new IndexOutOfBoundsException("row");
            }
            this.cells[row * this.numColumns + column] = cell;
        }

        public void setRowStyle(final int row, final Style style) {
            this.rowStyles[row] = style;
        }
    }
}
