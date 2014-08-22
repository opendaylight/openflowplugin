/*
 * (c) Copyright 2010-2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.opendaylight.util.cache.CacheableDataType;
import org.opendaylight.util.cache.WeakValueCache;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.opendaylight.util.StringUtils.EOL;

/**
 * Represents a {@link Subnet} that has one or more {@link IpRange}s
 * associated with it that narrow the scope of the subnet (hence "partial").
 * <p>
 * It follows that all IP addresses represented
 * within the range(s) must fall within the given subnet, and that if more
 * than one range is specified they do not overlap.
 * <p>
 * All constructors for this class are private. Creating instances
 * of {@code PartialSubnet} is done via the static methods on the class.
 * <p>
 * Instances of this class are immutable, making them inherently thread-safe.
 * <p>
 * This class overrides {@link #equals} and {@link #hashCode} so that instances
 * play nicely with the Collection classes.
 * <p>
 * This class implements the {@link Comparable} interface to ensure that
 * a sorted list is presented in an intuitive order.
 *
 * @author Simon Hunt
 */
public final class PartialSubnet extends CacheableDataType
    implements Comparable<PartialSubnet> {

    private static final long serialVersionUID = -2645545994827752170L;

    /** Holds a subnet. */
    private final Subnet subnet;

    /** Holds the ranges that are subsets of the above subnet. */
    private final List<IpRange> ranges;

    // derived values
    private final transient BigInteger sz;
    private final transient long szLong;
    private final transient int szInt;

    /** Private constructor.
     *
     * @param subnet the subnet
     * @param ranges the list of ranges
     */
    private PartialSubnet(Subnet subnet, List<IpRange> ranges) {
        // note: the two parameters here are guaranteed to be non-null,
        //          and validated
        this.subnet = subnet;
        this.ranges = ranges;

        // compute the derived size values
        BigInteger b = BigInteger.ZERO;
        for (IpRange r: ranges)
            b = b.add(r.size());
        sz = b;
        szLong = AbstractByteRange.bigIntegerAsLong(b);
        szInt = AbstractByteRange.bigIntegerAsInt(b);
    }

    @Override
    public String toString() {
        return makeKey(subnet, ranges);
    }

    /** Returns a multi-line string representation of this instance.
     *
     * @return a multi-line string representation
     */
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString()).append(EOL);
        sb.append("**Size: ").append(sz);
        if (szLong == -1)
            sb.append(" (LONG and INT overflow)");
        else if (szInt == -1)
            sb.append(" (INT overflow)");
        sb.append(EOL);
        sb.append("**Subnet: ").append(subnet.toDebugString());
        for (int i=0; i<ranges.size(); i++)
          sb.append("**IpRange[").append(i).append("]: ")
                  .append(ranges.get(i).toDebugString());
        return sb.toString();
    }

    /** Returns the subnet.
     *
     * @return the subnet
     */
    public Subnet getSubnet() {
        return subnet;
    }

    /** Returns the first IP range. Equivalent to:
     * <pre>
     * .getRanges().get(0);
     * </pre>
     *
     * @return the first IP range
     */
    public IpRange getRange() {
        return ranges.get(0);
    }

    /** Returns a copy of the list of IP ranges.
     *
     * @return a copy of the list of IP ranges
     */
    public List<IpRange> getRanges() {
        return new ArrayList<IpRange>(ranges);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final PartialSubnet that = (PartialSubnet) o;
        return ranges.equals(that.ranges) && subnet.equals(that.subnet);
    }

    @Override
    public int hashCode() {
        return 31 * subnet.hashCode() + ranges.hashCode();
    }

    @Override
    public int compareTo(PartialSubnet o) {
        int result = subnet.compareTo(o.subnet);
        if (result != 0)
            return result;
        // if we get to here... subnets are the same

        int i=0;
        while(i<ranges.size() && i<o.ranges.size()) {
            result = ranges.get(i).compareTo(o.ranges.get(i));
            if (result != 0)
                return result;
            i++;
        }
        // if we get to here... ranges (up to shortest list) are the same

        // have the shorter list come before the longer list
        return ranges.size() - o.ranges.size();
    }

    /** Returns true if this partial subnet contains the given IP address;
     * false otherwise.
     *
     * @param ip the IP address
     * @return true if this partial subnet contains the IP address;
     *          false otherwise.
     */
    public boolean contains(IpAddress ip) {
        for (IpRange r: ranges) {
            if (r.contains(ip))
                return true;
        }
        return false;
    }

    /** Returns true if this partial subnet contains the given
     * IP address range; false otherwise.
     *
     * @param range the IP address range
     * @return true if this partial subnet contains the IP address range;
     *          false otherwise.
     */
    public boolean contains(IpRange range) {
        for (IpRange r: ranges) {
            if (r.contains(range))
                return true;
        }
        return false;
    }

    /** Returns the size of the partial subnet. In other words, how
     * many IP addresses are represented by the defined range(s).
     *
     * @return the size of the partial subnet (number of IP addresses)
     * @see #sizeAsLong
     * @see #sizeAsInt
     */
    public BigInteger size() {
        return sz;
    }

    /** Returns the size of the partial subnet as a long value, unless the
     * value would be greater than {@link Long#MAX_VALUE}, in which case
     * this method returns -1L.
     *
     * @return the size of the range if it does not
     *          overflow a long; -1L otherwise
     * @see #size
     */
    public long sizeAsLong() {
        return szLong;
    }

    /** Returns the size of the partial subnet as an integer value, unless the
     * value would be greater than {@link Integer#MAX_VALUE}, in which case
     * this method returns -1.
     *
     * @return the size of the range if it does not
     *          overflow an int; -1 otherwise
     * @see #size
     */
    public int sizeAsInt() {
        return szInt;
    }

    /** Returns an IP address iterator that iterates across the IP addresses
     * defined in the range(s) associated with this partial subnet.
     *
     * @return an IP address iterator
     */
    public Iterator<IpAddress> iterator() {
        return new Iter(ranges);
    }

    // === PRIVATE serialization ==============================================

    //== Implementation note:
    //      We use default serialization to serialize the subnet and
    //      range fields

    private Object readResolve() throws ObjectStreamException {
        // when this is called, subnet and range have been populated.
        // return the appropriate cached instance instead.
        Object o;
        try {
            o = valueOf(subnet, ranges);
        } catch (NullPointerException e) {
            throw new InvalidObjectException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new InvalidObjectException(e.getMessage());
        }
        return o;
    }


    //=== STATIC METHODS ======================================================

    private static final WeakValueCache<String, PartialSubnet> cachedItems =
            new WeakValueCache<String, PartialSubnet>(getRefQ());

    /** Creates the lookup key for an instance of PartialSubnet.
     *
     * @param subnet the subnet and mask
     * @param ranges the IP address ranges
     * @return the key
     */
    private static String makeKey(Subnet subnet, List<IpRange> ranges) {
        StringBuilder sb = new StringBuilder(subnet.toString());
        for (IpRange r: ranges)
            sb.append(",").append(r);
        return sb.toString();
    }

    //=== PUBLIC API ==========================================================

    /** Returns a {@code PartialSubnet} instance that encapsulates the given
     * subnet, with an implied IP address range that spans the complete subnet.
     * This is equivalent to calling:
     * <pre>
     * PartialSubnet.valueOf(subnet, (IpRange)null);
     * </pre>
     *
     * @param subnet the subnet
     * @return the partial subnet instance
     * @throws NullPointerException if subnet is null
     */
    public static PartialSubnet valueOf(Subnet subnet) {
        return valueOf(subnet, (IpRange)null);
    }

    /** Returns a {@code PartialSubnet} instance that encapsulates the given
     * subnet, and IP address range.
     * Note that the IP range (if specified) must fall completely
     * within the IP addresses specified by the subnet.
     * If {@code null} is specified as the range, this is taken as shorthand
     * for specifying a range that completely matches the entire subnet.
     *
     * @param subnet the subnet
     * @param range the IP range (or null for entire subnet)
     * @return the partial subnet instance
     * @throws NullPointerException if subnet is null
     * @throws IllegalArgumentException if range is not appropriate for
     *          the given subnet
     */
    public static PartialSubnet valueOf(Subnet subnet, IpRange range) {
        List<IpRange> r = new ArrayList<IpRange>();
        if (range != null)
            r.add(range);
        return valueOf(subnet, r);
    }

    /** Returns a {@code PartialSubnet} instance that encapsulates the
     * given subnet, and IP address ranges.
     * Note that the IP ranges (if specified) must fall completely
     * within the IP addresses specified by the subnet, and must not overlap.
     * If {@code null} or an empty list is supplied as the list of ranges,
     * this is taken as shorthand for specifying a single range that
     * completely matches the entire subnet.
     *
     * @param subnet the subnet
     * @param ranges the list of ranges (or null/empty for entire subnet)
     * @return the partial subnet instance
     * @throws NullPointerException if subnet is null
     * @throws IllegalArgumentException if the ranges are not appropriate
     *          for the given subnet
     */
    public static PartialSubnet valueOf(Subnet subnet, List<IpRange> ranges) {
        if (subnet == null)
            throw new NullPointerException("subnet cannot be null");

        List<IpRange> r = (ranges == null) ?
                new ArrayList<IpRange>() : new ArrayList<IpRange>(ranges);

        // fabricate a single range that spans the entire subnet, if no
        // ranges were specified
        if (r.size() == 0)
            r.add(subnet.getEquivalentIpRange());

        // find or create an instance and return it
        final String key = makeKey(subnet, r);
        synchronized (cachedItems) {
            PartialSubnet result = cachedItems.get(key);
            if (result == null) {
                validate(subnet, r);
                result = new PartialSubnet(subnet, r);
                cachedItems.put(key, result);
            }
            return result;
        }
    }


    /** Ensures that the subnet and ranges are valid. Parameters are guaranteed
     * to be non-null, and the list of ranges contains at least one element.
     *
     * @param subnet the subnet
     * @param ranges the IP address ranges
     * @throws IllegalArgumentException if there is an issue
     */
    private static void validate(Subnet subnet, List<IpRange> ranges) {
        // first check that the IP families are all the same...
        IpAddress.Family f = subnet.getFamily();
        for (IpRange r: ranges) {
            if (!f.equals(r.getFamily()))
                throw new IllegalArgumentException("mis-matched IP families");
        }

        // now check that all the ranges are contained within the subnet
        IpRange er = subnet.getEquivalentIpRange();
        for (IpRange r: ranges) {
            if (!er.contains(r))
                throw new IllegalArgumentException("Range: " + r +
                                " falls outside the subnet " + subnet);
        }

        // finally check that none of the ranges overlap
        if (ranges.size() > 1) {
            final int n = ranges.size();
            for (int i=0; i<n-1; i++) {
                for (int j=i+1; j<n; j++) {
                    IpRange ri = ranges.get(i);
                    IpRange rj = ranges.get(j);
                    if (ri.intersects(rj))
                        throw new IllegalArgumentException(
                            "Intersecting ranges (" + i + "," + j + "): [" +
                                                    ri + "],[" + rj + "]");
                }
            }
        }
    }

    /** Returns a {@code PartialSubnet} instance that encapsulates the
     * subnet and any specified IP address range(s) expressed by the given
     * string specification. Note that the IP range(s) (if specified) must fall
     * completely within the IP addresses specified by the subnet, and if more
     * than one range is declared, they must not overlap.
     * <p>
     * The expected form of the specification string is:
     * <pre>
     * {subnet}[,{ip-range}[,{ip-range}[,...]]]
     * </pre>
     * where {@code subnet} is the form accepted by
     * {@link Subnet#valueOf(String)}, and
     * {@code ip-range} is the form accepted by
     * {@link IpRange#valueOf(String)}.
     * For example:
     * <pre>
     * 192.168.12.0/24,192.168.12.11-17
     * </pre>
     * defines a partial subnet that restricts the given subnet
     * ({@code 192.168.12.0} through {@code 192.168.12.255} with subnet mask
     * {@code 255.255.255.0})
     * to the IP addresses {@code 192.168.12.11} through {@code 192.168.12.17}.
     * <p>
     * Another example:
     * <pre>
     * 192.168.12.0/23
     * </pre>
     * defines a partial subnet that is equivalent to the subnet
     * ({@code 192.168.12.0} through {@code 192.168.13.255} with subnet mask
     * {@code 255.255.254.0})
     * with all IP addresses included. The implied range is
     * {@code 192.168.12-13.*}.
     * <p>
     * Some more examples:
     * <pre>
     * 10.11.12.0/24,10.11.12.0-127,10.11.12.200-205
     * 15.37.24.0/21,15.37.24.*,15.37.25.1-100,15.37.25.121-123
     * </pre>
     * Note that there can be no white-space in the string.
     *
     * @param spec the string specification
     * @return the partial subnet instance
     * @throws NullPointerException if spec is null
     * @throws IllegalArgumentException if the parameter is malformed
     */
    public static PartialSubnet valueOf(String spec) {
        if (spec == null)
            throw new NullPointerException("spec cannot be null");

        String s = spec.trim();
        if (s.matches(".*\\s.*"))
            throw new IllegalArgumentException("spec cannot contain " +
                                                "whitespace : [" + s + "]");

        String[] pieces = s.split(",");
        if (pieces.length < 1)
            throw new IllegalArgumentException(E_MALFORMED + s);

        Subnet subnet;
        try {
            subnet = Subnet.valueOf(pieces[0]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(E_MALFORMED + s, e);
        }

        List<IpRange> ranges = new ArrayList<IpRange>();

        if (pieces.length == 1) {
            // implied range
            ranges.add(subnet.getEquivalentIpRange());

        } else {
            for (int i=1; i<pieces.length; i++) {
                IpRange range;
                try {
                    range = IpRange.valueOf(pieces[i]);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(E_MALFORMED + s, e);
                }
                ranges.add(range);
            }
        }

        return valueOf(subnet, ranges);
    }

    static final String E_MALFORMED = "Malformed Partial Subnet spec: ";


    // ===============================================

    /** Private iterator implementation. */
    private static class Iter implements Iterator<IpAddress> {

        private final List<Iterator<IpAddress>> iterators;

        private int currentIdx = 0;
        private Iterator<IpAddress> currentIter;

        private Iter(List<IpRange> ranges) {
            iterators = new ArrayList<Iterator<IpAddress>>(ranges.size());
            for (IpRange r: ranges)
                iterators.add(r.iterator());
            currentIter = iterators.get(0);
        }

        @Override
        public boolean hasNext() {
            return currentIdx < iterators.size()-1 || currentIter.hasNext();
        }

        @Override
        public IpAddress next() {
            IpAddress ip = currentIter.next();
            if (ip == null && currentIdx < iterators.size()-1) {
                currentIter = iterators.get(++currentIdx);
                ip = currentIter.next();
            }
            return ip;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
