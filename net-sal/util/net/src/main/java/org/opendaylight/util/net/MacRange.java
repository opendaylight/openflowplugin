/*
 * (c) Copyright 2010-2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.opendaylight.util.ByteArrayGenerator;
import org.opendaylight.util.Tokenizer;
import org.opendaylight.util.cache.WeakValueCache;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Represents a range of MAC addresses. Instances of this class are created
 * using the {@link #valueOf} factory method.
 * <p>
 * A string specification is used to define the MAC address range, using the
 * form:
 * <pre>
 * {byteSpec}:{byteSpec}:{byteSpec}:{byteSpec}:{byteSpec}:{byteSpec}
 * </pre>
 * where <em>byteSpec</em> represents a single byte in the address, and is of
 * the form:
 * <pre>
 * {nn} | {nn}-{mm} | *
 * </pre>
 * where <em>nn</em> and <em>mm</em> are hex values in the range 00 to FF,
 * <em>mm</em> (when specified) is &gt; <em>nn</em>, and <em>*</em> stands
 * for 00-FF.
 * <p>
 * For example, the specification {@code "FE:00:45:01:00-03:*"} declares that
 * the range of MAC addresses starts with {@code FE:00:45:01:00:00} and ends
 * with {@code FE:00:45:01:03:FF}.
 *
 * @author Simon Hunt
 */
public final class MacRange extends AbstractByteRange<MacAddress>
        implements Comparable<MacRange> {

    private static final long serialVersionUID = -6839227257889118723L;

    /** Constructs a MAC range from the range specification.
     *
     * @param spec the range specification
     */
    private MacRange(String spec) {
        super(spec);
    }

    @Override
    protected ByteArrayGenerator getBag(String spec) {
        ByteArrayGenerator bag = ByteArrayGenerator.createFromHex(spec);
        if (bag.size() != 6)
            throw new IllegalArgumentException("Malformed MAC range spec [" +
                                                spec + "]");
        return bag;
    }

    @Override
    public boolean contains(MacAddress macAddress) {
        return bag.contains(macAddress.toByteArray());
    }

    @Override
    public MacAddress first() {
        return MacAddress.valueOf(bag.lowest());
    }

    @Override
    public MacAddress last() {
        return MacAddress.valueOf(bag.highest());
    }

    @Override
    public MacAddress random() {
        return MacAddress.valueOf(bag.generate());
    }

    @Override
    public Iterator<MacAddress> iterator() {
        return new MacIterator();
    }

    @Override
    public int compareTo(MacRange o) {
        int result = first().compareTo(o.first());
        return result == 0 ? last().compareTo(o.last()) : result;
    }

    // === PRIVATE serialization ==============================================

    //== Implementation note:
    //      We use default serialization to serialize the String spec.

    private Object readResolve() throws ObjectStreamException {
        // when this is called, spec has been populated.
        // create a new instance to make sure the byte array generator
        // is initialized
        Object o;
        try {
            o = valueOf(spec);
        } catch (NullPointerException e) {
            throw new InvalidObjectException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new InvalidObjectException(e.getMessage());
        }
        return o;
    }

    //=== CACHE ===============================================================

    private static final WeakValueCache<String, MacRange> cachedRanges =
            new WeakValueCache<String, MacRange>(getRefQ());

    /** Ensures that all equivalent MAC range encoding keys
     * map to the same instance of MacRange.
     * <p>
     * Note that this method is always called from inside
     * a block synchronized on {@link #cachedRanges}.
     *
     * @param range a newly constructed MacRange (which may get dropped)
     * @param key the lookup key
     * @return a reference to the appropriate unique MacRange instance
     */
    private static MacRange intern(MacRange range, String key) {
        MacRange alreadyCached = cachedRanges.get(range.normalizedSpec);
        MacRange keeper = alreadyCached != null ? alreadyCached : range;
        cachedRanges.put(range.normalizedSpec, keeper); // cached by string rep
        cachedRanges.put(key, keeper); // cached by given key
        return keeper;
    }

    /** Returns a MAC range for the given specification.
     * See the class documentation for a description of the
     * specification format.
     *
     * @param spec the MAC address range specification
     * @return a MAC range instance that embodies the specified
     *          MAC address range
     */
    public static MacRange valueOf(String spec) {
        final String key = spec.trim().toUpperCase(Locale.getDefault());
        synchronized (cachedRanges) {
            MacRange result = cachedRanges.get(key);
            return (result == null) ? intern(new MacRange(key), key) : result;
        }
    }

    /** Returns a MAC range for the given prefix. The implicit lower bytes
     * will be defined as '*'. For example,
     * <pre>
     *     MacPrefix p = MacPrefix.valueOf("11:22:33:44");
     *     MacRange r = MacRange.valueOf(p);    // 11:22:33:44:*:*
     *     MacAddress low = MacRange.first();   // 11:22:33:44:00:00
     *     MacAddress high = MacRange.last();   // 11:22:33:44:FF:FF
     * </pre>
     * @param prefix the MAC prefix
     * @return the equivalent MAC range
     */
    public static MacRange valueOf(MacPrefix prefix) {
        StringBuilder sb = new StringBuilder(prefix.toString());
        for (int i=prefix.size(); i<MacAddress.MAC_ADDR_SIZE; i++)
            sb.append(COLON_STAR);
        return valueOf(sb.toString());
    }

    /** Returns a list of MAC ranges that embody the given range specifications.
     * The range specifications should be supplied as a comma separated list.
     * See the class documentation for a description of the specification
     * format.
     *
     * @param commaSeparatedSpecs the comma separated list of range specs
     * @return a list of MAC range instances
     */
    public static List<MacRange> createRanges(String commaSeparatedSpecs) {
        List<MacRange> ranges = new ArrayList<MacRange>();
        Tokenizer t = new Tokenizer(commaSeparatedSpecs, COMMA);
        while (t.hasNext())
            ranges.add(valueOf(t.next()));
        return ranges;
    }

    /** Converts a list of MAC address ranges to a comma separated string
     * representation. This method is the inverse
     * of {@link #createRanges(String)}.
     *
     * @param ranges the list of ranges
     * @return a comma separated list representation
     */
    public static String rangeListToString(List<MacRange> ranges) {
        if (ranges == null || ranges.size() == 0)
            return "";

        StringBuilder sb = new StringBuilder();
        for (MacRange r: ranges) {
            sb.append(r).append(",");
        }
        final int len = sb.length();
        sb.replace(len-1,len,""); // remove trailing comma
        return sb.toString();
    }

    //==== The iterator

    private class MacIterator extends ByteRangeIterator<MacAddress> {
        @Override
        public MacAddress next() {
            byte[] next = it.next();
            return next==null ? null : MacAddress.valueOf(next);
        }
    }
}
