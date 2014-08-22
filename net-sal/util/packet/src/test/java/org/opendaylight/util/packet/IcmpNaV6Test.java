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
import org.opendaylight.util.packet.IcmpV6.NeighborAdvertiseData;
import org.junit.Assert;
import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;
import static org.opendaylight.util.net.EthernetType.IPv6;
import static org.opendaylight.util.packet.IcmpTypeCodeV6.NEIGHBOR_ADVERTISE_NDP;
import static org.opendaylight.util.packet.ProtocolId.ICMPV6;
import static org.opendaylight.util.packet.ProtocolId.IPV6;
import static org.opendaylight.util.packet.ProtocolUtils.hex;
import static org.junit.Assert.*;


/**
 * ICMPv6 Neighbor Advertisement packet unit tests.
 *
 * @author Frank Wood
 */
public class IcmpNaV6Test extends PacketTest {

    private static final String IPV6_ICMPV6 = "eth2-ipv6-icmpv6-na.hex";
    private static final int IPV6_ICMPV6_LEN = 78;

    private static final MacAddress DST_MAC = mac("00:00:86:05:80:da");
    private static final MacAddress SRC_MAC = mac("00:60:97:07:69:ea");

    private static final IpAddress SRC_IP = ip("fe80::260:97ff:fe07:69ea");
    private static final IpAddress DST_IP = ip("fe80::200:86ff:fe05:80da");

    private static final IpAddress NS_TGT_IP = ip("fe80::260:97ff:fe07:69ea");

    private static final int FLOW_LABEL = 0x00;
    private static final int UPPER_LAYER_LEN = 24;
    private static final int HOP_LIMIT = 255;

    private static final int CHECK_SUM = 0xafa5;

    private Packet decodeIpFile() {
        return Codec.decodeEthernet(getPacketReader(IPV6_ICMPV6));
    }

    private void verifyIp(Packet pkt) {
        assertEquals(3, pkt.size());
        assertEquals(ICMPV6, pkt.innermostId());

        IpV6 ip = pkt.get(IPV6);
        assertEquals(UPPER_LAYER_LEN, ip.nextProtocolLen());
        assertEquals(FLOW_LABEL, ip.flowLabel());
        assertEquals(IpType.IPV6_ICMP, ip.nextProtocol());
        assertEquals(HOP_LIMIT, ip.hopLimit());
        assertEquals(SRC_IP, ip.srcAddr());
        assertEquals(DST_IP, ip.dstAddr());

        assertEquals(0, ip.options().length);

        IcmpV6 icmp = pkt.get(ICMPV6);
        assertEquals(NEIGHBOR_ADVERTISE_NDP, icmp.typeCode());
        assertEquals(CHECK_SUM, icmp.checkSum());
        assertEquals(NS_TGT_IP, icmp.neighborAdvertiseData().targetAddr());
        assertTrue(icmp.neighborAdvertiseData().isSenderRouter());
        assertTrue(icmp.neighborAdvertiseData().isSolicitResponse());
        assertFalse(icmp.neighborAdvertiseData().override());

        IcmpOptionV6[] options = icmp.options();
        assertEquals(0, options.length);
    }

    private Packet createIcmpReq() {
        Ethernet eth = new Ethernet.Builder()
            .dstAddr(DST_MAC)
            .srcAddr(SRC_MAC)
            .type(IPv6)
            .build();

        IpV6 ip = new IpV6.Builder()
            .nextHdr(IpType.IPV6_ICMP)
            .hopLimit(HOP_LIMIT)
            .srcAddr(SRC_IP)
            .dstAddr(DST_IP)
            .build();

        IcmpV6 icmp = new IcmpV6.Builder()
            .neighborAdvertiseData(
                new NeighborAdvertiseData(true, true, false, NS_TGT_IP))
            .build();

        return new Packet(eth, ip, icmp);
    }

    @Test
    public void decode() {
        print(EOL + "decode()");
        Packet pkt = decodeIpFile();
        print(pkt.toDebugString());
        verifyIp(pkt);
    }

    @Test
    public void encode() {
        print(EOL + "encode()");
        Packet pkt = createIcmpReq();
        print(pkt.toDebugString());

        byte[] encoding = Codec.encode(pkt);
        print("enc=" + hex(encoding));

        PacketReader r = getPacketReader(IPV6_ICMPV6);
        byte[] expected = r.readBytes(IPV6_ICMPV6_LEN);
        print("exp=" + hex(expected));

        Assert.assertArrayEquals(expected, encoding);
    }

}
