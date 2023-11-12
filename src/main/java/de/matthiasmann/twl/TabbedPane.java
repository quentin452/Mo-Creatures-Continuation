package de.matthiasmann.twl;

import de.matthiasmann.twl.renderer.*;
import java.util.*;
import de.matthiasmann.twl.model.*;

public class TabbedPane extends Widget
{
    public static final AnimationState.StateKey STATE_FIRST_TAB;
    public static final AnimationState.StateKey STATE_LAST_TAB;
    private final ArrayList<Tab> tabs;
    private final BoxLayout tabBox;
    private final Widget tabBoxClip;
    private final Container container;
    final Container innerContainer;
    DialogLayout scrollControlls;
    Button btnScrollLeft;
    Button btnScrollRight;
    boolean scrollTabs;
    int tabScrollPosition;
    TabPosition tabPosition;
    Tab activeTab;
    
    public TabbedPane() {
        this.tabs = new ArrayList<Tab>();
        this.tabBox = new BoxLayout();
        this.tabBoxClip = new Widget();
        this.container = new Container();
        this.innerContainer = new Container();
        this.tabPosition = TabPosition.TOP;
        this.tabBox.setTheme("tabbox");
        this.tabBoxClip.setTheme("");
        this.innerContainer.setTheme("");
        this.innerContainer.setClip(true);
        this.tabBoxClip.add((Widget)this.tabBox);
        this.container.add((Widget)this.innerContainer);
        super.insertChild((Widget)this.container, 0);
        super.insertChild(this.tabBoxClip, 1);
        this.addActionMapping("nextTab", "cycleTabs", 1);
        this.addActionMapping("prevTab", "cycleTabs", -1);
        this.setCanAcceptKeyboardFocus(false);
    }
    
    public TabPosition getTabPosition() {
        return this.tabPosition;
    }
    
    public void setTabPosition(final TabPosition tabPosition) {
        if (tabPosition == null) {
            throw new NullPointerException("tabPosition");
        }
        if (this.tabPosition != tabPosition) {
            this.tabPosition = tabPosition;
            this.tabBox.setDirection(tabPosition.horz ? BoxLayout.Direction.HORIZONTAL : BoxLayout.Direction.VERTICAL);
            this.invalidateLayout();
        }
    }
    
    public boolean isScrollTabs() {
        return this.scrollTabs;
    }
    
    public void setScrollTabs(final boolean scrollTabs) {
        if (this.scrollTabs != scrollTabs) {
            this.scrollTabs = scrollTabs;
            if (this.scrollControlls == null && scrollTabs) {
                this.createScrollControlls();
            }
            this.tabBoxClip.setClip(scrollTabs);
            if (this.scrollControlls != null) {
                this.scrollControlls.setVisible(scrollTabs);
            }
            this.invalidateLayout();
        }
    }
    
    public Tab addTab(final String title, final Widget pane) {
        final Tab tab = new Tab();
        tab.setTitle(title);
        tab.setPane(pane);
        this.tabBox.add((Widget)tab.button);
        this.tabs.add(tab);
        if (this.tabs.size() == 1) {
            this.setActiveTab(tab);
        }
        this.updateTabStates();
        return tab;
    }
    
    public Tab getActiveTab() {
        return this.activeTab;
    }
    
    public void setActiveTab(final Tab tab) {
        if (tab != null) {
            this.validateTab(tab);
        }
        if (this.activeTab != tab) {
            final Tab prevTab = this.activeTab;
            this.activeTab = tab;
            if (prevTab != null) {
                prevTab.doCallback();
            }
            if (tab != null) {
                tab.doCallback();
            }
            if (this.scrollTabs) {
                this.validateLayout();
                int pos;
                int end;
                int size;
                if (this.tabPosition.horz) {
                    pos = tab.button.getX() - this.tabBox.getX();
                    end = tab.button.getWidth() + pos;
                    size = this.tabBoxClip.getWidth();
                }
                else {
                    pos = tab.button.getY() - this.tabBox.getY();
                    end = tab.button.getHeight() + pos;
                    size = this.tabBoxClip.getHeight();
                }
                final int border = (size + 19) / 20;
                pos -= border;
                end += border;
                if (pos < this.tabScrollPosition) {
                    this.setScrollPos(pos);
                }
                else if (end > this.tabScrollPosition + size) {
                    this.setScrollPos(end - size);
                }
            }
            if (tab != null && tab.pane != null) {
                tab.pane.requestKeyboardFocus();
            }
        }
    }
    
    public void removeTab(final Tab tab) {
        this.validateTab(tab);
        final int idx = (tab == this.activeTab) ? this.tabs.indexOf(tab) : -1;
        tab.setPane(null);
        this.tabBox.removeChild((Widget)tab.button);
        this.tabs.remove(tab);
        if (idx >= 0 && !this.tabs.isEmpty()) {
            this.setActiveTab(this.tabs.get(Math.min(this.tabs.size() - 1, idx)));
        }
        this.updateTabStates();
    }
    
    public void removeAllTabs() {
        this.innerContainer.removeAllChildren();
        this.tabBox.removeAllChildren();
        this.tabs.clear();
        this.activeTab = null;
    }
    
    public int getNumTabs() {
        return this.tabs.size();
    }
    
    public Tab getTab(final int index) {
        return this.tabs.get(index);
    }
    
    public int getActiveTabIndex() {
        if (this.tabs.isEmpty()) {
            return -1;
        }
        return this.tabs.indexOf(this.activeTab);
    }
    
    public void cycleTabs(final int direction) {
        if (!this.tabs.isEmpty()) {
            int idx = this.tabs.indexOf(this.activeTab);
            if (idx < 0) {
                idx = 0;
            }
            else {
                idx += direction;
                idx %= this.tabs.size();
                idx += this.tabs.size();
                idx %= this.tabs.size();
            }
            this.setActiveTab(this.tabs.get(idx));
        }
    }
    
    @Override
    public int getMinWidth() {
        int minWidth;
        if (this.tabPosition.horz) {
            int tabBoxWidth;
            if (this.scrollTabs) {
                tabBoxWidth = this.tabBox.getBorderHorizontal() + BoxLayout.computeMinWidthVertical((Widget)this.tabBox) + this.scrollControlls.getPreferredWidth();
            }
            else {
                tabBoxWidth = this.tabBox.getMinWidth();
            }
            minWidth = Math.max(this.container.getMinWidth(), tabBoxWidth);
        }
        else {
            minWidth = this.container.getMinWidth() + this.tabBox.getMinWidth();
        }
        return Math.max(super.getMinWidth(), minWidth + this.getBorderHorizontal());
    }
    
    @Override
    public int getMinHeight() {
        int minHeight;
        if (this.tabPosition.horz) {
            minHeight = this.container.getMinHeight() + this.tabBox.getMinHeight();
        }
        else {
            minHeight = Math.max(this.container.getMinHeight(), this.tabBox.getMinHeight());
        }
        return Math.max(super.getMinHeight(), minHeight + this.getBorderVertical());
    }
    
    @Override
    public int getPreferredInnerWidth() {
        if (this.tabPosition.horz) {
            int tabBoxWidth;
            if (this.scrollTabs) {
                tabBoxWidth = this.tabBox.getBorderHorizontal() + BoxLayout.computePreferredWidthVertical((Widget)this.tabBox) + this.scrollControlls.getPreferredWidth();
            }
            else {
                tabBoxWidth = this.tabBox.getPreferredWidth();
            }
            return Math.max(this.container.getPreferredWidth(), tabBoxWidth);
        }
        return this.container.getPreferredWidth() + this.tabBox.getPreferredWidth();
    }
    
    @Override
    public int getPreferredInnerHeight() {
        if (this.tabPosition.horz) {
            return this.container.getPreferredHeight() + this.tabBox.getPreferredHeight();
        }
        return Math.max(this.container.getPreferredHeight(), this.tabBox.getPreferredHeight());
    }
    
    @Override
    protected void layout() {
        int scrollCtrlsWidth = 0;
        int scrollCtrlsHeight = 0;
        int tabBoxWidth = this.tabBox.getPreferredWidth();
        int tabBoxHeight = this.tabBox.getPreferredHeight();
        if (this.scrollTabs) {
            scrollCtrlsWidth = this.scrollControlls.getPreferredWidth();
            scrollCtrlsHeight = this.scrollControlls.getPreferredHeight();
        }
        if (this.tabPosition.horz) {
            tabBoxHeight = Math.max(scrollCtrlsHeight, tabBoxHeight);
        }
        else {
            tabBoxWidth = Math.max(scrollCtrlsWidth, tabBoxWidth);
        }
        this.tabBox.setSize(tabBoxWidth, tabBoxHeight);
        switch (this.tabPosition) {
            case TOP: {
                this.tabBoxClip.setPosition(this.getInnerX(), this.getInnerY());
                this.tabBoxClip.setSize(Math.max(0, this.getInnerWidth() - scrollCtrlsWidth), tabBoxHeight);
                this.container.setSize(this.getInnerWidth(), Math.max(0, this.getInnerHeight() - tabBoxHeight));
                this.container.setPosition(this.getInnerX(), this.tabBoxClip.getBottom());
                break;
            }
            case LEFT: {
                this.tabBoxClip.setPosition(this.getInnerX(), this.getInnerY());
                this.tabBoxClip.setSize(tabBoxWidth, Math.max(0, this.getInnerHeight() - scrollCtrlsHeight));
                this.container.setSize(Math.max(0, this.getInnerWidth() - tabBoxWidth), this.getInnerHeight());
                this.container.setPosition(this.tabBoxClip.getRight(), this.getInnerY());
                break;
            }
            case RIGHT: {
                this.tabBoxClip.setPosition(this.getInnerX() - tabBoxWidth, this.getInnerY());
                this.tabBoxClip.setSize(tabBoxWidth, Math.max(0, this.getInnerHeight() - scrollCtrlsHeight));
                this.container.setSize(Math.max(0, this.getInnerWidth() - tabBoxWidth), this.getInnerHeight());
                this.container.setPosition(this.getInnerX(), this.getInnerY());
                break;
            }
            case BOTTOM: {
                this.tabBoxClip.setPosition(this.getInnerX(), this.getInnerY() - tabBoxHeight);
                this.tabBoxClip.setSize(Math.max(0, this.getInnerWidth() - scrollCtrlsWidth), tabBoxHeight);
                this.container.setSize(this.getInnerWidth(), Math.max(0, this.getInnerHeight() - tabBoxHeight));
                this.container.setPosition(this.getInnerX(), this.getInnerY());
                break;
            }
        }
        if (this.scrollControlls != null) {
            if (this.tabPosition.horz) {
                this.scrollControlls.setPosition(this.tabBoxClip.getRight(), this.tabBoxClip.getY());
                this.scrollControlls.setSize(scrollCtrlsWidth, tabBoxHeight);
            }
            else {
                this.scrollControlls.setPosition(this.tabBoxClip.getX(), this.tabBoxClip.getBottom());
                this.scrollControlls.setSize(tabBoxWidth, scrollCtrlsHeight);
            }
            this.setScrollPos(this.tabScrollPosition);
        }
    }
    
    private void createScrollControlls() {
        (this.scrollControlls = new DialogLayout()).setTheme("scrollControls");
        (this.btnScrollLeft = new Button()).setTheme("scrollLeft");
        this.btnScrollLeft.addCallback((Runnable)new CB(-1));
        (this.btnScrollRight = new Button()).setTheme("scrollRight");
        this.btnScrollRight.addCallback((Runnable)new CB(1));
        final DialogLayout.Group horz = this.scrollControlls.createSequentialGroup().addWidget((Widget)this.btnScrollLeft).addGap("scrollButtons").addWidget((Widget)this.btnScrollRight);
        final DialogLayout.Group vert = this.scrollControlls.createParallelGroup().addWidget((Widget)this.btnScrollLeft).addWidget((Widget)this.btnScrollRight);
        this.scrollControlls.setHorizontalGroup(horz);
        this.scrollControlls.setVerticalGroup(vert);
        super.insertChild((Widget)this.scrollControlls, 2);
    }
    
    void scrollTabs(int dir) {
        dir *= Math.max(1, this.tabBoxClip.getWidth() / 10);
        this.setScrollPos(this.tabScrollPosition + dir);
    }
    
    private void setScrollPos(int pos) {
        int maxPos;
        if (this.tabPosition.horz) {
            maxPos = this.tabBox.getWidth() - this.tabBoxClip.getWidth();
        }
        else {
            maxPos = this.tabBox.getHeight() - this.tabBoxClip.getHeight();
        }
        pos = Math.max(0, Math.min(pos, maxPos));
        this.tabScrollPosition = pos;
        if (this.tabPosition.horz) {
            this.tabBox.setPosition(this.tabBoxClip.getX() - pos, this.tabBoxClip.getY());
        }
        else {
            this.tabBox.setPosition(this.tabBoxClip.getX(), this.tabBoxClip.getY() - pos);
        }
        if (this.scrollControlls != null) {
            this.btnScrollLeft.setEnabled(pos > 0);
            this.btnScrollRight.setEnabled(pos < maxPos);
        }
    }
    
    @Override
    public void insertChild(final Widget child, final int index) {
        throw new UnsupportedOperationException("use addTab/removeTab");
    }
    
    @Override
    public void removeAllChildren() {
        throw new UnsupportedOperationException("use addTab/removeTab");
    }
    
    @Override
    public Widget removeChild(final int index) {
        throw new UnsupportedOperationException("use addTab/removeTab");
    }
    
    protected void updateTabStates() {
        for (int i = 0, n = this.tabs.size(); i < n; ++i) {
            final Tab tab = this.tabs.get(i);
            final de.matthiasmann.twl.AnimationState animationState = tab.button.getAnimationState();
            animationState.setAnimationState(TabbedPane.STATE_FIRST_TAB, i == 0);
            animationState.setAnimationState(TabbedPane.STATE_LAST_TAB, i == n - 1);
        }
    }
    
    private void validateTab(final Tab tab) {
        if (tab.button.getParent() != this.tabBox) {
            throw new IllegalArgumentException("Invalid tab");
        }
    }
    
    static {
        STATE_FIRST_TAB = AnimationState.StateKey.get("firstTab");
        STATE_LAST_TAB = AnimationState.StateKey.get("lastTab");
    }
    
    public enum TabPosition
    {
        TOP(true), 
        LEFT(false), 
        RIGHT(true), 
        BOTTOM(false);
        
        final boolean horz;
        
        private TabPosition(final boolean horz) {
            this.horz = horz;
        }
    }
    
    public class Tab extends HasCallback implements BooleanModel
    {
        final TabButton button;
        Widget pane;
        Runnable closeCallback;
        Object userValue;
        
        Tab() {
            this.button = new TabButton((BooleanModel)this);
        }
        
        public boolean getValue() {
            return TabbedPane.this.activeTab == this;
        }
        
        public void setValue(final boolean value) {
            if (value) {
                TabbedPane.this.setActiveTab(this);
            }
        }
        
        public Widget getPane() {
            return this.pane;
        }
        
        public void setPane(final Widget pane) {
            if (this.pane != pane) {
                if (this.pane != null) {
                    TabbedPane.this.innerContainer.removeChild(this.pane);
                }
                if ((this.pane = pane) != null) {
                    pane.setVisible(this.getValue());
                    TabbedPane.this.innerContainer.add(pane);
                }
            }
        }
        
        public Tab setTitle(final String title) {
            this.button.setText(title);
            return this;
        }
        
        public String getTitle() {
            return this.button.getText();
        }
        
        public Object getUserValue() {
            return this.userValue;
        }
        
        public void setUserValue(final Object userValue) {
            this.userValue = userValue;
        }
        
        public Tab setTheme(final String theme) {
            this.button.setUserTheme(theme);
            return this;
        }
        
        public Runnable getCloseCallback() {
            return this.closeCallback;
        }
        
        public void setCloseCallback(final Runnable closeCallback) {
            if (this.closeCallback != null) {
                this.button.removeCloseButton();
            }
            if ((this.closeCallback = closeCallback) != null) {
                this.button.setCloseButton(closeCallback);
            }
        }
        
        protected void doCallback() {
            if (this.pane != null) {
                this.pane.setVisible(this.getValue());
            }
            super.doCallback();
        }
    }
    
    private static class TabButton extends ToggleButton
    {
        Button closeButton;
        Alignment closeButtonAlignment;
        int closeButtonOffsetX;
        int closeButtonOffsetY;
        String userTheme;
        
        TabButton(final BooleanModel model) {
            super(model);
            this.setCanAcceptKeyboardFocus(false);
            this.closeButtonAlignment = Alignment.RIGHT;
        }
        
        public void setUserTheme(final String userTheme) {
            this.userTheme = userTheme;
            this.doSetTheme();
        }
        
        private void doSetTheme() {
            if (this.userTheme != null) {
                this.setTheme(this.userTheme);
            }
            else if (this.closeButton != null) {
                this.setTheme("tabbuttonWithCloseButton");
            }
            else {
                this.setTheme("tabbutton");
            }
            this.reapplyTheme();
        }
        
        protected void applyTheme(final ThemeInfo themeInfo) {
            super.applyTheme(themeInfo);
            if (this.closeButton != null) {
                this.closeButtonAlignment = (Alignment)themeInfo.getParameter("closeButtonAlignment", (Enum)Alignment.RIGHT);
                this.closeButtonOffsetX = themeInfo.getParameter("closeButtonOffsetX", 0);
                this.closeButtonOffsetY = themeInfo.getParameter("closeButtonOffsetY", 0);
            }
            else {
                this.closeButtonAlignment = Alignment.RIGHT;
                this.closeButtonOffsetX = 0;
                this.closeButtonOffsetY = 0;
            }
        }
        
        void setCloseButton(final Runnable callback) {
            (this.closeButton = new Button()).setTheme("closeButton");
            this.doSetTheme();
            this.add((Widget)this.closeButton);
            this.closeButton.addCallback(callback);
        }
        
        void removeCloseButton() {
            this.removeChild((Widget)this.closeButton);
            this.closeButton = null;
            this.doSetTheme();
        }
        
        public int getPreferredInnerHeight() {
            return this.computeTextHeight();
        }
        
        public int getPreferredInnerWidth() {
            return this.computeTextWidth();
        }
        
        protected void layout() {
            if (this.closeButton != null) {
                this.closeButton.adjustSize();
                this.closeButton.setPosition(this.getX() + this.closeButtonOffsetX + this.closeButtonAlignment.computePositionX(this.getWidth(), this.closeButton.getWidth()), this.getY() + this.closeButtonOffsetY + this.closeButtonAlignment.computePositionY(this.getHeight(), this.closeButton.getHeight()));
            }
        }
    }
    
    private class CB implements Runnable
    {
        final int dir;
        
        CB(final int dir) {
            this.dir = dir;
        }
        
        @Override
        public void run() {
            TabbedPane.this.scrollTabs(this.dir);
        }
    }
}
