package de.matthiasmann.twl.input.lwjgl;

import de.matthiasmann.twl.input.*;
import de.matthiasmann.twl.*;
import org.lwjgl.opengl.*;
import org.lwjgl.input.*;
import de.matthiasmann.twl.renderer.lwjgl.*;

public class LWJGLInput implements Input
{
    private boolean wasActive;
    
    public boolean pollInput(final GUI gui) {
        final boolean active = Display.isActive();
        if (this.wasActive && !active) {
            return this.wasActive = false;
        }
        this.wasActive = active;
        if (Keyboard.isCreated()) {
            while (Keyboard.next()) {
                gui.handleKey(Keyboard.getEventKey(), Keyboard.getEventCharacter(), Keyboard.getEventKeyState());
            }
        }
        if (Mouse.isCreated()) {
            while (Mouse.next()) {
                gui.handleMouse(Mouse.getEventX() / RenderScale.scale, (gui.getHeight() - Mouse.getEventY() - 1) / RenderScale.scale, Mouse.getEventButton(), Mouse.getEventButtonState());
                final int wheelDelta = Mouse.getEventDWheel();
                if (wheelDelta != 0) {
                    gui.handleMouseWheel(wheelDelta / 120);
                }
            }
        }
        return true;
    }
}
