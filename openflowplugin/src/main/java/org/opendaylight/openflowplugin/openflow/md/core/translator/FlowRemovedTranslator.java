/**
 * Copyright (c) 2014 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.translator;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.core.IMDMessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.AugmentTuple;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.openflow.md.core.extension.MatchExtensionHelper;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.common.IpConversionUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;
import org.opendaylight.openflowplugin.openflow.md.util.ByteUtil;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv6FlowLabel;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SwitchFlowRemovedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.RemovedFlowReason;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelIpv4Dst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TunnelIpv4Src;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowRemovedTranslator implements IMDMessageTranslator<OfHeader, List<DataObject>> {

    private static final Logger LOG = LoggerFactory.getLogger(FlowRemovedTranslator.class);
    private static final String PREFIX_SEPARATOR = "/";
    private final ConvertorExecutor convertorExecutor;

    public FlowRemovedTranslator(ConvertorExecutor convertorExecutor) {
        this.convertorExecutor = convertorExecutor;
    }

    @Override
    public List<DataObject> translate(final SwitchConnectionDistinguisher cookie, final SessionContext sc, final OfHeader msg) {
        if (msg instanceof FlowRemovedMessage) {
            FlowRemovedMessage ofFlow = (FlowRemovedMessage) msg;
            List<DataObject> list = new CopyOnWriteArrayList<DataObject>();
            LOG.debug("Flow Removed Message received: Table Id={}, Flow removed reason={} ", ofFlow.getTableId(),
                    ofFlow.getReason());

            SwitchFlowRemovedBuilder salFlowRemoved = new SwitchFlowRemovedBuilder();

            if (ofFlow.getCookie() != null) {
                salFlowRemoved.setCookie(new FlowCookie(ofFlow.getCookie()));
            }
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

            if(Objects.nonNull(ofFlow.getReason())) {
                salFlowRemoved.setRemovedReason(translateReason(ofFlow));
            }

            OpenflowVersion ofVersion = OpenflowVersion.get(sc.getPrimaryConductor().getVersion());
            org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match ofMatch = ofFlow
                    .getMatch();
            if (ofMatch != null) {
                salFlowRemoved.setMatch(fromMatch(ofMatch, sc.getFeatures().getDatapathId(), ofVersion));
            } else if (ofFlow.getMatchV10() != null) {
                final VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(sc.getPrimaryConductor().getVersion());
                data.setDatapathId(sc.getFeatures().getDatapathId());

                final Optional<MatchBuilder> matchBuilderOptional = convertorExecutor.convert(ofFlow.getMatchV10(), data);
                salFlowRemoved.setMatch(matchBuilderOptional.orElse(new MatchBuilder()).build());
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
    private RemovedFlowReason translateReason(FlowRemoved removedFlow) {
        LOG.debug("--Entering translateReason within FlowRemovedTranslator with reason:{} " + removedFlow.getReason());
        switch (removedFlow.getReason()) {
            case OFPRRIDLETIMEOUT:
                return RemovedFlowReason.OFPRRIDLETIMEOUT;
            case OFPRRHARDTIMEOUT:
                return RemovedFlowReason.OFPRRHARDTIMEOUT;
            case OFPRRDELETE:
                return RemovedFlowReason.OFPRRDELETE;
            case OFPRRGROUPDELETE:
                return RemovedFlowReason.OFPRRGROUPDELETE;
            default:
                LOG.debug("The flow being default and hence deleting it ");
                return RemovedFlowReason.OFPRRDELETE;
        }
    }

    public Match fromMatch(final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.grouping.Match ofMatch,
                           final BigInteger datapathid, final OpenflowVersion ofVersion) {
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

        for (MatchEntry entry : ofMatch.getMatchEntry()) {
            Class<? extends MatchField> field = entry.getOxmMatchField();
            if (field.equals(InPort.class)) {
                matchBuilder.setInPort(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(datapathid,
                        ((InPortCase) entry.getMatchEntryValue()).getInPort().getPortNumber().getValue(), ofVersion));
            } else if (field.equals(InPhyPort.class)) {
                matchBuilder.setInPhyPort(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(datapathid,
                        ((InPhyPortCase) entry.getMatchEntryValue()).getInPhyPort().getPortNumber().getValue(), ofVersion));
            } else if (field.equals(Metadata.class)) {
                MetadataBuilder metadata = new MetadataBuilder();
                MetadataCase metadataCase = ((MetadataCase) entry.getMatchEntryValue());
                metadata.setMetadata(new BigInteger(OFConstants.SIGNUM_UNSIGNED, metadataCase.getMetadata().getMetadata()));
                if (entry.isHasMask()) {
                    metadata.setMetadataMask(new BigInteger(OFConstants.SIGNUM_UNSIGNED, metadataCase.getMetadata().getMask()));
                }
                matchBuilder.setMetadata(metadata.build());
            } else if (field.equals(EthDst.class) || field.equals(EthSrc.class) || field.equals(EthType.class)) {
                if (ethernetMatch == null) {
                    ethernetMatch = new EthernetMatchBuilder();
                }
                if (field.equals(EthDst.class)) {
                    EthernetDestinationBuilder ethDst = new EthernetDestinationBuilder();
                    EthDstCase ethDstCase = (EthDstCase) entry.getMatchEntryValue();
                    ethDst.setAddress(ethDstCase.getEthDst().getMacAddress());
                    if (entry.isHasMask()) {
                        ethDst.setMask(new MacAddress(ByteUtil.bytesToHexstring(ethDstCase.getEthDst().getMask(), ":")));
                    }
                    ethernetMatch.setEthernetDestination(ethDst.build());
                } else if (field.equals(EthSrc.class)) {
                    EthernetSourceBuilder ethSrc = new EthernetSourceBuilder();
                    EthSrcCase ethSrcCase = ((EthSrcCase) entry.getMatchEntryValue());
                    ethSrc.setAddress(ethSrcCase.getEthSrc().getMacAddress());
                    if (entry.isHasMask()) {
                        ethSrc.setMask(new MacAddress(ByteUtil.bytesToHexstring(ethSrcCase.getEthSrc().getMask(), ":")));
                    }
                    ethernetMatch.setEthernetSource(ethSrc.build());
                } else if (field.equals(EthType.class)) {
                    EthernetTypeBuilder ethType = new EthernetTypeBuilder();
                    EthTypeCase ethTypeCase = ((EthTypeCase) entry.getMatchEntryValue());
                    ethType.setType(new EtherType(ethTypeCase.getEthType().getEthType().getValue().longValue()));
                    ethernetMatch.setEthernetType(ethType.build());
                }
            } else if (field.equals(VlanVid.class) || field.equals(VlanPcp.class)) {
                if (vlanMatch == null) {
                    vlanMatch = new VlanMatchBuilder();
                }
                if (field.equals(VlanVid.class)) {
                    boolean vlanIdPresent = false;
                    VlanIdBuilder vlanId = new VlanIdBuilder();
                    VlanVidCase vlanVidCase = ((VlanVidCase) entry.getMatchEntryValue());
                    Integer vlanVidValue = vlanVidCase.getVlanVid().getVlanVid();
                    if (vlanVidCase.getVlanVid().isCfiBit()) {
                        vlanIdPresent = true;
                    }
                    vlanId.setVlanIdPresent(vlanIdPresent);
                    if (vlanVidValue != null) {
                        vlanId.setVlanId(new VlanId(vlanVidValue));
                    }
                    vlanMatch.setVlanId(vlanId.build());
                } else if (field.equals(VlanPcp.class)) {
                    VlanPcpCase vlanPcpCase = ((VlanPcpCase) entry.getMatchEntryValue());
                    vlanMatch.setVlanPcp(new org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.VlanPcp(vlanPcpCase.getVlanPcp().getVlanPcp()));
                }
            } else if (field.equals(IpDscp.class) || field.equals(IpEcn.class) || field.equals(IpProto.class)) {
                if (ipMatch == null) {
                    ipMatch = new IpMatchBuilder();
                }
                if (field.equals(IpDscp.class)) {
                    IpDscpCase ipDscpCase = ((IpDscpCase) entry.getMatchEntryValue());
                    ipMatch.setIpDscp(new org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Dscp(
                            ipDscpCase.getIpDscp().getDscp()));
                } else if (field.equals(IpEcn.class)) {
                    IpEcnCase ipEcnCase = ((IpEcnCase) entry.getMatchEntryValue());
                    ipMatch.setIpEcn(ipEcnCase.getIpEcn().getEcn());
                } else if (field.equals(IpProto.class)) {
                    IpProtoCase ipProtoCase = ((IpProtoCase) entry.getMatchEntryValue());
                    ipMatch.setIpProtocol(ipProtoCase.getIpProto().getProtocolNumber());
                }
            } else if (field.equals(TcpSrc.class) || field.equals(TcpDst.class)) {
                if (tcpMatch == null) {
                    tcpMatch = new TcpMatchBuilder();
                }
                if (field.equals(TcpSrc.class)) {
                    TcpSrcCase tcpSrcCase = ((TcpSrcCase) entry.getMatchEntryValue());
                    tcpMatch.setTcpSourcePort(new PortNumber(tcpSrcCase.getTcpSrc().getPort().getValue()));
                } else if (field.equals(TcpDst.class)) {
                    TcpDstCase tcpDstCase = ((TcpDstCase) entry.getMatchEntryValue());
                    tcpMatch.setTcpDestinationPort(new PortNumber(tcpDstCase.getTcpDst().getPort().getValue()));
                }
            } else if (field.equals(UdpSrc.class) || field.equals(UdpDst.class)) {
                if (udpMatch == null) {
                    udpMatch = new UdpMatchBuilder();
                }
                if (field.equals(UdpSrc.class)) {
                    UdpSrcCase udpSrcCase = ((UdpSrcCase) entry.getMatchEntryValue());
                    udpMatch.setUdpSourcePort(new PortNumber(udpSrcCase.getUdpSrc().getPort().getValue()));
                } else if (field.equals(UdpDst.class)) {
                    UdpDstCase udpDstCase = ((UdpDstCase) entry.getMatchEntryValue());
                    udpMatch.setUdpDestinationPort(new PortNumber(udpDstCase.getUdpDst().getPort()));
                }
            } else if (field.equals(SctpSrc.class) || field.equals(SctpDst.class)) {
                if (sctpMatch == null) {
                    sctpMatch = new SctpMatchBuilder();
                }
                if (field.equals(SctpSrc.class)) {
                    SctpSrcCase sctpSrcCase = ((SctpSrcCase) entry.getMatchEntryValue());
                    sctpMatch.setSctpSourcePort(new PortNumber(sctpSrcCase.getSctpSrc().getPort()));
                } else if (field.equals(SctpDst.class)) {
                    SctpDstCase sctpDstCase = ((SctpDstCase) entry.getMatchEntryValue());
                    sctpMatch.setSctpDestinationPort(new PortNumber(sctpDstCase.getSctpDst().getPort()));
                }
            } else if (field.equals(Icmpv4Type.class) || field.equals(Icmpv4Code.class)) {
                if (icmpv4Match == null) {
                    icmpv4Match = new Icmpv4MatchBuilder();
                }
                if (field.equals(Icmpv4Type.class)) {
                    Icmpv4TypeCase icmpv4TypeCase = ((Icmpv4TypeCase) entry.getMatchEntryValue());
                    icmpv4Match.setIcmpv4Type(icmpv4TypeCase.getIcmpv4Type().getIcmpv4Type());
                } else if (field.equals(Icmpv4Code.class)) {
                    Icmpv4CodeCase icmpv4CodeCase = ((Icmpv4CodeCase) entry.getMatchEntryValue());
                    icmpv4Match.setIcmpv4Code(icmpv4CodeCase.getIcmpv4Code().getIcmpv4Code());
                }
            } else if (field.equals(Icmpv6Type.class) || field.equals(Icmpv6Code.class)) {
                if (icmpv6Match == null) {
                    icmpv6Match = new Icmpv6MatchBuilder();
                }
                if (field.equals(Icmpv6Type.class)) {
                    Icmpv6TypeCase icmpv6TypeCase = ((Icmpv6TypeCase) entry.getMatchEntryValue());
                    icmpv6Match.setIcmpv6Type(icmpv6TypeCase.getIcmpv6Type().getIcmpv6Type());
                } else if (field.equals(Icmpv6Code.class)) {
                    Icmpv6CodeCase icmpv6CodeCase = ((Icmpv6CodeCase) entry.getMatchEntryValue());
                    icmpv6Match.setIcmpv6Code(icmpv6CodeCase.getIcmpv6Code().getIcmpv6Code());
                }
            } else if (field.equals(Ipv4Src.class) || field.equals(Ipv4Dst.class)) {
                if (ipv4Match == null) {
                    ipv4Match = new Ipv4MatchBuilder();
                }
                if (field.equals(Ipv4Src.class)) {
                    Ipv4SrcCase ipv4SrcCase = ((Ipv4SrcCase) entry.getMatchEntryValue());
                    int prefix;
                    if (entry.isHasMask()) {
                        prefix = IpConversionUtil.countBits(ipv4SrcCase.getIpv4Src().getMask());
                    } else {
                        prefix = 32;
                    }
                    ipv4Match.setIpv4Source(
                        IpConversionUtil.createPrefix(ipv4SrcCase.getIpv4Src().getIpv4Address(), prefix)
                    );

                } else if (field.equals(Ipv4Dst.class)) {
                    Ipv4DstCase ipv4DstCase = ((Ipv4DstCase) entry.getMatchEntryValue());
                    int prefix;
                    if (entry.isHasMask()) {
                        prefix = IpConversionUtil.countBits(ipv4DstCase.getIpv4Dst().getMask());
                    } else {
                        prefix = 32;
                    }
                    ipv4Match.setIpv4Destination(
                        IpConversionUtil.createPrefix(ipv4DstCase.getIpv4Dst().getIpv4Address(), prefix)
                    );
                }
            } else if (field.equals(ArpOp.class) || field.equals(ArpSpa.class) || field.equals(ArpTpa.class)
                    || field.equals(ArpSha.class) || field.equals(ArpTha.class)) {
                if (arpMatch == null) {
                    arpMatch = new ArpMatchBuilder();
                }
                if (field.equals(ArpOp.class)) {
                    ArpOpCase arpOpCase = ((ArpOpCase) entry.getMatchEntryValue());
                    arpMatch.setArpOp(arpOpCase.getArpOp().getOpCode());
                } else if (field.equals(ArpSpa.class)) {

                    ArpSpaCase arpSpaCase = ((ArpSpaCase) entry.getMatchEntryValue());
                    int prefix;
                    if (entry.isHasMask()) {
                        prefix = IpConversionUtil.countBits(arpSpaCase.getArpSpa().getMask());
                    } else {
                        prefix = 32;
                    }
                    arpMatch.setArpSourceTransportAddress(
                        IpConversionUtil.createPrefix(arpSpaCase.getArpSpa().getIpv4Address(), prefix)
                    );
                } else if (field.equals(ArpTpa.class)) {
                    ArpTpaCase arpTpaCase = ((ArpTpaCase) entry.getMatchEntryValue());
                    int prefix;
                    if (entry.isHasMask()) {
                        prefix = IpConversionUtil.countBits(arpTpaCase.getArpTpa().getMask());
                    } else {
                        prefix = 32;
                    }

                    arpMatch.setArpTargetTransportAddress(
                        IpConversionUtil.createPrefix(arpTpaCase.getArpTpa().getIpv4Address(), prefix)
                    );
                } else if (field.equals(ArpSha.class)) {
                    ArpSourceHardwareAddressBuilder arpSha = new ArpSourceHardwareAddressBuilder();
                    ArpShaCase arpShaCase = ((ArpShaCase) entry.getMatchEntryValue());
                    arpSha.setAddress(arpShaCase.getArpSha().getMacAddress());
                    if (entry.isHasMask()) {
                        arpSha.setMask(new MacAddress(ByteUtil.bytesToHexstring(arpShaCase.getArpSha().getMask(), ":")));
                    }
                    arpMatch.setArpSourceHardwareAddress(arpSha.build());
                } else if (field.equals(ArpTha.class)) {
                    ArpThaCase arpThaCase = ((ArpThaCase) entry.getMatchEntryValue());
                    ArpTargetHardwareAddressBuilder arpTha = new ArpTargetHardwareAddressBuilder();
                    arpTha.setAddress(arpThaCase.getArpTha().getMacAddress());
                    if (entry.isHasMask()) {
                        arpTha.setMask(new MacAddress(ByteUtil.bytesToHexstring(arpThaCase.getArpTha().getMask(), ":")));
                    }
                    arpMatch.setArpTargetHardwareAddress(arpTha.build());
                }
            } else if (field.equals(TunnelIpv4Src.class) || field.equals(TunnelIpv4Dst.class)) {
                if (ipv4Match == null) {
                    ipv4Match = new Ipv4MatchBuilder();
                }
                if (field.equals(TunnelIpv4Src.class)) {
                    Ipv4SrcCase ipv4SrcCase = ((Ipv4SrcCase) entry.getMatchEntryValue());
                    int prefix;
                    if (entry.isHasMask()) {
                        prefix = IpConversionUtil.countBits(ipv4SrcCase.getIpv4Src().getMask());
                    } else {
                        prefix = 32;
                    }

                    ipv4Match.setIpv4Source(
                        IpConversionUtil.createPrefix(ipv4SrcCase.getIpv4Src().getIpv4Address(), prefix)
                    );
                } else if (field.equals(TunnelIpv4Dst.class)) {
                    Ipv4DstCase ipv4DstCase = ((Ipv4DstCase) entry.getMatchEntryValue());
                    int prefix;
                    if (entry.isHasMask()) {
                        prefix = IpConversionUtil.countBits(ipv4DstCase.getIpv4Dst().getMask());
                    } else {
                        prefix = 32;
                    }

                    ipv4Match.setIpv4Destination(
                        IpConversionUtil.createPrefix(ipv4DstCase.getIpv4Dst().getIpv4Address(), prefix)
                    );               }
            } else if (field.equals(Ipv6Src.class) || field.equals(Ipv6Dst.class) || field.equals(Ipv6Flabel.class)
                    || field.equals(Ipv6NdTarget.class) || field.equals(Ipv6NdSll.class)
                    || field.equals(Ipv6NdTll.class) || field.equals(Ipv6Exthdr.class)) {
                if (ipv6Match == null) {
                    ipv6Match = new Ipv6MatchBuilder();
                }
                if (field.equals(Ipv6Src.class)) {
                    Ipv6SrcCase ipv6SrcCase = ((Ipv6SrcCase) entry.getMatchEntryValue());
                    int prefix ;
                    if (entry.isHasMask()) {
                        prefix = IpConversionUtil.countBits(ipv6SrcCase.getIpv6Src().getMask());
                    } else {
                        prefix = 128;
                    }
                    ipv6Match.setIpv6Source(
                        IpConversionUtil.createPrefix(ipv6SrcCase.getIpv6Src().getIpv6Address(), prefix)
                    );
                } else if (field.equals(Ipv6Dst.class)) {
                    Ipv6DstCase ipv6DstCase = ((Ipv6DstCase) entry.getMatchEntryValue());
                    int prefix;
                    if (entry.isHasMask()) {
                        prefix = IpConversionUtil.countBits(ipv6DstCase.getIpv6Dst().getMask());
                    } else {
                        prefix = 128;
                    }
                    ipv6Match.setIpv6Destination(
                        IpConversionUtil.createPrefix(ipv6DstCase.getIpv6Dst().getIpv6Address(), prefix)
                    );
                } else if (field.equals(Ipv6Flabel.class)) {
                    Ipv6LabelBuilder ipv6Label = new Ipv6LabelBuilder();
                    Ipv6FlabelCase ipv6FlabelCase = ((Ipv6FlabelCase) entry.getMatchEntryValue());
                    ipv6Label.setIpv6Flabel(ipv6FlabelCase.getIpv6Flabel().getIpv6Flabel());
                    if (entry.isHasMask()) {
                        ipv6Label.setFlabelMask(new Ipv6FlowLabel(ByteUtil.bytesToUnsignedInt(ipv6FlabelCase.getIpv6Flabel().getMask())));
                    }
                    ipv6Match.setIpv6Label(ipv6Label.build());
                } else if (field.equals(Ipv6NdTarget.class)) {
                    Ipv6NdTargetCase ipv6NdTargetCase = ((Ipv6NdTargetCase) entry.getMatchEntryValue());
                    ipv6Match.setIpv6NdTarget(ipv6NdTargetCase.getIpv6NdTarget().getIpv6Address());
                } else if (field.equals(Ipv6NdSll.class)) {
                    Ipv6NdSllCase ipv6NdSllCase = ((Ipv6NdSllCase) entry.getMatchEntryValue());
                    ipv6Match.setIpv6NdSll(ipv6NdSllCase.getIpv6NdSll().getMacAddress());
                } else if (field.equals(Ipv6NdTll.class)) {
                    Ipv6NdTllCase ipv6NdTllCase = ((Ipv6NdTllCase) entry.getMatchEntryValue());
                    ipv6Match.setIpv6NdTll(ipv6NdTllCase.getIpv6NdTll().getMacAddress());
                } else if (field.equals(Ipv6Exthdr.class)) {
                    Ipv6ExthdrCase ipv6ExthdrCase = ((Ipv6ExthdrCase) entry.getMatchEntryValue());
                    // verify
                    Ipv6ExtHeaderBuilder ipv6ExtHeaderBuilder = new Ipv6ExtHeaderBuilder();
                    Ipv6ExthdrFlags pseudoField = ipv6ExthdrCase.getIpv6Exthdr().getPseudoField();
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
                    int bitmap = ByteBufUtils.fillBitMaskFromMap(map);
                    ipv6ExtHeaderBuilder.setIpv6Exthdr(bitmap);
                    if (entry.isHasMask()) {
                        ipv6ExtHeaderBuilder.setIpv6ExthdrMask(
                                ByteUtil.bytesToUnsignedShort(ipv6ExthdrCase.getIpv6Exthdr().getMask()));
                    }
                    ipv6Match.setIpv6ExtHeader(ipv6ExtHeaderBuilder.build());
                }
            } else if (field.equals(MplsLabel.class) || field.equals(MplsTc.class) || field.equals(MplsBos.class)
                    || field.equals(PbbIsid.class)) {
                if (protocolMatchFields == null) {
                    protocolMatchFields = new ProtocolMatchFieldsBuilder();
                }
                if (field.equals(MplsLabel.class)) {
                    MplsLabelCase mplsLabelCase = ((MplsLabelCase) entry.getMatchEntryValue());
                    protocolMatchFields.setMplsLabel(mplsLabelCase.getMplsLabel().getMplsLabel());
                } else if (field.equals(MplsTc.class)) {
                    MplsTcCase mplsTcCase = ((MplsTcCase) entry.getMatchEntryValue());
                    protocolMatchFields.setMplsTc(mplsTcCase.getMplsTc().getTc());
                } else if (field.equals(MplsBos.class)) {
                    MplsBosCase mplsBosCase = ((MplsBosCase) entry.getMatchEntryValue());
                    protocolMatchFields
                            .setMplsBos((short) (mplsBosCase.getMplsBos().isBos() ? 1 : 0));
                } else if (field.equals(PbbIsid.class)) {
                    PbbIsidCase pbbIsidCase = ((PbbIsidCase) entry.getMatchEntryValue());
                    PbbBuilder pbb = new PbbBuilder();
                    pbb.setPbbIsid(pbbIsidCase.getPbbIsid().getIsid());
                    if (entry.isHasMask()) {
                        pbb.setPbbMask(ByteUtil.bytesToUnsignedInt(pbbIsidCase.getPbbIsid().getMask()));
                    }
                    protocolMatchFields.setPbb(pbb.build());
                }
            } else if (field.equals(TunnelId.class)) {
                TunnelIdCase tunnelIdCase = ((TunnelIdCase) entry.getMatchEntryValue());
                TunnelBuilder tunnel = new TunnelBuilder();
                tunnel.setTunnelId(new BigInteger(OFConstants.SIGNUM_UNSIGNED, tunnelIdCase.getTunnelId().getTunnelId()));
                if (entry.isHasMask()) {
                    tunnel.setTunnelMask(new BigInteger(OFConstants.SIGNUM_UNSIGNED, tunnelIdCase.getTunnelId().getMask()));
                }
                matchBuilder.setTunnel(tunnel.build());
            }
        }

        AugmentTuple<Match> matchExtensionWrap =
                MatchExtensionHelper.processAllExtensions(
                        ofMatch.getMatchEntry(), ofVersion, MatchPath.SWITCHFLOWREMOVED_MATCH);
        if (matchExtensionWrap != null) {
            matchBuilder.addAugmentation(matchExtensionWrap.getAugmentationClass(), matchExtensionWrap.getAugmentationObject());
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

}
