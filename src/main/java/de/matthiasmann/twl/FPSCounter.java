package de.matthiasmann.twl;

public class FPSCounter extends Label
{
    private long startTime;
    private int frames;
    private int framesToCount;
    private final StringBuilder fmtBuffer;
    private final int decimalPoint;
    private final long scale;
    
    public FPSCounter(final int numIntegerDigits, final int numDecimalDigits) {
        this.framesToCount = 100;
        if (numIntegerDigits < 2) {
            throw new IllegalArgumentException("numIntegerDigits must be >= 2");
        }
        if (numDecimalDigits < 0) {
            throw new IllegalArgumentException("numDecimalDigits must be >= 0");
        }
        this.decimalPoint = numIntegerDigits + 1;
        this.startTime = System.nanoTime();
        (this.fmtBuffer = new StringBuilder()).setLength(numIntegerDigits + numDecimalDigits + Integer.signum(numDecimalDigits));
        long tmp = 1000000000L;
        for (int i = 0; i < numDecimalDigits; ++i) {
            tmp *= 10L;
        }
        this.scale = tmp;
        this.updateText(0);
    }
    
    public FPSCounter() {
        this(3, 2);
    }
    
    public int getFramesToCount() {
        return this.framesToCount;
    }
    
    public void setFramesToCount(final int framesToCount) {
        if (framesToCount <= 0) {
            throw new IllegalArgumentException("framesToCount < 1");
        }
        this.framesToCount = framesToCount;
    }
    
    @Override
    protected void paintWidget(final GUI gui) {
        if (++this.frames >= this.framesToCount) {
            this.updateFPS();
        }
        super.paintWidget(gui);
    }
    
    private void updateFPS() {
        final long curTime = System.nanoTime();
        final long elapsed = curTime - this.startTime;
        this.startTime = curTime;
        this.updateText((int)((this.frames * this.scale + (elapsed >> 1)) / elapsed));
        this.frames = 0;
    }
    
    private void updateText(int value) {
        final StringBuilder buf = this.fmtBuffer;
        int pos = buf.length();
        do {
            buf.setCharAt(--pos, (char)(48 + value % 10));
            value /= 10;
            if (this.decimalPoint == pos) {
                buf.setCharAt(--pos, '.');
            }
        } while (pos > 0);
        if (value > 0) {
            pos = buf.length();
            do {
                buf.setCharAt(--pos, '9');
                if (this.decimalPoint == pos) {
                    --pos;
                }
            } while (pos > 0);
        }
        this.setCharSequence(buf);
    }
}
