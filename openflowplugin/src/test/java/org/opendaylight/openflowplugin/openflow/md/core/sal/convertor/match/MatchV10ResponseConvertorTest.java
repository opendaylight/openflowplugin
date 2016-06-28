/*
 * Copyright (c) 2014-2015 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import java.math.BigInteger;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowWildcardsV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10Builder;

/**
 * @author michal.polkorab
 *
 */
public class MatchV10ResponseConvertorTest {

    /**
     * Initializes OpenflowPortsUtil
     */
    @Before
    public void startUp() {
        OpenflowPortsUtil.init();
    }

    /**
     * Test {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchV10ResponseConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData)}
     */
    @Test
    public void test() {
        MatchV10Builder builder = new MatchV10Builder();
        builder.setWildcards(new FlowWildcardsV10(false, false, false, false,
                false, false, false, false, false, false));
        builder.setNwSrcMask((short) 24);
        builder.setNwDstMask((short) 16);
        builder.setInPort(6653);
        builder.setDlSrc(new MacAddress("01:01:01:01:01:01"));
        builder.setDlDst(new MacAddress("02:02:02:02:02:02"));
        builder.setDlVlan(128);
        builder.setDlVlanPcp((short) 2);
        builder.setDlType(15);
        builder.setNwTos((short) 16);
        builder.setNwProto((short) 6);
        builder.setNwSrc(new Ipv4Address("1.1.1.2"));
        builder.setNwDst(new Ipv4Address("32.16.8.1"));
        builder.setTpSrc(2048);
        builder.setTpDst(4096);
        MatchV10 match = builder.build();

        final VersionDatapathIdConvertorData datapathIdConvertorData = new VersionDatapathIdConvertorData(OFConstants.OFP_VERSION_1_0);
        datapathIdConvertorData.setDatapathId(new BigInteger("42"));

        final Match salMatch = convert(match, datapathIdConvertorData).build();

        Assert.assertEquals("Wrong in port", "openflow:42:6653", salMatch.getInPort().getValue());
        Assert.assertEquals("Wrong dl src", new MacAddress("01:01:01:01:01:01"), salMatch.getEthernetMatch()
                .getEthernetSource().getAddress());
        Assert.assertEquals("Wrong dl dst", new MacAddress("02:02:02:02:02:02"), salMatch.getEthernetMatch()
                .getEthernetDestination().getAddress());
        Assert.assertEquals("Wrong dl type", 15, salMatch.getEthernetMatch().getEthernetType().getType().getValue().intValue());
        Assert.assertEquals("Wrong dl vlan", 128, salMatch.getVlanMatch().getVlanId().getVlanId().getValue().intValue());
        Assert.assertEquals("Wrong dl vlan pcp", 2, salMatch.getVlanMatch().getVlanPcp().getValue().intValue());
        Ipv4Match ipv4Match = (Ipv4Match) salMatch.getLayer3Match();
        Assert.assertEquals("Wrong nw src address", "1.1.1.2/24", ipv4Match.getIpv4Source().getValue());
        Assert.assertEquals("Wrong nw dst address", "32.16.8.1/16", ipv4Match.getIpv4Destination().getValue());
        Assert.assertEquals("Wrong ip protocol", 6, salMatch.getIpMatch().getIpProtocol().intValue());
        Assert.assertEquals("Wrong ip proto", null, salMatch.getIpMatch().getIpProto());
        Assert.assertEquals("Wrong ip ecn", null, salMatch.getIpMatch().getIpEcn());
        Assert.assertEquals("Wrong ip dscp", 4, salMatch.getIpMatch().getIpDscp().getValue().intValue());
        TcpMatch tcpMatch = (TcpMatch) salMatch.getLayer4Match();
        Assert.assertEquals("Wrong tp dst", 4096, tcpMatch.getTcpDestinationPort().getValue().intValue());
        Assert.assertEquals("Wrong tp src", 2048, tcpMatch.getTcpSourcePort().getValue().intValue());
        Assert.assertEquals("Wrong ICMPv4 match", null, salMatch.getIcmpv4Match());
    }

    /**
     * Test {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchV10ResponseConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData)}
     */
    @Test
    public void testWildcardedMatch() {
        MatchV10Builder builder = new MatchV10Builder();
        builder.setWildcards(new FlowWildcardsV10(true, true, true, true,
                true, true, true, true, true, true));
        builder.setNwSrcMask((short) 24);
        builder.setNwDstMask((short) 16);
        builder.setInPort(6653);
        builder.setDlSrc(new MacAddress("01:01:01:01:01:01"));
        builder.setDlDst(new MacAddress("02:02:02:02:02:02"));
        builder.setDlVlan(128);
        builder.setDlVlanPcp((short) 2);
        builder.setDlType(15);
        builder.setNwTos((short) 14);
        builder.setNwProto((short) 6);
        builder.setNwSrc(new Ipv4Address("1.1.1.2"));
        builder.setNwDst(new Ipv4Address("32.16.8.1"));
        builder.setTpSrc(2048);
        builder.setTpDst(4096);
        MatchV10 match = builder.build();

        final VersionDatapathIdConvertorData datapathIdConvertorData = new VersionDatapathIdConvertorData(OFConstants.OFP_VERSION_1_0);
        datapathIdConvertorData.setDatapathId(new BigInteger("42"));

        final Match salMatch = convert(match, datapathIdConvertorData).build();

        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl match", null, salMatch.getEthernetMatch());
        Assert.assertEquals("Wrong dl vlan match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong layer 3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong layer 4 match", null, salMatch.getLayer4Match());
        Assert.assertEquals("Wrong ip match", null, salMatch.getIpMatch());
        Assert.assertEquals("Wrong ICMPv4 match", null, salMatch.getIcmpv4Match());
    }

    /**
     * Test {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchV10ResponseConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData)}
     */
    @Test
    public void testWildcardedMatchWithNoValuesSet() {
        MatchV10Builder builder = new MatchV10Builder();
        builder.setWildcards(new FlowWildcardsV10(true, true, true, true,
                true, true, true, true, true, true));
        MatchV10 match = builder.build();

        final VersionDatapathIdConvertorData datapathIdConvertorData = new VersionDatapathIdConvertorData(OFConstants.OFP_VERSION_1_0);
        datapathIdConvertorData.setDatapathId(new BigInteger("42"));

        final Match salMatch = convert(match, datapathIdConvertorData).build();

        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl match", null, salMatch.getEthernetMatch());
        Assert.assertEquals("Wrong dl vlan match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong layer 3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong layer 4 match", null, salMatch.getLayer4Match());
        Assert.assertEquals("Wrong ip match", null, salMatch.getIpMatch());
        Assert.assertEquals("Wrong ICMPv4 match", null, salMatch.getIcmpv4Match());
    }

    /**
     * Test {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchV10ResponseConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData)}
     */
    @Test
    public void testMatchWithValuesUnset() {
        MatchV10Builder builder = new MatchV10Builder();
        builder.setWildcards(new FlowWildcardsV10(false, false, false, false,
                false, false, false, false, false, false));
        builder.setNwProto((short) 17);
        builder.setTpSrc(2048);
        builder.setTpDst(4096);
        MatchV10 match = builder.build();

        final VersionDatapathIdConvertorData datapathIdConvertorData = new VersionDatapathIdConvertorData(OFConstants.OFP_VERSION_1_0);
        datapathIdConvertorData.setDatapathId(new BigInteger("42"));

        final Match salMatch = convert(match, datapathIdConvertorData).build();

        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl match", null, salMatch.getEthernetMatch());
        Assert.assertEquals("Wrong dl vlan match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong layer 3 match", null, salMatch.getLayer3Match());
        UdpMatch udpMatch = (UdpMatch) salMatch.getLayer4Match();
        Assert.assertEquals("Wrong udp dst", 4096, udpMatch.getUdpDestinationPort().getValue().intValue());
        Assert.assertEquals("Wrong udp src", 2048, udpMatch.getUdpSourcePort().getValue().intValue());
        Assert.assertEquals("Wrong ICMPv4 match", null, salMatch.getIcmpv4Match());
    }

    /**
     * Test {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchV10ResponseConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData)}
     */
    @Test(expected=NullPointerException.class)
    public void testEmptyMatch() {
        MatchV10Builder builder = new MatchV10Builder();
        MatchV10 match = builder.build();

        final VersionDatapathIdConvertorData datapathIdConvertorData = new VersionDatapathIdConvertorData(OFConstants.OFP_VERSION_1_0);
        datapathIdConvertorData.setDatapathId(new BigInteger("42"));

        final Match salMatch = convert(match, datapathIdConvertorData).build();
    }

    /**
     * ICMPv4 match test for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchV10ResponseConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData)}
     */
    @Test
    public void testIcmpv4Match() {
        // NW_PROTO, TP_SRC, TP_DST are wildcarded.
        Long dlType = 0x800L;
        FlowWildcardsV10 wc = new FlowWildcardsV10(
            true, true, false, true, true, true, true, true, true, true);
        MatchV10Builder builder = new MatchV10Builder().
            setWildcards(wc).
            setDlType(dlType.intValue());
        MatchV10 match = builder.build();

        BigInteger dpid = BigInteger.valueOf(12345L);
        final VersionDatapathIdConvertorData datapathIdConvertorData = new VersionDatapathIdConvertorData(OFConstants.OFP_VERSION_1_0);
        datapathIdConvertorData.setDatapathId(dpid);

        Match salMatch = convert(match, datapathIdConvertorData).build();

        EthernetMatch etherMatch = salMatch.getEthernetMatch();
        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl src",
                            null, etherMatch.getEthernetSource());
        Assert.assertEquals("Wrong dl dst",
                            null, etherMatch.getEthernetDestination());
        Assert.assertEquals("Wrong dl type", dlType,
                            etherMatch.getEthernetType().getType().getValue());
        Assert.assertEquals("Wrong VLAN match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong IP match", null, salMatch.getIpMatch());
        Assert.assertEquals("Wrong L3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong L4 match", null, salMatch.getLayer4Match());
        Assert.assertEquals("Wrong ICMPv4 match",
                            null, salMatch.getIcmpv4Match());

        // NW_PROTO is not wildcarded but null.
        wc = new FlowWildcardsV10(
            true, true, false, true, true, true, false, true, true, true);
        match = builder.setWildcards(wc).build();

        salMatch = convert(match, datapathIdConvertorData).build();
        etherMatch = salMatch.getEthernetMatch();
        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl src",
                            null, etherMatch.getEthernetSource());
        Assert.assertEquals("Wrong dl dst",
                            null, etherMatch.getEthernetDestination());
        Assert.assertEquals("Wrong dl type", dlType,
                            etherMatch.getEthernetType().getType().getValue());
        Assert.assertEquals("Wrong VLAN match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong IP match", null, salMatch.getIpMatch());
        Assert.assertEquals("Wrong L3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong L4 match", null, salMatch.getLayer4Match());
        Assert.assertEquals("Wrong ICMPv4 match",
                            null, salMatch.getIcmpv4Match());

        // Specify ICMPv4 protocol.
        Short ipProto = 1;
        match = builder.setNwProto(ipProto).build();
        salMatch = convert(match, datapathIdConvertorData).build();
        etherMatch = salMatch.getEthernetMatch();
        IpMatch ipMatch = salMatch.getIpMatch();
        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl src",
                            null, etherMatch.getEthernetSource());
        Assert.assertEquals("Wrong dl dst",
                            null, etherMatch.getEthernetDestination());
        Assert.assertEquals("Wrong dl type", dlType,
                            etherMatch.getEthernetType().getType().getValue());
        Assert.assertEquals("Wrong VLAN match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong ip protocol",
                            ipProto, ipMatch.getIpProtocol());
        Assert.assertEquals("Wrong ip proto", null, ipMatch.getIpProto());
        Assert.assertEquals("Wrong ip ecn", null, ipMatch.getIpEcn());
        Assert.assertEquals("Wrong ip dscp", null, ipMatch.getIpDscp());
        Assert.assertEquals("Wrong L3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong L4 match", null, salMatch.getLayer4Match());
        Assert.assertEquals("Wrong ICMPv4 match",
                            null, salMatch.getIcmpv4Match());

        // TP_SRC is not wildcarded but null.
        wc = new FlowWildcardsV10(
            true, true, false, true, true, true, false, true, true, false);
        match = builder.setWildcards(wc).build();
        salMatch = convert(match, datapathIdConvertorData).build();
        etherMatch = salMatch.getEthernetMatch();
        ipMatch = salMatch.getIpMatch();
        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl src",
                            null, etherMatch.getEthernetSource());
        Assert.assertEquals("Wrong dl dst",
                            null, etherMatch.getEthernetDestination());
        Assert.assertEquals("Wrong dl type", dlType,
                            etherMatch.getEthernetType().getType().getValue());
        Assert.assertEquals("Wrong VLAN match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong ip protocol",
                            ipProto, ipMatch.getIpProtocol());
        Assert.assertEquals("Wrong ip proto", null, ipMatch.getIpProto());
        Assert.assertEquals("Wrong ip ecn", null, ipMatch.getIpEcn());
        Assert.assertEquals("Wrong ip dscp", null, ipMatch.getIpDscp());
        Assert.assertEquals("Wrong L3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong L4 match", null, salMatch.getLayer4Match());
        Assert.assertEquals("Wrong ICMPv4 match",
                            null, salMatch.getIcmpv4Match());

        // Specify ICMPv4 type.
        Short icmpType = 10;
        match = builder.setTpSrc(icmpType.intValue()).build();
        salMatch = convert(match, datapathIdConvertorData).build();
        etherMatch = salMatch.getEthernetMatch();
        ipMatch = salMatch.getIpMatch();
        Icmpv4Match icmpv4Match = salMatch.getIcmpv4Match();
        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl src",
                            null, etherMatch.getEthernetSource());
        Assert.assertEquals("Wrong dl dst",
                            null, etherMatch.getEthernetDestination());
        Assert.assertEquals("Wrong dl type", dlType,
                            etherMatch.getEthernetType().getType().getValue());
        Assert.assertEquals("Wrong VLAN match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong ip protocol",
                            ipProto, ipMatch.getIpProtocol());
        Assert.assertEquals("Wrong ip proto", null, ipMatch.getIpProto());
        Assert.assertEquals("Wrong ip ecn", null, ipMatch.getIpEcn());
        Assert.assertEquals("Wrong ip dscp", null, ipMatch.getIpDscp());
        Assert.assertEquals("Wrong L3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong L4 match", null, salMatch.getLayer4Match());
        Assert.assertEquals("Wrong ICMPv4 type",
                            icmpType, icmpv4Match.getIcmpv4Type());
        Assert.assertEquals("Wrong ICMPv4 code",
                            null, icmpv4Match.getIcmpv4Code());

        // TP_DST is not wildcarded but null.
        wc = new FlowWildcardsV10(
            true, true, false, true, true, true, false, true, false, false);
        match = builder.setWildcards(wc).build();
        salMatch = convert(match, datapathIdConvertorData).build();
        etherMatch = salMatch.getEthernetMatch();
        ipMatch = salMatch.getIpMatch();
        icmpv4Match = salMatch.getIcmpv4Match();
        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl src",
                            null, etherMatch.getEthernetSource());
        Assert.assertEquals("Wrong dl dst",
                            null, etherMatch.getEthernetDestination());
        Assert.assertEquals("Wrong dl type", dlType,
                            etherMatch.getEthernetType().getType().getValue());
        Assert.assertEquals("Wrong VLAN match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong ip protocol",
                            ipProto, ipMatch.getIpProtocol());
        Assert.assertEquals("Wrong ip proto", null, ipMatch.getIpProto());
        Assert.assertEquals("Wrong ip ecn", null, ipMatch.getIpEcn());
        Assert.assertEquals("Wrong ip dscp", null, ipMatch.getIpDscp());
        Assert.assertEquals("Wrong L3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong L4 match", null, salMatch.getLayer4Match());
        Assert.assertEquals("Wrong ICMPv4 type",
                            icmpType, icmpv4Match.getIcmpv4Type());
        Assert.assertEquals("Wrong ICMPv4 code",
                            null, icmpv4Match.getIcmpv4Code());

        // Specify ICMPv4 code only.
        Short icmpCode = 33;
        match = builder.setTpSrc(null).setTpDst(icmpCode.intValue()).build();
        salMatch = convert(match, datapathIdConvertorData).build();
        etherMatch = salMatch.getEthernetMatch();
        ipMatch = salMatch.getIpMatch();
        icmpv4Match = salMatch.getIcmpv4Match();
        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl src",
                            null, etherMatch.getEthernetSource());
        Assert.assertEquals("Wrong dl dst",
                            null, etherMatch.getEthernetDestination());
        Assert.assertEquals("Wrong dl type", dlType,
                            etherMatch.getEthernetType().getType().getValue());
        Assert.assertEquals("Wrong VLAN match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong ip protocol",
                            ipProto, ipMatch.getIpProtocol());
        Assert.assertEquals("Wrong ip proto", null, ipMatch.getIpProto());
        Assert.assertEquals("Wrong ip ecn", null, ipMatch.getIpEcn());
        Assert.assertEquals("Wrong ip dscp", null, ipMatch.getIpDscp());
        Assert.assertEquals("Wrong L3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong L4 match", null, salMatch.getLayer4Match());
        Assert.assertEquals("Wrong ICMPv4 type",
                            null, icmpv4Match.getIcmpv4Type());
        Assert.assertEquals("Wrong ICMPv4 code",
                            icmpCode, icmpv4Match.getIcmpv4Code());

        // Specify both ICMPv4 type and code.
        icmpType = 0;
        icmpCode = 8;
        match = builder.setTpSrc(icmpType.intValue()).
            setTpDst(icmpCode.intValue()).build();
        salMatch = convert(match, datapathIdConvertorData).build();
        etherMatch = salMatch.getEthernetMatch();
        ipMatch = salMatch.getIpMatch();
        icmpv4Match = salMatch.getIcmpv4Match();
        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl src",
                            null, etherMatch.getEthernetSource());
        Assert.assertEquals("Wrong dl dst",
                            null, etherMatch.getEthernetDestination());
        Assert.assertEquals("Wrong dl type", dlType,
                            etherMatch.getEthernetType().getType().getValue());
        Assert.assertEquals("Wrong VLAN match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong ip protocol",
                            ipProto, ipMatch.getIpProtocol());
        Assert.assertEquals("Wrong ip proto", null, ipMatch.getIpProto());
        Assert.assertEquals("Wrong ip ecn", null, ipMatch.getIpEcn());
        Assert.assertEquals("Wrong ip dscp", null, ipMatch.getIpDscp());
        Assert.assertEquals("Wrong L3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong L4 match", null, salMatch.getLayer4Match());
        Assert.assertEquals("Wrong ICMPv4 type",
                            icmpType, icmpv4Match.getIcmpv4Type());
        Assert.assertEquals("Wrong ICMPv4 code",
                            icmpCode, icmpv4Match.getIcmpv4Code());
    }

    /**
     * TCP match test for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchV10ResponseConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData)}
     */
    @Test
    public void testTcpMatch() {
        // TP_SRC, TP_DST are wildcarded.
        // NW_PROTO is not wildcarded but null.
        Long dlType = 0x800L;
        FlowWildcardsV10 wc = new FlowWildcardsV10(
            true, true, false, true, true, true, false, true, true, true);
        MatchV10Builder builder = new MatchV10Builder().
            setWildcards(wc).
            setDlType(dlType.intValue());
        MatchV10 match = builder.build();

        BigInteger dpid = BigInteger.valueOf(12345L);
        final VersionDatapathIdConvertorData datapathIdConvertorData = new VersionDatapathIdConvertorData(OFConstants.OFP_VERSION_1_0);
        datapathIdConvertorData.setDatapathId(dpid);

        Match salMatch = convert(match, datapathIdConvertorData).build();
        EthernetMatch etherMatch = salMatch.getEthernetMatch();
        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl src",
                            null, etherMatch.getEthernetSource());
        Assert.assertEquals("Wrong dl dst",
                            null, etherMatch.getEthernetDestination());
        Assert.assertEquals("Wrong dl type", dlType,
                            etherMatch.getEthernetType().getType().getValue());
        Assert.assertEquals("Wrong VLAN match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong IP match", null, salMatch.getIpMatch());
        Assert.assertEquals("Wrong L3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong L4 match", null, salMatch.getLayer4Match());
        Assert.assertEquals("Wrong ICMPv4 match",
                            null, salMatch.getIcmpv4Match());

        // Specify TCP protocol.
        Short ipProto = 6;
        match = builder.setNwProto(ipProto).build();
        salMatch = convert(match, datapathIdConvertorData).build();
        etherMatch = salMatch.getEthernetMatch();
        IpMatch ipMatch = salMatch.getIpMatch();
        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl src",
                            null, etherMatch.getEthernetSource());
        Assert.assertEquals("Wrong dl dst",
                            null, etherMatch.getEthernetDestination());
        Assert.assertEquals("Wrong dl type", dlType,
                            etherMatch.getEthernetType().getType().getValue());
        Assert.assertEquals("Wrong VLAN match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong ip protocol",
                            ipProto, ipMatch.getIpProtocol());
        Assert.assertEquals("Wrong ip proto", null, ipMatch.getIpProto());
        Assert.assertEquals("Wrong ip ecn", null, ipMatch.getIpEcn());
        Assert.assertEquals("Wrong ip dscp", null, ipMatch.getIpDscp());
        Assert.assertEquals("Wrong L3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong L4 match", null, salMatch.getLayer4Match());
        Assert.assertEquals("Wrong ICMPv4 match",
                            null, salMatch.getIcmpv4Match());

        // TP_SRC is not wildcarded but null.
        wc = new FlowWildcardsV10(
            true, true, false, true, true, true, false, true, true, false);
        match = builder.setWildcards(wc).build();
        salMatch = convert(match, datapathIdConvertorData).build();
        etherMatch = salMatch.getEthernetMatch();
        ipMatch = salMatch.getIpMatch();
        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl src",
                            null, etherMatch.getEthernetSource());
        Assert.assertEquals("Wrong dl dst",
                            null, etherMatch.getEthernetDestination());
        Assert.assertEquals("Wrong dl type", dlType,
                            etherMatch.getEthernetType().getType().getValue());
        Assert.assertEquals("Wrong VLAN match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong ip protocol",
                            ipProto, ipMatch.getIpProtocol());
        Assert.assertEquals("Wrong ip proto", null, ipMatch.getIpProto());
        Assert.assertEquals("Wrong ip ecn", null, ipMatch.getIpEcn());
        Assert.assertEquals("Wrong ip dscp", null, ipMatch.getIpDscp());
        Assert.assertEquals("Wrong L3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong L4 match", null, salMatch.getLayer4Match());
        Assert.assertEquals("Wrong ICMPv4 match",
                            null, salMatch.getIcmpv4Match());

        // Specify TCP source port.
        Integer srcPort = 60000;
        match = builder.setTpSrc(srcPort).build();
        salMatch = convert(match, datapathIdConvertorData).build();
        etherMatch = salMatch.getEthernetMatch();
        ipMatch = salMatch.getIpMatch();
        TcpMatch tcpMatch = (TcpMatch)salMatch.getLayer4Match();
        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl src",
                            null, etherMatch.getEthernetSource());
        Assert.assertEquals("Wrong dl dst",
                            null, etherMatch.getEthernetDestination());
        Assert.assertEquals("Wrong dl type", dlType,
                            etherMatch.getEthernetType().getType().getValue());
        Assert.assertEquals("Wrong VLAN match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong ip protocol",
                            ipProto, ipMatch.getIpProtocol());
        Assert.assertEquals("Wrong ip proto", null, ipMatch.getIpProto());
        Assert.assertEquals("Wrong ip ecn", null, ipMatch.getIpEcn());
        Assert.assertEquals("Wrong ip dscp", null, ipMatch.getIpDscp());
        Assert.assertEquals("Wrong L3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong TCP src", srcPort,
                            tcpMatch.getTcpSourcePort().getValue());
        Assert.assertEquals("Wrong TCP dst", null,
                            tcpMatch.getTcpDestinationPort());
        Assert.assertEquals("Wrong ICMPv4 match",
                            null, salMatch.getIcmpv4Match());

        // TP_DST is not wildcarded but null.
        wc = new FlowWildcardsV10(
            true, true, false, true, true, true, false, true, false, false);
        match = builder.setWildcards(wc).build();
        salMatch = convert(match, datapathIdConvertorData).build();
        etherMatch = salMatch.getEthernetMatch();
        ipMatch = salMatch.getIpMatch();
        tcpMatch = (TcpMatch)salMatch.getLayer4Match();
        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl src",
                            null, etherMatch.getEthernetSource());
        Assert.assertEquals("Wrong dl dst",
                            null, etherMatch.getEthernetDestination());
        Assert.assertEquals("Wrong dl type", dlType,
                            etherMatch.getEthernetType().getType().getValue());
        Assert.assertEquals("Wrong VLAN match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong ip protocol",
                            ipProto, ipMatch.getIpProtocol());
        Assert.assertEquals("Wrong ip proto", null, ipMatch.getIpProto());
        Assert.assertEquals("Wrong ip ecn", null, ipMatch.getIpEcn());
        Assert.assertEquals("Wrong ip dscp", null, ipMatch.getIpDscp());
        Assert.assertEquals("Wrong L3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong TCP src", srcPort,
                            tcpMatch.getTcpSourcePort().getValue());
        Assert.assertEquals("Wrong TCP dst", null,
                            tcpMatch.getTcpDestinationPort());
        Assert.assertEquals("Wrong ICMPv4 match",
                            null, salMatch.getIcmpv4Match());

        // Specify TCP destination port only.
        Integer dstPort = 6653;
        match = builder.setTpSrc(null).setTpDst(dstPort).build();
        salMatch = convert(match, datapathIdConvertorData).build();
        etherMatch = salMatch.getEthernetMatch();
        ipMatch = salMatch.getIpMatch();
        tcpMatch = (TcpMatch)salMatch.getLayer4Match();
        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl src",
                            null, etherMatch.getEthernetSource());
        Assert.assertEquals("Wrong dl dst",
                            null, etherMatch.getEthernetDestination());
        Assert.assertEquals("Wrong dl type", dlType,
                            etherMatch.getEthernetType().getType().getValue());
        Assert.assertEquals("Wrong VLAN match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong ip protocol",
                            ipProto, ipMatch.getIpProtocol());
        Assert.assertEquals("Wrong ip proto", null, ipMatch.getIpProto());
        Assert.assertEquals("Wrong ip ecn", null, ipMatch.getIpEcn());
        Assert.assertEquals("Wrong ip dscp", null, ipMatch.getIpDscp());
        Assert.assertEquals("Wrong L3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong TCP src", null,
                            tcpMatch.getTcpSourcePort());
        Assert.assertEquals("Wrong TCP dst", dstPort,
                            tcpMatch.getTcpDestinationPort().getValue());
        Assert.assertEquals("Wrong ICMPv4 match",
                            null, salMatch.getIcmpv4Match());

        // Specify both source and destination port.
        srcPort = 32767;
        dstPort = 9999;
        match = builder.setTpSrc(srcPort).setTpDst(dstPort).build();
        salMatch = convert(match, datapathIdConvertorData).build();
        etherMatch = salMatch.getEthernetMatch();
        ipMatch = salMatch.getIpMatch();
        tcpMatch = (TcpMatch)salMatch.getLayer4Match();
        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl src",
                            null, etherMatch.getEthernetSource());
        Assert.assertEquals("Wrong dl dst",
                            null, etherMatch.getEthernetDestination());
        Assert.assertEquals("Wrong dl type", dlType,
                            etherMatch.getEthernetType().getType().getValue());
        Assert.assertEquals("Wrong VLAN match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong ip protocol",
                            ipProto, ipMatch.getIpProtocol());
        Assert.assertEquals("Wrong ip proto", null, ipMatch.getIpProto());
        Assert.assertEquals("Wrong ip ecn", null, ipMatch.getIpEcn());
        Assert.assertEquals("Wrong ip dscp", null, ipMatch.getIpDscp());
        Assert.assertEquals("Wrong L3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong TCP src", srcPort,
                            tcpMatch.getTcpSourcePort().getValue());
        Assert.assertEquals("Wrong TCP dst", dstPort,
                            tcpMatch.getTcpDestinationPort().getValue());
        Assert.assertEquals("Wrong ICMPv4 match",
                            null, salMatch.getIcmpv4Match());
    }

    /**
     * UDP match test for {@link org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchV10ResponseConvertor#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10, org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData)}
     */
    @Test
    public void testUdpMatch() {
        // TP_SRC, TP_DST are wildcarded.
        // NW_PROTO is not wildcarded but null.
        Long dlType = 0x800L;
        FlowWildcardsV10 wc = new FlowWildcardsV10(
            true, true, false, true, true, true, false, true, true, true);
        MatchV10Builder builder = new MatchV10Builder().
            setWildcards(wc).
            setDlType(dlType.intValue());
        MatchV10 match = builder.build();

        BigInteger dpid = BigInteger.valueOf(12345L);
        final VersionDatapathIdConvertorData datapathIdConvertorData = new VersionDatapathIdConvertorData(OFConstants.OFP_VERSION_1_0);
        datapathIdConvertorData.setDatapathId(dpid);

        Match salMatch = convert(match, datapathIdConvertorData).build();
        EthernetMatch etherMatch = salMatch.getEthernetMatch();
        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl src",
                            null, etherMatch.getEthernetSource());
        Assert.assertEquals("Wrong dl dst",
                            null, etherMatch.getEthernetDestination());
        Assert.assertEquals("Wrong dl type", dlType,
                            etherMatch.getEthernetType().getType().getValue());
        Assert.assertEquals("Wrong VLAN match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong IP match", null, salMatch.getIpMatch());
        Assert.assertEquals("Wrong L3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong L4 match", null, salMatch.getLayer4Match());
        Assert.assertEquals("Wrong ICMPv4 match",
                            null, salMatch.getIcmpv4Match());

        // Specify UDP protocol.
        Short ipProto = 17;
        match = builder.setNwProto(ipProto).build();
        salMatch = convert(match, datapathIdConvertorData).build();
        etherMatch = salMatch.getEthernetMatch();
        IpMatch ipMatch = salMatch.getIpMatch();
        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl src",
                            null, etherMatch.getEthernetSource());
        Assert.assertEquals("Wrong dl dst",
                            null, etherMatch.getEthernetDestination());
        Assert.assertEquals("Wrong dl type", dlType,
                            etherMatch.getEthernetType().getType().getValue());
        Assert.assertEquals("Wrong VLAN match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong ip protocol",
                            ipProto, ipMatch.getIpProtocol());
        Assert.assertEquals("Wrong ip proto", null, ipMatch.getIpProto());
        Assert.assertEquals("Wrong ip ecn", null, ipMatch.getIpEcn());
        Assert.assertEquals("Wrong ip dscp", null, ipMatch.getIpDscp());
        Assert.assertEquals("Wrong L3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong L4 match", null, salMatch.getLayer4Match());
        Assert.assertEquals("Wrong ICMPv4 match",
                            null, salMatch.getIcmpv4Match());

        // TP_SRC is not wildcarded but null.
        wc = new FlowWildcardsV10(
            true, true, false, true, true, true, false, true, true, false);
        match = builder.setWildcards(wc).build();
        salMatch = convert(match, datapathIdConvertorData).build();
        etherMatch = salMatch.getEthernetMatch();
        ipMatch = salMatch.getIpMatch();
        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl src",
                            null, etherMatch.getEthernetSource());
        Assert.assertEquals("Wrong dl dst",
                            null, etherMatch.getEthernetDestination());
        Assert.assertEquals("Wrong dl type", dlType,
                            etherMatch.getEthernetType().getType().getValue());
        Assert.assertEquals("Wrong VLAN match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong ip protocol",
                            ipProto, ipMatch.getIpProtocol());
        Assert.assertEquals("Wrong ip proto", null, ipMatch.getIpProto());
        Assert.assertEquals("Wrong ip ecn", null, ipMatch.getIpEcn());
        Assert.assertEquals("Wrong ip dscp", null, ipMatch.getIpDscp());
        Assert.assertEquals("Wrong L3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong L4 match", null, salMatch.getLayer4Match());
        Assert.assertEquals("Wrong ICMPv4 match",
                            null, salMatch.getIcmpv4Match());

        // Specify UDP source port.
        Integer srcPort = 60000;
        match = builder.setTpSrc(srcPort).build();
        salMatch = convert(match, datapathIdConvertorData).build();
        etherMatch = salMatch.getEthernetMatch();
        ipMatch = salMatch.getIpMatch();
        UdpMatch udpMatch = (UdpMatch)salMatch.getLayer4Match();
        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl src",
                            null, etherMatch.getEthernetSource());
        Assert.assertEquals("Wrong dl dst",
                            null, etherMatch.getEthernetDestination());
        Assert.assertEquals("Wrong dl type", dlType,
                            etherMatch.getEthernetType().getType().getValue());
        Assert.assertEquals("Wrong VLAN match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong ip protocol",
                            ipProto, ipMatch.getIpProtocol());
        Assert.assertEquals("Wrong ip proto", null, ipMatch.getIpProto());
        Assert.assertEquals("Wrong ip ecn", null, ipMatch.getIpEcn());
        Assert.assertEquals("Wrong ip dscp", null, ipMatch.getIpDscp());
        Assert.assertEquals("Wrong L3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong UDP src", srcPort,
                            udpMatch.getUdpSourcePort().getValue());
        Assert.assertEquals("Wrong UDP dst", null,
                            udpMatch.getUdpDestinationPort());
        Assert.assertEquals("Wrong ICMPv4 match",
                            null, salMatch.getIcmpv4Match());

        // TP_DST is not wildcarded but null.
        wc = new FlowWildcardsV10(
            true, true, false, true, true, true, false, true, false, false);
        match = builder.setWildcards(wc).build();
        salMatch = convert(match, datapathIdConvertorData).build();
        etherMatch = salMatch.getEthernetMatch();
        ipMatch = salMatch.getIpMatch();
        udpMatch = (UdpMatch)salMatch.getLayer4Match();
        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl src",
                            null, etherMatch.getEthernetSource());
        Assert.assertEquals("Wrong dl dst",
                            null, etherMatch.getEthernetDestination());
        Assert.assertEquals("Wrong dl type", dlType,
                            etherMatch.getEthernetType().getType().getValue());
        Assert.assertEquals("Wrong VLAN match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong ip protocol",
                            ipProto, ipMatch.getIpProtocol());
        Assert.assertEquals("Wrong ip proto", null, ipMatch.getIpProto());
        Assert.assertEquals("Wrong ip ecn", null, ipMatch.getIpEcn());
        Assert.assertEquals("Wrong ip dscp", null, ipMatch.getIpDscp());
        Assert.assertEquals("Wrong L3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong UDP src", srcPort,
                            udpMatch.getUdpSourcePort().getValue());
        Assert.assertEquals("Wrong UDP dst", null,
                            udpMatch.getUdpDestinationPort());
        Assert.assertEquals("Wrong ICMPv4 match",
                            null, salMatch.getIcmpv4Match());

        // Specify UDP destination port only.
        Integer dstPort = 6653;
        match = builder.setTpSrc(null).setTpDst(dstPort).build();
        salMatch = convert(match, datapathIdConvertorData).build();
        etherMatch = salMatch.getEthernetMatch();
        ipMatch = salMatch.getIpMatch();
        udpMatch = (UdpMatch)salMatch.getLayer4Match();
        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl src",
                            null, etherMatch.getEthernetSource());
        Assert.assertEquals("Wrong dl dst",
                            null, etherMatch.getEthernetDestination());
        Assert.assertEquals("Wrong dl type", dlType,
                            etherMatch.getEthernetType().getType().getValue());
        Assert.assertEquals("Wrong VLAN match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong ip protocol",
                            ipProto, ipMatch.getIpProtocol());
        Assert.assertEquals("Wrong ip proto", null, ipMatch.getIpProto());
        Assert.assertEquals("Wrong ip ecn", null, ipMatch.getIpEcn());
        Assert.assertEquals("Wrong ip dscp", null, ipMatch.getIpDscp());
        Assert.assertEquals("Wrong L3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong UDP src", null,
                            udpMatch.getUdpSourcePort());
        Assert.assertEquals("Wrong UDP dst", dstPort,
                            udpMatch.getUdpDestinationPort().getValue());
        Assert.assertEquals("Wrong ICMPv4 match",
                            null, salMatch.getIcmpv4Match());

        // Specify both source and destination port.
        srcPort = 32767;
        dstPort = 9999;
        match = builder.setTpSrc(srcPort).setTpDst(dstPort).build();
        salMatch = convert(match, datapathIdConvertorData).build();
        etherMatch = salMatch.getEthernetMatch();
        ipMatch = salMatch.getIpMatch();
        udpMatch = (UdpMatch)salMatch.getLayer4Match();
        Assert.assertEquals("Wrong in port", null, salMatch.getInPort());
        Assert.assertEquals("Wrong dl src",
                            null, etherMatch.getEthernetSource());
        Assert.assertEquals("Wrong dl dst",
                            null, etherMatch.getEthernetDestination());
        Assert.assertEquals("Wrong dl type", dlType,
                            etherMatch.getEthernetType().getType().getValue());
        Assert.assertEquals("Wrong VLAN match", null, salMatch.getVlanMatch());
        Assert.assertEquals("Wrong ip protocol",
                            ipProto, ipMatch.getIpProtocol());
        Assert.assertEquals("Wrong ip proto", null, ipMatch.getIpProto());
        Assert.assertEquals("Wrong ip ecn", null, ipMatch.getIpEcn());
        Assert.assertEquals("Wrong ip dscp", null, ipMatch.getIpDscp());
        Assert.assertEquals("Wrong L3 match", null, salMatch.getLayer3Match());
        Assert.assertEquals("Wrong UDP src", srcPort,
                            udpMatch.getUdpSourcePort().getValue());
        Assert.assertEquals("Wrong UDP dst", dstPort,
                            udpMatch.getUdpDestinationPort().getValue());
        Assert.assertEquals("Wrong ICMPv4 match",
                            null, salMatch.getIcmpv4Match());
    }

    private MatchBuilder convert(MatchV10 match, VersionDatapathIdConvertorData data) {
        final Optional<MatchBuilder> salMatchOptional = ConvertorManager.getInstance().convert(match, data);

        return salMatchOptional.orElse(new MatchBuilder());
    }
}
