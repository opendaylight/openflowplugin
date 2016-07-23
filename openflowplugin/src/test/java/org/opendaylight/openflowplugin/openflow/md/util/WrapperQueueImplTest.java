/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.util;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowplugin.api.openflow.md.core.ConnectionConductor;
import org.opendaylight.openflowplugin.api.openflow.md.queue.WaterMarkListener;
import org.opendaylight.openflowplugin.openflow.md.queue.WrapperQueueImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class WrapperQueueImplTest {

    protected static final Logger LOG = LoggerFactory
            .getLogger(WrapperQueueImplTest.class);

    @Mock
    private ConnectionConductor connectionConductor;

    @Mock
    private WaterMarkListener waterMarkListener;

    private WrapperQueueImpl<Integer> wrapperQueueImpl;
    private final int capacity = 100;
    private Queue<Integer> queueDefault;
    private int highWaterMark = 80;
    private int lowWaterMark = 65;

    /**
     * Setup before tests
     */
    @Before
    public void setUp() {
        queueDefault = new ArrayBlockingQueue<>(capacity);

        wrapperQueueImpl = new WrapperQueueImpl<>(capacity, queueDefault,
                waterMarkListener);
    }

    /**
     * Test for check if wrapper is not null
     */
    @Test
    public void testWrapperQueueImpl() {
        Assert.assertNotNull("Wrapper can not be null.", wrapperQueueImpl);
    }

    /**
     * Test for set setAutoRead on false on high water mark
     */
    @Test
    public void testReadOnHighWaterMark() {

        Assert.assertFalse("Wrapper must be not flooded at the start.",
                wrapperQueueImpl.isFlooded());

        push(79);
        Assert.assertFalse("Wrapper should not be flooded.",
                wrapperQueueImpl.isFlooded());
        Mockito.verify(waterMarkListener, Mockito.times(0)).onHighWaterMark();

        push(1);
        Assert.assertTrue("Wrapper should be flooded.",
                wrapperQueueImpl.isFlooded());
        Mockito.verify(waterMarkListener, Mockito.times(1)).onHighWaterMark();

        Assert.assertEquals(
                "Size of queue has to be equals to 80% of capacity of queue",
                highWaterMark, queueDefault.size());
    }

    /**
     *
     */
    private void push(int size) {
        for (int i = 0; i < size; i++) {
            try {
                wrapperQueueImpl.offer(i);
            } catch (Exception e) {
                LOG.error("Failed to offer item to queue.", e);
            }
        }
    }

    /**
     * Test for setAutoRead on true on low water mark
     */
    @Test
    public void testReadOnLowWaterMark() {
        Mockito.verify(waterMarkListener, Mockito.times(0)).onHighWaterMark();
        push(80);
        Assert.assertTrue("Wrapper should be flooded.",
                wrapperQueueImpl.isFlooded());
        Mockito.verify(waterMarkListener, Mockito.times(1)).onHighWaterMark();

        Assert.assertEquals(
                "Size of queue has to be equals to 80% of capacity of queue",
                highWaterMark, queueDefault.size());

        poll(14);
        Mockito.verify(waterMarkListener, Mockito.times(0)).onLowWaterMark();
        Assert.assertTrue("Wrapper should be still flooded.",
                wrapperQueueImpl.isFlooded());

        poll(1);
        Mockito.verify(waterMarkListener, Mockito.times(1)).onLowWaterMark();

        Assert.assertEquals(
                "Size of queue has to be equals to 65% on lowWaterMark.",
                lowWaterMark, queueDefault.size());
        Assert.assertFalse("Wrapped should be not flooded.",
                wrapperQueueImpl.isFlooded());
    }

    /**
     * Polling messages
     */
    private void poll(int size) {

        for (int i = 0; i < size; i++) {
            wrapperQueueImpl.poll();
        }
    }

    /**
     * Test for one cycle.
     */
    @Test
    public void testEndReadOnHWMStartOnLWM() {

        Assert.assertFalse("Wrapper should not be flooded",
                wrapperQueueImpl.isFlooded());
        Mockito.verify(waterMarkListener, Mockito.times(0)).onLowWaterMark();
        Mockito.verify(waterMarkListener, Mockito.times(0)).onHighWaterMark();

        push(81);
        Assert.assertTrue("Wrapper should be flooded",
                wrapperQueueImpl.isFlooded());
        Mockito.verify(waterMarkListener, Mockito.times(0)).onLowWaterMark();
        Mockito.verify(waterMarkListener, Mockito.times(1)).onHighWaterMark();

        poll(17);
        Assert.assertFalse("Wrapper should not be flooded",
                wrapperQueueImpl.isFlooded());
        Mockito.verify(waterMarkListener, Mockito.times(1)).onLowWaterMark();
        Mockito.verify(waterMarkListener, Mockito.times(1)).onHighWaterMark();

        push(18);
        Assert.assertTrue("Wrapper should be flooded",
                wrapperQueueImpl.isFlooded());

        Mockito.verify(waterMarkListener, Mockito.times(1)).onLowWaterMark();
        Mockito.verify(waterMarkListener, Mockito.times(2)).onHighWaterMark();
    }
}
