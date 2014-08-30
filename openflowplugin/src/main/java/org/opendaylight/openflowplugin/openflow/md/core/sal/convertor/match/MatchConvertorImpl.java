/**
 * Copyright (c) 2013 Ericsson. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.extension.ExtensionResolvers;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowPortsUtil;
import org.opendaylight.openflowplugin.openflow.md.util.OpenflowVersion;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6FlowLabel;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpSourceHardwareAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpSourceHardwareAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpTargetHardwareAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.arp.match.fields.ArpTargetHardwareAddressBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestination;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetDestinationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSource;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ipv6.match.fields.Ipv6ExtHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ipv6.match.fields.Ipv6ExtHeaderBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ipv6.match.fields.Ipv6Label;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ipv6.match.fields.Ipv6LabelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Icmpv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer3Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Layer4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.MetadataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFields;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFieldsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TcpFlagMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TcpFlagMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TunnelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.ArpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv6MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.TunnelIpv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.TunnelIpv4MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.SctpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.SctpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.TcpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.protocol.match.fields.Pbb;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.protocol.match.fields.PbbBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.vlan.match.fields.VlanIdBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.OxmFieldsAction;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.TcpFlagMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanPcpMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanPcpMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanVidMatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.augments.rev131002.VlanVidMatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev130731.actions.grouping.Action;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.OpenflowBasicClass;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.PbbIsid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.SctpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.SctpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TcpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TcpFlag;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TcpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TunnelId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TunnelIpv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.TunnelIpv4Src;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.UdpDst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.UdpSrc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntriesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralExtensionListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;

/**
 * Utility class for converting a MD-SAL Flow into the OF flow mod
 */
public class MatchConvertorImpl implements MatchConvertor<List<MatchEntries>> {
    private static final Logger logger = LoggerFactory.getLogger(MatchConvertorImpl.class);
    static final String PREFIX_SEPARATOR = "/";
    static final Splitter PREFIX_SPLITTER = Splitter.on('/');
    private static final byte[] VLAN_VID_MASK = new byte[] { 16, 0 };
    private static final short PROTO_TCP = 6;
    private static final short PROTO_UDP = 17;
    private static final String noIp = "0.0.0.0/0";

    @Override
    public List<MatchEntries> convert(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match match, final BigInteger datapathid) {
        List<MatchEntries> matchEntriesList = new ArrayList<>();
        if (match == null) return matchEntriesList;
        if (match.getInPort() != null) {
            //TODO: currently this matchconverter is mapped to OF1.3 in MatchReactorMappingFactory. Will need to revisit during 1.4+
            matchEntriesList.add(toOfPort(InPort.class,
                    InventoryDataServiceUtil.portNumberfromNodeConnectorId(OpenflowVersion.OF13, match.getInPort())));
        }

        if (match.getInPhyPort() != null) {
            //TODO: currently this matchconverter is mapped to OF1.3 in MatchReactorMappingFactory. Will need to revisit during 1.4+
            matchEntriesList.add(toOfPort(InPhyPort.class,
                    InventoryDataServiceUtil.portNumberfromNodeConnectorId(OpenflowVersion.OF13, match.getInPhyPort())));
        }

        org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Metadata metadata = match
                .getMetadata();
        if (metadata != null) {
            matchEntriesList.add(toOfMetadata(Metadata.class, metadata.getMetadata(), metadata.getMetadataMask()));
        }

        EthernetMatch ethernetMatch = match.getEthernetMatch();
        if (ethernetMatch != null) {
            EthernetDestination ethernetDestination = ethernetMatch.getEthernetDestination();
            if (ethernetDestination != null) {
                matchEntriesList.add(toOfMacAddress(EthDst.class, ethernetDestination.getAddress(),
                        ethernetDestination.getMask()));
            }

            EthernetSource ethernetSource = ethernetMatch.getEthernetSource();
            if (ethernetSource != null) {
                matchEntriesList
                        .add(toOfMacAddress(EthSrc.class, ethernetSource.getAddress(), ethernetSource.getMask()));
            }

            if (ethernetMatch.getEthernetType() != null) {
                matchEntriesList.add(toOfEthernetType(ethernetMatch.getEthernetType()));
            }
        }

        VlanMatch vlanMatch = match.getVlanMatch();
        if (vlanMatch != null) {
            if (vlanMatch.getVlanId() != null) {
                matchEntriesList.add(toOfVlanVid(vlanMatch.getVlanId()));
            }

            if (vlanMatch.getVlanPcp() != null) {
                matchEntriesList.add(toOfVlanPcp(vlanMatch.getVlanPcp()));
            }
        }

        IpMatch ipMatch = match.getIpMatch();
        if (ipMatch != null) {
            if (ipMatch.getIpDscp() != null) {
                matchEntriesList.add(toOfIpDscp(ipMatch.getIpDscp()));
            }

            if (ipMatch.getIpEcn() != null) {
                matchEntriesList.add(toOfIpEcn(ipMatch.getIpEcn()));
            }

            if (ipMatch.getIpProtocol() != null) {
                matchEntriesList.add(toOfIpProto(ipMatch.getIpProtocol()));
            }

        }

        Layer4Match layer4Match = match.getLayer4Match();
        if (layer4Match != null) {
            if (layer4Match instanceof TcpMatch) {
                TcpMatch tcpMatch = (TcpMatch) layer4Match;
                if (tcpMatch.getTcpSourcePort() != null) {
                    matchEntriesList.add(toOfLayer3Port(TcpSrc.class, tcpMatch.getTcpSourcePort()));
                }

                if (tcpMatch.getTcpDestinationPort() != null) {
                    matchEntriesList.add(toOfLayer3Port(TcpDst.class, tcpMatch.getTcpDestinationPort()));
                }
            } else if (layer4Match instanceof UdpMatch) {
                UdpMatch udpMatch = (UdpMatch) layer4Match;
                if (udpMatch.getUdpSourcePort() != null) {
                    matchEntriesList.add(toOfLayer3Port(UdpSrc.class, udpMatch.getUdpSourcePort()));
                }

                if (udpMatch.getUdpDestinationPort() != null) {
                    matchEntriesList.add(toOfLayer3Port(UdpDst.class, udpMatch.getUdpDestinationPort()));
                }
            } else if (layer4Match instanceof SctpMatch) {
                SctpMatch sctpMatch = (SctpMatch) layer4Match;
                if (sctpMatch.getSctpSourcePort() != null) {
                    matchEntriesList.add(toOfLayer3Port(SctpSrc.class, sctpMatch.getSctpSourcePort()));
                }

                if (sctpMatch.getSctpDestinationPort() != null) {
                    matchEntriesList.add(toOfLayer3Port(SctpDst.class, sctpMatch.getSctpDestinationPort()));
                }
            }
        }

        Icmpv4Match icmpv4Match = match.getIcmpv4Match();
        if (icmpv4Match != null) {
            if (icmpv4Match.getIcmpv4Type() != null) {
                matchEntriesList.add(toOfIcmpv4Type(icmpv4Match.getIcmpv4Type()));
            }

            if (icmpv4Match.getIcmpv4Code() != null) {
                matchEntriesList.add(toOfIcmpv4Code(icmpv4Match.getIcmpv4Code()));
            }
        }

        Icmpv6Match icmpv6Match = match.getIcmpv6Match();
        if (icmpv6Match != null) {
            if (icmpv6Match.getIcmpv6Type() != null) {
                matchEntriesList.add(toOfIcmpv6Type(icmpv6Match.getIcmpv6Type()));
            }

            if (icmpv6Match.getIcmpv6Code() != null) {
                matchEntriesList.add(toOfIcmpv6Code(icmpv6Match.getIcmpv6Code()));
            }
        }

        Layer3Match layer3Match = match.getLayer3Match();
        if (layer3Match != null) {
            if (layer3Match instanceof Ipv4Match) {
                Ipv4Match ipv4Match = (Ipv4Match) layer3Match;
                if (ipv4Match.getIpv4Source() != null) {
                    matchEntriesList.add(toOfIpv4Prefix(Ipv4Src.class, ipv4Match.getIpv4Source()));
                }
                if (ipv4Match.getIpv4Destination() != null) {
                    matchEntriesList.add(toOfIpv4Prefix(Ipv4Dst.class, ipv4Match.getIpv4Destination()));
                }
            }
            if (layer3Match instanceof TunnelIpv4Match) {
                TunnelIpv4Match tunnelIpv4Src = (TunnelIpv4Match) layer3Match;
                if (tunnelIpv4Src.getTunnelIpv4Source() != null) {
                    matchEntriesList.add(NxmExtensionsConvertor.toNxmIpv4Tunnel(TunnelIpv4Src.class, tunnelIpv4Src.getTunnelIpv4Source()));
                }
                if (tunnelIpv4Src.getTunnelIpv4Destination() != null) {
                    matchEntriesList.add(NxmExtensionsConvertor.toNxmIpv4Tunnel(TunnelIpv4Dst.class, tunnelIpv4Src.getTunnelIpv4Destination()));
                }
            } else if (layer3Match instanceof ArpMatch) {
                ArpMatch arpMatch = (ArpMatch) layer3Match;
                if (arpMatch.getArpOp() != null) {
                    matchEntriesList.add(toOfArpOpCode(arpMatch.getArpOp()));
                }

                if (arpMatch.getArpSourceTransportAddress() != null) {
                    matchEntriesList.add(toOfIpv4Prefix(ArpSpa.class, arpMatch.getArpSourceTransportAddress()));
                }

                if (arpMatch.getArpTargetTransportAddress() != null) {
                    matchEntriesList.add(toOfIpv4Prefix(ArpTpa.class, arpMatch.getArpTargetTransportAddress()));
                }

                ArpSourceHardwareAddress arpSourceHardwareAddress = arpMatch.getArpSourceHardwareAddress();
                if (arpSourceHardwareAddress != null) {
                    matchEntriesList.add(toOfMacAddress(ArpSha.class, arpSourceHardwareAddress.getAddress(),
                            arpSourceHardwareAddress.getMask()));
                }

                ArpTargetHardwareAddress arpTargetHardwareAddress = arpMatch.getArpTargetHardwareAddress();
                if (arpTargetHardwareAddress != null) {
                    matchEntriesList.add(toOfMacAddress(ArpTha.class, arpTargetHardwareAddress.getAddress(),
                            arpTargetHardwareAddress.getMask()));
                }
            }

            else if (layer3Match instanceof Ipv6Match) {
                Ipv6Match ipv6Match = (Ipv6Match) layer3Match;
                if (ipv6Match.getIpv6Source() != null) {
                    matchEntriesList.add(toOfIpv6Prefix(Ipv6Src.class, ipv6Match.getIpv6Source()));
                }

                if (ipv6Match.getIpv6Destination() != null) {
                    matchEntriesList.add(toOfIpv6Prefix(Ipv6Dst.class, ipv6Match.getIpv6Destination()));
                }

                if (ipv6Match.getIpv6Label() != null) {
                    matchEntriesList.add(toOfIpv6FlowLabel(ipv6Match.getIpv6Label()));
                }

                if (ipv6Match.getIpv6NdTarget() != null) {
                    matchEntriesList.add(toOfIpv6Address(ipv6Match.getIpv6NdTarget()));
                }

                if (ipv6Match.getIpv6NdSll() != null) {
                    matchEntriesList.add(toOfMacAddress(Ipv6NdSll.class, ipv6Match.getIpv6NdSll(), null));
                }

                if (ipv6Match.getIpv6NdTll() != null) {
                    matchEntriesList.add(toOfMacAddress(Ipv6NdTll.class, ipv6Match.getIpv6NdTll(), null));
                }

                if (ipv6Match.getIpv6ExtHeader() != null) {
                    matchEntriesList.add(toOfIpv6ExtHeader(ipv6Match.getIpv6ExtHeader()));
                }
            }
        }

        ProtocolMatchFields protocolMatchFields = match.getProtocolMatchFields();
        if (protocolMatchFields != null) {
            if (protocolMatchFields.getMplsLabel() != null) {
                matchEntriesList.add(toOfMplsLabel(protocolMatchFields.getMplsLabel()));
            }

            if (protocolMatchFields.getMplsBos() != null) {
                matchEntriesList.add(toOfMplsBos(protocolMatchFields.getMplsBos()));
            }

            if (protocolMatchFields.getMplsTc() != null) {
                matchEntriesList.add(toOfMplsTc(protocolMatchFields.getMplsTc()));
            }

            if (protocolMatchFields.getPbb() != null) {
                matchEntriesList.add(toOfMplsPbb(protocolMatchFields.getPbb()));
            }
        }

        //FIXME: move to extensible support
        // TODO: Move to seperate bundle as soon as OF extensibility is supported by ofplugin/java
        TcpFlagMatch tcpFlagMatch = match.getTcpFlagMatch();
        if (tcpFlagMatch != null) {
            if (tcpFlagMatch.getTcpFlag() != null) {
                matchEntriesList.add(NxmExtensionsConvertor.toNxmTcpFlag(tcpFlagMatch.getTcpFlag()));
            }
        }

        org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Tunnel tunnel = match
                .getTunnel();
        if (tunnel != null) {
            matchEntriesList.add(toOfMetadata(TunnelId.class, tunnel.getTunnelId(), tunnel.getTunnelMask()));
        }
        
        
        /**
         * TODO: EXTENSION PROPOSAL (match, MD-SAL to OFJava)
         * - we might need version for conversion and for key
         * - sanitize NPE
         */
        Optional<GeneralExtensionListGrouping> extensionListOpt = ExtensionResolvers.getMatchExtensionResolver().getExtension(match);
        if (extensionListOpt.isPresent()) {
            for (ExtensionList extensionItem : extensionListOpt.get().getExtensionList()) {
                // TODO: get real version
                ConverterExtensionKey<? extends ExtensionKey> key = new ConverterExtensionKey<>(extensionItem.getExtensionKey(), OFConstants.OFP_VERSION_1_3);
                ConvertorToOFJava<MatchEntries> convertor = 
                        OFSessionUtil.getExtensionConvertorProvider().getConverter(key);
                MatchEntries ofMatch = convertor.convert(extensionItem.getExtension());
                matchEntriesList.add(ofMatch);
            }
        }

        return matchEntriesList;
    }

    /**
     * Method convert Openflow 1.0 specific flow match to MD-SAL format flow
     * match
     *
     * @param match
     * @return
     * @author avishnoi@in.ibm.com
     */
    public static Match fromOFMatchV10ToSALMatch(final MatchV10 swMatch, final BigInteger datapathid,  final OpenflowVersion ofVersion) {
        MatchBuilder matchBuilder = new MatchBuilder();
        EthernetMatchBuilder ethMatchBuilder = new EthernetMatchBuilder();
        VlanMatchBuilder vlanMatchBuilder = new VlanMatchBuilder();
        Ipv4MatchBuilder ipv4MatchBuilder = new Ipv4MatchBuilder();
        IpMatchBuilder ipMatchBuilder = new IpMatchBuilder();
        if (!swMatch.getWildcards().isINPORT().booleanValue() && swMatch.getInPort() != null) {
            matchBuilder.setInPort(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(datapathid,
                    (long) swMatch.getInPort(), ofVersion));
        }

        if (!swMatch.getWildcards().isDLSRC().booleanValue() && swMatch.getDlSrc() != null) {
            EthernetSourceBuilder ethSrcBuilder = new EthernetSourceBuilder();
            ethSrcBuilder.setAddress(swMatch.getDlSrc());
            ethMatchBuilder.setEthernetSource(ethSrcBuilder.build());
            matchBuilder.setEthernetMatch(ethMatchBuilder.build());
        }
        if (!swMatch.getWildcards().isDLDST().booleanValue() && swMatch.getDlDst() != null) {
            EthernetDestinationBuilder ethDstBuilder = new EthernetDestinationBuilder();
            ethDstBuilder.setAddress(swMatch.getDlDst());
            ethMatchBuilder.setEthernetDestination(ethDstBuilder.build());
            matchBuilder.setEthernetMatch(ethMatchBuilder.build());
        }
        if (!swMatch.getWildcards().isDLTYPE().booleanValue() && swMatch.getDlType() != null) {
            EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
            ethTypeBuilder.setType(new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType(
                    (long) swMatch.getDlType()));
            ethMatchBuilder.setEthernetType(ethTypeBuilder.build());
            matchBuilder.setEthernetMatch(ethMatchBuilder.build());
        }
        if (!swMatch.getWildcards().isDLVLAN().booleanValue() && swMatch.getDlVlan() != null) {
            VlanIdBuilder vlanIdBuilder = new VlanIdBuilder();
            int vlanId = (swMatch.getDlVlan() == ((int) 0xffff)) ? 0 : swMatch.getDlVlan();
            vlanIdBuilder.setVlanId(new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId(vlanId));
            vlanIdBuilder.setVlanIdPresent(vlanId == 0 ? false: true);
            vlanMatchBuilder.setVlanId(vlanIdBuilder.build());
            matchBuilder.setVlanMatch(vlanMatchBuilder.build());
        }
        if (!swMatch.getWildcards().isDLVLANPCP().booleanValue() && swMatch.getDlVlanPcp() != null) {
            vlanMatchBuilder.setVlanPcp(new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp(
                    swMatch.getDlVlanPcp()));
            matchBuilder.setVlanMatch(vlanMatchBuilder.build());
        }
        if (!swMatch.getWildcards().isDLTYPE().booleanValue() && swMatch.getNwSrc() != null) {
            String ipv4PrefixStr = swMatch.getNwSrc().getValue();
            if (swMatch.getNwSrcMask() != null) {
                ipv4PrefixStr += PREFIX_SEPARATOR + swMatch.getNwSrcMask();
            }
            if (!ipv4PrefixStr.equals(noIp)) {
                ipv4MatchBuilder.setIpv4Source(new Ipv4Prefix(ipv4PrefixStr));
                matchBuilder.setLayer3Match(ipv4MatchBuilder.build());
            }
        }
        if (!swMatch.getWildcards().isDLTYPE().booleanValue() && swMatch.getNwDst() != null) {
            String ipv4PrefixStr = swMatch.getNwDst().getValue();
            if (swMatch.getNwDstMask() != null) {
                ipv4PrefixStr += PREFIX_SEPARATOR + swMatch.getNwDstMask();
            }
            if (!ipv4PrefixStr.equals(noIp)) {
                ipv4MatchBuilder.setIpv4Destination(new Ipv4Prefix(ipv4PrefixStr));
                matchBuilder.setLayer3Match(ipv4MatchBuilder.build());
            }
        }
        if (!swMatch.getWildcards().isNWPROTO().booleanValue() && swMatch.getNwProto() != null) {
            ipMatchBuilder.setIpProtocol(swMatch.getNwProto());
            matchBuilder.setIpMatch(ipMatchBuilder.build());
        }
        if (!swMatch.getWildcards().isNWPROTO().booleanValue() && swMatch.getNwProto() == PROTO_TCP) {
            TcpMatchBuilder tcpMatchBuilder = new TcpMatchBuilder();
            if (!swMatch.getWildcards().isTPSRC().booleanValue() && swMatch.getTpSrc() != null) {
                tcpMatchBuilder
                        .setTcpSourcePort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber(
                                swMatch.getTpSrc()));
            }
            if (!swMatch.getWildcards().isTPDST().booleanValue() && swMatch.getTpDst() != null) {
                tcpMatchBuilder
                        .setTcpDestinationPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber(
                                swMatch.getTpDst()));
            }

            if (!swMatch.getWildcards().isTPSRC().booleanValue() || !swMatch.getWildcards().isTPDST().booleanValue()) {
                matchBuilder.setLayer4Match(tcpMatchBuilder.build());
            }
        }
        if (!swMatch.getWildcards().isNWPROTO().booleanValue() && swMatch.getNwProto() == PROTO_UDP) {
            UdpMatchBuilder udpMatchBuilder = new UdpMatchBuilder();
            if (!swMatch.getWildcards().isTPSRC().booleanValue() && swMatch.getTpSrc() != null) {
                udpMatchBuilder
                        .setUdpSourcePort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber(
                                swMatch.getTpSrc()));
            }
            if (!swMatch.getWildcards().isTPDST().booleanValue() && swMatch.getTpDst() != null) {
                udpMatchBuilder
                        .setUdpDestinationPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber(
                                swMatch.getTpDst()));
            }

            if (!swMatch.getWildcards().isTPSRC().booleanValue() || !swMatch.getWildcards().isTPDST().booleanValue()) {
                matchBuilder.setLayer4Match(udpMatchBuilder.build());
            }
        }
        if (!swMatch.getWildcards().isNWTOS().booleanValue() && swMatch.getNwTos() != null) {
            // DSCP default value is 0 from the library but controller side it
            // is null.
            // look if there better solution
            if (0 != swMatch.getNwTos()) {
                ipMatchBuilder.setIpDscp(new Dscp(swMatch.getNwTos()));
            }
            matchBuilder.setIpMatch(ipMatchBuilder.build());
        }

        return matchBuilder.build();
    }

    /**
     * Method converts Openflow 1.3+ specific flow match to MD-SAL format flow
     * match
     *
     * @param match
     * @param swMatch 
     * @param datapathid 
     * @param ofVersion 
     * @return md-sal match instance
     * @author avishnoi@in.ibm.com
     */
    public static MatchBuilder fromOFMatchToSALMatch(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.grouping.Match swMatch,
            final BigInteger datapathid, final OpenflowVersion ofVersion) {
        return OfMatchToSALMatchConvertor(swMatch.getMatchEntries(), datapathid, ofVersion);
    }

    private static MatchBuilder OfMatchToSALMatchConvertor(List<MatchEntries> swMatchList, final BigInteger datapathid, 
            OpenflowVersion ofVersion){
        
        MatchBuilder matchBuilder = new MatchBuilder();
        EthernetMatchBuilder ethMatchBuilder = new EthernetMatchBuilder();
        VlanMatchBuilder vlanMatchBuilder = new VlanMatchBuilder();
        IpMatchBuilder ipMatchBuilder = new IpMatchBuilder();
        TcpMatchBuilder tcpMatchBuilder = new TcpMatchBuilder();
        UdpMatchBuilder udpMatchBuilder = new UdpMatchBuilder();
        SctpMatchBuilder sctpMatchBuilder = new SctpMatchBuilder();
        Icmpv4MatchBuilder icmpv4MatchBuilder = new Icmpv4MatchBuilder();
        Icmpv6MatchBuilder icmpv6MatchBuilder = new Icmpv6MatchBuilder();
        Ipv4MatchBuilder ipv4MatchBuilder = new Ipv4MatchBuilder();
        ArpMatchBuilder arpMatchBuilder = new ArpMatchBuilder();
        Ipv6MatchBuilder ipv6MatchBuilder = new Ipv6MatchBuilder();
        ProtocolMatchFieldsBuilder protocolMatchFieldsBuilder = new ProtocolMatchFieldsBuilder();
        TcpFlagMatchBuilder tcpFlagMatchBuilder = new TcpFlagMatchBuilder();
        TunnelIpv4MatchBuilder tunnelIpv4MatchBuilder = new TunnelIpv4MatchBuilder();

        for (MatchEntries ofMatch : swMatchList) {

            if (ofMatch.getOxmMatchField().equals(InPort.class)) {
                PortNumberMatchEntry portNumber = ofMatch.getAugmentation(PortNumberMatchEntry.class);
                if(portNumber != null){
                    Long portNo = portNumber.getPortNumber().getValue();
                    String logicalName = OpenflowPortsUtil.getPortLogicalName(ofVersion, portNo);
                    matchBuilder.setInPort(new NodeConnectorId(logicalName == null? portNo.toString() : logicalName));
                }
            } else if (ofMatch.getOxmMatchField().equals(InPhyPort.class)) {
                PortNumberMatchEntry portNumber = ofMatch.getAugmentation(PortNumberMatchEntry.class);
                matchBuilder.setInPhyPort(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(datapathid,
                        portNumber.getPortNumber().getValue(), ofVersion));
            } else if (ofMatch.getOxmMatchField().equals(Metadata.class)) {
                MetadataBuilder metadataBuilder = new MetadataBuilder();
                MetadataMatchEntry metadataMatchEntry = ofMatch.getAugmentation(MetadataMatchEntry.class);
                if (metadataMatchEntry != null) {
                    metadataBuilder.setMetadata(new BigInteger(1, metadataMatchEntry.getMetadata()));
                    MaskMatchEntry maskMatchEntry = ofMatch.getAugmentation(MaskMatchEntry.class);
                    if (maskMatchEntry != null) {
                        metadataBuilder.setMetadataMask(new BigInteger(OFConstants.SIGNUM_UNSIGNED, maskMatchEntry
                                .getMask()));
                    }
                    matchBuilder.setMetadata(metadataBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(EthSrc.class)) {
                MacAddressMatchEntry macAddressMatchEntry = ofMatch.getAugmentation(MacAddressMatchEntry.class);
                if (macAddressMatchEntry != null) {
                    EthernetSourceBuilder ethSourceBuilder = new EthernetSourceBuilder();
                    ethSourceBuilder.setAddress(macAddressMatchEntry.getMacAddress());
                    ethMatchBuilder.setEthernetSource(ethSourceBuilder.build());
                    matchBuilder.setEthernetMatch(ethMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(EthDst.class)) {
                MacAddressMatchEntry macAddressMatchEntry = ofMatch.getAugmentation(MacAddressMatchEntry.class);
                if (macAddressMatchEntry != null) {
                    EthernetDestinationBuilder ethDestinationBuilder = new EthernetDestinationBuilder();
                    ethDestinationBuilder.setAddress(macAddressMatchEntry.getMacAddress());
                    ethMatchBuilder.setEthernetDestination(ethDestinationBuilder.build());
                    matchBuilder.setEthernetMatch(ethMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(EthType.class)) {
                EthTypeMatchEntry ethTypeMatchEntry = ofMatch.getAugmentation(EthTypeMatchEntry.class);
                if (ethTypeMatchEntry != null) {
                    EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
                    ethTypeBuilder
                            .setType(new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType(
                                    (long) ethTypeMatchEntry.getEthType().getValue()));
                    ethMatchBuilder.setEthernetType(ethTypeBuilder.build());
                    matchBuilder.setEthernetMatch(ethMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(VlanVid.class)) {
                VlanVidMatchEntry vlanVidMatchEntry = ofMatch.getAugmentation(VlanVidMatchEntry.class);
                if (vlanVidMatchEntry != null) {
                    VlanIdBuilder vlanBuilder = new VlanIdBuilder();
                    vlanBuilder.setVlanId(new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId(
                            vlanVidMatchEntry.getVlanVid()));
                    vlanMatchBuilder.setVlanId(vlanBuilder.build());
                    matchBuilder.setVlanMatch(vlanMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(VlanPcp.class)) {
                VlanPcpMatchEntry vlanPcpMatchEntry = ofMatch.getAugmentation(VlanPcpMatchEntry.class);
                if (vlanPcpMatchEntry != null) {
                    vlanMatchBuilder
                            .setVlanPcp(new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp(
                                    vlanPcpMatchEntry.getVlanPcp()));
                    matchBuilder.setVlanMatch(vlanMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(IpDscp.class)) {
                DscpMatchEntry dscpMatchEntry = ofMatch.getAugmentation(DscpMatchEntry.class);
                if (dscpMatchEntry != null) {
                    ipMatchBuilder.setIpDscp(new Dscp(dscpMatchEntry.getDscp().getValue()));
                    matchBuilder.setIpMatch(ipMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(IpEcn.class)) {
                EcnMatchEntry ecnMatchEntry = ofMatch.getAugmentation(EcnMatchEntry.class);
                if (ecnMatchEntry != null) {
                    ipMatchBuilder.setIpEcn(ecnMatchEntry.getEcn());
                    matchBuilder.setIpMatch(ipMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(IpProto.class)) {
                ProtocolNumberMatchEntry protocolNumberMatchEntry = ofMatch
                        .getAugmentation(ProtocolNumberMatchEntry.class);
                if (protocolNumberMatchEntry != null) {
                    ipMatchBuilder.setIpProtocol(protocolNumberMatchEntry.getProtocolNumber());
                    matchBuilder.setIpMatch(ipMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(TcpSrc.class)) {
                PortMatchEntry portMatchEntry = ofMatch.getAugmentation(PortMatchEntry.class);
                if (portMatchEntry != null) {
                    tcpMatchBuilder.setTcpSourcePort(portMatchEntry.getPort());
                    matchBuilder.setLayer4Match(tcpMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(TcpDst.class)) {
                PortMatchEntry portMatchEntry = ofMatch.getAugmentation(PortMatchEntry.class);
                if (portMatchEntry != null) {
                    tcpMatchBuilder.setTcpDestinationPort(portMatchEntry.getPort());
                    matchBuilder.setLayer4Match(tcpMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(UdpSrc.class)) {
                PortMatchEntry portMatchEntry = ofMatch.getAugmentation(PortMatchEntry.class);
                if (portMatchEntry != null) {
                    udpMatchBuilder.setUdpSourcePort(portMatchEntry.getPort());
                    matchBuilder.setLayer4Match(udpMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(UdpDst.class)) {
                PortMatchEntry portMatchEntry = ofMatch.getAugmentation(PortMatchEntry.class);
                if (portMatchEntry != null) {
                    udpMatchBuilder.setUdpDestinationPort(portMatchEntry.getPort());
                    matchBuilder.setLayer4Match(udpMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(SctpSrc.class)) {
                PortMatchEntry portMatchEntry = ofMatch.getAugmentation(PortMatchEntry.class);
                if (portMatchEntry != null) {
                    sctpMatchBuilder.setSctpSourcePort(portMatchEntry.getPort());
                    matchBuilder.setLayer4Match(sctpMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(SctpDst.class)) {
                PortMatchEntry portMatchEntry = ofMatch.getAugmentation(PortMatchEntry.class);
                if (portMatchEntry != null) {
                    sctpMatchBuilder.setSctpDestinationPort(portMatchEntry.getPort());
                    matchBuilder.setLayer4Match(sctpMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(Icmpv4Type.class)) {
                Icmpv4TypeMatchEntry icmpv4TypeMatchEntry = ofMatch.getAugmentation(Icmpv4TypeMatchEntry.class);
                if (icmpv4TypeMatchEntry != null) {
                    icmpv4MatchBuilder.setIcmpv4Type(icmpv4TypeMatchEntry.getIcmpv4Type());
                    matchBuilder.setIcmpv4Match(icmpv4MatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(Icmpv4Code.class)) {
                Icmpv4CodeMatchEntry icmpv4CodeMatchEntry = ofMatch.getAugmentation(Icmpv4CodeMatchEntry.class);
                if (icmpv4CodeMatchEntry != null) {
                    icmpv4MatchBuilder.setIcmpv4Code(icmpv4CodeMatchEntry.getIcmpv4Code());
                    matchBuilder.setIcmpv4Match(icmpv4MatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(Icmpv6Type.class)) {
                Icmpv6TypeMatchEntry icmpv6TypeMatchEntry = ofMatch.getAugmentation(Icmpv6TypeMatchEntry.class);
                if (icmpv6TypeMatchEntry != null) {
                    icmpv6MatchBuilder.setIcmpv6Type(icmpv6TypeMatchEntry.getIcmpv6Type());
                    matchBuilder.setIcmpv6Match(icmpv6MatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(Icmpv6Code.class)) {
                Icmpv6CodeMatchEntry icmpv6CodeMatchEntry = ofMatch.getAugmentation(Icmpv6CodeMatchEntry.class);
                if (icmpv6CodeMatchEntry != null) {
                    icmpv6MatchBuilder.setIcmpv6Code(icmpv6CodeMatchEntry.getIcmpv6Code());
                    matchBuilder.setIcmpv6Match(icmpv6MatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(Ipv4Src.class)
                    || ofMatch.getOxmMatchField().equals(Ipv4Dst.class)) {
                Ipv4AddressMatchEntry ipv4AddressMatchEntry = ofMatch.getAugmentation(Ipv4AddressMatchEntry.class);
                if (ipv4AddressMatchEntry != null) {
                    String ipv4PrefixStr = ipv4AddressMatchEntry.getIpv4Address().getValue();
                    MaskMatchEntry maskMatchEntry = ofMatch.getAugmentation(MaskMatchEntry.class);
                    if (maskMatchEntry != null) {
                        int receivedMask = ByteBuffer.wrap(maskMatchEntry.getMask()).getInt();
                        int shiftCount=0;
                        while(receivedMask != 0xffffffff){
                            receivedMask = receivedMask >> 1;
                            shiftCount++;
                        }
                        ipv4PrefixStr += PREFIX_SEPARATOR + (32-shiftCount);
                    }else{
                        //Openflow Spec : 1.3.2
                        //An all-one-bits oxm_mask is equivalent to specifying 0 for oxm_hasmask and omitting oxm_mask.
                        // So when user specify 32 as a mast, switch omit that mast and we get null as a mask in flow
                        // statistics response.

                        ipv4PrefixStr+=PREFIX_SEPARATOR + "32";
                    }
                    if (ofMatch.getOxmMatchField().equals(Ipv4Src.class)) {
                        ipv4MatchBuilder.setIpv4Source(new Ipv4Prefix(ipv4PrefixStr));
                    }
                    if (ofMatch.getOxmMatchField().equals(Ipv4Dst.class)) {
                        ipv4MatchBuilder.setIpv4Destination(new Ipv4Prefix(ipv4PrefixStr));
                    }
                    matchBuilder.setLayer3Match(ipv4MatchBuilder.build());
                }
            }

            else if (ofMatch.getOxmMatchField().equals(TunnelIpv4Dst.class)
                    || ofMatch.getOxmMatchField().equals(TunnelIpv4Src.class)) {
                Ipv4AddressMatchEntry ipv4AddressMatchEntry = ofMatch.getAugmentation(Ipv4AddressMatchEntry.class);
                if (ipv4AddressMatchEntry != null) {
                    String ipv4PrefixStr = ipv4AddressMatchEntry.getIpv4Address().getValue();
                    MaskMatchEntry maskMatchEntry = ofMatch.getAugmentation(MaskMatchEntry.class);
                    if (maskMatchEntry != null) {
                        ipv4PrefixStr += PREFIX_SEPARATOR + ByteBuffer.wrap(maskMatchEntry.getMask()).getInt();
                    }
                    if (ofMatch.getOxmMatchField().equals(TunnelIpv4Dst.class)) {
                        tunnelIpv4MatchBuilder.setTunnelIpv4Destination(new Ipv4Prefix(ipv4PrefixStr));
                    }
                    if (ofMatch.getOxmMatchField().equals(TunnelIpv4Src.class)) {
                        tunnelIpv4MatchBuilder.setTunnelIpv4Source(new Ipv4Prefix(ipv4PrefixStr));
                    }
                    matchBuilder.setLayer3Match(tunnelIpv4MatchBuilder.build());
                }
            }
            else if (ofMatch.getOxmMatchField().equals(ArpOp.class)) {
                OpCodeMatchEntry opCodeMatchEntry = ofMatch.getAugmentation(OpCodeMatchEntry.class);
                if (opCodeMatchEntry != null) {
                    arpMatchBuilder.setArpOp(opCodeMatchEntry.getOpCode());
                    matchBuilder.setLayer3Match(arpMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(ArpSpa.class)
                    || ofMatch.getOxmMatchField().equals(ArpTpa.class)) {
                Ipv4AddressMatchEntry ipv4AddressMatchEntry = ofMatch.getAugmentation(Ipv4AddressMatchEntry.class);
                if (ipv4AddressMatchEntry != null) {
                    String ipv4PrefixStr = ipv4AddressMatchEntry.getIpv4Address().getValue();
                    MaskMatchEntry maskMatchEntry = ofMatch.getAugmentation(MaskMatchEntry.class);
                    if (maskMatchEntry != null) {
                        ipv4PrefixStr += PREFIX_SEPARATOR + ByteBuffer.wrap(maskMatchEntry.getMask()).getInt();
                    }
                    if (ofMatch.getOxmMatchField().equals(ArpSpa.class)) {
                        arpMatchBuilder.setArpSourceTransportAddress(new Ipv4Prefix(ipv4PrefixStr));
                    }
                    if (ofMatch.getOxmMatchField().equals(ArpTpa.class)) {
                        arpMatchBuilder.setArpTargetTransportAddress(new Ipv4Prefix(ipv4PrefixStr));
                    }
                    matchBuilder.setLayer3Match(arpMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(ArpSha.class)
                    || ofMatch.getOxmMatchField().equals(ArpTha.class)) {
                MacAddressMatchEntry macAddressMatchEntry = ofMatch.getAugmentation(MacAddressMatchEntry.class);
                if (macAddressMatchEntry != null) {
                    if (ofMatch.getOxmMatchField().equals(ArpSha.class)) {
                        ArpSourceHardwareAddressBuilder arpSourceHardwareAddressBuilder = new ArpSourceHardwareAddressBuilder();
                        arpSourceHardwareAddressBuilder.setAddress(macAddressMatchEntry.getMacAddress());
                        MaskMatchEntry maskMatchEntry = ofMatch.getAugmentation(MaskMatchEntry.class);
                        if (maskMatchEntry != null) {
                            arpSourceHardwareAddressBuilder.setMask(new MacAddress(ByteBufUtils
                                    .macAddressToString(maskMatchEntry.getMask())));
                        }
                        arpMatchBuilder.setArpSourceHardwareAddress(arpSourceHardwareAddressBuilder.build());
                        matchBuilder.setLayer3Match(arpMatchBuilder.build());
                    }
                    if (ofMatch.getOxmMatchField().equals(ArpTha.class)) {
                        ArpTargetHardwareAddressBuilder arpTargetHardwareAddressBuilder = new ArpTargetHardwareAddressBuilder();
                        arpTargetHardwareAddressBuilder.setAddress(macAddressMatchEntry.getMacAddress());
                        MaskMatchEntry maskMatchEntry = ofMatch.getAugmentation(MaskMatchEntry.class);
                        if (maskMatchEntry != null) {
                            arpTargetHardwareAddressBuilder.setMask(new MacAddress(ByteBufUtils
                                    .macAddressToString(maskMatchEntry.getMask())));
                        }
                        arpMatchBuilder.setArpTargetHardwareAddress(arpTargetHardwareAddressBuilder.build());
                        matchBuilder.setLayer3Match(arpMatchBuilder.build());
                    }
                }
            } else if (ofMatch.getOxmMatchField().equals(Ipv6Src.class)
                    || ofMatch.getOxmMatchField().equals(Ipv6Dst.class)) {
                Ipv6AddressMatchEntry ipv6AddressMatchEntry = ofMatch.getAugmentation(Ipv6AddressMatchEntry.class);
                if (ipv6AddressMatchEntry != null) {
                    String ipv6PrefixStr = ipv6AddressMatchEntry.getIpv6Address().getValue();
                    MaskMatchEntry maskMatchEntry = ofMatch.getAugmentation(MaskMatchEntry.class);
                    if(maskMatchEntry != null){
                        ipv6PrefixStr += PREFIX_SEPARATOR
                                + MatchConvertorUtil.ipv6NetmaskArrayToCIDRValue(maskMatchEntry.getMask());
                    }
                        
                    if (ofMatch.getOxmMatchField().equals(Ipv6Src.class)) {
                        ipv6MatchBuilder.setIpv6Source(new Ipv6Prefix(ipv6PrefixStr));
                    }
                    if (ofMatch.getOxmMatchField().equals(Ipv6Dst.class)) {
                        ipv6MatchBuilder.setIpv6Destination(new Ipv6Prefix(ipv6PrefixStr));
                    }
                    matchBuilder.setLayer3Match(ipv6MatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(Ipv6Flabel.class)) {
                Ipv6FlabelMatchEntry ipv6FlabelMatchEntry = ofMatch.getAugmentation(Ipv6FlabelMatchEntry.class);
                if (ipv6FlabelMatchEntry != null) {
                    Ipv6LabelBuilder ipv6LabelBuilder = new Ipv6LabelBuilder();
                    ipv6LabelBuilder.setIpv6Flabel(new Ipv6FlowLabel(ipv6FlabelMatchEntry.getIpv6Flabel()));
                    MaskMatchEntry maskMatchEntry = ofMatch.getAugmentation(MaskMatchEntry.class);
                    if (maskMatchEntry != null) {
                        ipv6LabelBuilder.setFlabelMask(new Ipv6FlowLabel(Long.valueOf(ByteUtil
                                .bytesToUnsignedInt(maskMatchEntry.getMask()))));
                    }
                    ipv6MatchBuilder.setIpv6Label(ipv6LabelBuilder.build());
                    matchBuilder.setLayer3Match(ipv6MatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(Ipv6NdTarget.class)) {
                Ipv6AddressMatchEntry ipv6AddressMatchEntry = ofMatch.getAugmentation(Ipv6AddressMatchEntry.class);
                if (ipv6AddressMatchEntry != null) {
                    ipv6MatchBuilder.setIpv6NdTarget(ipv6AddressMatchEntry.getIpv6Address());
                    matchBuilder.setLayer3Match(ipv6MatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(Ipv6NdSll.class)) {
                MacAddressMatchEntry macAddressMatchEntry = ofMatch.getAugmentation(MacAddressMatchEntry.class);
                if (macAddressMatchEntry != null) {
                    ipv6MatchBuilder.setIpv6NdSll(macAddressMatchEntry.getMacAddress());
                    matchBuilder.setLayer3Match(ipv6MatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(Ipv6NdTll.class)) {
                MacAddressMatchEntry macAddressMatchEntry = ofMatch.getAugmentation(MacAddressMatchEntry.class);
                if (macAddressMatchEntry != null) {
                    ipv6MatchBuilder.setIpv6NdTll(macAddressMatchEntry.getMacAddress());
                    matchBuilder.setLayer3Match(ipv6MatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(Ipv6Exthdr.class)) {
                PseudoFieldMatchEntry pseudoFieldMatchEntry = ofMatch.getAugmentation(PseudoFieldMatchEntry.class);
                if (pseudoFieldMatchEntry != null) {
                    Ipv6ExtHeaderBuilder ipv6ExtHeaderBuilder = new Ipv6ExtHeaderBuilder();

                    Ipv6ExthdrFlags pField = pseudoFieldMatchEntry.getPseudoField();
                    Integer bitmap = MatchConvertorUtil.ipv6ExthdrFlagsToInt(pField);

                    ipv6ExtHeaderBuilder.setIpv6Exthdr(bitmap);
                    MaskMatchEntry maskMatchEntry = ofMatch.getAugmentation(MaskMatchEntry.class);
                    if (maskMatchEntry != null) {
                        ipv6ExtHeaderBuilder.setIpv6ExthdrMask(ByteUtil.bytesToUnsignedShort(maskMatchEntry.getMask()));
                    }
                    ipv6MatchBuilder.setIpv6ExtHeader(ipv6ExtHeaderBuilder.build());
                    matchBuilder.setLayer3Match(ipv6MatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(MplsLabel.class)) {
                MplsLabelMatchEntry mplsLabelMatchEntry = ofMatch.getAugmentation(MplsLabelMatchEntry.class);
                if (mplsLabelMatchEntry != null) {
                    protocolMatchFieldsBuilder.setMplsLabel(mplsLabelMatchEntry.getMplsLabel());
                    matchBuilder.setProtocolMatchFields(protocolMatchFieldsBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(MplsBos.class)) {
                BosMatchEntry bosMatchEntry = ofMatch.getAugmentation(BosMatchEntry.class);
                if (bosMatchEntry != null) {
                    protocolMatchFieldsBuilder.setMplsBos(bosMatchEntry.isBos() ? (short) 1 : (short) 0);
                    matchBuilder.setProtocolMatchFields(protocolMatchFieldsBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(MplsTc.class)) {
                TcMatchEntry tcMatchEntry = ofMatch.getAugmentation(TcMatchEntry.class);
                if (tcMatchEntry != null) {
                    protocolMatchFieldsBuilder.setMplsTc(tcMatchEntry.getTc());
                    matchBuilder.setProtocolMatchFields(protocolMatchFieldsBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(PbbIsid.class)) {
                IsidMatchEntry isidMatchEntry = ofMatch.getAugmentation(IsidMatchEntry.class);
                if (isidMatchEntry != null) {
                    PbbBuilder pbbBuilder = new PbbBuilder();
                    pbbBuilder.setPbbIsid(isidMatchEntry.getIsid());
                    MaskMatchEntry maskMatchEntry = ofMatch.getAugmentation(MaskMatchEntry.class);
                    if (maskMatchEntry != null) {
                        pbbBuilder.setPbbMask(ByteUtil.bytesToUnsignedInt(maskMatchEntry.getMask()));
                    }
                    protocolMatchFieldsBuilder.setPbb(pbbBuilder.build());
                    matchBuilder.setProtocolMatchFields(protocolMatchFieldsBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(TunnelId.class)) {
                TunnelBuilder tunnelBuilder = new TunnelBuilder();
                MetadataMatchEntry metadataMatchEntry = ofMatch.getAugmentation(MetadataMatchEntry.class);
                if (metadataMatchEntry != null) {
                    tunnelBuilder.setTunnelId(new BigInteger(1, metadataMatchEntry.getMetadata()));
                    MaskMatchEntry maskMatchEntry = ofMatch.getAugmentation(MaskMatchEntry.class);
                    if (maskMatchEntry != null) {
                        tunnelBuilder.setTunnelMask(new BigInteger(OFConstants.SIGNUM_UNSIGNED, maskMatchEntry
                                .getMask()));
                    }
                    matchBuilder.setTunnel(tunnelBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(TcpFlag.class)) {
                //FIXME: move to extensible support
                TcpFlagMatchEntry tcpFlagMatch = ofMatch.getAugmentation(TcpFlagMatchEntry.class);
                if (tcpFlagMatch != null) {
                    tcpFlagMatchBuilder.setTcpFlag(tcpFlagMatch.getTcpFlag());
                    matchBuilder.setTcpFlagMatch(tcpFlagMatchBuilder.build());
                }
            } 
        }
        return matchBuilder;
    }
    private static MatchEntries toOfMplsPbb(final Pbb pbb) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        boolean hasmask = false;
        matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntriesBuilder.setOxmMatchField(PbbIsid.class);
        IsidMatchEntryBuilder isidBuilder = new IsidMatchEntryBuilder();
        isidBuilder.setIsid(pbb.getPbbIsid());
        matchEntriesBuilder.addAugmentation(IsidMatchEntry.class, isidBuilder.build());
        if (pbb.getPbbMask() != null) {
            hasmask = true;
            addMaskAugmentation(matchEntriesBuilder, ByteUtil.unsignedIntToBytes(pbb.getPbbMask()));
        }
        matchEntriesBuilder.setHasMask(hasmask);
        return matchEntriesBuilder.build();
    }

    private static MatchEntries toOfMplsTc(final Short mplsTc) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntriesBuilder.setHasMask(false);
        matchEntriesBuilder.setOxmMatchField(MplsTc.class);
        TcMatchEntryBuilder tcBuilder = new TcMatchEntryBuilder();
        tcBuilder.setTc(mplsTc);
        matchEntriesBuilder.addAugmentation(TcMatchEntry.class, tcBuilder.build());
        return matchEntriesBuilder.build();
    }

    private static MatchEntries toOfMplsBos(final Short mplsBos) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntriesBuilder.setHasMask(false);
        matchEntriesBuilder.setOxmMatchField(MplsBos.class);
        BosMatchEntryBuilder bosBuilder = new BosMatchEntryBuilder();
        if (mplsBos != 0) {
            bosBuilder.setBos(true);
        } else {
            bosBuilder.setBos(false);
        }
        matchEntriesBuilder.addAugmentation(BosMatchEntry.class, bosBuilder.build());
        return matchEntriesBuilder.build();
    }

    private static MatchEntries toOfMplsLabel(final Long mplsLabel) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntriesBuilder.setHasMask(false);
        matchEntriesBuilder.setOxmMatchField(MplsLabel.class);
        MplsLabelMatchEntryBuilder mplsLabelBuilder = new MplsLabelMatchEntryBuilder();
        mplsLabelBuilder.setMplsLabel(mplsLabel);
        matchEntriesBuilder.addAugmentation(MplsLabelMatchEntry.class, mplsLabelBuilder.build());
        return matchEntriesBuilder.build();
    }

    private static MatchEntries toOfIpv6ExtHeader(final Ipv6ExtHeader ipv6ExtHeader) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        boolean hasmask = false;
        matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntriesBuilder.setOxmMatchField(Ipv6Exthdr.class);
        PseudoFieldMatchEntryBuilder pseudoBuilder = new PseudoFieldMatchEntryBuilder();
        Integer bitmap = ipv6ExtHeader.getIpv6Exthdr();
        final Boolean NONEXT = ((bitmap) & (1 << 0)) != 0;
        final Boolean ESP = ((bitmap) & (1 << 1)) != 0;
        final Boolean AUTH = ((bitmap) & (1 << 2)) != 0;
        final Boolean DEST = ((bitmap) & (1 << 3)) != 0;
        final Boolean FRAG = ((bitmap) & (1 << 4)) != 0;
        final Boolean ROUTER = ((bitmap) & (1 << 5)) != 0;
        final Boolean HOP = ((bitmap) & (1 << 6)) != 0;
        final Boolean UNREP = ((bitmap) & (1 << 7)) != 0;
        final Boolean UNSEQ = ((bitmap) & (1 << 8)) != 0;
        pseudoBuilder.setPseudoField(new Ipv6ExthdrFlags(AUTH, DEST, ESP, FRAG, HOP, NONEXT, ROUTER, UNREP, UNSEQ));
        matchEntriesBuilder.addAugmentation(PseudoFieldMatchEntry.class, pseudoBuilder.build());
        if (ipv6ExtHeader.getIpv6ExthdrMask() != null) {
            hasmask = true;
            addMaskAugmentation(matchEntriesBuilder, ByteUtil.unsignedShortToBytes(ipv6ExtHeader.getIpv6ExthdrMask()));
        }
        matchEntriesBuilder.setHasMask(hasmask);
        return matchEntriesBuilder.build();
    }

    private static MatchEntries toOfIpv6FlowLabel(final Ipv6Label ipv6Label) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        boolean hasmask = false;
        matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntriesBuilder.setOxmMatchField(Ipv6Flabel.class);
        Ipv6FlabelMatchEntryBuilder ipv6FlabelBuilder = new Ipv6FlabelMatchEntryBuilder();
        ipv6FlabelBuilder.setIpv6Flabel(ipv6Label.getIpv6Flabel());
        matchEntriesBuilder.addAugmentation(Ipv6FlabelMatchEntry.class, ipv6FlabelBuilder.build());
        if (ipv6Label.getFlabelMask() != null) {
            hasmask = true;
            addMaskAugmentation(matchEntriesBuilder, ByteUtil.unsignedIntToBytes(ipv6Label.getFlabelMask().getValue()));
        }
        matchEntriesBuilder.setHasMask(hasmask);
        return matchEntriesBuilder.build();
    }

    private static MatchEntries toOfPort(final Class<? extends MatchField> field, final Long portNumber) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntriesBuilder.setHasMask(false);
        matchEntriesBuilder.setOxmMatchField(field);
        PortNumberMatchEntryBuilder port = new PortNumberMatchEntryBuilder();
        port.setPortNumber(new PortNumber(portNumber));
        matchEntriesBuilder.addAugmentation(PortNumberMatchEntry.class, port.build());
        return matchEntriesBuilder.build();
    }

    private static MatchEntries toOfMetadata(final Class<? extends MatchField> field, final BigInteger metadata,
            final BigInteger metadataMask) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        boolean hasmask = false;
        matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntriesBuilder.setOxmMatchField(field);
        addMetadataAugmentation(matchEntriesBuilder, metadata);
        if (metadataMask != null) {
            hasmask = true;
            addMaskAugmentation(matchEntriesBuilder,
                    ByteUtil.convertBigIntegerToNBytes(metadataMask, OFConstants.SIZE_OF_LONG_IN_BYTES));
        }
        matchEntriesBuilder.setHasMask(hasmask);
        return matchEntriesBuilder.build();
    }

    public static MatchEntries toOfMacAddress(final Class<? extends MatchField> field,
            final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress macAddress,
            final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress mask) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        boolean hasmask = false;
        matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntriesBuilder.setOxmMatchField(field);
        addMacAddressAugmentation(matchEntriesBuilder, macAddress);
        if (mask != null) {
            hasmask = true;
            addMaskAugmentation(matchEntriesBuilder, ByteBufUtils.macAddressToBytes(mask.getValue()));
        }
        matchEntriesBuilder.setHasMask(hasmask);
        return matchEntriesBuilder.build();
    }

    private static MatchEntries toOfEthernetType(final EthernetType ethernetType) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntriesBuilder.setHasMask(false);
        matchEntriesBuilder.setOxmMatchField(EthType.class);
        EthTypeMatchEntryBuilder ethertypeBuilder = new EthTypeMatchEntryBuilder();
        ethertypeBuilder.setEthType(new EtherType(ethernetType.getType().getValue().intValue()));
        matchEntriesBuilder.addAugmentation(EthTypeMatchEntry.class, ethertypeBuilder.build());
        return matchEntriesBuilder.build();
    }

    private static MatchEntries toOfLayer3Port(final Class<? extends MatchField> field,
            final org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber portNumber) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntriesBuilder.setHasMask(false);
        matchEntriesBuilder.setOxmMatchField(field);
        PortMatchEntryBuilder portBuilder = new PortMatchEntryBuilder();
        portBuilder.setPort(portNumber);
        matchEntriesBuilder.addAugmentation(PortMatchEntry.class, portBuilder.build());
        return matchEntriesBuilder.build();
    }

    private static MatchEntries toOfIcmpv4Type(final Short icmpv4Type) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntriesBuilder.setHasMask(false);
        matchEntriesBuilder.setOxmMatchField(Icmpv4Type.class);
        Icmpv4TypeMatchEntryBuilder icmpv4TypeBuilder = new Icmpv4TypeMatchEntryBuilder();
        icmpv4TypeBuilder.setIcmpv4Type(icmpv4Type);
        matchEntriesBuilder.addAugmentation(Icmpv4TypeMatchEntry.class, icmpv4TypeBuilder.build());
        return matchEntriesBuilder.build();
    }

    private static MatchEntries toOfIcmpv4Code(final Short icmpv4Code) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntriesBuilder.setHasMask(false);
        matchEntriesBuilder.setOxmMatchField(Icmpv4Code.class);
        Icmpv4CodeMatchEntryBuilder icmpv4CodeBuilder = new Icmpv4CodeMatchEntryBuilder();
        icmpv4CodeBuilder.setIcmpv4Code(icmpv4Code);
        matchEntriesBuilder.addAugmentation(Icmpv4CodeMatchEntry.class, icmpv4CodeBuilder.build());
        return matchEntriesBuilder.build();
    }

    private static MatchEntries toOfIcmpv6Type(final Short icmpv6Type) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntriesBuilder.setHasMask(false);
        matchEntriesBuilder.setOxmMatchField(Icmpv6Type.class);
        Icmpv6TypeMatchEntryBuilder icmpv6TypeBuilder = new Icmpv6TypeMatchEntryBuilder();
        icmpv6TypeBuilder.setIcmpv6Type(icmpv6Type);
        matchEntriesBuilder.addAugmentation(Icmpv6TypeMatchEntry.class, icmpv6TypeBuilder.build());
        return matchEntriesBuilder.build();
    }

    private static MatchEntries toOfIcmpv6Code(final Short icmpv6Code) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntriesBuilder.setHasMask(false);
        matchEntriesBuilder.setOxmMatchField(Icmpv6Code.class);
        Icmpv6CodeMatchEntryBuilder icmpv6CodeBuilder = new Icmpv6CodeMatchEntryBuilder();
        icmpv6CodeBuilder.setIcmpv6Code(icmpv6Code);
        matchEntriesBuilder.addAugmentation(Icmpv6CodeMatchEntry.class, icmpv6CodeBuilder.build());
        return matchEntriesBuilder.build();
    }

    public static MatchEntries toOfIpv4Prefix(final Class<? extends MatchField> field, final Ipv4Prefix ipv4Prefix) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntriesBuilder.setOxmMatchField(field);
        boolean hasMask = addIpv4PrefixAugmentation(matchEntriesBuilder, ipv4Prefix);
        matchEntriesBuilder.setHasMask(hasMask);
        return matchEntriesBuilder.build();
    }

    private static MatchEntries toOfIpv6Prefix(final Class<? extends MatchField> field, final Ipv6Prefix ipv6Prefix) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntriesBuilder.setOxmMatchField(field);
        boolean hasmask = addIpv6PrefixAugmentation(matchEntriesBuilder, ipv6Prefix);
        matchEntriesBuilder.setHasMask(hasmask);
        return matchEntriesBuilder.build();
    }

    public static MatchEntries toOfIpDscp(final Dscp ipDscp) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntriesBuilder.setHasMask(false);
        matchEntriesBuilder.setOxmMatchField(IpDscp.class);
        DscpMatchEntryBuilder dscpBuilder = new DscpMatchEntryBuilder();
        dscpBuilder.setDscp(ipDscp);
        matchEntriesBuilder.addAugmentation(DscpMatchEntry.class, dscpBuilder.build());
        return matchEntriesBuilder.build();
    }

    public static MatchEntries toOfVlanPcp(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp vlanPcp) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntriesBuilder.setHasMask(false);
        matchEntriesBuilder.setOxmMatchField(VlanPcp.class);
        VlanPcpMatchEntryBuilder vlanPcpBuilder = new VlanPcpMatchEntryBuilder();
        vlanPcpBuilder.setVlanPcp(vlanPcp.getValue());
        matchEntriesBuilder.addAugmentation(VlanPcpMatchEntry.class, vlanPcpBuilder.build());
        return matchEntriesBuilder.build();
    }

    private static MatchEntries toOfVlanVid(final VlanId vlanId) {
        // TODO: verify
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        boolean hasmask = false;
        boolean setCfiBit = false;
        Integer vidEntryValue = 0;
        matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntriesBuilder.setOxmMatchField(VlanVid.class);
        VlanVidMatchEntryBuilder vlanVidBuilder = new VlanVidMatchEntryBuilder();
        if (Boolean.TRUE.equals(vlanId.isVlanIdPresent())) {
            setCfiBit = true;
            if (vlanId.getVlanId() != null) {
                vidEntryValue = vlanId.getVlanId().getValue();
            }
            hasmask = (vidEntryValue == 0);
        }
        vlanVidBuilder.setCfiBit(setCfiBit);
        vlanVidBuilder.setVlanVid(vidEntryValue);
        matchEntriesBuilder.addAugmentation(VlanVidMatchEntry.class, vlanVidBuilder.build());
        if (hasmask) {
            addMaskAugmentation(matchEntriesBuilder, VLAN_VID_MASK);
        }
        matchEntriesBuilder.setHasMask(hasmask);
        return matchEntriesBuilder.build();
    }

    private static MatchEntries toOfIpProto(final Short ipProtocol) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntriesBuilder.setHasMask(false);
        matchEntriesBuilder.setOxmMatchField(IpProto.class);
        ProtocolNumberMatchEntryBuilder protoNumberBuilder = new ProtocolNumberMatchEntryBuilder();
        protoNumberBuilder.setProtocolNumber(ipProtocol);
        matchEntriesBuilder.addAugmentation(ProtocolNumberMatchEntry.class, protoNumberBuilder.build());
        return matchEntriesBuilder.build();
    }

    private static MatchEntries toOfIpEcn(final Short ipEcn) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntriesBuilder.setHasMask(false);
        matchEntriesBuilder.setOxmMatchField(IpEcn.class);
        EcnMatchEntryBuilder ecnBuilder = new EcnMatchEntryBuilder();
        ecnBuilder.setEcn(ipEcn);
        matchEntriesBuilder.addAugmentation(EcnMatchEntry.class, ecnBuilder.build());
        return matchEntriesBuilder.build();
    }

    private static MatchEntries toOfArpOpCode(final Integer arpOp) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntriesBuilder.setHasMask(false);
        matchEntriesBuilder.setOxmMatchField(ArpOp.class);
        OpCodeMatchEntryBuilder opcodeBuilder = new OpCodeMatchEntryBuilder();
        opcodeBuilder.setOpCode(arpOp);
        matchEntriesBuilder.addAugmentation(OpCodeMatchEntry.class, opcodeBuilder.build());
        return matchEntriesBuilder.build();
    }

    private static MatchEntries toOfIpv6Address(final Ipv6Address address) {
        MatchEntriesBuilder matchEntriesBuilder = new MatchEntriesBuilder();
        matchEntriesBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntriesBuilder.setHasMask(false);
        matchEntriesBuilder.setOxmMatchField(Ipv6NdTarget.class);
        Ipv6AddressMatchEntryBuilder ipv6AddressBuilder = new Ipv6AddressMatchEntryBuilder();
        ipv6AddressBuilder.setIpv6Address(address);
        matchEntriesBuilder.addAugmentation(Ipv6AddressMatchEntry.class, ipv6AddressBuilder.build());
        return matchEntriesBuilder.build();
    }

    private static void addMaskAugmentation(final MatchEntriesBuilder builder, final byte[] mask) {
        MaskMatchEntryBuilder maskBuilder = new MaskMatchEntryBuilder();
        maskBuilder.setMask(mask);
        builder.addAugmentation(MaskMatchEntry.class, maskBuilder.build());
    }

    private static boolean addIpv6PrefixAugmentation(final MatchEntriesBuilder builder, final Ipv6Prefix address) {
        boolean hasMask = false;
        Iterator<String> addressParts = PREFIX_SPLITTER.split(address.getValue()).iterator();
        Ipv6Address ipv6Address = new Ipv6Address(addressParts.next());

        Integer prefix = null;
        if (addressParts.hasNext()) {
            prefix = Integer.parseInt(addressParts.next());
        }

        Ipv6AddressMatchEntryBuilder ipv6AddressBuilder = new Ipv6AddressMatchEntryBuilder();
        ipv6AddressBuilder.setIpv6Address(ipv6Address);
        builder.addAugmentation(Ipv6AddressMatchEntry.class, ipv6AddressBuilder.build());
        if (prefix != null) {
            hasMask = true;
            addMaskAugmentation(builder, convertIpv6PrefixToByteArray(prefix));
        }
        return hasMask;
    }

    private static byte[] convertIpv6PrefixToByteArray(final int prefix) {
        // TODO: Temporary fix. Has performance impacts.
        byte[] mask = new byte[16];
        int oneCount = prefix;
        for (int count = 0; count < 16; count++) {
            int byteBits = 0;
            if (oneCount >= 8) {
                byteBits = 8;
                oneCount = oneCount - 8;
            } else {
                byteBits = oneCount;
                oneCount = 0;
            }

            mask[count] = (byte) (256 - Math.pow(2, 8 - byteBits));
        }
        return mask;
    }

    private static void addMetadataAugmentation(final MatchEntriesBuilder builder, final BigInteger metadata) {
        MetadataMatchEntryBuilder metadataMatchEntry = new MetadataMatchEntryBuilder();
        metadataMatchEntry.setMetadata(ByteUtil.convertBigIntegerToNBytes(metadata, OFConstants.SIZE_OF_LONG_IN_BYTES));
        builder.addAugmentation(MetadataMatchEntry.class, metadataMatchEntry.build());
    }

    /**
     * @return true if Ipv4Prefix contains prefix (and it is used in mask),
     *         false otherwise
     */
    private static boolean addIpv4PrefixAugmentation(final MatchEntriesBuilder builder, final Ipv4Prefix address) {
        boolean hasMask = false;
        Iterator<String> addressParts = PREFIX_SPLITTER.split(address.getValue()).iterator();
        Ipv4Address ipv4Address = new Ipv4Address(addressParts.next());

        final int prefix;
        if (addressParts.hasNext()) {
            prefix = Integer.parseInt(addressParts.next());
        } else {
            prefix = 0;
        }

        Ipv4AddressMatchEntryBuilder ipv4AddressBuilder = new Ipv4AddressMatchEntryBuilder();
        ipv4AddressBuilder.setIpv4Address(ipv4Address);
        builder.addAugmentation(Ipv4AddressMatchEntry.class, ipv4AddressBuilder.build());
        if (prefix != 0) {
            int mask = 0xffffffff << (32 - prefix);
            byte[] maskBytes = new byte[] { (byte) (mask >>> 24), (byte) (mask >>> 16), (byte) (mask >>> 8),
                    (byte) mask };
            addMaskAugmentation(builder, maskBytes);
            hasMask = true;
        }
        return hasMask;
    }

    private static void addMacAddressAugmentation(final MatchEntriesBuilder builder, final MacAddress address) {
        MacAddressMatchEntryBuilder macAddress = new MacAddressMatchEntryBuilder();
        macAddress.setMacAddress(address);
        builder.addAugmentation(MacAddressMatchEntry.class, macAddress.build());
    }

    /**
     * Method converts OF SetField action to SAL SetFiled action.
     *
     * @param action
     * @param ofVersion current ofp version
     * @return
     */
    public static SetField fromOFSetFieldToSALSetFieldAction(
            final Action action, OpenflowVersion ofVersion) {
        logger.debug("Converting OF SetField action to SAL SetField action");
        SetFieldBuilder setField = new SetFieldBuilder();
        OxmFieldsAction oxmFields = action.getAugmentation(OxmFieldsAction.class);
        MatchBuilder match = OfMatchToSALMatchConvertor(oxmFields.getMatchEntries(), null, ofVersion);
        setField.fieldsFrom(match.build());
        return setField.build();
    }
}
