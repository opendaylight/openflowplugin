/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;
import static org.opendaylight.util.ByteUtils.toHexArrayString;
import static org.opendaylight.util.packet.ProtocolUtils.hex;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.Test;

import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.net.U16Id;
import org.opendaylight.util.net.U32Id;


/**
 * Unit tests for PacketWriter.
 *
 * @author Simon Hunt
 * @author Frank Wood
 */
public class PacketWriterTest extends PacketTest {

    private PacketWriter pkt;

    private static final int BASIC_LEN = 3;

    private void printByteArray(String s, Object v) {
        print("{}: {} --> {}", s, v, toHexArrayString(pkt.array()));
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        pkt = new PacketWriter(BASIC_LEN);
        print(pkt);
        assertEquals(AM_NEQ, BASIC_LEN, pkt.writableBytes());
        assertEquals(AM_NEQ, 0, pkt.wi());
    }

    @Test
    public void wrByte() {
        print(EOL + "wrByte()");
        pkt = new PacketWriter(1);
        pkt.writeByte(EXP_BYTE);
        printByteArray("Byte", EXP_BYTE);
        assertArrayEquals(AM_NEQ, EXP_BYTE_ARRAY, pkt.array());
    }

    @Test
    public void wrByteArray() {
        print(EOL + "wrByteArray()");
        pkt = new PacketWriter(EXP_MAGIC.length);
        pkt.writeBytes(EXP_MAGIC);
        printByteArray("Magic", toHexArrayString(EXP_MAGIC));
        assertArrayEquals(AM_NEQ, EXP_MAGIC, pkt.array());
    }

    @Test
    public void fillByteArray() {
        print(EOL + "fillByteArray()");
        int len = SKIPPED_BYTES.length;
        pkt = new PacketWriter(len);
        pkt.writeBytes(EXP_SKIP_BYTE, len);
        printByteArray("Fill", "9 x6");
        assertArrayEquals(AM_NEQ, SKIPPED_BYTES, pkt.array());
    }

    @Test
    public void wrZeros() {
        print(EOL + "wrZeros()");
        byte[] nines = SKIPPED_BYTES.clone();
        pkt = new PacketWriter(nines);
        print(pkt);
        pkt.writeZeros(4);
        printByteArray("Zero", "0 x4");
        assertArrayEquals(AM_NEQ, EXP_ZEROED_BYTES, pkt.array());
    }

    @Test
    public void wrInt() {
        print(EOL + "wrInt()");
        pkt = new PacketWriter(4);
        pkt.writeInt(EXP_INT);
        printByteArray("Int", EXP_INT);
        assertArrayEquals(AM_NEQ, EXP_INT_ARRAY, pkt.array());
    }

    @Test
    public void wrLong() {
        print(EOL + "wrLong()");
        pkt = new PacketWriter(8);
        pkt.writeLong(EXP_LONG);
        printByteArray("Long", EXP_LONG);
        assertArrayEquals(AM_NEQ, EXP_LONG_ARRAY, pkt.array());
    }

    @Test
    public void wrU8() {
        print(EOL + "wrU8()");
        pkt = new PacketWriter(1);
        pkt.writeU8(EXP_U8);
        printByteArray("U8", hex(EXP_U8));
        assertArrayEquals(AM_NEQ, EXP_U8_ARRAY, pkt.array());
    }

    @Test
    public void wrU16() {
        print(EOL + "wrU16()");
        pkt = new PacketWriter(2);
        pkt.writeU16(EXP_U16);
        printByteArray("U16", hex(EXP_U16));
        assertArrayEquals(AM_NEQ, EXP_U16_ARRAY, pkt.array());
    }

    @Test
    public void wrU24() {
        print(EOL + "wrU24()");
        pkt = new PacketWriter(3);
        pkt.writeU24(EXP_U24);
        printByteArray("U24", hex(EXP_U24));
        assertArrayEquals(AM_NEQ, EXP_U24_ARRAY, pkt.array());
    }

    @Test
    public void wrU32() {
        print(EOL + "wrU32()");
        pkt = new PacketWriter(4);
        pkt.writeU32(EXP_U32);
        printByteArray("U32", hex(EXP_U32));
        assertArrayEquals(AM_NEQ, EXP_U32_ARRAY, pkt.array());
    }

    @Test
    public void wrIpv4Address() {
        print(EOL + "wrIpv4Address()");
        pkt = new PacketWriter(IpAddress.IP_V4_ADDR_SIZE);
        pkt.write(EXP_IPv4);
        printByteArray("IPv4", EXP_IPv4);
        assertArrayEquals(AM_NEQ, EXP_IPv4_ARRAY, pkt.array());
    }

    @Test
    public void wrIpv6Address() {
        print(EOL + "wrIpv6Address()");
        pkt = new PacketWriter(IpAddress.IP_V6_ADDR_SIZE);
        pkt.write(EXP_IPv6);
        printByteArray("IPv6", EXP_IPv6);
        assertArrayEquals(AM_NEQ, EXP_IPv6_ARRAY, pkt.array());
    }

    @Test
    public void wrMacAddress() {
        print(EOL + "wrMacAddress()");
        pkt = new PacketWriter(MacAddress.MAC_ADDR_SIZE);
        pkt.write(EXP_MAC);
        printByteArray("MAC", EXP_MAC);
        assertArrayEquals(AM_NEQ, EXP_MAC_ARRAY, pkt.array());
    }

    @Test
    public void wrEthernetType() {
        print(EOL + "wrEthernetType()");
        pkt = new PacketWriter(2);
        pkt.write(EXP_ETHER);
        printByteArray("Ether", EXP_ETHER);
        assertArrayEquals(AM_NEQ, EXP_ETHER_ARRAY, pkt.array());
    }

    @Test
    public void wrIpProtocol() {
        print(EOL + "wrIpProtocol()");
        pkt = new PacketWriter(1);
        pkt.write(EXP_IPP);
        printByteArray("IPP", EXP_IPP);
        assertArrayEquals(AM_NEQ, EXP_IPP_ARRAY, pkt.array());
    }

    @Test
    public void wrIcmpv4Type() {
        print(EOL + "wrIcmpv4Type()");
        pkt = new PacketWriter(1);
        pkt.write(EXP_ICMPv4);
        printByteArray("ICMPv4", EXP_ICMPv4);
        assertArrayEquals(AM_NEQ, EXP_ICMPv4_ARRAY, pkt.array());
    }

    @Test
    public void wrIcmpv6Type() {
        print(EOL + "wrIcmpv6Type()");
        pkt = new PacketWriter(1);
        pkt.write(EXP_ICMPv6);
        printByteArray("ICMPv6", EXP_ICMPv6);
        assertArrayEquals(AM_NEQ, EXP_ICMPv6_ARRAY, pkt.array());
    }

    @Test
    public void wrPortNumber() {
        print(EOL + "wrPortNumber()");
        pkt = new PacketWriter(U16Id.LENGTH_IN_BYTES);
        pkt.write(EXP_PORT);
        printByteArray("Port", EXP_PORT);
        assertArrayEquals(AM_NEQ, EXP_PORT_ARRAY, pkt.array());
    }

    @Test
    public void wrBigPortNumber() {
        print(EOL + "wrBigPortNumber()");
        pkt = new PacketWriter(U32Id.LENGTH_IN_BYTES);
        pkt.write(EXP_BPORT);
        printByteArray("B.Port", EXP_BPORT);
        assertArrayEquals(AM_NEQ, EXP_BPORT_ARRAY, pkt.array());
    }

    @Test
    public void wrStringField() {
        print(EOL + "wrStringField()");
        pkt = new PacketWriter(STR_FIELD_LEN);
        pkt.writeString(EXP_STR, STR_FIELD_LEN);
        printByteArray("String", EXP_STR);
        assertArrayEquals(AM_NEQ, EXP_STR_ARRAY, pkt.array());
    }

    @Test
    public void wrStringFieldWithTruncation() {
        print(EOL + "wrStringFieldWithTruncation()");
        String orig = "Missy Boo Boo";
        String trunc = "Missy Boo";
        int fLen = 10;
        byte[] exp = {
                0x4d, 0x69, 0x73, 0x73, 0x79, 0x20, 0x42, 0x6f, 0x6f, 0x00
        };
        pkt = new PacketWriter(fLen);
        pkt.writeString(orig, fLen);
        printByteArray("Str.Trunc.", "\"" + orig + "\"");
        assertArrayEquals(AM_NEQ, exp, pkt.array());

        PacketReader rdr = new PacketReader(ByteBuffer.wrap(pkt.array()));
        String what = rdr.readString(fLen);
        print("Back out: \"{}\"", what);
        assertEquals(AM_NEQ, trunc, what);
    }

    @Test
    public void duplicatePseudoPacket() {
        print(EOL + "duplicatePseudoPacket()");
        byte[] expData = slurpedBytes(PACKET_DATA);

        pkt = new PacketWriter(expData.length);

        pkt.writeBytes(EXP_MAGIC);
        pkt.write(EXP_IPv4);
        pkt.write(EXP_IPv6);
        pkt.write(EXP_MAC);
        pkt.write(EXP_ETHER);
        pkt.write(EXP_IPP);
        pkt.write(EXP_ICMPv4);
        pkt.write(EXP_ICMPv6);
        pkt.write(EXP_PORT);
        pkt.write(EXP_BPORT);
        pkt.writeByte(EXP_BYTE);
        pkt.writeBytes((byte) 9, 6);
        pkt.writeInt(EXP_INT);
        pkt.writeLong(EXP_LONG);
        pkt.writeU8(EXP_U8);
        pkt.writeU16(EXP_U16);
        pkt.writeU24(EXP_U24);
        pkt.writeU32(EXP_U32);
        pkt.writeString(EXP_STR, STR_FIELD_LEN);

        // We should be done
        assertEquals(AM_NEQ, 0, pkt.writableBytes());
        assertArrayEquals(AM_NEQ, expData, pkt.array());
    }

    @Test
    public void writeBytesWriter() {
        print(EOL + "writeBytesWriter()");

        PacketWriter pwInt = new PacketWriter(21);
        pwInt.writeInt(EXP_INT);

        PacketWriter pwLong = new PacketWriter(31);
        pwLong.writeLong(EXP_LONG);

        pkt = new PacketWriter(pwInt.wi() + pwLong.wi());
        byte[] array = pkt.array();
        assertEquals(AM_NEQ, 12, array.length);

        pkt.writeBytes(pwInt);
        pkt.writeBytes(pwLong);

        array = pkt.array();
        assertEquals(AM_NEQ, 12, array.length);

        byte[] intArray = Arrays.copyOfRange(array, 0, 4);
        byte[] longArray = Arrays.copyOfRange(array, 4, 12);
        assertArrayEquals(AM_NEQ, EXP_INT_ARRAY, intArray);
        assertArrayEquals(AM_NEQ, EXP_LONG_ARRAY, longArray);
    }

}
