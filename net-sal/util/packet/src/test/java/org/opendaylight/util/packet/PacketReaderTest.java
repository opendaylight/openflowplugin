/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.net.*;
import org.junit.Before;
import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.*;
import static org.opendaylight.util.StringUtils.EOL;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Unit tests for PacketReader.
 *
 * @author Simon Hunt
 * @author Frank Wood
 */
public class PacketReaderTest extends PacketTest {

    private static final int PACKET_LENGTH = 118;
    private static final String EXP_TO_STR =
            "java.nio.HeapByteBuffer[pos=0 lim=118 cap=118]";

    private PacketReader pkt;

    private PacketReader createPacketReader() {
        return getPacketReader(PACKET_DATA);
    }

    @Before
    public void setUp() {
        pkt = createPacketReader();
        assertEquals(AM_NEQ, 0, pkt.ri());
        assertEquals(AM_UXS, PACKET_LENGTH, pkt.array().length);
    }

    @Test
    public void toStr() {
        pkt = createPacketReader();
        assertEquals(AM_NEQ, EXP_TO_STR, pkt.toString());
    }

    @Test
    public void odometer() {
        print(EOL + "odometer()");
        pkt = createPacketReader();
        assertEquals(AM_NEQ, 0, pkt.odometer());

        byte[] magic = pkt.readBytes(EXP_MAGIC.length);
        print("Magic: " + ByteUtils.toHexArrayString(magic));
        assertArrayEquals(AM_NEQ, EXP_MAGIC, magic);

        assertEquals(AM_NEQ, EXP_MAGIC.length, pkt.odometer());

        pkt.resetOdometer();

        IpAddress ip = pkt.readIPv4Address();
        print(" IPv4: " + ip);
        assertEquals(AM_NEQ, EXP_IPv4, ip);

        assertEquals(AM_NEQ, IpAddress.IP_V4_ADDR_SIZE, pkt.odometer());

        pkt.resetIndex();

        magic = pkt.readBytes(EXP_MAGIC.length);
        print("Magic: " + ByteUtils.toHexArrayString(magic));
        assertArrayEquals(AM_NEQ, EXP_MAGIC, magic);

        assertEquals(AM_NEQ, EXP_MAGIC.length, pkt.odometer());
    }

    @Test
    public void basic() {
        print(EOL + "basic()");
        print(pkt);

        byte[] magic = pkt.readBytes(EXP_MAGIC.length);
        print("Magic: " + ByteUtils.toHexArrayString(magic));
        assertArrayEquals(AM_NEQ, EXP_MAGIC, magic);

        IpAddress ip = pkt.readIPv4Address();
        print(" IPv4: " + ip);
        assertEquals(AM_NEQ, EXP_IPv4, ip);

        ip = pkt.readIPv6Address();
        print(" IPv6: " + ip);
        assertEquals(AM_NEQ, EXP_IPv6, ip);

        // Peek into the future
        assertEquals(AM_NEQ, 0x01, pkt.peek(1));

        MacAddress mac = pkt.readMacAddress();
        print("  MAC: " + mac);
        assertEquals(AM_NEQ, EXP_MAC, mac);

        // Peek into the past
        assertEquals(AM_NEQ, 0xfa-B, pkt.peek(-3));

        EthernetType ether = pkt.readEthernetType();
        print("Ether: " + ether);
        assertEquals(AM_NEQ, EXP_ETHER, ether);

        IpProtocol ipp = pkt.readIpProtocol();
        print("  IPP: " + ipp);
        assertEquals(AM_NEQ, EXP_IPP, ipp);

        ICMPv4Type ict4 = pkt.readIcmpv4Type();
        print("ICMP4: " + ict4);
        assertEquals(AM_NEQ, EXP_ICMPv4, ict4);

        ICMPv6Type ict6 = pkt.readIcmpv6Type();
        print("ICMP6: " + ict6);
        assertEquals(AM_NEQ, EXP_ICMPv6, ict6);

        PortNumber pn = pkt.readPortNumber();
        print(" Port: " + pn);
        assertEquals(AM_NEQ, EXP_PORT, pn);

        BigPortNumber bpn = pkt.readBigPortNumber();
        print("BPort: " + bpn);
        assertEquals(AM_NEQ, EXP_BPORT, bpn);

        byte b = pkt.readByte();
        print(" byte: " + b);
        assertEquals(AM_NEQ, EXP_BYTE, b);

        print("skipping bytes...");
        pkt.skip(SKIPPED_BYTES.length);

        int i = pkt.readInt();
        print("  int: " + i);
        assertEquals(AM_NEQ, EXP_INT, i);

        long l = pkt.readLong();
        print(" long: " + l);
        assertEquals(AM_NEQ, EXP_LONG, l);

        short u8 = pkt.peekU8();
        print("p  u8: " + u8);
        assertEquals(AM_NEQ, EXP_U8, u8);

        u8 = pkt.readU8();
        print("   u8: " + u8);
        assertEquals(AM_NEQ, EXP_U8, u8);

        int u16 = pkt.readU16();
        print("  u16: " + u16);
        assertEquals(AM_NEQ, EXP_U16, u16);

        int u24 = pkt.peekU24();
        print("p u24: " + u24);
        assertEquals(AM_NEQ, EXP_U24, u24);

        u24 = pkt.readU24();
        print("  u24: " + u24);
        assertEquals(AM_NEQ, EXP_U24, u24);

        long u32 = pkt.readU32();
        print("  u32: " + u32);
        assertEquals(AM_NEQ, EXP_U32, u32);

        String str = pkt.readString(STR_FIELD_LEN);
        print("  Str: \"" + str + "\"");
        assertEquals(AM_NEQ, EXP_STR, str);

        // should be at the end of the buffer
        assertEquals("bytes left over", 0, pkt.readableBytes());
    }

    @Test
    public void settingReaderIndex() {
        print(EOL + "basic()");
        print(pkt);

        pkt.readLong();
        print(pkt);
        assertEquals(AM_NEQ, 8, pkt.ri());

        pkt.readLong();
        print(pkt);
        assertEquals(AM_NEQ, 16, pkt.ri());

        pkt.ri(7);
        print(pkt);
        assertEquals(AM_NEQ, 7, pkt.ri());

        pkt.readLong();
        print(pkt);
        assertEquals(AM_NEQ, 15, pkt.ri());
    }

    @Test
    public void takeItToTheLimit() {
        print(EOL + "takeItToTheLimit()");
        print(pkt);

        assertEquals(AM_UXS, 118, pkt.limit());
    }

    @Test
    public void peekingU8() {
        print(EOL + "peekingU8()");
        // first 4 bytes of the test packet are: 0xcafebabe
        assertEquals(AM_NEQ, pkt.peekU8(), 0xca);
        assertEquals(AM_NEQ, pkt.peekU8(1), 0xfe);
        assertEquals(AM_NEQ, pkt.peekU8(2), 0xba);
        assertEquals(AM_NEQ, pkt.peekU8(3), 0xbe);
    }

    @Test
    public void peekingU16() {
        print(EOL + "peekingU16()");
        // first 4 bytes of the test packet are: 0xcafebabe
        assertEquals(AM_NEQ, pkt.peekU16(), 0xcafe);
        assertEquals(AM_NEQ, pkt.peekU16(1), 0xfeba);
        assertEquals(AM_NEQ, pkt.peekU16(2), 0xbabe);
    }

    @Test
    public void peekingU24() {
        print(EOL + "peekingU24()");
        // first 4 bytes of the test packet are: 0xcafebabe
        assertEquals(AM_NEQ, pkt.peekU24(), 0xcafeba);
        assertEquals(AM_NEQ, pkt.peekU24(1), 0xfebabe);
    }
}
