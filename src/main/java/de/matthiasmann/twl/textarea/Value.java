package de.matthiasmann.twl.textarea;

public final class Value
{
    public final float value;
    public final Unit unit;
    public static final Value ZERO_PX;
    public static final Value AUTO;
    
    public Value(final float value, final Unit unit) {
        this.value = value;
        this.unit = unit;
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        if (unit == Unit.AUTO && value != 0.0f) {
            throw new IllegalArgumentException("value must be 0 for Unit.AUTO");
        }
    }
    
    @Override
    public String toString() {
        if (this.unit == Unit.AUTO) {
            return this.unit.getPostfix();
        }
        return this.value + this.unit.getPostfix();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Value) {
            final Value other = (Value)obj;
            return this.value == other.value && this.unit == other.unit;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Float.floatToIntBits(this.value);
        hash = 17 * hash + this.unit.hashCode();
        return hash;
    }
    
    static {
        ZERO_PX = new Value(0.0f, Unit.PX);
        AUTO = new Value(0.0f, Unit.AUTO);
    }
    
    public enum Unit
    {
        PX(false, "px"), 
        PT(false, "pt"), 
        EM(true, "em"), 
        EX(true, "ex"), 
        PERCENT(false, "%"), 
        AUTO(false, "auto");
        
        final boolean fontBased;
        final String postfix;
        
        private Unit(final boolean fontBased, final String postfix) {
            this.fontBased = fontBased;
            this.postfix = postfix;
        }
        
        public boolean isFontBased() {
            return this.fontBased;
        }
        
        public String getPostfix() {
            return this.postfix;
        }
    }
}
