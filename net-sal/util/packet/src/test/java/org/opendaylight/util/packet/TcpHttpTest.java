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

import java.io.UnsupportedEncodingException;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;
import static org.opendaylight.util.StringUtils.UTF8;
import static org.opendaylight.util.net.EthernetType.IPv4;
import static org.opendaylight.util.packet.ProtocolId.*;
import static org.opendaylight.util.packet.ProtocolUtils.hex;
import static org.junit.Assert.*;


/**
 * TCP HTTP packet unit tests.
 *
 * @author Frank Wood
 */
public class TcpHttpTest extends PacketTest {

    private static final String TCP_HTTP = "eth2-ip-tcp-http.hex";
    private static final int TCP_HTTP_LEN = 533;

    private static final MacAddress DST_MAC = mac("fe:ff:20:00:01:00");
    private static final MacAddress SRC_MAC = mac("00:00:01:00:00:00");

    private static final IpAddress SRC_IP = ip("145.254.160.237");
    private static final IpAddress DST_IP = ip("65.208.228.223");

    private static final int IP_HDR_LEN = 20;
    private static final int IP_TOTAL_LEN = 519;
    private static final int IP_CHECK_SUM = 0x09010;
    private static final int IP_IDENT = 0x0f45;

    private static final int TTL = 128;

    private static final int SEQ_NUM = 0x038affe14;
    private static final int ACK_NUM = 0x0114c618c;
    private static final int WIN_SIZE = 9660;
    private static final int FLAGS = 0x18;
    private static final int FRAG_OFFSET = 0;

    private static final int TCP_CHECK_SUM = 0xa958;
    private static final int TCP_HDR_LEN = 20;

    private static final TcpUdpPort SRC_PORT = TcpUdpPort.tcpPort(3372);
    private static final TcpUdpPort DST_PORT = TcpUdpPort.tcpPort(80);

    private static final String HTTP_DATA =
        "GET /download.html HTTP/1.1\r\n" +
        "Host: www.ethereal.com\r\n" +
        "User-Agent: Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.6) Gecko/20040113\r\n" +
        "Accept: text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,image/jpeg,image/gif;q=0.2,*/*;q=0.1\r\n" +
        "Accept-Language: en-us,en;q=0.5\r\n" +
        "Accept-Encoding: gzip,deflate\r\n" +
        "Accept-Charset: ISO-8859-1,utf-8;q=0.7,*;q=0.7\r\n" +
        "Keep-Alive: 300\r\n" +
        "Connection: keep-alive\r\n" +
        "Referer: http://www.ethereal.com/development.html\r\n" +
        "\r\n";

    private Packet decodeTcpHttpFile() {
        return Codec.decodeEthernet(getPacketReader(TCP_HTTP));
    }

    private void verifyTcpHttp(Packet pkt) {

        assertEquals(4, pkt.size());
        assertEquals(UNKNOWN, pkt.innermostId());
        assertTrue(pkt.hasAll(ETHERNET, IP, TCP, UNKNOWN));
        assertTrue(pkt.hasAny(ETHERNET, IP, TCP, UNKNOWN));
        assertTrue(pkt.hasAny(IP, ARP));

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

        UnknownProtocol up = pkt.get(UNKNOWN);
        try {
            assertArrayEquals(HTTP_DATA.getBytes(UTF8), up.bytes());
        } catch (UnsupportedEncodingException e) {
            fail(e.toString());
        }
    }

    private Packet createTcpHttp() {
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
            .build();

        byte[] httpBytes = null;
        try {
            httpBytes = HTTP_DATA.getBytes(UTF8);
        } catch (UnsupportedEncodingException e) {
            fail(e.toString());
        }

        UnknownProtocol up = new UnknownProtocol.Builder()
            .bytes(httpBytes)
            .build();

        return new Packet(eth, ip, tcp, up);
    }

    @Test
    public void decode() {
        print(EOL + "decode()");
        Packet pkt = decodeTcpHttpFile();
        print(pkt);
        verifyTcpHttp(pkt);
    }

    @Test
    public void encode() {
        print(EOL + "encode()");
        Packet pkt = createTcpHttp();
        print(pkt);

        byte[] encoding = Codec.encode(pkt);
        print("enc=" + hex(encoding));

        PacketReader r = getPacketReader(TCP_HTTP);
        byte[] expected = r.readBytes(TCP_HTTP_LEN);
        print("exp=" + hex(expected));

        assertArrayEquals(expected, encoding);
    }

}
