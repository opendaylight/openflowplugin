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
import javax.annotation.Nullable;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.handlers.MultiMsgCollector;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.StatisticsGatheringService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.FlowsStatisticsUpdate;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.FlowTableStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.and.statistics.map.FlowTableAndStatisticsMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.statistics.FlowTableStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.flow.table.statistics.FlowTableStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.Queue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.port.rev130925.queues.QueueKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.NodeGroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.group.statistics.GroupStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.group.statistics.GroupStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.statistics.reply.GroupStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.NodeMeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.nodes.node.meter.MeterStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.nodes.node.meter.MeterStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.flow.capable.node.connector.statistics.FlowCapableNodeConnectorStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.flow.capable.node.connector.statistics.FlowCapableNodeConnectorStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.node.connector.statistics.and.port.number.map.NodeConnectorStatisticsAndPortNumberMap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.FlowCapableNodeConnectorQueueStatisticsData;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.FlowCapableNodeConnectorQueueStatisticsDataBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.flow.capable.node.connector.queue.statistics.FlowCapableNodeConnectorQueueStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.flow.capable.node.connector.queue.statistics.FlowCapableNodeConnectorQueueStatisticsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.queue.id.and.statistics.map.QueueIdAndStatisticsMap;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 2.4.2015.
 */
public final class StatisticsGatheringUtils {

    private static final SinglePurposeMultipartReplyTranslator MULTIPART_REPLY_TRANSLATOR = new SinglePurposeMultipartReplyTranslator();

    private StatisticsGatheringUtils() {
        throw new IllegalStateException("This class should not be instantiated.");
    }

    private static KeyedInstanceIdentifier<Node, NodeKey> getInstanceIdentifier(final DeviceContext deviceContext) {
        return InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(deviceContext.getPrimaryConnectionContext().getNodeId()));
    }

    public static ListenableFuture<Boolean> gatherQueueStatistics(final StatisticsGatheringService statisticsGatheringService,
                                                                  final DeviceContext deviceContext,
                                                                  final MultiMsgCollector multiMsgCollector) {
        ListenableFuture<RpcResult<List<MultipartReply>>> statisticsDataInFuture =
                JdkFutureAdapters.listenInPoolThread(statisticsGatheringService.getStatisticsOfType(MultipartType.OFPMPQUEUE, multiMsgCollector));
        return transformAndStoreStatisticsData(statisticsDataInFuture, deviceContext);
    }


    public static ListenableFuture<Boolean> gatherPortStatistics(final StatisticsGatheringService statisticsGatheringService,
                                                                 final DeviceContext deviceContext,
                                                                 final MultiMsgCollector multiMsgCollector) {

        ListenableFuture<RpcResult<List<MultipartReply>>> statisticsDataInFuture =
                JdkFutureAdapters.
                        listenInPoolThread(statisticsGatheringService.getStatisticsOfType(MultipartType.OFPMPPORTSTATS, multiMsgCollector));

        return transformAndStoreStatisticsData(statisticsDataInFuture, deviceContext);
    }

    public static ListenableFuture<Boolean> gatherMeterStatistics(final StatisticsGatheringService statisticsGatheringService,
                                                                  final DeviceContext deviceContext,
                                                                  final MultiMsgCollector multiMsgCollector) {
        ListenableFuture<RpcResult<List<MultipartReply>>> statisticsDataInFuture =
                JdkFutureAdapters.
                        listenInPoolThread(statisticsGatheringService.
                                getStatisticsOfType(MultipartType.OFPMPMETER, multiMsgCollector));
        return transformAndStoreStatisticsData(statisticsDataInFuture, deviceContext);
    }


    public static ListenableFuture<Boolean> gatherGroupStatistics(final StatisticsGatheringService statisticsGatheringService,
                                                                  final DeviceContext deviceContext,
                                                                  final MultiMsgCollector multiMsgCollector) {
        ListenableFuture<RpcResult<List<MultipartReply>>> statisticsDataInFuture =
                JdkFutureAdapters.
                        listenInPoolThread(statisticsGatheringService.
                                getStatisticsOfType(MultipartType.OFPMPGROUPDESC, multiMsgCollector));
        return transformAndStoreStatisticsData(statisticsDataInFuture, deviceContext);
    }

    public static ListenableFuture<Boolean> gatherFlowStatistics(final StatisticsGatheringService statisticsGatheringService,
                                                                 final DeviceContext deviceContext,
                                                                 final MultiMsgCollector multiMsgCollector) {
        ListenableFuture<RpcResult<List<MultipartReply>>> statisticsDataInFuture =
                JdkFutureAdapters.
                        listenInPoolThread(statisticsGatheringService.
                                getStatisticsOfType(MultipartType.OFPMPFLOW, multiMsgCollector));
        return transformAndStoreStatisticsData(statisticsDataInFuture, deviceContext);
    }

    public static ListenableFuture<Boolean> gatherTableStatistics(final StatisticsGatheringService statisticsGatheringService,
                                                                  final DeviceContext deviceContext,
                                                                  final MultiMsgCollector multiMsgCollector) {
        ListenableFuture<RpcResult<List<MultipartReply>>> statisticsDataInFuture =
                JdkFutureAdapters.listenInPoolThread(
                        statisticsGatheringService.getStatisticsOfType(MultipartType.OFPMPTABLE, multiMsgCollector));
        return transformAndStoreStatisticsData(statisticsDataInFuture, deviceContext);
    }

    private static ListenableFuture<Boolean> transformAndStoreStatisticsData(final ListenableFuture<RpcResult<List<MultipartReply>>> statisticsDataInFuture, final DeviceContext deviceContext) {
        return Futures.transform(statisticsDataInFuture, new Function<RpcResult<List<MultipartReply>>, Boolean>() {
            @Nullable
            @Override
            public Boolean apply(final RpcResult<List<MultipartReply>> rpcResult) {
                if (rpcResult.isSuccessful()) {
                    InstanceIdentifier basicIId = getInstanceIdentifier(deviceContext);
                    for (MultipartReply singleReply : rpcResult.getResult()) {
                        List<? extends DataObject> multipartDataList = MULTIPART_REPLY_TRANSLATOR.translate(deviceContext, singleReply);
                        for (DataObject singleMultipartData : multipartDataList) {
                            MultipartReplyMessage mpReply = (MultipartReplyMessage) singleMultipartData;
                            switch (mpReply.getType()) {
                                case OFPMPGROUPDESC:
                                    final GroupStatistics groupStatistics = new GroupStatisticsBuilder((GroupStats) singleMultipartData).build();
                                    final InstanceIdentifier<Group> groupIdent = basicIId.child(Group.class, new GroupKey(groupStatistics.getGroupId()));
                                    final InstanceIdentifier<NodeGroupStatistics> nGroupStatIdent = groupIdent
                                            .augmentation(NodeGroupStatistics.class);
                                    final InstanceIdentifier<GroupStatistics> gsIdent = nGroupStatIdent.child(GroupStatistics.class);
                                    deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, gsIdent, groupStatistics);
                                    break;
                                case OFPMPMETERCONFIG:
                                    final MeterStatistics stats = new MeterStatisticsBuilder((MeterStats) singleMultipartData).build();
                                    final InstanceIdentifier<Meter> meterIdent = basicIId.child(Meter.class, new MeterKey(stats.getMeterId()));
                                    final InstanceIdentifier<NodeMeterStatistics> nodeMeterStatIdent = meterIdent
                                            .augmentation(NodeMeterStatistics.class);
                                    final InstanceIdentifier<MeterStatistics> msIdent = nodeMeterStatIdent.child(MeterStatistics.class);
                                    deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, msIdent, stats);
                                    break;
                                case OFPMPPORTDESC:
                                    NodeConnectorStatisticsAndPortNumberMap nodeConnectorStatisticsAndPortNumberMap = (NodeConnectorStatisticsAndPortNumberMap) singleMultipartData;
                                    final FlowCapableNodeConnectorStatistics nodeConnectorStatistics = new FlowCapableNodeConnectorStatisticsBuilder(nodeConnectorStatisticsAndPortNumberMap).build();
                                    final NodeConnectorKey key = new NodeConnectorKey(nodeConnectorStatisticsAndPortNumberMap.getNodeConnectorId());
                                    final InstanceIdentifier<NodeConnector> nodeConnectorIdent = basicIId.child(NodeConnector.class, key);
                                    final InstanceIdentifier<FlowCapableNodeConnectorStatisticsData> nodeConnStatIdent = nodeConnectorIdent
                                            .augmentation(FlowCapableNodeConnectorStatisticsData.class);
                                    final InstanceIdentifier<FlowCapableNodeConnectorStatistics> flowCapNodeConnStatIdent =
                                            nodeConnStatIdent.child(FlowCapableNodeConnectorStatistics.class);
                                    deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, flowCapNodeConnStatIdent, nodeConnectorStatistics);
                                    break;
                                case OFPMPTABLE:
                                    FlowTableAndStatisticsMap flowTableAndStatisticsMap = (FlowTableAndStatisticsMap) singleMultipartData;
                                    final InstanceIdentifier<Table> tableIdent = basicIId
                                            .child(Table.class, new TableKey(flowTableAndStatisticsMap.getTableId().getValue()));
                                    final FlowTableStatistics flowTableStatistics = new FlowTableStatisticsBuilder(flowTableAndStatisticsMap).build();
                                    final InstanceIdentifier<FlowTableStatisticsData> tableStatIdent = tableIdent
                                            .augmentation(FlowTableStatisticsData.class);

                                    final InstanceIdentifier<FlowTableStatistics> tStatIdent = tableStatIdent.child(FlowTableStatistics.class);
                                    deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, tStatIdent, flowTableStatistics);
                                    break;
                                case OFPMPQUEUE:
                                    QueueIdAndStatisticsMap queueIdAndStatisticsMap = (QueueIdAndStatisticsMap) singleMultipartData;
                                    final FlowCapableNodeConnectorQueueStatistics statChild =
                                            new FlowCapableNodeConnectorQueueStatisticsBuilder(queueIdAndStatisticsMap).build();
                                    final FlowCapableNodeConnectorQueueStatisticsDataBuilder statBuild =
                                            new FlowCapableNodeConnectorQueueStatisticsDataBuilder();
                                    statBuild.setFlowCapableNodeConnectorQueueStatistics(statChild);
                                    final QueueKey qKey = new QueueKey(queueIdAndStatisticsMap.getQueueId());
                                    final InstanceIdentifier<Queue> queueIdent = basicIId
                                            .child(NodeConnector.class, new NodeConnectorKey(queueIdAndStatisticsMap.getNodeConnectorId()))
                                            .augmentation(FlowCapableNodeConnector.class)
                                            .child(Queue.class, qKey);
                                    final InstanceIdentifier<FlowCapableNodeConnectorQueueStatisticsData> queueStatIdent = queueIdent.augmentation(FlowCapableNodeConnectorQueueStatisticsData.class);
                                    deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, queueStatIdent, statBuild.build());
                                    break;
                                case OFPMPFLOW:
                                    FlowsStatisticsUpdate flowsStatisticsUpdate = (FlowsStatisticsUpdate) singleMultipartData;
                                    deviceContext.writeToTransaction(LogicalDatastoreType.OPERATIONAL, basicIId, flowsStatisticsUpdate);
                                    break;
                                case OFPMPEXPERIMENTER:
                                    //TODO : implement this
                                    break;

                            }
                        }
                    }
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
        });
    }
}
