//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Administrator\Downloads\Minecraft-Deobfuscator3000-1.2.3\Minecraft-Deobfuscator3000-1.2.3\1.7.10 stable mappings"!

//Decompiled by Procyon!

package org.xmlpull.mxp1;

import java.util.*;
import org.xmlpull.v1.*;
import org.xmlpull.mxp1_serializer.*;

public class MXParserFactory extends XmlPullParserFactory
{
    protected static boolean stringCachedParserAvailable;

    public XmlPullParser newPullParser() throws XmlPullParserException {
        XmlPullParser pp = null;
        if (MXParserFactory.stringCachedParserAvailable) {
            try {
                pp = (XmlPullParser)new MXParserCachingStrings();
            }
            catch (Exception ex) {
                MXParserFactory.stringCachedParserAvailable = false;
            }
        }
        if (pp == null) {
            pp = new MXParser();
        }
        final Enumeration e = super.features.keys();
        while (e.hasMoreElements()) {
            final String key = (String) e.nextElement();
            final Boolean value = (Boolean) super.features.get(key);
            if (value != null && value) {
                pp.setFeature(key, true);
            }
        }
        return pp;
    }

    public XmlSerializer newSerializer() throws XmlPullParserException {
        return new MXSerializer();
    }

    static {
        MXParserFactory.stringCachedParserAvailable = true;
    }
}
