package de.matthiasmann.twl;

import de.matthiasmann.twl.renderer.*;

public class ProgressBar extends TextWidget
{
    public static final AnimationState.StateKey STATE_VALUE_CHANGED;
    public static final AnimationState.StateKey STATE_INDETERMINATE;
    public static final float VALUE_INDETERMINATE = -1.0f;
    private Image progressImage;
    private float value;
    
    public ProgressBar() {
        this.getAnimationState().resetAnimationTime(ProgressBar.STATE_VALUE_CHANGED);
    }
    
    public float getValue() {
        return this.value;
    }
    
    public void setIndeterminate() {
        if (this.value >= 0.0f) {
            this.value = -1.0f;
            final de.matthiasmann.twl.AnimationState animationState = this.getAnimationState();
            animationState.setAnimationState(ProgressBar.STATE_INDETERMINATE, true);
            animationState.resetAnimationTime(ProgressBar.STATE_VALUE_CHANGED);
        }
    }
    
    public void setValue(float value) {
        if (value <= 0.0f) {
            value = 0.0f;
        }
        else if (value > 1.0f) {
            value = 1.0f;
        }
        if (this.value != value) {
            this.value = value;
            final de.matthiasmann.twl.AnimationState animationState = this.getAnimationState();
            animationState.setAnimationState(ProgressBar.STATE_INDETERMINATE, false);
            animationState.resetAnimationTime(ProgressBar.STATE_VALUE_CHANGED);
        }
    }
    
    public String getText() {
        return (String)this.getCharSequence();
    }
    
    public void setText(final String text) {
        this.setCharSequence(text);
    }
    
    public Image getProgressImage() {
        return this.progressImage;
    }
    
    public void setProgressImage(final Image progressImage) {
        this.progressImage = progressImage;
    }
    
    protected void applyThemeProgressBar(final ThemeInfo themeInfo) {
        this.setProgressImage(themeInfo.getImage("progressImage"));
    }
    
    @Override
    protected void applyTheme(final ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        this.applyThemeProgressBar(themeInfo);
    }
    
    @Override
    protected void paintWidget(final GUI gui) {
        final int width = this.getInnerWidth();
        final int height = this.getInnerHeight();
        if (this.progressImage != null && this.value >= 0.0f) {
            final int imageWidth = this.progressImage.getWidth();
            final int progressWidth = width - imageWidth;
            int scaledWidth = (int)(progressWidth * this.value);
            if (scaledWidth < 0) {
                scaledWidth = 0;
            }
            else if (scaledWidth > progressWidth) {
                scaledWidth = progressWidth;
            }
            this.progressImage.draw((AnimationState)this.getAnimationState(), this.getInnerX(), this.getInnerY(), imageWidth + scaledWidth, height);
        }
        super.paintWidget(gui);
    }
    
    @Override
    public int getMinWidth() {
        int minWidth = super.getMinWidth();
        final Image bg = this.getBackground();
        if (bg != null) {
            minWidth = Math.max(minWidth, bg.getWidth() + this.getBorderHorizontal());
        }
        return minWidth;
    }
    
    @Override
    public int getMinHeight() {
        int minHeight = super.getMinHeight();
        final Image bg = this.getBackground();
        if (bg != null) {
            minHeight = Math.max(minHeight, bg.getHeight() + this.getBorderVertical());
        }
        return minHeight;
    }
    
    @Override
    public int getPreferredInnerWidth() {
        int prefWidth = super.getPreferredInnerWidth();
        if (this.progressImage != null) {
            prefWidth = Math.max(prefWidth, this.progressImage.getWidth());
        }
        return prefWidth;
    }
    
    @Override
    public int getPreferredInnerHeight() {
        int prefHeight = super.getPreferredInnerHeight();
        if (this.progressImage != null) {
            prefHeight = Math.max(prefHeight, this.progressImage.getHeight());
        }
        return prefHeight;
    }
    
    static {
        STATE_VALUE_CHANGED = AnimationState.StateKey.get("valueChanged");
        STATE_INDETERMINATE = AnimationState.StateKey.get("indeterminate");
    }
}
