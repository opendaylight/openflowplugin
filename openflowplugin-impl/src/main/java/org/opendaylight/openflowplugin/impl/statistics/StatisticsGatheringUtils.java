/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.JdkFutureAdapters;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowDescriptor;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowHash;
import org.opendaylight.openflowplugin.api.openflow.registry.flow.FlowRegistryException;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowDescriptorFactory;
import org.opendaylight.openflowplugin.impl.registry.flow.FlowHashFactory;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.StatisticsGatheringService;
import org.opendaylight.openflowplugin.impl.util.FlowUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.statistics.FlowTableStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.statistics.FlowTableStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.Queue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.QueueKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupDescStatsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupStatisticsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.group.statistics.GroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.group.statistics.GroupStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.desc.stats.reply.GroupDescStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterConfigStatsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterStatisticsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterStatistics;
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

    private StatisticsGatheringUtils() {
        throw new IllegalStateException("This class should not be instantiated.");
    }

    private static KeyedInstanceIdentifier<Node, NodeKey> getInstanceIdentifier(final DeviceContext deviceContext) {
        return InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(deviceContext.getPrimaryConnectionContext().getNodeId()));
    }

    public static ListenableFuture<Boolean> gatherStatistics(final StatisticsGatheringService statisticsGatheringService,
                                                             final DeviceContext deviceContext,
                                                             final MultipartType type) {
        //FIXME : anytype listener must not be send as parameter, it has to be extracted from device context inside service
        final ListenableFuture<RpcResult<List<MultipartReply>>> statisticsDataInFuture =
                JdkFutureAdapters.listenInPoolThread(statisticsGatheringService.getStatisticsOfType(type));
        return transformAndStoreStatisticsData(statisticsDataInFuture, deviceContext);
    }

    private static ListenableFuture<Boolean> transformAndStoreStatisticsData(final ListenableFuture<RpcResult<List<MultipartReply>>> statisticsDataInFuture,
                                                                             final DeviceContext deviceContext) {
        return Futures.transform(statisticsDataInFuture, new Function<RpcResult<List<MultipartReply>>, Boolean>() {
            @Nullable
            @Override
            public Boolean apply(final RpcResult<List<MultipartReply>> rpcResult) {
                if (rpcResult.isSuccessful()) {
                    for (final MultipartReply singleReply : rpcResult.getResult()) {
                        final List<? extends DataObject> multipartDataList = MULTIPART_REPLY_TRANSLATOR.translate(deviceContext, singleReply);
                        for (final DataObject singleMultipartData : multipartDataList) {
                            if (singleMultipartData instanceof GroupStatisticsUpdated) {
                                processGroupStatistics((GroupStatisticsUpdated) singleMultipartData, deviceContext);
                            }

                            if (singleMultipartData instanceof MeterStatisticsUpdated) {
                                processMetersStatistics((MeterStatisticsUpdated) singleMultipartData, deviceContext);
                            }
                            if (singleMultipartData instanceof NodeConnectorStatisticsUpdate) {
                                processNodeConnectorStatistics((NodeConnectorStatisticsUpdate) singleMultipartData, deviceContext);
                            }
                            if (singleMultipartData instanceof FlowTableStatisticsUpdate) {
                                processFlowTableStatistics((FlowTableStatisticsUpdate) singleMultipartData, deviceContext);
                            }
                            if (singleMultipartData instanceof QueueStatisticsUpdate) {
                                processQueueStatistics((QueueStatisticsUpdate) singleMultipartData, deviceContext);
                            }
                            if (singleMultipartData instanceof FlowsStatisticsUpdate) {
                                processFlowStatistics((FlowsStatisticsUpdate) singleMultipartData, deviceContext);
                            }
                            if (singleMultipartData instanceof GroupDescStatsUpdated) {
                                processGroupDescStats((GroupDescStatsUpdated) singleMultipartData, deviceContext);
                            }
                            if (singleMultipartData instanceof MeterConfigStatsUpdated) {
                                processMeterConfigStatsUpdated((MeterConfigStatsUpdated) singleMultipartData, deviceContext);
                            }

                            //TODO : implement experimenter
                        }
                    }
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
        });
    }

    private static void processMeterConfigStatsUpdated(final MeterConfigStatsUpdated meterConfigStatsUpdated, final DeviceContext deviceContext) {
        NodeId nodeId = meterConfigStatsUpdated.getId();
        final InstanceIdentifier<FlowCapableNode> fNodeIdent = getFlowCapableNodeInstanceIdentifier(nodeId);
        deleteAllKnownMeters(deviceContext, fNodeIdent);
        for (MeterConfigStats meterConfigStats : meterConfigStatsUpdated.getMeterConfigStats()) {
            final MeterBuilder meterBuilder = new MeterBuilder(meterConfigStats);
            final MeterId meterId = meterConfigStats.getMeterId();
            final InstanceIdentifier<Meter> meterInstanceIdentifier = fNodeIdent.child(Meter.class, new MeterKey(meterId));
            deviceContext.getDeviceMeterRegistry().store(meterId);
            deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, meterInstanceIdentifier, meterBuilder.build());
        }

    }

    private static void processFlowStatistics(final FlowsStatisticsUpdate singleMultipartData, final DeviceContext deviceContext) {
        final FlowsStatisticsUpdate flowsStatistics = singleMultipartData;
        final InstanceIdentifier<Node> nodeIdent = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(flowsStatistics.getId()));

        deleteAllKnownFlows(deviceContext, nodeIdent);

        for (final FlowAndStatisticsMapList flowStat : flowsStatistics.getFlowAndStatisticsMapList()) {
            final FlowBuilder flowBuilder = new FlowBuilder(flowStat);
            FlowId flowId = null;
            FlowHash flowHash = FlowHashFactory.create(flowBuilder.build());
            short tableId = flowStat.getTableId();
            try {
                FlowDescriptor flowDescriptor = deviceContext.getDeviceFlowRegistry().retrieveIdForFlow(flowHash);
                flowId = flowDescriptor.getFlowId();
            } catch (FlowRegistryException e) {
                LOG.trace("Flow descriptor for flow hash {} wasn't found.", flowHash.hashCode());
                flowId = FlowUtil.createAlienFlowId(tableId);
                FlowDescriptor flowDescriptor = FlowDescriptorFactory.create(tableId, flowId);
                deviceContext.getDeviceFlowRegistry().store(flowHash, flowDescriptor);
            }
            FlowKey flowKey = new FlowKey(flowId);
            flowBuilder.setKey(flowKey);
            final TableKey tableKey = new TableKey(tableId);
            final InstanceIdentifier<FlowCapableNode> fNodeIdent = getFlowCapableNodeInstanceIdentifier(singleMultipartData.getId());
            final InstanceIdentifier<Flow> flowIdent = fNodeIdent.child(Table.class, tableKey).child(Flow.class, flowKey);
            deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, flowIdent, flowBuilder.build());
        }
    }

    private static void deleteAllKnownFlows(final DeviceContext deviceContext, final InstanceIdentifier<Node> nodeIdent) {
        if (deviceContext.getDeviceState().deviceSynchronized()) {
            for (Map.Entry<FlowHash, FlowDescriptor> registryEntry : deviceContext.getDeviceFlowRegistry().getAllFlowDescriptors().entrySet()) {
                FlowDescriptor flowDescriptor = registryEntry.getValue();

                FlowId flowId = flowDescriptor.getFlowId();
                FlowKey flowKey = new FlowKey(flowId);
                final InstanceIdentifier<Flow> flowInstanceIdentifier = nodeIdent
                        .augmentation(FlowCapableNode.class)
                        .child(Table.class, flowDescriptor.getTableKey())
                        .child(Flow.class, flowKey);

                LOG.trace("Deleting flow with id {}", flowInstanceIdentifier);
                deviceContext.addDeleteToTxChain(LogicalDatastoreType.OPERATIONAL, flowInstanceIdentifier);
            }
        }
        deviceContext.getDeviceFlowRegistry().removeMarked();
    }

    private static void processQueueStatistics(final QueueStatisticsUpdate singleMultipartData, final DeviceContext deviceContext) {
        final QueueStatisticsUpdate queueStatisticsUpdate = singleMultipartData;
        final InstanceIdentifier<Node> nodeIdent = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(queueStatisticsUpdate.getId()));
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
                final InstanceIdentifier<FlowCapableNodeConnectorQueueStatisticsData> queueStatIdent = queueIdent.augmentation(FlowCapableNodeConnectorQueueStatisticsData.class);
                deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, queueStatIdent, statBuild.build());
            }
        }
    }

    private static void processFlowTableStatistics(final FlowTableStatisticsUpdate singleMultipartData, final DeviceContext deviceContext) {
        final FlowTableStatisticsUpdate flowTableStatisticsUpdate = singleMultipartData;
        final InstanceIdentifier<FlowCapableNode> fNodeIdent = getFlowCapableNodeInstanceIdentifier(flowTableStatisticsUpdate.getId());

        for (final FlowTableAndStatisticsMap tableStat : flowTableStatisticsUpdate.getFlowTableAndStatisticsMap()) {
            final InstanceIdentifier<FlowTableStatistics> tStatIdent = fNodeIdent.child(Table.class, new TableKey(tableStat.getTableId().getValue()))
                    .augmentation(FlowTableStatisticsData.class).child(FlowTableStatistics.class);
            final FlowTableStatistics stats = new FlowTableStatisticsBuilder(tableStat).build();
            deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, tStatIdent, stats);
        }
    }

    private static void processNodeConnectorStatistics(final NodeConnectorStatisticsUpdate singleMultipartData, final DeviceContext deviceContext) {
        final NodeConnectorStatisticsUpdate nodeConnectorStatisticsUpdate = singleMultipartData;
        final InstanceIdentifier<Node> nodeIdent = InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(nodeConnectorStatisticsUpdate.getId()));
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

    private static void processMetersStatistics(final MeterStatisticsUpdated singleMultipartData, final DeviceContext deviceContext) {
        final MeterStatisticsUpdated meterStatisticsUpdated = singleMultipartData;
        final InstanceIdentifier<FlowCapableNode> fNodeIdent = getFlowCapableNodeInstanceIdentifier(meterStatisticsUpdated.getId());


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

    private static void deleteAllKnownMeters(final DeviceContext deviceContext, final InstanceIdentifier<FlowCapableNode> fNodeIdent) {
        for (MeterId meterId : deviceContext.getDeviceMeterRegistry().getAllMeterIds()) {
            final InstanceIdentifier<Meter> meterIdent = fNodeIdent.child(Meter.class, new MeterKey(meterId));
            deviceContext.addDeleteToTxChain(LogicalDatastoreType.OPERATIONAL, meterIdent);
        }
        deviceContext.getDeviceMeterRegistry().removeMarked();
    }

    private static void processGroupDescStats(GroupDescStatsUpdated groupDescStatsUpdated, final DeviceContext deviceContext) {
        NodeId nodeId = groupDescStatsUpdated.getId();
        final InstanceIdentifier<FlowCapableNode> fNodeIdent = getFlowCapableNodeInstanceIdentifier(nodeId);
        deleteAllKnownGroups(deviceContext, fNodeIdent);
        for (GroupDescStats groupDescStats : groupDescStatsUpdated.getGroupDescStats()) {
            final GroupBuilder groupBuilder = new GroupBuilder(groupDescStats);
            final GroupId groupId = groupDescStats.getGroupId();
            final InstanceIdentifier<Group> groupIdent = fNodeIdent.child(Group.class, new GroupKey(groupId));
            deviceContext.getDeviceGroupRegistry().store(groupId);
            deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, groupIdent, groupBuilder.build());
        }
    }

    private static void deleteAllKnownGroups(final DeviceContext deviceContext, final InstanceIdentifier<FlowCapableNode> fNodeIdent) {
        for (GroupId groupId : deviceContext.getDeviceGroupRegistry().getAllGroupIds()) {
            final InstanceIdentifier<Group> groupIdent = fNodeIdent.child(Group.class, new GroupKey(groupId));
            deviceContext.addDeleteToTxChain(LogicalDatastoreType.OPERATIONAL, groupIdent);
        }
        deviceContext.getDeviceGroupRegistry().removeMarked();
    }

    private static void processGroupStatistics(final GroupStatisticsUpdated singleMultipartData, final DeviceContext deviceContext) {
        final GroupStatisticsUpdated groupStatistics = singleMultipartData;
        NodeId nodeId = groupStatistics.getId();
        final InstanceIdentifier<FlowCapableNode> fNodeIdent = getFlowCapableNodeInstanceIdentifier(nodeId);

        for (final GroupStats groupStats : groupStatistics.getGroupStats()) {

            final InstanceIdentifier<Group> groupIdent = fNodeIdent.child(Group.class, new GroupKey(groupStats.getGroupId()));
            final InstanceIdentifier<NodeGroupStatistics> nGroupStatIdent = groupIdent
                    .augmentation(NodeGroupStatistics.class);

            final InstanceIdentifier<GroupStatistics> gsIdent = nGroupStatIdent.child(GroupStatistics.class);
            final GroupStatistics stats = new GroupStatisticsBuilder(groupStats).build();
            deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, gsIdent, stats);
        }
    }

    private static InstanceIdentifier<FlowCapableNode> getFlowCapableNodeInstanceIdentifier(final NodeId nodeId) {
        final InstanceIdentifier<Node> nodeIdent = InstanceIdentifier
                .create(Nodes.class).child(Node.class, new NodeKey(nodeId));
        return nodeIdent.augmentation(FlowCapableNode.class);
    }
}
