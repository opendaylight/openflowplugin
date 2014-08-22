/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import org.opendaylight.util.net.EthernetType;
import org.opendaylight.util.net.IpAddress;
import org.opendaylight.util.net.MacAddress;
import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;
import static org.opendaylight.util.packet.ProtocolId.*;
import static org.opendaylight.util.packet.ProtocolUtils.hex;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


/**
 * MPLS packet unit tests.
 *
 * @author Frank Wood
 */
public class MplsTest extends PacketTest {

    private static final String ETH2_MPLS = "eth2-mpls-ip-icmp.hex";
    private static final int ETH2_MPLS_LEN = 118;

    private static final MacAddress DST_MAC = mac("00:30:96:e6:fc:39");
    private static final MacAddress SRC_MAC = mac("00:30:96:05:28:38");

    private static final int LABEL = 29;
    private static final int TTL = 255;

    private static final IpAddress SRC_IP = ip("10.1.2.1");
    private static final IpAddress DST_IP = ip("10.34.0.1");

    private static final int IP_IDENT = 10;
    private static final int IP_CHECKSUM = 0xa56a;

    private static final int ICMP_IDENT = 2617;
    private static final int ICMP_SEQ_NUM = 1579;

    private static final byte[] ICMP_PAYLOAD = new byte[] {
        0x00, 0x00, 0x00, 0x00, 0x00, 0x1f, 0x33, 0x50,
        (byte)0xab, (byte)0xcd, (byte)0xab, (byte)0xcd,
        (byte)0xab, (byte)0xcd, (byte)0xab, (byte)0xcd,
        (byte)0xab, (byte)0xcd, (byte)0xab, (byte)0xcd,
        (byte)0xab, (byte)0xcd, (byte)0xab, (byte)0xcd,
        (byte)0xab, (byte)0xcd, (byte)0xab, (byte)0xcd,
        (byte)0xab, (byte)0xcd, (byte)0xab, (byte)0xcd,
        (byte)0xab, (byte)0xcd, (byte)0xab, (byte)0xcd,
        (byte)0xab, (byte)0xcd, (byte)0xab, (byte)0xcd,
        (byte)0xab, (byte)0xcd, (byte)0xab, (byte)0xcd,
        (byte)0xab, (byte)0xcd, (byte)0xab, (byte)0xcd,
        (byte)0xab, (byte)0xcd, (byte)0xab, (byte)0xcd,
        (byte)0xab, (byte)0xcd, (byte)0xab, (byte)0xcd,
        (byte)0xab, (byte)0xcd, (byte)0xab, (byte)0xcd,
        (byte)0xab, (byte)0xcd, (byte)0xab, (byte)0xcd,
        (byte)0xab, (byte)0xcd, (byte)0xab, (byte)0xcd,
        (byte)0xab, (byte)0xcd, (byte)0xab, (byte)0xcd,
    };

    private Packet decodeMplsFile() {
        return Codec.decodeEthernet(getPacketReader(ETH2_MPLS));
    }

    private void verifyMpls(Packet pkt) {
        assertEquals(4, pkt.size());
        assertEquals(ICMP, pkt.innermostId());

        Ethernet eth = pkt.get(ETHERNET);
        assertEquals(DST_MAC, eth.dstAddr());
        assertEquals(SRC_MAC, eth.srcAddr());
        assertEquals(EthernetType.MPLS_U, eth.type());

        Mpls mpls = pkt.get(MPLS);
        Mpls.Header[] hdrs = mpls.headers();
        assertEquals(1, hdrs.length);

        assertEquals(LABEL, hdrs[0].label());
        assertEquals(TTL, hdrs[0].ttl());

        Ip ip = pkt.get(IP);
        assertEquals(IpType.ICMP, ip.type());
        assertEquals(IP_IDENT, ip.ident());
        assertEquals(IP_CHECKSUM, ip.checkSum());
        assertEquals(TTL, ip.ttl());
    }

    private Packet createMpls() {
        Ethernet eth = new Ethernet.Builder()
            .dstAddr(DST_MAC)
            .srcAddr(SRC_MAC)
            .type(EthernetType.MPLS_U)
            .build();

        Mpls mpls = new Mpls.Builder()
            .headers(new Mpls.Header[] { new Mpls.Header(LABEL, TTL) })
            .build();

        Ip ip = new Ip.Builder()
            .srcAddr(SRC_IP)
            .dstAddr(DST_IP)
            .type(IpType.ICMP)
            .ident(IP_IDENT)
            .ttl(TTL)
            .build();

        Icmp icmp = new Icmp.Builder()
            .typeCode(IcmpTypeCode.ECHO_REQ)
            .ident(ICMP_IDENT)
            .seqNum(ICMP_SEQ_NUM)
            .bytes(ICMP_PAYLOAD)
            .build();

        return new Packet(eth, mpls, ip, icmp);
    }

    @Test
    public void decode() {
        print(EOL + "decode()");
        Packet pkt = decodeMplsFile();
        print(pkt.toDebugString());
        verifyMpls(pkt);
    }

    @Test
    public void encode() {
        print(EOL + "encode()");
        Packet pkt = createMpls();
        print(pkt.toDebugString());

        byte[] encoding = Codec.encode(pkt);
        print("enc=" + hex(encoding));

        PacketReader r = getPacketReader(ETH2_MPLS);
        byte[] expected = r.readBytes(ETH2_MPLS_LEN);
        print("exp=" + hex(expected));

        assertArrayEquals(expected, encoding);
    }

}
