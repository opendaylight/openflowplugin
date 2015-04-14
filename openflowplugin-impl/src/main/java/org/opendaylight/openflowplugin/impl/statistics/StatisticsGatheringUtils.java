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
import org.opendaylight.openflowplugin.impl.flow.registry.FlowDescriptorFactory;
import org.opendaylight.openflowplugin.impl.flow.registry.FlowHashFactory;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.StatisticsGatheringService;
import org.opendaylight.openflowplugin.impl.util.FlowUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupDescStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupDescStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.group.desc.GroupDescBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.desc.stats.reply.GroupDescStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.MeterStatisticsUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.nodes.node.meter.MeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.nodes.node.meter.MeterStatisticsBuilder;
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
                    final InstanceIdentifier basicIId = getInstanceIdentifier(deviceContext);
                    for (final MultipartReply singleReply : rpcResult.getResult()) {
                        final List<? extends DataObject> multipartDataList = MULTIPART_REPLY_TRANSLATOR.translate(deviceContext, singleReply);
                        for (final DataObject singleMultipartData : multipartDataList) {
                            boolean logFirstTime = true;
                            if (singleMultipartData instanceof GroupDescStatsUpdated) {
                                final GroupDescStatsUpdated groupDescStatsUpdated = (GroupDescStatsUpdated) singleMultipartData;
                                for (final GroupDescStats groupDescStats : groupDescStatsUpdated.getGroupDescStats()) {
                                    final InstanceIdentifier<Node> nodeIdent = InstanceIdentifier
                                            .create(Nodes.class).child(Node.class, new NodeKey(groupDescStatsUpdated.getId()));
                                    final InstanceIdentifier<FlowCapableNode> fNodeIdent = nodeIdent.augmentation(FlowCapableNode.class);
                                    final GroupKey groupKey = new GroupKey(groupDescStats.getGroupId());
                                    final InstanceIdentifier<Group> groupRef = fNodeIdent.child(Group.class, groupKey);
                                    final GroupBuilder groupBuilder = new GroupBuilder(groupDescStats);
                                    final NodeGroupDescStatsBuilder groupDesc = new NodeGroupDescStatsBuilder();
                                    groupDesc.setGroupDesc(new GroupDescBuilder(groupDescStats).build());
                                    groupBuilder.addAugmentation(NodeGroupDescStats.class, groupDesc.build());
                                    deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, groupRef, groupBuilder.build());
                                }
                            }

                            if (singleMultipartData instanceof MeterStatisticsUpdated) {
                                final MeterStatisticsUpdated meterStatisticsUpdated = (MeterStatisticsUpdated) singleMultipartData;
                                final InstanceIdentifier<Node> nodeIdent = InstanceIdentifier
                                        .create(Nodes.class).child(Node.class, new NodeKey(meterStatisticsUpdated.getId()));
                                final InstanceIdentifier<FlowCapableNode> fNodeIdent = nodeIdent.augmentation(FlowCapableNode.class);

                                for (final MeterStats mStat : meterStatisticsUpdated.getMeterStats()) {
                                    final MeterStatistics stats = new MeterStatisticsBuilder(mStat).build();

                                    final InstanceIdentifier<Meter> meterIdent = fNodeIdent.child(Meter.class, new MeterKey(mStat.getMeterId()));
                                    final InstanceIdentifier<NodeMeterStatistics> nodeMeterStatIdent = meterIdent
                                            .augmentation(NodeMeterStatistics.class);
                                    final InstanceIdentifier<MeterStatistics> msIdent = nodeMeterStatIdent.child(MeterStatistics.class);
                                    deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, msIdent, stats);
                                }
                            }
                            if (singleMultipartData instanceof NodeConnectorStatisticsUpdate) {
                                final NodeConnectorStatisticsUpdate nodeConnectorStatisticsUpdate = (NodeConnectorStatisticsUpdate) singleMultipartData;
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
                            if (singleMultipartData instanceof FlowTableStatisticsUpdate) {

                                final FlowTableStatisticsUpdate flowTableStatisticsUpdate = (FlowTableStatisticsUpdate) singleMultipartData;
                                final InstanceIdentifier<FlowCapableNode> fNodeIdent = InstanceIdentifier.create(Nodes.class)
                                        .child(Node.class, new NodeKey(flowTableStatisticsUpdate.getId())).augmentation(FlowCapableNode.class);

                                for (final FlowTableAndStatisticsMap tableStat : flowTableStatisticsUpdate.getFlowTableAndStatisticsMap()) {
                                    final InstanceIdentifier<FlowTableStatistics> tStatIdent = fNodeIdent.child(Table.class, new TableKey(tableStat.getTableId().getValue()))
                                            .augmentation(FlowTableStatisticsData.class).child(FlowTableStatistics.class);
                                    final FlowTableStatistics stats = new FlowTableStatisticsBuilder(tableStat).build();
                                    deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, tStatIdent, stats);
                                }
                            }
                            if (singleMultipartData instanceof QueueStatisticsUpdate) {
                                final QueueStatisticsUpdate queueStatisticsUpdate = (QueueStatisticsUpdate) singleMultipartData;
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
                            if (singleMultipartData instanceof FlowsStatisticsUpdate) {
                                final FlowsStatisticsUpdate flowsStatistics = (FlowsStatisticsUpdate) singleMultipartData;
                                final InstanceIdentifier<Node> nodeIdent = InstanceIdentifier.create(Nodes.class)
                                        .child(Node.class, new NodeKey(flowsStatistics.getId()));

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
                                    final InstanceIdentifier<FlowCapableNode> fNodeIdent = nodeIdent.augmentation(FlowCapableNode.class);
                                    final InstanceIdentifier<Flow> flowIdent = fNodeIdent.child(Table.class, tableKey).child(Flow.class, flowKey);
                                    deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, flowIdent, flowBuilder.build());
                                }
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
}
