package de.matthiasmann.twl;

import de.matthiasmann.twl.model.*;
import de.matthiasmann.twl.utils.*;

public class TreePathDisplay extends Widget
{
    private final BoxLayout pathBox;
    private final EditField editField;
    private Callback[] callbacks;
    private String separator;
    private TreeTableNode currentNode;
    private boolean allowEdit;
    
    public TreePathDisplay() {
        this.separator = "/";
        (this.pathBox = new PathBox()).setScroll(true);
        this.pathBox.setClip(true);
        (this.editField = new PathEditField()).setVisible(false);
        this.add((Widget)this.pathBox);
        this.add((Widget)this.editField);
    }
    
    public void addCallback(final Callback cb) {
        this.callbacks = CallbackSupport.addCallbackToList(this.callbacks, cb, Callback.class);
    }
    
    public void removeCallback(final Callback cb) {
        this.callbacks = CallbackSupport.removeCallbackFromList(this.callbacks, cb);
    }
    
    public TreeTableNode getCurrentNode() {
        return this.currentNode;
    }
    
    public void setCurrentNode(final TreeTableNode currentNode) {
        this.currentNode = currentNode;
        this.rebuildPathBox();
    }
    
    public String getSeparator() {
        return this.separator;
    }
    
    public void setSeparator(final String separator) {
        this.separator = separator;
        this.rebuildPathBox();
    }
    
    public boolean isAllowEdit() {
        return this.allowEdit;
    }
    
    public void setAllowEdit(final boolean allowEdit) {
        this.allowEdit = allowEdit;
        this.rebuildPathBox();
    }
    
    public void setEditErrorMessage(final String msg) {
        this.editField.setErrorMessage((Object)msg);
    }
    
    public EditField getEditField() {
        return this.editField;
    }
    
    protected String getTextFromNode(final TreeTableNode node) {
        final Object data = node.getData(0);
        String text = (data != null) ? data.toString() : "";
        if (text.endsWith(this.separator)) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }
    
    private void rebuildPathBox() {
        this.pathBox.removeAllChildren();
        if (this.currentNode != null) {
            this.recursiveAddNode(this.currentNode, null);
        }
    }
    
    private void recursiveAddNode(final TreeTableNode node, final TreeTableNode child) {
        if (node.getParent() != null) {
            this.recursiveAddNode(node.getParent(), node);
            final Button btn = new Button(this.getTextFromNode(node));
            btn.setTheme("node");
            btn.addCallback((Runnable)new Runnable() {
                @Override
                public void run() {
                    TreePathDisplay.this.firePathElementClicked(node, child);
                }
            });
            this.pathBox.add((Widget)btn);
            final Label l = new Label(this.separator);
            l.setTheme("separator");
            if (this.allowEdit) {
                l.addCallback((CallbackWithReason)new CallbackWithReason<Label.CallbackReason>() {
                    public void callback(final Label.CallbackReason reason) {
                        if (reason == Label.CallbackReason.DOUBLE_CLICK) {
                            TreePathDisplay.this.editPath(node);
                        }
                    }
                });
            }
            this.pathBox.add((Widget)l);
        }
    }
    
    void endEdit() {
        this.editField.setVisible(false);
        this.requestKeyboardFocus();
    }
    
    void editPath(final TreeTableNode cursorAfterNode) {
        final StringBuilder sb = new StringBuilder();
        int cursorPos = 0;
        if (this.currentNode != null) {
            cursorPos = this.recursiveAddPath(sb, this.currentNode, cursorAfterNode);
        }
        this.editField.setErrorMessage((Object)null);
        this.editField.setText(sb.toString());
        this.editField.setCursorPos(cursorPos, false);
        this.editField.setVisible(true);
        this.editField.requestKeyboardFocus();
    }
    
    private int recursiveAddPath(final StringBuilder sb, final TreeTableNode node, final TreeTableNode cursorAfterNode) {
        int cursorPos = 0;
        if (node.getParent() != null) {
            cursorPos = this.recursiveAddPath(sb, node.getParent(), cursorAfterNode);
            sb.append(this.getTextFromNode(node)).append(this.separator);
        }
        if (node == cursorAfterNode) {
            return sb.length();
        }
        return cursorPos;
    }
    
    protected boolean fireResolvePath(final String text) {
        if (this.callbacks != null) {
            for (final Callback cb : this.callbacks) {
                if (cb.resolvePath(text)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    protected void firePathElementClicked(final TreeTableNode node, final TreeTableNode child) {
        if (this.callbacks != null) {
            for (final Callback cb : this.callbacks) {
                cb.pathElementClicked(node, child);
            }
        }
    }
    
    @Override
    public int getPreferredInnerWidth() {
        return this.pathBox.getPreferredWidth();
    }
    
    @Override
    public int getPreferredInnerHeight() {
        return Math.max(this.pathBox.getPreferredHeight(), this.editField.getPreferredHeight());
    }
    
    @Override
    public int getMinHeight() {
        final int minInnerHeight = Math.max(this.pathBox.getMinHeight(), this.editField.getMinHeight());
        return Math.max(super.getMinHeight(), minInnerHeight + this.getBorderVertical());
    }
    
    @Override
    protected void layout() {
        this.layoutChildFullInnerArea((Widget)this.pathBox);
        this.layoutChildFullInnerArea((Widget)this.editField);
    }
    
    private class PathBox extends BoxLayout
    {
        public PathBox() {
            super(BoxLayout.Direction.HORIZONTAL);
        }
        
        protected boolean handleEvent(final Event evt) {
            if (!evt.isMouseEvent()) {
                return super.handleEvent(evt);
            }
            if (evt.getType() == Event.Type.MOUSE_CLICKED && evt.getMouseClickCount() == 2) {
                TreePathDisplay.this.editPath(TreePathDisplay.this.getCurrentNode());
                return true;
            }
            return evt.getType() != Event.Type.MOUSE_WHEEL;
        }
    }
    
    private class PathEditField extends EditField
    {
        protected void keyboardFocusLost() {
            if (!this.hasOpenPopups()) {
                this.setVisible(false);
            }
        }
        
        protected void doCallback(final int key) {
            super.doCallback(key);
            switch (key) {
                case 28: {
                    if (TreePathDisplay.this.fireResolvePath(this.getText())) {
                        TreePathDisplay.this.endEdit();
                        break;
                    }
                    break;
                }
                case 1: {
                    TreePathDisplay.this.endEdit();
                    break;
                }
            }
        }
    }
    
    public interface Callback
    {
        void pathElementClicked(final TreeTableNode p0, final TreeTableNode p1);
        
        boolean resolvePath(final String p0);
    }
}
