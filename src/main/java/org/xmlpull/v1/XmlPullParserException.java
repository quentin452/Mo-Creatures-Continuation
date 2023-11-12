package org.xmlpull.v1;

public class XmlPullParserException extends Exception
{
    protected Throwable detail;
    protected int row;
    protected int column;
    
    public XmlPullParserException(final String s) {
        super(s);
        this.row = -1;
        this.column = -1;
    }
    
    public XmlPullParserException(final String msg, final XmlPullParser parser, final Throwable chain) {
        super(((msg == null) ? "" : (msg + " ")) + ((parser == null) ? "" : ("(position:" + parser.getPositionDescription() + ") ")) + ((chain == null) ? "" : ("caused by: " + chain)));
        this.row = -1;
        this.column = -1;
        if (parser != null) {
            this.row = parser.getLineNumber();
            this.column = parser.getColumnNumber();
        }
        this.detail = chain;
    }
    
    public Throwable getDetail() {
        return this.detail;
    }
    
    public int getLineNumber() {
        return this.row;
    }
    
    public int getColumnNumber() {
        return this.column;
    }
    
    public void printStackTrace() {
        if (this.detail == null) {
            super.printStackTrace();
        }
        else {
            synchronized (System.err) {
                System.err.println(super.getMessage() + "; nested exception is:");
                this.detail.printStackTrace();
            }
        }
    }
}
