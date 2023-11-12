package de.matthiasmann.twl;

import java.util.logging.*;
import de.matthiasmann.twl.renderer.*;
import java.util.*;

public class DialogLayout extends Widget
{
    public static final int SMALL_GAP = -1;
    public static final int MEDIUM_GAP = -2;
    public static final int LARGE_GAP = -3;
    public static final int DEFAULT_GAP = -4;
    private static final boolean DEBUG_LAYOUT_GROUPS;
    protected Dimension smallGap;
    protected Dimension mediumGap;
    protected Dimension largeGap;
    protected Dimension defaultGap;
    protected ParameterMap namedGaps;
    protected boolean addDefaultGaps;
    protected boolean includeInvisibleWidgets;
    protected boolean redoDefaultGaps;
    protected boolean isPrepared;
    protected boolean blockInvalidateLayoutTree;
    protected boolean warnOnIncomplete;
    private Group horz;
    private Group vert;
    Throwable debugStackTrace;
    final HashMap<Widget, WidgetSpring> widgetSprings;
    static final int AXIS_X = 0;
    static final int AXIS_Y = 1;
    static final Gap NO_GAP;
    
    public DialogLayout() {
        this.addDefaultGaps = true;
        this.includeInvisibleWidgets = true;
        this.widgetSprings = new HashMap<Widget, WidgetSpring>();
        this.collectDebugStack();
    }
    
    public Group getHorizontalGroup() {
        return this.horz;
    }
    
    public void setHorizontalGroup(final Group g) {
        if (g != null) {
            g.checkGroup(this);
        }
        this.horz = g;
        this.collectDebugStack();
        this.layoutGroupsChanged();
    }
    
    public Group getVerticalGroup() {
        return this.vert;
    }
    
    public void setVerticalGroup(final Group g) {
        if (g != null) {
            g.checkGroup(this);
        }
        this.vert = g;
        this.collectDebugStack();
        this.layoutGroupsChanged();
    }
    
    public Dimension getSmallGap() {
        return this.smallGap;
    }
    
    public void setSmallGap(final Dimension smallGap) {
        this.smallGap = smallGap;
        this.maybeInvalidateLayoutTree();
    }
    
    public Dimension getMediumGap() {
        return this.mediumGap;
    }
    
    public void setMediumGap(final Dimension mediumGap) {
        this.mediumGap = mediumGap;
        this.maybeInvalidateLayoutTree();
    }
    
    public Dimension getLargeGap() {
        return this.largeGap;
    }
    
    public void setLargeGap(final Dimension largeGap) {
        this.largeGap = largeGap;
        this.maybeInvalidateLayoutTree();
    }
    
    public Dimension getDefaultGap() {
        return this.defaultGap;
    }
    
    public void setDefaultGap(final Dimension defaultGap) {
        this.defaultGap = defaultGap;
        this.maybeInvalidateLayoutTree();
    }
    
    public boolean isAddDefaultGaps() {
        return this.addDefaultGaps;
    }
    
    public void setAddDefaultGaps(final boolean addDefaultGaps) {
        this.addDefaultGaps = addDefaultGaps;
    }
    
    public void removeDefaultGaps() {
        if (this.horz != null && this.vert != null) {
            this.horz.removeDefaultGaps();
            this.vert.removeDefaultGaps();
            this.maybeInvalidateLayoutTree();
        }
    }
    
    public void addDefaultGaps() {
        if (this.horz != null && this.vert != null) {
            this.horz.addDefaultGap();
            this.vert.addDefaultGap();
            this.maybeInvalidateLayoutTree();
        }
    }
    
    public boolean isIncludeInvisibleWidgets() {
        return this.includeInvisibleWidgets;
    }
    
    public void setIncludeInvisibleWidgets(final boolean includeInvisibleWidgets) {
        if (this.includeInvisibleWidgets != includeInvisibleWidgets) {
            this.includeInvisibleWidgets = includeInvisibleWidgets;
            this.layoutGroupsChanged();
        }
    }
    
    private void collectDebugStack() {
        this.warnOnIncomplete = true;
        if (DialogLayout.DEBUG_LAYOUT_GROUPS) {
            this.debugStackTrace = new Throwable("DialogLayout created/used here").fillInStackTrace();
        }
    }
    
    private void warnOnIncomplete() {
        this.warnOnIncomplete = false;
        getLogger().log(Level.WARNING, "Dialog layout has incomplete state", this.debugStackTrace);
    }
    
    static Logger getLogger() {
        return Logger.getLogger(DialogLayout.class.getName());
    }
    
    protected void applyThemeDialogLayout(final ThemeInfo themeInfo) {
        try {
            this.blockInvalidateLayoutTree = true;
            this.setSmallGap(themeInfo.getParameterValue("smallGap", true, Dimension.class, Dimension.ZERO));
            this.setMediumGap(themeInfo.getParameterValue("mediumGap", true, Dimension.class, Dimension.ZERO));
            this.setLargeGap(themeInfo.getParameterValue("largeGap", true, Dimension.class, Dimension.ZERO));
            this.setDefaultGap(themeInfo.getParameterValue("defaultGap", true, Dimension.class, Dimension.ZERO));
            this.namedGaps = themeInfo.getParameterMap("namedGaps");
        }
        finally {
            this.blockInvalidateLayoutTree = false;
        }
        this.invalidateLayout();
    }
    
    @Override
    protected void applyTheme(final ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        this.applyThemeDialogLayout(themeInfo);
    }
    
    @Override
    public int getMinWidth() {
        if (this.horz != null) {
            this.prepare();
            return this.horz.getMinSize(0) + this.getBorderHorizontal();
        }
        return super.getMinWidth();
    }
    
    @Override
    public int getMinHeight() {
        if (this.vert != null) {
            this.prepare();
            return this.vert.getMinSize(1) + this.getBorderVertical();
        }
        return super.getMinHeight();
    }
    
    @Override
    public int getPreferredInnerWidth() {
        if (this.horz != null) {
            this.prepare();
            return this.horz.getPrefSize(0);
        }
        return super.getPreferredInnerWidth();
    }
    
    @Override
    public int getPreferredInnerHeight() {
        if (this.vert != null) {
            this.prepare();
            return this.vert.getPrefSize(1);
        }
        return super.getPreferredInnerHeight();
    }
    
    @Override
    public void adjustSize() {
        if (this.horz != null && this.vert != null) {
            this.prepare();
            final int minWidth = this.horz.getMinSize(0);
            final int minHeight = this.vert.getMinSize(1);
            final int prefWidth = this.horz.getPrefSize(0);
            final int prefHeight = this.vert.getPrefSize(1);
            final int maxWidth = this.getMaxWidth();
            final int maxHeight = this.getMaxHeight();
            this.setInnerSize(Widget.computeSize(minWidth, prefWidth, maxWidth), Widget.computeSize(minHeight, prefHeight, maxHeight));
            this.doLayout();
        }
    }
    
    @Override
    protected void layout() {
        if (this.horz != null && this.vert != null) {
            this.prepare();
            this.doLayout();
        }
        else if (this.warnOnIncomplete) {
            this.warnOnIncomplete();
        }
    }
    
    protected void prepare() {
        if (this.redoDefaultGaps) {
            if (this.addDefaultGaps) {
                try {
                    this.blockInvalidateLayoutTree = true;
                    this.removeDefaultGaps();
                    this.addDefaultGaps();
                }
                finally {
                    this.blockInvalidateLayoutTree = false;
                }
            }
            this.redoDefaultGaps = false;
            this.isPrepared = false;
        }
        if (!this.isPrepared) {
            for (final WidgetSpring s : this.widgetSprings.values()) {
                if (this.includeInvisibleWidgets || s.w.isVisible()) {
                    s.prepare();
                }
            }
            this.isPrepared = true;
        }
    }
    
    protected void doLayout() {
        this.horz.setSize(0, this.getInnerX(), this.getInnerWidth());
        this.vert.setSize(1, this.getInnerY(), this.getInnerHeight());
        try {
            for (final WidgetSpring s : this.widgetSprings.values()) {
                if (this.includeInvisibleWidgets || s.w.isVisible()) {
                    s.apply();
                }
            }
        }
        catch (IllegalStateException ex) {
            if (this.debugStackTrace != null && ex.getCause() == null) {
                ex.initCause(this.debugStackTrace);
            }
            throw ex;
        }
    }
    
    @Override
    public void invalidateLayout() {
        this.isPrepared = false;
        super.invalidateLayout();
    }
    
    @Override
    protected void paintWidget(final GUI gui) {
        this.isPrepared = false;
    }
    
    @Override
    protected void sizeChanged() {
        this.isPrepared = false;
        super.sizeChanged();
    }
    
    @Override
    protected void afterAddToGUI(final GUI gui) {
        this.isPrepared = false;
        super.afterAddToGUI(gui);
    }
    
    public Group createParallelGroup() {
        return new ParallelGroup();
    }
    
    public Group createParallelGroup(final Widget... widgets) {
        return this.createParallelGroup().addWidgets(widgets);
    }
    
    public Group createParallelGroup(final Group... groups) {
        return this.createParallelGroup().addGroups(groups);
    }
    
    public Group createSequentialGroup() {
        return new SequentialGroup();
    }
    
    public Group createSequentialGroup(final Widget... widgets) {
        return this.createSequentialGroup().addWidgets(widgets);
    }
    
    public Group createSequentialGroup(final Group... groups) {
        return this.createSequentialGroup().addGroups(groups);
    }
    
    @Override
    public void insertChild(final Widget child, final int index) throws IndexOutOfBoundsException {
        super.insertChild(child, index);
        this.widgetSprings.put(child, new WidgetSpring(child));
    }
    
    @Override
    public void removeAllChildren() {
        super.removeAllChildren();
        this.widgetSprings.clear();
        this.recheckWidgets();
        this.layoutGroupsChanged();
    }
    
    @Override
    public Widget removeChild(final int index) throws IndexOutOfBoundsException {
        final Widget widget = super.removeChild(index);
        this.widgetSprings.remove(widget);
        this.recheckWidgets();
        this.layoutGroupsChanged();
        return widget;
    }
    
    public boolean setWidgetAlignment(final Widget widget, final Alignment alignment) {
        if (widget == null) {
            throw new NullPointerException("widget");
        }
        if (alignment == null) {
            throw new NullPointerException("alignment");
        }
        final WidgetSpring ws = this.widgetSprings.get(widget);
        if (ws == null) {
            return false;
        }
        assert widget.getParent() == this;
        ws.alignment = alignment;
        return true;
    }
    
    protected void recheckWidgets() {
        if (this.horz != null) {
            this.horz.recheckWidgets();
        }
        if (this.vert != null) {
            this.vert.recheckWidgets();
        }
    }
    
    protected void layoutGroupsChanged() {
        this.redoDefaultGaps = true;
        this.maybeInvalidateLayoutTree();
    }
    
    protected void maybeInvalidateLayoutTree() {
        if (this.horz != null && this.vert != null && !this.blockInvalidateLayoutTree) {
            this.invalidateLayout();
        }
    }
    
    @Override
    protected void childVisibilityChanged(final Widget child) {
        if (!this.includeInvisibleWidgets) {
            this.layoutGroupsChanged();
        }
    }
    
    void removeChild(final WidgetSpring widgetSpring) {
        final Widget widget = widgetSpring.w;
        final int idx = this.getChildIndex(widget);
        assert idx >= 0;
        super.removeChild(idx);
        this.widgetSprings.remove(widget);
    }
    
    static {
        DEBUG_LAYOUT_GROUPS = Widget.getSafeBooleanProperty("debugLayoutGroups");
        NO_GAP = new Gap(0, 0, 32767);
    }
    
    public static class Gap
    {
        public final int min;
        public final int preferred;
        public final int max;
        
        public Gap() {
            this(0, 0, 32767);
        }
        
        public Gap(final int size) {
            this(size, size, size);
        }
        
        public Gap(final int min, final int preferred) {
            this(min, preferred, 32767);
        }
        
        public Gap(final int min, final int preferred, final int max) {
            if (min < 0) {
                throw new IllegalArgumentException("min");
            }
            if (preferred < min) {
                throw new IllegalArgumentException("preferred");
            }
            if (max < 0 || (max > 0 && max < preferred)) {
                throw new IllegalArgumentException("max");
            }
            this.min = min;
            this.preferred = preferred;
            this.max = max;
        }
    }
    
    abstract static class Spring
    {
        abstract int getMinSize(final int p0);
        
        abstract int getPrefSize(final int p0);
        
        abstract int getMaxSize(final int p0);
        
        abstract void setSize(final int p0, final int p1, final int p2);
        
        void collectAllSprings(final HashSet<Spring> result) {
            result.add(this);
        }
        
        boolean isVisible() {
            return true;
        }
    }
    
    private static class WidgetSpring extends Spring
    {
        final Widget w;
        Alignment alignment;
        int x;
        int y;
        int width;
        int height;
        int minWidth;
        int minHeight;
        int maxWidth;
        int maxHeight;
        int prefWidth;
        int prefHeight;
        int flags;
        
        WidgetSpring(final Widget w) {
            this.w = w;
            this.alignment = Alignment.FILL;
        }
        
        void prepare() {
            this.x = this.w.getX();
            this.y = this.w.getY();
            this.width = this.w.getWidth();
            this.height = this.w.getHeight();
            this.minWidth = this.w.getMinWidth();
            this.minHeight = this.w.getMinHeight();
            this.maxWidth = this.w.getMaxWidth();
            this.maxHeight = this.w.getMaxHeight();
            this.prefWidth = Widget.computeSize(this.minWidth, this.w.getPreferredWidth(), this.maxWidth);
            this.prefHeight = Widget.computeSize(this.minHeight, this.w.getPreferredHeight(), this.maxHeight);
            this.flags = 0;
        }
        
        @Override
        int getMinSize(final int axis) {
            switch (axis) {
                case 0: {
                    return this.minWidth;
                }
                case 1: {
                    return this.minHeight;
                }
                default: {
                    throw new IllegalArgumentException("axis");
                }
            }
        }
        
        @Override
        int getPrefSize(final int axis) {
            switch (axis) {
                case 0: {
                    return this.prefWidth;
                }
                case 1: {
                    return this.prefHeight;
                }
                default: {
                    throw new IllegalArgumentException("axis");
                }
            }
        }
        
        @Override
        int getMaxSize(final int axis) {
            switch (axis) {
                case 0: {
                    return this.maxWidth;
                }
                case 1: {
                    return this.maxHeight;
                }
                default: {
                    throw new IllegalArgumentException("axis");
                }
            }
        }
        
        @Override
        void setSize(final int axis, final int pos, final int size) {
            this.flags |= 1 << axis;
            switch (axis) {
                case 0: {
                    this.x = pos;
                    this.width = size;
                    break;
                }
                case 1: {
                    this.y = pos;
                    this.height = size;
                    break;
                }
                default: {
                    throw new IllegalArgumentException("axis");
                }
            }
        }
        
        void apply() {
            if (this.flags != 3) {
                this.invalidState();
            }
            if (this.alignment != Alignment.FILL) {
                final int newWidth = Math.min(this.width, this.prefWidth);
                final int newHeight = Math.min(this.height, this.prefHeight);
                this.w.setPosition(this.x + this.alignment.computePositionX(this.width, newWidth), this.y + this.alignment.computePositionY(this.height, newHeight));
                this.w.setSize(newWidth, newHeight);
            }
            else {
                this.w.setPosition(this.x, this.y);
                this.w.setSize(this.width, this.height);
            }
        }
        
        @Override
        boolean isVisible() {
            return this.w.isVisible();
        }
        
        void invalidState() {
            final StringBuilder sb = new StringBuilder();
            sb.append("Widget ").append(this.w).append(" with theme ").append(this.w.getTheme()).append(" is not part of the following groups:");
            if ((this.flags & 0x1) == 0x0) {
                sb.append(" horizontal");
            }
            if ((this.flags & 0x2) == 0x0) {
                sb.append(" vertical");
            }
            throw new IllegalStateException(sb.toString());
        }
    }
    
    private class GapSpring extends Spring
    {
        final int min;
        final int pref;
        final int max;
        final boolean isDefault;
        
        GapSpring(final int min, final int pref, final int max, final boolean isDefault) {
            this.convertConstant(0, min);
            this.convertConstant(0, pref);
            this.convertConstant(0, max);
            this.min = min;
            this.pref = pref;
            this.max = max;
            this.isDefault = isDefault;
        }
        
        @Override
        int getMinSize(final int axis) {
            return this.convertConstant(axis, this.min);
        }
        
        @Override
        int getPrefSize(final int axis) {
            return this.convertConstant(axis, this.pref);
        }
        
        @Override
        int getMaxSize(final int axis) {
            return this.convertConstant(axis, this.max);
        }
        
        @Override
        void setSize(final int axis, final int pos, final int size) {
        }
        
        private int convertConstant(final int axis, final int value) {
            if (value >= 0) {
                return value;
            }
            Dimension dim = null;
            switch (value) {
                case -1: {
                    dim = DialogLayout.this.smallGap;
                    break;
                }
                case -2: {
                    dim = DialogLayout.this.mediumGap;
                    break;
                }
                case -3: {
                    dim = DialogLayout.this.largeGap;
                    break;
                }
                case -4: {
                    dim = DialogLayout.this.defaultGap;
                    break;
                }
                default: {
                    throw new IllegalArgumentException("Invalid gap size: " + value);
                }
            }
            if (dim == null) {
                return 0;
            }
            if (axis == 0) {
                return dim.getX();
            }
            return dim.getY();
        }
    }
    
    private class NamedGapSpring extends Spring
    {
        final String name;
        
        public NamedGapSpring(final String name) {
            this.name = name;
        }
        
        @Override
        int getMaxSize(final int axis) {
            return this.getGap().max;
        }
        
        @Override
        int getMinSize(final int axis) {
            return this.getGap().min;
        }
        
        @Override
        int getPrefSize(final int axis) {
            return this.getGap().preferred;
        }
        
        @Override
        void setSize(final int axis, final int pos, final int size) {
        }
        
        private Gap getGap() {
            if (DialogLayout.this.namedGaps != null) {
                return DialogLayout.this.namedGaps.getParameterValue(this.name, true, Gap.class, DialogLayout.NO_GAP);
            }
            return DialogLayout.NO_GAP;
        }
    }
    
    public abstract class Group extends Spring
    {
        final ArrayList<Spring> springs;
        boolean alreadyAdded;
        
        public Group() {
            this.springs = new ArrayList<Spring>();
        }
        
        void checkGroup(final DialogLayout owner) {
            if (DialogLayout.this != owner) {
                throw new IllegalArgumentException("Can't add group from different layout");
            }
            if (this.alreadyAdded) {
                throw new IllegalArgumentException("Group already added to another group");
            }
        }
        
        public Group addGroup(final Group g) {
            g.checkGroup(DialogLayout.this);
            g.alreadyAdded = true;
            this.addSpring(g);
            return this;
        }
        
        public Group addGroups(final Group... groups) {
            for (final Group g : groups) {
                this.addGroup(g);
            }
            return this;
        }
        
        public Group addWidget(final Widget w) {
            if (w.getParent() != DialogLayout.this) {
                DialogLayout.this.add(w);
            }
            final WidgetSpring s = DialogLayout.this.widgetSprings.get(w);
            if (s == null) {
                throw new IllegalStateException("WidgetSpring for Widget not found: " + w);
            }
            this.addSpring(s);
            return this;
        }
        
        public Group addWidget(final Widget w, final Alignment alignment) {
            this.addWidget(w);
            DialogLayout.this.setWidgetAlignment(w, alignment);
            return this;
        }
        
        public Group addWidgets(final Widget... widgets) {
            for (final Widget w : widgets) {
                this.addWidget(w);
            }
            return this;
        }
        
        public Group addWidgetsWithGap(final String gapName, final Widget... widgets) {
            final AnimationState.StateKey stateNotFirst = AnimationState.StateKey.get(gapName.concat("NotFirst"));
            final AnimationState.StateKey stateNotLast = AnimationState.StateKey.get(gapName.concat("NotLast"));
            for (int i = 0, n = widgets.length; i < n; ++i) {
                if (i > 0) {
                    this.addGap(gapName);
                }
                final Widget w = widgets[i];
                this.addWidget(w);
                final de.matthiasmann.twl.AnimationState as = w.getAnimationState();
                as.setAnimationState(stateNotFirst, i > 0);
                as.setAnimationState(stateNotLast, i < n - 1);
            }
            return this;
        }
        
        public Group addGap(final int min, final int pref, final int max) {
            this.addSpring(new GapSpring(min, pref, max, false));
            return this;
        }
        
        public Group addGap(final int size) {
            this.addSpring(new GapSpring(size, size, size, false));
            return this;
        }
        
        public Group addMinGap(final int minSize) {
            this.addSpring(new GapSpring(minSize, minSize, 32767, false));
            return this;
        }
        
        public Group addGap() {
            this.addSpring(new GapSpring(0, 0, 32767, false));
            return this;
        }
        
        public Group addGap(final String name) {
            if (name.length() == 0) {
                throw new IllegalArgumentException("name");
            }
            this.addSpring(new NamedGapSpring(name));
            return this;
        }
        
        public void removeDefaultGaps() {
            int i = this.springs.size();
            while (i-- > 0) {
                final Spring s = this.springs.get(i);
                if (s instanceof GapSpring) {
                    if (!((GapSpring)s).isDefault) {
                        continue;
                    }
                    this.springs.remove(i);
                }
                else {
                    if (!(s instanceof Group)) {
                        continue;
                    }
                    ((Group)s).removeDefaultGaps();
                }
            }
        }
        
        public void addDefaultGap() {
            for (int i = 0; i < this.springs.size(); ++i) {
                final Spring s = this.springs.get(i);
                if (s instanceof Group) {
                    ((Group)s).addDefaultGap();
                }
            }
        }
        
        public boolean removeGroup(final Group g, final boolean removeWidgets) {
            for (int i = 0; i < this.springs.size(); ++i) {
                if (this.springs.get(i) == g) {
                    this.springs.remove(i);
                    if (removeWidgets) {
                        g.removeWidgets();
                        DialogLayout.this.recheckWidgets();
                    }
                    DialogLayout.this.layoutGroupsChanged();
                    return true;
                }
            }
            return false;
        }
        
        public void clear(final boolean removeWidgets) {
            if (removeWidgets) {
                this.removeWidgets();
            }
            this.springs.clear();
            if (removeWidgets) {
                DialogLayout.this.recheckWidgets();
            }
            DialogLayout.this.layoutGroupsChanged();
        }
        
        void addSpring(final Spring s) {
            this.springs.add(s);
            DialogLayout.this.layoutGroupsChanged();
        }
        
        void recheckWidgets() {
            int i = this.springs.size();
            while (i-- > 0) {
                final Spring s = this.springs.get(i);
                if (s instanceof WidgetSpring) {
                    if (DialogLayout.this.widgetSprings.containsKey(((WidgetSpring)s).w)) {
                        continue;
                    }
                    this.springs.remove(i);
                }
                else {
                    if (!(s instanceof Group)) {
                        continue;
                    }
                    ((Group)s).recheckWidgets();
                }
            }
        }
        
        void removeWidgets() {
            int i = this.springs.size();
            while (i-- > 0) {
                final Spring s = this.springs.get(i);
                if (s instanceof WidgetSpring) {
                    DialogLayout.this.removeChild((WidgetSpring)s);
                }
                else {
                    if (!(s instanceof Group)) {
                        continue;
                    }
                    ((Group)s).removeWidgets();
                }
            }
        }
    }
    
    static class SpringDelta implements Comparable<SpringDelta>
    {
        final int idx;
        final int delta;
        
        SpringDelta(final int idx, final int delta) {
            this.idx = idx;
            this.delta = delta;
        }
        
        @Override
        public int compareTo(final SpringDelta o) {
            return this.delta - o.delta;
        }
    }
    
    class SequentialGroup extends Group
    {
        @Override
        int getMinSize(final int axis) {
            int size = 0;
            for (int i = 0, n = this.springs.size(); i < n; ++i) {
                final Spring s = this.springs.get(i);
                if (DialogLayout.this.includeInvisibleWidgets || s.isVisible()) {
                    size += s.getMinSize(axis);
                }
            }
            return size;
        }
        
        @Override
        int getPrefSize(final int axis) {
            int size = 0;
            for (int i = 0, n = this.springs.size(); i < n; ++i) {
                final Spring s = this.springs.get(i);
                if (DialogLayout.this.includeInvisibleWidgets || s.isVisible()) {
                    size += s.getPrefSize(axis);
                }
            }
            return size;
        }
        
        @Override
        int getMaxSize(final int axis) {
            int size = 0;
            boolean hasMax = false;
            for (int i = 0, n = this.springs.size(); i < n; ++i) {
                final Spring s = this.springs.get(i);
                if (DialogLayout.this.includeInvisibleWidgets || s.isVisible()) {
                    final int max = s.getMaxSize(axis);
                    if (max > 0) {
                        size += max;
                        hasMax = true;
                    }
                    else {
                        size += s.getPrefSize(axis);
                    }
                }
            }
            return hasMax ? size : 0;
        }
        
        @Override
        public void addDefaultGap() {
            if (this.springs.size() > 1) {
                boolean wasGap = true;
                for (int i = 0; i < this.springs.size(); ++i) {
                    final Spring s = this.springs.get(i);
                    if (DialogLayout.this.includeInvisibleWidgets || s.isVisible()) {
                        final boolean isGap = s instanceof GapSpring || s instanceof NamedGapSpring;
                        if (!isGap && !wasGap) {
                            this.springs.add(i++, new GapSpring(-4, -4, -4, true));
                        }
                        wasGap = isGap;
                    }
                }
            }
            super.addDefaultGap();
        }
        
        @Override
        void setSize(final int axis, int pos, final int size) {
            final int prefSize = this.getPrefSize(axis);
            if (size == prefSize) {
                for (final Spring s : this.springs) {
                    if (DialogLayout.this.includeInvisibleWidgets || s.isVisible()) {
                        final int spref = s.getPrefSize(axis);
                        s.setSize(axis, pos, spref);
                        pos += spref;
                    }
                }
            }
            else if (this.springs.size() == 1) {
                final Spring s2 = this.springs.get(0);
                s2.setSize(axis, pos, size);
            }
            else if (this.springs.size() > 1) {
                this.setSizeNonPref(axis, pos, size, prefSize);
            }
        }
        
        private void setSizeNonPref(final int axis, int pos, final int size, final int prefSize) {
            int delta = size - prefSize;
            final boolean useMin = delta < 0;
            if (useMin) {
                delta = -delta;
            }
            final SpringDelta[] deltas = new SpringDelta[this.springs.size()];
            int resizeable = 0;
            for (int i = 0; i < this.springs.size(); ++i) {
                final Spring s = this.springs.get(i);
                if (DialogLayout.this.includeInvisibleWidgets || s.isVisible()) {
                    final int sdelta = useMin ? (s.getPrefSize(axis) - s.getMinSize(axis)) : (s.getMaxSize(axis) - s.getPrefSize(axis));
                    if (sdelta > 0) {
                        deltas[resizeable++] = new SpringDelta(i, sdelta);
                    }
                }
            }
            if (resizeable > 0) {
                if (resizeable > 1) {
                    Arrays.sort(deltas, 0, resizeable);
                }
                final int[] sizes = new int[this.springs.size()];
                int remaining = resizeable;
                for (final SpringDelta d : deltas) {
                    final int sdelta2 = delta / remaining;
                    int ddelta = Math.min(d.delta, sdelta2);
                    delta -= ddelta;
                    --remaining;
                    if (useMin) {
                        ddelta = -ddelta;
                    }
                    sizes[d.idx] = ddelta;
                }
                for (int j = 0; j < this.springs.size(); ++j) {
                    final Spring s2 = this.springs.get(j);
                    if (DialogLayout.this.includeInvisibleWidgets || s2.isVisible()) {
                        final int ssize = s2.getPrefSize(axis) + sizes[j];
                        s2.setSize(axis, pos, ssize);
                        pos += ssize;
                    }
                }
            }
            else {
                for (final Spring s : this.springs) {
                    if (DialogLayout.this.includeInvisibleWidgets || s.isVisible()) {
                        int ssize2;
                        if (useMin) {
                            ssize2 = s.getMinSize(axis);
                        }
                        else {
                            ssize2 = s.getMaxSize(axis);
                            if (ssize2 == 0) {
                                ssize2 = s.getPrefSize(axis);
                            }
                        }
                        s.setSize(axis, pos, ssize2);
                        pos += ssize2;
                    }
                }
            }
        }
    }
    
    class ParallelGroup extends Group
    {
        @Override
        int getMinSize(final int axis) {
            int size = 0;
            for (int i = 0, n = this.springs.size(); i < n; ++i) {
                final Spring s = this.springs.get(i);
                if (DialogLayout.this.includeInvisibleWidgets || s.isVisible()) {
                    size = Math.max(size, s.getMinSize(axis));
                }
            }
            return size;
        }
        
        @Override
        int getPrefSize(final int axis) {
            int size = 0;
            for (int i = 0, n = this.springs.size(); i < n; ++i) {
                final Spring s = this.springs.get(i);
                if (DialogLayout.this.includeInvisibleWidgets || s.isVisible()) {
                    size = Math.max(size, s.getPrefSize(axis));
                }
            }
            return size;
        }
        
        @Override
        int getMaxSize(final int axis) {
            int size = 0;
            for (int i = 0, n = this.springs.size(); i < n; ++i) {
                final Spring s = this.springs.get(i);
                if (DialogLayout.this.includeInvisibleWidgets || s.isVisible()) {
                    size = Math.max(size, s.getMaxSize(axis));
                }
            }
            return size;
        }
        
        @Override
        void setSize(final int axis, final int pos, final int size) {
            for (int i = 0, n = this.springs.size(); i < n; ++i) {
                final Spring s = this.springs.get(i);
                if (DialogLayout.this.includeInvisibleWidgets || s.isVisible()) {
                    s.setSize(axis, pos, size);
                }
            }
        }
        
        @Override
        public Group addGap() {
            DialogLayout.getLogger().log(Level.WARNING, "Useless call to addGap() on ParallelGroup", new Throwable());
            return this;
        }
    }
}
