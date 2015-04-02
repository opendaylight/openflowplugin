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
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.impl.rpc.RequestContextImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.MeterStatisticsService;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightFlowStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightFlowTableStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.OpendaylightGroupStatisticsServiceImpl;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.PortStatisticsService;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.QueueStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.OpendaylightFlowStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.OpendaylightFlowTableStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.OpendaylightGroupStatisticsService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 1.4.2015.
 */
public class StatisticsContextImpl implements StatisticsContext {

    private final List<RequestContext> requestContexts = new ArrayList();
    private final DeviceContext deviceContext;
    private final RequestContext requestContext;


    private final OpendaylightFlowStatisticsService flowStatisticsService;
    private final OpendaylightFlowTableStatisticsService flowTableStatisticsService;
    private final OpendaylightGroupStatisticsService groupStatisticsService;
    private final MeterStatisticsService meterStatisticsService;
    private final PortStatisticsService portStatisticsService;
    private final QueueStatisticsService queueStatisticsService;


    public StatisticsContextImpl(DeviceContext deviceContext, RequestContext requestContext) {
        this.deviceContext = deviceContext;
        this.requestContext = requestContext;

        flowStatisticsService = new OpendaylightFlowStatisticsServiceImpl(this, deviceContext);
        flowTableStatisticsService = new OpendaylightFlowTableStatisticsServiceImpl(this, deviceContext);
        groupStatisticsService = new OpendaylightGroupStatisticsServiceImpl(this, deviceContext);
        meterStatisticsService = new MeterStatisticsService(this, deviceContext);
        portStatisticsService = new PortStatisticsService(this, deviceContext);
        queueStatisticsService = new QueueStatisticsService(this, deviceContext);

    }


    @Override
    public ListenableFuture<Void> gatherDynamicData() {
        ListenableFuture<Boolean> flowStatistics = StatisticsGatheringUtils.gatherFlowStatistics(flowStatisticsService, deviceContext);
        ListenableFuture<Boolean> tableStatistics = StatisticsGatheringUtils.gatherTableStatistics(flowTableStatisticsService, deviceContext);
        ListenableFuture<Boolean> groupStatistics = StatisticsGatheringUtils.gatherGroupStatistics(groupStatisticsService, deviceContext);
        ListenableFuture<Boolean> meterStatistics = StatisticsGatheringUtils.gatherMeterStatistics(meterStatisticsService, deviceContext);
        ListenableFuture<Boolean> portStatistics = StatisticsGatheringUtils.gatherPortStatistics(portStatisticsService, deviceContext);
        ListenableFuture<Boolean> queueStatistics = StatisticsGatheringUtils.gatherQueueStatistics(queueStatisticsService, deviceContext);

        ListenableFuture<List<Boolean>> allFutures = Futures.allAsList(Arrays.asList(flowStatistics, tableStatistics, groupStatistics, meterStatistics, portStatistics, queueStatistics));
        final SettableFuture<Void> resultingFuture = SettableFuture.create();
        Futures.addCallback(allFutures, new FutureCallback<List<Boolean>>() {
            @Override
            public void onSuccess(final List<Boolean> booleans) {
                resultingFuture.set(null);
            }

            @Override
            public void onFailure(final Throwable throwable) {
                resultingFuture.setException(throwable);
            }
        });
        return resultingFuture;
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
}
