/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.sun.org.apache.xpath.internal.operations.Bool;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.impl.rpc.RequestContextImpl;
import org.opendaylight.openflowplugin.impl.services.RequestContextUtil;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.StatisticsGatheringService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.KeyedInstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 1.4.2015.
 */
public class StatisticsContextImpl implements StatisticsContext {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsContextImpl.class);
    public static final String CONNECTION_CLOSED = "Connection closed.";
    private final List<RequestContext> requestContexts = new ArrayList();
    private final DeviceContext deviceContext;


    private final StatisticsGatheringService statisticsGatheringService;

    public StatisticsContextImpl(final DeviceContext deviceContext) {
        this.deviceContext = deviceContext;
        statisticsGatheringService = new StatisticsGatheringService(this, deviceContext);

    }

    private void pollFlowStatistics() {
        final KeyedInstanceIdentifier<Node, NodeKey> nodeII = InstanceIdentifier.create(Nodes.class).child(Node.class, new NodeKey(deviceContext.getPrimaryConnectionContext().getNodeId()));
        final NodeRef nodeRef = new NodeRef(nodeII);
        final GetAllFlowsStatisticsFromAllFlowTablesInputBuilder builder =
                new GetAllFlowsStatisticsFromAllFlowTablesInputBuilder();
        builder.setNode(nodeRef);
        //TODO : process data from result
    }

    @Override
    public ListenableFuture<Boolean> gatherDynamicData() {

        final DeviceState devState = deviceContext.getDeviceState();

        final ListenableFuture<Boolean> resultingFuture;

        if (ConnectionContext.CONNECTION_STATE.WORKING.equals(deviceContext.getPrimaryConnectionContext().getConnectionState())) {
            final SettableFuture settableResultingFuture = SettableFuture.create();
            resultingFuture = settableResultingFuture;
            ListenableFuture<Boolean> emptyFuture = Futures.immediateFuture(new Boolean(false));
            final ListenableFuture<Boolean> flowStatistics = devState.isFlowStatisticsAvailable() ? wrapLoggingOnStatisticsRequestCall(MultipartType.OFPMPFLOW) : emptyFuture;

            final ListenableFuture<Boolean> tableStatistics = devState.isTableStatisticsAvailable() ? wrapLoggingOnStatisticsRequestCall(MultipartType.OFPMPTABLE) : emptyFuture;

            final ListenableFuture<Boolean> portStatistics = devState.isPortStatisticsAvailable() ? wrapLoggingOnStatisticsRequestCall(MultipartType.OFPMPPORTSTATS) : emptyFuture;

            final ListenableFuture<Boolean> queueStatistics = devState.isQueueStatisticsAvailable() ? wrapLoggingOnStatisticsRequestCall(MultipartType.OFPMPQUEUE) : emptyFuture;

            final ListenableFuture<Boolean> groupDescStatistics = devState.isGroupAvailable() ? wrapLoggingOnStatisticsRequestCall(MultipartType.OFPMPGROUPDESC) : emptyFuture;
            final ListenableFuture<Boolean> groupStatistics = devState.isGroupAvailable() ? wrapLoggingOnStatisticsRequestCall(MultipartType.OFPMPGROUP) : emptyFuture;

            final ListenableFuture<Boolean> meterConfigStatistics = devState.isMetersAvailable() ? wrapLoggingOnStatisticsRequestCall(MultipartType.OFPMPMETERCONFIG) : emptyFuture;
            final ListenableFuture<Boolean> meterStatistics = devState.isMetersAvailable() ? wrapLoggingOnStatisticsRequestCall(MultipartType.OFPMPMETER) : emptyFuture;


            final ListenableFuture<List<Boolean>> allFutures = Futures.allAsList(Arrays.asList(flowStatistics, tableStatistics, groupDescStatistics, groupStatistics, meterConfigStatistics, meterStatistics, portStatistics, queueStatistics));
            Futures.addCallback(allFutures, new FutureCallback<List<Boolean>>() {
                @Override
                public void onSuccess(final List<Boolean> booleans) {
                    boolean atLeastOneSuccess = false;
                    for (Boolean bool : booleans){
                        atLeastOneSuccess |= bool.booleanValue();
                    }
                    settableResultingFuture.set(new Boolean(atLeastOneSuccess));
                }

                @Override
                public void onFailure(final Throwable throwable) {
                    settableResultingFuture.setException(throwable);
                }
            });
        } else {
            resultingFuture = Futures.immediateFailedFuture(new Throwable("Device connection doesn't exist anymore."));
        }
        return resultingFuture;
    }

    private ListenableFuture<Boolean> wrapLoggingOnStatisticsRequestCall(final MultipartType type) {
        final ListenableFuture<Boolean> future = StatisticsGatheringUtils.gatherStatistics(statisticsGatheringService, deviceContext, type);
        Futures.addCallback(future, new FutureCallback() {
            @Override
            public void onSuccess(final Object o) {
                LOG.trace("Multipart response for {} was successful.", type);
            }

            @Override
            public void onFailure(final Throwable throwable) {
                LOG.trace("Multipart response for {} FAILED.", type, throwable);
            }
        });
        return future;
    }

    @Override
    public <T> void forgetRequestContext(final RequestContext<T> requestContext) {
        requestContexts.remove(requestContexts);
    }

    @Override
    public <T> SettableFuture<RpcResult<T>> storeOrFail(final RequestContext<T> data) {
        requestContexts.add(data);
        return data.getFuture();
    }

    @Override
    public <T> RequestContext<T> createRequestContext() {
        return new RequestContextImpl<>(this);
    }

    @Override
    public void close() throws Exception {
        for (RequestContext requestContext : requestContexts) {
            RequestContextUtil.closeRequestContextWithRpcError(requestContext, CONNECTION_CLOSED);
        }
    }
}
