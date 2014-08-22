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
import org.opendaylight.util.packet.IpV6.Option;
import org.junit.Assert;
import org.junit.Test;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;
import static org.opendaylight.util.net.EthernetType.IPv6;
import static org.opendaylight.util.packet.ProtocolId.*;
import static org.opendaylight.util.packet.ProtocolUtils.hex;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


/**
 * IPv6 packet unit tests.
 *
 * @author Frank Wood
 */
public class IpV6Test extends PacketTest {

    private static final String IPV6_HOP = "eth2-ipv6-hop-icmpv6.hex";
    private static final int IPV6_HOP_LEN = 90;

    private static final MacAddress DST_MAC = mac("33:33:00:00:00:16");
    private static final MacAddress SRC_MAC = mac("00:d0:09:e3:e8:de");

    private static final IpAddress DST_IP = ip("ff02::0016");
    private static final IpAddress SRC_IP = ip("fe80::02d0:09ff:fee3:e8de");

    private static final int FLOW_LABEL = 0x00;
    private static final int UPPER_LAYER_LEN = 28;
    private static final int HOP_LIMIT = 1;

    private static final byte[] OPTION_0 = new byte[] {
        0x05, 0x02, 0x00, 0x00, 0x01, 0x00
    };

    private static final byte[] ICMP_PAYLOAD = new byte[] {
        0x00, 0x00, 0x00, 0x01, 0x04, 0x00, 0x00, 0x00,
        (byte)0xff, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x01, (byte)0xff, (byte)0x98, 0x06, (byte)0xe1
    };

    private static final int CHECK_SUM = 0x074fe;

    private Packet decodeIpFile() {
        return Codec.decodeEthernet(getPacketReader(IPV6_HOP));
    }

    private void verifyIp(Packet pkt) {
        Ethernet eth = pkt.get(ETHERNET);
        assertEquals(DST_MAC, eth.dstAddr());
        assertEquals(SRC_MAC, eth.srcAddr());
        assertEquals(IPv6, eth.type());

        IpV6 ip = pkt.get(IPV6);
        assertEquals(IpTosDsfc.CS0, ip.tosDsfc());
        assertEquals(IpTosEcn.NOT_ECT, ip.tosEcn());
        assertEquals(UPPER_LAYER_LEN, ip.nextProtocolLen());
        assertEquals(FLOW_LABEL, ip.flowLabel());
        assertEquals(IpType.IPV6_ICMP, ip.nextProtocol());
        assertEquals(HOP_LIMIT, ip.hopLimit());
        assertEquals(SRC_IP, ip.srcAddr());
        assertEquals(DST_IP, ip.dstAddr());

        IpV6.Option[] opts = ip.options();
        assertEquals(1, opts.length);
        assertEquals(IpType.IPV6_ICMP, opts[0].nextHdr());
        assertArrayEquals(OPTION_0, opts[0].bytes());

        IcmpV6 icmp = pkt.get(ICMPV6);
        assertEquals(IcmpTypeCodeV6.MULTICAST_LISTENER_DISCO_REPORTS,
                     icmp.typeCode());
        assertEquals(CHECK_SUM, icmp.checkSum());
        assertArrayEquals(ICMP_PAYLOAD, icmp.bytes());
    }

    protected Packet createIcmpReq() {
        Ethernet eth = new Ethernet.Builder()
            .dstAddr(DST_MAC)
            .srcAddr(SRC_MAC)
            .type(IPv6)
            .build();

        IpV6 ip = new IpV6.Builder()
            .nextHdr(IpType.IPV6_HOPOPT)
            .hopLimit(HOP_LIMIT)
            .srcAddr(SRC_IP)
            .dstAddr(DST_IP)
            .options(new Option[] {
                new Option(IpType.IPV6_HOPOPT, IpType.IPV6_ICMP, OPTION_0)
            })
            .build();

        IcmpV6 icmp = new IcmpV6.Builder()
            .typeCode(IcmpTypeCodeV6.MULTICAST_LISTENER_DISCO_REPORTS)
            .bytes(ICMP_PAYLOAD)
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

        PacketReader r = getPacketReader(IPV6_HOP);
        byte[] expected = r.readBytes(IPV6_HOP_LEN);
        print("exp=" + hex(expected));

        Assert.assertArrayEquals(expected, encoding);
    }

}
