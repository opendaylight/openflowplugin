/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import java.nio.BufferUnderflowException;

import org.opendaylight.util.net.EthernetType;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.net.TcpUdpPort;
import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;
import static org.opendaylight.util.packet.ProtocolId.*;
import static org.opendaylight.util.packet.ProtocolUtils.hex;
import static org.junit.Assert.*;


/**
 * General encoding and decoding unit tests.
 *
 * @author Frank Wood
 */
public class CodecTest extends PacketTest {

    private static final String ETH2_BAD_SRC = "eth2-bad-src.hex";
    private static final String ETH2_IP_BAD_DST = "eth2-ip-bad-dst.hex";
    private static final String ETH2_UNKNOWN = "eth2-unknown.hex";

    private static final String ETH2_IP_UNKNOWN = "eth2-ip-unknown.hex";
    private static final int ETH2_IP_UNKNOWN_LEN = 60;

    private static final String ETH2_IP_UDP_UNKNOWN = "eth2-ip-udp-unknown.hex";

    private static final MacAddress DST_MAC = mac("52:54:00:9c:d1:65");
    private static final MacAddress SRC_MAC = mac("08:2e:5f:69:c4:40");

    private static final IpAddress SRC_IP = ip("10.10.102.121");
    private static final IpAddress DST_IP = ip("10.10.102.202");

    private static final int IP_IDENT = 3051;
    private static final int IP_TTL = 64;

    private static final MacAddress DST_MAC2 = mac("00:e0:b1:49:39:02");
    private static final MacAddress SRC_MAC2 = mac("00:13:72:25:fa:cd");

    private static final IpAddress SRC_IP2 = ip("172.22.178.234");
    private static final IpAddress DST_IP2 = ip("10.10.8.240");

    private static final TcpUdpPort PORT = TcpUdpPort.udpPort(0xffff);

    private static final EthernetType ET_UNKNOWN = EthernetType.valueOf(0x0602);

    private static final byte[] UKNOWN_PROTOCOL_BYTES = {
        (byte)0xab, (byte)0xcd, (byte)0xef, (byte)0xab, (byte)0xcd
    };

    private void verifyBadEthException(ProtocolException e) {
        assertNotNull(e.packet());
        assertNotNull(e.protocol());
        assertNotNull(e.reader());

        assertEquals(0, e.packet().size());
        assertEquals(DST_MAC, ((Ethernet)e.protocol()).dstAddr());
        assertEquals(ProtocolException.class, e.getCause().getClass());
        assertEquals(BufferUnderflowException.class,
                     e.getCause().getCause().getClass());
        
        print(e.decodeDebugString());
    }

    @Test
    public void decodeBadEthReader() {
        print(EOL + "decodeBadEthReader()");
        PacketReader r = getPacketReader(ETH2_BAD_SRC);

        try {
            Codec.decodeEthernet(r);
            fail("Exception should have been thrown");
        } catch (ProtocolException e) {
            verifyBadEthException(e);
        }
    }

    @Test
    public void decodeBadEthBytes() {
        print(EOL + "decodeBadEthBytes()");
        try {
            Codec.decodeEthernet(getPacketReader(ETH2_BAD_SRC).array());
            fail("Exception should have been thrown");
        } catch (ProtocolException e) {
            verifyBadEthException(e);
        }
    }

    private void verifyBadIpException(ProtocolException e) {
        assertNotNull(e.packet());
        assertNotNull(e.protocol());
        assertNotNull(e.reader());

        assertEquals(1, e.packet().size());
        Ethernet eth = e.packet().get(ETHERNET);
        assertEquals(DST_MAC, eth.dstAddr());
        assertEquals(SRC_MAC, eth.srcAddr());

        assertEquals(SRC_IP, ((Ip)e.protocol()).srcAddr());

        assertEquals(ProtocolException.class, e.getCause().getClass());
        assertEquals(BufferUnderflowException.class,
                     e.rootCause().getClass());
        
        print(e.decodeDebugString());
    }

    @Test
    public void decodeBadIpReader() {
        print(EOL + "decodeBadIpReader()");

        // verify that we can stop the decoding before we hit the error
        PacketReader r = getPacketReader(ETH2_IP_BAD_DST);
        Ethernet eth = Codec.decodeEthernet(r, 1).get(ETHERNET);
        assertEquals(DST_MAC, eth.dstAddr());
        assertEquals(SRC_MAC, eth.srcAddr());

        try {
            r = getPacketReader(ETH2_IP_BAD_DST);
            Codec.decodeEthernet(r);
            fail("Exception should have been thrown");
        } catch (ProtocolException e) {
            verifyBadIpException(e);
        }
    }

    @Test
    public void decodeBadIpBytes() {
        print(EOL + "decodeIpBytes()");

        // verify that we can stop the decoding before we hit the error
        Ethernet eth =
            Codec.decodeEthernet(getPacketReader(ETH2_IP_BAD_DST).array(),
                                 1).get(ETHERNET);
        assertEquals(DST_MAC, eth.dstAddr());
        assertEquals(SRC_MAC, eth.srcAddr());
    }

    @Test
    public void decodeUnknown() {
        print(EOL + "decodeUnknown()");
        PacketReader r = getPacketReader(ETH2_UNKNOWN);

        Packet pkt = Codec.decodeEthernet(r);
        print(pkt);

        assertEquals(2, pkt.size());
        assertEquals(UNKNOWN, pkt.innermostId());

        Ethernet eth = pkt.get(ETHERNET);
        assertEquals(DST_MAC, eth.dstAddr());
        assertEquals(SRC_MAC, eth.srcAddr());
        assertEquals(ET_UNKNOWN, eth.type());

        UnknownProtocol up = pkt.get(UNKNOWN);
        assertArrayEquals(UKNOWN_PROTOCOL_BYTES, up.bytes());
    }

    @Test
    public void decodeUnknownIp() {
        print(EOL + "decodeUnknownIp()");
        PacketReader r = getPacketReader(ETH2_IP_UNKNOWN);

        Packet pkt = Codec.decodeEthernet(r);
        print(pkt);

        assertEquals(3, pkt.size());
        assertEquals(UNKNOWN, pkt.innermostId());

        Ethernet eth = pkt.get(ETHERNET);
        assertEquals(DST_MAC, eth.dstAddr());
        assertEquals(SRC_MAC, eth.srcAddr());
        assertEquals(EthernetType.IPv4, eth.type());

        Ip ip = pkt.get(IP);
        assertEquals(SRC_IP, ip.srcAddr());
        assertEquals(DST_IP, ip.dstAddr());
        assertEquals(IpType.RESERVED, ip.type());

        UnknownProtocol up = pkt.get(UNKNOWN);
        assertArrayEquals(UKNOWN_PROTOCOL_BYTES, up.bytes());

        assertArrayEquals(new ProtocolId[] {ETHERNET, IP, UNKNOWN},
                          pkt.protocolIds().toArray(new ProtocolId[0]));
    }

    @Test
    public void encodeUnknownIp() {
        print(EOL + "encodeUnknownIp()");

        Ethernet eth = new Ethernet.Builder()
            .dstAddr(DST_MAC)
            .srcAddr(SRC_MAC)
            .build();

        Ip ip = new Ip.Builder()
            .srcAddr(SRC_IP)
            .dstAddr(DST_IP)
            .ident(IP_IDENT)
            .type(IpType.RESERVED)
            .doNotFrag(true)
            .ttl(IP_TTL)
            .build();

        UnknownProtocol up = new UnknownProtocol.Builder()
            .bytes(UKNOWN_PROTOCOL_BYTES)
            .build();

        Packet pkt = new Packet(eth, ip, up);

        print(pkt.toDebugString());

        byte[] encoding = Codec.encode(pkt);
        print("enc=" + hex(encoding));

        PacketReader r = getPacketReader(ETH2_IP_UNKNOWN);
        byte[] expected = r.readBytes(ETH2_IP_UNKNOWN_LEN);
        print("exp=" + hex(expected));

        assertArrayEquals(expected, encoding);
    }

    @Test
    public void decodeUnknownUdp() {
        print(EOL + "decodeUnknownUdp()");
        PacketReader r = getPacketReader(ETH2_IP_UDP_UNKNOWN);

        Packet pkt = Codec.decodeEthernet(r);
        print(pkt);

        assertEquals(4, pkt.size());
        assertEquals(UNKNOWN, pkt.innermostId());

        Ethernet eth = pkt.get(ETHERNET);
        assertEquals(DST_MAC2, eth.dstAddr());
        assertEquals(SRC_MAC2, eth.srcAddr());
        assertEquals(EthernetType.IPv4, eth.type());

        Ip ip = pkt.get(IP);
        assertEquals(SRC_IP2, ip.srcAddr());
        assertEquals(DST_IP2, ip.dstAddr());
        assertEquals(IpType.UDP, ip.type());

        Udp udp = pkt.get(UDP);
        assertEquals(PORT, udp.srcPort());
        assertEquals(PORT, udp.dstPort());

        UnknownProtocol up = pkt.get(UNKNOWN);
        assertArrayEquals(UKNOWN_PROTOCOL_BYTES, up.bytes());
    }

    @Test
    public void verifyFailure() {
        print(EOL + "verifyFailure()");

        try {
            new Ethernet.Builder().build();
            fail("Exception should have been thrown");
        } catch (ProtocolException e) {
            print(e);
            assertNull(e.packet());
            assertNull(e.reader());
            assertNotNull(e.protocol());
            print("Verify failed " + e.protocol().toDebugString());

            assertEquals(ProtocolException.class, e.getCause().getClass());
            assertEquals(ProtocolException.class, e.rootCause().getClass());
        }
    }

    @Test
    public void noIp() {
        print(EOL + "noIp()");

        try {
            Ethernet eth = new Ethernet.Builder()
                    .dstAddr(DST_MAC2)
                    .srcAddr(SRC_MAC2)
                    .type(EthernetType.IPv4)
                    .build();

            Udp udp = new Udp.Builder()
                    .srcPort(PORT)
                    .dstPort(PORT)
                    .build();

            Packet pkt = new Packet(eth, udp);
            Codec.encode(pkt);
            fail("Exception should have been thrown");
        } catch (ProtocolException e) {
            print(e);
        }
    }

}
