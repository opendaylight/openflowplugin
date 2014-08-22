/*
 * (c) Copyright 2011-2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.opendaylight.util.StringUtils;
import org.opendaylight.util.cache.CacheableDataType;
import org.opendaylight.util.cache.WeakValueCache;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.opendaylight.util.net.MacAddress.MAC_ADDR_SIZE;

/**
 * Represents a MAC address prefix; that is to say, the top
 * <i>N</i> bytes of the address are fixed (where <i>N</i> is 1 to 5) and
 * the remaining bytes are unspecified.
 * <p>
 * Instances of this class are created using the {@link #valueOf} factory
 * methods.
 * <p>
 * A string specification may be used to define the fixed bytes (with either
 * colon or hyphen as the delimiter):
 * <pre>
 * {byte} [ : {byte} [ : ... ] ]    or    {byte} [ - {byte} [ - ... ] ]
 * </pre>
 * where <em>byte</em> represents a hex value in the range 00 to FF.
 * <p>
 * For example, the specification {@code "FE:00:45"} declares a MAC prefix
 * where the top 3 bytes are <b>FE</b>, <b>00</b>, and <b>45</b>,
 * and the bottom three bytes may be any value.
 * <p>
 * Another example, the specification {@code "FE-00-45-12-AB"} declares a MAC
 * prefix where the top 5 bytes are <b>FE</b>, <b>00</b>, <b>45</b>,
 * <b>12</b>, and <b>AB</b>,
 * and the bottom byte may be any value.
 * <p>
 * Alternatively, a byte array may be used to specify the prefix. The
 * byte arrays to produce the prefixes equivalent to the above examples
 * are:
 * <pre>
 * final int B = 256; // makes it easier to read the declared byte arrays
 * byte[] b1 = { 0xfe-B, 00, 0x45 };
 * byte[] b2 = { 0xfe-B, 00, 0x45, 0x12, 0xab-B };
 * </pre>
 *
 * @author Simon Hunt
 */
public final class MacPrefix extends CacheableDataType
        implements Comparable<MacPrefix> {

    private static final long serialVersionUID = 8924359194837535900L;

    private static final String COLON = ":";
    private static final String DASH = "-";
    private static final String STAR = "*";

    private static final int BYTES_MIN = 1;
    private static final int BYTES_MAX = MAC_ADDR_SIZE-1;

    /** Precomputed string representation of this prefix.
     *  Note that this is the only field that is serialized.
     */
    private final String asString;

    /** Number of bytes in this prefix. */
    private transient int size;

    /** The range that is equivalent to this prefix. */
    private transient MacRange range;

    // === PRIVATE CONSTRUCTORS ===

    /** Constructs a newly allocated {@link MacPrefix} object that represents
     * the MAC prefix with the upper bytes specified by the byte array.
     * Note that the address bytes are presumed to be in network byte order;
     * that is, the highest order byte of the prefix is at index 0.
     * The array length is expected to be from 1 to 5. An exception is thrown
     * if it is not.
     *
     * @param bytes the prefix upper bytes
     * @throws NullPointerException if bytes is null
     * @throws IllegalArgumentException if bytes length is inappropriate
     */
    private MacPrefix(byte[] bytes) {
        if (bytes == null)
            throw new NullPointerException("prefix bytes array cannot be null");

        size = bytes.length;
        if (size < BYTES_MIN || size > BYTES_MAX)
            throw new IllegalArgumentException("Unsupported # bytes (" +
                                                size + ")");

        // Convert byte array to a mac address (string) to manipulate
        byte[] b = Arrays.copyOf(bytes, MAC_ADDR_SIZE); // padded with zeros
        String macStr = MacAddress.valueOf(b).toString();
        final int numChars = size * 3 - 1; // 1->2, 2->5, 3->8, 4->11, 5->14
        asString = macStr.substring(0, numChars);

        // Create the internal MacRange spec, from the bytes
        String[] hexBytes = macStr.split(COLON);
        // replace the tail bytes with stars
        for (int i=size; i<MAC_ADDR_SIZE; i++)
            hexBytes[i] = STAR;
        String spec = StringUtils.join(hexBytes, COLON);
        range = MacRange.valueOf(spec);
    }

    /** Constructs a newly allocated {@link MacPrefix} object that represents
     * the MAC prefix with the upper bytes specified by the given string.
     * The parsed string can be any of the following formats:
     * <ul>
     *     <li> "XX" </li>
     *     <li> "XX:XX" or "XX-XX" </li>
     *     <li> "XX:XX:XX" or "XX-XX-XX" </li>
     *     <li> "XX:XX:XX:XX" or "XX-XX-XX-XX" </li>
     *     <li> "XX:XX:XX:XX:XX" or "XX-XX-XX-XX-XX" </li>
     * </ul>
     * where each "X" represents a hex digit
     *
     * @param spec the string representation of the MAC prefix
     * @throws NullPointerException if spec is null
     * @throws IllegalArgumentException if spec is not an acceptable format
     */
    private MacPrefix(String spec) {
        if (spec == null)
                throw new NullPointerException("spec cannot be null");
        final String s = spec.trim(); // start by trimming any whitespace

        // validate the input string
        Matcher m = RE.matcher(s);
        if (!m.matches())
            throw new IllegalArgumentException("spec invalid: \"" +
                                                spec + "\"");

        // normalize the input spec
        asString = spec.replace(DASH, COLON).toLowerCase(Locale.getDefault());
        size = asString.split(COLON).length;
        StringBuilder sb = new StringBuilder(asString);
        for (int i=size; i<MAC_ADDR_SIZE; i++)
            sb.append(COLON).append(STAR);
        range = MacRange.valueOf(sb.toString());
    }

    private static final String H2 = MacAddress.H2;
    private static final String RE_STR = "^(" + H2 + ")([:-]" + H2 + "){0,4}";
    static final Pattern RE = Pattern.compile(RE_STR);

    // === PRIVATE serialization ======

    //== Implementation note:
    //      We use default serialization to serialize the string form of
    //      the mac prefix.

    private Object readResolve() throws ObjectStreamException {
        // when this is called, asString has been populated.
        // return the appropriate cached instance instead.
        Object o;
        try {
            o = valueOf(asString);
        } catch (IllegalArgumentException e) {
            throw new InvalidObjectException(e.getMessage());
        }
        return o;
    }

    // === PUBLIC instance methods =======

    @Override
    public String toString() {
        return asString;
    }

    /** Implements the Comparable interface, to return prefixes in
     * natural order.
     *
     * @param o the other MAC prefix
     * @return an integer value indicating the relative ordering
     * @see Comparable#compareTo
     */
    @Override
    public int compareTo(MacPrefix o) {
        return asString.compareTo(o.asString);
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof MacPrefix &&
                asString.equals(((MacPrefix) o).asString);
    }

    @Override
    public int hashCode() {
        return asString.hashCode();
    }

    /** Returns the number of bytes in this prefix.
     *
     * @return the number of bytes in the prefix
     */
    public int size() {
        return size;
    }

    /** Returns true if this MAC prefix applies to the specified MAC address.
     *
     * @param mac the address to examine
     * @return true if this is a prefix of the given MAC address
     */
    public boolean prefixes(MacAddress mac) {
        return range.contains(mac);
    }

    //=== STATIC METHODS =======

    /** Our self-trimming cache. */
    private static final WeakValueCache<String, MacPrefix> cachedPrefixes =
            new WeakValueCache<String, MacPrefix>(getRefQ());

    /** Ensures that all equivalent MAC prefix
     * encoding keys map to the same instance of MacPrefix.
     * <p>
     * Note that this method is always called from inside
     * a block synchronized on {@link #cachedPrefixes}.
     *
     * @param macPrefix a newly constructed MacPrefix (which may get dropped)
     * @param key the lookup key
     * @return a reference to the appropriate unique MacPrefix instance
     */
    private static MacPrefix intern(MacPrefix macPrefix, String key) {
        MacPrefix alreadyCached = cachedPrefixes.get(macPrefix.asString);
        MacPrefix keeper = alreadyCached != null ? alreadyCached : macPrefix;
        cachedPrefixes.put(macPrefix.asString, keeper); // cached by string rep
        cachedPrefixes.put(key, keeper); // cached by given key
        return keeper;
    }

    //=== PUBLIC static API ========

    /** Returns an object that represents the value of the MAC prefix defined
     * by the specified MAC prefix bytes. Note that the bytes are presumed to
     * be in network byte order; that is, the highest order byte of the prefix
     * is at index 0.
     * The array length is expected to be 1 to 5. An exception is thrown if
     * it is not.
     *
     * @param prefixBytes the prefix bytes
     * @return a MacPrefix instance
     * @throws NullPointerException if prefixBytes is null
     * @throws IllegalArgumentException if prefixBytes is not an
     *          acceptable length
     */
    public static MacPrefix valueOf(byte[] prefixBytes) {
        final String key = keyFromBytes(prefixBytes);
        synchronized (cachedPrefixes) {
            MacPrefix result = cachedPrefixes.get(key);
            return (result==null) ?
                    intern(new MacPrefix(prefixBytes), key) : result;
        } // sync
    }

    /** Returns an object that represents the value of the MAC prefix defined
     * by the specified string. Both uppercase and lowercase hex digits are
     * allowed.
     * Acceptable input formats are:
     * <ul>
     *     <li> "XX" </li>
     *     <li> "XX:XX" or "XX-XX" </li>
     *     <li> "XX:XX:XX" or "XX-XX-XX" </li>
     *     <li> "XX:XX:XX:XX" or "XX-XX-XX-XX" </li>
     *     <li> "XX:XX:XX:XX:XX" or "XX-XX-XX-XX-XX" </li>
     * </ul>
     * where each "X" represents a hex digit
     *
     * @param prefixStr the string representation of the MAC prefix
     * @return a MacPrefix instance
     * @throws NullPointerException if prefixStr is null
     * @throws IllegalArgumentException if prefixStr is not an
     *          acceptable format
     */
    public static MacPrefix valueOf(String prefixStr) {
        final String key = prefixStr.trim().toUpperCase(Locale.getDefault());
        synchronized (cachedPrefixes) {
            MacPrefix result = cachedPrefixes.get(key);
            return (result==null) ? intern(new MacPrefix(key), key) : result;
        }
    }

}
