/*
 * (c) Copyright 2009-2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.StringPool;
import org.opendaylight.util.cache.CacheableDataType;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an IP address (either IPv4 or IPv6).
 * <p>
 * Useful predicates allow the consumer to test for properties of the
 * represented IP address; for example, {@link #isLoopback()}.
 * <p>
 * All constructors for this class are private. Creating instances of
 * {@code IpAddress} is done via the static methods on the class.
 * <p>
 * Instances of this class are immutable, making them inherently threadsafe.
 * <p>
 * This class overrides {@link #equals} and {@link #hashCode} so that instances
 * play nicely with the Collection classes.
 * <p>
 * This class implements the {@link Comparable} interface to ensure that
 * a sorted list of addresses is presented in an intuitive order.
 *
 * @author Simon Hunt
 * @author Frank Wood
 * @author Steve Britt
 */
public final class IpAddress extends CacheableDataType
                             implements Comparable<IpAddress> {

    private static final long serialVersionUID = 3188998696969254695L;

    /** Number of bytes for IPv4 address. */
    public static final int IP_V4_ADDR_SIZE = 4;

    /** Number of bytes for IPv6 address. */
    public static final int IP_V6_ADDR_SIZE = 16;

    /** The field delimiter in IPv4 addresses (i.e.&nbsp;a dot). */
    public static final String DELIMITER_V4 = ".";
    /** The field delimiter in IPv6 addresses (i.e.&nbsp;a colon). */
    public static final String DELIMITER_V6 = ":";

    private static final String DELIM_V4_ESCAPED = "\\.";
    private static final Pattern DELIM_V4_PATTERN =
            Pattern.compile(DELIM_V4_ESCAPED);
    private static final Pattern DELIM_V6_PATTERN =
            Pattern.compile(DELIMITER_V6);

    private static final String EMPTY_STRING = "";
    private static final String ZERO = "0";

    // used to help in expressing bytes (with sign bit set) concisely
    //  e.g. you can say   0xff-Z   instead of casting with   (byte) 0xff
    private static final int Z = 256;
    private static final int MAX_PORT = 65535;
    private static final int NO_PORT = -1;

    private static final String BAD_ADDRESS_FORMAT = "Bad address format: ";
    private static final String BAD_PORT_NUMBER = "Bad port number: ";

    /** RegExp pattern that matches "[...]" or "[...]:nnn" */
    private static final String IPv6_PORT_STR = "^\\[(.*)\\](:(\\d+))?$";
    private static final Pattern IPv6_PORT_PATTERN =
            Pattern.compile(IPv6_PORT_STR);

    /** RegExp pattern that matches "n.n.n.n:p" */
    private static final String IPv4_PORT_STR =
            "^(\\d+\\.\\d+\\.\\d+\\.\\d+):(\\d+)$";
    private static final Pattern IPv4_PORT_PATTERN =
            Pattern.compile(IPv4_PORT_STR);

    private static final String IPv4_LOOPBACK_STR = "127.0.0.1";
    private static final String IPv6_LOOPBACK_STR = "::1";

    private static final String IPv4_UNDETERMINED_STR = "0.0.0.0";
    private static final String IPv6_UNDETERMINED_STR = "::";

    private static final String IPv4_BROADCAST_STR = "255.255.255.255";


    private static final StringPool SP = new StringPool();
    private static final ResourceBundle MULTICAST_LOOKUP =
            ResourceUtils.getBundledResource(IpAddress.class, "ipMulticast");
    private static final String MULTICAST_UNKNOWN =
            MULTICAST_LOOKUP.getString("UNKNOWN");

    /** The address family. */
    private transient Family family;

    /** The address class (lazily loaded). */
    private transient AddressClass addressClass;

    /** 
     * Holds either a 32-bit (4 byte) IPv4 address or a 128-bit (16 byte)
     * IPv6 address. The address bytes are in network byte order: the highest
     * order byte of the address is at index 0.
     * @serial address bytes
     */
    private byte[] bytes;

    /** 
     * Precomputed internal values for comparison of IPv6 addresses.
     * Highest order value is at index 0.
     */
    private transient long[] quarters;

    /** 
     * Internal representation of IPv6 address broken up into 8 pieces.
     * Temporary storage.
     */
    private transient int[] tmpEighths;

    /** 
     * Precomputed value set true if any of the top 3 quarters of an
     * IPv6 address are greater than 0.
     */
    private transient boolean higherOrderBitsSet = false;

    /** 
     * Precomputed internal value: IPv4 as a long;
     * IPv6 low order (copy of quarters[3]).
     */
    private transient long lowOrder;

    /** Precomputed string representation. */
    private transient String asString;

    /** Precomputed short representation (for IPv6). */
    private transient String asShortString;

    /** Precomputed full string representation (for IPv6). */
    private transient String asFullString;

    /** We are going to cache the hash code. */
    private transient int cachedHashCode;

    /** flag set true if this is a link-local address. */
    private transient boolean linkLocal;


    /** 
     * Constructs a newly allocated {@code IpAddress} object that represents
     * the IP address defined by the specified address bytes. Note that the
     * address bytes are presumed to be in network byte order; that is, the
     * highest order byte of the address is at index 0.
     * <p>
     * If the bytes array is of length 4, it is interpreted as an IPv4 address.
     * If the bytes array is of length 16, it is interpreted as an IPv6 address.
     * Other array lengths will throw an exception.
     *
     * @param bytes the address bytes
     * @param clone true of a defensive copy should be made of the given bytes
     * @throws NullPointerException if bytes is null
     * @throws IllegalArgumentException if bytes is an unsupported length
     */
    private IpAddress(byte[] bytes, boolean clone) {
        if (bytes == null)
            throw new NullPointerException("bytes array cannot be null");
        if (bytes.length == IP_V4_ADDR_SIZE) {
            init(clone ? bytes.clone() : bytes, Family.IPv4);
        } else if (bytes.length == IP_V6_ADDR_SIZE) {
            init(clone ? bytes.clone() : bytes, Family.IPv6);
        } else {
            throw new IllegalArgumentException("Unsupported number of bytes (" +
                                                bytes.length + ")");
        }
        // on exiting the constructor, we must have a byte array
        assert (family == Family.IPv4 && bytes.length == IP_V4_ADDR_SIZE) ||
                (family == Family.IPv6 && bytes.length == IP_V6_ADDR_SIZE);
        furtherValidation();
    }

    /** 
     * Constructs a newly allocated {@code IpAddress} object that represents
     * the IP address defined by the specified string.
     * <p>
     * Acceptable formats are:
     * <ul>
     * <li> <b>IPv4:</b> dotted decimal format ({@code "n.n.n.n"}) </li>
     * <li> <b>IPv6:</b> eight groups of four hexadecimal digits, separated
     *      by colon ({@code "x:x:x:x:x:x:x:x"}). Leading zeros may be
     *      dropped. </li>
     * <li> <b>IPv6:</b> shortened form, i.e. a single run of zeros replaced
     *      with double colons (e.g. {@code "FF:BAD::3"}) </li>
     * </ul>
     *
     * @param str the string representation of the IP address
     * @throws NullPointerException if str is null
     * @throws IllegalArgumentException if str is not an acceptable format
     */
    private IpAddress(String str) {
        if (str == null)
            throw new NullPointerException("String cannot be null");
        String s = str.trim(); // start by trimming any whitespace

        if (s.contains(DELIMITER_V4)) {
            parseIPv4(s);
        } else if (s.contains(DELIMITER_V6)) {
            // a string such as "1:2:3:4" will slip through the initial checks,
            // so we just catch out of bounds and re-throw
            try {
                parseIPv6(s);
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalArgumentException(BAD_ADDRESS_FORMAT + s);
            }
        } else {
            throw new IllegalArgumentException(BAD_ADDRESS_FORMAT + s);
        }
        // on exiting the constructor, we must have a byte array
        assert (family == Family.IPv4 && bytes.length == IP_V4_ADDR_SIZE) ||
                (family == Family.IPv6 && bytes.length == IP_V6_ADDR_SIZE);
        furtherValidation();
    }

    /** 
     * Allows us to make further tests on the address, and possibly set
     * flags (such as linkLocal).
     */
    private void furtherValidation() {
        checkForLinkLocal();
    }


    // see RFC-3927 section 2.1  ---  169.254.*.*
    private static final byte[] IPv4_LINK_LOCAL_VALUE = { 0xa9-Z, 0xfe-Z };

    // see RFC-4291 section 2.5.6  ---  fe80::*:*:*:*
    // mask is top 10 bits (of the top 4 bytes, i.e. a single quarter)
    private static final long IPv6_LINK_LOCAL_MASK = 0xffc00000L;
    private static final long IPv6_LINK_LOCAL_TAG  = 0xfe800000L;

    private void checkForLinkLocal() {
        if (this.family == Family.IPv4) {
            // See RFC-3927 section 2.1
            linkLocal = bytes[0] == IPv4_LINK_LOCAL_VALUE[0] &&
                        bytes[1] == IPv4_LINK_LOCAL_VALUE[1];

        } else {
            // IPv6
            /* From RFC-4291
               2.5.6.  Link-Local IPv6 Unicast Addresses

               Link-Local addresses are for use on a single link.  Link-Local
               addresses have the following format:

               |   10     |
               |  bits    |         54 bits     |        64 bits           |
               +----------+---------------------+--------------------------+
               |1111111010|           0         |     interface ID         |
               +----------+---------------------+--------------------------+

               Link-Local addresses are designed to be used for addressing on a
               single link for purposes such as automatic address configuration,
               neighbor discovery, or when no routers are present.

               Routers must not forward any packets with Link-Local source or
               destination addresses to other links.
             */

            // First verify that top 10 bits contain: "fe80::/10"
            long topQuarter = quarters[0];
            long nextQuarter = quarters[1];

            linkLocal = (topQuarter & IPv6_LINK_LOCAL_MASK) == IPv6_LINK_LOCAL_TAG;
            if (linkLocal) {
                // verify that no bits are set in the 54 bit reserved region
                long topReservedBits = topQuarter & ~IPv6_LINK_LOCAL_MASK;
                if (topReservedBits != 0 || nextQuarter != 0)
                    throw new IllegalArgumentException(E_BAD_IPv6_LINK_LOCAL);
            }
        }
    }

    private static final String E_BAD_IPv6_LINK_LOCAL =
            "Illegally formed IPv6 Link-Local address";

    /** 
     * Attempts to parse the string as an IPv4 address.
     *
     * @param str the string
     * @throws IllegalArgumentException if str is not a valid IPv4 address
     */
    private void parseIPv4(String str) {
        String[] strBytes = DELIM_V4_PATTERN.split(str);
        if (strBytes.length != 4)
            throw new IllegalArgumentException(BAD_ADDRESS_FORMAT + str);
        byte[] addressBytes = new byte[4];
        boolean ok = true;  // innocent until proven guilty
        try {
            for (int i=0; i<4; i++) {
                int b = Integer.parseInt(strBytes[i]);
                if (b>=0 && b<=255) {
                    addressBytes[i] = (byte) (b>127 ? b-Z : b);
                } else {
                    ok = false;
                    break;
                }

            }
        } catch (NumberFormatException e) {
            ok = false;
        }
        if (!ok)
            throw new IllegalArgumentException(BAD_ADDRESS_FORMAT + str);

        init(addressBytes, Family.IPv4);
    }


    /** 
     * Attempt to parse the string as an IPv6 address.
     * Both full and shortened notations are acceptable.
     *
     * @param str the string
     * @throws IllegalArgumentException if str is not a valid IPv6 address
     */
    private void parseIPv6(String str) {
        String[] strHex = DELIM_V6_PATTERN.split(str, -1);
        final int n = strHex.length;
        // check for too many or too few elements ("::" will give 3 elements)
        if (n < 3 || n > 8)
            throw new IllegalArgumentException(BAD_ADDRESS_FORMAT + str);

        // if first/last element is "", second/second-to-last element MUST be ""
        if ( (EMPTY_STRING.equals(strHex[0]) &&
                !EMPTY_STRING.equals(strHex[1])) ||
             (EMPTY_STRING.equals(strHex[n-1]) &&
                !EMPTY_STRING.equals(strHex[n-2])) ) {
            throw new IllegalArgumentException(BAD_ADDRESS_FORMAT + str);
        }
        // replace leading and trailing empty strings with a "0", so we don't
        // have to test later
        if (EMPTY_STRING.equals(strHex[0]))
            strHex[0] = ZERO;
        if (EMPTY_STRING.equals(strHex[n-1]))
            strHex[n-1] = ZERO;

        // find the element that represents the double colon (if any)
        int dc = -1;
        for (int i=1; i<n-1; i++) {
            if (EMPTY_STRING.equals(strHex[i])) {
                if (dc == -1) {
                    dc = i;
                    // do NOT break here.. we need to verify there are no
                    // more double-colons
                } else {
                    // a second double-colon is not allowed
                    throw new IllegalArgumentException(BAD_ADDRESS_FORMAT + str);
                }
            }
        }

        // now fill out an array of 8 ints by parsing the elements
        tmpEighths = new int[8];
        boolean ok = true;

        if (dc == -1) {
            // no double-colon: just a straight run
            try {
                for (int i=0; i<8; i++) {
                    int x = Integer.parseInt(strHex[i], 16);
                    if (x >= 0 && x <= 0xFFFF) {
                        tmpEighths[i] = x;
                    } else {
                        ok = false;
                        break;
                    }
                }
            } catch (NumberFormatException e) {
                ok = false;
            }

        } else {

            // we have a double colon, so fill the array from both ends
            try {
                int i = 0;
                while (i<dc) {
                    int x = Integer.parseInt(strHex[i],16);
                    if (x>=0 && x<=0xFFFF) {
                        tmpEighths[i++] = x;
                    } else {
                        ok = false;
                        break;
                    }
                }
                if (ok) {
                    i = n-1;
                    int j = 7;
                    while (i>dc) {
                        int x = Integer.parseInt(strHex[i--],16);
                        if (x>=0 && x<=0xFFFF) {
                            tmpEighths[j--] = x;
                        } else {
                            ok = false;
                            break;
                        }
                    }
                }
            } catch (NumberFormatException e) {
                ok = false;
            }
        }
        if (!ok)
            throw new IllegalArgumentException(BAD_ADDRESS_FORMAT + str);

        // create the byte array from the int array
        bytes = new byte[IP_V6_ADDR_SIZE];
        for (int i=0; i<IP_V6_ADDR_SIZE; i+=2) {
            int j = i/2;
            int b = (tmpEighths[j] >>> 8) & 0xFF;
            bytes[i] = (byte) (b>127 ? b-Z : b);
            b = tmpEighths[j] & 0xFF;
            bytes[i+1] = (byte) (b>127 ? b-Z : b);
        }

        // finally, create the other internal values and release
        // the temporary storage
        family = Family.IPv6;
        createQuarters();
        cacheHashCode();
        tmpEighths = null;
    }


    /** 
     * Pre-computes useful values. This method will only be called if the
     * byte array holds a valid number of bytes (4 or 16).
     *
     * @param bytes the address bytes
     * @param family the address family : IPv4 or IPv6
     */
    private void init(byte[] bytes, Family family) {
        this.bytes = bytes;
        this.family = family;

        // pre-compute internal representations
        createQuarters();
        cacheHashCode();
        tmpEighths = null; // discard temp data
    }

    /** 
     * Creates the lowOrder representation of the address, and
     * creates the 'quarters' representation for IPv6. This representation
     * is used in the {@link #compareTo} implementation.
     */
    private void createQuarters() {
        if (family == Family.IPv6)
            quarters = new long[4];

        int offset = 0;
        int i6 = 0;
        while (offset < bytes.length) {
            lowOrder = bytes[offset+3] & 0xFFL;
            lowOrder |= ((bytes[offset+2] << 8)  & 0xFF00L);
            lowOrder |= ((bytes[offset+1] << 16) & 0xFF0000L);
            lowOrder |= ((bytes[offset]   << 24) & 0xFF000000L);
            if (family == Family.IPv6)
                quarters[i6++] = lowOrder;
            offset += 4;
        }
        if (family == Family.IPv6)
            higherOrderBitsSet = (quarters[0] | quarters[1] | quarters[2]) > 0;
    }

    /** 
     * Creates the eighths array from the byte array. Used only
     * for IPv6 addresses, as an aid to creating the string representation.
     */
    private void createEighths() {
        if (family == Family.IPv6) {
            tmpEighths = new int[IP_V6_ADDR_SIZE/2];
            for (int i=0, j=0; i<IP_V6_ADDR_SIZE; i+=2) {
                int x = bytes[i] << 8 & 0xFF00;
                x |= bytes[i+1] & 0xFF;
                tmpEighths[j++] = x;
            }
        }
    }

    /** 
     * Locates a run of zeros in an IPv6 address, and creates a shortened
     * form of the address if such a run exists. If no such run is found,
     * {@link #asShortString} is left as null.
     */
    private void findRunOfZeros() {
        int longestIndex = -1;
        int longestRun = 0;
        int currentIndex = -1;
        int currentRun = 0;
        boolean zeros = false;

        for (int i=0; i<tmpEighths.length; i++) {
            if (tmpEighths[i] == 0) {
                if (!zeros) {
                    currentIndex = i;
                    currentRun = 1;
                    zeros = true;
                } else {
                    currentRun++;
                }
            } else {
                if (zeros) {  // end of a run of zeros
                    zeros = false;
                    if (currentRun > longestRun) {
                        longestRun = currentRun;
                        longestIndex = currentIndex;
                    }
                }
            }
        }

        // don't forget store results in longest* vars, if last eighth is zero
        if (zeros) {
            if (currentRun > longestRun) {
                longestRun = currentRun;
                longestIndex = currentIndex;
            }
        }

        // did we locate a run of zeros?
        if (longestIndex > -1) {
            StringBuilder sb = new StringBuilder();
            int i=0;
            while (i<longestIndex)
                sb.append(Integer.toHexString(tmpEighths[i++])
                        .toUpperCase(Locale.getDefault()))
                        .append(DELIMITER_V6);

            if (sb.length() == 0)
                sb.append(DELIMITER_V6);

            sb.append(DELIMITER_V6);
            i += longestRun;
            while (i<tmpEighths.length) {
                sb.append(Integer.toHexString(tmpEighths[i++])
                                .toUpperCase(Locale.getDefault()));
                if (i<tmpEighths.length)
                    sb.append(DELIMITER_V6);
            }
            asShortString = sb.toString();
        }
    }

    /** Caches the string representation. */
    private synchronized void cacheStrings() {
        StringBuilder sb = new StringBuilder();
        if (family == Family.IPv4) {
            for (byte b: bytes) {
                if (sb.length() > 0)
                    sb.append(DELIMITER_V4);
                sb.append(b<0 ? b+Z : b);
            }

        } else { // IPv6
            createEighths();
            for (int eighth : tmpEighths) {
                if (sb.length() > 0)
                    sb.append(DELIMITER_V6);
                sb.append(Integer.toHexString(eighth)
                                .toUpperCase(Locale.getDefault()));
            }
            findRunOfZeros();
            tmpEighths = null;
        }
        asString = sb.toString();
    }

    /** Caches the hash code. Since instances of this class are immutable
     * we can pre-compute this value and cache it.
     */
    private void cacheHashCode() {
        cachedHashCode = family.hashCode();
        cachedHashCode = 31 * cachedHashCode + Arrays.hashCode(bytes);
    }

    // === PRIVATE serialization ==============================================

    //== Implementation note:
    //      We use default serialization to serialize the byte[] form of the
    //      ip address.

    private Object readResolve() throws ObjectStreamException {
        // when this is called, bytes has been populated.
        // return a new instance with all derived fields populated.
        Object o;
        try {
            o = valueOf(bytes);
        } catch (NullPointerException e) {
            throw new InvalidObjectException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new InvalidObjectException(e.getMessage());
        }
        return o;
    }

    //=== Public API ==========================================================

    @Override
    public String toString() {
        if (asString == null)
            cacheStrings();
        return asString;
    }

    /** 
     * If IPv6, returns the shortened form of this address.
     *  If IPv4, returns the same as {@link #toString}.
     *
     * @return the shortened form
     */
    public String toShortString() {
        if (asString == null)
            cacheStrings();
        return asShortString != null ? asShortString : asString;
    }

    /** 
     * If IPv6, returns the address with all fields zero-filled.
     *  If IPv4, returns the same as {@link #toString()}.
     *
     * @return the fully written string
     */
    public String toFullString() {
        if (family == Family.IPv4) return toString();

        // We lazy initialize the zero-padded form, under the assumption that
        // it is not used as often as the other string forms.
        synchronized (this) {
            if (asFullString == null) {
                createEighths(); // values between colons
                StringBuilder sb = new StringBuilder(40);
                for (int eighth: tmpEighths) {
                    if (sb.length() > 0) {
                        sb.append(DELIMITER_V6);
                    }
                    sb.append(String.format("%04X", eighth));
                }
                asFullString = sb.toString();
                tmpEighths = null; // don't need these any more
            }
            return asFullString;
        }
    }

    /** 
     * Returns this IP address as a string that includes the specified port
     * number. In the case of an IPv6 address, square brackets are included.
     * <p>
     * Examples:
     * <ul>
     * <li> "{@code 15.43.37.2:8080}"
     * <li> "{@code [FEDC:0:1:0:0:0:AB:23]:8080}"
     * </ul>
     *
     * @param portNumber the port number to append
     * @return the ip address and port number
     * @throws IllegalArgumentException if portNumber is &lt; 0 or &gt 65535
     */
    public String toStringWithPort(int portNumber) {
        int port = verifyPort(portNumber);
        StringBuilder sb = new StringBuilder(toString());
        if (family == Family.IPv6) {
            sb.insert(0, "[");
            sb.append("]");
        }
        sb.append(":").append(port);
        return sb.toString();
    }

    /** 
     * Returns a newly allocated byte array containing bytes that represent
     * the IP address. This will be an array of length 4 for an IPv4 address,
     * or an array of length 16 for an IPv6 address. Note that the address
     * bytes are in network byte order; that is, the highest order byte of the
     * address is at index 0.
     *
     * @return a byte array representing the IP address
     */
    public byte[] toByteArray() {
        return bytes.clone();
    }

    /** 
     * Writes the bytes that represent the IP address directly into
     * {@link ByteBuffer} at the buffer's current position. This will be an
     * array of length 4 for an IPv4 address, or an array of length 16 for an
     * IPv6 address. Note that the address bytes are in network byte order;
     * that is, the highest order byte of the address is at index 0.
     *
     * @param b the byte buffer to write into
     */
    public void intoBuffer(ByteBuffer b) {
        b.put(bytes);
    }

    /** 
     * Returns an InetAddress instance equivalent to this IP address.
     * This method doesn't block, i.e.&nbsp;no reverse name service lookup
     * is performed.
     *
     * @return the InetAddress for this IpAddress instance
     * @see InetAddress#getByAddress
     */
    public InetAddress toInetAddress() {
        try {
            return InetAddress.getByAddress(bytes);
        } catch (UnknownHostException e) {
            // If IP address is of illegal length which is protected
            // by this class.
            throw new IllegalStateException(e);
        }
    }

    /** 
     * Returns the family this address belongs to.
     *
     * @return IPv4 or IPv6
     */
    public Family getFamily() {
        return family;
    }

    /** 
     * Returns the address class.
     *
     * @return the address class
     */
    public synchronized AddressClass getAddressClass() {
        if (addressClass == null) {
            if (family == Family.IPv6) {
                addressClass = AddressClass.CLASSLESS;
            } else {
                int topByte = bytes[0];
                if ((topByte & 0x80) == 0) {            // 0xxx xxxx
                    addressClass = AddressClass.A;
                } else if ((topByte & 0xC0) == 0x80) {  // 10xx xxxx
                    addressClass = AddressClass.B;
                } else if ((topByte & 0xE0) == 0xC0) {  // 110x xxxx
                    addressClass = AddressClass.C;
                } else if ((topByte & 0xF0) == 0xE0) {  // 1110 xxxx
                    addressClass = AddressClass.D;
                } else if ((topByte & 0xF0) == 0xF0) {  // 1111 xxxx
                    addressClass = AddressClass.E;
                } else {
                    addressClass = AddressClass.CLASSLESS;
                }
            }
        }
        return addressClass;
    }

    /** 
     * Returns a friendly name for this IP address, if it is a multicast
     * address, and if the name exists. If this is not a multicast address,
     * null is returned.
     * <p>
     * A value of "(Unknown)" will be returned if no known mapping exists.
     *
     * @return the multicast address friendly name; or null if this is
     *          not a multicast address
     */
    public String getMulticastName() {
        if (isMulticast()) {
            try {
                String multicastName = MULTICAST_LOOKUP.getString(toString());
                return SP.get(multicastName); // reference same string instance
            } catch (MissingResourceException e) {
                return MULTICAST_UNKNOWN;
            }
        }
        return null;
    }

    /** 
     * Returns true if the address represented is a "multicast" address.
     * {@code 224.0.0.0} - {@code 239.255.255.255} for IPv4 (Address class D).
     * <p>
     * TODO - include logic for IPv6
     *
     * @return true if this is a multicast address
     */
    public boolean isMulticast() {
        return getAddressClass().equals(AddressClass.D);
    }

    /** 
     * Returns true if the address represented is a "broadcast" address.
     * {@code 255.255.255.255} for IPv4.
     * <p>
     * TODO - include logic for IPv6
     *
     * @return true if this is a broadcast address
     */
    public boolean isBroadcast() {
        return this.equals(BROADCAST_IPv4);
    }

    /** 
     * Returns true if the address is "reserved".
     * {@code 0.0.0.0} - {@code 0.255.255.255} for IPv4.
     * <p>
     * TODO - include logic for IPv6
     *
     * @return true if this is a reserved address
     */
    public boolean isReserved() {
        return family == Family.IPv4 && bytes[0] == 0;
    }

    /** 
     * Returns true if the address represented is a "loopback" address.
     * ({@code 127.0.0.0} - {@code 127.255.255.255} for IPv4;
     * {@code ::1} for IPv6).
     *
     * @return true if this is the loopback address for the appropriate family
     */
    public boolean isLoopback() {
        return this.equals(LOOPBACK_IPv4) ||
               this.equals(LOOPBACK_IPv6) ||
               (this.family==Family.IPv4 && bytes[0] == 127);
    }

    /** 
     * Returns true if the address represented is the "undetermined" address.
     * ({@code 0.0.0.0} for IPv4; {@code ::} for IPv6 ).
     *
     * @return true if this is the undetermined address for the
     *  appropriate family
     */
    public boolean isUndetermined() {
        return this.equals(UNDETERMINED_IPv4) || this.equals(UNDETERMINED_IPv6);
    }

    /** 
     * Returns true if this address is a "link local" address.
     * ({@code 169.254.*.*} for IPv4; {@code fe80::*:*:*:*} for IPv6 ).
     *
     * @return true if this is a link local address
     */
    public boolean isLinkLocal() {
        return linkLocal;
    }


/*
        todo -- boolean isUnicast()
          * Unicast addresses

            A unicast address identifies a single network interface.
            The protocol delivers packets sent to a unicast address to
            that specific interface. Unicast IPv6 addresses can have a
            scope which is reflected in more specific address names:
              * global unicast address
              * link-local address
              * unique local unicast address
*/

/*
    // can't do -- boolean isAnycast()
        You can't determine an anycast address from the address alone
        (it looks like a unicast address). Anycast addresses differ only by
        being injected into the routing protocol at multiple points in
        the network.
 */

/*
    todo -- boolean isMulticast() -- augment existing (IPv4) predicate
       Multicast addresses begin with an octet of 1 bits (prefix of FF00::/8)
        The four least-significant bits of the second address octet identify
        the address scope, i.e. the span over which the multicast address is
        propagated

        Commonly implemented scopes are:
         node-local (0x1)
         link-local (0x2)
         site-local (0x5)
         organization-local (0x8)
         global (0xE)

       The least-significant 112 bits form the multicast group identifier.
         Only the low-order 32 bits of the group ID are commonly used, because
         of traditional methods of forming 32 bit identifiers from Ethernet
         addresses Defined group IDs are 0x1 for all-nodes multicast addressing
         and 0x2 for all-routers multicast addressing.

       Another group of multicast addresses are solicited-node multicast
         addresses which are formed with the prefix FF02::1:FF00:0/104, and
         where the rest of the group ID (least significant 24 bits) is filled
         from the interface's unicast or anycast address. These addresses allow
         link-layer address resolution via Neighbor Discovery Protocol (NDP)
         on the link without disturbing all nodes on the local network.
*/

    // todo -- predicates for special addresses
/*
Unspecified address

    * ::/128 - the address with all zero bits is called the unspecified address.
      This address must never be assigned to an interface and is to be used
      only in software before the application has learned its host's source
      address appropriate for a pending connection. Routers must not forward
      packets with the unspecified address.

Link local addresses

    * ::1/128 - the loopback address is a unicast localhost address.
      If an application in a host sends packets to this address, the IPv6 stack
      will loop these packets back on the same virtual interface (corresponding
      to 127.0.0.1 in IPv4).
    * fe80::/10 - The link-local prefix specifies that the address is only
      valid in the scope of a given local link. This is analogous to the
      Autoconfiguration IP addresses 169.254.0.0/16 in IPv4.

Unique local addresses

    * fc00::/7 - unique local addresses (ULA) are routable only within a set of
      cooperating sites. They were defined in RFC 4193 as a replacement for
      site-local addresses (see below). The addresses include a 40-bit
      pseudorandom number in the routing prefix that intends to minimize the
      risk of conflicts if sites merge or packets are misrouted into the
      Internet. Despite the restricted, local usage of these addresses, they
      have a global address scope. This is a departure from the site-local
      address definition that unique local addresses replaced.

Multicast addresses

    * ff00::/8 - The multicast prefix designates multicast addresses as defined
      in "IP Version 6 Addressing Architecture" (RFC 4291). Some of these have
      been assigned to specific protocols, for example ff0X::101 will reach all
      local NTP servers (RFC 2375).

Solicited-node multicast addresses

    * ff02::1:FFXX:XXXX - XX:XXXX are the 3 low order octets of the
      corresponding unicast or anycast address.

IPv4 transition

    * ::ffff:0:0/96 - this prefix is used for IPv4 mapped addresses
      (see Transition mechanisms below).
    * 2001::/32 - Used for Teredo tunneling.
    * 2002::/16 - this prefix is used for 6to4 addressing.

ORCHID

    * 2001:10::/28 - ORCHID (Overlay Routable Cryptographic Hash Identifiers)
      as per (RFC 4843). These are non-routed IPv6 addresses used for
      Cryptographic Hash Identifiers.

Documentation

    * 2001:db8::/32 � this prefix is used in documentation (RFC 3849). The
      addresses should be used anywhere an example IPv6 address is given, or
      model networking scenarios are described.

Deprecated or obsolete addresses

    * ::/96 - This is a 96-bit zero-value prefix originally known as
      IPv4-compatible addresses. This class of addresses were used to represent
      IPv4 addresses within an IPv6 transition technology. Such an IPv6 address
      has its first 96 bits set to zero, while its last 32 bits are the IPv4
      address that is represented. The Internet Engineering Task Force (IETF)
      has deprecated the use of IPv4-compatible addresses with publication
      RFC 4291. The only remaining use of this address format is to represent
      an IPv4 address in a table or database with fixed size members that must
      also be able to store an IPv6 address.

    * fec0::/10 - The site-local prefix specifies that the address is valid
      only inside the local organization. Its use has been deprecated in
      September 2004 by RFC 3879 and new systems must not support this special
      type of address.
*/

    /** 
     * Implements the Comparable interface, to return addresses
     * in natural order. If an IPv6 address is compared with an IPv4 address,
     * the former will always come after the latter if any of its top 3/4 bits
     * are non-zero.
     *
     * @param o the other IP address
     * @return an integer value indicating the relative ordering
     * @see Comparable#compareTo
     */
    @Override
    public int compareTo(IpAddress o) {
        long result = 0;
        if (this.family == Family.IPv6) {
            if (o.family == Family.IPv6) {
                // comparing two IPv6 addresses
                int i = 0;
                while (result == 0 && i < quarters.length) {
                    result = this.quarters[i] - o.quarters[i];
                    i++;
                }
            } else {
                // comparing IPv6 against IPv4
                result = this.higherOrderBitsSet ? 1
                                                 : this.lowOrder - o.lowOrder;
                if (result == 0)
                    result = 1; // for 'same' address, put IPv6 after IPv4
            }

        } else {  // this.addressFamily == IPv4
            if (o.family == Family.IPv6) {
                // comparing IPv4 against IPv6
                result = o.higherOrderBitsSet ? -1
                                              : this.lowOrder - o.lowOrder;
                if (result == 0)
                    result = -1; // for 'same' address, put IPv4 before IPv6
            } else {
                // comparing two IPv4 addresses
                result = this.lowOrder - o.lowOrder;
            }
        }
        return Long.signum(result);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IpAddress ip = (IpAddress) o;
        return family == ip.family && Arrays.equals(bytes, ip.bytes);
    }

    @Override
    public int hashCode() {
        return cachedHashCode;
    }

    /** 
     * Obtains a subnet mask with the given number of 1 bits.
     *
     * @param oneBitCount the number of 1 bits
     * @return the appropriate subnet mask
     * @throws IllegalArgumentException if oneBitCount is inappropriate
     */
    SubnetMask getSubnetMask(int oneBitCount) {
        return SubnetMask.valueOf(oneBitCount, family);
    }


    /** Specifies the IP address families. */
    public static enum Family {
        /** IPv4 family of IP Addresses. */
        IPv4,
        /** IPv6 family of IP Addresses. */
        IPv6,
    }

    /** 
     * Specifies the IP address class.
     * A,B,C,D,E for IPv4, or CLASSLESS for IPv6.
     */
    public static enum AddressClass {
        A(8, "10.0.0.0/8"),      //   0.0.0.0  -  127.255.255.255
        B(16, "172.16.0.0/12"),  // 128.0.0.0  -  191.255.255.255
        C(24, "192.168.0.0/16"), // 192.0.0.0  -  223.255.255.255
        D(-1, null),             // 224.0.0.0  -  239.255.255.255  (Multicast)
        E(-1, null),             // 240.0.0.0  -  255.255.255.255  (Reserved)
        CLASSLESS(-1, null);

        private final Subnet privateNetwork;
        private final SubnetMask impliedMask;

        /** Defines each address class.
         *
         * @param n the number of bits in the network portion of the address
         * @param privateStr the string representing the subnet for the
         *                   private network
         */
        private AddressClass(int n, String privateStr) {
            privateNetwork = privateStr == null ? null
                                                : Subnet.valueOf(privateStr);
            impliedMask = n > 0 ? SubnetMask.valueOf(n, Family.IPv4) : null;
        }

        /** Returns the subnet representing the "private network" defined for
         * the address class, for classes A, B, C; null for other classes.
         * That is:
         * <pre>
         *  Class A : "10.0.0.0/8"
         *  Class B : "172.16.0.0/12"
         *  Class C : "192.168.0.0/16"
         * </pre>
         * @return the private network
         */
        public Subnet getPrivateNetwork() {
            return privateNetwork;
        }

        /** Returns the subnet mask implied by the address class, for
         * classes A, B, C; null for other classes.
         * That is:
         * <pre>
         *  Class A : 255.0.0.0
         *  Class B : 255.255.0.0
         *  Class C : 255.255.255.0
         * </pre>
         * @return the implied subnet mask
         */
        public SubnetMask getImpliedMask() {
            return impliedMask;
        }
    }


    /** 
     * Verifies that a port number is valid.
     *
     * @param portStr the port number as a string
     * @return the port number
     * @throws IllegalArgumentException if the port is not 0..65535
     */
    private static int verifyPort(String portStr) {
        int portNum = NO_PORT;
        boolean ok = true;
        try {
            portNum = Integer.parseInt(portStr);
            if (portNum < 0 || portNum > MAX_PORT) {
                ok = false;
            }
        } catch (NumberFormatException e) {
            ok = false;
        }
        if (!ok)
            throw new IllegalArgumentException(BAD_PORT_NUMBER + portStr);

        return portNum;
    }

    /** 
     * Verifies the value of a port number.
     * Note that -1 is used to signify "undetermined" port number, and is
     * a valid parameter.
     *
     * @param port the port number to verify
     * @return the port number
     * @throws IllegalArgumentException if the parameter is not -1..65535
     */
    private static int verifyPort(int port) {
        if (port < -1 || port > MAX_PORT)
            throw new IllegalArgumentException(BAD_PORT_NUMBER + port);
        return port;
    }

    //=== PUBLIC API ==========================================================

    /** The loopback address (IPv4); that is, 127.0.0.1. */
    public static final IpAddress LOOPBACK_IPv4 = valueOf(IPv4_LOOPBACK_STR);
    
    /** The loopback address (IPv6); that is, "::1". */
    public static final IpAddress LOOPBACK_IPv6 = valueOf(IPv6_LOOPBACK_STR);

    /** The undetermined address (IPv4); that is, "0.0.0.0". */
    public static final IpAddress UNDETERMINED_IPv4 =
            valueOf(IPv4_UNDETERMINED_STR);
    /** The undetermined address (IPv6), that is "::". */
    public static final IpAddress UNDETERMINED_IPv6 =
            valueOf(IPv6_UNDETERMINED_STR);

    /** 
     * The (limited) broadcast address (IPv4), that is "255.255.255.255".
     * (To other hosts on the LAN.)
     */
    public static final IpAddress BROADCAST_IPv4 = valueOf(IPv4_BROADCAST_STR);


    /** 
     * Returns an IP address instance that represents the value of the
     * IP address defined by the specified address bytes. Note that the address
     * bytes are presumed to be in network byte order; that is, the highest
     * order byte of the address is at index 0.
     * <p>
     * If the bytes array is of length 4, it is interpreted as an IPv4 address.
     * If the bytes array is of length 16, it is interpreted as an IPv6 address.
     * Other array lengths will throw an exception.
     *
     * @param bytes the address bytes
     * @return an IP address instance
     * @throws NullPointerException if the parameter is null
     * @throws IllegalArgumentException if bytes is an unsupported length
     */
    public static IpAddress valueOf(byte[] bytes) {
        return new IpAddress(bytes, true);
    }

    /** Convenience method that simply delegates to {@link #valueOf(byte[])}.
     *
     * @param bytes the address bytes
     * @return an IP address instance
     * @throws NullPointerException if the parameter is null
     * @throws IllegalArgumentException if bytes is an unsupported length
     */
    public static IpAddress ip(byte[] bytes) {
        return valueOf(bytes);
    }

    /** 
     * Returns an IP address instance that represents the value of the
     * IP address defined by the specified string. Both uppercase and lowercase
     * hex digits are allowed (for <b>IPv6</b> addresses).
     * <p>
     * Acceptable formats are:
     * <ul>
     * <li> <b>IPv4:</b> dotted decimal format ({@code "n.n.n.n"}) --
     *      where 'n' is from 0 to 255</li>
     * <li> <b>IPv4:</b> dotted decimal format with port number suffix
     *      ({@code "n.n.n.n:p"}) -- where 'n' is from 0 to 255, and 'p' is
     *      from 0 to 65535 </li>
     * <li> <b>IPv6:</b> eight groups of hexadecimal digits, separated by colon
     *      ({@code "x:x:x:x:x:x:x:x"}) -- where 'x' is from 0000 to FFFF.
     *      Leading zeros may be dropped. </li>
     * <li> <b>IPv6:</b> shortened form, i.e. a single run of zeros may be
     *      replaced with double colons (e.g. {@code "FF:BAD::3"}) </li>
     * <li> <b>IPv6:</b> an address within square brackets
     *      (e.g. {@code "[2001:db8::1428:57ab]"})</li>
     * <li> <b>IPv6:</b> an address within square brackets with port number
     *      suffix (e.g. {@code "[2001:db8::1428:57ab]:443"}) </li>
     * </ul>
     * Note that if a port number suffix is included:
     * <ul>
     * <li> the port number is dropped</li>
     * <li> even though the port number is dropped, an exception will be
     *      thrown if the port number is &lt; 0 or &gt; 65535 </li>
     * </ul>
     *
     * @param s the string representation of the IP address
     * @return an IP address instance
     * @throws NullPointerException if s is null
     * @throws IllegalArgumentException if s is not an acceptable format
     */
    public static IpAddress valueOf(String s) {
        String key = s.trim().toUpperCase(Locale.getDefault());
        String portStr = null;

        // Check for string forms that include a port number...
        Matcher m = IPv6_PORT_PATTERN.matcher(key);
        if (m.matches()) {
            key = m.group(1); // the address is inside the square brackets
            portStr = m.group(3); // the third group is the port number
        } else {
            // not a square bracket form. Is it an IPv4 port form? "n.n.n.n:p"
            m = IPv4_PORT_PATTERN.matcher(key);
            if (m.matches()) {
                key = m.group(1); // pull out just the address
                portStr = m.group(2); // the second group is the port number
            }
        }
        if (portStr != null) {
            // throw away the result, just don't catch the exception
            verifyPort(portStr);
        }

        return new IpAddress(key);
    }

    /**
     * Convenience method that simply delegates to {@link #valueOf(String)}. 
     * Note that code can be written more concisely by using a static import 
     * of this method; for example, the following two statements are
     * equivalent:
     * <pre>
     * IpAddress ip = IpAddress.valueOf("15.255.124.7");
     * DnsName a = dns("foo.hp.com");
     * </pre>
     *
     * @param s the string representation of the IP address
     * @return an IP address instance
     * @throws NullPointerException if s is null
     * @throws IllegalArgumentException if address is not an acceptable format
     */
    public static IpAddress ip(String s) {
        return valueOf(s);
    }


    /** 
     * Returns an IP address instance that represents the value of the
     * IP address defined by the specified {@link java.net.InetAddress}
     * instance.
     *
     * @param ia the InetAddress
     * @return an IP address instance representing the same address
     * @throws NullPointerException if the parameter is null
     */
    public static IpAddress valueOf(InetAddress ia) {
        // simply retrieve the byte[] from the InetAddress
        return valueOf(ia.getAddress());
    }

    /**
     * Convenience method that simply delegates 
     * to {@link #valueOf(java.net.InetAddress)}.
     *
     * @param ia the InetAddress
     * @return an IP address instance representing the same address
     * @throws NullPointerException if the parameter is null
     */
    public static IpAddress ip(InetAddress ia) {
        return valueOf(ia);
    }

    /**
     * Reads bytes from the specified byte buffer and creates an IP address
     * instance. If {@code v6} is true 16 bytes will be read; otherwise 4 bytes
     * will be read.
     *
     * @param buffer the byte buffer from which to read bytes 
     * @param v6 true if reading an IPv6 address; false for IPv4 
     * @return an IP address instance
     * @throws BufferUnderflowException if the buffer does not have sufficient
     *         bytes remaining
     */
    public static IpAddress valueFrom(ByteBuffer buffer, boolean v6) {
        byte bytes[] = new byte[v6 ? IP_V6_ADDR_SIZE : IP_V4_ADDR_SIZE];
        buffer.get(bytes);
        return new IpAddress(bytes, false);
    }


    /** 
     * Returns the value of the port number from an address string
     * (if one is present).
     * <p>
     * Some examples:
     * <ul>
     * <li> {@code "127.0.0.1:8080"} will return {@code 8080} </li>
     * <li> {@code "127.0.0.1"} will return {@code -1} (no port number)</li>
     * <li> {@code "[2001:db8::1428:57ab]:443"} will return {@code 443} </li>
     * <li> {@code "[2001:db8::1428:57ab]"} will return {@code -1}
     *      (no port number)</li>
     * </ul>
     *
     * @param address the address string
     * @return the port number
     * @throws NullPointerException if address string is null
     * @throws IllegalArgumentException if the string is badly formatted or
     *          the port number is &lt; 0 or &gt; 65535
     */
    public static int valueOfPort(String address) {
        String portStr;
        Matcher m = IPv6_PORT_PATTERN.matcher(address);
        if (!m.matches()) {
            m = IPv4_PORT_PATTERN.matcher(address);
            if (!m.matches()) {
                throw new IllegalArgumentException(BAD_ADDRESS_FORMAT + address);
            }

            // matched  "n.n.n.n:p"  pattern
            portStr = m.group(2);

        } else {
            // matched  "[ ... ]:p"  pattern
            portStr = m.group(3);
        }
        return (portStr != null) ? verifyPort(portStr) : NO_PORT;
    }

}
