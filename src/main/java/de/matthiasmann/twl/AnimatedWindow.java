package de.matthiasmann.twl;

import de.matthiasmann.twl.model.*;
import de.matthiasmann.twl.utils.*;
import de.matthiasmann.twl.renderer.*;

public class AnimatedWindow extends Widget
{
    private int numAnimSteps;
    private int currentStep;
    private int animSpeed;
    private BooleanModel model;
    private Runnable modelCallback;
    private Runnable[] callbacks;
    
    public AnimatedWindow() {
        this.numAnimSteps = 10;
        this.setVisible(false);
    }
    
    public void addCallback(final Runnable cb) {
        this.callbacks = CallbackSupport.addCallbackToList(this.callbacks, cb, Runnable.class);
    }
    
    public void removeCallback(final Runnable cb) {
        this.callbacks = CallbackSupport.removeCallbackFromList(this.callbacks, cb);
    }
    
    private void doCallback() {
        CallbackSupport.fireCallbacks(this.callbacks);
    }
    
    public int getNumAnimSteps() {
        return this.numAnimSteps;
    }
    
    public void setNumAnimSteps(final int numAnimSteps) {
        if (numAnimSteps < 1) {
            throw new IllegalArgumentException("numAnimSteps");
        }
        this.numAnimSteps = numAnimSteps;
    }
    
    public void setState(final boolean open) {
        if (open && !this.isOpen()) {
            this.animSpeed = 1;
            this.setVisible(true);
            this.doCallback();
        }
        else if (!open && !this.isClosed()) {
            this.animSpeed = -1;
            this.doCallback();
        }
        if (this.model != null) {
            this.model.setValue(open);
        }
    }
    
    public BooleanModel getModel() {
        return this.model;
    }
    
    public void setModel(final BooleanModel model) {
        if (this.model != model) {
            if (this.model != null) {
                this.model.removeCallback(this.modelCallback);
            }
            if ((this.model = model) != null) {
                if (this.modelCallback == null) {
                    this.modelCallback = new ModelCallback();
                }
                model.addCallback(this.modelCallback);
                this.syncWithModel();
            }
        }
    }
    
    public boolean isOpen() {
        return this.currentStep == this.numAnimSteps && this.animSpeed >= 0;
    }
    
    public boolean isOpening() {
        return this.animSpeed > 0;
    }
    
    public boolean isClosed() {
        return this.currentStep == 0 && this.animSpeed <= 0;
    }
    
    public boolean isClosing() {
        return this.animSpeed < 0;
    }
    
    public boolean isAnimating() {
        return this.animSpeed != 0;
    }
    
    public boolean handleEvent(final Event evt) {
        if (this.isOpen()) {
            if (super.handleEvent(evt)) {
                return true;
            }
            if (evt.isKeyPressedEvent()) {
                switch (evt.getKeyCode()) {
                    case 1: {
                        this.setState(false);
                        return true;
                    }
                }
            }
            return false;
        }
        else {
            if (this.isClosed()) {
                return false;
            }
            final int mouseX = evt.getMouseX() - this.getX();
            final int mouseY = evt.getMouseY() - this.getY();
            return mouseX >= 0 && mouseX < this.getAnimatedWidth() && mouseY >= 0 && mouseY < this.getAnimatedHeight();
        }
    }
    
    @Override
    public int getMinWidth() {
        int minWidth = 0;
        for (int i = 0, n = this.getNumChildren(); i < n; ++i) {
            final Widget child = this.getChild(i);
            minWidth = Math.max(minWidth, child.getMinWidth());
        }
        return Math.max(super.getMinWidth(), minWidth + this.getBorderHorizontal());
    }
    
    @Override
    public int getMinHeight() {
        int minHeight = 0;
        for (int i = 0, n = this.getNumChildren(); i < n; ++i) {
            final Widget child = this.getChild(i);
            minHeight = Math.max(minHeight, child.getMinHeight());
        }
        return Math.max(super.getMinHeight(), minHeight + this.getBorderVertical());
    }
    
    @Override
    public int getPreferredInnerWidth() {
        return BoxLayout.computePreferredWidthVertical(this);
    }
    
    @Override
    public int getPreferredInnerHeight() {
        return BoxLayout.computePreferredHeightHorizontal(this);
    }
    
    @Override
    protected void layout() {
        this.layoutChildrenFullInnerArea();
    }
    
    @Override
    protected void paint(final GUI gui) {
        if (this.animSpeed != 0) {
            this.animate();
        }
        if (this.isOpen()) {
            super.paint(gui);
        }
        else if (!this.isClosed() && this.getBackground() != null) {
            this.getBackground().draw(this.getAnimationState(), this.getX(), this.getY(), this.getAnimatedWidth(), this.getAnimatedHeight());
        }
    }
    
    private void animate() {
        this.currentStep += this.animSpeed;
        if (this.currentStep == 0 || this.currentStep == this.numAnimSteps) {
            this.setVisible(this.currentStep > 0);
            this.animSpeed = 0;
            this.doCallback();
        }
    }
    
    private int getAnimatedWidth() {
        return this.getWidth() * this.currentStep / this.numAnimSteps;
    }
    
    private int getAnimatedHeight() {
        return this.getHeight() * this.currentStep / this.numAnimSteps;
    }
    
    void syncWithModel() {
        this.setState(this.model.getValue());
    }
    
    class ModelCallback implements Runnable
    {
        @Override
        public void run() {
            AnimatedWindow.this.syncWithModel();
        }
    }
}
