package de.matthiasmann.twl.utils;

import de.matthiasmann.twl.renderer.*;
import java.util.*;

public class StateSelect
{
    private static boolean useOptimizer;
    private final StateExpression[] expressions;
    private final AnimationState.StateKey[] programKeys;
    private final short[] programCodes;
    public static final StateSelect EMPTY;
    static final int CODE_RESULT = 32768;
    static final int CODE_MASK = 32767;
    
    public StateSelect(final Collection<StateExpression> expressions) {
        this((StateExpression[])expressions.toArray(new StateExpression[expressions.size()]));
    }
    
    public StateSelect(final StateExpression... expressions) {
        this.expressions = expressions;
        final StateSelectOptimizer sso = StateSelect.useOptimizer ? StateSelectOptimizer.optimize(expressions) : null;
        if (sso != null) {
            this.programKeys = sso.programKeys;
            this.programCodes = sso.programCodes;
        }
        else {
            this.programKeys = null;
            this.programCodes = null;
        }
    }
    
    public static boolean isUseOptimizer() {
        return StateSelect.useOptimizer;
    }
    
    public static void setUseOptimizer(final boolean useOptimizer) {
        StateSelect.useOptimizer = useOptimizer;
    }
    
    public int getNumExpressions() {
        return this.expressions.length;
    }
    
    public StateExpression getExpression(final int idx) {
        return this.expressions[idx];
    }
    
    public int evaluate(final AnimationState as) {
        if (this.programKeys != null) {
            return this.evaluateProgram(as);
        }
        return this.evaluateExpr(as);
    }
    
    private int evaluateExpr(final AnimationState as) {
        int i = 0;
        for (int n = this.expressions.length; i < n && !this.expressions[i].evaluate(as); ++i) {}
        return i;
    }
    
    private int evaluateProgram(final AnimationState as) {
        int pos = 0;
        do {
            if (as == null || !as.getAnimationState(this.programKeys[pos >> 1])) {
                ++pos;
            }
            pos = this.programCodes[pos];
        } while (pos >= 0);
        return pos & 0x7FFF;
    }
    
    static {
        StateSelect.useOptimizer = false;
        EMPTY = new StateSelect(new StateExpression[0]);
    }
}
