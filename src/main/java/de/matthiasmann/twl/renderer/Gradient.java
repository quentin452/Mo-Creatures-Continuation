package de.matthiasmann.twl.renderer;

import java.util.*;
import de.matthiasmann.twl.*;

public class Gradient
{
    private final Type type;
    private Wrap wrap;
    private final ArrayList<Stop> stops;
    
    public Gradient(final Type type) {
        if (type == null) {
            throw new NullPointerException("type");
        }
        this.type = type;
        this.wrap = Wrap.SCALE;
        this.stops = new ArrayList<Stop>();
    }
    
    public Type getType() {
        return this.type;
    }
    
    public void setWrap(final Wrap wrap) {
        if (wrap == null) {
            throw new NullPointerException("wrap");
        }
        this.wrap = wrap;
    }
    
    public Wrap getWrap() {
        return this.wrap;
    }
    
    public int getNumStops() {
        return this.stops.size();
    }
    
    public Stop getStop(final int index) {
        return this.stops.get(index);
    }
    
    public Stop[] getStops() {
        return this.stops.toArray(new Stop[this.stops.size()]);
    }
    
    public void addStop(final float pos, final Color color) {
        if (color == null) {
            throw new NullPointerException("color");
        }
        final int numStops = this.stops.size();
        if (numStops == 0) {
            if (pos < 0.0f) {
                throw new IllegalArgumentException("first stop must be >= 0.0f");
            }
            if (pos > 0.0f) {
                this.stops.add(new Stop(0.0f, color));
            }
        }
        if (numStops > 0 && pos <= this.stops.get(numStops - 1).pos) {
            throw new IllegalArgumentException("pos must be monotone increasing");
        }
        this.stops.add(new Stop(pos, color));
    }
    
    public enum Type
    {
        HORIZONTAL, 
        VERTICAL;
    }
    
    public enum Wrap
    {
        SCALE, 
        CLAMP, 
        REPEAT, 
        MIRROR;
    }
    
    public static class Stop
    {
        final float pos;
        final Color color;
        
        public Stop(final float pos, final Color color) {
            this.pos = pos;
            this.color = color;
        }
        
        public float getPos() {
            return this.pos;
        }
        
        public Color getColor() {
            return this.color;
        }
    }
}
