/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyShort;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.openflowjava.protocol.api.util.EncodeConstants;
import org.opendaylight.openflowjava.protocol.impl.core.connection.MessageListenerWrapper;
import org.opendaylight.openflowjava.protocol.impl.serialization.SerializationFactory;
import org.opendaylight.openflowjava.statistics.CounterEventTypes;
import org.opendaylight.openflowjava.statistics.StatisticsCounters;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.binding.DataObject;

/**
 * Test counters for encoding (at least DS_ENCODE_SUCCESS, DS_ENCODE_FAIL and DS_FLOW_MODS_SENT counters have to be enabled)
 * @author madamjak
 *
 */
public class OFEncoderStatisticsTest {

    @Mock ChannelHandlerContext mockChHndlrCtx ;
    @Mock SerializationFactory mockSerializationFactory ;
    @Mock MessageListenerWrapper wrapper;
    @Mock OfHeader mockMsg ;
    @Mock ByteBuf mockOut ;
    @Mock Future<Void> future;
    @Mock GenericFutureListener<Future<Void>> listener;
    @Mock FlowModInput mockFlowModInput;

    private StatisticsCounters statCounters;
    private OFEncoder ofEncoder;

    /**
     * Initialize tests, start and reset counters before each test
     */
    @Before
    public void initTlest(){
        MockitoAnnotations.initMocks(this);
        ofEncoder = new OFEncoder() ;
        ofEncoder.setSerializationFactory(mockSerializationFactory) ;
        statCounters = StatisticsCounters.getInstance();
        statCounters.startCounting(false, 0);
    }

    /**
     * Stop counting after each test
     */
    @After
    public void tierDown(){
        statCounters.stopCounting();
    }

    /**
     * Test counting of success encode (counter DS_ENCODE_SUCCESS has to be enabled)
     */
    @Test
    public void testEncodeSuccessCounter() {
        CounterEventTypes cet = CounterEventTypes.DS_ENCODE_SUCCESS;
        if(! statCounters.isCounterEnabled(cet)){
            Assert.fail("Counter " + cet + " is not enabled.");
        }
        int count = 4;
        when(mockOut.readableBytes()).thenReturn(1);
        when(wrapper.getMsg()).thenReturn(mockMsg);
        when(wrapper.getMsg().getVersion()).thenReturn((short) EncodeConstants.OF13_VERSION_ID);
        try {
            for(int i = 0; i< count; i++){
                ofEncoder.encode(mockChHndlrCtx, wrapper, mockOut);
            }
        } catch (Exception e) {
            Assert.fail();
        }
        Assert.assertEquals("Wrong - bad counter value for OFEncoder encode succesfully ", count, statCounters.getCounter(cet).getCounterValue());
    }

    /**
     * Test counting of flow-mod sent (counter DS_FLOW_MODS_SENT has to be enabled)
     */
    @Test
    public void testFlowModSentCounter() {
        CounterEventTypes cet = CounterEventTypes.DS_FLOW_MODS_SENT;
        if(! statCounters.isCounterEnabled(cet)){
            Assert.fail("Counter " + cet + " is not enabled.");
        }
        int count = 4;
        when(mockOut.readableBytes()).thenReturn(1);
        when(wrapper.getMsg()).thenReturn(mockFlowModInput);
        when(wrapper.getMsg().getVersion()).thenReturn((short) EncodeConstants.OF13_VERSION_ID);
        try {
            for(int i = 0; i< count; i++){
                ofEncoder.encode(mockChHndlrCtx, wrapper, mockOut);
            }
        } catch (Exception e) {
            Assert.fail();
        }
        Assert.assertEquals("Wrong - bad counter value for OFEncoder flow-mod sent", count, statCounters.getCounter(cet).getCounterValue());
    }
    /**
     * Test counting of encode fail (counter DS_ENCODE_FAIL has to be enabled)
     */

    @Test
    public void testEncodeEncodeFailCounter() {
        CounterEventTypes cet = CounterEventTypes.DS_ENCODE_FAIL;
        if(! statCounters.isCounterEnabled(cet)){
            Assert.fail("Counter " + cet + " is not enabled.");
        }
        int count = 2;
        when(wrapper.getMsg()).thenReturn(mockMsg);
        when(wrapper.getListener()).thenReturn(listener);
        when(wrapper.getMsg().getVersion()).thenReturn((short) EncodeConstants.OF13_VERSION_ID);
        doThrow(new IllegalArgumentException()).when(mockSerializationFactory).messageToBuffer(anyShort(),any(ByteBuf.class), any(DataObject.class));
        try {
            for(int i = 0; i< count; i++){
                ofEncoder.encode(mockChHndlrCtx, wrapper, mockOut);
            }
        } catch (Exception e) {
            Assert.fail();
        }
        Assert.assertEquals("Wrong - bad counter value for OFEncoder fail encode", count, statCounters.getCounter(cet).getCounterValue());
    }
}
