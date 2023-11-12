package de.matthiasmann.twl.model;

import de.matthiasmann.twl.renderer.*;

public abstract class AbstractTableColumnHeaderModel implements TableColumnHeaderModel
{
    private static final AnimationState.StateKey[] EMPTY_STATE_ARRAY;
    
    @Override
    public AnimationState.StateKey[] getColumnHeaderStates() {
        return AbstractTableColumnHeaderModel.EMPTY_STATE_ARRAY;
    }
    
    @Override
    public boolean getColumnHeaderState(final int column, final int stateIdx) {
        return false;
    }
    
    static {
        EMPTY_STATE_ARRAY = new AnimationState.StateKey[0];
    }
}
