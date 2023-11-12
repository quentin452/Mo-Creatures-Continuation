package de.matthiasmann.twl;

import de.matthiasmann.twl.theme.*;
import de.matthiasmann.twl.utils.*;
import java.util.*;
import java.beans.*;
import de.matthiasmann.twl.renderer.*;
import java.security.*;
import java.util.logging.*;

public class Widget
{
    public static final AnimationState.StateKey STATE_KEYBOARD_FOCUS;
    public static final AnimationState.StateKey STATE_HAS_OPEN_POPUPS;
    public static final AnimationState.StateKey STATE_HAS_FOCUSED_CHILD;
    public static final AnimationState.StateKey STATE_DISABLED;
    private static final int LAYOUT_INVALID_LOCAL = 1;
    private static final int LAYOUT_INVALID_GLOBAL = 3;
    private Widget parent;
    private int posX;
    private int posY;
    private int width;
    private int height;
    private int layoutInvalid;
    private boolean clip;
    private boolean visible;
    private boolean hasOpenPopup;
    private boolean enabled;
    private boolean locallyEnabled;
    private String theme;
    private ThemeManager themeManager;
    private Image background;
    private Image overlay;
    private Object tooltipContent;
    private Object themeTooltipContent;
    private InputMap inputMap;
    private ActionMap actionMap;
    private TintAnimator tintAnimator;
    private PropertyChangeSupport propertyChangeSupport;
    volatile GUI guiInstance;
    private OffscreenSurface offscreenSurface;
    private RenderOffscreen renderOffscreen;
    private final de.matthiasmann.twl.AnimationState animState;
    private final boolean sharedAnimState;
    private short borderLeft;
    private short borderTop;
    private short borderRight;
    private short borderBottom;
    private short minWidth;
    private short minHeight;
    private short maxWidth;
    private short maxHeight;
    private short offscreenExtraLeft;
    private short offscreenExtraTop;
    private short offscreenExtraRight;
    private short offscreenExtraBottom;
    private ArrayList<Widget> children;
    private Widget lastChildMouseOver;
    private Widget focusChild;
    private MouseCursor mouseCursor;
    private FocusGainedCause focusGainedCause;
    private boolean focusKeyEnabled;
    private boolean canAcceptKeyboardFocus;
    private boolean depthFocusTraversal;
    private static final ThreadLocal<Widget[]> focusTransferInfo;
    private static final boolean WARN_ON_UNHANDLED_ACTION;
    
    public Widget() {
        this(null, false);
    }
    
    public Widget(final de.matthiasmann.twl.AnimationState animState) {
        this(animState, false);
    }
    
    public Widget(final de.matthiasmann.twl.AnimationState animState, final boolean inherit) {
        this.visible = true;
        this.enabled = true;
        this.locallyEnabled = true;
        this.focusKeyEnabled = true;
        this.depthFocusTraversal = true;
        Class<?> clazz = this.getClass();
        do {
            this.theme = clazz.getSimpleName().toLowerCase(Locale.ENGLISH);
            clazz = clazz.getSuperclass();
        } while (this.theme.length() == 0 && clazz != null);
        if (animState == null || inherit) {
            this.animState = new de.matthiasmann.twl.AnimationState(animState);
            this.sharedAnimState = false;
        }
        else {
            this.animState = animState;
            this.sharedAnimState = true;
        }
    }
    
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        this.createPropertyChangeSupport().addPropertyChangeListener(listener);
    }
    
    public void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
        this.createPropertyChangeSupport().addPropertyChangeListener(propertyName, listener);
    }
    
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        if (this.propertyChangeSupport != null) {
            this.propertyChangeSupport.removePropertyChangeListener(listener);
        }
    }
    
    public void removePropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
        if (this.propertyChangeSupport != null) {
            this.propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
        }
    }
    
    public boolean hasOpenPopups() {
        return this.hasOpenPopup;
    }
    
    public final Widget getParent() {
        return this.parent;
    }
    
    public final Widget getRootWidget() {
        Widget w;
        Widget p;
        for (w = this; (p = w.parent) != null; w = p) {}
        return w;
    }
    
    public final GUI getGUI() {
        return this.guiInstance;
    }
    
    public final boolean isVisible() {
        return this.visible;
    }
    
    public void setVisible(final boolean visible) {
        if (this.visible != visible) {
            if (!(this.visible = visible)) {
                final GUI gui = this.getGUI();
                if (gui != null) {
                    gui.widgetHidden(this);
                }
                if (this.parent != null) {
                    this.parent.childHidden(this);
                }
            }
            if (this.parent != null) {
                this.parent.childVisibilityChanged(this);
            }
        }
    }
    
    public final boolean isLocallyEnabled() {
        return this.locallyEnabled;
    }
    
    public final boolean isEnabled() {
        return this.enabled;
    }
    
    public void setEnabled(final boolean enabled) {
        if (this.locallyEnabled != enabled) {
            this.locallyEnabled = enabled;
            this.firePropertyChange("locallyEnabled", !enabled, enabled);
            this.recursivelyEnabledChanged(this.getGUI(), this.parent == null || this.parent.enabled);
        }
    }
    
    public final int getX() {
        return this.posX;
    }
    
    public final int getY() {
        return this.posY;
    }
    
    public final int getWidth() {
        return this.width;
    }
    
    public final int getHeight() {
        return this.height;
    }
    
    public final int getRight() {
        return this.posX + this.width;
    }
    
    public final int getBottom() {
        return this.posY + this.height;
    }
    
    public final int getInnerX() {
        return this.posX + this.borderLeft;
    }
    
    public final int getInnerY() {
        return this.posY + this.borderTop;
    }
    
    public final int getInnerWidth() {
        return Math.max(0, this.width - this.borderLeft - this.borderRight);
    }
    
    public final int getInnerHeight() {
        return Math.max(0, this.height - this.borderTop - this.borderBottom);
    }
    
    public final int getInnerRight() {
        return this.posX + Math.max(this.borderLeft, this.width - this.borderRight);
    }
    
    public final int getInnerBottom() {
        return this.posY + Math.max(this.borderTop, this.height - this.borderBottom);
    }
    
    public boolean isInside(final int x, final int y) {
        return x >= this.posX && y >= this.posY && x < this.posX + this.width && y < this.posY + this.height;
    }
    
    public boolean setPosition(final int x, final int y) {
        return this.setPositionImpl(x, y);
    }
    
    public boolean setSize(final int width, final int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("negative size");
        }
        final int oldWidth = this.width;
        final int oldHeight = this.height;
        if (oldWidth != width || oldHeight != height) {
            this.width = width;
            this.height = height;
            this.sizeChanged();
            if (this.propertyChangeSupport != null) {
                this.firePropertyChange("width", oldWidth, width);
                this.firePropertyChange("height", oldHeight, height);
            }
            return true;
        }
        return false;
    }
    
    public boolean setInnerSize(final int width, final int height) {
        return this.setSize(width + this.borderLeft + this.borderRight, height + this.borderTop + this.borderBottom);
    }
    
    public short getBorderTop() {
        return this.borderTop;
    }
    
    public short getBorderLeft() {
        return this.borderLeft;
    }
    
    public short getBorderBottom() {
        return this.borderBottom;
    }
    
    public short getBorderRight() {
        return this.borderRight;
    }
    
    public int getBorderHorizontal() {
        return this.borderLeft + this.borderRight;
    }
    
    public int getBorderVertical() {
        return this.borderTop + this.borderBottom;
    }
    
    public boolean setBorderSize(final int top, final int left, final int bottom, final int right) {
        if (top < 0 || left < 0 || bottom < 0 || right < 0) {
            throw new IllegalArgumentException("negative border size");
        }
        if (this.borderTop != top || this.borderBottom != bottom || this.borderLeft != left || this.borderRight != right) {
            final int innerWidth = this.getInnerWidth();
            final int innerHeight = this.getInnerHeight();
            final int deltaLeft = left - this.borderLeft;
            final int deltaTop = top - this.borderTop;
            this.borderLeft = (short)left;
            this.borderTop = (short)top;
            this.borderRight = (short)right;
            this.borderBottom = (short)bottom;
            if (this.children != null && (deltaLeft != 0 || deltaTop != 0)) {
                for (int i = 0, n = this.children.size(); i < n; ++i) {
                    adjustChildPosition(this.children.get(i), deltaLeft, deltaTop);
                }
            }
            this.setInnerSize(innerWidth, innerHeight);
            this.borderChanged();
            return true;
        }
        return false;
    }
    
    public boolean setBorderSize(final int horizontal, final int vertical) {
        return this.setBorderSize(vertical, horizontal, vertical, horizontal);
    }
    
    public boolean setBorderSize(final int border) {
        return this.setBorderSize(border, border, border, border);
    }
    
    public boolean setBorderSize(final Border border) {
        if (border == null) {
            return this.setBorderSize(0, 0, 0, 0);
        }
        return this.setBorderSize(border.getBorderTop(), border.getBorderLeft(), border.getBorderBottom(), border.getBorderRight());
    }
    
    public short getOffscreenExtraTop() {
        return this.offscreenExtraTop;
    }
    
    public short getOffscreenExtraLeft() {
        return this.offscreenExtraLeft;
    }
    
    public short getOffscreenExtraBottom() {
        return this.offscreenExtraBottom;
    }
    
    public short getOffscreenExtraRight() {
        return this.offscreenExtraRight;
    }
    
    public void setOffscreenExtra(final int top, final int left, final int bottom, final int right) {
        if (top < 0 || left < 0 || bottom < 0 || right < 0) {
            throw new IllegalArgumentException("negative offscreen extra size");
        }
        this.offscreenExtraTop = (short)top;
        this.offscreenExtraLeft = (short)left;
        this.offscreenExtraBottom = (short)bottom;
        this.offscreenExtraRight = (short)right;
    }
    
    public void setOffscreenExtra(final Border offscreenExtra) {
        if (offscreenExtra == null) {
            this.setOffscreenExtra(0, 0, 0, 0);
        }
        else {
            this.setOffscreenExtra(offscreenExtra.getBorderTop(), offscreenExtra.getBorderLeft(), offscreenExtra.getBorderBottom(), offscreenExtra.getBorderRight());
        }
    }
    
    public int getMinWidth() {
        return Math.max(this.minWidth, this.borderLeft + this.borderRight);
    }
    
    public int getMinHeight() {
        return Math.max(this.minHeight, this.borderTop + this.borderBottom);
    }
    
    public void setMinSize(final int width, final int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("negative size");
        }
        this.minWidth = (short)Math.min(width, 32767);
        this.minHeight = (short)Math.min(height, 32767);
    }
    
    public int getPreferredInnerWidth() {
        int right = this.getInnerX();
        if (this.children != null) {
            for (int i = 0, n = this.children.size(); i < n; ++i) {
                final Widget child = this.children.get(i);
                right = Math.max(right, child.getRight());
            }
        }
        return right - this.getInnerX();
    }
    
    public int getPreferredWidth() {
        int prefWidth = this.borderLeft + this.borderRight + this.getPreferredInnerWidth();
        final Image bg = this.getBackground();
        if (bg != null) {
            prefWidth = Math.max(prefWidth, bg.getWidth());
        }
        return Math.max(this.minWidth, prefWidth);
    }
    
    public int getPreferredInnerHeight() {
        int bottom = this.getInnerY();
        if (this.children != null) {
            for (int i = 0, n = this.children.size(); i < n; ++i) {
                final Widget child = this.children.get(i);
                bottom = Math.max(bottom, child.getBottom());
            }
        }
        return bottom - this.getInnerY();
    }
    
    public int getPreferredHeight() {
        int prefHeight = this.borderTop + this.borderBottom + this.getPreferredInnerHeight();
        final Image bg = this.getBackground();
        if (bg != null) {
            prefHeight = Math.max(prefHeight, bg.getHeight());
        }
        return Math.max(this.minHeight, prefHeight);
    }
    
    public int getMaxWidth() {
        return this.maxWidth;
    }
    
    public int getMaxHeight() {
        return this.maxHeight;
    }
    
    public void setMaxSize(final int width, final int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("negative size");
        }
        this.maxWidth = (short)Math.min(width, 32767);
        this.maxHeight = (short)Math.min(height, 32767);
    }
    
    public static int computeSize(final int min, int preferred, final int max) {
        if (max > 0) {
            preferred = Math.min(preferred, max);
        }
        return Math.max(min, preferred);
    }
    
    public void adjustSize() {
        this.setSize(computeSize(this.getMinWidth(), this.getPreferredWidth(), this.getMaxWidth()), computeSize(this.getMinHeight(), this.getPreferredHeight(), this.getMaxHeight()));
        this.validateLayout();
    }
    
    public void invalidateLayout() {
        if (this.layoutInvalid < 3) {
            this.invalidateLayoutLocally();
            if (this.parent != null) {
                this.layoutInvalid = 3;
                this.parent.childInvalidateLayout(this);
            }
        }
    }
    
    public void validateLayout() {
        if (this.layoutInvalid != 0) {
            this.layoutInvalid = 0;
            this.layout();
        }
        if (this.children != null) {
            for (int i = 0, n = this.children.size(); i < n; ++i) {
                this.children.get(i).validateLayout();
            }
        }
    }
    
    public String getTheme() {
        return this.theme;
    }
    
    public void setTheme(final String theme) {
        if (theme == null) {
            throw new IllegalArgumentException("theme is null");
        }
        if (theme.length() > 0) {
            final int slashIdx = theme.lastIndexOf(47);
            if (slashIdx > 0) {
                throw new IllegalArgumentException("'/' is only allowed as first character in theme name");
            }
            if (slashIdx < 0) {
                if (theme.indexOf(46) >= 0) {
                    throw new IllegalArgumentException("'.' is only allowed for absolute theme paths");
                }
            }
            else if (theme.length() == 1) {
                throw new IllegalArgumentException("'/' requires a theme path");
            }
            for (int i = 0, n = theme.length(); i < n; ++i) {
                final char ch = theme.charAt(i);
                if (Character.isISOControl(ch) || ch == '*') {
                    throw new IllegalArgumentException("invalid character '" + TextUtil.toPrintableString(ch) + "' in theme name");
                }
            }
        }
        this.theme = theme;
    }
    
    public final String getThemePath() {
        return this.getThemePath(0).toString();
    }
    
    public boolean isClip() {
        return this.clip;
    }
    
    public void setClip(final boolean clip) {
        this.clip = clip;
    }
    
    public boolean isFocusKeyEnabled() {
        return this.focusKeyEnabled;
    }
    
    public void setFocusKeyEnabled(final boolean focusKeyEnabled) {
        this.focusKeyEnabled = focusKeyEnabled;
    }
    
    public Image getBackground() {
        return this.background;
    }
    
    public void setBackground(final Image background) {
        this.background = background;
    }
    
    public Image getOverlay() {
        return this.overlay;
    }
    
    public void setOverlay(final Image overlay) {
        this.overlay = overlay;
    }
    
    public MouseCursor getMouseCursor(final Event evt) {
        return this.getMouseCursor();
    }
    
    public MouseCursor getMouseCursor() {
        return this.mouseCursor;
    }
    
    public void setMouseCursor(final MouseCursor mouseCursor) {
        this.mouseCursor = mouseCursor;
    }
    
    public final int getNumChildren() {
        if (this.children != null) {
            return this.children.size();
        }
        return 0;
    }
    
    public final Widget getChild(final int index) throws IndexOutOfBoundsException {
        if (this.children != null) {
            return this.children.get(index);
        }
        throw new IndexOutOfBoundsException();
    }
    
    public void add(final Widget child) {
        this.insertChild(child, this.getNumChildren());
    }
    
    public void insertChild(final Widget child, final int index) throws IndexOutOfBoundsException {
        if (child == null) {
            throw new IllegalArgumentException("child is null");
        }
        if (child == this) {
            throw new IllegalArgumentException("can't add to self");
        }
        if (child.parent != null) {
            throw new IllegalArgumentException("child widget already in tree");
        }
        if (this.children == null) {
            this.children = new ArrayList<Widget>();
        }
        if (index < 0 || index > this.children.size()) {
            throw new IndexOutOfBoundsException();
        }
        child.setParent(this);
        this.children.add(index, child);
        final GUI gui = this.getGUI();
        if (gui != null) {
            child.recursivelySetGUI(gui);
        }
        adjustChildPosition(child, this.posX + this.borderLeft, this.posY + this.borderTop);
        child.recursivelyEnabledChanged(null, this.enabled);
        if (gui != null) {
            child.recursivelyAddToGUI(gui);
        }
        if (this.themeManager != null) {
            child.applyTheme(this.themeManager);
        }
        try {
            this.childAdded(child);
        }
        catch (Exception ex) {
            this.getLogger().log(Level.SEVERE, "Exception in childAdded()", ex);
        }
    }
    
    public final int getChildIndex(final Widget child) {
        if (this.children != null) {
            for (int i = 0, n = this.children.size(); i < n; ++i) {
                if (this.children.get(i) == child) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    public boolean removeChild(final Widget child) {
        final int idx = this.getChildIndex(child);
        if (idx >= 0) {
            this.removeChild(idx);
            return true;
        }
        return false;
    }
    
    public Widget removeChild(final int index) throws IndexOutOfBoundsException {
        if (this.children != null) {
            final Widget child = this.children.remove(index);
            this.unparentChild(child);
            if (this.lastChildMouseOver == child) {
                this.lastChildMouseOver = null;
            }
            if (this.focusChild == child) {
                this.focusChild = null;
            }
            this.childRemoved(child);
            return child;
        }
        throw new IndexOutOfBoundsException();
    }
    
    public void removeAllChildren() {
        if (this.children != null) {
            this.focusChild = null;
            this.lastChildMouseOver = null;
            for (int i = 0, n = this.children.size(); i < n; ++i) {
                final Widget child = this.children.get(i);
                this.unparentChild(child);
            }
            this.children.clear();
            if (this.hasOpenPopup) {
                final GUI gui = this.getGUI();
                assert gui != null;
                this.recalcOpenPopups(gui);
            }
            this.allChildrenRemoved();
        }
    }
    
    public void destroy() {
        if (this.children != null) {
            for (int i = 0, n = this.children.size(); i < n; ++i) {
                this.children.get(i).destroy();
            }
        }
        if (this.offscreenSurface != null) {
            this.offscreenSurface.destroy();
            this.offscreenSurface = null;
        }
    }
    
    public boolean canAcceptKeyboardFocus() {
        return this.canAcceptKeyboardFocus;
    }
    
    public void setCanAcceptKeyboardFocus(final boolean canAcceptKeyboardFocus) {
        this.canAcceptKeyboardFocus = canAcceptKeyboardFocus;
    }
    
    public boolean isDepthFocusTraversal() {
        return this.depthFocusTraversal;
    }
    
    public void setDepthFocusTraversal(final boolean depthFocusTraversal) {
        this.depthFocusTraversal = depthFocusTraversal;
    }
    
    public boolean requestKeyboardFocus() {
        if (this.parent != null && this.visible) {
            if (this.parent.focusChild == this) {
                return true;
            }
            final boolean clear = this.focusTransferStart();
            try {
                return this.parent.requestKeyboardFocus(this);
            }
            finally {
                this.focusTransferClear(clear);
            }
        }
        return false;
    }
    
    public void giveupKeyboardFocus() {
        if (this.parent != null && this.parent.focusChild == this) {
            this.parent.requestKeyboardFocus(null);
        }
    }
    
    public boolean hasKeyboardFocus() {
        return this.parent != null && this.parent.focusChild == this;
    }
    
    public boolean focusNextChild() {
        return this.moveFocus(true, 1);
    }
    
    public boolean focusPrevChild() {
        return this.moveFocus(true, -1);
    }
    
    public boolean focusFirstChild() {
        return this.moveFocus(false, 1);
    }
    
    public boolean focusLastChild() {
        return this.moveFocus(false, -1);
    }
    
    public de.matthiasmann.twl.AnimationState getAnimationState() {
        return this.animState;
    }
    
    public boolean hasSharedAnimationState() {
        return this.sharedAnimState;
    }
    
    public TintAnimator getTintAnimator() {
        return this.tintAnimator;
    }
    
    public void setTintAnimator(final TintAnimator tintAnimator) {
        this.tintAnimator = tintAnimator;
    }
    
    public RenderOffscreen getRenderOffscreen() {
        return this.renderOffscreen;
    }
    
    public void setRenderOffscreen(final RenderOffscreen renderOffscreen) {
        this.renderOffscreen = renderOffscreen;
    }
    
    public Object getTooltipContent() {
        return this.tooltipContent;
    }
    
    public void setTooltipContent(final Object tooltipContent) {
        this.tooltipContent = tooltipContent;
        this.updateTooltip();
    }
    
    public InputMap getInputMap() {
        return this.inputMap;
    }
    
    public void setInputMap(final InputMap inputMap) {
        this.inputMap = inputMap;
    }
    
    public ActionMap getActionMap() {
        return this.actionMap;
    }
    
    public ActionMap getOrCreateActionMap() {
        if (this.actionMap == null) {
            this.actionMap = new ActionMap();
        }
        return this.actionMap;
    }
    
    public void setActionMap(final ActionMap actionMap) {
        this.actionMap = actionMap;
    }
    
    public Widget getWidgetAt(final int x, final int y) {
        final Widget child = this.getChildAt(x, y);
        if (child != null) {
            return child.getWidgetAt(x, y);
        }
        return this;
    }
    
    protected void applyTheme(final ThemeInfo themeInfo) {
        this.applyThemeBackground(themeInfo);
        this.applyThemeOverlay(themeInfo);
        this.applyThemeBorder(themeInfo);
        this.applyThemeOffscreenExtra(themeInfo);
        this.applyThemeMinSize(themeInfo);
        this.applyThemeMaxSize(themeInfo);
        this.applyThemeMouseCursor(themeInfo);
        this.applyThemeInputMap(themeInfo);
        this.applyThemeTooltip(themeInfo);
        this.invalidateLayout();
    }
    
    protected void applyThemeBackground(final ThemeInfo themeInfo) {
        this.setBackground(themeInfo.getImage("background"));
    }
    
    protected void applyThemeOverlay(final ThemeInfo themeInfo) {
        this.setOverlay(themeInfo.getImage("overlay"));
    }
    
    protected void applyThemeBorder(final ThemeInfo themeInfo) {
        this.setBorderSize((Border)themeInfo.getParameterValue("border", false, (Class)Border.class));
    }
    
    protected void applyThemeOffscreenExtra(final ThemeInfo themeInfo) {
        this.setOffscreenExtra((Border)themeInfo.getParameterValue("offscreenExtra", false, (Class)Border.class));
    }
    
    protected void applyThemeMinSize(final ThemeInfo themeInfo) {
        this.setMinSize(themeInfo.getParameter("minWidth", 0), themeInfo.getParameter("minHeight", 0));
    }
    
    protected void applyThemeMaxSize(final ThemeInfo themeInfo) {
        this.setMaxSize(themeInfo.getParameter("maxWidth", 32767), themeInfo.getParameter("maxHeight", 32767));
    }
    
    protected void applyThemeMouseCursor(final ThemeInfo themeInfo) {
        this.setMouseCursor(themeInfo.getMouseCursor("mouseCursor"));
    }
    
    protected void applyThemeInputMap(final ThemeInfo themeInfo) {
        this.setInputMap((InputMap)themeInfo.getParameterValue("inputMap", false, (Class)InputMap.class));
    }
    
    protected void applyThemeTooltip(final ThemeInfo themeInfo) {
        this.themeTooltipContent = themeInfo.getParameterValue("tooltip", false);
        if (this.tooltipContent == null) {
            this.updateTooltip();
        }
    }
    
    protected Object getThemeTooltipContent() {
        return this.themeTooltipContent;
    }
    
    protected Object getTooltipContentAt(final int mouseX, final int mouseY) {
        Object content = this.getTooltipContent();
        if (content == null) {
            content = this.getThemeTooltipContent();
        }
        return content;
    }
    
    protected void updateTooltip() {
        final GUI gui = this.getGUI();
        if (gui != null) {
            gui.requestTooltipUpdate(this, false);
        }
    }
    
    protected void resetTooltip() {
        final GUI gui = this.getGUI();
        if (gui != null) {
            gui.requestTooltipUpdate(this, true);
        }
    }
    
    protected void addActionMapping(final String action, final String methodName, final Object... params) {
        this.getOrCreateActionMap().addMapping(action, (Object)this, methodName, params, 1);
    }
    
    public void reapplyTheme() {
        if (this.themeManager != null) {
            this.applyTheme(this.themeManager);
        }
    }
    
    protected boolean isMouseInside(final Event evt) {
        return this.isInside(evt.getMouseX(), evt.getMouseY());
    }
    
    protected boolean handleEvent(final Event evt) {
        return evt.isKeyEvent() && this.handleKeyEvent(evt);
    }
    
    protected boolean handleKeyStrokeAction(final String action, final Event event) {
        return this.actionMap != null && this.actionMap.invoke(action, event);
    }
    
    protected void moveChild(final int from, final int to) {
        if (this.children == null) {
            throw new IndexOutOfBoundsException();
        }
        if (to < 0 || to >= this.children.size()) {
            throw new IndexOutOfBoundsException("to");
        }
        if (from < 0 || from >= this.children.size()) {
            throw new IndexOutOfBoundsException("from");
        }
        final Widget child = this.children.remove(from);
        this.children.add(to, child);
    }
    
    protected boolean requestKeyboardFocus(final Widget child) {
        if (child != null && child.parent != this) {
            throw new IllegalArgumentException("not a direct child");
        }
        if (this.focusChild != child) {
            if (child == null) {
                this.recursivelyChildFocusLost(this.focusChild);
                this.keyboardFocusChildChanged(this.focusChild = null);
            }
            else {
                final boolean clear = this.focusTransferStart();
                try {
                    final FocusGainedCause savedCause = this.focusGainedCause;
                    if (savedCause == null) {
                        this.focusGainedCause = FocusGainedCause.CHILD_FOCUSED;
                    }
                    try {
                        if (!this.requestKeyboardFocus()) {
                            return false;
                        }
                    }
                    finally {
                        this.focusGainedCause = savedCause;
                    }
                    this.recursivelyChildFocusLost(this.focusChild);
                    this.keyboardFocusChildChanged(this.focusChild = child);
                    if (!child.sharedAnimState) {
                        child.animState.setAnimationState(Widget.STATE_KEYBOARD_FOCUS, true);
                    }
                    final FocusGainedCause cause = child.focusGainedCause;
                    final Widget[] fti = Widget.focusTransferInfo.get();
                    child.keyboardFocusGained((cause != null) ? cause : FocusGainedCause.MANUAL, (fti != null) ? fti[0] : null);
                }
                finally {
                    this.focusTransferClear(clear);
                }
            }
        }
        if (!this.sharedAnimState) {
            this.animState.setAnimationState(Widget.STATE_HAS_FOCUSED_CHILD, this.focusChild != null);
        }
        return this.focusChild != null;
    }
    
    protected void beforeRemoveFromGUI(final GUI gui) {
    }
    
    protected void afterAddToGUI(final GUI gui) {
    }
    
    protected void layout() {
    }
    
    protected void positionChanged() {
    }
    
    protected void sizeChanged() {
        this.invalidateLayoutLocally();
    }
    
    protected void borderChanged() {
        this.invalidateLayout();
    }
    
    protected void childInvalidateLayout(final Widget child) {
        this.invalidateLayout();
    }
    
    protected void childAdded(final Widget child) {
        this.invalidateLayout();
    }
    
    protected void childRemoved(final Widget exChild) {
        this.invalidateLayout();
    }
    
    protected void allChildrenRemoved() {
        this.invalidateLayout();
    }
    
    protected void childVisibilityChanged(final Widget child) {
    }
    
    protected void keyboardFocusChildChanged(final Widget child) {
    }
    
    protected void keyboardFocusLost() {
    }
    
    protected void keyboardFocusGained() {
    }
    
    protected void keyboardFocusGained(final FocusGainedCause cause, final Widget previousWidget) {
        this.keyboardFocusGained();
    }
    
    protected void widgetDisabled() {
    }
    
    protected void paint(final GUI gui) {
        this.paintBackground(gui);
        this.paintWidget(gui);
        this.paintChildren(gui);
        this.paintOverlay(gui);
    }
    
    protected void paintWidget(final GUI gui) {
    }
    
    protected void paintBackground(final GUI gui) {
        final Image bgImage = this.getBackground();
        if (bgImage != null) {
            bgImage.draw((AnimationState)this.getAnimationState(), this.posX, this.posY, this.width, this.height);
        }
    }
    
    protected void paintOverlay(final GUI gui) {
        final Image ovImage = this.getOverlay();
        if (ovImage != null) {
            ovImage.draw((AnimationState)this.getAnimationState(), this.posX, this.posY, this.width, this.height);
        }
    }
    
    protected void paintChildren(final GUI gui) {
        if (this.children != null) {
            for (int i = 0, n = this.children.size(); i < n; ++i) {
                final Widget child = this.children.get(i);
                if (child.visible) {
                    child.drawWidget(gui);
                }
            }
        }
    }
    
    protected void paintChild(final GUI gui, final Widget child) {
        if (child.parent != this) {
            throw new IllegalArgumentException("can only render direct children");
        }
        child.drawWidget(gui);
    }
    
    protected void paintDragOverlay(final GUI gui, final int mouseX, final int mouseY, final int modifier) {
    }
    
    protected final void invalidateLayoutLocally() {
        if (this.layoutInvalid < 1) {
            this.layoutInvalid = 1;
            final GUI gui = this.getGUI();
            if (gui != null) {
                gui.hasInvalidLayouts = true;
            }
        }
    }
    
    protected void layoutChildFullInnerArea(final Widget child) {
        if (child.parent != this) {
            throw new IllegalArgumentException("can only layout direct children");
        }
        child.setPosition(this.getInnerX(), this.getInnerY());
        child.setSize(this.getInnerWidth(), this.getInnerHeight());
    }
    
    protected void layoutChildrenFullInnerArea() {
        if (this.children != null) {
            for (int i = 0, n = this.children.size(); i < n; ++i) {
                this.layoutChildFullInnerArea(this.children.get(i));
            }
        }
    }
    
    protected List<Widget> getKeyboardFocusOrder() {
        if (this.children == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList((List<? extends Widget>)this.children);
    }
    
    private int collectFocusOrderList(final ArrayList<Widget> list) {
        int idx = -1;
        for (final Widget child : this.getKeyboardFocusOrder()) {
            if (child.visible && child.isEnabled()) {
                if (child.canAcceptKeyboardFocus) {
                    if (child == this.focusChild) {
                        idx = list.size();
                    }
                    list.add(child);
                }
                if (!child.depthFocusTraversal) {
                    continue;
                }
                final int subIdx = child.collectFocusOrderList(list);
                if (subIdx == -1) {
                    continue;
                }
                idx = subIdx;
            }
        }
        return idx;
    }
    
    private boolean moveFocus(final boolean relative, final int dir) {
        final ArrayList<Widget> focusList = new ArrayList<Widget>();
        int curIndex = this.collectFocusOrderList(focusList);
        if (focusList.isEmpty()) {
            return false;
        }
        if (dir < 0) {
            if (!relative || --curIndex < 0) {
                curIndex = focusList.size() - 1;
            }
        }
        else if (!relative || ++curIndex >= focusList.size()) {
            curIndex = 0;
        }
        final Widget widget = focusList.get(curIndex);
        try {
            widget.focusGainedCause = FocusGainedCause.FOCUS_KEY;
            widget.requestKeyboardFocus(null);
            widget.requestKeyboardFocus();
        }
        finally {
            widget.focusGainedCause = null;
        }
        return true;
    }
    
    private boolean focusTransferStart() {
        final Widget[] fti = Widget.focusTransferInfo.get();
        if (fti == null) {
            Widget w;
            Widget root;
            for (root = (w = this.getRootWidget()); w.focusChild != null; w = w.focusChild) {}
            if (w == root) {
                w = null;
            }
            Widget.focusTransferInfo.set(new Widget[] { w });
            return true;
        }
        return false;
    }
    
    private void focusTransferClear(final boolean clear) {
        if (clear) {
            Widget.focusTransferInfo.set(null);
        }
    }
    
    protected final Widget getChildAt(final int x, final int y) {
        if (this.children != null) {
            int i = this.children.size();
            while (i-- > 0) {
                final Widget child = this.children.get(i);
                if (child.visible && child.isInside(x, y)) {
                    return child;
                }
            }
        }
        return null;
    }
    
    protected void updateTintAnimation() {
        this.tintAnimator.update();
    }
    
    protected final void firePropertyChange(final PropertyChangeEvent evt) {
        if (this.propertyChangeSupport != null) {
            this.propertyChangeSupport.firePropertyChange(evt);
        }
    }
    
    protected final void firePropertyChange(final String propertyName, final boolean oldValue, final boolean newValue) {
        if (this.propertyChangeSupport != null) {
            this.propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
        }
    }
    
    protected final void firePropertyChange(final String propertyName, final int oldValue, final int newValue) {
        if (this.propertyChangeSupport != null) {
            this.propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
        }
    }
    
    protected final void firePropertyChange(final String propertyName, final Object oldValue, final Object newValue) {
        if (this.propertyChangeSupport != null) {
            this.propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue);
        }
    }
    
    void setParent(final Widget parent) {
        this.parent = parent;
    }
    
    private void unparentChild(final Widget child) {
        final GUI gui = this.getGUI();
        if (child.hasOpenPopup) {
            assert gui != null;
            gui.closePopupFromWidgets(child);
        }
        this.recursivelyChildFocusLost(child);
        if (gui != null) {
            child.recursivelyRemoveFromGUI(gui);
        }
        child.recursivelyClearGUI(gui);
        child.parent = null;
        try {
            child.destroy();
        }
        catch (Exception ex) {
            this.getLogger().log(Level.SEVERE, "Exception in destroy()", ex);
        }
        adjustChildPosition(child, -this.posX, -this.posY);
        child.recursivelyEnabledChanged(null, child.locallyEnabled);
    }
    
    private void recursivelySetGUI(final GUI gui) {
        assert this.guiInstance == null : "guiInstance must be null";
        this.guiInstance = gui;
        if (this.children != null) {
            int i = this.children.size();
            while (i-- > 0) {
                this.children.get(i).recursivelySetGUI(gui);
            }
        }
    }
    
    private void recursivelyAddToGUI(final GUI gui) {
        assert this.guiInstance == gui : "guiInstance must be equal to gui";
        if (this.layoutInvalid != 0) {
            gui.hasInvalidLayouts = true;
        }
        if (!this.sharedAnimState) {
            this.animState.setGUI(gui);
        }
        try {
            this.afterAddToGUI(gui);
        }
        catch (Exception ex) {
            this.getLogger().log(Level.SEVERE, "Exception in afterAddToGUI()", ex);
        }
        if (this.children != null) {
            int i = this.children.size();
            while (i-- > 0) {
                this.children.get(i).recursivelyAddToGUI(gui);
            }
        }
    }
    
    private void recursivelyClearGUI(final GUI gui) {
        assert this.guiInstance == gui : "guiInstance must be null";
        this.guiInstance = null;
        this.themeManager = null;
        if (this.children != null) {
            int i = this.children.size();
            while (i-- > 0) {
                this.children.get(i).recursivelyClearGUI(gui);
            }
        }
    }
    
    private void recursivelyRemoveFromGUI(final GUI gui) {
        assert this.guiInstance == gui : "guiInstance must be equal to gui";
        if (this.children != null) {
            int i = this.children.size();
            while (i-- > 0) {
                this.children.get(i).recursivelyRemoveFromGUI(gui);
            }
        }
        this.focusChild = null;
        if (!this.sharedAnimState) {
            this.animState.setGUI((GUI)null);
        }
        try {
            this.beforeRemoveFromGUI(gui);
        }
        catch (Exception ex) {
            this.getLogger().log(Level.SEVERE, "Exception in beforeRemoveFromGUI()", ex);
        }
    }
    
    private void recursivelyChildFocusLost(Widget w) {
        while (w != null) {
            final Widget next = w.focusChild;
            if (!w.sharedAnimState) {
                w.animState.setAnimationState(Widget.STATE_KEYBOARD_FOCUS, false);
            }
            try {
                w.keyboardFocusLost();
            }
            catch (Exception ex) {
                this.getLogger().log(Level.SEVERE, "Exception in keyboardFocusLost()", ex);
            }
            w.focusChild = null;
            w = next;
        }
    }
    
    private void recursivelyEnabledChanged(final GUI gui, boolean enabled) {
        enabled &= this.locallyEnabled;
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (!this.sharedAnimState) {
                this.getAnimationState().setAnimationState(Widget.STATE_DISABLED, !enabled);
            }
            if (!enabled) {
                if (gui != null) {
                    gui.widgetDisabled(this);
                }
                try {
                    this.widgetDisabled();
                }
                catch (Exception ex) {
                    this.getLogger().log(Level.SEVERE, "Exception in widgetDisabled()", ex);
                }
                try {
                    this.giveupKeyboardFocus();
                }
                catch (Exception ex) {
                    this.getLogger().log(Level.SEVERE, "Exception in giveupKeyboardFocus()", ex);
                }
            }
            try {
                this.firePropertyChange("enabled", !enabled, enabled);
            }
            catch (Exception ex) {
                this.getLogger().log(Level.SEVERE, "Exception in firePropertyChange(\"enabled\")", ex);
            }
            if (this.children != null) {
                int i = this.children.size();
                while (i-- > 0) {
                    final Widget child = this.children.get(i);
                    child.recursivelyEnabledChanged(gui, enabled);
                }
            }
        }
    }
    
    private void childHidden(final Widget child) {
        if (this.focusChild == child) {
            this.recursivelyChildFocusLost(this.focusChild);
            this.focusChild = null;
        }
        if (this.lastChildMouseOver == child) {
            this.lastChildMouseOver = null;
        }
    }
    
    final void setOpenPopup(final GUI gui, final boolean hasOpenPopup) {
        if (this.hasOpenPopup != hasOpenPopup) {
            this.hasOpenPopup = hasOpenPopup;
            if (!this.sharedAnimState) {
                this.getAnimationState().setAnimationState(Widget.STATE_HAS_OPEN_POPUPS, hasOpenPopup);
            }
            if (this.parent != null) {
                if (hasOpenPopup) {
                    this.parent.setOpenPopup(gui, true);
                }
                else {
                    this.parent.recalcOpenPopups(gui);
                }
            }
        }
    }
    
    final void recalcOpenPopups(final GUI gui) {
        if (gui.hasOpenPopups(this)) {
            this.setOpenPopup(gui, true);
            return;
        }
        if (this.children != null) {
            int i = this.children.size();
            while (i-- > 0) {
                if (this.children.get(i).hasOpenPopup) {
                    this.setOpenPopup(gui, true);
                    return;
                }
            }
        }
        this.setOpenPopup(gui, false);
    }
    
    final boolean isLayoutInvalid() {
        return this.layoutInvalid != 0;
    }
    
    final void drawWidget(final GUI gui) {
        if (this.renderOffscreen != null) {
            this.drawWidgetOffscreen(gui);
            return;
        }
        if (this.tintAnimator != null && this.tintAnimator.hasTint()) {
            this.drawWidgetTint(gui);
            return;
        }
        if (this.clip) {
            this.drawWidgetClip(gui);
            return;
        }
        this.paint(gui);
    }
    
    private void drawWidgetTint(final GUI gui) {
        if (this.tintAnimator.isFadeActive()) {
            this.updateTintAnimation();
        }
        final Renderer renderer = gui.getRenderer();
        this.tintAnimator.paintWithTint(renderer);
        try {
            if (this.clip) {
                this.drawWidgetClip(gui);
            }
            else {
                this.paint(gui);
            }
        }
        finally {
            renderer.popGlobalTintColor();
        }
    }
    
    private void drawWidgetClip(final GUI gui) {
        final Renderer renderer = gui.getRenderer();
        renderer.clipEnter(this.posX, this.posY, this.width, this.height);
        try {
            this.paint(gui);
        }
        finally {
            renderer.clipLeave();
        }
    }
    
    private void drawWidgetOffscreen(final GUI gui) {
        final RenderOffscreen ro = this.renderOffscreen;
        final Renderer renderer = gui.getRenderer();
        final OffscreenRenderer offscreenRenderer = renderer.getOffscreenRenderer();
        if (offscreenRenderer != null) {
            int extraTop = this.offscreenExtraTop;
            int extraLeft = this.offscreenExtraLeft;
            int extraRight = this.offscreenExtraRight;
            int extraBottom = this.offscreenExtraBottom;
            final int[] effectExtra = ro.getEffectExtraArea(this);
            if (effectExtra != null) {
                extraTop += effectExtra[0];
                extraLeft += effectExtra[1];
                extraRight += effectExtra[2];
                extraBottom += effectExtra[3];
            }
            if (this.offscreenSurface != null && !ro.needPainting(gui, this.parent, this.offscreenSurface)) {
                ro.paintOffscreenSurface(gui, this, this.offscreenSurface);
                return;
            }
            this.offscreenSurface = offscreenRenderer.startOffscreenRendering(this, this.offscreenSurface, this.posX - extraLeft, this.posY - extraTop, this.width + extraLeft + extraRight, this.height + extraTop + extraBottom);
            if (this.offscreenSurface != null) {
                try {
                    if (this.tintAnimator != null && this.tintAnimator.hasTint()) {
                        this.drawWidgetTint(gui);
                    }
                    else {
                        this.paint(gui);
                    }
                }
                finally {
                    offscreenRenderer.endOffscreenRendering();
                }
                ro.paintOffscreenSurface(gui, this, this.offscreenSurface);
                return;
            }
        }
        this.renderOffscreen = null;
        ro.offscreenRenderingFailed(this);
        this.drawWidget(gui);
    }
    
    Widget getWidgetUnderMouse() {
        if (!this.visible) {
            return null;
        }
        Widget w;
        for (w = this; w.lastChildMouseOver != null && w.visible; w = w.lastChildMouseOver) {}
        return w;
    }
    
    private static void adjustChildPosition(final Widget child, final int deltaX, final int deltaY) {
        child.setPositionImpl(child.posX + deltaX, child.posY + deltaY);
    }
    
    final boolean setPositionImpl(final int x, final int y) {
        final int deltaX = x - this.posX;
        final int deltaY = y - this.posY;
        if (deltaX != 0 || deltaY != 0) {
            this.posX = x;
            this.posY = y;
            if (this.children != null) {
                for (int i = 0, n = this.children.size(); i < n; ++i) {
                    adjustChildPosition(this.children.get(i), deltaX, deltaY);
                }
            }
            this.positionChanged();
            if (this.propertyChangeSupport != null) {
                this.firePropertyChange("x", x - deltaX, x);
                this.firePropertyChange("y", y - deltaY, y);
            }
            return true;
        }
        return false;
    }
    
    void applyTheme(final ThemeManager themeManager) {
        this.themeManager = themeManager;
        final String themePath = this.getThemePath();
        if (themePath.length() == 0) {
            if (this.children != null) {
                for (int i = 0, n = this.children.size(); i < n; ++i) {
                    this.children.get(i).applyTheme(themeManager);
                }
            }
            return;
        }
        final DebugHook hook = DebugHook.getDebugHook();
        hook.beforeApplyTheme(this);
        ThemeInfo themeInfo = null;
        try {
            themeInfo = themeManager.findThemeInfo(themePath);
            if (themeInfo != null && this.theme.length() > 0) {
                try {
                    this.applyTheme(themeInfo);
                }
                catch (Exception ex) {
                    this.getLogger().log(Level.SEVERE, "Exception in applyTheme()", ex);
                }
            }
        }
        finally {
            hook.afterApplyTheme(this);
        }
        this.applyThemeToChildren(themeManager, themeInfo, hook);
    }
    
    public static boolean isAbsoluteTheme(final String theme) {
        return theme.length() > 1 && theme.charAt(0) == '/';
    }
    
    private void applyThemeImpl(final ThemeManager themeManager, ThemeInfo themeInfo, final DebugHook hook) {
        this.themeManager = themeManager;
        if (this.theme.length() > 0) {
            hook.beforeApplyTheme(this);
            try {
                if (isAbsoluteTheme(this.theme)) {
                    themeInfo = themeManager.findThemeInfo(this.theme.substring(1));
                }
                else {
                    themeInfo = themeInfo.getChildTheme(this.theme);
                }
                if (themeInfo != null) {
                    try {
                        this.applyTheme(themeInfo);
                    }
                    catch (Exception ex) {
                        this.getLogger().log(Level.SEVERE, "Exception in applyTheme()", ex);
                    }
                }
            }
            finally {
                hook.afterApplyTheme(this);
            }
        }
        this.applyThemeToChildren(themeManager, themeInfo, hook);
    }
    
    private void applyThemeToChildren(final ThemeManager themeManager, final ThemeInfo themeInfo, final DebugHook hook) {
        if (this.children != null && themeInfo != null) {
            for (int i = 0, n = this.children.size(); i < n; ++i) {
                final Widget child = this.children.get(i);
                child.applyThemeImpl(themeManager, themeInfo, hook);
            }
        }
    }
    
    private StringBuilder getThemePath(int length) {
        length += this.theme.length();
        final boolean abs = isAbsoluteTheme(this.theme);
        StringBuilder sb;
        if (this.parent != null && !abs) {
            sb = this.parent.getThemePath(length + 1);
            if (this.theme.length() > 0 && sb.length() > 0) {
                sb.append('.');
            }
        }
        else {
            sb = new StringBuilder(length);
        }
        if (abs) {
            return sb.append(this.theme.substring(1));
        }
        return sb.append(this.theme);
    }
    
    Event translateMouseEvent(Event evt) {
        if (this.renderOffscreen instanceof OffscreenMouseAdjustments) {
            final int[] newXY = ((OffscreenMouseAdjustments)this.renderOffscreen).adjustMouseCoordinates(this, evt);
            evt = evt.createSubEvent(newXY[0], newXY[1]);
        }
        return evt;
    }
    
    Widget routeMouseEvent(Event evt) {
        assert !evt.isMouseDragEvent();
        evt = this.translateMouseEvent(evt);
        if (this.children != null) {
            int i = this.children.size();
            while (i-- > 0) {
                final Widget child = this.children.get(i);
                if (child.visible && child.isMouseInside(evt) && this.setMouseOverChild(child, evt)) {
                    if (evt.getType() == Event.Type.MOUSE_ENTERED || evt.getType() == Event.Type.MOUSE_EXITED) {
                        return child;
                    }
                    final Widget result = child.routeMouseEvent(evt);
                    if (result != null) {
                        if (evt.getType() == Event.Type.MOUSE_BTNDOWN && this.focusChild != child) {
                            try {
                                child.focusGainedCause = FocusGainedCause.MOUSE_BTNDOWN;
                                if (child.isEnabled() && child.canAcceptKeyboardFocus()) {
                                    this.requestKeyboardFocus(child);
                                }
                            }
                            finally {
                                child.focusGainedCause = null;
                            }
                        }
                        return result;
                    }
                    continue;
                }
            }
        }
        if (evt.getType() == Event.Type.MOUSE_BTNDOWN && this.isEnabled() && this.canAcceptKeyboardFocus()) {
            try {
                this.focusGainedCause = FocusGainedCause.MOUSE_BTNDOWN;
                if (this.focusChild == null) {
                    this.requestKeyboardFocus();
                }
                else {
                    this.requestKeyboardFocus(null);
                }
            }
            finally {
                this.focusGainedCause = null;
            }
        }
        if (evt.getType() != Event.Type.MOUSE_WHEEL) {
            this.setMouseOverChild(null, evt);
        }
        if (!this.isEnabled() && isMouseAction(evt)) {
            return this;
        }
        if (this.handleEvent(evt)) {
            return this;
        }
        return null;
    }
    
    static boolean isMouseAction(final Event evt) {
        final Event.Type type = evt.getType();
        return type == Event.Type.MOUSE_BTNDOWN || type == Event.Type.MOUSE_BTNUP || type == Event.Type.MOUSE_CLICKED || type == Event.Type.MOUSE_DRAGGED;
    }
    
    void routePopupEvent(final Event evt) {
        this.handleEvent(evt);
        if (this.children != null) {
            for (int i = 0, n = this.children.size(); i < n; ++i) {
                this.children.get(i).routePopupEvent(evt);
            }
        }
    }
    
    static boolean getSafeBooleanProperty(final String name) {
        try {
            return Boolean.getBoolean(name);
        }
        catch (AccessControlException ex) {
            return false;
        }
    }
    
    private boolean handleKeyEvent(final Event evt) {
        if (this.children != null) {
            if (this.focusKeyEnabled && this.guiInstance != null) {
                this.guiInstance.setFocusKeyWidget(this);
            }
            if (this.focusChild != null && this.focusChild.isVisible() && this.focusChild.handleEvent(evt)) {
                return true;
            }
        }
        if (this.inputMap != null) {
            final String action = this.inputMap.mapEvent(evt);
            if (action != null) {
                if (this.handleKeyStrokeAction(action, evt)) {
                    return true;
                }
                if (Widget.WARN_ON_UNHANDLED_ACTION) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Unhandled action ''{0}'' for class ''{1}''", new Object[] { action, this.getClass().getName() });
                }
            }
        }
        return false;
    }
    
    void handleFocusKeyEvent(final Event evt) {
        if (evt.isKeyPressedEvent()) {
            if ((evt.getModifiers() & 0x9) != 0x0) {
                this.focusPrevChild();
            }
            else {
                this.focusNextChild();
            }
        }
    }
    
    boolean setMouseOverChild(final Widget child, final Event evt) {
        if (this.lastChildMouseOver != child) {
            if (child != null) {
                final Widget result = child.routeMouseEvent(evt.createSubEvent(Event.Type.MOUSE_ENTERED));
                if (result == null) {
                    return false;
                }
            }
            if (this.lastChildMouseOver != null) {
                this.lastChildMouseOver.routeMouseEvent(evt.createSubEvent(Event.Type.MOUSE_EXITED));
            }
            this.lastChildMouseOver = child;
        }
        return true;
    }
    
    void collectLayoutLoop(final ArrayList<Widget> result) {
        if (this.layoutInvalid != 0) {
            result.add(this);
        }
        if (this.children != null) {
            for (int i = 0, n = this.children.size(); i < n; ++i) {
                this.children.get(i).collectLayoutLoop(result);
            }
        }
    }
    
    private PropertyChangeSupport createPropertyChangeSupport() {
        if (this.propertyChangeSupport == null) {
            this.propertyChangeSupport = new PropertyChangeSupport(this);
        }
        return this.propertyChangeSupport;
    }
    
    private Logger getLogger() {
        return Logger.getLogger(Widget.class.getName());
    }
    
    static {
        STATE_KEYBOARD_FOCUS = AnimationState.StateKey.get("keyboardFocus");
        STATE_HAS_OPEN_POPUPS = AnimationState.StateKey.get("hasOpenPopups");
        STATE_HAS_FOCUSED_CHILD = AnimationState.StateKey.get("hasFocusedChild");
        STATE_DISABLED = AnimationState.StateKey.get("disabled");
        focusTransferInfo = new ThreadLocal<Widget[]>();
        WARN_ON_UNHANDLED_ACTION = getSafeBooleanProperty("warnOnUnhandledAction");
    }
    
    public interface OffscreenMouseAdjustments extends RenderOffscreen
    {
        int[] adjustMouseCoordinates(final Widget p0, final Event p1);
    }
    
    public interface RenderOffscreen
    {
        void paintOffscreenSurface(final GUI p0, final Widget p1, final OffscreenSurface p2);
        
        void offscreenRenderingFailed(final Widget p0);
        
        int[] getEffectExtraArea(final Widget p0);
        
        boolean needPainting(final GUI p0, final Widget p1, final OffscreenSurface p2);
    }
}
