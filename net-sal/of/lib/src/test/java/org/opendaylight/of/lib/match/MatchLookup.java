/*
 * (c) Copyright 2012,2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.opendaylight.util.SafeMap;

import static org.junit.Assert.*;
import static org.opendaylight.of.lib.match.OxmBasicFieldType.*;
import static org.opendaylight.util.junit.TestTools.AM_NEQ;
import static org.opendaylight.util.junit.TestTools.AM_WRCL;

/**
 * Utility test class.
 *
 * @author Simon Hunt
 */
public class MatchLookup {

    // mapping of match field type to concrete class
    private static final SafeMap<OxmBasicFieldType,
            Class<? extends MatchField>> MF_CLS =
            new SafeMap.Builder<OxmBasicFieldType,
                    Class<? extends MatchField>>(MatchField.class)
                    .add(IN_PORT, MfbInPort.class)
                    .add(IN_PHY_PORT, MfbInPhyPort.class)
                    .add(METADATA, MfbMetadata.class)
                    .add(ETH_DST, MfbEthDst.class)
                    .add(ETH_SRC, MfbEthSrc.class)
                    .add(ETH_TYPE, MfbEthType.class)
                    .add(VLAN_VID, MfbVlanVid.class)
                    .add(VLAN_PCP, MfbVlanPcp.class)
                    .add(IP_DSCP, MfbIpDscp.class)
                    .add(IP_ECN, MfbIpEcn.class)
                    .add(IP_PROTO, MfbIpProto.class)
                    .add(IPV4_SRC, MfbIpv4Src.class)
                    .add(IPV4_DST, MfbIpv4Dst.class)
                    .add(TCP_SRC, MfbTcpSrc.class)
                    .add(TCP_DST, MfbTcpDst.class)
                    .add(UDP_SRC, MfbUdpSrc.class)
                    .add(UDP_DST, MfbUdpDst.class)
                    .add(SCTP_SRC, MfbSctpSrc.class)
                    .add(SCTP_DST, MfbSctpDst.class)
                    .add(ICMPV4_TYPE, MfbIcmpv4Type.class)
                    .add(ICMPV4_CODE, MfbIcmpv4Code.class)
                    .add(ARP_OP, MfbArpOp.class)
                    .add(ARP_SPA, MfbArpSpa.class)
                    .add(ARP_TPA, MfbArpTpa.class)
                    .add(ARP_SHA, MfbArpSha.class)
                    .add(ARP_THA, MfbArpTha.class)
                    .add(IPV6_SRC, MfbIpv6Src.class)
                    .add(IPV6_DST, MfbIpv6Dst.class)
                    .add(IPV6_FLABEL, MfbIpv6Flabel.class)
                    .add(ICMPV6_TYPE, MfbIcmpv6Type.class)
                    .add(ICMPV6_CODE, MfbIcmpv6Code.class)
                    .add(IPV6_ND_TARGET, MfbIpv6NdTarget.class)
                    .add(IPV6_ND_SLL, MfbIpv6NdSll.class)
                    .add(IPV6_ND_TLL, MfbIpv6NdTll.class)
                    .add(MPLS_LABEL, MfbMplsLabel.class)
                    .add(MPLS_TC, MfbMplsTc.class)
                    .add(MPLS_BOS, MfbMplsBos.class)
                    .add(PBB_ISID, MfbPbbIsid.class)
                    .add(TUNNEL_ID, MfbTunnelId.class)
                    .add(IPV6_EXTHDR, MfbIpv6Exthdr.class)
                    .build();

    // mapping of match field type to base abstract class
    private static final SafeMap<OxmBasicFieldType,
            Class<? extends MFieldBasic>> MF_BASE_CLS =
            new SafeMap.Builder<OxmBasicFieldType,
                    Class<? extends MFieldBasic>>(MFieldBasic.class)
                    .add(IN_PORT, MFieldBasicBigPort.class)
                    .add(IN_PHY_PORT, MFieldBasicBigPort.class)
                    .add(METADATA, MFieldBasicLong.class)
                    .add(ETH_DST, MFieldBasicMac.class)
                    .add(ETH_SRC, MFieldBasicMac.class)
                    .add(ETH_TYPE, MfbEthType.class)
                    .add(VLAN_VID, MfbVlanVid.class)
                    .add(VLAN_PCP, MFieldBasicInt.class)
                    .add(IP_DSCP, MFieldBasicInt.class)
                    .add(IP_ECN, MFieldBasicInt.class)
                    .add(IP_PROTO, MfbIpProto.class)
                    .add(IPV4_SRC, MFieldBasicIp.class)
                    .add(IPV4_DST, MFieldBasicIp.class)
                    .add(TCP_SRC, MFieldBasicPort.class)
                    .add(TCP_DST, MFieldBasicPort.class)
                    .add(UDP_SRC, MFieldBasicPort.class)
                    .add(UDP_DST, MFieldBasicPort.class)
                    .add(SCTP_SRC, MFieldBasicPort.class)
                    .add(SCTP_DST, MFieldBasicPort.class)
                    .add(ICMPV4_TYPE, MfbIcmpv4Type.class)
                    .add(ICMPV4_CODE, MFieldBasicInt.class)
                    .add(ARP_OP, MFieldBasicInt.class)
                    .add(ARP_SPA, MFieldBasicIp.class)
                    .add(ARP_TPA, MFieldBasicIp.class)
                    .add(ARP_SHA, MFieldBasicMac.class)
                    .add(ARP_THA, MFieldBasicMac.class)
                    .add(IPV6_SRC, MFieldBasicIp.class)
                    .add(IPV6_DST, MFieldBasicIp.class)
                    .add(IPV6_FLABEL, MFieldBasicInt.class)
                    .add(ICMPV6_TYPE, MfbIcmpv6Type.class)
                    .add(ICMPV6_CODE, MFieldBasicInt.class)
                    .add(IPV6_ND_TARGET, MFieldBasicIp.class)
                    .add(IPV6_ND_SLL, MFieldBasicMac.class)
                    .add(IPV6_ND_TLL, MFieldBasicMac.class)
                    .add(MPLS_LABEL, MFieldBasicInt.class)
                    .add(MPLS_TC, MFieldBasicInt.class)
                    .add(MPLS_BOS, MFieldBasicInt.class)
                    .add(PBB_ISID, MFieldBasicInt.class)
                    .add(TUNNEL_ID, MFieldBasicLong.class)
                    .add(IPV6_EXTHDR, MfbIpv6Exthdr.class)
                    .build();


    /** Verifies that the specified match field is of the correct type,
     * and contains the specified value and mask (if any).
     *
     * @param mf the match field
     * @param expType the expected type
     * @param valueMask value and mask
     */
    public static void verifyField(MatchField mf, OxmBasicFieldType expType,
                          Object... valueMask) {

        assertEquals(AM_NEQ, expType, mf.getFieldType());
        assertTrue(AM_WRCL, MF_CLS.get(expType).isInstance(mf));

        Class<? extends MFieldBasic> base = MF_BASE_CLS.get(expType);
        int len = valueMask.length;
        // null mask means no mask
        if (len > 1 && valueMask[1] == null)
            len--;

        boolean expHasMask = len > 1;
        Object expValue = len > 0 ? valueMask[0] : null;
        Object expMask = len > 1 ? valueMask[1] : null;
        assertEquals(AM_NEQ, mf.hasMask(), expHasMask);

        //===============================================================
        // NOTE: Not a recommended pattern for production code, but this
        //       is convenient for testing match fields concisely.
        //
        //  ++++ DO NOT REPLICATE THIS PATTERN IN PRODUCTION CODE ++++
        //===============================================================
        if (base == MFieldBasicBigPort.class) {
            MFieldBasicBigPort p = (MFieldBasicBigPort) mf;
            assertEquals(AM_NEQ, expValue, p.getPort());

        } else if (base == MFieldBasicInt.class) {
            MFieldBasicInt i = (MFieldBasicInt) mf;
            assertEquals(AM_NEQ, expValue, i.getValue());
            if (expMask != null)
                assertEquals(AM_NEQ, expMask, i.getMask());

        } else if (base == MFieldBasicIp.class) {
            MFieldBasicIp ip = (MFieldBasicIp) mf;
            assertEquals(AM_NEQ, expValue, ip.getIpAddress());
            if (expMask != null)
                assertEquals(AM_NEQ, expMask, ip.getMask());

        } else if (base == MFieldBasicLong.class) {
            MFieldBasicLong l = (MFieldBasicLong) mf;
            assertEquals(AM_NEQ, expValue, l.getValue());
            if (expMask != null)
                assertEquals(AM_NEQ, expMask, l.getMask());

        } else if (base == MFieldBasicMac.class) {
            MFieldBasicMac mac = (MFieldBasicMac) mf;
            assertEquals(AM_NEQ, expValue, mac.getMacAddress());
            if (expMask != null)
                assertEquals(AM_NEQ, expMask, mac.getMask());

        } else if (base == MFieldBasicPort.class) {
            MFieldBasicPort p = (MFieldBasicPort) mf;
            assertEquals(AM_NEQ, expValue, p.getPort());

        } else if (base == MfbEthType.class) {
            MfbEthType et = (MfbEthType) mf;
            assertEquals(AM_NEQ, expValue, et.getEthernetType());

        } else if (base == MfbVlanVid.class) {
            MfbVlanVid vid = (MfbVlanVid) mf;
            assertEquals(AM_NEQ, expValue, vid.getVlanId());

        } else if (base == MfbIpProto.class) {
            MfbIpProto ipp = (MfbIpProto) mf;
            assertEquals(AM_NEQ, expValue, ipp.getIpProtocol());

        } else if (base == MfbIcmpv4Type.class) {
            MfbIcmpv4Type t = (MfbIcmpv4Type) mf;
            assertEquals(AM_NEQ, expValue, t.getICMPv4Type());

        } else if (base == MfbIcmpv6Type.class) {
            MfbIcmpv6Type t = (MfbIcmpv6Type) mf;
            assertEquals(AM_NEQ, expValue, t.getICMPv6Type());

        } else {
            fail("Did not match base class: " + base);
        }
    }


}
