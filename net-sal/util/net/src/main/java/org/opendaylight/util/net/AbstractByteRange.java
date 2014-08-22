/*
 * (c) Copyright 2010-2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.opendaylight.util.ByteArrayGenerator;
import org.opendaylight.util.cache.CacheableDataType;

import java.math.BigInteger;
import java.util.Iterator;

import static org.opendaylight.util.StringUtils.EOL;

/**
 * Abstract base class for IpRange and MacRange.
 *
 * @param <T> the type of objects of which this is a range
 *
 * @author Simon Hunt
 */
abstract class AbstractByteRange<T> extends CacheableDataType {

    private static final long serialVersionUID = -4036666120354394045L;

    protected static final BigInteger MAX_LONG =
            BigInteger.valueOf(Long.MAX_VALUE);
    protected static final BigInteger MAX_INT =
            BigInteger.valueOf(Integer.MAX_VALUE);

    protected static final String DOT = ".";
    protected static final String COMMA = ",";
    protected static final String COLON_STAR = ":*";

    protected final String spec;
    protected final String normalizedSpec;
    protected final transient ByteArrayGenerator bag;

    /** Constructor that creates the internal byte array generator from
     * the range specification.
     *
     * @param spec the range specification
     */
    protected AbstractByteRange(String spec) {
        this.spec = spec;
        this.bag = getBag(spec);
        this.normalizedSpec = bag.getNormalizedSpec();
    }

    /** Subclasses must return a byte array generator appropriate for the
     * given range specification.
     *
     * @param spec the range specification
     * @return the byte array generator
     * @throws IllegalArgumentException if spec is malformed
     */
    protected abstract ByteArrayGenerator getBag(String spec);

    @Override
    public String toString() {
        return spec;
    }

    /** Returns a multi-line string representing the internal state of
     * this range.
     *
     * @return the debug string
     */
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString()).append(EOL);
        sb.append(" N-Spec  : ").append(normalizedSpec).append(EOL);
        sb.append(" Class   : ").append(getClass().getName()).append(EOL);
        sb.append(" First   : ").append(first()).append(EOL);
        sb.append(" Last    : ").append(last()).append(EOL);
        sb.append(" Size    : ").append(size());
        if (sizeAsLong() == -1)
            sb.append(" (LONG and INT overflow)");
        else if (sizeAsInt() == -1)
            sb.append(" (INT overflow)");
        sb.append(EOL);
        return sb.toString();
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final AbstractByteRange<T> that = (AbstractByteRange<T>) o;
        return bag.equals(that.bag);
    }

    @Override
    public int hashCode() {
        return bag.hashCode();
    }

    /** Returns {@code true} if <em>this</em> range is a superset of
     * the <em>other</em> range; false otherwise.
     * Put another way, the set of all items {@code T_other} produced
     * by {@code other.iterator()} is a subset of the set of all
     * items {@code T_this} produced by {@code this.iterator()}.
     *
     * @param other the other range to compare with
     * @return true if this range is a superset of the other range;
     *          false otherwise
     * @throws IllegalArgumentException if other range is not the same type
     *          as this one
     * @throws NullPointerException if other is null
     */
    public boolean contains(AbstractByteRange<T> other) {
        return this.bag.isSuperset(other.bag);
    }

    /** Returns {@code true} if <em>this</em> range intersects
     * the <em>other</em> range; false otherwise.
     * Put another way, the set of all items {@code T_other} produced
     * by {@code other.iterator()} contains at least one item from the set
     * of all items {@code T_this} produced by {@code this.iterator()}.
     *
     * @param other the other range to compare with
     * @return true if this range intersects the other range; false otherwise
     * @throws IllegalArgumentException if other range is not the same type
     *          as this one
     * @throws NullPointerException if other is null
     */
    public boolean intersects(AbstractByteRange<T> other) {
        return this.bag.intersects(other.bag);
    }

    /** A predicate that returns {@code true} if this range contains
     * the given item; false otherwise.
     *
     * @param t the item
     * @return true if this range contains the item; false otherwise
     */
    public abstract boolean contains(T t);

    /** Returns the first item in the range.
     *
     * @return the first item in the range
     */
    public abstract T first();

    /** Returns the last item in the range.
     *
     * @return the last item in the range
     */
    public abstract T last();

    /** Returns the size of the range. In other words, how many items are
     * represented by the range.
     *
     * @return the size of the range
     * @see #sizeAsLong
     * @see #sizeAsInt
     */
    public BigInteger size() {
        return bag.resultSpaceSize();
    }

    /** Returns the size of the range as a long value, unless the value
     * would be greater than {@link Long#MAX_VALUE}, in which case this
     * method returns -1L.
     *
     * @return the size of the range if it does not overflow
     *          a long; -1L otherwise
     * @see #size
     */
    public long sizeAsLong() {
        return bigIntegerAsLong(size());
    }

    /** Returns the size of the range as an integer value, unless the value
     * would be greater than {@link Integer#MAX_VALUE}, in which case this
     * method returns -1.
     *
     * @return the size of the range if it does not overflow
     *          an int; -1 otherwise
     * @see #size
     */
    public int sizeAsInt() {
        return bigIntegerAsInt(size());
    }

    /** Returns the long value (or -1 if overflow).
     *
     * @param b the big integer
     * @return the long value
     */
    static long bigIntegerAsLong(BigInteger b) {
        return (b.compareTo(MAX_LONG) > 0) ? -1L : b.longValue();
    }

    /** Returns the int value (or -1 if overflow).
     *
     * @param b the big integer
     * @return the int value
     */
    static int bigIntegerAsInt(BigInteger b) {
        return (b.compareTo(MAX_INT) > 0) ? -1 : b.intValue();
    }



    /** Returns a randomly generated item that falls within this range.
     *
     * @return a random item within this range
     */
    public abstract T random();

    /** Returns an iterator for the range. The iterator will return
     * successive items starting with the {@link #first} and ending
     * with the {@link #last}, emitting a total count of {@link #size} items.
     *
     * @return an iterator for the range
     */
    public abstract Iterator<T> iterator();


    //=== the iterator

    /** A partial implementation of a range iterator.
     *
     * @param <V> the type of item that this iterator spits out
     */
    protected abstract class ByteRangeIterator<V> implements Iterator<V> {

        protected final Iterator<byte[]> it;

        /** Constructs a range iterator, based on the internal byte
         * array generator.
         */
        protected ByteRangeIterator() {
            it = AbstractByteRange.this.bag.iterator();
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public abstract V next();

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
