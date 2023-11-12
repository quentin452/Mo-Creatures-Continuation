package de.matthiasmann.twl.textarea;

import java.io.*;

class Parser
{
    public static final int YYEOF = -1;
    private static final int ZZ_BUFFERSIZE = 16384;
    public static final int YYSTRING1 = 6;
    public static final int YYINITIAL = 0;
    public static final int YYSTYLE = 2;
    public static final int YYVALUE = 4;
    public static final int YYSTRING2 = 8;
    private static final String ZZ_CMAP_PACKED = "\t\u0000\u0001\u0003\u0001\u0002\u0001\u0000\u0001\u0003\u0001\u0001\u0012\u0000\u0001\u0003\u0001\u0000\u0001\u0013\u0001\f\u0003\u0000\u0001\u0012\u0002\u0000\u0001\u0005\u0001\u0000\u0001\n\u0001\u0006\u0001\t\u0001\u0004\n\b\u0001\r\u0001\u0011\u0002\u0000\u0001\u000b\u0001\u0000\u0001\u000e\u001a\u0007\u0004\u0000\u0001\u0007\u0001\u0000\u001a\u0007\u0001\u000f\u0001\u0000\u0001\u0010\uff82\u0000";
    private static final char[] ZZ_CMAP;
    private static final int[] ZZ_ACTION;
    private static final String ZZ_ACTION_PACKED_0 = "\u0005\u0000\u0001\u0001\u0001\u0002\u0001\u0001\u0001\u0003\u0001\u0001\u0001\u0004\u0001\u0005\u0001\u0006\u0001\u0007\u0001\b\u0001\t\u0001\n\u0001\u000b\u0002\f\u0001\u0001\u0001\r\u0001\u000e\u0001\u000f\u0001\u0010\u0001\u0011\u0001\u0012\u0001\u0013\u0001\u0010\u0001\u0014\u0001\u0010\u0001\u0015\u0004\u0000";
    private static final int[] ZZ_ROWMAP;
    private static final String ZZ_ROWMAP_PACKED_0 = "\u0000\u0000\u0000\u0014\u0000(\u0000<\u0000P\u0000d\u0000x\u0000\u008c\u0000d\u0000�\u0000�\u0000d\u0000d\u0000d\u0000d\u0000d\u0000d\u0000d\u0000\u00c8\u0000d\u0000\u00dc\u0000\u00f0\u0000d\u0000d\u0000\u0104\u0000d\u0000d\u0000d\u0000\u0118\u0000d\u0000\u012c\u0000d\u0000\u0140\u0000\u0154\u0000\u0168\u0000\u017c";
    private static final int[] ZZ_TRANS;
    private static final String ZZ_TRANS_PACKED_0 = "\u0001\u0006\u0003\u0007\u0001\b\u0001\t\u0001\n\u0001\u000b\u0001\u0006\u0001\f\u0001\r\u0001\u000e\u0001\u000f\u0001\u0010\u0001\u0011\u0001\u0012\u0005\u0006\u0001\u0013\u0002\u0014\u0001\b\u0001\u0006\u0001\u0015\u0001\u0016\u0005\u0006\u0001\u0017\u0002\u0006\u0001\u0018\u0003\u0006\u0010\u0019\u0001\u0018\u0001\u001a\u0001\u001b\u0001\u001c\u0012\u001d\u0001\u001e\u0001\u001d\u0013\u001f\u0001 \u0015\u0000\u0003\u0007\u0015\u0000\u0001!\u0015\u0000\u0001\u000b\u0012\u0000\u0003\u000b\r\u0000\u0001\u0014\u0018\u0000\u0001\u0016\u0012\u0000\u0003\u0016\u000b\u0000\u0010\u0019\u0004\u0000\u0012\u001d\u0001\u0000\u0001\u001d\u0013\u001f\u0001\u0000\u0005\"\u0001#\u0013\"\u0001$\u000e\"\u0004\u0000\u0001\u0014\u0001#\u000e\u0000\u0004\"\u0001\u0014\u0001$\u000e\"";
    private static final int[] ZZ_ATTRIBUTE;
    private static final String ZZ_ATTRIBUTE_PACKED_0 = "\u0005\u0000\u0001\t\u0002\u0001\u0001\t\u0002\u0001\u0007\t\u0001\u0001\u0001\t\u0002\u0001\u0002\t\u0001\u0001\u0003\t\u0001\u0001\u0001\t\u0001\u0001\u0001\t\u0004\u0000";
    private Reader zzReader;
    private int zzState;
    private int zzLexicalState;
    private char[] zzBuffer;
    private int zzMarkedPos;
    private int zzCurrentPos;
    private int zzStartRead;
    private int zzEndRead;
    private int yyline;
    private int yycolumn;
    private boolean zzAtEOF;
    static final int EOF = 0;
    static final int IDENT = 1;
    static final int STAR = 2;
    static final int DOT = 3;
    static final int HASH = 4;
    static final int GT = 5;
    static final int COMMA = 6;
    static final int STYLE_BEGIN = 7;
    static final int STYLE_END = 8;
    static final int COLON = 9;
    static final int SEMICOLON = 10;
    static final int ATRULE = 11;
    boolean sawWhitespace;
    final StringBuilder sb;
    
    private static int[] zzUnpackAction() {
        final int[] result = new int[36];
        zzUnpackAction("\u0005\u0000\u0001\u0001\u0001\u0002\u0001\u0001\u0001\u0003\u0001\u0001\u0001\u0004\u0001\u0005\u0001\u0006\u0001\u0007\u0001\b\u0001\t\u0001\n\u0001\u000b\u0002\f\u0001\u0001\u0001\r\u0001\u000e\u0001\u000f\u0001\u0010\u0001\u0011\u0001\u0012\u0001\u0013\u0001\u0010\u0001\u0014\u0001\u0010\u0001\u0015\u0004\u0000", 0, result);
        return result;
    }
    
    private static int zzUnpackAction(final String packed, final int offset, final int[] result) {
        int i = 0;
        int j = offset;
        final int l = packed.length();
        while (i < l) {
            int count = packed.charAt(i++);
            final int value = packed.charAt(i++);
            do {
                result[j++] = value;
            } while (--count > 0);
        }
        return j;
    }
    
    private static int[] zzUnpackRowMap() {
        final int[] result = new int[36];
        zzUnpackRowMap("\u0000\u0000\u0000\u0014\u0000(\u0000<\u0000P\u0000d\u0000x\u0000\u008c\u0000d\u0000�\u0000�\u0000d\u0000d\u0000d\u0000d\u0000d\u0000d\u0000d\u0000\u00c8\u0000d\u0000\u00dc\u0000\u00f0\u0000d\u0000d\u0000\u0104\u0000d\u0000d\u0000d\u0000\u0118\u0000d\u0000\u012c\u0000d\u0000\u0140\u0000\u0154\u0000\u0168\u0000\u017c", 0, result);
        return result;
    }
    
    private static int zzUnpackRowMap(final String packed, final int offset, final int[] result) {
        int i = 0;
        int j = offset;
        int high;
        for (int l = packed.length(); i < l; high = packed.charAt(i++) << 16, result[j++] = (high | packed.charAt(i++))) {}
        return j;
    }
    
    private static int[] zzUnpackTrans() {
        final int[] result = new int[400];
        zzUnpackTrans("\u0001\u0006\u0003\u0007\u0001\b\u0001\t\u0001\n\u0001\u000b\u0001\u0006\u0001\f\u0001\r\u0001\u000e\u0001\u000f\u0001\u0010\u0001\u0011\u0001\u0012\u0005\u0006\u0001\u0013\u0002\u0014\u0001\b\u0001\u0006\u0001\u0015\u0001\u0016\u0005\u0006\u0001\u0017\u0002\u0006\u0001\u0018\u0003\u0006\u0010\u0019\u0001\u0018\u0001\u001a\u0001\u001b\u0001\u001c\u0012\u001d\u0001\u001e\u0001\u001d\u0013\u001f\u0001 \u0015\u0000\u0003\u0007\u0015\u0000\u0001!\u0015\u0000\u0001\u000b\u0012\u0000\u0003\u000b\r\u0000\u0001\u0014\u0018\u0000\u0001\u0016\u0012\u0000\u0003\u0016\u000b\u0000\u0010\u0019\u0004\u0000\u0012\u001d\u0001\u0000\u0001\u001d\u0013\u001f\u0001\u0000\u0005\"\u0001#\u0013\"\u0001$\u000e\"\u0004\u0000\u0001\u0014\u0001#\u000e\u0000\u0004\"\u0001\u0014\u0001$\u000e\"", 0, result);
        return result;
    }
    
    private static int zzUnpackTrans(final String packed, final int offset, final int[] result) {
        int i = 0;
        int j = offset;
        final int l = packed.length();
        while (i < l) {
            int count = packed.charAt(i++);
            int value = packed.charAt(i++);
            --value;
            do {
                result[j++] = value;
            } while (--count > 0);
        }
        return j;
    }
    
    private static int[] zzUnpackAttribute() {
        final int[] result = new int[36];
        zzUnpackAttribute("\u0005\u0000\u0001\t\u0002\u0001\u0001\t\u0002\u0001\u0007\t\u0001\u0001\u0001\t\u0002\u0001\u0002\t\u0001\u0001\u0003\t\u0001\u0001\u0001\t\u0001\u0001\u0001\t\u0004\u0000", 0, result);
        return result;
    }
    
    private static int zzUnpackAttribute(final String packed, final int offset, final int[] result) {
        int i = 0;
        int j = offset;
        final int l = packed.length();
        while (i < l) {
            int count = packed.charAt(i++);
            final int value = packed.charAt(i++);
            do {
                result[j++] = value;
            } while (--count > 0);
        }
        return j;
    }
    
    private void append() {
        this.sb.append(this.zzBuffer, this.zzStartRead, this.zzMarkedPos - this.zzStartRead);
    }
    
    public void unexpected() throws IOException {
        throw new IOException("Unexpected \"" + this.yytext() + "\" at line " + this.yyline + ", column " + this.yycolumn);
    }
    
    public void expect(final int token) throws IOException {
        if (this.yylex() != token) {
            this.unexpected();
        }
    }
    
    Parser(final Reader in) {
        this.zzLexicalState = 0;
        this.zzBuffer = new char[16384];
        this.sb = new StringBuilder();
        this.zzReader = in;
    }
    
    private static char[] zzUnpackCMap(final String packed) {
        final char[] map = new char[65536];
        int i = 0;
        int j = 0;
        while (i < 72) {
            int count = packed.charAt(i++);
            final char value = packed.charAt(i++);
            do {
                map[j++] = value;
            } while (--count > 0);
        }
        return map;
    }
    
    private boolean zzRefill() throws IOException {
        if (this.zzStartRead > 0) {
            System.arraycopy(this.zzBuffer, this.zzStartRead, this.zzBuffer, 0, this.zzEndRead - this.zzStartRead);
            this.zzEndRead -= this.zzStartRead;
            this.zzCurrentPos -= this.zzStartRead;
            this.zzMarkedPos -= this.zzStartRead;
            this.zzStartRead = 0;
        }
        if (this.zzCurrentPos >= this.zzBuffer.length) {
            final char[] newBuffer = new char[this.zzCurrentPos * 2];
            System.arraycopy(this.zzBuffer, 0, newBuffer, 0, this.zzBuffer.length);
            this.zzBuffer = newBuffer;
        }
        final int numRead = this.zzReader.read(this.zzBuffer, this.zzEndRead, this.zzBuffer.length - this.zzEndRead);
        if (numRead > 0) {
            this.zzEndRead += numRead;
            return false;
        }
        if (numRead != 0) {
            return true;
        }
        final int c = this.zzReader.read();
        if (c == -1) {
            return true;
        }
        this.zzBuffer[this.zzEndRead++] = (char)c;
        return false;
    }
    
    public final void yybegin(final int newState) {
        this.zzLexicalState = newState;
    }
    
    public final String yytext() {
        return new String(this.zzBuffer, this.zzStartRead, this.zzMarkedPos - this.zzStartRead);
    }
    
    private void zzScanError(final String message) {
        throw new Error(message);
    }
    
    public int yylex() throws IOException {
        int zzEndReadL = this.zzEndRead;
        char[] zzBufferL = this.zzBuffer;
        final char[] zzCMapL = Parser.ZZ_CMAP;
        final int[] zzTransL = Parser.ZZ_TRANS;
        final int[] zzRowMapL = Parser.ZZ_ROWMAP;
        final int[] zzAttrL = Parser.ZZ_ATTRIBUTE;
        while (true) {
            int zzMarkedPosL = this.zzMarkedPos;
            boolean zzR = false;
            for (int zzCurrentPosL = this.zzStartRead; zzCurrentPosL < zzMarkedPosL; ++zzCurrentPosL) {
                switch (zzBufferL[zzCurrentPosL]) {
                    case '\u000b':
                    case '\f':
                    case '\u0085':
                    case '\u2028':
                    case '\u2029': {
                        ++this.yyline;
                        this.yycolumn = 0;
                        zzR = false;
                        break;
                    }
                    case '\r': {
                        ++this.yyline;
                        this.yycolumn = 0;
                        zzR = true;
                        break;
                    }
                    case '\n': {
                        if (zzR) {
                            zzR = false;
                            break;
                        }
                        ++this.yyline;
                        this.yycolumn = 0;
                        break;
                    }
                    default: {
                        zzR = false;
                        ++this.yycolumn;
                        break;
                    }
                }
            }
            if (zzR) {
                boolean zzPeek;
                if (zzMarkedPosL < zzEndReadL) {
                    zzPeek = (zzBufferL[zzMarkedPosL] == '\n');
                }
                else if (this.zzAtEOF) {
                    zzPeek = false;
                }
                else {
                    final boolean eof = this.zzRefill();
                    zzEndReadL = this.zzEndRead;
                    zzMarkedPosL = this.zzMarkedPos;
                    zzBufferL = this.zzBuffer;
                    zzPeek = (!eof && zzBufferL[zzMarkedPosL] == '\n');
                }
                if (zzPeek) {
                    --this.yyline;
                }
            }
            int zzAction = -1;
            final int n = zzMarkedPosL;
            this.zzStartRead = n;
            this.zzCurrentPos = n;
            int zzCurrentPosL = n;
            this.zzState = this.zzLexicalState / 2;
            int zzInput;
            while (true) {
                if (zzCurrentPosL < zzEndReadL) {
                    zzInput = zzBufferL[zzCurrentPosL++];
                }
                else {
                    if (this.zzAtEOF) {
                        zzInput = -1;
                        break;
                    }
                    this.zzCurrentPos = zzCurrentPosL;
                    this.zzMarkedPos = zzMarkedPosL;
                    final boolean eof2 = this.zzRefill();
                    zzCurrentPosL = this.zzCurrentPos;
                    zzMarkedPosL = this.zzMarkedPos;
                    zzBufferL = this.zzBuffer;
                    zzEndReadL = this.zzEndRead;
                    if (eof2) {
                        zzInput = -1;
                        break;
                    }
                    zzInput = zzBufferL[zzCurrentPosL++];
                }
                final int zzNext = zzTransL[zzRowMapL[this.zzState] + zzCMapL[zzInput]];
                if (zzNext == -1) {
                    break;
                }
                this.zzState = zzNext;
                final int zzAttributes = zzAttrL[this.zzState];
                if ((zzAttributes & 0x1) != 0x1) {
                    continue;
                }
                zzAction = this.zzState;
                zzMarkedPosL = zzCurrentPosL;
                if ((zzAttributes & 0x8) == 0x8) {
                    break;
                }
            }
            this.zzMarkedPos = zzMarkedPosL;
            switch ((zzAction < 0) ? zzAction : Parser.ZZ_ACTION[zzAction]) {
                case 6: {
                    return 6;
                }
                case 22: {
                    continue;
                }
                case 20: {
                    this.yybegin(4);
                    this.sb.append('\'');
                }
                case 23: {
                    continue;
                }
                case 10: {
                    return 11;
                }
                case 24: {
                    continue;
                }
                case 3: {
                    this.sawWhitespace = false;
                    return 2;
                }
                case 25: {
                    continue;
                }
                case 18: {
                    this.yybegin(6);
                    this.sb.append('\'');
                }
                case 26: {
                    continue;
                }
                case 19: {
                    this.yybegin(8);
                    this.sb.append('\"');
                }
                case 27: {
                    continue;
                }
                case 16: {
                    this.append();
                }
                case 28: {
                    continue;
                }
                case 4: {
                    this.sawWhitespace = false;
                    return 1;
                }
                case 29: {
                    continue;
                }
                case 21: {
                    this.yybegin(4);
                    this.sb.append('\"');
                }
                case 30: {
                    continue;
                }
                case 9: {
                    return 9;
                }
                case 31: {
                    continue;
                }
                case 2: {
                    this.sawWhitespace = true;
                }
                case 32: {
                    continue;
                }
                case 15: {
                    this.yybegin(0);
                    return 8;
                }
                case 33: {
                    continue;
                }
                case 17: {
                    this.yybegin(2);
                    return 10;
                }
                case 34: {
                    continue;
                }
                case 14: {
                    this.yybegin(4);
                    this.sb.setLength(0);
                    return 9;
                }
                case 35: {
                    continue;
                }
                case 7: {
                    return 5;
                }
                case 36: {
                    continue;
                }
                case 11: {
                    this.yybegin(2);
                    return 7;
                }
                case 37: {
                    continue;
                }
                case 13: {
                    return 1;
                }
                case 38: {
                    continue;
                }
                case 1: {
                    this.unexpected();
                }
                case 39: {
                    continue;
                }
                case 5: {
                    return 3;
                }
                case 40: {
                    continue;
                }
                case 8: {
                    return 4;
                }
                case 41: {
                    continue;
                }
                case 12:
                case 42: {
                    continue;
                }
                default: {
                    if (zzInput == -1 && this.zzStartRead == this.zzCurrentPos) {
                        this.zzAtEOF = true;
                        return 0;
                    }
                    this.zzScanError("Error: could not match input");
                    continue;
                }
            }
        }
    }
    
    static {
        ZZ_CMAP = zzUnpackCMap("\t\u0000\u0001\u0003\u0001\u0002\u0001\u0000\u0001\u0003\u0001\u0001\u0012\u0000\u0001\u0003\u0001\u0000\u0001\u0013\u0001\f\u0003\u0000\u0001\u0012\u0002\u0000\u0001\u0005\u0001\u0000\u0001\n\u0001\u0006\u0001\t\u0001\u0004\n\b\u0001\r\u0001\u0011\u0002\u0000\u0001\u000b\u0001\u0000\u0001\u000e\u001a\u0007\u0004\u0000\u0001\u0007\u0001\u0000\u001a\u0007\u0001\u000f\u0001\u0000\u0001\u0010\uff82\u0000");
        ZZ_ACTION = zzUnpackAction();
        ZZ_ROWMAP = zzUnpackRowMap();
        ZZ_TRANS = zzUnpackTrans();
        ZZ_ATTRIBUTE = zzUnpackAttribute();
    }
}
