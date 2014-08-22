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
import static org.opendaylight.util.packet.Dns.RecordType.ANY;
import static org.opendaylight.util.packet.Dns.RecordType.NSEC;
import static org.opendaylight.util.packet.Dns.RecordType.RRSIG;
import static org.opendaylight.util.packet.Dns.SigData.Algorithm.RSA_SHA1;
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
import org.opendaylight.util.packet.Dns.NextSecData;
import org.opendaylight.util.packet.Dns.OpCode;
import org.opendaylight.util.packet.Dns.Record;
import org.opendaylight.util.packet.Dns.ResRecord;
import org.opendaylight.util.packet.Dns.SigData;


/**
 * DNS ANY response unit tests.
 *
 * @author Frank Wood
 */
public class DnsAnyRespTest extends PacketTest {

    private static final String TEST_DATA_FILE
        = "eth2-ip-udp-dns-any-resp.hex";
    
    private static final int PKT_LEN = 470;

    private static final MacAddress SRC_MAC = mac("1c:7e:e5:58:4c:5d");
    private static final MacAddress DST_MAC = mac("00:24:81:b3:e4:de");

    private static final IpAddress DST_IP = ip("192.168.0.100");
    private static final IpAddress SRC_IP = ip("192.168.0.1");
    private static final int IP_TTL = 64;
    private static final int IP_IDENT = 0;
    private static final int IP_CHECK_SUM = 0xb76f;
    
    private static final TcpUdpPort DST_PORT = TcpUdpPort.udpPort(6144);
    private static final TcpUdpPort SRC_PORT = TcpUdpPort.udpPort(53);
    private static final int UDP_LEN = 436;
    private static final int UDP_CHECK_SUM = 0x0728;

    private static final int TXID = 0xafa5;
    private static final String QUERY_NAME = "www.isc.org";
    
    private static final int ANS_TTL0 = 3565;
    private static final int ANS_TTL1 = 25;
    
    private static final int NUM_LABELS = 3;
    
    private static final long ORIG_TTL0 = 3600;
    private static final long ORIG_TTL1 = 60;

    private static final long EXPIRATION = 1374773610L;
    private static final long SIGNED = 1372181610L;          
    private static final int SIGN_ID = 50012;
    private static final String SIGNERS_NAME = "isc.org";
    
    private static final byte[] SIGNITURE0 = new byte[] {
        0x73, (byte)0xf6, 0x12, (byte)0xaf,
        0x4d, (byte)0xd1, (byte)0x98, 0x7c,
        (byte)0x8d, (byte)0xa9, (byte)0x90, 0x07,
        0x09, (byte)0x86, 0x5e, (byte)0x85,
        0x2c, (byte)0xf6, 0x64, 0x0d,
        (byte)0xd0, 0x37, 0x05, 0x77,
        0x3c, (byte)0xb2, (byte)0xe0, 0x6e,
        0x4f, 0x08, 0x37, 0x52,
        0x44, (byte)0x9b, (byte)0x97, (byte)0xda,
        (byte)0x99, 0x62, (byte)0x8b, 0x0e,
        0x75, 0x6d, 0x1f, (byte)0xf0,
        (byte)0xb2, 0x58, (byte)0x95, (byte)0xa1,
        (byte)0x90, 0x72, 0x03, 0x60,
        (byte)0xb1, (byte)0xe2, 0x2d, (byte)0x90,
        0x06, 0x0c, (byte)0xd7, (byte)0xe6,
        (byte)0xc5, (byte)0xda, (byte)0xa2, 0x04,
        0x66, (byte)0x90,(byte) 0xf5, (byte)0xdb,
        (byte)0xb4, (byte)0xef, 0x59, 0x2f,
        (byte)0xe2, 0x4a, (byte)0x97, 0x48,
        0x33, (byte)0xf3, 0x61, 0x3f,
        0x0a, (byte)0xb9, 0x13, (byte)0x88,
        (byte)0xb3, 0x65, 0x00, 0x7d,
        0x5b, (byte)0xa9, 0x11, (byte)0x9d,
        (byte)0x80, 0x23, (byte)0xdf, (byte)0xe2,
        (byte)0xf3, 0x41, 0x30, 0x79,
        0x50, (byte)0xdf, (byte)0xd3, (byte)0xe7,
        (byte)0x8a, 0x5d, (byte)0xe5, (byte)0xd5,
        0x67, 0x01, 0x4a, (byte)0xde,
        (byte)0xad, 0x0d, (byte)0xbc, (byte)0xe1,
        (byte)0x84, 0x4e, 0x0f, (byte)0x99,
        0x1c, 0x14, 0x2f, 0x63,
        0x4e, 0x58, 0x29, 0x41,        
    };
    
    private static final byte[] SIGNITURE1 = new byte[] {
        (byte)0xb1, 0x13, 0x44, (byte)0xdd,
        0x1a, 0x54, 0x08, (byte)0xaa,
        0x13, (byte)0xe5, 0x77, (byte)0xd1,
        (byte)0xf5, 0x68, 0x09, (byte)0xc7,
        0x19, (byte)0x8e, (byte)0xdf, 0x1e,
        (byte)0x9d, 0x21, (byte)0xd8, (byte)0xf0,
        0x4b, 0x3c, 0x08, (byte)0xaa,
        0x19, (byte)0xb7, (byte)0xd4, 0x3b,
        0x59, (byte)0xa1, (byte)0x86, 0x46,
        (byte)0xfa, 0x3a, 0x31, 0x6d,
        (byte)0xa2, (byte)0xd9, (byte)0x94, (byte)0xc1,
        (byte)0xcd, (byte)0x92, 0x68, (byte)0xa7,
        0x34, 0x18, 0x50, 0x60,
        (byte)0xd5, 0x50, (byte)0xf6, 0x56,
        (byte)0xfd, (byte)0x9d, 0x6e, 0x22,
        0x27, 0x4f, 0x67, (byte)0xa5,
        (byte)0xe0, 0x00, 0x74, (byte)0x9b,
        0x72, (byte)0x85, (byte)0xc2, 0x2e,
        (byte)0x9b, (byte)0xd4, (byte)0xc2, (byte)0xf3,
        0x5a, 0x1f, 0x7a, (byte)0x9e,
        (byte)0x92, (byte)0x9c, (byte)0x94, (byte)0x9b,
        0x01, 0x3c, 0x19, (byte)0x9f,
        0x46, 0x3e, (byte)0xc0, 0x6f,
        (byte)0xcc, 0x3c, 0x6f, (byte)0xea,
        (byte)0x81, 0x03, 0x66, 0x30,
        0x6d, 0x51, 0x71, 0x14,
        0x2b, (byte)0xd2, 0x24, 0x03,
        0x49, 0x3b, 0x34, 0x1f,
        (byte)0xbf, (byte)0x9f, (byte)0x91, 0x47,
        0x6e, 0x3d, 0x52, 0x1c,
        0x1e, 0x4f, 0x07, 0x5e,
        0x31, (byte)0xde, 0x60, (byte)0xe2,
    };
    
    private static final String NEXT_DOMAIN_NAME = "www-dev.isc.org";
    private static final byte[] TYPE_BITMAP = new byte[] {
        0x00, 0x06, 0x40, 0x00, 0x00, 0x08, 0x00, 0x03
    };
    
    private static final IpAddress ANS_IP =
            IpAddress.valueOf("2001:4f8:0:2::69");
    
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
        assertEquals(ANY, q.recType());        
        
        assertEquals(4, dns.answers().length);
        
        ResRecord<?> r = dns.answers()[0];
        assertEquals(QUERY_NAME, r.name());
        assertEquals(INTERNET, r.clsType());
        assertEquals(RRSIG, r.recType());
        assertEquals(ANS_TTL0, r.ttl());
        SigData sd = (SigData) r.data();
        assertEquals(NSEC, sd.typeCovered());
        assertEquals(RSA_SHA1, sd.algorithm());
        assertEquals(NUM_LABELS, sd.labels());
        assertEquals(ORIG_TTL0, sd.origTtl());
        assertEquals(EXPIRATION, sd.expirationTs());
        assertEquals(SIGNED, sd.signedTs());
        assertEquals(SIGN_ID, sd.signingId());
        assertEquals(SIGNERS_NAME, sd.signersName());
        assertArrayEquals(SIGNITURE0, sd.signature());
        
        r = dns.answers()[1];
        assertEquals(QUERY_NAME, r.name());
        assertEquals(INTERNET, r.clsType());
        assertEquals(NSEC, r.recType());
        assertEquals(ANS_TTL0, r.ttl());
        NextSecData nsd = (NextSecData) r.data();
        assertEquals(NEXT_DOMAIN_NAME, nsd.domainName());
        assertArrayEquals(TYPE_BITMAP, nsd.typeBitmap());
        
        r = dns.answers()[2];
        assertEquals(QUERY_NAME, r.name());
        assertEquals(INTERNET, r.clsType());
        assertEquals(RRSIG, r.recType());
        assertEquals(ANS_TTL1, r.ttl());
        sd = (SigData) r.data();
        assertEquals(AAAA, sd.typeCovered());
        assertEquals(RSA_SHA1, sd.algorithm());
        assertEquals(NUM_LABELS, sd.labels());
        assertEquals(ORIG_TTL1, sd.origTtl());
        assertEquals(EXPIRATION, sd.expirationTs());
        assertEquals(SIGNED, sd.signedTs());
        assertEquals(SIGN_ID, sd.signingId());
        assertEquals(SIGNERS_NAME, sd.signersName());
        assertArrayEquals(SIGNITURE1, sd.signature());
        
        r = dns.answers()[3];
        assertEquals(QUERY_NAME, r.name());
        assertEquals(INTERNET, r.clsType());
        assertEquals(AAAA, r.recType());
        assertEquals(ANS_TTL1, r.ttl());
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
            .query(QUERY_NAME, ANY)
            .answers(
                new ResRecord<SigData>(QUERY_NAME, RRSIG, ANS_TTL0,
                        new SigData(NSEC, RSA_SHA1, NUM_LABELS, ORIG_TTL0,
                                    EXPIRATION, SIGNED, SIGN_ID, SIGNERS_NAME,
                                    SIGNITURE0)),
                new ResRecord<NextSecData>(QUERY_NAME, NSEC, ANS_TTL0,
                        new NextSecData(NEXT_DOMAIN_NAME, TYPE_BITMAP)),
                new ResRecord<SigData>(QUERY_NAME, RRSIG, ANS_TTL1,
                        new SigData(AAAA, RSA_SHA1, NUM_LABELS, ORIG_TTL1,
                                    EXPIRATION, SIGNED, SIGN_ID, SIGNERS_NAME,
                                    SIGNITURE1)),
                new ResRecord<IpAddress>(QUERY_NAME, AAAA, ANS_TTL1, ANS_IP)
            )
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
