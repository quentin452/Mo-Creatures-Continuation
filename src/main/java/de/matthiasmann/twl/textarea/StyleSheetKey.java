package de.matthiasmann.twl.textarea;

public class StyleSheetKey
{
    final String element;
    final String className;
    final String id;
    
    public StyleSheetKey(final String element, final String className, final String id) {
        this.element = element;
        this.className = className;
        this.id = id;
    }
    
    public String getClassName() {
        return this.className;
    }
    
    public String getElement() {
        return this.element;
    }
    
    public String getId() {
        return this.id;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof StyleSheetKey) {
            final StyleSheetKey other = (StyleSheetKey)obj;
            if (this.element == null) {
                if (other.element != null) {
                    return false;
                }
            }
            else if (!this.element.equals(other.element)) {
                return false;
            }
            if (this.className == null) {
                if (other.className != null) {
                    return false;
                }
            }
            else if (!this.className.equals(other.className)) {
                return false;
            }
            if ((this.id != null) ? this.id.equals(other.id) : (other.id == null)) {
                return true;
            }
            return false;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + ((this.element != null) ? this.element.hashCode() : 0);
        hash = 53 * hash + ((this.className != null) ? this.className.hashCode() : 0);
        hash = 53 * hash + ((this.id != null) ? this.id.hashCode() : 0);
        return hash;
    }
    
    public boolean matches(final StyleSheetKey what) {
        return (this.element == null || this.element.equals(what.element)) && (this.className == null || this.className.equals(what.className)) && (this.id == null || this.id.equals(what.id));
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder().append(this.element);
        if (this.className != null) {
            sb.append('.').append(this.className);
        }
        if (this.id != null) {
            sb.append('#').append(this.id);
        }
        return sb.toString();
    }
}
