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
import static org.opendaylight.util.packet.Dns.RecordType.AAAA;
import static org.opendaylight.util.packet.ProtocolId.DNS;
import static org.opendaylight.util.packet.ProtocolId.ETHERNET;
import static org.opendaylight.util.packet.ProtocolId.IP;
import static org.opendaylight.util.packet.ProtocolId.UDP;
import static org.opendaylight.util.packet.ProtocolUtils.hex;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.net.TcpUdpPort;
import org.opendaylight.util.packet.Dns.OpCode;
import org.opendaylight.util.packet.Dns.Record;
import org.opendaylight.util.packet.Dns.ResRecord;


/**
 * DNS AAAA (IPv6) response unit tests.
 *
 * @author Frank Wood
 */
public class DnsAaaaRespTest extends PacketTest {

    private static final String TEST_DATA_FILE
        = "eth2-ip-udp-dns-aaaa-resp.hex";
    
    private static final int PKT_LEN = 102;

    private static final MacAddress SRC_MAC = mac("00:c0:9f:32:41:8c");
    private static final MacAddress DST_MAC = mac("00:e0:18:b1:0c:ad");

    private static final IpAddress DST_IP = ip("192.168.170.8");
    private static final IpAddress SRC_IP = ip("192.168.170.20");
    private static final int IP_TTL = 128;
    private static final int IP_IDENT = 0xd05a;
    private static final int IP_CHECK_SUM = 0x94cc;
    
    private static final TcpUdpPort DST_PORT = TcpUdpPort.udpPort(32795);
    private static final TcpUdpPort SRC_PORT = TcpUdpPort.udpPort(53);
    private static final int UDP_LEN = 68;
    private static final int UDP_CHECK_SUM = 0x5f1b;

    private static final int TXID = 0xf0d4;
    private static final int ANS_TTL = 86400;
    
    private static final String QUERY_NAME = "www.netbsd.org";
    private static final IpAddress ANS_IP =
            IpAddress.valueOf("2001:4f8:4:7:2e0:81ff:fe52:9a6b");

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
        assertEquals(IP_CHECK_SUM, ip.checkSum());

        Udp udp = pkt.get(UDP);
        assertEquals(SRC_PORT, udp.srcPort());
        assertEquals(DST_PORT, udp.dstPort());
        assertEquals(UDP_LEN, udp.len());
        assertEquals(UDP_CHECK_SUM, udp.checkSum());

        Dns dns = pkt.get(DNS);
        assertEquals(TXID, dns.txId());
        assertFalse(dns.query());
        assertEquals(OpCode.QUERY, dns.opCode());
        assertTrue(dns.recurDesired());
        assertTrue(dns.svrRecurAvail());
        assertFalse(dns.trunc());
        assertFalse(dns.responderAuth());
        assertFalse(dns.authData());
        assertFalse(dns.checkDisabled());
        assertEquals(0, dns.authorities().length);
        assertEquals(0, dns.additionals().length);
        
        Record[] queries = dns.queries();
        assertEquals(1, queries.length);
        Record q = queries[0];
        assertEquals(QUERY_NAME, q.name());
        assertEquals(INTERNET, q.clsType());
        assertEquals(AAAA, q.recType());        
        
        assertEquals(1, dns.answers().length);
        ResRecord<?> r = dns.answers()[0];
        assertEquals(QUERY_NAME, r.name());
        assertEquals(INTERNET, r.clsType());
        assertEquals(AAAA, r.recType());
        assertEquals(ANS_TTL, r.ttl());
        IpAddress addr = (IpAddress) r.data();
        assertEquals(ANS_IP, addr);
    }

    private Packet create() {
        Ethernet eth = new Ethernet.Builder()
            .dstAddr(DST_MAC)
            .srcAddr(SRC_MAC)
            .type(IPv4)
            .build();

        Ip ip = new Ip.Builder()
            .ttl(IP_TTL)
            .type(IpType.UDP)
            .srcAddr(SRC_IP)
            .dstAddr(DST_IP)
            .ident(IP_IDENT)
            .build();

        Udp udp = new Udp.Builder()
            .srcPort(SRC_PORT)
            .dstPort(DST_PORT)
            .build();

        Dns dns = new Dns.Builder()
            .txId(TXID)
            .opCode(OpCode.QUERY)
            .recurDesired(true)
            .svrRecurAvail(true)
            .query(QUERY_NAME, AAAA)
            .answer(QUERY_NAME, AAAA, ANS_TTL, ANS_IP)
            .build();

        return new Packet(eth, ip, udp, dns);
    }

    @Test
    public void decode() {
        print(EOL + "decode()");
        Packet pkt = decodeFile();
        print(pkt.toDebugString());
        verify(pkt);
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

}
