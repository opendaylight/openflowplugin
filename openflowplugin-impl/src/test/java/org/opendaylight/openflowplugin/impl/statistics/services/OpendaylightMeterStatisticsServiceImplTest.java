/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

import com.google.common.util.concurrent.FutureCallback;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorManagerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterConfigStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetAllMeterStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterStatisticsInputBuilder;
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
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Test for {@link OpendaylightMeterStatisticsServiceImpl}.
 */
@Deprecated
public class OpendaylightMeterStatisticsServiceImplTest extends AbstractSingleStatsServiceTest {
    private static final org.opendaylight.yang.gen.v1.urn
                .opendaylight.openflow.common.types.rev130731.MeterId METER_ID = new org.opendaylight.yang.gen.v1.urn
                    .opendaylight.openflow.common.types.rev130731.MeterId(Uint32.valueOf(123));
    @Captor
    private ArgumentCaptor<MultipartRequestInput> requestInput;

    private GetAllMeterStatisticsImpl getAllMeterStatistics;
    private GetMeterStatisticsImpl getMeterStatistics;
    private GetAllMeterConfigStatisticsImpl getAllMeterConfigStatistics;
    private GetMeterFeaturesImpl getMeterFeatures;

    @Override
    public void setUp() {
        final var xid = new AtomicLong();
        final var convertorManager = ConvertorManagerFactory.createDefaultManager();

        getAllMeterStatistics = new GetAllMeterStatisticsImpl(rqContextStack, deviceContext, xid,
            notificationPublishService, convertorManager);
        getMeterStatistics = new GetMeterStatisticsImpl(rqContextStack, deviceContext, xid,
            notificationPublishService, convertorManager);
        getAllMeterConfigStatistics = new GetAllMeterConfigStatisticsImpl(rqContextStack, deviceContext, xid,
            notificationPublishService, convertorManager);
        getMeterFeatures = new GetMeterFeaturesImpl(rqContextStack, deviceContext, xid,
            notificationPublishService, convertorManager);

        doAnswer(answerVoidToCallback).when(outboundQueueProvider)
                .commitEntry(eq(Uint32.valueOf(42)), requestInput.capture(), any(FutureCallback.class));
    }

    @After
    public void tearDown() {
        verify(notificationPublishService).offerNotification(ArgumentMatchers.any());
    }

    @Test
    public void testGetAllMeterConfigStatistics() throws Exception {
        GetAllMeterConfigStatisticsInputBuilder input = new GetAllMeterConfigStatisticsInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        rpcResult = RpcResultBuilder.<Object>success(List.of(
                new MultipartReplyMessageBuilder()
                        .setVersion(EncodeConstants.OF_VERSION_1_3)
                        .setMultipartReplyBody(new MultipartReplyMeterConfigCaseBuilder()
                                .setMultipartReplyMeterConfig(new MultipartReplyMeterConfigBuilder()
                                        .setMeterConfig(List.of(new MeterConfigBuilder()
                                                .setFlags(new MeterFlags(true, false, false, false))
                                                .setMeterId(METER_ID)
                                                .setBands(List.of(new BandsBuilder()
                                                        .setMeterBand(new MeterBandDropCaseBuilder()
                                                                .setMeterBandDrop(new MeterBandDropBuilder()
                                                                        .setBurstSize(Uint32.valueOf(61))
                                                                        .setRate(Uint32.valueOf(62))
                                                                        .setType(MeterBandType.OFPMBTDROP)
                                                                        .build())
                                                                .build())
                                                        .build()))
                                                .build()))
                                        .build())
                                .build())
                        .build()
        )).build();

        final var resultFuture = getAllMeterConfigStatistics.invoke(input.build());

        assertTrue(resultFuture.isDone());
        final var rpcResult = resultFuture.get();
        assertTrue(rpcResult.isSuccessful());
        assertEquals(MultipartType.OFPMPMETERCONFIG, requestInput.getValue().getType());
    }

    @Test
    public void testGetAllMeterStatistics() throws Exception {
        GetAllMeterStatisticsInputBuilder input = new GetAllMeterStatisticsInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        rpcResult = buildMeterStatisticsReply();

        final var resultFuture = getAllMeterStatistics.invoke(input.build());

        assertTrue(resultFuture.isDone());
        final var rpcResult = resultFuture.get();
        assertTrue(rpcResult.isSuccessful());
        assertEquals(MultipartType.OFPMPMETER, requestInput.getValue().getType());
    }

    @Test
    public void testGetMeterFeatures() throws Exception {
        GetMeterFeaturesInputBuilder input = new GetMeterFeaturesInputBuilder()
                .setNode(createNodeRef("unitProt:123"));

        rpcResult = RpcResultBuilder.<Object>success(List.of(
                new MultipartReplyMessageBuilder()
                        .setVersion(EncodeConstants.OF_VERSION_1_3)
                        .setMultipartReplyBody(new MultipartReplyMeterFeaturesCaseBuilder()
                                .setMultipartReplyMeterFeatures(new MultipartReplyMeterFeaturesBuilder()
                                        .setBandTypes(new MeterBandTypeBitmap(true, false))
                                        .setCapabilities(new MeterFlags(true, false, false, false))
                                        .setMaxBands(Uint8.valueOf(71))
                                        .setMaxColor(Uint8.valueOf(72))
                                        .setMaxMeter(Uint32.valueOf(73))
                                        .build())
                                .build())
                        .build()
        )).build();

        final var resultFuture = getMeterFeatures.invoke(input.build());

        assertTrue(resultFuture.isDone());
        final var rpcResult = resultFuture.get();
        assertTrue(rpcResult.isSuccessful());
        assertEquals(MultipartType.OFPMPMETERFEATURES, requestInput.getValue().getType());
    }

    @Test
    public void testGetMeterStatistics() throws Exception {
        GetMeterStatisticsInputBuilder input = new GetMeterStatisticsInputBuilder()
                .setNode(createNodeRef("unitProt:123"))
                .setMeterId(new MeterId(Uint32.valueOf(21)));

        rpcResult = buildMeterStatisticsReply();

        final var resultFuture = getMeterStatistics.invoke(input.build());

        assertTrue(resultFuture.isDone());
        final var rpcResult = resultFuture.get();
        assertTrue(rpcResult.isSuccessful());
        assertEquals(MultipartType.OFPMPMETER, requestInput.getValue().getType());
    }

    protected RpcResult<Object> buildMeterStatisticsReply() {
        return RpcResultBuilder.<Object>success(List.of(
                new MultipartReplyMessageBuilder()
                        .setVersion(EncodeConstants.OF_VERSION_1_3)
                        .setMultipartReplyBody(new MultipartReplyMeterCaseBuilder()
                                .setMultipartReplyMeter(new MultipartReplyMeterBuilder()
                                        .setMeterStats(List.of(new MeterStatsBuilder()
                                                .setMeterId(METER_ID)
                                                .setByteInCount(Uint64.valueOf(81))
                                                .setDurationSec(Uint32.valueOf(82))
                                                .setDurationNsec(Uint32.valueOf(83))
                                                .setFlowCount(Uint32.valueOf(84))
                                                .setPacketInCount(Uint64.valueOf(85))
                                                .setMeterBandStats(List.of(new MeterBandStatsBuilder()
                                                        .setByteBandCount(Uint64.valueOf(86))
                                                        .setPacketBandCount(Uint64.valueOf(87))
                                                        .build()))
                                                .build()))
                                        .build())
                                .build())
                        .build()
        )).build();
    }
}