/*
 * (c) Copyright 2010-2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.opendaylight.util.ByteArrayGenerator;
import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.Tokenizer;
import org.opendaylight.util.cache.WeakValueCache;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Represents a range of IP addresses. Instances of this class are usually
 * created using the {@link #valueOf} factory method.
 * (However, see also {@link Subnet#getEquivalentIpRange()})
 * <p>
 * A string specification is used to define the IP address range.
 * This can be one of two forms, declaring either a range of IPv4 addresses or
 * a range of IPv6 addresses.
 * <p>
 * <b>For IPv4 addresses</b> the form is:
 * <pre>
 * {byteSpec}.{byteSpec}.{byteSpec}.{byteSpec}
 * </pre>
 * where <em>byteSpec</em> represents a single byte in the address, and is
 * of the form:
 * <pre>
 * {n} | {n}-{m} | *
 * </pre>
 * where <em>n</em> and <em>m</em> are values in the range 0 to 255, <em>m</em>
 * (when specified) is &gt; <em>n</em>, and <em>*</em> stands for 0-255
 * <p>
 * For example, the specification {@code "15.29.36-37.1-255"} declares that
 * the range of IP addresses starts with {@code 15.29.36.1} through
 * {@code 15.29.36.255}, then continues with {@code 15.29.37.1} through
 * {@code 15.29.37.255}.
 *
 * <p>
 * <b>For IPv6 addresses</b> the form is:
 * <pre>
 * {2Bytes}:{2Bytes}:{2Bytes}:{2Bytes}:{2Bytes}:{2Bytes}:{2Bytes}:{2Bytes}
 * </pre>
 * where <em>2Bytes</em> represents two bytes in the address and is of
 * the form:
 * <pre>
 * {nnnn} | {nnnn}-{mmmm} | *
 * </pre>
 * where <em>nnnn</em> and <em>mmmm</em> are hex values in the range
 * 0000 to FFFF, <em>mmmm</em> (when specified) is &gt; <em>nnnn</em>,
 * and <em>*</em> stands for 0000-FFFF.
 * <p>
 * Leading zeros may be dropped, and "double-colon" shorthand notation may
 * be used. For example, the specification {@code "FF:FAB::0000-0003:*"}
 * declares that the range of IP addresses starts with
 * <pre>
 * 00FF:0FAB:0000:0000:0000:0000:0000:0000
 * </pre>
 * and ends with
 * <pre>
 * 00FF:0FAB:0000:0000:0000:0000:0003:FFFF
 * </pre>
 *
 * @author Simon Hunt
 */
public final class IpRange extends AbstractByteRange<IpAddress>
        implements Comparable<IpRange> {

    private static final long serialVersionUID = -9146743566875878346L;

    /** Constructs the range from the given spec.
     *
     * @param spec the range specification
     */
    private IpRange(String spec) {
        super(spec);
    }

    @Override
    protected ByteArrayGenerator getBag(String spec) {
        String bSpec = convertIpSpecToByteSpec(spec);
        ByteArrayGenerator bag;

        if (spec.contains(DOT)) {
            bag = ByteArrayGenerator.create(bSpec);
            if (bag.size() != 4)
                throw new IllegalArgumentException("Malformed IPv4 spec [" +
                                                    spec + "]");
        } else {
            bag = ByteArrayGenerator.createFromHex(bSpec);
            if (bag.size() != 16)
                throw new IllegalArgumentException("Malformed IPv6 spec [" +
                                                    spec + "]");
        }
        return bag;
    }

    @Override
    public boolean contains(IpAddress ipAddress) {
        return bag.contains(ipAddress.toByteArray());
    }

    @Override
    public IpAddress first() {
        return IpAddress.valueOf(bag.lowest());
    }

    @Override
    public IpAddress last() {
        return IpAddress.valueOf(bag.highest());
    }

    @Override
    public IpAddress random() {
        return IpAddress.valueOf(bag.generate());
    }

    @Override
    public Iterator<IpAddress> iterator() {
        return new IpIterator();
    }

    @Override
    public int compareTo(IpRange o) {
        int result = first().compareTo(o.first());
        return result == 0 ? last().compareTo(o.last()) : result;
    }

    /** Returns the IP address family of this range.
     *
     * @return the IP address family
     */
    public IpAddress.Family getFamily() {
        return first().getFamily();
    }

    /** Returns an instance of {@link Subnet} equivalent to this IP address
     * range, if appropriate; null otherwise.
     * <p>
     * So, what is meant by "if appropriate"? Well, if the {@link #first first}
     * IP address in this range represents the subnet address with
     * <em>all zero bits</em> in the host portion of the address, and the
     * {@link #last last} IP address in this range represents the subnet
     * address with <em>all one bits</em> in the host portion of the address,
     * then the equivalent subnet can be computed and returned. For example:
     * <pre>
     * IpRange range = IpRange.valueOf("15.23.12-13.*");
     * Subnet subnet = range.getEquivalentSubnet();
     *
     * assert(subnet.equals(Subnet.valueOf("15.23.12.0/23")));
     * assert(subnet.getAddress().equals(IpAddress.valueOf("15.23.12.0");
     * assert(subnet.getMask().equals(SubnetMask.valueOf("255.255.254.0");
     *
     * ---
     * IpRange range = IpRange.valueOf("15.23.47.1-100");
     * Subnet subnet = range.getEquivalentSubnet();
     *
     * assert(subnet == null);
     * </pre>
     *
     * @return the subnet equivalent to this IP address range, if appropriate;
     *      null otherwise
     */
    public Subnet getEquivalentSubnet() {
        /* Implementation Note:
         * For range R, (first address Rf, last address Rl) there exists an
         * equivalent subnet IFF:
         *   Rf represents the subnet address with all zero bits for the host,
         *   and
         *   Rl represents the subnet address with all one bits for the host.
         * In which case:
         *   the subnet address = Rf
         *   the subnet mask    = NOT( Rf XOR Rl )
         */
            // <-- Network --><-- Host -->                : *Gotcha
            //   ..nnnnnnn..     .0000.     (first)       : 0011
            //   ..nnnnnnn..     .1111.     (last)        : 1100
            //   ..0000000..     .1111.     (XOR)         : 1111
            //   ..1111111..     .0000.     (NOT)
        byte[] bytes = first().toByteArray();
        ByteUtils.xor(bytes, last().toByteArray());
        ByteUtils.not(bytes);
        SubnetMask mask;
        try {
            mask = SubnetMask.valueOf(bytes);
        } catch (IllegalArgumentException e) {
            return null; // bytes do not represent a valid subnet mask
        }
        // ensure we weren't caught by the *Gotcha
        if (mask.networkPortion(first()).equals(first()))
            return Subnet.valueOf(first(), mask);

        return null;
    }

    // === PRIVATE serialization ==============================================

    //== Implementation note:
    //      We use default serialization to serialize the String spec.

    private Object readResolve() throws ObjectStreamException {
        // when this is called, spec has been populated.
        // create a new instance to make sure the byte array generator is
        // initialized
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

    // ====


    /** Converts a subnet address and mask to an equivalent IP address range.
     *
     * @param subnet the subnet and mask
     * @return the equivalent range
     */
    static IpRange valueOf(Subnet subnet) {
        // NOTE: subnet bytes are guaranteed to have the host bits zeroed out
        byte[] subnetBytes = subnet.getAddress().toByteArray();
        byte[] maskBytes = subnet.getMask().toByteArray();

        byte[] lowest = subnetBytes.clone();

        byte[] hostMask = maskBytes.clone();
        ByteUtils.not(hostMask); // subnet mask flipped

        byte[] highest = subnetBytes.clone();
        // set all the bits in the host portion of the address
        ByteUtils.or(highest, hostMask);

        if (subnet.getFamily().equals(IpAddress.Family.IPv4))
            return valueOf(createIpv4RangeSpec(lowest, highest));
        return valueOf(createIpv6RangeSpec(lowest, highest));
    }

    private static String createIpv4RangeSpec(byte[] lowest, byte[] highest) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<lowest.length; i++) {
            if (lowest[i] == 0 && highest[i] == FF)
                sb.append("*");
            else if (lowest[i] == highest[i])
                sb.append(byteAsInt(lowest[i]));
            else
                sb.append(byteAsInt(lowest[i]))
                  .append("-")
                  .append(byteAsInt(highest[i]));
            sb.append(".");
        }
        final int len = sb.length();
        sb.replace(len-1, len, ""); // remove trailing delimiter
        return sb.toString();
    }

    private static int byteAsInt(byte b) { return b < 0 ? b+B : b; }

    private static String createIpv6RangeSpec(byte[] lowest, byte[] highest) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<lowest.length; i+=2) {
            if (lowest[i] == 0 && lowest[i+1] == 0 &&
                highest[i] == FF && highest[i+1] == FF)
                sb.append("*");
            else if (lowest[i] == highest[i] && lowest[i+1] == highest[i+1])
                sb.append(ByteUtils.hex(lowest,i,2));
            else
                sb.append(ByteUtils.hex(lowest,i,2))
                  .append("-")
                  .append(ByteUtils.hex(highest,i,2));
            sb.append(":");
        }
        final int len = sb.length();
        sb.replace(len-1, len, ""); // remove trailing delimiter
        return sb.toString();
    }

    //=== CACHE ===============================================================

    private static final WeakValueCache<String, IpRange> cachedRanges =
            new WeakValueCache<String, IpRange>(getRefQ());

    /** Ensures that all equivalent IP range encoding keys
     * map to the same instance of IpRange.
     * <p>
     * Note that this method is always called from inside
     * a block synchronized on {@link #cachedRanges}.
     *
     * @param range a newly constructed IpRange (which may get dropped)
     * @param key the lookup key
     * @return a reference to the appropriate unique IpRange instance
     */
    private static IpRange intern(IpRange range, String key) {
        IpRange alreadyCached = cachedRanges.get(range.normalizedSpec);
        IpRange keeper = alreadyCached != null ? alreadyCached : range;
        cachedRanges.put(range.normalizedSpec, keeper); // cached by str rep
        cachedRanges.put(key, keeper); // cached by given key
        return keeper;
    }



    /** Returns an IP range for the given specification.
     * See the class documentation for a description of the specification
     * format.
     *
     * @param spec the IP address range specification
     * @return an IP range instance that embodies the specified IP address
     *          range
     */
    public static IpRange valueOf(String spec) {
        // from CIDR...
        if (spec.contains("/"))
            return Subnet.valueOf(spec).getEquivalentIpRange();

        final String key = spec.trim().toUpperCase(Locale.getDefault());
        synchronized (cachedRanges) {
            IpRange result = cachedRanges.get(spec);
            return (result == null) ? intern(new IpRange(key), key) : result;
        }
    }

    /** Returns a list of IP ranges that embody the given range specifications.
     * The range specifications should be supplied as a comma separated list.
     * See the class documentation for a description of the specification
     * format.
     *
     * @param commaSeparatedSpecs the comma separated list of range specs
     * @return a list of IP range instances
     */
    public static List<IpRange> createRanges(String commaSeparatedSpecs) {
        List<IpRange> ranges = new ArrayList<IpRange>();
        Tokenizer t = new Tokenizer(commaSeparatedSpecs, COMMA);
        while (t.hasNext())
            ranges.add(valueOf(t.next()));
        return ranges;
    }

    /** Converts a list of IP address ranges to a comma separated string
     * representation.
     * This method is the inverse of {@link #createRanges(String)}.
     *
     * @param ranges the list of ranges
     * @return a comma separated list representation
     */
    public static String rangeListToString(List<IpRange> ranges) {
        if (ranges == null || ranges.size() == 0)
            return "";

        StringBuilder sb = new StringBuilder();
        for (IpRange r: ranges) {
            sb.append(r).append(",");
        }
        final int len = sb.length();
        sb.replace(len-1,len,""); // remove trailing comma
        return sb.toString();
    }

    // ========= The iterator

    private class IpIterator extends ByteRangeIterator<IpAddress> {

        @Override
        public IpAddress next() {
            byte[] next = it.next();
            return next==null ? null : IpAddress.valueOf(next);
        }

    }

    /** Transforms an IP spec to a byte spec.
     *
     * @param spec the IP spec
     * @return the corresponding byte spec
     * @throws NullPointerException if the spec is null
     * @throws IllegalArgumentException if the supplied spec is malformed
     */
    private static String convertIpSpecToByteSpec(String spec) {
        if (spec == null)
            throw new NullPointerException("spec cannot be null");

        String byteSpec;

        // TODO - revise this assumption if we ever support mixed notation IPv6
        if (spec.contains(DOT)) {
            // attempt to interpret this as an IPv4 address spec
            byteSpec = spec.replace('.', ':');
        } else {
            // attempt to interpret this as an IPv6 address spec

            // first, pull apart spec: low and high
            String[] pieces = spec.split(":");
            final int n = pieces.length;
            String[] low = new String[n];
            String[] high = new String[n];
            for (int i=0; i<n; i++) {
                if (pieces[i].contains("-")) {
                    // split further
                    String[] loHi = pieces[i].split("-");
                    low[i] = loHi[0];
                    high[i] = loHi[1];
                } else if (pieces[i].equals("*")) {
                    low[i] = "0000";
                    high[i] = "ffff";
                } else {
                    low[i] = pieces[i];
                    high[i] = pieces[i];
                }
            }
            // now reassemble a low address and a high address, and let the
            // IpAddress class figure out the "double-colon" shorthand, if any
            IpAddress ipLow;
            IpAddress ipHigh;
            try {
                ipLow = IpAddress.valueOf(reassemble(low));
                ipHigh = IpAddress.valueOf(reassemble(high));
            } catch (IllegalArgumentException iae) {
                // rethrow, but couched in our own terms
                throw new IllegalArgumentException("Malformed IPv6 spec [" +
                                                    spec + "]");
            }

            // from here, we can assemble a byte spec
            byte[] lowBytes = ipLow.toByteArray();
            byte[] highBytes = ipHigh.toByteArray();

            StringBuilder sb = new StringBuilder();
            for (int i=0; i<lowBytes.length; i++)
                sb.append(makeByteSpec(lowBytes[i], highBytes[i])).append(":");
            final int len = sb.length();
            sb.replace(len-1, len, "");     // drop final appended colon

            byteSpec = sb.toString();
        }
        return byteSpec;
    }

    private static final int B = 256;
    private static final int FF = 0xff-B;

    private static String makeByteSpec(byte lo, byte hi) {
        int intLo = lo < 0 ? B+lo : lo;
        int intHi = hi < 0 ? B+hi : hi;
        if (intLo == intHi)
            return String.format("%02X", intLo);
        return String.format("%02X-%02X", intLo, intHi);
    }

    private static String reassemble(String[] pieces) {
        StringBuilder sb = new StringBuilder();
        for (String s: pieces) {
            sb.append(s).append(":");
        }
        final int len = sb.length();
        if (sb.toString().endsWith(":")) {
            sb.replace(len-1, len, "");     // drop final appended colon
        }
        return sb.toString();
    }
}
