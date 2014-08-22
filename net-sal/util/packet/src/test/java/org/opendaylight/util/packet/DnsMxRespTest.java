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
import static org.opendaylight.util.packet.Dns.RecordType.MX;
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
import org.opendaylight.util.packet.Dns.MxData;
import org.opendaylight.util.packet.Dns.OpCode;
import org.opendaylight.util.packet.Dns.Record;
import org.opendaylight.util.packet.Dns.ResRecord;


/**
 * DNS MX response unit tests.
 *
 * @author Frank Wood
 */
public class DnsMxRespTest extends PacketTest {

    private static final String TEST_DATA_FILE
        = "eth2-ip-udp-dns-mx-resp.hex";
    
    private static final int PKT_LEN = 298;

    private static final MacAddress SRC_MAC = mac("00:c0:9f:32:41:8c");
    private static final MacAddress DST_MAC = mac("00:e0:18:b1:0c:ad");

    private static final IpAddress DST_IP = ip("192.168.170.8");
    private static final IpAddress SRC_IP = ip("192.168.170.20");
    private static final int IP_TTL = 128;
    private static final int IP_IDENT = 0xccbb;
    private static final int IP_CHECK_SUM = 0x97a7;
    
    private static final TcpUdpPort DST_PORT = TcpUdpPort.udpPort(32795);
    private static final TcpUdpPort SRC_PORT = TcpUdpPort.udpPort(53);
    private static final int UDP_LEN = 264;
    private static final int UDP_CHECK_SUM = 0xd6f3;

    private static final int TXID = 0xf76f;
    private static final int ANS_TTL = 552;
    private static final int ADD_TTL = 600;
    
    private static final String QUERY_NAME = "google.com";
    private static final int[] ANS_PREF = {
        40, 10, 10, 10, 10, 40
    };
    private static final String[] ANS_NAMES = {
        "smtp4.google.com",
        "smtp5.google.com",
        "smtp6.google.com",
        "smtp1.google.com",
        "smtp2.google.com",
        "smtp3.google.com",
    };
    private static final String[] ADD_IPS = {
        "216.239.37.26",
        "64.233.167.25",
        "66.102.9.25",
        "216.239.57.25",
        "216.239.37.25",
        "216.239.57.26",
    };

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
        
        Record[] queries = dns.queries();
        assertEquals(1, queries.length);
        Record q = queries[0];
        assertEquals(QUERY_NAME, q.name());
        assertEquals(INTERNET, q.clsType());
        assertEquals(MX, q.recType());        
        
        assertEquals(6, dns.answers().length);
        for (int i=0; i<dns.answers().length; i++) {
            ResRecord<?> r = dns.answers()[i];
            assertEquals(QUERY_NAME, r.name());
            assertEquals(INTERNET, r.clsType());
            assertEquals(MX, r.recType());
            assertEquals(ANS_TTL, r.ttl());
            MxData mx = (MxData) r.data();
            assertEquals(ANS_PREF[i], mx.pref());
            assertEquals(ANS_NAMES[i], mx.name());
        }

        assertEquals(6, dns.additionals().length);
        for (int i=0; i<dns.additionals().length; i++) {
            ResRecord<?> r = dns.additionals()[i];
            assertEquals(ANS_NAMES[i], r.name());
            assertEquals(INTERNET, r.clsType());
            assertEquals(A, r.recType());
            assertEquals(ADD_TTL, r.ttl());
            assertEquals(ADD_IPS[i], ((IpAddress) r.data()).toString());
        }
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

        ResRecord<?>[] ans = new ResRecord<?>[6];
        for (int i=0; i<6; i++) {
            MxData mx = new MxData(ANS_PREF[i], ANS_NAMES[i]);
            ans[i] = new ResRecord<MxData>(QUERY_NAME, MX, ANS_TTL, mx);
        }
        
        ResRecord<?>[] add = new ResRecord<?>[6];
        for (int i=0; i<6; i++) {
            IpAddress addIp = IpAddress.valueOf(ADD_IPS[i]);
            add[i] = new ResRecord<IpAddress>(ANS_NAMES[i], A, ADD_TTL, addIp);
        }
        
        Dns dns = new Dns.Builder()
            .txId(TXID)
            .opCode(OpCode.QUERY)
            .recurDesired(true)
            .svrRecurAvail(true)
            .query(QUERY_NAME, MX)
            .answers(ans)
            .additionals(add)
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
