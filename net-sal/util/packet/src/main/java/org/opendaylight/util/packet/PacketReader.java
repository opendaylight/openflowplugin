/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.PrimitiveUtils;
import org.opendaylight.util.net.*;

import java.nio.ByteBuffer;


/**
 * Wraps a {@link ByteBuffer} and provides methods to read rich data types
 * from it. Supported data types are from the {@code org.opendaylight.util.net} package.
 *
 * @author Simon Hunt
 * @author Frank Wood
 * @author Thomas Vachuska
 */
public class PacketReader {

    private static final int BIT_SHIFT_8 = 8;
    private static final int BIT_SHIFT_16 = 16;
    private static final int U8_MASK = 0xff;
    private static final int U16_MASK = 0xffff;
    private static final int U24_MASK = 0xffffff;

    private final ByteBuffer b;
    private final int start;
    private int trip;

    /**
     * Constructs a packet reader wrapping a {@link ByteBuffer}.
     *
     * @param b the byte buffer
     */
    public PacketReader(ByteBuffer b) {
        this.b = b;
        start = b.position();
        trip = start;
    }

    @Override
    public String toString() {
        return b.toString();
    }

    /**
     * Number of bytes read since creation of this reader or since the last
     * call to reset the odometer.
     *
     * @return the number of bytes read since creation or reset
     */
    public int odometer() {
        return b.position() - trip;
    }

    /**
     * Resets the odometer to the current buffer position.
     */
    public void resetOdometer() {
        trip = b.position();
    }

    /**
     * Resets the buffer position to the reader's original starting position.
     * The odometer is reset to 0.
     */
    public void resetIndex() {
        b.position(start);
        trip = start;
    }

    /**
     * Returns the reader index.
     *
     * @return the reader index
     */
    public int ri() {
        return b.position();
    }

    /**
     * Sets the reader index to the given position. The new position must
     * be non-negative and no larger than the current limit.
     *
     * @param newPosition the new position
     * @throws IllegalArgumentException if preconditions for newPosition are
     *          not met
     */
    public void ri(int newPosition) {
        b.position(newPosition);
    }

    /**
     * Returns the count of readable bytes remaining.
     *
     * @return the number of readable bytes
     */
    public int readableBytes() {
        return b.remaining();
    }

    /**
     * Returns the limit of usable bytes.
     *
     * @return the limit
     */
    public int limit() {
        return b.limit();
    }

    /**
     * Returns the byte array that backs this reader.
     *
     * @return the backing byte array
     */
    public byte[] array() {
        return b.array();
    }

    /**
     * Reads an IPv4 address from the buffer.
     *
     * @return an IP address (IPv4)
     */
    public IpAddress readIPv4Address() {
        return IpAddress.valueFrom(b, false);
    }

    /**
     * Reads an IPv6 address from the buffer.
     *
     * @return an IP address (IPv6)
     */
    public IpAddress readIPv6Address() {
        return IpAddress.valueFrom(b, true);
    }

    /**
     * Reads a MAC address from the buffer.
     *
     * @return a MAC address
     */
    public MacAddress readMacAddress() {
        return MacAddress.valueFrom(b);
    }

    /**
     * Readsa VLAN ID from the buffer.
     *
     * @return a VLAN ID
     */
    public VlanId readVlanId() { return VlanId.valueFrom(b); }

    /** Reads an Ethernet type from the buffer.
     *
     * @return an Ethernet type
     */
    public EthernetType readEthernetType() {
        return EthernetType.valueOf(readU16());
    }

    /**
     * Reads an IP Protocol from the buffer.
     *
     * @return an IP Protocol
     */
    public IpProtocol readIpProtocol() {
        return IpProtocol.valueOf(readU8());
    }

    /**
     * Reads an ICMPv4 Type from the buffer.
     *
     * @return an ICMPv4 type
     */
    public ICMPv4Type readIcmpv4Type() {
        return ICMPv4Type.valueOf(readU8());
    }

    /**
     * Reads an ICMPv6 Type from the buffer.
     *
     * @return an ICMPv6 type
     */
    public ICMPv6Type readIcmpv6Type() {
        return ICMPv6Type.valueOf(readU8());
    }

    /**
     * Reads a (u16) port number from the buffer.
     *
     * @return a port number
     */
    public PortNumber readPortNumber() {
        return PortNumber.valueOf(readU16());
    }

    /**
     * Reads a (u32) port number from the buffer.
     *
     * @return a big port number
     */
    public BigPortNumber readBigPortNumber() {
        return BigPortNumber.valueOf(readU32());
    }

    /**
     * Reads a string from the buffer. The data in the buffer is assumed
     * to be in a field of length <em>N</em>, null terminated and encoded
     * as ASCII. At the end of this operation the reader index of the
     * underlying buffer will have advanced by <em>N</em> bytes.
     *
     * @param n the size of the string field
     * @return a string
     */
    public String readString(int n) {
        return ByteUtils.getNullTerminatedAscii(readBytes(n));
    }

    /**
     * Reads the specified number of bytes from the buffer, returning them in
     * a newly allocated array.
     *
     * @param n the number of bytes to read
     * @return a byte array containing the read bytes
     */
    public byte[] readBytes(int n) {
        byte[] bytes = new byte[n];
        b.get(bytes);
        return bytes;
    }

    /**
     * Reads and returns a single byte from the buffer.
     *
     * @return the next byte in the buffer
     */
    public byte readByte() {
        return b.get();
    }

    /**
     * Skips buffer data by increasing the current reader index by the
     * specified number of bytes.
     *
     * @param count the number of bytes to skip
     */
    public void skip(int count) {
        b.position(b.position() + count);
    }

    /**
     * Reads four bytes as an int.
     *
     * @return the int value
     */
    public int readInt() {
        return b.getInt();
    }

    /**
     * Reads eight bytes as a long.
     *
     * @return the long value
     */
    public long readLong() {
        return b.getLong();
    }

    /**
     * Reads a byte, interpreting it as an unsigned 8-bit value.
     *
     * @return the U8 equivalent of the byte read
     */
    public short readU8() {
        return PrimitiveUtils.fromU8(b.get());
    }

    /**
     * Peek at the byte at the specified offset from current reader index.
     *
     * @param offset from the current reader index
     * @return the byte at offset
     */
    public byte peek(int offset) {
        return b.get(b.position() + offset);
    }

    /**
     * Peek at the byte at the given offset from the current position,
     * interpreting it as an unsigned 8-bit value.
     *
     * @param offset the offset from the current position
     * @return the U8 equivalent of the byte
     */
    public short peekU8(int offset) {
        return PrimitiveUtils.fromU8(b.get(b.position() + offset));
    }

    /**
     * Peek at the next byte from the current position, interpreting it as
     * an unsigned 8-bit value.
     *
     * @return the U8 equivalent of the byte
     */
    public short peekU8() {
        return peekU8(0);
    }

    /**
     * Peek at the next two bytes from the given offset from the current
     * position, interpreting them as an unsigned 16-bit value.
     *
     * @param offset the offset from the current position
     * @return the U16 equivalent of the bytes read
     */
    public int peekU16(int offset) {
        int i = b.position() + offset;
        return U16_MASK &
                ((U8_MASK & b.get(i)) << BIT_SHIFT_8) |
                (U8_MASK & b.get(i+1));
    }

    /**
     * Peek at the next two bytes from the current position, interpreting them
     * as an unsigned 16-bit value.
     *
     * @return the U16 equivalent of the bytes read
     */
    public int peekU16() {
        return peekU16(0);
    }

    /**
     * Peek at the next three bytes from the given offset from the current
     * position, interpreting them as an unsigned 24-bit value.
     *
     * @param offset the offset from the current position
     * @return the U24 equivalent of the bytes read
     */
    public int peekU24(int offset) {
        int i = b.position() + offset;
        return U24_MASK &
                ((U8_MASK & b.get(i)) << BIT_SHIFT_16) |
                ((U8_MASK & b.get(i+1)) << BIT_SHIFT_8) |
                (U8_MASK & b.get(i+2));
    }

    /**
     * Peek at the next three bytes from the current position, interpreting
     * them as an unsigned 24-bit value.
     *
     * @return the U24 equivalent of the bytes read
     */
    public int peekU24() {
        return peekU24(0);
    }

    /**
     * Reads two bytes, interpreting them as an unsigned 16-bit value.
     *
     * @return the U16 equivalent of the bytes read
     */
    public int readU16() {
        return PrimitiveUtils.fromU16(b.getShort());
    }

    /**
     * Reads three bytes, interpreting them as an unsigned 24-bit value.
     *
     * @return the U24 equivalent of the bytes read
     */
    public int readU24() {
        return U24_MASK &
                ((U8_MASK & b.get()) << BIT_SHIFT_16) |
                ((U8_MASK & b.get()) << BIT_SHIFT_8) |
                (U8_MASK & b.get());
    }

    /**
     * Reads four bytes, interpreting them as an unsigned 32-bit value.
     *
     * @return the U32 equivalent of the bytes read
     */
    public long readU32() {
        return PrimitiveUtils.fromU32(b.getInt());
    }

}
