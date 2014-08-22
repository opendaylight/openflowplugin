/*
 * (c) Copyright 2009-2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.StringPool;
import org.opendaylight.util.cache.CacheableDataType;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.opendaylight.util.ByteUtils.hexLookupLower;
import static org.opendaylight.util.ByteUtils.hexLookupUpper;

/**
 * Represents a MAC address.
 * <p>
 * All constructors for this class are private. Creating instances
 * of {@code MacAddress} is done via the static methods on the class.
 * <p>
 * Instances of this class are immutable, making them inherently threadsafe.
 * <p>
 * This class overrides {@link #equals} and {@link #hashCode} so that instances
 * play nicely with the Collection classes.
 * <p>
 * This class implements the {@link Comparable} interface to ensure that a
 * sorted list of addresses is presented in an intuitive order.
 *
 * @author Simon Hunt
 */
public final class MacAddress extends CacheableDataType
        implements Comparable<MacAddress> {

    private static final long serialVersionUID = 5137929174003545890L;

    /** Number of bytes for a MAC address. */
    public static final int MAC_ADDR_SIZE = 6;
    /** Mask for a single byte. */
    private static final int BYTE_MASK = 0xff;
    /** Number of bits in a byte. */
    private static final int BYTE_BITS = 8;
    // masked off bottom 48 bits
    private static final long LONG_MASK = 0xffffffffffffL;

    // RegExp constants...
    private static final String PS = "^(";     // pattern start, start group
    private static final String PE = ")$";     // end group, pattern end
    static final String H2 = "[0-9A-Fa-f]{2}"; // 2 hex digits
    private static final String CD = ")[:-]("; // end group, delim, start group
    private static final String GG = ")(";     // end group, start group
    private static final String RE_1_STR = PS+H2+CD+H2+CD+H2+CD+H2+CD+H2+CD+H2+PE;
    private static final String RE_2_STR = PS+H2+GG+H2+GG+H2+CD+H2+GG+H2+GG+H2+PE;
    private static final String RE_3_STR = PS+H2+GG+H2+GG+H2+GG+H2+GG+H2+GG+H2+PE;
    private static final Pattern RE_1 = Pattern.compile(RE_1_STR);
    private static final Pattern RE_2 = Pattern.compile(RE_2_STR);
    private static final Pattern RE_3 = Pattern.compile(RE_3_STR);

    private static final String E_ADDR_FMT = "Bad MAC address format: ";
    private static final String E_INT_CODING = "Bad internal coding: ";
    private static final String E_NULL_BYTES = "bytes array cannot be null";
    private static final String E_BAD_LEN = "bad number of bytes (not 6): ";

    private static final Format DEFAULT_FORMAT = Format.MULTI_COLON;
    private static final boolean DEFAULT_TO_LOWERCASE = true;

    private static final ResourceBundle ETH_CO_LOOKUP =
        ResourceUtils.getBundledResource(MacAddress.class, "ethernetCompanies");

    private static final String UNKNOWN = ETH_CO_LOOKUP.getString("UNKNOWN");
    private static final String PRIVATE = ETH_CO_LOOKUP.getString("PRIVATE");
    private static final String EMPTY = "";
    private static final String COLON = ":";
    private static final String DASH = "-";
    private static final StringPool SP = new StringPool();

    private static final String BROADCAST_STR = "ff:ff:ff:ff:ff:ff";

    /**
     * Precomputed internal value. The MAC address stored as a long.
     * @serial long value
     */
    private final long asLong;

    /** 
     * Holds a 48-bit (6 byte) MAC address.
     * The address bytes are in network byte order: the highest order
     * byte of the address is at index 0.
     */
    private transient byte[] bytes;

    /** Holds the default string format; lazily initialized. */
    private transient String asString;

    /** Ethernet Company (cached value after lookup from properties file). */
    private transient String ethCo;


    /** 
     * Constructs a newly allocated {@code MacAddress} object that represents
     * the MAC address defined by the specified address bytes. Note that the
     * address bytes are presumed to be in network byte order; that is, the
     * highest order byte of the address is at index 0. The array length is
     * expected to be 6. An exception is thrown if it is not.
     *
     * @param bytes the address bytes
     * @param clone true of a defensive copy should be made of the given bytes
     * @throws NullPointerException if bytes is null
     * @throws IllegalArgumentException if bytes is not of length 6
     */
    private MacAddress(byte[] bytes, boolean clone) {
        if (bytes == null)
            throw new NullPointerException(E_NULL_BYTES);

        if (bytes.length != MAC_ADDR_SIZE)
            throw new IllegalArgumentException(E_BAD_LEN + bytes.length);

        this.bytes = clone ? bytes.clone() : bytes;

        asLong = computeLong(this.bytes);

        // on exiting the constructor, we must have a byte array, and there
        // should be no bits set outside the bottom 48
        assert (asLong & LONG_MASK) == asLong;
    }

    /** 
     * Constructs a newly allocated {@code MacAddress} object that represents
     * the MAC address defined by the specified string. The parsed string can
     * be any of the following formats:
     * <ul>
     * <li> "XX:XX:XX:XX:XX:XX" </li>
     * <li> "XX-XX-XX-XX-XX-XX" </li>
     * <li> "XXXXXX:XXXXXX" </li>
     * <li> "XXXXXX-XXXXXX" </li>
     * <li> "XXXXXXXXXXXX" </li>
     * </ul>
     * where each "X" represents a hex digit (upper- or lower-case).
     *
     * @param str the string representation of the MAC address
     * @throws IllegalArgumentException if address is not an acceptable format
     */
    private MacAddress(String str) {
        // find an RE that matches the input string
        Matcher m = RE_1.matcher(str);
        if (!m.matches()) {
            m = RE_2.matcher(str);
            if (!m.matches()) {
                m = RE_3.matcher(str);
                if (!m.matches())
                    throw new IllegalArgumentException(E_ADDR_FMT + str);
            }
        }
        bytes = ByteUtils.parseHex(str);

        asLong = computeLong(this.bytes);

        // on exiting the constructor, we must have a byte array, and there
        // should be no bits set outside the bottom 48
        assert bytes.length == MAC_ADDR_SIZE;
        assert (asLong & LONG_MASK) == asLong;
    }

    /** 
     * Constructs a newly allocated {@code MacAddress} object that represents
     * the MAC address defined by the encoded long.
     *
     * @param address the MAC encoded as a long
     * @throws IllegalArgumentException if address is an invalid coding
     */
    private MacAddress(long address) {
        asLong = address & LONG_MASK;
        if (asLong != address)
            throw new IllegalArgumentException(E_INT_CODING + address);

        bytes = new byte[MAC_ADDR_SIZE];
        long macAddr = asLong;
        for (int i = MAC_ADDR_SIZE - 1; i >= 0; i--) {
            bytes[i] = (byte) (macAddr & BYTE_MASK);
            macAddr >>>= BYTE_BITS;
        }

        // on exiting the constructor, we must have a byte array, and there
        // should be no bits set outside the bottom 48
        assert bytes.length == MAC_ADDR_SIZE;
        assert (asLong & LONG_MASK) == asLong;
    }

    //== Implementation note:
    //      We use default serialization to serialize the long form of the
    //      MAC address.

    private Object readResolve() throws ObjectStreamException {
        // when this is called, asLong has been populated.
        // return a new instance with all derived fields populated.
        Object o;
        try {
            o = valueOf(asLong);
        } catch (IllegalArgumentException e) {
            throw new InvalidObjectException(e.getMessage());
        }
        return o;
    }

    /** 
     * Computes the long value from an array of (6) bytes.
     *
     * @param bytes the array of bytes
     * @return the long value
     */
    private long computeLong(byte[] bytes) {
        long macLong = 0;
        for (int i = 0; i < MAC_ADDR_SIZE; i++) {
            macLong <<= BYTE_BITS;
            macLong |= bytes[i] & BYTE_MASK;
        }
        return macLong;
    }


    /** 
     * Returns the ethernet company name that this MAC address is associated
     * with. This is done by looking at the top 3 bytes of the address and
     * returning the name of the company that has been assigned that byte
     * pattern.
     * <p>
     * A value of "(Unknown)" will be returned if no known mapping exists.
     *
     * @return the ethernet company name
     */
    public synchronized String getEthernetCompany() {
        if (ethCo == null) {
            String key = ByteUtils.hex(bytes, 0, 3);
            // pull the string out of the resource bundle and cache
            // note that the resource bundle uses lowercase hex digits:
            //   xxxxxx = Company Name
            try {
                String name = ETH_CO_LOOKUP.getString(key);
                ethCo = EMPTY.equals(name) ? PRIVATE : SP.get(name);
            } catch (MissingResourceException e) {
                ethCo = UNKNOWN;
            }
        }
        return ethCo;
    }

    /** 
     * Returns a string representation of this MAC address 
     * in "XX:XX:XX:XX:XX:XX" form.
     * <p>
     * This is the same result as invoking
     * {@code macAddr.toFormattedString(MacAddress.Format.MULTI_COLON)}.
     *
     * @return the MAC address as a string
     */
    @Override
    public String toString() {
        if (asString == null)
            asString = toFormattedString(DEFAULT_FORMAT, DEFAULT_TO_LOWERCASE);
        return asString;
    }

    /** 
     * Returns a string representation of this MAC address, using the
     * specified {@link Format}.
     * <ul>
     * <li> Format.MULTI_COLON => "XX:XX:XX:XX:XX:XX" </li>
     * <li> Format.MULTI_DASH => "XX-XX-XX-XX-XX-XX" </li>
     * <li> Format.SINGLE_COLON => "XXXXXX:XXXXXX" </li>
     * <li> Format.SINGLE_DASH => "XXXXXX-XXXXXX" </li>
     * <li> Format.NO_DELIMITER => "XXXXXXXXXXXX" </li>
     * </ul>
     * where each "X" is a hex digit
     *
     * @param fmt the format to use
     * @return the formatted address
     */
    public String toFormattedString(Format fmt) {
        return toFormattedString(fmt, DEFAULT_TO_LOWERCASE);
    }

    /** 
     * Returns a string representation of this MAC address, using the
     * specified {@link Format}.
     * <ul>
     * <li> Format.MULTI_COLON => "XX:XX:XX:XX:XX:XX" </li>
     * <li> Format.MULTI_DASH => "XX-XX-XX-XX-XX-XX" </li>
     * <li> Format.SINGLE_COLON => "XXXXXX:XXXXXX" </li>
     * <li> Format.SINGLE_DASH => "XXXXXX-XXXXXX" </li>
     * <li> Format.NO_DELIMITER => "XXXXXXXXXXXX" </li>
     * </ul>
     * where each "X" is a hex digit
     *
     * @param fmt the format to use
     * @param toLowerCase if true, hex digits will be lowercase
     * @return the formatted address
     */
    public String toFormattedString(Format fmt, boolean toLowerCase) {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (byte b: bytes) {
            count++;
            sb.append(toLowerCase ? hexLookupLower(b) : hexLookupUpper(b));
            if (fmt.delimited && (count == 3 || (fmt.full && count < 6)))
                sb.append(fmt.delimStr);
        }
        return sb.toString();
    }

    /** 
     * Returns true if this is the broadcast address (FF:FF:FF:FF:FF:FF).
     *
     * @return true if this is the broadcast address
     */
    public boolean isBroadcast() {
        return this.equals(BROADCAST);
    }

    /** 
     * Returns true if this is a multicast address. That is, if the least
     * significant bit of the most significant byte is 1.
     *
     * @return true if this is a multicast address
     */
    public boolean isMulticast() {
        return (bytes[0] & 0x1) != 0;
    }

    // These are the long equivalents of the link local addresses:
    // 01:80:c2:00:00:0e => 1652522221582
    // 01:80:c2:00:00:03 => 1652522221571
    // 01:80:c2:00:00:00 => 1652522221568
    private static final long LINK_LOCAL_0E_LONG = 1652522221582L;
    private static final long LINK_LOCAL_03_LONG = 1652522221571L;
    private static final long LINK_LOCAL_00_LONG = 1652522221568L;

    /**
     * Returns true if this is one of the "Link Local" addresses. That is, if
     * the address is one of the following: 
     * <ul>
     *     <li> 01:80:c2:00:00:0e </li>
     *     <li> 01:80:c2:00:00:03 </li>
     *     <li> 01:80:c2:00:00:00 </li>
     * </ul>
     * <p>
     * cf. Link Layer Discovery Protocol (LLDP)    
     * 
     * @return true if this is a link local address
     */
    public boolean isLinkLocal() {
        return (asLong == LINK_LOCAL_0E_LONG || 
                asLong == LINK_LOCAL_03_LONG || 
                asLong == LINK_LOCAL_00_LONG);
    }

    /** 
     * Returns true if this is a VMAC used by VRRP. That is, if the 
     * the address has MSB 5 Bytes as 00-00-5E-00-01
     *
     * @return true if this is a VMAC address used by VRRP
     */
    public boolean isVrrpVmac() {
        return (bytes[0] == 0x0 && bytes[1] == 0x0 && bytes[2] == 0x5e &&
                bytes[3] == 0x0 && bytes[4] == 0x1);
    }

    /** 
     * Returns a newly allocated byte array containing bytes that represent
     * the MAC address. Note that the address bytes are in network byte order;
     * that is, the highest order byte of the address is at index 0.
     *
     * @return a byte array representing the MAC address
     */
    public byte[] toByteArray() {
        return bytes.clone();
    }

    /** 
     * Writes the bytes that represent the MAC address directly into
     * {@link ByteBuffer} at the buffer's current position. Note that the
     * address bytes are in network byte order; that is, the highest order byte
     * of the address is at index 0.
     *
     * @param b the byte buffer to write into 
     */
    public void intoBuffer(ByteBuffer b) {
        b.put(bytes);
    }   

    /** 
     * Returns the MAC address represented as a long.
     * <p>
     * This method is provided to support the encoding of MAC addresses
     * as long values.
     *
     * @return the MAC encoded as a long
     */
    public long toLong() {
        return asLong;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return asLong == ((MacAddress) o).asLong;
    }

    @Override
    public int hashCode() {
        return ((Long) asLong).hashCode();
    }

    /** 
     * Implements the Comparable interface, to return addresses in
     * natural order.
     *
     * @param o the other MAC address
     * @return an integer value indicating the relative ordering
     * @see Comparable#compareTo
     */
    @Override
    public int compareTo(MacAddress o) {
        return Long.signum(asLong - o.asLong);
    }


    /** 
     * Designates the different MAC address string formats available.
     *  @see MacAddress#toFormattedString
     */
    public static enum Format {
        MULTI_COLON  ( true, COLON, true ),
        MULTI_DASH   ( true, DASH, true ),
        SINGLE_COLON ( true, COLON, false ),
        SINGLE_DASH  ( true, DASH, false),
        NO_DELIMITER ( false, EMPTY, false ),
        ;

        private final boolean delimited;
        private final String delimStr;
        private final boolean full;

        private Format(boolean delimited, String delimStr, boolean full) {
            this.delimited = delimited;
            this.delimStr = delimStr;
            this.full = full;
        }
    }


    /** The broadcast address, that is ff:ff:ff:ff:ff:ff. */
    public static final MacAddress BROADCAST = valueOf(BROADCAST_STR);
    
    /** The link local address ending in 0E, that is 01:80:c2:00:00:0e. */
    public static final MacAddress LINK_LOCAL_0E = mac(LINK_LOCAL_0E_LONG);

    /** The link local address ending in 03, that is 01:80:c2:00:00:03. */
    public static final MacAddress LINK_LOCAL_03 = mac(LINK_LOCAL_03_LONG);

    /** The link local address ending in 00, that is 01:80:c2:00:00:00. */
    public static final MacAddress LINK_LOCAL_00 = mac(LINK_LOCAL_00_LONG);

    /** 
     * Returns an object that represents the value of the MAC address defined
     * by the specified address bytes. Note that the address bytes are presumed
     * to be in network byte order; that is, the highest order byte of the
     * address is at index 0. The array length is expected to be 6.
     * An exception is thrown if it is not.
     *
     * @param bytes the MAC address bytes
     * @return a MacAddress instance
     * @throws NullPointerException if the parameter is null
     * @throws IllegalArgumentException if bytes length is not 6
     */
    public static MacAddress valueOf(byte[] bytes) {
        return new MacAddress(bytes, true);
    }

    /**
     * Convenience method that simply delegates to {@link #valueOf(byte[])}.
     *
     * @param bytes the MAC address bytes
     * @return a MacAddress instance
     * @throws NullPointerException if the parameter is null
     * @throws IllegalArgumentException if bytes length is not 6
     */
    public static MacAddress mac(byte[] bytes) {
        return valueOf(bytes);
    }
    
    /** 
     * Returns an object that represents the value of the MAC address defined
     * by the specified string. Both uppercase and lowercase hex digits are
     * allowed.
     * <p>
     * Acceptable input formats are:
     * <ul>
     * <li> "XX:XX:XX:XX:XX:XX" </li>
     * <li> "XX-XX-XX-XX-XX-XX" </li>
     * <li> "XXXXXX:XXXXXX" </li>
     * <li> "XXXXXX-XXXXXX" </li>
     * <li> "XXXXXXXXXXXX" </li>
     * </ul>
     * where each "X" represents a hex digit
     *
     * @param address the string representation of the MAC address
     * @return a MacAddress instance
     * @throws NullPointerException if address is null
     * @throws IllegalArgumentException if address is not an acceptable format
     */
    public static MacAddress valueOf(String address) {
        return new MacAddress(address.trim().toUpperCase(Locale.getDefault()));
    }

    /**
     * Convenience method that simply delegates to {@link #valueOf(String)}.
     *
     * @param address the string representation of the MAC address
     * @return a MacAddress instance
     * @throws NullPointerException if address is null
     * @throws IllegalArgumentException if address is not an acceptable format
     */
    public static MacAddress mac(String address) {
        return valueOf(address);
    }

    /** Returns an object that represents the value of the MAC address encoded
     * by the specified long value.
     *
     * @param encodedAddress the encoded MAC address
     * @return a MacAddress instance
     * @throws IllegalArgumentException if the specified long is not a valid
     *          encodement
     */
    public static MacAddress valueOf(long encodedAddress) {
        return new MacAddress(encodedAddress);
    }

    /**
     * Convenience method that simply delegates to {@link #valueOf(long)}.
     *
     * @param encodedAddress the encoded MAC address
     * @return a MacAddress instance
     * @throws IllegalArgumentException if the specified long is not a valid
     *          encodement
     */
    public static MacAddress mac(long encodedAddress) {
        return valueOf(encodedAddress);
    }

    /**
     * Reads 6 bytes from the specified byte buffer and creates a MAC address
     * entity.
     *
     * @param buffer byte buffer from which to read the bytes
     * @return a MacAddress instance
     * @throws BufferUnderflowException if the buffer does not have 6 bytes
     *         remaining
     */
    public static MacAddress valueFrom(ByteBuffer buffer) {
        byte bytes[] = new byte[MAC_ADDR_SIZE];
        buffer.get(bytes);
        return new MacAddress(bytes, false);
    }

}
