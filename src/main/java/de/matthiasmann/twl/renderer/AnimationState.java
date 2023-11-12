package de.matthiasmann.twl.renderer;

import java.util.*;

public interface AnimationState
{
    int getAnimationTime(final StateKey p0);
    
    boolean getAnimationState(final StateKey p0);
    
    boolean getShouldAnimateState(final StateKey p0);
    
    public static final class StateKey
    {
        private final String name;
        private final int id;
        private static final HashMap<String, StateKey> keys;
        private static final ArrayList<StateKey> keysByID;
        
        private StateKey(final String name, final int id) {
            this.name = name;
            this.id = id;
        }
        
        public String getName() {
            return this.name;
        }
        
        public int getID() {
            return this.id;
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof StateKey) {
                final StateKey other = (StateKey)obj;
                return this.id == other.id;
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return this.id;
        }
        
        public static synchronized StateKey get(final String name) {
            if (name.length() == 0) {
                throw new IllegalArgumentException("name");
            }
            StateKey key = StateKey.keys.get(name);
            if (key == null) {
                key = new StateKey(name, StateKey.keys.size());
                StateKey.keys.put(name, key);
                StateKey.keysByID.add(key);
            }
            return key;
        }
        
        public static synchronized StateKey get(final int id) {
            return StateKey.keysByID.get(id);
        }
        
        public static synchronized int getNumStateKeys() {
            return StateKey.keys.size();
        }
        
        static {
            keys = new HashMap<String, StateKey>();
            keysByID = new ArrayList<StateKey>();
        }
    }
}
