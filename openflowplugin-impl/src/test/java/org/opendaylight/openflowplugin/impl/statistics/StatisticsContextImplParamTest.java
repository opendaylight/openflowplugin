/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics;

import static com.google.common.util.concurrent.Futures.immediateFuture;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.ListenableFuture;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProviderFactory;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

@RunWith(Parameterized.class)
public class StatisticsContextImplParamTest extends StatisticsContextImpMockInitiation {


    public StatisticsContextImplParamTest(final boolean isTable, final boolean isFlow, final boolean isGroup, final boolean isMeter, final boolean isPort,
                                          final boolean isQueue) {
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




    @Test
    public void gatherDynamicDataTest() {

        when(mockedDeviceContext.getDeviceState()).thenReturn(mockedDeviceState);

        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        final StatisticsContextImpl<MultipartReply> statisticsContext = new StatisticsContextImpl<MultipartReply>(
                true, mockedDeviceContext ,convertorManager, mockedStatisticsManager,
                MultipartWriterProviderFactory.createDefaultProvider(mockedDeviceContext));

        final ListenableFuture<RpcResult<List<MultipartReply>>> rpcResult = immediateFuture(RpcResultBuilder.success(Collections.<MultipartReply>emptyList()).build());
        when(mockedStatisticsGatheringService.getStatisticsOfType(any(EventIdentifier.class), any(MultipartType
                .class))).thenReturn(rpcResult);
        when(mockedStatisticsOnFlyGatheringService.getStatisticsOfType(any(EventIdentifier.class), any(MultipartType
                .class))).thenReturn(rpcResult);

        statisticsContext.setStatisticsGatheringService(mockedStatisticsGatheringService);
        statisticsContext.setStatisticsGatheringOnTheFlyService(mockedStatisticsOnFlyGatheringService);

        final ListenableFuture<Boolean> futureResult = statisticsContext.gatherDynamicData();

        try {
            assertTrue(futureResult.get());
        } catch (InterruptedException | ExecutionException e) {
            fail("Exception wasn't expected.");
        }

    }

}
