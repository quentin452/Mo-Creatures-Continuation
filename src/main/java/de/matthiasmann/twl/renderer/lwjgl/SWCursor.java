package de.matthiasmann.twl.renderer.lwjgl;

import de.matthiasmann.twl.renderer.*;
import org.lwjgl.opengl.*;

class SWCursor extends TextureAreaBase implements MouseCursor
{
    private final LWJGLTexture texture;
    private final int hotSpotX;
    private final int hotSpotY;
    private final Image imageRef;
    
    SWCursor(final LWJGLTexture texture, final int x, final int y, final int width, final int height, final int hotSpotX, final int hotSpotY, final Image imageRef) {
        super(x, y, width, height, (float)texture.getTexWidth(), (float)texture.getTexHeight());
        this.texture = texture;
        this.hotSpotX = hotSpotX;
        this.hotSpotY = hotSpotY;
        this.imageRef = imageRef;
    }
    
    void render(final int x, final int y) {
        if (this.imageRef != null) {
            this.imageRef.draw((AnimationState)this.texture.renderer.swCursorAnimState, x - this.hotSpotX, y - this.hotSpotY);
        }
        else if (this.texture.bind()) {
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            GL11.glBegin(7);
            this.drawQuad(x - this.hotSpotX, y - this.hotSpotY, this.width, this.height);
            GL11.glEnd();
        }
    }
}
