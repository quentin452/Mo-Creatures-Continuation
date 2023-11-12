package de.matthiasmann.twl.utils;

import java.io.*;

public class MultiStringReader extends Reader
{
    private final String[] strings;
    private String cur;
    private int nr;
    private int pos;
    
    public MultiStringReader(final String... strings) {
        this.strings = strings;
    }
    
    @Override
    public int read(final char[] cbuf, final int off, int len) throws IOException {
        while (this.cur == null || this.pos == this.cur.length()) {
            if (this.nr == this.strings.length) {
                return -1;
            }
            this.cur = this.strings[this.nr++];
            this.pos = 0;
        }
        final int remain = this.cur.length() - this.pos;
        if (len > remain) {
            len = remain;
        }
        this.cur.getChars(this.pos, this.pos += len, cbuf, off);
        return len;
    }
    
    @Override
    public void close() {
    }
}
