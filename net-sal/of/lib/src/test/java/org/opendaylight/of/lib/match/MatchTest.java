/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.junit.Test;
import org.opendaylight.of.lib.OfPacketWriter;
import org.opendaylight.util.ByteUtils;
import org.opendaylight.util.net.EthernetType;
import org.opendaylight.util.net.IpProtocol;
import org.opendaylight.util.net.MacAddress;

import static org.opendaylight.of.lib.ProtocolVersion.V_1_0;
import static org.opendaylight.of.lib.ProtocolVersion.V_1_3;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.MatchFactory.createMatch;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for {@link Match}.
 *
 * @author Simon Hunt
 */
public class MatchTest extends AbstractMatchTest {

    private static final MacAddress MAC_1 = mac("aabbcc112233");
    private static final MacAddress MAC_2 = mac("ddeeff112233");

    private static final MatchField MF_ETH_SRC =
            createBasicField(V_1_3, ETH_SRC, MAC_1);
    private static final MatchField MF_ETH_DST =
            createBasicField(V_1_3, ETH_DST, MAC_2);
    private static final MatchField MF_ETH_TYPE =
            createBasicField(V_1_3, ETH_TYPE, EthernetType.IPv4);
    private static final MatchField MF_IP_PROTO =
            createBasicField(V_1_3, IP_PROTO, IpProtocol.TCP);
    private static final MatchField MF_TCP_DST =
            createBasicField(V_1_3, TCP_DST, pn(25));

    @Test
    public void basic() {
        print(EOL + "basic");
        MutableMatch mm = createMatch(V_1_3)
                .addField(MF_ETH_SRC).addField(MF_ETH_TYPE)
                .addField(MF_IP_PROTO).addField(MF_TCP_DST);
        Match m1 = (Match) mm.toImmutable();

        mm = createMatch(V_1_3)
                .addField(MF_ETH_SRC).addField(MF_ETH_TYPE)
                .addField(MF_IP_PROTO).addField(MF_TCP_DST);
        Match m2 = (Match) mm.toImmutable();

        verifyNotSameButEqual(m1, m2);

        // now try with different field
        mm = createMatch(V_1_3)
                .addField(MF_ETH_DST).addField(MF_ETH_TYPE)
                .addField(MF_IP_PROTO).addField(MF_TCP_DST);
        m2 = (Match) mm.toImmutable();

        verifyNotEqual(m1, m2);

        // try with one field fewer
        mm = createMatch(V_1_3)
                .addField(MF_ETH_SRC).addField(MF_ETH_TYPE)
                .addField(MF_IP_PROTO);
        m2 = (Match) mm.toImmutable();

        verifyNotEqual(m1, m2);
    }

    @Test
    public void createMatchWithUdpFields() {
        print(EOL + "createMatchWithUdpFields()");
        MutableMatch mm = createMatch(V_1_3)
                .addField(createBasicField(V_1_3, ETH_TYPE, EthernetType.IPv4))
                .addField(createBasicField(V_1_3, IP_PROTO, IpProtocol.UDP))
                .addField(createBasicField(V_1_3, UDP_SRC, pn(126)))
                .addField(createBasicField(V_1_3, UDP_DST, pn(127)));
        Match m = (Match) mm.toImmutable();
        OfPacketWriter pkt = new OfPacketWriter(m.getTotalLength());
        MatchFactory.encodeMatch(m, pkt);
        print(ByteUtils.toHexString(pkt.array()));

        mm = createMatch(V_1_0)
                .addField(createBasicField(V_1_0, ETH_TYPE, EthernetType.IPv4))
                .addField(createBasicField(V_1_0, IP_PROTO, IpProtocol.UDP))
                .addField(createBasicField(V_1_0, UDP_SRC, pn(126)))
                .addField(createBasicField(V_1_0, UDP_DST, pn(127)));
        m = (Match) mm.toImmutable();
        pkt = new OfPacketWriter(m.getTotalLength());
        MatchFactory.encodeMatch(m, pkt);
        print(ByteUtils.toHexString(pkt.array()));
    }
    
    @Test
    public void create10IpsWithNoMask() {
        print(EOL + "create10IpsWithNoMask()");
        MutableMatch mm = createMatch(V_1_0)
                .addField(createBasicField(V_1_0, ETH_TYPE, EthernetType.IPv4))
                .addField(createBasicField(V_1_0, IPV4_SRC, ip("10.0.0.1")))
                .addField(createBasicField(V_1_0, IPV4_DST, ip("10.0.0.22")));
        Match m = (Match) mm.toImmutable();
        OfPacketWriter pkt = new OfPacketWriter(m.getTotalLength());        
        MatchFactory.encodeMatch(m, pkt);
        print(ByteUtils.toHexString(pkt.array()));
    }
}
