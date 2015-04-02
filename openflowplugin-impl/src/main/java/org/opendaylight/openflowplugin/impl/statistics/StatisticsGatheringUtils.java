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
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.OpendaylightFlowStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.flow.and.statistics.map.list.FlowAndStatisticsMapList;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.GetFlowTablesStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.OpendaylightFlowTableStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetAllGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.OpendaylightGroupStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.OpendaylightMeterStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.GetAllNodeConnectorsStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.OpendaylightPortStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPortsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.GetAllQueuesStatisticsFromAllPortsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.OpendaylightQueueStatisticsService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 2.4.2015.
 */
public final class StatisticsGatheringUtils {

    private StatisticsGatheringUtils() {
        throw new IllegalStateException("This class should not be instantiated.");
    }

    private static NodeRef createNodeRef(DeviceContext deviceContext) {
        final KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(deviceContext.getPrimaryConnectionContext().getNodeId()));
        return new NodeRef(nodeInstanceIdentifier);
    }

    public static ListenableFuture<Boolean> gatherQueueStatistics(OpendaylightQueueStatisticsService queueStatisticsService, DeviceContext deviceContext) {

        final GetAllQueuesStatisticsFromAllPortsInputBuilder builder =
                new GetAllQueuesStatisticsFromAllPortsInputBuilder();

        builder.setNode(createNodeRef(deviceContext));

        ListenableFuture<RpcResult<GetAllQueuesStatisticsFromAllPortsOutput>> statisticsDataInFuture =
                JdkFutureAdapters.
                        listenInPoolThread(queueStatisticsService.
                                getAllQueuesStatisticsFromAllPorts(builder.build()));

        return Futures.transform(statisticsDataInFuture, new Function<RpcResult<GetAllQueuesStatisticsFromAllPortsOutput>, Boolean>() {
            @Nullable
            @Override
            public Boolean apply(final RpcResult<GetAllQueuesStatisticsFromAllPortsOutput> rpcResult) {
                if (rpcResult.isSuccessful()) {
                    //TODO : implement data read and put them into transaction chain
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
        });
    }


    public static ListenableFuture<Boolean> gatherPortStatistics(OpendaylightPortStatisticsService portStatisticsService, DeviceContext deviceContext) {

        final GetAllNodeConnectorsStatisticsInputBuilder builder =
                new GetAllNodeConnectorsStatisticsInputBuilder();

        builder.setNode(createNodeRef(deviceContext));

        ListenableFuture<RpcResult<GetAllNodeConnectorsStatisticsOutput>> statisticsDataInFuture =
                JdkFutureAdapters.
                        listenInPoolThread(portStatisticsService.
                                getAllNodeConnectorsStatistics(builder.build()));

        return Futures.transform(statisticsDataInFuture, new Function<RpcResult<GetAllNodeConnectorsStatisticsOutput>, Boolean>() {
            @Nullable
            @Override
            public Boolean apply(final RpcResult<GetAllNodeConnectorsStatisticsOutput> rpcResult) {
                if (rpcResult.isSuccessful()) {
                    //TODO : implement data read and put them into transaction chain
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
        });
    }

    public static ListenableFuture<Boolean> gatherMeterStatistics(OpendaylightMeterStatisticsService meterStatisticsService, DeviceContext deviceContext) {

        final GetAllMeterStatisticsInputBuilder builder =
                new GetAllMeterStatisticsInputBuilder();

        builder.setNode(createNodeRef(deviceContext));

        ListenableFuture<RpcResult<GetAllMeterStatisticsOutput>> statisticsDataInFuture =
                JdkFutureAdapters.
                        listenInPoolThread(meterStatisticsService.
                                getAllMeterStatistics(builder.build()));

        return Futures.transform(statisticsDataInFuture, new Function<RpcResult<GetAllMeterStatisticsOutput>, Boolean>() {
            @Nullable
            @Override
            public Boolean apply(final RpcResult<GetAllMeterStatisticsOutput> rpcResult) {
                if (rpcResult.isSuccessful()) {
                    //TODO : implement data read and put them into transaction chain
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
        });
    }


    public static ListenableFuture<Boolean> gatherGroupStatistics(OpendaylightGroupStatisticsService groupStatisticsService, DeviceContext deviceContext) {
        final GetAllGroupStatisticsInputBuilder builder =
                new GetAllGroupStatisticsInputBuilder();
        builder.setNode(createNodeRef(deviceContext));
        ListenableFuture<RpcResult<GetAllGroupStatisticsOutput>> allFlowTablesDataInFuture =
                JdkFutureAdapters.
                        listenInPoolThread(groupStatisticsService.
                                getAllGroupStatistics(builder.build()));

        return Futures.transform(allFlowTablesDataInFuture, new Function<RpcResult<GetAllGroupStatisticsOutput>, Boolean>() {
            @Nullable
            @Override
            public Boolean apply(final RpcResult<GetAllGroupStatisticsOutput> rpcResult) {
                if (rpcResult.isSuccessful()) {
                    //TODO : implement data read and put them into transaction chain
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
        });
    }

    public static ListenableFuture<Boolean> gatherFlowStatistics(OpendaylightFlowStatisticsService flowStatisticsService, DeviceContext deviceContext) {
        final GetAllFlowsStatisticsFromAllFlowTablesInputBuilder builder =
                new GetAllFlowsStatisticsFromAllFlowTablesInputBuilder();
        builder.setNode(createNodeRef(deviceContext));
        ListenableFuture<RpcResult<GetAllFlowsStatisticsFromAllFlowTablesOutput>> allFlowTablesDataInFuture =
                JdkFutureAdapters.
                        listenInPoolThread(flowStatisticsService.
                                getAllFlowsStatisticsFromAllFlowTables(builder.build()));

        return Futures.transform(allFlowTablesDataInFuture, new Function<RpcResult<GetAllFlowsStatisticsFromAllFlowTablesOutput>, Boolean>() {
            @Nullable
            @Override
            public Boolean apply(final RpcResult<GetAllFlowsStatisticsFromAllFlowTablesOutput> rpcResult) {
                if (rpcResult.isSuccessful()) {
                    List<FlowAndStatisticsMapList> flowAndStatsList = rpcResult.getResult().getFlowAndStatisticsMapList();
                    //TODO : implement data read and put them into transaction chain
                    for (FlowAndStatisticsMapList flowAndStatisticsMap : flowAndStatsList) {
                    }
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
        });
    }

    public static ListenableFuture<Boolean> gatherTableStatistics(OpendaylightFlowTableStatisticsService flowTableStatisticsService, DeviceContext deviceContext) {
        GetFlowTablesStatisticsInputBuilder getFlowTablesStatisticsInputBuilder = new GetFlowTablesStatisticsInputBuilder();
        getFlowTablesStatisticsInputBuilder.setNode(createNodeRef(deviceContext));
        ListenableFuture<RpcResult<GetFlowTablesStatisticsOutput>> flowTableStaticsDataInFuture = JdkFutureAdapters.listenInPoolThread(flowTableStatisticsService.getFlowTablesStatistics(getFlowTablesStatisticsInputBuilder.build()));
        return Futures.transform(flowTableStaticsDataInFuture, new Function<RpcResult<GetFlowTablesStatisticsOutput>, Boolean>() {
            @Nullable
            @Override
            public Boolean apply(
                    final RpcResult<GetFlowTablesStatisticsOutput> rpcResult) {
                if (rpcResult.isSuccessful()) {
                    //TODO : implement data read and put them into transaction chain
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
        });
    }
}
