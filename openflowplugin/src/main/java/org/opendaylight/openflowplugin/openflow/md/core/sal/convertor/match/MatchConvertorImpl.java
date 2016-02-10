/**
 * Copyright (c) 2013, 2015 Ericsson. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match;

import static org.opendaylight.openflowjava.util.ByteBufUtils.macAddressToString;

import com.google.common.base.Optional;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;

import com.google.common.primitives.UnsignedBytes;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.ConverterExtensionKey;
import org.opendaylight.openflowplugin.extension.api.ConvertorToOFJava;
import org.opendaylight.openflowplugin.openflow.md.core.extension.ExtensionResolvers;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.IpConversionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.session.OFSessionUtil;
import org.opendaylight.openflowplugin.openflow.md.util.ActionUtil;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Dscp;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6FlowLabel;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DottedQuad;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.set.field._case.SetFieldBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.TunnelBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatch;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.VlanMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.*;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.SetFieldCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.action.grouping.action.choice.set.field._case.SetFieldAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.action.rev150203.actions.grouping.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.Ipv6ExthdrFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.PortNumber;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanPcp;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.VlanVid;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpOpCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpOpCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpShaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpShaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpSpaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpSpaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpThaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpThaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpTpaCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ArpTpaCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthTypeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.EthTypeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv4CodeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv4CodeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv4TypeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv4TypeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv6CodeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv6CodeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv6TypeCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Icmpv6TypeCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPhyPortCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPhyPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPortCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.InPortCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpDscpCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpDscpCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpEcnCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpEcnCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpProtoCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.IpProtoCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4DstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4DstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4SrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv4SrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6DstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6DstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6ExthdrCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6ExthdrCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6FlabelCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6FlabelCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6NdSllCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6NdSllCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6NdTargetCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6NdTargetCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6NdTllCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6NdTllCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6SrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.Ipv6SrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MetadataCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MetadataCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MplsBosCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MplsBosCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MplsLabelCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MplsLabelCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MplsTcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.MplsTcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.PbbIsidCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.PbbIsidCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.SctpDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.SctpDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.SctpSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.SctpSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TcpDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TcpDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TcpSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TcpSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TunnelIdCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.TunnelIdCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.UdpDstCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.UdpDstCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.UdpSrcCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.UdpSrcCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanPcpCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanPcpCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.VlanVidCase;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.v10.grouping.MatchV10;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.ExtensionKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralExtensionListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.list.grouping.ExtensionList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelIpv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelIpv4Src;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for converting a MD-SAL Flow into the OF flow mod
 */
public class MatchConvertorImpl implements MatchConvertor<List<MatchEntry>> {
    private static final Logger logger = LoggerFactory.getLogger(MatchConvertorImpl.class);
    private static final byte[] VLAN_VID_MASK = new byte[]{16, 0};
    private static final short PROTO_TCP = 6;
    private static final short PROTO_UDP = 17;
    private static final short PROTO_ICMPV4 = 1;
    private static final String NO_IP = "0.0.0.0/0";

    @Override
    public List<MatchEntry> convert(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match match, final BigInteger datapathid) {
        List<MatchEntry> matchEntryList = new ArrayList<>();
        if (match == null) {
            return matchEntryList;
        }
        if (match.getInPort() != null) {
            //TODO: currently this matchconverter is mapped to OF1.3 in MatchReactorMappingFactory. Will need to revisit during 1.4+
            matchEntryList.add(toOfPort(InPort.class,
                    InventoryDataServiceUtil.portNumberfromNodeConnectorId(OpenflowVersion.OF13, match.getInPort())));
        }

        if (match.getInPhyPort() != null) {
            //TODO: currently this matchconverter is mapped to OF1.3 in MatchReactorMappingFactory. Will need to revisit during 1.4+
            matchEntryList.add(toOfPhyPort(InPhyPort.class,
                    InventoryDataServiceUtil.portNumberfromNodeConnectorId(OpenflowVersion.OF13, match.getInPhyPort())));
        }

        org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Metadata metadata = match
                .getMetadata();
        if (metadata != null) {
            matchEntryList.add(toOfMetadata(Metadata.class, metadata.getMetadata(), metadata.getMetadataMask()));
        }

        ethernetMatch(matchEntryList, match.getEthernetMatch());
        vlanMatch(matchEntryList, match.getVlanMatch());
        ipMatch(matchEntryList, match.getIpMatch());
        layer4Match(matchEntryList, match.getLayer4Match());
        icmpv4Match(matchEntryList, match.getIcmpv4Match());
        icmpv6Match(matchEntryList, match.getIcmpv6Match());
        layer3Match(matchEntryList, match.getLayer3Match());
        protocolMatchFields(matchEntryList, match.getProtocolMatchFields());

        org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.Tunnel tunnel = match
                .getTunnel();
        if (tunnel != null) {
            MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
            TunnelIdCaseBuilder tunnelIdCaseBuilder = new TunnelIdCaseBuilder();
            TunnelIdBuilder tunnelIdBuilder = new TunnelIdBuilder();
            boolean hasMask = false;
            if (null != tunnel.getTunnelMask()) {
                hasMask = true;
                tunnelIdBuilder.setMask(ByteUtil.convertBigIntegerToNBytes(tunnel.getTunnelMask(), OFConstants.SIZE_OF_LONG_IN_BYTES));
            }
            tunnelIdBuilder.setTunnelId(ByteUtil.convertBigIntegerToNBytes(tunnel.getTunnelId(), OFConstants.SIZE_OF_LONG_IN_BYTES));
            tunnelIdCaseBuilder.setTunnelId(tunnelIdBuilder.build());
            matchEntryBuilder.setMatchEntryValue(tunnelIdCaseBuilder.build());
            matchEntryBuilder.setHasMask(hasMask);
            matchEntryBuilder.setOxmMatchField(TunnelId.class);
            matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
            matchEntryList.add(matchEntryBuilder.build());
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
                ConvertorToOFJava<MatchEntry> convertor =
                        OFSessionUtil.getExtensionConvertorProvider().getConverter(key);
                MatchEntry ofMatch = convertor.convert(extensionItem.getExtension());
                matchEntryList.add(ofMatch);
            }
        }

        return matchEntryList;
    }


    private void protocolMatchFields(List<MatchEntry> matchEntryList,
                                     ProtocolMatchFields protocolMatchFields) {
        if (protocolMatchFields != null) {
            if (protocolMatchFields.getMplsLabel() != null) {
                matchEntryList.add(toOfMplsLabel(protocolMatchFields.getMplsLabel()));
            }

            if (protocolMatchFields.getMplsBos() != null) {
                matchEntryList.add(toOfMplsBos(protocolMatchFields.getMplsBos()));
            }

            if (protocolMatchFields.getMplsTc() != null) {
                matchEntryList.add(toOfMplsTc(protocolMatchFields.getMplsTc()));
            }

            if (protocolMatchFields.getPbb() != null) {
                matchEntryList.add(toOfMplsPbb(protocolMatchFields.getPbb()));
            }
        }
    }


    /**
     * @param matchEntryList
     * @param layer3Match
     */
    private void layer3Match(List<MatchEntry> matchEntryList,
                             Layer3Match layer3Match) {
        if (layer3Match != null) {
            if(layer3Match instanceof Ipv4MatchArbitraryBitMask) {
                Ipv4MatchArbitraryBitMask ipv4MatchArbitraryBitMaskFields = (Ipv4MatchArbitraryBitMask) layer3Match;
                if (ipv4MatchArbitraryBitMaskFields.getIpv4SourceAddressNoMask() != null) {
                    MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                    matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntryBuilder.setOxmMatchField(Ipv4Src.class);

                    Ipv4SrcCaseBuilder ipv4SrcCaseBuilder = new Ipv4SrcCaseBuilder();
                    Ipv4SrcBuilder ipv4SrcBuilder = new Ipv4SrcBuilder();

                    ipv4SrcBuilder.setIpv4Address(ipv4MatchArbitraryBitMaskFields.getIpv4SourceAddressNoMask());
                    DottedQuad sourceArbitrarySubNetMask = ipv4MatchArbitraryBitMaskFields.getIpv4SourceArbitraryBitMask();

                    boolean hasMask = false;
                    if (sourceArbitrarySubNetMask != null) {
                        byte[] maskByteArray = IpConversionUtil.convertArbitraryMaskToByteArray(sourceArbitrarySubNetMask);
                        if (maskByteArray != null) {
                            ipv4SrcBuilder.setMask(maskByteArray);
                            hasMask = true;
                        }
                    }
                    matchEntryBuilder.setHasMask(hasMask);
                    ipv4SrcCaseBuilder.setIpv4Src(ipv4SrcBuilder.build());
                    matchEntryBuilder.setMatchEntryValue(ipv4SrcCaseBuilder.build());
                    matchEntryList.add(matchEntryBuilder.build());
                }
                if (ipv4MatchArbitraryBitMaskFields.getIpv4DestinationAddressNoMask() != null) {
                    MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                    matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntryBuilder.setOxmMatchField(Ipv4Dst.class);

                    Ipv4DstCaseBuilder ipv4DstCaseBuilder = new Ipv4DstCaseBuilder();
                    Ipv4DstBuilder ipv4DstBuilder = new Ipv4DstBuilder();

                    ipv4DstBuilder.setIpv4Address(ipv4MatchArbitraryBitMaskFields.getIpv4DestinationAddressNoMask());
                    DottedQuad destArbitrarySubNetMask = ipv4MatchArbitraryBitMaskFields.getIpv4DestinationArbitraryBitMask();

                    boolean hasMask = false;
                    if (destArbitrarySubNetMask != null) {
                        byte[] maskByteArray = IpConversionUtil.convertArbitraryMaskToByteArray(destArbitrarySubNetMask);
                        if (maskByteArray != null) {
                            ipv4DstBuilder.setMask(maskByteArray);
                            hasMask = true;
                        }
                    }
                    matchEntryBuilder.setHasMask(hasMask);
                    ipv4DstCaseBuilder.setIpv4Dst(ipv4DstBuilder.build());
                    matchEntryBuilder.setMatchEntryValue(ipv4DstCaseBuilder.build());
                    matchEntryList.add(matchEntryBuilder.build());
                }
            }
            if(layer3Match instanceof Ipv4Match){
                Ipv4Match ipv4Match = (Ipv4Match) layer3Match;
                if (ipv4Match.getIpv4Source() != null) {
                    Ipv4Prefix ipv4Prefix = ipv4Match.getIpv4Source();
                    MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                    matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntryBuilder.setOxmMatchField(Ipv4Src.class);

                    Ipv4SrcCaseBuilder ipv4SrcCaseBuilder = new Ipv4SrcCaseBuilder();
                    Ipv4SrcBuilder ipv4SrcBuilder = new Ipv4SrcBuilder();

                    Iterator<String> addressParts = IpConversionUtil.splitToParts(ipv4Prefix);
                    Ipv4Address ipv4Address = new Ipv4Address(addressParts.next());
                    ipv4SrcBuilder.setIpv4Address(ipv4Address);
                    boolean hasMask = false;
                    byte[] mask = extractIpv4Mask(addressParts);
                    if (null != mask) {
                        ipv4SrcBuilder.setMask(mask);
                        hasMask = true;
                    }
                    matchEntryBuilder.setHasMask(hasMask);
                    ipv4SrcCaseBuilder.setIpv4Src(ipv4SrcBuilder.build());
                    matchEntryBuilder.setMatchEntryValue(ipv4SrcCaseBuilder.build());
                    matchEntryList.add(matchEntryBuilder.build());
                }
                if (ipv4Match.getIpv4Destination() != null) {
                    Ipv4Prefix ipv4Prefix = ipv4Match.getIpv4Destination();
                    MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                    matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntryBuilder.setOxmMatchField(Ipv4Dst.class);

                    Ipv4DstCaseBuilder ipv4DstCaseBuilder = new Ipv4DstCaseBuilder();
                    Ipv4DstBuilder ipv4DstBuilder = new Ipv4DstBuilder();

                    Iterator<String> addressParts = IpConversionUtil.splitToParts(ipv4Prefix);
                    Ipv4Address ipv4Address = new Ipv4Address(addressParts.next());
                    ipv4DstBuilder.setIpv4Address(ipv4Address);
                    boolean hasMask = false;
                    byte[] mask = extractIpv4Mask(addressParts);
                    if (null != mask) {
                        ipv4DstBuilder.setMask(mask);
                        hasMask = true;
                    }
                    matchEntryBuilder.setHasMask(hasMask);
                    ipv4DstCaseBuilder.setIpv4Dst(ipv4DstBuilder.build());
                    matchEntryBuilder.setMatchEntryValue(ipv4DstCaseBuilder.build());
                    matchEntryList.add(matchEntryBuilder.build());
                }
            }
            if (layer3Match instanceof TunnelIpv4Match) {
                TunnelIpv4Match tunnelIpv4Src = (TunnelIpv4Match) layer3Match;
                if (tunnelIpv4Src.getTunnelIpv4Source() != null) {
                    Ipv4Prefix ipv4Prefix = tunnelIpv4Src.getTunnelIpv4Source();
                    MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                    matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntryBuilder.setOxmMatchField(Ipv4Src.class);

                    Ipv4SrcCaseBuilder ipv4SrcCaseBuilder = new Ipv4SrcCaseBuilder();
                    Ipv4SrcBuilder ipv4SrcBuilder = new Ipv4SrcBuilder();

                    Iterator<String> addressParts = IpConversionUtil.splitToParts(ipv4Prefix);
                    Ipv4Address ipv4Address = new Ipv4Address(addressParts.next());
                    ipv4SrcBuilder.setIpv4Address(ipv4Address);
                    boolean hasMask = false;
                    byte[] mask = extractIpv4Mask(addressParts);
                    if (null != mask) {
                        ipv4SrcBuilder.setMask(mask);
                        hasMask = true;
                    }
                    matchEntryBuilder.setHasMask(hasMask);
                    ipv4SrcCaseBuilder.setIpv4Src(ipv4SrcBuilder.build());
                    matchEntryBuilder.setMatchEntryValue(ipv4SrcCaseBuilder.build());
                    matchEntryList.add(matchEntryBuilder.build());
                }
                if (tunnelIpv4Src.getTunnelIpv4Destination() != null) {
                    Ipv4Prefix ipv4Prefix = tunnelIpv4Src.getTunnelIpv4Destination();
                    MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                    matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntryBuilder.setOxmMatchField(Ipv4Dst.class);

                    Ipv4DstCaseBuilder ipv4DstCaseBuilder = new Ipv4DstCaseBuilder();
                    Ipv4DstBuilder ipv4DstBuilder = new Ipv4DstBuilder();

                    Iterator<String> addressParts = IpConversionUtil.splitToParts(ipv4Prefix);
                    Ipv4Address ipv4Address = new Ipv4Address(addressParts.next());
                    ipv4DstBuilder.setIpv4Address(ipv4Address);
                    boolean hasMask = false;
                    byte[] mask = extractIpv4Mask(addressParts);
                    if (null != mask) {
                        ipv4DstBuilder.setMask(mask);
                        hasMask = true;
                    }
                    matchEntryBuilder.setHasMask(hasMask);
                    ipv4DstCaseBuilder.setIpv4Dst(ipv4DstBuilder.build());
                    matchEntryBuilder.setMatchEntryValue(ipv4DstCaseBuilder.build());
                    matchEntryList.add(matchEntryBuilder.build());
                }
            } else if (layer3Match instanceof ArpMatch) {
                ArpMatch arpMatch = (ArpMatch) layer3Match;
                if (arpMatch.getArpOp() != null) {
                    matchEntryList.add(toOfArpOpCode(arpMatch.getArpOp()));
                }

                if (arpMatch.getArpSourceTransportAddress() != null) {
                    Ipv4Prefix ipv4Prefix = arpMatch.getArpSourceTransportAddress();
                    MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                    matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntryBuilder.setOxmMatchField(ArpSpa.class);

                    ArpSpaCaseBuilder arpSpaCaseBuilder = new ArpSpaCaseBuilder();
                    ArpSpaBuilder arpSpaBuilder = new ArpSpaBuilder();

                    Iterator<String> addressParts = IpConversionUtil.splitToParts(ipv4Prefix);
                    Ipv4Address ipv4Address = new Ipv4Address(addressParts.next());
                    arpSpaBuilder.setIpv4Address(ipv4Address);
                    boolean hasMask = false;
                    byte[] mask = extractIpv4Mask(addressParts);
                    if (null != mask) {
                        arpSpaBuilder.setMask(mask);
                        hasMask = true;
                    }
                    matchEntryBuilder.setHasMask(hasMask);
                    arpSpaCaseBuilder.setArpSpa(arpSpaBuilder.build());
                    matchEntryBuilder.setMatchEntryValue(arpSpaCaseBuilder.build());
                    matchEntryList.add(matchEntryBuilder.build());
                }

                if (arpMatch.getArpTargetTransportAddress() != null) {
                    Ipv4Prefix ipv4Prefix = arpMatch.getArpTargetTransportAddress();
                    MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                    matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntryBuilder.setOxmMatchField(ArpTpa.class);

                    ArpTpaCaseBuilder arpTpaCaseBuilder = new ArpTpaCaseBuilder();
                    ArpTpaBuilder arpTpaBuilder = new ArpTpaBuilder();

                    Iterator<String> addressParts = IpConversionUtil.splitToParts(ipv4Prefix);
                    Ipv4Address ipv4Address = new Ipv4Address(addressParts.next());
                    arpTpaBuilder.setIpv4Address(ipv4Address);
                    boolean hasMask = false;
                    byte[] mask = extractIpv4Mask(addressParts);
                    if (null != mask) {
                        arpTpaBuilder.setMask(mask);
                        hasMask = true;
                    }
                    matchEntryBuilder.setHasMask(hasMask);
                    arpTpaCaseBuilder.setArpTpa(arpTpaBuilder.build());
                    matchEntryBuilder.setMatchEntryValue(arpTpaCaseBuilder.build());
                    matchEntryList.add(matchEntryBuilder.build());
                }

                ArpSourceHardwareAddress arpSourceHardwareAddress = arpMatch.getArpSourceHardwareAddress();
                if (arpSourceHardwareAddress != null) {
                    MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                    matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntryBuilder.setOxmMatchField(ArpSha.class);

                    ArpShaCaseBuilder arpShaCaseBuilder = new ArpShaCaseBuilder();
                    ArpShaBuilder arpShaBuilder = new ArpShaBuilder();
                    arpShaBuilder.setMacAddress(arpSourceHardwareAddress.getAddress());
                    boolean hasMask = false;
                    if (null != arpSourceHardwareAddress.getMask()) {
                        arpShaBuilder.setMask(ByteBufUtils.macAddressToBytes(arpSourceHardwareAddress.getMask().getValue()));
                        hasMask = true;
                    }
                    arpShaCaseBuilder.setArpSha(arpShaBuilder.build());
                    matchEntryBuilder.setMatchEntryValue(arpShaCaseBuilder.build());
                    matchEntryBuilder.setHasMask(hasMask);
                    matchEntryList.add(matchEntryBuilder.build());
                }

                ArpTargetHardwareAddress arpTargetHardwareAddress = arpMatch.getArpTargetHardwareAddress();
                if (arpTargetHardwareAddress != null) {
                    MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                    matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntryBuilder.setOxmMatchField(ArpTha.class);

                    ArpThaCaseBuilder arpThaCaseBuilder = new ArpThaCaseBuilder();
                    ArpThaBuilder arpThaBuilder = new ArpThaBuilder();
                    arpThaBuilder.setMacAddress(arpTargetHardwareAddress.getAddress());
                    boolean hasMask = false;
                    if (null != arpTargetHardwareAddress.getMask()) {
                        arpThaBuilder.setMask(ByteBufUtils.macAddressToBytes(arpTargetHardwareAddress.getMask().getValue()));
                        hasMask = true;
                    }
                    arpThaCaseBuilder.setArpTha(arpThaBuilder.build());
                    matchEntryBuilder.setMatchEntryValue(arpThaCaseBuilder.build());
                    matchEntryBuilder.setHasMask(hasMask);
                    matchEntryList.add(matchEntryBuilder.build());
                }
            } else if (layer3Match instanceof Ipv6Match) {
                Ipv6Match ipv6Match = (Ipv6Match) layer3Match;
                if (ipv6Match.getIpv6Source() != null) {
                    Ipv6Prefix ipv6Prefix = ipv6Match.getIpv6Source();
                    MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                    matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntryBuilder.setOxmMatchField(Ipv6Src.class);

                    Ipv6SrcCaseBuilder ipv6SrcCaseBuilder = new Ipv6SrcCaseBuilder();
                    Ipv6SrcBuilder ipv6SrcBuilder = new Ipv6SrcBuilder();
                    final Integer prefix = IpConversionUtil.extractIpv6Prefix(ipv6Prefix);
                    boolean hasMask = false;
                    if (null != prefix) {
                        ipv6SrcBuilder.setMask(IpConversionUtil.convertIpv6PrefixToByteArray(prefix));
                        hasMask = true;
                    }
                    ipv6SrcBuilder.setIpv6Address(IpConversionUtil.extractIpv6Address(ipv6Prefix));
                    ipv6SrcCaseBuilder.setIpv6Src(ipv6SrcBuilder.build());
                    matchEntryBuilder.setHasMask(hasMask);
                    matchEntryBuilder.setMatchEntryValue(ipv6SrcCaseBuilder.build());
                    matchEntryList.add(matchEntryBuilder.build());
                }

                if (ipv6Match.getIpv6Destination() != null) {
                    Ipv6Prefix ipv6Prefix = ipv6Match.getIpv6Destination();
                    MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                    matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntryBuilder.setOxmMatchField(Ipv6Dst.class);

                    Ipv6DstCaseBuilder ipv6DstCaseBuilder = new Ipv6DstCaseBuilder();
                    Ipv6DstBuilder ipv6DstBuilder = new Ipv6DstBuilder();
                    final Integer prefix = IpConversionUtil.extractIpv6Prefix(ipv6Prefix);
                    boolean hasMask = false;
                    if (null != prefix) {
                        ipv6DstBuilder.setMask(IpConversionUtil.convertIpv6PrefixToByteArray(prefix));
                        hasMask = true;
                    }
                    ipv6DstBuilder.setIpv6Address(IpConversionUtil.extractIpv6Address(ipv6Prefix));
                    ipv6DstCaseBuilder.setIpv6Dst(ipv6DstBuilder.build());
                    matchEntryBuilder.setHasMask(hasMask);
                    matchEntryBuilder.setMatchEntryValue(ipv6DstCaseBuilder.build());
                    matchEntryList.add(matchEntryBuilder.build());
                }

                if (ipv6Match.getIpv6Label() != null) {
                    matchEntryList.add(toOfIpv6FlowLabel(ipv6Match.getIpv6Label()));
                }

                if (ipv6Match.getIpv6NdTarget() != null) {
                    matchEntryList.add(toOfIpv6NdTargetAddress(ipv6Match.getIpv6NdTarget()));
                }

                if (ipv6Match.getIpv6NdSll() != null) {
                    MacAddress ipv6NdSll = ipv6Match.getIpv6NdSll();
                    MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                    matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntryBuilder.setOxmMatchField(Ipv6NdSll.class);

                    Ipv6NdSllCaseBuilder ipv6NdSllCaseBuilder = new Ipv6NdSllCaseBuilder();
                    Ipv6NdSllBuilder ipv6NdSllBuilder = new Ipv6NdSllBuilder();
                    ipv6NdSllBuilder.setMacAddress(ipv6NdSll);
                    ipv6NdSllCaseBuilder.setIpv6NdSll(ipv6NdSllBuilder.build());
                    matchEntryBuilder.setMatchEntryValue(ipv6NdSllCaseBuilder.build());
                    matchEntryBuilder.setHasMask(false);
                    matchEntryList.add(matchEntryBuilder.build());
                }

                if (ipv6Match.getIpv6NdTll() != null) {
                    MacAddress ipv6NdSll = ipv6Match.getIpv6NdTll();
                    MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                    matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntryBuilder.setOxmMatchField(Ipv6NdTll.class);

                    Ipv6NdTllCaseBuilder ipv6NdTllCaseBuilder = new Ipv6NdTllCaseBuilder();
                    Ipv6NdTllBuilder ipv6NdTllBuilder = new Ipv6NdTllBuilder();
                    ipv6NdTllBuilder.setMacAddress(ipv6NdSll);
                    ipv6NdTllCaseBuilder.setIpv6NdTll(ipv6NdTllBuilder.build());
                    matchEntryBuilder.setMatchEntryValue(ipv6NdTllCaseBuilder.build());
                    matchEntryBuilder.setHasMask(false);
                    matchEntryList.add(matchEntryBuilder.build());

                }

                if (ipv6Match.getIpv6ExtHeader() != null) {
                    matchEntryList.add(toOfIpv6ExtHeader(ipv6Match.getIpv6ExtHeader()));
                }
            }
        }
    }

    private void icmpv6Match(List<MatchEntry> matchEntryList,
                             Icmpv6Match icmpv6Match) {
        if (icmpv6Match != null) {
            if (icmpv6Match.getIcmpv6Type() != null) {
                matchEntryList.add(toOfIcmpv6Type(icmpv6Match.getIcmpv6Type()));
            }

            if (icmpv6Match.getIcmpv6Code() != null) {
                matchEntryList.add(toOfIcmpv6Code(icmpv6Match.getIcmpv6Code()));
            }
        }
    }


    private void icmpv4Match(List<MatchEntry> matchEntryList,
                             Icmpv4Match icmpv4Match) {
        if (icmpv4Match != null) {
            if (icmpv4Match.getIcmpv4Type() != null) {
                matchEntryList.add(toOfIcmpv4Type(icmpv4Match.getIcmpv4Type()));
            }

            if (icmpv4Match.getIcmpv4Code() != null) {
                matchEntryList.add(toOfIcmpv4Code(icmpv4Match.getIcmpv4Code()));
            }
        }
    }


    private void layer4Match(List<MatchEntry> matchEntryList,
                             Layer4Match layer4Match) {
        if (layer4Match != null) {
            if (layer4Match instanceof TcpMatch) {
                TcpMatch tcpMatch = (TcpMatch) layer4Match;

                if (tcpMatch.getTcpSourcePort() != null) {
                    MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                    matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntryBuilder.setOxmMatchField(TcpSrc.class);

                    TcpSrcCaseBuilder tcpSrcCaseBuilder = new TcpSrcCaseBuilder();
                    TcpSrcBuilder tcpSrcBuilder = new TcpSrcBuilder();
                    tcpSrcBuilder.setPort(tcpMatch.getTcpSourcePort());
                    tcpSrcCaseBuilder.setTcpSrc(tcpSrcBuilder.build());

                    matchEntryBuilder.setMatchEntryValue(tcpSrcCaseBuilder.build());
                    matchEntryBuilder.setHasMask(false);
                    matchEntryList.add(matchEntryBuilder.build());
                }
                if (tcpMatch.getTcpDestinationPort() != null) {
                    MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                    matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntryBuilder.setOxmMatchField(TcpDst.class);

                    TcpDstCaseBuilder tcpDstCaseBuilder = new TcpDstCaseBuilder();
                    TcpDstBuilder tcpDstBuilder = new TcpDstBuilder();
                    tcpDstBuilder.setPort(tcpMatch.getTcpDestinationPort());
                    tcpDstCaseBuilder.setTcpDst(tcpDstBuilder.build());
                    matchEntryBuilder.setMatchEntryValue(tcpDstCaseBuilder.build());
                    matchEntryBuilder.setHasMask(false);
                    matchEntryList.add(matchEntryBuilder.build());
                }
            } else if (layer4Match instanceof UdpMatch) {
                UdpMatch udpMatch = (UdpMatch) layer4Match;
                if (udpMatch.getUdpSourcePort() != null) {
                    MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                    matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntryBuilder.setOxmMatchField(UdpSrc.class);

                    UdpSrcCaseBuilder udpSrcCaseBuilder = new UdpSrcCaseBuilder();
                    UdpSrcBuilder udpSrcBuilder = new UdpSrcBuilder();
                    boolean hasMask = false;
                    udpSrcBuilder.setPort(udpMatch.getUdpSourcePort());
                    udpSrcCaseBuilder.setUdpSrc(udpSrcBuilder.build());
                    matchEntryBuilder.setMatchEntryValue(udpSrcCaseBuilder.build());
                    matchEntryBuilder.setHasMask(hasMask);
                    matchEntryList.add(matchEntryBuilder.build());
                }

                if (udpMatch.getUdpDestinationPort() != null) {
                    MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                    matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntryBuilder.setOxmMatchField(UdpDst.class);

                    UdpDstCaseBuilder udpDstCaseBuilder = new UdpDstCaseBuilder();
                    UdpDstBuilder udpDstBuilder = new UdpDstBuilder();
                    udpDstBuilder.setPort(udpMatch.getUdpDestinationPort());
                    udpDstCaseBuilder.setUdpDst(udpDstBuilder.build());
                    matchEntryBuilder.setMatchEntryValue(udpDstCaseBuilder.build());
                    matchEntryBuilder.setHasMask(false);
                    matchEntryList.add(matchEntryBuilder.build());
                }
            } else if (layer4Match instanceof SctpMatch) {
                SctpMatch sctpMatch = (SctpMatch) layer4Match;
                if (sctpMatch.getSctpSourcePort() != null) {
                    MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                    matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntryBuilder.setOxmMatchField(SctpSrc.class);

                    SctpSrcCaseBuilder sctpSrcCaseBuilder = new SctpSrcCaseBuilder();
                    SctpSrcBuilder sctpSrcBuilder = new SctpSrcBuilder();
                    sctpSrcBuilder.setPort(sctpMatch.getSctpSourcePort());
                    sctpSrcCaseBuilder.setSctpSrc(sctpSrcBuilder.build());
                    matchEntryBuilder.setMatchEntryValue(sctpSrcCaseBuilder.build());
                    matchEntryBuilder.setHasMask(false);
                    matchEntryList.add(matchEntryBuilder.build());
                }

                if (sctpMatch.getSctpDestinationPort() != null) {
                    MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                    matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                    matchEntryBuilder.setOxmMatchField(SctpDst.class);

                    SctpDstCaseBuilder sctpDstCaseBuilder = new SctpDstCaseBuilder();
                    SctpDstBuilder sctpDstBuilder = new SctpDstBuilder();
                    sctpDstBuilder.setPort(sctpMatch.getSctpDestinationPort());
                    sctpDstCaseBuilder.setSctpDst(sctpDstBuilder.build());
                    matchEntryBuilder.setMatchEntryValue(sctpDstCaseBuilder.build());
                    matchEntryBuilder.setHasMask(false);
                    matchEntryList.add(matchEntryBuilder.build());
                }
            }
        }
    }


    private void ipMatch(List<MatchEntry> matchEntryList, IpMatch ipMatch) {
        if (ipMatch != null) {
            if (ipMatch.getIpDscp() != null) {
                matchEntryList.add(toOfIpDscp(ipMatch.getIpDscp()));
            }

            if (ipMatch.getIpEcn() != null) {
                matchEntryList.add(toOfIpEcn(ipMatch.getIpEcn()));
            }

            if (ipMatch.getIpProtocol() != null) {
                matchEntryList.add(toOfIpProto(ipMatch.getIpProtocol()));
            }

        }
    }


    private void vlanMatch(List<MatchEntry> matchEntryList,
                           VlanMatch vlanMatch) {
        if (vlanMatch != null) {
            if (vlanMatch.getVlanId() != null) {
                VlanId vlanId = vlanMatch.getVlanId();
                MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                matchEntryBuilder.setOxmMatchField(VlanVid.class);

                VlanVidCaseBuilder vlanVidCaseBuilder = new VlanVidCaseBuilder();
                VlanVidBuilder vlanVidBuilder = new VlanVidBuilder();
                boolean setCfiBit = false;
                Integer vidEntryValue = 0;
                boolean hasmask = false;
                if (Boolean.TRUE.equals(vlanId.isVlanIdPresent())) {
                    setCfiBit = true;
                    if (vlanId.getVlanId() != null) {
                        vidEntryValue = vlanId.getVlanId().getValue();
                    }
                    hasmask = (vidEntryValue == 0);
                    if (hasmask) {
                        vlanVidBuilder.setMask(VLAN_VID_MASK);
                    }
                }

                vlanVidBuilder.setCfiBit(setCfiBit);
                vlanVidBuilder.setVlanVid(vidEntryValue);
                vlanVidCaseBuilder.setVlanVid(vlanVidBuilder.build());
                matchEntryBuilder.setMatchEntryValue(vlanVidCaseBuilder.build());
                matchEntryBuilder.setHasMask(hasmask);
                matchEntryList.add(matchEntryBuilder.build());
            }

            if (vlanMatch.getVlanPcp() != null) {
                matchEntryList.add(toOfVlanPcp(vlanMatch.getVlanPcp()));
            }
        }
    }


    private void ethernetMatch(List<MatchEntry> matchEntryList,
                               EthernetMatch ethernetMatch) {
        if (ethernetMatch != null) {
            EthernetDestination ethernetDestination = ethernetMatch.getEthernetDestination();
            if (ethernetDestination != null) {
                MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                matchEntryBuilder.setOxmMatchField(EthDst.class);
                EthDstCaseBuilder ethDstCaseBuilder = new EthDstCaseBuilder();
                EthDstBuilder ethDstBuilder = new EthDstBuilder();
                ethDstBuilder.setMacAddress(ethernetDestination.getAddress());
                boolean hasMask = false;
                if (null != ethernetDestination.getMask()) {
                    ethDstBuilder.setMask(ByteBufUtils.macAddressToBytes(ethernetDestination.getMask().getValue()));
                    hasMask = true;
                }
                ethDstCaseBuilder.setEthDst(ethDstBuilder.build());
                matchEntryBuilder.setMatchEntryValue(ethDstCaseBuilder.build());
                matchEntryBuilder.setHasMask(hasMask);
                matchEntryList.add(matchEntryBuilder.build());
            }

            EthernetSource ethernetSource = ethernetMatch.getEthernetSource();
            if (ethernetSource != null) {
                MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
                matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
                matchEntryBuilder.setOxmMatchField(EthSrc.class);

                EthSrcCaseBuilder ethSrcCaseBuilder = new EthSrcCaseBuilder();
                EthSrcBuilder ethDstBuilder = new EthSrcBuilder();
                ethDstBuilder.setMacAddress(ethernetSource.getAddress());
                boolean hasMask = false;
                if (null != ethernetSource.getMask()) {
                    ethDstBuilder.setMask(ByteBufUtils.macAddressToBytes(ethernetSource.getMask().getValue()));
                    hasMask = true;
                }
                ethSrcCaseBuilder.setEthSrc(ethDstBuilder.build());
                matchEntryBuilder.setMatchEntryValue(ethSrcCaseBuilder.build());
                matchEntryBuilder.setHasMask(hasMask);
                matchEntryList.add(matchEntryBuilder.build());
            }

            if (ethernetMatch.getEthernetType() != null) {
                matchEntryList.add(toOfEthernetType(ethernetMatch.getEthernetType()));
            }
        }
    }


    private static byte[] extractIpv4Mask(final Iterator<String> addressParts) {
        final int prefix;
        if (addressParts.hasNext()) {
            int potentionalPrefix = Integer.parseInt(addressParts.next());
            prefix = potentionalPrefix < 32 ? potentionalPrefix : 0;
        } else {
            prefix = 0;
        }

        if (prefix != 0) {
            int mask = 0xffffffff << (32 - prefix);
            byte[] maskBytes = new byte[]{(byte) (mask >>> 24), (byte) (mask >>> 16), (byte) (mask >>> 8),
                    (byte) mask};
            return maskBytes;
        }
        return null;
    }

    /**
     * Method convert Openflow 1.0 specific flow match to MD-SAL format flow
     * match
     * @param swMatch source match
     * @param datapathid datapath id
     * @param ofVersion openflow version
     * @return match builder
     */
    public static MatchBuilder fromOFMatchV10ToSALMatch(@Nonnull final MatchV10 swMatch, @Nonnull final BigInteger datapathid, @Nonnull final OpenflowVersion ofVersion) {
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
            int vlanId = (swMatch.getDlVlan() == (0xffff)) ? 0 : swMatch.getDlVlan();
            vlanIdBuilder.setVlanId(new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId(vlanId));
            vlanIdBuilder.setVlanIdPresent(vlanId == 0 ? false : true);
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
                ipv4PrefixStr += IpConversionUtil.PREFIX_SEPARATOR + swMatch.getNwSrcMask();
            } else {
                //Openflow Spec : 1.3.2
                //An all-one-bits oxm_mask is equivalent to specifying 0 for oxm_hasmask and omitting oxm_mask.
                // So when user specify 32 as a mast, switch omit that mast and we get null as a mask in flow
                // statistics response.

                ipv4PrefixStr += IpConversionUtil.PREFIX_SEPARATOR + "32";
            }
            if (!NO_IP.equals(ipv4PrefixStr)) {
                ipv4MatchBuilder.setIpv4Source(new Ipv4Prefix(ipv4PrefixStr));
                matchBuilder.setLayer3Match(ipv4MatchBuilder.build());
            }
        }
        if (!swMatch.getWildcards().isDLTYPE().booleanValue() && swMatch.getNwDst() != null) {
            String ipv4PrefixStr = swMatch.getNwDst().getValue();
            if (swMatch.getNwDstMask() != null) {
                ipv4PrefixStr += IpConversionUtil.PREFIX_SEPARATOR + swMatch.getNwDstMask();
            } else {
                //Openflow Spec : 1.3.2
                //An all-one-bits oxm_mask is equivalent to specifying 0 for oxm_hasmask and omitting oxm_mask.
                // So when user specify 32 as a mast, switch omit that mast and we get null as a mask in flow
                // statistics response.

                ipv4PrefixStr += IpConversionUtil.PREFIX_SEPARATOR + "32";
            }
            if (!NO_IP.equals(ipv4PrefixStr)) {
                ipv4MatchBuilder.setIpv4Destination(new Ipv4Prefix(ipv4PrefixStr));
                matchBuilder.setLayer3Match(ipv4MatchBuilder.build());
            }
        }
        if (!swMatch.getWildcards().isNWPROTO().booleanValue() && swMatch.getNwProto() != null) {
            Short nwProto = swMatch.getNwProto();
            ipMatchBuilder.setIpProtocol(nwProto);
            matchBuilder.setIpMatch(ipMatchBuilder.build());

            int proto = nwProto.intValue();
            if (proto == PROTO_TCP) {
                TcpMatchBuilder tcpMatchBuilder = new TcpMatchBuilder();
                boolean hasTcp = false;
                if (!swMatch.getWildcards().isTPSRC().booleanValue() && swMatch.getTpSrc() != null) {
                    tcpMatchBuilder
                            .setTcpSourcePort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber(
                                    swMatch.getTpSrc()));
                    hasTcp = true;
                }
                if (!swMatch.getWildcards().isTPDST().booleanValue() && swMatch.getTpDst() != null) {
                    tcpMatchBuilder
                            .setTcpDestinationPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber(
                                    swMatch.getTpDst()));
                    hasTcp = true;
                }

                if (hasTcp) {
                    matchBuilder.setLayer4Match(tcpMatchBuilder.build());
                }
            } else if (proto == PROTO_UDP) {
                UdpMatchBuilder udpMatchBuilder = new UdpMatchBuilder();
                boolean hasUdp = false;
                if (!swMatch.getWildcards().isTPSRC().booleanValue() && swMatch.getTpSrc() != null) {
                    udpMatchBuilder
                            .setUdpSourcePort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber(
                                    swMatch.getTpSrc()));
                    hasUdp = true;
                }
                if (!swMatch.getWildcards().isTPDST().booleanValue() && swMatch.getTpDst() != null) {
                    udpMatchBuilder
                            .setUdpDestinationPort(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber(
                                    swMatch.getTpDst()));
                    hasUdp = true;
                }

                if (hasUdp) {
                    matchBuilder.setLayer4Match(udpMatchBuilder.build());
                }
            } else if (proto == PROTO_ICMPV4) {
                Icmpv4MatchBuilder icmpv4MatchBuilder = new Icmpv4MatchBuilder();
                boolean hasIcmpv4 = false;
                if (!swMatch.getWildcards().isTPSRC().booleanValue()) {
                    Integer type = swMatch.getTpSrc();
                    if (type != null) {
                        icmpv4MatchBuilder.setIcmpv4Type(type.shortValue());
                        hasIcmpv4 = true;
                    }
                }
                if (!swMatch.getWildcards().isTPDST().booleanValue()) {
                    Integer code = swMatch.getTpDst();
                    if (code != null) {
                        icmpv4MatchBuilder.setIcmpv4Code(code.shortValue());
                        hasIcmpv4 = true;
                    }
                }

                if (hasIcmpv4) {
                    matchBuilder.setIcmpv4Match(icmpv4MatchBuilder.build());
                }
            }
        }
        if (!swMatch.getWildcards().isNWTOS().booleanValue() && swMatch.getNwTos() != null) {
            Short dscp = ActionUtil.tosToDscp(swMatch.getNwTos().shortValue());
            ipMatchBuilder.setIpDscp(new Dscp(dscp));
            matchBuilder.setIpMatch(ipMatchBuilder.build());
        }

        return matchBuilder;
    }

    /**
     * Method converts Openflow 1.3+ specific flow match to MD-SAL format flow
     * match
     *
     * @param swMatch source match
     * @param datapathid datapath id
     * @param ofVersion openflow version
     * @return md-sal match instance
     * @author avishnoi@in.ibm.com
     */
    public static MatchBuilder fromOFMatchToSALMatch(
            @Nonnull final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match swMatch,
            @Nonnull final BigInteger datapathid, @Nonnull final OpenflowVersion ofVersion) {
        return OfMatchToSALMatchConvertor(swMatch.getMatchEntry(), datapathid, ofVersion);
    }

    private static MatchBuilder OfMatchToSALMatchConvertor(final List<MatchEntry> swMatchList, final BigInteger datapathid,
                                                           final OpenflowVersion ofVersion) {

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
        Ipv4MatchArbitraryBitMaskBuilder ipv4MatchArbitraryBitMaskBuilder = new Ipv4MatchArbitraryBitMaskBuilder();
        ArpMatchBuilder arpMatchBuilder = new ArpMatchBuilder();
        Ipv6MatchBuilder ipv6MatchBuilder = new Ipv6MatchBuilder();
        ProtocolMatchFieldsBuilder protocolMatchFieldsBuilder = new ProtocolMatchFieldsBuilder();
        TunnelIpv4MatchBuilder tunnelIpv4MatchBuilder = new TunnelIpv4MatchBuilder();

        for (MatchEntry ofMatch : swMatchList) {

            if (ofMatch.getOxmMatchField().equals(InPort.class)) {
                PortNumber portNumber = ((InPortCase) ofMatch.getMatchEntryValue()).getInPort().getPortNumber();
                if (portNumber != null) {
                    matchBuilder.setInPort(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(datapathid, portNumber.getValue(), ofVersion));
                }
            } else if (ofMatch.getOxmMatchField().equals(InPhyPort.class)) {
                PortNumber portNumber = ((InPhyPortCase) ofMatch.getMatchEntryValue()).getInPhyPort().getPortNumber();
                matchBuilder.setInPhyPort(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(datapathid,
                        portNumber.getValue(), ofVersion));
            } else if (ofMatch.getOxmMatchField().equals(Metadata.class)) {
                MetadataBuilder metadataBuilder = new MetadataBuilder();
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.metadata._case.Metadata metadata = ((MetadataCase) ofMatch.getMatchEntryValue()).getMetadata();
                if (metadata != null) {
                    metadataBuilder.setMetadata(new BigInteger(OFConstants.SIGNUM_UNSIGNED, metadata.getMetadata()));
                    byte[] metadataMask = metadata.getMask();
                    if (metadataMask != null) {
                        metadataBuilder.setMetadataMask(new BigInteger(OFConstants.SIGNUM_UNSIGNED, metadataMask));
                    }
                    matchBuilder.setMetadata(metadataBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(EthSrc.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.eth.src._case.EthSrc ethSrcCase = ((EthSrcCase) ofMatch.getMatchEntryValue()).getEthSrc();
                if (ethSrcCase != null) {
                    EthernetSourceBuilder ethSourceBuilder = new EthernetSourceBuilder();
                    ethSourceBuilder.setAddress(ethSrcCase.getMacAddress());
                    byte[] mask = ethSrcCase.getMask();
                    if (mask != null) {
                        ethSourceBuilder.setMask(new MacAddress(macAddressToString(mask)));
                    }
                    ethMatchBuilder.setEthernetSource(ethSourceBuilder.build());
                    matchBuilder.setEthernetMatch(ethMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(EthDst.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.eth.dst._case.EthDst ethDstCase = ((EthDstCase) ofMatch.getMatchEntryValue()).getEthDst();
                if (ethDstCase != null) {
                    EthernetDestinationBuilder ethDestinationBuilder = new EthernetDestinationBuilder();
                    ethDestinationBuilder.setAddress(ethDstCase.getMacAddress());
                    byte[] destinationMask = ethDstCase.getMask();
                    if (destinationMask != null) {
                        ethDestinationBuilder.setMask(new MacAddress(macAddressToString(destinationMask)));
                    }
                    ethMatchBuilder.setEthernetDestination(ethDestinationBuilder.build());
                    matchBuilder.setEthernetMatch(ethMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(EthType.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.eth.type._case.EthType ethTypeCase = ((EthTypeCase) ofMatch.getMatchEntryValue()).getEthType();
                if (ethTypeCase != null) {
                    EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
                    ethTypeBuilder
                            .setType(new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType(
                                    (long) ethTypeCase.getEthType().getValue()));
                    ethMatchBuilder.setEthernetType(ethTypeBuilder.build());
                    matchBuilder.setEthernetMatch(ethMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(VlanVid.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.vlan.vid._case.VlanVid vlanVid = ((VlanVidCase) ofMatch.getMatchEntryValue()).getVlanVid();
                if (vlanVid != null) {
                    VlanIdBuilder vlanBuilder = new VlanIdBuilder();
                    vlanBuilder.setVlanId(new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId(
                            vlanVid.getVlanVid()))
                            .setVlanIdPresent(vlanVid.isCfiBit());
                    vlanBuilder.setVlanIdPresent(vlanVid.isCfiBit());
                    vlanMatchBuilder.setVlanId(vlanBuilder.build());

                    matchBuilder.setVlanMatch(vlanMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(VlanPcp.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.vlan.pcp._case.VlanPcp vlanPcp = ((VlanPcpCase) ofMatch.getMatchEntryValue()).getVlanPcp();
                if (vlanPcp != null) {
                    vlanMatchBuilder
                            .setVlanPcp(new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp(
                                    vlanPcp.getVlanPcp()));
                    matchBuilder.setVlanMatch(vlanMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(IpDscp.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ip.dscp._case.IpDscp ipDscp = ((IpDscpCase) ofMatch.getMatchEntryValue()).getIpDscp();
                if (ipDscp != null) {
                    ipMatchBuilder.setIpDscp(new Dscp(ipDscp.getDscp().getValue()));
                    matchBuilder.setIpMatch(ipMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(IpEcn.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ip.ecn._case.IpEcn ipEcn = ((IpEcnCase) ofMatch.getMatchEntryValue()).getIpEcn();
                if (ipEcn != null) {
                    ipMatchBuilder.setIpEcn(ipEcn.getEcn());
                    matchBuilder.setIpMatch(ipMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(IpProto.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ip.proto._case.IpProto ipProto = ((IpProtoCase) ofMatch.getMatchEntryValue()).getIpProto();
                Short protocolNumber = ipProto.getProtocolNumber();
                if (protocolNumber != null) {
                    ipMatchBuilder.setIpProtocol(protocolNumber);
                    matchBuilder.setIpMatch(ipMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(TcpSrc.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.tcp.src._case.TcpSrc tcpSrc = ((TcpSrcCase) ofMatch.getMatchEntryValue()).getTcpSrc();
                if (tcpSrc != null) {
                    tcpMatchBuilder.setTcpSourcePort(tcpSrc.getPort());
                    matchBuilder.setLayer4Match(tcpMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(TcpDst.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.tcp.dst._case.TcpDst tcpDst = ((TcpDstCase) ofMatch.getMatchEntryValue()).getTcpDst();
                org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber portNumber = tcpDst.getPort();
                if (portNumber != null) {
                    tcpMatchBuilder.setTcpDestinationPort(portNumber);
                    matchBuilder.setLayer4Match(tcpMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(UdpSrc.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.udp.src._case.UdpSrc udpSrc = ((UdpSrcCase) ofMatch.getMatchEntryValue()).getUdpSrc();
                if (udpSrc != null) {
                    udpMatchBuilder.setUdpSourcePort(udpSrc.getPort());
                    matchBuilder.setLayer4Match(udpMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(UdpDst.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.udp.dst._case.UdpDst udpDst = ((UdpDstCase) ofMatch.getMatchEntryValue()).getUdpDst();
                if (udpDst != null) {
                    udpMatchBuilder.setUdpDestinationPort(udpDst.getPort());
                    matchBuilder.setLayer4Match(udpMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(SctpSrc.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.sctp.src._case.SctpSrc sctpSrc = ((SctpSrcCase) ofMatch.getMatchEntryValue()).getSctpSrc();
                org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber portNumber = sctpSrc.getPort();
                if (portNumber != null) {
                    sctpMatchBuilder.setSctpSourcePort(portNumber);
                    matchBuilder.setLayer4Match(sctpMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(SctpDst.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.sctp.dst._case.SctpDst sctpDst = ((SctpDstCase) ofMatch.getMatchEntryValue()).getSctpDst();
                org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber portNumber = sctpDst.getPort();
                if (portNumber != null) {
                    sctpMatchBuilder.setSctpDestinationPort(portNumber);
                    matchBuilder.setLayer4Match(sctpMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(Icmpv4Type.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv4.type._case.Icmpv4Type icmpv4Type = ((Icmpv4TypeCase) ofMatch.getMatchEntryValue()).getIcmpv4Type();
                Short type = icmpv4Type.getIcmpv4Type();
                if (type != null) {
                    icmpv4MatchBuilder.setIcmpv4Type(type);
                    matchBuilder.setIcmpv4Match(icmpv4MatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(Icmpv4Code.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv4.code._case.Icmpv4Code icmpv4Code = ((Icmpv4CodeCase) ofMatch.getMatchEntryValue()).getIcmpv4Code();
                Short v4code = icmpv4Code.getIcmpv4Code();
                if (v4code != null) {
                    icmpv4MatchBuilder.setIcmpv4Code(v4code);
                    matchBuilder.setIcmpv4Match(icmpv4MatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(Icmpv6Type.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv6.type._case.Icmpv6Type icmpv6Type = ((Icmpv6TypeCase) ofMatch.getMatchEntryValue()).getIcmpv6Type();
                Short v6type = icmpv6Type.getIcmpv6Type();
                if (v6type != null) {
                    icmpv6MatchBuilder.setIcmpv6Type(v6type);
                    matchBuilder.setIcmpv6Match(icmpv6MatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(Icmpv6Code.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.icmpv6.code._case.Icmpv6Code icmpv6Code = ((Icmpv6CodeCase) ofMatch.getMatchEntryValue()).getIcmpv6Code();
                Short v6code = icmpv6Code.getIcmpv6Code();
                if (v6code != null) {
                    icmpv6MatchBuilder.setIcmpv6Code(v6code);
                    matchBuilder.setIcmpv6Match(icmpv6MatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(Ipv4Src.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv4.src._case.Ipv4Src ipv4Address = ((Ipv4SrcCase) ofMatch.getMatchEntryValue()).getIpv4Src();
                if (ipv4Address != null) {
                    byte[] mask = ipv4Address.getMask();
                    if(IpConversionUtil.isMaskArbitraryBitMask(mask) && mask != null) {
                        DottedQuad dottedQuadMask = IpConversionUtil.createArbitraryBitMask(mask);
                        String Ipv4Address = ipv4Address.getIpv4Address().getValue();
                        setIpv4MatchArbitraryBitMaskBuilderFields(ipv4MatchArbitraryBitMaskBuilder, ofMatch, dottedQuadMask, Ipv4Address);
                        matchBuilder.setLayer3Match(ipv4MatchArbitraryBitMaskBuilder.build());
                    }
                    else {
                        String ipv4PrefixStr = ipv4Address.getIpv4Address().getValue();
                        setIpv4MatchBuilderFields(ipv4MatchBuilder, ofMatch, mask, ipv4PrefixStr);
                        matchBuilder.setLayer3Match(ipv4MatchBuilder.build());
                    }
                }
            } else if (ofMatch.getOxmMatchField().equals(Ipv4Dst.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv4.dst._case.Ipv4Dst ipv4Address = ((Ipv4DstCase) ofMatch.getMatchEntryValue()).getIpv4Dst();
                if (ipv4Address != null) {
                    byte[] mask = ipv4Address.getMask();
                    if(IpConversionUtil.isMaskArbitraryBitMask(mask) && mask != null) {
                        DottedQuad dottedQuadMask = IpConversionUtil.createArbitraryBitMask(mask);
                        String Ipv4Address = ipv4Address.getIpv4Address().getValue();
                        setIpv4MatchArbitraryBitMaskBuilderFields(ipv4MatchArbitraryBitMaskBuilder, ofMatch, dottedQuadMask, Ipv4Address);
                        matchBuilder.setLayer3Match(ipv4MatchArbitraryBitMaskBuilder.build());
                    }
                    else {
                        String ipv4PrefixStr = ipv4Address.getIpv4Address().getValue();
                        setIpv4MatchBuilderFields(ipv4MatchBuilder, ofMatch, mask, ipv4PrefixStr);
                        matchBuilder.setLayer3Match(ipv4MatchBuilder.build());
                    }
                }
            } else if (ofMatch.getOxmMatchField().equals(TunnelIpv4Dst.class)
                    || ofMatch.getOxmMatchField().equals(TunnelIpv4Src.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv4.dst._case.Ipv4Dst tunnelIpv4Dst = ((Ipv4DstCase) ofMatch.getMatchEntryValue()).getIpv4Dst();
                if (tunnelIpv4Dst != null) {
                    String ipv4PrefixStr = tunnelIpv4Dst.getIpv4Address().getValue();
                    byte[] mask = tunnelIpv4Dst.getMask();
                    ipv4PrefixStr += IpConversionUtil.PREFIX_SEPARATOR + ByteBuffer.wrap(tunnelIpv4Dst.getMask()).getInt();
                    setIpv4MatchBuilderFields(ipv4MatchBuilder, ofMatch, mask, ipv4PrefixStr);
                    matchBuilder.setLayer3Match(tunnelIpv4MatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(TunnelIpv4Src.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv4.src._case.Ipv4Src tunnelIpv4Dst = ((Ipv4SrcCase) ofMatch.getMatchEntryValue()).getIpv4Src();
                if (tunnelIpv4Dst != null) {
                    String ipv4PrefixStr = tunnelIpv4Dst.getIpv4Address().getValue();
                    byte[] mask = tunnelIpv4Dst.getMask();
                    ipv4PrefixStr += IpConversionUtil.PREFIX_SEPARATOR + ByteBuffer.wrap(tunnelIpv4Dst.getMask()).getInt();
                    setIpv4MatchBuilderFields(ipv4MatchBuilder, ofMatch, mask, ipv4PrefixStr);
                    matchBuilder.setLayer3Match(tunnelIpv4MatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(ArpOp.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.arp.op._case.ArpOp arpOp = ((ArpOpCase) ofMatch.getMatchEntryValue()).getArpOp();
                if (arpOp != null) {
                    arpMatchBuilder.setArpOp(arpOp.getOpCode());

                    matchBuilder.setLayer3Match(arpMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(ArpSpa.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.arp.spa._case.ArpSpa arpSpa = ((ArpSpaCase) ofMatch.getMatchEntryValue()).getArpSpa();
                if (arpSpa != null) {
                    int mask = 32;
                    if (null != arpSpa.getMask()){
                        mask = IpConversionUtil.countBits(arpSpa.getMask());
                    }
                    Ipv4Prefix ipv4Prefix = IpConversionUtil.createPrefix(arpSpa.getIpv4Address(), mask);
                    arpMatchBuilder.setArpSourceTransportAddress(ipv4Prefix);
                    matchBuilder.setLayer3Match(arpMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(ArpTpa.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.arp.tpa._case.ArpTpa arpTpa = ((ArpTpaCase) ofMatch.getMatchEntryValue()).getArpTpa();
                if (arpTpa != null) {
                    int mask = 32;
                    if (null != arpTpa.getMask()){
                        mask = IpConversionUtil.countBits(arpTpa.getMask());
                    }
                    Ipv4Prefix ipv4Prefix = IpConversionUtil.createPrefix(arpTpa.getIpv4Address(), mask);

                    arpMatchBuilder.setArpTargetTransportAddress(ipv4Prefix);
                    matchBuilder.setLayer3Match(arpMatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(ArpSha.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.arp.sha._case.ArpSha arpSha = ((ArpShaCase) ofMatch.getMatchEntryValue()).getArpSha();
                MacAddress macAddress = arpSha.getMacAddress();
                if (macAddress != null) {
                    if (ofMatch.getOxmMatchField().equals(ArpSha.class)) {
                        ArpSourceHardwareAddressBuilder arpSourceHardwareAddressBuilder = new ArpSourceHardwareAddressBuilder();
                        arpSourceHardwareAddressBuilder.setAddress(macAddress);
                        byte[] mask = arpSha.getMask();
                        if (mask != null) {
                            arpSourceHardwareAddressBuilder.setMask(new MacAddress(ByteBufUtils
                                    .macAddressToString(mask)));
                        }
                        arpMatchBuilder.setArpSourceHardwareAddress(arpSourceHardwareAddressBuilder.build());
                        matchBuilder.setLayer3Match(arpMatchBuilder.build());
                    }
                }
            } else if (ofMatch.getOxmMatchField().equals(ArpTha.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.arp.tha._case.ArpTha arpTha = ((ArpThaCase) ofMatch.getMatchEntryValue()).getArpTha();
                MacAddress macAddress = arpTha.getMacAddress();
                if (macAddress != null) {
                    if (ofMatch.getOxmMatchField().equals(ArpTha.class)) {
                        ArpTargetHardwareAddressBuilder arpTargetHardwareAddressBuilder = new ArpTargetHardwareAddressBuilder();
                        arpTargetHardwareAddressBuilder.setAddress(macAddress);
                        byte[] mask = arpTha.getMask();
                        if (mask != null) {
                            arpTargetHardwareAddressBuilder.setMask(new MacAddress(ByteBufUtils
                                    .macAddressToString(mask)));
                        }
                        arpMatchBuilder.setArpTargetHardwareAddress(arpTargetHardwareAddressBuilder.build());
                        matchBuilder.setLayer3Match(arpMatchBuilder.build());
                    }
                }
            } else if (ofMatch.getOxmMatchField().equals(Ipv6Src.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.src._case.Ipv6Src ipv6Src = ((Ipv6SrcCase) ofMatch.getMatchEntryValue()).getIpv6Src();

                if (ipv6Src != null) {
                    String ipv6PrefixStr = ipv6Src.getIpv6Address().getValue();
                    byte[] mask = ipv6Src.getMask();
                    setIpv6MatchBuilderFields(ipv6MatchBuilder, ofMatch, ipv6PrefixStr, mask);
                    matchBuilder.setLayer3Match(ipv6MatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(Ipv6Dst.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.dst._case.Ipv6Dst ipv6Dst = ((Ipv6DstCase) ofMatch.getMatchEntryValue()).getIpv6Dst();

                if (ipv6Dst != null) {
                    String ipv6PrefixStr = ipv6Dst.getIpv6Address().getValue();
                    byte[] mask = ipv6Dst.getMask();
                    setIpv6MatchBuilderFields(ipv6MatchBuilder, ofMatch, ipv6PrefixStr, mask);
                    matchBuilder.setLayer3Match(ipv6MatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(Ipv6Flabel.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.flabel._case.Ipv6Flabel ipv6Flabel = ((Ipv6FlabelCase) ofMatch.getMatchEntryValue()).getIpv6Flabel();
                if (ipv6Flabel != null) {
                    Ipv6LabelBuilder ipv6LabelBuilder = new Ipv6LabelBuilder();
                    ipv6LabelBuilder.setIpv6Flabel(new Ipv6FlowLabel(ipv6Flabel.getIpv6Flabel()));
                    byte[] mask = ipv6Flabel.getMask();
                    if (mask != null) {
                        ipv6LabelBuilder.setFlabelMask(new Ipv6FlowLabel(Long.valueOf(ByteUtil
                                .bytesToUnsignedInt(mask))));
                    }
                    ipv6MatchBuilder.setIpv6Label(ipv6LabelBuilder.build());
                    matchBuilder.setLayer3Match(ipv6MatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(Ipv6NdTarget.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.nd.target._case.Ipv6NdTarget ipv6NdTarget = ((Ipv6NdTargetCase) ofMatch.getMatchEntryValue()).getIpv6NdTarget();
                if (ipv6NdTarget != null) {
                    ipv6MatchBuilder.setIpv6NdTarget(ipv6NdTarget.getIpv6Address());
                    matchBuilder.setLayer3Match(ipv6MatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(Ipv6NdSll.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.nd.sll._case.Ipv6NdSll ipv6NdSll = ((Ipv6NdSllCase) ofMatch.getMatchEntryValue()).getIpv6NdSll();
                if (ipv6NdSll != null) {
                    ipv6MatchBuilder.setIpv6NdSll(ipv6NdSll.getMacAddress());
                    matchBuilder.setLayer3Match(ipv6MatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(Ipv6NdTll.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.nd.tll._case.Ipv6NdTll ipv6NdTll = ((Ipv6NdTllCase) ofMatch.getMatchEntryValue()).getIpv6NdTll();
                if (ipv6NdTll != null) {
                    ipv6MatchBuilder.setIpv6NdTll(ipv6NdTll.getMacAddress());
                    matchBuilder.setLayer3Match(ipv6MatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(Ipv6Exthdr.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.ipv6.exthdr._case.Ipv6Exthdr ipv6Exthdr = ((Ipv6ExthdrCase) ofMatch.getMatchEntryValue()).getIpv6Exthdr();
                if (ipv6Exthdr != null) {
                    Ipv6ExtHeaderBuilder ipv6ExtHeaderBuilder = new Ipv6ExtHeaderBuilder();

                    Ipv6ExthdrFlags pField = ipv6Exthdr.getPseudoField();
                    Integer bitmap = MatchConvertorUtil.ipv6ExthdrFlagsToInt(pField);

                    ipv6ExtHeaderBuilder.setIpv6Exthdr(bitmap);
                    byte[] mask = ipv6Exthdr.getMask();
                    if (mask != null) {
                        ipv6ExtHeaderBuilder.setIpv6ExthdrMask(ByteUtil.bytesToUnsignedShort(mask));
                    }
                    ipv6MatchBuilder.setIpv6ExtHeader(ipv6ExtHeaderBuilder.build());
                    matchBuilder.setLayer3Match(ipv6MatchBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(MplsLabel.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.mpls.label._case.MplsLabel mplsLabel = ((MplsLabelCase) ofMatch.getMatchEntryValue()).getMplsLabel();
                if (mplsLabel != null) {
                    protocolMatchFieldsBuilder.setMplsLabel(mplsLabel.getMplsLabel());
                    matchBuilder.setProtocolMatchFields(protocolMatchFieldsBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(MplsBos.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.mpls.bos._case.MplsBos mplsBos = ((MplsBosCase) ofMatch.getMatchEntryValue()).getMplsBos();
                if (mplsBos != null) {
                    protocolMatchFieldsBuilder.setMplsBos(mplsBos.isBos() ? (short) 1 : (short) 0);
                    matchBuilder.setProtocolMatchFields(protocolMatchFieldsBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(MplsTc.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.mpls.tc._case.MplsTc mplsTc = ((MplsTcCase) ofMatch.getMatchEntryValue()).getMplsTc();
                if (mplsTc != null) {
                    protocolMatchFieldsBuilder.setMplsTc(mplsTc.getTc());
                    matchBuilder.setProtocolMatchFields(protocolMatchFieldsBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(PbbIsid.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.pbb.isid._case.PbbIsid pbbIsid = ((PbbIsidCase) ofMatch.getMatchEntryValue()).getPbbIsid();
                if (pbbIsid != null) {
                    PbbBuilder pbbBuilder = new PbbBuilder();
                    pbbBuilder.setPbbIsid(pbbIsid.getIsid());
                    byte[] mask = pbbIsid.getMask();
                    if (mask != null) {
                        pbbBuilder.setPbbMask(ByteUtil.bytesToUnsignedMedium(mask));
                    }
                    protocolMatchFieldsBuilder.setPbb(pbbBuilder.build());
                    matchBuilder.setProtocolMatchFields(protocolMatchFieldsBuilder.build());
                }
            } else if (ofMatch.getOxmMatchField().equals(TunnelId.class)) {
                org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.tunnel.id._case.TunnelId tunnelId = ((TunnelIdCase) ofMatch.getMatchEntryValue()).getTunnelId();
                TunnelBuilder tunnelBuilder = new TunnelBuilder();
                if (tunnelId.getTunnelId()!= null) {
                    tunnelBuilder.setTunnelId(new BigInteger(OFConstants.SIGNUM_UNSIGNED, tunnelId.getTunnelId()));
                    byte[] mask = tunnelId.getMask();
                    if (null != mask) {
                        tunnelBuilder.setTunnelMask(new BigInteger(OFConstants.SIGNUM_UNSIGNED, mask));
                    }
                    matchBuilder.setTunnel(tunnelBuilder.build());
                }
            }
        }
        return matchBuilder;
    }

    private static void setIpv6MatchBuilderFields(final Ipv6MatchBuilder ipv6MatchBuilder, final MatchEntry ofMatch, final String ipv6PrefixStr, final byte[] mask) {
        Ipv6Prefix ipv6Prefix;

        if (mask != null) {
            ipv6Prefix = IpConversionUtil.createPrefix(new Ipv6Address(ipv6PrefixStr), mask);
        } else {
            ipv6Prefix = IpConversionUtil.createPrefix(new Ipv6Address(ipv6PrefixStr));
        }

        if (ofMatch.getOxmMatchField().equals(Ipv6Src.class)) {
            ipv6MatchBuilder.setIpv6Source(ipv6Prefix);
        }
        if (ofMatch.getOxmMatchField().equals(Ipv6Dst.class)) {
            ipv6MatchBuilder.setIpv6Destination(ipv6Prefix);
        }
    }

    private static void setIpv4MatchBuilderFields(final Ipv4MatchBuilder ipv4MatchBuilder, final MatchEntry ofMatch, final byte[] mask, final String ipv4PrefixStr) {
        Ipv4Prefix ipv4Prefix;
        if (mask != null) {
            ipv4Prefix = IpConversionUtil.createPrefix(new Ipv4Address(ipv4PrefixStr), mask);
        } else {
            //Openflow Spec : 1.3.2
            //An all-one-bits oxm_mask is equivalent to specifying 0 for oxm_hasmask and omitting oxm_mask.
            // So when user specify 32 as a mast, switch omit that mast and we get null as a mask in flow
            // statistics response.
            ipv4Prefix = IpConversionUtil.createPrefix(new Ipv4Address(ipv4PrefixStr));
        }
        if (ofMatch.getOxmMatchField().equals(Ipv4Src.class)) {
            ipv4MatchBuilder.setIpv4Source(ipv4Prefix);
        }
        if (ofMatch.getOxmMatchField().equals(Ipv4Dst.class)) {
            ipv4MatchBuilder.setIpv4Destination(ipv4Prefix);
        }
    }

    private static void setIpv4MatchArbitraryBitMaskBuilderFields(final Ipv4MatchArbitraryBitMaskBuilder ipv4MatchArbitraryBitMaskBuilder, final MatchEntry ofMatch, final DottedQuad mask, final String ipv4AddressStr) {
        Ipv4Address ipv4Address;
        DottedQuad dottedQuad;
        if (mask != null) {
            dottedQuad = mask;
            if (ofMatch.getOxmMatchField().equals(Ipv4Src.class)) {
                ipv4MatchArbitraryBitMaskBuilder.setIpv4SourceArbitraryBitMask(dottedQuad);
            }
            if (ofMatch.getOxmMatchField().equals(Ipv4Dst.class)) {
                ipv4MatchArbitraryBitMaskBuilder.setIpv4DestinationArbitraryBitMask(dottedQuad);
            }
        }
        ipv4Address = new Ipv4Address(ipv4AddressStr);
        if (ofMatch.getOxmMatchField().equals(Ipv4Src.class)) {
            ipv4MatchArbitraryBitMaskBuilder.setIpv4SourceAddressNoMask(ipv4Address);
        }
        if (ofMatch.getOxmMatchField().equals(Ipv4Dst.class)) {
            ipv4MatchArbitraryBitMaskBuilder.setIpv4DestinationAddressNoMask(ipv4Address);
        }
    }


    private static MatchEntry toOfMplsPbb(final Pbb pbb) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        boolean hasmask = false;
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setOxmMatchField(PbbIsid.class);
        PbbIsidCaseBuilder pbbIsidCaseBuilder = new PbbIsidCaseBuilder();
        PbbIsidBuilder pbbIsidBuilder = new PbbIsidBuilder();
        pbbIsidBuilder.setIsid(pbb.getPbbIsid());
        if (pbb.getPbbMask() != null) {
            hasmask = true;
            pbbIsidBuilder.setMask(ByteUtil.unsignedMediumToBytes(pbb.getPbbMask()));
        }
        pbbIsidCaseBuilder.setPbbIsid(pbbIsidBuilder.build());
        matchEntryBuilder.setMatchEntryValue(pbbIsidCaseBuilder.build());
        matchEntryBuilder.setHasMask(hasmask);
        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfMplsTc(final Short mplsTc) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(MplsTc.class);
        MplsTcCaseBuilder mplsTcCaseBuilder = new MplsTcCaseBuilder();
        MplsTcBuilder mplsTcBuilder = new MplsTcBuilder();
        mplsTcBuilder.setTc(mplsTc);
        mplsTcCaseBuilder.setMplsTc(mplsTcBuilder.build());
        matchEntryBuilder.setMatchEntryValue(mplsTcCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfMplsBos(final Short mplsBos) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(MplsBos.class);
        MplsBosCaseBuilder mplsBosCaseBuilder = new MplsBosCaseBuilder();
        MplsBosBuilder mplsBosBuilder = new MplsBosBuilder();
        boolean isBos = false;
        if (mplsBos.shortValue() != 0) {
            isBos = true;
        }
        mplsBosBuilder.setBos(isBos);
        mplsBosCaseBuilder.setMplsBos(mplsBosBuilder.build());

        matchEntryBuilder.setMatchEntryValue(mplsBosCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfMplsLabel(final Long mplsLabel) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(MplsLabel.class);

        MplsLabelCaseBuilder mplsLabelCaseBuilder = new MplsLabelCaseBuilder();
        MplsLabelBuilder mplsLabelBuilder = new MplsLabelBuilder();
        mplsLabelBuilder.setMplsLabel(mplsLabel);
        mplsLabelCaseBuilder.setMplsLabel(mplsLabelBuilder.build());
        matchEntryBuilder.setMatchEntryValue(mplsLabelCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfIpv6ExtHeader(final Ipv6ExtHeader ipv6ExtHeader) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        boolean hasmask = false;
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setOxmMatchField(Ipv6Exthdr.class);
        Ipv6ExthdrCaseBuilder ipv6ExthdrCaseBuilder = new Ipv6ExthdrCaseBuilder();
        Ipv6ExthdrBuilder ipv6ExthdrBuilder = new Ipv6ExthdrBuilder();

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

        ipv6ExthdrBuilder.setPseudoField(new Ipv6ExthdrFlags(AUTH, DEST, ESP, FRAG, HOP, NONEXT, ROUTER, UNREP, UNSEQ));
        //TODO ipv6ExthdrBuilder.setMask()
        if (ipv6ExtHeader.getIpv6ExthdrMask() != null) {
            hasmask = true;
            ipv6ExthdrBuilder.setMask(ByteUtil.unsignedShortToBytes(ipv6ExtHeader.getIpv6ExthdrMask()));
        }
        ipv6ExthdrCaseBuilder.setIpv6Exthdr(ipv6ExthdrBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ipv6ExthdrCaseBuilder.build());
        matchEntryBuilder.setHasMask(hasmask);
        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfIpv6FlowLabel(final Ipv6Label ipv6Label) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        boolean hasmask = false;
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setOxmMatchField(Ipv6Flabel.class);
        Ipv6FlabelCaseBuilder ipv6FlabelCaseBuilder = new Ipv6FlabelCaseBuilder();
        Ipv6FlabelBuilder ipv6FlabelBuilder = new Ipv6FlabelBuilder();
        ipv6FlabelBuilder.setIpv6Flabel(ipv6Label.getIpv6Flabel());
        if (ipv6Label.getFlabelMask() != null) {
            hasmask = true;
            ipv6FlabelBuilder.setMask(ByteUtil.unsignedIntToBytes(ipv6Label.getFlabelMask().getValue()));
        }
        ipv6FlabelCaseBuilder.setIpv6Flabel(ipv6FlabelBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ipv6FlabelCaseBuilder.build());
        matchEntryBuilder.setHasMask(hasmask);
        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfPort(final Class<? extends MatchField> field, final Long portNumber) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(field);
        InPortCaseBuilder caseBuilder = new InPortCaseBuilder();
        InPortBuilder portBuilder = new InPortBuilder();
        portBuilder.setPortNumber(new PortNumber(portNumber));
        caseBuilder.setInPort(portBuilder.build());
        matchEntryBuilder.setMatchEntryValue(caseBuilder.build());

        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfPhyPort(final Class<? extends MatchField> field, final Long portNumber) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(field);
        InPhyPortCaseBuilder caseBuilder = new InPhyPortCaseBuilder();
        InPhyPortBuilder portBuilder = new InPhyPortBuilder();
        portBuilder.setPortNumber(new PortNumber(portNumber));
        caseBuilder.setInPhyPort(portBuilder.build());
        matchEntryBuilder.setMatchEntryValue(caseBuilder.build());

        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfMetadata(final Class<? extends MatchField> field, final BigInteger metadata,
                                           final BigInteger metadataMask) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        boolean hasmask = false;
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setOxmMatchField(field);
        MetadataCaseBuilder metadataCaseBuilder = new MetadataCaseBuilder();
        org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.metadata._case.MetadataBuilder metadataBuilder = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.match.entry.value.metadata._case.MetadataBuilder();
        metadataBuilder.setMetadata(ByteUtil.convertBigIntegerToNBytes(metadata, OFConstants.SIZE_OF_LONG_IN_BYTES));
        if (metadataMask != null) {
            hasmask = true;
            metadataBuilder.setMask(ByteUtil.convertBigIntegerToNBytes(metadataMask, OFConstants.SIZE_OF_LONG_IN_BYTES));
        }
        metadataCaseBuilder.setMetadata(metadataBuilder.build());
        matchEntryBuilder.setMatchEntryValue(metadataCaseBuilder.build());
        matchEntryBuilder.setHasMask(hasmask);
        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfEthernetType(final EthernetType ethernetType) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(EthType.class);
        EthTypeCaseBuilder ethTypeCaseBuilder = new EthTypeCaseBuilder();
        EthTypeBuilder ethTypeBuilder = new EthTypeBuilder();
        EtherType etherType = new EtherType(ethernetType.getType().getValue().intValue());
        ethTypeBuilder.setEthType(etherType);
        ethTypeCaseBuilder.setEthType(ethTypeBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ethTypeCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfIcmpv4Type(final Short icmpv4Type) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(Icmpv4Type.class);
        Icmpv4TypeCaseBuilder icmpv4TypeCaseBuilder = new Icmpv4TypeCaseBuilder();
        Icmpv4TypeBuilder icmpv4TypeBuilder = new Icmpv4TypeBuilder();
        icmpv4TypeBuilder.setIcmpv4Type(icmpv4Type);
        icmpv4TypeCaseBuilder.setIcmpv4Type(icmpv4TypeBuilder.build());
        matchEntryBuilder.setMatchEntryValue(icmpv4TypeCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfIcmpv4Code(final Short icmpv4Code) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(Icmpv4Code.class);
        Icmpv4CodeCaseBuilder icmpv4CodeCaseBuilder = new Icmpv4CodeCaseBuilder();
        Icmpv4CodeBuilder icmpv4CodeBuilder = new Icmpv4CodeBuilder();
        icmpv4CodeBuilder.setIcmpv4Code(icmpv4Code);
        icmpv4CodeCaseBuilder.setIcmpv4Code(icmpv4CodeBuilder.build());
        matchEntryBuilder.setMatchEntryValue(icmpv4CodeCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfIcmpv6Type(final Short icmpv6Type) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(Icmpv6Type.class);
        Icmpv6TypeCaseBuilder icmpv6TypeCaseBuilder = new Icmpv6TypeCaseBuilder();
        Icmpv6TypeBuilder icmpv6TypeBuilder = new Icmpv6TypeBuilder();
        icmpv6TypeBuilder.setIcmpv6Type(icmpv6Type);
        icmpv6TypeCaseBuilder.setIcmpv6Type(icmpv6TypeBuilder.build());
        matchEntryBuilder.setMatchEntryValue(icmpv6TypeCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfIcmpv6Code(final Short icmpv6Code) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(Icmpv6Code.class);
        Icmpv6CodeCaseBuilder icmpv6CodeCaseBuilder = new Icmpv6CodeCaseBuilder();
        Icmpv6CodeBuilder icmpv6CodeBuilder = new Icmpv6CodeBuilder();
        icmpv6CodeBuilder.setIcmpv6Code(icmpv6Code);
        icmpv6CodeCaseBuilder.setIcmpv6Code(icmpv6CodeBuilder.build());
        matchEntryBuilder.setMatchEntryValue(icmpv6CodeCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    public static MatchEntry toOfIpDscp(final Dscp ipDscp) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(IpDscp.class);

        IpDscpCaseBuilder ipDscpCaseBuilder = new IpDscpCaseBuilder();
        IpDscpBuilder ipDscpBuilder = new IpDscpBuilder();
        ipDscpBuilder.setDscp(ipDscp);
        ipDscpCaseBuilder.setIpDscp(ipDscpBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ipDscpCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    public static MatchEntry toOfVlanPcp(
            final org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp vlanPcp) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(VlanPcp.class);
        VlanPcpCaseBuilder vlanPcpCaseBuilder = new VlanPcpCaseBuilder();
        VlanPcpBuilder vlanPcpBuilder = new VlanPcpBuilder();
        vlanPcpBuilder.setVlanPcp(vlanPcp.getValue());
        vlanPcpCaseBuilder.setVlanPcp(vlanPcpBuilder.build());
        matchEntryBuilder.setMatchEntryValue(vlanPcpCaseBuilder.build());
        return matchEntryBuilder.build();
    }


    private static MatchEntry toOfIpProto(final Short ipProtocol) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(IpProto.class);
        IpProtoCaseBuilder ipProtoCaseBuilder = new IpProtoCaseBuilder();
        IpProtoBuilder ipProtoBuilder = new IpProtoBuilder();
        ipProtoBuilder.setProtocolNumber(ipProtocol);
        ipProtoCaseBuilder.setIpProto(ipProtoBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ipProtoCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfIpEcn(final Short ipEcn) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(IpEcn.class);
        IpEcnCaseBuilder ipEcnCaseBuilder = new IpEcnCaseBuilder();
        IpEcnBuilder ipEcnBuilder = new IpEcnBuilder();
        ipEcnBuilder.setEcn(ipEcn);
        ipEcnCaseBuilder.setIpEcn(ipEcnBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ipEcnCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfArpOpCode(final Integer arpOp) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(ArpOp.class);
        ArpOpCaseBuilder arpOpCaseBuilder = new ArpOpCaseBuilder();
        ArpOpBuilder arpOpBuilder = new ArpOpBuilder();
        arpOpBuilder.setOpCode(arpOp);
        arpOpCaseBuilder.setArpOp(arpOpBuilder.build());
        matchEntryBuilder.setMatchEntryValue(arpOpCaseBuilder.build());
        return matchEntryBuilder.build();
    }

    private static MatchEntry toOfIpv6NdTargetAddress(final Ipv6Address address) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setOxmClass(OpenflowBasicClass.class);
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(Ipv6NdTarget.class);

        Ipv6NdTargetCaseBuilder ipv6NdTargetCaseBuilder = new Ipv6NdTargetCaseBuilder();
        Ipv6NdTargetBuilder ipv6NdTargetBuilder = new Ipv6NdTargetBuilder();
        ipv6NdTargetBuilder.setIpv6Address(address);
        ipv6NdTargetCaseBuilder.setIpv6NdTarget(ipv6NdTargetBuilder.build());
        matchEntryBuilder.setMatchEntryValue(ipv6NdTargetCaseBuilder.build());
        return matchEntryBuilder.build();
    }




    /**
     * Method converts OF SetField action to SAL SetFiled action.
     *
     * @param action input action
     * @param ofVersion current ofp version
     * @return set field builder
     */
    public static SetField fromOFSetFieldToSALSetFieldAction(
            final Action action, final OpenflowVersion ofVersion) {
        logger.debug("Converting OF SetField action to SAL SetField action");
        SetFieldCase setFieldCase = (SetFieldCase) action.getActionChoice();
        SetFieldAction setFieldAction = setFieldCase.getSetFieldAction();

        SetFieldBuilder setField = new SetFieldBuilder();
        MatchBuilder match = OfMatchToSALMatchConvertor(setFieldAction.getMatchEntry(), null, ofVersion);
        setField.fieldsFrom(match.build());
        return setField.build();
    }

}
