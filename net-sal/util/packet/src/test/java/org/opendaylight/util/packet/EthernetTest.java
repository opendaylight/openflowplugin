/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.packet;

import org.opendaylight.util.net.EthernetType;
import org.opendaylight.util.net.MacAddress;
import org.opendaylight.util.packet.Ethernet.Control;
import org.opendaylight.util.packet.Ethernet.Dsap;
import org.opendaylight.util.packet.Ethernet.Ssap;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;
import static org.opendaylight.util.packet.Ethernet.NONE;
import static org.opendaylight.util.packet.Ethernet.VlanPriority.PRIORITY_1;
import static org.opendaylight.util.packet.Ethernet.VlanPriority.PRIORITY_5;
import static org.opendaylight.util.packet.ProtocolId.ETHERNET;
import static org.opendaylight.util.packet.ProtocolUtils.hex;
import static org.junit.Assert.*;


/**
 * Ethernet packet unit tests.
 *
 * @author Frank Wood
 */
public class EthernetTest extends PacketTest {

    private static final String ETH2_ARP_REQ = "eth2-arp-req.hex";
    private static final int ETH2_HDR_LEN = 14;

    private static final MacAddress DST_MAC1 = mac("08:86:3b:33:87:e8");
    private static final MacAddress SRC_MAC1 = mac("44:1e:a1:ce:5c:e2");

    private static final String ETH8203_LLC_STP = "eth8023-llc-stp.hex";
    private static final int ETH8023_HDR_LEN = 22;

    private static final MacAddress DST_MAC2 = mac("01:00:0c:cc:cc:cd");
    private static final MacAddress SRC_MAC2 = mac("6c:9c:ed:cf:ed:04");

    private static final String ETH2_VLAN = "eth2-vlan.hex";
    private static final int ETH2_VLAN_HDR_LEN = 18;
    private static final int VLAN_ID = 32;

    private static final MacAddress DST_MAC3 = mac("00:40:05:40:ef:24");
    private static final MacAddress SRC_MAC3 = mac("00:60:08:9f:b1:f3");

    private static final String ETH2_VLANPRI = "eth2-vlanpri.hex";
    private static final int ETH2_VLANPRI_LEN = 18;
    private static final int VLAN_ID2 = 2080;
    private static final Ethernet.VlanPriority VLAN_PRI = PRIORITY_5;

    private static final String ETH8023_LLC_UNK = "eth8023-llc-unk.hex";

    private static final MacAddress DST_MAC4 = mac("00:00:00:00:00:00");
    private static final MacAddress SRC_MAC4 = mac("00:01:00:2c:80:00");
    private static final int ETH8023_LLC_UNK_TYPE_LEN = 4;
    private static final int ETH8023_LLC_UNK_BYTES_LEN = 113;


    /*
     * Ethernet 2
     */

    private Packet decodeEth2File() {
        return Codec.decodeEthernet(getPacketReader(ETH2_ARP_REQ), 1);
    }

    private void verifyEth2(Packet pkt) {
        Ethernet eth = pkt.get(ETHERNET);
        assertEquals(DST_MAC1, eth.dstAddr());
        assertEquals(SRC_MAC1, eth.srcAddr());
        assertEquals(0, eth.len());
        assertEquals(EthernetType.ARP, eth.type());
        assertEquals(Dsap.NO_DSAP, eth.dsap());
        assertEquals(Ssap.NO_SSAP, eth.ssap());
        assertEquals(Control.NO_CONTROL, eth.control());
        assertEquals(0, eth.snapId().vendor());
        assertEquals(0, eth.snapId().local());
    }

    private Packet createEth2() {
        return new Packet(new Ethernet.Builder()
            .dstAddr(DST_MAC1)
            .srcAddr(SRC_MAC1)
            .type(EthernetType.ARP)
            .build());
    }

    @Test
    public void decodeEth2() {
        print(EOL + "decodeEth2()");
        Packet pkt = decodeEth2File();
        print(pkt.toDebugString());
        verifyEth2(pkt);
    }

    @Test
    public void encodeEth2() {
        print(EOL + "encodeEth2()");
        Packet pkt = createEth2();
        print(pkt.toDebugString());
        verifyEth2(pkt);

        byte[] encoding = Codec.encode(pkt);
        // only match with the ethernet portion the rest are 0's
        encoding = Arrays.copyOf(encoding, ETH2_HDR_LEN);
        print("enc=" + hex(encoding));

        PacketReader r = getPacketReader(ETH2_ARP_REQ);
        byte[] expected = r.readBytes(ETH2_HDR_LEN);
        print("exp=" + hex(expected));

        Assert.assertArrayEquals(expected, encoding);
    }

    /*
     * 802.3 SNAP
     */

    private Packet decode8023SnapFile() {
        return Codec.decodeEthernet(getPacketReader(ETH8203_LLC_STP), 1);
    }

    private void verify8023Snap(Packet pkt) {
        Ethernet eth = pkt.get(ETHERNET);
        assertEquals(DST_MAC2, eth.dstAddr());
        assertEquals(SRC_MAC2, eth.srcAddr());
        assertEquals(50, eth.len());
        assertTrue(eth.type().isUnknown());
        assertEquals(Dsap.SNAP, eth.dsap());
        assertEquals(Ssap.SNAP, eth.ssap());
        assertEquals(Control.UNNUMBERED, eth.control());
        assertEquals(0x00000c, eth.snapId().vendor());
        assertEquals(0x010b, eth.snapId().local());
    }

    private Packet create8023Snap() {
        Ethernet eth = new Ethernet.Builder()
            .dstAddr(DST_MAC2)
            .srcAddr(SRC_MAC2)
            .type(NONE)
            .dsap(Dsap.SNAP)
            .ssap(Ssap.SNAP)
            .control(Control.UNNUMBERED)
            .snapId(new Ethernet.SnapId(0x00000c, 0x010b))
            .build();

        UnknownProtocol up = new UnknownProtocol.Builder()
            .bytes(new byte[50])
            .build();

        return new Packet(eth, up);
    }

    @Test
    public void decode8023Snap() {
        print(EOL + "decode8023Snap()");
        Packet pkt = decode8023SnapFile();
        print(pkt.toDebugString());
        verify8023Snap(pkt);
    }

    @Test
    public void encode8023Snap() {
        print(EOL + "encode8023Snap()");
        Packet pkt = create8023Snap();
        print(pkt.toDebugString());

        byte[] encoding = Codec.encode(pkt);
        // only match with the ethernet portion the rest are 0's
        encoding = Arrays.copyOf(encoding, ETH8023_HDR_LEN);
        print("enc=" + hex(encoding));

        PacketReader r = getPacketReader(ETH8203_LLC_STP);
        byte[] expected = r.readBytes(ETH8023_HDR_LEN);
        print("exp=" + hex(expected));

        Assert.assertArrayEquals(expected, encoding);
    }

    /*
     * Ethernet 2 with VLAN.
     */

    private Packet decodeEth2VlanFile() {
        return Codec.decodeEthernet(getPacketReader(ETH2_VLAN), 1);
    }

    private void verifyEth2Vlan(Packet pkt) {
        Ethernet eth = pkt.get(ETHERNET);
        assertEquals(DST_MAC3, eth.dstAddr());
        assertEquals(SRC_MAC3, eth.srcAddr());
        assertEquals(0, eth.len());
        assertEquals(VLAN_ID, eth.vlanId());
        assertEquals(false, eth.vlanDei());
        assertEquals(PRIORITY_1, eth.vlanPriority());
        assertEquals(EthernetType.IPv4, eth.type());
    }

    private Packet createEth2Vlan() {
        return new Packet(new Ethernet.Builder()
            .dstAddr(DST_MAC3)
            .srcAddr(SRC_MAC3)
            .vlanId(VLAN_ID)
            .type(EthernetType.IPv4)
            .build());
    }

    @Test
    public void decodeEth2Vlan() {
        print(EOL + "decodeEth2Vlan()");
        Packet pkt = decodeEth2VlanFile();
        print(pkt.toDebugString());
        verifyEth2Vlan(pkt);
    }

    @Test
    public void encodeEth2Vlan() {
        print(EOL + "encodeEth2Vlan()");
        Packet pkt = createEth2Vlan();
        print(pkt.toDebugString());
        verifyEth2Vlan(pkt);

        byte[] encoding = Codec.encode(pkt);
        // only match with the ethernet portion the rest are 0's
        encoding = Arrays.copyOf(encoding, ETH2_VLAN_HDR_LEN);
        print("enc=" + hex(encoding));

        PacketReader r = getPacketReader(ETH2_VLAN);
        byte[] expected = r.readBytes(ETH2_VLAN_HDR_LEN);
        print("exp=" + hex(expected));

        assertArrayEquals(expected, encoding);
    }

    @Test
    public void decodeEncodeEth2VlanPriBadPayload() {
        print(EOL + "decodeEncodeEth2VlanPriBadPayload()");
        PacketReader r = getPacketReader(ETH2_VLANPRI);

        Packet pkt = Codec.decodeEthernet(r);
        print(pkt.toDebugString());
        Ethernet eth = pkt.get(ETHERNET);
        assertEquals(VLAN_ID2, eth.vlanId());
        assertEquals(VLAN_PRI, eth.vlanPriority());
        assertTrue(eth.vlanDei());

        byte[] encoding = Codec.encode(new Packet(eth));
        // only match with the ethernet portion the rest are 0's
        encoding = Arrays.copyOf(encoding, ETH2_VLANPRI_LEN);
        print("enc=" + hex(encoding));

        r = getPacketReader(ETH2_VLANPRI);
        byte[] expected = r.readBytes(ETH2_VLANPRI_LEN);
        print("exp=" + hex(expected));

        assertArrayEquals(expected, encoding);
    }

    @Test
    public void decode8023Len4UnknownPayload() {
        print(EOL + "decode8023Len4UnknownPayload()");

        PacketReader r = getPacketReader(ETH8023_LLC_UNK);

        Packet pkt = Codec.decodeEthernet(r);
        print(pkt.toDebugString());
        Ethernet eth = pkt.get(ETHERNET);
        assertEquals(DST_MAC4, eth.dstAddr());
        assertEquals(SRC_MAC4, eth.srcAddr());
        assertEquals(ETH8023_LLC_UNK_TYPE_LEN, eth.len());

        UnknownProtocol up = pkt.get(ProtocolId.UNKNOWN);
        assertEquals(ETH8023_LLC_UNK_BYTES_LEN, up.bytes().length);
    }

}
