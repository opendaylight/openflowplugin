/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.lib.match;

import org.junit.Test;
import org.opendaylight.of.lib.AbstractTest;
import org.opendaylight.of.lib.OfPacketReader;
import org.opendaylight.of.lib.ProtocolVersion;
import org.opendaylight.util.ValidationException;

import java.util.List;

import static org.junit.Assert.fail;
import static org.opendaylight.of.lib.match.MatchValidator.*;
import static org.opendaylight.util.StringUtils.toCamelCase;
import static org.opendaylight.util.junit.TestTools.*;

/**
 * Unit tests for the MatchValidator.
 *
 * @author Simon Hunt
 */
public class MatchValidatorTest extends AbstractTest {

    private static final String GOOD = "GOOD to go!";

    private OfPacketReader pkt;
    private Match match;

    private OfPacketReader getPkt(String name) {
        return getPacketReader("match/v13/prereq/" + name + ".hex");
    }

    private OfPacketReader getBadCb(String basename) {
        return getPkt(basename + "Bad");
    }
    private OfPacketReader getGoodCb(String basename) {
        return getPkt(basename + "Good");
    }
    private OfPacketReader getDupCb(String basename) {
        return getPkt(basename + "Dup");
    }

    /** Checks that the specified strings are present in the issues.
     *
     * @param ve the exception to validate
     * @param expStrs the expected strings
     */
    private void assertStringsInIssues(ValidationException ve,
                                       String... expStrs) {
        final List<String> issues = ve.getIssues();
        int foundCount = 0;
        for (String exp: expStrs) {
            print("  checking for the message string: \"{}\"", exp);
            boolean found = false;
            for (String s: issues) {
                if (s.contains(exp)) {
                    found = true;
                    break;
                }
            }
            if (found) {
                foundCount++;
                print("    ...found it");
            } else
                print("    ...MISSING");
        }
        if (foundCount < expStrs.length)
            fail("missing expected issue strings");
        if (issues.size() > foundCount)
            fail("unexpected issue strings present");
    }

    /** Makes sure the parsed match actually has the dependent field
     * that we are trying to test, in it.
     *
     * @param match the parsed match
     * @param depField the dependent field
     */
    private void checkSanity(Match match, OxmBasicFieldType depField) {
        boolean found = false;
        for (MatchField mf : match.getMatchFields())
            if (mf.getFieldType() == depField) {
                found = true;
                break;
            }
        if (!found)
            fail("Missing dependent field: " + depField);
    }

    /** Verifies the Happy-Path.
     *
     * @param suffix the dependent field suffix
     * @param depField the dependent field
     */
    private void verifyGood(String suffix, OxmBasicFieldType depField) {
        pkt = getGoodCb(toCamelCase(depField)+suffix);
        print(pkt);
        try {
            match = MatchFactory.parseMatch(pkt, ProtocolVersion.V_1_3);
            print(match.toDebugString());
            checkSanity(match, depField);
            MatchFactory.validatePreRequisites(match);
            print(GOOD);
        } catch (Exception e) {
            print(e);
            fail(AM_UNEX);
        }
        checkEOBuffer(pkt);
    }

    private void verifyGood(OxmBasicFieldType depField) {
        verifyGood("", depField);
    }


    /** Verifies the Naughty-Path.
     *
     * @param pkt the data buffer to parse
     * @param depField the dependent field
     * @param expIssues expected issue strings
     */
    private void verifyIssues(OfPacketReader pkt, OxmBasicFieldType depField,
                              String... expIssues) {
        print(pkt);
        try {
            match = MatchFactory.parseMatch(pkt, ProtocolVersion.V_1_3);
            print(match.toDebugString());
            checkSanity(match, depField);
            MatchFactory.validatePreRequisites(match);
            fail(AM_NOEX);
        } catch (ValidationException e) {
            print(FMT_EX, e);
            assertStringsInIssues(e, expIssues);
        } catch (Exception e) {
            print(e);
            fail(AM_UNEX);
        }
        checkEOBuffer(pkt);
    }

    private void verifyBadIssues(OxmBasicFieldType depField,
                                 String... expIssues) {
        verifyIssues(getBadCb(toCamelCase(depField)), depField, expIssues);
    }

    private void verifyBadIssues(String suffix, OxmBasicFieldType depField,
                                 String... expIssues) {
        verifyIssues(getBadCb(toCamelCase(depField) + suffix),
                     depField, expIssues);
    }

    private void verifyDupIssues(OxmBasicFieldType depField,
                                 String... expIssues) {
        verifyIssues(getDupCb(toCamelCase(depField)), depField, expIssues);
    }


    /* =====================================================================
     *   TESTING STARTS HERE...
     * ===================================================================== */

    // === IN_PORT : no pre-requisites
    @Test
    public void inPortGood() {
        print(EOL + "inPortGood()");
        verifyGood(OxmBasicFieldType.IN_PORT);
    }

    @Test
    public void inPortDup() {
        print(EOL + "inPortDup()");
        verifyDupIssues(OxmBasicFieldType.IN_PORT, E_DUP);
    }

    // === IN_PHY_PORT : requires IN_PORT present
    @Test
    public void inPhyPortGood() {
        print(EOL + "inPhyPortGood()");
        verifyGood(OxmBasicFieldType.IN_PHY_PORT);
    }

    @Test
    public void inPhyPortBad() {
        print(EOL + "inPhyPortBad()");
        verifyBadIssues(OxmBasicFieldType.IN_PHY_PORT, REQ_IN_PORT);
    }

    @Test
    public void inPhyPortDup() {
        print(EOL + "inPhyPortDup()");
        verifyDupIssues(OxmBasicFieldType.IN_PHY_PORT, REQ_IN_PORT, E_DUP);
    }

    // === METADATA : no pre-requisites
    @Test
    public void metadataGood() {
        print(EOL + "metadataGood()");
        verifyGood(OxmBasicFieldType.METADATA);
    }

    @Test
    public void metadataDup() {
        print(EOL + "metadataDup()");
        verifyDupIssues(OxmBasicFieldType.METADATA, E_DUP);
    }

    // === ETH_DST : no pre-requisites
    @Test
    public void ethDstGood() {
        print(EOL + "ethDstGood()");
        verifyGood(OxmBasicFieldType.ETH_DST);
    }

    @Test
    public void ethDstDup() {
        print(EOL + "ethDstDup()");
        verifyDupIssues(OxmBasicFieldType.ETH_DST, E_DUP);
    }

    // === ETH_SRC : no pre-requisites
    @Test
    public void ethSrcGood() {
        print(EOL + "ethSrcGood()");
        verifyGood(OxmBasicFieldType.ETH_SRC);
    }

    @Test
    public void ethSrcDup() {
        print(EOL + "ethSrcDup()");
        verifyDupIssues(OxmBasicFieldType.ETH_SRC, E_DUP);
    }

    // === ETH_TYPE : no pre-requisites
    @Test
    public void ethTypeGood() {
        print(EOL + "ethTypeGood()");
        verifyGood(OxmBasicFieldType.ETH_TYPE);
    }

    @Test
    public void ethTypeDup() {
        print(EOL + "ethTypeDup()");
        verifyDupIssues(OxmBasicFieldType.ETH_TYPE, E_DUP);
    }

    // === VLAN_VID : no pre-requisites
    @Test
    public void vlanVidGood() {
        print(EOL + "vlanVidGood()");
        verifyGood(OxmBasicFieldType.VLAN_VID);
    }

    @Test
    public void vlanVidDup() {
        print(EOL + "vlanVidDup()");
        verifyDupIssues(OxmBasicFieldType.VLAN_VID, E_DUP) ;
    }

    // === VLAN_PCP : requires VLAN_VID != NONE
    @Test
    public void vlanPcpGood() {
        print(EOL + "vlanPcpGood()");
        verifyGood(OxmBasicFieldType.VLAN_PCP);
    }

    @Test
    public void vlanPcpDup() {
        print(EOL + "vlanPcpDup()");
        verifyDupIssues(OxmBasicFieldType.VLAN_PCP, E_DUP);
    }

    @Test
    public void vlanPcpNoVidBad() {
        print(EOL + "vlanPcpNoVidBad()");
        verifyBadIssues("NoVid", OxmBasicFieldType.VLAN_PCP,
                REQ_VLAN_VID_NOT_NONE);
    }

    @Test
    public void vlanPcpVidNoneBad() {
        print(EOL + "vlanPcpVidNoneBad()");
        verifyBadIssues("VidNone", OxmBasicFieldType.VLAN_PCP,
                REQ_VLAN_VID_NOT_NONE);
    }

    // === IP_DSCP : requires ETH_TYPE = IPv4 or IPv6
    @Test
    public void ipDscpIPv4Good() {
        print(EOL + "ipDscpIPv4Good()");
        verifyGood("IPv4", OxmBasicFieldType.IP_DSCP);
    }

    @Test
    public void ipDscpIPv6Good() {
        print(EOL + "ipDscpIPv6Good()");
        verifyGood("IPv6", OxmBasicFieldType.IP_DSCP);
    }

    @Test
    public void ipDscpDup() {
        print(EOL + "ipDscpDup()");
        verifyDupIssues(OxmBasicFieldType.IP_DSCP, E_DUP);
    }

    @Test
    public void ipDscpBad() {
        print(EOL + "ipDscpBad()");
        verifyBadIssues(OxmBasicFieldType.IP_DSCP, REQ_ETH_TYPE_IP4IP6);
    }

    @Test
    public void ipDscpNoEthBad() {
        print(EOL + "ipDscpNoEthBad()");
        verifyBadIssues("NoEth", OxmBasicFieldType.IP_DSCP,
                REQ_ETH_TYPE_IP4IP6);
    }

    // === IP_ECN : requires ETH_TYPE = IPv4 or IPv6
    @Test
    public void ipEcnIPv4Good() {
        print(EOL + "ipEcnIPv4Good()");
        verifyGood("IPv4", OxmBasicFieldType.IP_ECN);
    }

    @Test
    public void ipEcnIPv6Good() {
        print(EOL + "ipEcnIPv6Good()");
        verifyGood("IPv6", OxmBasicFieldType.IP_ECN);
    }

    @Test
    public void ipEcnDup() {
        print(EOL + "ipEcnDup()");
        verifyDupIssues(OxmBasicFieldType.IP_ECN, E_DUP);
    }

    @Test
    public void ipEcnBad() {
        print(EOL + "ipEcnBad()");
        verifyBadIssues(OxmBasicFieldType.IP_ECN, REQ_ETH_TYPE_IP4IP6);
    }

    @Test
    public void ipEcnNoEthBad() {
        print(EOL + "ipEcnNoEthBad()");
        verifyBadIssues("NoEth", OxmBasicFieldType.IP_ECN, REQ_ETH_TYPE_IP4IP6);
    }

    // === IP_PROTO : requires ETH_TYPE = IPv4 or IPv6
    @Test
    public void ipProtoIPv4Good() {
        print(EOL + "ipProtoIPv4Good()");
        verifyGood("IPv4", OxmBasicFieldType.IP_PROTO);
    }

    @Test
    public void ipProtoIPv6Good() {
        print(EOL + "ipProtoIPv6Good()");
        verifyGood("IPv6", OxmBasicFieldType.IP_PROTO);
    }

    @Test
    public void ipProtoDup() {
        print(EOL + "ipProtoDup()");
        verifyDupIssues(OxmBasicFieldType.IP_PROTO, E_DUP);
    }

    @Test
    public void ipProtoBad() {
        print(EOL + "ipProtoBad()");
        verifyBadIssues(OxmBasicFieldType.IP_PROTO, REQ_ETH_TYPE_IP4IP6);
    }

    @Test
    public void ipProtoNoEthBad() {
        print(EOL + "ipProtoNoEthBad()");
        verifyBadIssues("NoEth", OxmBasicFieldType.IP_PROTO,
                REQ_ETH_TYPE_IP4IP6);
    }

    // === IPV4_SRC : requires ETH_TYPE = IPv4
    @Test
    public void ipv4SrcGood() {
        print(EOL + "ipv4SrcGood()");
        verifyGood(OxmBasicFieldType.IPV4_SRC);
    }

    @Test
    public void ipv4SrcBad() {
        print(EOL + "ipv4SrcBad()");
        verifyBadIssues(OxmBasicFieldType.IPV4_SRC, REQ_ETH_TYPE_IP4);
    }

    @Test
    public void ipv4SrcNoEthBad() {
        print(EOL + "ipv4SrcNoEthBad()");
        verifyBadIssues("NoEth", OxmBasicFieldType.IPV4_SRC, REQ_ETH_TYPE_IP4);
    }

    @Test
    public void ipv4SrcDup() {
        print(EOL + "ipv4SrcDup()");
        verifyDupIssues(OxmBasicFieldType.IPV4_SRC, E_DUP);
    }

    // === IPV4_DST : requires ETH_TYPE = IPv4
    @Test
    public void ipv4DstGood() {
        print(EOL + "ipv4DstGood()");
        verifyGood(OxmBasicFieldType.IPV4_DST);
    }

    @Test
    public void ipv4DstBad() {
        print(EOL + "ipv4DstBad()");
        verifyBadIssues(OxmBasicFieldType.IPV4_DST, REQ_ETH_TYPE_IP4);
    }

    @Test
    public void ipv4DstNoEthBad() {
        print(EOL + "ipv4DstNoEthBad()");
        verifyBadIssues("NoEth", OxmBasicFieldType.IPV4_DST, REQ_ETH_TYPE_IP4);
    }

    @Test
    public void ipv4DstDup() {
        print(EOL + "ipv4DstDup()");
        verifyDupIssues(OxmBasicFieldType.IPV4_DST, E_DUP);
    }

    // === TCP_SRC : requires IP_PROTO = TCP
    @Test
    public void tcpSrcGood() {
        print(EOL + "TcpSrcGood()");
        verifyGood(OxmBasicFieldType.TCP_SRC);
    }

    @Test
    public void tcpSrcBad() {
        print(EOL + "TcpSrcBad()");
        verifyBadIssues(OxmBasicFieldType.TCP_SRC, REQ_IP_PROTO_TCP);
    }

    @Test
    public void tcpSrcNoProtoBad() {
        print(EOL + "TcpSrcNoProtoBad()");
        verifyBadIssues("NoProto", OxmBasicFieldType.TCP_SRC, REQ_IP_PROTO_TCP);
    }

    @Test
    public void tcpSrcDup() {
        print(EOL + "TcpSrcDup()");
        verifyDupIssues(OxmBasicFieldType.TCP_SRC, E_DUP);
    }

    // === TCP_DST : requires IP_PROTO = TCP
    @Test
    public void tcpDstGood() {
        print(EOL + "tcpDstGood()");
        verifyGood(OxmBasicFieldType.TCP_DST);
    }

    @Test
    public void tcpDstBad() {
        print(EOL + "tcpDstBad()");
        verifyBadIssues(OxmBasicFieldType.TCP_DST, REQ_IP_PROTO_TCP);
    }

    @Test
    public void tcpDstNoProtoBad() {
        print(EOL + "tcpDstNoProtoBad()");
        verifyBadIssues("NoProto", OxmBasicFieldType.TCP_DST, REQ_IP_PROTO_TCP);
    }

    @Test
    public void tcpDstDup() {
        print(EOL + "tcpDstDup()");
        verifyDupIssues(OxmBasicFieldType.TCP_DST, E_DUP);
    }

    // === UDP_SRC : requires IP_PROTO = UDP
    @Test
    public void udpSrcGood() {
        print(EOL + "udpSrcGood()");
        verifyGood(OxmBasicFieldType.UDP_SRC);
    }

    @Test
    public void udpSrcBad() {
        print(EOL + "udpSrcBad()");
        verifyBadIssues(OxmBasicFieldType.UDP_SRC, REQ_IP_PROTO_UDP);
    }

    @Test
    public void udpSrcNoProtoBad() {
        print(EOL + "udpSrcNoProtoBad()");
        verifyBadIssues("NoProto", OxmBasicFieldType.UDP_SRC, REQ_IP_PROTO_UDP);
    }

    @Test
    public void udpSrcDup() {
        print(EOL + "udpSrcDup()");
        verifyDupIssues(OxmBasicFieldType.UDP_SRC, E_DUP);
    }

    // === UDP_DST : requires IP_PROTO = UDP
    @Test
    public void udpDstGood() {
        print(EOL + "udpDstGood()");
        verifyGood(OxmBasicFieldType.UDP_DST);
    }

    @Test
    public void udpDstBad() {
        print(EOL + "udpDstBad()");
        verifyBadIssues(OxmBasicFieldType.UDP_DST, REQ_IP_PROTO_UDP);
    }

    @Test
    public void udpDstNoProtoBad() {
        print(EOL + "udpDstNoProtoBad()");
        verifyBadIssues("NoProto", OxmBasicFieldType.UDP_DST, REQ_IP_PROTO_UDP);
    }

    @Test
    public void udpDstDup() {
        print(EOL + "udpDstDup()");
        verifyDupIssues(OxmBasicFieldType.UDP_DST, E_DUP);
    }

    // === SCTP_SRC : requires IP_PROTO = SCTP
    @Test
    public void sctpSrcGood() {
        print(EOL + "sctpSrcGood()");
        verifyGood(OxmBasicFieldType.SCTP_SRC);
    }

    @Test
    public void sctpSrcBad() {
        print(EOL + "sctpSrcBad()");
        verifyBadIssues(OxmBasicFieldType.SCTP_SRC, REQ_IP_PROTO_SCTP);
    }

    @Test
    public void sctpSrcNoProtoBad() {
        print(EOL + "sctpSrcNoProtoBad()");
        verifyBadIssues("NoProto", OxmBasicFieldType.SCTP_SRC,
                REQ_IP_PROTO_SCTP);
    }

    @Test
    public void sctpSrcDup() {
        print(EOL + "sctpSrcDup()");
        verifyDupIssues(OxmBasicFieldType.SCTP_SRC, E_DUP);
    }

    // === SCTP_DST : requires IP_PROTO = SCTP
    @Test
    public void sctpDstGood() {
        print(EOL + "sctpDstGood()");
        verifyGood(OxmBasicFieldType.SCTP_DST);
    }

    @Test
    public void sctpDstBad() {
        print(EOL + "sctpDstBad()");
        verifyBadIssues(OxmBasicFieldType.SCTP_DST, REQ_IP_PROTO_SCTP);
    }

    @Test
    public void sctpDstNoProtoBad() {
        print(EOL + "sctpDstNoProtoBad()");
        verifyBadIssues("NoProto", OxmBasicFieldType.SCTP_DST,
                REQ_IP_PROTO_SCTP);
    }

    @Test
    public void sctpDstDup() {
        print(EOL + "sctpDstDup()");
        verifyDupIssues(OxmBasicFieldType.SCTP_DST, E_DUP);
    }


    // === ICMPV4_TYPE : requires IP_PROTO = ICMP
    @Test
    public void icmpv4TypeGood() {
        print(EOL + "icmpv4TypeGood()");
        verifyGood(OxmBasicFieldType.ICMPV4_TYPE);
    }

    @Test
    public void icmpv4TypeBad() {
        print(EOL + "icmpv4TypeBad()");
        verifyBadIssues(OxmBasicFieldType.ICMPV4_TYPE, REQ_IP_PROTO_ICMP);
    }

    @Test
    public void icmpv4TypeNoProtoBad() {
        print(EOL + "icmpv4TypeNoProtoBad()");
        verifyBadIssues("NoProto", OxmBasicFieldType.ICMPV4_TYPE,
                REQ_IP_PROTO_ICMP);
    }

    @Test
    public void icmpv4TypeDup() {
        print(EOL + "icmpv4TypeDup()");
        verifyDupIssues(OxmBasicFieldType.ICMPV4_TYPE, E_DUP);
    }

    // === ICMPV4_CODE : requires IP_PROTO = ICMP
    @Test
    public void icmpv4CodeGood() {
        print(EOL + "icmpv4CodeGood()");
        verifyGood(OxmBasicFieldType.ICMPV4_CODE);
    }

    @Test
    public void icmpv4CodeBad() {
        print(EOL + "icmpv4CodeBad()");
        verifyBadIssues(OxmBasicFieldType.ICMPV4_CODE, REQ_IP_PROTO_ICMP);
    }

    @Test
    public void icmpv4CodeNoProtoBad() {
        print(EOL + "icmpv4CodeNoProtoBad()");
        verifyBadIssues("NoProto", OxmBasicFieldType.ICMPV4_CODE,
                REQ_IP_PROTO_ICMP);
    }

    @Test
    public void icmpv4CodeDup() {
        print(EOL + "icmpv4CodeDup()");
        verifyDupIssues(OxmBasicFieldType.ICMPV4_CODE, E_DUP);
    }

    // === ARP_OP : requires ETH_TYPE = ARP
    @Test
    public void arpOpGood() {
        print(EOL + "arpOpGood()");
        verifyGood(OxmBasicFieldType.ARP_OP);
    }

    @Test
    public void arpOpBad() {
        print(EOL + "arpOpBad()");
        verifyBadIssues(OxmBasicFieldType.ARP_OP, REQ_ETH_TYPE_ARP);
    }

    @Test
    public void arpOpNoEthBad() {
        print(EOL + "arpOpNoEthBad()");
        verifyBadIssues("NoEth", OxmBasicFieldType.ARP_OP, REQ_ETH_TYPE_ARP);
    }

    @Test
    public void arpOpDup() {
        print(EOL + "arpOpDup()");
        verifyDupIssues(OxmBasicFieldType.ARP_OP, E_DUP);
    }

    // === ARP_SPA : requires ETH_TYPE = ARP
    @Test
    public void arpSpaGood() {
        print(EOL + "arpSpaGood()");
        verifyGood(OxmBasicFieldType.ARP_SPA);
    }

    @Test
    public void arpSpaBad() {
        print(EOL + "arpSpaBad()");
        verifyBadIssues(OxmBasicFieldType.ARP_SPA, REQ_ETH_TYPE_ARP);
    }

    @Test
    public void arpSpaNoEthBad() {
        print(EOL + "arpSpaNoEthBad()");
        verifyBadIssues("NoEth", OxmBasicFieldType.ARP_SPA, REQ_ETH_TYPE_ARP);
    }

    @Test
    public void arpSpaDup() {
        print(EOL + "arpSpaDup()");
        verifyDupIssues(OxmBasicFieldType.ARP_SPA, E_DUP);
    }

    // === ARP_TPA : requires ETH_TYPE = ARP
    @Test
    public void arpTpaGood() {
        print(EOL + "arpTpaGood()");
        verifyGood(OxmBasicFieldType.ARP_TPA);
    }

    @Test
    public void arpTpaBad() {
        print(EOL + "arpTpaBad()");
        verifyBadIssues(OxmBasicFieldType.ARP_TPA, REQ_ETH_TYPE_ARP);
    }

    @Test
    public void arpTpaNoEthBad() {
        print(EOL + "arpTpaNoEthBad()");
        verifyBadIssues("NoEth", OxmBasicFieldType.ARP_TPA, REQ_ETH_TYPE_ARP);
    }

    @Test
    public void arpTpaDup() {
        print(EOL + "arpTpaDup()");
        verifyDupIssues(OxmBasicFieldType.ARP_TPA, E_DUP);
    }

    // === ARP_SHA : requires ETH_TYPE = ARP
    @Test
    public void arpShaGood() {
        print(EOL + "arpShaGood()");
        verifyGood(OxmBasicFieldType.ARP_SHA);
    }

    @Test
    public void arpShaBad() {
        print(EOL + "arpShaBad()");
        verifyBadIssues(OxmBasicFieldType.ARP_SHA, REQ_ETH_TYPE_ARP);
    }

    @Test
    public void arpShaNoEthBad() {
        print(EOL + "arpShaNoEthBad()");
        verifyBadIssues("NoEth", OxmBasicFieldType.ARP_SHA, REQ_ETH_TYPE_ARP);
    }

    @Test
    public void arpShaDup() {
        print(EOL + "arpShaDup()");
        verifyDupIssues(OxmBasicFieldType.ARP_SHA, E_DUP);
    }

    // === ARP_THA : requires ETH_TYPE = ARP
    @Test
    public void arpThaGood() {
        print(EOL + "arpThaGood()");
        verifyGood(OxmBasicFieldType.ARP_THA);
    }

    @Test
    public void arpThaBad() {
        print(EOL + "arpThaBad()");
        verifyBadIssues(OxmBasicFieldType.ARP_THA, REQ_ETH_TYPE_ARP);
    }

    @Test
    public void arpThaNoEthBad() {
        print(EOL + "arpThaNoEthBad()");
        verifyBadIssues("NoEth", OxmBasicFieldType.ARP_THA, REQ_ETH_TYPE_ARP);
    }

    @Test
    public void arpThaDup() {
        print(EOL + "arpThaDup()");
        verifyDupIssues(OxmBasicFieldType.ARP_THA, E_DUP);
    }

    // === IPV6_SRC : requires ETH_TYPE = IPv6
    @Test
    public void ipv6SrcGood() {
        print(EOL + "ipv6SrcGood()");
        verifyGood(OxmBasicFieldType.IPV6_SRC);
    }

    @Test
    public void ipv6SrcBad() {
        print(EOL + "ipv6SrcBad()");
        verifyBadIssues(OxmBasicFieldType.IPV6_SRC, REQ_ETH_TYPE_IP6);
    }

    @Test
    public void ipv6SrcNoEthBad() {
        print(EOL + "ipv6SrcNoEthBad()");
        verifyBadIssues("NoEth", OxmBasicFieldType.IPV6_SRC, REQ_ETH_TYPE_IP6);
    }

    @Test
    public void ipv6SrcDup() {
        print(EOL + "ipv6SrcDup()");
        verifyDupIssues(OxmBasicFieldType.IPV6_SRC, E_DUP);
    }

    // === IPV6_DST : requires ETH_TYPE = IPv6
    @Test
    public void ipv6DstGood() {
        print(EOL + "ipv6DstGood()");
        verifyGood(OxmBasicFieldType.IPV6_DST);
    }

    @Test
    public void ipv6DstBad() {
        print(EOL + "ipv6DstBad()");
        verifyBadIssues(OxmBasicFieldType.IPV6_DST, REQ_ETH_TYPE_IP6);
    }

    @Test
    public void ipv6DstNoEthBad() {
        print(EOL + "ipv6DstNoEthBad()");
        verifyBadIssues("NoEth", OxmBasicFieldType.IPV6_DST, REQ_ETH_TYPE_IP6);
    }

    @Test
    public void ipv6DstDup() {
        print(EOL + "ipv6DstDup()");
        verifyDupIssues(OxmBasicFieldType.IPV6_DST, E_DUP);
    }

    // === IPV6_FLABEL : requires ETH_TYPE = IPv6
    @Test
    public void ipv6FlabelGood() {
        print(EOL + "ipv6FlabelGood()");
        verifyGood(OxmBasicFieldType.IPV6_FLABEL);
    }

    @Test
    public void ipv6FlabelBad() {
        print(EOL + "ipv6FlabelBad()");
        verifyBadIssues(OxmBasicFieldType.IPV6_FLABEL, REQ_ETH_TYPE_IP6);
    }

    @Test
    public void ipv6FlabelNoEthBad() {
        print(EOL + "ipv6FlabelNoEthBad()");
        verifyBadIssues("NoEth", OxmBasicFieldType.IPV6_FLABEL,
                REQ_ETH_TYPE_IP6);
    }

    @Test
    public void ipv6FlabelDup() {
        print(EOL + "ipv6FlabelDup()");
        verifyDupIssues(OxmBasicFieldType.IPV6_FLABEL, E_DUP);
    }

    // === ICMPV6_TYPE : requires IP_PROTO = ICMPv6
    @Test
    public void icmpv6TypeGood() {
        print(EOL + "icmpv6TypeGood()");
        verifyGood(OxmBasicFieldType.ICMPV6_TYPE);
    }

    @Test
    public void icmpv6TypeBad() {
        print(EOL + "icmpv6TypeBad()");
        verifyBadIssues(OxmBasicFieldType.ICMPV6_TYPE, REQ_IP_PROTO_ICMPv6);
    }

    @Test
    public void icmpv6NoProtoTypeBad() {
        print(EOL + "icmpv6NoProtoTypeBad()");
        verifyBadIssues("NoProto", OxmBasicFieldType.ICMPV6_TYPE,
                REQ_IP_PROTO_ICMPv6);
    }

    @Test
    public void icmpv6TypeDup() {
        print(EOL + "icmpv6TypeDup()");
        verifyDupIssues(OxmBasicFieldType.ICMPV6_TYPE, E_DUP);
    }

    // === ICMPV6_CODE : requires IP_PROTO = ICMPv6
    @Test
    public void icmpv6CodeGood() {
        print(EOL + "icmpv6CodeGood()");
        verifyGood(OxmBasicFieldType.ICMPV6_CODE);
    }

    @Test
    public void icmpv6CodeBad() {
        print(EOL + "icmpv6CodeBad()");
        verifyBadIssues(OxmBasicFieldType.ICMPV6_CODE, REQ_IP_PROTO_ICMPv6);
    }

    @Test
    public void icmpv6NoProtoCodeBad() {
        print(EOL + "icmpv6NoProtoCodeBad()");
        verifyBadIssues("NoProto", OxmBasicFieldType.ICMPV6_CODE,
                REQ_IP_PROTO_ICMPv6);
    }

    @Test
    public void icmpv6CodeDup() {
        print(EOL + "icmpv6CodeDup()");
        verifyDupIssues(OxmBasicFieldType.ICMPV6_CODE, E_DUP);
    }

    // === IPV6_ND_TARGET : requires ICMPV6_TYPE = NBR_SOL or NBR_ADV
    @Test
    public void ipv6NdTargetNbrSolGood() {
        print(EOL + "ipv6NdTargetNbrSolGood()");
        verifyGood("NbrSol", OxmBasicFieldType.IPV6_ND_TARGET);
    }

    @Test
    public void ipv6NdTargetNbrAdvGood() {
        print(EOL + "ipv6NdTargetNbrAdvGood()");
        verifyGood("NbrAdv", OxmBasicFieldType.IPV6_ND_TARGET);
    }

    @Test
    public void ipv6NdTargetBad() {
        print(EOL + "ipv6NdTargetBad()");
        verifyBadIssues(OxmBasicFieldType.IPV6_ND_TARGET, REQ_ICMPV6_SOLADV);
    }

    @Test
    public void ipv6NdTargetNoITypeBad() {
        print(EOL + "ipv6NdTargetNoITypeBad()");
        verifyBadIssues("NoIType", OxmBasicFieldType.IPV6_ND_TARGET,
                REQ_ICMPV6_SOLADV);
    }

    @Test
    public void ipv6NdTargetDup() {
        print(EOL + "ipv6NdTargetDup()");
        verifyDupIssues(OxmBasicFieldType.IPV6_ND_TARGET, E_DUP);
    }

    // === IPV6_ND_SLL : requires ICMPV6_TYPE = NBR_SOL
    @Test
    public void ipv6NdSllGood() {
        print(EOL + "ipv6NdSllGood()");
        verifyGood(OxmBasicFieldType.IPV6_ND_SLL);
    }

    @Test
    public void ipv6NdSllBad() {
        print(EOL + "ipv6NdSllBad()");
        verifyBadIssues(OxmBasicFieldType.IPV6_ND_SLL, REQ_ICMPV6_SOL);
    }

    @Test
    public void ipv6NdSllNoITypeBad() {
        print(EOL + "ipv6NdSllNoITypeBad()");
        verifyBadIssues("NoIType", OxmBasicFieldType.IPV6_ND_SLL,
                REQ_ICMPV6_SOL);
    }

    @Test
    public void ipv6NdSllDup() {
        print(EOL + "ipv6NdSllDup()");
        verifyDupIssues(OxmBasicFieldType.IPV6_ND_SLL, E_DUP);
    }

    // === IPV6_ND_TLL : requires ICMPV6_TYPE = NBR_ADV
    @Test
    public void ipv6NdTllGood() {
        print(EOL + "ipv6NdTllGood()");
        verifyGood(OxmBasicFieldType.IPV6_ND_TLL);
    }

    @Test
    public void ipv6NdTllBad() {
        print(EOL + "ipv6NdTllBad()");
        verifyBadIssues(OxmBasicFieldType.IPV6_ND_TLL, REQ_ICMPV6_ADV);
    }

    @Test
    public void ipv6NdTllNoITypeBad() {
        print(EOL + "ipv6NdTllNoITypeBad()");
        verifyBadIssues("NoIType", OxmBasicFieldType.IPV6_ND_TLL,
                REQ_ICMPV6_ADV);
    }

    @Test
    public void ipv6NdTllDup() {
        print(EOL + "ipv6NdTllDup()");
        verifyDupIssues(OxmBasicFieldType.IPV6_ND_SLL, E_DUP);
    }

    // === MPLS_LABEL : requires ETH_TYPE = MPLS_U or MPLS_M
    @Test
    public void mplsLabelMuGood() {
        print(EOL + "mplsLabelMuGood()");
        verifyGood("Mu", OxmBasicFieldType.MPLS_LABEL);
    }

    @Test
    public void mplsLabelMmGood() {
        print(EOL + "mplsLabelMmGood()");
        verifyGood("Mm", OxmBasicFieldType.MPLS_LABEL);
    }

    @Test
    public void mplsLabelBad() {
        print(EOL + "mplsLabelBad()");
        verifyBadIssues(OxmBasicFieldType.MPLS_LABEL, REQ_ETH_TYPE_MPLS);
    }

    @Test
    public void mplsLabelNoEthBad() {
        print(EOL + "mplsLabelNoEthBad()");
        verifyBadIssues("NoEth", OxmBasicFieldType.MPLS_LABEL,
                REQ_ETH_TYPE_MPLS);
    }

    @Test
    public void mplsLabelDup() {
        print(EOL + "mplsLabelDup()");
        verifyDupIssues(OxmBasicFieldType.MPLS_LABEL, E_DUP);
    }

    // === MPLS_TC : requires ETH_TYPE = MPLS_U or MPLS_M
    @Test
    public void mplsTcMuGood() {
        print(EOL + "mplsTcMuGood()");
        verifyGood("Mu", OxmBasicFieldType.MPLS_TC);
    }

    @Test
    public void mplsTcMmGood() {
        print(EOL + "mplsTcMmGood()");
        verifyGood("Mm", OxmBasicFieldType.MPLS_TC);
    }

    @Test
    public void mplsTcBad() {
        print(EOL + "mplsTcBad()");
        verifyBadIssues(OxmBasicFieldType.MPLS_TC, REQ_ETH_TYPE_MPLS);
    }

    @Test
    public void mplsTcNoEthBad() {
        print(EOL + "mplsTcNoEthBad()");
        verifyBadIssues("NoEth", OxmBasicFieldType.MPLS_TC, REQ_ETH_TYPE_MPLS);
    }

    @Test
    public void mplsTcDup() {
        print(EOL + "mplsTcDup()");
        verifyDupIssues(OxmBasicFieldType.MPLS_TC, E_DUP);
    }

    // === MPLS_BOS : requires ETH_TYPE = MPLS_U or MPLS_M
    @Test
    public void mplsBosMuGood() {
        print(EOL + "mplsBosMuGood()");
        verifyGood("Mu", OxmBasicFieldType.MPLS_BOS);
    }

    @Test
    public void mplsBosMmGood() {
        print(EOL + "mplsBosMmGood()");
        verifyGood("Mm", OxmBasicFieldType.MPLS_BOS);
    }

    @Test
    public void mplsBosBad() {
        print(EOL + "mplsBosBad()");
        verifyBadIssues(OxmBasicFieldType.MPLS_BOS, REQ_ETH_TYPE_MPLS);
    }

    @Test
    public void mplsBosNoEthBad() {
        print(EOL + "mplsBosNoEthBad()");
        verifyBadIssues("NoEth", OxmBasicFieldType.MPLS_BOS, REQ_ETH_TYPE_MPLS);
    }

    @Test
    public void mplsBosDup() {
        print(EOL + "mplsBosDup()");
        verifyDupIssues(OxmBasicFieldType.MPLS_BOS, E_DUP);
    }

    // === PBB_ISID : requires ETH_TYPE = PBB
    @Test
    public void pbbIsidGood() {
        print(EOL + "pbbIsidGood()");
        verifyGood(OxmBasicFieldType.PBB_ISID);
    }

    @Test
    public void pbbIsidBad() {
        print(EOL + "pbbIsidBad()");
        verifyBadIssues(OxmBasicFieldType.PBB_ISID, REQ_ETH_TYPE_PBB);
    }

    @Test
    public void pbbIsidNoEthBad() {
        print(EOL + "pbbIsidNoEthBad()");
        verifyBadIssues("NoEth", OxmBasicFieldType.PBB_ISID, REQ_ETH_TYPE_PBB);
    }

    @Test
    public void pbbIsidDup() {
        print(EOL + "pbbIsidDup()");
        verifyDupIssues(OxmBasicFieldType.PBB_ISID, E_DUP);
    }

    // === TUNNEL_ID : no pre-requisites
    @Test
    public void tunnelIdGood() {
        print(EOL + "tunnelIdGood()");
        verifyGood(OxmBasicFieldType.TUNNEL_ID);
    }

    @Test
    public void tunnelIdDup() {
        print(EOL + "tunnelIdDup()");
        verifyDupIssues(OxmBasicFieldType.TUNNEL_ID, E_DUP);
    }

    // === IPV6_EXTHDR : requires ETH_TYPE = IPv6
    @Test
    public void ipv6ExthdrGood() {
        print(EOL + "ipv6ExthdrGood()");
        verifyGood(OxmBasicFieldType.IPV6_EXTHDR);
    }

    @Test
    public void ipv6ExthdrBad() {
        print(EOL + "ipv6ExthdrBad()");
        verifyBadIssues(OxmBasicFieldType.IPV6_EXTHDR, REQ_ETH_TYPE_IP6);
    }

    @Test
    public void ipv6ExthdrNoEthBad() {
        print(EOL + "ipv6ExthdrNoEthBad()");
        verifyBadIssues("NoEth", OxmBasicFieldType.IPV6_EXTHDR,
                REQ_ETH_TYPE_IP6);
    }

    @Test
    public void ipv6ExthdrDup() {
        print(EOL + "ipv6ExthdrDup()");
        verifyDupIssues(OxmBasicFieldType.IPV6_EXTHDR, E_DUP);
    }

}
