/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core.connection;

import static org.mockito.Mockito.doReturn;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yangtools.yang.common.Uint32;

/**
 * Unit tests for ChannelOutboundQueue02.
 *
 * @author madamjak
 */
@RunWith(MockitoJUnitRunner.class)
public class ChannelOutboundQueue02Test {
    private static int counter;
    private static final int CHANNEL_OUTBOUND_QUEUE_SIZE = 1024;
    private static final int RPC_RESPONSE_EXPIRATION = 1;
    private static final RemovalListener<RpcResponseKey, ResponseExpectedRpcListener<?>> REMOVAL_LISTENER =
        notification -> notification.getValue().discard();

    @Mock EchoInput echoInput;
    @Mock BarrierInput barrierInput;
    @Mock EchoReplyInput echoReplyInput;
    @Mock ExperimenterInput experimenterInput;
    private ConnectionAdapterImpl adapter;
    private Cache<RpcResponseKey, ResponseExpectedRpcListener<?>> cache;

    /**
     * Initialize mocks.
     */
    @Before
    public void setUp() {
        mockXid(echoInput);
        mockXid(barrierInput);
        mockXid(echoReplyInput);
        mockXid(experimenterInput);
    }

    private static void mockXid(final OfHeader message) {
        doReturn(Uint32.ZERO).when(message).getXid();
    }

    /**
     * Disconnect adapter after each test.
     */
    @After
    public void tearDown() {
        if (adapter != null && adapter.isAlive()) {
            adapter.disconnect();
        }
    }

    /**
     * Test write to closed / opened channel.
     */
    @Test
    public void test01() throws Exception {
        final EmbeddedChannel ec = new EmbeddedChannel(new EmbededChannelHandler());
        adapter = new ConnectionAdapterImpl(ec, InetSocketAddress.createUnresolved("localhost", 9876), true,
                CHANNEL_OUTBOUND_QUEUE_SIZE);
        cache = CacheBuilder.newBuilder().concurrencyLevel(1).expireAfterWrite(
                RPC_RESPONSE_EXPIRATION, TimeUnit.MINUTES).removalListener(REMOVAL_LISTENER).build();
        adapter.setResponseCache(cache);
        final ChannelOutboundQueue cq = (ChannelOutboundQueue) ec.pipeline().last();
        counter = 0;
        adapter.barrier(barrierInput);
        adapter.echo(echoInput);
        cq.channelInactive(ec.pipeline().lastContext());
        ec.runPendingTasks();
        Assert.assertEquals("Wrong - ChannelOutboundHandlerAdapter.write was invoked on closed channel",0, counter);
        cq.channelActive(ec.pipeline().lastContext());
        counter = 0;
        adapter.barrier(barrierInput);
        adapter.experimenter(experimenterInput);
        ec.runPendingTasks();
        Assert.assertEquals("Wrong - ChannelOutboundHandlerAdapter.write has not been invoked on opened channel",
                2, counter);
    }

    /**
     * Test write to read only / writable channel.
     */
    @Test
    public void test02() {
        final ChangeWritableEmbededChannel ec = new ChangeWritableEmbededChannel(new EmbededChannelHandler());
        adapter = new ConnectionAdapterImpl(ec, InetSocketAddress.createUnresolved("localhost", 9876), true,
                CHANNEL_OUTBOUND_QUEUE_SIZE);
        cache = CacheBuilder.newBuilder().concurrencyLevel(1).expireAfterWrite(
                RPC_RESPONSE_EXPIRATION, TimeUnit.MINUTES) .removalListener(REMOVAL_LISTENER).build();
        adapter.setResponseCache(cache);
        ec.setReadOnly();
        counter = 0;
        adapter.barrier(barrierInput);
        adapter.echo(echoInput);
        ec.runPendingTasks();
        Assert.assertEquals("Wrong - write to readonly channel",0, counter);
        ec.setWritable();
        adapter.echoReply(echoReplyInput);
        adapter.echo(echoInput);
        ec.runPendingTasks();
        Assert.assertEquals("Wrong - write to writtable channel",4, counter);
    }

    /**
     * Channel Handler for testing.
     * @author madamjak
     */
    private static final class EmbededChannelHandler extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(final ChannelHandlerContext ctx, final Object msg,
                final ChannelPromise promise) {
            if (msg instanceof MessageListenerWrapper) {
                counter++;
            }
        }
    }

    /**
     * Class for testing - channel can change state to read only or writable.
     * @author madamjak
     */
    private static final class ChangeWritableEmbededChannel extends EmbeddedChannel {
        private boolean isWrittable;

        ChangeWritableEmbededChannel(final ChannelHandler channelHandler) {
            super(channelHandler);
            setReadOnly();
        }

        @Override
        public boolean isWritable() {
            return isWrittable;
        }

        public void setWritable() {
            isWrittable = true;
        }

        public void setReadOnly() {
            isWrittable = false;
        }
    }
}
