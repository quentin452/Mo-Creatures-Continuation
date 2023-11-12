package de.matthiasmann.twl.model;

public class BitfieldBooleanModel extends HasCallback implements BooleanModel
{
    private final IntegerModel bitfield;
    private final int bitmask;
    
    public BitfieldBooleanModel(final IntegerModel bitfield, final int bit) {
        if (bitfield == null) {
            throw new NullPointerException("bitfield");
        }
        if (bit < 0 || bit > 30) {
            throw new IllegalArgumentException("invalid bit index");
        }
        if (bitfield.getMinValue() != 0) {
            throw new IllegalArgumentException("bitfield.getMinValue() != 0");
        }
        final int bitfieldMax = bitfield.getMaxValue();
        if ((bitfieldMax & bitfieldMax + 1) != 0x0) {
            throw new IllegalArgumentException("bitfield.getmaxValue() must eb 2^x");
        }
        if (bitfieldMax < 1 << bit) {
            throw new IllegalArgumentException("bit index outside of bitfield range");
        }
        this.bitfield = bitfield;
        this.bitmask = 1 << bit;
        bitfield.addCallback(new CB());
    }
    
    @Override
    public boolean getValue() {
        return (this.bitfield.getValue() & this.bitmask) != 0x0;
    }
    
    @Override
    public void setValue(final boolean value) {
        final int oldBFValue = this.bitfield.getValue();
        final int newBFValue = value ? (oldBFValue | this.bitmask) : (oldBFValue & ~this.bitmask);
        if (oldBFValue != newBFValue) {
            this.bitfield.setValue(newBFValue);
        }
    }
    
    class CB implements Runnable
    {
        @Override
        public void run() {
            BitfieldBooleanModel.this.doCallback();
        }
    }
}
