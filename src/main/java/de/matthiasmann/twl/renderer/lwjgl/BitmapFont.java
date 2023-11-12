package de.matthiasmann.twl.renderer.lwjgl;

import java.net.*;
import org.xmlpull.v1.*;
import java.util.*;
import java.io.*;
import de.matthiasmann.twl.*;
import org.lwjgl.opengl.*;
import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.utils.*;
import java.nio.*;

public class BitmapFont
{
    private static final int LOG2_PAGE_SIZE = 9;
    private static final int PAGE_SIZE = 512;
    private static final int PAGES = 128;
    private final LWJGLTexture texture;
    private final Glyph[][] glyphs;
    private final int lineHeight;
    private final int baseLine;
    private final int spaceWidth;
    private final int ex;
    private final boolean proportional;
    
    public BitmapFont(final LWJGLRenderer renderer, final XMLParser xmlp, final URL baseUrl) throws XmlPullParserException, IOException {
        xmlp.require(2, null, "font");
        xmlp.nextTag();
        xmlp.require(2, null, "info");
        xmlp.ignoreOtherAttributes();
        xmlp.nextTag();
        xmlp.require(3, null, "info");
        xmlp.nextTag();
        xmlp.require(2, null, "common");
        this.lineHeight = xmlp.parseIntFromAttribute("lineHeight");
        this.baseLine = xmlp.parseIntFromAttribute("base");
        if (xmlp.parseIntFromAttribute("pages", 1) != 1) {
            throw new UnsupportedOperationException("multi page fonts not supported");
        }
        if (xmlp.parseIntFromAttribute("packed", 0) != 0) {
            throw new UnsupportedOperationException("packed fonts not supported");
        }
        xmlp.ignoreOtherAttributes();
        xmlp.nextTag();
        xmlp.require(3, null, "common");
        xmlp.nextTag();
        xmlp.require(2, null, "pages");
        xmlp.nextTag();
        xmlp.require(2, null, "page");
        final int pageId = Integer.parseInt(xmlp.getAttributeValue(null, "id"));
        if (pageId != 0) {
            throw new UnsupportedOperationException("only page id 0 supported");
        }
        final String textureName = xmlp.getAttributeValue(null, "file");
        Util.checkGLError();
        this.texture = renderer.load(new URL(baseUrl, textureName), LWJGLTexture.Format.ALPHA, LWJGLTexture.Filter.NEAREST);
        Util.checkGLError();
        xmlp.nextTag();
        xmlp.require(3, null, "page");
        xmlp.nextTag();
        xmlp.require(3, null, "pages");
        xmlp.nextTag();
        xmlp.require(2, null, "chars");
        xmlp.ignoreOtherAttributes();
        xmlp.nextTag();
        int firstXAdvance = Integer.MIN_VALUE;
        boolean prop = true;
        this.glyphs = new Glyph[128][];
        while (!xmlp.isEndTag()) {
            xmlp.require(2, null, "char");
            final int idx = xmlp.parseIntFromAttribute("id");
            final int x = xmlp.parseIntFromAttribute("x");
            final int y = xmlp.parseIntFromAttribute("y");
            final int w = xmlp.parseIntFromAttribute("width");
            final int h = xmlp.parseIntFromAttribute("height");
            if (xmlp.parseIntFromAttribute("page", 0) != 0) {
                throw xmlp.error("Multiple pages not supported");
            }
            final int chnl = xmlp.parseIntFromAttribute("chnl", 0);
            final Glyph g = new Glyph(x, y, w, h, this.texture.getTexWidth(), this.texture.getTexHeight());
            g.xoffset = Short.parseShort(xmlp.getAttributeNotNull("xoffset"));
            g.yoffset = Short.parseShort(xmlp.getAttributeNotNull("yoffset"));
            g.xadvance = Short.parseShort(xmlp.getAttributeNotNull("xadvance"));
            this.addGlyph(idx, g);
            xmlp.nextTag();
            xmlp.require(3, null, "char");
            xmlp.nextTag();
            if (g.xadvance == firstXAdvance || g.xadvance <= 0) {
                continue;
            }
            if (firstXAdvance == Integer.MIN_VALUE) {
                firstXAdvance = g.xadvance;
            }
            else {
                prop = false;
            }
        }
        xmlp.require(3, null, "chars");
        xmlp.nextTag();
        if (xmlp.isStartTag()) {
            xmlp.require(2, null, "kernings");
            xmlp.ignoreOtherAttributes();
            xmlp.nextTag();
            while (!xmlp.isEndTag()) {
                xmlp.require(2, null, "kerning");
                final int first = xmlp.parseIntFromAttribute("first");
                final int second = xmlp.parseIntFromAttribute("second");
                final int amount = xmlp.parseIntFromAttribute("amount");
                this.addKerning(first, second, amount);
                xmlp.nextTag();
                xmlp.require(3, null, "kerning");
                xmlp.nextTag();
            }
            xmlp.require(3, null, "kernings");
            xmlp.nextTag();
        }
        xmlp.require(3, null, "font");
        final Glyph g2 = this.getGlyph(' ');
        this.spaceWidth = ((g2 != null) ? (g2.xadvance + g2.width) : 1);
        final Glyph gx = this.getGlyph('x');
        this.ex = ((gx != null) ? gx.height : 1);
        this.proportional = prop;
    }
    
    public BitmapFont(final LWJGLRenderer renderer, final Reader reader, final URL baseUrl) throws IOException {
        final BufferedReader br = new BufferedReader(reader);
        final HashMap<String, String> params = new HashMap<String, String>();
        parseFntLine(br, "info");
        parseFntLine(parseFntLine(br, "common"), params);
        this.lineHeight = parseInt(params, "lineHeight");
        this.baseLine = parseInt(params, "base");
        if (parseInt(params, "pages", 1) != 1) {
            throw new UnsupportedOperationException("multi page fonts not supported");
        }
        if (parseInt(params, "packed", 0) != 0) {
            throw new UnsupportedOperationException("packed fonts not supported");
        }
        parseFntLine(parseFntLine(br, "page"), params);
        if (parseInt(params, "id", 0) != 0) {
            throw new UnsupportedOperationException("only page id 0 supported");
        }
        this.texture = renderer.load(new URL(baseUrl, getParam(params, "file")), LWJGLTexture.Format.ALPHA, LWJGLTexture.Filter.NEAREST);
        this.glyphs = new Glyph[128][];
        parseFntLine(parseFntLine(br, "chars"), params);
        final int charCount = parseInt(params, "count");
        int firstXAdvance = Integer.MIN_VALUE;
        boolean prop = true;
        for (int charIdx = 0; charIdx < charCount; ++charIdx) {
            parseFntLine(parseFntLine(br, "char"), params);
            final int idx = parseInt(params, "id");
            final int x = parseInt(params, "x");
            final int y = parseInt(params, "y");
            final int w = parseInt(params, "width");
            final int h = parseInt(params, "height");
            if (parseInt(params, "page", 0) != 0) {
                throw new IOException("Multiple pages not supported");
            }
            final Glyph g = new Glyph(x, y, w, h, this.texture.getTexWidth(), this.texture.getTexHeight());
            g.xoffset = parseShort(params, "xoffset");
            g.yoffset = parseShort(params, "yoffset");
            g.xadvance = parseShort(params, "xadvance");
            this.addGlyph(idx, g);
            if (g.xadvance != firstXAdvance && g.xadvance > 0) {
                if (firstXAdvance == Integer.MIN_VALUE) {
                    firstXAdvance = g.xadvance;
                }
                else {
                    prop = false;
                }
            }
        }
        parseFntLine(parseFntLine(br, "kernings"), params);
        for (int kerningCount = parseInt(params, "count"), kerningIdx = 0; kerningIdx < kerningCount; ++kerningIdx) {
            parseFntLine(parseFntLine(br, "kerning"), params);
            final int first = parseInt(params, "first");
            final int second = parseInt(params, "second");
            final int amount = parseInt(params, "amount");
            this.addKerning(first, second, amount);
        }
        final Glyph g2 = this.getGlyph(' ');
        this.spaceWidth = ((g2 != null) ? (g2.xadvance + g2.width) : 1);
        final Glyph gx = this.getGlyph('x');
        this.ex = ((gx != null) ? gx.height : 1);
        this.proportional = prop;
    }
    
    public static BitmapFont loadFont(final LWJGLRenderer renderer, final URL url) throws IOException {
        boolean startTagSeen = false;
        try {
            final XMLParser xmlp = new XMLParser(url);
            try {
                xmlp.require(0, null, null);
                xmlp.nextTag();
                startTagSeen = true;
                Util.checkGLError();
                return new BitmapFont(renderer, xmlp, url);
            }
            finally {
                xmlp.close();
            }
        }
        catch (XmlPullParserException ex) {
            if (startTagSeen) {
                throw (IOException)new IOException().initCause((Throwable)ex);
            }
            final InputStream is = url.openStream();
            try {
                final InputStreamReader isr = new InputStreamReader(is, "UTF8");
                return new BitmapFont(renderer, isr, url);
            }
            finally {
                is.close();
            }
        }
    }
    
    public boolean isProportional() {
        return this.proportional;
    }
    
    public int getBaseLine() {
        return this.baseLine;
    }
    
    public int getLineHeight() {
        return this.lineHeight;
    }
    
    public int getSpaceWidth() {
        return this.spaceWidth;
    }
    
    public int getEM() {
        return this.lineHeight;
    }
    
    public int getEX() {
        return this.ex;
    }
    
    public void destroy() {
        this.texture.destroy();
    }
    
    private void addGlyph(final int idx, final Glyph g) {
        if (idx <= 65535) {
            Glyph[] page = this.glyphs[idx >> 9];
            if (page == null) {
                page = (this.glyphs[idx >> 9] = new Glyph[512]);
            }
            page[idx & 0x1FF] = g;
        }
    }
    
    private void addKerning(final int first, final int second, final int amount) {
        if (first >= 0 && first <= 65535 && second >= 0 && second <= 65535) {
            final Glyph g = this.getGlyph((char)first);
            if (g != null) {
                g.setKerning(second, amount);
            }
        }
    }
    
    final Glyph getGlyph(final char ch) {
        final Glyph[] page = this.glyphs[ch >> 9];
        if (page != null) {
            return page[ch & '\u01ff'];
        }
        return null;
    }
    
    public int computeTextWidth(final CharSequence str, int start, final int end) {
        int width = 0;
        Glyph lastGlyph = null;
        while (start < end) {
            lastGlyph = this.getGlyph(str.charAt(start++));
            if (lastGlyph != null) {
                width = lastGlyph.xadvance;
                break;
            }
        }
        while (start < end) {
            final char ch = str.charAt(start++);
            final Glyph g = this.getGlyph(ch);
            if (g != null) {
                width += lastGlyph.getKerning(ch);
                lastGlyph = g;
                width += g.xadvance;
            }
        }
        return width;
    }
    
    public int computeVisibleGlpyhs(final CharSequence str, final int start, final int end, final int availWidth) {
        int index = start;
        int width = 0;
        Glyph lastGlyph = null;
        while (index < end) {
            final char ch = str.charAt(index);
            final Glyph g = this.getGlyph(ch);
            if (g != null) {
                if (lastGlyph != null) {
                    width += lastGlyph.getKerning(ch);
                }
                lastGlyph = g;
                if (this.proportional) {
                    width += g.xadvance;
                    if (width > availWidth) {
                        break;
                    }
                }
                else {
                    if (width + g.width + g.xoffset > availWidth) {
                        break;
                    }
                    width += g.xadvance;
                }
            }
            ++index;
        }
        return index - start;
    }
    
    protected int drawText(int x, final int y, final CharSequence str, int start, final int end) {
        final int startX = x;
        Glyph lastGlyph = null;
        while (start < end) {
            lastGlyph = this.getGlyph(str.charAt(start++));
            if (lastGlyph != null) {
                if (lastGlyph.width > 0) {
                    lastGlyph.draw(x, y);
                }
                x += lastGlyph.xadvance;
                break;
            }
        }
        while (start < end) {
            final char ch = str.charAt(start++);
            final Glyph g = this.getGlyph(ch);
            if (g != null) {
                x += lastGlyph.getKerning(ch);
                lastGlyph = g;
                if (g.width > 0) {
                    g.draw(x, y);
                }
                x += g.xadvance;
            }
        }
        return x - startX;
    }
    
    protected int drawMultiLineText(final int x, int y, final CharSequence str, final int width, final HAlignment align) {
        int start;
        int numLines;
        int lineEnd;
        for (start = 0, numLines = 0; start < str.length(); start = lineEnd + 1, y += this.lineHeight, ++numLines) {
            lineEnd = TextUtil.indexOf(str, '\n', start);
            int xoff = 0;
            if (align != HAlignment.LEFT) {
                final int lineWidth = this.computeTextWidth(str, start, lineEnd);
                xoff = width - lineWidth;
                if (align == HAlignment.CENTER) {
                    xoff /= 2;
                }
            }
            this.drawText(x + xoff, y, str, start, lineEnd);
        }
        return numLines;
    }
    
    public void computeMultiLineInfo(final CharSequence str, final int width, final HAlignment align, final int[] multiLineInfo) {
        int start = 0;
        int idx = 0;
        while (start < str.length()) {
            final int lineEnd = TextUtil.indexOf(str, '\n', start);
            final int lineWidth = this.computeTextWidth(str, start, lineEnd);
            int xoff = width - lineWidth;
            if (align == HAlignment.LEFT) {
                xoff = 0;
            }
            else if (align == HAlignment.CENTER) {
                xoff /= 2;
            }
            multiLineInfo[idx++] = (lineWidth << 16 | (xoff & 0xFFFF));
            start = lineEnd + 1;
        }
    }
    
    protected void beginLine() {
        GL11.glDisable(3553);
        GL11.glBegin(7);
    }
    
    protected void endLine() {
        GL11.glEnd();
        GL11.glEnable(3553);
    }
    
    public void drawMultiLineLines(final int x, int y, final int[] multiLineInfo, final int numLines) {
        this.beginLine();
        try {
            for (final int info : multiLineInfo) {
                final int xoff = x + (short)info;
                final int lineWidth = info >>> 16;
                GL11.glVertex2i(xoff, y);
                GL11.glVertex2i(xoff + lineWidth, y);
                GL11.glVertex2i(xoff + lineWidth, y + 1);
                GL11.glVertex2i(xoff, y + 1);
                y += this.lineHeight;
            }
        }
        finally {
            this.endLine();
        }
    }
    
    public void drawLine(final int x0, final int y, final int x1) {
        this.beginLine();
        GL11.glVertex2i(x0, y);
        GL11.glVertex2i(x1, y);
        GL11.glVertex2i(x1, y + 1);
        GL11.glVertex2i(x0, y + 1);
        this.endLine();
    }
    
    public int computeMultiLineTextWidth(final CharSequence str) {
        int start = 0;
        int width = 0;
        while (start < str.length()) {
            final int lineEnd = TextUtil.indexOf(str, '\n', start);
            final int lineWidth = this.computeTextWidth(str, start, lineEnd);
            width = Math.max(width, lineWidth);
            start = lineEnd + 1;
        }
        return width;
    }
    
    public FontCache cacheMultiLineText(final LWJGLFontCache cache, final CharSequence str, final int width, final HAlignment align) {
        if (cache.startCompile()) {
            int numLines = 0;
            try {
                if (this.prepare()) {
                    try {
                        numLines = this.drawMultiLineText(0, 0, str, width, align);
                    }
                    finally {
                        this.cleanup();
                    }
                    this.computeMultiLineInfo(str, width, align, cache.getMultiLineInfo(numLines));
                }
            }
            finally {
                cache.endCompile(width, numLines * this.lineHeight);
            }
            return (FontCache)cache;
        }
        return null;
    }
    
    public FontCache cacheText(final LWJGLFontCache cache, final CharSequence str, final int start, final int end) {
        if (cache.startCompile()) {
            int width = 0;
            try {
                if (this.prepare()) {
                    try {
                        width = this.drawText(0, 0, str, start, end);
                    }
                    finally {
                        this.cleanup();
                    }
                }
            }
            finally {
                cache.endCompile(width, this.getLineHeight());
            }
            return (FontCache)cache;
        }
        return null;
    }
    
    boolean bind() {
        return this.texture.bind();
    }
    
    protected boolean prepare() {
        if (this.texture.bind()) {
            GL11.glBegin(7);
            return true;
        }
        return false;
    }
    
    protected void cleanup() {
        GL11.glEnd();
    }
    
    private static String parseFntLine(final BufferedReader br, final String tag) throws IOException {
        final String line = br.readLine();
        if (line == null || line.length() <= tag.length() || line.charAt(tag.length()) != ' ' || !line.startsWith(tag)) {
            throw new IOException("'" + tag + "' line expected");
        }
        return line;
    }
    
    private static void parseFntLine(final String line, final HashMap<String, String> params) {
        params.clear();
        final ParameterStringParser psp = new ParameterStringParser(line, ' ', '=');
        while (psp.next()) {
            params.put(psp.getKey(), psp.getValue());
        }
    }
    
    private static String getParam(final HashMap<String, String> params, final String key) throws IOException {
        final String value = params.get(key);
        if (value == null) {
            throw new IOException("Required parameter '" + key + "' not found");
        }
        return value;
    }
    
    private static int parseInt(final HashMap<String, String> params, final String key) throws IOException {
        final String value = getParam(params, key);
        try {
            return Integer.parseInt(value);
        }
        catch (IllegalArgumentException ex) {
            throw canParseParam(key, value, ex);
        }
    }
    
    private static int parseInt(final HashMap<String, String> params, final String key, final int defaultValue) throws IOException {
        final String value = params.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        }
        catch (IllegalArgumentException ex) {
            throw canParseParam(key, value, ex);
        }
    }
    
    private static short parseShort(final HashMap<String, String> params, final String key) throws IOException {
        final String value = getParam(params, key);
        try {
            return Short.parseShort(value);
        }
        catch (IllegalArgumentException ex) {
            throw canParseParam(key, value, ex);
        }
    }
    
    private static IOException canParseParam(final String key, final String value, final IllegalArgumentException ex) {
        return (IOException)new IOException("Can't parse parameter: " + key + '=' + value).initCause(ex);
    }
    
    static class Glyph extends TextureAreaBase
    {
        short xoffset;
        short yoffset;
        short xadvance;
        byte[][] kerning;
        
        public Glyph(final int x, final int y, final int width, final int height, final int texWidth, final int texHeight) {
            super(x, y, (height <= 0) ? 0 : width, height, (float)texWidth, (float)texHeight);
        }
        
        void draw(final int x, final int y) {
            this.drawQuad(x + this.xoffset, y + this.yoffset, this.width, this.height);
        }
        
        void draw(final FloatBuffer va, int x, int y) {
            x += this.xoffset;
            y += this.yoffset;
            va.put(this.tx0).put(this.ty0).put((float)x).put((float)y);
            va.put(this.tx0).put(this.ty1).put((float)x).put((float)(y + this.height));
            va.put(this.tx1).put(this.ty1).put((float)(x + this.width)).put((float)(y + this.height));
            va.put(this.tx1).put(this.ty0).put((float)(x + this.width)).put((float)y);
        }
        
        int getKerning(final char ch) {
            if (this.kerning != null) {
                final byte[] page = this.kerning[ch >>> 9];
                if (page != null) {
                    return page[ch & '\u01ff'];
                }
            }
            return 0;
        }
        
        void setKerning(final int ch, final int value) {
            if (this.kerning == null) {
                this.kerning = new byte[128][];
            }
            byte[] page = this.kerning[ch >>> 9];
            if (page == null) {
                page = (this.kerning[ch >>> 9] = new byte[512]);
            }
            page[ch & 0x1FF] = (byte)value;
        }
    }
}
