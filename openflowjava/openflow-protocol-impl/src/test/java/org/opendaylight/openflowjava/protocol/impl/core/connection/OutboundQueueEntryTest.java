/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core.connection;

import com.google.common.util.concurrent.FutureCallback;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.openflowjava.protocol.api.connection.OutboundQueueException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowRemovedMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReplyMessageBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInputBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link OutboundQueueEntry} class test.
 */
@RunWith(MockitoJUnitRunner.class)
public class OutboundQueueEntryTest {

    private static final Logger LOG = LoggerFactory.getLogger(OutboundQueueEntryTest.class);

    private static final Uint8 VERSION = Uint8.valueOf(13);
    private static final Uint32 VALUE = Uint32.ONE;

    private Integer failCounter = 0;

    @Mock
    private OfHeader ofHeader;
    @Mock
    private FutureCallback<OfHeader> futureCallback;

    private final OutboundQueueEntry outboundQueueEntry = new OutboundQueueEntry();
    private final OfHeader barrierInput = new BarrierInputBuilder().setVersion(VERSION).setXid(VALUE).build();
    private final OfHeader packetOutInput = new PacketOutInputBuilder().setVersion(VERSION).setXid(VALUE).build();
    private final OfHeader multipartReplyMessage = new MultipartReplyMessageBuilder().setVersion(VERSION).setXid(VALUE)
            .setFlags(new MultipartRequestFlags(false)).build();
    private final OfHeader flowModInput = new FlowModInputBuilder().setVersion(VERSION).setXid(VALUE).build();
    private final OfHeader flowRemoved = new FlowRemovedMessageBuilder().setVersion(VERSION).setXid(VALUE).build();

    @Test
    public void commit() {
        outboundQueueEntry.commit(ofHeader, futureCallback);
        Assert.assertTrue(outboundQueueEntry.isCommitted());
        Assert.assertFalse(outboundQueueEntry.isCompleted());
        Assert.assertFalse(outboundQueueEntry.isBarrier());
    }

    @Test
    public void reset() {
        outboundQueueEntry.commit(ofHeader, futureCallback);
        Assert.assertTrue(outboundQueueEntry.isCommitted());

        outboundQueueEntry.reset();
        Assert.assertFalse(outboundQueueEntry.isCommitted());
    }

    @Test
    public void isBarrier() {
        outboundQueueEntry.commit(barrierInput, futureCallback);
        Assert.assertTrue(outboundQueueEntry.isBarrier());
    }

    @Test
    public void takeMessage() {
        outboundQueueEntry.commit(packetOutInput, futureCallback);
        outboundQueueEntry.takeMessage();
        Mockito.verify(futureCallback).onSuccess(Mockito.any());
    }

    @Test
    public void complete() {
        final boolean result = outboundQueueEntry.complete(multipartReplyMessage);
        Assert.assertTrue(result);
        Assert.assertTrue(outboundQueueEntry.isCompleted());
    }

    @Test(expected = IllegalStateException.class)
    public void completeTwice() {
        outboundQueueEntry.complete(multipartReplyMessage);
        outboundQueueEntry.complete(multipartReplyMessage);
    }

    @Test
    public void fail() {
        outboundQueueEntry.commit(ofHeader, futureCallback);
        outboundQueueEntry.fail(null);
        Mockito.verify(futureCallback).onFailure(Mockito.<OutboundQueueException>any());
    }

    private Integer increaseFailCounter() {
        return ++this.failCounter;
    }

    @Test
    public void test() {

        final FutureCallback<OfHeader> result = new FutureCallback<>() {
            @Override
            public void onSuccess(@Nullable OfHeader header) {
                LOG.info("onSuccess: xid: {}", header.getXid());
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOG.info("onFailure! Error: {}", throwable);
                LOG.info("Failure called {} time", increaseFailCounter());
            }
        };

        /** This scenario creates entry with XID 1 then commit it, fail it and again commit it */
        /** Simulates behavior when entry is committed after fail */
        /** It shouldn't be in state completed and still have callback, it can consume all threads in thread pool */

        /** Entry but no callback */
        outboundQueueEntry.commit(flowModInput, null);
        /** Failed entry for whatever reason */
        outboundQueueEntry.fail(null);
        /** Commit the same entry adding callback */
        outboundQueueEntry.commit(flowModInput, result);

        Assert.assertTrue(outboundQueueEntry.isCompleted());
        Assert.assertTrue(outboundQueueEntry.isCommitted());

        /** This is check that no callback is in entry stuck */
        Assert.assertFalse(outboundQueueEntry.hasCallback());

        Assert.assertTrue(this.failCounter == 1);
    }
}
