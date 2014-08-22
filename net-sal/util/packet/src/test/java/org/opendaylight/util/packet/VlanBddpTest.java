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
import org.opendaylight.util.packet.LldpTlv.PortIdSubType;
import org.opendaylight.util.packet.LldpTlv.Type;
import org.junit.Test;

import java.util.Map;

import static org.opendaylight.util.junit.TestTools.EOL;
import static org.opendaylight.util.junit.TestTools.print;
import static org.opendaylight.util.packet.ProtocolId.BDDP;
import static org.opendaylight.util.packet.ProtocolId.ETHERNET;
import static org.opendaylight.util.packet.ProtocolUtils.hex;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;


/**
 * VLAN ID - BDDP packet unit tests.
 *
 * @author Frank Wood
 */
public class VlanBddpTest extends PacketTest {

    private static final String BDDP_DATA = "eth2-vlan-bddp.hex";
    private static final int BDDP_DATA_LEN = 74;

    private static final MacAddress DST_MAC = mac("ff:ff:ff:ff:ff:ff");
    private static final MacAddress SRC_MAC = mac("d0:7e:28:bc:34:b5");
    private static final MacAddress CID_MAC = mac("d0:7e:28:bc:34:b5");
    private static final int VLAN_ID = 200;
    private static final String PORT = "0x14";
    private static final int TTL = 120;
    private static final String SYS_DESC ="114960351888895685";

    private static final byte[] PRIV_0 = new byte[] {
        0x00, 0x26, (byte)0xe1, 0x00, 0x00, 0x32, (byte)0xd0, (byte)0x7e,
        0x28, (byte)0xbc, 0x34, (byte)0xb5
    };

    private Packet decodeBddpFile() {
        return Codec.decodeEthernet(getPacketReader(BDDP_DATA));
    }

    private void verifyBddp(Packet pkt) {
        assertEquals(2, pkt.size());
        assertEquals(BDDP, pkt.innermostId());

        Ethernet eth = pkt.get(ETHERNET);
        assertEquals(DST_MAC, eth.dstAddr());
        assertEquals(SRC_MAC, eth.srcAddr());
        assertEquals(VLAN_ID, eth.vlanId());
        assertEquals(EthernetType.BDDP, eth.type());

        Bddp bddp = pkt.get(BDDP);
        assertEquals(CID_MAC, bddp.chassisId().macAddr());
        assertEquals(PORT, bddp.portId().name());
        assertEquals(TTL, bddp.ttl().number().intValue());

        Map<LldpTlv.Type, LldpTlv> options = bddp.options();
        assertEquals(1, options.size());

        assertEquals(SYS_DESC, options.get(Type.SYS_DESC).name());

        LldpTlv[] privOpts = bddp.privateOptions();
        assertEquals(1, privOpts.length);

        assertArrayEquals(PRIV_0, privOpts[0].bytes());
    }

    protected Packet createBddp() {
        Ethernet eth = new Ethernet.Builder()
            .dstAddr(DST_MAC)
            .srcAddr(SRC_MAC)
            .vlanId(VLAN_ID)
            .type(EthernetType.BDDP)
            .build();

        Lldp lldp = new Lldp.Builder()
            .chassisId(LldpTlv.chassisIdMacAddr(CID_MAC))
            .portId(LldpTlv.portIdName(PortIdSubType.LOCAL, PORT))
            .ttl(LldpTlv.ttl(TTL))
            .options(new LldpTlv[] {
                new LldpTlv.Builder(Type.SYS_DESC).name(SYS_DESC).build(),
            })
            .privateOptions(new LldpTlv[] {
                new LldpTlv.PrivateBuilder().bytes(PRIV_0).build(),
            })
            .build();

        return new Packet(eth, new Bddp(lldp));
    }

    @Test
    public void decode() {
        print(EOL + "decode()");
        Packet pkt = decodeBddpFile();
        print(pkt.toDebugString());
        verifyBddp(pkt);
    }

    @Test
    public void encode() {
        print(EOL + "encode()");
        Packet pkt = createBddp();
        print(pkt.toDebugString());
        verifyBddp(pkt);

        byte[] encoding = Codec.encode(pkt);
        print("enc=" + hex(encoding));

        PacketReader r = getPacketReader(BDDP_DATA);
        byte[] expected = r.readBytes(BDDP_DATA_LEN);
        print("exp=" + hex(expected));

        assertArrayEquals(expected, encoding);
    }

}
