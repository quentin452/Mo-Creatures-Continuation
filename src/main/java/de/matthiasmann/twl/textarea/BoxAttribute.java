package de.matthiasmann.twl.textarea;

public class BoxAttribute
{
    public final StyleAttribute<Value> top;
    public final StyleAttribute<Value> left;
    public final StyleAttribute<Value> right;
    public final StyleAttribute<Value> bottom;
    
    BoxAttribute(final StyleAttribute<Value> top, final StyleAttribute<Value> left, final StyleAttribute<Value> right, final StyleAttribute<Value> bottom) {
        this.top = top;
        this.left = left;
        this.right = right;
        this.bottom = bottom;
    }
}
