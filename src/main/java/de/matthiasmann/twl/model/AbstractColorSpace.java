package de.matthiasmann.twl.model;

public abstract class AbstractColorSpace implements ColorSpace
{
    private final String colorSpaceName;
    private final String[] names;
    
    public AbstractColorSpace(final String colorSpaceName, final String... names) {
        this.colorSpaceName = colorSpaceName;
        this.names = names;
    }
    
    @Override
    public String getComponentName(final int component) {
        return this.names[component];
    }
    
    @Override
    public String getColorSpaceName() {
        return this.colorSpaceName;
    }
    
    @Override
    public int getNumComponents() {
        return this.names.length;
    }
    
    @Override
    public float getMinValue(final int component) {
        return 0.0f;
    }
}
