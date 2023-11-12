package de.matthiasmann.twl.utils;

import de.matthiasmann.twl.renderer.*;
import java.util.*;

final class StateSelectOptimizer
{
    private final AnimationState.StateKey[] keys;
    private final byte[] matrix;
    final AnimationState.StateKey[] programKeys;
    final short[] programCodes;
    int programIdx;
    
    static StateSelectOptimizer optimize(final StateExpression... expressions) {
        final int numExpr = expressions.length;
        if (numExpr == 0 || numExpr >= 255) {
            return null;
        }
        final BitSet bs = new BitSet();
        for (final StateExpression e : expressions) {
            e.getUsedStateKeys(bs);
        }
        final int numKeys = bs.cardinality();
        if (numKeys == 0 || numKeys > 16) {
            return null;
        }
        final AnimationState.StateKey[] keys = new AnimationState.StateKey[numKeys];
        int keyIdx = 0;
        int keyID = -1;
        while ((keyID = bs.nextSetBit(keyID + 1)) >= 0) {
            keys[keyIdx] = AnimationState.StateKey.get(keyID);
            ++keyIdx;
        }
        final int matrixSize = 1 << numKeys;
        final byte[] matrix = new byte[matrixSize];
        final de.matthiasmann.twl.AnimationState as = new de.matthiasmann.twl.AnimationState((de.matthiasmann.twl.AnimationState)null, keys[numKeys - 1].getID() + 1);
        for (int matrixIdx = 0; matrixIdx < matrixSize; ++matrixIdx) {
            for (int keyIdx2 = 0; keyIdx2 < numKeys; ++keyIdx2) {
                as.setAnimationState(keys[keyIdx2], (matrixIdx & 1 << keyIdx2) != 0x0);
            }
            int exprIdx;
            for (exprIdx = 0; exprIdx < numExpr && !expressions[exprIdx].evaluate((AnimationState)as); ++exprIdx) {}
            matrix[matrixIdx] = (byte)exprIdx;
        }
        final StateSelectOptimizer sso = new StateSelectOptimizer(keys, matrix);
        sso.compute(0, 0);
        return sso;
    }
    
    private StateSelectOptimizer(final AnimationState.StateKey[] keys, final byte[] matrix) {
        this.keys = keys;
        this.matrix = matrix;
        this.programKeys = new AnimationState.StateKey[matrix.length - 1];
        this.programCodes = new short[matrix.length * 2 - 2];
    }
    
    private int compute(final int bits, int mask) {
        if (mask == this.matrix.length - 1) {
            return (this.matrix[bits] & 0xFF) | 0x8000;
        }
        int best = -1;
        int bestScore = -1;
        int bestSet0 = 0;
        int bestSet2 = 0;
        final int matrixIdxInc = (bits == 0) ? 1 : Integer.lowestOneBit(bits);
        for (int keyIdx = 0; keyIdx < this.keys.length; ++keyIdx) {
            final int test = 1 << keyIdx;
            if ((mask & test) == 0x0) {
                int set0 = 0;
                int set2 = 0;
                for (int matrixIdx = bits; matrixIdx < this.matrix.length; matrixIdx += matrixIdxInc) {
                    if ((matrixIdx & mask) == bits) {
                        final int resultMask = 1 << (this.matrix[matrixIdx] & 0xFF);
                        if ((matrixIdx & test) == 0x0) {
                            set0 |= resultMask;
                        }
                        else {
                            set2 |= resultMask;
                        }
                    }
                }
                final int score = Integer.bitCount(set0 ^ set2);
                if (score > bestScore) {
                    bestScore = score;
                    bestSet0 = set0;
                    bestSet2 = set2;
                    best = keyIdx;
                }
            }
        }
        if (best < 0) {
            throw new AssertionError();
        }
        if (bestSet0 == bestSet2 && (bestSet0 & bestSet0 - 1) == 0x0) {
            final int result = Integer.numberOfTrailingZeros(bestSet0);
            return result | 0x8000;
        }
        final int bestMask = 1 << best;
        mask |= bestMask;
        final int idx = this.programIdx;
        this.programIdx += 2;
        this.programKeys[idx >> 1] = this.keys[best];
        this.programCodes[idx + 0] = (short)this.compute(bits | bestMask, mask);
        this.programCodes[idx + 1] = (short)this.compute(bits, mask);
        return idx;
    }
}
