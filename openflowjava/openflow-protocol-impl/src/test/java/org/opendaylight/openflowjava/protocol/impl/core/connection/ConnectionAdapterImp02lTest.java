/*
 * Copyright (c) 2014 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowjava.protocol.impl.core.connection;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.BarrierInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.EchoReplyInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.ExperimenterInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetQueueConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GroupModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MeterModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PacketOutInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.PortModInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.RoleRequestInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetAsyncInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.SetConfigInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.TableModInput;

/**
 * @author madamjak
 * @author michal.polkorab
 */
public class ConnectionAdapterImp02lTest {
    private static final int RPC_RESPONSE_EXPIRATION = 1;
    private static final RemovalListener<RpcResponseKey, ResponseExpectedRpcListener<?>> REMOVAL_LISTENER =
            new RemovalListener<RpcResponseKey, ResponseExpectedRpcListener<?>>() {
        @Override
        public void onRemoval(
                final RemovalNotification<RpcResponseKey, ResponseExpectedRpcListener<?>> notification) {
            notification.getValue().discard();
        }
    };

    @Mock EchoInput echoInput;
    @Mock BarrierInput barrierInput;
    @Mock EchoReplyInput echoReplyInput;
    @Mock ExperimenterInput experimenterInput;
    @Mock FlowModInput flowModInput;
    @Mock GetConfigInput getConfigInput;
    @Mock GetFeaturesInput getFeaturesInput;
    @Mock GetQueueConfigInput getQueueConfigInput;
    @Mock GroupModInput groupModInput;
    @Mock HelloInput helloInput;
    @Mock MeterModInput meterModInput;
    @Mock PacketOutInput packetOutInput;
    @Mock MultipartRequestInput multipartRequestInput;
    @Mock PortModInput portModInput;
    @Mock RoleRequestInput roleRequestInput;
    @Mock SetConfigInput setConfigInput;
    @Mock TableModInput tableModInput;
    @Mock GetAsyncInput getAsyncInput;
    @Mock SetAsyncInput setAsyncInput;
    private ConnectionAdapterImpl adapter;
    private Cache<RpcResponseKey, ResponseExpectedRpcListener<?>> cache;
    private OfHeader responseOfCall;
    /**
     * Initialize mocks
     */
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }
    /**
     * Disconnect adapter
     */
    @After
    public void tierDown(){
        if (adapter != null && adapter.isAlive()) {
            adapter.disconnect();
        }
    }
    /**
     * Test Rpc Calls
     */
    @Test
    public void testRcp() {
        final EmbeddedChannel embChannel = new EmbeddedChannel(new EmbededChannelHandler());
        adapter = new ConnectionAdapterImpl(embChannel, InetSocketAddress.createUnresolved("localhost", 9876), true);
        cache = CacheBuilder.newBuilder().concurrencyLevel(1).expireAfterWrite(RPC_RESPONSE_EXPIRATION, TimeUnit.MINUTES)
                .removalListener(REMOVAL_LISTENER).build();
        adapter.setResponseCache(cache);
        // -- barrier
        adapter.barrier(barrierInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - barrier", barrierInput, responseOfCall);
        // -- echo
        adapter.echo(echoInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - echo", echoInput, responseOfCall);
        // -- echoReply
        adapter.echoReply(echoReplyInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - echoReply",echoReplyInput, responseOfCall);
        // -- experimenter
        adapter.experimenter(experimenterInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - experimenter",experimenterInput, responseOfCall);
        // -- flowMod
        adapter.flowMod(flowModInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - flowMod", flowModInput, responseOfCall);
        // -- getConfig
        adapter.getConfig(getConfigInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - getConfig", getConfigInput, responseOfCall);
        // -- getFeatures
        adapter.getFeatures(getFeaturesInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - getFeatures",getFeaturesInput, responseOfCall);
        // -- getQueueConfig
        adapter.getQueueConfig(getQueueConfigInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - getQueueConfig",getQueueConfigInput, responseOfCall);
        // -- groupMod
        adapter.groupMod(groupModInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - groupMod", groupModInput, responseOfCall);
        // -- hello
        adapter.hello(helloInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - helloInput",helloInput, responseOfCall);
        // -- meterMod
        adapter.meterMod(meterModInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - meterMod",meterModInput, responseOfCall);
        // -- packetOut
        adapter.packetOut(packetOutInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - packetOut",packetOutInput, responseOfCall);
        // -- multipartRequest
        adapter.multipartRequest(multipartRequestInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - multipartRequest", multipartRequestInput, responseOfCall);
        // -- portMod
        adapter.portMod(portModInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - portMod", portModInput, responseOfCall);
        // -- roleRequest
        adapter.roleRequest(roleRequestInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - roleRequest", roleRequestInput, responseOfCall);
        // -- setConfig
        adapter.setConfig(setConfigInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - setConfig",setConfigInput, responseOfCall);
        // -- tableMod
        adapter.tableMod(tableModInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - tableMod", tableModInput, responseOfCall);
        // -- getAsync
        adapter.getAsync(getAsyncInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - getAsync", getAsyncInput, responseOfCall);
        // -- setAsync
        adapter.setAsync(setAsyncInput);
        embChannel.runPendingTasks();
        Assert.assertEquals("Wrong - setAsync", setAsyncInput, responseOfCall);
        adapter.disconnect();
    }

    /**
     * Channel Handler for testing
     * @author madamjak
     *
     */
    private class EmbededChannelHandler extends ChannelOutboundHandlerAdapter {
        @Override
        public void write(final ChannelHandlerContext ctx, final Object msg,
                final ChannelPromise promise) throws Exception {
            responseOfCall = null;
            if(msg instanceof MessageListenerWrapper){
                final MessageListenerWrapper listener = (MessageListenerWrapper) msg;
                final OfHeader ofHeader = listener.getMsg();
                responseOfCall = ofHeader;
            }
        }
    }
}
