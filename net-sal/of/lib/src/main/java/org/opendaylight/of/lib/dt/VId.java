/*
 * (c) Copyright 2012-2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.dt;

import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.net.U16Id;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Represents a virtual identifier (unsigned 16-bit).
 * <p>
 * Creating instances of {@code VId} is done via the static methods
 * of the class, or by using the predefined constants
 * {@link #NONE} or {@link #PRESENT}.
 * <p>
 * Instances of this class are immutable, making them inherently threadsafe.
 * <p>
 * This class overrides {@link #equals} and {@link #hashCode} so that
 * instances play nicely with the Collection classes.
 * <p>
 * This class implements the {@link Comparable} interface to ensure that
 * a sorted list of virtual identifiers is presented in an intuitive order.
 *
 * @author Simon Hunt
 */
public final class VId extends U16Id implements Comparable<VId> {

    private static final long serialVersionUID = -8941835944461424291L;

    private VId(int id) {
        super(id);
    }

    /**
     * Implements the Comparable interface, to return virtual IDs in
     * natural order.
     *
     * @param o the other ID
     * @return an integer value indicating relative ordering
     * @see Comparable#compareTo
     */
    @Override
    public int compareTo(VId o) {
        return this.id - o.id;
    }

    @Override
    public String toString() {
        String special = specialName(this);
        return special != null ? special : super.toString();
    }

    /** 
     * Returns an object that represents the virtual ID defined by 
     * the specified integer.
     *
     * @param vid the virtual ID number
     * @return the corresponding virtual ID instance
     * @throws IllegalArgumentException if the ID number is invalid
     */
    public static VId valueOf(int vid) {
        rangeCheck(vid);
        return new VId(vid);
    }

    /** 
     * Returns an object that represents the virtual ID defined by the 
     * specified string. The string is parsed as a base-10 value, unless it
     * has a "0x" prefix, in which case it is parsed as a hex number.
     *
     * @param s the virtual ID number as a string
     * @return the corresponding virtual ID instance
     * @throws IllegalArgumentException if the string is invalid
     * @throws NullPointerException if the string is null
     */
    public static VId valueOf(String s) {
        VId v = specialFromString(s);
        if (v != null)
            return v;

        try {
            return valueOf(parseIntStr(s));
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(E_BAD + s, nfe);
        }
    }

    /**
     * Convenience method that returns the virtual ID for the given string.
     * This method simply delegates to {@link #valueOf(String)}. By using a
     * static import of this method, more concise code can be written. For
     * example, the following two statements are equivalent:
     * <pre>
     * VId v = VId.valueOf("8");
     * VId v = vid("8");
     * </pre>
     *
     * @param s the virtual ID number as a string
     * @return the corresponding virtual ID instance
     * @throws IllegalArgumentException if the string is invalid
     * @throws NullPointerException if the string is null
     */
    public static VId vid(String s) {
        return valueOf(s);
    }

    /** 
     * Returns an object that represents the virtual ID defined by the 
     * specified byte array. The array is expected to be 
     * {@link #LENGTH_IN_BYTES} bytes long.
     *
     * @param bytes the encoded ID
     * @return the corresponding virtual ID instance
     * @throws NullPointerException if the byte array is null
     * @throws IllegalArgumentException if the byte array is not 2 bytes long
     */
    public static VId valueOf(byte[] bytes) {
        if (bytes == null)
            throw new NullPointerException(E_NULL_BYTES);
        if (bytes.length != LENGTH_IN_BYTES)
            throw new IllegalArgumentException(E_BYTES_BAD_LEN);

        return valueOf(ByteUtils.getU16(bytes, 0));
    }

    /**
     * Returns the logical name of the given virtual ID if it is a
     * special value; null otherwise.
     *
     * @param v the value
     * @return its logical name
     */
    public static String specialName(VId v) {
        return SPECIAL.get(v);
    }

    /**
     * A special value denoting the absence of a virtual ID.
     */
    public static final VId NONE = new VId(-2);

    /**
     * A special value denoting the presence of a virtual ID, without
     * defining the specific value.
     */
    public static final VId PRESENT = new VId(-1);

    private static final Map<VId, String> SPECIAL = new HashMap<>();
    static {
        SPECIAL.put(NONE, "NONE");
        SPECIAL.put(PRESENT, "PRESENT");
    }

    // return matching VId instance, or null, for the given string
    private static VId specialFromString(String s) {
        for (Map.Entry<VId, String> entry: SPECIAL.entrySet())
            if (entry.getValue().equals(s.toUpperCase(Locale.getDefault())))
                return entry.getKey();
        return null;
    }
}
