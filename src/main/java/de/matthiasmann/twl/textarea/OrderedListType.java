package de.matthiasmann.twl.textarea;

import de.matthiasmann.twl.utils.*;

public class OrderedListType
{
    public static final OrderedListType DECIMAL;
    protected final String characterList;
    
    public OrderedListType() {
        this.characterList = null;
    }
    
    public OrderedListType(final String characterList) {
        this.characterList = characterList;
    }
    
    public String format(final int nr) {
        if (nr >= 1 && this.characterList != null) {
            return TextUtil.toCharListNumber(nr, this.characterList);
        }
        return Integer.toString(nr);
    }
    
    static {
        DECIMAL = new OrderedListType();
    }
}
