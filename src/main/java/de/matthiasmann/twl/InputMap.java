package de.matthiasmann.twl;

import java.util.*;
import java.net.*;
import de.matthiasmann.twl.utils.*;
import java.io.*;
import org.xmlpull.v1.*;
import java.util.logging.*;

public final class InputMap
{
    private static final InputMap EMPTY_MAP;
    private final KeyStroke[] keyStrokes;
    
    private InputMap(final KeyStroke[] keyStrokes) {
        this.keyStrokes = keyStrokes;
    }
    
    public String mapEvent(final Event event) {
        if (event.isKeyEvent()) {
            final int mappedEventModifiers = KeyStroke.convertModifier(event);
            for (final KeyStroke ks : this.keyStrokes) {
                if (ks.match(event, mappedEventModifiers)) {
                    return ks.getAction();
                }
            }
        }
        return null;
    }
    
    public InputMap addKeyStrokes(final LinkedHashSet<KeyStroke> newKeyStrokes) {
        int size = newKeyStrokes.size();
        if (size == 0) {
            return this;
        }
        final KeyStroke[] combined = new KeyStroke[this.keyStrokes.length + size];
        newKeyStrokes.toArray(combined);
        for (final KeyStroke ks : this.keyStrokes) {
            if (!newKeyStrokes.contains(ks)) {
                combined[size++] = ks;
            }
        }
        return new InputMap(shrink(combined, size));
    }
    
    public InputMap addKeyStrokes(final InputMap map) {
        if (map == this || map.keyStrokes.length == 0) {
            return this;
        }
        if (this.keyStrokes.length == 0) {
            return map;
        }
        return this.addKeyStrokes(new LinkedHashSet<KeyStroke>(Arrays.asList(map.keyStrokes)));
    }
    
    public InputMap addKeyStroke(final KeyStroke keyStroke) {
        final LinkedHashSet<KeyStroke> newKeyStrokes = new LinkedHashSet<KeyStroke>(1, 1.0f);
        newKeyStrokes.add(keyStroke);
        return this.addKeyStrokes(newKeyStrokes);
    }
    
    public InputMap removeKeyStrokes(final Set<KeyStroke> keyStrokes) {
        if (keyStrokes.isEmpty()) {
            return this;
        }
        int size = 0;
        final KeyStroke[] result = new KeyStroke[this.keyStrokes.length];
        for (final KeyStroke ks : this.keyStrokes) {
            if (!keyStrokes.contains(ks)) {
                result[size++] = ks;
            }
        }
        return new InputMap(shrink(result, size));
    }
    
    public KeyStroke[] getKeyStrokes() {
        return this.keyStrokes.clone();
    }
    
    public static InputMap empty() {
        return InputMap.EMPTY_MAP;
    }
    
    public static InputMap parse(final URL url) throws IOException {
        try {
            final XMLParser xmlp = new XMLParser(url);
            try {
                xmlp.require(0, null, null);
                xmlp.nextTag();
                xmlp.require(2, null, "inputMapDef");
                xmlp.nextTag();
                final LinkedHashSet<KeyStroke> keyStrokes = parseBody(xmlp);
                xmlp.require(3, null, "inputMapDef");
                return new InputMap(keyStrokes.toArray(new KeyStroke[keyStrokes.size()]));
            }
            finally {
                xmlp.close();
            }
        }
        catch (XmlPullParserException ex) {
            throw (IOException)new IOException("Can't parse XML").initCause((Throwable)ex);
        }
    }
    
    public void writeXML(final OutputStream os) throws IOException {
        try {
            final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            final XmlSerializer serializer = factory.newSerializer();
            serializer.setOutput(os, "UTF8");
            serializer.startDocument("UTF8", Boolean.TRUE);
            serializer.text("\n");
            serializer.startTag((String)null, "inputMapDef");
            for (final KeyStroke ks : this.keyStrokes) {
                serializer.text("\n    ");
                serializer.startTag((String)null, "action");
                serializer.attribute((String)null, "name", ks.getAction());
                serializer.text(ks.getStroke());
                serializer.endTag((String)null, "action");
            }
            serializer.text("\n");
            serializer.endTag((String)null, "inputMapDef");
            serializer.endDocument();
        }
        catch (XmlPullParserException ex) {
            throw (IOException)new IOException("Can't generate XML").initCause((Throwable)ex);
        }
    }
    
    public static LinkedHashSet<KeyStroke> parseBody(final XMLParser xmlp) throws XmlPullParserException, IOException {
        final LinkedHashSet<KeyStroke> newStrokes = new LinkedHashSet<KeyStroke>();
        while (!xmlp.isEndTag()) {
            xmlp.require(2, null, "action");
            final String name = xmlp.getAttributeNotNull("name");
            final String key = xmlp.nextText();
            try {
                final KeyStroke ks = KeyStroke.parse(key, name);
                if (!newStrokes.add(ks)) {
                    Logger.getLogger(InputMap.class.getName()).log(Level.WARNING, "Duplicate key stroke: {0}", ks.getStroke());
                }
            }
            catch (IllegalArgumentException ex) {
                throw xmlp.error("can't parse Keystroke", ex);
            }
            xmlp.require(3, null, "action");
            xmlp.nextTag();
        }
        return newStrokes;
    }
    
    private static KeyStroke[] shrink(KeyStroke[] keyStrokes, final int size) {
        if (size != keyStrokes.length) {
            final KeyStroke[] tmp = new KeyStroke[size];
            System.arraycopy(keyStrokes, 0, tmp, 0, size);
            keyStrokes = tmp;
        }
        return keyStrokes;
    }
    
    static {
        EMPTY_MAP = new InputMap(new KeyStroke[0]);
    }
}
