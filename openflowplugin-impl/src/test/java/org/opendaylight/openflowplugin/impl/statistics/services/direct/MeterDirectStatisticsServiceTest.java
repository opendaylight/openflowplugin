/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetMeterStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.MultipartReplyMeter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.meter._case.multipart.reply.meter.MeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.meter._case.MultipartRequestMeter;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MeterDirectStatisticsServiceTest extends AbstractDirectStatisticsServiceTest {
    static final Long METER_NO = 1L;
    private MeterDirectStatisticsService service;

    @Override
    public void setUp() throws Exception {
        service = new MeterDirectStatisticsService(requestContextStack, deviceContext);
    }

    @Override
    public void testBuildRequestBody() throws Exception {
        final GetMeterStatisticsInput input = mock(GetMeterStatisticsInput.class);

        when(input.getNode()).thenReturn(createNodeRef(NODE_ID));
        when(input.getMeterId()).thenReturn(new MeterId(METER_NO));

        final MultipartRequestMeterCase body = (MultipartRequestMeterCase) service.buildRequestBody(input);
        final MultipartRequestMeter meter = body.getMultipartRequestMeter();

        assertEquals(METER_NO, meter.getMeterId().getValue());
    }

    @Override
    public void testBuildReply() throws Exception {
        final MultipartReply reply = mock(MultipartReply.class);
        final MultipartReplyMeterCase MeterCase = mock(MultipartReplyMeterCase.class);
        final MultipartReplyMeter meter = mock(MultipartReplyMeter.class);
        final MeterStats meterStat = mock(MeterStats.class);
        final List<MeterStats> meterStats = Arrays.asList(meterStat);
        final List<MultipartReply> input = Arrays.asList(reply);

        when(meter.getMeterStats()).thenReturn(meterStats);
        when(MeterCase.getMultipartReplyMeter()).thenReturn(meter);
        when(reply.getMultipartReplyBody()).thenReturn(MeterCase);

        when(meterStat.getMeterId()).thenReturn(new org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MeterId(METER_NO));
        when(meterStat.getByteInCount()).thenReturn(BigInteger.ONE);
        when(meterStat.getPacketInCount()).thenReturn(BigInteger.ONE);

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

        service.storeStatistics(output);
        verify(deviceContext).writeToTransactionWithParentsSlow(eq(LogicalDatastoreType.OPERATIONAL), any(), any());
    }
}