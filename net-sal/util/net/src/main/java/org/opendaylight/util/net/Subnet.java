/*
 * (c) Copyright 2010-2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import static org.opendaylight.util.StringUtils.EOL;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;

import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.cache.CacheableDataType;
import org.opendaylight.util.cache.WeakValueCache;

/**
 * Represents a subnet.
 * <p>
 * Defined by a (starting) subnet address and its associated subnet mask
 * (for both IPv4 and IPv6).
 * <p>
 * All constructors for this class are private. Creating instances
 * of {@code Subnet} is done via the static methods on the class.
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
public final class Subnet extends CacheableDataType
        implements Comparable<Subnet> {

    private static final long serialVersionUID = -2728683094867990655L;

    private static final String SLASH = "/";

    /** Holds the subnet address. */
    private final IpAddress address;

    /** The subnet mask. */
    private final SubnetMask mask;

    /** The equivalent IP range. */
    private final transient IpRange range;

    /** Private constructor.
     *
     * @param address the start address
     * @param mask the subnet mask
     */
    private Subnet(IpAddress address, SubnetMask mask) {
        // note: address and mask are guaranteed to be non-null, and validated
        this.address = address;
        this.mask = mask;
        this.range = IpRange.valueOf(this);
    }

    @Override
    public String toString() {
        return makeKey(address, mask);
    }

    /** Returns a multi-line string representation of this instance.
     *
     * @return a multi-line string representation
     */
    public String toDebugString() {
        StringBuilder sb = new StringBuilder(toString()).append(EOL);
        sb.append("  Address: ").append(address).append(EOL);
        sb.append("  Mask   : ").append(mask).append(EOL);
        sb.append("  Lowest : ").append(range.first()).append(EOL);
        sb.append("  Highest: ").append(range.last()).append(EOL);
        return sb.toString();
    }

    /** Returns the subnet address.
     *
     * @return the subnet address
     */
    public IpAddress getAddress() {
        return address;
    }

    /** Returns the subnet mask.
     *
     * @return the subnet mask
     */
    public SubnetMask getMask() {
        return mask;
    }

    /** Returns an IP range that represents the same set of IP addresses
     * that belong to this subnet.
     *
     * @return an equivalent IP range
     */
    public IpRange getEquivalentIpRange() {
        return range;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Subnet that = (Subnet) o;
        return mask.equals(that.mask) && address.equals(that.address);
    }

    @Override
    public int hashCode() {
        return 31 * address.hashCode() + mask.hashCode();
    }

    @Override
    public int compareTo(Subnet o) {
        int result = address.compareTo(o.address);
        return result == 0 ? mask.compareTo(o.mask) : result;
    }

    /** Returns the IP address family of the subnet and mask.
     *
     * @return the IP address family
     */
    public IpAddress.Family getFamily() {
        return address.getFamily();
    }

    /** Returns the broadcast address for this subnet.
     * For IPv4 this is the subnet address with the host portion set to
     * all 1 bits. For example:
     * <pre>
     * IpAddress broadcast = Subnet.valueOf("192.168.1.0/24").getBroadcastAddress();
     * assert(broadcast.equals(IpAddress.valueOf("192.168.1.255");
     * </pre>
     * TODO: need to decide how to handle IPv6 subnets
     *
     * @return the broadcast address associated with this subnet
     * @throws UnsupportedOperationException for IPv6 instances
     */
    public IpAddress getBroadcastAddress() {
        if (getFamily() == IpAddress.Family.IPv6)
            throw new UnsupportedOperationException("can't be used for " +
                                                    "IPv6 addresses");

        byte[] bytes = mask.toByteArray();          // get the subnet mask
        ByteUtils.not(bytes);                       // flip the bits
        ByteUtils.or(bytes, address.toByteArray()); // add in subnet address
        return IpAddress.valueOf(bytes);
    }

    /** Returns true if the given IP address is contained by this subnet.
     *
     * @param ip the IP address
     * @return true if this subnet contains the IP address; false otherwise
     */
    public boolean contains(IpAddress ip) {
        return range.contains(ip);
    }

    /** Returns true if the given IP address range is contained by this subnet.
     *
     * @param range the IP address range
     * @return true if this subnet contains the IP address range;
     *          false otherwise
     * @throws IllegalArgumentException if other range is not the same
     *          IP family as this one
     * @throws NullPointerException if other is null
     */
    public boolean contains(IpRange range) {
        return this.range.contains(range);
    }

    // === PRIVATE serialization ==============================================

    //== Implementation note:
    //      We use default serialization to serialize the address and
    //      mask fields

    private Object readResolve() throws ObjectStreamException {
        // when this is called, address and mask have been populated.
        // return the appropriate cached instance instead.
        Object o;
        try {
            o = valueOf(address, mask);
        } catch (NullPointerException e) {
            throw new InvalidObjectException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new InvalidObjectException(e.getMessage());
        }
        return o;
    }

    //=== STATIC METHODS ======================================================

    private static final WeakValueCache<String, Subnet> cachedSubnets =
            new WeakValueCache<String, Subnet>(getRefQ());

    /** Creates the lookup key for an instance of Subnet.
     *
     * @param subnet the subnet address
     * @param mask the subnet mask
     * @return the key
     */
    private static String makeKey(IpAddress subnet, SubnetMask mask) {
        return subnet.toString() + SLASH + mask.getOneBitCount();
    }

    //=== PUBLIC API ==========================================================

    /** Returns a {@code Subnet} instance that encapsulates the
     * given subnet address and subnet mask.
     *
     * @param address the subnet address
     * @param mask the subnet mask
     * @return the subnet instance
     * @throws NullPointerException if either address or mask is null
     * @throws IllegalArgumentException if mask is not appropriate for the
     *          given subnet address
     */
    public static Subnet valueOf(IpAddress address, SubnetMask mask) {
        if (address == null || mask == null)
            throw new NullPointerException("null parameter(s)");

        // first, make sure we are zeroing out the host portion of the address
        final IpAddress subnetAddress = mask.networkPortion(address);

        final String key = makeKey(subnetAddress, mask);
        synchronized (cachedSubnets) {
            Subnet result = cachedSubnets.get(key);
            if (result == null) {
                validate(subnetAddress, mask);
                result = new Subnet(subnetAddress, mask);
                cachedSubnets.put(key, result);
            }
            return result;
        }
    }

    /** Ensures that the subnet address and mask are valid together.
     *
     * @param address the subnet address
     * @param mask the subnet mask
     * @throws IllegalArgumentException if there is an issue
     */
    private static void validate(IpAddress address, SubnetMask mask) {
        if (!address.getFamily().equals(mask.getFamily()))
            throw new IllegalArgumentException("mis-matched IP families");
        // TODO: Review - further validation required??
        /* for example
         *   IPv4 Class A address is ok with
         *                  basic subnet mask validation (top 8 bits == 1)
         *   IPv4 Class B address requires top 16 bits == 1
         *   IPv4 Class C address requires top 24 bits == 1
         *   IPv6 further validation unknown at this point
         */
    }


    /** Returns a {@code Subnet} instance that encapsulates the subnet address
     * and subnet mask expressed in the given string specification. If a slash
     * is present in the string, it is assumed that CIDR notation is being
     * used. If no slash is present in the string, it is assumed that the
     * address is a class-A, class-B, or class-C (IPv4) address where the
     * subnet mask is implied.
     * <p>
     * Classless Inter-Domain Routing (CIDR) is a way of expressing one
     * (or many) subnets and their associated subnet mask.
     * <p>
     * The expected form of the string is
     * <pre>
     * {subnet-address}/{n}
     * </pre>
     * where {@code subnet-address} is the start subnet address
     * and {@code n}  is the number of (leftmost) '1' bits in the mask.
     * For example:
     * <pre>
     * 192.168.12.0/23
     * </pre>
     * applies the network mask 255.255.254.0 to the 192.168 network,
     * starting at 192.168.12.0. This notation represents the address
     * range 192.168.12.0 - 192.168.13.255. Compared to traditional
     * class-based networking, 192.168.12.0/23 represents an aggregation of
     * the two Class C subnets 192.168.12.0 and 192.168.13.0 each having a
     * subnet mask of 255.255.255.0.
     * <p>
     * Examples of implied subnet masks:
     * <pre>
     * Subnet.valueOf("15.0.0.0")  --> address 15.0.0.0, mask 255.0.0.0
     * Subnet.valueOf("180.0.0.0")  --> address 180.0.0.0, mask 255.255.0.0
     * Subnet.valueOf("200.0.0.0")  --> address 200.0.0.0, mask 255.255.255.0
     * </pre>
     *
     * @param spec the string specification
     * @return the subnet instance
     * @throws NullPointerException if the parameter is null
     * @throws IllegalArgumentException if the parameter is malformed
     */
    public static Subnet valueOf(String spec) {
        if (spec == null)
            throw new NullPointerException("spec cannot be null");

        if (!spec.contains(SLASH))
            return subnetBasedOnImpliedMask(spec);

        String[] pieces = spec.trim().split(SLASH);
        if (pieces.length != 2)
            throw new IllegalArgumentException(E_MALFORMED + spec);

        IpAddress address;
        try {
            address = IpAddress.valueOf(pieces[0]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(E_MALFORMED + spec, e);
        }

        SubnetMask mask;
        try {
            int numBits = Integer.valueOf(pieces[1]);
            mask = address.getSubnetMask(numBits);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(E_MALFORMED + spec, e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(E_MALFORMED + spec, e);
        }

        return valueOf(address, mask);
    }

    /** Returns the subnet with implied mask, based on class of the address.
     *
     * @param spec the IP address
     * @return the appropriate subnet instance
     */
    private static Subnet subnetBasedOnImpliedMask(String spec) {
        IpAddress address = IpAddress.valueOf(spec);
        IpAddress.AddressClass aClass = address.getAddressClass();
        switch (aClass) {
            case A:
            case B:
            case C:
                return Subnet.valueOf(address, aClass.getImpliedMask());

            case D:
            case E:
            case CLASSLESS:
            default:
                throw new IllegalArgumentException("IP address is not " +
                                            "class A,B,C address: " + spec);
        }

    }

    static final String E_MALFORMED = "Malformed CIDR specification: ";
}
