/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 *  and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.translator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.openflow.md.core.extension.ExtensionConverterManagerImpl;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionManagerOFImpl;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6FlowLabel;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.mod.removed.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ProtocolMatchFields;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Tunnel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.BosMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.DscpMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EcnMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.EthTypeMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Icmpv4CodeMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Icmpv4TypeMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Icmpv6CodeMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Icmpv6TypeMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv4AddressMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv4AddressMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv6AddressMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv6AddressMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.Ipv6FlabelMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.IsidMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MacAddressMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaskMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MaskMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MetadataMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.MplsLabelMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.OpCodeMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PortNumberMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.ProtocolNumberMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.PseudoFieldMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.TcMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanPcpMatchEntryBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MplsBos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MplsLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.MplsTc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.PbbIsid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.SctpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.SctpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TcpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TcpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TunnelId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TunnelIpv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TunnelIpv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.UdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.UdpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntriesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.binding.DataObject;

@RunWith(MockitoJUnitRunner.class)
public class FlowRemovedTranslatorTest extends FlowRemovedTranslator {

    private static final FlowRemovedTranslator flowRemovedTranslator = new FlowRemovedTranslator();
    private static final BigInteger DATA_PATH_ID = BigInteger.valueOf(42);

    @MockitoAnnotations.Mock
    SwitchConnectionDistinguisher switchConnectionDistinguisher;

    @MockitoAnnotations.Mock
    SessionContext sessionContext;

    @MockitoAnnotations.Mock
    FlowRemovedMessage msg;

    @MockitoAnnotations.Mock
    ConnectionConductor connectionConductor;

    @MockitoAnnotations.Mock
    GetFeaturesOutput featuresOutput;

    private static final MacAddress MAC_ADDRESS = new MacAddress("00:01:02:03:04:05");


    private static Map<Class<? extends MatchField>, Object> fieldClassesAndAugmentations = new HashMap<>();

    private void setupClassAndAugmentationMap() {

        PortNumberMatchEntryBuilder portNumberMatchEntryBuilder = new PortNumberMatchEntryBuilder();
        portNumberMatchEntryBuilder.setPortNumber(new PortNumber((long) 42));
        fieldClassesAndAugmentations.put(InPort.class, portNumberMatchEntryBuilder.build());
        fieldClassesAndAugmentations.put(InPhyPort.class, portNumberMatchEntryBuilder.build());

        MetadataMatchEntryBuilder metadataMatchEntryBuilder = new MetadataMatchEntryBuilder();
        metadataMatchEntryBuilder.setMetadata(new byte[0]);
        fieldClassesAndAugmentations.put(Metadata.class, metadataMatchEntryBuilder.build());

        MaskMatchEntryBuilder maskMatchEntryBuilder = new MaskMatchEntryBuilder();
        maskMatchEntryBuilder.setMask(new byte[24]);

        EthTypeMatchEntryBuilder ethTypeMatchEntryBuilder = new EthTypeMatchEntryBuilder();
        ethTypeMatchEntryBuilder.setEthType(new EtherType(6));
        fieldClassesAndAugmentations.put(EthType.class, ethTypeMatchEntryBuilder.build());

        VlanVidMatchEntryBuilder vlanVidMatchEntryBuilder = new VlanVidMatchEntryBuilder();
        vlanVidMatchEntryBuilder.setCfiBit(true);
        vlanVidMatchEntryBuilder.setVlanVid(42);
        fieldClassesAndAugmentations.put(VlanVid.class, vlanVidMatchEntryBuilder.build());

        VlanPcpMatchEntryBuilder vlanPcpMatchEntryBuilder = new VlanPcpMatchEntryBuilder();
        vlanPcpMatchEntryBuilder.setVlanPcp((short) 7);
        fieldClassesAndAugmentations.put(VlanPcp.class, vlanPcpMatchEntryBuilder.build());

        DscpMatchEntryBuilder dscpMatchEntryBuilder = new DscpMatchEntryBuilder();
        dscpMatchEntryBuilder.setDscp(new Dscp((short) 10));
        fieldClassesAndAugmentations.put(IpDscp.class, dscpMatchEntryBuilder.build());

        EcnMatchEntryBuilder ecnMatchEntryBuilder = new EcnMatchEntryBuilder();
        ecnMatchEntryBuilder.setEcn((short) 10);
        fieldClassesAndAugmentations.put(IpEcn.class, ecnMatchEntryBuilder.build());

        ProtocolNumberMatchEntryBuilder protocolNumberMatchEntryBuilder = new ProtocolNumberMatchEntryBuilder();
        protocolNumberMatchEntryBuilder.setProtocolNumber(OFConstants.OFP_VERSION_1_3);
        fieldClassesAndAugmentations.put(IpProto.class, protocolNumberMatchEntryBuilder.build());

        PortMatchEntryBuilder portMatchEntryBuilder = new PortMatchEntryBuilder();
        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber port = new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber(43);
        portMatchEntryBuilder.setPort(port);
        fieldClassesAndAugmentations.put(TcpSrc.class, portMatchEntryBuilder.build());
        fieldClassesAndAugmentations.put(TcpDst.class, portMatchEntryBuilder.build());
        fieldClassesAndAugmentations.put(UdpSrc.class, portMatchEntryBuilder.build());
        fieldClassesAndAugmentations.put(UdpDst.class, portMatchEntryBuilder.build());
        fieldClassesAndAugmentations.put(SctpSrc.class, portMatchEntryBuilder.build());
        fieldClassesAndAugmentations.put(SctpDst.class, portMatchEntryBuilder.build());

        Icmpv4TypeMatchEntryBuilder icmpv4TypeMatchEntryBuilder = new Icmpv4TypeMatchEntryBuilder();
        icmpv4TypeMatchEntryBuilder.setIcmpv4Type((short) 10);
        fieldClassesAndAugmentations.put(Icmpv4Type.class, icmpv4TypeMatchEntryBuilder.build());

        Icmpv4CodeMatchEntryBuilder icmpv4CodeMatchEntryBuilder = new Icmpv4CodeMatchEntryBuilder();
        icmpv4CodeMatchEntryBuilder.setIcmpv4Code((short) 10);
        fieldClassesAndAugmentations.put(Icmpv4Code.class, icmpv4CodeMatchEntryBuilder.build());

        Icmpv6TypeMatchEntryBuilder icmpv6TypeMatchEntryBuilder = new Icmpv6TypeMatchEntryBuilder();
        icmpv6TypeMatchEntryBuilder.setIcmpv6Type((short) 10);
        fieldClassesAndAugmentations.put(Icmpv6Type.class, icmpv6TypeMatchEntryBuilder.build());

        Icmpv6CodeMatchEntryBuilder icmpv6CodeMatchEntryBuilder = new Icmpv6CodeMatchEntryBuilder();
        icmpv6CodeMatchEntryBuilder.setIcmpv6Code((short) 10);
        fieldClassesAndAugmentations.put(Icmpv6Code.class, icmpv6CodeMatchEntryBuilder.build());


        OpCodeMatchEntryBuilder opCodeMatchEntryBuilder = new OpCodeMatchEntryBuilder();
        opCodeMatchEntryBuilder.setOpCode(42);
        fieldClassesAndAugmentations.put(ArpOp.class, opCodeMatchEntryBuilder.build());


        MacAddressMatchEntryBuilder macAddressMatchEntryBuilder = new MacAddressMatchEntryBuilder();
        macAddressMatchEntryBuilder.setMacAddress(MAC_ADDRESS);
        fieldClassesAndAugmentations.put(ArpSha.class, macAddressMatchEntryBuilder.build());
        fieldClassesAndAugmentations.put(ArpTha.class, macAddressMatchEntryBuilder.build());
        fieldClassesAndAugmentations.put(EthDst.class, macAddressMatchEntryBuilder.build());
        fieldClassesAndAugmentations.put(EthSrc.class, macAddressMatchEntryBuilder.build());


        Ipv6FlabelMatchEntryBuilder ipv6FlabelMatchEntryBuilder = new Ipv6FlabelMatchEntryBuilder();
        ipv6FlabelMatchEntryBuilder.setIpv6Flabel(new Ipv6FlowLabel((long) 42));
        fieldClassesAndAugmentations.put(Ipv6Flabel.class, ipv6FlabelMatchEntryBuilder.build());

        Ipv6AddressMatchEntryBuilder ipv6AddressMatchEntryBuilder = new Ipv6AddressMatchEntryBuilder();
        ipv6AddressMatchEntryBuilder.setIpv6Address(new Ipv6Address("2001:0DB8:AC10:FE01:0000:0000:0000:0000"));
        fieldClassesAndAugmentations.put(Ipv6NdTarget.class, ipv6AddressMatchEntryBuilder.build());

        fieldClassesAndAugmentations.put(Ipv6NdSll.class, macAddressMatchEntryBuilder.build());
        fieldClassesAndAugmentations.put(Ipv6NdTll.class, macAddressMatchEntryBuilder.build());
        fieldClassesAndAugmentations.put(Ipv6Exthdr.class, macAddressMatchEntryBuilder.build());

        PseudoFieldMatchEntryBuilder pseudoFieldMatchEntryBuilder = new PseudoFieldMatchEntryBuilder();
        pseudoFieldMatchEntryBuilder.setPseudoField(new Ipv6ExthdrFlags(true, true, true, true, true, true, true, true, true));
        fieldClassesAndAugmentations.put(Ipv6Exthdr.class, pseudoFieldMatchEntryBuilder.build());

        MplsLabelMatchEntryBuilder mplsLabelMatchEntryBuilder = new MplsLabelMatchEntryBuilder();
        mplsLabelMatchEntryBuilder.setMplsLabel((long) 42);
        fieldClassesAndAugmentations.put(MplsLabel.class, mplsLabelMatchEntryBuilder.build());

        TcMatchEntryBuilder tcMatchEntryBuilder = new TcMatchEntryBuilder();
        tcMatchEntryBuilder.setTc((short) 0);
        fieldClassesAndAugmentations.put(MplsTc.class, tcMatchEntryBuilder.build());

        BosMatchEntryBuilder bosMatchEntryBuilder = new BosMatchEntryBuilder();
        bosMatchEntryBuilder.setBos(false);
        fieldClassesAndAugmentations.put(MplsBos.class, bosMatchEntryBuilder.build());

        IsidMatchEntryBuilder isidMatchEntryBuilder = new IsidMatchEntryBuilder();
        isidMatchEntryBuilder.setIsid((long) 42);
        fieldClassesAndAugmentations.put(PbbIsid.class, isidMatchEntryBuilder.build());

        fieldClassesAndAugmentations.put(TunnelId.class, metadataMatchEntryBuilder.build());

        Ipv4AddressMatchEntryBuilder ipv4AddressMatchEntryBuilder = new Ipv4AddressMatchEntryBuilder();
        ipv4AddressMatchEntryBuilder.setIpv4Address(new Ipv4Address("10.0.0.1/24"));
        fieldClassesAndAugmentations.put(ArpTpa.class, ipv4AddressMatchEntryBuilder.build());
        fieldClassesAndAugmentations.put(ArpSpa.class, ipv4AddressMatchEntryBuilder.build());

        fieldClassesAndAugmentations.put(Ipv4Src.class, ipv4AddressMatchEntryBuilder.build());
        fieldClassesAndAugmentations.put(Ipv4Dst.class, ipv4AddressMatchEntryBuilder.build());


        fieldClassesAndAugmentations.put(TunnelIpv4Src.class, ipv4AddressMatchEntryBuilder.build());
        fieldClassesAndAugmentations.put(TunnelIpv4Dst.class, ipv4AddressMatchEntryBuilder.build());
        fieldClassesAndAugmentations.put(Ipv6Src.class, ipv6AddressMatchEntryBuilder.build());
        fieldClassesAndAugmentations.put(Ipv6Dst.class, ipv6AddressMatchEntryBuilder.build());
    }


    @Before
    public void setup() {
        when(sessionContext.getPrimaryConductor()).thenReturn(connectionConductor);
        when(connectionConductor.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(sessionContext.getFeatures()).thenReturn(featuresOutput);
        when(featuresOutput.getDatapathId()).thenReturn(DATA_PATH_ID);
        OpenflowPortsUtil.init();
        ExtensionConverterManagerImpl extensionConverterProvider = new ExtensionConverterManagerImpl();
        //extensionConverterProvider.registerMatchConvertor()
        SessionManagerOFImpl.getInstance().setExtensionConverterProvider(extensionConverterProvider);
    }

    @Test
    public void testTranslate() throws Exception {
        List<DataObject> dataObjectList = flowRemovedTranslator.translate(switchConnectionDistinguisher, sessionContext, msg);
        assertNotNull(dataObjectList);
    }

    @Test
    public void testFromMatch() throws Exception {
        setupClassAndAugmentationMap();
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.grouping.MatchBuilder matchBuilder = new MatchBuilder();
        List<MatchEntries> matchEntrieses = new ArrayList<>();
        for (Map.Entry entry : fieldClassesAndAugmentations.entrySet()) {
            MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
            matchEntriesBuilder.setOxmMatchField((Class<? extends MatchField>) entry.getKey());
            if (entry.getValue() != null) {
                matchEntriesBuilder.addAugmentation((Class<? extends Augmentation<MatchEntries>>) entry.getValue().getClass().getInterfaces()[0], (Augmentation<MatchEntries>) entry.getValue());
            }
            matchEntriesBuilder.setHasMask(false);
            matchEntrieses.add(matchEntriesBuilder.build());
        }
        matchBuilder.setMatchEntries(matchEntrieses);
        Match match = flowRemovedTranslator.fromMatch(matchBuilder.build(), DATA_PATH_ID, OpenflowVersion.OF13);
        assertNotNull(match);
        assertEthernetMatch(match.getEthernetMatch());
        assertIcmpV4Match(match.getIcmpv4Match());

        assertEquals("openflow:42:42", match.getInPhyPort().getValue());
        assertEquals("openflow:42:42", match.getInPort().getValue());
        assertIpMatch(match.getIpMatch());

        assertNotNull(match.getLayer3Match());
        assertNotNull(match.getLayer4Match());

        assertProtocolMatchFields(match.getProtocolMatchFields());

        assertMetada(match.getMetadata());

        assertTunnelMatch(match.getTunnel());

        assertVlanMatch(match.getVlanMatch());
    }

    private void assertTunnelMatch(Tunnel tunnel) {
        assertEquals(0, tunnel.getTunnelId().intValue());
    }

    private void assertVlanMatch(VlanMatch vlanMatch) {
        assertEquals(true, vlanMatch.getVlanId().isVlanIdPresent());
        assertEquals(new Integer(42), vlanMatch.getVlanId().getVlanId().getValue());

        assertEquals((short) 7, vlanMatch.getVlanPcp().getValue().shortValue());
    }

    private void assertMetada(org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Metadata metadata) {
        assertEquals(0, metadata.getMetadata().intValue());
    }

    private void assertProtocolMatchFields(ProtocolMatchFields protocolMatchFields) {
        assertEquals((short) 0, protocolMatchFields.getMplsBos().shortValue());
        assertEquals(42, protocolMatchFields.getMplsLabel().longValue());
        assertEquals((short) 0, protocolMatchFields.getMplsTc().shortValue());
        assertEquals(42, protocolMatchFields.getPbb().getPbbIsid().longValue());
    }

    private void assertIpMatch(IpMatch ipMatch) {
        assertEquals(10, ipMatch.getIpDscp().getValue().longValue());
        assertEquals(10, ipMatch.getIpEcn().shortValue());
        assertEquals(4, ipMatch.getIpProtocol().shortValue());
    }

    private void assertIcmpV4Match(Icmpv4Match icmpv4Match) {
        assertEquals(10, icmpv4Match.getIcmpv4Code().longValue());
        assertEquals(10, icmpv4Match.getIcmpv4Type().longValue());
    }

    private void assertEthernetMatch(EthernetMatch ethernetMatch) {
        assertEquals(MAC_ADDRESS, ethernetMatch.getEthernetDestination().getAddress());
        assertEquals(MAC_ADDRESS, ethernetMatch.getEthernetSource().getAddress());
        assertEquals(new Long(6), ethernetMatch.getEthernetType().getType().getValue());
    }

    @Test
    public void MatchEntryToIpv4PrefixTest() {
        Ipv4AddressMatchEntry ipv4AddressMatchEntry = new Ipv4AddressMatchEntryBuilder()
                .setIpv4Address(new Ipv4Address("10.0.0.0")).build();
        byte[] maskBytes = new byte[1];
        maskBytes[0] = (byte) 255;
        MaskMatchEntry maskMatchEntry = new MaskMatchEntryBuilder().setMask(maskBytes).build();
        MatchEntries entry = new MatchEntriesBuilder().setOxmMatchField(Ipv4Src.class)
                .addAugmentation(Ipv4AddressMatchEntry.class, ipv4AddressMatchEntry)
                .addAugmentation(MaskMatchEntry.class, maskMatchEntry).setHasMask(true).build();
        Ipv4Prefix ipv4Prefix = toIpv4Prefix(entry);
        assertEquals("10.0.0.0/8", ipv4Prefix.getValue());
    }

    @Test
    public void MatchEntryToIpv6PrefixTest() {
        Ipv6AddressMatchEntry ipv6AddressMatchEntry = new Ipv6AddressMatchEntryBuilder()
                .setIpv6Address(new Ipv6Address("1234:5678:9ABC:DEF0:FDCD:A987:6543:0")).build();
        byte[] maskBytes = new byte[1];
        maskBytes[0] = (byte) 255;
        MaskMatchEntry maskMatchEntry = new MaskMatchEntryBuilder().setMask(maskBytes).build();
        MatchEntries entry = new MatchEntriesBuilder().setOxmMatchField(Ipv6Src.class)
                .addAugmentation(Ipv6AddressMatchEntry.class, ipv6AddressMatchEntry)
                .addAugmentation(MaskMatchEntry.class, maskMatchEntry).setHasMask(true).build();
        Ipv6Prefix ipv6Prefix = toIpv6Prefix(entry);
        assertEquals("1234:5678:9ABC:DEF0:FDCD:A987:6543:0/8", ipv6Prefix.getValue());
    }

}
