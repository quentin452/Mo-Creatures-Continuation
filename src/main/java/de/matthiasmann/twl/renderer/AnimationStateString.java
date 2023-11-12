package de.matthiasmann.twl.renderer;

import de.matthiasmann.twl.*;

public class AnimationStateString implements AnimationState {
    String stateKey;

    public AnimationStateString(final String key) {
        this.stateKey = key;
    }
    // todo
    @Override
    public int getAnimationTime(StateKey p0) {
        return 0;
    }

    public boolean getAnimationState(final AnimationState.StateKey stateKey) {
        return stateKey.getName().contains(this.stateKey);
    }
   // todo
    @Override
    public boolean getShouldAnimateState(StateKey p0) {
        return false;
    }
}
