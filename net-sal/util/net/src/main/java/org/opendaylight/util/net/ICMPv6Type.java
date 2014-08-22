/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
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
 * Represents an ICMPv6 message type.
 * <p>
 * All constructors for this class are private. Creating instances of
 * {@code ICMPv6Type} is done via the static methods on the class.
 * <p>
 * Instances of this class are immutable, making them inherently threadsafe.
 * <p>
 * This class overrides {@link #equals} and {@link #hashCode} so that instances
 * play nicely with the Collection classes.
 * <p>
 * This class implements the {@link Comparable} interface to ensure that a
 * sorted list of types is presented in an intuitive order.
 *
 * @author Simon Hunt
 */
public final class ICMPv6Type extends CacheableDataType
                        implements Comparable<ICMPv6Type> {

    private static final long serialVersionUID = -6639155826680711087L;

    private static final ResourceBundle LOOKUP =
            ResourceUtils.getBundledResource(ICMPv6Type.class, "icmpv6Type");

    private static final String UNKNOWN = LOOKUP.getString("UNKNOWN");
    private static final String COLON = ":";

    /** The type code.
     * @serial type code
     */
    private final int code;

    /** The (short) name for this message type. */
    private transient String name;
    /** The description of this message type. */
    private transient String desc;
    /** True if the type is not "named" in the resource bundle. */
    private transient boolean unknown;

    // === PRIVATE CONSTRUCTORS ==============================================

    /**
     * Constructs an ICMPv6 type instance from the type code.
     * Validation of the type code is done in the static method.
     * @param code the type code
     * @see #valueOf(int)
     */
    private ICMPv6Type(int code) {
        this.code = code;
        String s;
        try {
            s = LOOKUP.getString(String.valueOf(code));
            unknown = false;
        } catch (MissingResourceException e) {
            s = UNKNOWN;
            unknown = true;
        }
        desc = s;
        String[] stuff = s.split(COLON);
        name = stuff[0].trim();
    }

    // === PRIVATE serialization ==============================================

    //== Implementation note:
    //      Default deserialization will set the serialized field (code) to
    //      whatever was in the stream, and will set the transient fields to
    //      defaults (null). This is good enough for our purposes, because we
    //      will simply use valueOf() to (create and?) return the
    //      cached instance.


    private Object readResolve() throws ObjectStreamException {
        // when this is called, code has been populated.
        // return the appropriate cached instance instead.
        Object o;
        try {
            o = valueOf(code);
        } catch (IllegalArgumentException e) {
            throw new InvalidObjectException(e.getMessage());
        }
        return o;
    }

    // === PUBLIC instance API ================================================

    /** Returns a string representing the ICMPv6 message type.
     *
     * @return the message type code and name
     */
    @Override
    public String toString() {
        return code + "(" + name + ")";
    }

    /** Returns the description of the ICMPv6 message type.
     * E.g. "DEST_UNR : Destination unreachable"
     *
     * @return the description
     */
    public String getDescription() {
        return desc;
    }

    /** Returns the (short) name of the message type.
     *  E.g. "DEST_UNR"
     *
     * @return the (short) name
     */
    public String getName() {
        return name;
    }

    /** Returns the ICMPv6 message type code.
     *
     * @return the message type code
     */
    public int getCode() {
        return code;
    }

    /** Returns true if the message type code is not one of the "named"
     * types. More specifically, the type is not defined in the
     * icmpv6Type resource bundle.
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
        return code == ((ICMPv6Type) o).code;
    }

    @Override
    public int hashCode() {
        return code;
    }

    /** Implements the Comparable interface, to return ICMPv6 message types in
     * natural order.
     *
     * @param o the other ICMPv6 message type instance
     * @return an integer value indicating relative ordering
     * @see Comparable#compareTo
     */
    @Override
    public int compareTo(ICMPv6Type o) {
        return this.code - o.code;
    }

    // === STATIC methods =====================================================

    /** Reverse lookup map from the properties file.
     * For each (numeric) entry in the properties file there are two mappings;
     * the full string and the (short) name (both converted to lower case).
     * For example:
     * <ul>
     * <li> "dest_unr : destination unreachable" -&gt; 1 </li>
     * <li> "dest_unr" -&gt; 1 </li>
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
    }

    /** Our self-trimming cache. */
    private static final WeakValueCache<String, ICMPv6Type> CACHED_TYPES =
            new WeakValueCache<String, ICMPv6Type>(getRefQ());

    /** Ensures that all equivalent ICMPv6 message type encoding keys map
     * to the same instance of ICMPv6Type.
     * <p>
     * Note that this method is always called from inside a
     * block synchronized on {@link #CACHED_TYPES}.
     *
     * @param it a newly constructed ICMPv6Type (which may get dropped)
     * @param key the lookup key
     * @return a reference to the appropriate unique ICMPv6Type instance
     */
    private static ICMPv6Type intern(ICMPv6Type it, String key) {
        final String canon = String.valueOf(it.code);
        ICMPv6Type alreadyCached = CACHED_TYPES.get(canon);
        ICMPv6Type keeper = alreadyCached != null ? alreadyCached : it;
        CACHED_TYPES.put(canon, keeper); // cached by normalized string rep
        CACHED_TYPES.put(key, keeper); // cached by given key
        return keeper;
    }

    // === PUBLIC static API ================================================

    /** The instance representing Neighbor Solicitation. */
    public static final ICMPv6Type NBR_SOL = valueOf("NBR_SOL");
    /** The instance representing Neighbor Advertisement. */
    public static final ICMPv6Type NBR_ADV = valueOf("NBR_ADV");


    /** Returns an object that represents the value of the ICMPv6 message type
     * identified by the specified string. For example:
     * <pre>
     *   ICMPv6Type rs = ICMPv6Type.valueOf("rtr_sol");
     *   String s = rs.getDescription(); // "RTR_SOL : Router Solicitation"
     *   ICMPv6Type rsAlso = ICMPv6Type.valueOf(s);
     *   assert rsAlso.equals(rs);
     * </pre>
     *
     * @param value the message type identifier
     * @return an object representing the specified message type
     */
    public static ICMPv6Type valueOf(String value) {
        if (value == null)
            throw new NullPointerException("value cannot be null");

        final String valueLc = value.toLowerCase(Locale.getDefault());
        if (!REVERSE_LOOKUP.containsKey(valueLc))
            throw new IllegalArgumentException("unknown value: " + value);

        final int typeCode = REVERSE_LOOKUP.get(valueLc);

        synchronized (CACHED_TYPES) {
            ICMPv6Type result = CACHED_TYPES.get(valueLc);
            return (result == null) ?
                    intern(new ICMPv6Type(typeCode), valueLc) : result;

        } // sync
    }

    /** Returns an object that represents the value of the ICMPv6 message type
     * identified by the specified type code.
     *
     * @param typeCode the ICMPv6 message type code
     * @return an object representing the specified protocol
     * @throws IllegalArgumentException if the parameter is &lt; 0 or &gt; 255
     */
    public static ICMPv6Type valueOf(int typeCode) {
        if (typeCode < 0 || typeCode > 255)
            throw new IllegalArgumentException("Bad ICMPv6 type code:" +
                                               typeCode);

        final String key = String.valueOf(typeCode);
        synchronized (CACHED_TYPES) {
            ICMPv6Type result = CACHED_TYPES.get(key);
            return (result == null) ?
                    intern(new ICMPv6Type(typeCode), key) : result;
        } // sync
    }
}
