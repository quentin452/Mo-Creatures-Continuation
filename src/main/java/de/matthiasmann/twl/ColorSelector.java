package de.matthiasmann.twl;

import java.nio.*;
import de.matthiasmann.twl.utils.*;
import de.matthiasmann.twl.model.*;
import de.matthiasmann.twl.renderer.*;

public class ColorSelector extends DialogLayout
{
    private static final String[] RGBA_NAMES;
    private static final String[] RGBA_PREFIX;
    final ByteBuffer imgData;
    final IntBuffer imgDataInt;
    ColorSpace colorSpace;
    float[] colorValues;
    ColorValueModel[] colorValueModels;
    private boolean useColorArea2D;
    private boolean showPreview;
    private boolean useLabels;
    private boolean showHexEditField;
    private boolean showNativeAdjuster;
    private boolean showRGBAdjuster;
    private boolean showAlphaAdjuster;
    private Runnable[] callbacks;
    private ColorModel model;
    private Runnable modelCallback;
    private boolean inModelSetValue;
    int currentColor;
    private ARGBModel[] argbModels;
    EditField hexColorEditField;
    private TintAnimator previewTintAnimator;
    private boolean recreateLayout;
    private static final int IMAGE_SIZE = 64;
    
    public ColorSelector(final ColorSpace colorSpace) {
        this.useColorArea2D = true;
        this.showPreview = false;
        this.useLabels = true;
        this.showHexEditField = false;
        this.showNativeAdjuster = true;
        this.showRGBAdjuster = true;
        this.showAlphaAdjuster = true;
        (this.imgData = ByteBuffer.allocateDirect(16384)).order(ByteOrder.BIG_ENDIAN);
        this.imgDataInt = this.imgData.asIntBuffer();
        this.currentColor = Color.WHITE.toARGB();
        this.setColorSpace(colorSpace);
    }
    
    public ColorSpace getColorSpace() {
        return this.colorSpace;
    }
    
    public void setColorSpace(final ColorSpace colorModel) {
        if (colorModel == null) {
            throw new NullPointerException("colorModel");
        }
        if (this.colorSpace != colorModel) {
            final boolean hasColor = this.colorSpace != null;
            this.colorSpace = colorModel;
            this.colorValues = new float[colorModel.getNumComponents()];
            if (hasColor) {
                this.setColor(this.currentColor);
            }
            else {
                this.setDefaultColor();
            }
            this.recreateLayout = true;
            this.invalidateLayout();
        }
    }
    
    public ColorModel getModel() {
        return this.model;
    }
    
    public void setModel(final ColorModel model) {
        if (this.model != model) {
            this.removeModelCallback();
            if ((this.model = model) != null) {
                this.addModelCallback();
                this.modelValueChanged();
            }
        }
    }
    
    public Color getColor() {
        return new Color(this.currentColor);
    }
    
    public void setColor(final Color color) {
        this.setColor(color.toARGB());
        this.updateModel();
    }
    
    public void setDefaultColor() {
        this.currentColor = Color.WHITE.toARGB();
        for (int i = 0; i < this.colorSpace.getNumComponents(); ++i) {
            this.colorValues[i] = this.colorSpace.getDefaultValue(i);
        }
        this.updateAllColorAreas();
        this.colorChanged();
    }
    
    public boolean isUseColorArea2D() {
        return this.useColorArea2D;
    }
    
    public void setUseColorArea2D(final boolean useColorArea2D) {
        if (this.useColorArea2D != useColorArea2D) {
            this.useColorArea2D = useColorArea2D;
            this.recreateLayout = true;
            this.invalidateLayout();
        }
    }
    
    public boolean isShowPreview() {
        return this.showPreview;
    }
    
    public void setShowPreview(final boolean showPreview) {
        if (this.showPreview != showPreview) {
            this.showPreview = showPreview;
            this.recreateLayout = true;
            this.invalidateLayout();
        }
    }
    
    public boolean isShowHexEditField() {
        return this.showHexEditField;
    }
    
    public void setShowHexEditField(final boolean showHexEditField) {
        if (this.showHexEditField != showHexEditField) {
            this.showHexEditField = showHexEditField;
            this.recreateLayout = true;
            this.invalidateLayout();
        }
    }
    
    public boolean isShowAlphaAdjuster() {
        return this.showAlphaAdjuster;
    }
    
    public void setShowAlphaAdjuster(final boolean showAlphaAdjuster) {
        if (this.showAlphaAdjuster != showAlphaAdjuster) {
            this.showAlphaAdjuster = showAlphaAdjuster;
            this.recreateLayout = true;
            this.invalidateLayout();
        }
    }
    
    public boolean isShowNativeAdjuster() {
        return this.showNativeAdjuster;
    }
    
    public void setShowNativeAdjuster(final boolean showNativeAdjuster) {
        if (this.showNativeAdjuster != showNativeAdjuster) {
            this.showNativeAdjuster = showNativeAdjuster;
            this.recreateLayout = true;
            this.invalidateLayout();
        }
    }
    
    public boolean isShowRGBAdjuster() {
        return this.showRGBAdjuster;
    }
    
    public void setShowRGBAdjuster(final boolean showRGBAdjuster) {
        if (this.showRGBAdjuster != showRGBAdjuster) {
            this.showRGBAdjuster = showRGBAdjuster;
            this.recreateLayout = true;
            this.invalidateLayout();
        }
    }
    
    public boolean isUseLabels() {
        return this.useLabels;
    }
    
    public void setUseLabels(final boolean useLabels) {
        if (this.useLabels != useLabels) {
            this.useLabels = useLabels;
            this.recreateLayout = true;
            this.invalidateLayout();
        }
    }
    
    public void addCallback(final Runnable cb) {
        this.callbacks = CallbackSupport.addCallbackToList(this.callbacks, cb, Runnable.class);
    }
    
    public void removeCallback(final Runnable cb) {
        this.callbacks = CallbackSupport.removeCallbackFromList(this.callbacks, cb);
    }
    
    protected void updateModel() {
        if (this.model != null) {
            this.inModelSetValue = true;
            try {
                this.model.setValue(this.getColor());
            }
            finally {
                this.inModelSetValue = false;
            }
        }
    }
    
    protected void colorChanged() {
        this.currentColor = ((this.currentColor & 0xFF000000) | this.colorSpace.toRGB(this.colorValues));
        CallbackSupport.fireCallbacks(this.callbacks);
        this.updateModel();
        if (this.argbModels != null) {
            for (final ARGBModel m : this.argbModels) {
                m.fireCallback();
            }
        }
        if (this.previewTintAnimator != null) {
            this.previewTintAnimator.setColor(this.getColor());
        }
        this.updateHexEditField();
    }
    
    protected void setColor(final int argb) {
        this.currentColor = argb;
        this.colorValues = this.colorSpace.fromRGB(argb & 0xFFFFFF);
        this.updateAllColorAreas();
    }
    
    protected int getNumComponents() {
        return this.colorSpace.getNumComponents();
    }
    
    public void layout() {
        if (this.recreateLayout) {
            this.createColorAreas();
        }
        super.layout();
    }
    
    @Override
    public int getMinWidth() {
        if (this.recreateLayout) {
            this.createColorAreas();
        }
        return super.getMinWidth();
    }
    
    @Override
    public int getMinHeight() {
        if (this.recreateLayout) {
            this.createColorAreas();
        }
        return super.getMinHeight();
    }
    
    @Override
    public int getPreferredInnerWidth() {
        if (this.recreateLayout) {
            this.createColorAreas();
        }
        return super.getPreferredInnerWidth();
    }
    
    @Override
    public int getPreferredInnerHeight() {
        if (this.recreateLayout) {
            this.createColorAreas();
        }
        return super.getPreferredInnerHeight();
    }
    
    protected void createColorAreas() {
        this.recreateLayout = false;
        this.setVerticalGroup(null);
        this.removeAllChildren();
        (this.argbModels = new ARGBModel[4])[0] = new ARGBModel(16);
        this.argbModels[1] = new ARGBModel(8);
        this.argbModels[2] = new ARGBModel(0);
        this.argbModels[3] = new ARGBModel(24);
        final int numComponents = this.getNumComponents();
        final Group horzAreas = this.createSequentialGroup().addGap();
        final Group vertAreas = this.createParallelGroup();
        Group horzLabels = null;
        final Group horzAdjuster = this.createParallelGroup();
        final Group horzControlls = this.createSequentialGroup();
        if (this.useLabels) {
            horzLabels = this.createParallelGroup();
            horzControlls.addGroup(horzLabels);
        }
        horzControlls.addGroup(horzAdjuster);
        final Group[] vertAdjuster = new Group[4 + numComponents];
        int numAdjuters = 0;
        for (int i = 0; i < vertAdjuster.length; ++i) {
            vertAdjuster[i] = this.createParallelGroup();
        }
        this.colorValueModels = new ColorValueModel[numComponents];
        for (int component = 0; component < numComponents; ++component) {
            this.colorValueModels[component] = new ColorValueModel(component);
            if (this.showNativeAdjuster) {
                final ValueAdjusterFloat vaf = new ValueAdjusterFloat(this.colorValueModels[component]);
                if (this.useLabels) {
                    final Label label = new Label(this.colorSpace.getComponentName(component));
                    label.setLabelFor(vaf);
                    horzLabels.addWidget(label);
                    vertAdjuster[numAdjuters].addWidget(label);
                }
                else {
                    vaf.setDisplayPrefix(this.colorSpace.getComponentShortName(component).concat(": "));
                    vaf.setTooltipContent(this.colorSpace.getComponentName(component));
                }
                horzAdjuster.addWidget(vaf);
                vertAdjuster[numAdjuters].addWidget(vaf);
                ++numAdjuters;
            }
        }
        for (int i = 0; i < this.argbModels.length; ++i) {
            if ((i == 3 && this.showAlphaAdjuster) || (i < 3 && this.showRGBAdjuster)) {
                final ValueAdjusterInt vai = new ValueAdjusterInt(this.argbModels[i]);
                if (this.useLabels) {
                    final Label label = new Label(ColorSelector.RGBA_NAMES[i]);
                    label.setLabelFor(vai);
                    horzLabels.addWidget(label);
                    vertAdjuster[numAdjuters].addWidget(label);
                }
                else {
                    vai.setDisplayPrefix(ColorSelector.RGBA_PREFIX[i]);
                    vai.setTooltipContent(ColorSelector.RGBA_NAMES[i]);
                }
                horzAdjuster.addWidget(vai);
                vertAdjuster[numAdjuters].addWidget(vai);
                ++numAdjuters;
            }
        }
        int component = 0;
        if (this.useColorArea2D) {
            while (component + 1 < numComponents) {
                final ColorArea2D area = new ColorArea2D(component, component + 1);
                area.setTooltipContent(this.colorSpace.getComponentName(component) + " / " + this.colorSpace.getComponentName(component + 1));
                horzAreas.addWidget(area);
                vertAreas.addWidget(area);
                component += 2;
            }
        }
        while (component < numComponents) {
            final ColorArea1D area2 = new ColorArea1D(component);
            area2.setTooltipContent(this.colorSpace.getComponentName(component));
            horzAreas.addWidget(area2);
            vertAreas.addWidget(area2);
            ++component;
        }
        if (this.showHexEditField && this.hexColorEditField == null) {
            this.createHexColorEditField();
        }
        if (this.showPreview) {
            if (this.previewTintAnimator == null) {
                this.previewTintAnimator = new TintAnimator(this, this.getColor());
            }
            final Widget previewArea = new Widget();
            previewArea.setTheme("colorarea");
            previewArea.setTintAnimator(this.previewTintAnimator);
            final Widget preview = new Container();
            preview.setTheme("preview");
            preview.add(previewArea);
            final Label label2 = new Label();
            label2.setTheme("previewLabel");
            label2.setLabelFor(preview);
            final Group horz = this.createParallelGroup();
            final Group vert = this.createSequentialGroup();
            horzAreas.addGroup(horz.addWidget(label2).addWidget(preview));
            vertAreas.addGroup(vert.addGap().addWidget(label2).addWidget(preview));
            if (this.showHexEditField) {
                horz.addWidget(this.hexColorEditField);
                vert.addGap().addWidget(this.hexColorEditField);
            }
        }
        final Group horzMainGroup = this.createParallelGroup().addGroup(horzAreas.addGap()).addGroup(horzControlls);
        final Group vertMainGroup = this.createSequentialGroup().addGroup(vertAreas);
        for (int j = 0; j < numAdjuters; ++j) {
            vertMainGroup.addGroup(vertAdjuster[j]);
        }
        if (this.showHexEditField) {
            if (this.hexColorEditField == null) {
                this.createHexColorEditField();
            }
            if (!this.showPreview) {
                horzMainGroup.addWidget(this.hexColorEditField);
                vertMainGroup.addWidget(this.hexColorEditField);
            }
            this.updateHexEditField();
        }
        this.setHorizontalGroup(horzMainGroup);
        this.setVerticalGroup(vertMainGroup.addGap());
    }
    
    protected void updateAllColorAreas() {
        if (this.colorValueModels != null) {
            for (final ColorValueModel cvm : this.colorValueModels) {
                cvm.fireCallback();
            }
            this.colorChanged();
        }
    }
    
    @Override
    protected void afterAddToGUI(final GUI gui) {
        super.afterAddToGUI(gui);
        this.addModelCallback();
    }
    
    @Override
    protected void beforeRemoveFromGUI(final GUI gui) {
        this.removeModelCallback();
        super.beforeRemoveFromGUI(gui);
    }
    
    private void removeModelCallback() {
        if (this.model != null) {
            this.model.removeCallback(this.modelCallback);
        }
    }
    
    private void addModelCallback() {
        if (this.model != null && this.getGUI() != null) {
            if (this.modelCallback == null) {
                this.modelCallback = new Runnable() {
                    @Override
                    public void run() {
                        ColorSelector.this.modelValueChanged();
                    }
                };
            }
            this.model.addCallback(this.modelCallback);
        }
    }
    
    private void createHexColorEditField() {
        (this.hexColorEditField = new EditField() {
            @Override
            protected void insertChar(final char ch) {
                if (this.isValid(ch)) {
                    super.insertChar(ch);
                }
            }
            
            @Override
            public void insertText(String str) {
                for (int i = 0, n = str.length(); i < n; ++i) {
                    if (!this.isValid(str.charAt(i))) {
                        final StringBuilder sb = new StringBuilder(str);
                        int j = n;
                        while (j-- >= i) {
                            if (!this.isValid(sb.charAt(j))) {
                                sb.deleteCharAt(j);
                            }
                        }
                        str = sb.toString();
                        break;
                    }
                }
                super.insertText(str);
            }
            
            private boolean isValid(final char ch) {
                final int digit = Character.digit(ch, 16);
                return digit >= 0 && digit < 16;
            }
        }).setTheme("hexColorEditField");
        this.hexColorEditField.setColumns(8);
        this.hexColorEditField.addCallback(new EditField.Callback() {
            @Override
            public void callback(final int key) {
                if (key == 1) {
                    ColorSelector.this.updateHexEditField();
                    return;
                }
                Color color = null;
                try {
                    color = Color.parserColor("#".concat(ColorSelector.this.hexColorEditField.getText()));
                    ColorSelector.this.hexColorEditField.setErrorMessage(null);
                }
                catch (Exception ex) {
                    ColorSelector.this.hexColorEditField.setErrorMessage("Invalid color format");
                }
                if (key == 28 && color != null) {
                    ColorSelector.this.setColor(color);
                }
            }
        });
    }
    
    void updateHexEditField() {
        if (this.hexColorEditField != null) {
            this.hexColorEditField.setText(String.format("%08X", this.currentColor));
        }
    }
    
    void modelValueChanged() {
        if (!this.inModelSetValue && this.model != null) {
            this.setColor(this.model.getValue().toARGB());
        }
    }
    
    static {
        RGBA_NAMES = new String[] { "Red", "Green", "Blue", "Alpha" };
        RGBA_PREFIX = new String[] { "R: ", "G: ", "B: ", "A: " };
    }
    
    class ColorValueModel extends AbstractFloatModel
    {
        private final int component;
        
        ColorValueModel(final int component) {
            this.component = component;
        }
        
        @Override
        public float getMaxValue() {
            return ColorSelector.this.colorSpace.getMaxValue(this.component);
        }
        
        @Override
        public float getMinValue() {
            return ColorSelector.this.colorSpace.getMinValue(this.component);
        }
        
        @Override
        public float getValue() {
            return ColorSelector.this.colorValues[this.component];
        }
        
        @Override
        public void setValue(final float value) {
            ColorSelector.this.colorValues[this.component] = value;
            this.doCallback();
            ColorSelector.this.colorChanged();
        }
        
        void fireCallback() {
            this.doCallback();
        }
    }
    
    class ARGBModel extends AbstractIntegerModel
    {
        private final int startBit;
        
        ARGBModel(final int startBit) {
            this.startBit = startBit;
        }
        
        @Override
        public int getMaxValue() {
            return 255;
        }
        
        @Override
        public int getMinValue() {
            return 0;
        }
        
        @Override
        public int getValue() {
            return ColorSelector.this.currentColor >> this.startBit & 0xFF;
        }
        
        @Override
        public void setValue(final int value) {
            ColorSelector.this.setColor((ColorSelector.this.currentColor & ~(255 << this.startBit)) | value << this.startBit);
        }
        
        void fireCallback() {
            this.doCallback();
        }
    }
    
    abstract class ColorArea extends Widget implements Runnable
    {
        DynamicImage img;
        Image cursorImage;
        boolean needsUpdate;
        
        @Override
        protected void applyTheme(final ThemeInfo themeInfo) {
            super.applyTheme(themeInfo);
            this.cursorImage = themeInfo.getImage("cursor");
        }
        
        abstract void createImage(final GUI p0);
        
        abstract void updateImage();
        
        abstract void handleMouse(final int p0, final int p1);
        
        @Override
        protected void paintWidget(final GUI gui) {
            if (this.img == null) {
                this.createImage(gui);
                this.needsUpdate = true;
            }
            if (this.img != null) {
                if (this.needsUpdate) {
                    this.updateImage();
                }
                this.img.draw((AnimationState)this.getAnimationState(), this.getInnerX(), this.getInnerY(), this.getInnerWidth(), this.getInnerHeight());
            }
        }
        
        @Override
        public void destroy() {
            super.destroy();
            if (this.img != null) {
                this.img.destroy();
                this.img = null;
            }
        }
        
        @Override
        protected boolean handleEvent(final Event evt) {
            switch (evt.getType()) {
                case MOUSE_BTNDOWN:
                case MOUSE_DRAGGED: {
                    this.handleMouse(evt.getMouseX() - this.getInnerX(), evt.getMouseY() - this.getInnerY());
                    return true;
                }
                case MOUSE_WHEEL: {
                    return false;
                }
                default: {
                    return evt.isMouseEvent() || super.handleEvent(evt);
                }
            }
        }
        
        @Override
        public void run() {
            this.needsUpdate = true;
        }
    }
    
    class ColorArea1D extends ColorArea
    {
        final int component;
        
        ColorArea1D(final int component) {
            this.component = component;
            for (int i = 0, n = ColorSelector.this.getNumComponents(); i < n; ++i) {
                if (i != component) {
                    ColorSelector.this.colorValueModels[i].addCallback(this);
                }
            }
        }
        
        @Override
        protected void paintWidget(final GUI gui) {
            super.paintWidget(gui);
            if (this.cursorImage != null) {
                final float minValue = ColorSelector.this.colorSpace.getMinValue(this.component);
                final float maxValue = ColorSelector.this.colorSpace.getMaxValue(this.component);
                final int pos = (int)((ColorSelector.this.colorValues[this.component] - maxValue) * (this.getInnerHeight() - 1) / (minValue - maxValue) + 0.5f);
                this.cursorImage.draw((AnimationState)this.getAnimationState(), this.getInnerX(), this.getInnerY() + pos, this.getInnerWidth(), 1);
            }
        }
        
        protected void createImage(final GUI gui) {
            this.img = gui.getRenderer().createDynamicImage(1, 64);
        }
        
        protected void updateImage() {
            final float[] temp = ColorSelector.this.colorValues.clone();
            final IntBuffer buf = ColorSelector.this.imgDataInt;
            final ColorSpace cs = ColorSelector.this.colorSpace;
            float x = cs.getMaxValue(this.component);
            final float dx = (cs.getMinValue(this.component) - x) / 63.0f;
            for (int i = 0; i < 64; ++i) {
                temp[this.component] = x;
                buf.put(i, cs.toRGB(temp) << 8 | 0xFF);
                x += dx;
            }
            this.img.update(ColorSelector.this.imgData, DynamicImage.Format.RGBA);
            this.needsUpdate = false;
        }
        
        @Override
        void handleMouse(final int x, final int y) {
            final float minValue = ColorSelector.this.colorSpace.getMinValue(this.component);
            final float maxValue = ColorSelector.this.colorSpace.getMaxValue(this.component);
            final int innerHeight = this.getInnerHeight();
            final int pos = Math.max(0, Math.min(innerHeight, y));
            final float value = maxValue + (minValue - maxValue) * pos / innerHeight;
            ColorSelector.this.colorValueModels[this.component].setValue(value);
        }
    }
    
    class ColorArea2D extends ColorArea
    {
        private final int componentX;
        private final int componentY;
        
        ColorArea2D(final int componentX, final int componentY) {
            this.componentX = componentX;
            this.componentY = componentY;
            for (int i = 0, n = ColorSelector.this.getNumComponents(); i < n; ++i) {
                if (i != componentX && i != componentY) {
                    ColorSelector.this.colorValueModels[i].addCallback(this);
                }
            }
        }
        
        @Override
        protected void paintWidget(final GUI gui) {
            super.paintWidget(gui);
            if (this.cursorImage != null) {
                final float minValueX = ColorSelector.this.colorSpace.getMinValue(this.componentX);
                final float maxValueX = ColorSelector.this.colorSpace.getMaxValue(this.componentX);
                final float minValueY = ColorSelector.this.colorSpace.getMinValue(this.componentY);
                final float maxValueY = ColorSelector.this.colorSpace.getMaxValue(this.componentY);
                final int posX = (int)((ColorSelector.this.colorValues[this.componentX] - maxValueX) * (this.getInnerWidth() - 1) / (minValueX - maxValueX) + 0.5f);
                final int posY = (int)((ColorSelector.this.colorValues[this.componentY] - maxValueY) * (this.getInnerHeight() - 1) / (minValueY - maxValueY) + 0.5f);
                this.cursorImage.draw((AnimationState)this.getAnimationState(), this.getInnerX() + posX, this.getInnerY() + posY, 1, 1);
            }
        }
        
        protected void createImage(final GUI gui) {
            this.img = gui.getRenderer().createDynamicImage(64, 64);
        }
        
        protected void updateImage() {
            final float[] temp = ColorSelector.this.colorValues.clone();
            final IntBuffer buf = ColorSelector.this.imgDataInt;
            final ColorSpace cs = ColorSelector.this.colorSpace;
            final float x0 = cs.getMaxValue(this.componentX);
            final float dx = (cs.getMinValue(this.componentX) - x0) / 63.0f;
            float y = cs.getMaxValue(this.componentY);
            final float dy = (cs.getMinValue(this.componentY) - y) / 63.0f;
            int i = 0;
            int idx = 0;
            while (i < 64) {
                temp[this.componentY] = y;
                float x2 = x0;
                for (int j = 0; j < 64; ++j) {
                    temp[this.componentX] = x2;
                    buf.put(idx++, cs.toRGB(temp) << 8 | 0xFF);
                    x2 += dx;
                }
                y += dy;
                ++i;
            }
            this.img.update(ColorSelector.this.imgData, DynamicImage.Format.RGBA);
            this.needsUpdate = false;
        }
        
        @Override
        void handleMouse(final int x, final int y) {
            final float minValueX = ColorSelector.this.colorSpace.getMinValue(this.componentX);
            final float maxValueX = ColorSelector.this.colorSpace.getMaxValue(this.componentX);
            final float minValueY = ColorSelector.this.colorSpace.getMinValue(this.componentY);
            final float maxValueY = ColorSelector.this.colorSpace.getMaxValue(this.componentY);
            final int innerWidtht = this.getInnerWidth();
            final int innerHeight = this.getInnerHeight();
            final int posX = Math.max(0, Math.min(innerWidtht, x));
            final int posY = Math.max(0, Math.min(innerHeight, y));
            final float valueX = maxValueX + (minValueX - maxValueX) * posX / innerWidtht;
            final float valueY = maxValueY + (minValueY - maxValueY) * posY / innerHeight;
            ColorSelector.this.colorValueModels[this.componentX].setValue(valueX);
            ColorSelector.this.colorValueModels[this.componentY].setValue(valueY);
        }
    }
}
