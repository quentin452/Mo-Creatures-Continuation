package de.matthiasmann.twl;

import java.util.logging.*;
import java.io.*;
import de.matthiasmann.twl.textarea.*;
import java.util.*;
import de.matthiasmann.twl.theme.*;
import de.matthiasmann.twl.utils.*;
import de.matthiasmann.twl.renderer.*;

public class TextArea extends Widget
{
    public static final AnimationState.StateKey STATE_HOVER;
    static final char[] EMPTY_CHAR_ARRAY;
    private final HashMap<String, Widget> widgets;
    private final HashMap<String, WidgetResolver> widgetResolvers;
    private final HashMap<String, Image> userImages;
    private final ArrayList<ImageResolver> imageResolvers;
    StyleSheetResolver styleClassResolver;
    private final Runnable modelCB;
    private TextAreaModel model;
    private ParameterMap fonts;
    private ParameterMap images;
    private Font defaultFont;
    private Callback[] callbacks;
    private MouseCursor mouseCursorNormal;
    private MouseCursor mouseCursorLink;
    private DraggableButton.DragListener dragListener;
    private final LClip layoutRoot;
    private final ArrayList<LImage> allBGImages;
    private final RenderInfo renderInfo;
    private boolean inLayoutCode;
    private boolean forceRelayout;
    private Dimension preferredInnerSize;
    private FontMapper fontMapper;
    private FontMapperCacheEntry[] fontMapperCache;
    private int lastMouseX;
    private int lastMouseY;
    private boolean lastMouseInside;
    private boolean dragging;
    private int dragStartX;
    private int dragStartY;
    private LElement curLElementUnderMouse;
    private static final int DEFAULT_FONT_SIZE = 14;
    private static final StateSelect HOVER_STATESELECT;
    private static final int FONT_MAPPER_CACHE_SIZE = 16;

    public TextArea() {
        this.widgets = new HashMap<String, Widget>();
        this.widgetResolvers = new HashMap<String, WidgetResolver>();
        this.userImages = new HashMap<String, Image>();
        this.imageResolvers = new ArrayList<ImageResolver>();
        this.layoutRoot = new LClip(null);
        this.allBGImages = new ArrayList<LImage>();
        this.renderInfo = new RenderInfo(this.getAnimationState());
        this.modelCB = new Runnable() {
            @Override
            public void run() {
                TextArea.this.forceRelayout();
            }
        };
    }

    public TextArea(final TextAreaModel model) {
        this();
        this.setModel(model);
    }

    public TextAreaModel getModel() {
        return this.model;
    }

    public void setModel(final TextAreaModel model) {
        if (this.model != null) {
            this.model.removeCallback(this.modelCB);
        }
        if ((this.model = model) != null) {
            model.addCallback(this.modelCB);
        }
        this.forceRelayout();
    }

    public void registerWidget(final String name, final Widget widget) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (widget.getParent() != null) {
            throw new IllegalArgumentException("Widget must not have a parent");
        }
        if (this.widgets.containsKey(name) || this.widgetResolvers.containsKey(name)) {
            throw new IllegalArgumentException("widget name already in registered");
        }
        if (this.widgets.containsValue(widget)) {
            throw new IllegalArgumentException("widget already registered");
        }
        this.widgets.put(name, widget);
    }

    public void registerWidgetResolver(final String name, final WidgetResolver resolver) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        if (resolver == null) {
            throw new NullPointerException("resolver");
        }
        if (this.widgets.containsKey(name) || this.widgetResolvers.containsKey(name)) {
            throw new IllegalArgumentException("widget name already in registered");
        }
        this.widgetResolvers.put(name, resolver);
    }

    public void unregisterWidgetResolver(final String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        this.widgetResolvers.remove(name);
    }

    public void unregisterWidget(final String name) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        final Widget w = this.widgets.get(name);
        if (w != null) {
            final int idx = this.getChildIndex(w);
            if (idx >= 0) {
                super.removeChild(idx);
                this.forceRelayout();
            }
        }
    }

    public void unregisterAllWidgets() {
        this.widgets.clear();
        super.removeAllChildren();
        this.forceRelayout();
    }

    public void registerImage(final String name, final Image image) {
        if (name == null) {
            throw new NullPointerException("name");
        }
        this.userImages.put(name, image);
    }

    public void registerImageResolver(final ImageResolver resolver) {
        if (resolver == null) {
            throw new NullPointerException("resolver");
        }
        if (!this.imageResolvers.contains(resolver)) {
            this.imageResolvers.add(resolver);
        }
    }

    public void unregisterImage(final String name) {
        this.userImages.remove(name);
    }

    public void unregisterImageResolver(final ImageResolver imageResolver) {
        this.imageResolvers.remove(imageResolver);
    }

    public void addCallback(final Callback cb) {
        this.callbacks = CallbackSupport.addCallbackToList(this.callbacks, cb, Callback.class);
    }

    public void removeCallback(final Callback cb) {
        this.callbacks = CallbackSupport.removeCallbackFromList(this.callbacks, cb);
    }

    public DraggableButton.DragListener getDragListener() {
        return this.dragListener;
    }

    public void setDragListener(final DraggableButton.DragListener dragListener) {
        this.dragListener = dragListener;
    }

    public StyleSheetResolver getStyleClassResolver() {
        return this.styleClassResolver;
    }

    public void setStyleClassResolver(final StyleSheetResolver styleClassResolver) {
        this.styleClassResolver = styleClassResolver;
        this.forceRelayout();
    }

    public void setDefaultStyleSheet() {
        try {
            final StyleSheet styleSheet = new StyleSheet();
            styleSheet.parse("p,ul{margin-bottom:1em}");
            this.setStyleClassResolver((StyleSheetResolver)styleSheet);
        }
        catch (IOException ex) {
            Logger.getLogger(TextArea.class.getName()).log(Level.SEVERE, "Can't create default style sheet", ex);
        }
    }

    public Rect getElementRect(final TextAreaModel.Element element) {
        final int[] offset = new int[2];
        final LElement le = this.layoutRoot.find(element, offset);
        if (le != null) {
            return new Rect(le.x + offset[0], le.y + offset[1], le.width, le.height);
        }
        return null;
    }

    @Override
    protected void applyTheme(final ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        this.applyThemeTextArea(themeInfo);
    }

    protected void applyThemeTextArea(final ThemeInfo themeInfo) {
        this.fonts = themeInfo.getParameterMap("fonts");
        this.images = themeInfo.getParameterMap("images");
        this.defaultFont = themeInfo.getFont("font");
        this.mouseCursorNormal = themeInfo.getMouseCursor("mouseCursor");
        this.mouseCursorLink = themeInfo.getMouseCursor("mouseCursor.link");
        this.forceRelayout();
    }

    @Override
    protected void afterAddToGUI(final GUI gui) {
        super.afterAddToGUI(gui);
        this.renderInfo.asNormal.setGUI(gui);
        this.renderInfo.asHover.setGUI(gui);
    }

    @Override
    public void insertChild(final Widget child, final int index) {
        throw new UnsupportedOperationException("use registerWidget");
    }

    @Override
    public void removeAllChildren() {
        throw new UnsupportedOperationException("use registerWidget");
    }

    @Override
    public Widget removeChild(final int index) {
        throw new UnsupportedOperationException("use registerWidget");
    }

    private void computePreferredInnerSize() {
        int prefWidth = -1;
        int prefHeight = -1;
        if (this.model == null) {
            prefWidth = 0;
            prefHeight = 0;
        }
        else if (this.getMaxWidth() > 0) {
            final int borderHorizontal = this.getBorderHorizontal();
            final int maxWidth = Math.max(0, this.getMaxWidth() - borderHorizontal);
            final int minWidth = Math.max(0, this.getMinWidth() - borderHorizontal);
            if (minWidth < maxWidth) {
                final LClip tmpRoot = new LClip(null);
                this.startLayout();
                try {
                    tmpRoot.width = maxWidth;
                    final Box box = new Box(tmpRoot, 0, 0, 0, false);
                    this.layoutElements(box, (Iterable)this.model);
                    box.finish();
                    prefWidth = Math.max(0, maxWidth - box.minRemainingWidth);
                    prefHeight = box.curY;
                }
                finally {
                    this.endLayout();
                }
            }
        }
        this.preferredInnerSize = new Dimension(prefWidth, prefHeight);
    }

    @Override
    public int getPreferredInnerWidth() {
        if (this.preferredInnerSize == null) {
            this.computePreferredInnerSize();
        }
        if (this.preferredInnerSize.getX() >= 0) {
            return this.preferredInnerSize.getX();
        }
        return this.getInnerWidth();
    }

    @Override
    public int getPreferredInnerHeight() {
        if (this.getInnerWidth() == 0) {
            if (this.preferredInnerSize == null) {
                this.computePreferredInnerSize();
            }
            if (this.preferredInnerSize.getY() >= 0) {
                return this.preferredInnerSize.getY();
            }
        }
        this.validateLayout();
        return this.layoutRoot.height;
    }

    @Override
    public int getPreferredWidth() {
        final int maxWidth = this.getMaxWidth();
        return Widget.computeSize(this.getMinWidth(), super.getPreferredWidth(), maxWidth);
    }

    @Override
    public void setMaxSize(final int width, final int height) {
        if (width != this.getMaxWidth()) {
            this.preferredInnerSize = null;
            this.invalidateLayout();
        }
        super.setMaxSize(width, height);
    }

    @Override
    public void setMinSize(final int width, final int height) {
        if (width != this.getMinWidth()) {
            this.preferredInnerSize = null;
            this.invalidateLayout();
        }
        super.setMinSize(width, height);
    }

    @Override
    protected void layout() {
        final int targetWidth = this.getInnerWidth();
        if (this.layoutRoot.width != targetWidth || this.forceRelayout) {
            this.layoutRoot.width = targetWidth;
            this.inLayoutCode = true;
            this.forceRelayout = false;
            this.startLayout();
            int requiredHeight;
            try {
                this.clearLayout();
                final Box box = new Box(this.layoutRoot, 0, 0, 0, true);
                if (this.model != null) {
                    this.layoutElements(box, (Iterable)this.model);
                    box.finish();
                    this.layoutRoot.adjustWidget(this.getInnerX(), this.getInnerY());
                    this.layoutRoot.collectBGImages(0, 0, this.allBGImages);
                }
                this.updateMouseHover();
                requiredHeight = box.curY;
            }
            finally {
                this.inLayoutCode = false;
                this.endLayout();
            }
            if (this.layoutRoot.height != requiredHeight && this.getInnerHeight() != (this.layoutRoot.height = requiredHeight)) {
                this.invalidateLayout();
            }
        }
    }

    @Override
    protected void paintWidget(final GUI gui) {
        final ArrayList<LImage> bi = this.allBGImages;
        final RenderInfo ri = this.renderInfo;
        ri.offsetX = this.getInnerX();
        ri.offsetY = this.getInnerY();
        ri.renderer = gui.getRenderer();
        for (int i = 0, n = bi.size(); i < n; ++i) {
            bi.get(i).draw(ri);
        }
        this.layoutRoot.draw(ri);
    }

    @Override
    protected void sizeChanged() {
        if (!this.inLayoutCode) {
            this.invalidateLayout();
        }
    }

    @Override
    protected void childAdded(final Widget child) {
    }

    @Override
    protected void childRemoved(final Widget exChild) {
    }

    @Override
    protected void allChildrenRemoved() {
    }

    @Override
    public void destroy() {
        super.destroy();
        this.clearLayout();
        this.forceRelayout();
    }

    @Override
    protected boolean handleEvent(final Event evt) {
        if (super.handleEvent(evt)) {
            return true;
        }
        if (!evt.isMouseEvent()) {
            return false;
        }
        final Event.Type eventType = evt.getType();
        if (this.dragging) {
            if (eventType == Event.Type.MOUSE_DRAGGED && this.dragListener != null) {
                this.dragListener.dragged(evt.getMouseX() - this.dragStartX, evt.getMouseY() - this.dragStartY);
            }
            if (evt.isMouseDragEnd()) {
                if (this.dragListener != null) {
                    this.dragListener.dragStopped();
                }
                this.dragging = false;
                this.updateMouseHover(evt);
            }
            return true;
        }
        this.updateMouseHover(evt);
        if (eventType == Event.Type.MOUSE_WHEEL) {
            return false;
        }
        if (eventType == Event.Type.MOUSE_BTNDOWN) {
            this.dragStartX = evt.getMouseX();
            this.dragStartY = evt.getMouseY();
        }
        if (eventType != Event.Type.MOUSE_DRAGGED) {
            if (this.curLElementUnderMouse != null && (eventType == Event.Type.MOUSE_CLICKED || eventType == Event.Type.MOUSE_BTNDOWN || eventType == Event.Type.MOUSE_BTNUP)) {
                final TextAreaModel.Element e = this.curLElementUnderMouse.element;
                if (this.callbacks != null) {
                    for (final Callback l : this.callbacks) {
                        if (l instanceof Callback2) {
                            ((Callback2)l).handleMouseButton(evt, e);
                        }
                    }
                }
            }
            if (eventType == Event.Type.MOUSE_CLICKED && this.curLElementUnderMouse != null && this.curLElementUnderMouse.href != null) {
                final String href = this.curLElementUnderMouse.href;
                if (this.callbacks != null) {
                    for (final Callback l : this.callbacks) {
                        l.handleLinkClicked(href);
                    }
                }
            }
            return true;
        }
        assert !this.dragging;
        this.dragging = true;
        if (this.dragListener != null) {
            this.dragListener.dragStarted();
        }
        return true;
    }

    @Override
    protected Object getTooltipContentAt(final int mouseX, final int mouseY) {
        if (this.curLElementUnderMouse != null && this.curLElementUnderMouse.element instanceof TextAreaModel.ImageElement) {
            return ((TextAreaModel.ImageElement)this.curLElementUnderMouse.element).getToolTip();
        }
        return super.getTooltipContentAt(mouseX, mouseY);
    }

    private void updateMouseHover(final Event evt) {
        this.lastMouseInside = this.isMouseInside(evt);
        this.lastMouseX = evt.getMouseX();
        this.lastMouseY = evt.getMouseY();
        this.updateMouseHover();
    }

    private void updateMouseHover() {
        LElement le = null;
        if (this.lastMouseInside) {
            le = this.layoutRoot.find(this.lastMouseX - this.getInnerX(), this.lastMouseY - this.getInnerY());
        }
        if (this.curLElementUnderMouse != le) {
            this.curLElementUnderMouse = le;
            this.layoutRoot.setHover(le);
            this.renderInfo.asNormal.resetAnimationTime(TextArea.STATE_HOVER);
            this.renderInfo.asHover.resetAnimationTime(TextArea.STATE_HOVER);
            this.updateTooltip();
        }
        if (le != null && le.href != null) {
            this.setMouseCursor(this.mouseCursorLink);
        }
        else {
            this.setMouseCursor(this.mouseCursorNormal);
        }
        this.getAnimationState().setAnimationState(TextArea.STATE_HOVER, this.lastMouseInside);
    }

    void forceRelayout() {
        this.forceRelayout = true;
        this.preferredInnerSize = null;
        this.invalidateLayout();
    }

    private void clearLayout() {
        this.layoutRoot.destroy();
        this.allBGImages.clear();
        super.removeAllChildren();
    }

    private void startLayout() {
        if (this.styleClassResolver != null) {
            this.styleClassResolver.startLayout();
        }
        final GUI gui = this.getGUI();
        this.fontMapper = ((gui != null) ? gui.getRenderer().getFontMapper() : null);
        this.fontMapperCache = null;
    }

    private void endLayout() {
        if (this.styleClassResolver != null) {
            this.styleClassResolver.layoutFinished();
        }
        this.fontMapper = null;
        this.fontMapperCache = null;
    }

    private void layoutElements(final Box box, final Iterable<TextAreaModel.Element> elements) {
        for (final TextAreaModel.Element e : elements) {
            this.layoutElement(box, e);
        }
    }

    private void layoutElement(final Box box, final TextAreaModel.Element e) {
        box.clearFloater((TextAreaModel.Clear)e.getStyle().get(StyleAttribute.CLEAR, this.styleClassResolver));
        if (e instanceof TextAreaModel.TextElement) {
            this.layoutTextElement(box, (TextAreaModel.TextElement)e);
        }
        else {
            if (box.wasPreformatted) {
                box.nextLine(false);
                box.wasPreformatted = false;
            }
            if (e instanceof TextAreaModel.ParagraphElement) {
                this.layoutParagraphElement(box, (TextAreaModel.ParagraphElement)e);
            }
            else if (e instanceof TextAreaModel.ImageElement) {
                this.layoutImageElement(box, (TextAreaModel.ImageElement)e);
            }
            else if (e instanceof TextAreaModel.WidgetElement) {
                this.layoutWidgetElement(box, (TextAreaModel.WidgetElement)e);
            }
            else if (e instanceof TextAreaModel.ListElement) {
                this.layoutListElement(box, (TextAreaModel.ListElement)e);
            }
            else if (e instanceof TextAreaModel.OrderedListElement) {
                this.layoutOrderedListElement(box, (TextAreaModel.OrderedListElement)e);
            }
            else if (e instanceof TextAreaModel.BlockElement) {
                this.layoutBlockElement(box, (TextAreaModel.ContainerElement)e);
            }
            else if (e instanceof TextAreaModel.TableElement) {
                this.layoutTableElement(box, (TextAreaModel.TableElement)e);
            }
            else if (e instanceof TextAreaModel.LinkElement) {
                this.layoutLinkElement(box, (TextAreaModel.LinkElement)e);
            }
            else if (e instanceof TextAreaModel.ContainerElement) {
                this.layoutContainerElement(box, (TextAreaModel.ContainerElement)e);
            }
            else {
                Logger.getLogger(TextArea.class.getName()).log(Level.SEVERE, "Unknown Element subclass: {0}", e.getClass());
            }
        }
    }

    private void layoutImageElement(final Box box, final TextAreaModel.ImageElement ie) {
        final Image image = this.selectImage(ie.getImageName());
        if (image == null) {
            return;
        }
        final LImage li = new LImage((TextAreaModel.Element)ie, image);
        li.href = box.href;
        this.layout(box, (TextAreaModel.Element)ie, li);
    }

    private void layoutWidgetElement(final Box box, final TextAreaModel.WidgetElement we) {
        Widget widget = this.widgets.get(we.getWidgetName());
        if (widget == null) {
            final WidgetResolver resolver = this.widgetResolvers.get(we.getWidgetName());
            if (resolver != null) {
                widget = resolver.resolveWidget(we.getWidgetName(), we.getWidgetParam());
            }
            if (widget == null) {
                return;
            }
        }
        if (widget.getParent() != null) {
            Logger.getLogger(TextArea.class.getName()).log(Level.SEVERE, "Widget already added: {0}", widget);
            return;
        }
        super.insertChild(widget, this.getNumChildren());
        widget.adjustSize();
        final LWidget lw = new LWidget((TextAreaModel.Element)we, widget);
        lw.width = widget.getWidth();
        lw.height = widget.getHeight();
        this.layout(box, (TextAreaModel.Element)we, lw);
    }

    private void layout(final Box box, final TextAreaModel.Element e, final LElement le) {
        final Style style = e.getStyle();
        final TextAreaModel.FloatPosition floatPosition = (TextAreaModel.FloatPosition)style.get(StyleAttribute.FLOAT_POSITION, this.styleClassResolver);
        final TextAreaModel.Display display = (TextAreaModel.Display)style.get(StyleAttribute.DISPLAY, this.styleClassResolver);
        le.marginTop = (short)this.convertToPX0(style, (StyleAttribute<Value>)StyleAttribute.MARGIN_TOP, box.boxWidth);
        le.marginLeft = (short)this.convertToPX0(style, (StyleAttribute<Value>)StyleAttribute.MARGIN_LEFT, box.boxWidth);
        le.marginRight = (short)this.convertToPX0(style, (StyleAttribute<Value>)StyleAttribute.MARGIN_RIGHT, box.boxWidth);
        le.marginBottom = (short)this.convertToPX0(style, (StyleAttribute<Value>)StyleAttribute.MARGIN_BOTTOM, box.boxWidth);
        int autoHeight = le.height;
        final int width = this.convertToPX(style, (StyleAttribute<Value>)StyleAttribute.WIDTH, box.boxWidth, le.width);
        if (width > 0) {
            if (le.width > 0) {
                autoHeight = width * le.height / le.width;
            }
            le.width = width;
        }
        final int height = this.convertToPX(style, (StyleAttribute<Value>)StyleAttribute.HEIGHT, le.height, autoHeight);
        if (height > 0) {
            le.height = height;
        }
        this.layout(box, e, le, floatPosition, display);
    }

    private void layout(final Box box, final TextAreaModel.Element e, final LElement le, final TextAreaModel.FloatPosition floatPos, final TextAreaModel.Display display) {
        final boolean leftRight = floatPos != TextAreaModel.FloatPosition.NONE;
        if (leftRight || display != TextAreaModel.Display.INLINE) {
            box.nextLine(false);
            if (!leftRight) {
                box.curY = box.computeTopPadding(le.marginTop);
                box.checkFloaters();
            }
        }
        box.advancePastFloaters(le.width, le.marginLeft, le.marginRight);
        if (le.width > box.lineWidth) {
            le.width = box.lineWidth;
        }
        if (leftRight) {
            if (floatPos == TextAreaModel.FloatPosition.RIGHT) {
                le.x = box.computeRightPadding(le.marginRight) - le.width;
                box.objRight.add(le);
            }
            else {
                le.x = box.computeLeftPadding(le.marginLeft);
                box.objLeft.add(le);
            }
        }
        else if (display == TextAreaModel.Display.INLINE) {
            if (box.getRemaining() < le.width && !box.isAtStartOfLine()) {
                box.nextLine(false);
            }
            le.x = box.getXAndAdvance(le.width);
        }
        else {
            switch ((TextAreaModel.HAlignment)e.getStyle().get(StyleAttribute.HORIZONTAL_ALIGNMENT, this.styleClassResolver)) {
                case CENTER:
                case JUSTIFY: {
                    le.x = box.lineStartX + (box.lineWidth - le.width) / 2;
                    break;
                }
                case RIGHT: {
                    le.x = box.computeRightPadding(le.marginRight) - le.width;
                    break;
                }
                default: {
                    le.x = box.computeLeftPadding(le.marginLeft);
                    break;
                }
            }
        }
        box.layout.add(le);
        if (leftRight) {
            assert box.lineStartIdx == box.layout.size() - 1;
            ++box.lineStartIdx;
            le.y = box.computeTopPadding(le.marginTop);
            box.computePadding();
        }
        else if (display != TextAreaModel.Display.INLINE) {
            box.accountMinRemaining(Math.max(0, box.lineWidth - le.width));
            box.nextLine(false);
        }
    }

    int convertToPX(Style style, final StyleAttribute<Value> attribute, final int full, final int auto) {
        style = style.resolve((StyleAttribute)attribute, this.styleClassResolver);
        final Value valueUnit = (Value)style.getNoResolve((StyleAttribute)attribute, this.styleClassResolver);
        Font font = null;
        if (valueUnit.unit.isFontBased()) {
            if (attribute == StyleAttribute.FONT_SIZE) {
                style = style.getParent();
                if (style == null) {
                    return 14;
                }
            }
            font = this.selectFont(style);
            if (font == null) {
                return 0;
            }
        }
        float value = valueUnit.value;
        switch (valueUnit.unit) {
            case EM: {
                value *= font.getEM();
                break;
            }
            case EX: {
                value *= font.getEX();
                break;
            }
            case PERCENT: {
                value *= full * 0.01f;
                break;
            }
            case PT: {
                value *= 1.33f;
                break;
            }
            case AUTO: {
                return auto;
            }
        }
        if (value >= 32767.0f) {
            return 32767;
        }
        if (value <= -32768.0f) {
            return -32768;
        }
        return Math.round(value);
    }

    int convertToPX0(final Style style, final StyleAttribute<Value> attribute, final int full) {
        return Math.max(0, this.convertToPX(style, attribute, full, 0));
    }

    private Font selectFont(final Style style) {
        StringList fontFamilies = (StringList)style.get(StyleAttribute.FONT_FAMILIES, this.styleClassResolver);
        if (fontFamilies != null) {
            if (this.fontMapper != null) {
                final Font font = this.selectFontMapper(style, this.fontMapper, fontFamilies);
                if (font != null) {
                    return font;
                }
            }
            if (this.fonts != null) {
                do {
                    final Font font = this.fonts.getFont(fontFamilies.getValue());
                    if (font != null) {
                        return font;
                    }
                } while ((fontFamilies = fontFamilies.getNext()) != null);
            }
        }
        return this.defaultFont;
    }

    private Font selectFontMapper(final Style style, final FontMapper fontMapper, final StringList fontFamilies) {
        final int fontSize = this.convertToPX(style, (StyleAttribute<Value>)StyleAttribute.FONT_SIZE, 14, 14);
        int fontStyle = 0;
        if ((int)style.get(StyleAttribute.FONT_WEIGHT, this.styleClassResolver) >= 550) {
            fontStyle |= 0x1;
        }
        if (style.get(StyleAttribute.FONT_ITALIC, this.styleClassResolver)) {
            fontStyle |= 0x2;
        }
        final TextDecoration textDecoration = (TextDecoration)style.get(StyleAttribute.TEXT_DECORATION, this.styleClassResolver);
        final TextDecoration textDecorationHover = (TextDecoration)style.get(StyleAttribute.TEXT_DECORATION_HOVER, this.styleClassResolver);
        int hashCode = fontSize;
        hashCode = hashCode * 67 + fontStyle;
        hashCode = hashCode * 67 + fontFamilies.hashCode();
        hashCode = hashCode * 67 + textDecoration.hashCode();
        hashCode = hashCode * 67 + ((textDecorationHover != null) ? textDecorationHover.hashCode() : 0);
        final int cacheIdx = hashCode & 0xF;
        if (this.fontMapperCache != null) {
            for (FontMapperCacheEntry ce = this.fontMapperCache[cacheIdx]; ce != null; ce = ce.next) {
                if (ce.hashCode == hashCode && ce.fontSize == fontSize && ce.fontStyle == fontStyle && ce.tdNormal == textDecoration && ce.tdHover == textDecorationHover && ce.fontFamilies.equals(fontFamilies)) {
                    return ce.font;
                }
            }
        }
        else {
            this.fontMapperCache = new FontMapperCacheEntry[16];
        }
        final FontParameter fpNormal = createFontParameter(textDecoration);
        StateSelect select;
        FontParameter[] params;
        if (textDecorationHover != null) {
            final FontParameter fpHover = createFontParameter(textDecorationHover);
            select = TextArea.HOVER_STATESELECT;
            params = new FontParameter[] { fpHover, fpNormal };
        }
        else {
            select = StateSelect.EMPTY;
            params = new FontParameter[] { fpNormal };
        }
        final Font font = fontMapper.getFont(fontFamilies, fontSize, fontStyle, select, params);
        final FontMapperCacheEntry ce2 = new FontMapperCacheEntry(fontSize, fontStyle, fontFamilies, textDecoration, textDecorationHover, hashCode, font);
        ce2.next = this.fontMapperCache[cacheIdx];
        this.fontMapperCache[cacheIdx] = ce2;
        return font;
    }

    private static FontParameter createFontParameter(final TextDecoration deco) {
        final FontParameter fp = new FontParameter();
        fp.put(FontParameter.UNDERLINE, deco == TextDecoration.UNDERLINE);
        fp.put(FontParameter.LINETHROUGH, deco == TextDecoration.LINE_THROUGH);
        return fp;
    }

    private FontData createFontData(final Style style) {
        final Font font = this.selectFont(style);
        if (font == null) {
            return null;
        }
        return new FontData(font, style.get(StyleAttribute.COLOR, this.styleClassResolver), style.get(StyleAttribute.COLOR_HOVER, this.styleClassResolver));
    }

    private Image selectImage(final Style style, final StyleAttribute<String> element) {
        final String imageName = (String)style.get((StyleAttribute)element, this.styleClassResolver);
        if (imageName != null) {
            return this.selectImage(imageName);
        }
        return null;
    }

    private Image selectImage(final String name) {
        Image image = this.userImages.get(name);
        if (image != null) {
            return image;
        }
        for (int i = 0; i < this.imageResolvers.size(); ++i) {
            image = this.imageResolvers.get(i).resolveImage(name);
            if (image != null) {
                return image;
            }
        }
        if (this.images != null) {
            return this.images.getImage(name);
        }
        return null;
    }

    private void layoutParagraphElement(final Box box, final TextAreaModel.ParagraphElement pe) {
        final Style style = pe.getStyle();
        final Font font = this.selectFont(style);
        this.doMarginTop(box, style);
        final LElement anchor = box.addAnchor((TextAreaModel.Element)pe);
        box.setupTextParams(style, font, true);
        this.layoutElements(box, (Iterable<TextAreaModel.Element>)pe);
        if (box.textAlignment == TextAreaModel.HAlignment.JUSTIFY) {
            box.textAlignment = TextAreaModel.HAlignment.LEFT;
        }
        box.nextLine(false);
        box.inParagraph = false;
        anchor.height = box.curY - anchor.y;
        this.doMarginBottom(box, style);
    }

    private void layoutTextElement(final Box box, final TextAreaModel.TextElement te) {
        final String text = te.getText();
        final Style style = te.getStyle();
        final FontData fontData = this.createFontData(style);
        final boolean pre = (boolean)style.get(StyleAttribute.PREFORMATTED, this.styleClassResolver);
        if (fontData == null) {
            return;
        }
        final Boolean inheritHoverStyle = (Boolean)style.resolve(StyleAttribute.INHERIT_HOVER, this.styleClassResolver).getRaw(StyleAttribute.INHERIT_HOVER);
        boolean inheritHover;
        if (inheritHoverStyle != null) {
            inheritHover = inheritHoverStyle;
        }
        else {
            inheritHover = (box.style != null && box.style == style.getParent());
        }
        box.setupTextParams(style, fontData.font, false);
        if (pre && !box.wasPreformatted) {
            box.nextLine(false);
        }
        int end;
        for (int idx = 0; idx < text.length(); idx = end) {
            end = TextUtil.indexOf(text, '\n', idx);
            if (pre) {
                this.layoutTextPre(box, te, fontData, text, idx, end, inheritHover);
            }
            else {
                this.layoutText(box, te, fontData, text, idx, end, inheritHover);
            }
            if (end < text.length() && text.charAt(end) == '\n') {
                ++end;
                box.nextLine(true);
            }
        }
        box.wasPreformatted = pre;
    }

    private void layoutText(final Box box, final TextAreaModel.TextElement te, final FontData fontData, final String text, int textStart, int textEnd, final boolean inheritHover) {
        int idx = textStart;
        while (textStart < textEnd && isSkip(text.charAt(textStart))) {
            ++textStart;
        }
        boolean endsWithSpace = false;
        while (textEnd > textStart && isSkip(text.charAt(textEnd - 1))) {
            endsWithSpace = true;
            --textEnd;
        }
        final Font font = fontData.font;
        if (textStart > idx && box.prevOnLineEndsNotWithSpace()) {
            box.curX += font.getSpaceWidth();
        }
        Boolean breakWord = null;
        idx = textStart;
        while (idx < textEnd) {
            assert !isSkip(text.charAt(idx));
            int end = idx;
            int visibleEnd = idx;
            if (box.textAlignment != TextAreaModel.HAlignment.JUSTIFY) {
                end = idx + font.computeVisibleGlpyhs((CharSequence)text, idx, textEnd, box.getRemaining());
                if ((visibleEnd = end) < textEnd) {
                    while (end > idx && isPunctuation(text.charAt(end))) {
                        --end;
                    }
                    if (!isBreak(text.charAt(end))) {
                        while (end > idx && !isBreak(text.charAt(end - 1))) {
                            --end;
                        }
                    }
                }
                while (end > idx && isSkip(text.charAt(end - 1))) {
                    --end;
                }
            }
            boolean advancePastFloaters = false;
            if (end == idx) {
                if (box.textAlignment != TextAreaModel.HAlignment.JUSTIFY && box.nextLine(false)) {
                    continue;
                }
                if (breakWord == null) {
                    breakWord = (Boolean)te.getStyle().get(StyleAttribute.BREAKWORD, this.styleClassResolver);
                }
                if (breakWord) {
                    if (visibleEnd == idx) {
                        end = idx + 1;
                    }
                    else {
                        end = visibleEnd;
                    }
                }
                else {
                    while (end < textEnd && !isBreak(text.charAt(end))) {
                        ++end;
                    }
                    while (end < textEnd && isPunctuation(text.charAt(end))) {
                        ++end;
                    }
                }
                advancePastFloaters = true;
            }
            if (idx < end) {
                final LText lt = new LText((TextAreaModel.Element)te, fontData, text, idx, end, box.doCacheText);
                if (advancePastFloaters) {
                    box.advancePastFloaters(lt.width, box.marginLeft, box.marginRight);
                }
                if (box.textAlignment == TextAreaModel.HAlignment.JUSTIFY && box.getRemaining() < lt.width) {
                    box.nextLine(false);
                }
                int width = lt.width;
                if (end < textEnd && isSkip(text.charAt(end))) {
                    width += font.getSpaceWidth();
                }
                lt.x = box.getXAndAdvance(width);
                lt.marginTop = (short)box.marginTop;
                lt.href = box.href;
                lt.inheritHover = inheritHover;
                box.layout.add(lt);
            }
            for (idx = end; idx < textEnd && isSkip(text.charAt(idx)); ++idx) {}
        }
        if (!box.isAtStartOfLine() && endsWithSpace) {
            box.curX += font.getSpaceWidth();
        }
    }

    private void layoutTextPre(final Box box, final TextAreaModel.TextElement te, final FontData fontData, final String text, final int textStart, final int textEnd, final boolean inheritHover) {
        final Font font = fontData.font;
        int idx = textStart;
        while (true) {
            Label_0224: {
                if (idx < textEnd) {
                    if (text.charAt(idx) == '\t') {
                        ++idx;
                        final int tabX = box.computeNextTabStop(te.getStyle(), font);
                        if (tabX < box.lineWidth) {
                            box.curX = tabX;
                        }
                        else if (!box.isAtStartOfLine()) {
                            break Label_0224;
                        }
                    }
                    final int tabIdx = text.indexOf(9, idx);
                    int end = textEnd;
                    if (tabIdx >= 0 && tabIdx < textEnd) {
                        end = tabIdx;
                    }
                    if (end > idx) {
                        final int count = font.computeVisibleGlpyhs((CharSequence)text, idx, end, box.getRemaining());
                        if (count == 0 && !box.isAtStartOfLine()) {
                            break Label_0224;
                        }
                        end = idx + Math.max(1, count);
                        final LText lt = new LText((TextAreaModel.Element)te, fontData, text, idx, end, box.doCacheText);
                        lt.x = box.getXAndAdvance(lt.width);
                        lt.marginTop = (short)box.marginTop;
                        lt.inheritHover = inheritHover;
                        box.layout.add(lt);
                    }
                    idx = end;
                    continue;
                }
            }
            if (idx >= textEnd) {
                break;
            }
            box.nextLine(false);
        }
    }

    private void doMarginTop(final Box box, final Style style) {
        final int marginTop = this.convertToPX0(style, (StyleAttribute<Value>)StyleAttribute.MARGIN_TOP, box.boxWidth);
        box.nextLine(false);
        box.advanceToY(box.computeTopPadding(marginTop));
    }

    private void doMarginBottom(final Box box, final Style style) {
        final int marginBottom = this.convertToPX0(style, (StyleAttribute<Value>)StyleAttribute.MARGIN_BOTTOM, box.boxWidth);
        box.setMarginBottom(marginBottom);
    }

    private void layoutContainerElement(final Box box, final TextAreaModel.ContainerElement ce) {
        final Style style = ce.getStyle();
        this.doMarginTop(box, style);
        box.addAnchor((TextAreaModel.Element)ce);
        this.layoutElements(box, (Iterable<TextAreaModel.Element>)ce);
        this.doMarginBottom(box, style);
    }

    private void layoutLinkElement(final Box box, final TextAreaModel.LinkElement le) {
        final String oldHref = box.href;
        box.href = le.getHREF();
        final Style style = le.getStyle();
        final TextAreaModel.Display display = (TextAreaModel.Display)style.get(StyleAttribute.DISPLAY, this.styleClassResolver);
        if (display == TextAreaModel.Display.BLOCK) {
            this.layoutBlockElement(box, (TextAreaModel.ContainerElement)le);
        }
        else {
            this.layoutContainerElement(box, (TextAreaModel.ContainerElement)le);
        }
        box.href = oldHref;
    }

    private void layoutListElement(final Box box, final TextAreaModel.ListElement le) {
        final Style style = le.getStyle();
        this.doMarginTop(box, style);
        final Image image = this.selectImage(style, (StyleAttribute<String>)StyleAttribute.LIST_STYLE_IMAGE);
        if (image != null) {
            final LImage li = new LImage((TextAreaModel.Element)le, image);
            li.marginRight = (short)this.convertToPX0(style, (StyleAttribute<Value>)StyleAttribute.PADDING_LEFT, box.boxWidth);
            this.layout(box, (TextAreaModel.Element)le, li, TextAreaModel.FloatPosition.LEFT, TextAreaModel.Display.BLOCK);
            final int imageHeight = li.height;
            li.height = 32767;
            this.layoutElements(box, (Iterable<TextAreaModel.Element>)le);
            li.height = imageHeight;
            box.objLeft.remove(li);
            box.advanceToY(li.bottom());
            box.computePadding();
        }
        else {
            this.layoutElements(box, (Iterable<TextAreaModel.Element>)le);
            box.nextLine(false);
        }
        this.doMarginBottom(box, style);
    }

    private void layoutOrderedListElement(final Box box, final TextAreaModel.OrderedListElement ole) {
        final Style style = ole.getStyle();
        final FontData fontData = this.createFontData(style);
        if (fontData == null) {
            return;
        }
        this.doMarginTop(box, style);
        final LElement anchor = box.addAnchor((TextAreaModel.Element)ole);
        final int start = Math.max(1, ole.getStart());
        final int count = ole.getNumElements();
        final OrderedListType type = (OrderedListType)style.get(StyleAttribute.LIST_STYLE_TYPE, this.styleClassResolver);
        final String[] labels = new String[count];
        int maxLabelWidth = this.convertToPX0(style, (StyleAttribute<Value>)StyleAttribute.PADDING_LEFT, box.boxWidth);
        for (int i = 0; i < count; ++i) {
            labels[i] = type.format(start + i).concat(". ");
            final int width = fontData.font.computeTextWidth((CharSequence)labels[i]);
            maxLabelWidth = Math.max(maxLabelWidth, width);
        }
        for (int i = 0; i < count; ++i) {
            final String label = labels[i];
            final TextAreaModel.Element li = ole.getElement(i);
            final Style liStyle = li.getStyle();
            this.doMarginTop(box, liStyle);
            final LText lt = new LText((TextAreaModel.Element)ole, fontData, label, 0, label.length(), box.doCacheText);
            final int labelWidth = lt.width;
            final int labelHeight = lt.height;
            final LText lText = lt;
            lText.width += this.convertToPX0(liStyle, (StyleAttribute<Value>)StyleAttribute.PADDING_LEFT, box.boxWidth);
            this.layout(box, (TextAreaModel.Element)ole, lt, TextAreaModel.FloatPosition.LEFT, TextAreaModel.Display.BLOCK);
            final LText lText2 = lt;
            lText2.x += Math.max(0, maxLabelWidth - labelWidth);
            lt.height = 32767;
            this.layoutElement(box, li);
            lt.height = labelHeight;
            box.objLeft.remove(lt);
            box.advanceToY(lt.bottom());
            box.computePadding();
            this.doMarginBottom(box, liStyle);
        }
        anchor.height = box.curY - anchor.y;
        this.doMarginBottom(box, style);
    }

    private Box layoutBox(final LClip clip, final int continerWidth, final int paddingLeft, final int paddingRight, final TextAreaModel.ContainerElement ce, final String href, final boolean doCacheText) {
        final Style style = ce.getStyle();
        final int paddingTop = this.convertToPX0(style, (StyleAttribute<Value>)StyleAttribute.PADDING_TOP, continerWidth);
        final int paddingBottom = this.convertToPX0(style, (StyleAttribute<Value>)StyleAttribute.PADDING_BOTTOM, continerWidth);
        final int marginBottom = this.convertToPX0(style, (StyleAttribute<Value>)StyleAttribute.MARGIN_BOTTOM, continerWidth);
        final Box box = new Box(clip, paddingLeft, paddingRight, paddingTop, doCacheText);
        box.href = href;
        box.style = style;
        this.layoutElements(box, (Iterable<TextAreaModel.Element>)ce);
        box.finish();
        final int contentHeight = box.curY + paddingBottom;
        final int boxHeight = Math.max(contentHeight, this.convertToPX(style, (StyleAttribute<Value>)StyleAttribute.HEIGHT, contentHeight, contentHeight));
        if (boxHeight > contentHeight) {
            int amount = 0;
            switch ((TextAreaModel.VAlignment)style.get(StyleAttribute.VERTICAL_ALIGNMENT, this.styleClassResolver)) {
                case BOTTOM: {
                    amount = boxHeight - contentHeight;
                    break;
                }
                case FILL:
                case MIDDLE: {
                    amount = (boxHeight - contentHeight) / 2;
                    break;
                }
            }
            if (amount > 0) {
                clip.moveContentY(amount);
            }
        }
        clip.height = boxHeight;
        clip.marginBottom = (short)Math.max(marginBottom, box.marginBottomAbs - box.curY);
        return box;
    }

    private void layoutBlockElement(final Box box, final TextAreaModel.ContainerElement be) {
        box.nextLine(false);
        final Style style = be.getStyle();
        final TextAreaModel.FloatPosition floatPosition = (TextAreaModel.FloatPosition)style.get(StyleAttribute.FLOAT_POSITION, this.styleClassResolver);
        final LImage bgImage = this.createBGImage(box, (TextAreaModel.Element)be);
        final int marginTop = this.convertToPX0(style, (StyleAttribute<Value>)StyleAttribute.MARGIN_TOP, box.boxWidth);
        final int marginLeft = this.convertToPX0(style, (StyleAttribute<Value>)StyleAttribute.MARGIN_LEFT, box.boxWidth);
        final int marginRight = this.convertToPX0(style, (StyleAttribute<Value>)StyleAttribute.MARGIN_RIGHT, box.boxWidth);
        int bgX = box.computeLeftPadding(marginLeft);
        int bgY = box.computeTopPadding(marginTop);
        int remaining = Math.max(0, box.computeRightPadding(marginRight) - bgX);
        final int paddingLeft = this.convertToPX0(style, (StyleAttribute<Value>)StyleAttribute.PADDING_LEFT, box.boxWidth);
        final int paddingRight = this.convertToPX0(style, (StyleAttribute<Value>)StyleAttribute.PADDING_RIGHT, box.boxWidth);
        int bgWidth;
        if (floatPosition == TextAreaModel.FloatPosition.NONE) {
            bgWidth = this.convertToPX(style, (StyleAttribute<Value>)StyleAttribute.WIDTH, remaining, remaining);
        }
        else {
            bgWidth = this.convertToPX(style, (StyleAttribute<Value>)StyleAttribute.WIDTH, box.boxWidth, Integer.MIN_VALUE);
            if (bgWidth == Integer.MIN_VALUE) {
                final LClip dummy = new LClip(null);
                dummy.width = Math.max(0, box.lineWidth - paddingLeft - paddingRight);
                final Box dummyBox = this.layoutBox(dummy, box.boxWidth, paddingLeft, paddingRight, be, null, false);
                dummyBox.nextLine(false);
                bgWidth = Math.max(0, dummy.width - dummyBox.minRemainingWidth);
            }
        }
        bgWidth = Math.max(0, bgWidth) + paddingLeft + paddingRight;
        if (floatPosition != TextAreaModel.FloatPosition.NONE) {
            box.advancePastFloaters(bgWidth, marginLeft, marginRight);
            bgX = box.computeLeftPadding(marginLeft);
            bgY = Math.max(bgY, box.curY);
            remaining = Math.max(0, box.computeRightPadding(marginRight) - bgX);
        }
        bgWidth = Math.min(bgWidth, remaining);
        if (floatPosition == TextAreaModel.FloatPosition.RIGHT) {
            bgX = box.computeRightPadding(marginRight) - bgWidth;
        }
        final LClip clip = new LClip((TextAreaModel.Element)be);
        clip.x = bgX;
        clip.y = bgY;
        clip.width = bgWidth;
        clip.marginLeft = (short)marginLeft;
        clip.marginRight = (short)marginRight;
        clip.href = box.href;
        box.layout.add(clip);
        final Box clipBox = this.layoutBox(clip, box.boxWidth, paddingLeft, paddingRight, be, box.href, box.doCacheText);
        box.lineStartIdx = box.layout.size();
        if (floatPosition == TextAreaModel.FloatPosition.NONE) {
            box.advanceToY(bgY + clip.height);
            box.setMarginBottom(clip.marginBottom);
            box.accountMinRemaining(clipBox.minRemainingWidth);
        }
        else {
            if (floatPosition == TextAreaModel.FloatPosition.RIGHT) {
                box.objRight.add(clip);
            }
            else {
                box.objLeft.add(clip);
            }
            box.computePadding();
        }
        if (bgImage != null) {
            bgImage.x = bgX;
            bgImage.y = bgY;
            bgImage.width = bgWidth;
            bgImage.height = clip.height;
            bgImage.hoverSrc = clip;
        }
    }

    private void computeTableWidth(final TextAreaModel.TableElement te, final int maxTableWidth, final int[] columnWidth, final int[] columnSpacing, final boolean[] columnsWithFixedWidth) {
        final int numColumns = te.getNumColumns();
        final int numRows = te.getNumRows();
        final int cellSpacing = te.getCellSpacing();
        final int cellPadding = te.getCellPadding();
        HashMap<Integer, Integer> colspanWidths = null;
        for (int col = 0; col < numColumns; ++col) {
            int width = 0;
            int marginLeft = 0;
            int marginRight = 0;
            boolean hasFixedWidth = false;
            for (int row = 0; row < numRows; ++row) {
                final TextAreaModel.TableCellElement cell = te.getCell(row, col);
                if (cell != null) {
                    final Style cellStyle = cell.getStyle();
                    final int colspan = cell.getColspan();
                    int cellWidth = this.convertToPX(cellStyle, (StyleAttribute<Value>)StyleAttribute.WIDTH, maxTableWidth, Integer.MIN_VALUE);
                    if (cellWidth == Integer.MIN_VALUE && (colspan > 1 || !hasFixedWidth)) {
                        final int paddingLeft = Math.max(cellPadding, this.convertToPX0(cellStyle, (StyleAttribute<Value>)StyleAttribute.PADDING_LEFT, maxTableWidth));
                        final int paddingRight = Math.max(cellPadding, this.convertToPX0(cellStyle, (StyleAttribute<Value>)StyleAttribute.PADDING_RIGHT, maxTableWidth));
                        final LClip dummy = new LClip(null);
                        dummy.width = maxTableWidth;
                        final Box dummyBox = this.layoutBox(dummy, maxTableWidth, paddingLeft, paddingRight, (TextAreaModel.ContainerElement)cell, null, false);
                        dummyBox.finish();
                        cellWidth = maxTableWidth - dummyBox.minRemainingWidth;
                    }
                    else if (colspan == 1 && cellWidth >= 0) {
                        hasFixedWidth = true;
                    }
                    if (colspan > 1) {
                        if (colspanWidths == null) {
                            colspanWidths = new HashMap<Integer, Integer>();
                        }
                        final Integer key = (col << 16) + colspan;
                        final Integer value = colspanWidths.get(key);
                        if (value == null || cellWidth > value) {
                            colspanWidths.put(key, cellWidth);
                        }
                    }
                    else {
                        width = Math.max(width, cellWidth);
                        marginLeft = Math.max(marginLeft, this.convertToPX(cellStyle, (StyleAttribute<Value>)StyleAttribute.MARGIN_LEFT, maxTableWidth, 0));
                        marginRight = Math.max(marginRight, this.convertToPX(cellStyle, (StyleAttribute<Value>)StyleAttribute.MARGIN_LEFT, maxTableWidth, 0));
                    }
                }
            }
            columnsWithFixedWidth[col] = hasFixedWidth;
            columnWidth[col] = width;
            columnSpacing[col] = Math.max(columnSpacing[col], marginLeft);
            columnSpacing[col + 1] = Math.max(cellSpacing, marginRight);
        }
        if (colspanWidths != null) {
            for (final Map.Entry<Integer, Integer> e : colspanWidths.entrySet()) {
                final int key2 = e.getKey();
                final int col2 = key2 >>> 16;
                final int colspan2 = key2 & 0xFFFF;
                int width2 = e.getValue();
                int remainingCols = colspan2;
                for (int i = 0; i < colspan2; ++i) {
                    if (columnsWithFixedWidth[col2 + i]) {
                        width2 -= columnWidth[col2 + i];
                        --remainingCols;
                    }
                }
                if (width2 > 0) {
                    for (int i = 0; i < colspan2 && remainingCols > 0; ++i) {
                        if (!columnsWithFixedWidth[col2 + i]) {
                            final int colWidth = width2 / remainingCols;
                            columnWidth[col2 + i] = Math.max(columnWidth[col2 + i], colWidth);
                            width2 -= colWidth;
                            --remainingCols;
                        }
                    }
                }
            }
        }
    }

    private void layoutTableElement(final Box box, final TextAreaModel.TableElement te) {
        final int numColumns = te.getNumColumns();
        final int numRows = te.getNumRows();
        final int cellSpacing = te.getCellSpacing();
        final int cellPadding = te.getCellPadding();
        final Style tableStyle = te.getStyle();
        if (numColumns == 0 || numRows == 0) {
            return;
        }
        this.doMarginTop(box, tableStyle);
        final LElement anchor = box.addAnchor((TextAreaModel.Element)te);
        final int left = box.computeLeftPadding(this.convertToPX0(tableStyle, (StyleAttribute<Value>)StyleAttribute.MARGIN_LEFT, box.boxWidth));
        final int right = box.computeRightPadding(this.convertToPX0(tableStyle, (StyleAttribute<Value>)StyleAttribute.MARGIN_RIGHT, box.boxWidth));
        final int maxTableWidth = Math.max(0, right - left);
        int tableWidth = Math.min(maxTableWidth, this.convertToPX(tableStyle, (StyleAttribute<Value>)StyleAttribute.WIDTH, box.boxWidth, Integer.MIN_VALUE));
        final boolean autoTableWidth = tableWidth == Integer.MIN_VALUE;
        if (tableWidth <= 0) {
            tableWidth = maxTableWidth;
        }
        final int[] columnWidth = new int[numColumns];
        final int[] columnSpacing = new int[numColumns + 1];
        final boolean[] columnsWithFixedWidth = new boolean[numColumns];
        columnSpacing[0] = Math.max(cellSpacing, this.convertToPX0(tableStyle, (StyleAttribute<Value>)StyleAttribute.PADDING_LEFT, box.boxWidth));
        this.computeTableWidth(te, tableWidth, columnWidth, columnSpacing, columnsWithFixedWidth);
        columnSpacing[numColumns] = Math.max(columnSpacing[numColumns], this.convertToPX0(tableStyle, (StyleAttribute<Value>)StyleAttribute.PADDING_RIGHT, box.boxWidth));
        int columnSpacingSum = 0;
        for (final int spacing : columnSpacing) {
            columnSpacingSum += spacing;
        }
        int columnWidthSum = 0;
        for (final int width : columnWidth) {
            columnWidthSum += width;
        }
        if (autoTableWidth) {
            tableWidth = Math.min(maxTableWidth, columnWidthSum + columnSpacingSum);
        }
        final int availableColumnWidth = Math.max(0, tableWidth - columnSpacingSum);
        if (availableColumnWidth != columnWidthSum && columnWidthSum > 0) {
            int available = availableColumnWidth;
            int toDistribute = columnWidthSum;
            int remainingCols = numColumns;
            for (int col = 0; col < numColumns; ++col) {
                if (columnsWithFixedWidth[col]) {
                    final int width2 = columnWidth[col];
                    available -= width2;
                    toDistribute -= width2;
                    --remainingCols;
                }
            }
            boolean allColumns = false;
            if (availableColumnWidth < 0) {
                available = availableColumnWidth;
                toDistribute = columnWidthSum;
                remainingCols = numColumns;
                allColumns = true;
            }
            for (int col2 = 0; col2 < numColumns && remainingCols > 0; ++col2) {
                if (allColumns || !columnsWithFixedWidth[col2]) {
                    final int width3 = columnWidth[col2];
                    final int newWidth = (toDistribute > 0) ? (width3 * available / toDistribute) : 0;
                    columnWidth[col2] = newWidth;
                    available -= newWidth;
                    toDistribute -= width3;
                }
            }
        }
        final LImage tableBGImage = this.createBGImage(box, (TextAreaModel.Element)te);
        box.textAlignment = TextAreaModel.HAlignment.LEFT;
        box.curY += Math.max(cellSpacing, this.convertToPX0(tableStyle, (StyleAttribute<Value>)StyleAttribute.PADDING_TOP, box.boxWidth));
        final LImage[] bgImages = new LImage[numColumns];
        for (int row = 0; row < numRows; ++row) {
            if (row > 0) {
                box.curY += cellSpacing;
            }
            LImage rowBGImage = null;
            final Style rowStyle = te.getRowStyle(row);
            if (rowStyle != null) {
                final int marginTop = this.convertToPX0(rowStyle, (StyleAttribute<Value>)StyleAttribute.MARGIN_TOP, tableWidth);
                box.curY = box.computeTopPadding(marginTop);
                final Image image = this.selectImage(rowStyle, (StyleAttribute<String>)StyleAttribute.BACKGROUND_IMAGE);
                if (image != null) {
                    rowBGImage = new LImage((TextAreaModel.Element)te, image);
                    rowBGImage.y = box.curY;
                    rowBGImage.x = left;
                    rowBGImage.width = tableWidth;
                    box.clip.bgImages.add(rowBGImage);
                }
                box.curY += this.convertToPX0(rowStyle, (StyleAttribute<Value>)StyleAttribute.PADDING_TOP, tableWidth);
                box.minLineHeight = this.convertToPX0(rowStyle, (StyleAttribute<Value>)StyleAttribute.HEIGHT, tableWidth);
            }
            int x = left;
            for (int col3 = 0; col3 < numColumns; ++col3) {
                x += columnSpacing[col3];
                final TextAreaModel.TableCellElement cell = te.getCell(row, col3);
                int width4 = columnWidth[col3];
                if (cell != null) {
                    for (int c = 1; c < cell.getColspan(); ++c) {
                        width4 += columnSpacing[col3 + c] + columnWidth[col3 + c];
                    }
                    final Style cellStyle = cell.getStyle();
                    final int paddingLeft = Math.max(cellPadding, this.convertToPX0(cellStyle, (StyleAttribute<Value>)StyleAttribute.PADDING_LEFT, tableWidth));
                    final int paddingRight = Math.max(cellPadding, this.convertToPX0(cellStyle, (StyleAttribute<Value>)StyleAttribute.PADDING_RIGHT, tableWidth));
                    final LClip clip = new LClip((TextAreaModel.Element)cell);
                    final LImage bgImage = this.createBGImage(box, (TextAreaModel.Element)cell);
                    if (bgImage != null) {
                        bgImage.x = x;
                        bgImage.width = width4;
                        bgImage.hoverSrc = clip;
                        bgImages[col3] = bgImage;
                    }
                    clip.x = x;
                    clip.y = box.curY;
                    clip.width = width4;
                    clip.marginTop = (short)this.convertToPX0(cellStyle, (StyleAttribute<Value>)StyleAttribute.MARGIN_TOP, tableWidth);
                    box.layout.add(clip);
                    this.layoutBox(clip, tableWidth, paddingLeft, paddingRight, (TextAreaModel.ContainerElement)cell, null, box.doCacheText);
                    col3 += Math.max(0, cell.getColspan() - 1);
                }
                x += width4;
            }
            box.nextLine(false);
            for (int col3 = 0; col3 < numColumns; ++col3) {
                final LImage bgImage2 = bgImages[col3];
                if (bgImage2 != null) {
                    bgImage2.height = box.curY - bgImage2.y;
                    bgImages[col3] = null;
                }
            }
            if (rowStyle != null) {
                box.curY += this.convertToPX0(rowStyle, (StyleAttribute<Value>)StyleAttribute.PADDING_BOTTOM, tableWidth);
                if (rowBGImage != null) {
                    rowBGImage.height = box.curY - rowBGImage.y;
                }
                this.doMarginBottom(box, rowStyle);
            }
        }
        box.curY += Math.max(cellSpacing, this.convertToPX0(tableStyle, (StyleAttribute<Value>)StyleAttribute.PADDING_BOTTOM, box.boxWidth));
        box.checkFloaters();
        box.accountMinRemaining(Math.max(0, box.lineWidth - tableWidth));
        if (tableBGImage != null) {
            tableBGImage.height = box.curY - tableBGImage.y;
            tableBGImage.x = left;
            tableBGImage.width = tableWidth;
        }
        anchor.x = left;
        anchor.width = tableWidth;
        anchor.height = box.curY - anchor.y;
        this.doMarginBottom(box, tableStyle);
    }

    private LImage createBGImage(final Box box, final TextAreaModel.Element element) {
        final Style style = element.getStyle();
        Image image = this.selectImage(style, (StyleAttribute<String>)StyleAttribute.BACKGROUND_IMAGE);
        if (image == null) {
            image = this.createBackgroundColor(style);
        }
        if (image != null) {
            final LImage bgImage = new LImage(element, image);
            bgImage.y = box.curY;
            box.clip.bgImages.add(bgImage);
            return bgImage;
        }
        return null;
    }

    private Image createBackgroundColor(final Style style) {
        final Color color = (Color)style.get(StyleAttribute.BACKGROUND_COLOR, this.styleClassResolver);
        if (color.getAlpha() != 0) {
            final Image white = this.selectImage("white");
            if (white != null) {
                final Image image = white.createTintedVersion(color);
                final Color colorHover = (Color)style.get(StyleAttribute.BACKGROUND_COLOR_HOVER, this.styleClassResolver);
                if (colorHover != null) {
                    return (Image)new StateSelectImage(TextArea.HOVER_STATESELECT, null, new Image[] { white.createTintedVersion(colorHover), image });
                }
                return image;
            }
        }
        return null;
    }

    static boolean isSkip(final char ch) {
        return Character.isWhitespace(ch);
    }

    static boolean isPunctuation(final char ch) {
        return ":;,.-!?".indexOf(ch) >= 0;
    }

    static boolean isBreak(final char ch) {
        return Character.isWhitespace(ch) || isPunctuation(ch) || ch == '\u3001' || ch == '\u3002';
    }

    static {
        STATE_HOVER = AnimationState.StateKey.get("hover");
        EMPTY_CHAR_ARRAY = new char[0];
        HOVER_STATESELECT = new StateSelect(new StateExpression[] { new StateExpression.Check(TextArea.STATE_HOVER) });
    }

    private static final class FontMapperCacheEntry
    {
        final int fontSize;
        final int fontStyle;
        final StringList fontFamilies;
        final TextDecoration tdNormal;
        final TextDecoration tdHover;
        final int hashCode;
        final Font font;
        FontMapperCacheEntry next;

        FontMapperCacheEntry(final int fontSize, final int fontStyle, final StringList fontFamilies, final TextDecoration tdNormal, final TextDecoration tdHover, final int hashCode, final Font font) {
            this.fontSize = fontSize;
            this.fontStyle = fontStyle;
            this.fontFamilies = fontFamilies;
            this.tdNormal = tdNormal;
            this.tdHover = tdHover;
            this.hashCode = hashCode;
            this.font = font;
        }
    }

    class Box
    {
        final LClip clip;
        final ArrayList<LElement> layout;
        final ArrayList<LElement> objLeft;
        final ArrayList<LElement> objRight;
        final StringBuilder lineInfo;
        final int boxLeft;
        final int boxWidth;
        final int boxMarginOffsetLeft;
        final int boxMarginOffsetRight;
        final boolean doCacheText;
        int curY;
        int curX;
        int lineStartIdx;
        int lastProcessedAnchorIdx;
        int marginTop;
        int marginLeft;
        int marginRight;
        int marginBottomAbs;
        int marginBottomNext;
        int lineStartX;
        int lineWidth;
        int fontLineHeight;
        int minLineHeight;
        int lastLineEnd;
        int lastLineBottom;
        int minRemainingWidth;
        boolean inParagraph;
        boolean wasAutoBreak;
        boolean wasPreformatted;
        TextAreaModel.HAlignment textAlignment;
        String href;
        Style style;

        Box(final LClip clip, final int paddingLeft, final int paddingRight, final int paddingTop, final boolean doCacheText) {
            this.objLeft = new ArrayList<LElement>();
            this.objRight = new ArrayList<LElement>();
            this.lineInfo = new StringBuilder();
            this.clip = clip;
            this.layout = clip.layout;
            this.boxLeft = paddingLeft;
            this.boxWidth = Math.max(0, clip.width - paddingLeft - paddingRight);
            this.boxMarginOffsetLeft = paddingLeft;
            this.boxMarginOffsetRight = paddingRight;
            this.doCacheText = doCacheText;
            this.curX = paddingLeft;
            this.curY = paddingTop;
            this.lineStartX = paddingLeft;
            this.lineWidth = this.boxWidth;
            this.minRemainingWidth = this.boxWidth;
            this.textAlignment = TextAreaModel.HAlignment.LEFT;
            assert this.layout.isEmpty();
        }

        void computePadding() {
            final int left = this.computeLeftPadding(this.marginLeft);
            final int right = this.computeRightPadding(this.marginRight);
            this.lineStartX = left;
            this.lineWidth = Math.max(0, right - left);
            if (this.isAtStartOfLine()) {
                this.curX = this.lineStartX;
            }
            this.accountMinRemaining(this.getRemaining());
        }

        int computeLeftPadding(final int marginLeft) {
            int left = this.boxLeft + Math.max(0, marginLeft - this.boxMarginOffsetLeft);
            for (int i = 0, n = this.objLeft.size(); i < n; ++i) {
                final LElement e = this.objLeft.get(i);
                left = Math.max(left, e.x + e.width + Math.max(e.marginRight, marginLeft));
            }
            return left;
        }

        int computeRightPadding(final int marginRight) {
            int right = this.boxLeft + this.boxWidth - Math.max(0, marginRight - this.boxMarginOffsetRight);
            for (int i = 0, n = this.objRight.size(); i < n; ++i) {
                final LElement e = this.objRight.get(i);
                right = Math.min(right, e.x - Math.max(e.marginLeft, marginRight));
            }
            return right;
        }

        int computePaddingWidth(final int marginLeft, final int marginRight) {
            return Math.max(0, this.computeRightPadding(marginRight) - this.computeLeftPadding(marginLeft));
        }

        int computeTopPadding(final int marginTop) {
            return Math.max(this.marginBottomAbs, this.curY + marginTop);
        }

        void setMarginBottom(final int marginBottom) {
            if (this.isAtStartOfLine()) {
                this.marginBottomAbs = Math.max(this.marginBottomAbs, this.curY + marginBottom);
            }
            else {
                this.marginBottomNext = Math.max(this.marginBottomNext, marginBottom);
            }
        }

        int getRemaining() {
            return Math.max(0, this.lineWidth - this.curX + this.lineStartX);
        }

        void accountMinRemaining(final int remaining) {
            this.minRemainingWidth = Math.min(this.minRemainingWidth, remaining);
        }

        int getXAndAdvance(final int amount) {
            final int x = this.curX;
            this.curX = x + amount;
            return x;
        }

        boolean isAtStartOfLine() {
            return this.lineStartIdx == this.layout.size();
        }

        boolean prevOnLineEndsNotWithSpace() {
            final int layoutSize = this.layout.size();
            if (this.lineStartIdx >= layoutSize) {
                return false;
            }
            final LElement le = this.layout.get(layoutSize - 1);
            if (le instanceof LText) {
                final LText lt = (LText)le;
                return !TextArea.isSkip(lt.text.charAt(lt.end - 1));
            }
            return true;
        }

        void checkFloaters() {
            this.removeObjFromList(this.objLeft);
            this.removeObjFromList(this.objRight);
            this.computePadding();
        }

        void clearFloater(final TextAreaModel.Clear clear) {
            if (clear != TextAreaModel.Clear.NONE) {
                int targetY = -1;
                if (clear == TextAreaModel.Clear.LEFT || clear == TextAreaModel.Clear.BOTH) {
                    for (int i = 0, n = this.objLeft.size(); i < n; ++i) {
                        final LElement le = this.objLeft.get(i);
                        if (le.height != 32767) {
                            targetY = Math.max(targetY, le.y + le.height);
                        }
                    }
                }
                if (clear == TextAreaModel.Clear.RIGHT || clear == TextAreaModel.Clear.BOTH) {
                    for (int i = 0, n = this.objRight.size(); i < n; ++i) {
                        final LElement le = this.objRight.get(i);
                        targetY = Math.max(targetY, le.y + le.height);
                    }
                }
                if (targetY >= 0) {
                    this.advanceToY(targetY);
                }
            }
        }

        void advanceToY(final int targetY) {
            this.nextLine(false);
            if (targetY > this.curY) {
                this.curY = targetY;
                this.checkFloaters();
            }
        }

        void advancePastFloaters(final int requiredWidth, final int marginLeft, final int marginRight) {
            if (this.computePaddingWidth(marginLeft, marginRight) < requiredWidth) {
                this.nextLine(false);
                do {
                    int targetY = Integer.MAX_VALUE;
                    if (!this.objLeft.isEmpty()) {
                        final LElement le = this.objLeft.get(this.objLeft.size() - 1);
                        if (le.height != 32767) {
                            targetY = Math.min(targetY, le.bottom());
                        }
                    }
                    if (!this.objRight.isEmpty()) {
                        final LElement le = this.objRight.get(this.objRight.size() - 1);
                        targetY = Math.min(targetY, le.bottom());
                    }
                    if (targetY == Integer.MAX_VALUE || targetY < this.curY) {
                        return;
                    }
                    this.curY = targetY;
                    this.checkFloaters();
                } while (this.computePaddingWidth(marginLeft, marginRight) < requiredWidth);
            }
        }

        boolean nextLine(final boolean force) {
            if (this.isAtStartOfLine() && (this.wasAutoBreak || !force)) {
                return false;
            }
            this.accountMinRemaining(this.getRemaining());
            int targetY = this.curY;
            int lineHeight = this.minLineHeight;
            if (this.isAtStartOfLine()) {
                lineHeight = Math.max(lineHeight, this.fontLineHeight);
            }
            else {
                for (int idx = this.lineStartIdx; idx < this.layout.size(); ++idx) {
                    final LElement le = this.layout.get(idx);
                    lineHeight = Math.max(lineHeight, le.height);
                }
                final LElement lastElement = this.layout.get(this.layout.size() - 1);
                final int remaining = this.lineStartX + this.lineWidth - (lastElement.x + lastElement.width);
                switch (this.textAlignment) {
                    case RIGHT: {
                        for (int idx2 = this.lineStartIdx; idx2 < this.layout.size(); ++idx2) {
                            final LElement lElement;
                            final LElement le2 = lElement = this.layout.get(idx2);
                            lElement.x += remaining;
                        }
                        break;
                    }
                    case CENTER: {
                        final int offset = remaining / 2;
                        for (int idx3 = this.lineStartIdx; idx3 < this.layout.size(); ++idx3) {
                            final LElement lElement2;
                            final LElement le3 = lElement2 = this.layout.get(idx3);
                            lElement2.x += offset;
                        }
                        break;
                    }
                    case JUSTIFY: {
                        if (remaining < this.lineWidth / 4) {
                            for (int num = this.layout.size() - this.lineStartIdx, i = 1; i < num; ++i) {
                                final LElement le3 = this.layout.get(this.lineStartIdx + i);
                                final int offset2 = remaining * i / (num - 1);
                                final LElement lElement3 = le3;
                                lElement3.x += offset2;
                            }
                            break;
                        }
                        break;
                    }
                }
                for (int idx2 = this.lineStartIdx; idx2 < this.layout.size(); ++idx2) {
                    final LElement le2 = this.layout.get(idx2);
                    switch ((TextAreaModel.VAlignment)le2.element.getStyle().get(StyleAttribute.VERTICAL_ALIGNMENT, TextArea.this.styleClassResolver)) {
                        case BOTTOM: {
                            le2.y = lineHeight - le2.height;
                            break;
                        }
                        case TOP: {
                            le2.y = 0;
                            break;
                        }
                        case MIDDLE: {
                            le2.y = (lineHeight - le2.height) / 2;
                            break;
                        }
                        case FILL: {
                            le2.y = 0;
                            le2.height = lineHeight;
                            break;
                        }
                    }
                    targetY = Math.max(targetY, this.computeTopPadding(le2.marginTop - le2.y));
                    this.marginBottomNext = Math.max(this.marginBottomNext, le2.bottom() - lineHeight);
                }
                for (int idx2 = this.lineStartIdx; idx2 < this.layout.size(); ++idx2) {
                    final LElement lElement4;
                    final LElement le2 = lElement4 = this.layout.get(idx2);
                    lElement4.y += targetY;
                }
            }
            this.processAnchors(targetY, lineHeight);
            this.minLineHeight = 0;
            this.lineStartIdx = this.layout.size();
            this.wasAutoBreak = !force;
            this.curY = targetY + lineHeight;
            this.marginBottomAbs = Math.max(this.marginBottomAbs, this.curY + this.marginBottomNext);
            this.marginBottomNext = 0;
            this.marginTop = 0;
            this.checkFloaters();
            return true;
        }

        void finish() {
            this.nextLine(false);
            this.clearFloater(TextAreaModel.Clear.BOTH);
            this.processAnchors(this.curY, 0);
            final int lineInfoLength = this.lineInfo.length();
            this.clip.lineInfo = new char[lineInfoLength];
            this.lineInfo.getChars(0, lineInfoLength, this.clip.lineInfo, 0);
        }

        int computeNextTabStop(final Style style, final Font font) {
            final int em = font.getEM();
            final int tabSize = (int)style.get(StyleAttribute.TAB_SIZE, TextArea.this.styleClassResolver);
            if (tabSize <= 0 || em <= 0) {
                return this.curX + font.getSpaceWidth();
            }
            final int tabSizePX = Math.min(tabSize, 32767 / em) * em;
            final int x = this.curX - this.lineStartX + font.getSpaceWidth();
            return this.curX + tabSizePX - x % tabSizePX;
        }

        private void removeObjFromList(final ArrayList<LElement> list) {
            int i = list.size();
            while (i-- > 0) {
                final LElement e = list.get(i);
                if (e.bottom() <= this.curY) {
                    list.remove(i);
                }
            }
        }

        void setupTextParams(final Style style, final Font font, final boolean isParagraphStart) {
            if (font != null) {
                this.fontLineHeight = font.getLineHeight();
            }
            else {
                this.fontLineHeight = 0;
            }
            if (isParagraphStart) {
                this.nextLine(false);
                this.inParagraph = true;
            }
            if (isParagraphStart || (!this.inParagraph && this.isAtStartOfLine())) {
                this.marginLeft = TextArea.this.convertToPX0(style, (StyleAttribute<Value>)StyleAttribute.MARGIN_LEFT, this.boxWidth);
                this.marginRight = TextArea.this.convertToPX0(style, (StyleAttribute<Value>)StyleAttribute.MARGIN_RIGHT, this.boxWidth);
                this.textAlignment = (TextAreaModel.HAlignment)style.get(StyleAttribute.HORIZONTAL_ALIGNMENT, TextArea.this.styleClassResolver);
                this.computePadding();
                this.curX = Math.max(0, this.lineStartX + TextArea.this.convertToPX(style, (StyleAttribute<Value>)StyleAttribute.TEXT_INDENT, this.boxWidth, 0));
            }
            this.marginTop = TextArea.this.convertToPX0(style, (StyleAttribute<Value>)StyleAttribute.MARGIN_TOP, this.boxWidth);
        }

        LElement addAnchor(final TextAreaModel.Element e) {
            final LElement le = new LElement(e);
            le.y = this.curY;
            le.x = this.boxLeft;
            le.width = this.boxWidth;
            this.clip.anchors.add(le);
            return le;
        }

        private void processAnchors(final int y, final int height) {
            while (this.lastProcessedAnchorIdx < this.clip.anchors.size()) {
                final LElement le = this.clip.anchors.get(this.lastProcessedAnchorIdx++);
                if (le.height == 0) {
                    le.y = y;
                    le.height = height;
                }
            }
            if (this.lineStartIdx > this.lastLineEnd) {
                this.lineInfo.append('\0').append((char)(this.lineStartIdx - this.lastLineEnd));
            }
            if (y > this.lastLineBottom) {
                this.lineInfo.append((char)y).append('\0');
            }
            this.lastLineBottom = y + height;
            this.lineInfo.append((char)this.lastLineBottom).append((char)(this.layout.size() - this.lineStartIdx));
            this.lastLineEnd = this.layout.size();
        }
    }

    static class RenderInfo
    {
        int offsetX;
        int offsetY;
        Renderer renderer;
        final de.matthiasmann.twl.AnimationState asNormal;
        final de.matthiasmann.twl.AnimationState asHover;

        RenderInfo(final de.matthiasmann.twl.AnimationState parent) {
            (this.asNormal = new de.matthiasmann.twl.AnimationState(parent)).setAnimationState(TextArea.STATE_HOVER, false);
            (this.asHover = new de.matthiasmann.twl.AnimationState(parent)).setAnimationState(TextArea.STATE_HOVER, true);
        }

        de.matthiasmann.twl.AnimationState getAnimationState(final boolean isHover) {
            return isHover ? this.asHover : this.asNormal;
        }
    }

    static class LElement
    {
        final TextAreaModel.Element element;
        int x;
        int y;
        int width;
        int height;
        short marginTop;
        short marginLeft;
        short marginRight;
        short marginBottom;
        String href;
        boolean isHover;
        boolean inheritHover;

        LElement(final TextAreaModel.Element element) {
            this.element = element;
        }

        void adjustWidget(final int offX, final int offY) {
        }

        void collectBGImages(final int offX, final int offY, final ArrayList<LImage> allBGImages) {
        }

        void draw(final RenderInfo ri) {
        }

        void destroy() {
        }

        boolean isInside(final int x, final int y) {
            return x >= this.x && x < this.x + this.width && y >= this.y && y < this.y + this.height;
        }

        LElement find(final int x, final int y) {
            return this;
        }

        LElement find(final TextAreaModel.Element element, final int[] offset) {
            if (this.element == element) {
                return this;
            }
            return null;
        }

        boolean setHover(final LElement le) {
            return this.isHover = (this == le || (le != null && this.element == le.element));
        }

        int bottom() {
            return this.y + this.height + this.marginBottom;
        }
    }

    static class FontData
    {
        final Font font;
        final Color color;
        final Color colorHover;

        FontData(final Font font, final Color color, Color colorHover) {
            if (colorHover == null) {
                colorHover = color;
            }
            this.font = font;
            this.color = maskWhite(color);
            this.colorHover = maskWhite(colorHover);
        }

        public Color getColor(final boolean isHover) {
            return isHover ? this.colorHover : this.color;
        }

        private static Color maskWhite(final Color c) {
            return Color.WHITE.equals((Object)c) ? null : c;
        }
    }

    static class LText extends LElement
    {
        final FontData fontData;
        final String text;
        final int start;
        final int end;
        FontCache cache;

        LText(final TextAreaModel.Element element, final FontData fontData, final String text, final int start, final int end, final boolean doCache) {
            super(element);
            final Font font = fontData.font;
            this.fontData = fontData;
            this.text = text;
            this.start = start;
            this.end = end;
            this.cache = (doCache ? font.cacheText((FontCache)null, (CharSequence)text, start, end) : null);
            this.height = font.getLineHeight();
            if (this.cache != null) {
                this.width = this.cache.getWidth();
            }
            else {
                this.width = font.computeTextWidth((CharSequence)text, start, end);
            }
        }

        @Override
        void draw(final RenderInfo ri) {
            final Color c = this.fontData.getColor(this.isHover);
            if (c != null) {
                this.drawTextWithColor(ri, c);
            }
            else {
                this.drawText(ri);
            }
        }

        private void drawTextWithColor(final RenderInfo ri, final Color c) {
            ri.renderer.pushGlobalTintColor(c.getRedFloat(), c.getGreenFloat(), c.getBlueFloat(), c.getAlphaFloat());
            this.drawText(ri);
            ri.renderer.popGlobalTintColor();
        }

        private void drawText(final RenderInfo ri) {
            final de.matthiasmann.twl.AnimationState as = ri.getAnimationState(this.isHover);
            if (this.cache != null) {
                this.cache.draw((AnimationState)as, this.x + ri.offsetX, this.y + ri.offsetY);
            }
            else {
                this.fontData.font.drawText((AnimationState)as, this.x + ri.offsetX, this.y + ri.offsetY, (CharSequence)this.text, this.start, this.end);
            }
        }

        @Override
        void destroy() {
            if (this.cache != null) {
                this.cache.destroy();
                this.cache = null;
            }
        }
    }

    static class LWidget extends LElement
    {
        final Widget widget;

        LWidget(final TextAreaModel.Element element, final Widget widget) {
            super(element);
            this.widget = widget;
        }

        @Override
        void adjustWidget(final int offX, final int offY) {
            this.widget.setPosition(this.x + offX, this.y + offY);
            this.widget.setSize(this.width, this.height);
        }
    }

    static class LImage extends LElement
    {
        final Image img;
        LElement hoverSrc;

        LImage(final TextAreaModel.Element element, final Image img) {
            super(element);
            this.img = img;
            this.width = img.getWidth();
            this.height = img.getHeight();
            this.hoverSrc = this;
        }

        @Override
        void draw(final RenderInfo ri) {
            this.img.draw((AnimationState)ri.getAnimationState(this.hoverSrc.isHover), this.x + ri.offsetX, this.y + ri.offsetY, this.width, this.height);
        }
    }

    static class LClip extends LElement
    {
        final ArrayList<LElement> layout;
        final ArrayList<LImage> bgImages;
        final ArrayList<LElement> anchors;
        char[] lineInfo;

        LClip(final TextAreaModel.Element element) {
            super(element);
            this.layout = new ArrayList<LElement>();
            this.bgImages = new ArrayList<LImage>();
            this.anchors = new ArrayList<LElement>();
            this.lineInfo = TextArea.EMPTY_CHAR_ARRAY;
        }

        @Override
        void draw(final RenderInfo ri) {
            ri.offsetX += this.x;
            ri.offsetY += this.y;
            ri.renderer.clipEnter(ri.offsetX, ri.offsetY, this.width, this.height);
            try {
                if (!ri.renderer.clipIsEmpty()) {
                    final ArrayList<LElement> ll = this.layout;
                    for (int i = 0, n = ll.size(); i < n; ++i) {
                        ll.get(i).draw(ri);
                    }
                }
            }
            finally {
                ri.renderer.clipLeave();
                ri.offsetX -= this.x;
                ri.offsetY -= this.y;
            }
        }

        @Override
        void adjustWidget(int offX, int offY) {
            offX += this.x;
            offY += this.y;
            for (int i = 0, n = this.layout.size(); i < n; ++i) {
                this.layout.get(i).adjustWidget(offX, offY);
            }
        }

        @Override
        void collectBGImages(int offX, int offY, final ArrayList<LImage> allBGImages) {
            offX += this.x;
            offY += this.y;
            for (int i = 0, n = this.bgImages.size(); i < n; ++i) {
                final LImage lImage;
                final LImage img = lImage = this.bgImages.get(i);
                lImage.x += offX;
                final LImage lImage2 = img;
                lImage2.y += offY;
                allBGImages.add(img);
            }
            for (int i = 0, n = this.layout.size(); i < n; ++i) {
                this.layout.get(i).collectBGImages(offX, offY, allBGImages);
            }
        }

        @Override
        void destroy() {
            for (int i = 0, n = this.layout.size(); i < n; ++i) {
                this.layout.get(i).destroy();
            }
            this.layout.clear();
            this.bgImages.clear();
            this.lineInfo = TextArea.EMPTY_CHAR_ARRAY;
        }

        @Override
        LElement find(int x, int y) {
            x -= this.x;
            y -= this.y;
            int lineTop = 0;
            int layoutIdx = 0;
            int lineBottom;
            for (int lineIdx = 0; lineIdx < this.lineInfo.length && y >= lineTop; lineTop = lineBottom) {
                lineBottom = this.lineInfo[lineIdx++];
                final int layoutCount = this.lineInfo[lineIdx++];
                if (layoutCount > 0) {
                    if (lineBottom == 0 || y < lineBottom) {
                        for (int i = 0; i < layoutCount; ++i) {
                            final LElement le = this.layout.get(layoutIdx + i);
                            if (le.isInside(x, y)) {
                                return le.find(x, y);
                            }
                        }
                        if (lineBottom > 0 && x >= this.layout.get(layoutIdx).x) {
                            LElement prev = null;
                            for (int j = 0; j < layoutCount; ++j) {
                                final LElement le2 = this.layout.get(layoutIdx + j);
                                if (le2.x >= x && (prev == null || prev.element == le2.element)) {
                                    return le2;
                                }
                                prev = le2;
                            }
                        }
                    }
                    layoutIdx += layoutCount;
                }
                if (lineBottom > 0) {}
            }
            return this;
        }

        @Override
        LElement find(final TextAreaModel.Element element, final int[] offset) {
            if (this.element == element) {
                return this;
            }
            LElement match = this.find(this.layout, element, offset);
            if (match == null) {
                match = this.find(this.anchors, element, offset);
            }
            return match;
        }

        private LElement find(final ArrayList<LElement> l, final TextAreaModel.Element e, final int[] offset) {
            for (int i = 0, n = l.size(); i < n; ++i) {
                final LElement match = l.get(i).find(e, offset);
                if (match != null) {
                    if (offset != null) {
                        final int n2 = 0;
                        offset[n2] += this.x;
                        final int n3 = 1;
                        offset[n3] += this.y;
                    }
                    return match;
                }
            }
            return null;
        }

        @Override
        boolean setHover(final LElement le) {
            boolean childHover = false;
            for (int i = 0, n = this.layout.size(); i < n; ++i) {
                childHover |= this.layout.get(i).setHover(le);
            }
            if (childHover) {
                this.isHover = true;
            }
            else {
                super.setHover(le);
            }
            for (int i = 0, n = this.layout.size(); i < n; ++i) {
                final LElement child = this.layout.get(i);
                if (child.inheritHover) {
                    child.isHover = this.isHover;
                }
            }
            return this.isHover;
        }

        void moveContentY(final int amount) {
            for (int i = 0, n = this.layout.size(); i < n; ++i) {
                final LElement lElement = this.layout.get(i);
                lElement.y += amount;
            }
            if (this.lineInfo.length > 0) {
                if (this.lineInfo[1] == '\0') {
                    final char[] lineInfo = this.lineInfo;
                    final int n3 = 0;
                    lineInfo[n3] += (char)amount;
                }
                else {
                    final int n2 = this.lineInfo.length;
                    final char[] tmpLineInfo = new char[n2 + 2];
                    tmpLineInfo[0] = (char)amount;
                    for (int j = 0; j < n2; j += 2) {
                        int lineBottom = this.lineInfo[j];
                        if (lineBottom > 0) {
                            lineBottom += amount;
                        }
                        tmpLineInfo[j + 2] = (char)lineBottom;
                        tmpLineInfo[j + 3] = this.lineInfo[j + 1];
                    }
                    this.lineInfo = tmpLineInfo;
                }
            }
        }
    }

    public interface Callback2 extends Callback
    {
        void handleMouseButton(final Event p0, final TextAreaModel.Element p1);
    }

    public interface Callback
    {
        void handleLinkClicked(final String p0);
    }

    public interface ImageResolver
    {
        Image resolveImage(final String p0);
    }

    public interface WidgetResolver
    {
        Widget resolveWidget(final String p0, final String p1);
    }
}
