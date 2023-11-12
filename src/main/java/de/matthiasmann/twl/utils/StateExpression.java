package de.matthiasmann.twl.utils;

import de.matthiasmann.twl.renderer.*;
import java.text.*;
import java.util.*;

public abstract class StateExpression
{
    boolean negate;
    
    public abstract boolean evaluate(final AnimationState p0);
    
    public static StateExpression parse(final String exp, final boolean negate) throws ParseException {
        final StringIterator si = new StringIterator(exp);
        final StateExpression expr = parse(si);
        if (si.hasMore()) {
            si.unexpected();
        }
        final StateExpression stateExpression = expr;
        stateExpression.negate ^= negate;
        return expr;
    }
    
    private static StateExpression parse(final StringIterator si) throws ParseException {
        final ArrayList<StateExpression> children = new ArrayList<StateExpression>();
        char kind = ' ';
        while (true) {
            if (!si.skipSpaces()) {
                si.unexpected();
            }
            char ch = si.peek();
            final boolean negate = ch == '!';
            if (negate) {
                ++si.pos;
                if (!si.skipSpaces()) {
                    si.unexpected();
                }
                ch = si.peek();
            }
            StateExpression child = null;
            if (Character.isJavaIdentifierStart(ch)) {
                child = new Check(AnimationState.StateKey.get(si.getIdent()));
            }
            else if (ch == '(') {
                ++si.pos;
                child = parse(si);
                si.expect(')');
            }
            else {
                if (ch == ')') {
                    break;
                }
                si.unexpected();
            }
            child.negate = negate;
            children.add(child);
            if (!si.skipSpaces()) {
                break;
            }
            ch = si.peek();
            if ("|+^".indexOf(ch) < 0) {
                break;
            }
            if (children.size() == 1) {
                kind = ch;
            }
            else if (kind != ch) {
                si.expect(kind);
            }
            ++si.pos;
        }
        if (children.isEmpty()) {
            si.unexpected();
        }
        assert children.size() == 1;
        if (children.size() == 1) {
            return children.get(0);
        }
        return new Logic(kind, (StateExpression[])children.toArray(new StateExpression[children.size()]));
    }
    
    StateExpression() {
    }
    
    abstract void getUsedStateKeys(final BitSet p0);
    
    private static class StringIterator
    {
        final String str;
        int pos;
        
        StringIterator(final String str) {
            this.str = str;
        }
        
        boolean hasMore() {
            return this.pos < this.str.length();
        }
        
        char peek() {
            return this.str.charAt(this.pos);
        }
        
        void expect(final char what) throws ParseException {
            if (!this.hasMore() || this.peek() != what) {
                throw new ParseException("Expected '" + what + "' got " + this.describePosition(), this.pos);
            }
            ++this.pos;
        }
        
        void unexpected() throws ParseException {
            throw new ParseException("Unexpected " + this.describePosition(), this.pos);
        }
        
        String describePosition() {
            if (this.pos >= this.str.length()) {
                return "end of expression";
            }
            return "'" + this.peek() + "' at " + (this.pos + 1);
        }
        
        boolean skipSpaces() {
            while (this.hasMore() && Character.isWhitespace(this.peek())) {
                ++this.pos;
            }
            return this.hasMore();
        }
        
        String getIdent() {
            final int start = this.pos;
            while (this.hasMore() && Character.isJavaIdentifierPart(this.peek())) {
                ++this.pos;
            }
            return this.str.substring(start, this.pos).intern();
        }
    }
    
    public static class Logic extends StateExpression
    {
        final StateExpression[] children;
        final boolean and;
        final boolean xor;
        
        public Logic(final char kind, final StateExpression... children) {
            if (kind != '|' && kind != '+' && kind != '^') {
                throw new IllegalArgumentException("kind");
            }
            this.children = children;
            this.and = (kind == '+');
            this.xor = (kind == '^');
        }
        
        @Override
        public boolean evaluate(final AnimationState as) {
            boolean result = this.and ^ this.negate;
            for (final StateExpression e : this.children) {
                final boolean value = e.evaluate(as);
                if (this.xor) {
                    result ^= value;
                }
                else if (this.and != value) {
                    return result ^ true;
                }
            }
            return result;
        }
        
        @Override
        void getUsedStateKeys(final BitSet bs) {
            for (final StateExpression e : this.children) {
                e.getUsedStateKeys(bs);
            }
        }
    }
    
    public static class Check extends StateExpression
    {
        final AnimationState.StateKey state;
        
        public Check(final AnimationState.StateKey state) {
            this.state = state;
        }
        
        @Override
        public boolean evaluate(final AnimationState as) {
            return this.negate ^ (as != null && as.getAnimationState(this.state));
        }
        
        @Override
        void getUsedStateKeys(final BitSet bs) {
            bs.set(this.state.getID());
        }
    }
}
