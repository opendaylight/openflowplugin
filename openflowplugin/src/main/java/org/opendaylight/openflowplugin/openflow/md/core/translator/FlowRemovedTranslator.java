/**
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributor: deepthi.v.v@ericsson.com
 */
package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import org.opendaylight.openflowplugin.openflow.md.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.match.MatchConvertorImpl;
import org.opendaylight.openflowplugin.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6FlowLabel;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SwitchFlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.RemovedReasonFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.mod.removed.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.mod.removed.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanId;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.FlowRemovedReason;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.oxm.fields.grouping.MatchEntries;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowRemovedTranslator implements IMDMessageTranslator<OfHeader, List<DataObject>> {

    protected static final Logger LOG = LoggerFactory.getLogger(FlowRemovedTranslator.class);
    private static final String PREFIX_SEPARATOR = "/";

    @Override
    public List<DataObject> translate(SwitchConnectionDistinguisher cookie, SessionContext sc, OfHeader msg) {
        if (msg instanceof FlowRemovedMessage) {
            FlowRemovedMessage ofFlow = (FlowRemovedMessage) msg;
            List<DataObject> list = new CopyOnWriteArrayList<DataObject>();
            LOG.debug("Flow Removed Message received: Table Id={}, Flow removed reason={} ", ofFlow.getTableId(),
                    ofFlow.getReason());

            SwitchFlowRemovedBuilder salFlowRemoved = new SwitchFlowRemovedBuilder();

            salFlowRemoved.setCookie(ofFlow.getCookie());
            salFlowRemoved.setPriority(ofFlow.getPriority());

            if (ofFlow.getTableId() != null) {
                salFlowRemoved.setTableId(ofFlow.getTableId().getValue().shortValue());
            }

            salFlowRemoved.setDurationSec(ofFlow.getDurationSec());
            salFlowRemoved.setDurationNsec(ofFlow.getDurationNsec());
            salFlowRemoved.setIdleTimeout(ofFlow.getIdleTimeout());
            salFlowRemoved.setHardTimeout(ofFlow.getHardTimeout());
            salFlowRemoved.setPacketCount(ofFlow.getPacketCount());
            salFlowRemoved.setByteCount(ofFlow.getByteCount());
            RemovedReasonFlags removeReasonFlag = new RemovedReasonFlags(
                    FlowRemovedReason.OFPRRDELETE.equals(ofFlow.getReason()),
                    FlowRemovedReason.OFPRRGROUPDELETE.equals(ofFlow.getReason()),
                    FlowRemovedReason.OFPRRHARDTIMEOUT.equals(ofFlow.getReason()),
                    FlowRemovedReason.OFPRRIDLETIMEOUT.equals(ofFlow.getReason())
                    );

            salFlowRemoved.setRemovedReason(removeReasonFlag);

            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.grouping.Match ofMatch = ofFlow
                    .getMatch();
            if (ofMatch != null) {
                salFlowRemoved.setMatch(fromMatch(ofMatch,sc.getFeatures().getDatapathId()));
            }
            else if(ofFlow.getMatchV10() != null){
                MatchBuilder matchBuilder = new MatchBuilder(MatchConvertorImpl.fromOFMatchV10ToSALMatch(ofFlow.getMatchV10(),sc.getFeatures().getDatapathId()));
                salFlowRemoved.setMatch(matchBuilder.build());
            }
            salFlowRemoved.setNode(new NodeRef(InventoryDataServiceUtil.identifierFromDatapathId(sc.getFeatures()
                    .getDatapathId())));
            list.add(salFlowRemoved.build());
            return list;
        } else {
            LOG.error("Message is not a flow removed message ");
            return Collections.emptyList();
        }
    }


    public Match fromMatch(
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev130731.match.grouping.Match ofMatch,BigInteger datapathid) {
        MatchBuilder matchBuilder = new MatchBuilder();
        EthernetMatchBuilder ethernetMatch = null;
        VlanMatchBuilder vlanMatch = null;
        IpMatchBuilder ipMatch = null;
        TcpMatchBuilder tcpMatch = null;
        UdpMatchBuilder udpMatch = null;
        SctpMatchBuilder sctpMatch = null;
        Icmpv4MatchBuilder icmpv4Match = null;
        Icmpv6MatchBuilder icmpv6Match = null;
        Ipv4MatchBuilder ipv4Match = null;
        ArpMatchBuilder arpMatch = null;
        Ipv6MatchBuilder ipv6Match = null;
        ProtocolMatchFieldsBuilder protocolMatchFields = null;

        for (MatchEntries entry : ofMatch.getMatchEntries()) {
            Class<? extends MatchField> field = entry.getOxmMatchField();
            if (field.equals(InPort.class)) {
                matchBuilder.setInPort(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(datapathid,entry.getAugmentation(PortNumberMatchEntry.class).getPortNumber().getValue()
                        .longValue()));
            } else if (field.equals(InPhyPort.class)) {
                matchBuilder.setInPhyPort(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(datapathid,entry.getAugmentation(PortNumberMatchEntry.class).getPortNumber().getValue()
                        .longValue()));
            } else if (field.equals(Metadata.class)) {
                MetadataBuilder metadata = new MetadataBuilder();
                metadata.setMetadata(new BigInteger(1, entry.getAugmentation(MetadataMatchEntry.class).getMetadata()));
                if (entry.isHasMask()) {
                    metadata.setMetadataMask(new BigInteger(OFConstants.SIGNUM_UNSIGNED, entry.getAugmentation(MaskMatchEntry.class).getMask()));
                }
                matchBuilder.setMetadata(metadata.build());
            } else if (field.equals(EthDst.class) || field.equals(EthSrc.class) || field.equals(EthType.class)) {
                if (ethernetMatch == null) {
                    ethernetMatch = new EthernetMatchBuilder();
                }
                if (field.equals(EthDst.class)) {
                    EthernetDestinationBuilder ethDst = new EthernetDestinationBuilder();
                    ethDst.setAddress(entry.getAugmentation(MacAddressMatchEntry.class).getMacAddress());
                    if (entry.isHasMask()) {
                        ethDst.setMask(new MacAddress(ByteUtil.bytesToHexstring(entry.getAugmentation(MaskMatchEntry.class).getMask(),":")));
                    }
                    ethernetMatch.setEthernetDestination(ethDst.build());
                } else if (field.equals(EthSrc.class)) {
                    EthernetSourceBuilder ethSrc = new EthernetSourceBuilder();
                    ethSrc.setAddress(entry.getAugmentation(MacAddressMatchEntry.class).getMacAddress());
                    if (entry.isHasMask()) {
                        ethSrc.setMask(new MacAddress(ByteUtil.bytesToHexstring(entry.getAugmentation(MaskMatchEntry.class).getMask(),":")));
                    }
                    ethernetMatch.setEthernetSource(ethSrc.build());
                } else if (field.equals(EthType.class)) {
                    EthernetTypeBuilder ethType = new EthernetTypeBuilder();
                    ethType.setType(new EtherType(entry.getAugmentation(EthTypeMatchEntry.class).getEthType()
                            .getValue().longValue()));
                    ethernetMatch.setEthernetType(ethType.build());
                }
            } else if (field.equals(VlanVid.class) || field.equals(VlanPcp.class)) {
                if (vlanMatch == null) {
                    vlanMatch = new VlanMatchBuilder();
                }
                if (field.equals(VlanVid.class)) {
                    boolean vlanIdPresent = false;
                    VlanIdBuilder vlanId = new VlanIdBuilder();
                    VlanVidMatchEntry vlanVid = entry.getAugmentation(VlanVidMatchEntry.class);
                    Integer vlanVidValue = vlanVid.getVlanVid();
                    if (vlanVid.isCfiBit()) {
                        vlanIdPresent = true;
                    }
                    vlanId.setVlanIdPresent(vlanIdPresent);
                    if (vlanVidValue != null) {
                        vlanId.setVlanId(new VlanId(vlanVidValue));
                    }
                    vlanMatch.setVlanId(vlanId.build());
                } else if (field.equals(VlanPcp.class)) {
                    vlanMatch.setVlanPcp(new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp(
                            entry.getAugmentation(VlanPcpMatchEntry.class).getVlanPcp().shortValue()));
                }
            } else if (field.equals(IpDscp.class) || field.equals(IpEcn.class) || field.equals(IpProto.class)) {
                if (ipMatch == null) {
                    ipMatch = new IpMatchBuilder();
                }
                if (field.equals(IpDscp.class)) {
                    ipMatch.setIpDscp(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Dscp(
                            entry.getAugmentation(DscpMatchEntry.class).getDscp().getValue()));
                } else if (field.equals(IpEcn.class)) {
                    ipMatch.setIpEcn(entry.getAugmentation(EcnMatchEntry.class).getEcn());
                } else if (field.equals(IpProto.class)) {
                    ipMatch.setIpProtocol(entry.getAugmentation(ProtocolNumberMatchEntry.class).getProtocolNumber());
                }
            } else if (field.equals(TcpSrc.class) || field.equals(TcpDst.class)) {
                if (tcpMatch == null) {
                    tcpMatch = new TcpMatchBuilder();
                }
                if (field.equals(TcpSrc.class)) {
                    tcpMatch.setTcpSourcePort(new PortNumber(entry.getAugmentation(PortMatchEntry.class).getPort()
                            .getValue()));
                } else if (field.equals(TcpDst.class)) {
                    tcpMatch.setTcpDestinationPort(new PortNumber(entry.getAugmentation(PortMatchEntry.class).getPort()
                            .getValue()));
                }
            } else if (field.equals(UdpSrc.class) || field.equals(UdpDst.class)) {
                if (udpMatch == null) {
                    udpMatch = new UdpMatchBuilder();
                }
                if (field.equals(UdpSrc.class)) {
                    udpMatch.setUdpSourcePort(new PortNumber(entry.getAugmentation(PortMatchEntry.class).getPort()
                            .getValue()));
                } else if (field.equals(UdpDst.class)) {
                    udpMatch.setUdpDestinationPort(new PortNumber(entry.getAugmentation(PortMatchEntry.class).getPort()
                            .getValue()));
                }
            } else if (field.equals(SctpSrc.class) || field.equals(SctpDst.class)) {
                if (sctpMatch == null) {
                    sctpMatch = new SctpMatchBuilder();
                }
                if (field.equals(SctpSrc.class)) {
                    sctpMatch.setSctpSourcePort(new PortNumber(entry.getAugmentation(PortMatchEntry.class).getPort()
                            .getValue()));
                } else if (field.equals(SctpDst.class)) {
                    sctpMatch.setSctpDestinationPort(new PortNumber(entry.getAugmentation(PortMatchEntry.class)
                            .getPort().getValue()));
                }
            } else if (field.equals(Icmpv4Type.class) || field.equals(Icmpv4Code.class)) {
                if (icmpv4Match == null) {
                    icmpv4Match = new Icmpv4MatchBuilder();
                }
                if (field.equals(Icmpv4Type.class)) {
                    icmpv4Match.setIcmpv4Type(entry.getAugmentation(Icmpv4TypeMatchEntry.class).getIcmpv4Type());
                } else if (field.equals(Icmpv4Code.class)) {
                    icmpv4Match.setIcmpv4Code(entry.getAugmentation(Icmpv4CodeMatchEntry.class).getIcmpv4Code());
                }
            } else if (field.equals(Icmpv6Type.class) || field.equals(Icmpv6Code.class)) {
                if (icmpv6Match == null) {
                    icmpv6Match = new Icmpv6MatchBuilder();
                }
                if (field.equals(Icmpv6Type.class)) {
                    icmpv6Match.setIcmpv6Type(entry.getAugmentation(Icmpv6TypeMatchEntry.class).getIcmpv6Type());
                } else if (field.equals(Icmpv6Code.class)) {
                    icmpv6Match.setIcmpv6Code(entry.getAugmentation(Icmpv6CodeMatchEntry.class).getIcmpv6Code());
                }
            } else if (field.equals(Ipv4Src.class) || field.equals(Ipv4Dst.class)) {
                if (ipv4Match == null) {
                    ipv4Match = new Ipv4MatchBuilder();
                }
                if (field.equals(Ipv4Src.class)) {
                    ipv4Match.setIpv4Source(toIpv4Prefix(entry));
                } else if (field.equals(Ipv4Dst.class)) {
                    ipv4Match.setIpv4Destination(toIpv4Prefix(entry));
                }
            } else if (field.equals(ArpOp.class) || field.equals(ArpSpa.class) || field.equals(ArpTpa.class)
                    || field.equals(ArpSha.class) || field.equals(ArpTha.class)) {
                if (arpMatch == null) {
                    arpMatch = new ArpMatchBuilder();
                }
                if (field.equals(ArpOp.class)) {
                    arpMatch.setArpOp(entry.getAugmentation(OpCodeMatchEntry.class).getOpCode());
                } else if (field.equals(ArpSpa.class)) {
                    arpMatch.setArpSourceTransportAddress(toIpv4Prefix(entry));
                } else if (field.equals(ArpTpa.class)) {
                    arpMatch.setArpTargetTransportAddress(toIpv4Prefix(entry));
                } else if (field.equals(ArpSha.class)) {
                    ArpSourceHardwareAddressBuilder arpSha = new ArpSourceHardwareAddressBuilder();
                    arpSha.setAddress(entry.getAugmentation(MacAddressMatchEntry.class).getMacAddress());
                    if (entry.isHasMask()) {
                        arpSha.setMask(new MacAddress(ByteUtil.bytesToHexstring(entry.getAugmentation(MaskMatchEntry.class).getMask(),":")));
                    }
                    arpMatch.setArpSourceHardwareAddress(arpSha.build());
                } else if (field.equals(ArpTha.class)) {
                    ArpTargetHardwareAddressBuilder arpTha = new ArpTargetHardwareAddressBuilder();
                    arpTha.setAddress(entry.getAugmentation(MacAddressMatchEntry.class).getMacAddress());
                    if (entry.isHasMask()) {
                        arpTha.setMask(new MacAddress(ByteUtil.bytesToHexstring(entry.getAugmentation(MaskMatchEntry.class).getMask(),":")));
                    }
                    arpMatch.setArpTargetHardwareAddress(arpTha.build());
                }
            } else if (field.equals(Ipv6Src.class) || field.equals(Ipv6Dst.class) || field.equals(Ipv6Flabel.class)
                    || field.equals(Ipv6NdTarget.class) || field.equals(Ipv6NdSll.class)
                    || field.equals(Ipv6NdTll.class) || field.equals(Ipv6Exthdr.class)) {
                if (ipv6Match == null) {
                    ipv6Match = new Ipv6MatchBuilder();
                }
                if (field.equals(Ipv6Src.class)) {
                    ipv6Match.setIpv6Source(toIpv6Prefix(entry));
                } else if (field.equals(Ipv6Dst.class)) {
                    ipv6Match.setIpv6Destination(toIpv6Prefix(entry));
                } else if (field.equals(Ipv6Flabel.class)) {
                    Ipv6LabelBuilder ipv6Label = new Ipv6LabelBuilder();
                    ipv6Label.setIpv6Flabel(entry.getAugmentation(Ipv6FlabelMatchEntry.class).getIpv6Flabel());
                    if (entry.isHasMask()) {
                        ipv6Label.setFlabelMask(new Ipv6FlowLabel(ByteUtil.bytesToUnsignedInt(entry.getAugmentation(MaskMatchEntry.class).getMask())));
                    }
                    ipv6Match.setIpv6Label(ipv6Label.build());
                } else if (field.equals(Ipv6NdTarget.class)) {
                    ipv6Match.setIpv6NdTarget(entry.getAugmentation(Ipv6AddressMatchEntry.class).getIpv6Address());
                } else if (field.equals(Ipv6NdSll.class)) {
                    ipv6Match.setIpv6NdSll(entry.getAugmentation(MacAddressMatchEntry.class).getMacAddress());
                } else if (field.equals(Ipv6NdTll.class)) {
                    ipv6Match.setIpv6NdTll(entry.getAugmentation(MacAddressMatchEntry.class).getMacAddress());
                } else if (field.equals(Ipv6Exthdr.class)) {
                    // verify
                    Ipv6ExtHeaderBuilder ipv6ExtHeaderBuilder = new Ipv6ExtHeaderBuilder();
                    Ipv6ExthdrFlags pseudoField = entry.getAugmentation(PseudoFieldMatchEntry.class).getPseudoField();
                    Map<Integer, Boolean> map = new HashMap<>();
                    map.put(0, pseudoField.isNonext());
                    map.put(1, pseudoField.isEsp());
                    map.put(2, pseudoField.isAuth());
                    map.put(3, pseudoField.isDest());
                    map.put(4, pseudoField.isFrag());
                    map.put(5, pseudoField.isRouter());
                    map.put(6, pseudoField.isHop());
                    map.put(7, pseudoField.isUnrep());
                    map.put(8, pseudoField.isUnseq());
                    int bitmap = fillBitMaskFromMap(map);
                    ipv6ExtHeaderBuilder.setIpv6Exthdr(bitmap);
                    if (entry.isHasMask()) {
                        ipv6ExtHeaderBuilder.setIpv6ExthdrMask(
                            ByteUtil.bytesToUnsignedShort(entry.getAugmentation(MaskMatchEntry.class).getMask()));
                    }
                    ipv6Match.setIpv6ExtHeader(ipv6ExtHeaderBuilder.build());
                }
            } else if (field.equals(MplsLabel.class) || field.equals(MplsTc.class) || field.equals(MplsBos.class)
                    || field.equals(PbbIsid.class)) {
                if (protocolMatchFields == null) {
                    protocolMatchFields = new ProtocolMatchFieldsBuilder();
                }
                if (field.equals(MplsLabel.class)) {
                    protocolMatchFields.setMplsLabel(entry.getAugmentation(MplsLabelMatchEntry.class).getMplsLabel());
                } else if (field.equals(MplsTc.class)) {
                    protocolMatchFields.setMplsTc(entry.getAugmentation(TcMatchEntry.class).getTc());
                } else if (field.equals(MplsBos.class)) {
                    protocolMatchFields
                            .setMplsBos((short) (entry.getAugmentation(BosMatchEntry.class).isBos() ? 1 : 0));
                } else if (field.equals(PbbIsid.class)) {
                    PbbBuilder pbb = new PbbBuilder();
                    pbb.setPbbIsid(entry.getAugmentation(IsidMatchEntry.class).getIsid());
                    if (entry.isHasMask()) {
                        pbb.setPbbMask(ByteUtil.bytesToUnsignedInt(entry.getAugmentation(MaskMatchEntry.class).getMask()));
                    }
                    protocolMatchFields.setPbb(pbb.build());
                }
            } else if (field.equals(TunnelId.class)) {
                TunnelBuilder tunnel = new TunnelBuilder();
                tunnel.setTunnelId(new BigInteger(1, entry.getAugmentation(MetadataMatchEntry.class).getMetadata()));
                if (entry.isHasMask()) {
                    tunnel.setTunnelMask(new BigInteger(OFConstants.SIGNUM_UNSIGNED, entry.getAugmentation(MaskMatchEntry.class).getMask()));
                }
                matchBuilder.setTunnel(tunnel.build());
            }
        }

        if (ethernetMatch != null) {
            matchBuilder.setEthernetMatch(ethernetMatch.build());
        }
        if (vlanMatch != null) {
            matchBuilder.setVlanMatch(vlanMatch.build());
        }
        if (ipMatch != null) {
            matchBuilder.setIpMatch(ipMatch.build());
        }

        if (tcpMatch != null) {
            matchBuilder.setLayer4Match(tcpMatch.build());
        } else if (udpMatch != null) {
            matchBuilder.setLayer4Match(udpMatch.build());
        } else if (sctpMatch != null) {
            matchBuilder.setLayer4Match(sctpMatch.build());
        }

        if (icmpv4Match != null) {
            matchBuilder.setIcmpv4Match(icmpv4Match.build());
        } else if (icmpv6Match != null) {
            matchBuilder.setIcmpv6Match(icmpv6Match.build());
        }

        if (ipv4Match != null) {
            matchBuilder.setLayer3Match(ipv4Match.build());
        } else if (arpMatch != null) {
            matchBuilder.setLayer3Match(arpMatch.build());
        } else if (ipv6Match != null) {
            matchBuilder.setLayer3Match(ipv6Match.build());
        }
        if (protocolMatchFields != null) {
            matchBuilder.setProtocolMatchFields(protocolMatchFields.build());
        }
        return matchBuilder.build();
    }

    /**
     * Fills the bitmask from boolean map where key is bit position
     *
     * @param booleanMap
     *            bit to boolean mapping
     * @return bit mask
     */
    private int fillBitMaskFromMap(Map<Integer, Boolean> booleanMap) {
        int bitmask = 0;

        for (Entry<Integer, Boolean> iterator : booleanMap.entrySet()) {
            if (iterator.getValue() != null && iterator.getValue().booleanValue()) {
                bitmask |= 1 << iterator.getKey();
            }
        }
        return bitmask;
    }

    private Ipv4Prefix toIpv4Prefix(MatchEntries entry) {
        String ipv4Prefix = entry.getAugmentation(Ipv4AddressMatchEntry.class).getIpv4Address().toString();
        if (entry.isHasMask()) {
            byte[] mask = entry.getAugmentation(MaskMatchEntry.class).getMask();
            ipv4Prefix = ipv4Prefix + PREFIX_SEPARATOR + countBits(mask);
        }
        return new Ipv4Prefix(ipv4Prefix);
    }

    private Ipv6Prefix toIpv6Prefix(MatchEntries entry) {
        String ipv6Prefix = entry.getAugmentation(Ipv6AddressMatchEntry.class).getIpv6Address().toString();
        if (entry.isHasMask()) {
            byte[] mask = entry.getAugmentation(MaskMatchEntry.class).getMask();
            ipv6Prefix = ipv6Prefix + PREFIX_SEPARATOR + countBits(mask);
        }
        return new Ipv6Prefix(ipv6Prefix);
    }

    private int toInt(byte b) {
        return b < 0 ? b + 256 : b;
    }

    private int countBits(byte[] mask) {
        int netmask = 0;
        for (byte b : mask) {
            netmask += Integer.bitCount(toInt(b));
        }
        return netmask;
    }
}
