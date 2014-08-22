/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import java.util.Iterator;
import java.util.Random;

import static org.opendaylight.util.StringUtils.EOL;

/**
 * Abstract base class for numeric-based ranges. Ranges are described by a
 * string specification of the form:
 * <pre>
 *     "{first}-{last}"
 * </pre>
 * where {first} and {last} are assumed to be parsable items of
 * type <em>T</em>, and {first} is "smaller" than {last}; i.e.:
 * <pre>
 * first.compareTo(last) <= 0  is true
 * </pre>
 *
 *
 * @param <T> the type of objects of which this is a range
 *
 * @author Simon Hunt
 */
abstract class AbstractRange<T extends Comparable<T>> {

    /** Exception message prefix for bad specification string. */
    protected static final String E_BAD_SPEC = "Bad range spec: ";
    /** Index for first item returned by {@link #splitSpec(String)}. */
    protected static final int FIRST = 0;
    /** Index for last item returned by {@link #splitSpec(String)}. */
    protected static final int LAST = 1;
    /** The hyphen character, "-". */
    protected static final String HYPHEN = "-";
    /** Random number generator. */
    protected static final Random RANDOM = new Random();

    protected final String spec;
    protected T first;
    protected T last;
    protected int size;

    /**
     * Constructs an abstract range.
     *
     * @param spec the range specification
     */
    protected AbstractRange(String spec) {
        this.spec = spec;
        parseSpec(spec);
    }

    @Override
    public String toString() {
        return spec;
    }

    /**
     * Returns a multi-line string representing the internal state of this
     * range.
     *
     * @return the debug string
     */
    public String toDebugString() {
        return toString() + EOL +
                " Class : " + getClass().getName() + EOL +
                " First : " + first() + EOL +
                " Last  : " + last() + EOL +
                " Size  : " + size() + EOL;
    }

    /**
     * Subclasses should parse the specification, setting the fields
     * {@link #first}, {@link #last}, and {@link #size} appropriately.
     * This method is invoked from the constructor.
     *
     * @param spec the range specification
     * @throws IllegalArgumentException if spec is malformed
     */
    protected abstract void parseSpec(String spec);

    /**
     * Convenience method to split the given string using hyphen as the
     * delimiter. Throws an exception if the spec cannot be split into two
     * non-empty strings.
     *
     * @param spec the range specification
     * @return an array containing the first and second strings
     */
    protected String[] splitSpec(String spec) {
        String[] items = spec.trim().split(HYPHEN);
        if (items.length != 2 ||
                items[FIRST].length() <= 0 || items[LAST].length() <= 0)
            throw new IllegalArgumentException(E_BAD_SPEC + spec);
        return items;
    }


    /**
     * Returns the first item in the range.
     *
     * @return the first item in the range
     */
    public T first() { return first; }


    /**
     * Returns the last item in the range.
     *
     * @return the last item in the range
     */
    public T last() { return last; }

    /**
     * Returns the size of the range. In other words, how many items are
     * represented by the range.
     *
     * @return the size of the range
     */
    public long size() { return size; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractRange that = (AbstractRange) o;
        return !(last != null ? !last.equals(that.last) : that.last != null) &&
                !(first != null ? !first.equals(that.first) : that.first != null);
    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (last != null ? last.hashCode() : 0);
        return result;
    }

    /**
     * Returns {@code true} if this range contains the specified item;
     * false otherwise.
     *
     * @param item the item to test
     * @return true if the item is contained by this range
     */
    public boolean contains(T item) {
        int cf = first.compareTo(item);
        int cl = last.compareTo(item);
        return cf <= 0 && cl >= 0;
    }

    /**
     * Returns {@code true} if <em>this</em> range is a superset of the
     * <em>other</em> range; false otherwise.
     * Put another way, the set of all items {@code T_other} produced by
     * {@code other.iterator()} is a subset of the set of all items
     * {@code T_this} produced by {@code this.iterator()}.
     *
     * @param other the other range to compare with
     * @return true if this range is a superset of the other range
     * @throws IllegalArgumentException if other range is not the same type
     *          as this one
     * @throws NullPointerException if other is null
     */
    public boolean contains(AbstractRange<T> other) {
        // Return true of other.first >= this.first && other.last <= this.last
        int cf = first.compareTo(other.first);
        int cl = last.compareTo(other.last);
        return cf <= 0 && cl >= 0;
    }

    public boolean intersects(AbstractRange<T> other) {
        // Return true if either other.first or other.last is contained in
        // this range, or if our first or last is contained in the other range.
        return contains(other.first) || contains(other.last) ||
                other.contains(first) || other.contains(last);
    }

    /**
     * Returns a randomly generated item that falls within this range.
     *
     * @return a random item within this range
     */
    public abstract T random();

    /**
     * Returns an iterator for the range. The iterator will return successive
     * items starting with the {@link #first()} and ending with the
     * {@link #last()}, emitting a total count of {@link #size()} items.
     *
     * @return an iterator for the range
     */
    public abstract Iterator<T> iterator();

    /**
     * A partial implementation of a range iterator.
     *
     * @param <V> the type of item that this iterator generates
     */
    protected static abstract class RangeIterator<V extends Comparable<V>>
            implements Iterator<V> {

        private final V last;
        protected V current;

        /**
         * Constructs a range iterator.
         *
         * @param first the first item of the iteration
         * @param last the last item of the iteration
         */
        protected RangeIterator(V first, V last) {
            current = first;
            this.last = last;
        }

        @Override
        public String toString() {
            return  "class=" + getClass().getSimpleName() +
                    ", last=" + last +
                    ", current=" + current;
        }

        @Override
        public boolean hasNext() {
            return current.compareTo(last) <= 0;
        }

        @Override
        public abstract V next();

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
