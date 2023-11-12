package de.matthiasmann.twl.model;

public interface GraphModel
{
    int getNumLines();
    
    GraphLineModel getLine(final int p0);
    
    boolean getScaleLinesIndependant();
}
