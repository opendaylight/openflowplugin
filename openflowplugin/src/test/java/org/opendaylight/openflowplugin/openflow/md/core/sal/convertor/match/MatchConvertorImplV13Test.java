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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.SctpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.BosMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.BosMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.DscpMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.DscpMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EcnMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EcnMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EthTypeMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EthTypeMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Icmpv4CodeMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Icmpv4CodeMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Icmpv4TypeMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Icmpv4TypeMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Icmpv6CodeMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Icmpv6CodeMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Icmpv6TypeMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Icmpv6TypeMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv4AddressMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv4AddressMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv6AddressMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv6AddressMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv6FlabelMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv6FlabelMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.IsidMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.IsidMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MacAddressMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MacAddressMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaskMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaskMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MetadataMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MetadataMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MplsLabelMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MplsLabelMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.OpCodeMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.OpCodeMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortNumberMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortNumberMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ProtocolNumberMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ProtocolNumberMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PseudoFieldMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PseudoFieldMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.TcMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.TcMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanPcpMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanPcpMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanVidMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanVidMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Ipv6ExthdrFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MplsBos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MplsLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MplsTc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OxmMatchType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.PbbIsid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.SctpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.SctpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TcpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TcpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TunnelId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.UdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.UdpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.grouping.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntriesBuilder;

/**
 * @author michal.polkorab
 *
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
     * Test {@link MatchConvertorImpl#fromOFMatchToSALMatch(Match, BigInteger, OpenflowVersion)}
     */
    @Test(expected=NullPointerException.class)
    public void testEmptyMatch() {
        MatchBuilder builder = new MatchBuilder();
        Match match = builder.build();

        MatchConvertorImpl.fromOFMatchToSALMatch(match, new BigInteger("42"), OpenflowVersion.OF10);
    }

    /**
     * Test {@link MatchConvertorImpl#fromOFMatchToSALMatch(Match, BigInteger, OpenflowVersion)}
     */
    @Test
    public void testEmptyMatchEntries() {
        MatchBuilder builder = new MatchBuilder();
        builder.setType(OxmMatchType.class);
        List<MatchEntries> entries = new ArrayList<>();
        builder.setMatchEntries(entries);
        Match match = builder.build();

        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow
        .MatchBuilder salMatch = MatchConvertorImpl.fromOFMatchToSALMatch(match, new BigInteger("42"), OpenflowVersion.OF10);
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match builtMatch = salMatch.build();

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
     * Test {@link MatchConvertorImpl#fromOFMatchToSALMatch(Match, BigInteger, OpenflowVersion)}
     */
    @Test
    public void testWithMatchEntriesNoMasks() {
        MatchBuilder builder = new MatchBuilder();
        builder.setType(OxmMatchType.class);
        List<MatchEntries> entries = new ArrayList<>();
        MatchEntriesBuilder entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(InPort.class);
        entriesBuilder.setHasMask(false);
        PortNumberMatchEntryBuilder portNumberBuilder = new PortNumberMatchEntryBuilder();
        portNumberBuilder.setPortNumber(new PortNumber(1L));
        entriesBuilder.addAugmentation(PortNumberMatchEntry.class, portNumberBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(InPhyPort.class);
        entriesBuilder.setHasMask(false);
        portNumberBuilder = new PortNumberMatchEntryBuilder();
        portNumberBuilder.setPortNumber(new PortNumber(2L));
        entriesBuilder.addAugmentation(PortNumberMatchEntry.class, portNumberBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Metadata.class);
        entriesBuilder.setHasMask(false);
        MetadataMatchEntryBuilder metadataBuilder = new MetadataMatchEntryBuilder();
        metadataBuilder.setMetadata(new byte[]{0, 1, 2, 3, 4, 5, 6, 7});
        entriesBuilder.addAugmentation(MetadataMatchEntry.class, metadataBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(EthDst.class);
        entriesBuilder.setHasMask(false);
        MacAddressMatchEntryBuilder macAddressBuilder = new MacAddressMatchEntryBuilder();
        macAddressBuilder.setMacAddress(new MacAddress("00:00:00:00:00:01"));
        entriesBuilder.addAugmentation(MacAddressMatchEntry.class, macAddressBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(EthSrc.class);
        entriesBuilder.setHasMask(false);
        macAddressBuilder = new MacAddressMatchEntryBuilder();
        macAddressBuilder.setMacAddress(new MacAddress("00:00:00:00:00:02"));
        entriesBuilder.addAugmentation(MacAddressMatchEntry.class, macAddressBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(EthType.class);
        entriesBuilder.setHasMask(false);
        EthTypeMatchEntryBuilder ethTypeBuilder = new EthTypeMatchEntryBuilder();
        ethTypeBuilder.setEthType(new EtherType(3));
        entriesBuilder.addAugmentation(EthTypeMatchEntry.class, ethTypeBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(VlanVid.class);
        entriesBuilder.setHasMask(false);
        VlanVidMatchEntryBuilder vlanVidBuilder = new VlanVidMatchEntryBuilder();
        vlanVidBuilder.setVlanVid(4);
        vlanVidBuilder.setCfiBit(true);
        entriesBuilder.addAugmentation(VlanVidMatchEntry.class, vlanVidBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(VlanPcp.class);
        entriesBuilder.setHasMask(false);
        VlanPcpMatchEntryBuilder pcpBuilder = new VlanPcpMatchEntryBuilder();
        pcpBuilder.setVlanPcp((short) 5);
        entriesBuilder.addAugmentation(VlanPcpMatchEntry.class, pcpBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(IpDscp.class);
        entriesBuilder.setHasMask(false);
        DscpMatchEntryBuilder dscpBuilder = new DscpMatchEntryBuilder();
        dscpBuilder.setDscp(new Dscp((short) 6));
        entriesBuilder.addAugmentation(DscpMatchEntry.class, dscpBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(IpEcn.class);
        entriesBuilder.setHasMask(false);
        EcnMatchEntryBuilder ecnBuilder = new EcnMatchEntryBuilder();
        ecnBuilder.setEcn((short) 7);
        entriesBuilder.addAugmentation(EcnMatchEntry.class, ecnBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(IpProto.class);
        entriesBuilder.setHasMask(false);
        ProtocolNumberMatchEntryBuilder protoBuilder =  new ProtocolNumberMatchEntryBuilder();
        protoBuilder.setProtocolNumber((short) 8);
        entriesBuilder.addAugmentation(ProtocolNumberMatchEntry.class, protoBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv4Src.class);
        entriesBuilder.setHasMask(false);
        Ipv4AddressMatchEntryBuilder ipv4AddressBuilder = new Ipv4AddressMatchEntryBuilder();
        ipv4AddressBuilder.setIpv4Address(new Ipv4Address("10.0.0.1"));
        entriesBuilder.addAugmentation(Ipv4AddressMatchEntry.class, ipv4AddressBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv4Dst.class);
        entriesBuilder.setHasMask(false);
        ipv4AddressBuilder = new Ipv4AddressMatchEntryBuilder();
        ipv4AddressBuilder.setIpv4Address(new Ipv4Address("10.0.0.2"));
        entriesBuilder.addAugmentation(Ipv4AddressMatchEntry.class, ipv4AddressBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(TcpSrc.class);
        entriesBuilder.setHasMask(false);
        PortMatchEntryBuilder portBuilder = new PortMatchEntryBuilder();
        portBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
                .inet.types.rev100924.PortNumber(9));
        entriesBuilder.addAugmentation(PortMatchEntry.class, portBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(TcpDst.class);
        entriesBuilder.setHasMask(false);
        portBuilder = new PortMatchEntryBuilder();
        portBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
                .inet.types.rev100924.PortNumber(10));
        entriesBuilder.addAugmentation(PortMatchEntry.class, portBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Icmpv4Type.class);
        entriesBuilder.setHasMask(false);
        Icmpv4TypeMatchEntryBuilder icmpv4TypeBuilder = new Icmpv4TypeMatchEntryBuilder();
        icmpv4TypeBuilder.setIcmpv4Type((short) 15);
        entriesBuilder.addAugmentation(Icmpv4TypeMatchEntry.class, icmpv4TypeBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Icmpv4Code.class);
        entriesBuilder.setHasMask(false);
        Icmpv4CodeMatchEntryBuilder icmpv4CodeBuilder = new Icmpv4CodeMatchEntryBuilder();
        icmpv4CodeBuilder.setIcmpv4Code((short) 16);
        entriesBuilder.addAugmentation(Icmpv4CodeMatchEntry.class, icmpv4CodeBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Icmpv6Type.class);
        entriesBuilder.setHasMask(false);
        Icmpv6TypeMatchEntryBuilder icmpv6TypeBuilder = new Icmpv6TypeMatchEntryBuilder();
        icmpv6TypeBuilder.setIcmpv6Type((short) 19);
        entriesBuilder.addAugmentation(Icmpv6TypeMatchEntry.class, icmpv6TypeBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Icmpv6Code.class);
        entriesBuilder.setHasMask(false);
        Icmpv6CodeMatchEntryBuilder icmpv6CodeBuilder = new Icmpv6CodeMatchEntryBuilder();
        icmpv6CodeBuilder.setIcmpv6Code((short) 20);
        entriesBuilder.addAugmentation(Icmpv6CodeMatchEntry.class, icmpv6CodeBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(MplsLabel.class);
        entriesBuilder.setHasMask(false);
        MplsLabelMatchEntryBuilder mplsLabelBuilder = new MplsLabelMatchEntryBuilder();
        mplsLabelBuilder.setMplsLabel(21L);
        entriesBuilder.addAugmentation(MplsLabelMatchEntry.class, mplsLabelBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(MplsTc.class);
        entriesBuilder.setHasMask(false);
        TcMatchEntryBuilder tcBuilder = new TcMatchEntryBuilder();
        tcBuilder.setTc((short) 22);
        entriesBuilder.addAugmentation(TcMatchEntry.class, tcBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(MplsBos.class);
        entriesBuilder.setHasMask(false);
        BosMatchEntryBuilder bosBuilder = new BosMatchEntryBuilder();
        bosBuilder.setBos(true);
        entriesBuilder.addAugmentation(BosMatchEntry.class, bosBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(PbbIsid.class);
        entriesBuilder.setHasMask(false);
        IsidMatchEntryBuilder isidBuilder = new IsidMatchEntryBuilder();
        isidBuilder.setIsid(23L);
        entriesBuilder.addAugmentation(IsidMatchEntry.class, isidBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(TunnelId.class);
        entriesBuilder.setHasMask(false);
        metadataBuilder = new MetadataMatchEntryBuilder();
        metadataBuilder.setMetadata(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
        entriesBuilder.addAugmentation(MetadataMatchEntry.class, metadataBuilder.build());
        entries.add(entriesBuilder.build());
        builder.setMatchEntries(entries);
        Match match = builder.build();

        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow
        .MatchBuilder salMatch = MatchConvertorImpl.fromOFMatchToSALMatch(match, new BigInteger("42"), OpenflowVersion.OF10);
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match builtMatch = salMatch.build();

        Assert.assertEquals("Wrong in port", "openflow:42:1", builtMatch.getInPort().getValue());
        Assert.assertEquals("Wrong in phy port", "openflow:42:2", builtMatch.getInPhyPort().getValue());
        Assert.assertEquals("Wrong metadata", new BigInteger(1, new byte[]{0, 1, 2, 3, 4, 5, 6, 7}), builtMatch.getMetadata().getMetadata());
        Assert.assertEquals("Wrong eth dst", new MacAddress("00:00:00:00:00:01"), builtMatch.getEthernetMatch().getEthernetDestination().getAddress());
        Assert.assertEquals("Wrong eth src", new MacAddress("00:00:00:00:00:02"), builtMatch.getEthernetMatch().getEthernetSource().getAddress());
        Assert.assertEquals("Wrong eth type", 3, builtMatch.getEthernetMatch().getEthernetType().getType().getValue().intValue());
        Assert.assertEquals("Wrong vlan id", 4, builtMatch.getVlanMatch().getVlanId().getVlanId().getValue().intValue());
        // TODO - finish implementation by setting the vlanIdPresent flag
//        Assert.assertEquals("Wrong vlan id entries", true, builtMatch.getVlanMatch().getVlanId().isVlanIdPresent());
        Assert.assertEquals("Wrong vlan pcp", 5, builtMatch.getVlanMatch().getVlanPcp().getValue().intValue());
        Assert.assertEquals("Wrong ip dscp", 6, builtMatch.getIpMatch().getIpDscp().getValue().intValue());
        Assert.assertEquals("Wrong ip ecn", 7, builtMatch.getIpMatch().getIpEcn().intValue());
        Assert.assertEquals("Wrong ip proto", null, builtMatch.getIpMatch().getIpProto());
        Assert.assertEquals("Wrong ip protocol", 8, builtMatch.getIpMatch().getIpProtocol().intValue());
        TcpMatch tcpMatch = (TcpMatch) builtMatch.getLayer4Match();
        Assert.assertEquals("Wrong tcp src port", 9, tcpMatch.getTcpSourcePort().getValue().intValue());
        Assert.assertEquals("Wrong tcp dst port", 10, tcpMatch.getTcpDestinationPort().getValue().intValue());
        Assert.assertEquals("Wrong icmpv4 type", 15, builtMatch.getIcmpv4Match().getIcmpv4Type().intValue());
        Assert.assertEquals("Wrong icmpv4 code", 16, builtMatch.getIcmpv4Match().getIcmpv4Code().intValue());
        Assert.assertEquals("Wrong icmpv6 type", 19, builtMatch.getIcmpv6Match().getIcmpv6Type().intValue());
        Assert.assertEquals("Wrong icmpv6 code", 20, builtMatch.getIcmpv6Match().getIcmpv6Code().intValue());
        Ipv4Match ipv4Match = (Ipv4Match) builtMatch.getLayer3Match();
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
     * Test {@link MatchConvertorImpl#fromOFMatchToSALMatch(Match, BigInteger, OpenflowVersion)}
     */
    @Test
    public void testWithMatchEntriesWithMasks() {
        MatchBuilder builder = new MatchBuilder();
        builder.setType(OxmMatchType.class);
        List<MatchEntries> entries = new ArrayList<>();
        MatchEntriesBuilder entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Metadata.class);
        entriesBuilder.setHasMask(true);
        MetadataMatchEntryBuilder metadataBuilder = new MetadataMatchEntryBuilder();
        metadataBuilder.setMetadata(new byte[]{0, 1, 2, 3, 4, 5, 6, 7});
        entriesBuilder.addAugmentation(MetadataMatchEntry.class, metadataBuilder.build());
        MaskMatchEntryBuilder maskBuilder = new MaskMatchEntryBuilder();
        maskBuilder.setMask(new byte[]{0, 0, 0, 0, 0, 0, 0, 1});
        entriesBuilder.addAugmentation(MaskMatchEntry.class, maskBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(EthDst.class);
        entriesBuilder.setHasMask(true);
        MacAddressMatchEntryBuilder macAddressBuilder = new MacAddressMatchEntryBuilder();
        macAddressBuilder.setMacAddress(new MacAddress("00:00:00:00:00:01"));
        entriesBuilder.addAugmentation(MacAddressMatchEntry.class, macAddressBuilder.build());
        maskBuilder = new MaskMatchEntryBuilder();
        maskBuilder.setMask(new byte[]{0, 0, 0, 0, 0, 2});
        entriesBuilder.addAugmentation(MaskMatchEntry.class, maskBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(EthSrc.class);
        entriesBuilder.setHasMask(true);
        macAddressBuilder = new MacAddressMatchEntryBuilder();
        macAddressBuilder.setMacAddress(new MacAddress("00:00:00:00:00:02"));
        entriesBuilder.addAugmentation(MacAddressMatchEntry.class, macAddressBuilder.build());
        maskBuilder = new MaskMatchEntryBuilder();
        maskBuilder.setMask(new byte[]{0, 0, 0, 0, 0, 3});
        entriesBuilder.addAugmentation(MaskMatchEntry.class, maskBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(VlanVid.class);
        entriesBuilder.setHasMask(true);
        VlanVidMatchEntryBuilder vlanVidBuilder = new VlanVidMatchEntryBuilder();
        vlanVidBuilder.setVlanVid(4);
        vlanVidBuilder.setCfiBit(true);
        entriesBuilder.addAugmentation(VlanVidMatchEntry.class, vlanVidBuilder.build());
        maskBuilder = new MaskMatchEntryBuilder();
        maskBuilder.setMask(new byte[]{0, 4});
        entriesBuilder.addAugmentation(MaskMatchEntry.class, maskBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv4Src.class);
        entriesBuilder.setHasMask(true);
        Ipv4AddressMatchEntryBuilder ipv4AddressBuilder = new Ipv4AddressMatchEntryBuilder();
        ipv4AddressBuilder.setIpv4Address(new Ipv4Address("10.0.0.1"));
        entriesBuilder.addAugmentation(Ipv4AddressMatchEntry.class, ipv4AddressBuilder.build());
        maskBuilder = new MaskMatchEntryBuilder();
        maskBuilder.setMask(new byte[]{(byte) 255, (byte) 255, (byte) 255, 0});
        entriesBuilder.addAugmentation(MaskMatchEntry.class, maskBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv4Dst.class);
        entriesBuilder.setHasMask(true);
        ipv4AddressBuilder = new Ipv4AddressMatchEntryBuilder();
        ipv4AddressBuilder.setIpv4Address(new Ipv4Address("10.0.0.2"));
        entriesBuilder.addAugmentation(Ipv4AddressMatchEntry.class, ipv4AddressBuilder.build());
        maskBuilder = new MaskMatchEntryBuilder();
        maskBuilder.setMask(new byte[]{(byte) 255, (byte) 255, (byte) 240, 0});
        entriesBuilder.addAugmentation(MaskMatchEntry.class, maskBuilder.build());
        entries.add(entriesBuilder.build());
        // FIXME - PbbIsid should check for mask length of 3 instead of 2
//        entriesBuilder = new MatchEntriesBuilder();
//        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
//        entriesBuilder.setOxmMatchField(PbbIsid.class);
//        entriesBuilder.setHasMask(true);
//        IsidMatchEntryBuilder isidBuilder = new IsidMatchEntryBuilder();
//        isidBuilder.setIsid(23L);
//        entriesBuilder.addAugmentation(IsidMatchEntry.class, isidBuilder.build());
//        maskBuilder = new MaskMatchEntryBuilder();
//        maskBuilder.setMask(new byte[]{0, 0, 7});
//        entriesBuilder.addAugmentation(MaskMatchEntry.class, maskBuilder.build());
//        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(TunnelId.class);
        entriesBuilder.setHasMask(true);
        metadataBuilder = new MetadataMatchEntryBuilder();
        metadataBuilder.setMetadata(new byte[]{1, 2, 3, 4, 5, 6, 7, 8});
        entriesBuilder.addAugmentation(MetadataMatchEntry.class, metadataBuilder.build());
        maskBuilder = new MaskMatchEntryBuilder();
        maskBuilder.setMask(new byte[]{0, 0, 0, 0, 0, 0, 0, 8});
        entriesBuilder.addAugmentation(MaskMatchEntry.class, maskBuilder.build());
        entries.add(entriesBuilder.build());
        builder.setMatchEntries(entries);
        Match match = builder.build();

        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow
        .MatchBuilder salMatch = MatchConvertorImpl.fromOFMatchToSALMatch(match, new BigInteger("42"), OpenflowVersion.OF10);
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match builtMatch = salMatch.build();

        Assert.assertEquals("Wrong metadata", new BigInteger(1, new byte[]{0, 1, 2, 3, 4, 5, 6, 7}), builtMatch.getMetadata().getMetadata());
        Assert.assertEquals("Wrong metadata mask", new BigInteger(1, new byte[]{0, 0, 0, 0, 0, 0, 0, 1}),
                builtMatch.getMetadata().getMetadataMask());
        Assert.assertEquals("Wrong eth dst", new MacAddress("00:00:00:00:00:01"), builtMatch.getEthernetMatch().getEthernetDestination().getAddress());
//        Assert.assertEquals("Wrong eth dst mask", new MacAddress("00:00:00:00:00:01"), builtMatch.getEthernetMatch().getEthernetDestination().getMask());
        Assert.assertEquals("Wrong eth src", new MacAddress("00:00:00:00:00:02"), builtMatch.getEthernetMatch().getEthernetSource().getAddress());
//        Assert.assertEquals("Wrong eth src mask", new MacAddress("00:00:00:00:00:03"), builtMatch.getEthernetMatch().getEthernetSource().getMask());
        Assert.assertEquals("Wrong vlan id", 4, builtMatch.getVlanMatch().getVlanId().getVlanId().getValue().intValue());
        // TODO - finish implementation by setting the vlanIdPresent flag
//        Assert.assertEquals("Wrong vlan id entries", true, builtMatch.getVlanMatch().getVlanId().isVlanIdPresent());
        Ipv4Match ipv4Match = (Ipv4Match) builtMatch.getLayer3Match();
        Assert.assertEquals("Wrong ipv4 src address", "10.0.0.1/24", ipv4Match.getIpv4Source().getValue());
        Assert.assertEquals("Wrong ipv4 dst address", "10.0.0.2/20", ipv4Match.getIpv4Destination().getValue());
//        Assert.assertEquals("Wrong pbb isid", 23, builtMatch.getProtocolMatchFields().getPbb().getPbbIsid().intValue());
        Assert.assertEquals("Wrong tunnel id", new BigInteger(1, new byte[]{1, 2, 3, 4, 5, 6, 7, 8}),
                builtMatch.getTunnel().getTunnelId());
    }

    /**
     * Test {@link MatchConvertorImpl#fromOFMatchToSALMatch(Match, BigInteger, OpenflowVersion)}
     */
    @Test
    public void testLayer4MatchUdp() {
        MatchBuilder builder = new MatchBuilder();
        builder.setType(OxmMatchType.class);
        List<MatchEntries> entries = new ArrayList<>();
        MatchEntriesBuilder entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(UdpSrc.class);
        entriesBuilder.setHasMask(false);
        PortMatchEntryBuilder portBuilder = new PortMatchEntryBuilder();
        portBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
                .inet.types.rev100924.PortNumber(11));
        entriesBuilder.addAugmentation(PortMatchEntry.class, portBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(UdpDst.class);
        entriesBuilder.setHasMask(false);
        portBuilder = new PortMatchEntryBuilder();
        portBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
                .inet.types.rev100924.PortNumber(12));
        entriesBuilder.addAugmentation(PortMatchEntry.class, portBuilder.build());
        entries.add(entriesBuilder.build());
        builder.setMatchEntries(entries);
        
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow
        .MatchBuilder builtMatch = MatchConvertorImpl.fromOFMatchToSALMatch(builder.build(), new BigInteger("42"), OpenflowVersion.OF13);
        
        UdpMatch udpMatch = (UdpMatch) builtMatch.getLayer4Match();
        Assert.assertEquals("Wrong udp src port", 11, udpMatch.getUdpSourcePort().getValue().intValue());
        Assert.assertEquals("Wrong udp dst port", 12, udpMatch.getUdpDestinationPort().getValue().intValue());
    }

    /**
     * Test {@link MatchConvertorImpl#fromOFMatchToSALMatch(Match, BigInteger, OpenflowVersion)}
     */
    @Test
    public void testLayer4MatchSctp() {
        MatchBuilder builder = new MatchBuilder();
        builder.setType(OxmMatchType.class);
        List<MatchEntries> entries = new ArrayList<>();
        MatchEntriesBuilder entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(SctpSrc.class);
        entriesBuilder.setHasMask(false);
        PortMatchEntryBuilder portBuilder = new PortMatchEntryBuilder();
        portBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
                .inet.types.rev100924.PortNumber(13));
        entriesBuilder.addAugmentation(PortMatchEntry.class, portBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(SctpDst.class);
        entriesBuilder.setHasMask(false);
        portBuilder = new PortMatchEntryBuilder();
        portBuilder.setPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf
                .inet.types.rev100924.PortNumber(14));
        entriesBuilder.addAugmentation(PortMatchEntry.class, portBuilder.build());
        entries.add(entriesBuilder.build());
        entries.add(entriesBuilder.build());
        builder.setMatchEntries(entries);
        
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow
        .MatchBuilder salMatchBuilder = MatchConvertorImpl.fromOFMatchToSALMatch(builder.build(), new BigInteger("42"), OpenflowVersion.OF13);
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match builtMatch = salMatchBuilder.build();
        
        SctpMatch udpMatch = (SctpMatch) builtMatch.getLayer4Match();
        Assert.assertEquals("Wrong sctp src port", 13, udpMatch.getSctpSourcePort().getValue().intValue());
        Assert.assertEquals("Wrong sctp dst port", 14, udpMatch.getSctpDestinationPort().getValue().intValue());
    }

    /**
     * Test {@link MatchConvertorImpl#fromOFMatchToSALMatch(Match, BigInteger, OpenflowVersion)}
     */
    @Test
    public void testLayer3MatchIpv6() {
        MatchBuilder builder = new MatchBuilder();
        builder.setType(OxmMatchType.class);
        List<MatchEntries> entries = new ArrayList<>();
        MatchEntriesBuilder entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv6Src.class);
        entriesBuilder.setHasMask(false);
        Ipv6AddressMatchEntryBuilder ipv6AddressBuilder = new Ipv6AddressMatchEntryBuilder();
        ipv6AddressBuilder.setIpv6Address(new Ipv6Address("0000:0001:0002:0003:0004:0005:0006:0007"));
        entriesBuilder.addAugmentation(Ipv6AddressMatchEntry.class, ipv6AddressBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv6Dst.class);
        entriesBuilder.setHasMask(false);
        ipv6AddressBuilder = new Ipv6AddressMatchEntryBuilder();
        ipv6AddressBuilder.setIpv6Address(new Ipv6Address("0001:0002:0003:0004:0005:0006:0007:0008"));
        entriesBuilder.addAugmentation(Ipv6AddressMatchEntry.class, ipv6AddressBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv6Flabel.class);
        entriesBuilder.setHasMask(false);
        Ipv6FlabelMatchEntryBuilder ipv6FlabelBuilder = new Ipv6FlabelMatchEntryBuilder();
        ipv6FlabelBuilder.setIpv6Flabel(new Ipv6FlowLabel(18L));
        entriesBuilder.addAugmentation(Ipv6FlabelMatchEntry.class, ipv6FlabelBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv6NdTarget.class);
        entriesBuilder.setHasMask(false);
        ipv6AddressBuilder = new Ipv6AddressMatchEntryBuilder();
        ipv6AddressBuilder.setIpv6Address(new Ipv6Address("0002:0003:0004:0005:0006:0007:0008:0009"));
        entriesBuilder.addAugmentation(Ipv6AddressMatchEntry.class, ipv6AddressBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv6NdSll.class);
        entriesBuilder.setHasMask(false);
        MacAddressMatchEntryBuilder macAddressBuilder = new MacAddressMatchEntryBuilder();
        macAddressBuilder.setMacAddress(new MacAddress("00:00:00:00:00:05"));
        entriesBuilder.addAugmentation(MacAddressMatchEntry.class, macAddressBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv6NdTll.class);
        entriesBuilder.setHasMask(false);
        macAddressBuilder = new MacAddressMatchEntryBuilder();
        macAddressBuilder.setMacAddress(new MacAddress("00:00:00:00:00:06"));
        entriesBuilder.addAugmentation(MacAddressMatchEntry.class, macAddressBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv6Exthdr.class);
        entriesBuilder.setHasMask(false);
        PseudoFieldMatchEntryBuilder pseudoBuilder = new PseudoFieldMatchEntryBuilder();
        pseudoBuilder.setPseudoField(new Ipv6ExthdrFlags(true, false, true, false, true, false, true, false, true));
        entriesBuilder.addAugmentation(PseudoFieldMatchEntry.class, pseudoBuilder.build());
        builder.setMatchEntries(entries);
        entries.add(entriesBuilder.build());

        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow
        .MatchBuilder salMatchBuilder = MatchConvertorImpl.fromOFMatchToSALMatch(builder.build(), new BigInteger("42"), OpenflowVersion.OF13);
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match builtMatch = salMatchBuilder.build();
        
        Ipv6Match ipv6Match = (Ipv6Match) builtMatch.getLayer3Match();
        Assert.assertEquals("Wrong ipv6 src address", "0000:0001:0002:0003:0004:0005:0006:0007/128",
                ipv6Match.getIpv6Source().getValue());
        Assert.assertEquals("Wrong ipv6 dst address", "0001:0002:0003:0004:0005:0006:0007:0008/128",
                ipv6Match.getIpv6Destination().getValue());
        Assert.assertEquals("Wrong ipv6 nd target", "0002:0003:0004:0005:0006:0007:0008:0009",
                ipv6Match.getIpv6NdTarget().getValue());
        Assert.assertEquals("Wrong ipv6 flabel", 18, ipv6Match.getIpv6Label().getIpv6Flabel().getValue().intValue());
        Assert.assertEquals("Wrong ipv6 nd sll", new MacAddress("00:00:00:00:00:05"), ipv6Match.getIpv6NdSll());
        Assert.assertEquals("Wrong ipv6 nd tll", new MacAddress("00:00:00:00:00:06"), ipv6Match.getIpv6NdTll());
        Assert.assertEquals("Wrong ipv6 ext header", 358, ipv6Match.getIpv6ExtHeader().getIpv6Exthdr().intValue());
    }

    /**
     * Test {@link MatchConvertorImpl#fromOFMatchToSALMatch(Match, BigInteger, OpenflowVersion)}
     */
    @Test
    public void testLayer3MatchIpv6ExtHeader2() {
        MatchBuilder builder = new MatchBuilder();
        builder.setType(OxmMatchType.class);
        List<MatchEntries> entries = new ArrayList<>();
        MatchEntriesBuilder entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(Ipv6Exthdr.class);
        entriesBuilder.setHasMask(true);
        PseudoFieldMatchEntryBuilder pseudoBuilder = new PseudoFieldMatchEntryBuilder();
        pseudoBuilder.setPseudoField(new Ipv6ExthdrFlags(false, true, false, true, false, true, false, true, false));
        entriesBuilder.addAugmentation(PseudoFieldMatchEntry.class, pseudoBuilder.build());
        MaskMatchEntryBuilder maskBuilder = new MaskMatchEntryBuilder();
        maskBuilder.setMask(new byte[]{1, 2});
        entriesBuilder.addAugmentation(MaskMatchEntry.class, maskBuilder.build());
        entries.add(entriesBuilder.build());
        builder.setMatchEntries(entries);

        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow
        .MatchBuilder salMatchBuilder = MatchConvertorImpl.fromOFMatchToSALMatch(builder.build(), new BigInteger("42"), OpenflowVersion.OF13);
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match builtMatch = salMatchBuilder.build();
        
        Ipv6Match ipv6Match = (Ipv6Match) builtMatch.getLayer3Match();
        Assert.assertEquals("Wrong ipv6 ext header", 153, ipv6Match.getIpv6ExtHeader().getIpv6Exthdr().intValue());
        Assert.assertEquals("Wrong ipv6 ext header mask", 258, ipv6Match.getIpv6ExtHeader().getIpv6ExthdrMask().intValue());
    }

    /**
     * Test {@link MatchConvertorImpl#fromOFMatchToSALMatch(Match, BigInteger, OpenflowVersion)}
     */
    @Test
    public void testLayer3MatchArp() {
        MatchBuilder builder = new MatchBuilder();
        builder.setType(OxmMatchType.class);
        List<MatchEntries> entries = new ArrayList<>();
        MatchEntriesBuilder entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(ArpOp.class);
        entriesBuilder.setHasMask(false);
        OpCodeMatchEntryBuilder opCodeBuilder = new OpCodeMatchEntryBuilder();
        opCodeBuilder.setOpCode(17);
        entriesBuilder.addAugmentation(OpCodeMatchEntry.class, opCodeBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(ArpSpa.class);
        entriesBuilder.setHasMask(false);
        Ipv4AddressMatchEntryBuilder ipv4AddressBuilder = new Ipv4AddressMatchEntryBuilder();
        ipv4AddressBuilder.setIpv4Address(new Ipv4Address("10.0.0.3"));
        entriesBuilder.addAugmentation(Ipv4AddressMatchEntry.class, ipv4AddressBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(ArpTpa.class);
        entriesBuilder.setHasMask(false);
        ipv4AddressBuilder = new Ipv4AddressMatchEntryBuilder();
        ipv4AddressBuilder.setIpv4Address(new Ipv4Address("10.0.0.4"));
        entriesBuilder.addAugmentation(Ipv4AddressMatchEntry.class, ipv4AddressBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(ArpSha.class);
        entriesBuilder.setHasMask(false);
        MacAddressMatchEntryBuilder macAddressBuilder = new MacAddressMatchEntryBuilder();
        macAddressBuilder.setMacAddress(new MacAddress("00:00:00:00:00:03"));
        entriesBuilder.addAugmentation(MacAddressMatchEntry.class, macAddressBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(ArpTha.class);
        entriesBuilder.setHasMask(false);
        macAddressBuilder = new MacAddressMatchEntryBuilder();
        macAddressBuilder.setMacAddress(new MacAddress("00:00:00:00:00:04"));
        entriesBuilder.addAugmentation(MacAddressMatchEntry.class, macAddressBuilder.build());
        entries.add(entriesBuilder.build());
        builder.setMatchEntries(entries);
        
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow
        .MatchBuilder salMatchBuilder = MatchConvertorImpl.fromOFMatchToSALMatch(builder.build(), new BigInteger("42"), OpenflowVersion.OF13);
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match builtMatch = salMatchBuilder.build();
        
        ArpMatch arpMatch = (ArpMatch) builtMatch.getLayer3Match();
        Assert.assertEquals("Wrong arp op", 17, arpMatch.getArpOp().intValue());
        Assert.assertEquals("Wrong arp spa", "10.0.0.3/32", arpMatch.getArpSourceTransportAddress().getValue());
        Assert.assertEquals("Wrong arp tpa", "10.0.0.4/32", arpMatch.getArpTargetTransportAddress().getValue());
        Assert.assertEquals("Wrong arp sha", "00:00:00:00:00:03", arpMatch.getArpSourceHardwareAddress().getAddress().getValue());
        Assert.assertEquals("Wrong arp tha", "00:00:00:00:00:04", arpMatch.getArpTargetHardwareAddress().getAddress().getValue());
    }

    /**
     * Test {@link MatchConvertorImpl#fromOFMatchToSALMatch(Match, BigInteger, OpenflowVersion)}
     */
    @Test
    public void testLayer3MatchArpWithMasks() {
        MatchBuilder builder = new MatchBuilder();
        builder.setType(OxmMatchType.class);
        List<MatchEntries> entries = new ArrayList<>();
        MatchEntriesBuilder entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(ArpSpa.class);
        entriesBuilder.setHasMask(true);
        Ipv4AddressMatchEntryBuilder ipv4AddressBuilder = new Ipv4AddressMatchEntryBuilder();
        ipv4AddressBuilder.setIpv4Address(new Ipv4Address("10.0.0.3"));
        entriesBuilder.addAugmentation(Ipv4AddressMatchEntry.class, ipv4AddressBuilder.build());
        MaskMatchEntryBuilder maskBuilder = new MaskMatchEntryBuilder();
        maskBuilder.setMask(new byte[]{(byte) 255, (byte) 255, (byte) 255, 0});
        entriesBuilder.addAugmentation(MaskMatchEntry.class, maskBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(ArpTpa.class);
        entriesBuilder.setHasMask(true);
        ipv4AddressBuilder = new Ipv4AddressMatchEntryBuilder();
        ipv4AddressBuilder.setIpv4Address(new Ipv4Address("10.0.0.4"));
        entriesBuilder.addAugmentation(Ipv4AddressMatchEntry.class, ipv4AddressBuilder.build());
        maskBuilder = new MaskMatchEntryBuilder();
        maskBuilder.setMask(new byte[]{(byte) 255, (byte) 128, 0, 0});
        entriesBuilder.addAugmentation(MaskMatchEntry.class, maskBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(ArpSha.class);
        entriesBuilder.setHasMask(true);
        MacAddressMatchEntryBuilder macAddressBuilder = new MacAddressMatchEntryBuilder();
        macAddressBuilder.setMacAddress(new MacAddress("00:00:00:00:00:03"));
        entriesBuilder.addAugmentation(MacAddressMatchEntry.class, macAddressBuilder.build());
        maskBuilder = new MaskMatchEntryBuilder();
        maskBuilder.setMask(new byte[]{0, 0, 1, 0, 4, 0});
        entriesBuilder.addAugmentation(MaskMatchEntry.class, maskBuilder.build());
        entries.add(entriesBuilder.build());
        entriesBuilder = new MatchEntriesBuilder();
        entriesBuilder.setOxmClass(OpenflowBasicClass.class);
        entriesBuilder.setOxmMatchField(ArpTha.class);
        entriesBuilder.setHasMask(true);
        macAddressBuilder = new MacAddressMatchEntryBuilder();
        macAddressBuilder.setMacAddress(new MacAddress("00:00:00:00:00:04"));
        entriesBuilder.addAugmentation(MacAddressMatchEntry.class, macAddressBuilder.build());
        maskBuilder = new MaskMatchEntryBuilder();
        maskBuilder.setMask(new byte[]{1, 1, 1, 2, 2, 2});
        entriesBuilder.addAugmentation(MaskMatchEntry.class, maskBuilder.build());
        entries.add(entriesBuilder.build());
        builder.setMatchEntries(entries);
        
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow
        .MatchBuilder salMatchBuilder = MatchConvertorImpl.fromOFMatchToSALMatch(builder.build(), new BigInteger("42"), OpenflowVersion.OF13);
        org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match builtMatch = salMatchBuilder.build();
        
        ArpMatch arpMatch = (ArpMatch) builtMatch.getLayer3Match();
        // FIXME - fix mask computation
//        Assert.assertEquals("Wrong arp spa", "10.0.0.3/8", arpMatch.getArpSourceTransportAddress().getValue());
//        Assert.assertEquals("Wrong arp tpa", "10.0.0.4/9", arpMatch.getArpTargetTransportAddress().getValue());
        Assert.assertEquals("Wrong arp sha", "00:00:00:00:00:03", arpMatch.getArpSourceHardwareAddress().getAddress().getValue());
        Assert.assertEquals("Wrong arp sha mask", "00:00:01:00:04:00", arpMatch.getArpSourceHardwareAddress().getMask().getValue());
        Assert.assertEquals("Wrong arp tha", "00:00:00:00:00:04", arpMatch.getArpTargetHardwareAddress().getAddress().getValue());
        Assert.assertEquals("Wrong arp tha mask", "01:01:01:02:02:02", arpMatch.getArpTargetHardwareAddress().getMask().getValue());
    }
}
