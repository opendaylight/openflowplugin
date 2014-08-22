/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.math.BigInteger;
import java.util.*;

/**
 * Provides common constants and utility methods.
 *
 * @author Simon Hunt
 */
public class CommonUtils {

    /** Number of spaces to indent for toDebugString() implementations. */
    public static final int INDENT_SIZE = 2;

    /** Default indentation for toDebugString() implementations. */
    public static final String INDENT = "  ";

    /** Combined EOL and spaced indent for toDebugString() implementations. */
    public static final String EOLI = StringUtils.EOL + INDENT;


    /** Null parameter(s) exception message. */
    public static final String E_NULL_PARAMS = "Null parameter(s)";

    /** "Already contains" exception message. */
    public static final String E_CONTAINS = "e_contains=Already contains: ";

    // no instantiation
    private CommonUtils() { }

    /**
     * Returns the given long as a string in hex form.  For example:
     * <pre>
     * String s = hex(48);   // "0x30"
     * </pre>
     *
     * @param value the value
     * @return the value in hex form
     */
    public static String hex(long value) {
        return "0x" + Long.toHexString(value);
    }

    /**
     * Returns the given int as a string in hex form.  For example:
     * <pre>
     * String s = hex(64);    // "0x40"
     * </pre>
     *
     * @param value the value
     * @return the value in hex form
     */
    public static String hex(int value) {
        return "0x" + Integer.toHexString(value);
    }

    /**
     * Converts a hex string with an optional "0x" prefix into an int. The hex
     * string is a string representation of an unsigned integer in base 16.
     * For example:
     * <pre>
     * int m = parseHexInt(&quot;0xff&quot;); // 255
     * int n = parseHexInt(&quot;ff&quot;);   // 255
     * </pre>
     *
     * @param hex a string representing a hexadecimal number with an optional
     *            "0x" prefix
     * @return the value as an int
     * @throws NumberFormatException if the hex string is not formatted
     * correctly
     */
    public static int parseHexInt(String hex) {
        return Integer.parseInt(hex.toLowerCase().replaceFirst("0x", ""), 16);
    }

    /**
     * Converts a hex string with an optional "0x" prefix into a long. The hex
     * string is a string representation of an unsigned long in base 16.
     * For example:
     * <pre>
     * long m = parseHexInt(&quot;0xff&quot;); // 255L
     * long n = parseHexInt(&quot;ff&quot;);   // 255L
     * </pre>
     *
     * @param hex a string representing a hexadecimal number with an optional
     *            "0x" prefix
     * @return the value as a long
     * @throws NumberFormatException if the hex string is not formatted
     * correctly
     */
    public static long parseHexLong(String hex) {
        return new BigInteger(hex.toLowerCase().replaceFirst("0x", ""), 16).longValue();
    }

    /**
     * Returns the size of the specified collection.
     * If null is specified, zero is returned.
     *
     * @param c the collection
     * @return the collection's size
     */
    public static int cSize(Collection<?> c) {
        return c == null ? 0 : c.size();
    }

    /**
     * Returns the size of the specified array.
     * If null is specified, zero is returned.
     *
     * @param array the array
     * @return the array's size
     */
    public static <T> int aSize(T[] array) {
        return array == null ? 0 : array.length;
    }

    /**
     * Returns the size of the specified array.
     * If null is specified, zero is returned.
     *
     * @param array the array
     * @return the array's size
     */
    public static int aSize(byte[] array) {
        return array == null ? 0 : array.length;
    }

    // =====================================================================
    // These methods verify something and silently return if all is well.
    //  If something is amiss, an exception is thrown.

    /**
     * Verifies that arguments are not null.
     * Throws a NullPointerException if one (or more) is.
     *
     * @param objects the objects to test
     * @throws NullPointerException if any argument is null
     */
    public static void notNull(Object... objects) {
        for (Object o : objects)
            if (o == null)
                throw new NullPointerException(E_NULL_PARAMS);
    }

    /**
     * Verifies that the given collection does not already contain
     * the specified object.
     *
     * @param coll the collection
     * @param o    the object
     * @throws IllegalArgumentException if the collection contains the object
     */
    public static void notContains(Collection<?> coll, Object o) {
        if (coll != null && coll.contains(o))
            throw new IllegalArgumentException(E_CONTAINS + o);
    }


    /**
     * Verifies that the given string will fit in the given field size.
     * The string length must be {@code fieldSize - 1} or less (we need
     * a byte for the null terminator).
     *
     * @param str       the string to test (may be null)
     * @param fieldSize the size of field to fit the string into
     * @throws IllegalArgumentException if the string will not fit the field
     */
    public static void stringField(String str, int fieldSize) {
        if (str != null && str.length() >= fieldSize)
            throw new IllegalArgumentException("String must be " +
                    (fieldSize - 1) + " characters or fewer");
    }

    /**
     * Produces a set of items from the supplied items.
     *
     * @param items items to be placed in set
     * @param <T> item type
     * @return set of items
     */
    @SafeVarargs
    public static <T> Set<T> itemSet(T... items) {
        return new HashSet<>(Arrays.asList(items));
    }

    /**
     * Produces a list of items from the supplied items.
     *
     * @param items items to be placed in list
     * @param <T> item type
     * @return list of items
     */
    @SafeVarargs
    public static <T> List<T> itemList(T... items) {
        return Arrays.asList(items);
    }

    /**
     * Provides a short-hand for safe binding of a source to target.
     * If target is null, source will be returned; otherwise target
     * will be returned.
     *
     * @param source source entity
     * @param target target entity
     * @param <T> entity type
     * @return source if target is null; otherwise target
     */
    public static <T> T safeBind(T source, T target) {
        return (target == null ? source : target);
    }

    /**
     * Provides a short-hand for safe unbinding of a source to target.
     *
     * @param source source entity
     * @param target target entity
     * @param <T> entity type
     * @return null if target is the same as source; otherwise target
     */
    public static <T> T safeUnbind(T source, T target) {
        return (target == source ? null : target);
    }

}
