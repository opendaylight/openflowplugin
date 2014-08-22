/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.dt;

import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.StringUtils;
import org.opendaylight.util.cache.CacheableDataType;
import org.opendaylight.util.cache.WeakValueCache;
import org.opendaylight.util.net.MacAddress;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.opendaylight.util.ResourceUtils.getBundledResource;
import static org.opendaylight.util.StringUtils.Align;

/**
 * Represents a Data Path Identifier; an aggregation of a {@link VId virtual
 * identifier} and a {@link MacAddress MAC address}.
 * <p>
 * All constructors for this class are private. Creating instances of
 * {@link DataPathId} is done via the static methods on the class.
 * <p>
 * Instances of this class are immutable, making them inherently thread-safe.
 * <p>
 * This class overrides {@link #equals} and {@link #hashCode} so that it plays
 * nicely with the Collection classes.
 * <p>
 * This class implements the {@link Comparable} interface to ensure that a
 * sorted list is presented in an intuitive order.
 *
 * @author Simon Hunt
 */
public final class DataPathId extends CacheableDataType
        implements Comparable<DataPathId> {

    private static final long serialVersionUID = -822396610167997127L;

    /** The length of a datapath id in bytes. */
    public static final int LENGTH_IN_BYTES = 8;

    // package private for unit test access
    private static final ResourceBundle RES = 
            getBundledResource(DataPathId.class);

    static final String E_NULL_SPEC = RES.getString("e_null_spec");
    static final String E_BAD = RES.getString("e_bad");
    static final String E_NULL_BYTES = RES.getString("e_null_bytes");
    static final String E_BYTES_BAD_LEN = RES.getString("e_bytes_bad_len");

    private static final int DPID_HEX_CHAR_COUNT = 16;
    private static final int VID_LENGTH = 2;
    private static final String OX = "0x";
    private static final String SLASH = "/";
    private static final String COLON = ":";
    private static final String DISPLAY_FORMAT = "%02x:%02x:%s";

    private static final String RE_BYTE = "[0-9a-fA-F]{2}";
    private static final String RE_VID = RE_BYTE + COLON + RE_BYTE;
    private static final String RE_MAC = RE_BYTE + COLON + RE_BYTE + COLON +
            RE_BYTE + COLON + RE_BYTE + COLON + RE_BYTE + COLON + RE_BYTE;

    private static final Pattern HEX_16_COLON =
            Pattern.compile("(" + RE_VID + "):(" + RE_MAC + ")");

    private static final Pattern HEX_16 =
            Pattern.compile("(DP|dp)?([0-9a-fA-F]{4})([0-9a-fA-F]{12})");

    private final VId vid;
    private final MacAddress mac;

    private final transient long asLong;
    private final transient String asString;    // "00:00:00:00:00:00:00:00"
    private final transient String asAltString; // "0/000000:000000"
    private final transient int hash;

    // Attachment intended to hold backing data path information
    transient Object memento; // see DatatypeUtils

    private DataPathId(VId vid, MacAddress mac) {
        this.vid = vid;
        this.mac = mac;
        this.asLong = makeLong();
        this.asString = makeKey(vid, mac);
        this.asAltString = makeAltString(vid, mac);
        this.hash = makeHash();
    }

    @Override
    public String toString() {
        return asString;
    }

    /**
     * Returns an alternative string representation for this datapath ID.
     *
     * @return an alternative string representation
     */
    public String toAltString() {
       return asAltString;
    }

    /**
     * Returns the virtual identifier.
     *
     * @return the virtual identifier
     */
    public VId getVid() {
        return vid;
    }

    /**
     * Returns the MAC address.
     *
     * @return the MAC address
     */
    public MacAddress getMacAddress() {
        return mac;
    }

    /** 
     * Returns a long value representation of this datapath ID. Note that
     * this representation is simply treated as an opaque 64-bit value.
     *
     * @return a long value representation of this datapath ID
     */
    public long toLong() {
        return asLong;
    }

    private long makeLong() {
        return ByteUtils.getLong(toByteArray(), 0);
    }

    /**
     * Returns a byte array representation of this datapath ID. The array is
     * {@value #LENGTH_IN_BYTES} bytes long, where the first two bytes
     * correspond to the virtual identifier, and the last six bytes correspond
     * to the MAC address.
     *
     * @return a byte array representation of this datapath ID
     */
    public byte[] toByteArray() {
        byte[] bytes = new byte[LENGTH_IN_BYTES];
        System.arraycopy(vid.toByteArray(), 0, bytes, 0, VID_LENGTH);
        System.arraycopy(mac.toByteArray(), 0, bytes, VID_LENGTH,
                         MacAddress.MAC_ADDR_SIZE);
        return bytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        DataPathId that = (DataPathId) o;
        return mac.equals(that.mac) && vid.equals(that.vid);
    }

    /** 
     * Calculates the hash code, based on those of the VID and MAC.
     * Note that we ensure the value is positive; this is required to
     * guarantee that hashBucket() returns a valid (positive) bucket index.
     *
     * @return the pre-computed hashcode
     */
    private int makeHash() {
        int h = 31 * vid.hashCode() + mac.hashCode();
        return h < 0 ? -h : h;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    /** 
     * Returns a bucket index, based on the hash code of this dpid instance
     * and the specified number of buckets.
     * <p>
     * For <em>N</em> buckets, the value returned will be in the range
     * <em>0 .. N-1</em>; the value is deterministic, meaning that the
     * <em>same</em> value is always returned for a specific value of
     * <em>N</em>.
     *
     * @param nBuckets the number of buckets
     * @return the hashed bucket index
     */
    public int hashBucket(int nBuckets) {
        return hash % nBuckets;
    }

    @Override
    public int compareTo(DataPathId o) {
        // sort by VID first, then by MAC address
        int result = vid.compareTo(o.vid);
        return result == 0 ? mac.compareTo(o.mac) : result;
    }

    // === PRIVATE serialization ===

    // == Implementation note:
    // We use default serialization to serialize the VID and MAC fields

    private Object readResolve() throws ObjectStreamException {
        // when this is called, vid and mac have been populated.
        // return the appropriate cached instance instead.
        Object o;
        try {
            o = valueOf(vid, mac);
        } catch (NullPointerException e) {
            throw new InvalidObjectException(e.getMessage());
        } catch (IllegalArgumentException e) {
            throw new InvalidObjectException(e.getMessage());
        }
        return o;
    }

    // === STATIC Methods ===

    private static final WeakValueCache<String, DataPathId> cachedDpids =
            new WeakValueCache<String, DataPathId>(getRefQ());

    /**
     * Creates the lookup key for an instance of DataPathId.
     *
     * @param vid the virtual identifier
     * @param mac the MAC address
     * @return the key
     */
    private static String makeKey(VId vid, MacAddress mac) {
        byte[] vbytes = vid.toByteArray();
        return String.format(DISPLAY_FORMAT, vbytes[0], vbytes[1],
                mac.toString());
    }

    /**
     * Creates an alternative string representation for an instance
     * of datapath ID.
     *
     * @param vid the virtual identifier
     * @param mac the MAC address
     * @return the alternate string representation
     */
    private static String makeAltString(VId vid, MacAddress mac) {
        return vid.toString() + SLASH +
                mac.toFormattedString(MacAddress.Format.SINGLE_COLON, true);
    }

    /**
     * Ensures that all equivalent encoding keys map to the same
     * instance of datapath ID.
     * <p>
     * Note that this method is always called from inside a block synchronized
     * on {@link #cachedDpids}.
     *
     * @param d a newly constructed datapath ID (which may get dropped)
     * @param key the lookup key
     * @return a reference to the appropriate unique datapath ID instance
     */
    private static DataPathId intern(DataPathId d, String key) {
        final String canon = d.toString();
        DataPathId alreadyCached = cachedDpids.get(canon);
        DataPathId keeper = alreadyCached != null ? alreadyCached : d;
        cachedDpids.put(canon, keeper); // cached by normalized string rep
        cachedDpids.put(key, keeper); // cached by given key
        return keeper;
    }

    // === PUBLIC API ===

    /**
     * Returns a datapath ID instance that encapsulates the given
     * virtual identifier and MAC address.
     *
     * @param vid the virtual identifier
     * @param mac the MAC address
     * @return the datapath ID
     * @throws NullPointerException if either parameter is null
     */
    public static DataPathId valueOf(VId vid, MacAddress mac) {
        if (vid == null || mac == null)
            throw new NullPointerException("null parameter(s)");

        String key = makeKey(vid, mac);
        synchronized (cachedDpids) {
            DataPathId result = cachedDpids.get(key);
            if (result == null) {
                result = new DataPathId(vid, mac);
                cachedDpids.put(key, result);
            }
            return result;
        }
    }

    /**
     * Returns a datpath ID instance that encapsulates the virtual
     * identifier and MAC address expressed in the given string specification.
     * <p>
     * The string spec can take three basic forms:
     * <ul>
     *     <li>{@code "<virtual-id>/<mac-address>"}</li>
     *     <li>{@code "<16-char-hex>"}</li>
     *     <li>{@code "<colon-delim-hex>"}</li>
     * </ul>
     * where {@code virtual-id} is an integer in the range {@code 0..65535} (or
     * {@code 0x0..0xffff}), {@code mac-address} is a MAC address format
     * accepted by {@link MacAddress#valueOf(String)}, {@code 16-char-hex} is
     * a 16-digit hex number, and {@code colon-delim-hex} is a 16-digit hex
     * number with colons between each byte.
     * <p>
     * For example, the following statements all yield the same value:
     * <pre>
     * DataPathId dpid = DataPathId.valueOf("123/00:05:53:AF:AA:C0");
     * DataPathId same = DataPathId.valueOf("0x7b/000553:afaac0");
     * DataPathId also = DataPathId.valueOf("007b000553AFAAC0");
     * DataPathId copy = DataPathId.valueOf("00:7B:00:05:53:AF:AA:C0");
     * </pre>
     *
     * @param spec the string specification
     * @return the datapath ID instance
     * @throws NullPointerException if spec is null
     * @throws IllegalArgumentException if spec is malformed
     */
    public static DataPathId valueOf(String spec) {
        if (spec == null)
            throw new NullPointerException(E_NULL_SPEC);

        synchronized (cachedDpids) {
            DataPathId result = cachedDpids.get(spec);
            if (result == null) {
                // first try 16-digit hex format
                Matcher m = HEX_16_COLON.matcher(spec);
                if (m.matches()) {
                    VId vid = VId.valueOf(ByteUtils.parseHex(m.group(1)));
                    MacAddress mac = MacAddress.valueOf(m.group(2));
                    result = intern(new DataPathId(vid, mac), spec);
                } else {
                    m = HEX_16.matcher(spec);
                    if (m.matches()) {
                        VId vid = VId.valueOf(OX + m.group(2));
                        MacAddress mac = MacAddress.valueOf(m.group(3));
                        result = intern(new DataPathId(vid, mac), spec);
                    } else {
                        // parse the spec as "vid/mac"
                        String[] bits = spec.split(SLASH);
                        if (bits.length != 2)
                            throw new IllegalArgumentException(E_BAD + spec);
                        VId vid;
                        MacAddress mac;
                        try {
                            vid = VId.valueOf(bits[0]);
                            mac = MacAddress.valueOf(bits[1]);
                        } catch (IllegalArgumentException e) {
                            throw new IllegalArgumentException(E_BAD + spec, e);
                        }
                        result = intern(new DataPathId(vid, mac), spec);
                    }
                }
            }
            return result;
        } // sync
    }

    /**
     * Convenience method that returns the datapath ID for the given string
     * representation. This method simply delegates to {@link #valueOf(String)}.
     * Using a static import of this method allows more concise code to be 
     * written, for example, the following two statements are equivalent:
     * <pre>
     *  DataPathId d = DataPathId.valueOf("1/112233:445566");     
     *  DataPathId d = dpid("1/112233:445566");     
     * </pre>
     * @param spec the specification 
     * @return the corresponding datapath ID instance
     */
    public static DataPathId dpid(String spec) {
        return valueOf(spec);
    }
    
    /**
     * Returns a datapath ID instance from the given byte array. The
     * array is expected to be {@value #LENGTH_IN_BYTES} bytes long.
     * <p>
     * The first two bytes are interpreted as a {@link VId}, and the remaining
     * six bytes are interpreted as a {@link MacAddress}.
     * <p>
     * For example:
     * <pre>
     * int B = 256;
     * byte[] bytes = new byte[] {
     *     0, 123,                                    // VID
     *     0x00, 0x05, 0x53, 0xaf-B, 0xaa-B, 0xc0-B   // MAC
     * };
     *
     * DataPathId dpid = DataPathId.valueOf(bytes);
     * </pre>
     *
     * @param bytes the byte array
     * @return the datapath ID
     * @throws NullPointerException if the byte array is null
     * @throws IllegalArgumentException if the byte array is not 8 bytes long
     */
    public static DataPathId valueOf(byte[] bytes) {
        if (bytes == null)
            throw new NullPointerException(E_NULL_BYTES);
        if (bytes.length != LENGTH_IN_BYTES)
            throw new IllegalArgumentException(E_BYTES_BAD_LEN);

        final String key = keyFromBytes(bytes);
        synchronized (cachedDpids) {
            DataPathId result = cachedDpids.get(key);
            if (result == null) {
                result = valueOf(hex16chars(bytes));
                cachedDpids.put(key, result);
            }
            return result;
        } // sync
    }

    private static String hex16chars(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b: bytes)
            sb.append(ByteUtils.byteToHex(b));
        return sb.toString();
    }

    /**
     * Returns a datapath ID instance from the given long value. Note that the
     * long is treated as an opaque value of 64 bits.
     *
     * @param value the value
     * @return the datapath ID
     */
    public static DataPathId valueOf(long value) {
        final String key = Long.toHexString(value);
        synchronized (cachedDpids) {
            DataPathId result = cachedDpids.get(key);
            if (result == null) {
                result = valueOf(addLeadingZeros(key));
                cachedDpids.put(key, result);
            }
            return result;
        } // sync
    }

    private static String addLeadingZeros(String key) {
        return StringUtils.pad(key, DPID_HEX_CHAR_COUNT, '0', Align.RIGHT);
    }
}