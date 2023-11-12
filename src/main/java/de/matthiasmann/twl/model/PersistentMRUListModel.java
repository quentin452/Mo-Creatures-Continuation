package de.matthiasmann.twl.model;

import java.util.prefs.*;
import java.util.zip.*;
import java.io.*;
import java.util.logging.*;

public class PersistentMRUListModel<T extends Serializable> extends SimpleMRUListModel<T>
{
    private final Class<T> clazz;
    private final Preferences prefs;
    private final String prefKey;
    
    public PersistentMRUListModel(final int maxEntries, final Class<T> clazz, final Preferences prefs, final String prefKey) {
        super(maxEntries);
        if (clazz == null) {
            throw new NullPointerException("clazz");
        }
        if (prefs == null) {
            throw new NullPointerException("prefs");
        }
        if (prefKey == null) {
            throw new NullPointerException("prefKey");
        }
        this.clazz = clazz;
        this.prefs = prefs;
        this.prefKey = prefKey;
        for (int numEntries = Math.min(prefs.getInt(this.keyForNumEntries(), 0), maxEntries), i = 0; i < numEntries; ++i) {
            T entry = null;
            if (clazz == String.class) {
                entry = clazz.cast(prefs.get(this.keyForIndex(i), null));
            }
            else {
                final byte[] data = prefs.getByteArray(this.keyForIndex(i), null);
                if (data != null && data.length > 0) {
                    entry = this.deserialize(data);
                }
            }
            if (entry != null) {
                this.entries.add(entry);
            }
        }
    }
    
    @Override
    public void addEntry(final T entry) {
        if (!this.clazz.isInstance(entry)) {
            throw new ClassCastException();
        }
        super.addEntry(entry);
    }
    
    @Override
    protected void saveEntries() {
        for (int i = 0; i < this.entries.size(); ++i) {
            final T obj = this.entries.get(i);
            if (this.clazz == String.class) {
                this.prefs.put(this.keyForIndex(i), (String)obj);
            }
            else {
                final byte[] data = this.serialize(obj);
                assert data != null;
                this.prefs.putByteArray(this.keyForIndex(i), data);
            }
        }
        this.prefs.putInt(this.keyForNumEntries(), this.entries.size());
    }
    
    protected byte[] serialize(final T obj) {
        try {
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            final DeflaterOutputStream dos = new DeflaterOutputStream(baos, new Deflater(9));
            try {
                final ObjectOutputStream oos = new ObjectOutputStream(dos);
                oos.writeObject(obj);
                oos.close();
            }
            finally {
                this.close(dos);
            }
            return baos.toByteArray();
        }
        catch (IOException ex) {
            this.getLogger().log(Level.SEVERE, "Unable to serialize MRU entry", ex);
            return new byte[0];
        }
    }
    
    protected T deserialize(final byte[] data) {
        try {
            final ByteArrayInputStream bais = new ByteArrayInputStream(data);
            final InflaterInputStream iis = new InflaterInputStream(bais);
            try {
                final ObjectInputStream ois = new ObjectInputStream(iis);
                final Object obj = ois.readObject();
                if (this.clazz.isInstance(obj)) {
                    return this.clazz.cast(obj);
                }
                this.getLogger().log(Level.WARNING, "Deserialized object of type " + obj.getClass() + " expected " + this.clazz);
            }
            finally {
                this.close(iis);
            }
        }
        catch (Exception ex) {
            this.getLogger().log(Level.SEVERE, "Unable to deserialize MRU entry", ex);
        }
        return null;
    }
    
    protected String keyForIndex(final int idx) {
        return this.prefKey + "_" + idx;
    }
    
    protected String keyForNumEntries() {
        return this.prefKey + "_entries";
    }
    
    private void close(final Closeable c) {
        try {
            c.close();
        }
        catch (IOException ex) {
            this.getLogger().log(Level.WARNING, "exception while closing stream", ex);
        }
    }
    
    Logger getLogger() {
        return Logger.getLogger(PersistentMRUListModel.class.getName());
    }
}
