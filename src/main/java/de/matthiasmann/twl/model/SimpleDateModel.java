package de.matthiasmann.twl.model;

public class SimpleDateModel extends HasCallback implements DateModel
{
    private long date;
    
    public SimpleDateModel() {
        this.date = System.currentTimeMillis();
    }
    
    public SimpleDateModel(final long date) {
        this.date = date;
    }
    
    public long getValue() {
        return this.date;
    }
    
    public void setValue(final long date) {
        if (this.date != date) {
            this.date = date;
            this.doCallback();
        }
    }
}
