package de.matthiasmann.twl.model;

public class SimpleStringModel extends HasCallback implements StringModel
{
    private String value;
    
    public SimpleStringModel() {
        this.value = "";
    }
    
    public SimpleStringModel(final String value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
    
    public void setValue(final String value) {
        if (value == null) {
            throw new NullPointerException("value");
        }
        if (!this.value.equals(value)) {
            this.value = value;
            this.doCallback();
        }
    }
}
