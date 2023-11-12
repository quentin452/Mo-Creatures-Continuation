package de.matthiasmann.twl.utils;

import java.util.*;

public class NaturalSortComparator
{
    public static final Comparator<String> stringComparator;
    public static final Comparator<String> stringPathComparator;
    
    private static int findDiff(final String s1, final int idx1, final String s2, final int idx2) {
        final int len = Math.min(s1.length() - idx1, s2.length() - idx2);
        for (int i = 0; i < len; ++i) {
            final char c1 = s1.charAt(idx1 + i);
            final char c2 = s2.charAt(idx2 + i);
            if (c1 != c2 && Character.toLowerCase(c1) != Character.toLowerCase(c2)) {
                return i;
            }
        }
        return len;
    }
    
    private static int findNumberStart(final String s, int i) {
        while (i > 0 && Character.isDigit(s.charAt(i - 1))) {
            --i;
        }
        return i;
    }
    
    private static int findNumberEnd(final String s, int i) {
        for (int len = s.length(); i < len && Character.isDigit(s.charAt(i)); ++i) {}
        return i;
    }
    
    public static int naturalCompareWithPaths(final String n1, final String n2) {
        final int diffOffset = findDiff(n1, 0, n2, 0);
        final int idx0 = n1.indexOf(47, diffOffset);
        final int idx2 = n2.indexOf(47, diffOffset);
        if ((idx0 ^ idx2) < 0) {
            return idx0;
        }
        return naturalCompare(n1, n2, diffOffset, diffOffset);
    }
    
    public static int naturalCompare(final String n1, final String n2) {
        return naturalCompare(n1, n2, 0, 0);
    }
    
    private static int naturalCompare(final String n1, final String n2, int i1, int i2) {
        char c1;
        char c2;
        while (true) {
            final int diffOffset = findDiff(n1, i1, n2, i2);
            i1 += diffOffset;
            i2 += diffOffset;
            if (i1 == n1.length() || i2 == n2.length()) {
                return n1.length() - n2.length();
            }
            c1 = n1.charAt(i1);
            c2 = n2.charAt(i2);
            if (!Character.isDigit(c1) && !Character.isDigit(c2)) {
                break;
            }
            final int s1 = findNumberStart(n1, i1);
            final int s2 = findNumberStart(n2, i2);
            if (Character.isDigit(n1.charAt(s1)) && Character.isDigit(n2.charAt(s2))) {
                i1 = findNumberEnd(n1, s1 + 1);
                i2 = findNumberEnd(n2, s2 + 1);
                try {
                    final long value1 = Long.parseLong(n1.substring(s1, i1), 10);
                    final long value2 = Long.parseLong(n2.substring(s2, i2), 10);
                    if (value1 != value2) {
                        return Long.signum(value1 - value2);
                    }
                    continue;
                }
                catch (NumberFormatException ex) {}
                break;
            }
            break;
        }
        final char cl1 = Character.toLowerCase(c1);
        final char cl2 = Character.toLowerCase(c2);
        assert cl1 != cl2;
        return cl1 - cl2;
    }
    
    private NaturalSortComparator() {
    }
    
    static {
        stringComparator = new Comparator<String>() {
            @Override
            public int compare(final String n1, final String n2) {
                return NaturalSortComparator.naturalCompare(n1, n2);
            }
        };
        stringPathComparator = new Comparator<String>() {
            @Override
            public int compare(final String n1, final String n2) {
                return NaturalSortComparator.naturalCompareWithPaths(n1, n2);
            }
        };
    }
}
