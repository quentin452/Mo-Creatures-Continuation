package de.matthiasmann.twl.utils;

import java.util.*;
import java.text.*;
import java.util.logging.*;
import java.lang.reflect.*;

public abstract class AbstractMathInterpreter implements SimpleMathParser.Interpreter
{
    private final ArrayList<Object> stack;
    private final HashMap<String, Function> functions;
    
    public AbstractMathInterpreter() {
        this.stack = new ArrayList<Object>();
        this.functions = new HashMap<String, Function>();
        this.registerFunction("min", new FunctionMin());
        this.registerFunction("max", new FunctionMax());
    }
    
    public final void registerFunction(final String name, final Function function) {
        if (function == null) {
            throw new NullPointerException("function");
        }
        this.functions.put(name, function);
    }
    
    public Number execute(final String str) throws ParseException {
        this.stack.clear();
        SimpleMathParser.interpret(str, this);
        if (this.stack.size() != 1) {
            throw new IllegalStateException("Expected one return value on the stack");
        }
        return this.popNumber();
    }
    
    public int[] executeIntArray(final String str) throws ParseException {
        this.stack.clear();
        final int count = SimpleMathParser.interpretArray(str, this);
        if (this.stack.size() != count) {
            throw new IllegalStateException("Expected " + count + " return values on the stack");
        }
        final int[] result = new int[count];
        int i = count;
        while (i-- > 0) {
            result[i] = this.popNumber().intValue();
        }
        return result;
    }
    
    public <T> T executeCreateObject(final String str, final Class<T> type) throws ParseException {
        this.stack.clear();
        final int count = SimpleMathParser.interpretArray(str, this);
        if (this.stack.size() != count) {
            throw new IllegalStateException("Expected " + count + " return values on the stack");
        }
        if (count == 1 && type.isInstance(this.stack.get(0))) {
            return type.cast(this.stack.get(0));
        }
        for (final Constructor<?> c : type.getConstructors()) {
            final Class<?>[] params = c.getParameterTypes();
            if (params.length == count) {
                boolean match = true;
                for (int i = 0; i < count; ++i) {
                    if (!ClassUtils.isParamCompatible(params[i], this.stack.get(i))) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    try {
                        return type.cast(c.newInstance(this.stack.toArray(new Object[count])));
                    }
                    catch (Exception ex) {
                        Logger.getLogger(AbstractMathInterpreter.class.getName()).log(Level.SEVERE, "can't instantiate object", ex);
                    }
                }
            }
        }
        throw new IllegalArgumentException("Can't construct a " + type + " from expression: \"" + str + "\"");
    }
    
    protected void push(final Object obj) {
        this.stack.add(obj);
    }
    
    protected Object pop() {
        final int size = this.stack.size();
        if (size == 0) {
            throw new IllegalStateException("stack underflow");
        }
        return this.stack.remove(size - 1);
    }
    
    protected Number popNumber() {
        final Object obj = this.pop();
        if (obj instanceof Number) {
            return (Number)obj;
        }
        throw new IllegalStateException("expected number on stack - found: " + ((obj != null) ? obj.getClass() : "null"));
    }
    
    @Override
    public void loadConst(final Number n) {
        this.push(n);
    }
    
    @Override
    public void add() {
        final Number b = this.popNumber();
        final Number a = this.popNumber();
        final boolean isFloat = isFloat(a) || isFloat(b);
        if (isFloat) {
            this.push(a.floatValue() + b.floatValue());
        }
        else {
            this.push(a.intValue() + b.intValue());
        }
    }
    
    @Override
    public void sub() {
        final Number b = this.popNumber();
        final Number a = this.popNumber();
        final boolean isFloat = isFloat(a) || isFloat(b);
        if (isFloat) {
            this.push(a.floatValue() - b.floatValue());
        }
        else {
            this.push(a.intValue() - b.intValue());
        }
    }
    
    @Override
    public void mul() {
        final Number b = this.popNumber();
        final Number a = this.popNumber();
        final boolean isFloat = isFloat(a) || isFloat(b);
        if (isFloat) {
            this.push(a.floatValue() * b.floatValue());
        }
        else {
            this.push(a.intValue() * b.intValue());
        }
    }
    
    @Override
    public void div() {
        final Number b = this.popNumber();
        final Number a = this.popNumber();
        final boolean isFloat = isFloat(a) || isFloat(b);
        if (isFloat) {
            if (Math.abs(b.floatValue()) == 0.0f) {
                throw new IllegalStateException("division by zero");
            }
            this.push(a.floatValue() / b.floatValue());
        }
        else {
            if (b.intValue() == 0) {
                throw new IllegalStateException("division by zero");
            }
            this.push(a.intValue() / b.intValue());
        }
    }
    
    @Override
    public void negate() {
        final Number a = this.popNumber();
        if (isFloat(a)) {
            this.push(-a.floatValue());
        }
        else {
            this.push(-a.intValue());
        }
    }
    
    @Override
    public void accessArray() {
        final Number idx = this.popNumber();
        final Object obj = this.pop();
        if (obj == null) {
            throw new IllegalStateException("null pointer");
        }
        if (!obj.getClass().isArray()) {
            throw new IllegalStateException("array expected");
        }
        try {
            this.push(Array.get(obj, idx.intValue()));
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            throw new IllegalStateException("array index out of bounds", ex);
        }
    }
    
    @Override
    public void accessField(final String field) {
        final Object obj = this.pop();
        if (obj == null) {
            throw new IllegalStateException("null pointer");
        }
        final Object result = this.accessField(obj, field);
        this.push(result);
    }
    
    protected Object accessField(final Object obj, final String field) {
        final Class<?> clazz = obj.getClass();
        try {
            if (clazz.isArray()) {
                if ("length".equals(field)) {
                    return Array.getLength(obj);
                }
            }
            else {
                Method m = findGetter(clazz, field);
                if (m == null) {
                    for (final Class<?> i : clazz.getInterfaces()) {
                        m = findGetter(i, field);
                        if (m != null) {
                            break;
                        }
                    }
                }
                if (m != null) {
                    return m.invoke(obj, new Object[0]);
                }
            }
        }
        catch (Throwable ex) {
            throw new IllegalStateException("error accessing field '" + field + "' of class '" + clazz + "'", ex);
        }
        throw new IllegalStateException("unknown field '" + field + "' of class '" + clazz + "'");
    }
    
    private static Method findGetter(final Class<?> clazz, final String field) {
        for (final Method m : clazz.getMethods()) {
            if (!Modifier.isStatic(m.getModifiers()) && m.getReturnType() != Void.TYPE && Modifier.isPublic(m.getDeclaringClass().getModifiers()) && m.getParameterTypes().length == 0 && (cmpName(m, field, "get") || cmpName(m, field, "is"))) {
                return m;
            }
        }
        return null;
    }
    
    private static boolean cmpName(final Method m, final String fieldName, final String prefix) {
        final String methodName = m.getName();
        final int prefixLength = prefix.length();
        final int fieldNameLength = fieldName.length();
        return methodName.length() == prefixLength + fieldNameLength && methodName.startsWith(prefix) && methodName.charAt(prefixLength) == Character.toUpperCase(fieldName.charAt(0)) && methodName.regionMatches(prefixLength + 1, fieldName, 1, fieldNameLength - 1);
    }
    
    @Override
    public void callFunction(final String name, final int args) {
        final Object[] values = new Object[args];
        int i = args;
        while (i-- > 0) {
            values[i] = this.pop();
        }
        final Function function = this.functions.get(name);
        if (function == null) {
            throw new IllegalArgumentException("Unknown function");
        }
        this.push(function.execute(values));
    }
    
    protected static boolean isFloat(final Number n) {
        return !(n instanceof Integer);
    }
    
    public abstract static class NumberFunction implements Function
    {
        protected abstract Object execute(final int... p0);
        
        protected abstract Object execute(final float... p0);
        
        @Override
        public Object execute(final Object... args) {
            for (final Object o : args) {
                if (!(o instanceof Integer)) {
                    final float[] values = new float[args.length];
                    for (int i = 0; i < values.length; ++i) {
                        values[i] = ((Number)args[i]).floatValue();
                    }
                    return this.execute(values);
                }
            }
            final int[] values2 = new int[args.length];
            for (int j = 0; j < values2.length; ++j) {
                values2[j] = ((Number)args[j]).intValue();
            }
            return this.execute(values2);
        }
    }
    
    static class FunctionMin extends NumberFunction
    {
        @Override
        protected Object execute(final int... values) {
            int result = values[0];
            for (int i = 1; i < values.length; ++i) {
                result = Math.min(result, values[i]);
            }
            return result;
        }
        
        @Override
        protected Object execute(final float... values) {
            float result = values[0];
            for (int i = 1; i < values.length; ++i) {
                result = Math.min(result, values[i]);
            }
            return result;
        }
    }
    
    static class FunctionMax extends NumberFunction
    {
        @Override
        protected Object execute(final int... values) {
            int result = values[0];
            for (int i = 1; i < values.length; ++i) {
                result = Math.max(result, values[i]);
            }
            return result;
        }
        
        @Override
        protected Object execute(final float... values) {
            float result = values[0];
            for (int i = 1; i < values.length; ++i) {
                result = Math.max(result, values[i]);
            }
            return result;
        }
    }
    
    public interface Function
    {
        Object execute(final Object... p0);
    }
}
