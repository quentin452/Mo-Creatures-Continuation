package de.matthiasmann.twl.theme;

import org.xmlpull.v1.*;
import java.net.*;
import java.io.*;
import de.matthiasmann.twl.*;
import java.util.*;
import de.matthiasmann.twl.utils.*;
import de.matthiasmann.twl.renderer.*;
import java.util.logging.*;

class ImageManager
{
    private final ParameterMapImpl constants;
    private final Renderer renderer;
    private final TreeMap<String, Image> images;
    private final TreeMap<String, MouseCursor> cursors;
    private Texture currentTexture;
    static final EmptyImage NONE;
    static final MouseCursor NOCURSOR;
    private static final int[] SPLIT_WEIGHTS_3;
    private static final int[] SPLIT_WEIGHTS_1;
    
    ImageManager(final ParameterMapImpl constants, final Renderer renderer) {
        this.constants = constants;
        this.renderer = renderer;
        this.images = new TreeMap<String, Image>();
        this.cursors = new TreeMap<String, MouseCursor>();
        this.images.put("none", (Image)ImageManager.NONE);
        this.cursors.put("os-default", ImageManager.NOCURSOR);
    }
    
    Image getImage(final String name) {
        return this.images.get(name);
    }
    
    Image getReferencedImage(final XMLParser xmlp) throws XmlPullParserException {
        final String ref = xmlp.getAttributeNotNull("ref");
        return this.getReferencedImage(xmlp, ref);
    }
    
    Image getReferencedImage(final XMLParser xmlp, final String ref) throws XmlPullParserException {
        if (ref.endsWith(".*")) {
            throw xmlp.error("wildcard mapping not allowed");
        }
        final Image img = this.images.get(ref);
        if (img == null) {
            throw xmlp.error("referenced image \"" + ref + "\" not found");
        }
        return img;
    }
    
    MouseCursor getReferencedCursor(final XMLParser xmlp, final String ref) throws XmlPullParserException {
        final MouseCursor cursor = this.cursors.get(ref);
        if (cursor == null) {
            throw xmlp.error("referenced cursor \"" + ref + "\" not found");
        }
        return this.unwrapCursor(cursor);
    }
    
    Map<String, Image> getImages(final String ref, final String name) {
        return ParserUtil.resolve(this.images, ref, name, null);
    }
    
    public MouseCursor getCursor(final String name) {
        return this.unwrapCursor(this.cursors.get(name));
    }
    
    Map<String, MouseCursor> getCursors(final String ref, final String name) {
        return ParserUtil.resolve(this.cursors, ref, name, ImageManager.NOCURSOR);
    }
    
    void parseImages(final XMLParser xmlp, final URL baseUrl) throws XmlPullParserException, IOException {
        xmlp.require(2, null, null);
        Texture texture = null;
        final String fileName = xmlp.getAttributeValue(null, "file");
        if (fileName != null) {
            final String fmt = xmlp.getAttributeValue(null, "format");
            final String filter = xmlp.getAttributeValue(null, "filter");
            xmlp.getAttributeValue(null, "comment");
            try {
                texture = this.renderer.loadTexture(new URL(baseUrl, fileName), fmt, filter);
                if (texture == null) {
                    throw new NullPointerException("loadTexture returned null");
                }
            }
            catch (IOException ex) {
                throw xmlp.error("Unable to load image file: " + fileName, ex);
            }
        }
        this.currentTexture = texture;
        try {
            xmlp.nextTag();
            while (!xmlp.isEndTag()) {
                final String name = xmlp.getAttributeNotNull("name");
                this.checkImageName(name, xmlp);
                final String tagName = xmlp.getName();
                if ("cursor".equals(xmlp.getName())) {
                    this.parseCursor(xmlp, name);
                }
                else {
                    final Image image = this.parseImage(xmlp, tagName);
                    this.images.put(name, image);
                }
                xmlp.require(3, null, tagName);
                xmlp.nextTag();
            }
        }
        finally {
            this.currentTexture = null;
            if (texture != null) {
                texture.themeLoadingDone();
            }
        }
    }
    
    private MouseCursor unwrapCursor(final MouseCursor cursor) {
        return (cursor == ImageManager.NOCURSOR) ? null : cursor;
    }
    
    private void checkImageName(final String name, final XMLParser xmlp) throws XmlPullParserException {
        ParserUtil.checkNameNotEmpty(name, xmlp);
        if (this.images.containsKey(name)) {
            throw xmlp.error("image \"" + name + "\" already defined");
        }
    }
    
    private Border getBorder(final Image image, Border border) {
        if (border == null && image instanceof HasBorder) {
            border = ((HasBorder)image).getBorder();
        }
        return border;
    }
    
    private void parseCursor(final XMLParser xmlp, final String name) throws IOException, XmlPullParserException {
        final String ref = xmlp.getAttributeValue(null, "ref");
        MouseCursor cursor;
        if (ref != null) {
            cursor = this.cursors.get(ref);
            if (cursor == null) {
                throw xmlp.error("referenced cursor \"" + ref + "\" not found");
            }
        }
        else {
            final ImageParams imageParams = new ImageParams();
            this.parseRectFromAttribute(xmlp, imageParams);
            final int hotSpotX = xmlp.parseIntFromAttribute("hotSpotX");
            final int hotSpotY = xmlp.parseIntFromAttribute("hotSpotY");
            final String imageRefStr = xmlp.getAttributeValue(null, "imageRef");
            Image imageRef = null;
            if (imageRefStr != null) {
                imageRef = this.getReferencedImage(xmlp, imageRefStr);
            }
            cursor = this.currentTexture.createCursor(imageParams.x, imageParams.y, imageParams.w, imageParams.h, hotSpotX, hotSpotY, imageRef);
            if (cursor == null) {
                cursor = ImageManager.NOCURSOR;
            }
        }
        this.cursors.put(name, cursor);
        xmlp.nextTag();
    }
    
    private Image parseImage(final XMLParser xmlp, final String tagName) throws XmlPullParserException, IOException {
        final ImageParams params = new ImageParams();
        params.condition = ParserUtil.parseCondition(xmlp);
        return this.parseImageNoCond(xmlp, tagName, params);
    }
    
    private Image parseImageNoCond(final XMLParser xmlp, final String tagName, final ImageParams params) throws XmlPullParserException, IOException {
        this.parseStdAttributes(xmlp, params);
        final Image image = this.parseImageDelegate(xmlp, tagName, params);
        return this.adjustImage(image, params);
    }
    
    private Image adjustImage(Image image, final ImageParams params) {
        final Border border = this.getBorder(image, params.border);
        if (params.tintColor != null && !Color.WHITE.equals((Object)params.tintColor)) {
            image = image.createTintedVersion(params.tintColor);
        }
        if (params.repeatX || params.repeatY) {
            image = (Image)new RepeatImage(image, border, params.repeatX, params.repeatY);
        }
        final Border imgBorder = this.getBorder(image, null);
        if ((border != null && border != imgBorder) || params.inset != null || params.center || params.condition != null || params.sizeOverwriteH >= 0 || params.sizeOverwriteV >= 0) {
            image = (Image)new ImageAdjustments(image, border, params.inset, params.sizeOverwriteH, params.sizeOverwriteV, params.center, params.condition);
        }
        return image;
    }
    
    private Image parseImageDelegate(final XMLParser xmlp, final String tagName, final ImageParams params) throws XmlPullParserException, IOException {
        if ("area".equals(tagName)) {
            return this.parseArea(xmlp, params);
        }
        if ("alias".equals(tagName)) {
            return this.parseAlias(xmlp);
        }
        if ("composed".equals(tagName)) {
            return this.parseComposed(xmlp, params);
        }
        if ("select".equals(tagName)) {
            return this.parseStateSelect(xmlp, params);
        }
        if ("grid".equals(tagName)) {
            return this.parseGrid(xmlp, params);
        }
        if ("animation".equals(tagName)) {
            return this.parseAnimation(xmlp, params);
        }
        if ("gradient".equals(tagName)) {
            return this.parseGradient(xmlp, params);
        }
        throw xmlp.error("Unexpected '" + tagName + "'");
    }
    
    private Image parseComposed(final XMLParser xmlp, final ImageParams params) throws IOException, XmlPullParserException {
        final ArrayList<Image> layers = new ArrayList<Image>();
        xmlp.nextTag();
        while (!xmlp.isEndTag()) {
            xmlp.require(2, null, null);
            final String tagName = xmlp.getName();
            final Image image = this.parseImage(xmlp, tagName);
            layers.add(image);
            params.border = this.getBorder(image, params.border);
            xmlp.require(3, null, tagName);
            xmlp.nextTag();
        }
        switch (layers.size()) {
            case 0: {
                return (Image)ImageManager.NONE;
            }
            case 1: {
                return layers.get(0);
            }
            default: {
                return (Image)new ComposedImage((Image[])layers.toArray(new Image[layers.size()]), params.border);
            }
        }
    }
    
    private Image parseStateSelect(final XMLParser xmlp, final ImageParams params) throws IOException, XmlPullParserException {
        final ArrayList<Image> stateImages = new ArrayList<Image>();
        final ArrayList<StateExpression> conditions = new ArrayList<StateExpression>();
        xmlp.nextTag();
        boolean last = false;
        while (!last && !xmlp.isEndTag()) {
            xmlp.require(2, null, null);
            StateExpression cond = ParserUtil.parseCondition(xmlp);
            final String tagName = xmlp.getName();
            Image image = this.parseImageNoCond(xmlp, tagName, new ImageParams());
            params.border = this.getBorder(image, params.border);
            xmlp.require(3, null, tagName);
            xmlp.nextTag();
            last = (cond == null);
            if (image instanceof ImageAdjustments) {
                final ImageAdjustments ia = (ImageAdjustments)image;
                if (ia.isSimple()) {
                    cond = and(cond, ia.condition);
                    image = ia.image;
                }
            }
            if (StateSelect.isUseOptimizer() && image instanceof StateSelectImage) {
                inlineSelect((StateSelectImage)image, cond, stateImages, conditions);
            }
            else {
                stateImages.add(image);
                if (cond == null) {
                    continue;
                }
                conditions.add(cond);
            }
        }
        if (conditions.size() < 1) {
            throw xmlp.error("state select image needs atleast 1 condition");
        }
        final StateSelect select = new StateSelect(conditions);
        final Image image2 = (Image)new StateSelectImage(select, params.border, (Image[])stateImages.toArray(new Image[stateImages.size()]));
        return image2;
    }
    
    private static void inlineSelect(final StateSelectImage src, final StateExpression cond, final ArrayList<Image> stateImages, final ArrayList<StateExpression> conditions) {
        final int n = src.images.length;
        final int m = src.select.getNumExpressions();
        for (int i = 0; i < n; ++i) {
            StateExpression imgCond = (i < m) ? src.select.getExpression(i) : null;
            imgCond = and(imgCond, cond);
            stateImages.add(src.images[i]);
            if (imgCond != null) {
                conditions.add(imgCond);
            }
        }
        if (n == m && cond != null) {
            stateImages.add((Image)ImageManager.NONE);
            conditions.add(cond);
        }
    }
    
    private static StateExpression and(StateExpression imgCond, final StateExpression cond) {
        if (imgCond == null) {
            imgCond = cond;
        }
        else if (cond != null) {
            imgCond = new StateExpression.Logic('+', new StateExpression[] { imgCond, cond });
        }
        return imgCond;
    }
    
    private Image parseArea(final XMLParser xmlp, final ImageParams params) throws IOException, XmlPullParserException {
        this.parseRectFromAttribute(xmlp, params);
        this.parseRotationFromAttribute(xmlp, params);
        final boolean tiled = xmlp.parseBoolFromAttribute("tiled", false);
        final int[] splitx = parseSplit2(xmlp, "splitx", Math.abs(params.w));
        final int[] splity = parseSplit2(xmlp, "splity", Math.abs(params.h));
        Image image;
        if (splitx != null || splity != null) {
            final boolean noCenter = xmlp.parseBoolFromAttribute("nocenter", false);
            final int columns = (splitx != null) ? 3 : 1;
            final int rows = (splity != null) ? 3 : 1;
            final Image[] imageParts = new Image[columns * rows];
            for (int r = 0; r < rows; ++r) {
                int imgY;
                int imgH;
                if (splity != null) {
                    imgY = params.y + splity[r];
                    imgH = (splity[r + 1] - splity[r]) * Integer.signum(params.h);
                }
                else {
                    imgY = params.y;
                    imgH = params.h;
                }
                for (int c = 0; c < columns; ++c) {
                    int imgX;
                    int imgW;
                    if (splitx != null) {
                        imgX = params.x + splitx[c];
                        imgW = (splitx[c + 1] - splitx[c]) * Integer.signum(params.w);
                    }
                    else {
                        imgX = params.x;
                        imgW = params.w;
                    }
                    final boolean isCenter = r == rows / 2 && c == columns / 2;
                    Image img;
                    if (noCenter && isCenter) {
                        img = (Image)new EmptyImage(imgW, imgH);
                    }
                    else {
                        img = this.createImage(xmlp, imgX, imgY, imgW, imgH, params.tintColor, isCenter & tiled, params.rot);
                    }
                    int idx = 0;
                    switch (params.rot) {
                        default: {
                            idx = r * columns + c;
                            break;
                        }
                        case CLOCKWISE_90: {
                            idx = c * rows + (rows - 1 - r);
                            break;
                        }
                        case CLOCKWISE_180: {
                            idx = (rows - 1 - r) * columns + (columns - 1 - c);
                            break;
                        }
                        case CLOCKWISE_270: {
                            idx = (columns - 1 - c) * rows + r;
                            break;
                        }
                    }
                    imageParts[idx] = img;
                }
            }
            switch (params.rot) {
                case CLOCKWISE_90:
                case CLOCKWISE_270: {
                    image = (Image)new GridImage(imageParts, (splity != null) ? ImageManager.SPLIT_WEIGHTS_3 : ImageManager.SPLIT_WEIGHTS_1, (splitx != null) ? ImageManager.SPLIT_WEIGHTS_3 : ImageManager.SPLIT_WEIGHTS_1, params.border);
                    break;
                }
                default: {
                    image = (Image)new GridImage(imageParts, (splitx != null) ? ImageManager.SPLIT_WEIGHTS_3 : ImageManager.SPLIT_WEIGHTS_1, (splity != null) ? ImageManager.SPLIT_WEIGHTS_3 : ImageManager.SPLIT_WEIGHTS_1, params.border);
                    break;
                }
            }
        }
        else {
            image = this.createImage(xmlp, params.x, params.y, params.w, params.h, params.tintColor, tiled, params.rot);
        }
        xmlp.nextTag();
        params.tintColor = null;
        if (tiled) {
            params.repeatX = false;
            params.repeatY = false;
        }
        return image;
    }
    
    private Image parseAlias(final XMLParser xmlp) throws XmlPullParserException, IOException {
        final Image image = this.getReferencedImage(xmlp);
        xmlp.nextTag();
        return image;
    }
    
    private static int[] parseSplit2(final XMLParser xmlp, final String attribName, final int size) throws XmlPullParserException {
        final String splitStr = xmlp.getAttributeValue(null, attribName);
        if (splitStr != null) {
            int comma = splitStr.indexOf(44);
            if (comma < 0) {
                throw xmlp.error(attribName + " requires 2 values");
            }
            try {
                final int[] result = new int[4];
                int i = 0;
                int start = 0;
                while (i < 2) {
                    String part = TextUtil.trim(splitStr, start, comma);
                    if (part.length() == 0) {
                        throw new NumberFormatException("empty string");
                    }
                    int off = 0;
                    int sign = 1;
                    switch (part.charAt(0)) {
                        case 'B':
                        case 'R':
                        case 'b':
                        case 'r': {
                            off = size;
                            sign = -1;
                        }
                        case 'L':
                        case 'T':
                        case 'l':
                        case 't': {
                            part = TextUtil.trim(part, 1);
                            break;
                        }
                    }
                    final int value = Integer.parseInt(part);
                    result[i + 1] = Math.max(0, Math.min(size, off + sign * value));
                    start = comma + 1;
                    comma = splitStr.length();
                    ++i;
                }
                if (result[1] > result[2]) {
                    final int tmp = result[1];
                    result[1] = result[2];
                    result[2] = tmp;
                }
                result[3] = size;
                return result;
            }
            catch (NumberFormatException ex) {
                throw xmlp.error("Unable to parse " + attribName + ": \"" + splitStr + "\"", ex);
            }
        }
        return null;
    }
    
    private void parseSubImages(final XMLParser xmlp, final Image[] textures) throws XmlPullParserException, IOException {
        int idx = 0;
        while (xmlp.isStartTag()) {
            if (idx == textures.length) {
                throw xmlp.error("Too many sub images");
            }
            final String tagName = xmlp.getName();
            textures[idx++] = this.parseImage(xmlp, tagName);
            xmlp.require(3, null, tagName);
            xmlp.nextTag();
        }
        if (idx != textures.length) {
            throw xmlp.error("Not enough sub images");
        }
    }
    
    private Image parseGrid(final XMLParser xmlp, final ImageParams params) throws IOException, XmlPullParserException {
        try {
            final int[] weightsX = ParserUtil.parseIntArrayFromAttribute(xmlp, "weightsX");
            final int[] weightsY = ParserUtil.parseIntArrayFromAttribute(xmlp, "weightsY");
            final Image[] textures = new Image[weightsX.length * weightsY.length];
            xmlp.nextTag();
            this.parseSubImages(xmlp, textures);
            final Image image = (Image)new GridImage(textures, weightsX, weightsY, params.border);
            return image;
        }
        catch (IllegalArgumentException ex) {
            throw xmlp.error("Invalid value", ex);
        }
    }
    
    private void parseAnimElements(final XMLParser xmlp, final String tagName, final ArrayList<AnimatedImage.Element> frames) throws XmlPullParserException, IOException {
        if ("repeat".equals(tagName)) {
            frames.add((AnimatedImage.Element)this.parseAnimRepeat(xmlp));
        }
        else if ("frame".equals(tagName)) {
            frames.add((AnimatedImage.Element)this.parseAnimFrame(xmlp));
        }
        else {
            if (!"frames".equals(tagName)) {
                throw xmlp.unexpected();
            }
            this.parseAnimFrames(xmlp, frames);
        }
    }
    
    private AnimatedImage.Img parseAnimFrame(final XMLParser xmlp) throws XmlPullParserException, IOException {
        final int duration = xmlp.parseIntFromAttribute("duration");
        if (duration < 0) {
            throw new IllegalArgumentException("duration must be >= 0 ms");
        }
        final AnimParams animParams = this.parseAnimParams(xmlp);
        final Image image = this.getReferencedImage(xmlp);
        final AnimatedImage.Img img = new AnimatedImage.Img(duration, image, animParams.tintColor, animParams.zoomX, animParams.zoomY, animParams.zoomCenterX, animParams.zoomCenterY);
        xmlp.nextTag();
        return img;
    }
    
    private AnimParams parseAnimParams(final XMLParser xmlp) throws XmlPullParserException {
        final AnimParams params = new AnimParams();
        params.tintColor = ParserUtil.parseColorFromAttribute(xmlp, "tint", this.constants, Color.WHITE);
        final float zoom = xmlp.parseFloatFromAttribute("zoom", 1.0f);
        params.zoomX = xmlp.parseFloatFromAttribute("zoomX", zoom);
        params.zoomY = xmlp.parseFloatFromAttribute("zoomY", zoom);
        params.zoomCenterX = xmlp.parseFloatFromAttribute("zoomCenterX", 0.5f);
        params.zoomCenterY = xmlp.parseFloatFromAttribute("zoomCenterY", 0.5f);
        return params;
    }
    
    private void parseAnimFrames(final XMLParser xmlp, final ArrayList<AnimatedImage.Element> frames) throws XmlPullParserException, IOException {
        final ImageParams params = new ImageParams();
        this.parseRectFromAttribute(xmlp, params);
        this.parseRotationFromAttribute(xmlp, params);
        final int duration = xmlp.parseIntFromAttribute("duration");
        if (duration < 1) {
            throw new IllegalArgumentException("duration must be >= 1 ms");
        }
        final int count = xmlp.parseIntFromAttribute("count");
        if (count < 1) {
            throw new IllegalArgumentException("count must be >= 1");
        }
        final AnimParams animParams = this.parseAnimParams(xmlp);
        final int xOffset = xmlp.parseIntFromAttribute("offsetx", 0);
        final int yOffset = xmlp.parseIntFromAttribute("offsety", 0);
        if (count > 1 && xOffset == 0 && yOffset == 0) {
            throw new IllegalArgumentException("offsets required for multiple frames");
        }
        for (int i = 0; i < count; ++i) {
            final Image image = this.createImage(xmlp, params.x, params.y, params.w, params.h, Color.WHITE, false, params.rot);
            final AnimatedImage.Img img = new AnimatedImage.Img(duration, image, animParams.tintColor, animParams.zoomX, animParams.zoomY, animParams.zoomCenterX, animParams.zoomCenterY);
            frames.add((AnimatedImage.Element)img);
            final ImageParams imageParams = params;
            imageParams.x += xOffset;
            final ImageParams imageParams2 = params;
            imageParams2.y += yOffset;
        }
        xmlp.nextTag();
    }
    
    private AnimatedImage.Repeat parseAnimRepeat(final XMLParser xmlp) throws XmlPullParserException, IOException {
        final String strRepeatCount = xmlp.getAttributeValue(null, "count");
        int repeatCount = 0;
        if (strRepeatCount != null) {
            repeatCount = Integer.parseInt(strRepeatCount);
            if (repeatCount <= 0) {
                throw new IllegalArgumentException("Invalid repeat count");
            }
        }
        boolean lastRepeatsEndless = false;
        boolean hasWarned = false;
        final ArrayList<AnimatedImage.Element> children = new ArrayList<AnimatedImage.Element>();
        xmlp.nextTag();
        while (xmlp.isStartTag()) {
            if (lastRepeatsEndless && !hasWarned) {
                hasWarned = true;
                this.getLogger().log(Level.WARNING, "Animation frames after an endless repeat won''t be displayed: {0}", xmlp.getPositionDescription());
            }
            final String tagName = xmlp.getName();
            this.parseAnimElements(xmlp, tagName, children);
            final AnimatedImage.Element e = children.get(children.size() - 1);
            lastRepeatsEndless = (e instanceof AnimatedImage.Repeat && ((AnimatedImage.Repeat)e).repeatCount == 0);
            xmlp.require(3, null, tagName);
            xmlp.nextTag();
        }
        return new AnimatedImage.Repeat((AnimatedImage.Element[])children.toArray(new AnimatedImage.Element[children.size()]), repeatCount);
    }
    
    private Border getBorder(final AnimatedImage.Element e) {
        if (e instanceof AnimatedImage.Repeat) {
            final AnimatedImage.Repeat r = (AnimatedImage.Repeat)e;
            for (final AnimatedImage.Element c : r.children) {
                final Border border = this.getBorder(c);
                if (border != null) {
                    return border;
                }
            }
        }
        else if (e instanceof AnimatedImage.Img) {
            final AnimatedImage.Img i = (AnimatedImage.Img)e;
            if (i.image instanceof HasBorder) {
                return ((HasBorder)i.image).getBorder();
            }
        }
        return null;
    }
    
    private Image parseAnimation(final XMLParser xmlp, final ImageParams params) throws XmlPullParserException, IOException {
        try {
            final String timeSource = xmlp.getAttributeNotNull("timeSource");
            final int frozenTime = xmlp.parseIntFromAttribute("frozenTime", -1);
            final AnimatedImage.Repeat root = this.parseAnimRepeat(xmlp);
            if (params.border == null) {
                params.border = this.getBorder((AnimatedImage.Element)root);
            }
            final Image image = (Image)new AnimatedImage(this.renderer, (AnimatedImage.Element)root, timeSource, params.border, (params.tintColor == null) ? Color.WHITE : params.tintColor, frozenTime);
            params.tintColor = null;
            return image;
        }
        catch (IllegalArgumentException ex) {
            throw xmlp.error("Unable to parse", ex);
        }
    }
    
    private Image parseGradient(final XMLParser xmlp, final ImageParams params) throws XmlPullParserException, IOException {
        try {
            final Gradient.Type type = xmlp.parseEnumFromAttribute("type", Gradient.Type.class);
            final Gradient.Wrap wrap = xmlp.parseEnumFromAttribute("wrap", Gradient.Wrap.class, Gradient.Wrap.SCALE);
            final Gradient gradient = new Gradient(type);
            gradient.setWrap(wrap);
            xmlp.nextTag();
            while (xmlp.isStartTag()) {
                xmlp.require(2, null, "stop");
                final float pos = xmlp.parseFloatFromAttribute("pos");
                final Color color = ParserUtil.parseColor(xmlp, xmlp.getAttributeNotNull("color"), this.constants);
                gradient.addStop(pos, color);
                xmlp.nextTag();
                xmlp.require(3, null, "stop");
                xmlp.nextTag();
            }
            return this.renderer.createGradient(gradient);
        }
        catch (IllegalArgumentException ex) {
            throw xmlp.error("Unable to parse", ex);
        }
    }
    
    private Image createImage(final XMLParser xmlp, int x, int y, int w, int h, final Color tintColor, final boolean tiled, final Texture.Rotation rotation) {
        if (w == 0 || h == 0) {
            return (Image)new EmptyImage(Math.abs(w), Math.abs(h));
        }
        final Texture texture = this.currentTexture;
        final int texWidth = texture.getWidth();
        final int texHeight = texture.getHeight();
        final int x2 = x + Math.abs(w);
        final int y2 = y + Math.abs(h);
        if (x < 0 || x >= texWidth || x2 < 0 || x2 > texWidth || y < 0 || y >= texHeight || y2 < 0 || y2 > texHeight) {
            this.getLogger().log(Level.WARNING, "texture partly outside of file: {0}", xmlp.getPositionDescription());
            x = Math.max(0, Math.min(x, texWidth));
            y = Math.max(0, Math.min(y, texHeight));
            w = Integer.signum(w) * (Math.max(0, Math.min(x2, texWidth)) - x);
            h = Integer.signum(h) * (Math.max(0, Math.min(y2, texHeight)) - y);
        }
        return texture.getImage(x, y, w, h, tintColor, tiled, rotation);
    }
    
    private void parseRectFromAttribute(final XMLParser xmlp, final ImageParams params) throws XmlPullParserException {
        if (this.currentTexture == null) {
            throw xmlp.error("can't create area outside of <imagefile> object");
        }
        final String xywh = xmlp.getAttributeNotNull("xywh");
        if ("*".equals(xywh)) {
            params.x = 0;
            params.y = 0;
            params.w = this.currentTexture.getWidth();
            params.h = this.currentTexture.getHeight();
        }
        else {
            try {
                final int[] coords = TextUtil.parseIntArray(xywh);
                if (coords.length != 4) {
                    throw xmlp.error("xywh requires 4 integer arguments");
                }
                params.x = coords[0];
                params.y = coords[1];
                params.w = coords[2];
                params.h = coords[3];
            }
            catch (IllegalArgumentException ex) {
                throw xmlp.error("can't parse xywh argument", ex);
            }
        }
    }
    
    private void parseRotationFromAttribute(final XMLParser xmlp, final ImageParams params) throws XmlPullParserException {
        if (this.currentTexture == null) {
            throw xmlp.error("can't create area outside of <imagefile> object");
        }
        final int rot = xmlp.parseIntFromAttribute("rot", 0);
        switch (rot) {
            case 0: {
                params.rot = Texture.Rotation.NONE;
                break;
            }
            case 90: {
                params.rot = Texture.Rotation.CLOCKWISE_90;
                break;
            }
            case 180: {
                params.rot = Texture.Rotation.CLOCKWISE_180;
                break;
            }
            case 270: {
                params.rot = Texture.Rotation.CLOCKWISE_270;
                break;
            }
            default: {
                throw xmlp.error("invalid rotation angle");
            }
        }
    }
    
    private void parseStdAttributes(final XMLParser xmlp, final ImageParams params) throws XmlPullParserException {
        params.tintColor = ParserUtil.parseColorFromAttribute(xmlp, "tint", this.constants, null);
        params.border = ParserUtil.parseBorderFromAttribute(xmlp, "border");
        params.inset = ParserUtil.parseBorderFromAttribute(xmlp, "inset");
        params.repeatX = xmlp.parseBoolFromAttribute("repeatX", false);
        params.repeatY = xmlp.parseBoolFromAttribute("repeatY", false);
        params.sizeOverwriteH = xmlp.parseIntFromAttribute("sizeOverwriteH", -1);
        params.sizeOverwriteV = xmlp.parseIntFromAttribute("sizeOverwriteV", -1);
        params.center = xmlp.parseBoolFromAttribute("center", false);
    }
    
    Logger getLogger() {
        return Logger.getLogger(ImageManager.class.getName());
    }
    
    static {
        NONE = new EmptyImage(0, 0);
        NOCURSOR = (MouseCursor)new MouseCursor() {};
        SPLIT_WEIGHTS_3 = new int[] { 0, 1, 0 };
        SPLIT_WEIGHTS_1 = new int[] { 1 };
    }
    
    static class ImageParams
    {
        int x;
        int y;
        int w;
        int h;
        Color tintColor;
        Border border;
        Border inset;
        boolean repeatX;
        boolean repeatY;
        int sizeOverwriteH;
        int sizeOverwriteV;
        boolean center;
        StateExpression condition;
        Texture.Rotation rot;
        
        ImageParams() {
            this.sizeOverwriteH = -1;
            this.sizeOverwriteV = -1;
        }
    }
    
    static class AnimParams
    {
        Color tintColor;
        float zoomX;
        float zoomY;
        float zoomCenterX;
        float zoomCenterY;
    }
}
