/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.MessageTranslator;
import org.opendaylight.openflowplugin.api.openflow.device.TranslatorLibrary;
import org.opendaylight.openflowplugin.api.openflow.md.core.TranslatorKey;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.extension.api.path.MatchPath;
import org.opendaylight.openflowplugin.impl.util.GroupUtil;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.FlowStatsResponseConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionConvertorData;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.data.VersionDatapathIdConvertorData;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.multipart.reply.multipart.reply.body.MultipartReplyDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.multipart.reply.multipart.reply.body.MultipartReplyFlowAggregateStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.multipart.reply.multipart.reply.body.MultipartReplyFlowAggregateStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.multipart.reply.multipart.reply.body.MultipartReplyFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.multipart.reply.multipart.reply.body.MultipartReplyFlowStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.multipart.reply.multipart.reply.body.MultipartReplyFlowTableStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.multipart.reply.multipart.reply.body.MultipartReplyFlowTableStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.multipart.reply.multipart.reply.body.MultipartReplyPortDesc;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.multipart.reply.multipart.reply.body.MultipartReplyPortDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.multipart.reply.multipart.reply.body.multipart.reply.port.desc.PortsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.QueueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyGroupDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyGroupFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyGroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyGroupStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Chaining;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.ChainingChecks;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupAll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupFf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupIndirect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupSelect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.SelectLiveness;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.SelectWeight;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.desc.stats.reply.GroupDescStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyMeterConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyMeterFeaturesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyMeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyMeterStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBand;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandDrop;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBandDscpRemark;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterBurst;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterKbps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterPktps;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.config.stats.reply.MeterConfigStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.duration.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.BytesBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.statistics.types.rev130925.node.connector.statistics.PacketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.reply.MultipartReplyBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterConfigCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortDescCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyPortStatsCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyQueueCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyTableFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.aggregate._case.MultipartReplyAggregate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.desc._case.MultipartReplyDesc;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.MultipartReplyTableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.multipart.reply.multipart.reply.body.MultipartReplyPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.multipart.reply.multipart.reply.body.MultipartReplyQueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.multipart.reply.multipart.reply.body.MultipartReplyQueueStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.multipart.reply.multipart.reply.body.MultipartReplyTableFeaturesBuilder;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class converts multipart reply messages to the objects that can be then written to datastore using
 * multipart writers.
 */
public final class MultipartReplyTranslatorUtil {
    private static final Logger LOG = LoggerFactory.getLogger(MultipartReplyTranslatorUtil.class);

    private MultipartReplyTranslatorUtil() {
        // Hidden on purpose
    }

    public static Optional<? extends MultipartReplyBody> translate(final OfHeader message, final DeviceInfo deviceInfo,
            final @Nullable ConvertorExecutor convertorExecutor, final @Nullable TranslatorLibrary translatorLibrary) {

        if (message instanceof MultipartReply) {
            final MultipartReply msg = (MultipartReply) message;
            final OpenflowVersion ofVersion = OpenflowVersion.get(deviceInfo.getVersion());
            final VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(deviceInfo.getVersion());
            data.setDatapathId(deviceInfo.getDatapathId());

            switch (msg.getType()) {
                case OFPMPFLOW:
                    return translateFlow(msg, data, convertorExecutor);
                case OFPMPAGGREGATE:
                    return Optional.of(translateAggregate(msg));
                case OFPMPPORTSTATS:
                    return Optional.of(translatePortStats(msg, ofVersion, deviceInfo.getDatapathId()));
                case OFPMPGROUP:
                    return translateGroup(msg, data, convertorExecutor);
                case OFPMPGROUPDESC:
                    return translateGroupDesc(msg, data, convertorExecutor);
                case OFPMPGROUPFEATURES:
                    return Optional.of(translateGroupFeatures(msg));
                case OFPMPMETER:
                    return translateMeter(msg, data, convertorExecutor);
                case OFPMPMETERCONFIG:
                    return translateMeterConfig(msg, data, convertorExecutor);
                case OFPMPMETERFEATURES:
                    return Optional.of(translateMeterFeatures(msg));
                case OFPMPTABLE:
                    return Optional.of(translateTable(msg));
                case OFPMPQUEUE:
                    return Optional.of(translateQueue(msg, ofVersion, deviceInfo.getDatapathId()));
                case OFPMPDESC:
                    return Optional.of(translateDesc(msg));
                case OFPMPTABLEFEATURES:
                    return translateTableFeatures(msg, deviceInfo.getVersion(), convertorExecutor);
                case OFPMPPORTDESC:
                    return translatePortDesc(msg, deviceInfo, translatorLibrary);
                default:
                    // TODO: log something?
                    break;
            }
        } else if (message instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112
            .MultipartReply) {
            return Optional.of(((org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112
                .MultipartReply) message).getMultipartReplyBody());
        }

        LOG.debug("Failed to translate {} for node {}.", message.implementedInterface(), deviceInfo);
        return Optional.empty();
    }

    private static Optional<MultipartReplyPortDesc> translatePortDesc(final MultipartReply msg,
            final DeviceInfo deviceInfo, final TranslatorLibrary translatorLibrary) {
        if (translatorLibrary == null) {
            return Optional.empty();
        }

        return Optional.of(new MultipartReplyPortDescBuilder()
            .setPorts(((MultipartReplyPortDescCase) msg.getMultipartReplyBody())
                .getMultipartReplyPortDesc().nonnullPorts().stream()
                .map(port -> {
                    final MessageTranslator<PortGrouping, FlowCapableNodeConnector> translator =
                        translatorLibrary .lookupTranslator(new TranslatorKey(deviceInfo.getVersion(),
                            PortGrouping.class.getName()));

                    return new PortsBuilder(translator.translate(port, deviceInfo, null)).build();
                })
                .collect(Collectors.toList()))
            .build());
    }

    private static Optional<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.multipart.reply
            .multipart.reply.body.MultipartReplyTableFeatures> translateTableFeatures(final MultipartReply msg,
                    final short version, final ConvertorExecutor convertorExecutor) {
        if (convertorExecutor == null) {
            return Optional.empty();
        }

        final MultipartReplyTableFeatures multipartReplyTableFeatures =
            ((MultipartReplyTableFeaturesCase) msg.getMultipartReplyBody()).getMultipartReplyTableFeatures();
        final Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features
            .TableFeatures>> tableFeaturesList = convertorExecutor
            .convert(multipartReplyTableFeatures, new VersionConvertorData(version));

        return Optional.of(new MultipartReplyTableFeaturesBuilder()
            .setTableFeatures(tableFeaturesList.orElse(List.of()))
            .build());
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.multipart.reply.multipart
            .reply.body.MultipartReplyDesc translateDesc(final MultipartReply msg) {
        final MultipartReplyDesc desc = ((MultipartReplyDescCase) msg.getMultipartReplyBody()).getMultipartReplyDesc();

        return new MultipartReplyDescBuilder()
            .setDescription(desc.getDpDesc())
            .setHardware(desc.getHwDesc())
            .setManufacturer(desc.getMfrDesc())
            .setSoftware(desc.getSwDesc())
            .setSerialNumber(desc.getSerialNum())
            .build();
    }

    private static Optional<MultipartReplyFlowStats> translateFlow(final MultipartReply msg,
            final VersionDatapathIdConvertorData data, final ConvertorExecutor convertor) {
        if (convertor == null) {
            return Optional.empty();
        }

        final FlowStatsResponseConvertorData flowData = new FlowStatsResponseConvertorData(data.getVersion());
        flowData.setDatapathId(data.getDatapathId());
        flowData.setMatchPath(MatchPath.FLOWS_STATISTICS_UPDATE_MATCH);

        final MultipartReplyFlowCase caseBody = (MultipartReplyFlowCase) msg.getMultipartReplyBody();
        final Optional<List<FlowAndStatisticsMapList>> flowAndStatisticsMapLists =
            convertor.convert(caseBody.getMultipartReplyFlow().getFlowStats(), flowData);

        return Optional.of(new MultipartReplyFlowStatsBuilder()
            .setFlowAndStatisticsMapList(flowAndStatisticsMapLists.orElse(List.of()))
            .build());
    }

    private static MultipartReplyFlowAggregateStats translateAggregate(final MultipartReply msg) {
        final MultipartReplyAggregate replyBody = ((MultipartReplyAggregateCase) msg.getMultipartReplyBody())
            .getMultipartReplyAggregate();

        return new MultipartReplyFlowAggregateStatsBuilder()
            .setByteCount(new Counter64(replyBody.getByteCount()))
            .setPacketCount(new Counter64(replyBody.getPacketCount()))
            .setFlowCount(new Counter32(replyBody.getFlowCount()))
            .build();
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.multipart.reply.multipart
            .reply.body.MultipartReplyPortStats translatePortStats(final MultipartReply msg,
                    final OpenflowVersion ofVersion, final Uint64 datapathId) {
        MultipartReplyPortStatsCase caseBody = (MultipartReplyPortStatsCase) msg.getMultipartReplyBody();
        MultipartReplyPortStats replyBody = caseBody.getMultipartReplyPortStats();

        List<NodeConnectorStatisticsAndPortNumberMap> statsMap = new ArrayList<>();
        for (PortStats portStats : replyBody.nonnullPortStats()) {
            final DurationBuilder durationBuilder = new DurationBuilder();
            if (portStats.getDurationSec() != null) {
                durationBuilder.setSecond(new Counter32(portStats.getDurationSec()));
            }
            if (portStats.getDurationNsec() != null) {
                durationBuilder.setNanosecond(new Counter32(portStats.getDurationNsec()));
            }

            statsMap.add(new NodeConnectorStatisticsAndPortNumberMapBuilder()
                .setNodeConnectorId(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(datapathId,
                    portStats.getPortNo(), ofVersion))
                .setBytes(new BytesBuilder()
                    .setReceived(portStats.getRxBytes())
                    .setTransmitted(portStats.getTxBytes())
                    .build())
                .setPackets(new PacketsBuilder()
                    .setReceived(portStats.getRxPackets())
                    .setTransmitted(portStats.getTxPackets())
                    .build())
                .setDuration(durationBuilder.build())
                .setCollisionCount(portStats.getCollisions())
                .setReceiveCrcError(portStats.getRxCrcErr())
                .setReceiveDrops(portStats.getRxDropped())
                .setReceiveErrors(portStats.getRxErrors())
                .setReceiveFrameError(portStats.getRxFrameErr())
                .setReceiveOverRunError(portStats.getRxOverErr())
                .setTransmitDrops(portStats.getTxDropped())
                .setTransmitErrors(portStats.getTxErrors())
                .build());
        }

        return new MultipartReplyPortStatsBuilder()
            .setNodeConnectorStatisticsAndPortNumberMap(statsMap)
            .build();
    }

    private static Optional<MultipartReplyGroupStats> translateGroup(final MultipartReply msg,
            final VersionDatapathIdConvertorData data, final ConvertorExecutor convertorExecutor) {
        if (convertorExecutor == null) {
            return Optional.empty();
        }

        final MultipartReplyGroup replyBody = ((MultipartReplyGroupCase) msg.getMultipartReplyBody())
            .getMultipartReplyGroup();
        final Optional<List<GroupStats>> groupStatsList = convertorExecutor.convert(
            replyBody.getGroupStats(), data);

        return Optional.of(new MultipartReplyGroupStatsBuilder()
            .setGroupStats(groupStatsList.orElse(List.of()))
            .build());
    }

    private static Optional<org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.reply
            .multipart.reply.body.MultipartReplyGroupDesc> translateGroupDesc(final MultipartReply msg,
                    final VersionDatapathIdConvertorData data, final ConvertorExecutor convertorExecutor) {
        if (convertorExecutor == null) {
            return Optional.empty();
        }

        MultipartReplyGroupDescCase caseBody = (MultipartReplyGroupDescCase) msg.getMultipartReplyBody();
        MultipartReplyGroupDesc replyBody = caseBody.getMultipartReplyGroupDesc();

        final Optional<List<GroupDescStats>> groupDescStatsList = convertorExecutor.convert(
            replyBody.getGroupDesc(), data);

        return Optional.of(new MultipartReplyGroupDescBuilder()
            .setGroupDescStats(groupDescStatsList.orElse(List.of()))
            .build());
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.reply.multipart
            .reply.body.MultipartReplyGroupFeatures translateGroupFeatures(final MultipartReply msg) {
        final MultipartReplyGroupFeatures replyBody = ((MultipartReplyGroupFeaturesCase) msg.getMultipartReplyBody())
            .getMultipartReplyGroupFeatures();
        final List<Class<? extends GroupType>> supportedGroups = new ArrayList<>();
        if (replyBody.getTypes().getOFPGTALL()) {
            supportedGroups.add(GroupAll.class);
        }
        if (replyBody.getTypes().getOFPGTSELECT()) {
            supportedGroups.add(GroupSelect.class);
        }
        if (replyBody.getTypes().getOFPGTINDIRECT()) {
            supportedGroups.add(GroupIndirect.class);
        }
        if (replyBody.getTypes().getOFPGTFF()) {
            supportedGroups.add(GroupFf.class);
        }

        List<Class<? extends GroupCapability>> supportedCapabilities = new ArrayList<>();
        if (replyBody.getCapabilities().getOFPGFCCHAINING()) {
            supportedCapabilities.add(Chaining.class);
        }
        if (replyBody.getCapabilities().getOFPGFCCHAININGCHECKS()) {
            supportedCapabilities.add(ChainingChecks.class);
        }
        if (replyBody.getCapabilities().getOFPGFCSELECTLIVENESS()) {
            supportedCapabilities.add(SelectLiveness.class);
        }
        if (replyBody.getCapabilities().getOFPGFCSELECTWEIGHT()) {
            supportedCapabilities.add(SelectWeight.class);
        }

        return new MultipartReplyGroupFeaturesBuilder()
            .setGroupTypesSupported(supportedGroups)
            .setMaxGroups(replyBody.getMaxGroups())
            .setGroupCapabilitiesSupported(supportedCapabilities)
            .setActions(GroupUtil.extractGroupActionsSupportBitmap(replyBody.getActionsBitmap()))
            .build();
    }

    private static Optional<MultipartReplyMeterStats> translateMeter(final MultipartReply msg,
            final VersionDatapathIdConvertorData data, final ConvertorExecutor convertorExecutor) {
        if (convertorExecutor == null) {
            return Optional.empty();
        }

        MultipartReplyMeter replyBody = ((MultipartReplyMeterCase) msg.getMultipartReplyBody()).getMultipartReplyMeter();
        final Optional<List<
            org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats>>
                meterStatsList = convertorExecutor.convert(replyBody.getMeterStats(), data);

        return Optional.of(new MultipartReplyMeterStatsBuilder()
            .setMeterStats(meterStatsList.orElse(List.of()))
            .build());
    }

    private static Optional<org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.reply
            .multipart.reply.body.MultipartReplyMeterConfig> translateMeterConfig(final MultipartReply msg,
                    final VersionDatapathIdConvertorData data, final ConvertorExecutor convertorExecutor) {
        if (convertorExecutor == null) {
            return Optional.empty();
        }

        MultipartReplyMeterConfigCase caseBody = (MultipartReplyMeterConfigCase) msg.getMultipartReplyBody();
        MultipartReplyMeterConfig replyBody = caseBody.getMultipartReplyMeterConfig();
        final Optional<List<MeterConfigStats>> meterConfigStatsList
                = convertorExecutor.convert(replyBody.getMeterConfig(), data);

        return Optional.of(new MultipartReplyMeterConfigBuilder()
            .setMeterConfigStats(meterConfigStatsList.orElse(List.of()))
            .build());
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.reply.multipart
            .reply.body.MultipartReplyMeterFeatures translateMeterFeatures(final MultipartReply msg) {
        MultipartReplyMeterFeaturesCase caseBody = (MultipartReplyMeterFeaturesCase) msg.getMultipartReplyBody();
        MultipartReplyMeterFeatures replyBody = caseBody.getMultipartReplyMeterFeatures();

        final List<Class<? extends MeterCapability>> supportedCapabilities = new ArrayList<>();
        if (replyBody.getCapabilities().getOFPMFBURST()) {
            supportedCapabilities.add(MeterBurst.class);
        }
        if (replyBody.getCapabilities().getOFPMFKBPS()) {
            supportedCapabilities.add(MeterKbps.class);
        }
        if (replyBody.getCapabilities().getOFPMFPKTPS()) {
            supportedCapabilities.add(MeterPktps.class);
        }
        if (replyBody.getCapabilities().getOFPMFSTATS()) {
            supportedCapabilities.add(MeterStats.class);
        }

        final List<Class<? extends MeterBand>> supportedMeterBand = new ArrayList<>();
        if (replyBody.getBandTypes().getOFPMBTDROP()) {
            supportedMeterBand.add(MeterBandDrop.class);
        }
        if (replyBody.getBandTypes().getOFPMBTDSCPREMARK()) {
            supportedMeterBand.add(MeterBandDscpRemark.class);
        }

        return new MultipartReplyMeterFeaturesBuilder()
            .setMaxBands(replyBody.getMaxBands())
            .setMaxColor(replyBody.getMaxColor())
            .setMaxMeter(new Counter32(replyBody.getMaxMeter()))
            .setMeterCapabilitiesSupported(supportedCapabilities)
            .setMeterBandSupported(supportedMeterBand)
            .build();
    }

    private static MultipartReplyFlowTableStats translateTable(final MultipartReply msg) {
        MultipartReplyTableCase caseBody = (MultipartReplyTableCase) msg.getMultipartReplyBody();
        MultipartReplyTable replyBody = caseBody.getMultipartReplyTable();

        List<FlowTableAndStatisticsMap> salFlowStats = new ArrayList<>();
        //TODO: Duplicate code: look at OpendaylightFlowTableStatisticsServiceImpl method transformToNotification
        for (TableStats swTableStats : replyBody.nonnullTableStats()) {
            salFlowStats.add(new FlowTableAndStatisticsMapBuilder()
                .setActiveFlows(new Counter32(swTableStats.getActiveCount()))
                .setPacketsLookedUp(new Counter64(swTableStats.getLookupCount()))
                .setPacketsMatched(new Counter64(swTableStats.getMatchedCount()))
                .setTableId(new TableId(swTableStats.getTableId()))
                .build());
        }

        return new MultipartReplyFlowTableStatsBuilder()
            .setFlowTableAndStatisticsMap(salFlowStats)
            .build();
    }

    private static MultipartReplyQueueStats translateQueue(final MultipartReply msg,
                                                           final OpenflowVersion ofVersion,
                                                           final Uint64 datapathId) {
        MultipartReplyQueueCase caseBody = (MultipartReplyQueueCase) msg.getMultipartReplyBody();
        MultipartReplyQueue replyBody = caseBody.getMultipartReplyQueue();

        List<QueueIdAndStatisticsMap> statsMap = new ArrayList<>();
        for (QueueStats queueStats : replyBody.nonnullQueueStats()) {
            statsMap.add(new QueueIdAndStatisticsMapBuilder()
                .setNodeConnectorId(InventoryDataServiceUtil.nodeConnectorIdfromDatapathPortNo(datapathId,
                    queueStats.getPortNo(), ofVersion))
                .setQueueId(new QueueId(queueStats.getQueueId()))
                .setTransmissionErrors(new Counter64(queueStats.getTxErrors()))
                .setTransmittedBytes(new Counter64(queueStats.getTxBytes()))
                .setTransmittedPackets(new Counter64(queueStats.getTxPackets()))
                .setDuration(new DurationBuilder()
                    .setSecond(new Counter32(queueStats.getDurationSec()))
                    .setNanosecond(new Counter32(queueStats.getDurationNsec()))
                    .build())
                .build());
        }

        return new MultipartReplyQueueStatsBuilder()
            .setQueueIdAndStatisticsMap(statsMap)
            .build();
    }
}
