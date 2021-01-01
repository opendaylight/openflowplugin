/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics;

import static com.google.common.util.concurrent.Futures.immediateFuture;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.Mockito;
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

@RunWith(Parameterized.class)
public class StatisticsContextImplParamTest extends StatisticsContextImpMockInitiation {

    @Mock
    private final OpenflowProviderConfig config = mock(OpenflowProviderConfig.class);

    public StatisticsContextImplParamTest(final boolean isTable, final boolean isFlow,
                                          final boolean isGroup, final boolean isMeter,
                                          final boolean isPort, final boolean isQueue) {
        super();
        this.isTable = isTable;
        this.isFlow = isFlow;
        this.isGroup = isGroup;
        this.isMeter = isMeter;
        this.isPort = isPort;
        this.isQueue = isQueue;
    }

    @Parameterized.Parameters(name = "{index}")
    public static Iterable<Object[]> data1() {
        return Arrays.asList(new Object[][]{
                {false, true, false, false, false, false},
                {true, false, false, false, false, false},
                {false, false, true, false, false, false},
                {false, false, false, true, false, false},
                {false, false, false, false, true, false},
                {false, false, false, false, false, true},
        });
    }

    @Before
    public void setUp() {
        Mockito.when(config.getIsTableStatisticsPollingOn()).thenReturn(true);
        Mockito.when(config.getIsFlowStatisticsPollingOn()).thenReturn(true);
        Mockito.when(config.getIsGroupStatisticsPollingOn()).thenReturn(true);
        Mockito.when(config.getIsMeterStatisticsPollingOn()).thenReturn(true);
        Mockito.when(config.getIsPortStatisticsPollingOn()).thenReturn(true);
        Mockito.when(config.getIsQueueStatisticsPollingOn()).thenReturn(true);
        Mockito.when(config.getBasicTimerDelay()).thenReturn(new NonZeroUint32Type(Uint32.valueOf(3000)));
        Mockito.when(config.getMaximumTimerDelay()).thenReturn(new NonZeroUint32Type(Uint32.valueOf(50000)));
    }

    @Test
    public void gatherDynamicDataTest() {

        when(mockedDeviceState.isTableStatisticsAvailable()).thenReturn(Boolean.TRUE);
        when(mockedDeviceState.isFlowStatisticsAvailable()).thenReturn(Boolean.TRUE);
        when(mockedDeviceState.isGroupAvailable()).thenReturn(Boolean.TRUE);
        when(mockedDeviceState.isMetersAvailable()).thenReturn(Boolean.TRUE);
        when(mockedDeviceState.isPortStatisticsAvailable()).thenReturn(Boolean.TRUE);
        when(mockedDeviceState.isQueueStatisticsAvailable()).thenReturn(Boolean.TRUE);
        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);

        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        final StatisticsContextImpl<MultipartReply> statisticsContext = new StatisticsContextImpl<>(
                mockedDeviceContext, convertorManager,
                MultipartWriterProviderFactory.createDefaultProvider(mockedDeviceContext),
                MoreExecutors.newDirectExecutorService(),
                config,
                true,
                false);

        final ListenableFuture<RpcResult<List<MultipartReply>>> rpcResult = immediateFuture(RpcResultBuilder
                .success(Collections.<MultipartReply>emptyList()).build());
        when(mockedStatisticsGatheringService.getStatisticsOfType(any(EventIdentifier.class), any(MultipartType
                .class))).thenReturn(rpcResult);
        when(mockedStatisticsOnFlyGatheringService.getStatisticsOfType(any(EventIdentifier.class), any(MultipartType
                .class))).thenReturn(rpcResult);

        statisticsContext.registerMastershipWatcher(mockedMastershipWatcher);
        statisticsContext.setStatisticsGatheringService(mockedStatisticsGatheringService);
        statisticsContext.setStatisticsGatheringOnTheFlyService(mockedStatisticsOnFlyGatheringService);
        statisticsContext.initializeDevice();

        verify(mockedStatisticsGatheringService, times(7))
                .getStatisticsOfType(any(EventIdentifier.class), any(MultipartType.class));
        verify(mockedStatisticsOnFlyGatheringService)
                .getStatisticsOfType(any(EventIdentifier.class), any(MultipartType.class));
    }

}
