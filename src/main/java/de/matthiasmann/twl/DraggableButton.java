package de.matthiasmann.twl;

public class DraggableButton extends Button
{
    private int dragStartX;
    private int dragStartY;
    private boolean dragging;
    private DragListener listener;
    
    public DraggableButton() {
    }
    
    public DraggableButton(final AnimationState animState) {
        super(animState);
    }
    
    public DraggableButton(final AnimationState animState, final boolean inherit) {
        super(animState, inherit);
    }
    
    public boolean isDragActive() {
        return this.dragging;
    }
    
    public DragListener getListener() {
        return this.listener;
    }
    
    public void setListener(final DragListener listener) {
        this.listener = listener;
    }
    
    public boolean handleEvent(final Event evt) {
        if (evt.isMouseEvent() && this.dragging) {
            if (evt.getType() == Event.Type.MOUSE_DRAGGED && this.listener != null) {
                this.listener.dragged(evt.getMouseX() - this.dragStartX, evt.getMouseY() - this.dragStartY);
            }
            if (evt.isMouseDragEnd()) {
                this.stopDragging(evt);
            }
            return true;
        }
        switch (evt.getType()) {
            case MOUSE_BTNDOWN: {
                this.dragStartX = evt.getMouseX();
                this.dragStartY = evt.getMouseY();
                break;
            }
            case MOUSE_DRAGGED: {
                assert !this.dragging;
                this.dragging = true;
                this.getModel().setArmed(false);
                this.getModel().setPressed(true);
                if (this.listener != null) {
                    this.listener.dragStarted();
                }
                return true;
            }
        }
        return super.handleEvent(evt);
    }
    
    private void stopDragging(final Event evt) {
        if (this.listener != null) {
            this.listener.dragStopped();
        }
        this.dragging = false;
        this.getModel().setArmed(false);
        this.getModel().setPressed(false);
        this.getModel().setHover(this.isMouseInside(evt));
    }
    
    public interface DragListener
    {
        void dragStarted();
        
        void dragged(final int p0, final int p1);
        
        void dragStopped();
    }
}
