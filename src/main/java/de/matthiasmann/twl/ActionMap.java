package de.matthiasmann.twl;

import de.matthiasmann.twl.utils.*;
import java.lang.reflect.*;
import java.util.logging.*;
import java.lang.annotation.*;

public class ActionMap
{
    public static final int FLAG_ON_PRESSED = 1;
    public static final int FLAG_ON_RELEASE = 2;
    public static final int FLAG_ON_REPEAT = 4;
    private Mapping[] mappings;
    private int numMappings;
    
    public ActionMap() {
        this.mappings = new Mapping[16];
    }
    
    public boolean invoke(final String action, final Event event) {
        final Mapping mapping = HashEntry.get(this.mappings, action);
        if (mapping != null) {
            mapping.call(event);
            return true;
        }
        return false;
    }
    
    public void addMapping(final String action, final Object target, final String methodName, final Object[] params, final int flags) throws IllegalArgumentException {
        if (action == null) {
            throw new NullPointerException("action");
        }
        for (final Method m : target.getClass().getMethods()) {
            if (m.getName().equals(methodName) && !Modifier.isStatic(m.getModifiers()) && ClassUtils.isParamsCompatible(m.getParameterTypes(), params)) {
                this.addMappingImpl(action, target, m, params, flags);
                return;
            }
        }
        throw new IllegalArgumentException("Can't find matching method: " + methodName);
    }
    
    public void addMapping(final String action, final Class<?> targetClass, final String methodName, final Object[] params, final int flags) throws IllegalArgumentException {
        if (action == null) {
            throw new NullPointerException("action");
        }
        for (final Method m : targetClass.getMethods()) {
            if (m.getName().equals(methodName) && Modifier.isStatic(m.getModifiers()) && ClassUtils.isParamsCompatible(m.getParameterTypes(), params)) {
                this.addMappingImpl(action, null, m, params, flags);
                return;
            }
        }
        throw new IllegalArgumentException("Can't find matching method: " + methodName);
    }
    
    public void addMapping(final String action, final Object target, final Method method, final Object[] params, final int flags) {
        if (action == null) {
            throw new NullPointerException("action");
        }
        if (!Modifier.isPublic(method.getModifiers())) {
            throw new IllegalArgumentException("Method is not public");
        }
        if (target == null && !Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException("Method is not static but target is null");
        }
        if (target != null && method.getDeclaringClass().isInstance(target)) {
            throw new IllegalArgumentException("method does not belong to target");
        }
        if (!ClassUtils.isParamsCompatible(method.getParameterTypes(), params)) {
            throw new IllegalArgumentException("Paramters don't match method");
        }
        this.addMappingImpl(action, target, method, params, flags);
    }
    
    public void addMapping(final Object target) {
        for (final Method m : target.getClass().getMethods()) {
            final Action action = m.getAnnotation(Action.class);
            if (action != null) {
                if (m.getParameterTypes().length > 0) {
                    throw new UnsupportedOperationException("automatic binding of actions not supported for methods with parameters");
                }
                String name = m.getName();
                if (action.name().length() > 0) {
                    name = action.name();
                }
                final int flags = (action.onPressed() ? 1 : 0) | (action.onRelease() ? 2 : 0) | (action.onRepeat() ? 4 : 0);
                this.addMappingImpl(name, target, m, null, flags);
            }
        }
    }
    
    protected void addMappingImpl(final String action, final Object target, final Method method, final Object[] params, final int flags) {
        HashEntry.insertEntry(this.mappings = HashEntry.maybeResizeTable(this.mappings, this.numMappings++), new Mapping(action, target, method, params, flags));
    }
    
    static class Mapping extends HashEntry<String, Mapping>
    {
        final Object target;
        final Method method;
        final Object[] params;
        final int flags;
        
        Mapping(final String key, final Object target, final Method method, final Object[] params, final int flags) {
            super(key);
            this.target = target;
            this.method = method;
            this.params = params;
            this.flags = flags;
        }
        
        void call(final Event e) {
            final Event.Type type = e.getType();
            if (type != Event.Type.KEY_RELEASED || (this.flags & 0x2) == 0x0) {
                if (type != Event.Type.KEY_PRESSED || (this.flags & 0x1) == 0x0) {
                    return;
                }
                if (e.isKeyRepeated()) {
                    if ((this.flags & 0x4) == 0x0) {
                        return;
                    }
                }
            }
            try {
                this.method.invoke(this.target, this.params);
            }
            catch (Exception ex) {
                Logger.getLogger(ActionMap.class.getName()).log(Level.SEVERE, "Exception while invoking action handler", ex);
            }
        }
    }
    
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD })
    public @interface Action {
        String name() default "";
        
        boolean onPressed() default true;
        
        boolean onRelease() default false;
        
        boolean onRepeat() default true;
    }
}
