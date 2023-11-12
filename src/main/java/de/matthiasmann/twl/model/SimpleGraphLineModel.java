package de.matthiasmann.twl.model;

public class SimpleGraphLineModel implements GraphLineModel
{
    private String visualStyleName;
    private float minValue;
    private float maxValue;
    private float[] data;
    
    public SimpleGraphLineModel(final String style, final int size, final float minValue, final float maxValue) {
        this.minValue = 0.0f;
        this.maxValue = 100.0f;
        this.setVisualStyleName(style);
        this.data = new float[size];
        this.minValue = minValue;
        this.maxValue = maxValue;
    }
    
    public String getVisualStyleName() {
        return this.visualStyleName;
    }
    
    public void setVisualStyleName(final String visualStyleName) {
        if (visualStyleName.length() < 1) {
            throw new IllegalArgumentException("Invalid style name");
        }
        this.visualStyleName = visualStyleName;
    }
    
    public int getNumPoints() {
        return this.data.length;
    }
    
    public float getPoint(final int idx) {
        return this.data[idx];
    }
    
    public float getMinValue() {
        return this.minValue;
    }
    
    public float getMaxValue() {
        return this.maxValue;
    }
    
    public void addPoint(final float value) {
        System.arraycopy(this.data, 1, this.data, 0, this.data.length - 1);
        this.data[this.data.length - 1] = value;
    }
    
    public void setMaxValue(final float maxValue) {
        this.maxValue = maxValue;
    }
    
    public void setMinValue(final float minValue) {
        this.minValue = minValue;
    }
    
    public void setNumPoints(final int numPoints) {
        final float[] newData = new float[numPoints];
        final int overlap = Math.min(this.data.length, numPoints);
        System.arraycopy(this.data, this.data.length - overlap, newData, numPoints - overlap, overlap);
        this.data = newData;
    }
}
