package de.matthiasmann.twl;

public class AnimationState implements de.matthiasmann.twl.renderer.AnimationState
{
    private final AnimationState parent;
    private State[] stateTable;
    private GUI gui;
    
    public AnimationState(final AnimationState parent, final int size) {
        this.parent = parent;
        this.stateTable = new State[size];
    }
    
    public AnimationState(final AnimationState parent) {
        this(parent, 16);
    }
    
    public AnimationState() {
        this(null);
    }
    
    public void setGUI(final GUI gui) {
        this.gui = gui;
        final long curTime = this.getCurrentTime();
        for (final State s : this.stateTable) {
            if (s != null) {
                s.lastChangedTime = curTime;
            }
        }
    }
    
    @Override
    public int getAnimationTime(final StateKey stateKey) {
        final State state = this.getState(stateKey);
        if (state != null) {
            return (int)Math.min(2147483647L, this.getCurrentTime() - state.lastChangedTime);
        }
        if (this.parent != null) {
            return this.parent.getAnimationTime(stateKey);
        }
        return (int)this.getCurrentTime() & Integer.MAX_VALUE;
    }
    
    @Override
    public boolean getAnimationState(final StateKey stateKey) {
        final State state = this.getState(stateKey);
        if (state != null) {
            return state.active;
        }
        return this.parent != null && this.parent.getAnimationState(stateKey);
    }
    
    @Override
    public boolean getShouldAnimateState(final StateKey stateKey) {
        final State state = this.getState(stateKey);
        if (state != null) {
            return state.shouldAnimate;
        }
        return this.parent != null && this.parent.getShouldAnimateState(stateKey);
    }
    
    @Deprecated
    public void setAnimationState(final String stateName, final boolean active) {
        this.setAnimationState(StateKey.get(stateName), active);
    }
    
    public void setAnimationState(final StateKey stateKey, final boolean active) {
        final State state = this.getOrCreate(stateKey);
        if (state.active != active) {
            state.active = active;
            state.lastChangedTime = this.getCurrentTime();
            state.shouldAnimate = true;
        }
    }
    
    @Deprecated
    public void resetAnimationTime(final String stateName) {
        this.resetAnimationTime(StateKey.get(stateName));
    }
    
    public void resetAnimationTime(final StateKey stateKey) {
        final State state = this.getOrCreate(stateKey);
        state.lastChangedTime = this.getCurrentTime();
        state.shouldAnimate = true;
    }
    
    @Deprecated
    public void dontAnimate(final String stateName) {
        this.dontAnimate(StateKey.get(stateName));
    }
    
    public void dontAnimate(final StateKey stateKey) {
        final State state = this.getState(stateKey);
        if (state != null) {
            state.shouldAnimate = false;
        }
    }
    
    private State getState(final StateKey stateKey) {
        final int id = stateKey.getID();
        if (id < this.stateTable.length) {
            return this.stateTable[id];
        }
        return null;
    }
    
    private State getOrCreate(final StateKey stateKey) {
        final int id = stateKey.getID();
        if (id < this.stateTable.length) {
            final State state = this.stateTable[id];
            if (state != null) {
                return state;
            }
        }
        return this.createState(id);
    }
    
    private State createState(final int id) {
        if (id >= this.stateTable.length) {
            final State[] newTable = new State[id + 1];
            System.arraycopy(this.stateTable, 0, newTable, 0, this.stateTable.length);
            this.stateTable = newTable;
        }
        final State state = new State();
        state.lastChangedTime = this.getCurrentTime();
        return this.stateTable[id] = state;
    }
    
    private long getCurrentTime() {
        return (this.gui != null) ? this.gui.curTime : 0L;
    }
    
    static final class State
    {
        long lastChangedTime;
        boolean active;
        boolean shouldAnimate;
    }
}
