/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.translator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.get.aggregate.flow.statistics.from.flow.table._for.given.match.output.AggregatedFlowStatistics;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyAggregateCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.aggregate._case.MultipartReplyAggregateBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

/**
 * Test of {@link AggregatedFlowStatisticsTranslator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AggregatedFlowStatisticsTranslatorTest {

    private AggregatedFlowStatisticsTranslator translator;
    @Mock
    private DeviceInfo deviceInfo;

    @Before
    public void setUp() {
        translator = new AggregatedFlowStatisticsTranslator();
    }

    @Test
    public void testTranslate() {
        MultipartReplyAggregateBuilder aggregateStatsValueBld = new MultipartReplyAggregateBuilder()
                .setByteCount(Uint64.ONE)
                .setFlowCount(Uint32.TWO)
                .setPacketCount(Uint64.valueOf(3));
        MultipartReplyAggregateCaseBuilder inputBld = new MultipartReplyAggregateCaseBuilder()
                .setMultipartReplyAggregate(aggregateStatsValueBld.build());
        MultipartReplyMessageBuilder mpInputBld = new MultipartReplyMessageBuilder()
                .setMultipartReplyBody(inputBld.build());

        final AggregatedFlowStatistics statistics = translator.translate(mpInputBld.build(), deviceInfo, null);

        Assert.assertEquals(aggregateStatsValueBld.getByteCount(), statistics.getByteCount().getValue());
        Assert.assertEquals(aggregateStatsValueBld.getFlowCount(), statistics.getFlowCount().getValue());
        Assert.assertEquals(aggregateStatsValueBld.getPacketCount(), statistics.getPacketCount().getValue());
    }
}