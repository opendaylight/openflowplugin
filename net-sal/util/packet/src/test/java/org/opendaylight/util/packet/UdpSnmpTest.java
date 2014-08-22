/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.net.TcpUdpPort;
import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;
import static org.opendaylight.util.net.EthernetType.IPv4;
import static org.opendaylight.util.packet.ProtocolId.*;
import static org.opendaylight.util.packet.ProtocolUtils.hex;
import static org.junit.Assert.*;


/**
 * UDP SNMP packet unit tests.
 *
 * @author Frank Wood
 */
public class UdpSnmpTest extends PacketTest {

    private static final String TCP_HTTP = "eth2-ip-udp-snmp.hex";
    private static final int TCP_HTTP_LEN = 82;

    private static final MacAddress DST_MAC = mac("08:00:37:15:e6:bc");
    private static final MacAddress SRC_MAC = mac("00:12:3f:4a:33:d2");

    private static final IpAddress SRC_IP = ip("172.31.19.54");
    private static final IpAddress DST_IP = ip("172.31.19.73");

    private static final int IP_HDR_LEN = 20;
    private static final int IP_TOTAL_LEN = 68;
    private static final int IP_CHECK_SUM = 0x11d2;
    private static final int IP_IDENT = 43545;

    private static final int TTL = 128;

    private static final int FRAG_OFFSET = 0;

    private static final int UDP_CHECK_SUM = 0x0ca5;
    private static final int UDP_LEN = 48;

    private static final TcpUdpPort SRC_PORT = TcpUdpPort.udpPort(15916);
    private static final TcpUdpPort DST_PORT = TcpUdpPort.udpPort(161);

    private static final byte[] SNMP_DATA = new byte[] {
        0x30, 0x26, 0x02, 0x01, 0x00, 0x04, 0x06, 0x70,
        0x75, 0x62, 0x6c, 0x69, 0x63, (byte)0xa0, 0x19, 0x02,
        0x01, 0x26, 0x02, 0x01, 0x00, 0x02, 0x01, 0x00,
        0x30, 0x0e, 0x30, 0x0c, 0x06, 0x08, 0x2b, 0x06,
        0x01, 0x02, 0x01, 0x01, 0x02, 0x00, 0x05, 0x00
    };

    private Packet decodeUdpSnmpFile() {
        return Codec.decodeEthernet(getPacketReader(TCP_HTTP));
    }

    private void verifyUdpSnmp(Packet pkt) {

        assertEquals(4, pkt.size());
        assertEquals(UNKNOWN, pkt.innermostId());
        assertTrue(pkt.hasAll(ETHERNET, IP, UDP, UNKNOWN));
        assertTrue(pkt.hasAny(ETHERNET, IP, UDP, UNKNOWN, DHCP));
        assertFalse(pkt.hasAny(DHCP));
        assertFalse(pkt.hasAll(ETHERNET, IP, UDP, UNKNOWN, DHCP));

        Ethernet eth = pkt.get(ETHERNET);
        assertEquals(DST_MAC, eth.dstAddr());
        assertEquals(SRC_MAC, eth.srcAddr());
        assertEquals(IPv4, eth.type());

        Ip ip = pkt.get(IP);
        assertEquals(IP_HDR_LEN, ip.hdrLen());
        assertEquals(IpTosDsfc.CS0, ip.tosDsfc());
        assertEquals(IpTosEcn.NOT_ECT, ip.tosEcn());
        assertEquals(IP_TOTAL_LEN, ip.totalLen());
        assertEquals(IP_IDENT, ip.ident());

        assertFalse(ip.doNotFrag());
        assertFalse(ip.moreFragToCome());
        assertEquals(FRAG_OFFSET, ip.fragOffset());

        assertEquals(TTL, ip.ttl());
        assertEquals(IpType.UDP, ip.type());
        assertEquals(IP_CHECK_SUM, ip.checkSum());
        assertEquals(SRC_IP, ip.srcAddr());
        assertEquals(DST_IP, ip.dstAddr());

        Udp udp = pkt.get(UDP);
        assertEquals(SRC_PORT, udp.srcPort());
        assertEquals(DST_PORT, udp.dstPort());
        assertEquals(UDP_LEN, udp.len());
        assertEquals(UDP_CHECK_SUM, udp.checkSum());

        UnknownProtocol up = pkt.get(UNKNOWN);
        assertArrayEquals(SNMP_DATA, up.bytes());
    }

    private Packet createUdpSnmp() {
        Ethernet eth = new Ethernet.Builder()
            .dstAddr(DST_MAC)
            .srcAddr(SRC_MAC)
            .type(IPv4)
            .build();

        Ip ip = new Ip.Builder()
            .ident(IP_IDENT)
            .ttl(TTL)
            .type(IpType.UDP)
            .srcAddr(SRC_IP)
            .dstAddr(DST_IP)
            .build();

        Udp udp = new Udp.Builder()
            .srcPort(SRC_PORT)
            .dstPort(DST_PORT)
            .build();

        UnknownProtocol up = new UnknownProtocol.Builder()
            .bytes(SNMP_DATA)
            .build();

        return new Packet(eth, ip, udp, up);
    }

    @Test
    public void decode() {
        print(EOL + "decode()");
        Packet pkt = decodeUdpSnmpFile();
        print(pkt);
        verifyUdpSnmp(pkt);
    }

    @Test
    public void encode() {
        print(EOL + "encode()");
        Packet pkt = createUdpSnmp();
        print(pkt);

        byte[] encoding = Codec.encode(pkt);
        print("enc=" + hex(encoding));

        PacketReader r = getPacketReader(TCP_HTTP);
        byte[] expected = r.readBytes(TCP_HTTP_LEN);
        print("exp=" + hex(expected));

        assertArrayEquals(expected, encoding);
    }

}
