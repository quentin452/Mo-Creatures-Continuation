package de.matthiasmann.twl.utils;

import java.util.*;

public class TypeMapping<V>
{
    Entry<V>[] table;
    int size;

    public TypeMapping() {
        this.table = (Entry<V>[])new Entry[16];
    }

    public void put(final Class<?> clazz, final V value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        this.removeCached();
        final Entry<V> entry = (Entry<V>)HashEntry.get((HashEntry[])this.table, (Object)clazz);
        if (entry != null) {
            HashEntry.remove((HashEntry[])this.table, (HashEntry)entry);
            --this.size;
        }
        this.insert(new Entry<V>(clazz, value, false));
    }

    public V get(final Class<?> clazz) {
        final Entry<V> entry = (Entry<V>)HashEntry.get((HashEntry[])this.table, (Object)clazz);
        if (entry != null) {
            return entry.value;
        }
        return this.slowGet(clazz);
    }

    public boolean remove(final Class<?> clazz) {
        if (HashEntry.remove((HashEntry[])this.table, (Object)clazz) != null) {
            this.removeCached();
            --this.size;
            return true;
        }
        return false;
    }

    public Set<V> getUniqueValues() {
        final HashSet<V> result = new HashSet<V>();
        for (Entry<V> e : this.table) {
            while (e != null) {
                if (!e.isCache) {
                    result.add(e.value);
                }
                e = (Entry<V>)e.next();
            }
        }
        return result;
    }

    public Map<Class<?>, V> getEntries() {
        final HashMap<Class<?>, V> result = new HashMap<Class<?>, V>();
        for (Entry<V> e : this.table) {
            while (e != null) {
                if (!e.isCache) {
                    result.put((Class<?>)e.key, e.value);
                }
                e = (Entry<V>)e.next();
            }
        }
        return result;
    }

    private V slowGet(final Class<?> clazz) {
        Entry<V> entry = null;
        Class<?> baseClass = clazz;
    Label_0086:
        do {
            for (final Class<?> ifClass : baseClass.getInterfaces()) {
                entry = (Entry<V>)HashEntry.get((HashEntry[])this.table, (Object)ifClass);
                if (entry != null) {
                    break Label_0086;
                }
            }
            baseClass = baseClass.getSuperclass();
            if (baseClass == null) {
                break;
            }
            entry = (Entry<V>)HashEntry.get((HashEntry[])this.table, (Object)baseClass);
        } while (entry == null);
        final V value = (entry != null) ? entry.value : null;
        this.insert(new Entry<V>(clazz, value, true));
        return value;
    }

    private void insert(final Entry<V> newEntry) {
        HashEntry.insertEntry((HashEntry[])(this.table = (Entry<V>[])HashEntry.maybeResizeTable((HashEntry[])this.table, this.size)), (HashEntry)newEntry);
        ++this.size;
    }

    private void removeCached() {
        for (Entry<V> e : this.table) {
            while (e != null) {
                final Entry<V> n = (Entry<V>)e.next();
                if (e.isCache) {
                    HashEntry.remove((HashEntry[])this.table, (HashEntry)e);
                    --this.size;
                }
                e = n;
            }
        }
    }

    static class Entry<V> extends HashEntry<Class<?>, Entry<V>>
    {
        final V value;
        final boolean isCache;

        public Entry(final Class<?> key, final V value, final boolean isCache) {
            super(key);
            this.value = value;
            this.isCache = isCache;
        }
    }
}
