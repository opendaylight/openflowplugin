/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;
import static org.opendaylight.util.net.EthernetType.IPv4;
import static org.opendaylight.util.packet.Dns.ClassType.INTERNET;
import static org.opendaylight.util.packet.Dns.RecordType.A;
import static org.opendaylight.util.packet.ProtocolId.DNS;
import static org.opendaylight.util.packet.ProtocolId.ETHERNET;
import static org.opendaylight.util.packet.ProtocolId.GRE;
import static org.opendaylight.util.packet.ProtocolId.IP;
import static org.opendaylight.util.packet.ProtocolId.UDP;
import static org.opendaylight.util.packet.ProtocolUtils.hex;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.opendaylight.util.junit.TestTools.StopWatch;
import org.opendaylight.util.net.EthernetType;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.net.TcpUdpPort;
import org.opendaylight.util.packet.Dns.OpCode;
import org.opendaylight.util.packet.Dns.Record;


/**
 * GRE tunnel DNS query unit tests.
 *
 * @author Frank Wood
 */
public class GreDnsQueryTest extends PacketTest {

    private static final String TEST_DATA_FILE = "eth2-gre-dns-query.hex";
    
    private static final int PKT_LEN = 122;

    private static final MacAddress DST_MAC = mac("b4:b5:2f:bf:84:83");
    private static final MacAddress SRC_MAC = mac("84:34:97:02:e1:c0");

    private static final IpAddress SRC_IP = ip("15.146.194.150");
    private static final IpAddress DST_IP = ip("15.146.194.98");
    private static final int IP_TTL = 64;
    private static final int IP_IDENT = 0x810e;
    private static final int IP_CHECK_SUM = 0x1538;
    
    private static final long GRE_KEY = 0xce5;
    private static final EthernetType GRE_PROTO = EthernetType.valueOf(0x6558);
    
    private static final MacAddress TNL_DST_MAC = mac("6c:3b:e5:39:f8:b4");
    private static final MacAddress TNL_SRC_MAC = mac("00:1a:4b:e2:a8:4a");

    private static final int TNL_VLAN_ID = 2;
    
    private static final IpAddress TNL_SRC_IP = ip("192.168.10.2");
    private static final IpAddress TNL_DST_IP = ip("192.168.10.3");
    private static final int TNL_IP_TTL = 64;
    private static final int TNL_IP_CHECK_SUM = 0xa559;
    
    private static final TcpUdpPort TNL_SRC_PORT = TcpUdpPort.udpPort(34417);
    private static final TcpUdpPort TNL_DST_PORT = TcpUdpPort.udpPort(53);
    private static final int TNL_UDP_LEN = 42;
    private static final int TNL_UDP_CHECK_SUM = 0x1757;

    private static final int TNL_TXID = 0xd42a;
    
    private static final String QUERY_NAME = "daisy.ubuntu.com";

    private Packet decodeFile() {
        return Codec.decodeEthernet(getPacketReader(TEST_DATA_FILE));
    }

    private void verify(Packet pkt) {
        Ethernet eth = pkt.get(ETHERNET);
        assertEquals(DST_MAC, eth.dstAddr());
        assertEquals(SRC_MAC, eth.srcAddr());
        assertEquals(IPv4, eth.type());

        Ip ip = pkt.get(IP);
        assertEquals(SRC_IP, ip.srcAddr());
        assertEquals(DST_IP, ip.dstAddr());
        assertEquals(IP_TTL, ip.ttl());
        assertEquals(IP_IDENT, ip.ident());
        assertEquals(IP_CHECK_SUM, ip.checkSum());

        Gre gre = pkt.get(GRE);
        assertEquals(0, gre.checkSum());
        assertEquals(GRE_KEY, gre.key());
        assertEquals(Gre.NONE, gre.seqNum());
        assertEquals(0, gre.version());
        assertEquals(GRE_PROTO, gre.protoType());

        Ethernet eth2 = pkt.get(ETHERNET, 1);
        assertEquals(TNL_DST_MAC, eth2.dstAddr());
        assertEquals(TNL_SRC_MAC, eth2.srcAddr());
        assertEquals(TNL_VLAN_ID, eth2.vlanId());
        assertEquals(IPv4, eth2.type());

        Ip ip2 = pkt.get(IP, 1);
        assertEquals(TNL_SRC_IP, ip2.srcAddr());
        assertEquals(TNL_DST_IP, ip2.dstAddr());
        assertEquals(TNL_IP_TTL, ip2.ttl());
        assertEquals(TNL_IP_CHECK_SUM, ip2.checkSum());
        
        Udp udp = pkt.get(UDP);
        assertEquals(TNL_SRC_PORT, udp.srcPort());
        assertEquals(TNL_DST_PORT, udp.dstPort());
        assertEquals(TNL_UDP_LEN, udp.len());
        assertEquals(TNL_UDP_CHECK_SUM, udp.checkSum());

        Dns dns = pkt.get(DNS);
        assertEquals(TNL_TXID, dns.txId());
        assertTrue(dns.query());
        assertEquals(OpCode.QUERY, dns.opCode());
        assertTrue(dns.recurDesired());
        assertFalse(dns.svrRecurAvail());
        assertFalse(dns.trunc());
        assertFalse(dns.responderAuth());
        assertFalse(dns.authData());
        assertFalse(dns.checkDisabled());
        assertEquals(0, dns.answers().length);
        assertEquals(0, dns.authorities().length);
        assertEquals(0, dns.additionals().length);
        
        Record[] queries = dns.queries();
        assertEquals(1, queries.length);
        Record q = queries[0];
        
        assertEquals(QUERY_NAME, q.name());
        assertEquals(INTERNET, q.clsType());
        assertEquals(A, q.recType());
    }

    private Packet create() {
        Ethernet eth = new Ethernet.Builder()
            .dstAddr(DST_MAC)
            .srcAddr(SRC_MAC)
            .type(IPv4)
            .build();

        Ip ip = new Ip.Builder()
            .ttl(IP_TTL)
            .type(IpType.GRE)
            .srcAddr(SRC_IP)
            .dstAddr(DST_IP)
            .ident(IP_IDENT)
            .doNotFrag(true)
            .build();

        Gre gre = new Gre.Builder()
            .protoType(GRE_PROTO)
            .key(GRE_KEY)
            .build();
        
        Ethernet eth2 = new Ethernet.Builder()
            .dstAddr(TNL_DST_MAC)
            .srcAddr(TNL_SRC_MAC)
            .vlanId(TNL_VLAN_ID)
            .type(IPv4)
            .build();
        
        Ip ip2 = new Ip.Builder()
            .ttl(TNL_IP_TTL)
            .type(IpType.UDP)
            .srcAddr(TNL_SRC_IP)
            .dstAddr(TNL_DST_IP)
            .doNotFrag(true)
            .build();
        
        Udp udp = new Udp.Builder()
            .srcPort(TNL_SRC_PORT)
            .dstPort(TNL_DST_PORT)
            .build();

        Dns dns = new Dns.Builder()
            .txId(TNL_TXID)
            .query(true)
            .opCode(OpCode.QUERY)
            .recurDesired(true)
            .query(QUERY_NAME, A)
            .build();

        return new Packet(eth, ip, gre, eth2, ip2, udp, dns);
    }

    @Test
    public void decode() {
        print(EOL + "decode()");
        Packet pkt = decodeFile();
        print(pkt);
        verify(pkt);
    }

    @Test
    public void packetInnermostVsGet() {
        print(EOL + "packetInnermostVsGet()");
        Packet pkt = create();
        assertEquals(pkt.get(6), pkt.innermost(DNS));
        assertEquals(pkt.innermostId(), pkt.innermost(DNS).id());
        assertEquals(pkt.get(4), pkt.innermost(IP));
        assertEquals(pkt.get(1), pkt.get(IP));
        assertNotSame(pkt.get(1), pkt.get(4));
        assertNotSame(pkt.get(IP), pkt.innermost(IP));
    }
    
    @Test
    public void encode() {
        print(EOL + "encode()");
        Packet pkt = create();
        print(pkt.toDebugString());

        byte[] encoding = Codec.encode(pkt);
        print("enc=" + hex(encoding));

        PacketReader r = getPacketReader(TEST_DATA_FILE);
        byte[] expected = r.readBytes(PKT_LEN);
        print("exp=" + hex(expected));
        
        assertArrayEquals(expected, encoding);
    }

    @Test
    public void performance() {
        byte[] buf = slurpedBytes(TEST_DATA_FILE);
        print("Packet size: " + buf.length);
        
        final int N = 30000;
        
        decodeLoop(buf, N);
        decodeLoop(buf, N);
        decodeLoop(buf, N);
        decodeLoop(buf, N);
        decodeLoop(buf, N);
        
        encodeLoop(buf, N);
        encodeLoop(buf, N);
        encodeLoop(buf, N);
        encodeLoop(buf, N);
        encodeLoop(buf, N);
        
        decodeEncodeLoop(buf, N);
        decodeEncodeLoop(buf, N);
        decodeEncodeLoop(buf, N);
        decodeEncodeLoop(buf, N);
    }

    private void decodeLoop(byte[] buf, int iterations) {
        StopWatch watch = new StopWatch("Decode");
        for (int n=0; n<iterations; n++) {
            Packet pkt = Codec.decodeEthernet(buf);
            assertTrue(pkt.has(DNS));
        }
        print(watch.stop().toString(iterations));
    }
    
    private void encodeLoop(byte[] buf, int iterations) {
        StopWatch watch = new StopWatch("Encode");
        Packet pkt = Codec.decodeEthernet(buf);
        for (int n=0; n<iterations; n++) {
            byte[] encBuf = Codec.encode(pkt);
            assertEquals(buf.length, encBuf.length);
        }
        print(watch.stop().toString(iterations));
    }

    private void decodeEncodeLoop(byte[] buf, int iterations) {
        StopWatch watch = new StopWatch("Decode/Encode");
        for (int n=0; n<iterations; n++) {
            Packet pkt = Codec.decodeEthernet(buf);
            assertTrue(pkt.has(DNS));
            
            byte[] encBuf = Codec.encode(pkt);
            assertEquals(buf.length, encBuf.length);
        }
        print(watch.stop().toString(iterations));
    }
    
}
