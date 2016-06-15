/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.FutureFallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceRegistry;
import org.opendaylight.openflowplugin.api.openflow.device.TxFacade;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.DeviceFlowRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.api.openflow.registry.group.DeviceGroupRegistry;
import org.opendaylight.openflowplugin.api.openflow.registry.meter.DeviceMeterRegistry;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.StatisticsGatherer;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowRegistryKeyFactory;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.EventsTimeCounter;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableStatisticsGatheringStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableStatisticsGatheringStatusBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.snapshot.gathering.status.grouping.SnapshotGatheringStatusEnd;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.snapshot.gathering.status.grouping.SnapshotGatheringStatusEndBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.snapshot.gathering.status.grouping.SnapshotGatheringStatusStartBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowStatisticsDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.statistics.FlowStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.statistics.FlowTableStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.statistics.FlowTableStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.Queue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.QueueBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.QueueKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupDescStatsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupStatisticsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.group.statistics.GroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.group.statistics.GroupStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.desc.stats.reply.GroupDescStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterConfigStatsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterStatisticsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.nodes.node.meter.MeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.nodes.node.meter.MeterStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.config.stats.reply.MeterConfigStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.NodeConnectorStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.flow.capable.node.connector.statistics.FlowCapableNodeConnectorStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.flow.capable.node.connector.statistics.FlowCapableNodeConnectorStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.FlowCapableNodeConnectorQueueStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.FlowCapableNodeConnectorQueueStatisticsDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.QueueStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.flow.capable.node.connector.queue.statistics.FlowCapableNodeConnectorQueueStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.flow.capable.node.connector.queue.statistics.FlowCapableNodeConnectorQueueStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMap;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utils for gatherig statistics
 */
public final class StatisticsGatheringUtils {

    private static String DATE_AND_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsGatheringUtils.class);
    private static final SinglePurposeMultipartReplyTranslator MULTIPART_REPLY_TRANSLATOR = new SinglePurposeMultipartReplyTranslator();
    private static final String QUEUE2_REQCTX = "QUEUE2REQCTX-";

    private StatisticsGatheringUtils() {
        throw new IllegalStateException("This class should not be instantiated.");
    }

    //TODO: Flow-,Group- and Meter- registry should be not in device context, consider move it in separate class
    static ListenableFuture<Boolean> gatherStatistics(final StatisticsGatherer statisticsGatheringService,
                                                      final DeviceInfo deviceInfo,
                                                      final MultipartType type,
                                                      final TxFacade txFacade,
                                                      final DeviceRegistry registry,
                                                      final Boolean initial) {
        EventIdentifier wholeProcessEventIdentifier = null;
        if (MultipartType.OFPMPFLOW.equals(type)) {
            wholeProcessEventIdentifier = new EventIdentifier(type.toString(), deviceInfo.getNodeId().getValue());
            EventsTimeCounter.markStart(wholeProcessEventIdentifier);
        }
        final EventIdentifier ofpQueuToRequestContextEventIdentifier = new EventIdentifier(QUEUE2_REQCTX + type.toString(), deviceInfo.getNodeId().toString());
        final ListenableFuture<RpcResult<List<MultipartReply>>> statisticsDataInFuture =
                JdkFutureAdapters.listenInPoolThread(statisticsGatheringService.getStatisticsOfType(
                        ofpQueuToRequestContextEventIdentifier, type));
        return transformAndStoreStatisticsData(statisticsDataInFuture, deviceInfo, wholeProcessEventIdentifier, type, txFacade, registry, initial);
    }

    private static ListenableFuture<Boolean> transformAndStoreStatisticsData(final ListenableFuture<RpcResult<List<MultipartReply>>> statisticsDataInFuture,
                                                                             final DeviceInfo deviceInfo,
                                                                             final EventIdentifier eventIdentifier,
                                                                             final MultipartType type,
                                                                             final TxFacade txFacade,
                                                                             final DeviceRegistry registry,
                                                                             final boolean initial) {
        return Futures.transform(statisticsDataInFuture, new AsyncFunction<RpcResult<List<MultipartReply>>, Boolean>() {
            @Nullable
            @Override
            public ListenableFuture<Boolean> apply(final RpcResult<List<MultipartReply>> rpcResult) {
                boolean isMultipartProcessed = Boolean.TRUE;
                if (rpcResult.isSuccessful()) {
                    LOG.debug("Stats reply successfully received for node {} of type {}", deviceInfo.getNodeId(), type);

                    // TODO: in case the result value is null then multipart data probably got processed on the fly -
                    // TODO: this contract should by clearly stated and enforced - now simple true value is returned
                    if (null != rpcResult.getResult()) {
                        Iterable<? extends DataObject> allMultipartData = Collections.emptyList();
                        DataObject multipartData = null;


                        try {
                            for (final MultipartReply singleReply : rpcResult.getResult()) {
                                final List<? extends DataObject> multipartDataList = MULTIPART_REPLY_TRANSLATOR.translate(
                                        deviceInfo.getDatapathId(),
                                        deviceInfo.getVersion(), singleReply);
                                multipartData = multipartDataList.get(0);
                                allMultipartData = Iterables.concat(allMultipartData, multipartDataList);
                            }
                        } catch (final Exception e) {
                            LOG.warn("stats processing of type {} for node {} failed during transfomation step",
                                    type, deviceInfo.getNodeId(), e);
                            return Futures.immediateFailedFuture(e);
                        }


                        try {
                            if (multipartData instanceof GroupStatisticsUpdated) {
                                processGroupStatistics((Iterable<GroupStatisticsUpdated>) allMultipartData, deviceInfo, txFacade);
                            } else if (multipartData instanceof MeterStatisticsUpdated) {
                                processMetersStatistics((Iterable<MeterStatisticsUpdated>) allMultipartData, deviceInfo, txFacade);
                            } else if (multipartData instanceof NodeConnectorStatisticsUpdate) {
                                processNodeConnectorStatistics((Iterable<NodeConnectorStatisticsUpdate>) allMultipartData, deviceInfo, txFacade);
                            } else if (multipartData instanceof FlowTableStatisticsUpdate) {
                                processFlowTableStatistics((Iterable<FlowTableStatisticsUpdate>) allMultipartData, deviceInfo, txFacade);
                            } else if (multipartData instanceof QueueStatisticsUpdate) {
                                processQueueStatistics((Iterable<QueueStatisticsUpdate>) allMultipartData, deviceInfo, txFacade);
                            } else if (multipartData instanceof FlowsStatisticsUpdate) {
                                /* FlowStat Processing is realized by NettyThread only by initPhase, otherwise it is realized
                                 * by MD-SAL thread */
                                return processFlowStatistics((Iterable<FlowsStatisticsUpdate>) allMultipartData, deviceInfo, txFacade, registry.getDeviceFlowRegistry(), initial, eventIdentifier);

                            } else if (multipartData instanceof GroupDescStatsUpdated) {
                                processGroupDescStats((Iterable<GroupDescStatsUpdated>) allMultipartData, deviceInfo, txFacade, registry.getDeviceGroupRegistry());
                            } else if (multipartData instanceof MeterConfigStatsUpdated) {
                                processMeterConfigStatsUpdated((Iterable<MeterConfigStatsUpdated>) allMultipartData, deviceInfo, txFacade, registry.getDeviceMeterRegistry());
                            } else {
                                isMultipartProcessed = Boolean.FALSE;
                            }
                        } catch (final Exception e) {
                            LOG.warn("stats processing of type {} for node {} failed during write-to-tx step",
                                    type, deviceInfo.getNodeId(), e);
                            return Futures.immediateFailedFuture(e);
                        }

                        LOG.debug("Stats reply added to transaction for node {} of type {}", deviceInfo.getNodeId(), type);

                        //TODO : implement experimenter
                    } else {
                        LOG.debug("Stats reply was empty for node {} of type {}", deviceInfo.getNodeId(), type);
                    }

                } else {
                    LOG.debug("Stats reply FAILED for node {} of type {}: {}", deviceInfo.getNodeId(), type, rpcResult.getErrors());
                    isMultipartProcessed = Boolean.FALSE;
                }
                return Futures.immediateFuture(isMultipartProcessed);
            }
        });
    }

    private static void processMeterConfigStatsUpdated(final Iterable<MeterConfigStatsUpdated> data,
                                                       final DeviceInfo deviceInfo,
                                                       final TxFacade txFacade,
                                                       final DeviceMeterRegistry meterRegistry) throws Exception {
        final InstanceIdentifier<FlowCapableNode> fNodeIdent = assembleFlowCapableNodeInstanceIdentifier(deviceInfo);
        deleteAllKnownMeters(meterRegistry, fNodeIdent, txFacade);
        for (final MeterConfigStatsUpdated meterConfigStatsUpdated : data) {
            for (final MeterConfigStats meterConfigStats : meterConfigStatsUpdated.getMeterConfigStats()) {
                final MeterId meterId = meterConfigStats.getMeterId();
                final InstanceIdentifier<Meter> meterInstanceIdentifier = fNodeIdent.child(Meter.class, new MeterKey(meterId));

                final MeterBuilder meterBuilder = new MeterBuilder(meterConfigStats);
                meterBuilder.setKey(new MeterKey(meterId));
                meterBuilder.addAugmentation(NodeMeterStatistics.class, new NodeMeterStatisticsBuilder().build());
                meterRegistry.store(meterId);
                txFacade.writeToTransaction(LogicalDatastoreType.OPERATIONAL, meterInstanceIdentifier, meterBuilder.build());
            }
        }
        txFacade.submitTransaction();
    }

    private static ListenableFuture<Boolean> processFlowStatistics(final Iterable<FlowsStatisticsUpdate> data,
                                                                   final DeviceInfo deviceInfo,
                                                                   final TxFacade txFacade,
                                                                   final DeviceFlowRegistry flowRegistry,
                                                                   final boolean initial,
                                                                   final EventIdentifier eventIdentifier) {
        final ListenableFuture<Void> deleteFuture = initial ? Futures.immediateFuture(null) : deleteAllKnownFlows(deviceInfo,
                flowRegistry, txFacade);
        return Futures.transform(deleteFuture, new Function<Void, Boolean>() {

            @Override
            public Boolean apply(final Void input) {
                writeFlowStatistics(data, deviceInfo, flowRegistry, txFacade);
                txFacade.submitTransaction();
                EventsTimeCounter.markEnd(eventIdentifier);
                return Boolean.TRUE;
            }
        });
    }

    public static void writeFlowStatistics(final Iterable<FlowsStatisticsUpdate> data,
                                           final DeviceInfo deviceInfo,
                                           final DeviceFlowRegistry registry,
                                           final TxFacade txFacade) {
        final InstanceIdentifier<FlowCapableNode> fNodeIdent = assembleFlowCapableNodeInstanceIdentifier(deviceInfo);
        try {
            for (final FlowsStatisticsUpdate flowsStatistics : data) {
                for (final FlowAndStatisticsMapList flowStat : flowsStatistics.getFlowAndStatisticsMapList()) {
                    final FlowBuilder flowBuilder = new FlowBuilder(flowStat);
                    flowBuilder.addAugmentation(FlowStatisticsData.class, refineFlowStatisticsAugmentation(flowStat).build());

                    final short tableId = flowStat.getTableId();
                    final FlowRegistryKey flowRegistryKey = FlowRegistryKeyFactory.create(flowBuilder.build());
                    final FlowId flowId = registry.storeIfNecessary(flowRegistryKey, tableId);

                    final FlowKey flowKey = new FlowKey(flowId);
                    flowBuilder.setKey(flowKey);
                    final TableKey tableKey = new TableKey(tableId);
                    final InstanceIdentifier<Flow> flowIdent = fNodeIdent.child(Table.class, tableKey).child(Flow.class, flowKey);
                    txFacade.writeToTransaction(LogicalDatastoreType.OPERATIONAL, flowIdent, flowBuilder.build());
                }
            }
        } catch (Exception e) {
            LOG.warn("Not able to write to transaction: {}", e.getMessage());
        }
    }

    /**
     * Method extracts flow statistics out of flowAndStatistics model
     *
     * @param flowAndStats
     */
    private static FlowStatisticsDataBuilder refineFlowStatisticsAugmentation(final FlowAndStatisticsMapList flowAndStats) {
        final FlowStatisticsBuilder flowStatisticsBuilder = new FlowStatisticsBuilder(flowAndStats);
        final FlowStatisticsDataBuilder flowStatisticsDataBld = new FlowStatisticsDataBuilder();
        flowStatisticsDataBld.setFlowStatistics(flowStatisticsBuilder.build());
        return flowStatisticsDataBld;
    }

    public static ListenableFuture<Void> deleteAllKnownFlows(final DeviceInfo deviceInfo,
                                                             final DeviceFlowRegistry registry,
                                                             final TxFacade txFacade) {
        final InstanceIdentifier<FlowCapableNode> flowCapableNodePath = assembleFlowCapableNodeInstanceIdentifier(deviceInfo);
        final ReadOnlyTransaction readTx = txFacade.getReadTransaction();
        final CheckedFuture<Optional<FlowCapableNode>, ReadFailedException> flowCapableNodeFuture = readTx.read(
                LogicalDatastoreType.OPERATIONAL, flowCapableNodePath);

        /* we wish to close readTx for fallBack */
        Futures.withFallback(flowCapableNodeFuture, new FutureFallback<Optional<FlowCapableNode>>() {

            @Override
            public ListenableFuture<Optional<FlowCapableNode>> create(final Throwable t) throws Exception {
                readTx.close();
                return Futures.immediateFailedFuture(t);
            }
        });
        /*
         * we have to read actual tables with all information before we set empty Flow list, merge is expensive and
         * not applicable for lists
         */
        return Futures.transform(flowCapableNodeFuture, new AsyncFunction<Optional<FlowCapableNode>, Void>() {

            @Override
            public ListenableFuture<Void> apply(final Optional<FlowCapableNode> flowCapNodeOpt) throws Exception {
                if (flowCapNodeOpt.isPresent()) {
                    for (final Table tableData : flowCapNodeOpt.get().getTable()) {
                        final Table table = new TableBuilder(tableData).setFlow(Collections.<Flow>emptyList()).build();
                        final InstanceIdentifier<Table> iiToTable = flowCapableNodePath.child(Table.class, tableData.getKey());
                        txFacade.writeToTransaction(LogicalDatastoreType.OPERATIONAL, iiToTable, table);
                    }
                }
                registry.removeMarked();
                readTx.close();
                return Futures.immediateFuture(null);
            }

        });
    }

    private static void processQueueStatistics(final Iterable<QueueStatisticsUpdate> data, final DeviceInfo deviceInfo, final TxFacade txFacade) throws Exception {
        // TODO: clean all queues of all node-connectors before writing up-to-date stats
        final InstanceIdentifier<Node> nodeIdent = deviceInfo.getNodeInstanceIdentifier();
        for (final QueueStatisticsUpdate queueStatisticsUpdate : data) {
            for (final QueueIdAndStatisticsMap queueStat : queueStatisticsUpdate.getQueueIdAndStatisticsMap()) {
                if (queueStat.getQueueId() != null) {
                    final FlowCapableNodeConnectorQueueStatistics statChild =
                            new FlowCapableNodeConnectorQueueStatisticsBuilder(queueStat).build();
                    final FlowCapableNodeConnectorQueueStatisticsDataBuilder statBuild =
                            new FlowCapableNodeConnectorQueueStatisticsDataBuilder();
                    statBuild.setFlowCapableNodeConnectorQueueStatistics(statChild);
                    final QueueKey qKey = new QueueKey(queueStat.getQueueId());
                    final InstanceIdentifier<Queue> queueIdent = nodeIdent
                            .child(NodeConnector.class, new NodeConnectorKey(queueStat.getNodeConnectorId()))
                            .augmentation(FlowCapableNodeConnector.class)
                            .child(Queue.class, qKey);
                    final QueueBuilder queueBuilder = new QueueBuilder()
                            .setKey(qKey)
                            .setQueueId(queueStat.getQueueId())
                            // node-connector-id is already contained in parent node and the port-id here is of incompatible format
                            .addAugmentation(FlowCapableNodeConnectorQueueStatisticsData.class, statBuild.build());
                    txFacade.writeToTransaction(LogicalDatastoreType.OPERATIONAL, queueIdent, queueBuilder.build());
                }
            }
        }
        txFacade.submitTransaction();
    }

    private static void processFlowTableStatistics(final Iterable<FlowTableStatisticsUpdate> data, final DeviceInfo deviceInfo, final TxFacade txFacade) throws Exception {
        final InstanceIdentifier<FlowCapableNode> fNodeIdent = assembleFlowCapableNodeInstanceIdentifier(deviceInfo);
        for (final FlowTableStatisticsUpdate flowTableStatisticsUpdate : data) {

            for (final FlowTableAndStatisticsMap tableStat : flowTableStatisticsUpdate.getFlowTableAndStatisticsMap()) {
                final InstanceIdentifier<FlowTableStatistics> tStatIdent = fNodeIdent.child(Table.class, new TableKey(tableStat.getTableId().getValue()))
                        .augmentation(FlowTableStatisticsData.class).child(FlowTableStatistics.class);
                final FlowTableStatistics stats = new FlowTableStatisticsBuilder(tableStat).build();
                txFacade.writeToTransaction(LogicalDatastoreType.OPERATIONAL, tStatIdent, stats);
            }
        }
        txFacade.submitTransaction();
    }

    private static void processNodeConnectorStatistics(final Iterable<NodeConnectorStatisticsUpdate> data, final DeviceInfo deviceInfo, final TxFacade txFacade) throws Exception {
        final InstanceIdentifier<Node> nodeIdent = deviceInfo.getNodeInstanceIdentifier();
        for (final NodeConnectorStatisticsUpdate nodeConnectorStatisticsUpdate : data) {
            for (final NodeConnectorStatisticsAndPortNumberMap nConnectPort : nodeConnectorStatisticsUpdate.getNodeConnectorStatisticsAndPortNumberMap()) {
                final FlowCapableNodeConnectorStatistics stats = new FlowCapableNodeConnectorStatisticsBuilder(nConnectPort).build();
                final NodeConnectorKey key = new NodeConnectorKey(nConnectPort.getNodeConnectorId());
                final InstanceIdentifier<NodeConnector> nodeConnectorIdent = nodeIdent.child(NodeConnector.class, key);
                final InstanceIdentifier<FlowCapableNodeConnectorStatisticsData> nodeConnStatIdent = nodeConnectorIdent
                        .augmentation(FlowCapableNodeConnectorStatisticsData.class);
                final InstanceIdentifier<FlowCapableNodeConnectorStatistics> flowCapNodeConnStatIdent =
                        nodeConnStatIdent.child(FlowCapableNodeConnectorStatistics.class);
                txFacade.writeToTransaction(LogicalDatastoreType.OPERATIONAL, flowCapNodeConnStatIdent, stats);
            }
        }
        txFacade.submitTransaction();
    }

    private static void processMetersStatistics(final Iterable<MeterStatisticsUpdated> data,
                                                final DeviceInfo deviceInfo,
                                                final TxFacade txFacade) throws Exception {
        final InstanceIdentifier<FlowCapableNode> fNodeIdent = assembleFlowCapableNodeInstanceIdentifier(deviceInfo);
        for (final MeterStatisticsUpdated meterStatisticsUpdated : data) {
            for (final MeterStats mStat : meterStatisticsUpdated.getMeterStats()) {
                final MeterStatistics stats = new MeterStatisticsBuilder(mStat).build();
                final MeterId meterId = mStat.getMeterId();
                final InstanceIdentifier<Meter> meterIdent = fNodeIdent.child(Meter.class, new MeterKey(meterId));
                final InstanceIdentifier<NodeMeterStatistics> nodeMeterStatIdent = meterIdent
                        .augmentation(NodeMeterStatistics.class);
                final InstanceIdentifier<MeterStatistics> msIdent = nodeMeterStatIdent.child(MeterStatistics.class);
                txFacade.writeToTransaction(LogicalDatastoreType.OPERATIONAL, msIdent, stats);
            }
        }
        txFacade.submitTransaction();
    }

    private static void deleteAllKnownMeters(final DeviceMeterRegistry meterRegistry, final InstanceIdentifier<FlowCapableNode> fNodeIdent, final TxFacade txFacade) throws Exception {
        for (final MeterId meterId : meterRegistry.getAllMeterIds()) {
            final InstanceIdentifier<Meter> meterIdent = fNodeIdent.child(Meter.class, new MeterKey(meterId));
            txFacade.addDeleteToTxChain(LogicalDatastoreType.OPERATIONAL, meterIdent);
        }
        meterRegistry.removeMarked();
    }

    private static void processGroupDescStats(final Iterable<GroupDescStatsUpdated> data, final DeviceInfo deviceInfo, final TxFacade txFacade, final DeviceGroupRegistry groupRegistry) throws Exception {
        final InstanceIdentifier<FlowCapableNode> fNodeIdent =
                deviceInfo.getNodeInstanceIdentifier().augmentation(FlowCapableNode.class);
        deleteAllKnownGroups(txFacade, fNodeIdent, groupRegistry);

        for (final GroupDescStatsUpdated groupDescStatsUpdated : data) {
            for (final GroupDescStats groupDescStats : groupDescStatsUpdated.getGroupDescStats()) {
                final GroupId groupId = groupDescStats.getGroupId();

                final GroupBuilder groupBuilder = new GroupBuilder(groupDescStats);
                groupBuilder.setKey(new GroupKey(groupId));
                groupBuilder.addAugmentation(NodeGroupStatistics.class, new NodeGroupStatisticsBuilder().build());

                final InstanceIdentifier<Group> groupIdent = fNodeIdent.child(Group.class, new GroupKey(groupId));

                groupRegistry.store(groupId);
                txFacade.writeToTransaction(LogicalDatastoreType.OPERATIONAL, groupIdent, groupBuilder.build());
            }
        }
        txFacade.submitTransaction();
    }

    private static void deleteAllKnownGroups(final TxFacade txFacade, final InstanceIdentifier<FlowCapableNode> fNodeIdent, final DeviceGroupRegistry groupRegistry) throws Exception {
        for (final GroupId groupId : groupRegistry.getAllGroupIds()) {
            final InstanceIdentifier<Group> groupIdent = fNodeIdent.child(Group.class, new GroupKey(groupId));
            txFacade.addDeleteToTxChain(LogicalDatastoreType.OPERATIONAL, groupIdent);
        }
        groupRegistry.removeMarked();
    }

    private static void processGroupStatistics(final Iterable<GroupStatisticsUpdated> data, final DeviceInfo deviceInfo, final TxFacade txFacade) throws Exception {
        final InstanceIdentifier<FlowCapableNode> fNodeIdent = assembleFlowCapableNodeInstanceIdentifier(deviceInfo);
        for (final GroupStatisticsUpdated groupStatistics : data) {
            for (final GroupStats groupStats : groupStatistics.getGroupStats()) {

                final InstanceIdentifier<Group> groupIdent = fNodeIdent.child(Group.class, new GroupKey(groupStats.getGroupId()));
                final InstanceIdentifier<NodeGroupStatistics> nGroupStatIdent = groupIdent
                        .augmentation(NodeGroupStatistics.class);

                final InstanceIdentifier<GroupStatistics> gsIdent = nGroupStatIdent.child(GroupStatistics.class);
                final GroupStatistics stats = new GroupStatisticsBuilder(groupStats).build();
                txFacade.writeToTransaction(LogicalDatastoreType.OPERATIONAL, gsIdent, stats);
            }
        }
        txFacade.submitTransaction();
    }

    private static InstanceIdentifier<FlowCapableNode> assembleFlowCapableNodeInstanceIdentifier(final DeviceInfo deviceInfo) {
        return deviceInfo.getNodeInstanceIdentifier().augmentation(FlowCapableNode.class);
    }

    /**
     * Writes snapshot gathering start timestamp + cleans end mark
     *
     * @param deviceContext txManager + node path keeper
     */
    static void markDeviceStateSnapshotStart(final DeviceContext deviceContext) {
        final InstanceIdentifier<FlowCapableStatisticsGatheringStatus> statusPath = deviceContext.getDeviceInfo()
                .getNodeInstanceIdentifier().augmentation(FlowCapableStatisticsGatheringStatus.class);

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_AND_TIME_FORMAT);
        final FlowCapableStatisticsGatheringStatus gatheringStatus = new FlowCapableStatisticsGatheringStatusBuilder()
                .setSnapshotGatheringStatusStart(new SnapshotGatheringStatusStartBuilder()
                        .setBegin(new DateAndTime(simpleDateFormat.format(new Date())))
                        .build())
                .setSnapshotGatheringStatusEnd(null) // TODO: reconsider if really need to clean end mark here
                .build();
        try {
            deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, statusPath, gatheringStatus);
        } catch (final Exception e) {
            LOG.warn("Can't write to transaction: {}", e);
        }

        deviceContext.submitTransaction();
    }

    /**
     * Writes snapshot gathering end timestamp + outcome
     *
     * @param deviceContext txManager + node path keeper
     * @param succeeded     outcome of currently finished gathering
     */
    static void markDeviceStateSnapshotEnd(final DeviceContext deviceContext, final boolean succeeded) {
        final InstanceIdentifier<SnapshotGatheringStatusEnd> statusEndPath = deviceContext.getDeviceInfo()
                .getNodeInstanceIdentifier().augmentation(FlowCapableStatisticsGatheringStatus.class)
                .child(SnapshotGatheringStatusEnd.class);

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_AND_TIME_FORMAT);
        final SnapshotGatheringStatusEnd gatheringStatus = new SnapshotGatheringStatusEndBuilder()
                .setEnd(new DateAndTime(simpleDateFormat.format(new Date())))
                .setSucceeded(succeeded)
                .build();
        try {
            deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, statusEndPath, gatheringStatus);
        } catch (Exception e) {
            LOG.warn("Can't write to transaction: {}", e);
        }

        deviceContext.submitTransaction();
    }
}
