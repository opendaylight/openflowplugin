/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6FlowLabel;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.PacketTypeMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFieldsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TunnelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchArbitraryBitMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchArbitraryBitMaskBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.TunnelIpv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.SctpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.protocol.match.fields.PbbBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.opendaylight.ipv6.arbitrary.bitmask.fields.rev160224.Ipv6ArbitraryMask;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.PacketType;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.PacketTypeCase;
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
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Unit tests for match conversion.
 *
 * @author michal.polkorab
 */
public class MatchConvertorTest {

    private ConvertorManager converterManager;

    /**
     * Initializes OpenflowPortsUtil.
     */
    @Before
    public void startUp() {
        converterManager = ConvertorManagerFactory.createDefaultManager();
    }

    @Test
    public void testConversion() {
        MatchBuilder builder = new MatchBuilder();
        builder.setInPort(new NodeConnectorId("openflow:42:1"));
        builder.setInPhyPort(new NodeConnectorId("openflow:42:2"));
        MetadataBuilder metadataBuilder = new MetadataBuilder();
        metadataBuilder.setMetadata(Uint64.valueOf(3));
        builder.setMetadata(metadataBuilder.build());
        EthernetMatchBuilder ethernetBuilder = new EthernetMatchBuilder();
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        ethTypeBuilder.setType(new EtherType(Uint32.valueOf(4)));
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
        vlanIdBuilder.setVlanId(new VlanId(Uint16.valueOf(7)));
        vlanIdBuilder.setVlanIdPresent(true);
        vlanBuilder.setVlanId(vlanIdBuilder.build());
        vlanBuilder.setVlanPcp(new VlanPcp(Uint8.valueOf(7)));
        builder.setVlanMatch(vlanBuilder.build());
        IpMatchBuilder ipMatchBuilder = new IpMatchBuilder();
        ipMatchBuilder.setIpDscp(new Dscp(Uint8.valueOf(8)));
        ipMatchBuilder.setIpEcn(Uint8.valueOf(9));
        ipMatchBuilder.setIpProtocol(Uint8.valueOf(10));
        builder.setIpMatch(ipMatchBuilder.build());
        TcpMatchBuilder tcpMatchBuilder = new TcpMatchBuilder();
        tcpMatchBuilder.setTcpSourcePort(new PortNumber(Uint16.valueOf(11)));
        tcpMatchBuilder.setTcpDestinationPort(new PortNumber(Uint16.valueOf(12)));
        builder.setLayer4Match(tcpMatchBuilder.build());
        Icmpv4MatchBuilder icmpv4Builder = new Icmpv4MatchBuilder();
        icmpv4Builder.setIcmpv4Type(Uint8.valueOf(13));
        icmpv4Builder.setIcmpv4Code(Uint8.valueOf(14));
        builder.setIcmpv4Match(icmpv4Builder.build());
        Icmpv6MatchBuilder icmpv6Builder = new Icmpv6MatchBuilder();
        icmpv6Builder.setIcmpv6Type(Uint8.valueOf(15));
        icmpv6Builder.setIcmpv6Code(Uint8.valueOf(16));
        builder.setIcmpv6Match(icmpv6Builder.build());
        ProtocolMatchFieldsBuilder protoBuilder = new ProtocolMatchFieldsBuilder();
        protoBuilder.setMplsLabel(Uint32.valueOf(17));
        protoBuilder.setMplsTc(Uint8.valueOf(18));
        protoBuilder.setMplsBos(Uint8.valueOf(19));
        PbbBuilder pbbBuilder = new PbbBuilder();
        pbbBuilder.setPbbIsid(Uint32.valueOf(20));
        protoBuilder.setPbb(pbbBuilder.build());
        builder.setProtocolMatchFields(protoBuilder.build());
        TunnelBuilder tunnelBuilder = new TunnelBuilder();
        tunnelBuilder.setTunnelId(Uint64.valueOf(21));
        builder.setTunnel(tunnelBuilder.build());
        Ipv4MatchBuilder ipv4MatchBuilder = new Ipv4MatchBuilder();
        ipv4MatchBuilder.setIpv4Source(new Ipv4Prefix("10.0.0.1/32"));
        ipv4MatchBuilder.setIpv4Destination(new Ipv4Prefix("10.0.0.2/32"));
        builder.setLayer3Match(ipv4MatchBuilder.build());
        Match match = builder.build();

        Optional<List<MatchEntry>> entriesOptional =
                converterManager.convert(match, new VersionConvertorData(OFConstants.OFP_VERSION_1_3));
        List<MatchEntry> entries = entriesOptional.orElseThrow();
        assertEquals("Wrong entries size", 24, entries.size());
        MatchEntry entry = entries.get(0);
        checkEntryHeader(entry, InPort.VALUE, false);
        assertEquals("Wrong in port", 1, ((InPortCase) entry.getMatchEntryValue()).getInPort()
                .getPortNumber().getValue().intValue());

        entry = entries.get(1);
        checkEntryHeader(entry, InPhyPort.VALUE, false);
        assertEquals("Wrong in phy port", 2, ((InPhyPortCase) entry.getMatchEntryValue())
                .getInPhyPort().getPortNumber().getValue().intValue());

        entry = entries.get(2);
        checkEntryHeader(entry, Metadata.VALUE, false);
        assertArrayEquals("Wrong metadata", new byte[]{0, 0, 0, 0, 0, 0, 0, 3},
                ((MetadataCase) entry.getMatchEntryValue()).getMetadata().getMetadata());
        entry = entries.get(3);
        checkEntryHeader(entry, EthDst.VALUE, false);
        assertEquals("Wrong eth dst", new MacAddress("00:00:00:00:00:06"),
                ((EthDstCase) entry.getMatchEntryValue()).getEthDst().getMacAddress());
        entry = entries.get(4);
        checkEntryHeader(entry, EthSrc.VALUE, false);
        assertEquals("Wrong eth src", new MacAddress("00:00:00:00:00:05"),
                ((EthSrcCase) entry.getMatchEntryValue()).getEthSrc().getMacAddress());
        entry = entries.get(5);
        checkEntryHeader(entry, EthType.VALUE, false);
        assertEquals("Wrong eth type", 4, ((EthTypeCase) entry.getMatchEntryValue())
                .getEthType().getEthType().getValue().intValue());
        entry = entries.get(6);
        checkEntryHeader(entry, VlanVid.VALUE, false);
        assertEquals("Wrong vlan id", 7, ((VlanVidCase) entry.getMatchEntryValue())
                .getVlanVid().getVlanVid().intValue());
        assertEquals("Wrong cfi bit", true, ((VlanVidCase) entry.getMatchEntryValue()).getVlanVid().getCfiBit());
        entry = entries.get(7);
        checkEntryHeader(entry,
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanPcp.VALUE, false);
        assertEquals("Wrong vlan pcp", 7, ((VlanPcpCase) entry.getMatchEntryValue())
                .getVlanPcp().getVlanPcp().intValue());
        entry = entries.get(8);
        checkEntryHeader(entry, IpDscp.VALUE, false);
        assertEquals("Wrong ip dscp", 8, ((IpDscpCase) entry.getMatchEntryValue())
                .getIpDscp().getDscp().getValue().intValue());
        entry = entries.get(9);
        checkEntryHeader(entry, IpEcn.VALUE, false);
        assertEquals("Wrong ip ecn", 9, ((IpEcnCase) entry.getMatchEntryValue())
                .getIpEcn().getEcn().intValue());
        entry = entries.get(10);
        checkEntryHeader(entry, IpProto.VALUE, false);
        assertEquals("Wrong ip proto", 10, ((IpProtoCase) entry.getMatchEntryValue())
                .getIpProto().getProtocolNumber().intValue());
        entry = entries.get(11);
        checkEntryHeader(entry, TcpSrc.VALUE, false);
        assertEquals("Wrong tcp src", 11, ((TcpSrcCase) entry.getMatchEntryValue())
                .getTcpSrc().getPort().getValue().intValue());
        entry = entries.get(12);
        checkEntryHeader(entry, TcpDst.VALUE, false);
        assertEquals("Wrong tcp dst", 12, ((TcpDstCase) entry.getMatchEntryValue())
                .getTcpDst().getPort().getValue().intValue());
        entry = entries.get(13);
        checkEntryHeader(entry, Icmpv4Type.VALUE, false);
        assertEquals("Wrong icmpv4 type", 13, ((Icmpv4TypeCase) entry.getMatchEntryValue())
                .getIcmpv4Type().getIcmpv4Type().intValue());
        entry = entries.get(14);
        checkEntryHeader(entry, Icmpv4Code.VALUE, false);
        assertEquals("Wrong icmpv4 code", 14, ((Icmpv4CodeCase) entry.getMatchEntryValue())
                .getIcmpv4Code().getIcmpv4Code().intValue());
        entry = entries.get(15);
        checkEntryHeader(entry, Icmpv6Type.VALUE, false);
        assertEquals("Wrong icmpv6 type", 15, ((Icmpv6TypeCase) entry.getMatchEntryValue())
                .getIcmpv6Type().getIcmpv6Type().intValue());
        entry = entries.get(16);
        checkEntryHeader(entry, Icmpv6Code.VALUE, false);
        assertEquals("Wrong icmpv6 code", 16, ((Icmpv6CodeCase) entry.getMatchEntryValue())
                .getIcmpv6Code().getIcmpv6Code().intValue());
        entry = entries.get(17);
        checkEntryHeader(entry, Ipv4Src.VALUE, false);
        assertEquals("Wrong ipv4 src", "10.0.0.1", ((Ipv4SrcCase) entry.getMatchEntryValue())
                .getIpv4Src().getIpv4Address().getValue());
        entry = entries.get(18);
        checkEntryHeader(entry, Ipv4Dst.VALUE, false);
        assertEquals("Wrong ipv4 dst", "10.0.0.2", ((Ipv4DstCase) entry.getMatchEntryValue())
                .getIpv4Dst().getIpv4Address().getValue());
        entry = entries.get(19);
        checkEntryHeader(entry, MplsLabel.VALUE, false);
        assertEquals("Wrong mpls label", 17, ((MplsLabelCase) entry.getMatchEntryValue())
                .getMplsLabel().getMplsLabel().intValue());
        entry = entries.get(20);
        checkEntryHeader(entry, MplsBos.VALUE, false);
        assertEquals("Wrong mpls bos", true, ((MplsBosCase) entry.getMatchEntryValue()).getMplsBos().getBos());
        entry = entries.get(21);
        checkEntryHeader(entry, MplsTc.VALUE, false);
        assertEquals("Wrong mpls tc", 18, ((MplsTcCase) entry.getMatchEntryValue())
                .getMplsTc().getTc().intValue());
        entry = entries.get(22);
        checkEntryHeader(entry, PbbIsid.VALUE, false);
        assertEquals("Wrong pbb isid", 20, ((PbbIsidCase) entry.getMatchEntryValue())
                .getPbbIsid().getIsid().intValue());
        entry = entries.get(23);
        checkEntryHeader(entry, TunnelId.VALUE, false);
        assertArrayEquals("Wrong tunnel id", new byte[]{0, 0, 0, 0, 0, 0, 0, 21},
                ((TunnelIdCase) entry.getMatchEntryValue()).getTunnelId().getTunnelId());
    }

    private static void checkEntryHeader(final MatchEntry entry, final MatchField field,
            final boolean hasMask) {
        assertEquals("Wrong oxm class", OpenflowBasicClass.VALUE, entry.getOxmClass());
        assertEquals("Wrong oxm field", field, entry.getOxmMatchField());
        assertEquals("Wrong hasMask", hasMask, entry.getHasMask());
    }

    @Test
    public void testIpv4MatchArbitraryBitMaskwithNoMask() {
        MatchBuilder builder = new MatchBuilder();
        Ipv4MatchArbitraryBitMaskBuilder ipv4MatchArbitraryBitMaskBuilder = new Ipv4MatchArbitraryBitMaskBuilder();
        ipv4MatchArbitraryBitMaskBuilder.setIpv4SourceAddressNoMask(new Ipv4Address("10.2.2.2"));
        ipv4MatchArbitraryBitMaskBuilder.setIpv4DestinationAddressNoMask(new Ipv4Address("10.1.1.1"));
        builder.setLayer3Match(ipv4MatchArbitraryBitMaskBuilder.build());
        Match match = builder.build();

        Optional<List<MatchEntry>> entriesOptional = converterManager.convert(match,
                new VersionConvertorData(OFConstants.OFP_VERSION_1_3));
        List<MatchEntry> entries = entriesOptional.orElseThrow();
        assertEquals("Wrong entries size", 2, entries.size());

        MatchEntry entry = entries.get(0);
        checkEntryHeader(entry, Ipv4Src.VALUE, false);
        assertEquals("wrong Ipv4Address source", "10.2.2.2",
                ((Ipv4SrcCase) entry.getMatchEntryValue()).getIpv4Src().getIpv4Address().getValue());
        entry = entries.get(1);
        checkEntryHeader(entry, Ipv4Dst.VALUE, false);
        assertEquals("wrong Ipv4Address destination", "10.1.1.1",
                ((Ipv4DstCase) entry.getMatchEntryValue()).getIpv4Dst().getIpv4Address().getValue());
    }

    @Test
    public void testIpv4MatchArbitraryBitMaskwithMask() {
        Ipv4MatchArbitraryBitMaskBuilder ipv4MatchArbitraryBitMaskBuilder = new Ipv4MatchArbitraryBitMaskBuilder();
        ipv4MatchArbitraryBitMaskBuilder.setIpv4SourceAddressNoMask(new Ipv4Address("10.2.2.2"));
        ipv4MatchArbitraryBitMaskBuilder.setIpv4SourceArbitraryBitmask(new DottedQuad("0.0.255.0"));
        ipv4MatchArbitraryBitMaskBuilder.setIpv4DestinationAddressNoMask(new Ipv4Address("10.1.1.1"));
        ipv4MatchArbitraryBitMaskBuilder.setIpv4DestinationArbitraryBitmask(new DottedQuad("0.240.0.0"));

        MatchBuilder builder = new MatchBuilder();
        builder.setLayer3Match(ipv4MatchArbitraryBitMaskBuilder.build());
        Match match = builder.build();

        Optional<List<MatchEntry>> entriesOptional = converterManager.convert(match,
                new VersionConvertorData(OFConstants.OFP_VERSION_1_3));
        List<MatchEntry> entries = entriesOptional.orElseThrow();
        assertEquals("Wrong entries size", 2, entries.size());

        MatchEntry entry = entries.get(0);
        checkEntryHeader(entry, Ipv4Src.VALUE, true);
        assertEquals("wrong Ipv4Address source", "10.2.2.2",
                ((Ipv4SrcCase) entry.getMatchEntryValue()).getIpv4Src().getIpv4Address().getValue());
        entry = entries.get(1);
        checkEntryHeader(entry, Ipv4Dst.VALUE, true);
        assertEquals("wrong Ipv4Adress destination", "10.1.1.1",
                ((Ipv4DstCase) entry.getMatchEntryValue()).getIpv4Dst().getIpv4Address().getValue());
    }

    @Test
    public void testUdpMatchConversion() {
        MatchBuilder builder = new MatchBuilder();
        UdpMatchBuilder udpMatchBuilder = new UdpMatchBuilder();
        udpMatchBuilder.setUdpSourcePort(new PortNumber(Uint16.valueOf(11)));
        udpMatchBuilder.setUdpDestinationPort(new PortNumber(Uint16.valueOf(12)));
        builder.setLayer4Match(udpMatchBuilder.build());
        Match match = builder.build();

        Optional<List<MatchEntry>> entriesOptional =
                converterManager.convert(match, new VersionConvertorData(OFConstants.OFP_VERSION_1_3));
        List<MatchEntry> entries = entriesOptional.orElseThrow();
        assertEquals("Wrong entries size", 2, entries.size());
        MatchEntry entry = entries.get(0);
        checkEntryHeader(entry, UdpSrc.VALUE, false);
        assertEquals("Wrong udp src", 11, ((UdpSrcCase) entry.getMatchEntryValue()).getUdpSrc()
                .getPort().getValue().intValue());
        entry = entries.get(1);
        checkEntryHeader(entry, UdpDst.VALUE, false);
        assertEquals("Wrong udp dst", 12, ((UdpDstCase) entry.getMatchEntryValue())
                .getUdpDst().getPort().getValue().intValue());
    }

    @Test
    public void testTunnelIpv4MatchConversion() {
        MatchBuilder builder = new MatchBuilder();
        TunnelIpv4MatchBuilder tunnelIpv4MatchBuilder = new TunnelIpv4MatchBuilder();
        tunnelIpv4MatchBuilder.setTunnelIpv4Source(new Ipv4Prefix("10.0.0.1/32"));
        tunnelIpv4MatchBuilder.setTunnelIpv4Destination(new Ipv4Prefix("10.0.0.2/32"));
        builder.setLayer3Match(tunnelIpv4MatchBuilder.build());
        Match match = builder.build();

        Optional<List<MatchEntry>> entriesOptional =
                converterManager.convert(match, new VersionConvertorData(OFConstants.OFP_VERSION_1_3));
        List<MatchEntry> entries = entriesOptional.orElseThrow();
        assertEquals("Wrong entries size", 2, entries.size());
        MatchEntry entry = entries.get(0);
        checkEntryHeader(entry, Ipv4Src.VALUE, false);
        assertEquals("Wrong ipv4 tunnel src", "10.0.0.1", ((Ipv4SrcCase) entry.getMatchEntryValue()).getIpv4Src()
                .getIpv4Address().getValue());
        entry = entries.get(1);
        checkEntryHeader(entry, Ipv4Dst.VALUE, false);
        assertEquals("Wrong ipv4 tunnel dst", "10.0.0.2", ((Ipv4DstCase) entry.getMatchEntryValue()).getIpv4Dst()
                .getIpv4Address().getValue());
    }

    @Test
    public void testSctpMatchConversion() {
        MatchBuilder builder = new MatchBuilder();
        SctpMatchBuilder sctpMatchBuilder = new SctpMatchBuilder();
        sctpMatchBuilder.setSctpSourcePort(new PortNumber(Uint16.valueOf(11)));
        sctpMatchBuilder.setSctpDestinationPort(new PortNumber(Uint16.valueOf(12)));
        builder.setLayer4Match(sctpMatchBuilder.build());
        Match match = builder.build();

        Optional<List<MatchEntry>> entriesOptional =
                converterManager.convert(match, new VersionConvertorData(OFConstants.OFP_VERSION_1_3));
        List<MatchEntry> entries = entriesOptional.orElseThrow();
        assertEquals("Wrong entries size", 2, entries.size());
        MatchEntry entry = entries.get(0);
        checkEntryHeader(entry, SctpSrc.VALUE, false);
        assertEquals("Wrong sctp src", 11, ((SctpSrcCase) entry.getMatchEntryValue()).getSctpSrc()
                .getPort().getValue().intValue());
        entry = entries.get(1);
        checkEntryHeader(entry, SctpDst.VALUE, false);
        assertEquals("Wrong sctp dst", 12, ((SctpDstCase) entry.getMatchEntryValue())
                .getSctpDst().getPort().getValue().intValue());
    }

    @Test
    public void testArpMatchConversion() {
        ArpMatchBuilder arpBuilder = new ArpMatchBuilder();
        arpBuilder.setArpOp(Uint16.valueOf(5));
        arpBuilder.setArpSourceTransportAddress(new Ipv4Prefix("10.0.0.3/32"));
        arpBuilder.setArpTargetTransportAddress(new Ipv4Prefix("10.0.0.4/32"));
        ArpSourceHardwareAddressBuilder srcHwBuilder = new ArpSourceHardwareAddressBuilder();
        srcHwBuilder.setAddress(new MacAddress("00:00:00:00:00:05"));
        arpBuilder.setArpSourceHardwareAddress(srcHwBuilder.build());
        ArpTargetHardwareAddressBuilder dstHwBuilder = new ArpTargetHardwareAddressBuilder();
        dstHwBuilder.setAddress(new MacAddress("00:00:00:00:00:06"));
        arpBuilder.setArpTargetHardwareAddress(dstHwBuilder.build());

        MatchBuilder builder = new MatchBuilder();
        builder.setLayer3Match(arpBuilder.build());
        Match match = builder.build();

        Optional<List<MatchEntry>> entriesOptional =
                converterManager.convert(match, new VersionConvertorData(OFConstants.OFP_VERSION_1_3));
        List<MatchEntry> entries = entriesOptional.orElseThrow();
        assertEquals("Wrong entries size", 5, entries.size());
        MatchEntry entry = entries.get(0);
        checkEntryHeader(entry, ArpOp.VALUE, false);
        assertEquals("Wrong arp op", 5, ((ArpOpCase) entry.getMatchEntryValue())
                .getArpOp().getOpCode().intValue());
        entry = entries.get(1);
        checkEntryHeader(entry, ArpSpa.VALUE, false);
        assertEquals("Wrong arp spa", "10.0.0.3", ((ArpSpaCase) entry.getMatchEntryValue())
                .getArpSpa().getIpv4Address().getValue());
        entry = entries.get(2);
        checkEntryHeader(entry, ArpTpa.VALUE, false);
        assertEquals("Wrong arp tpa", "10.0.0.4", ((ArpTpaCase) entry.getMatchEntryValue())
                .getArpTpa().getIpv4Address().getValue());
        entry = entries.get(3);
        checkEntryHeader(entry, ArpSha.VALUE, false);
        assertEquals("Wrong arp sha", "00:00:00:00:00:05", ((ArpShaCase) entry.getMatchEntryValue())
                .getArpSha().getMacAddress().getValue());
        entry = entries.get(4);
        checkEntryHeader(entry, ArpTha.VALUE, false);
        assertEquals("Wrong arp tha", "00:00:00:00:00:06", ((ArpThaCase) entry.getMatchEntryValue())
                .getArpTha().getMacAddress().getValue());
    }

    @Test
    public void testArpMatchConversionWithMasks() {
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

        MatchBuilder builder = new MatchBuilder();
        builder.setLayer3Match(arpBuilder.build());
        Match match = builder.build();

        Optional<List<MatchEntry>> entriesOptional =
                converterManager.convert(match, new VersionConvertorData(OFConstants.OFP_VERSION_1_3));
        List<MatchEntry> entries = entriesOptional.orElseThrow();
        assertEquals("Wrong entries size", 4, entries.size());
        MatchEntry entry = entries.get(0);
        entry = entries.get(0);
        checkEntryHeader(entry, ArpSpa.VALUE, true);
        assertEquals("Wrong arp spa", "10.0.0.0", ((ArpSpaCase) entry.getMatchEntryValue())
                .getArpSpa().getIpv4Address().getValue());
        assertArrayEquals("Wrong arp spa mask", new byte[]{(byte) 255, 0, 0, 0},
                ((ArpSpaCase) entry.getMatchEntryValue()).getArpSpa().getMask());
        entry = entries.get(1);
        checkEntryHeader(entry, ArpTpa.VALUE, true);
        assertEquals("Wrong arp tpa", "10.0.0.4", ((ArpTpaCase) entry.getMatchEntryValue()).getArpTpa()
                .getIpv4Address().getValue());
        assertArrayEquals("Wrong arp tpa mask", new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 254},
                ((ArpTpaCase) entry.getMatchEntryValue()).getArpTpa().getMask());
        entry = entries.get(2);
        checkEntryHeader(entry, ArpSha.VALUE, true);
        assertEquals("Wrong arp sha", "00:00:00:00:00:05", ((ArpShaCase) entry.getMatchEntryValue())
                .getArpSha().getMacAddress().getValue());
        assertArrayEquals("Wrong arp sha mask", new byte[]{0, 0, 0, 0, 0, 8},
                ((ArpShaCase) entry.getMatchEntryValue()).getArpSha().getMask());
        entry = entries.get(3);
        checkEntryHeader(entry, ArpTha.VALUE, true);
        assertEquals("Wrong arp tha", "00:00:00:00:00:06", ((ArpThaCase) entry.getMatchEntryValue()).getArpTha()
                .getMacAddress().getValue());
        assertArrayEquals("Wrong arp tha mask", new byte[]{0, 0, 0, 0, 0, 9},
                ((ArpThaCase) entry.getMatchEntryValue()).getArpTha().getMask());
    }

    @Test
    public void testIpv6MatchConversion() {
        Ipv6MatchBuilder ipv6Builder = new Ipv6MatchBuilder();
        ipv6Builder.setIpv6Source(new Ipv6Prefix("::1/128"));
        ipv6Builder.setIpv6Destination(new Ipv6Prefix("::2/128"));
        Ipv6LabelBuilder ipv6LabelBuilder = new Ipv6LabelBuilder();
        ipv6LabelBuilder.setIpv6Flabel(new Ipv6FlowLabel(Uint32.valueOf(3)));
        ipv6Builder.setIpv6Label(ipv6LabelBuilder.build());
        ipv6Builder.setIpv6NdTarget(new Ipv6Address("::4"));
        ipv6Builder.setIpv6NdSll(new MacAddress("00:00:00:00:00:05"));
        ipv6Builder.setIpv6NdTll(new MacAddress("00:00:00:00:00:06"));
        Ipv6ExtHeaderBuilder extHdrBuilder = new Ipv6ExtHeaderBuilder();
        extHdrBuilder.setIpv6Exthdr(Uint16.valueOf(153));
        ipv6Builder.setIpv6ExtHeader(extHdrBuilder.build());

        MatchBuilder builder = new MatchBuilder();
        builder.setLayer3Match(ipv6Builder.build());
        Match match = builder.build();

        Optional<List<MatchEntry>> entriesOptional =
                converterManager.convert(match, new VersionConvertorData(OFConstants.OFP_VERSION_1_3));
        List<MatchEntry> entries = entriesOptional.orElseThrow();
        assertEquals("Wrong entries size", 7, entries.size());
        MatchEntry entry = entries.get(0);
        /* Due to conversion ambiguities, we always get "has mask" because
         * an ip with no mask and prefix with /128 (or 32 in v4) cannot
         * be distinguished */
        checkEntryHeader(entry, Ipv6Src.VALUE, false);
        assertEquals("Wrong ipv6 src", "::1",
                ((Ipv6SrcCase) entry.getMatchEntryValue()).getIpv6Src().getIpv6Address().getValue());
        entry = entries.get(1);
        checkEntryHeader(entry, Ipv6Dst.VALUE, false);
        assertEquals("Wrong ipv6 dst", "::2",
                ((Ipv6DstCase) entry.getMatchEntryValue()).getIpv6Dst().getIpv6Address().getValue());
        entry = entries.get(2);
        checkEntryHeader(entry, Ipv6Flabel.VALUE, false);
        assertEquals("Wrong ipv6 flabel", 3,
                ((Ipv6FlabelCase) entry.getMatchEntryValue()).getIpv6Flabel().getIpv6Flabel().getValue().intValue());
        entry = entries.get(3);
        checkEntryHeader(entry, Ipv6NdTarget.VALUE, false);
        assertEquals("Wrong ipv6 nd target", "::4",
                ((Ipv6NdTargetCase) entry.getMatchEntryValue()).getIpv6NdTarget().getIpv6Address().getValue());
        entry = entries.get(4);
        checkEntryHeader(entry, Ipv6NdSll.VALUE, false);
        assertEquals("Wrong ipv6 nd sll", "00:00:00:00:00:05",
                ((Ipv6NdSllCase) entry.getMatchEntryValue()).getIpv6NdSll().getMacAddress().getValue());
        entry = entries.get(5);
        checkEntryHeader(entry, Ipv6NdTll.VALUE, false);
        assertEquals("Wrong ipv6 nd tll", "00:00:00:00:00:06",
                ((Ipv6NdTllCase) entry.getMatchEntryValue()).getIpv6NdTll().getMacAddress().getValue());
        entry = entries.get(6);
        checkEntryHeader(entry, Ipv6Exthdr.VALUE, false);
        assertEquals("Wrong ipv6 ext hdr", new Ipv6ExthdrFlags(false, true, false, true, false,
            true, false, true, false), ((Ipv6ExthdrCase) entry.getMatchEntryValue()).getIpv6Exthdr().getPseudoField());
    }

    @Test
    public void testIpv6MatchConversionWithMasks() {
        MatchBuilder builder = new MatchBuilder();
        Ipv6MatchBuilder ipv6Builder = new Ipv6MatchBuilder();
        ipv6Builder.setIpv6Source(new Ipv6Prefix("::/24"));
        ipv6Builder.setIpv6Destination(new Ipv6Prefix("::/64"));
        builder.setLayer3Match(ipv6Builder.build());
        Match match = builder.build();

        Optional<List<MatchEntry>> entriesOptional =
                converterManager.convert(match, new VersionConvertorData(OFConstants.OFP_VERSION_1_3));
        List<MatchEntry> entries = entriesOptional.orElseThrow();
        assertEquals("Wrong entries size", 2, entries.size());
        MatchEntry entry = entries.get(0);
        checkEntryHeader(entry, Ipv6Src.VALUE, true);
        assertEquals("Wrong ipv6 src", "::",
                ((Ipv6SrcCase) entry.getMatchEntryValue()).getIpv6Src().getIpv6Address().getValue());
        assertArrayEquals("Wrong ipv6 src mask", new byte[]{(byte) 255, (byte) 255, (byte) 255, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0}, ((Ipv6SrcCase) entry.getMatchEntryValue()).getIpv6Src().getMask());
        entry = entries.get(1);
        checkEntryHeader(entry, Ipv6Dst.VALUE, true);
        assertEquals("Wrong ipv6 dst", "::",
                ((Ipv6DstCase) entry.getMatchEntryValue()).getIpv6Dst().getIpv6Address().getValue());
        assertArrayEquals("Wrong ipv6 src mask", new byte[]{(byte) 255, (byte) 255, (byte) 255, (byte) 255,
            (byte) 255, (byte) 255, (byte) 255, (byte) 255, 0, 0, 0, 0, 0, 0, 0, 0},
            ((Ipv6DstCase) entry.getMatchEntryValue()).getIpv6Dst().getMask());
    }

    @Test
    public void testIpv6ExtHeaderConversion() {
        Ipv6MatchBuilder ipv6Builder = new Ipv6MatchBuilder();
        Ipv6ExtHeaderBuilder extHdrBuilder = new Ipv6ExtHeaderBuilder();
        extHdrBuilder.setIpv6Exthdr(Uint16.valueOf(358));
        extHdrBuilder.setIpv6ExthdrMask(Uint16.valueOf(258));
        ipv6Builder.setIpv6ExtHeader(extHdrBuilder.build());

        MatchBuilder builder = new MatchBuilder();
        builder.setLayer3Match(ipv6Builder.build());
        Match match = builder.build();

        Optional<List<MatchEntry>> entriesOptional =
                converterManager.convert(match, new VersionConvertorData(OFConstants.OFP_VERSION_1_3));
        List<MatchEntry> entries = entriesOptional.orElseThrow();
        assertEquals("Wrong entries size", 1, entries.size());
        MatchEntry entry = entries.get(0);
        checkEntryHeader(entry, Ipv6Exthdr.VALUE, true);
        assertEquals("Wrong ipv6 ext hdr", new Ipv6ExthdrFlags(true, false, true, false, true, false,
                true, false, true), ((Ipv6ExthdrCase) entry.getMatchEntryValue()).getIpv6Exthdr().getPseudoField());
        assertArrayEquals("Wrong ipv6 ext hdr mask", new byte[]{1, 2},
                ((Ipv6ExthdrCase) entry.getMatchEntryValue()).getIpv6Exthdr().getMask());
    }

    @Test
    public void testConversionWithMasks() {
        MatchBuilder builder = new MatchBuilder();
        MetadataBuilder metadataBuilder = new MetadataBuilder();
        metadataBuilder.setMetadata(Uint64.valueOf(3));
        metadataBuilder.setMetadataMask(Uint64.valueOf(15));
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
        vlanIdBuilder.setVlanId(new VlanId(Uint16.ZERO));
        vlanIdBuilder.setVlanIdPresent(true);
        vlanBuilder.setVlanId(vlanIdBuilder.build());
        builder.setVlanMatch(vlanBuilder.build());
        ProtocolMatchFieldsBuilder protoBuilder = new ProtocolMatchFieldsBuilder();
        PbbBuilder pbbBuilder = new PbbBuilder();
        pbbBuilder.setPbbIsid(Uint32.valueOf(20));
        pbbBuilder.setPbbMask(Uint32.valueOf(8));
        protoBuilder.setPbb(pbbBuilder.build());
        builder.setProtocolMatchFields(protoBuilder.build());
        TunnelBuilder tunnelBuilder = new TunnelBuilder();
        tunnelBuilder.setTunnelId(Uint64.valueOf(21));
        tunnelBuilder.setTunnelMask(Uint64.valueOf(14));
        builder.setTunnel(tunnelBuilder.build());
        Ipv4MatchBuilder ipv4MatchBuilder = new Ipv4MatchBuilder();
        ipv4MatchBuilder.setIpv4Source(new Ipv4Prefix("10.0.0.0/24"));
        ipv4MatchBuilder.setIpv4Destination(new Ipv4Prefix("10.0.0.0/8"));
        builder.setLayer3Match(ipv4MatchBuilder.build());
        Match match = builder.build();

        Optional<List<MatchEntry>> entriesOptional =
                converterManager.convert(match, new VersionConvertorData(OFConstants.OFP_VERSION_1_3));
        List<MatchEntry> entries = entriesOptional.orElseThrow();
        assertEquals("Wrong entries size", 8, entries.size());
        MatchEntry entry = entries.get(0);
        checkEntryHeader(entry, Metadata.VALUE, true);
        assertArrayEquals("Wrong metadata", new byte[]{0, 0, 0, 0, 0, 0, 0, 3},
                ((MetadataCase) entry.getMatchEntryValue()).getMetadata().getMetadata());
        assertArrayEquals("Wrong metadata mask", new byte[]{0, 0, 0, 0, 0, 0, 0, 15},
                ((MetadataCase) entry.getMatchEntryValue()).getMetadata().getMask());
        entry = entries.get(1);
        checkEntryHeader(entry, EthDst.VALUE, true);
        assertEquals("Wrong eth dst", new MacAddress("00:00:00:00:00:06"),
                ((EthDstCase) entry.getMatchEntryValue()).getEthDst().getMacAddress());
        assertArrayEquals("Wrong eth dst mask", new byte[]{0, 0, 0, 0, 0, 9},
                ((EthDstCase) entry.getMatchEntryValue()).getEthDst().getMask());
        entry = entries.get(2);
        checkEntryHeader(entry, EthSrc.VALUE, true);
        assertEquals("Wrong eth src", new MacAddress("00:00:00:00:00:05"),
                ((EthSrcCase) entry.getMatchEntryValue()).getEthSrc().getMacAddress());
        assertArrayEquals("Wrong eth src mask", new byte[]{0, 0, 0, 0, 0, 8},
                ((EthSrcCase) entry.getMatchEntryValue()).getEthSrc().getMask());
        entry = entries.get(3);
        checkEntryHeader(entry, VlanVid.VALUE, true);
        assertEquals("Wrong vlan id", 0, ((VlanVidCase) entry.getMatchEntryValue()).getVlanVid()
                .getVlanVid().intValue());
        assertEquals("Wrong cfi bit", true, ((VlanVidCase) entry.getMatchEntryValue()).getVlanVid().getCfiBit());
        assertArrayEquals("Wrong vlanId mask", new byte[]{16, 0},
                ((VlanVidCase) entry.getMatchEntryValue()).getVlanVid().getMask());
        entry = entries.get(4);
        checkEntryHeader(entry, Ipv4Src.VALUE, true);
        assertEquals("Wrong ipv4 src", "10.0.0.0", ((Ipv4SrcCase) entry.getMatchEntryValue())
                .getIpv4Src().getIpv4Address().getValue());
        assertArrayEquals("Wrong ipv4 src mask", new byte[]{(byte) 255, (byte) 255, (byte) 255, 0},
                ((Ipv4SrcCase) entry.getMatchEntryValue()).getIpv4Src().getMask());
        entry = entries.get(5);
        checkEntryHeader(entry, Ipv4Dst.VALUE, true);
        assertEquals("Wrong ipv4 dst", "10.0.0.0", ((Ipv4DstCase) entry.getMatchEntryValue())
                .getIpv4Dst().getIpv4Address().getValue());
        assertArrayEquals("Wrong ipv4 dst mask", new byte[]{(byte) 255, 0, 0, 0},
                ((Ipv4DstCase) entry.getMatchEntryValue()).getIpv4Dst().getMask());
        entry = entries.get(6);
        checkEntryHeader(entry, PbbIsid.VALUE, true);
        assertEquals("Wrong pbb isid", 20, ((PbbIsidCase) entry.getMatchEntryValue())
                .getPbbIsid().getIsid().intValue());
        assertArrayEquals("Wrong pbb isid mask", new byte[]{0, 0, 8},
                ((PbbIsidCase) entry.getMatchEntryValue()).getPbbIsid().getMask());
        entry = entries.get(7);
        checkEntryHeader(entry, TunnelId.VALUE, true);
        assertArrayEquals("Wrong tunnel id", new byte[]{0, 0, 0, 0, 0, 0, 0, 21},
                ((TunnelIdCase) entry.getMatchEntryValue()).getTunnelId().getTunnelId());
        assertArrayEquals("Wrong tunnel id mask", new byte[]{0, 0, 0, 0, 0, 0, 0, 14},
                ((TunnelIdCase) entry.getMatchEntryValue()).getTunnelId().getMask());
    }

    @Test
    public void testIpv6MatchArbitraryBitMask() {
        Ipv6MatchArbitraryBitMaskBuilder ipv6MatchArbitraryBitMaskBuilder = new Ipv6MatchArbitraryBitMaskBuilder();
        ipv6MatchArbitraryBitMaskBuilder
                .setIpv6SourceAddressNoMask(new Ipv6Address("fbA0:FFB6:FFF0:FFF0:FFF0:FFF0:FFF0:AFF0"));
        ipv6MatchArbitraryBitMaskBuilder
                .setIpv6SourceArbitraryBitmask(new Ipv6ArbitraryMask("fbA0:FFB6:FFF0:FFF0:FFF0:FFF0:FFF0:A555"));
        ipv6MatchArbitraryBitMaskBuilder
                .setIpv6DestinationAddressNoMask(new Ipv6Address("fbA0:FFB6:FFF0:FFF0:FFF0:FFF0:FFF0:AFF0"));
        ipv6MatchArbitraryBitMaskBuilder
                .setIpv6DestinationArbitraryBitmask(new Ipv6ArbitraryMask("fbA0:FFB6:FFF0:FFF0:FFF0:FFF0:FFF0:A555"));

        MatchBuilder builder = new MatchBuilder();
        builder.setLayer3Match(ipv6MatchArbitraryBitMaskBuilder.build());
        Match match = builder.build();

        Optional<List<MatchEntry>> entriesOptional = converterManager.convert(match,
                new VersionConvertorData(OFConstants.OFP_VERSION_1_3));
        List<MatchEntry> entries = entriesOptional.orElseThrow();
        assertEquals("Wrong entries size", 2, entries.size());

        MatchEntry entry = entries.get(0);
        checkEntryHeader(entry, Ipv6Src.VALUE, true);
        assertEquals("wrong Ipv6Adress source", "fbA0:FFB6:FFF0:FFF0:FFF0:FFF0:FFF0:AFF0",
                ((Ipv6SrcCase) entry.getMatchEntryValue()).getIpv6Src().getIpv6Address().getValue());
        entry = entries.get(1);
        checkEntryHeader(entry, Ipv6Dst.VALUE, true);
        assertEquals("wrong Ipv6Adress destination", "fbA0:FFB6:FFF0:FFF0:FFF0:FFF0:FFF0:AFF0",
                ((Ipv6DstCase) entry.getMatchEntryValue()).getIpv6Dst().getIpv6Address().getValue());
    }

    @Test
    public void testPacketTypeConversion() {
        MatchBuilder builder = new MatchBuilder();
        builder.setPacketTypeMatch(new PacketTypeMatchBuilder().setPacketType(Uint32.valueOf(0x1894f)).build());
        Match match = builder.build();

        Optional<List<MatchEntry>> entriesOptional =
                converterManager.convert(match, new VersionConvertorData(OFConstants.OFP_VERSION_1_3));
        List<MatchEntry> entries = entriesOptional.orElseThrow();
        assertEquals("Wrong entries size", 1, entries.size());
        MatchEntry entry = entries.get(0);
        checkEntryHeader(entry, PacketType.VALUE, false);
        assertEquals("Wrong in port", 0x1894f,
                ((PacketTypeCase) entry.getMatchEntryValue()).getPacketType().getPacketType().longValue());
    }
}

