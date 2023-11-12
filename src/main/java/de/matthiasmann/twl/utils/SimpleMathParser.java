package de.matthiasmann.twl.utils;

import java.text.*;

public class SimpleMathParser
{
    final String str;
    final Interpreter interpreter;
    int pos;
    
    private SimpleMathParser(final String str, final Interpreter interpreter) {
        this.str = str;
        this.interpreter = interpreter;
    }
    
    public static void interpret(final String str, final Interpreter interpreter) throws ParseException {
        new SimpleMathParser(str, interpreter).parse(false);
    }
    
    public static int interpretArray(final String str, final Interpreter interpreter) throws ParseException {
        return new SimpleMathParser(str, interpreter).parse(true);
    }
    
    private int parse(final boolean allowArray) throws ParseException {
        try {
            if (this.peek() == -1) {
                if (allowArray) {
                    return 0;
                }
                this.unexpected(-1);
            }
            int count = 0;
            while (true) {
                ++count;
                this.parseAddSub();
                final int ch = this.peek();
                if (ch == -1) {
                    break;
                }
                if (ch != 44 || !allowArray) {
                    this.unexpected(ch);
                }
                ++this.pos;
            }
            return count;
        }
        catch (ParseException ex) {
            throw ex;
        }
        catch (Exception ex2) {
            throw (ParseException)new ParseException("Unable to execute", this.pos).initCause(ex2);
        }
    }
    
    private void parseAddSub() throws ParseException {
        this.parseMulDiv();
        while (true) {
            final int ch = this.peek();
            switch (ch) {
                case 43: {
                    ++this.pos;
                    this.parseMulDiv();
                    this.interpreter.add();
                    continue;
                }
                case 45: {
                    ++this.pos;
                    this.parseMulDiv();
                    this.interpreter.sub();
                    continue;
                }
                default: {}
            }
        }
    }
    
    private void parseMulDiv() throws ParseException {
        this.parseIdentOrConst();
        while (true) {
            final int ch = this.peek();
            switch (ch) {
                case 42: {
                    ++this.pos;
                    this.parseIdentOrConst();
                    this.interpreter.mul();
                    continue;
                }
                case 47: {
                    ++this.pos;
                    this.parseIdentOrConst();
                    this.interpreter.div();
                    continue;
                }
                default: {}
            }
        }
    }
    
    private void parseIdentOrConst() throws ParseException {
        int ch = this.peek();
        if (Character.isJavaIdentifierStart((char)ch)) {
            final String ident = this.parseIdent();
            ch = this.peek();
            if (ch == 40) {
                ++this.pos;
                this.parseCall(ident);
                return;
            }
            this.interpreter.accessVariable(ident);
            while (ch == 46 || ch == 91) {
                ++this.pos;
                if (ch == 46) {
                    final String field = this.parseIdent();
                    this.interpreter.accessField(field);
                }
                else {
                    this.parseIdentOrConst();
                    this.expect(93);
                    this.interpreter.accessArray();
                }
                ch = this.peek();
            }
        }
        else if (ch == 45) {
            ++this.pos;
            this.parseIdentOrConst();
            this.interpreter.negate();
        }
        else if (ch == 46 || ch == 43 || Character.isDigit((char)ch)) {
            this.parseConst();
        }
        else if (ch == 40) {
            ++this.pos;
            this.parseAddSub();
            this.expect(41);
        }
    }
    
    private void parseCall(final String name) throws ParseException {
        int count = 1;
        this.parseAddSub();
        while (true) {
            final int ch = this.peek();
            if (ch == 41) {
                break;
            }
            if (ch == 44) {
                ++this.pos;
                ++count;
                this.parseAddSub();
            }
            else {
                this.unexpected(ch);
            }
        }
        ++this.pos;
        this.interpreter.callFunction(name, count);
    }
    
    private void parseConst() throws ParseException {
        final int len = this.str.length();
        int start = this.pos;
        switch (this.str.charAt(this.pos)) {
            case '+': {
                start = ++this.pos;
                break;
            }
            case '0': {
                if (this.pos + 1 < len && this.str.charAt(this.pos + 1) == 'x') {
                    this.pos += 2;
                    this.parseHexInt();
                    return;
                }
                break;
            }
        }
        while (this.pos < len && Character.isDigit(this.str.charAt(this.pos))) {
            ++this.pos;
        }
        Number n;
        if (this.pos < len && this.str.charAt(this.pos) == '.') {
            ++this.pos;
            while (this.pos < len && Character.isDigit(this.str.charAt(this.pos))) {
                ++this.pos;
            }
            if (this.pos - start <= 1) {
                this.unexpected(-1);
            }
            n = Float.valueOf(this.str.substring(start, this.pos));
        }
        else {
            n = Integer.valueOf(this.str.substring(start, this.pos));
        }
        this.interpreter.loadConst(n);
    }
    
    private void parseHexInt() throws ParseException {
        final int len = this.str.length();
        final int start = this.pos;
        while (this.pos < len && "0123456789abcdefABCDEF".indexOf(this.str.charAt(this.pos)) >= 0) {
            ++this.pos;
        }
        if (this.pos - start > 8) {
            throw new ParseException("Number to large at " + this.pos, this.pos);
        }
        if (this.pos == start) {
            this.unexpected((this.pos < len) ? this.str.charAt(this.pos) : -1);
        }
        this.interpreter.loadConst((int)Long.parseLong(this.str.substring(start, this.pos), 16));
    }
    
    private boolean skipSpaces() {
        while (this.pos != this.str.length()) {
            if (!Character.isWhitespace(this.str.charAt(this.pos))) {
                return true;
            }
            ++this.pos;
        }
        return false;
    }
    
    private int peek() {
        if (this.skipSpaces()) {
            return this.str.charAt(this.pos);
        }
        return -1;
    }
    
    private String parseIdent() {
        final int start = this.pos;
        while (this.pos < this.str.length() && Character.isJavaIdentifierPart(this.str.charAt(this.pos))) {
            ++this.pos;
        }
        return this.str.substring(start, this.pos);
    }
    
    private void expect(final int what) throws ParseException {
        final int ch = this.peek();
        if (ch != what) {
            this.unexpected(ch);
        }
        else {
            ++this.pos;
        }
    }
    
    private void unexpected(final int ch) throws ParseException {
        if (ch < 0) {
            throw new ParseException("Unexpected end of string", this.pos);
        }
        throw new ParseException("Unexpected character '" + (char)ch + "' at " + this.pos, this.pos);
    }
    
    public interface Interpreter
    {
        void accessVariable(final String p0);
        
        void accessField(final String p0);
        
        void accessArray();
        
        void loadConst(final Number p0);
        
        void add();
        
        void sub();
        
        void mul();
        
        void div();
        
        void callFunction(final String p0, final int p1);
        
        void negate();
    }
}
