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
import static org.opendaylight.util.packet.Dns.RecordType.SOA;
import static org.opendaylight.util.packet.Dns.ResponseCode.NAME_ERROR;
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
import org.opendaylight.util.packet.Dns.SoaData;


/**
 * DNS A (IPv4) response unit tests.
 *
 * @author Frank Wood
 */
public class DnsAResp3Test extends PacketTest {

    private static final String TEST_DATA_FILE
        = "eth2-ip-udp-dns-a3-resp.hex";
    
    private static final int PKT_LEN = 149;

    private static final MacAddress SRC_MAC = mac("78:ac:c0:2d:a0:00");
    private static final MacAddress DST_MAC = mac("00:24:81:b3:e4:de");

    private static final IpAddress DST_IP = ip("16.181.49.80");
    private static final IpAddress SRC_IP = ip("16.110.135.52");
    private static final int IP_TTL = 245;
    private static final int IP_IDENT = 0xc9c7;
    private static final int IP_CHECK_SUM = 0xe1f6;
    
    private static final TcpUdpPort DST_PORT = TcpUdpPort.udpPort(12082);
    private static final TcpUdpPort SRC_PORT = TcpUdpPort.udpPort(53);
    private static final int UDP_LEN = 115;
    private static final int UDP_CHECK_SUM = 0xc3df;

    private static final int TXID = 0x9793;
    
    private static final String QUERY_NAME = "www.xxxhpxxx.com";

    private static final String AUTH_NAME = "com";
    private static final int AUTH_TTL = 780;
    private static final String PRI_NAME = "a.gtld-servers.net";
    private static final String MB_NAME = "nstld.verisign-grs.com";
    private static final int SERIAL = 1373892002;
    private static final int REFRESH_INTERVAL = 1800;
    private static final int RETRY_INTERVAL = 900;
    private static final int EXPIRE_LIMIT = 604800;
    private static final int MIN_TTL = 86400;
        
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
        assertEquals(NAME_ERROR, dns.respCode());
        assertEquals(0, dns.answers().length);
        assertEquals(0, dns.additionals().length);
        
        Record[] queries = dns.queries();
        assertEquals(1, queries.length);
        Record q = queries[0];
        assertEquals(QUERY_NAME, q.name());
        assertEquals(INTERNET, q.clsType());
        assertEquals(A, q.recType());        
        
        assertEquals(1, dns.authorities().length);
        ResRecord<?> r = dns.authorities()[0];
        assertEquals(AUTH_NAME, r.name());
        assertEquals(INTERNET, r.clsType());
        assertEquals(SOA, r.recType());
        assertEquals(AUTH_TTL, r.ttl());
        SoaData sd = (SoaData) r.data();
        assertEquals(PRI_NAME, sd.nameServer());
        assertEquals(MB_NAME, sd.email());
        assertEquals(SERIAL, sd.serial());
        assertEquals(REFRESH_INTERVAL, sd.refreshSecs());
        assertEquals(RETRY_INTERVAL, sd.retrySecs());
        assertEquals(EXPIRE_LIMIT, sd.expireSecs());
        assertEquals(MIN_TTL, sd.minTtl());
    }

    private Packet create() {
        Ethernet eth = new Ethernet.Builder()
            .dstAddr(DST_MAC)
            .srcAddr(SRC_MAC)
            .type(IPv4)
            .build();

        Ip ip = new Ip.Builder()
            .ttl(IP_TTL)
            .doNotFrag(true)
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
            .respCode(NAME_ERROR)
            .query(QUERY_NAME, A)
            .authority(AUTH_NAME, SOA, AUTH_TTL,
                new SoaData(PRI_NAME, MB_NAME, SERIAL,
                            REFRESH_INTERVAL, RETRY_INTERVAL,
                            EXPIRE_LIMIT, MIN_TTL))
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
