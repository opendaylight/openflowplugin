/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.direct.multilayer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.statistics.services.direct.AbstractDirectStatisticsServiceTest;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.MultipartReplyMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.MeterStatsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter._case.MultipartRequestMeter;

public class MeterDirectStatisticsServiceTest extends AbstractDirectStatisticsServiceTest {
    static final Long METER_NO = 1L;
    private MeterDirectStatisticsService service;

    @Override
    public void setUp() throws Exception {
        service = new MeterDirectStatisticsService(requestContextStack, deviceContext, convertorManager, multipartWriterProvider);
    }

    @Override
    public void testBuildRequestBody() throws Exception {
        final GetMeterStatisticsInput input = mock(GetMeterStatisticsInput.class);

        when(input.getNode()).thenReturn(createNodeRef(NODE_ID));
        when(input.getMeterId()).thenReturn(new MeterId(METER_NO));

        final MultipartRequestMeterCase body = (MultipartRequestMeterCase) ((MultipartRequestInput) service
            .buildRequest(new Xid(42L), input))
            .getMultipartRequestBody();

        final MultipartRequestMeter meter = body.getMultipartRequestMeter();

        assertEquals(METER_NO, meter.getMeterId().getValue());
    }

    @Override
    public void testBuildReply() throws Exception {
        final MultipartReply reply = mock(MultipartReply.class);
        final MultipartReplyMeterCase MeterCase = mock(MultipartReplyMeterCase.class);
        final MultipartReplyMeter meter = mock(MultipartReplyMeter.class);
        final MeterStats meterStat = new MeterStatsBuilder()
                .setMeterId(new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId(METER_NO))
                .setByteInCount(BigInteger.ONE)
                .setPacketInCount(BigInteger.ONE)
                .setDurationSec(1L)
                .setDurationNsec(1L)
                .setFlowCount(0L)
                .setMeterBandStats(Collections.emptyList())
                .build();

        final List<MeterStats> meterStats = Collections.singletonList(meterStat);
        final List<MultipartReply> input = Collections.singletonList(reply);

        when(meter.getMeterStats()).thenReturn(meterStats);
        when(MeterCase.getMultipartReplyMeter()).thenReturn(meter);
        when(reply.getMultipartReplyBody()).thenReturn(MeterCase);

        final GetMeterStatisticsOutput output = service.buildReply(input, true);
        assertTrue(output.getMeterStats().size() > 0);

        final org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats stats =
                output.getMeterStats().get(0);

        assertEquals(stats.getMeterId().getValue(), METER_NO);
    }

    @Override
    public void testStoreStatistics() throws Exception {
        final org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats stat = mock(org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats.class);
        when(stat.getMeterId()).thenReturn(new MeterId(METER_NO));

        final List<org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.statistics.reply.MeterStats> stats = Arrays.asList(stat);
        final GetMeterStatisticsOutput output = mock(GetMeterStatisticsOutput.class);
        when(output.getMeterStats()).thenReturn(stats);

        multipartWriterProvider.lookup(MultipartType.OFPMPMETER).get().write(output, true);
        verify(deviceContext).writeToTransactionWithParentsSlow(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }
}
