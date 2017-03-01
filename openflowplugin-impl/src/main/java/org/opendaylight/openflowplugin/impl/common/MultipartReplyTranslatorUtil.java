/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.common;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.table.features._case.MultipartReplyTableFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.multipart.reply.multipart.reply.body.MultipartReplyPortStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMapKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.multipart.reply.multipart.reply.body.MultipartReplyQueueStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.multipart.reply.multipart.reply.body.MultipartReplyQueueStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMapBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.TableId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.multipart.reply.multipart.reply.body.MultipartReplyTableFeaturesBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class converts multipart reply messages to the objects that can be then written to datastore using
 * multipart writers
 */
public class MultipartReplyTranslatorUtil {
    private static final Logger LOG = LoggerFactory.getLogger(MultipartReplyTranslatorUtil.class);

    public static Optional<? extends MultipartReplyBody> translate(final OfHeader message,
                                                                   final DeviceInfo deviceInfo,
                                                                   @Nullable final ConvertorExecutor convertorExecutor,
                                                                   @Nullable final TranslatorLibrary translatorLibrary) {
        if (message instanceof MultipartReply) {
            final Optional<ConvertorExecutor> convertor = Optional.ofNullable(convertorExecutor);
            final Optional<TranslatorLibrary> translator = Optional.ofNullable(translatorLibrary);
            final MultipartReply msg = MultipartReply.class.cast(message);
            final OpenflowVersion ofVersion = OpenflowVersion.get(deviceInfo.getVersion());
            final VersionDatapathIdConvertorData data = new VersionDatapathIdConvertorData(deviceInfo.getVersion());
            data.setDatapathId(deviceInfo.getDatapathId());

            switch (msg.getType()) {
                case OFPMPFLOW:
                    return convertor.flatMap(c -> Optional.of(translateFlow(msg, data, c)));
                case OFPMPAGGREGATE:
                    return Optional.of(translateAggregate(msg));
                case OFPMPPORTSTATS:
                    return Optional.of(translatePortStats(msg, ofVersion, deviceInfo.getDatapathId()));
                case OFPMPGROUP:
                    return convertor.flatMap(c -> Optional.of(translateGroup(msg, data, c)));
                case OFPMPGROUPDESC:
                    return convertor.flatMap(c -> Optional.of(translateGroupDesc(msg, data, c)));
                case OFPMPGROUPFEATURES:
                    return Optional.of(translateGroupFeatures(msg));
                case OFPMPMETER:
                    return convertor.flatMap(c -> Optional.of(translateMeter(msg, data, c)));
                case OFPMPMETERCONFIG:
                    return convertor.flatMap(c -> Optional.of(translateMeterConfig(msg, data, c)));
                case OFPMPMETERFEATURES:
                    return Optional.of(translateMeterFeatures(msg));
                case OFPMPTABLE:
                    return Optional.of(translateTable(msg));
                case OFPMPQUEUE:
                    return Optional.of(translateQueue(msg, ofVersion, deviceInfo.getDatapathId()));
                case OFPMPDESC:
                    return Optional.of(translateDesc(msg));
                case OFPMPTABLEFEATURES:
                    return convertor.flatMap(c -> Optional.of(translateTableFeatures(msg, deviceInfo.getVersion(), c)));
                case OFPMPPORTDESC:
                    return translator.flatMap(t -> Optional.of(translatePortDesc(msg, deviceInfo, t)));
            }
        } else if (message instanceof org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112
            .MultipartReply) {
            return Optional.of(org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112
                .MultipartReply.class.cast(message).getMultipartReplyBody());
        }

        LOG.debug("Failed to translate {} for node {}.", message.getImplementedInterface(), deviceInfo.getLOGValue());
        return Optional.empty();
    }

    private static MultipartReplyPortDesc translatePortDesc(final MultipartReply msg,
                                                            final DeviceInfo deviceInfo,
                                                            final TranslatorLibrary translatorLibrary) {
        return new MultipartReplyPortDescBuilder()
            .setPorts(((MultipartReplyPortDescCase) msg.getMultipartReplyBody())
                .getMultipartReplyPortDesc()
                .getPorts()
                .stream()
                .map(port -> {
                    final TranslatorKey translatorKey = new TranslatorKey(
                        deviceInfo.getVersion(),
                        PortGrouping.class.getName());

                    final MessageTranslator<PortGrouping, FlowCapableNodeConnector> translator = translatorLibrary
                        .lookupTranslator(translatorKey);

                    return new PortsBuilder(translator
                        .translate(port, deviceInfo, null))
                        .build();
                })
                .collect(Collectors.toList()))
            .build();
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.multipart.reply.multipart.reply.body
        .MultipartReplyTableFeatures translateTableFeatures(final MultipartReply msg,
                                                            final short version,
                                                            final ConvertorExecutor convertorExecutor) {
        MultipartReplyTableFeaturesCase caseBody = (MultipartReplyTableFeaturesCase) msg.getMultipartReplyBody();
        final MultipartReplyTableFeatures multipartReplyTableFeatures = caseBody.getMultipartReplyTableFeatures();
        final Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.table.types.rev131026.table.features
            .TableFeatures>> tableFeaturesList = convertorExecutor
            .convert(multipartReplyTableFeatures, new VersionConvertorData(version));

        return new MultipartReplyTableFeaturesBuilder()
            .setTableFeatures(tableFeaturesList.orElse(Collections.emptyList()))
            .build();
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.multipart.reply.multipart.reply.body
        .MultipartReplyDesc translateDesc(final MultipartReply msg) {
        final MultipartReplyDesc desc = ((MultipartReplyDescCase) msg.getMultipartReplyBody()).getMultipartReplyDesc();

        return new MultipartReplyDescBuilder()
            .setDescription(desc.getDpDesc())
            .setHardware(desc.getHwDesc())
            .setManufacturer(desc.getMfrDesc())
            .setSoftware(desc.getSwDesc())
            .setSerialNumber(desc.getSerialNum())
            .build();
    }

    private static MultipartReplyFlowStats translateFlow(final MultipartReply msg,
                                                         final VersionDatapathIdConvertorData data,
                                                         final ConvertorExecutor convertorExecutor) {
        FlowStatsResponseConvertorData flowData = new FlowStatsResponseConvertorData(data.getVersion());
        flowData.setDatapathId(data.getDatapathId());
        flowData.setMatchPath(MatchPath.FLOWSSTATISTICSUPDATE_FLOWANDSTATISTICSMAPLIST_MATCH);
        MultipartReplyFlowStatsBuilder message = new MultipartReplyFlowStatsBuilder();
        MultipartReplyFlowCase caseBody = (MultipartReplyFlowCase) msg.getMultipartReplyBody();
        MultipartReplyFlow replyBody = caseBody.getMultipartReplyFlow();
        final Optional<List<FlowAndStatisticsMapList>> flowAndStatisticsMapLists =
            convertorExecutor.convert(replyBody.getFlowStats(), flowData);

        message.setFlowAndStatisticsMapList(flowAndStatisticsMapLists.orElse(Collections.emptyList()));
        return message.build();
    }

    private static MultipartReplyFlowAggregateStats translateAggregate(final MultipartReply msg) {
        MultipartReplyFlowAggregateStatsBuilder message = new MultipartReplyFlowAggregateStatsBuilder();
        MultipartReplyAggregateCase caseBody = (MultipartReplyAggregateCase) msg.getMultipartReplyBody();
        MultipartReplyAggregate replyBody = caseBody.getMultipartReplyAggregate();
        message.setByteCount(new Counter64(replyBody.getByteCount()));
        message.setPacketCount(new Counter64(replyBody.getPacketCount()));
        message.setFlowCount(new Counter32(replyBody.getFlowCount()));
        return message.build();
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.multipart.reply.multipart.reply.body
        .MultipartReplyPortStats translatePortStats(final MultipartReply msg,
                                                    final OpenflowVersion ofVersion,
                                                    final BigInteger datapathId) {
        MultipartReplyPortStatsBuilder message = new MultipartReplyPortStatsBuilder();
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

    private static MultipartReplyGroupStats translateGroup(final MultipartReply msg,
                                                           final VersionDatapathIdConvertorData data,
                                                           final ConvertorExecutor convertorExecutor) {
        MultipartReplyGroupStatsBuilder message = new MultipartReplyGroupStatsBuilder();
        MultipartReplyGroupCase caseBody = (MultipartReplyGroupCase) msg.getMultipartReplyBody();
        MultipartReplyGroup replyBody = caseBody.getMultipartReplyGroup();
        final Optional<List<GroupStats>> groupStatsList = convertorExecutor.convert(
            replyBody.getGroupStats(), data);

        message.setGroupStats(groupStatsList.orElse(Collections.emptyList()));

        return message.build();
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.reply.multipart.reply.body
        .MultipartReplyGroupDesc translateGroupDesc(final MultipartReply msg,
                                                    final VersionDatapathIdConvertorData data,
                                                    final ConvertorExecutor convertorExecutor) {
        MultipartReplyGroupDescBuilder message = new MultipartReplyGroupDescBuilder();
        MultipartReplyGroupDescCase caseBody = (MultipartReplyGroupDescCase) msg.getMultipartReplyBody();
        MultipartReplyGroupDesc replyBody = caseBody.getMultipartReplyGroupDesc();

        final Optional<List<GroupDescStats>> groupDescStatsList = convertorExecutor.convert(
            replyBody.getGroupDesc(), data);

        message.setGroupDescStats(groupDescStatsList.orElse(Collections.emptyList()));

        return message.build();
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.multipart.reply.multipart.reply.body
        .MultipartReplyGroupFeatures translateGroupFeatures(final MultipartReply msg) {
        MultipartReplyGroupFeaturesBuilder message = new MultipartReplyGroupFeaturesBuilder();
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

    private static MultipartReplyMeterStats translateMeter(final MultipartReply msg,
                                                           final VersionDatapathIdConvertorData data,
                                                           final ConvertorExecutor convertorExecutor) {
        MultipartReplyMeterStatsBuilder message = new MultipartReplyMeterStatsBuilder();
        MultipartReplyMeterCase caseBody = (MultipartReplyMeterCase) msg.getMultipartReplyBody();
        MultipartReplyMeter replyBody = caseBody.getMultipartReplyMeter();
        final Optional<List<org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats>> meterStatsList =
            convertorExecutor.convert(replyBody.getMeterStats(), data);

        message.setMeterStats(meterStatsList.orElse(Collections.emptyList()));

        return message.build();
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.reply.multipart.reply.body
        .MultipartReplyMeterConfig translateMeterConfig(final MultipartReply msg,
                                                        final VersionDatapathIdConvertorData data,
                                                        final ConvertorExecutor convertorExecutor) {
        MultipartReplyMeterConfigBuilder message = new MultipartReplyMeterConfigBuilder();
        MultipartReplyMeterConfigCase caseBody = (MultipartReplyMeterConfigCase) msg.getMultipartReplyBody();
        MultipartReplyMeterConfig replyBody = caseBody.getMultipartReplyMeterConfig();
        final Optional<List<MeterConfigStats>> meterConfigStatsList = convertorExecutor.convert(replyBody.getMeterConfig(), data);

        message.setMeterConfigStats(meterConfigStatsList.orElse(Collections.emptyList()));

        return message.build();
    }

    private static org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.reply.multipart.reply.body
        .MultipartReplyMeterFeatures translateMeterFeatures(final MultipartReply msg) {
        MultipartReplyMeterFeaturesBuilder message = new MultipartReplyMeterFeaturesBuilder();
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

    private static MultipartReplyFlowTableStats translateTable(final MultipartReply msg) {
        MultipartReplyFlowTableStatsBuilder message = new MultipartReplyFlowTableStatsBuilder();
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

    private static MultipartReplyQueueStats translateQueue(final MultipartReply msg,
                                                           final OpenflowVersion ofVersion,
                                                           final BigInteger datapathId) {
        MultipartReplyQueueStatsBuilder message = new MultipartReplyQueueStatsBuilder();
        MultipartReplyQueueCase caseBody = (MultipartReplyQueueCase) msg.getMultipartReplyBody();
        MultipartReplyQueue replyBody = caseBody.getMultipartReplyQueue();

        List<QueueIdAndStatisticsMap> statsMap =
            new ArrayList<>();

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

}
