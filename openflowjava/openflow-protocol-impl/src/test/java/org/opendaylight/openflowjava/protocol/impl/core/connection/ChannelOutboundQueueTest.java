/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core.connection;

import io.netty.channel.Channel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * UNit tests for ChannelOutboundQueue.
 *
 * @author michal.polkorab
 */
@RunWith(MockitoJUnitRunner.class)
public class ChannelOutboundQueueTest {

    @Mock Channel channel;

    /**
     * Test incorrect queue creation handling.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIncorrectQueueCreation() {
        new ChannelOutboundQueue(channel, 0, null);
    }

    /**
     * Test correct enqueue handling.
     */
    @Test
    public void testEnqueue() {
        ChannelOutboundQueue queue = new ChannelOutboundQueue(channel, 1, null);
        boolean enqueued;
        enqueued = queue.enqueue(new SimpleRpcListener<>("INPUT", "Failed to send INPUT"));
        Assert.assertTrue("Enqueue problem", enqueued);
        enqueued = queue.enqueue(new SimpleRpcListener<>("INPUT", "Failed to send INPUT"));
        Assert.assertFalse("Enqueue problem", enqueued);
    }
}
