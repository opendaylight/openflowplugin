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
import org.junit.Assert;
import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;
import static org.opendaylight.util.net.EthernetType.IPv4;
import static org.opendaylight.util.packet.ProtocolId.*;
import static org.opendaylight.util.packet.ProtocolUtils.hex;
import static org.junit.Assert.*;


/**
 * ICMP packet unit tests.
 *
 * @author Frank Wood
 */
public class IcmpTest extends PacketTest {

    private static final String ICMP_REQ = "eth2-icmp-req.hex";
    private static final String ICMP_REPLY = "eth2-icmp-reply.hex";
    private static final int ICMP_REQ_REPLY_LEN = 74;

    private static final String ICMP_DST_UNR = "eth2-icmp-dst-unr.hex";
    private static final int ICMP_DST_UNR_LEN = 60;

    private static final MacAddress DST_MAC = mac("08:86:3b:33:87:e8");
    private static final MacAddress SRC_MAC = mac("44:1e:a1:ce:5c:e2");

    private static final IpAddress DST_IP = ip("192.168.1.1");
    private static final IpAddress SRC_IP = ip("192.168.1.10");

    private static final int IP_CHECKSUM = 0x7e4f;

    private static final int IP_REQ_IDENT = 14614;
    private static final int REQ_TTL = 128;

    private static final int IP_REPLY_IDENT = 9835;
    private static final int REPLY_TTL = 64;

    private static final int ICMP_CHECKSUM = 0x4cca;

    private static final int ICMP_CHECKSUM_2 = 0xecea;

    private static final int ICMP_IDENT = 1;
    private static final int SEQ_NUM = 145;

    private static final byte[] DST_UNR_PAYLOAD = new byte[] {
        0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08
    };

    private Packet decodeIcmpReqFile() {
        return Codec.decodeEthernet(getPacketReader(ICMP_REQ));
    }

    private void verifyIcmpReq(Packet pkt) {
        assertEquals(3, pkt.size());
        assertEquals(ICMP, pkt.innermostId());

        Ethernet eth = pkt.get(ETHERNET);
        assertEquals(DST_MAC, eth.dstAddr());
        assertEquals(SRC_MAC, eth.srcAddr());
        assertEquals(IPv4, eth.type());

        Ip ip = pkt.get(IP);
        assertEquals(IpTosDsfc.CS0, ip.tosDsfc());
        assertEquals(IpTosEcn.NOT_ECT, ip.tosEcn());
        assertEquals(60, ip.totalLen());
        assertEquals(IP_REQ_IDENT, ip.ident());
        assertFalse(ip.doNotFrag());
        assertFalse(ip.moreFragToCome());
        assertEquals(0, ip.fragOffset());
        assertEquals(REQ_TTL, ip.ttl());
        assertEquals(IpType.ICMP, ip.type());
        assertEquals(IP_CHECKSUM, ip.checkSum());
        assertEquals(SRC_IP, ip.srcAddr());
        assertEquals(DST_IP, ip.dstAddr());

        Icmp icmp = pkt.get(ICMP);
        assertEquals(IcmpTypeCode.ECHO_REQ, icmp.typeCode());
        assertEquals(ICMP_CHECKSUM, icmp.checkSum());
        assertEquals(ICMP_IDENT, icmp.ident());
        assertEquals(SEQ_NUM, icmp.seqNum());
        assertEquals(32, icmp.bytes().length);
    }

    protected Packet createIcmpReq() {
        Ethernet eth = new Ethernet.Builder()
            .dstAddr(DST_MAC)
            .srcAddr(SRC_MAC)
            .type(IPv4)
            .build();

        Ip ip = new Ip.Builder()
            .ident(IP_REQ_IDENT)
            .ttl(REQ_TTL)
            .type(IpType.ICMP)
            .srcAddr(SRC_IP)
            .dstAddr(DST_IP)
            .build();

        // cheat and grab the bytes from the hex file
        Packet pkt = decodeIcmpReqFile();

        Icmp icmp = new Icmp.Builder()
            .typeCode(IcmpTypeCode.ECHO_REQ)
            .ident(ICMP_IDENT)
            .seqNum(SEQ_NUM)
            .bytes(((Icmp) pkt.get(ICMP)).bytes())
            .build();

        return new Packet(eth, ip, icmp);
    }

    @Test
    public void decode() {
        print(EOL + "decode()");
        Packet pkt = decodeIcmpReqFile();
        print(pkt);
        verifyIcmpReq(pkt);
    }

    @Test
    public void encode() {
        print(EOL + "encode()");
        Packet pkt = createIcmpReq();
        print(pkt);

        byte[] encoding = Codec.encode(pkt);
        print("enc=" + hex(encoding));

        PacketReader r = getPacketReader(ICMP_REQ);
        byte[] expected = r.readBytes(ICMP_REQ_REPLY_LEN);
        print("exp=" + hex(expected));

        Assert.assertArrayEquals(expected, encoding);
    }

    @Test
    public void decodeModifyEncode() {
        print(EOL + "decodeModifyEncode()");

        Packet pkt = decodeIcmpReqFile();
        print("Original " + pkt.toDebugString());

        Ethernet eth2 = new Ethernet.Builder((Ethernet)pkt.get(ETHERNET))
            .dstAddr(SRC_MAC)
            .srcAddr(DST_MAC)
            .build();

        Ip ip2 = new Ip.Builder((Ip)pkt.get(IP))
            .srcAddr(DST_IP)
            .dstAddr(SRC_IP)
            .ident(IP_REPLY_IDENT)
            .ttl(REPLY_TTL)
            .build();

        Icmp icmp2 = new Icmp.Builder((Icmp)pkt.get(ICMP))
            .typeCode(IcmpTypeCode.ECHO_REPLY)
            .build();

        Packet pkt2 = new Packet(eth2, ip2, icmp2);
        print("Modified " + pkt2.toDebugString());

        byte[] encoding2 = Codec.encode(pkt2);
        print("enc=" + hex(encoding2));

        PacketReader r2 = getPacketReader(ICMP_REPLY);
        byte[] expected2 = r2.readBytes(ICMP_REQ_REPLY_LEN);
        print("exp=" + hex(expected2));

        assertArrayEquals(expected2, encoding2);
    }

    @Test
    public void decodeEncodeOpaquePayload() {
        print(EOL + "decodeEncodeByteOnlyPayload()");

        PacketReader r = getPacketReader(ICMP_DST_UNR);
        Packet pkt = Codec.decodeEthernet(r);
        print(pkt.toDebugString());

        Icmp icmp = pkt.get(ICMP);
        assertEquals(IcmpTypeCode.DST_UNREACH_HOST, icmp.typeCode());
        assertEquals(ICMP_CHECKSUM_2, icmp.checkSum());
        assertArrayEquals(DST_UNR_PAYLOAD, icmp.bytes());

        byte[] encoding = Codec.encode(pkt);
        print("enc=" + hex(encoding));

        PacketReader r2 = getPacketReader(ICMP_DST_UNR);
        byte[] expected = r2.readBytes(ICMP_DST_UNR_LEN);
        print("exp=" + hex(expected));

        assertArrayEquals(expected, encoding);
    }

}
