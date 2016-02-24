/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6FlowLabel;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpSourceHardwareAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpTargetHardwareAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ipv6.match.fields.Ipv6ExtHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ipv6.match.fields.Ipv6LabelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.MetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFieldsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TunnelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6ArbitraryMaskMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.SctpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.protocol.match.fields.PbbBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.opendaylight.ipv6.arbitrary.bit.mask.fields.rev160224.Ipv6Arbitrary;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Ipv6ExthdrFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpOp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpSha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpSpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpTha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpTpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv4Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv4Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv6Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv6Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPort;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MplsBos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MplsLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MplsTc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.PbbIsid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.SctpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.SctpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TcpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TcpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TunnelId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.UdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.UdpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpOpCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpShaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpSpaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpThaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpTpaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthTypeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv4CodeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv4TypeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv6CodeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv6TypeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPhyPortCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPortCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpDscpCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpEcnCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpProtoCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4DstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4SrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6DstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6ExthdrCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6FlabelCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6NdSllCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6NdTargetCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6NdTllCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6SrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MetadataCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MplsBosCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MplsLabelCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MplsTcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.PbbIsidCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.SctpDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.SctpSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TcpDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TcpSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TunnelIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.UdpDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.UdpSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanPcpCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanVidCase;

import java.math.BigInteger;
import java.util.List;

/**
 * @author michal.polkorab
 */
public class MatchConvertorImpl2Test {

    private static final MatchConvertorImpl convertor = new MatchConvertorImpl();

    /**
     * Initializes OpenflowPortsUtil
     */
    @Before
    public void startUp() {
        OpenflowPortsUtil.init();
    }

    /**
     * Test {@link MatchConvertorImpl#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match, java.math.BigInteger)}
     */
    @Test
    public void testEmptyAndNullInput() {
        MatchBuilder builder = new MatchBuilder();
        Match match = builder.build();

        List<MatchEntry> entries = convertor.convert(null, new BigInteger("42"));
        Assert.assertEquals("Wrong entries size", 0, entries.size());

        entries = convertor.convert(match, new BigInteger("42"));
        Assert.assertEquals("Wrong entries size", 0, entries.size());
    }

    /**
     * Test {@link MatchConvertorImpl#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match, java.math.BigInteger)}
     */
    @Test
    public void testConversion() {
        MatchBuilder builder = new MatchBuilder();
        builder.setInPort(new NodeConnectorId("openflow:42:1"));
        builder.setInPhyPort(new NodeConnectorId("openflow:42:2"));
        MetadataBuilder metadataBuilder = new MetadataBuilder();
        metadataBuilder.setMetadata(new BigInteger("3"));
        builder.setMetadata(metadataBuilder.build());
        EthernetMatchBuilder ethernetBuilder = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(4L));
        ethernetBuilder.setEthernetType(ethTypeBuilder.build());
        EthernetSourceBuilder ethSrcBuilder = new EthernetSourceBuilder();
        ethSrcBuilder.setAddress(new MacAddress("00:00:00:00:00:05"));
        ethernetBuilder.setEthernetSource(ethSrcBuilder.build());
        EthernetDestinationBuilder ethDstBuilder = new EthernetDestinationBuilder();
        ethDstBuilder.setAddress(new MacAddress("00:00:00:00:00:06"));
        ethernetBuilder.setEthernetDestination(ethDstBuilder.build());
        builder.setEthernetMatch(ethernetBuilder.build());
        VlanMatchBuilder vlanBuilder = new VlanMatchBuilder();
        VlanIdBuilder vlanIdBuilder = new VlanIdBuilder();
        vlanIdBuilder.setVlanId(new VlanId(7));
        vlanIdBuilder.setVlanIdPresent(true);
        vlanBuilder.setVlanId(vlanIdBuilder.build());
        vlanBuilder.setVlanPcp(new VlanPcp((short) 7));
        builder.setVlanMatch(vlanBuilder.build());
        IpMatchBuilder ipMatchBuilder = new IpMatchBuilder();
        ipMatchBuilder.setIpDscp(new Dscp((short) 8));
        ipMatchBuilder.setIpEcn((short) 9);
        ipMatchBuilder.setIpProtocol((short) 10);
        builder.setIpMatch(ipMatchBuilder.build());
        TcpMatchBuilder tcpMatchBuilder = new TcpMatchBuilder();
        tcpMatchBuilder.setTcpSourcePort(new PortNumber(11));
        tcpMatchBuilder.setTcpDestinationPort(new PortNumber(12));
        builder.setLayer4Match(tcpMatchBuilder.build());
        Icmpv4MatchBuilder icmpv4Builder = new Icmpv4MatchBuilder();
        icmpv4Builder.setIcmpv4Type((short) 13);
        icmpv4Builder.setIcmpv4Code((short) 14);
        builder.setIcmpv4Match(icmpv4Builder.build());
        Icmpv6MatchBuilder icmpv6Builder = new Icmpv6MatchBuilder();
        icmpv6Builder.setIcmpv6Type((short) 15);
        icmpv6Builder.setIcmpv6Code((short) 16);
        builder.setIcmpv6Match(icmpv6Builder.build());
        ProtocolMatchFieldsBuilder protoBuilder = new ProtocolMatchFieldsBuilder();
        protoBuilder.setMplsLabel(17L);
        protoBuilder.setMplsTc((short) 18);
        protoBuilder.setMplsBos((short) 19);
        PbbBuilder pbbBuilder = new PbbBuilder();
        pbbBuilder.setPbbIsid(20L);
        protoBuilder.setPbb(pbbBuilder.build());
        builder.setProtocolMatchFields(protoBuilder.build());
        TunnelBuilder tunnelBuilder = new TunnelBuilder();
        tunnelBuilder.setTunnelId(new BigInteger("21"));
        builder.setTunnel(tunnelBuilder.build());
        Ipv4MatchBuilder ipv4MatchBuilder = new Ipv4MatchBuilder();
        ipv4MatchBuilder.setIpv4Source(new Ipv4Prefix("10.0.0.1/32"));
        ipv4MatchBuilder.setIpv4Destination(new Ipv4Prefix("10.0.0.2/32"));
        builder.setLayer3Match(ipv4MatchBuilder.build());
        Match match = builder.build();

        List<MatchEntry> entries = convertor.convert(match, new BigInteger("42"));
        Assert.assertEquals("Wrong entries size", 24, entries.size());
        MatchEntry entry = entries.get(0);
        checkEntryHeader(entry, InPort.class, false);
        Assert.assertEquals("Wrong in port", 1, ((InPortCase) entry.getMatchEntryValue()).getInPort()
                .getPortNumber().getValue().intValue());

        entry = entries.get(1);
        checkEntryHeader(entry, InPhyPort.class, false);
        Assert.assertEquals("Wrong in phy port", 2, ((InPhyPortCase) entry.getMatchEntryValue()).
                        getInPhyPort().getPortNumber().getValue().intValue());

        entry = entries.get(2);
        checkEntryHeader(entry, Metadata.class, false);
        Assert.assertArrayEquals("Wrong metadata", new byte[]{0, 0, 0, 0, 0, 0, 0, 3},
                ((MetadataCase) entry.getMatchEntryValue()).getMetadata().getMetadata());
        entry = entries.get(3);
        checkEntryHeader(entry, EthDst.class, false);
        Assert.assertEquals("Wrong eth dst", new MacAddress("00:00:00:00:00:06"),
                ((EthDstCase) entry.getMatchEntryValue()).getEthDst().getMacAddress());
        entry = entries.get(4);
        checkEntryHeader(entry, EthSrc.class, false);
        Assert.assertEquals("Wrong eth src", new MacAddress("00:00:00:00:00:05"),
                ((EthSrcCase) entry.getMatchEntryValue()).getEthSrc().getMacAddress());
        entry = entries.get(5);
        checkEntryHeader(entry, EthType.class, false);
        Assert.assertEquals("Wrong eth type", 4, ((EthTypeCase) entry.getMatchEntryValue())
                .getEthType().getEthType().getValue().intValue());
        entry = entries.get(6);
        checkEntryHeader(entry, VlanVid.class, false);
        Assert.assertEquals("Wrong vlan id", 7, ((VlanVidCase) entry.getMatchEntryValue())
                .getVlanVid().getVlanVid().intValue());
        Assert.assertEquals("Wrong cfi bit", true, ((VlanVidCase) entry.getMatchEntryValue())
                .getVlanVid().isCfiBit());
        entry = entries.get(7);
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanPcp.class, false);
        Assert.assertEquals("Wrong vlan pcp", 7, ((VlanPcpCase) entry.getMatchEntryValue())
                .getVlanPcp().getVlanPcp().intValue());
        entry = entries.get(8);
        checkEntryHeader(entry, IpDscp.class, false);
        Assert.assertEquals("Wrong ip dscp", 8, ((IpDscpCase) entry.getMatchEntryValue())
                .getIpDscp().getDscp().getValue().intValue());
        entry = entries.get(9);
        checkEntryHeader(entry, IpEcn.class, false);
        Assert.assertEquals("Wrong ip ecn", 9, ((IpEcnCase) entry.getMatchEntryValue())
                .getIpEcn().getEcn().intValue());
        entry = entries.get(10);
        checkEntryHeader(entry, IpProto.class, false);
        Assert.assertEquals("Wrong ip proto", 10, ((IpProtoCase) entry.getMatchEntryValue())
                .getIpProto().getProtocolNumber().intValue());
        entry = entries.get(11);
        checkEntryHeader(entry, TcpSrc.class, false);
        Assert.assertEquals("Wrong tcp src", 11, ((TcpSrcCase) entry.getMatchEntryValue())
                .getTcpSrc().getPort().getValue().intValue());
        entry = entries.get(12);
        checkEntryHeader(entry, TcpDst.class, false);
        Assert.assertEquals("Wrong tcp dst", 12, ((TcpDstCase) entry.getMatchEntryValue())
                .getTcpDst().getPort().getValue().intValue());
        entry = entries.get(13);
        checkEntryHeader(entry, Icmpv4Type.class, false);
        Assert.assertEquals("Wrong icmpv4 type", 13, ((Icmpv4TypeCase) entry.getMatchEntryValue())
                .getIcmpv4Type().getIcmpv4Type().intValue());
        entry = entries.get(14);
        checkEntryHeader(entry, Icmpv4Code.class, false);
        Assert.assertEquals("Wrong icmpv4 code", 14, ((Icmpv4CodeCase) entry.getMatchEntryValue())
                .getIcmpv4Code().getIcmpv4Code().intValue());
        entry = entries.get(15);
        checkEntryHeader(entry, Icmpv6Type.class, false);
        Assert.assertEquals("Wrong icmpv6 type", 15, ((Icmpv6TypeCase) entry.getMatchEntryValue())
                .getIcmpv6Type().getIcmpv6Type().intValue());
        entry = entries.get(16);
        checkEntryHeader(entry, Icmpv6Code.class, false);
        Assert.assertEquals("Wrong icmpv6 code", 16, ((Icmpv6CodeCase) entry.getMatchEntryValue())
                .getIcmpv6Code().getIcmpv6Code().intValue());
        entry = entries.get(17);
        checkEntryHeader(entry, Ipv4Src.class, false);
        Assert.assertEquals("Wrong ipv4 src", "10.0.0.1", ((Ipv4SrcCase) entry.getMatchEntryValue())
                .getIpv4Src().getIpv4Address().getValue());
        entry = entries.get(18);
        checkEntryHeader(entry, Ipv4Dst.class, false);
        Assert.assertEquals("Wrong ipv4 dst", "10.0.0.2", ((Ipv4DstCase) entry.getMatchEntryValue())
                .getIpv4Dst().getIpv4Address().getValue());
        entry = entries.get(19);
        checkEntryHeader(entry, MplsLabel.class, false);
        Assert.assertEquals("Wrong mpls label", 17, ((MplsLabelCase) entry.getMatchEntryValue())
                .getMplsLabel().getMplsLabel().intValue());
        entry = entries.get(20);
        checkEntryHeader(entry, MplsBos.class, false);
        Assert.assertEquals("Wrong mpls bos", true, ((MplsBosCase) entry.getMatchEntryValue()).getMplsBos().isBos());
        entry = entries.get(21);
        checkEntryHeader(entry, MplsTc.class, false);
        Assert.assertEquals("Wrong mpls tc", 18, ((MplsTcCase) entry.getMatchEntryValue())
                .getMplsTc().getTc().intValue());
        entry = entries.get(22);
        checkEntryHeader(entry, PbbIsid.class, false);
        Assert.assertEquals("Wrong pbb isid", 20, ((PbbIsidCase) entry.getMatchEntryValue())
                .getPbbIsid().getIsid().intValue());
        entry = entries.get(23);
        checkEntryHeader(entry, TunnelId.class, false);
        Assert.assertArrayEquals("Wrong tunnel id", new byte[]{0, 0, 0, 0, 0, 0, 0, 21},
                ((TunnelIdCase) entry.getMatchEntryValue()).getTunnelId().getTunnelId());
    }

    private static void checkEntryHeader(MatchEntry entry, Class<? extends MatchField> field, boolean hasMask) {
        Assert.assertEquals("Wrong oxm class", OpenflowBasicClass.class, entry.getOxmClass());
        Assert.assertEquals("Wrong oxm field", field, entry.getOxmMatchField());
        Assert.assertEquals("Wrong hasMask", hasMask, entry.isHasMask());
    }

    /**
     * Test {@link MatchConvertorImpl#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match, java.math.BigInteger)}
     */
    @Test
    public void testUdpMatchConversion() {
        MatchBuilder builder = new MatchBuilder();
        UdpMatchBuilder udpMatchBuilder = new UdpMatchBuilder();
        udpMatchBuilder.setUdpSourcePort(new PortNumber(11));
        udpMatchBuilder.setUdpDestinationPort(new PortNumber(12));
        builder.setLayer4Match(udpMatchBuilder.build());
        Match match = builder.build();

        List<MatchEntry> entries = convertor.convert(match, new BigInteger("42"));
        Assert.assertEquals("Wrong entries size", 2, entries.size());
        MatchEntry entry = entries.get(0);
        checkEntryHeader(entry, UdpSrc.class, false);
        Assert.assertEquals("Wrong udp src", 11, ((UdpSrcCase) entry.getMatchEntryValue()).getUdpSrc()
                .getPort().getValue().intValue());
        entry = entries.get(1);
        checkEntryHeader(entry, UdpDst.class, false);
        Assert.assertEquals("Wrong udp dst", 12, ((UdpDstCase) entry.getMatchEntryValue())
                .getUdpDst().getPort().getValue().intValue());
    }

    /**
     * Test {@link MatchConvertorImpl#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match, java.math.BigInteger)}
     */
    @Test
    public void testSctpMatchConversion() {
        MatchBuilder builder = new MatchBuilder();
        SctpMatchBuilder sctpMatchBuilder = new SctpMatchBuilder();
        sctpMatchBuilder.setSctpSourcePort(new PortNumber(11));
        sctpMatchBuilder.setSctpDestinationPort(new PortNumber(12));
        builder.setLayer4Match(sctpMatchBuilder.build());
        Match match = builder.build();

        List<MatchEntry> entries = convertor.convert(match, new BigInteger("42"));
        Assert.assertEquals("Wrong entries size", 2, entries.size());
        MatchEntry entry = entries.get(0);
        checkEntryHeader(entry, SctpSrc.class, false);
        Assert.assertEquals("Wrong sctp src", 11, ((SctpSrcCase) entry.getMatchEntryValue()).getSctpSrc()
                .getPort().getValue().intValue());
        entry = entries.get(1);
        checkEntryHeader(entry, SctpDst.class, false);
        Assert.assertEquals("Wrong sctp dst", 12, ((SctpDstCase) entry.getMatchEntryValue())
                .getSctpDst().getPort().getValue().intValue());
    }

    /**
     * Test {@link MatchConvertorImpl#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match, java.math.BigInteger)}
     */
    @Test
    public void testArpMatchConversion() {
        MatchBuilder builder = new MatchBuilder();
        ArpMatchBuilder arpBuilder = new ArpMatchBuilder();
        arpBuilder.setArpOp(5);
        arpBuilder.setArpSourceTransportAddress(new Ipv4Prefix("10.0.0.3/32"));
        arpBuilder.setArpTargetTransportAddress(new Ipv4Prefix("10.0.0.4/32"));
        ArpSourceHardwareAddressBuilder srcHwBuilder = new ArpSourceHardwareAddressBuilder();
        srcHwBuilder.setAddress(new MacAddress("00:00:00:00:00:05"));
        arpBuilder.setArpSourceHardwareAddress(srcHwBuilder.build());
        ArpTargetHardwareAddressBuilder dstHwBuilder = new ArpTargetHardwareAddressBuilder();
        dstHwBuilder.setAddress(new MacAddress("00:00:00:00:00:06"));
        arpBuilder.setArpTargetHardwareAddress(dstHwBuilder.build());
        builder.setLayer3Match(arpBuilder.build());
        Match match = builder.build();

        List<MatchEntry> entries = convertor.convert(match, new BigInteger("42"));
        Assert.assertEquals("Wrong entries size", 5, entries.size());
        MatchEntry entry = entries.get(0);
        checkEntryHeader(entry, ArpOp.class, false);
        Assert.assertEquals("Wrong arp op", 5, ((ArpOpCase) entry.getMatchEntryValue())
                .getArpOp().getOpCode().intValue());
        entry = entries.get(1);
        checkEntryHeader(entry, ArpSpa.class, false);
        Assert.assertEquals("Wrong arp spa", "10.0.0.3", ((ArpSpaCase) entry.getMatchEntryValue())
                .getArpSpa().getIpv4Address().getValue());
        entry = entries.get(2);
        checkEntryHeader(entry, ArpTpa.class, false);
        Assert.assertEquals("Wrong arp tpa", "10.0.0.4", ((ArpTpaCase) entry.getMatchEntryValue())
                .getArpTpa().getIpv4Address().getValue());
        entry = entries.get(3);
        checkEntryHeader(entry, ArpSha.class, false);
        Assert.assertEquals("Wrong arp sha", "00:00:00:00:00:05", ((ArpShaCase) entry.getMatchEntryValue())
                .getArpSha().getMacAddress().getValue());
        entry = entries.get(4);
        checkEntryHeader(entry, ArpTha.class, false);
        Assert.assertEquals("Wrong arp tha", "00:00:00:00:00:06", ((ArpThaCase) entry.getMatchEntryValue())
                .getArpTha().getMacAddress().getValue());
    }

    /**
     * Test {@link MatchConvertorImpl#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match, java.math.BigInteger)}
     */
    @Test
    public void testArpMatchConversionWithMasks() {
        MatchBuilder builder = new MatchBuilder();
        ArpMatchBuilder arpBuilder = new ArpMatchBuilder();
        /* Use canonnical prefixes !!! */
        arpBuilder.setArpSourceTransportAddress(new Ipv4Prefix("10.0.0.0/8"));
        arpBuilder.setArpTargetTransportAddress(new Ipv4Prefix("10.0.0.4/31"));
        ArpSourceHardwareAddressBuilder srcHwBuilder = new ArpSourceHardwareAddressBuilder();
        srcHwBuilder.setAddress(new MacAddress("00:00:00:00:00:05"));
        srcHwBuilder.setMask(new MacAddress("00:00:00:00:00:08"));
        arpBuilder.setArpSourceHardwareAddress(srcHwBuilder.build());
        ArpTargetHardwareAddressBuilder dstHwBuilder = new ArpTargetHardwareAddressBuilder();
        dstHwBuilder.setAddress(new MacAddress("00:00:00:00:00:06"));
        dstHwBuilder.setMask(new MacAddress("00:00:00:00:00:09"));
        arpBuilder.setArpTargetHardwareAddress(dstHwBuilder.build());
        builder.setLayer3Match(arpBuilder.build());
        Match match = builder.build();

        List<MatchEntry> entries = convertor.convert(match, new BigInteger("42"));
        Assert.assertEquals("Wrong entries size", 4, entries.size());
        MatchEntry entry = entries.get(0);
        entry = entries.get(0);
        checkEntryHeader(entry, ArpSpa.class, true);
        Assert.assertEquals("Wrong arp spa", "10.0.0.0", ((ArpSpaCase) entry.getMatchEntryValue())
                .getArpSpa().getIpv4Address().getValue());
        Assert.assertArrayEquals("Wrong arp spa mask", new byte[]{(byte) 255, 0, 0, 0},
                ((ArpSpaCase) entry.getMatchEntryValue()).getArpSpa().getMask());
        entry = entries.get(1);
        checkEntryHeader(entry, ArpTpa.class, true);
        Assert.assertEquals("Wrong arp tpa", "10.0.0.4", ((ArpTpaCase) entry.getMatchEntryValue()).getArpTpa()
                .getIpv4Address().getValue());
        Assert.assertArrayEquals("Wrong arp tpa mask", new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 254},
                ((ArpTpaCase) entry.getMatchEntryValue()).getArpTpa().getMask());
        entry = entries.get(2);
        checkEntryHeader(entry, ArpSha.class, true);
        Assert.assertEquals("Wrong arp sha", "00:00:00:00:00:05", ((ArpShaCase) entry.getMatchEntryValue())
                .getArpSha().getMacAddress().getValue());
        Assert.assertArrayEquals("Wrong arp sha mask", new byte[]{0, 0, 0, 0, 0, 8},
                ((ArpShaCase) entry.getMatchEntryValue()).getArpSha().getMask());
        entry = entries.get(3);
        checkEntryHeader(entry, ArpTha.class, true);
        Assert.assertEquals("Wrong arp tha", "00:00:00:00:00:06", ((ArpThaCase) entry.getMatchEntryValue()).getArpTha()
                .getMacAddress().getValue());
        Assert.assertArrayEquals("Wrong arp tha mask", new byte[]{0, 0, 0, 0, 0, 9},
                ((ArpThaCase) entry.getMatchEntryValue()).getArpTha().getMask());
    }

    /**
     * Test {@link MatchConvertorImpl#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match, java.math.BigInteger)}
     */
    @Test
    public void testIpv6MatchConversion() {
        MatchBuilder builder = new MatchBuilder();
        Ipv6MatchBuilder ipv6Builder = new Ipv6MatchBuilder();
        ipv6Builder.setIpv6Source(new Ipv6Prefix("::1/128"));
        ipv6Builder.setIpv6Destination(new Ipv6Prefix("::2/128"));
        Ipv6LabelBuilder ipv6LabelBuilder = new Ipv6LabelBuilder();
        ipv6LabelBuilder.setIpv6Flabel(new Ipv6FlowLabel(3L));
        ipv6Builder.setIpv6Label(ipv6LabelBuilder.build());
        ipv6Builder.setIpv6NdTarget(new Ipv6Address("::4"));
        ipv6Builder.setIpv6NdSll(new MacAddress("00:00:00:00:00:05"));
        ipv6Builder.setIpv6NdTll(new MacAddress("00:00:00:00:00:06"));
        Ipv6ExtHeaderBuilder extHdrBuilder = new Ipv6ExtHeaderBuilder();
        extHdrBuilder.setIpv6Exthdr(153);
        ipv6Builder.setIpv6ExtHeader(extHdrBuilder.build());
        builder.setLayer3Match(ipv6Builder.build());
        Match match = builder.build();

        List<MatchEntry> entries = convertor.convert(match, new BigInteger("42"));
        Assert.assertEquals("Wrong entries size", 7, entries.size());
        MatchEntry entry = entries.get(0);
        /* Due to conversion ambiguities, we always get "has mask" because
         * an ip with no mask and prefix with /128 (or 32 in v4) cannot
         * be distinguished */
        checkEntryHeader(entry, Ipv6Src.class, true);
        Assert.assertEquals("Wrong ipv6 src", "::1",
                ((Ipv6SrcCase) entry.getMatchEntryValue()).getIpv6Src().getIpv6Address().getValue());
        entry = entries.get(1);
        checkEntryHeader(entry, Ipv6Dst.class, true);
        Assert.assertEquals("Wrong ipv6 dst", "::2",
                ((Ipv6DstCase) entry.getMatchEntryValue()).getIpv6Dst().getIpv6Address().getValue());
        entry = entries.get(2);
        checkEntryHeader(entry, Ipv6Flabel.class, false);
        Assert.assertEquals("Wrong ipv6 flabel", 3,
                ((Ipv6FlabelCase) entry.getMatchEntryValue()).getIpv6Flabel().getIpv6Flabel().getValue().intValue());
        entry = entries.get(3);
        checkEntryHeader(entry, Ipv6NdTarget.class, false);
        Assert.assertEquals("Wrong ipv6 nd target", "::4",
                ((Ipv6NdTargetCase) entry.getMatchEntryValue()).getIpv6NdTarget().getIpv6Address().getValue());
        entry = entries.get(4);
        checkEntryHeader(entry, Ipv6NdSll.class, false);
        Assert.assertEquals("Wrong ipv6 nd sll", "00:00:00:00:00:05",
                ((Ipv6NdSllCase) entry.getMatchEntryValue()).getIpv6NdSll().getMacAddress().getValue());
        entry = entries.get(5);
        checkEntryHeader(entry, Ipv6NdTll.class, false);
        Assert.assertEquals("Wrong ipv6 nd tll", "00:00:00:00:00:06",
                ((Ipv6NdTllCase) entry.getMatchEntryValue()).getIpv6NdTll().getMacAddress().getValue());
        entry = entries.get(6);
        checkEntryHeader(entry, Ipv6Exthdr.class, false);
        Assert.assertEquals("Wrong ipv6 ext hdr", new Ipv6ExthdrFlags(false, true, false, true, false,
                true, false, true, false), ((Ipv6ExthdrCase) entry.getMatchEntryValue()).getIpv6Exthdr().getPseudoField());
    }

    /**
     * Test {@link MatchConvertorImpl#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match, java.math.BigInteger)}
     */
    @Test
    public void testIpv6MatchConversionWithMasks() {
        MatchBuilder builder = new MatchBuilder();
        Ipv6MatchBuilder ipv6Builder = new Ipv6MatchBuilder();
        ipv6Builder.setIpv6Source(new Ipv6Prefix("::/24"));
        ipv6Builder.setIpv6Destination(new Ipv6Prefix("::/64"));
        builder.setLayer3Match(ipv6Builder.build());
        Match match = builder.build();

        List<MatchEntry> entries = convertor.convert(match, new BigInteger("42"));
        Assert.assertEquals("Wrong entries size", 2, entries.size());
        MatchEntry entry = entries.get(0);
        checkEntryHeader(entry, Ipv6Src.class, true);
        Assert.assertEquals("Wrong ipv6 src", "::",
                ((Ipv6SrcCase) entry.getMatchEntryValue()).getIpv6Src().getIpv6Address().getValue());
        Assert.assertArrayEquals("Wrong ipv6 src mask", new byte[]{(byte) 255, (byte) 255, (byte) 255, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0}, ((Ipv6SrcCase) entry.getMatchEntryValue()).getIpv6Src().getMask());
        entry = entries.get(1);
        checkEntryHeader(entry, Ipv6Dst.class, true);
        Assert.assertEquals("Wrong ipv6 dst", "::",
                ((Ipv6DstCase) entry.getMatchEntryValue()).getIpv6Dst().getIpv6Address().getValue());
        Assert.assertArrayEquals("Wrong ipv6 src mask", new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 255,
                        (byte) 255, (byte) 255, (byte) 255, (byte) 255, 0, 0, 0, 0, 0, 0, 0, 0},
                ((Ipv6DstCase) entry.getMatchEntryValue()).getIpv6Dst().getMask());
    }

    /**
     * Test {@link MatchConvertorImpl#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match, java.math.BigInteger)}
     */
    @Test
    public void testIpv6ExtHeaderConversion() {
        MatchBuilder builder = new MatchBuilder();
        Ipv6MatchBuilder ipv6Builder = new Ipv6MatchBuilder();
        Ipv6ExtHeaderBuilder extHdrBuilder = new Ipv6ExtHeaderBuilder();
        extHdrBuilder.setIpv6Exthdr(358);
        extHdrBuilder.setIpv6ExthdrMask(258);
        ipv6Builder.setIpv6ExtHeader(extHdrBuilder.build());
        builder.setLayer3Match(ipv6Builder.build());
        Match match = builder.build();

        List<MatchEntry> entries = convertor.convert(match, new BigInteger("42"));
        Assert.assertEquals("Wrong entries size", 1, entries.size());
        MatchEntry entry = entries.get(0);
        checkEntryHeader(entry, Ipv6Exthdr.class, true);
        Assert.assertEquals("Wrong ipv6 ext hdr", new Ipv6ExthdrFlags(true, false, true, false, true, false,
                true, false, true), ((Ipv6ExthdrCase) entry.getMatchEntryValue()).getIpv6Exthdr().getPseudoField());
        Assert.assertArrayEquals("Wrong ipv6 ext hdr mask", new byte[]{1, 2},
                ((Ipv6ExthdrCase) entry.getMatchEntryValue()).getIpv6Exthdr().getMask());
    }

    /**
     * Test {@link MatchConvertorImpl#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match, java.math.BigInteger)}
     */
    @Test
    public void testConversionWithMasks() {
        MatchBuilder builder = new MatchBuilder();
        MetadataBuilder metadataBuilder = new MetadataBuilder();
        metadataBuilder.setMetadata(new BigInteger("3"));
        metadataBuilder.setMetadataMask(new BigInteger("15"));
        builder.setMetadata(metadataBuilder.build());
        EthernetMatchBuilder ethernetBuilder = new EthernetMatchBuilder();
        EthernetSourceBuilder ethSrcBuilder = new EthernetSourceBuilder();
        ethSrcBuilder.setAddress(new MacAddress("00:00:00:00:00:05"));
        ethSrcBuilder.setMask(new MacAddress("00:00:00:00:00:08"));
        ethernetBuilder.setEthernetSource(ethSrcBuilder.build());
        EthernetDestinationBuilder ethDstBuilder = new EthernetDestinationBuilder();
        ethDstBuilder.setAddress(new MacAddress("00:00:00:00:00:06"));
        ethDstBuilder.setMask(new MacAddress("00:00:00:00:00:09"));
        ethernetBuilder.setEthernetDestination(ethDstBuilder.build());
        builder.setEthernetMatch(ethernetBuilder.build());
        VlanMatchBuilder vlanBuilder = new VlanMatchBuilder();
        VlanIdBuilder vlanIdBuilder = new VlanIdBuilder();
        vlanIdBuilder.setVlanId(new VlanId(0));
        vlanIdBuilder.setVlanIdPresent(true);
        vlanBuilder.setVlanId(vlanIdBuilder.build());
        builder.setVlanMatch(vlanBuilder.build());
        ProtocolMatchFieldsBuilder protoBuilder = new ProtocolMatchFieldsBuilder();
        PbbBuilder pbbBuilder = new PbbBuilder();
        pbbBuilder.setPbbIsid(20L);
        pbbBuilder.setPbbMask(8L);
        protoBuilder.setPbb(pbbBuilder.build());
        builder.setProtocolMatchFields(protoBuilder.build());
        TunnelBuilder tunnelBuilder = new TunnelBuilder();
        tunnelBuilder.setTunnelId(new BigInteger("21"));
        tunnelBuilder.setTunnelMask(new BigInteger("14"));
        builder.setTunnel(tunnelBuilder.build());
        Ipv4MatchBuilder ipv4MatchBuilder = new Ipv4MatchBuilder();
        ipv4MatchBuilder.setIpv4Source(new Ipv4Prefix("10.0.0.0/24"));
        ipv4MatchBuilder.setIpv4Destination(new Ipv4Prefix("10.0.0.0/8"));
        builder.setLayer3Match(ipv4MatchBuilder.build());
        Match match = builder.build();

        List<MatchEntry> entries = convertor.convert(match, new BigInteger("42"));
        Assert.assertEquals("Wrong entries size", 8, entries.size());
        MatchEntry entry = entries.get(0);
        checkEntryHeader(entry, Metadata.class, true);
        Assert.assertArrayEquals("Wrong metadata", new byte[]{0, 0, 0, 0, 0, 0, 0, 3},
                ((MetadataCase) entry.getMatchEntryValue()).getMetadata().getMetadata());
        Assert.assertArrayEquals("Wrong metadata mask", new byte[]{0, 0, 0, 0, 0, 0, 0, 15},
                ((MetadataCase) entry.getMatchEntryValue()).getMetadata().getMask());
        entry = entries.get(1);
        checkEntryHeader(entry, EthDst.class, true);
        Assert.assertEquals("Wrong eth dst", new MacAddress("00:00:00:00:00:06"),
                ((EthDstCase) entry.getMatchEntryValue()).getEthDst().getMacAddress());
        Assert.assertArrayEquals("Wrong eth dst mask", new byte[]{0, 0, 0, 0, 0, 9},
                ((EthDstCase) entry.getMatchEntryValue()).getEthDst().getMask());
        entry = entries.get(2);
        checkEntryHeader(entry, EthSrc.class, true);
        Assert.assertEquals("Wrong eth src", new MacAddress("00:00:00:00:00:05"),
                ((EthSrcCase) entry.getMatchEntryValue()).getEthSrc().getMacAddress());
        Assert.assertArrayEquals("Wrong eth src mask", new byte[]{0, 0, 0, 0, 0, 8},
                ((EthSrcCase) entry.getMatchEntryValue()).getEthSrc().getMask());
        entry = entries.get(3);
        checkEntryHeader(entry, VlanVid.class, true);
        Assert.assertEquals("Wrong vlan id", 0, ((VlanVidCase) entry.getMatchEntryValue()).getVlanVid()
                .getVlanVid().intValue());
        Assert.assertEquals("Wrong cfi bit", true, ((VlanVidCase) entry.getMatchEntryValue()).getVlanVid()
                .isCfiBit());
        Assert.assertArrayEquals("Wrong vlanId mask", new byte[]{16, 0},
                ((VlanVidCase) entry.getMatchEntryValue()).getVlanVid().getMask());
        entry = entries.get(4);
        checkEntryHeader(entry, Ipv4Src.class, true);
        Assert.assertEquals("Wrong ipv4 src", "10.0.0.0", ((Ipv4SrcCase) entry.getMatchEntryValue())
                .getIpv4Src().getIpv4Address().getValue());
        Assert.assertArrayEquals("Wrong ipv4 src mask", new byte[]{(byte) 255, (byte) 255, (byte) 255, 0},
                ((Ipv4SrcCase) entry.getMatchEntryValue()).getIpv4Src().getMask());
        entry = entries.get(5);
        checkEntryHeader(entry, Ipv4Dst.class, true);
        Assert.assertEquals("Wrong ipv4 dst", "10.0.0.0", ((Ipv4DstCase) entry.getMatchEntryValue())
                .getIpv4Dst().getIpv4Address().getValue());
        Assert.assertArrayEquals("Wrong ipv4 dst mask", new byte[]{(byte) 255, 0, 0, 0},
                ((Ipv4DstCase) entry.getMatchEntryValue()).getIpv4Dst().getMask());
        entry = entries.get(6);
        checkEntryHeader(entry, PbbIsid.class, true);
        Assert.assertEquals("Wrong pbb isid", 20, ((PbbIsidCase) entry.getMatchEntryValue())
                .getPbbIsid().getIsid().intValue());
        Assert.assertArrayEquals("Wrong pbb isid mask", new byte[]{0, 0, 8},
                ((PbbIsidCase) entry.getMatchEntryValue()).getPbbIsid().getMask());
        entry = entries.get(7);
        checkEntryHeader(entry, TunnelId.class, true);
        Assert.assertArrayEquals("Wrong tunnel id", new byte[]{0, 0, 0, 0, 0, 0, 0, 21},
                ((TunnelIdCase) entry.getMatchEntryValue()).getTunnelId().getTunnelId());
        Assert.assertArrayEquals("Wrong tunnel id mask", new byte[]{0, 0, 0, 0, 0, 0, 0, 14},
                ((TunnelIdCase) entry.getMatchEntryValue()).getTunnelId().getMask());
    }

    /**
     * Test {@link MatchConvertorImpl#convert(org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match, java.math.BigInteger)}
     */
    @Test
    public void testIpv6MatchArbitraryBitMask(){
        MatchBuilder builder = new MatchBuilder();
        Ipv6ArbitraryMaskMatchBuilder ipv6MatchArbitraryBitMaskBuilder= new Ipv6ArbitraryMaskMatchBuilder();
        ipv6MatchArbitraryBitMaskBuilder.setIpv6SourceArbitraryAddress(new Ipv6Address("fbA0:FFB6:FFF0:FFF0:FFF0:FFF0:FFF0:AFF0"));
        ipv6MatchArbitraryBitMaskBuilder.setIpv6SourceArbitraryMask(new Ipv6Arbitrary("fbA0:FFB6:FFF0:FFF0:FFF0:FFF0:FFF0:A555"));
        ipv6MatchArbitraryBitMaskBuilder.setIpv6DestinationArbitraryAddress(new Ipv6Address("fbA0:FFB6:FFF0:FFF0:FFF0:FFF0:FFF0:AFF0"));
        ipv6MatchArbitraryBitMaskBuilder.setIpv6DestinationArbitraryMask(new Ipv6Arbitrary("fbA0:FFB6:FFF0:FFF0:FFF0:FFF0:FFF0:A555"));
        builder.setLayer3Match(ipv6MatchArbitraryBitMaskBuilder.build());
        Match match = builder.build();

        List<MatchEntry> entries = convertor.convert(match, new BigInteger("42"));
        Assert.assertEquals("Wrong entries size", 2, entries.size());

        MatchEntry entry = entries.get(0);
        checkEntryHeader(entry,Ipv6Src.class,true);
        Assert.assertEquals("wrong Ipv6Adress source", "fbA0:FFB6:FFF0:FFF0:FFF0:FFF0:FFF0:AFF0",((Ipv6SrcCase) entry.getMatchEntryValue()).getIpv6Src().getIpv6Address().getValue());
        entry = entries.get(1);
        checkEntryHeader(entry,Ipv6Dst.class,true);
        Assert.assertEquals("wrong Ipv6Adress destination", "fbA0:FFB6:FFF0:FFF0:FFF0:FFF0:FFF0:AFF0",((Ipv6DstCase) entry.getMatchEntryValue()).getIpv6Dst().getIpv6Address().getValue());
    }
}
