/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services;

import com.google.common.util.concurrent.FutureCallback;
import java.math.BigInteger;
import java.util.Collections;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManager;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterBandTypeBitmap;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.MeterBandDropCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.meter.band.header.meter.band.meter.band.drop._case.MeterBandDropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterConfigCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterFeaturesCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.MultipartReplyMeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.MeterStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.meter.stats.MeterBandStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.MultipartReplyMeterConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.MeterConfigBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.config._case.multipart.reply.meter.config.meter.config.BandsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter.features._case.MultipartReplyMeterFeaturesBuilder;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link OpendaylightMeterStatisticsServiceImpl}
 */
public class OpendaylightMeterStatisticsServiceImplTest extends AbstractSingleStatsServiceTest {

    private static final org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId METER_ID = new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId(123L);
    @Captor
    private ArgumentCaptor<MultipartRequestInput> requestInput;

    private OpendaylightMeterStatisticsServiceImpl meterStatisticsService;

    public void setUp() {
        final ConvertorManager convertorManager = ConvertorManagerFactory.createDefaultManager();
        meterStatisticsService = new OpendaylightMeterStatisticsServiceImpl(rqContextStack, deviceContext,
                new AtomicLong(), notificationPublishService, convertorManager);

        Mockito.doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(Matchers.eq(42L), requestInput.capture(), Matchers.any(FutureCallback.class));
    }

    @After
    public void tearDown() throws Exception {
        Mockito.verify(notificationPublishService).offerNotification(Matchers.<Notification>any());
    }

    @Test
    public void testGetAllMeterConfigStatistics() throws Exception {
        GetAllMeterConfigStatisticsInputBuilder input = new GetAllMeterConfigStatisticsInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        rpcResult = RpcResultBuilder.<Object>success(Collections.singletonList(
                new MultipartReplyMessageBuilder()
                        .setVersion(OFConstants.OFP_VERSION_1_3)
                        .setMultipartReplyBody(new MultipartReplyMeterConfigCaseBuilder()
                                .setMultipartReplyMeterConfig(new MultipartReplyMeterConfigBuilder()
                                        .setMeterConfig(Collections.singletonList(new MeterConfigBuilder()
                                                .setFlags(new MeterFlags(true, false, false, false))
                                                .setMeterId(METER_ID)
                                                .setBands(Collections.singletonList(new BandsBuilder()
                                                        .setMeterBand(new MeterBandDropCaseBuilder()
                                                                .setMeterBandDrop(new MeterBandDropBuilder()
                                                                        .setBurstSize(61L)
                                                                        .setRate(62L)
                                                                        .setType(MeterBandType.OFPMBTDROP)
                                                                        .build())
                                                                .build())
                                                        .build()))
                                                .build()))
                                        .build())
                                .build())
                        .build()
        )).build();

        final Future<RpcResult<GetAllMeterConfigStatisticsOutput>> resultFuture
                = meterStatisticsService.getAllMeterConfigStatistics(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetAllMeterConfigStatisticsOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPMETERCONFIG, requestInput.getValue().getType());
    }

    @Test
    public void testGetAllMeterStatistics() throws Exception {
        GetAllMeterStatisticsInputBuilder input = new GetAllMeterStatisticsInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        rpcResult = buildMeterStatisticsReply();

        final Future<RpcResult<GetAllMeterStatisticsOutput>> resultFuture
                = meterStatisticsService.getAllMeterStatistics(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetAllMeterStatisticsOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPMETER, requestInput.getValue().getType());
    }

    @Test
    public void testGetMeterFeatures() throws Exception {
        GetMeterFeaturesInputBuilder input = new GetMeterFeaturesInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        rpcResult = RpcResultBuilder.<Object>success(Collections.singletonList(
                new MultipartReplyMessageBuilder()
                        .setVersion(OFConstants.OFP_VERSION_1_3)
                        .setMultipartReplyBody(new MultipartReplyMeterFeaturesCaseBuilder()
                                .setMultipartReplyMeterFeatures(new MultipartReplyMeterFeaturesBuilder()
                                        .setBandTypes(new MeterBandTypeBitmap(true, false))
                                        .setCapabilities(new MeterFlags(true, false, false, false))
                                        .setMaxBands((short) 71)
                                        .setMaxColor((short) 72)
                                        .setMaxMeter(73L)
                                        .build())
                                .build())
                        .build()
        )).build();

        final Future<RpcResult<GetMeterFeaturesOutput>> resultFuture
                = meterStatisticsService.getMeterFeatures(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetMeterFeaturesOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPMETERFEATURES, requestInput.getValue().getType());
    }

    @Test
    public void testGetMeterStatistics() throws Exception {
        GetMeterStatisticsInputBuilder input = new GetMeterStatisticsInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setMeterId(new MeterId(21L));

        rpcResult = buildMeterStatisticsReply();

        final Future<RpcResult<GetMeterStatisticsOutput>> resultFuture
                = meterStatisticsService.getMeterStatistics(input.build());

        Assert.assertTrue(resultFuture.isDone());
        final RpcResult<GetMeterStatisticsOutput> rpcResult = resultFuture.get();
        Assert.assertTrue(rpcResult.isSuccessful());
        Assert.assertEquals(MultipartType.OFPMPMETER, requestInput.getValue().getType());
    }

    protected RpcResult<Object> buildMeterStatisticsReply() {
        return RpcResultBuilder.<Object>success(Collections.singletonList(
                new MultipartReplyMessageBuilder()
                        .setVersion(OFConstants.OFP_VERSION_1_3)
                        .setMultipartReplyBody(new MultipartReplyMeterCaseBuilder()
                                .setMultipartReplyMeter(new MultipartReplyMeterBuilder()
                                        .setMeterStats(Collections.singletonList(new MeterStatsBuilder()
                                                .setMeterId(METER_ID)
                                                .setByteInCount(BigInteger.valueOf(81L))
                                                .setDurationSec(82L)
                                                .setDurationNsec(83L)
                                                .setFlowCount(84L)
                                                .setPacketInCount(BigInteger.valueOf(85L))
                                                .setMeterBandStats(Collections.singletonList(new MeterBandStatsBuilder()
                                                        .setByteBandCount(BigInteger.valueOf(86L))
                                                        .setPacketBandCount(BigInteger.valueOf(87L))
                                                        .build()))
                                                .build()))
                                        .build())
                                .build())
                        .build()
        )).build();
    }
}