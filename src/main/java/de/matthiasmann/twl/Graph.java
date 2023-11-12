package de.matthiasmann.twl;

import java.util.*;
import de.matthiasmann.twl.utils.*;
import de.matthiasmann.twl.model.*;
import de.matthiasmann.twl.renderer.*;

public class Graph extends Widget
{
    private final GraphArea area;
    GraphModel model;
    private ParameterMap themeLineStyles;
    private int sizeMultipleX;
    private int sizeMultipleY;
    LineStyle[] lineStyles;
    private float[] renderXYBuffer;
    private static final float EPSILON = 1.0E-4f;
    
    public Graph() {
        this.sizeMultipleX = 1;
        this.sizeMultipleY = 1;
        this.lineStyles = new LineStyle[8];
        this.renderXYBuffer = new float[128];
        (this.area = new GraphArea()).setClip(true);
        this.add(this.area);
    }
    
    public Graph(final GraphModel model) {
        this();
        this.setModel(model);
    }
    
    public GraphModel getModel() {
        return this.model;
    }
    
    public void setModel(final GraphModel model) {
        this.model = model;
        this.invalidateLineStyles();
    }
    
    public int getSizeMultipleX() {
        return this.sizeMultipleX;
    }
    
    public void setSizeMultipleX(final int sizeMultipleX) {
        if (sizeMultipleX < 1) {
            throw new IllegalArgumentException("sizeMultipleX must be >= 1");
        }
        this.sizeMultipleX = sizeMultipleX;
    }
    
    public int getSizeMultipleY() {
        return this.sizeMultipleY;
    }
    
    public void setSizeMultipleY(final int sizeMultipleY) {
        if (sizeMultipleY < 1) {
            throw new IllegalArgumentException("sizeMultipleX must be >= 1");
        }
        this.sizeMultipleY = sizeMultipleY;
    }
    
    @Override
    protected void applyTheme(final ThemeInfo themeInfo) {
        super.applyTheme(themeInfo);
        this.applyThemeGraph(themeInfo);
    }
    
    protected void applyThemeGraph(final ThemeInfo themeInfo) {
        this.themeLineStyles = themeInfo.getParameterMap("lineStyles");
        this.setSizeMultipleX(themeInfo.getParameter("sizeMultipleX", 1));
        this.setSizeMultipleY(themeInfo.getParameter("sizeMultipleY", 1));
        this.invalidateLineStyles();
    }
    
    protected void invalidateLineStyles() {
        Arrays.fill(this.lineStyles, null);
    }
    
    void syncLineStyles() {
        final int numLines = this.model.getNumLines();
        if (this.lineStyles.length < numLines) {
            final LineStyle[] newLineStyles = new LineStyle[numLines];
            System.arraycopy(this.lineStyles, 0, newLineStyles, 0, this.lineStyles.length);
            this.lineStyles = newLineStyles;
        }
        for (int i = 0; i < numLines; ++i) {
            final GraphLineModel line = this.model.getLine(i);
            LineStyle style = this.lineStyles[i];
            if (style == null) {
                style = new LineStyle();
                this.lineStyles[i] = style;
            }
            final String visualStyle = TextUtil.notNull(line.getVisualStyleName());
            if (!style.name.equals(visualStyle)) {
                ParameterMap lineStyle = null;
                if (this.themeLineStyles != null) {
                    lineStyle = this.themeLineStyles.getParameterMap(visualStyle);
                }
                style.setStyleName(visualStyle, lineStyle);
            }
        }
    }
    
    void renderLine(final LineRenderer lineRenderer, final GraphLineModel line, final float minValue, final float maxValue, final LineStyle style) {
        int numPoints = line.getNumPoints();
        if (numPoints <= 0) {
            return;
        }
        if (this.renderXYBuffer.length < numPoints * 2) {
            this.renderXYBuffer = new float[numPoints * 2];
        }
        final float[] xy = this.renderXYBuffer;
        float delta = maxValue - minValue;
        if (Math.abs(delta) < 1.0E-4f) {
            delta = copySign(1.0E-4f, delta);
        }
        final float yscale = -this.getInnerHeight() / delta;
        final float yoff = (float)this.getInnerBottom();
        final float xscale = this.getInnerWidth() / (float)Math.max(1, numPoints - 1);
        final float xoff = (float)this.getInnerX();
        for (int i = 0; i < numPoints; ++i) {
            final float value = line.getPoint(i);
            xy[i * 2 + 0] = i * xscale + xoff;
            xy[i * 2 + 1] = (value - minValue) * yscale + yoff;
        }
        if (numPoints == 1) {
            xy[2] = xoff + xscale;
            xy[3] = xy[1];
            numPoints = 2;
        }
        lineRenderer.drawLine(xy, numPoints, style.lineWidth, style.color, false);
    }
    
    private static float copySign(final float magnitude, final float sign) {
        final int rawMagnitude = Float.floatToRawIntBits(magnitude);
        final int rawSign = Float.floatToRawIntBits(sign);
        final int rawResult = rawMagnitude | (rawSign & Integer.MIN_VALUE);
        return Float.intBitsToFloat(rawResult);
    }
    
    @Override
    public boolean setSize(final int width, final int height) {
        return super.setSize(round(width, this.sizeMultipleX), round(height, this.sizeMultipleY));
    }
    
    private static int round(final int value, final int grid) {
        return value - value % grid;
    }
    
    @Override
    protected void layout() {
        this.layoutChildFullInnerArea(this.area);
    }
    
    static class LineStyle
    {
        String name;
        Color color;
        float lineWidth;
        
        LineStyle() {
            this.name = "";
            this.color = Color.WHITE;
            this.lineWidth = 1.0f;
        }
        
        void setStyleName(final String name, final ParameterMap lineStyle) {
            this.name = name;
            if (lineStyle != null) {
                this.color = lineStyle.getParameter("color", Color.WHITE);
                this.lineWidth = Math.max(1.0E-4f, lineStyle.getParameter("width", 1.0f));
            }
        }
    }
    
    class GraphArea extends Widget
    {
        @Override
        protected void paintWidget(final GUI gui) {
            if (Graph.this.model != null) {
                Graph.this.syncLineStyles();
                final LineRenderer lineRenderer = gui.getRenderer().getLineRenderer();
                final int numLines = Graph.this.model.getNumLines();
                final boolean independantScale = Graph.this.model.getScaleLinesIndependant();
                float minValue = Float.MAX_VALUE;
                float maxValue = -3.4028235E38f;
                if (independantScale) {
                    for (int i = 0; i < numLines; ++i) {
                        final GraphLineModel line = Graph.this.model.getLine(i);
                        minValue = Math.min(minValue, line.getMinValue());
                        maxValue = Math.max(maxValue, line.getMaxValue());
                    }
                }
                for (int i = 0; i < numLines; ++i) {
                    final GraphLineModel line = Graph.this.model.getLine(i);
                    final LineStyle style = Graph.this.lineStyles[i];
                    if (independantScale) {
                        Graph.this.renderLine(lineRenderer, line, minValue, maxValue, style);
                    }
                    else {
                        Graph.this.renderLine(lineRenderer, line, line.getMinValue(), line.getMaxValue(), style);
                    }
                }
            }
        }
    }
}
