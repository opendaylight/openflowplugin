/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.impl.deserialization.DeserializationFactory;
import org.opendaylight.openflowjava.statistics.CounterEventTypes;
import org.opendaylight.openflowjava.statistics.StatisticsCounters;
import org.opendaylight.openflowjava.util.ByteBufUtils;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Test to count decoder events (counters US_DECODE_SUCCESS, US_DECODE_FAIL and
 * US_RECEIVED_IN_OFJAVA have to be enabled).
 *
 * @author madamjak
 */
@RunWith(MockitoJUnitRunner.class)
public class OFDecoderStatisticsTest {

    @Mock ChannelHandlerContext mockChHndlrCtx;
    @Mock DeserializationFactory mockDeserializationFactory;
    @Mock DataObject mockDataObject;

    private OFDecoder ofDecoder;
    private ByteBuf writeObj;
    private VersionMessageWrapper inMsg;
    private List<Object> outList;
    private StatisticsCounters statCounters;

    /**
     * Sets up test environment Start counting and reset counters before each test.
     */
    @Before
    public void setUp() {
        ofDecoder = new OFDecoder();
        ofDecoder.setDeserializationFactory(mockDeserializationFactory);
        outList = new ArrayList<>();
        statCounters = StatisticsCounters.getInstance();
        statCounters.startCounting(false, 0);
    }

    /**
     * Stop counting after each test.
     */
    @After
    public void tearDown() {
        statCounters.stopCounting();
    }

    /**
     * Test decode success counter.
     */
    @Test
    public void testDecodeSuccessfulCounter() {
        if (!statCounters.isCounterEnabled(CounterEventTypes.US_DECODE_SUCCESS)) {
            Assert.fail("Counter " + CounterEventTypes.US_DECODE_SUCCESS + " is not enable");
        }
        if (!statCounters.isCounterEnabled(CounterEventTypes.US_DECODE_FAIL)) {
            Assert.fail("Counter " + CounterEventTypes.US_DECODE_FAIL + " is not enable");
        }
        if (!statCounters
                .isCounterEnabled(CounterEventTypes.US_RECEIVED_IN_OFJAVA)) {
            Assert.fail("Counter " + CounterEventTypes.US_RECEIVED_IN_OFJAVA + " is not enable");
        }
        int count = 4;
        when(mockDeserializationFactory.deserialize(any(ByteBuf.class), any(Uint8.class))).thenReturn(mockDataObject);

        for (int i = 0; i < count; i++) {
            writeObj = ByteBufUtils.hexStringToByteBuf("16 03 01 00");
            inMsg = new VersionMessageWrapper(Uint8.valueOf(8), writeObj);
            ofDecoder.decode(mockChHndlrCtx, inMsg, outList);
        }

        Assert.assertEquals("Wrong - bad counter value for OFEncoder encode succesfully ",
                count,statCounters.getCounter(CounterEventTypes.US_DECODE_SUCCESS).getCounterValue());
        Assert.assertEquals(
                "Wrong - different between RECEIVED_IN_OFJAVA and (US_DECODE_SUCCESS + US_DECODE_FAIL)",
                statCounters.getCounter(CounterEventTypes.US_RECEIVED_IN_OFJAVA).getCounterValue(),
                statCounters.getCounter(CounterEventTypes.US_DECODE_SUCCESS).getCounterValue()
                + statCounters.getCounter(CounterEventTypes.US_DECODE_FAIL).getCounterValue());
    }

    /**
     * Test fail decode counter.
     */
    @Test
    public void testDecodeFailCounter() {
        if (!statCounters.isCounterEnabled(CounterEventTypes.US_DECODE_SUCCESS)) {
            Assert.fail("Counter " + CounterEventTypes.US_DECODE_SUCCESS + " is not enable");
        }
        if (!statCounters.isCounterEnabled(CounterEventTypes.US_DECODE_FAIL)) {
            Assert.fail("Counter " + CounterEventTypes.US_DECODE_FAIL + " is not enable");
        }
        if (!statCounters.isCounterEnabled(CounterEventTypes.US_RECEIVED_IN_OFJAVA)) {
            Assert.fail("Counter " + CounterEventTypes.US_RECEIVED_IN_OFJAVA + " is not enable");
        }
        int count = 2;
        when(mockDeserializationFactory.deserialize(any(ByteBuf.class), any(Uint8.class)))
            .thenThrow(new IllegalArgumentException());

        for (int i = 0; i < count; i++) {
            writeObj = ByteBufUtils.hexStringToByteBuf("16 03 01 00");
            inMsg = new VersionMessageWrapper(Uint8.valueOf(8), writeObj);
            ofDecoder.decode(mockChHndlrCtx, inMsg, outList);
        }

        Assert.assertEquals(
                "Wrong - bad counter value for OFEncoder encode succesfully ",
                count, statCounters.getCounter(CounterEventTypes.US_DECODE_FAIL).getCounterValue());
        Assert.assertEquals(
                "Wrong - different between RECEIVED_IN_OFJAVA and (US_DECODE_SUCCESS + US_DECODE_FAIL)",
                statCounters.getCounter(CounterEventTypes.US_RECEIVED_IN_OFJAVA).getCounterValue(),
                statCounters.getCounter(CounterEventTypes.US_DECODE_SUCCESS).getCounterValue()
                + statCounters.getCounter(CounterEventTypes.US_DECODE_FAIL).getCounterValue());
    }
}
