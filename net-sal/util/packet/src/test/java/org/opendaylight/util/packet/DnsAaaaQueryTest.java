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


/**
 * DNS AAAA (IPv6 host) query unit tests.
 *
 * @author Frank Wood
 */
public class DnsAaaaQueryTest extends PacketTest {

    private static final String TEST_DATA_FILE
        = "eth2-ip-udp-dns-aaaa-query.hex";
    
    private static final int PKT_LEN = 74;

    private static final MacAddress DST_MAC = mac("00:c0:9f:32:41:8c");
    private static final MacAddress SRC_MAC = mac("00:e0:18:b1:0c:ad");

    private static final IpAddress SRC_IP = ip("192.168.170.8");
    private static final IpAddress DST_IP = ip("192.168.170.20");
    private static final int IP_IDENT = 0x6f4c;
    private static final int IP_TTL = 64;
    private static final int IP_CHECK_SUM = 0xf5f6;
    
    private static final TcpUdpPort SRC_PORT = TcpUdpPort.udpPort(32795);
    private static final TcpUdpPort DST_PORT = TcpUdpPort.udpPort(53);
    private static final int UDP_LEN = 40;
    private static final int UDP_CHECK_SUM = 0x3432;

    private static final int TXID = 0xf0d4;
    private static final String QUERY_NAME = "www.netbsd.org";

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
        assertEquals(IP_IDENT, ip.ident());
        assertEquals(IP_TTL, ip.ttl());
        assertEquals(IP_CHECK_SUM, ip.checkSum());

        Udp udp = pkt.get(UDP);
        assertEquals(SRC_PORT, udp.srcPort());
        assertEquals(DST_PORT, udp.dstPort());
        assertEquals(UDP_LEN, udp.len());
        assertEquals(UDP_CHECK_SUM, udp.checkSum());

        Dns dns = pkt.get(DNS);
        assertEquals(TXID, dns.txId());
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
        assertEquals(AAAA, q.recType());
        
        // verify equals method
        Record aRec = new Record(QUERY_NAME, AAAA);
        assertEquals(aRec, q);
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
            .doNotFrag(true)
            .build();

        Udp udp = new Udp.Builder()
            .srcPort(SRC_PORT)
            .dstPort(DST_PORT)
            .build();

        Dns dns = new Dns.Builder()
            .txId(TXID)
            .query(true)
            .opCode(OpCode.QUERY)
            .recurDesired(true)
            .query(QUERY_NAME, AAAA)
            .build();

        return new Packet(eth, ip, udp, dns);
    }

    @Test
    public void decode() {
        print(EOL + "decode()");
        Packet pkt = decodeFile();
        print(pkt);
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
