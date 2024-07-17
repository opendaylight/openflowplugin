/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services.direct.singlelayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractDirectStatisticsServiceTest;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyMeterStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.request.multipart.request.body.MultipartRequestMeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.MeterBandStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yangtools.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

public class MeterDirectStatisticsServiceTest extends AbstractDirectStatisticsServiceTest {
    static final Uint32 METER_NO = Uint32.ONE;
    private SingleGetMeterStatistics service;

    @Override
    public void setUp() {
        service = new SingleGetMeterStatistics(requestContextStack,
                                                   deviceContext,
                                                   convertorManager,
                                                   multipartWriterProvider);
    }

    @Override
    public void testBuildRequestBody() {
        final var body = (MultipartRequestMeterStats) ((MultipartRequest) service
            .buildRequest(new Xid(Uint32.valueOf(42L)), new GetMeterStatisticsInputBuilder()
                .setMeterId(new MeterId(METER_NO))
                .setNode(createNodeRef(NODE_ID))
                .build()))
            .getMultipartRequestBody();

        assertEquals(METER_NO, body.getStatMeterId().getValue());
    }

    @Override
    public void testBuildReply() {
        final var output = service.buildReply(List.of(new MultipartReplyBuilder()
            .setMultipartReplyBody(new MultipartReplyMeterStatsBuilder()
                .setMeterStats(BindingMap.of(new MeterStatsBuilder()
                    .setMeterId(new MeterId(METER_NO))
                    .setByteInCount(new Counter64(Uint64.ONE))
                    .setPacketInCount(new Counter64(Uint64.ONE))
                    .setDuration(new DurationBuilder()
                        .setSecond(new Counter32(Uint32.ONE))
                        .setNanosecond(new Counter32(Uint32.ONE))
                        .build())
                    .setFlowCount(new Counter32(Uint32.ZERO))
                    .setMeterBandStats(new MeterBandStatsBuilder().build())
                    .build()))
                .build())
            .build()), true);
        assertTrue(output.nonnullMeterStats().size() > 0);

        final MeterStats stats = output.nonnullMeterStats().values().iterator().next();
        assertEquals(stats.getMeterId().getValue(), METER_NO);
    }

    @Override
    public void testStoreStatistics() {
        multipartWriterProvider.lookup(MultipartType.OFPMPMETER).orElseThrow()
            .write(new GetMeterStatisticsOutputBuilder()
                .setMeterStats(BindingMap.of(new org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918
                    .meter.statistics.reply.MeterStatsBuilder()
                    .setMeterId(new MeterId(METER_NO))
                    .build()))
                .build(), true);
        verify(deviceContext).writeToTransactionWithParentsSlow(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }
}
