package de.matthiasmann.twl.model;

import de.matthiasmann.twl.renderer.*;
import java.util.*;

public class StringAttributes implements AttributedString
{
    private final CharSequence seq;
    private final AnimationState baseAnimState;
    private final ArrayList<Marker> markers;
    private int position;
    private int markerIdx;
    private static final int NOT_FOUND = Integer.MIN_VALUE;
    private static final int IDX_MASK = Integer.MAX_VALUE;
    
    private StringAttributes(final AnimationState baseAnimState, final CharSequence seq) {
        if (seq == null) {
            throw new NullPointerException("seq");
        }
        this.seq = seq;
        this.baseAnimState = baseAnimState;
        this.markers = new ArrayList<Marker>();
    }
    
    public StringAttributes(final String text, final AnimationState baseAnimState) {
        this(baseAnimState, text);
    }
    
    public StringAttributes(final String text) {
        this(text, null);
    }
    
    public StringAttributes(final ObservableCharSequence cs, final AnimationState baseAnimState) {
        this(baseAnimState, (CharSequence)cs);
        cs.addCallback((ObservableCharSequence.Callback)new ObservableCharSequence.Callback() {
            public void charactersChanged(final int start, final int oldCount, final int newCount) {
                if (start < 0) {
                    throw new IllegalArgumentException("start");
                }
                if (oldCount > 0) {
                    StringAttributes.this.delete(start, oldCount);
                }
                if (newCount > 0) {
                    StringAttributes.this.insert(start, newCount);
                }
            }
        });
    }
    
    public StringAttributes(final ObservableCharSequence cs) {
        this(cs, null);
    }
    
    @Override
    public char charAt(final int index) {
        return this.seq.charAt(index);
    }
    
    @Override
    public int length() {
        return this.seq.length();
    }
    
    @Override
    public CharSequence subSequence(final int start, final int end) {
        return this.seq.subSequence(start, end);
    }
    
    @Override
    public String toString() {
        return this.seq.toString();
    }
    
    @Override
    public int getPosition() {
        return this.position;
    }
    
    @Override
    public void setPosition(final int pos) {
        if (pos < 0 || pos > this.seq.length()) {
            throw new IllegalArgumentException("pos");
        }
        this.position = pos;
        final int idx = this.find(pos);
        if (idx >= 0) {
            this.markerIdx = idx;
        }
        else if (pos > this.lastMarkerPos()) {
            this.markerIdx = this.markers.size();
        }
        else {
            this.markerIdx = (idx & Integer.MAX_VALUE) - 1;
        }
    }
    
    @Override
    public int advance() {
        if (this.markerIdx + 1 < this.markers.size()) {
            ++this.markerIdx;
            this.position = this.markers.get(this.markerIdx).position;
        }
        else {
            this.position = this.seq.length();
        }
        return this.position;
    }
    
    @Override
    public boolean getAnimationState(final AnimationState.StateKey state) {
        if (this.markerIdx >= 0 && this.markerIdx < this.markers.size()) {
            final Marker marker = this.markers.get(this.markerIdx);
            final int bitIdx = state.getID() << 1;
            if (marker.get(bitIdx)) {
                return marker.get(bitIdx + 1);
            }
        }
        return this.baseAnimState != null && this.baseAnimState.getAnimationState(state);
    }
    
    @Override
    public int getAnimationTime(final AnimationState.StateKey state) {
        if (this.baseAnimState != null) {
            return this.baseAnimState.getAnimationTime(state);
        }
        return 0;
    }
    
    @Override
    public boolean getShouldAnimateState(final AnimationState.StateKey state) {
        return this.baseAnimState != null && this.baseAnimState.getShouldAnimateState(state);
    }
    
    public void setAnimationState(final AnimationState.StateKey key, final int from, final int end, final boolean active) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        if (from > end) {
            throw new IllegalArgumentException("negative range");
        }
        if (from < 0 || end > this.seq.length()) {
            throw new IllegalArgumentException("range outside of sequence");
        }
        if (from == end) {
            return;
        }
        final int fromIdx = this.markerIndexAt(from);
        final int endIdx = this.markerIndexAt(end);
        final int bitIdx = key.getID() << 1;
        for (int i = fromIdx; i < endIdx; ++i) {
            final Marker m = this.markers.get(i);
            m.set(bitIdx);
            m.set(bitIdx + 1, active);
        }
    }
    
    public void removeAnimationState(final AnimationState.StateKey key, final int from, final int end) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        if (from > end) {
            throw new IllegalArgumentException("negative range");
        }
        if (from < 0 || end > this.seq.length()) {
            throw new IllegalArgumentException("range outside of sequence");
        }
        if (from == end) {
            return;
        }
        final int fromIdx = this.markerIndexAt(from);
        final int endIdx = this.markerIndexAt(end);
        this.removeRange(fromIdx, endIdx, key);
    }
    
    public void removeAnimationState(final AnimationState.StateKey key) {
        if (key == null) {
            throw new NullPointerException("key");
        }
        this.removeRange(0, this.markers.size(), key);
    }
    
    private void removeRange(final int start, final int end, final AnimationState.StateKey key) {
        final int bitIdx = key.getID() << 1;
        for (int i = start; i < end; ++i) {
            this.markers.get(i).clear(bitIdx);
            this.markers.get(i).clear(bitIdx + 1);
        }
    }
    
    public void clearAnimationStates() {
        this.markers.clear();
    }
    
    public void optimize() {
        if (this.markers.size() > 1) {
            Marker prev = this.markers.get(0);
            int i = 1;
            while (i < this.markers.size()) {
                final Marker cur = this.markers.get(i);
                if (prev.equals(cur)) {
                    this.markers.remove(i);
                }
                else {
                    prev = cur;
                    ++i;
                }
            }
        }
    }
    
    void insert(final int pos, final int count) {
        for (int idx = this.find(pos) & Integer.MAX_VALUE, end = this.markers.size(); idx < end; ++idx) {
            final Marker marker = this.markers.get(idx);
            marker.position += count;
        }
    }
    
    void delete(final int pos, final int count) {
        int removeIdx;
        final int startIdx = removeIdx = (this.find(pos) & Integer.MAX_VALUE);
        for (int end = this.markers.size(), idx = startIdx; idx < end; ++idx) {
            final Marker m = this.markers.get(idx);
            int newPos = m.position - count;
            if (newPos <= pos) {
                newPos = pos;
                removeIdx = idx;
            }
            m.position = newPos;
        }
        int idx = removeIdx;
        while (idx > startIdx) {
            this.markers.remove(--idx);
        }
    }
    
    private int lastMarkerPos() {
        final int numMarkers = this.markers.size();
        if (numMarkers > 0) {
            return this.markers.get(numMarkers - 1).position;
        }
        return 0;
    }
    
    private int markerIndexAt(final int pos) {
        int idx = this.find(pos);
        if (idx < 0) {
            idx &= Integer.MAX_VALUE;
            this.insertMarker(idx, pos);
        }
        return idx;
    }
    
    private void insertMarker(final int idx, final int pos) {
        final Marker newMarker = new Marker();
        if (idx > 0) {
            final Marker leftMarker = this.markers.get(idx - 1);
            assert leftMarker.position < pos;
            newMarker.or(leftMarker);
        }
        newMarker.position = pos;
        this.markers.add(idx, newMarker);
    }
    
    private int find(final int pos) {
        int lo = 0;
        int hi = this.markers.size();
        while (lo < hi) {
            final int mid = lo + hi >>> 1;
            final int markerPos = this.markers.get(mid).position;
            if (pos < markerPos) {
                hi = mid;
            }
            else {
                if (pos <= markerPos) {
                    return mid;
                }
                lo = mid + 1;
            }
        }
        return lo | Integer.MIN_VALUE;
    }
    
    static class Marker extends BitSet
    {
        int position;
    }
}
