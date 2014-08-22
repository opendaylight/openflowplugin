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
import org.opendaylight.util.packet.IcmpV6.RouterAdvertiseData;
import org.junit.Assert;
import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;
import static org.opendaylight.util.net.EthernetType.IPv6;
import static org.opendaylight.util.packet.IcmpTypeCodeV6.ROUTER_ADVERTISE_NDP;
import static org.opendaylight.util.packet.ProtocolId.ICMPV6;
import static org.opendaylight.util.packet.ProtocolId.IPV6;
import static org.opendaylight.util.packet.ProtocolUtils.hex;
import static org.junit.Assert.*;


/**
 * ICMPv6 Router Advertisement #2 packet unit tests.
 *
 * @author Frank Wood
 */
public class IcmpRa2V6Test extends PacketTest {

    private static final String IPV6_ICMPV6 = "eth2-ipv6-icmpv6-ra2.hex";
    private static final int IPV6_ICMPV6_LEN = 118;

    private static final MacAddress DST_MAC = mac("33:33:00:00:00:01");
    private static final MacAddress SRC_MAC = mac("00:60:97:07:69:ea");

    private static final IpAddress SRC_IP = ip("fe80::260:97ff:fe07:69ea");
    private static final IpAddress DST_IP = ip("ff02::1");

    private static final MacAddress SRC_LL_MAC = mac("00:60:97:07:69:ea");

    private static final int FLOW_LABEL = 0x00;
    private static final int UPPER_LAYER_LEN = 64;
    private static final int HOP_LIMIT = 255;

    private static final int ICMP_HOP_LIMIT = 64;
    private static final int ROUTER_LIFETIME = 1800;
    private static final int REACH_TIMER = 30000;
    private static final int RETRANS_TIMER = 1000;

    private static final int CHECK_SUM = 0x4625;

    private static final int MTU = 1500;

    private static final byte[] PREFIX_BYTES = new byte[] {
        0x40, (byte)0xc0, 0x00, 0x36, (byte)0xee, (byte)0x80, 0x00, 0x36,
        (byte)0xee, (byte)0x80, 0x00, 0x00, 0x00, 0x00, 0x3f, (byte)0xfe,
        0x05, 0x07, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
    };

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
        assertEquals(ROUTER_ADVERTISE_NDP, icmp.typeCode());
        assertEquals(CHECK_SUM, icmp.checkSum());

        RouterAdvertiseData ra = icmp.routerAdvertiseData();
        assertEquals(ICMP_HOP_LIMIT, ra.hopLimit());
        assertFalse(ra.managedAddrConfig());
        assertFalse(ra.otherConfig());
        assertEquals(ROUTER_LIFETIME, ra.routerLifetime());
        assertEquals(REACH_TIMER, ra.reachableTime());
        assertEquals(RETRANS_TIMER, ra.retransTimer());

        IcmpOptionV6[] options = icmp.options();
        assertEquals(3, options.length);

        assertEquals(IcmpOptionV6.Type.SRC_LL_ADDR, options[0].type());
        assertEquals(SRC_LL_MAC, options[0].linkLayerAddr());

        assertEquals(IcmpOptionV6.Type.MTU, options[1].type());
        assertEquals(MTU, options[1].mtu());

        assertEquals(IcmpOptionV6.Type.PREFIX_INFO, options[2].type());
        assertArrayEquals(PREFIX_BYTES, options[2].bytes());
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
            .routerAdvertiseData(
                new RouterAdvertiseData(ICMP_HOP_LIMIT, false, false,
                                        ROUTER_LIFETIME,
                                        REACH_TIMER,
                                        RETRANS_TIMER))
            .options(new IcmpOptionV6[] {
                new IcmpOptionV6(IcmpOptionV6.Type.SRC_LL_ADDR, SRC_LL_MAC),
                new IcmpOptionV6(IcmpOptionV6.Type.MTU, MTU),
                new IcmpOptionV6(IcmpOptionV6.Type.PREFIX_INFO, PREFIX_BYTES),
            })
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
