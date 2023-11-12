package de.matthiasmann.twl.utils;

import java.util.*;

public class ClassUtils
{
    private static final HashMap<Class<?>, Class<?>> primitiveTypeMap;
    
    private ClassUtils() {
    }
    
    public static Class<?> mapPrimitiveToWrapper(final Class<?> clazz) {
        final Class<?> mappedClass = ClassUtils.primitiveTypeMap.get(clazz);
        return (mappedClass != null) ? mappedClass : clazz;
    }
    
    public static boolean isParamCompatible(Class<?> type, final Object obj) {
        if (obj == null && !type.isPrimitive()) {
            return true;
        }
        type = mapPrimitiveToWrapper(type);
        return type.isInstance(obj);
    }
    
    public static boolean isParamsCompatible(final Class<?>[] types, final Object[] params) {
        if (types.length != params.length) {
            return false;
        }
        for (int i = 0; i < types.length; ++i) {
            if (!isParamCompatible(types[i], params[i])) {
                return false;
            }
        }
        return true;
    }
    
    static {
        (primitiveTypeMap = new HashMap<Class<?>, Class<?>>()).put(Boolean.TYPE, Boolean.class);
        ClassUtils.primitiveTypeMap.put(Byte.TYPE, Byte.class);
        ClassUtils.primitiveTypeMap.put(Short.TYPE, Short.class);
        ClassUtils.primitiveTypeMap.put(Character.TYPE, Character.class);
        ClassUtils.primitiveTypeMap.put(Integer.TYPE, Integer.class);
        ClassUtils.primitiveTypeMap.put(Long.TYPE, Long.class);
        ClassUtils.primitiveTypeMap.put(Float.TYPE, Float.class);
        ClassUtils.primitiveTypeMap.put(Double.TYPE, Double.class);
    }
}
