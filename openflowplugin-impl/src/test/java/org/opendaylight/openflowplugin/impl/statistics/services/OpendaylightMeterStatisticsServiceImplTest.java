/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services;

import com.google.common.util.concurrent.FutureCallback;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.opendaylight.controller.md.sal.binding.api.NotificationPublishService;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.impl.rpc.AbstractRequestContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yangtools.yang.common.RpcResult;

/**
 * Test for {@link OpendaylightMeterStatisticsServiceImpl}
 */
public class OpendaylightMeterStatisticsServiceImplTest extends AbstractStatsServiceTest {

    @Captor
    private ArgumentCaptor<MultipartRequestInput> requestInput;

    private RequestContext<Object> rqContext;

    private OpendaylightMeterStatisticsServiceImpl meterStatisticsService;
    @Mock
    private NotificationPublishService notificationPublishService;

    public void setUp() {
        meterStatisticsService = new OpendaylightMeterStatisticsServiceImpl(rqContextStack, deviceContext,
                new AtomicLong(), notificationPublishService);

        rqContext = new AbstractRequestContext<Object>(42L) {
            @Override
            public void close() {
                //NOOP
            }
        };
        Mockito.when(rqContextStack.<Object>createRequestContext()).thenReturn(rqContext);
    }

    @Test
    public void testGetAllMeterConfigStatistics() throws Exception {
        Mockito.doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(Matchers.eq(42L), requestInput.capture(), Matchers.any(FutureCallback.class));

        GetAllMeterConfigStatisticsInputBuilder input = new GetAllMeterConfigStatisticsInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        final Future<RpcResult<GetAllMeterConfigStatisticsOutput>> resultFuture
                = meterStatisticsService.getAllMeterConfigStatistics(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetAllMeterConfigStatisticsOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPMETERCONFIG, requestInput.getValue().getType());
    }

    @Test
    public void testGetAllMeterStatistics() throws Exception {
        Mockito.doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(Matchers.eq(42L), requestInput.capture(), Matchers.any(FutureCallback.class));

        GetAllMeterStatisticsInputBuilder input = new GetAllMeterStatisticsInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        final Future<RpcResult<GetAllMeterStatisticsOutput>> resultFuture
                = meterStatisticsService.getAllMeterStatistics(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetAllMeterStatisticsOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPMETER, requestInput.getValue().getType());
    }

    @Test
    public void testGetMeterFeatures() throws Exception {
        Mockito.doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(Matchers.eq(42L), requestInput.capture(), Matchers.any(FutureCallback.class));

        GetMeterFeaturesInputBuilder input = new GetMeterFeaturesInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        final Future<RpcResult<GetMeterFeaturesOutput>> resultFuture
                = meterStatisticsService.getMeterFeatures(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetMeterFeaturesOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPMETERFEATURES, requestInput.getValue().getType());
    }

    @Test
    public void testGetMeterStatistics() throws Exception {
        Mockito.doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(Matchers.eq(42L), requestInput.capture(), Matchers.any(FutureCallback.class));

        GetMeterStatisticsInputBuilder input = new GetMeterStatisticsInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setMeterId(new MeterId(21L));

        final Future<RpcResult<GetMeterStatisticsOutput>> resultFuture
                = meterStatisticsService.getMeterStatistics(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetMeterStatisticsOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPMETER, requestInput.getValue().getType());
    }
}