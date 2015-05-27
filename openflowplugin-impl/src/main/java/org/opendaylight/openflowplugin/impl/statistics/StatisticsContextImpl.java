/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.annotation.CheckForNull;
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
    private final DeviceState devState;
    private final ListenableFuture<Boolean> emptyFuture;

    private final StatisticsGatheringService statisticsGatheringService;

    public StatisticsContextImpl(@CheckForNull final DeviceContext deviceContext) {
        this.deviceContext = Preconditions.checkNotNull(deviceContext);
        devState = Preconditions.checkNotNull(deviceContext.getDeviceState());
        emptyFuture = Futures.immediateFuture(new Boolean(false));
        statisticsGatheringService = new StatisticsGatheringService(this, deviceContext);
    }

    @Override
    public ListenableFuture<Boolean> gatherDynamicData() {
        LOG.debug("Gathering statistics for device {}", deviceContext.getDeviceState().getNodeId().toString());
        final SettableFuture<Boolean> settableResultingFuture = SettableFuture.create();
        final ListenableFuture<Boolean> flowStatistics = gatherDynamicData(MultipartType.OFPMPFLOW);
        final ListenableFuture<Boolean> tableStatistics = gatherDynamicData(MultipartType.OFPMPTABLE);
        final ListenableFuture<Boolean> portStatistics = gatherDynamicData(MultipartType.OFPMPPORTSTATS);
        final ListenableFuture<Boolean> queueStatistics = gatherDynamicData(MultipartType.OFPMPQUEUE);
        final ListenableFuture<Boolean> groupDescStatistics = gatherDynamicData(MultipartType.OFPMPGROUPDESC);
        final ListenableFuture<Boolean> groupStatistics = gatherDynamicData(MultipartType.OFPMPGROUP);
        final ListenableFuture<Boolean> meterConfigStatistics = gatherDynamicData(MultipartType.OFPMPMETERCONFIG);
        final ListenableFuture<Boolean> meterStatistics = gatherDynamicData(MultipartType.OFPMPMETER);

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
        return settableResultingFuture;
    }

    @Override
    public ListenableFuture<Boolean> gatherDynamicData(final MultipartType multipartType) {
        Preconditions.checkArgument(multipartType != null);
        final ListenableFuture<Boolean> resultingFuture = deviceConnectionCheck();
        if (resultingFuture != null) {
            return resultingFuture;
        }
        switch (multipartType) {
            case OFPMPFLOW:
                return collectFlowStatistics(multipartType);
            case OFPMPTABLE:
                return collectTableStatistics(multipartType);
            case OFPMPPORTSTATS:
                return collectPortStatistics(multipartType);
            case OFPMPQUEUE:
                return collectQueueStatistics(multipartType);
            case OFPMPGROUPDESC:
                return collectGroupDescStatistics(multipartType);
            case OFPMPGROUP:
                return collectGroupStatistics(multipartType);
            case OFPMPMETERCONFIG:
                return collectMeterConfigStatistics(multipartType);
            case OFPMPMETER:
                return collectMeterStatistics(multipartType);
            default:
                LOG.warn("Unsuported Statistics type {}", multipartType);
                return Futures.immediateCheckedFuture(Boolean.TRUE);
        }
    }

    @Override
    public <T> RequestContext<T> createRequestContext() {
        final AbstractRequestContext<T> ret = new AbstractRequestContext<T>(deviceContext.getReservedXid()) {
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

    /**
     * Method checks a device state. It returns null for be able continue. Otherwise it returns immediateFuture
     * which has to be returned from caller too
     *
     * @return
     */
    private ListenableFuture<Boolean> deviceConnectionCheck() {
        if (!ConnectionContext.CONNECTION_STATE.WORKING.equals(deviceContext.getPrimaryConnectionContext().getConnectionState())) {
            ListenableFuture<Boolean> resultingFuture = SettableFuture.create();
            switch (deviceContext.getPrimaryConnectionContext().getConnectionState()) {
                case RIP:
                    final String errMsg = String.format("Device connection doesn't exist anymore. Primary connection status : %s",
                            deviceContext.getPrimaryConnectionContext().getConnectionState());
                    resultingFuture = Futures.immediateFailedFuture(new Throwable(errMsg));
                    break;
                default:
                    resultingFuture = Futures.immediateCheckedFuture(Boolean.TRUE);
                    break;
            }
            return resultingFuture;
        }
        return null;
    }

    private ListenableFuture<Boolean> collectFlowStatistics(final MultipartType multipartType) {
        return devState.isFlowStatisticsAvailable() ? StatisticsGatheringUtils.gatherStatistics(
                statisticsGatheringService, deviceContext, /*MultipartType.OFPMPFLOW*/ multipartType) : emptyFuture;
    }

    private ListenableFuture<Boolean> collectTableStatistics(final MultipartType multipartType) {
        return devState.isTableStatisticsAvailable() ? StatisticsGatheringUtils.gatherStatistics(
                statisticsGatheringService, deviceContext, /*MultipartType.OFPMPTABLE*/ multipartType) : emptyFuture;
    }

    private ListenableFuture<Boolean> collectPortStatistics(final MultipartType multipartType) {
        return devState.isPortStatisticsAvailable() ? StatisticsGatheringUtils.gatherStatistics(
                statisticsGatheringService, deviceContext, /*MultipartType.OFPMPPORTSTATS*/ multipartType) : emptyFuture;
    }

    private ListenableFuture<Boolean> collectQueueStatistics(final MultipartType multipartType) {
        return devState.isQueueStatisticsAvailable() ? StatisticsGatheringUtils.gatherStatistics(
                statisticsGatheringService, deviceContext, /*MultipartType.OFPMPQUEUE*/ multipartType) : emptyFuture;
    }

    private ListenableFuture<Boolean> collectGroupDescStatistics(final MultipartType multipartType) {
        return devState.isGroupAvailable() ? StatisticsGatheringUtils.gatherStatistics(
                statisticsGatheringService, deviceContext, /*MultipartType.OFPMPGROUPDESC*/ multipartType) : emptyFuture;
    }

    private ListenableFuture<Boolean> collectGroupStatistics(final MultipartType multipartType) {
        return devState.isGroupAvailable() ? StatisticsGatheringUtils.gatherStatistics(
                statisticsGatheringService, deviceContext, /*MultipartType.OFPMPGROUP*/ multipartType) : emptyFuture;
    }

    private ListenableFuture<Boolean> collectMeterConfigStatistics(final MultipartType multipartType) {
        return devState.isMetersAvailable() ? StatisticsGatheringUtils.gatherStatistics(
                statisticsGatheringService, deviceContext, /*MultipartType.OFPMPMETERCONFIG*/ multipartType) : emptyFuture;
    }

    private ListenableFuture<Boolean> collectMeterStatistics(final MultipartType multipartType) {
        return devState.isMetersAvailable() ? StatisticsGatheringUtils.gatherStatistics(
                statisticsGatheringService, deviceContext, /*MultipartType.OFPMPMETER*/ multipartType) : emptyFuture;
    }
}
