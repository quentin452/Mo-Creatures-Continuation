package drzhark.guiapi;

import de.matthiasmann.twl.*;
import java.util.*;
import java.lang.reflect.*;
import java.beans.*;
import java.security.*;

public class ModAction implements Runnable, PropertyChangeListener, TextArea.Callback, CallbackWithReason<ListBox.CallbackReason>
{
    private Object[] defaultArguments;
    private ArrayList<ModAction> mergedActions;
    private String methodName;
    private Class[] methodParams;
    private Object objectRef;
    private Object tag;
    
    private static Boolean checkArguments(final Class[] classTypes, final Object[] arguments) {
        if (classTypes.length != arguments.length) {
            return false;
        }
        for (int i = 0; i < classTypes.length; ++i) {
            if (!classTypes[i].isAssignableFrom(arguments[i].getClass())) {
                return false;
            }
        }
        return true;
    }
    
    public ModAction(final Object o, final String method, final Class... params) {
        this.mergedActions = new ArrayList<ModAction>();
        this.methodParams = new Class[0];
        this.setTag(method);
        this.methodParams = params;
        this.setupHandler(o, method);
    }
    
    public ModAction(final Object o, final String method, final String name, final Class... params) {
        this(o, method, params);
        this.setTag(name);
    }
    
    private ModAction(final String name) {
        this.mergedActions = new ArrayList<ModAction>();
        this.methodParams = new Class[0];
        this.setTag(name);
    }
    
    public Object[] call(final Object... args) throws Exception {
        try {
            if (this.mergedActions.isEmpty()) {
                return new Object[] { this.callInternal(args) };
            }
            final Object[] returnvals = new Object[this.mergedActions.size()];
            for (int i = 0; i < returnvals.length; ++i) {
                returnvals[i] = this.mergedActions.get(i).call(args);
            }
            return returnvals;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new Exception("error calling callback '" + this.getTag() + "'.", e);
        }
    }
    
    public void callback(final ListBox.CallbackReason reason) {
        if (this.methodParams.length != 1 || this.methodParams[0] != ListBox.CallbackReason.class) {
            throw new RuntimeException("invalid method parameters for a CallbackWithReason<ListBox.CallbackReason> callback. Modaction is '" + this.getTag() + "'.");
        }
        try {
            this.call(reason);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error when calling CallbackWithReason<ListBox.CallbackReason> callback. Modaction is '" + this.getTag() + "'.", e);
        }
    }
    
    private Object callInternal(Object... args) throws Exception {
        if (!checkArguments(this.methodParams, args) && this.defaultArguments != null) {
            args = this.defaultArguments;
        }
        try {
            final Method meth = this.getMethodRecursively(this.objectRef, this.methodName);
            return meth.invoke((this.objectRef instanceof Class) ? null : this.objectRef, args);
        }
        catch (Exception e) {
            throw new Exception("error calling callback '" + this.getTag() + "'.", e);
        }
    }
    
    private Method getMethodRecursively(final Object o, final String method) throws Exception {
        for (Class<?> currentclass = (Class<?>)((o instanceof Class) ? ((Class)o) : o.getClass()); currentclass != null; currentclass = currentclass.getSuperclass()) {
            try {
                final Method returnval = currentclass.getDeclaredMethod(method, (Class<?>[])this.methodParams);
                if (returnval != null) {
                    returnval.setAccessible(true);
                    return returnval;
                }
            }
            catch (Throwable t) {}
        }
        throw new Exception("Unable to locate method '" + method + "' anywhere in the inheritance chain of object '" + ((o instanceof Class) ? ((Class)o) : o.getClass()).getName() + "'!");
    }
    
    public Object getTag() {
        return this.tag;
    }
    
    public void handleLinkClicked(final String link) {
        if (this.methodParams.length != 1 || this.methodParams[0] != String.class) {
            throw new RuntimeException("invalid method parameters for a TextArea.Callback callback. Modaction is '" + this.getTag() + "'.");
        }
        try {
            this.call(link);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error when calling TextArea.Callback callback. Modaction is '" + this.getTag() + "'.", e);
        }
    }
    
    public ModAction mergeAction(final ModAction... newActions) {
        if (this.mergedActions.isEmpty()) {
            final ModAction merged = new ModAction("Merged ModAction");
            merged.mergedActions.add(this);
            for (final ModAction modAction : newActions) {
                merged.mergedActions.add(modAction);
            }
            return merged;
        }
        for (final ModAction modAction2 : newActions) {
            this.mergedActions.add(modAction2);
        }
        return this;
    }
    
    @Override
    public void propertyChange(final PropertyChangeEvent paramPropertyChangeEvent) {
        if (this.methodParams.length != 1 || this.methodParams[0] != PropertyChangeEvent.class) {
            throw new RuntimeException("invalid method parameters for a PropertyChangeListener callback. Modaction is '" + this.getTag() + "'.");
        }
        try {
            this.call(paramPropertyChangeEvent);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error when calling PropertyChangeListener callback. Modaction is '" + this.getTag() + "'.", e);
        }
    }
    
    @Override
    public void run() {
        try {
            this.call(new Object[0]);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error when calling Runnable callback. Modaction is '" + this.getTag() + "'.", e);
        }
    }
    
    public ModAction setDefaultArguments(final Object... Arguments) {
        if (!checkArguments(this.methodParams, Arguments)) {
            throw new InvalidParameterException("Arguments do not match the parameters.");
        }
        this.defaultArguments = Arguments;
        return this;
    }
    
    public void setTag(final Object tag) {
        this.tag = tag;
    }
    
    private void setupHandler(final Object o, final String method) {
        try {
            this.getMethodRecursively(o, method);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not locate Method with included information.", e);
        }
        this.methodName = method;
        this.objectRef = o;
    }
    
    @Override
    public String toString() {
        return "ModAction [methodName=" + this.methodName + ", tag=" + this.tag + "]";
    }
}
