/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.device;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.ConnectionAdapter;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.MessageSpy;

/**
 * Test for {@link PacketInRateLimiter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PacketInRateLimiterTest {

    private PacketInRateLimiter rateLimiter;
    @Mock
    private ConnectionAdapter connectionAdapter;
    @Mock
    private MessageSpy messageSpy;
    private InOrder caOrdered;

    @Before
    public void setUp() throws Exception {
        caOrdered = Mockito.inOrder(connectionAdapter);
        rateLimiter = new PacketInRateLimiter(connectionAdapter, 4, 10, messageSpy, 0.5f);
    }

    @Test
    public void testDisableFlow() throws Exception {
        rateLimiter.disableFlow();

        Mockito.verify(messageSpy).spyMessage(DeviceContext.class, MessageSpy.STATISTIC_GROUP.OFJ_BACKPRESSURE_ON);
        Mockito.verify(connectionAdapter).setPacketInFiltering(true);
    }

    @Test
    public void testEnableFlow() throws Exception {
        rateLimiter.enableFlow();

        Mockito.verify(messageSpy).spyMessage(DeviceContext.class, MessageSpy.STATISTIC_GROUP.OFJ_BACKPRESSURE_OFF);
        Mockito.verify(connectionAdapter).setPacketInFiltering(false);
    }

    @Test
    public void testDrainLowWaterMark() throws Exception {
        // scenario:
        // occupy 4 permits
        // drain low water mark = lwm temporarily set to 50% (= 2) and get limited
        // need to free 2 permits to escape the limit and reset lwm
        // now free to get more than 2 permits

        Assert.assertEquals(0, rateLimiter.getOccupiedPermits());
        Assert.assertFalse(rateLimiter.isLimited());
        acquirePermits(4);
        Assert.assertEquals(4, rateLimiter.getOccupiedPermits());

        // drain current
        rateLimiter.drainLowWaterMark();
        Assert.assertEquals(4, rateLimiter.getOccupiedPermits());
        Assert.assertTrue(rateLimiter.isLimited());
        caOrdered.verify(connectionAdapter).setPacketInFiltering(true);

        // release 1 permit ->  3 occupied but threshold = 2 -> stay limited
        rateLimiter.releasePermit();
        Assert.assertEquals(3, rateLimiter.getOccupiedPermits());
        Assert.assertTrue(rateLimiter.isLimited());

        // release 1 permit ->  2 occupied but threshold = 2 -> escape limit
        rateLimiter.releasePermit();
        Assert.assertEquals(2, rateLimiter.getOccupiedPermits());
        Assert.assertFalse(rateLimiter.isLimited());
        caOrdered.verify(connectionAdapter).setPacketInFiltering(false);

        // lwm is reset
        acquirePermits(4);
        Assert.assertEquals(6, rateLimiter.getOccupiedPermits());
        Assert.assertFalse(rateLimiter.isLimited());

        Mockito.verify(connectionAdapter, Mockito.times(2)).setPacketInFiltering(Matchers.anyBoolean());
    }

    private void acquirePermits(int permits) {
        for (int i = 0; i < permits; i++) {
            final boolean gainedPermit = rateLimiter.acquirePermit();
            if (!gainedPermit) {
                throw new IllegalStateException("not enough permits");
            }
        }
    }

    private void releasePermits(int permits) {
        for (int i = 0; i < permits; i++) {
            rateLimiter.releasePermit();
        }
    }

    @Test
    public void testAcquirePermit() throws Exception {
        Assert.assertEquals(0, rateLimiter.getOccupiedPermits());
        Assert.assertFalse(rateLimiter.isLimited());

        // approach hwm
        acquirePermits(10);
        Assert.assertEquals(10, rateLimiter.getOccupiedPermits());
        Assert.assertFalse(rateLimiter.isLimited());

        // hit hwm
        Assert.assertFalse(rateLimiter.acquirePermit());
        Assert.assertEquals(10, rateLimiter.getOccupiedPermits());
        Assert.assertTrue(rateLimiter.isLimited());
        caOrdered.verify(connectionAdapter).setPacketInFiltering(true);

        // approach lwm
        releasePermits(5);
        Assert.assertEquals(5, rateLimiter.getOccupiedPermits());
        Assert.assertTrue(rateLimiter.isLimited());

        // cross lwm
        rateLimiter.releasePermit();
        Assert.assertEquals(4, rateLimiter.getOccupiedPermits());
        Assert.assertFalse(rateLimiter.isLimited());
        caOrdered.verify(connectionAdapter).setPacketInFiltering(false);

        Mockito.verify(connectionAdapter, Mockito.times(2)).setPacketInFiltering(Matchers.anyBoolean());
    }

    @Test
    public void testChangeWaterMarks1() throws Exception {
        rateLimiter.changeWaterMarks(2, 4);
        acquirePermits(4);
        Assert.assertEquals(4, rateLimiter.getOccupiedPermits());
        Assert.assertFalse(rateLimiter.isLimited());

        // hit hwm
        Assert.assertFalse(rateLimiter.acquirePermit());
        Assert.assertEquals(4, rateLimiter.getOccupiedPermits());
        Assert.assertTrue(rateLimiter.isLimited());
        caOrdered.verify(connectionAdapter).setPacketInFiltering(true);

        // approach lwm
        rateLimiter.releasePermit();
        Assert.assertEquals(3, rateLimiter.getOccupiedPermits());
        Assert.assertTrue(rateLimiter.isLimited());

        // cross lwm, escape limit
        rateLimiter.releasePermit();
        Assert.assertEquals(2, rateLimiter.getOccupiedPermits());
        Assert.assertFalse(rateLimiter.isLimited());
        caOrdered.verify(connectionAdapter).setPacketInFiltering(false);

        Mockito.verify(connectionAdapter, Mockito.times(2)).setPacketInFiltering(Matchers.anyBoolean());
    }

    @Test
    public void testChangeWaterMarks2() throws Exception {
        // draining to lwm/occupied = 3/6
        acquirePermits(6);
        rateLimiter.drainLowWaterMark();
        Assert.assertEquals(6, rateLimiter.getOccupiedPermits());
        Assert.assertTrue(rateLimiter.isLimited());
        caOrdered.verify(connectionAdapter).setPacketInFiltering(true);

        rateLimiter.changeWaterMarks(7, 12);
        Assert.assertEquals(6, rateLimiter.getOccupiedPermits());
        Assert.assertTrue(rateLimiter.isLimited());

        // new lwm is equal to current occupied permits - we can acquire more but flow is still limited
        acquirePermits(1);
        Assert.assertTrue(rateLimiter.isLimited());
        Assert.assertEquals(7, rateLimiter.getOccupiedPermits());

        // cross lwm, escape old lwm limit, reset lwm
        rateLimiter.releasePermit();
        Assert.assertEquals(6, rateLimiter.getOccupiedPermits());
        Assert.assertFalse(rateLimiter.isLimited());
        caOrdered.verify(connectionAdapter).setPacketInFiltering(false);

        // free to reach hwm of 12
        acquirePermits(6);
        Assert.assertEquals(12, rateLimiter.getOccupiedPermits());
        Assert.assertFalse(rateLimiter.isLimited());

        Mockito.verify(connectionAdapter, Mockito.times(2)).setPacketInFiltering(Matchers.anyBoolean());
    }
}