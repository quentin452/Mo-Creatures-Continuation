package de.matthiasmann.twl.textarea;

import de.matthiasmann.twl.model.*;
import java.net.*;
import java.util.logging.*;
import java.io.*;
import java.util.*;
import org.xmlpull.v1.*;
import de.matthiasmann.twl.utils.*;

public class HTMLTextAreaModel extends HasCallback implements TextAreaModel
{
    private final ArrayList<Element> elements;
    private final ArrayList<String> styleSheetLinks;
    private final HashMap<String, Element> idMap;
    private String title;
    private final ArrayList<Style> styleStack;
    private final StringBuilder sb;
    private final int[] startLength;
    private ContainerElement curContainer;

    public HTMLTextAreaModel() {
        this.elements = new ArrayList<Element>();
        this.styleSheetLinks = new ArrayList<String>();
        this.idMap = new HashMap<String, Element>();
        this.styleStack = new ArrayList<Style>();
        this.sb = new StringBuilder();
        this.startLength = new int[2];
    }

    public HTMLTextAreaModel(final String html) {
        this();
        this.setHtml(html);
    }

    public HTMLTextAreaModel(final Reader r) throws IOException {
        this();
        this.parseXHTML(r);
    }

    public void setHtml(final String html) {
        Reader r;
        if (isXHTML(html)) {
            r = new StringReader(html);
        }
        else {
            r = new MultiStringReader(new String[] { "<html><body>", html, "</body></html>" });
        }
        this.parseXHTML(r);
    }

    @Deprecated
    public void readHTMLFromStream(final Reader r) throws IOException {
        this.parseXHTML(r);
    }

    public void readHTMLFromURL(final URL url) throws IOException {
        final InputStream in = url.openStream();
        try {
            this.parseXHTML(new InputStreamReader(in, "UTF8"));
        }
        finally {
            try {
                in.close();
            }
            catch (IOException ex) {
                Logger.getLogger(HTMLTextAreaModel.class.getName()).log(Level.SEVERE, "Exception while closing InputStream", ex);
            }
        }
    }

    public Iterator iterator() {
        return this.elements.iterator();
    }

    public Iterable<String> getStyleSheetLinks() {
        return this.styleSheetLinks;
    }

    public String getTitle() {
        return this.title;
    }

    public Element getElementById(final String id) {
        return this.idMap.get(id);
    }

    public void domModified() {
        this.doCallback();
    }

    public void parseXHTML(final Reader reader) {
        this.elements.clear();
        this.styleSheetLinks.clear();
        this.idMap.clear();
        this.title = null;
        try {
            final XmlPullParser xpp = XMLParser.createParser();
            xpp.setInput(reader);
            xpp.defineEntityReplacementText("nbsp", "�");
            xpp.require(0, (String)null, (String)null);
            xpp.nextTag();
            xpp.require(2, (String)null, "html");
            this.styleStack.clear();
            this.styleStack.add(new Style(null, null));
            this.curContainer = null;
            this.sb.setLength(0);
            while (xpp.nextTag() != 3) {
                xpp.require(2, (String)null, (String)null);
                final String name = xpp.getName();
                if ("head".equals(name)) {
                    this.parseHead(xpp);
                }
                else {
                    if (!"body".equals(name)) {
                        continue;
                    }
                    this.pushStyle(xpp);
                    final BlockElement be = new BlockElement(this.getStyle());
                    this.elements.add(be);
                    this.parseContainer(xpp, be);
                }
            }
            this.parseMain(xpp);
            this.finishText();
        }
        catch (Throwable ex) {
            Logger.getLogger(HTMLTextAreaModel.class.getName()).log(Level.SEVERE, "Unable to parse XHTML document", ex);
        }
        finally {
            this.doCallback();
        }
    }

    private void parseContainer(final XmlPullParser xpp, final ContainerElement container) throws XmlPullParserException, IOException {
        final ContainerElement prevContainer = this.curContainer;
        this.curContainer = container;
        this.pushStyle(null);
        this.parseMain(xpp);
        this.popStyle();
        this.curContainer = prevContainer;
    }

    private void parseMain(final XmlPullParser xpp) throws XmlPullParserException, IOException {
        int level = 1;
        int type;
        while (level > 0 && (type = xpp.nextToken()) != 1) {
            switch (type) {
                case 2: {
                    final String name = xpp.getName();
                    if ("head".equals(name)) {
                        this.parseHead(xpp);
                        continue;
                    }
                    ++level;
                    if ("br".equals(name)) {
                        this.sb.append("\n");
                        continue;
                    }
                    this.finishText();
                    final Style style = this.pushStyle(xpp);
                    Element element;
                    if ("img".equals(name)) {
                        final String src = TextUtil.notNull(xpp.getAttributeValue((String)null, "src"));
                        final String alt = xpp.getAttributeValue((String)null, "alt");
                        element = new ImageElement(style, src, alt);
                    }
                    else if ("p".equals(name)) {
                        final ParagraphElement pe = new ParagraphElement(style);
                        this.parseContainer(xpp, pe);
                        element = pe;
                        --level;
                    }
                    else if ("button".equals(name)) {
                        final String btnName = TextUtil.notNull(xpp.getAttributeValue((String)null, "name"));
                        final String btnParam = TextUtil.notNull(xpp.getAttributeValue((String)null, "value"));
                        element = new WidgetElement(style, btnName, btnParam);
                    }
                    else if ("ul".equals(name)) {
                        final ContainerElement ce = new ContainerElement(style);
                        this.parseContainer(xpp, ce);
                        element = ce;
                        --level;
                    }
                    else if ("ol".equals(name)) {
                        element = this.parseOL(xpp, style);
                        --level;
                    }
                    else if ("li".equals(name)) {
                        final ListElement le = new ListElement(style);
                        this.parseContainer(xpp, le);
                        element = le;
                        --level;
                    }
                    else if ("div".equals(name) || this.isHeading(name)) {
                        final BlockElement be = new BlockElement(style);
                        this.parseContainer(xpp, be);
                        element = be;
                        --level;
                    }
                    else if ("a".equals(name)) {
                        final String href = xpp.getAttributeValue((String)null, "href");
                        if (href == null) {
                            continue;
                        }
                        final LinkElement le2 = new LinkElement(style, href);
                        this.parseContainer(xpp, le2);
                        element = le2;
                        --level;
                    }
                    else {
                        if (!"table".equals(name)) {
                            continue;
                        }
                        element = this.parseTable(xpp, style);
                        --level;
                    }
                    this.curContainer.add(element);
                    this.registerElement(element);
                    continue;
                }
                case 3: {
                    --level;
                    final String name = xpp.getName();
                    if ("br".equals(name)) {
                        continue;
                    }
                    this.finishText();
                    this.popStyle();
                    continue;
                }
                case 4: {
                    final char[] buf = xpp.getTextCharacters(this.startLength);
                    if (this.startLength[1] > 0) {
                        final int pos = this.sb.length();
                        this.sb.append(buf, this.startLength[0], this.startLength[1]);
                        if (this.isPre()) {
                            continue;
                        }
                        this.removeBreaks(pos);
                        continue;
                    }
                    continue;
                }
                case 6: {
                    this.sb.append(xpp.getText());
                    continue;
                }
            }
        }
    }

    private void parseHead(final XmlPullParser xpp) throws XmlPullParserException, IOException {
        int level = 1;
        while (level > 0) {
            switch (xpp.nextTag()) {
                case 2: {
                    ++level;
                    final String name = xpp.getName();
                    if ("link".equals(name)) {
                        final String linkhref = xpp.getAttributeValue((String)null, "href");
                        if ("stylesheet".equals(xpp.getAttributeValue((String)null, "rel")) && "text/css".equals(xpp.getAttributeValue((String)null, "type")) && linkhref != null) {
                            this.styleSheetLinks.add(linkhref);
                        }
                    }
                    if ("title".equals(name)) {
                        this.title = xpp.nextText();
                        --level;
                        continue;
                    }
                    continue;
                }
                case 3: {
                    --level;
                    continue;
                }
            }
        }
    }

    private TableElement parseTable(final XmlPullParser xpp, final Style tableStyle) throws XmlPullParserException, IOException {
        final ArrayList<TableCellElement> cells = new ArrayList<TableCellElement>();
        final ArrayList<Style> rowStyles = new ArrayList<Style>();
        int numColumns = 0;
        final int cellSpacing = parseInt(xpp, "cellspacing", 0);
        final int cellPadding = parseInt(xpp, "cellpadding", 0);
    Block_6:
        while (true) {
            switch (xpp.nextTag()) {
                case 2: {
                    this.pushStyle(xpp);
                    final String name = xpp.getName();
                    if ("td".equals(name) || "th".equals(name)) {
                        final int colspan = parseInt(xpp, "colspan", 1);
                        final TableCellElement cell = new TableCellElement(this.getStyle(), colspan);
                        this.parseContainer(xpp, cell);
                        this.registerElement(cell);
                        cells.add(cell);
                        for (int col = 1; col < colspan; ++col) {
                            cells.add(null);
                        }
                    }
                    if ("tr".equals(name)) {
                        rowStyles.add(this.getStyle());
                        continue;
                    }
                    continue;
                }
                case 3: {
                    this.popStyle();
                    final String name = xpp.getName();
                    if ("tr".equals(name) && numColumns == 0) {
                        numColumns = cells.size();
                    }
                    if ("table".equals(name)) {
                        break Block_6;
                    }
                    continue;
                }
            }
        }
        final TableElement tableElement = new TableElement(tableStyle, numColumns, rowStyles.size(), cellSpacing, cellPadding);
        int row = 0;
        int idx = 0;
        while (row < rowStyles.size()) {
            tableElement.setRowStyle(row, rowStyles.get(row));
            for (int col2 = 0; col2 < numColumns && idx < cells.size(); ++col2, ++idx) {
                final TableCellElement cell2 = cells.get(idx);
                tableElement.setCell(row, col2, cell2);
            }
            ++row;
        }
        return tableElement;
    }

    private OrderedListElement parseOL(final XmlPullParser xpp, final Style olStyle) throws XmlPullParserException, IOException {
        final int start = parseInt(xpp, "start", 1);
        final OrderedListElement ole = new OrderedListElement(olStyle, start);
        this.registerElement(ole);
    Block_2:
        while (true) {
            switch (xpp.nextTag()) {
                case 2: {
                    this.pushStyle(xpp);
                    final String name = xpp.getName();
                    if ("li".equals(name)) {
                        final ContainerElement ce = new ContainerElement(this.getStyle());
                        this.parseContainer(xpp, ce);
                        this.registerElement(ce);
                        ole.add(ce);
                        continue;
                    }
                    continue;
                }
                case 3: {
                    this.popStyle();
                    final String name = xpp.getName();
                    if ("ol".equals(name)) {
                        break Block_2;
                    }
                    continue;
                }
            }
        }
        return ole;
    }

    private void registerElement(final Element element) {
        final StyleSheetKey styleSheetKey = element.getStyle().getStyleSheetKey();
        if (styleSheetKey != null) {
            final String id = styleSheetKey.getId();
            if (id != null) {
                this.idMap.put(id, element);
            }
        }
    }

    private static int parseInt(final XmlPullParser xpp, final String attribute, final int defaultValue) {
        final String value = xpp.getAttributeValue((String)null, attribute);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            }
            catch (IllegalArgumentException ex) {}
        }
        return defaultValue;
    }

    private static boolean isXHTML(final String doc) {
        return doc.length() > 5 && doc.charAt(0) == '<' && (doc.startsWith("<?xml") || doc.startsWith("<!DOCTYPE") || doc.startsWith("<html>"));
    }

    private boolean isHeading(final String name) {
        return name.length() == 2 && name.charAt(0) == 'h' && name.charAt(1) >= '0' && name.charAt(1) <= '6';
    }

    private boolean isPre() {
        return this.getStyle().get(StyleAttribute.PREFORMATTED, null);
    }

    private Style getStyle() {
        return this.styleStack.get(this.styleStack.size() - 1);
    }

    private Style pushStyle(final XmlPullParser xpp) {
        final Style parent = this.getStyle();
        StyleSheetKey key = null;
        String style = null;
        if (xpp != null) {
            final String className = xpp.getAttributeValue((String)null, "class");
            final String element = xpp.getName();
            final String id = xpp.getAttributeValue((String)null, "id");
            key = new StyleSheetKey(element, className, id);
            style = xpp.getAttributeValue((String)null, "style");
        }
        Style newStyle;
        if (style != null) {
            newStyle = (Style)new CSSStyle(parent, key, style);
        }
        else {
            newStyle = new Style(parent, key);
        }
        if (xpp != null && "pre".equals(xpp.getName())) {
            newStyle.put(StyleAttribute.PREFORMATTED, Boolean.TRUE);
        }
        this.styleStack.add(newStyle);
        return newStyle;
    }

    private void popStyle() {
        final int stackSize = this.styleStack.size();
        if (stackSize > 1) {
            this.styleStack.remove(stackSize - 1);
        }
    }

    private void finishText() {
        if (this.sb.length() > 0) {
            final Style style = this.getStyle();
            final TextElement e = new TextElement(style, this.sb.toString());
            this.registerElement(e);
            this.curContainer.add(e);
            this.sb.setLength(0);
        }
    }

    private void removeBreaks(int pos) {
        int idx = this.sb.length();
        while (idx-- > pos) {
            final char ch = this.sb.charAt(idx);
            if (Character.isWhitespace(ch) || Character.isISOControl(ch)) {
                this.sb.setCharAt(idx, ' ');
            }
        }
        if (pos > 0) {
            --pos;
        }
        boolean wasSpace = false;
        int idx2 = this.sb.length();
        while (idx2-- > pos) {
            final boolean isSpace = this.sb.charAt(idx2) == ' ';
            if (isSpace && wasSpace) {
                this.sb.deleteCharAt(idx2);
            }
            wasSpace = isSpace;
        }
    }
}
