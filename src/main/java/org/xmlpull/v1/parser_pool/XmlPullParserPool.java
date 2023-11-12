//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Administrator\Downloads\Minecraft-Deobfuscator3000-1.2.3\Minecraft-Deobfuscator3000-1.2.3\1.7.10 stable mappings"!

//Decompiled by Procyon!

package org.xmlpull.v1.parser_pool;

import java.util.*;
import org.xmlpull.v1.*;

public class XmlPullParserPool
{
    protected final List pool;
    protected XmlPullParserFactory factory;

    public XmlPullParserPool() throws XmlPullParserException {
        this(XmlPullParserFactory.newInstance());
    }

    public XmlPullParserPool(final XmlPullParserFactory factory) {
        this.pool = new ArrayList();
        if (factory == null) {
            throw new IllegalArgumentException();
        }
        this.factory = factory;
    }

    protected XmlPullParser newParser() throws XmlPullParserException {
        return this.factory.newPullParser();
    }

    public XmlPullParser getPullParserFromPool() throws XmlPullParserException {
        XmlPullParser pp = null;
        if (!this.pool.isEmpty()) {
            synchronized (this.pool) {
                if (!this.pool.isEmpty()) {
                    pp = (XmlPullParser) this.pool.remove(this.pool.size() - 1);
                }
            }
        }
        if (pp == null) {
            pp = this.newParser();
        }
        return pp;
    }

    public void returnPullParserToPool(final XmlPullParser pp) {
        if (pp == null) {
            throw new IllegalArgumentException();
        }
        synchronized (this.pool) {
            this.pool.add(pp);
        }
    }

    public static void main(final String[] args) throws Exception {
        final XmlPullParserPool pool = new XmlPullParserPool();
        final XmlPullParser p1 = pool.getPullParserFromPool();
        pool.returnPullParserToPool(p1);
        final XmlPullParser p2 = pool.getPullParserFromPool();
        if (p1 != p2) {
            throw new RuntimeException();
        }
        pool.returnPullParserToPool(p2);
        System.out.println(pool.getClass() + " OK");
    }
}
