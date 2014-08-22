/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.math.BigInteger;

/**
 * Provides useful methods regarding primitives.
 * <p>
 * Some protocols deal with unsigned values; for example, <em>u8</em>
 * (or {@code uint8_t} in C/C++) refers to an unsigned 8-bit value.
 * Java does not provide unsigned primitives, so some care needs to be taken
 * when dealing with such values in Java.
 * <p>
 * As an example, consider the following 8-bit value:
 * <pre>
 *     10010011
 * </pre>
 * Interpreted as an unsigned value (u8) this represents 147.
 * A Java {@code byte} can hold an 8-bit value, but Java bytes
 * are signed (the most significant bit is the sign-bit) so this same
 * collection of bits, stored in a {@code byte}, represents -109.
 * <p>
 * To interpret a u8 value correctly, it needs to be stored in the
 * "next size up" primitve (short) so that all 8 bits are unsigned:
 * <pre>
 *     0000000010010011
 *     ^
 *     sign bit
 * </pre>
 * This short value represents 147.
 * <p>
 * The <em>fromU*()</em> methods in this class treat their
 * parameter as an unsigned number and return the equivalent
 * unsigned value in the "next size up" primitive.
 * The <em>toU*</em> methods in this class treat their parameter
 * as an unsigned number and return the equivalent
 * value "encoded" in the "next size down" primitive.
 *
 * @author Simon Hunt
 */
public final class PrimitiveUtils {

    private static final short UBYTE_MAX = Byte.MAX_VALUE * 2 + 1;
    private static final int USHORT_MAX = Short.MAX_VALUE * 2 + 1;
    private static final long UINT_MAX = (long)Integer.MAX_VALUE * 2 + 1;
    private static final BigInteger ULONG_MAX =
            new BigInteger(Long.toBinaryString(-1), 2);

    private static final String E_VOOB = "Value out of bounds: ";


    // no instantiation
    private PrimitiveUtils() { }


    /** Convert the byte equivalent of an unsigned 8-bit field to a short.
     *
     * @param b a byte
     * @return unsigned value
     */
    public static short fromU8(byte b) {
        return (short) (b & 0xff);
    }

    /** Verifies that the given value will fit in an unsigned 8-bit field.
     * If it will, this method silently returns; if not, an exception is
     * thrown.
     *
     * @param uv unsigned value
     * @throws IllegalArgumentException if uv is not 0..2^8-1
     */
    public static void verifyU8(short uv) {
        if (uv < 0 || uv > UBYTE_MAX)
            throw new IllegalArgumentException(E_VOOB + "(u8): " + uv);
    }

    /** Convert the given value to the byte equivalent of an
     * unsigned 8-bit field.
     *
     * @param uv unsigned value
     * @return encoded byte
     * @throws IllegalArgumentException if uv is not 0..2^8-1
     */
    public static byte toU8(short uv) {
        verifyU8(uv);
        return (byte) uv;
    }

    /** Verifies that the given value will fit in an unsigned 8-bit field.
     * If it will, this method silently returns; if not, an exception is
     * thrown.
     *
     * @param uv unsigned value
     * @throws IllegalArgumentException if uv is not 0..2^8-1
     */
    public static void verifyU8(int uv) {
        if (uv < 0 || uv > UBYTE_MAX)
            throw new IllegalArgumentException(E_VOOB + "(u8): " + uv);
    }

    /** Convert the given value to the byte equivalent of an
     * unsigned 8-bit field.
     *
     * @param uv unsigned value
     * @return encoded byte
     * @throws IllegalArgumentException if uv is not 0..2^8-1
     */
    public static byte toU8(int uv) {
        verifyU8(uv);
        return (byte) uv;
    }

    /** Convert the short equivalent of an unsigned 16-bit field to an int.
     *
     * @param s a short
     * @return unsigned value
     */
    public static int fromU16(short s) {
        return s & 0xffff;
    }

    /** Verifies that the given value will fit in an unsigned 16-bit field.
     * If it will, this method silently returns; if not, an exception is
     * thrown.
     *
     * @param uv unsigned value
     * @throws IllegalArgumentException if uv is not 0..2^16-1
     */
    public static void verifyU16(int uv) {
        if (uv < 0 || uv > USHORT_MAX)
            throw new IllegalArgumentException(E_VOOB + "(u16): " + uv);
    }
    /** Convert the given value to the short equivalent of an unsigned
     * 16-bit field.
     *
     * @param uv unsigned value
     * @return encoded short
     * @throws IllegalArgumentException if uv is not 0..2^16-1
     */
    public static short toU16(int uv) {
        verifyU16(uv);
        return (short) uv;
    }

    /** Convert the int equivalent of an unsigned 32-bit field to a long.
     *
     * @param i an int
     * @return unsigned value
     */
    public static long fromU32(int i) {
        return i & 0xffffffffL;
    }

    /** Verifies that the given value will fit in an unsigned 32-bit field.
     * If it will, this method silently returns; if not, an exception is
     * thrown.
     *
     * @param uv unsigned value
     * @throws IllegalArgumentException if uv is not 0..2^32-1
     */
    public static void verifyU32(long uv) {
        if (uv < 0 || uv > UINT_MAX)
            throw new IllegalArgumentException(E_VOOB + "(u32): " + uv);
    }

    /** Convert the given value to the int equivalent of an unsigned
     * 32-bit field.
     *
     * @param uv unsigned value
     * @return encoded int
     * @throws IllegalArgumentException if uv is not 0..2^32-1
     */
    public static int toU32(long uv) {
        verifyU32(uv);
        return (int) uv;
    }

    /** Convert the long equivalent of an unsigned 64-bit field to a BigInteger.
     *
     * @param l a long
     * @return unsigned value
     */
    public static BigInteger fromU64(long l) {
        return new BigInteger(Long.toBinaryString(l), 2);
    }

    /** Verifies that the given value will fit in an unsigned 64-bit field.
     * If it will, this method silently returns; if not, an exception is
     * thrown.
     *
     * @param uv unsigned value
     * @throws IllegalArgumentException if uv is not 0..2^64-1
     */
    public static void verifyU64(BigInteger uv) {
        if (uv.signum() == -1 || uv.compareTo(ULONG_MAX) > 0)
            throw new IllegalArgumentException(E_VOOB + "(u64): " + uv);
    }

    /** Convert the given value to the long equivalent of an unsigned
     * 64-bit field.
     *
     * @param uv unsigned value
     * @return encoded long
     * @throws IllegalArgumentException if uv is not 0..2^64-1
     */
    public static long toU64(BigInteger uv) {
        verifyU64(uv);
        return uv.longValue();
    }

}
