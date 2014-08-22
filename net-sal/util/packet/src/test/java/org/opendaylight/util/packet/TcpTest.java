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
 * TCP packet unit tests.
 *
 * @author Frank Wood
 */
public class TcpTest extends PacketTest {

    private static final String ETH2_IP_TCP = "eth2-ip-tcp.hex";
    private static final int ETH2_IP_TCP_LEN = 66;

    private static final String ETH2_IP_TCP2 = "eth2-ip-tcp2.hex";
    private static final int ETH2_IP_TCP2_LEN = 66;

    private static final MacAddress DST_MAC = mac("52:54:00:9c:d1:65");
    private static final MacAddress SRC_MAC = mac("08:2e:5f:69:c4:40");

    private static final IpAddress SRC_IP = ip("10.10.102.121");
    private static final IpAddress DST_IP = ip("10.10.102.202");

    private static final int IP_HDR_LEN = 20;
    private static final int IP_TOTAL_LEN = 52;
    private static final int IP_CHECK_SUM = 0x4d82;
    private static final int IP_IDENT = 0x0beb;
    private static final int TTL = 64;
    private static final int SEQ_NUM = 0x43550692;
    private static final int ACK_NUM = 0x59e2bd09;
    private static final int WIN_SIZE = 0x08096;
    private static final int FLAGS = 0x10;
    private static final int FRAG_OFFSET = 0;

    private static final int TCP_CHECK_SUM = 0xeae9;
    private static final int TCP_HDR_LEN = 32;

    private static final TcpUdpPort SRC_PORT = TcpUdpPort.tcpPort(64088);
    private static final TcpUdpPort DST_PORT = TcpUdpPort.tcpPort(6633);
    private static final TcpUdpPort DST_PORT2 = TcpUdpPort.tcpPort(9999);

    private static final byte[] TCP_OPTIONS = new byte[] {
        0x01, 0x01, 0x08, 0x0a, 0x0f, (byte)0xc0, (byte)0x9b, (byte)0x8a,
        0x03, (byte)0xa2, 0x05, (byte)0xe4
    };

    private Packet decodeEth2IpTcpFile() {
        return Codec.decodeEthernet(getPacketReader(ETH2_IP_TCP));
    }

    private void verifyEth2IpTcp(Packet pkt) {
        assertEquals(3, pkt.size());
        assertEquals(TCP, pkt.innermostId());

        assertTrue(pkt.hasAll(ETHERNET, IP, TCP));
        assertTrue(pkt.hasAll(ETHERNET));
        assertTrue(pkt.hasAll(IP));
        assertTrue(pkt.hasAll(TCP));
        assertTrue(pkt.hasAny(ETHERNET, TCP));

        assertEquals(TCP, pkt.innermost().id());
        assertEquals(TCP, pkt.innermostId());

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

        assertTrue(ip.doNotFrag());
        assertFalse(ip.moreFragToCome());
        assertEquals(FRAG_OFFSET, ip.fragOffset());

        assertEquals(TTL, ip.ttl());
        assertEquals(IpType.TCP, ip.type());
        assertEquals(IP_CHECK_SUM, ip.checkSum());
        assertEquals(SRC_IP, ip.srcAddr());
        assertEquals(DST_IP, ip.dstAddr());

        Tcp tcp = pkt.get(TCP);
        assertEquals(SRC_PORT, tcp.srcPort());
        assertEquals(DST_PORT, tcp.dstPort());
        assertEquals(SEQ_NUM, tcp.seqNum());
        assertEquals(ACK_NUM, tcp.ackNum());
        assertEquals(TCP_HDR_LEN, tcp.hdrLen());
        assertEquals(FLAGS, tcp.flags());
        assertEquals(WIN_SIZE, tcp.winSize());
        assertEquals(TCP_CHECK_SUM, tcp.checkSum());
        assertArrayEquals(TCP_OPTIONS, tcp.options());
    }

    private Packet createEth2IpTcp() {
        Ethernet eth = new Ethernet.Builder()
            .dstAddr(DST_MAC)
            .srcAddr(SRC_MAC)
            .type(IPv4)
            .build();

        Ip ip = new Ip.Builder()
            .ident(IP_IDENT)
            .doNotFrag(true)
            .ttl(TTL)
            .type(IpType.TCP)
            .srcAddr(SRC_IP)
            .dstAddr(DST_IP)
            .build();

        Tcp tcp = new Tcp.Builder()
            .srcPort(SRC_PORT)
            .dstPort(DST_PORT)
            .seqNum(SEQ_NUM)
            .ackNum(ACK_NUM)
            .flags(FLAGS)
            .winSize(WIN_SIZE)
            .options(TCP_OPTIONS)
            .build();

        return new Packet(eth, ip, tcp);
    }

    @Test
    public void decode() {
        print(EOL + "decode()");
        Packet pkt = decodeEth2IpTcpFile();
        print(pkt);
        verifyEth2IpTcp(pkt);
    }

    @Test
    public void encode() {
        print(EOL + "encode()");
        Packet pkt = createEth2IpTcp();
        print(pkt);

        byte[] encoding = Codec.encode(pkt);
        print("enc=" + hex(encoding));

        PacketReader r = getPacketReader(ETH2_IP_TCP);
        byte[] expected = r.readBytes(ETH2_IP_TCP_LEN);
        print("exp=" + hex(expected));

        assertArrayEquals(expected, encoding);
    }

    @Test
    public void decodeModifyEncode() {
        print(EOL + "decodeModifyEncode()");

        Packet pkt = decodeEth2IpTcpFile();
        print("Original " + pkt.toDebugString());

        Tcp tcp2 = new Tcp.Builder((Tcp)pkt.get(TCP))
            .dstPort(DST_PORT2).build();

        Packet pkt2 = new Packet(pkt.get(ETHERNET), pkt.get(IP), tcp2);
        print("Modified " + pkt2.toDebugString());

        byte[] encoding2 = Codec.encode(pkt2);
        print("enc=" + hex(encoding2));

        PacketReader r2 = getPacketReader(ETH2_IP_TCP2);
        byte[] expected2 = r2.readBytes(ETH2_IP_TCP2_LEN);
        print("exp=" + hex(expected2));

        assertArrayEquals(expected2, encoding2);
    }

}
