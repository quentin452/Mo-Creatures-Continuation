package de.matthiasmann.twl.model;

import java.util.*;

public class SimpleGraphModel implements GraphModel
{
    private final ArrayList<GraphLineModel> lines;
    private boolean scaleLinesIndependant;
    
    public SimpleGraphModel() {
        this.lines = new ArrayList<GraphLineModel>();
    }
    
    public SimpleGraphModel(final GraphLineModel... lines) {
        this(Arrays.asList(lines));
    }
    
    public SimpleGraphModel(final Collection<GraphLineModel> lines) {
        this.lines = new ArrayList<GraphLineModel>(lines);
    }
    
    public GraphLineModel getLine(final int idx) {
        return this.lines.get(idx);
    }
    
    public int getNumLines() {
        return this.lines.size();
    }
    
    public boolean getScaleLinesIndependant() {
        return this.scaleLinesIndependant;
    }
    
    public void setScaleLinesIndependant(final boolean scaleLinesIndependant) {
        this.scaleLinesIndependant = scaleLinesIndependant;
    }
    
    public void addLine(final GraphLineModel line) {
        this.insertLine(this.lines.size(), line);
    }
    
    public void insertLine(final int idx, final GraphLineModel line) {
        if (line == null) {
            throw new NullPointerException("line");
        }
        if (this.indexOfLine(line) >= 0) {
            throw new IllegalArgumentException("line already added");
        }
        this.lines.add(idx, line);
    }
    
    public int indexOfLine(final GraphLineModel line) {
        for (int i = 0, n = this.lines.size(); i < n; ++i) {
            if (this.lines.get(i) == line) {
                return i;
            }
        }
        return -1;
    }
    
    public GraphLineModel removeLine(final int idx) {
        return this.lines.remove(idx);
    }
}
