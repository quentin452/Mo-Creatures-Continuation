package de.matthiasmann.twl;

import java.util.*;

public class DebugHook
{
    private static ThreadLocal<DebugHook> tls;
    
    public static DebugHook getDebugHook() {
        return DebugHook.tls.get();
    }
    
    public static DebugHook installHook(final DebugHook hook) {
        if (hook == null) {
            throw new NullPointerException("hook");
        }
        final DebugHook old = DebugHook.tls.get();
        DebugHook.tls.set(hook);
        return old;
    }
    
    public void beforeApplyTheme(final Widget widget) {
    }
    
    public void afterApplyTheme(final Widget widget) {
    }
    
    public void missingTheme(final String themePath) {
        System.err.println("Could not find theme: " + themePath);
    }
    
    public void missingChildTheme(final ThemeInfo parent, final String theme) {
        System.err.println("Missing child theme \"" + theme + "\" for \"" + parent.getThemePath() + "\"");
    }
    
    public void missingParameter(final ParameterMap map, final String paramName, final String parentDescription, final Class<?> dataType) {
        final StringBuilder sb = new StringBuilder("Parameter \"").append(paramName).append("\" ");
        if (dataType != null) {
            sb.append("of type ");
            if (dataType.isEnum()) {
                sb.append("enum ");
            }
            sb.append('\"').append(dataType.getSimpleName()).append('\"');
        }
        sb.append(" not set");
        if (map instanceof ThemeInfo) {
            sb.append(" for \"").append(((ThemeInfo)map).getThemePath()).append("\"");
        }
        else {
            sb.append(parentDescription);
        }
        System.err.println(sb.toString());
    }
    
    public void wrongParameterType(final ParameterMap map, final String paramName, final Class<?> expectedType, final Class<?> foundType, final String parentDescription) {
        System.err.println("Parameter \"" + paramName + "\" is a " + foundType.getSimpleName() + " expected a " + expectedType.getSimpleName() + parentDescription);
    }
    
    public void wrongParameterType(final ParameterList map, final int idx, final Class<?> expectedType, final Class<?> foundType, final String parentDescription) {
        System.err.println("Parameter at index " + idx + " is a " + foundType.getSimpleName() + " expected a " + expectedType.getSimpleName() + parentDescription);
    }
    
    public void replacingWithDifferentType(final ParameterMap map, final String paramName, final Class<?> oldType, final Class<?> newType, final String parentDescription) {
        System.err.println("Paramter \"" + paramName + "\" of type " + oldType + " is replaced with type " + newType + parentDescription);
    }
    
    public void missingImage(final String name) {
        System.err.println("Could not find image: " + name);
    }
    
    public void guiLayoutValidated(final int iterations, final Collection<Widget> loop) {
        if (loop != null) {
            System.err.println("WARNING: layout loop detected - printing");
            int index = 1;
            for (final Widget w : loop) {
                System.err.println(index + ": " + w);
                ++index;
            }
        }
    }
    
    public void usingFallbackTheme(final String themePath) {
    }
    
    static {
        DebugHook.tls = new ThreadLocal<DebugHook>() {
            @Override
            protected DebugHook initialValue() {
                return new DebugHook();
            }
        };
    }
}
