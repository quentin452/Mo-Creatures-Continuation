package de.matthiasmann.twl;

public interface CallbackWithReason<T extends Enum<T>>
{
    void callback(final T p0);
}
