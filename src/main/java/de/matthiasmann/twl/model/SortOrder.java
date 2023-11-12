package de.matthiasmann.twl.model;

import java.util.*;

public enum SortOrder
{
    ASCENDING {
        @Override
        public <T> Comparator<T> map(final Comparator<T> c) {
            return c;
        }

        @Override
        public SortOrder invert() {
            return SortOrder.DESCENDING;
        }
    },
    DESCENDING {
        @Override
        public <T> Comparator<T> map(final Comparator<T> c) {
            return Collections.reverseOrder(c);
        }

        @Override
        public SortOrder invert() {
            return SortOrder.ASCENDING;
        }
    };

    public abstract <T> Comparator<T> map(final Comparator<T> p0);

    public abstract SortOrder invert();
}
