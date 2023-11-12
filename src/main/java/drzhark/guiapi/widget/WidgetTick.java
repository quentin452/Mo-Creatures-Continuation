package drzhark.guiapi.widget;

import java.util.*;
import de.matthiasmann.twl.*;

public class WidgetTick extends Widget implements IWidgetAlwaysDraw
{
    protected ArrayList<iTick> ticks;
    
    public WidgetTick() {
        this.ticks = new ArrayList<iTick>();
        this.setMaxSize(0, 0);
    }
    
    public FrameTick addCallback(final Runnable callback) {
        final FrameTick tick = new FrameTick(callback);
        this.ticks.add(tick);
        return tick;
    }
    
    public DelayTick addCallback(final Runnable callback, final int timepertick) {
        final DelayTick tick = new DelayTick(callback, timepertick);
        this.ticks.add(tick);
        return tick;
    }
    
    public boolean addCustomTick(final iTick tick) {
        return this.ticks.add(tick);
    }
    
    public SingleTick addTimedCallback(final Runnable callback, final int delay) {
        final SingleTick tick = new SingleTick(callback, delay);
        this.ticks.add(tick);
        return tick;
    }
    
    public List<iTick> getTickArrayCopy() {
        return Collections.unmodifiableList((List<? extends iTick>)this.ticks);
    }
    
    protected void paintWidget(final GUI gui) {
        final iTick[] removedTicks = new iTick[this.ticks.size()];
        for (int i = 0; i < this.ticks.size(); ++i) {
            final iTick tick = this.ticks.get(i);
            try {
                tick.checkTick();
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Error when calling tick " + tick + " at position " + i + " in WidgetTick.", e);
            }
            if (tick.shouldRemove()) {
                removedTicks[i] = tick;
            }
        }
        for (int i = 0; i < removedTicks.length; ++i) {
            final iTick tick = removedTicks[i];
            if (tick != null) {
                this.ticks.remove(tick);
            }
        }
    }
    
    public class DelayTick implements iTick
    {
        long lastTick;
        boolean removeSelf;
        Runnable tickCallback;
        long timeToTick;
        
        public DelayTick(final Runnable callback, final int delay) {
            this.removeSelf = false;
            if (callback == null) {
                throw new IllegalArgumentException("Callback cannot be null.");
            }
            if (delay < 0) {
                throw new IllegalArgumentException("Delay must be 0 or higher.");
            }
            this.lastTick = 0L;
            this.timeToTick = delay;
            this.tickCallback = callback;
        }
        
        @Override
        public void checkTick() {
            final long millis = System.currentTimeMillis();
            if (this.lastTick + this.timeToTick < millis) {
                this.lastTick = millis;
                this.tickCallback.run();
            }
        }
        
        public void removeSelf() {
            this.removeSelf = true;
        }
        
        @Override
        public boolean shouldRemove() {
            return this.removeSelf;
        }
    }
    
    public class FrameTick implements iTick
    {
        boolean removeSelf;
        Runnable tickCallback;
        
        public FrameTick(final Runnable callback) {
            this.removeSelf = false;
            if (callback == null) {
                throw new IllegalArgumentException("Callback cannot be null.");
            }
            this.tickCallback = callback;
        }
        
        @Override
        public void checkTick() {
            this.tickCallback.run();
        }
        
        public void removeSelf() {
            this.removeSelf = true;
        }
        
        @Override
        public boolean shouldRemove() {
            return this.removeSelf;
        }
        
        @Override
        public String toString() {
            return "FrameTick [tickCallback=" + this.tickCallback + "]";
        }
    }
    
    public class SingleTick implements iTick
    {
        int delayBeforeRemove;
        Runnable tickCallback;
        long timeToTick;
        
        public SingleTick(final Runnable callback, final int delay) {
            if (callback == null) {
                throw new IllegalArgumentException("Callback cannot be null.");
            }
            if (delay < 0) {
                throw new IllegalArgumentException("Delay must be 0 or higher.");
            }
            this.timeToTick = -1L;
            this.delayBeforeRemove = delay;
            this.tickCallback = callback;
        }
        
        @Override
        public void checkTick() {
            if (this.delayBeforeRemove == 0) {
                this.tickCallback.run();
            }
            if (this.timeToTick == -1L) {
                this.timeToTick = System.currentTimeMillis() + this.delayBeforeRemove;
            }
            else if (this.timeToTick < System.currentTimeMillis()) {
                this.tickCallback.run();
            }
        }
        
        @Override
        public boolean shouldRemove() {
            return this.delayBeforeRemove == 0 || this.timeToTick < System.currentTimeMillis();
        }
        
        @Override
        public String toString() {
            return "SingleTick [tickCallback=" + this.tickCallback + "]";
        }
    }
    
    public interface iTick
    {
        void checkTick();
        
        boolean shouldRemove();
    }
}
