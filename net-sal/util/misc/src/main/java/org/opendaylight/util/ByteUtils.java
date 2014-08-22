/*
 * (c) Copyright 2007-2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Locale;

import static org.opendaylight.util.StringUtils.EMPTY;
import static org.opendaylight.util.StringUtils.EOL;

/**
 * Set of utility methods for encoding and decoding information using
 * byte arrays.
 *
 * @author Thomas Vachuska
 * @author Frank Wood
 * @author Simon Hunt
 */
public final class ByteUtils {
    private static final int DISTINCT_BYTE_VALUES = 256;

    /**
     * Pre-computed array containing the number of 1 bits for each byte value.
     */
    private static byte[] BITS_IN_BYTE = null;

    static {
        byte[] bib = new byte[DISTINCT_BYTE_VALUES];
        for (int i = 0; i < DISTINCT_BYTE_VALUES; i++)
            bib[i] = (byte) countBitsInByte((byte) i);
        BITS_IN_BYTE = bib;
    }

    static int countBitsInByte(byte b) {
        return (((b >>> 7) & 0x1) + ((b >>> 6) & 0x1) +
                ((b >>> 5) & 0x1) + ((b >>> 4) & 0x1) +
                ((b >>> 3) & 0x1) + ((b >>> 2) & 0x1) +
                ((b >>> 1) & 0x1) + (b & 0x1));
    }

    /*
    * We want to create a really fast byte to string lookup
    */
    private static final String[] HEX_UPPER = new String[DISTINCT_BYTE_VALUES];
    private static final String[] HEX_LOWER = new String[DISTINCT_BYTE_VALUES];
    private static final int FAST_BYTE_OFFSET = -Byte.MIN_VALUE;

    // Auxiliary char array for fast conversion to hex.
    private static final char HEX[] = new char[] {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    static {
        for (int i=0; i<DISTINCT_BYTE_VALUES; i++) {
            byte b = (byte) (i - FAST_BYTE_OFFSET);
            String s = byteToHex(b);
            HEX_LOWER[i] = s;
            HEX_UPPER[i] = s.toUpperCase(Locale.getDefault());
        }
    }


    // used to help in expressing bytes (with sign bit set) concisely
    //  e.g. you can say   0xff-B   instead of casting with   (byte) 0xff
    private static final int B = 256;

    /** Ascii character set name. */
    private static final String ASCII =  "US-ASCII";
    /** UTF-8 character set name. */
    private static final String UTF8 =  "UTF-8";
    /** A dot. */
    private static final String DOT = ".";
    /** A slash. */
    private static final String SLASH = "/";
    /** A hex file suffix.*/
    private static final String DOT_HEX = ".hex";
    /** Our own String pool to cut down on duplicate string instances. */
    private static final StringPool SP = new StringPool();


    // no instantiation
    private ByteUtils() { }

    /** Converts a byte (-128 .. 127) to the equivalent int (0 .. 255).
     *
     * @param b the byte
     * @return the int value
     */
    public static int byteToInt(byte b) {
        return b < 0 ? B+b : b;
    }

    /** Converts an int (0 .. 255) to the equivalent byte (-128 .. 127)
     *
     * @param i the int
     * @return the byte value
     */
    public static byte intToByte(int i) {
        return (byte) (i>127 ? i-B : i);
    }

    /**
     * Extracts and returns the short value from two bytes of a byte array
     * starting at the given offset.
     *
     * @param b the byte array from which to extract the short value
     * @param offset the start offset into the byte array
     * @return the short value represented by the two bytes at the given offset
     */
    public static short getShort(byte[] b, int offset) {
        return (short) (((b[offset] << 8) & 0xff00) | (b[offset + 1] & 0x00ff));
    }

    /**
     * Populates two bytes of a byte array with the given short value.
     *
     * @param b the byte array into which to put the short value
     * @param offset the start offset into the byte array
     * @param value the value to be represented by the two bytes at the
     *              given offset
     */
    public static void setShort(byte[] b, int offset, short value) {
        b[offset    ] = (byte) ((value & 0xff00) >> 8);
        b[offset + 1] = (byte) (value & 0x00ff);
    }

    /**
     * Populates two bytes of a byte array with the lower two bytes
     * of the given integer value.
     *
     * @param b the byte array into which to put the lower half of the
     *          integer value
     * @param offset the start offset into the byte array
     * @param value the value to be represented by the two bytes at the
     *              given offset
     */
    public static void setShort(byte[] b, int offset, int value) {
        b[offset    ] = (byte) ((value & 0xff00) >> 8);
        b[offset + 1] = (byte) (value & 0x00ff);
    }

    /**
     * Extracts the integer value from four bytes of a byte array
     * starting at the given offset.
     *
     * @param b the byte array from which to extract the integer value
     * @param offset the start offset into the byte array
     * @return the integer value represented by the four bytes at the
     * given offset
     */
    public static int getInteger(byte[] b, int offset) {
        return ((b[offset    ] << 24) & 0xff000000)  |
                ((b[offset + 1] << 16) & 0x00ff0000) |
                ((b[offset + 2] << 8) & 0x0000ff00)  |
                ((b[offset + 3]) & 0x000000ff);
    }

    /**
     * Populates four bytes of a byte array with the given integer value.
     *
     * @param b the byte array into which to put the integer value
     * @param offset the start offset into the byte array
     * @param value the integer value to be represented by the four bytes at
     *              the given offset
     */
    public static void setInteger(byte[] b, int offset, int value) {
        b[offset    ] = (byte) ((value & 0xff000000) >> 24);
        b[offset + 1] = (byte) ((value & 0x00ff0000) >> 16);
        b[offset + 2] = (byte) ((value & 0x0000ff00) >> 8);
        b[offset + 3] = (byte) ((value & 0x000000ff));
    }

    /**
     * Populates four bytes of a byte array with the lower four
     * bytes of the given long value.
     *
     * @param b the byte array into which to put the lower half of the
     *          long value
     * @param offset the start offset into the byte array
     * @param value the value to be represented by the four bytes at the
     *              given offset
     */
    public static void setInteger(byte[] b, int offset, long value) {
        b[offset    ] = (byte) ((value & 0xff000000) >> 24);
        b[offset + 1] = (byte) ((value & 0x00ff0000) >> 16);
        b[offset + 2] = (byte) ((value & 0x0000ff00) >> 8);
        b[offset + 3] = (byte) ((value & 0x000000ff));
    }

    /**
     * Extracts the long value from eight bytes of a byte array
     * starting at the given offset.
     *
     * @param b the byte array from which to extract the long value
     * @param offset the start offset into the byte array
     * @return the long value represented by the eight bytes at the given offset
     */
    public static long getLong(byte[] b, int offset) {
        return ((long)(getInteger(b, offset)) << 32) +
                (getInteger(b, offset + 4) & 0xFFFFFFFFL);
    }

    /**
     * Populates eight bytes of a byte array with the given long value.
     *
     * @param b the byte array into which to put the long value
     * @param offset the start offset into the byte array
     * @param value the long value to be represented by the eight bytes at
     *              the given offset
     */
    public static void setLong(byte[] b, int offset, long value) {
        b[offset    ] = (byte) ((value & 0xff00000000000000L) >>> 56);
        b[offset + 1] = (byte) ((value & 0x00ff000000000000L) >>> 48);
        b[offset + 2] = (byte) ((value & 0x0000ff0000000000L) >>> 40);
        b[offset + 3] = (byte) ((value & 0x000000ff00000000L) >>> 32);
        b[offset + 4] = (byte) ((value & 0x00000000ff000000L) >>> 24);
        b[offset + 5] = (byte) ((value & 0x0000000000ff0000L) >>> 16);
        b[offset + 6] = (byte) ((value & 0x000000000000ff00L) >>> 8);
        b[offset + 7] = (byte) ((value & 0x00000000000000ffL));
    }

    /** Reads a single byte from the array (at the given offset),
     * interpreting it as an unsigned 8-bit value.
     *
     * @param b the byte array from which to read the byte
     * @param offset the start offset into the byte array
     * @return a short value equivalent to the unsigned 8-bit value
     */
    public static short getU8(byte[] b, int offset) {
        return PrimitiveUtils.fromU8(b[offset]);
    }

    /** Populates a single byte of a byte array (at the given offset),
     * with the supplied equivalent unsigned 8-bit value.
     *
     * @param b the byte array into which to write the u8 value
     * @param offset the start offset into the byte array
     * @param value the unsigned value to be converted and inserted
     * @throws IllegalArgumentException if value is not 0..2^8-1
     */
    public static void setU8(byte[] b, int offset, short value) {
        b[offset] = PrimitiveUtils.toU8(value);
    }

    /** Reads two bytes from the array (at the given offset),
     * interpreting them as an unsigned 16-bit value.
     *
     * @param b the byte array from which to read
     * @param offset the start offset into the byte array
     * @return an int value equivalent to the unsigned 16-bit value
     */
    public static int getU16(byte[] b, int offset) {
        return PrimitiveUtils.fromU16(getShort(b, offset));
    }

    /** Populates two bytes of a byte array (at the given offset),
     * with the supplied equivalent unsigned 16-bit value.
     *
     * @param b the byte array into which to write the u16 value
     * @param offset the start offset into the byte array
     * @param value the unsigned value to be converted and inserted
     * @throws IllegalArgumentException if uv is not 0..2^16-1
     */
    public static void setU16(byte[] b, int offset, int value) {
        setShort(b, offset, PrimitiveUtils.toU16(value));
    }

    /** Reads four bytes from the array (at the given offset),
     * interpreting them as an unsigned 32-bit value.
     *
     * @param b the byte array from which to read
     * @param offset the start offset into the byte array
     * @return a long value equivalent to the unsigned 32-bit value
     */
    public static long getU32(byte[] b, int offset) {
        return PrimitiveUtils.fromU32(getInteger(b, offset));
    }

    /** Populates four bytes of a byte array (at the given offset),
     * with the supplied equivalent unsigned 32-bit value.
     *
     * @param b the byte array into which to write the u32 value
     * @param offset the start offset into the byte array
     * @param value the unsigned value to be converted and inserted
     * @throws IllegalArgumentException if uv is not 0..2^32-1
     */
    public static void setU32(byte[] b, int offset, long value) {
        setInteger(b, offset, PrimitiveUtils.toU32(value));
    }

    /** Reads eight bytes from the array (at the given offset),
     * interpreting them as an unsigned 64-bit value.
     *
     * @param b the byte array from which to read
     * @param offset the start offset into the byte array
     * @return a BigInteger value equivalent to the unsigned 64-bit value
     */
    public static BigInteger getU64(byte[] b, int offset) {
        return PrimitiveUtils.fromU64(getLong(b, offset));
    }

    /** Populates eight bytes of a byte array (at the given offset),
     * with the supplied equivalent unsigned 64-bit value.
     *
     * @param b the byte array into which to write the u64 value
     * @param offset the start offset into the byte array
     * @param value the unsigned value to be converted and inserted
     * @throws IllegalArgumentException if uv is not 0..2^64-1
     */
    public static void setU64(byte[] b, int offset, BigInteger value) {
        setLong(b, offset, PrimitiveUtils.toU64(value));
    }


    /**
     * Scans through the specified byte array
     * returning the index of the first byte that matches the given value,
     * or -1 if no match is found.
     *
     * @param b the byte array to search
     * @param match the byte value to match
     * @return the index into the byte array of the first byte that matches,
     *          or -1 if the match was not found
     */
    public static int indexOf(byte[] b, byte match) {
        return indexOf(b, match, 0, b.length);
    }

    /**
     * Scans through the specified byte array starting at the given index,
     * returning the index of the first byte that matches the given value,
     * or -1 if no match is found.
     *
     * @param b the byte array to search
     * @param match the byte value to match
     * @param startIndex the index into the array from which to start the
     *                   search (inclusive)
     * @return the index into the byte array of the first byte that matches,
     *          or -1 if the match was not found
     * @throws ArrayIndexOutOfBoundsException if startIndex is not within
     * array bounds
     */
    public static int indexOf(byte[] b, byte match, int startIndex) {
        return indexOf(b, match, startIndex, b.length);
    }

    /**
     * Scans through the specified byte array starting at the given index,
     * and ending before the given end index, returning the index of the
     * first byte that matches the given value, or -1 if no match is found.
     *
     * @param b the byte array to search
     * @param match the byte value to match
     * @param startIndex the index into the array from which to start the
     *                   search (inclusive)
     * @param endIndex the index which designates the end of the searchable
     *                 area (exclusive)
     * @return the index into the byte array of the first byte that matches,
     *          or -1 if the match was not found
     * @throws ArrayIndexOutOfBoundsException if startIndex/endIndex
     *          attempts access outside the array bounds
     */
    public static int indexOf(byte[] b, byte match,
                              int startIndex, int endIndex) {
        int i = startIndex;
        while (i < endIndex) {
            if (b[i] == match)
                return i;
            i++;
        }
        return -1;
    }


    /**
     * Counts and returns the number of 1 bits within the specified byte array.
     *
     * @param b the byte array from which to count the 1 bits
     * @return the number of 1 bits in the byte array
     */
    public static int countBits(byte[] b) {
        return countBits(b, 0, b.length);
    }

    /**
     * Counts and returns the number of 1 bits within the specified region of
     * a byte array, starting at the given offset, to the end of the array.
     *
     * @param b the byte array from which to count the 1 bits
     * @param offset the start offset into the byte array
     * @return number of 1 bits in the specified region of the byte array
     * @throws ArrayIndexOutOfBoundsException if offset is not within
     * array bounds
     */
    public static int countBits(byte[] b, int offset) {
        return countBits(b, offset, b.length-offset);
    }

    /**
     * Counts and returns the number of 1 bits within the specified region
     * of a byte array, starting at the given offset, for 'length' bytes.
     *
     * @param b the byte array from which to count the 1 bits
     * @param offset the start offset into the byte array
     * @param length the number of bytes to include in the count
     * @return number of 1 bits in the specified region of the byte array
     * @throws ArrayIndexOutOfBoundsException if offset/length attempts
     *          access outside the array bounds
     */
    public static int countBits(byte[] b, int offset, int length) {
        int count = 0;
        int idx = offset;
        for (int i = 0; i < length; i++)
            count += BITS_IN_BYTE[b[idx++] & 0xff];
        return count;
    }


    // === Bit Manipulation section ===

    /** This method reverses all the bits (logical NOT) in the given array.
     *
     * @param b the byte array
     */
    public static void not(byte[] b) {
        not(b, 0, b.length);
    }

    /** This method reverses all the bits (logical NOT) in the given array,
     * from offset to the end of the array.
     *
     * @param b the byte array
     * @param offset the start offset into the byte array
     * @throws ArrayIndexOutOfBoundsException if offset is not within
     *          array bounds
     */
    public static void not(byte[] b, int offset) {
        not(b, offset, b.length-offset);
    }

    /** This method reverses all the bits (logical NOT) in the given array
     * for the specified region.
     *
     * @param b the byte array
     * @param offset the start offset into the byte array
     * @param length the number of bytes to include in the operation
     * @throws ArrayIndexOutOfBoundsException if offset/length attempts
     *          access outside the array bounds
     */
    public static void not(byte[] b, int offset, int length) {
        validateOffsetLength(b, offset, length);
        int idx = offset;
        for (int i = 0; i < length; i++)
            b[idx++] ^= 0xff;
    }

    /** This method performs a logical AND of the bits in byte array
     * {@code b} with the {@code other} array. The result is left in byte
     * array {@code b}. Note that if the {@code other} array is shorter than
     * byte array {@code b}, the "missing" bytes are assumed to be {@code 0xff}
     * resulting in no change to the original array for those byte positions.
     *
     * @param b the byte array
     * @param other the operand with which to AND
     */
    public static void and(byte[] b, byte[] other) {
        and(b, other, 0, b.length);
    }

    /** This method performs a logical AND of the bits in byte array
     * {@code b} with the {@code other} array, from the specified offset to
     * the end of byte array {@code b}. The result is left in byte array
     * {@code b}. Note that if the {@code other} array is shorter than the
     * region specified, the "missing" bytes are assumed to be {@code 0xff}
     * resulting in no change to the original array for those byte positions.
     *
     * @param b the byte array
     * @param other the operand with which to AND
     * @param offset the start offset into the byte array
     * @throws ArrayIndexOutOfBoundsException if offset is not within
     *          array bounds
     */
    public static void and(byte[] b, byte[] other, int offset) {
        and(b, other, offset, b.length-offset);
    }

    /** This method performs a logical AND of the bits in byte array
     * {@code b} with the {@code other} array, for the specified region of
     * byte array {@code b}. The result is left in byte array {@code b}. Note
     * that if the {@code other} array is shorter than the region specified,
     * the "missing" bytes are assumed to be {@code 0xff} resulting in no
     * change to the original array for those byte positions.
     *
     * @param b the byte array
     * @param other the operand with which to AND
     * @param offset the start offset into the byte array
     * @param length the number of bytes to include in the operation
     * @throws ArrayIndexOutOfBoundsException if offset/length attempts access
     *          outside the array bounds
     */
    public static void and(byte[] b, byte[] other, int offset, int length) {
        validateOffsetLength(b, offset, length);
        int idx = offset;
        for (int i=0; i<length && i<other.length; i++)
            b[idx++] &= other[i];
    }

    /** This method performs a logical OR of the bits in byte array {@code b}
     * with the {@code other} array. The result is left in byte array {@code b}.
     * Note that if the {@code other} array is shorter than byte array
     * {@code b}, the "missing" bytes are assumed to be {@code 0x00} resulting
     * in no change to the original array for those byte positions.
     *
     * @param b the byte array
     * @param other the operand with which to OR
     */
    public static void or(byte[] b, byte[] other) {
        or(b, other, 0, b.length);
    }

    /** This method performs a logical OR of the bits in byte array {@code b}
     * with the {@code other} array, from the specified offset to the end of
     * byte array {@code b}. The result is left in byte array {@code b}. Note
     * that if the {@code other} array is shorter than the region specified,
     * the "missing" bytes are assumed to be {@code 0x00} resulting in no
     * change to the original array for those byte positions.
     *
     * @param b the byte array
     * @param other the operand with which to OR
     * @param offset the start offset into the byte array
     * @throws ArrayIndexOutOfBoundsException if offset is not within
     *          array bounds
     */
    public static void or(byte[] b, byte[] other, int offset) {
        or(b, other, offset, b.length-offset);
    }

    /** This method performs a logical OR of the bits in byte array {@code b}
     * with the {@code other} array, for the specified region of byte array
     * {@code b}. The result is left in byte array {@code b}. Note that if the
     * {@code other} array is shorter than the region specified, the "missing"
     * bytes are assumed to be {@code 0x00} resulting in no change to the
     * original array for those byte positions.
     *
     * @param b the byte array
     * @param other the operand with which to OR
     * @param offset the start offset into the byte array
     * @param length the number of bytes to include in the operation
     * @throws ArrayIndexOutOfBoundsException if offset/length attempts access
     *          outside the array bounds
     */
    public static void or(byte[] b, byte[] other, int offset, int length) {
        validateOffsetLength(b, offset, length);
        int idx = offset;
        for (int i=0; i<length && i<other.length; i++)
            b[idx++] |= other[i];
    }

    /** This method performs a logical EXCLUSIVE-OR of the bits in byte array
     * {@code b} with the {@code other} array. The result is left in byte array
     * {@code b}. Note that if the {@code other} array is shorter than byte
     * array {@code b}, the "missing" bytes are assumed to be {@code 0x00}
     * resulting in no change to the original array for those byte positions.
     *
     * @param b the byte array
     * @param other the operand with which to EXCLUSIVE-OR
     */
    public static void xor(byte[] b, byte[] other) {
        xor(b, other, 0, b.length);
    }

    /** This method performs a logical EXCLUSIVE-OR of the bits in byte array
     * {@code b} with the {@code other} array, from the specified offset to
     * the end of byte array {@code b}. The result is left in byte array
     * {@code b}. Note that if the {@code other} array is shorter than the
     * region specified, the "missing" bytes are assumed to be {@code 0x00}
     * resulting in no change to the original array for those byte positions.
     *
     * @param b the byte array
     * @param other the operand with which to EXCLUSIVE-OR
     * @param offset the start offset into the byte array
     * @throws ArrayIndexOutOfBoundsException if offset is not within
     *          array bounds
     */
    public static void xor(byte[] b, byte[] other, int offset) {
        xor(b, other, offset, b.length-offset);
    }

    /** This method performs a logical EXCLUSIVE-OR of the bits in byte array
     * {@code b} with the {@code other} array, for the specified region of
     * byte array {@code b}. The result is left in byte array {@code b}. Note
     * that if the {@code other} array is shorter than the region specified,
     * the "missing" bytes are assumed to be {@code 0x00} resulting in no
     * change to the original array for those byte positions.
     *
     * @param b the byte array
     * @param other the operand with which to EXCLUSIVE-OR
     * @param offset the start offset into the byte array
     * @param length the number of bytes to include in the operation
     * @throws ArrayIndexOutOfBoundsException if offset/length attempts
     *          access outside the array bounds
     */
    public static void xor(byte[] b, byte[] other, int offset, int length) {
        validateOffsetLength(b, offset, length);
        int idx = offset;
        for (int i=0; i<length && i<other.length; i++)
            b[idx++] ^= other[i];
    }

    private static final String E_OFF_BIG = "offset is too large: ";
    private static final String E_LEN_SMALL = "length cannot be smaller than 1";
    private static final String E_OFF_LEN_BIG =
                                        "offset and length are too large: ";

    // private helper method to throw AIOOB if necessary
    private static void validateOffsetLength(byte[] b, int offset, int length) {
        if (offset >= b.length)
            throw new ArrayIndexOutOfBoundsException(E_OFF_BIG + offset);
        if (length < 1)
            throw new IllegalArgumentException(E_LEN_SMALL);
        if (offset + length - 1 >= b.length)
            throw new ArrayIndexOutOfBoundsException(E_OFF_LEN_BIG +
                                                    (offset + length - 1));
    }

    // === Presentation section ===

    /**
     * Generates a hex dump string from the given bytes.
     *
     * @param b the byte array
     * @return a formatted hex image of the portion of the byte array
     */
    public static String hex(byte[] b) {
        return hex(b, 0, b.length);
    }

    /**
     * Generates a hex dump string from the specified offset to the end of
     * the given byte array.
     *
     * @param b the byte array
     * @param offset the start offset into the byte array
     * @return a formatted hex image of the portion of the byte array
     */
    public static String hex(byte[] b, int offset) {
        return hex(b, offset, b.length-offset);
    }

    /**
     * Generates a hex dump string from the specified region of the
     * given byte array.
     *
     * @param b the byte array
     * @param offset the start offset into the byte array
     * @param length the number of bytes to output
     * @return a formatted hex image of the byte array
     */
    public static String hex(byte[] b, int offset, int length) {
        if (0 == length)
            return EMPTY;
        validateOffsetLength(b, offset, length);
        char[] hex = new char[length << 1];
        for (int i = 0; i < length; i++) {
            hex[(i << 1)    ] = HEX[(b[i+offset] & 0xf0) >> 4];
            hex[(i << 1) + 1] = HEX[ b[i+offset] & 0x0f ];
        }
        return new String(hex);
    }

    /** Provides a fast lookup of the 2-digit hex string representation for
     * the given byte, using lowercase alpha digits.
     *
     * @param b the byte
     * @return the 2-digit hex representation
     */
    public static String hexLookupLower(byte b) {
        return HEX_LOWER[b+FAST_BYTE_OFFSET];
    }

    /** Provides a fast lookup of the 2-digit hex string representation for
     * the given byte, using uppercase alpha digits.
     *
     * @param b the byte
     * @return the 2-digit hex representation
     */
    public static String hexLookupUpper(byte b) {
        return HEX_UPPER[b+FAST_BYTE_OFFSET];
    }

    /** Returns a two character string representation of the given byte,
     * in hex.
     * @param b the byte
     * @return the string
     */
    public static String byteToHex(byte b) {
        StringBuilder sb = new StringBuilder();
        sb.append(HEX[(b & 0xf0) >> 4]).append(HEX[b & 0x0f]);
        return sb.toString();
    }

    /** Returns a string representation of the byte array as a hex dump,
     * with spaces inserted to help reading the string.
     *
     * @param bytes the byte array
     * @return the string
     */
    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        final int nb = bytes.length;
        for (int i=0; i<nb; i++) {
            sb.append(byteToHex(bytes[i]));
            if (i%2!=0 && i<nb-1) sb.append(" ");
        }
        return sb.toString();
    }

    /** Returns a string representation of a byte array. For example:
     * <pre>
     *     "0x[ 00 2e ff b7 ]"
     * </pre>
     *
     * @param bytes the byte array
     * @return a string representation
     */
    public static String toHexArrayString(byte[] bytes) {
        StringBuilder sb = new StringBuilder("0x[ ");
        for (byte b: bytes) {
            sb.append(byteToHex(b)).append(", ");
        }
        int len = sb.length();
        sb.replace(len-2, len, " ]");
        return sb.toString();
    }

    /** Returns a string representation of a byte array with "0x" prefix.
     * For example:
     * <pre>
     *     "0x002effb7"
     * </pre>
     *
     * @param bytes the byte array
     * @return a string representation
     */
    public static String hexWithPrefix(byte[] bytes) {
        return "0x" + hex(bytes);
    }

    /** Helper method to convert the hex string with "0x" prefix
     * to an array of bytes.
     *
     * @param s string with "0x" prefix
     * @return byte[] the byte array
     */
    public static byte[] parseHexWithPrefix(String s) {
        // remove "0x"
        return parseHex(s.substring(2, s.length()));
    }

    private static final String HEX_RE_STR = "[0-9a-fA-F]*";
    static final String E_NON_HEX = "invalid hex string: ";
    static final String E_NOT_EVEN = "string not even # chars: ";


    /**
     * Generates a byte array by parsing the given ascii string of hex
     * characters. This method is not a precise inverse of the {@link #hex}
     * method, since it will happily deal with whitespace etc. between
     * hex digits.
     * <p>
     * This method ignores any non-word characters (including new-lines).
     *
     * @param asciiHex a string of ascii hex digits; must be an even length
     * @return a byte array containing the byte representation of each
     *          two-byte ascii sequence
     * @throws IllegalArgumentException if the string cannot be parsed as hex
     */
    public static byte[] parseHex(String asciiHex) {
        // first, strip out all non-word characters
        final String hex = asciiHex.replaceAll("\\W", "");

        final int len = hex.length();
        if (!hex.matches(HEX_RE_STR))
            throw new IllegalArgumentException(E_NON_HEX + asciiHex);

        if (len % 2 != 0)
            throw new IllegalArgumentException(E_NOT_EVEN + asciiHex);

        byte[] b = new byte[len >> 1];
        for (int i = 0; i < len; i++) {
            // Parse each character of the string as a half-byte, i.e. a nybble.
            char a = hex.charAt(i);
            int j = i >> 1;
            if (i % 2 == 0)
                // Put the even bytes into upper 4 bits
                b[j] = (byte) (Character.digit(a, 16) << 4);
            else
                // Leave the odd bytes in the lower 4 bits
                b[j] |= (byte) (Character.digit(a, 16));
        }
        return b;
    }

    /**
     * Examines the given byte array and returns an array that is padded out
     * to a 64 bit boundary. That is, the returned array will have a length
     * that is a multiple of 8 bytes.
     *
     * @param b the byte array
     * @return a new array of bytes padded to a 64 bit boundary, or the
     *          original array if it is already padded (i.e. the length is
     *          a multiple of 8 bytes)
     */
    public static byte[] getPadded64BitBytes(byte[] b) {
        int origLen = b.length;
        int origDiv = origLen / 8;
        int origMod = origLen % 8;

        if (0 == origMod)
            return b;

        int newLen = (origDiv + 1) * 8;

        byte[] newBytes = new byte[newLen];
        System.arraycopy(b, 0, newBytes, 0, origLen);
        return newBytes;
    }

    /**
     * Helper method to convert the string to an array of bytes
     * (without local replacement).
     *
     * @param s string to convert
     * @return byte array of the string characters
     */
    public static byte[] getRawBytes(String s) {
        char[] chars = s.toCharArray();
        byte[] bytes = new byte[chars.length];
        for (int i = 0; i < chars.length; i++)
            bytes[i] = (byte) chars[i];
        return bytes;
    }

    /**
     * Interprets the given array of bytes as a UTF-8 encoded string,
     * and returns that string.
     *
     * @param bytes array of the string characters
     * @return the string
     */
    public static String getStringFromRawBytes(byte[] bytes) {
        if (bytes == null)
            return EMPTY;

        try {
            return SP.get(new String(bytes, UTF8));
        } catch (UnsupportedEncodingException e) {
            // should never happen, since UTF-8 is a standard charset
            throw new IllegalStateException(e);
        }
    }

    /** Interprets the given array of bytes as a null-terminated
     * ASCII string, and returns that string.
     *
     * @param bytes the null-terminated ASCII string encoded as bytes
     * @return the string
     */
    public static String getNullTerminatedAscii(byte[] bytes) {
        if (bytes == null)
            return EMPTY;

        int n = bytes.length;
        // The string is assumed to be null terminated; find the null...
        int index = 0;
        while (index < n && bytes[index] != 0)
            index++;
        try {
            // convert the bytes into a string, pool it, and return it...
            return SP.get(new String(bytes, 0, index, ASCII));
        } catch (UnsupportedEncodingException e) {
            // should never happen, since US-ASCII is a standard charset
            throw new IllegalStateException(e);
        }

    }
    /**
     * Creates a string of a byte array suitable for logging.
     *
     * @param msg a header message to display
     * @param b an array of bytes
     * @param len number of bytes in array
     * @return log string
     */
    public static String getDebugString(String msg, byte[] b, int len) {
        StringBuilder sb = new StringBuilder(msg);
        sb.append(" (");
        sb.append(len);
        sb.append(" bytes):").append(EOL);
        sb.append(getByteArrayString(b, len));
        return sb.toString();
    }

    /**
     * Helper method to create log string of a byte array.
     *
     * @param msg header message to display
     * @param b an array of bytes
     * @return log string
     */
    public static String getDebugString(String msg, byte[] b) {
        return getDebugString(msg, b, b.length);
    }

    /**
     * Returns a string representation of the given byte array.
     *
     * @param b an array of bytes
     * @return a string representation of the byte array
     */
    public static String getByteArrayString(byte[] b) {
        return getByteArrayString(b, b.length);
    }

    /**
     * Returns a string representation of the given byte array.
     *
     * @param b an array of bytes
     * @param len the number of bytes in array
     * @return a string representation of the byte array
     */
    public static String getByteArrayString(byte[] b, int len) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < len; i++) {
            sb.append(" ");

            String hexStr = Integer.toHexString(b[i]);
            int nHexChars = hexStr.length();

            if (1 == nHexChars) {
                sb.append('0');
                sb.append(hexStr);
            } else if (2 < nHexChars) {
                sb.append(hexStr.charAt(nHexChars - 2));
                sb.append(hexStr.charAt(nHexChars - 1));
            } else {
                sb.append(hexStr);
            }

            if (15 == (i % 16))
                sb.append(EOL);
        }

        sb.append(EOL);
        return sb.toString();
    }

    /**
     * Returns a 'filtered' string representation of the given byte array:
     * <pre>
     * "[&lt;chars&gt;] [&lt;bytes&gt;]"
     * </pre>
     * where all control chars
     * in &lt;chars&gt; are replaced by '.' and
     * &lt;bytes&gt; is of the form "00 ab ..".
     *
     * @param b byte array
     * @return the filtered string representation
     */
    public static String getFilteredString(byte[] b) {
        StringBuilder asciiBuffer = new StringBuilder();
        StringBuilder hexBuffer = new StringBuilder();

        for (int i = 0; i < b.length; i++) {
            int byteVal = 0x0ff & b[i];
            char asciiChar = (char)(0x07f & b[i]);
            if (byteVal <= 0x07f && Character.isLetterOrDigit(asciiChar))
                asciiBuffer.append(asciiChar);
            else
                asciiBuffer.append('.');

            String hex = Integer.toHexString(byteVal);
            if (0 < i)
                hexBuffer.append(' ');
            if (1 == hex.length())
                hexBuffer.append('0');
            hexBuffer.append(hex);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(asciiBuffer);
        sb.append("] [");
        sb.append(hexBuffer);
        sb.append("]");

        return sb.toString();
    }

    /** Reads in a human readable file of hex data and returns it
     * as a byte array. If the resource file cannot be found, null is
     * returned.
     * <p>
     * For example, given the file contents:
     * <pre>
     *     # comment
     *     ff ab 00
     *     # another comment
     *     11 22 33
     * </pre>
     * invoking the method will return the following byte array:
     * <pre>
     *     [ 0xff, 0xab, 0x00, 0x11, 0x22, 0x33 ]
     * </pre>
     *
     * @param path the path of the file resource
     * @param cl the classloader to use to locate the file resource
     * @return a byte array equivalent to the hex data from the file
     * @throws IOException if there is a problem reading the file
     * @see #slurpBytesFromHexFile(Class, String)
     */
    public static byte[] slurpBytesFromHexFile(String path, ClassLoader cl)
            throws IOException {
        String text = StringUtils.getFileContents(path, cl);
        if (text == null)
            return null;

        String noComments = StringUtils.stripCommentLines(text);
        return parseHex(noComments);
    }

    /** Reads in a human readable file of hex data and returns it
     * as a byte array.
     * <p>
     * This form locates the .hex file by looking in the same package as the
     * given class, and using the given base name. Note that both the class
     * and the .hex file need to be in the same jar file.
     * <p>
     * For example, supposing the jar file contains:
     *
     * <pre>
     * com/hp/sdn/app/example/SomeObject.class
     * com/hp/sdn/app/example/MyPacketData.hex
     * </pre>
     *
     * Then, to correctly load the hex file data, call:
     * <pre>
     * byte[] pktData =
     *     ByteUtils.slurpBytesFromHexFile(SomeObject.class, "MyPacketData");
     * </pre>
     *
     * @param c the class used to locate the hex file
     * @param baseName the hex file base name
     * @return a byte array equivalent to the hex data from the file
     * @throws IOException if there is a problem reading the file
     * @see #slurpBytesFromHexFile(String, ClassLoader)
     */
    public static byte[] slurpBytesFromHexFile(Class<?> c, String baseName)
            throws IOException {
        String className = c.getName();
        int lastDot = className.lastIndexOf(DOT);
        String path = className.replace(DOT, SLASH);
        StringBuilder sb = new StringBuilder(path.substring(0, lastDot+1));
        sb.append(baseName).append(DOT_HEX);
        return slurpBytesFromHexFile(sb.toString(), c.getClassLoader());
    }
}
