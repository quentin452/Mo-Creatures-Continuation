package de.matthiasmann.twl.utils;

public final class TextUtil
{
    private static final String ROMAN_NUMBERS = "\u2182M\u2182\u2181M\u2181MCMDCDCXCLXLXIXVIVI";
    private static final String ROMAN_VALUES = "\u2710\u2328\u1388\u0fa0\u03e8\u0384\u01f4\u0190dZ2(\n\t\u0005\u0004\u0001";
    public static final int MAX_ROMAN_INTEGER = 39999;
    
    private TextUtil() {
    }
    
    public static int countNumLines(final CharSequence str) {
        final int n = str.length();
        int count = 0;
        if (n > 0) {
            ++count;
            for (int i = 0; i < n; ++i) {
                if (str.charAt(i) == '\n') {
                    ++count;
                }
            }
        }
        return count;
    }
    
    public static String stripNewLines(final String str) {
        int idx = str.lastIndexOf(10);
        if (idx < 0) {
            return str;
        }
        final StringBuilder sb = new StringBuilder(str);
        do {
            if (sb.charAt(idx) == '\n') {
                sb.deleteCharAt(idx);
            }
        } while (--idx >= 0);
        return sb.toString();
    }
    
    public static String limitStringLength(final String str, final int length) {
        if (str.length() > length) {
            return str.substring(0, length);
        }
        return str;
    }
    
    public static String notNull(final String str) {
        if (str == null) {
            return "";
        }
        return str;
    }
    
    public static int indexOf(final CharSequence cs, final char ch, final int start) {
        return indexOf(cs, ch, start, cs.length());
    }
    
    public static int indexOf(final CharSequence cs, final char ch, int start, final int end) {
        while (start < end) {
            if (cs.charAt(start) == ch) {
                return start;
            }
            ++start;
        }
        return end;
    }
    
    public static int indexOf(final String str, final char ch, final int start) {
        final int idx = str.indexOf(ch, start);
        if (idx < 0) {
            return str.length();
        }
        return idx;
    }
    
    public static int skipSpaces(final CharSequence s, final int start) {
        return skipSpaces(s, start, s.length());
    }
    
    public static int skipSpaces(final CharSequence s, int start, final int end) {
        while (start < end && Character.isWhitespace(s.charAt(start))) {
            ++start;
        }
        return start;
    }
    
    public static String trim(final CharSequence s, final int start) {
        return trim(s, start, s.length());
    }
    
    public static String trim(final CharSequence s, int start, int end) {
        for (start = skipSpaces(s, start, end); end > start && Character.isWhitespace(s.charAt(end - 1)); --end) {}
        if (s instanceof String) {
            return ((String)s).substring(start, end);
        }
        if (s instanceof StringBuilder) {
            return ((StringBuilder)s).substring(start, end);
        }
        return s.subSequence(start, end).toString();
    }
    
    public static String createString(final char ch, final int len) {
        final char[] buf = new char[len];
        for (int i = 0; i < len; ++i) {
            buf[i] = ch;
        }
        return new String(buf);
    }
    
    public static int[] parseIntArray(final String str) throws NumberFormatException {
        final int count = countElements(str);
        final int[] result = new int[count];
        int idx = 0;
        int pos = 0;
        while (idx < count) {
            final int comma = indexOf(str, ',', pos);
            result[idx] = Integer.parseInt(str.substring(pos, comma));
            pos = comma + 1;
            ++idx;
        }
        return result;
    }
    
    public static int countElements(final String str) {
        int count = 0;
        for (int pos = 0; pos < str.length(); pos = indexOf(str, ',', pos) + 1) {
            ++count;
        }
        return count;
    }
    
    public static String toPrintableString(final char ch) {
        if (Character.isISOControl(ch)) {
            return '\\' + Integer.toOctalString(ch);
        }
        return Character.toString(ch);
    }
    
    public static String toRomanNumberString(int value) throws IllegalArgumentException {
        if (value < 1 || value > 39999) {
            throw new IllegalArgumentException();
        }
        final StringBuilder sb = new StringBuilder();
        int idxValues = 0;
        int idxNumbers = 0;
        do {
            final int romanValue = "\u2710\u2328\u1388\u0fa0\u03e8\u0384\u01f4\u0190dZ2(\n\t\u0005\u0004\u0001".charAt(idxValues);
            final int romanNumberLen = (idxValues & 0x1) + 1;
            while (value >= romanValue) {
                sb.append("\u2182M\u2182\u2181M\u2181MCMDCDCXCLXLXIXVIVI", idxNumbers, idxNumbers + romanNumberLen);
                value -= romanValue;
            }
            idxNumbers += romanNumberLen;
        } while (++idxValues < 17);
        return sb.toString();
    }
    
    public static String toCharListNumber(int value, final String list) throws IllegalArgumentException {
        if (value < 1) {
            throw new IllegalArgumentException("value");
        }
        int pos = 16;
        final char[] tmp = new char[pos];
        do {
            tmp[--pos] = list.charAt(--value % list.length());
            value /= list.length();
        } while (value > 0);
        return new String(tmp, pos, tmp.length - pos);
    }
}
