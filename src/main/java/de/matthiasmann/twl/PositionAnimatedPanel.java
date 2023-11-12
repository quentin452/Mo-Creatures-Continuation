package de.matthiasmann.twl;

import de.matthiasmann.twl.model.*;

public class PositionAnimatedPanel extends Widget
{
    private final Widget animatedWidget;
    private MouseSensitiveRectangle rect;
    private Direction direction;
    private int moveSpeedIn;
    private int moveSpeedOut;
    private int auraSizeX;
    private int auraSizeY;
    private boolean forceVisible;
    private boolean forceJumps;
    private BooleanModel forceVisibleModel;
    private Runnable forceVisibleModelCallback;
    
    public PositionAnimatedPanel(final Widget animatedWidget) {
        this.direction = Direction.TOP;
        if (animatedWidget == null) {
            throw new NullPointerException("animatedWidget");
        }
        this.animatedWidget = animatedWidget;
        this.setClip(true);
        this.add(animatedWidget);
    }
    
    public Direction getHideDirection() {
        return this.direction;
    }
    
    public void setHideDirection(final Direction direction) {
        if (direction == null) {
            throw new NullPointerException("direction");
        }
        this.direction = direction;
    }
    
    public int getMoveSpeedIn() {
        return this.moveSpeedIn;
    }
    
    public void setMoveSpeedIn(final int moveSpeedIn) {
        this.moveSpeedIn = moveSpeedIn;
    }
    
    public int getMoveSpeedOut() {
        return this.moveSpeedOut;
    }
    
    public void setMoveSpeedOut(final int moveSpeedOut) {
        this.moveSpeedOut = moveSpeedOut;
    }
    
    public int getAuraSizeX() {
        return this.auraSizeX;
    }
    
    public void setAuraSizeX(final int auraSizeX) {
        this.auraSizeX = auraSizeX;
    }
    
    public int getAuraSizeY() {
        return this.auraSizeY;
    }
    
    public void setAuraSizeY(final int auraSizeY) {
        this.auraSizeY = auraSizeY;
    }
    
    public boolean isForceVisible() {
        return this.forceVisible;
    }
    
    public void setForceVisible(final boolean forceVisible) {
        this.forceVisible = forceVisible;
        if (this.forceVisibleModel != null) {
            this.forceVisibleModel.setValue(forceVisible);
        }
    }
    
    public boolean isForceVisibleJumps() {
        return this.forceJumps;
    }
    
    public void setForceVisibleJumps(final boolean forceJumps) {
        this.forceJumps = forceJumps;
    }
    
    public BooleanModel getForceVisibleModel() {
        return this.forceVisibleModel;
    }
    
    public void setForceVisibleModel(final BooleanModel forceVisibleModel) {
        if (this.forceVisibleModel != forceVisibleModel) {
            if (this.forceVisibleModel != null) {
                this.forceVisibleModel.removeCallback(this.forceVisibleModelCallback);
            }
            if ((this.forceVisibleModel = forceVisibleModel) != null) {
                if (this.forceVisibleModelCallback == null) {
                    this.forceVisibleModelCallback = new ForceVisibleModelCallback();
                }
                forceVisibleModel.addCallback(this.forceVisibleModelCallback);
                this.syncWithForceVisibleModel();
            }
        }
    }
    
    public boolean isHidden() {
        final int x = this.animatedWidget.getX();
        final int y = this.animatedWidget.getY();
        return x == this.getInnerX() + this.direction.x * this.animatedWidget.getWidth() && y == this.getInnerY() + this.direction.y * this.animatedWidget.getHeight();
    }
    
    @Override
    public int getMinWidth() {
        return Math.max(super.getMinWidth(), this.animatedWidget.getMinWidth() + this.getBorderHorizontal());
    }
    
    @Override
    public int getMinHeight() {
        return Math.max(super.getMinHeight(), this.animatedWidget.getMinHeight() + this.getBorderVertical());
    }
    
    @Override
    public int getPreferredInnerWidth() {
        return this.animatedWidget.getPreferredWidth();
    }
    
    @Override
    public int getPreferredInnerHeight() {
        return this.animatedWidget.getPreferredHeight();
    }
    
    @Override
    protected void applyTheme(final ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        this.setHideDirection((Direction)themeInfo.getParameter("hidedirection", (Enum)Direction.TOP));
        this.setMoveSpeedIn(themeInfo.getParameter("speed.in", 2));
        this.setMoveSpeedOut(themeInfo.getParameter("speed.out", 1));
        this.setAuraSizeX(themeInfo.getParameter("aura.width", 50));
        this.setAuraSizeY(themeInfo.getParameter("aura.height", 50));
        this.setForceVisibleJumps(themeInfo.getParameter("forceVisibleJumps", false));
    }
    
    @Override
    protected void afterAddToGUI(final GUI gui) {
        super.afterAddToGUI(gui);
        this.rect = gui.createMouseSenitiveRectangle();
        this.setRectSize();
    }
    
    @Override
    protected void beforeRemoveFromGUI(final GUI gui) {
        super.beforeRemoveFromGUI(gui);
        this.rect = null;
    }
    
    @Override
    protected void layout() {
        this.animatedWidget.setSize(this.getInnerWidth(), this.getInnerHeight());
        this.setRectSize();
    }
    
    @Override
    protected void positionChanged() {
        this.setRectSize();
    }
    
    @Override
    protected void paint(final GUI gui) {
        if (this.rect != null) {
            final int x = this.getInnerX();
            final int y = this.getInnerY();
            final boolean forceOpen = this.forceVisible || this.hasOpenPopups();
            if (forceOpen && this.forceJumps) {
                this.animatedWidget.setPosition(x, y);
            }
            else if (forceOpen || this.rect.isMouseOver()) {
                this.animatedWidget.setPosition(this.calcPosIn(this.animatedWidget.getX(), x, this.direction.x), this.calcPosIn(this.animatedWidget.getY(), y, this.direction.y));
            }
            else {
                this.animatedWidget.setPosition(this.calcPosOut(this.animatedWidget.getX(), x, this.direction.x * this.animatedWidget.getWidth()), this.calcPosOut(this.animatedWidget.getY(), y, this.direction.y * this.animatedWidget.getHeight()));
            }
        }
        super.paint(gui);
    }
    
    private void setRectSize() {
        if (this.rect != null) {
            this.rect.setXYWH(this.getX() - this.auraSizeX, this.getY() - this.auraSizeY, this.getWidth() + 2 * this.auraSizeX, this.getHeight() + 2 * this.auraSizeY);
        }
    }
    
    private int calcPosIn(final int cur, final int org, final int dir) {
        if (dir < 0) {
            return Math.min(org, cur + this.moveSpeedIn);
        }
        return Math.max(org, cur - this.moveSpeedIn);
    }
    
    private int calcPosOut(final int cur, final int org, final int dist) {
        if (dist < 0) {
            return Math.max(org + dist, cur - this.moveSpeedIn);
        }
        return Math.min(org + dist, cur + this.moveSpeedIn);
    }
    
    void syncWithForceVisibleModel() {
        this.setForceVisible(this.forceVisibleModel.getValue());
    }
    
    public enum Direction
    {
        TOP(0, -1), 
        LEFT(-1, 0), 
        BOTTOM(0, 1), 
        RIGHT(1, 0);
        
        final int x;
        final int y;
        
        private Direction(final int x, final int y) {
            this.x = x;
            this.y = y;
        }
    }
    
    class ForceVisibleModelCallback implements Runnable
    {
        @Override
        public void run() {
            PositionAnimatedPanel.this.syncWithForceVisibleModel();
        }
    }
}
