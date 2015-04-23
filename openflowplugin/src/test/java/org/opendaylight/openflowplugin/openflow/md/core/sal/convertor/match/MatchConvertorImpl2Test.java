/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import java.math.BigInteger;
import java.util.List;

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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.SctpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.protocol.match.fields.PbbBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.BosMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.DscpMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EcnMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EthTypeMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Icmpv4CodeMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Icmpv4TypeMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Icmpv6CodeMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Icmpv6TypeMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv4AddressMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv6AddressMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv6FlabelMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.IsidMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MacAddressMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaskMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MetadataMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MplsLabelMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.OpCodeMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortNumberMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ProtocolNumberMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PseudoFieldMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.TcMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanPcpMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanVidMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Ipv6ExthdrFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.ArpOp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.ArpSha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.ArpSpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.ArpTha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.ArpTpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.EthDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.EthSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.EthType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Icmpv4Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Icmpv4Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Icmpv6Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Icmpv6Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.InPhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.InPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.IpDscp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.IpEcn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.IpProto;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6Exthdr;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6Flabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6NdSll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6NdTarget;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6NdTll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Ipv6Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MplsBos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MplsLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MplsTc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.PbbIsid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.SctpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.SctpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TcpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TcpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TunnelId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.UdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.UdpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;

/**
 * @author michal.polkorab
 *
 */
public class MatchConvertorImpl2Test {

    MatchConvertorImpl convertor = new MatchConvertorImpl();

    /**
     * Initializes OpenflowPortsUtil
     */
    @Before
    public void startUp() {
        OpenflowPortsUtil.init();
    }

    /**
     * Test {@link MatchConvertorImpl#convert(Match, BigInteger)
     */
    @Test
    public void testEmptyAndNullInput() {
        MatchBuilder builder = new MatchBuilder();
        Match match = builder.build();

        List<MatchEntries> entries = convertor.convert(null, new BigInteger("42"));
        Assert.assertEquals("Wrong entries size", 0, entries.size());

        entries = convertor.convert(match, new BigInteger("42"));
        Assert.assertEquals("Wrong entries size", 0, entries.size());
    }

    /**
     * Test {@link MatchConvertorImpl#convert(Match, BigInteger)
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

        List<MatchEntries> entries = convertor.convert(match, new BigInteger("42"));
        Assert.assertEquals("Wrong entries size", 24, entries.size());
        MatchEntries entry = entries.get(0);
        checkEntryHeader(entry, InPort.class, false);
        Assert.assertEquals("Wrong in port", 1, entry.getAugmentation(PortNumberMatchEntry.class)
                .getPortNumber().getValue().intValue());
        entry = entries.get(1);
        checkEntryHeader(entry, InPhyPort.class, false);
        Assert.assertEquals("Wrong in phy port", 2, entry.getAugmentation(PortNumberMatchEntry.class)
                .getPortNumber().getValue().intValue());
        entry = entries.get(2);
        checkEntryHeader(entry, Metadata.class, false);
        Assert.assertArrayEquals("Wrong metadata", new byte[]{0, 0, 0, 0, 0, 0, 0, 3},
                entry.getAugmentation(MetadataMatchEntry.class).getMetadata());
        entry = entries.get(3);
        checkEntryHeader(entry, EthDst.class, false);
        Assert.assertEquals("Wrong eth dst", new MacAddress("00:00:00:00:00:06"),
                entry.getAugmentation(MacAddressMatchEntry.class).getMacAddress());
        entry = entries.get(4);
        checkEntryHeader(entry, EthSrc.class, false);
        Assert.assertEquals("Wrong eth src", new MacAddress("00:00:00:00:00:05"),
                entry.getAugmentation(MacAddressMatchEntry.class).getMacAddress());
        entry = entries.get(5);
        checkEntryHeader(entry, EthType.class, false);
        Assert.assertEquals("Wrong eth type", 4, entry.getAugmentation(EthTypeMatchEntry.class)
                .getEthType().getValue().intValue());
        entry = entries.get(6);
        checkEntryHeader(entry, VlanVid.class, false);
        Assert.assertEquals("Wrong vlan id", 7, entry.getAugmentation(VlanVidMatchEntry.class)
                .getVlanVid().intValue());
        Assert.assertEquals("Wrong cfi bit", true, entry.getAugmentation(VlanVidMatchEntry.class)
                .isCfiBit());
        entry = entries.get(7);
        checkEntryHeader(entry, org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.VlanPcp.class, false);
        Assert.assertEquals("Wrong vlan pcp", 7, entry.getAugmentation(VlanPcpMatchEntry.class)
                .getVlanPcp().intValue());
        entry = entries.get(8);
        checkEntryHeader(entry, IpDscp.class, false);
        Assert.assertEquals("Wrong ip dscp", 8, entry.getAugmentation(DscpMatchEntry.class)
                .getDscp().getValue().intValue());
        entry = entries.get(9);
        checkEntryHeader(entry, IpEcn.class, false);
        Assert.assertEquals("Wrong ip ecn", 9, entry.getAugmentation(EcnMatchEntry.class)
                .getEcn().intValue());
        entry = entries.get(10);
        checkEntryHeader(entry, IpProto.class, false);
        Assert.assertEquals("Wrong ip proto", 10, entry.getAugmentation(ProtocolNumberMatchEntry.class)
                .getProtocolNumber().intValue());
        entry = entries.get(11);
        checkEntryHeader(entry, TcpSrc.class, false);
        Assert.assertEquals("Wrong tcp src", 11, entry.getAugmentation(PortMatchEntry.class)
                .getPort().getValue().intValue());
        entry = entries.get(12);
        checkEntryHeader(entry, TcpDst.class, false);
        Assert.assertEquals("Wrong tcp dst", 12, entry.getAugmentation(PortMatchEntry.class)
                .getPort().getValue().intValue());
        entry = entries.get(13);
        checkEntryHeader(entry, Icmpv4Type.class, false);
        Assert.assertEquals("Wrong icmpv4 type", 13, entry.getAugmentation(Icmpv4TypeMatchEntry.class)
                .getIcmpv4Type().intValue());
        entry = entries.get(14);
        checkEntryHeader(entry, Icmpv4Code.class, false);
        Assert.assertEquals("Wrong icmpv4 code", 14, entry.getAugmentation(Icmpv4CodeMatchEntry.class)
                .getIcmpv4Code().intValue());
        entry = entries.get(15);
        checkEntryHeader(entry, Icmpv6Type.class, false);
        Assert.assertEquals("Wrong icmpv6 type", 15, entry.getAugmentation(Icmpv6TypeMatchEntry.class)
                .getIcmpv6Type().intValue());
        entry = entries.get(16);
        checkEntryHeader(entry, Icmpv6Code.class, false);
        Assert.assertEquals("Wrong icmpv6 code", 16, entry.getAugmentation(Icmpv6CodeMatchEntry.class)
                .getIcmpv6Code().intValue());
        entry = entries.get(17);
        checkEntryHeader(entry, Ipv4Src.class, false);
        Assert.assertEquals("Wrong ipv4 src", "10.0.0.1", entry.getAugmentation(Ipv4AddressMatchEntry.class)
                .getIpv4Address().getValue());
        entry = entries.get(18);
        checkEntryHeader(entry, Ipv4Dst.class, false);
        Assert.assertEquals("Wrong ipv4 dst", "10.0.0.2", entry.getAugmentation(Ipv4AddressMatchEntry.class)
                .getIpv4Address().getValue());
        entry = entries.get(19);
        checkEntryHeader(entry, MplsLabel.class, false);
        Assert.assertEquals("Wrong mpls label", 17, entry.getAugmentation(MplsLabelMatchEntry.class)
                .getMplsLabel().intValue());
        entry = entries.get(20);
        checkEntryHeader(entry, MplsBos.class, false);
        Assert.assertEquals("Wrong mpls bos", true, entry.getAugmentation(BosMatchEntry.class).isBos());
        entry = entries.get(21);
        checkEntryHeader(entry, MplsTc.class, false);
        Assert.assertEquals("Wrong mpls tc", 18, entry.getAugmentation(TcMatchEntry.class)
                .getTc().intValue());
        entry = entries.get(22);
        checkEntryHeader(entry, PbbIsid.class, false);
        Assert.assertEquals("Wrong pbb isid", 20, entry.getAugmentation(IsidMatchEntry.class)
                .getIsid().intValue());
        entry = entries.get(23);
        checkEntryHeader(entry, TunnelId.class, false);
        Assert.assertArrayEquals("Wrong tunnel id", new byte[]{0, 0, 0, 0, 0, 0, 0, 21},
                entry.getAugmentation(MetadataMatchEntry.class).getMetadata());
    }

    private static void checkEntryHeader(MatchEntries entry, Class<? extends MatchField> field, boolean hasMask) {
        Assert.assertEquals("Wrong oxm class", OpenflowBasicClass.class, entry.getOxmClass());
        Assert.assertEquals("Wrong oxm field", field, entry.getOxmMatchField());
        Assert.assertEquals("Wrong hasMask", hasMask, entry.isHasMask());
    }

    /**
     * Test {@link MatchConvertorImpl#convert(Match, BigInteger)
     */
    @Test
    public void testUdpMatchConversion() {
        MatchBuilder builder = new MatchBuilder();
        UdpMatchBuilder udpMatchBuilder = new UdpMatchBuilder();
        udpMatchBuilder.setUdpSourcePort(new PortNumber(11));
        udpMatchBuilder.setUdpDestinationPort(new PortNumber(12));
        builder.setLayer4Match(udpMatchBuilder.build());
        Match match = builder.build();

        List<MatchEntries> entries = convertor.convert(match, new BigInteger("42"));
        Assert.assertEquals("Wrong entries size", 2, entries.size());
        MatchEntries entry = entries.get(0);
        checkEntryHeader(entry, UdpSrc.class, false);
        Assert.assertEquals("Wrong udp src", 11, entry.getAugmentation(PortMatchEntry.class)
                .getPort().getValue().intValue());
        entry = entries.get(1);
        checkEntryHeader(entry, UdpDst.class, false);
        Assert.assertEquals("Wrong udp dst", 12, entry.getAugmentation(PortMatchEntry.class)
                .getPort().getValue().intValue());
    }

    /**
     * Test {@link MatchConvertorImpl#convert(Match, BigInteger)
     */
    @Test
    public void testSctpMatchConversion() {
        MatchBuilder builder = new MatchBuilder();
        SctpMatchBuilder sctpMatchBuilder = new SctpMatchBuilder();
        sctpMatchBuilder.setSctpSourcePort(new PortNumber(11));
        sctpMatchBuilder.setSctpDestinationPort(new PortNumber(12));
        builder.setLayer4Match(sctpMatchBuilder.build());
        Match match = builder.build();

        List<MatchEntries> entries = convertor.convert(match, new BigInteger("42"));
        Assert.assertEquals("Wrong entries size", 2, entries.size());
        MatchEntries entry = entries.get(0);
        checkEntryHeader(entry, SctpSrc.class, false);
        Assert.assertEquals("Wrong sctp src", 11, entry.getAugmentation(PortMatchEntry.class)
                .getPort().getValue().intValue());
        entry = entries.get(1);
        checkEntryHeader(entry, SctpDst.class, false);
        Assert.assertEquals("Wrong sctp dst", 12, entry.getAugmentation(PortMatchEntry.class)
                .getPort().getValue().intValue());
    }

    /**
     * Test {@link MatchConvertorImpl#convert(Match, BigInteger)
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

        List<MatchEntries> entries = convertor.convert(match, new BigInteger("42"));
        Assert.assertEquals("Wrong entries size", 5, entries.size());
        MatchEntries entry = entries.get(0);
        checkEntryHeader(entry, ArpOp.class, false);
        Assert.assertEquals("Wrong arp op", 5, entry.getAugmentation(OpCodeMatchEntry.class)
                .getOpCode().intValue());
        entry = entries.get(1);
        checkEntryHeader(entry, ArpSpa.class, false);
        Assert.assertEquals("Wrong arp spa", "10.0.0.3", entry.getAugmentation(Ipv4AddressMatchEntry.class)
                .getIpv4Address().getValue());
        entry = entries.get(2);
        checkEntryHeader(entry, ArpTpa.class, false);
        Assert.assertEquals("Wrong arp tpa", "10.0.0.4", entry.getAugmentation(Ipv4AddressMatchEntry.class)
                .getIpv4Address().getValue());
        entry = entries.get(3);
        checkEntryHeader(entry, ArpSha.class, false);
        Assert.assertEquals("Wrong arp sha", "00:00:00:00:00:05", entry.getAugmentation(MacAddressMatchEntry.class)
                .getMacAddress().getValue());
        entry = entries.get(4);
        checkEntryHeader(entry, ArpTha.class, false);
        Assert.assertEquals("Wrong arp tha", "00:00:00:00:00:06", entry.getAugmentation(MacAddressMatchEntry.class)
                .getMacAddress().getValue());
    }

    /**
     * Test {@link MatchConvertorImpl#convert(Match, BigInteger)
     */
    @Test
    public void testArpMatchConversionWithMasks() {
        MatchBuilder builder = new MatchBuilder();
        ArpMatchBuilder arpBuilder = new ArpMatchBuilder();
        arpBuilder.setArpSourceTransportAddress(new Ipv4Prefix("10.0.0.3/8"));
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

        List<MatchEntries> entries = convertor.convert(match, new BigInteger("42"));
        Assert.assertEquals("Wrong entries size", 4, entries.size());
        MatchEntries entry = entries.get(0);
        entry = entries.get(0);
        checkEntryHeader(entry, ArpSpa.class, true);
        Assert.assertEquals("Wrong arp spa", "10.0.0.0", entry.getAugmentation(Ipv4AddressMatchEntry.class)
                .getIpv4Address().getValue());
        Assert.assertArrayEquals("Wrong arp spa mask", new byte[]{(byte) 255, 0, 0, 0},
                entry.getAugmentation(MaskMatchEntry.class).getMask());
        entry = entries.get(1);
        checkEntryHeader(entry, ArpTpa.class, true);
        Assert.assertEquals("Wrong arp tpa", "10.0.0.4", entry.getAugmentation(Ipv4AddressMatchEntry.class)
                .getIpv4Address().getValue());
        Assert.assertArrayEquals("Wrong arp tpa mask", new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 254},
                entry.getAugmentation(MaskMatchEntry.class).getMask());
        entry = entries.get(2);
        checkEntryHeader(entry, ArpSha.class, true);
        Assert.assertEquals("Wrong arp sha", "00:00:00:00:00:05", entry.getAugmentation(MacAddressMatchEntry.class)
                .getMacAddress().getValue());
        Assert.assertArrayEquals("Wrong arp sha mask", new byte[]{0, 0, 0, 0, 0, 8},
                entry.getAugmentation(MaskMatchEntry.class).getMask());
        entry = entries.get(3);
        checkEntryHeader(entry, ArpTha.class, true);
        Assert.assertEquals("Wrong arp tha", "00:00:00:00:00:06", entry.getAugmentation(MacAddressMatchEntry.class)
                .getMacAddress().getValue());
        Assert.assertArrayEquals("Wrong arp tha mask", new byte[]{0, 0, 0, 0, 0, 9},
                entry.getAugmentation(MaskMatchEntry.class).getMask());
    }

    /**
     * Test {@link MatchConvertorImpl#convert(Match, BigInteger)
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

        List<MatchEntries> entries = convertor.convert(match, new BigInteger("42"));
        Assert.assertEquals("Wrong entries size", 7, entries.size());
        MatchEntries entry = entries.get(0);
        checkEntryHeader(entry, Ipv6Src.class, true); /* will allways return true now */
        Assert.assertEquals("Wrong ipv6 src", "::1",
                entry.getAugmentation(Ipv6AddressMatchEntry.class).getIpv6Address().getValue());
        entry = entries.get(1);
        checkEntryHeader(entry, Ipv6Dst.class, false);
        Assert.assertEquals("Wrong ipv6 dst", "::2",
                entry.getAugmentation(Ipv6AddressMatchEntry.class).getIpv6Address().getValue());
        entry = entries.get(2);
        checkEntryHeader(entry, Ipv6Flabel.class, false);
        Assert.assertEquals("Wrong ipv6 flabel", 3,
                entry.getAugmentation(Ipv6FlabelMatchEntry.class).getIpv6Flabel().getValue().intValue());
        entry = entries.get(3);
        checkEntryHeader(entry, Ipv6NdTarget.class, false);
        Assert.assertEquals("Wrong ipv6 nd target", "::4",
                entry.getAugmentation(Ipv6AddressMatchEntry.class).getIpv6Address().getValue());
        entry = entries.get(4);
        checkEntryHeader(entry, Ipv6NdSll.class, false);
        Assert.assertEquals("Wrong ipv6 nd sll", "00:00:00:00:00:05",
                entry.getAugmentation(MacAddressMatchEntry.class).getMacAddress().getValue());
        entry = entries.get(5);
        checkEntryHeader(entry, Ipv6NdTll.class, false);
        Assert.assertEquals("Wrong ipv6 nd tll", "00:00:00:00:00:06",
                entry.getAugmentation(MacAddressMatchEntry.class).getMacAddress().getValue());
        entry = entries.get(6);
        checkEntryHeader(entry, Ipv6Exthdr.class, false);
        Assert.assertEquals("Wrong ipv6 ext hdr", new Ipv6ExthdrFlags(false, true, false, true, false,
                true, false, true, false), entry.getAugmentation(PseudoFieldMatchEntry.class).getPseudoField());
    }

    /**
     * Test {@link MatchConvertorImpl#convert(Match, BigInteger)
     */
    @Test
    public void testIpv6MatchConversionWithMasks() {
        MatchBuilder builder = new MatchBuilder();
        Ipv6MatchBuilder ipv6Builder = new Ipv6MatchBuilder();
        ipv6Builder.setIpv6Source(new Ipv6Prefix("::/24"));
        ipv6Builder.setIpv6Destination(new Ipv6Prefix("::/64"));
        builder.setLayer3Match(ipv6Builder.build());
        Match match = builder.build();

        List<MatchEntries> entries = convertor.convert(match, new BigInteger("42"));
        Assert.assertEquals("Wrong entries size", 2, entries.size());
        MatchEntries entry = entries.get(0);
        checkEntryHeader(entry, Ipv6Src.class, true);
        Assert.assertEquals("Wrong ipv6 src", "::",
                entry.getAugmentation(Ipv6AddressMatchEntry.class).getIpv6Address().getValue());
        Assert.assertArrayEquals("Wrong ipv6 src mask", new byte[]{(byte) 255, (byte) 255, (byte) 255, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0, 0}, entry.getAugmentation(MaskMatchEntry.class).getMask());
        entry = entries.get(1);
        checkEntryHeader(entry, Ipv6Dst.class, true);
        Assert.assertEquals("Wrong ipv6 dst", "::",
                entry.getAugmentation(Ipv6AddressMatchEntry.class).getIpv6Address().getValue());
        Assert.assertArrayEquals("Wrong ipv6 src mask", new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 255,
                (byte) 255, (byte) 255, (byte) 255, (byte) 255, 0, 0, 0, 0, 0, 0, 0, 0},
                entry.getAugmentation(MaskMatchEntry.class).getMask());
    }

    /**
     * Test {@link MatchConvertorImpl#convert(Match, BigInteger)
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

        List<MatchEntries> entries = convertor.convert(match, new BigInteger("42"));
        Assert.assertEquals("Wrong entries size", 1, entries.size());
        MatchEntries entry = entries.get(0);
        checkEntryHeader(entry, Ipv6Exthdr.class, true);
        Assert.assertEquals("Wrong ipv6 ext hdr", new Ipv6ExthdrFlags(true, false, true, false, true, false,
                true, false, true), entry.getAugmentation(PseudoFieldMatchEntry.class).getPseudoField());
        Assert.assertArrayEquals("Wrong ipv6 ext hdr mask", new byte[]{1, 2}, entry.getAugmentation(MaskMatchEntry.class)
                .getMask());
    }

    /**
     * Test {@link MatchConvertorImpl#convert(Match, BigInteger)
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
        ipv4MatchBuilder.setIpv4Source(new Ipv4Prefix("10.0.0.1/24"));
        ipv4MatchBuilder.setIpv4Destination(new Ipv4Prefix("10.0.0.2/8"));
        builder.setLayer3Match(ipv4MatchBuilder.build());
        Match match = builder.build();

        List<MatchEntries> entries = convertor.convert(match, new BigInteger("42"));
        Assert.assertEquals("Wrong entries size", 8, entries.size());
        MatchEntries entry = entries.get(0);
        checkEntryHeader(entry, Metadata.class, true);
        Assert.assertArrayEquals("Wrong metadata", new byte[]{0, 0, 0, 0, 0, 0, 0, 3},
                entry.getAugmentation(MetadataMatchEntry.class).getMetadata());
        Assert.assertArrayEquals("Wrong metadata mask", new byte[]{0, 0, 0, 0, 0, 0, 0, 15},
                entry.getAugmentation(MaskMatchEntry.class).getMask());
        entry = entries.get(1);
        checkEntryHeader(entry, EthDst.class, true);
        Assert.assertEquals("Wrong eth dst", new MacAddress("00:00:00:00:00:06"),
                entry.getAugmentation(MacAddressMatchEntry.class).getMacAddress());
        Assert.assertArrayEquals("Wrong eth dst mask", new byte[]{0, 0, 0, 0, 0, 9},
                entry.getAugmentation(MaskMatchEntry.class).getMask());
        entry = entries.get(2);
        checkEntryHeader(entry, EthSrc.class, true);
        Assert.assertEquals("Wrong eth src", new MacAddress("00:00:00:00:00:05"),
                entry.getAugmentation(MacAddressMatchEntry.class).getMacAddress());
        Assert.assertArrayEquals("Wrong eth src mask", new byte[]{0, 0, 0, 0, 0, 8},
                entry.getAugmentation(MaskMatchEntry.class).getMask());
        entry = entries.get(3);
        checkEntryHeader(entry, VlanVid.class, true);
        Assert.assertEquals("Wrong vlan id", 0, entry.getAugmentation(VlanVidMatchEntry.class)
                .getVlanVid().intValue());
        Assert.assertEquals("Wrong cfi bit", true, entry.getAugmentation(VlanVidMatchEntry.class)
                .isCfiBit());
        Assert.assertArrayEquals("Wrong vlanId mask", new byte[]{16, 0},
                entry.getAugmentation(MaskMatchEntry.class).getMask());
        entry = entries.get(4);
        checkEntryHeader(entry, Ipv4Src.class, true);
        Assert.assertEquals("Wrong ipv4 src", "10.0.0.0", entry.getAugmentation(Ipv4AddressMatchEntry.class)
                .getIpv4Address().getValue());
        Assert.assertArrayEquals("Wrong ipv4 src mask", new byte[]{(byte) 255, (byte) 255, (byte) 255, 0},
                entry.getAugmentation(MaskMatchEntry.class).getMask());
        entry = entries.get(5);
        checkEntryHeader(entry, Ipv4Dst.class, true);
        Assert.assertEquals("Wrong ipv4 dst", "10.0.0.0", entry.getAugmentation(Ipv4AddressMatchEntry.class)
                .getIpv4Address().getValue());
        Assert.assertArrayEquals("Wrong ipv4 dst mask", new byte[]{(byte) 255, 0, 0, 0},
                entry.getAugmentation(MaskMatchEntry.class).getMask());
        entry = entries.get(6);
        checkEntryHeader(entry, PbbIsid.class, true);
        Assert.assertEquals("Wrong pbb isid", 20, entry.getAugmentation(IsidMatchEntry.class)
                .getIsid().intValue());
        // FIXME - fix mask computation (length should be 3 instead of 4)
//        Assert.assertArrayEquals("Wrong pbb isid mask", new byte[]{0, 0, 8},
//                entry.getAugmentation(MaskMatchEntry.class).getMask());
        entry = entries.get(7);
        checkEntryHeader(entry, TunnelId.class, true);
        Assert.assertArrayEquals("Wrong tunnel id", new byte[]{0, 0, 0, 0, 0, 0, 0, 21},
                entry.getAugmentation(MetadataMatchEntry.class).getMetadata());
        Assert.assertArrayEquals("Wrong tunnel id mask", new byte[]{0, 0, 0, 0, 0, 0, 0, 14},
                entry.getAugmentation(MaskMatchEntry.class).getMask());
    }
}
