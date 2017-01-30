/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.impl.util.GroupUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.FlowStatsResponseConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.AggregateFlowStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.QueueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupDescStatsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupFeaturesUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupStatisticsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Chaining;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.ChainingChecks;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupAll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupDescStatsReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupFeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupFf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupIndirect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupSelect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupStatisticsReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.SelectLiveness;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.SelectWeight;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.desc.stats.reply.GroupDescStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterConfigStatsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterFeaturesUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterStatisticsUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandDrop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandDscpRemark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBurst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterConfigStatsReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterFeaturesReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterKbps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterPktps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterStatisticsReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.config.stats.reply.MeterConfigStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.AggregateFlowStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.duration.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.BytesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.PacketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterConfigCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.aggregate._case.MultipartReplyAggregate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.flow._case.MultipartReplyFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group._case.MultipartReplyGroup;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.desc._case.MultipartReplyGroupDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.features._case.MultipartReplyGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.MultipartReplyMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.MultipartReplyMeterConfig;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.features._case.MultipartReplyMeterFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.MultipartReplyPortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.port.stats._case.multipart.reply.port.stats.PortStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.MultipartReplyQueue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.queue._case.multipart.reply.queue.QueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.MultipartReplyTable;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table._case.multipart.reply.table.TableStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.NodeConnectorStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.QueueStatisticsUpdateBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId;
import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * Class converts multipart reply messages to the notification objects defined
 * by statistics provider (manager ). It is ment to be replaced by translators
 * and to be used for translating statistics data only.
 *
 * @author avishnoi@in.ibm.com
 */
@Deprecated
public class SinglePurposeMultipartReplyTranslator {
    public static Optional<? extends DataContainer> translate(final DeviceInfo deviceInfo,
                                                              final DataContainer dataContainer,
                                                              @Nullable final ConvertorExecutor convertorExecutor) {
        if (dataContainer instanceof MultipartReply) {
            final Optional<ConvertorExecutor> convertor = Optional.ofNullable(convertorExecutor);
            final MultipartReply msg = MultipartReply.class.cast(dataContainer);
            final NodeId node = nodeIdFromDatapathId(deviceInfo.getDatapathId());
            final OpenflowVersion ofVersion = OpenflowVersion.get(deviceInfo.getVersion());
            final VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(deviceInfo.getVersion());
            data.setDatapathId(deviceInfo.getDatapathId());

            switch (msg.getType()) {
                case OFPMPFLOW:
                    return convertor.flatMap(c -> Optional.of(translateFlow(msg, node, data, c)));
                case OFPMPAGGREGATE:
                    return Optional.of(translateAggregate(msg, node));
                case OFPMPPORTSTATS:
                    return Optional.of(translatePortStats(msg, node, ofVersion, deviceInfo.getDatapathId()));
                case OFPMPGROUP:
                    return convertor.flatMap(c -> Optional.of(translateGroup(msg, node, data, c)));
                case OFPMPGROUPDESC:
                    return convertor.flatMap(c -> Optional.of(translateGroupDesc(msg, node, data, c)));
                case OFPMPGROUPFEATURES:
                    return Optional.of(translateGroupFeatures(msg, node));
                case OFPMPMETER:
                    return convertor.flatMap(c -> Optional.of(translateMeter(msg, node, data, c)));
                case OFPMPMETERCONFIG:
                    return convertor.flatMap(c -> Optional.of(translateMeterConfig(msg, node, data, c)));
                case OFPMPMETERFEATURES:
                    return Optional.of(translateMeterFeatures(msg, node));
                case OFPMPTABLE:
                    return Optional.of(translateTable(msg, node));
                case OFPMPQUEUE:
                    return Optional.of(translateQueue(msg, node, ofVersion, deviceInfo.getDatapathId()));
            }
        } else if (dataContainer instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112
            .MultipartReply) {
            return Optional.of(org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112
                .MultipartReply.class.cast(dataContainer).getMultipartReplyBody());
        }

        return Optional.empty();
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819
        .FlowAndStatisticsMapList translateFlow(final MultipartReply msg,
                                                final NodeId node,
                                                final VersionDatapathIdConvertorData data,
                                                final ConvertorExecutor convertorExecutor) {
        FlowStatsResponseConvertorData flowData = new FlowStatsResponseConvertorData(data.getVersion());
        flowData.setDatapathId(data.getDatapathId());
        flowData.setMatchPath(MatchPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH);
        FlowsStatisticsUpdateBuilder message = new FlowsStatisticsUpdateBuilder();
        message.setId(node);
        message.setMoreReplies(msg.getFlags().isOFPMPFREQMORE());
        message.setTransactionId(generateTransactionId(msg.getXid()));
        MultipartReplyFlowCase caseBody = (MultipartReplyFlowCase) msg.getMultipartReplyBody();
        MultipartReplyFlow replyBody = caseBody.getMultipartReplyFlow();
        final Optional<List<FlowAndStatisticsMapList>> flowAndStatisticsMapLists =
            convertorExecutor.convert(replyBody.getFlowStats(), flowData);

        message.setFlowAndStatisticsMapList(flowAndStatisticsMapLists.orElse(Collections.emptyList()));
        return message.build();
    }

    private static AggregateFlowStatistics translateAggregate(final MultipartReply msg,
                                                       final NodeId node) {
        AggregateFlowStatisticsUpdateBuilder message = new AggregateFlowStatisticsUpdateBuilder();
        message.setId(node);
        message.setMoreReplies(msg.getFlags().isOFPMPFREQMORE());
        message.setTransactionId(generateTransactionId(msg.getXid()));

        MultipartReplyAggregateCase caseBody = (MultipartReplyAggregateCase) msg.getMultipartReplyBody();
        MultipartReplyAggregate replyBody = caseBody.getMultipartReplyAggregate();
        message.setByteCount(new Counter64(replyBody.getByteCount()));
        message.setPacketCount(new Counter64(replyBody.getPacketCount()));
        message.setFlowCount(new Counter32(replyBody.getFlowCount()));
        return message.build();
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214
        .NodeConnectorStatisticsAndPortNumberMap translatePortStats(final MultipartReply msg,
                                                                    final NodeId node,
                                                                    final OpenflowVersion ofVersion,
                                                                    final BigInteger datapathId) {
        NodeConnectorStatisticsUpdateBuilder message = new NodeConnectorStatisticsUpdateBuilder();
        message.setId(node);
        message.setMoreReplies(msg.getFlags().isOFPMPFREQMORE());
        message.setTransactionId(generateTransactionId(msg.getXid()));

        MultipartReplyPortStatsCase caseBody = (MultipartReplyPortStatsCase) msg.getMultipartReplyBody();
        MultipartReplyPortStats replyBody = caseBody.getMultipartReplyPortStats();

        List<NodeConnectorStatisticsAndPortNumberMap> statsMap =
                new ArrayList<>();
        for (PortStats portStats : replyBody.getPortStats()) {

            NodeConnectorStatisticsAndPortNumberMapBuilder statsBuilder =
                    new NodeConnectorStatisticsAndPortNumberMapBuilder();
            statsBuilder.setNodeConnectorId(
                    InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(datapathId,
                            portStats.getPortNo(), ofVersion));

            BytesBuilder bytesBuilder = new BytesBuilder();
            bytesBuilder.setReceived(portStats.getRxBytes());
            bytesBuilder.setTransmitted(portStats.getTxBytes());
            statsBuilder.setBytes(bytesBuilder.build());

            PacketsBuilder packetsBuilder = new PacketsBuilder();
            packetsBuilder.setReceived(portStats.getRxPackets());
            packetsBuilder.setTransmitted(portStats.getTxPackets());
            statsBuilder.setPackets(packetsBuilder.build());

            DurationBuilder durationBuilder = new DurationBuilder();
            if (portStats.getDurationSec() != null) {
                durationBuilder.setSecond(new Counter32(portStats.getDurationSec()));
            }
            if (portStats.getDurationNsec() != null) {
                durationBuilder.setNanosecond(new Counter32(portStats.getDurationNsec()));
            }
            statsBuilder.setDuration(durationBuilder.build());
            statsBuilder.setCollisionCount(portStats.getCollisions());
            statsBuilder.setKey(new NodeConnectorStatisticsAndPortNumberMapKey(statsBuilder.getNodeConnectorId()));
            statsBuilder.setReceiveCrcError(portStats.getRxCrcErr());
            statsBuilder.setReceiveDrops(portStats.getRxDropped());
            statsBuilder.setReceiveErrors(portStats.getRxErrors());
            statsBuilder.setReceiveFrameError(portStats.getRxFrameErr());
            statsBuilder.setReceiveOverRunError(portStats.getRxOverErr());
            statsBuilder.setTransmitDrops(portStats.getTxDropped());
            statsBuilder.setTransmitErrors(portStats.getTxErrors());

            statsMap.add(statsBuilder.build());
        }
        message.setNodeConnectorStatisticsAndPortNumberMap(statsMap);


        return message.build();
    }

    private static GroupStatisticsReply translateGroup(final MultipartReply msg,
                                                final NodeId node,
                                                final VersionDatapathIdConvertorData data,
                                                final ConvertorExecutor convertorExecutor) {
        GroupStatisticsUpdatedBuilder message = new GroupStatisticsUpdatedBuilder();
        message.setId(node);
        message.setMoreReplies(msg.getFlags().isOFPMPFREQMORE());
        message.setTransactionId(generateTransactionId(msg.getXid()));
        MultipartReplyGroupCase caseBody = (MultipartReplyGroupCase) msg.getMultipartReplyBody();
        MultipartReplyGroup replyBody = caseBody.getMultipartReplyGroup();
        final Optional<List<GroupStats>> groupStatsList = convertorExecutor.convert(
                replyBody.getGroupStats(), data);

        message.setGroupStats(groupStatsList.orElse(Collections.emptyList()));

        return message.build();
    }

    private static GroupDescStatsReply translateGroupDesc(final MultipartReply msg,
                                                   final NodeId node,
                                                   final VersionDatapathIdConvertorData data,
                                                   final ConvertorExecutor convertorExecutor) {
        GroupDescStatsUpdatedBuilder message = new GroupDescStatsUpdatedBuilder();
        message.setId(node);
        message.setMoreReplies(msg.getFlags().isOFPMPFREQMORE());
        message.setTransactionId(generateTransactionId(msg.getXid()));
        MultipartReplyGroupDescCase caseBody = (MultipartReplyGroupDescCase) msg.getMultipartReplyBody();
        MultipartReplyGroupDesc replyBody = caseBody.getMultipartReplyGroupDesc();

        final Optional<List<GroupDescStats>> groupDescStatsList = convertorExecutor.convert(
                replyBody.getGroupDesc(), data);

        message.setGroupDescStats(groupDescStatsList.orElse(Collections.emptyList()));

        return message.build();
    }

    private static GroupFeaturesReply translateGroupFeatures(final MultipartReply msg,
                                                      final NodeId node) {
        GroupFeaturesUpdatedBuilder message = new GroupFeaturesUpdatedBuilder();
        message.setId(node);
        message.setMoreReplies(msg.getFlags().isOFPMPFREQMORE());
        message.setTransactionId(generateTransactionId(msg.getXid()));
        MultipartReplyGroupFeaturesCase caseBody = (MultipartReplyGroupFeaturesCase) msg.getMultipartReplyBody();
        MultipartReplyGroupFeatures replyBody = caseBody.getMultipartReplyGroupFeatures();
        List<Class<? extends GroupType>> supportedGroups =
                new ArrayList<>();

        if (replyBody.getTypes().isOFPGTALL()) {
            supportedGroups.add(GroupAll.class);
        }
        if (replyBody.getTypes().isOFPGTSELECT()) {
            supportedGroups.add(GroupSelect.class);
        }
        if (replyBody.getTypes().isOFPGTINDIRECT()) {
            supportedGroups.add(GroupIndirect.class);
        }
        if (replyBody.getTypes().isOFPGTFF()) {
            supportedGroups.add(GroupFf.class);
        }
        message.setGroupTypesSupported(supportedGroups);
        message.setMaxGroups(replyBody.getMaxGroups());

        List<Class<? extends GroupCapability>> supportedCapabilities =
                new ArrayList<>();

        if (replyBody.getCapabilities().isOFPGFCCHAINING()) {
            supportedCapabilities.add(Chaining.class);
        }
        if (replyBody.getCapabilities().isOFPGFCCHAININGCHECKS()) {
            supportedCapabilities.add(ChainingChecks.class);
        }
        if (replyBody.getCapabilities().isOFPGFCSELECTLIVENESS()) {
            supportedCapabilities.add(SelectLiveness.class);
        }
        if (replyBody.getCapabilities().isOFPGFCSELECTWEIGHT()) {
            supportedCapabilities.add(SelectWeight.class);
        }

        message.setGroupCapabilitiesSupported(supportedCapabilities);

        message.setActions(GroupUtil.extractGroupActionsSupportBitmap(replyBody.getActionsBitmap()));
        return message.build();
    }

    private static MeterStatisticsReply translateMeter(final MultipartReply msg,
                                                final NodeId node,
                                                final VersionDatapathIdConvertorData data,
                                                final ConvertorExecutor convertorExecutor) {
        MeterStatisticsUpdatedBuilder message = new MeterStatisticsUpdatedBuilder();
        message.setId(node);
        message.setMoreReplies(msg.getFlags().isOFPMPFREQMORE());
        message.setTransactionId(generateTransactionId(msg.getXid()));

        MultipartReplyMeterCase caseBody = (MultipartReplyMeterCase) msg.getMultipartReplyBody();
        MultipartReplyMeter replyBody = caseBody.getMultipartReplyMeter();
        final Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats>> meterStatsList =
                convertorExecutor.convert(replyBody.getMeterStats(), data);

        message.setMeterStats(meterStatsList.orElse(Collections.emptyList()));

        return message.build();
    }

    private static MeterConfigStatsReply translateMeterConfig(final MultipartReply msg,
                                                       final NodeId node,
                                                       final VersionDatapathIdConvertorData data,
                                                       final ConvertorExecutor convertorExecutor) {
        MeterConfigStatsUpdatedBuilder message = new MeterConfigStatsUpdatedBuilder();
        message.setId(node);
        message.setMoreReplies(msg.getFlags().isOFPMPFREQMORE());
        message.setTransactionId(generateTransactionId(msg.getXid()));

        MultipartReplyMeterConfigCase caseBody = (MultipartReplyMeterConfigCase) msg.getMultipartReplyBody();
        MultipartReplyMeterConfig replyBody = caseBody.getMultipartReplyMeterConfig();
        final Optional<List<MeterConfigStats>> meterConfigStatsList = convertorExecutor.convert(replyBody.getMeterConfig(), data);

        message.setMeterConfigStats(meterConfigStatsList.orElse(Collections.emptyList()));

        return message.build();
    }

    private static MeterFeaturesReply translateMeterFeatures(final MultipartReply msg,
                                                      final NodeId node) {
        MeterFeaturesUpdatedBuilder message = new MeterFeaturesUpdatedBuilder();
        message.setId(node);
        message.setMoreReplies(msg.getFlags().isOFPMPFREQMORE());
        message.setTransactionId(generateTransactionId(msg.getXid()));

        MultipartReplyMeterFeaturesCase caseBody = (MultipartReplyMeterFeaturesCase) msg.getMultipartReplyBody();
        MultipartReplyMeterFeatures replyBody = caseBody.getMultipartReplyMeterFeatures();
        message.setMaxBands(replyBody.getMaxBands());
        message.setMaxColor(replyBody.getMaxColor());
        message.setMaxMeter(new Counter32(replyBody.getMaxMeter()));

        List<Class<? extends MeterCapability>> supportedCapabilities =
                new ArrayList<>();
        if (replyBody.getCapabilities().isOFPMFBURST()) {
            supportedCapabilities.add(MeterBurst.class);
        }
        if (replyBody.getCapabilities().isOFPMFKBPS()) {
            supportedCapabilities.add(MeterKbps.class);

        }
        if (replyBody.getCapabilities().isOFPMFPKTPS()) {
            supportedCapabilities.add(MeterPktps.class);

        }
        if (replyBody.getCapabilities().isOFPMFSTATS()) {
            supportedCapabilities.add(MeterStats.class);

        }
        message.setMeterCapabilitiesSupported(supportedCapabilities);

        List<Class<? extends MeterBand>> supportedMeterBand =
                new ArrayList<>();
        if (replyBody.getBandTypes().isOFPMBTDROP()) {
            supportedMeterBand.add(MeterBandDrop.class);
        }
        if (replyBody.getBandTypes().isOFPMBTDSCPREMARK()) {
            supportedMeterBand.add(MeterBandDscpRemark.class);
        }
        message.setMeterBandSupported(supportedMeterBand);
        return message.build();
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215
        .FlowTableAndStatisticsMap translateTable(final MultipartReply msg,
                                                  final NodeId node) {
        FlowTableStatisticsUpdateBuilder message = new FlowTableStatisticsUpdateBuilder();
        message.setId(node);
        message.setMoreReplies(msg.getFlags().isOFPMPFREQMORE());
        message.setTransactionId(generateTransactionId(msg.getXid()));

        MultipartReplyTableCase caseBody = (MultipartReplyTableCase) msg.getMultipartReplyBody();
        MultipartReplyTable replyBody = caseBody.getMultipartReplyTable();
        List<TableStats> swTablesStats = replyBody.getTableStats();

        List<FlowTableAndStatisticsMap> salFlowStats = new ArrayList<FlowTableAndStatisticsMap>();
        for (TableStats swTableStats : swTablesStats) {
            FlowTableAndStatisticsMapBuilder statisticsBuilder = new FlowTableAndStatisticsMapBuilder();

            statisticsBuilder.setActiveFlows(new Counter32(swTableStats.getActiveCount()));
            statisticsBuilder.setPacketsLookedUp(new Counter64(swTableStats.getLookupCount()));
            statisticsBuilder.setPacketsMatched(new Counter64(swTableStats.getMatchedCount()));
            statisticsBuilder.setTableId(new TableId(swTableStats.getTableId()));
            salFlowStats.add(statisticsBuilder.build());
        }

        message.setFlowTableAndStatisticsMap(salFlowStats);
        return message.build();
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216
        .QueueIdAndStatisticsMap translateQueue(final MultipartReply msg,
                                                final NodeId node,
                                                final OpenflowVersion ofVersion,
                                                final BigInteger datapathId) {
        QueueStatisticsUpdateBuilder message = new QueueStatisticsUpdateBuilder();
        message.setId(node);
        message.setMoreReplies(msg.getFlags().isOFPMPFREQMORE());
        message.setTransactionId(generateTransactionId(msg.getXid()));

        MultipartReplyQueueCase caseBody = (MultipartReplyQueueCase) msg.getMultipartReplyBody();
        MultipartReplyQueue replyBody = caseBody.getMultipartReplyQueue();

        List<QueueIdAndStatisticsMap> statsMap =
                new ArrayList<QueueIdAndStatisticsMap>();

        for (QueueStats queueStats : replyBody.getQueueStats()) {

            QueueIdAndStatisticsMapBuilder statsBuilder =
                    new QueueIdAndStatisticsMapBuilder();
            statsBuilder.setNodeConnectorId(
                    InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(datapathId,
                            queueStats.getPortNo(), ofVersion));
            statsBuilder.setTransmissionErrors(new Counter64(queueStats.getTxErrors()));
            statsBuilder.setTransmittedBytes(new Counter64(queueStats.getTxBytes()));
            statsBuilder.setTransmittedPackets(new Counter64(queueStats.getTxPackets()));

            DurationBuilder durationBuilder = new DurationBuilder();
            durationBuilder.setSecond(new Counter32(queueStats.getDurationSec()));
            durationBuilder.setNanosecond(new Counter32(queueStats.getDurationNsec()));
            statsBuilder.setDuration(durationBuilder.build());

            statsBuilder.setQueueId(new QueueId(queueStats.getQueueId()));
            statsBuilder.setNodeConnectorId(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(datapathId,
                    queueStats.getPortNo(), ofVersion));

            statsMap.add(statsBuilder.build());
        }
        message.setQueueIdAndStatisticsMap(statsMap);

        return message.build();
    }

    private static NodeId nodeIdFromDatapathId(final BigInteger datapathId) {
        String current = datapathId.toString();
        return new NodeId("openflow:" + current);
    }

    private static TransactionId generateTransactionId(final Long xid) {
        BigInteger bigIntXid = BigInteger.valueOf(xid);
        return new TransactionId(bigIntXid);
    }
}
