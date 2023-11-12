package de.matthiasmann.twl.theme;

import org.xmlpull.v1.*;
import java.io.*;
import java.net.*;
import org.lwjgl.opengl.*;
import de.matthiasmann.twl.renderer.*;
import java.util.*;
import java.text.*;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.utils.*;

public class ThemeManager
{
    private static final HashMap<String, Class<? extends Enum<?>>> enums;
    static final Object NULL;
    final ParameterMapImpl constants;
    private final Renderer renderer;
    private final CacheContext cacheContext;
    private final ImageManager imageManager;
    private final HashMap<String, Font> fonts;
    private final HashMap<String, ThemeInfoImpl> themes;
    private final HashMap<String, InputMap> inputMaps;
    private final MathInterpreter mathInterpreter;
    private Font defaultFont;
    private Font firstFont;
    final ParameterMapImpl emptyMap;
    final ParameterListImpl emptyList;

    private ThemeManager(final Renderer renderer, final CacheContext cacheContext) throws XmlPullParserException, IOException {
        this.constants = new ParameterMapImpl(this, (ThemeInfoImpl)null);
        this.renderer = renderer;
        this.cacheContext = cacheContext;
        this.imageManager = new ImageManager(this.constants, renderer);
        this.fonts = new HashMap<String, Font>();
        this.themes = new HashMap<String, ThemeInfoImpl>();
        this.inputMaps = new HashMap<String, InputMap>();
        this.emptyMap = new ParameterMapImpl(this, (ThemeInfoImpl)null);
        this.emptyList = new ParameterListImpl(this, (ThemeInfoImpl)null);
        this.mathInterpreter = new MathInterpreter();
    }

    public CacheContext getCacheContext() {
        return this.cacheContext;
    }

    public void destroy() {
        for (final Font font : this.fonts.values()) {
            font.destroy();
        }
        this.cacheContext.destroy();
    }

    public Font getDefaultFont() {
        return this.defaultFont;
    }

    public static ThemeManager createThemeManager(final URL url, final Renderer renderer) throws IOException {
        if (url == null) {
            throw new IllegalArgumentException("url is null");
        }
        if (renderer == null) {
            throw new IllegalArgumentException("renderer is null");
        }
        return createThemeManager(url, renderer, renderer.createNewCacheContext());
    }

    public static ThemeManager createThemeManager(final URL url, final Renderer renderer, final CacheContext cacheContext) throws IOException {
        if (url == null) {
            throw new IllegalArgumentException("url is null");
        }
        if (renderer == null) {
            throw new IllegalArgumentException("renderer is null");
        }
        if (cacheContext == null) {
            throw new IllegalArgumentException("cacheContext is null");
        }
        try {
            renderer.setActiveCacheContext(cacheContext);
            final ThemeManager tm = new ThemeManager(renderer, cacheContext);
            tm.insertDefaultConstants();
            tm.parseThemeFile(url);
            if (tm.defaultFont == null) {
                tm.defaultFont = tm.firstFont;
            }
            return tm;
        }
        catch (XmlPullParserException ex) {
            throw (IOException)new IOException().initCause((Throwable)ex);
        }
    }

    public static <E extends Enum<E>> void registerEnumType(final String name, final Class<E> enumClazz) {
        if (!enumClazz.isEnum()) {
            throw new IllegalArgumentException("not an enum class");
        }
        final Class<?> curClazz = ThemeManager.enums.get(name);
        if (curClazz != null && curClazz != enumClazz) {
            throw new IllegalArgumentException("Enum type name \"" + name + "\" is already in use by " + curClazz);
        }
        ThemeManager.enums.put(name, enumClazz);
    }

    public ThemeInfo findThemeInfo(final String themePath) {
        return this.findThemeInfo(themePath, true, true);
    }

    private ThemeInfo findThemeInfo(final String themePath, final boolean warn, final boolean useFallback) {
        int start = TextUtil.indexOf(themePath, '.', 0);
        ThemeInfo info = (ThemeInfo)this.themes.get(themePath.substring(0, start));
        if (info == null) {
            info = (ThemeInfo)this.themes.get("*");
            if (info != null) {
                if (!useFallback) {
                    return null;
                }
                DebugHook.getDebugHook().usingFallbackTheme(themePath);
            }
        }
        while (info != null && ++start < themePath.length()) {
            final int next = TextUtil.indexOf(themePath, '.', start);
            info = info.getChildTheme(themePath.substring(start, next));
            start = next;
        }
        if (info == null && warn) {
            DebugHook.getDebugHook().missingTheme(themePath);
        }
        return info;
    }

    public Image getImageNoWarning(final String name) {
        return this.imageManager.getImage(name);
    }

    public Image getImage(final String name) {
        final Image img = this.imageManager.getImage(name);
        if (img == null) {
            DebugHook.getDebugHook().missingImage(name);
        }
        return img;
    }

    public Object getCursor(final String name) {
        return this.imageManager.getCursor(name);
    }

    public ParameterMap getConstants() {
        return (ParameterMap)this.constants;
    }

    private void insertDefaultConstants() {
        this.constants.put("SINGLE_COLUMN", (Object)(-1));
        this.constants.put("MAX", (Object)32767);
    }

    private void parseThemeFile(final URL url) throws IOException {
        try {
            final XMLParser xmlp = new XMLParser(url);
            try {
                xmlp.setLoggerName(ThemeManager.class.getName());
                xmlp.require(0, null, null);
                xmlp.nextTag();
                this.parseThemeFile(xmlp, url);
            }
            finally {
                xmlp.close();
            }
        }
        catch (XmlPullParserException ex) {
            throw new ThemeException(ex.getMessage(), url, ex.getLineNumber(), ex.getColumnNumber(), (Throwable)ex);
        }
        catch (ThemeException ex2) {
            throw ex2;
        }
        catch (Exception ex3) {
            throw (IOException)new IOException("while parsing Theme XML: " + url).initCause(ex3);
        }
    }

    private void parseThemeFile(final XMLParser xmlp, final URL baseUrl) throws XmlPullParserException, IOException {
        xmlp.require(2, null, "themes");
        xmlp.nextTag();
        while (!xmlp.isEndTag()) {
            xmlp.require(2, null, null);
            final String tagName = xmlp.getName();
            if ("images".equals(tagName) || "textures".equals(tagName)) {
                this.imageManager.parseImages(xmlp, baseUrl);
            }
            else if ("include".equals(tagName)) {
                final String fontFileName = xmlp.getAttributeNotNull("filename");
                try {
                    this.parseThemeFile(new URL(baseUrl, fontFileName));
                }
                catch (ThemeException ex) {
                    ex.addIncludedBy(baseUrl, xmlp.getLineNumber(), xmlp.getColumnNumber());
                    throw ex;
                }
                xmlp.nextTag();
            }
            else {
                final String name = xmlp.getAttributeNotNull("name");
                if ("theme".equals(tagName)) {
                    if (this.themes.containsKey(name)) {
                        throw xmlp.error("theme \"" + name + "\" already defined");
                    }
                    this.themes.put(name, this.parseTheme(xmlp, name, null, baseUrl));
                }
                else if ("inputMapDef".equals(tagName)) {
                    if (this.inputMaps.containsKey(name)) {
                        throw xmlp.error("inputMap \"" + name + "\" already defined");
                    }
                    this.inputMaps.put(name, this.parseInputMap(xmlp, name, null));
                }
                else if ("fontDef".equals(tagName)) {
                    if (this.fonts.containsKey(name)) {
                        throw xmlp.error("font \"" + name + "\" already defined");
                    }
                    final boolean makeDefault = xmlp.parseBoolFromAttribute("default", false);
                    final Font font = this.parseFont(xmlp, baseUrl);
                    this.fonts.put(name, font);
                    if (this.firstFont == null) {
                        this.firstFont = font;
                    }
                    if (makeDefault) {
                        if (this.defaultFont != null) {
                            throw xmlp.error("default font already set");
                        }
                        this.defaultFont = font;
                    }
                }
                else {
                    if (!"constantDef".equals(tagName)) {
                        throw xmlp.unexpected();
                    }
                    this.parseParam(xmlp, baseUrl, "constantDef", null, this.constants);
                }
            }
            xmlp.require(3, null, tagName);
            xmlp.nextTag();
        }
        xmlp.require(3, null, "themes");
    }

    private InputMap getInputMap(final XMLParser xmlp, final String name) throws XmlPullParserException {
        final InputMap im = this.inputMaps.get(name);
        if (im == null) {
            throw xmlp.error("Undefined input map: " + name);
        }
        return im;
    }

    private InputMap parseInputMap(final XMLParser xmlp, final String name, final ThemeInfoImpl parent) throws XmlPullParserException, IOException {
        InputMap base = InputMap.empty();
        if (xmlp.parseBoolFromAttribute("merge", false)) {
            if (parent == null) {
                throw xmlp.error("Can't merge on top level");
            }
            final Object o = parent.getParam(name);
            if (o instanceof InputMap) {
                base = (InputMap)o;
            }
            else if (o != null) {
                throw xmlp.error("Can only merge with inputMap - found a " + o.getClass().getSimpleName());
            }
        }
        final String baseName = xmlp.getAttributeValue(null, "ref");
        if (baseName != null) {
            base = base.addKeyStrokes(this.getInputMap(xmlp, baseName));
        }
        xmlp.nextTag();
        final LinkedHashSet<KeyStroke> keyStrokes = (LinkedHashSet<KeyStroke>)InputMap.parseBody(xmlp);
        final InputMap im = base.addKeyStrokes((LinkedHashSet<KeyStroke>)keyStrokes);
        return im;
    }

    private Font parseFont(final XMLParser xmlp, final URL baseUrl) throws XmlPullParserException, IOException {
        URL url = baseUrl;
        final String fileName = xmlp.getAttributeValue(null, "filename");
        if (fileName != null) {
            url = new URL(url, fileName);
        }
        final StringList fontFamilies = parseList(xmlp, "families");
        int fontSize = 0;
        int fontStyle = 0;
        if (fontFamilies != null) {
            fontSize = xmlp.parseIntFromAttribute("size");
            for (StringList style = parseList(xmlp, "style"); style != null; style = style.getNext()) {
                if ("bold".equalsIgnoreCase(style.getValue())) {
                    fontStyle |= 0x1;
                }
                else if ("italic".equalsIgnoreCase(style.getValue())) {
                    fontStyle |= 0x2;
                }
            }
        }
        final FontParameter baseParams = new FontParameter();
        this.parseFontParameter(xmlp, baseParams);
        final ArrayList<FontParameter> fontParams = new ArrayList<FontParameter>();
        final ArrayList<StateExpression> stateExpr = new ArrayList<StateExpression>();
        xmlp.nextTag();
        while (!xmlp.isEndTag()) {
            xmlp.require(2, null, "fontParam");
            final StateExpression cond = ParserUtil.parseCondition(xmlp);
            if (cond == null) {
                throw xmlp.error("Condition required");
            }
            stateExpr.add(cond);
            final FontParameter params = new FontParameter(baseParams);
            this.parseFontParameter(xmlp, params);
            fontParams.add(params);
            xmlp.nextTag();
            xmlp.require(3, null, "fontParam");
            xmlp.nextTag();
        }
        fontParams.add(baseParams);
        final StateSelect stateSelect = new StateSelect(stateExpr);
        final FontParameter[] stateParams = fontParams.toArray(new FontParameter[fontParams.size()]);
        Util.checkGLError();
        if (fontFamilies != null) {
            final FontMapper fontMapper = this.renderer.getFontMapper();
            if (fontMapper != null) {
                final Font font = fontMapper.getFont(fontFamilies, fontSize, fontStyle, stateSelect, stateParams);
                if (font != null) {
                    return font;
                }
            }
        }
        Util.checkGLError();
        return this.renderer.loadFont(url, stateSelect, stateParams);
    }

    private void parseFontParameter(final XMLParser xmlp, final FontParameter fp) throws XmlPullParserException {
        for (int i = 0, n = xmlp.getAttributeCount(); i < n; ++i) {
            if (xmlp.isAttributeUnused(i)) {
                final String name = xmlp.getAttributeName(i);
                final FontParameter.Parameter<?> type = (FontParameter.Parameter<?>)FontParameter.getParameter(name);
                if (type != null) {
                    final String value = xmlp.getAttributeValue(i);
                    final Class<?> dataClass = (Class<?>)type.getDataClass();
                    if (dataClass == Color.class) {
                        final FontParameter.Parameter<Color> colorType = (FontParameter.Parameter<Color>)type;
                        fp.put((FontParameter.Parameter)colorType, (Object)ParserUtil.parseColor(xmlp, value, this.constants));
                    }
                    else if (dataClass == Integer.class) {
                        final FontParameter.Parameter<Integer> intType = (FontParameter.Parameter<Integer>)type;
                        fp.put((FontParameter.Parameter)intType, (Object)this.parseMath(xmlp, value).intValue());
                    }
                    else if (dataClass == Boolean.class) {
                        final FontParameter.Parameter<Boolean> boolType = (FontParameter.Parameter<Boolean>)type;
                        fp.put((FontParameter.Parameter)boolType, (Object)xmlp.parseBool(value));
                    }
                    else {
                        if (dataClass != String.class) {
                            throw xmlp.error("dataClass not yet implemented: " + dataClass);
                        }
                        final FontParameter.Parameter<String> strType = (FontParameter.Parameter<String>)type;
                        fp.put((FontParameter.Parameter)strType, (Object)value);
                    }
                }
            }
        }
    }

    private static StringList parseList(final XMLParser xmlp, final String name) {
        final String value = xmlp.getAttributeValue(null, name);
        if (value != null) {
            return parseList(value, 0);
        }
        return null;
    }

    private static StringList parseList(final String value, int idx) {
        idx = TextUtil.skipSpaces(value, idx);
        if (idx >= value.length()) {
            return null;
        }
        final int end = TextUtil.indexOf(value, ',', idx);
        final String part = TextUtil.trim(value, idx, end);
        return new StringList(part, parseList(value, end + 1));
    }

    private void parseThemeWildcardRef(final XMLParser xmlp, final ThemeInfoImpl parent) throws IOException, XmlPullParserException {
        final String ref = xmlp.getAttributeValue(null, "ref");
        if (parent == null) {
            throw xmlp.error("Can't declare wildcard themes on top level");
        }
        if (ref == null) {
            throw xmlp.error("Reference required for wildcard theme");
        }
        if (!ref.endsWith("*")) {
            throw xmlp.error("Wildcard reference must end with '*'");
        }
        final String refPath = ref.substring(0, ref.length() - 1);
        if (refPath.length() > 0 && !refPath.endsWith(".")) {
            throw xmlp.error("Wildcard must end with \".*\" or be \"*\"");
        }
        parent.wildcardImportPath = refPath;
        xmlp.nextTag();
    }

    private ThemeInfoImpl parseTheme(final XMLParser xmlp, final String themeName, final ThemeInfoImpl parent, final URL baseUrl) throws IOException, XmlPullParserException {
        if (!themeName.equals("*") || parent != null) {
            ParserUtil.checkNameNotEmpty(themeName, xmlp);
            if (themeName.indexOf(46) >= 0) {
                throw xmlp.error("'.' is not allowed in names");
            }
        }
        final ThemeInfoImpl ti = new ThemeInfoImpl(this, themeName, parent);
        final ThemeInfoImpl oldEnv = this.mathInterpreter.setEnv(ti);
        try {
            if (xmlp.parseBoolFromAttribute("merge", false)) {
                if (parent == null) {
                    throw xmlp.error("Can't merge on top level");
                }
                final ThemeInfoImpl tiPrev = parent.getTheme(themeName);
                if (tiPrev != null) {
                    ti.copy(tiPrev);
                }
            }
            final String ref = xmlp.getAttributeValue(null, "ref");
            if (ref != null) {
                ThemeInfoImpl tiRef = null;
                if (parent != null) {
                    tiRef = parent.getTheme(ref);
                }
                if (tiRef == null) {
                    tiRef = (ThemeInfoImpl)this.findThemeInfo(ref);
                }
                if (tiRef == null) {
                    throw xmlp.error("referenced theme info not found: " + ref);
                }
                ti.copy(tiRef);
            }
            ti.maybeUsedFromWildcard = xmlp.parseBoolFromAttribute("allowWildcard", true);
            xmlp.nextTag();
            while (!xmlp.isEndTag()) {
                xmlp.require(2, null, null);
                final String tagName = xmlp.getName();
                final String name = xmlp.getAttributeNotNull("name");
                if ("param".equals(tagName)) {
                    this.parseParam(xmlp, baseUrl, "param", ti, (ParameterMapImpl)ti);
                }
                else {
                    if (!"theme".equals(tagName)) {
                        throw xmlp.unexpected();
                    }
                    if (name.length() == 0) {
                        this.parseThemeWildcardRef(xmlp, ti);
                    }
                    else {
                        final ThemeInfoImpl tiChild = this.parseTheme(xmlp, name, ti, baseUrl);
                        ti.putTheme(name, tiChild);
                    }
                }
                xmlp.require(3, null, tagName);
                xmlp.nextTag();
            }
        }
        finally {
            this.mathInterpreter.setEnv(oldEnv);
        }
        return ti;
    }

    private void parseParam(final XMLParser xmlp, final URL baseUrl, final String tagName, final ThemeInfoImpl parent, final ParameterMapImpl target) throws XmlPullParserException, IOException {
        try {
            xmlp.require(2, null, tagName);
            final String name = xmlp.getAttributeNotNull("name");
            xmlp.nextTag();
            final String valueTagName = xmlp.getName();
            final Object value = this.parseValue(xmlp, valueTagName, name, baseUrl, parent);
            xmlp.require(3, null, valueTagName);
            xmlp.nextTag();
            xmlp.require(3, null, tagName);
            if (value instanceof Map) {
                final Map<String, ?> map = (Map<String, ?>)value;
                if (parent == null && map.size() != 1) {
                    throw xmlp.error("constant definitions must define exactly 1 value");
                }
                target.put((Map)map);
            }
            else {
                ParserUtil.checkNameNotEmpty(name, xmlp);
                target.put(name, value);
            }
        }
        catch (NumberFormatException ex) {
            throw xmlp.error("unable to parse value", ex);
        }
    }

    private ParameterListImpl parseList(final XMLParser xmlp, final URL baseUrl, final ThemeInfoImpl parent) throws XmlPullParserException, IOException {
        final ParameterListImpl result = new ParameterListImpl(this, parent);
        xmlp.nextTag();
        while (xmlp.isStartTag()) {
            final String tagName = xmlp.getName();
            final Object obj = this.parseValue(xmlp, tagName, null, baseUrl, parent);
            xmlp.require(3, null, tagName);
            result.params.add(obj);
            xmlp.nextTag();
        }
        return result;
    }

    private ParameterMapImpl parseMap(final XMLParser xmlp, final URL baseUrl, final String name, final ThemeInfoImpl parent) throws XmlPullParserException, IOException, NumberFormatException {
        final ParameterMapImpl result = new ParameterMapImpl(this, parent);
        if (xmlp.parseBoolFromAttribute("merge", false)) {
            if (parent == null) {
                throw xmlp.error("Can't merge on top level");
            }
            final Object obj = parent.getParam(name);
            if (obj instanceof ParameterMapImpl) {
                final ParameterMapImpl base = (ParameterMapImpl)obj;
                result.copy(base);
            }
            else if (obj != null) {
                throw xmlp.error("Can only merge with map - found a " + obj.getClass().getSimpleName());
            }
        }
        final String ref = xmlp.getAttributeValue(null, "ref");
        if (ref != null) {
            Object obj2 = parent.getParam(ref);
            if (obj2 == null) {
                obj2 = this.constants.getParam(ref);
                if (obj2 == null) {
                    throw new IOException("Referenced map not found: " + ref);
                }
            }
            if (!(obj2 instanceof ParameterMapImpl)) {
                throw new IOException("Expected a map got a " + obj2.getClass().getSimpleName());
            }
            final ParameterMapImpl base2 = (ParameterMapImpl)obj2;
            result.copy(base2);
        }
        xmlp.nextTag();
        while (xmlp.isStartTag()) {
            final String tagName = xmlp.getName();
            this.parseParam(xmlp, baseUrl, "param", parent, result);
            xmlp.require(3, null, tagName);
            xmlp.nextTag();
        }
        return result;
    }

    private Object parseValue(final XMLParser xmlp, final String tagName, final String wildcardName, final URL baseUrl, final ThemeInfoImpl parent) throws XmlPullParserException, IOException, NumberFormatException {
        try {
            if ("list".equals(tagName)) {
                return this.parseList(xmlp, baseUrl, parent);
            }
            if ("map".equals(tagName)) {
                return this.parseMap(xmlp, baseUrl, wildcardName, parent);
            }
            if ("inputMapDef".equals(tagName)) {
                return this.parseInputMap(xmlp, wildcardName, parent);
            }
            if ("fontDef".equals(tagName)) {
                return this.parseFont(xmlp, baseUrl);
            }
            if ("enum".equals(tagName)) {
                final String enumType = xmlp.getAttributeNotNull("type");
                final Class<? extends Enum> enumClazz = ThemeManager.enums.get(enumType);
                if (enumClazz == null) {
                    throw xmlp.error("enum type \"" + enumType + "\" not registered");
                }
                return xmlp.parseEnumFromText(enumClazz);
            }
            else {
                if ("bool".equals(tagName)) {
                    return xmlp.parseBoolFromText();
                }
                final String value = xmlp.nextText();
                if ("color".equals(tagName)) {
                    return ParserUtil.parseColor(xmlp, value, this.constants);
                }
                if ("float".equals(tagName)) {
                    return this.parseMath(xmlp, value).floatValue();
                }
                if ("int".equals(tagName)) {
                    return this.parseMath(xmlp, value).intValue();
                }
                if ("string".equals(tagName)) {
                    return value;
                }
                if ("font".equals(tagName)) {
                    final Font font = this.fonts.get(value);
                    if (font == null) {
                        throw xmlp.error("Font \"" + value + "\" not found");
                    }
                    return font;
                }
                else {
                    if ("border".equals(tagName)) {
                        return this.parseObject(xmlp, value, Border.class);
                    }
                    if ("dimension".equals(tagName)) {
                        return this.parseObject(xmlp, value, Dimension.class);
                    }
                    if ("gap".equals(tagName) || "size".equals(tagName)) {
                        return this.parseObject(xmlp, value, DialogLayout.Gap.class);
                    }
                    if ("constant".equals(tagName)) {
                        Object result = this.constants.getParam(value);
                        if (result == null) {
                            throw xmlp.error("Unknown constant: " + value);
                        }
                        if (result == ThemeManager.NULL) {
                            result = null;
                        }
                        return result;
                    }
                    else if ("image".equals(tagName)) {
                        if (!value.endsWith(".*")) {
                            return this.imageManager.getReferencedImage(xmlp, value);
                        }
                        if (wildcardName == null) {
                            throw new IllegalArgumentException("Wildcard's not allowed");
                        }
                        return this.imageManager.getImages(value, wildcardName);
                    }
                    else if ("cursor".equals(tagName)) {
                        if (!value.endsWith(".*")) {
                            return this.imageManager.getReferencedCursor(xmlp, value);
                        }
                        if (wildcardName == null) {
                            throw new IllegalArgumentException("Wildcard's not allowed");
                        }
                        return this.imageManager.getCursors(value, wildcardName);
                    }
                    else {
                        if ("inputMap".equals(tagName)) {
                            return this.getInputMap(xmlp, value);
                        }
                        throw xmlp.error("Unknown type \"" + tagName + "\" specified");
                    }
                }
            }
        }
        catch (NumberFormatException ex) {
            throw xmlp.error("unable to parse value", ex);
        }
    }

    private Number parseMath(final XMLParser xmlp, final String str) throws XmlPullParserException {
        try {
            return this.mathInterpreter.execute(str);
        }
        catch (ParseException ex) {
            throw xmlp.error("unable to evaluate", this.unwrap(ex));
        }
    }

    private <T> T parseObject(final XMLParser xmlp, final String str, final Class<T> type) throws XmlPullParserException {
        try {
            return this.mathInterpreter.executeCreateObject(str, type);
        }
        catch (ParseException ex) {
            throw xmlp.error("unable to evaluate", this.unwrap(ex));
        }
    }

    private Throwable unwrap(final ParseException ex) {
        if (ex.getCause() != null) {
            return ex.getCause();
        }
        return ex;
    }

    ThemeInfo resolveWildcard(final String base, final String name, final boolean useFallback) {
        assert !(!base.endsWith("."));
        final String fullPath = base.concat(name);
        final ThemeInfo info = this.findThemeInfo(fullPath, false, useFallback);
        if (info != null && ((ThemeInfoImpl)info).maybeUsedFromWildcard) {
            return info;
        }
        return null;
    }

    static {
        enums = new HashMap<String, Class<? extends Enum<?>>>();
        registerEnumType("alignment", Alignment.class);
        registerEnumType("direction", PositionAnimatedPanel.Direction.class);
        NULL = new Object();
    }

    class MathInterpreter extends AbstractMathInterpreter
    {
        private ThemeInfoImpl env;

        public ThemeInfoImpl setEnv(final ThemeInfoImpl env) {
            final ThemeInfoImpl oldEnv = this.env;
            this.env = env;
            return oldEnv;
        }

        @Override
        public void accessVariable(final String name) {
            for (ThemeInfoImpl e = this.env; e != null; e = e.parent) {
                Object obj = e.getParam(name);
                if (obj != null) {
                    this.push(obj);
                    return;
                }
                obj = e.getChildThemeImpl(name, false);
                if (obj != null) {
                    this.push(obj);
                    return;
                }
            }
            final Object obj2 = ThemeManager.this.constants.getParam(name);
            if (obj2 != null) {
                this.push(obj2);
                return;
            }
            final Font font = ThemeManager.this.fonts.get(name);
            if (font != null) {
                this.push(font);
                return;
            }
            throw new IllegalArgumentException("variable not found: " + name);
        }

        @Override
        protected Object accessField(final Object obj, final String field) {
            if (obj instanceof ThemeInfoImpl) {
                final Object result = ((ThemeInfoImpl)obj).getTheme(field);
                if (result != null) {
                    return result;
                }
            }
            if (obj instanceof ParameterMapImpl) {
                final Object result = ((ParameterMapImpl)obj).getParam(field);
                if (result == null) {
                    throw new IllegalArgumentException("field not found: " + field);
                }
                return result;
            }
            else {
                if (obj instanceof Image && "border".equals(field)) {
                    Border border = null;
                    if (obj instanceof HasBorder) {
                        border = ((HasBorder)obj).getBorder();
                    }
                    return (border != null) ? border : Border.ZERO;
                }
                return super.accessField(obj, field);
            }
        }
    }
}
