/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import org.opendaylight.util.net.*;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static org.opendaylight.util.PrimitiveUtils.*;


/**
 * Wraps a {@link ByteBuffer} and provides methods to write rich data types
 * into it. Supported data types are from the {@code org.opendaylight.util.net} package.
 * 
 * @author Simon Hunt
 * @author Frank Wood
 * @author Thomas Vachuska
 */
public class PacketWriter {

    private static final String ASCII = "US-ASCII";

    private static final int BIT_SHIFT_8 = 8;
    private static final int BIT_SHIFT_16 = 16;
    private static final int U8_MASK = 0x0ff;
    private static final int U24_MASK = 0x00ffffff;
    
    private final ByteBuffer bb;
    
    /**
     * Constructs a packet writer wrapping a {@link ByteBuffer}.
     *
     * @param bb the byte buffer
     */
    public PacketWriter(ByteBuffer bb) {
        this.bb = bb;
    }
    
    /**
     * Constructs a packet writer creating a backing {@link ByteBuffer}.
     *
     * @param capacity the new writer's capacity, in bytes
     */
    public PacketWriter(int capacity) {
        this.bb = ByteBuffer.allocate(capacity);
    }

    /**
     * Constructs a packet writer backed by the byte array.
     *
     * @param bytes the new writer's backed byte array
     */
    public PacketWriter(byte[] bytes) {
        this.bb = ByteBuffer.wrap(bytes);
    }
    
    @Override
    public String toString() {
        return bb.toString();
    }
    
    /**
     * Returns the writer index.
     *
     * @return the writer index
     */
    public int wi() {
        return bb.position();
    }

    /**
     * Returns the count of writable bytes remaining.
     *
     * @return the number of writable bytes
     */
    public int writableBytes() {
        return bb.remaining();
    }

    /**
     * Returns the byte array that backs this reader.
     *
     * @return the backing byte array
     */
    public byte[] array() {
        return bb.array();
    }    
    
    /**
     * Writes an IP address to the buffer. If the argument is an IPv4
     * address, 4 bytes will be written; If the argument is an IPv6
     * address, 16 bytes will be written.
     *
     * @param ip the IP address to write
     */
    public void write(IpAddress ip) {
        ip.intoBuffer(bb);
    }

    /**
     * Writes a MAC address to the buffer.
     *
     * @param mac the MAC address to write
     */
    public void write(MacAddress mac) {
        mac.intoBuffer(bb);
    }

    /**
     * Writes a VLAN ID (u12 in two bytes) to the buffer.
     *
     * @param vlanId the VLAN ID to write
     */
    public void write(VlanId vlanId) { vlanId.intoBuffer(bb); }

    /**
     * Writes an Ethernet Type value (u16) to the buffer.
     *
     * @param et the Ethernet Type to write
     */
    public void write(EthernetType et) {
        writeU16(et.getNumber());
    }

    /**
     * Writes an IP Protocol value (u8) to the buffer.
     *
     * @param ipp the IP Protocol to write
     */
    public void write(IpProtocol ipp) {
        writeU8(ipp.getNumber());
    }

    /**
     * Writes an ICMPv4 Type value (u8) to the buffer.
     *
     * @param it the ICMPv4 Type to write
     */
    public void write(ICMPv4Type it) {
        writeU8(it.getCode());
    }

    /**
     * Writes an ICMPv6 Type value (u8) to the buffer.
     *
     * @param it the ICMPv6 Type to write
     */
    public void write(ICMPv6Type it) {
        writeU8(it.getCode());
    }

    /** Writes a (u16) port number to the buffer.
     *
     * @param pn the port number to write
     */
    public void write(PortNumber pn) {
        writeBytes(pn.toByteArray());
    }

    /**
     * Writes a (u32) port number to the buffer.
     *
     * @param pn the port number to write
     */
    public void write(BigPortNumber pn) {
        writeBytes(pn.toByteArray());
    }

    /**
     * Writes a null-terminated string to the buffer, allocating a field
     * of the specified length. Note that if the specified string is longer
     * than fieldLength-1, it will be truncated, with the last byte of the
     * field being set to NUL (0x00). If the specified string is shorter
     * than fieldLength-1, the remaining bytes in the field will be set to NUL.
     * If the specified string is null, a zero-filled array will be returned.
     *
     * @param str the string to write
     * @param fieldLength the length of the field
     */
    public void writeString(String str, int fieldLength) {
        try {
            if (str == null) {
                writeZeros(fieldLength);
            } else {
                byte[] asBytes = str.getBytes(ASCII);
                int toWrite = asBytes.length >= fieldLength
                        ? fieldLength - 1 : asBytes.length;
                if (asBytes.length > toWrite)
                    asBytes = Arrays.copyOf(asBytes, toWrite);
                int zeroFill = fieldLength - toWrite;
                writeBytes(asBytes);
                writeZeros(zeroFill);
            }
        } catch (UnsupportedEncodingException e) {
            // should never happen
            throw new IllegalStateException(e);
        }
    }

    /**
     * Writes a string to the buffer.  The string is converted to an ASCII
     * encoded array of bytes.  The number of bytes written is equal to the
     * string length.
     *
     * @param str the string to write
     */
    public void writeString(String str) {
        try {
            writeBytes(str.getBytes(ASCII));
        } catch (UnsupportedEncodingException e) {
            // should never happen
            throw new IllegalStateException(e);
        }
    }

    /**
     * Writes a single byte to the buffer.
     *
     * @param b the byte to write
     */
    public void writeByte(byte b) {
        bb.put(b);
    }

    /**
     * Writes the specified byte array to the buffer.
     *
     * @param bytes the bytes to write
     */
    public void writeBytes(byte[] bytes) {
        bb.put(bytes);
    }

    /**
     * Writes the bytes in the passed in writer up to it's write index to
     * the buffer.
     *
     * @param w packet writer containing the bytes to write
     */
    public void writeBytes(PacketWriter w) {
        bb.put(w.array(), 0, w.wi());
    }

    /**
     * Writes the given byte value the specified number of times.
     *
     * @param b the byte value to write
     * @param count the number of times to write it
     */
    public void writeBytes(byte b, int count) {
        for (int i=0; i<count; i++)
            bb.put(b);
    }

    /**
     * Writes the specified number of NUL bytes (0x00) to the buffer.
     *
     * @param count the number of zeros to write
     */
    public void writeZeros(int count) {
        writeBytes((byte)0, count);
    }

    /**
     * Writes an int to the buffer as four bytes.
     *
     * @param i the int value
     */
    public void writeInt(int i) {
        bb.putInt(i);
    }

    /**
     * Writes a long to the buffer as eight bytes.
     *
     * @param l the long value
     */
    public void writeLong(long l) {
        bb.putLong(l);
    }

    /**
     * Writes a single byte (unsigned 8-bit) equivalent to the given value.
     *
     * @param uv the unsigned value to write
     * @throws IllegalArgumentException if u is not 0..2^8-1
     */
    public void writeU8(short uv) {
        bb.put(toU8(uv));
    }

    /**
     * Writes a single byte (unsigned 8-bit) equivalent to the given value.
     *
     * @param uv the unsigned value to write
     * @throws IllegalArgumentException if u is not 0..2^8-1
     */
    public void writeU8(int uv) {
        bb.put(toU8(uv));
    }

    /**
     * Writes two bytes (unsigned 16-bit) equivalent to the given value.
     *
     * @param uv the unsigned value to write
     * @throws IllegalArgumentException if u is not 0..2^16-1
     */
    public void writeU16(int uv) {
        bb.putShort(toU16(uv));
    }

    /**
     * Set two bytes (unsigned 16-bit) equivalent to the given value at the
     * given index.
     *
     * @param idx the absolute index in the buffer
     * @param uv the unsigned value to write
     * @throws IllegalArgumentException if u is not 0..2^16-1
    */
    public void setU16(int idx, int uv) {
        bb.putShort(idx, toU16(uv));
    }

    /**
     * Set two bytes (unsigned 32-bit) equivalent to the given value at the
     * given index.
     *
     * @param idx the absolute index in the buffer
     * @param uv the unsigned value to write
     * @throws IllegalArgumentException if u is not 0..2^16-1
    */
    public void setU32(int idx, long uv) {
        bb.putInt(idx, toU32(uv));
    }

    /**
     * Writes three bytes (unsigned 24-bit) equivalent to the given value.
     *
     * @param uv the unsigned value to write
     * @throws IllegalArgumentException if u is not 0..2^24-1
     */
    public void writeU24(int uv) {
        if ((uv & U24_MASK) != uv)
            throw new IllegalArgumentException("Not u24: " + uv);
        writeU8(U8_MASK & (uv >> BIT_SHIFT_16));
        writeU8(U8_MASK & (uv >> BIT_SHIFT_8));
        writeU8(U8_MASK & uv);
    }

    /**
     * Writes four bytes (unsigned 32-bit) equivalent to the given value.
     *
     * @param uv the unsigned value to write
     * @throws IllegalArgumentException if u is not 0..2^32-1
     */
    public void writeU32(long uv) {
        bb.putInt(toU32(uv));
    }
    
}
