package de.matthiasmann.twl.utils;

public class CascadedHashMap<K, V>
{
    private Entry<K, V>[] table;
    private int size;
    private CascadedHashMap<K, V> fallback;
    
    public V get(final K key) {
        final Entry<K, V> entry = getEntry(this, key);
        if (entry != null) {
            return entry.value;
        }
        return null;
    }
    
    public V put(final K key, final V value) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        V oldValue = null;
        if (this.table != null) {
            final Entry<K, V> entry = HashEntry.get(this.table, key);
            if (entry != null) {
                oldValue = entry.value;
                entry.value = value;
                return oldValue;
            }
            if (this.fallback != null) {
                oldValue = this.fallback.get(key);
            }
        }
        this.insertEntry(key, value);
        return oldValue;
    }
    
    public void collapseAndSetFallback(final CascadedHashMap<K, V> map) {
        if (this.fallback != null) {
            this.collapsePutAll(this.fallback);
            this.fallback = null;
        }
        this.fallback = map;
    }
    
    protected static <K, V> Entry<K, V> getEntry(CascadedHashMap<K, V> map, final K key) {
        do {
            if (map.table != null) {
                final Entry<K, V> entry = HashEntry.get(map.table, key);
                if (entry != null) {
                    return entry;
                }
            }
            map = map.fallback;
        } while (map != null);
        return null;
    }
    
    private void collapsePutAll(CascadedHashMap<K, V> map) {
        do {
            final Entry<K, V>[] tab = map.table;
            if (tab != null) {
                for (int i = 0, n = tab.length; i < n; ++i) {
                    for (Entry<K, V> e = tab[i]; e != null; e = (Entry<K, V>)e.next) {
                        if (HashEntry.get(this.table, e.key) == null) {
                            this.insertEntry(e.key, e.value);
                        }
                    }
                }
            }
            map = map.fallback;
        } while (map != null);
    }
    
    private void insertEntry(final K key, final V value) {
        if (this.table == null) {
            this.table = (Entry<K, V>[])new Entry[16];
        }
        this.table = HashEntry.maybeResizeTable(this.table, ++this.size);
        final Entry<K, V> entry = new Entry<K, V>(key, value);
        HashEntry.insertEntry(this.table, entry);
    }
    
    protected static class Entry<K, V> extends HashEntry<K, Entry<K, V>>
    {
        V value;
        
        public Entry(final K key, final V value) {
            super(key);
            this.value = value;
        }
    }
}
