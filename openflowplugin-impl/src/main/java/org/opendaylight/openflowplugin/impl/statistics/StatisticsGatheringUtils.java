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
import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.binding.api.ReadTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryKey;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.StatisticsGatherer;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowRegistryKeyFactory;
import org.opendaylight.openflowplugin.impl.statistics.ofpspecific.EventsTimeCounter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
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
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 2.4.2015.
 */
public final class StatisticsGatheringUtils {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsGatheringUtils.class);
    private static final SinglePurposeMultipartReplyTranslator MULTIPART_REPLY_TRANSLATOR = new SinglePurposeMultipartReplyTranslator();
    public static final String QUEUE2_REQCTX = "QUEUE2REQCTX-";

    private StatisticsGatheringUtils() {
        throw new IllegalStateException("This class should not be instantiated.");
    }


    public static ListenableFuture<Boolean> gatherStatistics(final StatisticsGatherer statisticsGatheringService,
                                                             final DeviceContext deviceContext,
                                                             final MultipartType type) {
        final String deviceId = deviceContext.getPrimaryConnectionContext().getNodeId().toString();
        EventIdentifier wholeProcessEventIdentifier = null;
        if (MultipartType.OFPMPFLOW.equals(type)) {
            wholeProcessEventIdentifier = new EventIdentifier(type.toString(), deviceId);
            EventsTimeCounter.markStart(wholeProcessEventIdentifier);
        }
        EventIdentifier ofpQueuToRequestContextEventIdentifier = new EventIdentifier(QUEUE2_REQCTX + type.toString(), deviceId);
        final ListenableFuture<RpcResult<List<MultipartReply>>> statisticsDataInFuture =
                JdkFutureAdapters.listenInPoolThread(statisticsGatheringService.getStatisticsOfType(
                        ofpQueuToRequestContextEventIdentifier, type));
        return transformAndStoreStatisticsData(statisticsDataInFuture, deviceContext, wholeProcessEventIdentifier);
    }

    private static ListenableFuture<Boolean> transformAndStoreStatisticsData(final ListenableFuture<RpcResult<List<MultipartReply>>> statisticsDataInFuture,
                                                                             final DeviceContext deviceContext,
                                                                             final EventIdentifier eventIdentifier) {
        return Futures.transform(statisticsDataInFuture, new Function<RpcResult<List<MultipartReply>>, Boolean>() {
            @Nullable
            @Override
            public Boolean apply(final RpcResult<List<MultipartReply>> rpcResult) {
                if (rpcResult.isSuccessful()) {
                    boolean isMultipartProcessed = Boolean.TRUE;

                    // TODO: in case the result value is null then multipart data probably got processed on the fly -
                    // TODO: this contract should by clearly stated and enforced - now simple true value is returned
                    if (null != rpcResult.getResult()) {
                        Iterable<? extends DataObject> allMultipartData = Collections.emptyList();
                        DataObject multipartData = null;
                        for (final MultipartReply singleReply : rpcResult.getResult()) {
                            final List<? extends DataObject> multipartDataList = MULTIPART_REPLY_TRANSLATOR.translate(deviceContext, singleReply);
                            multipartData = multipartDataList.get(0);
                            allMultipartData = Iterables.concat(allMultipartData, multipartDataList);
                        }

                        if (multipartData instanceof GroupStatisticsUpdated) {
                            processGroupStatistics((Iterable<GroupStatisticsUpdated>) allMultipartData, deviceContext);
                        } else if (multipartData instanceof MeterStatisticsUpdated) {
                            processMetersStatistics((Iterable<MeterStatisticsUpdated>) allMultipartData, deviceContext);
                        } else if (multipartData instanceof NodeConnectorStatisticsUpdate) {
                            processNodeConnectorStatistics((Iterable<NodeConnectorStatisticsUpdate>) allMultipartData, deviceContext);
                        } else if (multipartData instanceof FlowTableStatisticsUpdate) {
                            processFlowTableStatistics((Iterable<FlowTableStatisticsUpdate>) allMultipartData, deviceContext);
                        } else if (multipartData instanceof QueueStatisticsUpdate) {
                            processQueueStatistics((Iterable<QueueStatisticsUpdate>) allMultipartData, deviceContext);
                        } else if (multipartData instanceof FlowsStatisticsUpdate) {
                            processFlowStatistics((Iterable<FlowsStatisticsUpdate>) allMultipartData, deviceContext);
                            EventsTimeCounter.markEnd(eventIdentifier);
                        } else if (multipartData instanceof GroupDescStatsUpdated) {
                            processGroupDescStats((Iterable<GroupDescStatsUpdated>) allMultipartData, deviceContext);
                        } else if (multipartData instanceof MeterConfigStatsUpdated) {
                            processMeterConfigStatsUpdated((Iterable<MeterConfigStatsUpdated>) allMultipartData, deviceContext);
                        } else {
                            isMultipartProcessed = Boolean.FALSE;
                        }
                        //TODO : implement experimenter
                    }

                    return isMultipartProcessed;
                }
                return Boolean.FALSE;
            }
        });
    }

    private static void processMeterConfigStatsUpdated(final Iterable<MeterConfigStatsUpdated> data, final DeviceContext deviceContext) {
        final InstanceIdentifier<FlowCapableNode> fNodeIdent = assembleFlowCapableNodeInstanceIdentifier(deviceContext);
        deleteAllKnownMeters(deviceContext, fNodeIdent);
        for (final MeterConfigStatsUpdated meterConfigStatsUpdated : data) {
            for (final MeterConfigStats meterConfigStats : meterConfigStatsUpdated.getMeterConfigStats()) {
                final MeterId meterId = meterConfigStats.getMeterId();
                final InstanceIdentifier<Meter> meterInstanceIdentifier = fNodeIdent.child(Meter.class, new MeterKey(meterId));

                final MeterBuilder meterBuilder = new MeterBuilder(meterConfigStats);
                meterBuilder.setKey(new MeterKey(meterId));
                meterBuilder.addAugmentation(NodeMeterStatistics.class, new NodeMeterStatisticsBuilder().build());
                deviceContext.getDeviceMeterRegistry().store(meterId);
                deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, meterInstanceIdentifier, meterBuilder.build());
            }
        }
        deviceContext.submitTransaction();
    }

    private static void processFlowStatistics(final Iterable<FlowsStatisticsUpdate> data, final DeviceContext deviceContext) {
        deleteAllKnownFlows(deviceContext);
        writeFlowStatistics(data, deviceContext);
        deviceContext.submitTransaction();
    }

    public static void writeFlowStatistics(Iterable<FlowsStatisticsUpdate> data, DeviceContext deviceContext) {
        final InstanceIdentifier<FlowCapableNode> fNodeIdent = assembleFlowCapableNodeInstanceIdentifier(deviceContext);
        for (final FlowsStatisticsUpdate flowsStatistics : data) {
            for (final FlowAndStatisticsMapList flowStat : flowsStatistics.getFlowAndStatisticsMapList()) {
                final FlowBuilder flowBuilder = new FlowBuilder(flowStat);
                flowBuilder.addAugmentation(FlowStatisticsData.class, refineFlowStatisticsAugmentation(flowStat).build());

                final short tableId = flowStat.getTableId();
                final FlowRegistryKey flowRegistryKey = FlowRegistryKeyFactory.create(flowBuilder.build());
                final FlowId flowId = deviceContext.getDeviceFlowRegistry().storeIfNecessary(flowRegistryKey, tableId);

                final FlowKey flowKey = new FlowKey(flowId);
                flowBuilder.setKey(flowKey);
                final TableKey tableKey = new TableKey(tableId);
                final InstanceIdentifier<Flow> flowIdent = fNodeIdent.child(Table.class, tableKey).child(Flow.class, flowKey);
                deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, flowIdent, flowBuilder.build());
            }
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

    public static void deleteAllKnownFlows(final DeviceContext deviceContext) {
        if (deviceContext.getDeviceState().deviceSynchronized()) {
            InstanceIdentifier<FlowCapableNode> flowCapableNodePath = assembleFlowCapableNodeInstanceIdentifier(deviceContext);
            final Short numOfTablesOnDevice = deviceContext.getDeviceState().getFeatures().getTables();
            for (short i = 0; i < numOfTablesOnDevice; i++) {
                final KeyedInstanceIdentifier<Table, TableKey> iiToTable = flowCapableNodePath.child(Table.class, new TableKey(i));
                final ReadTransaction readTx = deviceContext.getReadTransaction();
                final CheckedFuture<Optional<Table>, ReadFailedException> tableDataFuture = readTx.read(LogicalDatastoreType.OPERATIONAL, iiToTable);
                try {
                    final Optional<Table> tableDataOpt = tableDataFuture.get();
                    if (tableDataOpt.isPresent()) {
                        final Table tableData = tableDataOpt.get();
                        final Table table = new TableBuilder(tableData).setFlow(Collections.<Flow>emptyList()).build();
                        deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, iiToTable, table);
                    }
                } catch (final InterruptedException e) {
                    LOG.trace("Reading of table features for table wit ID {} was interrputed.", i);
                } catch (final ExecutionException e) {
                    LOG.trace("Reading of table features for table wit ID {} encountered execution exception {}.", i, e);
                }
            }
        }
    }

    private static void processQueueStatistics(final Iterable<QueueStatisticsUpdate> data, final DeviceContext deviceContext) {
        // TODO: clean all queues of all node-connectors before writing up-to-date stats
        final InstanceIdentifier<Node> nodeIdent = deviceContext.getDeviceState().getNodeInstanceIdentifier();
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
                    deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, queueIdent, queueBuilder.build());
                }
            }
        }
        deviceContext.submitTransaction();
    }

    private static void processFlowTableStatistics(final Iterable<FlowTableStatisticsUpdate> data, final DeviceContext deviceContext) {
        final InstanceIdentifier<FlowCapableNode> fNodeIdent = assembleFlowCapableNodeInstanceIdentifier(deviceContext);
        for (final FlowTableStatisticsUpdate flowTableStatisticsUpdate : data) {

            for (final FlowTableAndStatisticsMap tableStat : flowTableStatisticsUpdate.getFlowTableAndStatisticsMap()) {
                final InstanceIdentifier<FlowTableStatistics> tStatIdent = fNodeIdent.child(Table.class, new TableKey(tableStat.getTableId().getValue()))
                        .augmentation(FlowTableStatisticsData.class).child(FlowTableStatistics.class);
                final FlowTableStatistics stats = new FlowTableStatisticsBuilder(tableStat).build();
                deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, tStatIdent, stats);
            }
        }
        deviceContext.submitTransaction();
    }

    private static void processNodeConnectorStatistics(final Iterable<NodeConnectorStatisticsUpdate> data, final DeviceContext deviceContext) {
        final InstanceIdentifier<Node> nodeIdent = deviceContext.getDeviceState().getNodeInstanceIdentifier();
        for (final NodeConnectorStatisticsUpdate nodeConnectorStatisticsUpdate : data) {
            for (final NodeConnectorStatisticsAndPortNumberMap nConnectPort : nodeConnectorStatisticsUpdate.getNodeConnectorStatisticsAndPortNumberMap()) {
                final FlowCapableNodeConnectorStatistics stats = new FlowCapableNodeConnectorStatisticsBuilder(nConnectPort).build();
                final NodeConnectorKey key = new NodeConnectorKey(nConnectPort.getNodeConnectorId());
                final InstanceIdentifier<NodeConnector> nodeConnectorIdent = nodeIdent.child(NodeConnector.class, key);
                final InstanceIdentifier<FlowCapableNodeConnectorStatisticsData> nodeConnStatIdent = nodeConnectorIdent
                        .augmentation(FlowCapableNodeConnectorStatisticsData.class);
                final InstanceIdentifier<FlowCapableNodeConnectorStatistics> flowCapNodeConnStatIdent =
                        nodeConnStatIdent.child(FlowCapableNodeConnectorStatistics.class);
                deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, flowCapNodeConnStatIdent, stats);
            }
        }
        deviceContext.submitTransaction();
    }

    private static void processMetersStatistics(final Iterable<MeterStatisticsUpdated> data,
                                                final DeviceContext deviceContext) {
        final InstanceIdentifier<FlowCapableNode> fNodeIdent = assembleFlowCapableNodeInstanceIdentifier(deviceContext);
        for (final MeterStatisticsUpdated meterStatisticsUpdated : data) {
            for (final MeterStats mStat : meterStatisticsUpdated.getMeterStats()) {
                final MeterStatistics stats = new MeterStatisticsBuilder(mStat).build();
                final MeterId meterId = mStat.getMeterId();
                final InstanceIdentifier<Meter> meterIdent = fNodeIdent.child(Meter.class, new MeterKey(meterId));
                final InstanceIdentifier<NodeMeterStatistics> nodeMeterStatIdent = meterIdent
                        .augmentation(NodeMeterStatistics.class);
                final InstanceIdentifier<MeterStatistics> msIdent = nodeMeterStatIdent.child(MeterStatistics.class);
                deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, msIdent, stats);
            }
        }
        deviceContext.submitTransaction();
    }

    private static void deleteAllKnownMeters(final DeviceContext deviceContext, final InstanceIdentifier<FlowCapableNode> fNodeIdent) {
        for (final MeterId meterId : deviceContext.getDeviceMeterRegistry().getAllMeterIds()) {
            final InstanceIdentifier<Meter> meterIdent = fNodeIdent.child(Meter.class, new MeterKey(meterId));
            deviceContext.addDeleteToTxChain(LogicalDatastoreType.OPERATIONAL, meterIdent);
        }
        deviceContext.getDeviceMeterRegistry().removeMarked();
    }

    private static void processGroupDescStats(final Iterable<GroupDescStatsUpdated> data, final DeviceContext deviceContext) {
        final InstanceIdentifier<FlowCapableNode> fNodeIdent =
                deviceContext.getDeviceState().getNodeInstanceIdentifier().augmentation(FlowCapableNode.class);
        deleteAllKnownGroups(deviceContext, fNodeIdent);

        for (final GroupDescStatsUpdated groupDescStatsUpdated : data) {
            for (final GroupDescStats groupDescStats : groupDescStatsUpdated.getGroupDescStats()) {
                final GroupId groupId = groupDescStats.getGroupId();

                final GroupBuilder groupBuilder = new GroupBuilder(groupDescStats);
                groupBuilder.setKey(new GroupKey(groupId));
                groupBuilder.addAugmentation(NodeGroupStatistics.class, new NodeGroupStatisticsBuilder().build());

                final InstanceIdentifier<Group> groupIdent = fNodeIdent.child(Group.class, new GroupKey(groupId));

                deviceContext.getDeviceGroupRegistry().store(groupId);
                deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, groupIdent, groupBuilder.build());
            }
        }
        deviceContext.submitTransaction();
    }

    private static void deleteAllKnownGroups(final DeviceContext deviceContext, final InstanceIdentifier<FlowCapableNode> fNodeIdent) {
        for (final GroupId groupId : deviceContext.getDeviceGroupRegistry().getAllGroupIds()) {
            final InstanceIdentifier<Group> groupIdent = fNodeIdent.child(Group.class, new GroupKey(groupId));
            deviceContext.addDeleteToTxChain(LogicalDatastoreType.OPERATIONAL, groupIdent);
        }
        deviceContext.getDeviceGroupRegistry().removeMarked();
    }

    private static void processGroupStatistics(final Iterable<GroupStatisticsUpdated> data, final DeviceContext deviceContext) {
        final InstanceIdentifier<FlowCapableNode> fNodeIdent = assembleFlowCapableNodeInstanceIdentifier(deviceContext);
        for (final GroupStatisticsUpdated groupStatistics : data) {
            for (final GroupStats groupStats : groupStatistics.getGroupStats()) {

                final InstanceIdentifier<Group> groupIdent = fNodeIdent.child(Group.class, new GroupKey(groupStats.getGroupId()));
                final InstanceIdentifier<NodeGroupStatistics> nGroupStatIdent = groupIdent
                        .augmentation(NodeGroupStatistics.class);

                final InstanceIdentifier<GroupStatistics> gsIdent = nGroupStatIdent.child(GroupStatistics.class);
                final GroupStatistics stats = new GroupStatisticsBuilder(groupStats).build();
                deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, gsIdent, stats);
            }
        }
        deviceContext.submitTransaction();
    }

    private static InstanceIdentifier<FlowCapableNode> assembleFlowCapableNodeInstanceIdentifier(final DeviceContext deviceContext) {
        return deviceContext.getDeviceState().getNodeInstanceIdentifier().augmentation(FlowCapableNode.class);
    }
}
