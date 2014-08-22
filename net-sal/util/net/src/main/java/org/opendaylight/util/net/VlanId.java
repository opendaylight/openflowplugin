/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.net;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Represents a VLAN Identifier, which is an unsigned, 12-bit value.
 * There are two special values:
 * <ul>
 *     <li> VlanId.NONE - denoting the absence of a VLAN ID </li>
 *     <li> VlanId.PRESENT - denoting the presence of a VLAN ID, without
 *          specifying its value </li>
 * </ul>
 * <p>
 * All constructors for this class are private. Creating instances of
 * {@code VlanId} is done via the static methods on the class.
 * <p>
 * Instances of this class are immutable, making them inherently threadsafe.
 * <p>
 * This class overrides {@link #equals} and {@link #hashCode} so that instances
 * play nicely with the Collection classes.
 * <p>
 * This class implements the {@link Comparable} interface to ensure that
 * a sorted list of identifiers is presented in an intuitive order.
 *
 * @author Simon Hunt
 */
public class VlanId implements Comparable<VlanId> {

    private static final String E_VOOB = "Value out of bounds (not u12): ";
    private static final String OX = "0x";
    private static final int BYTE_ARRAY_SIZE = 2;

    /**
     * The maximum permissible value ({@value #MAX_VALUE}).
     */
    public static final int MAX_VALUE = 0xfff;
    /**
     * The minimum permissible value ({@value #MIN_VALUE}).
     */
    public static final int MIN_VALUE = 0x0;

    private final int value;

    private VlanId(int value) {
        this.value = value;
    }

    /**
     * Returns a string representation of this VLAN ID, as a decimal number.
     *
     * @return the VLAN ID as a string
     */
    @Override
    public String toString() {
        String special = specialName(this);
        return special != null ? special : Integer.toString(value);
    }

    /**
     * Returns the Virtual Network Identifier represented as an integer.
     *
     * @return the VNI encoded as an int
     */
    public int toInt() {
        return value;
    }

    /**
     * Writes this VLAN ID into the given {@link ByteBuffer} at the buffer's
     * (as a two byte value with the top four bits set to 0).
     *
     * @param b the byte buffer to write into
     */
    public void intoBuffer(ByteBuffer b) {
        b.putShort((short) value);
    }


    // throws exception if not u12
    private static void validate(int value) {
        if (value < MIN_VALUE || value > MAX_VALUE)
            throw new IllegalArgumentException(E_VOOB + value);
    }

    /**
     * Returns a VLAN ID instance representing the value given by the
     * specified integer.
     *
     * @param value the value
     * @return the VLAN ID for the given value
     * @throws IllegalArgumentException if value is not u12
     */
    public static VlanId valueOf(int value) {
        validate(value);
        return new VlanId(value);
    }

    /**
     * A convenience method that simply delegates to {@link #valueOf(int)}.
     * <p>
     * Included so that this method may be statically imported, such that it
     * is easy to express VLAN IDs in code:
     * <pre>
     *     import static org.opendaylight.util.ip.VlanId.vlan;
     *     ...
     *     VlanId v = vlan(3);
     * </pre>
     *
     * @param value the value
     * @return the VLAN ID for the given value
     * @throws IllegalArgumentException if value is not u12
     */
    public static VlanId vlan(int value) {
        return valueOf(value);
    }

    /**
     * Returns a VLAN ID instance representing the value given by the
     * specified string. Recognized formats are decimal or hex
     * (prefixed with "0x") strings, or the special values:
     * <ul>
     *     <li>"PRESENT"</li>
     *     <li>"NONE"</li>
     * </ul>
     * <p>
     * The following examples return a VLAN ID of 23:
     * <pre>
     *     VlanId v1 = VlanId.valueOf("23");
     *     VlanId v2 = VlanId.valueOf("0x17");
     * </pre>
     *
     * @param value a string representing a VLAN ID number
     * @return the VLAN ID for the given value
     * @throws NullPointerException if value is null
     * @throws IllegalArgumentException if value is not parsable as a u12 value
     */
    public static VlanId valueOf(String value) {
        VlanId vlan = specialFromString(value);
        if (vlan != null)
            return vlan;

        final String vstr = value.toLowerCase(Locale.getDefault()).trim();
        int v;
        if (vstr.startsWith(OX))
            v = Integer.parseInt(vstr.substring(2), 16);
        else
            v = Integer.parseInt(value);
        return valueOf(v);
    }

    /**
     * Reads 2 bytes from the specified byte buffer and creates a VLAN ID
     * entity from the value.
     *
     * @param buffer byte buffer from which to read the bytes
     * @return a VLAN ID instance
     * @throws BufferUnderflowException if the buffer does not have 2 bytes
     *          remaining
     * @throws IllegalArgumentException if the read value is not u12
     */
    public static VlanId valueFrom(ByteBuffer buffer) {
        return valueOf(buffer.getShort());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VlanId v = (VlanId) o;
        return value == v.value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public int compareTo(VlanId o) {
        return this.value - o.value;
    }

    // ===

    /**
     * Returns the logical name of the given VLAN ID if it is a special value;
     * null otherwise.
     *
     * @param v the value
     * @return its logical name
     */
    public static String specialName(VlanId v) {
        return SPECIAL.get(v);
    }

    /**
     * A special value denoting the absence of a VLAN ID.
     */
    public static final VlanId NONE = new VlanId(-2);

    /**
     * A special value denoting the presence of a VLAN ID, without defining
     * the specific value.
     */
    public static final VlanId PRESENT = new VlanId(-1);

    private static final Map<VlanId, String> SPECIAL = new HashMap<>();
    static {
        SPECIAL.put(NONE, "NONE");
        SPECIAL.put(PRESENT, "PRESENT");
    }

    // return matching VlanId instance, or null, for the given string
    private static VlanId specialFromString(String s) {
        for (Map.Entry<VlanId, String> entry: SPECIAL.entrySet())
            if (entry.getValue().equals(s.toUpperCase(Locale.getDefault())))
                return entry.getKey();
        return null;
    }

}
