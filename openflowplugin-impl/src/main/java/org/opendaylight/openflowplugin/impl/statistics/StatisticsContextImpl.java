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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceState;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.StatisticsContext;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.openflowplugin.impl.services.RequestContextUtil;
import org.opendaylight.openflowplugin.impl.statistics.services.dedicated.StatisticsGatheringService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Martin Bobak &lt;mbobak@cisco.com&gt; on 1.4.2015.
 */
public class StatisticsContextImpl implements StatisticsContext {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsContextImpl.class);
    private static final String CONNECTION_CLOSED = "Connection closed.";
    private final Collection<RequestContext<?>> requestContexts = new HashSet<>();
    private final DeviceContext deviceContext;

    private final StatisticsGatheringService statisticsGatheringService;

    public StatisticsContextImpl(final DeviceContext deviceContext) {
        this.deviceContext = deviceContext;
        statisticsGatheringService = new StatisticsGatheringService(this, deviceContext);
    }

    @Override
    public ListenableFuture<Boolean> gatherDynamicData() {

        final SettableFuture<Boolean> settableResultingFuture = SettableFuture.create();
        ListenableFuture<Boolean> resultingFuture = settableResultingFuture;


        if (ConnectionContext.CONNECTION_STATE.WORKING.equals(deviceContext.getPrimaryConnectionContext().getConnectionState())) {
            final DeviceState devState = deviceContext.getDeviceState();
            final ListenableFuture<Boolean> emptyFuture = Futures.immediateFuture(new Boolean(false));
            final ListenableFuture<Boolean> flowStatistics = devState.isFlowStatisticsAvailable() ? StatisticsGatheringUtils.gatherStatistics(statisticsGatheringService, deviceContext, MultipartType.OFPMPFLOW) : emptyFuture;

            final ListenableFuture<Boolean> tableStatistics = devState.isTableStatisticsAvailable() ? StatisticsGatheringUtils.gatherStatistics(statisticsGatheringService, deviceContext, MultipartType.OFPMPTABLE) : emptyFuture;

            final ListenableFuture<Boolean> portStatistics = devState.isPortStatisticsAvailable() ? StatisticsGatheringUtils.gatherStatistics(statisticsGatheringService, deviceContext, MultipartType.OFPMPPORTSTATS) : emptyFuture;

            final ListenableFuture<Boolean> queueStatistics = devState.isQueueStatisticsAvailable() ? StatisticsGatheringUtils.gatherStatistics(statisticsGatheringService, deviceContext, MultipartType.OFPMPQUEUE) : emptyFuture;

            final ListenableFuture<Boolean> groupDescStatistics = devState.isGroupAvailable() ? StatisticsGatheringUtils.gatherStatistics(statisticsGatheringService, deviceContext, MultipartType.OFPMPGROUPDESC) : emptyFuture;
            final ListenableFuture<Boolean> groupStatistics = devState.isGroupAvailable() ? StatisticsGatheringUtils.gatherStatistics(statisticsGatheringService, deviceContext, MultipartType.OFPMPGROUP) : emptyFuture;

            final ListenableFuture<Boolean> meterConfigStatistics = devState.isMetersAvailable() ? StatisticsGatheringUtils.gatherStatistics(statisticsGatheringService, deviceContext, MultipartType.OFPMPMETERCONFIG) : emptyFuture;
            final ListenableFuture<Boolean> meterStatistics = devState.isMetersAvailable() ? StatisticsGatheringUtils.gatherStatistics(statisticsGatheringService, deviceContext, MultipartType.OFPMPMETER) : emptyFuture;


            final ListenableFuture<List<Boolean>> allFutures = Futures.allAsList(Arrays.asList(flowStatistics, tableStatistics, groupDescStatistics, groupStatistics, meterConfigStatistics, meterStatistics, portStatistics, queueStatistics));
            Futures.addCallback(allFutures, new FutureCallback<List<Boolean>>() {
                @Override
                public void onSuccess(final List<Boolean> booleans) {
                    boolean atLeastOneSuccess = false;
                    for (final Boolean bool : booleans) {
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
            switch (deviceContext.getPrimaryConnectionContext().getConnectionState()) {
                case RIP:
                    resultingFuture = Futures.immediateFailedFuture(new Throwable(String.format("Device connection doesn't exist anymore. Primary connection status : %s", deviceContext.getPrimaryConnectionContext().getConnectionState())));
                    break;
                default:
                    resultingFuture = Futures.immediateCheckedFuture(Boolean.TRUE);
                    break;
            }


        }
        return resultingFuture;
    }

    @Override
    public <T> RequestContext<T> createRequestContext() {
        final Long xid = deviceContext.getReservedXid();
        if (xid == null) {
            LOG.debug("Device is shut down");
            return null;
        }

        final AbstractRequestContext<T> ret = new AbstractRequestContext<T>(xid) {
            @Override
            public void close() {
                requestContexts.remove(this);
            }
        };
        requestContexts.add(ret);
        return ret;
    }

    @Override
    public void close() {
        for (final RequestContext<?> requestContext : requestContexts) {
            RequestContextUtil.closeRequestContextWithRpcError(requestContext, CONNECTION_CLOSED);
        }
    }
}
