package de.matthiasmann.twl;

import java.util.logging.*;

public final class Timer
{
    private static final int TIMER_COUNTER_IN_CALLBACK = -1;
    private static final int TIMER_COUNTER_DO_START = -2;
    private static final int TIMER_COUNTER_DO_STOP = -3;
    final GUI gui;
    int counter;
    int delay;
    boolean continuous;
    Runnable callback;
    
    public Timer(final GUI gui) {
        this.delay = 10;
        if (gui == null) {
            throw new NullPointerException("gui");
        }
        this.gui = gui;
    }
    
    public boolean isRunning() {
        return this.counter > 0 || (this.continuous && this.counter == -1);
    }
    
    public void setDelay(final int delay) {
        if (delay < 1) {
            throw new IllegalArgumentException("delay < 1");
        }
        this.delay = delay;
    }
    
    public void start() {
        if (this.counter == 0) {
            this.counter = this.delay;
            this.gui.activeTimers.add(this);
        }
        else if (this.counter < 0) {
            this.counter = -2;
        }
    }
    
    public void stop() {
        if (this.counter > 0) {
            this.counter = 0;
            this.gui.activeTimers.remove(this);
        }
        else if (this.counter < 0) {
            this.counter = -3;
        }
    }
    
    public void setCallback(final Runnable callback) {
        this.callback = callback;
    }
    
    public boolean isContinuous() {
        return this.continuous;
    }
    
    public void setContinuous(final boolean continuous) {
        this.continuous = continuous;
    }
    
    boolean tick(final int delta) {
        final int newCounter = this.counter - delta;
        if (newCounter <= 0) {
            final boolean doStop = !this.continuous;
            this.counter = -1;
            this.doCallback();
            if (this.counter == -3) {
                this.counter = 0;
                return false;
            }
            if (doStop && this.counter != -2) {
                this.counter = 0;
                return false;
            }
            this.counter = Math.max(1, newCounter + this.delay);
        }
        else {
            this.counter = newCounter;
        }
        return true;
    }
    
    private void doCallback() {
        if (this.callback != null) {
            try {
                this.callback.run();
            }
            catch (Throwable ex) {
                Logger.getLogger(Timer.class.getName()).log(Level.SEVERE, "Exception in callback", ex);
            }
        }
    }
}
