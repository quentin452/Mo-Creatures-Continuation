package de.matthiasmann.twl.renderer.lwjgl;

import de.matthiasmann.twl.renderer.AnimationState;
import org.lwjgl.input.*;
import org.lwjgl.*;
import java.net.*;
import de.matthiasmann.twl.utils.*;
import java.io.*;
import java.util.*;
import java.nio.*;
import de.matthiasmann.twl.*;
import org.lwjgl.opengl.*;
import de.matthiasmann.twl.renderer.*;
import java.util.logging.*;

public class LWJGLRenderer implements Renderer, LineRenderer
{
    public static final AnimationState.StateKey STATE_LEFT_MOUSE_BUTTON;
    public static final AnimationState.StateKey STATE_MIDDLE_MOUSE_BUTTON;
    public static final AnimationState.StateKey STATE_RIGHT_MOUSE_BUTTON;
    public static final FontParameter.Parameter<Integer> FONTPARAM_OFFSET_X;
    public static final FontParameter.Parameter<Integer> FONTPARAM_OFFSET_Y;
    public static final FontParameter.Parameter<Integer> FONTPARAM_UNDERLINE_OFFSET;
    private final IntBuffer ib16;
    final int maxTextureSize;
    private int viewportX;
    private int viewportBottom;
    private int width;
    private int height;
    private boolean hasScissor;
    private final TintStack tintStateRoot;
    private final Cursor emptyCursor;
    private boolean useQuadsForLines;
    private boolean useSWMouseCursors;
    private SWCursor swCursor;
    private int mouseX;
    private int mouseY;
    private LWJGLCacheContext cacheContext;
    private FontMapper fontMapper;
    final SWCursorAnimState swCursorAnimState;
    final ArrayList<TextureArea> textureAreas;
    final ArrayList<TextureAreaRotated> rotatedTextureAreas;
    final ArrayList<LWJGLDynamicImage> dynamicImages;
    protected TintStack tintStack;
    protected final ClipStack clipStack;
    protected final Rect clipRectTemp;

    public LWJGLRenderer() throws LWJGLException {
        this.ib16 = BufferUtils.createIntBuffer(16);
        this.textureAreas = new ArrayList<TextureArea>();
        this.rotatedTextureAreas = new ArrayList<TextureAreaRotated>();
        this.dynamicImages = new ArrayList<LWJGLDynamicImage>();
        this.tintStateRoot = new TintStack();
        this.tintStack = this.tintStateRoot;
        this.clipStack = new ClipStack();
        this.clipRectTemp = new Rect();
        this.syncViewportSize();
        GL11.glGetInteger(3379, this.ib16);
        this.maxTextureSize = this.ib16.get(0);
        if (Mouse.isCreated()) {
            final int minCursorSize = Cursor.getMinCursorSize();
            final IntBuffer tmp = BufferUtils.createIntBuffer(minCursorSize * minCursorSize);
            this.emptyCursor = new Cursor(minCursorSize, minCursorSize, minCursorSize / 2, minCursorSize / 2, 1, tmp, (IntBuffer)null);
        }
        else {
            this.emptyCursor = null;
        }
        this.swCursorAnimState = new SWCursorAnimState();
    }

    public boolean isUseQuadsForLines() {
        return this.useQuadsForLines;
    }

    public void setUseQuadsForLines(final boolean useQuadsForLines) {
        this.useQuadsForLines = useQuadsForLines;
    }

    public boolean isUseSWMouseCursors() {
        return this.useSWMouseCursors;
    }

    public void setUseSWMouseCursors(final boolean useSWMouseCursors) {
        this.useSWMouseCursors = useSWMouseCursors;
    }

    @Override
    public CacheContext createNewCacheContext() {
        return (CacheContext)new LWJGLCacheContext(this);
    }

    private LWJGLCacheContext activeCacheContext() {
        if (this.cacheContext == null) {
            this.setActiveCacheContext(this.createNewCacheContext());
        }
        return this.cacheContext;
    }

    @Override
    public CacheContext getActiveCacheContext() {
        return (CacheContext)this.activeCacheContext();
    }

    @Override
    public void setActiveCacheContext(final CacheContext cc) throws IllegalStateException {
        if (cc == null) {
            throw new NullPointerException();
        }
        if (!cc.isValid()) {
            throw new IllegalStateException("CacheContext is invalid");
        }
        if (!(cc instanceof LWJGLCacheContext)) {
            throw new IllegalArgumentException("CacheContext object not from this renderer");
        }
        final LWJGLCacheContext lwjglCC = (LWJGLCacheContext)cc;
        if (lwjglCC.renderer != this) {
            throw new IllegalArgumentException("CacheContext object not from this renderer");
        }
        this.cacheContext = lwjglCC;
        try {
            for (final TextureArea ta : this.textureAreas) {
                ta.destroyRepeatCache();
            }
            for (final TextureAreaRotated tar : this.rotatedTextureAreas) {
                tar.destroyRepeatCache();
            }
        }
        finally {
            this.textureAreas.clear();
            this.rotatedTextureAreas.clear();
        }
    }

    public void syncViewportSize() {
        this.ib16.clear();
        GL11.glGetInteger(2978, this.ib16);
        this.viewportX = this.ib16.get(0);
        this.width = this.ib16.get(2);
        this.height = this.ib16.get(3);
        this.viewportBottom = this.ib16.get(1) + this.height;
    }

    public void setViewport(final int x, final int y, final int width, final int height) {
        this.viewportX = x;
        this.viewportBottom = y + height;
        this.width = width;
        this.height = height;
    }

    @Override
    public long getTimeMillis() {
        final long res = Sys.getTimerResolution();
        long time = Sys.getTime();
        if (res != 1000L) {
            time = time * 1000L / res;
        }
        return time;
    }

    protected void setupGLState() {
        GL11.glPushAttrib(847876);
        GL11.glMatrixMode(5889);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0, (double)this.width, (double)this.height, 0.0, -1.0, 1.0);
        GL11.glMatrixMode(5888);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glEnable(3553);
        GL11.glEnable(3042);
        GL11.glEnable(2848);
        GL11.glDisable(2929);
        GL11.glDisable(2896);
        GL11.glDisable(3089);
        GL11.glBlendFunc(770, 771);
        GL11.glHint(3154, 4354);
    }

    protected void revertGLState() {
        GL11.glPopMatrix();
        GL11.glMatrixMode(5889);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    @Override
    public boolean startRendering() {
        if (this.width <= 0 || this.height <= 0) {
            return false;
        }
        this.prepareForRendering();
        this.setupGLState();
        RenderScale.doscale();
        return true;
    }

    @Override
    public void endRendering() {
        this.renderSWCursor();
        RenderScale.descale();
        this.revertGLState();
    }

    public void pauseRendering() {
        RenderScale.descale();
        this.revertGLState();
    }

    public void resumeRendering() {
        this.hasScissor = false;
        this.setupGLState();
        RenderScale.doscale();
        this.setClipRect();
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    public int getViewportX() {
        return this.viewportX;
    }

    public int getViewportY() {
        return this.viewportBottom - this.height;
    }

    @Override
    public Font loadFont(final URL url, final StateSelect select, final FontParameter... parameterList) throws IOException {
        Util.checkGLError();
        if (url == null) {
            throw new NullPointerException("url");
        }
        if (select == null) {
            throw new NullPointerException("select");
        }
        if (parameterList == null) {
            throw new NullPointerException("parameterList");
        }
        if (select.getNumExpressions() + 1 != parameterList.length) {
            throw new IllegalArgumentException("select.getNumExpressions() + 1 != parameterList.length");
        }
        Util.checkGLError();
        final BitmapFont bmFont = this.activeCacheContext().loadBitmapFont(url);
        Util.checkGLError();
        return (Font)new LWJGLFont(this, bmFont, select, parameterList);
    }

    @Override
    public Texture loadTexture(final URL url, final String formatStr, final String filterStr) throws IOException {
        LWJGLTexture.Format format = LWJGLTexture.Format.COLOR;
        LWJGLTexture.Filter filter = LWJGLTexture.Filter.NEAREST;
        if (formatStr != null) {
            try {
                format = LWJGLTexture.Format.valueOf(formatStr.toUpperCase(Locale.ENGLISH));
            }
            catch (IllegalArgumentException ex) {
                this.getLogger().log(Level.WARNING, "Unknown texture format: {0}", formatStr);
            }
        }
        if (filterStr != null) {
            try {
                filter = LWJGLTexture.Filter.valueOf(filterStr.toUpperCase(Locale.ENGLISH));
            }
            catch (IllegalArgumentException ex) {
                this.getLogger().log(Level.WARNING, "Unknown texture filter: {0}", filterStr);
            }
        }
        return this.load(url, format, filter);
    }

    @Override
    public LineRenderer getLineRenderer() {
        return (LineRenderer)this;
    }

    @Override
    public OffscreenRenderer getOffscreenRenderer() {
        return null;
    }

    @Override
    public FontMapper getFontMapper() {
        return this.fontMapper;
    }

    public void setFontMapper(final FontMapper fontMapper) {
        this.fontMapper = fontMapper;
    }

    @Override
    public DynamicImage createDynamicImage(final int width, final int height) {
        if (width <= 0) {
            throw new IllegalArgumentException("width");
        }
        if (height <= 0) {
            throw new IllegalArgumentException("height");
        }
        if (width > this.maxTextureSize || height > this.maxTextureSize) {
            this.getLogger().log(Level.WARNING, "requested size {0} x {1} exceeds maximum texture size {3}", new Object[] { width, height, this.maxTextureSize });
            return null;
        }
        int texWidth = width;
        int texHeight = height;
        final ContextCapabilities caps = GLContext.getCapabilities();
        final boolean useTextureRectangle = caps.GL_EXT_texture_rectangle || caps.GL_ARB_texture_rectangle;
        if (!useTextureRectangle && !caps.GL_ARB_texture_non_power_of_two) {
            texWidth = nextPowerOf2(width);
            texHeight = nextPowerOf2(height);
        }
        final int proxyTarget = useTextureRectangle ? 34039 : 32868;
        GL11.glTexImage2D(proxyTarget, 0, 6408, texWidth, texHeight, 0, 6408, 5121, (ByteBuffer)null);
        this.ib16.clear();
        GL11.glGetTexLevelParameter(proxyTarget, 0, 4096, this.ib16);
        if (this.ib16.get(0) != texWidth) {
            this.getLogger().log(Level.WARNING, "requested size {0} x {1} failed proxy texture test", new Object[] { texWidth, texHeight });
            return null;
        }
        final int target = useTextureRectangle ? 34037 : 3553;
        final int id = GL11.glGenTextures();
        GL11.glBindTexture(target, id);
        GL11.glTexImage2D(target, 0, 6408, texWidth, texHeight, 0, 6408, 5121, (ByteBuffer)null);
        GL11.glTexParameteri(target, 10240, 9728);
        GL11.glTexParameteri(target, 10241, 9728);
        final LWJGLDynamicImage image = new LWJGLDynamicImage(this, target, id, width, height, texWidth, texHeight, Color.WHITE);
        this.dynamicImages.add(image);
        return (DynamicImage)image;
    }

    @Override
    public Image createGradient(final Gradient gradient) {
        return (Image)new GradientImage(this, gradient);
    }

    @Override
    public void clipEnter(final int x, final int y, final int w, final int h) {
        this.clipStack.push(x, y, w, h);
        this.setClipRect();
    }

    @Override
    public void clipEnter(final Rect rect) {
        this.clipStack.push(rect);
        this.setClipRect();
    }

    @Override
    public void clipLeave() {
        this.clipStack.pop();
        this.setClipRect();
    }

    @Override
    public boolean clipIsEmpty() {
        return this.clipStack.isClipEmpty();
    }

    @Override
    public void setCursor(final MouseCursor cursor) {
        try {
            this.swCursor = null;
            if (this.isMouseInsideWindow()) {
                if (cursor instanceof LWJGLCursor) {
                    this.setNativeCursor(((LWJGLCursor)cursor).cursor);
                }
                else if (cursor instanceof SWCursor) {
                    this.setNativeCursor(this.emptyCursor);
                    this.swCursor = (SWCursor)cursor;
                }
                else {
                    this.setNativeCursor(null);
                }
            }
        }
        catch (LWJGLException ex) {
            this.getLogger().log(Level.WARNING, "Could not set native cursor", (Throwable)ex);
        }
    }

    @Override
    public void setMousePosition(final int mouseX, final int mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    @Override
    public void setMouseButton(final int button, final boolean state) {
        this.swCursorAnimState.setAnimationState(button, state);
    }

    public LWJGLTexture load(final URL textureUrl, final LWJGLTexture.Format fmt, final LWJGLTexture.Filter filter) throws IOException {
        Util.checkGLError();
        return this.load(textureUrl, fmt, filter, null);
    }

    public LWJGLTexture load(final URL textureUrl, final LWJGLTexture.Format fmt, final LWJGLTexture.Filter filter, final TexturePostProcessing tpp) throws IOException {
        if (textureUrl == null) {
            throw new NullPointerException("textureUrl");
        }
        Util.checkGLError();
        final LWJGLCacheContext cc = this.activeCacheContext();
        Util.checkGLError();
        if (tpp != null) {
            Util.checkGLError();
            return cc.createTexture(textureUrl, fmt, filter, tpp);
        }
        Util.checkGLError();
        return cc.loadTexture(textureUrl, fmt, filter);
    }

    @Override
    public void pushGlobalTintColor(final float r, final float g, final float b, final float a) {
        this.tintStack = this.tintStack.push(r, g, b, a);
    }

    @Override
    public void popGlobalTintColor() {
        this.tintStack = this.tintStack.pop();
    }

    public void pushGlobalTintColorReset() {
        this.tintStack = this.tintStack.pushReset();
    }

    public void setColor(final Color color) {
        this.tintStack.setColor(color);
    }

    public void drawLine(final float[] pts, final int numPts, final float width, final Color color, final boolean drawAsLoop) {
        if (numPts * 2 > pts.length) {
            throw new ArrayIndexOutOfBoundsException(numPts * 2);
        }
        if (numPts >= 2) {
            this.tintStack.setColor(color);
            GL11.glDisable(3553);
            if (this.useQuadsForLines) {
                this.drawLinesAsQuads(numPts, pts, width, drawAsLoop);
            }
            else {
                this.drawLinesAsLines(numPts, pts, width, drawAsLoop);
            }
            GL11.glEnable(3553);
        }
    }

    private void drawLinesAsLines(final int numPts, final float[] pts, final float width, final boolean drawAsLoop) {
        GL11.glLineWidth(width);
        GL11.glBegin(drawAsLoop ? 2 : 3);
        for (int i = 0; i < numPts; ++i) {
            GL11.glVertex2f(pts[i * 2 + 0], pts[i * 2 + 1]);
        }
        GL11.glEnd();
    }

    private void drawLinesAsQuads(final int numPts, final float[] pts, float width, final boolean drawAsLoop) {
        width *= 0.5f;
        GL11.glBegin(7);
        for (int i = 1; i < numPts; ++i) {
            drawLineAsQuad(pts[i * 2 - 2], pts[i * 2 - 1], pts[i * 2 + 0], pts[i * 2 + 1], width);
        }
        if (drawAsLoop) {
            final int idx = numPts * 2;
            drawLineAsQuad(pts[idx], pts[idx + 1], pts[0], pts[1], width);
        }
        GL11.glEnd();
    }

    private static void drawLineAsQuad(final float x0, final float y0, final float x1, final float y1, final float w) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        final float l = (float)Math.sqrt(dx * dx + dy * dy) / w;
        dx /= l;
        dy /= l;
        GL11.glVertex2f(x0 - dx + dy, y0 - dy - dx);
        GL11.glVertex2f(x0 - dx - dy, y0 - dy + dx);
        GL11.glVertex2f(x1 + dx - dy, y1 + dy + dx);
        GL11.glVertex2f(x1 + dx + dy, y1 + dy - dx);
    }

    protected void prepareForRendering() {
        this.hasScissor = false;
        this.tintStack = this.tintStateRoot;
        this.clipStack.clearStack();
    }

    protected void renderSWCursor() {
        if (this.swCursor != null) {
            this.tintStack = this.tintStateRoot;
            this.swCursor.render(this.mouseX, this.mouseY);
        }
    }

    protected void setNativeCursor(final Cursor cursor) throws LWJGLException {
        Mouse.setNativeCursor(cursor);
    }

    protected boolean isMouseInsideWindow() {
        return Mouse.isInsideWindow();
    }

    protected void getTintedColor(final Color color, final float[] result) {
        result[0] = this.tintStack.r * color.getRed();
        result[1] = this.tintStack.g * color.getGreen();
        result[2] = this.tintStack.b * color.getBlue();
        result[3] = this.tintStack.a * color.getAlpha();
    }

    protected void getTintedColor(final float[] color, final float[] result) {
        result[0] = this.tintStack.r * color[0];
        result[1] = this.tintStack.g * color[1];
        result[2] = this.tintStack.b * color[2];
        result[3] = this.tintStack.a * color[3];
    }

    public void setClipRect() {
        final Rect rect = this.clipRectTemp;
        if (this.clipStack.getClipRect(rect)) {
            GL11.glScissor(this.viewportX + rect.getX() * RenderScale.scale, this.viewportBottom - rect.getBottom() * RenderScale.scale, rect.getWidth() * RenderScale.scale, rect.getHeight() * RenderScale.scale);
            if (!this.hasScissor) {
                GL11.glEnable(3089);
                this.hasScissor = true;
            }
        }
        else if (this.hasScissor) {
            GL11.glDisable(3089);
            this.hasScissor = false;
        }
    }

    public boolean getClipRect(final Rect rect) {
        return this.clipStack.getClipRect(rect);
    }

    Logger getLogger() {
        return Logger.getLogger(LWJGLRenderer.class.getName());
    }

    private static int nextPowerOf2(int i) {
        i = (--i | i >> 1);
        i |= i >> 2;
        i |= i >> 4;
        i |= i >> 8;
        i |= i >> 16;
        return i + 1;
    }

    static {
        STATE_LEFT_MOUSE_BUTTON = AnimationState.StateKey.get("leftMouseButton");
        STATE_MIDDLE_MOUSE_BUTTON = AnimationState.StateKey.get("middleMouseButton");
        STATE_RIGHT_MOUSE_BUTTON = AnimationState.StateKey.get("rightMouseButton");
        FONTPARAM_OFFSET_X = FontParameter.newParameter("offsetX", 0);
        FONTPARAM_OFFSET_Y = FontParameter.newParameter("offsetY", 0);
        FONTPARAM_UNDERLINE_OFFSET = FontParameter.newParameter("underlineOffset", 0);
    }

    private static class SWCursorAnimState implements AnimationState
    {
        private final long[] lastTime;
        private final boolean[] active;

        SWCursorAnimState() {
            this.lastTime = new long[3];
            this.active = new boolean[3];
        }

        void setAnimationState(final int idx, final boolean isActive) {
            if (idx >= 0 && idx < 3 && this.active[idx] != isActive) {
                this.lastTime[idx] = Sys.getTime();
                this.active[idx] = isActive;
            }
        }

        public int getAnimationTime(final AnimationState.StateKey state) {
            long curTime = Sys.getTime();
            final int idx = this.getMouseButton(state);
            if (idx >= 0) {
                curTime -= this.lastTime[idx];
            }
            return (int)curTime & Integer.MAX_VALUE;
        }

        public boolean getAnimationState(final AnimationState.StateKey state) {
            final int idx = this.getMouseButton(state);
            return idx >= 0 && this.active[idx];
        }

        public boolean getShouldAnimateState(final AnimationState.StateKey state) {
            return true;
        }

        private int getMouseButton(final AnimationState.StateKey key) {
            if (key == LWJGLRenderer.STATE_LEFT_MOUSE_BUTTON) {
                return 0;
            }
            if (key == LWJGLRenderer.STATE_MIDDLE_MOUSE_BUTTON) {
                return 2;
            }
            if (key == LWJGLRenderer.STATE_RIGHT_MOUSE_BUTTON) {
                return 1;
            }
            return -1;
        }
    }
}
