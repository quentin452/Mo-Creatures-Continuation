package de.matthiasmann.twl.utils;

import java.lang.reflect.*;
import de.matthiasmann.twl.*;

public class CallbackSupport
{
    private CallbackSupport() {
    }

    private static void checkNotNull(final Object callback) {
        if (callback == null) {
            throw new NullPointerException("callback");
        }
    }

    public static <T> T[] addCallbackToList(final T[] curList, final T callback, final Class<T> clazz) {
        checkNotNull(callback);
        final int curLength = (curList == null) ? 0 : curList.length;
        final T[] newList = (T[])Array.newInstance(clazz, curLength + 1);
        if (curLength > 0) {
            System.arraycopy(curList, 0, newList, 0, curLength);
        }
        newList[curLength] = callback;
        return newList;
    }

    public static <T> int findCallbackPosition(final T[] list, final T callback) {
        checkNotNull(callback);
        if (list != null) {
            for (int i = 0, n = list.length; i < n; ++i) {
                if (list[i] == callback) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static <T> T[] removeCallbackFromList(final T[] curList, final int index) {
        final int curLength = curList.length;
        assert index >= 0 && index < curLength;
        if (curLength == 1) {
            return null;
        }
        final int newLength = curLength - 1;
        final T[] newList = (T[])Array.newInstance(curList.getClass().getComponentType(), newLength);
        System.arraycopy(curList, 0, newList, 0, index);
        System.arraycopy(curList, index + 1, newList, index, newLength - index);
        return newList;
    }

    public static <T> T[] removeCallbackFromList(T[] curList, final T callback) {
        final int idx = findCallbackPosition(curList, callback);
        if (idx >= 0) {
            curList = removeCallbackFromList(curList, idx);
        }
        return curList;
    }

    public static void fireCallbacks(final Runnable[] callbacks) {
        if (callbacks != null) {
            for (final Runnable cb : callbacks) {
                cb.run();
            }
        }
    }

    public static <T extends Enum<T>> void fireCallbacks(final CallbackWithReason<T>[] callbacks, final T reason) {
        if (callbacks != null) {
            Class<T> enumClass = reason.getDeclaringClass();
            for (final CallbackWithReason<T> cb : callbacks) {
                cb.callback(Enum.valueOf(enumClass, reason.name()));
            }
        }
    }
}
