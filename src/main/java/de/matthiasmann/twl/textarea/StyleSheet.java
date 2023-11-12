package de.matthiasmann.twl.textarea;

import java.net.*;
import java.io.*;
import de.matthiasmann.twl.renderer.*;
import java.util.logging.*;
import de.matthiasmann.twl.utils.*;
import java.util.*;

public class StyleSheet implements StyleSheetResolver
{
    static final Object NULL;
    private final ArrayList<Selector> rules;
    private final IdentityHashMap<Style, Object> cache;
    private ArrayList<AtRule> atrules;

    public StyleSheet() {
        this.rules = new ArrayList<Selector>();
        this.cache = new IdentityHashMap<Style, Object>();
    }

    public void parse(final URL url) throws IOException {
        final InputStream is = url.openStream();
        try {
            this.parse(new InputStreamReader(is, "UTF8"));
        }
        finally {
            is.close();
        }
    }

    public void parse(final String style) throws IOException {
        this.parse(new StringReader(style));
    }

    public void parse(final Reader r) throws IOException {
        final Parser parser = new Parser(r);
        final ArrayList<Selector> selectors = new ArrayList<Selector>();
        int what;
        while ((what = parser.yylex()) != 0) {
            if (what == 11) {
                parser.expect(1);
                final AtRule atrule = new AtRule(parser.yytext());
                parser.expect(7);
                while ((what = parser.yylex()) != 8) {
                    if (what != 1) {
                        parser.unexpected();
                    }
                    final String key = parser.yytext();
                    parser.expect(9);
                    what = parser.yylex();
                    if (what != 10 && what != 8) {
                        parser.unexpected();
                    }
                    final String value = TextUtil.trim(parser.sb, 0);
                    try {
                        atrule.entries.put(key, value);
                    }
                    catch (IllegalArgumentException ex) {}
                    if (what == 8) {
                        break;
                    }
                }
                if (this.atrules == null) {
                    this.atrules = new ArrayList<AtRule>();
                }
                this.atrules.add(atrule);
            }
            else {
                Selector selector = null;
            Label_0419:
                while (true) {
                    String element = null;
                    String className = null;
                    String pseudoClass = null;
                    String id = null;
                    parser.sawWhitespace = false;
                    switch (what) {
                        default: {
                            parser.unexpected();
                            break;
                        }
                        case 3:
                        case 4:
                        case 9: {
                            break;
                        }
                        case 1: {
                            element = parser.yytext();
                        }
                        case 2: {
                            what = parser.yylex();
                            break;
                        }
                    }
                    while ((what == 3 || what == 4 || what == 9) && !parser.sawWhitespace) {
                        parser.expect(1);
                        final String text = parser.yytext();
                        if (what == 3) {
                            className = text;
                        }
                        else if (what == 9) {
                            pseudoClass = text;
                        }
                        else {
                            id = text;
                        }
                        what = parser.yylex();
                    }
                    selector = new Selector(element, className, id, pseudoClass, selector);
                    switch (what) {
                        case 5: {
                            selector.directChild = true;
                            what = parser.yylex();
                            continue;
                        }
                        case 6:
                        case 7: {
                            break Label_0419;
                        }
                    }
                }
                selector.directChild = true;
                selectors.add(selector);
                switch (what) {
                    default: {
                        parser.unexpected();
                    }
                    case 7: {
                        final CSSStyle style = new CSSStyle();
                        while ((what = parser.yylex()) != 8) {
                            if (what != 1) {
                                parser.unexpected();
                            }
                            final String key2 = parser.yytext();
                            parser.expect(9);
                            what = parser.yylex();
                            if (what != 10 && what != 8) {
                                parser.unexpected();
                            }
                            final String value2 = TextUtil.trim(parser.sb, 0);
                            try {
                                style.parseCSSAttribute(key2, value2);
                            }
                            catch (IllegalArgumentException ex2) {}
                            if (what == 8) {
                                break;
                            }
                        }
                        for (int i = 0, n = selectors.size(); i < n; ++i) {
                            Style selectorStyle = (Style)style;
                            selector = selectors.get(i);
                            if (selector.pseudoClass != null) {
                                selectorStyle = this.transformStyle(style, selector.pseudoClass);
                            }
                            this.rules.add(selector);
                            int score = 0;
                            for (Selector s = selector; s != null; s = s.tail) {
                                if (s.directChild) {
                                    ++score;
                                }
                                if (s.element != null) {
                                    score += 256;
                                }
                                if (s.className != null) {
                                    score += 65536;
                                }
                                if (s.id != null) {
                                    score += 16777216;
                                }
                            }
                            selector.score = score;
                            selector.style = selectorStyle;
                        }
                        selectors.clear();
                    }
                    case 6: {
                        continue;
                    }
                }
            }
        }
    }

    public int getNumAtRules() {
        return (this.atrules != null) ? this.atrules.size() : 0;
    }

    public AtRule getAtRule(final int idx) {
        if (this.atrules == null) {
            throw new IndexOutOfBoundsException();
        }
        return this.atrules.get(idx);
    }

    public void registerFonts(final FontMapper fontMapper, final URL baseUrl) {
        if (this.atrules == null) {
            return;
        }
        for (final AtRule atrule : this.atrules) {
            if ("font-face".equals(atrule.name)) {
                final String family = atrule.get("font-family");
                final String src = atrule.get("src");
                if (family == null || src == null) {
                    continue;
                }
                for (StringList srcs = CSSStyle.parseList(src, 0); srcs != null; srcs = srcs.getNext()) {
                    final String url = CSSStyle.stripURL(srcs.getValue());
                    try {
                        fontMapper.registerFont(family, new URL(baseUrl, url));
                    }
                    catch (IOException ex) {
                        Logger.getLogger(StyleSheet.class.getName()).log(Level.SEVERE, "Could not register font: " + url, ex);
                    }
                }
            }
        }
    }

    @Override
    public void layoutFinished() {
        this.cache.clear();
    }

    @Override
    public void startLayout() {
        this.cache.clear();
    }

    @Override
    public Style resolve(final Style style) {
        final Object cacheData = this.cache.get(style);
        if (cacheData == null) {
            return this.resolveSlow(style);
        }
        if (cacheData == StyleSheet.NULL) {
            return null;
        }
        return (Style)cacheData;
    }

    private Style resolveSlow(final Style style) {
        final Selector[] candidates = new Selector[this.rules.size()];
        int numCandidates = 0;
        for (int i = 0, n = this.rules.size(); i < n; ++i) {
            final Selector selector = this.rules.get(i);
            if (this.matches(selector, style)) {
                candidates[numCandidates++] = selector;
            }
        }
        if (numCandidates > 1) {
            Arrays.sort(candidates, 0, numCandidates);
        }
        Style result = null;
        boolean copy = true;
        for (int j = 0, n2 = numCandidates; j < n2; ++j) {
            final Style ruleStyle = candidates[j].style;
            if (result == null) {
                result = ruleStyle;
            }
            else {
                if (copy) {
                    result = new Style(result);
                    copy = false;
                }
                result.putAll(ruleStyle);
            }
        }
        this.putIntoCache(style, result);
        return result;
    }

    private void putIntoCache(final Style key, final Style style) {
        this.cache.put(key, (style == null) ? StyleSheet.NULL : style);
    }

    private boolean matches(Selector selector, Style style) {
        do {
            final StyleSheetKey styleSheetKey = style.getStyleSheetKey();
            if (styleSheetKey != null) {
                if (selector.matches(styleSheetKey)) {
                    selector = selector.tail;
                    if (selector == null) {
                        return true;
                    }
                }
                else if (selector.directChild) {
                    return false;
                }
            }
            style = style.getParent();
        } while (style != null);
        return false;
    }

    private Style transformStyle(final CSSStyle style, final String pseudoClass) {
        final Style result = new Style(style.getParent(), style.getStyleSheetKey());
        if ("hover".equals(pseudoClass)) {
            result.put(StyleAttribute.COLOR_HOVER, style.getRaw(StyleAttribute.COLOR));
            result.put(StyleAttribute.BACKGROUND_COLOR_HOVER, style.getRaw(StyleAttribute.BACKGROUND_COLOR));
            result.put(StyleAttribute.TEXT_DECORATION_HOVER, style.getRaw(StyleAttribute.TEXT_DECORATION));
        }
        return result;
    }

    static {
        NULL = new Object();
    }

    static class Selector extends StyleSheetKey implements Comparable<Selector>
    {
        final String pseudoClass;
        final Selector tail;
        boolean directChild;
        Style style;
        int score;

        Selector(final String element, final String className, final String id, final String pseudoClass, final Selector tail) {
            super(element, className, id);
            this.pseudoClass = pseudoClass;
            this.tail = tail;
        }

        @Override
        public int compareTo(final Selector other) {
            return this.score - other.score;
        }
    }

    public static class AtRule implements Iterable<Map.Entry<String, String>>
    {
        final String name;
        final HashMap<String, String> entries;

        public AtRule(final String name) {
            this.name = name;
            this.entries = new HashMap<String, String>();
        }

        public String getName() {
            return this.name;
        }

        public String get(final String key) {
            return this.entries.get(key);
        }

        @Override
        public Iterator<Map.Entry<String, String>> iterator() {
            return (Iterator<Map.Entry<String, String>>) Collections.unmodifiableSet((Set<? extends Map.Entry<String, String>>)this.entries.entrySet()).iterator();
        }
    }
}
