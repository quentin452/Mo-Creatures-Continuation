package de.matthiasmann.twl.model;

import java.util.*;

public class SimpleAutoCompletionResult extends AutoCompletionResult
{
    private final String[] results;
    
    public SimpleAutoCompletionResult(final String text, final int prefixLength, final Collection<String> results) {
        super(text, prefixLength);
        this.results = results.toArray(new String[results.size()]);
    }
    
    public SimpleAutoCompletionResult(final String text, final int prefixLength, final String... results) {
        super(text, prefixLength);
        this.results = results.clone();
    }
    
    public int getNumResults() {
        return this.results.length;
    }
    
    public String getResult(final int idx) {
        return this.results[idx];
    }
}
