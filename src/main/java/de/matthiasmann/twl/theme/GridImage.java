package de.matthiasmann.twl.theme;

import de.matthiasmann.twl.renderer.*;
import de.matthiasmann.twl.*;
import de.matthiasmann.twl.renderer.AnimationState;

public class GridImage implements Image, HasBorder
{
    private final Image[] images;
    private final int[] weightX;
    private final int[] weightY;
    private final Border border;
    private final int width;
    private final int height;
    private final int[] columnWidth;
    private final int[] rowHeight;
    private final int weightSumX;
    private final int weightSumY;

    GridImage(final Image[] images, final int[] weightX, final int[] weightY, final Border border) {
        if (weightX.length == 0 || weightY.length == 0) {
            throw new IllegalArgumentException("zero dimension size not allowed");
        }
        assert weightX.length * weightY.length == images.length;
        this.images = images;
        this.weightX = weightX;
        this.weightY = weightY;
        this.border = border;
        this.columnWidth = new int[weightX.length];
        this.rowHeight = new int[weightY.length];
        int widthTmp = 0;
        for (int x = 0; x < weightX.length; ++x) {
            int widthColumn = 0;
            for (int y = 0; y < weightY.length; ++y) {
                widthColumn = Math.max(widthColumn, this.getImage(x, y).getWidth());
            }
            widthTmp += widthColumn;
            this.columnWidth[x] = widthColumn;
        }
        this.width = widthTmp;
        int heightTmp = 0;
        for (int y2 = 0; y2 < weightY.length; ++y2) {
            int heightRow = 0;
            for (int x2 = 0; x2 < weightX.length; ++x2) {
                heightRow = Math.max(heightRow, this.getImage(x2, y2).getHeight());
            }
            heightTmp += heightRow;
            this.rowHeight[y2] = heightRow;
        }
        this.height = heightTmp;
        int tmpSumX = 0;
        for (final int weight : weightX) {
            if (weight < 0) {
                throw new IllegalArgumentException("negative weight in weightX");
            }
            tmpSumX += weight;
        }
        this.weightSumX = tmpSumX;
        int tmpSumY = 0;
        for (final int weight2 : weightY) {
            if (weight2 < 0) {
                throw new IllegalArgumentException("negative weight in weightY");
            }
            tmpSumY += weight2;
        }
        this.weightSumY = tmpSumY;
        if (this.weightSumX <= 0) {
            throw new IllegalArgumentException("zero weightX not allowed");
        }
        if (this.weightSumY <= 0) {
            throw new IllegalArgumentException("zero weightX not allowed");
        }
    }

    private GridImage(final Image[] images, final GridImage src) {
        this.images = images;
        this.weightX = src.weightX;
        this.weightY = src.weightY;
        this.border = src.border;
        this.columnWidth = src.columnWidth;
        this.rowHeight = src.rowHeight;
        this.weightSumX = src.weightSumX;
        this.weightSumY = src.weightSumY;
        this.width = src.width;
        this.height = src.height;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void draw(final AnimationState as, final int x, final int y) {
        this.draw(as, x, y, this.width, this.height);
    }

    public void draw(final AnimationState as, final int x, int y, final int width, final int height) {
        int deltaY = height - this.height;
        int remWeightY = this.weightSumY;
        int yi = 0;
        int idx = 0;
        while (yi < this.weightY.length) {
            int heightRow = this.rowHeight[yi];
            if (remWeightY > 0) {
                final int partY = deltaY * this.weightY[yi] / remWeightY;
                remWeightY -= this.weightY[yi];
                heightRow += partY;
                deltaY -= partY;
            }
            int tmpX = x;
            int deltaX = width - this.width;
            int remWeightX = this.weightSumX;
            for (int xi = 0; xi < this.weightX.length; ++xi, ++idx) {
                int widthColumn = this.columnWidth[xi];
                if (remWeightX > 0) {
                    final int partX = deltaX * this.weightX[xi] / remWeightX;
                    remWeightX -= this.weightX[xi];
                    widthColumn += partX;
                    deltaX -= partX;
                }
                this.images[idx].draw(as, tmpX, y, widthColumn, heightRow);
                tmpX += widthColumn;
            }
            y += heightRow;
            ++yi;
        }
    }

    public Border getBorder() {
        return this.border;
    }

    public Image createTintedVersion(final Color color) {
        final Image[] newImages = new Image[this.images.length];
        for (int i = 0; i < newImages.length; ++i) {
            newImages[i] = this.images[i].createTintedVersion(color);
        }
        return (Image)new GridImage(newImages, this);
    }

    private Image getImage(final int x, final int y) {
        return this.images[x + y * this.weightX.length];
    }
}
