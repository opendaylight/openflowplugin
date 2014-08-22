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
import org.opendaylight.util.packet.LldpTlv.Capability;
import org.opendaylight.util.packet.LldpTlv.PortIdSubType;
import org.opendaylight.util.packet.LldpTlv.Type;
import org.junit.Test;

import java.util.Map;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;
import static org.opendaylight.util.packet.LldpTlv.Capability.BRIDGE;
import static org.opendaylight.util.packet.LldpTlv.Capability.ROUTER;
import static org.opendaylight.util.packet.ProtocolId.ETHERNET;
import static org.opendaylight.util.packet.ProtocolId.LLDP;
import static org.opendaylight.util.packet.ProtocolUtils.hex;
import static org.junit.Assert.*;


/**
 * LLDP packet unit tests.
 *
 * @author Frank Wood
 */
public class LldpTest extends PacketTest {

    private static final String ETH2_LLDP = "eth2-lldp.hex";
    private static final int ETH2_LLDP_LEN = 261;

    private static final MacAddress DST_MAC = mac("01:80:c2:00:00:0e");
    private static final MacAddress SRC_MAC = mac("08:2e:5f:69:c4:7b");
    private static final MacAddress CID_MAC = mac("08:2e:5f:69:c4:40");
    private static final String PORT = "5";
    private static final int TTL = 120;
    private static final String PORT_DESC = "5";
    private static final String SYS_NAME = "HP Stack E3800";
    private static final String SYS_DESC =
            "HP J9587A 3800-24G-PoE+-2XG Switch, " +
            "revision KA.15.10.0004, ROM KA.15.09 (/ws/swbuildm/rel_irvine_" +
            "qaoff/code/build/tam(swbuildm_rel_irvine_qaoff_rel_irvine))";

    private static final byte[] MGMT_ADDR = new byte[] {
        0x07, 0x06, 0x08, 0x2e, 0x5f, 0x69, (byte)0xc4, 0x40,
        0x02, 0x00, 0x00, 0x00, 0x00, 0x00
    };

    private static final byte[] PRIV_0 = new byte[] {
        0x00, (byte)0x80, (byte)0xc2, 0x01, 0x00, 0x0a
    };

    private static final byte[] PRIV_1 = new byte[] {
        0x00, 0x12, 0x0f, 0x01, 0x03, 0x6c, 0x01, 0x00, 0x1e
    };

    private static final byte[] PRIV_2 = new byte[] {
        0x00, 0x12, 0x0f, 0x02, 0x07, 0x01, 0x01
    };

    private Packet decodeEth2LldpFile() {
        return Codec.decodeEthernet(getPacketReader(ETH2_LLDP));
    }

    private void verifyEthLldp(Packet pkt) {
        assertEquals(2, pkt.size());
        assertEquals(LLDP, pkt.innermostId());

        Ethernet eth = pkt.get(ETHERNET);
        assertEquals(DST_MAC, eth.dstAddr());
        assertEquals(SRC_MAC, eth.srcAddr());
        assertEquals(EthernetType.LLDP, eth.type());

        Lldp lldp = pkt.get(LLDP);
        assertEquals(CID_MAC, lldp.chassisId().macAddr());
        assertEquals(PORT, lldp.portId().name());
        assertEquals(TTL, lldp.ttl().number().intValue());

        Map<LldpTlv.Type, LldpTlv> options = lldp.options();
        assertEquals(5, options.size());

        assertEquals(PORT_DESC, options.get(Type.PORT_DESC).name());
        assertEquals(SYS_NAME, options.get(Type.SYS_NAME).name());
        assertEquals(SYS_DESC, options.get(Type.SYS_DESC).name());

        LldpTlv tlv = options.get(Type.CAPS);
        assertEquals(2, tlv.supported().length);
        assertTrue(Capability.has(tlv.supported(), BRIDGE, ROUTER));

        assertEquals(1, tlv.enabled().length);
        assertTrue(Capability.has(tlv.enabled(), BRIDGE));

        assertArrayEquals(MGMT_ADDR, options.get(Type.MGMT_ADDR).bytes());

        LldpTlv[] privOpts = lldp.privateOptions();
        assertEquals(3, privOpts.length);

        assertArrayEquals(PRIV_0, privOpts[0].bytes());
        assertArrayEquals(PRIV_1, privOpts[1].bytes());
        assertArrayEquals(PRIV_2, privOpts[2].bytes());
    }

    protected Packet createEth2Lldp() {
        Ethernet eth = new Ethernet.Builder()
            .dstAddr(DST_MAC)
            .srcAddr(SRC_MAC)
            .type(EthernetType.LLDP)
            .build();

        Lldp lldp = new Lldp.Builder()
            .chassisId(LldpTlv.chassisIdMacAddr(CID_MAC))
            .portId(LldpTlv.portIdName(PortIdSubType.LOCAL, PORT))
            .ttl(LldpTlv.ttl(TTL))
            .options(new LldpTlv[] {
                new LldpTlv.Builder(Type.PORT_DESC).name(PORT_DESC).build(),
                new LldpTlv.Builder(Type.SYS_NAME).name(SYS_NAME).build(),
                new LldpTlv.Builder(Type.SYS_DESC).name(SYS_DESC).build(),
                new LldpTlv.Builder(Type.CAPS)
                    .supported(BRIDGE, ROUTER).enabled(BRIDGE).build(),
                new LldpTlv.Builder(Type.MGMT_ADDR).bytes(MGMT_ADDR).build()
            })
            .privateOptions(new LldpTlv[] {
                new LldpTlv.PrivateBuilder().bytes(PRIV_0).build(),
                new LldpTlv.PrivateBuilder().bytes(PRIV_1).build(),
                new LldpTlv.PrivateBuilder().bytes(PRIV_2).build(),
            })
            .build();

        return new Packet(eth, lldp);
    }

    @Test
    public void decodeEth2Lldp() {
        print(EOL + "decodeEth2Lldp()");
        Packet pkt = decodeEth2LldpFile();
        print(pkt.toDebugString());
        verifyEthLldp(pkt);
    }

    @Test
    public void encodeEth2Lldp() {
        print(EOL + "encode()");
        Packet pkt = createEth2Lldp();
        print(pkt.toDebugString());
        verifyEthLldp(pkt);

        byte[] encoding = Codec.encode(pkt);
        print("enc=" + hex(encoding));

        PacketReader r = getPacketReader(ETH2_LLDP);
        byte[] expected = r.readBytes(ETH2_LLDP_LEN);
        print("exp=" + hex(expected));

        assertArrayEquals(expected, encoding);
    }

}
