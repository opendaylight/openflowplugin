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
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.FlowStatisticsService;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.FlowTableStatisticsService;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.GroupStatisticsService;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.MeterStatisticsService;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.PortStatisticsService;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.QueueStatisticsService;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.FlowStatsResponseConvertor;
import org.opendaylight.openflowplugin.openflow.md.core.translator.MultipartReplyTranslator;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 2.4.2015.
 */
public final class StatisticsGatheringUtils {

    private static final FlowStatsResponseConvertor FLOW_STATS_RESPONSE_CONVERTOR = new FlowStatsResponseConvertor();
    private static final MultipartReplyTranslator MULTIPART_REPLY_TRANSLATOR = new MultipartReplyTranslator();


    private StatisticsGatheringUtils() {
        throw new IllegalStateException("This class should not be instantiated.");
    }

    private static NodeRef createNodeRef(DeviceContext deviceContext) {
        final KeyedInstanceIdentifier<Node, NodeKey> nodeInstanceIdentifier = getInstanceIdentifier(deviceContext);
        return new NodeRef(nodeInstanceIdentifier);
    }

    private static KeyedInstanceIdentifier<Node, NodeKey> getInstanceIdentifier(final DeviceContext deviceContext) {
        return InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(deviceContext.getPrimaryConnectionContext().getNodeId()));
    }

    public static ListenableFuture<Boolean> gatherQueueStatistics(QueueStatisticsService queueStatisticsService) {

        ListenableFuture<RpcResult<List<MultipartReply>>> statisticsDataInFuture =
                JdkFutureAdapters.listenInPoolThread(queueStatisticsService.getAllQueuesStatisticsFromAllPorts());

        return transformAndStoreStatisticsData(statisticsDataInFuture);
    }


    public static ListenableFuture<Boolean> gatherPortStatistics(PortStatisticsService portStatisticsService) {

        ListenableFuture<RpcResult<List<MultipartReply>>> statisticsDataInFuture =
                JdkFutureAdapters.listenInPoolThread(portStatisticsService.getAllNodeConnectorsStatistics());

        return transformAndStoreStatisticsData(statisticsDataInFuture);
    }

    public static ListenableFuture<Boolean> gatherMeterStatistics(MeterStatisticsService meterStatisticsService) {


        ListenableFuture<RpcResult<List<MultipartReply>>> statisticsDataInFuture =
                JdkFutureAdapters.
                        listenInPoolThread(meterStatisticsService.
                                getAllMeterStatistics());

        return transformAndStoreStatisticsData(statisticsDataInFuture);
    }


    public static ListenableFuture<Boolean> gatherGroupStatistics(GroupStatisticsService groupStatisticsService) {
        ListenableFuture<RpcResult<List<MultipartReply>>> statisticsDataInFuture =
                JdkFutureAdapters.
                        listenInPoolThread(groupStatisticsService.
                                getAllGroupStatistics());

        return transformAndStoreStatisticsData(statisticsDataInFuture);
    }

    public static ListenableFuture<Boolean> gatherFlowStatistics(FlowStatisticsService flowStatisticsService) {
        ListenableFuture<RpcResult<List<MultipartReply>>> statisticsDataInFuture =
                JdkFutureAdapters.
                        listenInPoolThread(flowStatisticsService.

                                getAllFlowsStatisticsFromAllFlowTables());
        return transformAndStoreStatisticsData(statisticsDataInFuture);

    }

    public static ListenableFuture<Boolean> gatherTableStatistics(FlowTableStatisticsService flowTableStatisticsService) {

        ListenableFuture<RpcResult<List<MultipartReply>>> statisticsDataInFuture =
                JdkFutureAdapters.listenInPoolThread(
                        flowTableStatisticsService.getFlowTablesStatistics());

        return transformAndStoreStatisticsData(statisticsDataInFuture);
    }

    private static ListenableFuture<Boolean> transformAndStoreStatisticsData(final ListenableFuture<RpcResult<List<MultipartReply>>> statisticsDataInFuture) {
        return Futures.transform(statisticsDataInFuture, new Function<RpcResult<List<MultipartReply>>, Boolean>() {
            @Nullable
            @Override
            public Boolean apply(final RpcResult<List<MultipartReply>> rpcResult) {
                if (rpcResult.isSuccessful()) {
                    //TODO : implement data read and put them into transaction chain
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            }
        });
    }
}
