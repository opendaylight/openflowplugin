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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractDirectStatisticsServiceTest;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter64;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.reply.multipart.reply.body.MultipartReplyMeterStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.request.multipart.request.body.MultipartRequestMeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.DurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.MeterBandStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartReplyBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.MultipartRequest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yangtools.yang.common.Uint32;

public class MeterDirectStatisticsServiceTest extends AbstractDirectStatisticsServiceTest {
    static final Uint32 METER_NO = Uint32.ONE;
    private MeterDirectStatisticsService service;

    @Override
    public void setUp() {
        service = new MeterDirectStatisticsService(requestContextStack,
                                                   deviceContext,
                                                   convertorManager,
                                                   multipartWriterProvider);
    }

    @Override
    public void testBuildRequestBody() {
        final GetMeterStatisticsInput input = mock(GetMeterStatisticsInput.class);

        lenient().when(input.getNode()).thenReturn(createNodeRef(NODE_ID));
        when(input.getMeterId()).thenReturn(new MeterId(METER_NO));

        final MultipartRequestMeterStats body = (MultipartRequestMeterStats) ((MultipartRequest) service
            .buildRequest(new Xid(Uint32.valueOf(42L)), input))
            .getMultipartRequestBody();

        assertEquals(METER_NO, body.getStatMeterId().getValue());
    }

    @Override
    public void testBuildReply() {
        final MeterStats meterStat = new MeterStatsBuilder()
                .setMeterId(new MeterId(METER_NO))
                .setByteInCount(new Counter64(BigInteger.ONE))
                .setPacketInCount(new Counter64(BigInteger.ONE))
                .setDuration(new DurationBuilder()
                        .setSecond(new Counter32(1L))
                        .setNanosecond(new Counter32(1L))
                        .build())
                .setFlowCount(new Counter32(0L))
                .setMeterBandStats(new MeterBandStatsBuilder().build())
                .build();

        final MultipartReply reply = new MultipartReplyBuilder()
                .setMultipartReplyBody(new MultipartReplyMeterStatsBuilder()
                        .setMeterStats(Collections.singletonList(meterStat))
                        .build())
                .build();

        final List<MultipartReply> input = Collections.singletonList(reply);

        final GetMeterStatisticsOutput output = service.buildReply(input, true);
        assertTrue(output.getMeterStats().size() > 0);

        final MeterStats stats = output.getMeterStats().get(0);
        assertEquals(stats.getMeterId().getValue(), METER_NO);
    }

    @Override
    public void testStoreStatistics() {
        final org.opendaylight.yang.gen.v1.urn
                .opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats stat =
                mock(org.opendaylight.yang.gen.v1.urn
                        .opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats.class);
        when(stat.getMeterId()).thenReturn(new MeterId(METER_NO));

        final List<org.opendaylight.yang.gen.v1.urn
                .opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats>
                stats = Collections.singletonList(stat);
        final GetMeterStatisticsOutput output = mock(GetMeterStatisticsOutput.class);
        when(output.getMeterStats()).thenReturn(stats);

        multipartWriterProvider.lookup(MultipartType.OFPMPMETER).get().write(output, true);
        verify(deviceContext).writeToTransactionWithParentsSlow(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }
}
