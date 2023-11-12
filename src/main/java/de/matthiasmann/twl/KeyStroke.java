package de.matthiasmann.twl;

import de.matthiasmann.twl.utils.*;
import java.util.*;

public final class KeyStroke
{
    private static final int SHIFT = 1;
    private static final int CTRL = 2;
    private static final int META = 4;
    private static final int ALT = 8;
    private static final int CMD = 20;
    private final int modifier;
    private final int keyCode;
    private final char keyChar;
    private final String action;
    
    private KeyStroke(final int modifier, final int keyCode, final char keyChar, final String action) {
        this.modifier = modifier;
        this.keyCode = keyCode;
        this.keyChar = keyChar;
        this.action = action;
    }
    
    public String getAction() {
        return this.action;
    }
    
    public String getStroke() {
        final StringBuilder sb = new StringBuilder();
        if ((this.modifier & 0x1) == 0x1) {
            sb.append("shift ");
        }
        if ((this.modifier & 0x2) == 0x2) {
            sb.append("ctrl ");
        }
        if ((this.modifier & 0x8) == 0x8) {
            sb.append("alt ");
        }
        if ((this.modifier & 0x14) == 0x14) {
            sb.append("cmd ");
        }
        else if ((this.modifier & 0x4) == 0x4) {
            sb.append("meta ");
        }
        if (this.keyCode != 0) {
            sb.append(Event.getKeyNameForCode(this.keyCode));
        }
        else {
            sb.append("typed ").append(this.keyChar);
        }
        return sb.toString();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof KeyStroke) {
            final KeyStroke other = (KeyStroke)obj;
            return this.modifier == other.modifier && this.keyCode == other.keyCode && this.keyChar == other.keyChar;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + this.modifier;
        hash = 83 * hash + this.keyCode;
        hash = 83 * hash + this.keyChar;
        return hash;
    }
    
    public static KeyStroke parse(final String stroke, final String action) {
        if (stroke == null) {
            throw new NullPointerException("stroke");
        }
        if (action == null) {
            throw new NullPointerException("action");
        }
        int idx = TextUtil.skipSpaces(stroke, 0);
        int modifers = 0;
        char keyChar = '\0';
        int keyCode = 0;
        boolean typed = false;
        boolean end = false;
        while (idx < stroke.length()) {
            final int endIdx = TextUtil.indexOf(stroke, ' ', idx);
            final String part = stroke.substring(idx, endIdx);
            if (end) {
                throw new IllegalArgumentException("Unexpected: " + part);
            }
            if (typed) {
                if (part.length() != 1) {
                    throw new IllegalArgumentException("Expected single character after 'typed'");
                }
                keyChar = part.charAt(0);
                if (keyChar == '\0') {
                    throw new IllegalArgumentException("Unknown character: " + part);
                }
                end = true;
            }
            else if ("ctrl".equalsIgnoreCase(part) || "control".equalsIgnoreCase(part)) {
                modifers |= 0x2;
            }
            else if ("shift".equalsIgnoreCase(part)) {
                modifers |= 0x1;
            }
            else if ("meta".equalsIgnoreCase(part)) {
                modifers |= 0x4;
            }
            else if ("cmd".equalsIgnoreCase(part)) {
                modifers |= 0x14;
            }
            else if ("alt".equalsIgnoreCase(part)) {
                modifers |= 0x8;
            }
            else if ("typed".equalsIgnoreCase(part)) {
                typed = true;
            }
            else {
                keyCode = Event.getKeyCodeForName(part.toUpperCase(Locale.ENGLISH));
                if (keyCode == 0) {
                    throw new IllegalArgumentException("Unknown key: " + part);
                }
                end = true;
            }
            idx = TextUtil.skipSpaces(stroke, endIdx + 1);
        }
        if (!end) {
            throw new IllegalArgumentException("Unexpected end of string");
        }
        return new KeyStroke(modifers, keyCode, keyChar, action);
    }
    
    public static KeyStroke fromEvent(final Event event, final String action) {
        if (event == null) {
            throw new NullPointerException("event");
        }
        if (action == null) {
            throw new NullPointerException("action");
        }
        if (event.getType() != Event.Type.KEY_PRESSED) {
            throw new IllegalArgumentException("Event is not a Type.KEY_PRESSED");
        }
        final int modifiers = convertModifier(event);
        return new KeyStroke(modifiers, event.getKeyCode(), '\0', action);
    }
    
    boolean match(final Event e, final int mappedEventModifiers) {
        return mappedEventModifiers == this.modifier && (this.keyCode == 0 || this.keyCode == e.getKeyCode()) && (this.keyChar == '\0' || (e.hasKeyChar() && this.keyChar == e.getKeyChar()));
    }
    
    static int convertModifier(final Event event) {
        final int eventModifiers = event.getModifiers();
        int modifiers = 0;
        if ((eventModifiers & 0x9) != 0x0) {
            modifiers |= 0x1;
        }
        if ((eventModifiers & 0x24) != 0x0) {
            modifiers |= 0x2;
        }
        if ((eventModifiers & 0x12) != 0x0) {
            modifiers |= 0x4;
        }
        if ((eventModifiers & 0x2) != 0x0) {
            modifiers |= 0x14;
        }
        if ((eventModifiers & 0x600) != 0x0) {
            modifiers |= 0x8;
        }
        return modifiers;
    }
}
