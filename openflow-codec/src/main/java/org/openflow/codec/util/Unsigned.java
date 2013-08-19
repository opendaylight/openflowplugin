package org.openflow.codec.util;

import java.math.BigInteger;

import org.openflow.codec.io.IDataBuffer;

/*****
 * A util library class for dealing with the lack of unsigned datatypes in Java
 *
 * @author Rob Sherwood (rob.sherwood@stanford.edu)
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */

public class Unsigned {
    /**
     * Get an unsigned byte from the current position of the IDataBuffer
     *
     * @param bb
     *            IDataBuffer to get the byte from
     * @return an unsigned byte contained in a short
     */
    public static short getUnsignedByte(IDataBuffer bb) {
        return ((short) (bb.get() & (short) 0xff));
    }

    /**
     * Get an unsigned byte from the specified offset in the IDataBuffer
     *
     * @param bb
     *            IDataBuffer to get the byte from
     * @param offset
     *            the offset to get the byte from
     * @return an unsigned byte contained in a short
     */
    public static short getUnsignedByte(IDataBuffer bb, int offset) {
        return ((short) (bb.get(offset) & (short) 0xff));
    }

    /**
     * Put an unsigned byte into the specified IDataBuffer at the current
     * position
     *
     * @param bb
     *            IDataBuffer to put the byte into
     * @param v
     *            the short containing the unsigned byte
     */
    public static void putUnsignedByte(IDataBuffer bb, short v) {
        bb.put((byte) (v & 0xff));
    }

    /**
     * Put an unsigned byte into the specified IDataBuffer at the specified
     * offset
     *
     * @param bb
     *            IDataBuffer to put the byte into
     * @param v
     *            the short containing the unsigned byte
     * @param offset
     *            the offset to insert the unsigned byte at
     */
    public static void putUnsignedByte(IDataBuffer bb, short v, int offset) {
        bb.put(offset, (byte) (v & 0xff));
    }

    /**
     * Get an unsigned short from the current position of the IDataBuffer
     *
     * @param bb
     *            IDataBuffer to get the byte from
     * @return an unsigned short contained in a int
     */
    public static int getUnsignedShort(IDataBuffer bb) {
        return (bb.getShort() & 0xffff);
    }

    /**
     * Get an unsigned short from the specified offset in the IDataBuffer
     *
     * @param bb
     *            IDataBuffer to get the short from
     * @param offset
     *            the offset to get the short from
     * @return an unsigned short contained in a int
     */
    public static int getUnsignedShort(IDataBuffer bb, int offset) {
        return (bb.getShort(offset) & 0xffff);
    }

    /**
     * Put an unsigned short into the specified IDataBuffer at the current
     * position
     *
     * @param bb
     *            IDataBuffer to put the short into
     * @param v
     *            the int containing the unsigned short
     */
    public static void putUnsignedShort(IDataBuffer bb, int v) {
        bb.putShort((short) (v & 0xffff));
    }

    /**
     * Put an unsigned short into the specified IDataBuffer at the specified
     * offset
     *
     * @param bb
     *            IDataBuffer to put the short into
     * @param v
     *            the int containing the unsigned short
     * @param offset
     *            the offset to insert the unsigned short at
     */
    public static void putUnsignedShort(IDataBuffer bb, int v, int offset) {
        bb.putShort(offset, (short) (v & 0xffff));
    }

    /**
     * Get an unsigned int from the current position of the IDataBuffer
     *
     * @param bb
     *            IDataBuffer to get the int from
     * @return an unsigned int contained in a long
     */
    public static long getUnsignedInt(IDataBuffer bb) {
        return ((long) bb.getInt() & 0xffffffffL);
    }

    /**
     * Get an unsigned int from the specified offset in the IDataBuffer
     *
     * @param bb
     *            IDataBuffer to get the int from
     * @param offset
     *            the offset to get the int from
     * @return an unsigned int contained in a long
     */
    public static long getUnsignedInt(IDataBuffer bb, int offset) {
        return ((long) bb.getInt(offset) & 0xffffffffL);
    }

    /**
     * Put an unsigned int into the specified IDataBuffer at the current
     * position
     *
     * @param bb
     *            IDataBuffer to put the int into
     * @param v
     *            the long containing the unsigned int
     */
    public static void putUnsignedInt(IDataBuffer bb, long v) {
        bb.putInt((int) (v & 0xffffffffL));
    }

    /**
     * Put an unsigned int into the specified IDataBuffer at the specified
     * offset
     *
     * @param bb
     *            IDataBuffer to put the int into
     * @param v
     *            the long containing the unsigned int
     * @param offset
     *            the offset to insert the unsigned int at
     */
    public static void putUnsignedInt(IDataBuffer bb, long v, int offset) {
        bb.putInt(offset, (int) (v & 0xffffffffL));
    }

    /**
     * Get an unsigned long from the current position of the IDataBuffer
     *
     * @param bb
     *            IDataBuffer to get the long from
     * @return an unsigned long contained in a BigInteger
     */
    public static BigInteger getUnsignedLong(IDataBuffer bb) {
        byte[] v = new byte[8];
        for (int i = 0; i < 8; ++i) {
            v[i] = bb.get(i);
        }
        return new BigInteger(1, v);
    }

    /**
     * Get an unsigned long from the specified offset in the IDataBuffer
     *
     * @param bb
     *            IDataBuffer to get the long from
     * @param offset
     *            the offset to get the long from
     * @return an unsigned long contained in a BigInteger
     */
    public static BigInteger getUnsignedLong(IDataBuffer bb, int offset) {
        byte[] v = new byte[8];
        for (int i = 0; i < 8; ++i) {
            v[i] = bb.get(offset + i);
        }
        return new BigInteger(1, v);
    }

    /**
     * Put an unsigned long into the specified IDataBuffer at the current
     * position
     *
     * @param bb
     *            IDataBuffer to put the long into
     * @param v
     *            the BigInteger containing the unsigned long
     */
    public static void putUnsignedLong(IDataBuffer bb, BigInteger v) {
        bb.putLong(v.longValue());
    }

    /**
     * Put an unsigned long into the specified IDataBuffer at the specified
     * offset
     *
     * @param bb
     *            IDataBuffer to put the long into
     * @param v
     *            the BigInteger containing the unsigned long
     * @param offset
     *            the offset to insert the unsigned long at
     */
    public static void putUnsignedLong(IDataBuffer bb, BigInteger v, int offset) {
        bb.putLong(offset, v.longValue());
    }
}
