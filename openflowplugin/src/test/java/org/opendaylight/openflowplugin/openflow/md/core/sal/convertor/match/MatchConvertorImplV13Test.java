/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6FlowLabel;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchArbitraryBitMask;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.SctpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Ipv6ExthdrFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpOp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpSha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpSpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpTpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv4Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv4Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv6Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv6Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpDscp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpEcn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpProto;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Exthdr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Flabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6NdSll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6NdTarget;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6NdTll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Ipv6Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MplsBos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MplsLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MplsTc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.PbbIsid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.SctpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.SctpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TcpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TcpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TunnelId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.UdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpOpCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpShaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpSpaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpThaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpTpaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthTypeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv4CodeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv4TypeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv6CodeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv6TypeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPhyPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpDscpCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpEcnCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpProtoCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4DstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4SrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6DstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6ExthdrCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6FlabelCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6NdSllCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6NdTargetCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6NdTllCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6SrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MetadataCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MplsBosCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MplsLabelCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MplsTcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.PbbIsidCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.SctpDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.SctpSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TcpDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TcpSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TunnelIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.UdpDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.UdpSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanPcpCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanVidCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.arp.op._case.ArpOpBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.arp.sha._case.ArpShaBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.arp.spa._case.ArpSpaBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.arp.tha._case.ArpThaBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.arp.tpa._case.ArpTpaBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.eth.dst._case.EthDstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.eth.src._case.EthSrcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.eth.type._case.EthTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv4.code._case.Icmpv4CodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv4.type._case.Icmpv4TypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv6.code._case.Icmpv6CodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv6.type._case.Icmpv6TypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.in.phy.port._case.InPhyPortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.in.port._case.InPortBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ip.dscp._case.IpDscpBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ip.ecn._case.IpEcnBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ip.proto._case.IpProtoBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv4.dst._case.Ipv4DstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv4.src._case.Ipv4SrcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.dst._case.Ipv6DstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.exthdr._case.Ipv6ExthdrBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.flabel._case.Ipv6FlabelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.nd.sll._case.Ipv6NdSllBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.nd.target._case.Ipv6NdTargetBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.nd.tll._case.Ipv6NdTllBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.src._case.Ipv6SrcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.mpls.bos._case.MplsBosBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.mpls.label._case.MplsLabelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.mpls.tc._case.MplsTcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.pbb.isid._case.PbbIsidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.sctp.dst._case.SctpDstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.sctp.src._case.SctpSrcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.tcp.dst._case.TcpDstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.tcp.src._case.TcpSrcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.tunnel.id._case.TunnelIdBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.udp.dst._case.UdpDstBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.udp.src._case.UdpSrcBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.vlan.pcp._case.VlanPcpBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.vlan.vid._case.VlanVidBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder;

/**
 * @author michal.polkorab
 */
public class MatchConvertorImplV13Test {

    /**
     * Initializes OpenflowPortsUtil
     */
    @Before
    public void startUp() {
        OpenflowPortsUtil.init();
    }

    /**
     * Test {@link MatchConvertorImpl#fromOFMatchToSALMatch(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match, java.math.BigInteger, org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion)}
     */
    @Test(expected = NullPointerException.class)
    public void testEmptyMatch() {
        final MatchBuilder builder = new MatchBuilder();

        MatchConvertorImpl.fromOFMatchToSALMatch(builder.build(), new BigInteger("42"), OpenflowVersion.OF10);
    }

    /**
     * Test {@link MatchConvertorImpl#fromOFMatchToSALMatch(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match, java.math.BigInteger, org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion)}
     */
    @Test
    public void testEmptyMatchEntry() {

        final MatchBuilder builder = new MatchBuilder();
        builder.setType(OxmMatchType.class);
        final List<MatchEntry> entries = new ArrayList<>();
        builder.setMatchEntry(entries);
        final Match match = builder.build();

        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow
                .MatchBuilder salMatch = MatchConvertorImpl.fromOFMatchToSALMatch(match, new BigInteger("42"), OpenflowVersion.OF10);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match builtMatch = salMatch.build();

        Assert.assertEquals("Wrong match entries", null, builtMatch.getEthernetMatch());
        Assert.assertEquals("Wrong match entries", null, builtMatch.getIcmpv4Match());
        Assert.assertEquals("Wrong match entries", null, builtMatch.getIcmpv6Match());
        Assert.assertEquals("Wrong match entries", null, builtMatch.getInPhyPort());
        Assert.assertEquals("Wrong match entries", null, builtMatch.getInPort());
        Assert.assertEquals("Wrong match entries", null, builtMatch.getIpMatch());
        Assert.assertEquals("Wrong match entries", null, builtMatch.getLayer3Match());
        Assert.assertEquals("Wrong match entries", null, builtMatch.getLayer4Match());
        Assert.assertEquals("Wrong match entries", null, builtMatch.getMetadata());
        Assert.assertEquals("Wrong match entries", null, builtMatch.getProtocolMatchFields());
        Assert.assertEquals("Wrong match entries", null, builtMatch.getVlanMatch());
        Assert.assertEquals("Wrong match entries", null, builtMatch.getTunnel());

    }

    /**
     * Test {@link MatchConvertorImpl#fromOFMatchToSALMatch(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match, java.math.BigInteger, org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion)}
     */
    @Test
    public void testWithMatchEntryNoMasks() {
        final List<MatchEntry> entries = new ArrayList<>();
        MatchEntryBuilder entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPort.class);
        entriesBuilder.setHasMask(false);
        final InPortCaseBuilder caseBuilder = new InPortCaseBuilder();
        final InPortBuilder portBuilder = new InPortBuilder();
        portBuilder.setPortNumber(new PortNumber(1L));
        caseBuilder.setInPort(portBuilder.build());
        entriesBuilder.setMatchEntryValue(caseBuilder.build());
        entries.add(entriesBuilder.build());

        final InPhyPortCaseBuilder inPhyPortCaseBuilder = new InPhyPortCaseBuilder();
        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPhyPort.class);
        entriesBuilder.setHasMask(false);
        final InPhyPortBuilder inPhyPortBuilder = new InPhyPortBuilder();
        inPhyPortBuilder.setPortNumber(new PortNumber(2L));
        inPhyPortCaseBuilder.setInPhyPort(inPhyPortBuilder.build());
        entriesBuilder.setMatchEntryValue(inPhyPortCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Metadata.class);
        entriesBuilder.setHasMask(false);
        final MetadataCaseBuilder metadataCaseBuilder = new MetadataCaseBuilder();
        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.metadata._case.MetadataBuilder metadataBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.metadata._case.MetadataBuilder();
        metadataBuilder.setMetadata(new byte[]{0, 1, 2, 3, 4, 5, 6, 7});
        metadataCaseBuilder.setMetadata(metadataBuilder.build());
        entriesBuilder.setMatchEntryValue(metadataCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(EthDst.class);
        entriesBuilder.setHasMask(false);
        final EthDstCaseBuilder ethDstCaseBuilder = new EthDstCaseBuilder();
        final EthDstBuilder ethDstBuilder = new EthDstBuilder();
        ethDstBuilder.setMacAddress(new MacAddress("00:00:00:00:00:01"));
        ethDstCaseBuilder.setEthDst(ethDstBuilder.build());
        entriesBuilder.setMatchEntryValue(ethDstCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(EthSrc.class);
        entriesBuilder.setHasMask(false);
        final EthSrcCaseBuilder ethSrcCaseBuilder = new EthSrcCaseBuilder();
        final EthSrcBuilder ethSrcBuilder = new EthSrcBuilder();
        ethSrcBuilder.setMacAddress(new MacAddress("00:00:00:00:00:02"));
        ethSrcCaseBuilder.setEthSrc(ethSrcBuilder.build());
        entriesBuilder.setMatchEntryValue(ethSrcCaseBuilder.build());
        entries.add(entriesBuilder.build());


        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(EthType.class);
        entriesBuilder.setHasMask(false);
        final EthTypeCaseBuilder ethTypeCaseBuilder = new EthTypeCaseBuilder();

        final EthTypeBuilder ethTypeBuilder = new EthTypeBuilder();
        ethTypeBuilder.setEthType(new EtherType(3));
        ethTypeCaseBuilder.setEthType(ethTypeBuilder.build());
        entriesBuilder.setMatchEntryValue(ethTypeCaseBuilder.build());
        entries.add(entriesBuilder.build());


        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(VlanVid.class);
        final VlanVidCaseBuilder vlanVidCaseBuilder = new VlanVidCaseBuilder();
        entriesBuilder.setHasMask(false);
        final VlanVidBuilder vlanVidBuilder = new VlanVidBuilder();
        vlanVidBuilder.setVlanVid(4);
        vlanVidBuilder.setCfiBit(true);
        vlanVidCaseBuilder.setVlanVid(vlanVidBuilder.build());
        entriesBuilder.setMatchEntryValue(vlanVidCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(VlanPcp.class);
        final VlanPcpCaseBuilder vlanPcpCaseBuilder = new VlanPcpCaseBuilder();
        entriesBuilder.setHasMask(false);
        final VlanPcpBuilder vlanPcpBuilder = new VlanPcpBuilder();
        vlanPcpBuilder.setVlanPcp((short) 5);
        vlanPcpCaseBuilder.setVlanPcp(vlanPcpBuilder.build());
        entriesBuilder.setMatchEntryValue(vlanPcpCaseBuilder.build());
        entries.add(entriesBuilder.build());


        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(IpDscp.class);
        entriesBuilder.setHasMask(false);
        final IpDscpCaseBuilder ipDscpCaseBuilder = new IpDscpCaseBuilder();
        final IpDscpBuilder ipDscpBuilder = new IpDscpBuilder();
        ipDscpBuilder.setDscp(new Dscp((short) 6));
        ipDscpCaseBuilder.setIpDscp(ipDscpBuilder.build());
        entriesBuilder.setMatchEntryValue(ipDscpCaseBuilder.build());
        entries.add(entriesBuilder.build());


        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(IpEcn.class);
        entriesBuilder.setHasMask(false);
        final IpEcnCaseBuilder ipEcnCaseBuilder = new IpEcnCaseBuilder();
        final IpEcnBuilder ipEcnBuilder = new IpEcnBuilder();
        ipEcnBuilder.setEcn((short) 7);
        ipEcnCaseBuilder.setIpEcn(ipEcnBuilder.build());
        entriesBuilder.setMatchEntryValue(ipEcnCaseBuilder.build());
        entries.add(entriesBuilder.build());


        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(IpProto.class);
        final IpProtoCaseBuilder ipProtoCaseBuilder = new IpProtoCaseBuilder();
        entriesBuilder.setHasMask(false);
        final IpProtoBuilder ipProtoBuilder = new IpProtoBuilder();
        ipProtoBuilder.setProtocolNumber((short) 8);
        ipProtoCaseBuilder.setIpProto(ipProtoBuilder.build());
        entriesBuilder.setMatchEntryValue(ipProtoCaseBuilder.build());
        entries.add(entriesBuilder.build());


        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv4Src.class);
        entriesBuilder.setHasMask(false);
        final Ipv4SrcCaseBuilder ipv4AddressBuilder = new Ipv4SrcCaseBuilder();
        final Ipv4SrcBuilder ipv4SrcBuilder = new Ipv4SrcBuilder();
        ipv4SrcBuilder.setIpv4Address(new Ipv4Address("10.0.0.1"));
        ipv4AddressBuilder.setIpv4Src(ipv4SrcBuilder.build());
        entriesBuilder.setMatchEntryValue(ipv4AddressBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv4Dst.class);
        entriesBuilder.setHasMask(false);
        final Ipv4DstCaseBuilder ipv4DstCaseBuilder = new Ipv4DstCaseBuilder();
        final Ipv4DstBuilder ipv4DstBuilder = new Ipv4DstBuilder();
        ipv4DstBuilder.setIpv4Address(new Ipv4Address("10.0.0.2"));
        ipv4DstCaseBuilder.setIpv4Dst(ipv4DstBuilder.build());
        entriesBuilder.setMatchEntryValue(ipv4DstCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(TcpSrc.class);
        entriesBuilder.setHasMask(false);
        final TcpSrcCaseBuilder tcpSrcCaseBuilder = new TcpSrcCaseBuilder();
        final TcpSrcBuilder tcpSrcBuilder = new TcpSrcBuilder();
        tcpSrcBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
                .inet.types.rev100924.PortNumber(9));
        tcpSrcCaseBuilder.setTcpSrc(tcpSrcBuilder.build());
        entriesBuilder.setMatchEntryValue(tcpSrcCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(TcpDst.class);
        entriesBuilder.setHasMask(false);
        final TcpDstCaseBuilder tcpDstCaseBuilder = new TcpDstCaseBuilder();
        final TcpDstBuilder tcpDstBuilder = new TcpDstBuilder();
        tcpDstBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
                .inet.types.rev100924.PortNumber(10));
        tcpDstCaseBuilder.setTcpDst(tcpDstBuilder.build());
        entriesBuilder.setMatchEntryValue(tcpDstCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Icmpv4Type.class);
        entriesBuilder.setHasMask(false);
        final Icmpv4TypeCaseBuilder icmpv4TypeCaseBuilder = new Icmpv4TypeCaseBuilder();
        final Icmpv4TypeBuilder icmpv4TypeBuilder = new Icmpv4TypeBuilder();
        icmpv4TypeBuilder.setIcmpv4Type((short) 15);
        icmpv4TypeCaseBuilder.setIcmpv4Type(icmpv4TypeBuilder.build());
        entriesBuilder.setMatchEntryValue(icmpv4TypeCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Icmpv4Code.class);
        entriesBuilder.setHasMask(false);
        final Icmpv4CodeCaseBuilder icmpv4CodeCaseBuilder = new Icmpv4CodeCaseBuilder();
        final Icmpv4CodeBuilder icmpv4CodeBuilder = new Icmpv4CodeBuilder();
        icmpv4CodeBuilder.setIcmpv4Code((short) 16);
        icmpv4CodeCaseBuilder.setIcmpv4Code(icmpv4CodeBuilder.build());
        entriesBuilder.setMatchEntryValue(icmpv4CodeCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Icmpv6Type.class);
        entriesBuilder.setHasMask(false);
        final Icmpv6TypeCaseBuilder icmpv6TypeCaseBuilder = new Icmpv6TypeCaseBuilder();
        final Icmpv6TypeBuilder icmpv6TypeBuilder = new Icmpv6TypeBuilder();
        icmpv6TypeBuilder.setIcmpv6Type((short) 19);
        icmpv6TypeCaseBuilder.setIcmpv6Type(icmpv6TypeBuilder.build());
        entriesBuilder.setMatchEntryValue(icmpv6TypeCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Icmpv6Code.class);
        entriesBuilder.setHasMask(false);
        final Icmpv6CodeCaseBuilder icmpv6CodeCaseBuilder = new Icmpv6CodeCaseBuilder();
        final Icmpv6CodeBuilder icmpv6CodeBuilder = new Icmpv6CodeBuilder();
        icmpv6CodeBuilder.setIcmpv6Code((short) 20);
        icmpv6CodeCaseBuilder.setIcmpv6Code(icmpv6CodeBuilder.build());
        entriesBuilder.setMatchEntryValue(icmpv6CodeCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(MplsLabel.class);
        entriesBuilder.setHasMask(false);
        final MplsLabelCaseBuilder mplsLabelCaseBuilder = new MplsLabelCaseBuilder();

        final MplsLabelBuilder mplsLabelBuilder = new MplsLabelBuilder();
        mplsLabelBuilder.setMplsLabel(21L);
        mplsLabelCaseBuilder.setMplsLabel(mplsLabelBuilder.build());
        entriesBuilder.setMatchEntryValue(mplsLabelCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(MplsTc.class);
        entriesBuilder.setHasMask(false);
        final MplsTcCaseBuilder mplsTcCaseBuilder = new MplsTcCaseBuilder();
        final MplsTcBuilder mplsTcBuilder = new MplsTcBuilder();
        mplsTcBuilder.setTc((short) 22);
        mplsTcCaseBuilder.setMplsTc(mplsTcBuilder.build());
        entriesBuilder.setMatchEntryValue(mplsTcCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(MplsBos.class);
        entriesBuilder.setHasMask(false);
        final MplsBosCaseBuilder mplsBosCaseBuilder = new MplsBosCaseBuilder();
        final MplsBosBuilder mplsBosBuilder = new MplsBosBuilder();
        mplsBosBuilder.setBos(true);
        mplsBosCaseBuilder.setMplsBos(mplsBosBuilder.build());
        entriesBuilder.setMatchEntryValue(mplsBosCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(PbbIsid.class);
        entriesBuilder.setHasMask(false);
        final PbbIsidCaseBuilder pbbIsidCaseBuilder = new PbbIsidCaseBuilder();
        final PbbIsidBuilder pbbIsidBuilder = new PbbIsidBuilder();
        pbbIsidBuilder.setIsid(23L);
        pbbIsidCaseBuilder.setPbbIsid(pbbIsidBuilder.build());
        entriesBuilder.setMatchEntryValue(pbbIsidCaseBuilder.build());

        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(TunnelId.class);
        entriesBuilder.setHasMask(false);
        final TunnelIdCaseBuilder tunnelIdCaseBuilder = new TunnelIdCaseBuilder();
        final TunnelIdBuilder tunnelIdBuilder = new TunnelIdBuilder();
        tunnelIdBuilder.setTunnelId(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
        tunnelIdCaseBuilder.setTunnelId(tunnelIdBuilder.build());
        entriesBuilder.setMatchEntryValue(tunnelIdCaseBuilder.build());
        entries.add(entriesBuilder.build());

        final MatchBuilder builder = new MatchBuilder();
        builder.setMatchEntry(entries);

        final Match match = builder.build();

        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow
                .MatchBuilder salMatch = MatchConvertorImpl.fromOFMatchToSALMatch(match, new BigInteger("42"), OpenflowVersion.OF10);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match builtMatch = salMatch.build();

        Assert.assertEquals("Wrong in port", "openflow:42:1", builtMatch.getInPort().getValue());
        Assert.assertEquals("Wrong in phy port", "openflow:42:2", builtMatch.getInPhyPort().getValue());
        Assert.assertEquals("Wrong metadata", new BigInteger(1, new byte[]{0, 1, 2, 3, 4, 5, 6, 7}), builtMatch.getMetadata().getMetadata());
        Assert.assertEquals("Wrong eth dst", new MacAddress("00:00:00:00:00:01"), builtMatch.getEthernetMatch().getEthernetDestination().getAddress());
        Assert.assertEquals("Wrong eth src", new MacAddress("00:00:00:00:00:02"), builtMatch.getEthernetMatch().getEthernetSource().getAddress());
        Assert.assertEquals("Wrong eth type", 3, builtMatch.getEthernetMatch().getEthernetType().getType().getValue().intValue());
        Assert.assertEquals("Wrong vlan id", 4, builtMatch.getVlanMatch().getVlanId().getVlanId().getValue().intValue());
        Assert.assertEquals("Wrong vlan id entries", true, builtMatch.getVlanMatch().getVlanId().isVlanIdPresent());
        Assert.assertEquals("Wrong vlan pcp", 5, builtMatch.getVlanMatch().getVlanPcp().getValue().intValue());
        Assert.assertEquals("Wrong ip dscp", 6, builtMatch.getIpMatch().getIpDscp().getValue().intValue());
        Assert.assertEquals("Wrong ip ecn", 7, builtMatch.getIpMatch().getIpEcn().intValue());
        Assert.assertEquals("Wrong ip proto", null, builtMatch.getIpMatch().getIpProto());
        Assert.assertEquals("Wrong ip protocol", 8, builtMatch.getIpMatch().getIpProtocol().intValue());

        final TcpMatch tcpMatch = (TcpMatch) builtMatch.getLayer4Match();
        Assert.assertEquals("Wrong tcp src port", 9, tcpMatch.getTcpSourcePort().getValue().intValue());
        Assert.assertEquals("Wrong tcp dst port", 10, tcpMatch.getTcpDestinationPort().getValue().intValue());
        Assert.assertEquals("Wrong icmpv4 type", 15, builtMatch.getIcmpv4Match().getIcmpv4Type().intValue());
        Assert.assertEquals("Wrong icmpv4 code", 16, builtMatch.getIcmpv4Match().getIcmpv4Code().intValue());
        Assert.assertEquals("Wrong icmpv6 type", 19, builtMatch.getIcmpv6Match().getIcmpv6Type().intValue());
        Assert.assertEquals("Wrong icmpv6 code", 20, builtMatch.getIcmpv6Match().getIcmpv6Code().intValue());

        final Ipv4Match ipv4Match = (Ipv4Match) builtMatch.getLayer3Match();
        Assert.assertEquals("Wrong ipv4 src address", "10.0.0.1/32", ipv4Match.getIpv4Source().getValue());
        Assert.assertEquals("Wrong ipv4 dst address", "10.0.0.2/32", ipv4Match.getIpv4Destination().getValue());
        Assert.assertEquals("Wrong mpls label", 21, builtMatch.getProtocolMatchFields().getMplsLabel().intValue());
        Assert.assertEquals("Wrong mpls bos", 1, builtMatch.getProtocolMatchFields().getMplsBos().intValue());
        Assert.assertEquals("Wrong mpls tc", 22, builtMatch.getProtocolMatchFields().getMplsTc().intValue());
        Assert.assertEquals("Wrong pbb isid", 23, builtMatch.getProtocolMatchFields().getPbb().getPbbIsid().intValue());
        Assert.assertEquals("Wrong tunnel id", new BigInteger(1, new byte[]{1, 2, 3, 4, 5, 6, 7, 8}),
                builtMatch.getTunnel().getTunnelId());
    }

    /**
     * Test {@link MatchConvertorImpl#fromOFMatchToSALMatch(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match, java.math.BigInteger, org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion)}
     */
    @Test
    public void testWithMatchEntryWithMasks() {
        final MatchBuilder builder = new MatchBuilder();
        builder.setType(OxmMatchType.class);
        final List<MatchEntry> entries = new ArrayList<>();
        MatchEntryBuilder entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Metadata.class);
        entriesBuilder.setHasMask(true);


        final MetadataCaseBuilder metadataCaseBuilder = new MetadataCaseBuilder();
        final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.metadata._case.MetadataBuilder metadataBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.metadata._case.MetadataBuilder();
        metadataBuilder.setMetadata(new byte[]{0, 1, 2, 3, 4, 5, 6, 7});
        metadataBuilder.setMask(new byte[]{0, 0, 0, 0, 0, 0, 0, 1});
        metadataCaseBuilder.setMetadata(metadataBuilder.build());
        entriesBuilder.setMatchEntryValue(metadataCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(EthDst.class);
        entriesBuilder.setHasMask(true);
        final EthDstCaseBuilder ethDstCaseBuilder = new EthDstCaseBuilder();
        final EthDstBuilder ethDstBuilder = new EthDstBuilder();
        ethDstBuilder.setMacAddress(new MacAddress("00:00:00:00:00:01"));
        ethDstBuilder.setMask(new byte[]{0, 0, 0, 0, 1, 1});
        ethDstCaseBuilder.setEthDst(ethDstBuilder.build());
        entriesBuilder.setMatchEntryValue(ethDstCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(EthSrc.class);
        entriesBuilder.setHasMask(true);
        final EthSrcCaseBuilder ethSrcCaseBuilder = new EthSrcCaseBuilder();
        final EthSrcBuilder ethSrcBuilder = new EthSrcBuilder();
        ethSrcBuilder.setMacAddress(new MacAddress("00:00:00:00:00:02"));
        ethSrcBuilder.setMask(new byte[]{0, 0, 0, 0, 2, 2});
        ethSrcCaseBuilder.setEthSrc(ethSrcBuilder.build());
        entriesBuilder.setMatchEntryValue(ethSrcCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(VlanVid.class);
        entriesBuilder.setHasMask(true);
        final VlanVidCaseBuilder vlanVidCaseBuilder = new VlanVidCaseBuilder();
        final VlanVidBuilder vlanVidBuilder = new VlanVidBuilder();
        vlanVidBuilder.setVlanVid(4);
        vlanVidBuilder.setCfiBit(true);
        vlanVidBuilder.setMask(new byte[]{0, 4});
        vlanVidCaseBuilder.setVlanVid(vlanVidBuilder.build());
        entriesBuilder.setMatchEntryValue(vlanVidCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv4Src.class);
        entriesBuilder.setHasMask(true);
        final Ipv4SrcCaseBuilder ipv4SrcCaseBuilder = new Ipv4SrcCaseBuilder();
        final Ipv4SrcBuilder ipv4SrcBuilder = new Ipv4SrcBuilder();
        ipv4SrcBuilder.setIpv4Address(new Ipv4Address("10.0.0.0"));
        ipv4SrcBuilder.setMask(new byte[]{(byte) 255, (byte) 255, (byte) 255, 0});
        ipv4SrcCaseBuilder.setIpv4Src(ipv4SrcBuilder.build());
        entriesBuilder.setMatchEntryValue(ipv4SrcCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv4Dst.class);
        entriesBuilder.setHasMask(true);
        final Ipv4DstCaseBuilder ipv4DstCaseBuilder = new Ipv4DstCaseBuilder();
        final Ipv4DstBuilder ipv4AddressBuilder = new Ipv4DstBuilder();
        ipv4AddressBuilder.setIpv4Address(new Ipv4Address("10.0.0.0"));
        ipv4AddressBuilder.setMask(new byte[]{(byte) 255, (byte) 255, (byte) 240, 0});
        ipv4DstCaseBuilder.setIpv4Dst(ipv4AddressBuilder.build());
        entriesBuilder.setMatchEntryValue(ipv4DstCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(PbbIsid.class);
        entriesBuilder.setHasMask(true);
        final PbbIsidCaseBuilder pbbIsidCaseBuilder = new PbbIsidCaseBuilder();

        final PbbIsidBuilder pbbIsidBuilder = new PbbIsidBuilder();
        pbbIsidBuilder.setIsid(23L);
        pbbIsidBuilder.setMask(new byte[]{0, 0, 7});
        pbbIsidCaseBuilder.setPbbIsid(pbbIsidBuilder.build());
        entriesBuilder.setMatchEntryValue(pbbIsidCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(TunnelId.class);
        entriesBuilder.setHasMask(true);
        final TunnelIdCaseBuilder tunnelIdCaseBuilder = new TunnelIdCaseBuilder();
        final TunnelIdBuilder tunnelIdBuilder = new TunnelIdBuilder();
        tunnelIdBuilder.setTunnelId(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
        tunnelIdBuilder.setMask(new byte[]{0, 0, 0, 0, 0, 0, 0, 8});
        tunnelIdCaseBuilder.setTunnelId(tunnelIdBuilder.build());
        entriesBuilder.setMatchEntryValue(tunnelIdCaseBuilder.build());
        entries.add(entriesBuilder.build());
        builder.setMatchEntry(entries);
        final Match match = builder.build();

        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow
                .MatchBuilder salMatch = MatchConvertorImpl.fromOFMatchToSALMatch(match, new BigInteger("42"), OpenflowVersion.OF10);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match builtMatch = salMatch.build();

        Assert.assertEquals("Wrong metadata", new BigInteger(1, new byte[]{0, 1, 2, 3, 4, 5, 6, 7}), builtMatch.getMetadata().getMetadata());
        Assert.assertEquals("Wrong metadata mask", new BigInteger(1, new byte[]{0, 0, 0, 0, 0, 0, 0, 1}),
                builtMatch.getMetadata().getMetadataMask());
        Assert.assertEquals("Wrong eth dst", new MacAddress("00:00:00:00:00:01"), builtMatch.getEthernetMatch().getEthernetDestination().getAddress());
        Assert.assertEquals("Wrong eth dst mask", new MacAddress("00:00:00:00:01:01"), builtMatch.getEthernetMatch().getEthernetDestination().getMask());
        Assert.assertEquals("Wrong eth src", new MacAddress("00:00:00:00:00:02"), builtMatch.getEthernetMatch().getEthernetSource().getAddress());
        Assert.assertEquals("Wrong eth src mask", new MacAddress("00:00:00:00:02:02"), builtMatch.getEthernetMatch().getEthernetSource().getMask());
        Assert.assertEquals("Wrong vlan id", 4, builtMatch.getVlanMatch().getVlanId().getVlanId().getValue().intValue());
        Assert.assertEquals("Wrong vlan id entries", true, builtMatch.getVlanMatch().getVlanId().isVlanIdPresent());
        final Ipv4Match ipv4Match = (Ipv4Match) builtMatch.getLayer3Match();
        Assert.assertEquals("Wrong ipv4 src address", "10.0.0.0/24", ipv4Match.getIpv4Source().getValue());
        Assert.assertEquals("Wrong ipv4 dst address", "10.0.0.0/20", ipv4Match.getIpv4Destination().getValue());
        Assert.assertEquals("Wrong pbb isid", 23, builtMatch.getProtocolMatchFields().getPbb().getPbbIsid().intValue());
        Assert.assertEquals("Wrong tunnel id", new BigInteger(1, new byte[]{1, 2, 3, 4, 5, 6, 7, 8}),
                builtMatch.getTunnel().getTunnelId());
    }

    /**
     * Test {@link MatchConvertorImpl#fromOFMatchToSALMatch(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match, java.math.BigInteger, org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion)}
     */
    @Test
    public void testWithMatchEntryWithArbitraryMasks() {
        final MatchBuilder builder = new MatchBuilder();
        builder.setType(OxmMatchType.class);
        final List<MatchEntry> entries = new ArrayList<>();
        MatchEntryBuilder entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Metadata.class);
        entriesBuilder.setHasMask(true);

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv4Src.class);
        entriesBuilder.setHasMask(true);
        final Ipv4SrcCaseBuilder ipv4SrcCaseBuilder = new Ipv4SrcCaseBuilder();
        final Ipv4SrcBuilder ipv4SrcBuilder = new Ipv4SrcBuilder();
        ipv4SrcBuilder.setIpv4Address(new Ipv4Address("10.1.1.1"));
        ipv4SrcBuilder.setMask(new byte[]{(byte) 255, 0, (byte) 255, 0});
        ipv4SrcCaseBuilder.setIpv4Src(ipv4SrcBuilder.build());
        entriesBuilder.setMatchEntryValue(ipv4SrcCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv4Dst.class);
        entriesBuilder.setHasMask(true);
        final Ipv4DstCaseBuilder ipv4DstCaseBuilder = new Ipv4DstCaseBuilder();
        final Ipv4DstBuilder ipv4AddressBuilder = new Ipv4DstBuilder();
        ipv4AddressBuilder.setIpv4Address(new Ipv4Address("10.0.1.1"));
        ipv4AddressBuilder.setMask(new byte[]{(byte) 255, 0, (byte) 240, 0});
        ipv4DstCaseBuilder.setIpv4Dst(ipv4AddressBuilder.build());
        entriesBuilder.setMatchEntryValue(ipv4DstCaseBuilder.build());
        entries.add(entriesBuilder.build());

        builder.setMatchEntry(entries);
        final Match match = builder.build();

        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow
                .MatchBuilder salMatch = MatchConvertorImpl.fromOFMatchToSALMatch(match, new BigInteger("42"), OpenflowVersion.OF10);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match builtMatch = salMatch.build();

        final Ipv4MatchArbitraryBitMask ipv4MatchArbitraryBitMask = (Ipv4MatchArbitraryBitMask) builtMatch.getLayer3Match();
        Assert.assertEquals("Wrong ipv4 src address", "10.1.1.1", ipv4MatchArbitraryBitMask.getIpv4SourceAddressNoMask().getValue());
        Assert.assertEquals("Wrong ipv4 dst address", "10.0.1.1", ipv4MatchArbitraryBitMask.getIpv4DestinationAddressNoMask().getValue());
    }

    /**
     * Test {@link MatchConvertorImpl#fromOFMatchToSALMatch(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match, java.math.BigInteger, org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion)}
     */
    @Test
    public void testLayer4MatchUdp() {
        final MatchBuilder builder = new MatchBuilder();
        builder.setType(OxmMatchType.class);
        final List<MatchEntry> entries = new ArrayList<>();
        MatchEntryBuilder entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.UdpSrc.class);
        entriesBuilder.setHasMask(false);
        final UdpSrcCaseBuilder udpSrcCaseBuilder = new UdpSrcCaseBuilder();
        final UdpSrcBuilder portBuilder = new UdpSrcBuilder();
        portBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
                .inet.types.rev100924.PortNumber(11));
        udpSrcCaseBuilder.setUdpSrc(portBuilder.build());
        entriesBuilder.setMatchEntryValue(udpSrcCaseBuilder.build());
        entries.add(entriesBuilder.build());


        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(UdpDst.class);
        entriesBuilder.setHasMask(false);
        final UdpDstCaseBuilder udpDstCaseBuilder = new UdpDstCaseBuilder();
        final UdpDstBuilder udpDstBuilder = new UdpDstBuilder();
        udpDstBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
                .inet.types.rev100924.PortNumber(12));
        udpDstCaseBuilder.setUdpDst(udpDstBuilder.build());
        entriesBuilder.setMatchEntryValue(udpDstCaseBuilder.build());
        entries.add(entriesBuilder.build());

        builder.setMatchEntry(entries);

        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow
                .MatchBuilder builtMatch = MatchConvertorImpl.fromOFMatchToSALMatch(builder.build(), new BigInteger("42"), OpenflowVersion.OF13);

        final UdpMatch udpMatch = (UdpMatch) builtMatch.getLayer4Match();
        Assert.assertEquals("Wrong udp src port", 11, udpMatch.getUdpSourcePort().getValue().intValue());
        Assert.assertEquals("Wrong udp dst port", 12, udpMatch.getUdpDestinationPort().getValue().intValue());
    }

    /**
     * Test {@link MatchConvertorImpl#fromOFMatchToSALMatch(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match, java.math.BigInteger, org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion)}
     */
    @Test
    public void testLayer4MatchSctp() {
        final MatchBuilder builder = new MatchBuilder();
        builder.setType(OxmMatchType.class);
        final List<MatchEntry> entries = new ArrayList<>();
        MatchEntryBuilder entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(SctpSrc.class);
        entriesBuilder.setHasMask(false);
        final SctpSrcCaseBuilder sctpSrcCaseBuilder = new SctpSrcCaseBuilder();
        final SctpSrcBuilder portBuilder = new SctpSrcBuilder();
        portBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
                .inet.types.rev100924.PortNumber(13));
        sctpSrcCaseBuilder.setSctpSrc(portBuilder.build());
        entriesBuilder.setMatchEntryValue(sctpSrcCaseBuilder.build());
        entries.add(entriesBuilder.build());


        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(SctpDst.class);
        entriesBuilder.setHasMask(false);
        final SctpDstCaseBuilder sctpDstCaseBuilder = new SctpDstCaseBuilder();
        final SctpDstBuilder sctpDstBuilder = new SctpDstBuilder();
        sctpDstBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
                .inet.types.rev100924.PortNumber(14));
        sctpDstCaseBuilder.setSctpDst(sctpDstBuilder.build());
        entriesBuilder.setMatchEntryValue(sctpDstCaseBuilder.build());
        entries.add(entriesBuilder.build());
        builder.setMatchEntry(entries);

        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow
                .MatchBuilder salMatchBuilder = MatchConvertorImpl.fromOFMatchToSALMatch(builder.build(), new BigInteger("42"), OpenflowVersion.OF13);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match builtMatch = salMatchBuilder.build();

        final SctpMatch udpMatch = (SctpMatch) builtMatch.getLayer4Match();
        Assert.assertEquals("Wrong sctp src port", 13, udpMatch.getSctpSourcePort().getValue().intValue());
        Assert.assertEquals("Wrong sctp dst port", 14, udpMatch.getSctpDestinationPort().getValue().intValue());
    }

    /**
     * Test {@link MatchConvertorImpl#fromOFMatchToSALMatch(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match, java.math.BigInteger, org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion)}
     */
    @Test
    public void testLayer3MatchIpv6() {
        final MatchBuilder builder = new MatchBuilder();
        builder.setType(OxmMatchType.class);
        final List<MatchEntry> entries = new ArrayList<>();
        MatchEntryBuilder entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv6Src.class);
        entriesBuilder.setHasMask(false);
        final Ipv6SrcCaseBuilder ipv6SrcCaseBuilder = new Ipv6SrcCaseBuilder();
        final Ipv6SrcBuilder ipv6AddressBuilder = new Ipv6SrcBuilder();
        ipv6AddressBuilder.setIpv6Address(new Ipv6Address("2001:cdba:0000:0000:0000:0000:3257:9657"));
        ipv6SrcCaseBuilder.setIpv6Src(ipv6AddressBuilder.build());
        entriesBuilder.setMatchEntryValue(ipv6SrcCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv6Dst.class);
        entriesBuilder.setHasMask(false);
        final Ipv6DstCaseBuilder ipv6DstCaseBuilder = new Ipv6DstCaseBuilder();
        final Ipv6DstBuilder ipv6DstBuilder = new Ipv6DstBuilder();
        ipv6DstBuilder.setIpv6Address(new Ipv6Address("2001:cdba:0000:0000:0000:0000:3257:9658"));
        ipv6DstCaseBuilder.setIpv6Dst(ipv6DstBuilder.build());
        entriesBuilder.setMatchEntryValue(ipv6DstCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv6Flabel.class);
        entriesBuilder.setHasMask(false);
        final Ipv6FlabelCaseBuilder ipv6FlabelCaseBuilder = new Ipv6FlabelCaseBuilder();
        final Ipv6FlabelBuilder ipv6FlabelBuilder = new Ipv6FlabelBuilder();
        ipv6FlabelBuilder.setIpv6Flabel(new Ipv6FlowLabel(18L));
        ipv6FlabelCaseBuilder.setIpv6Flabel(ipv6FlabelBuilder.build());
        entriesBuilder.setMatchEntryValue(ipv6FlabelCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv6NdTarget.class);
        entriesBuilder.setHasMask(false);
        final Ipv6NdTargetCaseBuilder ipv6NdTargetCaseBuilder = new Ipv6NdTargetCaseBuilder();
        final Ipv6NdTargetBuilder ipv6NdTargetBuilder = new Ipv6NdTargetBuilder();
        ipv6NdTargetBuilder.setIpv6Address(new Ipv6Address("2001:cdba:0000:0000:0000:0000:3257:9659"));
        ipv6NdTargetCaseBuilder.setIpv6NdTarget(ipv6NdTargetBuilder.build());
        entriesBuilder.setMatchEntryValue(ipv6NdTargetCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv6NdSll.class);
        entriesBuilder.setHasMask(false);
        final Ipv6NdSllCaseBuilder ipv6NdSllCaseBuilder = new Ipv6NdSllCaseBuilder();
        final Ipv6NdSllBuilder ipv6NdSllBuilder = new Ipv6NdSllBuilder();
        ipv6NdSllBuilder.setMacAddress(new MacAddress("00:00:00:00:00:05"));
        ipv6NdSllCaseBuilder.setIpv6NdSll(ipv6NdSllBuilder.build());
        entriesBuilder.setMatchEntryValue(ipv6NdSllCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv6NdTll.class);
        entriesBuilder.setHasMask(false);
        final Ipv6NdTllCaseBuilder ipv6NdTllCaseBuilder = new Ipv6NdTllCaseBuilder();
        final Ipv6NdTllBuilder ipv6NdTllBuilder = new Ipv6NdTllBuilder();
        ipv6NdTllBuilder.setMacAddress(new MacAddress("00:00:00:00:00:06"));
        ipv6NdTllCaseBuilder.setIpv6NdTll(ipv6NdTllBuilder.build());
        entriesBuilder.setMatchEntryValue(ipv6NdTllCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv6Exthdr.class);
        entriesBuilder.setHasMask(false);
        final Ipv6ExthdrCaseBuilder ipv6ExthdrCaseBuilder = new Ipv6ExthdrCaseBuilder();
        final Ipv6ExthdrBuilder ipv6ExthdrBuilder = new Ipv6ExthdrBuilder();
        ipv6ExthdrBuilder.setPseudoField(new Ipv6ExthdrFlags(true, false, true, false, true, false, true, false, true));
        ipv6ExthdrCaseBuilder.setIpv6Exthdr(ipv6ExthdrBuilder.build());
        entriesBuilder.setMatchEntryValue(ipv6ExthdrCaseBuilder.build());
        builder.setMatchEntry(entries);
        entries.add(entriesBuilder.build());

        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow
                .MatchBuilder salMatchBuilder = MatchConvertorImpl.fromOFMatchToSALMatch(builder.build(), new BigInteger("42"), OpenflowVersion.OF13);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match builtMatch = salMatchBuilder.build();

        final Ipv6Match ipv6Match = (Ipv6Match) builtMatch.getLayer3Match();
        Assert.assertEquals("Wrong ipv6 src address", "2001:cdba:0000:0000:0000:0000:3257:9657/128",
                ipv6Match.getIpv6Source().getValue());
        Assert.assertEquals("Wrong ipv6 dst address", "2001:cdba:0000:0000:0000:0000:3257:9658/128",
                ipv6Match.getIpv6Destination().getValue());
        Assert.assertEquals("Wrong ipv6 nd target", "2001:cdba:0000:0000:0000:0000:3257:9659",
                ipv6Match.getIpv6NdTarget().getValue());
        Assert.assertEquals("Wrong ipv6 flabel", 18, ipv6Match.getIpv6Label().getIpv6Flabel().getValue().intValue());
        Assert.assertEquals("Wrong ipv6 nd sll", new MacAddress("00:00:00:00:00:05"), ipv6Match.getIpv6NdSll());
        Assert.assertEquals("Wrong ipv6 nd tll", new MacAddress("00:00:00:00:00:06"), ipv6Match.getIpv6NdTll());
        Assert.assertEquals("Wrong ipv6 ext header", 358, ipv6Match.getIpv6ExtHeader().getIpv6Exthdr().intValue());
    }

    /**
     * Test {@link MatchConvertorImpl#fromOFMatchToSALMatch(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match, java.math.BigInteger, org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion)}
     */
    @Test
    public void testLayer3MatchIpv6ExtHeader2() {
        final MatchBuilder builder = new MatchBuilder();
        builder.setType(OxmMatchType.class);
        final List<MatchEntry> entries = new ArrayList<>();
        final MatchEntryBuilder entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv6Exthdr.class);
        entriesBuilder.setHasMask(true);
        final Ipv6ExthdrCaseBuilder ipv6ExthdrCaseBuilder = new Ipv6ExthdrCaseBuilder();
        final Ipv6ExthdrBuilder ipv6ExthdrBuilder = new Ipv6ExthdrBuilder();
        ipv6ExthdrBuilder.setPseudoField(new Ipv6ExthdrFlags(false, true, false, true, false, true, false, true, false));
        ipv6ExthdrBuilder.setMask(new byte[]{1, 2});
        ipv6ExthdrCaseBuilder.setIpv6Exthdr(ipv6ExthdrBuilder.build());
        entriesBuilder.setMatchEntryValue(ipv6ExthdrCaseBuilder.build());
        entries.add(entriesBuilder.build());
        builder.setMatchEntry(entries);

        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow
                .MatchBuilder salMatchBuilder = MatchConvertorImpl.fromOFMatchToSALMatch(builder.build(), new BigInteger("42"), OpenflowVersion.OF13);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match builtMatch = salMatchBuilder.build();

        final Ipv6Match ipv6Match = (Ipv6Match) builtMatch.getLayer3Match();
        Assert.assertEquals("Wrong ipv6 ext header", 153, ipv6Match.getIpv6ExtHeader().getIpv6Exthdr().intValue());
        Assert.assertEquals("Wrong ipv6 ext header mask", 258, ipv6Match.getIpv6ExtHeader().getIpv6ExthdrMask().intValue());
    }

    /**
     * Test {@link MatchConvertorImpl#fromOFMatchToSALMatch(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match, java.math.BigInteger, org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion)}
     */
    @Test
    public void testLayer3MatchArp() {
        final MatchBuilder builder = new MatchBuilder();
        builder.setType(OxmMatchType.class);
        final List<MatchEntry> entries = new ArrayList<>();
        MatchEntryBuilder entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(ArpOp.class);
        entriesBuilder.setHasMask(false);
        final ArpOpCaseBuilder arpOpCaseBuilder = new ArpOpCaseBuilder();
        final ArpOpBuilder arpOpBuilder = new ArpOpBuilder();
        arpOpBuilder.setOpCode(17);
        arpOpCaseBuilder.setArpOp(arpOpBuilder.build());
        entriesBuilder.setMatchEntryValue(arpOpCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(ArpSpa.class);
        entriesBuilder.setHasMask(false);
        final ArpSpaCaseBuilder arpSpaCaseBuilder = new ArpSpaCaseBuilder();
        final ArpSpaBuilder arpSpaBuilder = new ArpSpaBuilder();
        arpSpaBuilder.setIpv4Address(new Ipv4Address("10.0.0.3"));
        arpSpaCaseBuilder.setArpSpa(arpSpaBuilder.build());
        entriesBuilder.setMatchEntryValue(arpSpaCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(ArpTpa.class);
        entriesBuilder.setHasMask(false);
        final ArpTpaCaseBuilder arpTpaCaseBuilder = new ArpTpaCaseBuilder();
        final ArpTpaBuilder arpTpaBuilder = new ArpTpaBuilder();
        arpTpaBuilder.setIpv4Address(new Ipv4Address("10.0.0.4"));
        arpTpaCaseBuilder.setArpTpa(arpTpaBuilder.build());
        entriesBuilder.setMatchEntryValue(arpTpaCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(ArpSha.class);
        entriesBuilder.setHasMask(false);
        final ArpShaCaseBuilder arpShaCaseBuilder = new ArpShaCaseBuilder();
        final ArpShaBuilder arpShaBuilder = new ArpShaBuilder();
        arpShaBuilder.setMacAddress(new MacAddress("00:00:00:00:00:03"));
        arpShaCaseBuilder.setArpSha(arpShaBuilder.build());
        entriesBuilder.setMatchEntryValue(arpShaCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpTha.class);
        entriesBuilder.setHasMask(false);
        final ArpThaCaseBuilder arpThaCaseBuilder = new ArpThaCaseBuilder();
        final ArpThaBuilder arpThaBuilder = new ArpThaBuilder();
        arpThaBuilder.setMacAddress(new MacAddress("00:00:00:00:00:04"));
        arpThaCaseBuilder.setArpTha(arpThaBuilder.build());
        entriesBuilder.setMatchEntryValue(arpThaCaseBuilder.build());
        entries.add(entriesBuilder.build());
        builder.setMatchEntry(entries);

        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow
                .MatchBuilder salMatchBuilder = MatchConvertorImpl.fromOFMatchToSALMatch(builder.build(), new BigInteger("42"), OpenflowVersion.OF13);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match builtMatch = salMatchBuilder.build();

        final ArpMatch arpMatch = (ArpMatch) builtMatch.getLayer3Match();
        Assert.assertEquals("Wrong arp op", 17, arpMatch.getArpOp().intValue());
        Assert.assertEquals("Wrong arp spa", "10.0.0.3/32", arpMatch.getArpSourceTransportAddress().getValue());
        Assert.assertEquals("Wrong arp tpa", "10.0.0.4/32", arpMatch.getArpTargetTransportAddress().getValue());
        Assert.assertEquals("Wrong arp sha", "00:00:00:00:00:03", arpMatch.getArpSourceHardwareAddress().getAddress().getValue());
        Assert.assertEquals("Wrong arp tha", "00:00:00:00:00:04", arpMatch.getArpTargetHardwareAddress().getAddress().getValue());
    }

    /**
     * Test {@link MatchConvertorImpl#fromOFMatchToSALMatch(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match, java.math.BigInteger, org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion)}
     */
    @Test
    public void testLayer3MatchArpWithMasks() {
        final MatchBuilder builder = new MatchBuilder();
        builder.setType(OxmMatchType.class);
        final List<MatchEntry> entries = new ArrayList<>();
        MatchEntryBuilder entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(ArpSpa.class);
        entriesBuilder.setHasMask(true);
        final ArpSpaCaseBuilder arpSpaCaseBuilder = new ArpSpaCaseBuilder();
        final ArpSpaBuilder arpSpaBuilder = new ArpSpaBuilder();
        arpSpaBuilder.setIpv4Address(new Ipv4Address("10.0.0.3"));
        arpSpaBuilder.setMask(new byte[]{(byte) 255, (byte) 255, (byte) 255, 0});
        arpSpaCaseBuilder.setArpSpa(arpSpaBuilder.build());
        entriesBuilder.setMatchEntryValue(arpSpaCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(ArpTpa.class);
        entriesBuilder.setHasMask(true);
        final ArpTpaCaseBuilder arpTpaCaseBuilder = new ArpTpaCaseBuilder();
        final ArpTpaBuilder arpTpaBuilder = new ArpTpaBuilder();
        arpTpaBuilder.setIpv4Address(new Ipv4Address("10.0.0.0"));
        arpTpaBuilder.setMask(new byte[]{(byte) 255, (byte) 128, 0, 0});
        arpTpaCaseBuilder.setArpTpa(arpTpaBuilder.build());
        entriesBuilder.setMatchEntryValue(arpTpaCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(ArpSha.class);
        entriesBuilder.setHasMask(true);
        final ArpShaCaseBuilder arpShaCaseBuilder = new ArpShaCaseBuilder();
        final ArpShaBuilder arpShaBuilder = new ArpShaBuilder();
        arpShaBuilder.setMacAddress(new MacAddress("00:00:00:00:00:03"));
        arpShaBuilder.setMask(new byte[]{0, 0, 1, 0, 4, 0});
        arpShaCaseBuilder.setArpSha(arpShaBuilder.build());
        entriesBuilder.setMatchEntryValue(arpShaCaseBuilder.build());
        entries.add(entriesBuilder.build());

        entriesBuilder = new MatchEntryBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpTha.class);
        entriesBuilder.setHasMask(true);
        final ArpThaCaseBuilder arpThaCaseBuilder = new ArpThaCaseBuilder();
        final ArpThaBuilder arpThaBuilder = new ArpThaBuilder();
        arpThaBuilder.setMacAddress(new MacAddress("00:00:00:00:00:04"));
        arpThaBuilder.setMask(new byte[]{1, 1, 1, 2, 2, 2});
        arpThaCaseBuilder.setArpTha(arpThaBuilder.build());
        entriesBuilder.setMatchEntryValue(arpThaCaseBuilder.build());
        entries.add(entriesBuilder.build());

        builder.setMatchEntry(entries);

        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow
                .MatchBuilder salMatchBuilder = MatchConvertorImpl.fromOFMatchToSALMatch(builder.build(), new BigInteger("42"), OpenflowVersion.OF13);
        final org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match builtMatch = salMatchBuilder.build();

        final ArpMatch arpMatch = (ArpMatch) builtMatch.getLayer3Match();
        Assert.assertEquals("Wrong arp spa", "10.0.0.3/24", arpMatch.getArpSourceTransportAddress().getValue());
        Assert.assertEquals("Wrong arp tpa", "10.0.0.0/9", arpMatch.getArpTargetTransportAddress().getValue());
        Assert.assertEquals("Wrong arp sha", "00:00:00:00:00:03", arpMatch.getArpSourceHardwareAddress().getAddress().getValue());
        Assert.assertEquals("Wrong arp sha mask", "00:00:01:00:04:00", arpMatch.getArpSourceHardwareAddress().getMask().getValue());
        Assert.assertEquals("Wrong arp tha", "00:00:00:00:00:04", arpMatch.getArpTargetHardwareAddress().getAddress().getValue());
        Assert.assertEquals("Wrong arp tha mask", "01:01:01:02:02:02", arpMatch.getArpTargetHardwareAddress().getMask().getValue());
    }
}
