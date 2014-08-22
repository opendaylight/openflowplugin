/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.junit.Test;
import org.opendaylight.of.lib.AbstractTest;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.of.lib.VersionMismatchException;
import org.opendaylight.util.ValidationException;
import org.opendaylight.util.net.EthernetType;
import org.opendaylight.util.net.IpProtocol;
import org.opendaylight.util.net.PortNumber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.opendaylight.of.lib.ProtocolVersion.*;
import static org.opendaylight.of.lib.match.FieldFactory.createBasicField;
import static org.opendaylight.of.lib.match.MatchFactory.createMatch;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.*;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for MutableMatch.
 *
 * @author Simon Hunt
 */
public class MutableMatchTest extends AbstractTest {

    private void checkVersionChecker(ProtocolVersion pv, OxmBasicFieldType ft,
                                     boolean expAllowed) {
        try {
            // NOTE: assume protocol version of the match and field are the same
            MutableMatch.versionCheck(pv, pv, ft);
            if (!expAllowed)
                fail(AM_NOEX);
            print("{} {} is ok", pv, ft);
        } catch (VersionMismatchException vme) {
            if (expAllowed)
                fail(AM_UNEX);
            else
                print(FMT_EX, vme);
        } catch (Exception e) {
            print(e);
            fail(AM_WREX);
        }
    }

    @Test
    public void versionCheck10() {
        print(EOL + "versionCheck10()");
        checkVersionChecker(V_1_0, IN_PORT, true);
        checkVersionChecker(V_1_0, IN_PHY_PORT, false);
        checkVersionChecker(V_1_0, METADATA, false);
        checkVersionChecker(V_1_0, ETH_DST, true);
        checkVersionChecker(V_1_0, ETH_SRC, true);
        checkVersionChecker(V_1_0, ETH_TYPE, true);
        checkVersionChecker(V_1_0, VLAN_VID, true);
        checkVersionChecker(V_1_0, VLAN_PCP, true);
        checkVersionChecker(V_1_0, IP_DSCP, true);
        checkVersionChecker(V_1_0, IP_ECN, false);
        checkVersionChecker(V_1_0, IP_PROTO, true);
        checkVersionChecker(V_1_0, IPV4_SRC, true);
        checkVersionChecker(V_1_0, IPV4_DST, true);
        checkVersionChecker(V_1_0, TCP_SRC, true);
        checkVersionChecker(V_1_0, TCP_DST, true);
        checkVersionChecker(V_1_0, UDP_SRC, true);
        checkVersionChecker(V_1_0, UDP_DST, true);
        checkVersionChecker(V_1_0, SCTP_SRC, false);
        checkVersionChecker(V_1_0, SCTP_DST, false);
        checkVersionChecker(V_1_0, ICMPV4_TYPE, true);
        checkVersionChecker(V_1_0, ICMPV4_CODE, true);
        checkVersionChecker(V_1_0, ARP_OP, true);
        checkVersionChecker(V_1_0, ARP_SPA, false);
        checkVersionChecker(V_1_0, ARP_TPA, false);
        checkVersionChecker(V_1_0, ARP_SHA, false);
        checkVersionChecker(V_1_0, ARP_THA, false);
        checkVersionChecker(V_1_0, IPV6_SRC, false);
        checkVersionChecker(V_1_0, IPV6_DST, false);
        checkVersionChecker(V_1_0, IPV6_FLABEL, false);
        checkVersionChecker(V_1_0, ICMPV6_TYPE, false);
        checkVersionChecker(V_1_0, ICMPV6_CODE, false);
        checkVersionChecker(V_1_0, IPV6_ND_TARGET, false);
        checkVersionChecker(V_1_0, IPV6_ND_SLL, false);
        checkVersionChecker(V_1_0, IPV6_ND_TLL, false);
        checkVersionChecker(V_1_0, MPLS_LABEL, false);
        checkVersionChecker(V_1_0, MPLS_TC, false);
        checkVersionChecker(V_1_0, MPLS_BOS, false);
        checkVersionChecker(V_1_0, PBB_ISID, false);
        checkVersionChecker(V_1_0, TUNNEL_ID, false);
        checkVersionChecker(V_1_0, IPV6_EXTHDR, false);
    }

    @Test
    public void versionCheck11() {
        print(EOL + "versionCheck11()");
        checkVersionChecker(V_1_1, IN_PORT, true);
        checkVersionChecker(V_1_1, IN_PHY_PORT, false);
        checkVersionChecker(V_1_1, METADATA, true);
        checkVersionChecker(V_1_1, ETH_DST, true);
        checkVersionChecker(V_1_1, ETH_SRC, true);
        checkVersionChecker(V_1_1, ETH_TYPE, true);
        checkVersionChecker(V_1_1, VLAN_VID, true);
        checkVersionChecker(V_1_1, VLAN_PCP, true);
        checkVersionChecker(V_1_1, IP_DSCP, true);
        checkVersionChecker(V_1_1, IP_ECN, false);
        checkVersionChecker(V_1_1, IP_PROTO, true);
        checkVersionChecker(V_1_1, IPV4_SRC, true);
        checkVersionChecker(V_1_1, IPV4_DST, true);
        checkVersionChecker(V_1_1, TCP_SRC, true);
        checkVersionChecker(V_1_1, TCP_DST, true);
        checkVersionChecker(V_1_1, UDP_SRC, true);
        checkVersionChecker(V_1_1, UDP_DST, true);
        checkVersionChecker(V_1_1, SCTP_SRC, true);
        checkVersionChecker(V_1_1, SCTP_DST, true);
        checkVersionChecker(V_1_1, ICMPV4_TYPE, true);
        checkVersionChecker(V_1_1, ICMPV4_CODE, true);
        checkVersionChecker(V_1_1, ARP_OP, true);
        checkVersionChecker(V_1_1, ARP_SPA, false);
        checkVersionChecker(V_1_1, ARP_TPA, false);
        checkVersionChecker(V_1_1, ARP_SHA, false);
        checkVersionChecker(V_1_1, ARP_THA, false);
        checkVersionChecker(V_1_1, IPV6_SRC, false);
        checkVersionChecker(V_1_1, IPV6_DST, false);
        checkVersionChecker(V_1_1, IPV6_FLABEL, false);
        checkVersionChecker(V_1_1, ICMPV6_TYPE, false);
        checkVersionChecker(V_1_1, ICMPV6_CODE, false);
        checkVersionChecker(V_1_1, IPV6_ND_TARGET, false);
        checkVersionChecker(V_1_1, IPV6_ND_SLL, false);
        checkVersionChecker(V_1_1, IPV6_ND_TLL, false);
        checkVersionChecker(V_1_1, MPLS_LABEL, true);
        checkVersionChecker(V_1_1, MPLS_TC, true);
        checkVersionChecker(V_1_1, MPLS_BOS, false);
        checkVersionChecker(V_1_1, PBB_ISID, false);
        checkVersionChecker(V_1_1, TUNNEL_ID, false);
        checkVersionChecker(V_1_1, IPV6_EXTHDR, false);
    }


    @Test
    public void versionCheck12() {
        print(EOL + "versionCheck12()");
        checkVersionChecker(V_1_2, IN_PORT, true);
        checkVersionChecker(V_1_2, IN_PHY_PORT, true);
        checkVersionChecker(V_1_2, METADATA, true);
        checkVersionChecker(V_1_2, ETH_DST, true);
        checkVersionChecker(V_1_2, ETH_SRC, true);
        checkVersionChecker(V_1_2, ETH_TYPE, true);
        checkVersionChecker(V_1_2, VLAN_VID, true);
        checkVersionChecker(V_1_2, VLAN_PCP, true);
        checkVersionChecker(V_1_2, IP_DSCP, true);
        checkVersionChecker(V_1_2, IP_ECN, true);
        checkVersionChecker(V_1_2, IP_PROTO, true);
        checkVersionChecker(V_1_2, IPV4_SRC, true);
        checkVersionChecker(V_1_2, IPV4_DST, true);
        checkVersionChecker(V_1_2, TCP_SRC, true);
        checkVersionChecker(V_1_2, TCP_DST, true);
        checkVersionChecker(V_1_2, UDP_SRC, true);
        checkVersionChecker(V_1_2, UDP_DST, true);
        checkVersionChecker(V_1_2, SCTP_SRC, true);
        checkVersionChecker(V_1_2, SCTP_DST, true);
        checkVersionChecker(V_1_2, ICMPV4_TYPE, true);
        checkVersionChecker(V_1_2, ICMPV4_CODE, true);
        checkVersionChecker(V_1_2, ARP_OP, true);
        checkVersionChecker(V_1_2, ARP_SPA, true);
        checkVersionChecker(V_1_2, ARP_TPA, true);
        checkVersionChecker(V_1_2, ARP_SHA, true);
        checkVersionChecker(V_1_2, ARP_THA, true);
        checkVersionChecker(V_1_2, IPV6_SRC, true);
        checkVersionChecker(V_1_2, IPV6_DST, true);
        checkVersionChecker(V_1_2, IPV6_FLABEL, true);
        checkVersionChecker(V_1_2, ICMPV6_TYPE, true);
        checkVersionChecker(V_1_2, ICMPV6_CODE, true);
        checkVersionChecker(V_1_2, IPV6_ND_TARGET, true);
        checkVersionChecker(V_1_2, IPV6_ND_SLL, true);
        checkVersionChecker(V_1_2, IPV6_ND_TLL, true);
        checkVersionChecker(V_1_2, MPLS_LABEL, true);
        checkVersionChecker(V_1_2, MPLS_TC, true);
        checkVersionChecker(V_1_2, MPLS_BOS, false);
        checkVersionChecker(V_1_2, PBB_ISID, false);
        checkVersionChecker(V_1_2, TUNNEL_ID, false);
        checkVersionChecker(V_1_2, IPV6_EXTHDR, false);
    }


    @Test
    public void versionCheck13() {
        print(EOL + "versionCheck13()");
        checkVersionChecker(V_1_3, IN_PORT, true);
        checkVersionChecker(V_1_3, IN_PHY_PORT, true);
        checkVersionChecker(V_1_3, METADATA, true);
        checkVersionChecker(V_1_3, ETH_DST, true);
        checkVersionChecker(V_1_3, ETH_SRC, true);
        checkVersionChecker(V_1_3, ETH_TYPE, true);
        checkVersionChecker(V_1_3, VLAN_VID, true);
        checkVersionChecker(V_1_3, VLAN_PCP, true);
        checkVersionChecker(V_1_3, IP_DSCP, true);
        checkVersionChecker(V_1_3, IP_ECN, true);
        checkVersionChecker(V_1_3, IP_PROTO, true);
        checkVersionChecker(V_1_3, IPV4_SRC, true);
        checkVersionChecker(V_1_3, IPV4_DST, true);
        checkVersionChecker(V_1_3, TCP_SRC, true);
        checkVersionChecker(V_1_3, TCP_DST, true);
        checkVersionChecker(V_1_3, UDP_SRC, true);
        checkVersionChecker(V_1_3, UDP_DST, true);
        checkVersionChecker(V_1_3, SCTP_SRC, true);
        checkVersionChecker(V_1_3, SCTP_DST, true);
        checkVersionChecker(V_1_3, ICMPV4_TYPE, true);
        checkVersionChecker(V_1_3, ICMPV4_CODE, true);
        checkVersionChecker(V_1_3, ARP_OP, true);
        checkVersionChecker(V_1_3, ARP_SPA, true);
        checkVersionChecker(V_1_3, ARP_TPA, true);
        checkVersionChecker(V_1_3, ARP_SHA, true);
        checkVersionChecker(V_1_3, ARP_THA, true);
        checkVersionChecker(V_1_3, IPV6_SRC, true);
        checkVersionChecker(V_1_3, IPV6_DST, true);
        checkVersionChecker(V_1_3, IPV6_FLABEL, true);
        checkVersionChecker(V_1_3, ICMPV6_TYPE, true);
        checkVersionChecker(V_1_3, ICMPV6_CODE, true);
        checkVersionChecker(V_1_3, IPV6_ND_TARGET, true);
        checkVersionChecker(V_1_3, IPV6_ND_SLL, true);
        checkVersionChecker(V_1_3, IPV6_ND_TLL, true);
        checkVersionChecker(V_1_3, MPLS_LABEL, true);
        checkVersionChecker(V_1_3, MPLS_TC, true);
        checkVersionChecker(V_1_3, MPLS_BOS, true);
        checkVersionChecker(V_1_3, PBB_ISID, true);
        checkVersionChecker(V_1_3, TUNNEL_ID, true);
        checkVersionChecker(V_1_3, IPV6_EXTHDR, true);
    }

    // the following tests are to validate defining TCP/UDP/SCTP ports for 1.0

    private static final PortNumber PORT_A = pn(2046);
    private static final PortNumber PORT_B = pn(25);

    @Test
    public void tcpPorts10() {
        print(EOL + "tcpPorts10()");
        final ProtocolVersion pv = V_1_0;
        MutableMatch mm = MatchFactory.createMatch(pv);
        mm.addField(createBasicField(pv, ETH_TYPE, EthernetType.IPv4));
        mm.addField(createBasicField(pv, IP_PROTO, IpProtocol.TCP));
        mm.addField(createBasicField(pv, TCP_SRC, PORT_A));
        mm.addField(createBasicField(pv, TCP_DST, PORT_B));
        Match m = (Match) mm.toImmutable();
        print(m.toDebugString());
        assertEquals(AM_UXS, 4, m.getMatchFields().size());
        // NOTE: all we really care about here is that the code compiles, and
        //       gets to here without throwing an IllegalArgumentException.
    }

    @Test
    public void udpPorts10() {
        print(EOL + "udpPorts10()");
        final ProtocolVersion pv = V_1_0;
        MutableMatch mm = MatchFactory.createMatch(pv);
        mm.addField(createBasicField(pv, ETH_TYPE, EthernetType.IPv4));
        mm.addField(createBasicField(pv, IP_PROTO, IpProtocol.UDP));
        mm.addField(createBasicField(pv, UDP_SRC, PORT_A));
        mm.addField(createBasicField(pv, UDP_DST, PORT_B));
        Match m = (Match) mm.toImmutable();
        print(m.toDebugString());
        assertEquals(AM_UXS, 4, m.getMatchFields().size());
        // NOTE: all we really care about here is that the code compiles, and
        //       gets to here without throwing an IllegalArgumentException.
    }

    // need to check that exception is thrown if PVs don't match
    @Test
    public void dontAllow10FieldsIn13Match() {
        print(EOL + "dontAllow10FieldsIn13Match()");
        try {
            MutableMatch mm = createMatch(V_1_3);
            mm.addField(createBasicField(V_1_0, ETH_TYPE, EthernetType.IPv4));
            print(mm);
            fail(AM_NOEX);
        } catch (VersionMismatchException e) {
            print(FMT_EX, e);
            assertEquals(AM_WREXMSG, "V_1_3: Field wrong version: V_1_0",
                    e.getMessage());
        } catch (Exception e) {
            print(e);
            fail(AM_WREX);
        }
    }

    @Test
    public void dontAllow13FieldsIn10Match() {
        print(EOL + "dontAllow13FieldsIn10Match()");
        try {
            MutableMatch mm = createMatch(V_1_0);
            mm.addField(createBasicField(V_1_3, ETH_TYPE, EthernetType.IPv4));
            print(mm);
            fail(AM_NOEX);
        } catch (VersionMismatchException e) {
            print(FMT_EX, e);
            assertEquals(AM_WREXMSG, "V_1_0: Field wrong version: V_1_3",
                    e.getMessage());
        } catch (Exception e) {
            print(e);
            fail(AM_WREX);
        }
    }

    // We need to support the arbitrary order of addition of fields

    @Test
    public void duplicateField() {
        print(EOL + "duplicateField()");
        final ProtocolVersion pv = V_1_3;
        MutableMatch mm = createMatch(pv);
        mm.addField(createBasicField(pv, ETH_TYPE, EthernetType.IPv4));
        mm.addField(createBasicField(pv, IPV4_DST, ip("10.0.0.3")));
        try {
            mm.addField(createBasicField(pv, ETH_TYPE, EthernetType.IPv4));
            fail(AM_NOEX);
        } catch (IllegalArgumentException e) {
            print(FMT_EX, e);
        }
        Match m = (Match) mm.toImmutable();
        print(m.toDebugString());
        assertEquals(AM_UXS, 2, m.getMatchFields().size());
    }

    @Test
    public void outOfOrderMatch() {
        print(EOL + "outOfOrderMatch()");
        final ProtocolVersion pv = V_1_3;
        MutableMatch mm = createMatch(pv);
        mm.addField(createBasicField(pv, ETH_TYPE, EthernetType.IPv4));
        mm.addField(createBasicField(pv, IPV4_DST, ip("10.0.0.3")));
        Match m1 = (Match) mm.toImmutable();
        print(m1.toDebugString());

        mm = createMatch(pv);
        mm.addField(createBasicField(pv, IPV4_DST, ip("10.0.0.3")));
        mm.addField(createBasicField(pv, ETH_TYPE, EthernetType.IPv4));
        Match m2 = (Match) mm.toImmutable();
        print(m2.toDebugString());

        assertEquals(AM_NEQ, m1, m2);
    }

    @Test
    public void catchMissingPreReq() {
        print(EOL + "catchMissingPreReq()");
        final ProtocolVersion pv = V_1_3;
        Match m;
        MutableMatch mm = createMatch(pv);
        try {
            mm.addField(createBasicField(pv, IPV4_DST, ip("10.0.0.3")));
            m  = (Match) mm.toImmutable();
            fail(AM_NOEX);
        } catch (ValidationException e) {
            print(FMT_EX, e);
        }
        // fix the problem...
        mm.addField(createBasicField(pv, ETH_TYPE, EthernetType.IPv4));
        m  = (Match) mm.toImmutable();
        print(m.toDebugString());
        assertEquals(AM_UXS, 2, m.getMatchFields().size());
    }

    @Test
    public void udpPorts10Reversed() {
        print(EOL + "udpPorts10Reversed()");
        final ProtocolVersion pv = V_1_0;
        MutableMatch mm = MatchFactory.createMatch(pv);
        mm.addField(createBasicField(pv, UDP_DST, PORT_B));
        mm.addField(createBasicField(pv, UDP_SRC, PORT_A));
        mm.addField(createBasicField(pv, IP_PROTO, IpProtocol.UDP));
        mm.addField(createBasicField(pv, ETH_TYPE, EthernetType.IPv4));
        Match m = (Match) mm.toImmutable();
        print(m.toDebugString());
        assertEquals(AM_UXS, 4, m.getMatchFields().size());
    }

    @Test
    public void udpPorts13Reversed() {
        print(EOL + "udpPorts13Reversed()");
        final ProtocolVersion pv = V_1_3;
        MutableMatch mm = MatchFactory.createMatch(pv);
        mm.addField(createBasicField(pv, UDP_DST, PORT_B));
        mm.addField(createBasicField(pv, UDP_SRC, PORT_A));
        mm.addField(createBasicField(pv, IP_PROTO, IpProtocol.UDP));
        mm.addField(createBasicField(pv, ETH_TYPE, EthernetType.IPv4));
        Match m = (Match) mm.toImmutable();
        print(m.toDebugString());
        assertEquals(AM_UXS, 4, m.getMatchFields().size());
    }


}
