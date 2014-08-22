/*
 * (c) Copyright 2010-2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;

import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.cache.CacheableDataType;
import org.opendaylight.util.cache.WeakValueCache;
import org.opendaylight.util.net.IpAddress.Family;

/**
 * Represents a subnet mask (either IPv4 or IPv6).
 * <p>
 * All constructors for this class are private. Creating instances of
 * {@code SubnetMask} is done via the static methods on the class.
 * <p>
 * Instances of this class are immutable, making them inherently thread-safe.
 * <p>
 * This class overrides {@link #equals} and {@link #hashCode} so that instances
 * play nicely with the Collection classes.
 * <p>
 * This class implements the {@link Comparable} interface to ensure that a
 * sorted list of subnet masks is presented in an intuitive order.
 *
 * @author Simon Hunt
 */
public final class SubnetMask extends CacheableDataType
        implements Comparable<SubnetMask> {

    private static final long serialVersionUID = 87576304792061262L;

    private static final String E_INVALID = "invalid subnet mask value: ";

    private static final int B = 256;
    private static final int FF = 0xff-B;

    /** Array of bytes with successively more 1 bits from the left.
     *  Note that the unit tests use this as well as createMaskArray().
     */
    static final byte[] BYTE_ME = { 0x80-B, 0xc0-B, 0xe0-B, 0xf0-B,
                                    0xf8-B, 0xfc-B, 0xfe-B, 0xff-B };

    /** Holds the mask in an IP address instance. */
    private final IpAddress mask;

    /** The number of 1 bits */
    private final transient int oneBitCount;

    /** Constructs a newly allocated {@code SubnetMask} object that represents
     * the subnet mask defined by the given {@code IpAddress} instance.
     *
     * @param mask the IP address instance representing the mask
     */
    private SubnetMask(IpAddress mask) {
        // note: mask is guaranteed to be not-null here...
        oneBitCount = validateMask(mask);
        this.mask = mask;
    }

    /** Throws an exception if mask is not acceptable (IPv4).
     *
     * @param mask the proposed subnet mask value
     * @return the number of 1 bits
     */
    private int validateMask(IpAddress mask) {
        /*
         * IPv4:  4 bytes =  32 bits
         * IPv6: 16 bytes = 128 bits
         *
         * verify that:
         *   + top 8 bits are 1's   << makes sense in IPv4. But IPv6 ?
         *   + bottom bit is 0.
         *   + 1's are on the left, 0's are on the right,
         */
        byte[] bytes = mask.toByteArray();
        final int nBytes = bytes.length;

        if (bytes[0] != FF)
            throw new IllegalArgumentException(E_INVALID +
                                            "(top byte not 255): " + mask);

        if ((bytes[nBytes-1] & 0x1) > 0)
            throw new IllegalArgumentException(E_INVALID +
                                            "(last bit not 0): " + mask);

        // find first byte that is NOT 0xff
        int idx = -1;
        // note - last byte guaranteed by above test to not be FF
        while (bytes[++idx] == FF)
            /* empty */
            ;

        // make sure this byte has 1 bits on the left and 0 bits on the right
        byte b = bytes[idx];
        if (b != 0x00 && ByteUtils.indexOf(BYTE_ME, b) < 0)
            throw new IllegalArgumentException(E_INVALID + mask);

        // finally, check that all the bytes to the right are 00.
        idx++;
        while (idx < nBytes) {
            if (bytes[idx++] != 0x00)
                throw new IllegalArgumentException(E_INVALID + mask);
        }
        return ByteUtils.countBits(bytes);
    }


    @Override
    public String toString() {
        return mask.toString();
    }

    /** Returns a string representation of this subnet mask showing the
     * internal state.
     *
     * @return a debug string representation
     */
    public String toDebugString() {
        return toString() + " (" + this.oneBitCount + " bits, " +
                mask.getFamily() + ")";
    }

    /** Returns a newly allocated byte array containing bytes that represent
     * the subnet mask. This will be an array of length 4 for an IPv4 subnet
     * mask, and an array of length 16 for an IPv6 subnet mask. Note that the
     * address bytes are in network byte order; that is, the highest order byte
     * of the address is at index 0.
     *
     * @return a byte array representing the subnet mask
     */
    public byte[] toByteArray() {
        return mask.toByteArray();
    }

    /** Returns the IP address equivalent to this subnet mask.
     *
     * @return the equivalent IP address
     */
    public IpAddress toIpAddress() {
        return mask;
    }

    /** Returns the IP address family of this subnet mask.
     *
     * @return the IP address family
     */
    public Family getFamily() {
        return mask.getFamily();
    }

    /** Returns the number of 1 bits in the mask.
     *
     * @return the number of 1 bits
     */
    public int getOneBitCount() {
        return oneBitCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final SubnetMask that = (SubnetMask) o;
        return mask.equals(that.mask);

    }

    @Override
    public int hashCode() {
        return mask.hashCode();
    }

    @Override
    public int compareTo(SubnetMask o) {
        return mask.compareTo(o.mask);
    }

    /** Applies this mask (via logical AND) to calculate and return the
     * network portion of the specified address.
     * For example:
     * <pre>
     * SubnetMask.MASK_255_255_248_0
     *           .networkPortion(IpAddress.valueOf("15.37.129.123"))
     * </pre>
     * will return the "IP address" {@code 15.37.128.0}
     *
     * @param address the address to process
     * @return an IP address representing the "network portion" of the
     *          given address
     * @throws NullPointerException if address is null
     * @throws IllegalArgumentException if there is a mismatch of IPv4/IPv6
     */
    public IpAddress networkPortion(IpAddress address) {
        validateFamily(address);
        byte[] bytes = this.toByteArray(); // start with mask
        ByteUtils.and(bytes, address.toByteArray()); // and the address
        return IpAddress.valueOf(bytes); // return the result
    }

    /** Applies this mask (via logical AND of NOT(mask) ) to calculate and
     * return the host portion of the specified address.
     * For example:
     * <pre>
     * SubnetMask.MASK_255_255_248_0
     *           .hostPortion(IpAddress.valueOf("15.37.129.123"))
     * </pre>
     * will return the "IP address" {@code 0.0.1.123}
     *
     * @param address the address to process
     * @return an IP address representing the "host portion" of the
     *          given address
     * @throws NullPointerException if address is null
     * @throws IllegalArgumentException if there is a mismatch of IPv4/IPv6
     */
    public IpAddress hostPortion(IpAddress address) {
        validateFamily(address);
        byte[] bytes = this.toByteArray(); // start with mask
        ByteUtils.not(bytes); // flip the bits
        ByteUtils.and(bytes, address.toByteArray()); // and the address
        return IpAddress.valueOf(bytes); // return the result
    }

    /** Ensures that the specified address is the same family as this mask.
     *
     * @param address the address to compare
     */
    private void validateFamily(IpAddress address) {
        if (address == null)
            throw new NullPointerException("address cannot be null");
        if (!address.getFamily().equals(this.getFamily()))
            throw new IllegalArgumentException("family mismatch: expected " +
                                               this.getFamily() +
                                               " but found " +
                                               address.getFamily());
    }

    // === PRIVATE serialization ==============================================

    //== Implementation note:
    //      We use default serialization to serialize the IpAddress mask

    private Object readResolve() throws ObjectStreamException {
        // when this is called, mask has been populated.
        // return the appropriate cached instance instead.
        Object o;
        try {
            o = valueOf(mask);
        } catch (NullPointerException e) {
            throw new InvalidObjectException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new InvalidObjectException(e.getMessage());
        }
        return o;
    }

    //=== STATIC METHODS ======================================================

    private static final WeakValueCache<String, SubnetMask> cachedMasks =
            new WeakValueCache<String, SubnetMask>(getRefQ());

    public static final SubnetMask MASK_255_0_0_0 = valueOf("255.0.0.0");
    public static final SubnetMask MASK_255_255_0_0 = valueOf("255.255.0.0");
    public static final SubnetMask MASK_255_255_248_0 = valueOf("255.255.248.0");
    public static final SubnetMask MASK_255_255_255_0 = valueOf("255.255.255.0");

    static {
        // pre-load the cache with our predefined constants...
        cachedMasks.put(MASK_255_0_0_0.toString(), MASK_255_0_0_0);
        cachedMasks.put(MASK_255_255_0_0.toString(), MASK_255_255_0_0);
        cachedMasks.put(MASK_255_255_248_0.toString(), MASK_255_255_248_0);
        cachedMasks.put(MASK_255_255_255_0.toString(), MASK_255_255_255_0);
    }

    //=== PUBLIC API ==========================================================

    /** Returns a {@code SubnetMask} object that represents the subnet mask
     * with the value of the given IP address.
     *
     * @param mask the subnet mask value expressed as an IP address
     * @return the subnet mask instance
     * @throws NullPointerException if mask is null
     * @throws IllegalArgumentException if mask is not a valid value for
     *          a subnet mask
     */
    public static SubnetMask valueOf(IpAddress mask) {
        final String key = mask.toString();
        synchronized (cachedMasks) {
            SubnetMask result = cachedMasks.get(key);
            if (result == null) {
                result = new SubnetMask(mask);
                cachedMasks.put(key, result);
            }
            return result;
        }
    }

    /** Returns a {@code SubnetMask} object that represents the subnet mask
     * defined by the specified byte array. Note that the mask bytes are
     * presumed to be in network byte order; that is, the highest order byte of
     * the mask is at index 0.
     * <p>
     * If the bytes array is of length 4, it is interpretted as an IPv4 subnet
     * mask. If the bytes array is of length 16, it is interpretted as an IPv6
     * subnet mask. Other array lengths will throw an exception.
     *
     * @param maskBytes the subnet mask bytes
     * @return the subnet mask instance
     * @throws NullPointerException if maskBytes is null
     * @throws IllegalArgumentException if maskBytes do not define
     *          a valid subnet mask
     */
    public static SubnetMask valueOf(byte[] maskBytes) {
        return valueOf(IpAddress.valueOf(maskBytes));
    }

    /** Returns a {@code SubnetMask} object that represents the value of the
     * subnet mask defined by the specified string. Both uppercase and
     * lowercase hex digits are allowed (for <b>IPv6</b> masks).
     * <p>
     * Acceptable formats are:
     * <ul>
     * <li> <b>IPv4:</b> dotted decimal format ({@code "n.n.n.n"}) --
     *      where 'n' is from 0 to 255</li>
     * <li> <b>IPv6:</b> eight groups of hexadecimal digits, separated by colon
     *      ({@code "x:x:x:x:x:x:x:x"}) -- where 'x' is from 0000 to FFFF.
     *      Leading zeros may be dropped. </li>
     * <li> <b>IPv6:</b> shortened form, i.e. a single run of zeros may be
     *      replaced with double colons
     *      (e.g. {@code "FFFF:FFFF:FFC0::"}) </li>
     * <li> <b>IPv6:</b> an address within square brackets
     *      (e.g. {@code "[FFFF:FFFF:FFC0::]"}) </li>
     * </ul>
     *
     * @param mask the string representation of the subnet mask
     * @return the subnet mask instance
     * @throws NullPointerException if mask is null
     * @throws IllegalArgumentException if mask is not an acceptable format
     */
    public static SubnetMask valueOf(String mask) {
        return valueOf(IpAddress.valueOf(mask));
    }

    /** Returns a {@code SubnetMask} object that represents the value of the
     * subnet mask implied by the given CIDR notation. For example:
     * <pre>
     * SubnetMask.fromCidr("192.168.0.0/23")
     * </pre>
     * would return the subnet mask equivalent to {@code "255.255.254.0"}
     *
     * @param cidr the CIDR notation
     * @return the appropriate subnet mask
     */
    public static SubnetMask fromCidr(String cidr) {
        return Subnet.valueOf(cidr).getMask();
    }

    // ========================================================================

    /** the minimum number of 1 bits allowed in a subnet mask */
    static final int MIN_ONE_BITS_ALLOWED = 8;

    /** the maximum number of 1 bits allowed in an IPv4 subnet mask */
    static final int MAX_ONE_BITS_IN_IPv4 = 31;

    /** the maximum number of 1 bits allowed in an IPv6 subnet mask */
    static final int MAX_ONE_BITS_IN_IPv6 = 127;


    /** Returns a {@code SubnetMask} object that represents the value of the
     * subnet mask defined by the number of 1 bits, for the given IP address
     * family.
     *
     * @param oneBitCount the number of one bits in the mask
     * @param family the IP address family for the mask
     * @return the subnet mask instance
     * @throws IllegalArgumentException if the oneBitCount is not acceptable
     */
    static SubnetMask valueOf(int oneBitCount, Family family) {
        if (oneBitCount < MIN_ONE_BITS_ALLOWED)
            throw new IllegalArgumentException("oneBitCount cannot be " +
                    "less than " + MIN_ONE_BITS_ALLOWED);
        SubnetMask mask = null;
        switch (family) {
            case IPv4:
                if (oneBitCount > MAX_ONE_BITS_IN_IPv4)
                    throw new IllegalArgumentException("oneBitCount cannot " +
                      "be more than " + MAX_ONE_BITS_IN_IPv4 + " for IPv4");
                mask = SubnetMask.valueOf(
                      createMaskArray(oneBitCount, IpAddress.IP_V4_ADDR_SIZE));
                break;
            case IPv6:
                if (oneBitCount > MAX_ONE_BITS_IN_IPv6)
                    throw new IllegalArgumentException("oneBitCount cannot " +
                      "be more than " + MAX_ONE_BITS_IN_IPv6 + " for IPv6");
                mask = SubnetMask.valueOf(
                      createMaskArray(oneBitCount, IpAddress.IP_V6_ADDR_SIZE));
                break;
        }
        return mask;
    }

    /** Creates a byte array representation of the required subnet mask.
     *
     * @param oneBitCount the number of one bits
     * @param size the size of the array
     * @return the array
     */
    private static byte[] createMaskArray(int oneBitCount, int size) {
        byte[] array = new byte[size]; // all 0 bits in every element
        final int fullBytes = oneBitCount / 8;
        final int bitsOver = oneBitCount % 8;
        for (int i=0; i<fullBytes; i++)
            array[i] = 0xff-B; // all 1 bits
        if (bitsOver > 0)
            array[fullBytes] = BYTE_ME[bitsOver-1];
        return array;
    }

}
