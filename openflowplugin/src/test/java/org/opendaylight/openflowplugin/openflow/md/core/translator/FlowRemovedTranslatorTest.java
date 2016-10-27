/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.translator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.core.extension.ExtensionConverterManagerImpl;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionManagerOFImpl;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6FlowLabel;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.mod.removed.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ProtocolMatchFields;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Tunnel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Ipv6ExthdrFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpOp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpSha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpSpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpTha;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.ArpTpa;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv4Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv4Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv6Code;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Icmpv6Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPhyPort;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.InPort;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.Metadata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MplsBos;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MplsLabel;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.PbbIsid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.SctpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TcpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TunnelId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.UdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.UdpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanPcp;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yangtools.yang.binding.DataObject;

@RunWith(MockitoJUnitRunner.class)
public class FlowRemovedTranslatorTest {

    private FlowRemovedTranslator flowRemovedTranslator;
    private static final BigInteger DATA_PATH_ID = BigInteger.valueOf(42);
    public static final Ipv6Address IPV_6_ADDRESS = new Ipv6Address("2001:0DB8:AC10:FE01:0000:0000:0000:0000");
    private static final byte[] IPV_6_ADDRESS_MASK = ByteUtil.unsignedIntToBytes(new Long(64));
    private static final Ipv4Address IPV_4_ADDRESS = new Ipv4Address("10.0.0.1");
    private static final byte[] IPV_4_ADDRESS_MASK = ByteUtil.unsignedIntToBytes(new Long(8));

    @Mock
    SwitchConnectionDistinguisher switchConnectionDistinguisher;

    @Mock
    SessionContext sessionContext;

    @Mock
    FlowRemovedMessage msg;

    @Mock
    ConnectionConductor connectionConductor;

    @Mock
    GetFeaturesOutput featuresOutput;

    private static final MacAddress MAC_ADDRESS = new MacAddress("00:01:02:03:04:05");


    private static List<MatchEntry> fieldClassesAndAugmentations = new ArrayList<>();


    private static void setupClassAndAugmentationMap() {


        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        InPortBuilder portBuilder = new InPortBuilder();
        portBuilder.setPortNumber(new PortNumber((long) 42));
        InPortCaseBuilder inPortCaseBuilder = new InPortCaseBuilder();
        inPortCaseBuilder.setInPort(portBuilder.build());
        matchEntryBuilder.setMatchEntryValue(inPortCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(InPort.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        InPhyPortBuilder inPhyPortBuilder = new InPhyPortBuilder();
        PortNumber portNumber = new PortNumber((long) 42);
        inPhyPortBuilder.setPortNumber(portNumber);
        InPhyPortCaseBuilder inPhyPortCaseBuilder = new InPhyPortCaseBuilder();
        inPhyPortCaseBuilder.setInPhyPort(inPhyPortBuilder.build());
        matchEntryBuilder.setMatchEntryValue(inPhyPortCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(InPhyPort.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        MetadataCaseBuilder metadataCaseBuilder = new MetadataCaseBuilder();
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.metadata._case.MetadataBuilder metadataBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.metadata._case.MetadataBuilder();
        metadataBuilder.setMetadata(new byte[0]);
        metadataBuilder.setMask(new byte[0]);
        metadataCaseBuilder.setMetadata(metadataBuilder.build());
        matchEntryBuilder.setMatchEntryValue(metadataCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(Metadata.class);
        matchEntryBuilder.setHasMask(false);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());


        EthTypeBuilder ethTypeBuilder = new EthTypeBuilder();
        ethTypeBuilder.setEthType(new EtherType(6));
        EthTypeCaseBuilder ethTypeCaseBuilder = new EthTypeCaseBuilder();
        ethTypeCaseBuilder.setEthType(ethTypeBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ethTypeCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(EthType.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        VlanVidBuilder vlanVidBuilder = new VlanVidBuilder();
        vlanVidBuilder.setCfiBit(true);
        vlanVidBuilder.setVlanVid(42);
        VlanVidCaseBuilder vlanVidCaseBuilder = new VlanVidCaseBuilder();
        vlanVidCaseBuilder.setVlanVid(vlanVidBuilder.build());
        matchEntryBuilder.setMatchEntryValue(vlanVidCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanVid.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        VlanPcpBuilder vlanPcpBuilder = new VlanPcpBuilder();
        vlanPcpBuilder.setVlanPcp((short) 7);
        VlanPcpCaseBuilder vlanPcpCaseBuilder = new VlanPcpCaseBuilder();
        vlanPcpCaseBuilder.setVlanPcp(vlanPcpBuilder.build());
        matchEntryBuilder.setMatchEntryValue(vlanPcpCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(VlanPcp.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        IpDscpBuilder ipDscpBuilder = new IpDscpBuilder();
        ipDscpBuilder.setDscp(new Dscp((short) 10));
        IpDscpCaseBuilder ipDscpCaseBuilder = new IpDscpCaseBuilder();
        ipDscpCaseBuilder.setIpDscp(ipDscpBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ipDscpCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpDscp.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        IpEcnBuilder ipEcnBuilder = new IpEcnBuilder();
        ipEcnBuilder.setEcn((short) 10);
        IpEcnCaseBuilder ipEcnCaseBuilder = new IpEcnCaseBuilder();
        ipEcnCaseBuilder.setIpEcn(ipEcnBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ipEcnCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.IpEcn.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        IpProtoBuilder ipProtoBuilder = new IpProtoBuilder();
        ipProtoBuilder.setProtocolNumber(OFConstants.OFP_VERSION_1_3);
        IpProtoCaseBuilder ipProtoCaseBuilder = new IpProtoCaseBuilder();
        ipProtoCaseBuilder.setIpProto(ipProtoBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ipProtoCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(IpProto.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        TcpSrcBuilder tcpSrcBuilder = new TcpSrcBuilder();
        org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber port = new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber(43);
        tcpSrcBuilder.setPort(port);
        TcpSrcCaseBuilder tcpSrcCaseBuilder = new TcpSrcCaseBuilder();
        tcpSrcCaseBuilder.setTcpSrc(tcpSrcBuilder.build());
        matchEntryBuilder.setMatchEntryValue(tcpSrcCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.TcpSrc.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        TcpDstBuilder tcpDstBuilder = new TcpDstBuilder();
        tcpDstBuilder.setPort(port);
        TcpDstCaseBuilder tcpDstCaseBuilder = new TcpDstCaseBuilder();
        tcpDstCaseBuilder.setTcpDst(tcpDstBuilder.build());
        matchEntryBuilder.setMatchEntryValue(tcpDstCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(TcpDst.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        UdpSrcBuilder udpSrcBuilder = new UdpSrcBuilder();
        udpSrcBuilder.setPort(port);
        UdpSrcCaseBuilder udpSrcCaseBuilder = new UdpSrcCaseBuilder();
        udpSrcCaseBuilder.setUdpSrc(udpSrcBuilder.build());
        matchEntryBuilder.setMatchEntryValue(udpSrcCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(UdpSrc.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        UdpDstBuilder udpDstBuilder = new UdpDstBuilder();
        udpDstBuilder.setPort(port);
        UdpDstCaseBuilder udpDstCaseBuilder = new UdpDstCaseBuilder();
        udpDstCaseBuilder.setUdpDst(udpDstBuilder.build());
        matchEntryBuilder.setMatchEntryValue(udpDstCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(UdpDst.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        SctpSrcBuilder sctpSrcBuilder = new SctpSrcBuilder();
        sctpSrcBuilder.setPort(port);
        SctpSrcCaseBuilder sctpSrcCaseBuilder = new SctpSrcCaseBuilder();
        sctpSrcCaseBuilder.setSctpSrc(sctpSrcBuilder.build());
        matchEntryBuilder.setMatchEntryValue(sctpSrcCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.SctpSrc.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        SctpDstBuilder sctpDstBuilder = new SctpDstBuilder();
        sctpDstBuilder.setPort(port);
        SctpDstCaseBuilder sctpDstCaseBuilder = new SctpDstCaseBuilder();
        sctpDstCaseBuilder.setSctpDst(sctpDstBuilder.build());
        matchEntryBuilder.setMatchEntryValue(sctpDstCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(SctpDst.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        Icmpv4TypeBuilder icmpv4TypeBuilder = new Icmpv4TypeBuilder();
        icmpv4TypeBuilder.setIcmpv4Type((short) 10);
        Icmpv4TypeCaseBuilder icmpv4TypeCaseBuilder = new Icmpv4TypeCaseBuilder();
        icmpv4TypeCaseBuilder.setIcmpv4Type(icmpv4TypeBuilder.build());
        matchEntryBuilder.setMatchEntryValue(icmpv4TypeCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(Icmpv4Type.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        Icmpv4CodeBuilder icmpv4CodeBuilder = new Icmpv4CodeBuilder();
        icmpv4CodeBuilder.setIcmpv4Code((short) 10);
        Icmpv4CodeCaseBuilder icmpv4CodeCaseBuilder = new Icmpv4CodeCaseBuilder();
        icmpv4CodeCaseBuilder.setIcmpv4Code(icmpv4CodeBuilder.build());
        matchEntryBuilder.setMatchEntryValue(icmpv4CodeCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(Icmpv4Code.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        Icmpv6TypeBuilder icmpv6TypeBuilder = new Icmpv6TypeBuilder();
        icmpv6TypeBuilder.setIcmpv6Type((short) 10);
        Icmpv6TypeCaseBuilder icmpv6TypeCaseBuilder = new Icmpv6TypeCaseBuilder();
        icmpv6TypeCaseBuilder.setIcmpv6Type(icmpv6TypeBuilder.build());
        matchEntryBuilder.setMatchEntryValue(icmpv6TypeCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(Icmpv6Type.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        Icmpv6CodeBuilder icmpv6CodeBuilder = new Icmpv6CodeBuilder();
        icmpv6CodeBuilder.setIcmpv6Code((short) 10);
        Icmpv6CodeCaseBuilder icmpv6CodeCaseBuilder = new Icmpv6CodeCaseBuilder();
        icmpv6CodeCaseBuilder.setIcmpv6Code(icmpv6CodeBuilder.build());
        matchEntryBuilder.setMatchEntryValue(icmpv6CodeCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(Icmpv6Code.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        ArpOpBuilder arpOpBuilder = new ArpOpBuilder();
        arpOpBuilder.setOpCode(42);
        ArpOpCaseBuilder arpOpCaseBuilder = new ArpOpCaseBuilder();
        arpOpCaseBuilder.setArpOp(arpOpBuilder.build());
        matchEntryBuilder.setMatchEntryValue(arpOpCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(ArpOp.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        ArpShaBuilder arpShaBuilder = new ArpShaBuilder();
        arpShaBuilder.setMacAddress(MAC_ADDRESS);
        ArpShaCaseBuilder arpShaCaseBuilder = new ArpShaCaseBuilder();
        arpShaCaseBuilder.setArpSha(arpShaBuilder.build());
        matchEntryBuilder.setMatchEntryValue(arpShaCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(ArpSha.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        ArpThaBuilder arpThaBuilder = new ArpThaBuilder();
        arpThaBuilder.setMacAddress(MAC_ADDRESS);
        ArpThaCaseBuilder arpThaCaseBuilder = new ArpThaCaseBuilder();
        arpThaCaseBuilder.setArpTha(arpThaBuilder.build());
        matchEntryBuilder.setMatchEntryValue(arpThaCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(ArpTha.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        EthDstBuilder ethDstBuilder = new EthDstBuilder();
        ethDstBuilder.setMacAddress(MAC_ADDRESS);
        EthDstCaseBuilder ethDstCaseBuilder = new EthDstCaseBuilder();
        ethDstCaseBuilder.setEthDst(ethDstBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ethDstCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.EthDst.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        EthSrcBuilder ethSrcBuilder = new EthSrcBuilder();
        ethSrcBuilder.setMacAddress(MAC_ADDRESS);
        EthSrcCaseBuilder ethSrcCaseBuilder = new EthSrcCaseBuilder();
        ethSrcCaseBuilder.setEthSrc(ethSrcBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ethSrcCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(EthSrc.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());


        Ipv6FlabelBuilder ipv6FlabelBuilder = new Ipv6FlabelBuilder();
        ipv6FlabelBuilder.setIpv6Flabel(new Ipv6FlowLabel((long) 42));
        Ipv6FlabelCaseBuilder ipv6FlabelCaseBuilder = new Ipv6FlabelCaseBuilder();
        ipv6FlabelCaseBuilder.setIpv6Flabel(ipv6FlabelBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ipv6FlabelCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(Ipv6Flabel.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        Ipv6NdTargetBuilder ipv6NdTargetBuilder = new Ipv6NdTargetBuilder();
        ipv6NdTargetBuilder.setIpv6Address(IPV_6_ADDRESS);
        Ipv6NdTargetCaseBuilder ipv6NdTargetCaseBuilder = new Ipv6NdTargetCaseBuilder();
        ipv6NdTargetCaseBuilder.setIpv6NdTarget(ipv6NdTargetBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ipv6NdTargetCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(Ipv6NdTarget.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        Ipv6NdSllBuilder ipv6NdSllBuilder = new Ipv6NdSllBuilder();
        ipv6NdSllBuilder.setMacAddress(MAC_ADDRESS);
        Ipv6NdSllCaseBuilder ipv6NdSllCaseBuilder = new Ipv6NdSllCaseBuilder();
        ipv6NdSllCaseBuilder.setIpv6NdSll(ipv6NdSllBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ipv6NdSllCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(Ipv6NdSll.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        Ipv6NdTllBuilder ipv6NdTllBuilder = new Ipv6NdTllBuilder();
        ipv6NdTllBuilder.setMacAddress(MAC_ADDRESS);
        Ipv6NdTllCaseBuilder ipv6NdTllCaseBuilder = new Ipv6NdTllCaseBuilder();
        ipv6NdTllCaseBuilder.setIpv6NdTll(ipv6NdTllBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ipv6NdTllCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(Ipv6NdTll.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        Ipv6ExthdrBuilder ipv6ExthdrBuilder = new Ipv6ExthdrBuilder();
        Ipv6ExthdrFlags ipv6ExthdrFlags = new Ipv6ExthdrFlags(true, true, true, true, true, true, true, true, true);
        ipv6ExthdrBuilder.setPseudoField(ipv6ExthdrFlags);
        ipv6ExthdrBuilder.setMask(IPV_6_ADDRESS_MASK);
        Ipv6ExthdrCaseBuilder ipv6ExthdrCaseBuilder = new Ipv6ExthdrCaseBuilder();
        ipv6ExthdrCaseBuilder.setIpv6Exthdr(ipv6ExthdrBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ipv6ExthdrCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(Ipv6Exthdr.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        MplsLabelBuilder mplsLabelBuilder = new MplsLabelBuilder();
        mplsLabelBuilder.setMplsLabel((long) 42);
        MplsLabelCaseBuilder mplsLabelCaseBuilder = new MplsLabelCaseBuilder();
        mplsLabelCaseBuilder.setMplsLabel(mplsLabelBuilder.build());
        matchEntryBuilder.setMatchEntryValue(mplsLabelCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(MplsLabel.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        MplsTcBuilder mplsTcBuilder = new MplsTcBuilder();
        mplsTcBuilder.setTc((short) 0);
        MplsTcCaseBuilder mplsTcCaseBuilder = new MplsTcCaseBuilder();
        mplsTcCaseBuilder.setMplsTc(mplsTcBuilder.build());
        matchEntryBuilder.setMatchEntryValue(mplsTcCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MplsTc.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        MplsBosBuilder mplsBosBuilder = new MplsBosBuilder();
        mplsBosBuilder.setBos(false);
        MplsBosCaseBuilder mplsBosCaseBuilder = new MplsBosCaseBuilder();
        mplsBosCaseBuilder.setMplsBos(mplsBosBuilder.build());
        matchEntryBuilder.setMatchEntryValue(mplsBosCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(MplsBos.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        PbbIsidBuilder pbbIsidBuilder = new PbbIsidBuilder();
        pbbIsidBuilder.setIsid((long) 42);
        PbbIsidCaseBuilder pbbIsidCaseBuilder = new PbbIsidCaseBuilder();
        pbbIsidCaseBuilder.setPbbIsid(pbbIsidBuilder.build());
        matchEntryBuilder.setMatchEntryValue(pbbIsidCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(PbbIsid.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        TunnelIdBuilder tunnelIdBuilder = new TunnelIdBuilder();
        tunnelIdBuilder.setMask(new byte[0]);
        tunnelIdBuilder.setTunnelId(new byte[42]);
        TunnelIdCaseBuilder tunnelIdCaseBuilder = new TunnelIdCaseBuilder();
        tunnelIdCaseBuilder.setTunnelId(tunnelIdBuilder.build());
        matchEntryBuilder.setMatchEntryValue(tunnelIdCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(TunnelId.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        ArpTpaBuilder arpTpaBuilder = new ArpTpaBuilder();
        arpTpaBuilder.setIpv4Address(IPV_4_ADDRESS);
        arpTpaBuilder.setMask(new byte[0]);
        ArpTpaCaseBuilder arpTpaCaseBuilder = new ArpTpaCaseBuilder();
        arpTpaCaseBuilder.setArpTpa(arpTpaBuilder.build());
        matchEntryBuilder.setMatchEntryValue(arpTpaCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(ArpTpa.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        ArpSpaBuilder arpSpaBuilder = new ArpSpaBuilder();
        arpSpaBuilder.setMask(new byte[0]);
        arpSpaBuilder.setIpv4Address(IPV_4_ADDRESS);
        ArpSpaCaseBuilder arpSpaCaseBuilder = new ArpSpaCaseBuilder();
        arpSpaCaseBuilder.setArpSpa(arpSpaBuilder.build());
        matchEntryBuilder.setMatchEntryValue(arpSpaCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(ArpSpa.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        Ipv4SrcBuilder ipv4SrcBuilder = new Ipv4SrcBuilder();
        ipv4SrcBuilder.setIpv4Address(IPV_4_ADDRESS);
        ipv4SrcBuilder.setMask(IPV_4_ADDRESS_MASK);
        Ipv4SrcCaseBuilder ipv4SrcCaseBuilder = new Ipv4SrcCaseBuilder();
        ipv4SrcCaseBuilder.setIpv4Src(ipv4SrcBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ipv4SrcCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(Ipv4Src.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        Ipv4DstBuilder ipv4DstBuilder = new Ipv4DstBuilder();
        ipv4DstBuilder.setIpv4Address(IPV_4_ADDRESS);
        ipv4DstBuilder.setMask(IPV_4_ADDRESS_MASK);
        Ipv4DstCaseBuilder ipv4DstCaseBuilder = new Ipv4DstCaseBuilder();
        ipv4DstCaseBuilder.setIpv4Dst(ipv4DstBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ipv4DstCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(Ipv4Dst.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());


        Ipv6SrcBuilder ipv6SrcBuilder = new Ipv6SrcBuilder();
        ipv6SrcBuilder.setIpv6Address(IPV_6_ADDRESS);
        ipv6SrcBuilder.setMask(IPV_6_ADDRESS_MASK);
        Ipv6SrcCaseBuilder ipv6SrcCaseBuilder = new Ipv6SrcCaseBuilder();
        ipv6SrcCaseBuilder.setIpv6Src(ipv6SrcBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ipv6SrcCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(Ipv6Src.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());

        Ipv6DstBuilder ipv6DstBuilder = new Ipv6DstBuilder();
        ipv6DstBuilder.setIpv6Address(IPV_6_ADDRESS);
        ipv6DstBuilder.setMask(IPV_6_ADDRESS_MASK);
        Ipv6DstCaseBuilder ipv6DstCaseBuilder = new Ipv6DstCaseBuilder();
        ipv6DstCaseBuilder.setIpv6Dst(ipv6DstBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ipv6DstCaseBuilder.build());
        matchEntryBuilder.setOxmMatchField(Ipv6Dst.class);
        fieldClassesAndAugmentations.add(matchEntryBuilder.build());
    }


    @Before
    public void setup() {
        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        flowRemovedTranslator = new FlowRemovedTranslator(convertorManager);
        when(sessionContext.getPrimaryConductor()).thenReturn(connectionConductor);
        when(connectionConductor.getVersion()).thenReturn(OFConstants.OFP_VERSION_1_3);
        when(sessionContext.getFeatures()).thenReturn(featuresOutput);
        when(featuresOutput.getDatapathId()).thenReturn(DATA_PATH_ID);
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
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.MatchBuilder matchBuilder = new MatchBuilder();
        matchBuilder.setMatchEntry(fieldClassesAndAugmentations);
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

    private static void assertTunnelMatch(final Tunnel tunnel) {
        assertEquals(0, tunnel.getTunnelId().intValue());
    }

    private static void assertVlanMatch(final VlanMatch vlanMatch) {
        assertEquals(true, vlanMatch.getVlanId().isVlanIdPresent());
        assertEquals(new Integer(42), vlanMatch.getVlanId().getVlanId().getValue());

        assertEquals((short) 7, vlanMatch.getVlanPcp().getValue().shortValue());
    }

    private static void assertMetada(final org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Metadata metadata) {
        assertEquals(0, metadata.getMetadata().intValue());
    }

    private static void assertProtocolMatchFields(final ProtocolMatchFields protocolMatchFields) {
        assertEquals((short) 0, protocolMatchFields.getMplsBos().shortValue());
        assertEquals(42, protocolMatchFields.getMplsLabel().longValue());
        assertEquals((short) 0, protocolMatchFields.getMplsTc().shortValue());
        assertEquals(42, protocolMatchFields.getPbb().getPbbIsid().longValue());
    }

    private static void assertIpMatch(final IpMatch ipMatch) {
        assertEquals(10, ipMatch.getIpDscp().getValue().longValue());
        assertEquals(10, ipMatch.getIpEcn().shortValue());
        assertEquals(4, ipMatch.getIpProtocol().shortValue());
    }

    private static void assertIcmpV4Match(final Icmpv4Match icmpv4Match) {
        assertEquals(10, icmpv4Match.getIcmpv4Code().longValue());
        assertEquals(10, icmpv4Match.getIcmpv4Type().longValue());
    }

    private static void assertEthernetMatch(final EthernetMatch ethernetMatch) {
        assertEquals(MAC_ADDRESS, ethernetMatch.getEthernetDestination().getAddress());
        assertEquals(MAC_ADDRESS, ethernetMatch.getEthernetSource().getAddress());
        assertEquals(new Long(6), ethernetMatch.getEthernetType().getType().getValue());
    }

}
