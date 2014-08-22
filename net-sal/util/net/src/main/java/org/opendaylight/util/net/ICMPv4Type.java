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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an ICMPv4 message type.
 * <p>
 * All constructors for this class are private. Creating instances of
 * {@code ICMPv4Type} is done via the static methods on the class.
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
public final class ICMPv4Type extends CacheableDataType
                        implements Comparable<ICMPv4Type> {

    private static final long serialVersionUID = 5982083693019135467L;

    private static final ResourceBundle LOOKUP =
            ResourceUtils.getBundledResource(ICMPv4Type.class, "icmpv4Type");

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
     * Constructs an ICMPv4 type instance from the type code.
     * Validation of the type code is done in the static method.
     * @param code the type code
     * @see #valueOf(int)
     */
    private ICMPv4Type(int code) {
        this.code = code;
        String s;
        try {
            s = LOOKUP.getString(String.valueOf(code));
            unknown = false;
        } catch (MissingResourceException e) {
            ReservedRange range = matchRange(code);
            if (range != null) {
                s = range.desc;
            } else {
                s = UNKNOWN;
                unknown = true;
            }
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

    /** Returns a string representing the ICMPv4 message type.
     *
     * @return the message type code and name
     */
    @Override
    public String toString() {
        return code + "(" + name + ")";
    }

    /** Returns the description of the ICMPv4 message type.
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

    /** Returns the ICMPv4 message type code.
     *
     * @return the message type code
     */
    public int getCode() {
        return code;
    }

    /** Returns true if the message type code is not one of the "named"
     * types. More specifically, the type is not defined in the
     * ICMPv4Type resource bundle and is not {@link #isReserved reserved}.
     *
     * @return true if unknown
     */
    public boolean isUnknown() {
        return unknown;
    }

    /** Returns true if the message type code is one of the reserved
     * values.
     *
     * @return true if this message type is a reserved value
     */
    public boolean isReserved() {
        boolean result = false;
        for (ReservedRange range: RANGES)
            if (range.hit(code)) {
                result = true;
                break;
            }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return code == ((ICMPv4Type) o).code;
    }

    @Override
    public int hashCode() {
        return code;
    }

    /** Implements the Comparable interface, to return ICMPv4 message types in
     * natural order.
     *
     * @param o the other IP Protocol instance
     * @return an integer value indicating relative ordering
     * @see Comparable#compareTo
     */
    @Override
    public int compareTo(ICMPv4Type o) {
        return this.code - o.code;
    }

    // === STATIC methods =====================================================

    // === tiny class to represent reserved ranges
    private static class ReservedRange {
        private int first;
        private int last;
        private String desc;

        ReservedRange(int first, int last, String desc) {
            this.first = first;
            this.last = last;
            this.desc = desc;
        }
        private boolean hit(int code) {
            return code >= first && code <= last;
        }
    }

    /** Reverse lookup map from the properties file.
     * For each (numeric) entry in the properties file there are two mappings;
     * the full string and the (short) name (both converted to lower case).
     * For example:
     * <ul>
     * <li> "dest_unr : destination unreachable" -&gt; 3 </li>
     * <li> "dest_unr" -&gt; 3 </li>
     * </ul>
     */
    private static final Map<String, Integer> REVERSE_LOOKUP =
            new HashMap<String,Integer>();

    /** Listed reserved code ranges. */
    private static final List<ReservedRange> RANGES =
            new ArrayList<ReservedRange>();

    private static final Pattern RE_DIGITS = Pattern.compile("\\d+");
    private static final Pattern RE_RESERVED =
            Pattern.compile("RES_(\\d+)_(\\d+)");

    /** Static initialization of reverse lookup map, and ranges */
    // look in icmpv4Type.properties for details...
    static {
        for (String key: LOOKUP.keySet()) {
            if (RE_DIGITS.matcher(key).matches()) {
                int number = Integer.valueOf(key);
                String name = LOOKUP.getString(key)
                        .toLowerCase(Locale.getDefault());
                String[] stuff = name.split(COLON);
                REVERSE_LOOKUP.put(name, number);
                REVERSE_LOOKUP.put(stuff[0].trim(), number);
            } else {
                Matcher m = RE_RESERVED.matcher(key);
                if (m.matches()) {
                    int first = Integer.valueOf(m.group(1));
                    int last = Integer.valueOf(m.group(2));
                    RANGES.add(new ReservedRange(first, last,
                                                 LOOKUP.getString(key)));
                }
            }
        }
    }

    /** Returns the reserved range that matches the given code;
     * or null for no match.
     *
     * @param code the code to lookup
     * @return the reserved range
     */
    private static ReservedRange matchRange(int code) {
        ReservedRange range = null;
        for (ReservedRange r: RANGES)
            if (r.hit(code)) {
                range = r;
                break;
            }
        return range;
    }

    /** Our self-trimming cache. */
    private static final WeakValueCache<String, ICMPv4Type> CACHED_TYPES =
            new WeakValueCache<String, ICMPv4Type>(getRefQ());

    /** Ensures that all equivalent ICMPv4 message type encoding keys map
     * to the same instance of ICMPv4Type.
     * <p>
     * Note that this method is always called from inside a
     * block synchronized on {@link #CACHED_TYPES}.
     *
     * @param it a newly constructed ICMPv4Type (which may get dropped)
     * @param key the lookup key
     * @return a reference to the appropriate unique ICMPv4Type instance
     */
    private static ICMPv4Type intern(ICMPv4Type it, String key) {
        final String canon = String.valueOf(it.code);
        ICMPv4Type alreadyCached = CACHED_TYPES.get(canon);
        ICMPv4Type keeper = alreadyCached != null ? alreadyCached : it;
        CACHED_TYPES.put(canon, keeper); // cached by normalized string rep
        CACHED_TYPES.put(key, keeper); // cached by given key
        return keeper;
    }

    // === PUBLIC static API ================================================

    /** The instance representing Echo request. */
    public static final ICMPv4Type ECHO_REQ = valueOf("ECHO_REQ");
    /** The instance representing Echo reply. */
    public static final ICMPv4Type ECHO_REP = valueOf("ECHO_REP");


    /** Returns an object that represents the value of the ICMPv4 message type
     * identified by the specified string. For example:
     * <pre>
     *   ICMPv4Type rs = ICMPv4Type.valueOf("rtr_sol");
     *   String s = rs.getDescription(); // "RTR_SOL : Router Solicitation"
     *   ICMPv4Type rsAlso = ICMPv4Type.valueOf(s);
     *   assert rsAlso.equals(rs);
     * </pre>
     *
     * @param value the message type identifier
     * @return an object representing the specified message type
     */
    public static ICMPv4Type valueOf(String value) {
        if (value == null)
            throw new NullPointerException("value cannot be null");

        final String valueLc = value.toLowerCase(Locale.getDefault());
        if (!REVERSE_LOOKUP.containsKey(valueLc))
            throw new IllegalArgumentException("unknown value: " + value);

        final int typeCode = REVERSE_LOOKUP.get(valueLc);

        synchronized (CACHED_TYPES) {
            ICMPv4Type result = CACHED_TYPES.get(valueLc);
            return (result == null) ?
                    intern(new ICMPv4Type(typeCode), valueLc) : result;

        } // sync
    }

    /** Returns an object that represents the value of the ICMPv4 message type
     * identified by the specified type code.
     *
     * @param typeCode the ICMPv4 message type code
     * @return an object representing the specified protocol
     * @throws IllegalArgumentException if the parameter is &lt; 0 or &gt; 255
     */
    public static ICMPv4Type valueOf(int typeCode) {
        if (typeCode < 0 || typeCode > 255)
            throw new IllegalArgumentException("Bad ICMPv4 type code:" +
                                               typeCode);

        final String key = String.valueOf(typeCode);
        synchronized (CACHED_TYPES) {
            ICMPv4Type result = CACHED_TYPES.get(key);
            return (result == null) ?
                    intern(new ICMPv4Type(typeCode), key) : result;
        } // sync
    }
}
