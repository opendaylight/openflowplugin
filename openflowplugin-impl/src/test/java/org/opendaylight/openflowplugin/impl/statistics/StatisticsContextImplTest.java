/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProviderFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.NonZeroUint32Type;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflow.provider.config.rev160510.OpenflowProviderConfig;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;

@RunWith(MockitoJUnitRunner.class)
public class StatisticsContextImplTest extends StatisticsContextImpMockInitiation {
    private static final Uint32 TEST_XID = Uint32.valueOf(55);

    private StatisticsContextImpl<MultipartReply> statisticsContext;
    private ConvertorManager convertorManager;
    @Mock
    private final OpenflowProviderConfig config =
            Mockito.mock(OpenflowProviderConfig.class);

    @Before
    public void setUp() {
        convertorManager = ConvertorManagerFactory.createDefaultManager();
        when(mockedDeviceInfo.reserveXidForDeviceMessage()).thenReturn(TEST_XID);
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);
        when(config.getIsTableStatisticsPollingOn()).thenReturn(true);
        when(config.getIsFlowStatisticsPollingOn()).thenReturn(true);
        when(config.getIsGroupStatisticsPollingOn()).thenReturn(true);
        when(config.getIsMeterStatisticsPollingOn()).thenReturn(true);
        when(config.getIsPortStatisticsPollingOn()).thenReturn(true);
        when(config.getIsQueueStatisticsPollingOn()).thenReturn(true);
        when(config.getBasicTimerDelay()).thenReturn(new NonZeroUint32Type(Uint32.valueOf(3000)));
        when(config.getMaximumTimerDelay()).thenReturn(new NonZeroUint32Type(Uint32.valueOf(50000)));

        initStatisticsContext();
    }

    private void initStatisticsContext() {
        statisticsContext = new StatisticsContextImpl<>(mockedDeviceContext, convertorManager,
                MultipartWriterProviderFactory
                        .createDefaultProvider(mockedDeviceContext),
                MoreExecutors.directExecutor(),
                config,
                true,
                false);

        statisticsContext.setStatisticsGatheringService(mockedStatisticsGatheringService);
        statisticsContext.setStatisticsGatheringOnTheFlyService(mockedStatisticsOnFlyGatheringService);
    }

    @Test
    public void testCreateRequestContext() {
        final RequestContext<Object> requestContext = statisticsContext.createRequestContext();
        assertNotNull(requestContext);
        assertEquals(TEST_XID, requestContext.getXid().getValue());
        assertFalse(requestContext.getFuture().isDone());
    }

    /**
     * There is nothing to check in close method.
     */
    @Test
    public void testClose() throws InterruptedException, ExecutionException {
        statisticsContext =
                new StatisticsContextImpl<>(mockedDeviceContext,
                        convertorManager,
                        MultipartWriterProviderFactory
                                .createDefaultProvider(mockedDeviceContext),
                        MoreExecutors.directExecutor(),
                        config,
                        true,
                        false);

        final RequestContext<Object> requestContext = statisticsContext.createRequestContext();
        statisticsContext.close();
        assertTrue(requestContext.getFuture().isDone());
        final RpcResult<?> rpcResult = requestContext.getFuture().get();
        assertFalse(rpcResult.isSuccessful());
    }

    @Test
    public void testGatherDynamicData_none() {
        statisticsContext.instantiateServiceInstance();
        Mockito.verifyNoMoreInteractions(mockedStatisticsGatheringService, mockedStatisticsOnFlyGatheringService);
    }

    @Test
    public void testGatherDynamicData_all() {
        Mockito.reset(mockedDeviceState);
        when(mockedDeviceState.isTableStatisticsAvailable()).thenReturn(Boolean.TRUE);
        when(mockedDeviceState.isFlowStatisticsAvailable()).thenReturn(Boolean.TRUE);
        when(mockedDeviceState.isGroupAvailable()).thenReturn(Boolean.TRUE);
        when(mockedDeviceState.isMetersAvailable()).thenReturn(Boolean.TRUE);
        when(mockedDeviceState.isPortStatisticsAvailable()).thenReturn(Boolean.TRUE);
        when(mockedDeviceState.isQueueStatisticsAvailable()).thenReturn(Boolean.TRUE);
        when(mockedDeviceInfo.getNodeInstanceIdentifier()).thenReturn(DUMMY_NODE_ID);
        initStatisticsContext();

        when(mockedStatisticsGatheringService
                     .getStatisticsOfType(any(EventIdentifier.class), any(MultipartType.class)))
                .thenReturn(Futures.immediateFuture(
                        RpcResultBuilder.success(Collections.<MultipartReply>emptyList()).build()));
        when(mockedStatisticsOnFlyGatheringService
                     .getStatisticsOfType(any(EventIdentifier.class), any(MultipartType.class)))
                .thenReturn(Futures.immediateFuture(
                        RpcResultBuilder.success(Collections.<MultipartReply>emptyList()).build()));

        statisticsContext.registerMastershipWatcher(mockedMastershipWatcher);
        statisticsContext.setStatisticsGatheringService(mockedStatisticsGatheringService);
        statisticsContext.setStatisticsGatheringOnTheFlyService(mockedStatisticsOnFlyGatheringService);
        statisticsContext.initializeDevice();

        verify(mockedStatisticsGatheringService, times(7))
                .getStatisticsOfType(any(EventIdentifier.class), any(MultipartType.class));
        verify(mockedStatisticsOnFlyGatheringService)
                .getStatisticsOfType(any(EventIdentifier.class), any(MultipartType.class));
        Mockito.verifyNoMoreInteractions(mockedStatisticsGatheringService, mockedStatisticsOnFlyGatheringService);
    }
}
