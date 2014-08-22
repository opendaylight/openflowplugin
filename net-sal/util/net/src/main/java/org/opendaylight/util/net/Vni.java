/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.net;

import java.util.Locale;

/**
 * Represents a Virtual Network Identifier, which is an unsigned, 24-bit value.
 * <p>
 * All constructors for this class are private. Creating instances of
 * {@code Vni} is done via the static methods on the class.
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
 * @author Jesse Hummer
 * @author Scott Simes
 */
public class Vni implements Comparable<Vni> {

    private static final String E_VOOB = "Value out of bounds (not u24): ";
    private static final String OX = "0x";

    /**
     * The maximum permissible value ({@value #MAX_VALUE}).
     */
    public static final int MAX_VALUE = 0xffffff;
    /**
     * The minimum permissible value ({@value #MIN_VALUE}).
     */
    public static final int MIN_VALUE = 0x0;

    private final int value;

    private Vni(int value) {
        this.value = value;
    }

    /**
     * Returns a string representation of this Virtual Network Identifier, as
     * a decimal number.
     *
     * @return the VNI as a string
     */
    @Override
    public String toString() {
        return Integer.toString(value);
    }

    /**
     * Returns the Virtual Network Identifier represented as an integer.
     *
     * @return the VNI encoded as an int
     */
    public int toInt() {
        return value;
    }

    private static void validate(int value) {
        if (value < MIN_VALUE || value > MAX_VALUE)
            throw new IllegalArgumentException(E_VOOB + "(u24): " + value);
    }

    /**
     * Returns a Virtual Network Identifier instance representing the
     * value given by the specified integer.
     *
     * @param value the value
     * @return the Virtual Network Identifier for the given value
     * @throws IllegalArgumentException if value is not u24
     */
    public static Vni valueOf(int value) {
        validate(value);
        return new Vni(value);
    }

    /**
     * A convenience method that simply delegates to {@link #valueOf(int)}.
     * <p>
     * Included so that this method may be statically imported, such that it
     * is easy to express VNIs in code:
     * <pre>
     *     import static org.opendaylight.util.ip.Vni.vni;
     *     ...
     *     Vni v = vni(3);
     * </pre>
     *
     * @param value the value
     * @return the Virtual Network Identifier for the given value
     * @throws IllegalArgumentException if value is not u24
     */
    public static Vni vni(int value) {
        return valueOf(value);
    }

    /**
     * Returns a Virtual Network Identifier instance representing the
     * value given by the specified string. Recognized formats are decimal
     * or hex (prefixed with "0x") strings.
     * <p>
     * The following examples return a VNI of 23:
     * <pre>
     *     Vni v1 = Vni.valueOf("23");
     *     Vni v2 = Vni.valueOf("0x17");
     * </pre>
     *
     * @param value a string representing a number
     * @return the Virtual Network Identifier for the given value
     * @throws NullPointerException if value is null
     * @throws IllegalArgumentException if value is not parsable as a u24 value
     */
    public static Vni valueOf(String value) {
        final String vstr = value.toLowerCase(Locale.getDefault()).trim();
        int v;
        if (vstr.startsWith(OX))
            v = Integer.parseInt(vstr.substring(2), 16);
        else
            v = Integer.parseInt(value);
        return valueOf(v);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vni vni = (Vni) o;
        return value == vni.value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public int compareTo(Vni o) {
        return this.value - o.value;
    }
}
