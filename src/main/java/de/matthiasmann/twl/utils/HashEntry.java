package de.matthiasmann.twl.utils;

import java.lang.reflect.*;

public class HashEntry<K, T extends HashEntry<K, T>>
{
    public final K key;
    final int hash;
    T next;
    
    public HashEntry(final K key) {
        this.key = key;
        this.hash = key.hashCode();
    }
    
    public T next() {
        return this.next;
    }
    
    public static <K, T extends HashEntry<K, T>> T get(final T[] table, final Object key) {
        int hash;
        T e;
        Object k;
        for (hash = key.hashCode(), e = table[hash & table.length - 1]; e != null && (e.hash != hash || ((k = e.key) != key && !key.equals(k))); e = (T)e.next) {}
        return e;
    }
    
    public static <K, T extends HashEntry<K, T>> void insertEntry(final T[] table, final T newEntry) {
        final int idx = newEntry.hash & table.length - 1;
        newEntry.next = (T)table[idx];
        table[idx] = newEntry;
    }
    
    public static <K, T extends HashEntry<K, T>> T remove(final T[] table, final Object key) {
        final int hash = key.hashCode();
        final int idx = hash & table.length - 1;
        T e = table[idx];
        T p = null;
        Object k;
        while (e != null && (e.hash != hash || ((k = e.key) != key && !key.equals(k)))) {
            p = e;
            e = (T)e.next;
        }
        if (e != null) {
            if (p != null) {
                p.next = e.next;
            }
            else {
                table[idx] = (T)e.next;
            }
        }
        return e;
    }
    
    public static <K, T extends HashEntry<K, T>> void remove(final T[] table, final T entry) {
        final int idx = entry.hash & table.length - 1;
        T e = table[idx];
        if (e == entry) {
            table[idx] = (T)e.next;
        }
        else {
            T p;
            do {
                p = e;
                e = (T)e.next;
            } while (e != entry);
            p.next = e.next;
        }
    }
    
    public static <K, T extends HashEntry<K, T>> T[] maybeResizeTable(T[] table, final int usedCount) {
        if (usedCount * 4 > table.length * 3) {
            table = (T[])resizeTable((HashEntry[])table, table.length * 2);
        }
        return table;
    }
    
    private static <K, T extends HashEntry<K, T>> T[] resizeTable(final T[] table, final int newSize) {
        if (newSize < 4 || (newSize & newSize - 1) != 0x0) {
            throw new IllegalArgumentException("newSize");
        }
        final T[] newTable = (T[])Array.newInstance(table.getClass().getComponentType(), newSize);
        for (int i = 0, n = table.length; i < n; ++i) {
            T ne;
            for (T e = table[i]; e != null; e = ne) {
                ne = (T)e.next;
                final int ni = e.hash & newSize - 1;
                e.next = (T)newTable[ni];
                newTable[ni] = e;
            }
        }
        return newTable;
    }
}
