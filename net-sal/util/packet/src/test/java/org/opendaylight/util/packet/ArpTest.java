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
import static org.opendaylight.util.packet.Arp.OpCode.REPLY;
import static org.opendaylight.util.packet.Arp.OpCode.REQ;
import static org.opendaylight.util.packet.ProtocolId.ARP;
import static org.opendaylight.util.packet.ProtocolId.ETHERNET;
import static org.opendaylight.util.packet.ProtocolUtils.hex;
import static org.junit.Assert.*;


/**
 * ARP packet unit tests.
 *
 * @author Frank Wood
 */
public class ArpTest extends PacketTest {

    private static final String ETH2_ARP_REQ = "eth2-arp-req.hex";
    private static final int ETH2_ARP_REQ_LEN = 60;

    private static final String ETH2_ARP_REPLY = "eth2-arp-reply.hex";
    private static final int ETH2_ARP_REPLY_LEN = 60;

    private static final MacAddress DST_MAC = mac("08:86:3b:33:87:e8");
    private static final MacAddress SRC_MAC = mac("44:1e:a1:ce:5c:e2");

    private static final MacAddress TGT_MAC = mac("08:86:3b:33:87:e8");
    private static final MacAddress SND_MAC = mac("44:1e:a1:ce:5c:e2");

    private static final IpAddress TGT_IP = ip("192.168.1.1");
    private static final IpAddress SND_IP = ip("192.168.1.9");

    private Packet decodeEth2ArpReqFile() {
        return Codec.decodeEthernet(getPacketReader(ETH2_ARP_REQ));
    }

    private void verifyEth2ArpReq(Packet pkt) {
        assertEquals(2, pkt.size());
        assertEquals(ARP, pkt.innermostId());

        Ethernet eth = pkt.get(ETHERNET);
        assertEquals(DST_MAC, eth.dstAddr());
        assertEquals(SRC_MAC, eth.srcAddr());
        assertEquals(EthernetType.ARP, eth.type());

        Arp arp = pkt.get(ARP);
        assertEquals(HardwareType.ETHERNET, arp.hwType());
        assertEquals(REQ, arp.opCode());
        assertEquals(SND_MAC, arp.senderMacAddr());
        assertEquals(SND_IP, arp.senderIpAddr());
        assertEquals(TGT_MAC, arp.targetMacAddr());
        assertEquals(TGT_IP, arp.targetIpAddr());
    }

    private Packet createEth2ArpReq() {
        Ethernet eth = new Ethernet.Builder()
            .dstAddr(DST_MAC)
            .srcAddr(SRC_MAC)
            .type(EthernetType.ARP)
            .build();

        Arp arp = new Arp.Builder()
            .opCode(REQ)
            .senderMacAddr(SND_MAC)
            .senderIpAddr(SND_IP)
            .targetMacAddr(TGT_MAC)
            .targetIpAddr(TGT_IP)
            .build();

        return new Packet(eth, arp);
    }

    @Test
    public void decode() {
        print(EOL + "decode()");
        Packet pkt = decodeEth2ArpReqFile();
        print(pkt);
        verifyEth2ArpReq(pkt);
    }

    @Test
    public void encode() {
        print(EOL + "encode()");
        Packet pkt = createEth2ArpReq();
        print(pkt);
        verifyEth2ArpReq(pkt);

        byte[] encoding = Codec.encode(pkt);
        print("enc=" + hex(encoding));

        PacketReader r = getPacketReader(ETH2_ARP_REQ);
        byte[] expected = r.readBytes(ETH2_ARP_REQ_LEN);
        print("exp=" + hex(expected));

        assertArrayEquals(expected, encoding);
    }

    @Test
    public void decodeModifyEncode() {
        print(EOL + "decodeModifyEncode()");

        Packet pkt = decodeEth2ArpReqFile();
        print("Original " + pkt.toDebugString());

        Ethernet eth2 = new Ethernet.Builder((Ethernet)pkt.get(ETHERNET))
            .dstAddr(SRC_MAC)
            .srcAddr(DST_MAC)
            .build();

        Arp arp2 = new Arp.Builder((Arp)pkt.get(ARP))
            .opCode(REPLY)
            .senderMacAddr(TGT_MAC)
            .senderIpAddr(TGT_IP)
            .targetMacAddr(SND_MAC)
            .targetIpAddr(SND_IP)
            .build();

        Packet pkt2 = new Packet(eth2, arp2);
        print("Modified " + pkt2.toDebugString());

        byte[] encoding2 = Codec.encode(pkt2);
        print("enc=" + hex(encoding2));

        PacketReader r2 = getPacketReader(ETH2_ARP_REPLY);
        byte[] expected2 = r2.readBytes(ETH2_ARP_REPLY_LEN);
        print("exp=" + hex(expected2));

        assertArrayEquals(expected2, encoding2);
    }

    @Test
    public void encodeBadIp() {
        print(EOL + "encodeBadIp()");

        try {
            Arp arp = new Arp.Builder()
                .opCode(REQ)
                .senderMacAddr(SND_MAC)
                .senderIpAddr(SND_IP)
                .targetMacAddr(TGT_MAC)
                .targetIpAddr(IpAddress.LOOPBACK_IPv6)
                .build();
            fail("Exception should have been thrown");
        } catch (ProtocolException e) {
            print(e);
            assertNotNull(e.protocol());
            print("Failed creating " + e.protocol().toDebugString());
        }
    }

}
