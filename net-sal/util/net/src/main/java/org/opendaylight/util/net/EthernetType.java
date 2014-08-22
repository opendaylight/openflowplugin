/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.cache.CacheableDataType;
import org.opendaylight.util.cache.WeakValueCache;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Represents an Ethernet frame type.
 * <p>
 * All constructors for this class are private. Creating instances of
 * {@code EthernetType} is done via the static methods on the class.
 * <p>
 * Instances of this class are immutable, making them inherently threadsafe.
 * <p>
 * This class overrides {@link #equals} and {@link #hashCode} so that instances
 * play nicely with the Collection classes.
 * <p>
 * This class implements the {@link Comparable} interface to ensure that a
 * sorted list of ethernet types is presented in an intuitive order.
 *
 * @author Simon Hunt
 * @author Thomas Vachuska
 */
public final class EthernetType extends CacheableDataType
                            implements Comparable<EthernetType> {

    private static final long serialVersionUID = -7240112724631609362L;

    /** Length of an Ethernet type when encoded as a byte array. */
    public static final int LENGTH_IN_BYTES = 2;

    private static final ResourceBundle ETH_TYPE_LOOKUP =
          ResourceUtils.getBundledResource(EthernetType.class, "ethernetType");

    private static final String UNKNOWN = ETH_TYPE_LOOKUP.getString("UNKNOWN");
    private static final String COLON = ":";

    /** The ethernet type number.
     * @serial type number
     */
    private final int number;

    /** The number as a hex string, padded to 4 digits. */
    private transient String hex;

    /** The descriptive name for the Ethernet frame type.
     * E.g. "IPv4 : Internet Protocol IPv4".
     */
    private transient String name;

    /** The short name for the Ethernet frame type. E.g. "IPv4" */
    private transient String shortName;

    /** True if the protocol is not a "named" value from the resource bundle. */
    private transient boolean unknown;

    // === PRIVATE CONSTRUCTORS ===============================================

    /**
     * Constructs an Ethernet type instance from the protocol number.
     * Validation of the type number is done in the static method.
     * @param number the type number
     * @see #valueOf(int)
     */
    private EthernetType(int number) {
        this.number = number;
        this.hex = String.format("%04x", number);
        String s;
        try {
            s = ETH_TYPE_LOOKUP.getString(hex);
            unknown = false;
        } catch (MissingResourceException e) {
            s = UNKNOWN;
            unknown = true;
        }
        name = s;
        String[] stuff = s.split(COLON);
        shortName = stuff[0].trim();
    }

    // === PRIVATE serialization ==============================================

    //== Implementation note:
    //      Default deserialization will set the serialized field (number) to
    //      whatever was in the stream, and will set the transient fields to
    //      defaults (null). This is good enough for our purposes, because we
    //      will simply use valueOf() to (create and?) return the
    //      cached instance.


    private Object readResolve() throws ObjectStreamException {
        // when this is called, number has been populated.
        // return the appropriate cached instance instead.
        Object o;
        try {
            o = valueOf(number);
        } catch (IllegalArgumentException e) {
            throw new InvalidObjectException(e.getMessage());
        }
        return o;
    }

    // === PUBLIC instance API ================================================

    /** Returns a string describing the Ethernet type.
     *
     * @return the Ethernet type number and name
     */
    @Override
    public String toString() {
        return "0x" + hex + "(" + shortName + ")";
    }

    /** Returns the descriptive name of the Ethernet type.
     * E.g. "IPv4 : Internet Protocol IPv4"
     *
     * @return the descriptive name
     */
    public String getName() {
        return name;
    }

    /** Returns the short name of the Ethernet type.
     *  E.g. "IPv4"
     *
     * @return the short name
     */
    public String getShortName() {
        return shortName;
    }

    /** Returns the Ethernet type number.
     *
     * @return the type number
     */
    public int getNumber() {
        return number;
    }

    /** Returns true if the ethernet type number is not one of the "named"
     * Ethernet types. More specifically, the type is not defined in the
     * ethernetType resource bundle.
     *
     * @return true if unknown
     */
    public boolean isUnknown() {
        return unknown;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return number == ((EthernetType) o).number;
    }

    @Override
    public int hashCode() {
        return number;
    }

    /** Implements the Comparable interface, to return Ethernet types in
     * natural order.
     *
     * @param o the other Ethernet type instance
     * @return an integer value indicating relative ordering
     * @see Comparable#compareTo
     */
    @Override
    public int compareTo(EthernetType o) {
        return this.number - o.number;
    }


    // === STATIC methods =====================================================

    /** Reverse lookup map from the properties file.
     * For each (numeric) entry in the properties file there are two mappings;
     * the full string and the "short name" (both converted to lower case).
     * For example:
     * <ul>
     * <li> "ipv4 : internet protocol ipv4" -&gt; 0x0800 </li>
     * <li> "ipv4" -&gt; 0x0800 </li>
     * </ul>
     */
    private static final Map<String, Integer> reverseLookup =
            new HashMap<String,Integer>();

    private static final Pattern RE_DIGITS = Pattern.compile("[0-9a-fA-F]+");

    /** Static initialization of reverse lookup map */
    static {
        for (String key: ETH_TYPE_LOOKUP.keySet()) {
            if (RE_DIGITS.matcher(key).matches()) {
                int number = Integer.parseInt(key, 16);
                String name = ETH_TYPE_LOOKUP.getString(key)
                                        .toLowerCase(Locale.getDefault());
                String[] stuff = name.split(COLON);
                reverseLookup.put(name, number);
                reverseLookup.put(stuff[0].trim(), number);
            }
        }
    }


    /** Our self-trimming cache. */
    private static final WeakValueCache<Integer, EthernetType> cachedEthTypes =
                     new WeakValueCache<Integer, EthernetType>(getRefQ());

    /** Ensures that all equivalent Ethernet type encoding keys
     * map to the same instance of EthernetType.
     * <p>
     * Note that this method is always called from inside
     * a block synchronized on {@link #cachedEthTypes}.
     *
     * @param et a newly constructed EthernetType (which may get dropped)
     * @param key the lookup key
     * @return a reference to the appropriate unique EthernetType instance
     */
    private static synchronized EthernetType intern(EthernetType et, int key) {
        cachedEthTypes.put(key, et); // cached by given key
        return et;
    }

    // === PUBLIC static API ==================================================

    /** The instance representing IPv4 (0x0800). */
    public static final EthernetType IPv4 = valueOf(0x0800);
    /** The instance representing ARP (0x0806). */
    public static final EthernetType ARP = valueOf(0x0806);
    /** The instance representing RARP (0x8035). */
    public static final EthernetType RARP = valueOf(0x8035);
    /** The instance representing VLAN (0x8100). */
    public static final EthernetType VLAN = valueOf(0x8100);
    /** The instance representing SNMP (0x814c). */
    public static final EthernetType SNMP = valueOf(0x814c);
    /** The instance representing IPv6 (0x86dd). */
    public static final EthernetType IPv6 = valueOf(0x86dd);
    /** The instance representing MPLS (unicast) (0x8847). */
    public static final EthernetType MPLS_U = valueOf(0x8847);
    /** The instance representing MPLS (multicast) (0x8848). */
    public static final EthernetType MPLS_M = valueOf(0x8848);
    /** The instance representing PRV_BRDG (0x88a8). */
    public static final EthernetType PRV_BRDG = valueOf(0x88a8);
    /** The instance representing LLDP (0x88cc). */
    public static final EthernetType LLDP = valueOf(0x88cc);
    /** The instance representing PBB (0x88e7). */
    public static final EthernetType PBB = valueOf(0x88e7);
    /** The instance representing BDDP (0x8999). */
    public static final EthernetType BDDP = valueOf(0x8999);


    /** Returns an object that represents the value of the Ethernet type
     * identified by the specified string. For example:
     * <pre>
     *   EthernetType arp = EthernetType.valueOf("arp");
     *   String s = arp.getName(); // "ARP : Address Resolution Protocol"
     *   EthernetType arpAlso = EthernetType.valueOf(s);
     *   assert arpAlso.equals(arp);
     * </pre>
     *
     * @param value the Ethernet type identifier
     * @return an object representing the specified Ethernet type
     */
    public static EthernetType valueOf(String value) {
        if (value == null)
            throw new NullPointerException("value cannot be null");

        final String valueLc = value.toLowerCase(Locale.getDefault());
        final Integer key = reverseLookup.get(valueLc);
        if (key == null)
            throw new IllegalArgumentException("unknown value: " + value);
        return valueOf(key);
    }

    /** Returns an object that represents the value of the Ethernet type
     * identified by the specified number.
     *
     * @param typeNumber the Ethernet type number
     * @return an object representing the specified Ethernet type
     * @throws IllegalArgumentException if the parameter is &lt; 0 or &gt; 0xffff
     */
    public static EthernetType valueOf(int typeNumber) {
        if (typeNumber < 0 || typeNumber > 0xffff)
            throw new IllegalArgumentException("Bad Ethernet type number: " +
                                                typeNumber);

        EthernetType result = cachedEthTypes.get(typeNumber);
        return (result == null) ?
                intern(new EthernetType(typeNumber), typeNumber) : result;
    }

}
