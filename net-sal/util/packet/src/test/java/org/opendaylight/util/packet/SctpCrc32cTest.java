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
import org.opendaylight.util.packet.Sctp.Chunk;
import org.opendaylight.util.packet.Sctp.Chunk.Type;
import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;
import static org.opendaylight.util.net.EthernetType.IPv4;
import static org.opendaylight.util.packet.ProtocolId.*;
import static org.opendaylight.util.packet.ProtocolUtils.hex;
import static org.junit.Assert.*;


/**
 * SCTP CRC32c packet unit tests.
 *
 * @author Frank Wood
 */
public class SctpCrc32cTest extends PacketTest {

    private static final String ETH2_IP_SCTP = "eth2-ip-sctp-crc32c.hex";
    private static final int ETH2_IP_SCTP_LEN = 62;

    private static final MacAddress DST_MAC = mac("00:0e:2e:24:37:5f");
    private static final MacAddress SRC_MAC = mac("00:04:96:08:e0:40");

    private static final IpAddress SRC_IP = ip("203.255.252.194");
    private static final IpAddress DST_IP = ip("155.230.24.155");

    private static final int IP_IDENT = 0;
    private static final int TTL = 59;
    private static final int FRAG_OFFSET = 0;
    private static final int IP_CHECK_SUM = 0xc206;

    private static final int SRC_PORT = 80;
    private static final int DST_PORT = 32836;

    private static final long VERIFY_TAG = 0x3bb99c46L;
    private static final long SCTP_CHECK_SUM = 0xdafdf97bL;

    private static final byte[] CHUNK_DATA = new byte[] {
        0x2b, 0x2d, 0x7e, (byte)0xb2, 0x00, 0x01, (byte)0x9e, 0x6d,
        0x00, 0x00, 0x00, 0x00
    };

    private Packet decodeEth2IpSctpFile() {
        return Codec.decodeEthernet(getPacketReader(ETH2_IP_SCTP));
    }

    private void verifyEth2IpSctp(Packet pkt) {
        assertEquals(3, pkt.size());
        assertEquals(SCTP, pkt.innermostId());

        Ethernet eth = pkt.get(ETHERNET);
        assertEquals(DST_MAC, eth.dstAddr());
        assertEquals(SRC_MAC, eth.srcAddr());
        assertEquals(IPv4, eth.type());

        Ip ip = pkt.get(IP);
        assertEquals(IpTosDsfc.CS0, ip.tosDsfc());
        assertEquals(IpTosEcn.NOT_ECT, ip.tosEcn());
        assertEquals(IP_IDENT, ip.ident());

        assertTrue(ip.doNotFrag());
        assertFalse(ip.moreFragToCome());
        assertEquals(FRAG_OFFSET, ip.fragOffset());

        assertEquals(TTL, ip.ttl());
        assertEquals(IpType.SCTP, ip.type());
        assertEquals(IP_CHECK_SUM, ip.checkSum());
        assertEquals(SRC_IP, ip.srcAddr());
        assertEquals(DST_IP, ip.dstAddr());

        Sctp sctp = pkt.get(SCTP);
        assertEquals(SRC_PORT, sctp.srcPort());
        assertEquals(DST_PORT, sctp.dstPort());
        assertEquals(VERIFY_TAG, sctp.verifyTag());
        assertEquals(SCTP_CHECK_SUM, sctp.checkSum());

        Chunk[] chunks = sctp.chunks();
        assertEquals(1, chunks.length);

        assertEquals(Type.SACK, chunks[0].type());
        assertEquals(0, chunks[0].flags());
        assertArrayEquals(CHUNK_DATA, chunks[0].data());
    }

    private Packet createEth2IpSctp() {
        Ethernet eth = new Ethernet.Builder()
            .dstAddr(DST_MAC)
            .srcAddr(SRC_MAC)
            .type(IPv4)
            .build();

        Ip ip = new Ip.Builder()
            .ident(IP_IDENT)
            .doNotFrag(true)
            .ttl(TTL)
            .type(IpType.SCTP)
            .srcAddr(SRC_IP)
            .dstAddr(DST_IP)
            .build();

        Sctp sctp = new Sctp.Builder()
            .srcPort(SRC_PORT)
            .dstPort(DST_PORT)
            .verifyTag(VERIFY_TAG)
            .chunks(new Chunk[] { new Chunk(Type.SACK, 0, CHUNK_DATA) })
            .build();

        return new Packet(eth, ip, sctp);
    }

    @Test
    public void decode() {
        print(EOL + "decode()");
        Packet pkt = decodeEth2IpSctpFile();
        print(pkt.toDebugString());
        verifyEth2IpSctp(pkt);
    }

    @Test
    public void encode() {
        print(EOL + "encode()");
        Packet pkt = createEth2IpSctp();
        print(pkt.toDebugString());

        byte[] encoding = Codec.encode(pkt);
        print("enc=" + hex(encoding));

        PacketReader r = getPacketReader(ETH2_IP_SCTP);
        byte[] expected = r.readBytes(ETH2_IP_SCTP_LEN);
        print("exp=" + hex(expected));

        assertArrayEquals(expected, encoding);
    }

}
