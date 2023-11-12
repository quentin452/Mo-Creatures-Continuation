package de.matthiasmann.twl.utils;

import de.matthiasmann.twl.AnimationState;
import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.*;

public class TintAnimator
{
    private static final float ZERO_EPSILON = 0.001f;
    private static final float ONE_EPSILON = 0.999f;
    private final TimeSource timeSource;
    private final float[] currentTint;
    private int fadeDuration;
    private boolean fadeActive;
    private boolean hasTint;
    private Runnable[] fadeDoneCallbacks;

    public TintAnimator(final TimeSource timeSource, final Color color) {
        if (timeSource == null) {
            throw new NullPointerException("timeSource");
        }
        if (color == null) {
            throw new NullPointerException("color");
        }
        this.timeSource = timeSource;
        this.currentTint = new float[12];
        this.setColor(color);
    }

    public TintAnimator(final GUI gui, final Color color) {
        this(new GUITimeSource(gui), color);
    }

    public TintAnimator(final Widget owner, final Color color) {
        this(new GUITimeSource(owner), color);
    }

    public TintAnimator(final TimeSource timeSource) {
        this(timeSource, Color.WHITE);
    }

    public TintAnimator(final GUI gui) {
        this(new GUITimeSource(gui));
    }

    public TintAnimator(final Widget owner) {
        this(new GUITimeSource(owner));
    }

    public void setColor(final Color color) {
        color.getFloats(this.currentTint, 0);
        color.getFloats(this.currentTint, 4);
        this.hasTint = !Color.WHITE.equals((Object)color);
        this.fadeActive = false;
        this.fadeDuration = 0;
        this.timeSource.resetTime();
    }

    public void addFadeDoneCallback(final Runnable cb) {
        this.fadeDoneCallbacks = (Runnable[])CallbackSupport.addCallbackToList((Object[])this.fadeDoneCallbacks, (Object)cb, (Class)Runnable.class);
    }

    public void removeFadeDoneCallback(final Runnable cb) {
        this.fadeDoneCallbacks = (Runnable[])CallbackSupport.removeCallbackFromList((Object[])this.fadeDoneCallbacks, (Object)cb);
    }

    public void fadeTo(final Color color, final int fadeDuration) {
        if (fadeDuration <= 0) {
            this.setColor(color);
        }
        else {
            color.getFloats(this.currentTint, 8);
            System.arraycopy(this.currentTint, 0, this.currentTint, 4, 4);
            this.fadeActive = true;
            this.fadeDuration = fadeDuration;
            this.hasTint = true;
            this.timeSource.resetTime();
        }
    }

    public void fadeToHide(final int fadeDuration) {
        if (fadeDuration <= 0) {
            this.currentTint[3] = 0.0f;
            this.fadeActive = false;
            this.fadeDuration = 0;
            this.hasTint = true;
        }
        else {
            System.arraycopy(this.currentTint, 0, this.currentTint, 4, 8);
            this.currentTint[11] = 0.0f;
            this.fadeActive = !this.isZeroAlpha();
            this.fadeDuration = fadeDuration;
            this.hasTint = true;
            this.timeSource.resetTime();
        }
    }

    public void update() {
        if (this.fadeActive) {
            final int time = this.timeSource.getTime();
            final float t = Math.min(time, this.fadeDuration) / (float)this.fadeDuration;
            final float tm1 = 1.0f - t;
            final float[] tint = this.currentTint;
            for (int i = 0; i < 4; ++i) {
                tint[i] = tm1 * tint[i + 4] + t * tint[i + 8];
            }
            if (time >= this.fadeDuration) {
                this.fadeActive = false;
                this.hasTint = (this.currentTint[0] < 0.999f || this.currentTint[1] < 0.999f || this.currentTint[2] < 0.999f || this.currentTint[3] < 0.999f);
                CallbackSupport.fireCallbacks(this.fadeDoneCallbacks);
            }
        }
    }

    public boolean isFadeActive() {
        return this.fadeActive;
    }

    public boolean hasTint() {
        return this.hasTint;
    }

    public boolean isZeroAlpha() {
        return this.currentTint[3] <= 0.001f;
    }

    public void paintWithTint(final Renderer renderer) {
        final float[] tint = this.currentTint;
        renderer.pushGlobalTintColor(tint[0], tint[1], tint[2], tint[3]);
    }

    public static final class GUITimeSource implements TimeSource
    {
        private final Widget owner;
        private final GUI gui;
        private long startTime;
        private boolean pendingReset;

        public GUITimeSource(final Widget owner) {
            if (owner == null) {
                throw new NullPointerException("owner");
            }
            this.owner = owner;
            this.gui = null;
            this.resetTime();
        }

        public GUITimeSource(final GUI gui) {
            if (gui == null) {
                throw new NullPointerException("gui");
            }
            this.owner = null;
            this.gui = gui;
        }

        @Override
        public int getTime() {
            final GUI g = this.getGUI();
            if (g != null) {
                if (this.pendingReset) {
                    this.pendingReset = false;
                    this.startTime = g.getCurrentTime();
                }
                return (int)(g.getCurrentTime() - this.startTime) & Integer.MAX_VALUE;
            }
            return 0;
        }

        @Override
        public void resetTime() {
            final GUI g = this.getGUI();
            if (g != null) {
                this.startTime = g.getCurrentTime();
                this.pendingReset = false;
            }
            else {
                this.pendingReset = true;
            }
        }

        private GUI getGUI() {
            return (this.gui != null) ? this.gui : this.owner.getGUI();
        }
    }

    public static class AnimationStateTimeSource implements TimeSource
    {
        private final AnimationState animState;
        private final de.matthiasmann.twl.renderer.AnimationState.StateKey animStateKey;

        public AnimationStateTimeSource(final AnimationState animState, final String animStateName) {
            this(animState, de.matthiasmann.twl.renderer.AnimationState.StateKey.get(animStateName));
        }

        public AnimationStateTimeSource(final AnimationState animState, final de.matthiasmann.twl.renderer.AnimationState.StateKey animStateKey) {
            if (animState == null) {
                throw new NullPointerException("animState");
            }
            if (animStateKey == null) {
                throw new NullPointerException("animStateKey");
            }
            this.animState = animState;
            this.animStateKey = animStateKey;
        }

        @Override
        public int getTime() {
            return this.animState.getAnimationTime(this.animStateKey);
        }

        @Override
        public void resetTime() {
            this.animState.resetAnimationTime(this.animStateKey);
        }
    }

    public interface TimeSource
    {
        void resetTime();

        int getTime();
    }
}
