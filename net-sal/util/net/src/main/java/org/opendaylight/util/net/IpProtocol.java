/*
 * (c) Copyright 2009-2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.net;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.opendaylight.util.ResourceUtils;
import org.opendaylight.util.cache.CacheableDataType;
import org.opendaylight.util.cache.WeakValueCache;

/**
 * Represents an IP Protocol.
 * <p>
 * All constructors for this class are private. Creating instances of
 * {@code IpProtocol} is done via the static methods on the class.
 * <p>
 * Instances of this class are immutable, making them inherently threadsafe.
 * <p>
 * This class overrides {@link #equals} and {@link #hashCode} so that instances
 * play nicely with the Collection classes.
 * <p>
 * This class implements the {@link Comparable} interface to ensure that a
 * sorted list of protocols is presented in an intuitive order.
 *
 * @author Simon Hunt
 */
public final class IpProtocol extends CacheableDataType
                              implements Comparable<IpProtocol> {

    private static final long serialVersionUID = -7330596390895608534L;

    private static final ResourceBundle LOOKUP =
            ResourceUtils.getBundledResource(IpProtocol.class, "ipProtocols");

    private static final String UNKNOWN = LOOKUP.getString("UNKNOWN");
    private static final String COLON = ":";
    private static final int ICMPv6_CODE = 58;

    /** The protocol number.
     * @serial protocol number
     */
    private final int number;

    /** The descriptive name for the protocol.
     * E.g. "TCP : Transmission Control Protocol".
     */
    private transient String name;

    /** The short name for the protocol. E.g. "TCP" */
    private transient String shortName;

    /** True if the protocol is not a "named" value from the resource bundle. */
    private transient boolean unknown;

    // === PRIVATE CONSTRUCTORS ===============================================

    /**
     * Constructs an IP protocol instance from the protocol number.
     * Validation of the protocol number is done in the static method.
     * @param number the protocol number
     * @see #valueOf(int)
     */
    private IpProtocol(int number) {
        this.number = number;
        String s;
        try {
            s = LOOKUP.getString(String.valueOf(number));
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

    /** Returns a string describing the IP Protocol.
     *
     * @return the protocol number and name
     */
    @Override
    public String toString() {
        return number + "(" + shortName + ")";
    }

    /** Returns the descriptive name of the protocol.
     * E.g. "TCP : Transmission Control Protocol"
     *
     * @return the descriptive name
     */
    public String getName() {
        return name;
    }

    /** Returns the short name of the protocol.
     *  E.g. "TCP"
     *
     * @return the short name
     */
    public String getShortName() {
        return shortName;
    }

    /** Returns the protocol number.
     *
     * @return the protocol number
     */
    public int getNumber() {
        return number;
    }

    /** Returns true if the protocol number is not one of the "named"
     * protocols. More specifically, the protocol is not defined in the
     * ipProtocols resource bundle.
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
        return number == ((IpProtocol) o).number;
    }

    @Override
    public int hashCode() {
        return number;
    }

    /** Implements the Comparable interface, to return protocols in
     * natural order.
     *
     * @param o the other IP Protocol instance
     * @return an integer value indicating relative ordering
     * @see Comparable#compareTo
     */
    @Override
    public int compareTo(IpProtocol o) {
        return this.number - o.number;
    }


    // === STATIC methods =====================================================

    /** Reverse lookup map from the properties file.
     * For each (numeric) entry in the properties file there are two mappings;
     * the full string and the "short name" (both converted to lower case).
     * For example:
     * <ul>
     * <li> "tcp : transmission control protocol" -&gt; 6 </li>
     * <li> "tcp" -&gt; 6 </li>
     * </ul>
     */
    private static final Map<String, Integer> REVERSE_LOOKUP =
            new HashMap<String,Integer>();

    private static final Pattern RE_DIGITS = Pattern.compile("\\d+");

    /** Static initialization of reverse lookup map */
    static {
        for (String key: LOOKUP.keySet()) {
            if (RE_DIGITS.matcher(key).matches()) {
                int number = Integer.valueOf(key);
                String name = LOOKUP.getString(key)
                                        .toLowerCase(Locale.getDefault());
                String[] stuff = name.split(COLON);
                REVERSE_LOOKUP.put(name, number);
                REVERSE_LOOKUP.put(stuff[0].trim(), number);
            }
        }
        // a couple of special cases
        REVERSE_LOOKUP.put("ICMPv6", ICMPv6_CODE);
        REVERSE_LOOKUP.put("icmpv6", ICMPv6_CODE);
    }


    /** Our self-trimming cache. */
    private static final WeakValueCache<String, IpProtocol> CACHED_PROTOCOLS =
                     new WeakValueCache<String, IpProtocol>(getRefQ());

    /** Ensures that all equivalent IP protocol encoding keys
     * map to the same instance of IpProtocol.
     * <p>
     * Note that this method is always called from inside
     * a block synchronized on {@link #CACHED_PROTOCOLS}.
     *
     * @param ipp a newly constructed IpProtocol (which may get dropped)
     * @param key the lookup key
     * @return a reference to the appropriate unique IpProtocol instance
     */
    private static IpProtocol intern(IpProtocol ipp, String key) {
        final String canon = String.valueOf(ipp.number);
        IpProtocol alreadyCached = CACHED_PROTOCOLS.get(canon);
        IpProtocol keeper = alreadyCached != null ? alreadyCached : ipp;
        CACHED_PROTOCOLS.put(canon, keeper); // cached by normalized string rep
        CACHED_PROTOCOLS.put(key, keeper); // cached by given key
        return keeper;
    }

    // === PUBLIC static API ==================================================

    /** The instance representing TCP. */
    public static final IpProtocol TCP = valueOf("tcp");
    /** The instance representing UDP. */
    public static final IpProtocol UDP = valueOf("udp");
    /** The instance representing SCTP. */
    public static final IpProtocol SCTP = valueOf("sctp");
    /** The instance representing ICMP. */
    public static final IpProtocol ICMP = valueOf("ICMP");
    /** The instance representing ICMPv6. */
    public static final IpProtocol ICMPv6 = valueOf("IPv6-ICMP");

    /** Returns an object that represents the value of the IP Protocol
     * identified by the specified string. For example:
     * <pre>
     *   IpProtocol tcpProto = IpProtocol.valueOf("tcp");
     *   String s = tcpProto.getName(); // "TCP : Transmission Control Protocol"
     *   IpProtocol tcpAlso = IpProtocol.valueOf(s);
     *   assert tcpAlso.equals(tcpProto);
     * </pre>
     *
     * @param value the protocol identifier
     * @return an object representing the specified protocol
     */
    public static IpProtocol valueOf(String value) {
        if (value == null)
            throw new NullPointerException("value cannot be null");

        final String valueLc = value.toLowerCase(Locale.getDefault());
        if (!REVERSE_LOOKUP.containsKey(valueLc))
            throw new IllegalArgumentException("unknown value: " + value);

        final int protocolNumber = REVERSE_LOOKUP.get(valueLc);

        synchronized (CACHED_PROTOCOLS) {
            IpProtocol result = CACHED_PROTOCOLS.get(valueLc);
            return (result == null) ?
                    intern(new IpProtocol(protocolNumber), valueLc) : result;
        } // sync
    }

    /** Returns an object that represents the value of the IP Protocol
     * identified by the specified protocol number.
     *
     * @param protocolNumber the IP Protocol number
     * @return an object representing the specified protocol
     * @throws IllegalArgumentException if the parameter is &lt; 0 or &gt; 255
     */
    public static IpProtocol valueOf(int protocolNumber) {
        if (protocolNumber < 0 || protocolNumber > 255)
            throw new IllegalArgumentException("Bad protocol number: " +
                                                protocolNumber);

        final String key = String.valueOf(protocolNumber);
        synchronized (CACHED_PROTOCOLS) {
            IpProtocol result = CACHED_PROTOCOLS.get(key);
            return (result == null) ?
                    intern(new IpProtocol(protocolNumber), key) : result;
        } // sync
    }

}
