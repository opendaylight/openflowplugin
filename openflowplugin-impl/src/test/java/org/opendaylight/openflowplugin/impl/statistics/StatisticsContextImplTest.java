/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.connection.ConnectionContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProviderFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsContextImplTest extends StatisticsContextImpMockInitiation {

    private static final Logger LOG = LoggerFactory.getLogger(StatisticsContextImplTest.class);

    private static final Long TEST_XID = 55L;
    private StatisticsContextImpl<MultipartReply> statisticsContext;
    private ConvertorManager convertorManager;

    @Before
    public void setUp() throws Exception {
        convertorManager = ConvertorManagerFactory.createDefaultManager();
        when(mockedDeviceInfo.reserveXidForDeviceMessage()).thenReturn(TEST_XID);
        Mockito.when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);
        initStatisticsContext();
    }

    private void initStatisticsContext() {
        statisticsContext = new StatisticsContextImpl<MultipartReply>(
                true, mockedDeviceContext, convertorManager, mockedStatisticsManager,
                MultipartWriterProviderFactory.createDefaultProvider(mockedDeviceContext));

        statisticsContext.setStatisticsGatheringService(mockedStatisticsGatheringService);
        statisticsContext.setStatisticsGatheringOnTheFlyService(mockedStatisticsOnFlyGatheringService);
    }

    @Test
    public void testCreateRequestContext() {
        final RequestContext<Object> requestContext = statisticsContext.createRequestContext();
        assertNotNull(requestContext);
        assertEquals(TEST_XID, requestContext.getXid().getValue());
        Assert.assertFalse(requestContext.getFuture().isDone());
    }

    /**
     * There is nothing to check in close method
     */
    @Test
    public void testClose() throws Exception {
        final StatisticsContextImpl<MultipartReply> statisticsContext = new StatisticsContextImpl<MultipartReply>(
                true, mockedDeviceContext, convertorManager, mockedStatisticsManager,
                MultipartWriterProviderFactory.createDefaultProvider(mockedDeviceContext));

        final RequestContext<Object> requestContext = statisticsContext.createRequestContext();
        statisticsContext.close();
        try {
            Assert.assertTrue(requestContext.getFuture().isDone());
            final RpcResult<?> rpcResult = requestContext.getFuture().get();
            Assert.assertFalse(rpcResult.isSuccessful());
            Assert.assertFalse(rpcResult.isSuccessful());
        } catch (final Exception e) {
            LOG.error("request future value should be finished", e);
            Assert.fail("request context closing failed");
        }
    }

    @Test
    public void testGatherDynamicData_none() throws Exception {
        final ListenableFuture<Boolean> gatheringResult = statisticsContext.gatherDynamicData();
        Assert.assertTrue(gatheringResult.isDone());
        Assert.assertTrue(gatheringResult.get());
        Mockito.verifyNoMoreInteractions(mockedStatisticsGatheringService, mockedStatisticsOnFlyGatheringService);
    }

    @Test
    public void testGatherDynamicData_all() throws Exception {
        Mockito.reset(mockedDeviceState);
        when(mockedDeviceState.isTableStatisticsAvailable()).thenReturn(Boolean.TRUE);
        when(mockedDeviceState.isFlowStatisticsAvailable()).thenReturn(Boolean.TRUE);
        when(mockedDeviceState.isGroupAvailable()).thenReturn(Boolean.TRUE);
        when(mockedDeviceState.isMetersAvailable()).thenReturn(Boolean.TRUE);
        when(mockedDeviceState.isPortStatisticsAvailable()).thenReturn(Boolean.TRUE);
        when(mockedDeviceState.isQueueStatisticsAvailable()).thenReturn(Boolean.TRUE);
        when(mockedDeviceInfo.getNodeInstanceIdentifier()).thenReturn(dummyNodeII);
        initStatisticsContext();

        when(mockedStatisticsGatheringService.getStatisticsOfType(Matchers.any(EventIdentifier.class), Matchers.any(MultipartType.class)))
                .thenReturn(
                        Futures.immediateFuture(RpcResultBuilder.success(Collections.<MultipartReply>emptyList()).build())
                );
        when(mockedStatisticsOnFlyGatheringService.getStatisticsOfType(Matchers.any(EventIdentifier.class), Matchers.any(MultipartType.class)))
                .thenReturn(
                        Futures.immediateFuture(RpcResultBuilder.success(Collections.<MultipartReply>emptyList()).build())
                );

        final ListenableFuture<Boolean> gatheringResult = statisticsContext.gatherDynamicData();
        Assert.assertTrue(gatheringResult.isDone());
        Assert.assertTrue(gatheringResult.get());
        verify(mockedStatisticsGatheringService, times(7))
                .getStatisticsOfType(Matchers.any(EventIdentifier.class), Matchers.any(MultipartType.class));
        verify(mockedStatisticsOnFlyGatheringService)
                .getStatisticsOfType(Matchers.any(EventIdentifier.class), Matchers.any(MultipartType.class));
        Mockito.verifyNoMoreInteractions(mockedStatisticsGatheringService, mockedStatisticsOnFlyGatheringService);
    }

    @Test
    public void testDeviceConnectionCheck_WORKING() throws Exception {
        final ListenableFuture<Boolean> deviceConnectionCheckResult = statisticsContext.deviceConnectionCheck();
        Assert.assertTrue(deviceConnectionCheckResult.get());
    }

    @Test
    public void testDeviceConnectionCheck_RIP() throws Exception {
        Mockito.reset(mockedConnectionContext);
        when(mockedConnectionContext.getConnectionState()).thenReturn(ConnectionContext.CONNECTION_STATE.RIP);
        final ListenableFuture<Boolean> deviceConnectionCheckResult = statisticsContext.deviceConnectionCheck();

        try {
            deviceConnectionCheckResult.get();
            Assert.fail("connection in state RIP should have caused exception here");
        } catch (final Exception e) {
            LOG.debug("expected behavior for RIP connection achieved");
            Assert.assertTrue(e instanceof ExecutionException);
        }
    }

    @Test
    public void testDeviceConnectionCheck_HANSHAKING() throws Exception {
        Mockito.reset(mockedConnectionContext);
        when(mockedConnectionContext.getConnectionState()).thenReturn(ConnectionContext.CONNECTION_STATE.HANDSHAKING);
        final ListenableFuture<Boolean> deviceConnectionCheckResult = statisticsContext.deviceConnectionCheck();

        try {
            final Boolean checkPositive = deviceConnectionCheckResult.get();
            Assert.assertTrue(checkPositive);
        } catch (final Exception e) {
            Assert.fail("connection in state HANDSHAKING should NOT have caused exception here");
        }
    }
}
