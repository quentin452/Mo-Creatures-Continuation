package de.matthiasmann.twl.model;

public interface AutoCompletionDataSource
{
    AutoCompletionResult collectSuggestions(final String p0, final int p1, final AutoCompletionResult p2);
}
