package de.matthiasmann.twl;

import de.matthiasmann.twl.utils.*;
import de.matthiasmann.twl.model.*;
import java.util.concurrent.*;
import de.matthiasmann.twl.renderer.*;

public class EditField extends Widget
{
    public static final AnimationState.StateKey STATE_ERROR;
    public static final AnimationState.StateKey STATE_READONLY;
    public static final AnimationState.StateKey STATE_HOVER;
    public static final AnimationState.StateKey STATE_CURSOR_MOVED;
    final EditFieldModel editBuffer;
    public TextRenderer textRenderer;
    private PasswordMasker passwordMasking;
    private Runnable modelChangeListener;
    private StringModel model;
    private boolean readOnly;
    StringAttributes attributes;
    private int cursorPos;
    int scrollPos;
    int selectionStart;
    int selectionEnd;
    int numberOfLines;
    boolean multiLine;
    boolean pendingScrollToCursor;
    boolean pendingScrollToCursorForce;
    private int maxTextLength;
    private int columns;
    private Image cursorImage;
    Image selectionImage;
    private char passwordChar;
    private Object errorMsg;
    private boolean errorMsgFromModel;
    private Callback[] callbacks;
    private Menu popupMenu;
    private boolean textLongerThenWidget;
    private boolean forwardUnhandledKeysToCallback;
    private boolean autoCompletionOnSetText;
    boolean scrollToCursorOnSizeChange;
    private EditFieldAutoCompletionWindow autoCompletionWindow;
    private int autoCompletionHeight;
    private InfoWindow errorInfoWindow;
    private Label errorInfoLabel;

    public EditField(final de.matthiasmann.twl.AnimationState parentAnimationState, final EditFieldModel editFieldModel) {
        super(parentAnimationState, true);
        this.maxTextLength = 32767;
        this.columns = 5;
        this.autoCompletionOnSetText = true;
        this.scrollToCursorOnSizeChange = true;
        this.autoCompletionHeight = 100;
        if (editFieldModel == null) {
            throw new NullPointerException("editFieldModel");
        }
        this.editBuffer = editFieldModel;
        this.textRenderer = new TextRenderer(this.getAnimationState());
        this.passwordChar = '*';
        this.textRenderer.setTheme("renderer");
        this.textRenderer.setClip(true);
        this.add(this.textRenderer);
        this.setCanAcceptKeyboardFocus(true);
        this.setDepthFocusTraversal(false);
        this.addActionMapping("cut", "cutToClipboard", new Object[0]);
        this.addActionMapping("copy", "copyToClipboard", new Object[0]);
        this.addActionMapping("paste", "pasteFromClipboard", new Object[0]);
        this.addActionMapping("selectAll", "selectAll", new Object[0]);
    }

    public EditField(final de.matthiasmann.twl.AnimationState parentAnimationState) {
        this(parentAnimationState, new DefaultEditFieldModel());
    }

    public EditField() {
        this(null);
    }

    public void addCallback(final Callback cb) {
        this.callbacks = CallbackSupport.addCallbackToList(this.callbacks, cb, Callback.class);
    }

    public void removeCallback(final Callback cb) {
        this.callbacks = CallbackSupport.removeCallbackFromList(this.callbacks, cb);
    }

    public boolean isForwardUnhandledKeysToCallback() {
        return this.forwardUnhandledKeysToCallback;
    }

    public void setForwardUnhandledKeysToCallback(final boolean forwardUnhandledKeysToCallback) {
        this.forwardUnhandledKeysToCallback = forwardUnhandledKeysToCallback;
    }

    public boolean isAutoCompletionOnSetText() {
        return this.autoCompletionOnSetText;
    }

    public void setAutoCompletionOnSetText(final boolean autoCompletionOnSetText) {
        this.autoCompletionOnSetText = autoCompletionOnSetText;
    }

    public boolean isScrollToCursorOnSizeChange() {
        return this.scrollToCursorOnSizeChange;
    }

    public void setScrollToCursorOnSizeChange(final boolean scrollToCursorOnSizeChange) {
        this.scrollToCursorOnSizeChange = scrollToCursorOnSizeChange;
    }

    protected void doCallback(final int key) {
        if (this.callbacks != null) {
            for (final Callback cb : this.callbacks) {
                cb.callback(key);
            }
        }
    }

    public boolean isPasswordMasking() {
        return this.passwordMasking != null;
    }

    public void setPasswordMasking(final boolean passwordMasking) {
        if (passwordMasking != this.isPasswordMasking()) {
            if (passwordMasking) {
                this.passwordMasking = new PasswordMasker(this.editBuffer, this.passwordChar);
            }
            else {
                this.passwordMasking = null;
            }
            this.updateTextDisplay();
        }
    }

    public char getPasswordChar() {
        return this.passwordChar;
    }

    public void setPasswordChar(final char passwordChar) {
        this.passwordChar = passwordChar;
        if (this.passwordMasking != null && this.passwordMasking.maskingChar != passwordChar) {
            this.passwordMasking = new PasswordMasker(this.editBuffer, passwordChar);
            this.updateTextDisplay();
        }
    }

    public int getColumns() {
        return this.columns;
    }

    public void setColumns(final int columns) {
        if (columns < 0) {
            throw new IllegalArgumentException("columns");
        }
        this.columns = columns;
    }

    public boolean isMultiLine() {
        return this.multiLine;
    }

    public void setMultiLine(final boolean multiLine) {
        this.multiLine = multiLine;
        if (!multiLine && this.numberOfLines > 1) {
            this.setText("");
        }
    }

    public StringModel getModel() {
        return this.model;
    }

    public void setModel(final StringModel model) {
        this.removeModelChangeListener();
        if (this.model != null) {
            this.model.removeCallback(this.modelChangeListener);
        }
        this.model = model;
        if (this.getGUI() != null) {
            this.addModelChangeListener();
        }
    }

    public void setText(final String text) {
        this.setText(text, false);
    }

    void setText(String text, final boolean fromModel) {
        text = TextUtil.limitStringLength(text, this.maxTextLength);
        this.editBuffer.replace(0, this.editBuffer.length(), text);
        this.cursorPos = (this.multiLine ? 0 : this.editBuffer.length());
        this.selectionStart = 0;
        this.selectionEnd = 0;
        this.updateSelection();
        this.updateText(this.autoCompletionOnSetText, fromModel, 0);
        this.scrollToCursor(true);
    }

    public String getText() {
        return this.editBuffer.toString();
    }

    public StringAttributes getStringAttributes() {
        if (this.attributes == null) {
            this.textRenderer.setCache(false);
            this.attributes = new StringAttributes(this.editBuffer, (AnimationState)this.getAnimationState());
        }
        return this.attributes;
    }

    public void disableStringAttributes() {
        if (this.attributes != null) {
            this.attributes = null;
        }
    }

    public String getSelectedText() {
        return this.editBuffer.substring(this.selectionStart, this.selectionEnd);
    }

    public boolean hasSelection() {
        return this.selectionStart != this.selectionEnd;
    }

    public int getCursorPos() {
        return this.cursorPos;
    }

    public int getTextLength() {
        return this.editBuffer.length();
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public void setReadOnly(final boolean readOnly) {
        if (this.readOnly != readOnly) {
            this.readOnly = readOnly;
            this.popupMenu = null;
            this.getAnimationState().setAnimationState(EditField.STATE_READONLY, readOnly);
            this.firePropertyChange("readonly", !readOnly, readOnly);
        }
    }

    public void insertText(final String str) {
        if (!this.readOnly) {
            boolean update = false;
            if (this.hasSelection()) {
                this.deleteSelection();
                update = true;
            }
            final int insertLength = Math.min(str.length(), this.maxTextLength - this.editBuffer.length());
            if (insertLength > 0) {
                final int inserted = this.editBuffer.replace(this.cursorPos, 0, str.substring(0, insertLength));
                if (inserted > 0) {
                    this.cursorPos += inserted;
                    update = true;
                }
            }
            if (update) {
                this.updateText(true, false, 0);
            }
        }
    }

    public void pasteFromClipboard() {
        String cbText = Clipboard.getClipboard();
        if (cbText != null) {
            if (!this.multiLine) {
                cbText = TextUtil.stripNewLines(cbText);
            }
            this.insertText(cbText);
        }
    }

    public void copyToClipboard() {
        String text;
        if (this.hasSelection()) {
            text = this.getSelectedText();
        }
        else {
            text = this.getText();
        }
        if (this.isPasswordMasking()) {
            text = TextUtil.createString(this.passwordChar, text.length());
        }
        Clipboard.setClipboard(text);
    }

    public void cutToClipboard() {
        if (!this.hasSelection()) {
            this.selectAll();
        }
        String text = this.getSelectedText();
        if (!this.readOnly) {
            this.deleteSelection();
            this.updateText(true, false, 211);
        }
        if (this.isPasswordMasking()) {
            text = TextUtil.createString(this.passwordChar, text.length());
        }
        Clipboard.setClipboard(text);
    }

    public int getMaxTextLength() {
        return this.maxTextLength;
    }

    public void setMaxTextLength(final int maxTextLength) {
        this.maxTextLength = maxTextLength;
    }

    void removeModelChangeListener() {
        if (this.model != null && this.modelChangeListener != null) {
            this.model.removeCallback(this.modelChangeListener);
        }
    }

    void addModelChangeListener() {
        if (this.model != null) {
            if (this.modelChangeListener == null) {
                this.modelChangeListener = new ModelChangeListener();
            }
            this.model.addCallback(this.modelChangeListener);
            this.modelChanged();
        }
    }

    @Override
    protected void afterAddToGUI(final GUI gui) {
        super.afterAddToGUI(gui);
        this.addModelChangeListener();
    }

    @Override
    protected void beforeRemoveFromGUI(final GUI gui) {
        this.removeModelChangeListener();
        super.beforeRemoveFromGUI(gui);
    }

    @Override
    protected void applyTheme(final ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        this.applyThemeEditField(themeInfo);
    }

    protected void applyThemeEditField(final ThemeInfo themeInfo) {
        this.cursorImage = themeInfo.getImage("cursor");
        this.selectionImage = themeInfo.getImage("selection");
        this.autoCompletionHeight = themeInfo.getParameter("autocompletion-height", 100);
        this.columns = themeInfo.getParameter("columns", 5);
        this.setPasswordChar((char)themeInfo.getParameter("passwordChar", 42));
    }

    @Override
    protected void layout() {
        this.layoutChildFullInnerArea(this.textRenderer);
        this.checkTextWidth();
        this.layoutInfoWindows();
    }

    @Override
    protected void positionChanged() {
        this.layoutInfoWindows();
    }

    private void layoutInfoWindows() {
        if (this.autoCompletionWindow != null) {
            this.layoutAutocompletionWindow();
        }
        if (this.errorInfoWindow != null) {
            this.layoutErrorInfoWindow();
        }
    }

    private void layoutAutocompletionWindow() {
        int y = this.getBottom();
        final GUI gui = this.getGUI();
        if (gui != null && y + this.autoCompletionHeight > gui.getInnerBottom()) {
            final int ytop = this.getY() - this.autoCompletionHeight;
            if (ytop >= gui.getInnerY()) {
                y = ytop;
            }
        }
        this.autoCompletionWindow.setPosition(this.getX(), y);
        this.autoCompletionWindow.setSize(this.getWidth(), this.autoCompletionHeight);
    }

    private int computeInnerWidth() {
        if (this.columns > 0) {
            final Font font = this.getFont();
            if (font != null) {
                return font.computeTextWidth("X") * this.columns;
            }
        }
        return 0;
    }

    private int computeInnerHeight() {
        final int lineHeight = this.getLineHeight();
        if (this.multiLine) {
            return lineHeight * this.numberOfLines;
        }
        return lineHeight;
    }

    @Override
    public int getMinWidth() {
        int minWidth = super.getMinWidth();
        minWidth = Math.max(minWidth, this.computeInnerWidth() + this.getBorderHorizontal());
        return minWidth;
    }

    @Override
    public int getMinHeight() {
        int minHeight = super.getMinHeight();
        minHeight = Math.max(minHeight, this.computeInnerHeight() + this.getBorderVertical());
        return minHeight;
    }

    @Override
    public int getPreferredInnerWidth() {
        return this.computeInnerWidth();
    }

    @Override
    public int getPreferredInnerHeight() {
        return this.computeInnerHeight();
    }

    public void setErrorMessage(final Object errorMsg) {
        this.errorMsgFromModel = false;
        this.getAnimationState().setAnimationState(EditField.STATE_ERROR, errorMsg != null);
        if (this.errorMsg != errorMsg) {
            this.errorMsg = errorMsg;
            this.updateTooltip();
        }
        if (errorMsg != null) {
            if (this.hasKeyboardFocus()) {
                this.openErrorInfoWindow();
            }
        }
        else if (this.errorInfoWindow != null) {
            this.errorInfoWindow.closeInfo();
        }
    }

    @Override
    public Object getTooltipContent() {
        if (this.errorMsg != null) {
            return this.errorMsg;
        }
        Object tooltip = super.getTooltipContent();
        if (tooltip == null && !this.isPasswordMasking() && this.textLongerThenWidget && !this.hasKeyboardFocus()) {
            tooltip = this.getText();
        }
        return tooltip;
    }

    public void setAutoCompletionWindow(final EditFieldAutoCompletionWindow window) {
        if (this.autoCompletionWindow != window) {
            if (this.autoCompletionWindow != null) {
                this.autoCompletionWindow.closeInfo();
            }
            this.autoCompletionWindow = window;
        }
    }

    public EditFieldAutoCompletionWindow getAutoCompletionWindow() {
        return this.autoCompletionWindow;
    }

    public void setAutoCompletion(final AutoCompletionDataSource dataSource) {
        if (dataSource == null) {
            this.setAutoCompletionWindow(null);
        }
        else {
            this.setAutoCompletionWindow(new EditFieldAutoCompletionWindow(this, dataSource));
        }
    }

    public void setAutoCompletion(final AutoCompletionDataSource dataSource, final ExecutorService executorService) {
        if (dataSource == null) {
            this.setAutoCompletionWindow(null);
        }
        else {
            this.setAutoCompletionWindow(new EditFieldAutoCompletionWindow(this, dataSource, executorService));
        }
    }

    public boolean handleEvent(final Event evt) {
        final boolean selectPressed = (evt.getModifiers() & 0x9) != 0x0;
        if (evt.isMouseEvent()) {
            final boolean hover = evt.getType() != Event.Type.MOUSE_EXITED && this.isMouseInside(evt);
            this.getAnimationState().setAnimationState(EditField.STATE_HOVER, hover);
        }
        if (evt.isMouseDragEvent()) {
            if (evt.getType() == Event.Type.MOUSE_DRAGGED && (evt.getModifiers() & 0x40) != 0x0) {
                final int newPos = this.getCursorPosFromMouse(evt.getMouseX(), evt.getMouseY());
                this.setCursorPos(newPos, true);
            }
            return true;
        }
        if (super.handleEvent(evt)) {
            return true;
        }
        if (this.autoCompletionWindow != null && this.autoCompletionWindow.handleEvent(evt)) {
            return true;
        }
        switch (evt.getType()) {
            case KEY_PRESSED: {
                switch (evt.getKeyCode()) {
                    case 14: {
                        this.deletePrev();
                        return true;
                    }
                    case 211: {
                        this.deleteNext();
                        return true;
                    }
                    case 28:
                    case 156: {
                        if (this.multiLine) {
                            if (!evt.hasKeyCharNoModifiers()) {
                                break;
                            }
                            this.insertChar('\n');
                        }
                        else {
                            this.doCallback(28);
                        }
                        return true;
                    }
                    case 1: {
                        this.doCallback(evt.getKeyCode());
                        return true;
                    }
                    case 199: {
                        this.setCursorPos(this.computeLineStart(this.cursorPos), selectPressed);
                        return true;
                    }
                    case 207: {
                        this.setCursorPos(this.computeLineEnd(this.cursorPos), selectPressed);
                        return true;
                    }
                    case 203: {
                        this.moveCursor(-1, selectPressed);
                        return true;
                    }
                    case 205: {
                        this.moveCursor(1, selectPressed);
                        return true;
                    }
                    case 200: {
                        if (this.multiLine) {
                            this.moveCursorY(-1, selectPressed);
                            return true;
                        }
                        break;
                    }
                    case 208: {
                        if (this.multiLine) {
                            this.moveCursorY(1, selectPressed);
                            return true;
                        }
                        break;
                    }
                    case 15: {
                        return false;
                    }
                    default: {
                        if (evt.hasKeyCharNoModifiers()) {
                            this.insertChar(evt.getKeyChar());
                            return true;
                        }
                        break;
                    }
                }
                if (this.forwardUnhandledKeysToCallback) {
                    this.doCallback(evt.getKeyCode());
                    return true;
                }
                return false;
            }
            case KEY_RELEASED: {
                switch (evt.getKeyCode()) {
                    case 1:
                    case 14:
                    case 28:
                    case 156:
                    case 199:
                    case 203:
                    case 205:
                    case 207:
                    case 211: {
                        return true;
                    }
                    default: {
                        return evt.hasKeyCharNoModifiers() || this.forwardUnhandledKeysToCallback;
                    }
                }
            }
            case MOUSE_BTNUP: {
                if (evt.getMouseButton() == 1 && this.isMouseInside(evt)) {
                    this.showPopupMenu(evt);
                    return true;
                }
                break;
            }
            case MOUSE_BTNDOWN: {
                if (evt.getMouseButton() == 0 && this.isMouseInside(evt)) {
                    final int newPos = this.getCursorPosFromMouse(evt.getMouseX(), evt.getMouseY());
                    this.setCursorPos(newPos, selectPressed);
                    this.scrollPos = this.textRenderer.lastScrollPos;
                    return true;
                }
                break;
            }
            case MOUSE_CLICKED: {
                if (evt.getMouseClickCount() == 2) {
                    final int newPos = this.getCursorPosFromMouse(evt.getMouseX(), evt.getMouseY());
                    this.selectWordFromMouse(newPos);
                    this.cursorPos = this.selectionStart;
                    this.scrollToCursor(false);
                    this.cursorPos = this.selectionEnd;
                    this.scrollToCursor(false);
                    return true;
                }
                if (evt.getMouseClickCount() == 3) {
                    this.selectAll();
                    return true;
                }
                break;
            }
            case MOUSE_WHEEL: {
                return false;
            }
        }
        return evt.isMouseEvent();
    }

    protected void showPopupMenu(final Event evt) {
        if (this.popupMenu == null) {
            this.popupMenu = this.createPopupMenu();
        }
        if (this.popupMenu != null) {
            this.popupMenu.openPopupMenu(this, evt.getMouseX(), evt.getMouseY());
        }
    }

    protected Menu createPopupMenu() {
        final Menu menu = new Menu();
        if (!this.readOnly) {
            menu.add("cut", new Runnable() {
                @Override
                public void run() {
                    EditField.this.cutToClipboard();
                }
            });
        }
        menu.add("copy", new Runnable() {
            @Override
            public void run() {
                EditField.this.copyToClipboard();
            }
        });
        if (!this.readOnly) {
            menu.add("paste", new Runnable() {
                @Override
                public void run() {
                    EditField.this.pasteFromClipboard();
                }
            });
            menu.add("clear", new Runnable() {
                @Override
                public void run() {
                    if (!EditField.this.isReadOnly()) {
                        EditField.this.setText("");
                    }
                }
            });
        }
        menu.addSpacer();
        menu.add("select all", new Runnable() {
            @Override
            public void run() {
                EditField.this.selectAll();
            }
        });
        return menu;
    }

    private void updateText(final boolean updateAutoCompletion, final boolean fromModel, final int key) {
        if (this.model != null && !fromModel) {
            try {
                this.model.setValue(this.getText());
                if (this.errorMsgFromModel) {
                    this.setErrorMessage(null);
                }
            }
            catch (Exception ex) {
                if (this.errorMsg == null || this.errorMsgFromModel) {
                    this.setErrorMessage(ex.getMessage());
                    this.errorMsgFromModel = true;
                }
            }
        }
        this.updateTextDisplay();
        if (this.multiLine) {
            final int numLines = this.textRenderer.getNumTextLines();
            if (this.numberOfLines != numLines) {
                this.numberOfLines = numLines;
                this.invalidateLayout();
            }
        }
        this.doCallback(key);
        if ((this.autoCompletionWindow != null && this.autoCompletionWindow.isOpen()) || updateAutoCompletion) {
            this.updateAutoCompletion();
        }
    }

    private void updateTextDisplay() {
        this.textRenderer.setCharSequence((CharSequence)((this.passwordMasking != null) ? this.passwordMasking : this.editBuffer));
        this.textRenderer.cacheDirty = true;
        this.checkTextWidth();
        this.scrollToCursor(false);
    }

    private void checkTextWidth() {
        this.textLongerThenWidget = (this.textRenderer.getPreferredWidth() > this.textRenderer.getWidth());
    }

    protected void moveCursor(final int dir, final boolean select) {
        this.setCursorPos(this.cursorPos + dir, select);
    }

    protected void moveCursorY(final int dir, final boolean select) {
        if (this.multiLine) {
            final int x = this.computeRelativeCursorPositionX(this.cursorPos);
            int lineStart;
            if (dir < 0) {
                lineStart = this.computeLineStart(this.cursorPos);
                if (lineStart == 0) {
                    this.setCursorPos(0, select);
                    return;
                }
                lineStart = this.computeLineStart(lineStart - 1);
            }
            else {
                lineStart = Math.min(this.computeLineEnd(this.cursorPos) + 1, this.editBuffer.length());
            }
            this.setCursorPos(this.computeCursorPosFromX(x, lineStart), select);
        }
    }

    protected void setCursorPos(int pos, final boolean select) {
        pos = Math.max(0, Math.min(this.editBuffer.length(), pos));
        if (!select) {
            final boolean hadSelection = this.hasSelection();
            this.selectionStart = pos;
            this.selectionEnd = pos;
            if (hadSelection) {
                this.updateSelection();
            }
        }
        if (this.cursorPos != pos) {
            if (select) {
                if (this.hasSelection()) {
                    if (this.cursorPos == this.selectionStart) {
                        this.selectionStart = pos;
                    }
                    else {
                        this.selectionEnd = pos;
                    }
                }
                else {
                    this.selectionStart = this.cursorPos;
                    this.selectionEnd = pos;
                }
                if (this.selectionStart > this.selectionEnd) {
                    final int t = this.selectionStart;
                    this.selectionStart = this.selectionEnd;
                    this.selectionEnd = t;
                }
                this.updateSelection();
            }
            if (this.cursorPos != pos) {
                this.getAnimationState().resetAnimationTime(EditField.STATE_CURSOR_MOVED);
            }
            this.cursorPos = pos;
            this.scrollToCursor(false);
            this.updateAutoCompletion();
        }
    }

    protected void updateSelection() {
        if (this.attributes != null) {
            this.attributes.removeAnimationState(TextWidget.STATE_TEXT_SELECTION);
            this.attributes.setAnimationState(TextWidget.STATE_TEXT_SELECTION, this.selectionStart, this.selectionEnd, true);
            this.attributes.optimize();
            this.textRenderer.cacheDirty = true;
        }
    }

    public void setCursorPos(final int pos) {
        if (pos < 0 || pos > this.editBuffer.length()) {
            throw new IllegalArgumentException("pos");
        }
        this.setCursorPos(pos, false);
    }

    public void selectAll() {
        this.selectionStart = 0;
        this.selectionEnd = this.editBuffer.length();
        this.updateSelection();
    }

    public void setSelection(final int start, final int end) {
        if (start < 0 || start > end || end > this.editBuffer.length()) {
            throw new IllegalArgumentException();
        }
        this.selectionStart = start;
        this.selectionEnd = end;
        this.updateSelection();
    }

    protected void selectWordFromMouse(final int index) {
        this.selectionStart = index;
        this.selectionEnd = index;
        while (this.selectionStart > 0 && !Character.isWhitespace(this.editBuffer.charAt(this.selectionStart - 1))) {
            --this.selectionStart;
        }
        while (this.selectionEnd < this.editBuffer.length() && !Character.isWhitespace(this.editBuffer.charAt(this.selectionEnd))) {
            ++this.selectionEnd;
        }
        this.updateSelection();
    }

    protected void scrollToCursor(final boolean force) {
        final int renderWidth = this.textRenderer.getWidth() - 5;
        if (renderWidth <= 0) {
            this.pendingScrollToCursor = true;
            this.pendingScrollToCursorForce = force;
            return;
        }
        this.pendingScrollToCursor = false;
        final int xpos = this.computeRelativeCursorPositionX(this.cursorPos);
        if (xpos < this.scrollPos + 5) {
            this.scrollPos = Math.max(0, xpos - 5);
        }
        else if (force || xpos - this.scrollPos > renderWidth) {
            this.scrollPos = Math.max(0, xpos - renderWidth);
        }
        if (this.multiLine) {
            final ScrollPane sp = ScrollPane.getContainingScrollPane(this);
            if (sp != null) {
                final int lineHeight = this.getLineHeight();
                final int lineY = this.computeLineNumber(this.cursorPos) * lineHeight;
                sp.validateLayout();
                sp.scrollToAreaY(lineY, lineHeight, lineHeight / 2);
            }
        }
    }

    protected void insertChar(final char ch) {
        if (!this.readOnly && (!Character.isISOControl(ch) || (this.multiLine && ch == '\n'))) {
            boolean update = false;
            if (this.hasSelection()) {
                this.deleteSelection();
                update = true;
            }
            if (this.editBuffer.length() < this.maxTextLength && this.editBuffer.replace(this.cursorPos, 0, ch)) {
                ++this.cursorPos;
                update = true;
            }
            if (update) {
                this.updateText(true, false, 0);
            }
        }
    }

    protected void deletePrev() {
        if (!this.readOnly) {
            if (this.hasSelection()) {
                this.deleteSelection();
                this.updateText(true, false, 211);
            }
            else if (this.cursorPos > 0) {
                --this.cursorPos;
                this.deleteNext();
            }
        }
    }

    protected void deleteNext() {
        if (!this.readOnly) {
            if (this.hasSelection()) {
                this.deleteSelection();
                this.updateText(true, false, 211);
            }
            else if (this.cursorPos < this.editBuffer.length() && this.editBuffer.replace(this.cursorPos, 1, "") >= 0) {
                this.updateText(true, false, 211);
            }
        }
    }

    protected void deleteSelection() {
        if (this.editBuffer.replace(this.selectionStart, this.selectionEnd - this.selectionStart, "") >= 0) {
            this.setCursorPos(this.selectionStart, false);
        }
    }

    protected void modelChanged() {
        final String modelText = this.model.getValue();
        if (this.editBuffer.length() != modelText.length() || !this.getText().equals(modelText)) {
            this.setText(modelText, true);
        }
    }

    protected boolean hasFocusOrPopup() {
        return this.hasKeyboardFocus() || this.hasOpenPopups();
    }

    protected Font getFont() {
        return this.textRenderer.getFont();
    }

    protected int getLineHeight() {
        final Font font = this.getFont();
        if (font != null) {
            return font.getLineHeight();
        }
        return 0;
    }

    protected int computeLineNumber(final int cursorPos) {
        final EditFieldModel eb = this.editBuffer;
        int lineNr = 0;
        for (int i = 0; i < cursorPos; ++i) {
            if (eb.charAt(i) == '\n') {
                ++lineNr;
            }
        }
        return lineNr;
    }

    protected int computeLineStart(int cursorPos) {
        if (!this.multiLine) {
            return 0;
        }
        for (EditFieldModel eb = this.editBuffer; cursorPos > 0 && eb.charAt(cursorPos - 1) != '\n'; --cursorPos) {}
        return cursorPos;
    }

    protected int computeLineEnd(int cursorPos) {
        final EditFieldModel eb = this.editBuffer;
        final int endIndex = eb.length();
        if (!this.multiLine) {
            return endIndex;
        }
        while (cursorPos < endIndex && eb.charAt(cursorPos) != '\n') {
            ++cursorPos;
        }
        return cursorPos;
    }

    protected int computeRelativeCursorPositionX(final int cursorPos) {
        int lineStart = 0;
        if (this.multiLine) {
            lineStart = this.computeLineStart(cursorPos);
        }
        return this.textRenderer.computeRelativeCursorPositionX(lineStart, cursorPos);
    }

    protected int computeRelativeCursorPositionY(final int cursorPos) {
        if (this.multiLine) {
            return this.getLineHeight() * this.computeLineNumber(cursorPos);
        }
        return 0;
    }

    protected int getCursorPosFromMouse(int x, int y) {
        final Font font = this.getFont();
        if (font != null) {
            x -= this.textRenderer.lastTextX;
            int lineStart = 0;
            int lineEnd = this.editBuffer.length();
            if (this.multiLine) {
                y -= this.textRenderer.computeTextY();
                final int lineHeight = font.getLineHeight();
                final int endIndex = lineEnd;
                while (true) {
                    lineEnd = this.computeLineEnd(lineStart);
                    if (lineStart >= endIndex) {
                        break;
                    }
                    if (y < lineHeight) {
                        break;
                    }
                    lineStart = Math.min(lineEnd + 1, endIndex);
                    y -= lineHeight;
                }
            }
            return this.computeCursorPosFromX(x, lineStart, lineEnd);
        }
        return 0;
    }

    protected int computeCursorPosFromX(final int x, final int lineStart) {
        return this.computeCursorPosFromX(x, lineStart, this.computeLineEnd(lineStart));
    }

    protected int computeCursorPosFromX(final int x, final int lineStart, final int lineEnd) {
        final Font font = this.getFont();
        if (font != null) {
            return lineStart + font.computeVisibleGlpyhs((CharSequence)((this.passwordMasking != null) ? this.passwordMasking : this.editBuffer), lineStart, lineEnd, x + font.getSpaceWidth() / 2);
        }
        return lineStart;
    }

    @Override
    protected void paintOverlay(final GUI gui) {
        if (this.cursorImage != null && this.hasFocusOrPopup()) {
            final int xpos = this.textRenderer.lastTextX + this.computeRelativeCursorPositionX(this.cursorPos);
            final int ypos = this.textRenderer.computeTextY() + this.computeRelativeCursorPositionY(this.cursorPos);
            this.cursorImage.draw((AnimationState)this.getAnimationState(), xpos, ypos, this.cursorImage.getWidth(), this.getLineHeight());
        }
        super.paintOverlay(gui);
    }

    private void openErrorInfoWindow() {
        if (this.autoCompletionWindow == null || !this.autoCompletionWindow.isOpen()) {
            if (this.errorInfoWindow == null) {
                (this.errorInfoLabel = new Label()).setClip(true);
                (this.errorInfoWindow = new InfoWindow(this)).setTheme("editfield-errorinfowindow");
                this.errorInfoWindow.add((Widget)this.errorInfoLabel);
            }
            this.errorInfoLabel.setText(this.errorMsg.toString());
            this.errorInfoWindow.openInfo();
            this.layoutErrorInfoWindow();
        }
    }

    private void layoutErrorInfoWindow() {
        int x = this.getX();
        int width = this.getWidth();
        final Widget container = this.errorInfoWindow.getParent();
        if (container != null) {
            width = Math.max(width, Widget.computeSize(this.errorInfoWindow.getMinWidth(), this.errorInfoWindow.getPreferredWidth(), this.errorInfoWindow.getMaxWidth()));
            final int popupMaxRight = container.getInnerRight();
            if (x + width > popupMaxRight) {
                x = popupMaxRight - Math.min(width, container.getInnerWidth());
            }
            this.errorInfoWindow.setSize(width, this.errorInfoWindow.getPreferredHeight());
            this.errorInfoWindow.setPosition(x, this.getBottom());
        }
    }

    @Override
    protected void keyboardFocusGained() {
        if (this.errorMsg != null) {
            this.openErrorInfoWindow();
        }
        else {
            this.updateAutoCompletion();
        }
    }

    @Override
    protected void keyboardFocusLost() {
        super.keyboardFocusLost();
        if (this.errorInfoWindow != null) {
            this.errorInfoWindow.closeInfo();
        }
        if (this.autoCompletionWindow != null) {
            this.autoCompletionWindow.closeInfo();
        }
    }

    protected void updateAutoCompletion() {
        if (this.autoCompletionWindow != null) {
            this.autoCompletionWindow.updateAutoCompletion();
        }
    }

    static {
        STATE_ERROR = AnimationState.StateKey.get("error");
        STATE_READONLY = AnimationState.StateKey.get("readonly");
        STATE_HOVER = AnimationState.StateKey.get("hover");
        STATE_CURSOR_MOVED = AnimationState.StateKey.get("cursorMoved");
    }

    protected class ModelChangeListener implements Runnable
    {
        @Override
        public void run() {
            EditField.this.modelChanged();
        }
    }

    protected class TextRenderer extends TextWidget
    {
        int lastTextX;
        int lastScrollPos;
        AttributedStringFontCache cache;
        boolean cacheDirty;

        protected TextRenderer(final de.matthiasmann.twl.AnimationState animState) {
            super(animState);
        }

        @Override
        protected void paintWidget(final GUI gui) {
            if (EditField.this.pendingScrollToCursor) {
                EditField.this.scrollToCursor(EditField.this.pendingScrollToCursorForce);
            }
            this.lastScrollPos = (EditField.this.hasFocusOrPopup() ? EditField.this.scrollPos : 0);
            this.lastTextX = this.computeTextX();
            final Font font = this.getFont();
            if (EditField.this.attributes != null && font instanceof Font2) {
                this.paintWithAttributes((Font2)font);
            }
            else if (EditField.this.hasSelection() && EditField.this.hasFocusOrPopup()) {
                if (EditField.this.multiLine) {
                    this.paintMultiLineWithSelection();
                }
                else {
                    this.paintWithSelection(0, EditField.this.editBuffer.length(), this.computeTextY());
                }
            }
            else {
                this.paintLabelText((AnimationState)this.getAnimationState());
            }
        }

        protected void paintWithSelection(final int lineStart, final int lineEnd, final int yoff) {
            final int selStart = EditField.this.selectionStart;
            final int selEnd = EditField.this.selectionEnd;
            if (EditField.this.selectionImage != null && selEnd > lineStart && selStart <= lineEnd) {
                final int xpos0 = this.lastTextX + this.computeRelativeCursorPositionX(lineStart, selStart);
                final int xpos2 = (lineEnd < selEnd) ? this.getInnerRight() : (this.lastTextX + this.computeRelativeCursorPositionX(lineStart, Math.min(lineEnd, selEnd)));
                EditField.this.selectionImage.draw((AnimationState)this.getAnimationState(), xpos0, yoff, xpos2 - xpos0, this.getFont().getLineHeight());
            }
            this.paintWithSelection(this.getAnimationState(), selStart, selEnd, lineStart, lineEnd, yoff);
        }

        protected void paintMultiLineWithSelection() {
            final EditFieldModel eb = EditField.this.editBuffer;
            int lineStart = 0;
            final int endIndex = eb.length();
            int yoff = this.computeTextY();
            final int lineHeight = EditField.this.getLineHeight();
            while (lineStart < endIndex) {
                final int lineEnd = EditField.this.computeLineEnd(lineStart);
                this.paintWithSelection(lineStart, lineEnd, yoff);
                yoff += lineHeight;
                lineStart = lineEnd + 1;
            }
        }

        protected void paintMultiLineSelectionBackground() {
            final int lineHeight = EditField.this.getLineHeight();
            int lineStart = EditField.this.computeLineStart(EditField.this.selectionStart);
            final int lineNumber = EditField.this.computeLineNumber(lineStart);
            final int endIndex = EditField.this.selectionEnd;
            int yoff = this.computeTextY() + lineHeight * lineNumber;
            int xstart = this.lastTextX + this.computeRelativeCursorPositionX(lineStart, EditField.this.selectionStart);
            while (lineStart < endIndex) {
                final int lineEnd = EditField.this.computeLineEnd(lineStart);
                int xend;
                if (lineEnd < endIndex) {
                    xend = this.getInnerRight();
                }
                else {
                    xend = this.lastTextX + this.computeRelativeCursorPositionX(lineStart, endIndex);
                }
                EditField.this.selectionImage.draw((AnimationState)this.getAnimationState(), xstart, yoff, xend - xstart, lineHeight);
                yoff += lineHeight;
                lineStart = lineEnd + 1;
                xstart = this.getInnerX();
            }
        }

        protected void paintWithAttributes(final Font2 font) {
            if (EditField.this.selectionEnd > EditField.this.selectionStart && EditField.this.selectionImage != null) {
                this.paintMultiLineSelectionBackground();
            }
            if (this.cache == null || this.cacheDirty) {
                this.cacheDirty = false;
                if (EditField.this.multiLine) {
                    this.cache = font.cacheMultiLineText(this.cache, EditField.this.attributes);
                }
                else {
                    this.cache = font.cacheText(this.cache, EditField.this.attributes);
                }
            }
            final int y = this.computeTextY();
            if (this.cache != null) {
                this.cache.draw(this.lastTextX, y);
            }
            else if (EditField.this.multiLine) {
                font.drawMultiLineText(this.lastTextX, y, EditField.this.attributes);
            }
            else {
                font.drawText(this.lastTextX, y, EditField.this.attributes);
            }
        }

        @Override
        protected void sizeChanged() {
            if (EditField.this.scrollToCursorOnSizeChange) {
                EditField.this.scrollToCursor(true);
            }
        }

        @Override
        protected int computeTextX() {
            int x = this.getInnerX();
            final int pos = this.getAlignment().hpos;
            if (pos > 0) {
                x += Math.max(0, this.getInnerWidth() - this.computeTextWidth()) * pos / 2;
            }
            return x - this.lastScrollPos;
        }

        @Override
        public void destroy() {
            super.destroy();
            if (this.cache != null) {
                this.cache.destroy();
                this.cache = null;
            }
        }
    }

    static class PasswordMasker implements CharSequence
    {
        final CharSequence base;
        final char maskingChar;

        public PasswordMasker(final CharSequence base, final char maskingChar) {
            this.base = base;
            this.maskingChar = maskingChar;
        }

        @Override
        public int length() {
            return this.base.length();
        }

        @Override
        public char charAt(final int index) {
            return this.maskingChar;
        }

        @Override
        public CharSequence subSequence(final int start, final int end) {
            throw new UnsupportedOperationException("Not supported.");
        }
    }

    public interface Callback
    {
        void callback(final int p0);
    }
}
